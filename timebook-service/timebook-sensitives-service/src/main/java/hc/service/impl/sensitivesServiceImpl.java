package hc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hc.SensitiveWordUtil;
import hc.common.customize.Sensitive;
import hc.common.dtos.ResponseResult;
import hc.common.enums.AppHttpCodeEnum;
import hc.mapper.SensitivesMapper;
import hc.service.SensitivesService;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class sensitivesServiceImpl extends ServiceImpl<SensitivesMapper,Sensitive> implements SensitivesService {
    @Override
    public ResponseResult checkSensitives(String content) {
        List<Sensitive> sensitiveList = list();
        List<String> sensitives = sensitiveList.stream().map(Sensitive::getSensitives).collect(Collectors.toList());
        SensitiveWordUtil.initMap(sensitives);
        byte[] bytes = content.getBytes();
        byte[] encryptedBytes = Base64.getEncoder().encode(bytes);
        String word=new String(encryptedBytes);
        Map<String, Integer> map = SensitiveWordUtil.matchWords(word);
        if(map.size()>0)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_BREACHES);
        return ResponseResult.okResult();
    }
}
