package com.yeonhx03.marketbriefapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebApiConfiguration implements WebMvcConfigurer {

    private final WriteApiKeyInterceptor writeApiKeyInterceptor;
    private final String[] allowedOrigins;

    public WebApiConfiguration(
            @Value("${market-brief.write-api-key:}") String writeApiKey,
            @Value("${market-brief.cors.allowed-origins:http://localhost:5173}")
            String allowedOrigins
    ) {
        this.writeApiKeyInterceptor = new WriteApiKeyInterceptor(writeApiKey);
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(writeApiKeyInterceptor)
                .addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST")
                .allowedHeaders("Content-Type", WriteApiKeyInterceptor.HEADER_NAME)
                .maxAge(3600);
    }
}
