package io.yggdrasil.labs.mealmate.app.family.executor;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.family.assembler.FamilyMemberAssembler;
import io.yggdrasil.labs.mealmate.app.family.dto.co.FamilyMemberCO;
import io.yggdrasil.labs.mealmate.app.family.dto.qry.GetFamilyMemberDetailQry;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.MemberPreferenceRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetFamilyMemberDetailQryExe {

    private final FamilyMemberRepository familyMemberRepository;
    private final MemberPreferenceRepository memberPreferenceRepository;
    private final FamilyMemberAssembler familyMemberAssembler;

    public FamilyMemberCO execute(GetFamilyMemberDetailQry qry) {
        FamilyMember familyMember =
                familyMemberRepository
                        .findByIdAndFamilyId(qry.getMemberId(), qry.getFamilyId())
                        .orElseThrow(() -> new IllegalArgumentException("Member does not exist"));
        MemberPreference memberPreference =
                memberPreferenceRepository.findByMemberId(qry.getMemberId()).orElse(null);
        return familyMemberAssembler.toDetailCO(familyMember, memberPreference);
    }
}
