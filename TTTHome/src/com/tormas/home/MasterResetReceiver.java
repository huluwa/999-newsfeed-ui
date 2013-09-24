package com.tormas.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MasterResetReceiver extends BroadcastReceiver 
{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MasterResetReceiver","MasterResetReceiver===");
        if(intent.getAction().equals("oms.action.MASTERRESET"))
        {
            Log.d("MasterResetReceiver","entering masterRESET===");
            Launcher.setHomeInitTag(context, 0);               
        }
    }        
}