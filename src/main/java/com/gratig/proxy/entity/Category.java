package com.gratig.proxy.entity;

import lombok.Data;

import java.util.Set;

@Data
public class Category {
    private boolean blocked=false;
    private String name;
    private Set<String> sites;
}
