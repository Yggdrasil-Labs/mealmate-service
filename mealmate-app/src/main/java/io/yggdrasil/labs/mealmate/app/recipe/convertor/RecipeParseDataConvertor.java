package io.yggdrasil.labs.mealmate.app.recipe.convertor;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.CreateRecipeCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.NutritionFactCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.RecipeIngredientItemCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.RecipeStepItemCmd;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiErrorCode;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParsedData;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.IngredientType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.SeasonTag;

/**
 * RecipeParsedData → CreateRecipeCmd 转换器。
 *
 * <p>将 AI 解析的中间态数据转为可入库的命令对象。 String 枚举字段通过 valueOf 转换，转换异常时抛出 AI_RECIPE_INCOMPLETE。
 */
@Component
public class RecipeParseDataConvertor {

    /**
     * 将解析数据转换为创建菜品命令。
     *
     * @param data 解析数据
     * @return CreateRecipeCmd
     * @throws BizException 必填字段缺失或枚举转换失败时抛出 AI_RECIPE_INCOMPLETE
     */
    public CreateRecipeCmd toCreateRecipeCmd(RecipeParsedData data) {
        if (data == null || data.getName() == null || data.getName().isBlank()) {
            throw new BizException(AiErrorCode.AI_RECIPE_INCOMPLETE);
        }

        CreateRecipeCmd cmd = new CreateRecipeCmd();
        cmd.setName(data.getName());
        cmd.setRecipeType(
                parseEnum(RecipeType.class, data.getRecipeType(), RecipeType.HOME_COOKING));
        cmd.setSeasonTag(parseEnumOrNull(SeasonTag.class, data.getSeasonTag()));
        cmd.setCrowdTag(parseEnumOrNull(CrowdTag.class, data.getCrowdTag()));
        cmd.setTasteTags(data.getTasteTags());
        cmd.setDifficultyLevel(parseEnumOrNull(DifficultyLevel.class, data.getDifficultyLevel()));
        cmd.setCookingTimeMin(data.getCookingTimeMin());
        cmd.setBabyFriendly(data.getBabyFriendly());
        cmd.setWeightLossFriendly(data.getWeightLossFriendly());

        // 食材转换
        if (data.getIngredients() != null) {
            cmd.setIngredients(
                    data.getIngredients().stream()
                            .map(this::toIngredientCmd)
                            .collect(Collectors.toList()));
        }

        // 步骤转换
        if (data.getSteps() != null) {
            cmd.setSteps(
                    data.getSteps().stream().map(this::toStepCmd).collect(Collectors.toList()));
        }

        // 营养信息转换
        if (data.getNutritionFact() != null) {
            cmd.setNutritionFact(toNutritionCmd(data.getNutritionFact()));
        }

        return cmd;
    }

    private RecipeIngredientItemCmd toIngredientCmd(RecipeParsedData.IngredientItem item) {
        RecipeIngredientItemCmd cmd = new RecipeIngredientItemCmd();
        cmd.setIngredientName(item.getIngredientName());
        cmd.setIngredientType(
                parseEnum(IngredientType.class, item.getIngredientType(), IngredientType.OTHER));
        cmd.setQuantity(item.getQuantity() != null ? BigDecimal.valueOf(item.getQuantity()) : null);
        cmd.setUnit(item.getUnit());
        cmd.setMainIngredient(item.getMainIngredient() != null ? item.getMainIngredient() : false);
        return cmd;
    }

    private RecipeStepItemCmd toStepCmd(RecipeParsedData.StepItem item) {
        RecipeStepItemCmd cmd = new RecipeStepItemCmd();
        cmd.setStepNo(item.getStepNo());
        cmd.setContent(item.getContent());
        return cmd;
    }

    private NutritionFactCmd toNutritionCmd(RecipeParsedData.NutritionItem item) {
        NutritionFactCmd cmd = new NutritionFactCmd();
        cmd.setCalories(item.getCalories() != null ? BigDecimal.valueOf(item.getCalories()) : null);
        cmd.setProtein(item.getProtein() != null ? BigDecimal.valueOf(item.getProtein()) : null);
        cmd.setFat(item.getFat() != null ? BigDecimal.valueOf(item.getFat()) : null);
        cmd.setCarbohydrate(
                item.getCarbohydrate() != null ? BigDecimal.valueOf(item.getCarbohydrate()) : null);
        return cmd;
    }

    /** 解析枚举，失败时返回默认值 */
    private <T extends Enum<T>> T parseEnum(Class<T> clazz, String value, T defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(clazz, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /** 解析枚举，失败时返回 null */
    private <T extends Enum<T>> T parseEnumOrNull(Class<T> clazz, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(clazz, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
