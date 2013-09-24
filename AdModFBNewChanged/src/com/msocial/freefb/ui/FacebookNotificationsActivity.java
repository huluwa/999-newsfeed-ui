package com.msocial.freefb.ui;

import java.util.List;
import com.msocial.freefb.ui.FacebookEventActivity;
import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.ui.AccountListener.AccountManager;
import com.msocial.freefb.ui.adapter.FacebookNotificationAdapter;
import com.msocial.freefb.ui.view.FacebookNotificationItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FBNotifications;
import oms.sns.service.facebook.model.FBNotifications.EventInvites;
import oms.sns.service.facebook.model.FBNotifications.FriendRequests;
import oms.sns.service.facebook.model.FBNotifications.GroupInvites;
import oms.sns.service.facebook.model.FBNotifications.NotifyBase;
import oms.sns.service.facebook.model.FBNotifications.NotifyType;
import oms.sns.service.facebook.model.FBNotifications.Pokes;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

public class FacebookNotificationsActivity extends FacebookBaseActivity
{
    private final String TAG="FacebookNotificationsActivity";
    private FBNotifications notifications;
    private ListView notifyList;
    boolean isFromHome=false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_notification_ui);
        notifyList = (ListView)this.findViewById(R.id.facebook_notification_list);   
        notifyList.setFocusableInTouchMode(true);
        notifyList.setFocusable(true);
        notifyList.setOnCreateContextMenuListener(this);
        notifyList.setOnItemClickListener(listItemClickListener);
        isFromHome = this.getIntent().getBooleanExtra("fromhome", false);
        if(isFromHome)
        {
            Log.d(TAG, "I am from home lauch");
        }
        setTitle();
        
        setTitle(title);        

		View v = findViewById(R.id.progress_horizontal);
		if(v != null)
		{
		    progressHorizontal = (ProgressBar) v;
		}
		
        registerAccountListener();
        SocialORM.Account account = orm.getFacebookAccount();        
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);
	        	loadNotifications();
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }
	}
    
    @Override protected void onResume() 
    {
        super.onResume();
        Log.d(TAG, "onResume="+this);
        loadNotifications();    
    }    
    
    @Override
    public void onLogin() 
    {
    	super.onLogin();
    	
    	if(facebookA != null)
    	{
    		loadNotifications();
    	}
    }   

	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			Log.d(TAG, "notification Item clicked");
			if(FacebookNotificationItemView.class.isInstance(v))
			{
				FacebookNotificationItemView fv= (FacebookNotificationItemView)v;
				NotifyBase item = fv.getContent();				
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
						 FriendRequests fq = (FriendRequests)item;
						 if(fq.uids.size() > 0)
						 {
							 Intent intent = new Intent(mContext, FacebookRequestProcessActivity.class);						 
							 long[] uids = new long[fq.uids.size()];
							 for(int i=0;i<fq.uids.size();i++)
							 {
								 uids[i] = fq.uids.get(i);
							 }
							 intent.putExtra("uids", uids);
							 intent.putExtra("friend_request", true);
							 ((FacebookNotificationsActivity)mContext).startActivity(intent);
						 }
						 break;
					 }
					 case NotifyType.Pokes:
					 {
					     Log.d(TAG, "clicked Pokes");
					     Pokes pok = (Pokes)item;
					     if(pok.unread > 0)
					     {
    					     Intent intent = new Intent(mContext, FacebookPokeActivity.class);          
                             ((FacebookNotificationsActivity)mContext).startActivity(intent);
					     }
					     break;
					 }
					 case NotifyType.GroupInvites:
					 {  
					     Log.d(TAG,"clicked GroupInvites");
					     GroupInvites ginvites = (GroupInvites)item;
					     List<Long> gidlist = ginvites.uids;
					     if(gidlist!=null && gidlist.size()>0)
					     {
					         Intent intent = new Intent(mContext,FacebookGroupActivity.class);
					         intent.putExtra("fornotification", true);
					         long[] gids = new long[gidlist.size()];
					         for(int i=0;i<gidlist.size();i++)
					         {
					             gids[i] = gidlist.get(i);
					         }
					         intent.putExtra("gids", gids);
					         ((FacebookNotificationsActivity)mContext).startActivity(intent);
					     }
					     break;
					 }
					 case NotifyType.EventInvites:
					 {
					     Log.d(TAG,"clicked EventInvites");
					     Intent intent = new Intent(mContext,FacebookEventActivity.class);
					     EventInvites einvites = (EventInvites)item;
                         List<Long> eidlist = einvites.uids;
                         if(eidlist!=null && eidlist.size()>0)
                         {   
                             intent.putExtra("fornotification", true);
                             long[] eids = new long[eidlist.size()];
                             for(int i=0;i<eidlist.size();i++)
                             {
                                 eids[i] = eidlist.get(i);
                             }
                             intent.putExtra("eids", eids);   
                            ((FacebookNotificationsActivity)mContext).startActivity(intent);
                         }          
					     break;
					 }
				
	           	}
			}			
			
		}
	};

	@Override
	protected void loadRefresh()
	{
	    if(this.isInProcess() == true)
	    {
	        showToast();
	        return;
	    }
		loadNotifications();
	}
	
	/*
    @Override
	public void titleSelected()
    {
		super.titleSelected();
		if(isFromHome)
		{
			Intent intent = new Intent(this, FacebookProfileActivity.class);
			this.startActivity(intent);
			this.finish();
		}
		else
		{
		    loadNotifications();
		}
	}*/
    
    private void loadNotifications()
    {
    	handler.obtainMessage(FACEBOOK_NOTIFICATIONS_LOAD).sendToTarget();
    }
    
	@Override
	protected void createHandler() 
	{
		handler = new NotificationHandler();
	}

	public void setTitle() 
	{
		title=getString(R.string.notification_request);	
		if(isFromHome)
		{
			title = getString(R.string.menu_title_go_to_facebook);	
		}
	}	
	
	final int FACEBOOK_NOTIFICATIONS_LOAD     =10;
	final int FACEBOOK_NOTIFICATIONS_UI       =11;
	final int FACEBOOK_NOTIFICATIONS_LOAD_END =12;
	
	
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
	            case FACEBOOK_NOTIFICATIONS_LOAD:
	            {
	            	getNotifications();
	                break;	
	            }
	            case FACEBOOK_NOTIFICATIONS_UI:
	            {
	            	showNotificationUI();
	            	break;
	            }
	            case FACEBOOK_NOTIFICATIONS_LOAD_END:
	            {
	            	end();
	                break;	
	            }              
            }
        }
	}
	
	private void showNotificationUI()
	{
		if(notifications != null)
		{
		    FacebookNotificationAdapter fa = new FacebookNotificationAdapter(FacebookNotificationsActivity.this, notifications);
		    notifyList.setAdapter(fa);
		}
	}
	
	private void getNotifications()
	{
		if(facebookA == null || isInProcess())
		{
		    return;
		}
		
        begin();
		
    	Log.d(TAG, "before get notifications");
    	//notifyLoading();  
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
				
				notifications  = notifies;								
                if(donotcallnetwork == false)//I am still alive
                {
					handler.obtainMessage(FACEBOOK_NOTIFICATIONS_UI).sendToTarget();
	            	//cancelNotify();
                }       
                
                
                Message msd = handler.obtainMessage(FACEBOOK_NOTIFICATIONS_LOAD_END);
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
	            	Message msd = handler.obtainMessage(FACEBOOK_NOTIFICATIONS_LOAD_END);
	            	msd.getData().putBoolean(RESULT, false);
	            	handler.sendMessage(msd);
            	}
            }
    	});		
	}
}
