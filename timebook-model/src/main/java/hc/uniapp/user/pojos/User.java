package hc.uniapp.user.pojos;


import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户类
 */
@Data
@TableName("tb_user")
public class User implements Serializable {
    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户Id",required = true)
    @TableId(type = IdType.ASSIGN_ID)
    private String userId;
    /**
     * 用户微信openId
     */
    @ApiModelProperty(value = "用户微信openId",required = true)
    private String openId;
    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称",required = true)
    private String nickName;
    /**
     * 图片地址
     */
    @ApiModelProperty(value = "图片地址",required = false)
    private String avatarUrl;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @TableField(fill= FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 逻辑删除
     */
    private Integer isDeleted;
}
