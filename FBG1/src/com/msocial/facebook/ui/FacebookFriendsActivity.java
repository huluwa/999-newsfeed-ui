package com.msocial.facebook.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.msocial.facebook.R;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.providers.SocialORM.FacebookUsersCol;
import oms.sns.service.facebook.model.PhoneBook;
import com.msocial.facebook.service.SNSService;
import com.msocial.facebook.ui.adapter.FacebookFriendAdapter;
import com.msocial.facebook.ui.adapter.FacebookFriendCursorAdapter;
import com.msocial.facebook.ui.view.FacebookFriendItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class FacebookFriendsActivity  extends FacebookBaseActivity
{
    private final String TAG="FacebookFriendsActivity";    
	private ListView friendList;	
	
	private View     searchSpan;
	private EditText keyEdit;
	private TextView current_postion;
	
    private Cursor fcursor;		
	
	private MyWatcher watcher;
	
	private boolean friends_bd = false;
	private long toFindFriendUID=-1;
	//for contact call startActivityForResult
	private boolean friends_select = false;
	private long friend_select_pid = -1;
	private boolean mCreateShortcut;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Intent intent = this.getIntent();
        
        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) 
        {
            mCreateShortcut = true;
        }
        
        friends_bd = intent.getBooleanExtra("friends_bd", false);
        friends_select = intent.getBooleanExtra("friends_select", false);
        if(friends_select)
        {
           friend_select_pid = intent.getLongExtra("peopleid", -1);
        }
        
        setContentView(R.layout.facebook_friends_ui);
        friendList = (ListView)this.findViewById(R.id.facebook_user_list);   
        friendList.setFocusableInTouchMode(true);
        friendList.setFocusable(true);
        friendList.setOnCreateContextMenuListener(this);
        friendList.setOnItemClickListener(listItemClickListener);
        friendList.setOnScrollListener(scrollListener);
        
        current_postion = (TextView)this.findViewById(R.id.current_postion);
        //current_postion.setVisibility(View.GONE);
        
        searchSpan = this.findViewById(R.id.facebook_search_span);
        searchSpan.setVisibility(View.VISIBLE);
        keyEdit = (EditText)this.findViewById(R.id.embedded_text_editor);
        watcher = new MyWatcher();         
        keyEdit.addTextChangedListener(watcher); 
        
        View v = findViewById(R.id.progress_horizontal);
		if(v != null)
		{
		    progressHorizontal = (ProgressBar) v;
		}
        
        toFindFriendUID = this.getIntent().getLongExtra("hisuid", -1);
        	
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
    
    AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() 
    {		
		public void onScrollStateChanged(AbsListView view, int scrollState) {			
			
		}
		
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {		
			if(fcursor != null)
			{
				if(fcursor.moveToPosition(firstVisibleItem))
				{
					current_postion.setVisibility(View.VISIBLE);
					String name = fcursor.getString(fcursor.getColumnIndex(FacebookUsersCol.NAME));        
					current_postion.setText(name.subSequence(0, 1));
					name = null;
				}
			}
		}
	};
	
    @Override
    protected void loadAfterSetting()
    {
    	//a bug for request from my selft
    	FacebookFriendsActivity.this.setResult(RESULT_CANCELED);
        FacebookFriendsActivity.this.finish();
    }
    
    @Override
    protected void loadAfterSettingNoChange()
    {
    	FacebookFriendsActivity.this.setResult(RESULT_CANCELED);
        FacebookFriendsActivity.this.finish(); 	    
    	Log.d(TAG , "loadAfterSettingNoChange");
    }
    
	public void setTitle() 
	{
	 	title = "";		
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
            if(friends_bd)
            {
               fcursor = orm.searchFacebookBDCursor(toFindFriendUID, key);
            }
            else
            {
               fcursor = orm.searchFacebookUserCursor(toFindFriendUID, key);
            }
        }  
        else
        {
            if(fcursor != null)
            {
                fcursor.close();
                fcursor = null;
            }
            if(friends_bd)
            {
               fcursor = orm.getFacebookUsersOrderByBirthdayCursor(toFindFriendUID);
            }
            else
            {
                fcursor = orm.getAllFacebookSimpleUsersCursor(toFindFriendUID);
            }
            
        }        
       
        FacebookFriendCursorAdapter adapter = new FacebookFriendCursorAdapter(FacebookFriendsActivity.this, fcursor, friends_bd, true);        
        friendList.setAdapter(adapter);
    }
    
    //sync to calendar
	private void selectUserForContact(FacebookFriendItemView fv)
	{	    
	    FacebookUser.SimpleFBUser fbuser = fv.getUser();
	    Intent select_intent = new Intent();
	    select_intent.putExtra("uid",      fbuser.uid);
	    select_intent.putExtra("username", fbuser.name);
	    
       if(!isEmpty(fbuser.birthday)){
        
            String tempstr = fbuser.birthday;
            int year = new Date().getYear()+1900;
                
            if(tempstr.indexOf(",")==-1){
                tempstr = tempstr + ","+ year;
            }
                
            SimpleDateFormat df = new SimpleDateFormat("MMMM dd,yyyy",Locale.US);
            try {
               Date birthdayDate = df.parse(tempstr);
               String birthday = new SimpleDateFormat("yyyy-MM-dd").format(birthdayDate);         
               select_intent.putExtra("birthday", birthday);
            } catch (Exception e) {
                 Log.d(TAG," birthday format exception "+e.getMessage()+"==");
            }
       }
       
       FacebookUser us = orm.getFacebookUser(fbuser.uid);
		if(!isEmpty(us.message))select_intent.putExtra("status", us.message);
		
		PhoneBook pb = orm.getPhonebook(fbuser.uid);
		if(pb != null)
		{
		    if(!isEmpty(pb.cell))select_intent.putExtra("cell",   pb.cell);
		    if(!isEmpty(pb.email))select_intent.putExtra("email",  pb.email);
		    if(!isEmpty(pb.phone))select_intent.putExtra("phone",  pb.phone);
		}		

		String filepath = TwitterHelper.getImagePathFromURL(FacebookFriendsActivity.this.getApplicationContext(), fv.getUser().pic_square, false);
		if(!isEmpty(filepath))
		{
		    File mfile = new File(filepath);
		    if(mfile.exists())
		    {
		        // new MediaScannerNotifier(this,mfile);
		        select_intent.putExtra("picuri", Uri.fromFile(mfile).toString());       
		    }
		}
		
		FacebookFriendsActivity.this.setResult(RESULT_OK, select_intent);
        FacebookFriendsActivity.this.finish(); 	    
	}
	
	
	
	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			Log.d(TAG, "facebook friend Item clicked");
			if(friends_select)
			{
				if(FacebookFriendItemView.class.isInstance(v))
				{
					FacebookFriendItemView fv= (FacebookFriendItemView)v;
					selectUserForContact(fv);
				}				
			}
			else
			{
				if(gestureprocessed == true)
				{
					gestureprocessed = false;
					return;
				}
				else
				{
					if(FacebookFriendItemView.class.isInstance(v))
					{
						FacebookFriendItemView fv= (FacebookFriendItemView)v;
						if(mCreateShortcut == false)
						{
							Intent intent = new Intent(mContext, FacebookAccountActivity.class);
							intent.putExtra("uid",      fv.getUser().uid);
							intent.putExtra("username", fv.getUser().name);
							intent.putExtra("imageurl", fv.getUser().pic_square);					
							((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
						}
						else
						{
							constructShortCut(fv.getUser());
						}
						//((FacebookBaseActivity)(mContext)).startActivity(intent);
					}
				}
			}
			
		}
	};
	   
	private void constructShortCut(FacebookUser.SimpleFBUser user)
	{
		final Intent intent = createShortcutIntent(user.uid,  user.name, user.pic_square);
        setResultToParent(RESULT_OK, intent);
        finish();
	}
	
	 static Bitmap centerToFit(Bitmap bitmap, int width, int height, Context context) {
	        final int bitmapWidth = bitmap.getWidth();
	        final int bitmapHeight = bitmap.getHeight();

	        if (bitmapWidth < width || bitmapHeight < height) {
	            int color = context.getResources().getColor(R.color.light_blue);

	            Bitmap centered = Bitmap.createBitmap(bitmapWidth < width ? width : bitmapWidth,
	                    bitmapHeight < height ? height : bitmapHeight, Bitmap.Config.RGB_565);
	            centered.setDensity(bitmap.getDensity());
	            Canvas canvas = new Canvas(centered);
	            canvas.drawColor(color);
	            canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(2,2,centered.getWidth()-2, centered.getHeight()-2),
	                    null);

	            bitmap = centered;
	        }

	        return bitmap;
	    }
	
	private Intent createShortcutIntent(long uid, String title, String pic_url) {
        final Intent i = new Intent();
        final Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
        shortcutIntent.setClassName("com.msocial.facebook", "com.msocial.facebook.ui.FacebookAccountActivity");
        shortcutIntent.putExtra("uid",      uid);
        shortcutIntent.putExtra("username", title);
        shortcutIntent.putExtra("imageurl", pic_url);		
        i.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        i.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        
        //this is photo
        Bitmap bmp = null;
		String filepath = TwitterHelper.getImagePathFromURL_noFetch(pic_url);
		if(new File(filepath).exists())
		{
			try{				    
				bmp = BitmapFactory.decodeFile(filepath);	    	    
		    }
			catch(Exception ne){}
		}
		else
		{
			try{				    
				bmp = BitmapFactory.decodeResource(getResources(), R.drawable.no_avatar);	    	    
		    }
			catch(Exception ne){}		    
		}
		
		if(bmp == null)
		{
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.no_avatar);
		}
		
		//resize the bmp to 60dip
		bmp = centerToFit(bmp, 72, 72, this);
		
        Bitmap favicon = BitmapFactory.decodeResource(getResources(),R.drawable.facebook_ico);
        
        // Make a copy of the regular icon so we can modify the pixels.        
        Bitmap copy = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(copy);

        // Make a Paint for the white background rectangle and for
        // filtering the favicon.
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(Color.WHITE);

        final DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        final float density = metrics.density;
        
        // Create a rectangle that is slightly wider than the favicon
        final float iconSize = 8*density; // 16x16 favicon
        final float padding = 1*density;   // white padding around icon
        final float rectSize = iconSize + 2 * padding;
        final float y = bmp.getHeight() - rectSize;
        RectF r = new RectF(0, y, rectSize, y + rectSize);

        // Draw a white rounded rectangle behind the favicon
        canvas.drawRoundRect(r, 2, 2, p);

        // Draw the favicon in the same rectangle as the rounded rectangle
        // but inset by the padding (results in a 16x16 favicon).
        r.inset(padding, padding);
        canvas.drawBitmap(favicon, null, r, p);
        i.putExtra(Intent.EXTRA_SHORTCUT_ICON, copy);
    
        // Do not allow duplicate items
        i.putExtra("duplicate", false);
        return i;
    }

	
    private void setResultToParent(int resultCode, Intent data) 
    {
        Activity a = getParent() == null ? this : getParent();
        a.setResult(resultCode, data);
    }
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
    	if(facebookA != null)
    	{
    	    lauchGeFriends();
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
    	FacebookFriendsActivity.this.setResult(RESULT_CANCELED);
    	FacebookFriendsActivity.this.finish();
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
    	    
    	     if(friends_select == true)
    	     {
    	         Log.d(TAG, "not from contact");
                 FacebookFriendsActivity.this.setResult(RESULT_CANCELED);
                 FacebookFriendsActivity.this.finish();
    	     }
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
    protected void postToWall(long uid)
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
                    	 if(donotcallnetwork == false)
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
                    if(friends_bd == true)
                	{
                        if(fcursor != null)
                        {
                            fcursor.close();
                            fcursor = null;
                        }
                        fcursor = orm.getFacebookUsersOrderByBirthdayCursor(toFindFriendUID);
                    }
                    else
                    {
                        if(fcursor != null)
                        {
                            fcursor.close();
                            fcursor = null;
                        }                    
                        fcursor = orm.getAllFacebookSimpleUsersCursor(toFindFriendUID);
                	}
                    
                    if(fcursor != null && fcursor.getCount() > 0 && fcursor.moveToFirst())
                    {
                    	current_postion.setVisibility(View.VISIBLE);
                    	FacebookUser.SimpleFBUser suser = orm.formatSimpleFacebookUser(fcursor);
                        current_postion.setText(suser.name.substring(0, 1));
                    }
                    else
                    {
                    	current_postion.setVisibility(View.GONE);
                    }
                    
                    FacebookFriendCursorAdapter adapter = new FacebookFriendCursorAdapter(FacebookFriendsActivity.this, fcursor, friends_bd, false);
                    friendList.setAdapter(adapter);
                	break;
                }                        
            }
        }
    }
    
    @Override
    protected void addAsFriends(long uid)
    {
    	if(isSafeCallFacebook(true) == false)
    	{
    		return ;
    	}
    	
        begin();		
    	Log.d(TAG, "before get addAsFriends");
    	//notifyLoading();  
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}
    	
    	facebookA.addAsFriendAsync(uid,  new FacebookAdapter()
    	{
    		@Override public void addAsFriend(long uid, boolean suc)
            {
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}				
				if(donotcallnetwork == false )//I am still alive
                {   
                     //cancelNotify();
                }   
				Message msd = handler.obtainMessage(FACEBOOK_FRIENDS_ADD_END);
				msd.getData().putBoolean(RESULT, suc);
				msd.getData().putLong("uid", uid);
                handler.sendMessage(msd);
            }
    		
            @Override public void onException(FacebookException e, int method, Object[]args) 
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
	            	Message msd = handler.obtainMessage(FACEBOOK_FRIENDS_ADD_END);
					msd.getData().putBoolean(RESULT, false);
					msd.getData().putLong("uid", (Long)args[0]);
	                handler.sendMessage(msd);
            	}
            }
    	});
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
    
    private void notifyLoading() 
    {
    	if(friends_bd)
    	{
    		notify.notifyOnce(R.string.facebook_upcomingbd_friends_loading, R.drawable.facebook_logo, 30*1000);	
    	}
    	else
    	{
    	    notify.notifyOnce(R.string.facebook_friends_loading, R.drawable.facebook_logo, 30*1000);
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
