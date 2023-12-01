package hc.feign;

import hc.apis.sensitive.ISensitiveClient;
import hc.common.customize.Sensitive;
import hc.common.dtos.ResponseResult;
import hc.service.SensitivesService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/sensitive")
public class SensitiveClient implements ISensitiveClient {

    @Resource
    private SensitivesService sensitivesService;

    @Override
    @PostMapping("/check")
    public ResponseResult checkIsSensitive(@RequestBody Sensitive sensitive) {
        return sensitivesService.checkSensitives(sensitive.getSensitives());
    }
}
