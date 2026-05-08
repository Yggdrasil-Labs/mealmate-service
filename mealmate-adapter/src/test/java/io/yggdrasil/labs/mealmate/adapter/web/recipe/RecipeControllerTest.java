package io.yggdrasil.labs.mealmate.adapter.web.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.yggdrasil.labs.mealmate.adapter.web.recipe.convertor.RecipeWebConvertor;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.CreateRecipeRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipeDetailResponse;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipeIngredientRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipePageRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipeResponse;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipeSearchRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipeStepRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeIngredientsRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeNutritionRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeStatusRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeStepsRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.IngredientType;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.RecipeStatus;
import io.yggdrasil.labs.mealmate.app.recipe.application.RecipeAppService;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.CreateRecipeCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.DeleteRecipeCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeIngredientsCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeNutritionCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeStatusCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeStepsCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeDetailCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.GetRecipeDetailQry;
import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.PageRecipeQry;
import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.SearchRecipeQry;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @Mock private RecipeAppService recipeAppService;
    @Mock private RecipeWebConvertor recipeWebConvertor;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc =
                MockMvcBuilders.standaloneSetup(
                                new RecipeController(recipeAppService, recipeWebConvertor))
                        .setValidator(validator)
                        .build();
    }

    @Test
    void shouldGetRecipePage() throws Exception {
        PageRecipeQry qry = new PageRecipeQry();
        RecipeCO recipeCO = new RecipeCO();
        RecipeResponse response = new RecipeResponse();
        response.setId(1L);

        when(recipeWebConvertor.toPageRecipeQry(any(RecipePageRequest.class))).thenReturn(qry);
        when(recipeAppService.pageRecipe(qry)).thenReturn(List.of(recipeCO));
        when(recipeAppService.countRecipe(qry)).thenReturn(42);
        when(recipeWebConvertor.toRecipeResponseList(List.of(recipeCO)))
                .thenReturn(List.of(response));

        MvcResult result =
                mockMvc.perform(get("/api/recipes")).andExpect(status().isOk()).andReturn();

        assertEquals(
                1,
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .path("data")
                        .size());
        assertEquals(
                42,
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .path("totalCount")
                        .asInt());
        verify(recipeAppService).pageRecipe(qry);
        verify(recipeAppService).countRecipe(qry);
    }

    @Test
    void shouldGetRecipePageWithFilters() throws Exception {
        PageRecipeQry qry = new PageRecipeQry();

        when(recipeWebConvertor.toPageRecipeQry(any(RecipePageRequest.class))).thenReturn(qry);
        when(recipeAppService.pageRecipe(qry)).thenReturn(List.of());
        when(recipeAppService.countRecipe(qry)).thenReturn(0);
        when(recipeWebConvertor.toRecipeResponseList(List.of())).thenReturn(List.of());

        MvcResult result =
                mockMvc.perform(
                                get("/api/recipes")
                                        .param("keyword", "Soup")
                                        .param("recipeType", "SOUP")
                                        .param("isBabyFriendly", "true")
                                        .param("pageNum", "0")
                                        .param("pageSize", "0"))
                        .andExpect(status().isOk())
                        .andReturn();

        verify(recipeAppService).pageRecipe(qry);
        verify(recipeAppService).countRecipe(qry);
        assertEquals(
                1,
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .path("pageIndex")
                        .asInt());
        assertEquals(
                10,
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .path("pageSize")
                        .asInt());
    }

    @Test
    void shouldGetRecipeDetail() throws Exception {
        GetRecipeDetailQry qry = new GetRecipeDetailQry(1L);
        RecipeDetailCO recipeDetailCO = new RecipeDetailCO();
        RecipeDetailResponse response = new RecipeDetailResponse();
        response.setId(1L);

        when(recipeWebConvertor.toGetRecipeDetailQry(1L)).thenReturn(qry);
        when(recipeAppService.getRecipeDetail(qry)).thenReturn(recipeDetailCO);
        when(recipeWebConvertor.toRecipeDetailResponse(recipeDetailCO)).thenReturn(response);

        mockMvc.perform(get("/api/recipes/1")).andExpect(status().isOk());

        verify(recipeAppService).getRecipeDetail(qry);
    }

    @Test
    void shouldSearchRecipes() throws Exception {
        SearchRecipeQry qry = new SearchRecipeQry("Pumpkin", 5);
        RecipeCO recipeCO = new RecipeCO();
        RecipeResponse response = new RecipeResponse();
        response.setName("Pumpkin Soup");

        when(recipeWebConvertor.toSearchRecipeQry(any(RecipeSearchRequest.class))).thenReturn(qry);
        when(recipeAppService.searchRecipe(qry)).thenReturn(List.of(recipeCO));
        when(recipeWebConvertor.toRecipeResponseList(List.of(recipeCO)))
                .thenReturn(List.of(response));

        MvcResult result =
                mockMvc.perform(
                                get("/api/recipes/search")
                                        .param("keyword", "Pumpkin")
                                        .param("limit", "5"))
                        .andExpect(status().isOk())
                        .andReturn();

        assertEquals(
                "Pumpkin Soup",
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .path("data")
                        .get(0)
                        .path("name")
                        .asText());
        verify(recipeAppService).searchRecipe(qry);
    }

    @Test
    void shouldCreateRecipe() throws Exception {
        CreateRecipeRequest request = validCreateRequest();
        CreateRecipeCmd cmd = new CreateRecipeCmd();
        RecipeDetailCO recipeDetailCO = new RecipeDetailCO();
        RecipeDetailResponse response = new RecipeDetailResponse();
        response.setId(1L);

        when(recipeWebConvertor.toCreateRecipeCmd(request)).thenReturn(cmd);
        when(recipeAppService.createRecipe(cmd)).thenReturn(recipeDetailCO);
        when(recipeWebConvertor.toRecipeDetailResponse(recipeDetailCO)).thenReturn(response);

        MvcResult result =
                mockMvc.perform(
                                post("/api/recipes")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andReturn();

        assertEquals(
                1L,
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .path("data")
                        .path("id")
                        .asLong());
        verify(recipeAppService).createRecipe(cmd);
    }

    @Test
    void shouldUpdateRecipeBasics() throws Exception {
        UpdateRecipeRequest request = new UpdateRecipeRequest();
        request.setName("Updated Soup");
        request.setRecipeType(
                io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.RecipeType.SOUP);
        UpdateRecipeCmd cmd = new UpdateRecipeCmd();

        when(recipeWebConvertor.toUpdateRecipeCmd(1L, request)).thenReturn(cmd);

        mockMvc.perform(
                        put("/api/recipes/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(recipeAppService).updateRecipe(cmd);
    }

    @Test
    void shouldUpdateRecipeIngredients() throws Exception {
        UpdateRecipeIngredientsRequest request = new UpdateRecipeIngredientsRequest();
        request.setIngredients(List.of(validIngredient()));
        UpdateRecipeIngredientsCmd cmd = new UpdateRecipeIngredientsCmd();

        when(recipeWebConvertor.toUpdateRecipeIngredientsCmd(1L, request)).thenReturn(cmd);

        mockMvc.perform(
                        put("/api/recipes/1/ingredients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(recipeAppService).updateRecipeIngredients(cmd);
    }

    @Test
    void shouldUpdateRecipeSteps() throws Exception {
        UpdateRecipeStepsRequest request = new UpdateRecipeStepsRequest();
        RecipeStepRequest stepRequest = new RecipeStepRequest();
        stepRequest.setStepNo(1);
        stepRequest.setContent("Cook");
        request.setSteps(List.of(stepRequest));
        UpdateRecipeStepsCmd cmd = new UpdateRecipeStepsCmd();

        when(recipeWebConvertor.toUpdateRecipeStepsCmd(1L, request)).thenReturn(cmd);

        mockMvc.perform(
                        put("/api/recipes/1/steps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(recipeAppService).updateRecipeSteps(cmd);
    }

    @Test
    void shouldUpdateRecipeNutrition() throws Exception {
        UpdateRecipeNutritionRequest request = new UpdateRecipeNutritionRequest();
        request.setNutritionFact(
                new io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.NutritionFactRequest());
        UpdateRecipeNutritionCmd cmd = new UpdateRecipeNutritionCmd();

        when(recipeWebConvertor.toUpdateRecipeNutritionCmd(1L, request)).thenReturn(cmd);

        mockMvc.perform(
                        put("/api/recipes/1/nutrition")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(recipeAppService).updateRecipeNutrition(cmd);
    }

    @Test
    void shouldUpdateRecipeStatus() throws Exception {
        UpdateRecipeStatusRequest request = new UpdateRecipeStatusRequest();
        request.setStatus(RecipeStatus.INACTIVE);
        UpdateRecipeStatusCmd cmd = new UpdateRecipeStatusCmd();

        when(recipeWebConvertor.toUpdateRecipeStatusCmd(1L, request)).thenReturn(cmd);

        mockMvc.perform(
                        put("/api/recipes/1/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(recipeAppService).updateRecipeStatus(cmd);
    }

    @Test
    void shouldDeleteRecipe() throws Exception {
        DeleteRecipeCmd cmd = new DeleteRecipeCmd(1L);
        when(recipeWebConvertor.toDeleteRecipeCmd(1L)).thenReturn(cmd);

        mockMvc.perform(delete("/api/recipes/1")).andExpect(status().isOk());

        verify(recipeAppService).deleteRecipe(cmd);
    }

    @Test
    void shouldRejectInvalidCreateRecipeRequest() throws Exception {
        CreateRecipeRequest request = new CreateRecipeRequest();
        request.setName("");
        request.setIngredients(List.of());

        mockMvc.perform(
                        post("/api/recipes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(recipeWebConvertor);
        verifyNoInteractions(recipeAppService);
    }

    @Test
    void shouldRejectCreateRecipeRequestWithoutRecipeType() throws Exception {
        CreateRecipeRequest request = validCreateRequest();
        request.setRecipeType(null);

        mockMvc.perform(
                        post("/api/recipes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(recipeWebConvertor);
        verifyNoInteractions(recipeAppService);
    }

    private CreateRecipeRequest validCreateRequest() {
        CreateRecipeRequest request = new CreateRecipeRequest();
        request.setName("Pumpkin Soup");
        request.setRecipeType(
                io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.RecipeType.SOUP);
        request.setIngredients(List.of(validIngredient()));
        return request;
    }

    private RecipeIngredientRequest validIngredient() {
        RecipeIngredientRequest ingredient = new RecipeIngredientRequest();
        ingredient.setIngredientName("Pumpkin");
        ingredient.setIngredientType(IngredientType.VEGETABLE);
        return ingredient;
    }
}
