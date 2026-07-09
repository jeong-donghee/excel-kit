# excel-kit-processor

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](../LICENSE)

`@ExcelDownload` / `@ExcelColumn` 사용을 **컴파일 타임에 검증**하는 JSR-269 애노테이션 프로세서.
잘못된 사용을 런타임이 아니라 빌드에서 잡는다.

## 검증 규칙

- `@ExcelDownload`: 반환 타입에서 원소 타입을 정적으로 알 수 없고 `type`도 지정하지 않으면 → **컴파일 에러**
- `@ExcelColumn`: 메서드에 붙었는데 매개변수가 있으면 → **컴파일 에러**

```
error: @ExcelDownload: 반환 타입에서 원소 타입을 정적으로 알 수 없습니다.
       type 속성으로 명시하세요. 예) @ExcelDownload(type = Xxx.class)
```

## 사용

`excel-kit-grid`를 쓰는 프로젝트에서 이 프로세서를 애노테이션 프로세서 경로에 추가한다.

Gradle:

```kotlin
annotationProcessor("io.github.jeong-donghee:excel-kit-processor:0.2.0")
```

Maven:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>io.github.jeong-donghee</groupId>
        <artifactId>excel-kit-processor</artifactId>
        <version>0.2.0</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

런타임에 의존하지 않는다(애노테이션을 FQN 문자열로 매칭). 원소 타입 해석은 런타임(`grid`)과 동일한 규칙을 사용한다.

## 라이선스

[MIT](../LICENSE)
