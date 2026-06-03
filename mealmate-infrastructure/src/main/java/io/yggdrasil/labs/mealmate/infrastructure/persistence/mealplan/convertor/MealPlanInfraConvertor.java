package io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItemHistory;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.ShoppingItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.AdjustReason;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealPlanCrowdType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanSource;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PrepPriority;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PrepTaskStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PushStatus;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.MealPlanItemDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.MealPlanItemHistoryDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.PrepPlanDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.PrepPlanItemDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.ShoppingItemDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.WeeklyMealPlanDO;

/** 周计划基础设施层对象转换器。 */
@Mapper(componentModel = "spring")
public interface MealPlanInfraConvertor {

    // --- WeeklyMealPlan ---

    @Mapping(target = "items", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "toPlanStatus")
    @Mapping(target = "planSource", source = "planSource", qualifiedByName = "toPlanSource")
    WeeklyMealPlan toEntity(WeeklyMealPlanDO d);

    @Mapping(target = "status", source = "status", qualifiedByName = "fromPlanStatus")
    @Mapping(target = "planSource", source = "planSource", qualifiedByName = "fromPlanSource")
    WeeklyMealPlanDO toPlanDo(WeeklyMealPlan e);

    // --- MealPlanItem ---

    @Mapping(target = "mealType", source = "mealType", qualifiedByName = "toMealType")
    @Mapping(target = "crowdType", source = "crowdType", qualifiedByName = "toCrowdType")
    @Mapping(target = "weightLoss", source = "isWeightLoss")
    @Mapping(target = "babyMeal", source = "isBabyMeal")
    @Mapping(target = "manuallyAdjusted", source = "isManuallyAdjusted")
    MealPlanItem toItemEntity(MealPlanItemDO d);

    List<MealPlanItem> toItemEntities(List<MealPlanItemDO> list);

    @Mapping(target = "mealType", source = "mealType", qualifiedByName = "fromMealType")
    @Mapping(target = "crowdType", source = "crowdType", qualifiedByName = "fromCrowdType")
    @Mapping(target = "isWeightLoss", source = "weightLoss")
    @Mapping(target = "isBabyMeal", source = "babyMeal")
    @Mapping(target = "isManuallyAdjusted", source = "manuallyAdjusted")
    MealPlanItemDO toItemDo(MealPlanItem e);

    List<MealPlanItemDO> toItemDos(List<MealPlanItem> list);

    // --- PrepPlan ---

    @Mapping(target = "items", ignore = true)
    @Mapping(target = "pushStatus", source = "pushStatus", qualifiedByName = "toPushStatus")
    PrepPlan toPrepEntity(PrepPlanDO d);

    @Mapping(target = "pushStatus", source = "pushStatus", qualifiedByName = "fromPushStatus")
    PrepPlanDO toPrepDo(PrepPlan e);

    // --- PrepPlanItem ---

    @Mapping(target = "priority", source = "priority", qualifiedByName = "toPrepPriority")
    @Mapping(target = "taskStatus", source = "taskStatus", qualifiedByName = "toPrepTaskStatus")
    PrepPlanItem toPrepItemEntity(PrepPlanItemDO d);

    List<PrepPlanItem> toPrepItemEntities(List<PrepPlanItemDO> list);

    @Mapping(target = "priority", source = "priority", qualifiedByName = "fromPrepPriority")
    @Mapping(target = "taskStatus", source = "taskStatus", qualifiedByName = "fromPrepTaskStatus")
    PrepPlanItemDO toPrepItemDo(PrepPlanItem e);

    List<PrepPlanItemDO> toPrepItemDos(List<PrepPlanItem> list);

    // --- ShoppingItem ---

    @Mapping(target = "purchased", source = "purchasedFlag")
    ShoppingItem toShoppingEntity(ShoppingItemDO d);

    List<ShoppingItem> toShoppingEntities(List<ShoppingItemDO> list);

    @Mapping(target = "purchasedFlag", source = "purchased")
    ShoppingItemDO toShoppingDo(ShoppingItem e);

    List<ShoppingItemDO> toShoppingDos(List<ShoppingItem> list);

    // --- Named enum converters ---

    @Named("toPlanStatus")
    default PlanStatus toPlanStatus(String v) {
        return v == null ? null : PlanStatus.valueOf(v);
    }

    @Named("fromPlanStatus")
    default String fromPlanStatus(PlanStatus v) {
        return v == null ? null : v.name();
    }

    @Named("toPlanSource")
    default PlanSource toPlanSource(String v) {
        return v == null ? null : PlanSource.valueOf(v);
    }

    @Named("fromPlanSource")
    default String fromPlanSource(PlanSource v) {
        return v == null ? null : v.name();
    }

    @Named("toMealType")
    default MealType toMealType(String v) {
        return v == null ? null : MealType.valueOf(v);
    }

    @Named("fromMealType")
    default String fromMealType(MealType v) {
        return v == null ? null : v.name();
    }

    @Named("toCrowdType")
    default MealPlanCrowdType toCrowdType(String v) {
        return v == null ? null : MealPlanCrowdType.valueOf(v);
    }

    @Named("fromCrowdType")
    default String fromCrowdType(MealPlanCrowdType v) {
        return v == null ? null : v.name();
    }

    @Named("toPushStatus")
    default PushStatus toPushStatus(String v) {
        return v == null ? null : PushStatus.valueOf(v);
    }

    @Named("fromPushStatus")
    default String fromPushStatus(PushStatus v) {
        return v == null ? null : v.name();
    }

    @Named("toPrepPriority")
    default PrepPriority toPrepPriority(String v) {
        return v == null ? null : PrepPriority.valueOf(v);
    }

    @Named("fromPrepPriority")
    default String fromPrepPriority(PrepPriority v) {
        return v == null ? null : v.name();
    }

    @Named("toPrepTaskStatus")
    default PrepTaskStatus toPrepTaskStatus(String v) {
        return v == null ? null : PrepTaskStatus.valueOf(v);
    }

    @Named("fromPrepTaskStatus")
    default String fromPrepTaskStatus(PrepTaskStatus v) {
        return v == null ? null : v.name();
    }

    // --- MealPlanItemHistory ---

    @Mapping(target = "adjustReason", source = "adjustReason", qualifiedByName = "fromAdjustReason")
    MealPlanItemHistoryDO toHistoryDo(MealPlanItemHistory history);

    @Mapping(target = "adjustReason", source = "adjustReason", qualifiedByName = "toAdjustReason")
    MealPlanItemHistory toHistoryEntity(MealPlanItemHistoryDO d);

    List<MealPlanItemHistory> toHistoryEntities(List<MealPlanItemHistoryDO> list);

    @Named("toAdjustReason")
    default AdjustReason toAdjustReason(String v) {
        return v == null ? null : AdjustReason.valueOf(v);
    }

    @Named("fromAdjustReason")
    default String fromAdjustReason(AdjustReason v) {
        return v == null ? null : v.name();
    }
}
