package com.msocial.facebook.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.provider.Browser;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import com.msocial.facebook.R;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.ui.AccountListener.AccountManager;
import com.msocial.facebook.ui.view.ImageRun;
import com.msocial.facebook.ui.view.SNSItemView;
import com.msocial.facebook.util.DateUtil;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Notifications;
import oms.sns.service.facebook.model.Notifications.AppInfo;


public class FacebookNotificationManActivity extends FacebookBaseActivity{

    private ListView notificationList;
    private View     facebook_info_span;
    private TextView facebook_info;
    private int  viewsize   = 50;
    private int  limitsize  = 20;
    private int  currentPos = 0;
    private ArrayList<Notifications.Notification> notifications = new ArrayList<Notifications.Notification>();
    private ArrayList<Notifications.AppInfo>      appinfos = new ArrayList<Notifications.AppInfo>();  
    
    @Override
    protected void loadRefresh()
    {
    	super.loadRefresh();
    	if(this.isInProcess() == true)
    	{
    	    showToast();
    	}
    	launchLoadNotification();
    }
    
    /*
    protected void showOptionMenu()
	{
		setTitleMenuIconVisible(false);
	}*/
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_main_ui);
        
        notificationList = (ListView)this.findViewById(R.id.facebook_wall_list);   
        notificationList.setFocusableInTouchMode(true);
        notificationList.setFocusable(true);
        notificationList.setSelected(true);
        notificationList.setClickable(true);        
        notificationList.setOnCreateContextMenuListener(this);
        notificationList.setOnItemClickListener(listItemClickListener);
        
        facebook_info_span = (View)this.findViewById(R.id.facebook_info_span);
        facebook_info      = (TextView)this.findViewById(R.id.facebook_info);

		View v = findViewById(R.id.progress_horizontal);
		if(v != null)
		{
		    progressHorizontal = (ProgressBar) v;
		}
		
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
            perm_session = loginHelper.getPermanentSesstion(this);
            if(perm_session != null)
            {
                perm_session.attachActivity(this);
                
                facebookA = new AsyncFacebook(perm_session);
                
                new DeSerializationTask().execute((Void[])null);
                launchLoadNotification();
                setMenu(true);
            }
            else
            {
                setMenu(false);
                launchFacebookLogin();
            }
        }     
        
        notify.cancelNotification();
        //setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.facebook_notification);
    }
    
    private static String notification_sfile=TwitterHelper.notification;
		
    private class DeSerializationTask extends android.os.AsyncTask<Void, Void, Void>
    {       
        public DeSerializationTask()
        {
            super();            
            Log.d(TAG, "create DeSerializationTask for notification");
        }

		@Override
		protected Void doInBackground(Void... params)			  
        {
			deSerialization();
            return null;
        }
    }
	
	private void deSerialization()
	{
		synchronized(notifications)
		{
			FileInputStream fis = null;
			ObjectInputStream in = null;
			try{
			    fis = new FileInputStream(notification_sfile);
			    in = new ObjectInputStream(fis);
			    long lastrecord = in.readLong();
			    Date now = new Date();
			    
			    if((now.getTime() -lastrecord) >12*60*60*1000)
			    {
			    	Log.d(TAG, String.format("it is %1%s hours ago, ignore the data", (now.getTime() -lastrecord)/(60*60*1000)));
			    	in.close();
			    	return ;
			    }
			    
			    int count = in.readInt();
			    for(int i=0;i<count;i++)
			    {
			    	Notifications.Notification item = (Notifications.Notification) in.readObject();
			    	notifications.add(item);
			    }
			    
			    count = in.readInt();
			    for(int i=0;i<count;i++)
			    {
			    	Notifications.AppInfo item = (Notifications.AppInfo) in.readObject();
			    	appinfos.add(item);
			    }
			    
			    in.close();
			    handler.obtainMessage(FACEBOOK_NOTIFICATION_UI).sendToTarget();
			}
			catch(IOException ex)
			{
				Log.d(TAG, "deserialization fail="+ex.getMessage());
			}
			catch(ClassNotFoundException ex)
			{
				Log.d(TAG, "deserialization fail="+ex.getMessage());
			}
		}		
	}
	
	private void serialization()
	{
		if(notifications != null && notifications.size() > 0)
		{
			synchronized(notifications)
			{
				FileOutputStream fos = null;
				ObjectOutputStream out = null;
				try
				{
				    fos = new FileOutputStream(notification_sfile);
				    out = new ObjectOutputStream(fos);
				    Date date = new Date();
				    out.writeLong(date.getTime());
				    int count = notifications.size();
				    //just cache last 20 items
				    if(count > 20)
				    	count = 20;
				    out.writeInt(count);
				    
				    for(int i=0;i<count;i++)
				    {
				    	Notifications.Notification item = notifications.get(i);
				    	out.writeObject(item);
				    }
				    
				    count = appinfos.size();				    
				    out.writeInt(count);
				    for(int i=0;i<count;i++)
				    {
				    	Notifications.AppInfo item = appinfos.get(i);
				    	out.writeObject(item);
				    }				    
				    out.close();
				}
				catch(IOException ex)
				{
				    Log.d(TAG, "serialization fail="+ex.getMessage());
				}
			}
		}
	}
    
    private void doNoContent()
    {
        if(notifications.size() == 0)
        {
            //facebook_info_span.setVisibility(View.VISIBLE);
            //facebook_info.setText(R.string.no_notes_hint);
        }
        else
        {
            facebook_info_span.setVisibility(View.GONE);
        }
    }
    
    AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
        {
            Log.d(TAG, "Notification Item clicked");
            //I think we don't need to do this, if use read them, all should be read
            /*
            if(NotesItemView.class.isInstance(v))
            {
            	NotesItemView nv = (NotesItemView)v;
            	Notifications.Notification not = nv.getNotes();
            	if(not.is_unread == true)
            	{
	            	long nid = not.notification_id;
	            	Message msg = handler.obtainMessage(FACEBOOK_MARKREAD);
	            	msg.getData().putLong("nid", nid);
	            	msg.sendToTarget();
            	}
            }
            */            
        }
    };
    
    public void refreshUI(final long nid) 
    {
		for(int i=0;i<notifications.size();i++)
		{
			if(notifications.get(i).notification_id == nid)
			{
				notifications.get(i).is_unread = false;
				handler.post( new Runnable()
				{
					public void run()
					{						
						//process for UI
				    	for(int j=0;j<notificationList.getChildCount();j++)    		 
				        {
				            View v = notificationList.getChildAt(j);
				            if(NotesItemView.class.isInstance(v))
				            {
				            	NotesItemView nv = (NotesItemView)v;
				            	if(nv.getNotes().notification_id == nid)
				            	{
				            		nv.setUI();
				            		break;
				            	}
				            }
				        }
					}
				});
			}
		}
	}
    
    private void launchLoadNotification() 
    {
        handler.obtainMessage(FACEBOOK_NOTIFICATION_GET).sendToTarget();   
    }
    protected  static final int FACEBOOK_NOTIFICATION_GET         = 1;
    protected  static final int FACEBOOK_NOTIFICATION_GET_END     = 2;
    protected  static final int FACEBOOK_NOTIFICATION_UI          = 3; 
    protected  static final int FACEBOOK_MARKREAD                 = 4;
    protected  static final int FACEBOOK_MARKREAD_END             = 5;
    protected  static final int FACEBOOK_NOTIFICATION_APPINFO_GET             = 6;
    
    
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
                    notesGet();
                    break;
                }
                case FACEBOOK_NOTIFICATION_APPINFO_GET:
                {
                    appinfoGet();
                    break;
                }
                case FACEBOOK_MARKREAD:
                {
                	long nid = msg.getData().getLong("nid", -1);
                	markRead(nid);
                	break;
                }
                case FACEBOOK_MARKREAD_END:
                {
                	long nid = msg.getData().getLong("nid", -1);
                	if(msg.getData().getBoolean(RESULT, false))
                	{
                		refreshUI(nid);
                	}
                	break;
                }
                case FACEBOOK_NOTIFICATION_UI:
                {
                	if(isFinishing() == false)
                	{
	                    NoteAdapter na = new NoteAdapter(FacebookNotificationManActivity.this, notifications);
	                    notificationList.setAdapter(na);
                	}
                    
                    markUnread();
                    break;
                }
                case FACEBOOK_NOTIFICATION_GET_END:
                {
                    end();
                    break;
                }
            }
        }
        
    }
    
    private void appinfoGet()
    {
        if(this.isInProcess() == true)
        {
            Log.d(TAG, "appinfoGet previouse is still in loading, return");
            return;
        }
        if(existSession() == false)
        {
            return;
        } 
        
        begin();            
        Log.d(TAG, "before get appinfo ");
        
        synchronized(mLock)
        {
            inprocess = true;
        }           
        String app_ids = getAppids();
        facebookA.getAppinfoAsync(app_ids, new FacebookAdapter()
        {
            @Override public void getAppinfo(List<Notifications.AppInfo> appinfo)
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }
                addAppinfo(appinfo);
                if(donotcallnetwork == false )//I am still alive
                { 
                     Message msd = handler.obtainMessage(FACEBOOK_NOTIFICATION_UI);
                     msd.sendToTarget();
                     //cancelNotify();
                }       
                
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
    
    protected void addAppinfo(List<AppInfo> appinfo) {
        synchronized(appinfos)
        {
            boolean isExist = false;
            for(int i=0;i<appinfos.size();i++)
            {
                AppInfo oldinfo = appinfos.get(i);
                for(int j=0;j<appinfo.size();j++)
                {
                   AppInfo newinfo = appinfos.get(j);
                   if(oldinfo.app_id == newinfo.app_id)
                   {
                       oldinfo.display_name = newinfo.display_name;
                       oldinfo.canvas_name = newinfo.canvas_name;
                       newinfo = null;
                       appinfo.remove(j);
                       break;
                   }
                }
            }
            //if(appinfo.size()>0) appinfo.get(0).canvas_name = "nihao";
            appinfos.addAll(appinfo);
        }
    }

    private String getAppids() {
        if(notifications!=null)
        {
            String idstr="";
            List<Long> appids = new ArrayList<Long>();
            for(int i=0;i<notifications.size();i++)
            {
                Notifications.Notification notification = notifications.get(i);
                if(isExistInAppinfo(notification.app_id) == false)
                {
                    if(appids.contains(notification.app_id) == false)
                    {
                        appids.add(notification.app_id);
                        if(idstr.length() > 0)
                        {
                            idstr += "," + notification.app_id;
                        }
                        else
                        {
                            idstr += notification.app_id;
                        }
                    }
                }                
            }
            appids.clear();
            appids = null;
            return idstr;
        }
        else
        {
            return "";
        }
       
       
    }

    private boolean isExistInAppinfo(long app_id) {
        if(appinfos == null) return false;
        boolean value = false;
        for(int i=0;i<appinfos.size();i++)
        {
            Notifications.AppInfo appinfo = appinfos.get(i);
            if(appinfo.app_id == app_id )
            {
                if(isEmpty(appinfo.display_name)== true && isEmpty(appinfo.canvas_name) == true)
                {
                   // appinfos.remove(i);
                    value = false;
                    break;
                }
                else
                {
                    value = true;
                    break;
                }
            }
        }
        return value;
    }

    private void notesGet()
    {   
        if(this.isInProcess() == true)
        {
            Log.d(TAG, "previouse is still in loading, return");
            return;
        }
        
        if(existSession() == false)
        {
            return;
        }       
        
        begin();            
        Log.d(TAG, "before get notes message");
        //notifyLoading();  
        
        synchronized(mLock)
        {
            inprocess = true;
        }           
        
        facebookA.getNotificationListAsync(-1L, true, new FacebookAdapter()
        {
            @Override public void getNotificationList(Notifications notifications)
            {

                Log.d(TAG, "after get notification ="+notifications.notificationlist.size());
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                addNotes(notifications.notificationlist, notifications.appinfo);
                
                if(donotcallnetwork == false )//I am still alive
                {   
                     Message msg = handler.obtainMessage(FACEBOOK_NOTIFICATION_APPINFO_GET);
                     msg.sendToTarget();
                    
                     Message msd = handler.obtainMessage(FACEBOOK_NOTIFICATION_UI);
                     msd.sendToTarget();
                     //cancelNotify();
                }       
                
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
                if(donotcallnetwork == false )//I am still alive
                {   
                     //cancelNotify();
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
    
    @Override protected void onPause() 
    {
    	super.onPause();
    }

    @Override protected void onDestroy() 
    {    
        serialization();
        
        synchronized(notifications)    
        {
        	for(Notifications.Notification item:notifications)
        	{
        		item.despose();
        		item = null;
        	}
        }
        
        synchronized(notifications)    
        {
        	for(Notifications.AppInfo item:appinfos)
        	{
        		item.despose();
        		item = null;
        	}
        }
        
      	for(int j=0;j<notificationList.getChildCount();j++)    		 
        {
            View v = notificationList.getChildAt(j);
            if(NotesItemView.class.isInstance(v))
            {
            	NotesItemView nv = (NotesItemView)v;
            	nv.dispose();
            }
        }
      	super.onDestroy();
    }
    
    private void markUnread()
    {
    	List<Long> ids = new ArrayList<Long>();
    	if(notifications.size() > 0)
    	{
    	    for(Notifications.Notification item: notifications)
    	    {
    	    	if(item.is_unread == true)
    	    	{
    	    		ids.add(item.notification_id);
    	    	}
    	    }
    	}
    	
    	if(ids.size() == 0)    	
    	{
    		Log.d(TAG, "no need do mark read");
    		return ;
    	}
    	
    	if(existSession() == false)
        {
            return;
        }       
        
        Log.d(TAG, "before mark read");
        long []lids = new long[ids.size()];
        for(int i=0;i<ids.size();i++)
        {
        	lids[i] = ids.get(i);
        }
        
        ids.clear();
        ids = null;
        
        facebookA.notificationMarkReadArrayAsync(lids,  new FacebookAdapter()
        {
            @Override public void notificationMarkRead(boolean  suc, long[] nids)
            {
            	synchronized(notifications)
            	{
	            	if(notifications.size() > 0)
	            	{
	            		for(int i=0;i<nids.length;i++)
	            		{
	            	        for(Notifications.Notification item: notifications)
		            	    {
		            	    	if(item.notification_id == nids[i])
		            	    	{
		            	    		item.is_unread = false;
		            	    	}
	            	        }
	            		}
	            	}
            	}
            	
            	//update UI
            	Message msd = handler.obtainMessage(FACEBOOK_NOTIFICATION_UI);
                msd.sendToTarget();
                
                Log.d(TAG, "after mark read notification ="+suc);
            }
            
            @Override public void onException(FacebookException e, int method, Object[] args) 
            {
                Log.d(TAG, "fail to mark read notifcation="+e.getMessage());
               
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                     
                }
            }
        });
    	
    }
    
	public void markRead(final long nid) 
    {
    	if(existSession() == false)
        {
            return;
        }       
        
        Log.d(TAG, "before mark read");
        facebookA.notificationMarkReadAsync(nid,  new FacebookAdapter()
        {
            @Override public void notificationMarkRead(boolean  suc, long[]nids)
            {
                Log.d(TAG, "after mark read notification ="+suc);                
                Message msd = handler.obtainMessage(FACEBOOK_MARKREAD_END);                
                msd.getData().putBoolean(RESULT, true);
                msd.getData().putLong("nid", nid);
                handler.sendMessage(msd);
            }
            
            @Override public void onException(FacebookException e, int method, Object[] args) 
            {
                Log.d(TAG, "fail to mark read notifcation="+e.getMessage());
               
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                     Message msd = handler.obtainMessage(FACEBOOK_MARKREAD_END);      
                     msd.getData().putBoolean(RESULT, true);
                     msd.getData().putLong("nid", nid);
                     handler.sendMessage(msd);
                }
            }
        });
	}

	protected void addNotes(List<Notifications.Notification> tnotes, List<Notifications.AppInfo> apps) 
    {       
        synchronized(notifications)
        {
            for(int i=0;i<tnotes.size();i++)
            {
                Notifications.Notification item = tnotes.get(i);
                boolean isExist = false;
                for(int j=0;j<notifications.size();j++)
                {
                    Notifications.Notification exist = notifications.get(j);
                    if(item.notification_id == exist.notification_id)
                    {
                        isExist=true;
                        //update the content
                        Notifications.Notification outi = notifications.get(j) ;
                        outi=null;
                        
                        notifications.set(j, item);
                        break;
                    }
                }
                
                if(isExist == false)
                {
                    notifications.add(item);                
                }
            }
            
            java.util.Collections.sort(notifications);
            
            //remove the no use wall            
            while(notifications.size() > viewsize)
            {
                notifications.remove(notifications.size() -1);
            }
        }
        
        
        synchronized(appinfos)
        {
            for(int i=0;i<apps.size();i++)
            {
                Notifications.AppInfo item = apps.get(i);
                boolean isExist = false;
                for(int j=0;j<appinfos.size();j++)
                {
                    Notifications.AppInfo exist = appinfos.get(j);
                    if(item.app_id == exist.app_id)
                    {
                        isExist=true;
                        //update the content
                        Notifications.AppInfo outi = appinfos.get(j) ;
                        outi=null;
                        
                        appinfos.set(j, item);
                        break;
                    }
                }
                
                if(isExist == false)
                {
                	appinfos.add(item);                
                }
            }
            
//            java.util.Collections.sort(appinfos);
//            
//            //remove the no use wall , no need for app, just limited appinfo          
//            while(notifications.size() > viewsize)
//            {
//                notifications.remove(notifications.size() -1);
//            }
        }
    }

    @Override
    protected void createHandler() 
    {
        handler = new NotesHanlder();
    }
    
    /*
    @Override
    public void titleSelected() 
    {   
        super.titleSelected();  
        launchLoadNotification();
    }
    */

 
    @Override
    protected void doAfterLogin()
    {
        Log.d(TAG, "after login");
        //try to get the session
        perm_session = loginHelper.getPermanentSesstion(this);
        if(perm_session == null)
        {
            //re-launch the login UI
            Log.d(TAG, "fail to get permanent session");
            Toast.makeText(this,R.string.facebook_no_valid_session, Toast.LENGTH_SHORT).show();
            setMenu(false);         
            //reLaunchFacebookLogin();
        }
        else
        {
            setMenu(true);
            
            facebookA = new AsyncFacebook(perm_session);
            perm_session.attachActivity(this);
            
            launchLoadNotification();
        }   
    }
    
    private void setMenu(boolean logined)
    {
        if(logined == false)
        {
            notificationList.setOnCreateContextMenuListener(null);
            notificationList.setOnItemClickListener(null);
            if(optionMenu != null)
            {
                optionMenu.setGroupVisible(R.id.facebook_groupd_nemu, false);               
                optionMenu.setGroupVisible(R.id.facebook_groupd_login_nemu, true);
                
                optionMenu.findItem(R.id.facebook_menu_login).setVisible(true);
                optionMenu.findItem(R.id.facebook_menu_settings).setVisible(true);
            }           
        }
        else
        {
            notificationList.setOnCreateContextMenuListener(this);
            notificationList.setOnItemClickListener(listItemClickListener);
            if(optionMenu != null)
            {
                optionMenu.setGroupVisible(R.id.facebook_groupd_nemu, true);
                optionMenu.setGroupVisible(R.id.facebook_groupd_login_nemu, false);
                optionMenu.findItem(R.id.facebook_menu_login).setVisible(false);
            }           
        }
    }
    
    private void notifyLoading() 
    {
        notify.notifyOnce(R.string.facebook_notification_loading, R.drawable.facebook_logo, 30*1000);      
    }
    
    private class NoteAdapter extends BaseAdapter 
    {
        private final String TAG = "NoteAdapter";        
        private Context mContext;
        
        public NoteAdapter(Context con,  List<Notifications.Notification> tnotes)
        {
            mContext = con;
            mNotesItems = tnotes;
        }       
        
        public int getCount() 
        {
            return mNotesItems.size();
        }
        
        public Object getItem(int pos) 
        {       
            return mNotesItems.get(pos);
        }
        
        public long getItemId(int pos) 
        {
            return mNotesItems.get(pos).notification_id;
        }
        
        public View getView(int position, View convertView, ViewGroup arg2) 
        {       
             if (position < 0 || position >= getCount()) 
             {
                 return null;
             }
             
             NotesItemView v=null;
            
             Notifications.Notification di = (Notifications.Notification)getItem(position);
             if (convertView == null /*|| convertView instanceof SeparatorView*/) {
                 v = new NotesItemView(mContext, di);
             } else {
                  v = (NotesItemView) convertView;
                  v.setNoteItem(di);
             }
             
             return v;
        }       
        private List<Notifications.Notification>           mNotesItems;
    }
    
    public class NotesItemView extends SNSItemView {
        private final String TAG="NotesItemView";
        
        private ImageView imageView;
        private TextView publishDate;
        private TextView publishTxt;
        private TextView username;  
        
        private Notifications.Notification  note;
        private Notifications.AppInfo       appinfo;    
        
        String  imageURL;
        Handler handler;        
        
        public Notifications.Notification getNotes()
        {
            return note;
        }       
        
        public NotesItemView(Context ctx, AttributeSet attrs) 
        {
            super(ctx, attrs);
            mContext = ctx;        
            setOrientation(LinearLayout.VERTICAL);
            this.setVisibility(View.VISIBLE);   
            
            handler = new NotesInternalHandler();
        }
        
        public NotesItemView(Context context, Notifications.Notification di) 
        {       
            super(context);
            mContext = context;
            note = di;
            
            Log.d(TAG, "call mail box NotesItemView");
        
            handler = new NotesInternalHandler();
            init();
        }   
        
        
        private void updateUIFromUser()
        {
            handler.obtainMessage(UPDATE_UI).sendToTarget();
        }
        
        final int UPDATE_UI=0;
        final int UPDATE_PAT_UI=1;
        final int UPDATE_IMAGE_UI=2;
        public class NotesInternalHandler extends Handler
        {
            public NotesInternalHandler()
            {
                super();            
                Log.d(TAG, "new NotesInternalHandler");
            }
            
            @Override
            public void handleMessage(Message msg) 
            {
                switch(msg.what)
                {
                    case UPDATE_UI:
                    {          
                        if(appinfo != null)
                        {
                            username.setText(getAppName(appinfo));
                        }
                        break;
                    }
                    case UPDATE_PAT_UI:
                    {
                        String names = msg.getData().getString("usernames");                    
                        username.setText(names);
                        break;
                    }
                    case UPDATE_IMAGE_UI:
                    {
                        String url = msg.getData().getString("imageurl");
                        if(url == null)
                        {
                            imageView.setImageResource(R.drawable.noimage);
                        }                   
                        else
                        {                       
                            ImageRun imagerun = new ImageRun(handler, url, 0); 
                            imagerun.noimage = true;
                            imagerun.use_avatar = true;
                            imagerun.setImageView(imageView);
                            imagerun.post(imagerun);
                        }
                        break;
                    }
                }
            }           
        }
                
        public long getFromUID()
        {
            return note.app_id;
        }
        //get the image from database, 
        //if the user is not exist, will load the user data, and save them into database
        //
        private void setImage()
        {   
            if(imageURL == null)
            {
                long id = getFromUID();
                if(appinfo == null)
                {
                	for(Notifications.AppInfo ai: appinfos)
                	{
                		if(ai.app_id == id)
                		{
                			appinfo = ai;
                			break;
                		}
                	}
                }
                
                boolean getFromDB=false;
                if(appinfo == null)
                {
                    getFromDB = true;
                }
                else
                {
                    Log.d(TAG, "who am I="+appinfo);
                    imageURL = appinfo.icon_url;
                }
                getImageBMP(imageURL, false);                
                
            }
            else//I have get the image
            {
                getImageBMP(imageURL, false);
            }
        }
        
        private void getImageBMP(String url, boolean fromAnotherThread)
        {
            if(fromAnotherThread == true)
            {
                Message msg = handler.obtainMessage(UPDATE_IMAGE_UI);
                msg.getData().putString("imageurl", url);
                handler.sendMessage(msg);
            }
            else//from the same thread
            {
                ImageRun imagerun = new ImageRun(handler, url, 0);
                imagerun.noimage = true;
                imagerun.use_avatar = true;
                imagerun.setImageView(imageView);                
                imagerun.post(imagerun);
            }
        }
        
        protected void updateUIFromUser(List<FacebookUser> users) 
        {
            String uname="";
            for(int i=0;i<users.size();i++)
            {
                FacebookUser user = users.get(i);
                if(user.name != null )
                {
                    if(i>0)
                        uname +=", ";
                    
                    uname += user.name;
                }
            }
            
            if(uname.length() > 0)
            {
                Message msg = handler.obtainMessage(UPDATE_PAT_UI);
                msg.getData().putString("usernames", uname);
                handler.sendMessage(msg);
            }
        }
            
        private void init() 
        {
            Log.d(TAG,  "call NotesItemView init");
            LayoutInflater factory = LayoutInflater.from(mContext);
            removeAllViews();
            
            //container
            FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);
            FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);     
            view.setLayoutParams(paras);
            view.setVerticalScrollBarEnabled(true);
            addView(view);
            
            //child 1
            View v  = factory.inflate(R.layout.facebook_notification_item, null);      
            v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));
            view.addView(v);
            
            imageView  = (ImageView)v.findViewById(R.id.tweet_img_ui);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            imageView.setLayoutParams(params);
            
            publishDate  = (TextView)v.findViewById(R.id.tweet_publish_time);
            publishTxt   = (TextView)v.findViewById(R.id.tweet_publish_text);  
            publishTxt.setMovementMethod(LinkMovementMethod.getInstance());
            publishTxt.setLinksClickable(true);
			//publishTxt.setAutoLinkMask(Linkify.WEB_URLS);
			
            //hint to click here to open in browser
            //publishTxt.setTextColor(R.color.stream_source);
            username     = (TextView)v.findViewById(R.id.tweet_user_name);
            
            username.setSingleLine(true);            
            imageView.setOnClickListener(viewUserDetailsClick);
            
            //this.setOnClickListener(Linkclick);            
            setUI();        
        }

        
        @Override
        protected void onFinishInflate() 
        {   
            super.onFinishInflate();        
            init();
        }
        
        public String getText()
        {
            String content  = "";
            if(isEmpty(note.body_html) == false)
            {
            	if(isEmpty(note.title_html) == false)
                    content = note.title_html + "<br><br>" + note.body_html;
            	else
            		content = note.body_html;
            }
            else
            {
            	content = note.title_html;
            	//if no html content, we use the whole region to do click
            	if(isEmpty(content) == true && isEmpty(note.href) == false)
            	{
            		String data = getRawText();
            		content = String.format("<a href='%1$s'>%2$s</a>", note.href, data);
            		Log.d(TAG, "no html, use the href="+content);
            	}
            }
            
            //see there is a href
            if(content.contains("href=") == false)
            {
            	if(isEmpty(note.href) == false)
            	{
            		String data = getRawText();
            		content = String.format("<a href='%1$s'>%2$s</a>", note.href, data);
            		Log.d(TAG, "no href in content, use the href="+content);
            	}
            }
            
            //remove div
            StringBuffer ret = new StringBuffer(content.length());
            int start=0;
            int tagstart = content.indexOf("<div", 0);
            if(tagstart == -1)
            {
            	return content;
            }
            
            while(tagstart>=0)
            {
            	int end = content.indexOf(">", tagstart+4);
            	ret.append(content.substring(start, tagstart));
            	
            	start = end+1;
            	tagstart = content.indexOf("<div", start);            	
            }
            content.replace("</div>", "");
            return ret.toString();
        }
        
        public String getRawText()
        {
            String content  = "";
            if(isEmpty(note.body_text) == false)
            {
            	if(isEmpty(note.title_text) == false)
                    content = note.title_text + "\n\n" + SNSItemView.removeHTML(note.body_text, true);
            	else            		
            		content =  SNSItemView.removeHTML(note.body_text, true);
            }
            else
            {
            	content = note.title_text;
            }
            
//            StringBuffer ret = new StringBuffer(content.length());
//            int start=0;
//            int tagstart = content.indexOf("<div", 0);
//            if(tagstart == -1)
//            {
//            	return content;
//            }
//            
//            while(tagstart>=0)
//            {
//            	int end = content.indexOf(">", tagstart+4);
//            	ret.append(content.substring(start, tagstart));
//            	
//            	start = end+1;
//            	tagstart = content.indexOf("<div", start);            	
//            }
//            
//            content.replace("</div>", "");
//            
//            return ret.toString();
            
            return content;
                        
        }
        
        Html.TagHandler tagHandler = new Html.TagHandler() 
        {            
            public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) 
            {
                Log.d(TAG, "***** click what="+tag + " output="+output.toString());                
            }
        };
        
        private class MyURLSPan extends URLSpan
        {

            String url;
            public MyURLSPan(Parcel src) {
                super(src);                
            }
            
            public MyURLSPan(String src) {
                super(src);
                url = src;
            }

            @Override
            public String getURL() {                
                return super.getURL();
            }

            @Override
    		public void updateDrawState(TextPaint ds) 
            {			
    			//super.updateDrawState(ds);
            	ds.setColor(ds.linkColor);
                ds.setUnderlineText(true);
    		}
            
            @Override
            public void onClick(View widget) {   
                SpannableString sb = (SpannableString)publishTxt.getText();
                URLSpan[] spans = publishTxt.getUrls();
                    
                int start = sb.getSpanStart(this);
                int end   = sb.getSpanEnd(this);
                String text = sb.subSequence(start, end).toString();
                                                  
                Log.d("MyURLSPan", "click= text="+text + " url="+getURL());
                Uri uri = Uri.parse(getURL());
                process(uri);
            }            
        }
        
        private void setUI()
        {   
        	appinfo = null;
        	username.setText("");
            imageURL = "";
            
        	if(appinfo == null)
            {
             	for(Notifications.AppInfo ai: appinfos)
             	{
             		if(ai.app_id == this.getFromUID())
             		{
             			appinfo = ai;
             			break;
             		}
             	}
            }
                       
            if(appinfo != null)
            {  
                String tmpName = getAppName(appinfo);
                if(isEmpty(tmpName) == true)
                {
                    username.setText("");
                }
                else
                {
                    username.setText(tmpName);
                }
                
                imageURL = appinfo.icon_url;
            }
            
            setImage();
            
            publishDate.setText(DateUtil.converToRelativeTime(mContext, note.updated_time)); 
            try{
	            Spanned sp = Html.fromHtml(URLDecoder.decode(getText()), null, tagHandler);
	            publishTxt.setText(sp);
	            {
	                SpannableString sb = (SpannableString)publishTxt.getText();
	                SpannableString ss = new SpannableString(getRawText());                
	                URLSpan[] spans = publishTxt.getUrls();
	                Log.i(TAG, "myurlspan size:"+spans.length);
	                for (int i = 0; i < spans.length; i++) {
	                    int start = sb.getSpanStart(spans[i]);
	                    int end   = sb.getSpanEnd(spans[i]);
	                    String text = sb.subSequence(start, end).toString();
	                    
	                    MyURLSPan my = new MyURLSPan(spans[i].getURL());
	                    ss.setSpan(my, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	                    my = null;
	                    //Log.i(TAG,"---------------ss:"+ss.toString());
	                }
	                publishTxt.setText(ss);
	                ss = null;
	                sb = null;
	            }
            }catch(Exception ne)
            {
            	publishTxt.setText(getText());
            }
                       
            
            if(note.is_unread == true)
            {
            	Log.d(TAG, "i am not read, set backgroud color");
            	Resources res = getResources();
                Drawable mCacheSym = res.getDrawable(R.color.unread);                
            	this.setBackgroundDrawable(mCacheSym);
            	//publishTxt.setTextColor(Color.parseColor("#fffbe2"));
            }
            else
            {
                this.setBackgroundDrawable(null);
            	//publishTxt.setTextColor(Color.parseColor("#ffffff"));
            }
        }
        
        private String getAppName(AppInfo appinfo) {
            if(isEmpty(appinfo.display_name) == true)
            {
                return appinfo.canvas_name;
            }
            else
            {
                return appinfo.display_name;
            }
        }

        private void dispose(){
        	publishTxt.setText(null);
        	imageView.setImageBitmap(null);
            publishDate.setText(null);
            username.setText(null);  
        }
        
        public void setNoteItem(Notifications.Notification di) 
        {
            note = di;      
            //need reget the image
            imageURL = null;
            setUI();
        }
        HashMap<String, String>maps = new HashMap<String, String>();
        public ArrayList<String> extractUris(URLSpan[] spans) {
            int size = spans.length;
            maps.clear();
            ArrayList<String> accumulator = new ArrayList<String>();
            SpannableString sb = (SpannableString)publishTxt.getText();

            for (int i = 0; i < size; i++) {
                accumulator.add(spans[i].getURL());
                
                int start = sb.getSpanStart(spans[i]);
                int end   = sb.getSpanEnd(spans[i]);
                String text = sb.subSequence(start, end).toString();
                
                maps.put(spans[i].getURL(), text);
            }
            return accumulator;
        }
        
        View.OnClickListener  Linkclick = new View.OnClickListener()
        {
        	public void onClick(View v) 
			{								
			   Log.d(TAG, "Linkclick ");				   
			   
			   URLSpan[] spans = publishTxt.getUrls();

		        if (spans.length == 0) {
		            // Do nothing.
		        }
		        else if (spans.length == 1) {
		            Uri uri = Uri.parse(spans[0].getURL());		            
		            process(uri);
		        } 
		        else 
		        {
		            final java.util.ArrayList<String> urls = extractUris(spans);

		            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.select_dialog_item, urls) 
		            {
		            	public View getView(int position, View convertView, ViewGroup parent) {
		                    View v = super.getView(position, convertView, parent);
		                    try {
		                        String url = getItem(position).toString();
		                        TextView tv = (TextView) v;
		                        Drawable d = null;
		                        if(isProfile(url))
		                        {
		                        	d = mContext.getResources().getDrawable(R.drawable.ic_profile_titlebar);
		                        }
		                        else if(isPhoto(url))
		                        {
		                        	d = mContext.getResources().getDrawable(R.drawable.ic_photos_titlebar);
		                        }
		                        else
		                        {
		                        	d = mContext.getPackageManager().getActivityIcon(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		                        }
		                        if (d != null) {
		                            d.setBounds(0, 0, d.getIntrinsicHeight(), d.getIntrinsicHeight());
		                            tv.setCompoundDrawablePadding(10);
		                            tv.setCompoundDrawables(d, null, null, null);
		                        }
		                        final String telPrefix = "tel:";
		                        if (url.startsWith(telPrefix)) {
		                            url = PhoneNumberUtils.formatNumber(url.substring(telPrefix.length()));
		                        }
		                        tv.setText(maps.get(url));
		                    } catch (android.content.pm.PackageManager.NameNotFoundException ex) {
		                        ;
		                    }
		                    return v;
		                }
		            };

		            AlertDialog.Builder b = new AlertDialog.Builder(mContext);

		            DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
		                public final void onClick(DialogInterface dialog, int which) {
		                    if (which >= 0) {
		                        Uri uri = Uri.parse(urls.get(which));
		                        process(uri);
		                    }
		                }
		            };

		            b.setTitle(R.string.select_link_title);
		            b.setCancelable(true);
		            b.setAdapter(adapter, click);

		            b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		                public final void onClick(DialogInterface dialog, int which) {
		                    dialog.dismiss();
		                }
		            });

		            b.show();
		        }
			}
        	
        };        
        View.OnClickListener viewUserDetailsClick = new View.OnClickListener()
    	{
    		public void onClick(View v) 
    		{
    		    Log.d(TAG, "viewUserDetailsClick you click first one=");
    		    //mark read
    		    
    		}
    	};        
    	
    }    
    public boolean isProfile(String url)
	{
    	Uri uri = Uri.parse(url);
    	String path = uri.getPath();
    	Log.d(TAG, "isProfile url="+url +" path="+path);
    	if(isEmpty(path) == false)
    	{
    	    if(path.equals("/profile.php"))
    	    {
    	    	return true;
    	    }
    	}
		return false;
	}
	
	public boolean isPhoto(String url)
	{
		Uri uri = Uri.parse(url);
    	String path = uri.getPath();
    	
    	Log.d(TAG, "isPhoto url="+url +" path="+path);
    	if(isEmpty(path) == false)
    	{
    	    if(path.equals("/album.php") || path.equals("/photo.php"))
    	    {
    	    	return true;
    	    }
    	}
		return false;
	}
   
	public void setTitle() 
	{
		title = getString(R.string.menu_title_notifications);		
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {
            Log.d(TAG, "KEYCODE_BACK coming=" + this);
            stopLoading();
            restoreTitle();            
            
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
