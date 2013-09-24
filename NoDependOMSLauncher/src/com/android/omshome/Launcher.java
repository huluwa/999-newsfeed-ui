/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.omshome;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import android.os.SystemProperties;
import org.xmlpull.v1.XmlPullParser;
import android.app.StatusBarManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Parcelable;
import android.os.Process;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.LiveFolders;
import android.provider.Settings;
import android.provider.CallLog.Calls;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Display;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.omshome.CellLayout.CellInfo;
import com.android.omshome.quickaction.QuickLauncher;
import com.android.internal.util.XmlUtils;

/**
 * Default launcher application.
 */
public final class Launcher extends Activity
        implements View.OnClickListener, OnLongClickListener, LauncherModel.Callbacks, AllAppsView.Watcher {
    static final String TAG = "oms2.5Launcher";
    public static boolean LOGD = android.util.Config.DEBUG;
    
    public static final String HOMEPROPERTY_USESETTINGS = "omsnewhome.usesettings";

    static final boolean PROFILE_STARTUP = false;
    static final boolean DEBUG_WIDGETS = false;
    static final boolean DEBUG_USER_INTERFACE = false;

    private static final int WALLPAPER_SCREENS_SPAN = 2;

    private static final int MENU_GROUP_ADD = 1;
    private static final int MENU_GROUP_WALLPAPER = MENU_GROUP_ADD + 1;

    private static final int MENU_ADD = Menu.FIRST + 1;
    private static final int MENU_WALLPAPER_SETTINGS = MENU_ADD + 1;
    private static final int MENU_SEARCH = MENU_WALLPAPER_SETTINGS + 1;
    private static final int MENU_NOTIFICATIONS = MENU_SEARCH + 1;
    private static final int MENU_SETTINGS = MENU_NOTIFICATIONS + 1;
    private static final int MENU_CHOOSEHOME =  MENU_SETTINGS + 1;
    private static final int MENU_HOMESETTINGS =  MENU_CHOOSEHOME + 1;    
    
    private static final int REQUEST_CREATE_SHORTCUT = 1;
    private static final int REQUEST_CREATE_LIVE_FOLDER = 4;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_PICK_APPLICATION = 6;
    private static final int REQUEST_PICK_SHORTCUT = 7;
    private static final int REQUEST_PICK_LIVE_FOLDER = 8;
    private static final int REQUEST_PICK_APPWIDGET = 9;
    private static final int REQUEST_PICK_WALLPAPER = 10;
    private static final int REQUEST_PICK_APP_SHORTCUT = 11;
    private static final int REQUEST_HOME_SETTING = 12;
    public  static final int REQUEST_PAGE_MANAGER = 20;

    static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    static final String EXTRA_CUSTOM_WIDGET = "custom_widget";
    static final String SEARCH_WIDGET = "search_widget";

    // This flag was used to co-operate adding preload widget icons
    private static final String WIDGET_INIT_TAG = "home_widget_init_tag";

//    static final int SCREEN_COUNT = 5;
//    static final int DEFAULT_SCREEN = 2;
    static final int NUMBER_CELLS_X = 4;
    static final int NUMBER_CELLS_Y = 4;
    
    static final int DIALOG_CREATE_SHORTCUT = 100;
    static final int DIALOG_RENAME_FOLDER   = 101;

    private static final String PREFERENCES = "launcher.preferences";

    // Type: int
    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    // Type: boolean
    private static final String RUNTIME_STATE_ALL_APPS_FOLDER = "launcher.all_apps_folder";
    // Type: long
    private static final String RUNTIME_STATE_USER_FOLDERS = "launcher.user_folder";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cellX";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cellY";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_spanX";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_spanY";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_COUNT_X = "launcher.add_countX";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_COUNT_Y = "launcher.add_countY";
    // Type: int[]
    private static final String RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS = "launcher.add_occupied_cells";
    // Type: boolean
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
    // Type: long
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";

    static final int APPWIDGET_HOST_ID = 1033;

    private static final Object sLock = new Object();
    private static int sScreen = Workspace.DEFAULT_CURRENT_SCREEN;
    private static int sScreenCount = Workspace.DEFAULT_SCREEN_NUM;

    private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver();
    private final BroadcastReceiver mPhoneStateReceiver = new PhoneStateChangeIntentReceiver();
    
    public static final String  HOMEKEY_PRESSED_IN_HOME = "oms.content.action.HOMEKEY_PRESSED_IN_HOME";
    public static final String  ENDCALLKEY_PRESSED_IN_HOME = "oms.content.action.ENDCALLKEY_PRESSED_IN_HOME";
    
    private final  BroadcastReceiver mHomeKeyReceiver = new HomeKeyIntentReceiver();
    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();

    private LayoutInflater mInflater;

    private DragController mDragController;
    private Workspace mWorkspace;

    private AppWidgetManager mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;

    private CellLayout.CellInfo mAddItemCellInfo;
    private CellLayout.CellInfo mMenuAddInfo;
    private final int[] mCellCoordinates = new int[2];
    private FolderInfo mFolderInfo;

    private DeleteZone mDeleteZone;
    private HandleView mHandleView;
    public AllAppsView mAllAppsGrid;

    private Bundle mSavedState;

    private SpannableStringBuilder mDefaultKeySsb = null;

    private boolean mWorkspaceLoading = true;
    private boolean isPageMoving = false;  // use for lock screen when user change page sequence.
    private boolean isAvoidMoveBeforeClick = false; //when user click an activity, lock the screen until onStop. 

    private boolean showGoogleSearch = false;
    private boolean mPaused = true;
    private boolean mRestoring;
    private boolean mWaitingForResult;
    
    private boolean mIsLongPressed = false;

    private Bundle mSavedInstanceState;

    private LauncherModel mModel;
    private IconCache mIconCache;

    public ArrayList<ItemInfo> mDesktopItems = new ArrayList<ItemInfo>();
    static HashMap<Long, FolderInfo> mFolders = new HashMap<Long, FolderInfo>();

    private ImageView mPreviousView;
    private ImageView mNextView;

    protected RelativeLayout mBottomLayout;    
    private IndicatorWithMissCallNumberView mMissCallImageView;
    
    DragLayer mDragLayer;

    public  static final String HOME_INIT_TAG = "new_home_init_tag";
    
    //SKIP
    public static final String  SKIP_MOTION = "oms.content.action.SKIP_MOTION";
    public static final String SKIP_LONGCLICK = "com.android.omshome.skiplongclick"; 
    public static final String RESET_SKIP_LONGCLICK = "com.android.omshome.reset_skip_longclick";
    private final  BroadcastReceiver mSkipMotionReceiver = new SkipMotionIntentReceiver();
    private boolean skipLongClick = false; //for contact_sms widget
    //SKIP
    
    ShortCutAnimationListener shortCutAnimationListener;
    public int missCallCount = 0;
    
    private final ContentObserver mCallLogObserver = new MissedCallContentObserver(this, new Handler());
    
    public static final String ACTION_LOAD_COMPLETE = "com.android.omshome.intent.action.ACTION_LOAD_COMPLETE";
    public static final String ACTION_ITEM_ADDED = "com.android.omshome.intent.action.ITEM_ADDED";
    public static final String ACTION_UPDATE_CALENDAR = "com.android.omshome.intent.action.UPDATE_CALENDAR";
    public static final String SHORTCUT_SYMBOL_POSITION = "com.android.omshome.symbol_position";
    public static final String SHORTCUT_SYMBOL_BITMAP = "com.android.omshome.symbol_bitmap";    
    public static final String SHORTCUT_SYMBOL_INTENT = "com.android.omshome.symbol_intent";    
    public static final String EXTRA_ITEM_LAUNCH_INTENT = "com.android.omshome.intent.extra.ITEMINTENT";

    private final BroadcastReceiver mReloadReceiver = new ReloadIntentReceiver();
    private final BroadcastReceiver mCalendarReceiver = new CalendarReceiver();
    private final BroadcastReceiver mCalendarRefreshReceiver = new CalendarRefreshReceiver();
    private final HomeChangeReceiver mHomeChangeReceiver = new HomeChangeReceiver();
    final CallStateHandler mCallStateHandler = new CallStateHandler();
    private final RestoreBackupIntentReceiver mRestoreBackupIntentReceiver = new RestoreBackupIntentReceiver();
    private InstallShortcutReceiver mInstallShortcutReceiver;
    private UninstallShortcutReceiver mUnInstallShortcutReceiver;
    
    private boolean isNeedListenSdcardMount = false;
    private PageManagerHandler pageManagerHandler = null;
    
    public boolean amVisible = true;
    private boolean sdcardReloadNotDone = false;
    public static String displayConfigs;
    public ArrayList<AppWidgetHostView> widgetHostViews;

    // Hotseats (quick-launch icons next to AllApps)
    private static final int NUM_HOTSEATS = 2;
    private String[] mHotseatConfig = null;
    private Intent[] mHotseats = null;
    private Drawable[] mHotseatIcons = null;
    private CharSequence[] mHotseatLabels = null;
    
    public int dockStyle = 0;
    private boolean isAllAppsLoading = false;
    public int screenWidth = 0;
    public int screenHeight = 0;
    public long layoutId = 0;
    public boolean isNeedCheckCategory = false;
    
    public static boolean isLowLevelHardware()
    {
    	return false;//SystemProperties.getInt("isLowLevelHardware", 0) == 1; 
    }
    
    public static void setSettingsIntValue(Context con, String tag, int value)
    {
    	if(false)
    	{
	    	try{
				android.provider.Settings.System.putInt(con.getContentResolver(), tag, value);
			}catch(Exception ex){}
    	}
    	else
    	{
    		SharedPreferences settings = con.getSharedPreferences("home_settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(tag, value);
            editor.commit();            
    	}
    }
    
    public static int getSettingsIntValue(Context con, String tag) throws SettingNotFoundException
    {
    	int ret = 0;
    	if(false)
    	{
	    	try{
	    		ret = android.provider.Settings.System.getInt(con.getContentResolver(), tag);
			}catch(Exception ex){}
    	}
    	else
    	{
    		 SharedPreferences settings = con.getSharedPreferences("home_settings", Context.MODE_PRIVATE);
    	     ret = settings.getInt(tag, 0);    	     
    	}
    	return ret;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//    	Debug.startMethodTracing("/tmp/oms.trace");

    	Log.d(TAG, "onCreate="+this);
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        if(SystemProperties.getInt("home_enable_sensor", 0) == 1)
        {
           Log.d(TAG, "you enable the sensor, will have landscape and portrait mode");
           super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        LauncherApplication app = ((LauncherApplication)getApplication());
        mModel = app.setLauncher(this);
        mModel.setLauncher(this);
        mIconCache = app.getIconCache();       
        mDragController = new DragController(this);
        mInflater = getLayoutInflater();
        
        screenWidth = getDisplayWidth();
        screenHeight = getDisplayHeight();
        
        if (PROFILE_STARTUP) {
            android.os.Debug.startMethodTracing("/sdcard/launcher");
        }
        widgetHostViews = new ArrayList<AppWidgetHostView>();
        
//    	final LauncherORM orm = LauncherORM.instance(getApplicationContext());
//        String dockStylestr = orm.getSettingValue(LauncherORM.dock_style);
//        if(dockStylestr == null || "".equals(dockStylestr)){
//        	dockStyle = 0;
//        	orm.addSetting(LauncherORM.dock_style, "0");
//        }else{
//        	dockStyle = Integer.parseInt(dockStylestr);
//        }

        if(dockStyle==1){
            loadHotseats();
        }
        
        checkForLocaleChange();
        setWallpaperDimension();

        //get current fontScale
        final Resources res = getResources();
        final Configuration configuration = res.getConfiguration();
        preOrientation = configuration.orientation;
        mFontScale = configuration.fontScale;  
        		
        displayConfigs = DISPLAY_CONFIG_DEFAULT_2D;
        
        /*
        displayConfigs = getDisplayConfig();
        if(displayConfigs == null || displayConfigs.length() < 2){
        	Log.e(TAG, "displayConfigs read error, use 2D as default");
        	displayConfigs = DISPLAY_CONFIG_DEFAULT_2D;
        }
        
        if(isUsingHomeSettings()){
        	String value = orm.getSettingValue(LauncherORM.allapps_view_style);
        	int allapps_view_style = 0;
        	if(null != value){
        		allapps_view_style = Integer.valueOf(value);
        	}else{
        		allapps_view_style = 0;
        		orm.addSetting(LauncherORM.allapps_view_style, "0");
        	}
        	
        	if(dockStyle==1){
        		if(HomeSettingPreference.allapps_view_style_3d == allapps_view_style){
        			setContentView(R.layout.glauncher);
        			layoutId = R.layout.glauncher;
        		}else if(HomeSettingPreference.allapps_view_style_2d == allapps_view_style){   
        			setContentView(R.layout.glauncher_2d);
        			layoutId = R.layout.glauncher_2d;
        		}else{
        			setContentView(R.layout.glauncher_2d_land);
        			layoutId = R.layout.glauncher_2d_land;
        		}
        	}else{
        		if(HomeSettingPreference.allapps_view_style_3d == allapps_view_style){
        			setContentView(R.layout.launcher);
        			layoutId = R.layout.launcher;
        		}else if(HomeSettingPreference.allapps_view_style_2d == allapps_view_style){   
        			setContentView(R.layout.launcher_2d);
        			layoutId = R.layout.launcher_2d;
        		}else{
        			setContentView(R.layout.launcher_2d_land);
        			layoutId = R.layout.launcher_2d_land;
        		}
        	}
        	//Log.d(TAG, "setContentView  use  homesetting, dockStyle:"+dockStyle+" view choose:"+allapps_view_style);
        }else{ // use opl config.
        	
        	//Log.d(TAG, "setContentView  use opl, dockStyle:"+dockStyle+" view choose:"+displayConfigs.charAt(displayConfigs.length()-1-DISPLAY_CONFIG_2D_ALLAPP_INDEX));
        	if(dockStyle == 1){ // google style
        		if('0' != displayConfigs.charAt(displayConfigs.length()-1-DISPLAY_CONFIG_2D_ALLAPP_INDEX)){
	        		setContentView(R.layout.glauncher_2d);
	        		layoutId = R.layout.glauncher_2d;
	        	}else{
	        		setContentView(R.layout.glauncher);
	        		layoutId = R.layout.glauncher;
	        	}
	        }else{
	        	if('0' != displayConfigs.charAt(displayConfigs.length()-1-DISPLAY_CONFIG_2D_ALLAPP_INDEX)){
	        		setContentView(R.layout.launcher_2d);
	        		layoutId = R.layout.launcher_2d;
	        	}else{
	        		setContentView(R.layout.launcher);
	        		layoutId = R.layout.launcher;
	        	}
	        }
        }
        */
        
        getHomeInitTag();
        
    	setContentView(R.layout.launcher_2d);
		layoutId = R.layout.launcher_2d;
        
        setupViews();
        
        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new LauncherAppWidgetHost(this.getApplicationContext(), APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();
        
        isNeedResetViewImage = true;
        isNeedDelayCheckMissCallUI = true;
        isNeedStartWebWidgetManager = true;
        
        registerContentObservers();

        lockAllApps();

        initHome();
        
        mSavedState = savedInstanceState;
        restoreState(mSavedState);

        if (PROFILE_STARTUP) {
            android.os.Debug.stopMethodTracing();
        }

        //need enhance the launch
        mInstallShortcutReceiver = new InstallShortcutReceiver(this);
        mUnInstallShortcutReceiver = new UninstallShortcutReceiver(this);
        
        
        if (!mRestoring) {
        	if(LOGD)Log.d(TAG,"onCreate  mRestoring is false, startLoader=====");
            mWorkspaceLoading = true;//lock screen!
            isNeedCheckCategory = true;
            isNeedDeleteAllWebWidgetView = true;
            isNeedAllAppsSort = true;
            setIsAllAppsLoading(true);
            mModel.startLoader(this, true);
        }

        // For handling default keys
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);
       
        isNeedListenSdcardMount = false;
        registerReceivers();
        
        missCallCount = 0;
        shortCutAnimationListener = new ShortCutAnimationListener();
        try{
            Intent intent4searchwidget = new Intent("com.android.omshome.create");
            intent4searchwidget.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
            sendBroadcast(intent4searchwidget);
        }catch(Exception ne){
        	Log.d(TAG,"send com.android.omshome.create for search xxxx"); 
        }

        pageManagerHandler = new PageManagerHandler(this);
        
        getWindow().setFormat(PixelFormat.TRANSLUCENT);        
        getWindow().setBackgroundDrawable(null);
        //
        //the following code will cause Search back draw overlay, remove it
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        if(!LOGD)
        {
           LOGD = (SystemProperties.getInt("home_enable_checklog", 0) == 1);
        }	
    }
    
    private void startWebWidgetManager(){
    	//initialize web widget engine for home screen
    }
    
    public static boolean isAllowSwitchView()
    {   
    	if(true)
    	{
    		return false;
    	}
    	
        //allow switch && default display 3D
        String tmpAllowSwitchView = SystemProperties.get("omsnewhome.allowswitchview", "0");   //1 allow,0 didnot allow
        Boolean defaultAllowSwitch2Dand3D = "1".equals(tmpAllowSwitchView);
        return (defaultAllowSwitch2Dand3D && displayConfigs.equals(DISPLAY_CONFIG_DEFAULT_3D));
    }
    
    private void registerReceivers() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mCloseSystemDialogsReceiver, filter);
        
        IntentFilter Phonestatefilter = new IntentFilter("CallNotifier.InCall"); //incoming call
        Phonestatefilter.addAction("CallNotifier.IncomingDisconnected");//incoming call missed
        Phonestatefilter.addAction("CallNotifier.CallEnd");//call end
        
        registerReceiver(mPhoneStateReceiver, Phonestatefilter);      
        
        IntentFilter backAndHomefilter = new IntentFilter(HOMEKEY_PRESSED_IN_HOME); //home
        backAndHomefilter.addAction(ENDCALLKEY_PRESSED_IN_HOME);//end        
        
        registerReceiver(mHomeKeyReceiver, backAndHomefilter);
        
        //SKIP
        IntentFilter skipMotionFilter = new IntentFilter(SKIP_MOTION); //home
        skipMotionFilter.addAction(SKIP_LONGCLICK);
        skipMotionFilter.addAction(RESET_SKIP_LONGCLICK);
        registerReceiver(mSkipMotionReceiver,skipMotionFilter);
        //SKIP
        
        registerReceiver(mCalendarRefreshReceiver, new IntentFilter(ACTION_UPDATE_CALENDAR));
        registerCalendarReceiver();
        
        IntentFilter pageManagerFilter = new IntentFilter(INTENT_ADD_SCREEN);
        pageManagerFilter.addAction(INTENT_REMOVE_SCREEN);
        pageManagerFilter.addAction(INTENT_FINISH_PAGEMANAGER);
		pageManagerFilter.addAction(INTENT_CACHE_SCREEN);
		pageManagerFilter.addAction(INTENT_CHANGE_SCREEN);
		pageManagerFilter.addAction(INTENT_RELAUNCHE_ACTIVITY);
        registerReceiver(pageManagerReceiver,pageManagerFilter);
        
        IntentFilter homeChangeFilter = new IntentFilter("oms.action.HOME_CHANGED");
        registerReceiver(mHomeChangeReceiver, homeChangeFilter);
        
        IntentFilter installShortcutfilter = new IntentFilter(InstallShortcutReceiver.ACTION_INSTALL_SHORTCUT);
        registerReceiver(mInstallShortcutReceiver, installShortcutfilter);
        
        IntentFilter uninstallShortcutfilter = new IntentFilter(UninstallShortcutReceiver.ACTION_UNINSTALL_SHORTCUT);
        registerReceiver(mUnInstallShortcutReceiver, uninstallShortcutfilter);
        
        IntentFilter restorefilter = new IntentFilter(INTENT_RESTORE_BACKUP);
        registerReceiver(mRestoreBackupIntentReceiver, restorefilter);
                
        IntentFilter reloadfilter = new IntentFilter("android.intent.action.PACKAGE_RELOAD_COMP");
        reloadfilter.addAction("Home.DELAY_PACKAGE_RELOAD_COMP");
        registerReceiver(mReloadReceiver, reloadfilter);
        
        reloadfilter = new IntentFilter();
        reloadfilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        reloadfilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        registerReceiver(mReloadReceiver, reloadfilter);

    }

    @Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		//if(LOGD)Log.d(TAG, "dispatchTouchEvent  action:"+ev.getAction());
		return super.dispatchTouchEvent(ev);
	}

	private void checkForLocaleChange() {
        final LocaleConfiguration localeConfiguration = new LocaleConfiguration();
        readConfiguration(this, localeConfiguration);

        final Configuration configuration = getResources().getConfiguration();

        final String previousLocale = localeConfiguration.locale;
        final String locale = configuration.locale.toString();

        final int previousMcc = localeConfiguration.mcc;
        final int mcc = configuration.mcc;

        final int previousMnc = localeConfiguration.mnc;
        final int mnc = configuration.mnc;

        boolean localeChanged = !locale.equals(previousLocale) || mcc != previousMcc || mnc != previousMnc;

        if (localeChanged) {
            localeConfiguration.locale = locale;
            localeConfiguration.mcc = mcc;
            localeConfiguration.mnc = mnc;

            writeConfiguration(this, localeConfiguration);
			Log.d(TAG, "checkForLocaleChange localeChanged, iconcache flush");
            mIconCache.flush();

            if(dockStyle==1){
            	loadHotseats();
            }
        }
    }
    
    /**
     * start load init data.
     */
    public boolean isNeedinstallSTK = false;
    private void initHome() {
    	boolean inited = false;   
    	try {			
			inited = ((home_int_tag == 1));
		} catch (Exception e) {}
		
		if(!inited)
		{
			LauncherORM.instance(this).resetFavorites(this);
			setHomeInitTag(Launcher.this, 1);
			isNeedinstallSTK = false; //close the function.
		}
		else{
			//LauncherORM.instance(this).RebindAppWidgets();
			isNeedinstallSTK = false;
		}
		isAvoidMoveBeforeClick = false;
	}
   
    final static String INIT_TAG_FILE_NAME = "home_settings";
    public static void setHomeInitTag(Context con,int initValue)
    {
        SharedPreferences settings = con.getSharedPreferences(INIT_TAG_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(HOME_INIT_TAG, initValue);
        editor.commit();
    }
    
    public int default_page_index = Workspace.DEFAULT_CURRENT_SCREEN;
    public int home_int_tag = 1;
    
    private int getHomeInitTag()
    {
        SharedPreferences settings = getSharedPreferences(INIT_TAG_FILE_NAME, Context.MODE_PRIVATE);
        home_int_tag = settings.getInt(HOME_INIT_TAG, 0);
        
        int screen_count = settings.getInt(Workspace.TAG_SCREEN_NUM, -1);
        default_page_index = settings.getInt(LauncherORM.default_page_index, -1);
        
        if(screen_count == -1 || default_page_index == -1){
        	screen_count = Workspace.DEFAULT_SCREEN_NUM;
        	default_page_index = Workspace.DEFAULT_SCREEN_NUM;
        	
        	SharedPreferences.Editor editor = settings.edit();
        	editor.putInt(Workspace.TAG_SCREEN_NUM, screen_count);
        	editor.putInt(LauncherORM.default_page_index, default_page_index);
        	editor.commit();
        }
        
        setScreenCount(screen_count);
        
        return home_int_tag;
    }
    
    private static class LocaleConfiguration {
        public String locale;
        public int mcc = -1;
        public int mnc = -1;
    }

    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // Ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            context.getFileStreamPath(PREFERENCES).delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    static int getScreen() {
        synchronized (sLock) {
            return sScreen;
        }
    }

    static void setScreen(int screen) {
        synchronized (sLock) {
            sScreen = screen;
        }
    }
    
    static void setScreenCount(int screenCount) {
        synchronized (sLock) {
            sScreenCount = screenCount;
        }
    }
    
    static int getScreenCount() {
        synchronized (sLock) {
            return sScreenCount;
        }
    }

    private void setWallpaperDimension() {
        WallpaperManager wpm = (WallpaperManager)getSystemService(WALLPAPER_SERVICE);

        Display display = getWindowManager().getDefaultDisplay();
        boolean isPortrait = display.getWidth() < display.getHeight();

        final int width = isPortrait ? display.getWidth() : display.getHeight();
        final int height = isPortrait ? display.getHeight() : display.getWidth();
        wpm.suggestDesiredDimensions(width * WALLPAPER_SCREENS_SPAN, height);
    }

    // Note: This doesn't do all the client-id magic that BrowserProvider does
    // in Browser. (http://b/2425179)
    private Uri getDefaultBrowserUri() {
        String url = getString(R.string.default_browser_url);
        if (url.indexOf("{CID}") != -1) {
            url = url.replace("{CID}", "android-google");
        }
        return Uri.parse(url);
    }

    // Load the Intent templates from arrays.xml to populate the hotseats. For
    // each Intent, if it resolves to a single app, use that as the launch
    // intent & use that app's label as the contentDescription. Otherwise,
    // retain the ResolveActivity so the user can pick an app.
    private void loadHotseats() {
        if (mHotseatConfig == null) {
            mHotseatConfig = getResources().getStringArray(R.array.hotseats);
            if (mHotseatConfig.length > 0) {
                mHotseats = new Intent[mHotseatConfig.length];
                mHotseatLabels = new CharSequence[mHotseatConfig.length];
                mHotseatIcons = new Drawable[mHotseatConfig.length];
            } else {
                mHotseats = null;
                mHotseatIcons = null;
                mHotseatLabels = null;
            }

            TypedArray hotseatIconDrawables = getResources().obtainTypedArray(R.array.hotseat_icons);
            for (int i=0; i<mHotseatConfig.length; i++) {
                // load icon for this slot; currently unrelated to the actual activity
                try {
                    mHotseatIcons[i] = hotseatIconDrawables.getDrawable(i);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    Log.w(TAG, "Missing hotseat_icons array item #" + i);
                    mHotseatIcons[i] = null;
                }
            }
            hotseatIconDrawables.recycle();
        }

        PackageManager pm = getPackageManager();
        for (int i=0; i<mHotseatConfig.length; i++) {
            Intent intent = null;
            if (mHotseatConfig[i].equals("*BROWSER*")) {
                // magic value meaning "launch user's default web browser"
                // replace it with a generic web request so we can see if there is indeed a default
                String defaultUri = getString(R.string.default_browser_url);
                intent = new Intent(
                        Intent.ACTION_VIEW,
                        ((defaultUri != null)
                            ? Uri.parse(defaultUri)
                            : getDefaultBrowserUri())
                    ).addCategory(Intent.CATEGORY_BROWSABLE);
                // note: if the user launches this without a default set, she
                // will always be taken to the default URL above; this is
                // unavoidable as we must specify a valid URL in order for the
                // chooser to appear, and once the user selects something, that 
                // URL is unavoidably sent to the chosen app.
            } else {
                try {
                    intent = Intent.parseUri(mHotseatConfig[i], 0);
                } catch (java.net.URISyntaxException ex) {
                    Log.w(TAG, "Invalid hotseat intent: " + mHotseatConfig[i]);
                    // bogus; leave intent=null
                }
            }
            
            if (intent == null) {
                mHotseats[i] = null;
                mHotseatLabels[i] = getText(R.string.activity_not_found);
                continue;
            }

            if (LOGD) {
                Log.d(TAG, "loadHotseats: hotseat " + i 
                    + " initial intent=[" 
                    + intent.toUri(Intent.URI_INTENT_SCHEME)
                    + "]");
            }

            ResolveInfo bestMatch = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            List<ResolveInfo> allMatches = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (LOGD) { 
                Log.d(TAG, "Best match for intent: " + bestMatch);
                Log.d(TAG, "All matches: ");
                for (ResolveInfo ri : allMatches) {
                    Log.d(TAG, "  --> " + ri);
                }
            }
            // did this resolve to a single app, or the resolver?
            if (allMatches.size() == 0 || bestMatch == null) {
                // can't find any activity to handle this. let's leave the 
                // intent as-is and let Launcher show a toast when it fails 
                // to launch.
                mHotseats[i] = intent;

                // set accessibility text to "Not installed"
                mHotseatLabels[i] = getText(R.string.activity_not_found);
            } else {
                boolean found = false;
                for (ResolveInfo ri : allMatches) {
                    if (bestMatch.activityInfo.name.equals(ri.activityInfo.name)
                        && bestMatch.activityInfo.applicationInfo.packageName
                            .equals(ri.activityInfo.applicationInfo.packageName)) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    if (LOGD) Log.d(TAG, "Multiple options, no default yet");
                    // the bestMatch is probably the ResolveActivity, meaning the
                    // user has not yet selected a default
                    // so: we'll keep the original intent for now
                    mHotseats[i] = intent;

                    // set the accessibility text to "Select shortcut"
                    mHotseatLabels[i] = getText(R.string.title_select_shortcut);
                } else {
                    // we have an app!
                    // now reconstruct the intent to launch it through the front
                    // door
                    ComponentName com = new ComponentName(
                        bestMatch.activityInfo.applicationInfo.packageName,
                        bestMatch.activityInfo.name);
                    mHotseats[i] = new Intent(Intent.ACTION_MAIN).setComponent(com);

                    // load the app label for accessibility
                    mHotseatLabels[i] = bestMatch.activityInfo.loadLabel(pm);
                }
            }

            if (LOGD) {
                Log.d(TAG, "loadHotseats: hotseat " + i 
                    + " final intent=[" 
                    + ((mHotseats[i] == null)
                        ? "null"
                        : mHotseats[i].toUri(Intent.URI_INTENT_SCHEME))
                    + "] label=[" + mHotseatLabels[i]
                    + "]"
                    );
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(LOGD)Log.d(TAG,"onActivityResult requestCode:"+requestCode+" resultCode:"+resultCode+" intent:"+data);
        mWaitingForResult = false;

        // The pattern used here is that a user PICKs a specific application,
        // which, depending on the target, might need to CREATE the actual target.

        // For example, the user would PICK_SHORTCUT for "Music playlist", and we
        // launch over to the Music app to actually CREATE_SHORTCUT.

        if (resultCode == RESULT_OK && mAddItemCellInfo != null) {
            switch (requestCode) {
                case REQUEST_PICK_APPLICATION:
                    completeAddApplication(this, data, mAddItemCellInfo);
                    break;
                case REQUEST_PICK_SHORTCUT:
                    processShortcut(data);
                    break;
                case REQUEST_CREATE_SHORTCUT:
                    completeAddShortcut(data, mAddItemCellInfo);
                    break;
                case REQUEST_PICK_LIVE_FOLDER:
                    addLiveFolder(data);
                    break;
                case REQUEST_CREATE_LIVE_FOLDER:
                    completeAddLiveFolder(data, mAddItemCellInfo);
                    break;
                case REQUEST_PICK_APPWIDGET:
                    addAppWidget(data);
                    break;
                case REQUEST_CREATE_APPWIDGET:
                    completeAddAppWidget(data, mAddItemCellInfo);
                    break;
                case REQUEST_PICK_WALLPAPER:
                    // We just wanted the activity result here so we can clear mWaitingForResult
                    break;
            }
        } else if ((requestCode == REQUEST_PICK_APPWIDGET ||
                requestCode == REQUEST_CREATE_APPWIDGET) && resultCode == RESULT_CANCELED &&
                data != null) {
            // Clean up the appWidgetId if we canceled
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        } else if (requestCode == REQUEST_PAGE_MANAGER) {
            if(LOGD)Log.d(TAG, "====ERROR   onActivityResult REQUEST_PAGE_MANAGER==== data:"+data);
            if(data != null){
                showPage(data.getExtras().getInt(STRING_PAGE_INDEX), 0);
                if(true == isStartPageManager) {
                    isStartPageManager = false;
                }
            }
        } else if(requestCode == REQUEST_HOME_SETTING) {
        	Log.d(TAG, "back from setting");
            Process.killProcess(Process.myPid());
        }
    }

    @Override
    protected void onResume() {
    	if(LOGD)Log.d(TAG, "onResume");
        super.onResume();

        amVisible = true;
        isAvoidMoveBeforeClick = false;
        
        int tmpOrient = getResources().getConfiguration().orientation;
        if(tmpOrient != preOrientation)
        {
            Log.d(TAG,"onResume orientation:"+ tmpOrient);
        	Log.d(TAG, "why onPause and OnResume orientation is different");
        }        
        
        //when in background and sdcard plugs in, will lead reload message
        if(sdcardReloadNotDone)
        {
        	refreshWorkspace();
			sdcardReloadNotDone = false;
			 //begin: update call button icon
            new Thread("updateMissCallCount-bad"){
                public void run(){
                    updateMissCallCount();
                    mCallStateHandler.sendMessage(mCallStateHandler.obtainMessage(Launcher.this.MSG_UPDATEMISSCALL));
                }
            }.start();
            //end
        }
        else
        {
	        //clear isRestoreBackup flag
	        setIsRestoreBackupAPK(false);
	        setIsRestoreBackupWidget(false);	        
	        
	        
	        //for STK idle event
	        Intent intent = new Intent("com.android.action.IDLE_AVAILABLE");
	        sendBroadcast(intent);
	
	        mPaused = false;
	
	        if (mRestoring) {
	            mWorkspaceLoading = true;
	            isNeedAllAppsSort = true;
//	            if(LOGD)Log.d(TAG,"onResume  mRestoring is true, startLoader=====");
	            isNeedCheckCategory = true;
	            isNeedDeleteAllWebWidgetView = true;
	            mModel.startLoader(this, true);
	            mRestoring = false;
	        }
	
	        if(true == isStartPageManager) {
	            isStartPageManager = false;
	        }
	        
	        checkConfiguration();
	        
	        if(!isNeedDelayCheckMissCallUI){
	        	checkMissCallUI();
	        }
	        
	        if(dockStyle == 0){
	        	
	        	//for cmcc abondon style, but look good, need keep it
        		if(mLinePageIndicator != null)//for special move
        		{
        	        mLinePageIndicator.refreshPosition(getResources().getDrawable(R.drawable.page_indicator).getIntrinsicWidth(), 
        				  getResources().getDrawable(R.drawable.page_indicator).getIntrinsicHeight(), screenWidth, screenHeight, mWorkspace.getCurrentScreen(), mWorkspace.getChildCount());
        		}
        	  
        		//for oms2.0 traditional page view
        		PageIndicatorView pView = (PageIndicatorView) mDragLayer.findViewById(R.id.page_manager_iv);
        		if(pView != null)
        		{
        		    pView.drawPageIndicator(mWorkspace.getCurrentScreen(),mWorkspace.getChildCount());
        		}
	        	 
	        }
	        
	        //remove focus
	        if(isAllAppsVisible() == false)
	    	{
	    		 mAllAppsGrid.setFocusable(false);
	    	}
	        
	        if(mAllAppsGrid instanceof AllAppsScreenLayout){
	        	if(((AllAppsScreenLayout)mAllAppsGrid).isNeedRefresh){
	        		((AllAppsScreenLayout)mAllAppsGrid).refreshAllAppsUI();
	        	}
	        }else if(mAllAppsGrid instanceof AllApps2DWithCategory){
	        	if(((AllApps2DWithCategory)mAllAppsGrid).isNeedRefresh){
	        		((AllApps2DWithCategory)mAllAppsGrid).refreshAllAppsUI();
	        	}
	        }
	        
	        //process for camera and lock key
	        //mWorkspace.snapToCurrentScreenNoWallPaperMove();
	       
	       if(mWorkspace.mTmpStopCount != mStopCount){
	    	   mWorkspace.checkPendingInvalidate();
	       }
	        
        }
        
//        Looper.myQueue().addIdleHandler(new Idler());
        
//        mModel.mHandler.postIdle(new Runnable(){
//
//			public void run() {
//				Debug.stopMethodTracing();
//			}
//        	 
//        });
        
//        Log.d(TAG, "onResume exit");
    }
    
    
//    private class Idler implements MessageQueue.IdleHandler {
//        public final boolean queueIdle() {
//            Debug.stopMethodTracing();
//            Log.d(TAG, "Idler Debug stopMethodTracing ====");
//            //stopOmsChart();
//            return false;
//        }
//       
//        private void stopOmsChart(){
//            try{
//                java.io.FileOutputStream stop = new java.io.FileOutputStream("/data/bootchart-stop");
//                stop.write(0x31);
//            }catch(Exception e){
//                android.util.Log.i("b055", "error write stop profile"+e);
//            }
//        }
//    }
    
    boolean isNeedDelayCheckMissCallUI = false;
    private void checkMissCallUI(){
    	new Thread("updateMissCallCount"){
    		public void run(){
    			updateMissCallCount();
    			mCallStateHandler.sendMessage(mCallStateHandler.obtainMessage(Launcher.this.MSG_UPDATEMISSCALL));
    		}
    	}.start();
    }
    
    private final int MSG_UPDATEMISSCALL = 0x100;
    class CallStateHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_UPDATEMISSCALL:
				 if(dockStyle == 0){
					 updateBottomCallIcons();
				 }
				break;			
				case 0x101:
				{
					mWorkspace.postInvalidate();
				}
				break;
			}
			super.handleMessage(msg);			
		}
    }
    
    
    private boolean isRunningPageManager = false;
    public void callbackAfterBindAll()
    {
//    	if(LOGD)Log.d(TAG, "callbackAfterBindAll reset the focus");
    	if(isAllAppsVisible() == false)
    	{
    		 mAllAppsGrid.setFocusable(false);
    	}
    	
//    	if(LOGD)Log.d(TAG, "force invalidate mWorkspace.invalidate();");
    	if(false == isRunningPageManager){
    		mWorkspace.snapToScreen(mWorkspace.getCurrentScreen());
    	}
    }

    //remember orientation when home is at front
    int preOrientation = 0;
    public int mPauseCount = 0;

    @Override
    protected void onPause() {
    	preOrientation = getResources().getConfiguration().orientation;
    	if(LOGD)Log.d(TAG,"onPause orient="+preOrientation);

    	if(mAllAppsGrid instanceof AllAppsScreenLayout && null != ((AllAppsScreenLayout) mAllAppsGrid).mCorpusSelectionDialog){
   			((AllAppsScreenLayout) mAllAppsGrid).mCorpusSelectionDialog.dismiss();
    	}
    	
        mDragController.cancelDrag();
        //process for camera and lock key
        mWorkspace.snapToCurrentScreenNoWallPaperMove();

        if(isNewDraging)
        {
            forceEndNewDrag();
        }
        super.onPause();
        mPauseCount++;
    }
    
    @Override
	protected void onStart() {
    	if(LOGD)Log.d(TAG,"onStart");
		super.onStart();
	}

    public int mStopCount = 0;
	@Override
	protected void onStop() {
		if(LOGD)Log.d(TAG,"onStop");
		super.onStop();
        
		mStopCount++;
		amVisible = false;
		isAvoidMoveBeforeClick = false;
		
//		Debug.stopMethodTracing();
//		isOnstopCalled = true;
	}

	@Override
    public Object onRetainNonConfigurationInstance() {
        // Flag the loader to stop early before switching
        mModel.stopLoader();
        mAllAppsGrid.surrender();
        return Boolean.TRUE;
    }

    // We can't hide the IME if it was forced open.  So don't bother
    /*
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            final InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            inputManager.hideSoftInputFromWindow(lp.token, 0, new android.os.ResultReceiver(new
                        android.os.Handler()) {
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            Log.d(TAG, "ResultReceiver got resultCode=" + resultCode);
                        }
                    });
            Log.d(TAG, "called hideSoftInputFromWindow from onWindowFocusChanged");
        }
    }
    */

    private boolean acceptFilter() {
        final InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        return !inputManager.isFullscreenMode();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = super.onKeyDown(keyCode, event);
        if(isNotOPhone() == true)
        {
	        if (!handled && acceptFilter() && keyCode != KeyEvent.KEYCODE_ENTER) {
	            boolean gotKey = TextKeyListener.getInstance().onKeyDown(mWorkspace, mDefaultKeySsb,
	                    keyCode, event);
	            if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
	                // something usable has been typed - start a search
	                // the typed text will be retrieved and cleared by
	                // showSearchDialog()
	                // If there are multiple keystrokes before the search dialog takes focus,
	                // onSearchRequested() will be called for every keystroke,
	                // but it is idempotent, so it's fine.
	                return onSearchRequested();
	            }
	        }
        }

        // Eat the long press event so the keyboard doesn't come up.
        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            return true;
        }

        return handled;
    }

    private String getTypedText() {
        return mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        mDefaultKeySsb.clear();
        mDefaultKeySsb.clearSpans();
        Selection.setSelection(mDefaultKeySsb, 0);
    }

    /**
     * Restores the previous state, if it exists.
     *
     * @param savedState The previous state.
     */
    private void restoreState(Bundle savedState) {
//    	Log.d(TAG, "resoreState savedState:"+savedState);
        if (savedState == null) {
            return;
        }

        final boolean allApps = savedState.getBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, false);
        if (allApps) {
            //showAllApps(false);        	
        }

        final int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN, -1);
        if (currentScreen > -1) {
            mWorkspace.setCurrentScreen(currentScreen);
        }

        final int addScreen = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);
        if (addScreen > -1) {
            if(LOGD)Log.d(TAG,"addScreen refresh--------------");
            mAddItemCellInfo = new CellLayout.CellInfo();
            final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
            addItemCellInfo.valid = true;
            addItemCellInfo.screen = addScreen;
            addItemCellInfo.cellX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
            addItemCellInfo.cellY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
            addItemCellInfo.spanX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
            addItemCellInfo.spanY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
            addItemCellInfo.findVacantCellsFromOccupied(
                    savedState.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS),
                    savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_X),
                    savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y));
            mRestoring = true;
        }  

        boolean renameFolder = savedState.getBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
        if (renameFolder) {
            long id = savedState.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
            mFolderInfo = mModel.getFolderById(this, mFolders, id);
            mRestoring = true;
        }
    }

    private PageIndicatorLineStyleView mLinePageIndicator;
    private PageIndicatorView mPageIndicator;
    
    /**
     * Finds all the views we need and configure them properly.
     */
    private void setupViews() {
    	Log.i(TAG,"setupViews, orientation:" + getResources().getConfiguration().orientation);
        DragController dragController = mDragController;

        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        final DragLayer dragLayer = mDragLayer;
        dragLayer.setDragController(dragController);

        mAllAppsGrid = (AllAppsView)dragLayer.findViewById(R.id.all_apps_view);
        mAllAppsGrid.setLauncher(this);
        mAllAppsGrid.setDragController(dragController);
        
        // Manage focusability manually since this thing is always visible
        mAllAppsGrid.setFocusable(false); 

        mWorkspace = (Workspace) dragLayer.findViewById(R.id.workspace);
        final Workspace workspace = mWorkspace;
        workspace.setHapticFeedbackEnabled(false);

        if(mAllAppsGrid instanceof AllAppsScreenLayout){
        	mLinePageIndicator =  (PageIndicatorLineStyleView) findViewById(R.id.page_line_indicator);
        	mWorkspace.setPageIndicatorLineStyleView(mLinePageIndicator);
        }

        mDeleteZone = (DeleteZone) dragLayer.findViewById(R.id.delete_zone);

        mHandleView = (HandleView) findViewById(R.id.all_apps_button);
        mHandleView.setLauncher(this);
        mHandleView.setOnClickListener(this);
        //mHandleView.setOnLongClickListener(this);
        
        mMissCallImageView = (IndicatorWithMissCallNumberView) dragLayer.findViewById(R.id.misscall_count_iv);
        
        if(dockStyle==1){
	        ImageView hotseatLeft = (ImageView) findViewById(R.id.hotseat_left);
	        hotseatLeft.setContentDescription(mHotseatLabels[0]);
	        hotseatLeft.setImageDrawable(mHotseatIcons[0]);
	        ImageView hotseatRight = (ImageView) findViewById(R.id.hotseat_right);
	        hotseatRight.setContentDescription(mHotseatLabels[1]);
	        hotseatRight.setImageDrawable(mHotseatIcons[1]);
	
	        mPreviousView = (ImageView) dragLayer.findViewById(R.id.previous_screen);
	        mNextView = (ImageView) dragLayer.findViewById(R.id.next_screen);
	        Drawable previous = mPreviousView.getDrawable();
	        Drawable next = mNextView.getDrawable();
	        mWorkspace.setIndicators(previous, next);
        }else{
        	mPreviousView = (ImageView) dragLayer.findViewById(R.id.start_call_iv);
        	mPreviousView.setOnClickListener(this);
        	mPageIndicator = (PageIndicatorView) dragLayer.findViewById(R.id.page_manager_iv);
        	mPageIndicator.setOnClickListener(this);
        	workspace.setPageIndicator(mPageIndicator);
        }

        //we initialize workspace here for unknown cell count
        workspace.setLauncher(this);
        workspace.initWorkspace();
        workspace.setOnLongClickListener(this);
        workspace.setDragController(dragController);

        mDeleteZone.setLauncher(this);
        mDeleteZone.setDragController(dragController);
        mDeleteZone.setHandle(mHandleView);
          
        if(dockStyle==1){
        	mDeleteZone.setHandle(findViewById(R.id.all_apps_button_cluster));
        }else{
        	mBottomLayout = (RelativeLayout)findViewById(R.id.bottom_layout);
        	mDeleteZone.setBottom(mBottomLayout);
        }
        
        dragController.setLauncher(this);
        dragController.setDragScoller(workspace);
        dragController.setDragListener(mDeleteZone);
        dragController.setScrollView(dragLayer);
        dragController.setMoveTarget(workspace);

        // The order here is bottom to top.
        dragController.addDropTarget(workspace);
        dragController.addDropTarget(mDeleteZone);
    }

    
    class ShortCutAnimationListener implements Animation.AnimationListener{
        private FolderInfo folderInfo;
        
        public void setFolderInfo(FolderInfo tag) {
            folderInfo = tag;
        }
        
        public void onAnimationEnd(Animation animation) {
            if(folderInfo != null){
//                if(Launcher.LOGD)Log.d(TAG,"onAnimationEnd ------------------------");
                if (!folderInfo.opened) {
                    // Close any open folder
                    closeFolder();
                    // Open the requested folder
                    openFolder(folderInfo); 
                } else {
                    // Find the open folder...
                    Folder openFolder = mWorkspace.getFolderForTag(folderInfo);
                    int folderScreen;
                    if (openFolder != null) {
                        folderScreen = mWorkspace.getScreenForView(openFolder);
                        // .. and close it
                        closeFolder(openFolder);
                        if (folderScreen != mWorkspace.getCurrentScreen()) {
                            // Close any folder open on the current screen
                            closeFolder();
                            // Pull the folder onto this screen
                            openFolder(folderInfo);
                        }
                    }
                }
            }
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
    }

    
    @SuppressWarnings({"UnusedDeclaration"})
    public void previousScreen(View v) {
        if(LOGD)Log.d(TAG,"previousScreen");
        if (!isAllAppsVisible()) {
            mWorkspace.scrollLeft();
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void nextScreen(View v) {
        if(LOGD)Log.d(TAG,"nextScreen");
        if (!isAllAppsVisible()) {
            mWorkspace.scrollRight();
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void launchHotSeat(View v) {
        if (isAllAppsVisible()) return;

        int index = -1;
        if (v.getId() == R.id.hotseat_left) {
            index = 0;
        } else if (v.getId() == R.id.hotseat_right) {
            index = 1;
        }

        // reload these every tap; you never know when they might change
        loadHotseats();
        if (index >= 0 && index < mHotseats.length && mHotseats[index] != null) {
            Intent intent = mHotseats[index];
            startActivitySafely(
                mHotseats[index],
                "hotseat"
            );
        }
    }
    
    /**
     * Creates a view representing a shortcut.
     *
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from R.layout.application.
     */
    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.application,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), info);
    }

    /**
     * Creates a view representing a shortcut inflated from the specified resource.
     *
     * @param layoutResId The id of the XML layout used to create the shortcut.
     * @param parent The group the shortcut belongs to.
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from layoutResId.
     */
    View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
        TextView favorite = (TextView) mInflater.inflate(layoutResId, parent, false);
        //favorite.setText(info.title);
        // favorite.setTag(info);
        //check whether webwidget icon, cn003@borqs.com
        String title = null;
        
        if(info.intent != null){
        	title = getWidgetIconTitileById(info.intent.getStringExtra("Widget"));
        }
        
        if (title != null)
        {
            favorite.setText(title);
        }
        else
        {
            favorite.setText(info.title);
        }
        
        Bitmap icon = null;
        if(info.intent != null){
        	final ComponentName cn = info.intent.getComponent();
        	if(cn != null && "com.android.calendar".equals(cn.getPackageName())){
        		icon = Utilities.createIconBitmap(getResources().getDrawable(R.drawable.app_icon_calendar), this);
        	}else{
        		icon = info.getIcon(mIconCache);
        	}
        }else{
        	icon = info.getIcon(mIconCache);
        }
        
        favorite.setCompoundDrawablesWithIntrinsicBounds(null,new FastBitmapDrawable(icon),null, null);
        
        favorite.setTag(info);
        favorite.setOnClickListener(this);

        return favorite;
    }

    /*
     * Get webwidget icon title by widget uuid to support multi langues
     *
     * cn003@April.2010
     */
    private String getWidgetIconTitileById(String uuid)
    {
        if (uuid == null)
        {
            return null;
        }

        //TODO: This should not be hard code, but need define in WidgetTable.java, fix later
        Uri contentUri1 =Uri.parse("content://widgetmanager/widget/widgetName");
        Uri queryUri = contentUri1.buildUpon().appendPath(uuid).build();
        Cursor c = getContentResolver().query(queryUri, 
                    new String[] {"name"}, null, null, null);
        if (c == null || !c.moveToFirst()) 
        {
            return null;     	
        } 
        else
        {
            String name = c.getString(c.getColumnIndex("name"));
            c.close();
			return name;
        }

    }
	
    /**
     * Add an application shortcut to the workspace.
     *
     * @param data The intent describing the application.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    void completeAddApplication(Context context, Intent data, CellLayout.CellInfo cellInfo) {
    	if(LOGD)Log.d(TAG, "completeAddApplication  intent:"+data);
        cellInfo.screen = mWorkspace.getCurrentScreen();
        if (!findSingleSlot(cellInfo)) return;

        final ShortcutInfo info = mModel.getShortcutInfo(context.getPackageManager(),
                data, context);
        //close open folder
        closeFolder();

        if (info != null) {
            info.setActivity(data.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            info.container = ItemInfo.NO_ID;
            mWorkspace.addApplicationShortcut(info, cellInfo, isWorkspaceLocked());
        } else {
            Log.e(TAG, "Couldn't find ActivityInfo for selected application: " + data);
        }   
    }

    /**
     * Add a shortcut to the workspace.
     *
     * @param data The intent describing the shortcut.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    private void completeAddShortcut(Intent data, CellLayout.CellInfo cellInfo) {
        if(LOGD)Log.d(TAG,"completeAddShortcut");
        
    	// Close any open folder
        closeFolder();
        
        cellInfo.screen = mWorkspace.getCurrentScreen();
        if (!findSingleSlot(cellInfo)) return;

        final ShortcutInfo info = mModel.addShortcut(this, data, cellInfo, false);

        if (!mRestoring) {
            final View view = createShortcut(info);
            mWorkspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY, 1, 1,
                    isWorkspaceLocked());
        }
    }
    
    public boolean getRestoring(){
    	return mRestoring;
    }

    //get app widget information from xml raw data
    private AppWidgetProviderInfo parseProviderInfoXml(PackageManager mPackageManager,  ComponentName component, ResolveInfo ri) {
    	AppWidgetProviderInfo info = null;

    	final DisplayMetrics  metrics = this.getResources().getDisplayMetrics(); 
        ActivityInfo activityInfo = ri.activityInfo;
        XmlResourceParser parser = null;
        try {
            parser = activityInfo.loadXmlMetaData(mPackageManager,
                    AppWidgetManager.META_DATA_APPWIDGET_PROVIDER);
            if (parser == null) {
                Log.w(TAG, "No " + AppWidgetManager.META_DATA_APPWIDGET_PROVIDER + " meta-data for "
                        + "AppWidget provider '" + component + '\'');
                return null;
            }

            AttributeSet attrs = Xml.asAttributeSet(parser);

            int type;
            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                    && type != XmlPullParser.START_TAG) {
                // drain whitespace, comments, etc.
            }

            String nodeName = parser.getName();
            if (!"appwidget-provider".equals(nodeName)) {
                Log.w(TAG, "Meta-data does not start with appwidget-provider tag for"
                        + " AppWidget provider '" + component + '\'');
                return null;
            }

           
            info =  new AppWidgetProviderInfo();

            info.provider = component;
            
            TypedArray sa = getResources().obtainAttributes(attrs,
                    com.android.internal.R.styleable.AppWidgetProviderInfo);

            // These dimensions has to be resolved in the application's context.
            // We simply send back the raw complex data, which will be
            // converted to dp in {@link AppWidgetManager#getAppWidgetInfo}.
            TypedValue value = sa.peekValue(
                    com.android.internal.R.styleable.AppWidgetProviderInfo_minWidth);
            info.minWidth = value != null ? value.data : 0;
            
            info.minWidth = TypedValue.complexToDimensionPixelSize(info.minWidth, metrics);
            value = sa.peekValue(com.android.internal.R.styleable.AppWidgetProviderInfo_minHeight);
            info.minHeight = value != null ? value.data : 0;
            info.minHeight = TypedValue.complexToDimensionPixelSize(info.minHeight, metrics);

            info.updatePeriodMillis = sa.getInt(
                    com.android.internal.R.styleable.AppWidgetProviderInfo_updatePeriodMillis, 0);
            info.initialLayout = sa.getResourceId(
                    com.android.internal.R.styleable.AppWidgetProviderInfo_initialLayout, 0);
            String className = sa.getString(
                    com.android.internal.R.styleable.AppWidgetProviderInfo_configure);
            if (className != null) {
                info.configure = new ComponentName(component.getPackageName(), className);
            }
            info.label = activityInfo.loadLabel(mPackageManager).toString();
            info.icon = ri.getIconResource();
            sa.recycle();
        } catch (Exception e) {
            // Ok to catch Exception here, because anything going wrong because
            // of what a client process passes to us should not be fatal for the
            // system process.
            Log.w(TAG, "XML parsing failed for AppWidget provider '" + component + '\'', e);
            return null;
        } finally {
            if (parser != null) parser.close();
        }
        return info;
    }

    
    /**
     * Add a widget to the workspace.
     *
     * @param data The intent describing the appWidgetId.
     * @param cellInfo The position on screen where to create the widget.
     */
    private void completeAddAppWidget(Intent data, CellLayout.CellInfo cellInfo) {
    	
    	// Close any open folder
        closeFolder();
        
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        if (LOGD) Log.d(TAG, "completeAddAppWidget dumping extras content=" + extras.toString());

        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        //begin get the app widget info from xml again to fix the landscape and portrait switch
        final PackageManager pm = this.getPackageManager();
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(appWidgetInfo.provider);
        List<ResolveInfo> ri_list = pm.queryBroadcastReceivers(intent, PackageManager.GET_META_DATA);  
        if(ri_list != null && ri_list.size() > 0)
        {
        	AppWidgetProviderInfo tmp = parseProviderInfoXml(pm, appWidgetInfo.provider, ri_list.get(0));
        	if(tmp != null)
        	{
        		appWidgetInfo.minHeight  = tmp.minHeight;
        		appWidgetInfo.minWidth  = tmp.minWidth;
        		
        		tmp = null;
        	}
        }        
        //end for re-query the min width and height for configuration change

        // Calculate the grid spans needed to fit this widget
        CellLayout layout = (CellLayout) mWorkspace.getChildAt(cellInfo.screen);
        int[] spans = layout.rectToCell(appWidgetInfo.minWidth, appWidgetInfo.minHeight);

        Log.d(TAG, "completeAddAppWidget screen:"+cellInfo.screen+" spans0:"+spans[0]+" spans1:"+spans[1]);
        // Try finding open space on Launcher screen
        final int[] xy = mCellCoordinates;
        if (!findSlot(cellInfo, xy, spans[0], spans[1],false)) {
//            if (appWidgetId != -1) mAppWidgetHost.deleteAppWidgetId(appWidgetId);
//            return;
            //createView in current position. can find sutiable celllayout
           //  int index = findFitableSlotIndex(cellInfo,xy,spans[0],spans[1]);
            startNewDrag(data, APPWIDGET, cellInfo);
            return;
        }

        // Build Launcher-specific widget info and save to database
        LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId);
        launcherInfo.spanX = spans[0];
        launcherInfo.spanY = spans[1];
        launcherInfo.packageName = appWidgetInfo.provider.getPackageName();
        launcherInfo.className   = appWidgetInfo.provider.getClassName();

        LauncherModel.addItemToDatabase(this, launcherInfo,
                LauncherSettings.Favorites.CONTAINER_DESKTOP,
                mWorkspace.getCurrentScreen(), xy[0], xy[1], false);

        if (!mRestoring) {
            mDesktopItems.add(launcherInfo);

            // Perform actual inflation because we're live
            launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
            launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            launcherInfo.hostView.setTag(launcherInfo);

            Log.d(TAG, "completeAddAppWidget packageName:"+launcherInfo.packageName);
           if(launcherInfo.packageName.equals("oms.dcd") && (launcherInfo.hostView.getChildAt(0) instanceof ViewGroup)){
	        	widgetHostViews.add(launcherInfo.hostView);
	        }
            mWorkspace.addInCurrentScreen(launcherInfo.hostView, xy[0], xy[1],
                    launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());
        }
    }

    public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
