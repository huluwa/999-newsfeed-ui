package com.msocial.freefb.ui;

import com.msocial.freefb.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.providers.SocialORM.FacebookUsersCol;
import com.msocial.freefb.providers.SocialORM.PageCol;
import com.msocial.freefb.quickaction.QuickLauncher;
import com.msocial.freefb.service.ObjectHandler;
import com.msocial.freefb.service.SNSService;
import com.msocial.freefb.ui.adapter.FacebookFriendCursorAdapter;
import com.msocial.freefb.ui.adapter.FacebookStreamAdapter;
import com.msocial.freefb.ui.view.FacebookFriendItemView;
import com.msocial.freefb.ui.view.SNSItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Page;
import oms.sns.service.facebook.model.PhoneBook;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class FacebookNewFriendsActivity  extends FacebookBaseActivity
{
    private final String TAG="FacebookNewFriendsActivity";    
	private ListView friendList;	
	
	private View     searchSpan;
	private EditText keyEdit;
	private TextView current_postion;
	
    private Cursor fcursor;		
	
	private MyWatcher watcher;
	private long toFindFriendUID=-1;	
	private int  selectType=0;//friends, 1, Page
	
	//for page
	PageHandler pageHandler;
	private int limit  = 300;
	private int offset = 0;
	
	private Button friends_button;
	private Button page_button;
	
    private final int  FRIENDS =0;
    private final int  PAGE    =1;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Intent intent = this.getIntent();
        
        setContentView(R.layout.facebook_new_friends_ui);
        friendList = (ListView)this.findViewById(R.id.facebook_user_list);   
        friendList.setFocusableInTouchMode(true);
        friendList.setFocusable(true);
        friendList.setOnCreateContextMenuListener(this);
        friendList.setOnItemClickListener(listItemClickListener);
        friendList.setOnScrollListener(scrollListener);
        
        current_postion = (TextView)this.findViewById(R.id.current_postion);        
        
        searchSpan = this.findViewById(R.id.facebook_search_span);
        searchSpan.setVisibility(View.VISIBLE);
        keyEdit = (EditText)this.findViewById(R.id.embedded_text_editor);
        watcher = new MyWatcher();         
        keyEdit.addTextChangedListener(watcher); 
        
        pageHandler = new PageHandler();
        View v = findViewById(R.id.progress_horizontal);
		if(v != null)
		{
		    progressHorizontal = (ProgressBar) v;
		}
        
        toFindFriendUID = this.getIntent().getLongExtra("hisuid", -1);
        
        friends_button = (Button)this.findViewById(R.id.facebook_tab_wall_button);
        friends_button.setId(0);
        page_button = (Button)this.findViewById(R.id.facebook_tab_photo_button);
        page_button.setId(1);
        
        updateSubTabUI();
        
        friends_button.setOnClickListener(showContentClick);
        page_button.setOnClickListener(showContentClick);
        	
        setTitle("");
        SocialORM.Account account = orm.getFacebookAccount();        
        
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
        	    if(toFindFriendUID == -1)
        	    {
        	        toFindFriendUID = perm_session.getLogerInUserID();
        	    }
        	    
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);
	        	if(toFindFriendUID == perm_session.getLogerInUserID())
	        	{
		        	handler.obtainMessage(FACEBOOK_FRIEND_UI).sendToTarget();
		        	Cursor tmp = orm.getAllFacebookSimpleUsersCursor(toFindFriendUID);
		        	if(tmp == null || tmp.getCount() == 0)
		        	{
		        		Log.d(TAG, "I have no data, so load from web server");
		        	    lauchGeFriends();
		        	}
		        	else
		        	{
		        		tmp.close();
		        	}
	        	}
	        	else
	        	{
                    //TODO also need use cache
	        		//get this user's friends
	        		lauchGeFriends();
	        	}
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }
    }   
    
    @Override
    protected void syncAddressbook()
    {
    	
    	if(orm.isEnableSyncPhonebook() == false)
    	{
    		Message msg = handler.obtainMessage(SYNC_ADDRESS);    		
    		SyncAddressBookHelper.processSyncAddressBook(FacebookNewFriendsActivity.this.getApplicationContext(), true, null, msg);
    	}
    	else
    	{
    		doSyncInBackground();
    	}    	
    }
    
    private void doSyncInBackground()
    {
    	Toast.makeText(FacebookNewFriendsActivity.this, R.string.sns_lookupall_toast_msg, Toast.LENGTH_LONG).show();
    	SNSService.getSNSService().getContactService().resetOffset();
    	SNSService.getSNSService().getContactService().alarmPhonebookComming();
    }
    
    View.OnClickListener showContentClick = new OnClickListener()
	{
	    public void onClick(View v)
	    {
	        boolean changed = false;
	        if(v.getId() == 0)
	        {
	            if(selectType != FRIENDS)
	            {
	                changed = true;
	            }
	            selectType = FRIENDS;   	        
    	        
	        }
	        else if(v.getId() == 1)
	        {
	            if(selectType != PAGE)
                {
                    changed = true;
                }
	            selectType = PAGE;	            
	        }
	      
	        if(changed == true)//don't repeat to show
	        {
	            updateSubTabUI();
	        }
	    }
	};

	private void updateSubTabUI()
    {
        //if for status, show wall, hide others		
        if(selectType == FRIENDS)
        {
        	title = getString(R.string.menu_title_friends);	
            friends_button.setBackgroundResource(R.drawable.facebook_profile_button_white);                
            page_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);
            
            friends_button.setTextColor(Color.BLACK);        
            page_button.setTextColor(Color.WHITE);
            
            keyEdit.setHint(R.string.facebook_search_friends);
            handler.obtainMessage(FACEBOOK_FRIEND_UI).sendToTarget();
        }
        
        //if for info, hide other
        else if(selectType == PAGE)
        {            
        	title = getString(R.string.menu_title_pages);	
        	friends_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);                
            page_button.setBackgroundResource(R.drawable.facebook_profile_button_white);
            
            friends_button.setTextColor(Color.WHITE);
            page_button.setTextColor(Color.BLACK);
            keyEdit.setHint(R.string.facebook_search_page);
            
            Cursor tmp = orm.getAllPageCursor();
        	if(tmp == null || tmp.getCount() == 0)
        	{
        		Log.d(TAG, "I have no data, so load from web server");
        		lauchGetPages();
        	}
            handler.obtainMessage(FACEBOOK_PAGE_UI).sendToTarget();
	    }
        
        setTitle(title);
    }  
	
	@Override
    protected boolean  goNextPage()
	{
    	super.goNextPage();
    	boolean changed = false;
    	if(selectType == FRIENDS)
    	{	
    		changed = true;
    		selectType = PAGE;    
	    }
    	else if(selectType == PAGE)
    	{
    		changed   = false;
    		selectType = PAGE;
    	}	    	    
    	if(changed == true)
    	{
	        updateSubTabUI();
	        return true;
    	}
    	return false;
	}
    
    @Override
	protected boolean  goPrePage()
	{
    	super.goPrePage();    	
    	boolean changed = false;
    	if(selectType == FRIENDS)
    	{	
    		selectType  = FRIENDS; 	         	        
	    }
    	else if(selectType == PAGE)
    	{
    		changed   = true;
    		selectType  = FRIENDS;
    	}	    	 
    	if(changed == true)
    	{
	        updateSubTabUI();
	        return true;
    	}
    	return false;
	}   
    
    AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() 
    {		
		public void onScrollStateChanged(AbsListView view, int scrollState) 
		{		
			
		}		
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) 
		{		
			if(fcursor != null)
			{
				if(fcursor.moveToPosition(firstVisibleItem))
				{
					if(selectType == 0)
					{
						current_postion.setVisibility(View.VISIBLE);
						String name = fcursor.getString(fcursor.getColumnIndex(FacebookUsersCol.NAME));        
						current_postion.setText(name.subSequence(0, 1));
						name = null;
					}
					else
					{
						current_postion.setVisibility(View.VISIBLE);
						String name = fcursor.getString(fcursor.getColumnIndex(FacebookUsersCol.NAME));
                        if(isEmpty(name) == false)        
						current_postion.setText(name.subSequence(0, 1));
						name = null;
					}
				}
			}
		}
	};	
   
	public void setTitle() 
	{
	 	title = getString(R.string.menu_title_friends);		
	}
    
    private void doSearch(String key)
    {        
        if(key != null && key.length()>0)
        {
            if(fcursor != null)
            {
                fcursor.close();
                fcursor = null;
                
            }
            if(selectType == 0)
            {
               fcursor = orm.searchFacebookBDCursor(toFindFriendUID, key);
            }
            else
            {
               fcursor = orm.searchPageCursor(key);
            }
        }  
        else
        {
            if(fcursor != null)
            {
                fcursor.close();
                fcursor = null;
            }
            if(selectType == 0)
            {
               fcursor = orm.getAllFacebookSimpleUsersCursor(toFindFriendUID);
            }
            else
            {
                fcursor = orm.getAllPageCursor();
            }
            
        }        
       
        if(selectType == 0)
        {
            FacebookFriendCursorAdapter adapter = new FacebookFriendCursorAdapter(FacebookNewFriendsActivity.this, fcursor, false, true);        
            friendList.setAdapter(adapter);
        }
        else
        {
        	 PageAdapter adapter = new PageAdapter(FacebookNewFriendsActivity.this, fcursor);        
             friendList.setAdapter(adapter);
        }
    }
    
    QuickLauncher ql = new QuickLauncher();
	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			Log.d(TAG, "facebook friend Item clicked");
			if(FacebookFriendItemView.class.isInstance(v))
			{
				FacebookFriendItemView fv= (FacebookFriendItemView)v;
				ql.popupQuickLauncher(FacebookNewFriendsActivity.this, v, fv.getUser());
				/*
				Intent intent = new Intent(mContext, FacebookAccountActivity.class);
				intent.putExtra("uid",      fv.getUser().uid);
				intent.putExtra("username", fv.getUser().name);
				intent.putExtra("imageurl", fv.getUser().pic_square);					
				((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
				*/							
			}
			else if(PageItemView.class.isInstance(v))
			{
				PageItemView fv= (PageItemView)v;
				Intent intent = new Intent(mContext, FacebookAccountActivity.class);
				intent.putExtra("uid",      fv.getpage().page_id);
				intent.putExtra("username", fv.getpage().name);
				intent.putExtra("imageurl", fv.getpage().pic_square);	
				intent.putExtra("frompage", true);
				
				((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);			
			}
		}
	};
	   
	//don't move fcursor cursor, use a temp one
    private FacebookUser.SimpleFBUser getFriends(final Cursor cursor, int pos)
    {
        FacebookUser.SimpleFBUser fb = null;
        Cursor tmp = cursor;
        if(tmp != null)
        {
            boolean suc = tmp.moveToPosition(pos);
            if(suc)
            {
                fb = orm.formatSimpleFacebookUser(tmp);
            }
        }
        return fb;
    }	
		
    @Override
	protected void createHandler() 
    {
		handler = new MainHandler();		
	}

    //try to get wall message
    private boolean inloadPage=false;
    private void lauchGetPages()
    {   
    	Log.d(TAG, "call lauchGetPages="+this+" offset is "+offset);
        if(inloadPage == true)
        {
            Log.d(TAG, "I am get friends="+this);
            Toast.makeText(mContext, R.string.getting_page_from_server, Toast.LENGTH_SHORT).show();
            return;
        }
    	Message msg = handler.obtainMessage(FACEBOOK_PAGE_GET);
    	msg.sendToTarget();
    }
    
    //try to get wall message
    private void lauchGeFriends()
    {   
    	Log.d(TAG, "call lauchGeFriends="+this);
        if(this.isInProcess())
        {
            Log.d(TAG, "I am get friends="+this);
            Toast.makeText(mContext, R.string.getting_friendship_from_server, Toast.LENGTH_SHORT).show();
            return;
        }
    	Message msg = handler.obtainMessage(FACEBOOK_FRIENDS_GET);
    	msg.sendToTarget();
    }
    
    //reget the wall
    @Override
    protected void loadRefresh()
    {
    	Log.d(TAG, "call refresh="+this);
    	if(selectType ==0 )
    	{
    	    if(this.isInProcess() == true)
    	    {
    	        showToast();
    	        return;
    	    }
    	}
    	else
    	{
    	    if(inloadPage == true)
    	    {
    	        showToast();
    	        return;
    	    }
    	}
    	if(facebookA != null)
    	{
    		if(selectType == 0)
    		{
    	        lauchGeFriends();
    		}
    		else
    		{
    	        lauchGetPages();
    		}
    	}
    }
    
    @Override
    public void onLogin() 
	{		
    	super.onLogin();
    	
    	Log.d(TAG, "call onLogin="+this);
    	
    	if(facebookA != null)
    	{
    		lauchGeFriends();
    	}
	}
    
    @Override
    protected void doAfterLoginNothing()
    {
    	Log.d(TAG, "after login");
    	FacebookNewFriendsActivity.this.setResult(RESULT_CANCELED);
    	FacebookNewFriendsActivity.this.finish();
    }
    
    @Override
    protected void doAfterLogin()
    {
    	Log.d(TAG, "after login");
    	//try to get the session
    	perm_session = loginHelper.getPermanentSesstion(this);
    	if(perm_session == null)
    	{
    		 
    	}
    	else
    	{
    		facebookA = new AsyncFacebook(perm_session);
    		perm_session.attachActivity(this);
    		lauchGeFriends();
    	}
    }
    
    //context menu, post to wall
    @Override
    public void postToWall(long uid)
    {
    	Intent wallintent = new Intent(this,FacebookStatusUpdateActivity.class);
		wallintent.putExtra("fuid", new Long(uid));
		startActivity(wallintent);
    }
        
    final static int FACEBOOK_FRIENDS_GET    =0;
    final static int FACEBOOK_FRIEND_UI      =1;
    final static int FACEBOOK_FRIENDS_GET_END=2;
    final static int FACEBOOK_FRIENDS_GET_DB =3;
    final static int FACEBOOK_FRIENDS_ADD_END=5;
    
    final static int FACEBOOK_PAGE_GET     =10;
    final static int FACEBOOK_PAGE_UI      =11;
    final static int FACEBOOK_PAGES_GET_END=12;
    
    final static int SYNC_ADDRESS          = 20;
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
                case SYNC_ADDRESS:
                {
                	if(msg.getData().getBoolean("RESULT", false) == true)
                	{
                		doSyncInBackground();
                	}
                	break;
                }
	            case FACEBOOK_PAGE_GET:                	
	            {          
	                getPageFromServer();
	            	break;
	            }        
	        	case FACEBOOK_PAGES_GET_END:
	            {
	            	if(isFinishing() == true)
	            	{
	            		clearAsyncFacebook(true);
	            	}
	            	
	                 if(msg.getData().getBoolean(RESULT, false) == false)
	                 {   
	                     Log.d(TAG, "Fail to get page from web");
	                     end(); 
	                     Toast.makeText(mContext, R.string.fail_get_latest_page, Toast.LENGTH_SHORT).show();                        
	                 }
	                 else
	                 {
	                     int size = msg.getData().getInt("size");
	                     if(size == limit)
	                     {
	                         Log.d(TAG, "data size is "+ size +" offset is "+ offset+" has more data.continue get data from webserver ");
	                         offset += size;
	                         handler.obtainMessage(FACEBOOK_PAGE_GET).sendToTarget();
	                     }
	                     else
	                     {
	                         end(); 
	                         offset = 0;
	                     }
	                 }
	                 break;
	            }            	
	            case FACEBOOK_PAGE_UI:
	            {
	                Log.d(TAG, "call FACEBOOK_PAGE_UI");
	                if(fcursor != null)
	                {
	                    fcursor.close();
	                    fcursor = null;
	                }                    
	                fcursor = orm.getAllPageCursor();                
	                
	                if(fcursor != null && fcursor.getCount() > 0 && fcursor.moveToFirst())
	                {
	                	current_postion.setVisibility(View.VISIBLE);
	                	Page suser = orm.formatPage(fcursor);
                        if(isEmpty(suser.name) == false)
	                    current_postion.setText(suser.name.substring(0, 1));
	                }
	                else
	                {
	                	current_postion.setVisibility(View.GONE);
	                }
	                
	                PageAdapter adapter = new PageAdapter(FacebookNewFriendsActivity.this, fcursor);
	                friendList.setAdapter(adapter);
	            	break;
	            }
            	case FACEBOOK_FRIENDS_GET:                	
                {          
                    inprocess = true;
                    begin();
                    
                    Message backMsg = this.obtainMessage( FACEBOOK_FRIENDS_GET_END);
                    backMsg.getData().putLong("hisuid", toFindFriendUID);
                    //let service get Facebook user 
                    SNSService.getSNSService().getContactService().alarmFacebookUserComming(backMsg);
                    
                	break;
                }        
            	case FACEBOOK_FRIENDS_GET_END:
                {
                     end();                     
                     inprocess = false;
                     
                     if(msg.getData().getBoolean("RESULT", false) == false)
                     {   
                         Log.d(TAG, "Fail to get friend from web");
                         Toast.makeText(mContext, R.string.fail_get_latest_friendship, Toast.LENGTH_SHORT).show();                        
                     }
                     else
                     {
                    	 //no need show UI for exit
                    	 if(donotcallnetwork == false && selectType == FRIENDS)
                    	 {
                             this.obtainMessage(FACEBOOK_FRIEND_UI).sendToTarget();
                    	 }
                     }
                     break;
                }
            	case FACEBOOK_FRIENDS_ADD_END:
            	{
            		end();
            		if(isFinishing() == true)
            		{
            			clearAsyncFacebook(true);
            		}
            		break;
            	}
                case FACEBOOK_FRIEND_UI:
                {
                    if(fcursor != null)
                    {
                        fcursor.close();
                        fcursor = null;
                    }                    
                    fcursor = orm.getAllFacebookSimpleUsersCursor(toFindFriendUID);
                	
                    
                    if(fcursor != null && fcursor.getCount() > 0 && fcursor.moveToFirst())
                    {
                    	current_postion.setVisibility(View.VISIBLE);
                    	FacebookUser.SimpleFBUser suser = orm.formatSimpleFacebookUser(fcursor);
                        if(isEmpty(suser.name) == false)
                        current_postion.setText(suser.name.substring(0, 1));
                    }
                    else
                    {
                    	current_postion.setVisibility(View.GONE);
                    }
                    
                    FacebookFriendCursorAdapter adapter = new FacebookFriendCursorAdapter(FacebookNewFriendsActivity.this, fcursor, false, false);
                    friendList.setAdapter(adapter);
                	break;
                }                        
            }
        }
    }
    
    public class PageHandler implements ObjectHandler
    {
        public void process(Object obj) 
        {
            if(Page.class.isInstance(obj))
            {
                orm.insertPage((Page)obj);
            }           
        }       
    }
    
    @Override
    protected void onDestroy() 
    {
        if(fcursor != null)
        {
            try{
                fcursor.close();
            }catch(Exception ne){}
            fcursor = null;
        }
        
        clearAsyncFacebook(true);
        super.onDestroy();
    }
    
    public void getPageFromServer() 
    {	
        if(inloadPage == true)
        {
            Log.d(TAG, "I am in getting page, please wait");
            return;
        }
        begin();
        Log.d(TAG, "before get getFacebookPage");
       // notifyLoading();  
        synchronized(mLock)
        {
        	inloadPage = true;
        }
        
        if(perm_session == null)
        {
             perm_session = loginHelper.getPermanentSesstion(this);
             if(perm_session != null)
             {
                 perm_session.attachActivity(this);
             }
             if(facebookA == null)
             {
                 facebookA = new AsyncFacebook(perm_session);
             }             
        } 
        
        if(facebookA != null)
        {
            facebookA.setSession(perm_session);
            facebookA.getPageAsync(limit,offset,perm_session.getLogerInUserID(),pageHandler, new FacebookAdapter(){
                @Override
                public void getPage(int pagecount) 
                {
                    synchronized(mLock)
                    {
                    	inloadPage = false;
                    }
                    
                    if(donotcallnetwork == false )//I am still alive
                    {
                    	handler.obtainMessage(FACEBOOK_PAGE_UI).sendToTarget();
                        //cancelNotify();
                    }                    
                    
                    Message msd = handler.obtainMessage(FACEBOOK_PAGES_GET_END);
                    msd.getData().putBoolean(RESULT, true);
                    msd.getData().putInt("size", pagecount);
                    handler.sendMessage(msd);
                }

                @Override
                public void onException(FacebookException te, int method) {
                    synchronized(mLock)
                    {
                    	inloadPage = false;
                    }
                    if(donotcallnetwork == false )//I am still alive
                    {   
                         //cancelNotify();
                    }   
                    //get from Database
                    Message msd = handler.obtainMessage(FACEBOOK_PAGES_GET_END);
                    msd.getData().putBoolean(RESULT, false);
                    handler.sendMessage(msd);
                }
            });
         }
	}
    
    public class PageAdapter extends BaseAdapter 
    {
    	private Cursor cursor;
        public PageAdapter(Context con, Cursor pages) 
        {
        	cursor = pages;
        }

    	public int getCount() 
    	{
    		if(cursor.requery() == true)
    		{
    		    return cursor.getCount();
    		}
    		return 0;
    	}
    	public Object getItem(int pos) 
    	{	
    		if(cursor.requery() == true)
    		{
    			if(cursor.moveToFirst())
    			{
    				if(cursor.moveToPosition(pos))
    				{
    					return orm.formatPage(cursor);
    				}
    			}    			
    		}
    	    return null;
    	}
    	
    	public long getItemId(int pos) 
    	{
    		if(cursor.requery() == true)
    		{
    			if(cursor.moveToFirst())
    			{
    				if(cursor.moveToPosition(pos))
    				{
    					return cursor.getLong(cursor.getColumnIndex(PageCol.PAGEID));
    				}
    			}
    		}
    		return -1;
    	}
    	public View getView(int position, View convertView, ViewGroup arg2) 
    	{		
    		 if (position < 0 || position >= getCount()) 
    		 {
                 return null;    
             }
             
    		 PageItemView v=null;
    	
    		 Page di = (Page)getItem(position);
    		 if(di != null)
    		 {
	             if (convertView == null /*|| convertView instanceof SeparatorView*/) {
	                 v = new PageItemView(mContext, di);
	             } else {
	                  v = (PageItemView) convertView;
                  v.setItem(di);
	             }
    		 }
             //v.chooseFriendListener();
             return v;
    	}
    }

    public class PageItemView extends SNSItemView {

    	private String TAG="PageItemView";
    	ImageView    imageView;
    	TextView     txtView;
    	Page         info;
    	
    	public Page getpage()
    	{
    		return info;
    	}
    	public PageItemView(Context context, Page di) {
            super(context);
    		
    	    info = di;    		
    	    Log.d(TAG, "call PageItemView");
    		
    	    init();
    	}

    	//create the view
    	private void init() 
    	{		
    		Log.d(TAG,  "call init");
    		LayoutInflater factory = LayoutInflater.from(mContext);
    		removeAllViews();
    		
    		//child 1
    		View v  = factory.inflate(R.layout.facebook_new_friend_item, null);		
    		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
    		addView(v);
    		
    		imageView  = (ImageView)v.findViewById(R.id.facebook_friends_img_ui);
    		txtView    = (TextView)v.findViewById(R.id.facebook_friend_user_name);
    		
    		setUI();
    	}

		private void setUI() 
		{
			txtView.setText(info.name);
			
			String filepath = TwitterHelper.getImagePathFromURL_noFetch(info.pic_square);
			if(new File(filepath).exists() == true)
			{
				try
				{
					Bitmap tmp = BitmapFactory.decodeFile(filepath);
	    			if(tmp != null)
	    			{
	    			 imageView.setImageBitmap(tmp);
	    			}
				}catch(Exception ne){}
			}
			else
			{
				imageView.setImageResource(R.drawable.no_avatar);
			}			
		}
		
		@Override
		public String getText() {			
		    return null;
		}
		
		public void setItem(Page page)
		{
			info.despose();
			info = null;
			info = page;
			setUI();
		}
    }
    
    private void notifyLoading() 
    {
    	if(selectType == 0)
    	{
    	    notify.notifyOnce(R.string.facebook_friends_loading, R.drawable.facebook_logo, 30*1000);
    	}
    	else
    	{
    		notify.notifyOnce(R.string.facebook_pages_loading, R.drawable.facebook_logo, 30*1000);
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
}
