package hc.apis.sensitive.fallback;


import hc.apis.sensitive.ISensitiveClient;
import hc.common.customize.Sensitive;
import hc.common.dtos.ResponseResult;
import hc.common.enums.AppHttpCodeEnum;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Component
public class ISensitiveClientFallback implements ISensitiveClient {
    @Override
    public ResponseResult checkIsSensitive(@RequestBody Sensitive sensitive) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"敏感词校验服务失败");
    }
}
