package hc.uniapp.note.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class NoteDto implements Serializable {
    @ApiModelProperty(value = "日记Id",required = false)
    private String noteId;

    @ApiModelProperty(value = "用户Id",required = false)
    private String userId;

    @ApiModelProperty(value = "表情",required = true)
    private String emoji;

    @ApiModelProperty(value = "日记内容",required = true)
    private String content;

    @ApiModelProperty(value = "图片Id集合",required = true)
    private List<String> imageIds;
}
