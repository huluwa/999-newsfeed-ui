package com.tormas.litetwitter.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tormas.litetwitter.*;
import com.tormas.litetwitter.providers.SocialORM;
import com.tormas.litetwitter.providers.SocialORM.Account;
import com.tormas.litetwitter.providers.SocialORM.Follow;
import com.tormas.litetwitter.ui.TwitterMyAccountActivity.TwitterInfo;
import com.tormas.litetwitter.ui.TwitterMyAccountActivity.TwitterInfoAdapter;
import com.tormas.litetwitter.ui.TwitterMyAccountActivity.TwitterInfoItemView;
import com.tormas.litetwitter.ui.view.ImageRun;
import com.tormas.litetwitter.util.DateUtil;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserWithoutStatus;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import oms.sns.TwitterStatus;
import oms.sns.TwitterUser;

public class TwitterTweetsDetailActivity extends TwitterBaseActivity {
	private final String TAG="TwitterTweetsDetailActivity";
	
	TwitterStatus status;
	TwitterStatus tweets;

	private ImageView imageView;
	private TextView  publishDate;
	private TextView  publishTxt;
	private TextView  username;
	private Button    follow_do;
	
	private boolean isInFavor     =false;
	private boolean isFollowing   =false;
	private long    statusID    =-1;	
	private String  twitterID;
	private String  twitterName;
	private String  myUserID ;
	//private Status  detailStatus;
	
	private ListView myInfo;	
	private UserWithoutStatus user;
			
