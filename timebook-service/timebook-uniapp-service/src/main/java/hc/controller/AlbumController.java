package hc.controller;



import hc.service.AlbumService;
import hc.uniapp.album.dtos.SaveAlbumDto;
import hc.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/album")
@Api(value="相册管理",tags="uniapp相册管理")
public class AlbumController {
    @Resource
    private AlbumService albumService;

    @GetMapping("/getAll")
    @ApiOperation("查看所有相册")
    public ResponseResult findList(){
        return albumService.getAll();
    }

    @PostMapping("/save")
    @ApiOperation("新增相册")
    public ResponseResult saveAlbum(@RequestBody SaveAlbumDto saveAlbumDto){
        return albumService.saveAlbum(saveAlbumDto);
    }

    @PutMapping("/update")
    @ApiOperation("修改相册")
    public ResponseResult updateAlbum(@RequestParam String albumId,@RequestParam String albumName){
        return albumService.updateAlbum(albumId,albumName);
    }
    @PutMapping("/delete")
    @ApiOperation("删除相册")
    public ResponseResult deleteAlbum(@RequestParam  String albumId){
        return albumService.deleteAlbum(albumId);
    }
}
