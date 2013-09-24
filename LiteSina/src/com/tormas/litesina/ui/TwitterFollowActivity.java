package com.tormas.litesina.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import oms.sns.TwitterUser;
import com.tormas.litesina.*;
import com.tormas.litesina.providers.SocialORM;
import com.tormas.litesina.providers.SocialORM.Follow;
import com.tormas.litesina.ui.adapter.FollowAdapter;
import com.tormas.litesina.ui.view.FollowItemView;
import oms.sns.service.facebook.util.StringUtils;
import twitter4j.AsyncTwitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import twitter4j.SimplyUser;

public class TwitterFollowActivity extends StatusViewBaseActivity{

	private static final String TAG = "TwitterFollowActivity";
    
	private int    currentPage=0;	
	private int    pageCount=0;
	private int    pagesize=20;
	
	private long    next_cursor = -1;
	private long    pre_cursor =  -1;
	private long    current_cursor = -1;
	private int     count = 100;
	
	
	private List<SimplyUser> followings = new ArrayList<SimplyUser>();
	private List<SimplyUser> followers  = new ArrayList<SimplyUser>();
	private List<SimplyUser> searchResult  = new ArrayList<SimplyUser>();
	
	private List<SimplyUser> data;
	private ListView followList;
	private EditText keyEdit;
	private Button   searchDo;
	private View     searchSpan;   
	
	private View        facebook_slider_span;
	private ImageButton pre_slide;
	private ImageButton next_slide;
	private TextView    current_slide;
	
	private MyWatcher watcher;
	private String   twitterid="";	
	private boolean  isFollowerView ;
	private boolean  isFollowingView;	
	private boolean  isFindpeopleView;

