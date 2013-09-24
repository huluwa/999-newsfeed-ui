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

package com.borqs.omshome25;

import java.util.ArrayList;
import java.util.HashSet;

import com.borqs.omshome25.Launcher;
import com.borqs.omshome25.CellLayout.CellInfo;

import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.os.SystemProperties;

/**
 * The workspace is a wide area with a wallpaper and a finite number of screens. Each
 * screen contains a number of icons, folders or widgets the user can interact with.
 * A workspace is meant to be used with a fixed width only.
 */
public class Workspace extends ViewGroup implements DropTarget, DragSource, DragScroller {
    @SuppressWarnings({"UnusedDeclaration"})
    private static final String TAG = "oms2.5Launcher.Workspace";
    private static final int INVALID_SCREEN = -1;
    
    /**
     * The velocity at which a fling gesture will cause us to snap to the next screen
     */
    private static final int SNAP_VELOCITY = 200; //600;

    private final WallpaperManager mWallpaperManager;
    
    private boolean mFirstLayout = true;

    private int mCurrentScreen;
    private int mNextScreen = INVALID_SCREEN;
    private HomeScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int moveScale = 1;

    /**
     * CellInfo for the cell that is currently being dragged
     */
    private CellLayout.CellInfo mDragInfo;
    
    /**
     * Target drop area calculated during last acceptDrop call.
     */
    private int[] mTargetCell = null;

    private float mDownMotionX;
    private float mLastMotionX;
    private float mLastMotionY;
    
    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;

    private int mTouchState = TOUCH_STATE_REST;

    private OnLongClickListener mLongClickListener;

    private Launcher mLauncher;
    private IconCache mIconCache;
    private DragController mDragController;
    
    /**
     * Cache of vacant cells, used during drag events and invalidated as needed.
     */
    private CellLayout.CellInfo mVacantCache = null;
    
    private int[] mTempCell = new int[2];
    private int[] mTempEstimate = new int[2];

    private boolean mAllowLongPress = true;

    private int mTouchSlop;
    private int mMaximumVelocity;
    
    private static final int INVALID_POINTER = -1;

    private int mActivePointerId = INVALID_POINTER;
    
    private Drawable mPreviousIndicator;
    private Drawable mNextIndicator;
    
    public static final float NANOTIME_DIV = 1000000000.0f;
    public static final float SMOOTHING_SPEED = 0.75f;
    public static final float SMOOTHING_CONSTANT = (float) (0.016 / Math.log(SMOOTHING_SPEED));
    private float mSmoothingTime;
    private float mTouchX;

    protected static final String TAG_SCREEN_NUM = "com.borqs.omshome25.screen_num";
    public static int MAX_SCREEN_COUNT = 5;
    public static final int MIN_SCREEN_COUNT = 1;
    protected static final int DEFAULT_SCREEN_NUM = 3;
    public static final int DEFAULT_CURRENT_SCREEN = 1;
    private int mDefaultScreen;
    public int mScreenNum;  
    private Handler postHandler;
    private HandlerThread scrollThread;
    private Handler helpHandler;
    private static ArrayList<Bitmap> pageBitmaps ;
    private MyBounceInterpolator bounceInterpolator;
    private boolean NeedScrollForActionUP = true;
    public static int SLOP = 8;
    private final static int defaultSLOP = 8;
	private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
	private int mButtonBarHeight = 0;
	private float bitmapScale = 0.5f;

//    private WorkspaceOvershootInterpolator mScrollInterpolator;

    public static final float BASELINE_FLING_VELOCITY = 2500.f;
    public static final float FLING_VELOCITY_INFLUENCE = 0.4f;
    
    private PageIndicatorLineStyleView mLinePageIndicator ;
    private static boolean enableMoveFast=true;
    public static boolean use_oms_api = false;
    private static boolean home_workspace_more_invalidate = false;
    private static float   home_screen_move_speed=0.3f;
    private static boolean support_launcher_noinvalidate = true;
    private static float   pointer_count=-1.0f;//please set it as -1.0
    private boolean iamhdpi=false;
    private boolean memory_high_priority_smooth=false;

	static {
	    try{
	    	  MAX_SCREEN_COUNT = Integer.parseInt(SystemProperties.get("home_max_screen", "5"));
	    	  
	    	  if(MAX_SCREEN_COUNT > 10)
	    	  {
	    		  MAX_SCREEN_COUNT = 10;
	    	  }	    	  
	    	  else if(MAX_SCREEN_COUNT <1)
	    	  {
	    		  MAX_SCREEN_COUNT = 1;
	    	  }
	    	  
	    	  //default is true
	    	  //use_oms_api    = (SystemProperties.getInt("home_cache_workspace", 1) == 1);    	  
	    	  

	       }catch(Exception ne){
	           MAX_SCREEN_COUNT = 5;
	           use_oms_api      = false;
	          Log.d(TAG,"getProperty exception="+ne.getMessage());             
          }
   }
	
	private void initProperty()
	{
	    try{
           home_screen_move_speed = Float.valueOf(SystemProperties.get("home_screen_move_speed", "0.3"));
           
           if(home_screen_move_speed > 1.0f)
               home_screen_move_speed = 1.0f;
           
           if(home_screen_move_speed < 0.1)
               home_screen_move_speed = 0.1f;
           
        }catch(Exception ne)
        {
            Log.d(TAG,"getProperty exception="+ne.getMessage());
            home_screen_move_speed = 0.3f;
        }
        
        try{
            SLOP = Integer.parseInt(SystemProperties.get("home_touch_slot", "8"));
            if(SLOP>20){
                SLOP = 20;
            }
            else 
            if(SLOP<8){
                SLOP = 8;
            }
        }catch(Exception ne){
            Log.d(TAG,"getProperty exception="+ne.getMessage());
            SLOP = 8;
        }
        
        
        try{
            home_workspace_more_invalidate = (SystemProperties.getInt("home_workspace_more_invalidate", 1) == 1);
            support_launcher_noinvalidate = (SystemProperties.getInt("click_noinvalidate", 1) == 1);
            SHOW_DARW = (SystemProperties.getInt("home_enable_checkfps", 0) == 1);
            enableMoveFast = (SystemProperties.getInt("home_wallpaper_move", 0) == 0);
            
            pointer_count = SystemProperties.getInt("pointer_count", 9);
        }catch(Exception ne)
        {
            Log.d(TAG,"getProperty exception="+ne.getMessage());
            pointer_count = 9.0f;
        } 
	}
    
//    static class WorkspaceOvershootInterpolator implements Interpolator {
//        private static final float DEFAULT_TENSION = 1.3f;
//        private float mTension;
//
//        public WorkspaceOvershootInterpolator() {
//            mTension = DEFAULT_TENSION;
//        }
//        
//        public void setDistance(int distance) {
//            mTension = distance > 0 ? DEFAULT_TENSION / distance : DEFAULT_TENSION;
//        }
//
//        public void disableSettle() {
//            mTension = 0.f;
//        }
//
//        public float getInterpolation(float t) {
//            // _o(t) = t * t * ((tension + 1) * t + tension)
//            // o(t) = _o(t - 1) + 1
//            t -= 1.0f;
//            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
//        }
//    }
    
    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     * @param defStyle Unused.
     */
    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        
        final Resources res = context.getResources();
        mOrientation = res.getConfiguration().orientation;
        mButtonBarHeight = (int)res.getDimension(R.dimen.button_bar_height);
        
