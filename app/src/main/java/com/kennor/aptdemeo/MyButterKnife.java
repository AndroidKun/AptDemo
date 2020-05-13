package com.kennor.aptdemeo;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class MyButterKnife {

    public static void bind(Activity activity) {
        //获取activity的decorView
        View view = activity.getWindow().getDecorView();
        String qualifiedName = activity.getClass().getName();
        //找到该activity对应的Bind类
        String generateClass = qualifiedName + "_ViewBinding";
        try {
            Class.forName(generateClass).getConstructor(activity.getClass(), Class.forName("android.view.View"))
                    .newInstance(activity, view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unBind(Activity activity) {
        View view = activity.getWindow().getDecorView();
        String qualifiedName = activity.getClass().getName();
        //找到该activity对应的Bind类
        String generateClass = qualifiedName + "_ViewBinding";
        try {
            Object instance = Class.forName(generateClass).getConstructor(activity.getClass(), Class.forName("android.view.View"))
                    .newInstance(activity, view);
            Class<?> bindActivity = Class.forName(generateClass);
            for (Method declaredMethod : bindActivity.getDeclaredMethods()) {
                if(declaredMethod.getName().equals("unBind")){
                    declaredMethod.invoke(instance,activity);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
