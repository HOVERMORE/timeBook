package hc.apis.sensitive;


import hc.apis.sensitive.fallback.ISensitiveClientFallback;
import hc.common.customize.Sensitive;
import hc.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(value="timebook-sensitives-service",fallback = ISensitiveClientFallback.class)
public interface ISensitiveClient {
    @PostMapping("/sensitive/check")
    ResponseResult checkIsSensitive(@RequestBody Sensitive sensitive);
}
