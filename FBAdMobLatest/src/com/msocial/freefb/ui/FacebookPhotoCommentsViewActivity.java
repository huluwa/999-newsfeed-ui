package com.msocial.freefb.ui;

import java.util.ArrayList;
import java.util.List;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.ui.adapter.FacebookCommentsAdapter;
import com.msocial.freefb.ui.adapter.FacebookPhotoCommentAdapter;
import com.msocial.freefb.ui.view.ImageRun;
import com.msocial.freefb.util.DateUtil;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Photo;
import oms.sns.service.facebook.model.PhotoAlbum;
import oms.sns.service.facebook.model.PhotoComment;
import oms.sns.service.facebook.model.FacebookUser.SimpleFBUser;
import oms.sns.service.facebook.model.Stream.Comments;
import oms.sns.service.facebook.model.Stream.Comments.Stream_Post;
import oms.sns.service.facebook.util.StringUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookPhotoCommentsViewActivity extends FacebookBaseActivity{

	private ImageView photo_image;
	private Photo     photo;
	private PhotoAlbum album;
	private TextView  photo_name_detail;
	private TextView  photo_create_date;
	private List<PhotoComment> comments = new ArrayList<PhotoComment>();
	private ListView  commentsView;
	
	private EditText photo_comments;
	private Button photo_save;
	private Button photo_cancel;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_album_photo_comments_list);
        setTitle();
        setTitle(title);
        photo_image = (ImageView)this.findViewById(R.id.photo_image);
        photo_name_detail = (TextView)this.findViewById(R.id.photo_name_detail);
        photo_create_date = (TextView)this.findViewById(R.id.photo_create_date); 
        commentsView = (ListView)this.findViewById(R.id.comments_list);
        
        
        photo_save   = (Button)this.findViewById(R.id.photo_comment_add);
        photo_cancel = (Button)this.findViewById(R.id.photo_cancel);
        photo_comments = (EditText)this.findViewById(R.id.photo_comments);
        
        photo_save.setOnClickListener(photoCommentsClick);
        photo_cancel.setOnClickListener(cancelClick);
        
        photo = this.getIntent().getParcelableExtra("photo");
        album = this.getIntent().getParcelableExtra("album");
        if(photo != null || album != null)
        {
	    	ImageRun imagerun = new ImageRun(handler,getImageURL(), 0);
			imagerun.noimage = false;
	        imagerun.setImageView(photo_image);
	        imagerun.post(imagerun);
	        
	        photo_name_detail.setText(isEmpty(getCaption())==true?"No Caption":getCaption());	        
	        photo_create_date.setText("Created "+getCreateDate());
        }
        
        
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
            perm_session = loginHelper.getPermanentSesstion(this);
            if(perm_session != null)
            {
                perm_session.attachActivity(this);
                
                facebookA = new AsyncFacebook(perm_session);
                launchGetPhotoComments();
            }
            else
            {
                launchFacebookLogin();
            }
        }       
    }
	
	@Override
	protected void loadRefresh()
    {
	    super.loadRefresh();
	    if(this.isInProcess() == true)
	    {
	        showToast();
	        return;
	    }
	    launchGetPhotoComments();
    }
	  
	private String getImageURL()
	{
		if(photo != null)
			return photo.src_small;
		
		if(album != null)
			return album.cover_src_url;
		
		return "";
	}
	
	private String getCaption()
	{
		if(photo != null)
			return photo.caption;
		
		if(album != null)
			return album.name;
		
		return "";
	}
	
	private String getCreateDate()
	{
		if(photo != null && photo.created != null)
			return DateUtil.converToRelativeTime(mContext, photo.created);
		
		if(album != null && album.created!= null)
			return  DateUtil.converToRelativeTime(mContext, album.created);
		
		return "";
	}
	
	//for comments
    View.OnClickListener photoCommentsClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            //dimiss input keyboard
            hideInputKeyBoard(v);
            launchAddPhotoComment();
        }
    }; 
    
    View.OnClickListener cancelClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            finish();
        }
    };

    
    public void launchAddPhotoComment()
    {
        handler.obtainMessage(COMMENTS_ADD_POST).sendToTarget();
    }
    
	public void launchGetPhotoComments()
	{
	    handler.obtainMessage(PHOTO_COMMENTS_GET).sendToTarget();
	}
	
	@Override
	protected void createHandler() {
		handler = new PhotoHandler();
	}

	public void setTitle() {
		title="";		
	}
	
	public void getPhotoComments()
	{
	    if(this.isInProcess() == true)
	    {
	        return;
	    }
	    begin();       
        Log.d(TAG, "before get PhotoComments");
        synchronized(mLock)
        {
            inprocess = true;
        }
        
        if(perm_session != null)
        {
           if(facebookA == null)
           {
               facebookA = new AsyncFacebook(perm_session);
           }               
           facebookA.setSession(perm_session);
           facebookA.getPhotoCommentsAsync(photo.pid,new FacebookAdapter()
           {
               @Override public void getPhotoComment(List<PhotoComment> photocomments)
               {
                   Log.d(TAG," after getPhotoComments "+photocomments.size());
                   synchronized(mLock)
                   {
                       inprocess = false;
                   }
                   comments = photocomments;
                   handler.obtainMessage(PHOTO_COMMENTS_UI).sendToTarget();
                   Message msg = handler.obtainMessage(PHOTO_COMMENTS_GET_END);
                   msg.getData().putBoolean(RESULT, true);
                   msg.sendToTarget();
               }
               
               @Override public void onException(FacebookException e, int method) 
               {
                   Log.d(TAG, "fail to get photoComments exception "+e.getMessage());
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
                	   Message msg = handler.obtainMessage(PHOTO_COMMENTS_GET_END);
                       msg.getData().putBoolean(RESULT, false);
                       msg.sendToTarget();
                   }
                  
               }
           });  
        }     
	}
	
	private void addPhotoComment()
    {
        String content = photo_comments.getText().toString();
        if(StringUtils.isEmpty(content) == true)
        {
            return;
        }
        
        if(existSession() == false)
        {
            return;
        }    
        
        begin();
        showDialog(DLG_ADD_COMMAND);
        photo_save.setEnabled(false);
        synchronized(mLock)
        {
            inprocess = true;
        }        
        
        facebookA.photoAddCommentsAsync(photo.pid, content.trim(),new FacebookAdapter()
        {
            @Override public void photoAddComments(boolean suc)
            {
                Log.d(TAG, "after add comments="+suc);
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                Message rmsg = handler.obtainMessage(COMMENTS_ADD_POST_END);
                rmsg.getData().putBoolean(RESULT, true);
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
                    Message rmsg = handler.obtainMessage(COMMENTS_ADD_POST_END);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.sendToTarget();
                }
            }
        });
    }
	
	private void refreshCurrentComments()
	{
	    String content = photo_comments.getText().toString();
	    PhotoComment comment = new PhotoComment();
	    comment.from = perm_session.getLogerInUserID();
	    comment.pid  = photo.pid;
	    comment.time = System.currentTimeMillis();
	    comment.body = content.trim();
	    comments.add(0, comment);
	   /* ArrayList list = new ArrayList();
	    list.add(comment);
	    list.add(comments);
	    comments = list;*/
	    FacebookPhotoCommentAdapter adapter = new FacebookPhotoCommentAdapter(mContext,comments);
	    commentsView.setAdapter(adapter);
	}
	
	 
	 final static int PHOTO_COMMENTS_GET      =1;
	 final static int PHOTO_COMMENTS_UI       =2;
	 final static int PHOTO_COMMENTS_GET_END  =3;
	 final static int COMMENTS_ADD_POST      =4;    
	 final static int COMMENTS_ADD_POST_END  =5;
	 final static int SIMPLE_USERINFO_GET    =6;
	 private class PhotoHandler extends Handler 
    {
        public PhotoHandler()
        {
            super();            
            Log.d(TAG, "new PhotoHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case PHOTO_COMMENTS_GET:
	            {
	                 getPhotoComments();
	                 break;
	            }
	            case PHOTO_COMMENTS_UI:
	            {    
	                if(comments.size()>0)
	                {
	                    loadCommentsUserInfo(comments);
	                    FacebookPhotoCommentAdapter adapter = new FacebookPhotoCommentAdapter(mContext,comments);
	                    commentsView.setAdapter(adapter); 
	                }
	                else
	                {
	                    commentsView.setAdapter(null);
	                }
	                 break;
	            }
	            case PHOTO_COMMENTS_GET_END:
	            {
	            	end();
	                break;
	            }
	            case COMMENTS_ADD_POST:
                {
                    addPhotoComment();
                    break;
                }
                case COMMENTS_ADD_POST_END:
                {
                    end();
                    dismissDialog(DLG_ADD_COMMAND);
                    photo_save.setEnabled(true);
                    if(msg.getData().getBoolean(RESULT) == true)
                    {
                        refreshCurrentComments();
                    	photo_comments.setText("");
                    	Toast.makeText(mContext, R.string.sns_operate_succeed, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                    	Toast.makeText(mContext, R.string.sns_operate_failed, Toast.LENGTH_SHORT).show();                        
                    }
                    
                    break;
                }
                case SIMPLE_USERINFO_GET:
                {
                    long[] uids = msg.getData().getLongArray("uids");
                    Log.d(TAG,"entering get Simple_userInfo_get method uids size is "+uids.length);
                    getSimpleUserInfo(uids);
                    break;
                }
            }            
        }
    }
	 @Override
    public void onLogin() 
    {       
        super.onLogin();
        {
            launchGetPhotoComments();
        }    
    }

	public void getSimpleUserInfo(long[] uids) {
	    Log.d(TAG," enter into getSimpleUserInfo "+uids.length);
        facebookA.getSimpleUsersAsync(uids, new FacebookAdapter()
        {
            @Override public void getSimpleUsers( final List<FacebookUser.SimpleFBUser> fusers)
            {
                Log.d(TAG, "get simple info suc="+fusers.size());
                orm.addFacebookSimpleUser(fusers);
                handler.obtainMessage(PHOTO_COMMENTS_UI).sendToTarget();
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                Log.d(TAG, "get simple info fail="+e.getMessage());               
            }
        });    
    }
    
	private SimpleFBUser getSimpleFBUserByUID(long uid,List<SimpleFBUser> fusers) {
	   for(SimpleFBUser fuser : fusers)
	   {
            if(fuser.uid == uid)
            {
                return fuser;
            }
	   }
	   return null;
    }
    private void loadCommentsUserInfo(List<PhotoComment> comments) 
    {
	    if(comments != null)
	    { 
	         ArrayList<Long> uid_list = new ArrayList<Long>();
	         for(PhotoComment comment : comments)
	         {
	             FacebookUser user = orm.getFacebookUser(comment.from);
	             if(user == null)
	             {
	                 Log.d(TAG," uid = "+ comment.from);
	                 addUIDToArrayList(comment.from,uid_list);
	             }
	         }
	         Log.d(TAG,"uid list is "+uid_list.size());
	         if(uid_list.size()>0)
	         {
	             Message msg = handler.obtainMessage(SIMPLE_USERINFO_GET);
	             msg.getData().putLongArray("uids", converArrayListToArray(uid_list));
	             msg.sendToTarget();
	         }	         
	    }
	}

    private long[] converArrayListToArray(ArrayList<Long> uid_list) {
        long[] uidArray = new long[uid_list.size()];
        for(int i = 0 ; i < uid_list.size() ; i++)
        {
            uidArray[i] = uid_list.get(i);
        }
        Log.d(TAG," converArrayListToArray uid array size is  "+uidArray.length);
        return uidArray;
    }

    private void addUIDToArrayList(long fromid, ArrayList<Long> uid_list) {
        if(uid_list.size() ==0 )
        {
            uid_list.add(fromid);
        }
        else
        {
            for(int i = 0 ; i < uid_list.size() ; i++)
            {
               if(fromid == uid_list.get(i))
               {
                   break;
               }
               else
               {
                   if(i == (uid_list.size()-1))
                   {
                       uid_list.add(fromid);
                   }
               }
            }  
        } 
    }

    @Override
    public void onLogout() 
    {
        super.onLogout();
    }
    
    public void registerAccountListener() 
    {   
        AccountManager.registerAccountListener("FacebookPhotoCommentsViewActivity", this);        
    }
    public void unregisterAccountListener() 
    {   
        AccountManager.unregisterAccountListener("FacebookPhotoCommentsViewActivity");        
    }
}
