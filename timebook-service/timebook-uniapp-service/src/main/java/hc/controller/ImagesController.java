package hc.controller;


import hc.service.ImagesService;
import hc.common.dtos.ResponseResult;
import hc.uniapp.image.dtos.ImageDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/images")
@Api(value="图片管理",tags="uniapp图片管理")
public class ImagesController {
    @Resource
    private ImagesService imagesService;

    @PostMapping("/save")
    @ApiOperation("新增图片")
    public ResponseResult save(@RequestBody MultipartFile multipartFile, @RequestParam(value = "albumId") String albumId){
        return imagesService.saveImage(multipartFile,albumId);
    }

    @GetMapping("/getAll")
    @ApiOperation("查询所有图片")
    public ResponseResult findList(@RequestParam String albumId){
        return imagesService.getAll(albumId);
    }

    @GetMapping("/getOne")
    @ApiOperation("图片详情")
    public ResponseResult findOne(@RequestParam String imageId){
        return imagesService.findOne(imageId);
    }

    @PutMapping("/update")
    @ApiOperation("修改图片所在相册")
    public ResponseResult updateInfo(@RequestBody ImageDto imageDto){
        return imagesService.updateInfo(imageDto);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除图片")
    public ResponseResult deleteImage(String imageId,String albumId){
        return imagesService.deleteImage(imageId,albumId);
    }

    @GetMapping("/listByTime")
    @ApiOperation("查看近期相册")
    public ResponseResult getAlbumsBytime(){
        return imagesService.getImagesBytime();
    }
}
