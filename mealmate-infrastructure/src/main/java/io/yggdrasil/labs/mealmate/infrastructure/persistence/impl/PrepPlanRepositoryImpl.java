package io.yggdrasil.labs.mealmate.infrastructure.persistence.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PrepTaskStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.PrepPlanRepository;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.convertor.MealPlanInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.PrepPlanDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.PrepPlanItemDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.service.PrepPlanItemService;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.service.PrepPlanService;
import lombok.RequiredArgsConstructor;

/** 备菜计划仓储实现。 */
@Repository
@RequiredArgsConstructor
public class PrepPlanRepositoryImpl implements PrepPlanRepository {

    private final MealPlanInfraConvertor convertor;
    private final PrepPlanService prepPlanService;
    private final PrepPlanItemService prepPlanItemService;

    @Override
    public PrepPlan save(PrepPlan prepPlan) {
        PrepPlanDO planDo = convertor.toPrepDo(prepPlan);
        prepPlanService.save(planDo);
        Long prepPlanId = planDo.getId();

        if (prepPlan.getItems() != null && !prepPlan.getItems().isEmpty()) {
            prepPlan.getItems().forEach(item -> item.setPrepPlanId(prepPlanId));
            List<PrepPlanItemDO> itemDos = convertor.toPrepItemDos(prepPlan.getItems());
            prepPlanItemService.saveBatch(itemDos);
        }

        PrepPlan saved = convertor.toPrepEntity(planDo);
        saved.setItems(prepPlan.getItems());
        return saved;
    }

    @Override
    public Optional<PrepPlan> findByPlanId(Long planId) {
        if (planId == null) {
            return Optional.empty();
        }
        LambdaQueryWrapper<PrepPlanDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrepPlanDO::getPlanId, planId);
        PrepPlanDO planDo = prepPlanService.getOne(wrapper);
        if (planDo == null) {
            return Optional.empty();
        }
        PrepPlan plan = convertor.toPrepEntity(planDo);
        plan.setItems(findItemsByPrepPlanId(planDo.getId()));
        return Optional.of(plan);
    }

    @Override
    public void updateItemStatus(Long itemId, PrepTaskStatus status) {
        if (itemId == null || status == null) {
            return;
        }
        UpdateWrapper<PrepPlanItemDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", itemId).set("task_status", status.name());
        prepPlanItemService.update(wrapper);
    }

    private List<PrepPlanItem> findItemsByPrepPlanId(Long prepPlanId) {
        LambdaQueryWrapper<PrepPlanItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrepPlanItemDO::getPrepPlanId, prepPlanId);
        List<PrepPlanItemDO> list = prepPlanItemService.list(wrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return convertor.toPrepItemEntities(list);
    }
}
