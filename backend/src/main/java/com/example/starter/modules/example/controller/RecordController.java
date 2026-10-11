package com.example.starter.modules.example.controller;

import com.example.starter.auth.SessionUser;
import com.example.starter.common.ApiResponse;
import com.example.starter.common.PageResult;
import com.example.starter.modules.example.entity.*;
import com.example.starter.modules.example.service.RecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/records", "/api/admin/records"})
public class RecordController {
    private final RecordService service;
    public RecordController(RecordService service) { this.service = service; }
    // ADMIN在门户仍只访问自己的记录；管理前缀由SecurityFilterChain独立授权。
    private Long scope(HttpServletRequest request, SessionUser user) {
        return request.getServletPath().startsWith("/api/admin/") ? null : user.id();
    }
    @GetMapping
    public ApiResponse<PageResult<RecordView>> list(HttpServletRequest request, @AuthenticationPrincipal SessionUser user,
        @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword, @RequestParam(required = false) String status) {
        return ApiResponse.ok(service.list(scope(request, user), page, size, keyword, status));
    }
    @GetMapping("/{id}")
    public ApiResponse<RecordView> detail(@PathVariable Long id, HttpServletRequest request, @AuthenticationPrincipal SessionUser user) {
        return ApiResponse.ok(service.detail(id, scope(request, user)));
    }
    @PostMapping
    public ApiResponse<RecordView> create(@Valid @RequestBody RecordInput input, @AuthenticationPrincipal SessionUser user) {
        return ApiResponse.ok(service.create(input, user.id()));
    }
    @PutMapping("/{id}")
    public ApiResponse<RecordView> update(@PathVariable Long id, @Valid @RequestBody RecordInput input, HttpServletRequest request, @AuthenticationPrincipal SessionUser user) {
        return ApiResponse.ok(service.update(id, input, scope(request, user)));
    }
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request, @AuthenticationPrincipal SessionUser user) {
        service.delete(id, scope(request, user)); return ApiResponse.ok(null);
    }
}
