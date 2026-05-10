package com.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ScanShort API")
                        .description("A URL shortener API with Redis caching, QR code generation, and analytics")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ahmed Essam")
                                .email("ahmed.essam7722@gmail.com")
                                .url("https://www.linkedin.com/in/ahmed-essam7722/"))
                )
                .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
                .components(new Components()
                        .addSchemas("ApiErrorResponse", new ObjectSchema()
                                .addProperty("success", new BooleanSchema().example(false))
                                .addProperty("message", new StringSchema().example("Resource not found"))
                                .addProperty("errorCode", new StringSchema().example("NOT_FOUND"))
                                .addProperty("errors", new ArraySchema().nullable(true).example(null))
                        )
                        .addSecuritySchemes("Bearer Auth", new SecurityScheme()
                                .name("Bearer Auth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token")
                        )
                        .addResponses("400", new ApiResponse()
                                .description("Validation error")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema().$ref("#/components/schemas/ApiErrorResponse")))))
                        .addResponses("401", new ApiResponse()
                                .description("Unauthorized")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema().$ref("#/components/schemas/ApiErrorResponse")))))
                        .addResponses("403", new ApiResponse()
                                .description("Forbidden")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema().$ref("#/components/schemas/ApiErrorResponse")))))
                        .addResponses("404", new ApiResponse()
                                .description("Not found")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema().$ref("#/components/schemas/ApiErrorResponse")))))
                        .addResponses("409", new ApiResponse()
                                .description("Conflict")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema().$ref("#/components/schemas/ApiErrorResponse")))))
                        .addResponses("410", new ApiResponse()
                                .description("Gone (Not available anymore)")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema().$ref("#/components/schemas/ApiErrorResponse")))))
                        .addResponses("429", new ApiResponse()
                                .description("Too many requests")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema().$ref("#/components/schemas/ApiErrorResponse")))))
                );
    }
}