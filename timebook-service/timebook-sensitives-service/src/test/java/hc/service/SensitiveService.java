package hc.service;

import hc.common.customize.Sensitive;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Base64;
@SpringBootTest
public class SensitiveService{
    @Resource
            private SensitivesService sensitivesService;
    @Test
    void saveSensitive(){

//        decode(sensitives2);
//        sensitivesService.saveBatch(sensitivesList);
        String str="黄桂清";
        encode(str);
    }

    public  void decode(String str){
        StringBuilder output = new StringBuilder();
        String[] lines = str.split("\n");
        for (String line : lines) {
            output.append(line).append("==").append("\n");
        }
        str=output.toString();
        String[] a = str.split("=\n");
        int i=1;
        for(String ss:a) {
//            String replace = ss.replace("=","");
//            System.out.println((i++)+"   "+replace);
//            Sensitive sensitive=new Sensitive();
//            sensitive.setSensitives(replace);
//            sensitivesList.add(sensitive);


            String replace = ss.replace("=","");
            String base64EncodedString = replace;
            byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedString);
            String decodedString = new String(decodedBytes);
            System.out.println((i++)+"   "+decodedString);


        }
    }
    public void encode(String content){
        byte[] bytes = content.getBytes();
        byte[] encryptedBytes = Base64.getEncoder().encode(bytes);
        String word=new String(encryptedBytes);
        System.out.println(word);
    }
}
