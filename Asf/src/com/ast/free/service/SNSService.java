package com.ast.free.service;

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

import com.ast.free.R;
import com.ast.free.api.SocialServiceInterface;
import com.ast.free.providers.SocialORM;
import com.ast.free.providers.SocialORM.Account;
import com.ast.free.service.FacebookLoginHelper;
import com.ast.free.service.internal.ContactHelper;
import com.ast.free.service.internal.OmsService;
import com.ast.free.service.internal.OmsTask;
import com.ast.free.service.internal.OmsTask.ContactSyncTask;
import com.ast.free.service.internal.OmsTask.EventAddTask;
import com.ast.free.service.internal.OmsTask.EventSyncTask;
import com.ast.free.service.internal.OmsTask.LookupTask;
import com.ast.free.ui.AccountListener;
import com.ast.free.ui.FacebookBaseActivity;
import com.ast.free.ui.FacebookMessageActivity;
import com.ast.free.ui.FacebookLoginActivity;
import com.ast.free.ui.FacebookSettingPreference;
import com.ast.free.ui.FacebookStatusUpdateActivity;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.FBNotifications;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.MailboxThread;
import oms.sns.service.facebook.model.PhoneBook;
import oms.sns.service.facebook.model.UserInfo;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.util.ArrayUtils;
import android.os.SystemProperties;

public class SNSService extends Service implements SocialServiceInterface, AccountListener
{
	private final String TAG="SNSService";
       
	SocialORM orm;
	FacebookLoginHelper loginHelper;	
	
	public static boolean DEBUG = false;
	public static boolean SHOWTEST = false;
	//for test loop
	public static boolean TEST_LOOP = false;
	public final static boolean XAUTH = false;
	static 
	{
	    //for emulator
		if(android.os.SystemProperties.get("ro.kernel.qemu").equals("1"))
		{
			TEST_LOOP = true;
		}
		else
		{
			TEST_LOOP = false;
		}
	
	}
	
	final int  SYNC_FACEBOOK_CONTACTS = 1000;	
	final long SYNC_INTERVAL_TIME     = 60*1000L;
		
	boolean         getsesion = false;
	ServiceHandler  handler;	
	
	MailService          mailService;
	CoherenceCheck       coherenceCheck;
	ContactService       contactService;
	OmsService           omsService;
	ImageBackService     imageBackService;
	FacebookTaskEvent    fbEvent;
	NotificationService    notificationService;
	
	static SNSService mService;
	public static SNSService getSNSService()
	{	
		return mService;
	}
	
	public ContactService getContactService()
	{
	    return contactService;
	}
	
	public void setEnableNotification(boolean enable)
	{
		if(notificationService != null)
		{
			if(enable)
			    notificationService.Start();
			else
				notificationService.Stop();
		}
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
        //to make sure, we set the https, when crash
        try{
            orm.setFacebookUseHttps(orm.getFacebookUseHttps());
        }catch(Exception ne){}
        
        try{
        	SystemProperties.set("dalvik.vm.heapsize", "52m");	
        }catch(Exception ne)
        {
        	Log.d(TAG, "fail to set dalvik.vm.heapsize");
        }
        
        mService = this;
        loginHelper = FacebookLoginHelper.instance(this);
               
        
        IntentFilter filter = new IntentFilter("com.ast.free.getsession");
        registerReceiver(mHangReceiver, filter);
        
        handler = new ServiceHandler();        
        //check the session
        getsesion = loginHelper.restoreSesstion();
        	
        mailService = new MailService(this, orm, loginHelper);
        mailService.Start();
        
        coherenceCheck = new CoherenceCheck(this, orm, loginHelper);
        coherenceCheck.Start();
        
        contactService = new ContactService(this, orm, loginHelper);
        contactService.Start();       
        
        omsService = OmsService.instance(this, orm, loginHelper);
        omsService.Start();
        
        notificationService = new NotificationService(this, orm, loginHelper);
        notificationService.Start();
        
        fbEvent = new FacebookTaskEvent(this, omsService);
        //start the background thread
        Message msd = handler.obtainMessage(START_OMS_SERVICE);
        handler.sendMessageDelayed(msd, 10*1000);
        
        imageBackService = new ImageBackService(this, orm);
        
        registerAccountListener();
    }
	
	@Override
    public void onDestroy() 
	{   
        super.onDestroy();
        
        mailService.Stop();
        coherenceCheck.Stop();
        contactService.Stop();
        imageBackService.Stop();
        omsService.Stop();
        
        mService = null;
        unregisterAccountListener();
	}
	
	private class UpdateContactAsync extends android.os.AsyncTask<Void, Void, Void>
    {       
		public long    fuid;
		public long    peopleid ;
		public boolean updatelogo;
		public boolean updatebirthday;
		public boolean updateemail;
		public boolean updatecell;
        public UpdateContactAsync()
        {
            super();            
            Log.d(TAG, "create UpdateContactAsync");
        }

