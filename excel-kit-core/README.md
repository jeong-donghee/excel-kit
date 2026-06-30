# excel-kit-core

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../LICENSE)

`excel-kit`의 공통 기반 모듈. 워크북 생성, 셀 값 매핑, 자주 쓰는 스타일을 제공합니다.
보통 직접 의존하기보다 [`excel-kit-report`](../excel-kit-report)나 [`excel-kit-stream`](../excel-kit-stream)을 통해 전이 의존됩니다.

## 제공 기능

| 클래스 | 역할 |
|--------|------|
| `Workbooks` | XSSF(인메모리) / SXSSF(스트리밍) 워크북 팩토리 |
| `Cells` | 자바 타입(숫자·불리언·날짜·문자열·null) → POI 셀 타입 매핑 |
| `CellStyles` | 헤더 스타일, 얇은 테두리 등 재사용 스타일 헬퍼 |
| `ExcelKitException` | POI checked 예외를 감싸는 런타임 예외 |

## 예시

```java
try (XSSFWorkbook wb = Workbooks.xssf()) {
    Row row = wb.createSheet("s").createRow(0);
    Cells.setValue(row.createCell(0), "이름");   // 문자열
    Cells.setValue(row.createCell(1), 1234);      // 숫자
    Cells.setValue(row.createCell(2), LocalDate.now()); // 날짜
}
```

## 설치

```xml
<dependency>
    <groupId>io.github.jeong-donghee</groupId>
    <artifactId>excel-kit-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

## 라이선스

[MIT](../LICENSE)
