package com.kennor.compiler.activity;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public class FactoryGroupedClasses {
    private static final String SUFFIX = "Factory";
    private String qualifiedClassName;
    private Messager mMessager;

    private Map<String, FactoryAnnotatedClass> itemsMap = new LinkedHashMap<>();

    public FactoryGroupedClasses(String qualifiedClassName, Messager mMessager) {
        this.qualifiedClassName = qualifiedClassName;
        this.mMessager = mMessager;
    }

    public void add(FactoryAnnotatedClass toInsert) {
        itemsMap.put(toInsert.getmId(), toInsert);
    }

    public void generateCode(Elements elementUtils, Filer filer) throws IOException {
        //  Generate java file
        TypeElement superClassName = elementUtils.getTypeElement(qualifiedClassName);
        String factoryClassName = superClassName.getSimpleName()+SUFFIX;
        String qualifiedFactoryClassName = qualifiedClassName+SUFFIX;
        PackageElement pkg = elementUtils.getPackageOf(superClassName);
        String packName = pkg.isUnnamed()?null:pkg.getQualifiedName().toString();

        mMessager.printMessage(Diagnostic.Kind.WARNING,"superClassName:"+superClassName);
        mMessager.printMessage(Diagnostic.Kind.WARNING,"factoryClassName:"+factoryClassName);
        mMessager.printMessage(Diagnostic.Kind.WARNING,"qualifiedFactoryClassName:"+qualifiedFactoryClassName);
        mMessager.printMessage(Diagnostic.Kind.WARNING,"packName:"+packName);

        MethodSpec.Builder method = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.STATIC)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class,"id")
                .returns(TypeName.get(superClassName.asType()));
        method.beginControlFlow("if(id == null)")
                .addStatement("throw new IllegalArgumentException($S)", "id is null!")
                .endControlFlow();

        for (FactoryAnnotatedClass item : itemsMap.values()) {
            method.beginControlFlow("if ($S.equals(id))", item.getmId())
                    .addStatement("return new $L()", item.getmAnnotatedClassElement().getQualifiedName().toString())
                    .endControlFlow();
        }
        method.addStatement("throw new IllegalArgumentException($S + id)", "Unknown id = ");
        TypeSpec typeSpec = TypeSpec
                .classBuilder(factoryClassName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(method.build())
                .build();

        JavaFile.builder(packName, typeSpec).build().writeTo(filer);
    }
}
