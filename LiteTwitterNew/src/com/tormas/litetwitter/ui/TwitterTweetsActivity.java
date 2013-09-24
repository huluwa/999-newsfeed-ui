package com.tormas.litetwitter.ui;

import com.tormas.litetwitter.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tormas.litetwitter.providers.SocialORM;
import com.tormas.litetwitter.ui.TwitterAccountListener.TwitterAccountManager;
import com.tormas.litetwitter.ui.adapter.SimplyStatusAdapter;
import com.tormas.litetwitter.util.SeSimplyUser;
import com.tormas.litetwitter.util.SeStatus;

import twitter4j.AndroidAsyncTwitter;
import twitter4j.AsyncTwitter;
import twitter4j.Paging;
import twitter4j.SimplyStatus;
import twitter4j.SimplyUser;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class TwitterTweetsActivity  extends StatusViewBaseActivity {
	private final String TAG="TwitterTweetsActivity";
    ListView tweets;
    Button   sendButton;
    
	//get from database
	private  long timetoload  = 60*1000;
	private  final long FriendCount = 100;
	
	private int lastVisiblePos  = 0;
	private boolean withfootview = true;
	
	//performance enhancement, only get the updated	
	private List<SimplyStatus> currentStatus = new ArrayList<SimplyStatus>();
	private List<SimplyStatus> searchStatus = new ArrayList<SimplyStatus>();
	
	String uid;
	String friendline_uid=null;
	String userline_uid  =null;
	private Menu statusMenu;
	private EditText sendcontent;
	private TextView textCount;
	private MyWatcher watcher;
	private View twitter_compose_span;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_tweets);
        View view = this.findViewById(R.id.twitter_tweets_layout);
        view.setKeepScreenOn(true);   
        tweets = (ListView)this.findViewById(R.id.twitter_tweets_list);
        tweets.setFocusableInTouchMode(true);
        tweets.setFocusable(true);
        tweets.setOnCreateContextMenuListener(this); 
        tweets.setOnItemClickListener(statusTweetsClickListener);

        sendButton = (Button)this.findViewById(R.id.status_update);
        textCount = (TextView)this.findViewById(R.id.tweets_message_text_counter);
        sendcontent = (EditText) this.findViewById(R.id.twitter_tweet_message_editor);
        sendcontent.setHint(R.string.twitter_compose_tweet_hint);
        InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(defaultTextLength)};
        sendcontent.setFilters(filters);   
        sendcontent.setVerticalScrollBarEnabled(true);
        watcher = new MyWatcher(); 	    
        sendcontent.addTextChangedListener(watcher);
        sendButton.setOnClickListener(statusUpdateOnClik);
        
        twitter_compose_span = this.findViewById(R.id.twitter_compose_span);
        twitter_compose_span.setVisibility(View.VISIBLE);
        processIntent();
        
        setTitle(finalTitle);
