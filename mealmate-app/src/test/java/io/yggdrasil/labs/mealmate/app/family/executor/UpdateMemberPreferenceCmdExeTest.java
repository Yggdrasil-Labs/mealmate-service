package io.yggdrasil.labs.mealmate.app.family.executor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.family.convertor.FamilyMemberConvertor;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.UpdateMemberPreferenceCmd;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SpicyLevel;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyMemberRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;
import io.yggdrasil.labs.mealmate.domain.family.repo.MemberPreferenceRepository;
import io.yggdrasil.labs.mealmate.domain.family.service.FamilyDomainService;

@ExtendWith(MockitoExtension.class)
class UpdateMemberPreferenceCmdExeTest {

    @Mock private FamilyProfileRepository familyProfileRepository;
    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private MemberPreferenceRepository memberPreferenceRepository;
    @Mock private FamilyMemberConvertor familyMemberConvertor;

    @Test
    void shouldRejectInvalidBabySpicyPreference() {
        UpdateMemberPreferenceCmd cmd = new UpdateMemberPreferenceCmd();
        cmd.setFamilyId(1L);
        cmd.setMemberId(9L);
        cmd.setSpicyLevel(SpicyLevel.HOT);

        FamilyMember babyMember = new FamilyMember();
        babyMember.setId(9L);
        babyMember.setFamilyId(1L);
        babyMember.setRoleType(MemberRoleType.BABY);

        MemberPreference preference = new MemberPreference();
        preference.setSpicyLevel(SpicyLevel.HOT);

        when(familyProfileRepository.existsById(1L)).thenReturn(true);
        when(familyMemberRepository.findByIdAndFamilyId(9L, 1L))
                .thenReturn(Optional.of(babyMember));
        when(familyMemberConvertor.toMemberPreference(cmd)).thenReturn(preference);

        UpdateMemberPreferenceCmdExe cmdExe =
                new UpdateMemberPreferenceCmdExe(
                        familyProfileRepository,
                        familyMemberRepository,
                        memberPreferenceRepository,
                        new FamilyDomainService(),
                        familyMemberConvertor);

        assertThrows(IllegalArgumentException.class, () -> cmdExe.execute(cmd));
        verify(memberPreferenceRepository, never()).save(preference);
        verify(memberPreferenceRepository, never()).update(preference);
    }
}
