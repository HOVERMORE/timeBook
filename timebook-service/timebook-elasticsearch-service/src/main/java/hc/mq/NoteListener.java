package hc.mq;

import hc.apis.note.INoteServiceClient;
import hc.common.constants.MqConstants;
import hc.common.exception.CustomizeException;
import hc.service.ElasticsearchService;
import hc.uniapp.note.dtos.NoteHighDocDto;
import hc.uniapp.note.dtos.NoteSugDocDto;
import hc.uniapp.note.pojos.Note;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static hc.LogUtils.*;
import static hc.common.constants.ElasticSearchConstants.TIME_BOOK;

@Component
public class NoteListener {
    @Resource
    private INoteServiceClient iNoteServiceClient;
    @Resource
    private ElasticsearchService elasticsearchService;
    @RabbitListener(queues = MqConstants.TIMEBOOK_INSERT_QUEUE)
    public void listenTimebookInsert(String id){
        Note note = iNoteServiceClient.getNote(id);
        saveNoteDoc(note);
    }
    @RabbitListener(queues = MqConstants.TIMEBOOK_UPDATE_QUEUE)
    public void listenTimebookUpdate(String id){
        Note note = iNoteServiceClient.getNote(id);
        if(note!=null) {
            deleteNoteDoc(note.getNoteId());
            saveNoteDoc(note);
        }else
            warn("es更新服务：无法找到该日记");
    }
    @RabbitListener(queues = MqConstants.TIMEBOOK_DELETE_QUEUE)
    public void listenTimebookDelete(String id){
       deleteNoteDoc(id);
    }
    void saveNoteDoc(Note note){
        if(note!=null) {
            boolean bool = elasticsearchService.addIndexDocumentById(TIME_BOOK,
                    new NoteSugDocDto(note), NoteSugDocDto::getNoteId);
            if (!bool) {
                error("es新增搜索失败");
            }
        }else{
            warn("es新增服务：无法找到该日记");
        }
    }
    void deleteNoteDoc(String id){
        Boolean deleteFlag = elasticsearchService.deleteDocument(TIME_BOOK, id);
        if (!deleteFlag) {
            error("es删除失败");
        }
    }
}
