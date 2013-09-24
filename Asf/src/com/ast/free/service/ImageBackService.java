package com.ast.free.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.ast.free.providers.SocialORM;
import com.ast.free.providers.SocialProvider;
import com.ast.free.providers.SocialORM.Follow;
import com.ast.free.ui.TwitterHelper;
import com.ast.free.ui.view.ImageRun;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Page;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

//try to get the image in background service
public class ImageBackService 
{
    final String TAG="ImageBackService";
        
    private FacebookUserObserver    facebookObserver;	
    private TwitterUserObserver     twitterFollowingObserver;
    private TwitterUserObserver     twitterFollowerObserver;
    private PageObserver            pageObserver;
    Handler handler;
    SocialORM orm;
    Context   mContext;
    List<String >urls = new ArrayList<String>();    
    public ImageBackService(Context con, SocialORM orm)
    {
        handler = new ImageHandler();
        this.orm = orm;
        
        //just first time
        handler.obtainMessage(RE_GET_URLS).sendToTarget();
        mContext = con;
        init();
    }
    private void init()
    {
    	facebookObserver         = new FacebookUserObserver();
    	twitterFollowingObserver = new TwitterUserObserver(0);
    	twitterFollowerObserver  = new TwitterUserObserver(1);
    	pageObserver             = new PageObserver();
    	mContext.getContentResolver().registerContentObserver(Uri.parse(SocialProvider.CONTENT_URI.toString()+"/facebookusers"), true, facebookObserver);
    	mContext.getContentResolver().registerContentObserver(Uri.parse(SocialProvider.CONTENT_URI.toString()+"/following"), true, twitterFollowingObserver);
    	mContext.getContentResolver().registerContentObserver(Uri.parse(SocialProvider.CONTENT_URI.toString()+"/follower"),  true, twitterFollowerObserver);  
    	mContext.getContentResolver().registerContentObserver(Uri.parse(SocialProvider.CONTENT_URI.toString()+"/page"), true, pageObserver);
    }
    
    public void Stop()
    {
    	mContext.getContentResolver().unregisterContentObserver(facebookObserver);
    	mContext.getContentResolver().unregisterContentObserver(twitterFollowingObserver);
    	mContext.getContentResolver().unregisterContentObserver(twitterFollowerObserver);
    }
    
    static long nLastPID = -1;
    private class PageObserver extends ContentObserver
    {

        public PageObserver() {
            super(new Handler());
        }
        
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
        
        @Override
        public void onChange(boolean selfChange) 
        {               
            String next = "";
            Page page = orm.getLastPage();
            if(page!=null && page.page_id != nLastPID)
            {
                nLastPID = page.page_id;
                next = page.pic_square;
            }
            else
            {
                return;
            }
            
            if(next != null && next.length()>0)
            {
                synchronized(urls)
                {
                    urls.add(next);
                }
                Log.d(TAG, "new page is coming="+page.name);
                Message msd = handler.obtainMessage(TRY_TO_GET_IMAGE);
                msd.getData().putString("imageurl", next);
                handler.sendMessage(msd);
            }
        }
    }
    
    static long nLastFUserID=-1;
    private class FacebookUserObserver extends ContentObserver 
	{
    	public FacebookUserObserver() 
	    {
	        super(new Handler());	    
	    }
	    
	    @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
	    
	    @Override
	    public void onChange(boolean selfChange) 
	    {		    	
	        String next = "";
	    	FacebookUser.SimpleFBUser user = orm.getLastSimpleFacebookUser();
	    	if(user!=null && user.uid != nLastFUserID)
	    	{
	    		nLastFUserID = user.uid;
	    		next = user.pic_square;
	    	}
	    	else
	    	{
	    		return;
	    	}
	    	
	    	if(next != null && next.length()>0)
	    	{
		    	synchronized(urls)
		    	{
		    		urls.add(next);
		    	}
		    	//Log.d(TAG, "new user is coming="+user.name);
		    	Message msd = handler.obtainMessage(TRY_TO_GET_IMAGE);
	            msd.getData().putString("imageurl", next);
                handler.sendMessage(msd);
	    	}
	    }
	}
    
    
    static int nLastTUserID=-1;
    private class TwitterUserObserver extends ContentObserver 
	{
    	int mType=0;
	    public TwitterUserObserver(int type) 
	    {
	        super(new Handler());
	        mType = type;
	    }
	    