        mWallpaperManager = WallpaperManager.getInstance(context);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Workspace, defStyle, 0);
        mDefaultScreen = a.getInt(R.styleable.Workspace_defaultScreen, 1);
        a.recycle();

        setHapticFeedbackEnabled(false);

        bounceInterpolator = new MyBounceInterpolator();
        bounceInterpolator.setChoice(USED_FOR_DEFAULT);
        
        if('0' != Launcher.displayConfigs.charAt(Launcher.displayConfigs.length()-1-Launcher.DISPLAY_CONFIG_2D_ALLAPP_INDEX))
        {
        	mScroller = new HomeScroller(getContext());
        	moveScale = 1; //2;
        	bitmapScale = 0.5f;
        }
        else
        {
//        	mScroller = new HomeScroller(getContext(),null);
            mScroller = new HomeScroller(getContext(),bounceInterpolator);
            moveScale = 1;
            bitmapScale = 0.5f;
        }
        
        DisplayMetrics dm = res.getDisplayMetrics();
        
        if (dm.densityDpi < dm.DENSITY_HIGH) {
        	bitmapScale = (float)SystemProperties.getInt("3d_bitmap_scale", 1);
//        	Log.d(TAG, "bitmapScale" + bitmapScale);
        }
        
		pageBitmaps = new ArrayList<Bitmap>();
		
		try{
		    memory_high_priority_smooth = (SystemProperties.getInt("memory_high_priority_smooth", 0) == 1);
		}catch(Exception ne){}
		//use OMS performance API
		if(use_oms_api)
		{
                    /*
			if(memory_high_priority_smooth == false)
			{
		        setAlwaysDrawnWithCacheEnabled(true);		
		        setUseOpaqueDrawingCache(true);
			}
                    */
		}
		
		isDestroyed = false;
    }
    
    public static int USED_FOR_DEFAULT = 100;
    public static int USED_FOR_LONGDISTANCE = 0;
    public static int USED_FOR_SHORTDISTANCE_36 = 1;
    public static int USED_FOR_SHORTDISTANCE_18 = 2;
    public static int USED_FOR_SHORTDISTANCE_12 = 3;
    public static int USED_FOR_LASTSCREEN = 4;
    public static int USED_FOR_BACKTOCURRENTSCREEN = 5;
    static class MyBounceInterpolator implements android.view.animation.Interpolator
    {
	    public MyBounceInterpolator() {
	    }

	    private int choice = 0;
	    public void setChoice(int choiceFlag){
	    	choice = choiceFlag;
	    }
	    
	    public int getChoice(){
	    	return choice;
	    }
	    
	    public float getInterpolation(float t) 
	    {	       
	    	float newt = t;
	    	if(USED_FOR_LONGDISTANCE == choice) { //for long distance
		    	if (t <= 0.7f) { 
		    		newt = 1.05f - (105.0f/49.0f) * (t-0.7f) * (t-0.7f);
		    	} else {
		    		newt = 1.0f + (5.0f/9.0f)*(t - 1)*(t -1);
		    	}
	    	} else if(USED_FOR_SHORTDISTANCE_36 == choice){ //for short distance
	    		if (t <= 0.1f) { 
	    			newt = 36.0f * t;
	    		} else {
	    			newt = 26.0f/9.0f - (17.0f/9.0f)*t;
	    		}
	    	} else if(USED_FOR_SHORTDISTANCE_18 == choice){//for short distance
	    		if (t <= 0.1f) { 
	    			newt = 18.0f * t;
	    		} else {
	    			newt = 17.0f/9.0f - (8.0f/9.0f)*t;
	    		}
	    	} else if(USED_FOR_SHORTDISTANCE_12 == choice){
	    		if (t <= 0.1f) { 
	    			newt = 12.0f * t;
	    		} else {
	    			newt = 11.0f/9.0f - (2.0f/9.0f)*t;
	    		} 
	    	} else if(USED_FOR_LASTSCREEN == choice){
	    		
	    		 float mFactor = 2.f;
		    
	    		 if (mFactor == 1.0f) {
    				 newt = (float)(1.0f - (1.0f - t) * (1.0f - t));
    			 } else {
    				 newt = (float)(1.0f - Math.pow((1.0f - t), 2 * mFactor));
    			 }
	    	} else if(USED_FOR_BACKTOCURRENTSCREEN == choice){
	    		if (t <= 0.5f) { 
		    		newt = 1.05f - (4.2f) * (t-0.5f) * (t-0.5f);
		    	} else {
		    		newt = 1.0f + 0.2f*(t - 1)*(t -1);
		    	}
	    	}
	        
	        return newt;
       }
    }
    final int SCROLL_NEW  = 0;
    final int SCROLL_NEXT = 1;
    
    /**
     * Initializes various states for this workspace.
     */
    public void initWorkspace() {
        if(Launcher.LOGD)Log.d(TAG,"initWorkspace");        
        /* google scroller
        Context context = getContext();
        mScrollInterpolator = new WorkspaceOvershootInterpolator();
        mScroller = new Scroller(context, mScrollInterpolator);
        */
        
        mScreenNum = Launcher.getScreenCount() > 0 ? Launcher.getScreenCount() : DEFAULT_SCREEN_NUM;
        
        mDefaultScreen = mLauncher.default_page_index;
    	if(mDefaultScreen >= mScreenNum){
    		mDefaultScreen = mScreenNum/2;
			Launcher.setSettingsIntValue(getContext(), LauncherORM.default_page_index, mDefaultScreen);
		}	
        
        if(mLauncher.getIsNeedUseOldScreenIndex()){
        	mCurrentScreen = Launcher.getScreen();
        	mLauncher.setIsNeedUseOldScreenIndex(false);
        }else{
        	mCurrentScreen = mDefaultScreen;
        }
        
        Launcher.setScreen(mCurrentScreen);
        Launcher.setScreenCount(mScreenNum);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        
        initCellLayout();
    }
    
    private void initCellLayout(){
//    	if(Launcher.LOGD)Log.d(TAG, "initCellLayout");
        final int tmpCount = getChildCount();
        for(int j=0;j<tmpCount;j++){
        	CellLayout child = (CellLayout)getChildAt(j);
        	if(child != null) {
        		child.removeAllViews();
        	}
        }
        
        removeAllViews();
        
        final LayoutInflater mInflater = mLauncher.getLayoutInflater();
        
        for (int i = 0; i < mScreenNum; i++) {
            View view = mInflater.inflate(R.layout.workspace_screen, null);  
            view.setId(100 + i); //give cell layout an id.
            addView(view, i);
        }
    }

    //use for page exchange, the screen no need update.
    public void resetCellLayout(){
//    	if(Launcher.LOGD)Log.d(TAG, "resetCellLayout");
		 for(int j=0;j<this.getChildCount();j++){
		 	CellLayout child = (CellLayout)getChildAt(j);
		 	if(child != null) {
		 		child.reSetCellInfoScreen();
		 	}
		 }
    }
    
    public static ArrayList<Bitmap> getPageViews(){
        return pageBitmaps; 
    }
    
    public static void clearPageViews() {
//        if(Launcher.LOGD)Log.d(TAG, "clearPageViews");
        if(pageBitmaps != null) {
            for(int i=0;i<pageBitmaps.size();i++)
            {
                pageBitmaps.get(i).recycle();
            }
            pageBitmaps.clear();
        } 
    }
    
	/*public void createPageBitmaps(){
		if(Launcher.LOGD)Log.d(TAG,"createPageBitmaps start "+ System.currentTimeMillis());
		if(pageBitmaps != null) {
			pageBitmaps.clear();
			for(int i=0;i<getChildCount();++i){
				View view = getChildAt(i);
				if(null != view){
					view.setDrawingCacheEnabled(true);
					
					Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
					view.destroyDrawingCache(); 
					view.setDrawingCacheEnabled(false);
					
					pageBitmaps.add(bitmap); 
					bitmap = null;
				}
			}
			
			if(mLauncher != null) {
				Intent intent = new Intent(Launcher.INTENT_CREATE_BITMAP_OK);
				mLauncher.sendBroadcast(intent);
				if(Launcher.LOGD)Log.d(TAG,"after create bitmap successful send a broadcast message");
			}
	   }
	   if(Launcher.LOGD)Log.d(TAG,"createPageBitmaps exit  "+ System.currentTimeMillis());
	}*/

    //to save memory usage    
    public void createPageBitmaps()
    {
//        if(Launcher.LOGD)Log.d(TAG,"createPageBitmaps start "+System.currentTimeMillis());
        System.gc();
        
        if(pageBitmaps != null) {
            pageBitmaps.clear();
            CellLayout cell = (CellLayout)getChildAt(0); 
            int width = cell.getWidth();
            int height = cell.getHeight();
            
            for (int i = 0; i < getChildCount(); i++) {
        		cell = (CellLayout)getChildAt(i);
        		Bitmap bitmap = Bitmap.createBitmap((int) (width*bitmapScale), (int) (height*bitmapScale),
        				Bitmap.Config.ARGB_8888);
        		
        		Canvas c = new Canvas(bitmap);
        		//c.translate(-cell.getLeftPadding(), -cell.getTopPadding());
        		try{
        			c.scale(bitmapScale, bitmapScale);
            		cell.dispatchDraw(c);
            	}catch(Exception ex){
            		if(Launcher.LOGD)Log.d(TAG, ex.getMessage());
            	}
            	pageBitmaps.add(bitmap);            
            }
            
            if(mLauncher != null) {
                Intent intent = new Intent(Launcher.INTENT_CREATE_BITMAP_OK);
                mLauncher.sendBroadcast(intent);
//                if(Launcher.LOGD)Log.d(TAG,"after create bitmap successful send a broadcast message");
            }
       }
//       if(Launcher.LOGD)Log.d(TAG,"createPageBitmaps exit  " + System.currentTimeMillis());        
    }
    
    @Override
    public void addView(View child, int index, LayoutParams params) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, index, params);
    }

    @Override
    public void addView(View child) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, width, height);
    }

    @Override
    public void addView(View child, LayoutParams params) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, params);
    }

    /**
     * @return The open folder on the current screen, or null if there is none
     */
    Folder getOpenFolder() {
        CellLayout currentScreen = (CellLayout) getChildAt(mCurrentScreen);
        if(currentScreen != null) {
            int count = currentScreen.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = currentScreen.getChildAt(i);
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
                if (lp.cellHSpan == 4 && lp.cellVSpan == 4 && child instanceof Folder) {
                    return (Folder) child;
                }
            }
        }
        return null;
    }
    
    /**
     * @return The open folder on the current screen, or null if there is none
     */
    Folder getOpenFolder(int screen) {
        CellLayout currentScreen = (CellLayout) getChildAt(screen);
        if(currentScreen != null) {
            int count = currentScreen.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = currentScreen.getChildAt(i);
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
                if (lp.cellHSpan == 4 && lp.cellVSpan == 4 && child instanceof Folder) {
                    return (Folder) child;
                }
            }
        }
        return null;
    }
    
    
    Folder getOpenLiveFolder() {
        CellLayout currentScreen = (CellLayout) getChildAt(mCurrentScreen);
        if(currentScreen != null) {
            int count = currentScreen.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = currentScreen.getChildAt(i);
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
                if (lp.cellHSpan == 4 && lp.cellVSpan == 4 && child instanceof LiveFolder) {
                    return (Folder) child;
                }
            }
        }
        return null;
    }

    ArrayList<Folder> getOpenFolders() {
        final int screens = getChildCount();
        ArrayList<Folder> folders = new ArrayList<Folder>(screens);

        for (int screen = 0; screen < screens; screen++) {
            CellLayout currentScreen = (CellLayout) getChildAt(screen);
            if(currentScreen != null) {
                int count = currentScreen.getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = currentScreen.getChildAt(i);
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
                    if (lp.cellHSpan == 4 && lp.cellVSpan == 4 && child instanceof Folder) {
                        folders.add((Folder) child);
                        break;
                    }
                }
            }
        }

        return folders;
    }

    boolean isDefaultScreenShowing() {
        return mCurrentScreen == mDefaultScreen;
    }

    /**
     * Returns the index of the currently displayed screen.
     *
     * @return The index of the currently displayed screen.
     */
    int getCurrentScreen() {
        return mCurrentScreen;
    }

     /**
     * Returns how many screens there are.
     */
    int getScreenCount() {
        return getChildCount();
    }

    /**
     * Sets the current screen.
     *
     * @param currentScreen
     */
    public void setCurrentScreen(int currentScreen) {
        if (!mScroller.isFinished()) mScroller.abortAnimation();
        clearVacantCache();
        mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
//        if(Launcher.LOGD)Log.d(TAG,"setCurrentScreen: "+mCurrentScreen);
        if(mLauncher.dockStyle==1){
        	mPreviousIndicator.setLevel(mCurrentScreen);
        	mNextIndicator.setLevel(mCurrentScreen);
        }
        scrollTo(mCurrentScreen * getWidth(), 0);
        updateWallpaperOffset();
        invalidate();
    }

    /**
     * Adds the specified child in the current screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     */
    void addInCurrentScreen(View child, int x, int y, int spanX, int spanY) {
        addInScreen(child, mCurrentScreen, x, y, spanX, spanY, false);
    }

    /**
     * Adds the specified child in the current screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the children list.
     */
    void addInCurrentScreen(View child, int x, int y, int spanX, int spanY, boolean insert) {
        addInScreen(child, mCurrentScreen, x, y, spanX, spanY, insert);
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     */
    void addInScreen(View child, int screen, int x, int y, int spanX, int spanY) {
        addInScreen(child, screen, x, y, spanX, spanY, false);
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the children list.
     */
    void addInScreen(View child, int screen, int x, int y, int spanX, int spanY, boolean insert) {
//        if(Launcher.LOGD)Log.d(TAG,"addInScreen screen:"+screen+" view:"+child);
        if (screen < 0 || screen >= getChildCount()) {
            Log.e(TAG, "The screen must be >= 0 and < " + getChildCount()
                + " (was " + screen + "); skipping child");
            return;
        }

        clearVacantCache();

        final CellLayout group = (CellLayout) getChildAt(screen);
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
        } else {
            lp.cellX = x;
            lp.cellY = y;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
        }
        group.addView(child, insert ? 0 : -1, lp);
        if (!(child instanceof Folder)) {
            child.setHapticFeedbackEnabled(false);
            child.setOnLongClickListener(mLongClickListener);
        }
        if (child instanceof DropTarget) {
            mDragController.addDropTarget((DropTarget)child);
        }
    }

//    void addWidget(View view, Widget widget) {
//        addInScreen(view, widget.screen, widget.cellX, widget.cellY, widget.spanX,
//                widget.spanY, false);
//    }
//
//    void addWidget(View view, Widget widget, boolean insert) {
//        addInScreen(view, widget.screen, widget.cellX, widget.cellY, widget.spanX,
//                widget.spanY, insert);
//    }
 
    CellLayout.CellInfo findAllVacantCells(boolean[] occupied) {
        CellLayout group = (CellLayout) getChildAt(mCurrentScreen);
        if (group != null) {
            return group.findAllVacantCells(occupied, null);
        }
        return null;
    }

    public void clearVacantCache() {
        if (mVacantCache != null) {
            mVacantCache.clearVacantCells();
            mVacantCache = null;
        }
    }

    /**
     * Registers the specified listener on each screen contained in this workspace.
     *
     * @param l The listener used to respond to long clicks.
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClickListener = l;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).setOnLongClickListener(l);
        }
    }

    public void updateWallpaperOffset() {
        updateWallpaperOffset(getChildAt(getChildCount() - 1).getRight() - (mRight - mLeft));
    }

    private void updateWallpaperOffset(int scrollRange) {
        IBinder token = getWindowToken();
        if (token != null) {
             if(scrollRange != 0 &&  getChildCount() != 1)
	        {	 
                  mWallpaperManager.setWallpaperOffsetSteps(1.0f / (getChildCount() - 1), 0 );
                  mWallpaperManager.setWallpaperOffsets(getWindowToken(),
                    Math.max(0.f, Math.min(mScrollX/(float)scrollRange, 1.f)), 0);
             }
        }
    }
    
    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        mTouchX = x;
        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
    }
    
    static long timedraw = 0; 
    boolean isScrolling = false;
    @Override
    public void computeScroll() {
        if(pointer_count == -1.0)
        {
            initProperty();
        }
        
    	long now = System.currentTimeMillis();
//    	long spend = now - timedraw;
    	timedraw = now;
    	
        if (mScroller.computeScrollOffset()) {            
        	mTouchX = mScrollX = mScroller.getCurrX();
            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
            mScrollY = mScroller.getCurrY();
            if(pressedHome == true)
            {
                updateWallpaperOffset();
            }
            else
            {
                if(isChangedScreen() || noItemsInCurrentScreen())
                {
                	if(NeedScrollForActionUP == true )
                        updateWallpaperOffset();
                } 
            }
            
           // updateWallpaperOffset();
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
            mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
            if(mLauncher.dockStyle==1){
	            mPreviousIndicator.setLevel(mCurrentScreen);
	            mNextIndicator.setLevel(mCurrentScreen);
            }
            Launcher.setScreen(mCurrentScreen);
            mNextScreen = INVALID_SCREEN;
            clearChildrenCache();
            updateWallpaperOffset();
            reStartWidgetAnimation();
            
            if(isNeedForceSetFocusFlag){
//                Log.d(TAG,"entering forceSetViewFocus");
                forceSetViewFocus();
            }
            
            //TODO if mDragLayer isDragging need find sutiable slot
          if(mLauncher.isNewDraging)
          {
              if(mLauncher.findSlot(mLauncher.newDragCellInfo,mLauncher.newDragxy, mLauncher.newDragSpan[0], mLauncher.newDragSpan[1],true))
              {
                  //Make Toast info
                  if(mLauncher.mToast ==null)
                  {
                      mLauncher.mToast = Toast.makeText(mLauncher.getApplicationContext(),mLauncher.getString(R.string.in_of_space) , 400);
                  }
                  else
                  {
                      mLauncher.mToast.setText(mLauncher.getString(R.string.in_of_space));
                  }
                  
                  mLauncher.mToast.show();
              }
              else if(mLauncher.isSnapingToDestination == true)
              {
                  // snapToNextScreen or snapToPreScreen
                  Message msg = mLauncher.mLauncherLauncher.obtainMessage(Launcher.SNAP_TO_SCREEN);
                  mLauncher.mLauncherLauncher.sendMessageDelayed(msg, 300);
              }
          }         
           
        } else if (mTouchState == TOUCH_STATE_SCROLLING) {
            final float noww = System.nanoTime() / NANOTIME_DIV;
            final float e = (float) Math.exp((noww - mSmoothingTime) / SMOOTHING_CONSTANT);
            final float dx = mTouchX - mScrollX;
            mScrollX += (pointer_count * dx * e)/12.0;            
            mSmoothingTime = noww;
            // Keep generating points as long as we're more than 1px away from the target
            if (dx > 1.f || dx < -1.f) {            	
            	
            	if(home_workspace_more_invalidate)
            	{
	                if(noItemsInCurrentScreen())
	                {
	                	updateWallpaperOffset();
	                }                             
	                postInvalidate();
            	}
                
            }
        }
    }

    boolean needrecord =false;
    int     count = 1;
    long alltime  = 0;
    long lastDrawtime=0;    
    boolean SHOW_DARW = false;
    boolean CHECK_MAX_DRAW_CAPABILITY = false;
    @Override
    protected void dispatchDraw(Canvas canvas) {
    	long pre = System.currentTimeMillis();
        long span = (pre-lastDrawtime);
        if(span < 200)
        {
            if(alltime > 0)
               count++;
            alltime += span;
        }
        lastDrawtime = pre;

        boolean restore = false;
        int restoreCount = 0;    
        
        int drawCount = 0;

        // ViewGroup.dispatchDraw() supports many features we don't need:
        // clip to padding, layout animation, animation listener, disappearing
        // children, etc. The following implementation attempts to fast-track
        // the drawing dispatch by drawing only what we know needs to be drawn.

        boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING && mNextScreen == INVALID_SCREEN;
        // If we are not scrolling or flinging, draw only the current screen
        if (fastDraw) {
        	if(mCurrentScreen >= 0 && mCurrentScreen <  getChildCount()){
        		drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());
        		drawCount++;
        	}
        } else {
        	boolean isNeedDrawAll = false;
            final long drawingTime = getDrawingTime();
            final float scrollPos = (float) mScrollX / getWidth();
            final int leftScreen = (int) scrollPos;
            final int rightScreen = leftScreen + 1;
            if (leftScreen >= 0 && leftScreen < getChildCount()) {
                drawChild(canvas, getChildAt(leftScreen), drawingTime);
                drawCount++;
            }else{
                isNeedDrawAll = true;
            }
            
            if(isNeedDrawAll){
            	Log.d(TAG, "dispatchDraw oops! draw last screen. leftScreen:"+leftScreen+" screenCount:"+getChildCount());
            	for (int i = 0; i < getChildCount(); i++) 
            	{
            		drawChild(canvas, getChildAt(i), drawingTime);
            		drawCount++;
            	}
            }else if (scrollPos != leftScreen && rightScreen < getChildCount() && rightScreen >= 0) {
                drawChild(canvas, getChildAt(rightScreen), drawingTime);
                drawCount++;
            }
        }

        if (restore) {
            canvas.restoreToCount(restoreCount);
        }
        
        long drawtime = (System.currentTimeMillis()-pre);
        if(Launcher.LOGD && SHOW_DARW)
        {
        	Log.d(TAG, "dispatchDraw time:"+ drawtime + " span="+span + " average time="+alltime/count + "\npage="+drawCount);
        }

        if(CHECK_MAX_DRAW_CAPABILITY)
        {
            mLauncher.mCallStateHandler.obtainMessage(0x101).sendToTarget();
            mLauncher.finishButton.setText("" + drawtime +"\n"+span + "\n"+alltime/count + "\n"+drawCount+"\n"+count);
        }
        
    }

    private float mScale = 1.0f;
    public void setScale(float scale) {
        mScale = scale;
        invalidate();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        computeScroll();
        mDragController.setWindowToken(getWindowToken());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        // The children are given the same width and height as the workspace
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }


        if (mFirstLayout) {
            setHorizontalScrollBarEnabled(false);
            scrollTo(mCurrentScreen * width, 0);
            setHorizontalScrollBarEnabled(true);
            updateWallpaperOffset(width * (getChildCount() - 1));
            mFirstLayout = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
//    	if(Launcher.LOGD)Log.d(TAG,"requestChildRectangleOnScreen view:"+child);
        int screen = indexOfChild(child);
        if (screen != mCurrentScreen || !mScroller.isFinished()) {
            if (!mLauncher.isWorkspaceLocked()) {
                snapToScreen(screen);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
//    	if(Launcher.LOGD)Log.d(TAG, "onRequestFocusInDescendants  direction:"+direction+" previouslyFocusedRect:"+previouslyFocusedRect);
        if (!mLauncher.isAllAppsVisible()) {
            final Folder openFolder = getOpenFolder();
            if (openFolder != null) {
                return openFolder.requestFocus(direction, previouslyFocusedRect);
            } else {
                int focusableScreen;
                if (mNextScreen != INVALID_SCREEN) {
                    focusableScreen = mNextScreen;
                } else {
                    focusableScreen = mCurrentScreen;
                }
				if(null != getChildAt(focusableScreen)) {
					getChildAt(focusableScreen).requestFocus(direction, previouslyFocusedRect);
				}
		    }
        }
        return false;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
//   	Log.d(TAG, "dispatchUnhandledMove   focused:"+focused+"  direction:"+direction);
        if (direction == View.FOCUS_LEFT) {
            if (getCurrentScreen() > 0) {
                snapToScreen(getCurrentScreen() - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (getCurrentScreen() < getChildCount() - 1) {
                snapToScreen(getCurrentScreen() + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (!mLauncher.isAllAppsVisible()) {
            final Folder openFolder = getOpenFolder();
            if (openFolder == null) {
                getChildAt(mCurrentScreen).addFocusables(views, direction);
                if (direction == View.FOCUS_LEFT) {
                    if (mCurrentScreen > 0) {
                        getChildAt(mCurrentScreen - 1).addFocusables(views, direction);
                    }
                } else if (direction == View.FOCUS_RIGHT){
                    if (mCurrentScreen < getChildCount() - 1) {
                        getChildAt(mCurrentScreen + 1).addFocusables(views, direction);
                    }
                }
            } else {
                openFolder.addFocusables(views, direction);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mLauncher.isWorkspaceLocked() || mLauncher.isAllAppsVisible()) {
                return false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean skipMotion = false;
    public void skipMotion(){
       skipMotion = true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent ev:"+ev.getAction()+" width:"+this.getWidth()+" measuredWidth:"+this.getMeasuredWidth());
        final boolean workspaceLocked = mLauncher.isWorkspaceLocked();
        final boolean allAppsVisible = mLauncher.isAllAppsVisible();
        if (workspaceLocked || allAppsVisible) {
//            if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent return false   ---------  workspaceLocked || allAppsVisible");
            return false; // We don't want the events.  Let them fall through to the all apps view.
        }

        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onTouchEvent will be called and we do the actual
         * scrolling there.
         */

        /*
         * Shortcut the most recurring case: the user is in the dragging
         * state and he is moving his finger.  We want to intercept this
         * motion.
         */
        final int action = ev.getAction();
        if(action == MotionEvent.ACTION_MOVE && skipMotion){
//            if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent return false   ---------  skipMotion && ACTION_MOVE");
            return false;
        }
        
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
//            if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent return true   ---------  !TOUCH_STATE_REST && ACTION_MOVE");
            return true;
        }

        pressedHome = false;

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        
        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                 * Locally do absolute value. mLastMotionX is set to the y value
                 * of the down event.
                 */
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX();
                final float y = ev.getY();
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);
                int moveSlop = SLOP;
                final int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > moveSlop;
                boolean yMoved = yDiff > moveSlop;

                CellInfo cellInfo = (CellInfo)getChildAt(this.getCurrentScreen()).getTag();
                if(cellInfo!=null){
                	if(cellInfo.spanX == 4 && cellInfo.spanY == 4 && yDiff > (0.577f * xDiff)){
                		xMoved = false;
                	}
                }                 

                if (xMoved || yMoved) {
                    
                    if (xMoved) {
                        // Scroll if the user moved far enough along the X axis
                        mTouchState = TOUCH_STATE_SCROLLING;
                        //when move, record the current postion as first pos
                        mDownMotionX = x;
                        mLastMotionX = x;
                        mLastMotionY = y;
                        mTouchX = mScrollX;
                        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                        pauseWidgetAnimation();
                        enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
                        increasePriority();
                        alltime = 0;
                        count = 1;
 
                    }
                    // Either way, cancel any pending longpress
                    if (mAllowLongPress) {
                        mAllowLongPress = false;
                        // Try canceling the long press. It could also have been scheduled
                        // by a distant descendant, so use the mAllowLongPress flag to block
                        // everything
                        final View currentScreen = getChildAt(mCurrentScreen);
                        currentScreen.cancelLongPress();
                    }
                }else{
//                	Log.d(TAG, "onInterceptTouchEvent xMoved == false, no move");
                }
                
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                // Remember location of down touch
            	mDownMotionX = x;
                mLastMotionX = x;
                mLastMotionY = y;
                mActivePointerId = ev.getPointerId(0);
                mAllowLongPress = true;
                NeedScrollForActionUP = true;                
                
                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't.  mScroller.isFinished should be false when
                 * being flinged.
                 */
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                //if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent ACTION_DOWN, mLastMotionX:"+x+" mLastMotionY:"+y);
                
                if(mLauncher.isNewDraging)
                {
                    return true;
                }
                
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            	final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();
                mVelocityTracker.clear();
                
                if(skipMotion == false){
                    if(SLOP <= defaultSLOP)
                    {
                        //SLOP<=8 we think the screen is electric capacity type touch screen
                        //else we think screen is electric resistance type touch screen
                        //for electric resistance type touch screen, don't considerate the easy-click case 
                        //for tickect:107048
                	if(Math.abs(velocityX) > 200 && null != mDragController && !mDragController.isDragging())
                	{            
                		if(mCurrentScreen > 0 && velocityX > 200)
                		{
                            increasePriority();
                			snapToScreen(mCurrentScreen - 1, velocityX, false);                 
                		}
                		
                		if(mCurrentScreen < getChildCount() - 1 && velocityX < -200)
                		{
                                increasePriority();
                			snapToScreen(mCurrentScreen + 1, velocityX,false);                
                		}	                
                		mLauncher.ignoreClick = true;	 
                		
//                          if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent velocityX ="+velocityX);
                    }
                }  	
                	
                if (mTouchState != TOUCH_STATE_SCROLLING) {
                    final CellLayout currentScreen = (CellLayout)getChildAt(mCurrentScreen);
                    if (!currentScreen.lastDownOnOccupiedCell()) {
                        getLocationOnScreen(mTempCell);
                        // Send a tap to the wallpaper if the last down was on empty space
                        final int pointerIndex = ev.findPointerIndex(0);
                        mWallpaperManager.sendWallpaperCommand(getWindowToken(), 
                                "android.wallpaper.tap",
                                mTempCell[0] + (int) ev.getX(pointerIndex),
                                mTempCell[1] + (int) ev.getY(pointerIndex), 0, null);
                    }
                }
              }else{
                	//SKIP
                	skipMotion = false;
                	//SKIP
                }
                
                // Release the drag
                clearChildrenCache();
                mTouchState = TOUCH_STATE_REST;
                mActivePointerId = INVALID_POINTER;
                mAllowLongPress = false;
                
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                break;
                
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        //if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent return "+  String.valueOf(mTouchState != TOUCH_STATE_REST));
        return mTouchState != TOUCH_STATE_REST;
    }
    
    private void onSecondaryPointerUp(MotionEvent ev) {
    }

    /**
     * If one of our descendant views decides that it could be focused now, only
     * pass that along if it's on the current screen.
     *
     * This happens when live folders requery, and if they're off screen, they
     * end up calling requestFocus, which pulls it on screen.
     */
    @Override
    public void focusableViewAvailable(View focused) {
        View current = getChildAt(mCurrentScreen);
        View v = focused;
        while (true) {
            if (v == current) {
                super.focusableViewAvailable(focused);
                return;
            }
            if (v == this) {
                return;
            }
            ViewParent parent = v.getParent();
            if (parent instanceof View) {
                v = (View)v.getParent();
            } else {
                return;
            }
        }
    }

    int oldTID;
    boolean setPriority = false;
    public void increasePriority()
    {
    	if(setPriority)
    	{
	    	try{
	         	oldTID = Process.getThreadPriority(Process.myTid());
	         	if(oldTID != Process.THREAD_PRIORITY_URGENT_DISPLAY)
	         	{
		            //android.os.Process.setThreadPriority(Process.);
		            android.os.Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_URGENT_DISPLAY);
		             
//		            int another = Process.getThreadPriority(Process.myTid());             
//		 	        Log.d(TAG, "increase Priority tid =" + Process.myTid()+ " old priority="+oldTID + " new="+another);
	         	}
	        }catch(Exception ne)
	        {
	        	Log.d(TAG, "increasePriority  ne:"+ne.getMessage());
	        }
    	}
    }
    
    void enableChildrenCache(int fromScreen, int toScreen) {
//    	long pre = System.currentTimeMillis();
        if (fromScreen > toScreen) {
            final int temp = fromScreen;
            fromScreen = toScreen;
            toScreen = temp;
        }
        
        //OMS performance API
        //enable cell cache
        if(use_oms_api && memory_high_priority_smooth == false)
        {
	        setChildrenDrawnWithCacheEnabled(true);
	        setChildrenDrawingCacheEnabled(true);
	        
	        final int count = getChildCount();
	        for (int i = 0; i < count; i++) {
	            final View view = getChildAt(i);
	            view.setDrawingCacheEnabled(true);
	            // Update the drawing caches
	            //view.buildDrawingCache(true);
	        }
        }//end OMS performance API        
        else        
        {
	        final int count = getChildCount();
	        
	        fromScreen = Math.max(fromScreen, 0);
	        toScreen = Math.min(toScreen, count - 1);
	        
	        for (int i = fromScreen; i <= toScreen; i++) {
	            final CellLayout layout = (CellLayout) getChildAt(i);
	            layout.setChildrenDrawnWithCacheEnabled(true);
	            layout.setChildrenDrawingCacheEnabled(true);
	        }
        }
        
//        if(Launcher.LOGD)Log.d(TAG, "enableChildrenCache time:"+(System.currentTimeMillis()-pre));
    }
    
    void clearChildrenCache() {
    	if(use_oms_api && memory_high_priority_smooth == false)
    	{
    	    //no cache in workspace
    	    setChildrenDrawnWithCacheEnabled(false);
    	}
    	
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final CellLayout layout = (CellLayout) getChildAt(i);
			layout.setChildrenDrawnWithCacheEnabled(false);
		}		

		if(setPriority)
    	{
		    try{
//		    	int oldTIDTmp = Process.getThreadPriority(Process.myTid());	    	
		    	android.os.Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_DEFAULT);	    	
//		    	int newTIDTmp = Process.getThreadPriority(Process.myTid());
//		    	if(Launcher.LOGD)Log.d(TAG, "decrease Priority current tid =" + Process.myTid() + " old priority=" + oldTIDTmp+ " priority="+newTIDTmp);        	         
	        }catch(Exception ne)
	        {
	        	if(Launcher.LOGD)Log.d(TAG, ne.getMessage(), ne);
	        }
    	}
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        
//        if (mLauncher.isWorkspaceLocked()) {
//            return false; // We don't want the events.  Let them fall through to the all apps view.
//        }
//        if (mLauncher.isAllAppsVisible()) {
//            // Cancel any scrolling that is in progress.
//            if (!mScroller.isFinished()) {
//                mScroller.abortAnimation();
//            }
//            snapToScreen(mCurrentScreen);
//            return false; // We don't want the events.  Let them fall through to the all apps view.
//        }
//
//
//        final int action = ev.getAction();
//        
//        if (mVelocityTracker == null) {
//            mVelocityTracker = VelocityTracker.obtain();
//        }
//        mVelocityTracker.addMovement(ev);
//
//        switch (action & MotionEvent.ACTION_MASK) {
//        case MotionEvent.ACTION_DOWN:
//            /*
//             * If being flinged and user touches, stop the fling. isFinished
//             * will be false if being flinged.
//             */
//            if (!mScroller.isFinished()) {
//                mScroller.abortAnimation();
//            }
//
//            // Remember where the motion event started
//            mLastMotionX = ev.getX();
//            mLastMotionY =  ev.getY();
//            mDownMotionX =  ev.getX();
//            mActivePointerId = ev.getPointerId(0);
//            if (mTouchState == TOUCH_STATE_SCROLLING) {
//                enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
//            }
//            if(Launcher.LOGD)Log.d(TAG,"onTouchEvent ACTION_DOWN, mLastMotionX:"+mLastMotionX);
//            break;
//        case MotionEvent.ACTION_MOVE:
//        	final int pointerIndex = ev.findPointerIndex(mActivePointerId);
//        	if(mTouchState != TOUCH_STATE_SCROLLING){        		
//        		final int xDiff = (int) Math.abs(ev.getX(pointerIndex) - mLastMotionX);
//        		final int yDiff = (int) Math.abs(ev.getY(pointerIndex) - mLastMotionY);
//                int moveSlop = SLOP;
// 
//        		boolean xMoved = xDiff > moveSlop;
//        		CellInfo cellInfo = (CellInfo)getChildAt(this.getCurrentScreen()).getTag();
//        		if(cellInfo!=null){
//        			if(cellInfo.spanX == 4 && cellInfo.spanY == 4  && yDiff > (0.577f * xDiff)){
//        				xMoved = false;
//        			}
//        		}
//        		
//        		if (xMoved) {
//        			mTouchState = TOUCH_STATE_SCROLLING ;
//                                alltime = 0;
//                                count = 1;
//
//        			
//        		    mTouchX = mScrollX;
//                    mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
//                    
//                    pauseWidgetAnimation();
//        			enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
//        			increasePriority();
//        		}
//        	}
//        	
//            if (mTouchState == TOUCH_STATE_SCROLLING) {
//                // Scroll to follow the motion event
//                final float x = ev.getX(pointerIndex);
//                final float deltaX = mLastMotionX - x;
//                //must use float to decrease the loose point
//                float deltaOrignal = (mDownMotionX - x);
//                mLastMotionX = x;
//
//                if (deltaX < 0) {
//                    if (mScrollX > 0) {
//	                    if (mTouchX > 0) {
//	                        mTouchX += Math.max(-mTouchX, deltaX);
//	                        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
//	                        invalidate();
//	                            
//	                    }
//	                    mLastMotionX = x;
//	                }
//                    else if(mScrollX <=0)
//                    {
//                    	NeedScrollForActionUP = false;
//                    	scrollToWithoutWallPaperMove((int)(deltaOrignal/1.75), false);
//                    }
//                } else if (deltaX > 0) {
//
//                    final float availableToScroll = getChildAt(getChildCount() - 1).getRight() -
//                            mTouchX - getWidth();
//                    if (availableToScroll > 0) {
//                        mTouchX += Math.min(availableToScroll, deltaX);
//                        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
//                        invalidate();
//                    }
//                    else if(availableToScroll <=0)
//                    {
//                    	NeedScrollForActionUP = false;
//                    	scrollToWithoutWallPaperMove(mCurrentScreen*getWidth() + (int)(Math.abs((int)deltaOrignal)/1.75), true);
//                    	
//                    	//add one screen automatic
//                    	/*
//                    	final CellLayout cellView = (CellLayout)getChildAt(mCurrentScreen);
//                        if(cellView.getChildCount()  > 0 && getChildCount()<MAX_SCREEN_COUNT && deltaOrignal > getWidth()/2)
//                        {
//                            mTouchState = TOUCH_STATE_REST;      
//                        	mVelocityTracker.clear();
//                        	mLauncher.addPage();
//                        	snapToScreenWithVelocityX(mCurrentScreen + 1, 300);
//                        	mCurrentScreen = mCurrentScreen+1;
//                        }*/
//                    }
//                    
//                } else {
//                    awakenScrollBars();
//                }
//            }
//            break;
//        case MotionEvent.ACTION_UP:
//            if (mTouchState == TOUCH_STATE_SCROLLING) {
//                final VelocityTracker velocityTracker = mVelocityTracker;
//                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
//                final int velocityX = (int) velocityTracker.getXVelocity(mActivePointerId);
//                
//                alltime = 0;
//                count = 1;
//                increasePriority();
//				
//                final int screenWidth = getWidth();
//                final int whichScreen = (mScrollX + (screenWidth / 2)) / screenWidth;
//                final float scrolledPos = (float) mScrollX / screenWidth;
//                
//                if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
//                    // Fling hard enough to move left.
//                    // Don't fling across more than one screen at a time.
//                    final int bound = scrolledPos < whichScreen ?
//                            mCurrentScreen - 1 : mCurrentScreen;
//                    snapToScreen(Math.min(whichScreen, bound), velocityX, true);
//                } else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
//                    // Fling hard enough to move right
//                    // Don't fling across more than one screen at a time.
//                    final int bound = scrolledPos > whichScreen ?
//                            mCurrentScreen + 1 : mCurrentScreen;
//                    snapToScreen(Math.max(whichScreen, bound), velocityX, true);
//                } else {
//                    snapToDestination(velocityX);
//                }
//
//                if (mVelocityTracker != null) {
//                    mVelocityTracker.recycle();
//                    mVelocityTracker = null;
//                }
//            }
//            else
//            {
//                mLauncher.endNewDrag(ev.getX(),ev.getY());
//            }
//            mTouchState = TOUCH_STATE_REST;
//            mActivePointerId = INVALID_POINTER;
//            mDragController.endDragForce();
//            break;
//        case MotionEvent.ACTION_CANCEL:
//            
//            if(mTouchState != TOUCH_STATE_REST)
//            {
//                mLauncher.endNewDrag(ev.getX(),ev.getY()); 
//            }
//            mTouchState = TOUCH_STATE_REST;
//            if(null != mVelocityTracker){
//            	mVelocityTracker.clear();
//            }
//            //force to end the drag
//            mDragController.endDragForce();
//            mActivePointerId = INVALID_POINTER;
//            break;
//        case MotionEvent.ACTION_POINTER_UP:
//            onSecondaryPointerUp(ev);
//            break;
//        }
//
//        return true;
//    }

    
    int preDelta  = 0; 
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //if(Launcher.LOGD)Log.d(TAG,"onTouchEvent ev:"+ev.getAction());
        if(pointer_count == -1.0f)
        {
            initProperty();
        }
        
        if (mLauncher.isWorkspaceLocked()) {
            return false; // We don't want the events.  Let them fall through to the all apps view.
        }
        if (mLauncher.isAllAppsVisible()) {
            // Cancel any scrolling that is in progress.
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            snapToScreen(mCurrentScreen);
            return false; // We don't want the events.  Let them fall through to the all apps view.
        }


        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();
        
        if (mVelocityTracker == null) {
        	mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
       
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            /*
             * If being flinged and user touches, stop the fling. isFinished
             * will be false if being flinged.
             */
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
       

            // Remember where the motion event started
            mLastMotionX = x;
            mLastMotionY = y;
            mDownMotionX = x;
            mActivePointerId = ev.getPointerId(0);
            
//            if(Launcher.LOGD)Log.d(TAG,"onTouchEvent ACTION_DOWN, mLastMotionX:"+mLastMotionX);
            break;
        case MotionEvent.ACTION_MOVE:
        	final int pointerIndex = ev.findPointerIndex(mActivePointerId);
            //final float xx = ev.getX(pointerIndex);
            final float xx = x;
        	if(mTouchState != TOUCH_STATE_SCROLLING){        		
        		final int xDiff = (int) Math.abs(xx - mLastMotionX);
        		final int yDiff = (int) Math.abs(y - mLastMotionY);
                int moveSlop = SLOP;
 
        		boolean xMoved = xDiff > moveSlop;
        		CellInfo cellInfo = (CellInfo)getChildAt(this.getCurrentScreen()).getTag();
        		if(cellInfo!=null){
        			if(cellInfo.spanX == 4 && cellInfo.spanY == 4  && yDiff > (0.577f * xDiff)){
        				xMoved = false;
        			}
        		}
        		
        		if (xMoved) {
        			mTouchState = TOUCH_STATE_SCROLLING ;
                    alltime = 0;
                    count = 1;
        			
        		    mTouchX = mScrollX;
                    mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                    
                    pauseWidgetAnimation();
        			enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
        			increasePriority();
        		}
        	}
        	
        	if (mTouchState == TOUCH_STATE_SCROLLING) {
                // Scroll to follow the motion event
                int deltaX = (int) (mLastMotionX - xx);               
                
                //must use float to decrease the loose point
                float deltaOrignal = (mDownMotionX - xx);
                
                if(preDelta ==0)
                	preDelta = deltaX;

                if (deltaX < 0) {
                    if (mScrollX > 0) {
                    	//change the move direction
                	    if (mTouchX > 0) {
                            mTouchX += Math.max(-mTouchX, deltaX);
                            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                            invalidate();
                        }
                        mLastMotionX = xx;
                    }
                    else if(mScrollX <=0)
                    {
                    	NeedScrollForActionUP = false;
                    	scrollToWithoutWallPaperMove((int)(deltaOrignal/1.75), false);
                    }
                } else if (deltaX > 0) {
                    final int availableToScroll = getChildAt(getChildCount() - 1).getRight() -
                            mScrollX - getWidth();
                    if (availableToScroll > 0) {
                    	mTouchX += Math.min(availableToScroll, deltaX);
                        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                        invalidate();
                        mLastMotionX = xx;
                    }
                    else if(availableToScroll <=0)
                    {
                    	NeedScrollForActionUP = false;
                    	scrollToWithoutWallPaperMove(mCurrentScreen*getWidth() + (int)(Math.abs((int)deltaOrignal)/1.75), true);
                    	
                    	//add one screen automatic       
                    	if(false/*SystemProperties.getInt("home_enable_auto_add_page", 0) == 1*/)
                    	{
	                    	final CellLayout cellView = (CellLayout)getChildAt(mCurrentScreen);
	                        if(cellView.getChildCount()  > 0 && getChildCount()<MAX_SCREEN_COUNT && deltaOrignal > getWidth()/2)
	                        {
	                            mTouchState = TOUCH_STATE_REST;      
	                        	mVelocityTracker.clear();
	                        	mLauncher.addPage();
	                        	snapToScreenWithVelocityX(mCurrentScreen + 1, 300);
	                        	mCurrentScreen = mCurrentScreen+1;
	                        }
                    	}
                    }
                } else {
                    awakenScrollBars();
                }
                
                if(mLinePageIndicator != null)
                {
	                if(mLauncher.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
	                    mLinePageIndicator.movePosition(Math.round((1.0f*mLinePageIndicator.getWidth())/ getChildCount()), mLinePageIndicator.getHeight(), deltaOrignal, mLauncher.screenWidth, mLauncher.screenHeight, mCurrentScreen, getChildCount());
	                else if(mLauncher.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
	                	mLinePageIndicator.movePosition(mLinePageIndicator.getWidth(), Math.round((1.0f*mLinePageIndicator.getHeight())/ getChildCount()), deltaOrignal, mLauncher.screenWidth, mLauncher.screenHeight, mCurrentScreen, getChildCount());
                }
                 
      //          if(Launcher.LOGD)Log.d(TAG,"onTouchEvent ACTION_MOVE, mScrollX="+mScrollX + " deltaX:+"+deltaX+" mLastMotionX:"+mLastMotionX);
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();
                mVelocityTracker.clear();

                alltime = 0;
                count = 1;
                increasePriority();
                int span  = (int)(x - mLastMotionX);
                float deltaOrignal = (mDownMotionX - x);
            	if(mCurrentScreen > 0 && velocityX > 200)
            	{
            		increasePriority();
           // 		if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX  enter 1111");
            		 if(-100 <= deltaOrignal && deltaOrignal <= 0){
                     	snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen - 1, velocityX);
                     }else{
                    	 snapToScreenWithVelocityX(mCurrentScreen - 1, velocityX);
                     }
            	}
            	else if(mCurrentScreen > 0 && span > (getWidth()/10))
            	{
            		increasePriority();
    //        		if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX  enter 2222 deltaOrignal:"+deltaOrignal);
            		if(-100 <= deltaOrignal && deltaOrignal <= 0){
                     	snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen - 1, velocityX);
                     }else{
                    	snapToScreenWithVelocityX(mCurrentScreen - 1, velocityX);
                     }
            	}	
            	else if(mCurrentScreen < getChildCount() - 1 && velocityX < -200)
            	{
            		increasePriority();
            	//	if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX  enter 3333");
            		if(0 <= deltaOrignal && deltaOrignal <= 100){
                    	snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen + 1, velocityX);
                    }else{
                    	snapToScreenWithVelocityX(mCurrentScreen + 1, velocityX);
                    }
            	}
            	else if(mCurrentScreen < getChildCount() - 1 && span < -(getWidth()/10))
            	{
            		increasePriority();
           // 		if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX  enter 4444deltaOrignal:"+deltaOrignal);
            		if(0 <= deltaOrignal && deltaOrignal <= 100){
                    	snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen + 1, velocityX);
                    }else{
                    	snapToScreenWithVelocityX(mCurrentScreen + 1, velocityX);
                    }
            	}
            	else
            	{
            		increasePriority();
//            		if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX  enter 5555 velocityX:"+velocityX);
//            		if(velocityX > 0 && mCurrentScreen > 0){  // move to left
//            			snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen - 1, velocityX);
//            		} else if(velocityX < 0 && (mCurrentScreen < getChildCount() - 1)){   // move to right
//            			snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen + 1, velocityX);
//            		} else {
            			snapToDestination(velocityX);
//            		}
            	}
                	
               // if(Launcher.LOGD)Log.d(TAG,"onTouchEvent ACTION_UP, velocityX:"+velocityX+"SNAP_VELOCITY:"+SNAP_VELOCITY+" mMaximumVelocity"+mMaximumVelocity);
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
            }
            else
            {
               //add for auto fit app widget to sutiable place
                mLauncher.endNewDrag(ev.getX(),ev.getY()); 
            }
            
            mTouchState = TOUCH_STATE_REST;
            
            //force to end the drag
            mDragController.endDragForce();
            
            break;
        case MotionEvent.ACTION_CANCEL:
            //add for auto fit app widget to sutiable place
            if(mTouchState != TOUCH_STATE_REST)
            {
                mLauncher.endNewDrag(ev.getX(),ev.getY()); 
            }

            mTouchState = TOUCH_STATE_REST;
            if(null != mVelocityTracker){
            	mVelocityTracker.clear();
            }
            //force to end the drag
            mDragController.endDragForce();
            mActivePointerId = INVALID_POINTER;
            break;
        case MotionEvent.ACTION_POINTER_UP:                      
            onSecondaryPointerUp(ev);            
            break;        
        }

   //     if(Launcher.LOGD)Log.d(TAG,"onTouchEvent return true");
        return true;
    } 
    
     private void scrollToWithoutWallPaperMove(int x, boolean isRight)
    {
//    	if(Launcher.LOGD)Log.d(TAG,"scrollToWithoutWallPaperMove");
    	scrollTo(x, 0);
    }    

    final float distance = 0.8f;
    private boolean isChangedScreen()
    {
        final int screenWidth = getWidth();
        float rate = distance;
        //to right
        if((mDownMotionX - mLastMotionX) < 0)
        {
        	rate = 1-distance;
        }
        
        final int whichScreen = (mScrollX + (int)(screenWidth *rate)) / screenWidth;
        final int whichScreen2 = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        return  (mCurrentScreen != whichScreen2);        
    }
    
    private void snapToDestination(int velocityX) {
        final int screenWidth = getWidth();
        float rate = distance;
        //to right
        if((mDownMotionX - mLastMotionX) < 0)
        {
        	rate = 1-distance;
        }
        final int whichScreen = (mScrollX + (int)(screenWidth *rate)) / screenWidth;

        snapToScreen(whichScreen, 0/*velocityX*/, true);
    }
    
    private boolean noItemsInCurrentScreen()
    {
    	//will enable move wall paper when move screen
    	if(enableMoveFast == false)
    	{
    		return true;
    	}
    	
        boolean hasNoItems = false;
        final CellLayout cellView = (CellLayout)getChildAt(mCurrentScreen);
        if(cellView.getChildCount() < 6)
        {
        	if(cellView.isFull() == true)
        	{
                hasNoItems = true;
        	}
        }
        return hasNoItems;
    }
    
    void snapToScreenWithVelocityX(int whichScreen, int velocityX) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        clearVacantCache();
        enableChildrenCache(mCurrentScreen, whichScreen);
