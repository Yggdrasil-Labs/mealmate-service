package io.yggdrasil.labs.mealmate.app.mealplan.executor;

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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AddItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.ConfirmPlanCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.DeleteItemCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.GenerateWeeklyPlanCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.ReplaceItemCmd;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.MemberPreferenceRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlan;
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

/** MealPlan Executor 单元测试，覆盖核心状态守卫和编排逻辑。 */
@ExtendWith(MockitoExtension.class)
class MealPlanExecutorTest {

    @Mock private WeeklyMealPlanRepository weeklyMealPlanRepository;
    @Mock private PrepPlanRepository prepPlanRepository;
    @Mock private RecipeRepository recipeRepository;
    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private MemberPreferenceRepository memberPreferenceRepository;
    @Mock private WeekPlanGenerateDomainService weekPlanGenerateDomainService;
    @Mock private IngredientFilterDomainService ingredientFilterDomainService;
    @Mock private DuplicateCheckDomainService duplicateCheckDomainService;
    @Mock private PrepPlanDeriveDomainService prepPlanDeriveDomainService;

    // ─── GenerateWeeklyPlanCmdExe ───

    @Test
    void generate_shouldReject_whenWeekStartDateIsNotMonday() {
        GenerateWeeklyPlanCmdExe exe =
                new GenerateWeeklyPlanCmdExe(
                        weeklyMealPlanRepository,
                        recipeRepository,
                        familyMemberRepository,
                        memberPreferenceRepository,
                        weekPlanGenerateDomainService,
                        ingredientFilterDomainService,
                        duplicateCheckDomainService);

        GenerateWeeklyPlanCmd cmd = new GenerateWeeklyPlanCmd();
        cmd.setFamilyId(1L);
        cmd.setWeekStartDate(LocalDate.of(2026, 6, 24)); // Wednesday

        BizException ex = assertThrows(BizException.class, () -> exe.execute(cmd));
        assertEquals("PLAN_WEEK_START_DATE_INVALID", ex.getErrCode());
    }

    @Test
    void generate_shouldThrow_whenExistingPlanIsConfirmed() {
        GenerateWeeklyPlanCmdExe exe =
                new GenerateWeeklyPlanCmdExe(
                        weeklyMealPlanRepository,
                        recipeRepository,
                        familyMemberRepository,
                        memberPreferenceRepository,
                        weekPlanGenerateDomainService,
                        ingredientFilterDomainService,
                        duplicateCheckDomainService);

        LocalDate monday = LocalDate.of(2026, 6, 22);
        GenerateWeeklyPlanCmd cmd = new GenerateWeeklyPlanCmd();
        cmd.setFamilyId(1L);
        cmd.setWeekStartDate(monday);

        WeeklyMealPlan confirmed =
                WeeklyMealPlan.builder().id(100L).familyId(1L).status(PlanStatus.CONFIRMED).build();
        when(weeklyMealPlanRepository.findByFamilyIdAndWeekStartDateForUpdate(1L, monday))
                .thenReturn(Optional.of(confirmed));

        BizException ex = assertThrows(BizException.class, () -> exe.execute(cmd));
        assertEquals("MEAL_PLAN_ALREADY_CONFIRMED", ex.getErrCode());
    }

    // ─── ConfirmPlanCmdExe ───

