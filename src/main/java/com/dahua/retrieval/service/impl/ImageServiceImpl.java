package com.dahua.retrieval.service.impl;

import com.dahua.retrieval.entity.Image;
import com.dahua.retrieval.service.ImageService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ImageServiceImpl implements ImageService {
    @Override
    public void upload(String imageId) {

    }

    @Override
    public void download(String imageId) {

    }

    @Override
    public Image uploadImage(MultipartFile imageFile, String description, String[] tags) {
        return null;
    }

    @Override
    public List<Image> searchByImage(MultipartFile imageFile) {
        return List.of();
    }

    @Override
    public List<Image> getImagesByTag(String tag) {
        return List.of();
    }

    @Override
    public List<Image> searchImagesByKeyword(String keyword) {
        return List.of();
    }

    @Override
    public Image getImageById(Long id) {
        return null;
    }

    @Override
    public void deleteImage(Long id) {

    }

    @Override
    public Image updateImage(Long id, String description, String[] tags) {
        return null;
    }
}
