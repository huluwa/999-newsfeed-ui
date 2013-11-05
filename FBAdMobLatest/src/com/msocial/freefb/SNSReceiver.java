package com.msocial.freefb;

import java.io.File;
import java.io.IOException;

import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.providers.SocialProvider;
import com.msocial.freefb.service.FacebookLoginHelper;
import com.msocial.freefb.service.SNSService;
import oms.sns.service.facebook.client.FacebookSession;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.msocial.freefb.R;

public class SNSReceiver extends BroadcastReceiver 
{
    private final String TAG="SNSReceiver";
    private Context context;

	@Override
    public void onReceive(Context context, Intent intent) 
    {
	    this.context = context;
        String action = intent.getAction();
        if("oms.action.MASTERRESET".equals(action) ){
            //reset to the initial status. so we need reset database
            // clear cached file
            //SocialProvider.resetDatabase(context);
           // ClearCacheThread clearCache = new ClearCacheThread();
            //clearCache.start();
            
            Log.d(TAG,"entering MASTERRESET");
        }
        else if ("android.permission.MASTER_CLEAR".equals(action) ) {            
            Log.d(TAG,"entering MASTER_CLEAR");
        } 
        else if(intent.getAction().equals(android.content.Intent.ACTION_BOOT_COMPLETED)) 
        {
        	//start service     
            FacebookLoginHelper loginHelper = FacebookLoginHelper.instance(context);
            loginHelper.restoreSesstion();
            FacebookSession perm_session = loginHelper.getPermanentSesstion();
            if(perm_session == null)
            {
                int pid = android.os.Process.myPid();
                Log.d(TAG, "no valid session, no need start the service, kill the process="+pid);                
                android.os.Process.killProcess(pid);
                Log.d(TAG, "after kill com.msocial.freefb process="+pid);
            }
            else
            {
            	Log.d(TAG, "start SNS service");            	
            	perm_session = null;
            	
                Intent in = new Intent(context, SNSService.class);            
                context.startService(in);
            }
        }   
        else if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(intent.getAction())) 
        {            
        	Log.d(TAG, "clear all logo");
        	
        	File files = new File("/data/data/com.msocial.freefb/files/");
        	if(files.exists())
        	{
        		//try 
        		//{
					//Runtime.getRuntime().exec("rm /data/data/com.msocial.freefb/files/* ");
        	        ClearCacheThread clearCacheThread = new ClearCacheThread();
        	        clearCacheThread.start();
				//} catch (IOException e) 
				//{					
				//	Log.e(TAG, "fail to delete files="+e.getMessage());
				//}
        	}
        }
        else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(intent.getAction())) 
        {
            Log.d(TAG, "has storage now");
        }
    }
	
	private class ClearCacheThread extends Thread
	{
        @Override
        public void run() {
            File file = new File("/data/data/com.msocial.freefb/files/");
            deleteFile(file);
            clearDatabase();
            Log.d(TAG, "removed files");            
        }
        
	}

    private void deleteFile(File file) {
        if(file != null && file.exists())
        {
            if(file.isDirectory())
            {
                File[] files = file.listFiles();       
                for(int i=0;i<files.length;i++)
                {
                    deleteFile(files[i]);
                }
                try{
                    file.delete();
                }catch(Exception e){}
            }
            else
            {
                try{
                    file.delete();
                }catch(Exception e){}
            }
        }
        
    }

    private void clearDatabase() {
        //check database 
        // if sns.db    
        File file = new File("/data/data/com.msocial.freefb/databases/sns.db");
        int maxDBsize = Integer.valueOf(context.getString(R.string.facebook_db_max_size));
        long filesize = file.length();
        long maxFileSize = maxDBsize*1024L*1024L;
        Log.d(TAG,"sns.db size="+filesize+" max size="+maxFileSize);
        if(filesize> maxFileSize)
        {
            SocialORM.instance(context).clearDatabase();;
        }
    }
}
