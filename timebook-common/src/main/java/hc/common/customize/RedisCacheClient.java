package hc.common.customize;

import cn.hutool.core.util.StrUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import cn.hutool.json.JSONUtil;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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
        //1,从redis查询商铺缓存
        String json=stringRedisTemplate.opsForValue().get(key);
        //2.判断是否存在
        if(StrUtil.isNotBlank(json)){
            //3.存在，直接返回
            return JSONUtil.toBean(json,type);
        }
        //判断命中的是否是空值
        if(json!=null){
            return null;
        }
        //4.不存在，根据id查询数据库
        T t=dbFallback.apply(id);
        //5.不存在，返回错误
        if(t==null){
            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
            return null;
        }
        //6.存在，写入redis
        this.setCache(key,t,time,unit);
        //7.返回
        return t;
    }

}
