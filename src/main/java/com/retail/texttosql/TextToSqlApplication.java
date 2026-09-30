package com.retail.texttosql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TextToSqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(TextToSqlApplication.class, args);
    }
}
