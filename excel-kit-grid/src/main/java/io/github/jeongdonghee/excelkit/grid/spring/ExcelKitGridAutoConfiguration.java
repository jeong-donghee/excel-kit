package io.github.jeongdonghee.excelkit.grid.spring;

import io.github.jeongdonghee.excelkit.grid.ExcelDataExtractor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * excel-kit-grid 자동설정 — {@code @ExcelDownload} 리턴값 핸들러를 등록한다.
 * Spring MVC(서블릿) 웹 애플리케이션에서만 활성화된다.
 */
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@ConditionalOnClass(RequestMappingHandlerAdapter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ExcelKitGridAutoConfiguration {

    @Bean
    public ExcelDownloadReturnValueHandler excelDownloadReturnValueHandler(
            ObjectProvider<ExcelDataExtractor> extractors) {
        return new ExcelDownloadReturnValueHandler(extractors.orderedStream().toList());
    }

    @Bean
    public ExcelReturnValueHandlerRegistrar excelReturnValueHandlerRegistrar(
            RequestMappingHandlerAdapter adapter, ExcelDownloadReturnValueHandler handler) {
        return new ExcelReturnValueHandlerRegistrar(adapter, handler);
    }
}
