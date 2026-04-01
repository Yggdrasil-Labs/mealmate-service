package io.yggdrasil.labs.mealmate.domain.family.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.yggdrasil.labs.mealmate.domain.family.model.enums.GenderType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberStatus;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberTargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 家庭成员：UC1 核心实体。年龄由 {@link #birthday} 在查询侧推导，领域不持久化年龄字段。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMember {

    private Long id;

    /** 归属家庭，与 {@link FamilyProfile#getId()} 对应。 */
    private Long familyId;

    private String name;

    /** 角色类型；同一家庭允许多个 {@link MemberRoleType#BABY}，不做唯一约束。 */
    private MemberRoleType roleType;

    private GenderType gender;

    /** 生日；用于推导年龄，避免库表冗余存储 age。 */
    private LocalDate birthday;

    /** 成员个人地域，可与家庭默认地域不同。 */
    private String region;

    private MemberTargetType targetType;
    private String avatarUrl;
    private Integer sortNo;
    private MemberStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    /** 逻辑删除，语义同 {@link FamilyProfile#getDeleted()}。 */
    private Integer deleted;
}
