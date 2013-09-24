package com.tormas.litesina.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tormas.litesina.*;
import com.tormas.litesina.providers.SocialORM.Account;
import com.tormas.litesina.providers.SocialORM.Follow;
import com.tormas.litesina.ui.adapter.FollowAdapter;
import com.tormas.litesina.ui.adapter.SimplyStatusAdapter;
import com.tormas.litesina.ui.view.ImageRun;
import twitter4j.AsyncTwitter;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.SimplyStatus;
import twitter4j.SimplyUser;
import twitter4j.Tweet;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.UserWithoutStatus;
import oms.sns.TwitterUser;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TwitterUserDetailsActivity extends StatusViewBaseActivity {
	private final String TAG="TwitterUserDetailsActivity";
	
	TwitterUser user;
	boolean isMyFriends = false;
	ImageView photo;
	Button    stopFollow;
	Button    seachDO;	
	TextView  userName;
	TextView  following_desc;
	TextView  recentlable;
	ListView  tweets;
	View      userline_search_span, profile_span;
	EditText  searchEdit;
	private int currentPage = 0;
	private int offset = 20;
	private int pageCount;	 
	
	private  long lastGetTime =-1;	
	private  List<SimplyStatus> currentStatus=new ArrayList<SimplyStatus>();
	private  List<Tweet> currentTweet=new ArrayList<Tweet>();
	
	private boolean iamStatusView=true;
	private boolean isForOutSeach=false;
	
	private String twitterid;
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_user_detail);
        
        //try to assign the count value
        recentlable = (TextView)this.findViewById(R.id.following_detail_recent_tweets);
        recentlable.setText(R.string.recent_tweets);        
        tweets = (ListView)this.findViewById(R.id.twitter_recent_tweets_list);
        tweets.setFocusableInTouchMode(true);
        tweets.setFocusable(true);
        tweets.setOnCreateContextMenuListener(this);
        
        userline_search_span = this.findViewById(R.id.userline_search_span);
        profile_span         = this.findViewById(R.id.profile_span);
                
        processIntent();        
	}
	
	private void processIntent()
	{
		user          = (TwitterUser)getIntent().getParcelableExtra("currentuser");
        isForOutSeach = getIntent().getBooleanExtra("search", false);       
        
        if(user != null)
        {
	        setTitle(user.screenName);
	        
	        getTwitterUserByScreenname(String.valueOf(user.id));	        
        }
        //set ui
        if(isForOutSeach==false)
        {	
			initUserUI(); 
	        stopFollow = (Button)this.findViewById(R.id.folowing_undo);
	        stopFollow.setOnClickListener(stopFollowingClick);
	        
	        //a bug from 
	        
	        //TODO need use interface friendship/exists.xml?user_a=me&user_b=user.screenName
	        
	        if(isMyFriends)
	            stopFollow.setText(R.string.following_detail_stop_button);
	        else
	        	stopFollow.setText(R.string.following_detail_stop_button_add);
        }
        else
        {
        	setTitle("");
        	twitterid = this.getIntent().getStringExtra(TWITTER_ID);
        	loadSearchForFollowing();
        }
        
        if(this.checkTwitterAccount(this, orm.getTwitterAccount()))
        {
        	launchFollow();
        }
	}
	
	private void initUserUI()
	{
		photo= (ImageView)this.findViewById(R.id.following_img_ui);
        ImageRun imagerun = new ImageRun(handler, user.profileImageUrl, 1);
        imagerun.use_avatar = true;
        imagerun.addHostAndPath = true;
	    imagerun.setImageView(photo);		
        imagerun.post(imagerun);
		
		userName = (TextView)this.findViewById(R.id.following_username);
		userName.setText(user.name);
		
		following_desc = (TextView)this.findViewById(R.id.following_desc);
		if(isEmpty(user.description) == false)
		{	
			following_desc.setText(user.description);
		}
		else
		{
		    following_desc.setVisibility(View.GONE);
		}	
		
	}
	
	private void getTwitterUserByScreenname(String screenname)
	{
		if(twitterA == null)
    	{
    		Account account = orm.getTwitterAccount();
    		if(checkTwitterAccount(TwitterUserDetailsActivity.this, account))
    		{
    	        twitterA = new AsyncTwitter(account.token, account.token_secret,true);
    		}
    		else
    		{
    			return;
    		}    		
    	}
		
		handler.obtainMessage(TWITTER_GET_USER_DETAIL).sendToTarget();
	}
	
	private void getUserDetailInfo(){
		twitterA.getUserDetailSimplyAsync(String.format("%1$s", user.id), new TwitterAdapter() 
	    {
			@Override public void gotUserDetailSimply(UserWithoutStatus extuser)
            {
				user.description = extuser.getDescription();
				user.profileImageUrl = extuser.getProfileImageURL()!=null?extuser.getProfileImageURL().toString():"";
				user.screenName = extuser.getScreenName();				
				user.name = extuser.getName();
				user.id = extuser.getId();
				Log.d(TAG, "After get user detail="+extuser);	
				
				handler.post(new Runnable(){
					public void run()
					{
						initUserUI();
					}
				});
				
            }

			@Override public void onException(TwitterException e, int method) 
            {	   
				
            	Log.d(TAG, "Fail to get ="+e.getMessage());			            	
				Log.d(TAG, "We are user detail");
				if(isInAynscTaskAndStoped())
             	{
             		Log.d(TAG, "User stop passive");
             	}
             	
            }			
	    });
	}
	
	
	
	@Override
	protected void onNewIntent(Intent intent) 
    {
    	Log.d(TAG, "new request for user view="+intent);
    	super.onNewIntent(intent);
		
		setIntent(intent);
		processIntent();
	}
	
	
	public void titleSelected()
	{
		//navigate to following activity
	    if(isForOutSeach == false)
	    {
	    	Intent intent = new Intent(this, TwitterFollowActivity.class);
        	intent.putExtra(FOLLOWING_VIEW, true);
        	intent.putExtra(TWITTER_ID, String.valueOf(user.id));        	
	        startActivityForResult(intent, TWITTER_FOLLOWING);	 
	    }		
	}
	
    public void setTitle() 
	{	
    	if(isForOutSeach)
	        finalTitle = "";
    	else
    	{
    		if(user != null)
    		    finalTitle = user.screenName;
    	}
    		
	}   
	
	private void launchFollow()
	{
		if(twitterA == null)
    	{
    		Account account = orm.getTwitterAccount();
    		if(checkTwitterAccount(TwitterUserDetailsActivity.this, account))
    		{
    	        twitterA = new AsyncTwitter(account.token, account.token_secret,true);
    		}
    		else
    		{
    			return;
    		}    		
    	}
		
		handler.obtainMessage(TWITTER_STATUS_USER_LINE).sendToTarget();
		if(isForOutSeach == false)
		{
		    handler.obtainMessage(TWITTER_IS_FRIENDS).sendToTarget();
		}
	}
	
    @Override
    public void createHandler() 
    {
        handler =  new UserDetailHandler();      
    }
    
    @Override
    protected void loadAction()
	{
		Log.d(TAG, "get user recent tweets");    
		//launchFollow();
	}
    
    @Override
    protected void loadAfterSetting()
    {
        super.loadAfterSetting();
        twitterA = null;
        launchFollow();
    }

    @Override
    protected void loadRefresh()
	{
		Log.d(TAG, "call refresh");    
		launchFollow();
	}
    
    @Override   
	protected void loadFollowersFromFollowing()
	{
    	super.loadFollowersFromFollowing();
    	
    	Intent intent = new Intent(this, TwitterFollowActivity.class);
    	intent.putExtra(FOLLOWER_VIEW, true);
    	intent.putExtra(TWITTER_ID,    String.valueOf(user.id));
    	intent.putExtra(TWITTER_UserName,    String.valueOf(user.screenName));
    	this.startActivityForResult(intent, TWITTER_FOLLOWER);
    	
    	System.gc();
    	finish();
	}
    
    @Override
    protected void  loadFollowingsFromFollower()
    {
    	super.loadFollowingsFromFollower();
    	
    	Intent intent = new Intent(this, TwitterFollowActivity.class);
    	intent.putExtra(FOLLOWING_VIEW, true);
    	intent.putExtra(TWITTER_ID,    String.valueOf(user.id));
    	intent.putExtra(TWITTER_UserName,    String.valueOf(user.screenName));
    	this.startActivityForResult(intent, TWITTER_FOLLOWING);    	
    	
    	System.gc();
    	finish();
    }
    
    MyWatcher watcher;
    @Override
    protected void loadSearchForFollowing()
    {
    	super.loadSearchForFollowing();
    	
    	//TODO
    	profile_span.setVisibility(View.GONE);
    	userline_search_span.setVisibility(View.VISIBLE);
    	seachDO = (Button)this.findViewById(R.id.userline_search_do);
    	//seachDO.setBackgroundResource(R.drawable.search);
    	searchEdit = (EditText)this.findViewById(R.id.userline_embedded_text_editor);
    	searchEdit.requestFocus();
    	
    	watcher = new MyWatcher();         
    	searchEdit.addTextChangedListener(watcher);
    	seachDO.setOnClickListener(seachListener);
    }
    
    private class MyWatcher implements TextWatcher 
    {   
       public void afterTextChanged(Editable s) 
       {
           //do search
           doSearch(s.toString());
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }
    
   private void doSearch(String str)
   {
	   if(isEmpty(str))
	   {
		   this.goFistPage();
		   return;
	   }
		if(iamStatusView == false)
		{
			if(currentTweet != null && currentTweet.size() > 0)
			{
				List<Tweet> tempdata = new ArrayList<Tweet>();
				for(int i=0;i<currentTweet.size();i++)
				{
					Tweet item = currentTweet.get(i);
					if(item.text.contains(str) || (item.fromUser!= null && item.fromUser.contains(str)) || (item.toUser != null && item.toUser.contains(str)))
					{
				        tempdata.add(item);
					}
				}
				//for search
				SimplyStatusAdapter ta = new SimplyStatusAdapter(TwitterUserDetailsActivity.this, (ArrayList<Tweet>)tempdata, true);
	        	tweets.setAdapter(ta);	        	
		    }
		}
		else
		{
			if(currentStatus != null && currentStatus.size() > 0)
			{
				List<SimplyStatus> tempdata = new ArrayList<SimplyStatus>();
				for(int i=0;i<currentStatus.size();i++)
				{
					SimplyStatus st = currentStatus.get(i);
					if(st.text.contains(str) || (st.getUser() != null && st.getUser().screenName.contains(str)))
					{
				        tempdata.add(st);
					}
				}
	        	//for status
	        	SimplyStatusAdapter sta = new SimplyStatusAdapter(TwitterUserDetailsActivity.this, (ArrayList<SimplyStatus>)tempdata);
	        	tweets.setAdapter(sta);	        	
			}
		}
   }
    
    View.OnClickListener seachListener = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			String key = searchEdit.getText().toString().trim();
			if(key != null && key.length()>0)
			{
			     Message msg = handler.obtainMessage(TWEET_SEARCH);			     
		         msg.getData().putString("keyword", key);
		         if(isForOutSeach)
		             msg.getData().putString("from", twitterid);
		         else
		        	 msg.getData().putString("from", user.screenName);
		         handler.sendMessage(msg);
			}
			//searchEdit.setText("");
		}
	};
    
	View.OnClickListener stopFollowingClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			if(isMyFriends)
			{
				Message msg = basichandler.obtainMessage(TWEET_UNFOLLOW);			     
		        msg.getData().putString(TWITTER_ID, String.valueOf(user.id));		         
		        basichandler.sendMessage(msg);
			}
			else
			{
				Message msg = basichandler.obtainMessage(TWEET_FOLLOW);			     
		        msg.getData().putString(TWITTER_ID, String.valueOf(user.id));		         
		        basichandler.sendMessage(msg);
			}
		}
	};
	   
    private void processTwitterException(TwitterException e, int method)
    {
    	//if exception, the time will be not right, will take the request time            	
     	lastGetTime = System.currentTimeMillis()+1000;            	
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
            if(donotcallnetwork == false)//I am still alive
            {            	
                cancelNotify();
                Log.d(TAG, "Fail to get ="+e.getMessage());
            }	     
     	}
    }
    
    
    @Override
	protected boolean goPrePage()
	{
		if(currentPage  == 0)
			return false;
		
		currentPage = currentPage-1;
		setTitle(String.format("%1$s/%2$s", currentPage+1, pageCount));
		boolean ret = goPage(currentPage);
		
		System.gc();
		return ret;
	}
	
	@Override
	protected boolean goNextPage()
	{
		if(currentPage==pageCount-1)
			return false;
		
		currentPage = currentPage+1;
		setTitle(String.format("%1$s/%2$s", currentPage+1, pageCount));
		boolean ret = goPage(currentPage);
		
		System.gc();
		return ret;
	}
	
	private void goFistPage()
	{
		goPage(0);
	}
	
	//changed the content
	private void updateCurrentPageUI()
	{
		goPage(currentPage);
	}

	List<SimplyStatus> sdata ; 
	List<Tweet>        tdata ;
	private boolean goPage(int page)
	{
		boolean ret = false;
		if(iamStatusView == false)
		{
			if(tdata != null && tdata.size() > 0)
			{
				List<Tweet> tempdata = new ArrayList<Tweet>();
				for(int i=0;i<tdata.size();i++)
				{
					if(i>=page * offset && i<(page+1)*offset)
					{
				        tempdata.add(tdata.get(i));
					}
				}
				//for search
				SimplyStatusAdapter ta = new SimplyStatusAdapter(TwitterUserDetailsActivity.this, (ArrayList<Tweet>)tdata, true);
	        	tweets.setAdapter(ta);
	        	ret = true;
		    }
		}
		else
		{
			if(sdata != null && sdata.size() > 0)
			{
				List<SimplyStatus> tempdata = new ArrayList<SimplyStatus>();
				for(int i=0;i<sdata.size();i++)
				{
					if(i>=page * offset && i<(page+1)*offset)
					{
				        tempdata.add(sdata.get(i));
					}
				}
	        	//for status
	        	SimplyStatusAdapter sta = new SimplyStatusAdapter(TwitterUserDetailsActivity.this, (ArrayList<SimplyStatus>)sdata);
	        	tweets.setAdapter(sta);
	        	ret = true;
			}
		}
		return ret;
	}

     private final int TWITTER_GET_USER_DETAIL = 9000;
    
  //handler follow message
	public class UserDetailHandler extends Handler
	{
		public UserDetailHandler()
        {
            super();
            Log.d(TAG, "new FollowHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case TWITTER_STATUS_USER_LINE:
				{	
					getUserLine();
					break;
				}
	            case TWITTER_GET_USER_DETAIL:
	            {
	            	getUserDetailInfo();
	            	break;
	            }
	            case TWITTER_IS_FRIENDS:
	            {
	                checkIsFriends();
	                break;
	            }
	            case TWEET_CREATE_UI:
	            {
                    using();
	            	iamStatusView = true;
	            	TwitterUserDetailsActivity.this.setTitle(TITLE_CONSTRUCT_UI);
					
	            	if(currentStatus != null && currentStatus.size()>0)
	            	{
	            	    pageCount=currentStatus.size()/offset + 1;
	            	    if((pageCount-1)*offset == currentStatus.size())
	            	    {
	            	    	pageCount = pageCount -1;
	            	    }
	            	    currentPage=0;	                
		                goFistPage();	       
	            	}
	            	
			    	Log.d(TAG, "After get user line status");			    	
	            	handler.obtainMessage(TWEET_LOAD_end).sendToTarget();
	            	break;
	            }
	            case TWEET_CONTRUCT_SEARCH_UI:
	            {
                    using();
	            	iamStatusView = false;
	            	TwitterUserDetailsActivity.this.setTitle(TITLE_CONSTRUCT_UI);
					
	            	if(currentTweet != null && currentTweet.size()>0)
	            	{
	            	    pageCount=currentTweet.size()/offset + 1;
	            	    if((pageCount-1)*offset == currentTweet.size())
	            	    {
	            	    	pageCount = pageCount -1;
	            	    }
	            	    currentPage=0;
		                goFistPage();	       
	            	}
	            	break;
	            }
	            case TWEET_SEARCH:
	            {
	            	TwitterUserDetailsActivity.this.seachDO.setEnabled(false);
			    	//TwitterSearchActivity.this.showDialog(DLG_TWEET_SEARCH);
			    	String key = msg.getData().getString("keyword");
			    	String from = msg.getData().getString("from");
			    	searchTrends(key, from);
			    	Log.d(TAG, "before get twee");
	            	break;
	            }
	            case TWEET_LOAD_end:
		        {
				    end();	
					//restore the network
				    twitterA.resumeCallNetWork();
					break;
		        }
	            case TWEET_SEARCH_END:
		        {			    	
		    	    seachDO.setEnabled(true);
                    end();

                    boolean res = msg.getData().getBoolean(RESULT, false);
                    if(res == true)
                    {
                    	
                    }
                    else
                    {
                    	Toast.makeText(TwitterUserDetailsActivity.this, R.string.fail_search_hint, Toast.LENGTH_SHORT).show();
                    }
		    	    Log.d(TAG, "TWEET_SEARCH_END");			    	
		    	 
		    	    twitterA.resumeCallNetWork();
		    	    break;
		        }
	            case TWEET_UNFOLLOW_END:
	            {
	            	boolean suc = msg.getData().getBoolean(RESULT);
	            	if(suc == true)
	            	{
	            		stopFollow.setText(R.string.following_detail_stop_button_add);
	            		isMyFriends = false;
	            	}
	            	break;
	            }
	            case TWEET_FOLLOW_END:
	            {
	            	boolean suc = msg.getData().getBoolean(RESULT);
	            	if(suc == true)
	            	{
	            		stopFollow.setText(R.string.following_detail_stop_button);
	            		isMyFriends = true;
	            	}
	            	break;
	            }
            }
        }
	}

	@Override
	public void stopMyself() 
	{
		if(inprocess == true)
		{
                    stoping();
		}
		else
		{
			handler.obtainMessage(TWEET_LOAD_end).sendToTarget();
		}
	}
	
	private void checkIsFriends()
	{
	    String tA = twitterid_db;
	    String tB = String.valueOf(user.id);
	    twitterA.existsFriendshipAsync(tA,tB,new TwitterAdapter()
	    {
	       @Override public void  gotExistsFriendship(boolean retvalue)
	       {
	           if(retvalue)
	           {
	               handler.post(new Runnable(){
	                   public void run()
	                   {
	                       stopFollow.setText(R.string.following_detail_stop_button);
	                   }
	               });    
	           }
	           else
	           {
	               handler.post(new Runnable(){
                       public void run()
                       {
                           stopFollow.setText(R.string.following_detail_stop_button_add);
                       }
                   });   
	           }
	           
	           isMyFriends = retvalue;
	           
	       }
	       
	       @Override public void onException(TwitterException e, int method) 
           {
              Log.d(TAG, "checkIsFriend exception "+e.getMessage()+"method =="+method);
           }       

	    });
	}
	
	public void getUserLine() 
	{
		synchronized(mLock)
	    {
	        inprocess        = true;
	    }
	
        begin();
	    if(lastGetTime != -1)
	    {
		    final Date from = new Date(lastGetTime);
		    twitterA.getSimplyUserTimelineAsync(String.valueOf(user.id),from , new TwitterAdapter() 
 	        {
 		        @Override public void gotSimplyUserTimeline(List<SimplyStatus> statuses)
 	            {
 		        	
		 			lastGetTime = System.currentTimeMillis()+1000; 
		 			synchronized(mLock)
		 		   	{
		 		        inprocess = false;
		 		    }
		 				
		 			currentStatus = statuses;
		 			sdata = currentStatus;
		 			if(statuses.size() > 0)
		 			{
		 		        lastGetTime = currentStatus.get(0).getCreatedAt().getTime()+1000;
		 			}
 			        
                    if(donotcallnetwork == false)//I am still alive
                    {
                      Log.d(TAG, "After get friends update status from="+from.toLocaleString() + " count="+statuses.size());		         	            	
 	            	  cancelNotify(); 	            	
 	            	  handler.obtainMessage(TWEET_CREATE_UI).sendToTarget();
                    }
 	            }
 	
 	            @Override public void onException(TwitterException e, int method) 
 	            {
 	            	processTwitterException(e, method);
 	            }			
 	        });    
		}
		else
		{
		    twitterA.getSimplyUserTimelineAsync(String.valueOf(user.id), new TwitterAdapter() 
 	        {
 		 	    @Override public void gotSimplyUserTimeline(List<SimplyStatus> statuses)
 	            {
	 			    lastGetTime = System.currentTimeMillis()+1000; 
	 			    synchronized(mLock)
	 			    {
	 			        inprocess = false;
	 			    }
 					
 			        currentStatus = statuses;
 			        sdata = currentStatus;
 					
                    if(donotcallnetwork == false)//I am still alive
                    {
                        Log.d(TAG, "After get user line status");
                        handler.obtainMessage(TWEET_CREATE_UI).sendToTarget();
            	        cancelNotify();
                    }			                    
 	            }
 	
 	            @Override public void onException(TwitterException e, int method) 
 	            {
 	            	processTwitterException(e, method);
	 	        }
 	        });       	 
	    }
	}
	public void searchTrends(String key, String from) 
	{
	    synchronized(mLock)
    	    {
    	        inprocess        = true;
    	    } 
            searchbegin();	
            Query query = new Query();
            query.setQuery(key);
            query.setFrom(from);
            twitterA.searchAcync(query, new TwitterAdapter() 
            {  
	 	        @Override public void searched(QueryResult result)
                {	
			    	synchronized(mLock)
			    	{
			    	    inprocess = false;			    	    
			    	}	
			    	//append status into currentStatus, keep just 20 items
	            	//construct the currentStatus            	            	
	            	//getFirstViewStatus(result.getTweets());
	            	tdata = currentTweet= result.getTweets();
	            	//don't construct UI when go back, need do it when resume
	                if(donotcallnetwork == false)//I am still alive
	                {
	                	Log.d(TAG, "search result="+result.getTweets().size());                    	
	            	    handler.obtainMessage(TWEET_CONTRUCT_SEARCH_UI).sendToTarget();            	    
	        	    }
	                
	                Message nsg = handler.obtainMessage(TWEET_SEARCH_END);
	                nsg.getData().putBoolean(RESULT, true);
	                nsg.sendToTarget();
                }

	            @Override public void onException(TwitterException e, int method) 
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
	             		Message nsg = handler.obtainMessage(TWEET_SEARCH_END);
		                nsg.getData().putBoolean(RESULT, false);
		                nsg.sendToTarget();
	             	}
	            	Log.d(TAG, "Fail to search ="+e.getMessage());    
	            }			
        });  
	}
	
	private void getFirstViewStatus(List<Tweet> tweets)
    {
        //just need the count
        if(currentTweet != null)
        {
        	currentTweet.clear();
        	currentTweet = null;
        }
        currentTweet = new ArrayList<Tweet>();
        Log.d(TAG, "After search");
        int pos=0;
        while(pos < offset && pos <tweets.size())
        {           
        	currentTweet.add(tweets.get(pos));
            pos++;
        }
        while(pos <tweets.size())
        {
            Tweet st = tweets.get(pos);
            st = null;
            tweets.remove(pos);
            pos ++;
        }
        tweets = null;        
    }
}
