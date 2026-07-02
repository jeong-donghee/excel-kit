package io.github.jeongdonghee.excelkit.grid.spring;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.github.jeongdonghee.excelkit.core.ExcelKitException;
import io.github.jeongdonghee.excelkit.grid.ExcelDataExtractor;
import io.github.jeongdonghee.excelkit.grid.ExcelDownload;
import io.github.jeongdonghee.excelkit.grid.ExcelDownloadOptions;
import io.github.jeongdonghee.excelkit.grid.GridExcelWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link ExcelDownload}가 붙은 컨트롤러 메서드의 반환 값을 가로채 .xlsx 다운로드로 내보내는 핸들러.
 */
public class ExcelDownloadReturnValueHandler implements HandlerMethodReturnValueHandler {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final List<ExcelDataExtractor> extractors;

    public ExcelDownloadReturnValueHandler(List<ExcelDataExtractor> extractors) {
        this.extractors = List.copyOf(extractors);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.hasMethodAnnotation(ExcelDownload.class);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);
        ExcelDownload annotation = returnType.getMethodAnnotation(ExcelDownload.class);

        Object value = unwrap(returnValue);
        Collection<?> rows = toCollection(value);
        Class<?> elementType = resolveElementType(returnType, annotation, rows);

        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        if (response == null) {
            throw new ExcelKitException("no HttpServletResponse available for @ExcelDownload");
        }
        response.setContentType(XLSX_CONTENT_TYPE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(annotation.filename()));
        GridExcelWriter.write(rows, elementType, ExcelDownloadOptions.from(annotation), response.getOutputStream());
    }

    private Object unwrap(Object returnValue) {
        Object value = returnValue;
        if (value instanceof ResponseEntity<?> entity) {
            value = entity.getBody();
        }
        if (value == null) {
            return null;
        }
        for (ExcelDataExtractor extractor : extractors) {
            if (extractor.supports(value)) {
                return extractor.extract(value);
            }
        }
        return value;
    }

    private Collection<?> toCollection(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Collection<?> collection) {
            return collection;
        }
        if (value instanceof Object[] array) {
            return Arrays.asList(array);
        }
        throw new ExcelKitException("cannot convert return value to a collection: " + value.getClass().getName());
    }

    private Class<?> resolveElementType(MethodParameter returnType, ExcelDownload annotation, Collection<?> rows) {
        if (annotation.type() != void.class) {
            return annotation.type();
        }
        Class<?> resolved = ElementTypes.find(ResolvableType.forMethodParameter(returnType));
        if (resolved != null && resolved != Object.class) {
            return resolved;
        }
        if (!rows.isEmpty()) {
            return rows.iterator().next().getClass();
        }
        throw new ExcelKitException("cannot determine element type for @ExcelDownload on "
                + returnType.getMethod() + "; specify @ExcelDownload(type = ...)");
    }

    private String contentDisposition(String filename) {
        String name = filename.toLowerCase().endsWith(".xlsx") ? filename : filename + ".xlsx";
        return ContentDisposition.attachment().filename(name, StandardCharsets.UTF_8).build().toString();
    }
}
