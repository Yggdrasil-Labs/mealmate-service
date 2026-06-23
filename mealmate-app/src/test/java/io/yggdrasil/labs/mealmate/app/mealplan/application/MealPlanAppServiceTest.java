package io.yggdrasil.labs.mealmate.app.mealplan.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AddItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.ConfirmPlanCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.DeleteItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.GenerateWeeklyPlanCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.ReplaceItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.AdjustMealItemCmdExe;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.GetItemHistoryQryExe;
import io.yggdrasil.labs.mealmate.app.mealplan.executor.GetRecommendRecipeQryExe;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.MemberPreferenceRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.PrepPlanRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.DuplicateCheckDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.IngredientFilterDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.PrepPlanDeriveDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.WeekPlanGenerateDomainService;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;

@ExtendWith(MockitoExtension.class)
class MealPlanAppServiceTest {

    @Mock private WeeklyMealPlanRepository weeklyMealPlanRepository;
    @Mock private PrepPlanRepository prepPlanRepository;
    @Mock private RecipeRepository recipeRepository;
    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private MemberPreferenceRepository memberPreferenceRepository;
    @Mock private WeekPlanGenerateDomainService weekPlanGenerateDomainService;
    @Mock private IngredientFilterDomainService ingredientFilterDomainService;
    @Mock private DuplicateCheckDomainService duplicateCheckDomainService;
    @Mock private PrepPlanDeriveDomainService prepPlanDeriveDomainService;
    @Mock private AdjustMealItemCmdExe adjustMealItemCmdExe;
    @Mock private GetRecommendRecipeQryExe getRecommendRecipeQryExe;
    @Mock private GetItemHistoryQryExe getItemHistoryQryExe;

    private MealPlanAppService service;

    @BeforeEach
    void setUp() {
        service =
                new MealPlanAppService(
                        weeklyMealPlanRepository,
                        prepPlanRepository,
                        recipeRepository,
                        familyMemberRepository,
                        memberPreferenceRepository,
                        weekPlanGenerateDomainService,
                        ingredientFilterDomainService,
                        duplicateCheckDomainService,
                        prepPlanDeriveDomainService,
                        adjustMealItemCmdExe,
                        getRecommendRecipeQryExe,
                        getItemHistoryQryExe);
    }

    // ─── generateWeeklyPlan ───

    @Test
    void generateWeeklyPlan_shouldReject_whenWeekStartDateIsNotMonday() {
        // 2026-06-24 is Wednesday
        GenerateWeeklyPlanCmd cmd = new GenerateWeeklyPlanCmd();
        cmd.setFamilyId(1L);
        cmd.setWeekStartDate(LocalDate.of(2026, 6, 24));

        BizException ex = assertThrows(BizException.class, () -> service.generateWeeklyPlan(cmd));
        assertEquals("PLAN_WEEK_START_DATE_INVALID", ex.getErrCode());
    }

    @Test
    void generateWeeklyPlan_shouldThrow_whenExistingPlanIsConfirmed() {
        LocalDate monday = LocalDate.of(2026, 6, 22);
        GenerateWeeklyPlanCmd cmd = new GenerateWeeklyPlanCmd();
        cmd.setFamilyId(1L);
        cmd.setWeekStartDate(monday);

        WeeklyMealPlan confirmed =
                WeeklyMealPlan.builder().id(100L).familyId(1L).status(PlanStatus.CONFIRMED).build();
        when(weeklyMealPlanRepository.findByFamilyIdAndWeekStartDateForUpdate(1L, monday))
                .thenReturn(Optional.of(confirmed));

        BizException ex = assertThrows(BizException.class, () -> service.generateWeeklyPlan(cmd));
        assertEquals("MEAL_PLAN_ALREADY_CONFIRMED", ex.getErrCode());
    }

    // ─── confirmPlan ───

