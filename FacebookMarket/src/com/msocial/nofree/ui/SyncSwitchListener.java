package com.msocial.nofree.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.util.Log;

public interface SyncSwitchListener {	
	public void setEnable(boolean enable);	
	public void unregisterSyncSwitchListener();
	public void registerSyncSwitchListener();
	
	public class SyncSwithManager
	{
		final static String TAG ="SyncSwithManager";
		private static Map<String, SyncSwitchListener> listener = new HashMap<String, SyncSwitchListener>(); 
		public static void registerSyncSwithListener(String key, SyncSwitchListener obj)
		{
			synchronized(listener)
			{
				listener.put(key, obj);
			}
		}	
		public static void unregisterSyncSwithListener(String key)
		{
			synchronized(listener)
			{
				listener.put(key, null);
				listener.remove(key);
			}
		}
		
		public static  void setEnable(boolean enable)
		{
			Log.d(TAG, "setEnable ="+enable);
			synchronized(listener)
			{
				Set<String> set = listener.keySet();
				Iterator<String> it = set.iterator();
				while(it.hasNext())
				{
					String key = it.next();
					
					SyncSwitchListener list = listener.get(key);
					if(list != null)
					{
					    list.setEnable(enable);
					}
				}
			}
		}
	}

}
