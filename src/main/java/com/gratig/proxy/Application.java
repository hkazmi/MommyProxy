package com.gratig.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@Slf4j
@Configuration
public class Application {

    public static void main(String argv[]) {
        log.info("Starting at: " + new File(".").getAbsolutePath());
        SpringApplication.run(Application.class, argv);
    }
}
