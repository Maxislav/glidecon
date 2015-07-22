package com.atlas.mars.glidecon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by Администратор on 7/22/15.
 */
public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private static final String TAG = "MainActivityLog";
    ScreenParams screenParams;
    ImageView rotateImageView;//вращающаяся картинка
    FrameLayout rotationAreaFrame; // область по которой слушается движение
    int centerRotationX, centerRotationY;
    double alpha = 0.0, dAlpha = 0.0, moveAlpha = 0.0;
    float rotationImageAngle = 0;

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


        centerRotationX = screenParams.widthPixels / 2;
        centerRotationY = screenParams.heightPixels - (screenParams.widthPixels / 2);
        rotationAreaFrame.setOnTouchListener(this);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final float X = event.getRawX();
        final float Y = event.getRawY();

        float prot, pril, tg;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //Log.d(TAG, "ACTION_DOWN " + X + " : " + Y);

                // float tg = ((float)centerRotationY - Y)/(X-(float)centerRotationX);
                prot = (float) centerRotationY - Y;
                pril = X - (float) centerRotationX;
                tg = prot / pril;
                alpha = Math.atan((double) tg) * 180 / Math.PI;
                Log.d(TAG, "" + alpha);
                break;
            case MotionEvent.ACTION_MOVE:
                prot = (float) centerRotationY - Y;
                pril = X - (float) centerRotationX;
                tg = prot / pril;
                moveAlpha = (Math.atan((double) tg) * 180 / Math.PI);
                dAlpha = alpha - moveAlpha;
                rotationImageAngle = (float) dAlpha;
                rotateImageView.setRotation(rotationImageAngle);
                Log.d(TAG, "" + dAlpha);

                // Log.d(TAG, "ACTION_MOVE " + X + " : " + Y);
                break;

        }

        return true;
    }
}