//        
//        if((mCurrentScreen != whichScreen) || noItemsInCurrentScreen() == true)
//        {
//        	int offset = getChildAt(getChildCount() - 1).getRight() - (mRight - mLeft);
//        	if(NeedScrollForActionUP)
//        	    updateWallpaperOffset(offset);
//        }

        final int screenDelta = Math.abs(whichScreen - mCurrentScreen);
        mNextScreen = whichScreen;
        
        if(mLauncher.dockStyle==1){
        	
        }else{
        	if(mLinePageIndicator != null){
        		mLinePageIndicator.refreshPosition(0, 0, mLauncher.screenWidth,mLauncher.screenHeight,  mNextScreen, getChildCount());
        	}else{
        		pageIndicator.drawPageIndicator(mNextScreen,getChildCount());
        	}
        }
        
        View focusedChild = getFocusedChild();
        if (focusedChild != null && screenDelta != 0 && focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }
        
        final int newX = whichScreen * getWidth();
        final int delta = newX - mScrollX;
   
//        if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX whichScreen:"+whichScreen+" velocityX:"+velocityX+ "    delta:"+delta);
        
        int duration = 0;
        	if(false == NeedScrollForActionUP) {
            	bounceInterpolator.setChoice(USED_FOR_DEFAULT);
            	duration = 550/moveScale;
    //        	if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX 111");
            }else if(Math.abs(delta) <= 7){
	        		bounceInterpolator.setChoice(USED_FOR_SHORTDISTANCE_36);
	        		duration = 100/moveScale;//3.6
	  //      		if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX 222");
	        }else if(Math.abs(delta) < 30){
	        		bounceInterpolator.setChoice(USED_FOR_SHORTDISTANCE_18);
	        		duration = 100/moveScale;
	   //     		if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX 333");
        	}else{
            	bounceInterpolator.setChoice(USED_FOR_DEFAULT);
            	int tmpVelocityX = Math.abs(velocityX);
            	if(tmpVelocityX < 400){
            		if(Math.abs(delta) >= (int)(getWidth()*(1-distance)))
            		{
            			 duration = (int)(300* (1.0f*Math.abs(delta)*2)/getWidth())/moveScale;
            		}
            		else
            		{
            			duration = 300/moveScale;
            		}
            	}else{
            		duration = (int)(600 / ( Math.abs(velocityX) / 1400.0f));
            	}
            	
      //      	if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX 777");
            }

        
        duration = (int)(duration * home_screen_move_speed);
        awakenScrollBars(duration);
        
        if (mTouchState != TOUCH_STATE_SCROLLING) {
        	pauseWidgetAnimation();
        }
        
        mScroller.startScroll(mScrollX, 0, delta, 0, duration);
        invalidate();
        mLauncher.resetSkipLongClick();
    }
    
    void snapToScreenWithVelocityXSMALLSLOP(int whichScreen, int velocityX) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        clearVacantCache();
        enableChildrenCache(mCurrentScreen, whichScreen);
        
