package hc.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hc.apis.sensitive.ISensitiveClient;
import hc.common.customize.RedisCacheClient;
import hc.common.customize.Sensitive;
import hc.common.exception.ParamErrorException;
import hc.mapper.AlbumMapper;
import hc.service.AlbumService;
import hc.service.ImageAlbumService;
import hc.thread.UserHolder;
import hc.uniapp.album.dtos.SaveAlbumDto;
import hc.uniapp.album.pojos.Album;
import hc.common.dtos.ResponseResult;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static hc.LogUtils.info;
import static hc.common.constants.RedisConstants.*;
import static hc.common.enums.AppHttpCodeEnum.*;

@Service
@Transactional
public class AlbumServiceImpl extends ServiceImpl<AlbumMapper, Album> implements AlbumService {
    private static final int DEFAULT_ALBUM_TYPE=0;

    @Resource
    private ISensitiveClient sensitiveClient;

    @Resource
    private ImageAlbumService imageAlbumService;

    @Resource
    private RedisCacheClient redisCacheClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Boolean albumIsExists(String albumId) {
        if(StrUtil.isBlank(albumId)){
            info("albumId缺失");
            throw new ParamErrorException(NO_FOUND_PARAM);
        }
        Album album=redisCacheClient.queryWithPassThrough(CACHE_ALBUM_KEY,albumId
                ,Album.class,this::getById,CACHE_ALBUM_TTL, TimeUnit.DAYS);
        if(album!=null)
            return true;
        return false;
    }

    @Override
    public ResponseResult getAll() {
        List<Album> albumList=query().eq("user_id", UserHolder.getUser().getUserId())
                .orderByAsc("update_time").list();
        if(CollUtil.isEmpty(albumList))
            return ResponseResult.okResult(SUCCESS.getCode(),"无数据");
        List<Album> albumResult=albumSortByDesc(albumList);
        return ResponseResult.okResult(albumResult);
    }

    @Override
    public ResponseResult saveAlbum(SaveAlbumDto saveAlbumDto) {
        if(StrUtil.isBlank(saveAlbumDto.getAlbumName())){
            info("新增相册失败");
            throw new ParamErrorException(NO_FOUND_PARAM);
        }
        Album isExist = query().eq("album_name", saveAlbumDto.getAlbumName())
                .eq("user_id", UserHolder.getUser().getUserId()).one();
        if(isExist!=null)
            return ResponseResult.errorResult(DATA_EXIST,"相册名已存在");
        Album album=new Album();
        Sensitive sensitive=new Sensitive();
        ResponseResult result = sensitiveClient.checkIsSensitive(sensitive.setSensitives(saveAlbumDto.getAlbumName()));
        if(result.getCode()!=SUCCESS.getCode())
            return result;
        album.setUserId(saveAlbumDto.getUserId());
        album.setAlbumName(saveAlbumDto.getAlbumName());
        album.setType(10);
        save(album);
        return ResponseResult.okResult(album);
    }

    @Override
    public Boolean setCover(String albumId, String imageUrl) {
        Album album=redisCacheClient.queryWithPassThrough(CACHE_ALBUM_KEY,albumId,Album.class,
                this::getById,CACHE_ALBUM_TTL,TimeUnit.DAYS);
        stringRedisTemplate.delete(CACHE_ALBUM_KEY+albumId);
        if(album==null){
            info("无法设置封面，不存在该相册");
           return false;
        }
        album.setImageUrl(imageUrl);
        return updateById(album);
    }

    @Override
    public Boolean createDefaultAlbum(String userId) {
        Album album=new Album();
        album.setUserId(userId);
        album.setType(0);
        album.setAlbumName("全部");
        boolean flag=save(album);
        return flag;
    }

    @Override
    public ResponseResult updateAlbum(String albumId, String albumName) {
        Album album=redisCacheClient.queryWithPassThrough(CACHE_ALBUM_KEY,albumId,Album.class,
                this::getById,CACHE_ALBUM_TTL,TimeUnit.DAYS);
        if(album==null)
            return ResponseResult.errorResult(DATA_NOT_EXIST,"没有该相册");
        Sensitive sensitive=new Sensitive();
        ResponseResult result = sensitiveClient.checkIsSensitive(sensitive.setSensitives(albumName));
        if(result.getCode()!=SUCCESS.getCode())
            return result;
        album.setAlbumName(albumName);
        if(album.getType()!=DEFAULT_ALBUM_TYPE) {
            stringRedisTemplate.delete(CACHE_ALBUM_KEY+albumId);
            updateById(album);
            return ResponseResult.okResult("修改成功");
        }else
            return ResponseResult.errorResult(DATA_EXIST,"默认不可修改");
    }

    @Override
    public ResponseResult deleteAlbum(String albumId) {
        if(StrUtil.isBlank(albumId)){
            info("albumId为空，不可删除");
            throw new ParamErrorException(NO_FOUND_PARAM);
        }
        Album album=redisCacheClient.queryWithPassThrough(CACHE_ALBUM_KEY,albumId,Album.class,
                this::getById,CACHE_ALBUM_TTL,TimeUnit.DAYS);
        if(album!=null) {
            if (album.getType() != DEFAULT_ALBUM_TYPE) {
                stringRedisTemplate.delete(CACHE_ALBUM_KEY + albumId);
                removeById(albumId);
                imageAlbumService.deleteOtherAlbum(albumId);
                return ResponseResult.okResult(200, "删除成功");
            } else
                return ResponseResult.errorResult(DATA_EXIST, "默认不可删除");
        } else {
            return ResponseResult.errorResult(DATA_NOT_EXIST, "网络出现问题，请稍后访问");
        }
    }

    public String getRedisCacheAlbumId(String userId){
        String key= DEFAULT_ALBUM_KEY+userId;
        String json=stringRedisTemplate.opsForValue().get(key);
        //2.判断是否存在
        if(StrUtil.isNotBlank(json)){
            //3.存在，直接返回
            return json;
        }
        //判断命中的是否是空值
        if(json!=null){
            return null;
        }
        //4.不存在，根据id查询数据库
        Album album=query().eq("user_id",UserHolder.getUser().getUserId())
                .eq("type",DEFAULT_ALBUM_TYPE).one();
        if(StrUtil.isNotBlank(album.getAlbumId()))
            stringRedisTemplate.opsForValue().set(key,album.getAlbumId(),DEFAULT_ALBUM_TTL,TimeUnit.DAYS);
        else
            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
        return album.getAlbumId();
    }

    private List<Album> albumSortByDesc(List<Album> list){
        if(CollUtil.isEmpty(list))
            return null;
        Album defaultAlbum=null;
        int removeId=0;
        Collections.sort(list, new Comparator<Album>() {
            @Override
            public int compare(Album a1, Album a2) {
                return a2.getCreateTime().compareTo(a1.getCreateTime());
            }
        });
        for(Album album:list){
            if(album.getType()==0) {
                defaultAlbum = album;
                removeId=list.indexOf(album);
            }
        }
        if(defaultAlbum!=null) {
            list.remove(removeId);
            list.add(0, defaultAlbum);
        }
        return list;
    }
}
