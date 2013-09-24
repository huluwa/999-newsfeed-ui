package com.msocial.nofree.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.msocial.nofree.R;
import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.providers.SocialProvider;
import com.msocial.nofree.service.FacebookLoginHelper;
import com.msocial.nofree.service.SNSService;
import com.msocial.nofree.service.dell.ContactHelper;

import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookSession;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class FacebookSettingPreference extends PreferenceActivity 
implements Preference.OnPreferenceChangeListener, OnPreferenceClickListener 
{
	
	SocialORM orm;
	private String TAG="FacebookSettingPreference";
    private boolean changed = false;
	private boolean needrelogin;
	private boolean comfromservice;	 
	private boolean forsignin;
	private boolean fromtabview;
	private Handler handler;
	
	private static FacebookSettingPreference facebookreference = null;
    public static void finishPreference()
    {
        if(facebookreference != null)
        {
            facebookreference.finish();
        }
    }
    
    long size = 0;
    private class CalulateSNSImageSize extends android.os.AsyncTask<Void, Void, Void>
    {       
        public CalulateSNSImageSize()
        {
            super();            
            Log.d(TAG, "create CalulateSNSImageSize");
        }

		@Override
		protected Void doInBackground(Void... params)			  
        {        
			size = 0;
			File file = new File(TwitterHelper.getTmpPath());
	        if(file.exists() == true)
	        {
	        	File files[] = file.listFiles();
	        	for(File item:files)
	        	{
	        		size += item.length();
	        	}
	        }           
	        
	        handler.post( new Runnable()
	        {
	        	public void run()
	        	{
	        		Preference e = findPreference("clear_cached_facebook_image_key");        
		            e.setSummary(String.format(getString(R.string.pref_clear_facebook_image_cache_summary) +" (%1$s M)", size/(1024*1024)));
	        	}
	        });
	        
            return null;
        }
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        fromtabview = this.getIntent().getBooleanExtra("fromtabview", false);   
        if(fromtabview == false)
        {
           // this.requestWindowFeature(Window.FEATURE_LEFT_ICON);
            //this.requestWindowFeature(Window.FEATURE_RIGHT_ICON);
            this.requestWindowFeature(Window.FEATURE_PROGRESS);
            this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
            //this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.facebook_title);
        }
        
        super.onCreate(savedInstanceState);   
        
        handler = new Handler();
        //Resources res = getResources();
        //Drawable mCacheSym = res.getDrawable(R.color.facebook_backgroud);
        // this.getWindow().setBackgroundDrawableResource(android.R.color.white);
        facebookreference = this;

        //this.getListView().setBackgroundResource(android.R.color.white);
        // Load the XML preferences file
        addPreferencesFromResource(R.xml.facebook_preference); 
        
        setTitle();
        forsignin = this.getIntent().getBooleanExtra("forsignin", false);
        if(forsignin)
        {
        	this.setTitle(R.string.facebook_setting_sign_in);
        }
        orm = SocialORM.instance(this);
        SocialORM.Account account = orm.getFacebookAccount();
        Preference e = findPreference("key_facebook_accout");
        if(e != null)
        {
	        e.setOnPreferenceChangeListener(this);
	        e.setTitle(R.string.facebook_email_title);
	        e.setSummary(account.email);
	        ((EditTextPreference)(e)).setText(account.email);	
	        EditText emailText = ((EditTextPreference)e).getEditText();
	        //emailText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }
        
        e = findPreference("key_facebook_pwd");
        if(e != null)
        {
	        e.setOnPreferenceChangeListener(this);
	        e.setTitle(R.string.facebook_password_title);
	        if(account.password != null && account.password.length()>0)
	            e.setSummary("******");
	        else
	        	e.setSummary("");
	        
	        EditText editText = ((EditTextPreference)e).getEditText();      
	        if (editText != null) 
	        {
	            editText.setSingleLine();
	            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
	            //editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
	        }
        
                ((EditTextPreference)(e)).setText(account.password);
        }
        
        
        handler.post( new Runnable()
        {
        	public void run()
        	{
                new CalulateSNSImageSize().execute((Void[])null);
        	}
        });
        
        e = findPreference("clear_cached_facebook_image_key");        
        e.setSummary(getString(R.string.pref_clear_facebook_image_cache_summary) +" (retreiving cache size...)");
        
        //if false, 
        boolean enbaledSNS = orm.isSNSEnable();
        e = findPreference("key_facebook_allways_enable");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.facebook_allways_enable_title);
        e.setSummary(getString(enbaledSNS==false?R.string.sns_forbiden_summary:R.string.sns_allow_summary) + " "+getString(R.string.facebook_allways_enable__desc));
	    ((CheckBoxPreference)(e)).setChecked(enbaledSNS);
        
	    
	    boolean enbaledPSession = orm.isUsePermanentSeesion();
        e = findPreference("key_facebook_permanent_session_enable");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.facebook_permanent_session_enable_title);
        e.setSummary(enbaledPSession==false?R.string.sns_forbiden_summary:R.string.sns_allow_summary);
	    ((CheckBoxPreference)(e)).setChecked(enbaledPSession);
	    
	    	    
	    boolean sync_address_book = orm.isEnableSyncPhonebook();
        e = findPreference("key_facebook_sync_address_book");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.sync_address_book);
        e.setSummary(sync_address_book==false?R.string.sns_forbiden_summary:R.string.sns_allow_summary);
        ((CheckBoxPreference)(e)).setChecked(sync_address_book);
        
        boolean allways_prompt_dialog = orm.isAlwaysPromptSyncDialog();
        e = findPreference("key_allways_prompt_dialog");
        e.setOnPreferenceChangeListener(this);
        e.setTitle(R.string.is_allways_prompt_dialog);
        e.setSummary(allways_prompt_dialog==false?R.string.donnot_prompt_dialog:R.string.allow_prompt_dialog);
        ((CheckBoxPreference)(e)).setChecked(allways_prompt_dialog);
        
        
        needrelogin = this.getIntent().getBooleanExtra("needrelogin", false);
        comfromservice = this.getIntent().getBooleanExtra("comfromservice", false);
        
        boolean show_on_homescreen = orm.getFacebookShowOnHomescreen();
        e = findPreference("key_facebook_show_on_homescreen");
        if(e != null)
        {
	        e.setOnPreferenceChangeListener(this);
	        e.setSummary(show_on_homescreen?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
	        ((CheckBoxPreference)e).setChecked(show_on_homescreen);
        }
        
        boolean copytoemail = orm.copytoEmail();
        e = findPreference("key_facebook_copy_to_email");
        if(e!=null)
        {
            e.setOnPreferenceChangeListener(this);
            e.setSummary(copytoemail?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
            ((CheckBoxPreference)e).setChecked(copytoemail);
        }
        
        boolean copytosms = orm.copyNewMessagetoSms();
        e = findPreference("key_facebook_copy_new_msg_to_sms");
        if(e != null)
        {
	        e.setOnPreferenceChangeListener(this);
	        e.setSummary(copytosms?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
	        ((CheckBoxPreference)e).setChecked(copytosms);
        }
        
        
        boolean uploadphotosize = orm.isFacebookUseOriginalPhoto();
        e = findPreference("key_facebook_upload_photo_size");
        e.setOnPreferenceChangeListener(this);
        e.setSummary(uploadphotosize?R.string.sns_uploadphoto_original_summary:R.string.sns_uploadphoto_compressed_summary);
        ((CheckBoxPreference)e).setChecked(uploadphotosize);
                
        boolean usinghttps = orm.getFacebookUseHttps();
        Preference https = findPreference("key_facebook_use_https_connection");
        https.setOnPreferenceChangeListener(this);
        https.setTitle(R.string.title_use_https_connections);
        https.setSummary(usinghttps?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
        ((CheckBoxPreference)https).setChecked(usinghttps);       
        
        
        boolean use_logo = orm.getFacebookUseLogo();
        e = findPreference("key_facebook_show_use_logo");
        e.setOnPreferenceChangeListener(this);
        e.setSummary(R.string.summary_show_use_logo);
        ((CheckBoxPreference)e).setChecked(use_logo);
        
        boolean use_email = orm.getFacebookUseEmail();
        e = findPreference("key_facebook_show_use_email");
        e.setOnPreferenceChangeListener(this);
        e.setSummary(R.string.summary_show_use_email);
        ((CheckBoxPreference)e).setChecked(use_email);
        
        boolean use_phonenumber = orm.getFacebookUsePhonenumber();
        e = findPreference("key_facebook_show_use_phonenumber");
        e.setOnPreferenceChangeListener(this);
        e.setSummary(R.string.summary_show_use_phonenumber);
        ((CheckBoxPreference)e).setChecked(use_phonenumber);
        
        boolean use_birthday = orm.getFacebookUseBirthday();
        e = findPreference("key_facebook_show_use_birthday");
        e.setOnPreferenceChangeListener(this);
        e.setSummary(R.string.summary_show_use_birthday);
        ((CheckBoxPreference)e).setChecked(use_birthday);
        
        boolean sync_birthday_event = orm.getFacebookSyncBirthdayEvent();
        e = findPreference("key_facebook_sync_birthday_event");
        e.setOnPreferenceChangeListener(this);
        e.setSummary(R.string.summary_sync_birthday_event);
        ((CheckBoxPreference)e).setChecked(sync_birthday_event);   
        
        boolean notifyEnable = orm.isNotificationEnable();
        e = findPreference("key_facebook_notification_enable");
        e.setOnPreferenceChangeListener(this);
        e.setSummary(notifyEnable?R.string.sns_active_summary:R.string.sns_inactive_summary);
        ((CheckBoxPreference)e).setChecked(notifyEnable);
        
        String value = orm.getNotificationInterval();
        ListPreference le = (ListPreference)findPreference("key_facebook_notification_list");
        le.setOnPreferenceChangeListener(this);
        le.setValue(value);
        
        CharSequence en[] = le.getEntries();
    	CharSequence cs[] = le.getEntryValues();
    	String entry="";
    	for(int i=0;i<cs.length;i++)
    	{
    		if(((String)value).equals(cs[i].toString()))
    		{
    			entry = en[i].toString();
    		}
    	}
    	le.setSummary(entry);
        
        boolean vibrate_on = orm.getNotificationVibrate();
        e = findPreference("key_facebook_notification_vibrate");
        if(e != null)
        {
            e.setOnPreferenceChangeListener(this);        
            ((CheckBoxPreference)e).setChecked(vibrate_on);
        }
        
        
        boolean led_on = orm.getNotificationLED();
        e = findPreference("key_facebook_notification_led");
        if(e != null)
        {
            e.setOnPreferenceChangeListener(this);        
            ((CheckBoxPreference)e).setChecked(led_on);
        }
        
        //for set, 
        e = findPreference("key_facebook_notification_message_enable");
        if(e != null)
        {
        	 e.setOnPreferenceChangeListener(this);        
             ((CheckBoxPreference)e).setChecked(orm.isNotificationEnable(0));
        }
        e = findPreference("key_facebook_notification_poke_enable");
        if(e != null)
        {
        	 e.setOnPreferenceChangeListener(this);        
             ((CheckBoxPreference)e).setChecked(orm.isNotificationEnable(1));
        }
        e = findPreference("key_facebook_notification_reqeust_enable");
        if(e != null)
        {
        	 e.setOnPreferenceChangeListener(this);        
             ((CheckBoxPreference)e).setChecked(orm.isNotificationEnable(4));
        }
        e = findPreference("key_facebook_notification_event_enable");
        if(e != null)
        {
        	 e.setOnPreferenceChangeListener(this);        
             ((CheckBoxPreference)e).setChecked(orm.isNotificationEnable(2));
        }
        e = findPreference("key_facebook_notification_group_enable");
        if(e != null)
        {
        	 e.setOnPreferenceChangeListener(this);        
             ((CheckBoxPreference)e).setChecked(orm.isNotificationEnable(3));
        }
        
        
        e = findPreference("key_show_title_bar");
        if(e != null)
        {
             e.setOnPreferenceChangeListener(this);        
             ((CheckBoxPreference)e).setChecked(orm.isShowTitleBar());
        }
        
        e = findPreference("reset_default_key");
        if(e != null)
        {
             e.setOnPreferenceChangeListener(this);        
             ((CheckBoxPreference)e).setChecked(false);
        }
        
        e = findPreference("clear_cached_facebook_image_key");
        if(e != null)
        {
             e.setOnPreferenceChangeListener(this);        
             ((CheckBoxPreference)e).setChecked(false);
        }
        
        e = findPreference("clear_fbinfo_key");
        if(e != null)
        {
             e.setOnPreferenceChangeListener(this);        
             ((CheckBoxPreference)e).setChecked(false);
        }
        
        
//        e = findPreference("key_facebook_help_tips");
//        e.setOnPreferenceClickListener(this);        
	}
	
	public void setTitle()
	{
		if(fromtabview)
		{
			setTitle("");
		}
		else
		{
			setTitle(R.string.menu_title_settings);
		}
	}
	
	/*
	@Override
	public void titleSelected() 
	{		
		super.titleSelected();
		
		if(forsignin)
		{		
			 SocialORM.Account account = orm.getFacebookAccount();
			 if(account.email != null && account.email.length() > 0 && account.password!= null && account.password.length()>0)
			 {
		         Intent intent = new Intent(this, FacebookLoginActivity.class);  
		         intent.putExtra("comefromsetting", true);
		     	 startActivity(intent);
		     	 this.finish();
			 }
		}
	}*/
	
	
	public boolean onPreferenceChange(Preference pref, Object value) 
	{
		try{
			String key = pref.getKey(); 
			if (key.equals("key_facebook_accout")) 
		    {          
				String account = (String)value;
				//if(account !=null && account.trim().length() > 0)
		        orm.updateFacebookAccount(account.trim());
				
				((EditTextPreference)(pref)).setText(account);
				pref.setSummary(account);
				changed = true;
		    }
			else if (key.equals("key_facebook_pwd"))
			{
				String pwd = (String)value;
				if(pwd !=null && pwd.trim().length() > 0)
				{
		          orm.updateFacebookPwd(pwd.trim());
		          pref.setSummary("******");
				}
				else
				{
				   orm.updateFacebookPwd(pwd.trim());
				   pref.setSummary("");
				}
				((EditTextPreference)(pref)).setText(pwd);
				
				changed = true;
			}
			else if(key.equals("key_facebook_allways_enable"))
			{
				boolean enable = (Boolean)value;
				orm.setSNSEnable(enable);
				pref.setSummary(getString(enable==false?R.string.sns_forbiden_summary:R.string.sns_allow_summary) + " "+getString(R.string.facebook_allways_enable__desc));
	 	        ((CheckBoxPreference)(pref)).setChecked(enable);			
			}
			else if(key.equals("key_facebook_permanent_session_enable"))
			{
				boolean enable = (Boolean)value;
				orm.setUsePermanentSeesion(enable);
				pref.setSummary(enable==true?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
	 	        ((CheckBoxPreference)(pref)).setChecked(enable);			
			}
			else if(key.equals("key_facebook_sync_address_book"))
			{
			    boolean enable = (Boolean)value;		    
			    SyncAddressBookHelper.processSyncAddressBook(FacebookSettingPreference.this, enable, pref, null);
			}
			else if(key.equals("key_allways_prompt_dialog"))
			{
				boolean enable = (Boolean)value;
				orm.enableAlwaysPromptSyncDialog(enable);			
				pref.setSummary(enable==true?R.string.allow_prompt_dialog:R.string.donnot_prompt_dialog);
	 	        ((CheckBoxPreference)(pref)).setChecked(enable);	
			}		
			else if(key.equals("key_facebook_show_on_homescreen"))
			{
			    boolean checked = (Boolean)value;
	            orm.setFacebookShowOnHomescreen(checked);
	            pref.setSummary(checked?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
	            ((CheckBoxPreference)(pref)).setChecked(checked);  
			}
			else if(key.equals("key_facebook_copy_to_email"))
	        {
	            boolean checked = (Boolean)value;
	            orm.setcopytoEmail(checked);
	            pref.setSummary(checked?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
	            ((CheckBoxPreference)(pref)).setChecked(checked);  
	        }
			else if(key.equals("key_facebook_copy_new_msg_to_sms"))
	        {
	            boolean checked = (Boolean)value;
	            orm.setCopyNewMessagetoSms(checked);
	            pref.setSummary(checked?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
	            ((CheckBoxPreference)(pref)).setChecked(checked);  
	        }
			else if(key.equals("key_facebook_upload_photo_size"))
			{
			    boolean checked = (Boolean)value;
	            orm.setFacebookUseOriginalPhoto(checked);
	            pref.setSummary(checked?R.string.sns_uploadphoto_original_summary:R.string.sns_uploadphoto_compressed_summary);
	            ((CheckBoxPreference)(pref)).setChecked(checked);  
			}
			else if( pref.getKey().equals("key_facebook_use_https_connection"))
			{
				boolean checked = (Boolean)value;
	            orm.setFacebookUseHttps(checked);
	            pref.setSummary(checked==true?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
	            ((CheckBoxPreference)(pref)).setChecked(checked);
			}		
			else if(key.equals("key_facebook_show_use_logo"))
			{
			    boolean checked = (Boolean)value;
	            orm.setFacebookUseLogo(checked);
	            ((CheckBoxPreference)(pref)).setChecked(checked); 
			}
			else if(key.equals("key_facebook_show_use_email"))
			{
			    boolean checked = (Boolean)value;
	            orm.setFacebookUseEmail(checked);
	            ((CheckBoxPreference)(pref)).setChecked(checked); 
			}
			else if(key.equals("key_facebook_show_use_phonenumber"))
	        {
			    boolean checked = (Boolean)value;
	            orm.setFacebookUsePhonenumber(checked);
	            ((CheckBoxPreference)(pref)).setChecked(checked); 
	        }
	        else if(key.equals("key_facebook_show_use_birthday"))
	        {
	            boolean checked = (Boolean)value;
	            orm.setFacebookUseBirthday(checked);
	            ((CheckBoxPreference)(pref)).setChecked(checked); 
	        }
	        else if(key.equals("key_facebook_sync_birthday_event"))
	        {
	            boolean checked = (Boolean)value;
	            orm.setFacebookSyncBirthdayEvent(checked);
	            ((CheckBoxPreference)(pref)).setChecked(checked);
	        }
	        else if(key.equals("key_facebook_notification_enable"))
	        {
	            boolean checked = (Boolean)value;
	            orm.setNotificationEnable(checked);
	            pref.setSummary(checked?R.string.sns_active_summary:R.string.sns_inactive_summary);
	            ((CheckBoxPreference)(pref)).setChecked(checked);  
	            
	            //let SNSService do start and stop
	            if(SNSService.getSNSService() != null)
	            {
	            	SNSService.getSNSService().setEnableNotification(checked);
	            }
	        }		
	        else if(key.equals("key_facebook_notification_list"))
	        {
	        	orm.setNotificationInterval((String)value);
	        	ListPreference le = (ListPreference)pref;
	        	CharSequence en[] = le.getEntries();
	        	CharSequence cs[] = le.getEntryValues();
	        	String entry="";
	        	for(int i=0;i<cs.length;i++)
	        	{
	        		if(((String)value).equals(cs[i].toString()))
	        		{
	        			entry = en[i].toString();
	        		}
	        	}
	        	pref.setSummary(entry);
	        }
	        else if(key.equals("key_facebook_notification_vibrate"))
	        {
	            boolean checked = (Boolean)value;
	            orm.enableVibrate(checked);
	            ((CheckBoxPreference)(pref)).setChecked(checked);
	        }
	        else if(key.equals("key_facebook_notification_led"))
	        {
	            boolean checked = (Boolean)value;
	            orm.enableLED(checked);
	            ((CheckBoxPreference)(pref)).setChecked(checked);
	        }
	        else if(key.equals("key_facebook_notification_message_enable"))
	        {
	            boolean checked = (Boolean)value;
	            orm.setNotificationEnable(0, checked);
	            ((CheckBoxPreference)(pref)).setChecked(checked);
	        }
	        else if(key.equals("key_facebook_notification_poke_enable"))
	        {
	            boolean checked = (Boolean)value;
	            orm.setNotificationEnable(1,checked);
	            ((CheckBoxPreference)(pref)).setChecked(checked);
	        }
	        else if(key.equals("key_facebook_notification_reqeust_enable"))
	        {
	            boolean checked = (Boolean)value;
	            orm.setNotificationEnable(4,checked);
	            ((CheckBoxPreference)(pref)).setChecked(checked);
	        }
	        else if(key.equals("key_facebook_notification_event_enable"))
	        {
	            boolean checked = (Boolean)value;
	            orm.setNotificationEnable(2,checked);
	            ((CheckBoxPreference)(pref)).setChecked(checked);
	        }
	        else if(key.equals("key_facebook_notification_group_enable"))
	        {
	            boolean checked = (Boolean)value;
	            orm.setNotificationEnable(3,checked);
	            ((CheckBoxPreference)(pref)).setChecked(checked);
	        }
	        else if(key.equals("key_show_title_bar"))
	        {
	        	/*
	            boolean checked = (Boolean)value;
	            orm.setTitleBarVisible(checked);
	            ((CheckBoxPreference)(pref)).setChecked(checked);
	            
	            //we create a exception to let the program restart
	            String dd="";
	            char ss = dd.charAt(10);
	            */
	        }		
	        else if(key.equals("reset_default_key"))
	        {
	        	AlertDialog dialog = new AlertDialog.Builder(this).
	            setTitle(R.string.pref_extras_reset_default_dlg_title).
	            setMessage(R.string.pref_extras_reset_default_dlg).
	            setPositiveButton(getString(R.string.sns_ok),
		        new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            	SocialProvider.resetDatabase(FacebookSettingPreference.this);  
		            }
		        }).setNegativeButton(getString(R.string.sns_cancel),
		        new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {

		            }
		        }).create();
		        dialog.show();
	           
	        }
	        else if(key.equals("clear_cached_facebook_image_key"))
	        {
	        	AlertDialog dialog = new AlertDialog.Builder(this).
	            setTitle(R.string.pref_clear_facebook_image_cache_dlg_title).
	            setMessage(R.string.pref_clear_facebook_image_cache_dlg).
	            setPositiveButton(getString(R.string.sns_ok),
		        new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            	
		               Log.d(TAG,"entering clear cached images ");
		  	           File path = new File(TwitterHelper.tempimagePath);
		  	           deleteFiles(path);
		  	        	 
		  	           File path_nosdcard = new File(TwitterHelper.tempimagePath_nosdcard);
		  	           deleteFiles(path_nosdcard);
		            }
		        }).setNegativeButton(getString(R.string.sns_cancel),
		        new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {

		            }
		        }).create();
		        dialog.show();
	           
	        }
	        else if(key.equals("clear_fbinfo_key"))
	        {
	        	AlertDialog dialog = new AlertDialog.Builder(this).
	            setTitle(R.string.pref_clear_fbinfo_dlg_title).
	            setMessage(R.string.pref_clear_fbinfo_dlg).
	            setPositiveButton(getString(R.string.sns_ok),
		        new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {		            	
		            	ContactHelper.clearFacebookInfo(FacebookSettingPreference.this);  		            }
		        }).setNegativeButton(getString(R.string.sns_cancel),
		        new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {

		            }
		        }).create();
		        dialog.show();
	           
	        }
			
		}catch(Exception ne){}
       
		
		return true;
	}
	
	private void showHelpPage()
	{
		Intent intent = new Intent(this, AboutActivity.class);
		intent.putExtra("forhelp", true);
		startActivity(intent);
	}
	
    final static int SYNC_ADDRESS_BOOK = 1;
    private static Handler syncHandler;
    private class SyncHandler extends Handler 
    {
        public SyncHandler()
        {
            super();            
            Log.d(TAG, "new SyncHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case SYNC_ADDRESS_BOOK:
                {
                    
                    break;
                }    
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {            
            Log.d(TAG, "KEYCODE_BACK is comming="+this);
            if(changed || needrelogin == true)
            {
            	//will lead to re login UI
            	this.setResult(1000);
	            this.finish();
            }
            
            //service can't start sub activity
            if(comfromservice)
            {
            	//start login activity
            	Intent intent = new Intent(this, FacebookLoginActivity.class);            
        		startActivity(intent);
            }
            
            super.onKeyDown(keyCode, event);           
            return true;
        }        
        else
        {
        	return super.onKeyDown(keyCode, event);        
        }
    }

	public boolean onPreferenceClick(Preference pref) 
	{
	 	Log.d(TAG, "onPreferenceClick="+pref);
		String key = pref.getKey(); 
		if(key.equalsIgnoreCase("key_facebook_help_tips"))
	    {
	     	Log.d(TAG, "show help tips");
	      	showHelpPage();
	    }
		return false;
	}
	
	public boolean deleteDirectory(File path) 
	   {
		    if( path.exists() ) 
		    {
		        File[] files = path.listFiles();
		        for(int i=0; i<files.length; i++) 
	                {
		            if(files[i].isDirectory()) 
		            {
		                deleteDirectory(files[i]);
		            }
		            else 
		            {
		            	try
		            	{
		                    files[i].delete();
		            	}
		            	catch(Exception ne)
		            	{
		            		Log.d(TAG, "delete file fail="+files[i].getAbsolutePath());
		            	}
		            }
		        }
		    }
		    return( path.delete() );
	   }
	   
	   public void deleteFiles(File path) 
	   {
	           if( path.exists() ) 
		   {
		       File[] files = path.listFiles();
		       for(int i=0; i<files.length; i++) 
		       {
		           if(files[i].isDirectory()) 
		           {
		               try
		               {
		                   deleteDirectory(files[i]);
		               }
		               catch(Exception ne)
		               {
		            		Log.d(TAG, "delete file fail="+files[i].getAbsolutePath());
		               }
		           }
		           else 
		           {
		               try
		               {
		                   files[i].delete();
		               }
		               catch(Exception ne)
		               {
		            		Log.d(TAG, "delete file fail="+files[i].getAbsolutePath());
		               }
		           }
		       }
		   }     
	    }
   
}
