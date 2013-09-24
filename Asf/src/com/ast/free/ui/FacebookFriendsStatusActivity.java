package com.ast.free.ui;

import java.util.ArrayList;
import java.util.List;

import com.ast.free.R;
import com.ast.free.providers.SocialORM;
import com.ast.free.ui.AccountListener.AccountManager;
import com.ast.free.ui.adapter.FacebookStatusAdapter;
import com.ast.free.ui.view.FacebookFriendItemView;
import com.ast.free.ui.view.FacebookStatusView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.UserStatus;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class FacebookFriendsStatusActivity extends FacebookBaseActivity
{
    private final String TAG="FacebookFriendsStatusActivity";    
	private ListView friendList;	
	private View     searchSpan;
	private EditText keyEdit;
	private Button   searchDo;
	
	private List<UserStatus> statuses;
	private List<UserStatus> searchResult;
	private int currentPage = 0;
	private int pagesize    = 20;
	private int pageCount   = 1;
	private MyWatcher watcher;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_main_ui);
        friendList = (ListView)this.findViewById(R.id.facebook_wall_list);   
        friendList.setFocusableInTouchMode(true);
        friendList.setFocusable(true);
        friendList.setOnCreateContextMenuListener(this);
        friendList.setOnItemClickListener(listItemClickListener);
        
        searchSpan = this.findViewById(R.id.facebook_search_span);
        searchSpan.setVisibility(View.VISIBLE);
        keyEdit = (EditText)this.findViewById(R.id.embedded_text_editor);
        watcher = new MyWatcher();         
        keyEdit.addTextChangedListener(watcher);
        searchDo = (Button)this.findViewById(R.id.search_do);
        searchDo.setOnClickListener(seachListener);
        searchDo.setBackgroundResource(R.drawable.search);
        
        searchResult = new ArrayList<UserStatus>();
        
        setTitle("");
        SocialORM.Account account = orm.getFacebookAccount();    
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);
	        	lauchGeStatus();
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }
    }
	public void setTitle() 
	{
		title = "";		
	}
	 
    AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{	
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			Log.d(TAG, "facebook status Item clicked");
			if(gestureprocessed == true)
			{
				gestureprocessed = false;
				return;
			}
			else
			{
				if(FacebookStatusView.class.isInstance(v))
				{
					FacebookStatusView fv= (FacebookStatusView)v;
					Intent intent = new Intent(mContext, FacebookAccountActivity.class);
					intent.putExtra("uid",      fv.getUserID());
					intent.putExtra("username", fv.getUserName());
					FacebookUser user = orm.getFacebookUser(fv.getUserID());
					if(user != null)
					{
					    intent.putExtra("imageurl", user.pic_square);
					}
					
					((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
				}
			}
			
		}
	};

    
    private void doSearch(String key)
    {
        searchResult.clear();        
        if(statuses != null && key != null && key.length()>0)
        {
            for(int i=0;i<statuses.size();i++)
            {
                UserStatus user = statuses.get(i);
                if(user.message.toLowerCase().indexOf(key.toLowerCase())>=0 ||
                   user.username.toLowerCase().indexOf(key.toLowerCase())>=0)
                {
                    searchResult.add(user);
                }
            }
            //show UI
            //refresh the UI
            FacebookStatusAdapter sa = new FacebookStatusAdapter(FacebookFriendsStatusActivity.this, searchResult, true);
            friendList.setAdapter(sa);
        }
        else
        {
            goFistPage();
        }
    }
    View.OnClickListener seachListener = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            String key = keyEdit.getText().toString().trim();
            doSearch(key);
        }
    };
  	
	protected boolean goPrePage()
	{
		if(currentPage == 0)
			return false;
		
		currentPage = currentPage-1;
		setTitle(String.format("%1$s/%2$s", currentPage+1, pageCount));
		boolean ret = goPage(currentPage);
		
		System.gc();
		return ret;
	}
		
	protected boolean  goNextPage()
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
	
	private boolean goPage(int page)
	{
		boolean ret = false;
		if(statuses != null && statuses.size() > 0)
		{
			List<UserStatus> tempdata = new ArrayList<UserStatus>();
			for(int i=0;i<statuses.size();i++)
			{
				if(i>=page * pagesize && i<(page+1)*pagesize)
				{
			        tempdata.add(statuses.get(i));
				}
			}
			
			//refresh the UI
			FacebookStatusAdapter sa = new FacebookStatusAdapter(FacebookFriendsStatusActivity.this, tempdata, true);
    		friendList.setAdapter(sa);
			ret = true;
		}
		return ret;
	}
	
		
    @Override
	protected void createHandler() 
    {
		handler = new MainHandler();		
	}
    
    //try to get wall message
    private void lauchGeStatus()
    {
    	handler.obtainMessage(FACEBOOK_FRIENDS_STATUS_GET).sendToTarget();
    }
    
    //reget the wall
    @Override
    protected void loadRefresh()
    {
    	lauchGeStatus();
    }
    
    @Override
    protected void doAfterLogin()
    {
    	Log.d(TAG, "after login");
    	//try to get the session
    	perm_session = loginHelper.getPermanentSesstion(this);
    	if(perm_session == null)
    	{
    		//re-launch the login UI
    		launchFacebookLogin();
    	}
    	else
    	{
    		facebookA = new AsyncFacebook(perm_session);
    		perm_session.attachActivity(this);
    		lauchGeStatus();
    	}	
    }
    
    //context menu, post to wall
    @Override
    protected void postToWall(long uid)
    {
    	/*
    	Intent intent = new Intent(this, FacebookWallPostActivity.class);
    	intent.putExtra("posttouid", uid);
    	startActivity(intent);
    	*/
    	Intent wallintent = new Intent(this,FacebookStatusUpdateActivity.class);
		wallintent.putExtra("fuid", new Long(uid));
		startActivity(wallintent);
    }
        
    final static int FACEBOOK_FRIENDS_STATUS_GET    =0;
    final static int FACEBOOK_FRIEND_STATUS_UI      =1;
    final static int FACEBOOK_FRIENDS_STATUS_GET_END=2;
    final static int FACEBOOK_FRIENDS_STATUS_GET_DB =3;
    
    private class MainHandler extends Handler 
    {
        public MainHandler()
        {
            super();            
            Log.d(TAG, "new MainHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
            	case FACEBOOK_FRIENDS_STATUS_GET:                	
                {
                	getFriendsStatus();
                	break;
                }
                case FACEBOOK_FRIEND_STATUS_UI:
                {
                	using();
                	
                	if(statuses != null && statuses.size()>0)
	            	{
	            	    pageCount=statuses.size()/pagesize + 1;
	            	    if((pageCount-1)*pagesize == statuses.size())
	            	    {
	            	    	pageCount = pageCount -1;
	            	    }
	            	    currentPage=0;	                
		                goFistPage();	       
	            	}
                	
                	break;
                } 
                case FACEBOOK_FRIENDS_STATUS_GET_DB:
                {
                	statuses = orm.getFriendsRecentStatus();
                	handler.obtainMessage(FACEBOOK_FRIEND_STATUS_UI).sendToTarget();
                	
                	handler.obtainMessage(FACEBOOK_FRIENDS_STATUS_GET_END).sendToTarget();
                	break;
                }
                case FACEBOOK_FRIENDS_STATUS_GET_END:
                {
                	end();
                	
							
				    if(pageCount > 1)
				    {
				        setTitle(String.format("%1$s/%2$s", currentPage+1, pageCount));
				    }
                	break;
                }                
            }
        }
    }
    
   
    private void getFriendsStatus()
    {
    	begin();
		
    	Log.d(TAG, "before get getFriendsStatus");
    	//notifyLoading();  
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}
    	
    	facebookA.getFriendsStatusAsync(1, 1000,  new FacebookAdapter()
    	{
    		@Override public void getFriendsStatus(List<UserStatus> frds)
            {
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
				statuses = frds;
				Log.d(TAG, "after get my friends lastest status="+frds.size());
				//save to db
				orm.addFriendStatus(statuses);
                if(donotcallnetwork == false)//I am still alive
                {
					handler.obtainMessage(FACEBOOK_FRIEND_STATUS_UI).sendToTarget();
	            	//cancelNotify();
                }       
                handler.obtainMessage(FACEBOOK_FRIENDS_STATUS_GET_END).sendToTarget();
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
            	if(donotcallnetwork == false )//I am still alive
                {   
                     //cancelNotify();
                }   
            	if(isInAynscTaskAndStoped())
            	{
            		Log.d(TAG, "User stop passive");
            	}
            	else
            	{
	            	handler.obtainMessage(FACEBOOK_FRIENDS_STATUS_GET_END).sendToTarget();
	            	
	            	//get from Database
	            	handler.obtainMessage(FACEBOOK_FRIENDS_STATUS_GET_DB).sendToTarget();
            	}
            }
    	});
    	
    }
    
    private void notifyLoading() 
    {
    	notify.notifyOnce(R.string.facebook_status_loading, R.drawable.facebook_logo, 30*1000);		
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
    
    public void registerAccountListener() {
		AccountManager.registerAccountListener("FacebookFriendsStatusActivity", this);		
	}
	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("FacebookFriendsStatusActivity");		
	}
}
