package com.tormas.litesina.ui;
import com.tormas.litesina.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.tormas.litesina.providers.SocialORM;
import com.tormas.litesina.providers.SocialORM.Follow;
import com.tormas.litesina.ui.adapter.TwitterSelectUserAdapter;
import com.tormas.litesina.ui.view.TwitterSelectUserItemView;
import twitter4j.SimplyUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
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
import android.widget.ListView;
import android.widget.Toast;

public class TwitterUserSelectActivity extends StatusViewBaseActivity{
	private final String TAG = "TwitterUserSelectActivity";
	
	private ListView followList;
	private EditText keyEdit;
	private Button   searchDo;
	private View     searchSpan;   
	
	private MyWatcher watcher;
	private List<Follow> searchResult;
	private List<Follow> follows;
	private List<String> snames = new ArrayList<String>();
	
	private String twitterid;
	//twitter API  setting
	int pagesize= 100;
	private long    next_cursor = -1;
    private long    pre_cursor =  -1;
    private long    current_cursor = -1;
	private int     count = 100;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
 	   super.onCreate(savedInstanceState);
 	   setContentView(R.layout.twitter_follow);
 	   
 	  followList = (ListView)this.findViewById(R.id.twitter_follow_list);
      followList.setFocusableInTouchMode(true);
      followList.setFocusable(true);
      followList.setOnCreateContextMenuListener(this);       

      searchSpan = this.findViewById(R.id.twitter_follow_search_span);
      searchSpan.setVisibility(View.VISIBLE);
                  
      searchDo = (Button)this.findViewById(R.id.follow_search_do);
      searchDo.setBackgroundResource(R.drawable.search);
      searchDo.setOnClickListener(seachListener);
      
      keyEdit = (EditText)this.findViewById(R.id.follow_embedded_text_editor);
      watcher = new MyWatcher();         
      keyEdit.addTextChangedListener(watcher);
      
      searchResult = new ArrayList<Follow>();
      
      noImpactTitle = true;
      setHeadTitle(getString(R.string.facebook_user_select));
      
      setTitle(R.string.facebook_user_select); 	  
             
      
      SocialORM.Account account = orm.getTwitterAccount();	       
      if(checkTwitterAccount(this, account) == true)
      {
    	  twitterid = twitterid_db;
          loadFollows();
	  } 	  
      twitter_action.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void loadRefresh()
	{
		super.loadRefresh();
		
		loadFollows();
	}
    
    public void setTitle()
    {
    	finalTitle = getString(R.string.facebook_user_select);
    }
    
    @Override
	public void createHandler() 
    {
    	handler = new SelectHandler();		
	}
	
	private void loadFollows() 
	{		
		handler.obtainMessage(TWITTER_FOLLOWS_GET).sendToTarget();
	}

