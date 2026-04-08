package io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yggdrasil.labs.mybatis.annotation.AutoMybatis;

import lombok.Data;

/**
 * 家庭画像表 {@code family_profile} 映射。
 *
 * <p>JSON 列在 DO 中使用字符串承载，与领域 Map 的互转由 FamilyProfileInfraConvertor 完成。
 */
@Data
@TableName("family_profile")
@AutoMybatis
public class FamilyProfileDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("family_name")
    private String familyName;

    @TableField("family_code")
    private String familyCode;

    private String status;

    private String region;

    @TableField("meal_goal_json")
    private String mealGoalJson;

    private String remark;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private String createdBy;

    @TableField("updated_by")
    private String updatedBy;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
