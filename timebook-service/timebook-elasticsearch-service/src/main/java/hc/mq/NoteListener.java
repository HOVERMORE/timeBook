package hc.mq;

import hc.apis.sensitive.INoteServiceClient;
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
        if(note!=null) {
            boolean bool = elasticsearchService.addIndexDocumentById(TIME_BOOK,
                    new NoteSugDocDto(note), NoteSugDocDto::getNoteId);
            if (!bool) {
                throw new CustomizeException("es新增搜索失败");
            }
        }else{
            warn("es新增服务：无法找到该日记");
        }
    }
    @RabbitListener(queues = MqConstants.TIMEBOOK_UPDATE_QUEUE)
    public void listenTimebookUpdate(String id){
        Note org = iNoteServiceClient.getNote(id);
        if(org!=null) {
            Boolean deleteFlag = elasticsearchService.deleteDocument(TIME_BOOK, org.getNoteId());
            if (!deleteFlag) {
                info("es删除-更新失败");
                throw new CustomizeException("es删除-更新失败");
            }
            Boolean addFlag = elasticsearchService.addIndexDocumentById(TIME_BOOK,
                    new NoteHighDocDto(org), NoteHighDocDto::getNoteId);
            if (!addFlag) {
                info("es新增-更新失败");
                throw new CustomizeException("es新增-更新失败");
            }
        }else
            warn("es新增服务：无法找到该日记");
    }
    @RabbitListener(queues = MqConstants.TIMEBOOK_DELETE_QUEUE)
    public void listenTimebookDelete(String id){
        boolean bool = elasticsearchService.deleteDocument(TIME_BOOK, id);
        if(!bool){
            error("es新增服务：无法找到该日记");
            throw new CustomizeException("es删除搜索失败");
        }
    }
}
