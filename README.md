# excel-kit

[![CI](https://github.com/jeong-donghee/excel-kit/actions/workflows/ci.yml/badge.svg)](https://github.com/jeong-donghee/excel-kit/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.jeong-donghee/excel-kit-core?label=maven%20central)](https://central.sonatype.com/search?q=g:io.github.jeong-donghee)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://adoptium.net/)

> Apache POI 기반 엑셀 생성 툴킷 — **차트·표·이미지 리포트**와 **대용량 스트리밍 목록**을 위한 작고 명료한 API.

Apache POI는 강력하지만, 차트 하나를 그리거나 목록을 엑셀로 떨구는 데에도 장황한 보일러플레이트가 필요합니다.
`excel-kit`은 두 가지 흔한 요구 — **리포트형 엑셀**과 **목록형 엑셀** — 을 각각의 모듈로 분리해, 의도가 드러나는 빌더 API로 감쌉니다.

## Table of Contents

- [모듈](#모듈)
- [설치](#설치)
- [사용법](#사용법)
- [빌드](#빌드)
- [설계 노트](#설계-노트)
- [로드맵](#로드맵)
- [기여](#기여)
- [라이선스](#라이선스)

## 모듈

| 모듈 | 설명 | POI 모드 |
|------|------|----------|
| [`excel-kit-core`](excel-kit-core) | 공통 워크북 팩토리·셀·스타일 유틸. 다른 모듈의 기반. | — |
| [`excel-kit-report`](excel-kit-report) | 표·막대/선 차트·이미지를 조합한 리포트. | XSSF (인메모리) |
| [`excel-kit-stream`](excel-kit-stream) | `@ExcelColumn`으로 매핑한 대용량 목록 익스포트. | SXSSF (스트리밍) |

필요한 모듈만 의존성으로 가져오면 됩니다. `report`/`stream`은 `core`를 전이 의존합니다.

## 설치

Maven:

```xml
<!-- 리포트형 엑셀이 필요할 때 -->
<dependency>
    <groupId>io.github.jeong-donghee</groupId>
    <artifactId>excel-kit-report</artifactId>
    <version>0.1.0</version>
</dependency>

<!-- 대용량 목록 익스포트가 필요할 때 -->
<dependency>
    <groupId>io.github.jeong-donghee</groupId>
    <artifactId>excel-kit-stream</artifactId>
    <version>0.1.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("io.github.jeong-donghee:excel-kit-report:0.1.0")
implementation("io.github.jeong-donghee:excel-kit-stream:0.1.0")
```

> 요구 사항: **Java 21+**.

## 사용법

### 리포트형 — 표 + 차트 (`excel-kit-report`)

```java
List<String> headers = List.of("월", "매출");
List<List<Object>> rows = List.of(
        List.of("1월", 100),
        List.of("2월", 150),
        List.of("3월", 130));

try (ReportBuilder report = ReportBuilder.create()) {
    ReportSheet sheet = report.sheet("매출");
    TableRange table = sheet.table(headers, rows);
    sheet.barChart("월별 매출", table, /*categoryCol*/ 0, /*valueCol*/ 1)
         .lineChart("추세",      table, 0, 1);
    report.writeTo(Path.of("report.xlsx"));
}
```

### 목록형 — 대용량 스트리밍 (`excel-kit-stream`)

```java
class Sale {
    @ExcelColumn(header = "월",   order = 1) String month;
    @ExcelColumn(header = "매출", order = 2) int amount;
}

try (OutputStream out = Files.newOutputStream(Path.of("sales.xlsx"))) {
    StreamingExcelExporter.of(Sale.class)
            .sheetName("매출")
            .windowSize(500)      // 메모리에 유지할 행 수
            .export(sales, out);  // sales: Iterable<Sale>
}
```

## 빌드

Maven Wrapper가 포함되어 있어 별도 설치가 필요 없습니다(단, **JDK 21+** 필요).

```bash
./mvnw clean verify
```

## 설계 노트

- **왜 모듈 분리인가** — 리포트는 풀 기능 XSSF(인메모리)가, 대용량 목록은 SXSSF(스트리밍, 저메모리)가 알맞습니다.
  관심사가 다르므로 모듈을 나누되, POI 보일러플레이트는 `core`에서 공유합니다.
- **groupId vs 패키지** — Maven 좌표는 `io.github.jeong-donghee`, 자바 패키지는 하이픈을 뺀 `io.github.jeongdonghee.excelkit.*`.

## 로드맵

- [ ] 셀 포맷(숫자/통화/날짜) 지정 API
- [ ] `excel-kit-stream`: 컬럼별 포맷·너비, 중첩 프로퍼티 매핑
- [ ] `excel-kit-report`: 파이/면적 차트, 다중 시리즈
- [ ] Maven Central 0.1.0 최초 발행

## 기여

이슈와 PR을 환영합니다. 커밋은 [Conventional Commits](https://www.conventionalcommits.org/),
버전은 [SemVer](https://semver.org/), 변경 이력은 [CHANGELOG.md](CHANGELOG.md)를 따릅니다.

## 라이선스

[MIT](LICENSE) © 2026 Donghee Jeong
