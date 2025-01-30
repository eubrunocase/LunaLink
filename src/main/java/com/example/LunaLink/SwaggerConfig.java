package com.example.LunaLink;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {


    @Bean
    public OpenAPI swagger() {
        return new OpenAPI()
                .info(new Info()
                .title("LunaLink")
                        .version("1.0")
                        .description("LunaLink Documentation")
                        .license(new License().name("apache-2.0").url("https://springdoc.org")));
    }




}
