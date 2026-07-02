# excel-kit-examples

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../LICENSE)

`excel-kit-grid`의 **권장 사용법(베스트 프랙티스) 레퍼런스** 앱과 **메모리 벤치마크**. 발행하지 않는다.

## 실행

```bash
./mvnw -pl excel-kit-examples spring-boot:run
```

| 엔드포인트 | 설명 |
|------------|------|
| `GET /sessions/excel` | ① 가장 단순 — `List<T>` 반환 + 행번호 컬럼 |
| `GET /sessions/wrapped/excel` | ② 공통 응답 래퍼(`ApiResponse<List<T>>`) 그대로 반환 |
| `GET /sessions/large/excel` | ③ 25만 행 → 시트 3개로 자동 분할 |

## 베스트 프랙티스 포인트

- **DTO**([`SessionRow`](src/main/java/io/github/jeongdonghee/excelkit/examples/SessionRow.java)):
  단순 값은 필드에 `@ExcelColumn`, 두 필드를 합치거나 계산하는 컬럼은 **메서드**에 `@ExcelColumn`.
- **컨트롤러**([`SessionController`](src/main/java/io/github/jeongdonghee/excelkit/examples/SessionController.java)):
  `@ExcelDownload`만 붙이고 평소처럼 목록/래퍼를 반환. 응답마다 매핑을 손대지 않는다.
- **공통 래퍼**([`ExcelKitConfig`](src/main/java/io/github/jeongdonghee/excelkit/examples/ExcelKitConfig.java)):
  래퍼 타입당 `ExcelDataExtractor` 빈 하나만 등록.
- **대용량**: `maxRowsPerSheet`로 분할, `sheetNumbering`으로 시트명 번호 표기. 내부는 SXSSF 스트리밍.

## 메모리 벤치마크

행 수를 늘려도 **피크 힙이 거의 일정**함을 보여준다(SXSSF는 최근 N행만 메모리에 유지).

```bash
./mvnw -pl excel-kit-examples -q exec:java \
  -Dexec.mainClass=io.github.jeongdonghee.excelkit.examples.benchmark.MemoryBenchmark
```

낮은 힙으로 대용량이 견디는지 확인:

```bash
java -Xmx256m -cp <classpath> io.github.jeongdonghee.excelkit.examples.benchmark.MemoryBenchmark
```

### 측정 결과 (JVM `-Xmx256m`, macOS)

| 행 수 | 피크 힙 | 소요 시간 |
|------:|--------:|----------:|
| 100,000 | 33.7 MB | 0.3s |
| 500,000 | 33.5 MB | 1.0s |
| 1,000,000 | 33.7 MB | 2.0s |

**피크 힙이 행 수와 무관하게 ~34MB로 일정.** 100만 행도 256MB 힙에서 완료된다.
순진하게 전체 워크북을 인메모리(XSSF)로 만들면 힙이 행 수에 비례해 늘고 결국 OOM 나지만,
SXSSF는 최근 N행만 유지하고 나머지를 임시 파일로 흘려보내므로 힙이 일정하다.

> 위 수치는 이 모듈의 `MemoryBenchmark`를 `-Xmx256m`로 직접 돌린 결과다(재현 가능).

## 라이선스

[MIT](../LICENSE)
