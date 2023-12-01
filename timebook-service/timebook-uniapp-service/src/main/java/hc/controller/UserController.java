package hc.controller;


import hc.service.UserService;

import hc.common.dtos.ResponseResult;
import hc.uniapp.user.pojos.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
@Api(value="用户登录",tags="uniapp用户登录")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户登录
     * @param user
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public ResponseResult login(@RequestBody User user){
        return userService.login(user);
    }


    /**
     * 获取openId
     * @param code
     * @return
     */
    @GetMapping("/getOpenId")
    @ApiOperation("用户获取openId")
    public ResponseResult getWechatUserId(@RequestParam(value="code")String code){
        return userService.getOpenId(code);
    }

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    @PutMapping("/update")
    @ApiOperation("修改信息")
    public ResponseResult updateInfo(@RequestBody User user){
        return userService.updateInfo(user);
    }

    /**
     * 查看用户信息
     * @param userId
     * @return
     */
    @GetMapping("/getOne")
    @ApiOperation("查看用户信息")
    public ResponseResult findOne(@RequestParam String userId){
        return userService.findOne(userId);
    }
}
