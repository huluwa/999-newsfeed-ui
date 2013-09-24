package com.msocial.facebook.service;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.Contacts;
import android.util.Log;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.service.dell.ContactHelper;
import com.msocial.facebook.ui.AccountListener;
import com.msocial.facebook.ui.TwitterHelper;
import oms.sns.service.facebook.model.FacebookUser;

public class CoherenceCheck implements  AccountListener{
	final static String TAG = "data-coherence";
	private SNSService          mContext;
	private FacebookLoginHelper loginHelper;
	private SocialORM           orm;
		
	public CoherenceCheck(SNSService con, SocialORM orm, FacebookLoginHelper loginHelper)
	{
		Log.d(TAG, "create CoherenceCheck");		
		mContext = con;
		this.loginHelper = loginHelper;
		this.orm         = SocialORM.instance(con);		
		
		registerAccountListener();
	}

	public void Start()
	{
		Log.d(TAG, "Start..........");		
		//do check in background
		long datachecktime = orm.getLastDataCheckTime();
		long now = System.currentTimeMillis();
		
		//don't do at first time, it is to waste time, 
		//for test, do it every time
		//datachecktime = 1;		
		if(datachecktime == 0)
		{
			orm.setLastDataCheckTime(now);
			datachecktime = now;
		}
		
		if(now - datachecktime > 30*24*60*60*1000L)
		{
			new DataCheckTask(mContext, orm).execute((Void[])null);
		}		
	}
	
	public static String[] baseProjection = new String[] 
	{
         android.provider.BaseColumns._ID,         
    };
	
	protected static class DataCheckTask extends android.os.AsyncTask<Void, Void, Void>
    {       
        public DataCheckTask(Context con, SocialORM orm)
        {
            super();
            this.orm = orm;       
            this.con = con.getApplicationContext();
            Log.d(TAG, "data check");
        }
       
        Context con;
        SocialORM orm;
        
        //get Facebook id from Contact extension table
        private long getFacebookID(int pid)
        {
        	return ContactHelper.getFacebookIDByPid(con, pid);        	
        }
        
        private boolean existInFriendship(long uid, List<Long>uids)
        {
        	return uids.contains(uid);        	
        }
        
        private void removeFacebookData(int pid, long uid)
        {
        	ContactHelper.removeFacebookDataByPid(con, pid, uid);
        	
        	//remove Facebook user picture and other
        	removeFacebookCacheData(uid);
        }
        
        public void removeFacebookCacheData(long uid) 
        {
        	FacebookUser.SimpleFBUser suser = orm.getSimpleFacebookUser(uid);
        	if(suser != null)
        	{
        		String filePath = TwitterHelper.getImagePathFromURL_noFetch(suser.pic_square);
        		if(new File(filePath).exists() == true)
        		{
        			Log.d(TAG, "delete unfriend pic="+filePath);
        			new File(filePath).delete();
        			orm.removeFacebookUser(uid);
        		}
        	}
    	}
        
        @Override
        protected Void doInBackground(Void ...pbs) 
        {
        	Log.d(TAG, "do data coherence checking");
        	orm.setLastDataCheckTime(System.currentTimeMillis());
        	
        	List<Long>uids = orm.getFriendIDs();
        	java.util.Collections.sort(uids);
        	
        	//get all people and remove the people facebook data, if she/he is not in our friendship table
        	
        	Cursor cursor = con.getContentResolver().query(Contacts.People.CONTENT_URI, baseProjection, null, null, null);
        	if(cursor != null && cursor.moveToFirst())
        	{
        		while(cursor.moveToNext())
        		{
        			int pid = cursor.getInt(cursor.getColumnIndex(android.provider.BaseColumns._ID));
        			long fid = getFacebookID(pid);
        			if(fid > 0)
        			{
        				if(existInFriendship(fid, uids) == false)
        				{
        					removeFacebookData(pid, fid);        					
        				}  
        				else
        				{
        					Log.d(TAG, "fid is friends to me="+fid);
        				}
        			}
        		}
        	}
        	
        	Log.d(TAG, "finish do data coherence checking");
			return null;
        }        
    }
	
	public void Stop()
	{
		Log.d(TAG, "Stop..........");
	}

	public void onLogin() 
	{
		Log.d(TAG, "onLogin");
		//this.Start();
	}

	public void onLogout() 
	{
		Log.d(TAG, "onLogout");		
	}

	public void registerAccountListener() 
    {
        AccountManager.registerAccountListener(this.getClass().getName(), this);     
    }
    public void unregisterAccountListener() 
    {
        AccountManager.unregisterAccountListener(this.getClass().getName());     
    }

}
