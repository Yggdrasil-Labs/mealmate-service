package io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yggdrasil.labs.mybatis.annotation.AutoMybatis;

import lombok.Data;

/** 菜谱营养表 {@code recipe_nutrition} 映射。 */
@Data
@TableName("recipe_nutrition")
@AutoMybatis
public class RecipeNutritionDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("recipe_id")
    private Long recipeId;

    @TableField("calories")
    private BigDecimal calories;

    @TableField("protein")
    private BigDecimal protein;

    @TableField("fat")
    private BigDecimal fat;

    @TableField("carbohydrate")
    private BigDecimal carbohydrate;

    @TableField("fiber")
    private BigDecimal fiber;

    @TableField("calcium")
    private BigDecimal calcium;

    @TableField("sodium")
    private BigDecimal sodium;

    @TableField("nutrition_json")
    private String nutritionJson;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private String createdBy;

    @TableField("updated_by")
    private String updatedBy;
}
