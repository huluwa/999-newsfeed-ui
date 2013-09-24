package com.msocial.facebook.service;

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

import com.msocial.facebook.R;
import com.msocial.facebook.api.SocialServiceInterface;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.providers.SocialORM.Account;
import com.msocial.facebook.service.FacebookLoginHelper;
import com.msocial.facebook.service.FacebookSyncHelper;
import com.msocial.facebook.service.dell.ContactHelper;
import com.msocial.facebook.service.dell.OmsService;
import com.msocial.facebook.service.dell.OmsTask;
import com.msocial.facebook.service.dell.OmsTask.ContactSyncTask;
import com.msocial.facebook.service.dell.OmsTask.EventAddTask;
import com.msocial.facebook.service.dell.OmsTask.EventSyncTask;
import com.msocial.facebook.service.dell.OmsTask.LookupTask;
import com.msocial.facebook.ui.AccountListener;
import com.msocial.facebook.ui.FacebookBaseActivity;
import com.msocial.facebook.ui.FacebookMessageActivity;
import com.msocial.facebook.ui.FacebookLoginActivity;
import com.msocial.facebook.ui.FacebookSettingPreference;
import com.msocial.facebook.ui.FacebookStatusUpdateActivity;
import com.msocial.facebook.ui.SyncAddressBookHelper;
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

public class SNSService extends Service implements SocialServiceInterface, AccountListener
{
	private final String TAG="SNSService";
       
	SocialORM orm;
	FacebookLoginHelper loginHelper;	
	FacebookSyncHelper syncHelper;
	
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
	AddressBookSyncService phonebookService;
	NotificationService    notificationService;
	
	public NotificationService getNotificationService()
	{
	    return notificationService;
	}
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
        
        mService = this;
        loginHelper = FacebookLoginHelper.instance(this);
        syncHelper = FacebookSyncHelper.instance(this);
               
        
        IntentFilter filter = new IntentFilter("com.msocial.facebook.getsession");
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
        
        phonebookService = new AddressBookSyncService(this, orm, loginHelper);
        phonebookService.Start();
        
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
			if(intent.getAction().equals(AddressBookSyncService.ACTION))
			{
				Log.d(TAG,"&&&&&&&&&&&& Facebook address book sync = "+new Date().toLocaleString());
				this.phonebookService.alarmPhonebookSyncComming();
			}
			if(intent.getAction().equals("com.msocial.facebook.intent.action.FACEBOOK_USER"))
			{
				Log.d(TAG, "&&&&&&&&&&&  Facebook User time it out="+ new Date().toLocaleString());
				Log.d(TAG, "next to get Facebook User");
				Message msg = intent.getParcelableExtra("callback");				
				this.contactService.alarmFacebookUserComming(msg);
			}
			else if(intent.getAction().equals("com.msocial.facebook.intent.action.FACEBOOK_PHONEBOOK"))
			{
				Log.d(TAG, "&&&&&&&&&&& Facebook PhoneBook time it out="+ new Date().toLocaleString());
				Log.d(TAG, "next to get Facebook phonebook");
				Message msg = intent.getParcelableExtra("callback");    
				this.contactService.alarmPhonebookComming(msg);
			}
			else if(intent.getAction().equals("com.msocial.facebook.intent.action.MAIL_CHECK"))
			{
				Log.d(TAG, "&&&&&&&&&&& Facebook Mail time it out="+ new Date().toLocaleString());
				Log.d(TAG, "next to get mail");
				this.mailService.alarmMailCheckComming();
			}
			else if(intent.getAction().equals("com.msocial.facebook.intent.action.reset.FACEBOOK_PHONEBOOK"))
			{
			    Log.d(TAG, "&&&&&&&&&&& Facebook PhoneBook reset");
                Log.d(TAG, "next to get Facebook User reset");
                this.contactService.Stop();                
                this.contactService.reschedulePhonebook(false);
			}
			else if(intent.getAction().equals("com.msocial.facebook.intent.action.reset.MAIL_CHECK"))
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
        
