package io.yggdrasil.labs.mealmate.app.family.executor;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.family.convertor.FamilyMemberConvertor;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.UpdateFamilyMemberCmd;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;
import io.yggdrasil.labs.mealmate.domain.family.service.FamilyDomainService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateFamilyMemberCmdExe {

    private final FamilyProfileRepository familyProfileRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyDomainService familyDomainService;
    private final FamilyMemberConvertor familyMemberConvertor;

    public void execute(UpdateFamilyMemberCmd cmd) {
        familyDomainService.assertFamilyExists(familyProfileRepository, cmd.getFamilyId());
        FamilyMember persistedMember =
                familyMemberRepository
                        .findByIdAndFamilyId(cmd.getMemberId(), cmd.getFamilyId())
                        .orElseThrow(() -> new IllegalArgumentException("Member does not exist"));
        familyDomainService.assertMemberBelongsToFamily(persistedMember, cmd.getFamilyId());

        FamilyMember updatedMember = familyMemberConvertor.toFamilyMember(cmd);
        updatedMember.setId(persistedMember.getId());
        updatedMember.setFamilyId(persistedMember.getFamilyId());
        if (updatedMember.getSortNo() == null) {
            updatedMember.setSortNo(persistedMember.getSortNo());
        }
        if (updatedMember.getStatus() == null) {
            updatedMember.setStatus(persistedMember.getStatus());
        }
        familyMemberRepository.update(updatedMember);
    }
}
