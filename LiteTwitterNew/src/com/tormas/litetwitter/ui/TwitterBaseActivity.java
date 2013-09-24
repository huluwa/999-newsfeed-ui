package com.tormas.litetwitter.ui;

import com.tormas.litetwitter.providers.SocialORM;
import com.tormas.litetwitter.providers.SocialORM.Account;
import com.tormas.litetwitter.ui.AccountListener.AccountManager;
import com.tormas.litetwitter.ui.ActivityBase.TitleListener;
import com.tormas.litetwitter.util.StatusNotification;
import twitter4j.AndroidAsyncTwitter;
import twitter4j.AsyncTwitter;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.AsyncTwitter.AsyncTask;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import com.tormas.litetwitter.R;

public abstract class TwitterBaseActivity extends ActivityBase implements TitleListener,TwitterAccountListener{
	
    //for activity
	public static final int TWITTER_SETTING  =1000;	
	public static final int TWITTER_DONOTHING=1001;
	
	public static final int TREND_DETAIL   =1002;	
	public static final int TWITTER_TWEETS =1003;
	public static final int TWEET_DETAIL   =1004;
	public static final int TWITTER_COMPOSE=1005;
	public static final int TWITTER_FOLLOWER        =1006;
	public static final int TWITTER_FOLLOWING       =1007;
	public static final int TWITTER_FOLLOWER_SEARCH =1008;
	public static final int TWITTER_ACCOUNT         =1009;	
	public static final int TWITTER_USER_SELECT     =1010;
	//for Handler message
	//for compose message view
	protected static final int TWITTER_SEND_MESSAGE      =2001;
	protected static final int TWITTER_DIRECT_MESSAGE    =2010;	
	protected static final int TWITTER_REPLY_MESSAGE     =2011;
	protected static final int TWITTER_RETWEET_MESSAGE   =2012;
	protected static final int TWITTER_MULTI_DIRECT_MESSAGE =2013;
	protected static final int TWITTER_SEND_MESSAGE_END  =2101;
	protected static final int TWITTER_SEND_MESSAGE_FAIL =2102;
	protected static final int TWITTER_UPDATE_STATUS     =2103;
	protected static final int TWITTER_UPDATE_TIRLE_PROGRESS = 2104;
	protected static final int TWITTER_MESSAGE_SET_ADD_BAR   = 2105;
	protected static final int TWITTER_RETWEET              = 2106;
	protected static final int TWITTER_RETWEET_END         = 2107;
	
	protected static final int TWITTER_STATUS_FRIEND_LINE=2002;
	protected static final int TWITTER_STATUS_USER_LINE  =2003;
	protected static final int TWITTER_STATUS_SEARCH_LINE=2004;
	protected static final int TWITTER_IS_FRIENDS        =2005;
	
	//detail view
	protected static final int TWEET_GET_USERINFO =3000;
	protected static final int TWEET_GET_USERINFO_END =3100;
	protected static final int TWEET_GET_STATUS   =3001;
	protected static final int TWEET_GET_STATUS_END   =3101;
	protected static final int TWEET_VIEW_FOLLOW  =3002;
	
	
	protected static final int TWEET_FAVOR        =3003;
	protected static final int TWEET_FAVOR_END    =3004;
	protected static final int TWEET_UNFAVOR      =3005;
	protected static final int TWEET_UNFAVOR_END  =3006;
	
	protected static final int TWEET_FOLLOW       =3007;
	protected static final int TWEET_FOLLOW_END   =3008;
	
	protected static final int TWEET_UNFOLLOW     =3009;
	protected static final int TWEET_UNFOLLOW_END =3010;	
	
	protected static final int FOLLOW_LOAD_BEGIN  =3011;
	protected static final int FOLLOW_CREATE_UI   =3012;
	protected static final int FOLLOW_LOAD_END    =3013;
	protected static final int FOLLOW_SEARCH_BEGIN=3014;
	protected static final int FOLLOW_SEARCH_END  =3015;
	protected static final int ACCOUNT_CREATE_UI  =3016;
	protected static final int FIND_PEOPLE_BEGIN  =3017;
	
