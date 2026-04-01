package io.yggdrasil.labs.mealmate.infrastructure.persistence.family.convertor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.OilLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SaltLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SpicyLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SweetLevel;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.MemberPreferenceDO;

@SpringJUnitConfig(classes = {FamilyInfraMapping.class, MemberPreferenceInfraConvertorImpl.class})
class MemberPreferenceInfraConvertorTest {

    @Autowired private MemberPreferenceInfraConvertor memberPreferenceInfraConvertor;

    @Test
    void shouldMapListFieldsToCommaSeparatedStringInDo() {
        MemberPreference entity = new MemberPreference();
        entity.setTasteTags(List.of("a", "b"));
        entity.setAvoidIngredients(List.of("x"));
        entity.setAllergyIngredients(List.of("y", "z"));

        MemberPreferenceDO dataObject = memberPreferenceInfraConvertor.toDo(entity);

        assertEquals("a,b", dataObject.getTasteTags());
        assertEquals("x", dataObject.getAvoidIngredients());
        assertEquals("y,z", dataObject.getAllergyIngredients());
    }

    @Test
    void shouldMapCommaSeparatedStringBackToListInEntity() {
        MemberPreferenceDO dataObject = new MemberPreferenceDO();
        dataObject.setTasteTags(" mild , hot ");
        dataObject.setAvoidIngredients("");
        dataObject.setAllergyIngredients(null);

        MemberPreference entity = memberPreferenceInfraConvertor.toEntity(dataObject);

        assertEquals(List.of("mild", "hot"), entity.getTasteTags());
        assertEquals(0, entity.getAvoidIngredients().size());
        assertEquals(0, entity.getAllergyIngredients().size());
    }

    @Test
    void shouldConvertJsonMapsRoundTrip() {
        Map<String, Object> nutrition = new HashMap<>();
        nutrition.put("kcal", 2000);
        Map<String, Object> extra = new HashMap<>();
        extra.put("note", "x");

        MemberPreference entity = new MemberPreference();
        entity.setNutritionGoal(nutrition);
        entity.setExtraRule(extra);
        entity.setSpicyLevel(SpicyLevel.MILD);
        entity.setSweetLevel(SweetLevel.LIGHT);
        entity.setOilLevel(OilLevel.MODERATE);
        entity.setSaltLevel(SaltLevel.MODERATE);

        MemberPreferenceDO dataObject = memberPreferenceInfraConvertor.toDo(entity);
        assertEquals(true, dataObject.getNutritionGoalJson().contains("2000"));
        assertEquals(true, dataObject.getExtraRuleJson().contains("note"));

        MemberPreference roundTrip = memberPreferenceInfraConvertor.toEntity(dataObject);
        assertEquals(2000, ((Number) roundTrip.getNutritionGoal().get("kcal")).intValue());
        assertEquals("x", roundTrip.getExtraRule().get("note"));
        assertEquals(SpicyLevel.MILD, roundTrip.getSpicyLevel());
    }
}