//        if((mCurrentScreen != whichScreen) || noItemsInCurrentScreen() == true){
//        	int offset = getChildAt(getChildCount() - 1).getRight() - (mRight - mLeft);
//        	if(NeedScrollForActionUP)
//        	    updateWallpaperOffset(offset);
//        }

        final int screenDelta = Math.abs(whichScreen - mCurrentScreen);
        mNextScreen = whichScreen;
        if(mLauncher.dockStyle==1){
        	
        }else{
        	if(mLinePageIndicator != null){
        		mLinePageIndicator.refreshPosition(0, 0, mLauncher.screenWidth,mLauncher.screenHeight,  mNextScreen, getChildCount());
        	}else{
        		pageIndicator.drawPageIndicator(mNextScreen,getChildCount());
        	}
        }
        
        View focusedChild = getFocusedChild();
        if (focusedChild != null && screenDelta != 0 && focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }
        
        final int newX = whichScreen * getWidth();
        final int delta = newX - mScrollX;
        int duration = 650/moveScale;
    	
        
        duration = (int)(duration * home_screen_move_speed);
        awakenScrollBars(duration);
  //      if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityXSMALLSLOP whichScreen:"+whichScreen+" velocityX:"+velocityX+ " delta:"+delta);
        if (mTouchState != TOUCH_STATE_SCROLLING) {
        	pauseWidgetAnimation();
        }
        
        mScroller.startScroll(mScrollX, 0, delta, 0, duration);
        invalidate();
        
        mLauncher.resetSkipLongClick();
    }
    
    public void snapToCurrentScreenNoWallPaperMove()
    {
        final int newX = mCurrentScreen * getWidth();
        final int delta = newX - mScrollX;
        if(delta != 0)
        {
        	clearVacantCache();
            enableChildrenCache(mCurrentScreen, mCurrentScreen);    
        
            mNextScreen = mCurrentScreen;
            if(mLauncher.dockStyle==1){
            	//TODO
            }else{
            	if(mLinePageIndicator != null){
            		mLinePageIndicator.refreshPosition(0, 0, mLauncher.screenWidth,mLauncher.screenHeight,  mNextScreen, getChildCount());
            	}else{
            		pageIndicator.drawPageIndicator(mNextScreen,getChildCount());
            	}
            }
        	
     //   	if(Launcher.LOGD)Log.d(TAG, "snapToCurrentScreenNoWallPaperMove whichScreen:"+mCurrentScreen + " delta="+delta);   
 	        final int duration = 10;
 	        
 	        awakenScrollBars(duration);
 	        pauseWidgetAnimation();
 	        mScroller.startScroll(mScrollX, 0, delta, 0, duration);         
            invalidate();
        }
        
        mLauncher.resetSkipLongClick();
    }
    
    boolean pressedHome  = false;
    public void snapToScreenWithWallPaperMove(int pageIndex)
    {
    	 pressedHome = true;
    	 mScroller.abortAnimation();
    	 mScroller.forceFinished(true);
    	 
    	 snapToScreen(pageIndex);
    }
    
    public HomeScroller getScroller(){
    	return mScroller;
    }

    void snapToScreen(int whichScreen) {
        snapToScreen(whichScreen, 0, false);
    }

    private void snapToScreen(int whichScreen, int velocity, boolean settle) {
        if (!mScroller.isFinished()) return;

        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        
        clearVacantCache();
        enableChildrenCache(mCurrentScreen, whichScreen);

        mNextScreen = whichScreen;

        if(mLauncher.dockStyle==1){
        	mPreviousIndicator.setLevel(mNextScreen);
        	mNextIndicator.setLevel(mNextScreen);
        }else{
        	if(mLinePageIndicator != null){
        		mLinePageIndicator.refreshPosition(0, 0, mLauncher.screenWidth,mLauncher.screenHeight,  mNextScreen, getChildCount());
        	}else{
        		pageIndicator.drawPageIndicator(mNextScreen,getChildCount());
        	}
        }

        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichScreen != mCurrentScreen &&
                focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }
        
        final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
        final int newX = whichScreen * getWidth();
        final int delta = newX - mScrollX;
        int duration = (screenDelta + 1) * 100;

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
//        
//        if (settle) {
//            mScrollInterpolator.setDistance(screenDelta);
//        } else {
//            mScrollInterpolator.disableSettle();
//        }
         if(delta != 0)
        {
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration += (duration / (velocity / BASELINE_FLING_VELOCITY))
                    * FLING_VELOCITY_INFLUENCE;
        } else {
            duration += 100;
        }

        duration = (int)(duration * home_screen_move_speed);
        
        awakenScrollBars(duration);
        pauseWidgetAnimation();
        mScroller.startScroll(mScrollX, 0, delta, 0, duration);
        invalidate();
        }
        
        mLauncher.resetSkipLongClick();
    }
    
    void startDrag(CellLayout.CellInfo cellInfo) {
//        if(Launcher.LOGD)Log.d(TAG,"startDrag");
        View child = cellInfo.cell;
        
        // Make sure the drag was started by a long press as opposed to a long click.
        if (!child.isInTouchMode()) {
            return;
        }
        
        mDragInfo = cellInfo;
        mDragInfo.screen = mCurrentScreen;
        
        CellLayout current = ((CellLayout) getChildAt(mCurrentScreen));

        current.onDragChild(child);
        mDragController.startDrag(child, this, child.getTag(), DragController.DRAG_ACTION_MOVE);
        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final SavedState state = new SavedState(super.onSaveInstanceState());
        state.currentScreen = mCurrentScreen;
//        if(Launcher.LOGD)Log.d(TAG,"onSaveInstanceState savedState.currentScreen=mCurrentScreen= "+mCurrentScreen);
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        
        if(state != null){
            SavedState savedState = (SavedState) state;
//	        if(Launcher.LOGD)Log.d(TAG,"onRestoreInstanceState savedState.currentScreen:"+savedState.currentScreen);
	        try{
	        	super.onRestoreInstanceState(savedState.getSuperState());
	        } catch(IllegalArgumentException ex) {
	        	if(Launcher.LOGD)Log.d(TAG, "onRestoreInstanceState ex:"+ex.getMessage());
	        }
	        
	        if (savedState.currentScreen != -1) {
	            mCurrentScreen = savedState.currentScreen;
	            Launcher.setScreen(mCurrentScreen);
	        }
        }
    }

    void addApplicationShortcut(ShortcutInfo info, CellLayout.CellInfo cellInfo) {
        addApplicationShortcut(info, cellInfo, false);
    }

    void addApplicationShortcut(ShortcutInfo info, CellLayout.CellInfo cellInfo,
            boolean insertAtFirst) {
    	
    	final CellLayout.CellInfo mMenuAddInfo =  findAllVacantCells(null) ; 
    	if(mMenuAddInfo != null && mMenuAddInfo.valid == true)
    	{
	        final CellLayout layout = (CellLayout) getChildAt(cellInfo.screen);
	        final int[] result = new int[2];
	
	        layout.cellToPoint(cellInfo.cellX, cellInfo.cellY, result);
	        onDropExternal(result[0], result[1], info, layout, insertAtFirst);
    	}
    }

    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
