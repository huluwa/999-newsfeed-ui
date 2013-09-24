package com.tormas.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CloseView extends ImageView{
    private final String TAG = "CloseView";
    Bitmap mBitmap;
    
    public CloseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.close_drag_view);
    }

    public CloseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.close_drag_view);
    }

    public CloseView(Context context) {
        super(context);
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.close_drag_view);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mBitmap != null && mBitmap.isRecycled() == false)
        {
            mBitmap.recycle();
        }
    }
    
    
}
