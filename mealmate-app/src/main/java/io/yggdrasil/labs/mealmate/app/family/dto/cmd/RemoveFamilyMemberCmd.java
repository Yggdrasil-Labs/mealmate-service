package io.yggdrasil.labs.mealmate.app.family.dto.cmd;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoveFamilyMemberCmd {

    @NotNull private Long familyId;

    @NotNull private Long memberId;
}
