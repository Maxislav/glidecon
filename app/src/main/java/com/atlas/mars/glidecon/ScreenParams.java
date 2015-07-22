package com.atlas.mars.glidecon;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * Created by Администратор on 7/22/15.
 */
public class ScreenParams {
    public  int density;
    public  int widthPixels;
    public  int heightPixels;
    public ScreenParams(Activity activity){
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        density = displayMetrics.densityDpi;
        widthPixels = displayMetrics.widthPixels;
        heightPixels = displayMetrics.heightPixels;
    }
}