		@Override
		protected Void doInBackground(Void... params)			  
        {           
            if(fuid!=-1 && peopleid!=-1)
            {
            	
                fbEvent.doUpdateContact(fuid,peopleid,updatelogo,updatebirthday,updateemail,updatecell);
            }       
            return null;
        }
    }
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		
		if(intent == null)
		    return;
		
		if(intent.getAction() != null)
		{
			if(intent.getAction().equals("com.ast.free.intent.action.FACEBOOK_USER"))
			{
				Log.d(TAG, "&&&&&&&&&&&  Facebook User time it out="+ new Date().toLocaleString());
				Log.d(TAG, "next to get Facebook User");
				Message msg = intent.getParcelableExtra("callback");				
				this.contactService.alarmFacebookUserComming(msg);
			}			
			else if(intent.getAction().equals("com.ast.free.intent.action.MAIL_CHECK"))
			{
				Log.d(TAG, "&&&&&&&&&&& Facebook Mail time it out="+ new Date().toLocaleString());
				Log.d(TAG, "next to get mail");
				this.mailService.alarmMailCheckComming();
			}			
			else if(intent.getAction().equals("com.ast.free.intent.action.reset.MAIL_CHECK"))
			{
                Log.d(TAG, "next to get Mail check reset");
                this.mailService.Stop();
                this.mailService.rescheduleMail(false);
			}
			else if(intent.getAction().equals(NotificationService.ACTION))
			{
			    Log.d(TAG, "next to get notification check reset");                
                this.notificationService.alarmNotificationComming();
			}
		}
				
		boolean isAddAsFriend = intent.getBooleanExtra("addasfriend", false);
		if(isAddAsFriend)
		{
		    Log.d(TAG, "add as friend task");
		    long fuid = intent.getLongExtra("fuid", -1);
		    if(fuid!=-1)
		    {
		        fbEvent.doAddAsFriend(fuid);
		    }	    
		}
        
