package io.github.jeongdonghee.excelkit.grid.spring;

import java.util.Collection;

import org.springframework.core.ResolvableType;

/**
 * 선언된 반환 타입의 제네릭 트리를 걸어 컬렉션의 원소 타입을 정적으로 찾는다.
 * {@code List<T>}, {@code ResponseEntity<List<T>>}, {@code ApiResponse<List<T>>} 등 모두 커버한다.
 */
final class ElementTypes {

    private ElementTypes() {
    }

    static Class<?> find(ResolvableType type) {
        if (type == null || type == ResolvableType.NONE) {
            return null;
        }
        Class<?> raw = type.resolve();
        if (raw != null && raw.isArray()) {
            return raw.getComponentType();
        }
        if (raw != null && Collection.class.isAssignableFrom(raw)) {
            return type.asCollection().getGeneric(0).resolve();
        }
        for (ResolvableType generic : type.getGenerics()) {
            Class<?> found = find(generic);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
