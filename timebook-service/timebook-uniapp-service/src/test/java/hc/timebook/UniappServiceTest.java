package hc.timebook;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import hc.api.PythonSocket;
import hc.apis.sensitive.IElasticsearchClient;
import hc.common.constants.MqConstants;
import hc.common.customize.RedisCacheClient;
import hc.common.dtos.ResponseResult;
import hc.service.*;
import hc.thread.UserHolder;
import hc.uniapp.album.pojos.Album;
import hc.uniapp.image.pojos.Image;
import hc.uniapp.note.dtos.NoteDto;
import hc.uniapp.note.dtos.NoteHighDocDto;
import hc.uniapp.note.dtos.SearchNote;
import hc.uniapp.note.pojos.Note;
import hc.uniapp.user.pojos.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static hc.common.constants.ElasticSearchConstants.SEARCH_CONTENT;
import static hc.common.constants.RedisConstants.CACHE_ALBUM_KEY;
import static hc.common.constants.RedisConstants.CACHE_ALBUM_TTL;

@SpringBootTest
public class UniappServiceTest {
    @Resource
    private SearchService searchService;
    @Resource
    private AlbumService albumService;
    @Resource
    private RedisCacheClient redisCacheClient;
    @Resource
    private UserService userService;
    @Resource
    private ImageAlbumService imageAlbumService;
    @Resource
    private ImagesService imagesService;
    @Resource
    private NoteService noteService;
    @Resource
    private IElasticsearchClient elasticsearchClient;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Test
    void test(){
        setUser();
        String content="全 全 人 人 人像 2023-12-01 2023-12-02 2023-12-01";
        ResponseResult result = searchService.searchAlbumOrImage(content);
        System.out.println(result.getCode());
        System.out.println(result.getData().toString());
    }

    @Test
    void testRedis(){
        setUser();
        Album album=albumService.getById("1729076022051225602");
        album=redisCacheClient.queryWithPassThrough(CACHE_ALBUM_KEY,album.getAlbumId()
                ,Album.class,albumService::getById,CACHE_ALBUM_TTL, TimeUnit.MINUTES);
        if(album==null)
            System.out.println("该相册不存在");
        else
            album.toString();
    }
    @BeforeEach
    void setUser(){
        User byId = userService.getById("1729076021958950914");
        UserHolder.saveUser(byId);
    }
    @AfterEach
    void clearUser(){
        UserHolder.removeUser();
    }
    @Test
    void testDefault(){
        setUser();
        imagesService.deleteImage("1111","1111");
    }

    @Test
    void saveNote(){
        setUser();
        List<String> ids=new ArrayList<>();
        ids.add("1730552541839761409");
        ids.add("1730556408891666433");
        NoteDto noteDto=new NoteDto().setUserId(UserHolder.getUser().getUserId()).setEmoji("1")
                .setContent("测试2").setImageIds(ids);
        ResponseResult result = noteService.saveNote(noteDto);
        System.out.println(result.toString());
    }
    @Test
    void updateNote(){
        setUser();
        Note note=new Note().setNoteId("1731874253558648834").setContent("测试");
        note.setUserId(UserHolder.getUser().getUserId());
        NoteDto noteDto= BeanUtil.copyProperties(note,NoteDto.class);
        ResponseResult result=noteService.updateNote(noteDto);
        System.out.println(result.toString());
    }
    @Test
    void deleteNote(){
        setUser();
        Note note=new Note().setNoteId("1731874253558648834");
        note.setUserId(UserHolder.getUser().getUserId());
        NoteDto noteDto= BeanUtil.copyProperties(note,NoteDto.class);
        ResponseResult result=noteService.deleteNote(noteDto.getNoteId());
        System.out.println(result.toString());
        clearUser();
    }
    @Test
    void getNoteALl(){
        setUser();
        ResponseResult result=noteService.findList();
        System.out.println(result);
        clearUser();
    }
    @Test
    void searchServiceSensitive(){
        setUser();
        ResponseResult result=searchService.searchAlbumOrImage("全  bingdu");
        System.out.println(result);
    }
    @Test
    void tsetImage(){
        Image image=imagesService.getById("1730556408891666433");
        System.out.println(JSONUtil.toJsonStr(image));
    }

    @Test
    public void testApi() throws Exception {
        Image image=imagesService.getById("1730556408891666433");
        System.out.println("发送数据："+JSONUtil.toJsonStr(image));
        PythonSocket pythonSocket = new PythonSocket();
        pythonSocket.remoteCall(JSONUtil.toJsonStr(image));
    }
    @Test
    void getNode(){
        Note byId = noteService.getById("1732976803716403202");
        System.out.println(byId);
    }
    @Test
    void elasticsearchClientTest(){
        ResponseResult noteResponse = elasticsearchClient.searchNote(new SearchNote()
                .setSearchColumn(SEARCH_CONTENT)
                .setContent("jintian"));
        String listStr = JSONUtil.toJsonStr(noteResponse.getData());
        List<NoteHighDocDto> list = JSONUtil.toList(listStr, NoteHighDocDto.class);
        if(list!=null){
            for(NoteHighDocDto n:list)
                System.out.println(n);
        }else{
            System.out.println(6);
        }
    }
    @Test
    void esSearchSug(){
        ResponseResult result = searchService.searchSuggestion("c");
        System.out.println(result);
    }
    @Test
    void esSearchNote(){
        ResponseResult result = searchService.searchNote("ceshi");
        System.out.println(result);
    }

    @Test
    void RabbitMqInsert(){
        Note org=noteService.getById("1732976803716403202");
        rabbitTemplate.convertAndSend(MqConstants.TIMEBOOK_EXCHANGE,
                MqConstants.TIMEBOOK_INSERT_KEY,org.getNoteId());
    }
    @Test
    void RabbitMqDelete(){
        String noteId="1732976803716403202";
        rabbitTemplate.convertAndSend(MqConstants.TIMEBOOK_EXCHANGE,
                MqConstants.TIMEBOOK_DELETE_KEY,noteId);
    }
    @Test
    void RabbitMqUpdate(){
        String noteId="1732976803716403202";
        rabbitTemplate.convertAndSend(MqConstants.TIMEBOOK_EXCHANGE,
                MqConstants.TIMEBOOK_UPDATE_KEY,noteId);
    }
}
