package io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

/** 供 Recipe MapStruct InfraConvertor 复用的字段转换（CSV 列表、JSON Map），无业务规则。 */
@Component
public class RecipeInfraMapping {

    @Named("commaToList")
    public List<String> commaToList(String value) {
        if (StrUtil.isBlank(value)) {
            return Collections.emptyList();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Named("listToComma")
    public String listToComma(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));
    }

    @Named("jsonToMap")
    public Map<String, Object> jsonToMap(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        String normalized = json.trim();
        if (normalized.startsWith("\"") && normalized.endsWith("\"")) {
            normalized = normalized.substring(1, normalized.length() - 1).replace("\\\"", "\"");
        }
        return JSONUtil.parseObj(normalized);
    }

    @Named("mapToJson")
    public String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return JSONUtil.toJsonStr(map);
    }
}
