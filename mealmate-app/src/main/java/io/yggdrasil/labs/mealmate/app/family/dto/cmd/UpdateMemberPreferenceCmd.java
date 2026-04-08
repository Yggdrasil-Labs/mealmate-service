package io.yggdrasil.labs.mealmate.app.family.dto.cmd;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotNull;

import io.yggdrasil.labs.mealmate.domain.family.model.enums.OilLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SaltLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SpicyLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SweetLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberPreferenceCmd {

    @NotNull private Long familyId;

    @NotNull private Long memberId;

    private List<String> tasteTags;
    private List<String> avoidIngredients;
    private List<String> allergyIngredients;
    private SpicyLevel spicyLevel;
    private SweetLevel sweetLevel;
    private OilLevel oilLevel;
    private SaltLevel saltLevel;
    private Map<String, Object> nutritionGoal;
    private Map<String, Object> extraRule;
}
