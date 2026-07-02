package io.github.jeongdonghee.excelkit.processor;

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * {@code @ExcelDownload}/{@code @ExcelColumn} 사용을 컴파일 타임에 검증한다.
 *
 * <ul>
 *   <li>{@code @ExcelDownload}: 반환 타입에서 원소 타입을 정적으로 알 수 없고 {@code type}도 없으면 컴파일 에러.</li>
 *   <li>{@code @ExcelColumn}: 메서드에 붙었는데 매개변수가 있으면 컴파일 에러.</li>
 * </ul>
 *
 * <p>런타임 모듈(grid)에 의존하지 않도록 애노테이션은 FQN 문자열로 매칭한다.
 */
@SupportedAnnotationTypes({
        ExcelKitProcessor.EXCEL_DOWNLOAD,
        ExcelKitProcessor.EXCEL_COLUMN
})
public class ExcelKitProcessor extends AbstractProcessor {

    static final String EXCEL_DOWNLOAD = "io.github.jeongdonghee.excelkit.grid.ExcelDownload";
    static final String EXCEL_COLUMN = "io.github.jeongdonghee.excelkit.grid.ExcelColumn";

    private Types types;
    private Elements elements;
    private Messager messager;
    private TypeMirror collectionErasure;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.types = processingEnv.getTypeUtils();
        this.elements = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        TypeElement collection = elements.getTypeElement("java.util.Collection");
        this.collectionErasure = (collection != null) ? types.erasure(collection.asType()) : null;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            String name = annotation.getQualifiedName().toString();
            if (EXCEL_DOWNLOAD.equals(name)) {
                for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                    if (element instanceof ExecutableElement method) {
                        checkExcelDownload(method);
                    }
                }
            } else if (EXCEL_COLUMN.equals(name)) {
                for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                    if (element.getKind() == ElementKind.METHOD && element instanceof ExecutableElement method) {
                        checkExcelColumnMethod(method);
                    }
                }
            }
        }
        return false;
    }

    private void checkExcelDownload(ExecutableElement method) {
        if (hasExplicitType(method)) {
            return;
        }
        if (!isElementTypeResolvable(method.getReturnType())) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "@ExcelDownload: 반환 타입에서 원소 타입을 정적으로 알 수 없습니다. "
                            + "type 속성으로 명시하세요. 예) @ExcelDownload(type = Xxx.class)",
                    method);
        }
    }

    private void checkExcelColumnMethod(ExecutableElement method) {
        if (!method.getParameters().isEmpty()) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "@ExcelColumn 메서드는 매개변수가 없어야 합니다.", method);
        }
    }

    /** {@code type} 속성이 명시되고 void가 아니면 true. */
    private boolean hasExplicitType(ExecutableElement method) {
        for (AnnotationMirror mirror : method.getAnnotationMirrors()) {
            TypeElement annElement = (TypeElement) mirror.getAnnotationType().asElement();
            if (!EXCEL_DOWNLOAD.equals(annElement.getQualifiedName().toString())) {
                continue;
            }
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                    : mirror.getElementValues().entrySet()) {
                if (entry.getKey().getSimpleName().contentEquals("type")) {
                    Object value = entry.getValue().getValue();
                    if (value instanceof TypeMirror typeMirror) {
                        return typeMirror.getKind() != TypeKind.VOID
                                && !"java.lang.Void".equals(typeMirror.toString());
                    }
                }
            }
        }
        return false;
    }

    private boolean isElementTypeResolvable(TypeMirror type) {
        TypeMirror element = findElement(type);
        return element != null && isConcrete(element);
    }

    private TypeMirror findElement(TypeMirror type) {
        if (type == null) {
            return null;
        }
        switch (type.getKind()) {
            case ARRAY:
                return ((ArrayType) type).getComponentType();
            case DECLARED:
                DeclaredType declared = (DeclaredType) type;
                if (isCollection(declared) && !declared.getTypeArguments().isEmpty()) {
                    return declared.getTypeArguments().get(0);
                }
                for (TypeMirror argument : declared.getTypeArguments()) {
                    TypeMirror found = findElement(argument);
                    if (found != null) {
                        return found;
                    }
                }
                return null;
            default:
                return null;
        }
    }

    private boolean isCollection(DeclaredType type) {
        return collectionErasure != null && types.isAssignable(types.erasure(type), collectionErasure);
    }

    private boolean isConcrete(TypeMirror type) {
        if (type.getKind() == TypeKind.ARRAY) {
            return true;
        }
        if (type.getKind() != TypeKind.DECLARED) {
            return false; // 타입변수·와일드카드 → 정적으로 결정 불가
        }
        return !"java.lang.Object".equals(types.erasure(type).toString());
    }
}
