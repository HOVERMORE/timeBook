package hc.uniapp.note.pojos;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import hc.uniapp.image.pojos.Image;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
@TableName("tb_note")
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Note implements Serializable {

    @ApiModelProperty(value = "日记Id",required =true)
    @TableId(type = IdType.ASSIGN_ID)
    private String noteId;

    @ApiModelProperty(value = "用户Id",required = false)
    private String userId;

    @ApiModelProperty(value = "表情",required = false)
    private String emoji;

    @ApiModelProperty(value = "日记内容",required = true)
    private String content;

    @ApiModelProperty(value = "图片Id集合",required = true)
    @TableField(exist = false)
    private List<Image> images;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill= FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer isDeleted;
}
