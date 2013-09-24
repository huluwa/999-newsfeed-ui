package com.msocial.free.ui.view;

import com.msocial.free.R;
import com.msocial.free.ui.FacebookMessageActivity;
import com.msocial.free.ui.FacebookNotificationsActivity;
import oms.sns.service.facebook.model.FBNotifications;
import oms.sns.service.facebook.model.FBNotifications.*;
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

public class FacebookNotificationItemView extends SNSItemView{
    
	private NotifyBase item;
	private final static String TAG = "FacebookNotificationItemView";
	
	private ImageView imageview;
	private TextView  indicateView;
	private TextView  detailView;
	private Handler   handler;
	private TextView  notifyItemSizeIconView;
	private View      number_layout;
	
	 public NotifyBase getContent()
	 {
	     return item;
	 }
	 
     public FacebookNotificationItemView(Context ctx, AttributeSet attrs) 
	 {
		super(ctx, attrs);		
		mContext = ctx;		
		setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
        handler = new Handler();
    }
	
	public FacebookNotificationItemView(Context context, NotifyBase notify) 
	{		
		super(context);
		mContext = context;
		item = notify;
		Log.d(TAG, "call  FacebookNotificationItemView ctx:"+context);
		handler = new Handler();
		init();
	}
		
	private void init() 
	{
		Log.d(TAG,  "call FacebookNotificationItemView init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();
		
		//container
		FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);			
		FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(paras);
		view.setVerticalScrollBarEnabled(true);
		addView(view);
		
		//child 1
		View v  = factory.inflate(R.layout.facebook_notify_item, null);	
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		view.addView(v);	
		
		imageview     = (ImageView)v.findViewById(R.id.facebook_notify_img_ui);
		indicateView  =	(TextView)v.findViewById(R.id.facebook_notify_name);
		detailView    =	(TextView)v.findViewById(R.id.facebook_notify_detail);
		notifyItemSizeIconView = (TextView)v.findViewById(R.id.nofiy_item_icon_size);
		number_layout =(View)this.findViewById(R.id.facebook_number_layout);
		
		setUI();
	}	
	
