package io.yggdrasil.labs.mealmate.adapter.web.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.yggdrasil.labs.mealmate.adapter.web.MealMateExceptionHandler;
import io.yggdrasil.labs.mealmate.app.mealplan.application.AiMealPlanAppService;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.AiMealPlanResultCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.DayMealCO;

/**
 * AiMealPlanController 单元测试。
 *
 * <p>使用 standalone MockMvc + Mockito 验证 HTTP 协议层行为，不启动 Spring 上下文。
 */
@ExtendWith(MockitoExtension.class)
class AiMealPlanControllerTest {

    @Mock private AiMealPlanAppService aiMealPlanAppService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        AiMealPlanController controller = new AiMealPlanController(aiMealPlanAppService);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        mockMvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .setMessageConverters(converter)
                        .setControllerAdvice(new MealMateExceptionHandler())
                        .build();
    }

    @Test
    void generate_success_returns200() throws Exception {
        // Given: mock service 返回正常结果
        AiMealPlanResultCO resultCO =
                AiMealPlanResultCO.builder()
                        .planId(1L)
                        .weekStartDate("2026-07-06")
                        .weekEndDate("2026-07-12")
                        .status("DRAFT")
                        .planSource("AI")
                        .dayMeals(
                                Map.of(
                                        "2026-07-06",
                                        DayMealCO.builder()
                                                .date("2026-07-06")
                                                .breakfast(List.of())
                                                .lunch(List.of())
                                                .dinner(List.of())
                                                .build()))
                        .reasoning(Map.of("2026-07-06", "周一清淡开胃"))
                        .fallback(false)
                        .build();
        when(aiMealPlanAppService.generate(any())).thenReturn(resultCO);

        // When
        MvcResult mvcResult =
                mockMvc.perform(
                                post("/api/ai/meal-plans/generate")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
{"familyId": 1, "weekStartDate": "2026-07-06", "userHint": "清淡一些"}
"""))
                        .andExpect(status().isOk())
                        .andReturn();

        // Then: JSON 结构正确
        var json =
                objectMapper.readTree(
                        mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(true, json.path("success").asBoolean());
        assertEquals(1, json.path("data").path("planId").asInt());
        assertEquals("2026-07-06", json.path("data").path("weekStartDate").asText());
        assertEquals("2026-07-12", json.path("data").path("weekEndDate").asText());
        assertEquals("AI", json.path("data").path("planSource").asText());
        assertEquals(false, json.path("data").path("fallback").asBoolean());
        verify(aiMealPlanAppService).generate(any());
    }

    @Test
    void generate_invalidDate_returns400() throws Exception {
        // Given: weekStartDate 为 null（缺失字段）
        mockMvc.perform(
                        post("/api/ai/meal-plans/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"familyId": 1, "weekStartDate": null}
                                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generate_missingFamilyId_returns400() throws Exception {
        // Given: familyId 为 null（缺失字段）
        mockMvc.perform(
                        post("/api/ai/meal-plans/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"familyId": null, "weekStartDate": "2026-07-06"}
                                        """))
                .andExpect(status().isBadRequest());
    }
}
