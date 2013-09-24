package com.android.omshome.screenmanager;

import java.util.ArrayList;

import com.android.omshome.CellLayout;
import com.android.omshome.EditPageActivity;
import com.android.omshome.Launcher;
import com.android.omshome.PageImageAdapter;
import com.android.omshome.ScreenView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import com.android.omshome.R;
import android.os.SystemProperties;

public class NewGallery extends Gallery implements ScreenView.FlingListener{
    private final static String TAG = "NewGallery";
    
    private ScreenView mDownTouchedView;
    private int mDownTouchedPos;   
    
    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_DRAGGING = 2;
    private final static int TOUCH_STATE_SCROLLING = 3;
    private final static int TOUCH_STATE_HOLDING_DRAGVIEW = 4;
    private int TOUCH_STATE = TOUCH_STATE_REST;
    
    private boolean mDownTouchedViewUP = false;
    private int mStartOffsetPos = -1;
    private int mEndOffsetPos = -1;
    private boolean onUpFinished = true;
    private long mLastEndFlingTime = -1;
    private boolean mScrollIntoSlot = true;
    
//    private boolean backThreadFinished = true;
//    private boolean dragThreadFinished = true;
    private int mMinTouchSlop = 6;
    
    public interface PageChangeListener {
        public void onPageChange(int prePage, int curPage);
        public void removePage(int pageIndex);
        public void onPageExchange(int oldPos,int newPos);
        public void changeCurPageText(int curPage);
        public void onStartDrag();
        public void onEndDrag();
        public void flingToLastScreen();
    }

    public NewGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Intent intent = new Intent(Launcher.INTENT_CACHE_SCREEN);
        context.sendBroadcast(intent);
        //mPaddingTop = (int)this.getResources().getDimension(R.dimen.page_padding_top);
        mMinTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public NewGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        Intent intent = new Intent(Launcher.INTENT_CACHE_SCREEN);
        context.sendBroadcast(intent);
        //mPaddingTop = (int)this.getResources().getDimension(R.dimen.page_padding_top);
        mMinTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public NewGallery(Context context) {
        super(context);
        Intent intent = new Intent(Launcher.INTENT_CACHE_SCREEN);
        context.sendBroadcast(intent);
       // mPaddingTop = (int)this.getResources().getDimension(R.dimen.page_padding_top);
        mMinTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }
    
    public void setPaddingTop(int paddingTop)
    {
        mPaddingTop = paddingTop;   
    }
    //have to make sure if all moved action (onToucheEvent-->MOVE action) has finished
    int newPos = -1;
    View nextView = null;
    int nextPos = -1;
    @Override
    void onUp() {
       if(Launcher.LOGD) Log.d(TAG,"entering onUp");
        if(TOUCH_STATE == TOUCH_STATE_DRAGGING)
        {
            if(mDownTouchedView.isFling) return; //if touched view is Fling . do nothing in onUp
            if(Launcher.LOGD) Log.d(TAG,"entering onUp");
            onUpFinished = false;
        	//put view to the right position
            for(int i=0;i<this.getChildCount()-1;i++)
            {
                View view = this.getChildAt(i);
                int mDownTouchedViewLeft = mDownTouchedView.getLeft();
                int viewLeft = view.getLeft();
                if(mDownTouchedViewLeft <= viewLeft )
                {
                    // put mDownTouchedView in front of view
                    //mDownTouchedView.layout(l, t, r, b)
                    nextView = view;
                    nextPos = i;
                    break;	
                }
            } 
            rePlaceDragedView();  
        }
        else if(TOUCH_STATE == TOUCH_STATE_HOLDING_DRAGVIEW)
        {
            super.onUp();
        }
        else
        {
            TOUCH_STATE = TOUCH_STATE_REST;
            super.onUp();
        }
    }
    
   public Runnable replaceDragedViewCallBack = new Runnable(){
        public void run() {   
            rePlaceDragedView();
        }
    };
    