    @Test
    void confirm_shouldThrow_whenPlanAlreadyConfirmed() {
        ConfirmPlanCmdExe exe =
                new ConfirmPlanCmdExe(
                        weeklyMealPlanRepository,
                        prepPlanRepository,
                        recipeRepository,
                        prepPlanDeriveDomainService);

        WeeklyMealPlan plan =
                WeeklyMealPlan.builder()
                        .id(1L)
                        .familyId(1L)
                        .status(PlanStatus.CONFIRMED)
                        .items(Collections.emptyList())
                        .build();
        when(weeklyMealPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(weeklyMealPlanRepository.findByIdWithItems(1L)).thenReturn(Optional.of(plan));

        BizException ex =
                assertThrows(
                        BizException.class,
                        () -> exe.execute(ConfirmPlanCmd.builder().planId(1L).build()));
        assertEquals("MEAL_PLAN_ALREADY_CONFIRMED", ex.getErrCode());
    }

    @Test
    void confirm_shouldDeriveAndUpdateStatus() {
        ConfirmPlanCmdExe exe =
                new ConfirmPlanCmdExe(
                        weeklyMealPlanRepository,
                        prepPlanRepository,
                        recipeRepository,
                        prepPlanDeriveDomainService);

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
                        PrepPlan.builder()
                                .id(10L)
                                .planId(1L)
                                .items(Collections.emptyList())
                                .build());
        when(prepPlanRepository.save(any()))
                .thenReturn(
                        PrepPlan.builder()
                                .id(10L)
                                .planId(1L)
                                .items(Collections.emptyList())
                                .build());
        when(prepPlanDeriveDomainService.deriveShoppingList(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        exe.execute(ConfirmPlanCmd.builder().planId(1L).build());

        verify(weeklyMealPlanRepository).updateStatus(1L, PlanStatus.CONFIRMED);
        verify(prepPlanRepository).save(any());
    }

    // ─── DeleteItemCmdExe ───

    @Test
    void delete_shouldThrow_whenLastItemInSlot() {
        DeleteItemCmdExe exe = new DeleteItemCmdExe(weeklyMealPlanRepository);

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
                        () -> exe.execute(DeleteItemCmd.builder().planId(1L).itemId(10L).build()));
        assertEquals("MEAL_PLAN_ITEM_LAST_ONE", ex.getErrCode());
    }

    @Test
    void delete_shouldSucceed_whenSlotHasMultipleItems() {
        DeleteItemCmdExe exe = new DeleteItemCmdExe(weeklyMealPlanRepository);

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

        exe.execute(DeleteItemCmd.builder().planId(1L).itemId(10L).build());
        verify(weeklyMealPlanRepository).deleteItem(10L);
    }

    // ─── ReplaceItemCmdExe ───

    @Test
    void replace_shouldThrow_whenPlanIsNotDraft() {
        ReplaceItemCmdExe exe = new ReplaceItemCmdExe(weeklyMealPlanRepository);

        WeeklyMealPlan plan =
                WeeklyMealPlan.builder().id(1L).familyId(1L).status(PlanStatus.CONFIRMED).build();
        when(weeklyMealPlanRepository.findById(1L)).thenReturn(Optional.of(plan));

        ReplaceItemCmd cmd = new ReplaceItemCmd();
        cmd.setPlanId(1L);
        cmd.setItemId(10L);
        cmd.setRecipeId(200L);

        BizException ex = assertThrows(BizException.class, () -> exe.execute(cmd));
        assertEquals("MEAL_PLAN_ALREADY_CONFIRMED", ex.getErrCode());
    }

    // ─── AddItemCmdExe ───

    @Test
    void add_shouldThrow_whenPlanIsNotDraft() {
        AddItemCmdExe exe = new AddItemCmdExe(weeklyMealPlanRepository);

        WeeklyMealPlan plan =
                WeeklyMealPlan.builder().id(1L).familyId(1L).status(PlanStatus.CONFIRMED).build();
        when(weeklyMealPlanRepository.findById(1L)).thenReturn(Optional.of(plan));

        AddItemCmd cmd = new AddItemCmd();
        cmd.setPlanId(1L);
        cmd.setRecipeId(100L);
        cmd.setMealDate(LocalDate.of(2026, 6, 22));
        cmd.setMealType("LUNCH");

        BizException ex = assertThrows(BizException.class, () -> exe.execute(cmd));
        assertEquals("MEAL_PLAN_ALREADY_CONFIRMED", ex.getErrCode());
    }
}
