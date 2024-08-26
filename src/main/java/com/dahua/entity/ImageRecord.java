package com.dahua.entity;

import java.util.Date;
import java.io.Serializable;

/**
 * (ImageRecord)实体类
 *
 * @author makejava
 * @since 2024-08-24 23:09:35
 */
public class ImageRecord implements Serializable {
    private static final long serialVersionUID = -57254944880750700L;
    /**
     * 图片id
     */
    private Integer id;
    /**
     * 图片标签
     */
    private String imageTags;
    /**
     * 图片路径
     */
    private String imagePath;
    
    private Date createTime;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImageTags() {
        return imageTags;
    }

    public void setImageTags(String imageTags) {
        this.imageTags = imageTags;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}

