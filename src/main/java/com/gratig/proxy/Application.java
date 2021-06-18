package com.gratig.proxy;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@Slf4j
@Configuration
public class Application {
    public final static List<String> blocks = Arrays.asList("www.youtube.com", "www.vimeo.com");

    public static void main(String argv[]) {
        log.info("Starting server now");
        SpringApplication.run(Application.class, argv);
    }
}
