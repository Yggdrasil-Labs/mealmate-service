package io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yggdrasil.labs.mybatis.annotation.AutoMybatis;

import lombok.Data;

/** 备菜计划条目表 {@code prep_plan_item} 映射。物理删除，无 @TableLogic。 */
@Data
@TableName("prep_plan_item")
@AutoMybatis
public class PrepPlanItemDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("prep_plan_id")
    private Long prepPlanId;

    @TableField("ingredient_name")
    private String ingredientName;

    private BigDecimal quantity;

    private String unit;

    @TableField("storage_method")
    private String storageMethod;

    private String priority;

    @TableField("task_status")
    private String taskStatus;

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
