package com.kennor.compiler.activity;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.text.View;
import javax.tools.Diagnostic;

public class BindClass {

    private static TypeElement enclosingElement;
    private static Messager mMessager;
    private List<ViewBinding> fields;
    private boolean isFinal;
    TypeName targetTypeName;
    ClassName bindingClassName;
    ClassName VIEW = ClassName.get("android.view", "View");

    private BindClass(TypeElement enclosingElement) {
        TypeName targetType = TypeName.get(enclosingElement.asType());
        if (targetType instanceof ParameterizedTypeName) {
            targetType = ((ParameterizedTypeName) targetType).rawType;
        }
        String packageName = enclosingElement.getQualifiedName().toString();
        packageName = packageName.substring(0, packageName.lastIndexOf("."));
        String className = enclosingElement.getSimpleName().toString();
        ClassName bindingClassName = ClassName.get(packageName, className + "_ViewBinding");
        boolean isFinal = enclosingElement.getModifiers().contains(Modifier.FINAL);
        this.targetTypeName = targetType;
        this.bindingClassName = bindingClassName;
        this.isFinal = isFinal;
        fields = new ArrayList<>();
    }

    public static BindClass createBindClass(TypeElement enclosingElement, Messager mMessager) {
        BindClass.enclosingElement = enclosingElement;
        BindClass.mMessager = mMessager;
        return new BindClass(enclosingElement);
    }

    public void addAnnotationField(ViewBinding viewBinding) {
        fields.add(viewBinding);
    }

    public ClassName getBindingClassName() {
        return bindingClassName;
    }

    public List<ViewBinding> getFields() {
        return fields;
    }

    public void setFields(List<ViewBinding> fields) {
        this.fields = fields;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public TypeName getTargetTypeName() {
        return targetTypeName;
    }

    public void setTargetTypeName(TypeName targetTypeName) {
        this.targetTypeName = targetTypeName;
    }

    public void setBindingClassName(ClassName bindingClassName) {
        this.bindingClassName = bindingClassName;
    }

    public JavaFile preJavaFile()  {
        return JavaFile.builder(bindingClassName.packageName(), createTypeSpec())
                .addFileComment("kennor")
                .build();
    }

    private TypeSpec createTypeSpec()  {
        TypeSpec.Builder result = TypeSpec.classBuilder(bindingClassName.simpleName())
                .addModifiers(Modifier.PUBLIC);
        if (isFinal) {
            result.addModifiers(Modifier.FINAL);
        }
        result.addMethod(createConstructor(targetTypeName));
        result.addMethod(unBindMethod(targetTypeName));

        return result.build();
    }


    private MethodSpec createConstructor(TypeName targetType){
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        constructor.addParameter(targetType, "target", Modifier.FINAL);
        constructor.addParameter(VIEW, "source");
        for (ViewBinding bindings : fields) {
            addViewBinding(constructor, bindings);
        }
        return constructor.build();
    }
    private void addViewBinding(MethodSpec.Builder result, ViewBinding binding){
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("target.$L = ", binding.getName());
//        boolean requiresCast = binding.getType().equals(VIEW);
//        if (!requiresCast) {
//            builder.add("source.findViewById($L)", binding.getValue());
//        } else {
//            builder.add("$T.findViewByCast", targetTypeName);
//            builder.add("(source, $L", binding.getValue());
//            builder.add(", $T.class", binding.getType());
//            builder.add(")");
//        }
        builder.add("($L)source.findViewById($L)",binding.getType(), binding.getValue());
        result.addStatement("$L", builder.build());

    }

    private MethodSpec unBindMethod(TypeName targetTypeName) {
        MethodSpec.Builder unBindMethod = MethodSpec.methodBuilder("unBind")
                .addParameter(targetTypeName,"target")
                .addModifiers(Modifier.PUBLIC);
        for (ViewBinding bindings : fields) {
            CodeBlock.Builder builder = CodeBlock.builder()
                    .add("target.$L = null", bindings.getName());
            unBindMethod.addStatement("$L", builder.build());
        }
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("android.util.Log.w(\"TTT\", \"  unBindMethod\");");
        unBindMethod.addStatement("$L", builder.build());
        return unBindMethod.build();
    }
}
