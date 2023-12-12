package hc.uniapp.note.dtos;

import hc.uniapp.note.pojos.Note;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Data
public class NoteSugDocDto extends Note implements Serializable {
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
        int length=10;
        if(note.getContent().length()<10) {
            length=note.getContent().length();
        }
        this.suggestion=Arrays.asList(note.getContent().substring(0,length));
    }
    public NoteSugDocDto(){
        super();
    }

    @Override
    public String toString() {
        return super.toString()+"\nNoteSugDocDto{" +
                "suggestion=" + suggestion +
                '}';
    }
}
