package com.kennor.aptdemeo.shape;

import com.kennor.annotations.Factory;
import com.kennor.annotations.IShape;

@Factory(id = "Rectangle",type = IShape.class)
public class Rectangle implements IShape {


    @Override
    public void draw() {
        System.out.println("Draw a Rectangle");
    }
}
