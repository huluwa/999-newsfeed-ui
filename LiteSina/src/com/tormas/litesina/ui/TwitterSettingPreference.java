package com.tormas.litesina.ui;

import com.tormas.litesina.*;
import com.tormas.litesina.providers.SocialORM;
import twitter4j.AsyncTwitter;
import twitter4j.Twitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserWithStatus;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class TwitterSettingPreference extends PreferenceActivity 
implements Preference.OnPreferenceChangeListener 
{
	private static final int LOAD_TWITTER_VERIFY = 0;
	SocialORM orm;
	private String TAG = "TwitterSettingPreference";
	boolean isRightAccount=true;
	HandlerLoad handler;
	
	String user;
	String password;
	String uid;
	boolean checked  =false;
	String finalTitle = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "entering onCreate twitterChanged = " + SocialORM.twitterChanged);            
	        // Load the XML preferences file
	        addPreferencesFromResource(R.xml.twitter_preference);  
	       
//	        Resources res = getResources();
//	        Drawable mCacheSym = res.getDrawable(R.color.facebook_backgroud);
//	        this.getWindow().setBackgroundDrawable(mCacheSym);
	        
	        orm = new SocialORM(this);	   
	        
	        SocialORM.Account account = orm.getTwitterAccount();
	      
	        Preference pwde = findPreference("key_twitter_pwd");
	        if(pwde != null)
	        {
	            pwde.setOnPreferenceChangeListener(this);
	            pwde.setTitle(R.string.twitter_password_title);
	            if(account.password != null && account.password.length()>0)
	                pwde.setSummary("******");
	            else
	                pwde.setSummary("");
	             
	            EditText editText = ((EditTextPreference)pwde).getEditText();      
	            if (editText != null) {
	                editText.setSingleLine();
	                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
	            }
	            ((EditTextPreference)(pwde)).setText(account.password); 
	            password=account.password;
	            
	        }
	        
	        Preference uide = findPreference("key_twitter_uid");
	        if(uide !=null )
	        {
	            uide.setOnPreferenceChangeListener(this);
	            uide.setTitle(R.string.twitter_userid_title);
	            uide.setSummary(account.screenname);
	            ((EditTextPreference)(uide)).setText(account.screenname);
	            uid=account.screenname;
	            user = account.screenname;
	        }	
	        
	        Preference countPre = findPreference("key_twitter_view_count");
	        if(countPre != null)
	        {
	            countPre.setOnPreferenceChangeListener(this);
	            countPre.setTitle(R.string.twitter_view_count_title);
	            countPre.setSummary(String.format("%1$s", orm.getTweetViewCount()));
	            ((EditTextPreference)(countPre)).setText(String.format("%1$s", orm.getTweetViewCount())); 
	        }
	        
            Preference followPre = findPreference("key_follow_view_count");
            followPre.setOnPreferenceChangeListener(this);
            followPre.setTitle(R.string.twitter_follow_viewcount_title);
            followPre.setSummary(String.format("%1$s", orm.getFollowViewCount()));
            ((EditTextPreference)(followPre)).setText(String.format("%1$s", orm.getFollowViewCount()));
            
            
            Preference tweetPref = findPreference("key_tweet_view_timeout");
            tweetPref.setOnPreferenceChangeListener(this);
            tweetPref.setTitle(R.string.twitter_tweet_view_timeout_title);
            long tweettimeout = orm.getTweetTimeout()/1000;
            tweetPref.setSummary(String.format("%1$s", tweettimeout));
            ((EditTextPreference)(tweetPref)).setText(String.format("%1$s", tweettimeout));
            
            Preference trendPref = findPreference("key_trend_view_timeout");
            trendPref.setOnPreferenceChangeListener(this);
            trendPref.setTitle(R.string.twitter_trend_view_timeout_title);
            long trendtimeout = orm.getTrendsTimeout()/1000;
            trendPref.setSummary(String.format("%1$s", trendtimeout));
            ((EditTextPreference)(trendPref)).setText(String.format("%1$s", trendtimeout));
             
            boolean show_on_homescreen = orm.getTwitterShowOnHomescreen();
            Preference e = findPreference("key_twitter_show_on_homescreen");
            if(e !=null )
            {
                e.setOnPreferenceChangeListener(this);
                e.setTitle(R.string.title_show_on_homescreen);
                e.setSummary(show_on_homescreen?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
                ((CheckBoxPreference)e).setChecked(show_on_homescreen);
            }
            boolean usinghttps = orm.getTwitterUseHttps();
            Preference https = findPreference("key_twitter_use_https_connection");
            https.setOnPreferenceChangeListener(this);
            https.setTitle(R.string.title_use_https_connections);
            https.setSummary(usinghttps?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
            ((CheckBoxPreference)https).setChecked(usinghttps);            
            
            
            boolean uploadphotosize = orm.isTwitterUseOriginalPhoto();
            Preference uploadphoto_pre = findPreference("key_twitter_upload_photo_size");
            uploadphoto_pre.setOnPreferenceChangeListener(this);
            uploadphoto_pre.setTitle(R.string.title_upload_photo_size);
            uploadphoto_pre.setSummary(uploadphotosize?R.string.sns_uploadphoto_original_summary:R.string.sns_uploadphoto_compressed_summary);
            ((CheckBoxPreference)uploadphoto_pre).setChecked(uploadphotosize);
            
            boolean loadphoto = orm.isTwitterLoadAutoPhoto();
            Preference load_pic_pre = findPreference("key_twitter_load_photo_auto");
            load_pic_pre.setOnPreferenceChangeListener(this);
            load_pic_pre.setTitle(R.string.title_load_photo);
            load_pic_pre.setSummary(loadphoto?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
            ((CheckBoxPreference)load_pic_pre).setChecked(loadphoto);
            
            
            
	        handler = new HandlerLoad();
	        if(account.screenname != null)
	        {
	        	isRightAccount = true;
	        }
	        
	        boolean foraccount = this.getIntent().getBooleanExtra("foraccount", false);
	        if(foraccount)//just show the specific setting
	        {	
	            if(countPre != null)
	            {
	                this.getPreferenceScreen().removePreference(countPre);
	            }
	        }
	        else
	        {
	        	//this.getPreferenceScreen().removePreference(accounte);
	        	//this.getPreferenceScreen().removePreference(pwde);
	        	//this.getPreferenceScreen().removePreference(uide);
	        }
	        
	        if(SocialORM.twitterChanged == false)
	        {
	            finalTitle = getString(R.string.menu_title_settings);
	        }
	        else
	        {
	            finalTitle = getString(R.string.twitter_verify_account_title);
	        }        
	        setTitle();
	}
	
	public void onDestory()
	{
	    Log.d(TAG,"entering onDestroy");
	    super.onDestroy();
	   
	    SocialORM.twitterChanged = false;
	}
	
	void setVerifyAccount()
	{
		if(SocialORM.twitterChanged == true)
		{   
		    finalTitle = getString(R.string.twitter_verify_account_title);		
		}
		setTitle();
	}
	
	private void setTitle()
	{
	    setTitle(finalTitle);
	}
	
	
    public void setTitle(String  title) {
        // TODO Auto-generated method stub
        super.setTitle(title);
    }
    
    
    
  /*  @Override
	public void titleSelected() {
		// TODO Auto-generated method stub
		super.titleSelected();
		
		if(SocialORM.twitterChanged==true)
        {
        	checkAccountInBackground(true);      
        }		
	}
    */

    public boolean onPreferenceChange(Preference pref, Object value) {
		if (pref.getKey().equals("key_twitter_accout")) 
	    {          
			String account = (String)value;
			if(account !=null && account.trim().length() > 0)
	        orm.updateTwitterUsername(account.trim());
			user = account.trim();
			
			((EditTextPreference)(pref)).setText(account);
			pref.setSummary(account);
			SocialORM.twitterChanged = true;
			setVerifyAccount();
	    }
		else if (pref.getKey().equals("key_twitter_pwd"))
		{
			String pwd = (String)value;
			if(pwd !=null && pwd.trim().length() > 0)
			{
	         orm.updateTwitterPwd(pwd.trim());
	         pref.setSummary("******");   
			}
			else
			{
			  orm.updateTwitterPwd(pwd.trim());
			  pref.setSummary("");
			}
			((EditTextPreference)(pref)).setText(pwd.trim());	
			password = pwd.trim();
			SocialORM.twitterChanged = true;
			setVerifyAccount();
		}
		else if (pref.getKey().equals("key_twitter_uid"))
		{
			String uidd = (String)value;
			//if(uidd !=null && uidd.trim().length() > 0)
	        orm.updateTwitterUID(uidd.trim());
			
			((EditTextPreference)(pref)).setText(uidd.trim());
			pref.setSummary(uidd.trim());
			
			orm.updateTwitterUsername(uidd.trim());
			user = uidd.trim();
			
			uid = uidd.trim();
			SocialORM.twitterChanged=true;
			setVerifyAccount();
			
		}
		else if (pref.getKey().equals("key_twitter_view_count"))
        {
            String uidd = (String)value;
            if(uidd == null || uidd.trim().length()<=0)
            {   
            	Toast.makeText(TwitterSettingPreference.this, R.string.sns_setting_numeric_checking, Toast.LENGTH_SHORT).show();
            	return false;
            	
            }
            else 
            {
            	 int nsset = 0;
            	 try{
                     nsset = Integer.parseInt(uidd);
                 }
                 catch(Exception e)
                 {
                     Toast.makeText(TwitterSettingPreference.this.getApplicationContext(), R.string.sns_setting_numeric_checking, Toast.LENGTH_SHORT).show();
                     return false;
                 }
            	if(nsset>20 || nsset<=0)
                {
            		Toast.makeText(TwitterSettingPreference.this, R.string.pref_twitter_count_value, Toast.LENGTH_SHORT).show();
                	return false;
                }
            }
            if(uidd != null && uidd.trim().length() > 0)
            {
                int nsset = orm.setTweetViewCount(uidd.trim());
                ((EditTextPreference)(pref)).setText(String.format("%1$s", nsset));
                pref.setSummary(String.format("%1$s", nsset));               
            }
        }
		else if (pref.getKey().equals("key_follow_view_count"))
        {
            String uidd = (String)value;
            
            if(uidd == null || uidd.trim().length()<=0)
            {   
            	Toast.makeText(TwitterSettingPreference.this, R.string.sns_setting_numeric_checking, Toast.LENGTH_SHORT).show();
            	return false;
            	
            }
            else 
            {
            	int nsset =0;
                try{
                    nsset = Integer.parseInt(uidd);;
                }
                catch(Exception e)
                {
                    Toast.makeText(TwitterSettingPreference.this.getApplicationContext(), R.string.sns_setting_numeric_checking, Toast.LENGTH_SHORT).show();
                    return false;
                }
            	if(nsset>100 )
                {
            		Toast.makeText(TwitterSettingPreference.this, R.string.pref_twitter_count_value, Toast.LENGTH_SHORT).show();
                	return false;
                }
            	else if(nsset<=0)
            	{
            	    Toast.makeText(TwitterSettingPreference.this, R.string.sns_setting_numeric_checking, Toast.LENGTH_SHORT).show();
            		return false;
            	}
            	
            }
            if(uidd !=null && uidd.trim().length() > 0)
            {
                int nsset = orm.setFollowViewCount(uidd.trim());
                ((EditTextPreference)(pref)).setText(String.format("%1$s", nsset));
                pref.setSummary(String.format("%1$s", nsset));               
            }
        }
		else if(pref.getKey().equals("key_tweet_view_timeout"))
		{
			 String timeout = (String)value;
			 if(timeout == null || timeout.trim().length()<=0)
            {   
            	Toast.makeText(TwitterSettingPreference.this, R.string.pref_twitter_timeout_value, Toast.LENGTH_SHORT).show();
            	return false;
            	
            }
            else 
            {
            	int nsset =0;
            	try{
            	    nsset = Integer.parseInt(timeout);
            	}
            	catch(Exception e)
            	{
            	    Toast.makeText(TwitterSettingPreference.this.getApplicationContext(), R.string.sns_setting_numeric_checking, Toast.LENGTH_SHORT).show();
            	    return false;
            	}
            	
            	if(nsset<60)
                {
            		Toast.makeText(TwitterSettingPreference.this, R.string.pref_twitter_timeout_value, Toast.LENGTH_SHORT).show();
                	return false;
                }
            }
            if(timeout !=null && timeout.trim().length() > 0)
            {
                int nsset = orm.setTweetsViewTimeout(timeout.trim());
                ((EditTextPreference)(pref)).setText(String.format("%1$s", nsset));
                pref.setSummary(String.format("%1$s", nsset));               
            }
		}
		else if(pref.getKey().equals("key_trend_view_timeout"))
		{
			String timeout = (String)value;
			 if(timeout == null || timeout.trim().length()<=0)
            {   
            	Toast.makeText(TwitterSettingPreference.this, R.string.pref_twitter_timeout_value, Toast.LENGTH_SHORT).show();
            	return false;
            	
            }
            else 
            {
            	int nsset = 0;
            	try{
                    nsset = Integer.parseInt(timeout);
                }
                catch(Exception e)
                {
                    Toast.makeText(TwitterSettingPreference.this.getApplicationContext(), R.string.sns_setting_numeric_checking, Toast.LENGTH_SHORT).show();
                    return false;
                }
            	if(nsset<60)
                {
            		Toast.makeText(TwitterSettingPreference.this, R.string.pref_twitter_timeout_value, Toast.LENGTH_SHORT).show();
                	return false;
                }
            }
			 
            if(timeout !=null && timeout.trim().length() > 0)
            {
                int nsset = orm.setTrendsViewTimeout(timeout.trim());
            
                ((EditTextPreference)(pref)).setText(String.format("%1$s", nsset));
                pref.setSummary(String.format("%1$s", nsset));               
            }
		}
		else if(pref.getKey().equals("key_twitter_show_on_homescreen"))
        {
            boolean checked = (Boolean)value;
            orm.setTwitterShowOnHomescreen(checked);
            pref.setSummary(checked==true?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
            ((CheckBoxPreference)(pref)).setChecked(checked);  
        }
		else if( pref.getKey().equals("key_twitter_use_https_connection"))
		{
			boolean checked = (Boolean)value;
            orm.setTwitterUseHttps(checked);
            pref.setSummary(checked==true?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
            ((CheckBoxPreference)(pref)).setChecked(checked);  
		}
		else if( pref.getKey().equals("key_twitter_upload_photo_size"))
		{
		    boolean checked = (Boolean)value;
		    orm.setTwitterUseOriginalPhoto(checked);
		    pref.setSummary(checked==true?R.string.sns_uploadphoto_original_summary:R.string.sns_uploadphoto_compressed_summary);
		    ((CheckBoxPreference)(pref)).setChecked(checked); 
		}
		else if( pref.getKey().equals("key_twitter_load_photo_auto"))
		{
		    boolean checked = (Boolean)value;
		    orm.setTwitterLoadAutoPhoto(checked);
		    pref.setSummary(checked==true?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
		    ((CheckBoxPreference)(pref)).setChecked(checked); 
		}
		
		return false;
	}
	
	private boolean isEmptyAccount()
	{
		return user==null || user.length()==0 || password==null || password.length()==0;
	}
	
	void checkAccountInBackground(boolean showDialog)
	{
	    if(isEmptyAccount())
        {
           isRightAccount = false;    
           Toast.makeText(this, R.string.uncomplete_user_pwd, Toast.LENGTH_SHORT).show();
           return;
        }
	   
		Message msg = handler.obtainMessage(GET_USERID);
		msg.getData().putBoolean("showdialog", showDialog);
	    handler.sendMessageDelayed(msg, 1000);     
	}
	
    private final int GET_USERID  =1;
    private final int GET_USERID_END  =2;
    
 	private class HandlerLoad extends Handler 
 	{
 		public HandlerLoad()
 		{
 			super();
 			
 			Log.d(TAG, "new HandlerLoad");
 		}
 		
 		@Override
 		public void handleMessage(Message msg)  
 		{
 			switch(msg.what)
 			{
 			    case GET_USERID:
 			    { 	
 			    	boolean showdialog = msg.getData().getBoolean("showdialog"); 			    	
 			    	verifyUser(showdialog);
 			    	break;
 			    }
 			   case GET_USERID_END:
 			   {
 				   boolean res = msg.getData().getBoolean("result", false);
 				   boolean showdialog = msg.getData().getBoolean("showdialog",false);
 				   if(showdialog == true)
				   {
					   TwitterSettingPreference.this.dismissDialog(LOAD_TWITTER_VERIFY);
				   }
 				  
 				   if(res == false)
 				   {
 				       Toast.makeText(TwitterSettingPreference.this, R.string.wrong_username_pwd, Toast.LENGTH_SHORT).show();
 				   }
 				   else
 				   {
 					   //Toast.makeText(TwitterSettingPreference.this, R.string.right_username_pwd, Toast.LENGTH_SHORT).show();
 					   if(showdialog == true)//back key already processed the result
 					   {
                           setResult(100);
                           finish();
 					   } 					  
 				   } 				  
 				   break;
 			   } 			  
 			}
 		}
 	}
	
 	private boolean aminprocess;
 	private void verifyUser(final boolean showdialog)
 	{
 		if(aminprocess == true)
 		{
 			Log.d(TAG, "i am check account, just return");
 			return;
 		}
 		
 		if(showdialog)
	    {
	    	TwitterSettingPreference.this.showDialog(LOAD_TWITTER_VERIFY);
	    }                   
    
 		aminprocess = true;
    	/*AsyncTwitter tw = new AsyncTwitter(user, password);
    	tw.setUseHttps(orm.getTwitterUseHttps());
    	tw.setUserId(uid);
		tw.getUserVerifyAsync(new TwitterAdapter()
		{
			@Override public void gotUserDetail(User user )
		    {
				if(user != null)
				{
					isRightAccount = true;
					orm.updateTwitterUID(user.getScreenName());
				}
				else
				{
					isRightAccount = false;
				}
				
				aminprocess = false;
				Message msd = handler.obtainMessage(GET_USERID_END);
				msd.getData().putBoolean("result", isRightAccount);
				msd.getData().putBoolean("showdialog", showdialog);
				handler.sendMessage(msd);							          
		    }

		    @Override public void onException(TwitterException e, int method) 
		    {	            	
		    	Log.d(TAG, "Fail to get ="+e.getMessage());
		    	isRightAccount = false;	
		    	
		    	aminprocess = false;
		    	
		    	Message msd = handler.obtainMessage(GET_USERID_END);
				msd.getData().putBoolean("result", false);
				msd.getData().putString("errormsg", e.getMessage());
				msd.getData().putBoolean("showdialog", showdialog);
				handler.sendMessage(msd);
		    }			
		});*/
 	}
	//check the setting
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        /*if (keyCode == KeyEvent.KEYCODE_BACK) 
        {          
            Log.d(TAG, "KEYCODE_BACK is comming"); 
            SocialORM.twitterChanged = true;
        	this.setResult(-1);
        	this.finish();
        	return true;
           
        }        
        else
        {*/
        	return super.onKeyDown(keyCode, event);        
        //}  
    }
	
	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case LOAD_TWITTER_VERIFY: 
            {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.twitter_verify_account_title);
                dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
                dialog.setCanceledOnTouchOutside(true);                
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
            }
        }
        
        return null;
    }
}

