package io.yggdrasil.labs.mealmate.app.family.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.family.assembler.FamilyMemberAssembler;
import io.yggdrasil.labs.mealmate.app.family.convertor.FamilyProfileConvertor;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.CreateFamilyCmd;
import io.yggdrasil.labs.mealmate.app.family.dto.co.FamilyProfileCO;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyProfile;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.FamilyStatus;
import io.yggdrasil.labs.mealmate.domain.family.repo.FamilyProfileRepository;

@ExtendWith(MockitoExtension.class)
class CreateFamilyCmdExeTest {

    @Mock private FamilyProfileRepository familyProfileRepository;
    @Mock private FamilyProfileConvertor familyProfileConvertor;
    @Mock private FamilyMemberAssembler familyMemberAssembler;

    @Test
    void shouldCreateFamilyWithDefaultStatusAndGeneratedCode() {
        CreateFamilyCmd cmd = new CreateFamilyCmd();
        cmd.setFamilyName("Weekend Family");

        FamilyProfile familyProfile = new FamilyProfile();
        familyProfile.setFamilyName("Weekend Family");

        FamilyProfile savedFamilyProfile = new FamilyProfile();
        savedFamilyProfile.setId(1L);
        savedFamilyProfile.setFamilyName("Weekend Family");
        savedFamilyProfile.setFamilyCode("FAM_TEST_CODE");
        savedFamilyProfile.setStatus(FamilyStatus.ENABLED);

        FamilyProfileCO familyProfileCO = new FamilyProfileCO();
        familyProfileCO.setId(1L);
        familyProfileCO.setFamilyName("Weekend Family");

        when(familyProfileConvertor.toFamilyProfile(cmd)).thenReturn(familyProfile);
        when(familyProfileRepository.save(familyProfile)).thenReturn(savedFamilyProfile);
        when(familyMemberAssembler.toFamilyProfileCO(savedFamilyProfile))
                .thenReturn(familyProfileCO);

        CreateFamilyCmdExe cmdExe =
                new CreateFamilyCmdExe(
                        familyProfileRepository, familyProfileConvertor, familyMemberAssembler);

        FamilyProfileCO result = cmdExe.execute(cmd);

        ArgumentCaptor<FamilyProfile> familyProfileCaptor =
                ArgumentCaptor.forClass(FamilyProfile.class);
        verify(familyProfileRepository).save(familyProfileCaptor.capture());
        FamilyProfile persistedFamilyProfile = familyProfileCaptor.getValue();
        assertEquals("Weekend Family", persistedFamilyProfile.getFamilyName());
        assertEquals(FamilyStatus.ENABLED, persistedFamilyProfile.getStatus());
        assertNotNull(persistedFamilyProfile.getFamilyCode());
        assertEquals(familyProfileCO, result);
    }
}
