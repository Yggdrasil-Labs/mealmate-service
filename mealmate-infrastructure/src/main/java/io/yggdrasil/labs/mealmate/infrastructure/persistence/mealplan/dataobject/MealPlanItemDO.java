package io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yggdrasil.labs.mybatis.annotation.AutoMybatis;

import lombok.Data;

/** 餐计划条目表 {@code meal_plan_item} 映射。物理删除，无 @TableLogic。 */
@Data
@TableName("meal_plan_item")
@AutoMybatis
public class MealPlanItemDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("plan_id")
    private Long planId;

    @TableField("meal_date")
    private LocalDate mealDate;

    @TableField("meal_type")
    private String mealType;

    @TableField("recipe_id")
    private Long recipeId;

    @TableField("crowd_type")
    private String crowdType;

    @TableField("is_weight_loss")
    private Boolean isWeightLoss;

    @TableField("is_baby_meal")
    private Boolean isBabyMeal;

    @TableField("duplicate_flag")
    private Boolean duplicateFlag;

    @TableField("sort_order")
    private Integer sortOrder;

    private String remark;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;
}
