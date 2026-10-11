package com.example.starter.modules.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;

public record RecordView(
    @JsonSerialize(using = ToStringSerializer.class) Long id,
    String title, String description, String status,
    @JsonSerialize(using = ToStringSerializer.class) Long ownerId,
    String ownerUsername,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime updatedAt
) {}
