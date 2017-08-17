package com.makersmakingchange.maker_assist.Activity;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.makersmakingchange.maker_assist.R;

import java.util.List;

/**************************************************
 **************Makers Making Change****************
 **************************************************
 ****Developed by Milad Hajihassan on 3/28/2017.***
 **************************************************
 **************************************************/

public class MainActivity extends AppCompatActivity {
    Button settingsBeginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Setup layout
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_main);
        //Set the button to enter settings activity
        settingsBeginButton = (Button) findViewById(R.id.main_begin_settings_button);
        settingsBeginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });
    }

}