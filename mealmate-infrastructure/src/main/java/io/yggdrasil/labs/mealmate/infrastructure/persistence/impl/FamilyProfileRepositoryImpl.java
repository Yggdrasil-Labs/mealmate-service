package io.yggdrasil.labs.mealmate.infrastructure.persistence.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import io.yggdrasil.labs.mealmate.domain.family.model.FamilyProfile;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.convertor.FamilyProfileInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.FamilyProfileDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.service.FamilyProfileService;
import lombok.RequiredArgsConstructor;

/** 基于 @AutoMybatis 生成的 Service 适配领域仓储，避免手写/直连 Mapper。 */
@Repository
@RequiredArgsConstructor
public class FamilyProfileRepositoryImpl implements FamilyProfileRepository {

    private final FamilyProfileInfraConvertor familyProfileInfraConvertor;
    private final FamilyProfileService familyProfileService;

    @Override
    public FamilyProfile save(FamilyProfile familyProfile) {
        FamilyProfileDO dataObject = familyProfileInfraConvertor.toDo(familyProfile);
        familyProfileService.save(dataObject);
        return familyProfileInfraConvertor.toEntity(dataObject);
    }

    @Override
    public boolean existsById(Long familyId) {
        if (familyId == null) {
            return false;
        }
        return familyProfileService.getById(familyId) != null;
    }

    @Override
    public Optional<FamilyProfile> findById(Long familyId) {
        if (familyId == null) {
            return Optional.empty();
        }
        FamilyProfileDO dataObject = familyProfileService.getById(familyId);
        return Optional.ofNullable(familyProfileInfraConvertor.toEntity(dataObject));
    }
}
