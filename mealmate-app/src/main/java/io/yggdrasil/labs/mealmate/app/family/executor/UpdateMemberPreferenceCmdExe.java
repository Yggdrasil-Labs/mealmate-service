package io.yggdrasil.labs.mealmate.app.family.executor;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.family.convertor.FamilyMemberConvertor;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.UpdateMemberPreferenceCmd;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.MemberPreferenceRepository;
import io.yggdrasil.labs.mealmate.domain.family.service.FamilyDomainService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateMemberPreferenceCmdExe {

    private final FamilyProfileRepository familyProfileRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final MemberPreferenceRepository memberPreferenceRepository;
    private final FamilyDomainService familyDomainService;
    private final FamilyMemberConvertor familyMemberConvertor;

    public void execute(UpdateMemberPreferenceCmd cmd) {
        familyDomainService.assertFamilyExists(familyProfileRepository, cmd.getFamilyId());
        FamilyMember familyMember =
                familyMemberRepository
                        .findByIdAndFamilyId(cmd.getMemberId(), cmd.getFamilyId())
                        .orElseThrow(() -> new IllegalArgumentException("Member does not exist"));
        familyDomainService.assertMemberBelongsToFamily(familyMember, cmd.getFamilyId());

        MemberPreference memberPreference = familyMemberConvertor.toMemberPreference(cmd);
        memberPreference.setMemberId(cmd.getMemberId());
        familyDomainService.normalizePreference(memberPreference);
        familyDomainService.validatePreferenceForMember(
                familyMember.getRoleType(), memberPreference);

        memberPreferenceRepository
                .findByMemberId(cmd.getMemberId())
                .ifPresent(
                        existingPreference -> memberPreference.setId(existingPreference.getId()));
        if (memberPreference.getId() == null) {
            memberPreferenceRepository.save(memberPreference);
            return;
        }
        memberPreferenceRepository.update(memberPreference);
    }
}
