package com.xpj.madness.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableAutoConfiguration
@EnableRetry
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }
}
