package com.atlas.mars.glidecon;

import android.app.Activity;

/**
 * Created by mars on 7/23/15.
 */
public class CenterR  extends  ScreenParams{

    public int X;
    public int Y;

    public CenterR(Activity activity) {
        super(activity);
        this.X = widthPixels / 2;
        this.Y = heightPixels - (widthPixels / 2);
    }
}
