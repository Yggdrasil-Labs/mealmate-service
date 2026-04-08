package io.yggdrasil.labs.mealmate.app.family.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.family.assembler.FamilyMemberAssembler;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.RemoveFamilyMemberCmd;
import io.yggdrasil.labs.mealmate.app.family.dto.co.FamilyMemberCO;
import io.yggdrasil.labs.mealmate.app.family.dto.qry.GetFamilyMemberDetailQry;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.MemberPreferenceRepository;
import io.yggdrasil.labs.mealmate.domain.family.service.FamilyDomainService;

@ExtendWith(MockitoExtension.class)
class GetFamilyMemberDetailQryExeTest {

    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private MemberPreferenceRepository memberPreferenceRepository;
    @Mock private FamilyProfileRepository familyProfileRepository;
    @Mock private FamilyMemberAssembler familyMemberAssembler;

    @Test
    void shouldReturnCombinedMemberDetail() {
        GetFamilyMemberDetailQry qry = new GetFamilyMemberDetailQry(1L, 2L);
        FamilyMember member = new FamilyMember();
        member.setId(2L);
        member.setFamilyId(1L);
        MemberPreference preference = new MemberPreference();
        preference.setMemberId(2L);
        FamilyMemberCO memberCO = new FamilyMemberCO();
        memberCO.setId(2L);
        memberCO.setFamilyId(1L);

        when(familyMemberRepository.findByIdAndFamilyId(2L, 1L)).thenReturn(Optional.of(member));
        when(memberPreferenceRepository.findByMemberId(2L)).thenReturn(Optional.of(preference));
        when(familyMemberAssembler.toDetailCO(member, preference)).thenReturn(memberCO);

        GetFamilyMemberDetailQryExe qryExe =
                new GetFamilyMemberDetailQryExe(
                        familyMemberRepository, memberPreferenceRepository, familyMemberAssembler);

        FamilyMemberCO result = qryExe.execute(qry);

        assertEquals(2L, result.getId());
        assertEquals(1L, result.getFamilyId());
    }

    @Test
    void shouldRejectWhenMemberMissing() {
        GetFamilyMemberDetailQry qry = new GetFamilyMemberDetailQry(1L, 200L);
        when(familyMemberRepository.findByIdAndFamilyId(200L, 1L)).thenReturn(Optional.empty());

        GetFamilyMemberDetailQryExe qryExe =
                new GetFamilyMemberDetailQryExe(
                        familyMemberRepository, memberPreferenceRepository, familyMemberAssembler);

        assertThrows(IllegalArgumentException.class, () -> qryExe.execute(qry));
    }

    @Test
    void shouldRemoveMemberAndDeletePreferenceInOneUseCase() {
        RemoveFamilyMemberCmd cmd = new RemoveFamilyMemberCmd(10L, 20L);
        FamilyMember member = new FamilyMember();
        member.setId(20L);
        member.setFamilyId(10L);
        member.setRoleType(MemberRoleType.ADULT);

        when(familyProfileRepository.existsById(10L)).thenReturn(true);
        when(familyMemberRepository.findByIdAndFamilyId(20L, 10L)).thenReturn(Optional.of(member));

        RemoveFamilyMemberCmdExe cmdExe =
                new RemoveFamilyMemberCmdExe(
                        familyProfileRepository,
                        familyMemberRepository,
                        memberPreferenceRepository,
                        new FamilyDomainService());

        cmdExe.execute(cmd);

        verify(familyMemberRepository).logicalDeleteById(20L);
        verify(memberPreferenceRepository).deleteByMemberId(20L);
    }
}
