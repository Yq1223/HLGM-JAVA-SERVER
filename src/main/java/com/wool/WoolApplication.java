package com.wool;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wool.mapper")
public class WoolApplication {
    public static void main(String[] args) {
        SpringApplication.run(WoolApplication.class, args);
    }
}
