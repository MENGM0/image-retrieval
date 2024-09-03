package com.dahua.retrieval.controller;

import com.dahua.retrieval.common.Result;
import com.dahua.retrieval.service.QueryImageService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/images")

public class ImageController {

    QueryImageService queryImageService = new QueryImageService();

    // http://localhost:8080/images/path/50
    @GetMapping(value = "/path/{count}", produces = "application/json")
    public Result<List<String>> getImages(@PathVariable Integer count) {
        // 返回图片路径列表
        List<String> path = queryImageService.getImageList(count);
        System.out.println("one request");
        System.out.println("path: " + path.toString());
        return  Result.success(path);
    }

    // http://localhost:8080/images/label
    @GetMapping(value = "/label", produces = "application/json")
    public Result<List<String>> getImageLabels() {
        // TODO:返回所有图片标签，查表

        return  Result.success(null);
    }
}
