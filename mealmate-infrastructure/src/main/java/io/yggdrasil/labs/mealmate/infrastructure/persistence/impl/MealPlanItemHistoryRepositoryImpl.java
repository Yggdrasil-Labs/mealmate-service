package io.yggdrasil.labs.mealmate.infrastructure.persistence.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItemHistory;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.MealPlanItemHistoryRepository;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.convertor.MealPlanInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.MealPlanItemHistoryDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.mealplan.dataobject.service.MealPlanItemHistoryService;
import lombok.RequiredArgsConstructor;

/** 餐计划条目调整历史仓储实现。 */
@Repository
@RequiredArgsConstructor
public class MealPlanItemHistoryRepositoryImpl implements MealPlanItemHistoryRepository {

    private final MealPlanInfraConvertor convertor;
    private final MealPlanItemHistoryService historyService;

    @Override
    public void save(MealPlanItemHistory history) {
        MealPlanItemHistoryDO dObj = convertor.toHistoryDo(history);
        historyService.save(dObj);
    }

    @Override
    public List<MealPlanItemHistory> findByItemId(Long itemId) {
        List<MealPlanItemHistoryDO> dos =
                historyService.list(
                        new LambdaQueryWrapper<MealPlanItemHistoryDO>()
                                .eq(MealPlanItemHistoryDO::getItemId, itemId)
                                .orderByDesc(MealPlanItemHistoryDO::getAdjustedAt));
        return convertor.toHistoryEntities(dos);
    }
}
