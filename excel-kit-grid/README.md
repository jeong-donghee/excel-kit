# excel-kit-grid

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../LICENSE)

스프링 컨트롤러가 반환하는 목록을 **어노테이션만으로** 엑셀 다운로드로 바꿔주는 모듈.
매핑을 응답마다 손으로 짜던 반복을 없앤다. 대용량은 내부적으로 **SXSSF 스트리밍**으로 저메모리 처리한다.

- **요구사항**: Java 17+, Spring Boot 3 (Spring 6, `jakarta.servlet`)
- 컨트롤러는 `List<T>`(또는 공통 응답 래퍼)를 반환하고, `@ExcelDownload`만 붙이면 끝.

## 설치

Maven:

```xml
<dependency>
    <groupId>io.github.jeong-donghee</groupId>
    <artifactId>excel-kit-grid</artifactId>
    <version>0.2.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("io.github.jeong-donghee:excel-kit-grid:0.2.0")
// (선택) @ExcelDownload/@ExcelColumn 오용을 컴파일 타임에 잡는 프로세서
annotationProcessor("io.github.jeong-donghee:excel-kit-processor:0.2.0")
```

> Maven에서 컴파일 타임 검증을 켜려면 `maven-compiler-plugin`의 `annotationProcessorPaths`에
> `excel-kit-processor`를 추가한다 ([excel-kit-processor](../excel-kit-processor) 참고).

## 빠른 시작

```java
// 1) 엑셀로 내보낼 필드를 DTO에 선언
public class SessionRow {
    @ExcelColumn(header = "세션 ID", order = 1)
    private String sessionId;

    private String user;
    private String dept;

    // 두 필드를 합친 파생 컬럼 → 메서드에 선언
    @ExcelColumn(header = "사용자", order = 2)
    public String userLabel() {
        return user + " (" + dept + ")";
    }
}

// 2) 컨트롤러에 @ExcelDownload. 이게 전부.
@ExcelDownload(filename = "sessions", rowNumberColumn = "No")
@GetMapping("/sessions/excel")
public List<SessionRow> sessions() {
    return sessionService.findAll();
}
```

`GET /sessions/excel` → `sessions.xlsx` 다운로드. (`filename`엔 확장자를 쓰지 않는다 — `.xlsx`는 자동.)

## 파생 컬럼 / 로직 컬럼

컬럼 값이 필드 하나로 안 떨어지면, **메서드에 `@ExcelColumn`** 을 붙인다.

```java
@ExcelColumn(header = "상태", order = 3)
public String status() {
    return (connectionCount + queryCount > 10) ? "warning" : "normal";
}
```

## 공통 응답 래퍼 사용 시

`ApiResponse<List<T>>` 같은 공통 포맷을 그대로 반환해도 된다. 앱에 **래퍼당 추출기 빈 하나**만 등록하면 된다.

```java
@ExcelDownload(filename = "sessions")
@GetMapping("/sessions/excel")
public ApiResponse<List<SessionRow>> sessions() {
    return ApiResponse.ok(sessionService.findAll());   // 원소 타입은 선언 제네릭에서 자동 추출
}

@Bean
ExcelDataExtractor apiResponseExtractor() {
    return ExcelDataExtractor.forType(ApiResponse.class, ApiResponse::getData);
}
```

- `List`/`Collection`/배열/`ResponseEntity`는 기본 내장 처리 → 빈이 필요 없다.
- 래퍼가 여러 개면 타입별로 빈을 추가한다(기존 것 수정 없음). 등록된 순서대로 시도한다.
- 반환 타입에 **제네릭이 없을 때만**(로타입/제네릭 없는 구체 래퍼) 원소 타입을 명시한다:
  `@ExcelDownload(type = SessionRow.class)`.

## 대용량 / 시트 분할

```java
@ExcelDownload(
    filename        = "sessions",
    sheetName       = "세션목록",                        // 분할 시 세션목록_1, 세션목록_2 …
    sheetNumbering  = SheetNumbering.SUFFIX_UNDERSCORE,  // 기본값 (SUFFIX_PAREN → "세션목록 (1)")
    maxRowsPerSheet = 500_000,                           // 미지정 시 1,048,575 (엑셀 한도-헤더)
    maxSheets       = 10,                                // 미지정 시 무제한
    overflowPolicy  = OverflowPolicy.TRUNCATE            // 기본값
)
@GetMapping("/sessions/excel")
public List<SessionRow> sessions() { return sessionService.findAll(); }
```

- `maxRowsPerSheet` 초과 → 자동으로 다음 시트 분할, **헤더는 각 시트에 반복**.
- 행 번호(`rowNumberColumn`)는 **1부터, 헤더 제외, 시트가 나뉘어도 전역 연속**.
- `maxSheets`(가드)를 넘기면 `overflowPolicy`에 따라: `TRUNCATE`(나머지 버림 + 경고로그, 기본) / `FAIL`(예외).
  가드를 설정하지 않으면 데이터 유실은 없다.

## 어노테이션 레퍼런스

**`@ExcelColumn`** (필드 또는 무인자 메서드)

| 속성 | 설명 |
|------|------|
| `header` | 헤더 텍스트 |
| `order` | 컬럼 순서(오름차순) |

**`@ExcelDownload`** (컨트롤러 메서드)

| 속성 | 기본값 | 설명 |
|------|--------|------|
| `filename` | 필수 | 파일 베이스명 (`.xlsx` 자동 첨부) |
| `sheetName` | 클래스명 기반 | 시트 베이스명 |
| `sheetNumbering` | `SUFFIX_UNDERSCORE` | 분할 시트 번호 표기 방식 |
| `rowNumberColumn` | 없음 | 지정 시 맨 앞에 행번호 컬럼(값=헤더 텍스트) |
| `maxRowsPerSheet` | `1048575` | 시트당 최대 데이터 행 (헤더 제외, 엑셀 한도 1,048,576 기준) |
| `maxSheets` | 무제한 | 시트 수 상한(가드) |
| `overflowPolicy` | `TRUNCATE` | 가드 초과 시 동작 (`TRUNCATE`/`FAIL`) |
| `type` | 자동 추출 | 반환 타입에 제네릭이 없을 때만 원소 타입 명시 |

## 컴파일 타임 검증

`excel-kit-processor`(애노테이션 프로세서)가 빌드 시점에 오용을 잡는다. 예: 반환 타입에서 원소 타입을
알 수 없고 `type`도 없으면 **컴파일 에러**. (런타임에도 앱 시작 시 fail-fast)

## 라이선스

[MIT](../LICENSE)
