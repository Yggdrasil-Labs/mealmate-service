package io.yggdrasil.labs.mealmate.adapter.web.mealplan;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.yggdrasil.labs.mealmate.app.mealplan.application.MealPlanAppService;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AddItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AdjustMealItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.ConfirmPlanCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.DeleteItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.GenerateWeeklyPlanCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.ManualAddItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.ReplaceItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.UpdatePrepItemStatusCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.UpdateShoppingItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.ConfirmPlanCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.MealPlanItemCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.MealPlanItemHistoryCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.PrepPlanCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.RecipeBriefCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.ShoppingItemCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.WeeklyMealPlanCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetCurrentWeekPlanQry;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetItemHistoryQry;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetMealPlanDetailQry;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetPrepPlanQry;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetRecommendRecipeQry;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.qry.GetShoppingListQry;
import lombok.RequiredArgsConstructor;

/** 周餐计划 HTTP 适配层。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meal-plans")
@Tag(name = "MealPlan", description = "Weekly meal plan management APIs.")
public class MealPlanController {

    private final MealPlanAppService mealPlanAppService;

    @PostMapping("/generate")
    @Operation(summary = "Generate weekly plan", description = "Generates a new weekly meal plan.")
    public SingleResponse<WeeklyMealPlanCO> generateWeeklyPlan(
            @Valid @RequestBody GenerateWeeklyPlanCmd cmd) {
        SingleResponse<WeeklyMealPlanCO> response = SingleResponse.<WeeklyMealPlanCO>buildSuccess();
        response.setData(mealPlanAppService.generateWeeklyPlan(cmd));
        return response;
    }

    @GetMapping("/current")
    @Operation(
            summary = "Get current week plan",
            description = "Returns the meal plan for the current week or specified week.")
    public SingleResponse<WeeklyMealPlanCO> getCurrentWeekPlan(
            @Parameter(description = "Family identifier.") @RequestParam(required = false)
                    Long familyId,
            @Parameter(description = "Week start date (Monday, yyyy-MM-dd).")
                    @RequestParam(required = false)
                    String weekStartDate) {
        SingleResponse<WeeklyMealPlanCO> response = SingleResponse.<WeeklyMealPlanCO>buildSuccess();
        java.time.LocalDate parsedDate =
                weekStartDate != null ? java.time.LocalDate.parse(weekStartDate) : null;
        response.setData(
                mealPlanAppService.getCurrentWeekPlan(
                        GetCurrentWeekPlanQry.builder()
                                .familyId(familyId)
                                .weekStartDate(parsedDate)
                                .build()));
        return response;
    }

    @GetMapping("/{planId}")
    @Operation(summary = "Get plan detail", description = "Returns detailed meal plan information.")
    public SingleResponse<WeeklyMealPlanCO> getPlanDetail(
            @Parameter(description = "Plan identifier.") @PathVariable("planId") Long planId) {
        SingleResponse<WeeklyMealPlanCO> response = SingleResponse.<WeeklyMealPlanCO>buildSuccess();
        response.setData(
                mealPlanAppService.getPlanDetail(
                        GetMealPlanDetailQry.builder().planId(planId).build()));
        return response;
    }

    @PutMapping("/{planId}/items/{itemId}/replace")
    @Operation(summary = "Replace item recipe", description = "Replaces the recipe of a plan item.")
    public Response replaceItem(
            @Parameter(description = "Plan identifier.") @PathVariable("planId") Long planId,
            @Parameter(description = "Item identifier.") @PathVariable("itemId") Long itemId,
            @Valid @RequestBody ReplaceItemCmd cmd) {
        cmd.setPlanId(planId);
        cmd.setItemId(itemId);
        mealPlanAppService.replaceItem(cmd);
        return Response.buildSuccess();
    }

    @PostMapping("/{planId}/items")
    @Operation(summary = "Add item", description = "Adds a new item to the meal plan.")
    public Response addItem(
            @Parameter(description = "Plan identifier.") @PathVariable("planId") Long planId,
            @Valid @RequestBody AddItemCmd cmd) {
        cmd.setPlanId(planId);
        mealPlanAppService.addItem(cmd);
        return Response.buildSuccess();
    }

    @DeleteMapping("/{planId}/items/{itemId}")
    @Operation(summary = "Delete item", description = "Removes an item from the meal plan.")
    public Response deleteItem(
            @Parameter(description = "Plan identifier.") @PathVariable("planId") Long planId,
            @Parameter(description = "Item identifier.") @PathVariable("itemId") Long itemId) {
        mealPlanAppService.deleteItem(
                DeleteItemCmd.builder().planId(planId).itemId(itemId).build());
        return Response.buildSuccess();
    }

    @PostMapping("/{planId}/items/manual")
    @Operation(
            summary = "Manual add item",
            description = "Adds an item by recipe name lookup or plain text.")
    public Response manualAddItem(
            @Parameter(description = "Plan identifier.") @PathVariable("planId") Long planId,
            @Valid @RequestBody ManualAddItemCmd cmd) {
        cmd.setPlanId(planId);
        mealPlanAppService.manualAddItem(cmd);
        return Response.buildSuccess();
    }

    @PostMapping("/{planId}/confirm")
    @Operation(
            summary = "Confirm plan",
            description = "Confirms the plan and derives prep plan and shopping list.")
    public SingleResponse<ConfirmPlanCO> confirmPlan(
            @Parameter(description = "Plan identifier.") @PathVariable("planId") Long planId) {
        SingleResponse<ConfirmPlanCO> response = SingleResponse.<ConfirmPlanCO>buildSuccess();
        response.setData(
                mealPlanAppService.confirmPlan(ConfirmPlanCmd.builder().planId(planId).build()));
        return response;
    }

    @GetMapping("/{planId}/prep-plan")
    @Operation(summary = "Get prep plan", description = "Returns the prep plan for a meal plan.")
    public SingleResponse<PrepPlanCO> getPrepPlan(
            @Parameter(description = "Plan identifier.") @PathVariable("planId") Long planId) {
        SingleResponse<PrepPlanCO> response = SingleResponse.<PrepPlanCO>buildSuccess();
        response.setData(
                mealPlanAppService.getPrepPlan(GetPrepPlanQry.builder().planId(planId).build()));
        return response;
    }

    @PutMapping("/{planId}/prep-plan/items/{itemId}/status")
    @Operation(
            summary = "Update prep item status",
            description = "Updates the status of a prep plan item.")
    public Response updatePrepItemStatus(
            @Parameter(description = "Plan identifier.") @PathVariable("planId") Long planId,
            @Parameter(description = "Item identifier.") @PathVariable("itemId") Long itemId,
            @Valid @RequestBody UpdatePrepItemStatusCmd cmd) {
        cmd.setPlanId(planId);
        cmd.setItemId(itemId);
        mealPlanAppService.updatePrepItemStatus(cmd);
        return Response.buildSuccess();
    }

    @GetMapping("/{planId}/shopping-list")
    @Operation(
            summary = "Get shopping list",
            description = "Returns the shopping list for a meal plan.")
    public MultiResponse<ShoppingItemCO> getShoppingList(
            @Parameter(description = "Plan identifier.") @PathVariable("planId") Long planId) {
        MultiResponse<ShoppingItemCO> response = MultiResponse.<ShoppingItemCO>buildSuccess();
        response.setData(
                mealPlanAppService.getShoppingList(
                        GetShoppingListQry.builder().planId(planId).build()));
        return response;
    }

    @PutMapping("/{planId}/shopping-list/items/{itemId}")
    @Operation(
            summary = "Update shopping item",
            description = "Updates purchase status of a shopping item.")
    public Response updateShoppingItem(
            @Parameter(description = "Plan identifier.") @PathVariable("planId") Long planId,
            @Parameter(description = "Item identifier.") @PathVariable("itemId") Long itemId,
            @Valid @RequestBody UpdateShoppingItemCmd cmd) {
        cmd.setPlanId(planId);
        cmd.setItemId(itemId);
        mealPlanAppService.updateShoppingItem(cmd);
        return Response.buildSuccess();
    }

    @PutMapping("/{planId}/items/{itemId}")
    @Operation(
            summary = "Adjust meal item",
            description = "Replaces the recipe of a plan item with history tracking.")
    public SingleResponse<MealPlanItemCO> adjustMealItem(
            @Parameter(description = "Plan identifier.") @PathVariable("planId") Long planId,
            @Parameter(description = "Item identifier.") @PathVariable("itemId") Long itemId,
            @Valid @RequestBody AdjustMealItemCmd cmd) {
        cmd.setPlanId(planId);
        cmd.setItemId(itemId);
        SingleResponse<MealPlanItemCO> response = SingleResponse.<MealPlanItemCO>buildSuccess();
        response.setData(mealPlanAppService.adjustMealItem(cmd));
        return response;
    }

    @GetMapping("/{planId}/items/{itemId}/recommend")
    @Operation(
            summary = "Get recommend recipes",
            description = "Returns recommended replacement recipes for a plan item.")
    public MultiResponse<RecipeBriefCO> getRecommendRecipes(
            @Parameter(description = "Plan identifier.") @PathVariable("planId") Long planId,
            @Parameter(description = "Item identifier.") @PathVariable("itemId") Long itemId) {
        MultiResponse<RecipeBriefCO> response = MultiResponse.<RecipeBriefCO>buildSuccess();
        response.setData(
                mealPlanAppService.getRecommendRecipes(
                        GetRecommendRecipeQry.builder().planId(planId).itemId(itemId).build()));
        return response;
    }

    @GetMapping("/{planId}/items/{itemId}/history")
    @Operation(
            summary = "Get item adjust history",
            description = "Returns adjustment history for a plan item.")
    public MultiResponse<MealPlanItemHistoryCO> getItemHistory(
            @Parameter(description = "Plan identifier.") @PathVariable("planId") Long planId,
            @Parameter(description = "Item identifier.") @PathVariable("itemId") Long itemId) {
        MultiResponse<MealPlanItemHistoryCO> response =
                MultiResponse.<MealPlanItemHistoryCO>buildSuccess();
        response.setData(
                mealPlanAppService.getItemHistory(
                        GetItemHistoryQry.builder().planId(planId).itemId(itemId).build()));
        return response;
    }
}
