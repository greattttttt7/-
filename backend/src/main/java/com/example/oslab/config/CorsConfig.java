package com.example.oslab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final SubmissionIsolationInterceptor submissionIsolationInterceptor;

    public CorsConfig(SubmissionIsolationInterceptor submissionIsolationInterceptor) {
        this.submissionIsolationInterceptor = submissionIsolationInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(submissionIsolationInterceptor)
                .addPathPatterns("/api/submissions", "/api/submissions/*")
                .excludePathPatterns("/api/submissions/judge");
    }

}
