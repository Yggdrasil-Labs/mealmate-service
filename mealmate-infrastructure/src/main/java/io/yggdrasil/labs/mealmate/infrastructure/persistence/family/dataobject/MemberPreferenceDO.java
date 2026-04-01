package io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yggdrasil.labs.mybatis.annotation.AutoMybatis;

import lombok.Data;

/**
 * 成员偏好表 {@code member_preference} 映射。
 *
 * <p>列表类字段在库表为逗号分隔字符串；结构化字段为 JSON 字符串。
 */
@Data
@TableName("member_preference")
@AutoMybatis
public class MemberPreferenceDO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("member_id")
    private Long memberId;

    @TableField("taste_tags")
    private String tasteTags;

    @TableField("avoid_ingredients")
    private String avoidIngredients;

    @TableField("allergy_ingredients")
    private String allergyIngredients;

    @TableField("spicy_level")
    private String spicyLevel;

    @TableField("sweet_level")
    private String sweetLevel;

    @TableField("oil_level")
    private String oilLevel;

    @TableField("salt_level")
    private String saltLevel;

    @TableField("nutrition_goal_json")
    private String nutritionGoalJson;

    @TableField("extra_rule_json")
    private String extraRuleJson;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private String createdBy;

    @TableField("updated_by")
    private String updatedBy;
}
