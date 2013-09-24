package com.borqs.omshome25;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class IndicatorWithMissCallNumberView extends View{
//	private static final String TAG = "oms2.5Launcher.IndicatorWithMissCallNumberView";
    private Paint textpointPaint;
    private Resources res;
    private int    mMissCount;
    private static Bitmap toomuchbackground;
    private static Bitmap background;    
    
    public IndicatorWithMissCallNumberView(Context context, AttributeSet attrs){
    	super(context, attrs);
    }

    private void initZoneView(Context context) {
    	res = context.getResources();		
    	
    	TextView tv = new TextView(context);
    	textpointPaint = tv.getPaint();
    	//textpointPaint = new Paint();
    	textpointPaint.setColor(Color.WHITE);
    	int textsize = res.getDimensionPixelSize(R.dimen.misscall_textSize);
    	textpointPaint.setTypeface(Typeface.DEFAULT_BOLD);
    	//textpointPaint.setTypeface(Typeface.SANS_SERIF);
    	textpointPaint.setTextSize(textsize);
    	//textpointPaint.setAlpha(250);
    	tv = null;
		
		
//	    bitmapWidth  = res.getDimensionPixelSize(R.dimen.misscall_textWidth);
//	    bitmapHeight = res.getDimensionPixelSize(R.dimen.misscall_textWidth);
	    
	    toomuchbackground = BitmapFactory.decodeResource(res, R.drawable.cmcc_home_bottom_misscall_toomany_icon);
	    background = BitmapFactory.decodeResource(res, R.drawable.cmcc_home_bottom_misscall_icon);	   
	}
    
    public void setMissCallCount(int missCount)
    {
    	mMissCount = missCount;
    	if(mMissCount > 0)
    	{	
    		invalidate();
    	}
    }    
   
	@Override
	protected void onDraw(Canvas canvas) 
    {
    	drawMissCallIndicator(canvas, mMissCount);
    }
    
    private void drawMissCallIndicator(Canvas canvas, int missCallCount) 
    {    	
//    	if(backRect == null )
//    	{
//    		backRect = new Rect(0, 0, bitmapWidth, bitmapHeight);
//    	}    	
    	
    	if(background == null){
    		initZoneView(getContext());
    	}
        
    	if(mMissCount >0 && mMissCount < 99)
    	{
    		
    		canvas.drawBitmap(background, new Rect(0, 0, background.getWidth(), background.getHeight()), new Rect(0, 0, background.getWidth(), background.getHeight()), null);
    		
    		//draw indicator    		
    		String miss = String.valueOf(mMissCount);    		
    		int width = (int)(textpointPaint.measureText(miss));
    		FontMetrics fm  = textpointPaint.getFontMetrics();
//    		int height = (int)(Math.abs(fm.ascent) + Math.abs(fm.descent));
    		canvas.drawText(miss, ((background.getWidth()-width)/2), (background.getHeight()/2 +Math.abs(fm.descent))-1 , textpointPaint);    		
    	}
    	else
    	{
    		canvas.drawBitmap(toomuchbackground, new Rect(0, 0, toomuchbackground.getWidth(), toomuchbackground.getHeight()), new Rect(0, 0, toomuchbackground.getWidth(), toomuchbackground.getHeight()), null);
    	}        
    }
    
    public static void clearStaticData(){       
    	
//    	backRect = null;
	    if(background != null)
    	{
	    	background.recycle();
	    	background = null;
    	}
	    
	    if(toomuchbackground != null)
    	{
	    	toomuchbackground.recycle();
	    	toomuchbackground = null;
    	}
	    
//       bitmapWidth = 0;
//       bitmapHeight = 0;
    }
}
