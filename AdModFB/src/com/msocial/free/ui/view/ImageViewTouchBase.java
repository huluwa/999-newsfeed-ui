package com.msocial.free.ui.view;

import com.msocial.free.ui.FacebookPhotoCommentsActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Config;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class ImageViewTouchBase extends ImageView 
{
    public static final String TAG="ImageView";
    public Context context;
    public ImageViewTouchBase(Context context, AttributeSet attrs) {
        super(context, attrs);       
    }
    public ImageViewTouchBase(Context context) {
        super(context);
    }
    
    public ImageViewTouchBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        Log.d(TAG, "onKeyDown keycode="+keyCode + " event="+event);
        switch(event.getAction())
        {
            case KeyEvent.ACTION_DOWN:
            {
                break;
            }
            case KeyEvent.ACTION_UP:
            {
                break;
            }
        }
        return true;
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) 
    {
        Log.d(TAG, "onKeyUp keycode="+keyCode + " event="+event);
        
        scrollTo(-100+getLeft(), -100+getTop());
        return true;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) 
    {
        switch (ev.getAction()) 
        {
            case MotionEvent.ACTION_DOWN: 
            {
                offsetx = 0;
                offsety = 0;
                
                Log.d(TAG, "ACTION_DOWN ="+ev);
                startx = (int)ev.getX();                
                starty = (int)ev.getY();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                Log.d(TAG, "ACTION_UP xspan="+ev);
                
                startx = 0;
                starty = 0;
                break;
            }  
            case MotionEvent.ACTION_MOVE:
            {
                Log.d(TAG, "ACTION_MOVE xspan="+ev);
                int mx = (int)ev.getX();                
                int my = (int)ev.getY();
              
                int nSpanx = startx-mx;
                int nSpany = starty-my;
                
                Log.d(TAG, "nSpanx="+nSpanx + " nSpany="+nSpany + " mx="+mx + " my="+my + " width="+getWidth() + " left="+getLeft());
                if(getRight()> 100 && getBottom()>100 && getTop() < 400 &&  getLeft() < 400)
                {
                    scrollBy(nSpanx, nSpany);
                    //return true;
                }
                
                startx = mx;
                starty = my;
                
                break;
            }
        }
        return true;
    }
    
    
    
    @Override
	public void setImageBitmap(Bitmap bm) {
		if(context != null && FacebookPhotoCommentsActivity.class.isInstance(context) && bm!=null)
		{
			//reset photo_image size;
			int width = bm.getWidth();
			int height = bm.getHeight();
			int screenWidth = ((FacebookPhotoCommentsActivity)context).getWindowManager().getDefaultDisplay().getWidth();
			int screenHeight = ((FacebookPhotoCommentsActivity)context).getWindowManager().getDefaultDisplay().getHeight();
		    
			if(screenWidth < screenHeight)
			{
			    //portrait mode
			    screenHeight = (screenWidth * height)/width;
	            
	            if(width < screenWidth)
	            {
	                LayoutParams params = this.getLayoutParams();
	                params.width = screenWidth;
	                params.height = screenHeight;
	            }
			}
			else
			{
			    //landspace mode
                screenWidth = (screenHeight * width)/height;
                if(width < screenWidth)
                {
                    LayoutParams params = this.getLayoutParams();
                    params.width = screenWidth;
                    params.height = screenHeight;
                }
			}
			
		}
		super.setImageBitmap(bm);
		//reset PhotoCommentsActivity
	}



	int offsetx=0;
    int offsety=0;
    
    int startx = 0;
    int starty = 0;
   
    
}

