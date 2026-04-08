package io.yggdrasil.labs.mealmate.adapter.web.family.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.yggdrasil.labs.mealmate.adapter.web.family.dto.AddFamilyMemberRequest;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.CreateFamilyRequest;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.FamilyMemberResponse;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.FamilyProfileResponse;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.UpdateFamilyMemberRequest;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.UpdateMemberPreferenceRequest;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.AddFamilyMemberCmd;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.CreateFamilyCmd;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.RemoveFamilyMemberCmd;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.UpdateFamilyMemberCmd;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.UpdateMemberPreferenceCmd;
import io.yggdrasil.labs.mealmate.app.family.dto.co.FamilyMemberCO;
import io.yggdrasil.labs.mealmate.app.family.dto.co.FamilyProfileCO;
import io.yggdrasil.labs.mealmate.app.family.dto.qry.GetFamilyMemberDetailQry;
import io.yggdrasil.labs.mealmate.app.family.dto.qry.GetFamilyMemberListQry;
import io.yggdrasil.labs.mealmate.app.family.dto.qry.GetFamilyProfileQry;

@Mapper(componentModel = "spring")
public interface FamilyMemberWebConvertor {

    CreateFamilyCmd toCreateFamilyCmd(CreateFamilyRequest request);

    @Mapping(target = "familyId", source = "familyId")
    AddFamilyMemberCmd toAddFamilyMemberCmd(Long familyId, AddFamilyMemberRequest request);

    @Mapping(target = "familyId", source = "familyId")
    @Mapping(target = "memberId", source = "memberId")
    UpdateFamilyMemberCmd toUpdateFamilyMemberCmd(
            Long familyId, Long memberId, UpdateFamilyMemberRequest request);

    @Mapping(target = "familyId", source = "familyId")
    @Mapping(target = "memberId", source = "memberId")
    UpdateMemberPreferenceCmd toUpdateMemberPreferenceCmd(
            Long familyId, Long memberId, UpdateMemberPreferenceRequest request);

    default RemoveFamilyMemberCmd toRemoveFamilyMemberCmd(Long familyId, Long memberId) {
        return new RemoveFamilyMemberCmd(familyId, memberId);
    }

    default GetFamilyProfileQry toGetFamilyProfileQry(Long familyId) {
        return new GetFamilyProfileQry(familyId);
    }

    default GetFamilyMemberListQry toGetFamilyMemberListQry(Long familyId) {
        return new GetFamilyMemberListQry(familyId);
    }

    default GetFamilyMemberDetailQry toGetFamilyMemberDetailQry(Long familyId, Long memberId) {
        return new GetFamilyMemberDetailQry(familyId, memberId);
    }

    FamilyProfileResponse toFamilyProfileResponse(FamilyProfileCO profileCO);

    FamilyMemberResponse toFamilyMemberResponse(FamilyMemberCO familyMemberCO);

    List<FamilyMemberResponse> toFamilyMemberResponseList(List<FamilyMemberCO> familyMemberCOList);
}
