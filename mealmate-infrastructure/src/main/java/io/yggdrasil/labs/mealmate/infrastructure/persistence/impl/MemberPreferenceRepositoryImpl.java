package io.yggdrasil.labs.mealmate.infrastructure.persistence.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;
import io.yggdrasil.labs.mealmate.domain.family.repo.MemberPreferenceRepository;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.convertor.MemberPreferenceInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.MemberPreferenceDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.service.MemberPreferenceService;
import lombok.RequiredArgsConstructor;

/** 基于 @AutoMybatis 生成的 Service 适配成员偏好仓储。 */
@Repository
@RequiredArgsConstructor
public class MemberPreferenceRepositoryImpl implements MemberPreferenceRepository {

    private final MemberPreferenceInfraConvertor memberPreferenceInfraConvertor;
    private final MemberPreferenceService memberPreferenceService;

    @Override
    public Optional<MemberPreference> findByMemberId(Long memberId) {
        if (memberId == null) {
            return Optional.empty();
        }
        LambdaQueryWrapper<MemberPreferenceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberPreferenceDO::getMemberId, memberId);
        return Optional.ofNullable(memberPreferenceService.getOne(queryWrapper))
                .map(memberPreferenceInfraConvertor::toEntity);
    }

    @Override
    public void save(MemberPreference preference) {
        memberPreferenceService.save(memberPreferenceInfraConvertor.toDo(preference));
    }

    @Override
    public void update(MemberPreference preference) {
        memberPreferenceService.updateById(memberPreferenceInfraConvertor.toDo(preference));
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        LambdaQueryWrapper<MemberPreferenceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberPreferenceDO::getMemberId, memberId);
        memberPreferenceService.remove(queryWrapper);
    }
}
