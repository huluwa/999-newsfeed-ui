package com.msocial.freefb.ui;

import com.msocial.freefb.R;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.service.SNSService;
import com.msocial.freefb.ui.AccountListener.AccountManager;
import com.msocial.freefb.ui.adapter.FacebookStreamAdapter;
import com.msocial.freefb.ui.adapter.MessageAdapter;
import com.msocial.freefb.ui.view.FacebookStreamItemView;
import com.msocial.freefb.ui.view.ImageRun;
import com.msocial.freefb.ui.view.MessageItemView;
import com.msocial.freefb.util.DateUtil;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Stream;
import oms.sns.service.facebook.model.Wall;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Window;

public class FacebookMainActivity extends FacebookBaseActivity
{
    private final String TAG="FacebookMainActivity";    
	private ListView wallList;
	View facebook_compose_span;
	private List<Stream> streams = new ArrayList<Stream>();
	private Button sendButton;	
	private EditText sendcontent;	
	int limit=20;
	int viewsize=50;
	long starttime;
	
	View     facebook_info_span;
	TextView facebook_info;
		
	public long inputuid=-1;
	FacebookUser user ;
	ImageView imageView;
    TextView facebook_username;
    TextView facebook_status_text;
    TextView facebook_time;
    View     facebook_msg_bottom_span;
    Button   facebook_msg_bottom_region;
    
    private int lastVisiblePos  = 0;
    
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
		
        FacebookMainActivity.this.setTitle(R.string.facebook_wall_title);
        
        facebook_compose_span = this.findViewById(R.id.facebook_compose_span);
        facebook_compose_span.setVisibility(View.VISIBLE);
        
