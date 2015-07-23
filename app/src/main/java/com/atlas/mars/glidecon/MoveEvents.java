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
    double alpha = 0.0, dAlpha = 0.0, moveAlpha = 0.0, endAlpha = 0.0;
    int centerRotationX, centerRotationY;
    View rotationAreaFrame;
    View rotateImageView;
    CenterR center;

    public  MoveEvents(View rotationAreaFrame, View rotateImageView,  Activity activity){
        this.rotationAreaFrame = rotationAreaFrame;
        this.rotateImageView = rotateImageView;
        center = new CenterR(activity);
        centerRotationX = center.X;
        centerRotationY = center.Y;
        rotationAreaFrame.setOnTouchListener(this);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final float X = event.getRawX();
        final float Y = event.getRawY();
        float _a, _b, tg;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                endAlpha = rotateImageView.getRotation();
                _a = X - (float) centerRotationX;
                _b = (float) centerRotationY - Y;
                if (0 < _a && 0 < _b) { // 0 - 90
                    tg = _a / _b;
                    alpha = (Math.atan((double) tg) * 180 / Math.PI);
                } else if (_b < 0 && 0 < _a) { // 90 - 180
                    tg = _b / _a;
                    alpha = 90 - (Math.atan((double) tg) * 180 / Math.PI);
                } else if (_a < 0 && _b < 0) { // 180 - 270
                    tg = _a / _b;
                    alpha = 180 + (Math.atan((double) tg) * 180 / Math.PI);
                } else { // 270 - 0
                    tg = _b / _a;
                    alpha = 270 - (Math.atan((double) tg) * 180 / Math.PI);
                }

                Log.d(TAG, "" + alpha);
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "+++ACTION_UP");
                endAlpha = rotateImageView.getRotation();
                break;
            case MotionEvent.ACTION_MOVE:
                _a = X - (float) centerRotationX;
                _b = (float) centerRotationY - Y;

                if (0 < _a && 0 < _b) { // 0 - 90
                    tg = _a / _b;
                    moveAlpha = (Math.atan((double) tg) * 180 / Math.PI);
                } else if (_b < 0 && 0 < _a) { // 90 - 180
                    tg = _b / _a;
                    moveAlpha = 90 - (Math.atan((double) tg) * 180 / Math.PI);
                } else if (_a < 0 && _b < 0) { // 180 - 270
                    tg = _a / _b;
                    moveAlpha = 180 + (Math.atan((double) tg) * 180 / Math.PI);
                } else { // 270 - 0
                    tg = _b / _a;
                    moveAlpha = 270 - (Math.atan((double) tg) * 180 / Math.PI);
                }
                dAlpha = moveAlpha - alpha;
                rotateImageView.setRotation((float) (endAlpha + dAlpha));
                break;
            case MotionEvent.ACTION_OUTSIDE:
                Log.d(TAG, "+++ACTION_OUTSIDE");
                break;
            case MotionEvent.ACTION_CANCEL:
                endAlpha = rotateImageView.getRotation();
                Log.d(TAG, "" + endAlpha);
                break;

        }

        return true;
    }
}
