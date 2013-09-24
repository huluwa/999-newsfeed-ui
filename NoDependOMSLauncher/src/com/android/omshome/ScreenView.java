package com.android.omshome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Scroller;

public class ScreenView extends View{
    
    private int position;
    int mAnimationDuration = 400;
    private final static String  TAG = "ScreenView";
    private FlingRunnable mFlingRunnable = new FlingRunnable();
    public boolean isFling = false; // isFling == true means screenview is Fling. else means screenview is Scrolling
   
    static Bitmap dr;
    static Bitmap foucsdr;
    Bitmap   source;
    int      imagePadding = 0;
    
    public void setBitmap(Bitmap bmp)
    {
    	source = bmp;
    }
    
    Rect backRect = null;
    Rect sourceRect = null;
    boolean noUseNow = false;
    
    public void setBackgroundNull()
    {
    	if(Launcher.LOGD)Log.d(TAG, "setBackgroundNull");
    	if(dr != null)
    	{
    		dr.recycle();
    		dr = null;
    	}
    	
    	if(foucsdr != null)
    	{
    		foucsdr.recycle();
    		foucsdr = null;
    	}
    	
    	noUseNow = true;
//    	requestLayout();
    	invalidate();
    } 
    
    @Override
	protected void onDraw(Canvas canvas) 
    {
    	if(backRect == null )
    	{
    		backRect = new Rect(0, 0,getRight()-getLeft(), getBottom()-getTop());
    	}
    	int dWidth =  this.getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
    	int dHeight = this.getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
    	int vWidth = source.getWidth();
    	int vHeight = source.getHeight();
    	
    	float scaleWidth = vWidth*1.0f/dWidth;
    	float scaleHeight = vHeight*1.0f/dHeight;
    	
    	
    	if(Launcher.LOGD)Log.d(TAG,"vWidht="+vWidth+"==vHeight="+vHeight+"==dWidth="+dWidth+"==dHeight="+dHeight+"==scaleWidht="+scaleWidth+"==scaleHeight="+scaleHeight);
    	float scale = Math.max(scaleWidth,scaleHeight);
    	float finalWidth = (int)(vWidth/scale);
        float finalHeight = (int)(vHeight/scale);
        float left = (dWidth-finalWidth)/2.0f + getPaddingLeft();
        float top = (dHeight-finalHeight)/2.0f + getPaddingTop();
    	if(sourceRect == null )
    	{
    		sourceRect = new Rect((int)left,(int)top,(int)(finalWidth+left),(int)(finalHeight+top));
    	}
    	
    	//draw background
    	if(noUseNow == false)
    	{
    		if(isFocusable())
    		{
    			if(foucsdr != null)
    	    	{
    	    		canvas.drawBitmap(foucsdr, new Rect(0, 0, foucsdr.getWidth(), foucsdr.getHeight()), backRect, null);
    	    	}
    		}
    		else
    		{
		    	if(dr != null)
		    	{		    	
		    		canvas.drawBitmap(dr, new Rect(0, 0, dr.getWidth(), dr.getHeight()), backRect, null);
		    	}
    		}
    	}
    	
    	//draw screen
    	if(source != null)
    	{
    		if(Launcher.LOGD)Log.d(TAG, "onDraw   draw bitmap");
    		
    		/*Matrix mMatrix = new Matrix();
    		mMatrix.setScale(scale,scale);
    		float dx = (dWidth - vWidth * scale) * 0.5f;
            float dy = (dHeight - vHeight * scale) * 0.5f;
    		
    		mMatrix.postTranslate(dx, dy);
    		canvas.drawBitmap(source, mMatrix, null);*/
    		
    		//canvas.drawBitmap(source, (dWidth-finalWidth)/2, (dHeight/finalHeight)/2, null);
    		canvas.drawBitmap(source,new Rect(0, 0, source.getWidth(), source.getHeight()), sourceRect, null);
    	}
	}

	private boolean isCurrentFocus() {		
		return iamfoucse == true;
	}

	public FlingRunnable getFlingRunnable()
    {
    	return mFlingRunnable;
    }
    
