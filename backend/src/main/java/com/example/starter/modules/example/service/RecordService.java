package com.example.starter.modules.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.starter.common.ApiException;
import com.example.starter.common.PageResult;
import com.example.starter.modules.example.entity.*;
import com.example.starter.modules.example.mapper.RecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecordService {
    private final RecordMapper mapper;
    public RecordService(RecordMapper mapper) { this.mapper = mapper; }

    public PageResult<RecordView> list(Long ownerId, int page, int size, String keyword, String status) {
        if (page < 1 || size < 1 || size > 100 || (keyword != null && keyword.length() > 100)) throw new ApiException(400, "查询参数不合法");
        if (status != null) validStatus(status);
        String escaped = keyword == null || keyword.isEmpty() ? null
            : "%" + keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        var result = mapper.selectRecords(new Page<>(page, size), ownerId, escaped, status);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }
    public RecordView detail(Long id, Long ownerId) {
        validId(id);
        var record = mapper.selectRecord(id, ownerId);
        if (record == null) throw ApiException.notFound();
        return record;
    }
    @Transactional
    public RecordView create(RecordInput input, Long currentUserId) {
        String status = input.status() == null ? "ENABLED" : validStatus(input.status());
        var entity = new RecordEntity();
        entity.setTitle(input.title()); entity.setDescription(input.description());
        entity.setStatus(status); entity.setOwnerId(currentUserId);
        mapper.insert(entity);
        return detail(entity.getId(), currentUserId);
    }
    @Transactional
    public RecordView update(Long id, RecordInput input, Long ownerId) {
        detail(id, ownerId);
        String status = validStatus(input.status());
        var update = new UpdateWrapper<RecordEntity>().eq("id", id).eq(ownerId != null, "owner_id", ownerId)
            .set("title", input.title()).set("description", input.description()).set("status", status);
        if (mapper.update(null, update) == 0) throw ApiException.notFound();
        return detail(id, ownerId);
    }
    @Transactional
    public void delete(Long id, Long ownerId) {
        validId(id);
        if (mapper.delete(new QueryWrapper<RecordEntity>().eq("id", id).eq(ownerId != null, "owner_id", ownerId)) == 0) throw ApiException.notFound();
    }
    private void validId(Long id) { if (id == null || id < 1) throw new ApiException(400, "ID不合法"); }
    private String validStatus(String status) {
        if (!"ENABLED".equals(status) && !"DISABLED".equals(status)) throw new ApiException(400, "状态必须为ENABLED或DISABLED");
        return status;
    }
}
