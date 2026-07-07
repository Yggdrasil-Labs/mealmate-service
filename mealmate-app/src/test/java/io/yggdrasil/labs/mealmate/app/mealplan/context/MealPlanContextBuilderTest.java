package io.yggdrasil.labs.mealmate.app.mealplan.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberTargetType;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.MemberPreferenceRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.IngredientFilterDomainService;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeQueryCriteria;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;

@ExtendWith(MockitoExtension.class)
class MealPlanContextBuilderTest {

    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private MemberPreferenceRepository memberPreferenceRepository;
    @Mock private RecipeRepository recipeRepository;
    @Mock private IngredientFilterDomainService ingredientFilterDomainService;

    @InjectMocks private MealPlanContextBuilder contextBuilder;

    /** familySummary 应使用角色代称（成人/宝宝/访客），不暴露真实姓名。 */
    @Test
    void build_familySummary_usesRoleNotRealName() {
        // 准备 3 个成员，name 均为 "张三"，但角色不同
        FamilyMember adult =
                createMember(1L, "张三", MemberRoleType.ADULT, MemberTargetType.BALANCED);
        FamilyMember baby = createMember(2L, "张三", MemberRoleType.BABY, MemberTargetType.BALANCED);
        FamilyMember guest =
                createMember(3L, "张三", MemberRoleType.GUEST, MemberTargetType.WEIGHT_LOSS);

        when(familyMemberRepository.findByFamilyId(1L)).thenReturn(List.of(adult, baby, guest));
        when(memberPreferenceRepository.findByMemberId(anyLong())).thenReturn(Optional.empty());
        when(recipeRepository.page(any(RecipeQueryCriteria.class)))
                .thenReturn(Collections.emptyList());
        when(ingredientFilterDomainService.filter(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        MealPlanContext context = contextBuilder.build(1L);

        // familySummary 不含真实姓名
        assertFalse(context.getFamilySummary().contains("张三"), "familySummary 不应包含真实姓名");
        // 包含角色代称
        assertTrue(context.getFamilySummary().contains("成人"), "familySummary 应包含 '成人'");
        assertTrue(context.getFamilySummary().contains("宝宝"), "familySummary 应包含 '宝宝'");
        assertTrue(context.getFamilySummary().contains("访客"), "familySummary 应包含 '访客'");
    }

    /** 候选菜品数量不超过 MAX_RECIPES（80）。 */
    @Test
    void build_recipeCatalog_maxRecipes() {
        // 准备 1 个成员（无偏好）
        FamilyMember member =
                createMember(1L, "成员A", MemberRoleType.ADULT, MemberTargetType.BALANCED);
        when(familyMemberRepository.findByFamilyId(1L)).thenReturn(List.of(member));
        when(memberPreferenceRepository.findByMemberId(1L)).thenReturn(Optional.empty());

        // 200 道菜，过滤后返回 150 道
        List<Recipe> allRecipes = createRecipes(200);
        List<Recipe> filteredRecipes = createRecipes(150);
        when(recipeRepository.page(any(RecipeQueryCriteria.class))).thenReturn(allRecipes);
        when(ingredientFilterDomainService.filter(any(), any(), any())).thenReturn(filteredRecipes);

        MealPlanContext context = contextBuilder.build(1L);

        // 候选 ID 列表最多 80 条
        assertEquals(
                MealPlanContextBuilder.MAX_RECIPES,
                context.getCandidateIds().size(),
                "candidateIds 应不超过 MAX_RECIPES");
        assertEquals(
                MealPlanContextBuilder.MAX_RECIPES,
                context.getCandidateRecipes().size(),
                "candidateRecipes 应不超过 MAX_RECIPES");
    }

    /** 偏好汇总应聚合所有成员的忌口食材。 */
    @Test
    void build_aggregatesAllMemberPreferences() {
        // 两个成员，各有不同忌口
        FamilyMember member1 =
                createMember(1L, "成员1", MemberRoleType.ADULT, MemberTargetType.BALANCED);
        FamilyMember member2 =
                createMember(2L, "成员2", MemberRoleType.ADULT, MemberTargetType.WEIGHT_LOSS);
        when(familyMemberRepository.findByFamilyId(1L)).thenReturn(List.of(member1, member2));

        // 成员1 忌口香菜，过敏花生
        MemberPreference pref1 = new MemberPreference();
        pref1.setMemberId(1L);
        pref1.setAvoidIngredients(List.of("香菜"));
        pref1.setAllergyIngredients(List.of("花生"));
        pref1.setTasteTags(List.of("清淡"));
        when(memberPreferenceRepository.findByMemberId(1L)).thenReturn(Optional.of(pref1));

        // 成员2 忌口辣椒，过敏虾
        MemberPreference pref2 = new MemberPreference();
        pref2.setMemberId(2L);
        pref2.setAvoidIngredients(List.of("辣椒"));
        pref2.setAllergyIngredients(List.of("虾"));
        pref2.setTasteTags(List.of("酸甜"));
        when(memberPreferenceRepository.findByMemberId(2L)).thenReturn(Optional.of(pref2));

        when(recipeRepository.page(any(RecipeQueryCriteria.class)))
                .thenReturn(Collections.emptyList());
        when(ingredientFilterDomainService.filter(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        MealPlanContext context = contextBuilder.build(1L);

        // 忌口应聚合两个成员的
        Set<String> avoid = context.getAvoidIngredients();
        assertTrue(avoid.contains("香菜"), "应包含成员1的忌口：香菜");
        assertTrue(avoid.contains("辣椒"), "应包含成员2的忌口：辣椒");

        // 过敏应聚合两个成员的
        Set<String> allergy = context.getAllergyIngredients();
        assertTrue(allergy.contains("花生"), "应包含成员1的过敏：花生");
        assertTrue(allergy.contains("虾"), "应包含成员2的过敏：虾");

        // preferenceSummary 应包含所有汇总
        assertTrue(context.getPreferenceSummary().contains("香菜"));
        assertTrue(context.getPreferenceSummary().contains("辣椒"));
    }

    /** 编译通过即满足——如果这个测试类能运行，说明 MealPlanContextBuilder 和相关类型编译正确。 */
    @Test
    void build_compilesSuccessfully() {
        // 验证类实例化和方法签名正确
        MealPlanContext ctx =
                MealPlanContext.builder()
                        .familySummary("test")
                        .preferenceSummary("test")
                        .recipeCatalog("test")
                        .candidateIds(List.of(1L))
                        .candidateRecipes(Collections.emptyList())
                        .avoidIngredients(Set.of("x"))
                        .allergyIngredients(Set.of("y"))
                        .build();
        assertEquals("test", ctx.getFamilySummary());
    }

    // ─── 测试辅助方法 ───

    private FamilyMember createMember(
            Long id, String name, MemberRoleType role, MemberTargetType target) {
        FamilyMember m = new FamilyMember();
        m.setId(id);
        m.setFamilyId(1L);
        m.setName(name);
        m.setRoleType(role);
        m.setTargetType(target);
        m.setBirthday(LocalDate.of(1990, 1, 1));
        return m;
    }

    private List<Recipe> createRecipes(int count) {
        List<Recipe> recipes = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Recipe r = Recipe.builder().id((long) i).name("菜品" + i).build();
            recipes.add(r);
        }
        return recipes;
    }
}
