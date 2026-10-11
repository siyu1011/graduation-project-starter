package com.example.starter.auth;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@TableName("sys_user")
public class UserEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    @JsonIgnore
    private String passwordHash;
    private String displayName;
    private String role;
    private Integer enabled;
    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public String getUsername() { return username; }
    public void setUsername(String value) { username = value; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String value) { passwordHash = value; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String value) { displayName = value; }
    public String getRole() { return role; }
    public void setRole(String value) { role = value; }
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer value) { enabled = value; }
}
