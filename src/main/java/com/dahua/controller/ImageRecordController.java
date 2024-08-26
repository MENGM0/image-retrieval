package com.dahua.controller;

import com.dahua.entity.ImageRecord;
import com.dahua.service.ImageRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//import javax.annotation.Resource;

/**
 * (ImageRecord)表控制层
 *
 * @author makejava
 * @since 2024-08-24 23:09:32
 */
@RestController
@RequestMapping("imageRecord")
public class ImageRecordController {
    /**
     * 服务对象
     */
//    @Resource
    private ImageRecordService imageRecordService;

    /**
     * 分页查询
     *
     * @param imageRecord 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
    @GetMapping
    public ResponseEntity<Page<ImageRecord>> queryByPage(ImageRecord imageRecord, PageRequest pageRequest) {
        return ResponseEntity.ok(this.imageRecordService.queryByPage(imageRecord, pageRequest));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public ResponseEntity<ImageRecord> queryById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(this.imageRecordService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param imageRecord 实体
     * @return 新增结果
     */
    @PostMapping
    public ResponseEntity<ImageRecord> add(ImageRecord imageRecord) {
        return ResponseEntity.ok(this.imageRecordService.insert(imageRecord));
    }

    /**
     * 编辑数据
     *
     * @param imageRecord 实体
     * @return 编辑结果
     */
    @PutMapping
    public ResponseEntity<ImageRecord> edit(ImageRecord imageRecord) {
        return ResponseEntity.ok(this.imageRecordService.update(imageRecord));
    }

    /**
     * 删除数据
     *
     * @param id 主键
     * @return 删除是否成功
     */
    @DeleteMapping
    public ResponseEntity<Boolean> deleteById(Integer id) {
        return ResponseEntity.ok(this.imageRecordService.deleteById(id));
    }

}

