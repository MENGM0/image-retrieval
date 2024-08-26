package com.dahua.VideoPlatform.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.dahua.VideoPlatform.utils.FileUtil.getFileAbsolutePaths;
import static com.dahua.VideoPlatform.utils.FileUtil.getRandomPaths;
import static com.dahua.constant.Constant.TELEGRAM_IMAGE_ROOT_PATH;

@Service
public class QueryImageService {

    public static void main(String[] args) {
        System.out.println("hello world");
    }
    private static List<String> imagePathList = new ArrayList<>();

    static {
        // 初始化图片路径列表，实际应用中应从数据库或文件系统获取
        imagePathList = getFileAbsolutePaths(TELEGRAM_IMAGE_ROOT_PATH);
    }

    /**
     * 返回图片路径
     * @return
     */
    public List<String> getImageList(Integer count){
        return getRandomPaths(imagePathList, count);
    }

    // TODO: 向量化图片
    public void generateImageFeature(){
        //
    }


    // TODO: 以图搜图
    public List<String> searchImageByImage(String imagePath){
        return null;
    }
}
