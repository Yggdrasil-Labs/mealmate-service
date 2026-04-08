package io.yggdrasil.labs.mealmate.app.family.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.family.convertor.FamilyMemberConvertor;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.AddFamilyMemberCmd;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberStatus;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;
import io.yggdrasil.labs.mealmate.domain.family.service.FamilyDomainService;

@ExtendWith(MockitoExtension.class)
class AddFamilyMemberCmdExeTest {

    @Mock private FamilyProfileRepository familyProfileRepository;
    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private FamilyDomainService familyDomainService;
    @Mock private FamilyMemberConvertor familyMemberConvertor;

    @Test
    void shouldAddMemberUnderValidFamily() {
        AddFamilyMemberCmd cmd = new AddFamilyMemberCmd();
        cmd.setFamilyId(1L);
        cmd.setName("Alice");
        cmd.setRoleType(MemberRoleType.ADULT);

        FamilyMember member = new FamilyMember();
        member.setName("Alice");
        when(familyMemberConvertor.toFamilyMember(cmd)).thenReturn(member);

        AddFamilyMemberCmdExe cmdExe =
                new AddFamilyMemberCmdExe(
                        familyProfileRepository,
                        familyMemberRepository,
                        familyDomainService,
                        familyMemberConvertor);

        cmdExe.execute(cmd);

        verify(familyDomainService).assertFamilyExists(familyProfileRepository, 1L);
        verify(familyMemberRepository).save(member);
        assertEquals(1L, member.getFamilyId());
        assertEquals(MemberStatus.ACTIVE, member.getStatus());
        assertEquals(0, member.getSortNo());
    }

    @Test
    void shouldRejectAddWhenFamilyMissing() {
        AddFamilyMemberCmd cmd = new AddFamilyMemberCmd();
        cmd.setFamilyId(100L);
        cmd.setName("Bob");
        cmd.setRoleType(MemberRoleType.ADULT);

        doThrow(new IllegalArgumentException("Family does not exist"))
                .when(familyDomainService)
                .assertFamilyExists(familyProfileRepository, 100L);

        AddFamilyMemberCmdExe cmdExe =
                new AddFamilyMemberCmdExe(
                        familyProfileRepository,
                        familyMemberRepository,
                        familyDomainService,
                        familyMemberConvertor);

        assertThrows(IllegalArgumentException.class, () -> cmdExe.execute(cmd));
        verifyNoInteractions(familyMemberConvertor);
        verifyNoInteractions(familyMemberRepository);
    }
}