	//for trend view
	protected static final int TREND_LOAD_TREND        =4000;
	protected static final int TREND_CREATE_TREND_UI   =4001;
    //protected static final int TREND_LOAD_TREND_REPEAT =4002;
    protected static final int TREND_LOAD_TREND_end    =4003;
    
    //for search view
    protected static final int TWEET_SEARCH            = 5000;
    protected static final int TWEET_SEARCH_END        = 5001;
    protected static final int TWEET_CONTRUCT_SEARCH_UI= 5002;
    
    //for tweets view
    protected static final int TWEET_LOAD_BEGIN     = 6000;
    protected static final int TWEET_LOAD_end       = 6001;
    protected static final int TWEET_CREATE_UI      = 6002;
    protected static final int TWEET_LOAD_INTERAL   = 6003;
    protected static final int MESSAGE_DIRECT       = 6004;
    protected static final int MESSAGE_SEND         = 6005;
    protected static final int MESSAGE_INBOX_DELETE = 6006;    
    protected static final int MESSAGE_INBOX_DELETE_END = 6007;
    protected static final int TWEET_MESSAGE_LOAD_end   = 6008;
    protected static final int TWEET_UPDATE             = 6009;
    protected static final int TWEET_UPDATE_END         = 6010;
    
    //for account info loading
    protected static final int FOLLOW_USER_DETAILS     = 7000;
    protected static final int FOLLOW_USER_DETAILS_END = 7001;
    
    //for image
    protected static final int STATUS_INSERT_IMG    = 80000;	
	protected static final int STATUS_CAPTURE_PHOTO = 80001;
    
    //private    
    protected static final int STOP_PROCESS        = 10002;
    
    //favor
    protected static final int FAVOR_DELETE       = 20000;
    protected static final int FAVOR_DELETE_END   = 20001;
    
    protected static final int GET_DATA_FROM_DB   = 30000;
    
    
    
    //
    //for dialog
    protected static final int DLG_TWEET_SEARCH  =0;
    protected static final int DLG_TWEET_COMPOSE =1;    
    protected static final int DLG_TREND_LOADING =2;
    protected static final int DLG_TWEET_LOADING =3;
    protected static final int DLG_ADD_FAVOR     =4;
    protected static final int DLG_DESTORY_FAVOR =5;
    protected static final int DLG_ADD_FOLLOW    =6;
    protected static final int DLG_DESTORY_FOLLOW =7;
    protected static final int DLG_SEND_MSG       =8;
    protected static final int DLG_RETWEET        =9;
    protected static final int TWITTER_UPDATE_STATUS_DLG =10;
	
	private static final String TAG = "TwitterBaseActivity";
	
	//intent parameters
	protected static final String STATUS_ID  ="status_id";//status id
	protected static final String TWITTER_ID ="twitter_id";//user id
	protected static final String RESULT     ="result";
	protected static final String REPLY     ="reply";
	protected static final String RETWEET   ="retweet";
	protected static final String FORWARD   ="forward";
	protected static final String DIRECT    ="direct";
	protected static final String UPDATE    ="update";
	protected static final String CONTENT    ="content";
	protected static final String FAVORITEVIEW ="view_favorities";
	protected static final String MESSAGEVIEW  ="view_messages";
	protected static final String FOLLOWER_SEARCH_VIEW ="view_follower_search";
	protected static final String FOLLOWER_VIEW        ="view_follower";
	protected static final String FOLLOWING_VIEW       ="view_following";
	protected static final String ACTION_CHECK_CONTECT ="com.tormas.litetwitter.intent.action.ACTION_CHECK_CONTECT";
	
	//for activity result
	protected static final int TWITTER_LOGIN = 20;
	
