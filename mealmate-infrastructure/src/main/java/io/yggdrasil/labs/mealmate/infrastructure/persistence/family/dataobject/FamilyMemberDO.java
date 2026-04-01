package io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yggdrasil.labs.mybatis.annotation.AutoMybatis;

import lombok.Data;

/** 家庭成员表 {@code family_member} 映射。枚举列以字符串存储，与领域枚举互转由 InfraConvertor 完成。 */
@Data
@TableName("family_member")
@AutoMybatis
public class FamilyMemberDO {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("family_id")
    private Long familyId;

    private String name;

    @TableField("role_type")
    private String roleType;

    private String gender;

    private LocalDate birthday;

    private String region;

    @TableField("target_type")
    private String targetType;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("sort_no")
    private Integer sortNo;

    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private String createdBy;

    @TableField("updated_by")
    private String updatedBy;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
