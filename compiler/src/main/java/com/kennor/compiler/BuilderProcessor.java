package com.kennor.compiler;

import com.kennor.annotations.BindView;
import com.kennor.annotations.Builder;
import com.kennor.annotations.Factory;
import com.kennor.annotations.Optional;
import com.kennor.annotations.Required;
import com.kennor.compiler.activity.BindClass;
import com.kennor.compiler.activity.FactoryAnnotatedClass;
import com.kennor.compiler.activity.FactoryGroupedClasses;
import com.kennor.compiler.activity.ViewBinding;
import com.kennor.compiler.exception.ProcessingException;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

//@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {
    private Types mTypeUtils;
    private Messager mMessager;
    private Filer mFiler;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mTypeUtils = processingEnvironment.getTypeUtils();
        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnvironment.getFiler();
        mElementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotations = new LinkedHashSet<>();
        supportedAnnotations.add(Builder.class.getCanonicalName());
        supportedAnnotations.add(Required.class.getCanonicalName());
        supportedAnnotations.add(Optional.class.getCanonicalName());
        supportedAnnotations.add(Factory.class.getCanonicalName());
        supportedAnnotations.add(BindView.class.getCanonicalName());
        return supportedAnnotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        mMessager.printMessage(Diagnostic.Kind.WARNING, "============>process");

        try {
            parseRoundEnvironment(roundEnvironment);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //scan all class who has @Builder.
        for (Element element : roundEnvironment.getElementsAnnotatedWith(Builder.class)) {
            mMessager.printMessage(Diagnostic.Kind.WARNING, element.getSimpleName().toString());
        }

        Map<String, FactoryGroupedClasses> factoryClasses = new LinkedHashMap<>();

        List<String> ids = new ArrayList<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWith(Factory.class)) {
            mMessager.printMessage(Diagnostic.Kind.WARNING, element.getSimpleName().toString());
            if (element.getKind() != ElementKind.CLASS) {
                throw new ProcessingException(element, "Only classes can be annotated with @%s",
                        Factory.class.getSimpleName());
            }
            TypeElement typeElement = (TypeElement) element;
            FactoryAnnotatedClass annotatedClass = new FactoryAnnotatedClass(typeElement);
            checkValidClass(annotatedClass, ids);

            FactoryGroupedClasses factoryClass = factoryClasses.get(annotatedClass.getmQualifiedSuperClassName());
            if (factoryClass == null) {
                String qualifiedGroupName = annotatedClass.getmQualifiedSuperClassName();
                factoryClass = new FactoryGroupedClasses(qualifiedGroupName, mMessager);
                factoryClasses.put(qualifiedGroupName, factoryClass);
            }
            factoryClass.add(annotatedClass);
        }
        for (FactoryGroupedClasses factoryClass : factoryClasses.values()) {
            try {
                factoryClass.generateCode(mElementUtils, mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private void parseRoundEnvironment(RoundEnvironment roundEnv) throws ClassNotFoundException, IOException {
        Map<TypeElement, BindClass> map = new LinkedHashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(BindView.class)) {
            //enclosingElement:MainActivity
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            int annotationValue = element.getAnnotation(BindView.class).value();
            BindClass bindClass = map.get(enclosingElement);
            if (bindClass == null) {
                bindClass = BindClass.createBindClass(enclosingElement,mMessager);
                map.put(enclosingElement, bindClass);
            }
            String name = element.getSimpleName().toString();
            TypeName type = TypeName.get(element.asType());
            //name:textView
            //type:android.widget.TextView
            //annotationValue:2131165353
            ViewBinding viewBinding = ViewBinding.createViewBind(name, type, annotationValue);
            bindClass.addAnnotationField(viewBinding);
        }
        for (Map.Entry<TypeElement, BindClass> entry : map.entrySet()) {
            //getBindingClassName:com.kennor.aptdemeo.MainActivity_ViewBinding
            mMessager.printMessage(Diagnostic.Kind.WARNING, "getBindingClassName:" + entry.getValue().getBindingClassName());
            entry.getValue().preJavaFile().writeTo(mFiler);
        }
    }

    private void checkValidClass(FactoryAnnotatedClass item, List<String> ids) throws ProcessingException {
        if (ids.contains(item.getmId())) {
            throw new ProcessingException(item.getmAnnotatedClassElement(), "The class %s id had used,please change other one.",
                    item.getmAnnotatedClassElement().getQualifiedName().toString());
        }
        ids.add(item.getmId());

        TypeElement classElement = item.getmAnnotatedClassElement();
        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ProcessingException(classElement, "The class %s is not public.",
                    classElement.getQualifiedName().toString());
        }

        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new ProcessingException(classElement,
                    "The class %s is abstract. You can't annotate abstract classes with @%",
                    classElement.getQualifiedName().toString(), Factory.class.getSimpleName());
        }

        //com.kennor.annotations.IShape
        TypeElement superClassElement = mElementUtils.getTypeElement(item.getmQualifiedSuperClassName());
        if (superClassElement.getKind() == ElementKind.INTERFACE) {
            if (!classElement.getInterfaces().contains(superClassElement.asType())) {
                throw new ProcessingException(classElement,
                        "The class %s annotated with @%s must implement the interface %s",
                        classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                        item.getmQualifiedSuperClassName());
            }

        } else {
            TypeElement currentClass = classElement;
            while (true) {
                TypeMirror superClassType = currentClass.getSuperclass();
                if (superClassType.getKind() == TypeKind.NONE) {
                    throw new ProcessingException(classElement,
                            "The class %s annotated with @%s must inherit from %s",
                            classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                            item.getmQualifiedSuperClassName());
                }

                if (superClassType.toString().equals(item.getmQualifiedSuperClassName())) {
                    break;
                }
                currentClass = (TypeElement) mTypeUtils.asElement(superClassType);
            }

            for (Element enclosed : classElement.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                    ExecutableElement constructorElement = (ExecutableElement) enclosed;
                    if (constructorElement.getParameters().size() == 0 &&
                            constructorElement.getModifiers().contains(Modifier.PUBLIC)) {
                        return;
                    }
                }
            }

            throw new ProcessingException(classElement,
                    "The class %s must provide an public empty default constructor",
                    classElement.getQualifiedName().toString());
        }

    }

}