	//could we just keep one instance?
	protected AsyncTwitter twitterA;	
		
	protected SocialORM orm; 
	protected String    mysname;
	protected String    twitterid_db;
	protected StatusNotification notify;
	protected Handler handler;
	protected String  finalTitle;
	protected boolean isForLoginuser = false;
	protected int defaultTextLength = 140;
	public Twitter getTwitter()
	{
		return twitterA;
	}
	
	
	 protected View titleLayout;
	 protected TextView headerTitle;
	 protected boolean isShowTitleBar = false;
	 protected ProgressBar headerProgressBar;
	 protected ImageView   twitter_refresh;
	 protected Button      twitter_action;
	 
	@Override
	public void setTitle(CharSequence title) {		
		if(this.isBackgroud())
		{
			Log.d(TAG, "i am inback for setTitle="+title);
		}
		else
		{
			Log.d(TAG, "set Title="+title + " who="+this);
		    super.setTitle(title);
		    if(headerTitle != null)
		    headerTitle.setText(title);
		    
		    if(twitter_action != null)
		    twitter_action.setText(title);
		}
	}
	
    @Override
	public void setTitle(int titleId) {
    	if(this.isBackgroud())
		{
			Log.d(TAG, "i am inback for setTitle="+getString(titleId));
		}
		else
		{
			Log.d(TAG, "set Title="+getString(titleId)+" who="+this);
			super.setTitle(titleId);
			if(headerTitle != null)
			    headerTitle.setText(getString(titleId));
			
			 if(twitter_action != null)
				twitter_action.setText(getString(titleId));
		}
		
	}

    public void titleSelected()
    {
    	
    }

	@Override
	public void onLowMemory() 
	{
		System.gc();
		super.onLowMemory();
	}
	
