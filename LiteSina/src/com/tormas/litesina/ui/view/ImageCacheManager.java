package com.tormas.litesina.ui.view;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.util.Log;

public class ImageCacheManager {
    final String TAG="sns-image cache";
    int cacheSize=20;//it is too big, we just 20
    List<ImageCache> caches = new ArrayList<ImageCache>();
        
    private static ImageCacheManager _instance;
    public static ImageCacheManager instance()
    {
    	if(_instance == null)
    	{
    		_instance = new ImageCacheManager();
    	}
    	
    	return _instance;
    }
    
    private ImageCacheManager()
    {
    	
    }
    public void addCache(String url, Bitmap bmp)
    {
        if(bmp != null && (bmp.getHeight() > 80 || bmp.getWidth() > 80))
        {
            Log.d(TAG, "size is too big height="+bmp.getHeight() + " width="+bmp.getWidth());
            return;
        }
    	synchronized(caches)
    	{
	        ImageCache cache = new ImageCache();
	        cache.url = url;
	        cache.bmp = bmp;
	        cache.age = System.currentTimeMillis();
	        
	        caches.add(cache);
	        
	        if(caches.size() > cacheSize)
	        {
	            //remove last 5 items
	            for(int i=0;i<cacheSize/8;i++)
	            {
	            	ImageCache item = caches.remove(caches.size()-1);
	            	
	                item.bmp = null;
	                item.url = null;
	                item.age = 0;
	                item = null;
	            }
	        }
    	}
    }
    
    public void dump()
    {
        Log.d(TAG, "size="+caches.size());
        long memsize=0;
        for(int i=0;i<caches.size();i++)
        {
            memsize += caches.get(i).bmp.getRowBytes();
            Log.d(TAG, "item: "+caches.get(i).url + " bmp="+caches.get(i).bmp.getRowBytes());
        }
        Log.d(TAG, "totle size: "+memsize); 
    }
    
    public ImageCache getCache(String key)
    {
    	synchronized(caches)
    	{
	        for(int i=0;i<caches.size();i++)
	        {
	            ImageCache cache = caches.get(i);
	            if(cache.url.equalsIgnoreCase(key))
	            {
	                Log.d(TAG, "get from cache");
	                cache.ref++;
	                
	                return cache;
	            }
	        }
    	}
        return null;
    }
    
    public class ImageCache
    {
        public String url;
        public Bitmap bmp;
        public long   age;
        public int    ref;
        
        //for compare
        public boolean equals(Object ref)
        {            
            if(url.equalsIgnoreCase(((ImageCache)ref).url))
                return true;
            else
                return false;
        }
        
        //for sort
        public int compareTo(ImageCache ref)
        {
            if(age >=ref.age)
            {
                return 1;
            }
            else
            {
               return -1; 
            }
        } 
    }

}
