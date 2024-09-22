package com.dahua.retrieval.algorithm;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xm.Similarity;
import org.xm.tendency.word.HownetWordTendency;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.dahua.retrieval.common.constant.Constant.*;


public class CaptionProcess {

    private Map<String, List<String>> imageToTags;
    private Map<String, String> imageToCaption;
    private Map<String, List<String>> tagToImages;
    private List<String> tags;
    private Map<String, Integer> tagCount;

    public CaptionProcess(String tagPath, String annotationPath){
        this.imageToTags = new HashMap<>();
        this.tagToImages = new HashMap<>();
        this.imageToCaption = new HashMap<>();
        this.tagCount = new HashMap<>();
        this.tags = new ArrayList<>();
        try {
            // 读取文件内容到字符串
            String content = new String(Files.readAllBytes(Paths.get(tagPath)));
            String content2 = new String(Files.readAllBytes(Paths.get(annotationPath)));
            // 将字符串转换为JSONArray
            JSONArray jsonArray = new JSONArray(content);
            JSONArray jsonArray2 = new JSONArray(content2);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                JSONObject obj2 = jsonArray2.getJSONObject(i);
                JSONArray caption = (JSONArray) obj.get("caption");
                String imageId = obj.get("image_id").toString();
                String url = obj.get("url").toString();

                imageToCaption.put(obj2.get("image_id").toString(), obj2.get("caption").toString());

                ArrayList<String> tagList = new ArrayList<>();
                for (int j = 0; j < caption.length(); j++) {
                    String tag = caption.getString(j);
                    tagList.add(tag);
                    // tag库
                    if (!tags.contains(tag)){
                        tags.add(tag);
                        tagCount.put(tag, 1);
                    }else{
                        Integer count = tagCount.get(tag) + 1;
                        tagCount.put(tag, count);
                    }
                    // 检查Map中是否已经存在该key，如果存在，则向对应的List添加元素
                    if (tagToImages.containsKey(tag)) {
                        tagToImages.get(tag).add(imageId);
                    } else {
                        // 如果不存在，先创建一个新的List，然后添加元素，最后将List与key关联
                        List<String> imageList = new ArrayList<>();
                        imageList.add(imageId);
                        tagToImages.put(tag, imageList);
                    }
                }
                imageToTags.put(imageId, tagList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void search(String keyword) throws IOException {
        Map<String, Float> similarity = new HashMap<>();
        for (String tag: this.tags){
            similarity.put(tag, (float) Similarity.phraseSimilarity(keyword, tag));
        }
        List<Map.Entry<String, Float>> top50 = similarity.entrySet().stream()
                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                .limit(50)
                .collect(Collectors.toList());

        // 打印前50个元素
        top50.forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }

    public List<Map.Entry<String, Float>> searchSentence(String[] keywords) throws IOException {
        Map<String, Float> similarity = new HashMap<>();
        String phrase = String.join("", keywords);
        for (String imageId: this.imageToCaption.keySet()){
            String caption = this.imageToCaption.get(imageId);
            similarity.put(imageId, (float) Similarity.phraseSimilarity(phrase, caption));
        }
        List<Map.Entry<String, Float>> top50 = similarity.entrySet().stream()
                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                .limit(50)
                .collect(Collectors.toList());

        // 打印前50个元素
        top50.forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));

        createImageGridWithLabels(top50, 5, 100, 20, phrase);
        return top50;
    }
    public void searchSentence(String keyword) throws IOException {
        Map<String, Float> similarity = new HashMap<>();
        for (String imageId: this.imageToCaption.keySet()){
            String caption = this.imageToCaption.get(imageId);
            similarity.put(imageId, (float) Similarity.phraseSimilarity(keyword, caption));
        }
        List<Map.Entry<String, Float>> top50 = similarity.entrySet().stream()
                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                .limit(50)
                .collect(Collectors.toList());

        // 打印前50个元素
        top50.forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));

        createImageGridWithLabels(top50, 5, 100, 20, keyword);
    }

    public static void createImageGridWithLabels(List<Map.Entry<String, Float>> imageEntries, int columns, int imageSize, int textOffset, String name) throws IOException {
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
        ImageIO.write(gridImage, "png", new File(ACC_VAL_DATASET_SEMANTIC_PLOT_RESULT_DIR + "\\" + name + ".jpg"));
    }

    public static void main(String[] args) throws IOException {
        // demo
        double result = Similarity.cilinSimilarity("电动车", "自行车");
        System.out.println(result);
        String word = "混蛋";
        HownetWordTendency hownetWordTendency = new HownetWordTendency();
        result = hownetWordTendency.getTendency(word);
        System.out.println(word + "  词语情感趋势值：" + result);

        // process
        CaptionProcess captionProcess = new CaptionProcess(ACC_VAL_CAPTION_TAG_FILE_PATH, ACC_VAL_CAPTION_ANNOTATION_FILE_PATH);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("请输入一个字符串或字符串数组，输入'exit'退出程序：");
            String input = scanner.nextLine();
            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("程序已退出。");
                break;
            }
            try {
                // 尝试将输入转换为字符串数组
                String[] inputArray = input.split(",");
                // 处理字符串数组
                captionProcess.searchSentence(inputArray);
            } catch (Exception e) {
                // 如果输入不是有效的字符串数组，将其视为单个字符串
                captionProcess.searchSentence(new String[]{input});
            }
        }
        scanner.close();

        System.out.println("hello");
    }
}