        sendButton = (Button)this.findViewById(R.id.wall_post);        
        sendcontent = (EditText) this.findViewById(R.id.facebook_wall_message_editor);
        sendcontent.setHint("Write on my wall...");
        InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(140)};
        sendcontent.setFilters(filters);   
        sendcontent.setVerticalScrollBarEnabled(true);      
        sendButton.setText(R.string.facebook_main_send);
        sendButton.setOnClickListener(wallPostOnClik);
        if(isFromTabView() )
        {
        	sendButton.setVisibility(View.VISIBLE);
        }
        
        View facebook_profile_span = this .findViewById(R.id.facebook_profile_span);
		facebook_profile_span.setVisibility(View.VISIBLE);
		
    	imageView  = (ImageView)this.findViewById(R.id.facebook_img_ui);
      	facebook_username    = (TextView)this.findViewById(R.id.facebook_username);
      	facebook_status_text = (TextView)this.findViewById(R.id.facebook_status_text);
      	facebook_time        = (TextView)this.findViewById(R.id.facebook_time);
        
      	/*facebook_msg_bottom_span = (View)this.findViewById(R.id.facebook_msg_bottom_span);
      	facebook_msg_bottom_span.setVisibility(View.VISIBLE);
      	
      	facebook_msg_bottom_region = (Button)this.findViewById(R.id.facebook_msg_bottom_region);
      	facebook_msg_bottom_region.setText(R.string.load_older_msg);
      	facebook_msg_bottom_region.setOnClickListener(loadOlderClick);
      	facebook_msg_bottom_region.requestFocus();*/
      	
        //start com.msocial.freefb service
        Intent in = new Intent(this.getApplicationContext(), SNSService.class);            
        startService(in);
        
        IntentFilter filter = new IntentFilter("com.msocial.freefb.getsession");
        registerReceiver(mHangReceiver, filter);
        
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);
	        	inputuid = this.getIntent().getLongExtra("uid", perm_session.getLogerInUserID());
	        	if(isMyself() == false)
	        	{
	        		sendcontent.setHint("Write on his(her) wall...");
	        	}
	        	lauchGetWallMessage();
	        	setMenu(true);
        	}
        	else
        	{
        		setMenu(false);
        		launchFacebookLogin();
        	}
        }
    }
    
    @Override
	protected void removeLikeAction(final String pid, boolean suc)
	{
		if(suc)
		{
			for(int i=0;i<streams.size();i++)
			{
				if(streams.get(i).post_id.equals(pid))
				{
					streams.get(i).likes.user_likes--;
					streams.get(i).likes.count--;
					handler.post( new Runnable()
					{
						public void run()
						{
					        //process for UI
					    	for(int j=0;j<wallList.getChildCount();j++)    		 
					        {
					            View v = wallList.getChildAt(j);
					            if(FacebookStreamItemView.class.isInstance(v))
					            {
					            	FacebookStreamItemView fv = (FacebookStreamItemView)v;
					            	if(fv.getStream().post_id.equals(pid))
					            	{
						            	fv.refreshUI();
						            	break;
					            	}					            	
					            }
					        }
						}
					});
					break;
				}
			}
		}
	}
	
	@Override
	protected void addLikeAction(final String pid, boolean suc)
	{
		if(suc)
		{
			for(int i=0;i<streams.size();i++)
			{
				if(streams.get(i).post_id.equals(pid))
				{
					streams.get(i).likes.user_likes++;
					streams.get(i).likes.count++;
					handler.post( new Runnable()
					{
						public void run()
						{
					        //process for UI
					    	for(int j=0;j<wallList.getChildCount();j++)    		 
					        {
					            View v = wallList.getChildAt(j);
					            if(FacebookStreamItemView.class.isInstance(v))
					            {
					            	FacebookStreamItemView fv = (FacebookStreamItemView)v;
					            	if(fv.getStream().post_id.equals(pid))
					            	{
						            	fv.refreshUI();
						            	break;
					            	}					            	
					            }
					        }
						}
					});
					break;
				}
			}
		}
	}
	
    public boolean isMyself()
    {
    	boolean ret = true;
    	if(perm_session != null)
    	{
    	    if(inputuid != perm_session.getLogerInUserID())
    	    {
    	    	ret = false;
    	    }
    	}
    	return ret;
    }
    @Override
	protected void onNewIntent(Intent intent) 
	{		
		super.onNewIntent(intent);
		
		Log.d(TAG, "onNewIntent"+intent);
		setIntent(intent);
		
		//come from another new
		if(perm_session != null)
		{
		    long tmp = this.getIntent().getLongExtra("uid", perm_session.getLogerInUserID());
		    if(tmp != inputuid)
		    {
		    	inputuid = tmp;
		    	if(this.isMyself() == false)
		    	{
		    	    sendcontent.setHint("Write on his(her) wall...");
		    	}
		    	else
		    	{
		    		sendcontent.setHint("Write on my wall...");
		    	}
		    	
		    	//remove pre data and reset UI
		    	starttime = 0;		    	
            	wallList.setAdapter(null);
            	streams.clear();
            	
    	        lauchGetWallMessage();
		    }
		}
	}

    
    private void doNoContent()
    {
    	if(streams.size() == 0)
    	{
    		facebook_info_span.setVisibility(View.VISIBLE);
    		facebook_info.setText(R.string.no_wall_hint);
    	}
    	else
    	{
    		facebook_info_span.setVisibility(View.GONE);
    	}
    }
    
    @Override protected void onPause() {
        super.onPause();
    
        //this.unregisterReceiver(mHangReceiver);
    }
    
    @Override protected void onDestroy() {
        super.onDestroy();
    
        this.unregisterReceiver(mHangReceiver);
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
                starttime = 0; 
                wallList.setAdapter(null);
                if(streams != null && streams.size() > 0)
                {
                	streams.clear();
                    doNoContent();
                }
            }
        });
       
    }
    
    @Override protected void onResume() 
    {
        super.onResume();
        Log.d(TAG, "onResume");
        
        if(afterlogin == true)
        {
        	lauchGetWallMessage();
        	afterlogin = false;
        }
        
        if(loginHelper.getPermanentSesstion(this) != null)        
        {
        	setMenu(true);        
        }
        else
        {
        	setMenu(false);
        }
        //IntentFilter filter = new IntentFilter("com.msocial.freefb.getsession");
        //registerReceiver(mHangReceiver, filter);
    }    
    
    
   public void setTitle() 
    {
    	title = this.getString(R.string.facebook_wall_title);
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
    
    
	
	View.OnClickListener loadOlderClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			Log.d(TAG, "load older message");
			loadOlderPost();
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
    
    
    //try to get wall message
    private void lauchGetWallMessage()
    {   
    	//show profile logo and name   	  
    	getUserProfile();
    	
    	lauchGetWallMessage(true);
    }
    
    void getUserProfile()
    {	      	
    	imageView.setImageResource(R.drawable.noimage);
    	facebook_status_text.setText("");
    	facebook_status_text.setText("");
    	facebook_time.setText("");
      	user = orm.getFacebookUser(inputuid);
      	if(user != null)
      	{
      		facebook_username.setText(user.name);
      		if(isEmpty(user.message) == false)
      		{
      			facebook_status_text.setText(user.message);
      			facebook_status_text.setVisibility(View.VISIBLE);
      			
      			facebook_time.setVisibility(View.VISIBLE);
      			facebook_time.setText(DateUtil.converToRelativeTime(FacebookMainActivity.this, user.getStatustime()));
      		}
      		else
      		{
      			facebook_status_text.setVisibility(View.GONE);
      			facebook_time.setVisibility(View.GONE);
      		}
      		
      		String url = user.pic_square;
      		if(url == null)
      		{
      			imageView.setImageResource(R.drawable.noimage);
      		}            		
      		else
      		{            			
          		ImageRun imagerun = new ImageRun(handler, url, 0);		
          		imagerun.use_avatar = true;
          		imagerun.setImageView(imageView);
          		imagerun.post(imagerun);
      		}
      	}
      	else
      	{
      		long[] uids = new long[1];
  			uids[0] = inputuid;
  			facebookA.getBasicUsersAsync(uids, new FacebookAdapter()
  	    	{
  	    		@Override public void getUsers(List<FacebookUser> users)
  	            {
  	    			if(users != null && users.size()>0)
  	    			{
  	    				user = users.get(0);
  	    				Log.d(TAG, "after get user info="+user);
  	    									    
  	    				handler.post( new Runnable()
  						{
  							public void run()
  							{								
  		    				    ImageRun imagerun = new ImageRun(handler, user.pic_square, 0);	
  		    				    imagerun.use_avatar = true;
  		    					imagerun.setImageView(imageView);
  		    					imagerun.post(imagerun);		    			    
  							}
  						});
  						
  						//update database
  						orm.addFacebookUser(user);
  						handler.post( new Runnable()
  						{
  							public void run()
  							{
  								facebook_username.setText(user.name);
  								if(isEmpty(user.message) == false)
  					      		{
  								    facebook_status_text.setVisibility(View.VISIBLE);
                                    facebook_time.setVisibility(View.VISIBLE);
  					      			facebook_status_text.setText(user.message);
  					      			facebook_time.setText(DateUtil.converToRelativeTime(FacebookMainActivity.this, user.getStatustime()));
  					      		}
  					      		else
  					      		{
  					      			facebook_status_text.setVisibility(View.GONE);
  					      		    facebook_time.setVisibility(View.GONE);
  					      		}	  								
  							}							
  						});
  						
  	    			}
  	            }
  	    		
  	            @Override public void onException(FacebookException e, int method) 
  	            {
  	            	Log.d(TAG, "fail to get the image");	            	 	
  	            }
  	    	});
      	}
    	
    }
    
    private void lauchGetWallMessage(boolean hasProgress)
    {
    	Message msg = handler.obtainMessage(FACEBOOK_WALL_GET);
    	msg.getData().putBoolean("hasprogress",hasProgress);
    	msg.sendToTarget();
    }
    
    //reget the wall
    @Override
    protected void loadRefresh()
    {
    	lastVisiblePos = wallList.getFirstVisiblePosition();	
    	
    	loadRefresh(true);
    }
    

    protected void loadRefresh(boolean hasprogress)
    {
    	starttime = 0;
    	//if enter idle, then come back, will call on resume, at this time,
    	//if user is not login, how to process, give a chance for refresh
    	//
    	//check the account
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
	        	
	        	lauchGetWallMessage(hasprogress);
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
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
    		Log.d(TAG, "fail to get permanent session");
    		Toast.makeText(this, R.string.facebook_no_valid_session, Toast.LENGTH_SHORT).show();
    		setMenu(false); 		
    		//reLaunchFacebookLogin();
    	}
    	else
    	{
    		setMenu(true);
    		
    		facebookA = new AsyncFacebook(perm_session);
    		perm_session.attachActivity(this);
    		
    		inputuid = this.getIntent().getLongExtra("uid", perm_session.getLogerInUserID());
    		lauchGetWallMessage();
    	}   
    }
    
    private void setMenu(boolean logined)
    {
    	if(logined == false)
    	{
	    	wallList.setOnCreateContextMenuListener(null);
	    	wallList.setOnItemClickListener(null);
	    	if(optionMenu != null)
	    	{
				optionMenu.setGroupVisible(R.id.facebook_groupd_nemu, false);				
				optionMenu.setGroupVisible(R.id.facebook_groupd_login_nemu, true);
				
				optionMenu.findItem(R.id.facebook_menu_login).setVisible(true);
				optionMenu.findItem(R.id.facebook_menu_settings).setVisible(true);
	    	}	    	
		}
		else
		{
			wallList.setOnCreateContextMenuListener(this);
			wallList.setOnItemClickListener(listItemClickListener);
			if(optionMenu != null)
			{
				optionMenu.setGroupVisible(R.id.facebook_groupd_nemu, true);
				optionMenu.setGroupVisible(R.id.facebook_groupd_login_nemu, false);
				optionMenu.findItem(R.id.facebook_menu_login).setVisible(false);
			}			
	    }
    }
    @Override
	public void titleSelected() 
    {		
		super.titleSelected();
		//TODO send message to my wall
		handler.obtainMessage(FACEBOOK_WALL_POST).sendToTarget();
	}
    
    //context menu, post to wall
    @Override
    public void postToWall(long uid)
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
    
    @Override
    protected void toWallToWallMessage(long uid_from,String fromusername)
    {
        //Message msd =  handler.obtainMessage(FACEBOOK_WALL_TO_WALL_GET);
       // msd.getData().putLong("fromuid",uid_from);
       // msd.sendToTarget();
        Intent walltowallintent = new Intent(this,FacebookWallToWallActivity.class);
        walltowallintent.putExtra("uid1",inputuid);
        walltowallintent.putExtra("uid2", uid_from);
        if(user!=null && !isEmpty(user.name)) walltowallintent.putExtra("uid1_name",user.name);
        walltowallintent.putExtra("uid2_name", fromusername);
        startActivity(walltowallintent);
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
                case FACEBOOK_WALL_GET:                	
                {
                	if(isBackgroud() == false)
                	{
                		boolean hasprogress = msg.getData().getBoolean("hasprogress");
                		Log.d(TAG, " hasProgress is "+ hasprogress);
                	    getWallMessage(starttime, true);
                	}
                	else
                	{
                		Log.d(TAG, "I am in background, don't call me");
                	}
                	break;
                }
                case FACEBOOK_WALL_UI:
                {
                	FacebookStreamAdapter af = new FacebookStreamAdapter(FacebookMainActivity.this, streams,true);
                	int count = wallList.getFooterViewsCount();
            		if(count<=0)
            		{
            			Button but = new Button(FacebookMainActivity.this);
            			but.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button_background));
                		but.setText(getString(R.string.load_older_msg));
                		but.setOnClickListener(loadOlderClick);
                		wallList.addFooterView(but);
            		}
            		wallList.setAdapter(af);
            		
            		wallList.setSelection(lastVisiblePos);
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
                case FACEBOOK_WALL_GET_END:
                {
                    /*boolean hasprogress = msg.getData().getBoolean("hasprogress");
                	if(hasprogress){
                		end();
                	}
                	int count = msg.getData().getInt("count", -1);
                	if(count == limit)
                	{
                		lauchGetWallMessage(false);//continue to load more
                	}
                	
                	doNoContent();*/
                	
                	end();
                	doNoContent();
                	/*
                	 * no need for this
                	Message mds = handler.obtainMessage(FACEBOOK_WALL_GET);
                	handler.sendMessageDelayed(mds, orm.getFacebookStreamTimeout()*1000);
                	*/
                	break;
                }
               
            }
        }
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
		    	
			    facebookA.postWallAsync(inputuid, content, new FacebookAdapter()
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
    
	private void getLastViewCount(final List<Stream> sts)
	{
		synchronized(streams)
    	{
	    	for(int i=0;i<sts.size();i++)
			{
	    		Stream item = sts.get(i);
				boolean isExist = false;
				for(int j=0;j<streams.size();j++)
				{
					Stream exist = streams.get(j);
					if(item.post_id.equalsIgnoreCase(exist.post_id))
					{
						isExist=true;
						//update the content						
						exist.dispose();
						exist=null;
						
						streams.set(j, item);
						break;
					}
				}
				
				if(isExist == false)
				{
					streams.add(item);				
				}
		    }	    	
	    	java.util.Collections.sort(streams);	    	
	    	//remove the no use wall, tmp we don't need, but this will cause memory issue   
	    	/*
	    	while(streams.size() > viewsize)
	    	{
	    		streams.remove(streams.size() -1);
	    	}
	    	*/
	    	
	    	 if(streams.size()>0)
	         {
	             starttime = streams.get(0).updated_time+1000;
	         }	
    	}
	}
	
	@Override
    protected void loadOlderPost()
    {
		if(streams != null && streams.size() > 0)
		{
			long lasttime = streams.get(streams.size()-1).updated_time;
			
			this.lastVisiblePos = wallList.getFirstVisiblePosition()+1;			
			Log.d(TAG, "pos="+lastVisiblePos);
			
			getWallMessage(lasttime, false);
		}	
		else
		{
			getWallMessage(0, true);
		}
    }
	private void getWallMessage(long fromstartTime, boolean newpost) 
	{	
		if(this.isInProcess())
		{
			Log.d(TAG, "I am still in process="+this);
			return ;
		}
		
		if(facebookA == null)
		{
			Log.d(TAG, "no session="+this);
			return ;
		}
		
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
		if(inputuid == -1)
    	{
    		inputuid = perm_session.getLogerInUserID();
    	}
		
		facebookA.getWallStreamAsync(inputuid,fromstartTime, limit, newpost, new FacebookAdapter()
    	{
    		@Override public void getWallStream(List<Stream> sts)
            {
    			Log.d(TAG, "after get stream="+sts.size());
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
				getLastViewCount(sts);
				
                if(donotcallnetwork == false)//I am still alive
                {							
	            	//cancelNotify();		            	
                } 
                //just has new item will change the UI
                if(sts.size() > 0)
                {
                    handler.obtainMessage(FACEBOOK_WALL_UI).sendToTarget();
                }
                
                Message rmsg = handler.obtainMessage(FACEBOOK_WALL_GET_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.sendToTarget();
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
            	
            	Log.d(TAG, "after get stream ex="+e.getMessage());
            	if(isInAynscTaskAndStoped())
             	{
             		Log.d(TAG, "User stop passive");
             	}
             	else
             	{
	            	Message rmsg = handler.obtainMessage(FACEBOOK_WALL_GET_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.sendToTarget();
             	}
            }
    	});
	}
    
   /* private void getWallMessage(boolean hasprogress)
    {
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
    	
    	Log.d(TAG, "before get wall message");
     	//notifyLoading();  
    	
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}
    	
    	//for refresh
    	if(inputuid == -1)
    	{
    		inputuid = perm_session.getLogerInUserID();
    	}
    	
    	facebookA.getWallAsync(inputuid, currentPos, limit, hasprogress, new FacebookAdapter()
    	{
    		@Override public void getWall(List<Wall> wallsfromweb,boolean hasprogress)
            {
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
				addWall(wallsfromweb);
				currentPos += wallsfromweb.size();
				
                if(donotcallnetwork == false)//I am still alive
                {   
                	 Message msd = handler.obtainMessage(FACEBOOK_WALL_UI);
                     msd.getData().putBoolean("hasprogress", hasprogress);
                     handler.sendMessage(msd);
	            	 //cancelNotify();
                }       
                
                Message msd = handler.obtainMessage(FACEBOOK_WALL_GET_END);
                msd.getData().putInt("count", wallsfromweb.size());
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
	            	 Message msd = handler.obtainMessage(FACEBOOK_WALL_GET_END);
	                 msd.getData().putBoolean("hasprogress", (Boolean)args[3]);
	                 handler.sendMessage(msd);
            	}
            }
    	});
    	
    }*/
    
    @Override protected void doAfterLoginNothing()
    {
    	super.doAfterLoginNothing();
    	
    	FacebookMainActivity.this.setResult(RESULT_CANCELED);
        FacebookMainActivity.this.finish();    
    }
    
    @Override
    protected void loadAfterSettingNoChange()
    {
        FacebookMainActivity.this.setResult(RESULT_CANCELED);
        FacebookMainActivity.this.finish();      
        Log.d(TAG , "loadAfterSettingNoChange");
    }
    
    private void notifyLoading() 
    {
    	notify.notifyOnce(R.string.facebook_wall_loading, R.drawable.facebook_logo, 30*1000);		
	}   
    public void registerAccountListener() {
		AccountManager.registerAccountListener("FacebookMainActivity", this);		
	}
	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("FacebookMainActivity");		
	}
}
