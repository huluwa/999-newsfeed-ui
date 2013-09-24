package oms.sns.facebook.service;

import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import oms.sns.facebook.providers.SocialORM;
import oms.sns.facebook.ui.AccountListener;
import oms.sns.facebook.ui.SyncAddressBookHelper;
import oms.sns.facebook.ui.SyncSwitchListener;
import oms.sns.facebook.ui.SyncSwitchListener.SyncSwithManager;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookSession;

public class AddressBookSyncService implements ServiceInterface, AccountListener, SyncSwitchListener { 
	final String TAG="sns-AddressBookSyncService";
	final static String ACTION="oms.sns.facebook.intent.action.FACEBOOK_LOOKUP_SYNC";
	final static String SERVICE="oms.sns.facebook.service.SNSService";
    SNSService             mContext;
	
    FacebookLoginHelper   loginHelper;
	private AsyncFacebook facebookA;
	FacebookSession       perm;
	SocialORM             orm;
	Handler handler; 
    long sync_period = 24*60*60*1000L;
	
	public AddressBookSyncService(SNSService con, SocialORM orm, FacebookLoginHelper loginHelper)
	{
		Log.d(TAG, "create AddressBookSyncService");
		handler = new PhonebookSyncHandler();
		mContext = con;
		this.loginHelper = loginHelper;
		this.orm         = orm;
		registerAccountListener();
		registerSyncSwitchListener();
			
	}

	//contact should be get at the first time
	public void Start() 
	{	
		Log.d(TAG, "start AddressBookSyncService");
		
		//this is not right, if reboot again, it will do it again, 
		//we need record the last time, then calculate the next time to to.
		//
		perm = loginHelper.getPermanentSesstion();
		if(SNSService.TEST_LOOP || perm != null)
		{
			if(facebookA == null)
			{
				facebookA = new AsyncFacebook(perm);
			}				
        	facebookA.setSession(perm);	        	
	        
			reschedulePhonebookSync(false);
		}
	}
	
	public void reschedulePhonebookSync(boolean force)
	{
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		long nexttime;
		
		long current_time = System.currentTimeMillis();
		long last_sync_time = orm.getLastAddressbookSyncTime();
		long donespan  = (current_time-last_sync_time);
		long left_time = orm.getAddressbookLookupPeriod()*sync_period -donespan;
		if(donespan <0 || left_time <=0)
		{
			long waittime=1;
			for(int i=0;i<nErrorCount && i<10;i++)
			{
				waittime = waittime*2;
			}
            nexttime = System.currentTimeMillis()+ 15*60*1000*waittime;	
		}
		else
		{
			nexttime = System.currentTimeMillis()+ left_time;	
		}
		
		if(force == true)
		{
			nexttime = System.currentTimeMillis()+ 15*60*1000;
		}
		
		if(SNSService.TEST_LOOP)
		{
			nexttime = System.currentTimeMillis()+ 5*60*1000;
		}
		
		
		Intent i = new Intent();
		i.setClassName("oms.sns.facebook", SERVICE);
		i.setAction(ACTION);
		PendingIntent phonebookpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);		
		alarmMgr.set(AlarmManager.RTC_WAKEUP, nexttime, phonebookpi);
	}
	
	//time is up, need reget the data
	public void alarmPhonebookSyncComming()
	{
		Log.d(TAG, "alarm addressbook sync Comming");		
		Message msg = handler.obtainMessage(FACEBOOK_LOOKUP_SYNC);
		//SNSService already take over this, remove it
		//SyncAddressBookHelper.checkIsEnableAddressbookSync(mContext, orm, msg);
		msg.sendToTarget();
		
		long nexttime = System.currentTimeMillis()+ orm.getAddressbookLookupPeriod()*sync_period;	
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
	 
	 final int FACEBOOK_LOOKUP_SYNC = 1;
	 static int nErrorCount=0;
	 private class PhonebookSyncHandler extends Handler 
	 {
	        public PhonebookSyncHandler()
	        {
	            super();            
	            Log.d(TAG, "new LOOKUP PhonebookSyncHandler");
	        }
	        
	        @Override
	        public void handleMessage(Message msg) 
	        {
	            switch(msg.what)
	            {
		            case FACEBOOK_LOOKUP_SYNC:
		            {
		            	orm.setLastAddressbookSyncTime(System.currentTimeMillis());
		            	
		            	Intent intent = new Intent(mContext,SNSService.class);
		            	intent.putExtra("lookupall", true);
		            	//no need prompt dialog, this is in background
		            	intent.putExtra("noneedifnorturnon", true);
		            	mContext.startService(intent);
		            	break;
		            }
	            }
	        }
	    }

		

	public void Stop() {
		// TODO Auto-generated method stub
		
	}

	public void afterLogin() {
		// TODO Auto-generated method stub
		
	}


	
	public void Pause() 
	{}

	public void logout() 
	{}

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
        	reschedulePhonebookSync(true);
    		//rescheduleFacebookUser(true);
		}
	}

	public void onLogout() 
	{
		perm = null;
		facebookA = null;
		
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
		Intent i = new Intent();
		i.setClassName("oms.sns.facebook", SERVICE);
		i.setAction(ACTION);
		PendingIntent phonebookpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
	
		alarmMgr.cancel(phonebookpi);
	}

	public void registerAccountListener() 
	{
		AccountManager.registerAccountListener("AddressBookSyncService", this);
	}

	public void unregisterAccountListener() 
	{
		AccountManager.unregisterAccountListener("AddressBookSyncService");
	}

	public void registerSyncSwitchListener() {
		SyncSwithManager.registerSyncSwithListener("AddressBookSyncService", this);
	}

	public void setEnable(boolean enable) 
	{
		if(enable == true)
		{
			Log.d(TAG, "user turn on the switch for sync phone with Facebook phonebook="+this);
		    this.Start();
		}
	}

	public void unregisterSyncSwitchListener() 
	{
		SyncSwithManager.unregisterSyncSwithListener("AddressBookSyncService");		
	}
}
