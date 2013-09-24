package com.msocial.nofree.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FBNotifications;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Notifications;
import oms.sns.service.facebook.model.Notifications.AppInfo;
import oms.sns.service.facebook.model.Notifications.Notification;

import com.msocial.nofree.R;
import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.providers.SocialORM.FacebookUsersCol;
import com.msocial.nofree.quickaction.QuickLauncher;
import com.msocial.nofree.service.SNSService;
import com.msocial.nofree.ui.AllAppsScreen.TitleActionListener;
import com.msocial.nofree.ui.FacebookProfileActivity.ActivityItemView;
import com.msocial.nofree.ui.FacebookProfileActivity.AppsAdapter;
import com.msocial.nofree.ui.FacebookProfileActivity.ProfileAdapter;
import com.msocial.nofree.ui.FacebookProfileActivity.ProfileItemView;
import com.msocial.nofree.ui.view.ImageCacheManager;
import com.msocial.nofree.ui.view.SNSItemView;
import com.msocial.nofree.ui.view.ImageCacheManager.ImageCache;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class DashBoardActivity extends FacebookBaseActivity implements TitleActionListener
{
	private ImageView head_inivte_friends;
	
	private GridView  mainMenu;
	private GridView  shortcut;
	private ListView  streamList;	
	private AllAppsScreen workspace;
	private Cursor        users;
	
	
    Button   facebook_current_pos;
    int unread=0;
    boolean doSetTitleAfterLoginInOnResume = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.workspace_ui);
		
		        
        facebook_current_pos = (Button)this.findViewById(R.id.facebook_current_pos);
        setPosition(true);
	        
		workspace = (AllAppsScreen)findViewById(R.id.workspace);
		workspace.setTitleListener(this);
		//init ui
		initUI();
		
		Intent in = new Intent(this.getApplicationContext(), SNSService.class);            
	    startService(in);
	    
        //
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
            perm_session = loginHelper.getPermanentSesstion(this);
            if(perm_session != null)
            {
                //perm_session.attachActivity(this);
                facebookA = new AsyncFacebook(perm_session);
                handler.obtainMessage(FACEBOOK_NOTIFICATION_GET).sendToTarget();
                reschedule();
            }
            else
            {             
                launchFacebookLogin();
            }
        }
        
        setTitle();
        setTitle(title);
	}
	
	private final int MainUI     = 0;
	private final int ShortCutUI = 1;
	private void initUI()
	{
		float density = this.getResources().getDisplayMetrics().density;
		{
			mainMenu = new GridView(DashBoardActivity.this);
			ViewGroup vg = (ViewGroup)workspace.getChildAt(MainUI);
			mainMenu.setColumnWidth((int)(100*density));
			mainMenu.setSelector(getResources().getDrawable(R.drawable.menuicon_hl));
			mainMenu.setNumColumns(GridView.AUTO_FIT);
			mainMenu.setAlwaysDrawnWithCacheEnabled(true);
			
			mainMenu.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.FILL_PARENT));
			vg.addView(mainMenu);
			
			loadApps();
			mainMenu.setAdapter(new AppsAdapter(this.getApplicationContext(), mApps));
			mainMenu.setFocusableInTouchMode(true);
			mainMenu.setFocusable(true);
			mainMenu.setSelected(true);
			mainMenu.setClickable(true);  
			mainMenu.setOnCreateContextMenuListener(this);
			mainMenu.setOnItemClickListener(listItemClickListener);
		}
		
		{
			shortcut = new GridView(DashBoardActivity.this);
			ViewGroup vg2 = (ViewGroup)workspace.getChildAt(ShortCutUI);
			shortcut.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.FILL_PARENT));
			vg2.addView(shortcut);
			shortcut.setColumnWidth((int)(100*density));
			shortcut.setNumColumns(GridView.AUTO_FIT);
			shortcut.setSelector(getResources().getDrawable(R.drawable.menuicon_hl));
			shortcut.setAlwaysDrawnWithCacheEnabled(true);
			
			//shortcut.setVerticalSpacing((int)(20*density));
			//shortcut.setHorizontalSpacing((int)(10*density));
			
			        
			shortcut.setFocusableInTouchMode(true);
			shortcut.setFocusable(true);
			shortcut.setSelected(true);
			shortcut.setClickable(true);  
			shortcut.setOnCreateContextMenuListener(this);
			shortcut.setOnItemClickListener(profileClickListener);        
		    users = orm.getAllShoutCutFacebookUsersCursor();
		    refreshShortCut();
		}
	}

	@Override
	public void refreshShortCut() 
    {
        if(users == null || users.moveToFirst() == false)
        {
            users = orm.getAllShoutCutFacebookUsersCursor();
        }
        
        shortcut.setAdapter(null);    	
        ProfileAdapter pa = new ProfileAdapter(this, orm, users);
        shortcut.setAdapter(pa);
	}
	
	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int position,long ID) 
		{
			if(ActivityItemView.class.isInstance(v))
			{
				ActivityInfo isnfo = mApps.get(v.getId());
				Intent intent;
				
				int pos = isnfo.name.lastIndexOf(".ui.");
				String cname = isnfo.name.substring(pos+1);
				intent = new Intent();		
				intent.setClassName("com.msocial.nofree", isnfo.name);
				if(isnfo.name.contains("FacebookAccountActivity"))
				{
					intent.putExtra("comefrommyself", true);
				}
				else if(isnfo.name.contains("AboutActivity"))
				{
					intent.putExtra("forabout", true);
				}
				
				DashBoardActivity.this.startActivityForResult(intent, 100);
			}
		}
	};
	
	QuickLauncher ql = new QuickLauncher();
	AdapterView.OnItemClickListener profileClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int position,long ID) 
		{
			if(ProfileItemView.class.isInstance(v))
			{
				ProfileItemView pv = (ProfileItemView)v;
				FacebookUser.SimpleFBUser user = pv.getUser();
				ql.popupQuickLauncher(DashBoardActivity.this, v,user);
				/*
				Intent intent = new Intent(mContext, FacebookAccountActivity.class);
				intent.putExtra("uid",      user.uid);
				intent.putExtra("username", user.name);
				intent.putExtra("imageurl", user.pic_square);					
				((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
				*/
			}
		}
	};
	
	private AdapterView.OnItemClickListener shortCutClick = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) 
		{
			Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("qiupu://profile/details?uid="+id));            
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);           
            startActivity(intent);	
		}		
	};
	
	private List<ActivityInfo> mApps = new ArrayList<ActivityInfo>();

    private void loadApps() 
    {
        PackageManager pm = getPackageManager();
        PackageInfo pinfo = null;
        try {
            pinfo = pm.getPackageInfo("com.msocial.nofree", PackageManager.GET_ACTIVITIES);
            if (pinfo != null) 
            {
                ActivityInfo[] tmpAppsArray = new ActivityInfo[15];
                for (ActivityInfo act : pinfo.activities) 
                {
                	if(act.name.contains(".ui.FacebookStreamActivity"))
                	{
                	    tmpAppsArray[0] = act;
                	}
                	else if(act.name.contains(".ui.FacebookAccountActivity"))
                    {
                	    tmpAppsArray[1] = act;
                    }
                	else if(act.name.contains(".ui.FacebookAlbumActivity"))
                	{
                	    tmpAppsArray[2] = act;
                	}
                    else if(act.name.contains(".ui.FacebookNewFriendsActivity"))
                    {
                        tmpAppsArray[3] = act;
                    }
                    else if(act.name.contains(".ui.FacebookMessageActivity"))
                    {
                        tmpAppsArray[4] = act;
                    }
                    else if(act.name.contains(".ui.FacebookNotificationsActivity"))
                    {
                        tmpAppsArray[5] = act;
                    }
                    else if(act.name.contains(".ui.FacebookEventActivity"))
                    {
                        tmpAppsArray[6] = act;
                    }
                    else if(act.name.contains(".ui.FacebookNotesActivity"))
                    {
                        tmpAppsArray[7] = act;
                    }
                    else if(act.name.contains(".FacebookNotificationManActivity"))
                    {
                        tmpAppsArray[8] = act;
                    }
                    else if(act.name.contains(".FacebookPhonebookActivity"))
                    {
                        tmpAppsArray[9] = act;
                    }
                    else if(act.name.contains(".FacebookLocationUpdateActivity"))
                    {
                        tmpAppsArray[10] = act;
                    }
                    else if(act.name.contains(".FacebookStatusUpdateActivity"))
                    {
                        tmpAppsArray[11] = act;
                    }
                    else if(act.name.contains(".FacebookSettingPreference"))
                    {
                        tmpAppsArray[12] = act;
                    }
                    else if(act.name.contains(".FacebookShareActivity"))
                    {
                        tmpAppsArray[13] = act;
                    }
                    else if(act.name.contains(".AboutActivity"))
                    {
                        tmpAppsArray[14] = act;
                    }                	
                    else
                    {
                    	/*act.name.contains(".ui.FacebookLocationUpdateActivity") ||
                        act.name.contains(".ui.FacebookPhonebookActivity")      ||
                        act.name.contains(".ui.FacebookSettingPreference")      ||                	                  		
                        act.name.contains(".ui.FacebookMainActivity") ||
                        act.name.contains(".ui.FacebookNotificationManActivity") ||
                        act.name.contains(".ui.FacebookNotificationsActivity") ||
                        act.name.contains(".ui.FacebookStatusUpdateActivity") || 
                        act.name.contains(".ui.AboutActivity")*/   
                    }               	    
                }
                
                sort(tmpAppsArray);
            }
        }catch (Exception e) {}
        
    }

    private void sort(ActivityInfo[] appArray)
    {
        for(int i=0; i<appArray.length; i++)
        {
            mApps.add(appArray[i]);
        }
        
        appArray = null;
    }

	@Override
	public void setTitle() 
	{
		title=getString(R.string.facebook_main_menu);		
		FacebookUser.SimpleFBUser user = orm.getSimpleFacebookUser(getLoginUserID());
		if(user != null)
		{
			title = user.name;
			user.despose();
			user = null;
		}		
		else
		{
		    if(perm_session != null)
            {
                long uids[] = new long[1];
                uids[0] = perm_session.getLogerInUserID();
                facebookA.getBasicUsersAsync(uids, new FacebookAdapter()
                {
                    public void getUsers(List<FacebookUser> users) 
                    {
                       if(users!=null && users.size()>0)
                       {
                           Log.d(TAG, "---get login user account -------- name:"+users.get(0).getName());
                           title = users.get(0).getName();
                           orm.addFacebookUser(users);
                           updateTitle(title);   
                       }
                    }
                });
            }
		}
	}

	@Override
	public void setPageTitle(int index) {
		if(index == 0)
		{
			setTitle(title);
			setPosition(true);
		}
		else
		{
			setPosition(false);
			setTitle(R.string.facebook_recent_visit_page_title);
		}
	}
	
	@Override
    protected void createHandler() {
        handler = new NotesHanlder();
    }
    
    protected  static final int FACEBOOK_NOTIFICATION_GET         = 1;
    protected  static final int FACEBOOK_NOTIFICATION_GET_END     = 2;
    
    private class NotesHanlder extends Handler
    {
	    public NotesHanlder()
        {
            super();            
            Log.d(TAG, "new NotesHanlder");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case FACEBOOK_NOTIFICATION_GET:
	            {
	            	notesGetNotification();
	            	//notesGet();
	            	break;
	            }
	            case FACEBOOK_NOTIFICATION_GET_END:
	            {
	            	if(isFinishing() == true)
	            	{
	            		clearAsyncFacebook(true);
	            	}
	            	else
	            	{
	            	    processNotificationSummary(notifies);
	            	}
	            	
	            	break;
	            }
            }
        }
    }
    
    public void reschedule()
	{
        Log.d(TAG, "reschedule at "+ new Date().toLocaleString());
		long nexttime = System.currentTimeMillis()+ 100;		
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent();
        i.setClassName("com.msocial.nofree", "com.msocial.nofree.ui.FacebookProfileActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);        
		alarmMgr.set(AlarmManager.RTC, nexttime, pi);		
	}
    
    
    public void alarmComming(boolean force)
	{
         if(orm.isNotificationEnable())
         {
			Log.d(TAG, " &&&&&&&&&&&  time it out="+ new Date().toLocaleString());
			Message msg = handler.obtainMessage(FACEBOOK_NOTIFICATION_GET);
			msg.sendToTarget();
			
			AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
			long nexttime = System.currentTimeMillis()+ ((force==true)?100:(5*60*1000));	
			
			Intent i = new Intent();
	        i.setClassName("com.msocial.nofree", "com.msocial.nofree.ui.FacebookProfileActivity");
	        i.setAction(ACTION_CHECK_CONTECT);
	        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);        
			alarmMgr.set(AlarmManager.RTC, nexttime, pi);
         }        
	}	
    
    
    @Override protected void doAfterLoginNothing()
    {
    	super.doAfterLoginNothing();
    	
    	DashBoardActivity.this.setResult(RESULT_CANCELED);
    	DashBoardActivity.this.finish();    
    }
    
    @Override
    protected void doAfterLogin()
    {
    	Log.d(TAG, "after login");
    	//try to get the session
    	doSetTitleAfterLoginInOnResume = true;
    	perm_session = loginHelper.getPermanentSesstion(this);
    	if(perm_session == null)
    	{
    	    //re-launch the login UI
    	    Log.d(TAG, "fail to get permanent session");
    	    Toast.makeText(this, R.string.facebook_no_valid_session, Toast.LENGTH_SHORT).show();    		
    	}
    	else
    	{
    	    facebookA = new AsyncFacebook(perm_session);
    	    perm_session.attachActivity(this);
    		
    	    //handler.obtainMessage(FACEBOOK_NOTIFICATION_GET).sendToTarget();
    	}   
    }
    
    @Override
	protected void onNewIntent(Intent intent) 
	{		
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent="+intent);
		if(intent.getAction() != null && intent.getAction().equals(ACTION_CHECK_CONTECT))
		{
		    setTitle();
		    setTitle(title);
			setIntent(intent);
		}
	}
    
    @Override protected void onResume() 
    {
        super.onResume();	
    	if(doSetTitleAfterLoginInOnResume == true)
    	{
    		String temptitle=getString(R.string.facebook_main_menu);		
    		FacebookUser.SimpleFBUser user = orm.getSimpleFacebookUser(getLoginUserID());
    		if(user != null)
    		{
    			temptitle = user.name;
    			user.despose();
    			user = null;
    		}
    		setTitle(temptitle);
    		setTitle();
    		
    		if(notifies != null)
        	{
        		 notifies.msg.unread = 0;
        		 notifies.entInvite.uids.clear();
        		 notifies.poke.unread = 0;
        		 notifies.grdInvite.uids.clear();
        		 notifies.frdRequest.uids.clear();
        		 processNotificationSummary(notifies);
        	}
    	}
    	doSetTitleAfterLoginInOnResume = false;	    
		if(users != null)
		{
			try{
			    users.close();
			}catch(Exception ne){}
		}
		users = orm.getAllShoutCutFacebookUsersCursor();    		
        ProfileAdapter pa = new ProfileAdapter(this, orm, users);
        shortcut.setAdapter(pa);
    	
    	//cancel first
    	AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
    	Intent i = new Intent();
        i.setClassName("com.msocial.nofree", "com.msocial.nofree.ui.FacebookProfileActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pi);
         
        //resume to call
        alarmComming(false);        
    }
    
    @Override protected void onPause() 
    {   
        super.onPause(); 
        
        AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
        
        Intent i = new Intent();
        i.setClassName("com.msocial.nofree", "com.msocial.nofree.ui.FacebookProfileActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pi);
        
        Log.d(TAG, "Cancel alarm");
    }
    
    private void vibratePhone()
    {
    	if(orm.getNotificationVibrate())
    	{
    	    final Vibrator vib  = (android.os.Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(2*1000);
    	}
    }
    
    private void FlashLED()
    {
    	if(orm.getNotificationLED())
    	{
    		Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            findViewById(R.id.container).startAnimation(shake);
    	}
    }
    
    FBNotifications notifies ;
    private void notesGet()
	{	
    	if(isSafeCallFacebook(true) == false)
    	{
    		return ;
    	}
    	
        Log.d(TAG, "before get notes message");
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}	    	
    	
    	
    	facebookA.getNotificationsAsync(perm_session.getLogerInUserID(),  new FacebookAdapter()
    	{
    		@Override public void geNotifications(FBNotifications notifies)
            {
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
				DashBoardActivity.this.notifies = notifies;
//				processNotificationSummary(notifies);
                if(donotcallnetwork == false)//I am still alive
                {
					handler.obtainMessage(FACEBOOK_NOTIFICATIONS_UI).sendToTarget();
	            	//cancelNotify();
                }       
                
                
                Message msd = handler.obtainMessage(FACEBOOK_NOTIFICATION_GET_END);
            	msd.getData().putBoolean(RESULT, true);
            	handler.sendMessage(msd);
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
            	//get from Database
            	if(isInAynscTaskAndStoped())
            	{
            		Log.d(TAG, "User stop passive");
            	}
            	else
            	{
	            	Message msd = handler.obtainMessage(FACEBOOK_NOTIFICATION_GET_END);
	            	msd.getData().putBoolean(RESULT, false);
	            	handler.sendMessage(msd);
            	}
            }
    	});
	}
    
    //we can set UI here
    private void processNotificationSummary(FBNotifications notifies)
    {
    	//check whether enable show Message, Request, Poke,
    	//for Message UI update
        if(null == notifies)
        {
            Log.e(TAG,"processNotificationSummary notifies is null!");
            return;
        }
        
    	if(orm.isNotificationEnable(0))
    	{
    		//process Inbox
            //updateUnreadMailInBoxThreads(notifies.msg.unread);
    	}
    	
    	int unprocessMessage = 0;
    	if(orm.isNotificationEnable(1))
    	{
    		unprocessMessage += notifies.poke.unread;
    	}
    	
    	if(orm.isNotificationEnable(2))
    	{
    		int eventInviteCount = notifies.entInvite.uids.size();
    		unprocessMessage += notifies.entInvite.uids.size();
    		//updateEventInviteRequests(eventInviteCount);
    	}
    	
    	if(orm.isNotificationEnable(3))
    	{
    		unprocessMessage += notifies.grdInvite.uids.size();
    	}
    	
    	if(orm.isNotificationEnable(4))
    	{
    		unprocessMessage += notifies.frdRequest.uids.size();
    	}
    	
    	//set UI right-top information
    	//updateUnreadRequests(unprocessMessage);
    }
    
    private void notesGetNotification()
	{	
    	if(isSafeCallFacebook(true) == false)
    	{
    		return ;
    	}
    	
        Log.d(TAG, "before get notes message");
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}	    	
    	
    	facebookA.getNotificationListAsync(-1L, false, new FacebookAdapter()
        {
            @Override public void getNotificationList(Notifications notifications)
            {
                Log.d(TAG, "after get notification ="+notifications.notificationlist.size());
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                newNotes(notifications.notificationlist, notifications.appinfo);
                
                Message msd = handler.obtainMessage(FACEBOOK_NOTIFICATION_GET_END);                
                handler.sendMessage(msd);
            }
            
            @Override public void onException(FacebookException e, int method, Object[] args) 
            {
                Log.d(TAG, "fail to get get notifcation="+e.getMessage());
                synchronized(mLock)
                {
                    inprocess = false;
                }
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                     Message msd = handler.obtainMessage(FACEBOOK_NOTIFICATION_GET_END);                    
                     handler.sendMessage(msd);
                }
            }
        });
	}
    
	protected void newNotes(final ArrayList<Notification> notificationlist, ArrayList<AppInfo> appinfo) 
	{
	    SNSService.getSNSService().getNotificationService().processNotify(notificationlist);
	    
	    if(false)
	    {
    		//change UI behavior
    		handler.post( new Runnable()
    		{
    			public void run()
    			{
    				if(notificationlist != null && notificationlist.size() > 0)
    				{
    					
    					if(unread != notificationlist.size() && notificationlist.size()>0)
    					{
    						unread = notificationlist.size();
    						/*
    						 *vibratePhone();
    						 *FlashLED();
    						 */
    					}
    				}
    				else
    				{
    					
    				}
    			}
    		});
	    }
	}
	
	private void setPosition(boolean inLeft)
    {
    	if(inLeft)
    	    facebook_current_pos.setBackgroundResource(R.drawable.profile_on);
    	else
    		facebook_current_pos.setBackgroundResource(R.drawable.profile_off);
    }
	
	View.OnClickListener showNotificationClick = new View.OnClickListener()
    {
        public void onClick(View v) 
		{
		    Log.d(TAG, "showNotificationClick you click first one=");
				 
		    Intent intent = new Intent(DashBoardActivity.this, FacebookNotificationManActivity.class);
		    DashBoardActivity.this.startActivityForResult(intent, FACEBOOK_NOTIFICATION_RESULT);
		}
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) 
    {
        switch (requestCode) 
        {
	        case FACEBOOK_NOTIFICATION_RESULT: 
	        {
	        	//make the unread to read
	        	return ;
	        }
        }
        
        super.onActivityResult(requestCode, resultCode, intent);
    }
    
}
