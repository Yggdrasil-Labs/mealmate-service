package io.yggdrasil.labs.mealmate.start.openapi;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.baomidou.mybatisplus.autoconfigure.DdlAutoConfiguration;
import com.yggdrasil.labs.mybatis.config.MybatisPlusAutoConfiguration;

import io.yggdrasil.labs.mealmate.adapter.web.family.FamilyMemberController;
import io.yggdrasil.labs.mealmate.adapter.web.family.convertor.FamilyMemberWebConvertor;
import io.yggdrasil.labs.mealmate.app.family.application.FamilyMemberAppService;
import io.yggdrasil.labs.mealmate.start.config.OpenApiConfig;

@SpringBootTest(
        classes = OpenApiDocumentationIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationIntegrationTest {

    @Resource private MockMvc mockMvc;

    @Test
    void shouldExposeOpenApiDocumentationAtV3ApiDocs() throws Exception {
        MvcResult result =
                mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        String body = result.getResponse().getContentAsString();

        assertTrue(body.contains("/api/families/{familyId}"));
        assertTrue(body.contains("/api/families/{familyId}/members"));
        assertTrue(body.contains("Family"));
        assertTrue(body.contains("roleType"));
        assertTrue(body.contains("tasteTags"));
        assertTrue(body.contains("ADULT"));
        assertTrue(body.contains("ACTIVE"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(
            exclude = {
                DataSourceAutoConfiguration.class,
                FlywayAutoConfiguration.class,
                MybatisPlusAutoConfiguration.class,
                DdlAutoConfiguration.class
            })
    @Import({
        OpenApiConfig.class,
        FamilyMemberController.class,
        OpenApiDocumentationIntegrationTest.TestBeans.class
    })
    static class TestApplication {}

    @TestConfiguration
    static class TestBeans {

        @Bean
        FamilyMemberAppService familyMemberAppService() {
            return Mockito.mock(FamilyMemberAppService.class);
        }

        @Bean
        FamilyMemberWebConvertor familyMemberWebConvertor() {
            return Mockito.mock(FamilyMemberWebConvertor.class);
        }
    }
}
