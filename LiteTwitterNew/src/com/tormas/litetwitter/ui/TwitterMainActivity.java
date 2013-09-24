package com.tormas.litetwitter.ui;

import com.tormas.litetwitter.R;
import com.tormas.litetwitter.providers.SocialORM;
import com.tormas.litetwitter.providers.SocialORM.Account;
import com.tormas.litetwitter.providers.SocialORM.Trend;
import com.tormas.litetwitter.ui.AccountListener.AccountManager;
import com.tormas.litetwitter.ui.adapter.TrendsAdapter;
import com.tormas.litetwitter.ui.view.TrendItemView;
import twitter4j.AndroidAsyncTwitter;
import twitter4j.AsyncTwitter;
import twitter4j.Last10Trends;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.Last10Trends.TrendsItem;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

public class TwitterMainActivity extends TwitterBaseActivity {
    private final String TAG="TwitterMainActivity";
	
	ListView trendslist;
	HandlerLoad handler;	
	private long trendsTimeout;
	
	Last10Trends currentTweets;
	View     twitter_info_span;
	TextView twitter_info;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
	    setContentView(R.layout.twitter_main_ui);
	    
        trendslist = (ListView)this.findViewById(R.id.twitter_trends_list);        
        trendslist.setFocusableInTouchMode(true);
        trendslist.setFocusable(true);
        trendslist.setOnItemClickListener(trendOnClik);
        trendslist.setOnCreateContextMenuListener(this);
        
        twitter_info_span = (View)this.findViewById(R.id.twitter_info_span);
		twitter_info      = (TextView)this.findViewById(R.id.twitter_info);
        
        //check the setting        
        trendsTimeout = 60*1000;
		SocialORM.Account account = orm.getTwitterAccount();	
		trendsTimeout = orm.getTrendsTimeout();		
		
		if(checkTwitterAccount(this, account) == true)		
		{
	    	reschedule();
		}
		
