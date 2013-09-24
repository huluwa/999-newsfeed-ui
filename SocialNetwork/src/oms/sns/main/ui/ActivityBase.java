package oms.sns.main.ui;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import oms.sns.main.R;
import oms.sns.main.providers.SocialORM;
import oms.sns.main.service.SNSService;
import twitter4j.AsyncTwitter.AsyncTask;
import twitter4j.threadpool.QueuedThreadPool.PoolThread;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import android.text.ClipboardManager;

public class ActivityBase extends Activity implements NetworkConnectionListener
{	
	public interface TitleListener
	{
		public void setTitle();
	}
	
	private final String TAG="ActivityBase";
	protected static final boolean DEBUG=SNSService.DEBUG;
	
	protected static String START_REQUEST= "Loading...";//"Sending...";
	protected static String LOAD_FROM_DB="Loading...";//"Loading...";
	protected static String PREPARING="Loading...";//"Preparing...";
	protected static String TITLE_FINISH_NETWORK = "Loading...";//"Parsing...";
	protected static String TITLE_STOPING      = "Loading...";//"Stopping...";
	protected static String TITLE_CONSTRUCT_UI = "Loading...";//"Using...";
	protected static String TITLE_SEARCH = "Searching...";
	
	protected static final int STOP_PROGRESS       =90;
	protected static final int DATA_READY_PROGRESS =10;
	
	
	protected Handler basichandler;
	protected Activity mContext;
	protected static final int UI_SET_PROGRESS     = 10000;
	protected static final int UI_SET_TITLE        = 10001;
	protected static final int INVALID_SESSION     = 200002;
	protected static final int NO_EXT_PERMISSION   = 200003;
	
	
	//protected AndroidAsyncTwitter omstwitterA;
	protected boolean donotcallnetwork=false;
	
	//process whether is in process
	protected boolean inprocess =false;
	protected Object mLock = new Object();
    
    public synchronized boolean  isInProcess()
    {
        return inprocess;       
    }
    
    //copy item view content 
    protected void doCopy(String content) 
    {    
        Log.d(TAG, "text content="+content);       
        android.text.ClipboardManager clip = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);            
        if (clip != null) {
            clip.setText(content);
        }       
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {	
    	super.onCreate(savedInstanceState);
    	
    	START_REQUEST = this.getString(R.string.loading_string);
    	LOAD_FROM_DB  = this.getString(R.string.loading_string);
    	PREPARING     = this.getString(R.string.loading_string);
    	TITLE_FINISH_NETWORK = this.getString(R.string.loading_string);
    	TITLE_STOPING = this.getString(R.string.stoping_string);
    	TITLE_CONSTRUCT_UI = this.getString(R.string.loading_string);
    	TITLE_SEARCH = this.getString(R.string.loading_string);
    }
    
    @Override protected void onResume() 
    {
    	Log.d(TAG, "onResume="+this);
    	
        super.onResume();
        donotcallnetwork=false;         
    }
    protected synchronized boolean isBackgroud()
    {
    	return donotcallnetwork;
    }
    
    @Override protected void onPause() {
        super.onPause();
        donotcallnetwork = true;        
        Log.d(TAG, "onPause="+this);
    }
    
    @Override protected void onDestroy() 
    {
        if (DEBUG) 
        {
            Log.v(TAG, "onDestroy: this=" + this);
        }
        donotcallnetwork = true;
        
        super.onDestroy();
    }
    
    protected static boolean isEmpty(String str)
    {
    	return str==null || str.length() ==0;
    }
    
    //the parse occupy 25% time
    public void updateProgress(int pos, int count) 
    {
        if(this.isBackgroud() == false)
		{
		    Log.d(TAG, "progress pos="+pos + " count="+count);			
		    int poss = (int)(((float)pos/(float)count)*(STOP_PROGRESS-DATA_READY_PROGRESS));
		    updateProgress((DATA_READY_PROGRESS + poss ) * 100);
		}
		else
		{
		    Log.d(TAG, "I am in backgound, progress pos="+pos + " count="+count);
		}
    }	
	
