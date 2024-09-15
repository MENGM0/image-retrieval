package com.dahua.retrieval.service;

import java.util.List;

public interface SemanticSearch {

    // 1. 返回图片库的标签信息
    List<String> getLabels();


    // 2. 根据搜索关键词，返回最符合的图片列表
    List<String> getImageByWords(String[] keywords);
}
