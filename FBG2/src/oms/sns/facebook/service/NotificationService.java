package oms.sns.facebook.service;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import oms.sns.facebook.R;
import oms.sns.facebook.providers.SocialORM;
import oms.sns.facebook.ui.AccountListener;
import oms.sns.facebook.ui.view.SNSItemView;
import oms.sns.facebook.util.StatusNotification;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.Notifications;

public class NotificationService implements ServiceInterface, AccountListener{
	//we will remember last notifications in memory
	
	final String TAG="***-NotificationService";
	final static String ACTION="oms.sns.facebook.intent.action.FACEBOOK_NOTIFICATION_SYNC";
	final static String SERVICE="oms.sns.facebook.service.SNSService";
    SNSService             mContext;
	
    FacebookLoginHelper   loginHelper;
	private AsyncFacebook facebookA;
	FacebookSession       perm;
	SocialORM             orm;
	Handler handler; 
    long sync_period = 1*60*1000L;//1 minutes
    StatusNotification notify; 
    PowerManager pownermMgr;
    public NotificationService(SNSService con, SocialORM orm, FacebookLoginHelper loginHelper)
	{
		Log.d(TAG, "create NotificationService");
		handler = new NotificationHandler();
		mContext = con;
		this.loginHelper = loginHelper;
		this.orm         = orm.instance(con);
		notify           = new StatusNotification(mContext);
		
		pownermMgr = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
		registerAccountListener();
	}
    
	//notification should be get at the first time
	public void Start() 
	{	
		Log.d(TAG, "start NotificationService");
		
		try{
			String ca = orm.getSettingValue("lastNotificationID");
			if(ca != null && ca.length() > 0)
		    lastNotificationID = Long.valueOf(ca);
		}catch(NumberFormatException ne){}
		//this is not right, if reboot again, it will do it again, 
		//we need record the last time, then calculate the next time to to.
		//
		perm = loginHelper.getPermanentSesstion();
		if(/*SNSService.TEST_LOOP || */perm != null)
		{
			if(facebookA == null)
			{
				facebookA = new AsyncFacebook(perm);
			}				
        	facebookA.setSession(perm);	    
        	
			rescheduleNotificationSync(true);
		}
	}
	
