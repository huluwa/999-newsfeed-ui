package com.msocial.freefb.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.ui.FacebookBaseActivity;
import com.msocial.freefb.ui.adapter.FacebookFriendAdapter;
import com.msocial.freefb.ui.adapter.FacebookFriendCursorAdapter;
import com.msocial.freefb.ui.adapter.FacebookPokeCursorAdapter;
import com.msocial.freefb.ui.view.FacebookFriendItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.PokeResponse;
import oms.sns.service.facebook.util.ArrayUtils;

public class FacebookPokeActivity extends FacebookBaseActivity{

    private final String TAG="FacebookPokeActivity";    
    private ListView friendList;    

    private List<PokeResponse> pokes = new ArrayList<PokeResponse>();
    private List<Long> uids = new ArrayList<Long>();
    private Cursor fCursor;
    private int currentPos = -1;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Intent intent = this.getIntent();
       
        setContentView(R.layout.facebook_main_ui);
        friendList = (ListView)this.findViewById(R.id.facebook_wall_list);   
        friendList.setFocusableInTouchMode(true);
        friendList.setFocusable(true);
        friendList.setOnCreateContextMenuListener(this);
        friendList.setOnItemClickListener(listItemClickListener);
    
        setTitle(R.string.menu_title_pokes);
        SocialORM.Account account = orm.getFacebookAccount();
       
        if(checkFacebookAccount(this, account))
        {
            perm_session = loginHelper.getPermanentSesstion(this);
            if(perm_session != null)
            {
                perm_session.attachActivity(this);
                facebookA = new AsyncFacebook(perm_session);
                launchGetPokes();
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
            Log.d(TAG, "facebook friend Item clicked");
            if(FacebookFriendItemView.class.isInstance(v))
            {
                FacebookFriendItemView fv= (FacebookFriendItemView)v;
                Intent intent = new Intent(mContext, FacebookAccountActivity.class);
                intent.putExtra("uid",      fv.getUser().uid);
                intent.putExtra("username", fv.getUser().name);
                intent.putExtra("imageurl", fv.getUser().pic_square);                   
                ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
                //((FacebookBaseActivity)(mContext)).startActivity(intent);
            }
        }
         
    };  
    
