package io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yggdrasil.labs.mybatis.annotation.AutoMybatis;

import lombok.Data;

/** 备菜计划表 {@code prep_plan} 映射。 */
@Data
@TableName("prep_plan")
@AutoMybatis
public class PrepPlanDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("plan_id")
    private Long planId;

    @TableField("push_status")
    private String pushStatus;

    @TableField("generated_time")
    private LocalDateTime generatedTime;

    private String remark;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
