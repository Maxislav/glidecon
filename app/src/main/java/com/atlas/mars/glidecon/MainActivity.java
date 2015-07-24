package com.atlas.mars.glidecon;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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
    MoveEvents moveEvents;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        screenParams = new ScreenParams(this);



        onActionBarCreate();
        init();


    }

    @Override
    protected void onPause() {
        moveEvents.onPause();
        super.onPause();


    }

    private void onActionBarCreate(){
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.action_bar, null);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setCustomView(v);
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
        moveEvents = new MoveEvents(rotationAreaFrame, rotateImageView, snosImageView, this);
    }




}
