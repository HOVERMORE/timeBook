package hc.uniapp.album.pojos;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_album")
public class Album  implements Serializable {
    /**
     * 相册id
     */
    @ApiModelProperty(value = "相册Id",required = true)
    @TableId(type = IdType.ASSIGN_ID)
    private String albumId;
    /**
     * 用户Id
     */
    @ApiModelProperty(value="用户Id",required = true)
    private String userId;
    /**
     * 相册名
     */
    @ApiModelProperty(value="相册名",required = true)
    private String albumName;
    /**
     * 类型
     */
    @ApiModelProperty(value="类型(系统/用户自建)",required = true)
    private Integer type;
    /**
     * 封面
     */
    @ApiModelProperty(value="封面图片",required = false)
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
