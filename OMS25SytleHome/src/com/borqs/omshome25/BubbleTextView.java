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

import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.TableMaskFilter;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;

import android.text.TextUtils;
import android.os.SystemProperties;

/**
 * TextView that draws a bubble behind the text. We cannot use a LineBackgroundSpan
 * because we want to make the bubble taller than the text and TextView's clip is
 * too aggressive.
 */
public class BubbleTextView extends TextView {
    static final String TAG = "oms2.5Launcher.BubbleTextView";
    static final float CORNER_RADIUS = 8.0f;
    static final float PADDING_H = 5.0f;
    static final float PADDING_V = 1.0f;

    private final RectF mRect = new RectF();
    private Paint mPaint;

    private boolean mBackgroundSizeChanged;
    private Drawable mBackground;
    private float mCornerRadius;
    private float mPaddingH;
    private float mPaddingV;

//    private Bitmap mSelectionBitmap;
//    private Canvas mSelectionCanvas;
    private float mDensity;
    private static Paint mBlurPaint = new Paint();
    private static Paint sNullPaint = new Paint();
    private static Paint sGlowColorPressedPaint = new Paint();
    private int ignore_span       = 0;
    private static int ignore_len = -1;
    
    public BubbleTextView(Context context) {
        super(context);
        init();
    }

    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BubbleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
	public void getHitRect(Rect outRect) {
    	        
        //Rect tmp = new Rect();
		super.getHitRect(outRect);
		outRect.left  = outRect.left    + ignore_span;
		outRect.right = outRect.right   - ignore_span;		
		outRect.top    = outRect.top    + ignore_span;
		outRect.bottom = outRect.bottom - ignore_span;
	}

	private void init() {
        setFocusable(true);
        setFocusableInTouchMode(false);
        mBackground = getBackground();
        setBackgroundDrawable(null);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        final Resources res = getResources();
        mPaint.setColor(res.getColor(R.color.bubble_dark_background));

        mDensity = res.getDisplayMetrics().density;
        
        mCornerRadius = CORNER_RADIUS * mDensity;
        mPaddingH = PADDING_H * mDensity;
        //noinspection PointlessArithmeticExpression
        mPaddingV = PADDING_V * mDensity;
        setMaxLines(2);
        //setEllipsize(TextUtils.TruncateAt.END);
        if(ignore_len < 0)
        {
        	ignore_len = 0;
        	try{
        	    ignore_len = Integer.parseInt(SystemProperties.get("home_touch_ignore_span", "0"));
        	}catch(Exception ne){}
        }
        
        ignore_span = (int)(ignore_len*mDensity);
        mBlurPaint.setMaskFilter(new BlurMaskFilter(3*mDensity, BlurMaskFilter.Blur.NORMAL));
        sGlowColorPressedPaint.setColor(0xff69b2dc);
        sGlowColorPressedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
    }

    @Override
    protected boolean setFrame(int left, int top, int right, int bottom) {
        if (mLeft != left || mRight != right || mTop != top || mBottom != bottom) {
            mBackgroundSizeChanged = true;
        }
        return super.setFrame(left, top, right, bottom);
    }
  
    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mBackground || super.verifyDrawable(who);
    }

    @Override
    protected void drawableStateChanged() {
        Drawable d = mBackground;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
        super.drawableStateChanged();
    }

    @Override
    public void draw(Canvas canvas) {
        final Drawable background = mBackground;
        if (background != null) {
            final int scrollX = mScrollX;
            final int scrollY = mScrollY;

            if (mBackgroundSizeChanged) {
                background.setBounds(0, 0,  mRight - mLeft, mBottom - mTop);
                mBackgroundSizeChanged = false;
            }

            if ((scrollX | scrollY) == 0) {
                background.draw(canvas);
            } else {
                canvas.translate(scrollX, scrollY);
                background.draw(canvas);
                canvas.translate(-scrollX, -scrollY);
            }
        }

        final Layout layout = getLayout();
        final RectF rect = mRect;
        final int left = getCompoundPaddingLeft();
        final int top = getExtendedPaddingTop();

        rect.set(left + layout.getLineLeft(0) - mPaddingH,
                top + layout.getLineTop(0) -  mPaddingV,
                Math.min(left + layout.getLineRight(0) + mPaddingH, mScrollX + mRight - mLeft),
                top + layout.getLineBottom(0) + mPaddingV);
        canvas.drawRoundRect(rect, mCornerRadius, mCornerRadius, mPaint);

        super.draw(canvas);
    }
    
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = super.onTouchEvent(event);
		int action = event.getAction();
		switch(action){
		case MotionEvent.ACTION_DOWN:
			 setPressedIcon();
			 break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			 resetIcon();
			 break;
		default:
		}
		
		return ret;
	}
	
	private Drawable rawDraw;
    public void setPressedIcon(){
       rawDraw = getCompoundDrawables()[1];//top drawable
	   final Bitmap rawicon = Utilities.getExtendBitmapFromDrawable(rawDraw, getContext());
	   Bitmap newicon = rawicon.copy(Config.ARGB_8888, true);
	   Canvas canvas = new Canvas(newicon);
	   
	   drawSelectedAllAppsBitmap(canvas, newicon, rawicon);
	   
	   Drawable draw = new BitmapDrawable(newicon);
	   ((BitmapDrawable)draw).setTargetDensity(getResources().getDisplayMetrics());
	   setCompoundDrawablesWithIntrinsicBounds(null,draw, null, null);
    }
  
    public void resetIcon(){
	    setCompoundDrawablesWithIntrinsicBounds(null, rawDraw, null, null);
    }
	   
    void drawSelectedAllAppsBitmap(Canvas dest, Bitmap destBitmap, Bitmap src) {
        int[] xy = new int[2];
        Bitmap mask = destBitmap.extractAlpha(mBlurPaint, xy);

        dest.drawBitmap(mask, xy[0], xy[1], sGlowColorPressedPaint);
        dest.drawBitmap(src,0, 0, sNullPaint);
        mask.recycle();
        
         /* effect 2
	        Paint sGlowColorPressedPaint = new Paint();
	        Paint sBlurPaint = new Paint();
	        sBlurPaint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
	        sGlowColorPressedPaint.setColor(0xff69b2dc);
	        sGlowColorPressedPaint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 30));
	        sGlowColorPressedPaint.setAlpha(210);
	        sGlowColorPressedPaint.setAntiAlias(true);
	   
	        dest.drawColor(0, PorterDuff.Mode.CLEAR);
	        dest.drawBitmap(src,0, 0, new Paint());

	        int[] xy = new int[2];
	        Bitmap mask = destBitmap.extractAlpha(sBlurPaint, xy);

	        float px = (destWidth - src.getWidth()) / 2;
	        float py = (destHeight - src.getHeight()) / 2;
	        dest.drawBitmap(mask, px + xy[0], py + xy[1],sGlowColorPressedPaint);

	        mask.recycle();*/
        
    }
	    
	@Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBackground.setCallback(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBackground.setCallback(null);
    }
}
