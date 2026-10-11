package com.example.starter.modules.example.entity;

import com.baomidou.mybatisplus.annotation.*;

@TableName("demo_record")
public class RecordEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String status;
    private Long ownerId;
    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long value) { ownerId = value; }
}
