package com.atlas.mars.glidecon;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.phrase.Phrase;

import java.util.HashMap;

/**
 * Created by mars on 5/5/15.
 */
public class SettingActivity extends AppCompatActivity {
    String web;
    DataBaseHelper db;
    String START_ALTITUDE, INTERVAL_UPDATE;
    EditText edTextStartAltitude, edTextIntervalUpdate;
    boolean isInit = false;
    public  static final String PROTOCOL_TYPE = "protocolType";
    private int spinnerPosition;
    HashMap<String, String> mapSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        db = new DataBaseHelper(this);
        mapSetting = db.mapSetting;
        onActionBarCreate();
        init();
    }

    @Override
    public boolean  onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        ActionBar actionBar = getSupportActionBar();
       // actionBar.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.actionbar_background, null));
        return true;
    }

    private void onActionBarCreate(){
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.action_bar, null);
        TextView title = (TextView)v.findViewById(R.id.title);
        title.setText(R.string.app_settings);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setCustomView(v);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                Intent answerIntent = new Intent();
                saveSetting();
                setResult(RESULT_OK, answerIntent);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void init(){
        edTextStartAltitude = (EditText)findViewById(R.id.edTextStartAltitude);
        edTextIntervalUpdate = (EditText)findViewById(R.id.edTextIntervalUpdate);
        START_ALTITUDE = mapSetting.get(db.START_ALTITUDE);
        INTERVAL_UPDATE = mapSetting.get(db.INTERVAL_UPDATE);


        if(START_ALTITUDE !=null){
            edTextStartAltitude.setText(START_ALTITUDE);
        }
        if(INTERVAL_UPDATE !=null){
            edTextIntervalUpdate.setText(INTERVAL_UPDATE);
        }
        web = getString(R.string.web);


        onDraw();
    }
    private void onDraw(){
        parsePasteWeb((WebView)findViewById(R.id.wLogin), getString(R.string.start_altitude) );
        parsePasteWeb((WebView)findViewById(R.id.wPass), getString(R.string.interval_update) );

    }
    private void parsePasteWeb(WebView browser, String put ){
        CharSequence formatted = Phrase.from(web).put("content", put).format();
        browser.loadData(formatted.toString(), "text/html; charset=UTF-8", null);
    }
    private void saveSetting(){
        START_ALTITUDE = edTextStartAltitude.getText().toString();
        INTERVAL_UPDATE = edTextIntervalUpdate.getText().toString();


        if(START_ALTITUDE !=null){
            mapSetting.put(db.START_ALTITUDE, START_ALTITUDE);
        }
        if(INTERVAL_UPDATE !=null){
            mapSetting.put(db.INTERVAL_UPDATE, INTERVAL_UPDATE);
        }
        db.saveSetting();
    }



    public void toastShow(String str) {
        Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
    }
}
