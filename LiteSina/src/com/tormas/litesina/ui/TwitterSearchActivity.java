package com.tormas.litesina.ui;

import java.util.ArrayList;
import java.util.List;

import com.tormas.litesina.*;
import com.tormas.litesina.providers.SocialORM.Account;
import com.tormas.litesina.ui.adapter.SimplyStatusAdapter;
import twitter4j.AsyncTwitter;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TwitterSearchActivity extends StatusViewBaseActivity 
{	
	private final String TAG="TwitterSearchActivity";
	
	
    ListView tweets;
	private String trend_URL;
	private String trend_name;	
	
	private List<Tweet> currentStatus;
	EditText keyEdit;
	Button   searchDo;
	View     twitter_info_span;
	TextView twitter_info;
	
	private  long FriendCount = 20;
	private  boolean finishedCall   = false;
	private  boolean finishedCallUI = false;
	MyWatcher watcher;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.twitter_tweets);
        
        tweets = (ListView)this.findViewById(R.id.twitter_tweets_list);
        tweets.setFocusableInTouchMode(true);
        tweets.setFocusable(true);
        tweets.setOnCreateContextMenuListener(this);
        tweets.setOnItemClickListener(statusTweetsClickListener);
        
        View span = this.findViewById(R.id.twitter_trend_search_span);
        span.setVisibility(View.VISIBLE);
        
        keyEdit = (EditText)this.findViewById(R.id.embedded_text_editor);
        watcher = new MyWatcher(); 	    
        keyEdit.addTextChangedListener(watcher);
        
        twitter_info_span = this.findViewById(R.id.twitter_info_span);
        twitter_info      = (TextView)this.findViewById(R.id.twitter_info);
                
        searchDo = (Button)this.findViewById(R.id.search_do);
        searchDo.setOnClickListener(seachListener);
        //searchDo.setText("Search");
        
        trend_URL  = getIntent().getStringExtra("currenttrendurl");
        trend_name = getIntent().getStringExtra("currenttrendname");
        keyEdit.setText(trend_name);
        setTitle(R.string.twitter_search_title);
        //this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.search);
        
        if(this.checkTwitterAccount(this, orm.getTwitterAccount()))
        {
            launchSearch();
        }
        
        twitter_action.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void loadRefresh()
    {
        //Log.d(TAG, "return from twitter refresh to call the launch");
        String key = keyEdit.getText().toString().trim();
        if(key != null && key.length()>0 && isInProcess() == false)
        {
             Message msg = handler.obtainMessage(TWEET_SEARCH);              
             msg.getData().putString("keyword", key);
             trend_name = key;
             handler.sendMessage(msg);
        }
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) 
    {    
    
        // bind search key action - android sends 'ENTER' key since the search key is taking
        // the place of the ENTER Key on the keyboard for this edit text.
        // Make sure the search criteria is not "empty"
        String searchCriteria = keyEdit.getText().toString().trim();
        if(event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && searchCriteria.length() > 0)
        {
        	doSearch();
        }
        return super.dispatchKeyEvent(event);
    }
    
	@Override
	protected void onNewIntent(Intent intent) 
	{		
		super.onNewIntent(intent);
		
		setIntent(intent);
		trend_URL  = getIntent().getStringExtra("currenttrendurl");
	    trend_name = getIntent().getStringExtra("currenttrendname");
	    
	    
        if(this.checkTwitterAccount(this, orm.getTwitterAccount()))
        {
            launchSearch();
        }
	}
	
	public void setTitle() 
	{
		finalTitle = getString(R.string.twitter_search_title);	
		//finalTitle = "";
	}   
    
    public void titleSelected() {	
		super.titleSelected();
		
		doSearch();
	}

	private class MyWatcher implements TextWatcher 
	{   
       public void afterTextChanged(Editable s) 
       {   
    	   doSearch(s.toString());
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }
    
    List<Tweet>searchStatus = new ArrayList<Tweet>();
    private void doSearch(String key)
    {
		searchStatus.clear();        
        if(currentStatus != null && key != null && key.length()>0)
        {
            for(int i=0;i<currentStatus.size();i++)
            {
            	Tweet user = currentStatus.get(i);
                if(user.getText().toLowerCase().indexOf(key.toLowerCase())>=0 ||
                   user.getFromUser().toLowerCase().indexOf(key.toLowerCase())>=0)
                {
                	searchStatus.add(user);
                }
            }
            //show UI
            //refresh the UI
            SimplyStatusAdapter ta = new SimplyStatusAdapter(TwitterSearchActivity.this, (ArrayList<Tweet>)searchStatus, true);
        	tweets.setAdapter(ta);
        }   
        else
        {
        	if(currentStatus == null)
        	{
        		tweets.setAdapter(null);
        	}
        	else
        	{
	        	SimplyStatusAdapter ta = new SimplyStatusAdapter(TwitterSearchActivity.this, (ArrayList<Tweet>)currentStatus, true);
	        	tweets.setAdapter(ta);
        	}
        }
    }
    
    @Override public void loadAction()
    {
    	super.loadAction();    	
    	launchSearch();
    }
    
    @Override
    protected void loadAfterSetting()
    {
        super.loadAfterSetting();
        twitterA = null;
        launchSearch();
    }

    void launchSearch()
    {
    	if(twitterA == null)
    	{
    		Account account = orm.getTwitterAccount();
    		if(checkTwitterAccount(TwitterSearchActivity.this, account))
    		{
    	        twitterA = new AsyncTwitter(account.token, account.token_secret,true);
    		}
    		else
    		{
    			return;
    		}    		
    	}
        
        Message msg = handler.obtainMessage(TWEET_SEARCH);
        msg.getData().putString("keyword", trend_name);        
        handler.sendMessageDelayed(msg, 1000);
    }
    
   

    
    public static final int TWEET_DETAIL=0;
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
            /*case TWITTER_SETTING:
            {
            	//if not finished will launch again
            	if(finishedCall == false)
            	{
            		launchSearch();
            	}
            	else if(finishedCallUI == false && finishedCall==true)//construct UI;
            	{
            		handler.obtainMessage(TWEET_CONTRUCT_SEARCH_UI).sendToTarget();
            	    handler.obtainMessage(TWEET_SEARCH_END).sendToTarget();
            	}
            	break;
            }*/
            	
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
    
    
    View.OnClickListener seachListener = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			doSearch();
		}
	};
    
	private void doSearch()
	{
		String key = keyEdit.getText().toString().trim();
		if(key != null && key.length()>0 && isInProcess() == false)
		{
		     Message msg = handler.obtainMessage(TWEET_SEARCH);			     
	         msg.getData().putString("keyword", key);
	         trend_name = key;
	         handler.sendMessage(msg);
		}
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
			    case TWEET_SEARCH:
			    {
			    	//TwitterSearchActivity.this.showDialog(DLG_TWEET_SEARCH);
			    	String key = msg.getData().getString("keyword");
			    	searchTrends(key);			    	
			    	break;
			    }
			    case TWEET_CONTRUCT_SEARCH_UI:
			    {
			    	if(currentStatus != null && currentStatus.size() == 0)
			    	{
			    		tweets.setAdapter(null);
			    		
			    		String key = keyEdit.getText().toString().trim();
			    		String form = String.format(getString(R.string.no_result_search_hint), key);
			    		twitter_info_span.setVisibility(View.VISIBLE);
			    		twitter_info.setText(form);
			    	}
			    	else
			    	{
			    		twitter_info_span.setVisibility(View.GONE);
				    	SimplyStatusAdapter ta = new SimplyStatusAdapter(TwitterSearchActivity.this, (ArrayList<Tweet>)currentStatus, true);
		            	tweets.setAdapter(ta);
			    	}
	            	
	            	Message mds = handler.obtainMessage(TWEET_SEARCH_END);
	            	mds.getData().putBoolean(RESULT, true);
                    mds.sendToTarget();
			    	break;
			    }
			    case TWEET_SEARCH_END:
			    {
			    	end();
			    	dismissDlg(DLG_TWEET_SEARCH);
			    	
			    	setTitle(R.string.twitter_search_title);			    				    	
			    	Log.d(TAG, "TWEET_SEARCH_END");
			    	
			    	if(msg.getData().getBoolean(RESULT, false) == false)
			    	{
			    		Toast.makeText(TwitterSearchActivity.this, R.string.fail_search_hint, Toast.LENGTH_SHORT).show();
			    	}
			    	//TODO
			    	//twitterA.resumeCallNetWork();
			    	break;
			    }
			}
		}
	}
    
    void searchTrends(String keyword)
    {
    	if(this.isInProcess() || isEmpty(keyword))
    	{
    		return;
    	}
    	
    	synchronized(mLock)
    	{
    	    inprocess        = true;
    	    finishedCall     = false;
    	    finishedCallUI   = false;
    	}
    	
    	searchbegin();
    	
    	Log.d(TAG, "seach keyword="+keyword);    	
    	Query query = new Query();
        query.setQuery(keyword);
    	twitterA.searchAcync(query, new TwitterAdapter() 
        {
			@Override public void searched(QueryResult result)
            {				
			  
		    	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	    finishedCall = true;
		    	}	
		    	//append status into currentStatus, keep just 20 items
            	//construct the currentStatus            	            	
            	getFirstViewStatus(result.getTweets());
            	
            	//don't construct UI when go back, need do it when resume
                if(donotcallnetwork == false)//I am still alive
                {
                	Log.d(TAG, "search result="+result.getTweets().size());
                	finishedCallUI = true;
            	    handler.obtainMessage(TWEET_CONTRUCT_SEARCH_UI).sendToTarget();                	    
        	    }      
                else
                {
                    handler.obtainMessage(TWEET_SEARCH_END).sendToTarget();
                }
                                	
            }

            @Override public void onException(TwitterException e, int method) 
            {      
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
            	
            	Log.d(TAG, "Fail to search ="+e.getMessage());
            	if(isInAynscTaskAndStoped())
             	{
             		Log.d(TAG, "User stop passive");
             	}
             	else
             	{
	                if(donotcallnetwork == false)
	                {
	                    handler.obtainMessage(TWEET_SEARCH_END).sendToTarget();	                	
	                }
             	}               
            }			
        });       	     
    }    
    
    public void dismissDlg(int dlgTweetSearch) 
    {
    	//TwitterSearchActivity.this.dismissDialog(DLG_TWEET_SEARCH);		
	}

	private void getFirstViewStatus(List<Tweet> statuses)
    {
        //just need the count
        if(currentStatus != null)
        {
            currentStatus.clear();
            currentStatus = null;
        }
        currentStatus = new ArrayList<Tweet>();
        Log.d(TAG, "After get friends status");
        int pos=0;
        while(pos < FriendCount && pos <statuses.size())
        {           
            currentStatus.add(statuses.get(pos));
            pos++;
        }
        while(pos <statuses.size())
        {
            Tweet st = statuses.get(pos);
            st = null;
            statuses.remove(pos);
            pos ++;
        }
        statuses = null;        
    }

    @Override
    public void createHandler() 
    {
        handler = new HandlerLoad();        
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {           
            if(currentStatus != null)
            {
                currentStatus.clear();
                currentStatus = null;
            }
            tweets.removeAllViewsInLayout();
            tweets = null;
            
            this.stopLoading();
            System.gc();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

	@Override
	public void stopMyself() 
	{	    
		if(isInProcess() == true)
		{
		   	end();
		}
		//handler.obtainMessage(TWEET_SEARCH_END).sendToTarget();
	}
}
