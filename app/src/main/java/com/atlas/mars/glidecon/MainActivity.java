package com.atlas.mars.glidecon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Администратор on 7/22/15.
 */
public class MainActivity extends AppCompatActivity  {
    private static final String TAG = "MainActivityLog";
    ScreenParams screenParams;
    ImageView rotateImageView;//вращающаяся картинка
    ImageView snosImageView;//вращающаяся сносы
    FrameLayout rotationAreaFrame; // область по которой слушается движение
    LinearLayout linearLayoutInfo; // контейнер с текстом
    float rotationImageAngle = 0;
    MoveEvents moveEvents;
    static MyReceiver myReceiver;
    MyJQuery myJQuery;

    TextView speedTextView , altitudeTextView, varioTextView , qualityTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        screenParams = new ScreenParams(this);
        new DataBaseHelper(this);
        onActionBarCreate();

        init();

    }

    @Override
    public boolean  onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        ActionBar actionBar = getSupportActionBar();
        // actionBar.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.actionbar_background, null));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent questionIntent;
        switch (item.getItemId()){
            case R.id.action_settings:
                questionIntent = new Intent(this, SettingActivity.class);
                startActivityForResult(questionIntent, 0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //Todo нажато сохранение
            }
        }

    }

    @Override
    protected void onPause() {
        moveEvents.onPause();
        unregisterReceiver(myReceiver);
        stopService(new Intent(this, MyService.class));
        super.onPause();

    }




    @Override
    protected void onResume() {
        onCreateMyReceiver();
        startService(new Intent(this, MyService.class));
        super.onResume();
    }

    private void onCreateMyReceiver(){
       /* if(myReceiver!=null){
            unregisterReceiver(myReceiver);
        }*/
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

        linearLayoutInfo = (LinearLayout)findViewById(R.id.linearLayoutInfo);
        setSisze();
        speedTextView = (TextView)findViewById(R.id.speedTextView);
        altitudeTextView = (TextView)findViewById(R.id.altitudeTextView);
        varioTextView = (TextView)findViewById(R.id.varioTextView);
        qualityTextView = (TextView)findViewById(R.id.qualityTextView);


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
    private void setSisze(){
        ViewTreeObserver observer= linearLayoutInfo.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                myJQuery = new MyJQuery();
                ArrayList<View> textList = myJQuery.findViewByTagClass(linearLayoutInfo, TextView.class);
                int H = linearLayoutInfo.getHeight();
                double hText = (H/3) - 3*(5*screenParams.density)-(10*screenParams.density);

                for( View v : textList){
                    TextView textView = (TextView)v;
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)hText);
                }


                // buttonReload.setLayoutParams(new  FrameLayout.LayoutParams (buttonReload.getHeight(),buttonReload.getHeight() ));
                // buttonMenu.setLayoutParams(new  FrameLayout.LayoutParams ((int)(buttonMenu.getHeight()/2),buttonMenu.getHeight() ));
            }
        });
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // int datapassed = arg1.getIntExtra("DATAPASSED", 0);

            String text = Double.toString(arg1.getExtras().getDouble("lat"));
            String speed = Double.toString(arg1.getExtras().getDouble("speed"));
            String altitude= Double.toString(arg1.getExtras().getDouble("altitude"));
            double _vario = arg1.getExtras().getDouble("vario");

            String vario;
            if(_vario<0){
                vario =  ""+Double.toString(_vario);
                varioTextView.setTextColor(getResources().getColor(R.color.red));
            }else{
                vario =  "+ "+Double.toString(_vario);
                varioTextView.setTextColor(getResources().getColor(R.color.green));
            }
            String quality = Double.toString(arg1.getExtras().getDouble("quality"));

            Log.d(TAG, speed);
            speedTextView.setText(speed);
            altitudeTextView.setText(altitude);
            varioTextView.setText(vario);
            qualityTextView.setText(quality);
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
