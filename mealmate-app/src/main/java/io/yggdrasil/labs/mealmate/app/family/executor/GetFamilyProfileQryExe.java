package io.yggdrasil.labs.mealmate.app.family.executor;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.family.assembler.FamilyMemberAssembler;
import io.yggdrasil.labs.mealmate.app.family.dto.co.FamilyProfileCO;
import io.yggdrasil.labs.mealmate.app.family.dto.qry.GetFamilyProfileQry;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetFamilyProfileQryExe {

    private final FamilyProfileRepository familyProfileRepository;
    private final FamilyMemberAssembler familyMemberAssembler;

    public FamilyProfileCO execute(GetFamilyProfileQry qry) {
        return familyProfileRepository
                .findById(qry.getFamilyId())
                .map(familyMemberAssembler::toFamilyProfileCO)
                .orElseThrow(() -> new IllegalArgumentException("Family does not exist"));
    }
}
