package com.atlas.mars.glidecon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
    static MyReceiver myReceiver;

    TextView speedTextView;


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
        stopService(new Intent(this, MyService.class));



    }

    @Override
    protected void onResume() {
        onCreateMyReceiver();
        startService(new Intent(this, MyService.class));
        super.onResume();
    }

    private void onCreateMyReceiver(){
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyService.LOCATION);
        registerReceiver(myReceiver, intentFilter);
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
        speedTextView = (TextView)findViewById(R.id.speedTextView);
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

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // int datapassed = arg1.getIntExtra("DATAPASSED", 0);

            String text = Double.toString(arg1.getExtras().getDouble("lat"));
            String speed = Double.toString(arg1.getExtras().getDouble("speed"));
            Log.d(TAG, speed);
            speedTextView.setText(speed);
           // textView.setText(text);
            /*
            Toast.makeText(MainActivity.this,
                    text,
                    Toast.LENGTH_LONG)
                    .show();*/
            //   textView.setText();
        }
    }


}
