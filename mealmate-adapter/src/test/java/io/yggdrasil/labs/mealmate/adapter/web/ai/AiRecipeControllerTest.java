package io.yggdrasil.labs.mealmate.adapter.web.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;

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

import io.yggdrasil.labs.mealmate.app.recipe.application.AiRecipeAppService;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.AiRecipeConfirmResultCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.AiRecipeParseResultCO;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParsedData;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeParseStatus;

@ExtendWith(MockitoExtension.class)
class AiRecipeControllerTest {

    @Mock private AiRecipeAppService aiRecipeAppService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AiRecipeController controller = new AiRecipeController(aiRecipeAppService);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        mockMvc =
                MockMvcBuilders.standaloneSetup(controller).setMessageConverters(converter).build();
    }

    @Test
    void chat_newSession_returns200WithSessionIdAndRefiningStatus() throws Exception {
        // Given
        AiRecipeParseResultCO resultCO =
                AiRecipeParseResultCO.builder()
                        .sessionId("new-session-id")
                        .reply("已解析番茄炒蛋基本信息")
                        .parsed(
                                RecipeParsedData.builder()
                                        .name("番茄炒蛋")
                                        .recipeType("HOME_COOKING")
                                        .build())
                        .status(RecipeParseStatus.REFINING)
                        .suggestions(List.of("补充烹饪步骤"))
                        .build();
        when(aiRecipeAppService.chat(any())).thenReturn(resultCO);

        // When
        MvcResult mvcResult =
                mockMvc.perform(
                                post("/api/ai/recipes/chat")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
                                                {"sessionId": null, "message": "番茄炒蛋，2个番茄3个鸡蛋"}
                                                """))
                        .andExpect(status().isOk())
                        .andReturn();

        // Then
        var json =
                objectMapper.readTree(
                        mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(true, json.path("success").asBoolean());
        assertEquals("new-session-id", json.path("data").path("sessionId").asText());
        assertEquals("REFINING", json.path("data").path("status").asText());
        assertEquals("番茄炒蛋", json.path("data").path("parsed").path("name").asText());
        assertNotNull(json.path("data").path("suggestions"));
        verify(aiRecipeAppService).chat(any());
    }

    @Test
    void chat_existingSession_returns200WithMergedParsed() throws Exception {
        // Given
        AiRecipeParseResultCO resultCO =
                AiRecipeParseResultCO.builder()
                        .sessionId("existing-session")
                        .reply("已补充步骤")
                        .parsed(
                                RecipeParsedData.builder()
                                        .name("番茄炒蛋")
                                        .ingredients(
                                                List.of(
                                                        new RecipeParsedData.IngredientItem(
                                                                "番茄", "VEGETABLE", 2.0, "个", true)))
                                        .steps(List.of(new RecipeParsedData.StepItem(1, "炒鸡蛋")))
                                        .build())
                        .status(RecipeParseStatus.READY_TO_CONFIRM)
                        .suggestions(List.of())
                        .build();
        when(aiRecipeAppService.chat(any())).thenReturn(resultCO);

        // When
        MvcResult mvcResult =
                mockMvc.perform(
                                post("/api/ai/recipes/chat")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
{"sessionId": "existing-session", "message": "先炒鸡蛋"}
"""))
                        .andExpect(status().isOk())
                        .andReturn();

        // Then
        var json =
                objectMapper.readTree(
                        mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals("READY_TO_CONFIRM", json.path("data").path("status").asText());
        assertNotNull(json.path("data").path("parsed").path("steps"));
    }

    @Test
    void confirm_returns200WithRecipeId() throws Exception {
        // Given
        when(aiRecipeAppService.confirm(any())).thenReturn(new AiRecipeConfirmResultCO(123L));

        // When
        MvcResult mvcResult =
                mockMvc.perform(
                                post("/api/ai/recipes/confirm")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                """
{"sessionId": "session-1", "recipe": {"name": "番茄炒蛋"}}
"""))
                        .andExpect(status().isOk())
                        .andReturn();

        // Then
        var json =
                objectMapper.readTree(
                        mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(true, json.path("success").asBoolean());
        assertEquals(123, json.path("data").path("recipeId").asInt());
        verify(aiRecipeAppService).confirm(any());
    }

    @Test
    void chat_missingMessage_returns400() throws Exception {
        // Given: message 为空
        mockMvc.perform(
                        post("/api/ai/recipes/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"sessionId": null, "message": ""}
                                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirm_missingSessionId_returns400() throws Exception {
        // Given: sessionId 为空
        mockMvc.perform(
                        post("/api/ai/recipes/confirm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"sessionId": "", "recipe": {"name": "test"}}
                                        """))
                .andExpect(status().isBadRequest());
    }
}
