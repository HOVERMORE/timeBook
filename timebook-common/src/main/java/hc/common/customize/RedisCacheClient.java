package hc.common.customize;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;

import cn.hutool.json.JSONObject;
import hc.uniapp.album.pojos.Album;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import cn.hutool.json.JSONUtil;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static hc.LogUtils.error;
import static hc.common.constants.RedisConstants.CACHE_NULL_TTL;

@Component
@Slf4j
public class RedisCacheClient {
    private final StringRedisTemplate stringRedisTemplate;

    public RedisCacheClient(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate=stringRedisTemplate;
    }
    private void setCache(String key, Object value, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(value),time,unit);
    }

    /**
     * 设置空值解决缓存击穿问题
     * @param keyPrefix
     * @param id
     * @param type
     * @param dbFallback
     * @param time
     * @param unit
     * @return
     * @param <T>
     * @param <ID>
     */
    public <T,ID> T queryWithPassThrough(String keyPrefix, ID id, Class<T> type
            , Function<ID,T> dbFallback, Long time, TimeUnit unit){
        String key=keyPrefix+id;
        String json=stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(json)){
            return JSONUtil.toBean(json,type);
        }
        if(json!=null){
            return null;
        }
        T t=dbFallback.apply(id);
        if(t==null){
            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
            error("未找到该类,已设置为空值");
            return null;
        }
        this.setCache(key,t,time,unit);
        return t;
    }

}
