package com.max.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@Slf4j
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class RestApplication {

    public static void main(String[] args) {
        log.debug("Application started");
        SpringApplication.run(RestApplication.class, args);
    }
}