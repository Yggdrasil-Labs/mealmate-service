package io.yggdrasil.labs.mealmate.start;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CreateFamilyApiIntegrationTest {

    @Resource private MockMvc mockMvc;
    @Resource private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateFamilyViaHttpApi() throws Exception {
        Integer beforeCount =
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM family_profile", Integer.class);

        mockMvc.perform(
                        post("/api/families")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                new CreateFamilyPayload("API Created Family"))))
                .andExpect(status().isOk());

        Integer afterCount =
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM family_profile", Integer.class);
        assertEquals(beforeCount + 1, afterCount);
    }

    private record CreateFamilyPayload(String familyName) {}
}
