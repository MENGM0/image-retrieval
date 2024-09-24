package com.dahua.retrieval.service;

import com.dahua.retrieval.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {

    // 1. 图片上传
    void upload(String imageId);

    // 2. 图片下载
    void download(String imageId);

    Image uploadImage(MultipartFile imageFile, String description, String[] tags);

    List<Image> searchByImage(MultipartFile imageFile);

    List<Image> getImagesByTag(String tag);

    List<Image> searchImagesByKeyword(String keyword);

    Image getImageById(Long id);

    void deleteImage(Long id);

    Image updateImage(Long id, String description, String[] tags);
}
