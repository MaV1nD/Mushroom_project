package com.mushroom.server.controller;

import com.mushroom.server.model.Ad;
import com.mushroom.server.model.Category;
import com.mushroom.server.model.User;
import com.mushroom.server.repository.AdRepository;
import com.mushroom.server.repository.CategoryRepository;
import com.mushroom.server.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
public class AdController {

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final Path uploadPath = Paths.get("uploads");

    @GetMapping
    public List<AdResponse> getAllAds(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer categoryId
    ) {
        String pattern = (query != null && !query.trim().isEmpty()) ? "%" + query.trim() + "%" : null;
        Integer catIdClean = (categoryId != null && categoryId > 0) ? categoryId : null;

        return adRepository.searchAds(pattern, catIdClean)
                .stream()
                .map(AdResponse::new)
                .toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAd(
            @RequestParam("username") String username,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        Ad ad = new Ad();
        ad.setUser(user);
        ad.setCategory(category);
        ad.setTitle(title);
        ad.setDescription(description);
        ad.setPrice(price);

        if (image != null && !image.isEmpty()) {
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Files.copy(image.getInputStream(), uploadPath.resolve(filename));
            ad.setImagePath(filename);
        }

        adRepository.save(ad);
        return ResponseEntity.ok("Объявление создано");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAd(@PathVariable Integer id, @RequestParam String username) {
        Ad ad = adRepository.findById(id).orElseThrow(() -> new RuntimeException("Не найдено"));
        if (!ad.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(403).body("Это не ваше объявление!");
        }
        adRepository.delete(ad);
        return ResponseEntity.ok("Удалено");
    }

    @GetMapping("/image/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws Exception {
        Path file = uploadPath.resolve(filename);
        if (!Files.exists(file)) return ResponseEntity.notFound().build();
        Resource resource = new UrlResource(file.toUri());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
    }

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @Data
    static class AdResponse {
        Integer id;
        String title;
        String description;
        BigDecimal price;
        String username;
        String sellerFullName;
        String categoryName;
        String imagePath;
        LocalDateTime createdAt;

        public AdResponse(Ad ad) {
            this.id = ad.getId();
            this.title = ad.getTitle();
            this.description = ad.getDescription();
            this.price = ad.getPrice();
            this.imagePath = ad.getImagePath();
            this.createdAt = ad.getCreatedAt();

            if (ad.getUser() != null) {
                this.username = ad.getUser().getUsername();
                this.sellerFullName = ad.getUser().getFullName();
            } else {
                this.username = "Неизвестно";
            }

            this.categoryName = (ad.getCategory() != null) ? ad.getCategory().getName() : "Без категории";
        }
    }
}