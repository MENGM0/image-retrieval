package com.dahua.retrieval.service;

public interface ImageService {

    // 1. 图片上传
    void upload(String imageId);

    // 2. 图片下载
    void download(String imageId);
}
