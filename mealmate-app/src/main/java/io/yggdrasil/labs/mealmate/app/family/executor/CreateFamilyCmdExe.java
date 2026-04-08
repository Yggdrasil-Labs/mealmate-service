package io.yggdrasil.labs.mealmate.app.family.executor;

import java.util.UUID;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.family.assembler.FamilyMemberAssembler;
import io.yggdrasil.labs.mealmate.app.family.convertor.FamilyProfileConvertor;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.CreateFamilyCmd;
import io.yggdrasil.labs.mealmate.app.family.dto.co.FamilyProfileCO;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyProfile;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.FamilyStatus;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateFamilyCmdExe {

    private static final String FAMILY_CODE_PREFIX = "FAM_";

    private final FamilyProfileRepository familyProfileRepository;
    private final FamilyProfileConvertor familyProfileConvertor;
    private final FamilyMemberAssembler familyMemberAssembler;

    public FamilyProfileCO execute(CreateFamilyCmd cmd) {
        FamilyProfile familyProfile = familyProfileConvertor.toFamilyProfile(cmd);
        familyProfile.setFamilyCode(generateFamilyCode());
        familyProfile.setStatus(FamilyStatus.ENABLED);
        FamilyProfile savedFamilyProfile = familyProfileRepository.save(familyProfile);
        return familyMemberAssembler.toFamilyProfileCO(savedFamilyProfile);
    }

    private String generateFamilyCode() {
        return FAMILY_CODE_PREFIX + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
