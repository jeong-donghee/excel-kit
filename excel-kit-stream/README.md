# excel-kit-stream

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../LICENSE)

`@ExcelColumn`으로 매핑한 POJO 목록을 **SXSSF 스트리밍**으로 내보내는 모듈.
API 응답 등 대량의 단순 목록을 저메모리로 .xlsx에 쓸 때 사용합니다. 차트/이미지 리포트는 [`excel-kit-report`](../excel-kit-report)를 쓰세요.

## 핵심 API

- `@ExcelColumn(header, order)` — 컬럼으로 내보낼 필드 표시. `header` 생략 시 필드명, `order` 오름차순 배치.
- `StreamingExcelExporter<T>` — `of(Type.class)` → `sheetName()` / `windowSize()` → `export(rows, out)`.

## 예시

```java
class Sale {
    @ExcelColumn(header = "월",   order = 1) String month;
    @ExcelColumn(header = "매출", order = 2) int amount;
    String internalNote; // @ExcelColumn 없으면 제외
}

try (OutputStream out = Files.newOutputStream(Path.of("sales.xlsx"))) {
    StreamingExcelExporter.of(Sale.class)
            .sheetName("매출")
            .windowSize(500)      // 메모리에 유지할 행 수(초과분은 임시 파일로 플러시)
            .export(sales, out);  // sales: Iterable<Sale>
}
```

## 왜 SXSSF인가

XSSF는 전체 워크북을 메모리에 들고 있어 행이 수십만 건이면 OOM 위험이 있습니다.
SXSSF는 최근 N행만 메모리에 유지하고 나머지는 임시 파일로 흘려보내, 메모리를 일정하게 유지합니다.
임시 파일 정리는 익스포터가 내부에서 처리합니다.

## 설치

```xml
<dependency>
    <groupId>io.github.jeong-donghee</groupId>
    <artifactId>excel-kit-stream</artifactId>
    <version>0.1.0</version>
</dependency>
```

## 라이선스

[MIT](../LICENSE)
