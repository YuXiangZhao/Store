package com.nytimes.android.external.storeannotations;


import com.google.common.base.CaseFormat;
import com.nytimes.android.external.store.base.annotation.Resizable;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


public class Generator {

    private static final String TARGET_PACKAGE = "com.nytimes.android.store.generated";
    private static final String PACKAGE_NAME = "com.nytimes.android.";
    private static final String PERSISTER_NAME = "Persister";
    private static final String STORE_NAME = "Store";
    private final ProcessingEnvironment env;
    private final TypeElement classElement;
    private PackageElement packageOf;


    public Generator(TypeElement classElement, ProcessingEnvironment env) throws IllegalArgumentException {
        this.classElement = classElement;
        this.env = env;
        packageOf = env.getElementUtils().getPackageOf(classElement);
    }


    public static String capitalize(String s) {
        if (s.length() == 0) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    void generateFile() {
        env.getMessager().printMessage(Diagnostic.Kind.WARNING, "class has annotation");

        TypeSpec.Builder classBuilder = createClass(classElement.getSimpleName().toString());

        ClassName textView = ClassName.get("android.widget", "TextView");
        ClassName list = ClassName.get("java.util", "List");
        ClassName arrayList = ClassName.get("java.util", "ArrayList");
        TypeName listOfTextviews = ParameterizedTypeName.get(list, textView);

        MethodSpec.Builder resizeableViewsMethod = MethodSpec.methodBuilder("resizeableViews")
                .returns(listOfTextviews)
                .addStatement("$T result = new $T<>()", listOfTextviews, arrayList)
                .addParameter(TypeName.get(classElement.asType()), classElement.getSimpleName()
                        .toString().toLowerCase(Locale.US));
        for (Element enclosedElement : classElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD
                    && enclosedElement.getAnnotation(Resizable.class) != null) {
                classBuilder = generateFieldGetter(
                        classBuilder, enclosedElement, resizeableViewsMethod);
            }
        }
        resizeableViewsMethod.addStatement("return result");
        classBuilder.addMethod(resizeableViewsMethod.build());
        writeFile(packageOf.toString(), classBuilder);

    }

    private void writeFile(String className, TypeSpec.Builder moduleClassBuilder) {
        JavaFile moduleFile = JavaFile.builder(className, moduleClassBuilder.build())
                .build();
        writeOut(classElement.getSimpleName() + "$FieldGetter", moduleFile);
    }

    private TypeSpec.Builder generateFieldGetter(TypeSpec.Builder classBuilder,
                                                 Element field, MethodSpec.Builder resizeableViewsMethod) {

        classBuilder = generateGetResizeableFields(field, methodName(field), classBuilder, resizeableViewsMethod);

        return classBuilder;
    }

    private String methodName(Element realMethod) {
        return capitalize(realMethod.getSimpleName().toString());
    }

    private TypeSpec.Builder createClass(String className) {
        String fieldGetterClassName = className + "$FieldGetter";
        return TypeSpec.classBuilder(fieldGetterClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    }

    private List<? extends Element> getMethods() {
        return classElement.getEnclosedElements();
    }

    private void writeOut(String className, JavaFile file) {
        try { // write the file
            JavaFileObject source = env.getFiler().createSourceFile(TARGET_PACKAGE + "." + className);
            Writer writer = source.openWriter();
            writer.write(file.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // Note: calling e.printStackTrace() will print IO errors
            // that occur from the file already existing after its first run, this is normal
        }
    }

    private TypeSpec.Builder generateGetResizeableFields(Element field, String fieldName,
                                                         TypeSpec.Builder classBuilder, MethodSpec.Builder resizeableViewsMethod) {
        String classNameLowerCase = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, classElement.getSimpleName().toString());
        resizeableViewsMethod.addStatement("result.add(" + classNameLowerCase + "." + fieldName.toLowerCase() + ")");

        return classBuilder;
    }

}
