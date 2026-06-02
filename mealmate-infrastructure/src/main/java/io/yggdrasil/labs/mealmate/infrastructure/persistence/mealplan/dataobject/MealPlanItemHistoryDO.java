package io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yggdrasil.labs.mybatis.annotation.AutoMybatis;

import lombok.Data;

/** 餐计划条目调整历史表 {@code meal_plan_item_history} 映射。 */
@Data
@TableName("meal_plan_item_history")
@AutoMybatis
public class MealPlanItemHistoryDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("item_id")
    private Long itemId;

    @TableField("plan_id")
    private Long planId;

    @TableField("old_recipe_id")
    private Long oldRecipeId;

    @TableField("new_recipe_id")
    private Long newRecipeId;

    @TableField("adjust_reason")
    private String adjustReason;

    @TableField("adjusted_at")
    private LocalDateTime adjustedAt;

    @TableField("adjusted_by")
    private Long adjustedBy;
}