	public void rescheduleNotificationSync(boolean force)
	{
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		long nexttime;
		
		long current_time = System.currentTimeMillis();
		long last_sync_time = orm.getNotificationLastTime();
		long donespan  = (current_time-last_sync_time);
		long left_time = Long.valueOf(orm.getNotificationInterval())*sync_period -donespan;
		if(donespan <0 || left_time <=0)
		{
			long waittime=1;
			for(int i=0;i<nErrorCount && i<10;i++)
			{
				waittime = waittime*2;
			}
            nexttime = System.currentTimeMillis()+ 1*60*1000*waittime;	
		}
		else
		{
			nexttime = System.currentTimeMillis()+ left_time;	
		}
		
		if(force == true)
		{
			nexttime = System.currentTimeMillis()+ 1*60*1000;
		}
		
		if(SNSService.TEST_LOOP)
		{
			nexttime = System.currentTimeMillis()+ 1*60*1000;
		}
		
		
		Intent i = new Intent();
		i.setClassName("oms.sns.facebook", SERVICE);
		i.setAction(ACTION);
		PendingIntent phonebookpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);		
		alarmMgr.set(AlarmManager.RTC_WAKEUP, nexttime, phonebookpi);
	}
	
	//time is up, need reget the data
	public void alarmNotificationComming()
	{
		Log.d(TAG, "alarm notification sync Comming");		
		if(enableNotifications())
		{
			Message msg = handler.obtainMessage(FACEBOOK_NOTIFY_SYNC);		
			msg.sendToTarget();
		}
		
		//re-schedule
		long nexttime = System.currentTimeMillis()+ Long.valueOf(orm.getNotificationInterval())*sync_period;	
		if(SNSService.TEST_LOOP)
		{
			nexttime = System.currentTimeMillis()+ 2*60*1000;
		}
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		
		Intent i = new Intent();
		i.setClassName("oms.sns.facebook", SERVICE);
		i.setAction(ACTION);
		PendingIntent phonebookpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);		
		alarmMgr.set(AlarmManager.RTC_WAKEUP, nexttime, phonebookpi);
	}
	
	private boolean enableNotifications()
	{
		if(orm.isNotificationEnable())
		{
			return true;
		}
		
		return false;
	}
	
    final int FACEBOOK_NOTIFY_SYNC     = 1;
    final int FACEBOOK_NOTIFY_SYNC_END = 2;
	static int nErrorCount=0;
	private class NotificationHandler extends Handler 
	{
        public NotificationHandler()
        {
            super();            
            Log.d(TAG, "new NotificationHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case FACEBOOK_NOTIFY_SYNC:
	            {
	            	Log.d(TAG, "begin to get notifications");
	            	//do it directly
	            	getNotification();
	            	break;
	            }
	            case FACEBOOK_NOTIFY_SYNC_END:
	            {
	            	break;
	            }
            }
        }
    }
    
	private void getNotification()
	{
		if(facebookA == null)
		{
			Log.d(TAG, "no facebookA ");
			return ;
		}
        facebookA.getNotificationListAsync(-1L, false, new FacebookAdapter()
        {
            @Override public void getNotificationList(Notifications notifications)
            {
            	orm.setLastNotificationSyncTime(System.currentTimeMillis());
            	nErrorCount = 0;
                Log.d(TAG, "after get notification ="+notifications.notificationlist.size());               
                processNotify(notifications.notificationlist);   
                
                Message msd = handler.obtainMessage(FACEBOOK_NOTIFY_SYNC_END);                
                handler.sendMessage(msd);
            }
            
            @Override public void onException(FacebookException e, int method, Object[] args) 
            {
                 Log.d(TAG, "fail to get get notifcation="+e.getMessage());
               
                 nErrorCount++;
                 Message msd = handler.obtainMessage(FACEBOOK_NOTIFY_SYNC_END);                    
                 handler.sendMessage(msd);
               
            }
        });    
	}
	
	static long lastNotificationID = 0;
	public void processNotify(ArrayList<Notifications.Notification> notifications)
	{
		String text = "";
		int unread = 0;
		long lastID=0;
		for(int i=0;i<notifications.size();i++)
		{
			Notifications.Notification item = notifications.get(i);
			if(item.is_unread == true)
			{
				unread++;
				
				if(text.length() == 0)
				{
					lastID = item.notification_id;					
				    text = item.body_text;
				    if(text == null || text.length() == 0)
				    {
				        text = item.title_text;
				    }
				}				
			}
		}		
		notifications.clear();
		if(lastID != 0 && lastNotificationID != lastID)
		{
			Log.d(TAG, "lastNotificationID="+lastNotificationID + " new latest id="+lastID);
			
			lastNotificationID = lastID; 
			orm.addSetting("lastNotificationID", String.valueOf(lastNotificationID));
			
			if(unread > 0)
			{
			    String notify = String.format(mContext.getString(R.string.notification_format), unread, unread>1?"s":"");
			    text = SNSItemView.removeHTML(text,true);
			    sendNotification(notify,text);
			}
		}
		else
		{
			Log.d(TAG, "no new unread");
		}
	}
	
	//send notification to Notification bar, open notification bar, will stay there, 
	//but if any press, will clear all facebook notifications
	private void sendNotification(String title, String content)
	{
	     WakeLock wl = pownermMgr.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "SCREEN_BRIGHT_WAKE_LOCK");
		 wl.acquire(10*1000);
		
		 if(orm.getNotificationVibrate())
		 {
			 final Vibrator vib  = (android.os.Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
	         vib.vibrate(2*1000);
		 }
         
         Notification notification = new Notification();
         notification.flags |= Notification.FLAG_SHOW_LIGHTS ;
         notification.flags |= Notification.DEFAULT_SOUND ;
         notification.ledARGB = 0xff00ff00;
         notification.ledOnMS = 500;
         notification.ledOffMS = 2000;         
         
         if(orm.getNotificationVibrate())
		 {
             notification.defaults |= Notification.DEFAULT_VIBRATE;
		 }
         
         notification.defaults |= Notification.DEFAULT_SOUND;
         notify.notifyNotifications(title, content, R.drawable.facebook_logo,    Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE, notification);
	}
	
	public void Pause() {cancelAlarm();}	
	public void Stop() 
	{
		Log.d(TAG, "stop notification ");
		cancelAlarm();
	}
	
	public void afterLogin() {}
	public void logout() {}

	public void onLogin() 
	{
		perm = null;
		perm = loginHelper.getPermanentSesstion();
		if(perm != null)
		{
			if(facebookA == null)
			{
				facebookA = new AsyncFacebook(perm);
			}				
        	facebookA.setSession(perm);
        	
        	//nErrorCount = 0;
        	//after login
        	rescheduleNotificationSync(true);    		
		}
	}

	//if user want to stop the notification
	private void cancelAlarm()
	{
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
		Intent i = new Intent();
		i.setClassName("oms.sns.facebook", SERVICE);
		i.setAction(ACTION);
		PendingIntent phonebookpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
	
		alarmMgr.cancel(phonebookpi);
	}
	
	public void onLogout() 
	{
		perm = null;
		facebookA = null;
		
		cancelAlarm();
	}

	public void registerAccountListener() 
	{
		AccountManager.registerAccountListener("NotificationService", this);
	}

	public void unregisterAccountListener() 
	{
		AccountManager.unregisterAccountListener("NotificationService");
	}

}
