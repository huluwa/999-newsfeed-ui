package com.msocial.nofree.ui;

import com.msocial.nofree.R;
import com.msocial.nofree.providers.SocialORM;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class FacebookViewSettingPreference extends PreferenceActivity 
implements Preference.OnPreferenceChangeListener 
{
	final String TAG ="FacebookViewSettingPreference";
	private SocialORM orm;
	
	public boolean onPreferenceChange(Preference pref, Object value) {
		String key = pref.getKey();
		if(key.equals("key_facebook_stream_timeout"))
		{
			String uidd = (String)value;
			int timeout = 120;
			try{
				timeout = Integer.parseInt(uidd);
				if(timeout < 120)
				{
					//prompt error dialog
					Toast.makeText(FacebookViewSettingPreference.this,R.string.pref_news_feed_teimout_value, Toast.LENGTH_SHORT).show();
                    return false;
				}	
				
				if(uidd !=null && uidd.trim().length() > 0)
	            {
	                int nsset = orm.setFacebookStreamTimeout(timeout);
	                ((EditTextPreference)(pref)).setText(String.format("%1$s", nsset));
	                pref.setSummary(String.format("%1$s", nsset));               
	            }
			}catch(NumberFormatException ne){
			    Toast.makeText(this.getApplicationContext(),R.string.sns_setting_numeric_checking, Toast.LENGTH_SHORT).show();
			}            
		}
		else if(key.equals("key_facebook_friend_update_period"))
		{
		    String uidd = (String)value;
            if(uidd !=null && uidd.trim().length() > 0)
            {
                int tempval = 3;
                try
                {
                    tempval = Integer.parseInt(uidd.trim());
                    int oldval = orm.getFacebookFriendUpdatePeriod();
                    int nsset = orm.setFacebookFriendUpdatePeriod(tempval);
                    if(nsset<=0)
                    {
                        nsset = 3;
                        orm.setFacebookFriendUpdatePeriod(3);
                    }
                    
                    ((EditTextPreference)(pref)).setText(String.format("%1$s", nsset));
                    pref.setSummary(String.format("%1$s", nsset));
                    
                    //reset ContactService alarm to new period
                    if(tempval != oldval)
                    {
                        Intent i = new Intent();
                        i.setClassName("com.msocial.nofree", "com.msocial.nofree.service.SNSService");
                        i.setAction("com.msocial.nofree.intent.action.FACEBOOK_USER");
                        this.startService(i);
                    }
                    
                }catch(NumberFormatException ne){
                    Toast.makeText(this.getApplicationContext(),R.string.sns_setting_numeric_checking, Toast.LENGTH_SHORT).show();
                }               
            }
		}
		else if(key.equals("key_facebook_contact_update_period"))
		{
			String uidd = (String)value;
            if(uidd !=null && uidd.trim().length() > 0)
            {
            	int tempval = 3;
            	try
            	{
            		tempval = Integer.parseInt(uidd.trim());
            		int oldval = orm.getFacebookContactUpdatePeriod();
                    int nsset = orm.setFacebookContactUpdatePeriod(tempval);
                    if(nsset<=0)
                    {
                        nsset = 3;
                        orm.setFacebookContactUpdatePeriod(3);
                    }
                    
                    ((EditTextPreference)(pref)).setText(String.format("%1$s", nsset));
                    pref.setSummary(String.format("%1$s", nsset));
                    
                    //reset ContactService alarm to new period
                    if(tempval != oldval)
                    {
                        Intent i = new Intent();
                        i.setClassName("com.msocial.nofree", "com.msocial.nofree.service.SNSService");
                        i.setAction("com.msocial.nofree.intent.action.reset.FACEBOOK_PHONEBOOK");
                        this.startService(i);
                    }
                    
            	}catch(NumberFormatException ne){
            	    Toast.makeText(this.getApplicationContext(),R.string.sns_setting_numeric_checking, Toast.LENGTH_SHORT).show();
            	}               
            }
		}
		else if(key.equals("key_facebook_phonebook_sync_period"))
		{
			String uidd = (String)value;
            if(uidd !=null && uidd.trim().length() > 0)
            {
            	int tempval = 7;
            	try
            	{
            		tempval = Integer.parseInt(uidd.trim());
                    int nsset = orm.setAddressbookLookupPeriod(tempval);
                   
                    ((EditTextPreference)(pref)).setText(String.format("%1$s", nsset));
                    pref.setSummary(String.format("%1$s", nsset));
                    
                    //reset ContactService alarm to new period
                    Intent i = new Intent();
                    i.setClassName("com.msocial.nofree", "com.msocial.nofree.service.SNSService");
                    i.setAction("com.msocial.nofree.intent.action.reset.FACEBOOK_PHONEBOOK_SYNC");
                    this.startService(i);
                    
            	}catch(NumberFormatException ne){
            	    Toast.makeText(this.getApplicationContext(),R.string.sns_setting_numeric_checking, Toast.LENGTH_SHORT).show();
            	}               
            }
		}
		else if(key.equals("key_facebook_mail_update_period"))
		{
			String uidd = (String)value;	
            if(uidd !=null && uidd.trim().length() > 0)
            {
            	int tempval = 6;
            	try{
            		 tempval = Integer.parseInt(uidd.trim());
            		 int nsset = orm.setFacebookMailUpdatePeriod(tempval);
                     if(nsset<=0)
                     {
                         nsset = 6;
                         orm.setFacebookMailUpdatePeriod(6);
                     }
                     ((EditTextPreference)(pref)).setText(String.format("%1$s", nsset));
                     pref.setSummary(String.format("%1$s", nsset));  
                     
                     //reset MailService alarm to new period
                     Intent i = new Intent();
                     i.setClassName("com.msocial.nofree", "com.msocial.nofree.service.SNSService");
                     i.setAction("com.msocial.nofree.intent.action.reset.MAIL_CHECK");
                     this.startService(i);
            	}catch(NumberFormatException ne){
            	    Toast.makeText(this.getApplicationContext(),R.string.sns_setting_numeric_checking, Toast.LENGTH_SHORT).show();
            	}           
            }
		}
	    else if(key.equals("key_facebook_accounticonsize_list"))
        {
            ListPreference e = ((ListPreference)pref);
            String temp_value = (String)value;
            if(temp_value!=null)
            {
                orm.setFacebookIconSizeSetting(temp_value);
                CharSequence[] entries = e.getEntries();
                int index = Integer.parseInt(temp_value);
                e.setSummary(entries[index]);
            }
           
        }
		
		return false; 
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{       
        super.onCreate(savedInstanceState);
        
        // Load the XML preferences file
        addPreferencesFromResource(R.xml.facebook_display_setting); 
        
//        Resources res = getResources();
//        Drawable mCacheSym = res.getDrawable(R.color.facebook_backgroud);
//        this.getWindow().setBackgroundDrawable(mCacheSym);
        
        orm = SocialORM.instance(this);        
        this.setTitle(R.string.menu_title_settings);
        
        //setting for view
        int timeout = orm.getFacebookStreamTimeout();
        Preference e = findPreference("key_facebook_stream_timeout");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.facebook_stream_timeout_title);
        e.setSummary(String.format("%1$s", timeout));
        ((EditTextPreference)(e)).setText(String.format("%1$s", timeout));	
              
        int contact_update_period = orm.getFacebookContactUpdatePeriod();
        e = findPreference("key_facebook_contact_update_period");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.facebook_contact_update_period_title);
        e.setSummary(String.format("%1$s", contact_update_period));
        ((EditTextPreference)(e)).setText(String.format("%1$s", contact_update_period));
               
        int friend_update_period = orm.getFacebookFriendUpdatePeriod();
        e = findPreference("key_facebook_friend_update_period");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.facebook_friend_update_period_title);
        e.setSummary(String.format("%1$s", friend_update_period));
        ((EditTextPreference)(e)).setText(String.format("%1$s", friend_update_period));
        
        int mail_update_period = orm.getFacebookMailUpdatePeriod();
        e = findPreference("key_facebook_mail_update_period");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.facebook_mail_update_period_title);
        e.setSummary(String.format("%1$s", mail_update_period));
        ((EditTextPreference)(e)).setText(String.format("%1$s", mail_update_period));
        
        
        int phonebook_sync_period = orm.getAddressbookLookupPeriod();
        e = findPreference("key_facebook_phonebook_sync_period");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.facebook_phonebook_sync_period_title);
        e.setSummary(String.format("%1$s",phonebook_sync_period));
        ((EditTextPreference)(e)).setText(String.format("%1$s",phonebook_sync_period));
               
        
        e = findPreference("key_facebook_accounticonsize_list");
        if(e !=null )
        {
            e.setOnPreferenceChangeListener(this);
            if(ListPreference.class.isInstance(e))
            {   
                ListPreference temp_pre = (ListPreference)e;    
                CharSequence[] entries = temp_pre.getEntries();
                CharSequence[] entryvalues = temp_pre.getEntryValues();
                int index = 0;
                String value = temp_pre.getValue();
                if(value!=null)
                {
                    index = Integer.parseInt(value);
                    orm.setFacebookIconSizeSetting(value);
                }
                else
                {
                    temp_pre.setDefaultValue(entryvalues[index]);    
                }
                
                temp_pre.setSummary(entries[index].toString());
        }
            
        }
	}
}
