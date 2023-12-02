package hc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hc.uniapp.album.dtos.SaveAlbumDto;
import hc.uniapp.album.pojos.Album;
import hc.common.dtos.ResponseResult;


public interface AlbumService extends IService<Album> {
    Boolean albumIsExists(String albumId);

    ResponseResult getAll();

    ResponseResult saveAlbum(SaveAlbumDto saveAlbumDto);

    Boolean setCover(String albumId, String imageUrl);

    Boolean createDefaultAlbum(String userId);

    ResponseResult updateAlbum(String albumId, String albumName);

    ResponseResult deleteAlbum(String albumId);

     String getRedisCacheAlbumId(String id);
}