//    	if(LOGD)Log.d(TAG, "removeAppWidget");
    	if(launcherInfo.packageName.equals("oms.dcd")){
    		for(int i=0; i<widgetHostViews.size(); i++){
    			AppWidgetHostView tmp = widgetHostViews.get(i);
    			if(tmp == launcherInfo.hostView){
    				widgetHostViews.remove(tmp);
    				break;
    			}
    		}
    	}

    	launcherInfo.unbind();
        mDesktopItems.remove(launcherInfo);
        if(dockStyle==1){
             launcherInfo.hostView = null;
        }
    }
 
    public LauncherAppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    void closeSystemDialogs() {
        getWindow().closeAllPanels();
        
        QuickLauncher.dissmissHint();

        try {
            dismissDialog(DIALOG_CREATE_SHORTCUT);
            // Unlock the workspace if the dialog was showing
        } catch (Exception e) {
            // An exception is thrown if the dialog is not visible, which is fine
        }

        try {
            dismissDialog(DIALOG_RENAME_FOLDER);
            // Unlock the workspace if the dialog was showing
        } catch (Exception e) {
            // An exception is thrown if the dialog is not visible, which is fine
        }

        // Whatever we were doing is hereby canceled.
        mWaitingForResult = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
//    	if(LOGD)Log.d(TAG, "onNewIntent");
        super.onNewIntent(intent);

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            // also will cancel mWaitingForResult.
            closeSystemDialogs();

            boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                        != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            boolean allAppsVisible = isAllAppsVisible();
           /* if (!mWorkspace.isDefaultScreenShowing()) {
                mWorkspace.moveToDefaultScreen(alreadyOnHome && !allAppsVisible);
            }*/
            closeAllApps(alreadyOnHome && allAppsVisible);

            final View v = getWindow().peekDecorView();
            if (v != null && v.getWindowToken() != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	if(LOGD)Log.d(TAG,"onRestoreInstanceState  savedInstanceState:"+savedInstanceState);
        // Do not call super here
        mSavedInstanceState = savedInstanceState;
        
        //dont restore the dialog , refer to: Activity.restoreManagedDialogs
        mSavedInstanceState.putBundle("android:savedDialogs", null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	if(LOGD)Log.d(TAG,"onSaveInstanceState  outState:"+outState);
        outState.putInt(RUNTIME_STATE_CURRENT_SCREEN, mWorkspace.getCurrentScreen());

        final ArrayList<Folder> folders = mWorkspace.getOpenFolders();
        if (folders.size() > 0) {
            final int count = folders.size();
            long[] ids = new long[count];
            for (int i = 0; i < count; i++) {
                final FolderInfo info = folders.get(i).getInfo();
                ids[i] = info.id;
            }
            outState.putLongArray(RUNTIME_STATE_USER_FOLDERS, ids);
        } else {       
            //super.onSaveInstanceState(outState);
        }

        // TODO should not do this if the drawer is currently closing.
        if (isAllAppsVisible()) {
            outState.putBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, true);
        }

        if (mAddItemCellInfo != null && mAddItemCellInfo.valid && mWaitingForResult) {
            final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
            
            if(addItemCellInfo.screen >=0 && addItemCellInfo.screen < mWorkspace.getChildCount()){
            	final CellLayout layout = (CellLayout) mWorkspace.getChildAt(addItemCellInfo.screen);
            	
            	outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN, addItemCellInfo.screen);
            	outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X, addItemCellInfo.cellX);
            	outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y, addItemCellInfo.cellY);
            	outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X, addItemCellInfo.spanX);
            	outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y, addItemCellInfo.spanY);
            	outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_X, layout.getCountX());
            	outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y, layout.getCountY());
            	outState.putBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS,
            			layout.getOccupiedCells());
            }
        }

        if (mFolderInfo != null && mWaitingForResult) {
            outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
            outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID, mFolderInfo.id);
        }
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "onDestroy="+this);
        
        super.onDestroy();

        try {
        	mAppWidgetHost.stopListening();        	        	
        	mAppWidgetHost = null;
        } catch (Exception ex) {
            Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction"+ex.getMessage());
        }

        TextKeyListener.getInstance().release();

        mModel.stopLoader();
        
        if(LauncherORM.instanceUnSafe() != null)
        {
        	LauncherORM.instanceUnSafe().destroy();
        }

        unbindDesktopItems();

        getContentResolver().unregisterContentObserver(mWidgetObserver);
     
        unregisterReceiver(mCloseSystemDialogsReceiver);
        unregisterReceiver(mPhoneStateReceiver);
        unregisterReceiver(mHomeKeyReceiver);
        
        getContentResolver().unregisterContentObserver(mCallLogObserver);        

        if(isNeedStartWebWidgetManager == false){
        }

        //SKIP
        unregisterReceiver(mSkipMotionReceiver);
        //SKIP
       
        unregisterReceiver(mCalendarReceiver);
        unregisterReceiver(mCalendarRefreshReceiver);      
        unregisterReceiver(mHomeChangeReceiver);
        unregisterReceiver(pageManagerReceiver);
        unregisterReceiver(mInstallShortcutReceiver);
        unregisterReceiver(mUnInstallShortcutReceiver);
        unregisterReceiver(mRestoreBackupIntentReceiver);
        unregisterReceiver(mReloadReceiver);
        
        Workspace.clearPageViews();
        
        //close current opened all app view
        closeAllApps(false);
        if(null != mDragLayer){
//        	if (LOGD)Log.d(TAG, "mDragLayer removeAllViews ===================== ");
        	mDragLayer.removeAllViews();
        }
        
        //after UI Views removed, the invalidate will not take effect to bitmap
        //ticket 10062
        mIconCache.unbindDrawables();        
        mIconCache.flush();
        
        //add for orientation change.
        Utilities.clearStaticData();
        CalendarReceiver.clearStaticData();
        PageIndicatorView.clearStaticData();
        IndicatorWithMissCallNumberView.clearStaticData();
        Category.clearData();
        
        System.gc();
        displayConfigs = DISPLAY_CONFIG_DEFAULT_2D;
        
        if(mKillProcessByHomeChange) {
//        	if (LOGD)Log.d(TAG, "exit by home change");
        	mKillProcessByHomeChange = false;
        	try{
        		 ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        		 am.restartPackage(getPackageName());
        	}catch(Exception ex){
        		if (LOGD)Log.d(TAG, "exit by home change with -1");
        	}
        }
    
