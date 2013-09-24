package com.tormas.home;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.tormas.home.CellLayout.CellInfo;
import com.tormas.home.Workspace.MyBounceInterpolator;
import com.tormas.home.quickaction.QuickLauncher;
import com.tormas.home.ui.CorpusSelectionDialog.OnCorpusSelectedListener;

public class AllAppsScreen extends ViewGroup implements DragSource, DragScroller {
    private static final String TAG = "oms2.5Launcher.AllAppsScreen";
    private Context mContext;
    
    private boolean mFirstLayout = true;
    private static final int INVALID_SCREEN = -1;
    private int mNextScreen = INVALID_SCREEN;

    public int mCurrentScreen;
    private float mDownMotionX;
    private float mLastMotionX;
    private float mLastMotionY;
    
    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;
    private int mTouchState = TOUCH_STATE_REST;
    private static final int SNAP_VELOCITY = 200;

    private OnLongClickListener mLongClickListener;

    private Launcher mLauncher;
    private IconCache mIconCache;
    private HomeScroller mScroller;
    private DragController mDragController;
    private VelocityTracker mVelocityTracker;
    private Workspace.WorkspaceOvershootInterpolator mScrollInterpolator;
    private MyBounceInterpolator bounceInterpolator;
    private boolean mAllowLongPress = true;

    private int mTouchSlop;
    private int mMaximumVelocity;
    
    private static final int INVALID_POINTER = -1;
    
    private Drawable mNextIndicator;
    private Drawable mPreviousIndicator;
    private float mTouchX;
    private float mSmoothingTime;
    
    private int SLOP = Workspace.SLOP;
    private static final float NANOTIME_DIV = Workspace.NANOTIME_DIV;
    private static final float SMOOTHING_SPEED = Workspace.SMOOTHING_SPEED;
    private static final float SMOOTHING_CONSTANT = Workspace.SMOOTHING_CONSTANT;
    private static final float BASELINE_FLING_VELOCITY = Workspace.BASELINE_FLING_VELOCITY;
    private static final float FLING_VELOCITY_INFLUENCE = Workspace.FLING_VELOCITY_INFLUENCE;

    private PageIndicatorLineStyleView mLinePageIndicator ;
    private int pageIndicatorRawWidth = 0;
    private int pageIndicatorRawHeight = 0;
    
    public AllAppsScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        setHapticFeedbackEnabled(false);

        bounceInterpolator = new MyBounceInterpolator();
        bounceInterpolator.setChoice(Workspace.USED_FOR_DEFAULT);
        mScroller = new HomeScroller(getContext()/*,bounceInterpolator*/);
        pageIndicatorRawWidth = context.getResources().getDrawable(R.drawable.page_indicator).getIntrinsicWidth();
        pageIndicatorRawHeight = context.getResources().getDrawable(R.drawable.page_indicator).getIntrinsicHeight();
		initAllAppsScreens();
		