//    	if(Launcher.LOGD)Log.d(TAG, "onDrop  x:"+x+" y:"+y+" xOffset:"+xOffset+" yOffset:"+yOffset);
    	if(mLauncher != null){
    	   	mLauncher.closeLiveFolder();
    	}
    		
        final CellLayout cellLayout = getCurrentDropLayout();
        if (source != this) {
            onDropExternal(x - xOffset, y - yOffset, dragInfo, cellLayout);
        } else {
            // Move internally
            if (mDragInfo != null) {
                final View cell = mDragInfo.cell;
                int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;                
                if (index != mDragInfo.screen) {
                    final CellLayout originalCellLayout = (CellLayout) cell.getParent();
                    originalCellLayout.removeView(cell);
                    cellLayout.addView(cell);
                }
                mTargetCell = estimateDropCell(x - xOffset, y - yOffset,
                        mDragInfo.spanX, mDragInfo.spanY, cell, cellLayout, mTargetCell);
                cellLayout.onDropChild(cell, mTargetCell);

                final ItemInfo info = (ItemInfo) cell.getTag();
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                LauncherModel.moveItemInDatabase(mLauncher, info,
                        LauncherSettings.Favorites.CONTAINER_DESKTOP, index, lp.cellX, lp.cellY);
            }
        }
    }

    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        clearVacantCache();
    }

    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        final CellLayout cellLayout = (CellLayout) getChildAt(mCurrentScreen);
        if (source != this) {
//            Log.d(TAG,"b191 onDragOver==source="+source+"==dragView="+dragView+"==dragInfo=");
            //onDropExternal(x - xOffset, y - xOffset, dragInfo, cellLayout);
        } else {
//            Log.d(TAG,"b191 onDragOver else ==source="+source+"==dragView="+dragView+"==dragInfo="+dragInfo);
            if(mDragInfo != null)
            {
            	if(DeleteZone.isInTrashMode() == false)
            	{
                    final View cell = mDragInfo.cell;
                    cellLayout.onDragOverChild(cell,x-xOffset,y-yOffset);
            	}
            }
        }
    }

    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        clearVacantCache();
    }

    private void onDropExternal(int x, int y, Object dragInfo, CellLayout cellLayout) {
        onDropExternal(x, y, dragInfo, cellLayout, false);
    }
    
    public void onDropExternal(int x, int y, Object dragInfo, CellLayout cellLayout,
            boolean insertAtFirst) {
//        if(Launcher.LOGD)Log.d(TAG, "onDropExternal");
        // Drag from somewhere else
        ItemInfo info = (ItemInfo) dragInfo;

        View view;

        switch (info.itemType) {
        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
            if (info.container == NO_ID && info instanceof ApplicationInfo) {
                // Came from all apps -- make a copy
                info = new ShortcutInfo((ApplicationInfo)info);
            }
          //calendar app icon need use special drawable, not the relative app's own icon.
            if(null != ((ShortcutInfo) info).intent && null != ((ShortcutInfo) info).intent.getComponent() && CALENDAR_CLASS_NAME.equals(((ShortcutInfo) info).intent.getComponent().getClassName())){
                ((ShortcutInfo) info).setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_icon_calendar));
                Intent calendarIntent = new Intent(Launcher.ACTION_ITEM_ADDED);
                calendarIntent.putExtra(Launcher.EXTRA_ITEM_LAUNCH_INTENT, ((ShortcutInfo)info).intent);
                mLauncher.sendBroadcast(calendarIntent);
            }
            view = mLauncher.createShortcut(R.layout.application, cellLayout, (ShortcutInfo)info);
            break;
        case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
            view = FolderIcon.fromXml(R.layout.folder_icon, mLauncher,
                    (ViewGroup) getChildAt(mCurrentScreen), ((UserFolderInfo) info));
            break;
        default:
            Log.e(TAG, new IllegalStateException("Unknown item type: " + info.itemType).toString());
            return;
        }

        cellLayout.addView(view, insertAtFirst ? 0 : -1);
        view.setHapticFeedbackEnabled(false);
        view.setOnLongClickListener(mLongClickListener);
        if (view instanceof DropTarget) {
            mDragController.addDropTarget((DropTarget) view);
        }

        mTargetCell = estimateDropCell(x, y, 1, 1, view, cellLayout, mTargetCell);
        cellLayout.onDropChild(view, mTargetCell);
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();

        LauncherModel.addOrMoveItemInDatabase(mLauncher, info,
                LauncherSettings.Favorites.CONTAINER_DESKTOP, mCurrentScreen, lp.cellX, lp.cellY);
         mLauncher.mDesktopItems.add(info);
    }
    
    /**
     * Return the current {@link CellLayout}, correctly picking the destination
     * screen while a scroll is in progress.
     */
    private CellLayout getCurrentDropLayout() {
        int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;
        return (CellLayout) getChildAt(index);
    }

    /**
     * {@inheritDoc}
     */
    public boolean acceptDrop(DragSource source, int x, int y,
            int xOffset, int yOffset, DragView dragView, Object dragInfo) {
        final CellLayout layout = getCurrentDropLayout();
        final CellLayout.CellInfo cellInfo = mDragInfo;
        final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
        final int spanY = cellInfo == null ? 1 : cellInfo.spanY;

        if (mVacantCache == null) {
            final View ignoreView = cellInfo == null ? null : cellInfo.cell;
            mVacantCache = layout.findAllVacantCells(null, ignoreView);
        }

        return mVacantCache.findCellForSpan(mTempEstimate, spanX, spanY, false);
    }
    
    /**
     * {@inheritDoc}
     */
    public Rect estimateDropLocation(DragSource source, int x, int y,
            int xOffset, int yOffset, DragView dragView, Object dragInfo, Rect recycle) {
        final CellLayout layout = getCurrentDropLayout();
        
        final CellLayout.CellInfo cellInfo = mDragInfo;
        final int spanX = cellInfo == null ? 1 : cellInfo.spanX;
        final int spanY = cellInfo == null ? 1 : cellInfo.spanY;
        final View ignoreView = cellInfo == null ? null : cellInfo.cell;
        
        final Rect location = recycle != null ? recycle : new Rect();
        
        // Find drop cell and convert into rectangle
        int[] dropCell = estimateDropCell(x - xOffset, y - yOffset,
                spanX, spanY, ignoreView, layout, mTempCell);
        
        if (dropCell == null) {
            return null;
        }
        
        layout.cellToPoint(dropCell[0], dropCell[1], mTempEstimate);
        location.left = mTempEstimate[0];
        location.top = mTempEstimate[1];
        
        layout.cellToPoint(dropCell[0] + spanX, dropCell[1] + spanY, mTempEstimate);
        location.right = mTempEstimate[0];
        location.bottom = mTempEstimate[1];
        
        return location;
    }

    /**
     * Calculate the nearest cell where the given object would be dropped.
     */
    private int[] estimateDropCell(int pixelX, int pixelY,
            int spanX, int spanY, View ignoreView, CellLayout layout, int[] recycle) {
        // Create vacant cell cache if none exists
        if (mVacantCache == null) {
            mVacantCache = layout.findAllVacantCells(null, ignoreView);
        }

        // Find the best target drop location
        return layout.findNearestVacantArea(pixelX, pixelY,
                spanX, spanY, mVacantCache, recycle);
    }
    
    void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    public void setDragController(DragController dragController) {
        mDragController = dragController;
    }

    public void onDropCompleted(View target, boolean success) {
//        if(Launcher.LOGD)Log.d(TAG,"onDropCompleted  success:"+success);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            
            if(success) 
            {
                layout.saveMovedCells(); 
            }
            else
            {
                //restore status before dragging
                layout.restoreStatusBeforeMoved();
                //requestLayout();
            }
            layout.clearArrangeAction();
            layout.clearDrawRects();
        }
        clearVacantCache();

        if (success){
            if (target != this && mDragInfo != null) {
                final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
                cellLayout.removeView(mDragInfo.cell);
                if (mDragInfo.cell instanceof DropTarget) {
                    mDragController.removeDropTarget((DropTarget)mDragInfo.cell);
                }
                //final Object tag = mDragInfo.cell.getTag();
            }
        } else {
            if (mDragInfo != null) {
                final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
                cellLayout.onDropAborted(mDragInfo.cell);
            }
        }

        mDragInfo = null;
    }

    public void scrollLeft() {
//        if(Launcher.LOGD)Log.d(TAG,"scrollLeft");
        clearVacantCache();
        if (mScroller.isFinished()) {
            if (mCurrentScreen > 0) snapToScreen(mCurrentScreen - 1);
        } else {
            if (mNextScreen > 0) snapToScreen(mNextScreen - 1);            
        }
    }

    public void scrollRight() {
//        if(Launcher.LOGD)Log.d(TAG,"scrollRight");
        clearVacantCache();
        if (mScroller.isFinished()) {
            if (mCurrentScreen < getChildCount() -1) snapToScreen(mCurrentScreen + 1);
        } else {
            if (mNextScreen < getChildCount() -1) snapToScreen(mNextScreen + 1);            
        }
    }

    public int getScreenForView(View v) {
        int result = -1;
        if (v != null) {
            ViewParent vp = v.getParent();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (vp == getChildAt(i)) {
                    return i;
                }
            }
        }
        return result;
    }

    public Folder getFolderForTag(Object tag) {
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            CellLayout currentScreen = ((CellLayout) getChildAt(screen));
            int count = currentScreen.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = currentScreen.getChildAt(i);
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
                if (lp.cellHSpan == 4 && lp.cellVSpan == 4 && child instanceof Folder) {
                    Folder f = (Folder) child;
                    if (f.getInfo() == tag) {
                        return f;
                    }
                }
            }
        }
        return null;
    }

    public View getViewForTag(Object tag) {
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            CellLayout currentScreen = ((CellLayout) getChildAt(screen));
            int count = currentScreen.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = currentScreen.getChildAt(i);
                if (child.getTag() == tag) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * @return True is long presses are still allowed for the current touch
     */
    public boolean allowLongPress() {
        return mAllowLongPress;
    }
    
    /**
     * Set true to allow long-press events to be triggered, usually checked by
     * {@link Launcher} to accept or block dpad-initiated long-presses.
     */
    public void setAllowLongPress(boolean allowLongPress) {
        mAllowLongPress = allowLongPress;
    }

    void removeItems(final ArrayList<ApplicationInfo> apps) {
        final int count = getChildCount();
        final PackageManager manager = getContext().getPackageManager();
        final AppWidgetManager widgets = AppWidgetManager.getInstance(getContext());

        final HashSet<String> packageNames = new HashSet<String>();
        final int appCount = apps.size();
        for (int i = 0; i < appCount; i++) {
            packageNames.add(apps.get(i).componentName.getPackageName());
        }

        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);

            // Avoid ANRs by treating each screen separately
            post(new Runnable() {
                public void run() {
                    final ArrayList<View> childrenToRemove = new ArrayList<View>();
                    childrenToRemove.clear();
        
                    int childCount = layout.getChildCount();
                    for (int j = 0; j < childCount; j++) {
                        final View view = layout.getChildAt(j);
                        Object tag = view.getTag();
        
                        if (tag instanceof ShortcutInfo) {
                            final ShortcutInfo info = (ShortcutInfo) tag;
                            final Intent intent = info.intent;
                            final ComponentName name = intent.getComponent();
        
                            if (Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                                for (String packageName: packageNames) {
                                    if (packageName.equals(name.getPackageName())) {
                                        // TODO: This should probably be done on a worker thread
                                        LauncherModel.deleteItemFromDatabase(mLauncher, info);
                                        childrenToRemove.add(view);
                                    }
                                }
                            }
                        } else if (tag instanceof UserFolderInfo) {
                            final UserFolderInfo info = (UserFolderInfo) tag;
                            final ArrayList<ShortcutInfo> contents = info.contents;
                            final ArrayList<ShortcutInfo> toRemove = new ArrayList<ShortcutInfo>(1);
                            final int contentsCount = contents.size();
                            boolean removedFromFolder = false;
        
                            for (int k = 0; k < contentsCount; k++) {
                                final ShortcutInfo appInfo = contents.get(k);
                                final Intent intent = appInfo.intent;
                                final ComponentName name = intent.getComponent();
        
                                if (Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                                    for (String packageName: packageNames) {
                                        if (packageName.equals(name.getPackageName())) {
                                            toRemove.add(appInfo);
                                            // TODO: This should probably be done on a worker thread
                                            LauncherModel.deleteItemFromDatabase(
                                                    mLauncher, appInfo);
                                            removedFromFolder = true;
                                        }
                                    }
                                }
                            }
        
                            contents.removeAll(toRemove);
                            if (removedFromFolder) {
                                final Folder folder = getOpenFolder();
                                if (folder != null) folder.notifyDataSetChanged();
                            }
                        } else if (tag instanceof LiveFolderInfo) {
                            final LiveFolderInfo info = (LiveFolderInfo) tag;
                            final Uri uri = info.uri;
                            final ProviderInfo providerInfo = manager.resolveContentProvider(
                                    uri.getAuthority(), 0);

                            if (providerInfo != null) {
                                for (String packageName: packageNames) {
                                    if (packageName.equals(providerInfo.packageName)) {
                                        // TODO: This should probably be done on a worker thread
                                        LauncherModel.deleteItemFromDatabase(mLauncher, info);
                                        childrenToRemove.add(view);                        
                                    }
                                }
                            }
                        } else if (tag instanceof LauncherAppWidgetInfo) {
                            final LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) tag;
                            final AppWidgetProviderInfo provider =
                                    widgets.getAppWidgetInfo(info.appWidgetId);
                            if (provider != null) {
                                for (String packageName: packageNames) {
                                    if (packageName.equals(provider.provider.getPackageName())) {
                                        // TODO: This should probably be done on a worker thread
                                        LauncherModel.deleteItemFromDatabase(mLauncher, info);
                                        childrenToRemove.add(view);                                
                                    }
                                }
                            }
                        }
                    }
        
                    childCount = childrenToRemove.size();
                    for (int j = 0; j < childCount; j++) {
                        View child = childrenToRemove.get(j);
                        layout.removeViewInLayout(child);
                        if (child instanceof DropTarget) {
                            mDragController.removeDropTarget((DropTarget)child);
                        }
                    }
        
                    if (childCount > 0) {
                        layout.requestLayout();
                        layout.invalidate();
                    }
                }
            });
        }
    }
    
    private void getIconCache(){
    	if(mIconCache == null){
    		LauncherApplication app = (LauncherApplication)getContext().getApplicationContext();
    		mIconCache = app.getIconCache();
    	}
    }

    void updateShortcuts(ArrayList<ApplicationInfo> apps) {
        final int count = getChildCount();
        getIconCache();
        
        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            int childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                final View view = layout.getChildAt(j);
                Object tag = view.getTag();
                if (tag instanceof ShortcutInfo) {
                    ShortcutInfo info = (ShortcutInfo)tag;
                    // We need to check for ACTION_MAIN otherwise getComponent() might
                    // return null for some shortcuts (for instance, for shortcuts to
                    // web pages.)
                    final Intent intent = info.intent;
                    final ComponentName name = intent.getComponent();
                    if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION &&
                            Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                        final int appCount = apps.size();
                        for (int k=0; k<appCount; k++) {
                            ApplicationInfo app = apps.get(k);
                            if (app.componentName.equals(name)) {
                                info.setIcon(mIconCache.getIcon(info.intent));
                                ((TextView)view).setCompoundDrawablesWithIntrinsicBounds(null,
                                        new FastBitmapDrawable(info.getIcon(mIconCache)),
                                        null, null);
                                }
                        }
                    }
                }
            }
        }
    }

    void moveToDefaultScreen(boolean animate) {
//        if(Launcher.LOGD)Log.d(TAG,"moveToDefaultScreen");
        if (animate) {
            snapToScreen(mDefaultScreen);
        } else {
            setCurrentScreen(mDefaultScreen);
        }
        getChildAt(mDefaultScreen).requestFocus();
    }

    void setIndicators(Drawable previous, Drawable next) {
        mPreviousIndicator = previous;
        mNextIndicator = next;
        previous.setLevel(mCurrentScreen);
        next.setLevel(mCurrentScreen);
    }

    PageIndicatorView pageIndicator;
    void setPageIndicator(PageIndicatorView view) {
        pageIndicator = view;
    }
    public static class SavedState extends BaseSavedState {
        int currentScreen = -1;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentScreen = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentScreen);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public static final String CALENDAR_CLASS_NAME = "com.android.calendar.MonthActivity";
    public ArrayList<BubbleTextView> findCalendarShortcuts(){
        ArrayList<BubbleTextView> ivs = new ArrayList<BubbleTextView>();
        final int count = getChildCount();
      
        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            final int countj = layout.getChildCount();
            for (int j = 0; j < countj; j++) {
                final View view = layout.getChildAt(j);
                final Object tag = view.getTag();
//                Log.d(TAG, "findCalendarShortcuts  view:"+view+" tag:"+tag);
                
                if(view instanceof BubbleTextView && tag instanceof ShortcutInfo){
                    if(null != ((ShortcutInfo)tag).intent.getComponent() && ((ShortcutInfo)tag).intent.getComponent().getClassName().equals(CALENDAR_CLASS_NAME)){
                        ivs.add((BubbleTextView)view);
                    }
                }
            }
        }
        
