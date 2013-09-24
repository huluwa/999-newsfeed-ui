package com.msocial.facebook.ui;

import com.msocial.facebook.R;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.providers.SocialORM.FacebookUsersCol;
import com.msocial.facebook.service.SNSService;
import com.msocial.facebook.ui.view.ImageCacheManager;
import com.msocial.facebook.ui.view.SNSItemView;
import com.msocial.facebook.ui.view.ImageCacheManager.ImageCache;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FBNotifications;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.MailboxThread;
import oms.sns.service.facebook.model.Notifications;
import oms.sns.service.facebook.model.Notifications.AppInfo;
import oms.sns.service.facebook.model.Notifications.Notification;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.view.animation.Animation;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.view.animation.AnimationUtils;

public class FacebookProfileActivity extends FacebookBaseActivity
{
	final String TAG = "FacebookProfileActivity";
    GridView mGrid;
    GridView mGrid1;
    GridView mGrid2;
    View     facebook_search_span;
    View     facebook_bottom_span;
    
    Button   facebook_bottom_region;    
    TextView facebook_bottom_region_notification_size;
    TextView facebook_inbox_unread_size_tv;
    TextView facebook_request_unread_size_tv;
    TextView facebook_request_unread_size_event;
    LinearLayout inboxlayout;
    LinearLayout requestlayout; 
    LinearLayout eventlayout;
    
    EditText embedded_search_text_editor;
    Cursor   users ;
    Button   facebook_current_pos;
    
    ViewGroup mContainer;
    ImageView mImageView;
    int unread=0;
    boolean doSetTitleAfterLoginInOnResume = false;
    
    ViewFlipper flipper;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
        loadApps(); // do this in onresume?
        
        setContentView(R.layout.facebook_start_ui);
        
        mContainer = (ViewGroup) findViewById(R.id.container);        
        mContainer.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
        
        //this.getWindow().setBackgroundDrawable();
        mGrid = (GridView) findViewById(R.id.myGrid);
        mGrid.setAdapter(new AppsAdapter(this.getApplicationContext(), mApps));
        mGrid.setFocusableInTouchMode(true);
        mGrid.setFocusable(true);
        mGrid.setSelected(true);
        mGrid.setClickable(true);  
        mGrid.setOnCreateContextMenuListener(this);
        mGrid.setOnItemClickListener(listItemClickListener);
        mGrid.setVisibility(View.VISIBLE);
        
        mGrid1 = (GridView) findViewById(R.id.myGrid_shotcut1);        
        mGrid1.setFocusableInTouchMode(true);
        mGrid1.setFocusable(true);
        mGrid1.setSelected(true);
        mGrid1.setClickable(true);  
        mGrid1.setOnCreateContextMenuListener(this);
        mGrid1.setOnItemClickListener(profileClickListener);        
        users = orm.getAllShoutCutFacebookUsersCursor();
        refreshShortCut();
     
        /*
        mGrid2 = (GridView) findViewById(R.id.myGrid_shotcut2);        
        mGrid2.setFocusableInTouchMode(true);
        mGrid2.setFocusable(true);
        mGrid2.setSelected(true);
        mGrid2.setClickable(true);  
        mGrid2.setOnCreateContextMenuListener(this);
        mGrid2.setOnItemClickListener(profileClickListener);
        mGrid2.setVisibility(View.VISIBLE);
        */        
        
        flipper = (ViewFlipper)findViewById(R.id.facebook_start_ui_flipper);
        
        facebook_bottom_region = (Button)this.findViewById(R.id.facebook_bottom_region);
        facebook_bottom_region.setText(R.string.menu_title_notifications);
        facebook_bottom_region.setOnClickListener(showNotificationClick);
        
        facebook_current_pos = (Button)this.findViewById(R.id.facebook_current_pos);
        setPosition(true);
        
        facebook_bottom_region_notification_size = (TextView)this.findViewById(R.id.facebook_bottom_region_notification_size);
                
        inboxlayout =  (LinearLayout)findViewById(R.id.inboxlayout);
        facebook_inbox_unread_size_tv = (TextView)findViewById(R.id.facebook_inbox_unread_size_tv);
                
        requestlayout = (LinearLayout)findViewById(R.id.requestlayout);
        facebook_request_unread_size_tv = (TextView)findViewById(R.id.facebook_request_unread_size_tv);
        
        eventlayout = (LinearLayout)findViewById(R.id.eventlayout);
        facebook_request_unread_size_event = (TextView)findViewById(R.id.facebook_request_unread_size_event);        
        
        
        facebook_search_span = this.findViewById(R.id.facebook_search_span);
        facebook_bottom_span = this.findViewById(R.id.facebook_bottom_span);
        