    @Test
    void confirmPlan_shouldThrow_whenPlanAlreadyConfirmed() {
        WeeklyMealPlan plan =
                WeeklyMealPlan.builder().id(1L).familyId(1L).status(PlanStatus.CONFIRMED).build();
        when(weeklyMealPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(weeklyMealPlanRepository.findByIdWithItems(1L)).thenReturn(Optional.of(plan));

        BizException ex =
                assertThrows(
                        BizException.class,
                        () -> service.confirmPlan(ConfirmPlanCmd.builder().planId(1L).build()));
        assertEquals("MEAL_PLAN_ALREADY_CONFIRMED", ex.getErrCode());
    }

    // ─── deleteItem ───

    @Test
    void deleteItem_shouldThrow_whenLastItemInSlot() {
        LocalDate date = LocalDate.of(2026, 6, 22);
        MealPlanItem item =
                MealPlanItem.builder()
                        .id(10L)
                        .planId(1L)
                        .mealDate(date)
                        .mealType(MealType.LUNCH)
                        .recipeId(100L)
                        .build();

        WeeklyMealPlan plan =
                WeeklyMealPlan.builder()
                        .id(1L)
                        .familyId(1L)
                        .status(PlanStatus.DRAFT)
                        .items(List.of(item))
                        .build();

        when(weeklyMealPlanRepository.findItemById(10L)).thenReturn(Optional.of(item));
        when(weeklyMealPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(weeklyMealPlanRepository.findByIdWithItems(1L)).thenReturn(Optional.of(plan));

        BizException ex =
                assertThrows(
                        BizException.class,
                        () ->
                                service.deleteItem(
                                        DeleteItemCmd.builder().planId(1L).itemId(10L).build()));
        assertEquals("MEAL_PLAN_ITEM_LAST_ONE", ex.getErrCode());
    }

    // ─── replaceItem state guard ───

    @Test
    void replaceItem_shouldThrow_whenPlanIsNotDraft() {
        WeeklyMealPlan plan =
                WeeklyMealPlan.builder().id(1L).familyId(1L).status(PlanStatus.CONFIRMED).build();
        when(weeklyMealPlanRepository.findById(1L)).thenReturn(Optional.of(plan));

        ReplaceItemCmd cmd = new ReplaceItemCmd();
        cmd.setPlanId(1L);
        cmd.setItemId(10L);
        cmd.setRecipeId(200L);

        BizException ex = assertThrows(BizException.class, () -> service.replaceItem(cmd));
        assertEquals("MEAL_PLAN_ALREADY_CONFIRMED", ex.getErrCode());
    }

    // ─── addItem state guard ───

    @Test
    void addItem_shouldThrow_whenPlanIsNotDraft() {
        WeeklyMealPlan plan =
                WeeklyMealPlan.builder().id(1L).familyId(1L).status(PlanStatus.CONFIRMED).build();
        when(weeklyMealPlanRepository.findById(1L)).thenReturn(Optional.of(plan));

        AddItemCmd cmd = new AddItemCmd();
        cmd.setPlanId(1L);
        cmd.setRecipeId(100L);
        cmd.setMealDate(LocalDate.of(2026, 6, 22));
        cmd.setMealType("LUNCH");

        BizException ex = assertThrows(BizException.class, () -> service.addItem(cmd));
        assertEquals("MEAL_PLAN_ALREADY_CONFIRMED", ex.getErrCode());
    }

    // ─── happy paths ───

    @Test
    void deleteItem_shouldSucceed_whenSlotHasMultipleItems() {
        LocalDate date = LocalDate.of(2026, 6, 22);
        MealPlanItem item1 =
                MealPlanItem.builder()
                        .id(10L)
                        .planId(1L)
                        .mealDate(date)
                        .mealType(MealType.LUNCH)
                        .recipeId(100L)
                        .build();
        MealPlanItem item2 =
                MealPlanItem.builder()
                        .id(11L)
                        .planId(1L)
                        .mealDate(date)
                        .mealType(MealType.LUNCH)
                        .recipeId(200L)
                        .build();

        WeeklyMealPlan plan =
                WeeklyMealPlan.builder()
                        .id(1L)
                        .familyId(1L)
                        .status(PlanStatus.DRAFT)
                        .items(List.of(item1, item2))
                        .build();

        when(weeklyMealPlanRepository.findItemById(10L)).thenReturn(Optional.of(item1));
        when(weeklyMealPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(weeklyMealPlanRepository.findByIdWithItems(1L)).thenReturn(Optional.of(plan));

        // Should not throw
        service.deleteItem(DeleteItemCmd.builder().planId(1L).itemId(10L).build());

        verify(weeklyMealPlanRepository).deleteItem(10L);
    }

    @Test
    void confirmPlan_shouldDeriveAndUpdateStatus() {
        WeeklyMealPlan plan =
                WeeklyMealPlan.builder()
                        .id(1L)
                        .familyId(1L)
                        .status(PlanStatus.DRAFT)
                        .items(List.of(MealPlanItem.builder().recipeId(100L).build()))
                        .build();

        when(weeklyMealPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(weeklyMealPlanRepository.findByIdWithItems(1L)).thenReturn(Optional.of(plan));
        when(recipeRepository.findByIds(any())).thenReturn(Collections.emptyList());
        when(prepPlanDeriveDomainService.derivePrepPlan(anyLong(), any(), any()))
                .thenReturn(
                        io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlan.builder()
                                .id(10L)
                                .planId(1L)
                                .items(Collections.emptyList())
                                .build());
        when(prepPlanRepository.save(any()))
                .thenReturn(
                        io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlan.builder()
                                .id(10L)
                                .planId(1L)
                                .items(Collections.emptyList())
                                .build());
        when(prepPlanDeriveDomainService.deriveShoppingList(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        service.confirmPlan(ConfirmPlanCmd.builder().planId(1L).build());

        // 验证状态通过聚合根转换后持久化
        verify(weeklyMealPlanRepository).updateStatus(1L, PlanStatus.CONFIRMED);
        verify(prepPlanRepository).save(any());
    }
}
