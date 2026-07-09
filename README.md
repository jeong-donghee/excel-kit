# excel-kit

[![Maven Central](https://img.shields.io/maven-central/v/io.github.jeong-donghee/excel-kit-grid?label=maven%20central)](https://central.sonatype.com/artifact/io.github.jeong-donghee/excel-kit-grid)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://adoptium.net/)

Apache POI 기반 엑셀 생성을 더 간결하게 만드는 자바 라이브러리. 관심사별 모듈로 제공한다.

## 모듈

| 모듈 | 설명 |
|------|------|
| [`excel-kit-core`](excel-kit-core) | 공통 워크북·셀·스타일 유틸 (다른 모듈의 기반) |
| [`excel-kit-grid`](excel-kit-grid) | 스프링 목록 엑셀 익스포트 — `@ExcelDownload`만 붙이면 다운로드 |
| [`excel-kit-processor`](excel-kit-processor) | `@ExcelDownload`/`@ExcelColumn` 컴파일 타임 검증 |
| [`excel-kit-canvas`](excel-kit-canvas) | 어노테이션 기반 2D 차트 익스포트 — 클래스에 `@ExcelChart`, 리스트만 넘기면 차트 |

사용 예제와 메모리 벤치마크는 별도 레포 [jeong-donghee/excel-kit-examples](https://github.com/jeong-donghee/excel-kit-examples)에 있다.

## 빠른 예시 (`excel-kit-grid`)

```java
@ExcelDownload(filename = "sessions", rowNumberColumn = "No")
@GetMapping("/sessions/excel")
public List<SessionRow> sessions() { return service.findAll(); }
```

자세한 사용법은 [`excel-kit-grid`의 README](excel-kit-grid)를 참고.

## 빌드

```bash
./mvnw clean verify
```

Maven Wrapper 포함 (JDK 17+ 필요). 커밋은 [Conventional Commits](https://www.conventionalcommits.org/),
버전은 [SemVer](https://semver.org/)를 따른다.

## 라이선스

[MIT](LICENSE) © 2026 Donghee Jeong
