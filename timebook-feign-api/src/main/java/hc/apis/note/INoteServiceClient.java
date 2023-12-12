package hc.apis.note;

import hc.apis.note.fallback.INoteServiceClientFallback;
import hc.uniapp.note.pojos.Note;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value="timebook-uniapp-service",fallback = INoteServiceClientFallback.class)
public interface INoteServiceClient {
    @PostMapping("/apis/note/{id}")
    Note getNote(@PathVariable String id);
}
