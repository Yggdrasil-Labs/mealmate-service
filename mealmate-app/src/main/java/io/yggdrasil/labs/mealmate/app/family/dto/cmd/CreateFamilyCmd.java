package io.yggdrasil.labs.mealmate.app.family.dto.cmd;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFamilyCmd {

    @NotBlank private String familyName;
}
