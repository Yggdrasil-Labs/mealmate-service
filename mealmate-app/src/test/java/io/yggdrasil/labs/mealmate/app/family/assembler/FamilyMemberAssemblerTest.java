package io.yggdrasil.labs.mealmate.app.family.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.yggdrasil.labs.mealmate.app.family.dto.co.FamilyMemberCO;
import io.yggdrasil.labs.mealmate.app.family.dto.co.FamilyProfileCO;
import io.yggdrasil.labs.mealmate.app.family.dto.co.MemberPreferenceCO;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyProfile;
import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.FamilyStatus;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.GenderType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberStatus;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberTargetType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.OilLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SaltLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SpicyLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SweetLevel;

@SpringJUnitConfig(classes = {FamilyMemberAssemblerImpl.class})
class FamilyMemberAssemblerTest {

    @Autowired private FamilyMemberAssembler familyMemberAssembler;

    @Test
    void shouldMapMemberAndPreferenceToDetailCo() {
        FamilyMember member = new FamilyMember();
        member.setId(10L);
        member.setFamilyId(20L);
        member.setName("Alice");
        member.setRoleType(MemberRoleType.ADULT);
        member.setGender(GenderType.FEMALE);
        member.setBirthday(LocalDate.of(1995, 5, 1));
        member.setRegion("CN-SH");
        member.setTargetType(MemberTargetType.BALANCED);
        member.setAvatarUrl("https://example.com/avatar.png");
        member.setSortNo(1);
        member.setStatus(MemberStatus.ACTIVE);

        MemberPreference preference = new MemberPreference();
        preference.setMemberId(10L);
        preference.setTasteTags(List.of("mild", "home"));
        preference.setAvoidIngredients(List.of("cilantro"));
        preference.setAllergyIngredients(List.of("peanut"));
        preference.setSpicyLevel(SpicyLevel.MILD);
        preference.setSweetLevel(SweetLevel.LIGHT);
        preference.setOilLevel(OilLevel.MODERATE);
        preference.setSaltLevel(SaltLevel.MODERATE);
        preference.setNutritionGoal(Map.of("protein", "high"));
        preference.setExtraRule(Map.of("nightSnack", false));

        FamilyMemberCO detail = familyMemberAssembler.toDetailCO(member, preference);

        assertNotNull(detail);
        assertEquals(10L, detail.getId());
        assertEquals(20L, detail.getFamilyId());
        assertEquals("Alice", detail.getName());
        assertEquals(MemberRoleType.ADULT, detail.getRoleType());
        assertNotNull(detail.getPreference());

        MemberPreferenceCO preferenceCO = detail.getPreference();
        assertEquals(List.of("mild", "home"), preferenceCO.getTasteTags());
        assertEquals(List.of("cilantro"), preferenceCO.getAvoidIngredients());
        assertEquals(SpicyLevel.MILD, preferenceCO.getSpicyLevel());
        assertEquals("high", preferenceCO.getNutritionGoal().get("protein"));
    }

    @Test
    void shouldMapFamilyToProfileCo() {
        FamilyProfile familyProfile = new FamilyProfile();
        familyProfile.setId(1L);
        familyProfile.setFamilyName("Weekend Family");
        familyProfile.setFamilyCode("FAM001");
        familyProfile.setStatus(FamilyStatus.ENABLED);
        familyProfile.setRegion("CN-ZJ");
        familyProfile.setMealGoal(Map.of("style", "balanced"));
        familyProfile.setRemark("test");

        FamilyProfileCO profileCO = familyMemberAssembler.toFamilyProfileCO(familyProfile);

        assertNotNull(profileCO);
        assertEquals(1L, profileCO.getId());
        assertEquals("Weekend Family", profileCO.getFamilyName());
        assertEquals("FAM001", profileCO.getFamilyCode());
        assertEquals(FamilyStatus.ENABLED, profileCO.getStatus());
        assertEquals("balanced", profileCO.getMealGoal().get("style"));
    }
}
