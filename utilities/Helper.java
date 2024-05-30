package com.aceburgundy.criminalintent.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Helper {

    private Helper() {}

    public static boolean onTablet(Context context) {
        double heightInInches = (double) context.getResources().getConfiguration().screenHeightDp / 160;
        double widthInInches = (double) context.getResources().getConfiguration().screenWidthDp / 160;
        double heightSquared = Math.pow(heightInInches, 2);
        double widthSquared = Math.pow(widthInInches, 2);

        // pythagorean theorem to get the diagonal size of the screen
        double screenDiagonalLength = Math.sqrt(heightSquared + widthSquared);

        return screenDiagonalLength >= 7.0;
    }

    public static boolean onPhone(Context context) {
        return !onTablet(context);
    }

    public static void disableElement(View view) {
        view.setEnabled(false);
        try {
            Method method = view.getClass().getMethod("setTextColor", int.class);
            method.invoke(view, Color.GRAY);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // The view doesn't have a setTextColor method, do nothing
        }
    }

    public static void enableElement(View view) {
        view.setEnabled(true);
        try {
            Method method = view.getClass().getMethod("setTextColor", int.class);
            method.invoke(view, Color.BLACK);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // The view doesn't have a setTextColor method, do nothing
        }
    }

}
