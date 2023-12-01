package hc.uniapp.user.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserDto implements Serializable {
    @ApiModelProperty(value = "用户Id",required = true)
    private String userId;

    @ApiModelProperty(value = "用户微信openId",required = true)
    private String openId;

    @ApiModelProperty(value = "用户昵称",required = true)
    private String nickName;

    @ApiModelProperty(value = "图片地址",required = false)
    private String avatarUrl;
}
