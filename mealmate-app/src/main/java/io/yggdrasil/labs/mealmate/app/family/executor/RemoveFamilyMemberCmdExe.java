package io.yggdrasil.labs.mealmate.app.family.executor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.family.dto.cmd.RemoveFamilyMemberCmd;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.MemberPreferenceRepository;
import io.yggdrasil.labs.mealmate.domain.family.service.FamilyDomainService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RemoveFamilyMemberCmdExe {

    private final FamilyProfileRepository familyProfileRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final MemberPreferenceRepository memberPreferenceRepository;
    private final FamilyDomainService familyDomainService;

    @Transactional(rollbackFor = Exception.class)
    public void execute(RemoveFamilyMemberCmd cmd) {
        familyDomainService.assertFamilyExists(familyProfileRepository, cmd.getFamilyId());
        FamilyMember familyMember =
                familyMemberRepository
                        .findByIdAndFamilyId(cmd.getMemberId(), cmd.getFamilyId())
                        .orElseThrow(() -> new IllegalArgumentException("Member does not exist"));
        familyDomainService.assertMemberBelongsToFamily(familyMember, cmd.getFamilyId());

        familyMemberRepository.logicalDeleteById(cmd.getMemberId());
        memberPreferenceRepository.deleteByMemberId(cmd.getMemberId());
    }
}
