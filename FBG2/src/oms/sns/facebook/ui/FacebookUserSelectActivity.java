package oms.sns.facebook.ui;

import java.util.ArrayList;
import java.util.List;
import oms.sns.facebook.R;
import oms.sns.facebook.providers.SocialORM;
import oms.sns.facebook.ui.AccountListener.AccountManager;
import oms.sns.facebook.ui.adapter.FacebookSelectUserAdapter;
import oms.sns.facebook.ui.view.FacebookSelectUserItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class FacebookUserSelectActivity extends FacebookBaseActivity
{
    private final String TAG = "FacebookUserSelectActivity";
	private ListView friendList;
	private View searchSpan;
	private EditText keyEdit;
	private Button searchDo;
	private MyWatcher watcher;
	private List<FacebookUser> searchResult;
	private List<FacebookUser> friends;
	private List<Long>ids = new ArrayList<Long>();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
 	   super.onCreate(savedInstanceState);
 	   setContentView(R.layout.facebook_main_ui);
 	   
 	   friendList = (ListView)this.findViewById(R.id.facebook_wall_list);   
       friendList.setFocusableInTouchMode(true);
       friendList.setFocusable(true);
       
       searchSpan = this.findViewById(R.id.facebook_search_span);
       searchSpan.setVisibility(View.VISIBLE);
       keyEdit = (EditText)this.findViewById(R.id.embedded_text_editor);
       watcher = new MyWatcher();         
       keyEdit.addTextChangedListener(watcher);
       searchDo = (Button)this.findViewById(R.id.search_do);
       searchDo.setOnClickListener(seachListener);
       searchDo.setBackgroundResource(R.drawable.search);
       
       searchResult = new ArrayList<FacebookUser>();
       
       setTitle(R.string.facebook_user_select);
 	   
 	   SocialORM.Account account = orm.getFacebookAccount();
       if(checkFacebookAccount(this, account))
       {
       	   perm_session = loginHelper.getPermanentSesstion(this);
       	   if(perm_session!=null)
       	   {
               perm_session.attachActivity(this);
           	   facebookA = new AsyncFacebook(perm_session);
           	   
           	   loadUsers();
       	   }
       	   else
       	   {
       	     launchFacebookLogin();
       	   }
       }
    }
	public void setTitle()
	{
		title = this.getString(R.string.facebook_user_select);
	}
	
	private void loadUsers() 
	{		
		handler.obtainMessage(FACEBOOK_FRIENDS_GET).sendToTarget();
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
	public boolean onCreateOptionsMenu(Menu menu) 
    {
	     super.onCreateOptionsMenu(menu);
	     MenuInflater inflater = getMenuInflater();
	     inflater.inflate(R.menu.facebook_user_select_menu, menu);
	     
	     return true;
	}
   
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
            case R.id.facebook_menu_select_done:
            {
            	selectDone();
            	break;
            }
            case R.id.facebook_menu_selectall:
            {
            	doSelectAll(true);
            	break;
            }
	        case R.id.facebook_menu_unselectall:
	        {	
	        	doSelectAll(false);
	        	break;
	        }
        }
        return true;
    }
    
    private void selectDone()
    {
        getSelectedItems();
		
		Intent data = new Intent();
		if(ids.size()>0)
		{
			long[] uids = new long[ids.size()];
			for(int i=0;i<ids.size();i++)
			{
				uids[i] = ids.get(i);
			}
		    data.putExtra("uids", uids);
		}
		
		this.setResult(100, data);
		this.finish();
    }
    /*
    @Override
	public void titleSelected() 
	{		 
		super.titleSelected();
	    selectDone();
	}
	*/

    
    protected void getSelectedItems()
    {
    	ids.clear();
    	if(friends != null)
    	{
			for(int i=0;i<friends.size();i++)
	    	{
	    	    FacebookUser user = friends.get(i);	    	    
    	    	if(user.selected)
    	    	{
    	    		ids.add(user.uid);		        	    		
    	    	}	    	    
	    	}
    	}
    }
    
    private void doSelectAll(boolean sel) 
    {		
    	if(friends != null)
    	{
			for(int i=0;i<friends.size();i++)
	    	{
	    	    FacebookUser user = friends.get(i);	    	    
    	    	user.selected = sel; 	    
	    	}
    	}
    	friendList.requestLayout();
    	friendList.invalidate();
    	
    	//process for UI
    	for(int i=0;i<friendList.getChildCount();i++)    		 
        {
            View v = friendList.getChildAt(i);
            if(FacebookSelectUserItemView.class.isInstance(v))
            {
                ((FacebookSelectUserItemView)v).setCheckBoxSelected(sel);
            }
        }
	}

	private void doSearch(String key)
    {
        searchResult.clear();        
        if(friends != null && key != null && key.length()>0)
        {
            for(int i=0;i<friends.size();i++)
            {
                FacebookUser user = friends.get(i);
                if(user.name != null && user.name.toLowerCase().indexOf(key.toLowerCase())>=0)
                {
                    searchResult.add(user);
                }
            }
            //show UI
            //refresh the UI
            FacebookSelectUserAdapter sa = new FacebookSelectUserAdapter(FacebookUserSelectActivity.this, searchResult);
            friendList.setAdapter(sa);
        }
        else
        {
        	showUI();        	
        }
    }

	private void showUI() 
	{
		if(friends != null)
    	{
			 FacebookSelectUserAdapter sa = new FacebookSelectUserAdapter(FacebookUserSelectActivity.this, friends);
             friendList.setAdapter(sa);
    	}		
	}

	@Override
	protected void createHandler() 
	{		
		handler = new SelectHandler();
	}
	
	final int FACEBOOK_FRIENDS_GET = 0;
	final int FACEBOOK_FRIEND_UI   = 1;
	final int FACEBOOK_FRIENDS_GET_END = 2;
	
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
	            case FACEBOOK_FRIENDS_GET:                	
	            {
	            	getFriendsFromDB();
	            	break;
	            }
	            case FACEBOOK_FRIEND_UI:
	            {	
	            	showUI();	            	
	            	break;
	            }
	            case FACEBOOK_FRIENDS_GET_END:
	            {            
	            	end();
                	setTitle(R.string.facebook_user_select);
	            	break;
	            }               
            }
        }
    }
	
	private void notifyLoading() 
    {
    	notify.notifyOnce(R.string.facebook_friends_loading, R.drawable.facebook_logo, 30*1000);		
	}
    
	private void getFriendsFromDB()
	{
		friends = orm.getAllFacebookUsers();
		if(friends.size() <=0)
		{
			getFriends();
		}
		else//do UI
		{
			showUI();
		}
	}
	
	private void getFriends()
    {
		begin();
	    
    	Log.d(TAG, "before get friend");
    	//notifyLoading();  
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}
    	
    	facebookA.getMyFriendsAsync(perm_session.getLogerInUserID(),  true, new FacebookAdapter()
    	{
    		@Override public void getMyFriends(List<FacebookUser> frds)
            {
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
				friends = frds;
				
                if(donotcallnetwork == false)//I am still alive
                {
					handler.obtainMessage(FACEBOOK_FRIEND_UI).sendToTarget();
	            	//cancelNotify();
                }       
                handler.obtainMessage(FACEBOOK_FRIENDS_GET_END).sendToTarget();
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
            	    handler.obtainMessage(FACEBOOK_FRIENDS_GET_END).sendToTarget();
             	}
            }
    	});
    	
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
