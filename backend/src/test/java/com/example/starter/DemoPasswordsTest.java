package com.example.starter;

import java.nio.file.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

class DemoPasswordsTest {
    @Test void committedDemoHashesMatchPublicPasswords() throws Exception {
        String sql = Files.readString(Path.of("../database/demo-data.sql"));
        var encoder = new BCryptPasswordEncoder();
        String[][] accounts = {{"admin","Admin123!"},{"user","User123!"},{"user2","User123!"},{"disabled","Disabled123!"}};
        for (var account : accounts) {
            var matcher = Pattern.compile("'" + account[0] + "','([^']+)'").matcher(sql);
            assertTrue(matcher.find(), account[0]);
            assertTrue(encoder.matches(account[1], matcher.group(1)), account[0]);
            assertFalse(sql.contains("'" + account[1] + "'"), "SQL不能含明文密码");
        }
    }
}
