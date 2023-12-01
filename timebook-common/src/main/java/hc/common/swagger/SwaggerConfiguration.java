package hc.common.swagger;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configurable
@EnableSwagger2
public class SwaggerConfiguration {
    @Bean
    public Docket buildDocket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(buildApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("hc"))
                .paths(PathSelectors.any())
                .build();

    }
    @Bean
    private ApiInfo buildApiInfo() {
        Contact contact = new Contact("master","","");
        return new ApiInfoBuilder()
                .title("timebook---平台管理API文档")
                .description("timebook后台api")
                .contact(contact)
                .version("1.0.0").build();
    }
}
