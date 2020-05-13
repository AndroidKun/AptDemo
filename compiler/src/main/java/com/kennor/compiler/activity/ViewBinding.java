package com.kennor.compiler.activity;

import com.squareup.javapoet.TypeName;

public class ViewBinding {
    private final String name;
    private final TypeName type;
    private final int value;

    private ViewBinding(String name, TypeName type, int value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public static ViewBinding createViewBind(String name, TypeName type, int value) {
        return new ViewBinding(name, type, value);
    }

    public String getName() {
        return name;
    }

    public TypeName getType() {
        return type;
    }

    public int getValue() {
        return value;
    }
}
