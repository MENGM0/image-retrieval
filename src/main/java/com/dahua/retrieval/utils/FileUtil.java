package com.dahua.retrieval.utils;

import com.dahua.retrieval.algorithm.FingerPrint;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.dahua.retrieval.constant.Constant.*;


public class FileUtil {

    public static void main(String[] args) {
        String dir = IMAGE_PATH;

//        List<String> fileName = getFileNames(dir);
        List<String> filePath = getFileAbsolutePaths(TELEGRAM_IMAGE_ROOT_PATH);
        List<String> randomFilePath = getRandomPaths(filePath, 5);
//        System.out.println(getRandomPaths(filePath, 5));
        System.out.println(filePath.size());
        System.out.println(getRandomPaths(filePath, 5));
    }

    public static List<String> getFileNames(String directoryPath){
        List<String> fileNames = new ArrayList<>();
        // 创建File对象
        File directory = new File(directoryPath);

        // 检查路径是否存在且为目录
        if (directory.exists() && directory.isDirectory()) {
            // 获取目录下所有文件/目录名称
            File[] files = directory.listFiles();
            // 检查是否有文件/目录存在
            if (files != null) {
                for (File file : files) {
                    // 添加文件名到列表中
                    fileNames.add(file.getName());
                }
            }
        } else {
            System.out.println("The provided path is not a valid directory or does not exist.");
        }
        return fileNames;
    }

    public static List<String> getFileAbsolutePaths(String directoryPath) {
        List<String> filePaths = new ArrayList<>();
        File directory = new File(directoryPath);

        // 递归方法，用于遍历目录
        traverseDirectory(directory, filePaths);

        return filePaths;
    }

    private static void traverseDirectory(File dir, List<String> filePaths) {
        if (dir.isDirectory()) {
            for (File item : Objects.requireNonNull(dir.listFiles())) {
                if (item.isDirectory()) {
                    // 如果是目录，则递归调用
                    traverseDirectory(item, filePaths);
                } else {
                    // 检查文件扩展名是否为jpg或png，如果是,则添加到列表中
                    if (item.getName().toLowerCase().endsWith(".jpg") ||
                            item.getName().toLowerCase().endsWith(".png")) {
                        filePaths.add(item.getAbsolutePath());
                    }
                }
            }
        }
    }

    // 随机获取路径列表中的n个路径
    public static List<String> getRandomPaths(List<String> filePaths, int n) {
        // 创建列表的副本以避免修改原始列表
        List<String> fileListCopy = new ArrayList<>(filePaths);
        // 打乱列表顺序
        Collections.shuffle(fileListCopy);
        // 获取前n个元素，如果n大于列表大小，则返回整个列表
        return fileListCopy.subList(0, Math.min(n, fileListCopy.size()));
    }

    public static void getIndexMapImageId(){
        List<String> imagePathList =  getFileAbsolutePaths(ACC_VAL_IMAGE_ROOT_PATH);
        List<JSONObject> mapJson = new ArrayList<>();

        for (int i = 1; i  <= imagePathList.size(); i++){
            String path = imagePathList.get(i-1);
            JSONObject obj = new JSONObject();//创建JSONObject对象
            obj.put("index", i);
            obj.put("image_id", new File(path).getName());
            mapJson.add(obj);
        }

        // 将List转换为JSONArray
        JSONArray jsonArray = new JSONArray(mapJson);
        // 写入文件
        try (FileWriter file = new FileWriter(ACC_VAL_INDEX_MAP_IMAGE_ID_FILE)) {
            file.write(jsonArray.toString(4)); // 4是缩进的空格数
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void calcMemory(){
        // 获取Runtime对象
        Runtime runtime = Runtime.getRuntime();

        // 获取总内存
        long totalMemory = runtime.totalMemory();

        // 获取空闲内存
        long freeMemory = runtime.freeMemory();

        // 获取已使用内存
        long usedMemory = totalMemory - freeMemory;

        // 打印内存信息
        System.out.println("总内存: " + totalMemory + " bytes");
        System.out.println("空闲内存: " + freeMemory + " bytes");
        System.out.println("已使用内存: " + usedMemory + " bytes");

        // 打印内存使用百分比
        double memoryUsagePercentage = (double) usedMemory / totalMemory * 100;
        System.out.println("内存使用百分比: " + memoryUsagePercentage + "%");
    }



}
