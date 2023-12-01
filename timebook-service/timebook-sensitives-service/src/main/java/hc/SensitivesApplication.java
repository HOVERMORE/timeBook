package hc;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("hc.mapper")
public class SensitivesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SensitivesApplication.class);
    }
}