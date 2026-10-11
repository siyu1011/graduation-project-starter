package com.example.starter.modules.example.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecordInput(
    @NotBlank(message = "标题不能为空") @Size(max = 100, message = "标题最多100字") String title,
    @Size(max = 2000, message = "描述最多2000字") String description,
    String status
) {
    public RecordInput {
        title = title == null ? null : title.strip();
        description = description == null ? "" : description;
    }
}
