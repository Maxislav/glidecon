package com.atlas.mars.glidecon;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by mars on 7/23/15.
 */
public class MoveEvents implements View.OnTouchListener {
    private static final String TAG = "MoveEventsLog";
    double alphaDisk = 0.0,  endAlphaDisk = 0.0;
    double alphaSnos = 0.0,  endAlphaSnos = 0.0;
    int centerRotationX, centerRotationY;
    View rotationAreaFrame;
    View rotateImageView;
    View snosImageView;
    CenterR center;

    private final int  H_IMAGE = 0; //
    private final int  S_IMAGE = 1;

    private int whoRotate;

    public  MoveEvents(View rotationAreaFrame, View rotateImageView, View snosImageView,  Activity activity){
        this.rotationAreaFrame = rotationAreaFrame;
        this.rotateImageView = rotateImageView;
        this.snosImageView = snosImageView;
        center = new CenterR(activity);
        centerRotationX = center.X;
        centerRotationY = center.Y;
        rotationAreaFrame.setOnTouchListener(this);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final float X = event.getRawX();
        final float Y = event.getRawY();
        double moveAlpha = 0.0,dAlpha = 0.0;
        float _a, _b, tg;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                endAlphaDisk = rotateImageView.getRotation();
                endAlphaSnos = snosImageView.getRotation();
                _a = X - (float) centerRotationX;
                _b = (float) centerRotationY - Y;
                alphaDisk = getAlphaDisk(_a, _b);

                if( alphaDisk< endAlphaSnos+90+20 && endAlphaSnos-20+90<alphaDisk){
                    whoRotate = S_IMAGE;
                }else{
                    whoRotate = H_IMAGE;
                }

                Log.d(TAG, "alphaDisk " + alphaDisk);
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "+++ACTION_UP");
                endAlphaDisk = rotateImageView.getRotation();
                endAlphaSnos = snosImageView.getRotation();
                break;
            case MotionEvent.ACTION_MOVE:
                _a = X - (float) centerRotationX;
                _b = (float) centerRotationY - Y;

                moveAlpha = getAlphaDisk(_a, _b);
                dAlpha = moveAlpha - alphaDisk;
                if(whoRotate == S_IMAGE){
                    snosImageView.setRotation((float) (endAlphaSnos + dAlpha));
                }else{
                    rotateImageView.setRotation((float) (endAlphaDisk + dAlpha));
                }

                break;
            case MotionEvent.ACTION_OUTSIDE:
                Log.d(TAG, "+++ACTION_OUTSIDE");
                break;
            case MotionEvent.ACTION_CANCEL:
                endAlphaDisk = rotateImageView.getRotation();
                Log.d(TAG, "ACTION_CANCEL " + endAlphaDisk);
                break;

        }

        return true;
    }

    private double getAlphaDisk( float _a, float _b){
        float tg;
        double alphaDisk;
        if (0 < _a && 0 < _b) { // 0 - 90
            tg = _a / _b;
            alphaDisk = (Math.atan((double) tg) * 180 / Math.PI);
        } else if (_b < 0 && 0 < _a) { // 90 - 180
            tg = _b / _a;
            alphaDisk = 90 - (Math.atan((double) tg) * 180 / Math.PI);
        } else if (_a < 0 && _b < 0) { // 180 - 270
            tg = _a / _b;
            alphaDisk = 180 + (Math.atan((double) tg) * 180 / Math.PI);
        } else { // 270 - 0
            tg = _b / _a;
            alphaDisk = 270 - (Math.atan((double) tg) * 180 / Math.PI);
        }
        return  alphaDisk;
    }
}
