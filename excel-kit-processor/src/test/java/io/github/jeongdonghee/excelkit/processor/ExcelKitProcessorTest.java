package io.github.jeongdonghee.excelkit.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;

class ExcelKitProcessorTest {

    /** 주어진 소스를 우리 프로세서만 붙여 컴파일(-proc:only)하고 ERROR 진단 개수를 센다. */
    private long errorCount(String simpleName, String code) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);

        List<String> options = List.of("-proc:only", "-cp", System.getProperty("java.class.path"));
        JavaFileObject source = new SimpleJavaFileObject(
                URI.create("string:///" + simpleName + ".java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return code;
            }
        };

        JavaCompiler.CompilationTask task =
                compiler.getTask(null, fileManager, diagnostics, options, null, List.of(source));
        task.setProcessors(List.of(new ExcelKitProcessor()));
        task.call();

        return diagnostics.getDiagnostics().stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                .count();
    }

    @Test
    void listReturnCompilesWithoutError() {
        String code = """
                import io.github.jeongdonghee.excelkit.grid.*;
                import java.util.List;
                class Ok {
                    @ExcelColumn(header = "a") String a;
                    @ExcelDownload(filename = "x")
                    List<Ok> f() { return null; }
                }
                """;
        assertEquals(0, errorCount("Ok", code));
    }

    @Test
    void genericWrapperResolvesElementType() {
        String code = """
                import io.github.jeongdonghee.excelkit.grid.*;
                import java.util.List;
                class Wrap {
                    @ExcelColumn(header = "a") String a;
                    static class Api<T> { T data; }
                    @ExcelDownload(filename = "x")
                    Api<List<Wrap>> f() { return null; }
                }
                """;
        assertEquals(0, errorCount("Wrap", code));
    }

    @Test
    void nonGenericReturnWithoutTypeFails() {
        String code = """
                import io.github.jeongdonghee.excelkit.grid.*;
                class Bad {
                    @ExcelColumn(header = "a") String a;
                    @ExcelDownload(filename = "x")
                    Bad f() { return null; }
                }
                """;
        assertTrue(errorCount("Bad", code) >= 1);
    }

    @Test
    void nonGenericReturnWithTypePasses() {
        String code = """
                import io.github.jeongdonghee.excelkit.grid.*;
                class WithType {
                    @ExcelColumn(header = "a") String a;
                    @ExcelDownload(filename = "x", type = WithType.class)
                    Object f() { return null; }
                }
                """;
        assertEquals(0, errorCount("WithType", code));
    }

    @Test
    void columnMethodWithParameterFails() {
        String code = """
                import io.github.jeongdonghee.excelkit.grid.*;
                import java.util.List;
                class Col {
                    @ExcelColumn(header = "a") String bad(int p) { return ""; }
                    @ExcelDownload(filename = "x")
                    List<Col> f() { return null; }
                }
                """;
        assertTrue(errorCount("Col", code) >= 1);
    }
}
