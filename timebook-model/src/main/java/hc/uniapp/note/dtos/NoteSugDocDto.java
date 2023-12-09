package hc.uniapp.note.dtos;

import hc.uniapp.note.pojos.Note;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class NoteSugDocDto extends Note {
    private List<String> suggestion;

    public NoteSugDocDto(Note note){
        super(note.getNoteId(),
                note.getUserId(),
                note.getEmoji(),
                note.getContent(),
                note.getImages(),
                note.getCreateTime(),
                note.getUpdateTime(),
                note.getIsDeleted());
        this.suggestion=Arrays.asList(note.getContent().substring(0,10));
    }
    public NoteSugDocDto(){
        super();
    }
}
