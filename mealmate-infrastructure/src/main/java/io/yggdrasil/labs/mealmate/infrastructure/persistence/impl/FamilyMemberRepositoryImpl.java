package io.yggdrasil.labs.mealmate.infrastructure.persistence.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.convertor.FamilyMemberInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.FamilyMemberDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.service.FamilyMemberService;
import lombok.RequiredArgsConstructor;

/** 基于 @AutoMybatis 生成的 Service 实现成员仓储能力。 */
@Repository
@RequiredArgsConstructor
public class FamilyMemberRepositoryImpl implements FamilyMemberRepository {

    private final FamilyMemberInfraConvertor familyMemberInfraConvertor;
    private final FamilyMemberService familyMemberService;

    @Override
    public List<FamilyMember> findByFamilyId(Long familyId) {
        LambdaQueryWrapper<FamilyMemberDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(FamilyMemberDO::getFamilyId, familyId)
                .orderByAsc(FamilyMemberDO::getSortNo, FamilyMemberDO::getId);
        return familyMemberService.list(queryWrapper).stream()
                .map(familyMemberInfraConvertor::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FamilyMember> findById(Long memberId) {
        if (memberId == null) {
            return Optional.empty();
        }
        FamilyMemberDO dataObject = familyMemberService.getById(memberId);
        return Optional.ofNullable(familyMemberInfraConvertor.toEntity(dataObject));
    }

    @Override
    public Optional<FamilyMember> findByIdAndFamilyId(Long memberId, Long familyId) {
        if (memberId == null || familyId == null) {
            return Optional.empty();
        }
        LambdaQueryWrapper<FamilyMemberDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FamilyMemberDO::getId, memberId).eq(FamilyMemberDO::getFamilyId, familyId);
        return Optional.ofNullable(familyMemberService.getOne(queryWrapper))
                .map(familyMemberInfraConvertor::toEntity);
    }

    @Override
    public void save(FamilyMember member) {
        familyMemberService.save(familyMemberInfraConvertor.toDo(member));
    }

    @Override
    public void update(FamilyMember member) {
        familyMemberService.updateById(familyMemberInfraConvertor.toDo(member));
    }

    @Override
    public void logicalDeleteById(Long memberId) {
        familyMemberService.removeById(memberId);
    }
}
