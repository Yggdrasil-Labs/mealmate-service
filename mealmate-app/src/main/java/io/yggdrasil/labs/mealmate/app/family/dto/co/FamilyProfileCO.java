package io.yggdrasil.labs.mealmate.app.family.dto.co;

import java.util.Map;

import io.yggdrasil.labs.mealmate.domain.family.model.enums.FamilyStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyProfileCO {

    private Long id;
    private String familyName;
    private String familyCode;
    private FamilyStatus status;
    private String region;
    private Map<String, Object> mealGoal;
    private String remark;
}
