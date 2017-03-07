package com.project.shehel.ohho;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * Created by shehel on 2/26/17.
 */
public class FloatingWindow extends Service{
    private static final String TAG = "AND";
    private WindowManager wm;
    private LinearLayout ll;
    public IBinder onBind(Intent intent) {
        return null;
    }
    String message;
    public int onStartCommand (Intent intent, int flags, int startId) {

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);
        
        LinearLayout.LayoutParams llParameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setBackgroundColor(Color.argb(20, 255, 0, 0));
        ll.setLayoutParams(llParameters);

        //Set parameters for window manager
        WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(400, 150, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        parameters.x = 0;
        parameters.y = 0;
        parameters.gravity = Gravity.CENTER | Gravity.CENTER;

        wm.addView(ll, parameters);



        String value = intent.getStringExtra("id.Message");
            // do something with the value here
        Log.i(TAG, "Message "+value );

        return START_NOT_STICKY;
    }



}