	Menu status_menu;
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_tweets_detail);
        
		imageView  = (ImageView)findViewById(R.id.tweet_img_ui);
		publishDate  = (TextView)findViewById(R.id.tweet_publish_time);
		publishTxt   = (TextView)findViewById(R.id.tweet_publish_text);
		username     = (TextView)findViewById(R.id.tweet_user_name);
		follow_do    = (Button)findViewById(R.id.follow_do);
		
		follow_do.setEnabled(false);
		follow_do.setVisibility(View.GONE);
		follow_do.setOnClickListener(doFollowOrNot);
		
		myInfo = (ListView)this.findViewById(R.id.twitter_info_list);
        myInfo.setFocusableInTouchMode(true);
        myInfo.setFocusable(true); 
        myInfo.setOnItemClickListener(itemClick);
		        
		processIntent();
		
		twitter_action.setVisibility(View.VISIBLE);
    }
	
	AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener()
    {
		public void onItemClick(AdapterView<?> arg0, View v, int arg2,long arg3)
		{	
			if(TwitterInfoItemView.class.isInstance(v))
	        {
	            TwitterInfo info = ((TwitterInfoItemView)v).getItem();
	            
	            if(info!=null)
	            {
	            	//0 following, 1 follower, 2 updates, 4 favorites
	            	switch(info.type)
	            	{
	                	case TwitterInfo.following:
	                	{  
	                		if(user.getFriendsCount() > 0)
	                		{
		                		Intent intent = new Intent(TwitterTweetsDetailActivity.this, TwitterFollowActivity.class);
		                    	intent.putExtra(FOLLOWING_VIEW, true);
		                    	intent.putExtra(TWITTER_ID,    twitterID);
		                    	startActivity(intent);
	                		}
	                		break;
	                	}
	                	case TwitterInfo.follower:
	                	{   
	                		if(user.getFollowersCount() > 0)
	                		{
		                		Intent intent = new Intent(TwitterTweetsDetailActivity.this, TwitterFollowActivity.class);
		                    	intent.putExtra(FOLLOWER_VIEW, true);
		                    	intent.putExtra(TWITTER_ID,    twitterID);
		                    	startActivity(intent);
	                		}
	                		break;
	                	}
	                	case TwitterInfo.updates:
	                	{
	                		if(user.getStatusesCount() > 0)
	                		{
		                		Intent intent = new Intent(TwitterTweetsDetailActivity.this, TwitterTweetsActivity.class);
		        	        	intent.putExtra("userline_uid", twitterID);
		        	        	//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        	        	startActivity(intent);
	                		}
	                		break;
	                	}
	                	case TwitterInfo.favorites:
	                	{
	                		if(user.getFavouritesCount() > 0)
	                		{
		                		Intent intent = new Intent(TwitterTweetsDetailActivity.this, TwitterFavoritesActivity.class);
		        	        	intent.putExtra(FAVORITEVIEW, true);
		        	        	intent.putExtra(TWITTER_ID, twitterID);
		        		        startActivity(intent);
	                		}
	                		break;
	                	}
	            	}
	            }
	        }
        }		
    };

    
    @Override
    public void setContentView(int resId) {
        super.setContentView(resId);

        Log.d(TAG, "setContentView=" + this);
        View top = this.findViewById(resId);
        if (top != null) {
            Log.d(TAG, "set background=" + this);
            Resources res = getResources();
            Drawable mCacheSym = res.getDrawable(R.color.facebook_backgroud);
            top.setBackgroundDrawable(mCacheSym);
        }
        
        if(isShowTitleBar == false)
        {   
            titleLayout = findViewById(R.id.twitter_action_layout);
            if(titleLayout != null)
            {
	            titleLayout.setVisibility(View.VISIBLE);
	            headerTitle = (TextView)findViewById(R.id.twitter_title);
	            headerProgressBar = (ProgressBar)findViewById(R.id.header_progressbar);
	            twitter_refresh = (ImageView)findViewById(R.id.twitter_refresh);
	            twitter_refresh.setOnClickListener(new View.OnClickListener() {					
					@Override
					public void onClick(View arg0) {
						launchContent();						
					}
				});
	            
	            twitter_action = (Button)findViewById(R.id.twitter_action);
	            twitter_action.setOnClickListener(new View.OnClickListener() {					
					@Override
					public void onClick(View arg0) {
						titleSelected();						
					}
				});
            }
        }       
    }	
	 
    @Override
    public void setContentView(View view) {
        super.setContentView(view);

        Log.d(TAG, "setContentView=" + this);
        if (view != null) {
            Log.d(TAG, "set background=" + this);
            Resources res = getResources();
            Drawable mCacheSym = res.getDrawable(R.color.facebook_backgroud);
            view.setBackgroundDrawable(mCacheSym);
        }
        
        if(isShowTitleBar == false)
        {
            titleLayout = findViewById(R.id.twitter_action_layout);
            if(titleLayout != null)
            {
	            titleLayout.setVisibility(View.VISIBLE);
	            headerTitle = (TextView)findViewById(R.id.twitter_title);
	            headerProgressBar = (ProgressBar)findViewById(R.id.header_progressbar);
	            twitter_refresh = (ImageView)findViewById(R.id.twitter_refresh);
	            twitter_refresh.setOnClickListener(new View.OnClickListener() {					
					@Override
					public void onClick(View arg0) {
						launchContent();						
					}
				});
	            
	            twitter_action = (Button)findViewById(R.id.twitter_action);
	            twitter_action.setOnClickListener(new View.OnClickListener() {					
					@Override
					public void onClick(View arg0) {
						titleSelected();						
					}
				});
            }
        }
    }
    
	private void processIntent()
	{
		Account account = orm.getTwitterAccount();		
		myUserID =account.screenname;		
		//why default is false, because, we don't know the value
		
		//come from two places
        //one is tweets view, will pass TwitterStatus data
        //another is trends view
        status = (TwitterStatus)getIntent().getParcelableExtra("currentstatus");
        if(status != null)
        {        	
        	isInFavor    = status.isFavorited;            
            twitterName  = status.user.name;
            twitterID    = status.user.screenName;
            statusID     = status.id;    

            isFollowing  = status.user.following;
        	this.setTitle(twitterName);
        	
        	Log.d(TAG, "come from status="+twitterName);       	
        }
        else
        {
        	//
        	tweets = (TwitterStatus)getIntent().getParcelableExtra("currenttweet");
        	if(tweets != null)
        	{
        	    twitterName   = tweets.user.name;
                twitterID     = tweets.user.screenName;
                statusID      = tweets.id;
                isFollowing   = tweets.user.following;
                this.setTitle(twitterName); 
        	}
        	Log.d(TAG, "come from tweet="+twitterName);        	
        }        
        
        setUI();
        if(checkTwitterAccount(this, account) == true)
        {
            launchContent();
        }
	}
	
	@Override
	protected void onNewIntent(Intent intent) 
    {
    	Log.d(TAG, "new request for tweet detail view="+intent);
    	super.onNewIntent(intent);
		
    	String tid = intent.getStringExtra(TWITTER_ID);
    	if(tid != null && tid.equalsIgnoreCase(twitterID))
    	{
    		Log.d(TAG, "I am the same user ignore="+tid);
    		return ;
    	}
		setIntent(intent);
		processIntent();
	}
    
    public void titleSelected() 
    {        
        Intent intent = new Intent(mContext, TwitterComposeActivity.class);             
        intent.putExtra(STATUS_ID,    statusID);    
        intent.putExtra(TWITTER_ID,    twitterID); 
        intent.putExtra(REPLY, true);
        startActivityForResult(intent, TWITTER_DONOTHING);    
    }

    public void setTitle() 
	{	
	    finalTitle = "Reply";
	}   
	  
	View.OnClickListener doFollowOrNot = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			Log.d(TAG, "call onclick event");
			
			if(isFollowing)
			{
				 Message message = basichandler.obtainMessage(TWEET_UNFOLLOW);
		         message.getData().putString(TWITTER_ID, twitterID);
		         message.sendToTarget();
			}
			else
            {
				Message message = basichandler.obtainMessage(TWEET_FOLLOW);			
				message.getData().putString(TWITTER_ID, twitterID);
				message.sendToTarget();
		    }
		}
	};
	
    private void launchContent()
    {
         //try to get whether it is my follow, whether it is in my favor\
       
        if(inprocess == true)
        {
            Log.d(TAG,"is loading twittertweetsdetail "+this);
            return ;
        }
         Message msg = handler.obtainMessage(TWITTER_IS_FRIENDS);
    	 if(isFollowing == false)
    	 {
             handler.sendMessageDelayed(msg, 2*1000);
    	 }
         
         if(status != null)
         {
        	 msg = handler.obtainMessage(TWEET_GET_STATUS);         
             handler.sendMessageDelayed(msg, 8*1000);
         }
         
        //get user detail informations
        msg =handler.obtainMessage(FOLLOW_USER_DETAILS);		
 	    msg.getData().putString(TWITTER_ID, twitterID);
 		msg.sendToTarget();		
        
    }
    @Override public void loadAction()
    {
        super.loadAction();
        launchContent();
    }

    @Override
    protected void loadAfterSetting()
    {
        super.loadAfterSetting();
        //reset twitterA
        twitterA = null;
        launchContent();
    }

    void setFollowingText()
    {
    	follow_do.setText(isFollowing==true?getString(R.string.following_detail_stop_button):getString(R.string.following_detail_stop_button_add));
    }
	private void setUI()
	{
		TwitterStatus tmp = ((status == null)?tweets:status);
		
		//status contain the following infomation
		//if(status != null)
		{
			follow_do.setVisibility(View.VISIBLE);
			follow_do.setEnabled(true);
			setFollowingText();
		}
		
		if(tmp != null)
		{
			username.setText(tmp.user.name);
			
			ImageRun imagerun = new ImageRun(handler, tmp.user.profileImageUrl, 1);
			imagerun.setImageView(imageView);		
			imagerun.use_avatar = true;
			imagerun.addHostAndPath = true;
			imagerun.post(imagerun);
			
			publishDate.setText(DateUtil.converToRelativeTime(mContext, tmp.createdAt));			
			publishTxt.setText(tmp.text);		
		}	
	}
	
    protected void loadFollowersFromFollowing()
    {
        
        Intent intent = new Intent(this, TwitterFollowActivity.class);
        intent.putExtra(FOLLOWER_VIEW, true);
        intent.putExtra(TWITTER_ID,    twitterID);
        this.startActivityForResult(intent, TWITTER_FOLLOWER);
        
        System.gc();
        finish();
    }
    
    protected void  loadFollowingsFromFollower()
    {
        
        Intent intent = new Intent(this, TwitterFollowActivity.class);
        intent.putExtra(FOLLOWING_VIEW, true);
        intent.putExtra(TWITTER_ID,    twitterID);
        this.startActivityForResult(intent, TWITTER_FOLLOWING);
        
        System.gc();
        finish();
    }
	
	
	 @Override
	public boolean onCreateOptionsMenu(Menu menu) 
    {
	     super.onCreateOptionsMenu(menu);	     
	     //menu.setOptionalIconsVisible(false);	     
	     MenuInflater inflater = getMenuInflater();
	     inflater.inflate(R.menu.twitter_status_item, menu);
	     
	     status_menu = menu;	
	     return true;
	 }
	 
	@Override public boolean onPrepareOptionsMenu(Menu menu)
    {    	
    	 super.onPrepareOptionsMenu(menu);
    	 if(isInProcess() == true)
    	 {
    	     menu.findItem(R.id.status_stop).setVisible(true);
    	     menu.findItem(R.id.status_refresh).setVisible(false);
    	 }
    	 else
    	 {
    	     menu.findItem(R.id.status_stop).setVisible(false);
             menu.findItem(R.id.status_refresh).setVisible(true);
    	 }
    	 menu.findItem(R.id.status_reply).setVisible(false);
    	 menu.findItem(R.id.status_favor_remove).setVisible(isInFavor==false?false:true);
    	 menu.findItem(R.id.status_favor_add).setVisible(isInFavor==true?false:true);
 	     
    	 menu.findItem(R.id.status_follow).setVisible(isFollowing==true?false:true);
    	 menu.findItem(R.id.status_unfollow).setVisible(isFollowing==false?false:true);
    	 menu.findItem(R.id.menu_following).setVisible(true);
         menu.findItem(R.id.menu_follower).setVisible(true);
    	 
    	 return true;
    }
	 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
            case R.id.status_refresh:
            {
                launchContent();
                break;
            }
            case R.id.status_stop:
            {
                if(twitterA != null)
                {
                    twitterA.stopCallNetWork();
                }
                end();
                stopLoading();
                break;
            }
	        case R.id.status_reply:
	        {
	        	Intent intent = new Intent(TwitterTweetsDetailActivity.this, TwitterComposeActivity.class);	        	
	        	intent.putExtra(STATUS_ID,  statusID);
	        	intent.putExtra(TWITTER_ID, twitterID); 
	        	intent.putExtra(REPLY, true);
	    		startActivityForResult(intent, TWITTER_DONOTHING);       	
	        	break;
	        }
	        case R.id.status_direct:
            {
                Intent intent = new Intent(TwitterTweetsDetailActivity.this, TwitterComposeActivity.class);
                intent.putExtra(STATUS_ID,  statusID);
                intent.putExtra(TWITTER_ID, twitterID);        
                intent.putExtra(DIRECT, true);
                startActivityForResult(intent, TWITTER_DONOTHING);          
                break;
            }
	        case R.id.status_retweet:
            {
                doRetweet();      
                break;
            }
	        case R.id.status_forward:
	        {
	            Intent intent = new Intent(TwitterTweetsDetailActivity.this, TwitterComposeActivity.class);             
                intent.putExtra(STATUS_ID,  statusID);
                intent.putExtra(TWITTER_ID, twitterID);                  
                intent.putExtra(RETWEET, true);  
                TwitterStatus tmp = status == null?tweets:status;
                intent.putExtra(CONTENT, tmp.text);
                
                startActivityForResult(intent, TWITTER_DONOTHING); 
	            break;
	        }
	        case R.id.status_favor_add:
	        {
	            Message message = basichandler.obtainMessage(TWEET_FAVOR);
	            message.getData().putLong(STATUS_ID, statusID);
	            
	            message.sendToTarget();	  	
	        	break;	        	
	        }
	        case R.id.status_favor_remove:
	        {
	            Message message = basichandler.obtainMessage(TWEET_FAVOR);
                message.getData().putLong(STATUS_ID, statusID);
                message.sendToTarget();
	        	break;	        	
	        }
	        case R.id.status_follow:
	        {		        	
	            Message message = basichandler.obtainMessage(TWEET_FOLLOW);
                message.getData().putString(TWITTER_ID, twitterID);
                message.sendToTarget();
	        	break;
	        }
	        case R.id.status_unfollow:
	        {
	            Message message = basichandler.obtainMessage(TWEET_UNFOLLOW);
                message.getData().putString(TWITTER_ID, twitterID);
                message.sendToTarget();	        	
	        	break;
	        }
	        case R.id.status_goto_hisline:
	        {
	        	Intent intent = new Intent(TwitterTweetsDetailActivity.this, TwitterTweetsActivity.class);
	        	intent.putExtra("friendline_uid", twitterID);
	        	//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	startActivityForResult(intent, TWITTER_TWEETS);
	        	break;
	        }
	        case R.id.status_goto_hisownerline:
	        {
	        	Intent intent = new Intent(TwitterTweetsDetailActivity.this, TwitterTweetsActivity.class);
	        	intent.putExtra("userline_uid", twitterID);
	        	//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	startActivityForResult(intent, TWITTER_TWEETS);
	    		break;
	        }
	        case R.id.menu_follower:
            {
                loadFollowersFromFollowing();
                break;
            }
            case R.id.menu_following:
            {               
                loadFollowingsFromFollower();
                break;
            }
        }
        return true;
    }
    
    protected void doRetweet()
    {
        Message msg = basichandler.obtainMessage(TWITTER_RETWEET);
        msg.getData().putLong(STATUS_ID,  statusID);
        msg.sendToTarget();
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
			    case TWITTER_IS_FRIENDS:
			    {
			        
			    	twitterA.existsFriendshipAsync(myUserID, twitterID, new TwitterAdapter() 
			        {
			    	    @Override public void  gotExistsFriendship(boolean retvalue)
			            {
							//isInMyFollower = retvalue;
							isFollowing = retvalue;
							handler.obtainMessage(TWEET_VIEW_FOLLOW).sendToTarget();
							handler.obtainMessage(TWEET_LOAD_end).sendToTarget();
			            }

			            @Override public void onException(TwitterException e, int method) 
			            {	            	
			            	Log.d(TAG, "Fail to get ="+e.getMessage());			            	
							Log.d(TAG, "We are not friends");
							handler.obtainMessage(TWEET_VIEW_FOLLOW).sendToTarget();
							handler.obtainMessage(TWEET_LOAD_end).sendToTarget();
			            }			
			        });       	     
			    	break;
			    }
			    case TWEET_GET_STATUS:
			    {
			    	twitterA.showAsync(statusID, new TwitterAdapter() 
			        {
						@Override public void gotShow(Status st)
			            {
							isInFavor = st.isFavorited();
							Log.d(TAG, "after get the status");
							handler.obtainMessage(TWEET_LOAD_end).sendToTarget();
			            }

			            @Override public void onException(TwitterException e, int method) 
			            {	            	
			            	Log.d(TAG, "Fail to get ="+e.getMessage());
			            	handler.obtainMessage(TWEET_LOAD_end).sendToTarget();
			            }			
			        });       	     
			    	break;			    	
			    }
			    case FOLLOW_USER_DETAILS:
			    {
			    	String tid = msg.getData().getString(TWITTER_ID);
			    	getUserDetails(tid);
			    	break;
			    }
			    case FOLLOW_USER_DETAILS_END:
			    {
			    	end();
			    	Log.d(TAG, "FOLLOW_USER_DETAILS_END");
			    	break;
			    }
			    case FOLLOW_CREATE_UI:
			    {
			    	List<TwitterInfo> twitterinfolist = new ArrayList<TwitterInfo>();
	            	//0 following, 1 follower, 2 updates, 3 favorites
			    	
	            	twitterinfolist.add(new TwitterInfo(TwitterInfo.following,user.getFriendsCount(),"following"));
	            	twitterinfolist.add(new TwitterInfo(TwitterInfo.follower,user.getFollowersCount(),"followers"));
	            	twitterinfolist.add(new TwitterInfo(TwitterInfo.updates,user.getStatusesCount(),"updates(user timeline)"));
	            	twitterinfolist.add(new TwitterInfo(TwitterInfo.favorites,user.getFavouritesCount(),"favorites"));
	            	
	            	TwitterInfoAdapter infoadapter = new TwitterInfoAdapter(TwitterTweetsDetailActivity.this, twitterinfolist);
	            	myInfo.setAdapter(infoadapter);    
			    	break;
			    }
			    case TWEET_LOAD_end:
			    {
			    	//finishLoading(twitterName);
			    	break;
			    }
			    case  TWEET_VIEW_FOLLOW:
			    {
			    	setFollowingText();
			    	break;
			    }
			    //do action
			    case  TWEET_FOLLOW_END:
                {
                    Log.d(TAG, "end call follow api");
                    if(msg.getData().getBoolean(RESULT, false) ==true)
                    {
                        isFollowing = true;
                    }
                    setFollowingText();
                    
                    break;
                }
                case TWEET_UNFOLLOW_END:
                {
                    Log.d(TAG, "end call un follow api");
                    if(msg.getData().getBoolean(RESULT, false) == true)
                    {
                        isFollowing = false;
                    }
                    setFollowingText();
                    break;
                }
			    case  TWEET_FAVOR_END:
			    {
			    	Log.d(TAG, "end call favor api");
			    	if(msg.getData().getBoolean(RESULT, false) == true)
			    	    isInFavor = true;
			    	
			    	break;
			    }
			    case TWEET_UNFAVOR_END:
			    {
			    	Log.d(TAG, "end call follow api");
			    	if(msg.getData().getBoolean(RESULT, false) == true)
                        isInFavor = false;
			    	
			    	break;
			    }
			}
		}
	}
	
	private void getUserDetails(String tid)
	{
		//view data from db first
		//handler.obtainMessage(GET_DATA_FROM_DB).sendToTarget();
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
    	begin();
    	twitterA.getUserDetailSimplyAsync(String.format("%1$s", tid), new TwitterAdapter() 
        {
			@Override public void gotUserDetailSimply(UserWithoutStatus extuser)
            {
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				user = extuser;
				Log.d(TAG, "After get user detail="+extuser);			
				
				handler.obtainMessage(FOLLOW_CREATE_UI).sendToTarget();
				handler.obtainMessage(FOLLOW_USER_DETAILS_END).sendToTarget();
				
				//save the data to db
				saveSimplyUserToDB(user);
            }

            private void saveSimplyUserToDB(UserWithoutStatus user) 
            {							
            	Follow fuser = orm.new Follow();
            	//fuser.isFollower = user.following;
            	fuser.UID = user.getId();
            	fuser.Name = user.getName();
            	fuser.SName = user.getScreenName();
            	fuser.ProfileImgUrl = user.getProfileImageURL().toString();
            	List<Follow> uses = new ArrayList<Follow>();
            	uses.add(fuser);
				orm.AddTwitterUser(uses);
				uses = null;
			}

			@Override public void onException(TwitterException e, int method) 
            {	   
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
            	Log.d(TAG, "Fail to get ="+e.getMessage());			            	
				Log.d(TAG, "We are user detail");
				if(isInAynscTaskAndStoped())
             	{
             		Log.d(TAG, "User stop passive");
             	}
             	else
             	{
					handler.obtainMessage(FOLLOW_USER_DETAILS_END).sendToTarget();					
             	}
            }			
        });       	    
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {   
        	//TODO
            //wait for stop or finish the job
            //
            this.stopLoading();
            System.gc();            
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void createHandler() 
    {
        handler = new HandlerLoad();        
    }
}
