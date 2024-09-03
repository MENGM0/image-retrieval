package com.dahua.retrieval.service.impl;
import com.dahua.retrieval.service.SimilarityCalculate;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import static com.dahua.retrieval.constant.Constant.AI_CHALLENGER_CAPTION_VAL_ROOT_PATH;
import static com.dahua.retrieval.utils.FileUtil.getFileAbsolutePaths;

/**
 * 均值哈希实现图像指纹比较
 * @author guyadong
 *
 */
public class FingerPrint implements SimilarityCalculate {
    /**
     * 图像指纹的尺寸,将图像resize到指定的尺寸，来计算哈希数组
     */
    private static final int HASH_SIZE=16;
    /**
     * 保存图像指纹的二值化矩阵
     */
    private final byte[] binaryzationMatrix;
    public FingerPrint(byte[] hashValue) {
        if(hashValue.length!=HASH_SIZE*HASH_SIZE)
            throw new IllegalArgumentException(String.format("length of hashValue must be %d",HASH_SIZE*HASH_SIZE ));
        this.binaryzationMatrix=hashValue;
    }
    public FingerPrint(String hashValue) {
        this(toBytes(hashValue));
    }
    public FingerPrint (BufferedImage src){
        this(hashValue(src));
    }
    private static byte[] hashValue(BufferedImage src){
        BufferedImage hashImage = resize(src,HASH_SIZE,HASH_SIZE);
        byte[] matrixGray = (byte[]) toGray(hashImage).getData().getDataElements(0, 0, HASH_SIZE, HASH_SIZE, null);
        return  binaryzation(matrixGray);
    }
    /**
     * 从压缩格式指纹创建{@link FingerPrint}对象
     * @param compactValue
     * @return
     */
    public static FingerPrint createFromCompact(byte[] compactValue){
        return new FingerPrint(uncompact(compactValue));
    }

    public static boolean validHashValue(byte[] hashValue){
        if(hashValue.length!=HASH_SIZE)
            return false;
        for(byte b:hashValue){
            if(0!=b&&1!=b)return false;
        }
        return true;
    }
    public static boolean validHashValue(String hashValue){
        if(hashValue.length()!=HASH_SIZE)
            return false;
        for(int i=0;i<hashValue.length();++i){
            if('0'!=hashValue.charAt(i)&&'1'!=hashValue.charAt(i))return false;
        }
        return true;
    }
    public byte[] compact(){
        return compact(binaryzationMatrix);
    }

    /**
     * 指纹数据按位压缩
     * @param hashValue
     * @return
     */
    private static byte[] compact(byte[] hashValue){
        byte[] result=new byte[(hashValue.length+7)>>3];
        byte b=0;
        for(int i=0;i<hashValue.length;++i){
            if(0==(i&7)){
                b=0;
            }
            if(1==hashValue[i]){
                b|=1<<(i&7);
            }else if(hashValue[i]!=0)
                throw new IllegalArgumentException("invalid hashValue,every element must be 0 or 1");
            if(7==(i&7)||i==hashValue.length-1){
                result[i>>3]=b;
            }
        }
        return result;
    }

    /**
     * 压缩格式的指纹解压缩
     * @param compactValue
     * @return
     */
    private static byte[] uncompact(byte[] compactValue){
        byte[] result=new byte[compactValue.length<<3];
        for(int i=0;i<result.length;++i){
            if((compactValue[i>>3]&(1<<(i&7)))==0)
                result[i]=0;
            else
                result[i]=1;
        }
        return result;
    }
    /**
     * 字符串类型的指纹数据转为字节数组
     * @param hashValue
     * @return
     */
    private static byte[] toBytes(String hashValue){
        hashValue=hashValue.replaceAll("\\s", "");
        byte[] result=new byte[hashValue.length()];
        for(int i=0;i<result.length;++i){
            char c = hashValue.charAt(i);
            if('0'==c)
                result[i]=0;
            else if('1'==c)
                result[i]=1;
            else
                throw new IllegalArgumentException("invalid hashValue String");
        }
        return result;
    }
    /**
     * 缩放图像到指定尺寸
     * @param src
     * @param width
     * @param height
     * @return
     */
    private static BufferedImage resize(Image src,int width,int height){
        BufferedImage result = new BufferedImage(width, height,
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = result.getGraphics();
        try{
            g.drawImage(src.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        }finally{
            g.dispose();
        }
        return result;
    }
    /**
     * 计算均值
     * @param src
     * @return
     */
    private static  int mean(byte[] src){
        long sum=0;
        // 将数组元素转为无符号整数
        for(byte b:src)sum+=(long)b&0xff;
        return (int) (Math.round((float)sum/src.length));
    }
    /**
     * 二值化处理
     * @param src
     * @return
     */
    private static byte[] binaryzation(byte[]src){
        byte[] dst = src.clone();
        int mean=mean(src);
        for(int i=0;i<dst.length;++i){
            // 将数组元素转为无符号整数再比较
            dst[i]=(byte) (((int)dst[i]&0xff)>=mean?1:0);
        }
        return dst;

    }
    /**
     * 转灰度图像
     * @param src
     * @return
     */
    private static BufferedImage toGray(BufferedImage src){
        if(src.getType()==BufferedImage.TYPE_BYTE_GRAY){
            return src;
        }else{
            // 图像转灰
            BufferedImage grayImage = new BufferedImage(src.getWidth(), src.getHeight(),
                    BufferedImage.TYPE_BYTE_GRAY);
            new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(src, grayImage);
            return grayImage;
        }
    }

    @Override
    public String toString() {
        return toString(true);
    }
    /**
     * @param multiLine 是否分行
     * @return
     */
    public String toString(boolean multiLine) {
        StringBuffer buffer=new StringBuffer();
        int count=0;
        for(byte b:this.binaryzationMatrix){
            buffer.append(0==b?'0':'1');
            if(multiLine&&++count%HASH_SIZE==0)
                buffer.append('\n');
        }
        return buffer.toString();
    }
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FingerPrint){
            return Arrays.equals(this.binaryzationMatrix,((FingerPrint)obj).binaryzationMatrix);
        }else
            return super.equals(obj);
    }