//        buildCurrentStatus();
//        Log.d(TAG,"currentStatus size is "+currentStatus.size());
//        handler.obtainMessage(TWEET_CREATE_UI).sendToTarget();
    }
    
    private void buildCurrentStatus(){
        //currentStatus = new ArrayList<SimplyStatus>();
       try{
           SimplyUser user = new SimplyUser();
           user.id = 0;
           user.name = "kk";
           user.screenName = "kk";
           user.profileImageUrl = "";
           for(int i = 0;i <2;i++)
           {
                SimplyStatus  si = new SimplyStatus();
                si.id = i;
                si.createdAt = new Date();
                si.text = " just a test "+i;
               
                si.user = user;
                currentStatus.add(si);
           }
       }
       catch(Exception e){
           Log.d(TAG,"build status exception");
       }
   }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) 
    {    
    
        // bind search key action - android sends 'ENTER' key since the search key is taking
        // the place of the ENTER Key on the keyboard for this edit text.
        // Make sure the search criteria is not "empty"
        String searchCriteria = sendcontent.getText().toString().trim();
        if(event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && searchCriteria.length() > 0)
        {
            sendMessage();
        }
        return super.dispatchKeyEvent(event);
    }
    
    private void processIntent()
    {   
        String preuid = uid;
    	SocialORM.Account account = orm.getTwitterAccount();
        //FriendCount = 100;//orm.getTweetViewCount();
        timetoload = orm.getTweetTimeout();
 		uid = account.screenname;
 		
 		//default to get the friend line,
		//from detail view his friend line
 		
		String lUid = this.getIntent().getStringExtra("friendline_uid");
		
		if(lUid != null)
		{   
		    //just for this case home-->twitter-->my account-->update status
		    // need clear CurrentTweets
		    if(friendline_uid == null && userline_uid == null)
	        {
		        clearCurrentTweets();
	        }		    

			friendline_uid = lUid;
			uid = friendline_uid;
			//clear use line
			if(userline_uid != null)
			{
			    clearCurrentTweets();
			    userline_uid = null;
			}
			Log.d(TAG, "friendline_uid="+friendline_uid);
		}
		//get user line status
		lUid = this.getIntent().getStringExtra("userline_uid");
		if(lUid != null)
		{
		    //just for this case home-->twitter-->my account-->update status
            // need clear CurrentTweets
		    if(friendline_uid == null && userline_uid == null)
            {
                clearCurrentTweets();
            }
			userline_uid = lUid;
			uid =  userline_uid;
			
			//for user line clear friend line
			if(friendline_uid != null)
			{
			    friendline_uid = null;
			    clearCurrentTweets();
			}
			Log.d(TAG, "userline_uid="+userline_uid);
		}		
		//this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.brownbird_left);
		if(checkTwitterAccount(this, account) == true)
		{
			if(twitterid_db.equalsIgnoreCase(uid))
	        {
			    setTitle(R.string.twitter_tweets_title);
	        }
	        else
	        {
	        	setTitle(R.string.Tweets_Title_direct_message);
	        }
			if(preuid!=null && false == preuid.equalsIgnoreCase(uid))
			{
			    clearCurrentTweets();
			}
			if(needSerialization() == true)
			{
			    new DeSerializationTask().execute((Void[])null);
			}
			launchTweetsLoad();
		}		
		
		//remember the final title
		if(uid!=null && uid.equals(twitterid_db))
		{
		    twitter_compose_span.setVisibility(View.VISIBLE);
		}
		else
		{
		    twitter_compose_span.setVisibility(View.GONE);
		}
		setTitle();
    }
    
    private void clearCurrentTweets()
    { 
        lastVisiblePos = 0;
        curTwitterPage = 1;      
        if(currentStatus != null && currentStatus.size()>0)
        {
            for(SimplyStatus item :currentStatus)
            {
                item.despose();
                item = null;
            }
            currentStatus.clear();
        }
        SimplyStatusAdapter ta = new SimplyStatusAdapter(TwitterTweetsActivity.this, (ArrayList<SimplyStatus>)currentStatus);
        tweets.setAdapter(ta);
    }
    public void reschedule()
	{
    	Log.d(TAG, "reschedule at "+ new Date().toLocaleString());
		long nexttime = System.currentTimeMillis()+ 1000;		
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);	
		        
		Intent i = new Intent();
        i.setClassName("com.tormas.litetwitter", "com.tormas.litetwitter.ui.TwitterTweetsActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent onepid = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmMgr.set(AlarmManager.RTC, nexttime,onepid);	        
	}
	
    static int nTimes;
	public void alarmComming()
	{
		nTimes ++;
		Log.d(TAG, "tims="+nTimes+"  &&&&&&&&&&& time it out="+ new Date().toLocaleString());
		Message msg = handler.obtainMessage(TWEET_LOAD_INTERAL);
		msg.sendToTarget();
		
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
		long nexttime = System.currentTimeMillis()+ orm.getTweetTimeout();
		
		Intent i = new Intent();
		i.setClassName("com.tormas.litetwitter", "com.tormas.litetwitter.ui.TwitterTweetsActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent onepid = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmMgr.set(AlarmManager.RTC, nexttime,onepid);	   
	}
    
    @Override
	protected void onNewIntent(Intent intent) 
    {
    	Log.d(TAG, "new request for tweet detail view="+intent);
    	super.onNewIntent(intent);
		if(intent.getAction() != null && intent.getAction().equals(ACTION_CHECK_CONTECT))
		{
			setIntent(intent);
		}
		else
		{
			setIntent(intent);
			processIntent();
		}
	}

	//when resume, will recall the tweets,
	//to save battery, if the trend is in background, don't call the message 
    @Override protected void onResume() 
    {
    	super.onResume();
    	//cancel first
    	cancelAlarming();
        //resume to call
    	alarmComming();    	
    }
    
    private void cancelAlarming()
    {
        Log.d(TAG, "cancel alarming firstly");
        AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent();
        i.setClassName("com.tormas.litetwitter", "com.tormas.litetwitter.ui.TwitterTweetsActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pi);
    }
    
    public View.OnClickListener loadOlderClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            Log.d(TAG, "load older message");               
            loadOldPost();
        }
    };
    
    private void loadOldPost()
    {
        if(currentStatus != null && currentStatus.size() > 0)
        {
            //if for load old, no loop again, if user need to update the UI in interval, they need press refresh 
            cancelAlarming();
            
            this.lastVisiblePos = tweets.getFirstVisiblePosition()+1;           
            Log.d(TAG, "pos="+lastVisiblePos);
            
            Log.d(TAG, "by max id");
            callGetTweetsMaxID();
        }
        else
        {
            Log.d(TAG, "no content, so just refresh");            
            alarmComming();
        }
    }
    @Override protected void onPause() 
    {   
        super.onPause();
        serialization();
        cancelAlarming();
    }
  
    
    @Override protected void onDestroy() 
    {   
        super.onDestroy();
        
        //do serialization when user is current session user
        serialization();
        
        tweets.setAdapter(null);
        //tweets = null;
        
        if(currentStatus != null)
        {
            currentStatus.clear();
            currentStatus = null;
        }        
        cancelAlarming();
    }	
    
    @Override
    public void onLowMemory() {     
        super.onLowMemory();
        
        //if memory less than 2M, release UI
        Log.d(TAG, "onLowMemory");
        freeMemory();
    }
    
    private void freeMemory()
    {
        cancelAlarming();
        
        lastVisiblePos = 0;
        Toast.makeText(this, "Low memory", Toast.LENGTH_SHORT).show();
        if(currentStatus != null)
        {
            synchronized(currentStatus)
            {
                tweets.setAdapter(null);    
                
                while(currentStatus.size() > FriendCount)
                {
                    int pos = currentStatus.size() -1;
                    SimplyStatus item = currentStatus.get(pos);
                    item.despose();
                    item = null;
                    currentStatus.remove(pos);
                }           
                handler.obtainMessage(TWEET_CREATE_UI).sendToTarget();
            }
            
            System.gc();
            reschedule();
        }        
    }
    public void setTitle() 
	{
    	finalTitle = getString(R.string.twitter_tweets_title);
    	if(twitterid_db != null && uid != null)
    	{
			if(twitterid_db.equalsIgnoreCase(uid))
	        {
			    finalTitle = getString(R.string.twitter_tweets_title);		   
	        }
	        else
	        {
	        	 finalTitle = getString(R.string.Tweets_Title_direct_message);        	
	        }
    	}
	}   
    
    View.OnClickListener statusUpdateOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			sendMessage();
		}
	};
    
    public void titleSelected() 
    {		
		if(uid!=null && uid.equals(twitterid_db))
		{ 
		    //sendMessage();
		    //DO nothing
		}
		else
		{
		    Intent intent = new Intent(TwitterTweetsActivity.this, TwitterComposeActivity.class);
            intent.putExtra(TWITTER_ID, uid);        
            intent.putExtra(DIRECT, true);
            startActivityForResult(intent, TWITTER_DONOTHING);
		}
	}
    
    private void sendMessage()
    {
    	String content = sendcontent.getText().toString().trim();
    	if(content!=null && content.length()>0)
    	{
    	    //TODO show dialog update status
    	    showDialog(TWITTER_UPDATE_STATUS_DLG);
    		Message msg = handler.obtainMessage(TWEET_UPDATE);
    		msg.getData().putString("content", content);
    		msg.sendToTarget();
    	}
    	
		
    }

	protected void loadNextPage()
	{
		super.loadNextPage();
		launchTweetsLoad();
	}
	protected void loadPrePage()
	{
		super.loadPrePage();
		launchTweetsLoad();
	}
    
   /* @Override public boolean onPrepareOptionsMenu(Menu menu)
    {    	
    	 super.onPrepareOptionsMenu(menu);
    	 //don't show for user line's page
    	 if(userline_uid != null)
         {
             menu.findItem(R.id.menu_next_page).setVisible(false);   
    	     menu.findItem(R.id.menu_pre_page).setVisible(false);
         }
    	 return true;
    }*/
    
	@Override
    public void createHandler() 
    {
        handler = new HandlerLoad();        
    }
    
    @Override public void loadAction()
    {
    	super.loadAction();
    	
    	//prompt no UI
    	if(twitterA == null)
		{
			SocialORM.Account account = orm.getTwitterAccount();
			if(checkTwitterAccount(TwitterTweetsActivity.this, account, true) == false)		
			{
				return;
			}
		}	 
		
		reschedule();
    }

    @Override
    protected void loadAfterSetting()
    {
        super.loadAfterSetting();
        twitterA = null;
        
        loadAction();
    }
    
    @Override
	protected void loadRefresh()
	{
	    super.loadRefresh();
		if(twitterA == null)
		{
			SocialORM.Account account = orm.getTwitterAccount();
			if(checkTwitterAccount(TwitterTweetsActivity.this, account) == false)		
			{
				return;
			}
		}	
		lastVisiblePos = tweets.getFirstVisiblePosition();    
		reschedule();
	}

    private void notifyLoading() 
    {
    	notify.notifyOnce(R.string.twitter_tweets_loading, R.drawable.twitter, 30*1000);		
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        switch(requestCode)
        {
            case TWEET_DETAIL:
            {
            	//TODO
            	//return from tweets details
            	Log.d(TAG, "return from detail view");
            	break;
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
    
	private void launchTweetsLoad() 
	{
		if(twitterA == null)
		{
			SocialORM.Account account = orm.getTwitterAccount();			
			if(checkTwitterAccount(TwitterTweetsActivity.this, account))
			{
				twitterA = new AsyncTwitter(account.token, account.token_secret,true);				
			}
			else
			{
				return;
			}    
		}	        
		reschedule();
	}
   
    private class HandlerLoad extends Handler 
	{
		public HandlerLoad()
		{
			super();
			
			Log.d(TAG, "new HandlerLoad");
		}
		
		@Override
		public void handleMessage(Message msg) 
		{
			switch(msg.what)
			{
			    case TWEET_LOAD_INTERAL:
			    {
			    	Log.d(TAG, "before get tweets TWEET_LOAD_INTERAL ");
			    	this.removeMessages(TWEET_LOAD_INTERAL);
			    	if(isInProcess())
			    	{
			    		Log.d(TAG, "I am still in loading");	
			    		return;
			    	}
			    	//just get the updated
			    	if(isBackgroud() == false)
			    	{	
			    	    callGetTweetsLatest();//get latest tweets
			    	}
			    	else
			    	{
			    	    Log.d(TAG, "%%& i am in background, will not call the twitter network");
			    	}
			    	break;
			    }
			    case TWEET_CREATE_UI:
			    {
			    	Log.d(TAG, "After get friends status");
			    	if(currentStatus != null)
			    	{
			    		SimplyStatusAdapter ta = new SimplyStatusAdapter(TwitterTweetsActivity.this, (ArrayList<SimplyStatus>)currentStatus);
    	            	ta.withfootview = withfootview;
			    		tweets.setAdapter(ta);    	            	
    	            	tweets.setSelection(lastVisiblePos);
			    	}
			    	else
			    	{
			    	    Log.e(TAG, "why come here, TWEET_CREATE_UI");
			    	}
			    	
	            	break;
			    }
				case  TWEET_LOAD_end:
				{
					end();
					//restore the network
				    twitterA.resumeCallNetWork();
					dissDlg(DLG_TWEET_LOADING);	
					showFooterViewText(getString(R.string.load_older_msg));
					break;
				}
				case TWEET_FAVOR_END:
				{
				    Log.d(TAG, "return from favor");
				    break;
				}
				case TWEET_UNFAVOR_END:
                {
                    Log.d(TAG, "return from destory favor");
                    break;
                }
				case TWEET_FOLLOW_END:
                {
                    Log.d(TAG, "return from follow");
                    break;
                }
                case TWEET_UNFOLLOW_END:
                {
                    Log.d(TAG, "return from destory follow");
                    break;
                }
                case TWEET_UPDATE:
                {
                	String content = msg.getData().getString("content");
                	updateStatus(content);                	
                	break;
                }
                case TWEET_UPDATE_END:
                {
                	end();
                	dismissDialog(TWITTER_UPDATE_STATUS_DLG);
                	boolean result = msg.getData().getBoolean(RESULT);
                	if(result)
                	{
                		sendcontent.setText("");
                		Toast.makeText(TwitterTweetsActivity.this, R.string.sns_operate_succeed, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String errormsg = msg.getData().getString("errormsg");
                        Toast.makeText(TwitterTweetsActivity.this, getString(R.string.sns_operate_failed) + ", " + errormsg , Toast.LENGTH_SHORT).show();
                    }
                	break;
                }
			}			
	    }

		private void dissDlg(int dlgTweetLoading) 
		{
			//TwitterTweetsActivity.this.dismissDialog(DLG_TWEET_LOADING);
		}
	}
    
    private void showFooterViewText(String text) {
       if(tweets == null) return;
        for(int i= tweets.getChildCount()-1;i>0;i--)            
        {
            View v = tweets.getChildAt(i);
            if(Button.class.isInstance(v))
            {
                Button bt = (Button)v;
                bt.setText(text);
                break;
            }
        } 
    }
    
    @Override
    public synchronized boolean  isInProcess()
    {
        return inprocess || inprocessold;       
    }
    
    boolean inprocessold = false;
    private void callGetTweetsMaxID()
    {
	    if(inprocessold == true || inprocess == true)
	    {
		    Log.d(TAG, "I am process get old");
		    return ;
    	}
    	
        if(twitterA == null)
        {
            Log.d(TAG, "your twitter are not login, please login firstly");
            return ;            
        }
       
        long maxID = this.getMaxID();
        if(maxID == -1)
        {
            alarmComming();
        }
        else
        {
            begin();
            showFooterViewText(getString(R.string.loading_string));
            Log.d(TAG, "before get tweets callGetTweets");
            
            notifyLoading(); 
            synchronized(mLock)
            {
                inprocessold = true;
            }
            getFriendStatusLineUpdateByMaxID(maxID); 
        }
    }
    
    private long getLatestID()
    {
        long sinceID = -1;
        if(currentStatus != null && currentStatus.size()>0)
        {  
            if(currentStatus.get(0).isFromSerialize == false)
            {
                sinceID = currentStatus.get(0).id+1;
            }
        }
        return sinceID;
    }
    
    private long getMaxID()
    {
        long maxid = -1;
        if(currentStatus != null && currentStatus.size()>0)
        {
            if(currentStatus.get(0).isFromSerialize == false)
            {
                maxid = currentStatus.get(currentStatus.size() -1).id -1;
            }
           
        }        
        return maxid;
    }
    
    private void callGetTweetsLatest()
    {
    	if(twitterA == null)
    	{
    		Log.d(TAG, "your twitter are not login, please login firstly");
    		return ;    		
    	}
    	
    	begin();		
    	
    	showFooterViewText(getString(R.string.loading_string));
    	
    	Log.d(TAG, "before get tweets callGetTweetsLatest");
    	notifyLoading();  
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}
    	long sinceID = getLatestID();
    	if(sinceID == -1)
    	{
    		getFriendStatusLine();
    	}
    	else
    	{
    		getFriendStatusLineUpdate(sinceID);
    	}
    }
    
    public void updateStatus(String content) 
    {
    	if(twitterA == null)
    	{
    		Log.d(TAG, "your twitter are not login 2, please login firstly");
    		return ;
    	}
    	begin();    	
    	twitterA.updateStatusAsync(content, new TwitterAdapter() 
        {
        	 @Override public void  updatedStatus(Status status)
             {
                Log.d(TAG, "after update status="+status);
                Message smd = handler.obtainMessage(TWEET_UPDATE_END);
                smd.getData().putBoolean(RESULT, true);
                handler.sendMessage(smd);
             }
            
             @Override public void onException(TwitterException e, int method) 
             {                   
                Log.d(TAG, "Fail to updated ="+e.getMessage());  
                if(isInAynscTaskAndStoped())
             	{
             		Log.d(TAG, "User stop passive");
             	}
             	else
             	{
	                Message smd = handler.obtainMessage(TWEET_UPDATE_END);
	                smd.getData().putBoolean(RESULT, false);
	                smd.getData().putString("errormsg", e.getMessage());
	                handler.sendMessage(smd);
             	}
             }           
        });        	
		
	}

	private void processTwitterException(TwitterException e, int method)
    {
    	synchronized(mLock)
    	{
    	    inprocess = false;
    	}
    	
    	if(isInAynscTaskAndStoped())
     	{
     		Log.d(TAG, "User stop passive");
     	}
     	else
     	{
    	    handler.obtainMessage(TWEET_LOAD_end).sendToTarget();     	
	    
	        if(isBackgroud() == false)//I am still alive
	        {            	
	        	cancelNotify();
	        	Log.d(TAG, "Fail to get ="+e.getMessage());
	        }
     	}
    }
    
	private void getFriendStatusLineUpdateByMaxID(final long maxID)
	{
	    Paging page = new Paging();       
	    page.setMaxId(maxID);
	    page.setCount(20);
        
        if(friendline_uid != null || (friendline_uid==null && userline_uid==null))
        {
            twitterA.getSimplyFriendsTimelineAsync(uid,page , new TwitterAdapter() 
            {
                @Override public void gotSimplyFriendsTimeline(List<SimplyStatus> statuses)
                {
                    synchronized(mLock)
                    {
                    	inprocessold = false;
                    }
                    
                    if(donotcallnetwork == false)//I am still alive
                    {
                        Log.d(TAG, "After get friends update status from maxid="+maxID + " count="+statuses.size());
                        cancelNotify();
                        if(statuses.size() > 0)
                        {
                            getSecondViewStatus(statuses, true);                                               
                            handler.obtainMessage(TWEET_CREATE_UI).sendToTarget();
                        }
                    }
                    
                    handler.obtainMessage(TWEET_LOAD_end).sendToTarget();
                }
    
                @Override public void onException(TwitterException e, int method) 
                {
                	synchronized(mLock)
                    {
                	    inprocessold = false;
                    }
                    processTwitterException(e, method);
                }           
            });    
        }
        else if(userline_uid != null)
        {
            twitterA.getSimplyUserTimelineAsync(uid,page , new TwitterAdapter() 
            {
                @Override public void gotSimplyUserTimeline(List<SimplyStatus> statuses)
                {
                    synchronized(mLock)
                    {
                    	inprocessold = false;
                    }
                    
                    if(donotcallnetwork == false)//I am still alive
                    {
                        Log.d(TAG, "After get friends update status from maxid="+ maxID + " count="+statuses.size());
                        cancelNotify();
                        if(statuses.size() > 0)
                        {
                            getSecondViewStatus(statuses, true);
                            handler.obtainMessage(TWEET_CREATE_UI).sendToTarget();
                        }
                    }        
                    
                    handler.obtainMessage(TWEET_LOAD_end).sendToTarget();
                }
    
                @Override public void onException(TwitterException e, int method) 
                {
                	synchronized(mLock)
                    {
                	    inprocessold = false;
                    }
                    processTwitterException(e, method);
                }           
            });    
        }
	}
	
    private void getFriendStatusLineUpdate(final long sinceID)
    {	
    	Paging page = new Paging();
    	page.setSinceId(sinceID);
    	page.setCount(20);
    	if(friendline_uid != null || (friendline_uid==null && userline_uid==null))
    	{
	        twitterA.getSimplyFriendsTimelineAsync(uid, page , new TwitterAdapter() 
	        {
				@Override public void gotSimplyFriendsTimeline(List<SimplyStatus> statuses)
	            {
					synchronized(mLock)
			    	{
			    	    inprocess = false;
			    	}
					
                    if(donotcallnetwork == false)//I am still alive
                    {
    	            	Log.d(TAG, "After get friends update status from sinceid="+sinceID + " count="+statuses.size());
    	            	cancelNotify();
    	            	if(statuses.size() > 0)
    	            	{
        	            	getSecondViewStatus(statuses, false);
        	            	handler.obtainMessage(TWEET_CREATE_UI).sendToTarget();
    	            	}
                    }  
                    handler.obtainMessage(TWEET_LOAD_end).sendToTarget();
	            }
	
	            @Override public void onException(TwitterException e, int method) 
	            {
	            	processTwitterException(e, method);
	            }			
	        });    
    	}
    	else if(userline_uid != null)
    	{
    		 twitterA.getSimplyUserTimelineAsync(uid, page , new TwitterAdapter() 
 	        {
 				@Override public void gotSimplyUserTimeline(List<SimplyStatus> statuses)
 	            {
 					synchronized(mLock)
 			    	{
 			    	    inprocess = false;
 			    	}
 					
                    if(donotcallnetwork == false)//I am still alive
                    {
     	            	Log.d(TAG, "After get friends update status from sinceID="+sinceID + " count="+statuses.size());
     	            	cancelNotify();   
     	            	if(statuses.size() > 0)
     	            	{
     	            	    getSecondViewStatus(statuses, false);
     	            	    handler.obtainMessage(TWEET_CREATE_UI).sendToTarget();
     	            	}
                    }
                    handler.obtainMessage(TWEET_LOAD_end).sendToTarget();
 	            }
 	
 	            @Override public void onException(TwitterException e, int method) 
 	            {
 	            	processTwitterException(e, method);
 	            }			
 	        });    
    	}
    }

	private void getFriendStatusLine()
    {	
    	if(friendline_uid != null || (friendline_uid==null && userline_uid==null))
    	{
	        twitterA.getSimplyFriendsTimelineByPageAsync(uid, this.curTwitterPage, new TwitterAdapter() 
	        {
				@Override public void gotSimplyFriendsTimeline(List<SimplyStatus> statuses)
	            {
					synchronized(mLock)
			    	{
			    	    inprocess = false;
			    	}
					
                    if(isBackgroud() == false)//I am still alive
                    {
                    	if(statuses.size() > 0)
                    	{
	    					//just need the count
	    					getFirstViewStatus(statuses);      	
	    					
	    					//TODO if uid is myself doserize
	    					
	    					handler.obtainMessage(TWEET_CREATE_UI).sendToTarget();
                    	}
    	            	cancelNotify();
                    }
                    handler.obtainMessage(TWEET_LOAD_end).sendToTarget();                    
	            }
	
	            @Override public void onException(TwitterException e, int method) 
	            {
	            	//if exception, the time will be not right, will take the request time
	                try {
                        if(failCallMethod!=null)
                        {
                               failCallMethod.invoke(TwitterTweetsActivity.this, (Object[])null);
                        }
                    } catch (IllegalArgumentException e1) {} 
                    catch (IllegalAccessException e1) {} 
                    catch (InvocationTargetException e1) {}
                                    
	            	processTwitterException(e, method);	            	
	            }
	        });       	 
    	}
    	else if(userline_uid != null)
    	{
    		 twitterA.getSimplyUserTimelineAsync(uid, new TwitterAdapter() 
 	         {
 				@Override public void gotSimplyUserTimeline(List<SimplyStatus> statuses)
 	            {
 					synchronized(mLock)
 			    	{
 			    	    inprocess = false;
 			    	}
 					
                    if(isBackgroud() == false)//I am still alive
                    {
     	            	Log.d(TAG, "After get friends status size = "+statuses.size());
     	            	
     	            	if(statuses.size() > 0)
     	            	{
	     	            	getFirstViewStatus(statuses);
	                        
	     	            	handler.obtainMessage(TWEET_CREATE_UI).sendToTarget();
     	            	}
     	            	cancelNotify();     	            	
     	            	
                    }
                    handler.obtainMessage(TWEET_LOAD_end).sendToTarget();                    
 	            }
 	
 	            @Override public void onException(TwitterException e, int method) 
 	            {
 	            	//if exception, the time will be not right, will take the request time
 	            	processTwitterException(e, method);
 	            }
 	        });       	 
    	}
    }
	
	private void getSecondViewStatus(List<SimplyStatus> statuses, boolean isforold)
	{
	    handler.post( new Runnable()
        {
            public void run()
            {
                tweets.setAdapter(null);
            }
        });
	    
        synchronized(currentStatus)
        {
            int currentStatusSize = currentStatus.size();
            if(currentStatusSize >= FriendCount)
            {
                for(int i=currentStatusSize-1;i>=currentStatusSize-20;i--)
                {
                    SimplyStatus st = currentStatus.get(i);
                    st.despose();
                    st = null;
                    currentStatus.remove(i);                        
                }
            }
            
    		//append status into currentStatus, keep just 20 items           
    	    if(isforold == false)
            {   
            	
                for(int i=statuses.size()-1;i>=0;i--)
                {   
                    currentStatus.add(0, statuses.get(i));         
                }
            }
    	    else//for old
    	    {
    	        for(int i=0;i<statuses.size();i++)
                {
                	currentStatus.add(statuses.get(i));         
                }
    	    }
        }        
	}
	
	private void getFirstViewStatus(List<SimplyStatus> statuses)
	{
	    handler.post( new Runnable()
	    {
    	    public void run()
    	    {
    	        tweets.setAdapter(null);
    	    }
	    });
	    
	    synchronized(currentStatus)
	    {
    	    //just need the count
    	    if(currentStatus != null)
    	    {
    	        currentStatus.clear();	        
    	    }
    	    
    	    //remove all serialization status
            Log.d(TAG, "After get friends status size is "+ statuses.size());
            
            int pos=0;
            while(pos < FriendCount && pos <statuses.size())
            {           
                currentStatus.add(statuses.get(pos));
                pos++;
            }
            while(pos <statuses.size())
            {
            	SimplyStatus st = statuses.get(pos);
                st = null;
                statuses.remove(pos);
                pos ++;
            }
            statuses = null;        
	    }
	}
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {   
            this.stopLoading();             
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

	@Override
	public void stopMyself() 
	{	    
		if(isInProcess() == true)
		{
            stoping();
		}
		
		cancelAlarming();
	}
	
	private class MyWatcher implements TextWatcher 
	{   
       public void afterTextChanged(Editable s) 
       {
    	   textCount.setText(String.format("%1$s", defaultTextLength-s.length()));    	  
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
         //super.onCreateOptionsMenu(menu);        
         //menu.setOptionalIconsVisible(false);      
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.twitter_main, menu);
         return true;
     }
	
	@Override public boolean onPrepareOptionsMenu(Menu menu)
    {       
        // super.onPrepareOptionsMenu(menu);
         
         if(inprocess == true)
         {
             menu.findItem(R.id.menu_trend_main_stop).setVisible(true);
             menu.findItem(R.id.menu_trend_main_refresh).setVisible(false);
         }
         else
         {      
             menu.findItem(R.id.menu_trend_main_stop).setVisible(false);
             menu.findItem(R.id.menu_trend_main_refresh).setVisible(true);
         }
         
         //don't show setting
         menu.findItem(R.id.menu_twitter_settings).setVisible(true);   
         menu.findItem(R.id.menu_following).setVisible(false);
         menu.findItem(R.id.menu_follower).setVisible(false);
         //menu.findItem(R.id.menu_twitter_about).setVisible(true);
         menu.findItem(R.id.twitter_log_out).setVisible(true);
         return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
            case R.id.menu_twitter_upload_photo:
            {
                Intent intent = new Intent(this, TwitterUploadPictureActivity.class);               
                startActivity(intent);                  
                break;
            }
            
            case R.id.menu_twitter_settings:
            {
                Intent intent = new Intent(this, TwitterSettingPreference.class);
                intent.putExtra("foraccount", true);
                startActivityForResult(intent, TWITTER_SETTING);                
                break;
            }
            case R.id.menu_twitter_myaccount:
            {
                Intent intent = new Intent(this, TwitterMyAccountActivity.class);
                intent.putExtra("foraccount", true);
                startActivityForResult(intent, TWITTER_ACCOUNT);                
                break;
            }
            case R.id.menu_tweets:
            {
                Intent intent = new Intent(this, TwitterTweetsActivity.class);
                startActivityForResult(intent, TWITTER_TWEETS);             
                break;              
            }
            case R.id.menu_trends:
            {
                Intent intent = new Intent(this, TwitterMainActivity.class);
                startActivity(intent);    
                break;
            }
            case R.id.menu_compose_message:
            {
                Intent intent = new Intent(this, TwitterMessageActivity.class);
                intent.putExtra(MESSAGEVIEW, true);             
                startActivityForResult(intent, TWITTER_TWEETS); 
                break;
            }
            case R.id.menu_favorites:
            {
                Intent intent = new Intent(this, TwitterFavoritesActivity.class);
                intent.putExtra(FAVORITEVIEW, true);
                startActivityForResult(intent, TWITTER_TWEETS);  
                break;
            }
            case R.id.menu_follow_search:
            {
                Intent intent = new Intent(this, TwitterFollowActivity.class);
                intent.putExtra(FOLLOWER_SEARCH_VIEW, true);
                startActivityForResult(intent, TWITTER_FOLLOWER);    
                break;
            }
            case R.id.menu_following:
            {
                Intent intent = new Intent(this, TwitterFollowActivity.class);
                intent.putExtra(FOLLOWING_VIEW, true);
                intent.putExtra(TWITTER_ID, orm.getTwitterAccount().uid);
                
                startActivityForResult(intent, TWITTER_FOLLOWING);   
                break;
            }
            case R.id.menu_follower:
            {
                Intent intent = new Intent(this, TwitterFollowActivity.class);
                intent.putExtra(FOLLOWER_VIEW, true);
                intent.putExtra(TWITTER_ID,    orm.getTwitterAccount().uid);
                startActivityForResult(intent, TWITTER_FOLLOWER_SEARCH);     
                break;
            }
            case R.id.menu_trend_main_stop:
            {
                stopProcess();
                break;
            }
            case R.id.menu_trend_main_refresh:
            {
                loadRefresh();
                break;
            }
            case R.id.menu_search:
            {
                 //view details
                 Intent intent = new Intent(mContext, TwitterSearchActivity.class);
                 ((TwitterTweetsActivity)mContext).startActivityForResult(intent, TwitterTweetsActivity.TWITTER_TWEETS);
                 break;
            }
            case R.id.menu_twitter_about:
            {
                Intent intent = new Intent(mContext,AboutActivity.class);
                intent.putExtra("forabout",   true);
                intent.putExtra("fortwitter", true);
                mContext.startActivity(intent);
                break;
            }
            case R.id.twitter_log_out:
            {
                doLogout();
                break;
            }
        }
        return true;
    }
    
    private void doLogout() {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(
                R.string.twitter_logout_title).setMessage(
                getString(R.string.facebook_logout_message)).setPositiveButton(
                getString(R.string.sns_ok),
                new DialogInterface.OnClickListener() 
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                        // remove twitterid,twitter password from DB
                        orm.logoutTwitter();
                        Intent intent = new Intent(TwitterTweetsActivity.this, TwitterLoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityForResult(intent, TWITTER_LOGIN);
                        TwitterAccountManager.logout();
                    }
                }).setNegativeButton(getString(R.string.sns_cancel),
                new DialogInterface.OnClickListener() 
                {
                    public void onClick(DialogInterface dialog, int whichButton) {}
                }).create();
                dialog.show();
    }
    
    @Override
    public void onLogin() {
        super.onLogin();    
        SocialORM.Account account = orm.getTwitterAccount();
        //FriendCount = 100;//orm.getTweetViewCount();
        timetoload = orm.getTweetTimeout();
        uid = account.screenname;
        if(twitterA == null)
        {
            twitterA = new AndroidAsyncTwitter(account.token, account.token_secret,true);
            twitterA.attachActivity(TwitterTweetsActivity.this);
        }
        else
        {   
            twitterA.setUserId(account.email);
            twitterA.setPassword(account.password);
        }   
        mysname = account.screenname;
        twitterid_db = account.screenname;
        
        twitter_compose_span.setVisibility(View.VISIBLE);
        tweets.setAdapter(null);
        currentStatus.clear();
        setTitle();
        setTitle(finalTitle);
       //must be session user self
        new DeSerializationTask().execute((Void[])null);
        reschedule();
    }
    
    @Override protected void doAfterLoginNothing()
    {
        super.doAfterLoginNothing();
        TwitterTweetsActivity.this.setResult(RESULT_CANCELED);
        TwitterTweetsActivity.this.finish();    
    }
    
    private class DeSerializationTask extends android.os.AsyncTask<Void, Void, Void>
    {       
        public DeSerializationTask()
        {
            super();            
            Log.d(TAG, "create DeSerializationTask="+this);
        }

        @Override
        protected Void doInBackground(Void... params)             
        {
            deSerialization();
            return null;
        }
    }
    
    private void deSerialization()
    {
        String filepath = TwitterHelper.twitterfriendline+uid+".ser";
        synchronized(currentStatus)
        {
            FileInputStream fis = null;
            ObjectInputStream in = null;
            try{
                fis = new FileInputStream(filepath);
                in = new ObjectInputStream(fis);
                long lastrecord = in.readLong();
                Date now = new Date();
                
                if((now.getTime() -lastrecord) >2*24*60*60*1000L)
                {
                    Log.d(TAG, String.format("it is %1%s hours ago, ignore the data", (now.getTime() -lastrecord)/(60*60*1000)));
                    in.close();
                    return ;
                }
                
                int count = in.readInt();
                for(int i=0;i<count;i++)
                {
                    SeStatus seitem = (SeStatus) in.readObject();
                    SimplyStatus item = unFormatSeStatus(seitem);
                    currentStatus.add(item);
                }
                in.close();
                /*if(currentStatus.size()>0)
                {
                    //lasttime = currentStatus.get(0).updated_time+1000;
                }*/
                handler.obtainMessage(TWEET_CREATE_UI).sendToTarget();
            }
            catch(IOException ex)
            {
                Log.d(TAG, "deserialization fail="+ex.getMessage());
            }
            catch(ClassNotFoundException ex)
            {
                Log.d(TAG, "deserialization fail="+ex.getMessage());
            }
        }
    }
    
    private long last_ser_sid = 0;
    private void serialization()
    { 
        if(needSerialization() == false)
        {
            Log.d(TAG, "no need to searialize,  please check if it sis right? uid="+uid + 
                    " friend uid="+friendline_uid + " user uid="+userline_uid + " twitterid_db="+twitterid_db);
            return;
        }
        else if(currentStatus.size()>0 && last_ser_sid == currentStatus.get(0).id)
        {
            Log.d(TAG,"the current cache is the latest cache");
            return;
        }
        String filepath = TwitterHelper.twitterfriendline;
        String filename = uid + ".ser";
        File file = new File(filepath);
        if(file.exists() == false)
        {
            file.mkdir();
        }
        File file1 = new File(filepath,filename);
        if(file1.exists() == false)
        {
            try {
                file1.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Log.d(TAG, "enter serialization filepath si "+filepath);
        if(currentStatus != null && currentStatus.size() > 0)
        {
            synchronized(currentStatus)
            {
                FileOutputStream fos = null;
                ObjectOutputStream out = null;
                try
                {   
                    fos = new FileOutputStream(filepath+filename);
                    out = new ObjectOutputStream(fos);
                    Date date = new Date();
                    out.writeLong(date.getTime());
                    int count = currentStatus.size();
                    //just cache last 20 items
                    if(count > 20)                      
                        count = 20;
                    out.writeInt(count);
                    for(int i=0;i<count;i++)
                    {
                        SimplyStatus item = currentStatus.get(i);
                        SeStatus seitem = formatSeStatus(item);
                        out.writeObject(seitem);
                    }
                    
                    out.close();
                    last_ser_sid = currentStatus.get(0).getId();
                }
                catch(IOException ex)
                {
                    Log.d(TAG, "serialization fail= "+ex.getMessage());
                }
            }
        }
    }
    
    private SimplyStatus unFormatSeStatus(SeStatus seitem)
    {
        SimplyStatus item = null;
        try {
            if(seitem!=null)
            {
                item = new SimplyStatus();
                item.id = seitem.id;
                item.createdAt = seitem.createdAt;
                item.isFavorited = seitem.isFavorited;
                item.ismytweets = seitem.ismytweets;
                item.isFromSerialize = true;
                item.selected = seitem.selected;
                item.text = seitem.text;
                item.user = unFormatSeSimplyUser(seitem.user);
            }
            
        } catch (TwitterException e) {
            Log.d(TAG,"unformat SeStatus fialed "+e.getMessage());
        }
        
        return item;
    }
    
    private SimplyUser unFormatSeSimplyUser(SeSimplyUser seuser)
    {
        SimplyUser user = null;
        try {
            if(seuser != null)
            {
                user = new SimplyUser();
                user.description = seuser.description;
                user.following = seuser.following;
                user.id = seuser.id;
                user.name = seuser.name;
                user.notifications = seuser.notifications;
                user.profileImageUrl = seuser.profileImageUrl;
                user.screenName = seuser.screenName;
            }
        } catch (TwitterException e) {
            Log.d(TAG,"unformat SeSimplyUser fialed "+e.getMessage());
        }
        
        return user;
        
    }
    
    private SeStatus formatSeStatus(SimplyStatus item) {
         SeStatus seitem = new SeStatus();
         seitem.createdAt = item.createdAt;
         seitem.id = item.id;
         seitem.isFavorited = item.isFavorited;
         seitem.ismytweets = item.ismytweets;
         seitem.selected = item.selected;
         seitem.text = item.text;
         seitem.user = formatSeUser(item.user);
        return seitem;
    }

    private SeSimplyUser formatSeUser(SimplyUser user) {
         SeSimplyUser seuser = null;
         if(user!=null)
         {
             seuser = new SeSimplyUser();
             seuser.description = user.description;
             seuser.following = user.following;
             seuser.id = user.id;
             seuser.name = user.name;
             seuser.notifications = user.notifications;
             seuser.profileImageUrl = user.profileImageUrl;
             seuser.screenName = user.screenName;
         }
        return seuser;
    }

    private boolean needSerialization()
    {
        boolean needSerialization = false;
        if(isEmpty(twitterid_db)==false)
        {
            if(isEmpty(friendline_uid) == false && friendline_uid.equals(twitterid_db))
            {
                needSerialization = true;
            }
            else if(isEmpty(friendline_uid) == true && isEmpty(userline_uid) ==true)
            {
                needSerialization = true;
            }
        }
        return needSerialization;
    }
   
}
