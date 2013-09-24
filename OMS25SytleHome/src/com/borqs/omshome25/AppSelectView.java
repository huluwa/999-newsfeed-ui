package com.borqs.omshome25;

import com.borqs.omshome25.R;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class AppSelectView extends LinearLayout
{
	protected Context mContext;
	private TextView appName;
	private CheckBox chekbox;
	private ApplicationInfo appInfo;
	
	public AppSelectView(Context context) {
		super(context);	
		mContext = context;
	}
	
	public AppSelectView(Context ctx, AttributeSet attrs) 
	{
		super(ctx, attrs);
		mContext = ctx;
	}
	
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	private void init() 
	{	
		chekbox    = (CheckBox)findViewById(R.id.allapps_item_check);
		appName   = (TextView)findViewById(R.id.allapps_item);
		
		setOnClickListener(new View.OnClickListener() {			
			public void onClick(View arg0) {
				setSelectItem(!appInfo.selected);	
			}
		});
	}	
	public void setSelectItem(boolean sel)
	{		
		appInfo.selected = sel;
		chekbox.setChecked(sel);
	}
	
	public void setUI()
	{
		appName.setText(appInfo.title);
		
		final BitmapDrawable bd = new BitmapDrawable(appInfo.iconBitmap);
		bd.setTargetDensity(getResources().getDisplayMetrics());
		appName.setCompoundDrawablesWithIntrinsicBounds(bd, null, null, null);
		appName.setText(appInfo.title);
		
		setSelectItem(appInfo.selected);		
	}
	
	public void setAppInfo(ApplicationInfo item)
	{
		appInfo = item;
		setUI();
	}
}
