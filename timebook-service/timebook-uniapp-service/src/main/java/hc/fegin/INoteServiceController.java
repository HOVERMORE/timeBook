package hc.fegin;

import hc.apis.note.INoteServiceClient;
import hc.common.customize.RedisCacheClient;
import hc.service.NoteService;
import hc.uniapp.note.pojos.Note;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static hc.common.constants.RedisConstants.*;

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
        return redisCacheClient.queryWithPassThrough(CACHE_NOTE_KEY,id,Note.class,
                noteService::getById,CACHE_NOTE_TTL, TimeUnit.DAYS);
    }
}
