package io.yggdrasil.labs.mealmate.app.family.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import io.yggdrasil.labs.mealmate.app.family.dto.cmd.AddFamilyMemberCmd;
import io.yggdrasil.labs.mealmate.app.family.executor.AddFamilyMemberCmdExe;
import io.yggdrasil.labs.mealmate.app.family.executor.CreateFamilyCmdExe;
import io.yggdrasil.labs.mealmate.app.family.executor.GetFamilyMemberDetailQryExe;
import io.yggdrasil.labs.mealmate.app.family.executor.GetFamilyMemberListQryExe;
import io.yggdrasil.labs.mealmate.app.family.executor.GetFamilyProfileQryExe;
import io.yggdrasil.labs.mealmate.app.family.executor.RemoveFamilyMemberCmdExe;
import io.yggdrasil.labs.mealmate.app.family.executor.UpdateFamilyMemberCmdExe;
import io.yggdrasil.labs.mealmate.app.family.executor.UpdateMemberPreferenceCmdExe;

@SpringJUnitConfig(classes = FamilyMemberAppServiceValidationTest.TestConfig.class)
class FamilyMemberAppServiceValidationTest {

    @Autowired private FamilyMemberAppService familyMemberAppService;
    @Autowired private AddFamilyMemberCmdExe addFamilyMemberCmdExe;

    @Test
    void shouldRejectInvalidAddMemberCmd() {
        AddFamilyMemberCmd cmd = new AddFamilyMemberCmd();
        cmd.setFamilyId(null);
        cmd.setName("");
        cmd.setRoleType(null);

        assertThrows(
                ConstraintViolationException.class, () -> familyMemberAppService.addMember(cmd));
        verifyNoInteractions(addFamilyMemberCmdExe);
    }

    @Configuration
    static class TestConfig {

        @Bean
        static MethodValidationPostProcessor methodValidationPostProcessor(
                LocalValidatorFactoryBean validator) {
            MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
            processor.setValidator(validator);
            return processor;
        }

        @Bean
        LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }

        @Bean
        CreateFamilyCmdExe createFamilyCmdExe() {
            return mock(CreateFamilyCmdExe.class);
        }

        @Bean
        AddFamilyMemberCmdExe addFamilyMemberCmdExe() {
            return mock(AddFamilyMemberCmdExe.class);
        }

        @Bean
        UpdateFamilyMemberCmdExe updateFamilyMemberCmdExe() {
            return mock(UpdateFamilyMemberCmdExe.class);
        }

        @Bean
        UpdateMemberPreferenceCmdExe updateMemberPreferenceCmdExe() {
            return mock(UpdateMemberPreferenceCmdExe.class);
        }

        @Bean
        RemoveFamilyMemberCmdExe removeFamilyMemberCmdExe() {
            return mock(RemoveFamilyMemberCmdExe.class);
        }

        @Bean
        GetFamilyProfileQryExe getFamilyProfileQryExe() {
            return mock(GetFamilyProfileQryExe.class);
        }

        @Bean
        GetFamilyMemberListQryExe getFamilyMemberListQryExe() {
            return mock(GetFamilyMemberListQryExe.class);
        }

        @Bean
        GetFamilyMemberDetailQryExe getFamilyMemberDetailQryExe() {
            return mock(GetFamilyMemberDetailQryExe.class);
        }

        @Bean
        FamilyMemberAppService familyMemberAppService(
                CreateFamilyCmdExe createFamilyCmdExe,
                AddFamilyMemberCmdExe addFamilyMemberCmdExe,
                UpdateFamilyMemberCmdExe updateFamilyMemberCmdExe,
                UpdateMemberPreferenceCmdExe updateMemberPreferenceCmdExe,
                RemoveFamilyMemberCmdExe removeFamilyMemberCmdExe,
                GetFamilyProfileQryExe getFamilyProfileQryExe,
                GetFamilyMemberListQryExe getFamilyMemberListQryExe,
                GetFamilyMemberDetailQryExe getFamilyMemberDetailQryExe) {
            return new FamilyMemberAppService(
                    createFamilyCmdExe,
                    addFamilyMemberCmdExe,
                    updateFamilyMemberCmdExe,
                    updateMemberPreferenceCmdExe,
                    removeFamilyMemberCmdExe,
                    getFamilyProfileQryExe,
                    getFamilyMemberListQryExe,
                    getFamilyMemberDetailQryExe);
        }
    }
}
