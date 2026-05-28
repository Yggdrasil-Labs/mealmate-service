package io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yggdrasil.labs.mybatis.annotation.AutoMybatis;

import lombok.Data;

/** 周餐计划主表 {@code weekly_meal_plan} 映射。 */
@Data
@TableName("weekly_meal_plan")
@AutoMybatis
public class WeeklyMealPlanDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("family_id")
    private Long familyId;

    @TableField("week_start_date")
    private LocalDate weekStartDate;

    @TableField("week_end_date")
    private LocalDate weekEndDate;

    private String status;

    @TableField("plan_source")
    private String planSource;

    @TableField("rule_snapshot_json")
    private String ruleSnapshotJson;

    private String remark;

    @TableField("generated_time")
    private LocalDateTime generatedTime;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;

    @TableLogic(value = "0", delval = "id")
    private Long deleted;
}
