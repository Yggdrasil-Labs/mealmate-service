package io.yggdrasil.labs.mealmate.app.family.executor;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.family.convertor.FamilyMemberConvertor;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.AddFamilyMemberCmd;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberStatus;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;
import io.yggdrasil.labs.mealmate.domain.family.service.FamilyDomainService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AddFamilyMemberCmdExe {

    private final FamilyProfileRepository familyProfileRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyDomainService familyDomainService;
    private final FamilyMemberConvertor familyMemberConvertor;

    public void execute(AddFamilyMemberCmd cmd) {
        familyDomainService.assertFamilyExists(familyProfileRepository, cmd.getFamilyId());
        FamilyMember familyMember = familyMemberConvertor.toFamilyMember(cmd);
        familyMember.setFamilyId(cmd.getFamilyId());
        if (familyMember.getStatus() == null) {
            familyMember.setStatus(MemberStatus.ACTIVE);
        }
        if (familyMember.getSortNo() == null) {
            familyMember.setSortNo(0);
        }
        familyMemberRepository.save(familyMember);
    }
}