	private void setUI()
	{
		 switch(item.type)
		 {
			 case NotifyType.Message:
			 {
				 FBNotifications.Messages msg = (FBNotifications.Messages)item;
				 if(msg!= null && msg.unread>0)
				 {
					 imageview.setImageResource(R.drawable.email_icon_40);
					 indicateView.setText(mContext.getString(R.string.sns_message_text));
					 updateNotifyItemIcon(msg.unread);
				 }
				 else
				 {
					 imageview.setImageResource(R.drawable.email_icon_40);
					 indicateView.setText(mContext.getString(R.string.sns_no_new_message));
					 updateNotifyItemIcon(0);
				 }				 
				 break;
			 }
			 case NotifyType.FriendRequests:
			 {
				 FBNotifications.FriendRequests msg = (FBNotifications.FriendRequests)item;
				 if(msg!= null && msg.uids.size()>0)
				 {
					 imageview.setImageResource(R.drawable.notice_summary_40);
					 indicateView.setText(mContext.getString(R.string.sns_friends_request));
					 updateNotifyItemIcon(msg.uids.size());
				 }
				 else
				 {
					 imageview.setImageResource(R.drawable.notice_summary_40);
					 indicateView.setText(mContext.getString(R.string.sns_no_pending_friend_request));
					 updateNotifyItemIcon(0);
				 }	
				 
				 //add a view to do confirm request
				 break;
			 }
			 case NotifyType.Pokes:
			 {
				 FBNotifications.Pokes msg = (FBNotifications.Pokes)item;
				 if(msg!= null && msg.unread>0)
				 {
					 imageview.setImageResource(R.drawable.poke_40);
					 indicateView.setText(mContext.getString(R.string.sns_new_pokes));
					 updateNotifyItemIcon(msg.unread);
				 }
				 else
				 {
					 imageview.setImageResource(R.drawable.poke_40);
					 indicateView.setText(R.string.sns_no_pending_pokes);
					 updateNotifyItemIcon(0);
				 }				
				 break;
			 }
			 case NotifyType.Shares:
			 {
				 FBNotifications.Shares msg = (FBNotifications.Shares)item;
				 if(msg!= null && msg.unread>0)
				 {
					 imageview.setImageResource(R.drawable.cmcc_list_message_new);
					 indicateView.setText(mContext.getString(R.string.sns_new_shares));
					 updateNotifyItemIcon(msg.unread);
				 }
				 else
				 {
					 imageview.setImageResource(R.drawable.cmcc_list_message_readsms);
					 indicateView.setText(R.string.sns_no_new_shares);
					 updateNotifyItemIcon(0);
				 }				
				 break;
			 }
			 case NotifyType.GroupInvites:
			 {
				 FBNotifications.GroupInvites msg = (FBNotifications.GroupInvites)item;
				 if(msg!= null && msg.uids.size()>0)
				 {
					 imageview.setImageResource(R.drawable.friends_40);
					 indicateView.setText(mContext.getString(R.string.sns_group_invites));
					 updateNotifyItemIcon(msg.uids.size());
				 }
				 else
				 {
					 imageview.setImageResource(R.drawable.friends_40);
					 indicateView.setText(R.string.sns_no_pending_group_invites);
					 updateNotifyItemIcon(0);
				 }		
				 break;
			 }
			 case NotifyType.EventInvites:
			 {
				 FBNotifications.EventInvites msg = (FBNotifications.EventInvites)item;
				 if(msg!= null && msg.uids.size()>0)
				 {
					 imageview.setImageResource(R.drawable.event_40);
					 indicateView.setText(mContext.getString(R.string.sns_events_invites));
					 updateNotifyItemIcon(msg.uids.size());
				 }
				 else
				 {
					 imageview.setImageResource(R.drawable.event_40);
					 indicateView.setText(R.string.sns_no_pending_event_invites);
					 updateNotifyItemIcon(0);
				 }		
				 break;
			 } 
		 }
	}
	
	private void updateNotifyItemIcon(int count)
	{
	    Log.d(TAG, "updateNotifyItemIcon count:"+count);
	    if(0 == count)
	    {
	        //imageview.setImageResource(R.drawable.inbox_small_nonew);
	        number_layout.setVisibility(View.GONE);
	    }
	    else
	    {
	        String cStr = String.valueOf(count);
	        if(count < 10)
	        {
	            notifyItemSizeIconView.setPadding(15, 5, 0, 0);
	        }
	        else if(count >10 && count < 99)
	        {
	            notifyItemSizeIconView.setPadding(13, 5, 0, 0);
	        }
	        else
	        {
	            cStr = "99+";
	            notifyItemSizeIconView.setPadding(8, 5, 0, 0);
	        }
	        
	        //imageview.setImageResource(R.drawable.inbox_small_new);
	        notifyItemSizeIconView.setText(cStr);
	        number_layout.setVisibility(View.VISIBLE);
	    }
	}
	
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
		

	public void setContentItem(NotifyBase notify) 
	{
		item = notify;		
		setUI();
	}
	
    public void chooseNotifyListener() 
	{	
		//setOnClickListener(actionClik);	    
	}
	
	View.OnClickListener actionClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "actionClik you click first one=");			 
			 //view details
			 switch(item.type)
			 {
				 case NotifyType.Message:
				 {
					 Intent intent = new Intent(mContext, FacebookMessageActivity.class);			 
					 ((FacebookNotificationsActivity)mContext).startActivity(intent);
					 break;
				 }
				 case NotifyType.FriendRequests:
				 {
					 //do 
					 Log.d(TAG, "clicked friends requests");
					 break;
				 }
			 }
		}
	};

	@Override
	public String getText() 
	{		 
		return "";
	}
		
}
