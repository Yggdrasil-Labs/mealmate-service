package io.yggdrasil.labs.mealmate.domain.family.model;

import java.time.LocalDateTime;
import java.util.Map;

import io.yggdrasil.labs.mealmate.domain.family.model.enums.FamilyStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 家庭画像：归组与默认配置载体，成员通过 {@code familyId} 归属本聚合上下文。
 *
 * <p>{@code deleted} 与仓储查询配合表示逻辑删除；具体过滤策略由基础设施层实现。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyProfile {

    /** 主键，与持久化表 {@code family_profile.id} 对应。 */
    private Long id;

    private String familyName;

    /** 业务侧唯一编码，库表层有唯一约束。 */
    private String familyCode;

    private FamilyStatus status;

    /** 默认地域等轻量元数据。 */
    private String region;

    /** 家庭级饮食目标，领域与接口层可用 Map 表达，持久化为 JSON。 */
    private Map<String, Object> mealGoal;

    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    /** 逻辑删除：0 有效，非 0 视为已删除（与库表约定一致）。 */
    private Integer deleted;
}
