package com.msocial.nofree.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.msocial.nofree.R;
import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.service.FacebookLoginHelper;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.ExtendedPermission;
import oms.sns.service.facebook.util.StringUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class FacebookPermSettingPreference extends PreferenceActivity 
implements Preference.OnPreferenceChangeListener 
{
	final String TAG ="FacebookPermSettingPreference";
	String currentPermission;
	private Handler         handler;
	private FacebookSession sf;
	private AsyncFacebook   asyncF;
	private SocialORM       orm;
	
	private static HashMap<String, String> extPermMapMethod = new HashMap<String, String>();
	public static ArrayList<String> permList = new ArrayList<String>();
	
	static 
	{
		permList.add("read_stream");		
		permList.add("publish_stream");
		permList.add("email");
		permList.add("status_update");
		permList.add("photo_upload");
		permList.add("create_event");
		permList.add("video_upload");
		permList.add("share_item");
		permList.add("rsvp_event");
		permList.add("create_note");
		permList.add("read_mailbox");
		
		for(int i=0;i<permList.size();i++)
		{
			extPermMapMethod.put(String.format("key_facebook_%1$s", permList.get(i)),       permList.get(i));
		}		
	}
	
	private void loadRefresh()
	{
		 loadFromDatabase();
		 checkExtPermission();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 super.onCreateOptionsMenu(menu);
	     MenuInflater inflater = getMenuInflater();
	     inflater.inflate(R.menu.facebook_option_menu, menu);
	     return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
	    {    
		    case R.id.facebook_menu_refresh:
		    {
		    	loadRefresh();
		    	break;
		    }
	    }
		
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {		
		 super.onPrepareOptionsMenu(menu);
	     menu.findItem(R.id.facebook_menu_refresh).setVisible(true);	
	     return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		//this.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        //this.requestWindowFeature(Window.FEATURE_PROGRESS);
        this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);        
        super.onCreate(savedInstanceState);     
        
        // Load the XML preferences file
        addPreferencesFromResource(R.xml.facebook_perm_pre);
        
//        Resources res = getResources();
//        Drawable mCacheSym = res.getDrawable(R.color.facebook_backgroud);
//        this.getWindow().setBackgroundDrawable(mCacheSym);
        
        setTitle(R.string.pref_facebook_perm_settings);
        orm = SocialORM.instance(this);
        //process extends permission
        loadFromDatabase();
        
        
        Preference su = findPreference("key_facebook_perm_assign");	        
	    su.setOnPreferenceChangeListener(this);
	    boolean enbaledAssign = orm.isEnableAssignPermission();        
	    ((CheckBoxPreference)(su)).setChecked(enbaledAssign);
	    
        checkExtPermission();
	}
	
	private void loadFromDatabase()
	{
		String[] perm_descArray = this.getResources().getStringArray(R.array.permission_desc_list);
		String[] perm_NameArray = this.getResources().getStringArray(R.array.permission_name_list);
	    Set<String> set = extPermMapMethod.keySet();
        Iterator<String> it = set.iterator();
        while(it != null && it.hasNext())
        {
        	String key = it.next();
        	Log.d(TAG, "which key="+key);
        	Preference su = findPreference(key);	        
 	        su.setOnPreferenceChangeListener(this);
 	        
 	        String perm = extPermMapMethod.get(key);
 	        //su.setTitle(perm);
 	        for(int i=0;i<permList.size();i++)
 	        {
 	        	if(permList.get(i).equals(perm))
 	        	{
 	        		String perm_desc = perm_descArray[i];
 	        		su.setSummary(perm_desc);
 	        		
 	        		String title = perm_NameArray[i];
 	        		su.setTitle(title);
 	        		break;
 	        	}
 	        } 	        
 	        
 	        boolean enableSU = orm.isExtPermissionAllow(extPermMapMethod.get(key)); 	        
 	        ((CheckBoxPreference)(su)).setChecked(enableSU);
        }
	}
	private void checkExtPermission()
	{
		FacebookLoginHelper loginHelper = FacebookLoginHelper.instance(this);
		loginHelper.restoreSesstion();
		sf = loginHelper.getPermanentSesstion();
		
		if(sf != null)
		{
			asyncF = new AsyncFacebook(sf);
			handler = new CheckHandler();
			//this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_menu_login);
			Message msg = handler.obtainMessage(CHECK_PERMISSION);
			handler.sendMessageDelayed(msg, 1*1000);
		}
		else
		{
			//this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_menu_logout);
		}
	}

	private Preference findPref(String fperm)
	{
		Preference pref=null;
		Set<String> set = extPermMapMethod.keySet();	            			
    	Iterator<String> it = set.iterator();
    	while(it != null && it.hasNext())
    	{
    		String key = it.next();
    		String perm = extPermMapMethod.get(key);
    		if(perm.equalsIgnoreCase(fperm))
    		{
    			pref = findPreference(key);
    			break;
    		}
    	}
    	
    	return pref;
	}

	final int CHECK_PERMISSION     =0;
	final int CHECK_PERMISSION_END =1;
	final int REVOKE_PERMISSION    =2;
	final int REVOKE_PERMISSION_END=3;
	
    private class CheckHandler extends Handler 
    {
        public CheckHandler()
        {
            super();            
            Log.d(TAG, "new CheckHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case CHECK_PERMISSION:
	            {	     
	            	setTitle(R.string.facebook_check_permission_title);
	            	setProgress(0);	   
	            	
	            	asyncF.hasAppPermission_batch_run_async(permList,new FacebookAdapter()
	            	{
	            	  
                        @Override
                        public void hasAppPermission_batch_run(List<Boolean> resultlist)
                        {   
                            Log.d(TAG, "after check= "+resultlist.size());
                            
                           
                            boolean[] resultarray = new boolean[resultlist.size()];
                            for(int i=0;i<permList.size();i++)
                            {
                                resultarray[i] = resultlist.get(i).booleanValue();
                            }
                            Message resMsg = handler.obtainMessage(CHECK_PERMISSION_END);
                            resMsg.getData().putBooleanArray("resultarray", resultarray);
                            handler.sendMessage(resMsg);
                        }

                        @Override public void onException(FacebookException e, int method) 
                        {   
                            Log.e(TAG, "fail to call hasAppPermission="+e.getMessage());
                            
                            handler.obtainMessage(CHECK_PERMISSION_END).sendToTarget();                             
                        }
	            	});	            	
	            
	            	break;
	            }
	            case CHECK_PERMISSION_END:
	            {
	                boolean[] booleanarray = msg.getData().getBooleanArray("resultarray");
	            	if(booleanarray!=null && booleanarray.length==extPermMapMethod.size())
	            	{
	            	    for(int step=0;step<permList.size();step++)
	                    {   
	            	        String value = permList.get(step);
                            Preference su = findPref(value);
                            if(su != null)
                            {
                                //su.setSummary(booleanarray[step]==true?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
                                ((CheckBoxPreference)(su)).setChecked(booleanarray[step]);                                 
                                orm.updateExtPermissions(value,booleanarray[step]);
                            }
                            else
                            {
                                Log.d(TAG, "why come here="+value);
                            }
                                            
                            setProgress((int)((float)step/((float)(extPermMapMethod.size()-1)))*100*100);
                            if(step == extPermMapMethod.size()-1)
                            {
                                setTitle("");
                            }
                        }
	            	}
	            	else
	            	{
	            	    setTitle("");
	            	}
	            	
	            	setTitle(R.string.pref_facebook_perm_settings);
	            
	            	break;
	            }
	            case REVOKE_PERMISSION:
	            {
	            	FacebookPermSettingPreference.this.showDialog(DLG_CHECKING);
	            	String key  = msg.getData().getString("key");
	            	final String perm = msg.getData().getString("perm");
	            	
					asyncF.revokePermissionAsync(perm, new FacebookAdapter()
            		{
            			@Override
            			public void revokePermission(String chperm, boolean revoked)
            			{	
            				Log.d(TAG, "after revoke="+chperm+ " perm="+revoked);
            				
            				Message resMsg = handler.obtainMessage(REVOKE_PERMISSION_END);
            				resMsg.getData().putBoolean("revoked", revoked);
            				resMsg.getData().putString("permission", chperm);
            				
            				handler.sendMessage(resMsg);
            			}

                        @Override public void onException(FacebookException e, int method) 
                        {	
                        	Log.e(TAG, "fail to call revoke="+e.getMessage());
                        	Message resMsg = handler.obtainMessage(REVOKE_PERMISSION_END);
                            resMsg.getData().putString("permission", perm);
                            handler.sendMessage(resMsg);	                        	
                        }
						
            		});
					
	            	break;
	            }
	            case REVOKE_PERMISSION_END:
	            {
	            	FacebookPermSettingPreference.this.dismissDialog(DLG_CHECKING);
	            	
	            	String chperm   = msg.getData().getString("permission");
	            	boolean revoked = msg.getData().getBoolean("revoked");
	            	if(revoked)
					{
						orm.disableExtPermissions(chperm);
						
						Preference su = findPref(chperm);
			 	        //su.setSummary(revoked==true?R.string.sns_forbiden_summary:R.string.sns_allow_summary);
			 	        ((CheckBoxPreference)(su)).setChecked(!revoked);						
					}
	            	else
	            	{
	            	    String failmessage = "Fail to revoke ";
	            	    if(StringUtils.isEmpty(chperm))
	            	    {
	            	        failmessage = failmessage + chperm;
	            	    }
	            		Toast.makeText(FacebookPermSettingPreference.this,failmessage, Toast.LENGTH_SHORT).show();
	            	}
	            	break;
	            }
            }
        }
    }
	
	private void revokePermission(String key, String perm) 
	{		
		if(sf != null)
		{
			 final String permission = perm;
			 final String permKey    = key;
			 AlertDialog dialog = new AlertDialog.Builder(this)
	         .setTitle(getString(R.string.facebook_revoke_ext_permission_model))
	         .setMessage(String.format(getString(R.string.facebook_revoke_permission_conform_mes), perm))
	         .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() 
	         {
	            public void onClick(DialogInterface dialog, int whichButton) 
	            {	                    
	            	Message msg = handler.obtainMessage(REVOKE_PERMISSION);
	            	msg.getData().putString("key",  permKey);
	            	msg.getData().putString("perm", permission);
	            	
	            	handler.sendMessage(msg);
	            }
	         })
	         .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() 
	         {
	            public void onClick(DialogInterface dialog, int whichButton) 
	            {
	            	
	            }
	         })
	         .create();
	         dialog.show();		
		}
		else
		{
			Toast.makeText(this, R.string.sns_not_login_message, Toast.LENGTH_SHORT).show();
		}
	}
	
	final int FACEBOOK_EXT_PERMISSION_UI=0;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
	      switch(requestCode)
	      {  
	            case FACEBOOK_EXT_PERMISSION_UI:
	            {		 
	            	//check current permission
	            	//currentPermission;
	            	loadFromDatabase();
	            	break;
	            }
	      }
	}
	
	public boolean onPreferenceChange(Preference pref, Object value) {
		String key = pref.getKey(); 
		if(     key.equals("key_facebook_status_update")||
				key.equals("key_facebook_photo_upload")||
				key.equals("key_facebook_create_event")||
				key.equals("key_facebook_video_upload")||
				key.equals("key_facebook_email")||
				key.equals("key_facebook_read_mailbox")||
				key.equals("key_facebook_read_stream")||
				key.equals("key_facebook_publish_stream")||
				key.equals("key_facebook_share_item")||
				key.equals("key_facebook_create_note")||
				key.equals("key_facebook_event_rvsp"))
		{
			currentPermission = extPermMapMethod.get(key);
			boolean checked = (Boolean)value;
			
			//TODO
			//revoke the permission
			if(checked == false)
			{	
				revokePermission(key, currentPermission);
			}
			else
			{
				Log.d(TAG, "prompt permission check UI= "+currentPermission);            	
            	Intent intent = new Intent(this, FacebookExtPermissionActivity.class);
            	intent.putExtra("permission", currentPermission);
        		startActivityForResult(intent, FACEBOOK_EXT_PERMISSION_UI);     
			}
		}
		else if(key.equals("key_facebook_perm_assign"))
		{
			boolean enable = (Boolean)value;
			orm.EnableAssignPermission(enable);			
 	        ((CheckBoxPreference)(pref)).setChecked(enable);	
		}
		return false;
	}
	
	final static int DLG_CHECKING=0;
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DLG_CHECKING: 
            {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.sns_revoke);
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
