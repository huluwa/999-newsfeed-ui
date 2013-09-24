package com.ast.free.service;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import oms.sns.service.facebook.client.FacebookClientException;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.Desktop.DesktopApplication;
import com.ast.free.providers.SocialORM;
import com.ast.free.providers.SocialORM.Account;
import com.ast.free.ui.FacebookBaseActivity;
import oms.sns.service.facebook.client.FacebookSession;

public class FacebookLoginHelper {
	
	private final String TAG = "FacebookLoginHelper";
//  
	public static String API_KEY    = "";
    public static String SECRET_KEY = "";
    
    
    //make it not easy to de-compile
    static 
    {
        API_KEY    = "882a8490361da98702bf97a021ddc14d";
        SECRET_KEY = "62f8ce9f74b12f84c123cc23437a4a32";
    }
    
    public final static String DB_SESSION_KEY       ="facebook_session_key";
    public final static String DB_PERM_SESSION_KEY  ="facebook_perm_session_key";
    public final static String DB_SECRET_KEY        ="facebook_secret_key";
    public final static String DB_PERM_SECRET_KEY   ="facebook_perm_secret_key";
    public final static String DB_LOGGED_USER_ID    ="facebook_logged_user_id";
    
    private static FacebookSession perm_session;
    private static FacebookSession period_session;
    private static FacebookLoginHelper _instance;
    private static DesktopApplication da;
    
    private static Context   mContext;
	private static SocialORM orm;

	Map<FacebookBaseActivity, FacebookSession>sessionMap = new HashMap<FacebookBaseActivity, FacebookSession>();
	
    public static FacebookLoginHelper instance(Context con)
    {
    	if(_instance == null)
    	{
    		_instance = new FacebookLoginHelper(con.getApplicationContext());
    	}
    	
    	//re-assign the context
    	mContext = null;
    	mContext = con.getApplicationContext();
    	
    	orm=null;
		orm = SocialORM.instance(mContext.getApplicationContext());
		
    	return _instance;
    }
    
	private FacebookLoginHelper(Context con) 
	{
		mContext = con.getApplicationContext();
		orm = SocialORM.instance(mContext.getApplicationContext());
		
		//just create one time
		da = new DesktopApplication(API_KEY, SECRET_KEY);
	}
	public DesktopApplication getDesktopApp()
	{
		return da;
	}
	public FacebookSession getTempSesstion() 
	{	
		return period_session;
	}
	
	public FacebookSession getPermanentSesstion() 
	{	
		if(perm_session == null)
		{
			Log.d(TAG, "permanent session is null");
			return period_session;
		}
		else
		{
		    String db_perm_session = orm.getSettingValue(DB_PERM_SESSION_KEY);
		    if(db_perm_session!=null && false == db_perm_session.equals(perm_session.getSessionKey()))
		    {
		        perm_session   = null;
		        period_session = null;	
		        
		        restoreSesstion();
		    }		 
		}
		return perm_session;		
	}
	
	public void destroy(FacebookBaseActivity base)
	{
		synchronized(sessionMap)
		{
			FacebookSession temp = sessionMap.get(base);
			sessionMap.remove(base);
			
			Log.d(TAG, "remove session from cache="+base);
			if(temp != null)
			{
			    temp.destroy();
                temp = null;
			}
			
		}
	}
	public FacebookSession getPermanentSesstion(FacebookBaseActivity base) 
	{	
		synchronized(sessionMap)
		{
			FacebookSession temp = sessionMap.get(base);
			if(temp == null)
			{
			    FacebookSession se = constructPermSession();
			    if(se != null)
			    {
			        sessionMap.put(base, se);
			    }
			    
			    return se;
			}
			
			Log.d(TAG, "get session from cache="+base);
			return temp;
		}
	}
	
	//for background service, we distinguish them from Activity http task
	public FacebookSession constructPermSession()
	{		
		FacebookSession tmp = null;
		Log.d(TAG, "constructPermSession");        
    	String sessionkey = orm.getSettingValue(DB_PERM_SESSION_KEY);
		if(sessionkey != null)
		{
			Log.d(TAG, "restore permanant session");
		    String secretKey  = orm.getSettingValue(DB_PERM_SECRET_KEY);
		    long userID       = Long.parseLong(orm.getSettingValue(DB_LOGGED_USER_ID));
		    
		    tmp = new FacebookSession(API_KEY, secretKey, sessionkey, userID);
		}
		else//we already have tmp session
		{
			Log.d(TAG, "no permanant session");	
			sessionkey = orm.getSettingValue(DB_SESSION_KEY);
			if(sessionkey != null)
			{
				Log.d(TAG, "construct temp session");
			    String secretKey  = orm.getSettingValue(DB_SECRET_KEY);
			    long userID       = Long.parseLong(orm.getSettingValue(DB_LOGGED_USER_ID));			    
			    tmp = new FacebookSession(API_KEY, secretKey, sessionkey, userID);			    
			}
			else
			{
				Log.d(TAG, "no temp session");
			}
		}		
		return tmp;
	}
	
