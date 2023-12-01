package hc.timebook;

import hc.common.customize.RedisCacheClient;
import hc.common.dtos.ResponseResult;
import hc.service.AlbumService;
import hc.service.SearchService;
import hc.uniapp.album.pojos.Album;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

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
    @Test
    void test(){
        String content="2023-12-01 全 2023-12-02";
        ResponseResult result = searchService.searchAll(content);
        System.out.println(result.getCode());
        System.out.println(result.getData().toString());
    }

    @Test
    void testRedis(){
        Album album=albumService.getById("1729076022051225602");
        redisCacheClient.queryWithPassThrough(CACHE_ALBUM_KEY,album.getAlbumId()
                ,Album.class,albumService::getById,CACHE_ALBUM_TTL, TimeUnit.MINUTES);
        if(album==null)
            System.out.println("该相册不存在");
    }
}