//        if(Launcher.LOGD)Log.d(TAG,"findCalendarShortcuts  count:"+ivs.size());
        return ivs;
    }
    
    public void updateCalendarInconInFolder(Bitmap newBitmap){
//    	if(Launcher.LOGD)Log.d(TAG, "updateCalendarInconInFolder");
    	if(newBitmap == null){
    		return;
    	}
    	
    	 for (int i = 0; i <  getChildCount(); i++) {
             final CellLayout layout = (CellLayout) getChildAt(i);
             for (int j = 0; j < layout.getChildCount(); j++) {
                 final View view = layout.getChildAt(j);
                 final Object tag = view.getTag();
//                 if(Launcher.LOGD)Log.d(TAG, "updateCalendarInconInFolder view:"+view+" tag:"+tag);
                 if(view instanceof FolderIcon && tag instanceof UserFolderInfo){ // folder close status
                	if(((UserFolderInfo)tag).opened == false){
                		ArrayList<ShortcutInfo> infos = ((UserFolderInfo)tag).contents;
                		if(infos != null && infos.size()>0){
                			for(ShortcutInfo info:infos){
                				if(null != info.intent && null != info.intent.getComponent() && info.intent.getComponent().getClassName().equals(CALENDAR_CLASS_NAME)){
                					info.mIcon = newBitmap;
                				}
                			}
                		}
                	}
                 }else if(view instanceof UserFolder){ // folder open status
            		 ListAdapter adapter = ((UserFolder)view).mContent.getAdapter();
            		 for(int k = 0; k<adapter.getCount(); k++){
            			 ShortcutInfo info = (ShortcutInfo)adapter.getItem(k);
            			 if(null != info.intent && null != info.intent.getComponent() && info.intent.getComponent().getClassName().equals(CALENDAR_CLASS_NAME)){
        					info.mIcon = newBitmap;
                         }
            		 }
            		 
            		 ((UserFolder)view).notifyDataSetChanged();
                 }
             }
         }
    }

    public void setScreenNum(int num){
    	mScreenNum = num;
    }

    private final static int PAUSE_DCD = 0xFF;
    private final static int RESTART_DCD = 0xFE;
	private void pauseWidgetAnimation(){
//		if(Launcher.LOGD)Log.d(TAG, "pauseWidgetAnimation mLauncher.widgetHostViews size:"+mLauncher.widgetHostViews.size());
    	
    	for(int i=0; i<mLauncher.widgetHostViews.size(); i++){
			AppWidgetHostView dcd = mLauncher.widgetHostViews.get(i);
			View cv = dcd.getChildAt(0);
			if(cv != null && cv instanceof ViewGroup){
				if(((ViewGroup)cv).getChildCount() > 0){
					View cvchild = ((ViewGroup)cv).getChildAt(0);
					if(cvchild != null){
//						Log.d(TAG, "pauseWidgetAnimation  setTag view:"+cvchild);
						cvchild.setTag(PAUSE_DCD);
					}
				}
			}
    	}
	}
	
	private void reStartWidgetAnimation(){
//		if(Launcher.LOGD)Log.d(TAG, "reStartWidgetAnimation");
		
		for(int i=0; i<mLauncher.widgetHostViews.size(); i++){
			AppWidgetHostView dcd = mLauncher.widgetHostViews.get(i);
			View cv = dcd.getChildAt(0);
			if(cv != null && cv instanceof ViewGroup){
				View cvchild = ((ViewGroup)cv).getChildAt(0);
				if(cvchild != null){
//					Log.d(TAG, "reStartWidgetAnimation  setTag view:"+cvchild);
					cvchild.setTag(RESTART_DCD);
				}
			}
    	}
	}
    
	//currently just for DCD 
	public ArrayList<AppWidgetHostView> findAppWidgetHostViews(int pageIndex){
		if(pageIndex < 0 || pageIndex >= this.getChildCount()){
			return null;
		}
		
		final String DCD_APPWIDGET_COMPONENT_NAME = "oms.dcd.appwidget.DCDWidget";
        ArrayList<AppWidgetHostView> ivs = new ArrayList<AppWidgetHostView>();
      
            final CellLayout layout = (CellLayout) getChildAt(pageIndex);
            final int countj = layout.getChildCount();
            for (int j = 0; j < countj; j++) {
                final View view = layout.getChildAt(j);
                
                if(view instanceof LauncherAppWidgetHostView){
                	AppWidgetProviderInfo apinfo = ((LauncherAppWidgetHostView)view).getAppWidgetInfo();
                    if(apinfo != null && apinfo.provider != null && DCD_APPWIDGET_COMPONENT_NAME.equals(apinfo.provider.getClassName())){
                        ivs.add((AppWidgetHostView)view);
                    }
                }
            }
        
        //if(Launcher.LOGD)Log.d(TAG,"findAppWidgetHostViews  count:"+ivs.size());
        return ivs;
    }
	
	public final static String STK_CLASSNAME = "com.android.borqsstk.MainScreen";
	public void refreshSTKShortcuts(){
//		Log.d(TAG, "refreshSTKShortcuts");
        final int count = getChildCount();
        getIconCache();
        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            final int countj = layout.getChildCount();
            for (int j = 0; j < countj; j++) {
                final View view = layout.getChildAt(j);
                final Object tag = view.getTag();
                if(view instanceof BubbleTextView && tag instanceof ShortcutInfo){
                    if(null != ((ShortcutInfo)tag).intent.getComponent() && ((ShortcutInfo)tag).intent.getComponent().getClassName().equals(STK_CLASSNAME)){
                    	 Drawable stkDrawable = new BitmapDrawable(mIconCache.getIcon(((ShortcutInfo)tag).intent));
                         ((BitmapDrawable)stkDrawable).setTargetDensity(mLauncher.getResources().getDisplayMetrics());
                         ((BubbleTextView)view).setCompoundDrawablesWithIntrinsicBounds(null,  stkDrawable, null, null);
                    }
                }
            }
        }
        
        updateSTKIconInFolder();
    }
    
    private void updateSTKIconInFolder(){
    	if(Launcher.LOGD)Log.d(TAG, "updateSTKIconInFolder");
    	getIconCache();
    	for (int i = 0; i <  getChildCount(); i++) {
             final CellLayout layout = (CellLayout) getChildAt(i);
             for (int j = 0; j < layout.getChildCount(); j++) {
                 final View view = layout.getChildAt(j);
                 final Object tag = view.getTag();
                 if(view instanceof FolderIcon && tag instanceof UserFolderInfo){ // folder close status
                	if(((UserFolderInfo)tag).opened == false){
                		ArrayList<ShortcutInfo> infos = ((UserFolderInfo)tag).contents;
                		if(infos != null && infos.size()>0){
                			for(ShortcutInfo info:infos){
                				if(null != info.intent && null != info.intent.getComponent() && info.intent.getComponent().getClassName().equals(STK_CLASSNAME)){
                					info.mIcon = mIconCache.getIcon(info.intent);
                				}
                			}
                		}
                	}
                 }else if(view instanceof UserFolder){ // folder open status
            		 ListAdapter adapter = ((UserFolder)view).mContent.getAdapter();
            		 for(int k = 0; k<adapter.getCount(); k++){
            			 ShortcutInfo info = (ShortcutInfo)adapter.getItem(k);
            			 if(null != info.intent && null != info.intent.getComponent() && info.intent.getComponent().getClassName().equals(STK_CLASSNAME)){
        					info.mIcon = mIconCache.getIcon(info.intent);
                         }
            		 }
            		 
            		 ((UserFolder)view).notifyDataSetChanged();
                 }
             }
         }
    }
	
	@Override
	public void invalidate() {
		if(mOrientation == Configuration.ORIENTATION_PORTRAIT){
			if(mLauncher != null && mLauncher.mBottomLayout != null){
				super.invalidate(mScrollX, 0, mScrollX+getWidth(), getHeight()-mLauncher.mBottomLayout.getHeight());
			}else{
				super.invalidate();
			}
		}else{
			super.invalidate();
		}
	}
	
	public void forceSetViewFocus(){
//	    if(Launcher.LOGD)Log.d(TAG, "forceSetViewFocus");
	    View parentView = getChildAt(getCurrentScreen());
        View view = null;
        if(parentView !=null)
        {
            view = FocusFinder.getInstance().findNextFocus((CellLayout)parentView, null, View.FOCUS_RIGHT);
            if(view!=null)
            {
                view.requestFocus();
            }
        }
        
	    isNeedForceSetFocusFlag = false;
	}
	
	private boolean isNeedForceSetFocusFlag = false;
	public void setNeedForceSetFocusFlag(boolean flag){
//	    if(Launcher.LOGD)Log.d(TAG, "setNeedForceSetFocusFlag");
	    isNeedForceSetFocusFlag = flag;
	}
	
	public void setPageIndicatorLineStyleView(PageIndicatorLineStyleView pv){
    	mLinePageIndicator = pv;
    }
	
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
		ViewGroup vp = (ViewGroup)getChildAt(mCurrentScreen);
		if(vp.getChildCount() == 0)
		{
			
		}
		return super.dispatchKeyEvent(event);
	}
	
	public int mTmpStopCount = 0;
	public boolean isDestroyed;
	public void temporaryDisableInvalidate()
    {
    }
	
	public void checkPendingInvalidate(){
	}
	
	
}
