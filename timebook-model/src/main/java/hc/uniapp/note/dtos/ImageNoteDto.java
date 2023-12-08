package hc.uniapp.note.dtos;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("tb_image_note")
public class ImageNoteDto implements Serializable {

    @ApiModelProperty(value = "日记与图片关联Id",required = true)
    @TableId(type = IdType.ASSIGN_ID)
    private String imageNoteId;

    @ApiModelProperty(value = "日记Id",required = true)
    private String noteId;

    @ApiModelProperty(value = "图片Id",required = true)
    private String imageId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill= FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer isDeleted;
}
