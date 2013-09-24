package com.tormas.litetwitter;

import com.tormas.litetwitter.ui.TwitterHelper;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.util.Log;

public class SdCardReceiver extends BroadcastReceiver 
{   
    @Override 
    public void onReceive(Context context, Intent intent) 
    {
    	String action = intent.getAction();
        Log.d("sns-sdcard event", "action is: "+action);
        if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) 
        {
        	Log.d("sns-sdcard event", "ACTION_MEDIA_UNMOUNTED");
        	TwitterHelper.unmountSdcard();
        } 
        else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) 
        {
        	Log.d("sns-sdcard event", "ACTION_MEDIA_MOUNTED");
            if (intent.getBooleanExtra("read-only", false))
            {
            	TwitterHelper.mountSdcard(true);
            }
            else
            {
            	TwitterHelper.mountSdcard(false);            	
            }
        }  
    }       
}
