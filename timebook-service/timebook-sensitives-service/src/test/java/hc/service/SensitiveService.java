package hc.service;

import hc.common.customize.Sensitive;
import hc.common.dtos.ResponseResult;
import org.junit.jupiter.api.Test;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;


@SpringBootTest
public class SensitiveService{
    @Resource
            private SensitivesService sensitivesService;
    @Test
    void saveSensitive(){

//        decode(sensitives2);
//        sensitivesService.saveBatch(sensitivesList);
        String str="root";
//        ResponseResult result = sensitivesService.checkSensitives(str);
//        System.out.println(result.getCode()+"  "+result.getErrorMsg());
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

    @Test
    void testString(){
        String content="root ";
        List<String> split = Arrays.stream(content.split("\\s+")).collect(Collectors.toList());
        System.out.println(split.toString());
        Sensitive sensitive=new Sensitive().setSensitives(split.toString()
                .replace("[","")
                .replace("]",""));
        ResponseResult result=sensitivesService.checkSensitives(sensitive.getSensitives());
        System.out.println(result);
    }
}
