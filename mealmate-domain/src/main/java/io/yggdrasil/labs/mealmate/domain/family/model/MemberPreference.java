package io.yggdrasil.labs.mealmate.domain.family.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.yggdrasil.labs.mealmate.domain.family.model.enums.OilLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SaltLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SpicyLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SweetLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成员饮食偏好扩展，与 {@link FamilyMember} 一对一（{@code member_id} 唯一）。
 *
 * <p>列表字段在领域层为 {@link List}，持久化层可映射为逗号分隔字符串；结构化目标为 Map/JSON。
 *
 * <p>删除策略：成员逻辑删除时由应用层事务内物理删除对应偏好行，领域模型本身不承载「已删除」标记。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberPreference {

    private Long id;

    /** 关联成员主键，全库唯一。 */
    private Long memberId;

    /** 口味标签等业务列表；写入前应在领域服务中做去空、去重与 trim。 */
    private List<String> tasteTags;

    private List<String> avoidIngredients;
    private List<String> allergyIngredients;
    private SpicyLevel spicyLevel;
    private SweetLevel sweetLevel;
    private OilLevel oilLevel;
    private SaltLevel saltLevel;
    private Map<String, Object> nutritionGoal;
    private Map<String, Object> extraRule;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
