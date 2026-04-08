package io.yggdrasil.labs.mealmate.adapter.web.family;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.yggdrasil.labs.mealmate.adapter.web.family.convertor.FamilyMemberWebConvertor;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.AddFamilyMemberRequest;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.CreateFamilyRequest;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.FamilyMemberResponse;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.FamilyProfileResponse;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.UpdateFamilyMemberRequest;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.UpdateMemberPreferenceRequest;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.app.family.application.FamilyMemberAppService;
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

@ExtendWith(MockitoExtension.class)
class FamilyMemberControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @Mock private FamilyMemberAppService familyMemberAppService;
    @Mock private FamilyMemberWebConvertor familyMemberWebConvertor;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc =
                MockMvcBuilders.standaloneSetup(
                                new FamilyMemberController(
                                        familyMemberAppService, familyMemberWebConvertor))
                        .setValidator(validator)
                        .build();
    }

    @Test
    void shouldGetFamilyProfile() throws Exception {
        GetFamilyProfileQry qry = new GetFamilyProfileQry(1L);
        FamilyProfileCO profileCO = new FamilyProfileCO();
        profileCO.setFamilyName("Weekend Family");
        FamilyProfileResponse response = new FamilyProfileResponse();
        response.setFamilyName("Weekend Family");

        when(familyMemberWebConvertor.toGetFamilyProfileQry(1L)).thenReturn(qry);
        when(familyMemberAppService.getFamilyProfile(qry)).thenReturn(profileCO);
        when(familyMemberWebConvertor.toFamilyProfileResponse(profileCO)).thenReturn(response);

        MvcResult result =
                mockMvc.perform(get("/api/families/1")).andExpect(status().isOk()).andReturn();
        String familyName =
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .path("data")
                        .path("familyName")
                        .asText();
        assertEquals("Weekend Family", familyName);
        verify(familyMemberAppService).getFamilyProfile(qry);
    }

    @Test
    void shouldCreateFamily() throws Exception {
        CreateFamilyRequest request = new CreateFamilyRequest();
        request.setFamilyName("Weekend Family");
        CreateFamilyCmd cmd = new CreateFamilyCmd();
        cmd.setFamilyName("Weekend Family");
        FamilyProfileCO profileCO = new FamilyProfileCO();
        profileCO.setId(1L);
        profileCO.setFamilyName("Weekend Family");
        FamilyProfileResponse response = new FamilyProfileResponse();
        response.setId(1L);
        response.setFamilyName("Weekend Family");

        when(familyMemberWebConvertor.toCreateFamilyCmd(request)).thenReturn(cmd);
        when(familyMemberAppService.createFamily(cmd)).thenReturn(profileCO);
        when(familyMemberWebConvertor.toFamilyProfileResponse(profileCO)).thenReturn(response);

        MvcResult result =
                mockMvc.perform(
                                post("/api/families")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andReturn();

        String familyName =
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .path("data")
                        .path("familyName")
                        .asText();
        assertEquals("Weekend Family", familyName);
        verify(familyMemberAppService).createFamily(cmd);
    }

    @Test
    void shouldGetFamilyMembers() throws Exception {
        GetFamilyMemberListQry qry = new GetFamilyMemberListQry(1L);
        FamilyMemberCO memberCO = new FamilyMemberCO();
        memberCO.setId(11L);
        FamilyMemberResponse response = new FamilyMemberResponse();
        response.setId(11L);

        when(familyMemberWebConvertor.toGetFamilyMemberListQry(1L)).thenReturn(qry);
        when(familyMemberAppService.getFamilyMemberList(qry)).thenReturn(List.of(memberCO));
        when(familyMemberWebConvertor.toFamilyMemberResponseList(List.of(memberCO)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/families/1/members")).andExpect(status().isOk());
        verify(familyMemberAppService).getFamilyMemberList(qry);
    }

    @Test
    void shouldGetMemberDetail() throws Exception {
        GetFamilyMemberDetailQry qry = new GetFamilyMemberDetailQry(1L, 11L);
        FamilyMemberCO memberCO = new FamilyMemberCO();
        memberCO.setId(11L);
        FamilyMemberResponse response = new FamilyMemberResponse();
        response.setId(11L);

        when(familyMemberWebConvertor.toGetFamilyMemberDetailQry(1L, 11L)).thenReturn(qry);
        when(familyMemberAppService.getFamilyMemberDetail(qry)).thenReturn(memberCO);
        when(familyMemberWebConvertor.toFamilyMemberResponse(memberCO)).thenReturn(response);

        mockMvc.perform(get("/api/families/1/members/11")).andExpect(status().isOk());
        verify(familyMemberAppService).getFamilyMemberDetail(qry);
    }

    @Test
    void shouldAddMember() throws Exception {
        AddFamilyMemberRequest request = new AddFamilyMemberRequest();
        request.setName("Alice");
        request.setRoleType(MemberRoleType.ADULT);
        AddFamilyMemberCmd cmd = new AddFamilyMemberCmd();
        cmd.setFamilyId(1L);
        cmd.setName("Alice");

        when(familyMemberWebConvertor.toAddFamilyMemberCmd(1L, request)).thenReturn(cmd);

        mockMvc.perform(
                        post("/api/families/1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(familyMemberAppService).addMember(cmd);
    }

    @Test
    void shouldUpdateMember() throws Exception {
        UpdateFamilyMemberRequest request = new UpdateFamilyMemberRequest();
        request.setName("Alice");
        request.setRoleType(MemberRoleType.ADULT);
        UpdateFamilyMemberCmd cmd = new UpdateFamilyMemberCmd();
        cmd.setFamilyId(1L);
        cmd.setMemberId(11L);

        when(familyMemberWebConvertor.toUpdateFamilyMemberCmd(1L, 11L, request)).thenReturn(cmd);

        mockMvc.perform(
                        put("/api/families/1/members/11")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(familyMemberAppService).updateMember(cmd);
    }

    @Test
    void shouldUpdatePreference() throws Exception {
        UpdateMemberPreferenceRequest request = new UpdateMemberPreferenceRequest();
        request.setNutritionGoal(Map.of("protein", "high"));
        UpdateMemberPreferenceCmd cmd = new UpdateMemberPreferenceCmd();
        cmd.setFamilyId(1L);
        cmd.setMemberId(11L);

        when(familyMemberWebConvertor.toUpdateMemberPreferenceCmd(1L, 11L, request))
                .thenReturn(cmd);

        mockMvc.perform(
                        put("/api/families/1/members/11/preference")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(familyMemberAppService).updateMemberPreference(cmd);
    }

    @Test
    void shouldDeleteMember() throws Exception {
        RemoveFamilyMemberCmd cmd = new RemoveFamilyMemberCmd(1L, 11L);
        when(familyMemberWebConvertor.toRemoveFamilyMemberCmd(1L, 11L)).thenReturn(cmd);

        mockMvc.perform(delete("/api/families/1/members/11")).andExpect(status().isOk());

        verify(familyMemberAppService).removeMember(cmd);
    }

    @Test
    void shouldRejectInvalidAddMemberRequest() throws Exception {
        AddFamilyMemberRequest request = new AddFamilyMemberRequest();
        request.setRoleType(MemberRoleType.ADULT);

        mockMvc.perform(
                        post("/api/families/1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(familyMemberWebConvertor);
        verifyNoInteractions(familyMemberAppService);
    }

    @Test
    void shouldRejectInvalidCreateFamilyRequest() throws Exception {
        CreateFamilyRequest request = new CreateFamilyRequest();

        mockMvc.perform(
                        post("/api/families")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(familyMemberWebConvertor);
        verifyNoInteractions(familyMemberAppService);
    }
}
