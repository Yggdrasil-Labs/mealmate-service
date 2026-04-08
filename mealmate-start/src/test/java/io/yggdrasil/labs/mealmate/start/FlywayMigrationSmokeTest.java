package io.yggdrasil.labs.mealmate.start;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * 验证测试环境下 Flyway 能自动执行迁移，且 UC1 三张业务表已创建。
 *
 * <p>使用内存 H2（MySQL 兼容模式），与生产 MySQL 类型略有差异，仅作迁移与表存在性冒烟。
 */
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
class FlywayMigrationSmokeTest {

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void shouldApplyFlywayMigrationAndCreateFamilyTables() {
        Integer familyProfileCount =
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ="
                                + " 'family_profile'",
                        Integer.class);
        Integer familyMemberCount =
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ="
                                + " 'family_member'",
                        Integer.class);
        Integer memberPreferenceCount =
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ="
                                + " 'member_preference'",
                        Integer.class);
        Integer familyProfileDataCount =
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM family_profile", Integer.class);
        Integer familyMemberDataCount =
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM family_member", Integer.class);
        String familyProfileIdIdentity =
                jdbcTemplate.queryForObject(
                        "SELECT IS_IDENTITY FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME ="
                                + " 'family_profile' AND COLUMN_NAME = 'id'",
                        String.class);
        String familyMemberIdIdentity =
                jdbcTemplate.queryForObject(
                        "SELECT IS_IDENTITY FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME ="
                                + " 'family_member' AND COLUMN_NAME = 'id'",
                        String.class);
        String memberPreferenceIdIdentity =
                jdbcTemplate.queryForObject(
                        "SELECT IS_IDENTITY FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME ="
                                + " 'member_preference' AND COLUMN_NAME = 'id'",
                        String.class);

        assertEquals(1, familyProfileCount);
        assertEquals(1, familyMemberCount);
        assertEquals(1, memberPreferenceCount);
        assertEquals(1, familyProfileDataCount);
        assertEquals(3, familyMemberDataCount);
        assertEquals("YES", familyProfileIdIdentity);
        assertEquals("YES", familyMemberIdIdentity);
        assertEquals("YES", memberPreferenceIdIdentity);
    }
}
