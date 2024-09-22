package com.dahua.retrieval.common.constant;

public class Constant {
    // ==========================================相似圖片參數==========================================
    public static Integer TOP_N = 50;

    // ==========================================公共路径==========================================
    public static String ACC_VAL_DATASET_IMAGE_ROOT_PATH = "F:\\dataset\\ai_challenger_caption_validation_20170910\\caption_validation_images_20170910";
    public static String ACC_DEMO_DATASET_IMAGE_ROOT_PATH = "F:\\dataset\\ai_challenger_caption_validation_20170910\\caption_validation_images_20170910_demo";
    public static String RESOURCE_ROOT_PATH = "F:\\projects\\qingmiao-2024\\image-retrieval\\src\\main\\resources\\";

    // ==========================================内容检索==========================================
    // val（验证集，30000 张图片）
    public static String ACC_VAL_DATASET_FEATURE_FINGER_PRINT_JSON_PATH = RESOURCE_ROOT_PATH + "data\\content\\acc_val_feature.json";
    public static String ACC_VAL_DATASET_FEATURE_COMPARE_RESULT_JSON_PATH = RESOURCE_ROOT_PATH + "data\\content\\acc_val_feature_compare_result.json";
    public static String ACC_VAL_DATASET_INDEX_2_IMAGE_ID_PATH = RESOURCE_ROOT_PATH + "data\\content\\acc_val_index_2_image_id.txt";
    public static String ACC_VAL_DATASET_CONTENT_PLOT_RESULT_DIR = RESOURCE_ROOT_PATH + "plot\\content";

    // demo（小数据集）
    public static String ACC_DEMO_DATASET_FEATURE_FINGER_PRINT_JSON_PATH = RESOURCE_ROOT_PATH + "data\\content\\acc_demo_feature.json";
    public static String ACC_DEMO_DATASET_FEATURE_COMPARE_RESULT_JSON_PATH = RESOURCE_ROOT_PATH + "data\\content\\acc_demo_feature_compare_result.json";
    public static String ACC_DEMO_DATASET_INDEX_2_IMAGE_ID_PATH = RESOURCE_ROOT_PATH + "data\\content\\acc_demo_index_2_image_id.txt";
    public static String ACC_DEMO_DATASET_CONTENT_PLOT_RESULT_DIR = "plot\\content";

    // ==========================================文本检索==========================================
    public static String ACC_VAL_CAPTION_TAG_FILE_PATH = RESOURCE_ROOT_PATH + "F:\\projects\\image-retrieval\\src\\main\\resources\\json\\sentence\\caption_validation_tags_20170910.json";
    public static String ACC_VAL_CAPTION_ANNOTATION_FILE_PATH = "F:\\projects\\image-retrieval\\src\\main\\resources\\json\\sentence\\caption_validation_annotations_20170910.json";
    public static String ACC_VAL_INDEX_MAP_IMAGE_ID_FILE = "F:\\projects\\image-retrieval\\src\\main\\resources\\json\\val_idx_map_image_id.txt";
    public static String ACC_VAL_DATASET_SEMANTIC_PLOT_RESULT_DIR = "plot\\content";



}
