package com.example.starter.config;

import com.example.starter.auth.SessionUser;
import com.example.starter.auth.UserService;
import com.example.starter.common.ApiResponse;
import com.example.starter.common.JsonResponseWriter;
import jakarta.servlet.DispatcherType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserService users, JsonResponseWriter json) throws Exception {
        http.userDetailsService(users)
            .csrf(csrf -> csrf.csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                .csrfTokenRequestHandler(new XorCsrfTokenRequestAttributeHandler()))
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/auth/csrf").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .requestCache(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(form -> form.loginPage("/api/auth/login").loginProcessingUrl("/api/auth/login")
                .successHandler((req, res, auth) -> json.write(res, 200, ApiResponse.ok(((SessionUser) auth.getPrincipal()).view())))
                .failureHandler((req, res, ex) -> {
                    if (ex instanceof AuthenticationServiceException) {
                        log.error("登录服务异常", ex);
                        json.write(res, 500, ApiResponse.error(500, "服务暂时不可用"));
                    } else {
                        json.write(res, 401, ApiResponse.error(401, "账号或密码错误，或账号已停用"));
                    }
                }))
            .logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/api/auth/logout", "POST"))
                .invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID")
                .logoutSuccessHandler((req, res, auth) -> {
                    if (auth == null) json.write(res, 401, ApiResponse.error(401, "请先登录"));
                    else json.write(res, 200, ApiResponse.ok(null));
                }))
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((req, res, ex) -> json.write(res, 401, ApiResponse.error(401, "请先登录")))
                .accessDeniedHandler((req, res, ex) -> json.write(res, 403, ApiResponse.error(403, "无权限或安全校验已失效，请刷新后重试"))));
        return http.build();
    }
}
