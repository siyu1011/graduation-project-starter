package com.example.starter;

import com.example.starter.auth.*;
import com.example.starter.common.*;
import com.example.starter.config.SecurityConfig;
import com.example.starter.modules.example.controller.RecordController;
import com.example.starter.modules.example.service.RecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {AuthController.class, RecordController.class}, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, JsonResponseWriter.class, ApiExceptionHandler.class})
class SecurityWebTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder passwords;
    @MockitoBean UserService users;
    @MockitoBean RecordService records;

    @BeforeEach void accounts() {
        String hash = passwords.encode("User123!");
        when(users.loadUserByUsername("user")).thenReturn(new SessionUser(2L, "user", "演示用户", "USER", hash, true));
        when(users.loadUserByUsername("disabled")).thenReturn(new SessionUser(4L, "disabled", "停用用户", "USER", hash, false));
    }
    record Token(MockHttpSession session, String name, String value) {}
    Token token(MockHttpSession session) throws Exception {
        var request = get("/api/auth/csrf");
        if (session != null) request.session(session);
        var result = mvc.perform(request).andExpect(status().isOk()).andReturn();
        var data = json.readTree(result.getResponse().getContentAsString()).get("data");
        return new Token((MockHttpSession) result.getRequest().getSession(), data.get("headerName").asText(), data.get("token").asText());
    }
    MockHttpSession login() throws Exception {
        var csrf = token(null);
        var result = mvc.perform(post("/api/auth/login").session(csrf.session()).header(csrf.name(), csrf.value())
            .param("username", "user").param("password", "User123!"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value("2"))
            .andExpect(jsonPath("$.data.password").doesNotExist()).andReturn();
        return (MockHttpSession) result.getRequest().getSession();
    }
    @Test void anonymousReadsAreJson401() throws Exception {
        mvc.perform(get("/api/records")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value(401));
        mvc.perform(get("/api/admin/records")).andExpect(status().isUnauthorized());
    }
    @Test void loginRequiresCsrf() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", "user").param("password", "User123!"))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value(403));
    }
    @Test void authenticationServiceFailureIs500WithoutInternalDetails() throws Exception {
        when(users.loadUserByUsername("unavailable")).thenThrow(
            new org.springframework.security.authentication.InternalAuthenticationServiceException("database connection unavailable"));
        var csrf = token(null);
        mvc.perform(post("/api/auth/login").session(csrf.session()).header(csrf.name(), csrf.value())
            .param("username", "unavailable").param("password", "anything"))
            .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("服务暂时不可用")).andExpect(jsonPath("$.data").isEmpty());
    }
    @Test void wrongPasswordAndDisabledAccountAreRejected() throws Exception {
        for (String username : new String[]{"user", "disabled"}) {
            var csrf = token(null);
            mvc.perform(post("/api/auth/login").session(csrf.session()).header(csrf.name(), csrf.value())
                .param("username", username).param("password", username.equals("user") ? "bad" : "User123!"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.data").isEmpty());
        }
    }
    @Test void loginPersistsAuthenticationAndRotatesTokenAndSession() throws Exception {
        var before = token(null);
        String oldId = before.session().getId();
        mvc.perform(post("/api/auth/login").session(before.session()).header(before.name(), before.value())
            .param("username", "user").param("password", "User123!")).andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertNotEquals(oldId, before.session().getId());
        mvc.perform(get("/api/auth/me").session(before.session())).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.role").value("USER")).andExpect(jsonPath("$.data.passwordHash").doesNotExist());
        mvc.perform(post("/api/records").session(before.session()).header(before.name(), before.value())
            .contentType("application/json").content("{\"title\":\"test\"}")).andExpect(status().isForbidden());
        verifyNoInteractions(records);
    }
    @Test void userCannotEnterManagementEvenWithValidCsrf() throws Exception {
        var session = login();
        var csrf = token(session);
        mvc.perform(get("/api/admin/records").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/records").session(session).header(csrf.name(), csrf.value())
            .contentType("application/json").content("{\"title\":\"test\"}")).andExpect(status().isForbidden());
        verifyNoInteractions(records);
    }
    @Test void invalidAndMissingCsrfCannotWrite() throws Exception {
        var session = login();
        mvc.perform(delete("/api/records/1").session(session)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/records/1").session(session).header("X-CSRF-TOKEN", "invalid")).andExpect(status().isForbidden());
        verifyNoInteractions(records);
    }
    @Test void logoutInvalidatesSessionAndAnonymousLogoutIs401() throws Exception {
        var session = login(); var csrf = token(session);
        mvc.perform(post("/api/auth/logout").session(session).header(csrf.name(), csrf.value()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
        org.junit.jupiter.api.Assertions.assertTrue(session.isInvalid());
        mvc.perform(get("/api/records")).andExpect(status().isUnauthorized());
        var anonymous = token(null);
        mvc.perform(post("/api/auth/logout").session(anonymous.session()).header(anonymous.name(), anonymous.value()))
            .andExpect(status().isUnauthorized());
    }
    @Test void unknownFieldsAndInvalidBodyAre400() throws Exception {
        var csrf = token(login());
        for (String body : new String[]{"{\"title\":\"ok\",\"ownerId\":3}", "{\"title\":\"  \"}", "{\"title\":\"ok\",\"description\":\"" + "a".repeat(2001) + "\"}"}) {
            mvc.perform(post("/api/records").session(csrf.session()).header(csrf.name(), csrf.value())
                .contentType("application/json").content(body)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        }
        verifyNoInteractions(records);
    }
    @Test void authenticatedUnknownPathIs404Json() throws Exception {
        mvc.perform(get("/api/unknown").session(login())).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404)).andExpect(content().contentTypeCompatibleWith("application/json"));
    }
}