		setAlwaysDrawnWithCacheEnabled(true);
    }
    
    public void initAllAppsScreens() {
        if(Launcher.LOGD)Log.d(TAG,"initAllAppsScreens");
        mCurrentScreen = 0;
        
        LauncherApplication app = (LauncherApplication)getContext().getApplicationContext();
        mIconCache = app.getIconCache();

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }
    
    public void initAllAppsScreenCellLayout(int screenCount){
    	if(Launcher.LOGD)Log.d(TAG, "initCellLayout");
        final int tmpCount = getChildCount();
        for(int j=0;j<tmpCount;j++){
        	CellLayout child = (CellLayout)getChildAt(j);
        	if(child != null) {
        		child.removeAllViews();
        	}
        }
        
        removeAllViews();
        
        final int childCount = screenCount > 0 ? screenCount:1;
        final LayoutInflater mInflater = LayoutInflater.from(mContext);
        for (int i = 0; i < childCount; i++) {
            View view = mInflater.inflate(R.layout.all_apps_screen, null);  
            view.setId(200 + i); //give cell layout an id.
            addView(view, i);
        }
    }

    //use for page exchange, the screen no need update.
    public void resetCellLayout(){
    	if(Launcher.LOGD)Log.d(TAG, "resetCellLayout");
		for(int j=0;j<this.getChildCount();j++){
		 	CellLayout child = (CellLayout)getChildAt(j);
		 	if(child != null) {
		 		child.reSetCellInfoScreen();
		 	}
		}
    }
    
    @Override
    public void addView(View child) {
        if (!(child instanceof AllAppsCellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have AllAppsCellLayout children.");
        }
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (!(child instanceof AllAppsCellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have AllAppsCellLayout children.");
        }
        super.addView(child, index);
    }

    @Override
    public void addView(View child, LayoutParams params) {
        if (!(child instanceof AllAppsCellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have AllAppsCellLayout children.");
        }
        super.addView(child, params);
    }

    public void setCurrentScreen(int currentScreen) {
        if (!mScroller.isFinished()) mScroller.abortAnimation();
        mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
        scrollTo(mCurrentScreen * getWidth(), 0);
        invalidate();
    }

    void addInCurrentScreen(View child, int x, int y, int spanX, int spanY) {
        addInScreen(child, mCurrentScreen, x, y, spanX, spanY, false);
    }

    void addInCurrentScreen(View child, int x, int y, int spanX, int spanY, boolean insert) {
        addInScreen(child, mCurrentScreen, x, y, spanX, spanY, insert);
    }

    void addInScreen(View child, int screen, int x, int y, int spanX, int spanY) {
        addInScreen(child, screen, x, y, spanX, spanY, false);
    }

    void addInScreen(View child, int screen, int x, int y, int spanX, int spanY, boolean insert) {
//        if(Launcher.LOGD)Log.d(TAG,"addInScreen screen:"+screen+" view:"+child);
        if (screen < 0 || screen >= getChildCount()) {
            Log.e(TAG, "The screen must be >= 0 and < " + getChildCount()
                + " (was " + screen + "); skipping child");
            return;
        }

        final AllAppsCellLayout group = (AllAppsCellLayout) getChildAt(screen);
        AllAppsCellLayout.LayoutParams lp = (AllAppsCellLayout.LayoutParams)child.getLayoutParams();
        if (lp == null) {
            lp = new AllAppsCellLayout.LayoutParams(x, y, spanX, spanY);
        } else {
            lp.cellX = x;
            lp.cellY = y;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
        }
       
        child.setFocusable(true);
        child.setFocusableInTouchMode(true);
        group.addView(child, insert ? 0 : -1, lp);
        
        if (!(child instanceof Folder)) {
            child.setHapticFeedbackEnabled(false);
            child.setOnLongClickListener(mLongClickListener);
        }
        if (child instanceof DropTarget) {
            mDragController.addDropTarget((DropTarget)child);
        }
    }
    
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClickListener = l;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).setOnLongClickListener(l);
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        mTouchX = x;
        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
    }
    
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mTouchX = mScrollX = mScroller.getCurrX();
            mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
            mScrollY = mScroller.getCurrY();
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
            mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
            mNextScreen = INVALID_SCREEN;
            clearChildrenCache();
        } else if (mTouchState == TOUCH_STATE_SCROLLING) {
            final float now = System.nanoTime() / NANOTIME_DIV;
            final float e = (float) Math.exp((now - mSmoothingTime) / SMOOTHING_CONSTANT);
            final float dx = mTouchX - mScrollX;
            mScrollX += dx * e;
            mSmoothingTime = now;
            
            if (dx > 1.f || dx < -1.f) {
                postInvalidate();
            }
        }
    }

    boolean needrecord =false;
    int     count = 1;
    long alltime  = 0;
    long lastDrawtime=0;    
    boolean SHOW_DARW = true;
        
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
        

        boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING && mNextScreen == INVALID_SCREEN;
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
        
        if(Launcher.LOGD && SHOW_DARW)
        {
        	long drawtime = (System.currentTimeMillis()-pre);
        	Log.d(TAG, "dispatchDraw time:"+ drawtime + " span="+span + " average time="+alltime/count + "\npage="+drawCount);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        computeScroll();
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
    	if(Launcher.LOGD)Log.d(TAG,"requestChildRectangleOnScreen view:"+child);
        int screen = indexOfChild(child);
        if (screen != mCurrentScreen || !mScroller.isFinished()) {
            snapToScreen(screen);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
    	if(Launcher.LOGD)Log.d(TAG, "onRequestFocusInDescendants  direction:"+direction+" rect:"+previouslyFocusedRect);
        int focusableScreen;
        if (mNextScreen != INVALID_SCREEN) {
            focusableScreen = mNextScreen;
        } else {
            focusableScreen = mCurrentScreen;
        }
		if(null != getChildAt(focusableScreen)) {
			getChildAt(focusableScreen).requestFocus(direction, previouslyFocusedRect);
		}
        return false;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
    	if(Launcher.LOGD)Log.d(TAG, "dispatchUnhandledMove   focused view:"+focused+"  direction:"+direction);
        if (direction == View.FOCUS_LEFT) {
            if (mCurrentScreen > 0) {
                snapToScreen(mCurrentScreen - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (mCurrentScreen < getChildCount() - 1) {
                snapToScreen(mCurrentScreen + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
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
    }

	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent ev:"+ev.getAction()+" currenScreen:"+mCurrentScreen+"  view:"+getChildAt(mCurrentScreen));
        final int action = ev.getAction();
        
        if(mLoadingBar!=null && mLoadingBar.isShown()){
        	if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent return false, AllApps is shown");
        	return false;
        }
        
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent return true   ---------  !TOUCH_STATE_REST && ACTION_MOVE");
            return true;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        
        final float x = ev.getX();
        final float y = ev.getY();
        
        switch (action) {
            case MotionEvent.ACTION_MOVE: {               
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);
                int moveSlop = SLOP;
                final int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > moveSlop;
                boolean yMoved = yDiff > moveSlop;

                if (xMoved || yMoved) {
                    
                	 if(yDiff > (0.577f * xDiff)){
             			xMoved = false;
             			
             			final VelocityTracker velocityTracker = mVelocityTracker;
     	                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
     	                int velocityX = (int) velocityTracker.getXVelocity();
     	                
             			if(yDiff > this.getHeight()/4 || velocityX > 600)
             			{
             			    //change to other category
             				boolean next = (y - mLastMotionY) > 0;
             				
             				
             				mLastMotionY = y;
            				mLastMotionX = x;
            				mDownMotionX = x;            				
             				if(SelectNextCategory(next) == true)
             				{
     	        		        mVelocityTracker.clear();   
     	        		        
     	        		        return true;
             				}
             			}
             		}
                	 
                    if (xMoved) {
                        // Scroll if the user moved far enough along the X axis
                        mTouchState = TOUCH_STATE_SCROLLING;
                        //when move, record the current postion as first pos
                        mDownMotionX = x;
                        mLastMotionX = x;
                        mLastMotionY = y;
                        mTouchX = mScrollX;
                        mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
                        enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
                        
                        alltime = 0;
                        count = 1;
                    }
                    // Either way, cancel any pending longpress
                    if (mAllowLongPress) {
                        mAllowLongPress = false;

                        final View currentScreen = getChildAt(mCurrentScreen);
                        currentScreen.cancelLongPress();
                    }
                }else{
                }
                
                break;
            }
            case MotionEvent.ACTION_DOWN: {              
                // Remember location of down touch
            	mDownMotionX = x;
                mLastMotionX = x;
                mLastMotionY = y;
                mAllowLongPress = true;
                
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent ACTION_DOWN, mLastMotionX:"+x+" mLastMotionY:"+y);
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            	alltime = 0;
                count = 1;
                   
            	final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();
                mVelocityTracker.clear();

                // Release the drag
                clearChildrenCache();
                mTouchState = TOUCH_STATE_REST;               
                mAllowLongPress = false;
                
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                break; 
        }

        if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent return "+  String.valueOf(mTouchState != TOUCH_STATE_REST));
        return mTouchState != TOUCH_STATE_REST;
    }   

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

    void enableChildrenCache(int fromScreen, int toScreen) {
        if (fromScreen > toScreen) {
            final int temp = fromScreen;
            fromScreen = toScreen;
            toScreen = temp;
        }
        
        //enable cell cache
        setChildrenDrawnWithCacheEnabled(true);
        setChildrenDrawingCacheEnabled(true);
        
        
        final int count = getChildCount();

        fromScreen = Math.max(fromScreen, 0);
        toScreen = Math.min(toScreen, count - 1);

        for (int i = fromScreen; i <= toScreen; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(true);
            layout.setChildrenDrawingCacheEnabled(true);
        }
    }

    void clearChildrenCache() {
    	setChildrenDrawnWithCacheEnabled(false);
    	/*
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(false);
        }
        */
    }

    boolean NeedScrollForActionUP = false;
    
    int preDelta  = 0; 
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(Launcher.LOGD)Log.d(TAG,"onTouchEvent ev:"+ev.getAction());
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
            
            if(Launcher.LOGD)Log.d(TAG,"onTouchEvent ACTION_DOWN, mLastMotionX:"+mLastMotionX);
            break;
        case MotionEvent.ACTION_MOVE:
            //final float xx = ev.getX(pointerIndex);
            final float xx = x;
        	if(mTouchState != TOUCH_STATE_SCROLLING){        		
        		final int xDiff = (int) Math.abs(xx - mLastMotionX);
        		final int yDiff = (int) Math.abs(y - mLastMotionY);
                int moveSlop = SLOP;
 

        		boolean xMoved = xDiff > moveSlop;
        	    if(yDiff > (0.577f * xDiff)){
        			xMoved = false;
        			
        			final VelocityTracker velocityTracker = mVelocityTracker;
	                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
	                int velocityX = (int) velocityTracker.getXVelocity();
	                
        			if(yDiff > this.getHeight()/4 || velocityX > 600)
        			{
        			    //change to other category
        				boolean next = (y - mLastMotionY) > 0;
        				mLastMotionY = y;
        				mLastMotionX = xx;
        				mDownMotionX = xx;
        				if(SelectNextCategory(next) == true)
        				{
	        		        mVelocityTracker.clear();   
	        		        
	        		        return true;
        				}
        			}
        		}
        	    
        		if (xMoved) {
        			mTouchState = TOUCH_STATE_SCROLLING ;
        		    mTouchX = mScrollX;
                    mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
        			enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
        			
        			alltime = 0;
                    count = 1;
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
                    final int availableToScroll = getChildAt(getChildCount() - 1).getRight() - mScrollX - getWidth();
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
                    }
                    
                } else {
                    awakenScrollBars();
                }
                if(mLauncher.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    mLinePageIndicator.movePosition(Math.round((1.0f*mLinePageIndicator.getWidth())/ getChildCount()), mLinePageIndicator.getHeight(), deltaOrignal, mLauncher.screenWidth, mLauncher.screenHeight, mCurrentScreen, getChildCount());
                else if(mLauncher.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                	mLinePageIndicator.movePosition(mLinePageIndicator.getWidth(), Math.round((1.0f*mLinePageIndicator.getHeight())/ getChildCount()), deltaOrignal, mLauncher.screenWidth, mLauncher.screenHeight, mCurrentScreen, getChildCount());
            }
            break;
        case MotionEvent.ACTION_UP:
        	
        	alltime = 0;
            count = 1;
               
            if (mTouchState == TOUCH_STATE_SCROLLING) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();
                mVelocityTracker.clear();

                int span  = (int)(x - mLastMotionX);
                float deltaOrignal = (mDownMotionX - x);
            	if(mCurrentScreen > 0 && velocityX > 200)
            	{
      //      		if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX  enter 1111");
            		 if(-100 <= deltaOrignal && deltaOrignal <= 0){
                     	snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen - 1, velocityX);
                     }else{
                    	 snapToScreenWithVelocityX(mCurrentScreen - 1, velocityX);
                     }
            	}
            	else if(mCurrentScreen > 0 && span > (getWidth()/10))
            	{
          //  		if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX  enter 2222 deltaOrignal:"+deltaOrignal);
            		if(-100 <= deltaOrignal && deltaOrignal <= 0){
                     	snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen - 1, velocityX);
                     }else{
                    	 snapToScreenWithVelocityX(mCurrentScreen - 1, velocityX);
                     }
            	}	
            	else if(mCurrentScreen < getChildCount() - 1 && velocityX < -200)
            	{
       //     		if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX  enter 3333");
            		if(0 <= deltaOrignal && deltaOrignal <= 100){
                    	snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen + 1, velocityX);
                    }else{
                    	snapToScreenWithVelocityX(mCurrentScreen + 1, velocityX);
                    }
            	}
            	else if(mCurrentScreen < getChildCount() - 1 && span < -(getWidth()/10))
            	{
    //        		if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX  enter 4444deltaOrignal:"+deltaOrignal);
            		if(0 <= deltaOrignal && deltaOrignal <= 100){
                    	snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen + 1, velocityX);
                    }else{
                    	snapToScreenWithVelocityX(mCurrentScreen + 1, velocityX);
                    }
            	}
            	else
            	{
    //        		if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX  enter 5555");
//            		if(velocityX > 0 && mCurrentScreen > 0){  // move to left
//            			snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen - 1, velocityX);
//            		} else if(velocityX < 0 && (mCurrentScreen < getChildCount() - 1)){   // move to right
//            			snapToScreenWithVelocityXSMALLSLOP(mCurrentScreen + 1, velocityX);
//            		} else {
            			snapToDestination(velocityX);
//            		}
            	}
                	
    //            if(Launcher.LOGD)Log.d(TAG,"onTouchEvent ACTION_UP, velocityX:"+velocityX+"SNAP_VELOCITY:"+SNAP_VELOCITY+" mMaximumVelocity"+mMaximumVelocity);
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
            }
            
            mTouchState = TOUCH_STATE_REST;
            break;
        case MotionEvent.ACTION_CANCEL:
            mTouchState = TOUCH_STATE_REST;
            if(null != mVelocityTracker){
            	mVelocityTracker.clear();
            }
            break;      
        }

    //    if(Launcher.LOGD)Log.d(TAG,"onTouchEvent return true");
        return true;
    } 
    
    private OnCorpusSelectedListener corpusSelect;
    public  void setOnSelectCategoryListener(OnCorpusSelectedListener select)
    {
    	corpusSelect = select;
    }
    private boolean SelectNextCategory(boolean next) {
    	return corpusSelect.selectNexCategory(next);
	}

	public void setPageIndicatorLineStyleView(PageIndicatorLineStyleView pv){
    	mLinePageIndicator = pv;
    }
    
    void snapToScreenWithVelocityX(int whichScreen, int velocityX) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        enableChildrenCache(mCurrentScreen, whichScreen);

        final int screenDelta = Math.abs(whichScreen - mCurrentScreen);
        mNextScreen = whichScreen;
        
        if(mLauncher.dockStyle==1){
        	
        }else{
        	//pageIndicator.drawPageIndicator(mNextScreen,getChildCount());
            mLinePageIndicator.refreshPosition(pageIndicatorRawWidth, pageIndicatorRawHeight, mLauncher.screenWidth, mLauncher.screenHeight, mNextScreen, getChildCount());
        }
        
        View focusedChild = getFocusedChild();
        if (focusedChild != null && screenDelta != 0 && focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }
        
        final int newX = whichScreen * getWidth();
        final int delta = newX - mScrollX;
        int duration = 0;
   //     if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX whichScreen:"+whichScreen+" velocityX:"+velocityX+ "    delta:"+delta);
    	if(false == NeedScrollForActionUP) {
        	bounceInterpolator.setChoice(Workspace.USED_FOR_DEFAULT);
        	duration = 550;
        	if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX 111");
        }else if(Math.abs(delta) <= 7){
        		bounceInterpolator.setChoice(Workspace.USED_FOR_SHORTDISTANCE_36);
        		duration = 100;//3.6
        		if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX 222");
        }else if(Math.abs(delta) < 30){
        		bounceInterpolator.setChoice(Workspace.USED_FOR_SHORTDISTANCE_18);
        		duration = 100;
        		if(Launcher.LOGD)Log.d(TAG, "snapToScreenWithVelocityX 333");
    	}else{
        	bounceInterpolator.setChoice(Workspace.USED_FOR_DEFAULT);
        	int tmpVelocityX = Math.abs(velocityX);
        	if(tmpVelocityX < 400){
        		if(Math.abs(delta) >= (int)(getWidth()*(1-distance)))
        		{
        			 duration = (int)(300* (1.0f*Math.abs(delta)*2)/getWidth());
        		}
        		else
        		{
        			duration = 300;
        		}
        	}else{
        		duration = (int)(600 / ( Math.abs(velocityX) / 1400.0f));
        	}
        }

        awakenScrollBars(duration);
        mScroller.startScroll(mScrollX, 0, delta, 0, duration);
        invalidate();
        mLauncher.resetSkipLongClick();
    }
    
    void snapToScreenWithVelocityXSMALLSLOP(int whichScreen, int velocityX) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        enableChildrenCache(mCurrentScreen, whichScreen);
        
        final int screenDelta = Math.abs(whichScreen - mCurrentScreen);
        mNextScreen = whichScreen;
        if(mLauncher.dockStyle==1){
        	
        }else{
//        	pageIndicator.drawPageIndicator(mNextScreen,getChildCount());
        	mLinePageIndicator.refreshPosition(pageIndicatorRawWidth, pageIndicatorRawHeight, mLauncher.screenWidth, mLauncher.screenHeight, mNextScreen, getChildCount());
        }
        
        View focusedChild = getFocusedChild();
        if (focusedChild != null && screenDelta != 0 && focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }
        
        final int newX = whichScreen * getWidth();
        final int delta = newX - mScrollX;
        int duration = 650;
    	
        awakenScrollBars(duration);
        mScroller.startScroll(mScrollX, 0, delta, 0, duration);
        invalidate();
        
        mLauncher.resetSkipLongClick();
    }
    
    private void scrollToWithoutWallPaperMove(int x, boolean isRight)
    {
    	if(true)
    	{
//    		//post a toast to let user select category    		
//    		QuickLauncher.popupHint(mLauncher, categoryView);    		
//    		mLauncher.mCallStateHandler.postDelayed(new Runnable()
//    		{
//    			public void run()
//    			{
//    				QuickLauncher.dissmissHint();
//    			}
//    		}, 3000);
    		return;
    	}
    	
    	if(Launcher.LOGD)Log.d(TAG,"scrollToWithoutWallPaperMove return in this view");
    	scrollTo(x, 0);
    }    

    final float distance = 0.8f;
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
    
    public HomeScroller getScroller(){
    	return mScroller;
    }

    void snapToScreen(int whichScreen) {
        snapToScreen(whichScreen, 0, false);
    }

    private void snapToScreen(int whichScreen, int velocity, boolean settle) {
    	if(Launcher.LOGD)Log.d(TAG, "snapToScreen whichScreen:"+whichScreen);
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        enableChildrenCache(mCurrentScreen, whichScreen);

        mNextScreen = whichScreen;
//        pageIndicator.drawPageIndicator(mNextScreen,getChildCount());
        mLinePageIndicator.refreshPosition(pageIndicatorRawWidth, pageIndicatorRawHeight, mLauncher.screenWidth, mLauncher.screenHeight, mNextScreen, getChildCount());
        
        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichScreen != mCurrentScreen &&
                focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        }
        
        final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
        final int newX = whichScreen * getWidth();
        final int delta = newX - mScrollX;
        int duration = (screenDelta + 1) * 200;

        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        
        if(delta != 0)
        {
	        velocity = Math.abs(velocity);
	        if (velocity > 0) {
	            duration += (duration / (velocity / BASELINE_FLING_VELOCITY))
	                    * FLING_VELOCITY_INFLUENCE;
	        } else {
	            duration += 100;
	        }
	
	        awakenScrollBars(duration);
	        mScroller.startScroll(mScrollX, 0, delta, 0, duration);
	        invalidate();
        }
    }

    void startDrag(CellLayout.CellInfo cellInfo) {
        View child = cellInfo.cell;
        
        // Make sure the drag was started by a long press as opposed to a long click.
        if (!child.isInTouchMode()) {
            return;
        }
        
        CellLayout current = ((CellLayout) getChildAt(mCurrentScreen));
        current.onDragChild(child);
        invalidate();
    }
    
    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    public void setDragController(DragController dragController) {
        mDragController = dragController;
    }

    public void scrollLeft() {
        if (mScroller.isFinished()) {
            if (mCurrentScreen > 0) snapToScreen(mCurrentScreen - 1);
        } else {
            if (mNextScreen > 0) snapToScreen(mNextScreen - 1);            
        }
    }

    public void scrollRight() {
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

    public boolean allowLongPress() {
        return mAllowLongPress;
    }
    
    public void setAllowLongPress(boolean allowLongPress) {
        mAllowLongPress = allowLongPress;
    }

    void setIndicators(Drawable previous, Drawable next) {
        mPreviousIndicator = previous;
        mNextIndicator = next;
        previous.setLevel(mCurrentScreen);
        next.setLevel(mCurrentScreen);
    }

    private PageIndicatorView pageIndicator;
    void setPageIndicator(PageIndicatorView view) {
        pageIndicator = view;
    }
    
    private View categoryView;
    void setCategoryView(View view) {
        categoryView = view;
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
    
	public void onDropCompleted(View target, boolean success) {
		
	}
	
	View mLoadingBar;
	public void setLoadingBar(View loadingBar){
		mLoadingBar = loadingBar;
	}
	
}
