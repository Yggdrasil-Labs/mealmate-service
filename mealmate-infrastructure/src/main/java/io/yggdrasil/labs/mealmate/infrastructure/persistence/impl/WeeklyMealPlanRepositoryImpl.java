package io.yggdrasil.labs.mealmate.infrastructure.persistence.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.ShoppingItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.convertor.MealPlanInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.MealPlanItemDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.ShoppingItemDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.WeeklyMealPlanDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.service.MealPlanItemService;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.service.ShoppingItemService;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.service.WeeklyMealPlanService;
import lombok.RequiredArgsConstructor;

/** 周计划仓储实现。 */
@Repository
@RequiredArgsConstructor
public class WeeklyMealPlanRepositoryImpl implements WeeklyMealPlanRepository {

    private final MealPlanInfraConvertor convertor;
    private final WeeklyMealPlanService weeklyMealPlanService;
    private final MealPlanItemService mealPlanItemService;
    private final ShoppingItemService shoppingItemService;

    @Override
    public WeeklyMealPlan save(WeeklyMealPlan plan) {
        WeeklyMealPlanDO planDo = convertor.toPlanDo(plan);
        weeklyMealPlanService.save(planDo);
        Long planId = planDo.getId();

        if (plan.getItems() != null && !plan.getItems().isEmpty()) {
            plan.getItems().forEach(item -> item.setPlanId(planId));
            List<MealPlanItemDO> itemDos = convertor.toItemDos(plan.getItems());
            mealPlanItemService.saveBatch(itemDos);
        }

        WeeklyMealPlan saved = convertor.toEntity(planDo);
        saved.setItems(plan.getItems());
        return saved;
    }

    @Override
    public Optional<WeeklyMealPlan> findById(Long planId) {
        if (planId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(weeklyMealPlanService.getById(planId)).map(convertor::toEntity);
    }

    @Override
    public Optional<WeeklyMealPlan> findByIdWithItems(Long planId) {
        if (planId == null) {
            return Optional.empty();
        }
        WeeklyMealPlanDO planDo = weeklyMealPlanService.getById(planId);
        if (planDo == null) {
            return Optional.empty();
        }
        WeeklyMealPlan plan = convertor.toEntity(planDo);
        plan.setItems(findItemsByPlanId(planId));
        return Optional.of(plan);
    }

    @Override
    public Optional<WeeklyMealPlan> findByFamilyIdAndWeekStartDate(
            Long familyId, LocalDate weekStartDate) {
        LambdaQueryWrapper<WeeklyMealPlanDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyMealPlanDO::getFamilyId, familyId)
                .eq(WeeklyMealPlanDO::getWeekStartDate, weekStartDate);
        WeeklyMealPlanDO planDo = weeklyMealPlanService.getOne(wrapper);
        if (planDo == null) {
            return Optional.empty();
        }
        WeeklyMealPlan plan = convertor.toEntity(planDo);
        plan.setItems(findItemsByPlanId(planDo.getId()));
        return Optional.of(plan);
    }

    @Override
    public Optional<WeeklyMealPlan> findByFamilyIdAndWeekStartDateForUpdate(
            Long familyId, LocalDate weekStartDate) {
        LambdaQueryWrapper<WeeklyMealPlanDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyMealPlanDO::getFamilyId, familyId)
                .eq(WeeklyMealPlanDO::getWeekStartDate, weekStartDate)
                .last("FOR UPDATE");
        return Optional.ofNullable(weeklyMealPlanService.getOne(wrapper)).map(convertor::toEntity);
    }

    @Override
    public void updateStatus(Long planId, PlanStatus status) {
        if (planId == null || status == null) {
            return;
        }
        UpdateWrapper<WeeklyMealPlanDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", planId).set("status", status.name());
        weeklyMealPlanService.update(wrapper);
    }

    @Override
    public void logicalDelete(Long planId) {
        if (planId == null) {
            return;
        }
        // @TableLogic 将 deleted 设为 1，唯一索引 uk_family_week 含 deleted 列，可释放槽位
        weeklyMealPlanService.removeById(planId);
    }

    @Override
    public void deleteItemsByPlanId(Long planId) {
        if (planId == null) {
            return;
        }
        LambdaQueryWrapper<MealPlanItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MealPlanItemDO::getPlanId, planId);
        mealPlanItemService.remove(wrapper);
    }

    @Override
    public MealPlanItem saveItem(MealPlanItem item) {
        MealPlanItemDO itemDo = convertor.toItemDo(item);
        if (itemDo.getId() != null) {
            mealPlanItemService.updateById(itemDo);
        } else {
            mealPlanItemService.save(itemDo);
        }
        return convertor.toItemEntity(itemDo);
    }

    @Override
    public void deleteItem(Long itemId) {
        if (itemId == null) {
            return;
        }
        mealPlanItemService.removeById(itemId);
    }

    @Override
    public Optional<MealPlanItem> findItemById(Long itemId) {
        if (itemId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mealPlanItemService.getById(itemId))
                .map(convertor::toItemEntity);
    }

    @Override
    public List<ShoppingItem> findShoppingItemsByPlanId(Long planId) {
        if (planId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ShoppingItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingItemDO::getPlanId, planId)
                .orderByAsc(ShoppingItemDO::getSortNo, ShoppingItemDO::getId);
        List<ShoppingItemDO> list = shoppingItemService.list(wrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return convertor.toShoppingEntities(list);
    }

    @Override
    public void saveShoppingItems(Long planId, List<ShoppingItem> items) {
        if (planId == null || items == null || items.isEmpty()) {
            return;
        }
        items.forEach(i -> i.setPlanId(planId));
        List<ShoppingItemDO> dos = convertor.toShoppingDos(items);
        shoppingItemService.saveBatch(dos);
    }

    @Override
    public void updateShoppingItemPurchased(Long itemId, boolean purchased) {
        if (itemId == null) {
            return;
        }
        UpdateWrapper<ShoppingItemDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", itemId).set("purchased_flag", purchased);
        shoppingItemService.update(wrapper);
    }

    private List<MealPlanItem> findItemsByPlanId(Long planId) {
        LambdaQueryWrapper<MealPlanItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MealPlanItemDO::getPlanId, planId)
                .orderByAsc(MealPlanItemDO::getMealDate, MealPlanItemDO::getSortOrder);
        List<MealPlanItemDO> list = mealPlanItemService.list(wrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return convertor.toItemEntities(list);
    }
}
