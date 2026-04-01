package io.yggdrasil.labs.mealmate.start;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(familyProfileCount).isEqualTo(1);
        assertThat(familyMemberCount).isEqualTo(1);
        assertThat(memberPreferenceCount).isEqualTo(1);
    }
}
