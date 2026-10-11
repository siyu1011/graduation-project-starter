package com.example.starter.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class JsonResponseWriter {
    private final ObjectMapper mapper;
    public JsonResponseWriter(ObjectMapper mapper) { this.mapper = mapper; }
    public void write(HttpServletResponse response, int status, Object value) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), value);
    }
}
