package com.tormas.litetwitter.ui;

import java.util.ArrayList;
import java.util.List;

import oms.sns.TwitterUser;
import com.tormas.litetwitter.R;
import com.tormas.litetwitter.providers.SocialORM;
import com.tormas.litetwitter.providers.SocialORM.Follow;
import com.tormas.litetwitter.ui.view.ImageRun;
import com.tormas.litetwitter.ui.view.SNSItemView;
import twitter4j.AsyncTwitter;
import twitter4j.SimplyUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.UserWithoutStatus;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TwitterMyAccountActivity  extends StatusViewBaseActivity 
{
	private String TAG="TwitterMyAccountActivity";
	private ListView myInfo;	
	private UserWithoutStatus user;
	private SimplyUser dbuser;
	private ImageView userImageView;
	private TextView  userNameView;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_account);
        Log.d(TAG, "onCreate="+this);
        
        myInfo = (ListView)this.findViewById(R.id.twitter_info_list);
        myInfo.setFocusableInTouchMode(true);
        myInfo.setFocusable(true); 
        myInfo.setOnItemClickListener(itemClick);
        
        userImageView = (ImageView)this.findViewById(R.id.twitter_img_ui);
        userNameView  = (TextView)this.findViewById(R.id.twitter_username);
                
        this.setTitle(R.string.twitter_myaccount_activity);
        
        //try to get the follow or following
        SocialORM.Account account = orm.getTwitterAccount();
        if(checkTwitterAccount(this, account) == true)
		{
        	loadAction();        	
		}
    }
	
	AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener()
    {
		public void onItemClick(AdapterView<?> arg0, View v, int arg2,long arg3)
		{	
			if(TwitterInfoItemView.class.isInstance(v))
	        {
	            TwitterInfo info = ((TwitterInfoItemView)v).item;
	            
	            if(info!=null)
	            {
	            	//0 following, 1 follower, 2 updates, 4 favorites
	            	switch(info.type)
	            	{
	                	case TwitterInfo.following:
	                	{  
	                		if(user.getFriendsCount() > 0)
	                		{
		                		Intent intent = new Intent(TwitterMyAccountActivity.this, TwitterFollowActivity.class);
		                    	intent.putExtra(FOLLOWING_VIEW, true);
		                    	intent.putExtra(TWITTER_ID,    twitterid_db);
		                    	startActivity(intent);
	                		}
	                		break;
	                	}
	                	case TwitterInfo.follower:
	                	{   
	                		if(user.getFollowersCount() > 0)
	                		{		                	
		                		Intent intent = new Intent(TwitterMyAccountActivity.this, TwitterFollowActivity.class);
		                    	intent.putExtra(FOLLOWER_VIEW, true);
		                    	intent.putExtra(TWITTER_ID,    twitterid_db);
		                    	startActivity(intent);
	                		}
	                		break;
	                	}
	                	case TwitterInfo.updates:
	                	{
	                		if(user.getStatusesCount() > 0)
	                		{
		                		Intent intent = new Intent(TwitterMyAccountActivity.this, TwitterTweetsActivity.class);
		        	        	intent.putExtra("userline_uid", twitterid_db);
		        	        	//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        	        	startActivity(intent);
	                		}
	                		break;
	                	}
	                	case TwitterInfo.favorites:
	                	{
	                		if(user.getFavouritesCount() > 0)
	                		{
		                		Intent intent = new Intent(TwitterMyAccountActivity.this, TwitterFavoritesActivity.class);
		        	        	intent.putExtra(FAVORITEVIEW, true);
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
	protected void loadRefresh()
    {
	    SocialORM.Account account = orm.getTwitterAccount();
        if(checkTwitterAccount(this, account) == true)
        {
            loadAction();           
        }        
    }
	
	public void setTitle() 
	{
		finalTitle = getString(R.string.twitter_myaccount_activity);		
	}   
	
	/*
	@Override
	public void titleSelected() 
	{		
		super.titleSelected();
	
		//try to get from web site
		Message msg =handler.obtainMessage(FOLLOW_USER_DETAILS);		
	    msg.getData().putString(TWITTER_ID, twitterid_db);
		msg.sendToTarget();	
	}
	*/



	@Override public void loadAction()
    {
        super.loadAction();	
	    launchAccountLoad();
    }
	
	private void launchAccountLoad() 
	{
		if(twitterA == null)
		{
			SocialORM.Account account = orm.getTwitterAccount();			
			if(checkTwitterAccount(TwitterMyAccountActivity.this, account))
			{				
				twitterA = new AsyncTwitter(account.token, account.token_secret,true);				
			}
			else
			{
				return;
			}    
		}
		
		Message msg =handler.obtainMessage(FOLLOW_USER_DETAILS);		
	    msg.getData().putString(TWITTER_ID, twitterid_db);
		msg.sendToTarget();		
		//set the pre-UI
		//handler.obtainMessage(GET_DATA_FROM_DB).sendToTarget();		
	}

	@Override
	public void createHandler() 
	{
		handler = new UserAccountHandler();
	}
	
	private void getUserDetails()
	{
		//view data from db first
		//handler.obtainMessage(GET_DATA_FROM_DB).sendToTarget();
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
    	begin();
    	twitterA.getUserDetailSimplyAsync(String.format("%1$s", twitterid_db), new TwitterAdapter() 
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
					handler.obtainMessage(GET_DATA_FROM_DB).sendToTarget();
             	}
            }			
        });       	    
	}
	
	//handler follow message
	public class UserAccountHandler extends Handler
	{
	    public UserAccountHandler()
        {
            super();
            Log.d(TAG, "new UserAccountHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case FOLLOW_USER_DETAILS:
	            {
	            	getUserDetails();
	            	break;
	            }
	            case FOLLOW_CREATE_UI:
	            {
	            	/*List<SimplyUser> tempdata = new ArrayList<SimplyUser>();
	            	try{
	            		SimplyUser suser = new SimplyUser();
		            	suser.name = user.getName();
		            	suser.screenName = user.getScreenName();
		            	suser.profileImageUrl = user.getProfileImageURL().toString();
		            	tempdata.add(suser);
	            	}catch (TwitterException ne){}
	    			//refresh the UI
	    			FollowAdapter fa = new FollowAdapter(TwitterMyAccountActivity.this, tempdata, 1);
	    			myInfo.setAdapter(fa);*/ 
	            	userNameView.setText(user.getName());
	            	setImageView(user.getProfileImageURL().toString());
	            	List<TwitterInfo> twitterinfolist = new ArrayList<TwitterInfo>();
	            	//0 following, 1 follower, 2 updates, 3 favorites
	            	twitterinfolist.add(new TwitterInfo(TwitterInfo.following,user.getFriendsCount(),"following"));
	            	twitterinfolist.add(new TwitterInfo(TwitterInfo.follower,user.getFollowersCount(),"followers"));
	            	twitterinfolist.add(new TwitterInfo(TwitterInfo.updates,user.getStatusesCount(),"updates(user timeline)"));
	            	twitterinfolist.add(new TwitterInfo(TwitterInfo.favorites,user.getFavouritesCount(),"favorites"));
	            	
	            	TwitterInfoAdapter infoadapter = new TwitterInfoAdapter(TwitterMyAccountActivity.this,twitterinfolist);
	            	myInfo.setAdapter(infoadapter);        	
	            	break;
	            }	          
	            case FOLLOW_USER_DETAILS_END:
	            {
	            	end();
	            	break;
	            }
	            case GET_DATA_FROM_DB:
	            {
	            	/*ArrayList<Follow> follows = orm.getTwitterUser(twitterid_db);
	            	List<SimplyUser> tempdata = new ArrayList<SimplyUser>();
	            	try{
	            		Follow item;
	            		if(follows.size() > 0)
	            		{
	            			item = follows.get(0);
	            			dbuser = new SimplyUser();
	            			dbuser.name = item.Name;
	            			dbuser.screenName = item.SName;
	            			dbuser.profileImageUrl = item.ProfileImgUrl;
			            	tempdata.add(dbuser);
	            		}
		            	
	            	}catch (TwitterException ne){}
	    			//refresh the UI
	    			FollowAdapter fa = new FollowAdapter(TwitterMyAccountActivity.this, tempdata, 1);
	    			myInfo.setAdapter(fa);*/
	            		
	            	break;
	            }
            }
        }
	}
	
	@Override
	public void stopMyself() 
	{	    
		if(isInProcess() == true)
		{
                    stoping();
		}		
	}
	
	private void setImageView(String imageUrl)
	{
	  if(imageUrl!=null)
	  {
		  if(imageUrl.endsWith("_mini.jpg"))
		  {
			 imageUrl = imageUrl.substring(0, imageUrl.length()-8)+"bigger.jpg"; 
		  }
		  else if(imageUrl.endsWith("_normal.jpg"))
		  {
			  imageUrl = imageUrl.substring(0,imageUrl.length()-10)+"bigger.jpg";
		  }
		  
		  Log.d(TAG, "imageUrl is === "+imageUrl);
		  ImageRun imagerun = new ImageRun(handler, imageUrl, 1);
		  imagerun.use_avatar = true;
		  imagerun.addHostAndPath = true;
		  imagerun.setImageView(userImageView);		
		  imagerun.post(imagerun);
	  }
	}
	
		
	@Override
	protected void loadSearchForFollowing()
	{
		super.loadSearchForFollowing();
		
		//go to user detail search		
		Intent intent = new Intent(this, TwitterUserDetailsActivity.class);
    	intent.putExtra(FOLLOWING_VIEW, true);
    	intent.putExtra(TWITTER_ID,    twitterid_db);
    	TwitterUser tuser = new TwitterUser();
    	tuser.screenName = twitterid_db;
    	if(user != null)
    	{
    	    tuser.profileImageUrl = user.getProfileImageURL().toString();
    	    tuser.id = user.getId();   
    	    tuser.name = user.getName();
    	    tuser.screenName = user.getScreenName();
    	}
    	else
    	{
    		if(dbuser == null)
    		{
    			//need wait to get user infomation
    			Toast.makeText(this, R.string.twitter_load_search_message, Toast.LENGTH_SHORT).show();
    			return;
    		}
    		tuser.profileImageUrl = dbuser.profileImageUrl;     	    
     	    tuser.name            = dbuser.name;
     	    tuser.screenName      = dbuser.screenName;    		
    	}
    	
    	intent.putExtra("currentuser",    tuser);    	
    	intent.putExtra("search",    true);
    	
    	this.startActivityForResult(intent, TWITTER_FOLLOWING);
	}
	
	@Override   
	protected void loadFollowersFromFollowing()
	{
    	super.loadFollowersFromFollowing();
    	
    	Intent intent = new Intent(this, TwitterFollowActivity.class);
    	intent.putExtra(FOLLOWER_VIEW, true);
    	intent.putExtra(TWITTER_ID,    twitterid_db);
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
    	intent.putExtra(TWITTER_ID,    twitterid_db);
    	this.startActivityForResult(intent, TWITTER_FOLLOWING);
    	
    	System.gc();
    	finish();
    }
    
    /**********add by jessie for ************/
    
    public static class TwitterInfo {
    	public int number;
    	public String title;
    	public int type; //0 following, 1 follower, 2 updates, 4 favorites
    	
    	public static final int following=0;
    	public static final int follower =1;
    	public static final int updates  =2;
    	public static final int favorites=4;
    	
    	
    	public TwitterInfo(int type,int number,String title)
    	{
    		this.type = type;
    		this.number = number;
    		this.title = title;
    	}
    	
        int getType()
    	{
    		return type;
    	}
    	
    }
    
    public static class TwitterInfoAdapter extends BaseAdapter{
    	private final String TAG = "TwitterInfoAdapter";        
        private Context         mContext;   
        private List<TwitterInfo> infolist;
        
        public TwitterInfoAdapter(Context con,List<TwitterInfo> infolist)
        {
        	mContext = con;
        	this.infolist = infolist; 
        	Log.d(TAG, "create FacebookEventAdapter");
        }
    	
    	public int getCount() 
    	{		
    		return infolist.size();
    	}

    	public Object getItem(int pos) {
    		return infolist.get(pos);
    	}

    	public long getItemId(int pos) {
    		
    		return infolist.get(pos).hashCode();
    	}

    	public View getView(int position, View convertView, ViewGroup arg2) 
    	{		
    		 if (position < 0 || position >= getCount()) 
    		 {
                 return null;    
    		 }         
    		 TwitterInfoItemView v=null;
    		 TwitterInfo obj = (TwitterInfo)getItem(position);
             if (convertView == null /*|| convertView instanceof SeparatorView*/) {
                 v = new TwitterInfoItemView(mContext, obj);
             } else {
                  v = (TwitterInfoItemView) convertView;
                  v.setContentItem(obj);
             }        
            // v.chooseNotifyListener();
             return v;
    	}	
    }
    
    public static class TwitterInfoItemView extends SNSItemView{
       
    	private TwitterInfo item;
    	private Context mContext;
    	private TextView  numberview;   	
    	
    	
    	public TwitterInfo getItem()
    	{
    		return item;
    	}
    	public TwitterInfoItemView(Context context) 
    	{
			super(context);
			this.mContext = context;
			
		}
    	
        public TwitterInfoItemView(Context ctx, AttributeSet attrs)
        {
			super(ctx, attrs);
			this.mContext = ctx;
		}	
		
		public TwitterInfoItemView(Context ctx, TwitterInfo info)
		{
			super(ctx);
			this.mContext = ctx;
			this.item = info;
			init();
		}
		
		public void setContentItem(TwitterInfo info)
		{
			this.item = info;
			setUI();
		}
        	
		@Override
		public String getText() 
		{
			return null;
		}
		
		private void init() 
		{
			Log.d("TwitterInfoItemView",  "call TwitterInfoItemView init");
			LayoutInflater factory = LayoutInflater.from(mContext);
			removeAllViews();
			
			//container
			FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);			
			FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
			view.setLayoutParams(paras);
			view.setVerticalScrollBarEnabled(true);
			addView(view);
			
			//child 1
			View v  = factory.inflate(R.layout.twitter_info_item, null);	
			v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
			view.addView(v);	
			
			numberview  =	(TextView)v.findViewById(R.id.twitter_info);
			setUI();
		}	
		
		private void setUI()
		{
		   //0 following, 1 follower, 2 updates, 4 favorites
		   numberview.setText(String.format("%2$s (%1$s)", String.valueOf(item.number), item.title));		   
	    }
		
		@Override
		protected void onFinishInflate() 
		{	
			super.onFinishInflate();		
			init();
		}		

    	
    }
}
