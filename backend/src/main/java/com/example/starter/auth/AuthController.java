package com.example.starter.auth;

import com.example.starter.common.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record CsrfView(String token, String headerName) {}
    @GetMapping("/csrf")
    public ApiResponse<CsrfView> csrf(CsrfToken token) { return ApiResponse.ok(new CsrfView(token.getToken(), token.getHeaderName())); }
    @GetMapping("/me")
    public ApiResponse<CurrentUser> me(@AuthenticationPrincipal SessionUser user) { return ApiResponse.ok(user.view()); }
}
