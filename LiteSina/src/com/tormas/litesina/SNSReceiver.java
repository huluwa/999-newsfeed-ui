package com.tormas.litesina;

import java.io.File;
import java.io.IOException;

import com.tormas.litesina.providers.SocialORM;
import com.tormas.litesina.providers.SocialProvider;
import com.tormas.litesina.service.SNSService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.tormas.litesina.R;

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
            Intent in = new Intent(context, SNSService.class);            
            context.startService(in);            
        }   
        else if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(intent.getAction())) 
        {            
        	Log.d(TAG, "clear all logo");
        	
        	File files = new File("/data/data/com.tormas.litesina/files/");
        	if(files.exists())
        	{
        		//try 
        		//{
					//Runtime.getRuntime().exec("rm /data/data/com.tormas.litesina/files/* ");
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
            File file = new File("/data/data/com.tormas.litesina/files/");
            deleteFile(file);
            clearDatabase();
            Log.d(TAG, "removed files");
            this.destroy();
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
        File file = new File("/data/data/com.tormas.litesina/databases/sns.db");
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
