package com.tormas.litetwitter.ui;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URLEncodedUtils;

import oms.sns.TwitterStatus;
import oms.sns.TwitterUser;
import com.tormas.litetwitter.*;
import com.tormas.litetwitter.ui.view.FollowItemView;
import com.tormas.litetwitter.ui.view.SNSItemView;
import com.tormas.litetwitter.ui.view.SelectSimplyStatusItemView;
import com.tormas.litetwitter.ui.view.SimplyStatusItemView;
import twitter4j.SimplyStatus;
import twitter4j.Tweet;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public abstract class StatusViewBaseActivity extends TwitterBaseActivity implements View.OnCreateContextMenuListener{
	final String TAG = "StatusViewBaseActivity";
	
	protected boolean inDeleteMode=false;
	protected boolean isForFollowing;	
	protected boolean isFollowerSearch;
	protected ContextMenu contextMenu;
	protected Menu optionMenu;
	protected int      curTwitterPage=1;
	protected boolean  reachlastpage =false;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	     super.onCreate(savedInstanceState);
	     Log.d(TAG, "onCreate="+this);
	     
	     this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}
	
	@Override
	public void createHandler() {
		// TODO Auto-generated method stub
		
	}

	protected void setUIReadyProgress()
	{
		
	}
	
	AdapterView.OnItemClickListener statusTweetsClickListener = new AdapterView.OnItemClickListener()
	{
		private String TAG="Twitter Item clicked";
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			
			if(SimplyStatusItemView.class.isInstance(v))
			{
				 //Intent intent = new Intent(mContext, TwitterTweetsDetailActivity.class);
			    Intent intent = new Intent(mContext,TwitterOptionActivity.class);
				 SimplyStatusItemView view = (SimplyStatusItemView)v;
				 int type = view.getType();
				 SimplyStatus status = view.getStatus();
				 Tweet tweet         = view.getTweet();
				 if(type ==0 && status != null)
				 {
					 TwitterStatus twitem = new TwitterStatus();
			         twitem.createdAt = status.getCreatedAt().getTime();
			         twitem.id        = status.getId();		        
			         twitem.isFavorited       = status.isFavorited();		        
			         twitem.text    = status.getText();
			         
			         twitem.user = new TwitterUser();
			         twitem.user.id = status.getUser().getId();		         
			         twitem.user.name           = status.getUser().getName();
			         twitem.user.profileImageUrl = status.getUser().getProfileImageURL().toString();
			         twitem.user.screenName      = status.getUser().getScreenName();	
			         twitem.user.notifications    = status.getUser().notifications;
			         twitem.user.following    = status.getUser().following;
			         
					 intent.putExtra("currentstatus", twitem);
					 intent.putExtra("fromstatus", true);
					 //((TwitterBaseActivity)mContext).startActivityForResult(intent, TwitterBaseActivity.TWEET_DETAIL);
					mContext.startActivity(intent);
				 }
				 else if(type ==1 && tweet != null)
				 {
					 TwitterStatus twitem = new TwitterStatus();
			         twitem.createdAt = tweet.getCreatedAt().getTime();
			         twitem.id        = tweet.getId();		         
			         twitem.inReplyToUserId   = tweet.getToUserId();
			         twitem.isFavorited       = false;
			         twitem.text    = tweet.getText();
			         
			         twitem.user = new TwitterUser();
			         twitem.user.id = tweet.getFromUserId();		         		         
			         twitem.user.name           = tweet.getFromUser();
			         twitem.user.profileImageUrl = tweet.getProfileImageUrl();
			         twitem.user.screenName      = tweet.getFromUser();
			                 
					 intent.putExtra("currenttweet", twitem);
					 intent.putExtra("fromtweet", true);
					 
					// ((TwitterBaseActivity)mContext).startActivityForResult(intent, TwitterBaseActivity.TWEET_DETAIL);
					 mContext.startActivity(intent);
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
            //titleLayout = (LinearLayout)findViewById(R.id.header_title_layout);
            //titleLayout.setVisibility(View.GONE);
            
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
						loadRefresh();						
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
						loadRefresh();						
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
    

	protected boolean gestureprocessed=false;
	protected boolean goPrePage()
	{
		return false;
	}
	protected boolean goNextPage()
	{
		return false;
	}
	
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
    {
        AdapterView.AdapterContextMenuInfo i = (AdapterView.AdapterContextMenuInfo) menuInfo;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.status_item_context, menu);
        
        contextMenu = menu;
        //set copy
        if(SNSItemView.class.isInstance(i.targetView))
        {
            SNSItemView snsView = (SNSItemView)i.targetView;
            if(isEmpty(snsView.getText()) == false)
            {
                contextMenu.findItem(R.id.context_copy).setVisible(true);
            }
            else
            {
                contextMenu.findItem(R.id.context_copy).setVisible(false);
            }
        }   
        //set title
        if(SelectSimplyStatusItemView.class.isInstance(i.targetView))
        {
        	SelectSimplyStatusItemView view = (SelectSimplyStatusItemView)i.targetView;	
        	contextMenu.setHeaderTitle(view.getName());
        }
        else if(FollowItemView.class.isInstance(i.targetView))
        {
        	FollowItemView view = (FollowItemView)i.targetView;        	
	        contextMenu.setHeaderTitle(view.getName());
        }
	    else if(SimplyStatusItemView.class.isInstance(i.targetView))
	    {   
	        SimplyStatusItemView view = (SimplyStatusItemView)i.targetView;	     
	        contextMenu.setHeaderTitle(view.getName());
	        
	        //find whether have link, if have, open in browser
	        List<String> links = view.getLinks();
	        if(links != null && links.size() > 0)
	        {
	            menu.findItem(R.id.context_link_browser).setVisible(true);
	        }
	        else
	        {
	        	menu.findItem(R.id.context_link_browser).setVisible(false);
	        }
	        
	        List<String> screennames = view.getUserScreenName();
	        if(screennames != null && screennames.size()>0)
	        {
	        	menu.findItem(R.id.context_to_user_account).setVisible(true);	        	
	        }
	        else
	        {
	        	menu.findItem(R.id.context_to_user_account).setVisible(false);
	        }
	        
	        ArrayList<String> searchStrs = view.getSearchString();
	        if(searchStrs != null && searchStrs.size()>0)
	        {
	        	menu.findItem(R.id.context_to_search).setVisible(true);
	        }
	        else
	        {
	        	menu.findItem(R.id.context_to_search).setVisible(false);
	        }
	        
	        
	        
	    }
        
        //TODO
        //customize the context menu,
        //in favorities, tweets, search
        //the context is different    
        if(TwitterTweetsActivity.class.isInstance(this))
        {
             menu.findItem(R.id.context_retweet).setVisible(true);
        }
        else if(TwitterFavoritesActivity.class.isInstance(this))
        {
        	TwitterFavoritesActivity fa = (TwitterFavoritesActivity)this;
        	boolean others = fa.IamOthers();
        	if(others)
        	{
    		    menu.findItem(R.id.context_unfavorite).setVisible(false);             
        	}
        	else
        	{
	        	//don't show favor memu
	        	if(inDeleteMode)//no menu
	        	{
	        	    menu.findItem(R.id.context_favorite).setVisible(false);
	                menu.findItem(R.id.context_unfavorite).setVisible(false);
	                menu.findItem(R.id.context_reply).setVisible(false);
	                menu.findItem(R.id.context_forward).setVisible(false);
	                menu.findItem(R.id.context_direct).setVisible(false);
	        	}
	        	else
	        	{
	        	    menu.findItem(R.id.context_favorite).setVisible(false);
	                menu.findItem(R.id.context_unfavorite).setVisible(true);
	        	}    
        	}
        }
        else if(TwitterMessageActivity.class.isInstance(this))
        {
        	//don't show favor memu
        	if(inDeleteMode)//no menu
        	{
        	    menu.findItem(R.id.context_favorite).setVisible(false);
                menu.findItem(R.id.context_unfavorite).setVisible(false);
                menu.findItem(R.id.context_reply).setVisible(false);
                menu.findItem(R.id.context_forward).setVisible(false);
                menu.findItem(R.id.context_direct).setVisible(false);
                menu.findItem(R.id.context_delete).setVisible(false);
        	}
        	else
        	{
        		menu.findItem(R.id.context_favorite).setVisible(false);
                menu.findItem(R.id.context_unfavorite).setVisible(false);
                menu.findItem(R.id.context_direct).setVisible(false);
                 
        		menu.findItem(R.id.context_reply).setVisible(true);
                menu.findItem(R.id.context_forward).setVisible(true);
                menu.findItem(R.id.context_delete).setVisible(true);
        	}    	
        }       
        else if(TwitterFollowActivity.class.isInstance(this))
        {
        	menu.findItem(R.id.context_favorite).setVisible(false);
            menu.findItem(R.id.context_unfavorite).setVisible(false);
            menu.findItem(R.id.context_reply).setVisible(false);
            menu.findItem(R.id.context_forward).setVisible(false);            
            menu.findItem(R.id.context_delete).setVisible(false);
            
            if(isFollowerSearch == false)
            {
                menu.findItem(R.id.context_direct).setVisible(true);
            	
                if(isForLoginuser == true)
                {
		            if(isForFollowing)
		                menu.findItem(R.id.context_stop_following).setVisible(true);                
		            else
		                menu.findItem(R.id.context_following).setVisible(true);
                }
                else//for other user, we just do add following
                {
                	menu.findItem(R.id.context_following).setVisible(true);
                }
            }
            else
            {
            	menu.findItem(R.id.context_direct).setVisible(false);
            }
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) 
    {   
        AdapterView.AdapterContextMenuInfo i = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        long     statusid;
        String   twitterId;
        String content;
        
        if(TwitterFavoritesActivity.class.isInstance(this) && 
        		SelectSimplyStatusItemView.class.isInstance(i.targetView))
        {
        	SelectSimplyStatusItemView view = (SelectSimplyStatusItemView)i.targetView;
        	statusid  = view.getStatusID();
	        twitterId = view.getScreenName();	        
        	content = view.getText();
        }
        else if(TwitterFollowActivity.class.isInstance(this))
        {
        	FollowItemView view = (FollowItemView)i.targetView;
        	statusid  = -1;
	        twitterId = view.getScreenName();
	        content   = view.getText();	        
        }
	    else
	    {	
	        Object obj = ((SimplyStatusItemView)i.targetView).GetContent();
	        SimplyStatusItemView view = (SimplyStatusItemView)i.targetView;
	        statusid  = view.getStatusID();
	        twitterId = view.getTwitterID();
	        content   = view.getText();	 
	        
	        Log.d(TAG, "select target view statusid&switterId&content is "+statusid+"&&"+twitterId+"&&"+content);	    	
	    }
        
        switch (item.getItemId()) 
        {
            case R.id.context_copy:
            {
                if(SNSItemView.class.isInstance(i.targetView))
                {   
                    String text = ((SNSItemView)(i.targetView)).getText();
                    doCopy(text);
                }
                break;
            }
            case R.id.context_link_browser:
            {
            	if(SimplyStatusItemView.class.isInstance(i.targetView))
            	{
            		SimplyStatusItemView view = (SimplyStatusItemView)i.targetView;
            		List<String> links = view.getLinks();
            		if(links != null && links.size() > 0)
            		{
            			for(int step=0;step<links.size();step++)
            			{
	            			Intent intent = new Intent(Intent.ACTION_VIEW);
	                    	intent.setData(Uri.parse(links.get(step)));
	                    	try
	                    	{
	                    	    startActivity(intent);
	                    	}
	                    	catch(android.content.ActivityNotFoundException ne)
	                    	{
	                    		Log.e(TAG, "fail to start activity="+ne.getMessage());
	                    		Toast.makeText(mContext.getApplicationContext(), "Can't open "+links.get(step), Toast.LENGTH_SHORT).show();
	                    	}
            			}
            		}
            	}
            	break;
            }
            case R.id.context_to_user_account:
            {
            	if(SimplyStatusItemView.class.isInstance(i.targetView))
            	{
            		SimplyStatusItemView view = (SimplyStatusItemView)i.targetView;
            		ArrayList<String> screennames = view.getUserScreenName();
            		Intent intent = new Intent(mContext,TwitterListActivity.class);
            		intent.putStringArrayListExtra("foruseraccount",screennames);
            		this.startActivity(intent);
            	}
            	
            	break;
            }
            
            case R.id.context_to_search:
            {
            	if(SimplyStatusItemView.class.isInstance(i.targetView))
            	{
            		SimplyStatusItemView view = (SimplyStatusItemView)i.targetView;
            		ArrayList<String> searchStrs = view.getSearchString();
            		Intent intent = new Intent(mContext,TwitterListActivity.class);
            		intent.putStringArrayListExtra("forsearch",searchStrs);
            		this.startActivity(intent);
            	}
            	
            	break;
            }
            case R.id.context_reply: 
            { 
                Intent intent = new Intent(mContext, TwitterComposeActivity.class);             
                intent.putExtra(STATUS_ID,     statusid);    
                intent.putExtra(TWITTER_ID,    twitterId); 
                intent.putExtra(REPLY, true);
                startActivityForResult(intent, TWITTER_DONOTHING);       
                break;
            }
            case R.id.context_forward:
            {                
                Intent intent = new Intent(mContext, TwitterComposeActivity.class);             
                intent.putExtra(STATUS_ID,     statusid);    
                intent.putExtra(TWITTER_ID,    twitterId);                 
                intent.putExtra(FORWARD, true);
                intent.putExtra(CONTENT, content);             
                startActivityForResult(intent, TWITTER_DONOTHING);   
                //doRetweet();
                break;
            }
            case R.id.context_retweet:
            {
                Message message = basichandler.obtainMessage(TWITTER_RETWEET);
                message.getData().putLong(STATUS_ID, statusid);
                message.sendToTarget();
                break;
            }
            case R.id.context_favorite:
            {
                Message message = basichandler.obtainMessage(TWEET_FAVOR);
                message.getData().putLong(STATUS_ID,       statusid);    
                message.getData().putString(TWITTER_ID,    twitterId); 
                message.sendToTarget();  
                break;
            }  
            case R.id.context_unfavorite:
            {
            	Message message = basichandler.obtainMessage(TWEET_UNFAVOR);
            	message.getData().putLong(STATUS_ID,       statusid);    
                message.getData().putString(TWITTER_ID,    twitterId); 
                message.sendToTarget();  
                break;
            }
            case R.id.context_direct:
            {
                Intent intent = new Intent(mContext, TwitterComposeActivity.class);
                intent.putExtra(STATUS_ID,     statusid);    
                intent.putExtra(TWITTER_ID,    twitterId);        
                intent.putExtra(DIRECT, true);
                startActivityForResult(intent, TWITTER_DONOTHING);          
                break;
            }    
            case R.id.context_delete:
            {
            	ContextDeleteAction(statusid);
            	break;
            }
            case R.id.context_stop_following:
            {
                doStopFollowing(twitterId);
            	break;
            }
            case R.id.context_following:
            {
            	doFollowing(twitterId);
            	break;
            }            
        }
        
        return true;
    }
    
    protected void doStopFollowing(String twitterId){}
    protected void doFollowing(String twitterId){}
    protected void ContextDeleteAction(long statusid) {}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
    {
	     super.onCreateOptionsMenu(menu);	     
	       
	     MenuInflater inflater = getMenuInflater();
		 inflater.inflate(R.menu.twitter_status_menu, menu);
	    
	     return true;
	}
    
    @Override public boolean onPrepareOptionsMenu(Menu menu)
    {    	
    	 super.onPrepareOptionsMenu(menu);
	     
    	 //favor have no this menu
    	
    	 if(isInProcess() == true)
	     {
    		 menu.findItem(R.id.menu_twitter_loading_stop).setVisible(true);
    		 menu.findItem(R.id.menu_status_refresh).setVisible(false);    
	     }
    	 else
    	 {
    		 menu.findItem(R.id.menu_status_refresh).setVisible(true);    	
    		 menu.findItem(R.id.menu_twitter_loading_stop).setVisible(false);
    	 }
	    
    	 //process Message activity
    	 if(TwitterMessageActivity.class.isInstance(this))
    	 {
    		 if(inDeleteMode)
        	 {
        		 menu.findItem(R.id.menu_status_refresh).setVisible(false);
        		 menu.findItem(R.id.menu_status_settings).setVisible(false);
        		 menu.findItem(R.id.twitter_menu_selectall).setVisible(true);
        		 menu.findItem(R.id.twitter_menu_unselectall).setVisible(true);
        	 }
    		 else
    		 {
                 //menu.findItem(R.id.menu_status_refresh).setVisible(true);
    			 menu.findItem(R.id.menu_status_settings).setVisible(false);
        		 menu.findItem(R.id.twitter_menu_selectall).setVisible(false);
        		 menu.findItem(R.id.twitter_menu_unselectall).setVisible(false);
    		 }
    		 
    		 menu.findItem(R.id.menu_twitter_inbox_messgae).setVisible(true);
    		 menu.findItem(R.id.menu_twitter_outbox_messgae).setVisible(true);
    	 }
    	 else if(TwitterFavoritesActivity.class.isInstance(this))
    	 {
    		 TwitterFavoritesActivity fa = (TwitterFavoritesActivity)this;
         	 boolean others = fa.IamOthers();
         	 if(others)
         	 {
         	     menu.findItem(R.id.menu_status_settings).setVisible(false);
        		 menu.findItem(R.id.twitter_menu_selectall).setVisible(false);
        		 menu.findItem(R.id.twitter_menu_unselectall).setVisible(false);            
         	 }
         	 else
         	 {    	  	 
	        	 if(inDeleteMode)
	        	 {
	        		 menu.findItem(R.id.menu_status_refresh).setVisible(false);
	        		 menu.findItem(R.id.menu_status_settings).setVisible(false);
	        		 menu.findItem(R.id.twitter_menu_selectall).setVisible(true);
	        		 menu.findItem(R.id.twitter_menu_unselectall).setVisible(true);
	        	 }
	        	 else
	        	 {
	        		 menu.findItem(R.id.menu_status_settings).setVisible(false);
	        		 menu.findItem(R.id.twitter_menu_selectall).setVisible(false);
	        		 menu.findItem(R.id.twitter_menu_unselectall).setVisible(false);
	        	 }
         	 }
         	 
         	 menu.findItem(R.id.menu_next_page).setVisible(false); 
         	 menu.findItem(R.id.menu_pre_page).setVisible(false); 
         	//for navigation
         	/*if(hasMore())
            {
                menu.findItem(R.id.menu_next_page).setVisible(true);
                menu.findItem(R.id.menu_next_page).setTitle(R.string.sns_page_more);
            }
            else
            {
                menu.findItem(R.id.menu_next_page).setVisible(false); 
            }
            
            if(isTheFirst() == false)
            {
                menu.findItem(R.id.menu_pre_page).setVisible(true);
                menu.findItem(R.id.menu_pre_page).setTitle(R.string.sns_page_pre);
            }
            else
            {
                menu.findItem(R.id.menu_pre_page).setVisible(false);                 
            }*/
    	 }
         else if(TwitterFollowActivity.class.isInstance(this))
         {
        	 menu.findItem(R.id.menu_status_settings).setVisible(false);
        	 
        	 if(hasMore())
        	 {
                 menu.findItem(R.id.menu_next_page).setVisible(true);
                 menu.findItem(R.id.menu_next_page).setTitle(R.string.sns_page_more);
        	 }
        	 else
        	 {
        		 menu.findItem(R.id.menu_next_page).setVisible(false); 
        	 }
             
             if(isTheFirst() == false)
             {
                 menu.findItem(R.id.menu_pre_page).setVisible(true);
                 menu.findItem(R.id.menu_pre_page).setTitle(R.string.sns_page_pre);
             }
             else
             {
            	 menu.findItem(R.id.menu_pre_page).setVisible(false);                 
             }
                     	       	 
        	 if(isFollowerSearch == true)
        	 {
        		 //remove search option menu
        	 }
         }
         else if(TwitterUserSelectActivity.class.isInstance(this))
         {
        	 menu.findItem(R.id.twitter_menu_selectall).setVisible(true);
        	 menu.findItem(R.id.twitter_menu_unselectall).setVisible(true);
        	 if(hasMore())
        	 {
                 menu.findItem(R.id.menu_next_page).setVisible(true);
                 menu.findItem(R.id.menu_next_page).setTitle(R.string.sns_page_more);
        	 }
        	 else
        	 {
        		 menu.findItem(R.id.menu_next_page).setVisible(false); 
        	 }
             
             if(isTheFirst() == false)
             {
                 menu.findItem(R.id.menu_pre_page).setVisible(true);
                 menu.findItem(R.id.menu_pre_page).setTitle(R.string.sns_page_pre);
             }
             else
             {
            	 menu.findItem(R.id.menu_pre_page).setVisible(false);                 
             }
         }
         else if(TwitterUserDetailsActivity.class.isInstance(this))
         {
        	 menu.findItem(R.id.menu_follower_following).setVisible(true);
        	 menu.findItem(R.id.menu_following_followers).setVisible(true);
        	 menu.findItem(R.id.menu_following_search).setVisible(true);
        	 menu.findItem(R.id.menu_twitter_messgae).setVisible(true);        	 
        	 String search = this.getString(R.string.context_following_search);        	 
        	 menu.findItem(R.id.menu_following_search).setTitle(search + ((TwitterUserDetailsActivity)this).user.name);
         }
         else if(TwitterSearchActivity.class.isInstance(this))
         {
        	 ;
         }
         else if(TwitterMyAccountActivity.class.isInstance(this))
         {
             menu.findItem(R.id.menu_follower_following).setVisible(true);
        	 menu.findItem(R.id.menu_following_followers).setVisible(true);
        	 menu.findItem(R.id.menu_following_search).setVisible(true);
        	 menu.findItem(R.id.menu_following_search).setTitle(R.string.menu_twitter_title_search_me);
        	 menu.findItem(R.id.menu_status_settings).setVisible(true);  
        	 menu.findItem(R.id.menu_find_people).setVisible(true);
         }
         else if(TwitterUploadPictureActivity.class.isInstance(this))
         {
        	 menu.findItem(R.id.menu_status_refresh).setVisible(false);
        	 menu.findItem(R.id.twitter_menu_insert_img).setVisible(true);
        	 menu.findItem(R.id.twitter_menu_capture_photo).setVisible(true);
        	 menu.findItem(R.id.menu_twitter_loading_stop).setVisible(false);
         }
         else if(TwitterComposeActivity.class.isInstance(this))
         {
        	 menu.findItem(R.id.menu_status_refresh).setVisible(false);
         }
    	 /* it is hard to do page nav
         else if(TwitterTweetsActivity.class.isInstance(this) || TwitterFollowActivity.class.isInstance(this) )         
         {
        	
         }*/
    	 
    	 return true;
    }
    
    protected Method failCallMethod;
    protected void AddTwitterPage()
    {
    	curTwitterPage ++;    
    	Log.d(TAG, "resore the curTwitterPage++");
    }
    
    protected void SubTwitterPage()
    {
    	Log.d(TAG, "resore the curTwitterPage--");
    	curTwitterPage--;
    	if(curTwitterPage <1)
    		curTwitterPage = 1;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	boolean processed = true;
    	super.onOptionsItemSelected(item);
    
    	switch (item.getItemId()) 
        {
	        case R.id.menu_status_settings:
	        {
	        	Intent intent = new Intent(this, TwitterSettingPreference.class);            
	        	startActivityForResult(intent, TWITTER_SETTING); 
	        	break;
	        }
	        case R.id.menu_twitter_loading_stop:
	        {
	        	this.basichandler.obtainMessage(STOP_PROCESS).sendToTarget();
	        	break;
	        }	
	        case R.id.menu_status_refresh:
	        {
	        	loadRefresh();
	        	break;
	        }
	        case R.id.menu_following_search:
	        {
	        	loadSearchForFollowing();
	        	break;
	        }
	        case R.id.menu_following_followers:
	        {
	        	loadFollowersFromFollowing();
	        	break;
	        }
	        case R.id.menu_follower_following:
	        {	        	
	        	loadFollowingsFromFollower();
	        	break;
	        }
	        case R.id.menu_find_people:
	        {
	        	Intent intent = new Intent(this,TwitterFollowActivity.class);
	        	intent.putExtra("forfindpeople", true);
	        	startActivity(intent);
	        	break;
	        }
	        case R.id.menu_twitter_messgae:
            {
            	if(TwitterUserDetailsActivity.class.isInstance(this))
            	{
            		Intent intent = new Intent(this, TwitterComposeActivity.class);                    
                    intent.putExtra(TWITTER_ID, ((TwitterUserDetailsActivity)this).user.screenName);
                    intent.putExtra(DIRECT, true);
                    startActivityForResult(intent, TWITTER_DONOTHING);
            	}
            	break;
            }
	        case R.id.menu_next_page:
	        {
	        	if(this.isInProcess() == false)
	        	{
		        	curTwitterPage ++;
		        	try{
		        	    failCallMethod = StatusViewBaseActivity.class.getDeclaredMethod("SubTwitterPage", (Class[])null);
		        	}catch(java.lang.NoSuchMethodException ne){}
		        	loadNextPage();
	        	}
	        	else
	        	{
	        		Toast.makeText(StatusViewBaseActivity.this, R.string.twitter_wait_task_prompt, Toast.LENGTH_SHORT).show();
	        	}
	        	break;
	        }
	        case R.id.menu_pre_page:
	        {
	            //nextPage();
	            if(this.isInProcess() == false)
	            {
	                curTwitterPage --;
	                try{
	                    failCallMethod = StatusViewBaseActivity.class.getDeclaredMethod("AddTwitterPage", (Class[])null);
	                    }catch(java.lang.NoSuchMethodException ne){}
	                    loadPrePage();
	                    }
	            else
	            {
	                Toast.makeText(StatusViewBaseActivity.this, R.string.twitter_wait_task_prompt, Toast.LENGTH_SHORT).show();
	            }
	            break;
	        }
	        case R.id.twitter_menu_selectall:
            {
            	doSelectAll(true);
            	break;
            }
	        case R.id.menu_twitter_inbox_messgae:
	        {
	        	loadInboxMessage();
	        	break;
	        }
	        case R.id.menu_twitter_outbox_messgae:
	        {
	        	loadOutboxMessage();
	        	break;
	        }
	        case R.id.twitter_menu_unselectall:
	        {	
	        	doSelectAll(false);
	        	break;
	        }
	        case R.id.menu_message_new:
	        {
	        	loadNewMessage();
	        	break;
	        }
	        case R.id.menu_message_delete:
	        {
	        	loadDeleteMessage();
	        	break;
	        }
	        case R.id.twitter_menu_insert_img:
	        {
	     		Intent intent = new Intent(Intent.ACTION_PICK);
	            intent.setType("image/*");
	
	            try 
	            {
	                 startActivityForResult(intent,STATUS_INSERT_IMG);
	            } 
	            catch (android.content.ActivityNotFoundException e) 
	            {
	                 String message = "Can NOT pick media, mime type:\nNo Activity found to handle this action.";
	                 Log.e(TAG, message);
	            }
		        break;
	        }
	        case R.id.twitter_menu_capture_photo:
	        {     
	            requestCamera();          
	        	break;
	        }
	        default:
	        {
	        	processed = false;
	        	break;
	        }
        }
        return processed;
    }
    
    protected void loadInboxMessage()
    {
    	
    }
    
    protected void loadOutboxMessage()
    {
    	
    }
    protected void nextPage()
    {
        if(this.isInProcess() == false)
        {
            curTwitterPage ++;
            try{
                failCallMethod = StatusViewBaseActivity.class.getDeclaredMethod("SubTwitterPage", (Class[])null);
            }catch(java.lang.NoSuchMethodException ne){}
            loadNextPage();
        }
        else
        {
            Toast.makeText(StatusViewBaseActivity.this, R.string.twitter_wait_task_prompt, Toast.LENGTH_SHORT).show();
        }
    }
    
    protected void doRetweet()
    {
        
    }
    
    protected void doSelectAll(boolean sel) {
				
	}
    
    protected void requestCamera(){}

	protected boolean isTheFirst() {
        // TODO Auto-generated method stub
        return false;
    }


    protected boolean hasMore() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override public void stopProcess() 
    {
    	if(twitterA != null)
    	    twitterA.stopCallNetWork();
    	
    	stopMyself();   
    	stopLoading();    	 	
    }
    public void stopMyself()
    {
    	
    }
    
    protected void setTitle(String title, int progress)
    {
    	 setProgress(progress*100);
	     setTitle(title);
    }
}
