package hc.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hc.SensitiveWordUtil;
import hc.common.customize.Sensitive;
import hc.common.dtos.ResponseResult;
import hc.common.enums.AppHttpCodeEnum;
import hc.common.exception.CustomizeException;
import hc.mapper.SensitivesMapper;
import hc.service.SensitivesService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;



@Service
public class sensitivesServiceImpl extends ServiceImpl<SensitivesMapper,Sensitive> implements SensitivesService {
    @Override
    public ResponseResult checkSensitives(String content) {
        List<Sensitive> sensitiveList=list();
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
