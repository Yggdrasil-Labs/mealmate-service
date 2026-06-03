package io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yggdrasil.labs.mybatis.annotation.AutoMybatis;

import lombok.Data;

/** 采购清单条目表 {@code shopping_item} 映射。物理删除，无 @TableLogic。 */
@Data
@TableName("shopping_item")
@AutoMybatis
public class ShoppingItemDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("plan_id")
    private Long planId;

    @TableField("ingredient_name")
    private String ingredientName;

    @TableField("total_quantity")
    private BigDecimal totalQuantity;

    private String unit;

    @TableField("purchased_flag")
    private Boolean purchasedFlag;

    @TableField("sort_no")
    private Integer sortNo;

    private String remark;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;
}
