package hc;


import com.alibaba.fastjson.JSON;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class RequestUtils {
    private static RestTemplate restTemplate = new RestTemplate();

    public static String doGet(String url, Map<String,String> param) throws Exception{
        boolean isFrist = true;
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String,String> e:param.entrySet()){
            if(isFrist) sb.append("?");
            else sb.append("&");
            isFrist=false;
            sb.append(e.getKey());
            sb.append("=");
            sb.append(e.getValue());
        }
        ResponseEntity<String> response = restTemplate.getForEntity(url+sb.toString(),String.class);
        return response.getBody();
    }
    public static String doPost(String url,Object data) throws Exception{
        String d = JSON.toJSONString(data);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(d, httpHeaders);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        return response.getBody();
    }
    public static String doPost(String url, Map<String,String> headers, Object data) throws Exception{
        String d = JSON.toJSONString(data);
        HttpHeaders httpHeaders = new HttpHeaders();
        for(Map.Entry<String,String> e:headers.entrySet()){
            httpHeaders.add(e.getKey(),e.getValue());
        }
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(d, httpHeaders);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        return response.getBody();
    }
}
