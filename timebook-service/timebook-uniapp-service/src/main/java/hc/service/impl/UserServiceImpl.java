package hc.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hc.apis.sensitive.ISensitiveClient;
import hc.common.customize.Sensitive;
import hc.common.exception.CustomizeException;
import hc.common.exception.DataException;
import hc.common.exception.ParamErrorException;

import hc.mapper.UserMapper;
import hc.service.AlbumService;
import hc.service.UserService;

import hc.common.dtos.ResponseResult;
import hc.thread.UserHolder;
import hc.uniapp.user.dtos.UserDto;
import hc.uniapp.user.pojos.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static hc.LogUtils.error;
import static hc.LogUtils.info;
import static hc.RequestUtils.doGet;

import static hc.common.constants.RedisConstants.LOGIN_USER_KEY;
import static hc.common.constants.RedisConstants.LOGIN_USER_TTL;
import static hc.common.enums.AppHttpCodeEnum.*;
import static hc.constants.WxConstants.*;

@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ISensitiveClient sensitiveClient;


    @Resource
    private AlbumService albumService;
    @Override
    public ResponseResult login(User user) {
        if(StrUtil.isBlank(user.getOpenId())||StrUtil.isBlank(user.getNickName())){
            error("登录失败");
            throw new ParamErrorException(NO_FOUND_PARAM);
        }
        User isExist=query().eq("open_id",user.getOpenId()).one();
        Sensitive sensitive=new Sensitive();
        if(isExist==null) {
            sensitive.setSensitives(user.getNickName());
            ResponseResult result = sensitiveClient.checkIsSensitive(sensitive);
            if(result.getCode()!=SUCCESS.getCode())
                return result;
            save(user);
            user = query().eq("open_id", user.getOpenId()).one();
            albumService.createDefaultAlbum(user.getUserId());
        }else {
            BeanUtil.copyProperties(isExist, user);
        }
        UserDto userDto= BeanUtil.copyProperties(user, UserDto.class);
        info("登录成功");

        saveRedis(userDto,userDto.getUserId());

        return ResponseResult.okResult(userDto);
    }

    public void saveRedis(UserDto userDto,String userId){
        //7.2，将user对象转为Hash存储
        Map<String, Object> userMap = BeanUtil.beanToMap(userDto,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName,fieldValue)->{
                            if(fieldValue==null)
                                fieldValue='0';
                            else
                                fieldValue=fieldValue.toString();
                            return fieldValue;
                        }));
        //7.3，存储
        String userIdKey= String.format("%s%s", LOGIN_USER_KEY, userId);
        stringRedisTemplate.opsForHash().putAll(userIdKey,userMap);
        stringRedisTemplate.expire(userIdKey,LOGIN_USER_TTL, TimeUnit.MINUTES);
    }

    @Override
    public ResponseResult getOpenId(String code) {
        if(StrUtil.isBlank(code)) {
            error("不能获取openId");
            throw new ParamErrorException(NO_FOUND_PARAM);
        }
        Map<String,String> param = new HashMap<>();
        param.put("appid",APP_ID);
        param.put("secret",APP_SECRET);
        param.put("js_code", OPEN_ID_URL);
        param.put("grant_type","authorization_code");
        String result;
        try{
            result = doGet(OPEN_ID_URL,param);
        }catch(Exception e){
            e.printStackTrace();
            throw new CustomizeException("获取用户OPEN_ID_URL错误 :"+e.toString());
        }
        info(result);
        JSONObject data = (JSONObject) JSON.parse(result);
        if(data.get("errcode")!=null){
            throw new CustomizeException("session失效");
        }
        String openid = data.getString("openid");
        if(StrUtil.isBlank(openid)) throw new CustomizeException("未获取到openid");
        return ResponseResult.okResult(openid);
    }

    @Override
    public ResponseResult updateInfo(User user) {
        if(StrUtil.isBlank(UserHolder.getUser().getUserId())){
            error("修改失败");
            throw new ParamErrorException(NO_FOUND_PARAM);
        }
        User org = getById(UserHolder.getUser().getUserId());
        if(org==null){
            error("修改用户信息失败");
            throw new DataException(DATA_NOT_EXIST);
        }
        ResponseResult checkResult=new ResponseResult<>();
        Sensitive sensitive=new Sensitive();
        if(!StrUtil.isBlank(user.getNickName())) {
            sensitive.setSensitives(user.getNickName());
            checkResult=sensitiveClient.checkIsSensitive(sensitive);
            if(checkResult.getCode()!=SUCCESS.getCode()){
                return checkResult;
            }
            org.setNickName(user.getNickName());
        }
        if(!StrUtil.isBlank(user.getAvatarUrl())) {
            org.setAvatarUrl(user.getAvatarUrl());
        }
        updateById(org);
        return ResponseResult.okResult(SUCCESS.getCode(),"修改成功");
    }

    @Override
    public ResponseResult findOne(String userId) {
        if(StrUtil.isBlank(userId)){
            info("userId为空");
            throw new ParamErrorException(NO_FOUND_PARAM);
        }
        User user=getById(userId);
        UserDto userDto=BeanUtil.copyProperties(user,UserDto.class);
        return ResponseResult.okResult(userDto);
    }

}
