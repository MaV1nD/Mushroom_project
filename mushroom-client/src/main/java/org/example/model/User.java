package org.example.model;

import lombok.Data;

@Data
public class User {
    private Integer id;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String location;
}