package com.makersmakingchange.maker_assist;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Region;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.makersmakingchange.maker_assist.Adapter.QuickMenuAdapter;
import com.makersmakingchange.maker_assist.Manager.FeatureItem;
import com.makersmakingchange.maker_assist.Manager.IntroItem;

import java.util.ArrayList;

/**************************************************
 **************Makers Making Change****************
 **************************************************
 ****Developed by Milad Hajihassan on 3/28/2017.***
 **************************************************
 **************************************************/

public class MakerAssistService  extends AccessibilityService {

    public SharedPreferences prefs=null;
    public SharedPreferences.Editor editor=null;
    public static boolean isServiceEnabled = false;
    public static boolean isAdminActive = false;
    DevicePolicyManager devicePolicyManager;
    ComponentName adminComponent;
    private WindowManager windowManager;
    private LinearLayout quickButtonLayout;
    private LinearLayout quickMenuLayout;
    private LinearLayout swipeMenuLayout;
    private LinearLayout swipePointLayout;
    private LinearLayout volumeMenuLayout;
    private LinearLayout zoomMenuLayout;
    private LinearLayout modeButtonLayout;
    private LinearLayout modeDescriptionLayout;
    private LinearLayout introLayout;

    WindowManager.LayoutParams quickButtonParams;
    WindowManager.LayoutParams quickMenuParams;
    WindowManager.LayoutParams swipeMenuParams;
    WindowManager.LayoutParams swipePointParams;
    WindowManager.LayoutParams volumeMenuParams;
    WindowManager.LayoutParams zoomMenuParams;
    WindowManager.LayoutParams modeButtonParams;
    WindowManager.LayoutParams modeDescriptionParams;
    WindowManager.LayoutParams introParams;

    public int currentApiVersion;
    public static Context mContext;
    public boolean stayAwake;
    public String OVERLAY;
    public boolean descriptionOverlay;
    public int quickMenuPageNum=1;
    public int introPageNum=1;
    public int quickMenuNumColumns=2;
    public ArrayList<IntroItem> introList=new ArrayList<>();
    public ArrayList<FeatureItem> featureList=new ArrayList<>();
    public ArrayList<FeatureItem> featureSelected=new ArrayList<>();
    AudioManager audioManager;

    public PowerManager.WakeLock wakeLock;

    public Typeface fontAwesome;
    public Typeface fontLipsync;

    @SuppressLint("RtlHardcoded")
    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mContext=this;

        currentApiVersion = android.os.Build.VERSION.SDK_INT;

        //Current Overlay layout
        OVERLAY="";

        //Create a Typeface from a font for button icons
        fontAwesome = Typeface.createFromAsset( getAssets(), "fontawesome-webfont.ttf" );
        fontLipsync = Typeface.createFromAsset( getAssets(), "lipsync-icons20.ttf" );

        stayAwake=false;
        descriptionOverlay=true;
        //Add all the information in into layout
        introList.add(new IntroItem(R.string.intro_text_title1,R.string.intro_text_description1,R.string.intro_text_next_1));
        introList.add(new IntroItem(R.string.intro_text_title2,R.string.intro_text_description2,R.string.intro_text_next_2));
        introList.add(new IntroItem(R.string.intro_text_title3,R.string.intro_text_description3,R.string.intro_text_next_3));
        introList.add(new IntroItem(R.string.intro_text_title4,R.string.intro_text_description4,R.string.intro_text_next_4));
        //Add all the features and their font icons to featureList arraylist
        featureList.add(new FeatureItem("Back",R.string.icon_back));
        featureList.add(new FeatureItem("Home",R.string.icon_home));
        featureList.add(new FeatureItem("Recent",R.string.icon_display));
        featureList.add(new FeatureItem("Notification",R.string.icon_notification));
        featureList.add(new FeatureItem("Stay On",R.string.icon_awake_on));
        featureList.add(new FeatureItem("Volume",R.string.icon_volume));
        featureList.add(new FeatureItem("Lock phone",R.string.icon_lock));
        featureList.add(new FeatureItem("Settings",R.string.icon_settings));
        if (currentApiVersion >= Build.VERSION_CODES.LOLLIPOP) {
            featureList.add(new FeatureItem("Power", R.string.icon_power));
        }
        //Add all the features that exist in Android 7 or plus
        if (currentApiVersion >= Build.VERSION_CODES.N){
            featureList.add(new FeatureItem("Zoom",R.string.icon_zoomset));
            featureList.add(new FeatureItem("Swipe",R.string.icon_swipe));
        }
        //Setup the intro layout
        setupIntroLayout(0,0);
        //Setup the maker assist floating quick button layout
        setupQuickButtonLayout();

        //Setup the swipe point layout
        setupSwipePointLayout(400,400);

        //Setup the mode button layout
        setupModeLayout(20,100);

        //Setup the volume adjustment menu layout
        setupVolumeMenuLayout(0,0);

        //Setup the zoom menu layout
        setupZoomMenuLayout(0,200);

        //Setup power manager for wakelock
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Loneworker - FULL WAKE LOCK");
        isServiceEnabled=false;

