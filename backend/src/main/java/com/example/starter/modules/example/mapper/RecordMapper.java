package com.example.starter.modules.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.starter.modules.example.entity.RecordEntity;
import com.example.starter.modules.example.entity.RecordView;
import org.apache.ibatis.annotations.Param;

public interface RecordMapper extends BaseMapper<RecordEntity> {
    IPage<RecordView> selectRecords(Page<RecordView> page, @Param("ownerId") Long ownerId, @Param("keyword") String keyword, @Param("status") String status);
    RecordView selectRecord(@Param("id") Long id, @Param("ownerId") Long ownerId);
}
