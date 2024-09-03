package com.dahua.retrieval.service.impl;

import com.dahua.retrieval.service.SimilarityCalculate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.dahua.retrieval.constant.Constant.AI_CHALLENGER_CAPTION_VAL_ROOT_PATH;
import static com.dahua.retrieval.utils.FileUtil.calcMemory;
import static com.dahua.retrieval.utils.FileUtil.getFileAbsolutePaths;

public class PerceptualHashAlgorithm implements SimilarityCalculate {

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
        List<String> imageList = getFileAbsolutePaths(AI_CHALLENGER_CAPTION_VAL_ROOT_PATH);
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
        List<String> imageList = getFileAbsolutePaths(AI_CHALLENGER_CAPTION_VAL_ROOT_PATH);
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

    public static void main(String[] args) throws IOException {

        // 单线程读取
//        readFingerPrintListSingleThread();
        // 多线程读取
        Map<String, FingerPrint> path2FingerMap = readFingerPrintListMultiThread();

        calcMemory();

        int compareNum = 2;
        // 获取前10个元素
        List<Map.Entry<String, FingerPrint>> first10Entries = new ArrayList<>(path2FingerMap.entrySet())
                .stream()
                .limit(compareNum)
                .collect(Collectors.toList());
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
            Map<String, Float> res = fp1.batchCompare(path2FingerMap);
            // 按照值从大到小排序并取前20个元素
            List<Map.Entry<String, Float>> top20Entries = res.entrySet().stream()
                    .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                    .limit(20)
                    .collect(Collectors.toList());
            plotSimilarImageList(ip1, top20Entries, 5);

            System.out.println(top20Entries);
            System.out.println("比对" + first10Entries.size() + "张图片，运行时间" + (System.currentTimeMillis() - startTime) + "毫秒");
        }

        long sumTime = (System.currentTimeMillis() - startTime) ;
        System.out.println("==================读取图片" + first10Entries.size() + "张==================");
        System.out.println("==================运行时间：" + sumTime + "毫秒==================");
    }
}
