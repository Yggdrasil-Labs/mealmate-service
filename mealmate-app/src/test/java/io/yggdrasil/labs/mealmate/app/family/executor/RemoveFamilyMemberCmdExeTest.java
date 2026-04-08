package io.yggdrasil.labs.mealmate.app.family.executor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.family.dto.cmd.RemoveFamilyMemberCmd;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.MemberPreferenceRepository;
import io.yggdrasil.labs.mealmate.domain.family.service.FamilyDomainService;

@ExtendWith(MockitoExtension.class)
class RemoveFamilyMemberCmdExeTest {

    @Mock private FamilyProfileRepository familyProfileRepository;
    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private MemberPreferenceRepository memberPreferenceRepository;
    @Mock private FamilyDomainService familyDomainService;

    @InjectMocks private RemoveFamilyMemberCmdExe removeFamilyMemberCmdExe;

    @Test
    void shouldLogicallyDeleteMemberAndPhysicallyDeletePreference() {
        RemoveFamilyMemberCmd cmd = new RemoveFamilyMemberCmd(1L, 10L);

        FamilyMember member = new FamilyMember();
        member.setId(10L);
        member.setFamilyId(1L);
        when(familyMemberRepository.findByIdAndFamilyId(10L, 1L)).thenReturn(Optional.of(member));

        removeFamilyMemberCmdExe.execute(cmd);

        verify(familyMemberRepository).logicalDeleteById(10L);
        verify(memberPreferenceRepository).deleteByMemberId(10L);
    }
}
