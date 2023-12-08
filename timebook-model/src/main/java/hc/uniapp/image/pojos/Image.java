package hc.uniapp.image.pojos;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图片类
 */
@Data
@TableName("tb_image")
@ToString
public class Image implements Serializable {
    /**
     * 图片id
     */
    @ApiModelProperty(value = "图片id",required = true)
    @TableId(type = IdType.ASSIGN_ID)
    private String imageId;
    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户userId",required = true)
    private String userId;
    /**
     * 默认图片名
     */
    @ApiModelProperty(value="默认图片名",required = false)
    private String imageName;
    /**
     * 备注图片名
     */
    @ApiModelProperty(value="备注图片名",required = false)
    private String remark;
    /**
     * 图片地址
     */
    @ApiModelProperty(value="图片地址",required = true)
    private String imageUrl;
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
