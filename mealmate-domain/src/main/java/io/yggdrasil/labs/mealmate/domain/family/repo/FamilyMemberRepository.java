package io.yggdrasil.labs.mealmate.domain.family.repo;

import java.util.List;
import java.util.Optional;

import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;

/** 家庭成员仓储接口。列表与单条查询默认仅包含未逻辑删除的成员。 */
public interface FamilyMemberRepository {

    /** 某家庭下的有效成员列表，通常按 sort_no 等在实现层排序。 */
    List<FamilyMember> findByFamilyId(Long familyId);

    Optional<FamilyMember> findById(Long memberId);
}
