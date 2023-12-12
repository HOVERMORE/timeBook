package hc.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = {"hc.apis.sensitive.fallback","hc.apis.elasticsearch.fallback"})
public class InitConfig {
}
