package hc.uniapp.note.dtos;

import hc.uniapp.image.pojos.Image;
import hc.uniapp.note.pojos.Note;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class NoteHighDocDto extends Note implements Serializable {

    @ApiModelProperty(value = "高亮内容",required = true)
    private String highLight;
    public NoteHighDocDto(Note note){
        super(note.getNoteId(),
                note.getUserId(),
                note.getEmoji(),
                note.getContent(),
                note.getImages(),
                note.getCreateTime(),
                note.getUpdateTime(),
                note.getIsDeleted());
    }
    public NoteHighDocDto(){
        super();
    }
}
