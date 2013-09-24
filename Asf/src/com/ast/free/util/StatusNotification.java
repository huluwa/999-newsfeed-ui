package com.ast.free.util;

import com.ast.free.R;
import com.ast.free.ui.FacebookBaseActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RemoteViews;

public class StatusNotification {

	public static final String TAG = "StatusNotification";
	private Context mContext;
	private NotificationManager mNM;
	
	public static final int NOTIFICATION_UPDATE_STATUS = 1;
	public static final int NOTIFICATION_notice        = 2;

	public StatusNotification(Context ctx) {
		mContext = ctx;
		mNM = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	public void setContext(Context ctx)
	{
		mContext = ctx;
		mNM = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void notify(String text, int icon) {
		notify(text, icon, Notification.FLAG_ONGOING_EVENT);
	}

	public void notify(int id_text, int icon) {
		notify(mContext.getResources().getString(id_text), icon);
	}

	public void notifyOnce(String text, int icon, int milliseconds) 
	{
		notify(text, icon, Notification.FLAG_AUTO_CANCEL);
		if (milliseconds > 0) 
		{
			if (mHandlerClear == null) 
			{
				mHandlerClear = new Handler() 
				{
					public void handleMessage(Message msg) 
					{
						if (msg.what == MSG_CLEAR)
						{
							cancel();
						}
					}
				};
			}
			
			Message msg = mHandlerClear.obtainMessage(MSG_CLEAR);
			mHandlerClear.sendMessageDelayed(msg, milliseconds);
		}
	}

	public void notifyOnce(int id_text, int icon, int milliseconds) 
	{
		notifyOnce(mContext.getResources().getString(id_text), icon, milliseconds);
	}
	
	public void notifyNotifications(String title, String content, int icon, int flag, Notification notification) 
	{
		Intent statusintent = new Intent();
		statusintent.setClassName("com.ast.free", "com.ast.free.ui.FacebookNotificationManActivity");
		statusintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		statusintent.setAction(Intent.ACTION_VIEW);
		 
		if (icon != 0)
		{
			notification.icon = icon;
		}
		notification.tickerText = title;
		notification.flags |= flag;
		notification.contentIntent = PendingIntent.getActivity(mContext, 0,statusintent, 0);
		RemoteViews views = new RemoteViews(mContext.getPackageName() , R.layout.notification_view); 
		views.setImageViewResource(R.id.listicon1, R.drawable.facebookicon);		
		views.setTextViewText(R.id.text1, title);
		if(content == null || content.length() == 0)
		{
			views.setViewVisibility(R.id.text2, View.GONE);
		}
		else
		{
			views.setViewVisibility(R.id.text2, View.VISIBLE);
		    views.setTextViewText(R.id.text2, content);
		}
		notification.contentView = views;

		mNM.notify(NOTIFICATION_notice, notification);
	}
	
	
	public void notify(String text, int icon, int flag) 
	{
		Intent statusintent = new Intent();
		statusintent.setClassName("com.ast.free.service.ui", "com.ast.free.service.ui.SocialViewActivity");
		statusintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		statusintent.setAction("StatusNotification/" + text);
		
		Notification notification = new Notification();
		if (icon != 0)
		{
			notification.icon = icon;
		}
		notification.tickerText = text;
		notification.flags |= flag;
		notification.contentIntent = PendingIntent.getActivity(mContext, 0,statusintent, 0);
		RemoteViews views = new RemoteViews(mContext.getPackageName() , R.layout.notification_view); 
		if(FacebookBaseActivity.class.isInstance(mContext))
		{
		    views.setImageViewResource(R.id.listicon1, R.drawable.facebookicon);
		}
		else
		{
		    //views.setImageViewResource(R.id.listicon1, R.drawable.twitter);
		}
		views.setTextViewText(R.id.text1, text);
		views.setViewVisibility(R.id.text2, View.GONE);
		notification.contentView = views;

		mNM.notify(NOTIFICATION_UPDATE_STATUS, notification);
	}
	
	public void cancel() {
		if (mNM != null)
			mNM.cancel(NOTIFICATION_UPDATE_STATUS);
	}
	
	public void cancelNotification()
	{
		if (mNM != null)
			mNM.cancel(NOTIFICATION_notice);
	}
	
	private int MSG_CLEAR = 0;
	Handler mHandlerClear = null;
}
