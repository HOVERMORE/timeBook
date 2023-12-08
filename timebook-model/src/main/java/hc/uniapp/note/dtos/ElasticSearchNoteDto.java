package hc.uniapp.note.dtos;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import hc.uniapp.image.pojos.Image;
import hc.uniapp.note.pojos.Note;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class ElasticSearchNoteDto implements Serializable {

    @ApiModelProperty(value = "日记Id",required =true)
    private String noteId;

    @ApiModelProperty(value = "用户Id",required = false)
    private String userId;

    @ApiModelProperty(value = "表情",required = false)
    private String emoji;

    @ApiModelProperty(value = "日记内容",required = true)
    private String content;

    @ApiModelProperty(value = "图片Id集合",required = true)
    private List<Image> images;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;

    @ApiModelProperty(value = "高亮内容",required = true)
    private String highLight;

}