	public SocialORM getSocialORM()
	{
		return orm;
	}
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);        
    	//this.requestWindowFeature(Window.FEATURE_LEFT_ICON);
       // this.requestWindowFeature(Window.FEATURE_RIGHT_ICON);
        this.requestWindowFeature(Window.FEATURE_PROGRESS);
        this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
       // this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.twitter_title);
        
        int orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
        super.setRequestedOrientation(orientation);
        
        Resources res = getResources();
        Drawable mCacheSym = res.getDrawable(R.color.facebook_backgroud);
        this.getWindow().setBackgroundDrawable(mCacheSym);
        
        Log.d(TAG, "onCreate="+this);
        
        mContext = this;
        createHandler();
        
        notify = new StatusNotification(this);        
        orm = new SocialORM(this);
        twitterid_db = orm.getTwitterAccount().uid;        
        
        basichandler = new BasicHandler();        
        setTitle();
        registerAccountListener();
        defaultTextLength = Integer.valueOf(getString(R.string.twitter_default_count));
    }
    
    
   
    protected void begin()
    {
    	basichandler.obtainMessage(TITLE_PROGRESS_begin).sendToTarget();    	
    }
    protected void searchbegin()
    {
    	basichandler.obtainMessage(TITLE_PROGRESS_search_begin).sendToTarget();    	
    }
    protected void prepare()
    {
    	basichandler.obtainMessage(TITLE_PROGRESS_prepare).sendToTarget();
    }
    protected void afterPrepare()
    {
    	basichandler.obtainMessage(TITLE_PROGRESS_afterprepare).sendToTarget();
    }
    protected void using()
    {
    	basichandler.obtainMessage(TITLE_PROGRESS_using).sendToTarget();
    }
    protected void stoping()
    {
    	inprocess = false;
    	basichandler.obtainMessage(TITLE_PROGRESS_end).sendToTarget();
    	//basichandler.obtainMessage(TITLE_PROGRESS_stop).sendToTarget();
    }
    
    @Override
	public void setContentView(int resId)
	{
		super.setContentView(resId);
		
		View top = this.findViewById(resId);
		if(top != null)
		{
			Log.d(TAG, "set background="+this);
			Resources res = getResources();
		    Drawable mCacheSym = res.getDrawable(R.color.facebook_backgroud);
			top.setBackgroundDrawable(mCacheSym);
		}
		
	}
    
    protected void end()
    {
    	inprocess = false;
    	basichandler.obtainMessage(TITLE_PROGRESS_end).sendToTarget();
    }
    
	//
    final int TITLE_PROGRESS_begin          = 8900;
    final int TITLE_PROGRESS_search_begin   = 8901;
    final int TITLE_PROGRESS_prepare        = 8902;
    final int TITLE_PROGRESS_afterprepare   = 8903;
    final int TITLE_PROGRESS_using   = 8904;
    final int TITLE_PROGRESS_end     = 8905;
    final int TITLE_PROGRESS_stop    = 8906;
    //process the basic twitter action
    private class BasicHandler extends Handler 
    {
        public BasicHandler()
        {
            super();
            
            Log.d(TAG, "new HandlerLoad");
        }
        
        protected void setProgressNoTitle(int progress)
        {
        	setProgress(progress);
            if(null == headerProgressBar)
            {
                return;
            }
            
            if(100*100 == progress)
            {
            	if(headerProgressBar != null)
            	{
	                headerProgressBar.setVisibility(View.GONE);	                
            	}
            	if(twitter_refresh != null)
            	{
            		twitter_refresh.setVisibility(View.VISIBLE);
            	}
            	
            }
            else
            {
            	if(headerProgressBar != null)
            	{
                    headerProgressBar.setVisibility(View.VISIBLE);                
            	}
            	
            	if(twitter_refresh != null)
            	{
            		twitter_refresh.setVisibility(View.GONE);
            	}
            }
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case  TITLE_PROGRESS_begin:
	            {  
	     	    	if(isBackgroud())
	     			{
	     				
	     			}
	     			else
	     			{
	     				setProgressNoTitle(20);
	     	    	    
	     			    //setTitle(START_REQUEST);
	     			}
	         	   break;
	            }
	            case  TITLE_PROGRESS_search_begin:
	            {  
	     	    	if(isBackgroud())
	     			{
	     				
	     			}
	     			else
	     			{
	     				setProgressNoTitle(20);
	     			    //setTitle(TITLE_SEARCH);
	     			}
	         	   break;
	            }
	            case  TITLE_PROGRESS_prepare:
	            {
	         		if(isBackgroud())
	     			{
	     				
	     			}
	     			else
	     			{
	     				setProgressNoTitle(5);
	     			    //setTitle(PREPARING);
	     			}
	         	    break;
	            }
	            case TITLE_PROGRESS_afterprepare:
	            {
	         	    if(isBackgroud())
	       			{
	       				
	       			}
	       			else
	       			{
	       				setProgressNoTitle(10);
	       			    //setTitle(PREPARING);
	       			}
	         	    break;
	            }
	            case  TITLE_PROGRESS_using:
	            {
	         	    if(isBackgroud())
	       		    {
	       				
	       		    }
	       			else
	       			{
	       				setProgressNoTitle(DATA_READY_PROGRESS*100);
	       	    	    //setTitle(TITLE_CONSTRUCT_UI);
	       	        }
	         	    break;
	            }
	            case TITLE_PROGRESS_stop:
	            {
	            	if(isBackgroud())
	       			{
	       				
	       			}
	       			else
	       			{
	       				setProgressNoTitle(STOP_PROGRESS*100);
                        //setTitle(TITLE_STOPING);
	       		 	}
	            	break;
	            }
	            case  TITLE_PROGRESS_end:
	            {
	         	    if(isBackgroud())
	       			{
	       				
	       			}
	       			else
	       			{
	       				setProgressNoTitle(100*100);	
	       	    	    setTitle(finalTitle);
	       		 	}
	         	    break;
	            }            
	            case STOP_PROCESS:
	            {
	            	Log.d(TAG, "call STOP_PROCESS will stop the loading");
	            	stopProcess();
	            	break;
	            }
                case UI_SET_PROGRESS:
                {
                	int progress = msg.getData().getInt("progress");
                	mContext.setProgress(progress);
                	break;
                }
                case UI_SET_TITLE:
                {
                	String title = msg.getData().getString("title");
            	    mContext.setTitle(title);
                	break;
                }
                case TWEET_FAVOR:
                {
                	Log.d("TAG", "entering Tweet_favor ");
                    mContext.showDialog(DLG_ADD_FAVOR);
                    long statusID = msg.getData().getLong(STATUS_ID);
                    twitterA.createFavoriteAsync(statusID, new TwitterAdapter() 
                    {
                        @Override public void createdFavorite(Status st)
                        {
                            Log.d(TAG, "We suc create favor");
                            if(handler !=null)
                            {
                                Message message = handler.obtainMessage(TWEET_FAVOR_END);
                                message.getData().putBoolean(RESULT, true);
                                message.sendToTarget();
                            }
                            mContext.dismissDialog(DLG_ADD_FAVOR);
                        }
    
                        @Override public void onException(TwitterException e, int method) 
                        {                   
                            Log.d(TAG, "Fail to add favor ="+e.getMessage()); 
                            if(handler !=null)
                            {
                                Message message = handler.obtainMessage(TWEET_FAVOR_END);
                                message.getData().putBoolean(RESULT, false);
                                message.sendToTarget();
                            }
                            mContext.dismissDialog(DLG_ADD_FAVOR);
                        }           
                    });          
                    break;
                }
                case TWEET_UNFAVOR:
                {
                	Log.d(TAG, "entering Tweet_unfavor ");
                    mContext.showDialog(DLG_DESTORY_FAVOR);
                    long statusID = msg.getData().getLong(STATUS_ID);
                    twitterA.destroyFavoriteAsync(statusID, new TwitterAdapter() 
                    {
                        @Override public void destroyedFavorite(Status st)
                        {
                            Log.d(TAG, "We suc destory favor");
                            Message message = handler.obtainMessage(TWEET_UNFAVOR_END);
                            message.getData().putBoolean(RESULT, true);
                            message.getData().putLong(STATUS_ID, st.getId());
                            message.sendToTarget();  
                            mContext.dismissDialog(DLG_DESTORY_FAVOR);
                        }
    
                        @Override public void onException(TwitterException e, int method) 
                        {                   
                            Log.d(TAG, "Fail to remove favor ="+e.getMessage());                            
                            Message message = handler.obtainMessage(TWEET_UNFAVOR_END);
                            message.getData().putBoolean(RESULT, false);
                            message.sendToTarget(); 
                            mContext.dismissDialog(DLG_DESTORY_FAVOR);
                        }           
                    });        
                    break;
                }
                case TWEET_FOLLOW:
                {
                    mContext.showDialog(DLG_ADD_FOLLOW);
                    String twitterID = msg.getData().getString(TWITTER_ID);
                    twitterA.createFriendshipAsync(twitterID, new TwitterAdapter(){
                        @Override public void createdFriendship(User user)
                        {
                            Log.d(TAG, "We suc follow user="+user);
                            Message message = handler.obtainMessage(TWEET_FOLLOW_END);
                            message.getData().putBoolean(RESULT, true);
                            message.getData().putString(TWITTER_ID, user.getScreenName());
                            message.sendToTarget();  
                            mContext.dismissDialog(DLG_ADD_FOLLOW);
                        }
                        
                        @Override public void onException(TwitterException e, int method) 
                        {                   
                            Log.d(TAG, "Fail to add follow ="+e.getMessage());                          
                            Message message = handler.obtainMessage(TWEET_FOLLOW_END);
                            message.getData().putBoolean(RESULT, false);                            
                            message.sendToTarget(); 
                            mContext.dismissDialog(DLG_ADD_FOLLOW);
                        }           
                    });
                    
                    break;
                }
                case TWEET_UNFOLLOW:
                {
                    mContext.showDialog(DLG_DESTORY_FOLLOW);
                    String twitterID = msg.getData().getString(TWITTER_ID);
                    twitterA.destroyFriendshipAsync(twitterID, new TwitterAdapter(){
                    	@Override public void destroyedFriendship(User user){
                    		Log.d(TAG, "We suc remove follow user="+user);
                            Message message = handler.obtainMessage(TWEET_UNFOLLOW_END);
                            message.getData().putBoolean(RESULT, true);
                            message.getData().putString(TWITTER_ID, user.getScreenName());
                            message.sendToTarget();           
                            mContext.dismissDialog(DLG_DESTORY_FOLLOW);
                    	}
                    	
                    	@Override public void onException(TwitterException e, int method) 
                        {                   
                            Log.d(TAG, "Fail to remove follow ="+e.getMessage());                           
                            Message message = handler.obtainMessage(TWEET_UNFOLLOW_END);
                            message.getData().putBoolean(RESULT, false);                            
                            message.sendToTarget();    
                            mContext.dismissDialog(DLG_DESTORY_FOLLOW);
                        }           
                    });
                    break;
                }
                case TWITTER_RETWEET:
                {
                    mContext.showDialog(DLG_RETWEET);
                    long statusId = msg.getData().getLong(STATUS_ID);
                    twitterA.retweetStatusAsync(statusId, new TwitterAdapter()
                    {
                        @Override public void  retweetedStatus(Status status)
                        {
                           Log.d(TAG, "after retweet status="+status);
                           Message msd = basichandler.obtainMessage(TWITTER_RETWEET_END);
                           msd.getData().putBoolean(RESULT, true);
                           msd.sendToTarget();
                           mContext.dismissDialog(DLG_RETWEET);
                        }
                       
                        @Override public void onException(TwitterException e, int method) 
                        {                   
                           Log.d(TAG, "Fail to retweet status ="+e.getMessage());    
                           Message msd = basichandler.obtainMessage(TWITTER_RETWEET_END);
                           msd.getData().putBoolean(RESULT, false);
                           msd.sendToTarget();
                           mContext.dismissDialog(DLG_RETWEET);
                        }           
                    });
                    break;
                }
                case TWITTER_RETWEET_END:
                {
                    boolean result = msg.getData().getBoolean(RESULT);
                    if(result == true)
                    {
                        Toast.makeText(mContext,getString(R.string.retweet_success), Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(mContext,getString(R.string.retweet_failed), Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    }
    /*
     * (to implement the handler)
     * @see com.tormas.litetwitter.ui.TwitterHandlerInterface#createHandler()
     */
    public void createHandler()
    {
    	
    }
    
	public void stopProcess() 
	{
		Log.d(TAG, "call stop process="+this);
	}

	boolean checkTwitterAccount(Context con, Account account)
	{
		if(isEmpty(account.token) == true || isEmpty(account.token_secret) == true/*account.email == null || account.email.length() ==0 
			||	account.password == null || account.password.length()==0 ||
				account.screenname == null || account.screenname.length() == 0*/ )
		{
			//TODO prompt to input the email and password        	
        	/*Intent intent = new Intent(con, TwitterSettingPreference.class);            
        	((Activity)con).startActivityForResult(intent, TWITTER_SETTING);   */
		    Intent intent = new Intent(con, TwitterLoginActivity.class);
		    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityForResult(intent, TWITTER_LOGIN);
			return false;
		}
		
		if(twitterA == null)
		{
		    twitterA = new AndroidAsyncTwitter(account.token,account.token_secret,true);
		    twitterA.attachActivity(TwitterBaseActivity.this);
		    twitterA.setUserId(account.email);
		    twitterA.setPassword(account.password);
		}
		else
		{	
			twitterA.setUserId(account.email);
			twitterA.setPassword(account.password);
		}	
		mysname = account.screenname;
		twitterid_db = account.screenname;
		
		return true;
	}
	
	boolean checkTwitterAccount(Context con, Account account, boolean nosetting)
	{
		if(isEmpty(account.token) == true || isEmpty(account.token_secret) == true /*account.email == null || account.email.length() ==0 
			||	account.password == null || account.password.length()==0 ||
				account.screenname == null || account.screenname.length() == 0*/)
		{
			//prompt to input the email and password        
			if(nosetting == false)
			{
	        	Intent intent = new Intent(con, TwitterSettingPreference.class);            
	        	((Activity)con).startActivityForResult(intent, TWITTER_SETTING);    		
			}
			return false;
		}
		
		if(twitterA == null)
		{
		    twitterA = new AndroidAsyncTwitter(account.token, account.token_secret,true);
		    twitterA.attachActivity(TwitterBaseActivity.this);
		    twitterA.setUserId(account.email);
            twitterA.setPassword(account.password);
		}
		else
		{	
			twitterA.setUserId(account.email);
			twitterA.setPassword(account.password);
		}	
		mysname = account.screenname;
		twitterid_db = account.screenname;
		
		return true;
	}
	
    //notify the status
    protected void cancelNotify() 
    {
        notify.cancel();
    }
	
    @Override protected void onResume() 
    {
    	super.onResume();    
    	if(twitterA != null)
            twitterA.resumeCallNetWork();        
    	
    	restoreTitle();
    }    
    
    private void restoreTitle()
    {
    	setTitle(finalTitle);
    }
    
    @Override protected void onPause() {
        super.onPause();                
        stopProcess();
    }
    
    @Override protected void onDestroy() 
    {   
        super.onDestroy();   
        this.unregisterAccountListener();
        //cancel the loading
        if(twitterA != null)
        {
           twitterA.stopCallNetWork();           
        }
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	return super.onOptionsItemSelected(item);
    }
    
    protected void loadAfterSetting()
	{
		Log.d(TAG, "return from twitter setting to call the launch");
	}
    
	protected void loadAction()
	{
		Log.d(TAG, "return from twitter setting to call the launch");
	}
	protected void loadAfterSettingNoChange()
	{
	    Log.d(TAG,"return from twitter setting to home screen");
	}
	protected void loadRefresh()
	{
		Log.d(TAG, "return from twitter refresh to call the launch");
	}
	protected void doNothing() 
	{
		Log.d(TAG, "return from twitter doNothing");		
	}	
	protected void loadSearchForFollowing()
    {
    	Log.d(TAG, "call search in user detail");
    }
	protected void loadFollowersFromFollowing()
	{
		Log.d(TAG, "enter followers view");
	}
	protected void loadFollowingsFromFollower()
	{
		Log.d(TAG, "enter follower's following view");
	}
	protected void loadNextPage()
	{
		Log.d(TAG, "load next page");
	}
	protected void loadPrePage()
	{
		Log.d(TAG, "load next page");
	}
	protected void loadNewMessage()
	{
		Log.d(TAG, "load new message");
	}
	protected void loadDeleteMessage()
	{
		Log.d(TAG, "load delete message");
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
	      switch(requestCode)
	      {  
	           /* case TWITTER_SETTING:
	            {
	            	Log.d(TAG, "return from setting");
	            	//changed account
	            	if(resultCode == 100)
	            	{
	            	    loadAfterSetting();
	            	}
	            	else if(resultCode == 0)
	            	{
	            	    loadAfterSettingNoChange();
	            	}
	            	else
	            	{
	            		loadAction();
	            	}
	            	
	            	break;
	            }*/
	            case TWITTER_LOGIN:
	            {
	                if(resultCode == 200)
	                {
	                    //login suc
	                    doAfterLogin();
	                }
	                else if(resultCode == 1000)
	                {
	                    doAfterLoginNothing();
	                }
	            }
	            case TWITTER_DONOTHING:
	            {
	            	doNothing();
	            	break;
	            }
	            case TWITTER_TWEETS:
	            {
	            	Log.d(TAG, "return from tweets view");
	                System.gc();
	                break;
	            }
	            case TWITTER_COMPOSE:
	            {
	            	Log.d(TAG, "return from compose message");
	            	break;
	            }
	            case TWITTER_FOLLOWER:
	            {
	            	Log.d(TAG, "return from follow view");
	            	break;
	            }
	            case TWITTER_FOLLOWING:
	            {
	            	Log.d(TAG, "return from following view");
	            	break;
	            }
	            case TWITTER_FOLLOWER_SEARCH:
	            {
	            	Log.d(TAG, "return from follow search view");
	            	break;
	            }
	      }
	}
	
	protected void doAfterLoginNothing() {
        // TODO Auto-generated method stub
        
    }

    protected void doAfterLogin() {
       //doAfterLogin
    }

    @Override protected Dialog onCreateDialog(int id) {
	        String title="";
	        boolean valid=true;
	        switch (id) {
	            case DLG_TWEET_SEARCH: 
	            {
	                title= getString(R.string.twitter_dlg_tweet_search);
	                break;
	            }
	            case DLG_SEND_MSG:      
	            {
	                title=getString(R.string.twitter_dlg_send_msg);
	                break;
	            }
	            case DLG_TWEET_COMPOSE:
	            {
                    title = getString(R.string.twitter_dlg_update_status);
                    break;
                }	          
	            case DLG_TREND_LOADING: 
	            {
	                title = getString(R.string.twitter_dlg_load_trend);
	                break;
	            }	      
	            case DLG_TWEET_LOADING: 
	            {
	                title = getString(R.string.twitter_dlg_load_tweets);
	                break;
	            }
	            case DLG_ADD_FAVOR:
	            {
	                title = getString(R.string.twitter_dlg_add_favor);
                    break;
	            }
	            case DLG_DESTORY_FAVOR:
                {
                    title = getString(R.string.twitter_dlg_destory_favor);
                    break;
                }
	            case DLG_ADD_FOLLOW:
                {
                    title = "";//getString(R.string.twitter_dlg_add_following);
                    break;
                }
	            case DLG_DESTORY_FOLLOW:
                {
                    title = "";//getString(R.string.twitter_dlg_destory_follow);
                    break;
                }
	            case DLG_RETWEET:
	            {
	                title = getString(R.string.twitter_dlg_retweet);
	                break;
	            }
	            case TWITTER_UPDATE_STATUS_DLG:
	            {
	                title = getString(R.string.twitter_send_status);
	                break;
	            }
                default:
                    valid =false;
                    break;
	        }
	        
	        if(valid)
	        {
	            ProgressDialog dialog = new ProgressDialog(this);
    	        dialog.setTitle(title);
                dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
                dialog.setCanceledOnTouchOutside(true);                
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
	        }
            
	        return null;
	    }
	 
	    @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event)  
	    {        
	        if (keyCode == KeyEvent.KEYCODE_BACK) 
	        {         
	            stopLoading();	            
	            System.gc();
                    
	        }
	        return super.onKeyDown(keyCode, event);
	    }
		
		public void finishLoading(String title)
		{
			 updateProgress(100*100);			 
             updateTitle(title);
		}	
	
  private Object logObj = new Object();
  public void onLogin() {
        Log.d(TAG, "onLogin=" + this);
        synchronized (logObj) {
           //TODO
        }
    }

    public void onLogout() {
        Log.d(TAG, "onLogout=" + this);
        synchronized (logObj) {
           //TODO
        }
    }
		
	public void registerAccountListener() 
    {
        TwitterAccountManager.registerAccountListener(this.getClass().getName(), this);     
    }
    public void unregisterAccountListener() 
    {
        TwitterAccountManager.unregisterAccountListener(this.getClass().getName());     
    }
}
