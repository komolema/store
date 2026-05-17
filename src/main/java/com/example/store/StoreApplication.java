package com.example.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class StoreApplication {

    private static final Logger log = LoggerFactory.getLogger(StoreApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(StoreApplication.class, args);
        log.info("StoreApplication started with {} args", args.length);
    }
}
