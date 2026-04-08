package io.yggdrasil.labs.mealmate.app.family.application;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

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
import io.yggdrasil.labs.mealmate.app.family.executor.AddFamilyMemberCmdExe;
import io.yggdrasil.labs.mealmate.app.family.executor.CreateFamilyCmdExe;
import io.yggdrasil.labs.mealmate.app.family.executor.GetFamilyMemberDetailQryExe;
import io.yggdrasil.labs.mealmate.app.family.executor.GetFamilyMemberListQryExe;
import io.yggdrasil.labs.mealmate.app.family.executor.GetFamilyProfileQryExe;
import io.yggdrasil.labs.mealmate.app.family.executor.RemoveFamilyMemberCmdExe;
import io.yggdrasil.labs.mealmate.app.family.executor.UpdateFamilyMemberCmdExe;
import io.yggdrasil.labs.mealmate.app.family.executor.UpdateMemberPreferenceCmdExe;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class FamilyMemberAppService {

    private final CreateFamilyCmdExe createFamilyCmdExe;
    private final AddFamilyMemberCmdExe addFamilyMemberCmdExe;
    private final UpdateFamilyMemberCmdExe updateFamilyMemberCmdExe;
    private final UpdateMemberPreferenceCmdExe updateMemberPreferenceCmdExe;
    private final RemoveFamilyMemberCmdExe removeFamilyMemberCmdExe;
    private final GetFamilyProfileQryExe getFamilyProfileQryExe;
    private final GetFamilyMemberListQryExe getFamilyMemberListQryExe;
    private final GetFamilyMemberDetailQryExe getFamilyMemberDetailQryExe;

    public FamilyProfileCO createFamily(@Valid CreateFamilyCmd cmd) {
        return createFamilyCmdExe.execute(cmd);
    }

    public void addMember(@Valid AddFamilyMemberCmd cmd) {
        addFamilyMemberCmdExe.execute(cmd);
    }

    public void updateMember(@Valid UpdateFamilyMemberCmd cmd) {
        updateFamilyMemberCmdExe.execute(cmd);
    }

    public void updateMemberPreference(@Valid UpdateMemberPreferenceCmd cmd) {
        updateMemberPreferenceCmdExe.execute(cmd);
    }

    public void removeMember(@Valid RemoveFamilyMemberCmd cmd) {
        removeFamilyMemberCmdExe.execute(cmd);
    }

    public FamilyProfileCO getFamilyProfile(@Valid GetFamilyProfileQry qry) {
        return getFamilyProfileQryExe.execute(qry);
    }

    public List<FamilyMemberCO> getFamilyMemberList(@Valid GetFamilyMemberListQry qry) {
        return getFamilyMemberListQryExe.execute(qry);
    }

    public FamilyMemberCO getFamilyMemberDetail(@Valid GetFamilyMemberDetailQry qry) {
        return getFamilyMemberDetailQryExe.execute(qry);
    }
}
