package com.dahua.retrieval.algorithm;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.dahua.retrieval.common.constant.Constant.*;
import static com.dahua.retrieval.utils.FileUtil.getFileAbsolutePaths;

public class PerceptualHashAlgorithm {
    /**
     * 抽取图片的 pHash 特征
     * @param rootPath
     * @param jsonPath
     * @return
     */
    public static Map<String, FingerPrint> extractImageFeature(String rootPath, String jsonPath){
        //        ==================读取图片30000张==================
        //        ==================运行时间：47秒===================
        long startTime = System.currentTimeMillis();
        // 获取处理器的核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2 ;
        System.out.println("corePoolSize: " + corePoolSize);
        // 图片路径数组
        List<String> imageList = getFileAbsolutePaths(rootPath);
        // 创建一个固定大小的线程池
        ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);
        // 进度条
        int total = imageList.size();
        AtomicInteger progress = new AtomicInteger(0);
        Map<String, FingerPrint> imageIdFeatureMap = new HashMap<>();
        // 线程安全
        List<JSONObject> featureJson = Collections.synchronizedList(new ArrayList<>());
        for (String imagePath : imageList) {
            // 提交读取图片的任务到线程池
            executor.submit(() -> {
                try {
                    File imageFile = new File(imagePath);
                    FingerPrint fp = new FingerPrint(ImageIO.read(imageFile));
                    imageIdFeatureMap.put(imageFile.getName(), fp);
                    JSONObject obj = new JSONObject();
                    obj.put("feature_fp", fp.toString());
                    obj.put("image_id", imageFile.getName());
                    featureJson.add(obj);

                    progress.incrementAndGet();
                    for (int i = 1; i <= total; i += 1000) {
                        if (progress.get() == i){
                            System.out.println(String.format("# 处理进度 %d / %d", progress.get(), total));
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        // 关闭线程池，不接受新任务，等待已提交的任务完成
        executor.shutdown();
        try {
            // 等待直到所有任务完成或超时
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long sumTime = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("==================读取图片" + featureJson.size() + "张==================");
        System.out.println("==================运行时间：" + sumTime + "秒===================");

        // 将List转换为JSONArray
        JSONArray jsonArray = new JSONArray(featureJson);
        // 写入文件
        try (FileWriter file = new FileWriter(jsonPath)) {
            file.write(jsonArray.toString(4)); // 4是缩进的空格数
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageIdFeatureMap;
    }

    /**
     * 加载所有图片的特征
     * @param jsonPath
     * @return
     */
    public static Map<String, FingerPrint> loadImageFeature(String jsonPath){
        Map<String, FingerPrint> imageFeatureMap = new HashMap<>();
        try {
            // 读取文件内容到字符串
            String content = new String(Files.readAllBytes(Paths.get(jsonPath)));
            // 将字符串转换为JSONArray
            JSONArray jsonArray = new JSONArray(content);
            // 将JSONArray转换为List<JSONObject>
//            List<JSONObject> jsonObjectList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String imageId = obj.get("image_id").toString();
                FingerPrint featureFp = new FingerPrint(obj.get("feature_fp").toString());
                imageFeatureMap.put(imageId, featureFp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFeatureMap;
    }

    /**
     * 画图
     * @param imageEntries
     * @param columns
     * @param imageSize
     * @param textOffset
     * @throws IOException
     */
    public static void createImageGridWithLabels(List<Map.Entry<String, Double>> imageEntries, int columns, int imageSize, int textOffset) throws IOException {
        String imageId = imageEntries.get(1).getKey();
        int rows = (int) Math.ceil((double) imageEntries.size() / columns);

        // 计算整个网格的宽度和高度（包括文本高度）
        int gridWidth = columns * imageSize;
        int gridHeight = rows * (imageSize + textOffset);

        // 创建一个新的图像，用于绘制所有图片
        BufferedImage gridImage = new BufferedImage(gridWidth, gridHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = gridImage.createGraphics();

        // 设置背景色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, gridWidth, gridHeight);

        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (index < imageEntries.size()) {
                    Map.Entry<String, Double> entry = imageEntries.get(index);
                    // 读取图片
                    BufferedImage image = ImageIO.read(new File(ACC_VAL_DATASET_IMAGE_ROOT_PATH + "\\" + entry.getKey()));
                    // 调整图片大小
                    BufferedImage resizedImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = resizedImage.createGraphics();
                    g2.drawImage(image.getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH), 0, 0, null);
                    g2.dispose();

                    // 绘制图片到网格
                    g2d.drawImage(resizedImage, col * imageSize, row * (imageSize + textOffset), null);

                    // 绘制分数
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                    String label = String.valueOf(entry.getValue());
                    FontMetrics metrics = g2d.getFontMetrics();
                    int x = col * imageSize + (imageSize - metrics.stringWidth(label)) / 2;
                    int y = row * (imageSize + textOffset) + imageSize + textOffset - metrics.getDescent();
                    g2d.drawString(label, x, y);

                    index++;
                }
            }
        }

        g2d.dispose();

        // 保存最终的图片
        ImageIO.write(gridImage, "png", new File(ACC_VAL_DATASET_CONTENT_PLOT_RESULT_DIR + "\\" + imageId));
    }

    public static void plotPartialSimilarImages(Map<String, Map<String, Double>> similarList) throws IOException {
        // 创建一个List来存储结果
        List<Map.Entry<String, Double>> imageEntries = new ArrayList<>();
        int plotCount = 10;   // 写入图片数量
        int similarCount = 20;   // 20张相似图片
        int count = 0;
        for (Map.Entry<String, Map<String, Double>> outerEntry : similarList.entrySet()) {
            imageEntries.clear();
            // 添加图片本身
            imageEntries.add(new java.util.AbstractMap.SimpleEntry<>(outerEntry.getKey(), 1.0));
            // 相似图片列表
            Map<String, Double> innerMap = outerEntry.getValue();
            // 排序，取前20个相似度最高的图片
            List<Map.Entry<String, Double>> sortedEntries = innerMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .limit(similarCount)
                    .collect(Collectors.toList());
            // 将选择的Entry添加到结果列表中
            imageEntries.addAll(sortedEntries);
            // 更新计数器
            count++;
            if (count >= plotCount) {
                break;
            }
            createImageGridWithLabels(imageEntries, 5, 100, 20);
        }
    }


    /**
     * 路径下所有图片名称映射到index -> (imageId,  index)，节省存储空间
     * @param imageRootPath 图片路径
     * @param indexSavePath 索引存储路径
     */
    private static void writeImageIdIndex(String imageRootPath, String indexSavePath){
        List<String> imagePathList = getFileAbsolutePaths(imageRootPath);
        List<String> imageNameList = imagePathList.stream().map(p -> new File(p).getName()).distinct().toList();
        // 创建一个HashMap来存储映射
        Map<Integer, String> imageNameMap = new HashMap<>();

        // 将列表转换为映射
        for (int i = 1; i <= imageNameList.size(); i++) {
            imageNameMap.put(i, imageNameList.get(i-1));
        }
        // 使用try-with-resources语句自动关闭资源
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexSavePath))) {
            // 遍历映射并写入文件
            for (Map.Entry<Integer, String> entry : imageNameMap.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue());
                writer.newLine(); // 写入换行符，使得每对数据占一行
            }
            System.out.println
                    ("Map has been written to " + indexSavePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取图片id索引文件
     * @param indexSavePath
     * @param type
     * @return
     */
    public static Object readImageIdIndex(String indexSavePath, Integer type){
        // 创建一个HashMap来存储图像路径
        Map<Integer, String> imageNameMap = new HashMap<>();
        // 使用try-with-resources语句自动关闭资源
        try (BufferedReader reader = new BufferedReader(new FileReader(indexSavePath))) {
            String line;
            // 逐行读取文件
            while ((line = reader.readLine()) != null) {
                // 分割每行数据
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    try {
                        // 将索引和图像路径存储到映射中
                        int index = Integer.parseInt(parts[0]);
                        String imagePath = parts[1];
                        imageNameMap.put(index, imagePath);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing index: " + parts[0]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 创建一个新的HashMap来存储反转的映射
        Map<String, Integer> reversedMap = new HashMap<>();
        // 遍历原始映射并反转键和值
        for (Map.Entry<Integer, String> entry : imageNameMap.entrySet()) {
            reversedMap.put(entry.getValue(), entry.getKey());
        }
        switch (type){
            case 1:
                return reversedMap;
            default:
                return imageNameMap;
        }
    }


    /**
     * 加载相似图片列表
     * @param jsonPath
     * @param indexPath
     * @return
     */
    public static Map<String, Map<String, Double>> loadSimilarList(String jsonPath, String indexPath){
        Map<String, Map<String, Double>> similarList = new HashMap<>();
        try {
            // 读取文件内容到字符串
            String content = new String(Files.readAllBytes(Paths.get(jsonPath)));
            // 读取index文件
            Map<String, Integer> imageIdToIndex = (Map<String, Integer>) readImageIdIndex(indexPath, 1);
            Map<Integer, String> indexIdToImageId = imageIdToIndex.entrySet().stream().collect(Collectors.toMap(entry -> entry.getValue(), entry -> entry.getKey()));

            // 将字符串转换为JSONArray
            JSONArray jsonArray = new JSONArray(content);
            for (int i = 0; i < jsonArray.length(); i++) {
                Map<String, Double> imageScoreList = new HashMap<>();
                JSONObject obj = jsonArray.getJSONObject(i);
                String imageId = String.valueOf(indexIdToImageId.get(Integer.valueOf(obj.get("image_id").toString())));
                JSONArray similarJsonArray = (JSONArray) obj.get("similar_image");
                JSONArray scoreJsonArray = (JSONArray) obj.get("similar_score");
                for (int j = 0; j < similarJsonArray.length(); j++){
                    String similarImageId = String.valueOf(indexIdToImageId.get(Integer.valueOf(similarJsonArray.get(j).toString()) ));
                    Double similarScore = Double.valueOf(scoreJsonArray.get(j).toString());
                    imageScoreList.put(similarImageId, similarScore);
                }
                similarList.put(imageId, imageScoreList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return similarList;
    }
    /**
     * 压缩
     * [
     *      {"image_id": xxx, "image_list": [xxx, xxx, ..., xxx], "similar_score": [yy, yy, ..., yyy]},
     *      {"image_id": xxx, "image_list": [xxx, xxx, ..., xxx], "similar_score": [yy, yy, ..., yyy]},
     *      ...
     * ]
     * @param imageId2Feature
     * @param savePath
     * @throws IOException
     */
    public static void compareSimilarImageListByIndex(Map<String, FingerPrint> imageId2Feature, String savePath, String indexPath) throws IOException {
        writeImageIdIndex(ACC_VAL_DATASET_IMAGE_ROOT_PATH, indexPath);  // imageId map to index
        Map<String, Integer> imageIdToIndex = (Map<String, Integer>) readImageIdIndex(indexPath, 1);
        List<Map.Entry<String, FingerPrint>> entries = new ArrayList<>(imageId2Feature.entrySet())
                .stream()
                .collect(Collectors.toList());
        long startTime = System.currentTimeMillis();
        List<JSONObject> resultJson = Collections.synchronizedList(new ArrayList<>());
        int topN = TOP_N;
        // 进度条
        int total = imageId2Feature.size();
        AtomicInteger progress = new AtomicInteger(0);

        // 获取处理器的核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2 ;
        // 创建一个固定大小的线程池
        ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);

        Map<String, FingerPrint> imageIdFeatureMap = new HashMap<>();
        for (Map.Entry<String, FingerPrint> entry: entries){
            // 提交读取图片的任务到线程池
            executor.submit(() -> {
                // 主图片
                String imageId = entry.getKey();
                // 特征库
                FingerPrint fp1 = entry.getValue();
                // 图片批量比对
                Map<String, Double> res = fp1.batchCompare(imageId2Feature);
                // 按照值从大到小排序并取前topN个元素
                List<Map.Entry<String, Double>> topNEntries = res.entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .limit(topN)
                        .collect(Collectors.toList());

                JSONObject obj1 = new JSONObject();//创建JSONObject对象
                List<String> similarImageList = new LinkedList<>();
                List<Double> similarScoreList = new LinkedList<>();
                for (int i = 0; i < topN; i++){
                    similarImageList.add(String.valueOf(imageIdToIndex.get(topNEntries.get(i).getKey())));
                    similarScoreList.add(topNEntries.get(i).getValue());
                }
                obj1.put("similar_image", similarImageList);
                obj1.put("similar_score", similarScoreList);
                obj1.put("image_id", imageIdToIndex.get(imageId));
                resultJson.add(obj1);
                // 打印进度
                progress.incrementAndGet();
                for (int i = 1; i <= total; i += 1000) {
                    if (progress.get() == i){
                        System.out.println(String.format("# 处理进度 %d / %d", progress.get(), total));
                    }
                }
            });
        }
        // 关闭线程池，不接受新任务，等待已提交的任务完成
        executor.shutdown();
        try {
            // 等待直到所有任务完成或超时
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("比对" + total + "张图片，运行时间" + (System.currentTimeMillis() - startTime) + "毫秒");
        System.out.println("平均运行时间" + (System.currentTimeMillis() - startTime)/total + "毫秒" );

        startTime = System.currentTimeMillis();
        // 将List转换为JSONArray
        JSONArray jsonArray = new JSONArray(resultJson);
        // 写入文件
        try (FileWriter file = new FileWriter(savePath)) {
            file.write(jsonArray.toString(4)); // 4是缩进的空格数
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("写入" + total + "张图片，运行时间" + (System.currentTimeMillis() - startTime)+ "毫秒");
        System.out.println("平均写入时间" + (System.currentTimeMillis() - startTime) / total + "毫秒" );
        System.out.println("写入路径：" + savePath);
    }

    public static void processValDataset() throws IOException {
        // 1. 抽取所有图片指纹信息，保存到json（多线程读取）
//        extractImageFeature(ACC_VAL_DATASET_IMAGE_ROOT_PATH, ACC_VAL_DATASET_FEATURE_FINGER_PRINT_JSON_PATH);
//        // 2. 计算相似图片
//        // 2.1 读取图片特征
        Map<String, FingerPrint> imageId2Feature = loadImageFeature(ACC_VAL_DATASET_FEATURE_FINGER_PRINT_JSON_PATH);
        System.out.println("imageId2Feature.size() = " + imageId2Feature.size());
//        // 2.2 计算图片的相似图片(top50)
        compareSimilarImageListByIndex(imageId2Feature, ACC_VAL_DATASET_FEATURE_COMPARE_RESULT_JSON_PATH, ACC_VAL_DATASET_INDEX_2_IMAGE_ID_PATH);

        // 3. 画图
        // 3.1 加载相似图片列表
        Map<String, Map<String, Double>> similarList = loadSimilarList(ACC_VAL_DATASET_FEATURE_COMPARE_RESULT_JSON_PATH, ACC_VAL_DATASET_INDEX_2_IMAGE_ID_PATH);
        System.out.println("similarList.size() = " + similarList.size());
        // 3.2 画图，展示相似图片
//        plotPartialSimilarImages(similarList);

    }

    public static void processDemoDataset() throws IOException {
        // demo（小数据量，直接运行）
        extractImageFeature(ACC_DEMO_DATASET_IMAGE_ROOT_PATH, ACC_DEMO_DATASET_FEATURE_FINGER_PRINT_JSON_PATH);
        Map<String, FingerPrint> fingerPrintMap = loadImageFeature(ACC_DEMO_DATASET_FEATURE_FINGER_PRINT_JSON_PATH);
        compareSimilarImageListByIndex(fingerPrintMap, ACC_DEMO_DATASET_FEATURE_COMPARE_RESULT_JSON_PATH, ACC_DEMO_DATASET_INDEX_2_IMAGE_ID_PATH);
        Map<String, Map<String, Double>> similarList = loadSimilarList(ACC_DEMO_DATASET_FEATURE_COMPARE_RESULT_JSON_PATH, ACC_DEMO_DATASET_INDEX_2_IMAGE_ID_PATH);
        plotPartialSimilarImages(similarList);
    }


    public static void main(String[] args) throws IOException {
        processValDataset();
    }
}
