package com.student.system;

import com.student.system.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class JwtAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testGenerateAndParseToken() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        assertNotNull(token);
        Claims claims = jwtUtil.parseToken(token);
        assertEquals("1", claims.getSubject());
        assertEquals("admin", claims.get("role", String.class));
        assertEquals("admin", claims.get("name", String.class));
    }

    @Test
    void testTokenNotExpired() {
        String token = jwtUtil.generateToken(1L, "student", "张三");
        assertFalse(jwtUtil.isExpired(token));
    }

    @Test
    void testPasswordEncoder() {
        String storedPassword = jdbcTemplate.queryForObject(
            "SELECT password FROM teacher WHERE name = 'admin' LIMIT 1", String.class);
        assertTrue(passwordEncoder.matches("admin123", storedPassword),
            "admin123 should match stored BCrypt hash");
    }

    @Test
    void testLoginWithValidAdminCredentials() throws Exception {
        Long adminId = jdbcTemplate.queryForObject(
            "SELECT id FROM teacher WHERE name = 'admin' LIMIT 1", Long.class);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + adminId + "\",\"password\":\"admin123\",\"role\":\"admin\"}"))
            .andReturn();
        System.out.println("Login response status: " + result.getResponse().getStatus());
        System.out.println("Login response body: " + result.getResponse().getContentAsString());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + adminId + "\",\"password\":\"admin123\",\"role\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.role").value("admin"));
    }

    @Test
    void testLoginWithWrongPassword() throws Exception {
        Long adminId = jdbcTemplate.queryForObject(
            "SELECT id FROM teacher WHERE name = 'admin' LIMIT 1", Long.class);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + adminId + "\",\"password\":\"wrong\",\"role\":\"admin\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void testLoginWithEmptyFields() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"\",\"password\":\"\",\"role\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/any-protected"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedEndpointWithValidToken() throws Exception {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        mockMvc.perform(get("/api/any-protected")
                .header("Authorization", "Bearer " + token))
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                assertNotEquals(401, status, "Should not return 401 with valid token");
            });
    }
}
