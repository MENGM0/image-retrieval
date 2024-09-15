package com.dahua.retrieval.controller;

import com.dahua.retrieval.common.Result;
import com.dahua.retrieval.service.ContentSearch;
import com.dahua.retrieval.service.impl.ContentSearchImpl;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/images")

public class ImageController {

    ContentSearch contentSearch = new ContentSearchImpl();

    // http://localhost:8080/images/path/50
    @GetMapping(value = "/path/{count}", produces = "application/json")
    public Result<List<String>> contentSimilarImage(@PathVariable Integer count) {
        // TODO: 根据图片id，返回内容相似图片
        contentSearch.getSimilarImageById("id");
        return  Result.success(null);
    }

    // http://localhost:8080/images/label
    @GetMapping(value = "/label", produces = "application/json")
    public Result<List<String>> getImageLabels() {
        // TODO:返回所有图片标签，查表

        return  Result.success(null);
    }
}
