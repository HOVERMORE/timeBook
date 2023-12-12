package hc.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("hc.apis.sensitive.fallback")
public class InitConfig {
}
