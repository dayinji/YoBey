package com.badprinter.yobey.utils;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by root on 15-8-26.
 */
public class Animation_3D extends Animation {

    private final float fromXDegrees;
    private final float toXDegrees;
    private final float fromYDegrees;
    private final float toYDegrees;
    private final float fromZDegrees;
    private final float toZDegrees;
    private final float fromZ;
    private final float toZ;
    private Camera camera;
    private int width = 0;
    private int height = 0;

    public Animation_3D(float fromXDegrees, float toXDegrees, float fromYDegrees,
                        float toYDegrees, float fromZDegrees, float toZDegrees,
                        float fromZ, float toZ) {
        this.fromXDegrees = fromXDegrees;
        this.toXDegrees = toXDegrees;
        this.fromYDegrees = fromYDegrees;
        this.toYDegrees = toYDegrees;
        this.fromZDegrees = fromZDegrees;
        this.toZDegrees = toZDegrees;
        this.fromZ = fromZ;
        this.toZ = toZ;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        this.width = width / 2;
        this.height = height / 2;
        camera = new Camera();
        //camera.translate(0, 0, 200);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float xDegrees = fromXDegrees + ((toXDegrees - fromXDegrees) * interpolatedTime);
        float yDegrees = fromYDegrees + ((toYDegrees - fromYDegrees) * interpolatedTime);
        float zDegrees = fromZDegrees + ((toZDegrees - fromZDegrees) * interpolatedTime);
        float z;
        if (interpolatedTime < getDuration()/2000f) {
            z = fromZ + ((toZ - fromZ) * interpolatedTime);
        }else {
            z = fromZ + ((toZ - fromZ) * (getDuration()/1000f-interpolatedTime));
        }
        camera.translate(0, 0, z);

        final Matrix matrix = t.getMatrix();

        camera.save();
        camera.rotateX(xDegrees);
        camera.rotateY(yDegrees);
        camera.rotateZ(zDegrees);
        System.out.println("" + yDegrees);
        camera.getMatrix(matrix);
        camera.restore();

        matrix.preTranslate(-this.width, -this.height);
        matrix.postTranslate(this.width, this.height);
    }
}