package com.thahir.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringBatchApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchApiApplication.class, args);
    }

}
