package com.gratig.proxy.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gratig.proxy.entity.Category;
import com.gratig.proxy.littleproxy.ProxyManager;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@Slf4j
public class ProxyController {
    private ObjectMapper mapper = new ObjectMapper();

    private final ProxyManager proxy;

    public ProxyController(ProxyManager proxy) {
        this.proxy = proxy;
    }

    @GetMapping("categories")
    public List<Category> getCategories() {
        return proxy.getBlocks();
    }

    @PostMapping("categories")
    public void setCategories(@Parameter(hidden = true) @Value("${category.file.loc}") String fileLoc, List<Category> categories) throws IOException {
        List<Category> blocks = proxy.getBlocks();
        blocks.clear();
        blocks.addAll(categories);
        save(fileLoc);
    }

    @GetMapping("enable/{enable}")
    public ResponseEntity enable(@PathVariable("enable") boolean enabled) {
        proxy.setEnabled(enabled);
        return ResponseEntity.ok().build();
    }
    @GetMapping("save")
    public ResponseEntity save(@Parameter(hidden = true)  @Value("${category.file.loc}") String fileLoc) throws IOException {
        List<Category> blocks = proxy.getBlocks();
        String categories = mapper.writeValueAsString(blocks);
        Files.write(new File(fileLoc).toPath(), categories.getBytes(StandardCharsets.UTF_8));
        return ResponseEntity.ok().build();
    }

    @GetMapping("load")
    public ResponseEntity load(@Parameter(hidden = true) @Value("${category.file.loc}") String fileLoc) throws IOException {
        List<Category> categories = mapper.readValue(new File(fileLoc), new TypeReference<List<Category>>() {
        });
        List<Category> blocks = proxy.getBlocks();
        blocks.clear();
        blocks.addAll(categories);
        return ResponseEntity.ok().build();
    }
}
