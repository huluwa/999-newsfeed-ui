package com.msocial.free.ui;

//import oms.frmwork.gesture.GestureEvent;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import com.msocial.free.*;
import com.msocial.free.providers.SocialORM;
import com.msocial.free.providers.SocialORM.Account;
import com.msocial.free.service.FacebookLoginHelper;
import com.msocial.free.ui.AccountListener.AccountManager;
import com.msocial.free.ui.ActivityBase.TitleListener;
import com.msocial.free.ui.FacebookAlbumActivity.FacebookAlbumItemView;
import com.msocial.free.ui.FacebookAlbumViewActivity.AlbumItemView;
import com.msocial.free.ui.view.CommentItemView;
import com.msocial.free.ui.FacebookNotificationManActivity.NotesItemView;
import com.msocial.free.ui.view.FacebookEventItemView;
import com.msocial.free.ui.view.FacebookFindFriendItemView;
import com.msocial.free.ui.view.FacebookFriendItemView;
import com.msocial.free.ui.view.FacebookMailItemView;
import com.msocial.free.ui.view.FacebookNotificationItemView;
import com.msocial.free.ui.view.FacebookPhoneBookItemView;
import com.msocial.free.ui.view.FacebookStatusView;
import com.msocial.free.ui.view.MessageItemView;
import com.msocial.free.ui.view.MessageThreadInfoItemView;
import com.msocial.free.ui.view.SNSItemView;

import com.msocial.free.util.StatusNotification;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.MailboxMessage;
import oms.sns.service.facebook.model.MailboxThread;
import oms.sns.service.facebook.model.MessageThreadInfo;
import oms.sns.service.facebook.model.Notes;
import oms.sns.service.facebook.model.Page;
import oms.sns.service.facebook.model.PhoneBook;
import oms.sns.service.facebook.model.Photo;
import oms.sns.service.facebook.model.PhotoAlbum;
import oms.sns.service.facebook.model.Stream;
import oms.sns.service.facebook.model.Stream.Comments.Stream_Post;
import oms.sns.service.facebook.util.StringUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.CountDownTimer;

