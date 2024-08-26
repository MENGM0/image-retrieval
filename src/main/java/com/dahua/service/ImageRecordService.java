package com.dahua.service;

import com.dahua.entity.ImageRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * (ImageRecord)表服务接口
 *
 * @author makejava
 * @since 2024-08-24 23:09:37
 */
public interface ImageRecordService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ImageRecord queryById(Integer id);

    /**
     * 分页查询
     *
     * @param imageRecord 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
    Page<ImageRecord> queryByPage(ImageRecord imageRecord, PageRequest pageRequest);

    /**
     * 新增数据
     *
     * @param imageRecord 实例对象
     * @return 实例对象
     */
    ImageRecord insert(ImageRecord imageRecord);

    /**
     * 修改数据
     *
     * @param imageRecord 实例对象
     * @return 实例对象
     */
    ImageRecord update(ImageRecord imageRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

}
