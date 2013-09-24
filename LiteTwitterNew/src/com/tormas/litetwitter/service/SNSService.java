package com.tormas.litetwitter.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import android.os.Parcelable;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;

import com.tormas.litetwitter.R;
import com.tormas.litetwitter.api.SocialServiceInterface;
import com.tormas.litetwitter.providers.SocialORM;
import com.tormas.litetwitter.providers.SocialORM.Account;
import com.tormas.litetwitter.ui.AccountListener;
import twitter4j.AsyncTwitter;
import twitter4j.SimplyStatus;
import twitter4j.Twitter;
import twitter4j.Last10Trends;
import twitter4j.Last10Trends.TrendsItem;
import twitter4j.TwitterException;
import oms.sns.TwitterTrends;

public class SNSService extends Service 
{
	private final String TAG="SNSService";
       
	SocialORM orm;
	
	
	public static boolean DEBUG = false;
	public static boolean SHOWTEST = false;
	//for test loop
	public static boolean TEST_LOOP = false;
	public final static boolean XAUTH = false;	
	
	static SNSService mService;
	public static SNSService getSNSService()
	{	
		return mService;
	}
	
	public static boolean isUsingSecurity()
	{
		boolean ret = false;
		if(mService != null)
		{
			ret = mService.orm.getFacebookUseHttps();
		}
		return ret;
	}
	
	@Override public void onCreate() 
    {
        super.onCreate();           
        Log.d(TAG, "start SNSService");     
        orm  = SocialORM.instance(this);    
        
        mService = this;
    }
	
	@Override
    public void onDestroy() 
	{   
        super.onDestroy();        
      
	}
	
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}	
}
