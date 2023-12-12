package hc.apis.elasticsearch;


import hc.apis.elasticsearch.fallback.IElasticsearchClientFallback;
import hc.common.dtos.ResponseResult;
import hc.uniapp.note.dtos.SearchNote;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(value="timebook-elasticsearch-service",fallback = IElasticsearchClientFallback.class)
public interface IElasticsearchClient {
    @PostMapping("/elasticsearch/searchNote")
    ResponseResult searchNote(@RequestBody SearchNote searchNote);

    @GetMapping("/elasticsearch/searchSuggestion/{prefix}")
    ResponseResult searchSuggestion(@PathVariable String prefix);



}
