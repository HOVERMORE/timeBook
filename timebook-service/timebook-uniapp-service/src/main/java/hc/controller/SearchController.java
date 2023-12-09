package hc.controller;


import hc.service.SearchService;
import hc.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/search")
@Api(value="搜索管理",tags="uniapp搜索管理")
public class SearchController {
    @Resource
    private SearchService searchService;
    @GetMapping("/albumOrImage/searchAll")
    @ApiOperation("查询所有相册或者图片")
    public ResponseResult searchAlbumOrImage(@RequestParam String content){
        return searchService.searchAlbumOrImage(content);
    }

    @GetMapping("/note/searchAll")
    @ApiOperation("查询日记")
    public ResponseResult searchNote(@RequestParam String content){
        return searchService.searchNote(content);
    }

    @GetMapping("/note/suggestion")
    @ApiOperation("自动补全搜索日记")
    public ResponseResult searchSuggestion(@RequestParam String prefix){
        return searchService.searchSuggestion(prefix);
    }
}
