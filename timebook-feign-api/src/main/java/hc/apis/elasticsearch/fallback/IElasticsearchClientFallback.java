package hc.apis.elasticsearch.fallback;

import hc.apis.elasticsearch.IElasticsearchClient;
import hc.common.dtos.ResponseResult;
import hc.common.enums.AppHttpCodeEnum;
import hc.uniapp.note.dtos.SearchNote;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Component
public class IElasticsearchClientFallback implements IElasticsearchClient {
    @Override
    public ResponseResult searchNote(@RequestBody SearchNote searchNote) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"搜索日记服务失败");
    }

    @Override
    public ResponseResult searchSuggestion(@PathVariable String prefix){
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"自动补全服务失败");
    }

}
