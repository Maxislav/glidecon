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
    ImageView snosImageView;//вращающаяся сносы
    FrameLayout rotationAreaFrame; // область по которой слушается движение
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
        findViewById(R.id.staticImageView).setLayoutParams(layoutParams);
        snosImageView =(ImageView)findViewById(R.id.snosImageView);
        snosImageView.setLayoutParams(layoutParams);
        new MoveEvents(rotationAreaFrame, rotateImageView, snosImageView, this);
    }



}
