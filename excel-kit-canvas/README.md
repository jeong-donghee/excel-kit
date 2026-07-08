# excel-kit-canvas

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../LICENSE)

데이터 클래스에 `@ExcelChart`를 붙이고 **리스트만 넘기면** 차트를 그려 .xlsx로 내보내는 모듈.
옵션은 전부 기본값이라 몰라도 되고, 필요할 때만 애노테이션 속성으로 지정한다. (grid와 같은 결)

- **요구사항**: Java 17+
- 지원 차트(2D): `BAR`, `LINE`, `AREA`, `RADAR`, `PIE`, `DOUGHNUT`, `SCATTER` (누적 막대 = `BAR` + `grouping=STACKED`)

## 설치

> 아직 미발행 — `0.2.0` 릴리스 예정.

```kotlin
// Gradle
implementation("io.github.jeong-donghee:excel-kit-canvas:0.2.0")
```
```xml
<!-- Maven -->
<dependency>
    <groupId>io.github.jeong-donghee</groupId>
    <artifactId>excel-kit-canvas</artifactId>
    <version>0.2.0</version>
</dependency>
```

## 사용법

```java
// 카테고리형 (bar/line/area/radar)
@ExcelChart(type = ChartType.BAR, title = "월별 매출", grouping = Grouping.STACKED)
class Sales {
    @ChartCategory String month;
    @ChartSeries(name = "매출") int revenue;
    @ChartSeries(name = "비용") int cost;   // 색은 팔레트 자동 (표현 계층 관심사)
}

// 파이형 (pie/doughnut) — 시리즈 1개, 슬라이스별 색은 @ChartColor(데이터)로
@ExcelChart(type = ChartType.PIE, title = "구성비")
class Share {
    @ChartCategory String name;
    @ChartSeries int value;
    @ChartColor String color;   // 예 "#4E79A7". 값이 비면 팔레트 자동
}

// XY형 (scatter)
@ExcelChart(type = ChartType.SCATTER, title = "상관")
class Point {
    @ChartX double x;
    @ChartY(name = "표본") double y;
}

// 사용 — 리스트만 넘기면 끝 (위→아래 자동 배치)
ExcelCanvas.create()
    .chart(salesList)
    .chart(shareList)
    .chart(pointList)
    .writeTo(Path.of("dashboard.xlsx"));
```

- **자동/자유 배치**: 위치를 안 주면 위에서 아래로 자동 배치. `@ExcelChart(col=0, row=0, width=8, height=15)`로 자유 배치.
- **빈 리스트**: 원소 타입을 알 수 없으므로 `canvas.chart(Sales.class, emptyList)` 오버로드를 쓴다.
- **색**: 지정 안 하면 기본 팔레트가 자동 배정(시리즈/슬라이스마다 다른 색). **요소별 색**이 필요하면 `@ChartColor` 필드(hex 문자열)로 — 색이 데이터로 들어온다(사용자가 고른 색 등). 파이·도넛 슬라이스, 단일 시리즈 막대에 적용.
- **검증**: `chart()` 호출 시 `@ExcelChart` 유무와 타입별 필수 필드(카테고리+시리즈, 파이는 시리즈 1개, 스캐터는 X+Y)를 검사해 어긋나면 예외.

## 애노테이션

| 애노테이션 | 대상 | 설명 |
|-----------|------|------|
| `@ExcelChart(type, title, legend, grouping, categoryAxisTitle, valueAxisTitle, col, row, width, height)` | 클래스 | 차트 메타. `type` 외 전부 기본값 |
| `@ChartCategory` | 필드 | 카테고리/슬라이스 라벨 (카테고리형·파이형) |
| `@ChartSeries(name)` | 필드 | 값 시리즈 (카테고리형·파이형; 파이는 1개). 색은 팔레트 자동 |
| `@ChartX` | 필드 | 분산형 X 값 |
| `@ChartY(name)` | 필드 | 분산형 Y 시리즈 |
| `@ChartColor` | 필드 | 요소별 색(hex 문자열). 파이·도넛 슬라이스, 단일 시리즈 막대. 없으면 팔레트 자동 |

## 라이선스

[MIT](../LICENSE)
