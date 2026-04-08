package io.yggdrasil.labs.mealmate.adapter.web.family;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.yggdrasil.labs.mealmate.adapter.web.family.convertor.FamilyMemberWebConvertor;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.AddFamilyMemberRequest;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.CreateFamilyRequest;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.FamilyMemberResponse;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.FamilyProfileResponse;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.UpdateFamilyMemberRequest;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.UpdateMemberPreferenceRequest;
import io.yggdrasil.labs.mealmate.app.family.application.FamilyMemberAppService;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/families")
@Tag(name = "Family", description = "Family profile and family member management APIs.")
public class FamilyMemberController {

    private final FamilyMemberAppService familyMemberAppService;
    private final FamilyMemberWebConvertor familyMemberWebConvertor;

    @PostMapping
    @Operation(
            summary = "Create family profile",
            description = "Creates a new family profile with server-generated family code.")
    public SingleResponse<FamilyProfileResponse> createFamily(
            @Valid @RequestBody CreateFamilyRequest request) {
        SingleResponse<FamilyProfileResponse> response =
                SingleResponse.<FamilyProfileResponse>buildSuccess();
        response.setData(
                familyMemberWebConvertor.toFamilyProfileResponse(
                        familyMemberAppService.createFamily(
                                familyMemberWebConvertor.toCreateFamilyCmd(request))));
        return response;
    }

    @GetMapping("/{familyId}")
    @Operation(
            summary = "Get family profile",
            description = "Returns the profile summary of the specified family.")
    public SingleResponse<FamilyProfileResponse> getFamilyProfile(
            @Parameter(description = "Unique identifier of the family.") @PathVariable("familyId")
                    Long familyId) {
        SingleResponse<FamilyProfileResponse> response =
                SingleResponse.<FamilyProfileResponse>buildSuccess();
        response.setData(
                familyMemberWebConvertor.toFamilyProfileResponse(
                        familyMemberAppService.getFamilyProfile(
                                familyMemberWebConvertor.toGetFamilyProfileQry(familyId))));
        return response;
    }

    @GetMapping("/{familyId}/members")
    @Operation(
            summary = "List family members",
            description = "Returns all members that belong to the specified family.")
    public MultiResponse<FamilyMemberResponse> getFamilyMembers(
            @Parameter(description = "Unique identifier of the family.") @PathVariable("familyId")
                    Long familyId) {
        MultiResponse<FamilyMemberResponse> response =
                MultiResponse.<FamilyMemberResponse>buildSuccess();
        response.setData(
                familyMemberWebConvertor.toFamilyMemberResponseList(
                        familyMemberAppService.getFamilyMemberList(
                                familyMemberWebConvertor.toGetFamilyMemberListQry(familyId))));
        return response;
    }

    @GetMapping("/{familyId}/members/{memberId}")
    @Operation(
            summary = "Get member detail",
            description = "Returns the detail of a single member in the specified family.")
    public SingleResponse<FamilyMemberResponse> getFamilyMemberDetail(
            @Parameter(description = "Unique identifier of the family.") @PathVariable("familyId")
                    Long familyId,
            @Parameter(description = "Unique identifier of the family member.")
                    @PathVariable("memberId")
                    Long memberId) {
        SingleResponse<FamilyMemberResponse> response =
                SingleResponse.<FamilyMemberResponse>buildSuccess();
        response.setData(
                familyMemberWebConvertor.toFamilyMemberResponse(
                        familyMemberAppService.getFamilyMemberDetail(
                                familyMemberWebConvertor.toGetFamilyMemberDetailQry(
                                        familyId, memberId))));
        return response;
    }

    @PostMapping("/{familyId}/members")
    @Operation(
            summary = "Add family member",
            description = "Creates a new member under the specified family.")
    public Response addMember(
            @Parameter(description = "Unique identifier of the family.") @PathVariable("familyId")
                    Long familyId,
            @Valid @RequestBody AddFamilyMemberRequest request) {
        familyMemberAppService.addMember(
                familyMemberWebConvertor.toAddFamilyMemberCmd(familyId, request));
        return Response.buildSuccess();
    }

    @PutMapping("/{familyId}/members/{memberId}")
    @Operation(
            summary = "Update family member",
            description = "Updates the basic profile of an existing family member.")
    public Response updateMember(
            @Parameter(description = "Unique identifier of the family.") @PathVariable("familyId")
                    Long familyId,
            @Parameter(description = "Unique identifier of the family member.")
                    @PathVariable("memberId")
                    Long memberId,
            @Valid @RequestBody UpdateFamilyMemberRequest request) {
        familyMemberAppService.updateMember(
                familyMemberWebConvertor.toUpdateFamilyMemberCmd(familyId, memberId, request));
        return Response.buildSuccess();
    }

    @PutMapping("/{familyId}/members/{memberId}/preference")
    @Operation(
            summary = "Update member preference",
            description = "Updates taste preference, dietary restrictions, and nutrition goals.")
    public Response updateMemberPreference(
            @Parameter(description = "Unique identifier of the family.") @PathVariable("familyId")
                    Long familyId,
            @Parameter(description = "Unique identifier of the family member.")
                    @PathVariable("memberId")
                    Long memberId,
            @Valid @RequestBody UpdateMemberPreferenceRequest request) {
        familyMemberAppService.updateMemberPreference(
                familyMemberWebConvertor.toUpdateMemberPreferenceCmd(familyId, memberId, request));
        return Response.buildSuccess();
    }

    @DeleteMapping("/{familyId}/members/{memberId}")
    @Operation(
            summary = "Remove family member",
            description = "Removes a member from the specified family.")
    public Response removeMember(
            @Parameter(description = "Unique identifier of the family.") @PathVariable("familyId")
                    Long familyId,
            @Parameter(description = "Unique identifier of the family member.")
                    @PathVariable("memberId")
                    Long memberId) {
        familyMemberAppService.removeMember(
                familyMemberWebConvertor.toRemoveFamilyMemberCmd(familyId, memberId));
        return Response.buildSuccess();
    }
}