public abstract class FacebookBaseActivity extends ActivityBase implements
        View.OnCreateContextMenuListener, TitleListener, AccountListener{

    final String TAG = "FacebookBaseActivity";
    // for activity result
    protected static final int FACEBOOK_SETTING 		= 0;
    protected static final int FACEBOOK_INBOX        	= 1;
    protected static final int FACEBOOK_LOGIN 			= 2;
    protected static final int FACEBOOK_MESSAGE_UI 		= 3;
    protected static final int FACEBOOK_ACCOUNT_UI 		= 4;
    public 	  static final int FACEBOOK_USER_DETAIL 	= 5;
    public    static final int FACEBOOK_MAIL_DETAIL 	= 6;
    protected static final int FACEBOOK_STATUS_UPDATE_UI= 7;
    protected static final int FACEBOOK_EXT_PERMISSION_UI = 8;
    protected static final int FACEBOOK_GROUP_UI 		= 9;
    protected static final int FACEBOOK_FRIENDS_UI 		= 10;
    public    static final int FACEBOOK_MAIL_SEND 		= 11;
    public    static final int FACEBOOK_PHONEBOOK 		= 12;
    public    static final int FACEBOOK_NEW_MAIL 		= 13;
    protected static final int FACEBOOK_USER_SELECT 	= 14;
    protected static final int FACEBOOK_FRIEND_STATUS 	= 15;
    public static final int FACEBOOK_PHONRBOOK_DETAIL 	= 16;
    public static final int FACEBOOK_NEWS_FEED 			= 17;

    public static final int FACEBOOK_LOCATION_UPDATE_UI = 18;
    public static final int FACEBOOK_EVENT 				= 19;
    public static final int FACEBOOK_FRIEND_BD_UI 		= 20;
    protected static final int FACEBOOK_FIND_FRIENDS 	= 21;
    protected static final int FACEBOOK_SET_CONTACT 	= 22;
    protected static final int FACEBOOK_NOTIFICATIONS_UI  = 23;
    protected static final int FACEBOOK_SEARCH_FRIENDS_UI = 24;
    public static final int FACEBOOK_COMMENTS 		      = 25;
    public static final int FACEBOOK_SELECT_STREAM_FILTRE = 26;
    public static final int FACEBOOK_NOTIFICATION_RESULT  = 27;

    // for facebook account/setting
    protected static final int FACEBOOK_LOGIN_MSG 		= 1000;
    protected static final int FACEBOOK_SETTING_MSG 	= 1001;
    protected static final int FACEBOOK_DONOTHING 		= 1002;

    public static final int FACEBOOK_GET_USERINFO 		= 1003;
    public static final int FACEBOOK_STATUS_UPDATE 		= 1004;

    // for wall message
    protected static final int FACEBOOK_WALL_GET 		= 2000;
    protected static final int FACEBOOK_WALL_UI 		= 2001;
    protected static final int FACEBOOK_WALL_GET_END 	= 2002;

    protected static final int FACEBOOK_WALL_POST 		= 2003;
    protected static final int FACEBOOK_WALL_POST_END 	= 2004;

    // for mailbox message
    protected static final int FACEBOOK_MAIL_INBOX_GET 	= 3000;
    protected static final int FACEBOOK_MAIL_INBOX_UI 	= 3001;
    protected static final int FACEBOOK_MAIL_INBOX_GET_END = 3002;

    protected static final int FACEBOOK_MAIL_SEND_GET 		= 3003;
    protected static final int FACEBOOK_MAIL_SEND_UI 		= 3004;
    protected static final int FACEBOOK_MAIL_SEND_GET_END 	= 3005;

    protected static final int FACEBOOK_MAIL_UPDATE_GET 	= 3006;
    protected static final int FACEBOOK_MAIL_UPDATE_UI 		= 3007;
    protected static final int FACEBOOK_MAIL_UPDATE_GET_END = 3008;

    protected static final int POKE_SOMEONE 				= 3009;
    protected static final int POKE_SOMEONE_END 			= 3010;

    public static final int LIKE_STREAM 					= 3011;
    public static final int UNLIKE_STREAM 					= 3012;
    protected static final int LIKE_STREAM_END 				= 3013;
    public static final int UNLIKE_STREAM_END 				= 3014;
    public static final int FACEBOOK_REMOVE_COMMENT 		= 3015;
    protected static final int FACEBOOK_REMOVE_COMMENT_END 	= 3016;

    protected static final int PHOTO_DELETE 				= 3100;
    protected static final int PHOTO_DELETE_END 			= 3101;
    protected static final int ALBUM_DELETE 				= 3102;
    protected static final int ALBUM_DELETE_END 			= 3103;

    protected static final int STATUS_INSERT_IMG 			= 4000;
    protected static final int STATUS_INSERT_VIDEO 			= 4001;
    protected static final int STATUS_CAPTURE_PHOTO 		= 4002;
    protected static final int STATUS_LINK_RESULT 			= 4003;
    protected static final String ACTION_CHECK_CONTECT = "com.msocial.free.intent.action.ACTION_CHECK_CONTECT";

    // for activity result code
    protected static final int CHANGED_ACCOUNT = 1000;
    protected static final int LOGIN_SUC       = 100;
    protected static final int LOGIN_FAIL      = 200;
    
    protected static final int PINFO_UINFO_GET    = 5000;    
    protected static final int EVENT_REQUEST_CODE = 5001;

    protected static final String RESULT = "result";
    
    protected AsyncFacebook facebookA;

    protected FacebookLoginHelper loginHelper;
    protected SocialORM orm;
    protected FacebookSession perm_session;

    protected Handler handler;
    protected ContextMenu contextMenu;
    protected Menu optionMenu;

    protected StatusNotification notify;

    protected abstract void createHandler();

    protected String title = "";

    protected boolean isShowTitleBar = false;
    protected LinearLayout titleLayout;
    protected TextView headerTitle;
    protected ProgressBar headerProgressBar;
	
    /**
     * Helper for detecting touch gestures.
     */
   GestureDetector mGestureDetector;
   
    protected boolean hasSession() {
        return perm_session != null;
    }

    protected ProgressBar progressHorizontal;
    boolean fromtabview = false;

    public boolean isFromTabView() {
        return fromtabview;
    }

    protected void showOptionMenu() 
    {
        if(isShowTitleBar)
        {
            
        }
    }

    protected void enableNoTitleBar() {
       
    }
    
    protected void setTitleMenuIconVisible(boolean dd)
    {
        
    }
    //need implemented in sub class, please make sure the clear is really need
    protected void clearAsyncFacebook(boolean forceclear)
    {
    	if(forceclear)
    		facebookA = null;
    }
    
    protected long getLoginUserID()
    {
        if(perm_session != null)
            return perm_session.getLogerInUserID();
        
        return -1;
    }

    protected void enableProgress() {
        //this.requestWindowFeature(Window.FEATURE_PROGRESS);
        this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        orm = SocialORM.instance(this.getApplicationContext());      
        //mGestureDetector = new GestureDetector(this, new FBGestureListener());
        //isShowTitleBar = orm.isShowTitleBar();
        
		showOptionMenu();

		fromtabview = this.getIntent().getBooleanExtra("fromtabview", false);
		if (fromtabview == false) {
			// this.requestWindowFeature(Window.FEATURE_LEFT_ICON);
			// this.requestWindowFeature(Window.FEATURE_RIGHT_ICON);
		    if(true == isShowTitleBar)
	        {
    			enableProgress();
				enableNoTitleBar();
    		}
		    else
		    {
		         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		    }
			// this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
            // R.drawable.facebook_title);
        } else {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            // this.requestWindowFeature(Window.FEATURE_RIGHT_ICON);
        }

        // int orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
        // super.setRequestedOrientation(orientation);

        Resources res = getResources();
        Drawable mCacheSym = res.getDrawable(R.color.facebook_backgroud);
        this.getWindow().setBackgroundDrawable(mCacheSym);

        Log.d(TAG, "onCreate=" + this);
        mContext = this;

        loginHelper = FacebookLoginHelper.instance(this.getApplicationContext());        
        
        setTitle();
        notify = new StatusNotification(mContext);

        basichandler = new BasicHandler();
        createHandler();

        this.registerAccountListener();
    }
    /*
    class FBGestureListener extends GestureDetector.SimpleOnGestureListener
    {
    	@Override 
    	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distancex, float distanceY)
    	{
    		Log.d(TAG, "do onScroll");
    		boolean ret = false;
    		gestureprocessed = true;
    		if(distancex > 0)    	
    		{       
    	        ret = goNextPage();
    	    }
    		else
    		{
    	        ret = goPrePage();    	    
    	    }
    		
    		return ret;
    	}
    }*/

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
            
            titleLayout = (LinearLayout)findViewById(R.id.header_no_title_layout);
            titleLayout.setVisibility(View.VISIBLE);
            headerTitle = (TextView)findViewById(R.id.header_title);
            headerProgressBar = (ProgressBar)findViewById(R.id.header_progressbar);
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
            //titleLayout = (LinearLayout)findViewById(R.id.header_title_layout);
            //titleLayout.setVisibility(View.GONE);
            titleLayout = (LinearLayout)findViewById(R.id.header_no_title_layout);
            titleLayout.setVisibility(View.VISIBLE);
            headerTitle = (TextView)findViewById(R.id.header_title);
            headerProgressBar = (ProgressBar)findViewById(R.id.header_progressbar);
        }
    }

    protected boolean gestureprocessed = false;
    protected static long gestureTime;
    
    /*
    @Override
    public boolean dispatchGestureEvent(GestureEvent event) {
        // don't process in Tab view
        if (isFromTabView()) {
            gestureprocessed = false;
            return false;
        }

        boolean ret = false;
        switch (event.getType()) {
        case GestureEvent.GestureType.LEFT: {
            gestureprocessed = true;
            ret = goNextPage();
            break;
        }
        case GestureEvent.GestureType.RIGHT: {
            gestureprocessed = true;
            ret = goPrePage();
            break;
        }
        }

        if (ret == true) {
            gestureTime = (new Date()).getTime();
            new CountDownTimer(0, 2 * 1000) {

                @Override
                public void onFinish() {
                    //Log.d(TAG, "************* time is coming, ");
                    gestureprocessed = false;
                }

                @Override
                public void onTick(long millisUntilFinished) {
                    //Log.d(TAG, "************* time is coming, ");
                    gestureprocessed = false;
                }

            }.start();
            return true;
        }

        return super.dispatchGestureEvent(event);
    }
    */
    
    protected boolean goNextPage() {
        return false;
    }

    protected boolean goPrePage() {
        return false;
    }

    boolean afterlogin = false;
    BroadcastReceiver mHangReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean connected = intent.getBooleanExtra("connected", false);
            if (connected == true) {
                Log.d(TAG, "after facebook user login in main");
                afterlogin = true;

                doAfterLogin();
            }
        }
    };

    protected boolean isSafeCallFacebook(boolean needCheckPreProcess)
    {
    	boolean enable = true;
    	if(this.isFinishing() == true)
 		{
 			Log.d(TAG, "I am finishing, return");
 			enable =  false;
 		}
    	 
		if(needCheckPreProcess == true && this.isInProcess() == true)
		{
			Log.d(TAG, "previouse is still in loading, return");
			enable = false;
		}
		
		if(existSession() == false)
    	{ 
			enable = false;
		}
		return enable;
    }
    
    protected void showToast(final String toastStr) {
       handler.post(new Runnable()
       {
           public void run()
           {
               Toast.makeText(mContext,toastStr,Toast.LENGTH_SHORT).show();
           }
       });
    }
    protected void showToast()
    {
        handler.post(new Runnable()
        {
            public void run()
            {
                Toast.makeText(mContext,getString(R.string.getting_info_from_server),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() 
    {        
        unregisterAccountListener();

        if(notify != null)
        {
	        notify.cancel();
	        notify = null;
        }
        
        if (perm_session != null) 
        {
            loginHelper.destroy(this);
            perm_session.destroy();
            //perm_session = null;
        }
        
        Log.i(TAG, "facebookA set null");
        clearAsyncFacebook(false);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopProcess();

        detatch();
    }

    protected void restoreTitle() {
        this.setTitle(title);
        
		/*if(isShowTitleBar == false)
        {
		    setProgressNoTitle(100 * 100);
		}
		else
		{
		    setProgress(100 * 100);
		}*/
        setProgressForFacebook(100*100);
        
    }

    @Override
    protected void onResume() {
        super.onResume();

        // restore the session, because the activity may come from the history
        perm_session = loginHelper.getPermanentSesstion(this);
        if (perm_session != null) {
            facebookA = new AsyncFacebook(perm_session);
            attatch();
        }
        // restore the title bar
        // restoreTitle();
    }

    private void detatch() {
        if (perm_session != null)
            perm_session.attachActivity(null);
    }

    private void attatch() {
        if (perm_session != null)
            perm_session.attachActivity(this);
    }

    public Handler getBasicHandler() {
        return basichandler;
    }

    public AsyncFacebook getAsyncFacebook() {
        return facebookA;
    }

    public FacebookSession getFSession() {
        return perm_session;
    }

    protected void begin() {
        basichandler.obtainMessage(TITLE_PROGRESS_begin).sendToTarget();
    }

    protected void begindb() {
        basichandler.obtainMessage(TITLE_PROGRESS_begin_db).sendToTarget();
    }

    protected void prepare() {
        basichandler.obtainMessage(TITLE_PROGRESS_prepare).sendToTarget();
    }

    protected void afterPrepare() {
        basichandler.obtainMessage(TITLE_PROGRESS_afterprepare).sendToTarget();
    }

    protected void using() {
        basichandler.obtainMessage(TITLE_PROGRESS_using).sendToTarget();
    }

    protected void stoping() {
        inprocess = false;
        basichandler.obtainMessage(TITLE_PROGRESS_end).sendToTarget();
        // basichandler.obtainMessage(TITLE_PROGRESS_stop).sendToTarget();
    }

    protected void end() {
        basichandler.obtainMessage(TITLE_PROGRESS_end).sendToTarget();
    }

    @Override
    public void setTitle(CharSequence title) {
        if (this.isBackgroud()) {

        } else {
            if(isShowTitleBar == false)
            {
                if(headerTitle != null){
                    headerTitle.setText(title);
                }
            }
            else{
                super.setTitle(title);
            }
        }
    }

    boolean checkFacebookAccount(Context con, Account account) {
        boolean logined = loginHelper.restoreSesstion();
        return true;
    }

    boolean checkFacebookAccountAndLogin(Context con, Account account) {
        boolean logined = loginHelper.restoreSesstion();
        if (logined == false) {
            launchFacebookLogin();
        }
        return true;
    }

    protected void launchFacebookLogin() {
        basichandler.obtainMessage(FACEBOOK_LOGIN_MSG).sendToTarget();
    }

    protected void reLaunchFacebookLogin() {
        basichandler.obtainMessage(FACEBOOK_LOGIN_MSG).sendToTarget();
    }

    protected void stopProcess() {

    }

    public static void formatFacebookIntent(Intent intent, SocialORM orm)
    {
        SocialORM.Account ac = orm.getFacebookAccount();
        intent.putExtra("forfacebook", true);
        intent.putExtra("email", ac.email);
        intent.putExtra("password", ac.password);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo i = (AdapterView.AdapterContextMenuInfo) menuInfo;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.facebook_context, menu);

        contextMenu = menu;
        // for copy context menu

        // set title
        if (SNSItemView.class.isInstance(i.targetView)) 
        {
            SNSItemView snsView = (SNSItemView) i.targetView;
            if (isEmpty(snsView.getText()) == false) 
            {
                contextMenu.findItem(R.id.facebook_menu_copy).setVisible(true);
            }
            else 
            {
                contextMenu.findItem(R.id.facebook_menu_copy).setVisible(false);
            }

            // find whether have link, if have, open in browser
            List<String> links = snsView.getLinks();
            if (links != null && links.size() > 0) 
            {
                menu.findItem(R.id.facebook_menu_open_browser).setVisible(true);
            } 
            else 
            {
                menu.findItem(R.id.facebook_menu_open_browser).setVisible(false);
            }

        }

        // set title

        if (FacebookMessageActivity.class.isInstance(this)) 
        {
            contextMenu.findItem(R.id.facebook_menu_reply).setVisible(false);

            if (MessageThreadInfoItemView.class.isInstance(i.targetView)) 
            {
            	MessageThreadInfoItemView view = (MessageThreadInfoItemView) i.targetView;
                contextMenu.setHeaderTitle(view.getUserName());
                
                if(view.isUpdate())
                {
                	contextMenu.findItem(R.id.facebook_menu_reply).setVisible(false);
                }
            }
        } 
        else if (FacebookMailDetailActivity.class.isInstance(this)) 
        {
            contextMenu.findItem(R.id.facebook_menu_reply).setVisible(false);
            contextMenu.findItem(R.id.facebook_menu_forward).setVisible(true);

            if (FacebookMailItemView.class.isInstance(i.targetView)) {
                FacebookMailItemView view = (FacebookMailItemView) i.targetView;
                contextMenu.setHeaderTitle(view.getUserName());
            }
        } 
        else if (FacebookMainActivity.class.isInstance(this)) 
        {
            contextMenu.findItem(R.id.facebook_menu_wall_post).setVisible(true);

            if (MessageItemView.class.isInstance(i.targetView)) {
                MessageItemView view = (MessageItemView) i.targetView;
                boolean isMyFriend = orm.isExistedFacebookUser(view.getFromUID());
                long inputid = ((FacebookMainActivity) this).inputuid;
                contextMenu.setHeaderTitle(view.getUserName());
                if (perm_session != null && ((inputid == view.getFromUID()) || (!isMyFriend && inputid != view.getFromUID()))) {
                    contextMenu.findItem(R.id.facebook_menu_wall_to_wall).setVisible(false);
                } else {
                    contextMenu.findItem(R.id.facebook_menu_wall_to_wall).setVisible(true);
                }
            }
        } 
        else if (FacebookPhonebookActivity.class.isInstance(this)) 
        {
            if (FacebookPhoneBookItemView.class.isInstance(i.targetView)) 
            {
                FacebookPhoneBookItemView view = (FacebookPhoneBookItemView) i.targetView;
                PhoneBook phone = view.getPhoneBook();
                if (isEmpty(phone.cell) == false) 
                {
                    contextMenu.findItem(R.id.facebook_menu_cell).setVisible(true);
                    contextMenu.findItem(R.id.facebook_menu_cell).setTitle(getString(R.string.phonebook_call) + " " + phone.cell);
                    contextMenu.findItem(R.id.facebook_menu_cell).setIcon(R.drawable.sym_action_call);
                }

                if (isEmpty(phone.phone) == false) 
                {
                    contextMenu.findItem(R.id.facebook_menu_phone).setVisible(true);
                    contextMenu.findItem(R.id.facebook_menu_phone).setTitle(getString(R.string.phonebook_call) + " " + phone.phone);
                    contextMenu.findItem(R.id.facebook_menu_phone).setIcon(R.drawable.sym_action_call);
                }

                if (isEmpty(phone.email) == false) 
                {
                    contextMenu.findItem(R.id.facebook_menu_email).setVisible(true);
                    contextMenu.findItem(R.id.facebook_menu_email).setTitle(getString(R.string.phonebook_mail) + " " + phone.email);   
                    contextMenu.findItem(R.id.facebook_menu_email).setIcon(R.drawable.sym_action_email);
                }

                contextMenu.findItem(R.id.facebook_menu_sync_to_contact).setVisible(true);
                contextMenu.setHeaderTitle(phone.username);

                contextMenu.findItem(R.id.facebook_menu_add_as_shout_cut).setVisible(true);
                contextMenu.findItem(R.id.facebook_con_menu_new_message).setVisible(true);

            }
        } 
        else if (FacebookFriendsActivity.class.isInstance(this) || FacebookNewFriendsActivity.class.isInstance(this)) 
        {
        	if(TextView.class.isInstance(i.targetView) == false)
        	{
	            contextMenu.findItem(R.id.facebook_menu_wall_post).setVisible(true);
	            contextMenu.findItem(R.id.facebook_con_menu_new_message).setVisible(true);
        	}

            if (FacebookFriendItemView.class.isInstance(i.targetView)) 
            {
                FacebookFriendItemView view = (FacebookFriendItemView) i.targetView;
                contextMenu.setHeaderTitle(view.getUser().name);
                contextMenu.findItem(R.id.facebook_menu_add_as_shout_cut).setVisible(true);

                if (view.getUser().isfriend == false) 
                {
                    contextMenu.findItem(R.id.facebook_menu_add_friend).setVisible(true);
                } 
                else
                {
                    contextMenu.findItem(R.id.facebook_menu_add_friend).setVisible(false);
                }
                
                //process for phonebook
                if(view.hasPhonebook)
                {
                	contextMenu.findItem(R.id.facebook_menu_view_phonebook).setVisible(true);
	                if (isEmpty(view.cellStr) == false) 
	                {
	                    contextMenu.findItem(R.id.facebook_menu_cell).setVisible(true);
	                    contextMenu.findItem(R.id.facebook_menu_cell).setTitle(getString(R.string.phonebook_call) + " " + view.cellStr);
	                    contextMenu.findItem(R.id.facebook_menu_cell).setIcon(R.drawable.sym_action_call);
	                }
	
	                if (isEmpty(view.phoneStr) == false) 
	                {
	                    contextMenu.findItem(R.id.facebook_menu_phone).setVisible(true);
	                    contextMenu.findItem(R.id.facebook_menu_phone).setTitle(getString(R.string.phonebook_call) + " " + view.phoneStr);
	                    contextMenu.findItem(R.id.facebook_menu_phone).setIcon(R.drawable.sym_action_call);
	                }
	
	                if (isEmpty(view.emialStr) == false) 
	                {
	                    contextMenu.findItem(R.id.facebook_menu_email).setVisible(true);
	                    contextMenu.findItem(R.id.facebook_menu_email).setTitle(getString(R.string.phonebook_mail) + " " + view.emialStr);   
	                    contextMenu.findItem(R.id.facebook_menu_email).setIcon(R.drawable.sym_action_email);
	                }
                }
            }
        } 
        else if (FacebookFriendsStatusActivity.class.isInstance(this)) 
        {
            contextMenu.findItem(R.id.facebook_menu_wall_post).setVisible(true);
            contextMenu.findItem(R.id.facebook_con_menu_new_message).setVisible(true);

            if (FacebookStatusView.class.isInstance(i.targetView)) {
                FacebookStatusView view = (FacebookStatusView) i.targetView;
                contextMenu.setHeaderTitle(view.getUserName());
            }
        } 
        else if (FacebookGroupDetailsActivity.class.isInstance(this)) 
        {
            if (FacebookFriendItemView.class.isInstance(i.targetView)) {
                FacebookFriendItemView view = (FacebookFriendItemView) i.targetView;
                FacebookUser.SimpleFBUser user = view.getUser();
                if (user.isfriend == false) {
                    contextMenu.findItem(R.id.facebook_menu_request_add_friends).setVisible(true);
                } else {
                    contextMenu.findItem(R.id.facebook_menu_request_add_friends).setVisible(false);
                }
                contextMenu.findItem(R.id.facebook_menu_wall_post).setVisible(true);

                contextMenu.setHeaderTitle(user.name);
            }
        } 
        else if (FacebookFindFriendsActivity.class.isInstance(this)) 
        {
            contextMenu.findItem(R.id.facebook_menu_request_phone_number).setVisible(true);

            if (FacebookFindFriendItemView.class.isInstance(i.targetView)) {
                FacebookFindFriendItemView view = (FacebookFindFriendItemView) i.targetView;
                contextMenu.setHeaderTitle(view.getPersonName());

                if (view.isFriend() == false) {
                    contextMenu.findItem(R.id.facebook_menu_request_add_friends).setVisible(true);
                } else {
                    contextMenu.findItem(R.id.facebook_menu_request_add_friends).setVisible(false);
                }
            }
        } 
        else if (FacebookPokeActivity.class.isInstance(this)) 
        {
            contextMenu.findItem(R.id.facebook_menu_poke_back).setVisible(false);
        } 
        else if (FacebookEventActivity.class.isInstance(this)) 
        {
            if(FacebookEventItemView.class.isInstance(i.targetView))
            {
                FacebookEventItemView view = (FacebookEventItemView) i.targetView;
                contextMenu.setHeaderTitle(view.getEvent().getName());
                if (perm_session != null) {
                    if (view.getEvent().creator == perm_session.getLogerInUserID()) {
                        contextMenu.findItem(R.id.facebook_event_cancel).setVisible(true);
                    } else {
                        contextMenu.findItem(R.id.facebook_event_cancel).setVisible(false);
                    }
                }
            }
        } 
        else if (FacebookCommentsActivity.class.isInstance(this)) 
        {
            // remove comment
            CommentItemView view = (CommentItemView) i.targetView;
            //contextMenu.setHeaderTitle(R.string.facebook_remove_comment_title);
            if (perm_session != null) 
            {
                if (view.getComment().fromid == perm_session.getLogerInUserID() || 
                    view.getComment().parentsuid == perm_session.getLogerInUserID()) 
                {
                    contextMenu.findItem(R.id.facebook_menu_delete).setVisible(true);
                } 
                else 
                {
                    contextMenu.findItem(R.id.facebook_menu_delete).setVisible(false);
                }
            }
        } 
        else if (FacebookNotificationManActivity.class.isInstance(this)) 
        {
            if (NotesItemView.class.isInstance(i.targetView)) {
                String href = ((NotesItemView) (i.targetView)).getNotes().href;
                if (isEmpty(href) == false) {
                    contextMenu.findItem(R.id.facebook_menu_open_browser).setVisible(true);
                }
            }
        } 
        else if (FacebookAlbumViewActivity.class.isInstance(this)) 
        {
            if (AlbumItemView.class.isInstance(i.targetView)) {
                contextMenu.findItem(R.id.facebook_menu_photo_comments).setVisible(true);
                AlbumItemView av = (AlbumItemView) i.targetView;
                if (av.photo.owner == perm_session.getLogerInUserID()) {
                    contextMenu.findItem(R.id.facebook_con_menu_photo_delete).setVisible(true);
                }
            }
        } 
        else if (FacebookAccountActivity.class.isInstance(this)) 
        {
            if (FacebookAlbumItemView.class.isInstance(i.targetView)) {
                FacebookAlbumItemView fa = (FacebookAlbumItemView) i.targetView;
                // just in owner context menu
                if (fa.getPhotoAlbum().owner == perm_session.getLogerInUserID()) {
                    contextMenu.findItem(R.id.facebook_menu_photo_comments).setVisible(true);
                    contextMenu.findItem(R.id.facebook_menu_photo_comments).setTitle(R.string.album_context_menu_edit);

                    contextMenu.findItem(R.id.facebook_con_menu_album_delete).setVisible(true);
                }
            }
        } 
        else if (FacebookAlbumActivity.class.isInstance(this)) 
        {
            if (FacebookAlbumItemView.class.isInstance(i.targetView)) {
                FacebookAlbumItemView fa = (FacebookAlbumItemView) i.targetView;

                // just in owner context menu
                if (fa.getPhotoAlbum().owner == perm_session.getLogerInUserID()) {
                    contextMenu.findItem(R.id.facebook_menu_photo_comments).setVisible(true);
                    contextMenu.findItem(R.id.facebook_menu_photo_comments).setTitle(R.string.album_context_menu_edit);

                    contextMenu.findItem(R.id.facebook_con_menu_album_delete).setVisible(true);
                }
            }
        }
        else if (FacebookProfileActivity.class.isInstance(this)) 
        {
            if (FacebookProfileActivity.ProfileItemView.class
                    .isInstance(i.targetView)) {
                FacebookProfileActivity.ProfileItemView pv = (FacebookProfileActivity.ProfileItemView) i.targetView;
                contextMenu.setHeaderTitle(pv.getUser().name);
                contextMenu.findItem(R.id.facebook_con_menu_shortcut_delete).setVisible(true);
                contextMenu.findItem(R.id.facebook_con_menu_shortcut_delete_all).setVisible(true);                
                contextMenu.findItem(R.id.facebook_con_menu_shortcut_delete).setTitle(R.string.context_short_cut_delete);
                contextMenu.findItem(R.id.facebook_menu_wall_post).setVisible(true);
                contextMenu.findItem(R.id.facebook_con_menu_new_message).setVisible(true);
            }
        }
        else if(FacebookNotesActivity.class.isInstance(this))
        {
            contextMenu.findItem(R.id.facebook_menu_note_edit).setVisible(true);
        }
    }
    
    protected void process(Uri uri)
    {
        boolean openinBrowser = true;
        if(isProfile(uri.toString()))
        {
            openinBrowser = false;
            Log.d(TAG, "open profile="+uri.toString());
            //launch profile
            //get id
            String id = uri.getQueryParameter("id");
            Intent intent = new Intent(mContext, FacebookAccountActivity.class);
            FacebookUser.SimpleFBUser user = orm.getSimpleFacebookUser(Long.valueOf(id));                                   
            if(user != null)
            {
                intent.putExtra("uid",      user.uid);
                intent.putExtra("username", user.name);
                intent.putExtra("imageurl", user.pic_square);               
            }
            else
            {
                intent.putExtra("uid",      Long.valueOf(id));
                intent.putExtra("username", id);                
            }                                   
            ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
            
        }
        else if(isPhoto(uri.toString()))
        {
            //launch photo                                  
            String owner = uri.getQueryParameter("id");
            String pid = uri.getQueryParameter("pid");
            String aid = uri.getQueryParameter("aid");
            
            Log.d(TAG, "owner="+owner + " pid="+pid + " aid="+aid);
            if(isEmpty(pid) == false)
            {  
                 String mergedpid = FacebookSession.mergePID_UID(pid,owner);
                
                 Log.d(TAG, "open photo="+uri.toString() + "owner="+owner + " pid="+pid + " aid="+aid + "mergedpid="+mergedpid);
                 openinBrowser = false;
                 Intent intent = new Intent(mContext,FacebookPhotoCommentsActivity.class);
                 intent.putExtra("forimageview", true);
                 intent.putExtra("fromoutside",  true);
                 intent.putExtra("pid", mergedpid);
                 intent.putExtra("owner",Long.parseLong(owner));
                 
                 startActivity(intent);                     
            }
            else if(isEmpty(aid) == false)
            {
                 
                 String mergedaid = FacebookSession.mergePID_UID(aid,owner);
                 Log.d(TAG, "open album="+uri.toString() + "owner="+owner + " pid="+pid + " aid="+aid +" mergedaid="+mergedaid);
                 //merged aid & uid
                 openinBrowser = false;
                 Intent intent = new Intent(mContext, FacebookAlbumViewActivity.class); 
                 intent.putExtra("albumid", mergedaid); 
                 intent.putExtra("owner", Long.parseLong(owner));
                 intent.putExtra("fromoutside",  true);
                 mContext.startActivity(intent);                        
            }                                   
        }
        
        if(openinBrowser == true)
        {                               
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
            formatFacebookIntent(intent, orm);                               
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            try{
            mContext.startActivity(intent);
            }catch(Exception ne){}
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) 
    {
        AdapterView.AdapterContextMenuInfo i = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) 
        {
	        case R.id.facebook_menu_open_browser: 
	        {
	            if (NotesItemView.class.isInstance(i.targetView)) 
	            {
	                String href = ((NotesItemView) (i.targetView)).getNotes().href;
	                if (isEmpty(href) == false) 
	                {
	                    Intent intent = new Intent(Intent.ACTION_VIEW);
	                    intent.setData(Uri.parse(href));
	                    formatFacebookIntent(intent, orm);
	                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                    startActivity(intent);
	                }
	            } 
	            else 
	            {
	                if (SNSItemView.class.isInstance(i.targetView)) 
	                {
	                    SNSItemView view = (SNSItemView) i.targetView;
	                    List<String> links = view.getLinks();
	
	                    for (int j = 0; j < links.size(); j++) {
	                        Intent intent = new Intent(Intent.ACTION_VIEW);
	                        intent.setData(Uri.parse(links.get(j)));
	                        formatFacebookIntent(intent, orm);
	                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                        startActivity(intent);
	                    }
	                }
	            }
	            break;
	        }
	        case R.id.facebook_menu_copy: 
	        {
	            if (SNSItemView.class.isInstance(i.targetView)) {
	                String content = ((SNSItemView) (i.targetView)).getText();
	                doCopy(content);
	            }
	            break;
	        }
	        case R.id.facebook_menu_forward: 
	        {
	            if (MessageThreadInfoItemView.class.isInstance(i.targetView)) {
	                MessageThreadInfoItemView v = (MessageThreadInfoItemView) i.targetView;
	                doMailForward(v.getThread());
	            }
	            if (FacebookMailItemView.class.isInstance(i.targetView)) {
	                FacebookMailItemView v = (FacebookMailItemView) i.targetView;
	                doMailForward(v.getMessage());
	            }
	            break;
	        }
	        case R.id.facebook_menu_reply: 
	        {
	            if (MessageThreadInfoItemView.class.isInstance(i.targetView)) {
	                MessageThreadInfoItemView v = (MessageThreadInfoItemView) i.targetView;
	                doMailReply(v.getThread());
	            }
	
	            if (FacebookMailItemView.class.isInstance(i.targetView)) {
	                FacebookMailItemView v = (FacebookMailItemView) i.targetView;
	                doMailReply(v.getMessage());
	            }
	            break;
	        }
	        case R.id.facebook_menu_wall_post: 
	        {
	            // get the post to UID
	            if (MessageItemView.class.isInstance(i.targetView)) {
	                MessageItemView view = (MessageItemView) i.targetView;
	                postToWall(view.getFromUID());
	            } else if (FacebookFriendItemView.class.isInstance(i.targetView)) {
	                FacebookFriendItemView view = (FacebookFriendItemView) i.targetView;
	                postToWall(view.getUser().uid);
	            } else if (FacebookStatusView.class.isInstance(i.targetView)) {
	                FacebookStatusView view = (FacebookStatusView) i.targetView;
	                postToWall(view.getUserID());
	            } else if (FacebookProfileActivity.ProfileItemView.class.isInstance(i.targetView)) {
	                FacebookProfileActivity.ProfileItemView view = (FacebookProfileActivity.ProfileItemView) i.targetView;
	                postToWall(view.getUser().uid);
	            }
	            break;
	        }
	        case R.id.facebook_menu_add_friend: 
	        {
	            if (FacebookFriendItemView.class.isInstance(i.targetView)) {
	                FacebookFriendItemView view = (FacebookFriendItemView) i.targetView;
	                addAsFriends(view.getUser().uid);
	            }
	            break;
	        }
	        case R.id.facebook_menu_add_as_shout_cut: 
	        {
	            if (FacebookFriendItemView.class.isInstance(i.targetView)) {
	                FacebookFriendItemView view = (FacebookFriendItemView) i.targetView;
	                addshortCut(view.getUser());
	            } else if (FacebookPhoneBookItemView.class.isInstance(i.targetView)) {
	                FacebookPhoneBookItemView view = (FacebookPhoneBookItemView) i.targetView;
	                PhoneBook phone = view.getPhoneBook();
	                FacebookUser user = new FacebookUser();
	                user.uid = phone.uid;
	                user.isShoutcut = true;
	                addshortCut(user);
	            }
	        }
	        case R.id.facebook_menu_request_phone_number: 
	        {
	            if (FacebookFindFriendItemView.class.isInstance(i.targetView)) {
	                FacebookFindFriendItemView view = (FacebookFindFriendItemView) i.targetView;
	                requestPhoneNumber(view.getFUID());
	            }
	            break;
	        }
	        case R.id.facebook_menu_request_add_friends: 
	        {
	            if (FacebookFindFriendItemView.class.isInstance(i.targetView)) {
	                FacebookFindFriendItemView view = (FacebookFindFriendItemView) i.targetView;
	                if (FacebookFindFriendsActivity.class.isInstance(this)) {
	                    FacebookFindFriendsActivity findA = (FacebookFindFriendsActivity) this;
	                    findA.requestFrinds(view.getFUID());
	                    findA.requestedUser(view.getUser().uid,  view.getUser().peopleid);
	                }
	            }
	            break;
	        }
	        case R.id.facebook_menu_view_phonebook:
	        {
	        	if(FacebookFriendItemView.class.isInstance(i.targetView))
	        	{
	        		FacebookFriendItemView fv= (FacebookFriendItemView)i.targetView;
				    Intent intent = new Intent(mContext, FacebookPhonebookDetailActivity.class);
				    intent.putExtra("phonebookid",       fv.getUID());								
				    startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_PHONRBOOK_DETAIL);
	        	}
	        	break;
	        }
	        case R.id.facebook_menu_cell: 
	        {
	            if (FacebookPhoneBookItemView.class.isInstance(i.targetView)) 
	            {
	                FacebookPhoneBookItemView view = (FacebookPhoneBookItemView) i.targetView;
	                loadCallCell(view.getPhoneBook().cell);
	            }
	            else if(FacebookFriendItemView.class.isInstance(i.targetView))
	            {
	            	FacebookFriendItemView view = (FacebookFriendItemView)i.targetView;
	            	loadCallCell(view.cellStr);
	            }
	            break;
	        }	        
	        case R.id.facebook_menu_phone: 
	        {
	            if (FacebookPhoneBookItemView.class.isInstance(i.targetView)) 
	            {
	                FacebookPhoneBookItemView view = (FacebookPhoneBookItemView) i.targetView;
	                loadCallCell(view.getPhoneBook().phone);
	            }
	            else if(FacebookFriendItemView.class.isInstance(i.targetView))
	            {
	            	FacebookFriendItemView view = (FacebookFriendItemView)i.targetView;
	            	loadCallCell(view.phoneStr);
	            }
	            break;
	        }
	        case R.id.facebook_menu_email: 
	        {
	            if (FacebookPhoneBookItemView.class.isInstance(i.targetView)) 
	            {
	                FacebookPhoneBookItemView view = (FacebookPhoneBookItemView) i.targetView;
	                loadSendEmail(view.getPhoneBook().email);
	            }
	            else if(FacebookFriendItemView.class.isInstance(i.targetView))
	            {
	            	FacebookFriendItemView view = (FacebookFriendItemView)i.targetView;
	            	loadSendEmail(view.emialStr);
	            }
	
	            break;
	        }
	        case R.id.facebook_menu_sync_to_contact: 
	        {
	            if (FacebookPhoneBookItemView.class.isInstance(i.targetView)) 
	            {
	                FacebookPhoneBookItemView view = (FacebookPhoneBookItemView) i.targetView;	                
	                syncToContact(view.getPhoneBook());
	            }
	            break;
	        }
	        case R.id.facebook_menu_poke_back: 
	        {
	            break;
	        }
	        case R.id.facebook_event_cancel: 
	        {
	            if (FacebookEventItemView.class.isInstance(i.targetView)) {
	                FacebookEventItemView view = (FacebookEventItemView) i.targetView;
	                doCancel(view.getEvent());
	            }
	            break;
	        }
	        case R.id.facebook_menu_delete: 
	        {
	            if (CommentItemView.class.isInstance(i.targetView)) {
	                CommentItemView view = (CommentItemView) i.targetView;
	                Message msg = basichandler.obtainMessage(FACEBOOK_REMOVE_COMMENT);
	                msg.getData().putString("comment_id", view.getComment().id);
	                msg.sendToTarget();
	            }
	            break;
	        }
	        case R.id.facebook_menu_wall_to_wall:
	        {
	            // see wall to wall message wall to wall message
	            if (MessageItemView.class.isInstance(i.targetView)) {
	                MessageItemView view = (MessageItemView) i.targetView;
	                toWallToWallMessage(view.getWall().fromid, view.getWall().fromusername);
	            }
	            break;
	        }
	        case R.id.facebook_menu_photo_comments: 
	        {
	            if (AlbumItemView.class.isInstance(i.targetView))// for photo
	            {
	                AlbumItemView av = (AlbumItemView) i.targetView;
	                addComments(av.photo, null);
	            } else if (FacebookAlbumItemView.class.isInstance(i.targetView))// for album
	            {
	                FacebookAlbumItemView av = (FacebookAlbumItemView) i.targetView;
	                addComments(null, av.getPhotoAlbum());
	            }
	            break;
	        }
	        case R.id.facebook_con_menu_album_delete: 
	        {
	            if (FacebookAlbumItemView.class.isInstance(i.targetView))// for album
	            {
	                FacebookAlbumItemView av = (FacebookAlbumItemView) i.targetView;
	                deleteAlbum(av.getPhotoAlbum().aid);
	            }
	            break;
	        }
	        case R.id.facebook_con_menu_photo_delete: 
	        {
	            if (AlbumItemView.class.isInstance(i.targetView))// for photo
	            {
	                AlbumItemView av = (AlbumItemView) i.targetView;
	                deletePhoto(av.photo);
	            }
	            break;
	        }
	        case R.id.facebook_con_menu_shortcut_delete: 
	        {
	            if (FacebookProfileActivity.class.isInstance(this)) {
	                if (FacebookProfileActivity.ProfileItemView.class.isInstance(i.targetView)) {
	                    FacebookProfileActivity.ProfileItemView pv = (FacebookProfileActivity.ProfileItemView) i.targetView;
	                    FacebookUser.SimpleFBUser user = pv.getUser();
	                    user.isShoutcut = false;
	
	                    orm.updateFacebookUserShortCut(user, false);
	                }
	                FacebookProfileActivity pa = (FacebookProfileActivity) this;
	                pa.refreshShortCut();
	            }
	            break;
	        }
	        case R.id.facebook_con_menu_shortcut_delete_all:
	        {
	        	if (FacebookProfileActivity.class.isInstance(this)) 
	        	{
	        		orm.deleteFacebookShortCut();
	                FacebookProfileActivity pa = (FacebookProfileActivity) this;
	                pa.refreshShortCut();
	            }
	        	break;
	        }
	        case R.id.facebook_con_menu_new_message: 
	        {
	            if (FacebookProfileActivity.ProfileItemView.class.isInstance(i.targetView)) {
	                FacebookProfileActivity.ProfileItemView pv = (FacebookProfileActivity.ProfileItemView) i.targetView;
	                FacebookUser.SimpleFBUser user = pv.getUser();
	                newMail(user.uid);
	            } else if (MessageItemView.class.isInstance(i.targetView)) {
	                MessageItemView view = (MessageItemView) i.targetView;
	                newMail(view.getFromUID());
	            } else if (FacebookFriendItemView.class.isInstance(i.targetView)) {
	                FacebookFriendItemView view = (FacebookFriendItemView) i.targetView;
	                newMail(view.getUser().uid);
	            } else if (FacebookStatusView.class.isInstance(i.targetView)) {
	                FacebookStatusView view = (FacebookStatusView) i.targetView;
	                newMail(view.getUserID());
	            } else if (FacebookPhoneBookItemView.class.isInstance(i.targetView)) {
	                FacebookPhoneBookItemView view = (FacebookPhoneBookItemView) i.targetView;
	                PhoneBook phone = view.getPhoneBook();
	                newMail(phone.uid);
	            }
	            break;
	        }
	        case R.id.facebook_menu_note_edit:
	        {
	            if(FacebookNotesActivity.NotesItemView.class.isInstance(i.targetView))
	            {
	                FacebookNotesActivity.NotesItemView view = (FacebookNotesActivity.NotesItemView)i.targetView;
	                Notes note = view.getNotes();
	                if(note != null)
	                {
	                    Intent intent = new Intent(mContext,FacebookNoteEditActivity.class);
	                    intent.putExtra("note_id", note.note_id);
	                    intent.putExtra("content", note.content);
	                    intent.putExtra("title",   note.title);
	                    startActivityForResult(intent, RESULT_OK);
	                }
	            }
	            break;
	        }
        }        
        return true;
    }

    private void addComments(Photo photo, PhotoAlbum album) {
        Intent intent = new Intent(mContext,
                FacebookPhotoCommentsActivity.class);
        intent.putExtra("photo", photo);
        intent.putExtra("album", album);
        intent.putExtra("forcomments", true);
        startActivity(intent);
    }

    protected void toWallToWallMessage(long uid_from, String fromusername) {

    }

    protected void removeComment(String commentid) {

    }

    protected void doCancel(Event event) {

    }

    protected void requestPhoneNumber(long fuid) {

    }

    protected void syncToContact(PhoneBook phonebook) {

    }

    protected void loadCallCell(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    protected void loadSendEmail(String email) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"+ email));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // override in sub-class
    protected void doMailReply(Object thread) {

    }

    protected void doMailForward(Object thread) {

    }

    protected void pageNavgattion(String title) {
        if (optionMenu != null) {
            optionMenu.findItem(R.id.facebook_menu_page_nav).setVisible(true);
            optionMenu.findItem(R.id.facebook_menu_page_nav).setTitle(
                    "Page :" + title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.facebook_option_menu, menu);

        optionMenu = menu;
        return true;
    }

    protected void shareFacebookApp()
    {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");                 
        send.putExtra(Intent.EXTRA_TEXT,  "http://market.android.com/search?q=com.msocial.free");                                  
        
        try {
            startActivity(Intent.createChooser(send, getText(com.android.internal.R.string.sendText)));
        } catch(android.content.ActivityNotFoundException ex) {
            // if no app handles it, do nothing
        }
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (loginHelper.getPermanentSesstion(this) != null) {
            menu.setGroupVisible(R.id.facebook_groupd_nemu, false);
            menu.setGroupVisible(R.id.facebook_groupd_login_nemu, false);
            menu.findItem(R.id.facebook_menu_login).setVisible(false);
        } else {
            menu.setGroupVisible(R.id.facebook_groupd_nemu, false);
            menu.setGroupVisible(R.id.facebook_groupd_login_nemu, true);
            menu.findItem(R.id.facebook_menu_login).setVisible(true);
            menu.findItem(R.id.facebook_menu_settings).setVisible(true);

            menu.setGroupVisible(R.id.facebook_group_about_nemu, true);
            menu.findItem(R.id.facebook_menu_about).setVisible(true);

            return true;
        }

        menu.findItem(R.id.facebook_menu_about).setVisible(false);

        // process main UI activity
        if (FacebookWallToWallActivity.class.isInstance(this)) {
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
        }
        if (FacebookSetContactActivity.class.isInstance(this)) {
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
        }
        
        if (FacebookMainActivity.class.isInstance(this))
        {
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
            menu.findItem(R.id.facebook_menu_status_update).setVisible(true);
            // menu.findItem(R.id.facebook_menu_account).setVisible(true);
            menu.findItem(R.id.facebook_menu_inbox).setVisible(true);
            menu.findItem(R.id.facebook_menu_friend_group).setVisible(true);
            menu.findItem(R.id.facebook_menu_logout).setVisible(true);
            menu.findItem(R.id.facebook_menu_friend_status_update).setVisible(true);
            // menu.findItem(R.id.facebook_menu_phonebook).setVisible(true);
            menu.findItem(R.id.facebook_menu_news_feed).setVisible(true);
            menu.findItem(R.id.facebook_menu_location_update).setVisible(true);

            // menu.findItem(R.id.facebook_menu_notifications).setVisible(true);

            menu.findItem(R.id.facebook_menu_go_mobile_facebook).setVisible(true);

            // menu.setGroupVisible(R.id.facebook_group_about_nemu, true);
            // menu.findItem(R.id.facebook_menu_about).setVisible(true);
        } 
        else if (FacebookNotesActivity.class.isInstance(this)) 
        {
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
            menu.findItem(R.id.facebook_menu_new_note).setVisible(true);   
            menu.findItem(R.id.facebook_menu_clear_cache).setVisible(true);
        } 
        else if(FacebookNoteDetailActivity.class.isInstance(this))
        {
            menu.findItem(R.id.facebook_menu_note_edit).setVisible(true); 
        }
        else if (FacebookProfileActivity.class.isInstance(this)) 
        {
            menu.findItem(R.id.facebook_menu_share_app).setVisible(true);
            menu.findItem(R.id.facebook_menu_logout).setVisible(true);
            menu.findItem(R.id.facebook_menu_status_update).setVisible(false);
            menu.findItem(R.id.facebook_menu_go_mobile_facebook).setVisible(true);
            menu.setGroupVisible(R.id.facebook_group_about_nemu, true);
            menu.findItem(R.id.facebook_menu_settings).setVisible(true);
            menu.findItem(R.id.facebook_menu_status_update).setVisible(true);
        } 
        else if (FacebookAccountActivity.class.isInstance(this)) 
        {
            menu.findItem(R.id.facebook_menu_new_mail).setVisible(true);
            // just yourself can see this menus
            if (((FacebookAccountActivity) (this)).comefrommyself) 
            {
                menu.findItem(R.id.facebook_menu_settings).setVisible(false);
                //menu.findItem(R.id.facebook_menu_status_update).setVisible(true);
                menu.findItem(R.id.facebook_menu_logout).setVisible(false);

                // move from main menu
                menu.findItem(R.id.facebook_menu_event_sync).setVisible(false);
                menu.findItem(R.id.facebook_menu_ext_permission).setVisible(
                        false);
                menu.findItem(R.id.facebook_menu_friends).setVisible(false);
                menu.findItem(R.id.facebook_menu_find_friends).setVisible(true);
                menu.findItem(R.id.facebook_menu_phonebook).setVisible(false);
                menu.findItem(R.id.facebook_menu_set_contact).setVisible(true);
                menu.findItem(R.id.facebook_menu_friend_group).setTitle(R.string.facebook_groups_title);
                menu.findItem(R.id.facebook_menu_friend_group).setVisible(true);

                // menu.findItem(R.id.facebook_menu_test).setVisible(true);
                menu.findItem(R.id.sns_task_management_menu).setVisible(false);
                // menu.findItem(R.id.facebook_menu_add_as_friend).setVisible(false);
                menu.findItem(R.id.facebook_menu_photo).setVisible(false);

            } 
            else 
            {
                menu.findItem(R.id.facebook_menu_refresh).setVisible(false);
                menu.findItem(R.id.facebook_menu_friend_group).setTitle(R.string.facebook_groups_title);
                menu.findItem(R.id.facebook_menu_friend_group).setVisible(true);

                boolean isFriend = ((FacebookAccountActivity) this).isFriend();
                if (isFriend) 
                {
                	// if is friend show wall menu ,if not don't show wall menu
                    menu.findItem(R.id.facebook_menu_wall).setVisible(false); 
                } 
                else 
                {
                    menu.findItem(R.id.facebook_menu_wall).setVisible(false);
                }
                menu.findItem(R.id.facebook_menu_photo).setVisible(false);

                FacebookAccountActivity fa = (FacebookAccountActivity) this;               
                if (fa.isFriend()) 
                {
                    menu.findItem(R.id.facebook_menu_send_notification).setVisible(false);
                    menu.findItem(R.id.facebook_menu_post_to_wall).setVisible(false);
                    // menu.findItem(R.id.facebook_menu_add_as_friend).setVisible(false);
                    if(fa.isPage() == false)
                    {
                        menu.findItem(R.id.facebook_menu_poke).setVisible(true);
                    }
                } 
                else 
                {
                    menu.findItem(R.id.facebook_menu_send_notification).setVisible(false);
                    menu.findItem(R.id.facebook_menu_post_to_wall).setVisible(false);
                    // menu.findItem(R.id.facebook_menu_add_as_friend).setVisible(true);
                }
                // TODO
                // If future, we can get our friends' friends, we can user this
                // menu
                //menu.findItem(R.id.facebook_menu_friends).setTitle("Friends");
                //menu.findItem(R.id.facebook_menu_friends).setVisible(true);
            }
            
            //no title action now, so need refresh
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
            
            if (this.isFromTabView() == true) {
                menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
            }

        } 
        else if (FacebookGroupActivity.class.isInstance(this) || (FacebookGroupDetailsActivity.class.isInstance(this))) 
        {
            if (hasMore()) {
                menu.findItem(R.id.facebook_menu_next_page).setVisible(true);
            } else {
                menu.findItem(R.id.facebook_menu_next_page).setVisible(false);
            }

            if (isTheFirst() == false) {
                menu.findItem(R.id.facebook_menu_pre_page).setVisible(true);
            } else {
                menu.findItem(R.id.facebook_menu_pre_page).setVisible(false);
            }

            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
        } 
        else if (FacebookMessageActivity.class.isInstance(this)) 
        {
        	/*
        	 * use tab control, no need option menu
        	menu.findItem(R.id.facebook_menu_inbox).setVisible(true);
        	menu.findItem(R.id.facebook_menu_sent).setVisible(true);
        	menu.findItem(R.id.facebook_menu_update).setVisible(true);
        	
        	int type = ((FacebookMessageActivity)this).getType(); 
        	switch(type)
        	{
	        	case 0:
	        	{
	        		menu.findItem(R.id.facebook_menu_inbox).setVisible(false);
	        		break;
	        	}
	        	case 1:
	        	{
	        		menu.findItem(R.id.facebook_menu_sent).setVisible(false);
	        		break;
	        	}
	        	case 2:
	        	{
	        		menu.findItem(R.id.facebook_menu_update).setVisible(false);
	        		break;
	        	}
        	}
        	*/
        	
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
            menu.findItem(R.id.facebook_menu_new_mail).setVisible(true);
            menu.findItem(R.id.facebook_menu_clear_cache).setVisible(true);
        } 
        else if (FacebookStatusUpdateActivity.class.isInstance(this)) 
        {
            if (((FacebookStatusUpdateActivity)(this)).isPostToWall()) 
            {
                menu.findItem(R.id.facebook_menu_insert_img).setVisible(false);
                menu.findItem(R.id.facebook_menu_capture_photo).setVisible(false);
                menu.findItem(R.id.facebook_menu_insert_video).setVisible(false);
                menu.findItem(R.id.facebook_menu_insert_link).setVisible(false);
            } 
            else 
            {
                menu.findItem(R.id.facebook_menu_insert_img).setVisible(true);
                menu.findItem(R.id.facebook_menu_capture_photo).setVisible(true);
                menu.findItem(R.id.facebook_menu_insert_video).setVisible(true);
                menu.findItem(R.id.facebook_menu_insert_link).setVisible(true);
            }

        } 
        else if (FacebookFriendsActivity.class.isInstance(this)) 
        {
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
            menu.findItem(R.id.facebook_menu_phonebook).setVisible(true);
        } 
        else if (FacebookPhonebookActivity.class.isInstance(this)) 
        {
            // menu.findItem(R.id.facebook_menu_contact_sync).setVisible(true);
            if (fromtabview == true) 
            {
                menu.findItem(R.id.facebook_menu_sync_phonebook_to_addressbook).setVisible(true);
            } 
            else 
            {
                menu.findItem(R.id.facebook_menu_sync_phonebook_to_addressbook).setVisible(true);
            }            
            menu.findItem(R.id.facebook_menu_add_phonebook_to_contact).setVisible(false);
            menu.findItem(R.id.facebook_menu_lookupall_contact).setVisible(true);
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
        }
        else if (FacebookFindFriendsActivity.class.isInstance(this)) 
        {
            //menu.findItem(R.id.facebook_menu_lookup_contact).setVisible(true);
        } 
        else if (FacebookStreamActivity.class.isInstance(this)) 
        {
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
            menu.findItem(R.id.facebook_menu_stream_publish).setVisible(true);
            menu.findItem(R.id.facebook_menu_stream_filter).setVisible(true);

        } 
        else if (FacebookLocationUpdateActivity.class.isInstance(this)) 
        {
            menu.findItem(R.id.facebook_menu_request_location).setVisible(true);

            FacebookLocationUpdateActivity flu = (FacebookLocationUpdateActivity) this;
            if (flu.isRequested()) {
                menu.findItem(R.id.facebook_menu_request_location).setTitle(
                        R.string.facebook_location_stop);
            } else {
                menu.findItem(R.id.facebook_menu_request_location).setTitle(
                        R.string.facebook_location_start);
            }

        } 
        else if (FacebookEventActivity.class.isInstance(this)) 
        {
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
            menu.findItem(R.id.facebook_menu_friend_birthday).setVisible(true);
            //menu.findItem(R.id.facebook_menu_add_event).setVisible(true);
            menu.findItem(R.id.facebook_menu_clear_cache).setVisible(true);
            //menu.findItem(R.id.facebook_menu_sync_event).setVisible(true);
            menu.findItem(R.id.facebook_menu_event_switch).setVisible(true);
            // menu.findItem(R.id.facebook_menu_stop).setVisible(true);
        } 
        else if (FacebookFindFriendsActivity.class.isInstance(this)) 
        {
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
        } 
        else if (FacebookNotificationsActivity.class.isInstance(this)) 
        {
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
        }
        else if (FacebookAlbumActivity.class.isInstance(this)) 
        {
            boolean isMyself = ((FacebookAlbumActivity) this).isMySelf();
            if (isMyself) {
                menu.findItem(R.id.facebook_menu_add_photo).setVisible(true);

            } else {
                menu.findItem(R.id.facebook_menu_add_photo).setVisible(false);

            }

            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
        } 
        else if (FacebookAlbumViewActivity.class.isInstance(this)) 
        {
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
            FacebookAlbumViewActivity act = (FacebookAlbumViewActivity) this;
            if (act.isOwner()) {
                menu.findItem(R.id.facebook_menu_album_delete).setVisible(true);
                menu.findItem(R.id.facebook_menu_edit_album).setVisible(true);
                menu.findItem(R.id.facebook_menu_add_photo).setVisible(true);
            } else {
                menu.findItem(R.id.facebook_menu_album_delete)
                        .setVisible(false);
                menu.findItem(R.id.facebook_menu_edit_album).setVisible(false);
            }
            
            if(act.isFromOutSide() == true && act.getOwnerID()>0)
            {
                menu.findItem(R.id.facebook_menu_goto_albumlist).setVisible(true);
            }
            else
            {
                menu.findItem(R.id.facebook_menu_goto_albumlist).setVisible(false);
            }

        }
        else if (FacebookMailDetailActivity.class.isInstance(this)) 
        {
            menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
            FacebookMailDetailActivity ss = (FacebookMailDetailActivity)this;
            if(ss.isFromInbox())
            {
                menu.findItem(R.id.facebook_menu_mail_reply).setVisible(false);
            }
        }
        else if(FacebookPhotoCommentsActivity.class.isInstance(this))
        {
            FacebookPhotoCommentsActivity fpc = (FacebookPhotoCommentsActivity)this;
            if(fpc.isFromOutSide() == true && isEmpty(fpc.getAlbumID()) == false)
            {
                menu.findItem(R.id.facebook_menu_goto_album).setVisible(true);
            }
            else
            {
                menu.findItem(R.id.facebook_menu_goto_album).setVisible(false);                
            }
        }       
        
        else if(FacebookPhonebookDetailActivity.class.isInstance(this))
        {
        	menu.findItem(R.id.facebook_menu_sync_phonebook_to_addressbook).setVisible(true);
        }
        else if(FacebookNewFriendsActivity.class.isInstance(this))
        {
        	menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
        	menu.findItem(R.id.facebook_menu_phonebook).setVisible(true);
        	menu.findItem(R.id.facebook_menu_sync_addressbook).setVisible(true);        	
        }
        //attention, is have more than one option menu, please move out from follow, 
        //or it will has bug
        else if(FacebookEventDetailActivity.class.isInstance(this) ||
        		FacebookCommentsActivity.class.isInstance(this)   ||
        		FacebookRequestProcessActivity.class.isInstance(this) ||
        		FacebookPageActivity.class.isInstance(this) ||
        		FacebookEventGuestActivity.class.isInstance(this) ||
        		FacebookPhotoCommentsViewActivity.class.isInstance(this) ||
        		FacebookNotificationManActivity.class.isInstance(this) || 
        		FacebookPokeActivity.class.isInstance(this) ||
        		FacebookFriendsStatusActivity.class.isInstance(this))
        		
        {
        	menu.findItem(R.id.facebook_menu_refresh).setVisible(true);
        }        
        

        return true;
    }

    // let base activity to do delete,
    protected void deletePhoto(final Photo photo) 
    {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(
        R.string.sns_delete).setMessage(
        String.format(getString(R.string.photo_delete_desc), isEmpty(photo.caption) == true ? "" : (" " + photo.caption))).setPositiveButton(
        getString(R.string.sns_ok),
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Message msg = basichandler.obtainMessage(PHOTO_DELETE);
                msg.getData().putParcelable("photo", photo);
                msg.sendToTarget();
            }
        }).setNegativeButton(getString(R.string.sns_cancel),
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        }).create();
        dialog.show();
    }

    protected void deleteAlbum(final String aid) {
        PhotoAlbum photoAlbum = orm.getAlbum(aid);
        String albumName = " " +getString(R.string.selected_album);
        if(photoAlbum!=null && isEmpty(photoAlbum.name) ==false)
        {
           albumName = " "+photoAlbum.name;
        }
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(
        R.string.sns_delete).setMessage(
        String.format(getString(R.string.album_delete_desc),albumName)).setPositiveButton(
        getString(R.string.sns_ok),
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Message msg = basichandler.obtainMessage(ALBUM_DELETE);
                msg.getData().putString("aid", aid);
                msg.sendToTarget();
            }
        }).setNegativeButton(getString(R.string.sns_cancel),
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).create();
        dialog.show();
    }

    // TODO
    private void deleteAlbumFromFacebook(final String aid) {

        if (existSession() == false) {
            return;
        }

        begin();

        synchronized (mLock) {
            inprocess = true;
        }

        facebookA.deleteAlbumAsync(aid, new FacebookAdapter() {
            @Override
            public void deleteAlbum(boolean suc) {
                Log.d(TAG, "after delete album =" + suc);
                synchronized (mLock) {
                    inprocess = false;
                }
                Message rmsg = basichandler.obtainMessage(ALBUM_DELETE_END);
                rmsg.getData().putString("aid", aid);
                rmsg.getData().putBoolean(RESULT, suc);
                rmsg.sendToTarget();
            }

            @Override
            public void onException(FacebookException e, int method) {
                synchronized (mLock) {
                    inprocess = false;
                }

                Log.d(TAG, "delete album ex=" + e.getMessage());

                if (isInAynscTaskAndStoped()) {
                    Log.d(TAG, "User stop passive");
                } else {
                    Message rmsg = basichandler.obtainMessage(ALBUM_DELETE_END);
                    rmsg.getData().putString("aid", aid);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.sendToTarget();
                }
            }
        });
    }

    private void deletePhotoFromFacebook(final Photo photo) {
        if (existSession() == false) {
            return;
        }

        begin();

        synchronized (mLock) {
            inprocess = true;
        }

        facebookA.deletePhotoAsync(photo.pid, new FacebookAdapter() {
            @Override
            public void deletePhoto(boolean suc) {
                Log.d(TAG, "after delete photo =" + suc);
                synchronized (mLock) {
                    inprocess = false;
                }
                Message rmsg = basichandler.obtainMessage(PHOTO_DELETE_END);
                rmsg.getData().putParcelable("photo", photo);
                rmsg.getData().putBoolean(RESULT, suc);
                rmsg.sendToTarget();
            }

            @Override
            public void onException(FacebookException e, int method) {
                synchronized (mLock) {
                    inprocess = false;
                }

                Log.d(TAG, "delete photo ex=" + e.getMessage());

                if (isInAynscTaskAndStoped()) {
                    Log.d(TAG, "User stop passive");
                } else {
                    Message rmsg = basichandler.obtainMessage(PHOTO_DELETE_END);
                    rmsg.getData().putParcelable("photo", photo);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.sendToTarget();
                }
            }
        });
    }

    protected void onAfterDeleteAlbum(String aid) {

    }

    protected void onAfterdeletePhoto(Photo photo) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
            case R.id.facebook_menu_mail_reply:
            {
            	doMailReply(null);
            	break;
            }
            case R.id.facebook_menu_new_note:
            {
            	createNewNotes();
            	break;
            }
            case R.id.facebook_menu_note_edit:
            {
                editNote();
                break;
            }
	        case R.id.facebook_menu_album_delete: 
	        {
	            if (FacebookAlbumViewActivity.class.isInstance(this)) {
	                FacebookAlbumViewActivity fa = (FacebookAlbumViewActivity) this;
	                deleteAlbum(fa.getAlbum().aid);
	            }
	            break;
	        }
	        case R.id.facebook_menu_photo: 
	        {
	            if (FacebookAccountActivity.class.isInstance(this)) {
	                long uid = ((FacebookAccountActivity) this).getAccountUID();
	                Intent intent = new Intent(FacebookBaseActivity.this, FacebookAlbumActivity.class);
	                intent.putExtra("uid", uid);
	                startActivity(intent);
	            }
	            break;
	        }
	        case R.id.facebook_menu_wall: 
	        {
	            if (FacebookAccountActivity.class.isInstance(this)) {
	                long uid = ((FacebookAccountActivity) this).getAccountUID();
	                Intent intent = new Intent(FacebookBaseActivity.this, FacebookMainActivity.class);
	                intent.putExtra("uid", uid);
	                startActivity(intent);
	            }
	            break;
	        }
	        case R.id.facebook_menu_login: 
	        {
	            checkFacebookAccountAndLogin(this, orm.getFacebookAccount());
	            break;
	        }
	        case R.id.facebook_menu_lookup_contact: 
	        {
	            lookupContact();
	            break;
	        }
	        case R.id.facebook_menu_sync_phonebook_to_addressbook: 
	        {
	        	doAddPhonebookIntoContact();	        	
	            break;
	        }
	        case R.id.facebook_menu_stream_publish: 
	        {
	            doPublishStream();
	            break;
	        }
	        case R.id.facebook_menu_stream_filter:
	        {
	            loadStreamFilter();
	            break;
	        }
	        case R.id.facebook_menu_news_feed: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this, FacebookStreamActivity.class);
	            startActivityForResult(intent, FACEBOOK_NEWS_FEED);
	            break;
	        }
	        case R.id.facebook_menu_friend_status_update: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,
	                    FacebookFriendsStatusActivity.class);
	            startActivityForResult(intent, FACEBOOK_FRIEND_STATUS);
	            break;
	        }
	        case R.id.facebook_menu_refresh: 
	        {
	            loadRefresh();
	            break;
	        }
	        case R.id.facebook_menu_settings: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,FacebookSettingPreference.class);
	            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	            startActivityForResult(intent, FACEBOOK_SETTING);
	            break;
	        }
	        case R.id.facebook_menu_status_update: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,FacebookStatusUpdateActivity.class);
	            startActivityForResult(intent, FACEBOOK_STATUS_UPDATE_UI);
	            break;
	        }
	        case R.id.facebook_menu_inbox: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,FacebookMessageActivity.class);
	            intent.putExtra("type", 0);
	            startActivityForResult(intent, FACEBOOK_MESSAGE_UI);
	            break;
	        }
	        case R.id.facebook_menu_sent:
	        {
	        	Intent intent = new Intent(FacebookBaseActivity.this,FacebookMessageActivity.class);
	            intent.putExtra("type", 1);
	            startActivityForResult(intent, FACEBOOK_MESSAGE_UI);
	        	break;	        	
	        }
	        case R.id.facebook_menu_update:
	        {
	        	Intent intent = new Intent(FacebookBaseActivity.this,FacebookMessageActivity.class);
	            intent.putExtra("type", 2);
	            startActivityForResult(intent, FACEBOOK_MESSAGE_UI);
	        	break;
	        }	        
	        case R.id.facebook_menu_account: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,FacebookAccountActivity.class);
	            if (FacebookMainActivity.class.isInstance(this)) {
	                intent.putExtra("comefrommyself", true);
	            }
	            startActivityForResult(intent, FACEBOOK_ACCOUNT_UI);
	            // startActivity(intent);
	
	            break;
	        }
	        case R.id.facebook_menu_ext_permission: 
	        {
	            break;
	        }
	        case R.id.facebook_menu_next_page: 
	        {
	            nextPage();
	            break;
	        }
	        case R.id.facebook_menu_pre_page: 
	        {
	            prePage();
	            break;
	        }
	        case R.id.facebook_menu_friend_group: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,FacebookGroupActivity.class);
	            if (FacebookAccountActivity.class.isInstance(this)) 
	            {
	                intent.putExtra("justshowhisgroups", true);
	                intent.putExtra("hisuid",  ((FacebookAccountActivity) (this)).uid);
	            }
	            startActivityForResult(intent, FACEBOOK_GROUP_UI);
	            break;
	        }
	        case R.id.facebook_menu_poke:
	        {
	        	if (FacebookAccountActivity.class.isInstance(this)) 
	            {
	        		Message msd = this.basichandler.obtainMessage(POKE_SOMEONE);
	                msd.getData().putLong("pokeuid", ((FacebookAccountActivity) (this)).uid);
	                msd.getData().putString("username", ((FacebookAccountActivity)(this)).username);
	                msd.sendToTarget();
	        		//doPoke(((FacebookAccountActivity) (this)).uid);
	            }
	        	break;
	        }
	        case R.id.facebook_menu_notifications: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this, FacebookNotificationsActivity.class);
	            startActivityForResult(intent, FACEBOOK_NOTIFICATIONS_UI);
	            break;
	        }
	        case R.id.facebook_menu_friends: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this, FacebookFriendsActivity.class);
	            // startActivityForResult(intent, FACEBOOK_FRIENDS_UI);
	            if (FacebookAccountActivity.class.isInstance(this)) {
	                intent.putExtra("hisuid", ((FacebookAccountActivity) (this)).uid);
	            }
	            startActivity(intent);
	            break;
	        }
	        case R.id.facebook_menu_send_notification: 
	        {
	            if (FacebookAccountActivity.class.isInstance(this)) {
	                doNotificationSend();
	            }
	            break;
	        }
	        case R.id.facebook_menu_logout: 
	        {
	            doLogout();
	            break;
	        }
	        case R.id.facebook_menu_share_app:
	        {
	            shareFacebookApp();
	            break;
	        }
	        case R.id.facebook_menu_event_sync: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,FacebookEventActivity.class);
	            startActivityForResult(intent, FACEBOOK_EVENT);
	            break;
	        }
	        case R.id.facebook_menu_phonebook: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,FacebookPhonebookActivity.class);
	            startActivityForResult(intent, FACEBOOK_PHONEBOOK);
	            break;
	        }
	        case R.id.facebook_menu_sync_addressbook:
	        {
	        	syncAddressbook();
	        	break;
	        }
	        case R.id.facebook_menu_find_friends: 
	        {
	            doFindFriends();
	            break;
	        }
	        case R.id.facebook_menu_set_contact: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,FacebookSetContactActivity.class);
	            startActivity(intent);
	            break;
	        }
	        case R.id.facebook_menu_contact_sync: 
	        {
	            doGlobalSyncToContact();
	            break;
	        }
	        case R.id.facebook_menu_add_phonebook_to_contact: 
	        {
	            doAddPhonebookIntoContact();
	            break;
	        }
	        case R.id.facebook_menu_lookupall_contact: 
	        {
	            doLookupallContact();
	            break;
	        }
	        case R.id.facebook_menu_new_mail: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,FacebookMailActivity.class);
	            intent.putExtra("newmail", true);
	            
	            if(FacebookAccountActivity.class.isInstance(this))
	            {
	                intent.putExtra("mailtowho", ((FacebookAccountActivity)this).getUID());
	            }
	            
	            startActivityForResult(intent, FACEBOOK_NEW_MAIL);
	            break;
	        }
	        case R.id.facebook_menu_insert_img: 
	        {
	            Intent intent = new Intent(Intent.ACTION_PICK);
	            intent.setType(com.google.android.mms.ContentType.IMAGE_UNSPECIFIED);
	
	            try {
	                startActivityForResult(intent, STATUS_INSERT_IMG);
	            } catch (android.content.ActivityNotFoundException e) {
	                String message = "Can NOT pick media, mime type: "
	                        + "\nNo Activity found to handle this action.";
	                Log.e(TAG, message, e);
	            }
	            break;
	        }
	        case R.id.facebook_menu_capture_photo: 
	        {
	            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	            if (orm.isFacebookUseOriginalPhoto()) {
	                intent.putExtra("get_uri", true);
	            }
	            startActivityForResult(intent, STATUS_CAPTURE_PHOTO);
	            break;
	        }
	        case R.id.facebook_menu_insert_video: 
	        {
	            Intent intent = new Intent(Intent.ACTION_PICK);
	            intent.setType(com.google.android.mms.ContentType.VIDEO_UNSPECIFIED);
	
	            try {
	                startActivityForResult(intent, STATUS_INSERT_VIDEO);
	            } catch (android.content.ActivityNotFoundException e) {
	                String message = "Can NOT pick media, mime type: "
	                        + "\nNo Activity found to handle this action.";
	                Log.e(TAG, message, e);
	            }
	            break;
	        }
	        case R.id.facebook_menu_insert_link: 
	        {
	            Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
	            PackageManager pm = this.getPackageManager();
	
	            List<ResolveInfo> list = new ArrayList<ResolveInfo>();
	            list.addAll(pm.queryIntentActivities(intent, 0));
	            for (int i = 0; i < list.size(); i++) {
	                ResolveInfo info = list.get(i);
	                String packagename = info.activityInfo.packageName;
	                if (packagename.indexOf("browser") >= 0) {
	                    intent.setComponent(new ComponentName(packagename, info.activityInfo.name));
	                    startActivityForResult(intent, STATUS_LINK_RESULT);
	                    break;
	                }
	            }
	            break;
	        }
	        case R.id.facebook_menu_location_update: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,FacebookLocationUpdateActivity.class);
	            startActivityForResult(intent, FACEBOOK_LOCATION_UPDATE_UI);
	            break;
	        }
	        case R.id.facebook_menu_request_location:
	        {
	            reqeustLoaction();
	            break;
	        }
	        case R.id.facebook_menu_friend_birthday: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,FacebookFriendsActivity.class);
	            intent.putExtra("friends_bd", true);
	            startActivityForResult(intent, FACEBOOK_FRIENDS_UI);
	            break;
	        }
	        case R.id.facebook_menu_add_event: 
	        {
	            Intent intent = new Intent(Intent.ACTION_EDIT);
	            intent.setComponent(new ComponentName("com.android.calendar", "com.android.calendar.FacebookEvent"));
	            startActivity(intent);
	            break;
	        }
	        case R.id.facebook_menu_clear_cache: 
	        {
	            doClearCache();
	            break;
	        }
	        case R.id.facebook_menu_event_switch:
	        {
	            switchEvent();
	            break;
	        }
	        case R.id.facebook_menu_sync_event: 
	        {
	            doGlobalSyncToEvent();
	            break;
	        }
	        case R.id.facebook_menu_stop: 
	        {
	            stopSync();
	            break;
	        }
	        /*
	         * case R.id.facebook_menu_test: { Intent intent = new
	         * Intent(FacebookBaseActivity.this,TestServiceActivity.class);
	         * startActivity(intent); break; }
	         */
	        case R.id.sns_task_management_menu: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,TaskManagementActivity.class);
	            startActivity(intent);
	            break;
	        }
	        case R.id.facebook_menu_go_mobile_facebook: {
	            Intent intent = new Intent(Intent.ACTION_VIEW);
	            if (orm.getFacebookUseHttps()) {
	                intent.setData(Uri.parse("https://touch.facebook.com"));
	            } else {
	                intent.setData(Uri.parse("http://touch.facebook.com"));
	            }
	
	            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            formatFacebookIntent(intent, orm);
	
	            startActivity(intent);
	            break;
	        }
	        case R.id.facebook_menu_about: 
	        {
	            Intent intent = new Intent(FacebookBaseActivity.this,AboutActivity.class);
                    intent.putExtra("forabout", true);
	            startActivity(intent);
	            break;
	        }
	        case R.id.facebook_menu_add_as_friend: 
	        {
	            addAsFriends();
	            break;
	        }
	        case R.id.facebook_menu_post_to_wall:
	        
	        {
	            if (FacebookAccountActivity.class.isInstance(this)) 
	            {
	                FacebookAccountActivity fa = (FacebookAccountActivity) this;
	                postToWall(fa.uid);
	            }
	            break;
	        }
	        case R.id.facebook_menu_add_photo:
	        {
	            addPhoto();
	            break;
	        }
	        case R.id.facebook_menu_edit_album: 
	        {
	            if (FacebookAlbumViewActivity.class.isInstance(this)) 
	            {
	                FacebookAlbumViewActivity fav = (FacebookAlbumViewActivity) this;
	                PhotoAlbum album = fav.getAlbum();
	                addComments(null, album);
	            }
	            break;
	        }
	        case R.id.facebook_menu_goto_album:
	        {
	            if(FacebookPhotoCommentsActivity.class.isInstance(this))
	            {
	               GoToAlbum(); 
	            }
	            break;
	        }
	        case R.id.facebook_menu_goto_albumlist:
	        {
	            if(FacebookAlbumViewActivity.class.isInstance(this))
	            {
	               GoToAlbumList(); 
	            }
	        }

        }
        return true;
    }

    protected void editNote() {
        // TODO Auto-generated method stub
        
    }

    protected void switchEvent() {
      
    }

    protected void loadStreamFilter() 
    {
        /*Intent intent = new Intent(FacebookBaseActivity.this, FacebookStreamFilterActivity.class);
        startActivityForResult(intent, FACEBOOK_SELECT_STREAM_FILTRE);*/
        
    }
    
    protected void syncAddressbook(){}
    protected void createNewNotes(){}
    
    protected void reloadStreamFilter(String filter, String name) 
    {
        
    }
    protected void addPhoto() {

    }
    
    protected void GoToAlbum()
    {
        
    }
    
    protected void GoToAlbumList()
    {
        
    }
        
    protected void doClearCache() {

    }

    protected void doNotificationSend() {

    }

    protected boolean isOnline() {
        return false;
    }

    protected void lookupContact() {

    }

    protected void doFindFriends() {
        Intent intent = new Intent(FacebookBaseActivity.this, FacebookFindFriendsActivity.class);
        startActivityForResult(intent, FACEBOOK_FIND_FRIENDS);
    }

    protected void reqeustLoaction() {
    }

    protected void doPublishStream() {
        // TODO Auto-generated method stub

    }

    protected void stopSync() {
    }

    protected void doGlobalSyncToContact() {
    }

    protected void doAddPhonebookIntoContact() {
    }

    protected void doLookupallContact() {
    }

    protected void doGlobalSyncToEvent() {
    }

    private void doLogout() {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(
        R.string.facebook_logout_title).setMessage(
        getString(R.string.facebook_logout_message)).setPositiveButton(
        getString(R.string.sns_ok),
        new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int whichButton) 
            {
                // remove session and expire the session
                FacebookSession se = loginHelper.getTempSesstion();
                if (se != null) {
                    orm.logout();
                    facebookA = new AsyncFacebook(se);
                    facebookA.expireSesssionAsync(se.getSessionKey(),
                            new FacebookAdapter() {
                                @Override
                                public void expireSession(String session) {
                                    Log.d(TAG, "expire the session=" + session);
                                    // remove session
                                    loginHelper.clearSesion();
                                    // remember previouse account to
                                    // save the user data
                                    orm.addSetting( SocialORM.pre_account, orm.getFacebookAccount().email);

                                    // reset the session and async in
                                    // activity
                                    AccountManager.logout();
                                    // prompt user to re-login
                                    basichandler.obtainMessage(FACEBOOK_LOGIN_MSG).sendToTarget();
                                }

                                @Override
                                public void onException(
                                        FacebookException e, int method) {
                                    // remove session
                                    loginHelper.clearSesion();
                                    // remember previouse account to
                                    // save the user data
                                    orm.addSetting(SocialORM.pre_account,orm.getFacebookAccount().email);

                                    // reset the session and async in
                                    // activity
                                    AccountManager.logout();

                                    // if fail, we still let user to do
                                    // login
                                    basichandler.obtainMessage(FACEBOOK_LOGIN_MSG).sendToTarget();

                                    Log.d(TAG, "fail to expire="+ e.getMessage());
                                }
                            });
                } 
                else 
                {
                    Log.d(TAG, "I am not invalid, so login direct");
                    // prompt user to re-login
                    basichandler.obtainMessage(FACEBOOK_LOGIN_MSG)
                            .sendToTarget();
                }
            }
        }).setNegativeButton(getString(R.string.sns_cancel),
        new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int whichButton) {}
        }).create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) 
    {
        switch (requestCode) 
        {
	        case FACEBOOK_COMMENTS: 
	        {
	            if (FacebookStreamActivity.class.isInstance(this) || FacebookAccountActivity.class.isInstance(this)) 
	            {
	                // get post id, and update commend count
	                if (intent != null) 
	                {
	                    String post_id = intent.getStringExtra("post_id");
	                    List<String> newcomments = intent.getStringArrayListExtra("newcomments");
	
	                    updateComments(post_id, newcomments);
	                }
	            }
	            break;
	        }
	        case FACEBOOK_SELECT_STREAM_FILTRE:
	        {
	            if (FacebookStreamActivity.class.isInstance(this) ) 
                {
                    // get post id, and update commend count
                    if (intent != null) 
                    {
                        String filter = intent.getStringExtra("filter");
                        reloadStreamFilter(filter, "");
                    }
                }
                break;
	        }
	        case FACEBOOK_SETTING: 
	        {
	            if (resultCode == CHANGED_ACCOUNT) {
	                Log.d(TAG, "account is changed");
	                loadAfterSetting();
	            } else {
	                if (fromtabview == false) {
	                    loadAfterSettingNoChange();
	                }
	            }
	            break;
	        }
	        case FACEBOOK_DONOTHING: 
	        {
	            doNothing();
	            break;
	        }
	        case FACEBOOK_LOGIN: 
	        {
	            // suc to get the code
	            if (resultCode == LOGIN_SUC) {
	                Log.d(TAG, "succeed in logging facebook");
	                doAfterLogin();
	            } else if (resultCode == 1000) {
	                Log.d(TAG, "user choose exit the login, do nothing");
	                doAfterLoginNothing();
	            } else if (resultCode == 200) {
	                Log.e(TAG, "fail to log in facebook=" + resultCode);
	                doAfterLogin();
	            } else {
	                Log.e(TAG, "fail to log in facebook=" + resultCode);
	                doAfterLogin();
	            }
	
	            break;
	        }
	        case STATUS_INSERT_IMG: 
	        {
	            Log.d(TAG, "after insert image===" + intent);
	            if (intent != null) {
	                showImg(intent, STATUS_INSERT_IMG);
	            }
	            break;
	        }
	        case STATUS_CAPTURE_PHOTO: 
	        {
	            // Log.d(Tag, msg)
	            if (intent != null) {
	                showImg(intent, STATUS_CAPTURE_PHOTO);
	            }
	            break;
	        }
	        case STATUS_INSERT_VIDEO: 
	        {
	            if (intent != null) {
	                showVideo(intent);
	            }
	            break;
	        }
	        case STATUS_LINK_RESULT: 
	        {
	            if (intent != null) {
	                showLink(intent);
	            }
	            break;
	        }
	        default:
	            break;

        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    protected void showLink(Intent intent) {
    }

    protected void showImg(Intent intent, int requestcode) {

    }

    protected void updateComments(String post_id, List<String> comments) {

    }

    protected void showVideo(Intent intent) {
    }

    protected void loadRefresh() {
        Log.d(TAG, "call loadRefresh=" + this);
    }

    protected void postToWall(long uid) {
        Intent wallintent = new Intent(this, FacebookStatusUpdateActivity.class);
        wallintent.putExtra("fuid", new Long(uid));
        startActivityForResult(wallintent, 9999);
    }

    protected void newMail(long uid) {
        Intent intent = new Intent(FacebookBaseActivity.this, FacebookMailActivity.class);
        intent.putExtra("newmail", true);
        intent.putExtra("mailtowho", uid);
        startActivityForResult(intent, FACEBOOK_NEW_MAIL);
    }

    protected void addAsFriends(long uid) {

    }

    protected void addshortCut(FacebookUser user) {
        user.isShoutcut = true;
        orm.updateFacebookUserShortCut(user, true);
    }

    protected void addshortCut(FacebookUser.SimpleFBUser user) {
        user.isShoutcut = true;
        orm.updateFacebookUserShortCut(user, true);
    }

    protected void addAsFriends() {

    }

    protected void loadAfterSetting() {
        Log.d(TAG, "call loadAfterSetting");
        launchFacebookLogin();
    }

    protected void doAfterLogin() {
        Log.d(TAG, "after login");
    }

    protected void doAfterLoginNothing() {
        Log.d(TAG, "after login");
    }

    protected void doNothing() {
        Log.d(TAG, "call doNothing");
    }

    protected void loadAfterSettingNoChange() {
        Log.d(TAG, "call loadAfterSettingNoChange");
    }

    protected void loadOlderPost() {

    }

    // notify the status
    protected void cancelNotify() {
    	if(notify != null)
    	{
            notify.cancel();
    	}
    }

    // process the basic facebook action,
    // such as login, ext permission
    final int TITLE_PROGRESS_begin = 8900;
    final int TITLE_PROGRESS_begin_db = 8901;
    final int TITLE_PROGRESS_prepare = 8902;
    final int TITLE_PROGRESS_afterprepare = 8903;
    final int TITLE_PROGRESS_using = 8904;
    final int TITLE_PROGRESS_end = 8905;
    final int TITLE_PROGRESS_stop = 8906;

    private class BasicHandler extends Handler {
        public BasicHandler() {
            super();
            Log.d(TAG, "new BasicHandler");
        }

        @Override
        public void handleMessage(Message msg) 
        {
            switch (msg.what) 
            {
	            case TITLE_PROGRESS_begin: 
	            {
	                if (isBackgroud()) {
	                    // to let the progress show the end when in background.
	                    if (isFromTabView() == true) {
	                        setProgressForFacebook(10 * 100);
	                    }
	                } else {
	                    setProgressForFacebook(10 * 100);
	                    //setTitle(START_REQUEST);
	                }
	                break;
	            }
	            case TITLE_PROGRESS_begin_db: 
	            {
	                if (isBackgroud()) {
	
	                } else {
	                    setProgressForFacebook(10 * 100);
	                    //setTitle(LOAD_FROM_DB);
	                }
	                break;
	            }
	            case TITLE_PROGRESS_prepare: 
	            {
	                if (isBackgroud()) {
	
	                } else {
	                    setProgressForFacebook(5 * 100);
	                    //setTitle(PREPARING);
	                }
	                break;
	            }
	            case TITLE_PROGRESS_afterprepare:
	            {
	                if (isBackgroud()) {
	
	                } else {
	                    setProgressForFacebook(10 * 100);
	                    //setTitle(PREPARING);
	                }
	                break;
	            }
	            case TITLE_PROGRESS_using: 
	            {
	                if (isBackgroud()) {
	                    // to let the progress show the end when in background.
	                    if (isFromTabView() == true) {
	                        setProgressForFacebook(DATA_READY_PROGRESS * 100);
	                    }
	                } else {
	                    setProgressForFacebook(DATA_READY_PROGRESS * 100);
	                    //setTitle(TITLE_CONSTRUCT_UI);
	                }
	                break;
	            }
	            case TITLE_PROGRESS_stop: 
	            {
	                if (isBackgroud()) {
	
	                } else {
	                    setProgressForFacebook(STOP_PROGRESS * 100);
	                    //setTitle(TITLE_STOPING);
	                }
	                break;
	            }
	            case TITLE_PROGRESS_end:
	            {
	                if (isBackgroud()) {
	                    // to let the progress show the end when in background.
	                    if (isFromTabView() == true) {
	                        setProgressForFacebook(100 * 100);	                        
	                    }	                    
	                    setProgressForFacebook(100 * 100);
	                } else {
	                    setProgressForFacebook(100 * 100);
	                    setTitle(title);
	                }
	                break;
	            }
	            case UI_SET_PROGRESS:
	            {
	                if (isBackgroud()) {
	                    // to let the progress show the end when in background.
	                    if (isFromTabView() == true) {
	                        int progress = msg.getData().getInt("progress");
	                        setProgressForFacebook(progress);
	                    }
	                } else {
	                    int progress = msg.getData().getInt("progress");
	                    setProgressForFacebook(progress);
	                }
	                break;
	            }
	            case UI_SET_TITLE: 
	            {
	                if (isBackgroud()) {
	
	                } else {
	                    String title = msg.getData().getString("title");
	                    if(isShowTitleBar == false){
	                        headerTitle.setText(title);
	                    }
	                    else
	                    {
	                        mContext.setTitle(title);
	                    }
	                }
	                break;
	            }
	            case FACEBOOK_LOGIN_MSG: 
	            {
	                Log.d(TAG, "prompt login UI");
	                Intent intent = new Intent(FacebookBaseActivity.this, FacebookLoginActivity.class);
	                // just one activity exist
	                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	                startActivityForResult(intent, FACEBOOK_LOGIN);
	                break;
	            }
	            case FACEBOOK_SETTING_MSG:
	            {
	                boolean forsignin = msg.getData().getBoolean("forsignin");
	                Log.d(TAG, "prompt setting activity");
	                Intent intent = new Intent(FacebookBaseActivity.this, FacebookSettingPreference.class);
	                intent.putExtra("forsignin", forsignin);
	                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	                startActivityForResult(intent, FACEBOOK_SETTING);
	                break;
	            }
	            case FACEBOOK_GET_USERINFO: 
	            {
	                // try to get userinfo and save it into database
	                // TODO
	                long uid = msg.getData().getLong("uid", -1);
	                if (uid != -1) {
	
	                }
	                break;
	            }
	            case FACEBOOK_STATUS_UPDATE: 
	            {
	                Intent intent = new Intent(FacebookBaseActivity.this,FacebookStatusUpdateActivity.class);
	                startActivity(intent);
	                break;
	            }
	            case INVALID_SESSION:
	            {
	                // re login
	                // clear data
	                Log.d(TAG, "INVALID_SESSION prompt login UI for invalid session");
	                Toast.makeText(mContext, R.string.facebook_invalid_session,Toast.LENGTH_SHORT).show();
	                Intent intent = new Intent(FacebookBaseActivity.this,FacebookLoginActivity.class);
	                intent.putExtra("forinvalidsession", true);
	                //
	                startActivityForResult(intent, FACEBOOK_LOGIN);
	                break;
	            }
	            case NO_EXT_PERMISSION: 
	            {
	                String permission = msg.getData().getString("permission");
	                Log.d(TAG, "NO_EXT_PERMISSION =" + permission);
	                Intent intent = new Intent(FacebookBaseActivity.this, FacebookExtPermissionActivity.class);
	                intent.putExtra("permission", permission);
	                startActivityForResult(intent, FACEBOOK_EXT_PERMISSION_UI);
	                break;
	            }
	            case POKE_SOMEONE: 
	            {
	            	//showDialog(DLG_FACEBOOK_POKE);
	               final long uid = msg.getData().getLong("pokeuid", -1);
                    String username = msg.getData().getString("username");
                    if(isEmpty(username))
                    {
                        username = String.valueOf(uid);
                    }
                    final String f_uname = username;
	                new AlertDialog.Builder(mContext)
	                .setTitle(String.format(getString(R.string.facebook_poke_dialog_title),username))
	                .setMessage(String.format(getString(R.string.facebook_poke_dialog_msg),username))
	                .setPositiveButton(R.string.menu_title_poke, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                        doPoke(uid,f_uname);
	                    }
	                })
	                .setNegativeButton(R.string.hint_album_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked Cancel so do some stuff */
	                    }
	                })
	                .create().show();
	                break;
	            }
	            case POKE_SOMEONE_END:
	            {
	            	//dismissDialog(DLG_FACEBOOK_POKE);
	            	break;
	            }
	            case LIKE_STREAM: 
	            {
	                showDialog(DLG_FACEBOOK_LIKE);
	                String uuid = msg.getData().getString("post_id");
	                doStreamLike(uuid);
	                break;
	            }
	            case UNLIKE_STREAM:
	            {
	                showDialog(DLG_FACEBOOK_UNLIKE);
	                String uuid = msg.getData().getString("post_id");
	                doStreamUnLike(uuid);
	                break;
	            }
	            case LIKE_STREAM_END:
	            {
	                finishPoke();
	                dismissDialog(DLG_FACEBOOK_LIKE);
	                break;
	            }
	            case UNLIKE_STREAM_END: 
	            {
	                dismissDialog(DLG_FACEBOOK_UNLIKE);
	                break;
	            }
	            case FACEBOOK_REMOVE_COMMENT:
	            {
	                showDialog(DLG_FACEBOOK_REMOVE_COMMENT);
	                String commentid = msg.getData().getString("comment_id");
	                removeComment(commentid);
	                break;
	            }
	            case FACEBOOK_REMOVE_COMMENT_END: 
	            {
	                dismissDialog(DLG_FACEBOOK_REMOVE_COMMENT);
	                boolean retvalue = msg.getData().getBoolean("result");
	                if (retvalue) {
	                    handler.post(new Runnable() {
	                        public void run() {
	                            Toast.makeText(mContext,getString(R.string.facebook_remove_comment_successfully),Toast.LENGTH_SHORT).show();
	                        }
	                    });
	                } else {
	                    handler.post(new Runnable() {
	                        public void run() {
	                            Toast.makeText(mContext,getString(R.string.facebook_remove_comment_failed),Toast.LENGTH_SHORT).show();
	                        }
	                    });
	                }
	                break;
	            }
	            case PHOTO_DELETE:
	            {
	                Photo photo = msg.getData().getParcelable("photo");
	                deletePhotoFromFacebook(photo);
	                break;
	            }
	            case PHOTO_DELETE_END: 
	            {
	                end();
	                if (msg.getData().getBoolean(RESULT) == true) {
	                    Toast.makeText(mContext, R.string.sns_operate_succeed,Toast.LENGTH_SHORT).show();
	                    Photo photo = msg.getData().getParcelable("photo");
	                    onAfterdeletePhoto(photo);
	                } else {
	                    Toast.makeText(mContext, R.string.sns_operate_failed,Toast.LENGTH_SHORT).show();
	                }
	                break;
	            }
	            case ALBUM_DELETE: 
	            {
	            	String aid = msg.getData().getString("aid");
	                deleteAlbumFromFacebook(aid);
	                break;
	            }
	            case ALBUM_DELETE_END: 
	            {
	                end();
	                if (msg.getData().getBoolean(RESULT) == true) {
	                    Toast.makeText(mContext, R.string.sns_operate_succeed,Toast.LENGTH_SHORT).show();
	                    String aid = msg.getData().getString("aid");
	                    onAfterDeleteAlbum(aid);
	                } else {
	                    Toast.makeText(mContext, R.string.sns_operate_failed,Toast.LENGTH_SHORT).show();
	                }
	                break;
	            }
            }
        }
    }
    
    protected void setProgressNoTitle(int progress)
    {
        if(null == headerProgressBar)
        {
            return;
        }
        
        if(100*100 == progress)
        {
            headerProgressBar.setVisibility(View.GONE);
            if(null != headerTitle)
            {
                headerTitle.setPadding(0, 0, 8, 0);
            }
        }
        else
        {
            headerProgressBar.setVisibility(View.VISIBLE);
            if(null != headerTitle)
            {
                headerTitle.setPadding(0, 0, 28, 0);
            }
        }
    }

    private void setProgressForFacebook(int progress) 
    {
        if (this.isFromTabView() == false) {
            if(isShowTitleBar == false)
            {
                setProgressNoTitle(progress);
            }
            else
            {
                if(100*100 == progress)
                {
                    setProgressBarIndeterminateVisibility(false);
                    setProgressBarIndeterminate(false);
                }
                else
                {
                    setProgressBarIndeterminateVisibility(true);
                    setProgressBarIndeterminate(true);
                }
                setProgress(progress);
            }
        } else {
            setCurProgress(progress);
        }
    }

    // need Tab view override the progress
    protected void setCurProgress(int pro) 
    {
        if (pro != 100 * 100) 
        {
            if (progressHorizontal != null) {
                progressHorizontal.setVisibility(View.VISIBLE);
                progressHorizontal.setProgress(pro / 100);
            }
        } 
        else 
        {
            if (progressHorizontal != null) {
                progressHorizontal.setVisibility(View.GONE);
            }
        }
    }

    final static int DLG_FACEBOOK_POKE           = 0;
    final static int DLG_FACEBOOK_LIKE           = 1;
    final static int DLG_FACEBOOK_UNLIKE         = 2;
    final static int DLG_FACEBOOK_REMOVE_COMMENT = 3;
    final static int DLG_POST_WALL               = 4;
    final static int DLG_ADD_COMMAND             = 5;
    final static int DLG_SEND_MAIL               = 6;
    final static int DLG_PHONEBOOK_ACTION        = 7;
    final static int DLG_EDIT_PHOTO_CAPTION      = 8;
    final static int DLG_SAVE_CHANGE             = 9;
    final static int DLG_SAVE_NOTE              = 10;
    final static int DLG_SET_CONTACT            =  11;
    final static int DLG_FIND_PEOPLE           = 12;
    final static int DLG_UPLOAD_MEDIA          = 13;
    final static int DLG_POST_CONTENT          = 14;
    final static int DLG_FACEBOOK_POKE_BACK  = 15;
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) 
        {
	        case DLG_FACEBOOK_POKE: {
	            ProgressDialog dialog = new ProgressDialog(this);
	            dialog.setTitle(R.string.facebook_poking_title);
	            dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
	            dialog.setCanceledOnTouchOutside(true);
	            dialog.setIndeterminate(true);
	            dialog.setCancelable(true);
	            return dialog;
	           
	        }
	        case DLG_FACEBOOK_POKE_BACK:{
	            ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.menu_facebook_title_poke_back);
                dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
                dialog.setCanceledOnTouchOutside(true);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;	            
	        }
	        case DLG_POST_CONTENT:
	        {
	            ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.facebook_dlg_post);
                dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
                dialog.setCanceledOnTouchOutside(true);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
	        }
	        case DLG_UPLOAD_MEDIA:
	        {
	            ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.facebook_dlg_uploading);
                dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
                dialog.setCanceledOnTouchOutside(true);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
	        }
	        case DLG_SET_CONTACT:
	        {
	            ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.facebook_dlg_set_contact);
                dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
                dialog.setCanceledOnTouchOutside(true);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
	        }
	        case DLG_FIND_PEOPLE:
	        {
	            ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.facebook_dlg_find_people);
                dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
                dialog.setCanceledOnTouchOutside(true);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
	        }
	        case DLG_EDIT_PHOTO_CAPTION:
	        {
	            ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.facebook_changing_caption);
                dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
                dialog.setCanceledOnTouchOutside(true);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
	        }
	        case DLG_SAVE_CHANGE:
	        {
	            ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.facebook_save_change);
                dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
                dialog.setCanceledOnTouchOutside(true);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
	            return dialog;
	        }
	        case DLG_SAVE_NOTE:
	        {
	            ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(R.string.facebook_dlg_save_note);
                dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
                dialog.setCanceledOnTouchOutside(true);
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                return dialog;
	        }
	        case DLG_FACEBOOK_UNLIKE: {
	            ProgressDialog dialog = new ProgressDialog(this);
	            dialog.setTitle(R.string.sns_remove_like);
	            dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
	            dialog.setCanceledOnTouchOutside(true);
	            dialog.setIndeterminate(true);
	            dialog.setCancelable(true);
	            return dialog;
	        }
	        case DLG_FACEBOOK_LIKE: {
	            ProgressDialog dialog = new ProgressDialog(this);
	            dialog.setTitle(R.string.sns_liking);
	            dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
	            dialog.setCanceledOnTouchOutside(true);
	            dialog.setIndeterminate(true);
	            dialog.setCancelable(true);
	            return dialog;
	        }
	        case DLG_FACEBOOK_REMOVE_COMMENT: {
	            ProgressDialog dialog = new ProgressDialog(this);
	            dialog.setTitle(R.string.facebook_remove_comment_title);
	            dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
	            dialog.setCanceledOnTouchOutside(true);
	            dialog.setIndeterminate(true);
	            dialog.setCancelable(true);
	            return dialog;
	        }
	        case DLG_POST_WALL: 
	        {
	            ProgressDialog dialog = new ProgressDialog(this);
	            dialog.setTitle(R.string.facebook_post_wall);
	            dialog.setMessage(getString(R.string.facebook_post_wall_waiting));
	            dialog.setCanceledOnTouchOutside(true);
	            dialog.setIndeterminate(true);
	            dialog.setCancelable(true);
	            return dialog;
	        }
	        case DLG_ADD_COMMAND:
	        {
	        	 ProgressDialog dialog = new ProgressDialog(this);
	             dialog.setTitle(R.string.facebook_add_comment_title);
	             dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
	             dialog.setCanceledOnTouchOutside(true);
	             dialog.setIndeterminate(true);
	             dialog.setCancelable(true);
	             return dialog;
	        }
	        case DLG_SEND_MAIL:
	        {
	        	 ProgressDialog dialog = new ProgressDialog(this);
	             dialog.setTitle(R.string.facebook_send_mail_title);
	             dialog.setMessage(getString(R.string.twitter_verify_account_wait_msg));
	             dialog.setCanceledOnTouchOutside(true);
	             dialog.setIndeterminate(true);
	             dialog.setCancelable(true);
	             return dialog;
	        }
        }

        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "KEYCODE_BACK coming=" + this);
            stopLoading();
            restoreTitle();
            //System.gc();

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        gestureprocessed = false;
        Log.d(TAG, "&&&&&&&&&&&&&&&&&on key up=" + event);

        return super.onKeyUp(keyCode, event);
    }

    protected void addLikeAction(final String pid, boolean suc) {
        Log.d(TAG, "call addLikeAction");
    }

    public void doStreamLike(final String uuid) {
        synchronized (mLock) {
            inprocess = true;
        }
        facebookA.streamLikeAsync(uuid, new FacebookAdapter() {
            @Override
            public void streamLike(boolean suc) {
                Log.d(TAG, "after add like=" + suc);
                synchronized (mLock) {
                    inprocess = false;
                }

                if (donotcallnetwork == false)// I am still alive
                {
                    cancelNotify();
                }

                addLikeAction(uuid, suc);
                Message rmsg = basichandler.obtainMessage(LIKE_STREAM_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.sendToTarget();
            }

            @Override
            public void onException(FacebookException e, int method) {
                synchronized (mLock) {
                    inprocess = false;
                }

                Log.d(TAG, "after get stream ex=" + e.getMessage());
                if (isInAynscTaskAndStoped()) {
                    Log.d(TAG, "User stop passive");
                } else {
                    Message rmsg = basichandler.obtainMessage(LIKE_STREAM_END);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.sendToTarget();
                }
            }
        });
    }

    protected void removeLikeAction(final String pid, boolean suc) {
        Log.d(TAG, "call removeLikeAction");
    }

    public void doStreamUnLike(final String uuid) {
        synchronized (mLock) {
            inprocess = true;
        }
        facebookA.streamUnLikeAsync(uuid, new FacebookAdapter() {
            @Override
            public void streamUnLike(boolean suc) {
                Log.d(TAG, "after remove like=" + suc);
                synchronized (mLock) {
                    inprocess = false;
                }

                if (donotcallnetwork == false)// I am still alive
                {
                    cancelNotify();
                }

                // change the content and reflect the UI
                removeLikeAction(uuid, suc);

                Message rmsg = basichandler.obtainMessage(UNLIKE_STREAM_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.sendToTarget();
            }

            @Override
            public void onException(FacebookException e, int method) {
                synchronized (mLock) {
                    inprocess = false;
                }

                Log.d(TAG, "after get stream ex=" + e.getMessage());
                if (isInAynscTaskAndStoped()) {
                    Log.d(TAG, "User stop passive");
                } else {
                    Message rmsg = basichandler.obtainMessage(UNLIKE_STREAM_END);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.sendToTarget();
                }

            }
        });
    }

    public void finishPoke() {
        // TODO Auto-generated method stub
    }

    public void doPoke(long uuid,String username) {
        // TODO Auto-generated method stub
    }

    protected void nextPage() {
        // TODO Auto-generated method stub
        return;
    }

    protected void prePage() {
        // TODO Auto-generated method stub
        return;
    }

    protected boolean isTheFirst() {
        // TODO Auto-generated method stub
        return false;
    }

    protected boolean hasMore() {
        // TODO Auto-generated method stub
        return false;
    }

    protected boolean existSession() {
        if (perm_session == null || facebookA == null) {
            Log.d(TAG, "no session exist=" + this);
            return false;
        }

        return true;
    }

    private Object logObj = new Object();

    // for account
    public void onLogin() {
        Log.d(TAG, "onLogin=" + this);
        synchronized (logObj) {
            perm_session = loginHelper.getPermanentSesstion(this);
            if (perm_session == null) {
                // re-launch the login UI
                launchFacebookLogin();
                return;
            } else {
                facebookA = new AsyncFacebook(perm_session);
                perm_session.attachActivity(this);
            }
        }
    }

    public void onLogout() {
        Log.d(TAG, "onLogout=" + this);
        synchronized (logObj) {
            perm_session = null;
            facebookA = null;
        }
    }

    public void showNoContentView() {
    }
    
    public boolean isProfile(String url)
	{
    	Uri uri = Uri.parse(url);
    	String path = uri.getPath();
    	Log.d("sns-link", "isProfile url="+url +" path="+path);
    	if(isEmpty(path) == false)
    	{
    	    if(path.equals("/profile.php"))
    	    {
    	    	return true;
    	    }
    	}
		return false;
	}
	 
	public boolean isPhoto(String url)
	{
		Uri uri = Uri.parse(url);
    	String path = uri.getPath();
    	
    	Log.d("sns-link", "isPhoto url="+url +" path="+path);
    	if(isEmpty(path) == false)
    	{
    	    if(path.equals("/album.php") || path.equals("/photo.php"))
    	    {
    	    	return true;
    	    }
    	}
		return false;
	}
	
	protected void loadUserInfoAndPageInfo(List<Stream> streams2) {
        Log.d(TAG,"entering load UserInfo and PageInfo method");
        if(streams2 != null)
        {    
            ArrayList<Long> uid_list = new ArrayList<Long>();
            for(Stream strem : streams2)
            {
                //get target id
                FacebookUser user = orm.getFacebookUser(strem.source_id);              
                if(user == null)
                {
                    Page page = orm.getPageBypid(strem.source_id);
                    if(page == null)
                    {
                         if(uid_list.contains(strem.source_id) == false)
                         {
                             uid_list.add(strem.source_id);
                         }
                    }
                }
                
                user = orm.getFacebookUser(strem.target_id);              
                if(user == null)
                {
                    Page page = orm.getPageBypid(strem.target_id);
                    if(page == null)
                    {
                         if(uid_list.contains(strem.target_id) == false)
                         {
                             uid_list.add(strem.target_id);
                         }
                    }
                }
                
                user = orm.getFacebookUser(strem.actor_id);              
                if(user == null)
                {
                     if(uid_list.contains(strem.actor_id) == false)
                     {
                         uid_list.add(strem.actor_id);
                     }
                }              
            }
            
           
            Log.d(TAG,"uid list is "+uid_list.size());
            if(uid_list.size()>0)
            {
                int round = uid_list.size()/100;
                int last_round_size = uid_list.size()%100;
                if(last_round_size > 0)
                {
                    round = round + 1;
                }
                    
                Log.d(TAG,"round is ="+round +" last_round_size is="+last_round_size);
                for(int j=0;j<round;j++)
                {  
                    int size = ((j+1)==round)?last_round_size:100;
                    long[] uids = new long[size];
                    for(int i=0;i<size;i++)
                    {
                       uids[i] = uid_list.get(j*100+i);
                    } 
                    Message msg = handler.obtainMessage(PINFO_UINFO_GET);
                    msg.getData().putLongArray("ids", uids);
                    if(j==0)
                    {
                        Log.d(TAG,"send message right now");
                        msg.sendToTarget(); 
                    }
                    else
                    {
                        Log.d(TAG,"send message delay");
                        handler.sendMessageDelayed(msg, 1000);
                    }
                }      
            }
        }        
    }
	
	protected void hideInputKeyBoard(View view)
    {
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null && view != null) 
        {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            imm = null;
        }
   }
	
	
	protected void cacheReplyMessage(String content, long tid,MessageThreadInfo mthread)
    {
        Log.d(TAG,"entering cacheReplyMessage Method tid is="+tid+"orginal threadid is="+mthread.thread_id+" content is="+content);
        if(tid > 0)
        {
            MailboxMessage message = new MailboxMessage();
            message.author = perm_session.getLogerInUserID();
            message.body = content;
            message.hasattachment = 0;
            message.threadid = tid;
            message.mid = "-1"; //tmp mid;
            message.timesent = new Date(); 
            orm.addMailMessages(message); // save MailMessage
            
            MessageThreadInfo mtInfo = new MessageThreadInfo();
            mtInfo.isoutbox = true;
            mtInfo.folder_id = mthread._id;
            mtInfo.isinbox = mthread.isinbox;
            mtInfo.message_count = mthread.message_count+1;
            mtInfo.object_id = mthread.object_id;
            mtInfo.parent_message_id = mthread.parent_message_id;
            mtInfo.parent_thread_id = mthread.parent_thread_id;
            mtInfo.recipients = mthread.recipients;
            mtInfo.snippet = content;
            mtInfo.snippet_author = perm_session.getLogerInUserID();
            mtInfo.subject = mthread.subject;
            mtInfo.thread_id = mthread.thread_id;
            mtInfo.unread = 0;
            mtInfo.update_update_time = mthread.update_update_time;
            mtInfo.inbox_updated_time = mthread.inbox_updated_time;
            mtInfo.outbox_updated_time = System.currentTimeMillis();
            orm.addMailThread(mtInfo); //save and update MailThread /Sent info
            
        }
      
    }
    
	public void registerAccountListener() 
    {
        AccountManager.registerAccountListener(this.getClass().getName(), this);     
    }
    public void unregisterAccountListener() 
    {
        AccountManager.unregisterAccountListener(this.getClass().getName());     
    }
}
