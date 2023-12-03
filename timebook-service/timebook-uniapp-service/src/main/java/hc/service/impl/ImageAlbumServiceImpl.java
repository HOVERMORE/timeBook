package hc.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import hc.common.customize.RedisCacheClient;

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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static hc.LogUtils.info;
import static hc.common.constants.RedisConstants.*;
import static hc.common.enums.AppHttpCodeEnum.*;
;


@Service
@Transactional
public class ImageAlbumServiceImpl extends ServiceImpl<ImageAlbumMapper, ImageAlbumDto> implements ImageAlbumService {
    @Resource
    private ImagesService imagesService;
    @Resource
    private AlbumService albumService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisCacheClient redisCacheClient;
    private static final ExecutorService CACHE_REBUILD_EXECUTOR= Executors.newFixedThreadPool(10);

    @Override
    public ResponseResult updateImageToAlbum(ImageDto imageDto) {
        int type=imageDto.getType();
        String defaultAlbumId=albumService.getRedisCacheAlbumId(UserHolder.getUser().getUserId());
        ResponseResult result;
        if(imageDto.getUpdateAlbumId().equals(defaultAlbumId)){
            return ResponseResult.errorResult(SERVER_ERROR,"不能移动或复制到该相册");
        }else if(imageDto.getOrgAlbumId().equals(defaultAlbumId)){
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
        List<Image> imageList = new ArrayList<>();
        if (CollUtil.isNotEmpty(imageAlbumDtoList)) {
            for (ImageAlbumDto imageAlbumDto : imageAlbumDtoList) {
                Image image = redisCacheClient.queryWithPassThrough(CACHE_IMAGE_KEY, imageAlbumDto.getImageId()
                        , Image.class, imagesService::getById, CACHE_IMAGE_TTL, TimeUnit.DAYS);
                imageList.add(image);
            }
        }
        return ResponseResult.okResult(imageList);
    }

    @Override
    public ResponseResult deleteAllAlbumPhotos(String imageId) {
        List<ImageAlbumDto> list = query().eq("image_id", imageId).eq("user_id", UserHolder.getUser().getUserId()).list();
        List<String> ids=new ArrayList<>();
        if(CollUtil.isEmpty(list))
            return ResponseResult.okResult(SUCCESS.getCode(),"无数据");
        for(ImageAlbumDto imageAlbumDto:list){
            ids.add(imageAlbumDto.getImageAlbumId());
            stringRedisTemplate.delete(CACHE_ALBUM_IMAGE_DTO_KEY+imageAlbumDto.getImageAlbumId()+imageId);
        }
        removeByIds(ids);
        return ResponseResult.okResult("删除成功");
    }

    @Override
    public ResponseResult deleteOtherAlbumPhotos(String imageId, String albumId) {
        ImageAlbumDto imageAlbumDto=queryWithLogicalExpire(albumId, imageId);
        stringRedisTemplate.delete(CACHE_ALBUM_IMAGE_DTO_KEY+imageAlbumDto.getAlbumId()+imageAlbumDto.getImageId());
        boolean isRemove = removeById(imageAlbumDto.getImageAlbumId());
        if(isRemove) {
            List<ImageAlbumDto> list = query().eq("album_id", albumId)
                    .eq("user_id", UserHolder.getUser().getUserId())
                    .orderByDesc("update_time").list();
            Image image =new Image();
            Album album =new Album();
            if(CollUtil.isNotEmpty(list)) {
                ImageAlbumDto latest = list.get(0);
                image=redisCacheClient.queryWithPassThrough(CACHE_IMAGE_KEY,latest.getAlbumId()
                        ,Image.class,imagesService::getById,CACHE_IMAGE_TTL,TimeUnit.DAYS);
                album=redisCacheClient.queryWithPassThrough(CACHE_IMAGE_KEY,albumId
                        ,Album.class,albumService::getById,CACHE_IMAGE_TTL,TimeUnit.DAYS);
                album.setImageUrl(image.getImageUrl());
            }else {
                album.setAlbumId(albumId);
                album.setImageUrl("");
            }
            stringRedisTemplate.delete(CACHE_ALBUM_KEY+album.getAlbumId());
            albumService.updateById(album);
            return ResponseResult.okResult("删除成功");
        }
        else
            return ResponseResult.errorResult(SERVER_ERROR,"删除失败");
    }

    @Override
    public ResponseResult saveAlbum(Image image, String albumId) {
        String defaultAlbumId=albumService.getRedisCacheAlbumId(UserHolder.getUser().getUserId());
        if(StrUtil.isBlank(albumId)){
            albumId=defaultAlbumId;
        }
        albumService.setCover(albumId,image.getImageUrl());
        if(defaultAlbumId.equals(albumId)){
            return ResponseResult.okResult();
        }
        ImageAlbumDto one=queryWithLogicalExpire(albumId,image.getImageId());
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
        if(CollUtil.isEmpty(list))
            return ResponseResult.okResult(SUCCESS.getCode(),"已无相册，不可删除");
        for(ImageAlbumDto imageAlbumDto:list){
            ids.add(imageAlbumDto.getImageAlbumId());
            stringRedisTemplate.delete(CACHE_ALBUM_IMAGE_DTO_KEY+imageAlbumDto.getImageAlbumId()
                    +imageAlbumDto.getImageId());
        }
        boolean flag=removeByIds(ids);
        if(flag){
            return ResponseResult.okResult("删除成功");
        }else
            return ResponseResult.errorResult(SERVER_ERROR,"服务出错");
    }

    private ResponseResult move(ImageDto imageDto){
        ImageAlbumDto imageAlbumDto=queryWithLogicalExpire(imageDto.getUpdateAlbumId(), imageDto.getImageId());
        if(imageAlbumDto!=null||imageDto.getOrgAlbumId().equals(imageDto.getUpdateAlbumId()))
            return ResponseResult.errorResult(DATA_EXIST,"不能移动，已存在该相册");
        ImageAlbumDto one=queryWithLogicalExpire(imageDto.getOrgAlbumId(), imageDto.getImageId());
        one.setAlbumId(imageDto.getUpdateAlbumId());
        stringRedisTemplate.delete(CACHE_ALBUM_IMAGE_DTO_KEY+one.getImageAlbumId()+one.getImageId());
        updateById(one);
        Image image=redisCacheClient.queryWithPassThrough(CACHE_IMAGE_KEY,imageDto.getImageId()
                ,Image.class,imagesService::getById,CACHE_IMAGE_TTL,TimeUnit.DAYS);
        albumService.setCover(imageDto.getUpdateAlbumId(),image.getImageUrl());
        return ResponseResult.okResult("移动成功");
    }
    private ResponseResult copy(ImageDto imageDto){
        ImageAlbumDto imageAlbumDto=queryWithLogicalExpire(imageDto.getUpdateAlbumId(), imageDto.getImageId());
        if(imageAlbumDto!=null||imageDto.getOrgAlbumId().equals(imageDto.getUpdateAlbumId()))
            return ResponseResult.errorResult(DATA_EXIST,"不能复制，已存在该相册");
        imageAlbumDto=new ImageAlbumDto();
        imageAlbumDto.setUserId(UserHolder.getUser().getUserId());
        imageAlbumDto.setImageId(imageDto.getImageId());
        imageAlbumDto.setAlbumId(imageDto.getUpdateAlbumId());
        save(imageAlbumDto);
        Image image=redisCacheClient.queryWithPassThrough(CACHE_IMAGE_KEY,imageDto.getImageId()
                ,Image.class,imagesService::getById,CACHE_IMAGE_TTL,TimeUnit.DAYS);
        albumService.setCover(imageDto.getUpdateAlbumId(),image.getImageUrl());
        return ResponseResult.okResult("复制成功");
    }

    public ImageAlbumDto queryWithLogicalExpire(String albumId,String imageId) {
        String key= CACHE_ALBUM_IMAGE_DTO_KEY+albumId+imageId;
        String json=stringRedisTemplate.opsForValue().get(key);
        //2.判断是否存在
        if(StrUtil.isNotBlank(json)){
            //3.存在，直接返回
            return  JSONUtil.toBean(json, ImageAlbumDto.class);
        }
        //4.不存在，根据id查询数据库
        ImageAlbumDto one = query().eq("album_id", albumId)
                .eq("image_id", imageId)
                .eq("user_id", UserHolder.getUser().getUserId()).one();
        if(one!=null)
            stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(one),CACHE_ALBUM_IMAGE_DTO_TTL,TimeUnit.DAYS);
        return one;
    }
}
