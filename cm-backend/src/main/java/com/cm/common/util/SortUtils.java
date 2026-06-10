package com.cm.common.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.util.Map;

public final class SortUtils {

    private SortUtils() {}

    public static <T> void applyMultiSort(LambdaQueryWrapper<T> wrapper,
                                          String sortFields,
                                          String sortOrders,
                                          Map<String, SFunction<T, ?>> fieldMapper,
                                          SFunction<T, ?> defaultField,
                                          boolean defaultAsc) {
        boolean applied = false;
        if (StringUtils.hasText(sortFields) && StringUtils.hasText(sortOrders)) {
            String[] fields = sortFields.split(",");
            String[] orders = sortOrders.split(",");
            for (int i = 0; i < fields.length && i < orders.length; i++) {
                String field = fields[i].trim();
                SFunction<T, ?> getter = fieldMapper.get(field);
                if (getter == null) {
                    continue;
                }
                boolean asc = !"descending".equalsIgnoreCase(orders[i].trim());
                wrapper.orderBy(true, asc, getter);
                applied = true;
            }
        }
        if (!applied && defaultField != null) {
            wrapper.orderBy(true, defaultAsc, defaultField);
        }
    }
}