package io.yggdrasil.labs.mealmate.adapter.web.family.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating a family profile.")
public class CreateFamilyRequest {

    @Schema(description = "Display name of the family.", example = "Weekend Family")
    @NotBlank
    private String familyName;
}
