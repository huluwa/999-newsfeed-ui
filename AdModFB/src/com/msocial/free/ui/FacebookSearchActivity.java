package com.msocial.free.ui;

import java.util.ArrayList;
import java.util.List;

import com.msocial.free.R;
import com.msocial.free.providers.SocialORM;
import com.msocial.free.ui.adapter.FacebookFriendAdapter;
import com.msocial.free.ui.view.FacebookFriendItemView;
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
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class FacebookSearchActivity extends FacebookBaseActivity{

    private ListView userList;
    private Button   search_friends_button;
    private Button   search_everyone_button;
    private boolean  forfriends=true;
    private List<FacebookUser.SimpleFBUser> searchResult = new ArrayList<FacebookUser.SimpleFBUser>();
    private List<FacebookUser.SimpleFBUser> friends      = new ArrayList<FacebookUser.SimpleFBUser>();
    private List<FacebookUser.SimpleFBUser> webfriends   = new ArrayList<FacebookUser.SimpleFBUser>();
    
    MyWatcher watcher;
    EditText  keyEdit;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitleMenuIconVisible(false);
        
        setContentView(R.layout.facebook_search_ui);
        
        userList = (ListView)this.findViewById(R.id.facebook_user_list);   
        userList.setFocusableInTouchMode(true);
        userList.setFocusable(true);
        userList.setClickable(true);        
        userList.setOnCreateContextMenuListener(this);
        userList.setOnItemClickListener(listItemClickListener);
        
        keyEdit = (EditText)this.findViewById(R.id.embedded_text_editor);
        watcher = new MyWatcher();         
        keyEdit.addTextChangedListener(watcher);
        
        search_friends_button = (Button)this.findViewById(R.id.search_friends_button);
        search_everyone_button = (Button)this.findViewById(R.id.search_everyone_button);
        
        search_everyone_button.setOnClickListener(everoneClick);
        search_friends_button.setOnClickListener(friendsClick);
        
        setClickButtoneStatus(forfriends);   
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
            perm_session = loginHelper.getPermanentSesstion(this);
            if(perm_session != null)
            {
                perm_session.attachActivity(this);
                
                facebookA = new AsyncFacebook(perm_session);                
            }
            else
            {
                launchFacebookLogin();
            }
        }        
    }
    
    View.OnClickListener everoneClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            forfriends = false;
            setClickButtoneStatus(forfriends);
            handler.obtainMessage(SEARCH_EVERYONE_GET).sendToTarget();
        }
    };
    
    View.OnClickListener friendsClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            forfriends = true;
            setClickButtoneStatus(forfriends);
            handler.obtainMessage(SEARCH_FRIENDS_GET).sendToTarget();
        }
    };
    
    private void setClickButtoneStatus(boolean forfr)
    {
        if(forfr == true)
        {
            search_friends_button.setClickable(false);
            search_friends_button.setEnabled(false);
            search_everyone_button.setEnabled(true);
            search_everyone_button.setClickable(true);
        }
        else
        {
            search_friends_button.setClickable(true);
            search_friends_button.setEnabled(true);
            search_everyone_button.setEnabled(false);
            search_everyone_button.setClickable(false);
        }
        userList.setAdapter(null);
    }
    
    AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
        {           
            if(FacebookFriendItemView.class.isInstance(v))
            {
                FacebookFriendItemView fv= (FacebookFriendItemView)v;
                Intent intent = new Intent(mContext, FacebookAccountActivity.class);
                intent.putExtra("uid",      fv.getUser().uid);
                intent.putExtra("username", fv.getUser().name);
                intent.putExtra("imageurl", fv.getUser().pic_square);
                if(forfriends)
                {
                    intent.putExtra("isfriend", true);
                }
                ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);                
            }              
        }
    };  
    
    
    @Override
    protected void createHandler() {
         handler = new SearchHandler();
    }

    final static int SEARCH_FRIENDS_GET  = 1;
    final static int SEARCH_EVERYONE_GET = 2;
    final static int SEARCH_UI           = 3;
    final static int SEARCH_END          = 4;
    
    private class SearchHandler extends Handler 
    {
        public SearchHandler()
        {
            super();            
            Log.d(TAG, "new SearchHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case SEARCH_FRIENDS_GET:
                {
                    String keyword = keyEdit.getText().toString().trim();
                    if(isEmpty(keyword) == false)
                    {
                        doSearch(keyword);
                    }
                    break;
                }
                case SEARCH_EVERYONE_GET:
                {
                    String keyword = keyEdit.getText().toString().trim();
                    if(isEmpty(keyword) == false)
                    {
                        webfriends.clear();
                        searchUserInUserTable(keyword);
                    }
                    break;
                }
                case SEARCH_UI:
                {
                    FacebookFriendAdapter sa = new FacebookFriendAdapter(FacebookSearchActivity.this, webfriends);
                    userList.setAdapter(sa);
                    break;
                }
                case SEARCH_END:
                {
                    end();
                    break;
                }
            }
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
    
    private void doSearch(String key)
    {
        if(this.forfriends)
        {
            searchResult.clear();       
            if(friends.size() == 0)
            {
                friends = orm.getAllFacebookSimpleUsers();
            }
            
            if(friends != null && key != null && key.length()>0)
            {
                for(int i=0;i<friends.size();i++)
                {
                    FacebookUser.SimpleFBUser user = friends.get(i);
                    if(user.name.toLowerCase().indexOf(key.toLowerCase())>=0)
                    {
                        searchResult.add(user);
                    }
                }
                //show UI
                //refresh the UI
                FacebookFriendAdapter sa = new FacebookFriendAdapter(FacebookSearchActivity.this, searchResult);
                userList.setAdapter(sa);
            }        
        }
        else
        {
            handler.obtainMessage(SEARCH_EVERYONE_GET).sendToTarget();
        }
    }
    
    public void searchUserInUserTable(String keyword) 
    {
        //stop pre-load        
        stopLoading();
        
        Log.d(TAG, "before get search ="+keyword);
        begin();
        synchronized(mLock)
        {
            inprocess = true;
        }
        
        facebookA.searchUsersAsync(false, keyword,  new FacebookAdapter()
        {
            @Override 
            public void getSimpleUsers(List<FacebookUser.SimpleFBUser> users)
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }    
                
                if(users != null)
                {
                    webfriends.addAll(users);
                    handler.obtainMessage(SEARCH_UI).sendToTarget();
                }
                Message msg = handler.obtainMessage(SEARCH_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
            }
            
            @Override public void onException(FacebookException e, int method, Object[]args) 
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
                    Message msg = handler.obtainMessage(SEARCH_END);
                    msg.getData().putBoolean(RESULT, false);
                    msg.sendToTarget();
                }
            }
        });
    }

    public void setTitle() 
    {
        title = getString(R.string.menu_twitter_title_search);
    }

    public void registerAccountListener() {
                
    }

    public void unregisterAccountListener() {
                
    }

}
