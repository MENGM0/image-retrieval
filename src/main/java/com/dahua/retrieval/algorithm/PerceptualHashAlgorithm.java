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
import java.util.stream.Collectors;

import static com.dahua.retrieval.common.constant.Constant.*;
import static com.dahua.retrieval.utils.FileUtil.getFileAbsolutePaths;

public class PerceptualHashAlgorithm {

    /**
     * 理论上：目前多线程，读取1000张图片，花费11秒，每张平均 0.011s； 对应 3w张，花费330s，即5.5分钟
     * 读取3w张图片，花费306秒。比对2张图片，运行时间82毫秒; 比对 29999张图片，运行时间1230秒，20.5分钟
     * TODO: 优化成矩阵运算
     * @return
     * @throws IOException
     */
    public static Map<String, FingerPrint> readFingerPrintListMultiThread() throws IOException {
        long startTime = System.currentTimeMillis();
        // 获取处理器的核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        System.out.println("corePoolSize: " + corePoolSize);
        // 图片路径数组
        List<String> imageList = getFileAbsolutePaths(ACC_VAL_IMAGE_ROOT_PATH);
//        imageList = imageList.subList(0, 1000);
        imageList = imageList.subList(0, 100);
        // 创建一个固定大小的线程池
        ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);
        Map<String, FingerPrint> path2FingerMap = new HashMap<>();
        for (String imagePath : imageList) {
            // 提交读取图片的任务到线程池
            executor.submit(() -> {
                try {
                    FingerPrint fp = new FingerPrint(ImageIO.read(new File(imagePath)));
                    path2FingerMap.put(imagePath, fp);
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
        System.out.println("==================读取图片" + path2FingerMap.size() + "张==================");
        System.out.println("==================运行时间：" + sumTime + "秒==================");
        return path2FingerMap;
    }

    /**
     * 目前单线程，读取1000张图片，花费34秒； 对应 3w张，花费1020s，即17分钟
     * @throws IOException
     */
    public static void readFingerPrintListSingleThread() throws IOException {
        long startTime = System.currentTimeMillis();
        // 所有图片加载到内存（3w）
        List<String> imageList = getFileAbsolutePaths(ACC_VAL_IMAGE_ROOT_PATH);
        imageList = imageList.subList(0, 1000);

        List<FingerPrint> fpList = new ArrayList<>();
        for (String imagePath: imageList){
            fpList.add(new FingerPrint(ImageIO.read(new File(imagePath))));
        }
        long sumTime = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("读取图片" + fpList.size() + "张");
        System.out.println("运行时间：" + sumTime + "秒");
    }




    public static void plotSimilarImageList(String path, List<Map.Entry<String, Float>> pathList, int N) throws IOException {
//        BufferedImage image1 = ImageIO.read(new File(path));
//        int width1 = image1.getWidth();
//        int height1 = image1.getHeight();
//
//        int totalImages = pathList.size() + 1;
//        int rows = (int) Math.ceil(totalImages / (double) N);
//        int cols = N;
//
//        int width2 = (int) Math.ceil((double) width1 / N);
//        int height2 = (int) Math.ceil((double) height1 / rows) * rows;
//
//        BufferedImage combinedImage = new BufferedImage(width1, height2, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = (Graphics2D) combinedImage.getGraphics();
//        g.drawImage(image1, 0, 0, null);
//
//        int count = 1;
//        for (Map.Entry<String, Float> entry : pathList) {
//            String subPath = entry.getKey();
//            BufferedImage image2 = ImageIO.read(new File(subPath));
//            g.drawImage(image2, (count - 1) % N * width2, (count - 1) / N * height2, null);
//            count++;
//        }
//
//        g.dispose();

        BufferedImage image1 = ImageIO.read(new File(path));
        int maxWidth = Math.max(image1.getWidth(), image1.getHeight());
        BufferedImage[] images = new BufferedImage[pathList.size() + 1];
        images[0] = image1;


        for (int i = 0; i < pathList.size(); i++) {
            BufferedImage img = ImageIO.read(new File(pathList.get(i).getKey()));
            double ratio = Math.min(maxWidth / (double) img.getWidth(), maxWidth / (double) img.getHeight());
            int newWidth = (int) (img.getWidth() * ratio);
            int newHeight = (int) (img.getHeight() * ratio);
            BufferedImage resizedImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resizedImg.createGraphics();
            g.drawImage(img, 0, 0, newWidth, newHeight, null);
            g.dispose();
            images[i + 1] = resizedImg;
        }

        int totalImages = images.length;
        int rows = (int) Math.ceil((double) totalImages / N);
        int cols = N;

        int width = cols * maxWidth;
        int height = rows * maxWidth;

        BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combinedImage.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < totalImages; i++) {
            int x = (i % cols) * maxWidth;
            int y = (i / cols) * maxWidth;
            g.drawImage(images[i], x, y, maxWidth, maxWidth, null);
        }

        g.dispose();
        ImageIO.write(combinedImage, "jpg", new File("F:\\projects\\image-retrieval\\src\\main\\resources\\static\\result.jpg"));

//        return combinedImage;
    }
    
    public static List<JSONObject> saveImageFingerPrintJson(String rootPath, String jsonPath){
        long startTime = System.currentTimeMillis();
        // 获取处理器的核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        System.out.println("corePoolSize: " + corePoolSize);
        // 图片路径数组
        List<String> imageList = getFileAbsolutePaths(rootPath);
        // 创建一个固定大小的线程池
        ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);
        Map<String, FingerPrint> path2FingerMap = new HashMap<>();
        List<JSONObject> featureJson = new ArrayList<>();
        for (String imagePath : imageList) {
            // 提交读取图片的任务到线程池
            executor.submit(() -> {
                try {
                    File imageFile = new File(imagePath);
                    FingerPrint fp = new FingerPrint(ImageIO.read(imageFile));
                    JSONObject obj = new JSONObject();//创建JSONObject对象
                    obj.put("feature_fp", fp.toString());
                    obj.put("image_id", imageFile.getName());
                    featureJson.add(obj);

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
        System.out.println("==================运行时间：" + sumTime + "秒==================");

        // 将List转换为JSONArray
        JSONArray jsonArray = new JSONArray(featureJson);
        // 写入文件
        try (FileWriter file = new FileWriter(jsonPath)) {
            file.write(jsonArray.toString(4)); // 4是缩进的空格数
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return featureJson;
    }

    public static Map<String, FingerPrint> loadJsonImageFeatureFP(String jsonPath){
        Map<String, FingerPrint> fingerPrintMap = new HashMap<>();
        try {
            // 读取文件内容到字符串
            String content = new String(Files.readAllBytes(Paths.get(jsonPath)));

            // 将字符串转换为JSONArray
            JSONArray jsonArray = new JSONArray(content);

            // 将JSONArray转换为List<JSONObject>
            List<JSONObject> jsonObjectList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
//                jsonObjectList.add(jsonArray.getJSONObject(i));
                JSONObject obj = jsonArray.getJSONObject(i);
                fingerPrintMap.put(obj.get("image_id").toString(), new FingerPrint(obj.get("feature_fp").toString()));
//                System.out.println(obj);
            }

            // 打印JSON对象列表
//            jsonObjectList.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println(fingerPrintMap);
        return fingerPrintMap;
    }

    // 跑demo
    public static void createImageGridWithLabels(List<Map.Entry<String, Float>> imageEntries, int columns, int imageSize, int textOffset) throws IOException {
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
                    Map.Entry<String, Float> entry = imageEntries.get(index);
                    // 读取图片
                    BufferedImage image = ImageIO.read(new File(ACC_VAL_IMAGE_ROOT_PATH + "\\" + entry.getKey()));
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
        ImageIO.write(gridImage, "png", new File(ACC_DEMO_CONTENT_PLOT_RESULT_PATH + "\\" + imageId));
    }
    public static void compareFpPlot(Map<String, FingerPrint> fingerPrintMap) throws IOException {
//        int compareNum = 2;
        int compareNum = fingerPrintMap.size();
        // 获取前10个元素
        List<Map.Entry<String, FingerPrint>> first10Entries = new ArrayList<>(fingerPrintMap.entrySet())
                .stream()
                .limit(compareNum)
                .collect(Collectors.toList());
        System.out.println("first10Entries: " +  first10Entries);
        long startTime = System.currentTimeMillis();
        for (Map.Entry<String, FingerPrint> entry1: first10Entries){
            // 比对图片
            String ip1 = entry1.getKey();
            FingerPrint fp1 = entry1.getValue();
            // 待比对图片列表
//            List<FingerPrint> fp2 = new ArrayList<>();
//            for (Map.Entry<String, FingerPrint> entry2 : path2FingerMap.entrySet()) {
//                if (!Objects.equals(entry1.getKey(), entry2.getKey())){
//                    fp2.add(entry2.getValue());
//                }
//            }
            // 比对
            Map<String, Float> res = fp1.batchCompare(fingerPrintMap);
            // 按照值从大到小排序并取前20个元素
            List<Map.Entry<String, Float>> top20Entries = res.entrySet().stream()
                    .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                    .limit(20)
                    .collect(Collectors.toList());
            createImageGridWithLabels(top20Entries, 5, 100, 20);
            System.out.println(top20Entries);
        }
        System.out.println("比对" + first10Entries.size() + "张图片，运行时间" + (System.currentTimeMillis() - startTime) + "毫秒");
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
            System.out.println("File has been read into imageNameMap.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 打印映射以验证结果
//        for (Map.Entry<Integer, String> entry : imageNameMap.entrySet()) {
//            System.out.println("Index: " + entry.getKey() + ", Image Path: " + entry.getValue());
//        }

        // 创建一个新的HashMap来存储反转的映射
        Map<String, Integer> reversedMap = new HashMap<>();
        // 遍历原始映射并反转键和值
        for (Map.Entry<Integer, String> entry : imageNameMap.entrySet()) {
            reversedMap.put(entry.getValue(), entry.getKey());
        }

        // 打印映射以验证结果
//        for (Map.Entry<String, Integer> entry : reversedMap.entrySet()) {
//            System.out.println("Index: " + entry.getKey() + ", Image Path: " + entry.getValue());
//        }
        switch (type){
            case 1:
                return reversedMap;
            default:
                return imageNameMap;
        }
    }



    /**
     * 跑结果
     *
     * [
     *      {"image_id": xxx, "top50": [{"image_id": xxx, "level": yyy}, ..., {"image_id": xxx, "level": yyy}]},
     *      {"image_id": xxx, "top50": [{"image_id": xxx, "level": yyy}, ..., {"image_id": xxx, "level": yyy}]},
     *      ...
     * ]
     */
    public static void compareFpJson(Map<String, FingerPrint> fingerPrintMap, String savePath) throws IOException {
//        int compareNum = 2;
        int compareNum = fingerPrintMap.size();
        // 获取前10个元素
        List<Map.Entry<String, FingerPrint>> first10Entries = new ArrayList<>(fingerPrintMap.entrySet())
                .stream()
                .limit(compareNum)
                .collect(Collectors.toList());
//        System.out.println("first10Entries: " +  first10Entries);
        long startTime = System.currentTimeMillis();
        List<JSONObject> resultJson = new ArrayList<>();
        int topN = 50;
        for (Map.Entry<String, FingerPrint> entry1: first10Entries){
            // 比对图片
            String ip1 = entry1.getKey();
            FingerPrint fp1 = entry1.getValue();
            // 比对
            Map<String, Float> res = fp1.batchCompare(fingerPrintMap);
            // 按照值从大到小排序并取前20个元素
            List<Map.Entry<String, Float>> topNEntries = res.entrySet().stream()
                    .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                    .limit(topN)
                    .collect(Collectors.toList());

            JSONObject obj1 = new JSONObject();//创建JSONObject对象
            List<JSONObject> topNJson = new ArrayList<>();
            for (int i = 1; i < topN; i++){
                JSONObject obj2 = new JSONObject();//创建JSONObject对象
                obj2.put("level", topNEntries.get(i).getValue());
                obj2.put("image_id", topNEntries.get(i).getKey());
                topNJson.add(obj2);
            }
            obj1.put("image_id", topNEntries.get(0).getKey());
            obj1.put("topN", topNJson);
            resultJson.add(obj1);
        }
        System.out.println("比对" + first10Entries.size() + "张图片，运行时间" + (System.currentTimeMillis() - startTime) + "毫秒");
        System.out.println("平均运行时间" + (System.currentTimeMillis() - startTime)/ first10Entries.size() + "毫秒" );

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
        System.out.println("写入" + first10Entries.size() + "张图片，运行时间" + (System.currentTimeMillis() - startTime)+ "毫秒");
        System.out.println("平均写入时间" + (System.currentTimeMillis() - startTime) / first10Entries.size() + "毫秒" );

    }


    /**
     * 压缩
     * [
     *      {"image_id": xxx, "image_list": [xxx, xxx, ..., xxx], "similar_score": [yy, yy, ..., yyy]},
     *      {"image_id": xxx, "image_list": [xxx, xxx, ..., xxx], "similar_score": [yy, yy, ..., yyy]},
     *      ...
     * ]
     * @param fingerPrintMap
     * @param savePath
     * @throws IOException
     */
    public static void compareFpJsonZip(Map<String, FingerPrint> fingerPrintMap, String savePath, String indexPath) throws IOException {
//        int compareNum = 2;

//        writeImageIdIndex(ACC_DEMO_IMAGE_ROOT_PATH, ACC_DEMO_IMAGE_ID_INDEX_PATH);  // demo
        writeImageIdIndex(ACC_VAL_IMAGE_ROOT_PATH, indexPath);  // val
        Map<String, Integer> imageIdToIndex = (Map<String, Integer>) readImageIdIndex(indexPath, 1);
        int compareNum = fingerPrintMap.size();
        // 获取前10个元素
        List<Map.Entry<String, FingerPrint>> first10Entries = new ArrayList<>(fingerPrintMap.entrySet())
                .stream()
                .limit(compareNum)
                .collect(Collectors.toList());
//        System.out.println("first10Entries: " +  first10Entries);
        long startTime = System.currentTimeMillis();
        List<JSONObject> resultJson = new LinkedList<>();
        int topN = TOP_N;
        for (Map.Entry<String, FingerPrint> entry1: first10Entries){
            // 比对图片
            String ip1 = entry1.getKey();
            FingerPrint fp1 = entry1.getValue();
            // 比对
            Map<String, Float> res = fp1.batchCompare(fingerPrintMap);
            // 按照值从大到小排序并取前20个元素
            List<Map.Entry<String, Float>> topNEntries = res.entrySet().stream()
                    .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                    .limit(topN + 1)
                    .collect(Collectors.toList());

            JSONObject obj1 = new JSONObject();//创建JSONObject对象
            List<String> similarImageList = new LinkedList<>();
            List<Float> similarScoreList = new LinkedList<>();
            for (int i = 1; i <= topN; i++){
                similarImageList.add(String.valueOf(imageIdToIndex.get(topNEntries.get(i).getKey())));
                similarScoreList.add(topNEntries.get(i).getValue());
            }
            obj1.put("similar_image", similarImageList);
            obj1.put("similar_score", similarScoreList);
            obj1.put("image_id", imageIdToIndex.get(topNEntries.get(0).getKey()));
            resultJson.add(obj1);
        }
        System.out.println("比对" + first10Entries.size() + "张图片，运行时间" + (System.currentTimeMillis() - startTime) + "毫秒");
        System.out.println("平均运行时间" + (System.currentTimeMillis() - startTime)/ first10Entries.size() + "毫秒" );

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
        System.out.println("写入" + first10Entries.size() + "张图片，运行时间" + (System.currentTimeMillis() - startTime)+ "毫秒");
        System.out.println("平均写入时间" + (System.currentTimeMillis() - startTime) / first10Entries.size() + "毫秒" );

    }

    // todo: 统计图片id是否重复
    public static void  statisticsDataset(String rootPath){
        List<String> imagePathList = getFileAbsolutePaths(rootPath);
        List<String> imageNameList = imagePathList.stream().map(p -> new File(p).getName()).distinct().toList();
        System.out.printf("Duplicate data： %b" , (imageNameList.size() == imageNameList.size()));
        System.out.println();
    }

    public static void simple(String rootPath){
        String indexPath = ACC_DEMO_IMAGE_ID_INDEX_PATH;
        List<String> imagePathList = getFileAbsolutePaths(rootPath);
        List<String> imageNameList = imagePathList.stream().map(p -> new File(p).getName()).distinct().toList();

    }

    // todo：定义一个数字1到3w的映射，不存图片id，存下标

    public static void main(String[] args) throws IOException {

        // 单线程读取
//        readFingerPrintListSingleThread();
        // 多线程读取
//        Map<String, FingerPrint> path2FingerMap = readFingerPrintListMultiThread();

        // 1. 抽取所有图片指纹信息，保存到json
//        saveImageFingerPrintJson(AI_CHALLENGER_CAPTION_VAL_ROOT_PATH, ACC_FEATURE_FINGER_PRINT_JSON_PATH);

        // 2. 跑demo：计算图片的top20相似图片，合并相似图片及其相似度，保存到一张图片内
//        Map<String, FingerPrint> fingerPrintMap = loadJsonImageFeatureFP(ACC_FEATURE_FINGER_PRINT_JSON_PATH);
//        compareFpPlot(fingerPrintMap);

        // 3. 跑结果：计算图片的top50【计算量：3w*3w】
//        Map<String, FingerPrint> fingerPrintMap = loadJsonImageFeatureFP(ACC_FEATURE_FINGER_PRINT_JSON_PATH);
//        compareFpJson(fingerPrintMap, ACC_VAL_FEATURE_COMPARE_RESULT_JSON_PATH);


        // todo: 按批次保存到 mysql，json文件太占内存了
        // 测试方案1： top50直接写入json
        // 测试方案2： 写入关联关系表（表行数指数级增加）
//        Map<String, FingerPrint> fingerPrintMap = loadJsonImageFeatureFP(ACC_DEMO_FEATURE_FINGER_PRINT_JSON_PATH);
//        compareFpJsonZip(fingerPrintMap, ACC_DEMO_FEATURE_COMPARE_RESULT_JSON_PATH, ACC_DEMO_IMAGE_ID_INDEX_PATH);

        Map<String, FingerPrint> fingerPrintMap = loadJsonImageFeatureFP(ACC_VAL_FEATURE_FINGER_PRINT_JSON_PATH);
        compareFpJsonZip(fingerPrintMap, ACC_VAL_FEATURE_COMPARE_RESULT_JSON_PATH, ACC_VAL_IMAGE_ID_INDEX_PATH);
//        simple(ACC_DEMO_IMAGE_ROOT_PATH);


        // 4. 跑结果，demo
//        saveImageFingerPrintJson(AI_CHALLENGER_CAPTION_DEMO_ROOT_PATH, ACC_FEATURE_FINGER_PRINT_DEMO_JSON_PATH);
//        Map<String, FingerPrint> fingerPrintMap = loadJsonImageFeatureFP(ACC_FEATURE_FINGER_PRINT_DEMO_JSON_PATH);
//        compareFpJson(fingerPrintMap, ACC_FEATURE_FINGER_PRINT_COMPARE_RESULT_JSON_PATH2);

        // 5. 统计是否有重复的image_id
//        statisticsDataset(AI_CHALLENGER_CAPTION_VAL_ROOT_PATH);
//        statisticsDataset(AI_CHALLENGER_CAPTION_TRAIN_ROOT_PATH);

    }
}