    public ScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(imagePadding == 0)
    	{
    		imagePadding = (int)getContext().getResources().getDimension(R.dimen.page_edit_image_padding);
    	}
        loadBackground();        
    }
    
    private void loadBackground()
    {
        if(dr == null)
        {
            dr = BitmapFactory.decodeResource(getContext().getResources(),  R.drawable.homescreen_menu_page_edit_bg);
        }
        if(foucsdr == null)
        {
        	foucsdr = BitmapFactory.decodeResource(getContext().getResources(),  R.drawable.homescreen_menu_page_edit_bg_focus);
        }
    }

    public ScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(imagePadding == 0)
    	{
    		imagePadding = (int)getContext().getResources().getDimension(R.dimen.page_edit_image_padding);
    	}
        loadBackground();
    }

    public ScreenView(Context context) {
        super(context);
        if(imagePadding == 0)
    	{
    		imagePadding = (int)getContext().getResources().getDimension(R.dimen.page_edit_image_padding);
    	}
        loadBackground();
    }
    
    public void setIndex(int pos)
    {
        this.position = pos;
    }
    
    public void unbind()
    {
    	mFlingRunnable = null;
    	source         = null;
    	
    	if(dr != null)
    	{
    		dr.recycle();
    		dr = null;
    	}
	   if(foucsdr != null)
       {
           foucsdr.recycle();
           foucsdr = null;
       }
    }
    
    public int getIndex()
    {
        return position;
    }
    
    public class FlingRunnable implements Runnable {
        /**
         * Tracks the decay of a fling scroll
         */
       public Scroller mScroller;

        /**
         * X value reported by mScroller on the previous fling
         */
        private int mLastFlingX;
        private int mLastFlingY;

        public FlingRunnable() {
            mScroller = new Scroller(getContext(),new AccelerateDecelerateInterpolator());
        }

        private void startCommon() {
            // Remove any pending flings
            removeCallbacks(this);
        }
        
        public void startUsingVelocity(int initialX,int initialY,int initialVelocitx,int initialVelocity,int minX,int maxX,int minY,int maxY) {
            if (initialVelocitx == 0 && initialVelocity == 0) return;
            
            startCommon();
            
            //int initialX = initialVelocitx < 0 ? Integer.MAX_VALUE : 0;
           // int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            mLastFlingX = initialX;
            mLastFlingY = initialY;
            mScroller.fling(initialX, initialY, initialVelocitx, initialVelocity,
                    minX, maxX, minY, maxY);
            post(this);
        }

        public void startUsingDistance(int distanceX,int distanceY) {
            if(Launcher.LOGD)Log.d(TAG,"##################### NewGallery startUsingDistance mscroll is finished?"+mScroller.isFinished() +"=== distanceX="+ distanceX +"===distanceY="+distanceY);          
            startCommon();
            
            mLastFlingX = 0;
            mLastFlingY = 0;
            mScroller.startScroll(0, 0, -distanceX, -distanceY, mAnimationDuration);
            post(this);
        }
       
        
        public void stop(boolean scrollIntoSlots) {
            removeCallbacks(this);
            endFling(scrollIntoSlots);
        }
        
        private void endFling(boolean scrollIntoSlots) {
            /*
             * Force the scroller's status to finished (without setting its
             * position to the end)
             */
            mScroller.forceFinished(true);
            if(mFlingListener != null)
            {
                if(isFling)
                {
                    mFlingListener.onFlingEnd();   
                }
                else
                {
                    mFlingListener.onScrollEnd();
                }
                
            }
            isFling = false;
        }

        public void run() {

            final Scroller scroller = mScroller;
            boolean more = scroller.computeScrollOffset();
            final int x = scroller.getCurrX();
            final int y = scroller.getCurrY();
            // Flip sign to convert finger direction to list items direction
            // (e.g. finger moving down means list is moving towards the top)
            int deltaX = mLastFlingX - x;
            int deltaY = mLastFlingY - y;

            // Pretend that each frame of a fling scroll is a touch scroll
           /* if (delta > 0) {
                // Moving towards the left. Use first view as mDownTouchPosition
                // Don't fling more than 1 screen
                delta = Math.min(getWidth() - mPaddingLeft - mPaddingRight - 1, delta);
            } else {
                // Moving towards the right. Use last view as mDownTouchPosition
                int offsetToLast = getChildCount() - 1;
                mDownTouchPosition = mFirstPosition + offsetToLast;

                // Don't fling more than 1 screen
                delta = Math.max(-(getWidth() - mPaddingRight - mPaddingLeft - 1), delta);
            }*/
            
            offsetLeftAndRight(deltaX);
            offsetTopAndBottom(deltaY);
            
            //invalidate();
            if(mFlingListener != null)
            {
                mFlingListener.onValidate();
            }

            if (more) {
                mLastFlingX = x;
                mLastFlingY = y;
                post(this);
            } else {
               endFling(true);
            }
        }       
    }
    
    
    public FlingListener mFlingListener;
    public void setFlingListener(FlingListener flingListener)
    {
        if(mFlingListener == null)
        {
            mFlingListener = flingListener;
        }
    }
    
    public interface FlingListener
    {
        public void onFlingEnd();
        public void onValidate();
        public void onScrollEnd();
    }

    boolean iamfoucse = false;
	public void setNeedFoucs(boolean b) {
		iamfoucse = b;		
	}
    

}
