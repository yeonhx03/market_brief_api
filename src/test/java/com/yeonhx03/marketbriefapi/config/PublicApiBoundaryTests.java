package com.yeonhx03.marketbriefapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "market-brief.write-api-key=test-secret",
        "market-brief.cors.allowed-origins=http://localhost:5173"
})
@AutoConfigureMockMvc
@Transactional
class PublicApiBoundaryTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void allowsPublicReadWithoutApiKey() throws Exception {
        mockMvc.perform(get("/api/articles/latest"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsWriteWithoutApiKey() throws Exception {
        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validArticleJson()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void rejectsWriteWithWrongApiKey() throws Exception {
        mockMvc.perform(post("/api/articles")
                        .header(WriteApiKeyInterceptor.HEADER_NAME, "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validArticleJson()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Valid write API key required"));
    }

    @Test
    void allowsWriteWithCorrectApiKey() throws Exception {
        mockMvc.perform(post("/api/articles")
                        .header(WriteApiKeyInterceptor.HEADER_NAME, "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validArticleJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void allowsCorsPreflightFromConfiguredWebOrigin() throws Exception {
        mockMvc.perform(options("/api/articles")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                "POST"
                        )
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                WriteApiKeyInterceptor.HEADER_NAME
                        ))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"
                ))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        "GET,POST"
                ));
    }

    @Test
    void rejectsCorsPreflightFromUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/articles")
                        .header(HttpHeaders.ORIGIN, "https://malicious.example")
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                "POST"
                        ))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
                ));
    }

    @Test
    void exposesHealthWithoutApiKey() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    private String validArticleJson() {
        return """
                {
                  "source": "Reuters",
                  "title": "Market closes higher",
                  "url": "https://example.com/articles/protected-write",
                  "collectedAt": "2026-08-30T09:00:00Z"
                }
                """;
    }
}
