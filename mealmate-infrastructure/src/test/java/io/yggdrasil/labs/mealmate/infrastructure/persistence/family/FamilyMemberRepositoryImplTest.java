package io.yggdrasil.labs.mealmate.infrastructure.persistence.family;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyProfile;
import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.FamilyStatus;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberStatus;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.convertor.FamilyMemberInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.convertor.FamilyProfileInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.convertor.MemberPreferenceInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.FamilyMemberDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.FamilyProfileDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.MemberPreferenceDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.service.FamilyMemberService;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.service.FamilyProfileService;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.service.MemberPreferenceService;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.impl.FamilyMemberRepositoryImpl;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.impl.FamilyProfileRepositoryImpl;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.impl.MemberPreferenceRepositoryImpl;

@ExtendWith(MockitoExtension.class)
class FamilyMemberRepositoryImplTest {

    @Mock private FamilyProfileInfraConvertor familyProfileInfraConvertor;
    @Mock private FamilyMemberInfraConvertor familyMemberInfraConvertor;
    @Mock private MemberPreferenceInfraConvertor memberPreferenceInfraConvertor;
    @Mock private FamilyProfileService familyProfileService;
    @Mock private FamilyMemberService familyMemberService;
    @Mock private MemberPreferenceService memberPreferenceService;

    @InjectMocks private FamilyProfileRepositoryImpl familyProfileRepository;

    @InjectMocks private FamilyMemberRepositoryImpl familyMemberRepository;

    @InjectMocks private MemberPreferenceRepositoryImpl memberPreferenceRepository;

    @Test
    void shouldFindFamilyById() {
        FamilyProfileDO dataObject = new FamilyProfileDO();
        dataObject.setId(1L);
        dataObject.setFamilyCode("FAM-001");
        dataObject.setStatus(FamilyStatus.ENABLED.name());
        when(familyProfileService.getById(1L)).thenReturn(dataObject);
        FamilyProfile familyProfile = new FamilyProfile();
        familyProfile.setId(1L);
        familyProfile.setFamilyCode("FAM-001");
        when(familyProfileInfraConvertor.toEntity(dataObject)).thenReturn(familyProfile);

        Optional<FamilyProfile> family = familyProfileRepository.findById(1L);

        assertTrue(family.isPresent());
        assertEquals(1L, family.get().getId());
        assertEquals("FAM-001", family.get().getFamilyCode());
    }

    @Test
    void shouldFindMembersByFamilyId() {
        FamilyMemberDO dataObject = new FamilyMemberDO();
        dataObject.setId(11L);
        dataObject.setFamilyId(1L);
        dataObject.setName("Alice");
        dataObject.setRoleType(MemberRoleType.ADULT.name());
        dataObject.setStatus(MemberStatus.ACTIVE.name());
        when(familyMemberService.list(anyFamilyMemberQuery())).thenReturn(List.of(dataObject));
        FamilyMember member = new FamilyMember();
        member.setId(11L);
        member.setFamilyId(1L);
        member.setName("Alice");
        when(familyMemberInfraConvertor.toEntity(dataObject)).thenReturn(member);

        List<FamilyMember> members = familyMemberRepository.findByFamilyId(1L);

        assertEquals(1, members.size());
        assertEquals(11L, members.get(0).getId());
        assertEquals("Alice", members.get(0).getName());
    }

    @Test
    void shouldFindMemberByIdAndFamilyId() {
        FamilyMemberDO dataObject = new FamilyMemberDO();
        dataObject.setId(12L);
        dataObject.setFamilyId(2L);
        dataObject.setRoleType(MemberRoleType.BABY.name());
        dataObject.setStatus(MemberStatus.ACTIVE.name());
        when(familyMemberService.getOne(anyFamilyMemberQuery())).thenReturn(dataObject);
        FamilyMember memberEntity = new FamilyMember();
        memberEntity.setId(12L);
        memberEntity.setFamilyId(2L);
        when(familyMemberInfraConvertor.toEntity(dataObject)).thenReturn(memberEntity);

        Optional<FamilyMember> member = familyMemberRepository.findByIdAndFamilyId(12L, 2L);

        assertTrue(member.isPresent());
        assertEquals(12L, member.get().getId());
        assertEquals(2L, member.get().getFamilyId());
    }

    @Test
    void shouldSaveNewMember() {
        FamilyMember member = new FamilyMember();
        member.setFamilyId(3L);
        member.setName("Bob");
        member.setRoleType(MemberRoleType.ADULT);
        member.setStatus(MemberStatus.ACTIVE);
        FamilyMemberDO mapped = new FamilyMemberDO();
        mapped.setFamilyId(3L);
        mapped.setName("Bob");
        mapped.setRoleType(MemberRoleType.ADULT.name());
        when(familyMemberInfraConvertor.toDo(member)).thenReturn(mapped);

        ArgumentCaptor<FamilyMemberDO> captor = ArgumentCaptor.forClass(FamilyMemberDO.class);

        familyMemberRepository.save(member);

        verify(familyMemberService).save(captor.capture());
        assertEquals(3L, captor.getValue().getFamilyId());
        assertEquals("Bob", captor.getValue().getName());
        assertEquals(MemberRoleType.ADULT.name(), captor.getValue().getRoleType());
    }

    @Test
    void shouldUpdatePreference() {
        MemberPreference preference = new MemberPreference();
        preference.setId(21L);
        preference.setMemberId(12L);
        preference.setTasteTags(List.of("light"));
        MemberPreferenceDO mapped = new MemberPreferenceDO();
        mapped.setId(21L);
        mapped.setMemberId(12L);
        mapped.setTasteTags("light");
        when(memberPreferenceInfraConvertor.toDo(preference)).thenReturn(mapped);

        memberPreferenceRepository.update(preference);

        ArgumentCaptor<MemberPreferenceDO> captor =
                ArgumentCaptor.forClass(MemberPreferenceDO.class);
        verify(memberPreferenceService).updateById(captor.capture());
        assertEquals(21L, captor.getValue().getId());
        assertEquals(12L, captor.getValue().getMemberId());
        assertEquals("light", captor.getValue().getTasteTags());
    }

    @Test
    void shouldLogicallyDeleteMember() {
        familyMemberRepository.logicalDeleteById(13L);

        verify(familyMemberService).removeById(13L);
    }

    @Test
    void shouldDeletePreferenceByMemberId() {
        memberPreferenceRepository.deleteByMemberId(13L);

        verify(memberPreferenceService).remove(anyMemberPreferenceQuery());
    }

    @Test
    void shouldReportFamilyExistsWhenRecordPresent() {
        when(familyProfileService.getById(1L)).thenReturn(new FamilyProfileDO());

        assertTrue(familyProfileRepository.existsById(1L));
    }

    @Test
    void shouldReturnEmptyWhenMemberMissing() {
        when(familyMemberService.getOne(anyFamilyMemberQuery())).thenReturn(null);

        Optional<FamilyMember> member = familyMemberRepository.findByIdAndFamilyId(99L, 1L);

        assertFalse(member.isPresent());
    }

    private LambdaQueryWrapper<FamilyMemberDO> anyFamilyMemberQuery() {
        return any();
    }

    private LambdaQueryWrapper<MemberPreferenceDO> anyMemberPreferenceQuery() {
        return any();
    }
}
