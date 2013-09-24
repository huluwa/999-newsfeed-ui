package com.msocial.facebook.ui;

import java.util.ArrayList;
import java.util.List;

import com.msocial.facebook.R;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.ui.AccountListener.AccountManager;
import com.msocial.facebook.ui.adapter.FacebookGroupAdapter;
import com.msocial.facebook.ui.adapter.GroupParcel;
import com.msocial.facebook.ui.view.FacebookFriendItemView;
import com.msocial.facebook.ui.view.FacebookGroupItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.Group;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class FacebookGroupActivity extends FacebookBaseActivity
{
    final String TAG="FacebookGroupActivity";
	private long     uid;
	private ListView groupList;
	private List<Group> friendGroups;
	private List<Long>  friendids;
	private boolean justshowhisgroups;
	private boolean fornotification;
	private long[] gids;
	private long hisuid;
	private int  currentPage=0, pagesize=30;
	
	 TextView eventinfoview;
	 View     infoSpan;
	     
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_group_ui);
        setTitle();
        setTitle(title);
        groupList = (ListView)this.findViewById(R.id.facebook_group_list);
        groupList.setFocusableInTouchMode(true);
        groupList.setFocusable(true);
        groupList.setOnCreateContextMenuListener(this);
        groupList.setOnItemClickListener(listItemClickListener);
        
        infoSpan      = (View)findViewById(R.id.facebook_info_span);
        eventinfoview = (TextView)findViewById(R.id.facebook_info);
        fornotification = this.getIntent().getBooleanExtra("fornotification", false);
        if(fornotification)
        {
           gids = this.getIntent().getLongArrayExtra("gids");
        }
        
        friendGroups = new ArrayList<Group>();
        justshowhisgroups = this.getIntent().getBooleanExtra("justshowhisgroups", false);
        hisuid            = this.getIntent().getLongExtra("hisuid", -1);
        
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);
	        	
	        	loadGroups();
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }        
    }
	
	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			 Log.d(TAG, "groupDetailClik you click first one=");
			 if(FacebookGroupItemView.class.isInstance(v))
			 {
				 FacebookGroupItemView view = (FacebookGroupItemView)v;
				 //view details
				 Intent intent = new Intent(mContext, FacebookGroupDetailsActivity.class);
				 GroupParcel groupp = new GroupParcel(view.getGroup());
				 intent.putExtra("group", groupp);	
				 intent.putExtra("fornotification", fornotification);
				 ((FacebookGroupActivity)mContext).startActivity(intent);
			 }
		}
	};	
	
	@Override
	protected void onNewIntent(Intent intent) {
		
		super.onNewIntent(intent);
		this.setIntent(intent);
		
		justshowhisgroups = intent.getBooleanExtra("justshowhisgroups", false);
        hisuid            = intent.getLongExtra("hisuid", -1);
        
        //TODO
        //need check login?
        loadGroups();
	}


	public void setTitle() 
	 {
	 	title = getString(R.string.facebook_groups_title);		
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
    		
    		loadFriends();
    	}
    }
	
	@Override
    protected void loadRefresh()
    {
        super.loadRefresh();
        
        loadGroups();
    }
	
	private void loadFriends()
	{		
		handler.obtainMessage(GET_FRIENDLIST).sendToTarget();
	}
	
	private void loadGroups()
	{		
		//handler.obtainMessage(GROUP_GET).sendToTarget();
		handler.obtainMessage(GROUP_GET_IN_ONETIME).sendToTarget();
		
	}
	
	@Override 
	protected void nextPage()
	{
		currentPage++;
		
		loadGroups();
	}
	
	@Override 
	protected void prePage()
	{
		currentPage--;
		if(currentPage<0)
			currentPage=0;
		
		loadGroups();
	}
	
	@Override 
	protected boolean hasMore()
	{
		if(friendGroups != null)
		    return friendGroups.size() == pagesize;
		else
			return false;
	}
	
	@Override 
	protected boolean isTheFirst()
	{
		return this.currentPage == 0;
	}
	
	@Override
	protected void createHandler() 
	{
		handler = new GroupHandler();		
	}	
	
	final static int GROUP_GET     =0;
	final static int GROUP_GET_END =1;
	final static int GROUP_UI      =2;
	final static int GET_FRIENDLIST=3;
	final static int GROUP_GET_IN_ONETIME=4;
	
	
	private class GroupHandler extends Handler 
    {
        public GroupHandler()
        {
            super();            
            Log.d(TAG, "new GroupHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case GET_FRIENDLIST:
	            {
	            	getMyFriends();
	            	break;
	            }
            	case GROUP_GET://update text
            	{	
            		if(friendids != null)
            		{
            		    for(int i=0;i<friendids.size();i++)
            		    {
            		    	getMyGroupd(friendids.get(i));
            		    }
            		}
            		
            		break;
            	}
            	case GROUP_GET_IN_ONETIME:
            	{
            		Log.d(TAG, "get groups in one time");
            		if(fornotification)
            		{
            		    if(gids!=null)
            		    {
            		        getGroupsBygids();
            		    }
            		}
            		else{
            		    if(justshowhisgroups)
                        {
                            getHisGroup(hisuid);
                        }
                        else
                        {
                            getMyGroupd(perm_session.getLogerInUserID());
                        }
            		}
            		
            		break;
            	}
            	case GROUP_UI://update text
            	{
            	    Log.d(TAG, "group UI");
            	    if(friendGroups == null || friendGroups.size() == 0)
            	    {
            	        infoSpan.setVisibility(View.VISIBLE);
            	        eventinfoview.setText(R.string.hint_no_group);
            	        Log.d(TAG, "set text");
            	    }
            	    else
            	    {
            	        infoSpan.setVisibility(View.GONE);
            	    }
            	    
            	    groupList.setOnItemClickListener(listItemClickListener);
            		FacebookGroupAdapter fa = new FacebookGroupAdapter(FacebookGroupActivity.this, friendGroups);
                	groupList.setAdapter(fa);
                	
                	handler.obtainMessage(GROUP_GET_END).sendToTarget();
            		break;            	
            	}            	
            	case GROUP_GET_END:
            	{
            		end();
            		if(friendGroups == null || friendGroups.size() == 0)
                    {
                         infoSpan.setVisibility(View.VISIBLE);
                         eventinfoview.setText(R.string.hint_no_group);
                    }
                    else
                    {
                        infoSpan.setVisibility(View.GONE);
                    }            		
            		break;
            	}
            }
        }
    }
	
	private void getMyFriends()
	{
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
	   
    	facebookA.getFriendUIDsAsync(perm_session.getLogerInUserID(), new FacebookAdapter()
    	{
    		@Override public void getFriendIDs(List<Long> ids)
            {
    			Log.d(TAG, "friend ids="+ids.size());
    			friendids = ids;
    			
    			loadGroups();
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	Log.d(TAG, "fail to get friends information");
            	if(isInAynscTaskAndStoped())
            	{
            		Log.d(TAG, "User stop passive");
            	}
            	else
            	{
	            	end();
	            	handler.post(new Runnable()
	            	{
						public void run() 
						{
							//setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.error);
						}
	            	});
            	}
            }
    	});
    	
	}
	
	//add new group into group list
	private void addGroups(List<Group> groups)
	{
		for(int i=0;i<groups.size();i++)
		{
			Group newg = groups.get(i);
			boolean exist=false;
			for(int j=0;j<friendGroups.size();j++)
			{
				Group oldg = friendGroups.get(j);
				if(newg.gid == oldg.gid)
				{
					exist = true;
				}
			}
			
			if(exist == false)
			{
				friendGroups.add(newg);
			}
		}		
	}
	
	private void getGroupsBygids()
	{
	    if(this.isInProcess() == true)
	    {
	        showToast();
	        return;
	    }
	    begin();
	    synchronized(mLock)
        {
            inprocess = true;
        }       
                
        facebookA.getGroupsAsync(gids,new FacebookAdapter()
        {
            @Override public void getGroups(List<Group> groups)
            {
                Log.d(TAG, "get groups="+groups.size());
                synchronized(mLock)
                {
                    inprocess = false;
                }
                friendGroups = groups;
                //addGroups(groups);
                
                if(donotcallnetwork == false)//I am still alive
                {                           
                    //cancelNotify();
                }       
                Message rmsg = handler.obtainMessage(GROUP_UI);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.sendToTarget();
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                Log.d(TAG, "after get groups ex="+e.getMessage());
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                    Message rmsg = handler.obtainMessage(GROUP_GET_END);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.sendToTarget();
                }
            }
        });     
	}
	
	//get someone's group
	private void getHisGroup(long hisid)
	{
	    if(this.isInProcess() == true)
	    {
	        showToast();
	        return;
	    }
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}    	
				
		facebookA.getMyGroupsAsync(hisid,currentPage*pagesize, pagesize,  new FacebookAdapter()
    	{
    		@Override public void getMyGroups(List<Group> groups)
            {
    			Log.d(TAG, "get groups="+groups.size());
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				friendGroups = groups;
				//addGroups(groups);
				
                if(donotcallnetwork == false)//I am still alive
                {							
	            	//cancelNotify();
                }       
                Message rmsg = handler.obtainMessage(GROUP_UI);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.sendToTarget();
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
            	
            	Log.d(TAG, "after get groups ex="+e.getMessage());
            	if(isInAynscTaskAndStoped())
            	{
            		Log.d(TAG, "User stop passive");
            	}
            	else
            	{
	            	Message rmsg = handler.obtainMessage(GROUP_GET_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.sendToTarget();
            	}
            }
    	});		
	}
	
	//get all my friends groups
	public void getMyGroupd(long uuid) 
	{
	    if(this.isInProcess() == true)
	    {
	        showToast();
	        return;
	    }
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}    	
				
		
	    facebookA.friendGroupsAsync(uuid, currentPage*pagesize, pagesize,  new FacebookAdapter()
    	{
    		@Override public void friendGroups(List<Group> groups)
            {
    			Log.d(TAG, "get groups="+groups.size());
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				if(friendGroups != null)
				{
				    groupList.setOnItemClickListener(null);
				    friendGroups.clear();
				}
				friendGroups = groups;
				//addGroups(groups);
				
                if(donotcallnetwork == false)//I am still alive
                {							
	            	//cancelNotify();
                }       
                Message rmsg = handler.obtainMessage(GROUP_UI);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.sendToTarget();
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
            	
            	Log.d(TAG, "after get groups ex="+e.getMessage());
            	if(isInAynscTaskAndStoped())
            	{
            		Log.d(TAG, "User stop passive");
            	}
            	else
            	{
	            	Message rmsg = handler.obtainMessage(GROUP_GET_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.sendToTarget();
            	}
            }
    	});		
	}
	
    public void registerAccountListener() {
		AccountManager.registerAccountListener("FacebookGroupActivity", this);		
	}
	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("FacebookGroupActivity");		
	}
}