    public void updateProgress(int progress)
    {    	
        if(this.isBackgroud() == false)
        {
            Log.d(TAG, "progress="+progress/100);
            Message msg = basichandler.obtainMessage(UI_SET_PROGRESS);
            msg.getData().putInt("progress", progress);
            msg.sendToTarget();
        }
        else
        {
            Log.d(TAG, "I am in backgound, updateProgress="+progress/100);
        }
    }
	
    public void updateTitle(String title)
    {
        if(this.isBackgroud() == false)
        {
	        Message msg = basichandler.obtainMessage(UI_SET_TITLE);
	        msg.getData().putString("title", title);
	        msg.sendToTarget();
	    }
	else
	{
	    Log.d(TAG, "I am in backgound, updateTitle="+title);
	}
    }
	  
    //TODO 
    //to save the network
    //when exit the activity, or onPause the activity,
    //
    //
    public HashMap<Integer, HttpURLConnection> connections = new HashMap<Integer, HttpURLConnection>();	
    public HashMap<Integer, Runnable> runs = new HashMap<Integer, Runnable>();
    public void stopLoading()
    {
        if(DEBUG)
        Log.d("********twitter-network", "call stopLoading="+connections.size()+" this="+this);
        synchronized(connections)
        {
            Set<Integer> set = connections.keySet();
            Iterator<Integer> it = set.iterator();
            while(it.hasNext())
            {
                int UID = it.next();
                HttpURLConnection con = connections.get(UID);
                if(DEBUG)
                Log.d(TAG, "stop UID="+UID+" connection="+con.getURL());
                con.setConnectTimeout(10);
                con.setReadTimeout(10);  
                
                Runnable run = runs.get(UID);                
            	if(run != null && twitter4j.AsyncTwitter.AsyncTask.class.isInstance(run))
            	{
            		((twitter4j.AsyncTwitter.AsyncTask) run).setStoped(true);
            	}            	
            }            
            connections.clear();            
        }
    }
    
    public void addRunnable(Integer uid, Runnable run) 
    {
		synchronized(runs)
		{
			runs.put(uid, run);
		}
	}
    
    public void addHttpConnection(int UID, HttpURLConnection con)
    {
        if(DEBUG)
        Log.d("********twitter-network", "add UID="+UID+" connection="+con.getURL() +" this="+this);
        synchronized(connections)
        {
            connections.put(UID, con);            
        }
    }
    public void releaseHttpConnection(int UID)
    {
        if(DEBUG)
        Log.d("********twitter-network", "remove connection="+UID+" this="+this);
        synchronized(connections)
        {
            connections.remove(UID);            
        }
        
        synchronized(runs)
		{
			runs.remove(UID);
		}
    }
    
    
    public void titleUpdateAfterNetwork()
	{
		 updateProgress(DATA_READY_PROGRESS*100);			 
         //updateTitle(TITLE_FINISH_NETWORK);
	}

    //for activity, for service need take care of the return code
	public void ProcessInvaidSession() 
	{		
		Log.d(TAG, "Invalid session, need relogin");
    	
    	Message msg = basichandler.obtainMessage(INVALID_SESSION);    	
    	msg.sendToTarget();
	}
	public void ProcessPermissionError(final String errorMsg)
	{
	    if(ActivityBase.this.isFinishing() == false)
        {
	        Log.d(TAG, "create ");
	        basichandler.post(new Runnable()
	        {
	            public void run()
	            {
	                Toast.makeText(ActivityBase.this, errorMsg, Toast.LENGTH_LONG).show();
	            }  
	        });
        }
	    else
	    {
	        Log.d(TAG, " ProcessPermissionError ActivityBase is finished");
	    }
	    
	}
	
	public void ProcessNoExtPermission(final String permission) 
	{	
	    
	}
	
	public static boolean isInAynscTaskAndStoped()
	{
		 boolean isStoped = false;
		 Thread th = Thread.currentThread(); 
         if(PoolThread.class.isInstance(th))
         {
         	PoolThread pt = (PoolThread)(th);
         	Runnable run = pt.currentRunning();
         	if(run != null && twitter4j.AsyncTwitter.AsyncTask.class.isInstance(run))
        	{
         		isStoped = ((twitter4j.AsyncTwitter.AsyncTask) run).Stoped();
        	}        	
         }
         
         return isStoped;
	}
}
