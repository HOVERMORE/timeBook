package hc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hc.common.dtos.ResponseResult;
import hc.uniapp.note.dtos.NoteDto;
import hc.uniapp.note.pojos.Note;

public interface NoteService extends IService<Note> {
    ResponseResult findList();

    ResponseResult updateNote(NoteDto noteDto);

    ResponseResult saveNote(NoteDto noteDto);

    ResponseResult deleteNote(String noteId);
}
