package io.yggdrasil.labs.mealmate.app.family.executor;

import java.util.List;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.family.assembler.FamilyMemberAssembler;
import io.yggdrasil.labs.mealmate.app.family.dto.co.FamilyMemberCO;
import io.yggdrasil.labs.mealmate.app.family.dto.qry.GetFamilyMemberListQry;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;
import io.yggdrasil.labs.mealmate.domain.family.service.FamilyDomainService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetFamilyMemberListQryExe {

    private final FamilyProfileRepository familyProfileRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyMemberAssembler familyMemberAssembler;
    private final FamilyDomainService familyDomainService;

    public List<FamilyMemberCO> execute(GetFamilyMemberListQry qry) {
        familyDomainService.assertFamilyExists(familyProfileRepository, qry.getFamilyId());
        return familyMemberAssembler.toFamilyMemberCOList(
                familyMemberRepository.findByFamilyId(qry.getFamilyId()));
    }
}
