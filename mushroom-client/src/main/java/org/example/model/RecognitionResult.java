package org.example.model;

import lombok.Data;

@Data
public class RecognitionResult {
    private String aiKey;
    private double confidence;
    private boolean found;
    private String displayName;
    private String description;
    private Boolean edible;
    private String cookingTips;
}