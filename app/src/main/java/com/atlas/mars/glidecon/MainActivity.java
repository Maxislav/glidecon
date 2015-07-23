package com.atlas.mars.glidecon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by Администратор on 7/22/15.
 */
public class MainActivity extends AppCompatActivity  {
    private static final String TAG = "MainActivityLog";
    ScreenParams screenParams;
    ImageView rotateImageView;//вращающаяся картинка
    FrameLayout rotationAreaFrame; // область по которой слушается движение
    float rotationImageAngle = 0;

    private final int  H_IMAGE = 0; //
    private final int  S_IMAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        screenParams = new ScreenParams(this);
        init();
    }

    private void init() {
        rotateImageView = (ImageView) findViewById(R.id.rotateImageView);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) screenParams.widthPixels, (int) screenParams.widthPixels);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
        rotateImageView.setLayoutParams(layoutParams);
        rotateImageView.setRotation(rotationImageAngle);
        rotationAreaFrame = (FrameLayout) findViewById(R.id.rotationAreaFrame);
        rotationAreaFrame.setLayoutParams(layoutParams);
        findViewById(R.id.staticImageView).setLayoutParams(layoutParams);

        new MoveEvents(rotationAreaFrame, rotateImageView, this);

        //rotationAreaFrame.setOnTouchListener(this);
    }









    /*@Override
    public boolean onTouch(View v, MotionEvent event) {
        final float X = event.getRawX();
        final float Y = event.getRawY();
        float _a, _b, tg;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                endAlpha = rotateImageView.getRotation();
                _a = X - (float) centerRotationX;
                _b = (float) centerRotationY - Y;
                if(0<_a && 0<_b ){ // 0 - 90
                    tg = _a / _b;
                    alpha = (Math.atan((double) tg) * 180 / Math.PI);
                }else if(_b<0 && 0<_a){ // 90 - 180
                    tg = _b / _a;
                    alpha = 90 - (Math.atan((double) tg) * 180 / Math.PI);
                }else if(_a<0 && _b<0){ // 180 - 270
                    tg = _a / _b;
                    alpha = 180 + (Math.atan((double) tg) * 180 / Math.PI);
                }else{ // 270 - 0
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

                if(0<_a && 0<_b ){ // 0 - 90
                    tg = _a / _b;
                    moveAlpha = (Math.atan((double) tg) * 180 / Math.PI);
                }else if(_b<0 && 0<_a){ // 90 - 180
                    tg = _b / _a;
                    moveAlpha = 90 - (Math.atan((double) tg) * 180 / Math.PI);
                }else if(_a<0 && _b<0){ // 180 - 270
                    tg = _a / _b;
                    moveAlpha = 180 + (Math.atan((double) tg) * 180 / Math.PI);
                }else{ // 270 - 0
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
    }*/
}
