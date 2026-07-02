package io.github.jeongdonghee.excelkit.examples;

import io.github.jeongdonghee.excelkit.grid.ExcelDataExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 베스트 프랙티스: 공통 응답 래퍼를 쓰는 앱은 <b>래퍼 타입당 extractor 빈 하나</b>만 등록한다.
 * 그러면 모든 {@code @ExcelDownload}에서 그 래퍼를 자동으로 벗겨 목록을 꺼낸다.
 *
 * <p>래퍼가 여러 개면 타입별로 빈을 더 추가하면 된다(기존 것 수정 없음). 예:
 * <pre>{@code
 * @Bean ExcelDataExtractor pageExtractor() {
 *     return ExcelDataExtractor.forType(Page.class, Page::getContent);
 * }
 * }</pre>
 */
@Configuration
public class ExcelKitConfig {

    @Bean
    public ExcelDataExtractor apiResponseExtractor() {
        return ExcelDataExtractor.forType(ApiResponse.class, ApiResponse::getData);
    }
}
