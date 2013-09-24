package com.msocial.nofree.ui;


import java.io.File;
import java.util.List;
import java.util.ArrayList;
import com.msocial.nofree.R;
import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.ui.AccountListener.AccountManager;
import com.msocial.nofree.ui.view.ImageCacheManager;
import com.msocial.nofree.ui.view.ImageRun;
import com.msocial.nofree.ui.view.SNSItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;

import oms.sns.service.facebook.model.FacebookUser;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class FacebookRequestProcessActivity extends FacebookBaseActivity
{
    private final String TAG="FacebookRequestProcessActivity";
    private boolean friend_request;
    private long[]  uids;
    
    private ListView list;
    private TextView hint;
    
    List<FacebookUser> requestusers = new ArrayList<FacebookUser>();
    @Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_comfirm_request);
              
        friend_request = this.getIntent().getBooleanExtra("friend_request", false);
        uids           = this.getIntent().getLongArrayExtra("uids");
        
        hint = (TextView)this.findViewById(R.id.request_hint); 
        setHint(uids.length);     
    	list = (ListView)this.findViewById(R.id.facebook_request_list);
    	       
    	setTitle();
    	setTitle(title);
        //get facebook
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);
	        	getRequestUserInfo();
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }
	}
    
    private void setHint(int length) {
        if(length ==0 )
        {
            hint.setText(String.format(getString(R.string.facebook_friends_request_model_no), length));
        }
        else if(length == 1)
        {
            hint.setText(String.format(getString(R.string.facebook_friends_request_model_signal), length));
        }
        else
        {
            hint.setText(String.format(getString(R.string.facebook_friends_request_model), length));       
        }
    }

    @Override
	protected void loadRefresh()
    {
        if(this.isInProcess() == true)
        {
            showToast();
            return;
        }
    	getRequestUserInfo();
    }
    
    @Override
	protected void doAfterLogin() 
    {		
		super.doAfterLogin();
		
		SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);
	        	getRequestUserInfo();
        	}
        	else
        	{
        	    launchFacebookLogin();
        	}
        }		
	}
    
    //for friends request
    private void getRequestUserInfo()
    {    	
    	//get user info
    	Message msd = handler.obtainMessage(FACEBOOK_GET_REQUEDT_USERINFO);
    	msd.getData().putLongArray("uids", uids);
    	handler.sendMessage(msd);
    	
    }
   
	@Override
	protected void createHandler() 
	{
		 handler = new ConfirmHandler();
	}

	public void setTitle() 
	{
		title=getString(R.string.notification_request);		
	}
	
	final int FACEBOOK_COMFIRM_REQUEST    =0;
	final int FACEBOOK_COMFIRM_REQUEST_END=1;
	final int FACEBOOK_COMFIRM_POKE       =2;
	final int FACEBOOK_COMFIRM_POKE_END   =3;
	final int FACEBOOK_GET_REQUEDT_USERINFO     = 4;
	final int FACEBOOK_GET_REQUEDT_USERINFO_END = 5;
	final int FACEBOOK_SET_UI                   = 6;
	final int FACEBOOK_IGNORE_REQUEST           = 7;
	final int FACEBOOK_IGNORE_REQUEST_END       = 8;
	
	
	private class ConfirmHandler extends Handler 
	{
        public ConfirmHandler()
        {
            super();            
            Log.d(TAG, "new ConfirmHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case  FACEBOOK_GET_REQUEDT_USERINFO:
                {
                	long []ruids = msg.getData().getLongArray("uids");
                	getRequestUserInfo(ruids);
                	break;
                }               
                case  FACEBOOK_GET_REQUEDT_USERINFO_END:
                {
                	end();
                	break;
                }
                case FACEBOOK_SET_UI:
                { 
                    long uid = msg.getData().getLong("uid");
                    if(uid > 0 )
                    {
                    	list.setAdapter(null);
                        removeRequestFriend(uid);
                    }
                    
                	ItemAdapter ia = new ItemAdapter(mContext, requestusers);
            		list.setAdapter(ia);
                	break;
                }
	            case FACEBOOK_COMFIRM_REQUEST:
	            {
	            	long uid = msg.getData().getLong("uid");
	            	confirmFriendRequest(uid, true);
	            	break;
	            }	           
	            case FACEBOOK_IGNORE_REQUEST:
	            {
	            	long uid = msg.getData().getLong("uid");
	            	confirmFriendRequest(uid, false);
	            	break;
	            }
	            
	            case FACEBOOK_COMFIRM_REQUEST_END:
	            {
	            	end();
	            	boolean suc = msg.getData().getBoolean(RESULT);
	            	long    uid = msg.getData().getLong("uid");
	            	
	            	if(suc)
	            	{
	            	    addFriendShiptoDB(uid);
	            	    //set Text hint -1;
	            	    setHint(uids.length-1);
	            		Toast.makeText(mContext, R.string.sns_operate_succeed, Toast.LENGTH_SHORT).show();	 
	            		Message msd_1 = handler.obtainMessage(FACEBOOK_SET_UI);
                        msd_1.getData().putLong("uid", uid);
                        msd_1.sendToTarget();
	            	}
	            	else
	            	{
	            		Toast.makeText(mContext,R.string.sns_operate_failed, Toast.LENGTH_SHORT).show();
	            	}
	            	break;
	            } 
            }
        }
	}


	public void getRequestUserInfo(long[] ruids) 
	{
	    if(this.isInProcess() == true)
	    {
	        return;
	    }
		if(facebookA == null)
		{
			return;
		}
		
		//remove already confirm uid
        List<Long> notclist =  new ArrayList<Long>();
        for(int i=0;i<ruids.length;i++)
        {
            if(ruids[i] > 0)
                notclist.add(ruids[i]);
        }
        if(notclist.size() <= 0)
        {
            Log.d(TAG, "no available user");
            notclist = null;
            return ;
        }
        
        long[] ussid = new long[notclist.size()];
        for(int i=0;i<notclist.size();i++)
        {
            ussid[i] = notclist.get(i);
        }
        notclist.clear();
        notclist = null;
        
		begin();
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}	
		
	    facebookA.getBasicUsersAsync(ussid,new FacebookAdapter()
    	{
	    	@Override public void getUsers(List<FacebookUser> users)
            {
	    		synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}				
	    		
	    		if(donotcallnetwork == false)
	    		{
	    			if(users != null && users.size()>0)
	    			{
	    				Log.d(TAG, "after get participants info="+users.size());
						requestusers = users;
	    				orm.addFacebookUser(requestusers);
	    				handler.obtainMessage(FACEBOOK_SET_UI).sendToTarget();
	    			}
	    		}
    			
    			handler.obtainMessage(FACEBOOK_GET_REQUEDT_USERINFO_END).sendToTarget();
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	Log.d(TAG, "fail to get the participants");			
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
            	    handler.obtainMessage(FACEBOOK_GET_REQUEDT_USERINFO_END).sendToTarget();
             	}
            }
    	});
	}
	
	public void addFriendShiptoDB(long uid) {
	    long[] uids = {uid};
        for(FacebookUser user : requestusers)
        {
            if(user.uid == uid)
            {
                orm.addFriends(perm_session.getLogerInUserID(), uids);
                orm.addFacebookUser(user); 
                break;
            }
        }
        
    }

    public void confirmFriendRequest(long uid, boolean confirm) 
	{
		if(facebookA == null)
			return;
		
		begin();
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
    	
	    facebookA.confirmRequestAsync(uid, confirm, new FacebookAdapter()
    	{
	    	@Override public void confirms(long uid, boolean suc)
            {
	    		synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}    	
	    		
	    		Message msd = handler.obtainMessage(FACEBOOK_COMFIRM_REQUEST_END);
	    		msd.getData().putBoolean(RESULT, true);
	    		msd.getData().putLong("uid", uid);
    			handler.sendMessage(msd);
            }
    		
            @Override public void onException(FacebookException e, int method, Object[] args) 
            {
            	long uid = (Long)args[2];
            	
            	Log.d(TAG, "fail to get comfirm");			
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
	            	Message msd = handler.obtainMessage(FACEBOOK_COMFIRM_REQUEST_END);
		    		msd.getData().putBoolean(RESULT, false);
		    		msd.getData().putLong("uid", uid);
	    			handler.sendMessage(msd);
             	}
            }
    	});
		
	}
	
	private void removeRequestFriend(long uid)
	{
	    if(requestusers!=null && requestusers.size()>0)
	    {
	        synchronized(requestusers)
	        {
    	        for(FacebookUser user : requestusers)
    	        {
    	            if(user.uid == uid)
    	            {
    	                requestusers.remove(user);
    	                break;
    	            }
    	        }
	        }
	        
	        
	        if(uids!=null && uids.length>0)
	        {
	        	long[] tmpids = new long[uids.length-1];
	        	int j=0;
	        	for(int k=0;k<uids.length;k++)
	        	{
	        		if(uids[k]!= uid)
	        		{
	        			tmpids[j++] = uids[k];
	        		}
	        	}
	        	uids = tmpids;
	        	tmpids = null;
	        }
	        
	       /* for(int i=0;i<uids.length;i++)
	        {
	            if(uid == uids[i])
	            {
	                uids[i] = -1;
	                break;
	            }
	        }*/	        
	    }
	}
	
	public class ItemAdapter extends BaseAdapter 
	{
	    private final String TAG = "ItemAdapter";        
	    private Context mContext;  
	    
	    public ItemAdapter(Context con,  List<FacebookUser>users)
	    {
	    	mContext = con;
	    	mUsersItems = users;   
	    	Log.d(TAG, "create ItemAdapter");
	    }
	    
		public int getCount() 
		{
			return mUsersItems.size();
		}
		public Object getItem(int pos) {		
		    return mUsersItems.get(pos);
		}
		public long getItemId(int pos) 
		{
			return mUsersItems.get(pos).uid;
		}
		public View getView(int position, View convertView, ViewGroup arg2) 
		{		
			 if (position < 0 || position >= getCount()) 
			 {
	             return null;    
	         }
	         
			 ItemView v=null;
		
			 FacebookUser di = (FacebookUser)getItem(position);
	         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
	             v = new ItemView(mContext, di);
	         } else {
	              v = (ItemView) convertView;
	              v.setContentItem(di);
	         }
	         return v;
		}	
		private List<FacebookUser> mUsersItems;
	}

	
	public class ItemView extends SNSItemView
	{	    
		 private FacebookUser  user;
		 private final static String TAG = "ItemView";
		
		 private TextView  name ;
		 private ImageView imageView;
		 public FacebookUser getUser()
		 {
		    return user;
		 }
		 
	     public ItemView(Context ctx, AttributeSet attrs) 
		 {
			super(ctx, attrs);		
			mContext = ctx;		
			setOrientation(LinearLayout.VERTICAL);
	        this.setVisibility(View.VISIBLE);		        
	    }
		
		public ItemView(Context context, FacebookUser fuser) 
		{		
			super(context);
			mContext = context;
			this.user = fuser;
			Log.d(TAG, "call  ItemView");			
			init();
		}
			
		private void init() 
		{
			Log.d(TAG,  "call ItemView init");
			LayoutInflater factory = LayoutInflater.from(mContext);
			removeAllViews();
			
			//container
			FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);			
			FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
			view.setLayoutParams(paras);
			view.setVerticalScrollBarEnabled(true);
			addView(view);
			
			View v  = factory.inflate(R.layout.facebook_friends_request_item, null);		
			v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
			view.addView(v);
			
			name    = (TextView)v.findViewById(R.id.facebook_friend_request_user_name);			
			imageView = (ImageView)v.findViewById(R.id.friend_img_ui);
			
			Button comfirm   = (Button)v.findViewById(R.id.facebook_friend_request_confirm);
			comfirm.setText(R.string.sns_confirm);				
			comfirm.setOnClickListener(confirmClick);
			
			Button ignore    = (Button)v.findViewById(R.id.facebook_friend_request_ignore);
			ignore.setText(R.string.sns_ignore);				
			ignore.setOnClickListener(ignoreClick);		
			
			setUI();
		}
		
		View.OnClickListener toUserDetailClick = new View.OnClickListener()
	    {
	        public void onClick(View v) 
	        {
	            Log.d(TAG, "entering userLogo Click...");
	            Intent intent = new Intent(mContext, FacebookAccountActivity.class);
	            intent.putExtra("uid",      user.uid);
	            intent.putExtra("username", user.name);
	            intent.putExtra("imageurl", user.pic_square);                   
	            ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
	        }
	    };
		public void setImage(String tmpurl) {
	        ImageRun imagerun = new ImageRun(handler, tmpurl, 1);
	        imagerun.noimage = true;
	        imagerun.setImageView(imageView);        
	        imagerun.post(imagerun);
	    }
		
		private String getImgURL(FacebookUser user)
	    {
	        if(user == null)
	            return null;
	        
	        String url = null;
	        int icon_size = orm.getFacebookIconSizeSetting(); //0 big 1 middle 2 small
	        switch(icon_size)
	        {
	            case 0:
	            {
	                url = user.pic;
	                if(url == null)
	                {
	                    url = user.pic_square;
	                }
	                break;
	            }
	            case 1:
	            { 
	                url = user.pic_small;
	                if(url == null)
	                {
	                    url = user.pic_square;
	                }
	                break;
	            }
	            case 2:
	            {
	                url = user.pic_square;
	                break;
	            }
	        }
	        return url;
	    }
		
		View.OnClickListener confirmClick = new View.OnClickListener()
	    {

			public void onClick(View arg0) 
			{
				 Message msd = handler.obtainMessage(FACEBOOK_COMFIRM_REQUEST);
				 msd.getData().putLong("uid", user.uid);
				 
				 handler.sendMessage(msd);
			}
	    	
	    };
	    View.OnClickListener ignoreClick = new View.OnClickListener()
	    {

			public void onClick(View arg0) 
			{					 
				 Message msd = handler.obtainMessage(FACEBOOK_IGNORE_REQUEST);
				 msd.getData().putLong("uid", user.uid);			 
				 handler.sendMessage(msd);
			}		    	
	    };			
		
		@Override
		protected void onFinishInflate() 
		{	
			super.onFinishInflate();		
			init();
		}			
		
		public void setUI()
		{
		    name.setText("");
		    imageView.setImageResource(R.drawable.no_avatar);
		    if(user != null)
            {
                name.setText(String.valueOf(user.name));
                name.setOnClickListener(toUserDetailClick);
                imageView.setOnClickListener(toUserDetailClick);
                String tmpurl = getImgURL(user);
                //first set small icon and then set the right icon                
                if(user!=null)
                {  
                    String smallFile = TwitterHelper.getImagePathFromURL_noFetch(user.pic_square);
                    if(new File(smallFile).exists() == true)
                    {
                        try{
                            Bitmap tmp = BitmapFactory.decodeFile(smallFile);
                            if(tmp != null)
                            {
                                ImageCacheManager.instance().addCache(user.pic_square, tmp);
                                imageView.setImageBitmap(tmp);
                            }
                        }catch(Exception ne){}
                    }
                    else
                    {
                        imageView.setImageResource(R.drawable.no_avatar);
                    }            
                }
                else
                {
                    imageView.setImageResource(R.drawable.no_avatar);
                }
                
                if(tmpurl != null)
                {
                   setImage(tmpurl);
                }  
            }
		}

		public void setContentItem(FacebookUser fuser) 
		{
			user = fuser;		
			setUI();
		}

		@Override
		public String getText()
		{			
			return "";
		}
	}
}
