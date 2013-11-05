package com.msocial.freefb.ui;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.service.SNSService;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.Log;

public class SyncAddressBookHelper 
{	
    public static void processSyncAddressBook(final Context con, final boolean enable, final Preference pref, final Message msg) 
    {      
        if(enable)
        {
        	if(Activity.class.isInstance(con))
        	{
	            AlertDialog dialog = new AlertDialog.Builder(con)
	            .setTitle(con.getString(R.string.sync_address_book))
	            .setMessage(con.getString(R.string.sync_address_book_desc))
	            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() 
	            {
	               public void onClick(DialogInterface dialog, int whichButton) 
	               {
	                   if(FacebookSettingPreference.class.isInstance(con))
	                   {
	                       FacebookSettingPreference fp = (FacebookSettingPreference)con;
	                       fp.orm.EnableSyncPhonebook(enable);
	                       pref.setSummary(enable==true?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
	                       ((CheckBoxPreference)(pref)).setChecked(enable);
	                       
	                       //in setting we need to sync immediately, when user turn on
	                       if(enable == true)
	                       {
	                           SNSService.getSNSService().getContactService().resetOffset();
	                           SNSService.getSNSService().getContactService().alarmPhonebookComming();
	                       }	                       
	                   }
	                   else
	                   {
	                       SocialORM orm = SocialORM.instance(con);
	                       orm.EnableSyncPhonebook(enable);
	                       orm = null; 
	                   }                   
	                  
	                   if(msg != null)
	                   {
	                	   //from login
	                	   if(msg.getData().getBoolean("fromlogin", false) == true)
	                       {
	                           msg.getData().putBoolean("RESULT", true);
	                           msg.sendToTarget();
	                       }
		                   else
		                   {
		                       SocialORM orm = SocialORM.instance(con);
		                       if(orm.isAlwaysPromptSyncDialog())
		                       {
    		                	   Intent diaglogIntent = new Intent(con,PromptUserSyncActivity.class);
    		                	   diaglogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		                	   PromptUserSyncActivity.appendMessage(msg);	
    		                	   con.getApplicationContext().startActivity(diaglogIntent); 
		                       }
		                       else
		                       {
		                           msg.getData().putBoolean("RESULT", true);
	                               msg.sendToTarget();
		                       }
		                   }
	                   }
	                   
	                   Log.d("Sync address book", "confirm to enable the sync address book");
	               }
	            })
	            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() 
	            {
	               public void onClick(DialogInterface dialog, int whichButton) 
	               {
	                   //
	                   if(FacebookSettingPreference.class.isInstance(con))
	                   {
	                       FacebookSettingPreference fp = (FacebookSettingPreference)con;
	                       fp.orm.EnableSyncPhonebook(false);
	                       pref.setSummary(R.string.sns_forbiden_summary);
	                       ((CheckBoxPreference)(pref)).setChecked(false);
	                   }
	                   
	                   if(msg != null)
	                   {
	                       msg.getData().putBoolean("RESULT", false);
	                       msg.sendToTarget();
	                   }
	                   Log.d("Sync address book", "cancel to enable the sync address book");
	               }
	            })
	            .create();
	            dialog.show();
        	}
        	else
        	{
        		Log.d("SyncAddressBookHelper", "just ignore the request in background, user need enable the request");
        		
        	    Intent diaglogIntent = new Intent(con,SynchronizeAlertActivity.class);
         	    diaglogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         	    SynchronizeAlertActivity.appendMessage(msg);	
         	    con.getApplicationContext().startActivity(diaglogIntent); 
        	}
        }
        else//just setting need pass false, else will always give the true
        {
            if(FacebookSettingPreference.class.isInstance(con))
            {
                FacebookSettingPreference fp = (FacebookSettingPreference)con;
                fp.orm.EnableSyncPhonebook(enable);
                pref.setSummary(enable==true?R.string.sns_allow_summary:R.string.sns_forbiden_summary);
                ((CheckBoxPreference)(pref)).setChecked(enable);
            }
            else
            {
                SocialORM orm = new SocialORM(con);
                orm.EnableSyncPhonebook(enable);
                orm = null; 
            }           
            //do remove the cache, do we need this or not?
            removeCachePhonebook();
        }
    }
    
    
    private static void removeCachePhonebook() {
        Log.d("removeCachePhonebook", "*******unimplemented ***************");
    }
    
    public static boolean needAlwaysPromptSyncDialog(Context context,SocialORM orm)
    {
    	boolean alwaysPromptSyncDialog = orm.isAlwaysPromptSyncDialog();   	
    	return alwaysPromptSyncDialog;
    }
    
	public static void checkIsEnableAddressbookSync(Context context,SocialORM orm,Message msg) 
	{	
	    boolean enableSync = orm.isEnableSyncPhonebook();
	    if(enableSync)
	    {
	    	if(needAlwaysPromptSyncDialog(context,orm))
	    	{
	    		 if(msg != null)
                 {
              	     //from login
              	     if(msg.getData().getBoolean("fromlogin", false) == true)
                     {
                         msg.getData().putBoolean("RESULT", true);
                         msg.sendToTarget();
                     }
	                 else
	                 {
	                     Intent diaglogIntent = new Intent(context,PromptUserSyncActivity.class);
	    		    	 PromptUserSyncActivity.appendMessage(msg);
	    		    	 diaglogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	          	 context.getApplicationContext().startActivity(diaglogIntent); 
	                 }
                 }
	    	}
	    	else
	    	{
	    	    if(msg != null)
                {
                   msg.getData().putBoolean("RESULT", true);
                   msg.sendToTarget();
                }
	    	}
	    }
	    else//a little trick if it is false, we need to make it to true, then the user need to select whether accept or not
	    {
	        //don't do for background, no need let user turn on this in background
	        //
	        if(msg != null)
	        {
    	        boolean noneedifnorturnon = msg.getData().getBoolean("noneedifnorturnon", false);
    	        if(noneedifnorturnon == true)
    	        {
    	            Log.d("sychronizing address book", "no need prompt syncrhonize dialog, and will not do synchronizing task");
    	            if(msg.getData().getBoolean("RESULT", false) == true)
    	            {
    	            	//we want to get it, whatever user turn on or off
    	            }
    	            else
    	            {
    	                msg.getData().putBoolean("RESULT", false);
    	            }
                    msg.sendToTarget();
    	        }
    	        else
    	        {
    	    	    processSyncAddressBook(context,true,null,msg);
    	        }
	        }
	        else
	        {
	            processSyncAddressBook(context,true,null,msg);
	        }
	    }
    }

}
