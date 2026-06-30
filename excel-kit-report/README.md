# excel-kit-report

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../LICENSE)

표·막대 차트·선 차트·이미지를 위에서 아래로 쌓아 **리포트형 엑셀(.xlsx)** 을 만드는 모듈.
풀 기능이 필요하므로 XSSF(인메모리)를 사용합니다. 대용량 단순 목록은 [`excel-kit-stream`](../excel-kit-stream)을 쓰세요.

## 핵심 API

- `ReportBuilder` — 진입점. `create()` → `sheet(name)` → `writeTo(...)`.
- `ReportSheet` — 한 시트에 `table()`, `barChart()`, `lineChart()`, `image()`를 순서대로 배치.
- `TableRange` — `table()`이 반환하는 표 위치. 차트가 데이터 컬럼을 참조할 때 사용.

## 예시

```java
List<String> headers = List.of("월", "매출");
List<List<Object>> rows = List.of(
        List.of("1월", 100),
        List.of("2월", 150),
        List.of("3월", 130));

try (ReportBuilder report = ReportBuilder.create()) {
    ReportSheet sheet = report.sheet("매출");
    TableRange table = sheet.table(headers, rows);
    sheet.barChart("월별 매출", table, 0, 1)   // (제목, 표, 카테고리 컬럼, 값 컬럼)
         .lineChart("추세",      table, 0, 1);
    report.writeTo(Path.of("report.xlsx"));
}
```

이미지 삽입:

```java
sheet.image(Files.readAllBytes(logoPath), Workbook.PICTURE_TYPE_PNG);
```

## 설치

```xml
<dependency>
    <groupId>io.github.jeong-donghee</groupId>
    <artifactId>excel-kit-report</artifactId>
    <version>0.1.0</version>
</dependency>
```

## 라이선스

[MIT](../LICENSE)
