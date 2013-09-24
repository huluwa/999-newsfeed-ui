package com.tormas.litetwitter.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.util.Log;

public interface TwitterAccountListener {
    public void onLogout();
    public void onLogin();  
    public void unregisterAccountListener();
    public void registerAccountListener();
    
    public class TwitterAccountManager
    {
        final static String TAG ="TwitterAccountManager";
        private static Map<String, TwitterAccountListener> listener = new HashMap<String, TwitterAccountListener>(); 
        public static void registerAccountListener(String key, TwitterAccountListener obj)
        {
            synchronized(listener)
            {
                listener.put(key, obj);
            }
        }   
        public static void unregisterAccountListener(String key)
        {
            synchronized(listener)
            {
                listener.put(key, null);
                listener.remove(key);
            }
        }
        
        public static  void logout()
        {
            Log.d(TAG, "logout");
            synchronized(listener)
            {
                Set<String> set = listener.keySet();
                Iterator<String> it = set.iterator();
                while(it.hasNext())
                {
                    String key = it.next();
                    
                    TwitterAccountListener list = listener.get(key);
                    if(list != null)
                    {
                        list.onLogout();
                    }
                }
            }
        }
        
        public static void login()
        {
            Log.d(TAG, "logout");
            synchronized(listener)
            {
                Set<String> set = listener.keySet();
                Iterator<String> it = set.iterator();
                while(it.hasNext())
                {
                    String key = it.next();
                    
                    TwitterAccountListener list = listener.get(key);
                    if(list != null)
                    {
                        list.onLogin();
                    }
                }
            }
        }
    }

}
