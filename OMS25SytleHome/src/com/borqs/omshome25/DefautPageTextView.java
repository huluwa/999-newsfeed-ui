package com.borqs.omshome25;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

public class DefautPageTextView extends TextView {

	Resources mRes;
	static final int mX = 0;
	static final int mY = 1;
	
	public DefautPageTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mRes = getContext().getResources();
	} 

	public DefautPageTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mRes = getContext().getResources();
	}

	public DefautPageTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mRes = getContext().getResources();
	}
	
	@Override
	public void setText(CharSequence text, BufferType type) {
		// TODO Auto-generated method stub
		if (mRes == null) {
			mRes = getContext().getResources();	
		}
		
		if (mRes.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			super.setText(text, type);
		} else {
			super.setText("", type);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (mRes == null) {
			mRes = getContext().getResources();	
		}
		
		if (mRes.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			DrawLandscapeTextView(canvas);
		}
	}
	
	private void DrawLandscapeTextView(Canvas canvas) {
		CharSequence text = mRes.getString(R.string.set_default_screen);
		TextPaint textPaint;
		textPaint = new TextPaint();
		textPaint.setTypeface(Typeface.DEFAULT);
		textPaint.setColor(Color.WHITE);
		textPaint.setAntiAlias(true);
		textPaint.setTextAlign(Align.CENTER);
	//float textSize = mRes.getDimension(R.dimen.defaultpage_textSize)-1.f;
	//	textPaint.setTextSize(textSize);	
		
		canvas.save();
		int rotateX = this.getWidth()/2;
		int rotateY = this.getHeight()/2;
	    canvas.rotate(-90,rotateX,rotateY);
	  
		canvas.drawText(text, 0, text.length(),rotateX,rotateY + 2*textPaint.descent(), textPaint);
		canvas.restore();
	}
}