	View.OnClickListener seachListener = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            String key = keyEdit.getText().toString().trim();
            doSearch(key);
        }
    };
    @Override
    protected void loadNextPage()
	{
        current_cursor = next_cursor;
		super.loadNextPage();
		loadFollows();
	}
    @Override
	protected void loadPrePage()
	{
        current_cursor = pre_cursor;
		super.loadPrePage();
		reachlastpage = false;	
		loadFollows();
	}
	
    @Override 
	protected boolean hasMore()
	{
    	/*if(reachlastpage == true)
			 return false;
    	
		if(follows != null)
		{
			 return follows.size() > 0;
		}
		else
			return false;*/
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
		//return this.curTwitterPage == 1;
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
	
    public void titleSelected() 
	{
		getSelectedItems();		
		Intent data = new Intent();
		if(snames.size()>0)
		{
			String[] uids = new String[snames.size()];
			for(int i=0;i<snames.size();i++)
			{
				uids[i] = snames.get(i);
			}
		    data.putExtra("snames", uids);
		}
		
		this.setResult(100, data);
		this.finish();
	}

    
    protected void getSelectedItems()
    {
    	snames.clear();
    	if(follows != null)
    	{
			for(int i=0;i<follows.size();i++)
	    	{
	    	    Follow v = follows.get(i);
	    	    
    	    	if(v.selected)
    	    	{
    	    		snames.add(v.SName);		        	    		
    	    	}
	    	    
	    	}
    	}
    }
    
    @Override
    protected void doSelectAll(boolean sel) 
    {		
    	for(int i=0;i<follows.size();i++)
    	{
    	    Follow v = follows.get(i);
    	    v.selected = sel;
    	}
    	followList.requestLayout();
    	followList.invalidate();
    	
    	//process for UI
    	for(int i=0;i<followList.getChildCount();i++)    		 
        {
            View v = followList.getChildAt(i);
            if(TwitterSelectUserItemView.class.isInstance(v))
            {
                ((TwitterSelectUserItemView)v).setCheckBoxSelected(sel);
            }
        }

	}

	private void doSearch(String key)
    {
        searchResult.clear();        
        if(follows != null && key != null && key.length()>0)
        {
            for(int i=0;i<follows.size();i++)
            {
                Follow user = follows.get(i);
                if(user.Name.toLowerCase().indexOf(key.toLowerCase())>=0)
                {
                    searchResult.add(user);
                }
            }
            //show UI
            //refresh the UI
            TwitterSelectUserAdapter sa = new TwitterSelectUserAdapter(TwitterUserSelectActivity.this, searchResult);
            followList.setAdapter(sa);
        }
        else
        {
        	showUI();        	
        }
    }

	private void showUI() 
	{
		handler.post( new Runnable()
		{
			public void run()
			{
				if(follows != null)
		    	{
					 TwitterSelectUserAdapter sa = new TwitterSelectUserAdapter(TwitterUserSelectActivity.this, follows);
		             followList.setAdapter(sa);
		    	}	
				else
				{
					followList.setAdapter(null);
			    }
			}
		});
	}

	
	final int TWITTER_FOLLOWS_GET = 0;
	final int TWITTER_FOLLOWS_UI   = 1;
	final int TWITTER_FOLLOWS_GET_END = 2;
	
	private class SelectHandler extends Handler 
    {
        public SelectHandler()
        {
            super();            
            Log.d(TAG, "new SelectHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case TWITTER_FOLLOWS_GET:                	
	            {
	            	//get from database firstly, then call from real network
	            	getFollowsFromDB();
	            	
	            	getFollows();
	            	break;
	            }
	            case TWITTER_FOLLOWS_UI:
	            {	
	            	Log.d(TAG, "TWITTER_FOLLOWS_UI");
	            	showUI();	            	
	            	break;
	            }
	            case TWITTER_FOLLOWS_GET_END:
	            {            
	            	end();
	            	if(msg.getData().getBoolean("NOMORE") == true)
	            	{
	            		Toast.makeText(TwitterUserSelectActivity.this, R.string.facebook_phonebook_nomore,Toast.LENGTH_SHORT).show();
	            	}
	            	
                	setTitle(R.string.facebook_user_select);
	            	break;
	            }               
            }
        }
    }
	
	private void notifyLoading() 
    {
    	notify.notifyOnce(R.string.facebook_friends_loading, R.drawable.twitter, 30*1000);		
	}
    
	private void getFollowsFromDB()
	{
		follows = orm.getFollowerUser();
		if(follows.size() > 0)
		{
			showUI();
		}
	}
	
	private void getFollows()
    {
	    if(this.isInProcess() == true)
	    {
	        return;
	    }
		begin();	    
    	Log.d(TAG, "before get follows");
    	notifyLoading();  
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	} 
 
		twitterA.getFollowersSimplyAsync(twitterid, curTwitterPage,current_cursor, new TwitterAdapter()
	    {
				@Override  public void gotFollowersSimply(List<SimplyUser> users)
	            {
					synchronized(mLock)
			    	{
			    	    inprocess = false;
			    	}
					if(users.size()>0)
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
						
						Message mds = handler.obtainMessage(TWITTER_FOLLOWS_GET_END);
						mds.getData().putBoolean("NOMORE", true);
						handler.sendMessage(mds);
						return ;
					}
					
					AddFollowerToDataBase(users);
					
					//get users
                    if(donotcallnetwork == false)//I am still alive
                    {
    	            	Log.d(TAG, "After get twitter follow count="+users.size());	    	            	
    	            	cancelNotify();
    	            	handler.obtainMessage(TWITTER_FOLLOWS_UI).sendToTarget();
                    }
                    
                    handler.obtainMessage(TWITTER_FOLLOWS_GET_END).sendToTarget();
	            }

	            @Override public void onException(TwitterException e, int method) 
	            {	         
	                Log.d(TAG, "get FollowersSimply excetpion "+e.getMessage());
	            	synchronized(mLock)
			    	{
			    	    inprocess = false;
			    	}
	            	if(donotcallnetwork == false )//I am still alive
	                {   
	                     cancelNotify();
	                }   
	            	try {
	            	    if(failCallMethod!=null)
						  failCallMethod.invoke(TwitterUserSelectActivity.this, (Object[])null);
					} catch (IllegalArgumentException e1) {						
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {						
						e1.printStackTrace();
					} catch (InvocationTargetException e1) {						
						e1.printStackTrace();
					}	
					
					handler.obtainMessage(TWITTER_FOLLOWS_GET_END).sendToTarget();
					getFollowsFromDB();       	
	            }			
	    });    
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

    protected void AddFollowerToDataBase(List<SimplyUser> users) 
	{	
            if(follows == null)
            {
                follows = new ArrayList<Follow>();
            }

	    if(users!=null && users.size()>0)
	    {
                boolean isIn=false;
		for(SimplyUser user : users)
		{
		    Follow follow = orm.new Follow();
		    follow.UID = user.getId();
		    follow.Name = user.getName();
		    follow.SName = user.getScreenName();
		    follow.ProfileImgUrl = user.getProfileImageURL();
		    follow.isFollower = true;
                    isIn = false;
                    for(Follow temp: follows)
                    {
                        if(temp.UID == follow.UID)
                        {
                            isIn = true;
                            break;
                        }
                    }
                    if(isIn == false)
                    {
		        follows.add(follow);
                    }
		}			
	    }		
	    orm.AddTwitterUser(follows);
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