//        QuickLauncher.dissmissQuickAction();
        Log.i(TAG, "onDestroy ok"+this);
    }
    
    private class ReloadIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
        	 String action = intent.getAction();
        	 mModel.isHaveSdcardMountMessage = false;
        	
        	 String packages[]=null;
        	 if(LOGD)Log.d(TAG, "ReloadIntentReceiver intent="+intent + " resume from last sdcard="+intent.getBooleanExtra("reload_after_finish_bind", false));
        	 if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
                 packages = intent.getStringArrayExtra(
                         Intent.EXTRA_CHANGED_PACKAGE_LIST);
                 if (packages == null || packages.length == 0) {
                	 Log.d(TAG, "ReloadIntentReceiver ACTION_EXTERNAL_APPLICATIONS_AVAILABLE no packages");
                     return;
                 }     

				 mModel.unSetLoaded();
				 
            } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
                 packages = intent.getStringArrayExtra(
                         Intent.EXTRA_CHANGED_PACKAGE_LIST);
                 if (packages == null || packages.length == 0) {
                	 Log.d(TAG, "ReloadIntentReceiver ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE no packages");
                     return;
                 }        

				 mModel.unSetLoaded();
            }
        	 
        	if(packages != null && mModel.ExistExternalAppAtDesktop(packages.clone(), true) == false)
         	{
         		Log.i(TAG,"exist one sdcard load, and no sdcard app in desktop, reload app only");
         		//Need reload all app UI
         		Launcher.this.refreshApp();
         		return ;
         	}
            
        	if(isNeedListenSdcardMount){
        		try{
        			//to ignore multi-message when reload by remount sdcard
        			isNeedListenSdcardMount = false;
        			if(amVisible)
        			{
                        if(LOGD)Log.d(TAG,"I am in front, reload all home");        			
	        			refreshWorkspace();
        			}
        			else
        			{
        				Log.i(TAG,"I am in background");
        				sdcardReloadNotDone = true;
        			}
        		}catch(Exception ne)
        		{
        			if(LOGD)Log.d(TAG, "ReloadIntentReceiver 2");
        		}
        	}else{
        		if(mWorkspaceLoading)
        		{
        			//send reload message to Home 
        			sdcardReloadNotDone = true;
        		}
        		Log.i(TAG,"ignore reload intent");
        	}
        }
    }
    
    boolean mKillProcessByHomeChange = false;
    private class HomeChangeReceiver extends BroadcastReceiver 
    {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            if(intent.getAction().equals("oms.action.HOME_CHANGED")) 
            {
            	Log.i(TAG, "oms.action.HOME_CHANGED");
            	
                final String homeName = intent.getStringExtra("configured_home");
                if(homeName!= null && !homeName.equals("com.android.omshome.Launcher")) 
                {
                	Log.d(TAG, "home changed to ="+homeName);
                	
                    if(null != mDragLayer){
                    	Log.d(TAG, "mDragLayer removeAllViews ===================== ");
                    	mDragLayer.removeAllViews();
                    }                    
                    
                    mKillProcessByHomeChange = true;
                    Launcher.this.finish(); 
                    try{                   
                        Process.killProcess(Process.myPid());                	              	
                    }catch(Exception ne){}
                }                
            }
        }
    }
    
    private void registerCalendarReceiver() {
        IntentFilter calFilter = new IntentFilter();

        //calFilter.addAction(Intent.ACTION_TIME_TICK);
        calFilter.addAction(Intent.ACTION_TIME_CHANGED);
        calFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        
        calFilter.addAction(Launcher.ACTION_ITEM_ADDED);
        calFilter.addAction(Launcher.ACTION_LOAD_COMPLETE);

        registerReceiver(mCalendarReceiver, calFilter);
    }

    //SKIP
    private class SkipMotionIntentReceiver extends BroadcastReceiver
    {
       @Override
       public void onReceive(Context context, Intent intent) 
       {
            if(LOGD)Log.d(TAG,"Received SKIP_MOTION");
            if(intent.getAction().equals(SKIP_MOTION)){
                mWorkspace.skipMotion();
            } else if (intent.getAction().equals(SKIP_LONGCLICK)){
                skipLongClick = true;
            } else if (intent.getAction().equals(RESET_SKIP_LONGCLICK)){
                skipLongClick = false;
            }
       }
    }
    //SKIP
    
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if(LOGD)Log.d(TAG,"startActivityForResult intent:"+intent+" requestCode:"+requestCode);
        if (requestCode >= 0) mWaitingForResult = true;
        
        mWorkspace.temporaryDisableInvalidate(); 
        super.startActivityForResult(intent, requestCode);
    }
    
    public void startActivityForResultWithInvalidate(Intent intent, int requestCode) {
        if(LOGD)Log.d(TAG,"startActivityForResultWithInvalidate intent:"+intent+" requestCode:"+requestCode);
        if (requestCode >= 0) mWaitingForResult = true;
        
        super.startActivityForResult(intent, requestCode);
    }
    
    public static boolean isNotOPhone()
    {
    	String ophone = SystemProperties.get("apps.setting.platformversion");
    	if(ophone!=null && (ophone.contains("OPhone") || ophone.contains("OMS")))
    	{
    		return false;
    	}
    	
    	return true;
    }

    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery,
            Bundle appSearchData, boolean globalSearch) {

        closeAllApps(true);

        if (initialQuery == null) {
            // Use any text typed in the launcher as the initial query
            initialQuery = getTypedText();
            Log.d(TAG, "user pressed initialQuery="+initialQuery);
            clearTypedText();
        }
       

        if(isNotOPhone() == true)
        {
        	if (appSearchData == null) {
                appSearchData = new Bundle();
            }
        	 
	        final SearchManager searchManager =
	                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	        searchManager.startSearch(initialQuery, selectInitialQuery, getComponentName(),
	            appSearchData, globalSearch);
        }
        else
        {        
	        Uri SERVO_SEARCH_URI = Uri.parse("content://search");
	        String content = initialQuery;
	    	content = content.toLowerCase();                	
	    	Intent sintent = new Intent(Intent.ACTION_VIEW, SERVO_SEARCH_URI).putExtra("SEARCH", content.toString());//here, content is what you want to search.
	    	
	        sintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        startActivity(sintent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isWorkspaceLocked()) {
//        	Log.d(TAG, " mWorkspaceLoading="+ mWorkspaceLoading + " mWaitingForResult="+ mWaitingForResult);
            return false;
        }

        super.onCreateOptionsMenu(menu);

        menu.add(MENU_GROUP_ADD, MENU_ADD, 0, R.string.menu_add)
            .setIcon(R.drawable.cmcc_toolbar_add).setAlphabeticShortcut('A');
        
        menu.add(MENU_GROUP_WALLPAPER, MENU_WALLPAPER_SETTINGS, 0, R.string.menu_wallpaper)
            .setIcon(R.drawable.cmcc_toolbar_wallpaper).setAlphabeticShortcut('W');

        if(showGoogleSearch)
        {
            menu.add(0, MENU_SEARCH, 0, R.string.menu_search)
                .setIcon(android.R.drawable.ic_search_category_default)
                .setAlphabeticShortcut(SearchManager.MENU_KEY);
        }

        menu.add(0, MENU_NOTIFICATIONS, 0, R.string.menu_notifications)
            .setIcon(R.drawable.cmcc_toolbar_notifications).setAlphabeticShortcut('N');

        menu.add(0, MENU_CHOOSEHOME, 0, R.string.menu_home)
            .setIcon(R.drawable.cmcc_toolbar_home).setAlphabeticShortcut('H');
        
        final Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        
        menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings)
            .setIcon(R.drawable.cmcc_toolbar_setting).setAlphabeticShortcut('P')
            .setIntent(settings);
        
        if(isUsingHomeSettings()){
        	menu.add(0, MENU_HOMESETTINGS, 0, R.string.menu_home_settings)
        	.setIcon(R.drawable.cmcc_toolbar_setting).setAlphabeticShortcut('S');
        }
            
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

//         If all apps is animating, don't show the menu, because we don't know
//         which one to show.
        if (mAllAppsGrid.isVisible()) {
            return false;
        }
        
          MenuItem addMenuItem = menu.getItem(0);
        mMenuAddInfo = mWorkspace.findAllVacantCells(null);
        if(mMenuAddInfo != null && true == mMenuAddInfo.valid) {
            addMenuItem.setVisible(true);
        } else {
            addMenuItem.setVisible(false);
        }
       // menu.setGroupEnabled(MENU_GROUP_ADD, mMenuAddInfo != null && mMenuAddInfo.valid);

        MenuItem homeMenuItem = menu.getItem(3);
        if(true == getHomes()) {
            homeMenuItem.setVisible(true);
        } else {
            homeMenuItem.setVisible(false);
        }

        // Only show the add and wallpaper options when we're not in all apps.
        boolean visible = !mAllAppsGrid.isOpaque();
        menu.setGroupVisible(MENU_GROUP_ADD, visible);
        menu.setGroupVisible(MENU_GROUP_WALLPAPER, visible);

        // Disable add if the workspace is full.
        if (visible) {
            mMenuAddInfo = mWorkspace.findAllVacantCells(null);
            menu.setGroupEnabled(MENU_GROUP_ADD, mMenuAddInfo != null && mMenuAddInfo.valid);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	            case MENU_ADD:
	            	mMenuAddInfo = mWorkspace.findAllVacantCells(null);
	                if(mMenuAddInfo != null && true == mMenuAddInfo.valid) {
	                	addItems();
	                }else{
	                	Toast.makeText(this, this.getResources().getString(R.string.out_of_space), Toast.LENGTH_SHORT).show();
	                }
	                return true;
	            case MENU_WALLPAPER_SETTINGS:
	                startWallpaper();
	                return true;
	            case MENU_SEARCH:
	                onSearchRequested();
	                return true;
	            case MENU_NOTIFICATIONS:
	                showNotifications();
	                return true;
	            case MENU_CHOOSEHOME:
	                showHomes();
	                return true;
	            case MENU_HOMESETTINGS:
	            	final Intent intent = new Intent(this, HomeSettingPreference.class);
	            	intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	            	startActivityForResult(intent, REQUEST_HOME_SETTING);
	        }
       
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Indicates that we want global search for this activity by setting the globalSearch
     * argument for {@link #startSearch} to true.
     */

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, true);
        return true;
    }

    public boolean isWorkspaceLocked() {
        return mWorkspaceLoading || mWaitingForResult || isPageMoving || isAvoidMoveBeforeClick;
    }

    private void addItems() {
        closeAllApps(true);
        showAddDialog(mMenuAddInfo);
    }

    void addAppWidget(Intent data) {
        // TODO: catch bad widget exception when sent
        int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
       AppWidgetProviderInfo appWidget = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        if (appWidget.configure != null) {
            // Launch over to configure widget, if needed
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidget.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            startActivityForResultSafely(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            // Otherwise just add it
            onActivityResult(REQUEST_CREATE_APPWIDGET, Activity.RESULT_OK, data);
        }
    }
   
      //Add Webwidget, cn003@March.2010
    private void addWebWidget(Intent data) 
    {
    }

    void processShortcut(Intent intent) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (applicationName != null && applicationName.equals(shortcutName)) {
            AllAppsListDialog aalDialog = new AllAppsListDialog(this, false);
            aalDialog.createDialog(Category.getAppsByCategory(Category.CATEGORY_ALLAPP)).show();
            
          //  showAllApps(true,true);
        } else {
            startActivityForResult(intent, REQUEST_CREATE_SHORTCUT);
        }
    }

    void addLiveFolder(Intent intent) {
        // Handle case where user selected "Folder"
        String folderName = getResources().getString(R.string.group_folder);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

//        if(LOGD)Log.d(TAG,"addLiveFolder  folderName:"+folderName+" shortcutName:"+shortcutName);
        
        if (folderName != null && folderName.equals(shortcutName)) {
        	
        	// Close any open folder
            closeFolder();
            
            addFolder();
        } else {
        	startActivityForResultWithInvalidate(intent, REQUEST_CREATE_LIVE_FOLDER);
        }
    }

    void addFolder() {
//        if(LOGD)Log.d(TAG,"addFolder");
        UserFolderInfo folderInfo = new UserFolderInfo();
        folderInfo.title = getText(R.string.folder_name);

        CellLayout.CellInfo cellInfo = mAddItemCellInfo;
        cellInfo.screen = mWorkspace.getCurrentScreen();
        if (!findSingleSlot(cellInfo)) return;

        // Update the model
        LauncherModel.addItemToDatabase(this, folderInfo,
                LauncherSettings.Favorites.CONTAINER_DESKTOP,
                mWorkspace.getCurrentScreen(), cellInfo.cellX, cellInfo.cellY, false);
        mFolders.put(folderInfo.id, folderInfo);

        // Create the view
        FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), folderInfo);
        mWorkspace.addInCurrentScreen(newFolder,
                cellInfo.cellX, cellInfo.cellY, 1, 1, isWorkspaceLocked());
        
    }

    void removeFolder(FolderInfo folder) {
    	folder.unbind();
        mFolders.remove(folder.id);
    }

    private void completeAddLiveFolder(Intent data, CellLayout.CellInfo cellInfo) {
    	if(LOGD)Log.d(TAG,"completeAddLiveFolder intent:"+data);
        cellInfo.screen = mWorkspace.getCurrentScreen();
        if (!findSingleSlot(cellInfo)) return;

        
        // Close any open folder
        closeFolder();
        
        final LiveFolderInfo info = addLiveFolder(this, data, cellInfo, false);

        if (!mRestoring) {
            final View view = LiveFolderIcon.fromXml(R.layout.live_folder_icon, this,
                    (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), info);
                mWorkspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY, 1, 1,
                        isWorkspaceLocked());
        }
    }

    static LiveFolderInfo addLiveFolder(Context context, Intent data,
            CellLayout.CellInfo cellInfo, boolean notify) {
        if(LOGD)Log.d(TAG,"addLiveFolder intent:"+data);
        Intent baseIntent = data.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT);
        String name = data.getStringExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME);

        Drawable icon = null;
        Intent.ShortcutIconResource iconResource = null;

        Parcelable extra = data.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON);
        if (extra != null && extra instanceof Intent.ShortcutIconResource) {
            try {
                iconResource = (Intent.ShortcutIconResource) extra;
                final PackageManager packageManager = context.getPackageManager();
                Resources resources = packageManager.getResourcesForApplication(
                        iconResource.packageName);
                final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                icon = resources.getDrawable(id);
            } catch (Exception e) {
                Log.w(TAG, "Could not load live folder icon: " + extra);
            }
        }

        if (icon == null) {
            icon = context.getResources().getDrawable(R.drawable.ic_launcher_folder);
        }

        final LiveFolderInfo info = new LiveFolderInfo();
        info.icon = Utilities.createIconBitmap(icon, context);
        info.title = name;
        info.iconResource = iconResource;
        info.uri = data.getData();
        info.baseIntent = baseIntent;
        info.displayMode = data.getIntExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
                LiveFolders.DISPLAY_MODE_GRID);

        LauncherModel.addItemToDatabase(context, info, LauncherSettings.Favorites.CONTAINER_DESKTOP,
                cellInfo.screen, cellInfo.cellX, cellInfo.cellY, notify);
        mFolders.put(info.id, info);

        return info;
    }

    public boolean findSingleSlot(CellLayout.CellInfo cellInfo) {
        final int[] xy = new int[2];
        if (findSlot(cellInfo, xy, 1, 1)) {
            cellInfo.cellX = xy[0];
            cellInfo.cellY = xy[1];
            return true;
        }
        return false;
    }

    private boolean findSlot(CellLayout.CellInfo cellInfo, int[] xy, int spanX, int spanY) {
        if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
            boolean[] occupied = mSavedState != null ?
                    mSavedState.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS) : null;
            cellInfo = mWorkspace.findAllVacantCells(occupied);
            if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
                Toast.makeText(this, getString(R.string.out_of_space), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void showNotifications() {
        final StatusBarManager statusBar = (StatusBarManager) getSystemService(STATUS_BAR_SERVICE);
        if (statusBar != null) {
            statusBar.expand();
        }
    }

    private void startWallpaper() {
        closeAllApps(true);
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper,
                getText(R.string.chooser_wallpaper));
        // NOTE: Adds a configure option to the chooser if the wallpaper supports it
        //       Removed in Eclair MR1
//        WallpaperManager wm = (WallpaperManager)
//                getSystemService(Context.WALLPAPER_SERVICE);
//        WallpaperInfo wi = wm.getWallpaperInfo();
//        if (wi != null && wi.getSettingsActivity() != null) {
//            LabeledIntent li = new LabeledIntent(getPackageName(),
//                    R.string.configure_wallpaper, 0);
//            li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
//            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { li });
//        }
        startActivityForResult(chooser, REQUEST_PICK_WALLPAPER);
    }

    /**
     * Registers various content observers. The current implementation registers
     * only a favorites observer to keep track of the favorites applications.
     */
    private void registerContentObservers() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI,
                true, mWidgetObserver);
        
        resolver.registerContentObserver(CallLog.Calls.CONTENT_URI, 
                false, mCallLogObserver);    
        
    }

    private void processHomeKeyPressed()
    {
    	//for none-OPhone
    	if(isNotOPhone())
    	{
    		try{
		    	Configuration configuration = getResources().getConfiguration();
				if(configuration.orientation != Configuration.ORIENTATION_PORTRAIT && Configuration.HARDKEYBOARDHIDDEN_YES == configuration.hardKeyboardHidden)							
				{
					 Log.d(TAG, "home key in ORIENTATION_LAND");
				}
				else
				{
					if(mPaused == false && Launcher.this.isAllAppsVisible() == false)//I am already have focus and all app is not visible
					{
						if(mWorkspace!=null && mWorkspace.getScroller()!=null)
						{
							if(mWorkspace.getScroller().isFinished())
							{
								final ContentResolver cr = getContentResolver();
								int defaultPage = Workspace.DEFAULT_CURRENT_SCREEN;
								try{
									defaultPage = getSettingsIntValue(Launcher.this, LauncherORM.default_page_index);
								}catch(Exception ne){}
						
								if(defaultPage >= mWorkspace.getChildCount())
								{
									defaultPage = mWorkspace.getChildCount()/2;
								}							
								setSettingsIntValue(Launcher.this, LauncherORM.default_page_index, defaultPage);
								showPage(defaultPage, 0);
							}
						}
					}
				}
    		}catch(Exception ne){}
    	}
		closeAllApps(true);
    }
    
    View preFocusView = null;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//    	if(LOGD)Log.d(TAG, "dispatchKeyEvent key:"+event.getKeyCode()+" action:"+event.getAction());
    	if(isNewDraging)
    	{
    	    forceEndNewDrag();
    	}
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            preFocusView = getWindow().getCurrentFocus();
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_HOME:
                	if(LOGD)Log.d(TAG, "home key is coming down");
                	closeAllApps(true);
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (SystemProperties.getInt("debug.launcher2.dumpstate", 0) != 0) {
                        dumpState();
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_0:
                	if(event.isLongPress()){
    	            	Intent call = new Intent(Intent.ACTION_DIAL);
    	            	call.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	            	call.putExtra("number", '+');
    	            	startActivity(call);
        			}    		
                	break;
        		case KeyEvent.KEYCODE_POUND:
        			if(event.isLongPress()){
        				mIsLongPressed = true;
//        				AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        		        if(mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
//        		            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
//        		            mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,AudioManager.VIBRATE_SETTING_ON);
//        		            mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,AudioManager.VIBRATE_SETTING_ON);
//        		        }else if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE){
//        		            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//        		            mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,AudioManager.VIBRATE_SETTING_OFF);
//        		            mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,AudioManager.VIBRATE_SETTING_OFF);
//        		        }
        				//by b533 long press # change to vibrate mode or silent mode
        				AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        				if (mAudioManager == null)
        					return true;
        				if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
        					boolean vibeInSilent = (1 == Settings.System.getInt(
        		    				   getContentResolver(),
        		                       Settings.System.VIBRATE_IN_SILENT,
        		                       1));
        		               mAudioManager.setRingerMode(
        		                       vibeInSilent ? AudioManager.RINGER_MODE_VIBRATE
        		                                    : AudioManager.RINGER_MODE_SILENT);
        				}else {
        					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        				}		
        			}    			
        			break;
        			
        		case KeyEvent.KEYCODE_1:        			
        		case KeyEvent.KEYCODE_2:
        		case KeyEvent.KEYCODE_3:
        		case KeyEvent.KEYCODE_4:
        		case KeyEvent.KEYCODE_5:
        		case KeyEvent.KEYCODE_6:
        		case KeyEvent.KEYCODE_7:
        		case KeyEvent.KEYCODE_8:
        		case KeyEvent.KEYCODE_9:
        			if(event.isLongPress()){
        				mIsLongPressed = true;
                		Intent call = new Intent("android.intent.action.DialerLongPressed");
		            	call.putExtra("number", event.getNumber());
        				sendBroadcast(call);
        			}
        			break;
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            View currentFocusView = getWindow().getCurrentFocus();
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                	if(LOGD)Log.d(TAG, "home key is coming up");
                	//closeAllApps(true);
                	processHomeKeyPressed();
                    return true;
                   
                case KeyEvent.KEYCODE_0:
                case KeyEvent.KEYCODE_1:
                case KeyEvent.KEYCODE_2:
                case KeyEvent.KEYCODE_3:
                case KeyEvent.KEYCODE_4:
                case KeyEvent.KEYCODE_5:
                case KeyEvent.KEYCODE_6:
                case KeyEvent.KEYCODE_7:
                case KeyEvent.KEYCODE_8:
                case KeyEvent.KEYCODE_9:
                case KeyEvent.KEYCODE_STAR:
                case KeyEvent.KEYCODE_POUND:
                	if(mIsLongPressed){
                		mIsLongPressed = false;
                	}else{
                		Intent call = new Intent(Intent.ACTION_DIAL);
		            	call.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		            	call.putExtra("number", event.getNumber());
		            	startActivity(call);
                	}

            	break;
            	//focus in Workspace, just care about up for first up event
                case KeyEvent.KEYCODE_DPAD_CENTER:               
                case KeyEvent.KEYCODE_DPAD_DOWN:
            	case KeyEvent.KEYCODE_DPAD_UP:
            	case KeyEvent.KEYCODE_DPAD_LEFT:
            	case KeyEvent.KEYCODE_DPAD_RIGHT:            	
            	{
            		if(currentFocusView != null && currentFocusView instanceof Workspace)
                	{
            			//current focus is workspace, but no children
            			//process no application (include no "+" )case, need pass the key to other region,
	            		//other case eat the event
	            		ViewGroup viewp = (ViewGroup)mWorkspace.getChildAt(mWorkspace.getCurrentScreen());
            			if(viewp != null && viewp.getChildCount() == 0)
            			{
            				Log.d(TAG, "no child in all workspace, need dispatch key to other region");
            				if(mBottomLayout!=null)mBottomLayout.requestFocus();
            				//return super.dispatchKeyEvent(event);
            				return true;
            			}
            			
            			View view = mWorkspace.getChildAt(mWorkspace.getCurrentScreen());
            			view.requestFocus(View.FOCUS_DOWN);
            			return true;
                	}
            		break;
                }
            }
            
            if(event.getKeyCode() >= KeyEvent.KEYCODE_A && event.getKeyCode() <= KeyEvent.KEYCODE_Z )
            {            	
                try{
                	Uri SERVO_SEARCH_URI = Uri.parse("content://search");
//                	KeyData kd = new KeyData();
//                	event.getKeyData(kd);
                	String content = String.valueOf(event.getDisplayLabel());
                	content = content.toLowerCase();                	
                	Intent sintent = new Intent(Intent.ACTION_VIEW, SERVO_SEARCH_URI).putExtra("SEARCH", content.toString());//here, content is what you want to search.
                	
                    sintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(sintent);
                }catch(Exception ne){}            	
            }
        }        

        //Log.d(TAG, "dispatchKeyEvent key CurrentFocus View:"+getCurrentFocus());
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (isAllAppsVisible()) {
        	if(mAllAppsGrid instanceof AllApps2DWithCategory){
        		if(((AllApps2DWithCategory)mAllAppsGrid).isEditModel && ((AllApps2DWithCategory)mAllAppsGrid).currentCategory != Category.CATEGORY_SHORTCUT){
        			((AllApps2DWithCategory)mAllAppsGrid).isEditModel = false;
	        		((AllApps2DWithCategory)mAllAppsGrid).refreshAllAppsUI();
	        	}else{
	        		//dismiss the dialog
	        		((AllApps2DWithCategory)mAllAppsGrid).dismissCorpusSelectionDialog();
	        		closeAllApps(true);	
	        	}
        	}else{
        		closeAllApps(true);
        	}
        } else {
            closeFolder();
        }
    }

    public void closeFolder() {
        Folder folder = mWorkspace.getOpenFolder();
        if (folder != null) {
            closeFolder(folder);
        }
    }
    
    public void closeFolder(int screen) {
        Folder folder = mWorkspace.getOpenFolder(screen);
        if (folder != null) {
            closeFolder(folder);
        }
    }
    
    public void closeLiveFolder() {
        Folder folder = mWorkspace.getOpenLiveFolder();
        if (folder != null) {
            closeFolder(folder);
        }
    }

    void closeFolder(Folder folder) {
        folder.getInfo().opened = false;
        ViewGroup parent = (ViewGroup) folder.getParent();
        if (parent != null) {
            parent.removeView(folder);
            if (folder instanceof DropTarget) {
                // Live folders aren't DropTargets.
                mDragController.removeDropTarget((DropTarget)folder);
            }
        }
        folder.onClose();
    }

    /**
     * Re-listen when widgets are reset.
     */
    private void onAppWidgetReset() {
    	if(mAppWidgetHost != null)
        mAppWidgetHost.startListening();
    }

    /**
     * Go through the and disconnect any of the callbacks in the drawables and the views or we
     * leak the previous Home screen on orientation change.
     */
    private void unbindDesktopItems() {
    	if(Launcher.LOGD)Log.d(TAG, "unbindDesktopItems");
        for (ItemInfo item: mDesktopItems) {
        	if(item != null)
        	{
                item.unbind();
        	}
        }
    }
    
    public boolean ignoreClick = false;
    /**
     * Launches the intent referred by the clicked shortcut.
     *
     * @param v The view representing the clicked shortcut.
     */
    public void onClick(View v) {
    	if(Launcher.LOGD)Log.d(TAG, "onClick v:"+v);
        Object tag = v.getTag();
        if (tag instanceof ShortcutInfo) {
        	if(ignoreClick == true)
        	{
        		ignoreClick = false;
//        		if(Launcher.LOGD)Log.i(TAG,"onClick ignore the click v:"+v);
        		return;
        	}
        	
            // Open shortcut
            final Intent intent = ((ShortcutInfo) tag).intent;
            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            intent.setSourceBounds(new Rect(pos[0], pos[1],pos[0] + v.getWidth(), pos[1] + v.getHeight()));
            isAvoidMoveBeforeClick = true;
            
            startActivitySafely(intent, tag);

            new Handler().postDelayed(new Runnable(){
            	public void run() {
            		isAvoidMoveBeforeClick = false;
            	}
            }, 300);
        } else if (tag instanceof FolderInfo) {
        	if(ignoreClick == true)
        	{
        		ignoreClick = false;
//        		if(Launcher.LOGD)Log.i(TAG,"onClick ignore the click v:"+v);
        		return;
        	}
        	
            handleFolderClick((FolderInfo) tag);
        } else if (v == mHandleView) {
        	ignoreClick = false;
            mAllAppsGrid.setStartForPicker(false);//reset all apps view for picker flag

            if (isAllAppsVisible()) {
                closeAllApps(true);
            } else {
                showAllApps(true);
            }
        } else if (v.getId() == R.id.start_call_iv){
        	startCall(v);
        } else if (v.getId() == R.id.page_manager_iv){
        	if(SystemProperties.getInt("home_enable_quicklauncher", 0) == 1 || this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        	{
        	    startPageManager(v);
        	}
        	else
        	{
        	    rightBtnClick(v);
        	}
        }
    }   

    public  static final String STRING_PAGE_INDEX = "pageIndex";
    public  static final String WITH_FADE_EFFECT  = "WITH_FADE_EFFECT";
    public  static final String CURRENT_PAGE_INDEX = "currentPageIndex";
	public  static final String PAGE_COUNT = "PageCount";
    private boolean isStartPageManager = false;
    public void rightBtnClick(View v){
		if(isAllAppsLoading() == true)
		{
			if(LOGD)Log.d(TAG,"rightBtnClick  in loading desktop, return direct");
			return;
		}
		
		if(isNeedCheckCategory){
			Category.initApplicationData(this, Category.allapps);
			isNeedCheckCategory = false;
		}
		
		QuickLauncher qlauncher = new QuickLauncher();
		qlauncher.popupQuickLauncher(this, v);
    }
    
    public  void startPageManager(View v){
    	if(mWorkspaceLoading == true)
    	{
//    		if(LOGD)Log.d(TAG,"startPageManager  in loading desktop, return direct");
    		return;
    	}
    	
    	if("bottom_style_oms25".equals(String.valueOf(mBottomLayout.getTag()))){
    		if(isAllAppsLoading() == true)
    		{
//    			if(LOGD)Log.d(TAG,"rightBtnClick  in loading desktop, return direct");
    			return;
    		}
    		
    		QuickLauncher qlauncher = new QuickLauncher();
    		qlauncher.popupQuickLauncher(this, v);
    		return;
    	}
    	
        if(LOGD)Log.d(TAG,"startPageManager  currentScreen:"+mWorkspace.getCurrentScreen());
        if (isAllAppsVisible()) {
            closeAllApps(true);
        } else {
            if(false == isStartPageManager) {
                isStartPageManager = true;
                Intent intent = null;
                if(isUsingHomeSettings())
                {
                	String value = LauncherORM.instance(getApplicationContext()).getSettingValue(LauncherORM.pagemanager_view_style);
                	int style = 0;
                	if(value == null){
                		LauncherORM.instance(getApplicationContext()).addSetting(LauncherORM.pagemanager_view_style, "0");
                	}else{
                		style = Integer.valueOf(value);
                	}
                    if(HomeSettingPreference.pagemanager_view_style_2d == style){
                        intent = new Intent(this, EditPageActivity.class);
                    }else{   
                        intent = new Intent(this, TextureSwitchActivity.class);
                    }
                }
                else
                {
                    //if('0' != displayConfigs.charAt(displayConfigs.length()-1-DISPLAY_CONFIG_2D_PAGEMANAGER_INDEX)){
                        intent = new Intent(this, EditPageActivity.class);
                    //}else{
                    //    intent = new Intent(this, TextureSwitchActivity.class);
                    //}
                }
                
                Bundle bundle = new Bundle();
                bundle.putInt(CURRENT_PAGE_INDEX,mWorkspace.getCurrentScreen());
				bundle.putInt(PAGE_COUNT,mWorkspace.getScreenCount());
                intent.putExtras(bundle);
                
                mWorkspace.temporaryDisableInvalidate(); 
                startActivity(intent);
            }
        }
    }

    public void startActivitySafely(Intent intent, Object tag) {
    	if(LOGD)Log.d(TAG, "startActivitySafely intent:"+intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag="+ tag + " intent=" + intent, e);
        }
    }
    
    void startActivityForResultSafely(Intent intent, int requestCode) {
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }catch(Exception ne)
        {
        	Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFolderClick(FolderInfo folderInfo) {
        if (!folderInfo.opened) {
            // Close any open folder
            closeFolder();
            // Open the requested folder
            openFolder(folderInfo);
        } else {
            // Find the open folder...
            Folder openFolder = mWorkspace.getFolderForTag(folderInfo);
            int folderScreen;
            if (openFolder != null) {
                folderScreen = mWorkspace.getScreenForView(openFolder);
                // .. and close it
                closeFolder(openFolder);
                if (folderScreen != mWorkspace.getCurrentScreen()) {
                    // Close any folder open on the current screen
                    closeFolder();
                    // Pull the folder onto this screen
                    openFolder(folderInfo);
                }
            }
        }
    }

    /**
     * Opens the user fodler described by the specified tag. The opening of the folder
     * is animated relative to the specified View. If the View is null, no animation
     * is played.
     *
     * @param folderInfo The FolderInfo describing the folder to open.
     */
    private void openFolder(FolderInfo folderInfo) {
//    	if(LOGD)Log.d(TAG, "openFolder folderInfo:"+folderInfo);
        Folder openFolder;

        if (folderInfo instanceof UserFolderInfo) {
            openFolder = UserFolder.fromXml(this);
        } else if (folderInfo instanceof LiveFolderInfo) {
            openFolder = com.android.omshome.LiveFolder.fromXml(this, folderInfo);
        } else {
            return;
        }

        openFolder.setDragController(mDragController);
        openFolder.setLauncher(this);

        openFolder.bind(folderInfo);
        folderInfo.opened = true;

        mWorkspace.addInScreen(openFolder, folderInfo.screen, 0, 0, 4, 4);
        openFolder.onOpen();
    }

    public boolean onLongClick(View v) {
//        if(Launcher.LOGD)Log.d(TAG,"onLongClick v:"+v+" id:"+v.getId());
        if(true == skipLongClick) {
//            if(Launcher.LOGD)Log.d(TAG,"skip onLongClick");
            skipLongClick = false;
            return true;
        }
        
        switch (v.getId()) {
            case R.id.start_call_iv:
                if (!isAllAppsVisible()) {
                    mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                    startCall(v);
                }
                return true;
            case R.id.page_manager_iv:
                if (!isAllAppsVisible()) {
                    mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                  startPageManager(v);
                }
                return true;           
        }

        if (isWorkspaceLocked()) {
            return false;
        }

        if (!(v instanceof CellLayout)) {
            v = (View) v.getParent();
        }

        CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) v.getTag();

        // This happens when long clicking an item with the dpad/trackball
        if (cellInfo == null) {
            return true;
        }

        if (mWorkspace.allowLongPress()) {
            if (cellInfo.cell == null) {
                if (cellInfo.valid) {
                    // User long pressed on empty space
                    mWorkspace.setAllowLongPress(false);
                    mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                    showAddDialog(cellInfo);
                }
            } else {
                if (!(cellInfo.cell instanceof Folder)) {
                    // User long pressed on an item
                    mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                    
//                    if(cellInfo.cell instanceof BubbleTextView){
//                    	((BubbleTextView)cellInfo.cell).resetIcon();
//                    }
                    
                    mWorkspace.startDrag(cellInfo);
                }
            }
        }
        return true;
    }

    @SuppressWarnings({"unchecked"})
    private void dismissPreview(final View v) {
        final PopupWindow window = (PopupWindow) v.getTag();
        if (window != null) {
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                public void onDismiss() {
                    ViewGroup group = (ViewGroup) v.getTag(R.id.workspace);
                    int count = group.getChildCount();
                    for (int i = 0; i < count; i++) {
                        ((ImageView) group.getChildAt(i)).setImageDrawable(null);
                    }
                    ArrayList<Bitmap> bitmaps = (ArrayList<Bitmap>) v.getTag(R.id.icon);
                    for (Bitmap bitmap : bitmaps) bitmap.recycle();

                    v.setTag(R.id.workspace, null);
                    v.setTag(R.id.icon, null);
                    window.setOnDismissListener(null);
                }
            });
            window.dismiss();
        }
        v.setTag(null);
    }

    private void showPreviews(View anchor) {
        showPreviews(anchor, 0, mWorkspace.getChildCount());
    }

    private void showPreviews(final View anchor, int start, int end) {
        final Resources resources = getResources();
        final Workspace workspace = mWorkspace;

        CellLayout cell = ((CellLayout) workspace.getChildAt(start));
        
        float max = workspace.getChildCount();
        
        final Rect r = new Rect();
        resources.getDrawable(R.drawable.preview_background).getPadding(r);
        int extraW = (int) ((r.left + r.right) * max);
        int extraH = r.top + r.bottom;

        int aW = cell.getWidth() - extraW;
        float w = aW / max;

        int width = cell.getWidth();
        int height = cell.getHeight();
        int x = cell.getLeftPadding();
        int y = cell.getTopPadding();
        width -= (x + cell.getRightPadding());
        height -= (y + cell.getBottomPadding());

        float scale = w / width;

        int count = end - start;

        final float sWidth = width * scale;
        float sHeight = height * scale;

        LinearLayout preview = new LinearLayout(this);

        PreviewTouchHandler handler = new PreviewTouchHandler(anchor);
        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>(count);

        for (int i = start; i < end; i++) {
            ImageView image = new ImageView(this);
            cell = (CellLayout) workspace.getChildAt(i);

            final Bitmap bitmap = Bitmap.createBitmap((int) sWidth, (int) sHeight,
                    Bitmap.Config.ARGB_8888);

            final Canvas c = new Canvas(bitmap);
            c.scale(scale, scale);
            c.translate(-cell.getLeftPadding(), -cell.getTopPadding());
            cell.dispatchDraw(c);

            image.setBackgroundDrawable(resources.getDrawable(R.drawable.preview_background));
            image.setImageBitmap(bitmap);
            image.setTag(i);
            image.setOnClickListener(handler);
            image.setOnFocusChangeListener(handler);
            image.setFocusable(true);
            if (i == mWorkspace.getCurrentScreen()) image.requestFocus();

            preview.addView(image,
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            bitmaps.add(bitmap);            
        }

        final PopupWindow p = new PopupWindow(this);
        p.setContentView(preview);
        p.setWidth((int) (sWidth * count + extraW));
        p.setHeight((int) (sHeight + extraH));
        p.setAnimationStyle(R.style.AnimationPreview);
        p.setOutsideTouchable(true);
        p.setFocusable(true);
        p.setBackgroundDrawable(new ColorDrawable(0));
        p.showAsDropDown(anchor, 0, 0);

        p.setOnDismissListener(new PopupWindow.OnDismissListener() {
            public void onDismiss() {
                dismissPreview(anchor);
            }
        });

        anchor.setTag(p);
        anchor.setTag(R.id.workspace, preview);
        anchor.setTag(R.id.icon, bitmaps);        
    }

    class PreviewTouchHandler implements View.OnClickListener, Runnable, View.OnFocusChangeListener {
        private final View mAnchor;

        public PreviewTouchHandler(View anchor) {
            mAnchor = anchor;
        }

        public void onClick(View v) {
            mWorkspace.snapToScreen((Integer) v.getTag());
            v.post(this);
        }

        public void run() {
            dismissPreview(mAnchor);            
        }

        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                mWorkspace.snapToScreen((Integer) v.getTag());
            }
        }
    }

    Workspace getWorkspace() {
        return mWorkspace;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CREATE_SHORTCUT:
                return new CreateShortcut().createDialog();
            case DIALOG_RENAME_FOLDER:
                return new RenameFolder().createDialog();
        }