    @Override
    protected void loadRefresh() {
        super.loadRefresh();
        
        launchGetPokes();
    }
    private void getPokes()
    {
        if(inprocess == true)
        {
            showToast();
            return ;
        }
        
        if(hasSession() == false)
        {
            Log.d(TAG, "session is null, to end");
            handler.obtainMessage(FACEBOOK_POKES_END).sendToTarget();
            return ;
        }
        
        begin();
        
        Log.d(TAG, "before getPokes");
        //notifyLoading();  
        synchronized(mLock)
        {
            inprocess = true;
        }
        
      
        facebookA.getPokesAsync( true, new FacebookAdapter()
        {
            @Override public void getPokes(List<PokeResponse> responsepoke)
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                Log.d(TAG," get Pokes "+responsepoke.size());
                pokes = responsepoke;
                
                if(donotcallnetwork == false)//I am still alive
                {
                    handler.obtainMessage(FACEBOOK_USERINFO_GET).sendToTarget();
                    //cancelNotify();
                }
                else
                {
                    handler.obtainMessage(FACEBOOK_POKES_END).sendToTarget();
                }
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                Log.d(TAG," get Poke exception "+e.getMessage());
                synchronized(mLock)
                {
                    inprocess = false;
                }
                handler.obtainMessage(FACEBOOK_POKES_END).sendToTarget();
            }
        });
        
    }
    
   public void completeUserInfo(List<Long> uids){
       Log.d(TAG," enter into completeUserInfo "+uids.size());
       if(uids!=null && uids.size()>0)
       {
            long[] temp_uids = new long[uids.size()];
            for(int i=0; i<temp_uids.length; i++)
            {
                temp_uids[i] = uids.get(i);
            }
            facebookA.getSimpleUsersAsync(temp_uids, new FacebookAdapter()
            {
                @Override public void getSimpleUsers(List<FacebookUser.SimpleFBUser> fusers)
                {
                    Log.d(TAG, "get basic info suc="+fusers.size());
                    orm.addFacebookSimpleUser(fusers);
                    handler.obtainMessage(FACEBOOK_POKES_UI).sendToTarget();                    
                    handler.obtainMessage(FACEBOOK_USERINFO_GET_END).sendToTarget();
                }
                
                @Override public void onException(FacebookException e, int method) 
                {
                    Log.d(TAG, "get basic info fail="+e.getMessage());
                    
                    handler.obtainMessage(FACEBOOK_POKES_UI).sendToTarget();
                    handler.obtainMessage(FACEBOOK_USERINFO_GET_END).sendToTarget();
                }
            });
        }
        else
        {
            handler.obtainMessage(FACEBOOK_POKES_UI).sendToTarget();
        }       
    }
   
   boolean isPoking = false;
    @Override
    public void doPoke(final long uid,String username) 
    {
        Log.d(TAG," poke uid is "+uid);
        if(isPoking == true)
        {
            return;
        } 
        begin();
        synchronized(mLock)
        {
           isPoking = true;
        }           
        if(facebookA != null)
        {
            facebookA.pokeAsync(uid,  new FacebookAdapter()
            {
                @Override public void poke(boolean suc)
                {
                    synchronized(mLock)
                    {
                       isPoking = false;
                    }           
                    Log.d(TAG," poke successfully ");   
                    Message msg = handler.obtainMessage(FACEBOOK_POKES_END);
                    msg.getData().putBoolean(RESULT, suc);
                    msg.getData().putLong("uid",uid);
                    msg.getData().putBoolean("ispoking", true);
                    msg.sendToTarget();
                }
                
                @Override public void onException(FacebookException e, int method) 
                {
                    synchronized(mLock)
                    {
                       isPoking = false;
                    }           
                    Log.d(TAG, "fail to poke="+e.getMessage());
                    Message msg = handler.obtainMessage(FACEBOOK_POKES_END);
                    msg.getData().putBoolean("ispoking", true);
                    msg.getData().putBoolean(RESULT, false);
                    msg.sendToTarget();
                }
            });
        }
    }
    
    final static int FACEBOOK_POKES_GET     = 0;
    final static int FACEBOOK_POKES_UI      = 1;
    final static int FACEBOOK_POKES_END     = 2;
    final static int FACEBOOK_USERINFO_GET  =  3;
    final static int FACEBOOK_USERINFO_GET_END =  4;
    
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
                case FACEBOOK_POKES_GET:                  
                {
                    getPokes();
                    break;
                }
                case FACEBOOK_USERINFO_GET:
                {
                    boolean allexist = true;
                    List<Long> needCompleteUI = new ArrayList<Long>();
                    for(int i = 0 ; i < pokes.size();i++)
                    {
                        PokeResponse pr = pokes.get(i);                       
                        FacebookUser.SimpleFBUser user = orm.getSimpleFacebookUser(pr.uid);
                        if(user==null)
                        {
                            allexist = false;   
                            needCompleteUI.add(pr.uid);
                        } 
                        uids.add(pr.uid);
                    }
                    
                    if(allexist == false)
                    {
                        completeUserInfo(needCompleteUI);
                    }
                    else//show UI directly
                    {
                        this.obtainMessage(FACEBOOK_POKES_UI).sendToTarget();
                        this.obtainMessage(FACEBOOK_USERINFO_GET_END).sendToTarget();                        
                    }
                    break;
                }
                case FACEBOOK_POKES_END:
                {
                    end(); 
                    if(msg.getData().getBoolean(RESULT) == true)
                    {
                        long uid = msg.getData().getLong("uid");
                        if(uid > 0)
                        {
                            removeUID(uid);
                            reShowUI();
                        }          
                    }
                    break;
                }
                case FACEBOOK_USERINFO_GET_END:
                {
                    end();
                    break;
                }
                case FACEBOOK_POKES_UI:
                {
                    friendList.setAdapter(null);  
                    if(fCursor != null)
                    {
                        fCursor.requery();
                    }
                    else
                    {
                        long[] uidarray = new long[uids.size()];
                        for(int i=0;i<uids.size();i++)
                        {
                            uidarray[i] = uids.get(i);
                        }
                        fCursor = orm.getFacebookUserCursorByUids(ArrayUtils.join(uidarray));
                    }
                    FacebookPokeCursorAdapter adapter = new FacebookPokeCursorAdapter(FacebookPokeActivity.this, fCursor);
                    friendList.setAdapter(adapter);
                    break;
                } 
            }
        }
    }
    
    private void launchGetPokes()
    {
        handler.obtainMessage(FACEBOOK_POKES_GET).sendToTarget();
    }
    
    
    
    public void reShowUI() {
        if(fCursor!=null)
        {
            friendList.setAdapter(null);
            fCursor.close();
            fCursor = null;    
        }
        
        handler.obtainMessage(FACEBOOK_POKES_UI).sendToTarget();
    }
    public void removeUID(long uid) {
        for(int i=0;i<uids.size();i++)
        {
            if(uids.get(i) == uid)
            {
                uids.remove(i);
            }
        }
        
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(fCursor !=null)
        {
            fCursor.close();
            fCursor = null;
        }
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
             AlertDialog dialog = new AlertDialog.Builder(this)
             .setTitle(R.string.facebook_login_fail)
             .setMessage(R.string.facebook_login_retry)
             .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() 
             {
                public void onClick(DialogInterface dialog, int whichButton) 
                {
                    launchFacebookLogin();                  
                }
             })
             .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() 
             {
                public void onClick(DialogInterface dialog, int whichButton) 
                {
                    
                }
             })
             .create();
             dialog.show();
        }
        else
        {
            facebookA = new AsyncFacebook(perm_session);
            perm_session.attachActivity(this);
            launchGetPokes();
        }
    }
    
    
    public void setTitle() 
    {
        title = getString(R.string.menu_title_pokes);     
    }
    @Override
    protected void createHandler() 
    {
        handler = new MainHandler();        
    }
    
    private void notifyLoading() 
    {
        
       notify.notifyOnce(R.string.facebook_pokes_loading, R.drawable.facebook_logo, 30*1000);

    }
    

    public void registerAccountListener() {
        AccountManager.registerAccountListener("FacebookFriendsActivity", this);        
    }
    public void unregisterAccountListener() {
        AccountManager.unregisterAccountListener("FacebookFriendsActivity");        
    }

}
