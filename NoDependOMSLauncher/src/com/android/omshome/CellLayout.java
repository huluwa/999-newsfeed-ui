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

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.app.WallpaperManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class CellLayout extends ViewGroup {
    private static final String TAG = "oms2.5Launcher.CellLayout";
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

    private final Rect mRect = new Rect();
    private final CellInfo mCellInfo = new CellInfo();
    
    int[] mCellXY = new int[2];
    boolean[][] mOccupied;

    private RectF mDragRect = new RectF();    

    private boolean mDirtyTag;
    private boolean mLastDownOnOccupiedCell = false;
    
    private final WallpaperManager mWallpaperManager;     

    public CellLayout(Context context) {
        this(context, null);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyle) {
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

        a.recycle();


        if (mOccupied == null) {
            if (mPortrait) {
                mOccupied = new boolean[mShortAxisCells][mLongAxisCells];
            } else {
                mOccupied = new boolean[mLongAxisCells][mShortAxisCells];
            }
        }
        
        if(Launcher.LOGD)Log.d(TAG, "create CellLayout mCellWidth:"+mCellWidth+" mCellHeight:"+mCellHeight
        		+" orientation:" + getResources().getConfiguration().orientation + " this="+this);
        
        mWallpaperManager = WallpaperManager.getInstance(getContext());
        if(Workspace.use_oms_api == false)
        {
            setAlwaysDrawnWithCacheEnabled(false);
        }        
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        // Cancel long press for all children
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.cancelLongPress();
        }
    }

    protected int getCountX() {
        return mPortrait ? mShortAxisCells : mLongAxisCells;
    }

    protected int getCountY() {
        return mPortrait ? mLongAxisCells : mShortAxisCells;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        // Generate an id for each view, this assumes we have at most 256x256 cells
        // per workspace screen
        final LayoutParams cellParams = (LayoutParams) params;
        cellParams.regenerateId = true;

        super.addView(child, index, params);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (child != null) {
            Rect r = new Rect();
            child.getDrawingRect(r);
            requestRectangleOnScreen(r);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //Log.d(TAG,"entering dispatchKeyEvent==focused="+this.isFocused());
        //if focused in cell layout. don't response KEYCODE_DPAD_CENTER
        if(this.isFocused() && (KeyEvent.KEYCODE_DPAD_CENTER == event.getKeyCode() || KeyEvent.KEYCODE_ENTER == event.getKeyCode()))
        {
            return true;
        }
        else
        {
            return super.dispatchKeyEvent(event);
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
        //if(Launcher.LOGD)Log.d(TAG,"onInterceptTouchEvent ev:"+action);
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

                // Instead of finding the interesting vacant cells here, wait until a
                // caller invokes getTag() to retrieve the result. Finding the vacant
                // cells is a bit expensive and can generate many new objects, it's
                // therefore better to defer it until we know we actually need it.

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
    public CellInfo getTag() {
        final CellInfo info = (CellInfo) super.getTag();
        if (mDirtyTag && info.valid) {
            final boolean portrait = mPortrait;
            final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
            final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

            final boolean[][] occupied = mOccupied;
            findOccupiedCells(xCount, yCount, occupied, null);

            findIntersectingVacantCells(info, info.cellX, info.cellY, xCount, yCount, occupied);

            mDirtyTag = false;
        }
        return info;
    }

    protected static void findIntersectingVacantCells(CellInfo cellInfo, int x, int y,
            int xCount, int yCount, boolean[][] occupied) {

        cellInfo.maxVacantSpanX = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanXSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanYSpanX = Integer.MIN_VALUE;
        cellInfo.clearVacantCells();

        if (occupied[x][y]) {
            return;
        }

        cellInfo.current.set(x, y, x, y);

        findVacantCell(cellInfo.current, xCount, yCount, occupied, cellInfo);
    }

    protected static void findVacantCell(Rect current, int xCount, int yCount, boolean[][] occupied,
            CellInfo cellInfo) {

        addVacantCell(current, cellInfo);

        if (current.left > 0) {
            if (isColumnEmpty(current.left - 1, current.top, current.bottom, occupied)) {
                current.left--;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.left++;
            }
        }

        if (current.right < xCount - 1) {
            if (isColumnEmpty(current.right + 1, current.top, current.bottom, occupied)) {
                current.right++;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.right--;
            }
        }

        if (current.top > 0) {
            if (isRowEmpty(current.top - 1, current.left, current.right, occupied)) {
                current.top--;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.top++;
            }
        }

        if (current.bottom < yCount - 1) {
            if (isRowEmpty(current.bottom + 1, current.left, current.right, occupied)) {
                current.bottom++;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.bottom--;
            }
        }
    }

    protected static void addVacantCell(Rect current, CellInfo cellInfo) {
        CellInfo.VacantCell cell = CellInfo.VacantCell.acquire();
        cell.cellX = current.left;
        cell.cellY = current.top;
        cell.spanX = current.right - current.left + 1;
        cell.spanY = current.bottom - current.top + 1;
        if (cell.spanX > cellInfo.maxVacantSpanX) {
            cellInfo.maxVacantSpanX = cell.spanX;
            cellInfo.maxVacantSpanXSpanY = cell.spanY;
        }
        if (cell.spanY > cellInfo.maxVacantSpanY) {
            cellInfo.maxVacantSpanY = cell.spanY;
            cellInfo.maxVacantSpanYSpanX = cell.spanX;
        }
        cellInfo.vacantCells.add(cell);
    }

    protected static boolean isColumnEmpty(int x, int top, int bottom, boolean[][] occupied) {
        for (int y = top; y <= bottom; y++) {
            if (occupied[x][y]) {
                return false;
            }
        }
        return true;
    }

    protected static boolean isRowEmpty(int y, int left, int right, boolean[][] occupied) {
        for (int x = left; x <= right; x++) {
            if (occupied[x][y]) {
                return false;
            }
        }
        return true;
    }

    protected CellInfo findAllVacantCells(boolean[] occupiedCells, View ignoreView) {
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

        boolean[][] occupied = mOccupied;

        if (occupiedCells != null) {
            for (int y = 0; y < yCount; y++) {
                for (int x = 0; x < xCount; x++) {
                    occupied[x][y] = occupiedCells[y * xCount + x];
                }
            }
        } else {
            findOccupiedCells(xCount, yCount, occupied, ignoreView);
        }

        CellInfo cellInfo = new CellInfo();

        cellInfo.cellX = -1;
        cellInfo.cellY = -1;
        cellInfo.spanY = 0;
        cellInfo.spanX = 0;
        cellInfo.maxVacantSpanX = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanXSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanYSpanX = Integer.MIN_VALUE;
        cellInfo.screen = mCellInfo.screen;

        Rect current = cellInfo.current;

        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                if (!occupied[x][y]) {
                    current.set(x, y, x, y);
                    findVacantCell(current, xCount, yCount, occupied, cellInfo);
                    occupied[x][y] = true;
                }
            }
        }

        cellInfo.valid = cellInfo.vacantCells.size() > 0;

        // Assume the caller will perform their own cell searching, otherwise we
        // risk causing an unnecessary rebuild after findCellForSpan()
        
        return cellInfo;
    }

    /**
     * Given a point, return the cell that strictly encloses that point 
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @param result Array of 2 ints to hold the x and y coordinate of the cell
     */
    protected void pointToCellExact(int x, int y, int[] result) {
        final boolean portrait = mPortrait;
        
        final int hStartPadding = portrait ? mShortAxisStartPadding : mLongAxisStartPadding;
        final int vStartPadding = portrait ? mLongAxisStartPadding : mShortAxisStartPadding;

        result[0] = (x - hStartPadding) / (mCellWidth + mWidthGap);
        result[1] = (y - vStartPadding) / (mCellHeight + mHeightGap);

        final int xAxis = portrait ? mShortAxisCells : mLongAxisCells;
        final int yAxis = portrait ? mLongAxisCells : mShortAxisCells;

        if (result[0] < 0) result[0] = 0;
        if (result[0] >= xAxis) result[0] = xAxis - 1;
        if (result[1] < 0) result[1] = 0;
        if (result[1] >= yAxis) result[1] = yAxis - 1;
    }
    
    /**
     * Given a point, return the cell that most closely encloses that point
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @param result Array of 2 ints to hold the x and y coordinate of the cell
     */
    protected void pointToCellRounded(int x, int y, int[] result) {
        pointToCellExact(x + (mCellWidth / 2), y + (mCellHeight / 2), result);
    }

    /**
     * Given a cell coordinate, return the point that represents the upper left corner of that cell
     * 
     * @param cellX X coordinate of the cell 
     * @param cellY Y coordinate of the cell
     * 
     * @param result Array of 2 ints to hold the x and y coordinate of the point
     */
    protected void cellToPoint(int cellX, int cellY, int[] result) {
        final boolean portrait = mPortrait;
        
        final int hStartPadding = portrait ? mShortAxisStartPadding : mLongAxisStartPadding;
        final int vStartPadding = portrait ? mLongAxisStartPadding : mShortAxisStartPadding;


        result[0] = hStartPadding + cellX * (mCellWidth + mWidthGap);
        result[1] = vStartPadding + cellY * (mCellHeight + mHeightGap);
    }

    protected int getCellWidth() {
        return mCellWidth;
    }

    protected int getCellHeight() {
        return mCellHeight;
    }

    protected int getLeftPadding() {
        return mPortrait ? mShortAxisStartPadding : mLongAxisStartPadding;
    }

    protected int getTopPadding() {
        return mPortrait ? mLongAxisStartPadding : mShortAxisStartPadding;        
    }

    protected int getRightPadding() {
        return mPortrait ? mShortAxisEndPadding : mLongAxisEndPadding;
    }

    protected int getBottomPadding() {
        return mPortrait ? mLongAxisEndPadding : mShortAxisEndPadding;        
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
            //Log.d(TAG, "onMeasure heightSpecSize:"+heightSpecSize+" longAxisStartPadding:"+longAxisStartPadding+" longAxisEndPadding:"+longAxisEndPadding
            //		+" cellHeight:"+cellHeight+" longAxisCells:"+longAxisCells+" vSpaceLeft:"+vSpaceLeft+" numLongGaps:"+numLongGaps);
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
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (mPortrait) {
                lp.setup(child, cellWidth, cellHeight, mWidthGap, mHeightGap, shortAxisStartPadding,
                        longAxisStartPadding);
            } else {
                lp.setup(child, cellWidth, cellHeight, mWidthGap, mHeightGap, longAxisStartPadding,
                        shortAxisStartPadding);
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
    	//if(Launcher.LOGD)Log.d(TAG, "onLayout changed:"+changed+" l:"+l+" t:"+t+" r:"+r+" b:"+b);
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();

                int childLeft = lp.x;
                int childTop = lp.y;
                
                child.layout(childLeft, childTop, childLeft + lp.width, childTop + lp.height);
                
//                if(Launcher.LOGD)Log.d(TAG, "onLayout child.layout  lp.w:"+lp.width+" lp.h:"+lp.height+" childLeft:"+childLeft+" childTop:"+childTop);
                
                if (lp.dropped) {
                    lp.dropped = false;

                    final int[] cellXY = mCellXY;
                    getLocationOnScreen(cellXY);
                    mWallpaperManager.sendWallpaperCommand(getWindowToken(), "android.home.drop",
                            cellXY[0] + childLeft + lp.width / 2,
                            cellXY[1] + childTop + lp.height / 2, 0, null);
                }
            }
        }
    }

    @Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            view.setDrawingCacheEnabled(enabled);
            // Update the drawing caches
            if(Workspace.use_oms_api == false)
            {
                view.buildDrawingCache(true);
            }
        }
    }

    @Override
    protected void setChildrenDrawnWithCacheEnabled(boolean enabled) {
        super.setChildrenDrawnWithCacheEnabled(enabled);        
    }

    /**
     * Find a vacant area that will fit the given bounds nearest the requested
     * cell location. Uses Euclidean distance to score multiple vacant areas.
     * 
     * @param pixelX The X location at which you want to search for a vacant area.
     * @param pixelY The Y location at which you want to search for a vacant area.
     * @param spanX Horizontal span of the object.
     * @param spanY Vertical span of the object.
     * @param vacantCells Pre-computed set of vacant cells to search.
     * @param recycle Previously returned value to possibly recycle.
     * @return The X, Y cell of a vacant area that can contain this object,
     *         nearest the requested location.
     */
    protected int[] findNearestVacantArea(int pixelX, int pixelY, int spanX, int spanY,
            CellInfo vacantCells, int[] recycle) {
        
        // Keep track of best-scoring drop area
        final int[] bestXY = recycle != null ? recycle : new int[2];
        final int[] cellXY = mCellXY;
        double bestDistance = Double.MAX_VALUE;
        
        // Bail early if vacant cells aren't valid
        if (!vacantCells.valid) {
            return null;
        }

        // Look across all vacant cells for best fit
        final int size = vacantCells.vacantCells.size();
        for (int i = 0; i < size; i++) {
            final CellInfo.VacantCell cell = vacantCells.vacantCells.get(i);
            
            // Reject if vacant cell isn't our exact size
            if (cell.spanX != spanX || cell.spanY != spanY) {
                continue;
            }
            
            // Score is center distance from requested pixel
            cellToPoint(cell.cellX, cell.cellY, cellXY);
            
            double distance = Math.sqrt(Math.pow(cellXY[0] - pixelX, 2) +
                    Math.pow(cellXY[1] - pixelY, 2));
            if (distance <= bestDistance) {
                bestDistance = distance;
                bestXY[0] = cell.cellX;
                bestXY[1] = cell.cellY;
            }
        }

        // Return null if no suitable location found 
        if (bestDistance < Double.MAX_VALUE) {
            return bestXY;
        } else {
            return null;
        }
    }
    
    /**
     * Drop a child at the specified position
     *
     * @param child The child that is being dropped
     * @param targetXY Destination area to move to
     */
    protected void onDropChild(View child, int[] targetXY) {
        if (child != null) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.cellX = targetXY[0];
            lp.cellY = targetXY[1];
            lp.isDragging = false;
            lp.dropped = true;
            mDragRect.setEmpty();
            child.requestLayout();
            invalidate();
        }
    }

    protected void onDropAborted(View child) {
        if (child != null) {
            ((LayoutParams) child.getLayoutParams()).isDragging = false;
            invalidate();
        }
        mDragRect.setEmpty();
    }

    /**
     * Start dragging the specified child
     * 
     * @param child The child that is being dragged
     */
    protected void onDragChild(View child) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        lp.isDragging = true;
        mDragRect.setEmpty();
    }
    
    int[] mLastDragCellXY = new int[2];
    boolean mLastDragAccept = false;
    /**
     * Drag a child over the specified position
     * 
     * @param child The child that is being dropped
     * @param cellX The child's new x cell location
     * @param cellY The child's new y cell location 
     */
    protected void onDragOverChild(View child, int cellX, int cellY) {
        int[] cellXY = mCellXY;
        pointToCellRounded(cellX, cellY, cellXY);
        //comment by jessie 2010-10-18
//        LayoutParams lp = (LayoutParams) child.getLayoutParams();
//        cellToRect(cellXY[0], cellXY[1], lp.cellHSpan, lp.cellVSpan, mDragRect);
//        invalidate();
        final boolean changedXY = (mLastDragCellXY[0] != cellXY[0] || mLastDragCellXY[1] != cellXY[1]);
        mLastDragCellXY[0] = cellXY[0];
        mLastDragCellXY[1] = cellXY[1];
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if(changedXY){
            cellToRect(cellXY[0], cellXY[1], lp.cellHSpan, lp.cellVSpan, mDragRect);
            
            //find views at rang of dragView 
            final List<View> cellGroup = findCells(cellXY[0], cellXY[1], lp.cellHSpan, lp.cellVSpan, child);
            mLastDragAccept = cellGroup == null;
            
            if(mLastDragAccept){
//                if(Launcher.LOGD)Log.d(TAG,"===entering mLastDragAccept="+mLastDragAccept+"==just clear ArrangeAction");
                clearArrangeAction();
            }else{
//                if(Launcher.LOGD)Log.d(TAG,"===entering mLastDragAccept="+mLastDragAccept+"start animation to move shortcut=CellGroupSize="+cellGroup.size());
                //wait for arrange action finish
                mLastDragAccept = true;
                clearArrangeAction();
                mArrangeRunnable.setViews(cellXY, child, cellGroup);
                postDelayed(mArrangeRunnable, 200);
            }
            invalidate();
        }
    }
    
    /**
     * Computes a bounding rectangle for a range of cells
     *  
     * @param cellX X coordinate of upper left corner expressed as a cell position
     * @param cellY Y coordinate of upper left corner expressed as a cell position
     * @param cellHSpan Width in cells 
     * @param cellVSpan Height in cells
     * @param dragRect Rectnagle into which to put the results
     */
    public void cellToRect(int cellX, int cellY, int cellHSpan, int cellVSpan, RectF dragRect) {
        final boolean portrait = mPortrait;
        final int cellWidth = mCellWidth;
        final int cellHeight = mCellHeight;
        final int widthGap = mWidthGap;
        final int heightGap = mHeightGap;
        
        final int hStartPadding = portrait ? mShortAxisStartPadding : mLongAxisStartPadding;
        final int vStartPadding = portrait ? mLongAxisStartPadding : mShortAxisStartPadding;
        
        int width = cellHSpan * cellWidth + ((cellHSpan - 1) * widthGap);
        int height = cellVSpan * cellHeight + ((cellVSpan - 1) * heightGap);

        int x = hStartPadding + cellX * (cellWidth + widthGap);
        int y = vStartPadding + cellY * (cellHeight + heightGap);
        
        dragRect.set(x, y, x + width, y + height);
    }
    
    /**
     * Computes the required horizontal and vertical cell spans to always 
     * fit the given rectangle.
     *  
     * @param width Width in pixels
     * @param height Height in pixels
     */
    public int[] rectToCell(int width, int height) {
        // Always assume we're working with the smallest span to make sure we
        // reserve enough space in both orientations.
        final Resources resources = getResources();
        int actualWidth = resources.getDimensionPixelSize(R.dimen.workspace_cell_width);
        int actualHeight = resources.getDimensionPixelSize(R.dimen.workspace_cell_height);
        int smallerSize = Math.min(actualWidth, actualHeight);

        // Always round up to next largest cell
         //google design
		int spanX = (width + smallerSize) / smallerSize;
        int spanY = (height + smallerSize) / smallerSize;

        /*
        //for process large span
        if(spanX >=4)
        	spanX = 4;
        
        if(spanY >=4)
        	spanY = 4;
        */

        return new int[] { spanX, spanY };
    }

    /**
     * Find the first vacant cell, if there is one.
     *
     * @param vacant Holds the x and y coordinate of the vacant cell
     * @param spanX Horizontal cell span.
     * @param spanY Vertical cell span.
     * 
     * @return True if a vacant cell was found
     */
    public boolean getVacantCell(int[] vacant, int spanX, int spanY) {
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
        final boolean[][] occupied = mOccupied;

        findOccupiedCells(xCount, yCount, occupied, null);

        return findVacantCell(vacant, spanX, spanY, xCount, yCount, occupied);
    }
    
    /*
     * just for scroll, please don't use this API
     */
    public boolean isFull()
    {
    	int[] vacant = new int[2];    
    	return getVacantCell(vacant, 1, 1);
    }

    static boolean findVacantCell(int[] vacant, int spanX, int spanY,
            int xCount, int yCount, boolean[][] occupied) {

        for (int y = 0; y < yCount; y++) {
            for (int x = 0; x < xCount; x++) {
                boolean available = !occupied[x][y];
                for (int j = y; j < y + spanY - 1 && y < yCount; j++) {
                    for (int i = x; i < x + spanX - 1 && x < xCount; i++) {
                        available = available && !occupied[i][j];
                        if (!available) break;
                    }
                }

                if (available) {
                    vacant[0] = x;
                    vacant[1] = y;
                    return true;
                }
            }
        }

        return false;
    }
    
    private boolean findVacantCell(int[] vacant, int spanX, int spanY,
            int xCount, int yCount, boolean[][] occupied, int[] ignoreCell) {

        for (int y = 0; y < yCount; y++) {
            for (int x = 0; x < xCount; x++) {
                boolean available = !occupied[x][y] && x + spanX <= xCount && y + spanY <= yCount;
                if(null != ignoreCell && x == ignoreCell[0] && y == ignoreCell[1]) available = false;
                
                for (int j = y; j < y + spanY; j++) {
                    for (int i = x; i < x + spanX; i++) {
                        available = available && !occupied[i][j];
                        if (!available) break;
                    }
                    if (!available) break;
                }

                if (available) {
                    vacant[0] = x;
                    vacant[1] = y;
//                    if(Launcher.LOGD) Log.d(TAG,"findVacantCell x="+x+"==y="+y);
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean[] getOccupiedCells() {
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
        final boolean[][] occupied = mOccupied;

        findOccupiedCells(xCount, yCount, occupied, null);

        final boolean[] flat = new boolean[xCount * yCount];
        for (int y = 0; y < yCount; y++) {
            for (int x = 0; x < xCount; x++) {
                flat[y * xCount + x] = occupied[x][y];
            }
        }

        return flat;
    }

    protected void findOccupiedCells(int xCount, int yCount, boolean[][] occupied, View ignoreView) {
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                occupied[x][y] = false;
            }
        }

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof Folder || child.equals(ignoreView)) {
                continue;
            }
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            for (int x = lp.cellX; x < lp.cellX + lp.cellHSpan && x < xCount; x++) {
                for (int y = lp.cellY; y < lp.cellY + lp.cellVSpan && y < yCount; y++) {
                    occupied[x][y] = true;
                }
            }
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new CellLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof CellLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new CellLayout.LayoutParams(p);
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        /**
         * Horizontal location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellX;

        /**
         * Vertical location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellY;

        /**
         * Number of cells spanned horizontally by the item.
         */
        @ViewDebug.ExportedProperty
        public int cellHSpan;

        /**
         * Number of cells spanned vertically by the item.
         */
        @ViewDebug.ExportedProperty
        public int cellVSpan;
        
        /**
         * Is this item currently being dragged
         */
        public boolean isDragging;

        // X coordinate of the view in the layout.
        @ViewDebug.ExportedProperty
        int x;
        // Y coordinate of the view in the layout.
        @ViewDebug.ExportedProperty
        int y;

        boolean regenerateId;
        
        boolean dropped;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            cellHSpan = 1;
            cellVSpan = 1;
        }
        
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            cellHSpan = 1;
            cellVSpan = 1;
        }
        
        public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan) {
            super(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellHSpan = cellHSpan;
            this.cellVSpan = cellVSpan;
        }

        
        public LayoutParams(int viewWidth, int viewHeight, int cellX, int cellY, int cellHSpan, int cellVSpan) {
            super(viewWidth, viewHeight);
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellHSpan = cellHSpan;
            this.cellVSpan = cellVSpan;
        }

        protected void setup(View view, int cellWidth, int cellHeight, int widthGap, int heightGap,
                int hStartPadding, int vStartPadding) {
            
            final int myCellHSpan = cellHSpan;
            final int myCellVSpan = cellVSpan;
            final int myCellX = cellX;
            final int myCellY = cellY;
            
            if(view!=null && !(view instanceof BubbleTextView)){
            	width = myCellHSpan * cellWidth + ((myCellHSpan - 1) * widthGap) -
            		leftMargin - rightMargin;
            	height = myCellVSpan * cellHeight + ((myCellVSpan - 1) * heightGap) -
            		topMargin - bottomMargin;
            }

            x = hStartPadding + myCellX * (cellWidth + widthGap) + leftMargin;
            y = vStartPadding + myCellY * (cellHeight + heightGap) + topMargin;
//            Log.d(TAG,"CellLayout.Params setup   width:"+width+" height:"+height+" x:"+x
//            		+" y:"+y+" cellWidth:"+cellWidth+" cellHeight:"+cellHeight
//            		+" widthGap:"+widthGap+" heightGap:"+heightGap+" hStartPadding:"+hStartPadding+" vStartPadding:"+vStartPadding);
        }
    }

    static final class CellInfo implements ContextMenu.ContextMenuInfo {
        /**
         * See View.AttachInfo.InvalidateInfo for futher explanations about
         * the recycling mechanism. In this case, we recycle the vacant cells
         * instances because up to several hundreds can be instanciated when
         * the user long presses an empty cell.
         */
        static final class VacantCell {
            int cellX;
            int cellY;
            int spanX;
            int spanY;

            // We can create up to 523 vacant cells on a 4x4 grid, 100 seems
            // like a reasonable compromise given the size of a VacantCell and
            // the fact that the user is not likely to touch an empty 4x4 grid
            // very often 
            private static final int POOL_LIMIT = 100;
            private static final Object sLock = new Object();

            private static int sAcquiredCount = 0;
            private static VacantCell sRoot;

            private VacantCell next;

            static VacantCell acquire() {
                synchronized (sLock) {
                    if (sRoot == null) {
                        return new VacantCell();
                    }

                    VacantCell info = sRoot;
                    sRoot = info.next;
                    sAcquiredCount--;

                    return info;
                }
            }

            void release() {
                synchronized (sLock) {
                    if (sAcquiredCount < POOL_LIMIT) {
                        sAcquiredCount++;
                        next = sRoot;
                        sRoot = this;
                    }
                }
            }

            @Override
            public String toString() {
                return "VacantCell[x=" + cellX + ", y=" + cellY + ", spanX=" + spanX +
                        ", spanY=" + spanY + "]";
            }
        }

        View cell;
        int cellX;
        int cellY;
        int spanX;
        int spanY;
        int screen;
        boolean valid;

        final ArrayList<VacantCell> vacantCells = new ArrayList<VacantCell>(VacantCell.POOL_LIMIT);
        int maxVacantSpanX;
        int maxVacantSpanXSpanY;
        int maxVacantSpanY;
        int maxVacantSpanYSpanX;
        final Rect current = new Rect();

        void clearVacantCells() {
            final ArrayList<VacantCell> list = vacantCells;
            final int count = list.size();

            for (int i = 0; i < count; i++) list.get(i).release();

            list.clear();
        }

        void findVacantCellsFromOccupied(boolean[] occupied, int xCount, int yCount) {
            if (cellX < 0 || cellY < 0) {
                maxVacantSpanX = maxVacantSpanXSpanY = Integer.MIN_VALUE;
                maxVacantSpanY = maxVacantSpanYSpanX = Integer.MIN_VALUE;
                clearVacantCells();
                return;
            }

            final boolean[][] unflattened = new boolean[xCount][yCount];
            for (int y = 0; y < yCount; y++) {
                for (int x = 0; x < xCount; x++) {
                    unflattened[x][y] = occupied[y * xCount + x];
                }
            }
            CellLayout.findIntersectingVacantCells(this, cellX, cellY, xCount, yCount, unflattened);
        }

        /**
         * This method can be called only once! Calling #findVacantCellsFromOccupied will
         * restore the ability to call this method.
         *
         * Finds the upper-left coordinate of the first rectangle in the grid that can
         * hold a cell of the specified dimensions.
         *
         * @param cellXY The array that will contain the position of a vacant cell if such a cell
         *               can be found.
         * @param spanX The horizontal span of the cell we want to find.
         * @param spanY The vertical span of the cell we want to find.
         *
         * @return True if a vacant cell of the specified dimension was found, false otherwise.
         */
        boolean findCellForSpan(int[] cellXY, int spanX, int spanY) {
            return findCellForSpan(cellXY, spanX, spanY, true);
        }

        boolean findCellForSpan(int[] cellXY, int spanX, int spanY, boolean clear) {
            final ArrayList<VacantCell> list = vacantCells;
            final int count = list.size();

            boolean found = false;

            if (this.spanX >= spanX && this.spanY >= spanY) {
                cellXY[0] = cellX;
                cellXY[1] = cellY;
                found = true;
            }

            // Look for an exact match first
            for (int i = 0; i < count; i++) {
                VacantCell cell = list.get(i);
                if (cell.spanX == spanX && cell.spanY == spanY) {
                    cellXY[0] = cell.cellX;
                    cellXY[1] = cell.cellY;
                    found = true;
                    break;
                }
            }

            // Look for the first cell large enough
            for (int i = 0; i < count; i++) {
                VacantCell cell = list.get(i);
                if (cell.spanX >= spanX && cell.spanY >= spanY) {
                    cellXY[0] = cell.cellX;
                    cellXY[1] = cell.cellY;
                    found = true;
                    break;
                }
            }

            if (clear) clearVacantCells();

            return found;
        }

        @Override
        public String toString() {
            return "Cell[view=" + (cell == null ? "null" : cell.getClass()) + ", x=" + cellX +
                    ", y=" + cellY + "]";
        }
    }

    public boolean lastDownOnOccupiedCell() {
        return mLastDownOnOccupiedCell;
    }
    
    /**
     * Finds the Views intersecting with the specified cell. If the cell is outside
     * of the layout, this is returned.
     *
     * @param cellX The X location of the cell to test.
     * @param cellY The Y location of the cell to test.
     * @param spanX The horizontal span of the cell to test.
     * @param spanY The vertical span of the cell to test.
     * @param ignoreCell View to ignore during the test.
     *
     * @return Returns the first View intersecting with the specified cell, this if the cell
     *         lies outside of this layout's grid or null if no View was found.
     */    
    List<View> findCells(int cellX, int cellY, int spanX, int spanY, View ignoreCell) {
        List<View> cells = new ArrayList<View>();

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            if (view == ignoreCell) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (cellX < lp.cellX + lp.cellHSpan && lp.cellX < cellX + spanX &&
                    cellY < lp.cellY + lp.cellVSpan && lp.cellY < cellY + spanY) {
                cells.add(view);
            }
        }
        if(cells.isEmpty()) {
            return null;
        }else {
            return cells;
        }
    }
    
    public void clearArrangeAction() {
        mArrangeRunnable.setViews(new int[]{-1,-1}, null, null);
        removeCallbacks(mArrangeRunnable);
    }
    
    private Object mArrangeLock = new Object();
    private ArrangeRunnable mArrangeRunnable = new ArrangeRunnable();
    Set<View> mNeedSaveCells = new HashSet<View>();
    private class ArrangeRunnable implements Runnable {
        private View dragChild;
        private List<View> cells;
        private int[] targetXY = new int[2]; //cellx,cellY current dragView's position
        private final Interpolator interpolator = new DecelerateInterpolator(1.5f);
        private static final long MOVE_DURATION = 300l;
        
        ArrangeRunnable() {
        }

        public void run() {
            synchronized (mArrangeLock) {
            	
            	if(DeleteZone.isInTrashMode())
            	{
            		return;
            	}
            
            if(cells == null) {return;}            
            LayoutParams lp = (LayoutParams) dragChild.getLayoutParams();
            
            final int cellX = targetXY[0];            
            final int cellY = targetXY[1];
            
            Map<View, int[]> moveMap = new HashMap<View, int[]>();                        
            boolean canMove = true;
            
            final boolean portrait = mPortrait;
            final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
            final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
            final boolean[][] occupied = mOccupied;

            findOccupiedCells(xCount, yCount, occupied, dragChild);
            
           // if(mScreenIndex == ci.screen){
               // changeOccupied(occupied, targetXY[0], targetXY[1], lp.cellHSpan, lp.cellVSpan, false); //set cell which dragView located occupied==false
           // }
            
            for (View cell : cells) {
                final LayoutParams clp = (LayoutParams) cell.getLayoutParams();                
                int[] vacant = new int[2];
                
                // clear org cell occupied & add a span 1 cell for change the new vacant cell pos
                changeOccupied(occupied, clp.cellX, clp.cellY, clp.cellHSpan, clp.cellVSpan, false); //change cell under dragView occupied==false               
                changeOccupied(occupied, cellX, cellY, lp.cellHSpan, lp.cellVSpan, true); //changed cell which dragView located occupied==ture
                
                final int[] ignoreCell = new int[] {clp.cellX, clp.cellY};
                //find vancant for cells
                if(findVacantCell(vacant, clp.cellHSpan, clp.cellVSpan, xCount, yCount, occupied, ignoreCell)) {                    
                    moveMap.put(cell, vacant);
//                    Log.d(TAG,"==start changeOccupied==true"+vacant[0]+"=="+vacant[1]+"==spanX="+clp.cellHSpan+"==spanY="+clp.cellVSpan);
                    changeOccupied(occupied, vacant[0], vacant[1], clp.cellHSpan, clp.cellVSpan, true);
                } else {
                    if(android.os.SystemProperties.get("animationto.diffscreen").equals("1") == true){
                        if(findSuitableVacantCell(vacant,cell,dragChild))
                        {
                          // TODO setOccupied as true which was occupided by cell 
                          moveMap.put(cell, vacant);
//                          Log.d(TAG,"b191 findVacantCell true");
                        }
                        else
                        {
                            canMove = false;
                            break;
                        }
                    }
                    else
                    {
                        canMove = false;
                        break;
                    }
                   
                    
                }
            }
            
            if(canMove) {
                for (View cell : cells) {
                    final int[] vacant = moveMap.get(cell);   
                    if(vacant == null) continue;

                    LayoutParams clp = (LayoutParams) cell.getLayoutParams(); 
                    if(cell.getTag() instanceof ShortcutInfo )
                    {
                        ShortcutInfo info = (ShortcutInfo)cell.getTag();
                        
                        if(info.newIndex >=0 && info.newIndex != info.screen)
                        { 
                             CellLayout.this.removeView(cell);
                             final CellLayout originalCellLayout = (CellLayout) ((ViewGroup)getParent()).getChildAt(info.newIndex);
                             info.screen = info.newIndex;
                             info.newIndex = -1;
                             clp.cellX = vacant[0];
                             clp.cellY = vacant[1];
                             originalCellLayout.addView(cell);
                             mNeedSaveCells.add(cell);
                             continue;
                         }    
                    }
                     
                    
                    final int[] orgPoints = new int[2]; 
                    cellToPoint(clp.cellX, clp.cellY, orgPoints);
                    
                    final int[] destPoints = new int[2]; 
                    cellToPoint(vacant[0], vacant[1], destPoints);
                    
                    Animation moveAnimation = new TranslateAnimation(orgPoints[0] - destPoints[0], 0, orgPoints[1] - destPoints[1], 0);
                    moveAnimation.setDuration(MOVE_DURATION);
                    moveAnimation.setInterpolator(interpolator);
                    
                    cell.startAnimation(moveAnimation);
                    
                    clp.cellX = vacant[0];
                    clp.cellY = vacant[1];  
                    mNeedSaveCells.add(cell);
                }
                mLastDragCellXY = new int[] {-1,-1};
            } else {
                mLastDragAccept = false;
            }
            
            CellLayout.this.requestLayout();
            CellLayout.this.invalidate();
                
            }
        }

        private void changeOccupied(boolean[][] occupied, int cellX, int cellY,
                int spanX, int spanY, boolean status) {
            for (int x = 0; x < occupied.length; x++) {
                for (int y = 0; y < occupied[x].length; y++) {
                    if(x >= cellX && x < cellX + spanX && y >= cellY && y < cellY + spanY) {
                        occupied[x][y] = status;
//                        Log.d(TAG,"==end changeOccupied end change occupied==true"+x+"=="+y+"==spanX="+spanX+"==spanY="+spanY+"=statis="+status);
                    }
                }
            }            
        }

        void setViews(int[] targetXY, View dragChild, List<View> cells) {
            this.dragChild = dragChild;
            this.cells = cells;
            this.targetXY[0] = targetXY[0];
            this.targetXY[1] = targetXY[1];
        }
    }
    
    void saveMovedCells() {
        if(!mNeedSaveCells.isEmpty()){
//            Log.d(TAG,"entering saveMovedCells== needSaveCells="+mNeedSaveCells.size());
            for (View cell : mNeedSaveCells) {
                final ItemInfo cellInfo = (ItemInfo)cell.getTag();
                final LayoutParams clp = (LayoutParams) cell.getLayoutParams();
                //if(Launcher.LOGD)Log.d(TAG,"===saveMovedCells=x="+clp.cellX+"==y="+clp.cellY+"==screen="+cellInfo.screen);
                LauncherModel.addOrMoveItemInDatabase(getContext(), cellInfo,cellInfo.container, cellInfo.screen, clp.cellX, clp.cellY);             
            }
            mNeedSaveCells.clear();
        }
    }
    

    public boolean findSuitableVacantCell(int[] vacant, View cell,View dragChild) {
        ShortcutInfo info = null;
        boolean findVancant = false;
        if(cell.getTag() instanceof ShortcutInfo)
        {
            info = (ShortcutInfo)cell.getTag();
            int screenIndex = info.screen;
//            Log.d(TAG,"findSuitableBacantCell=="+this.getParent());
            if(this.getParent() instanceof Workspace)
            {
                Workspace mWorkspace = (Workspace)this.getParent();
                for(int i=screenIndex+1; i<mWorkspace.getChildCount();i++)
                {
                    CellLayout cellLayout = (CellLayout)mWorkspace.getChildAt(i);
                    boolean[][] occupied = cellLayout.mOccupied;
                    final boolean portrait = mPortrait;
                    final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
                    final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
                    cellLayout.findOccupiedCells(xCount, yCount, occupied, dragChild);
                    
                    if(cellLayout.findVacantCell(vacant,info.spanX, info.spanY, xCount, yCount, occupied,null))
                    {
                        //find it stop
                        findVancant = true;
                        info.newIndex = i;
                        break;
                    }
                }
                
                //if didn't find go on find pre 
                if(findVancant == false)
                {
                    for(int i=screenIndex-1; i>=0;i--)
                    {
                        CellLayout cellLayout = (CellLayout)mWorkspace.getChildAt(i);
                        boolean[][] occupied = cellLayout.mOccupied;
                        final boolean portrait = mPortrait;
                        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
                        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
                        cellLayout.findOccupiedCells(xCount, yCount, occupied, dragChild);
                        
                        if(cellLayout.findVacantCell(vacant,info.spanX, info.spanY, xCount, yCount, occupied,null))
                        {
                            //find it stop
                            findVancant = true;
                            info.newIndex = i;
                            break;
                        }
                    }
                    
                }
            }
        }
        
        return findVancant;
    }

    public void clearDrawRects() {
        mDragRect.setEmpty();
        invalidate();
    }

    public void restoreStatusBeforeMoved() {
        if(!mNeedSaveCells.isEmpty()){
//            if(Launcher.LOGD)Log.d(TAG,"entering restoreStatusBeforeMoved== needSaveCells="+mNeedSaveCells.size());
            
            for (View cell : mNeedSaveCells) {
                final ItemInfo cellInfo = (ItemInfo)cell.getTag();
                final LayoutParams clp = (LayoutParams) cell.getLayoutParams();
                //Log.d(TAG,"==before restoreStatusBeforeMoved==cellX="+cellInfo.cellX+"==cellY="+cellInfo.cellY+"==lpX="+clp.cellX+"==lpY="+clp.cellY);
               
                clp.cellX = cellInfo.cellX;
                clp.cellY = cellInfo.cellY;
            }
            mNeedSaveCells.clear();
            requestLayout();
        }
    }
   
}