	    @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
	    
	    @Override
	    public void onChange(boolean selfChange) 
	    {		    
	    	String next = "";
	    	/* no urgent need
	    	Follow follow = orm.getLastTwitterFollowUser();
	    	if(follow.UID != nLastTUserID)
	    	{
	    		nLastTUserID = follow.UID;
	    		next = follow.ProfileImgUrl;
	    	}
	    	else
	    	{
	    		return;
	    	}*/
	    	
	    	if(next != null && next.length()>0)
	    	{
		    	synchronized(urls)
		    	{
		    		urls.add(next);
		    	}
		    	
		    	Log.d(TAG, "new twitter use is coming="+next);
		    	
		    	Message msd = handler.obtainMessage(TRY_TO_GET_IMAGE);
	            msd.getData().putString("imageurl", next);
                handler.sendMessage(msd);
	    	}
	    }
	}
    
    
    private String nextvalue()
    {
    	String value="";
    	synchronized(urls)
    	{        
	        if(urls.size()>0)
	        {
	            value = urls.get(0);
	            urls.remove(0);
	        }
	        else
	        {
	            //reget the urls
	            //reGetTheData();            
	        }
    	}
        return value;
    }
    
    private void reGetTheData()
    {
        synchronized(urls)
        {
            //try to get urls from database
            urls.clear();
            urls.addAll(orm.getTwitterUserImageURL());
            urls.addAll(orm.getFacebookImagesByuid(null, null));
            
            String next = nextvalue();
            if(next.length() > 0)
            {
                Message msd = handler.obtainMessage(TRY_TO_GET_IMAGE);
                msd.getData().putString("imageurl", next);
                handler.sendMessageDelayed(msd, 10*60*1000);
            }   
        }
    }
    
    private void removepath(String url)
    {
        Log.d(TAG, "get image from url="+url);
    }
    
    final int TRY_TO_GET_IMAGE    =0;
    final int TRY_TO_GET_IMAGE_END=1;
    final int RE_GET_URLS         =2;
    private class ImageHandler extends Handler 
    {
        public ImageHandler()
        {
            super();            
            Log.d(TAG, "new ImageHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case TRY_TO_GET_IMAGE:
                {
                    final String url = msg.getData().getString("imageurl");
                    
                    String filepath = TwitterHelper.getImagePathFromURL_noFetch(url);
                    if(new File(filepath).exists() == false)
                    {
	                    if(url != null && url.length()>0)
	                    {
	                    	//dispatch to thread pool
		                    Runnable obj = new Runnable()
		                    {
		                        public void run() 
		                        {
		                            String localpath = TwitterHelper.getImagePathFromURL(mContext.getApplicationContext(), url, false);  
		                            if(localpath != null)
		                            {
		                                removepath(localpath);
		                            }
		                        }
		                        
		                    };	                    
		                    ImageRun.getThreadPool().dispatch(obj);
	                    
		                    
		                    String next = nextvalue();                   
		                    Message msd = handler.obtainMessage(TRY_TO_GET_IMAGE);
		                    msd.getData().putString("imageurl", next);
		                    handler.sendMessageDelayed(msd, 30*1000);    
	                    }    
	                    else
	                    {
	                        //if no url, stop to loop to save battery
	                    }
                    }
                    else
                    {
                    	//Log.d(TAG, "url image exist="+url + " path="+filepath);
                    }
                    
                    break;
                } 
                case TRY_TO_GET_IMAGE_END:
                {
                    break;
                } 
                case RE_GET_URLS:
                {
                    Log.d(TAG, "reget the image from url");
                    reGetTheData();
                    
                    break;
                }
            }
        }
    }
}
