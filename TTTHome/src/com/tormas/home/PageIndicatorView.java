package com.tormas.home;

import com.tormas.home.Launcher;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.widget.ImageView;
import android.util.AttributeSet;
import android.util.Log;

public class PageIndicatorView extends ImageView{
    private static final String TAG = "Launcher.PageIndicator";
    private Paint pointPaint;    
    private Paint currentPointPaint;
    private static Bitmap currentBitmap;
    private static Bitmap normalBitmap;
    private Bitmap indicatorBitmap;
    private static int bitmapWidth;
    private static int bitmapHeight;
    
	public PageIndicatorView(Context context) {
		super(context);
        initZoneView(context);
	}

	public PageIndicatorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        initZoneView(context);
	}

	public PageIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
        initZoneView(context);
	}

    private void initZoneView(Context context) {
		pointPaint = new Paint();
		pointPaint.setColor(Color.WHITE);
		pointPaint.setAlpha(60);
		
		currentPointPaint = new Paint();
		currentPointPaint.setColor(Color.WHITE); 
		
		final Resources res = context.getResources();
		currentBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_indicator_current);
	    normalBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_indicator_normal);
	    bitmapWidth = res.getDimensionPixelSize(R.dimen.button_bar_width);
	    bitmapHeight = res.getDimensionPixelSize(R.dimen.button_bar_height);
	}
    
    public void drawPageIndicator(int currentPageIndex, int screenCount) {
    	if(Launcher.LOGD)Log.d(TAG, "drawPageIndicator currentPageIndex:"+currentPageIndex+" screenCount:"+screenCount);
    	final Resources resource = getResources();
    	
    	if(bitmapWidth == 0 || bitmapHeight == 0){
		    bitmapWidth = resource.getDimensionPixelSize(R.dimen.button_bar_width);
		    bitmapHeight = resource.getDimensionPixelSize(R.dimen.button_bar_height);
		    currentBitmap = BitmapFactory.decodeResource(resource, R.drawable.ic_indicator_current);
		    normalBitmap = BitmapFactory.decodeResource(resource, R.drawable.ic_indicator_normal);
		    if(Launcher.LOGD)Log.d(TAG, "drawPageIndicator bitmapWidth=0, reget w:"+bitmapWidth+" h:"+bitmapHeight);
    	}
    	
    	int size = 10;
    	Bitmap tmpBitmap = indicatorBitmap;
    	indicatorBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
        if(resource.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
        	size = bitmapWidth / 7;  // divide 9 to get proper width, not relative with screen count.
        	final Canvas canvas = new Canvas(indicatorBitmap);
        	int left =  (bitmapWidth - screenCount*size)/2 ;
        	
//        	if(Launcher.LOGD)Log.d(TAG,"drawPageIndicator  width:"+getWidth()+" height:"+getHeight()+" left:"+left+" top:"+0+" currentPageIndex:"+currentPageIndex+" screenCount:"+screenCount);
        	for(int i = 0; i < screenCount; i++){            
        		if(currentPageIndex == i) {
        			canvas.drawBitmap(currentBitmap, left, 0, null);
        		} else {
        			canvas.drawBitmap(normalBitmap, left, 0, null);
        		}
        		
        		left = left + size; 
        	}
        }else{
        	size = normalBitmap.getHeight();
        	final Canvas canvas = new Canvas(indicatorBitmap);
        	int top =  (bitmapHeight - screenCount*size)/2 ;
        	
        	for(int i = 0; i < screenCount; i++){            
        		if(currentPageIndex == i) {
        			canvas.drawBitmap(currentBitmap, 0, top, null);
        		} else {
        			canvas.drawBitmap(normalBitmap, 0, top, null);
        		}
        		
        		top = top + size; 
        	}
        }
        
        if(tmpBitmap != null){ 
        	tmpBitmap.recycle();
        	tmpBitmap = null;
        }
        setImageBitmap(indicatorBitmap);
    }
    
    public static void clearStaticData(){
       if(null != currentBitmap){
    	   currentBitmap.recycle();
    	   currentBitmap = null;
       }
       
       if(null != normalBitmap){
    	   normalBitmap.recycle();
    	   normalBitmap = null;
       }
       
       bitmapWidth = 0;
       bitmapHeight = 0;
    }
}
