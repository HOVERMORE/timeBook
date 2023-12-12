package hc.fegin;

import hc.apis.sensitive.INoteServiceClient;
import hc.common.customize.RedisCacheClient;
import hc.service.NoteService;
import hc.uniapp.image.pojos.Image;
import hc.uniapp.note.pojos.Note;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static hc.common.constants.RedisConstants.CACHE_IMAGE_KEY;
import static hc.common.constants.RedisConstants.CACHE_IMAGE_TTL;

@RestController
@RequestMapping("/apis")
public class INoteServiceController implements INoteServiceClient {
    @Resource
    private NoteService noteService;
    @Resource
    private RedisCacheClient redisCacheClient;
    @Override
    @PostMapping("/note/{id}")
    public Note getNote(@PathVariable String id) {
        return redisCacheClient.queryWithPassThrough(CACHE_IMAGE_KEY,id,Note.class,
                noteService::getById,CACHE_IMAGE_TTL, TimeUnit.DAYS);
    }
}
