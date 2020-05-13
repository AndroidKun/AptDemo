package com.kennor.aptdemeo.shape;

import com.kennor.annotations.Factory;
import com.kennor.annotations.IShape;

import java.io.Serializable;

@Factory(id = "Circle",type = IShape.class)
public class Circle implements IShape{
    @Override
    public void draw() {
        System.out.println("Draw a Circle");
    }
}
