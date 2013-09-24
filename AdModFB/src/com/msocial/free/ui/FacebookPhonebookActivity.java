package com.msocial.free.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.msocial.free.R;
import com.msocial.free.providers.SocialORM;
import com.msocial.free.service.FacebookLoginHelper;
import com.msocial.free.service.SNSService;
import com.msocial.free.service.FacebookSyncHelper;
import com.msocial.free.service.dell.ContactHelper;
import com.msocial.free.service.dell.ContactID;
import com.msocial.free.service.dell.ContactInternal;
import com.msocial.free.ui.AccountListener.AccountManager;
import com.msocial.free.ui.adapter.FacebookPhoneBookAdapter;
import com.msocial.free.ui.lisenter.SyncFacebookAdapter;
import com.msocial.free.ui.view.FacebookFriendItemView;
import com.msocial.free.ui.view.FacebookPhoneBookItemView;
import com.msocial.free.util.DateUtil;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.PhoneBook;
import oms.sns.service.facebook.util.StringUtils;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Contacts;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookPhonebookActivity extends FacebookBaseActivity
{
    private final static String TAG      = "FacebookPhonebookActivity";
    
	private ListView friendList;	
	private View     searchSpan;
	private EditText keyEdit;
		
	private Cursor          phonebooks;
	private Cursor          sync_cursor;
	
	private MyWatcher watcher;
    
	private int currentPage = 0;
	private int pagesize    = 20;
	private int pageCount   = 1;	
	private int totalCount  = 0;

	NotificationManager mNM;
	final int FACEBOOK_CONTACT_SYNCING = 1;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_phonebook_main);
        friendList = (ListView)this.findViewById(R.id.facebook_phone_list);   
        friendList.setFocusableInTouchMode(true);
        friendList.setFocusable(true);
        friendList.setOnCreateContextMenuListener(this);
        friendList.setOnItemClickListener(listItemClickListener);
        
        searchSpan = this.findViewById(R.id.facebook_search_span);
        searchSpan.setVisibility(View.VISIBLE);
        keyEdit = (EditText)this.findViewById(R.id.embedded_text_editor);
        watcher = new MyWatcher();         
        keyEdit.addTextChangedListener(watcher);        
                
        
        View v = findViewById(R.id.progress_horizontal);
		if(v != null)
		{
		    progressHorizontal = (ProgressBar) v;
		}

        //current_slide.setText(" ");
        
        setTitle(R.string.facebook_phonebook_title);
        
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);
	        	
	        	handler.obtainMessage(FACEBOOK_PHONEBOOK_UI).sendToTarget();	 
	        	Cursor tmp = orm.getPhonebooksCursor();
	        	if(tmp == null || tmp.getCount() == 0)
	        	{
	        	    lauchGePhoneBook();
	        	}
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }
    }
	
	@Override protected void onDestroy() 
    {  
	    if(phonebooks != null)
	    {
	        try{
	            phonebooks.close();
	        }catch(Exception ne){}
	        phonebooks = null;
	    }
	    
	    perm_session.destroy();
	    perm_session = null;
	    
	    clearAsyncFacebook(true);
	    super.onDestroy();
    }
	
	public void setTitle()
    {
    	title = getString(R.string.facebook_phonebook_title);
    }
	
	/*
	@Override
	public void titleSelected()
	{
		super.titleSelected();
		
		if(orm.isEnableSyncPhonebook() == false)
		{
		    Message msg = handler.obtainMessage(SYNC_PHONEBOOK_ADDRESSBOOK);
		    SyncAddressBookHelper.processSyncAddressBook(mContext, true, null, msg);
		}
		else
		{
		    doAddPhonebookIntoContact();
		}
	}*/
	
	@Override
    protected void doGlobalSyncToContact() 
	{
		Log.d(TAG, "start global sync contact");		
		
		Intent in = new Intent(this, SNSService.class);		
		in.putExtra("lookupall", true);
        startService(in);
	}
	
	@Override
	protected void doLookupallContact() 
	{
		doGlobalSyncToContact();
	}
	
	 @Override
     protected void loadRefresh()
     {
		 super.loadRefresh();
		 
		 SocialORM.Account account = orm.getFacebookAccount();
         if(checkFacebookAccount(this, account))
         {
        	  perm_session = loginHelper.getPermanentSesstion(this);
        	  if(perm_session != null)
        	  {
	        	  perm_session.attachActivity(this);
	        	  facebookA = new AsyncFacebook(perm_session);
	        	  
	        	  lauchGePhoneBook();
        	  }
        	   else
        	  {
        		  launchFacebookLogin();
        	  }
         }     
    	
      }
	
	ContactSaveHandler saveContact;
	Looper             saveLooper;
	boolean insaveing         =false;//to control, when I am doing saving
	boolean afterGetFriendData=false;//just get one time, when do repeat
	@Override
    protected void doAddPhonebookIntoContact() 
	{
		if(orm.isEnableSyncPhonebook() == false)
		{
		    Message msg = handler.obtainMessage(SYNC_PHONEBOOK_ADDRESSBOOK);
		    SyncAddressBookHelper.processSyncAddressBook(mContext, true, null, msg);
		}
		else
		{
			syncPhonebook();
		}		
	}
	
	private void syncPhonebook()
	{
		if(phonebooks == null || phonebooks.getCount() == 0)
		{
			return;
		}
		
	    if(insaveing == true)
        {
            Log.d(TAG, "I am saving contacts");
            Toast.makeText(FacebookPhonebookActivity.this,R.string.facebook_phonebook_in_saving, Toast.LENGTH_SHORT).show();
            return;
        }
	    
	    Toast.makeText(mContext, R.string.facebook_phonebook_sync_to_contact, Toast.LENGTH_SHORT).show();
	    
	    synchronized(mLock)
        {
            insaveing = true;
        }
	    
	    //re-get friend data for sync to native contact
	    if(afterGetFriendData == false)
	    {
	        handler.obtainMessage(GET_FRIEND_USER_INFO).sendToTarget();
	    }
	    else//for repeat do the save process, no need to get facebook information again
	    {
	        startToSaveContact();
	    }
	}
	
	private void startToSaveContact()
	{
	    //TODO you can add contact, if the pre is not finished
        Thread task = new Thread()
        {
            public void run()
            {
                this.setName("Contact save="+this.getId());               
                
                Looper.prepare();
                saveLooper = Looper.myLooper();             
                saveContact = new ContactSaveHandler();                
                //let wait reach first
                try{  Thread.currentThread().sleep(2000);}catch(InterruptedException ne){}
                
                synchronized (mLock)
                {
                    mLock.notifyAll();
                }   
                
                Looper.loop();  
                Log.d(TAG , "exit save contact thread");
                synchronized(mLock)
                {
                    insaveing = false;
                }
            }
        };
        task.start();
                
        synchronized (mLock)
        {
            try 
            {   
                mLock.wait(30*1000);
                if(saveContact != null)
                {
                    Message mds = saveContact.obtainMessage(NEXT_CONTACT);              
                    mds.getData().putInt("position", 0);                
                    saveContact.sendMessage(mds);
                }
                else
                {
                    Log.d(TAG, "why I am null");
                }
            } catch (InterruptedException e) {}
        }
	}
	
	final int NEXT_CONTACT            =9999;	
	final int FACEBOOK_FRIENDS_GET_END=9997;
	
	//don't move phonebooks cursor, use a temp one
	private PhoneBook getPhonebook(final Cursor pb, int pos)
	{
	    PhoneBook ph = null;
	    Cursor tmp = pb;
	    if(tmp != null)
	    {
	        boolean suc = tmp.moveToPosition(pos);
	        if(suc)
	        {
	            ph = orm.formatPhoneBook(tmp);
	        }
	    }
	    return ph;
	}
	
	private Cursor getSyncCursor()
	{
	    if(sync_cursor==null)
	    {
	        sync_cursor = orm.getPhonebooksCursor();
	    }
	    
	    if(sync_cursor != null)
	    {
	        sync_cursor.requery();
	    }
	    return sync_cursor;
	}
	
	private Cursor phonebooks()
	{
	    if(phonebooks != null)
	    {
	        boolean suc =  phonebooks.requery();
	    }
	    return phonebooks;
	}
	
	public class ContactSaveHandler extends Handler
	{
	    @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case NEXT_CONTACT:
	            {
	            	int pos = msg.getData().getInt("position", -1);
	            	Cursor cr = getSyncCursor();
	            	if(cr == null)
	            	{
	            	    if(sync_cursor != null)
                        {
                            sync_cursor.close();
                            sync_cursor = null;
                        }
	            	    
	            	    Log.d(TAG, "quit the save");
                        saveLooper.quit();
                        return ;
	            	}
	            	
	            	synchronized(cr)
	            	{
		            	if(pos != -1 && pos < getSyncCursor().getCount())
		            	{
		            		processProgress(pos, getSyncCursor().getCount());
			            	PhoneBook phone =  getPhonebook(getSyncCursor(), pos);
							addNewContact(FacebookPhonebookActivity.this, phone, orm);
							Log.d(TAG, "add phone to contact db="+phone+" pos="+pos+" totle="+getSyncCursor().getCount());
							
							//process next one
							Message mds = this.obtainMessage(NEXT_CONTACT);
							pos++;
							mds.getData().putInt("position", pos);
							
							sendMessage(mds);
		            	}
		            	else
		            	{
		            	    if(sync_cursor != null)
	                        {
	                            sync_cursor.close();
	                            sync_cursor = null;
	                        }
		            	    
		            		Log.d(TAG, "quit the save");
		            		saveLooper.quit();
		            	}
	            	}										
	            	break;
	            }
            }
        }
	};
	
	private static List<Integer> getPeopleID(Context con, long uid) 
	{		
		return ContactHelper.getPeopleList(con, uid);
	}
	
	public void processProgress(int pos, int size) 
	{		
		
	}
	
	//for debug test
	public static boolean addNewContact(Context con, PhoneBook phone, SocialORM orm)
	{
	    return ContactInternal.AddNewPhoneBook(con, orm, phone)>0;		
	}
	
	@Override protected void syncToContact(PhoneBook phonebook)
	{
		if(orm.isEnableSyncPhonebook() == false)
		{
		    Message msg = handler.obtainMessage(SYNC_PHONEBOOK_ONE_ADDRESSBOOK);
		    msg.getData().putLong("uid", phonebook.uid);		    
		    SyncAddressBookHelper.processSyncAddressBook(mContext, true, null, msg);
		}
		else
		{
			Log.d(TAG, "sync phonebook to contact="+phonebook);
			
			Message msg = handler.obtainMessage(SYNC_PHONEBOOK_ONE_ADDRESSBOOK);
			msg.getData().putLong("uid", phonebook.uid);
			SyncAddressBookHelper.checkIsEnableAddressbookSync(mContext, orm, msg);
	        
			
//			Toast.makeText(FacebookPhonebookActivity.this, R.string.facebook_phonebook_sync_to_contact, Toast.LENGTH_SHORT).show();
//			new SyncContactTask(FacebookPhonebookActivity.this.getApplicationContext(), phonebook.clone(), orm).execute();
		}
	}
	
	protected static class SyncContactTask extends android.os.AsyncTask<PhoneBook, Void, Void>
    {       
        public SyncContactTask(Context con, PhoneBook book, SocialORM orm)
        {
            super();
            this.orm = orm;
            this.book = book;
            this.con = con.getApplicationContext();
            Log.d(TAG, "Sync Phonebook to Contact");
        }
        PhoneBook book;
        Context con;
        SocialORM orm;
        
        @Override
        protected Void doInBackground(PhoneBook ...pbs) 
        {           
        	//we need the latest user logo
            addNewContact(con, book, orm);    
            book = null;
            return null;
        }        
    }
	
	@Override
	protected void loadCallCell(String phone) 
	{
		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
	}
	
	@Override
    protected void loadSendEmail(String email) 
	{		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + email));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
	}

	
	private void lauchGePhoneBook()
	{
	    if(this.isInProcess())
	    {
	        Log.d(TAG, "I am get phonebook="+this);
	        Toast.makeText(mContext, R.string.getting_phonebook_from_server, Toast.LENGTH_SHORT).show();
	        return;
	    }
	    
	    //change from iPhone design,
		//we think we can get phonebook without user action in background
		//just when user want to do sync them into address book
		//
	    Message msg = handler.obtainMessage(FACEBOOK_PHONEBOOK_GET);
		msg.getData().putBoolean("RESULT", true);
		msg.sendToTarget();
	    
	    //Message msg = handler.obtainMessage(FACEBOOK_PHONEBOOK_GET);
		//SyncAddressBookHelper.checkIsEnableAddressbookSync(mContext,orm,msg);
	}
	
	@Override
	protected void createHandler() 
	{
		handler = new PhonebookHandler();		
	} 
	
	
	final int FACEBOOK_PHONEBOOK_GET     =0;
	final int FACEBOOK_PHONEBOOK_UI      =1;
	final int FACEBOOK_PHONEBOOK_GET_END =2;
	final int GET_FRIEND_USER_INFO       =3;
	final int GET_FRIEND_USER_INFO_END   =4;
	final int SYNC_PHONEBOOK_ADDRESSBOOK =5;
	final int SYNC_PHONEBOOK_ONE_ADDRESSBOOK =6;
	
	private class PhonebookHandler extends Handler 
	{
        public PhonebookHandler()
        {
            super();            
            Log.d(TAG, "new PhonebookHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case SYNC_PHONEBOOK_ADDRESSBOOK:
                {
                	if(msg.getData().getBoolean("RESULT", false) == true)
                	{
                		syncPhonebook();
                	}
                	else
                	{
                		Log.d(TAG, "user select cancel");
                	}
                	break;
                }
                case SYNC_PHONEBOOK_ONE_ADDRESSBOOK:
                {
                	if(msg.getData().getBoolean("RESULT", false) == true)
                	{
                		Toast.makeText(FacebookPhonebookActivity.this, R.string.facebook_phonebook_sync_to_contact, Toast.LENGTH_SHORT).show();
                		
                		long uid = msg.getData().getLong("uid", -1);
                		PhoneBook pb = orm.getPhonebook(uid);
                		Log.d(TAG, "sync phonebook to contact="+pb);
                		new SyncContactTask(FacebookPhonebookActivity.this.getApplicationContext(), pb.clone(), orm).execute();
                		pb = null;
                	}
                	else
                	{
                		Log.d(TAG, "user select cancel");
                	}
                	break;
                }
	            case GET_FRIEND_USER_INFO:
	            {
	                Message backMsg = this.obtainMessage( GET_FRIEND_USER_INFO_END);	                
	                //let service get Facebook user 
	                SNSService.getSNSService().getContactService().alarmFacebookUserComming(backMsg);
	                
	                /*
	                AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
	                Intent i = new Intent();
	                i.setClassName("com.msocial.free", "com.msocial.free.service.SNSService");
	                i.setAction("com.msocial.free.intent.action.FACEBOOK_USER");
	                i.putExtra("callback", backMsg);
	                PendingIntent userpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
	                alarmMgr.set(AlarmManager.RTC_WAKEUP, 10, userpi);
	                */
	                break;
	            }
	            case GET_FRIEND_USER_INFO_END://get from back ground contact service result
                {
                    //if get friends fail, also continue to save the data
                    if(msg.getData().getBoolean("RESULT", false) == false)
                    {
                        startToSaveContact();
                    }
                    else
                    {
                        //begin to process save contact
                        afterGetFriendData = true;
                        startToSaveContact();  
                    }
                    break;
                }
                case FACEBOOK_PHONEBOOK_GET:
                {
                	if(msg.getData().getBoolean("RESULT", false)== true)
	            	{
                	    inprocess = true;
                	    begin();
                	    
                	    Message backMsg = this.obtainMessage(FACEBOOK_PHONEBOOK_GET_END);
                	    SNSService.getSNSService().getContactService().alarmPhonebookComming(backMsg);
                	    
                        //let service get phonebook                	    
                	    /*
                        AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
                        Intent i = new Intent();
                        i.setClassName("com.msocial.free", "com.msocial.free.service.SNSService");
                        i.setAction("com.msocial.free.intent.action.FACEBOOK_PHONEBOOK");
                        i.putExtra("callback", backMsg);
                        PendingIntent userpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
                        alarmMgr.set(AlarmManager.RTC_WAKEUP, 10, userpi);
                        */
	            	}	            	
	            	break; 
                }  
                case FACEBOOK_PHONEBOOK_UI:
                {
                    Log.d(TAG, "FACEBOOK_PHONEBOOK_UI");
                	if(phonebooks != null)
                    {
                        phonebooks.requery();
                    }
                    else
                    {
                        phonebooks = orm.getPhonebooksCursor();
                        Log.d(TAG, "do I have phonebook="+phonebooks);
                    }
                    
                	FacebookPhoneBookAdapter phonebook = new FacebookPhoneBookAdapter(FacebookPhonebookActivity.this, phonebooks);
                	friendList.setAdapter(phonebook);                 
                	break;
                }
                case FACEBOOK_PHONEBOOK_GET_END:
                {                    
                    end();
                    inprocess = false;
                    
                    if(msg.getData().getBoolean("RESULT", false) == false)
                    {   
                        Log.d(TAG, "Fail to get phone book from web");
                        Toast.makeText(mContext, R.string.fail_get_latest_phonebook, Toast.LENGTH_SHORT).show();                        
                    }
                    else
                    {
                        this.obtainMessage(FACEBOOK_PHONEBOOK_UI).sendToTarget();
                    }
                	break;
                }
            }
        }
	}
	
	private void notifyLoading() {
		notify.notifyOnce(R.string.facebook_phonebook_loading, R.drawable.facebook_logo, 30*1000);	
	}

	private void doSearch(String key)
    {  
        if(key != null && key.length()>0)
        {
            if(phonebooks != null)
            {
                phonebooks.close();
                phonebooks = null;                
            }            
            phonebooks = orm.searchPhonebooksCursor(key);            
        }  
        else
        {
            if(phonebooks != null)
            {
                phonebooks.close();
                phonebooks = null;
            }
            phonebooks = orm.getPhonebooksCursor();
        }     
        
        FacebookPhoneBookAdapter phonebook = new FacebookPhoneBookAdapter(FacebookPhonebookActivity.this, phonebooks);
        friendList.setAdapter(phonebook);         
        
        //handler.obtainMessage(FACEBOOK_PHONEBOOK_UI).sendToTarget();
    }
	
	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			Log.d(TAG, "phonebookdetailOnClik Item clicked");
			if(gestureprocessed == true)
			{
				gestureprocessed = false;
				return;
			}
			else
			{
				if(FacebookPhoneBookItemView.class.isInstance(v))
				{
					FacebookPhoneBookItemView fv= (FacebookPhoneBookItemView)v;
					Intent intent = new Intent(mContext, FacebookPhonebookDetailActivity.class);
					intent.putExtra("phonebookid",      fv.getPhoneBook().uid);								
					((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_PHONRBOOK_DETAIL);
				}
			}
			
		}
	};

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {         
            Log.d(TAG, "KEYCODE_BACK coming="+this);         
        }
        return super.onKeyDown(keyCode, event);
    }
    
    
	private class MyWatcher implements TextWatcher 
    {   
       public void afterTextChanged(Editable s) 
       {
           //do search
           doSearch(s.toString());
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }	
}
