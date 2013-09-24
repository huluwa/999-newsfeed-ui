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

package com.tormas.home;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class AllAppsCellLayout extends CellLayout {
    private static final String TAG = "oms2.5Launcher.AllAppsCellLayout";
    private boolean mPortrait = true;  //default set as portrait.

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

    private final Rect mRect = new Rect();
    private final CellInfo mCellInfo = new CellInfo();
    
    int[] mCellXY = new int[2];
    boolean[][] mOccupied;

    private RectF mDragRect = new RectF();

    private boolean mDirtyTag;
    private boolean mLastDownOnOccupiedCell = false;
    
    public AllAppsCellLayout(Context context) {
        this(context, null);
    }

    public AllAppsCellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsCellLayout(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);    	
    	
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CellLayout, defStyle, 0);

        mCellWidth = a.getDimensionPixelSize(R.styleable.CellLayout_cellWidth, 10);
        mCellHeight = a.getDimensionPixelSize(R.styleable.CellLayout_cellHeight, 10);
        
        mLongAxisStartPadding = 
            a.getDimensionPixelSize(R.styleable.CellLayout_longAxisStartPadding, 10);
        mLongAxisEndPadding = 
            a.getDimensionPixelSize(R.styleable.CellLayout_longAxisEndPadding, 10);
        mShortAxisStartPadding =
            a.getDimensionPixelSize(R.styleable.CellLayout_shortAxisStartPadding, 10);
        mShortAxisEndPadding = 
            a.getDimensionPixelSize(R.styleable.CellLayout_shortAxisEndPadding, 10);
        
        mShortAxisCells = a.getInt(R.styleable.CellLayout_shortAxisCells, 4);
        mLongAxisCells = a.getInt(R.styleable.CellLayout_longAxisCells, 4);

        
        mLongAxisEndPadding -= 8;
        if(Launcher.LOGD)Log.d(TAG, "AllAppsCellLayout mShortAxisCells:"+mShortAxisCells
        		+"  mLongAxisCells:"+mLongAxisCells+" mPortrait:"+mPortrait+" mOccupied:"+mOccupied+" mCellWidth:"+mCellWidth+" mCellHeight:"+mCellHeight);
        

        a.recycle();
        
        //this is different from Desktop
        setAlwaysDrawnWithCacheEnabled(false);

        final Resources res = context.getResources();
        mPortrait = (Configuration.ORIENTATION_LANDSCAPE == res.getConfiguration().orientation ? false : true);
        if (mOccupied == null) {
            if (mPortrait) { //Portrait
            	if(Launcher.LOGD)Log.d(TAG, "create Occupied["+mShortAxisCells+"]["+mLongAxisCells+"]");
                mOccupied = new boolean[mShortAxisCells][mLongAxisCells];
            } else {
            	if(Launcher.LOGD)Log.d(TAG, "create Occupied["+mLongAxisCells+"]["+mShortAxisCells+"]");
                mOccupied = new boolean[mLongAxisCells][mShortAxisCells];
            }
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mCellInfo.screen = ((ViewGroup) getParent()).indexOfChild(this);
    }
    
    public void reSetCellInfoScreen(){
    	mCellInfo.screen = ((ViewGroup) getParent()).indexOfChild(this);
    }
 
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
//        if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent ev:"+action);
        final CellInfo cellInfo = mCellInfo;

        if (action == MotionEvent.ACTION_DOWN) {
            final Rect frame = mRect;
            final int x = (int) ev.getX() + mScrollX;
            final int y = (int) ev.getY() + mScrollY;
            final int count = getChildCount();

            boolean found = false;
            for (int i = count - 1; i >= 0; i--) {
                final View child = getChildAt(i);

                if ((child.getVisibility()) == VISIBLE || child.getAnimation() != null) {
                    child.getHitRect(frame);
                    if (frame.contains(x, y)) {
                        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                        cellInfo.cell = child;
                        cellInfo.cellX = lp.cellX;
                        cellInfo.cellY = lp.cellY;
                        cellInfo.spanX = lp.cellHSpan;
                        cellInfo.spanY = lp.cellVSpan;
                        cellInfo.valid = true;
                        found = true;
                        mDirtyTag = false;
                        break;
                    }
                }
            }
            
            mLastDownOnOccupiedCell = found;

            if (!found) {
                int cellXY[] = mCellXY;
                pointToCellExact(x, y, cellXY);

                final boolean portrait = mPortrait;
                final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
                final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

                final boolean[][] occupied = mOccupied;
                findOccupiedCells(xCount, yCount, occupied, null);

                cellInfo.cell = null;
                cellInfo.cellX = cellXY[0];
                cellInfo.cellY = cellXY[1];
                cellInfo.spanX = 1;
                cellInfo.spanY = 1;
                cellInfo.valid = cellXY[0] >= 0 && cellXY[1] >= 0 && cellXY[0] < xCount &&
                        cellXY[1] < yCount && !occupied[cellXY[0]][cellXY[1]];

                mDirtyTag = true;
            }
            setTag(cellInfo);
        } else if (action == MotionEvent.ACTION_UP) {
            cellInfo.cell = null;
            cellInfo.cellX = -1;
            cellInfo.cellY = -1;
            cellInfo.spanX = 0;
            cellInfo.spanY = 0;
            cellInfo.valid = false;
            mDirtyTag = false;
            setTag(cellInfo);
        }

