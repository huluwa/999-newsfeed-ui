package com.borqs.omshome25;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class MasterResetReceiver extends BroadcastReceiver 
{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MasterResetReceiver","MasterResetReceiver===");
        if(intent.getAction().equals("oms.action.MASTERRESET"))
        {
            Log.d("MasterResetReceiver","entering masterRESET===");
            SharedPreferences settings = context.getSharedPreferences(Launcher.INIT_TAG_FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(Launcher.HOME_INIT_TAG, 0);
//            editor.putInt(Workspace.TAG_SCREEN_NUM,  Workspace.DEFAULT_SCREEN_NUM);
//        	editor.putInt(LauncherORM.default_page_index, Workspace.DEFAULT_CURRENT_SCREEN);
        	editor.remove(Workspace.TAG_SCREEN_NUM);
        	editor.remove(LauncherORM.default_page_index);
            editor.commit();
        }
    }      
}