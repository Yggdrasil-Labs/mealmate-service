package io.yggdrasil.labs.mealmate.domain.family.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.OilLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SaltLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SpicyLevel;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;

/**
 * UC1 家庭与成员相关的领域规则。
 *
 * <p>不依赖 Spring，由应用层编排事务与仓储调用；此处仅表达不变式与校验，便于单测与复用。
 */
public class FamilyDomainService {

    /**
     * 校验家庭存在。仓储由调用方注入，避免领域层直接依赖具体持久化实现类型。
     *
     * @param familyProfileRepository 家庭仓储（可为空，视为不存在）
     * @param familyId 家庭主键
     * @throws IllegalArgumentException 当家庭不存在或参数无效时
     */
    public void assertFamilyExists(FamilyProfileRepository familyProfileRepository, Long familyId) {
        if (familyId == null
                || familyProfileRepository == null
                || !familyProfileRepository.existsById(familyId)) {
            throw new IllegalArgumentException("Family does not exist");
        }
    }

    /**
     * 校验成员归属指定家庭，防止跨家庭误操作。
     *
     * @param member 成员实体（需已加载 {@code familyId}）
     * @param familyId 当前操作上下文中的家庭 ID
     * @throws IllegalArgumentException 当成员为空或归属不一致时
     */
    public void assertMemberBelongsToFamily(FamilyMember member, Long familyId) {
        if (member == null
                || member.getFamilyId() == null
                || !Objects.equals(member.getFamilyId(), familyId)) {
            throw new IllegalArgumentException("Member does not belong to this family");
        }
    }

    /**
     * 按角色校验偏好是否合法。当前仅对 {@link MemberRoleType#BABY} 施加辣度、盐度、油量限制。
     *
     * <p>与持久化无关：未设置的枚举字段视为「未选择」，不触发校验。
     *
     * @param memberRoleType 成员角色
     * @param preference 偏好（可为 null，非婴儿角色时直接通过）
     * @throws IllegalArgumentException 当违反婴儿饮食约束时
     */
    public void validatePreferenceForMember(
            MemberRoleType memberRoleType, MemberPreference preference) {
        if (memberRoleType != MemberRoleType.BABY || preference == null) {
            return;
        }
        if (preference.getSpicyLevel() == SpicyLevel.MILD
                || preference.getSpicyLevel() == SpicyLevel.MEDIUM
                || preference.getSpicyLevel() == SpicyLevel.HOT) {
            throw new IllegalArgumentException("Baby cannot have spicy preference");
        }
        if (preference.getSaltLevel() == SaltLevel.SALTY) {
            throw new IllegalArgumentException("Baby cannot have salty preference");
        }
        if (preference.getOilLevel() == OilLevel.RICH) {
            throw new IllegalArgumentException("Baby cannot have rich oil preference");
        }
    }

    /**
     * 规范化偏好中的列表类字段：去空、trim、去重，保证领域层与存储层（如逗号分隔字符串）映射时语义一致。
     *
     * @param preference 偏好，可为 null
     * @return 同一实例（为 null 时返回 null）
     */
    public MemberPreference normalizePreference(MemberPreference preference) {
        if (preference == null) {
            return null;
        }
        preference.setTasteTags(normalizeStringList(preference.getTasteTags()));
        preference.setAvoidIngredients(normalizeStringList(preference.getAvoidIngredients()));
        preference.setAllergyIngredients(normalizeStringList(preference.getAllergyIngredients()));
        return preference;
    }

    /** 列表规范化：null 视为空列表；保留首次出现顺序。 */
    private List<String> normalizeStringList(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
