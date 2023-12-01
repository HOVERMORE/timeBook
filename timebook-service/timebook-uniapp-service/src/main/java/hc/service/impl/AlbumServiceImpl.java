package hc.service.impl;


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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static hc.LogUtils.info;
import static hc.common.constants.RedisConstants.CACHE_ALBUM_KEY;
import static hc.common.constants.RedisConstants.CACHE_ALBUM_TTL;
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
                ,Album.class,this::getById,CACHE_ALBUM_TTL, TimeUnit.MINUTES);
        if(album!=null)
            return true;
        return false;
    }

    @Override
    public ResponseResult getAll() {
        List<Album> albumList=query().eq("user_id", UserHolder.getUser().getUserId())
                .orderByAsc("update_time").list();
        List<Album> albumResult=new ArrayList<>();
        Album defaultAlbum=query().eq("user_id", UserHolder.getUser().getUserId())
                .eq("type",DEFAULT_ALBUM_TYPE).one();
        albumResult.add(defaultAlbum);
        for(Album album:albumList){
            if(album.getType()==DEFAULT_ALBUM_TYPE)
                continue;
            albumResult.add(album);
        }
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
        Album album = getById(albumId);
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
        Album album=getById(albumId);
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
        Album album = getById(albumId);
        if(album.getType()!=DEFAULT_ALBUM_TYPE){
            removeById(albumId);
            imageAlbumService.deleteOtherAlbum(albumId);
            return ResponseResult.okResult(200,"删除成功");
        }
        return ResponseResult.errorResult(DATA_EXIST,"默认不可删除");
    }

}
