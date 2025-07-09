package com.nvd.expensetracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI expenseTrackerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Expense Tracker API")
                .description("API quản lý chi tiêu cá nhân. Gồm chức năng đăng ký, đăng nhập, thống kê, xuất CSV/Excel.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Nguyễn Văn Dũng")
                    .email("dung.nguyenvan.it@gmail.com")
                    .url("https://github.com/dungvh97/expense-tracker-api")))
            .components(new Components()
            .addSecuritySchemes("BearerToken",
                    new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("BearerToken", List.of()));
    }
}
