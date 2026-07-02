package io.github.jeongdonghee.excelkit.grid;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import io.github.jeongdonghee.excelkit.core.ExcelKitException;

/**
 * DTO 타입에서 {@link ExcelColumn}이 붙은 필드/메서드를 찾아 컬럼 목록으로 해석한다(타입별 캐시).
 */
final class ColumnResolver {

    private static final Map<Class<?>, List<GridColumn>> CACHE = new ConcurrentHashMap<>();

    private ColumnResolver() {
    }

    static List<GridColumn> resolve(Class<?> type) {
        return CACHE.computeIfAbsent(type, ColumnResolver::build);
    }

    private static List<GridColumn> build(Class<?> type) {
        List<GridColumn> columns = new ArrayList<>();

        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                ExcelColumn ann = field.getAnnotation(ExcelColumn.class);
                if (ann == null) {
                    continue;
                }
                field.setAccessible(true);
                String header = ann.header().isBlank() ? field.getName() : ann.header();
                columns.add(new GridColumn(header, ann.order(), fieldGetter(field)));
            }
            for (Method method : c.getDeclaredMethods()) {
                ExcelColumn ann = method.getAnnotation(ExcelColumn.class);
                if (ann == null) {
                    continue;
                }
                if (method.getParameterCount() != 0) {
                    throw new ExcelKitException("@ExcelColumn method must have no parameters: " + method);
                }
                method.setAccessible(true);
                String header = ann.header().isBlank() ? method.getName() : ann.header();
                columns.add(new GridColumn(header, ann.order(), methodGetter(method)));
            }
        }

        if (columns.isEmpty()) {
            throw new ExcelKitException("no @ExcelColumn found on " + type.getName());
        }
        columns.sort(Comparator.comparingInt(GridColumn::order));
        return List.copyOf(columns);
    }

    private static Function<Object, Object> fieldGetter(Field field) {
        return target -> {
            try {
                return field.get(target);
            } catch (IllegalAccessException e) {
                throw new ExcelKitException("cannot read field: " + field.getName(), e);
            }
        };
    }

    private static Function<Object, Object> methodGetter(Method method) {
        return target -> {
            try {
                return method.invoke(target);
            } catch (ReflectiveOperationException e) {
                throw new ExcelKitException("cannot invoke method: " + method.getName(), e);
            }
        };
    }
}
