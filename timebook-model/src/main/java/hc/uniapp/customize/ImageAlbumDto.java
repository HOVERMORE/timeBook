package hc.uniapp.customize;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_image_album")
public class ImageAlbumDto  implements Serializable {
    /**
     * 图片关联相册Id
     */
    @ApiModelProperty(value = "图片关联相册Id",required = true)
    @TableId(type = IdType.ASSIGN_ID)
    private String imageAlbumId;
    /**
     * 用户Id
     */
    @ApiModelProperty(value = "用户Id",required = true)
    private String userId;
    /**
     * 图片Id
     */
    @ApiModelProperty(value = "图片imageId",required = true)
    private String imageId;
    /**
     * 相册Id
     */
    @ApiModelProperty(value = "相册albumId",required = true)
    private String albumId;
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