        prefs = getSharedPreferences("MAKER_ASSIST", 0);
        editor = prefs.edit();
        //if(prefs.getBoolean("is_overlay_on", false)) {
            if (!prefs.getBoolean("is_intro_displayed", false)) {
                // Show intro layout
                OVERLAY="introMenu";
                windowManager.addView(introLayout, introParams);
                editor.putBoolean("is_intro_displayed", true);
                editor.commit();
            } else {
                //Show QuickButton if the user have gone over introduction
                OVERLAY = "quickButton";
                //add quickButton layout as default Layouts
                windowManager.addView(quickButtonLayout, quickButtonParams);
            }
       // }

    }
    private void setupLayout(String layoutName,int xParam,int yParam) {

        /********************************
         * Setup a LinearLayout
         *******************************/

        LinearLayout newLinearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        //create a LayoutParams and make it wrap the contents and stay as top layout even in lock screen
        WindowManager.LayoutParams newLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT);
        //Define default location of LinearLayout on screen using LayoutParams
        newLayoutParams.x = xParam;
        newLayoutParams.y = yParam;

        //Set LayoutParams of LinearLayout
        newLinearLayout.setLayoutParams(layoutParams);

        //Define the gravity rules for each LinearLayout
        if(layoutName=="quickButton") {
            newLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            quickButtonLayout = newLinearLayout;
            quickButtonParams = newLayoutParams;
            newLayoutParams.dimAmount = 0.00f;
        }
        else if(layoutName=="quickMenu") {
            newLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            quickMenuLayout = newLinearLayout;
            quickMenuParams = newLayoutParams;
            newLayoutParams.dimAmount = 0.00f;
        }
        else if(layoutName=="introMenu") {
            newLayoutParams.gravity = Gravity.CENTER;
            introLayout = newLinearLayout;
            introParams = newLayoutParams;
            newLayoutParams.dimAmount = 0.65f;
        }
        else if(layoutName=="modeButton") {
            newLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            modeButtonLayout = newLinearLayout;
            modeButtonParams = newLayoutParams;
            newLayoutParams.dimAmount = 0.00f;
        }
        else if(layoutName=="modeDescription") {
            newLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            modeDescriptionLayout = newLinearLayout;
            modeDescriptionParams = newLayoutParams;
            newLayoutParams.dimAmount = 0.00f;
        }
        else if(layoutName=="zoomMenu") {
            newLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            zoomMenuLayout = newLinearLayout;
            zoomMenuParams = newLayoutParams;
            newLayoutParams.dimAmount = 0.00f;
        }
        else if(layoutName=="swipeMenu") {
            newLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            swipeMenuLayout = newLinearLayout;
            swipeMenuParams = newLayoutParams;
            newLayoutParams.dimAmount = 0.00f;
        }
        else if(layoutName=="swipePoint") {
            newLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            swipePointLayout = newLinearLayout;
            swipePointParams = newLayoutParams;
            newLayoutParams.dimAmount = 0.00f;
        }
        else if(layoutName=="volumeMenu") {
            newLayoutParams.gravity = Gravity.CENTER;
            volumeMenuLayout = newLinearLayout;
            volumeMenuParams = newLayoutParams;
            newLayoutParams.dimAmount = 0.00f;
        }
    }

    private void setupIntroLayout(int xParam,int yParam) {

        /*****************************
         * Initialize intro Layout
         ****************************/

        introPageNum=0;
        //Setup intro Layout and its initial location on screen
        setupLayout("introMenu",xParam,yParam);

        //Set intro XML Layout
        LayoutInflater introLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        introLayout = (LinearLayout) introLayoutInflater.inflate(R.layout.overlay_intro, null);


        //Button to close introLayout
        final ImageButton introCloseButton = (ImageButton) introLayout.findViewById(R.id.intro_button_close);
        introCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Remove introLayout from windowManager
                windowManager.removeViewImmediate(introLayout);
                //Add QuickButton Layout to windowManager
                OVERLAY="quickButton";
                //add quickButton layout as default Layouts
                windowManager.addView(quickButtonLayout, quickButtonParams);
            }

        });

        final TextView textViewIntroTitle = (TextView) introLayout.findViewById(R.id.intro_title);
        final TextView textViewIntroDescription = (TextView) introLayout.findViewById(R.id.intro_description);
        final Button nextButton = (Button) introLayout.findViewById(R.id.intro_button_next);
        final Button previousButton = (Button) introLayout.findViewById(R.id.intro_button_previous);

        //Perform previousButton action to update the contents to previous page
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(introPageNum>0){
                    introPageNum--;
                    if(introPageNum==0){
                        previousButton.setVisibility(View.INVISIBLE);
                    }
                    else {
                        previousButton.setBackground(getResources().getDrawable(R.drawable.rectangle_rounded_green));
                    }

                    textViewIntroTitle.setText(introList.get(introPageNum).getIntroTitle());
                    textViewIntroDescription.setText(introList.get(introPageNum).getIntroDescription());
                    nextButton.setText(introList.get(introPageNum).getIntroNextButtonText());

                }

            }

        });

        //Perform nextButton action to update the contents to next page
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(introPageNum<(introList.size())-1){
                    introPageNum++;
                    previousButton.setVisibility(View.VISIBLE);
                    textViewIntroTitle.setText(introList.get(introPageNum).getIntroTitle());
                    textViewIntroDescription.setText(introList.get(introPageNum).getIntroDescription());
                    nextButton.setText(introList.get(introPageNum).getIntroNextButtonText());
                    nextButton.setBackground(getResources().getDrawable(R.drawable.rectangle_rounded_green));
                }

                else {
                    //Remove introLayout from windowManager
                    windowManager.removeViewImmediate(introLayout);
                    //Add QuickButton Layout to windowManager
                    OVERLAY="quickButton";
                    //add quickButton layout as default Layouts
                    windowManager.addView(quickButtonLayout, quickButtonParams);
                }

            }
        });

    }

    private void setupQuickButtonLayout() {

        /********************************
         * Initialize quickButton Layout
         *******************************/

        //Setup QuickButton and its initial location on screen
        setupLayout("quickButton",20,100);

        //Set the QuickButton XML Layout
        LayoutInflater quickMenuLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        quickButtonLayout = (LinearLayout) quickMenuLayoutInflater.inflate(R.layout.button_quickmenu, null);

        //Set the touch listener rules to Drag quickButton or open quickMenu Layout
        quickButtonLayout.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean stayedWithinClickDistance;
            //Max allowed duration for a "click", in milliseconds.
            private static final int MAX_CLICK_DURATION = 2000;
            // Max allowed distance to move during a "click", in DP.
            private static final int MAX_CLICK_DISTANCE = 15;
            //PressStartTime is used to define when an action down event was performed and
            //It is used to calculate touch press down duration
            private long pressStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        stayedWithinClickDistance = true;
                        pressStartTime = System.currentTimeMillis();
                        //initial location of quickButton
                        initialX = quickButtonParams.x;
                        initialY = quickButtonParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        long pressDuration = System.currentTimeMillis() - pressStartTime;
                        //Open QuickMenuWindow if the quickButton is clicked
                        //quickButton was pressed down less than 2 second and moved less than 15 dp
                        if (pressDuration < MAX_CLICK_DURATION && stayedWithinClickDistance) {
                            windowManager.removeView(quickButtonLayout);
                            setupQuickMenuLayout(quickButtonParams.x,quickButtonParams.y);
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Drag quickButton to relocate the quickButton if moved more than 15 dp
                        if (distance((initialX + (int) (event.getRawX() - initialTouchX)),
                                (initialY + (int) (event.getRawY() - initialTouchY)),
                                quickButtonParams.x,
                                quickButtonParams.y) > MAX_CLICK_DISTANCE) {
                            stayedWithinClickDistance = false;
                            quickButtonParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                            quickButtonParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                            //Update quickButton Layout location
                            windowManager.updateViewLayout(quickButtonLayout, quickButtonParams);
                            return true;
                        }
                }
                return false;
            }
        });
    }
    private void setupQuickMenuLayout(int xParam,int yParam) {

        /********************************
         * Initialize QuickMenu Layout
         *******************************/

        //Set the initial page of quickMenu features
        quickMenuPageNum=1;

        //Setup QuickMenu and its initial location on screen
        setupLayout("quickMenu",xParam,yParam);
        //Set the QuickMenu XML Layout
        LayoutInflater quickMenuLayoutParamsInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        quickMenuLayout = (LinearLayout) quickMenuLayoutParamsInflater.inflate(R.layout.overlay_quickmenu, null);

        //Close QuickMenu and open QuickButton if clicked outside quickMenuLayout
        quickMenuLayout.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_OUTSIDE:
                        windowManager.removeViewImmediate(quickMenuLayout);
                        OVERLAY="quickButton";
                        windowManager.addView(quickButtonLayout,quickButtonParams);
                        return true;

                }
                return false;
            }
        });

        //Define GridView in quickMenuLayout
        final GridView quickMenuGridView = (GridView) quickMenuLayout.findViewById(R.id.grid_view_quickmenu);

        final TextView quickMenuPageNumText = (TextView) quickMenuLayout.findViewById(R.id.page_quick_menu);

        //Update the FeatureList using current quickMenuPageNum
        refreshFeatureList(quickMenuPageNum);
        quickMenuPageNumText.setText(String.valueOf(quickMenuPageNum)+"/"+String.valueOf((int) Math.ceil((featureList.size()/4.0))));

        //Set QuickMenuAdapter as GridView custom adapter
        final QuickMenuAdapter gridViewAdapter= new QuickMenuAdapter(this, R.layout.list_item_quickmenu, featureSelected);
        quickMenuGridView.setAdapter(gridViewAdapter);

        //Set number of columns on GridView
        quickMenuGridView.setNumColumns(quickMenuNumColumns);

        //Set QuickMenu Width
        setQuickMenuWidth(quickMenuGridView,gridViewAdapter,quickMenuNumColumns);

        //Define quickMenuPrevious page Button resource id and set its icon
        final Button quickMenuPreviousButton = (Button) quickMenuLayout.findViewById(R.id.button_quickmenu_previous);
        quickMenuPreviousButton.setTypeface(fontAwesome);
        quickMenuPreviousButton.setText(R.string.icon_up);

        //Define quickMenuNextButton page Button resource id and set its icon
        final Button quickMenuNextButton = (Button) quickMenuLayout.findViewById(R.id.button_quickmenu_next);
        quickMenuNextButton.setTypeface(fontAwesome);
        quickMenuNextButton.setText(R.string.icon_down);

        quickMenuNextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Update adapter to next page if it's not last page and update it to first page if this is already last page
                gridViewAdapter.notifyDataSetChanged();
                quickMenuPageNum = ((quickMenuPageNum == Math.ceil((featureList.size()/4.0))) ? quickMenuPageNum=1 : quickMenuPageNum+1);
                refreshFeatureList(quickMenuPageNum);
                quickMenuGridView.setAdapter(gridViewAdapter);
                //Update page number text
                quickMenuPageNumText.setText(String.valueOf(quickMenuPageNum)+"/"+(int) Math.ceil(featureList.size()/4.0));

            }
        });

        quickMenuPreviousButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Update adapter to previous page if it's not first page and update it to last page if this is already first page
                gridViewAdapter.notifyDataSetChanged();
                quickMenuPageNum = ((quickMenuPageNum == 1) ? quickMenuPageNum= (int) Math.ceil(featureList.size()/4.0) : quickMenuPageNum-1);
                refreshFeatureList(quickMenuPageNum);
                quickMenuGridView.setAdapter(gridViewAdapter);
                //Update page number text
                quickMenuPageNumText.setText(String.valueOf(quickMenuPageNum)+"/"+(int) Math.ceil(featureList.size()/4.0));

            }
        });

        //Performing quickMenuGridView feature actions
        quickMenuGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Turn on descriptionOverlay message
                descriptionOverlay=true;
                adminComponent = new ComponentName(MakerAssistService.this, DeviceAdmin.class);
                devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                isAdminActive = devicePolicyManager.isAdminActive(adminComponent);

                //Check if device admin permission is enabled
                //Ask user to enable it if it's not enabled already
                if(!isAdminActive){
                    //Log.d("isDeviceAdmin:", String.valueOf(isAdminActive));

                    //Enable descriptionOverlay and show the layout with message to enable Admin permission
                    descriptionOverlay=true;
                    windowManager.addView(modeDescriptionLayout, modeDescriptionParams);
                    windowManager.addView(modeButtonLayout, modeButtonParams);
                    updateModeLayout("Admin", R.string.icon_settings, R.string.mode_description_admin);
                    //Set the current OVERLAY
                    OVERLAY = "permissionDescriptionMenu";

                    //Open DeviceAdminSettings
                    adminComponent = new ComponentName(MakerAssistService.this, DeviceAdmin.class);
                    Intent adminIntent = new Intent(devicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    adminIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    adminIntent.setComponent(new ComponentName("com.android.settings","com.android.settings.DeviceAdminSettings"));
                    startActivity(adminIntent);
                }
                //Check if Maker Assist service is enabled as accessibility service
                //Ask user to enable it if it's not enabled already
                else if(!isServiceEnabled){
                    //Log.d("isServiceEnabled:", String.valueOf(isServiceEnabled));

                    //Enable descriptionOverlay and show the layout with message to enable accessibility service
                    descriptionOverlay=true;
                    windowManager.addView(modeDescriptionLayout, modeDescriptionParams);
                    windowManager.addView(modeButtonLayout, modeButtonParams);
                    updateModeLayout("Service", R.string.icon_settings, R.string.mode_description_service);
                    //Set the current OVERLAY
                    OVERLAY = "permissionDescriptionMenu";

                    //Open AccessibilitySettings
                    Intent serviceIntent = new Intent();
                    serviceIntent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(serviceIntent);
                }
                else { //if It's enabled as accessibility service and deviceAdmin then let user perform feature actions
                    FeatureItem selectedFeature = featureSelected.get(position);
                    //parent.getItemAtPosition(position).toString()
                    switch (selectedFeature.getFeatureName()) {
                        case "Back":
                            //Log.d("quickMenu button:", "Back");
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                                    windowManager.addView(quickButtonLayout, quickButtonParams);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "Home":
                            //Log.d("quickMenu button:", "Home");
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                                    windowManager.addView(quickButtonLayout, quickButtonParams);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "Recent":
                            //Log.d("quickMenu button:", "Recent");
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                                    windowManager.addView(quickButtonLayout, quickButtonParams);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "Notification":
                            //Log.d("quickMenu button:", "Notification");
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
                                    windowManager.addView(quickButtonLayout, quickButtonParams);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "Stay On":
                        case "Stay Off":
                            if (wakeLock.isHeld()) {
                                featureList.set(featureList.indexOf(selectedFeature), new FeatureItem("Stay On", R.string.icon_awake_on));
                                wakeLock.release();
                            } else {
                                featureList.set(featureList.indexOf(selectedFeature), new FeatureItem("Stay Off", R.string.icon_awake_off));
                                wakeLock.acquire();
                            }

                            windowManager.addView(quickButtonLayout, quickButtonParams);
                            break;
                        case "Volume":
                            //Log.d("quickMenu button:", "Volume");
                            //Update OVERLAY
                            OVERLAY = "volumeMenu";
                            //Add volumeMenuLayout and modeDescriptionLayout
                            windowManager.addView(volumeMenuLayout, volumeMenuParams);
                            windowManager.addView(modeDescriptionLayout, modeDescriptionParams);
                            windowManager.addView(modeButtonLayout, modeButtonParams);
                            updateModeLayout("Volume", R.string.icon_volume, R.string.mode_description_volume);
                            break;
                        case "Lock phone":
                            //Log.d("quickMenu button:", "Lock phone");
                            try {
                                devicePolicyManager.lockNow();
                            } catch (SecurityException e) {
                                Intent lockIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                                lockIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Lock screen");
                                startActivity(lockIntent);
                            }
                            windowManager.addView(quickButtonLayout, quickButtonParams);
                            break;
                        case "Settings":
                            //Log.d("quickMenu button:", "Settings");
                            Intent settingsIntent = new Intent();
                            settingsIntent.setAction(Settings.ACTION_SETTINGS);
                            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(settingsIntent);
                            windowManager.addView(quickButtonLayout, quickButtonParams);
                            break;
                        case "Power":
                            //Log.d("quickMenu button:", "Power");
                            try {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
                                    windowManager.addView(quickButtonLayout, quickButtonParams);
                                }
                                else
                                    {
                                        /*
                                    Intent i = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                                i.putExtra("android.intent.extra.KEY_CONFIRM", false);
                                i.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                    Intent powerIntent = new Intent(Intent.ACTION_REBOOT);
                                        powerIntent.putExtra("android.intent.extra.KEY_CONFIRM", true);
                                        powerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(powerIntent);
                                    */
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "Zoom":
                            //Log.d("quickMenu button:", "Zoom");
                            //Update OVERLAY
                            OVERLAY = "zoomMenu";
                            //Add zoomMenuLayout and modeDescriptionLayout
                            windowManager.addView(modeDescriptionLayout, modeDescriptionParams);
                            windowManager.addView(modeButtonLayout, modeButtonParams);
                            windowManager.addView(zoomMenuLayout, zoomMenuParams);
                            updateModeLayout("Zoom", R.string.icon_zoomset, R.string.mode_description_zoom);
                            break;
                        case "Swipe":
                            //Log.d("quickMenu button:", "Swipe");
                            //Update OVERLAY
                            OVERLAY = "SwipePoint";
                            //Add swipePointLayout and modeDescriptionLayout
                            windowManager.addView(swipePointLayout, swipePointParams);
                            windowManager.addView(modeDescriptionLayout, modeDescriptionParams);
                            windowManager.addView(modeButtonLayout, modeButtonParams);
                            updateModeLayout("Swipe", R.string.icon_swipe, R.string.mode_description_swipe);
                            break;
                    }
                }
                //Remove quickMenuLayout once the feature is performed
                windowManager.removeViewImmediate(quickMenuLayout);

            }
        });
        //Add QuickMenuLayout after quickMenuLayout was removed
        OVERLAY="quickMenu";
        windowManager.addView(quickMenuLayout, quickMenuParams);
    }

    private void updateModeLayout(String modeName,int modeIcon,int modeDescription) {

        /********************************
         * Update quick mode Layout
         *******************************/

        //Update mode description Text
        final TextView descriptionText = (TextView) modeDescriptionLayout.findViewById(R.id.mode_description_text);
        descriptionText.setText(modeDescription);

        //Update modeButtonLayout icon
        final TextView modeTextIcon = (TextView) modeButtonLayout.findViewById(R.id.textView_swipe_icon);
        modeTextIcon.setTypeface(fontLipsync);
        modeTextIcon.setText(modeIcon);

        //Update modeButtonLayout
        windowManager.updateViewLayout(modeButtonLayout, modeButtonParams);
    }

    private void setupSwipePointLayout(int xParam,int yParam) {

        /*****************************
         * Initialize SwipePoint Layout
         ****************************/
        //Setup SwipePoint and its initial location on screen
        setupLayout("swipePoint",xParam,yParam);

        //Set the QuickButton XML Layout
        LayoutInflater swipePointLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        swipePointLayout = (LinearLayout) swipePointLayoutInflater.inflate(R.layout.button_swipe_point, null);

        //Set the touch listener rules to Drag swipePointLayout
        swipePointLayout.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean stayedWithinClickDistance;
            //Max allowed duration for a "click", in milliseconds.
            private static final int MAX_CLICK_DURATION = 1000;
            // Max allowed distance to move during a "click", in DP.
            private static final int MAX_CLICK_DISTANCE = 15;
            //pressStartTime is used to define when an action down event was performed and
            //It is used to calculate touch press down duration
            private long pressStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        stayedWithinClickDistance = true;
                        pressStartTime = System.currentTimeMillis();
                        //Initial location of swipePointLayout on touch down event
                        initialX = swipePointParams.x;
                        initialY = swipePointParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        long pressDuration = System.currentTimeMillis() - pressStartTime;
                        //Open SwipeMenuWindow if the swipePoint is clicked
                        //quickButton was pressed down 1 second and moved less than 15 dp
                        if (pressDuration < MAX_CLICK_DURATION && stayedWithinClickDistance) {
                            setupSwipeMenuLayout(swipePointParams.x,swipePointParams.y);
                            windowManager.removeViewImmediate(swipePointLayout);
                            windowManager.addView(swipeMenuLayout,swipeMenuParams);
                            OVERLAY="swipeMenu";
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Drag quickButton to relocate the quickButton
                        if (distance((initialX + (int) (event.getRawX() - initialTouchX)),
                                (initialY + (int) (event.getRawY() - initialTouchY)),
                                swipePointParams.x,
                                swipePointParams.y)  > MAX_CLICK_DISTANCE) {
                            stayedWithinClickDistance = false;
                            swipePointParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                            swipePointParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                            //Update quickButton Layout location
                            windowManager.updateViewLayout(swipePointLayout, swipePointParams);
                            return true;
                        }
                }
                return false;
            }
        });

    }
    private void setupSwipeMenuLayout(int xParam,int yParam) {

        /*****************************
         * Initialize swipeMenu Layout
         ****************************/
        //Setup swipeMenu Layout and its initial location on screen
        //setupLayout("swipeMenu",(this.getResources().getDisplayMetrics().widthPixels)/2,yParam+100);
        setupLayout("swipeMenu",xParam,yParam);

        //Inflate swipeMenuLayout using xml overlay_swipe layout
        LayoutInflater swipeMenuLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        swipeMenuLayout = (LinearLayout) swipeMenuLayoutInflater.inflate(R.layout.overlay_swipe, null);

        //Add swipeUpButton
        ImageButton swipeUpButton = (ImageButton) swipeMenuLayout.findViewById(R.id.swipe_up_icon);

        swipeUpButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.N)
            public void onClick(View v) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        //create a position point using swipeMenuParams and perform swipeAction
                        Point position = new Point(swipePointParams.x, swipePointParams.y);
                        swipeAction(position, "UP");
                        //Log.d("X swipe value :", String.valueOf(swipeMenuParams.x));
                        //Log.d("Y swipe value :", String.valueOf(swipeMenuParams.y));
                        //Remove swipeMenuLayout and modeButtonLayout from windowManager
                        windowManager.removeViewImmediate(swipeMenuLayout);
                        windowManager.removeViewImmediate(modeButtonLayout);

                        //Remove modeDescriptionLayout if it's already added to windowManager and not closed
                        if (descriptionOverlay == true) {
                            windowManager.removeViewImmediate(modeDescriptionLayout);
                            descriptionOverlay = false;
                        }
                        //Add quickButtonLayout to windowManager
                        windowManager.addView(quickButtonLayout, quickButtonParams);

                        //Update OVERLAY
                        OVERLAY = "quickButton";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //Add swipeLeftButton
        ImageButton swipeLeftButton = (ImageButton) swipeMenuLayout.findViewById(R.id.swipe_left_icon);
        swipeLeftButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                        //create a position point using swipeMenuParams and perform swipeAction
                        Point position= new Point(swipeMenuParams.x,swipeMenuParams.y);
                        swipeAction(position,"LEFT");
                        //Log.d("X swipe value :", String.valueOf(swipeMenuParams.x));
                        //Log.d("Y swipe value :", String.valueOf(swipeMenuParams.y));

                        //Remove swipeMenuLayout and modeButtonLayout from windowManager
                        windowManager.removeViewImmediate(swipeMenuLayout);
                        windowManager.removeViewImmediate(modeButtonLayout);

                        //Remove modeDescriptionLayout if it's already added to windowManager and not closed
                        if(descriptionOverlay==true) {
                            windowManager.removeViewImmediate(modeDescriptionLayout);
                            descriptionOverlay=false;
                        }
                        //Add quickButtonLayout to windowManager
                        windowManager.addView(quickButtonLayout, quickButtonParams);

                        //Update OVERLAY
                        OVERLAY="quickButton";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //Add swipeRightButton
        ImageButton swipeRightButton = (ImageButton) swipeMenuLayout.findViewById(R.id.swipe_right_icon);
        swipeRightButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        //Log.d("X swipe value :", String.valueOf(swipeMenuParams.x));
                        //Log.d("Y swipe value :", String.valueOf(swipeMenuParams.y));
                        //create a position point using swipeMenuParams and perform swipeAction
                        Point position= new Point(swipeMenuParams.x,swipeMenuParams.y);
                        swipeAction(position,"RIGHT");

                        //Remove swipeMenuLayout and modeButtonLayout from windowManager
                        windowManager.removeViewImmediate(swipeMenuLayout);
                        windowManager.removeViewImmediate(modeButtonLayout);

                        //Remove modeDescriptionLayout if it's already added to windowManager and not closed
                        if(descriptionOverlay==true) {
                            windowManager.removeViewImmediate(modeDescriptionLayout);
                            descriptionOverlay=false;
                        }
                        //Add quickButtonLayout to windowManager
                        windowManager.addView(quickButtonLayout, quickButtonParams);

                        //Update OVERLAY
                        OVERLAY="quickButton";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //swipeDownButton
        ImageButton swipeDownButton = (ImageButton) swipeMenuLayout.findViewById(R.id.swipe_down_icon);
        swipeDownButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                        //create a position point using swipeMenuParams and perform swipeAction
                        Point position= new Point(swipeMenuParams.x,swipeMenuParams.y);
                        swipeAction(position,"DOWN");
                        //Log.d("X swipe value :", String.valueOf(swipeMenuParams.x));
                        //Log.d("Y swipe value :", String.valueOf(swipeMenuParams.y));
                        //Remove swipeMenuLayout and modeButtonLayout from windowManager
                        windowManager.removeViewImmediate(swipeMenuLayout);
                        windowManager.removeViewImmediate(modeButtonLayout);

                        //Remove modeDescriptionLayout if it's already added to windowManager and not closed
                        if(descriptionOverlay==true) {
                            windowManager.removeViewImmediate(modeDescriptionLayout);
                            descriptionOverlay=false;
                        }
                        //Add quickButtonLayout to windowManager
                        windowManager.addView(quickButtonLayout, quickButtonParams);

                        //Update OVERLAY
                        OVERLAY="quickButton";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        swipeMenuLayout.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean stayedWithinClickDistance;

            //Max allowed duration for a "click", in milliseconds.
            private static final int MAX_CLICK_DURATION = 1000;
            //Max allowed distance to move during a "click", in DP.
            private static final int MAX_CLICK_DISTANCE = 15;
            private long pressStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        stayedWithinClickDistance = true;
                        pressStartTime = System.currentTimeMillis();
                        //Initial location of swipeMenuLayout
                        initialX = swipeMenuParams.x;
                        initialY = swipeMenuParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        long pressDuration = System.currentTimeMillis() - pressStartTime;
                        if (pressDuration < MAX_CLICK_DURATION && stayedWithinClickDistance) {

                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Drag swipeMenuLayout to relocate the swipeMenuLayout if moved more than 15 dp
                        if (distance((initialX + (int) (event.getRawX() - initialTouchX)),
                                (initialY + (int) (event.getRawY() - initialTouchY)),
                                swipeMenuParams.x,
                                swipeMenuParams.y)  > MAX_CLICK_DISTANCE) {
                            stayedWithinClickDistance = false;
                            swipeMenuParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                            swipeMenuParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(swipeMenuLayout, swipeMenuParams);
                            return true;
                        }
                        //Remove swipeMenuLayout and modeButtonLayout from windowManager if touched outside of layout
                    case MotionEvent.ACTION_OUTSIDE:
                        windowManager.removeViewImmediate(swipeMenuLayout);
                        windowManager.removeViewImmediate(modeButtonLayout);

                        //Remove modeDescriptionLayout if it's already added to windowManager and not closed
                        if(descriptionOverlay==true) {
                            windowManager.removeViewImmediate(modeDescriptionLayout);
                            descriptionOverlay=false;
                        }
                        //Add quickButtonLayout to windowManager
                        //windowManager.addView(quickButtonLayout,quickButtonParams);

                        //Update OVERLAY
                        OVERLAY="quickButton";
                        return true;

                }
                return false;
            }
        });

    }
    private void setupModeLayout(int xParam,int yParam) {

        /********************************
         * Initialize mode Layout
         *******************************/

        //Setup mode Layout and its initial location on screen
        setupLayout("modeButton",xParam,yParam);

        LayoutInflater quickModeButtonLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        modeButtonLayout = (LinearLayout) quickModeButtonLayoutInflater.inflate(R.layout.button_quickmenu_mode, null);

        //Set modeButtonLayout icon to swipe icon
        final TextView textViewSwipeIcon = (TextView) modeButtonLayout.findViewById(R.id.textView_swipe_icon);
        textViewSwipeIcon.setTypeface(fontLipsync);
        textViewSwipeIcon.setText(R.string.icon_swipe);

        //Setup modeDescription Layout and its initial location on screen
        setupLayout("modeDescription",xParam+dpToPx(50),yParam);

        //Set modeDescription XML Layout
        LayoutInflater modeDescriptionLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        modeDescriptionLayout = (LinearLayout) modeDescriptionLayoutInflater.inflate(R.layout.overlay_mode_description, null);

        //Button to close mode description layout
        final ImageButton descriptionCloseButton = (ImageButton) modeDescriptionLayout.findViewById(R.id.mode_button_close);
        descriptionCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(OVERLAY=="permissionDescriptionMenu"){
                    windowManager.removeViewImmediate(modeButtonLayout);
                    windowManager.addView(quickButtonLayout,quickButtonParams);
                    OVERLAY="quickButton";
                }
                windowManager.removeViewImmediate(modeDescriptionLayout);
                descriptionOverlay=false;
            }

        });
        modeDescriptionLayout.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean stayedWithinClickDistance;

            //Max allowed duration for a "click", in milliseconds.
            private static final int MAX_CLICK_DURATION = 1000;
            //Max allowed distance to move during a "click", in DP.
            private static final int MAX_CLICK_DISTANCE = 15;
            private long pressStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        stayedWithinClickDistance = true;
                        pressStartTime = System.currentTimeMillis();
                        //Initial location of swipeMenuLayout
                        initialX = modeDescriptionParams.x;
                        initialY = modeDescriptionParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        long pressDuration = System.currentTimeMillis() - pressStartTime;
                        if (pressDuration < MAX_CLICK_DURATION && stayedWithinClickDistance) {

                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Drag modeDescriptionLayout to relocate the modeDescriptionLayout if moved more than 15 dp
                        if (distance((initialX + (int) (event.getRawX() - initialTouchX)),
                                (initialY + (int) (event.getRawY() - initialTouchY)),
                                modeDescriptionParams.x,
                                modeDescriptionParams.y)  > MAX_CLICK_DISTANCE) {
                            stayedWithinClickDistance = false;
                            modeDescriptionParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                            modeDescriptionParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(modeDescriptionLayout, modeDescriptionParams);
                            return true;
                        }
                }
                return false;
            }
        });
    }

    private void setupZoomMenuLayout(int xParam,int yParam) {

        /*****************************
         * Initialize zoomMenu Layout
         ****************************/

        //Setup zoomMenu Layout and its initial location on screen
        setupLayout("zoomMenu",xParam,yParam-140);

        //Set zoomMenu XML Layout
        LayoutInflater zoomMenuLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        zoomMenuLayout = (LinearLayout) zoomMenuLayoutInflater.inflate(R.layout.overlay_zoom, null);

        //Button to close zoomMenuLayout
        final ImageButton zoomCloseButton = (ImageButton) zoomMenuLayout.findViewById(R.id.zoom_button_close);
        zoomCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Remove zoomMenuLayout and modeButtonLayout from windowManager
                windowManager.removeViewImmediate(zoomMenuLayout);
                windowManager.removeViewImmediate(modeButtonLayout);

                //Remove modeDescriptionLayout if it's already added to windowManager or closed
                if(descriptionOverlay==true) {
                    windowManager.removeViewImmediate(modeDescriptionLayout);
                    descriptionOverlay=false;

                }
                //Add QuickButton Layout to windowManager
                OVERLAY="quickButton";
                windowManager.addView(quickButtonLayout,quickButtonParams);
            }

        });

        //Add zoom in button icon
        final Button zoomInButton = (Button) zoomMenuLayout.findViewById(R.id.zoom_button_inc);
        zoomInButton.setTypeface(fontLipsync);
        zoomInButton.setText(R.string.icon_zoom_in);

        //Perform zoom in action
        zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleZoom(true);
            }

        });

        //Add zoom out button icon
        final Button zoomOutButton = (Button) zoomMenuLayout.findViewById(R.id.zoom_button_dec);
        zoomOutButton.setTypeface(fontLipsync);
        zoomOutButton.setText(R.string.icon_zoom_out);

        //Perform zoom out action
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleZoom(false);
            }
        });

    }

    private void setupVolumeMenuLayout(int xParam,int yParam) {

        final SeekBar ringtoneSeekBar;
        final SeekBar mediaSeekBar;
        final SeekBar alarmSeekBar;

        //Setup volumeMenu Layout and its initial location on screen
        setupLayout("volumeMenu",xParam,yParam+40);

        LayoutInflater volumeMenuLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        volumeMenuLayout = (LinearLayout) volumeMenuLayoutInflater.inflate(R.layout.overlay_volume, null);

        volumeMenuLayout.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_OUTSIDE:
                        //Remove volumeMenuLayout and modeButtonLayout from windowManager
                        windowManager.removeViewImmediate(volumeMenuLayout);
                        windowManager.removeViewImmediate(modeButtonLayout);

                        //Remove modeDescriptionLayout if it's already added to windowManager or closed
                        if(descriptionOverlay==true) {
                            windowManager.removeViewImmediate(modeDescriptionLayout);
                            descriptionOverlay=false;
                        }
                        //Add quickButtonLayout to windowManager
                        OVERLAY="quickButton";
                        windowManager.addView(quickButtonLayout,quickButtonParams);
                        return true;

                }
                return false;
            }
        });

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //Setup ringtone volume SeekBar and current volume
        ringtoneSeekBar = (SeekBar)volumeMenuLayout.findViewById(R.id.ringtone_seekbar);
        ringtoneSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
        ringtoneSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_RING));
        ringtoneSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean arg0) {
                audioManager.setStreamVolume(AudioManager.STREAM_RING,
                        progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Set ringtone Volume Icon on left side
        final TextView ringtoneImageView = (TextView) volumeMenuLayout.findViewById(R.id.ringtone_image_view);
        ringtoneImageView.setTypeface(fontAwesome);
        ringtoneImageView.setText(R.string.icon_ringtone2);

        //Set ringtone Volume increase button and increase ringtone Volume when it's clicked
        final Button ringtoneVolUpButton = (Button) volumeMenuLayout.findViewById(R.id.ringtone_button_inc);
        ringtoneVolUpButton.setTypeface(fontAwesome);
        ringtoneVolUpButton.setText(R.string.icon_plus);
        ringtoneVolUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.STREAM_RING);
                ringtoneSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_RING));
            }

        });

        //Set ringtone Volume decrease button and decrease ringtone Volume when it's clicked
        final Button ringtoneVolDownButton = (Button) volumeMenuLayout.findViewById(R.id.ringtone_button_dec);
        ringtoneVolDownButton.setTypeface(fontAwesome);
        ringtoneVolDownButton.setText(R.string.icon_minus);
        ringtoneVolDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.STREAM_RING);
                ringtoneSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_RING));
            }
        });

        //Setup media volume SeekBar and current volume
        mediaSeekBar = (SeekBar)volumeMenuLayout.findViewById(R.id.media_seekbar);
        mediaSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mediaSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        mediaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean arg0) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Set media Volume Icon on left side
        final TextView mediaImageView = (TextView) volumeMenuLayout.findViewById(R.id.media_image_view);
        mediaImageView.setTypeface(fontAwesome);
        mediaImageView.setText(R.string.icon_media);

        //Set media Volume increase button and increase media Volume when it's clicked
        final Button mediaVolUpButton = (Button) volumeMenuLayout.findViewById(R.id.media_button_inc);
        mediaVolUpButton.setTypeface(fontAwesome);
        mediaVolUpButton.setText(R.string.icon_plus);
        mediaVolUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        mediaSeekBar.getProgress()+1, 0);
                //audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                mediaSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            }
        });
        //Set media Volume decrease button and decrease media Volume when it's clicked
        final Button mediaVolDownButton = (Button) volumeMenuLayout.findViewById(R.id.media_button_dec);
        mediaVolDownButton.setTypeface(fontAwesome);
        mediaVolDownButton.setText(R.string.icon_minus);
        mediaVolDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        mediaSeekBar.getProgress()-1, 0);
                //audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
                mediaSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            }
        });

        //Setup alarm volume SeekBar and current volume
        alarmSeekBar = (SeekBar)volumeMenuLayout.findViewById(R.id.alarm_seekbar);
        alarmSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        alarmSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_ALARM));
        alarmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean arg0) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                        progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Set alarm Volume Icon on left side
        final TextView alarmImageView = (TextView) volumeMenuLayout.findViewById(R.id.alarm_image_view);
        alarmImageView.setTypeface(fontAwesome);
        alarmImageView.setText(R.string.icon_clock);

        //Set alarm Volume increase button and increase alarm Volume when it's clicked
        final Button alarmVolUpButton = (Button) volumeMenuLayout.findViewById(R.id.alarm_button_inc);
        alarmVolUpButton.setTypeface(fontAwesome);
        alarmVolUpButton.setText(R.string.icon_plus);
        alarmVolUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                        alarmSeekBar.getProgress()+1, 0);
                //audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.STREAM_ALARM);
                alarmSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_ALARM));
            }
        });

        //Set alarm Volume decrease button and decrease alarm Volume when it's clicked
        final Button alarmVolDownButton = (Button) volumeMenuLayout.findViewById(R.id.alarm_button_dec);
        alarmVolDownButton.setTypeface(fontAwesome);
        alarmVolDownButton.setText(R.string.icon_minus);
        alarmVolDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                        alarmSeekBar.getProgress()-1, 0);
                //audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.STREAM_ALARM);
                alarmSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_ALARM));
            }
        });
    }

    //Swipe action function to swipe each direction
    @TargetApi(24)
    private void swipeAction(Point position, String direction){

        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path p = new Path();
        p.moveTo(position.x, position.y);
        if(direction=="UP") {
            p.lineTo(position.x, position.y-1000);
        }
        else if(direction=="LEFT") {
            p.lineTo(position.x - 800, position.y);
        }
        else if(direction=="RIGHT") {
            p.lineTo(position.x + 800, position.y);
        }
        else if(direction=="DOWN") {
            p.lineTo(position.x, position.y+1000);
        }
        builder.addStroke(new GestureDescription.StrokeDescription(p, 50, 1500));
        GestureDescription gesture = builder.build();
        boolean isDispatched = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);

        //Toast.makeText(MakerAssistService.this, "Was it dispatched? " + isDispatched, Toast.LENGTH_SHORT).show();
    }

    //Get the height of screen view in px
    private static int getViewHeight(View view) {
        WindowManager wm = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int deviceWidth;
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2){
            Point size = new Point();
            display.getSize(size);
            deviceWidth = size.x;
        } else {
            deviceWidth = display.getWidth();
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        return view.getMeasuredHeight();
    }


    //Update the FeatureList based on FeatureList page number
    private void refreshFeatureList (int featurePages ) {
        featureSelected.clear();
        //Clear and add features to featureSelected based on page number
        for (int featureIndex = (featurePages-1)*4; featureIndex < (featurePages*4); featureIndex = featureIndex + 1) {
            //If number of features available are less than available space for each page
            //Make sure to add a feature only if it exist
            if(featureIndex<(featureList.size())){
                featureSelected.add(featureList.get(featureIndex));
            }
        }

    }



    //Set QuickMenu Width based on QuickMenuAdapter and number of columns
    private void setQuickMenuWidth(GridView gridView, QuickMenuAdapter gridViewAdapter, int columns) {
        if (gridViewAdapter == null) {
            return;
        }
        int totalWidth = 0;
        View listItem = gridViewAdapter.getView(0, null, gridView);
        listItem.measure(0, 0);
        totalWidth = (listItem.getMeasuredWidth()+gridView.getHorizontalSpacing())*columns;

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.width=totalWidth;
        gridView.setLayoutParams(params);
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);
        return pxToDp(distanceInPx);
    }
    public int dpToPx(int dp)
    {
        return (int) (dp * getResources().getSystem().getDisplayMetrics().density);
    }

    private float pxToDp(float px) {
        return px / getResources().getDisplayMetrics().density;
    }

    //Zoom/magnifier function
    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean handleZoom(boolean isVolumeUp) {
        // Obtain the controller on-demand, which allows us to avoid
        // dependencies on the accessibility service's lifecycle.
        final MagnificationController controller = getMagnificationController();

        // Adjust the current scale based on which volume key was pressed,
        // constraining the scale between 1x and 5x.
        final float currScale = controller.getScale();

        final float increment = isVolumeUp ? 0.05f : -0.05f;
        final float nextScale = Math.max(1f, Math.min(5f, currScale + increment));
        if (nextScale == currScale) {
            return false;
        }
        // Set the pivot, then scale around it.
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            controller.setScale(nextScale, true /* animate */);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            controller.setCenter(metrics.widthPixels / 2f, metrics.heightPixels / 2f, true);
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Remove the layouts based on status of OVERLAY variable
        if (OVERLAY.contentEquals("introMenu"))
        {
            windowManager.removeViewImmediate(introLayout);

        }
        if (OVERLAY.contentEquals("quickButton"))
        {
            windowManager.removeViewImmediate(quickButtonLayout);

        }
        if (OVERLAY.contentEquals("quickMenu"))
        {
            windowManager.removeViewImmediate(quickMenuLayout);
        }
        if (OVERLAY.contentEquals("swipePoint")){
            windowManager.removeView(modeButtonLayout);
            windowManager.removeView(swipePointLayout);
            if(descriptionOverlay==true){
                windowManager.removeView(modeDescriptionLayout);
            }
        }
        if (OVERLAY.contentEquals("swipeMenu")){
            windowManager.removeViewImmediate(modeButtonLayout);
            windowManager.removeViewImmediate(swipeMenuLayout);
            if(descriptionOverlay==true){
                windowManager.removeViewImmediate(modeDescriptionLayout);
            }
        }

        if (OVERLAY.contentEquals("permissionDescriptionMenu")){
            windowManager.removeViewImmediate(modeButtonLayout);
            if(descriptionOverlay==true){
                windowManager.removeViewImmediate(modeDescriptionLayout);
            }
        }
        if (OVERLAY.contentEquals("volumeMenu")){
            windowManager.removeViewImmediate(modeButtonLayout);
            windowManager.removeViewImmediate(volumeMenuLayout);
            if(descriptionOverlay==true){
                windowManager.removeViewImmediate(modeDescriptionLayout);
            }
        }
        if (OVERLAY.contentEquals("zoomMenu")){
            windowManager.removeViewImmediate(modeButtonLayout);
            windowManager.removeViewImmediate(zoomMenuLayout);
            if(descriptionOverlay==true){
                windowManager.removeViewImmediate(modeDescriptionLayout);
            }
        }

        //Reset overlay
        OVERLAY="";

        //Stop service
        stopSelf();

        //Update status of is_overlay_on SharedPreference
        SharedPreferences prefs = getSharedPreferences("MAKER_ASSIST", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_overlay_on", false);
        editor.commit();
        //Log.d("MAKER_ASSIST is: ","destroyed");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }


    public boolean onUnbind(Intent intent) {
        //Log.d("MAKER_ASSIST is: ","onUnbinded");
        stopService(new Intent(getApplication(), MakerAssistService.class));
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onServiceConnected() {

        final AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) {
            isServiceEnabled = false;
            editor.putBoolean("is_service_on", false);
            //stopService(new Intent(getApplication(), MakerAssistService.class));
            // If we fail to obtain the service info, the service is not really
            // connected and we should avoid setting anything up.
            //return;
        }
        else{
            isServiceEnabled = true;
            //Update status of is_intro_displayed and is_overlay_on SharedPreference
            editor.putBoolean("is_service_on", true);
            editor.putBoolean("is_intro_displayed", false);
            editor.putBoolean("is_overlay_on", true);
            editor.commit();
            //Log.d("MAKER_ASSIST is: ","connected ");
        }

        // We declared our intent to request key filtering in the meta-data
        // attached to our service in the manifest. Now, we can explicitly
        // turn on key filtering when needed.
        info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS |
                AccessibilityServiceInfo.DEFAULT |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY |
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
        // Set up a listener for changes in the state of magnification.
        if (currentApiVersion >= Build.VERSION_CODES.N) {
            getMagnificationController().addListener(new MagnificationController.OnMagnificationChangedListener() {
                @Override
                public void onMagnificationChanged(MagnificationController controller,
                                                   Region region, float scale, float centerX, float centerY) {
                    //Log.e("LOG_TAG", "Magnification scale is now " + scale);
                }
            });
        }
    }

}