//        Log.d(TAG, "onCreateDialog id="+id);
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_CREATE_SHORTCUT:
                break;
            case DIALOG_RENAME_FOLDER:
                if (mFolderInfo != null) {
                    EditText input = (EditText) dialog.findViewById(R.id.folder_name);
                    final CharSequence text = mFolderInfo.title;
                    input.setText(text);
                    input.setSelection(0, text.length());
                }
                break;
        }
    }

    void showRenameDialog(FolderInfo info) {
        mFolderInfo = info;
        mWaitingForResult = true;
        showDialog(DIALOG_RENAME_FOLDER);
    }

    private void showAddDialog(CellLayout.CellInfo cellInfo) {
//    	Log.d(TAG, "showAddDialog");
        mAddItemCellInfo = cellInfo;
        mWaitingForResult = true;
        showDialog(DIALOG_CREATE_SHORTCUT);
    }
    
    private void pickAppShortcut(int requestCode, int title) {
//        if(LOGD)Log.d(TAG,"pickAppShortcut title:"+title);
    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
        
        
        startActivityForResult(pickIntent, requestCode);
    }

    private void pickShortcut() {
        Bundle bundle = new Bundle();

        ArrayList<String> shortcutNames = new ArrayList<String>();
        shortcutNames.add(getString(R.string.group_applications));
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

        ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
        shortcutIcons.add(ShortcutIconResource.fromContext(Launcher.this,
                        R.drawable.ic_launcher_application));
        bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.title_select_shortcut));
        pickIntent.putExtras(bundle);

        startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
    }

    private class RenameFolder {
        private EditText mInput;

        Dialog createDialog() {
            mWaitingForResult = true;
            final View layout = View.inflate(Launcher.this, R.layout.rename_folder, null);
            mInput = (EditText) layout.findViewById(R.id.folder_name);

            AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
            builder.setIcon(0);
            builder.setTitle(getString(R.string.rename_folder_title));
            builder.setCancelable(true);
            builder.setOnCancelListener(new Dialog.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    cleanup();
                }
            });
            builder.setNegativeButton(getString(R.string.cancel_action),
                new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        cleanup();
                    }
                }
            );
            builder.setPositiveButton(getString(R.string.rename_action),
                new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        changeFolderName();
                    }
                }
            );
            builder.setView(layout);

            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                public void onShow(DialogInterface dialog) {
                    mWaitingForResult = true;
                    mInput.requestFocus();
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(mInput, 0);
                }
            });

            return dialog;
        }

        private void changeFolderName() {
        	try{
	            final String name = mInput.getText().toString();
	            if (!TextUtils.isEmpty(name)) {
	                // Make sure we have the right folder info
	                mFolderInfo = mFolders.get(mFolderInfo.id);
	                mFolderInfo.title = name;
	                LauncherModel.updateItemInDatabase(Launcher.this, mFolderInfo);
	
	                if (mWorkspaceLoading) {
	                    lockAllApps();
	                    isNeedCheckCategory = true;
	                    isNeedAllAppsSort = true;
	                    isNeedDeleteAllWebWidgetView = true;
	                    mModel.startLoader(Launcher.this, false);
	                } else {
	                    final FolderIcon folderIcon = (FolderIcon)
	                            mWorkspace.getViewForTag(mFolderInfo);
	                    if (folderIcon != null) {
	                        folderIcon.setText(name);
	                        getWorkspace().requestLayout();
	                    } else {
	                        lockAllApps();
	                        mWorkspaceLoading = true;
	                        isNeedCheckCategory = true;
	                        isNeedAllAppsSort = true;
	                        isNeedDeleteAllWebWidgetView = true;
	                        mModel.startLoader(Launcher.this, false);
	                    }
	                }
	            }
	            cleanup();
        	}catch(Exception ne){}
        }

        private void cleanup() {
            dismissDialog(DIALOG_RENAME_FOLDER);
            mWaitingForResult = false;
            mFolderInfo = null;
        }
    }

    // Now a part of LauncherModel.Callbacks. Used to reorder loading steps.
    public boolean isAllAppsVisible() {
        return (mAllAppsGrid != null) ? mAllAppsGrid.isVisible() : false;
    }

    boolean isAllAppsOpaque() {
        return mAllAppsGrid.isOpaque();
    }
    
    void showAllApps(boolean animated,boolean forPicker)
    {
        if(forPicker)
        {
            mAllAppsGrid.setStartForPicker(forPicker);
        }
        showAllApps(animated);
    }

    // AllAppsView.Watcher
    public void zoomed(float zoom) {
        if (zoom == 1.0f) {
            mWorkspace.setVisibility(View.GONE);
            if(mBottomLayout != null){
            	mBottomLayout.setVisibility(View.GONE);
            }
        }
    }

    boolean isNeedAllAppsSort = false;
    public void showAllApps(boolean animated) {        
        mAllAppsGrid.zoom(1.0f, animated);
        //mWorkspace.hide();
        
        mAllAppsGrid.setFocusable(true);
        if(mAllAppsGrid instanceof AllApps2D){
        	((AllApps2D)mAllAppsGrid).requestFocus();
        	((GridView)(((AllApps2D)mAllAppsGrid).getChildAt(0))).setSelection(0);
        }else if(mAllAppsGrid instanceof AllAppsScreenLayout){
//        	mWorkspace.setFocusable(false);
//        	mWorkspace.setFocusableInTouchMode(false);
        	mWorkspace.setVisibility(View.INVISIBLE);
        	((AllAppsScreenLayout)mAllAppsGrid).requestFocus();
        	
        	if(false){
        		((AllAppsScreenLayout)mAllAppsGrid).showHint();
        	}
        }
         
        // TODO: fade these two too    
        mDeleteZone.setVisibility(View.GONE);
    }

    /**
     * Things to test when changing this code.
     *   - Home from workspace
     *          - from center screen
     *          - from other screens
     *   - Home from all apps
     *          - from center screen
     *          - from other screens
     *   - Back from all apps
     *          - from center screen
     *          - from other screens
     *   - Launch app from workspace and quit
     *          - with back
     *          - with home
     *   - Launch app from all apps and quit
     *          - with back
     *          - with home
     *   - Go to a screen that's not the default, then all
     *     apps, and launch and app, and go back
     *          - with back
     *          -with home
     *   - On workspace, long press power and go back
     *          - with back
     *          - with home
     *   - On all apps, long press power and go back
     *          - with back
     *          - with home
     *   - On workspace, power off
     *   - On all apps, power off
     *   - Launch an app and turn off the screen while in that app
     *          - Go back with home key
     *          - Go back with back key  TODO: make this not go to workspace
     *          - From all apps
     *          - From workspace
     *   - Enter and exit car mode (becuase it causes an extra configuration changed)
     *          - From all apps
     *          - From the center workspace
     *          - From another workspace
     */
    void closeAllApps(boolean animated) {
//    	if(Launcher.LOGD)Log.d(TAG, "closeAllApps");
    	
        if (mAllAppsGrid.isVisible()) {
        	mWorkspace.setFocusable(true);
        	mWorkspace.setFocusableInTouchMode(true);
            mWorkspace.setVisibility(View.VISIBLE);
            
            if(mBottomLayout != null){
            	mBottomLayout.setVisibility(View.VISIBLE);
            }
            
            mAllAppsGrid.zoom(0.0f, animated);
            ((View)mAllAppsGrid).setFocusable(false);
          //  mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
        }
    }

    void lockAllApps() {
        // TODO
    }

    void unlockAllApps() {
        // TODO
    }

    /**
     * Displays the shortcut creation dialog and launches, if necessary, the
     * appropriate activity.
     */
    private class CreateShortcut implements DialogInterface.OnClickListener,
            DialogInterface.OnCancelListener, DialogInterface.OnDismissListener,
            DialogInterface.OnShowListener {

        private AddAdapter mAdapter;

        Dialog createDialog() {
            mWaitingForResult = true;

            mAdapter = new AddAdapter(Launcher.this);

            final AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
            builder.setTitle(getString(R.string.menu_item_add_item));
            builder.setAdapter(mAdapter, this);

            builder.setInverseBackgroundForced(true);

            AlertDialog dialog = builder.create();
            dialog.setOnCancelListener(this);
            dialog.setOnDismissListener(this);
            dialog.setOnShowListener(this);

            return dialog;
        }

        public void onCancel(DialogInterface dialog) {
            mWaitingForResult = false;
            cleanup();
        }

        public void onDismiss(DialogInterface dialog) {
        }

        private void cleanup() {
            try {
                dismissDialog(DIALOG_CREATE_SHORTCUT);
            } catch (Exception e) {
                // An exception is thrown if the dialog is not visible, which is fine
            }
        }

        /**
         * Handle the action clicked in the "Add to home" dialog.
         */
        public void onClick(DialogInterface dialog, int which) {
            Resources res = getResources();
            cleanup();

//            if(Launcher.LOGD)Log.d(TAG, "CreateShortcut onClick which:"+which);
            switch (which) {
                case AddAdapter.ITEM_SHORTCUT: {
                    // Insert extra item to handle picking application
                    pickShortcut();
                    break;
                }

                case AddAdapter.ITEM_APPWIDGET: {
                    int appWidgetId = Launcher.this.mAppWidgetHost.allocateAppWidgetId();

                    Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
                    pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    
                    ArrayList<AppWidgetProviderInfo> customInfo = new ArrayList<AppWidgetProviderInfo>();
                    ArrayList<Bundle> customExtras = new ArrayList<Bundle>();
                    //add installed webwidgets as customer "App Widget", cn003@March.2010
                    loadInstalledWebwidgets(customInfo, customExtras);
					
                    pickIntent.putParcelableArrayListExtra(
                        AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
                    
                    pickIntent.putParcelableArrayListExtra(
                        AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
                    
                    // start the pick activity
                    startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
                    break;
                }

                case AddAdapter.ITEM_LIVE_FOLDER: {
                    // Insert extra item to handle inserting folder
                    Bundle bundle = new Bundle();

                    ArrayList<String> shortcutNames = new ArrayList<String>();
                    shortcutNames.add(res.getString(R.string.group_folder));
                    bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

                    ArrayList<ShortcutIconResource> shortcutIcons =
                            new ArrayList<ShortcutIconResource>();
                    shortcutIcons.add(ShortcutIconResource.fromContext(Launcher.this,
                            R.drawable.ic_launcher_folder));
                    bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

                    Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                    pickIntent.putExtra(Intent.EXTRA_INTENT,
                            new Intent(LiveFolders.ACTION_CREATE_LIVE_FOLDER));
                    pickIntent.putExtra(Intent.EXTRA_TITLE,
                            getText(R.string.title_select_live_folder));
                    pickIntent.putExtras(bundle);

                    startActivityForResultWithInvalidate(pickIntent, REQUEST_PICK_LIVE_FOLDER);
                    break;
                }

                case AddAdapter.ITEM_WALLPAPER: {
                    startWallpaper();
                    break;
                }
            }
        }

        public void onShow(DialogInterface dialog) {
            mWaitingForResult = true;             
        }
    }

    /**
     * Load Installed Webwidgets into customer info listonLongClick
     *
     * @customInfo, Array list where store the web widgets info
     * @customExtras, Array list where store the corresponding widget extra info  
     */
    private void loadInstalledWebwidgets(ArrayList<AppWidgetProviderInfo> customInfo,
                 ArrayList<Bundle> customExtras)
    {		
    }
    
    private class HomeKeyIntentReceiver extends BroadcastReceiver
    {
		@Override
		public void onReceive(Context context, Intent intent) 
		{
		    Log.d(TAG, "end or home key is coming="+intent);
		    if(isNewDraging)
		    {
		         forceEndNewDrag();   
		    }
		    if(intent != null)
		    {
		        String action = intent.getAction();
		        if(action != null)
				{
				    if(action.equals(ENDCALLKEY_PRESSED_IN_HOME))
					{
					    closeAllApps(true);
					}
					else if(action.equals(HOMEKEY_PRESSED_IN_HOME))
					{
						Configuration configuration = getResources().getConfiguration();
						if(configuration.orientation != Configuration.ORIENTATION_PORTRAIT && Configuration.HARDKEYBOARDHIDDEN_YES == configuration.hardKeyboardHidden)							
						{
							 Log.d(TAG, "home key in ORIENTATION_LAND");
						}
						else
						{
							if(mPaused == false && Launcher.this.isAllAppsVisible() == false)//I am already have focus and all app is not visible
							{
								if(mWorkspace!=null && mWorkspace.getScroller()!=null){
									if(mWorkspace.getScroller().isFinished()){
										int defaultPage = Workspace.DEFAULT_CURRENT_SCREEN;
										final ContentResolver cr =  mWorkspace.getContext().getContentResolver();
										try{
											defaultPage = getSettingsIntValue(Launcher.this, LauncherORM.default_page_index);
										}catch(Exception ne){}
										
										if(defaultPage >= mWorkspace.getChildCount())
										{
											defaultPage = mWorkspace.getChildCount()/2;
										}							
										setSettingsIntValue(Launcher.this, LauncherORM.default_page_index, defaultPage);
										showPage(defaultPage, 0);
								    }
								}
							}
						}
							closeAllApps(true);
					}
				}
			}
		}    	
    }
    
    /**
     * Receives notifications when applications are added/removed.
     */
    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            closeSystemDialogs();
            String reason = intent.getStringExtra("reason");
            if (!"homekey".equals(reason)) {
                boolean animate = true;
                if (mPaused || "lock".equals(reason)) {
                    animate = false;
                }
                closeAllApps(animate);                
            }
        }
    }

    /**
     * Receives notifications whenever the appwidgets are reset.
     */
    private class AppWidgetResetObserver extends ContentObserver {
        public AppWidgetResetObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            onAppWidgetReset();
        }
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public int getCurrentWorkspaceScreen() {
        if (mWorkspace != null) {
            return mWorkspace.getCurrentScreen();
        } else {
//        	Log.w(TAG, "getCurrentWorkspaceScreen  mWorkspace null, return 0");
            return 0;
        }
    }

    /**
     * Refreshes the shortcuts shown on the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    long webwidgetpre;
    boolean ENABLE_TIME_CHECK = true;
    
    public void startWebWidgetEngine()
    {
    }
    
    android.widget.Button finishButton;
    public void startBinding() {
        if(Launcher.LOGD)Log.d(TAG,"startBinding");
        final Workspace workspace = mWorkspace;
        int count = workspace.getChildCount();
        for (int i = 0; i < count; i++) {
            // Use removeAllViewsInLayout() to avoid an extra requestLayout() and invalidate().
            ((ViewGroup) workspace.getChildAt(i)).removeAllViewsInLayout();
        }
        
        if (DEBUG_USER_INTERFACE || SystemProperties.getInt("home_enable_debug", 0) == 1) {
            finishButton = new android.widget.Button(this);
            finishButton.setText("Start");
            workspace.addInScreen(finishButton, 1, 0, 0, 1, 1);

            finishButton.setOnClickListener(new android.widget.Button.OnClickListener() {
                public void onClick(View v) {
                    mWorkspace.CHECK_MAX_DRAW_CAPABILITY = !mWorkspace.CHECK_MAX_DRAW_CAPABILITY;
                    if(mWorkspace.CHECK_MAX_DRAW_CAPABILITY == false)
                    {
                    	finishButton.setText("Start");
                    	mWorkspace.count = 1;
                    	mWorkspace.alltime = 0;                    	    
                    }
                    mWorkspace.postInvalidate();
                }
            });
        }
    }

    //begin for a new bind items
    int bindCount=0;
    final class AddRunnable implements Runnable
    {
    	public View     shortcut;    	
    	public ItemInfo item;
    	public int X;    	
    	public int Y;
    	public int spanX;    	
    	public int spanY;
    	public boolean insert;
    	
    	public void run()
    	{
    		mWorkspace.addInScreen(shortcut, item.screen, item.cellX, item.cellY, spanX, spanY, insert);
    		bindCount++;
    	}
    }
    
    
    BindDeferredHandler bindHandler = new BindDeferredHandler();
    private final class BindDeferredHandler {
        private LinkedList<Runnable> mQueue    = new LinkedList<Runnable>();
        private LinkedList<Runnable> mQueueRun = new LinkedList<Runnable>();        
        private final int QueueSize = 12;
        
        private MessageQueue mMessageQueue = Looper.myQueue();
        private Impl mHandler = new Impl();        

        private class Impl extends Handler implements MessageQueue.IdleHandler {
            public void handleMessage(Message msg) {
                synchronized (mQueue) {
                    if (mQueue.size() == 0) {
                        return;
                    }
                    while(mQueue.size() > 0 && mQueueRun.size() < QueueSize)
                    {
                    	mQueueRun.add(mQueue.removeFirst());
                    }
                }
                
                //do QueueSize at one time
                while(mQueueRun.size() > 0)
                {
                	Runnable r = mQueueRun.removeFirst();
                	r.run();                	
                	r = null;                	
                }
                
                //for check the orientation
                if(orientatonChangedInBackground())
             	{
//             		Log.d(TAG, "stop requestLayout, but let the items added in workspace");
             		return ;
             	}
               
             	
                mWorkspace.requestLayout();
                
                synchronized (mQueue) {
                    scheduleNextLocked();
                }
            }

            public boolean queueIdle() {
                handleMessage(null);
                return false;
            }
        }    

        public BindDeferredHandler() {
        }

        /** Schedule runnable to run after everything that's on the queue right now. */
        public void post(Runnable runnable) {
            synchronized (mQueue) {
                mQueue.add(runnable);
                if (mQueue.size() == 1) {
                    scheduleNextLocked();
                }
            }
        }
        void scheduleNextLocked() {
            if (mQueue.size() > 0) {
                Runnable peek = mQueue.getFirst();
                mHandler.sendEmptyMessage(1);                
            }
        }
    }
        
    public boolean isFinishedBindItems(int ItemtDesbindCount)
    {
    	if(Launcher.LOGD)Log.d(TAG, " curent binded="+bindCount + " all count="+ItemtDesbindCount);
    	return bindCount == ItemtDesbindCount;
    }
    //end for a new bind items
    
    boolean orientatonChangedInBackground()
    {
    	try{
	    	int tmpOrient = getResources().getConfiguration().orientation;
	     	if(preOrientation != tmpOrient)
	     	{
	     		Log.d(TAG, "home get different orientation, new Orient="+tmpOrient + " is in visible="+amVisible);
	     		sdcardReloadNotDone = true;
	     		return true;
	     	}
    	}catch(Exception ne){}
     	
     	return false;
    }    
  
    /**
     * Bind the items start-end from the list.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {
    	
     	if(orientatonChangedInBackground())
     	{
     		Log.d(TAG, "stop bindItems");
     		return ;
     	}
     	
        if(Launcher.LOGD)Log.d(TAG,"bindItems thread="+Thread.currentThread().getName());
        final Workspace workspace = mWorkspace;

        try{
	        for (int i=start; i<end; i++) {
	            final ItemInfo item = shortcuts.get(i);
	            mDesktopItems.add(item);
	            switch (item.itemType) {
	                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
	                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
	                    final View shortcut = createShortcut((ShortcutInfo)item);   
//	                    if(Launcher.isLowLevelHardware())
//	                    {
//		                    AddRunnable ar = new AddRunnable();
//		                    ar.shortcut = shortcut;
//		                    ar.item = item;
//		                    ar.spanX = 1;
//		                    ar.spanY = 1;
//		                    ar.insert = false;                    
//		                    bindHandler.post(ar);
//	                    }
//	                    else
	                    {
	                    	workspace.addInScreen(shortcut, item.screen, item.cellX, item.cellY, 1, 1,
	                            false);
	                    }
	                    break;
	                case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
	                    final FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
	                            (ViewGroup) workspace.getChildAt(workspace.getCurrentScreen()),
	                            (UserFolderInfo) item);
//	                    if(Launcher.isLowLevelHardware())
//	                    {
//		                    AddRunnable ar = new AddRunnable();
//		                    ar.shortcut = newFolder;
//		                    ar.item = item;
//		                    ar.spanX = 1;
//		                    ar.spanY = 1;
//		                    ar.insert = false;                    
//		                    bindHandler.post(ar);
//	                    }
//	                    else
	                    {
	                        workspace.addInScreen(newFolder, item.screen, item.cellX, item.cellY, 1, 1,
	                            false);
	                    }
	                    break;
	                case LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
	                    final FolderIcon newLiveFolder = LiveFolderIcon.fromXml(
	                            R.layout.live_folder_icon, this,
	                            (ViewGroup) workspace.getChildAt(workspace.getCurrentScreen()),
	                            (LiveFolderInfo) item);
//	                    if(Launcher.isLowLevelHardware())
//	                    {
//		                    AddRunnable ar = new AddRunnable();
//		                    ar.shortcut = newLiveFolder;
//		                    ar.item = item;
//		                    ar.spanX = 1;
//		                    ar.spanY = 1;
//		                    ar.insert = false;                    
//		                    bindHandler.post(ar);
//	                    }
//	                    else
	                    {
	                        workspace.addInScreen(newLiveFolder, item.screen, item.cellX, item.cellY, 1, 1,
	                            false);
	                    }
	                    break;
	              case LauncherSettings.Favorites.ITEM_TYPE_WIDGET_SEARCH:
	//                    final int screen = workspace.getCurrentScreen();
	//                    final View view = mInflater.inflate(R.layout.searchbar_main,
	//                            (ViewGroup) workspace.getChildAt(screen), false);
	//
	//                    //Search search = (Search) view.findViewById(R.id.widget_search);
	//                    //search.setLauncher(this);
	//
	//                    final Widget widget = (Widget) item;
	//                    view.setTag(widget);
	//
	//                    workspace.addWidget(view, widget, false);
	                    break;
	            }
	        }
        }catch(Exception ne){}

        if(Launcher.isLowLevelHardware())
        {
//        	bindHandler.post(new Runnable()
//	        {
//	        	public void run()
//	        	{
//	                workspace.requestLayout();
//	        	}
//	        });
        }
        else
        {
            workspace.requestLayout();
        }
        

        //re-check the bind
     	orientatonChangedInBackground();     	
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindFolders(HashMap<Long, FolderInfo> folders) {
        mFolders.clear();
        mFolders.putAll(folders);
    }

    /**
     * Add the views for a widget to the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
   /* public void bindAppWidget(LauncherAppWidgetInfo item) {
        final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: " + item);
        }
        final Workspace workspace = mWorkspace;

        final int appWidgetId = item.appWidgetId;
        final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component " + appWidgetInfo.provider);
        }

        item.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);

        item.hostView.setAppWidget(appWidgetId, appWidgetInfo);
        item.hostView.setTag(item);

        workspace.addInScreen(item.hostView, item.screen, item.cellX,
                item.cellY, item.spanX, item.spanY, false);

        workspace.requestLayout();

        mDesktopItems.add(item);

        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bound widget id="+item.appWidgetId+" in "
                    + (SystemClock.uptimeMillis()-start) + "ms");
        }
    }*/

    /**
     * Prepare the given view to be shown. This might include adjusting
     * {@link FrameLayout.LayoutParams} before inserting.
     */
    protected void prepareView(View view) {
        // Take requested dimensions from child, but apply default gravity.
        FrameLayout.LayoutParams requested = (FrameLayout.LayoutParams)view.getLayoutParams();
        if (requested == null) {
            requested = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT);
        }

        requested.gravity = Gravity.CENTER;
        view.setLayoutParams(requested);
    }

    protected AppWidgetHostView getDefaultView() {
        AppWidgetHostView hostview = new AppWidgetHostView(Launcher.this);
        View view = mInflater.inflate(R.layout.appwidget_error, hostview, false);
        prepareView(view);
        hostview.addView(view);
        hostview.requestLayout();
        return hostview;
    }


       /**
     * Add the views for a widget to the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppWidget(LauncherAppWidgetInfo item) {
        final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: " + item);
        }

        if(orientatonChangedInBackground())
     	{
        	Log.d(TAG, "bindAppWidget diff orientation");
     		return ;
     	}

        final Workspace workspace = mWorkspace;
    	try{
	
	        final int appWidgetId = item.appWidgetId;
	        
	        final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            if (DEBUG_WIDGETS) {
                Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component " + appWidgetInfo.provider);
            }
	        if(appWidgetInfo != null)
	        {
	        	if(LOGD)Log.d(TAG,"bindAppWidget appWidgetId="+appWidgetId + " info="+appWidgetInfo);
	        }
	        else
	        {
	        	if(LOGD)Log.d(TAG,"bindAppWidget appWidgetId="+appWidgetId + " info=null");
	        }
	        
	        if(appWidgetInfo != null && mAppWidgetHost != null)
	        {
	        	try{
		            item.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
		        } 
		        catch(Exception ne) 
		        {
                    if(false == (ne instanceof NullPointerException))
	                {
	                    Log.e(TAG, "what is the exception=" + ne.getMessage());
	                    throw ne;
	                }
	                Log.e(TAG, "exist null exception=" + ne.getMessage() + " bindAppWidget, Fail to load app widget="+appWidgetInfo);
	                item.hostView = getDefaultView();
		        }
		        
		        item.hostView.setAppWidget(appWidgetId, appWidgetInfo); 
		        item.hostView.setTag(item);
		
		        if(LOGD)Log.d(TAG,"bindAppWidget screen:"+item.screen+" hostView:"+item.hostView+" cellX:"+item.cellX+" cellY:"+item.cellY+" item:"+item);
		        workspace.addInScreen(item.hostView, item.screen, item.cellX,
		                item.cellY, item.spanX, item.spanY, false);
		
		        workspace.requestLayout();
		
		        if(item.packageName.equals("oms.dcd") /*&& (item.hostView.getChildAt(0) instanceof ViewGroup)*/){
		        	widgetHostViews.add(item.hostView);
		        }
		        mDesktopItems.add(item);
		        if (DEBUG_WIDGETS) {
		            Log.d(TAG, "bound widget id="+item.appWidgetId+" in "
		                    + (SystemClock.uptimeMillis()-start) + "ms");
			    }
		    }
	        else
	        {
	        	if(LOGD)Log.d(TAG,"bindAppWidget appWidgetId="+appWidgetId+" fail, rebind after 5 seconds="+item.packageName );
	        	
	        	/*
	        	ComponentName provider = new ComponentName(item.packageName, item.className);
	        	int []ids = mAppWidgetManager.getAppWidgetIds(provider);
	        	if(ids != null && ids.length > 0)
	        	{
		        	for(int i=0;i<ids.length;i++)
		        	{
		        		Log.d(TAG, "already have id="+ids[i]);
		        	}	        	
	        	}
	        	else//no id exist, so rebind the app widget
	        	*/	        	
//	        	{        		
//	        	
//	        		if(LOGD)Log.d(TAG,"no appwidget match exist, so rebind the app widget appWidgetId="+appWidgetId + "  for package=" +item.packageName );
//	        		int newID = LauncherORM.instance(this).RebindAppWidgets(appWidgetId);
//	        		item.appWidgetId = newID;        		
//	        	}
	        	
        		if(LOGD)Log.d(TAG,"no appwidget match exist, so rebind the app widget appWidgetId="+appWidgetId + "  for package=" +item.packageName );
        		int newID = LauncherORM.instance(this).RebindAppWidgets(appWidgetId);
        		if(newID != -1)
        		{
	        		item.appWidgetId = newID;
        		}
        		new Handler().postDelayed(new DelayRunnable(item, workspace), 10000);
	        }
    	}catch(Exception ne)
    	{
    		Log.i(TAG,"bind appwidget delay 10000="+ne.getMessage());
    		new Handler().postDelayed(new DelayRunnable(item, workspace), 10000);
    	}
    }

    private class DelayRunnable implements Runnable
    {
    	final LauncherAppWidgetInfo pitem;
    	final Workspace workspace;
    	
    	public DelayRunnable(LauncherAppWidgetInfo item, Workspace ws)
    	{
    		pitem  = new LauncherAppWidgetInfo(item.appWidgetId);
    		pitem.cellX = item.cellX;
    		pitem.cellY = item.cellY;
    		pitem.className = item.className;
    		pitem.container = item.container;
    		pitem.id = item.id;
    		pitem.isGesture = item.isGesture;
    		pitem.itemType  = item.itemType;
    		pitem.packageName = item.packageName;
    		pitem.screen      = item.screen;
    		pitem.spanX       = item.spanX;
    		pitem.spanY       = item.spanY;
    		
    		workspace = ws;
    	}
    	
		public void run() 
		{
		    final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(pitem.appWidgetId);
	        if(appWidgetInfo != null && mAppWidgetHost != null)
	        {
	        	try{
	        	    pitem.hostView = mAppWidgetHost.createView(Launcher.this, pitem.appWidgetId, appWidgetInfo);
	        	} catch(Exception e) {
	        		pitem.hostView = getDefaultView();
	        	}
		
	        	pitem.hostView.setAppWidget(pitem.appWidgetId, appWidgetInfo); 
	        	pitem.hostView.setTag(pitem);
		
		        if(LOGD)Log.d(TAG,"bindAppWidget screen:"+pitem.screen+" hostView:"+pitem.hostView+" cellX:"+pitem.cellX+" cellY:"+pitem.cellY+" item:"+pitem);
		        workspace.addInScreen(pitem.hostView, pitem.screen, pitem.cellX,
		        		pitem.cellY, pitem.spanX, pitem.spanY, false);
		
		        workspace.requestLayout();		
		
		        if(pitem.packageName.equals("oms.dcd") /*&& (pitem.hostView.getChildAt(0) instanceof ViewGroup)*/){
		        	widgetHostViews.add(pitem.hostView);
		        }
		        //memory leak
		        mDesktopItems.add(pitem);
	        }
	        else
	        {
	        	//delete the app widget fromd database
	        	//can't delete, because, Home can't predict the sdcard application is load OK
	        	//
	        	//need remove the app widget
	        	if(false)
	        	{
	        	    LauncherORM.instance(Launcher.this).RemoveAppWidgets(pitem.appWidgetId);
	        	}
	        	if(LOGD)Log.d(TAG,"bindAppWidget fail again, remove it from home ="+pitem.packageName);
	        }
		}
    	
    }
    
    /**
     * Add the views for a web widget to the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    boolean isNeedStartWebWidgetManager = true;
    boolean isNeedDeleteAllWebWidgetView = false;
	
    boolean isNeedResetViewImage = false;
    private void delaySetViewImage(){
    	
    	//add for check oriention changed
    	if(orientatonChangedInBackground() == true)
    	{
    		
    	}
    	else
    	{
    		mHandleView.setBackgroundResource(R.drawable.bottom_button_left_background);
        	mPageIndicator.setBackgroundResource(R.drawable.bottom_button_right_background);
    	}
    }
    
    /**
     * Callback saying that there aren't any more items to bind.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void finishBindingItems() {
    	if(LOGD)Log.d(TAG,"finishBindingItems");
    	bindCount = 0;
    	
    	delaySetViewImage();
    	
        if (mSavedState != null) {
            if (!mWorkspace.hasFocus()) {
                mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
            }

            final long[] userFolders = mSavedState.getLongArray(RUNTIME_STATE_USER_FOLDERS);
            if (userFolders != null) {
                for (long folderId : userFolders) {
                    final FolderInfo info = mFolders.get(folderId);
                    if (info != null) {
                        openFolder(info);
                    }
                }
                final Folder openFolder = mWorkspace.getOpenFolder();
                if (openFolder != null) {
                    openFolder.requestFocus();
                }
            }

            mSavedState = null; 
        }

        if (mSavedInstanceState != null) {
            try {
            	if(LOGD)Log.d(TAG, "mSavedInstanceState:"+mSavedInstanceState);
                   super.onRestoreInstanceState(mSavedInstanceState);
            } catch(IllegalArgumentException ex){
                Log.e(TAG, ex.toString());
            }
            mSavedInstanceState = null;
        }
      
        installIconAsync();

        mWorkspaceLoading = false;

        isNeedListenSdcardMount = true;
        
        //for calendar icon refresh:
        sendBroadcast(new Intent(ACTION_LOAD_COMPLETE));
        
        //check for set theme and language
        callbackAfterBindAll();

        //long heap = VMRuntime.getRuntime().getExternalBytesAllocated();
        //Log.d(TAG, "vm size pre="+heap/(1024*1024));
        
        //no necesary when have sdcard reload message
        if(sdcardReloadNotDone == false)
        {
	        if(mModel.isHaveSdcardMountMessage == true)
	        {
	        	if(mModel.ExistExternalAppAtDesktop(mModel.externalSdcardChangedApp, false) )
	        	{
	        		Log.i(TAG,"exist one sdcard load, and sdcard app in desktop");
	        		sdcardReloadNotDone = true;
	        	}
	        	else
	        	{
	        		Launcher.this.refreshApp();      		
	        	}
	        }
        }
        
        if(sdcardReloadNotDone == true )
		{
        	Log.i(TAG,"exist one sdcard load, send broadcast to reload process");
        	sdcardReloadNotDone = false;
        	mModel.isHaveSdcardMountMessage = false;
			Intent loadtent = new Intent("Home.DELAY_PACKAGE_RELOAD_COMP");
			loadtent.putExtra("reload_after_finish_bind", true);			
			//loadtent.setClassName("com.android.omshome", "com.android.omshome.Launcher");
			Launcher.this.sendBroadcast(loadtent);
		}
        
        //heap = VMRuntime.getRuntime().getExternalBytesAllocated();
        //Log.d(TAG, "vm size post="+heap/(1024*1024));

        if(isNeedDelayCheckMissCallUI){
        	checkMissCallUI();
        	isNeedDelayCheckMissCallUI = false;
        }
        
        new Handler().postDelayed(new Runnable(){
        	public void run(){
        		System.gc();
        	}
        }, 1000);
    }
    
    private void installIconAsync(){
//    	Log.d(TAG,"installIconAsync start");
    	new Handler().postDelayed(new Runnable(){
           public void run() {
               // when phone finished loading desktop icons for first power on/ master reset, 
               // send intent to WidgetManager to add preloaded widget shortcuts.
               boolean inited = false;     
               try {
                     inited = android.provider.Settings.System.getInt(getContentResolver(), WIDGET_INIT_TAG) != 0;
               } catch (Exception e) {
               }

               if(!inited){
                   Intent intent = new Intent("oms.widgetmanager.installed.action.INSTALL_PRELOAD_SHORTCUT");
                   sendBroadcast(intent);
               }   
           }
    	}, 2000);
    }
    
    /*
    private void checkInstallSTK(boolean isFirstCheck){
    	Log.d(TAG, "checkInstallSTK isFirstCheck:"+isFirstCheck+" enter");
    	ArrayList<ShortcutInfo> stkList = mModel.getSTKList();
    	ArrayList<ShortcutInfo> stkInFolderList = mModel.getSTKInFolderList();
    	Drawable stkIcon = null;
    	CharSequence title = null;
    	ResolveInfo stkInfo = null;
    	LauncherModel.Loader loader = mModel.getLoader();
    	boolean isNeedReCheck = false;
    	boolean isNeedClearSTKinDB = false;
    	
    	if(stkList != null && stkList.size()>0){
    		stkInfo =  getSTKResolveInfo();
    		if(stkInfo != null){
    			stkIcon = stkInfo.loadIcon(getPackageManager());
    			title = stkInfo.loadLabel(getPackageManager());
    			if(loader != null && stkIcon!= null && title != null){
    				if(LOGD)Log.d(TAG, "checkInstallSTK   desktop, size:"+stkList.size());
    				for (int i=0; i<stkList.size(); i++) {
    					final ShortcutInfo item = stkList.get(i);
    					item.mIcon = Utilities.createIconBitmap(stkIcon, this);
    					item.title = title;
    					mDesktopItems.add(item);
    					loader.mItems.add(item);
    					final View shortcut = createShortcut((ShortcutInfo) item);
    					mWorkspace.addInScreen(shortcut, item.screen, item.cellX, item.cellY, 1, 1, false);
    				}
    			}
    		} else { //open phone with no sim card, but the database contains some stk item, need delete these records.
    			if(isFirstCheck){
    				isNeedReCheck = true;
    			}
    			isNeedClearSTKinDB = true;
    		}
    	}
    	
    	mWorkspace.requestLayout(); 

    	if(stkInFolderList != null && stkInFolderList.size()>0){
    		if(LOGD)Log.d(TAG, "checkInstallSTK  add in folder");
    		if(null == stkIcon || null == title) {
    			stkInfo =  getSTKResolveInfo();
    			if(null != stkInfo){
    				stkIcon = stkInfo.loadIcon(getPackageManager());
    				title = stkInfo.loadLabel(getPackageManager());
    			}
    		}
    		
    		if(stkInfo != null && stkIcon!= null && title != null){
    			Log.d(TAG, "checkInstallSTK  add in folder, size:"+stkInFolderList.size());
				for (int i=0; i<stkInFolderList.size(); i++) {
					 final ShortcutInfo item = stkInFolderList.get(i);
					 if(null != item) {
						 UserFolderInfo folderInfo = LauncherModel.findOrMakeUserFolder(mFolders, item.container);
						 item.mIcon = Utilities.createIconBitmap(stkIcon, this);
		    	         item.title = title;
						 folderInfo.add(item);
					 }
				}
			} else {
				if(isFirstCheck){
    				isNeedReCheck = true;
    			}
				isNeedClearSTKinDB = true;
			}
    	}
    	
    	if(isFirstCheck){ // first check
    		if(isNeedReCheck){
    			new Handler().postDelayed(new Runnable(){
    				public void run() {
    					checkInstallSTK(false);
    					isNeedinstallSTK = false;
    				}},15*1000);
    		}else{ // check done
    			if(stkList != null && stkList.size()>0){
    				stkList.clear();
    			}
    			
    			if(stkInFolderList != null && stkInFolderList.size()>0){
    				stkInFolderList.clear();
    			}
    		}
    	}else{ // second check
    		if(isNeedClearSTKinDB){ // check done, can delete stk icon at this moment.
    			if(stkList != null && stkList.size()>0){
    				for (int i=0; i<stkList.size(); i++) {
    					Log.d(TAG, "checkInstallSTK  delete stk on desktop");
    					final ShortcutInfo item = stkList.get(i);
    					LauncherModel.deleteItemFromDatabase(this, item);
    				}
    			}
    			
    			if(stkInFolderList != null && stkInFolderList.size()>0){
    				for (int i=0; i<stkInFolderList.size(); i++) {
    					Log.d(TAG, "checkInstallSTK   delete stk in folder");
    					final ShortcutInfo item = stkInFolderList.get(i);
    					LauncherModel.deleteItemFromDatabase(this, item);
    				}
    			}
    		}
    		
    		if(stkList != null && stkList.size()>0){
				stkList.clear();
			}
			
			if(stkInFolderList != null && stkInFolderList.size()>0){
				stkInFolderList.clear();
			}
    	}
    	
    	if(LOGD)Log.d(TAG, "checkInstallSTK exit");
    }
    
    private ResolveInfo getSTKResolveInfo() {
    	final ResolveInfo resolveInfo;
    	final PackageManager packageManager = getPackageManager();
    	final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    	final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
    	if(apps == null || apps.size() == 0)Log.d(TAG, "resolveInfo is null, fail to install STK!");
    	if(apps != null & apps.size() > 0) {
    		for (int i=0; i < apps.size(); i++) {
    			Log.d(TAG, "installSTK, parse resolveInfo: "+apps.get(i).activityInfo.packageName);
    			if("com.android.borqsstk".equals(apps.get(i).activityInfo.packageName)) {
    				resolveInfo = apps.get(i);
    				return resolveInfo;
    			}
    		}
    	}    
    	return null;
    }*/

    /**
     * Add the icons for all apps.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAllApplications(ArrayList<ApplicationInfo> apps) {
    	if(isNeedCheckCategory){
			 Category.initialRes(this);
         }
    	
    	if(isNeedAllAppsSort)
			
    	mAllAppsGrid.setApps(apps);
        setIsAllAppsLoading(false);
        Log.d(TAG,"bindAllApplications ok-----------------------");
    }

    /**
     * A package was installed.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsAdded(ArrayList<ApplicationInfo> apps) {
    	Log.d(TAG, "bindAppsAdded");
    	if(!isRestoreBackupAPK){
    		removeDialog(DIALOG_CREATE_SHORTCUT);
			mAllAppsGrid.addAppsWithoutSort(apps);
    	}
    }

    /**
     * A package was updated.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsUpdated(ArrayList<ApplicationInfo> apps) {
        Log.d(TAG, "bindAppsUpdated");
        if(!isRestoreBackupAPK){
            removeDialog(DIALOG_CREATE_SHORTCUT);
            mWorkspace.updateShortcuts(apps);
		    mAllAppsGrid.updateApps(apps);
        }
    }

    /**
     * A package was uninstalled.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsRemoved(ArrayList<ApplicationInfo> apps) {
    	Log.d(TAG, "bindAppsRemoved");
		if(!isRestoreBackupAPK){
	        removeDialog(DIALOG_CREATE_SHORTCUT);
	        mWorkspace.removeItems(apps);
			mAllAppsGrid.removeApps(apps);
	    }
    }

    /**
     * Prints out out state for debugging.
     */
    public void dumpState() {
        Log.d(TAG, "BEGIN omshome dump state for launcher " + this);
        Log.d(TAG, "mSavedState=" + mSavedState);
        Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
        Log.d(TAG, "mRestoring=" + mRestoring);
        Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
        Log.d(TAG, "mSavedInstanceState=" + mSavedInstanceState);
        Log.d(TAG, "mDesktopItems.size=" + mDesktopItems.size());
        Log.d(TAG, "mFolders.size=" + mFolders.size());
        mModel.dumpState();
        mAllAppsGrid.dumpState();
        Log.d(TAG, "END launcher2 dump state");
        
        mDragLayer.dumpFocus();
        dumpItemInfo();
        
        dumpsettings();
    }
    
    private void dumpsettings()
    {
    	Log.d(TAG, "\n\nBEGIN omshome settings for oms home");
    	Log.d(TAG, "home_wallpaper_move defalt 0="+SystemProperties.get("home_wallpaper_move"));
    	Log.d(TAG, "home_max_screen  default 5="+SystemProperties.get("home_max_screen"));    	
    	Log.d(TAG, "home_touch_slot default 8="+SystemProperties.get("home_touch_slot"));
    	
    	Log.d(TAG, "home_enable_checklog default 0="+SystemProperties.get("home_enable_checklog"));
        Log.d(TAG, "home_enable_checkfps default 0="+SystemProperties.get("home_enable_checkfps"));
        
        Log.d(TAG, "3d_bitmap_scale mdpi default 1="+SystemProperties.get("3d_bitmap_scale"));
        
        Log.d(TAG, "home_cache_workspace default 1 ="+SystemProperties.get("home_cache_workspace"));
        
        Log.d(TAG, "home_enable_sensor default 0 ="+SystemProperties.get("home_enable_sensor"));
        
        Log.d(TAG, "home_category_support_game default 0 ="+SystemProperties.get("home_category_support_game"));
        Log.d(TAG, "omshome.allapps.use.oem default 0 ="+SystemProperties.get("omshome.allapps.use.oem"));
        Log.d(TAG, "omshome.allapps.use.carrier default 0 ="+SystemProperties.get("omshome.allapps.use.carrier"));
        
        Log.d(TAG, "use_default_icon default 0 ="+SystemProperties.get("use_default_icon"));
        Log.d(TAG, "use_background_icon default 0 ="+SystemProperties.get("use_background_icon"));
        Log.d(TAG, "home_workspace_more_invalidate default 0 ="+SystemProperties.get("home_workspace_more_invalidate"));
        
        
        
    	Log.d(TAG, "END omshome settings for oms home\n\n");
    }
    
    private void showPage(int pageIndex, int with_effect) 
    {
    	if(LOGD)Log.d(TAG, "showPage:"+pageIndex+" with_effect:"+with_effect);
    	if(pageIndex > mWorkspace.getChildCount() - 1 || pageIndex < 0)
    	{
    		Log.i(TAG, "why give me wrong page index="+pageIndex + " cellLayout="+mWorkspace.getChildCount());
    		closeAllApps(false);
    		return;
    	}

    	if(0 == with_effect)
    	{
	    	mWorkspace.snapToScreenWithWallPaperMove(pageIndex);	    
    	}
    	else
    	{
    		mWorkspace.setCurrentScreen(pageIndex);
    		 if(dockStyle==1){
    	        	
    	     }else{
    	        	//for cmcc abondon style, but look good, need keep it
            		if(mLinePageIndicator != null)//for special move
            		{
            	        mLinePageIndicator.refreshPosition(getResources().getDrawable(R.drawable.page_indicator).getIntrinsicWidth(), 
            				  getResources().getDrawable(R.drawable.page_indicator).getIntrinsicHeight(), screenWidth, screenHeight, mWorkspace.getCurrentScreen(), mWorkspace.getChildCount());
            		}
            	  
            		//for oms2.0 traditional page view
            		if(mPageIndicator != null)
            		{
            			mPageIndicator.drawPageIndicator(mWorkspace.getCurrentScreen(),mWorkspace.getChildCount());
            		}
    	        }
    		mWorkspace.updateWallpaperOffset();
    	}
    	
        //force reset view focus
    	mWorkspace.getChildAt(pageIndex).requestFocus();
        closeAllApps(false);
    }
    
    /**
     * Receives notifications when applications are added/removed.
     */
    private class PhoneStateChangeIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            if(intent != null)
            {
                String action = intent.getAction();
//                if(LOGD)Log.d(TAG, "PhoneStateChangeIntentReceiver onReceive action=" + action);
                if(action != null && (action.equals("CallNotifier.CallEnd") || action.equals("CallNotifier.IncomingDisconnected")
                		|| action.equals("CallNotifier.InCall")))
                {
                		new Thread("updateMissCallCount PhoneStateChangeIntentReceiver"){
                			public void run(){
//                				if(LOGD)Log.d(TAG, "PhoneStateChangeIntentReceiver onReceive thread" );
                				updateMissCallCount();
                				mCallStateHandler.sendMessage(mCallStateHandler.obtainMessage(Launcher.this.MSG_UPDATEMISSCALL));
                			}
                		}.start();
                }
            }
        }
    }

    private class MissedCallContentObserver extends ContentObserver {     
        private static final String TAG = "Launcher.MissedCallContentObserver";     
        private Context ctx;     
             
        public MissedCallContentObserver(Context context, Handler handler) {     
            super(handler);     
            ctx = context;     
        }     
        
        @Override    
        public void onChange(boolean selfChange) 
        {    
//            if(LOGD)Log.d(TAG,"onChange selfChange:"+selfChange);            
            new Thread("updateMissCallCount"){
            	public void run(){
            		if(LOGD)Log.d(TAG,"onChange thread:");
            		updateMissCallCount();   
            		mCallStateHandler.sendMessage(mCallStateHandler.obtainMessage(Launcher.this.MSG_UPDATEMISSCALL));
            	}
            }.start();            
        }     
             
        @Override    
        public boolean deliverSelfNotifications() { 
            return super.deliverSelfNotifications();     
        }     
    }    
    
    private void updateMissCallCount() {    	
        final String where = Calls.TYPE +" = " + Calls.MISSED_TYPE + " and " + Calls.NEW + " = 1" ;
        if(null != getContentResolver()){
        	Cursor csr = getContentResolver().query(Calls.CONTENT_URI, new String[] {Calls.TYPE, Calls.NEW}, where, null, Calls.DEFAULT_SORT_ORDER);
        	if(csr != null)
        	{
        		missCallCount = csr.getCount();
        		csr.close();
        	}
        }
        
//        if(LOGD)Log.d(TAG, "updateMissCallCount missCallCount:"+missCallCount);         
    }
    
    private void displayMissCallIcon(boolean needDisplay) {
        if(true == needDisplay) {
            mMissCallImageView.setVisibility(View.VISIBLE);   
            if(mAllAppsGrid instanceof AllAppsScreenLayout){
            	((AllAppsScreenLayout)mAllAppsGrid).mMissCallImageView.setVisibility(View.VISIBLE);   
            }
        } else {
            mMissCallImageView.setVisibility(View.GONE);
            if(mAllAppsGrid instanceof AllAppsScreenLayout){
            	((AllAppsScreenLayout)mAllAppsGrid).mMissCallImageView.setVisibility(View.GONE);   
            }
        }
    }
    
    
    int callstate_icon_id = R.drawable.bottom_button_call_normal_background;
    public void startCall(View v){
//        if (isAllAppsVisible()) {
//            closeAllApps(true);
//        } else { 
            TelephonyManager manager =  ((TelephonyManager)getSystemService(TELEPHONY_SERVICE));

            if(R.drawable.cmcc_home_bottom_icon_call == callstate_icon_id || R.drawable.bottom_button_call_normal_background == callstate_icon_id) {
            	Intent call = new Intent(Intent.ACTION_DIAL);
            	call.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            	startActivity(call);
            	startActivitySafely(call, null);
            } else if(R.drawable.cmcc_home_bottom_icon_incall == callstate_icon_id || R.drawable.bottom_button_call_incall_background == callstate_icon_id) {
            	Intent  call = new Intent(Intent.ACTION_MAIN, null);
            	call.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            	call.setClassName("com.android.phone", "com.android.phone.DialtactsActivity");
            	startActivitySafely(call, null);
            } else if(R.drawable.cmcc_home_bottom_icon_misscall == callstate_icon_id || R.drawable.bottom_button_call_misscall_background == callstate_icon_id) {
            	Intent call = new Intent("com.android.phone.action.RECENT_CALLS_TAB_VIEW", null);
            	call.putExtra("extra_menu", false);
            	call.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	startActivitySafely(call, null);
            }
//        }
    }
    
    private void updateBottomCallIcons() {
//        if(LOGD)Log.d(TAG, "updateBottomCallIcons");
        TelephonyManager manager =  ((TelephonyManager)getSystemService(TELEPHONY_SERVICE));
        final Resources res = getResources();
        if(null == manager){
//        	if(LOGD)Log.e(TAG, "updateBottomCallIcons  manager is null!");
        	return;
        }
        
        if(TelephonyManager.CALL_STATE_OFFHOOK == manager.getCallState()) {
//    		if(LOGD)Log.d(TAG, "CALL_STATE_OFFHOOK");
    		displayMissCallIcon(false);
    		
    		if(mBottomLayout != null && "bottom_style_oms2".equals(((String)mBottomLayout.getTag()))){
    			mPreviousView.setBackgroundDrawable(res.getDrawable(R.drawable.bottom_button_call_incall_background));
    			callstate_icon_id = R.drawable.bottom_button_call_incall_background;
    			if(mAllAppsGrid instanceof AllAppsScreenLayout){
    				((AllAppsScreenLayout)mAllAppsGrid).callButton.setBackgroundDrawable(res.getDrawable(R.drawable.bottom_button_call_incall_background));
    			}
    		}else{
    			mPreviousView.setImageResource(R.drawable.cmcc_home_bottom_icon_incall);
    			callstate_icon_id = R.drawable.cmcc_home_bottom_icon_incall;
    			if(mAllAppsGrid instanceof AllAppsScreenLayout){
    				((AllAppsScreenLayout)mAllAppsGrid).callButton.setImageResource(R.drawable.cmcc_home_bottom_icon_incall);
    			}
    		}
    	} else{ 
    		if (missCallCount <= 0) {
    			displayMissCallIcon(false);
    			if(mBottomLayout != null && "bottom_style_oms2".equals(((String)mBottomLayout.getTag()))){
    				mPreviousView.setBackgroundDrawable(res.getDrawable(R.drawable.bottom_button_call_normal_background));
    				callstate_icon_id = R.drawable.bottom_button_call_normal_background;
    				if(mAllAppsGrid instanceof AllAppsScreenLayout){
    					((AllAppsScreenLayout)mAllAppsGrid).callButton.setBackgroundDrawable(res.getDrawable(R.drawable.bottom_button_call_normal_background));
    				}
    			}else{
    				mPreviousView.setImageResource(R.drawable.cmcc_home_bottom_icon_call);
    				callstate_icon_id = R.drawable.cmcc_home_bottom_icon_call;
    				if(mAllAppsGrid instanceof AllAppsScreenLayout){
    					((AllAppsScreenLayout)mAllAppsGrid).callButton.setImageResource(R.drawable.cmcc_home_bottom_icon_call);
    				}
    			}
    		} else {    			
    			displayMissCallIcon(true);
    			mMissCallImageView.setMissCallCount(missCallCount);    			
    			if(mBottomLayout != null && "bottom_style_oms2".equals(((String)mBottomLayout.getTag()))){
    				mPreviousView.setBackgroundDrawable(res.getDrawable(R.drawable.bottom_button_call_misscall_background));
    				callstate_icon_id = R.drawable.bottom_button_call_misscall_background;
    				if(mAllAppsGrid instanceof AllAppsScreenLayout){
    					((AllAppsScreenLayout)mAllAppsGrid).mMissCallImageView.setMissCallCount(missCallCount);    		
    					((AllAppsScreenLayout)mAllAppsGrid).callButton.setBackgroundDrawable(res.getDrawable(R.drawable.bottom_button_call_misscall_background));
    				}
    			}else{
    				mPreviousView.setImageResource(R.drawable.cmcc_home_bottom_icon_misscall);
    				callstate_icon_id = R.drawable.cmcc_home_bottom_icon_misscall;
    				if(mAllAppsGrid instanceof AllAppsScreenLayout){
    					((AllAppsScreenLayout)mAllAppsGrid).mMissCallImageView.setMissCallCount(missCallCount);    		
    					((AllAppsScreenLayout)mAllAppsGrid).callButton.setImageResource(R.drawable.cmcc_home_bottom_icon_misscall);
    				}
    			}
    		}
    	}
    }
    
    //preference SoundAndDisplaySettings
    private List<ResolveInfo> mHomeList;
    private boolean getHomes() {
      Intent intent = new Intent(Intent.ACTION_MAIN);
      intent.addCategory(Intent.CATEGORY_HOME);
      List<ResolveInfo> mList =  getPackageManager().queryIntentActivities(
              intent, PackageManager.MATCH_DEFAULT_ONLY);
      if (mList != null) {
          int N = mList.size();
          // Only display the first matches that are either of equal
          // priority or have asked to be default options.
          ResolveInfo r0 = mList.get(0);
          for (int i=1; i<N; i++) {
              ResolveInfo ri = mList.get(i);
              if(LOGD)Log.d(TAG,"getHomes ResolveInfo ri["+i+"]:"+ri);
              if (r0.priority != ri.priority ||
                  r0.isDefault != ri.isDefault) {
                  while (i < N) {
                      mList.remove(i);
                      N--;
                  }
              }
          }
          
          mHomeList = mList;
          N = mList.size();
          if(N > 1)return true;
      
      } else {
          if (LOGD) Log.e(TAG, "no home activity found");
      }
      
      return false;
  }
    
   private int currentHomeIndex = 0;
   private void showHomes() {
       if(null == mHomeList)
           return;
       
       int N = mHomeList.size();
       PackageManager pm = getPackageManager();
       String configuredHome = Settings.System.getString(getContentResolver(), "configured_home");
       CharSequence[] mValue = new CharSequence[N];
       CharSequence[] mTitle = new CharSequence[N];
       for (int i = 0; i < N; i++) {
           ResolveInfo ri = mHomeList.get(i);
           mValue[i] = Integer.toString(i);
           mTitle[i] = ri.activityInfo.loadLabel(pm);
           if (configuredHome != null && configuredHome.equals(ri.activityInfo.name)) {
               currentHomeIndex = i;
           }
       }
       
       new AlertDialog.Builder(this).setTitle(R.string.menu_choose_home).setSingleChoiceItems(mTitle, currentHomeIndex,new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            //select current home, do nothing
            if(which != currentHomeIndex) {
                Settings.System.putString(getContentResolver(), "configured_home", 
                        mHomeList.get(which).activityInfo.name);
                
                final Intent intentNewhome = new Intent("oms.action.HOME_CHANGED");
                intentNewhome.putExtra("configured_home", mHomeList.get(which).activityInfo.name);
          
                Intent intent =  new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                		| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
             
                new Handler().postDelayed(new Runnable(){
                	public void run(){
                		Launcher.this.sendBroadcast(intentNewhome);
                	}
                }, 50);
          
                startActivity(intent);
            }
            dialog.cancel(); 
                
        }
    }).setNegativeButton(R.string.cancel_action, null).show();
   }
   
   /**
    * Receives notifications when applications want to change shortcut symbol
    */
   private class CalendarRefreshReceiver extends BroadcastReceiver {
       @Override
       public void onReceive(Context context, Intent intent) {
               Bitmap bitmap = intent.getParcelableExtra(SHORTCUT_SYMBOL_BITMAP);
               ArrayList<BubbleTextView> calendarIcons = mWorkspace.findCalendarShortcuts();
//               if(LOGD)Log.d(TAG,"CalendarRefreshReceiver onReceive:"+intent.getAction()+"  calendarIcons:"+calendarIcons.size());
           
               final Bitmap iconBaseBitmap = Utilities.getExtendBitmapFromDrawable(Launcher.this.getResources().getDrawable(R.drawable.app_icon_calendar), context);
               Bitmap mutableIconBaseBitmap =  iconBaseBitmap.copy(Config.ARGB_8888, true);
               
               Canvas canvas = new Canvas(mutableIconBaseBitmap);
               canvas.drawBitmap(bitmap, 0, 0, null);
               
               Drawable draw = new BitmapDrawable(mutableIconBaseBitmap);
               ((BitmapDrawable)draw).setTargetDensity(context.getResources().getDisplayMetrics());
               
               //update icon on desktop 
               if(calendarIcons != null && calendarIcons.size()>0){
                   for(BubbleTextView calendarShortCut:calendarIcons){
                	   calendarShortCut.setCompoundDrawablesWithIntrinsicBounds(null, draw, null, null);
                   }
               }
               
               //update icon in folder
               mWorkspace.updateCalendarInconInFolder(mutableIconBaseBitmap);
       }
   }
   
   public final static String INTENT_ADD_SCREEN = "com.android.omshome.addscreen";
   public final static String INTENT_REMOVE_SCREEN = "com.android.omshome.removescreen";
   public final static String INTENT_REMOVE_SCREEN_PAGEINDEX = "com.android.omshome.removescreen_page";
   public final static String INTENT_UPDATE_SCREEN_OK = "com.android.omshome.updatescreen.ok";
   public final static String INTENT_CACHE_SCREEN = "com.android.omshome.cachescreen";
   public final static String INTENT_CHANGE_SCREEN = "com.android.omshome.intent_change_screen";
   public final static String BUNDLE_CHANGE_SCREEN_INFO = "com.android.omshome.intent_change_screen.info";
   public final static String INTENT_FINISH_PAGEMANAGER = "com.android.omshome.finish.pagemanager";
   public final static String INTENT_CREATE_BITMAP_OK = "com.android.omshome.createbitmap.ok";
   public final static String INTENT_RELAUNCHE_ACTIVITY = "com.android.omshome.relaunch_activity";
   
   private BroadcastReceiver pageManagerReceiver = new PageManagerIntentReceiver();
   private class PageManagerIntentReceiver extends BroadcastReceiver {
       @Override
       public void onReceive(Context context, Intent intent) {
           if(intent == null) return;
           String action = intent.getAction();
//           if(Launcher.LOGD)Log.d(TAG,"HomeRefreshIntentReceiver onReceive action:"+action);
           if(action.equals(INTENT_RELAUNCHE_ACTIVITY))
           {
        	   
           }
           if(action.equals(INTENT_ADD_SCREEN)) {
        	   if(mWorkspace.getChildCount() < Workspace.MAX_SCREEN_COUNT){
        		   Message msg = pageManagerHandler.obtainMessage(PageManagerHandler.MESSAGE_OPERATE_COMMAND);
        		   msg.getData().putInt(PageManagerHandler.BUNDLE_PAGE_COMMAND, PageManagerHandler.MESSAGE_OPERATE_ADD);
        		   pageManagerHandler.sendMessage(msg);
        		   Log.d(TAG, "PageManagerIntentReceiver send page command: add");
        	   }else{
        		   Intent updateok_intent = new Intent(INTENT_UPDATE_SCREEN_OK);
        		   sendBroadcast(updateok_intent); 
        	   }
           } else if(action.equals(INTENT_REMOVE_SCREEN)) {
        	   if(mWorkspace.getChildCount() > 1){
        		   int index = intent.getIntExtra(INTENT_REMOVE_SCREEN_PAGEINDEX, -1);
        		   if(LOGD)Log.d(TAG,"HomeRefreshIntentReceiver remove page:"+index);
        		   if(-1 != index) {
        			   Message msg = pageManagerHandler.obtainMessage(PageManagerHandler.MESSAGE_OPERATE_COMMAND);
        			   msg.getData().putInt(PageManagerHandler.BUNDLE_PAGE_COMMAND, PageManagerHandler.MESSAGE_OPERATE_REMOVE);
        			   msg.getData().putInt(INTENT_REMOVE_SCREEN_PAGEINDEX, index);
        			   pageManagerHandler.sendMessage(msg);
        			   if(LOGD)Log.d(TAG, "PageManagerIntentReceiver send page command: remove");
        		   }
        	   }else{
        		   Intent updateok_intent = new Intent(INTENT_UPDATE_SCREEN_OK);
        		   sendBroadcast(updateok_intent); 
        	   }
           } else if(action.equals(INTENT_FINISH_PAGEMANAGER)) {
               if(LOGD)Log.d(TAG,"back to launcher screen:"+intent.getExtras().getInt(STRING_PAGE_INDEX));
               isRunningPageManager = false;
               int with_effect = intent.getExtras().getInt(WITH_FADE_EFFECT);
               showPage(intent.getExtras().getInt(STRING_PAGE_INDEX), with_effect);
               if(true == isStartPageManager) {
                   isStartPageManager = false; 
               }
           } else if(action.equals(INTENT_CACHE_SCREEN)) {
               if(LOGD)Log.d(TAG,"HomeRefreshIntentReceiver cache screen action:"+action);
			   mWorkspace.createPageBitmaps();
           } else if(action.equals(INTENT_CHANGE_SCREEN)){
        	   if(LOGD)Log.d(TAG,"change screen");
        	   int[] pageinfo = intent.getIntArrayExtra(BUNDLE_CHANGE_SCREEN_INFO);
        	   
        	   Message msg = pageManagerHandler.obtainMessage(PageManagerHandler.MESSAGE_OPERATE_COMMAND);
        	   msg.getData().putInt(PageManagerHandler.BUNDLE_PAGE_COMMAND, PageManagerHandler.MESSAGE_OPERATE_MOVE);
        	   msg.getData().putIntArray(BUNDLE_CHANGE_SCREEN_INFO, pageinfo);
        	   pageManagerHandler.sendMessage(msg);
        	   if(LOGD)Log.d(TAG, "PageManagerIntentReceiver send page command: move");
           }
       }
   }
   
   private void updateDefaultPage(int index){
	   try{
		   final ContentResolver cr =  mWorkspace.getContext().getContentResolver();
           final int defaultIndex = getSettingsIntValue(Launcher.this, LauncherORM.default_page_index);
//           if(LOGD)Log.d(TAG, "delete index="+index + " default index="+(defaultIndex));
           
           //1 delete index > default index, nothing to do, keep 
           //2 ==, current page deleted, so use the middle page as default page
           //3 <, keep the page as default page, but we  need change the index
           if(index > defaultIndex)
           {
        	   //nothing
//        	   if(LOGD)Log.d(TAG, "keep default page");
           }
           else if(index == defaultIndex)
           {
        	   setSettingsIntValue(Launcher.this, LauncherORM.default_page_index, 0);
//        	   if(LOGD)Log.d(TAG, "change to the 0 position");
           }
           else if(index < defaultIndex)
           {
        	   setSettingsIntValue(Launcher.this, LauncherORM.default_page_index, defaultIndex-1);
//        	   if(LOGD)Log.d(TAG, "move default to pre");
           }
        	   
       }catch(Exception ne){}
   }
   
   void addPage() {
//       if(LOGD)Log.d(TAG,"addPage");
	   if(getScreenCount() >= Workspace.MAX_SCREEN_COUNT)
	   {
		   Log.d(TAG, "meet max screen count");
		   return ;
	   }
	   
	   setSettingsIntValue(Launcher.this, Workspace.TAG_SCREEN_NUM, mWorkspace.getChildCount()+1);
       
       View view = mInflater.inflate(R.layout.workspace_screen, null);  
     
       mWorkspace.addView(view, mWorkspace.getChildCount());
       setScreenCount(mWorkspace.getChildCount());
       mWorkspace.setScreenNum(mWorkspace.getChildCount());
       mWorkspace.setOnLongClickListener(this);
       mWorkspace.requestLayout();
       mWorkspace.invalidate();
       
       Intent intent = new Intent(INTENT_UPDATE_SCREEN_OK);
       sendBroadcast(intent); 
   }
   
   private void removePage(final int pageIndex)  throws Exception{
//       if(LOGD)Log.d(TAG,"removePage pageIndex:"+pageIndex);
       isPageMoving = true;
       if(LOGD)Log.d(TAG, "removePage run db operation");
       try{
    	   
           if(null != mFolders && mFolders.size()>0){
    		   HashMap<Long, FolderInfo> tmpFolders = new HashMap<Long, FolderInfo>();
    		   Set<Long> keys = mFolders.keySet();
    		   Iterator<Long> iterator = keys.iterator();
    		   while(iterator.hasNext()){
    			  Long key = iterator.next();
    			  FolderInfo info = mFolders.get(key);
    			  if(info instanceof UserFolderInfo && info.screen == pageIndex){
    				  LauncherModel.deleteUserFolderContentsFromDatabase(Launcher.this, (UserFolderInfo)info);
    				  iterator.remove();
    			  }
    		   }
    		}
    	   
    	   //Update db
    	   LauncherModel.updateItemsWithDeleteScreenIndexFromDatabase(Launcher.this,pageIndex);
    	   
    	   //update mDesktopItems
    	   removeItemInfo(pageIndex);
    	   
    	   //update widgetHostViews
    	   widgetHostViews.removeAll(mWorkspace.findAppWidgetHostViews(pageIndex));
           
    	   CellLayout child = (CellLayout) mWorkspace.getChildAt(pageIndex);
           if(child != null) {
        	   child.removeAllViews();
           }
    	   
    	   mWorkspace.removeViewAt(pageIndex);
    	   setScreenCount(mWorkspace.getChildCount());
    	   mWorkspace.setScreenNum(mWorkspace.getChildCount());
    	   mWorkspace.resetCellLayout();
    	   mWorkspace.requestLayout();
    	   if(pageIndex == mWorkspace.getCurrentScreen()){
    		   mWorkspace.setCurrentScreen(mWorkspace.getCurrentScreen()-1);
    	   }else{
    		   mWorkspace.invalidate();
    	   }
       }catch(Exception ex){
    	   Log.d(TAG, "removePage operate db exit with 0:"+ex.getMessage());
    	   throw ex;
       }finally{
    	   isPageMoving = false;
    	   Intent intent = new Intent(INTENT_UPDATE_SCREEN_OK);
    	   sendBroadcast(intent);
       }
   }
   
   private void movePage(final int fromIndex, final int toIndex) throws Exception{
//	   if(LOGD)Log.i(TAG, "movePage  fromIndex:"+fromIndex+" toIndex:"+toIndex);
	   View view = mWorkspace.getChildAt(fromIndex);
	   isPageMoving = true;
	   
       try{
    	   LauncherModel.moveItemsWithScreenIndex(Launcher.this, fromIndex,toIndex);
    	   changeItemInfos(fromIndex,toIndex);
    	 
    	   if(-1 == toIndex){ // move to last screen
    		   mWorkspace.removeViewAt(fromIndex);
    		   mWorkspace.addView(view);
    	   } else if(fromIndex < toIndex) { // move right
    		   mWorkspace.removeViewAt(fromIndex);
    		   mWorkspace.addView(view,toIndex);
    	   } else {  //move left
    		   mWorkspace.removeViewAt(fromIndex);
    		   mWorkspace.addView(view,toIndex);
    	   }
    	   
    	   mWorkspace.resetCellLayout();
    	   mWorkspace.requestLayout();
    	   mWorkspace.invalidate();
       }catch(Exception ex){
    	   Log.e(TAG, "movePage operate db exit with 0:"+ex.getMessage());
    	   throw ex;
       }finally{ 
    	   isPageMoving = false;
       }
   }
   
   class PageManagerHandler extends Handler{
	   public final static int MESSAGE_OPERATE_NEXT = 0x1;
	   public final static int MESSAGE_OPERATE_COMMAND = 0x2;
	   public final static String BUNDLE_PAGE_COMMAND = "command";
	   public final static int MESSAGE_OPERATE_ADD = 0x1;
	   public final static int MESSAGE_OPERATE_REMOVE = 0x2;
	   public final static int MESSAGE_OPERATE_MOVE = 0x3;
	   public ArrayList<Message> pageCommands = new ArrayList<Message>();
	   private ContentResolver cr = null;
	   
	   public PageManagerHandler(Context con){
		   cr = getContentResolver();
	   }
	   
	   @Override
	   public void handleMessage(Message msg) {
		   switch(msg.what){
		   case MESSAGE_OPERATE_COMMAND:
			   synchronized(pageCommands){
				   pageCommands.add(msg);
			   }
			   
			   if(pageCommands.size() == 1){
				   operateMsg(msg);
			   }
			   break;
		   case MESSAGE_OPERATE_NEXT:
			   //remove previous command.
			   synchronized(pageCommands){
				   if(pageCommands.size()>0){
					   pageCommands.remove(0);
				   }
			   }
			   
			   if(pageCommands.size() > 0){
				   operateMsg(pageCommands.get(0));
			   }else{
				   //there is no more commands, so we collect garbage.
				   if(LOGD)Log.d(TAG, "all commands accomplished, call System.gc");
				   System.gc();
			   }
			   break;
		   default:
		   }
	   }
	   
	   private void operateMsg(Message msg){
//		   if(LOGD)Log.d(TAG, "PageManagerHandler command:"+msg.getData().getInt(BUNDLE_PAGE_COMMAND));
		   
		   int command = msg.getData().getInt(BUNDLE_PAGE_COMMAND);
		   try{
			   if(MESSAGE_OPERATE_ADD == command){				   
				   isRunningPageManager = true;
				   addPage();
			   }else if(MESSAGE_OPERATE_REMOVE == command){
				   int toBePageCount = mWorkspace.getChildCount()-1;
				   setSettingsIntValue(Launcher.this, Workspace.TAG_SCREEN_NUM, toBePageCount);
				   isRunningPageManager = true;
				   int index = msg.getData().getInt(INTENT_REMOVE_SCREEN_PAGEINDEX);
				   updateDefaultPage(index);
				   removePage(index);
			   }else if(MESSAGE_OPERATE_MOVE == command){
				   int[] pageinfo = msg.getData().getIntArray(BUNDLE_CHANGE_SCREEN_INFO);
				   movePage(pageinfo[0], pageinfo[1]);
			   }
		   }catch(Exception ex){
			   pageCommands.clear();
			   Log.e(TAG, "operate page manage error: "+ex.getMessage());
		   }finally{
			   pageManagerHandler.sendMessage(pageManagerHandler.obtainMessage(PageManagerHandler.MESSAGE_OPERATE_NEXT));
		   }
	   }
	   
   }
   
   private void changeItemInfos(int fromIndex, int toIndex){
//	   if(LOGD)Log.d(TAG, "changeItemInfos fromIndex:"+fromIndex+" toIndex:"+toIndex);
	   
	   //execute desktop items =======================
       ArrayList<ItemInfo> tmp = new ArrayList<ItemInfo>();
       for(int i=0; i<mDesktopItems.size(); i++){
		   if(mDesktopItems.get(i).screen == fromIndex){
			   tmp.add(mDesktopItems.get(i));
		   }
	   }
       
       if(-1 == toIndex){ // move to last screen
          for(ItemInfo info: mDesktopItems){
        	  if(info.screen > fromIndex){
        		  info.screen--;
        	  }
          }
       } else if(fromIndex < toIndex) { // move right
    	  for(ItemInfo info: mDesktopItems){
    		  if(info.screen>fromIndex && info.screen <= toIndex){
    			  info.screen--;
    		  }
    	  }
       } else {  //move left
       	  for(ItemInfo info: mDesktopItems){
       		  if(info.screen>=toIndex && info.screen < fromIndex){
       			  info.screen++;
       		  }
       	  }
       }
       
	   for(int i=0; i<tmp.size(); i++){
		   if(-1 == toIndex){
			   tmp.get(i).screen = mWorkspace.getChildCount() - 1;
		   }else{
			   tmp.get(i).screen = toIndex;
		   }
	   }
	   
	   //execute folders =======================
	   if(null != mFolders && mFolders.size()>0){
		   HashMap<Long, FolderInfo> tmpFolders = new HashMap<Long, FolderInfo>();
		   Set<Long> keys = mFolders.keySet();
		   Iterator<Long> iterator = keys.iterator();
		   while(iterator.hasNext()){
			  Long key = iterator.next();
			  FolderInfo info = mFolders.get(key);
			  if(info.screen == fromIndex){
//				  Log.d(TAG, "tmpFolders put key:"+key+" info:"+info.screen);
				  tmpFolders.put(key, info);
			  }
		   }
		   
		   if(-1 == toIndex){ // move to last screen
			   Set<Long> tmpkeys = mFolders.keySet();
			   Iterator<Long> tmpiterator = tmpkeys.iterator();
			   while(tmpiterator.hasNext()){
				  Long key = tmpiterator.next();
				  FolderInfo info = mFolders.get(key);
				  if(info.screen > fromIndex){
					  info.screen--;
//					  Log.d(TAG, "toIndex ==-1 info.screen--:"+ info.screen);
				  }
			   }
	       } else if(fromIndex < toIndex) { // move right
	    	   Set<Long> tmpkeys = mFolders.keySet();
			   Iterator<Long> tmpiterator = tmpkeys.iterator();
			   while(tmpiterator.hasNext()){
				  Long key = tmpiterator.next();
				  FolderInfo info = mFolders.get(key);
				  if(info.screen > fromIndex && info.screen <= toIndex){
//					  Log.d(TAG, "fromIndex < toIndex info.screen--:"+ info.screen);
					  info.screen--;
				  }
			   }
	       } else {  //move left
	       	   Set<Long> tmpkeys = mFolders.keySet();
			   Iterator<Long> tmpiterator = tmpkeys.iterator();
			   while(tmpiterator.hasNext()){
				  Long key = tmpiterator.next();
				  FolderInfo info = mFolders.get(key);
				  if(info.screen>=toIndex && info.screen < fromIndex){
//					  Log.d(TAG, "fromIndex >= toIndex info.screen++:"+ info.screen);
	       			  info.screen++;
	       		  }
			   }
	       }
		   
		   Set<Long> tmpkeys = tmpFolders.keySet();
		   Iterator<Long> tmpiterator = tmpkeys.iterator();
//		   Log.d(TAG, "tmpFolders, tmpkeys:"+ tmpkeys.size());
		   while(tmpiterator.hasNext()){
			  Long key = tmpiterator.next();
			  FolderInfo info = tmpFolders.get(key);
			  if(-1 == toIndex){
				  info.screen = mWorkspace.getChildCount() - 1;
//				  Log.d(TAG, "tmpFolders, reset  toIndex==-1 info.screen:"+  info.screen);
			  }else{
				  info.screen = toIndex;
//				  Log.d(TAG, "tmpFolders, reset  toIndex!=-1 info.screen:"+ info.screen);
			  }
		   }
	   }
	   
//	   if(LOGD)Log.d(TAG, "changeItemInfos exit");
	   //dumpItemInfo();
   }
   
   private void removeItemInfo(int screen){
//	   if(LOGD)Log.d(TAG, "removeItemInfo screen:"+screen);
	   
	   if(null != mDesktopItems && mDesktopItems.size() > 0){
		   for(int i=0; i<mDesktopItems.size(); i++){
			   if(mDesktopItems.get(i).screen == screen){
				   mDesktopItems.get(i).unbind();
				   mDesktopItems.remove(i);
				   i--;
			   }
		   }
		   
		   for(int i=0; i<mDesktopItems.size(); i++){
			   if(mDesktopItems.get(i).screen > screen){
				   mDesktopItems.get(i).screen--;
			   }
		   }
	   }
	   
	   if(null != mFolders && mFolders.size()>0){
		   Set<Long> keys = mFolders.keySet();
		   Iterator<Long> iterator = keys.iterator();
		   while(iterator.hasNext()){
			  Long key = iterator.next();
			  FolderInfo info = mFolders.get(key);
			  if(info.screen == screen){
				  iterator.remove();
			  }else if(info.screen>screen){
				  info.screen--;
			  }
		   }
	   }
	   
//	   if(LOGD)Log.d(TAG, "removeItemInfo exit");
	   //dumpItemInfo();
   }
   
   private void dumpItemInfo(){
	   Log.d(TAG, "dumpItemInfo  mDesktopItems:"+mDesktopItems.size());
	   for(ItemInfo info: mDesktopItems){
		   if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
			   Log.d(TAG, "info screen:"+info.screen+" cellX:"+info.cellX+" cellY:"+info.cellY+" name:"+((ApplicationInfo)info).title+" this:"+info);
		   }else if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET){
			   Log.d(TAG, "info screen:"+info.screen+" cellX:"+info.cellX+" cellY:"+info.cellY+" name:"+((LauncherAppWidgetInfo)info).className+ " this:"+ info);
		   }else if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER || info.itemType == LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER){
			   Log.d(TAG, "info screen:"+info.screen+" cellX:"+info.cellX+" cellY:"+info.cellY+" name:"+((FolderInfo)info).title+ " this:"+ info);
		   }
	   }
	   
	   Log.d(TAG, "dumpFolders  mFolders:"+mFolders.size());
	   if(mFolders.size()>0){
		   Set<Long> keys = mFolders.keySet();
		   Iterator<Long> iterator = keys.iterator();
		   while(iterator.hasNext()){
			  Long key = iterator.next();
			  FolderInfo info = mFolders.get(key);
			  Log.d(TAG, "info screen:"+info.screen+" cellX:"+info.cellX+" cellY:"+info.cellY+" name:"+((FolderInfo)info).title+ " this:"+ info);
			  if(info instanceof UserFolderInfo){
				  ArrayList<ShortcutInfo> infosInFolder = ((UserFolderInfo)info).contents;
				  if(null != infosInFolder && infosInFolder.size()>0){
					  for(ShortcutInfo tmp:infosInFolder){
						  Log.d(TAG, "info screen:"+tmp.screen+" cellX:"+tmp.cellX+" cellY:"+tmp.cellY+" name:"+((ShortcutInfo)tmp).title+" this:"+tmp);
					  }
				  }
			  }
		   }
	   }
	   
   }
   
   private void refreshApp() {       
       lockAllApps();  
       isNeedCheckCategory = true;
       isNeedAllAppsSort = true;       
       mModel.startLoader(this, false, true);       
   }
   
   private void refreshWorkspace() {
//	   if(LOGD)Log.d(TAG,"refreshWorkspace");
	   mWorkspaceLoading = true;

	   unbindDesktopItems();
	   mDesktopItems.clear();
	   
	   if(LauncherORM.instanceUnSafe() != null)
       {
       	   LauncherORM.instanceUnSafe().destroy();
       }
	   
	   isNeedResetViewImage = true;
       setupViews();
       
//   	   mIconCache.unbindDrawables(); //remove :11986 //ticket:1999, 10062
//	   mIconCache.flush();
       
       lockAllApps();  
       isNeedCheckCategory = true;
       isNeedAllAppsSort = true;
       isNeedDeleteAllWebWidgetView = true;
       mModel.startLoader(this, false);
       restoreState(mSavedState);
   }
   
   //used for reset/reclear/new ship phone
   private boolean installSTK(){
       if(LOGD)Log.d(TAG,"installSTK ");
       final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
       mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

       final PackageManager packageManager = this.getPackageManager();
       final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
       
       if(apps == null || apps.size() == 0)Log.d(TAG, "resolveInfo is null, fail to install STK!");
       if(apps != null & apps.size() > 0) {
           for (int i=0; i < apps.size(); i++) {
//        	   if(LOGD)Log.d(TAG, "installSTK, parse resolveInfo: "+apps.get(i).activityInfo.packageName);
               if("com.android.borqsstk".equals(apps.get(i).activityInfo.packageName)) {
                   final ResolveInfo resolveInfo = apps.get(i);

                   if (resolveInfo == null) {
                	   if(LOGD)Log.d(TAG, "resolveInfo is null, fail to install STK!");
                       return false;
                   }
                   
                   Intent intent = new Intent(Intent.ACTION_MAIN, null);
                   intent.addCategory(Intent.CATEGORY_LAUNCHER);
                   intent.setComponent(new ComponentName("com.android.borqsstk", "com.android.borqsstk.MainScreen"));
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                   
                   Intent shortIntent = new Intent(InstallShortcutReceiver.ACTION_INSTALL_SHORTCUT);
                   Drawable icon = resolveInfo.loadIcon(packageManager);
                   shortIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, ((BitmapDrawable)icon).getBitmap());
                   shortIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
                   shortIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, resolveInfo.loadLabel(packageManager));
                   sendBroadcast(shortIntent);
//                   if(LOGD)Log.d(TAG, "Success to send intent for install stk");
                   return true;
               }
           }
       }
       
       return false;
   }

   private static float mFontScale;
   private static int mTheme = -1;
   
   @Override
   public void onConfigurationChanged(Configuration newConfig) {
       Log.i(TAG, "onConfigurationChanged newConfig.orientation:"+newConfig.orientation);
       
       //
       //for enable sensor, here no onPause, no onCreate, so need remember the value
       preOrientation = newConfig.orientation;
       
       if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
	   {
    	   if(LOGD)Log.d(TAG,"onConfigurationChanged changed, ORIENTATION_LANDSCAPE force set portrait");
	   }
	   else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) 
	   {
		   if(LOGD)Log.d(TAG,"onConfigurationChanged changed, ORIENTATION_PORTRAIT");
	   }
       if (mFontScale != newConfig.fontScale) {
           if(LOGD)Log.d(TAG,"onConfigurationChanged fontScale changed, do nothing here,");
       }
       
       super.onConfigurationChanged(newConfig);
   }
   
   boolean checkConfiguration() {
        return true;
   }
   
   public LauncherModel getLauncherModel(){
	   return mModel;
   }
   
   public LauncherORM getLauncherORM(){
	   return LauncherORM.instance(this);
   }
   
   private boolean isNeedUseOldScreenIndex = false;
   public boolean getIsNeedUseOldScreenIndex(){
	   return isNeedUseOldScreenIndex;
   }
   public void setIsNeedUseOldScreenIndex(boolean index){
	   isNeedUseOldScreenIndex = index;
   }
   
   public final static String INTENT_RESTORE_BACKUP = "com.android.omshome.restoreBackup";
   private boolean isRestoreBackupAPK = false;
   private boolean isRestoreBackupWidget = false;
   private class RestoreBackupIntentReceiver extends BroadcastReceiver {
       @Override
       public void onReceive(Context context, Intent intent) 
       {
           if(intent != null)
           {
               String action = intent.getAction();
               if(Launcher.LOGD)Log.d(TAG, "RestoreBackupIntentReceiver onReceive action=" + action);
               if(action != null && action.equals(INTENT_RESTORE_BACKUP))
               {
            	   if(0 == intent.getIntExtra("type", -1)){
//            		   if(Launcher.LOGD)Log.d(TAG, "RestoreBackupIntentReceiver set isRestoreBackupAPK true");
            		   isRestoreBackupAPK = true;
            	   }else if(1 == intent.getIntExtra("type", -1)){
//            		   if(Launcher.LOGD)Log.d(TAG, "RestoreBackupIntentReceiver  set isRestoreBackupWidget true");
            		   isRestoreBackupWidget = true;
            	   }
               }
           }
       }
   }
   
   public boolean getIsRestoreBackupAPK(){
	   return isRestoreBackupAPK;
   }
   
   public void setIsRestoreBackupAPK(boolean flag){
	   isRestoreBackupAPK = flag;
   }
   
   public boolean getIsRestoreBackupWidget(){
	   return isRestoreBackupWidget;
   }
   
   public void setIsRestoreBackupWidget(boolean flag){
	   isRestoreBackupWidget = flag;
   }
 
   public int getDisplayHeight(){
   	   return getWindowManager().getDefaultDisplay().getHeight();
   }
     
   public int getDisplayWidth(){
   	   return getWindowManager().getDefaultDisplay().getWidth();
   }
   
   private static final String OPLTAG_DISPLAY_CONFIG = "opl_omshome_display_config";
   private static final String OPL_DISPLAY_CONFIG_FILE = "/opl/etc/new_omshome_display_config.xml";
   public final static int DISPLAY_CONFIG_2D_PAGEMANAGER_INDEX = 0;
   public final static int DISPLAY_CONFIG_2D_ALLAPP_INDEX = 1;
   public final static int DISPLAY_CONFIG_SCREEN_FAST_SPEED_INDEX = 2;
   public static final String DISPLAY_CONFIG_DEFAULT_2D = "011";
   public static final String DISPLAY_CONFIG_DEFAULT_3D = "000";
   public static String getDisplayConfig(){
	   if(isEmulator()){
		   return DISPLAY_CONFIG_DEFAULT_2D;
	   }
	   
       if(LOGD)Log.d(TAG,"getDisplayConfig");       
       try {
	       	XmlPullParser parser = null;
	       	if(new File(OPL_DISPLAY_CONFIG_FILE).exists() == true) 
	       	{
//	       		if(LOGD)Log.d(TAG, "load "+ OPL_DISPLAY_CONFIG_FILE);            		
	           	FileReader reader = new FileReader(new File(OPL_DISPLAY_CONFIG_FILE));
	           	parser = Xml.newPullParser();
                parser.setInput(reader);
               
                String xml_ns = "";
                XmlUtils.beginDocument(parser, OPLTAG_DISPLAY_CONFIG);
                final int depth = parser.getDepth();       
                int type;
                while (((type = parser.next()) != XmlPullParser.END_TAG ||
                        parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }
                    
                    if("value".equals(parser.getAttributeName(0))){
                    	return parser.getAttributeValue(0);
                    }
                }
	       	}else{
	       		if(LOGD)Log.d(TAG, "no " + OPL_DISPLAY_CONFIG_FILE);        
	       		return null;
	       	}
       }catch(Exception ex){
           ex.printStackTrace();
           return null;
       }
	
	   return null;
   }
   
   public static boolean isEmulator()
   {
       return android.os.SystemProperties.get("ro.kernel.qemu").equals("1") == true;
   }
   
   public static boolean isUsingHomeSettings(){
	   return SystemProperties.get(HOMEPROPERTY_USESETTINGS).equals("1") == true;
   }

   public void resetSkipLongClick(){
	   skipLongClick = false;
   }
   
   public CellLayout.CellInfo getMaddItemCellInfo()
   {
       return mAddItemCellInfo;
   }
   
   public boolean isAllAppsLoading(){
	   return isAllAppsLoading;
   }
   private void setIsAllAppsLoading(boolean loading)
   {
	   isAllAppsLoading = loading;
   }
   
 /***********************add app widget to Screen and auto find a sutiable space to drop it BEGIN************************/
   
  boolean findSlot(CellInfo cellInfo, int[] xy, int spanX, int spanY,boolean needToast) {
       if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
           boolean[] occupied = mSavedState != null ?
                   mSavedState.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS) : null;
           cellInfo = mWorkspace.findAllVacantCells(occupied);
           if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
               if(needToast)
               {
                   if(mToast == null)
                   {
                       mToast = Toast.makeText(this, getString(R.string.out_of_space), 400);
                   }
                   else
                   {
                       mToast.setText(getString(R.string.out_of_space));
                   }
                  
                   mToast.show();
               }
               return false;
           }
       }
       return true;
   }
   
   public int findFitableSingleSlot(CellLayout.CellInfo cellInfo)
   {
       final int[] xy = new int[2];
       int fitableIndex = findFitableSlotIndex(cellInfo, xy, 1, 1);
       if(fitableIndex != -1)
       {
           cellInfo.cellX = xy[0];
           cellInfo.cellY = xy[1];
       }
      return fitableIndex;
   }
   
   private int findFitableSlotIndex(CellLayout.CellInfo cellInfo, int[] xy, int spanX, int spanY) {
       int fitableIndex = -1;
       if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
           boolean[] occupied = mSavedState != null ?
                   mSavedState.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS) : null;
           int currentScreen = cellInfo.screen;
           boolean canDrop = false;
           for(int i=currentScreen; i<mWorkspace.getChildCount();i++)
           {
               CellLayout cellLayout = (CellLayout)mWorkspace.getChildAt(i);
               cellInfo = cellLayout.findAllVacantCells(occupied, null);
               if(cellInfo.findCellForSpan(xy, spanX, spanY))
               {
                  // fitIndex = i;
                  // break;
                   canDrop = true;
                  // mWorkspace.snapToScreen(i);
                   fitableIndex = i;
                   break;
               }
           }
           
           if(canDrop == false)
           {
               for(int i=currentScreen-1; i>=0;i--)
               {
                   CellLayout cellLayout = (CellLayout)mWorkspace.getChildAt(i);
                   cellInfo = cellLayout.findAllVacantCells(occupied, null);
                   if(cellInfo.findCellForSpan(xy, spanX, spanY))
                   {
                       canDrop = true;
                     //  mWorkspace.snapToScreen(i);
                       fitableIndex = i;
                       break;
                   }
               }
           }
           
           //cellInfo = mWorkspace.findAllVacantCells(occupied);
           //if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
            //   Toast.makeText(this, getString(R.string.out_of_space), Toast.LENGTH_SHORT).show();
           //    return false;
          // }
           if(canDrop == false)
           {
               Toast.makeText(this, getString(R.string.out_of_space), Toast.LENGTH_SHORT).show();
           }
           return fitableIndex;
       }
       else
       {
           return cellInfo.screen;
       }
   }
   
   CellInfo newDragCellInfo;
   boolean isNewDraging;
   private NewDragView newDragView;
   final Handler mLauncherLauncher = new LauncherHandler();
   boolean isSnapingToDestination = false;
   int mSnapingCurrentScreen = -1;
   int msnapingDestinationScreen = -1;
   boolean isForceEndDrag = false;
   int[] newDragxy;
   int[] newDragSpan;
   Toast mToast = null;
   private static final int APPLICATION = 0;
   private static final int SHORTCUT  = 1;
   private static final int FOLDER    = 2;
   private static final int LIVEFOLDER = 3;
   private static final int WEBWIDGET  = 4;
   private static final int APPWIDGET  = 5;
   
   void startNewDrag(Intent data,int dataType,CellInfo cellInfo)
   {
       //record data,dataType,cellInfo 
       newDragCellInfo = cellInfo;
       isNewDraging = true;
       createNewDragView(data,dataType);
   }
   
   private void createNewDragView(Intent data,int dataType) {
       switch(dataType)
       {
           case APPLICATION:
               createApplicationNewDragView(data);
               break;
           case SHORTCUT:
               createShortcutNewDragView(data);
               break;
           case FOLDER:
               createFolderNewDragView(data);
               break;
           case LIVEFOLDER:
               createLiveFilderNewDragView(data);
               break;
           case WEBWIDGET:
               createWebWidgetNewDragView(data);
               break;
           case APPWIDGET:
               createAppWidgetNewDragView(data);
               break;
           
       }
       
   }

   private void createAppWidgetNewDragView(Intent data) {
       Bundle extras = data.getExtras();
       final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
       AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
       //begin get the app widget info from xml again to fix the landscape and portrait switch
       final PackageManager pm = this.getPackageManager();
       Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
       intent.setComponent(appWidgetInfo.provider);
       List<ResolveInfo> ri_list = pm.queryBroadcastReceivers(intent, PackageManager.GET_META_DATA);  
       if(ri_list != null && ri_list.size() > 0)
       {
           AppWidgetProviderInfo tmp = parseProviderInfoXml(pm, appWidgetInfo.provider, ri_list.get(0));
           if(tmp != null)
           {
               appWidgetInfo.minHeight  = tmp.minHeight;
               appWidgetInfo.minWidth  = tmp.minWidth;
               
               tmp = null;
           }
       }        
       //end for re-query the min width and height for configuration change
       // Calculate the grid spans needed to fit this widget
       CellLayout layout = (CellLayout) mWorkspace.getChildAt(newDragCellInfo.screen);
       final int[] spans = layout.rectToCell(appWidgetInfo.minWidth, appWidgetInfo.minHeight);
       // Try finding open space on Launcher screen
       final int[] xy = mCellCoordinates; 
       newDragxy = xy;
       newDragSpan = spans;
       final LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId);
       launcherInfo.spanX = spans[0];
       launcherInfo.spanY = spans[1];
       launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
       launcherInfo.packageName = appWidgetInfo.provider.getPackageName();
       launcherInfo.className   = appWidgetInfo.provider.getClassName();
       launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
       launcherInfo.hostView.setTag(launcherInfo);
       
      // Log.d(TAG,"b191== hostView==left="+launcherInfo.hostView.getLeft()+"==top="+launcherInfo.hostView.getTop()+"==width="+launcherInfo.hostView.getWidth()+"==height"+launcherInfo.hostView.getHeight()+"=="+launcherInfo.hostView.getLayoutParams());
      int windowWidth = this.getWindowManager().getDefaultDisplay().getWidth();
      int windowHeight = this.getWindowManager().getDefaultDisplay().getHeight() - (int)(getResources().getDimension(R.dimen.button_bar_height));
      newDragView = new NewDragView(Launcher.this,windowWidth,windowHeight);
      newDragView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
      CellLayout.LayoutParams layoutParams = new CellLayout.LayoutParams(xy[0],xy[1],spans[0],spans[1]);
      newDragView.addView(launcherInfo.hostView,layoutParams);