        embedded_search_text_editor = (EditText)this.findViewById(R.id.embedded_search_text_editor);
        
        //embedded_search_text_editor.setOnFocusChangeListener(focusChangeListener);
        embedded_search_text_editor.clearFocus();
        //embedded_search_text_editor.setFocusable(false);        
        embedded_search_text_editor.setOnClickListener(clickListener);
        
        Intent in = new Intent(this.getApplicationContext(), SNSService.class);            
        startService(in);
        
        //don't let the input has focus
        mGrid.requestFocus();
        //
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
            perm_session = loginHelper.getPermanentSesstion(this);
            if(perm_session != null)
            {
                //perm_session.attachActivity(this);
                facebookA = new AsyncFacebook(perm_session);
                handler.obtainMessage(FACEBOOK_NOTIFICATION_GET).sendToTarget();
                reschedule();
            }
            else
            {             
                launchFacebookLogin();
            }
        }
        
        setTitle();
        setTitle(title);
    }

    private Animation inFromRightAnimation() {
        Animation inFromRight = new TranslateAnimation(
                                        Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
                                        Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
        inFromRight.setDuration(500);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }
   
    private Animation outToLeftAnimation() {
        Animation outtoLeft = new TranslateAnimation(
                                        Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
                                        Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
        outtoLeft.setDuration(500);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }
     
    private Animation inFromLeftAnimation() {
        Animation inFromLeft = new TranslateAnimation(
                                        Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
                                        Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
        inFromLeft.setDuration(500);
        inFromLeft.setInterpolator(new AccelerateInterpolator());
        return inFromLeft;
    }
   
    private Animation outToRightAnimation() {
        Animation outtoRight = new TranslateAnimation(
                                        Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f,
                                        Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
        outtoRight.setDuration(500);
        outtoRight.setInterpolator(new AccelerateInterpolator());
        return outtoRight;
    }
    
    private void setPosition(boolean inLeft)
    {
    	if(inLeft)
    	    facebook_current_pos.setBackgroundResource(R.drawable.profile_on);
    	else
    		facebook_current_pos.setBackgroundResource(R.drawable.profile_off);
    }
   
    //begin do
    public void beginNavToRight() 
    {
    	if(false == mGrid.isFocused()){
    	    return;
    	}
    	
    	//setTitle
    	String tempTitle = getString(R.string.facebook_recent_visit_page_title);
    	setTitle(tempTitle);
    	setPosition(false);
    	flipper.setInAnimation(inFromRightAnimation());
    	flipper.setOutAnimation(outToLeftAnimation());
    	flipper.showNext();
    }

    //back
    public void backtoFirst() 
    {
        if(mGrid.isFocused()){
            return;
        }
        setTitle(title);
    	setPosition(true);    	
    	flipper.setInAnimation(inFromLeftAnimation());
        flipper.setOutAnimation(outToRightAnimation());
        flipper.showPrevious();
    }
    
    @Override protected void onDestroy() 
    {
        try{
        	if(users != null)
        	{
        		users.close();
        		users = null;
        	}
        }catch(Exception ne){}
        
        int count = this.mGrid.getChildCount();
        if(count>0){
        	for(int i=0;i<count;i++){
        		View tmpView = mGrid.getChildAt(i);
        		if(ActivityItemView.class.isInstance(tmpView))
        		{
        			((ActivityItemView)tmpView).revoke();
        		}
        	}
        } 
        
        clearAsyncFacebook(true);
        super.onDestroy();
    }
    
    public void refreshShortCut() 
    {
        if(users == null || users.moveToFirst() == false)
        {
            users = orm.getAllShoutCutFacebookUsersCursor();
        }
        
    	mGrid1.setAdapter(null);    	
        ProfileAdapter pa = new ProfileAdapter(this, users);
        mGrid1.setAdapter(pa);
	}
    
    float mDownMotionX;
    private VelocityTracker mVelocityTracker;
    @Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
    	final int action = ev.getAction();
    	
    	if (mVelocityTracker == null) {
             mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
         
    	switch (action) {
            case MotionEvent.ACTION_MOVE:
            	float span = ev.getX() -  mDownMotionX;            	  
            	if(Math.abs(span) > 100)
            	{
            		if(span < 0)
            			goNextPage();
            		else
            			goPrePage();
            	}
        	break;
            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
            	mDownMotionX = ev.getX();
            	break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            	mVelocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) mVelocityTracker.getXVelocity();
                if(Math.abs(velocityX) > 200)
                {
                	if(velocityX < 0)
                		goNextPage();
                	else
                		goPrePage();
                }
            	mVelocityTracker.clear();
            	break;
    	}
		return super.dispatchTouchEvent(ev);
	}

	@Override 
    protected boolean  goNextPage()
    {
        beginNavToRight();
        return true;
    }
    
    @Override
	protected boolean  goPrePage()
	{
    	backtoFirst();
    	return true;
	}
    
    
    AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int position,long ID) 
		{
			if(ActivityItemView.class.isInstance(v))
			{
				ActivityInfo isnfo = mApps.get(v.getId());
				Intent intent;
				
				int pos = isnfo.name.lastIndexOf(".ui.");
				String cname = isnfo.name.substring(pos+1);
				intent = new Intent();		
				intent.setClassName("com.msocial.facebook", isnfo.name);
				if(isnfo.name.contains("FacebookAccountActivity"))
				{
					intent.putExtra("comefrommyself", true);
				}
				else if(isnfo.name.contains("AboutActivity"))
				{
					intent.putExtra("forabout", true);
				}
				
		        FacebookProfileActivity.this.startActivityForResult(intent, 100);
			}
		}
	};
	
	AdapterView.OnItemClickListener profileClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int position,long ID) 
		{
			if(ProfileItemView.class.isInstance(v))
			{
				ProfileItemView pv = (ProfileItemView)v;
				FacebookUser.SimpleFBUser user = pv.getUser();
				Intent intent = new Intent(mContext, FacebookAccountActivity.class);
				intent.putExtra("uid",      user.uid);
				intent.putExtra("username", user.name);
				intent.putExtra("imageurl", user.pic_square);					
				((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
			}
		}
	};
	
   
	/*
	@Override
	public void titleSelected() 
    {		
		super.titleSelected();
		
		Intent intent = new Intent(FacebookProfileActivity.this, FacebookStatusUpdateActivity.class);            
		startActivityForResult(intent, FACEBOOK_STATUS_UPDATE_UI);
	}
	*/
    
    @Override protected void doAfterLoginNothing()
    {
    	super.doAfterLoginNothing();
    	
    	FacebookProfileActivity.this.setResult(RESULT_CANCELED);
    	FacebookProfileActivity.this.finish();    
    }
    
    @Override
    protected void doAfterLogin()
    {
    	Log.d(TAG, "after login");
    	//try to get the session
    	doSetTitleAfterLoginInOnResume = true;
    	perm_session = loginHelper.getPermanentSesstion(this);
    	if(perm_session == null)
    	{
    	    //re-launch the login UI
    	    Log.d(TAG, "fail to get permanent session");
    	    Toast.makeText(this, R.string.facebook_no_valid_session, Toast.LENGTH_SHORT).show();    		
    	}
    	else
    	{
    	    facebookA = new AsyncFacebook(perm_session);
    	    perm_session.attachActivity(this);
    		
    	    //handler.obtainMessage(FACEBOOK_NOTIFICATION_GET).sendToTarget();
    	}   
    }
    
    View.OnClickListener clickListener = new View.OnClickListener()
    {
		public void onClick(View v) 
		{
	        showSearch();
		}
    };
	
    View.OnFocusChangeListener focusChangeListener = new  View.OnFocusChangeListener()
    {
		public void onFocusChange(View v, boolean hasFocus) 
		{
		    if(hasFocus == true)
		    {
		        showSearch();
	        }
		}    	
    };
    
    protected void showSearch() 
    {		
    	 Intent intent = new Intent(FacebookProfileActivity.this, FacebookPhonebookActivity.class);
         //Intent intent = new Intent(FacebookProfileActivity.this, FacebookSearchActivity.class);
	     FacebookProfileActivity.this.startActivity(intent);
    }
    
    View.OnClickListener showNotificationClick = new View.OnClickListener()
    {
        public void onClick(View v) 
		{
		    Log.d(TAG, "showNotificationClick you click first one=");
				 
		    Intent intent = new Intent(FacebookProfileActivity.this, FacebookNotificationManActivity.class);
		    FacebookProfileActivity.this.startActivityForResult(intent, FACEBOOK_NOTIFICATION_RESULT);
		}
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) 
    {
        switch (requestCode) 
        {
	        case FACEBOOK_NOTIFICATION_RESULT: 
	        {
	        	//make the unread to read
	        	facebook_bottom_region.setText(R.string.menu_title_notifications);
				facebook_bottom_region_notification_size.setVisibility(View.GONE);
				facebook_bottom_region.setCompoundDrawables(null, null, null, null);
	        	return ;
	        }
        }
        
        super.onActivityResult(requestCode, resultCode, intent);
    }
    
    private List<ActivityInfo> mApps = new ArrayList<ActivityInfo>();

    private void loadApps() 
    {
        PackageManager pm = getPackageManager();
        PackageInfo pinfo = null;
        try {
            pinfo = pm.getPackageInfo("com.msocial.facebook", PackageManager.GET_ACTIVITIES);
            if (pinfo != null) 
            {
                ActivityInfo[] tmpAppsArray = new ActivityInfo[15];
                for (ActivityInfo act : pinfo.activities) 
                {
                	if(act.name.contains(".ui.FacebookStreamActivity"))
                	{
                	    tmpAppsArray[0] = act;
                	}
                	else if(act.name.contains(".ui.FacebookAccountActivity"))
                    {
                	    tmpAppsArray[1] = act;
                    }
                	else if(act.name.contains(".ui.FacebookAlbumActivity"))
                	{
                	    tmpAppsArray[2] = act;
                	}
                    else if(act.name.contains(".ui.FacebookNewFriendsActivity"))
                    {
                        tmpAppsArray[3] = act;
                    }
                    else if(act.name.contains(".ui.FacebookMessageActivity"))
                    {
                        tmpAppsArray[4] = act;
                    }
                    else if(act.name.contains(".ui.FacebookNotificationsActivity"))
                    {
                        tmpAppsArray[5] = act;
                    }
                    else if(act.name.contains(".ui.FacebookEventActivity"))
                    {
                        tmpAppsArray[6] = act;
                    }
                    else if(act.name.contains(".ui.FacebookNotesActivity"))
                    {
                        tmpAppsArray[7] = act;
                    }
                    else if(act.name.contains(".FacebookNotificationManActivity"))
                    {
                        tmpAppsArray[8] = act;
                    }
                    else if(act.name.contains(".FacebookPhonebookActivity"))
                    {
                        tmpAppsArray[9] = act;
                    }
                    else if(act.name.contains(".FacebookLocationUpdateActivity"))
                    {
                        tmpAppsArray[10] = act;
                    }
                    else if(act.name.contains(".FacebookStatusUpdateActivity"))
                    {
                        tmpAppsArray[11] = act;
                    }
                    else if(act.name.contains(".FacebookSettingPreference"))
                    {
                        tmpAppsArray[12] = act;
                    }
                    else if(act.name.contains(".FacebookShareActivity"))
                    {
                        tmpAppsArray[13] = act;
                    }
                    else if(act.name.contains(".AboutActivity"))
                    {
                        tmpAppsArray[14] = act;
                    }                	
                    else
                    {
                    	/*act.name.contains(".ui.FacebookLocationUpdateActivity") ||
                        act.name.contains(".ui.FacebookPhonebookActivity")      ||
                        act.name.contains(".ui.FacebookSettingPreference")      ||                	                  		
                        act.name.contains(".ui.FacebookMainActivity") ||
                        act.name.contains(".ui.FacebookNotificationManActivity") ||
                        act.name.contains(".ui.FacebookNotificationsActivity") ||
                        act.name.contains(".ui.FacebookStatusUpdateActivity") || 
                        act.name.contains(".ui.AboutActivity")*/   
                    }               	    
                }
                
                sort(tmpAppsArray);
            }
        }catch (Exception e) {}
        
    }

    private void sort(ActivityInfo[] appArray)
    {
        for(int i=0; i<appArray.length; i++)
        {
            mApps.add(appArray[i]);
        }
        
        appArray = null;
    }
   

    public static class AppsAdapter extends BaseAdapter 
    {
    	private List<ActivityInfo> _mApps = new ArrayList<ActivityInfo>();
    	private Context            _context;
        public AppsAdapter(Context con, List<ActivityInfo> apps) 
        {	
        	_context = con.getApplicationContext();
        	_mApps.addAll(apps);
        }

        public View getView(int position, View convertView, ViewGroup parent) 
        {
            ActivityInfo info = _mApps.get(position);    
            ActivityItemView i;
            if (convertView == null) 
            {
                i = new ActivityItemView(_context, info);
            } 
            else 
            {
                i = (ActivityItemView) convertView;                
            }
            
            i.setId(position);
            i.setImageDrawable(info.loadIcon(_context.getPackageManager()));
            
            i.setText("");
            try
            {
	        String name = _context.getString(info.labelRes);
	        if(name != null)
	        {
	            i.setText(name);
	        }
            }catch(NotFoundException ne){}
            
            return i;
        }

        public final int getCount() {
            return _mApps.size();
        }

        public final Object getItem(int position) {
            return _mApps.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }
    }
    

    
    public static class ActivityItemView extends SNSItemView {

    	private String TAG="ActivityItemView";
    	ImageView    imageView;
    	TextView     txtView;
    	ActivityInfo info;
    	Drawable     appdrawable;
    	
    	public ActivityItemView(Context context, ActivityInfo di) {
            super(context.getApplicationContext());
    		
    	    info = di;    		
    	    Log.d(TAG, "call ActivityItemView");
    		
    	    init();
    	}


    	public void setText(String labelRes) {
	    	 txtView.setText(labelRes);
		}
	
	
    	public void revoke()
    	{
    		if(appdrawable != null)
			{
				appdrawable.setCallback(null);
			}
    	}
    	
    	
    	
		public void setImageDrawable(Drawable loadIcon) 
		{
			if(appdrawable != null)
			{
				appdrawable.setCallback(null);
			}
			
			appdrawable = loadIcon;
	    	imageView.setImageDrawable(appdrawable);
		}

	//create the view
    	private void init() 
    	{		
    		Log.d(TAG,  "call init");
    		LayoutInflater factory = LayoutInflater.from(mContext.getApplicationContext());
    		removeAllViews();
    		
    		//child 1
    		View v = factory.inflate(R.layout.facebook_thumbnail_item, null);    		
    		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
    		addView(v);
    		
    		txtView = (TextView)v.findViewById(R.id.activity_name);
    		imageView = (ImageView)v.findViewById(R.id.activity_icon);
    	}

		@Override
		public String getText() {			
		    return null;
		}    	
    }
    
    
    public class ProfileAdapter extends BaseAdapter 
    {
    	private Cursor cursor;
        public ProfileAdapter(Context con, List<FacebookUser> users) 
        {
        	mUsersItems = users;
        }
        
        public ProfileAdapter(Context con, Cursor users) 
        {
        	cursor = users;
        }

    	public int getCount() 
    	{
    		if(cursor !=null && cursor.requery() == true)
    		{
    		    return cursor.getCount();
    		}
    		return 0;
    	}
    	public Object getItem(int pos) {	
    		if(cursor != null &&cursor.requery() == true)
    		{
    			if(cursor.moveToFirst())
    			{
    				if(cursor.moveToPosition(pos))
    				{
    					return orm.formatSimpleFacebookUser(cursor);
    				}
    			}    			
    		}    		
    		
    	    return null;
    	}
    	
    	public long getItemId(int pos) 
    	{
    		if(cursor != null &&cursor.requery() == true)
    		{
    			if(cursor.moveToFirst())
    			{
    				if(cursor.moveToPosition(pos))
    				{
    					return cursor.getLong(cursor.getColumnIndex(FacebookUsersCol.UID));
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
             
    		 ProfileItemView v=null;
    	
    		 FacebookUser.SimpleFBUser di = (FacebookUser.SimpleFBUser)getItem(position);
    		 if(di != null)
    		 {
	             if (convertView == null /*|| convertView instanceof SeparatorView*/) {
	                 v = new ProfileItemView(mContext, di);
	             } else {
	                 v = (ProfileItemView) convertView;
                     v.setItem(di);
	             }
    		 }
             //v.chooseFriendListener();
             return v;
    	}	
    	private List<FacebookUser> mUsersItems;
    }
    
    public class ProfileItemView extends SNSItemView {

    	private String TAG="ProfileItemView";
    	ImageView    imageView;
    	TextView     txtView;
    	ImageView    bottomlineView;
    	
    	FacebookUser.SimpleFBUser info;
    	
    	public FacebookUser.SimpleFBUser getUser()
    	{
    		return info;
    	}
    	
    	public ProfileItemView(Context context, FacebookUser.SimpleFBUser di) {
            super(context);
    		
    	    info = di;    		
    	    Log.d(TAG, "call ProfileItemView");
    		
    	    init();
    	}
    	
    	//create the view
    	private void init() 
    	{		
    		Log.d(TAG,  "call init");
    		LayoutInflater factory = LayoutInflater.from(mContext);
    		removeAllViews();
    		
    		//child 1
    		View v = factory.inflate(R.layout.facebook_thumbnail_item, null);
    		//View v = factory.inflate(R.layout.facebook_thumbnail_profile_item, null);
    		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
    		addView(v);
    		
    		txtView = (TextView)v.findViewById(R.id.activity_name);
    		imageView = (ImageView)v.findViewById(R.id.activity_icon);
    		bottomlineView = (ImageView)v.findViewById(R.id.activity_sep);
    		bottomlineView.setVisibility(View.GONE);
    		
    		setUI();
    	}

		private void setUI() 
		{
			txtView.setText(info.name);
			
			ImageCache cache = ImageCacheManager.instance().getCache(info.pic_square);
            if(cache != null)
            {
                if(cache.bmp != null)
                {
                    imageView.setImageBitmap(cache.bmp);
                }
            }
            else
            {
                //I don't want cache the profile image
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
		}
		
		@Override
		public String getText() {			
		    return null;
		}
		
		public void setItem(FacebookUser.SimpleFBUser user)
		{
			info.despose();
			info = null;
			info = user;
			setUI();
		}
    }

    @Override
    protected void createHandler() {
        handler = new NotesHanlder();
    }
    
    protected  static final int FACEBOOK_NOTIFICATION_GET         = 1;
    protected  static final int FACEBOOK_NOTIFICATION_GET_END     = 2;
    
    private class NotesHanlder extends Handler
    {
	    public NotesHanlder()
        {
            super();            
            Log.d(TAG, "new NotesHanlder");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case FACEBOOK_NOTIFICATION_GET:
	            {
	            	notesGetNotification();
	            	//notesGet();
	            	break;
	            }
	            case FACEBOOK_NOTIFICATION_GET_END:
	            {
	            	if(isFinishing() == true)
	            	{
	            		clearAsyncFacebook(true);
	            	}
	            	else
	            	{
	            	    processNotificationSummary(notifies);
	            	}
	            	
	            	break;
	            }
            }
        }
    }
    
    public void reschedule()
	{
        Log.d(TAG, "reschedule at "+ new Date().toLocaleString());
		long nexttime = System.currentTimeMillis()+ 100;		
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent();
        i.setClassName("com.msocial.facebook", "com.msocial.facebook.ui.FacebookProfileActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);        
		alarmMgr.set(AlarmManager.RTC, nexttime, pi);		
	}
    
    
    public void alarmComming(boolean force)
	{
         if(orm.isNotificationEnable())
         {
			Log.d(TAG, " &&&&&&&&&&&  time it out="+ new Date().toLocaleString());
			Message msg = handler.obtainMessage(FACEBOOK_NOTIFICATION_GET);
			msg.sendToTarget();
			
			AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
			long nexttime = System.currentTimeMillis()+ ((force==true)?100:(5*60*1000));	
			
			Intent i = new Intent();
	        i.setClassName("com.msocial.facebook", "com.msocial.facebook.ui.FacebookProfileActivity");
	        i.setAction(ACTION_CHECK_CONTECT);
	        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);        
			alarmMgr.set(AlarmManager.RTC, nexttime, pi);
         }        
	}	
    
    @Override
	protected void onNewIntent(Intent intent) 
	{		
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent="+intent);
		if(intent.getAction() != null && intent.getAction().equals(ACTION_CHECK_CONTECT))
		{
		    setTitle();
		    setTitle(title);
			setIntent(intent);
		}
	}

    
    @Override protected void onResume() 
    {
        super.onResume();	
    	if(doSetTitleAfterLoginInOnResume == true)
    	{
    		String temptitle=getString(R.string.facebook_main_menu);		
    		FacebookUser.SimpleFBUser user = orm.getSimpleFacebookUser(getLoginUserID());
    		if(user != null)
    		{
    			temptitle = user.name;
    			user.despose();
    			user = null;
    		}
    		setTitle(temptitle);
    		setTitle();
    		
    		if(notifies != null)
        	{
        		 notifies.msg.unread = 0;
        		 notifies.entInvite.uids.clear();
        		 notifies.poke.unread = 0;
        		 notifies.grdInvite.uids.clear();
        		 notifies.frdRequest.uids.clear();
        		 processNotificationSummary(notifies);
        	}
    	}
    	doSetTitleAfterLoginInOnResume = false;
	    mGrid1.setAdapter(null);
		if(users != null)
		{
			try{
			    users.close();
			}catch(Exception ne){}
		}
		users = orm.getAllShoutCutFacebookUsersCursor();    		
        ProfileAdapter pa = new ProfileAdapter(this, users);
        mGrid1.setAdapter(pa);
    	
    	//cancel first
    	AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
    	Intent i = new Intent();
        i.setClassName("com.msocial.facebook", "com.msocial.facebook.ui.FacebookProfileActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pi);
         
        //resume to call
        alarmComming(false);        
    }
    
    @Override protected void onPause() 
    {   
        super.onPause();
        
        try{
        	if(users != null)
        	{
        		users.close();
        		users = null;
        	}
        	
        	mGrid1.setAdapter(null);
        }catch(Exception ne){}
        
        
        AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
        
        Intent i = new Intent();
        i.setClassName("com.msocial.facebook", "com.msocial.facebook.ui.FacebookProfileActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pi);
        
        Log.d(TAG, "Cancel alarm");
    }
    
    
    private void vibratePhone()
    {
    	if(orm.getNotificationVibrate())
    	{
    	    final Vibrator vib  = (android.os.Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(2*1000);
    	}
    }
    
    private void FlashLED()
    {
    	if(orm.getNotificationLED())
    	{
    		Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            findViewById(R.id.container).startAnimation(shake);
    	}
    }
    
    FBNotifications notifies ;
    private void notesGet()
	{	
    	if(isSafeCallFacebook(true) == false)
    	{
    		return ;
    	}
    	
        Log.d(TAG, "before get notes message");
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}	    	
    	
    	
    	facebookA.getNotificationsAsync(perm_session.getLogerInUserID(),  new FacebookAdapter()
    	{
    		@Override public void geNotifications(FBNotifications notifies)
            {
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
				FacebookProfileActivity.this.notifies = notifies;
//				processNotificationSummary(notifies);
                if(donotcallnetwork == false)//I am still alive
                {
					handler.obtainMessage(FACEBOOK_NOTIFICATIONS_UI).sendToTarget();
	            	//cancelNotify();
                }       
                
                
                Message msd = handler.obtainMessage(FACEBOOK_NOTIFICATION_GET_END);
            	msd.getData().putBoolean(RESULT, true);
            	handler.sendMessage(msd);
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
            	//get from Database
            	if(isInAynscTaskAndStoped())
            	{
            		Log.d(TAG, "User stop passive");
            	}
            	else
            	{
	            	Message msd = handler.obtainMessage(FACEBOOK_NOTIFICATION_GET_END);
	            	msd.getData().putBoolean(RESULT, false);
	            	handler.sendMessage(msd);
            	}
            }
    	});
	}
    
    //we can set UI here
    private void processNotificationSummary(FBNotifications notifies)
    {
    	//check whether enable show Message, Request, Poke,
    	//for Message UI update
        if(null == notifies)
        {
            Log.e(TAG,"processNotificationSummary notifies is null!");
            return;
        }
        
    	if(orm.isNotificationEnable(0))
    	{
    		//process Inbox
            updateUnreadMailInBoxThreads(notifies.msg.unread);
    	}
    	
    	int unprocessMessage = 0;
    	if(orm.isNotificationEnable(1))
    	{
    		unprocessMessage += notifies.poke.unread;
    	}
    	
    	if(orm.isNotificationEnable(2))
    	{
    		int eventInviteCount = notifies.entInvite.uids.size();
    		unprocessMessage += notifies.entInvite.uids.size();
    		//updateEventInviteRequests(eventInviteCount);
    	}
    	
    	if(orm.isNotificationEnable(3))
    	{
    		unprocessMessage += notifies.grdInvite.uids.size();
    	}
    	
    	if(orm.isNotificationEnable(4))
    	{
    		unprocessMessage += notifies.frdRequest.uids.size();
    	}
    	
    	//set UI right-top information
    	updateUnreadRequests(unprocessMessage);
    }
    
    private void notesGetNotification()
	{	
    	if(isSafeCallFacebook(true) == false)
    	{
    		return ;
    	}
    	
        Log.d(TAG, "before get notes message");
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}	    	
    	
    	facebookA.getNotificationListAsync(-1L, false, new FacebookAdapter()
        {
            @Override public void getNotificationList(Notifications notifications)
            {
                Log.d(TAG, "after get notification ="+notifications.notificationlist.size());
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                newNotes(notifications.notificationlist, notifications.appinfo);
                
                Message msd = handler.obtainMessage(FACEBOOK_NOTIFICATION_GET_END);                
                handler.sendMessage(msd);
            }
            
            @Override public void onException(FacebookException e, int method, Object[] args) 
            {
                Log.d(TAG, "fail to get get notifcation="+e.getMessage());
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
                     Message msd = handler.obtainMessage(FACEBOOK_NOTIFICATION_GET_END);                    
                     handler.sendMessage(msd);
                }
            }
        });
	}
    
	protected void newNotes(final ArrayList<Notification> notificationlist, ArrayList<AppInfo> appinfo) 
	{
	    SNSService.getSNSService().getNotificationService().processNotify(notificationlist);
	    
	    if(false)
	    {
    		//change UI behavior
    		handler.post( new Runnable()
    		{
    			public void run()
    			{
    				if(notificationlist != null && notificationlist.size() > 0)
    				{
    					facebook_bottom_region.setText(String.format(getString(R.string.nitification_format), notificationlist.size()));
    					float density = getResources().getDisplayMetrics().density;
    					facebook_bottom_region_notification_size.setVisibility(View.VISIBLE);
    					if(notificationlist.size() >=10)
    					{
    						facebook_bottom_region_notification_size.setPadding((int)(10*density), (int)(13*density), 0, 0);
    					}
    					else
    					{
    						facebook_bottom_region_notification_size.setPadding((int)(13*density), (int)(13*density), 0, 0);
    					}
    					facebook_bottom_region_notification_size.setText(String.valueOf(notificationlist.size()));					
    					Resources res = getResources();
    					Drawable mCacheSym = res.getDrawable(R.drawable.notification_small);
    					 
    					mCacheSym.setBounds(new Rect(0, 0, (int)(40*density), (int)(40*density)));
    					facebook_bottom_region.setCompoundDrawables(mCacheSym, null, null, null);
    					
    					if(unread != notificationlist.size() && notificationlist.size()>0)
    					{
    						unread = notificationlist.size();
    						/*
    						 *vibratePhone();
    						 *FlashLED();
    						 */
    					}
    				}
    				else
    				{
    					facebook_bottom_region.setText(R.string.menu_title_notifications);
    					facebook_bottom_region_notification_size.setVisibility(View.GONE);
    					facebook_bottom_region.setCompoundDrawables(null, null, null, null);
    				}
    			}
    		});
	    }
	}

	public void setTitle() 
	{
		title=getString(R.string.facebook_main_menu);		
		FacebookUser.SimpleFBUser user = orm.getSimpleFacebookUser(getLoginUserID());
		if(user != null)
		{
			title = user.name;
			user.despose();
			user = null;
		}		
		else
		{
		    if(perm_session != null)
            {
                long uids[] = new long[1];
                uids[0] = perm_session.getLogerInUserID();
                facebookA.getBasicUsersAsync(uids, new FacebookAdapter()
                {
                    public void getUsers(List<FacebookUser> users) 
                    {
                       if(users!=null && users.size()>0)
                       {
                           Log.d(TAG, "---get login user account -------- name:"+users.get(0).getName());
                           title = users.get(0).getName();
                           orm.addFacebookUser(users);
                           updateTitle(title);   
                       }
                    }
                });
            }
		}
	}
	
    private void updateUnreadMailInBoxThreads(int count)
    {
        Log.i(TAG, "--- updateUnreadMailInBoxThreads  count:"+count);
        inboxlayout.setVisibility(View.VISIBLE);
        if(count > 0)
        {
            if(count >= 10 && count<=99)
            {
                facebook_inbox_unread_size_tv.setPadding(10, 5, 0, 0);
            }
            else if(count>99)
            {
                facebook_inbox_unread_size_tv.setPadding(5, 5, 0, 0);
            }    
            else
            {
                facebook_inbox_unread_size_tv.setPadding(15, 5, 0, 0);
            }
            
            String value = count>99?"99+":String.valueOf(count);
            facebook_inbox_unread_size_tv.setText(value);
            inboxlayout.setVisibility(View.VISIBLE);
        }
        else
        {
            inboxlayout.setVisibility(View.GONE);
        }
    }
    
    private void updateUnreadRequests(int count)
    {
        Log.i(TAG, "--- updateUnreadRequests count:"+count);
        if(count > 0)
        {
            if(count >= 10)
            {
                facebook_request_unread_size_tv.setPadding(10, 5, 0, 0);
            }
            else if(count>99)
            {
                facebook_request_unread_size_tv.setPadding(5, 5, 0, 0);
            }  
            else
            {
                facebook_request_unread_size_tv.setPadding(15, 5, 0, 0);
            }
            String value = count>99?"99+":String.valueOf(count);
            facebook_request_unread_size_tv.setText(value);
            requestlayout.setVisibility(View.VISIBLE);
        }
        else
        {
            requestlayout.setVisibility(View.GONE);
        }
    }
    
    private void updateEventInviteRequests(int count)
    {
        Log.i(TAG, "--- updateEventInviteRequests count:"+count);
        if(count > 0)
        {
            if(count >= 10)
            {
                facebook_request_unread_size_event.setPadding(10, 5, 0, 0);
            }
            else if(count>99)
            {
                facebook_request_unread_size_event.setPadding(5, 5, 0, 0);
            }  
            else
            {
                facebook_request_unread_size_event.setPadding(15, 5, 0, 0);
            }
            String value = count>99?"99+":String.valueOf(count);
            facebook_request_unread_size_event.setText(value);
            eventlayout.setVisibility(View.VISIBLE);
        }
        else
        {
            eventlayout.setVisibility(View.GONE);
        }
    }
}
