package com.kennor.aptdemeo.shape;

import com.kennor.annotations.Factory;
import com.kennor.annotations.IShape;

@Factory(id = "Oval",type = IShape.class)
public class Oval implements IShape{
    @Override
    public void draw() {
        System.out.println("Draw a Oval");
    }
}
