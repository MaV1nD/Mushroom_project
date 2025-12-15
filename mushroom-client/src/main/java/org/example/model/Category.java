package org.example.model;

import lombok.Data;

@Data
public class Category {
    private Integer id;
    private String name;

    @Override
    public String toString() {
        return name;
    }
}