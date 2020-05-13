package com.kennor.compiler.activity;


import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

public class ActivityClass {

    public ActivityClass(TypeElement typeElement) {
        Name simpleName = typeElement.getSimpleName();
        Name qualifiedName = typeElement.getQualifiedName();
    }
}
