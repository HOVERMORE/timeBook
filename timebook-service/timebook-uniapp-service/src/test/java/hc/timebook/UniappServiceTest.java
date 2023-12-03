package hc.timebook;

import hc.common.customize.RedisCacheClient;
import hc.common.dtos.ResponseResult;
import hc.service.*;
import hc.thread.UserHolder;
import hc.uniapp.album.pojos.Album;
import hc.uniapp.note.dtos.NoteDto;
import hc.uniapp.note.pojos.Note;
import hc.uniapp.user.pojos.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    @Test
    void test(){
        setUser();
        String content="全 全 人 人 人像 2023-12-01 2023-12-02 2023-12-01";
        ResponseResult result = searchService.searchAll(content);
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
    void setUser(){
        User byId = userService.getById("1729076021958950914");
        UserHolder.saveUser(byId);
    }
    @Test
    void testDefault(){
        setUser();
        imagesService.deleteImage("1111","1111");
    }

    @Test
    void testNote(){
        setUser();
        List<String> ids=new ArrayList<>();
        ids.add("1730552541839761409");
        NoteDto noteDto=new NoteDto().setUserId(UserHolder.getUser().getUserId()).setEmoji("1")
                .setContent("黄清").setImageIds(ids);
        ResponseResult result = noteService.saveNote(noteDto);
        System.out.println(result.toString());
    }
    @Test
    void save(){
        setUser();
        Note note=new Note();
        note.setUserId(UserHolder.getUser().getUserId());
        noteService.save(note);
        System.out.println(note.getNoteId());
    }
}
