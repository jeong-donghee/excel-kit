package io.github.jeongdonghee.excelkit.grid.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * {@link ExcelDownloadReturnValueHandler}를 어댑터의 리턴값 핸들러 <b>맨 앞</b>에 끼운다.
 *
 * <p>{@code @RestController}(암묵적 {@code @ResponseBody})에서도 JSON 직렬화보다 먼저
 * {@code @ExcelDownload}가 잡히도록 하기 위함이다. (기본 등록은 뒤에 붙어 @ResponseBody에 밀림)
 */
class ExcelReturnValueHandlerRegistrar implements InitializingBean {

    private final RequestMappingHandlerAdapter adapter;
    private final ExcelDownloadReturnValueHandler handler;

    ExcelReturnValueHandlerRegistrar(RequestMappingHandlerAdapter adapter,
                                     ExcelDownloadReturnValueHandler handler) {
        this.adapter = adapter;
        this.handler = handler;
    }

    @Override
    public void afterPropertiesSet() {
        List<HandlerMethodReturnValueHandler> existing = adapter.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> updated = new ArrayList<>();
        updated.add(handler);
        if (existing != null) {
            updated.addAll(existing);
        }
        adapter.setReturnValueHandlers(updated);
    }
}
