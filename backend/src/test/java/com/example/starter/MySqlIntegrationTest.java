package com.example.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=${TEST_DB_URL}",
    "spring.datasource.username=${TEST_DB_USERNAME}",
    "spring.datasource.password=${TEST_DB_PASSWORD}"
})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".+")
@Transactional
class MySqlIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @BeforeAll static void isolatedDatabaseOnly() {
        assertTrue(System.getenv("TEST_DB_URL").matches("^jdbc:mysql://[^/]+/graduation_starter_test(?:\\?.*)?$"),
            "测试仅允许graduation_starter_test；禁止使用演示库");
    }
    record Token(MockHttpSession session, String name, String value) {}
    Token token(MockHttpSession session) throws Exception {
        var request = get("/api/auth/csrf");
        if (session != null) request.session(session);
        var result = mvc.perform(request).andExpect(status().isOk()).andReturn();
        var data = json.readTree(result.getResponse().getContentAsString()).get("data");
        return new Token((MockHttpSession) result.getRequest().getSession(), data.get("headerName").asText(), data.get("token").asText());
    }
    Token login(String username, String password) throws Exception {
        var csrf = token(null);
        var result = mvc.perform(post("/api/auth/login").session(csrf.session()).header(csrf.name(), csrf.value())
            .param("username", username).param("password", password)).andExpect(status().isOk()).andReturn();
        return token((MockHttpSession) result.getRequest().getSession());
    }
    Token user() throws Exception { return login("user", "User123!"); }
    Token admin() throws Exception { return login("admin", "Admin123!"); }
    @Test void eachRoleSeesCorrectListAndFiltersAndPaging() throws Exception {
        var u = user();
        mvc.perform(get("/api/records").servletPath("/api/records").session(u.session()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(12))
            .andExpect(jsonPath("$.data.records.length()").value(10)).andExpect(jsonPath("$.data.records[0].ownerId").value("2"))
            .andExpect(jsonPath("$.data.records[0].id").isString()).andExpect(jsonPath("$.data.page").value(1));
        mvc.perform(get("/api/records").session(u.session()).param("page","2")).andExpect(jsonPath("$.data.records.length()").value(2));
        mvc.perform(get("/api/records").session(u.session()).param("status","DISABLED")).andExpect(jsonPath("$.data.total").value(4));
        mvc.perform(get("/api/records").session(u.session()).param("keyword","100%_")).andExpect(jsonPath("$.data.total").value(1));
        mvc.perform(get("/api/records").session(u.session()).param("keyword","进度")).andExpect(jsonPath("$.data.total").value(1));
        mvc.perform(get("/api/records").session(u.session()).param("keyword","%")).andExpect(jsonPath("$.data.total").value(1));
        var a = admin();
        mvc.perform(get("/api/admin/records").servletPath("/api/admin/records").session(a.session()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(25));
        mvc.perform(get("/api/records").servletPath("/api/records").session(a.session())).andExpect(jsonPath("$.data.total").value(5));
        var second = login("user2", "User123!");
        mvc.perform(get("/api/records").session(second.session())).andExpect(jsonPath("$.data.total").value(8));
    }
    @Test void userCrudAndManagementChangesArePersisted() throws Exception {
        var u = user();
        var created = mvc.perform(post("/api/records").session(u.session()).header(u.name(),u.value())
            .contentType("application/json").content("{\"title\":\"  集成记录  \"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.title").value("集成记录"))
            .andExpect(jsonPath("$.data.description").value("")).andExpect(jsonPath("$.data.status").value("ENABLED"))
            .andExpect(jsonPath("$.data.ownerId").value("2")).andReturn();
        String id = json.readTree(created.getResponse().getContentAsString()).at("/data/id").asText();
        var a = admin();
        mvc.perform(put("/api/admin/records/" + id).servletPath("/api/admin/records/" + id).session(a.session()).header(a.name(),a.value())
            .contentType("application/json").content("{\"title\":\"已处理\",\"description\":\"管理员更新\",\"status\":\"DISABLED\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.ownerId").value("2"));
        mvc.perform(get("/api/records/" + id).session(u.session())).andExpect(jsonPath("$.data.title").value("已处理"))
            .andExpect(jsonPath("$.data.status").value("DISABLED"));
        assertEquals("已处理", jdbc.queryForObject("SELECT title FROM demo_record WHERE id=?", String.class, id));
        mvc.perform(delete("/api/records/" + id).session(u.session()).header(u.name(),u.value())).andExpect(status().isOk());
        mvc.perform(get("/api/records/" + id).session(u.session())).andExpect(status().isNotFound());
    }
    @Test void crossOwnerReadUpdateDeleteReturn404AndDoNotChangeData() throws Exception {
        var u = user();
        var before = jdbc.queryForMap("SELECT * FROM demo_record WHERE id=18");
        mvc.perform(get("/api/records/18").session(u.session())).andExpect(status().isNotFound());
        mvc.perform(put("/api/records/18").session(u.session()).header(u.name(),u.value())
            .contentType("application/json").content("{\"title\":\"越权\",\"status\":\"DISABLED\"}")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/records/18").session(u.session()).header(u.name(),u.value())).andExpect(status().isNotFound());
        assertEquals(before, jdbc.queryForMap("SELECT * FROM demo_record WHERE id=18"));
    }
    @Test void managementCreateAlwaysBelongsToCurrentAdmin() throws Exception {
        var a = admin();
        mvc.perform(post("/api/admin/records").servletPath("/api/admin/records").session(a.session()).header(a.name(),a.value())
            .contentType("application/json").content("{\"title\":\"管理新建\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.ownerId").value("1")).andExpect(jsonPath("$.data.ownerUsername").value("admin"));
    }
    @Test void badQueryValuesAre400() throws Exception {
        var u = user();
        for (var pair : new String[][]{{"page","0"},{"page","x"},{"size","0"},{"size","101"},{"status","BAD"},{"keyword","a".repeat(101)}}) {
            mvc.perform(get("/api/records").session(u.session()).param(pair[0],pair[1]))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        }
    }
    @Test void invalidWritesCannotChangeRows() throws Exception {
        var u = user();
        long before = jdbc.queryForObject("SELECT COUNT(*) FROM demo_record", Long.class);
        for (String body : new String[]{"{\"title\":\" \"}","{\"title\":\""+"a".repeat(101)+"\"}","{\"title\":\"ok\",\"status\":\"BAD\"}","{\"title\":\"ok\",\"ownerId\":3}","{\"title\":\"ok\",\"description\":\""+"a".repeat(2001)+"\"}"}) {
            mvc.perform(post("/api/records").session(u.session()).header(u.name(),u.value()).contentType("application/json").content(body))
                .andExpect(status().isBadRequest());
        }
        mvc.perform(put("/api/records/6").session(u.session()).header(u.name(),u.value()).contentType("application/json").content("{\"title\":\"ok\"}"))
            .andExpect(status().isBadRequest());
        assertEquals(before, jdbc.queryForObject("SELECT COUNT(*) FROM demo_record", Long.class));
    }
    @Test void missingAndInvalidIdsAndPathsReturnJsonErrors() throws Exception {
        var u = user();
        mvc.perform(get("/api/records/999999").session(u.session())).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value(404));
        mvc.perform(get("/api/records/0").session(u.session())).andExpect(status().isBadRequest());
        mvc.perform(get("/api/records/not-a-number").session(u.session())).andExpect(status().isBadRequest());
        mvc.perform(get("/api/unknown").session(u.session())).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value(404));
    }
    @Test void realDatabaseDisabledAndWrongPasswordCannotLogin() throws Exception {
        for (var pair : new String[][]{{"user","wrong"},{"disabled","Disabled123!"}}) {
            var csrf = token(null);
            mvc.perform(post("/api/auth/login").session(csrf.session()).header(csrf.name(),csrf.value())
                .param("username",pair[0]).param("password",pair[1]))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.data").isEmpty());
        }
    }
}
