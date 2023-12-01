package hc.service.impl;


import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hc.common.exception.CustomizeException;
import hc.common.exception.DataException;
import hc.common.exception.ParamErrorException;
import hc.common.exception.ServerErrorException;
import hc.file.service.MinIOFileService;

import hc.mapper.ImagesMapper;
import hc.service.AlbumService;
import hc.service.ImageAlbumService;
import hc.service.ImagesService;
import hc.thread.UserHolder;
import hc.uniapp.album.pojos.Album;
import hc.common.dtos.ResponseResult;
import hc.uniapp.image.dtos.ImageDto;
import hc.uniapp.image.dtos.RecentAlbumsDto;
import hc.uniapp.image.pojos.Image;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static hc.LogUtils.error;
import static hc.LogUtils.info;
import static hc.common.customize.JacksonObjectMapper.DEFAULT_DATE_TIME_FORMAT;
import static hc.common.enums.AppHttpCodeEnum.*;

@Service
@Transactional
public class ImagesServiceImpl extends ServiceImpl<ImagesMapper, Image> implements ImagesService {
    @Resource
    private MinIOFileService minIOFileService;

    @Resource
    private ImageAlbumService imageAlbumService;

    @Resource
    private AlbumService albumService;

    @Override
    public ResponseResult saveImage(MultipartFile multipartFile, String albumId) {
        if(multipartFile==null||multipartFile.getSize()==0) {
            info("图片参数缺失");
            throw new ParamErrorException(NO_FOUND_PARAM);
        }
        String fileName = UUID.randomUUID().toString().replace("-", "");
        String originalFilename = multipartFile.getOriginalFilename();
        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileId=null;
        try {
            info("图片开始上传...");
            fileId = minIOFileService.uploadImgFile("", fileName + postfix, multipartFile.getInputStream());
            info("图片上传完成，已保存到minIO中，fileId:{"+fileId+"}");
        } catch (IOException e) {
            e.printStackTrace();
            error("ImagesServiceImpl---上传图片失败");
            if(e.toString().contains("Maximum upload size exceeded"))
                return ResponseResult.errorResult(PICTURE_TOO_BIG);
        }
        Date currentDate=new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDate = dateFormat.format(currentDate);
        //3.保存到数据库中
        String imageName="IMG_"+formattedDate;
        Image image=new Image();
        image.setUserId(UserHolder.getUser().getUserId());
        image.setImageName(imageName);
        image.setImageUrl(fileId);
        save(image);
        imageAlbumService.saveAlbum(image,albumId);
        //4.返回结果
        return ResponseResult.okResult(image);
    }

    @Override
    public ResponseResult getAll(String albumId) {
        String DEFAULT_ALBUM=getDefaultAlbum();
        if(!albumService.albumIsExists(albumId)){
            info("相册不存在");
            throw new DataException(DATA_NOT_EXIST);
        }
        ResponseResult<List<Image>> result=new ResponseResult<>();
        if(albumId.equals(DEFAULT_ALBUM)) {
            List<Image> imageList = query().eq("user_id", UserHolder.getUser().getUserId()).orderByDesc("create_time").list();
            result.ok(imageList);
        }
        else
            result=imageAlbumService.getOtherAlbum(albumId);
        if(result==null){
            info("服务器出错");
            throw new ServerErrorException(SERVER_ERROR);
        }
        return result;
    }

    @Override
    public ResponseResult findOne(String imageId) {
        if(StrUtil.isBlank(imageId)){
            info("imageId缺失");
            throw new ParamErrorException(NO_FOUND_PARAM);
        }
        Image image = getById(imageId);
        return ResponseResult.okResult(image);
    }

    @Override
    public ResponseResult updateInfo(ImageDto imageDto) {
        if(imageDto==null||StrUtil.isBlank(imageDto.getImageId())
                ||StrUtil.isBlank(imageDto.getOrgAlbumId())||StrUtil.isBlank(imageDto.getUpdateAlbumId())){
            info("imageDto缺失，修改失败");
            throw new ParamErrorException(NO_FOUND_PARAM);
        }
        ResponseResult update = imageAlbumService.updateImageToAlbum(imageDto);
        return update;
    }

    @Transactional
    @Override
    public ResponseResult deleteImage(String imageId, String albumId) {
        String DEFAULT_ALBUM=getDefaultAlbum();
        if(StrUtil.isBlank(albumId)||StrUtil.isBlank(imageId)){
            info("参数缺失，删除图片失败");
            throw new ParamErrorException(NO_FOUND_PARAM);
        }
        if(albumId.equals(DEFAULT_ALBUM)){
            removeById(imageId);
            List<Image> list = query().eq("user_id", UserHolder.getUser().getUserId())
                    .orderByDesc("create_time").list();
            Image image = list.get(0);
            Album album = albumService.getById(DEFAULT_ALBUM);
            if(image!=null) {
                album.setImageUrl(image.getImageUrl());
                albumService.updateById(album);
            }else{
                album.setImageUrl("");
            }
            return imageAlbumService.deleteAllAlbumPhotos(imageId);
        }else {
            return imageAlbumService.deleteOtherAlbumPhotos(imageId,albumId);
        }
    }

    @Override
    public ResponseResult getImagesBytime() {
        String oneWeekAgoTime = getRecentTime(1,"week");
        String oneMonthAgoTime =  getRecentTime(1,"month");
        String threeMonthAgoTime = getRecentTime(3,"month");
        RecentAlbumsDto recentAlbumsDto=new RecentAlbumsDto();
        recentAlbumsDto.setRecentImages(getRecentImage(oneWeekAgoTime));
        recentAlbumsDto.setShortTimeImages(getRecentImage(oneMonthAgoTime));
        recentAlbumsDto.setLongTimeImages(getRecentImage(threeMonthAgoTime));
        return ResponseResult.okResult(recentAlbumsDto);
    }
    private String getRecentTime(int time,String type){
        LocalDateTime currentDateTime = LocalDateTime.now();
        if(type.equals("week")) {
            currentDateTime = currentDateTime.minusWeeks(time);
            return currentDateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT));
        }else if(type.equals("month")){
            currentDateTime = currentDateTime.minusMonths(time);
            return currentDateTime.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT));
        }else{
            error("无法获取当前时间");
            return "0";
        }
    }
    private String getDefaultAlbum(){
        return albumService.query().eq("user_id", UserHolder.getUser().getUserId())
                .eq("type",0).one().getAlbumId();
    }

    private List<Image> getRecentImage(String time){
        return  query().eq("user_id", UserHolder.getUser().getUserId())
                .gt("create_time", time).orderByAsc("create_time").list();
    }
}