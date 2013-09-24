package com.borqs.omshome25;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.widget.ImageView;
import android.util.AttributeSet;

public class PageIndicatorView extends ImageView{
//    private static final String TAG = "oms2.5Launcher.PageIndicator";
    private Paint pointPaint;    
    private Paint currentPointPaint;
    private Paint the3pointPaint;
    private static Bitmap currentBitmap;
    private static Bitmap normalBitmap;
    private Bitmap indicatorBitmap;
    private static int bitmapWidth;
    private static int bitmapHeight;
    private static float density;
    
	public PageIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    private void initZoneView(Context context) {
		pointPaint = new Paint();
		pointPaint.setColor(Color.WHITE);
		pointPaint.setAlpha(60);
		
		currentPointPaint = new Paint();
		currentPointPaint.setColor(Color.WHITE); 
		
		final Resources res = getResources();
		
		currentBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_indicator_current);
	    normalBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_indicator_normal);
	    bitmapWidth = res.getDimensionPixelSize(R.dimen.button_bar_width);
	    bitmapHeight = res.getDimensionPixelSize(R.dimen.button_bar_height);
	    pageIndexIcon = BitmapFactory.decodeResource(res, R.drawable.cmcc_launcher_ic_indicator_big_normal);
	    pageNormalIndexIcon = BitmapFactory.decodeResource(res, R.drawable.cmcc_launcher_ic_indicator_small_normal);
	    
	    pageIndexIcon.setDensity(res.getDisplayMetrics().densityDpi);
	    pageNormalIndexIcon.setDensity(res.getDisplayMetrics().densityDpi);
	    normalBitmap.setDensity(res.getDisplayMetrics().densityDpi);
	    currentBitmap.setDensity(res.getDisplayMetrics().densityDpi);
	    
	    density = res.getDisplayMetrics().density;
	    
	    the3pointPaint = new Paint();
	    the3pointPaint.setColor(Color.BLACK);
	    the3pointPaint.setTypeface(Typeface.DEFAULT_BOLD);
	    the3pointPaint.setTextSize(16*density);
	    the3pointPaint.setAntiAlias(true);
	}
    
    public void drawPageIndicator(int currentPageIndex, int screenCount) {
//    	if(Launcher.LOGD)Log.d(TAG, "drawPageIndicator currentPageIndex:"+currentPageIndex+" screenCount:"+screenCount);
    	if(bitmapWidth == 0){
    		initZoneView(getContext());
    	}
    	
    	int size = 10;
    	Bitmap tmpBitmap = indicatorBitmap;
    	indicatorBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
        if(bitmapWidth > bitmapHeight){
        	size = bitmapWidth / 7;  // divide 9 to get proper width, not relative with screen count.
        	final Canvas canvas = new Canvas(indicatorBitmap);
        	int left =  (bitmapWidth - screenCount*size)/2 ;
        	int top = 5;
        	if(density>1.f){
        		top = 1; 
        	}
        	
        	//        	if(Launcher.LOGD)Log.d(TAG,"drawPageIndicator  width:"+getWidth()+" height:"+getHeight()+" left:"+left+" top:"+0+" currentPageIndex:"+currentPageIndex+" screenCount:"+screenCount);
        	for(int i = 0; i < screenCount; i++){            
        		if(currentPageIndex == i) {
        			canvas.drawBitmap(currentBitmap, left, top, null);
        		} else {
        			canvas.drawBitmap(normalBitmap, left, top, null);
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
    
    private Bitmap pageIndexIcon, pageNormalIndexIcon;
    public void drawPageIndicatorWith3Point(int currentPageIndex, int screenCount) {
//    	if(Launcher.LOGD)Log.d(TAG, "drawPageIndicator currentPageIndex:"+currentPageIndex+" screenCount:"+screenCount);
    	int pageIconTop = Math.round(14*density); 
    	int pageNormalIconTop = Math.round(22*density);
    	int decreaseWidth = 0;
    	int deltaTextX = Math.round(-4 * density);
    	int deltaTextY = Math.round(3 * density); //3
    	
    	Bitmap tmpBitmap = indicatorBitmap;
    	indicatorBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
    	
        if(bitmapWidth > bitmapHeight){
        	final Canvas canvas = new Canvas(indicatorBitmap);
        	int left =  (bitmapWidth - pageIndexIcon.getWidth() - (screenCount-1)*pageNormalIndexIcon.getWidth())/2 ;
        	
        	if(screenCount == 1){
        		canvas.drawBitmap(pageIndexIcon, left, pageIconTop, null);
        		canvas.drawText(String.valueOf(currentPageIndex+1), left+pageIndexIcon.getWidth()/2 + deltaTextX, pageIconTop+pageIndexIcon.getHeight()/2 + deltaTextY, the3pointPaint);
        	}else if(currentPageIndex == 0){
        		canvas.drawBitmap(pageIndexIcon, left, pageIconTop, null);
        		canvas.drawText(String.valueOf(currentPageIndex+1), left+pageIndexIcon.getWidth()/2 + deltaTextX, pageIconTop+pageIndexIcon.getHeight()/2 + deltaTextY, the3pointPaint);
        		canvas.drawBitmap(pageNormalIndexIcon, left+pageIndexIcon.getWidth() - decreaseWidth, pageNormalIconTop, null);
        		if(screenCount>2){
        			canvas.drawBitmap(pageNormalIndexIcon, left+pageNormalIndexIcon.getWidth()+pageIndexIcon.getWidth() - 2* decreaseWidth, pageNormalIconTop, null);
        		}
        	}else if(currentPageIndex == screenCount -1){
        		canvas.drawBitmap(pageNormalIndexIcon, left, pageNormalIconTop, null);
        		if(screenCount>2){
        			canvas.drawBitmap(pageNormalIndexIcon, left+pageNormalIndexIcon.getWidth() - decreaseWidth, pageNormalIconTop, null);
            		canvas.drawBitmap(pageIndexIcon, left+2*pageNormalIndexIcon.getWidth() - decreaseWidth*2, pageIconTop, null);
            		canvas.drawText(String.valueOf(currentPageIndex+1), pageIndexIcon.getWidth()/2+left+2*pageNormalIndexIcon.getWidth() - decreaseWidth*2 + deltaTextX, pageIconTop+pageIndexIcon.getHeight()/2 + deltaTextY, the3pointPaint);
        		}else{
        			canvas.drawBitmap(pageIndexIcon, left+pageNormalIndexIcon.getWidth() - decreaseWidth, pageIconTop, null);
        			canvas.drawText(String.valueOf(currentPageIndex+1), pageIndexIcon.getWidth()/2+left+pageNormalIndexIcon.getWidth() - decreaseWidth + deltaTextX, pageIconTop+pageIndexIcon.getHeight()/2 + deltaTextY, the3pointPaint);
        		}
        	}else{
        		canvas.drawBitmap(pageNormalIndexIcon, left, pageNormalIconTop, null);
        		canvas.drawBitmap(pageIndexIcon, left+pageNormalIndexIcon.getWidth() - decreaseWidth, pageIconTop, null);
        		canvas.drawText(String.valueOf(currentPageIndex+1), pageIndexIcon.getWidth()/2+left+pageNormalIndexIcon.getWidth() - decreaseWidth + deltaTextX, pageIconTop+pageIndexIcon.getHeight()/2 + deltaTextY, the3pointPaint);
        		canvas.drawBitmap(pageNormalIndexIcon, left+pageNormalIndexIcon.getWidth()+pageIndexIcon.getWidth() - decreaseWidth, pageNormalIconTop, null);
        	}
        }else{  
//        	size = normalBitmap.getHeight();
//        	final Canvas canvas = new Canvas(indicatorBitmap);
//        	int top =  (bitmapHeight - screenCount*size)/2 ;
//        	
//        	for(int i = 0; i < screenCount; i++){            
//        		if(currentPageIndex == i) {
//        			canvas.drawBitmap(currentBitmap, 0, top, null);
//        		} else {
//        			canvas.drawBitmap(normalBitmap, 0, top, null);
//        		}
//        		
//        		top = top + size; 
//        	}
        }
        
        if(tmpBitmap != null){ 
        	tmpBitmap.recycle();
        	tmpBitmap = null;
        }
        setImageBitmap(indicatorBitmap);
    }
}
