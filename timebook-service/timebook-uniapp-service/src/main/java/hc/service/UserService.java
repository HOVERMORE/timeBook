package hc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hc.common.dtos.ResponseResult;
import hc.uniapp.user.pojos.User;

public interface UserService extends IService<User> {
    /**
     * 用户登录功能
     * @param user
     * @return
     */
    ResponseResult login(User user);

    /**
     * 获取openId
     * @param code
     * @return
     */
    ResponseResult getOpenId(String code);

    /**
     * 修改用户信息
     * @param user
     * @return
     */
    ResponseResult updateInfo(User user);

    /**
     * 查看用户信息
     * @param userId
     * @return
     */
    ResponseResult findOne(String userId);
}
