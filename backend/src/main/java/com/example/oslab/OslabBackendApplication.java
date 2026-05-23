package com.example.oslab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.example.oslab.mapper")
public class OslabBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OslabBackendApplication.class, args);
    }
}
