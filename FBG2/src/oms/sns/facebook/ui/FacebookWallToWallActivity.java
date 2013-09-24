package oms.sns.facebook.ui;

import java.util.ArrayList;
import java.util.List;

import oms.sns.facebook.R;
import oms.sns.facebook.providers.SocialORM;
import oms.sns.facebook.ui.adapter.MessageAdapter;
import oms.sns.facebook.ui.view.MessageItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Wall;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FacebookWallToWallActivity extends FacebookBaseActivity{
   
    private final String TAG="FacebookMainActivity";    
    private ListView wallList;
    View facebook_compose_span;
    
    private List<Wall> walls = new ArrayList<Wall>();
    private Button sendButton;  
    private EditText sendcontent;   
    int currentPos=0, limit=20;
    int viewsize=50;
    
    View     facebook_info_span;
    TextView facebook_info;

    long uid1 = -1;
    long uid2 = -1;
    String uid1_name;
    String uid2_name;
    FacebookUser user ;
    ImageView imageView;
    TextView facebook_username;
    TextView facebook_status_text;
    TextView facebook_time;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_wall_ui);
        
        wallList = (ListView)this.findViewById(R.id.facebook_wall_list);   
        wallList.setFocusableInTouchMode(true);
        wallList.setFocusable(true);
        wallList.setSelected(true);
        wallList.setClickable(true);        
        wallList.setOnCreateContextMenuListener(this);
        wallList.setOnItemClickListener(listItemClickListener);
        
        facebook_info_span = (View)this.findViewById(R.id.facebook_info_span);
        facebook_info      = (TextView)this.findViewById(R.id.facebook_info);
        
        View v = findViewById(R.id.progress_horizontal);
        if(v != null)
        {
            progressHorizontal = (ProgressBar) v;
        }
        
        //FacebookMainActivity.this.setTitle(R.string.facebook_wall_title);
        
        facebook_compose_span = this.findViewById(R.id.facebook_compose_span);
        facebook_compose_span.setVisibility(View.GONE);
        
        sendButton = (Button)this.findViewById(R.id.wall_post);        
        sendcontent = (EditText) this.findViewById(R.id.facebook_wall_message_editor);
        sendcontent.setHint("Write on his(her) wall...");
        InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(140)};
        sendcontent.setFilters(filters);   
        sendcontent.setVerticalScrollBarEnabled(true);        
        sendButton.setText(R.string.facebook_main_send);
        sendButton.setOnClickListener(wallPostOnClik);
        sendButton.setVisibility(View.VISIBLE);
        
        View facebook_profile_span = this .findViewById(R.id.facebook_profile_span);
        facebook_profile_span.setVisibility(View.VISIBLE);
        
        imageView  = (ImageView)this.findViewById(R.id.facebook_img_ui);
        facebook_username    = (TextView)this.findViewById(R.id.facebook_username);
        facebook_status_text = (TextView)this.findViewById(R.id.facebook_status_text);
        facebook_time        = (TextView)this.findViewById(R.id.facebook_time);
        facebook_time.setVisibility(View.GONE);
        facebook_status_text.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        Intent intent = getIntent();
        uid1 = intent.getLongExtra("uid1", -1);
        uid2 = intent.getLongExtra("uid2", -1);
        uid1_name = intent.getStringExtra("uid1_name");
        uid2_name = intent.getStringExtra("uid2_name");
        
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
            perm_session = loginHelper.getPermanentSesstion(this);
            if(perm_session != null)
            {
                perm_session.attachActivity(this);
                
                facebookA = new AsyncFacebook(perm_session);   
                initUI();
                lauchGetWallToWallMessage();
            }
            else
            {
                launchFacebookLogin();
            }
        }
    }

    private void initUI()
    {
       
        String title = "";
        if(perm_session == null) return;
        
        if(uid1 == perm_session.getLogerInUserID())
        {
            title = "My wall-to-wall with "+uid2_name;
            facebook_compose_span.setVisibility(View.VISIBLE);
        }
        else if(uid2 == perm_session.getLogerInUserID())
        {
            title = "My wall-to-wall with "+uid1_name;
            facebook_compose_span.setVisibility(View.VISIBLE);
        }
        else
        {
            title = uid1_name + "'s wall-to-wall with "+uid2_name;
            facebook_compose_span.setVisibility(View.GONE);
            
        }
        facebook_username.setText(title);
    }
    
    private void lauchGetWallToWallMessage()
    {
        Message msd =  handler.obtainMessage(FACEBOOK_WALL_TO_WALL_GET);
         msd.sendToTarget();
    }
    
    AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
        {
            Log.d(TAG, "wall Item clicked");
            
            if(MessageItemView.class.isInstance(v))
            {
                MessageItemView fv= (MessageItemView)v;
                Intent intent = new Intent(mContext, FacebookAccountActivity.class);
                intent.putExtra("uid",      fv.getWall().fromid);
                intent.putExtra("username", fv.getWall().getFromusername());
                intent.putExtra("imageurl", fv.getImagePath());             
                ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
            }
        }
    };
    
    
    View.OnClickListener wallPostOnClik = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            handler.obtainMessage(FACEBOOK_WALL_POST).sendToTarget();
        }
    };
    
    
    @Override
    protected void createHandler() 
    {
        handler = new MainHandler();        
    }
    
    final static int FACEBOOK_WALL_TO_WALL_GET = 2005;
    final static int FACEBOOK_WALL_TO_WALL_UI = 2006;
    final static int FACEBOOK_WALL_TO_WALL_GET_END = 2007;
    
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
                case FACEBOOK_WALL_POST:
                {
                    postWallMessage();
                    break;
                }
                case FACEBOOK_WALL_TO_WALL_GET:
                {
                    if(isBackgroud() == false)
                    {
                        getWallToWallMessage(uid1,uid2,true);
                    }
                    else
                    {
                        Log.d(TAG, "I am in background, don't call me");
                    }
                    
                    break;
                }
                case FACEBOOK_WALL_TO_WALL_UI:
                {
                    boolean hasprogress = msg.getData().getBoolean("hasprogress");
                    if(hasprogress){
                       using();
                    }
                    
                    if(walls != null && walls.size() > 0)
                    {
                        MessageAdapter sa = new MessageAdapter(FacebookWallToWallActivity.this, walls);
                        wallList.setAdapter(sa);                     
                    }
                    else
                    {
                        showNoContentView();
                    }
                    break;
                }
                case FACEBOOK_WALL_TO_WALL_GET_END:
                {
                   //TODO  
                
                    end(); 
                    doNoContent();
                    break;
                }
                case FACEBOOK_WALL_POST_END:
                {   
                    end();
                    boolean result = msg.getData().getBoolean(RESULT);
                    if(result)
                    {
                        loadRefresh(false);
                        sendcontent.setText("");
                    }
                    break;
                }            
            }
        }
    }
    
    private void getWallToWallMessage(long uid1,long uid2,boolean hasprogress)
    {
       if(this.isInProcess() == true)
       {
           return;
       }
        if(existSession() == false)
        {
            return;
        }
        
        //just show 99 items
        if(currentPos >=viewsize)
        {
            return;
        }       
        
        if(hasprogress){
           begin();   
        }
        
        Log.d(TAG, "before get wall to wall message");
        //notifyLoading();  
        
        synchronized(mLock)
        {
            inprocess = true;
        }
        
        if(uid1 == -1)
        {
            uid1 = perm_session.getLogerInUserID();
        }
        
        facebookA.getWalltoWallMessageAsync(uid1,uid2,true, new FacebookAdapter()
        {
            @Override public void getWall(List<Wall> wallsfromweb,boolean hasprogress)
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                walls.clear();
                walls = wallsfromweb;
                
                if(donotcallnetwork == false)//I am still alive
                {   
                     Message msd = handler.obtainMessage(FACEBOOK_WALL_TO_WALL_UI);
                     msd.getData().putBoolean("hasprogress", hasprogress);
                     handler.sendMessage(msd);
                }       
                
                Message msd = handler.obtainMessage(FACEBOOK_WALL_TO_WALL_GET_END);
                msd.getData().putBoolean("hasprogress", hasprogress);
                handler.sendMessage(msd);
            }
            
            @Override public void onException(FacebookException e, int method, Object[] args) 
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
                     Message msd = handler.obtainMessage(FACEBOOK_WALL_TO_WALL_GET_END); 
                     msd.getData().putBoolean("hasprogress", (Boolean)args[2]);
                     handler.sendMessage(msd);
                }
            }
        });
        
    }
    
    private void postWallMessage()
    {
        if(facebookA != null)
        {
            String content= sendcontent.getText().toString().trim();
            if(content != null && content.length() > 0)
            {
                begin();
                
                synchronized(mLock)
                {
                    inprocess = true;
                }
                long wall_to_uid = uid2;
                if(uid2==perm_session.getLogerInUserID())
                {
                    wall_to_uid = uid1;
                }
               
                facebookA.postWallAsync(wall_to_uid, content, new FacebookAdapter()
                {
                    @Override public void postWall(boolean suc)
                    {
                        Log.d(TAG, "post to wall="+suc);
                        synchronized(mLock)
                        {
                            inprocess = false;
                        }
                        
                        if(donotcallnetwork == false)//I am still alive
                        {                           
                            //cancelNotify();
                        }       
                        Message rmsg = handler.obtainMessage(FACEBOOK_WALL_POST_END);
                        rmsg.getData().putBoolean(RESULT, suc);
                        rmsg.sendToTarget();
                    }
                    
                    @Override public void onException(FacebookException e, int method) 
                    {
                        synchronized(mLock)
                        {
                            inprocess = false;
                        }
                        
                        Log.d(TAG, "post to wall ex="+e.getMessage());
                        if(isInAynscTaskAndStoped())
                        {
                            Log.d(TAG, "User stop passive");
                        }
                        else
                        {
                            Message rmsg = handler.obtainMessage(FACEBOOK_WALL_POST_END);
                            rmsg.getData().putBoolean(RESULT, false);
                            rmsg.sendToTarget();
                        }
                    }
                });
            }
        }
    }    
       
    private void doNoContent()
    {
        if(walls.size() == 0)
        {
            facebook_info_span.setVisibility(View.VISIBLE);
            facebook_info.setText(R.string.no_wall_hint);
        }
        else
        {
            facebook_info_span.setVisibility(View.GONE);
        }
    }
    
    protected void loadRefresh()
    {
        loadRefresh(true);
    }
    
    protected void loadRefresh(boolean hasprogress)
    {
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
            perm_session = loginHelper.getPermanentSesstion(this);
            if(perm_session != null)
            {
                perm_session.attachActivity(this);
                if(facebookA == null)
                {
                    facebookA = new AsyncFacebook(perm_session);
                }
                
                lauchGetWallToWallMessage();
            }
            else
            {
                launchFacebookLogin();
            }
        }
        
    }
    
    @Override
    public void onLogin() 
    {   
        super.onLogin();
        afterlogin = true;
    }
    
    @Override
    public void onLogout() 
    {   
        super.onLogout();
        
        //remove UI
        handler.post(new Runnable(){
            public void run(){
                currentPos = 0; 
                wallList.setAdapter(null);
                if(walls != null && walls.size() > 0)
                {
                    walls.clear();
                    doNoContent();
                }
            }
        });
       
    }

    public void setTitle() {
       title = getString(R.string.menu_facebook_title_wall_to_wall);
    }

    public void registerAccountListener() {
        AccountManager.registerAccountListener("FacebookMainActivity", this);       
    }
    public void unregisterAccountListener() {
        AccountManager.unregisterAccountListener("FacebookMainActivity");       
    }
  
}
