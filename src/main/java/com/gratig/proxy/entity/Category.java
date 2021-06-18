package com.gratig.proxy.entity;

import lombok.Data;

import java.util.SortedSet;

@Data
public class Category {
    private boolean blocked=false;
    private String name;
    private SortedSet<String> sites;
}
