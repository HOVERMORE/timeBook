package hc.service;


import com.baomidou.mybatisplus.extension.service.IService;
import hc.common.dtos.ResponseResult;
import hc.uniapp.image.dtos.ImageDto;
import hc.uniapp.image.pojos.Image;
import org.springframework.web.multipart.MultipartFile;


public interface ImagesService extends IService<Image> {
    /**
     * 上传图片
     *
     * @param multipartFile
     * @param albumId
     * @return
     */
    ResponseResult saveImage(MultipartFile multipartFile, String albumId);

    /**
     * 查询相册的所有图片
     * @param albumId
     * @return
     */
    ResponseResult getAll(String albumId);

    ResponseResult findOne(String imageId);

    ResponseResult updateInfo(ImageDto imageDto);

    ResponseResult deleteImage(String imageId,String albumId);

    ResponseResult getImagesBytime();
}
