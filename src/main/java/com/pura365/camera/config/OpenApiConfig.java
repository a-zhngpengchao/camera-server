package com.pura365.camera.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * OpenAPI/Swagger 配置
 * 访问地址: http://localhost:8080/swagger-ui.html
 * API文档: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Camera Server API")
                        .version("1.0.0")
                        .description("摄像头服务器管理系统API文档")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@pura365.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("本地开发环境"),
                        new Server().url("https://api.example.com").description("生产环境")
                ));
    }
}
