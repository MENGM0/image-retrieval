package com.dahua.retrieval.controller;

import com.dahua.retrieval.common.Result;
import com.dahua.retrieval.entity.Image;
import com.dahua.retrieval.service.ContentSearch;
import com.dahua.retrieval.service.ImageService;
import com.dahua.retrieval.service.impl.ContentSearchImpl;
import org.springframework.web.bind.annotation.*;
import java.util.List;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping
    public ResponseEntity<Image> uploadImage(@RequestParam("image") MultipartFile imageFile,
                                             @RequestParam("description") String description,
                                             @RequestParam("tags") String[] tags) {
        // 处理图片上传
        Image image = imageService.uploadImage(imageFile, description, tags);
        return ResponseEntity.ok(image);
    }

    @PostMapping("/search-by-image")
    public ResponseEntity<List<Image>> searchByImage(@RequestParam("image") MultipartFile imageFile) {
        List<Image> similarImages = imageService.searchByImage(imageFile);
        return ResponseEntity.ok(similarImages);
    }

    @GetMapping("/tags/{tag}")
    public ResponseEntity<List<Image>> getImagesByTag(@PathVariable String tag) {
        List<Image> images = imageService.getImagesByTag(tag);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Image>> searchImagesByKeyword(@RequestParam("keyword") String keyword) {
        List<Image> images = imageService.searchImagesByKeyword(keyword);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Image> getImageById(@PathVariable Long id) {
        Image image = imageService.getImageById(id);
        return ResponseEntity.ok(image);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Image> updateImage(@PathVariable Long id,
                                             @RequestParam("description") String description,
                                             @RequestParam("tags") String[] tags) {
        Image updatedImage = imageService.updateImage(id, description, tags);
        return ResponseEntity.ok(updatedImage);
    }
}
