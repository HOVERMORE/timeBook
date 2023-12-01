package hc.uniapp.gateway.filter;





import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static hc.common.constants.RedisConstants.LOGIN_USER_KEY;
import static hc.common.constants.RedisConstants.LOGIN_USER_TTL;


@Component
@Slf4j
public class AuthorizeFilter implements  Ordered, GlobalFilter {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String[] path={
                "/user/login"
        };

        //2.判断是否是登录
        if(check(path,request.getURI().getPath())){
            //放行
            return chain.filter(exchange);
        }

        //3.获取token
        String userId = request.getHeaders().getFirst("userId");

        //4.判断token是否存在
        if(StrUtil.isBlank(userId)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        String key=LOGIN_USER_KEY + userId;
        //基于token获取redis中的用户
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        if(userMap.isEmpty()){
            return chain.filter(exchange);
        }
        ServerHttpRequest serverHttpRequest = request.mutate().headers(httpHeaders -> {
            httpHeaders.add("userId", userId + "");
        }).build();
        //重制请求
        exchange.mutate().request(serverHttpRequest);
        //刷新token有效期
        stringRedisTemplate.expire(key,LOGIN_USER_TTL, TimeUnit.MINUTES);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match=PATH_MATCHER.match(url,requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }

}
