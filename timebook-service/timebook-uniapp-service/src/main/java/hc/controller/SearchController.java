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
    @GetMapping("/searchAll")
    @ApiOperation("查询所有")
    public ResponseResult searchAll(@RequestParam String context){
        return searchService.searchAll(context);
    }
}
