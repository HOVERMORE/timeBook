package hc.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import hc.common.exception.ServerErrorException;

import hc.mapper.ImageAlbumMapper;
import hc.service.AlbumService;
import hc.service.ImageAlbumService;
import hc.service.ImagesService;
import hc.thread.UserHolder;
import hc.uniapp.album.pojos.Album;
import hc.common.dtos.ResponseResult;
import hc.uniapp.customize.ImageAlbumDto;
import hc.uniapp.image.dtos.ImageDto;
import hc.uniapp.image.pojos.Image;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static hc.LogUtils.info;
import static hc.common.enums.AppHttpCodeEnum.DATA_EXIST;
import static hc.common.enums.AppHttpCodeEnum.SERVER_ERROR;
;


@Service
@Transactional
public class ImageAlbumServiceImpl extends ServiceImpl<ImageAlbumMapper, ImageAlbumDto> implements ImageAlbumService {
    @Resource
    private ImagesService imagesService;

    @Resource
    private AlbumService albumService;

    @Override
    public ResponseResult updateImageToAlbum(ImageDto imageDto) {
        int type=imageDto.getType();
        String DEFAULT_ALBUM=getDefaultAlbum();
        ResponseResult result;
        if(imageDto.getUpdateAlbumId().equals(DEFAULT_ALBUM)){
            return ResponseResult.errorResult(SERVER_ERROR,"不能移动或复制到该相册");
        }else if(imageDto.getOrgAlbumId().equals(DEFAULT_ALBUM)){
            result=copy(imageDto);
        }else{
            if(type==0)
                result=move(imageDto);
            else
                result= copy(imageDto);
        }
        return result;
    }

    @Override
    public ResponseResult<List<Image>> getOtherAlbum(String albumId) {
        List<ImageAlbumDto> imageAlbumDtoList = query().eq("album_id", albumId)
                .eq("user_id", UserHolder.getUser().getUserId()).orderByDesc("update_time").list();
        List<Image> imageList=new ArrayList<>();
        for(ImageAlbumDto imageAlbumDto:imageAlbumDtoList){
            Image image = imagesService.getById(imageAlbumDto.getImageId());
            imageList.add(image);
        }
        return ResponseResult.okResult(imageList);
    }

    @Override
    public ResponseResult deleteAllAlbumPhotos(String imageId) {
        List<ImageAlbumDto> list = query().eq("image_id", imageId).eq("user_id", UserHolder.getUser().getUserId()).list();
        List<String> ids=new ArrayList<>();
        for(ImageAlbumDto imageAlbumDto:list){
            ids.add(imageAlbumDto.getImageAlbumId());
        }
        removeByIds(ids);
        return ResponseResult.okResult("删除成功");
    }

    @Override
    public ResponseResult deleteOtherAlbumPhotos(String imageId, String albumId) {
        ImageAlbumDto imageAlbumDto = getImageAlbumDto(albumId, imageId);
        boolean isRemove = removeById(imageAlbumDto.getImageAlbumId());
        if(isRemove) {
            List<ImageAlbumDto> list = query().eq("album_id", albumId)
                    .eq("user_id", UserHolder.getUser().getUserId())
                    .orderByDesc("update_time").list();
            ImageAlbumDto latest = list.get(0);
            Image image =new Image();
            Album album =new Album();
            if(latest!=null) {
                image = imagesService.getById(latest.getAlbumId());
                album = albumService.getById(albumId);
                album.setImageUrl(image.getImageUrl());
            }else {
                album.setImageUrl("");
            }
            albumService.updateById(album);
            return ResponseResult.okResult("删除成功");
        }
        else
            return ResponseResult.errorResult(SERVER_ERROR,"删除失败");
    }

    @Override
    public ResponseResult saveAlbum(Image image, String albumId) {
        String DEFAULT_ALBUM=getDefaultAlbum();
        if(StrUtil.isBlank(albumId)){
            albumId=DEFAULT_ALBUM;
        }
        albumService.setCover(albumId,image.getImageUrl());
        if(DEFAULT_ALBUM.equals(albumId)){
            return ResponseResult.okResult();
        }
        ImageAlbumDto one = getImageAlbumDto(albumId,image.getImageId());
        if(one!=null){
            info("相册已存在该照片");
            throw new ServerErrorException(DATA_EXIST);
        }
        ImageAlbumDto imageAlbumDto=new ImageAlbumDto();
        imageAlbumDto.setImageId(image.getImageId());
        imageAlbumDto.setUserId(UserHolder.getUser().getUserId());
        imageAlbumDto.setAlbumId(albumId);
        save(imageAlbumDto);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult deleteOtherAlbum(String albumId) {
        List<ImageAlbumDto> list = query().eq("album_id", albumId).eq("user_id", UserHolder.getUser().getUserId()).list();
        List<String> ids=new ArrayList<>();
        for(ImageAlbumDto imageAlbumDto:list){
            ids.add(imageAlbumDto.getImageAlbumId());
        }
        boolean flag=removeByIds(ids);
        if(flag){
            return ResponseResult.okResult("删除成功");
        }else
            return ResponseResult.errorResult(SERVER_ERROR,"服务出错");
    }

    public ResponseResult move(ImageDto imageDto){
        ImageAlbumDto imageAlbumDto=getImageAlbumDto(imageDto.getUpdateAlbumId(), imageDto.getImageId());
        if(imageAlbumDto!=null||imageDto.getOrgAlbumId().equals(imageDto.getUpdateAlbumId()))
            return ResponseResult.errorResult(DATA_EXIST,"不能移动，已存在该相册");
        ImageAlbumDto one = getImageAlbumDto(imageDto.getOrgAlbumId(), imageDto.getImageId());
        one.setAlbumId(imageDto.getUpdateAlbumId());
        updateById(one);
        Image image = imagesService.getById(imageDto.getImageId());
        albumService.setCover(imageDto.getUpdateAlbumId(),image.getImageUrl());
        return ResponseResult.okResult("移动成功");
    }
    public ResponseResult copy(ImageDto imageDto){
        ImageAlbumDto imageAlbumDto=getImageAlbumDto(imageDto.getUpdateAlbumId(), imageDto.getImageId());
        if(imageAlbumDto!=null||imageDto.getOrgAlbumId().equals(imageDto.getUpdateAlbumId()))
            return ResponseResult.errorResult(DATA_EXIST,"不能复制，已存在该相册");
        imageAlbumDto=new ImageAlbumDto();
        imageAlbumDto.setUserId(UserHolder.getUser().getUserId());
        imageAlbumDto.setImageId(imageDto.getImageId());
        imageAlbumDto.setAlbumId(imageDto.getUpdateAlbumId());
        save(imageAlbumDto);
        Image image = imagesService.getById(imageDto.getImageId());
        albumService.setCover(imageDto.getUpdateAlbumId(),image.getImageUrl());
        return ResponseResult.okResult("复制成功");
    }

    private String getDefaultAlbum(){
        return albumService.query().eq("user_id", UserHolder.getUser().getUserId())
                .eq("type",0).one().getAlbumId();
    }

    public ImageAlbumDto getImageAlbumDto(String albumId,String imageId){
        return query().eq("album_id", albumId)
                .eq("image_id", imageId)
                .eq("user_id", UserHolder.getUser().getUserId()).one();
    }
}
