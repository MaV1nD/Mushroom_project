package org.example.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Ad {
    private Integer id;
    private String title;
    private String description;
    private BigDecimal price;
    private String username;
    private String sellerFullName;
    private String categoryName;
    private String imagePath;
    private LocalDateTime createdAt;

    public Category getCategory() {
        Category c = new Category();
        c.setName(categoryName);
        return c;
    }
}