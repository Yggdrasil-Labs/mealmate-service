package io.yggdrasil.labs.mealmate.domain.family.repo;

import java.util.Optional;

import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;

/** 成员偏好仓储。偏好表无逻辑删除字段，随成员生命周期由应用层协调物理删除。 */
public interface MemberPreferenceRepository {

    /** 每个成员最多一行偏好；无记录时返回 empty。 */
    Optional<MemberPreference> findByMemberId(Long memberId);

    /** 保存新偏好。 */
    void save(MemberPreference preference);

    /** 更新已有偏好。 */
    void update(MemberPreference preference);

    /** 按成员主键物理删除偏好。 */
    void deleteByMemberId(Long memberId);
}