		this.setTitle(R.string.twitter_main_title);
		//setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.twitter_title);		
    }  
    
    private void doNoContent()
    {
    	if(currentTweets == null || currentTweets.last_10.size() == 0)
    	{
    		twitter_info_span.setVisibility(View.VISIBLE);
    		twitter_info.setText(R.string.no_trends_hint);
    	}
    	else
    	{
    		twitter_info_span.setVisibility(View.GONE);
    	}
    }
    
	@Override
	protected void onNewIntent(Intent intent) 
	{		
		super.onNewIntent(intent);
		
		if(intent.getAction() != null && intent.getAction().equals(ACTION_CHECK_CONTECT))
		{
			setIntent(intent);	
		}
	}


	public void reschedule()
	{
		Log.d(TAG, "reschedule at "+ new Date().toLocaleString());
		long nexttime = System.currentTimeMillis()+ 100;		
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent();
        i.setClassName("com.tormas.litetwitter", "com.tormas.litetwitter.ui.TwitterMainActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        
		alarmMgr.set(AlarmManager.RTC, nexttime, pi);	        
	}
	
	static int nTimes;
	public void alarmComming()
	{
		if(twitter_info_span.isShown())
		{
			twitter_info.setText(R.string.twitter_get_last_trends);
		}
		
		nTimes++;
		Log.d(TAG, "tims="+nTimes+"  &&&&&&&&&&&  time it out="+ new Date().toLocaleString());
		Message msg = handler.obtainMessage(TREND_LOAD_TREND);
		msg.sendToTarget();
		
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
		long nexttime = System.currentTimeMillis()+ orm.getTrendsTimeout();	
		
		Intent i = new Intent();
        i.setClassName("com.tormas.litetwitter", "com.tormas.litetwitter.ui.TwitterMainActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        
		alarmMgr.set(AlarmManager.RTC, nexttime, pi);	    	
	}	
	
    public void setTitle() 
    {	
    	finalTitle = getString(R.string.twitter_main_title);
	}
    
    AdapterView.OnItemClickListener trendOnClik = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long id) 
		{			
			if(TrendItemView.class.isInstance(v))
			{
				 TrendItemView view = (TrendItemView)v;
				 
				 TrendsItem trend = view.getTrendsItem();
				 Log.d(TAG, "trendOnClik you click first one="+trend.link);	
				 
				 //view details
				 Intent intent = new Intent(mContext, TwitterSearchActivity.class);
				 intent.putExtra("currenttrendurl",  trend.link);
				 intent.putExtra("currenttrendname", trend.name);			 
				 
				 ((TwitterMainActivity)mContext).startActivityForResult(intent, TwitterMainActivity.TWITTER_TWEETS);
			}
		}
	};
    
	//when resume, will recall the trend,
	//to save battery, if the trend is in background, don't call the message 
    @Override protected void onResume() 
    {
    	super.onResume();	
    	
    	//cancel first
    	AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
    	Intent i = new Intent();
        i.setClassName("com.tormas.litetwitter", "com.tormas.litetwitter.ui.TwitterMainActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pi);
         
        //resume to call
    	alarmComming();    	
    }
    
    @Override protected void onPause() 
    {   
        super.onPause();
        
        AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
        
        Intent i = new Intent();
        i.setClassName("com.tormas.litetwitter", "com.tormas.litetwitter.ui.TwitterMainActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pi);
        
        Log.d(TAG, "Cancel alarm");
    }
    
    @Override protected void onDestroy() 
    {   
        super.onDestroy();
        AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
        
        Intent i = new Intent();
        i.setClassName("com.tormas.litetwitter", "com.tormas.litetwitter.ui.TwitterMainActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pi);
        
        Log.d(TAG, "Cancel alarm");
    }
	
    public void titleSelected() {
		//load tweets
		Intent intent = new Intent(this, TwitterTweetsActivity.class);
        startActivityForResult(intent, TWITTER_TWEETS);	
	}	

    //no UI
	@Override public void loadAction()
    {
    	super.loadAction();    	
    	
    	if(twitterA == null)
		{
			SocialORM.Account account = orm.getTwitterAccount();
			if(checkTwitterAccount(TwitterMainActivity.this, account, true) == false)		
			{
				//return ;
			}
		}	 
    	
    	reschedule();
    }
	
	 @Override
    protected void loadAfterSettingNoChange()
    {
        TwitterMainActivity.this.setResult(RESULT_CANCELED);
        TwitterMainActivity.this.finish();      
        Log.d(TAG , "loadAfterSettingNoChange");
    }
    
	//have UI
	@Override
	protected void loadRefresh()
	{
		super.loadRefresh();
			
		reschedule();
	}
	
    @Override
    protected void loadAfterSetting()
    {
        super.loadAfterSetting();
        twitterA = null;
        launchTrendsLoad();
    }

    private void launchTrendsLoad() 
    {
    	if(twitterA == null)
		{
			SocialORM.Account account = orm.getTwitterAccount();
			if(checkTwitterAccount(TwitterMainActivity.this, account) == true)		
			{
				//return ;
			}
		}	 
    	
    	reschedule();
	}
    
    boolean doIneedRefreshTrends= false;
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
    	  doIneedRefreshTrends = false;
	      switch(requestCode)
	      {  
	           /* case TWITTER_SETTING:
	            {	            	
	            	//back from setting, to refresh
	            	//reschedule();            		            	
	            	break;
	            }*/
	            case TWITTER_TWEETS:
	            {
	            	Log.d(TAG, "back from tweets");
	            	//reschedule();
	            	break;
	            }
	      }
	      super.onActivityResult(requestCode, resultCode, intent);
	}
    
    private class HandlerLoad extends Handler 
	{
		public HandlerLoad()
		{
			super();
			
			Log.d(TAG, "new TwitterMainActivity HandlerLoad");
		}
		
		@Override
		public void handleMessage(Message msg) 
		{
			switch(msg.what)
			{			
			    case TREND_LOAD_TREND:
			    {			    	
			    	showDLG(DLG_TREND_LOADING);
			    	this.removeMessages(TREND_LOAD_TREND);
			    	Log.d(TAG, "before get tweets TREND_LOAD_TREND ");
			    	if(isInProcess())
			    	{
			    		Log.d(TAG, "I am still in loading");				    	
			    		return;
			    	}
			    	//get trend
			    	if(isBackgroud() == false)
			    	{
			    	    getLast10Trends();
			    	}
			    	else
			    	{
			    	    Log.d(TAG, "%%& i am in background, will not call the twitter network");
			    	}			    	
			    	break;
			    }
			    case TREND_CREATE_TREND_UI:
			    {
			    	using();
					
			    	Log.d(TAG, "After get trends status");
	            	TrendsAdapter ta = new TrendsAdapter(TwitterMainActivity.this, currentTweets);
	            	trendslist.setAdapter(ta);
			    			    	
			    	break;
			    }
			    case TREND_LOAD_TREND_end:
			    {
			    	end();
			    	
			    	dismissDLG(DLG_TREND_LOADING);	
			    	doNoContent();
			    	break;
			    }
			}
		}
	}
    
    private void notifyLoading() 
    {
    	notify.notifyOnce(R.string.twitter_trend_loading, R.drawable.twitter, 30*1000);		
	}
    
    public void dismissDLG(int dlgTrendLoading) 
    {
    	  //mContext.dismissDialog(DLG_TREND_LOADING);		
	}
    public void showDLG(int dlgTrendLoading)
    {
          //mContext.showDialog(dlgTrendLoading);
    }

	private void getLast10Trends()
    {	
		if(twitterA == null)
		{
			Log.d(TAG, "why I am null");
			Account account = orm.getTwitterAccount();
			twitterA = new AndroidAsyncTwitter(account.token, account.token_secret,true);			
		}
		
    	begin();
    	
    	Log.d(TAG, "before get tweets getLast10Trends");
    	notifyLoading();   	
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}
        twitterA.trendsAcync(new TwitterAdapter() 
        {
			@Override public void trends(Last10Trends tweets)
            {
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
            	Log.d(TAG, "After get trends");
            	//clear
            	if(currentTweets != null)
            	{
            	    currentTweets.last_10.clear();            	
            	    currentTweets = null;
            	}
            	
            	currentTweets = tweets;
                        	
            	handler.obtainMessage(TREND_CREATE_TREND_UI).sendToTarget();
            	cancelNotify();
            	handler.obtainMessage(TREND_LOAD_TREND_end).sendToTarget();
            	//update DB            	
            	ArrayList<Trend> trends = new ArrayList<Trend>();
            	for(int i=0;i<currentTweets.last_10.size();i++)
            	{
            		TrendsItem item = currentTweets.last_10.get(i);
            		Trend tr = orm.new Trend();
            		tr.ID = String.format("%1$s", i);
            		tr.Name = item.name;
            		tr.URL  = item.link;
            		tr.Date = currentTweets.as_of;
            		trends.add(tr);
            	}
				orm.UpdateTrend(trends);
            }

            @Override public void onException(TwitterException e, int method) 
            {
            	//if exception, the time will be not right, will take the request time           	
            	            	
            	synchronized(mLock)
            	{
            	    inprocess = false;
            	}            	
            	
            	cancelNotify();  
            	Log.d(TAG, "Fail to get ="+e.getMessage()+" get from DB");
            	
            	//get from DB
            	ArrayList<Trend> trends = orm.getLastTrend();
            	if(trends.size() > 0)
            	{
            		currentTweets = null;
            	    Last10Trends last10 = new Last10Trends();            	    
            	    last10.as_of = trends.get(0).Date;            	    
            	    
            	    for(int i=0;i<trends.size();i++)
            	    {
            	    	Trend item = trends.get(i);
            	    	TrendsItem term = last10.new TrendsItem(item.Name, item.URL);
            	    	last10.last_10.add(term);            	    	
            	    }
            	    currentTweets = last10;              	
                	handler.obtainMessage(TREND_CREATE_TREND_UI).sendToTarget();            	    
            	}
            	
            	if(isInAynscTaskAndStoped())
             	{
             		Log.d(TAG, "User stop passive");
             	}
             	else
             	{	
	            	handler.obtainMessage(TREND_LOAD_TREND_end).sendToTarget();
             	}            	
            }
        });       	     
    }
    
	
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) 
    {
	     super.onCreateOptionsMenu(menu);	     
	     //menu.setOptionalIconsVisible(false);	     
	     MenuInflater inflater = getMenuInflater();
	     inflater.inflate(R.menu.twitter_main, menu);
	     
	     return true;
	 }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
    {
        AdapterView.AdapterContextMenuInfo i = (AdapterView.AdapterContextMenuInfo) menuInfo;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.twitter_trend_context, menu);
        
        if(TrendItemView.class.isInstance(i.targetView))
        {
	        menu.findItem(R.id.context_open_in_browser).setVisible(true);
	        
	        TrendItemView tv = (TrendItemView)i.targetView;
	    	TrendsItem ti = tv.getTrendsItem();	    	
	    	menu.setHeaderTitle(ti.name);	    	
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) 
    {   
        AdapterView.AdapterContextMenuInfo i = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()) 
        {
            case R.id.context_open_in_browser: 
            {
            	
            	TrendItemView tv = (TrendItemView)i.targetView;
            	TrendsItem ti = tv.getTrendsItem();            	
            	
            	Intent intent = new Intent(Intent.ACTION_VIEW);
            	intent.setData(Uri.parse(ti.link));
            	startActivity(intent);            	
            	break;
            }
        }
        return true;
    }
    
    @Override public boolean onPrepareOptionsMenu(Menu menu)
    {    	
    	 super.onPrepareOptionsMenu(menu);
	     
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
    	 menu.findItem(R.id.menu_twitter_settings).setVisible(false);   
    	 menu.findItem(R.id.menu_following).setVisible(false);
    	 menu.findItem(R.id.menu_follower).setVisible(false);
    	 menu.findItem(R.id.menu_trends).setVisible(false);
    	 menu.findItem(R.id.menu_compose_message).setVisible(false);
    	 menu.findItem(R.id.menu_twitter_myaccount).setVisible(false);
    	 menu.findItem(R.id.menu_favorites).setVisible(false);
    	 menu.findItem(R.id.menu_following).setVisible(false);
    	 menu.findItem(R.id.menu_follower).setVisible(false);
    	 menu.findItem(R.id.menu_search).setVisible(false);
    	 menu.findItem(R.id.menu_twitter_upload_photo).setVisible(false);
    	 menu.findItem(R.id.menu_twitter_about).setVisible(false);
    	 menu.findItem(R.id.twitter_log_out).setVisible(false);
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
				 ((TwitterMainActivity)mContext).startActivityForResult(intent, TwitterMainActivity.TWITTER_TWEETS);
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
                        Intent intent = new Intent(TwitterMainActivity.this, TwitterLoginActivity.class);
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

    //process the stop menu, when loading
    @Override public void stopProcess() 
    {
    	if(twitterA != null)
    	{
    	    twitterA.stopCallNetWork();
    	}
    	
    	//just do it when in process
    	if(this.isInProcess())
    	{
            end();
    	}
    	stopLoading();
    	
    	AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
    	Intent i = new Intent();
        i.setClassName("com.tormas.litetwitter", "com.tormas.litetwitter.ui.TwitterMainActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        
 		alarmMgr.cancel(pi);
    }
    
    @Override
    public void createHandler() {
        handler = new HandlerLoad();        
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {            
            this.stopLoading();
           // System.gc();
           // moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onLogin() {
        super.onLogin();
        reschedule();
    }
    
    @Override protected void doAfterLoginNothing()
    {
        super.doAfterLoginNothing();
        TwitterMainActivity.this.setResult(RESULT_CANCELED);
        TwitterMainActivity.this.finish();    
    }
    
}
