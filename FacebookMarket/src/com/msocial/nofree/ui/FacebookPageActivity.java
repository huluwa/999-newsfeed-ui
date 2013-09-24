package com.msocial.nofree.ui;

import java.io.File;
import java.util.List;

import com.msocial.nofree.R;
import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.providers.SocialORM.FacebookUsersCol;
import com.msocial.nofree.providers.SocialORM.PageCol;

import com.msocial.nofree.service.ObjectHandler;
import com.msocial.nofree.ui.view.SNSItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.model.Page;
import oms.sns.service.facebook.model.PhoneBook;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class FacebookPageActivity extends FacebookBaseActivity
{
    private final String TAG="FacebookPageActivity";    
	private ListView friendList;	
	
	private View     searchSpan;
	private EditText keyEdit;
	private TextView current_postion;
	
    private Cursor fcursor;		
	
	private MyWatcher watcher;
	private int limit  = 300;
	private int offset = 0;
	PageHandler pageHandler;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Intent intent = this.getIntent();        
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
        
        pageHandler = new PageHandler();
        View v = findViewById(R.id.progress_horizontal);
		if(v != null)
		{
		    progressHorizontal = (ProgressBar) v;
		}
        
        setTitle("");
        SocialORM.Account account = orm.getFacebookAccount();        
        
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{	    
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);
	        	handler.obtainMessage(FACEBOOK_PAGE_UI).sendToTarget();
	        	Cursor tmp = orm.getAllPageCursor();
	        	if(tmp == null || tmp.getCount() == 0)
	        	{
	        		Log.d(TAG, "I have no data, so load from web server");
	        		lauchGetPages();
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
    @Override
    protected void loadAfterSetting()
    {
    	//a bug for request from my selft
    	FacebookPageActivity.this.setResult(RESULT_CANCELED);
    	FacebookPageActivity.this.finish();
    }
    
    @Override
    protected void loadAfterSettingNoChange()
    {
    	FacebookPageActivity.this.setResult(RESULT_CANCELED);
    	FacebookPageActivity.this.finish(); 	    
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
            fcursor = orm.searchPageCursor(key);            
        }  
        else
        {
            if(fcursor != null)
            {
                fcursor.close();
                fcursor = null;
            }
            fcursor = orm.getAllPageCursor();           
            
        }        
       
        PageAdapter adapter = new PageAdapter(FacebookPageActivity.this, fcursor);        
        friendList.setAdapter(adapter);
    }
    
   
	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			Log.d(TAG, "facebook page Item clicked");			
			if(PageItemView.class.isInstance(v))
			{
				PageItemView fv= (PageItemView)v;
				Intent intent = new Intent(mContext, FacebookAccountActivity.class);
				intent.putExtra("uid",      fv.getpage().page_id);
				intent.putExtra("username", fv.getpage().name);
				intent.putExtra("imageurl", fv.getpage().pic_square);	
				intent.putExtra("frompage", true);
				
				((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
				//((FacebookBaseActivity)(mContext)).startActivity(intent);
			}
		}
	};
	   
		
    @Override
	protected void createHandler() 
    {
		handler = new MainHandler();		
	}
    
    //try to get wall message
    private void lauchGetPages()
    {   
    	Log.d(TAG, "call lauchGeFriends="+this+" offset is "+offset);
        if(this.isInProcess())
        {
            Log.d(TAG, "I am get friends="+this);
            Toast.makeText(mContext, R.string.getting_page_from_server, Toast.LENGTH_SHORT).show();
            return;
        }
    	Message msg = handler.obtainMessage(FACEBOOK_PAGE_GET);
    	msg.sendToTarget();
    }
    
    //reget the wall
    @Override
    protected void loadRefresh()
    {
    	Log.d(TAG, "call refresh="+this);
    	if(facebookA != null)
    	{
    		lauchGetPages();
    	}
    }
    
    @Override
    public void onLogin() 
	{		
    	super.onLogin();
    	
    	Log.d(TAG, "call onLogin="+this);
    	
    	if(facebookA != null)
    	{
    		lauchGetPages();
    	}
	}
    
    @Override
    protected void doAfterLoginNothing()
    {
    	Log.d(TAG, "after login");
    	FacebookPageActivity.this.setResult(RESULT_CANCELED);
    	FacebookPageActivity.this.finish();
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
    		lauchGetPages();
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
        
    final static int FACEBOOK_PAGE_GET    =0;
    final static int FACEBOOK_PAGE_UI      =1;
    final static int FACEBOOK_PAGES_GET_END=2;
    
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
                        current_postion.setText(suser.name.substring(0, 1));
                    }
                    else
                    {
                    	current_postion.setVisibility(View.GONE);
                    }
                    
                    PageAdapter adapter = new PageAdapter(FacebookPageActivity.this, fcursor);
                    friendList.setAdapter(adapter);
                	break;
                }                        
            }
        }
    }
    
    private void notifyLoading() 
    {	
    	notify.notifyOnce(R.string.facebook_pages_loading, R.drawable.facebook_logo, 30*1000);    	
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
    
    public void getPageFromServer() 
    {	
        if(isInProcess())
        {
            Log.d(TAG, "I am in getting page, please wait");
            return;
        }
        begin();
        Log.d(TAG, "before get getFacebookPage");
        //notifyLoading();  
        synchronized(mLock)
        {
            inprocess = true;
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
                       inprocess = false;
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
                        inprocess = false;
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
}
