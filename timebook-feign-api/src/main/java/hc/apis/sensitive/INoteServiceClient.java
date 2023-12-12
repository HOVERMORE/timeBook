package hc.apis.sensitive;

import hc.apis.sensitive.fallback.IElasticsearchClientFallback;
import hc.apis.sensitive.fallback.INoteServiceClientFallback;
import hc.common.dtos.ResponseResult;
import hc.uniapp.note.dtos.SearchNote;
import hc.uniapp.note.pojos.Note;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value="timebook-uniapp-service",fallback = INoteServiceClientFallback.class)
public interface INoteServiceClient {
    @PostMapping("/apis/note/{id}")
    Note getNote(@PathVariable String id);
}
