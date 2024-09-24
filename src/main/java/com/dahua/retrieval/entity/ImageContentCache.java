package com.dahua.retrieval.entity;

import com.dahua.retrieval.algorithm.FingerPrint;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.dahua.retrieval.algorithm.PerceptualHashAlgorithm.loadImageFeature;
import static com.dahua.retrieval.algorithm.PerceptualHashAlgorithm.loadSimilarList;
import static com.dahua.retrieval.common.constant.Constant.*;


@Component
public class ImageContentCache implements ApplicationRunner {

    // 图片库的特征
    private Map<String, FingerPrint> imageId2Feature;

    // 图片库的相似列表
    private Map<String, Map<String, Double>> similarList;


    public ImageContentCache(){
        this.imageId2Feature = new HashMap<>();
        this.similarList = new HashMap<>();
    }

    @PostConstruct
    private void init(){
        this.imageId2Feature = loadImageFeature(ACC_VAL_DATASET_FEATURE_FINGER_PRINT_JSON_PATH);
        this.similarList = loadSimilarList(ACC_VAL_DATASET_FEATURE_COMPARE_RESULT_JSON_PATH, ACC_VAL_DATASET_INDEX_2_IMAGE_ID_PATH);
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {

    }

    public Map<String, FingerPrint> getContentFeature(){
        return this.imageId2Feature;
    }


    public Map<String, Map<String, Double>> getSimilarList(){
        return this.similarList;
    }
}
