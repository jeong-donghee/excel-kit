# excel-kit-core

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../LICENSE)

`excel-kit`의 공통 기반 모듈. 다른 모듈이 공유하는 저수준 유틸을 제공한다.

| 클래스 | 역할 |
|--------|------|
| `Workbooks` | XSSF(인메모리) / SXSSF(스트리밍) 워크북 팩토리 |
| `Cells` | 자바 타입(숫자·불리언·날짜·문자열·null) → POI 셀 타입 매핑 |
| `CellStyles` | 헤더 스타일·테두리 등 재사용 스타일 헬퍼 |
| `ExcelKitException` | POI checked 예외를 감싸는 런타임 예외 |

보통 직접 의존하기보다 [`excel-kit-grid`](../excel-kit-grid) 등을 통해 전이 의존된다.

## 라이선스

[MIT](../LICENSE)
