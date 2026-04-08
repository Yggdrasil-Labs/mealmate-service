package io.yggdrasil.labs.mealmate.app.family.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.family.assembler.FamilyMemberAssembler;
import io.yggdrasil.labs.mealmate.app.family.dto.co.FamilyMemberCO;
import io.yggdrasil.labs.mealmate.app.family.dto.qry.GetFamilyMemberListQry;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;
import io.yggdrasil.labs.mealmate.domain.family.service.FamilyDomainService;

@ExtendWith(MockitoExtension.class)
class GetFamilyMemberListQryExeTest {

    @Mock private FamilyProfileRepository familyProfileRepository;
    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private FamilyMemberAssembler familyMemberAssembler;
    @Mock private FamilyDomainService familyDomainService;

    @Test
    void shouldReturnMemberListWhenFamilyExists() {
        GetFamilyMemberListQry qry = new GetFamilyMemberListQry(1L);
        FamilyMember member = new FamilyMember();
        member.setId(2L);
        FamilyMemberCO memberCO = new FamilyMemberCO();
        memberCO.setId(2L);

        when(familyMemberRepository.findByFamilyId(1L)).thenReturn(List.of(member));
        when(familyMemberAssembler.toFamilyMemberCOList(List.of(member)))
                .thenReturn(List.of(memberCO));

        GetFamilyMemberListQryExe qryExe =
                new GetFamilyMemberListQryExe(
                        familyProfileRepository,
                        familyMemberRepository,
                        familyMemberAssembler,
                        familyDomainService);

        List<FamilyMemberCO> result = qryExe.execute(qry);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
        verify(familyDomainService).assertFamilyExists(familyProfileRepository, 1L);
    }

    @Test
    void shouldRejectWhenFamilyMissing() {
        GetFamilyMemberListQry qry = new GetFamilyMemberListQry(999L);
        GetFamilyMemberListQryExe qryExe =
                new GetFamilyMemberListQryExe(
                        familyProfileRepository,
                        familyMemberRepository,
                        familyMemberAssembler,
                        familyDomainService);
        doThrow(new IllegalArgumentException("Family does not exist"))
                .when(familyDomainService)
                .assertFamilyExists(familyProfileRepository, 999L);

        assertThrows(IllegalArgumentException.class, () -> qryExe.execute(qry));
        verifyNoInteractions(familyMemberRepository);
    }
}