        boolean isUpdateContact = intent.getBooleanExtra("updatecontact", false);
        if(isUpdateContact)
        {
        	boolean ret = PromptLoginUI();
			if(ret)
			{
	            Log.d(TAG, "add as friend task");
	            long fuid = intent.getLongExtra("fuid", -1);
	            long peopleid = intent.getLongExtra("peopleid", -1);
	            boolean updatelogo = intent.getBooleanExtra("updatelogo", false);
	            boolean updatebirthday = intent.getBooleanExtra("updatebirthday", false);
	            boolean updateemail = intent.getBooleanExtra("updateemail", false);
	            boolean updatecell  = intent.getBooleanExtra("updatecell", false);
	            
	        	Message msg = handler.obtainMessage(UPDATE_CONTACT);
	        	msg.getData().putLong("fuid",     fuid);
	        	msg.getData().putLong("peopleid", peopleid);	        	
	        	msg.getData().putBoolean("updatelogo",     updatelogo);
	        	msg.getData().putBoolean("updatebirthday", updatebirthday);
	        	msg.getData().putBoolean("updateemail",    updateemail);
	        	msg.getData().putBoolean("updatecell", updatecell);	        	
	    		SyncAddressBookHelper.checkIsEnableAddressbookSync(this.getApplicationContext(), orm, msg);
			}
        }
		
		boolean isContactSync = intent.getBooleanExtra("contactsync", false);		
		if(isContactSync)
		{
			boolean ret = PromptLoginUI();
			if(ret)
			{
				Log.d(TAG, "new contact sync task");
				int peopleid = intent.getIntExtra("peopleid",-1);
				
				Message msg = handler.obtainMessage(CONTACT_SYNC);
	        	msg.getData().putInt("peopleid",    peopleid);				
				SyncAddressBookHelper.checkIsEnableAddressbookSync(this.getApplicationContext(), orm, msg);
			}
		}
		
		boolean isContactLookupAll = intent.getBooleanExtra("lookupall", false);
		if(isContactLookupAll)
		{
			boolean ret = PromptLoginUI();
			if(ret)
			{
				Log.d(TAG, "new contact lookup all task");			
				Message msg = handler.obtainMessage(LOOKUP_ALL);	
				//one case, from AddressBookSyncService, service, this is also a background task
				//we don't need dialog, is user disable synchronizing address book
				msg.getData().putBoolean("noneedifnorturnon", intent.getBooleanExtra("noneedifnorturnon", false));
				SyncAddressBookHelper.checkIsEnableAddressbookSync(this.getApplicationContext(), orm, msg);
			}
		}
		
		
		boolean isContactLookup = intent.getBooleanExtra("lookup", false);
		if(isContactLookup)
		{
			Log.d(TAG, "new contact lookup task");			
			
			int peopleid = intent.getIntExtra("peopleid",-1);
			int[] peopleids = intent.getIntArrayExtra("peopleids");
			
			Message msg = handler.obtainMessage(LOOKUP_SINGLE);        	
        	if(peopleid >= 0)
        		msg.getData().putInt("peopleid",peopleid);
			
			if(peopleids != null)			
				msg.getData().putIntArray("peopleids", peopleids);
			
			SyncAddressBookHelper.checkIsEnableAddressbookSync(this.getApplicationContext(), orm, msg);
		}
		
		
		boolean eventSync = intent.getBooleanExtra("eventsync", false);
		if(eventSync)
		{
			Log.d(TAG, "new event sync task");
			fbEvent.doEventSync();
			Toast.makeText(SNSService.this,getString(R.string.sns_eventsync_toast_msg), Toast.LENGTH_SHORT).show();
		}
		
		boolean eventadd = intent.getBooleanExtra("eventadd", false);
		if(eventadd)
		{
			Log.d(TAG, "new event add task");
			//int eventid = intent.getIntExtra("eventid", -1);
			long eventid_1 = intent.getLongExtra("eventid", -1);
			int eventid = Integer.parseInt(String.valueOf(eventid_1));
			int subcategoryid = intent.getIntExtra("subcategoryid", -1);
			int categoryid = intent.getIntExtra("categoryid",-1);
			fbEvent.doEventAdd(eventid,subcategoryid,categoryid);
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
