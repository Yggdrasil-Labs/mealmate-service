package io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yggdrasil.labs.mybatis.annotation.AutoMybatis;

import lombok.Data;

/** 菜谱主表 {@code recipe} 映射。枚举列以字符串存储，与领域枚举互转由 InfraConvertor 完成。 */
@Data
@TableName("recipe")
@AutoMybatis
public class RecipeDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField("recipe_type")
    private String recipeType;

    @TableField("source_type")
    private String sourceType;

    @TableField("season_tag")
    private String seasonTag;

    @TableField("crowd_tag")
    private String crowdTag;

    @TableField("taste_tag")
    private String tasteTag;

    @TableField("difficulty_level")
    private String difficultyLevel;

    @TableField("cooking_time_min")
    private Integer cookingTimeMin;

    @TableField("cover_image_url")
    private String coverImageUrl;

    @TableField("is_baby_friendly")
    private Boolean babyFriendly;

    @TableField("is_weight_loss_friendly")
    private Boolean weightLossFriendly;

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
    private Long deleted;
}
