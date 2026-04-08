package io.yggdrasil.labs.mealmate.app.family.dto.qry;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetFamilyMemberDetailQry {

    @NotNull private Long familyId;

    @NotNull private Long memberId;
}