//      CloseView closeView = new CloseView(Launcher.this);
//      closeView.setOnClickListener(new OnClickListener(){
//
//           public void onClick(View v) {
//               //just close this view;
//               if (appWidgetId != -1) mAppWidgetHost.deleteAppWidgetId(appWidgetId);
//                 newDragView.removeAllViews();
//                 mDragLayer.removeView(newDragView);
//                 isNewDraging = false;
//                 isSnapingToDestination = false;
//                 newDragView = null;
//           }
//           
//       });
//       if(closeView.mBitmap != null)
//       {
//           newDragView.addView(closeView,new LayoutParams(closeView.mBitmap.getWidth(),closeView.mBitmap.getHeight()));
//       }
       mDragLayer.addView(newDragView);
       newDragView.setOnClickListener(new OnClickListener(){
           public void onClick(View v) {
               newDragView.removeAllViews();
               if(isForceEndDrag == false && findSlot(newDragCellInfo, xy, spans[0], spans[1]))
               {
                       //drop here
                       LauncherModel.addItemToDatabase(Launcher.this, launcherInfo,
                               LauncherSettings.Favorites.CONTAINER_DESKTOP,
                               mWorkspace.getCurrentScreen(), xy[0], xy[1], false);
                       mDesktopItems.add(launcherInfo);
                       if(launcherInfo.packageName.equals("oms.dcd") && (launcherInfo.hostView.getChildAt(0) instanceof ViewGroup)){
                           widgetHostViews.add(launcherInfo.hostView);
                       }
                       mWorkspace.addInCurrentScreen(launcherInfo.hostView, xy[0], xy[1],launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());
              }
               else
               {
                   if (appWidgetId != -1) mAppWidgetHost.deleteAppWidgetId(appWidgetId);
               }
               mDragLayer.removeView(newDragView);
               isForceEndDrag = false;
               isNewDraging = false;
               isSnapingToDestination = false;
               mToast = null;
               newDragView = null;
           }
           
       });
       //snapToFitableScreen       
       int index = findFitableSlotIndex(newDragCellInfo,xy,spans[0],spans[1]);
       int currentScreen = getCurrentWorkspaceScreen();
      
       if(index != currentScreen && index >=0 && index < mWorkspace.getChildCount())
       {
           isSnapingToDestination = true;
           if(index > currentScreen)
           {   
               mSnapingCurrentScreen = currentScreen+1;
               msnapingDestinationScreen = index;
               Message msg = mLauncherLauncher.obtainMessage(SNAP_TO_SCREEN);
               mLauncherLauncher.sendMessageDelayed(msg, 300);        
           }
           else
           {
               mSnapingCurrentScreen = currentScreen-1;
               msnapingDestinationScreen = index;
               Message msg = mLauncherLauncher.obtainMessage(SNAP_TO_SCREEN);
               mLauncherLauncher.sendMessageDelayed(msg, 300);
           }
       }
   }

   private void createWebWidgetNewDragView(Intent data) {
    // TODO Auto-generated method stub
    
   }

   private void createLiveFilderNewDragView(Intent data) {
    // TODO Auto-generated method stub
    
   }

   private void createFolderNewDragView(Intent data) {
    // TODO Auto-generated method stub
    
   }

   private void createShortcutNewDragView(Intent data) {
    // TODO Auto-generated method stub
    
   }

   private void createApplicationNewDragView(Intent data) {
       // TODO Auto-generated method stub
           
   }

   void endNewDrag(float x, float y)
   {
       if(isNewDraging)
       {
           Rect frame_0 = new Rect();
           Rect frame_1 = new Rect();
           mWorkspace.getHitRect(frame_0);
           
           View childView_1 = null;
           boolean hasCloseView = false;
           for(int i=0;i<newDragView.getChildCount();i++)
           {   
               childView_1 = newDragView.getChildAt(i);
               if(childView_1 instanceof CloseView)
               {
                   hasCloseView = true;
                   childView_1.getHitRect(frame_1);
                   break;
               }
           }
           
           if( hasCloseView && frame_1.contains((int)x,(int)y))
           {
               childView_1.performClick();
           }
           else if(frame_0.contains((int)x,(int)y))
           {
               newDragView.performClick();
           }
       }
   }
   

   private void forceEndNewDrag() {
       isForceEndDrag = true;
       newDragView.performClick();
   }
   final static int SNAP_TO_SCREEN = 0;
   class LauncherHandler extends Handler{
    
    @Override
    public void handleMessage(Message msg) {
        //super.handleMessage(msg);
        switch(msg.what)
        {
            case SNAP_TO_SCREEN:
            {
               
                if( mSnapingCurrentScreen>=0 && mSnapingCurrentScreen<mWorkspace.getScreenCount())
                {
                    mWorkspace.snapToScreen(mSnapingCurrentScreen);
                    if(mSnapingCurrentScreen != msnapingDestinationScreen)
                    {
                        if(mSnapingCurrentScreen<msnapingDestinationScreen)
                        {
                            mSnapingCurrentScreen++;
                        }
                        else
                        {
                            mSnapingCurrentScreen--;
                        }
                    }
                    else
                    {
                        isSnapingToDestination = false;
                    }
                }
                break;
            }
        }
    }
       
   }
   
   /***********************add app widget to Screen and auto find a sutiable space to drop it END************************/

   
   boolean isOnstopCalled = false;
   public void forceInvalidView(final View view){
	   new Handler().postDelayed(new Runnable(){
		   public void run() {
//			   Log.d(TAG, "forceInvalidView view:"+view+" isOnstopCalled:"+isOnstopCalled);
			   if(!isOnstopCalled){
				   view.invalidate();
			   }
			   isOnstopCalled = false;
		   }
	   }, 1000);
   }
   
	public void postWaitedIntents() {
		Log.d(TAG, "postWaitedIntents mModel.waitedIntents:"+mModel.waitedIntents.size());
		synchronized(mModel.waitedIntents){
			final int size = mModel.waitedIntents.size();
			for(int i=0; i<size; i++){
				Intent intent = mModel.waitedIntents.get(i);
				mModel.onReceive(this, intent);
			}
			mModel.waitedIntents.clear();
		}
	}
	
}