	private String displayname="";
	private String keyword;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_follow);
        Log.d(TAG, "onCreate="+this);
        followList = (ListView)this.findViewById(R.id.twitter_follow_list);
        followList.setFocusableInTouchMode(true);
        followList.setFocusable(true);
        followList.setOnCreateContextMenuListener(this);
        followList.setOnItemClickListener(listItemClickListener);
        
        facebook_slider_span = this.findViewById(R.id.facebook_slider_span);
        //facebook_slider_span.setVisibility(View.VISIBLE);
        pre_slide  = (ImageButton)this.findViewById(R.id.pre_slide);
        pre_slide.setOnClickListener(preSlideClick);
        next_slide = (ImageButton)this.findViewById(R.id.next_slide);
        next_slide.setOnClickListener(nextSlideClick);        
        current_slide = (TextView)this.findViewById(R.id.current_slide);
        
        processIntent();
    }
	
	public void setTitle() 
	{
		if(isFindpeopleView)
		   finalTitle = getString(R.string.twitter_find_people);	
		else
		   finalTitle = displayname;
	}   
	
	private void processIntent()
	{
	    isFollowerSearch = this.getIntent().getBooleanExtra(FOLLOWER_SEARCH_VIEW, false);
        isFollowerView   = this.getIntent().getBooleanExtra(FOLLOWER_VIEW, false);
        isFollowingView  = this.getIntent().getBooleanExtra(FOLLOWING_VIEW, false);
        isFindpeopleView = this.getIntent().getBooleanExtra("forfindpeople", false);
        
        if(isFollowerSearch == true)
        {
            showSearch();
        	twitterid = twitterid_db;
        }        
        else
        {
            //begin search
            showSearch();
            //end search            
	        twitterid = this.getIntent().getStringExtra(TWITTER_ID);
	        displayname = this.getIntent().getStringExtra("displayname");
	        if(isEmpty(displayname))
	        {
	        	displayname = this.getIntent().getStringExtra(TWITTER_UserName);	        	
	        }
	        
	        isForFollowing = isFollowingView==true?true:false;
	        isForFollowing = isFollowerView==true?false:true;     
	        
        }
        
        if(isFindpeopleView)
        {
        	facebook_slider_span.setVisibility(View.GONE);
        	twitter_action.setVisibility(View.VISIBLE);
        }
        
        //try to get the follow or following
        SocialORM.Account account = orm.getTwitterAccount();
		pagesize = orm.getFollowViewCount();       
        if(checkTwitterAccount(this, account) == true)
		{        	 
            //for other user following, we just do add following
            if(!StringUtils.isEmpty(twitterid)&& twitterid.equalsIgnoreCase(twitterid_db))
            {
            	isForLoginuser = true;
            	if(isForFollowing)
            		displayname = getString(R.string.twitter_my_following);
            	else
            		displayname = getString(R.string.twitter_my_follower);
            }
            
        	//don't do for search
        	loadAction();        	
		}  
        
        //to remember title
        setTitle();
	}
	
	private void launchLoadFindPeople()
	{	
		if(isEmpty(keyword) == false)
		{
			Message msd = handler.obtainMessage(FIND_PEOPLE_BEGIN);
			msd.getData().putString("query",keyword);
			msd.sendToTarget();
		}
		else
		{
			Log.d(TAG, "no need to find");
		}
	}
	
	public void titleSelected() 
    {		
		// title selected just for find people
		if(isFindpeopleView)
		{   
			keyword = keyEdit.getText().toString().trim();
			
			//reset
			curTwitterPage = 1;
			reachlastpage  = false;
			launchLoadFindPeople();
		}
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) 
        {
    	    Log.d(TAG, "new request for follow view="+intent);
            super.onNewIntent(intent);

	    next_cursor = -1;
        pre_cursor =  -1;
        current_cursor = -1;
	
	    setIntent(intent);
	    processIntent();
	}
	
	
	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		private String TAG="Twitter Item clicked";
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			if(gestureprocessed == true)
			{
				gestureprocessed = false;
				return;
			}
			else
			{
				if(FollowItemView.class.isInstance(v))
				{
					 SimplyUser user = ((FollowItemView)v).getUser();
				     Log.d(TAG, "userDetailOnClik you click first one="+user.name);
				     Intent intent = new Intent(mContext, TwitterUserDetailsActivity.class);
					 TwitterUser tuser = new TwitterUser();
					 //a bug for twitter, she can't get the right following value
					 //TODO, how to fix??
					 tuser.following = user.following;
					 tuser.id        = user.id;
					 tuser.name      = user.name;
					 tuser.notifications   = user.notifications;
					 tuser.profileImageUrl = user.profileImageUrl;
					 tuser.screenName      = user.screenName;		
					 tuser.description     = user.description;
					  
					 intent.putExtra("currentuser", tuser);
					 ((Activity)mContext).startActivityForResult(intent, TwitterBaseActivity.TWITTER_FOLLOWING);
				}
			}			
		}
	};
	
	private void showSearch()
	{
        searchSpan = this.findViewById(R.id.twitter_follow_search_span);
        searchSpan.setVisibility(View.VISIBLE);
                    
        searchDo = (Button)this.findViewById(R.id.follow_search_do);
        searchDo.setBackgroundResource(R.drawable.search);
        searchDo.setOnClickListener(searchClikcListener);
        
        keyEdit = (EditText)this.findViewById(R.id.follow_embedded_text_editor);
        watcher = new MyWatcher();         
        keyEdit.addTextChangedListener(watcher);
	}
	
	View.OnClickListener preSlideClick = new View.OnClickListener()    		
	{
		public void onClick(View arg0) 
		{
			goPrePage();	
		}		
	};
    
	View.OnClickListener nextSlideClick = new View.OnClickListener()    		
	{
		public void onClick(View arg0) 
		{
			goNextPage();		
		}		
	};
	
	 @Override
     protected boolean hasMore()
     {   
	     if((next_cursor !=0 && next_cursor!=-1 ) || next_cursor%count>0 )
	     {
	         return true;
	     }
	     else
	     {
	         return false;
	     }
     }

     @Override
     protected boolean isTheFirst()
     {
         //return this.curTwitterPage <= 1;
         if(pre_cursor == -1 || current_cursor == -1 || current_cursor == 0)
         {
             return true;
         }
         else if(pre_cursor == 0 && current_cursor >0)
         {
             return false;
         }
         else if(pre_cursor > 0)
         {
             return false;
         }
         else
         {
             return true;
         }
     }

	
	View.OnClickListener searchClikcListener = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			//do search action, search content from and get the user details
		    String keyword = keyEdit.getText().toString().trim();
		    doSearch(keyword);
		}
	};
	
	private void doSearch(String key)
    {
        searchResult.clear();
        if(data != null)
        {
            if(key != null && key.length()>0)
            {
                for(int i=0;i<data.size();i++)
                {
                    SimplyUser user = data.get(i);
                    if(user.name.toLowerCase().indexOf(key.toLowerCase())>=0)
                    {
                        searchResult.add(user);
                    }
                }
                //show UI
                //refresh the UI
                FollowAdapter fa = new FollowAdapter(TwitterFollowActivity.this, searchResult, isForFollowing==true?0:1);
                followList.setAdapter(fa);
            }
            else
            {
                goFistPage();
            }
        }
    }
	
	protected void loadNextPage()
	{
		super.loadNextPage();	
		if(isFindpeopleView)
		{
		  launchLoadFindPeople();
		}
		else
		{
		  current_cursor = next_cursor;
		  launchFollowLoad();
		}
	}
	protected void loadPrePage()
	{
		super.loadPrePage();
		reachlastpage = false;	
		if(isFindpeopleView)
		{
		  launchLoadFindPeople();
		}
		else
		{
		  current_cursor = pre_cursor;
		  launchFollowLoad();
		}
	}
	
	@Override
	protected boolean goPrePage()
	{
		if(currentPage == 0)
			return false;
		
		currentPage = currentPage-1;
		current_slide.setText(String.format("%1$s/%2$s", currentPage+1, pageCount));
		boolean ret = goPage(currentPage);
		
		System.gc();
		return ret;
	}
	
	@Override
	protected boolean  goNextPage()
	{
		if(currentPage==pageCount-1 || pageCount ==0)
			return false;
		
		currentPage = currentPage+1;
		current_slide.setText(String.format("%1$s/%2$s", currentPage+1, pageCount));
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

	private boolean goPage(int page)
	{
		boolean ret = false;
		if(data != null && data.size() > 0)
		{
			List<SimplyUser> tempdata = new ArrayList<SimplyUser>();
			for(int i=0;i<data.size();i++)
			{
				if(i>=page * pagesize && i<(page+1)*pagesize)
				{
			        tempdata.add(data.get(i));
				}
			}
			
			//refresh the UI
			FollowAdapter fa = new FollowAdapter(TwitterFollowActivity.this, tempdata, isForFollowing==true?0:1);
			followList.setAdapter(fa);
			ret = true;
		}
		return ret;
	}
	
	@Override public void loadAction()
    {
        super.loadAction();
	
	    //reget the page
        pagesize = orm.FollowViewCount;
        if(!isFindpeopleView)
        {
        	launchFollowLoad();
        } 
    }
    @Override
    protected void loadAfterSetting()
    {
        super.loadAfterSetting();
        twitterA = null;
        if(!isFindpeopleView)
        {
        	launchFollowLoad();
        } 
        else
        {
        	launchLoadFindPeople();
        }
    }

	
	@Override
	protected void loadRefresh()
	{
		super.loadRefresh();
		if(!isFindpeopleView)
		{
			launchFollowLoad();
		}
		else
		{
			launchLoadFindPeople();
		}
		
	}
	
	private void launchFollowLoad() 
	{
		if(twitterA == null)
		{
			SocialORM.Account account = orm.getTwitterAccount();			
			if(checkTwitterAccount(TwitterFollowActivity.this, account))
			{	
				twitterA = new AsyncTwitter(account.token, account.token_secret,true);				
			}
			else
			{
				return;
			}    
		}
		
		if(isFollowerSearch == true)
    	{
			Message msg =handler.obtainMessage(FOLLOW_SEARCH_BEGIN);		
			msg.sendToTarget();
    	}
		else
		{
			Message msg =handler.obtainMessage(FOLLOW_LOAD_BEGIN);
			msg.getData().putString(TWITTER_ID, twitterid);
			msg.sendToTarget();
		}
	}

	@Override
	protected void doStopFollowing(String twitterId)
    {          	
		Message message = basichandler.obtainMessage(TWEET_UNFOLLOW);
        message.getData().putString(TWITTER_ID, twitterId);
        message.sendToTarget();      
    }
	@Override
    protected void doFollowing(String twitterID)
    {	
	    Message message = basichandler.obtainMessage(TWEET_FOLLOW);
        message.getData().putString(TWITTER_ID, twitterID);
        message.sendToTarget();
    }
	    
	@Override
	public void createHandler() 
	{
		handler = new FollowHandler();
	}
	
	//handler follow message
	public class FollowHandler extends Handler
	{
	    public FollowHandler()
        {
            super();
            Log.d(TAG, "new FollowHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case FIND_PEOPLE_BEGIN:
                {
                	String query = msg.getData().getString("query");
                	loadFindPeople(query);
                	break;
                }
	            case FOLLOW_SEARCH_BEGIN:
	            {
	            	break;
	            }
	            case FOLLOW_SEARCH_END:
	            {
	            	break;
	            }
	            case TWEET_FOLLOW_END://in follower, follow the un-following user
				{
					boolean suc = msg.getData().getBoolean(RESULT);
	            	if(suc)
	            	{
	            		String tid = msg.getData().getString(TWITTER_ID);
	            		Log.d(TAG, "follow "+tid);
	            		//update the status
	            		if(data != null)
	            		{
	            			for(int i=0;i<data.size();i++)
	            			{
	            				if(data.get(i).screenName.equalsIgnoreCase(tid))
	            				{
	            					data.get(i).following = true;
	            					//update UI items
	            					updateCurrentPageUI();
	            					break;
	            				}
	            			}
	            		}
	            	}
	            	else
	            	{
	            		Log.d(TAG, "fail follow");
	            	}					
					break;
				}
	            case TWEET_UNFOLLOW_END://in following, remove the following
	            {
	            	boolean suc = msg.getData().getBoolean(RESULT);
	            	if(suc)
	            	{
	            		String tid = msg.getData().getString(TWITTER_ID);
	            		Log.d(TAG, "unfollow "+tid);
	            		if(data != null)
	            		{
	            			for(int i=0;i<data.size();i++)
	            			{
	            				if(data.get(i).screenName.equalsIgnoreCase(tid))
	            				{
	            					data.get(i).following = false;
	            					
	            					data.remove(i);
	            					//update UI items
	            					updateCurrentPageUI();
	            					break;
	            				}
	            			}
	            		}
	            	}
	            	else
	            	{
	            		Log.d(TAG, "fail unfollow");
	            	}	            	
					break;
	            }
	            case FOLLOW_LOAD_BEGIN:
	            {	
            		Log.d(TAG, "before get favor FOLLOW_LOAD_BEGIN");
            		
            		String sname=msg.getData().getString(TWITTER_ID);
	            	callMyFollow(sname);	            	
	            	break;
	            }
	            case FOLLOW_CREATE_UI:
	            {
	            	if(data != null && data.size()>0)
	            	{
	            	    pageCount=data.size()/pagesize + 1;
	            	    if((pageCount-1)*pagesize == data.size())
	            	    {
	            	    	pageCount = pageCount -1;
	            	    }
	            	    currentPage=0;	                
		                goFistPage();	       
	            	}
	            	
	                
	                handler.obtainMessage(FOLLOW_LOAD_END).sendToTarget(); 
	            	break;
	            }
	            case FOLLOW_LOAD_END:
	            {
	            	end();
	            	if(msg.getData().getBoolean("NOMORE") == true)
	            	{
	            		Toast.makeText(TwitterFollowActivity.this, R.string.facebook_phonebook_nomore, Toast.LENGTH_SHORT).show();
	            	}
					//restore the network
				    twitterA.resumeCallNetWork();
							
				    if(pageCount > 0)
				    {
				    	current_slide.setText(String.format("%1$s/%2$s", currentPage+1, pageCount));
				    }
				    else
				    {
				    	current_slide.setText("");
				    }
	            	break;
	            }
            }
        }
	}
    private void notifyLoading() 
	{
    	if(isForFollowing)
	        notify.notifyOnce(R.string.twitter_following_loading, R.drawable.twitter, 30*1000);
    	else
    		notify.notifyOnce(R.string.twitter_follower_loading, R.drawable.twitter, 30*1000);
	}
    
    private void loadFindPeople(String query)
    {
    	if(this.isInProcess())
    	{
    		Log.d(TAG,"in finding people, please wait");
    		return ;
    	}
    	
    	begin();
    	Log.d(TAG,"before find people loadFindPeople");
    	synchronized(mLock)
    	{
    		inprocess = true;
    	}
    	if(this.isFindpeopleView)
    	{
    		twitterA.getFindPeopleAsync(query, curTwitterPage, -1, new TwitterAdapter(){
    			@Override public void gotFollowersSimply(List<SimplyUser> users)
	            {
					synchronized(mLock)
			    	{
			    	    inprocess = false;
			    	}
					
					if(users.size() == 0)
					{
						reachlastpage = true;	
						if(curTwitterPage > 1)
						{
							curTwitterPage = curTwitterPage -1;
						}
						
						Message mds = handler.obtainMessage(FOLLOW_LOAD_END);
						mds.getData().putBoolean("NOMORE", true);
						handler.sendMessage(mds);
						return ;
					}
					
					followings.clear();
					followings = users;
					
					data= followings;
					
					//re-get the data, so set the page as 0
					currentPage = 0;
					//get users
                    if(donotcallnetwork == false)//I am still alive
                    {
    	            	Log.d(TAG, "After find people count="+users.size());	
    	            	handler.obtainMessage(FOLLOW_CREATE_UI).sendToTarget();
                    }
                    else
                    {
                    	handler.obtainMessage(FOLLOW_LOAD_END).sendToTarget();
                    }
	            }

	            @Override public void onException(TwitterException e, int method) 
	            {	
	            	try {
	            	    if(failCallMethod!=null)
						   failCallMethod.invoke(TwitterFollowActivity.this, (Object[])null);
					} catch (IllegalArgumentException e1) {						
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {						
						e1.printStackTrace();
					} catch (InvocationTargetException e1) {						
						e1.printStackTrace();
					}					
					
	            	processTwitterException(e, method);
	            }		
    		});
    	}
    }
	    
	  
	public void callMyFollow(String userID) 
	{		
		begin();
		
    	Log.d(TAG, "before get Follow callMyFollow");
    	notifyLoading();  
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}
    	
		if(this.isForFollowing)
		{
			twitterA.getFriendsSimplyAsync(userID,curTwitterPage,current_cursor,new TwitterAdapter() 
	        {
				@Override public void gotFriendsSimply(List<SimplyUser> users)
	            {
					synchronized(mLock)
			    	{
			    	    inprocess = false;
			    	}
					
					if(users.size() > 0)
                    {
                        next_cursor = users.get(0).next_cursor;
                        pre_cursor = users.get(0).pre_cursor;      
                        
                        reSetPreCursor();
                        users.remove(0);
                    }
                    
					if(users.size() == 0)
					{
						reachlastpage = true;	
						if(curTwitterPage == 1)
						{
							curTwitterPage = curTwitterPage -1;
						}
						
						Message mds = handler.obtainMessage(FOLLOW_LOAD_END);
						mds.getData().putBoolean("NOMORE", true);
						handler.sendMessage(mds);
						return ;
					}
					
					
					followings.clear();
					followings = users;
					
					data= followings;
					
					//re-get the data, so set the page as 0
					currentPage = 0;
					//get users
                    if(donotcallnetwork == false)//I am still alive
                    {
    	            	Log.d(TAG, "After get following count="+users.size());	    	            	
    	            	cancelNotify();
    	            	handler.obtainMessage(FOLLOW_CREATE_UI).sendToTarget();
                    }
                    else
                    {
                    	handler.obtainMessage(FOLLOW_LOAD_END).sendToTarget();
                    }
	                
					//TODO
					//save the following to database or file system
					//
                    //AddFollowingToDataBase(users);
                    handler.post( new RunTask(users)
                    {
                    	public void run()
                    	{	 
                    		SimplyUser[] us = new SimplyUser[obj.size()];
                    	    us = obj.toArray(us);
                            new SaveFollowings(orm).execute(us);
                    	}
                    });
	            }

	            @Override public void onException(TwitterException e, int method) 
	            {	
	            	try {
	            	    if(failCallMethod!=null)
						   failCallMethod.invoke(TwitterFollowActivity.this, (Object[])null);
					} catch (IllegalArgumentException e1) {						
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {						
						e1.printStackTrace();
					} catch (InvocationTargetException e1) {						
						e1.printStackTrace();
					}					
					
	            	processTwitterException(e, method);
	            }			
	        });    
		}
		else
		{
			twitterA.getFollowersSimplyAsync(userID, curTwitterPage,current_cursor, new TwitterAdapter() 
	        {
				@Override public void gotFollowersSimply(List<SimplyUser> users)
	            {
					synchronized(mLock)
			    	{
			    	    inprocess = false;
			    	}
					
					if(users.size() > 0)
					{
    					next_cursor = users.get(0).next_cursor;
                        pre_cursor = users.get(0).pre_cursor;         
                        reSetPreCursor();
                        users.remove(0);
					}
                    
					if(users.size() == 0)
					{
						reachlastpage = true;	
						if(curTwitterPage > 1)
						{
							curTwitterPage = curTwitterPage -1;
						}
						
						Message mds = handler.obtainMessage(FOLLOW_LOAD_END);
						mds.getData().putBoolean("NOMORE", true);
						handler.sendMessage(mds);
						return ;
					}
					
					followers.clear();
					followers = users;
					data = followers;
					
					//re-get the data, so set the page as 0
					currentPage = 0;
					
					//get users
                    if(donotcallnetwork == false)//I am still alive
                    {
    	            	Log.d(TAG, "After get following count="+users.size());	    	            	
    	            	cancelNotify();
    	            	handler.obtainMessage(FOLLOW_CREATE_UI).sendToTarget();
                    }
                    else
                    {
                        handler.obtainMessage(FOLLOW_LOAD_END).sendToTarget();
                    }
					//TODO
					//save the following to database or file system
					//
                    //AddFollowerToDataBase(users);
                    handler.post( new RunTask(users)
                    {
                    	public void run()
                    	{
                    		SimplyUser[] us = new SimplyUser[obj.size()];
                    	    us = obj.toArray(us);
                            new SaveFollows(orm).execute(us);
                    	}
                    });
	            }

	            @Override public void onException(TwitterException e, int method) 
	            {		            	
	            	try {
	            	    if(failCallMethod!=null)
						   failCallMethod.invoke(TwitterFollowActivity.this, (Object[])null);
					} catch (IllegalArgumentException e1) {						
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {						
						e1.printStackTrace();
					} catch (InvocationTargetException e1) {						
						e1.printStackTrace();
					}					
	            	processTwitterException(e, method);
	            }			
	        });    
		}
	}

	protected void reSetPreCursor() 
	{
	    if(pre_cursor != 0 && pre_cursor == current_cursor)
        {
            if(next_cursor == 0)
            {
                pre_cursor = pre_cursor - pre_cursor%count;
                pre_cursor = (pre_cursor-count)>=0?pre_cursor-count:pre_cursor;
            }
            else
            {
                pre_cursor = (pre_cursor - count)>=0?pre_cursor-count:0;
            }
        }
    }

    private static class RunTask implements Runnable
	{
		List<SimplyUser> obj;
		public RunTask(List<SimplyUser>  obj)
		{
			super();
			this.obj = obj;			
		}
		
		public void run()
		{
			
		}
	}
	
	protected void AddFollowingToDataBase(List<SimplyUser> users) 
	{	
		List<Follow> follows = new ArrayList<Follow>();
		if(users!=null && users.size()>0){
			for(SimplyUser user : users){
				Follow follow = orm.new Follow();
				follow.UID = user.getId();
				follow.Name = user.getName();
				follow.SName = user.getScreenName();
				follow.ProfileImgUrl = user.getProfileImageURL();
				follow.isFollower = false;
				follows.add(follow);
			}		
		}
		orm.AddTwitterUser(follows);
	}

	private class SaveFollows extends android.os.AsyncTask<SimplyUser, Void, Void>
	{		
		public SaveFollows(SocialORM orm)
		{
			super();
			this.orm = orm;
			
			Log.d(TAG, "create SaveFollows");
		}
		
		public SocialORM orm;
		
		@Override
		protected Void doInBackground(SimplyUser... pbs) 
		{			
			if(pbs != null)
			{
				Log.d(TAG, "exec add following="+pbs.length);
				List<SimplyUser> follows = new ArrayList<SimplyUser>();
				for(SimplyUser pb:pbs)
				{
					follows.add(pb);
				}
				orm.AddTwitterFollowerUser(follows);
				
				follows.clear();
				follows = null;
			}
			
			pbs = null;
			return null;
		}		
	}
	private class SaveFollowings extends android.os.AsyncTask<SimplyUser, Void, Void>
	{		
		public SaveFollowings(SocialORM orm)
		{
			super();
			this.orm = orm;
			Log.d(TAG, "create SaveFollowings");
		}
		
		public SocialORM orm;
		
		@Override
		protected Void doInBackground(SimplyUser... pbs) 
		{			
			if(pbs != null)
			{
				Log.d(TAG, "exec add follower="+pbs.length);
				
				List<Follow> follows = new ArrayList<Follow>();
				if(pbs!=null && pbs.length>0)
				{
					for(SimplyUser user : pbs)
					{
						Follow follow = orm.new Follow();
						follow.UID = user.getId();
						follow.Name = user.getName();
						follow.SName = user.getScreenName();
						follow.isFollower = false;
						follows.add(follow);
					}		
				}
				orm.AddTwitterUser(follows);
				
				follows.clear();
				follows = null;				
			}
			pbs = null;
			return null;
		}
		
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
	    	if(twitterA != null)
	    	{
	    	    handler.obtainMessage(FOLLOW_LOAD_END).sendToTarget();
	    	}
     	}
    	
	    synchronized(TwitterFollowActivity.class)
        {
            if(donotcallnetwork == false)//I am still alive
            {            	
            	cancelNotify();
            	Log.d(TAG, "Fail to get ="+e.getMessage());
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
		else
		{
			handler.obtainMessage(FOLLOW_LOAD_END).sendToTarget();
		}
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
	
  
}