	public boolean restoreSesstion() 
	{
		boolean ret = false;
		if(perm_session == null)
		{
			Log.d(TAG, "perm_session == null restoreSesstion");
	        ret = false;
	    	String sessionkey = orm.getSettingValue(DB_PERM_SESSION_KEY);
			if(sessionkey != null)
			{
				Log.d(TAG, "restore permanant session");
			    String secretKey  = orm.getSettingValue(DB_PERM_SECRET_KEY);
			    long userID       = Long.parseLong(orm.getSettingValue(DB_LOGGED_USER_ID));
			    
			    perm_session = new FacebookSession(API_KEY, secretKey, sessionkey, userID);
			    ret = true;
			    
			    //restore temp session
			    Log.d(TAG, "restore temp session");
			    sessionkey = orm.getSettingValue(DB_SESSION_KEY);
				if(sessionkey != null)
				{
					Log.d(TAG, "construct peroid session");
				    secretKey  = orm.getSettingValue(DB_SECRET_KEY);				    			    
				    period_session = new FacebookSession(API_KEY, secretKey, sessionkey, userID);			    
				}
			    
			}
			else//we already have tmp session, then make it as permanant
			{
				sessionkey = orm.getSettingValue(DB_SESSION_KEY);
				if(sessionkey != null)
				{
					Log.d(TAG, "restore peroid session");
				    String secretKey  = orm.getSettingValue(DB_SECRET_KEY);
				    long userID       = Long.parseLong(orm.getSettingValue(DB_LOGGED_USER_ID));			    
				    period_session = new FacebookSession(API_KEY, secretKey, sessionkey, userID);
				    try
				    {
				    	Account account = orm.getFacebookAccount();
					    perm_session = da.AuthLogin(period_session.getSessionKey(), period_session.getSecretKey(), account.email, account.password);
					    Log.d(TAG, "after request permanant session");
				        ret = true;
				    }catch(FacebookException ne)
				    {
				    	Log.d(TAG, "restore exception="+ne.getMessage());
				    }
				}
			}
		}
		else
		{
			ret = true;
		}
		return ret;
	}
	
	public String getEmail()
	{
		return orm.getFacebookAccount().email;		
	}
	public String getPwd()
	{
		return orm.getFacebookAccount().password;		
	}
	
	public void clearSesion()
	{
		Log.d(TAG, "remove saved session key");
		orm.removeSetting(DB_SESSION_KEY);
		orm.removeSetting(DB_SECRET_KEY);		
		orm.removeSetting(DB_PERM_SESSION_KEY);
		orm.removeSetting(DB_PERM_SECRET_KEY);
		orm.removeSetting(SocialORM.facebook_pwd);
		
		period_session = null;
		perm_session   = null;
		
		synchronized(sessionMap)
		{
			sessionMap.clear();
		}
		
		//force gc
		System.gc();
	}
	
	public void recordTmpSession(String sessionKey, String secretKet, long uid)
	{
		orm.addSetting(DB_SESSION_KEY,    sessionKey);
		orm.addSetting(DB_SECRET_KEY,     secretKet);
		orm.addSetting(DB_LOGGED_USER_ID, String.format("%1$s", uid));
		
		//construct the facebook session
		period_session = new FacebookSession(API_KEY, secretKet, sessionKey, uid);
		
		Log.d(TAG, "recordTmpSession session="+sessionKey + " secret="+secretKet + " uid="+uid);
	}
	public void recordPermanantSession(String sessionKey, String secretKet, long uid)
	{
		orm.addSetting(DB_PERM_SESSION_KEY,    sessionKey);
		orm.addSetting(DB_PERM_SECRET_KEY,     secretKet);
		orm.addSetting(DB_LOGGED_USER_ID,      String.format("%1$s", uid));
		
		perm_session = new FacebookSession(API_KEY, secretKet, sessionKey, uid);
		Log.d(TAG, "recordPermanantSession session="+sessionKey + " secret="+secretKet + " uid="+uid);
	}

	public String getExtPermURL(String perm) 
	{		
		return da.getExtPermURL(perm);
	}
}
