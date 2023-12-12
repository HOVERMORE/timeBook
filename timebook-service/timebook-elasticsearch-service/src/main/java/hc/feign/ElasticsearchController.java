package hc.feign;

import hc.apis.elasticsearch.IElasticsearchClient;
import hc.apis.note.INoteServiceClient;
import hc.common.dtos.ResponseResult;
import hc.service.ElasticsearchService;
import hc.uniapp.note.dtos.NoteHighDocDto;
import hc.uniapp.note.dtos.SearchNote;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static hc.common.constants.ElasticSearchConstants.*;
import static hc.common.constants.ElasticSearchConstants.HIGH_COLUMN_CONTENT;

@RestController
@RequestMapping("/elasticsearch")
public class ElasticsearchController implements IElasticsearchClient {
    @Resource
    private ElasticsearchService elasticsearchService;

    @PostMapping("/searchNote")
    public ResponseResult searchNote(@RequestBody SearchNote searchNote) {
        List<NoteHighDocDto> noteDtos = elasticsearchService
                .queryHighLightAndSort(TIME_BOOK, searchNote.getSearchColumn(), searchNote.getContent(), CREATE_TIME, HIGH_COLUMN_CONTENT);
        return ResponseResult.okResult(noteDtos);
    }
    @GetMapping("/searchSuggestion/{prefix}")
    public ResponseResult  searchSuggestion(@PathVariable String prefix) {
        List<String> suggests = elasticsearchService.querySuggest(TIME_BOOK, SEARCH_SUGGESTION, prefix);
        return ResponseResult.okResult(suggests);
    }


}