    	boolean walltowall = intent.getBooleanExtra("walltowall", false);
		if(walltowall)
		{			
			Intent wallintent = new Intent(SNSService.this,FacebookStatusUpdateActivity.class);
			wallintent.putExtra("fuid", new Long(625142542));
			wallintent.putExtra("contact_name", "Huadong Liu");
			wallintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(wallintent);
		}		
	}
	
	final static int START_OMS_SERVICE              = 10;
	final static int FACEBOOK_SETTING_MSG           = 101;
	final static int FACEBOOK_LOGIN_MSG             = 102;
	
	final static int UPDATE_CONTACT = 103;
	final static int CONTACT_SYNC   = 104;
	final static int LOOKUP_ALL     = 105;
	final static int LOOKUP_SINGLE  = 106;

	private class ServiceHandler extends Handler 
    {
        public ServiceHandler()
        {
            super();            
            Log.d(TAG, "new ServiceHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case UPDATE_CONTACT:
	            {
	            	if(msg.getData().getBoolean("RESULT", false))
	            	{
		            	Log.d(TAG, "UPDATE_CONTACT");
		            	long fuid              = msg.getData().getLong("fuid", -1);
		 	            long peopleid          = msg.getData().getLong("peopleid", -1);
		 	            boolean updatelogo     = msg.getData().getBoolean("updatelogo", false);
		 	            boolean updatebirthday =  msg.getData().getBoolean("updatebirthday", false);
		 	            boolean updateemail    =  msg.getData().getBoolean("updateemail", false);
		 	            boolean updatecell     =  msg.getData().getBoolean("updatecell", false);
		 	            
		            	UpdateContactAsync ycas = new UpdateContactAsync();
		 	            ycas.fuid           = fuid;
		 	            ycas.peopleid       = peopleid;
		 	            ycas.updatebirthday = updatebirthday;
		 	            ycas.updatecell     = updatecell;
		 	            ycas.updateemail    = updateemail;
		 	            ycas.updatelogo     = updatelogo;
		                ycas.execute((Void[])null);
	            	}
	            	else
	            	{
	            		Log.d(TAG, "you select not to sync UPDATE_CONTACT");
	            	}
	            	break;
	            }
	            case CONTACT_SYNC:
	            {
	            	if(msg.getData().getBoolean("RESULT", false))
	            	{
		            	Log.d(TAG, "CONTACT_SYNC");
		            	int peopleid = msg.getData().getInt("peopleid", -1);
		            	fbEvent.doContactSync(peopleid);
	            	}
	            	else
	            	{
	            		Log.d(TAG, "you select not to sync CONTACT_SYNC");
	            	}
	            	break;
	            }
	            case LOOKUP_ALL:
	            {
	            	if(msg.getData().getBoolean("RESULT", false))
	            	{
		            	Log.d(TAG, "LOOKUP_ALL");
		            	OmsTask.LookupTask task = new OmsTask.LookupTask();
						task.id = OmsTask.LOOKUPALLID;
						omsService.queueTask(task);	
						Toast.makeText(SNSService.this,getString(R.string.sns_lookupall_toast_msg) , Toast.LENGTH_SHORT).show();
	            	}
	            	else
	            	{
	            		Log.d(TAG, "you select not to sync LOOKUP_ALL");
	            	}
	            	break;
	            }
	            case LOOKUP_SINGLE:
	            {
	            	if(msg.getData().getBoolean("RESULT", false))
	            	{
		            	Log.d(TAG, "LOOKUP_SINGLE");
		            	
		            	int peopleid = msg.getData().getInt("peopleid",-1);	    			
		    			int peopleids[] = msg.getData().getIntArray("peopleids");
		    			
		    			if(peopleid >= 0)
		    			    fbEvent.doContactLookup(peopleid);
		    			
		    			if(peopleids != null)	    			
		    				fbEvent.doContactLookup(peopleids);
	            	}
	            	else
	            	{
	            		Log.d(TAG, "you select not to sync LOOKUP_SINGLE");
	            	}
	            	break;
	            }
                case START_OMS_SERVICE:
                {
                	omsService.startThread();
                	break;
                }
            	case FACEBOOK_SETTING_MSG:
                {
                	Log.d(TAG, "launch setting page");
                	if(orm.isSNSEnable())
                	{
	                	Intent intent = new Intent(SNSService.this, FacebookSettingPreference.class);                	
	                	intent.putExtra("comfromservice", true);
	                	intent.putExtra("forsignin", true);
	                	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            		startActivity(intent);
                	}
                	else
                	{
                		Log.d(TAG, "in service, you disable the sns for setting");
                	}
                	break;
                }
                case FACEBOOK_LOGIN_MSG:
                {
                	
                	Log.d(TAG, "launch Login page");
                	if(orm.isSNSEnable())
                	{
	                	Intent intent = new Intent(SNSService.this, FacebookLoginActivity.class);
	                	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            		startActivity(intent);
		            }
	            	else
	            	{
	            		Log.d(TAG, "in service, you disable the sns for login");
	            	}
                	break;
                }
                
            }
        }
    }
    
    boolean checkFacebookAccount(Context con, Account account)
	{
		 return checkFacebookAccount(con, account, false);
		 
	}
    boolean checkFacebookAccount(Context con, Account account, boolean enablePromptLogin)
	{

		boolean logined = loginHelper.restoreSesstion();
        if(logined == false && enablePromptLogin)
        {
        	needLogin();
        }
		return true;
	}    
    
    public void needLogin()
    {
    	if(orm.isSNSEnable())
    	{
    	    handler.obtainMessage(FACEBOOK_LOGIN_MSG).sendToTarget();
    	}
    }
    
	BroadcastReceiver  mHangReceiver = new BroadcastReceiver() 
    {
        public void onReceive(Context context, Intent intent) 
        {	
         	boolean connected = intent.getBooleanExtra("connected", false);
         	if(connected == true)
         	{
         		Log.d(TAG, "after facebook user login");
         		getsesion = true;
         		
         		//re-get the friends information
         		contactService.afterLogin();
         		mailService.afterLogin();
         		omsService.afterLogin();
         	}
        }
    };
	long steps=0;
	public boolean PromptLoginUI()
	{
		boolean ret = isLogined();
		boolean retvalue = false;
		if(ret == false && orm.isSNSEnable())
		{			
			needLogin();
			//you have enable the facebook, so even if not login, 
			//we still queue the task, to let the tasks be processed in the future
			retvalue = true;
		}		
		
		if(ret==false && orm.isSNSEnable() == false)
		{
			steps++;
			if(steps%10 ==0)
			{
			   Log.d(TAG, "you disable the sns service="+steps);			   
			}
		
			handler.post(new Runnable()
			{
			   public void run()
			    {
			        Toast.makeText(SNSService.this, R.string.facebook_not_login_prompt, Toast.LENGTH_SHORT).show();
			    }
			});
      			
		}
		return ret;
	}
	
	
	
	private boolean isLogined() 
	{		
		return getsesion;
	}
    	
	private boolean isEmpty(String str)
	{
		return str==null || str.length() ==0;
	}
	
		
	boolean hasDataConnection()
	{
		return true;
	}
	
	static String latestNotification="";
	Object mNotifyLock = new Object();
	boolean isInCallNotify = false;
	
	
	
	Object mTrendsLock = new Object();
	boolean isInCallTrends = false;	
	

	@Override
	public IBinder onBind(Intent intent) 
	{		
		return null;
	}

	public void onLogin() 
	{
		Log.d(TAG, "login="+this);
	}

	public void onLogout() 
	{
		Log.d(TAG, "logout="+this);		
	}

	public void registerAccountListener() 
	{
		AccountManager.registerAccountListener("SNSService", this);		
	}

	public void unregisterAccountListener() 
	{		
		AccountManager.unregisterAccountListener("SNSService");
	}	
}