    void rePlaceDragedView()
    {
        if(Launcher.LOGD) Log.d(TAG,"entering replaceDragedView");
        
        // when back view stoped moving. can replaceDragView (start another animation)
        if(mFlingRunnable.mScroller.isFinished() == false)
        {
            if(Launcher.LOGD)Log.d(TAG,"replaceDragedView @@@@@@@@@@@@@@ entering a 50 mil sec delay");
            this.postDelayed(replaceDragedViewCallBack, 50);
        }
        else
        {
            //when press back key to home screen. EditPageActivity onDestroy will clear cache of this view 
            //if after clearing cache. invoke replaceDragedView will cause NullPointerException
            // so used try{}catch(Exception e) to catch NullPointException. avoid force close
           if(pageListener != null && ((EditPageActivity)pageListener).isFinishing())
           {
               return;
           }
            mDownTouchedViewUP = true;
            mDownTouchedView.setFlingListener(this);
            if( nextView != null)
            { 
               int rightL = nextView.getLeft();//-nextView.getWidth()-getSpacing();
               int rightT = nextView.getTop();
               int rightR = nextView.getRight();
               int rightB = nextView.getBottom();
               if(nextPos > 0)
               {
                   //base on preview's location
                   View preView = this.getChildAt(nextPos-1);
                   rightL = preView.getLeft()+preView.getWidth()+getSpacing();
                   rightT = preView.getTop();
                   rightR = rightL + preView.getWidth();
                   rightB = preView.getBottom();
               }
               
                //downTouchedView is the last View              
                detachViewFromParent(this.getChildCount()-1);
                addViewInLayout(mDownTouchedView, nextPos, mDownTouchedView.getLayoutParams());
                
                int l = mDownTouchedView.getLeft();
                int t = mDownTouchedView.getTop();
                
                int deltaX = rightL - l;
                int deltaY = rightT - t;
                mStartOffsetPos = nextPos+1;
                mEndOffsetPos   = this.getChildCount()-1;
                if(Launcher.LOGD)Log.d(TAG,"start-thread-mDownTouchedView rePlaceDragedView");
                mDownTouchedView.getFlingRunnable().startUsingDistance(deltaX,deltaY);
                
                //move to the right position;
                int nextViewPositionL = rightL + mDownTouchedView.getWidth()+getSpacing();
                int newViewCurL       = nextView.getLeft();
                int movedX = nextViewPositionL - newViewCurL;
                //int movedX = mDownTouchedView.getWidth()+getSpacing();
                mScrollIntoSlot = false;
                if(Launcher.LOGD)Log.d(TAG,"start-thread-back rePlaceDragedView");
                
                mFlingRunnable.startUsingDistance(movedX);
                newPos = nextPos;
               
            }
            else 
            {
                //means the mDownTouchedView was dragged to the last position
                View preView = this.getChildAt(this.getChildCount()-2);
                int rightL = preView.getLeft()+preView.getWidth()+getSpacing();
                int rightT = preView.getTop();
//                int rightR = preView.getRight();
//                int rightB = preView.getBottom();
                
                preView = null;
                int l = mDownTouchedView.getLeft();
                int t = mDownTouchedView.getTop();
                
                int deltaX = rightL - l;
                int deltaY = rightT - t;
                if(Launcher.LOGD)Log.d(TAG,"start-thread-mDownTouchedView rePlaceDragedView");
                mDownTouchedView.getFlingRunnable().startUsingDistance(deltaX,deltaY);
                newPos = -1;
            }
            
            nextView = null;
            
            if(newPos==-1 && mDownTouchedPos==(getChildCount()-1))
            {
                // move last one screen to last one screen
                return;
            }
            else
            {
                //old pos != new pos. send exchange message
                if(mDownTouchedPos != newPos)
                {  
                    ArrayList<Bitmap> imageList = ((PageImageAdapter)this.getAdapter()).getAllBitmap();
                    Bitmap bitmap = imageList.get(mDownTouchedPos);
                    imageList.remove(mDownTouchedPos);
                    if(newPos == -1)
                    {

                        imageList.add(bitmap);
                    }
                    else
                    {
                        imageList.add(newPos,bitmap);
                    }
                    pageListener.onPageExchange(mDownTouchedPos,newPos);
                }
            }
        }   
        
    }   

    
    //don't process long press event
    @Override
    public void onLongPress(MotionEvent e) {      
        return;
    }
    
