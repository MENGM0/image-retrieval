package com.dahua.dao;

import com.dahua.entity.ImageRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * (ImageRecord)表数据库访问层
 *
 * @author makejava
 * @since 2024-08-24 23:09:34
 */
public interface ImageRecordDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ImageRecord queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param imageRecord 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
    List<ImageRecord> queryAllByLimit(ImageRecord imageRecord, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param imageRecord 查询条件
     * @return 总行数
     */
    long count(ImageRecord imageRecord);

    /**
     * 新增数据
     *
     * @param imageRecord 实例对象
     * @return 影响行数
     */
    int insert(ImageRecord imageRecord);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<ImageRecord> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<ImageRecord> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<ImageRecord> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<ImageRecord> entities);

    /**
     * 修改数据
     *
     * @param imageRecord 实例对象
     * @return 影响行数
     */
    int update(ImageRecord imageRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

