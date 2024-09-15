package com.dahua.retrieval.service;

import java.util.List;

public interface ContentSearch {

    // 1. 根据imageId, 返回相似图片
    List<String> getSimilarImageById(String imageId);


    // 2. 根据上传图片，以图搜图，返回相似图片
    List<String> getSimilarImageByImage(String imageId);


}
