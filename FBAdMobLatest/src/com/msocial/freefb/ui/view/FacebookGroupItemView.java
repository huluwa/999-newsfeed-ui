package com.msocial.freefb.ui.view;

import com.msocial.freefb.R;
import com.msocial.freefb.ui.FacebookGroupActivity;
import com.msocial.freefb.ui.FacebookGroupDetailsActivity;
import com.msocial.freefb.ui.adapter.GroupParcel;
import oms.sns.service.facebook.model.Group;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class FacebookGroupItemView extends SNSItemView {
	private final String TAG="FacebookStatusView";
	
	private TextView  groupName;
	private ImageView  imageview;
	private Group     group;
	private Handler handler;

	
	public FacebookGroupItemView(Context ctx, AttributeSet attrs) 
	{
		super(ctx, attrs);		
		mContext = ctx;		
		handler = new Handler();
		setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
    }
	
	
	public FacebookGroupItemView(Context context, Group di) 
	{		
		super(context);
		mContext = context;
		group = di;
		handler = new Handler();
		Log.d(TAG, "call FacebookGroupItemView");
		
		init();
	}	
		
	private void init() 
	{
		Log.d(TAG,  "call FacebookGroupItemView init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();
		
		//container
		FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);		
		FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(paras);
		view.setVerticalScrollBarEnabled(true);
		addView(view);
		
		//child 1
		View v  = factory.inflate(R.layout.facebook_group_item, null);		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		view.addView(v);
		
		groupName  = (TextView)v.findViewById(R.id.facebook_group_name);	
		groupName.setText(group.name);
		
		imageview = (ImageView)v.findViewById(R.id.facebook_group_img_ui);
		imageview.setImageResource(R.drawable.friends);
        ImageRun imagerun = new ImageRun(handler, group.pic_samll, false, 0);
        imagerun.need_scale = true;
        imagerun.noimage = true;
        imagerun.width = 70;
        imagerun.setImageView(imageview);           
        imagerun.post(imagerun);
	}	
	
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	public void setGroupItem(Group di) 
	{
		group = di;		
		groupName.setText(group.name);
	}	
	public void chooseGroupListener() 
	{	
		//setOnClickListener(groupDetailClik);	    
	}
	
	View.OnClickListener groupDetailClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "groupDetailClik you click first one=");			 
			 //view details
			 Intent intent = new Intent(mContext, FacebookGroupDetailsActivity.class);
			 GroupParcel groupp = new GroupParcel(group);
			 intent.putExtra("group", groupp);			 
			 ((FacebookGroupActivity)mContext).startActivity(intent);
		}
	};


	@Override
	public String getText() 
	{		
		return "";
	}


	public Group getGroup() 
	{		
		return group;
	}
}