    /**
     * 与指定的压缩格式指纹比较相似度
     * @param compactValue
     * @return
     * @see #compare(FingerPrint)
     */
    public float compareCompact(byte[] compactValue){
        return compare(createFromCompact(compactValue));
    }
    /**
     * @param hashValue
     * @return
     * @see #compare(FingerPrint)
     */
    public float compare(String hashValue){
        return compare(new FingerPrint(hashValue));
    }
    /**
     * 与指定的指纹比较相似度
     * @param hashValue
     * @return
     * @see #compare(FingerPrint)
     */
    public float compare(byte[] hashValue){
        return compare(new FingerPrint(hashValue));
    }
    /**
     * 与指定图像比较相似度
     * @param image2
     * @return
     * @see #compare(FingerPrint)
     */
    public float compare(BufferedImage image2){
        return compare(new FingerPrint(image2));
    }
    /**
     * 比较指纹相似度
     * @param src
     * @return
     * @see #compare(byte[], byte[])
     */
    public float compare(FingerPrint src){
        if(src.binaryzationMatrix.length!=this.binaryzationMatrix.length)
            throw new IllegalArgumentException("length of hashValue is mismatch");
        return compare(binaryzationMatrix,src.binaryzationMatrix);
    }

    public Map<String, Float> batchCompare(Map<String, FingerPrint> srcMap){
        Map<String, Float> res = new HashMap<>();
        for (Map.Entry<String, FingerPrint> entry : srcMap.entrySet()){
            res.put(entry.getKey(), compare(entry.getValue()));
        }
        return res;
    }
    /**
     * 判断两个数组相似度，数组长度必须一致否则抛出异常
     * @param f1
     * @param f2
     * @return 返回相似度(0.0~1.0)
     */
    private static float compare(byte[] f1,byte[] f2){
        if(f1.length!=f2.length)
            throw new IllegalArgumentException("mismatch FingerPrint length");
        int sameCount=0;
        for(int i=0;i<f1.length;++i){
            if(f1[i]==f2[i])++sameCount;
        }
        return (float)sameCount/f1.length;
    }
    public static float compareCompact(byte[] f1,byte[] f2){
        return compare(uncompact(f1),uncompact(f2));
    }
    public static float compare(BufferedImage image1,BufferedImage image2){
        return new FingerPrint(image1).compare(new FingerPrint(image2));
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

    //todo:
    // 实际：

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


        //调用算法代码处理图片数据
//        FingerPrint fp1 = new FingerPrint(ImageIO.read(new File("F:\\projects\\java-learn\\src\\main\\resources\\static\\000c405f0389c9a2a5f1f419451140944a024211.jpg")));
//        FingerPrint fp2 =new FingerPrint(ImageIO.read(new File("F:\\projects\\java-learn\\src\\main\\resources\\static\\000ca7b1800c44ed2351f9465e7c7daa14304f66.jpg")));
//        System.out.println(fp1.toString(true));
//        //计算两张图片的相似度
//        System.out.printf("sim=%f",fp1.compare(fp2));
//        DecimalFormat df = new DecimalFormat("0.00%");

    }
}