    //item click event
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if(Launcher.LOGD)Log.d(TAG,"===entering onSingleTagUp TOUCH_STATE="+TOUCH_STATE + "mFlingRunnable.mScroll.isFinished="+mFlingRunnable.mScroller.isFinished());
        if(TOUCH_STATE == TOUCH_STATE_REST  && mFlingRunnable.mScroller.isFinished())
        {
            return super.onSingleTapUp(e);
        }
        else
        {
            return false;
        }
    }
    
    float mLastX = Float.MAX_VALUE;
    float mLastY = Float.MAX_VALUE;
    float mDownX = Float.MAX_VALUE;
    float mDownY = Float.MAX_VALUE;
    
    @Override
    public boolean onDown(MotionEvent e) {
        //record the initial touched position
        mLastX = e.getX();
        mLastY = e.getY();
        return super.onDown(e);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	float mCurX = event.getX();
    	float mCurY = event.getY();
    	
    	boolean retValue = false;
    	if(Launcher.LOGD)Log.d(TAG,"entering onTouchEvent TOUCH_STATE="+TOUCH_STATE+"==onUpFinished="+onUpFinished+"===mDownTouchedView.isFling="+(mDownTouchedView!=null?mDownTouchedView.isFling:null));
    	if(TOUCH_STATE == TOUCH_STATE_DRAGGING  && ( onUpFinished==false || (mDownTouchedView != null && mDownTouchedView.isFling)))
        {
            // if fling is not finished,the touch_state is still dragging. don't act dragging
    	    if(Launcher.LOGD)Log.d(TAG, " screen was locked onUpFinished="+onUpFinished +"==mDownTouchedView.isFling "+mDownTouchedView.isFling );
            
            return true;
        }
    	
        int action = event.getAction();
        switch(action)
        {
            case MotionEvent.ACTION_DOWN:
            {
                mDownX = mCurX;
                mDownY = mCurY;
                enableChildrenCache();
                break;
            }
            case MotionEvent.ACTION_MOVE:
            {
                int mScrollX = (int) (mCurX - mLastX);
                int mScrollY = (int) (mCurY - mLastY);
                
                float mMovedX = Math.abs(mCurX-mDownX);
                float mMovedY = Math.abs(mCurY-mDownY);
                
                // mMovedX/mMovedY < tg45
                boolean canDrag = (mMovedX <  mMovedY)  && (mMovedY > mMinTouchSlop);
                if(Launcher.LOGD)Log.d(TAG,"===== canDrag="+canDrag+"===mMovedX="+mMovedX+"==mMovedY="+mMovedY + "==mMinTouchSlop="+mMinTouchSlop);
                if(TOUCH_STATE == TOUCH_STATE_REST)
                {
                   if(/*mMovedY > mTouchedSlop*/ canDrag && this.getTouchedPosition() == mSelectedPosition && this.getChildCount()>1) // if is center view start drag
                    {
                        TOUCH_STATE = TOUCH_STATE_DRAGGING;
                        mDownTouchedView = (ScreenView)this.getTouchedDownView();
                        mDownTouchedPos = this.getTouchedPosition();
                        int l = mDownTouchedView.getLeft();
                        int t = mDownTouchedView.getTop();
                        int r = mDownTouchedView.getRight();
                        int b = mDownTouchedView.getBottom();
                        mDownTouchedView.layout(l+mScrollX, t+mScrollY, r+mScrollX, b+mScrollY);
                        //invalidate();
                        if(Launcher.LOGD)Log.d(TAG,"first time entering draging mDownTouchedPos="+mDownTouchedPos);
                        startDrag(mDownTouchedView,mDownTouchedPos);
                    } 
                }
                else if (TOUCH_STATE == TOUCH_STATE_HOLDING_DRAGVIEW)
                {
                    Rect mFrame = new Rect();
                    mDownTouchedView.getHitRect(mFrame);
                    if (mFrame.contains((int)event.getX(), (int)event.getY())) {
                        // if touched the last view should set touch_state to
                        // touch_state_dragging
                        TOUCH_STATE = TOUCH_STATE_DRAGGING;
                        mScrollIntoSlot = false;
                    }
                }
                else
                {
                    if(mDownTouchedView != null && TOUCH_STATE == TOUCH_STATE_DRAGGING && /*mFlingRunnable.mScroller.isFinished() &&*/ mLastX != Float.MAX_VALUE && mLastY != Float.MAX_VALUE)
                    {
                        int l = mDownTouchedView.getLeft();
                        int t = mDownTouchedView.getTop();
                        int r = mDownTouchedView.getRight();
                        int b = mDownTouchedView.getBottom();
                        mDownTouchedView.layout(l+mScrollX, t+mScrollY, r+mScrollX, b+mScrollY);
                        
                        invalidate();
                        // if mDownTouchedView was moved to the left edge of Gallery gallery scroll to right
                        // if mDownTouchedview was moved to the right edge of Gallery gallery scroll to left 
//                        int viewCenter = getCenterOfView(mDownTouchedView);
                        int viewLeft =  mDownTouchedView.getLeft()  + 1*mDownTouchedView.getWidth()/3;
                        int viewRight = mDownTouchedView.getLeft()  + 2*mDownTouchedView.getWidth()/3;
                        final int galleryLeft = mPaddingLeft;
                        final int galleryRight = getWidth() + mPaddingLeft+mPaddingRight;
                        int deltaX = mDownTouchedView.getWidth()+getSpacing();
                        long curTime = System.currentTimeMillis();
                        long interval = curTime - mLastEndFlingTime;
                        if(mScrollX < 0) //move to left
                        {
                            if( viewLeft < galleryLeft )
                            { 
                                //if is in the first pos don't scroll to left && isAllView isFling == false
                                
                                if(mSelectedPosition > 0 && mFlingRunnable.mScroller.isFinished() &&mLastEndFlingTime > 600)
                                {
                                    if(Launcher.LOGD) Log.d(TAG,"scroll to left mSelectedPosition="+mSelectedPosition + "===intervalTime="+interval);
                                    mStartOffsetPos = 0;
                                    mEndOffsetPos   = this.getChildCount()-2;
                                    if(Launcher.LOGD)Log.d(TAG,"start-thread-back onTouchEvent");
                                    mFlingRunnable.startUsingDistance(deltaX);
                                }
                                
                            }
                        }
                        else if(mScrollX > 0) //move to right
                        {
                            if( viewRight > galleryRight )
                            {
                                 //if is In last Position //don't scroll to right
                                if(mSelectedPosition<(this.getChildCount()-2) &&mFlingRunnable.mScroller.isFinished() && mLastEndFlingTime > 600)
                                {
                                    if(Launcher.LOGD) Log.d(TAG," scroll to left mSelectedPosition="+mSelectedPosition + "===intervalTime="+interval);
                                    mStartOffsetPos = 0;
                                    mEndOffsetPos   = this.getChildCount()-2;
                                    if(Launcher.LOGD)Log.d(TAG,"start-thread-back onTouchEvent");
                                    mFlingRunnable.startUsingDistance((-1) * deltaX);
                                }
                            }
                        }
                        retValue = true;                        
                    }  
                }        
                break;
            }
        }
        
        mLastX = mCurX;
        mLastY = mCurY;  
        super.onTouchEvent(event);
        return true;
    }
    
    

    @Override
    protected void scrollIntoSlots() {
        if((TOUCH_STATE == TOUCH_STATE_DRAGGING && mDownTouchedViewUP == true) || mScrollIntoSlot== false)
        {
            if(Launcher.LOGD)Log.d(TAG,"scrollIntoSlots == do nothing excepted doing onFinishedMovement");
            //when dragging view and entering onUp method. don't excute scrollIntoSlots method
            //just finished thread FlingRunnable 
            onFinishedMovement();
        }
        else
        {
            super.scrollIntoSlots();
        }
    }
    
    @Override
    void setSelectionToCenterChild() {
        if(TOUCH_STATE == TOUCH_STATE_DRAGGING || TOUCH_STATE == TOUCH_STATE_HOLDING_DRAGVIEW) //isDragging But mDownTouchedView is not animation
        {
            if(mDownTouchedViewUP == true)
            {
            	if(Launcher.LOGD) Log.d(TAG,"mDownTouchedViewUP setSelectionToCenterChild do nothing");            
            	
            	return;
            }
            
            View selView = mSelectedChild;
            if (mSelectedChild == null) return;
            
            int galleryCenter = getCenterOfGallery();
            
            int closestEdgeDistance = Integer.MAX_VALUE;
            int newSelectedChildIndex = 0;
            for (int i = getChildCount() - 2; i >= 0; i--) {
                
                View child = getChildAt(i);
                
                if (child.getLeft() <= galleryCenter && child.getRight() >=  galleryCenter) {
                    // This child is in the center
                    newSelectedChildIndex = i;
                    break;
                }
                
                int childClosestEdgeDistance = Math.min(Math.abs(child.getLeft() - galleryCenter),
                        Math.abs(child.getRight() - galleryCenter));
                if (childClosestEdgeDistance < closestEdgeDistance) {
                    closestEdgeDistance = childClosestEdgeDistance;
                    newSelectedChildIndex = i;
                }
            }
            mFirstPosition = 0;
            int newPos = mFirstPosition + newSelectedChildIndex;
            if (newPos != mSelectedPosition) {
                setSelectedPositionInt(newPos);
                setNextSelectedPositionInt(newPos);
                checkSelectionChanged();
            }
        }
        else
        {
            super.setSelectionToCenterChild();
        }
    }

    @Override
    public void setSelectedPositionInt(int position) {
        
        if(TOUCH_STATE == TOUCH_STATE_DRAGGING)
	    {
	        if(mDownTouchedViewUP == false)
	        {
	        	for(int i=0;i<getChildCount();i++)
	    		{
	    			View oldSelectedChild = getChildAt(i);	    			
			        oldSelectedChild.setSelected(false);			        
			        oldSelectedChild.setFocusable(false);	    			
	    		}
	        	
		        mSelectedPosition = position;
		        mSelectedChild = this.getChildAt(position);
		        
		        mSelectedChild.setSelected(true);
		        mSelectedChild.setFocusable(true);
		        
		        mSelectedRowId = getItemIdAtPosition(position);
		        pageListener.changeCurPageText(mSelectedPosition);
	        }
	    }
	    else
	    {
	        super.setSelectedPositionInt(position);
	    }
    }
    
    

    @Override
    int getLimitedMotionScrollAmount(boolean motionToLeft, int deltaX) {
        if(TOUCH_STATE == TOUCH_STATE_DRAGGING)
        {
            return deltaX;
        }
        else if(TOUCH_STATE == TOUCH_STATE_HOLDING_DRAGVIEW)
        {
            int extremeItemPosition = motionToLeft ? mItemCount - 2 : 0;
            View extremeChild = getChildAt(extremeItemPosition - mFirstPosition);
            if (extremeChild == null) {
                return deltaX;
            }
            
            int extremeChildCenter = getCenterOfView(extremeChild);
            int galleryCenter = getCenterOfGallery();
            
            if (motionToLeft) {
                if (extremeChildCenter <= galleryCenter) {
                    // The extreme child is past his boundary point!
                    return 0;
                }
            } else {
                if (extremeChildCenter >= galleryCenter) {
                    // The extreme child is past his boundary point!
                    return 0;
                }
            }
            
            int centerDifference = galleryCenter - extremeChildCenter;
            return motionToLeft? Math.max(centerDifference, deltaX): Math.min(centerDifference, deltaX); 
        }
        else
        {
            return super.getLimitedMotionScrollAmount(motionToLeft, deltaX);
        }
    }
    
    
    @Override
    void offsetChildrenLeftAndRight(int offset) {
        if(TOUCH_STATE == TOUCH_STATE_DRAGGING)
        {
            // when touch_state == touch_state_dragging
            // just offset the pointed view child
            if(mStartOffsetPos == -1 ||  mEndOffsetPos == -1) return;
            
            if(offset == 0 && mDownTouchedViewUP == false)
            {
                if(Launcher.LOGD)Log.d(TAG,"entering offsetchildrenLeftAndRight offset=0");
                //offset means child view has moved to the expected position. stop FlingRun right now don't scrollIntoSlot
                mShouldStopFling = true; 
            }
            else
            {
                for(int i=mStartOffsetPos;i<= mEndOffsetPos;i++)
                {
                    View view = this.getChildAt(i);
                    view.offsetLeftAndRight(offset);
                }
            }
        }
        else if(TOUCH_STATE == TOUCH_STATE_HOLDING_DRAGVIEW)
        {
            // when touch_state == touch_state_holding_dragview
            // just scroll view before the last one (the last one was the dragged view)
            for(int i=0;i<=this.getChildCount()-2;i++)
            {
                View view = this.getChildAt(i);
                view.offsetLeftAndRight(offset);
            }
        }
        else
        {
           // if(Launcher.LOGD) Log.d(TAG,"##################### entering super offsetChilrenLeftAndRight Dragging scroll"+offset);
            super.offsetChildrenLeftAndRight(offset);
        }
    }
    /**
     * after all view end scroll. call this method to reset all init param as default value
     * if touched view is finished before . reset all init param as default value here
     * else reset all init param in onScrollEnd method.
     * also see method onScrollEnd
     */
	@Override
	void onFinishedMovement() {
	    if(Launcher.LOGD) Log.d(TAG,"end-thread-back TOUCH_STATE="+TOUCH_STATE_DRAGGING + "=condition="+(mDownTouchedView !=null && mDownTouchedView.getFlingRunnable().mScroller.isFinished() && mDownTouchedViewUP)+"==mSelectedPosition="+mSelectedPosition);
	    if(TOUCH_STATE == TOUCH_STATE_DRAGGING)
	    {   
            if(mDownTouchedView !=null && mDownTouchedView.getFlingRunnable().mScroller.isFinished() && mDownTouchedViewUP)
            {
                //Log.d(TAG,"######################### onFinishedMovement after 6 seconds");
               // reset 
                mDownTouchedView.mFlingListener = null;
                mDownTouchedView = null;
                TOUCH_STATE = TOUCH_STATE_REST;
                mDownTouchedViewUP = false;
                onUpFinished = true;
                mScrollIntoSlot = true;
                setSelectedPositionInt(mSelectedPosition);
                setNextSelectedPositionInt(mSelectedPosition);
                pageListener.onEndDrag();
                
                invalidate();
            }
	        	
	    
	        mLastEndFlingTime = System.currentTimeMillis();
            mStartOffsetPos = -1;
            mEndOffsetPos   = -1;  
	    }
	    else
	    {
	        mStartOffsetPos = -1;
            mEndOffsetPos   = -1; 
	        super.onFinishedMovement();
	    }
		
	}
	
	

	@Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,float distanceY) {
	    if(TOUCH_STATE == TOUCH_STATE_REST)
	    {
	        //first time move
	        TOUCH_STATE = TOUCH_STATE_SCROLLING;
	    }
	    else if(TOUCH_STATE == TOUCH_STATE_DRAGGING)
        {
            return false;
        }
	    return super.onScroll(e1, e2, distanceX, distanceY);
    }
    
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
    	if(Launcher.LOGD) Log.d(TAG," entering onFling "+TOUCH_STATE);
        if(TOUCH_STATE == TOUCH_STATE_DRAGGING)
        {
           
            // fling touched view to the bottom if drag view is above the ordinary position when velocityY over 1200 act fling
            // if drag view is below the ordinary position as long as velocityY > 0 act fling
            int ordinaryTop = mPaddingTop;
            int downTouchedViewTop = mDownTouchedView.getTop();
            if(Launcher.LOGD) Log.d(TAG," fling velocityX" + velocityX + "==velocityY="+velocityY+"== ordinaryTop="+ordinaryTop+"==downTouchedViewTop="+downTouchedViewTop);
            if(( downTouchedViewTop > ordinaryTop && velocityY > 0)  || (downTouchedViewTop <= ordinaryTop && velocityY > 2000))
            {
                mDownTouchedView.isFling = true;
                mDownTouchedView.setFlingListener(this);
                // fling touched view to the bottom
                int initX = mDownTouchedView.getLeft()+mDownTouchedView.getWidth()/2;
                int initY = mDownTouchedView.getTop()+mDownTouchedView.getHeight()/2;
                
//                int minX = initX;
//                int minY = initY;
                
                final int bottomHeight = (int)this.getResources().getDimension(R.dimen.ep_button_bar_height);
                int l = mDownTouchedView.getLeft();
                int t = mDownTouchedView.getTop();
                
                int desL = (getWidth()-mDownTouchedView.getWidth())/2;
                int desT = getHeight() - bottomHeight;
                if(Launcher.LOGD)Log.d(TAG,"==desT is="+desT);
//                int maxX = desL + mDownTouchedView.getWidth()/2;
//                int maxY = desT + mDownTouchedView.getHeight()/2;
                if(Launcher.LOGD)Log.d(TAG,"start-thread-mDownTouchedView onFling");
                mDownTouchedView.getFlingRunnable().startUsingDistance(desL - l, desT - t);//startUsingVelocity(initX,initY,(int)-velocityX, (int)-velocityY, minX, maxX, minY, maxY);
                
            }
            else
            {
                mDownTouchedView.isFling = false;
            }
            return false;
        }
        else
        {
            if(Launcher.LOGD) Log.d(TAG,"excute super fling");
            return super.onFling(e1, e2, velocityX, velocityY);     
        }   
    }
    
    PageChangeListener pageListener;
    public void setPageChangeListener(PageChangeListener listener)
    {
        pageListener = listener;
    }

    public int getCurPageIndex() {
        return getSelectedItemPosition();
    }
    
    
    public void startDrag(ScreenView view,int viewPos) {
        if(Launcher.LOGD)Log.d(TAG," startDrag");
        //offsetNextView to current position
        TOUCH_STATE = TOUCH_STATE_DRAGGING;
        
        mDownTouchedView = (ScreenView)view;
        mDownTouchedPos = viewPos;
        onDragChild(view,viewPos);
        
    }
    
    private void onDragChild(View child,int viewPos) {
        int deltaX = 0;
        if(mDownTouchedPos == (this.getAdapter().getCount()-1))
        {
            //the touched view is the last view
            //move the preview to Right	
            int preViewIndex = mDownTouchedPos - mFirstPosition - 1;
            View preView = getChildAt(preViewIndex);
            deltaX = preView.getWidth()+getSpacing();//mDownTouchedView.getLeft() - preView.getLeft();
            
            mStartOffsetPos = 0;
            mEndOffsetPos = preViewIndex;
        	setSelectedPositionInt(mDownTouchedPos-1); // change mSelectedChild and mSelectedPosition        	
        	mOldSelectedPosition = (mDownTouchedPos-1);
        	mScrollIntoSlot = false;
        	if(Launcher.LOGD)Log.d(TAG,"start-thread-back onDragChild");
        	mFlingRunnable.startUsingDistance(deltaX);
        	
        }
        else
        {
        	//move the next view to left and all nextview position-1, attached the dragged view to the last position
        	detachViewFromParent(mDownTouchedPos);
        	addViewInLayout(mDownTouchedView, -1,mDownTouchedView.getLayoutParams());
        	
        	//if(Launcher.LOGD)  Log.d(TAG,"################### start UsingDistance=== startPos="+ mStartOffsetPos+"===endPos="+mEndOffsetPos);
        	View nextView = getChildAt(mDownTouchedPos);
        	if(nextView != null)
        	{
        	    deltaX = -nextView.getWidth()-getSpacing();//mDownTouchedView.getLeft() - nextView.getLeft();   	    
        	    mStartOffsetPos = mDownTouchedPos;
                mEndOffsetPos = this.getChildCount()-2; 
                setSelectedPositionInt(mDownTouchedPos); // change mSelectedChild and mSelectedPosition
                mScrollIntoSlot = false;
                if(Launcher.LOGD)Log.d(TAG,"start-thread-back onDragChild");
                mFlingRunnable.startUsingDistance(deltaX);
        	}
        } 
        
        pageListener.onStartDrag();
    }
	
	public void onFlingEnd() {	    
		if(Launcher.LOGD) Log.d(TAG,"end-thread-onDownTouchedView onFlingEnd");
	
		// when fling end. just set reset touche_state as touch_state_dragging
        mLastEndFlingTime = System.currentTimeMillis();
        TOUCH_STATE = TOUCH_STATE_HOLDING_DRAGVIEW;
        mScrollIntoSlot = true;
        if(TOUCH_STATE == TOUCH_STATE_HOLDING_DRAGVIEW)
        {   
            invalidate();
            return ;
        }
    }
	
    public void onValidate() {
        //must re-draw gallery
        invalidate();
    }
   
    /**
     * after TouchedView end scroll. call this method to reset all init param as default value
     * if all view stopped scroll before . reset all init param as default value here
     * else reset all init param in onFinishedMovement method.
     * also see method onFinishedMovement
     */
    public void onScrollEnd() {
       
        mLastEndFlingTime = System.currentTimeMillis();
        
        if(mDownTouchedView != null)
        {
            mDownTouchedView.mFlingListener = null;
        }
        
        if(Launcher.LOGD) Log.d(TAG,"end-thread-mDownTouchedView condition="+(TOUCH_STATE == TOUCH_STATE_DRAGGING && mFlingRunnable.mScroller.isFinished())+"==mSelectedPosition="+mSelectedPosition);
        if(TOUCH_STATE == TOUCH_STATE_DRAGGING && mFlingRunnable.mScroller.isFinished())
        {
            mDownTouchedViewUP = false;
            onUpFinished = true;
            TOUCH_STATE = TOUCH_STATE_REST;
            mLastX = Float.MAX_VALUE;
            mLastY = Float.MAX_VALUE;
            
            mStartOffsetPos = -1;
            mEndOffsetPos = -1;
            mDownTouchedView = null;
            mDownTouchedPos = -1;   
            mScrollIntoSlot = true;
            setSelectedPositionInt(mSelectedPosition);
            setNextSelectedPositionInt(mSelectedPosition);
            
            pageListener.onEndDrag();
            
            invalidate();
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        
        //Log.d(TAG, "getChildDrawingOrder child drawing order childCount="+childCount+"==child Index="+i+"==onUpFinished="+onUpFinished+"==newPos="+newPos);
        int orderIndex = i;
        if(!onUpFinished && newPos!=-1)
        {
            //newPos child will be drawed at last time.
            //and all child after newPos should be drawed before child which locate in newPos
           
            if(i == newPos)
            {
                orderIndex = newPos+1;   
            }
            else if(i == newPos+1)
            {
                orderIndex =  newPos;
            }
        }
        
        //Log.d(TAG, "getChildDrawingOrder order Index="+orderIndex);
        return orderIndex;
        
    }

    public void enableChildrenCache() {        
        setChildrenDrawnWithCacheEnabled(true);
        setChildrenDrawingCacheEnabled(true);        
    }
    
   /* @Override
    protected void dispatchDraw(Canvas canvas) {
        Log.d(TAG,"addPage dispatchDraw=="+needFlingToLast);
        super.dispatchDraw(canvas);
        if(needFlingToLast)
        {
            pageListener.flingToLastScreen();
            needFlingToLast = false;
        }
    }*/

    public void setLastViewAsSelectedScreen() {
       this.setSelectedPositionInt(this.getAdapter().getCount());
       this.setNextSelectedPositionInt(this.getAdapter().getCount());
    }
    
    public void clearCache()
    {
        this.detachAllViewsFromParent();
    }
    
}
