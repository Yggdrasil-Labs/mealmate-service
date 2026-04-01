package io.yggdrasil.labs.mealmate.domain.family.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.OilLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SaltLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SpicyLevel;

/** {@link FamilyDomainService} 的规则与规范化行为单测。 */
class FamilyDomainServiceTest {

    private final FamilyDomainService familyDomainService = new FamilyDomainService();

    @Test
    void shouldRejectBabySpicyPreference() {
        MemberPreference preference = new MemberPreference();
        preference.setSpicyLevel(SpicyLevel.HOT);
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        familyDomainService.validatePreferenceForMember(
                                MemberRoleType.BABY, preference));
    }

    @Test
    void shouldRejectBabySaltyPreference() {
        MemberPreference preference = new MemberPreference();
        preference.setSaltLevel(SaltLevel.SALTY);
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        familyDomainService.validatePreferenceForMember(
                                MemberRoleType.BABY, preference));
    }

    @Test
    void shouldRejectBabyRichOilPreference() {
        MemberPreference preference = new MemberPreference();
        preference.setOilLevel(OilLevel.RICH);
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        familyDomainService.validatePreferenceForMember(
                                MemberRoleType.BABY, preference));
    }

    @Test
    void shouldCleanupDuplicatesAndBlanksInPreferenceLists() {
        MemberPreference preference = new MemberPreference();
        preference.setTasteTags(Arrays.asList(" mild ", "", "mild", "  "));

        MemberPreference normalized = familyDomainService.normalizePreference(preference);

        org.junit.jupiter.api.Assertions.assertEquals(1, normalized.getTasteTags().size());
        org.junit.jupiter.api.Assertions.assertEquals("mild", normalized.getTasteTags().get(0));
    }

    @Test
    void shouldRejectWhenMemberFamilyIdMismatches() {
        FamilyMember member = new FamilyMember();
        member.setFamilyId(100L);

        assertThrows(
                IllegalArgumentException.class,
                () -> familyDomainService.assertMemberBelongsToFamily(member, 200L));
    }
}
