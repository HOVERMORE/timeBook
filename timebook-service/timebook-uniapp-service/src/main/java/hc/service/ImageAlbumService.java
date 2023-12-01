package hc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hc.common.dtos.ResponseResult;
import hc.uniapp.customize.ImageAlbumDto;
import hc.uniapp.image.dtos.ImageDto;
import hc.uniapp.image.pojos.Image;


import java.util.List;

public interface ImageAlbumService extends IService<ImageAlbumDto> {

   ResponseResult updateImageToAlbum(ImageDto imageDto);

   ResponseResult<List<Image>> getOtherAlbum(String albumId);
   ResponseResult deleteAllAlbumPhotos(String imageId);

   ResponseResult deleteOtherAlbumPhotos(String imageId, String albumId);

   ResponseResult saveAlbum(Image image, String albumId);

   ResponseResult deleteOtherAlbum(String albumId);
}
