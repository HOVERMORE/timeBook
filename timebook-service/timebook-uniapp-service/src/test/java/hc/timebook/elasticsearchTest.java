package hc.timebook;

import hc.service.impl.ElasticSearcherClient;
import hc.common.dtos.ResponseResult;
import hc.service.NoteService;
import hc.uniapp.note.dtos.ElasticSearchNoteDto;
import hc.uniapp.note.pojos.Note;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hc.common.constants.ElasticSearchConstants.*;

@SpringBootTest
public class elasticsearchTest {


    @Resource
    private NoteService noteService;
    @Resource
    private ElasticSearcherClient client;

    @Test
    void testClient(){
        Note note = noteService.getById("1732976803716403202");
        Note note1 = client.queryDocumentById(TIME_BOOK, note, Note::getNoteId, Note.class);
        System.out.println(note1);
    }
    @Test
    void testMatchAll(){
        List<Note> notes = client.queryMatchAll(TIME_BOOK,Note.class);
        for (Note note:notes){
            System.out.println(note);
        }
    }
    @Test
    void testQueryHighLightAndSort(){
        List<ElasticSearchNoteDto> noteDtos = client.queryHighLightAndSort(TIME_BOOK,
                CONTENT, "测试", CREATE_TIME, CONTENT);
        ResponseResult result= new ResponseResult<>();
        result.setData(noteDtos);
        System.out.println(result);
    }
    @Test
    void deleteEs(){
        Note note = noteService.getById("1732976803716403202");
        Boolean b = client.deleteDocument(TIME_BOOK, note, Note::getNoteId);
    }
    @Test
    void addByIds(){
        List<Note> list = noteService.list();
        client.addIndexDocumentByIds(TIME_BOOK,list, Note::getNoteId);
    }
    /**
     判断字符串是否含有Emoji表情
     **/
    private boolean isHasEmoji(String reviewerName) {
        Pattern pattern = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]");
        Matcher matcher = pattern.matcher(reviewerName);
        return matcher.find();
    }
    @Test
    void test(){
        String emoji="\uD83D\uDE00";
        boolean hasEmoji = isHasEmoji(emoji);
        System.out.println(hasEmoji);
    }
    @Test
    void addById(){
        Note note = noteService.getById("1732976803716403202");
        System.out.println(note);
        client.addIndexDocumentById(TIME_BOOK,note, Note::getNoteId);
    }
}