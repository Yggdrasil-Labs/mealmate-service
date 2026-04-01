package io.yggdrasil.labs.mealmate.domain.family.repo;

import java.util.Optional;

import io.yggdrasil.labs.mealmate.domain.family.model.FamilyProfile;

/** 家庭画像仓储接口。查询实现需尊重逻辑删除约定（不返回已删除家庭，除非显式场景另行约定）。 */
public interface FamilyProfileRepository {

    /** 是否存在有效家庭（实现应排除已逻辑删除记录）。 */
    boolean existsById(Long familyId);

    /** 按主键加载；若已逻辑删除则返回 empty（除非仓储另有显式 API）。 */
    Optional<FamilyProfile> findById(Long familyId);
}
