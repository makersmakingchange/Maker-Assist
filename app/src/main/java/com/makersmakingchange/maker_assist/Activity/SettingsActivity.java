package com.makersmakingchange.maker_assist.Activity;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.makersmakingchange.maker_assist.BuildConfig;
import com.makersmakingchange.maker_assist.MakerAssistService;
import com.makersmakingchange.maker_assist.R;

/**************************************************
 **************Makers Making Change****************
 **************************************************
 ****Developed by Milad Hajihassan on 3/28/2017.***
 **************************************************
 **************************************************/

public class SettingsActivity extends AppCompatPreferenceActivity {
    public SharedPreferences prefs=null;
    public SharedPreferences.Editor editor=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, new GeneralPreferenceFragment());
        transaction.commit();
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView actionBarTitle = (TextView) findViewById(R.id.actionbar_header_title);
        actionBarTitle.setText(getString(R.string.actionbar_title_settings));


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("ValidFragment")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public class GeneralPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        public SwitchPreference overlaySwitch;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
            overlaySwitch = (SwitchPreference) findPreference("overlay_switch");
            prefs = getSharedPreferences("MAKER_ASSIST", 0);
            editor = prefs.edit();

            //Use overlaySwitch to start and stop MakerAssistService
            if (overlaySwitch != null) {
                overlaySwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference arg0, Object isVibrateOnObject) {
                        boolean isOverlayOn = (Boolean) isVibrateOnObject;
                        if (isOverlayOn) {
                            //Start MakerAssistService
                            startService(new Intent(getApplication(), MakerAssistService.class));
                            editor.putBoolean("is_intro_displayed", false);
                            editor.putBoolean("is_overlay_on", true);
                            editor.commit();
                        }
                        else{
                            stopService(new Intent(getApplication(), MakerAssistService.class));
                            editor.putBoolean("is_overlay_on", false);
                            editor.commit();
                        }
                        return true;
                    }
                });
            }
        }
        private boolean appFirstTimeRun() {
            //Check if this is the first time running the App
            SharedPreferences appPreferences = getSharedPreferences("MAKER_ASSIST", 0);
            int appCurrentBuildVersion = BuildConfig.VERSION_CODE;
            int appLastBuildVersion = appPreferences.getInt("app_first_time", 0);
            //Log.d("appPreferences", "appCurrentBuildVersion = " + appCurrentBuildVersion);
            //Log.d("appPreferences", "app_first_time = " + appLastBuildVersion);
            if (appLastBuildVersion == appCurrentBuildVersion ) {
                return false;
            } else {
                appPreferences.edit().putInt("app_first_time",
                        appCurrentBuildVersion).apply();
                return true;
            }
        }
        public  boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
            ComponentName expectedComponentName = new ComponentName(context, accessibilityService);
            String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(),  Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (enabledServicesSetting == null)
                return false;
            TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
            colonSplitter.setString(enabledServicesSetting);

            while (colonSplitter.hasNext()) {
                String componentNameString = colonSplitter.next();
                ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

                if (enabledService != null && enabledService.equals(expectedComponentName))
                    return true;
            }
            return false;
        }
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            overlaySwitch = (SwitchPreference) findPreference("overlay_switch");
            prefs = getSharedPreferences("MAKER_ASSIST", 0);

            //If it's the first time running the App then set status of switch to off
            if(appFirstTimeRun()){
                overlaySwitch.setChecked(false);
            }
            //else use is_overlay_on boolean in SharedPreferences to set status of switch
            else {
                overlaySwitch.setChecked(prefs.getBoolean("is_overlay_on", false));

            }
            //Check if AccessibilityService is enabled and turn on overlay switch if it's enabled
           if(isAccessibilityServiceEnabled(SettingsActivity.this,MakerAssistService.class)) {
                overlaySwitch.setChecked(true);
                //startService(new Intent(getApplication(), MakerAssistService.class));
            }
            //Turn on overlay switch if AccessibilityService is disabled
            else{
                overlaySwitch.setChecked(false);
                stopService(new Intent(getApplication(), MakerAssistService.class));
            }
        }
        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        }
    }

}
