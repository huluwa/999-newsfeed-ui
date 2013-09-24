package com.android.omshome;

import com.android.omshome.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class NewDragView extends ViewGroup{
    private static final String TAG = "NewDragView";
    private int mWidth = -1;
    private int mHeight = -1;
    
    private boolean mPortrait; 

    private int mCellWidth;
    private int mCellHeight;
    
    private int mLongAxisStartPadding;
    private int mLongAxisEndPadding;

    private int mShortAxisStartPadding;
    private int mShortAxisEndPadding;

    private int mShortAxisCells;
    private int mLongAxisCells;

    private int mWidthGap;
    private int mHeightGap;
    
    public NewDragView(Context context, int width, int height) {
        this(context, null);
        mWidth = width;
        mHeight = height;    
    }
    
    public NewDragView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewDragView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mCellWidth = context.getResources().getDimensionPixelSize(R.dimen.workspace_cell_width);
        mCellHeight = context.getResources().getDimensionPixelSize(R.dimen.workspace_cell_height);
        
        mLongAxisStartPadding = 0;
        mLongAxisEndPadding = context.getResources().getDimensionPixelOffset(R.dimen.button_bar_height);
        mShortAxisStartPadding =0;
        mShortAxisEndPadding = 0;
        mShortAxisCells = 4;
        mLongAxisCells = 4;        
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
        
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        
        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
        }

        final int shortAxisCells = mShortAxisCells;
        final int longAxisCells = mLongAxisCells;
        final int longAxisStartPadding = mLongAxisStartPadding;
        final int longAxisEndPadding = mLongAxisEndPadding;
        final int shortAxisStartPadding = mShortAxisStartPadding;
        final int shortAxisEndPadding = mShortAxisEndPadding;
        final int cellWidth = mCellWidth;
        final int cellHeight = mCellHeight;

        mPortrait = heightSpecSize > widthSpecSize;

        int numShortGaps = shortAxisCells - 1;
        int numLongGaps = longAxisCells - 1;

        if (mPortrait) {
            int vSpaceLeft = heightSpecSize - longAxisStartPadding - longAxisEndPadding
                    - (cellHeight * longAxisCells);
            mHeightGap = vSpaceLeft / numLongGaps;

            int hSpaceLeft = widthSpecSize - shortAxisStartPadding - shortAxisEndPadding
                    - (cellWidth * shortAxisCells);
            if (numShortGaps > 0) {
                mWidthGap = hSpaceLeft / numShortGaps;
            } else {
                mWidthGap = 0;
            }
            if(Launcher.LOGD)Log.d(TAG, "onMeasure heightSpecSize:"+heightSpecSize+" longAxisStartPadding:"+longAxisStartPadding+" longAxisEndPadding:"+longAxisEndPadding
                    +" cellHeight:"+cellHeight+" longAxisCells:"+longAxisCells+" vSpaceLeft:"+vSpaceLeft+" numLongGaps:"+numLongGaps);
        } else {
            int hSpaceLeft = widthSpecSize - longAxisStartPadding - longAxisEndPadding
                    - (cellWidth * longAxisCells);
            mWidthGap = hSpaceLeft / numLongGaps;

            int vSpaceLeft = heightSpecSize - shortAxisStartPadding - shortAxisEndPadding
                    - (cellHeight * shortAxisCells);
            if (numShortGaps > 0) {
                mHeightGap = vSpaceLeft / numShortGaps;
            } else {
                mHeightGap = 0;
            }
        }    
        
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
            final int myCellHSpan = lp.cellHSpan;
            final int myCellVSpan = lp.cellVSpan;
            lp.width = myCellHSpan * cellWidth + ((myCellHSpan - 1) * mWidthGap) -
                    lp.leftMargin - lp.rightMargin;
            lp.height = myCellVSpan * cellHeight + ((myCellVSpan - 1) * mHeightGap) -
                    lp.topMargin - lp.bottomMargin;         
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
            int childheightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        }

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    private int basicLeft;
    private int basicTop ;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = getChildAt(i);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
            int childLeft = (mWidth - lp.width)/2;
            int childTop = (mHeight - lp.height)/2;
            if(child instanceof CloseView)
            {
                childLeft = basicLeft; 
                childTop  = basicTop;
            }
            else
            {
                basicLeft = childLeft;
                basicTop = childTop;
            }
           
            child.layout(childLeft,childTop,childLeft+lp.width,childTop+lp.height);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAllViews();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //TODO if move screen just dispatch the event to Workspace , return false
        return false;
        
    }

}
