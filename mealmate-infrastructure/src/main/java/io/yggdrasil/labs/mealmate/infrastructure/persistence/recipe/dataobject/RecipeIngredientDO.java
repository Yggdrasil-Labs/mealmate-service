package io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yggdrasil.labs.mybatis.annotation.AutoMybatis;

import lombok.Data;

/** 菜谱食材表 {@code recipe_ingredient} 映射。 */
@Data
@TableName("recipe_ingredient")
@AutoMybatis
public class RecipeIngredientDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("recipe_id")
    private Long recipeId;

    @TableField("ingredient_name")
    private String ingredientName;

    @TableField("ingredient_type")
    private String ingredientType;

    private BigDecimal quantity;

    private String unit;

    @TableField("is_main")
    private Boolean mainIngredient;

    @TableField("sort_no")
    private Integer sortNo;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private String createdBy;

    @TableField("updated_by")
    private String updatedBy;
}
