package com.tormas.litetwitter.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import com.tormas.litetwitter.R;
import com.tormas.litetwitter.ui.adapter.FacebookStatusItem;

import java.io.File;

public class FacebookStatusContentItemView extends SNSItemView 
{
	private FacebookStatusItem item;
	
	private final String TAG="FacebookStatusContentItemView";
	
	private ImageView imgView;
	private TextView  sizeView;
	private TextView  userName;
	private Button    removeBotton;
    Handler handler;
    public FacebookStatusItem getStatusContent()
    {
    	return item;
    }
	public FacebookStatusContentItemView(Context ctx, AttributeSet attrs) 
	{
		super(ctx, attrs);		
		mContext = ctx;		
		setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
        handler = new Handler();
    }
	
	public FacebookStatusContentItemView(Context context, FacebookStatusItem di) 
	{		
		super(context);
		mContext = context;
		item = di;
		
		Log.d(TAG, "call  FacebookStatusContentItemView");
		handler = new Handler();
		init();
	}
		
	private void init() 
	{
		Log.d(TAG,  "call FacebookStatusContentItemView init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();
		
		//container
		FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);
		FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(paras);
		view.setVerticalScrollBarEnabled(true);
		addView(view);
		
		//child 1
		View v  = factory.inflate(R.layout.facebook_status_update_item, null);		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		view.addView(v);
		
		imgView      = (ImageView)v.findViewById(R.id.facebook_status_img_ui);
		userName     = (TextView)v.findViewById(R.id.facebook_status_img_name_ui);		
		removeBotton = (Button)v.findViewById(R.id.remove_button);		
		sizeView     = (TextView)v.findViewById(R.id.facebook_status_img_size_ui);
		setUI();
	}	
	
	private void setUI()
	{
	    sizeView.setVisibility(View.VISIBLE);
	    long filesize = 0;
		if(item.type == FacebookStatusItem.ContentType.IMAGE || item.type == FacebookStatusItem.ContentType.CAMERA)
		{
			try{
			    filesize = (new File(Uri.parse(item.url).getPath()).length()/1024);
			}catch(Exception ne){}
			//if < 10K, show
		    Log.d(TAG, "image view is "+imgView);
			if(new File(item.url).exists() && (new File(item.url).length()/1024 < 10))
			{
			    //localfile 
			    Bitmap tmp = BitmapFactory.decodeFile(item.url);
                if(tmp != null)
                { 
                    imgView.setImageBitmap(tmp);
                }
			}
			else
			{
			    Log.d(TAG, "user default="+ item.url);
				imgView.setImageResource(R.drawable.noimage);
			}
			sizeView.setText(mContext.getString(R.string.sns_size)+String.format(" %1$sk", filesize));
		}	
		else if(item.type == FacebookStatusItem.ContentType.LINK)
		{
			if(item.bmp == null)
			    imgView.setImageResource(R.drawable.link_icon);
			else
				imgView.setImageBitmap(item.bmp);
			
			sizeView.setText(item.name);
		}
		else if(item.type == FacebookStatusItem.ContentType.VIDEO)
		{
			try{
			    filesize = (new File(Uri.parse(item.url).getPath()).length()/1024);
			}catch(Exception ne){} 
			imgView.setImageResource(R.drawable.cmcc_mediacenter_video_thumbnail);
			sizeView.setText(mContext.getString(R.string.sns_size)+String.format(" %1$sk", filesize));
		}
		
		if(item.uploadStatus == FacebookStatusItem.Status.SUC_UPLOAD)
		{
			removeBotton.setBackgroundResource(R.drawable.upload_good);
		}
		else
		{
			removeBotton.setBackgroundResource(R.drawable.delete_btn_style);
		}
			
		userName.setText(item.url);
	}
	
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
		
	public void chooseRemoveListener()
	{
		removeBotton.setOnClickListener(removeOnClik);
	}
	
	View.OnClickListener removeOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "removeOnClik you click first one="+item.name);			 
		}
	};
	
	public void setContentItem(FacebookStatusItem di) 
	{
		item = di;		
		setUI();
	}	
	public void setUploadedBackGround(boolean uploaded) 
	{
	    if(uploaded)
		{
			removeBotton.setBackgroundResource(R.drawable.upload_good);
		}
		else
		{
			removeBotton.setBackgroundResource(R.drawable.upload_bad);
		}		
	}
	
	public void hiddenRemoveBotton(){
		removeBotton.setVisibility(View.GONE);
	}
	@Override
	public String getText() 
	{		
		return "";
	}
}

