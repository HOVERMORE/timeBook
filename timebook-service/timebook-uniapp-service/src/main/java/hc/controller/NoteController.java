package hc.controller;

import hc.common.dtos.ResponseResult;
import hc.service.NoteService;
import hc.uniapp.note.dtos.NoteDto;
import hc.uniapp.note.pojos.Note;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/note")
@Api(value="日记管理",tags="uniapp日记管理")
public class NoteController {
    @Resource
    private NoteService noteService;

    @GetMapping("/getAll")
    @ApiOperation("查看所有日记")
    public ResponseResult findList(){
        return noteService.findList();
    }
    @PutMapping("/update")
    @ApiOperation("修改日记")
    public ResponseResult update(@RequestBody NoteDto noteDto){
        return noteService.updateNote(noteDto);
    }
    @PostMapping("/save")
    @ApiOperation("新增日记")
    public ResponseResult save(@RequestBody NoteDto noteDto){
        return noteService.saveNote(noteDto);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除日记")
    public ResponseResult delete(@RequestParam String noteId){
        return noteService.deleteNote(noteId);
    }
}