//        if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent return false");
        return false;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	//if(Launcher.LOGD)Log.d(TAG, "onMeasure");
        // TODO: currently ignoring padding
        
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
        
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        
        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("AllAppsCellLayout cannot have UNSPECIFIED dimensions");
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
//            Log.d(TAG, "onMeasure heightSpecSize:"+heightSpecSize+" widthSpecSize:"+widthSpecSize+" longAxisStartPadding:"+longAxisStartPadding+" longAxisEndPadding:"+longAxisEndPadding
//            		+" cellHeight:"+cellHeight+" longAxisCells:"+longAxisCells+" vSpaceLeft:"+vSpaceLeft+" numLongGaps:"+numLongGaps);
        } else {
            int hSpaceLeft = widthSpecSize - longAxisStartPadding - longAxisEndPadding
                    - (cellWidth * longAxisCells);
            mWidthGap = hSpaceLeft / numLongGaps;

            int vSpaceLeft = heightSpecSize - shortAxisStartPadding - shortAxisEndPadding
                    - (cellHeight * shortAxisCells);
            if (numShortGaps > 0) {
                mHeightGap = vSpaceLeft / numShortGaps;
                if(mHeightGap < 0)mHeightGap=0;
            } else {
                mHeightGap = 0;
            }
        }
        
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (mPortrait) {
                lp.setup(child, cellWidth, cellHeight, mWidthGap, mHeightGap, shortAxisStartPadding, longAxisStartPadding);
            } else {
                lp.setup(child, cellWidth, cellHeight, mWidthGap, mHeightGap, longAxisStartPadding, shortAxisStartPadding);
            }
            
            if (lp.regenerateId) {
                child.setId(((getId() & 0xFF) << 16) | (lp.cellX & 0xFF) << 8 | (lp.cellY & 0xFF));
                lp.regenerateId = false;
            }

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
            int childheightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        }

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//    	if(Launcher.LOGD)Log.d(TAG, "onLayout changed:"+changed+" l:"+l+" t:"+t+" r:"+r+" b:"+b);
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                AllAppsCellLayout.LayoutParams lp = (AllAppsCellLayout.LayoutParams) child.getLayoutParams();

                int childLeft = lp.x;
                int childTop = lp.y;
                
                child.layout(childLeft, childTop, childLeft + lp.width, childTop + lp.height);
//                if(Launcher.LOGD)Log.d(TAG, "onLayout child.layout  lp.w:"+lp.width+" lp.h:"+lp.height+" childLeft:"+childLeft+" childTop:"+childTop);
                
                if (lp.dropped) {
                    lp.dropped = false;

                    final int[] cellXY = mCellXY;
                    getLocationOnScreen(cellXY);
                }
            }
        }
    }

    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
    	return new AllAppsCellLayout.LayoutParams(getContext(), attrs);
    }

    @Override
	public void buildDrawingCache(boolean autoScale) 
    {		
    	//Throwable tw = new Throwable();
    	//Log.d(TAG, "buildDrawingCache autoScale="+autoScale, tw);   	
    	    	
		super.buildDrawingCache(autoScale);
	}

	@Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
    	final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            view.setDrawingCacheEnabled(enabled);
            // Update the drawing caches
            view.buildDrawingCache(true);
        }
    }    

    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    	return p instanceof AllAppsCellLayout.LayoutParams;
    } 
    
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    	return new AllAppsCellLayout.LayoutParams(p);
    }
    
    public static class LayoutParams extends CellLayout.LayoutParams {
    	public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }
        
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
        
        public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan) {
            super(cellX, cellY, cellHSpan, cellVSpan);
        }
        
        public LayoutParams(int viewWidth, int viewHeight, int cellX, int cellY, int cellHSpan, int cellVSpan) {
        	super(viewWidth, viewHeight, cellX, cellY, cellHSpan, cellVSpan);
        }
        
        public void setup(View view, int cellWidth, int cellHeight, int widthGap, int heightGap,
                int hStartPadding, int vStartPadding) {
            final int myCellX = cellX;
            final int myCellY = cellY;
            
            x = hStartPadding + myCellX * (cellWidth + widthGap) + leftMargin;
            y = vStartPadding + myCellY * (cellHeight + heightGap) + topMargin;
            
//            Log.d(TAG,"AllAppsCellLayout.Params setup width:"+width+" height:"+height+" x:"+x
//            		+" y:"+y+" cellWidth:"+cellWidth+" cellHeight:"+cellHeight
//            		+" widthGap:"+widthGap+" heightGap:"+heightGap+" hStartPadding:"+hStartPadding+" vStartPadding:"+vStartPadding);
        }
    }
}


