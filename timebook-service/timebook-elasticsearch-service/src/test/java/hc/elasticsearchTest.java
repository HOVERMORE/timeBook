package hc;

import hc.apis.note.INoteServiceClient;
import hc.service.ElasticsearchService;
import hc.uniapp.note.dtos.NoteSugDocDto;
import hc.uniapp.note.pojos.Note;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

import static hc.common.constants.ElasticSearchConstants.SEARCH_SUGGESTION;
import static hc.common.constants.ElasticSearchConstants.TIME_BOOK;

@SpringBootTest
public class elasticsearchTest {
    @Resource
    private ElasticsearchService elasticsearchService;
    @Resource
    private INoteServiceClient iNoteServiceClient;
    @Test
    void test(){
        String id="1732976803716403202";
        Note note = iNoteServiceClient.getNote(id);
        boolean bool = elasticsearchService.addIndexDocumentById(TIME_BOOK,
                new NoteSugDocDto(note), NoteSugDocDto::getNoteId);
    }
}
