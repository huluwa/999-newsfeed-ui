package com.ast.free.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.Runnable;
import com.ast.free.providers.SocialORM;
import com.ast.free.service.internal.ContactHelper;
import com.ast.free.service.internal.ContactInternal;
import com.ast.free.ui.AccountListener;
import com.ast.free.ui.SyncSwitchListener;
import com.ast.free.ui.TwitterHelper;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.PhoneBook;
import oms.sns.service.facebook.model.Photo;
import oms.sns.service.facebook.model.PhotoAlbum;
import oms.sns.service.facebook.model.StreamFilter;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ContactService implements ServiceInterface, AccountListener, SyncSwitchListener
{
	final String TAG="sns-ContactService";
    SNSService             mContext;
	
    FacebookLoginHelper   loginHelper;
	private AsyncFacebook facebookA;
	FacebookSession       perm;
	SocialORM             orm;
	Handler handler; 
	final long unit_period = 24*60*60L;
	
	long Phonebook_timeout=24*60*60;//please set the do time at 23:59:59pm at middle night
	long friends_timeout  =24*60*60;//please set the do time at 23:59:59pm at middle night
	int  limit           = 300;
	int  phonebooklimit  = 300;
	int  offset      = 0;
	int  phoneoffset = 0;
	final String CALLBACK = "callback";
	
	FacebookUserHandler userHander = new FacebookUserHandler();
	
	List<PhoneBook> frds = new ArrayList<PhoneBook>() ;
	

    public void resetOffset()
    {
        phoneoffset = 0;
    }
    
	
	public ContactService(SNSService con, SocialORM orm, FacebookLoginHelper loginHelper)
	{
		Log.d(TAG, "create ContactService");
		handler = new CoontactHandler();
		mContext = con;
		this.loginHelper = loginHelper;
		this.orm         = orm;
		
		this.Phonebook_timeout = getPhonebookTimeout();;
		this.friends_timeout   = getFriendsTimeout();
		
		registerAccountListener();
		registerSyncSwitchListener();
			
	}
	
	public long getPhonebookTimeout(){
		return orm.getFacebookContactUpdatePeriod() * unit_period;
	}
	
	private long getFriendsTimeout(){
		return orm.getFacebookFriendUpdatePeriod()  * unit_period;
	}
	
	//contact should be get at the first time
	public void Start() 
	{	
		Log.d(TAG, "start contact service");
		
		//this is not right, if reboot again, it will do it again, 
		//we need record the last time, then calculate the next time to to.
		//
		perm = loginHelper.getPermanentSesstion();
		//don't show the dialog for emulator
		if(/*SNSService.TEST_LOOP ||*/ perm != null)
		{
			rescheduleFacebookUser(false);
		}
	}
	
    public void rescheduleFacebookUser(boolean force)
	{
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		long nexttime ;
		
		long current_time = System.currentTimeMillis();		
		long last_update_time = orm.getLastUpdateFriendTime();
		long donespan  = (current_time-last_update_time);
		long left_time = getFriendsTimeout()*1000L - donespan;
		if(donespan <0 || left_time <=0)
		{		
			long waittime=1;
			for(int i=0;i<nErrorCount && i<10;i++)
			{
				waittime = waittime*2;
			}
            nexttime = System.currentTimeMillis()+ 10*1000*waittime;			
		}
		else
		{
			nexttime = System.currentTimeMillis()+ left_time;
		}
		
		if(force == true)
		{
			nexttime = System.currentTimeMillis()+ 10*1000;
		}
		
		if(SNSService.TEST_LOOP)
		{
			nexttime = System.currentTimeMillis()+ 90*1000;
		}
		
		
		Intent i = new Intent();
		i.setClassName("com.ast.free", "com.ast.free.service.SNSService");
		i.setAction("com.ast.free.intent.action.FACEBOOK_USER");
		PendingIntent userpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, nexttime, userpi);
	}
	
	
	public void alarmFacebookUserComming(Message callbackMessage)
    {
	    Log.d(TAG, "alarmFacebookUserComming");
        
        Message msg = handler.obtainMessage(FACEBOOK_FRIENDS_GET);
        if(callbackMessage != null)
        {
            msg.getData().putParcelable(CALLBACK, callbackMessage);
            msg.getData().putLong("hisuid", callbackMessage.getData().getLong("hisuid", -1));
        }
        handler.sendMessageDelayed(msg, 1*1000);
        
        long nexttime = System.currentTimeMillis()+ getFriendsTimeout()*1000L;
        
        if(SNSService.TEST_LOOP)
        {
            nexttime = System.currentTimeMillis()+ 90*1000;
        }
        
    
        AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        
        Intent i = new Intent();
        i.setClassName("com.ast.free", "com.ast.free.service.SNSService");
        i.setAction("com.ast.free.intent.action.FACEBOOK_USER");
        PendingIntent userpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, nexttime, userpi);
    }
	public void alarmFacebookUserComming()
	{
		Log.d(TAG, "alarmFacebookUserComming");
		
		Message msg = handler.obtainMessage(FACEBOOK_FRIENDS_GET);		
		handler.sendMessageDelayed(msg, 10*1000);
		
		long nexttime = System.currentTimeMillis()+ getFriendsTimeout()*1000L;
		
		if(SNSService.TEST_LOOP)
		{
			nexttime = System.currentTimeMillis()+ 90*1000;
		}
		
	
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		
		Intent i = new Intent();
		i.setClassName("com.ast.free", "com.ast.free.service.SNSService");
		i.setAction("com.ast.free.intent.action.FACEBOOK_USER");
		PendingIntent userpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, nexttime, userpi);
	}
	
	//after logined in
	public void afterLogin()
	{}
	
	public void Stop() 
	{
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
		
		Intent i = new Intent();
		i.setClassName("com.ast.free", "com.ast.free.service.SNSService");
		i.setAction("com.ast.free.intent.action.FACEBOOK_USER");
		PendingIntent userpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		

		alarmMgr.cancel(userpi);
	}

	
	public class FacebookUserHandler implements ObjectHandler
    {
	    long []uids = new long[1];
		public void process(Object obj) {
			if(FacebookUser.class.isInstance(obj))
			{
			    if(perm != null)
			    {
			    	try{
	    				//save data into database
	    				FacebookUser user = (FacebookUser)obj;
	    				orm.addFacebookUser(user);
	    				
	    				//save relation ship
	    				uids[0] = user.uid;				
	    				orm.addFriends(perm.getLogerInUserID(), uids);
	    				user.despose();
	    				user = null;    				
                        
			    	}catch(Exception ne){}
			    }
			}			
		}    	
    }
	  
	private void getFacebookFriends(Message callback)
	{
		SocialORM.Account account = orm.getFacebookAccount();
        if(mContext.checkFacebookAccount(mContext, account))
        {			
        	//perm = null;
            if(perm == null)
            {
			    perm = loginHelper.getPermanentSesstion();
            }
			if(perm != null)
			{
				if(facebookA == null)
				{
					facebookA = new AsyncFacebook(perm);
				}				
	        	facebookA.setSession(perm);	
	        	final Message callMsg = callback;
	        	long hisuid = -1;
	        	if(callMsg !=null)
	        	{
	        	   hisuid = callMsg.getData().getLong("hisuid", -1);
	        	}
	        	if(hisuid == -1)
	        	{
	        		hisuid = perm.getLogerInUserID();
	        	}
	        	else
	        	{
	        		Log.d(TAG, "begin to get who's friends list="+hisuid);
	        	}
	        	
	        	facebookA.getMyFriendsAsync(hisuid,  limit, offset, userHander, new FacebookAdapter()
		    	{
		    		@Override public void getMyFriends(int size)
		            {
		    			Log.d(TAG, "friend size="+size);
		    			orm.checkNoneFriends();
		    			Message msd = handler.obtainMessage(FACEBOOK_FRIENDS_GET_END);
		    			msd.getData().putBoolean("RESULT", true);
		    			msd.getData().putInt("size",       size);
		    			msd.getData().putParcelable(CALLBACK, callMsg);
		    			handler.sendMessage(msd);
		    			
		            }
		    		
		            @Override public void onException(FacebookException e, int method) 
		            {
		            	Log.d(TAG, "fail to get basic friends information");
		            	Message msd = handler.obtainMessage(FACEBOOK_FRIENDS_GET_END);
		    			msd.getData().putBoolean("RESULT", false);
		    			msd.getData().putParcelable(CALLBACK, callMsg);
		    			handler.sendMessage(msd);
		            }
		    	});	        	
	        }
			else
			{
				mContext.needLogin();
			}
        }
	}
	
	public void loadAlbumPhotoInfo() 
    {    
       if(facebookA != null)
       {
    	   facebookA.batch_run_getFacebookAlbumAndPhotoAsync(perm.getLogerInUserID(),new FacebookAdapter()       
	       {
	           @Override public void getFacebookAlbumAndPhoto( HashMap<Integer, Object> albumAndPhotos ) {
	               Log.d("ALBUM PHOTO"," get Album and Photo information successfylly ");
	               if(albumAndPhotos.size() > 2)
	               {
	                   List<PhotoAlbum> albums    = (List<PhotoAlbum>)albumAndPhotos.get(0);
	                   List<Photo> photos         = (List<Photo>)albumAndPhotos.get(1);
	                   List<StreamFilter> filters = (List<StreamFilter>)albumAndPhotos.get(2);
	                   
	                   orm.addAlbum(albums);
	                   orm.addPhoto(photos,true);
	                   orm.addStreamFilter(filters);	           
	               }
	            }	         
	           
				@Override public void onException(FacebookException e, int method) 
		        {
		            Log.d(TAG, "fail to get album and photo information exception "+e.getMessage());                   
		        }
	       });
       }
    }
	        
	public long[] constrcutUIDS(FacebookUser[] uers) 
	{
        long[] uids = new long[uers.length];
        for(int i = 0 ; i < uers.length ; i ++)
        {
            uids[i] = uers[i].uid;
        }
        return uids;
    }

	final static int FACEBOOK_FRIENDS_GET      = 3;
	final static int FACEBOOK_FRIENDS_GET_END  = 4;
	static int nErrorCount=0;
    private class CoontactHandler extends Handler 
    {
        public CoontactHandler()
        {
            super();            
            Log.d(TAG, "new CoontactHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
            	case FACEBOOK_FRIENDS_GET:
            	{
            	    Message msd = msg.getData().getParcelable(CALLBACK);            	    
            	    getFacebookFriends(msd);
            	    
            	    this.postDelayed(new Runnable()
            	    {

						public void run() 
						{
						    //try to get album again
		            	    loadAlbumPhotoInfo();
						}
            	    	
            	    }, 1*60*1000);
            	    
            	    break;
            	}
            	case FACEBOOK_FRIENDS_GET_END:
            	{	
            		boolean suc = msg.getData().getBoolean("RESULT");
            		if(suc)
            		{            	
            			nErrorCount = 0;
            			int size = msg.getData().getInt("size");
            			if(size < limit)//finish get the data
            			{
            				//set record time
            				offset = 0;
                			orm.setLastUpdateFriendTime(System.currentTimeMillis());
                			Message callback = msg.getData().getParcelable(CALLBACK);
                			if(callback != null)
                			{
                			    Message copy = Message.obtain(callback);
                			    Log.d(TAG, "Suc Get friend, call back to sender");
                			    copy.getData().putBoolean("RESULT", true);
                			    copy.getData().putBoolean("finished", true);
                			    copy.sendToTarget();                			    
                			}
            			}
            			else
            			{
            				//re-get the left data
            				offset+= size;
            				Log.d(TAG, "I still have left data, continue to get the data current offset="+offset);            				
            				Message msd = handler.obtainMessage(FACEBOOK_FRIENDS_GET);
            				Message callback = msg.getData().getParcelable(CALLBACK);
            				if(callback != null)
            				{
            				    msd.getData().putParcelable(CALLBACK, Message.obtain(callback));
            				}
            				handler.sendMessageDelayed(msd, 3*1000);
            				
            				//give the UI a chance to update
            				if(callback != null)
                            {
            				    Message copy = Message.obtain(callback);
                                Log.d(TAG, "Suc Get friend, call back to sender");
                                copy.getData().putBoolean("RESULT",   true);
                                copy.getData().putBoolean("finished", false);
                                copy.sendToTarget();                                
                            }
            			}
            			
            		}
            		else
            		{
            		    Message callback = msg.getData().getParcelable(CALLBACK);
                        if(callback != null)
                        {
                            Log.d(TAG, "fail Get friend, call back to sender");
                            callback.getData().putBoolean("RESULT", false);
                            callback.sendToTarget();                                
                        }
            		    else
            		    {
                			nErrorCount++;            			
                			Log.d(TAG, "Fail to get friend reschedule, current offset="+offset);
                			rescheduleFacebookUser(false);
            		    }
            		}
            		break;
            	}                
            }
        }
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
        	
        	nErrorCount = 0;
        	//after login        	
    		rescheduleFacebookUser(true);
		}
	}

	public void onLogout() 
	{
		perm = null;
		facebookA = null;
		
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
		Intent i = new Intent();
		i.setClassName("com.ast.free", "com.ast.free.service.SNSService");
		i.setAction("com.ast.free.intent.action.FACEBOOK_USER");
		PendingIntent userpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		
		alarmMgr.cancel(userpi);
	}

	public void registerAccountListener() 
	{
		AccountManager.registerAccountListener("ContactService", this);
	}

	public void unregisterAccountListener() 
	{
		AccountManager.unregisterAccountListener("ContactService");
	}

	public void registerSyncSwitchListener() {
		SyncSwithManager.registerSyncSwithListener("ContactService", this);
	}

	public void setEnable(boolean enable) 
	{
		if(enable == true)
		{
			Log.d(TAG, "user turn on the switch for sync phone with Facebook phonebook="+this);
			Start();		    
		}
	}

	public void unregisterSyncSwitchListener() {
		SyncSwithManager.unregisterSyncSwithListener("ContactService");		
	}
}
