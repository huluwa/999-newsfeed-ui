package com.msocial.freefb.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.ui.adapter.FacebookCommentsAdapter;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Stream;
import oms.sns.service.facebook.model.FacebookUser.SimpleFBUser;
import oms.sns.service.facebook.model.Stream.Comments;
import oms.sns.service.facebook.model.Stream.Comments.Stream_Post;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookCommentsActivity extends FacebookBaseActivity{
    private final static String TAG = "FacebookCommentsActivity";
	private EditText contentEdit;	
	private TextView textCount;
	private ListView comList;
    
	private String   post_id;
	private long     uid;
	private boolean  fornotificationsend;
	private long     source_id;
	private int      currentPos=0;
	private int      limit     = 100;
	private int      lastVisiblePos = 0;
	
	private List<String> commentsAdd = new ArrayList<String>();
	private Comments comments = new Comments();
	int totalcount;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_stream_comments_ui);        
        
        contentEdit = (EditText)this.findViewById(R.id.facebook_status_message_editor);
        contentEdit.setHint(R.string.facebook_wall_to_wall_compose);      
        contentEdit.setVerticalScrollBarEnabled(true);
            
        setTitle(R.string.facebook_comments_update_title);
        
        post_id = this.getIntent().getStringExtra("post_id");   
        uid     = this.getIntent().getLongExtra("uid", -1);       
        fornotificationsend = this.getIntent().getBooleanExtra("fornotificationsend", false);
        
        Button facebook_share_button = (Button)this.findViewById(R.id.facebook_share_button);
        facebook_share_button.setOnClickListener(shareClick);
        
        Stream.CommentsParcel cp = this.getIntent().getParcelableExtra("comments");
        if(cp != null)
        {
            totalcount = cp.group.count;
            source_id  = cp.source_id;
        	View facebook_comments_span = (View)this.findViewById(R.id.facebook_comments_span);
        	facebook_comments_span.setVisibility(View.VISIBLE);
        	comList = (ListView)this.findViewById(R.id.facebook_stream_comments_list);
        	comList.setOnCreateContextMenuListener(this);
        	loadCommentsUserInfo(cp.group);
        	comments = cp.group;
        	FacebookCommentsAdapter sa = new FacebookCommentsAdapter(this, comments, false);
            comList.setAdapter(sa);
            
        	if(totalcount>0 && totalcount != cp.group.stream_posts.size()){
        	    handler.obtainMessage(GET_COMMENTS).sendToTarget();
        	}
        }
        
        setTitle();
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);	        	
	        	facebookA = new AsyncFacebook(perm_session);	        	
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }     
    }
	 
    @Override protected void onDestroy() 
    {  
    	comList.setAdapter(null);
    	if(comments != null)
    	{
    	    comments.stream_posts.clear();
    	}
    	
    	commentsAdd.clear();
    	commentsAdd = null;
    	
    	super.onDestroy();
    }
	public View.OnClickListener  loadOlderClick= new View.OnClickListener() 
	{	
		public void onClick(View v) 
		{
			lastVisiblePos = comList.getFirstVisiblePosition();
			getCommentsByPostID();
		}
	};
	
	View.OnClickListener shareClick = new View.OnClickListener() 
	{		
		public void onClick(View v) {
			//send message to my wall		
			if(fornotificationsend == false)
			{
			    Log.d(TAG,"entering add commentts...");
		        handler.obtainMessage(ADD_COMMENTS).sendToTarget();
			}
			else//for notifications
			{  
			    Log.d(TAG,"entering send notifications...");
			    handler.obtainMessage(SEND_NOTIFICATIONS).sendToTarget();
			}				
		}
	};	
	
	public void setTitle() 
	{
		title = this.getString(R.string.facebook_comments_update_title);	
		if(fornotificationsend)
		{
			title = this.getString(R.string.facebook_send_notification_title);
		}
	}
	
	@Override protected void onResume() 
	{
	    super.onResume();
	}
	
	@Override
    protected void loadRefresh()
    {
	    if(this.isInProcess())
        {
	        showToast();
	        return;
        }
	    //get comments again      
        if(fornotificationsend == false)
        {
        	currentPos = 0;
        	lastVisiblePos = 0;
            handler.obtainMessage(GET_COMMENTS).sendToTarget();
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
    		launchFacebookLogin();
    	}
    	else
    	{
    		facebookA = new AsyncFacebook(perm_session);
    		perm_session.attachActivity(this);
    	}
    }

	/*
    @Override
	public void titleSelected() 
    {		
		super.titleSelected();
		
		//send message to my wall		
		if(fornotificationsend == false)
		{
		    Log.d(TAG,"entering add commentts...");
	        handler.obtainMessage(ADD_COMMENTS).sendToTarget();
		}
		else//for notifications
		{  
		    Log.d(TAG,"entering send notifications...");
		    handler.obtainMessage(SEND_NOTIFICATIONS).sendToTarget();
		}		
	}
	*/
    
   @Override
    protected void removeComment(String comment_id)
    {
       Message msg = handler.obtainMessage(REMOVE_COMMENT);
       msg.getData().putString("comment_id",comment_id);
       handler.sendMessage(msg);
     
    }    
	    
	@Override
	protected void createHandler() 
	{
		handler = new CommentsHandler();
	}
	
	//call update status
	final int ADD_COMMENTS       = 0;
	final int ADD_COMMENTS_END   = 1;
	final int SEND_NOTIFICATIONS = 2;
	final int SEND_NOTIFICATIONS_END = 3;
	final int GET_COMMENTS            = 4;
	final int GET_COMMENTS_END        = 5;
	final int REMOVE_COMMENT          = 6;
	final int RESET_COMMENT_UI      = 7;
	final int SIMPLE_USERINFO_GET   = 8;
	final int GET_COMMENTS_ACTION      = 9;
	
	private class CommentsHandler extends Handler 
    {
        public CommentsHandler()
        {
            super();            
            Log.d(TAG, "new CommentsHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case ADD_COMMENTS:
                {	
                	String content = contentEdit.getText().toString().trim();
                	if(content != null && content.length() > 0)
                	{
                	    addComments(content);
                	}
                	break;
                }            	
            	case ADD_COMMENTS_END://update text
            	{	
            		end();
            		dismissDialog(DLG_ADD_COMMAND);
            		if(msg.getData().getBoolean(RESULT) == true)
            		{
            			processResult();
            		}
            		else
            		{
            			Toast.makeText(FacebookCommentsActivity.this, R.string.facebook_add_comments_failed, Toast.LENGTH_SHORT).show();
            		}
            		break;
                }
            	case SEND_NOTIFICATIONS:
            	{
            		String content = contentEdit.getText().toString().trim();
                	if(content != null && content.length() > 0)
                	{
                	    sendNotification(content);
                	}
            		break;
            	}
            	case SEND_NOTIFICATIONS_END:
            	{
            		end();            		
            		
            		if(msg.getData().getBoolean(RESULT) == true)
            		{
            		    finish();
            		}
            		else
            		{
            			Toast.makeText(FacebookCommentsActivity.this, R.string.facebook_send_notification_failed, Toast.LENGTH_SHORT).show();
            		}
            		break;
            	}
            	case GET_COMMENTS:
            	{
            	    getCommentsByPostID();
            	    break;
            	}
            	case GET_COMMENTS_END:
            	{
            	    end();
            	    break;
            	}
            	case REMOVE_COMMENT:
            	{
            	    String commentid = msg.getData().getString("comment_id");
            	    removeCommentAsync(commentid);
            	    break;
            	}
            	case RESET_COMMENT_UI:
            	{
            	    String commentid = msg.getData().getString("comment_id");
            	    resetCommentUI(commentid);        	             	    
            	    break;
            	}
            	case SIMPLE_USERINFO_GET:
            	{  
            	    long[] uids = msg.getData().getLongArray("uids");
            	    Log.d(TAG,"entering get Simple_userInfo_get method uids size is "+uids.length);
            	    getSimpleUserInfo(uids);
            	    break;
            	}
            	case GET_COMMENTS_ACTION:
            	{
            		int prePos = msg.getData().getInt("currentPos");
            		copyComments(tmpcomments_result, prePos);
                    loadCommentsUserInfo(comments);
                	boolean hasmore = false;
                	if(currentPos+1 < totalcount)
                	{
                		hasmore = true;
                	}
                	
                    FacebookCommentsAdapter sa = new FacebookCommentsAdapter(mContext,comments, hasmore);
                    comList.setAdapter(sa);                    
                    comList.setSelection(lastVisiblePos);
                	
            		break;
            	}
            }
        }
    }
	
	void sendNotification(String content)
	{
	    if(existSession() == false)
        {
            return;
        }    
	    
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
		facebookA.sendNotificationsAsync(uid, content,new FacebookAdapter()
    	{
    		@Override public void sendNotifications(boolean suc)
            {
    			Log.d(TAG, "after notification="+suc);
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
                
                Message rmsg = handler.obtainMessage(SEND_NOTIFICATIONS_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.sendToTarget();
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
            	
            	Log.d(TAG, "send notification ex="+e.getMessage());
            	
            	if(isInAynscTaskAndStoped())
            	{
            		Log.d(TAG, "User stop passive");
            	}
            	else
            	{
	            	Message rmsg = handler.obtainMessage(SEND_NOTIFICATIONS_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.sendToTarget();
            	}
            }
    	});
	}
	
    private void getSimpleUserInfo(long[] uids) 
    {
        Log.d(TAG," enter into getSimpleUserInfo "+uids.length);
        facebookA.getSimpleUsersAsync(uids, new FacebookAdapter()
        {
            @Override public void getSimpleUsers( final List<FacebookUser.SimpleFBUser> fusers)
            {
                Log.d(TAG, "get simple info suc="+fusers.size());                
                
                orm.addFacebookSimpleUser(fusers);
                //just reset UI
                handler.post(new Runnable()
                {
                    public void run()
                    {
                    	boolean hasmore = false;
                    	if(currentPos+1 < totalcount)
                    	{
                    		hasmore = true;
                    	}
                    	lastVisiblePos = comList.getFirstVisiblePosition();
                        comList.setAdapter(null);
                        comList.setAdapter(new FacebookCommentsAdapter(FacebookCommentsActivity.this, comments, hasmore));
                        comList.setSelection(lastVisiblePos);
                    }
                });                 
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                Log.d(TAG, "get basic info fail="+e.getMessage());
               
            }
        });    
    }
    
    Comments tmpcomments_result;
    void getCommentsByPostID()
	{
    	if(this.isInProcess() == true)
 	    {
 	        return;
 	    }
    	 
        if(isSafeCallFacebook(true) == false)
        {
            return;
        }    
        
        begin();

        synchronized(mLock)
        {
	      inprocess = true;
        }
        
        for(int i=comList.getChildCount()-1;i>0;i--)            
        {
            View v = comList.getChildAt(i);
            if(Button.class.isInstance(v))
            {
                Button bt = (Button)v;
                bt.setText(getString(R.string.loading_string));
                break;
            }
        }
        
        int size = limit;
        if(totalcount<limit)
        {
        	size = totalcount;
        }
        	
        facebookA.getCommentsAsync(String.valueOf(post_id),new Long(source_id), currentPos, size, new FacebookAdapter()
        {            
            @Override public void getComments(Comments comments_result) 
            {
                if(comments_result!=null)
                {
                	tmpcomments_result = comments_result;
                	
                	handler.post(new Runnable()
                    {
                         public void run()
                         {
                        	 comList.setAdapter(null);                        	 
                        	 Message msg = handler.obtainMessage(GET_COMMENTS_ACTION);
                        	 msg.getData().putInt("currentPos", currentPos);                        	 
                        	 currentPos+= tmpcomments_result.stream_posts.size();
                        	 msg.sendToTarget();
                         }
                    });
                   
                }
                
                synchronized(mLock)
                {
                    inprocess = false;
                }                
                
                Message rmsg = handler.obtainMessage(GET_COMMENTS_END);
                rmsg.sendToTarget();
            }
            
            
            @Override public void onException(FacebookException e, int method) 
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                Log.d(TAG, "get Comments ex="+e.getMessage());
                
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                    Message rmsg = handler.obtainMessage(GET_COMMENTS_END);
                    rmsg.sendToTarget();
                }
            }
        });
	}
	  
    protected void copyComments(Comments coms, int prePos) 
    {
    	if(prePos == 0)//just do for first time, to remove the repeat
    	{
	    	if(coms.stream_posts.size() > 0)
	    	{
	    		synchronized(comments)
	        	{
	    			for(int i=0;i<coms.stream_posts.size();i++)
	    			{
	    				Stream_Post item = coms.stream_posts.get(i);
	    				boolean isExist = false;
	    				for(int j=0;j<comments.stream_posts.size();j++)
	    				{
	    					Stream_Post exist = comments.stream_posts.get(j);
	    					if(item.id.equalsIgnoreCase(exist.id))
	    					{
	    						isExist=true;
	    						//update the content						
	    						exist.despose();
	    						exist=null;
	    						
	    						comments.stream_posts.set(j, item);
	    						break;
	    					}
	    				}
	    				
	    				if(isExist == false)
	    				{
	    					comments.stream_posts.add(item);				
	    				}
	    		    }	
	        	}
	    	}
    	}
    	else
    	{
    		comments.stream_posts.addAll(coms.stream_posts);
    	}
	}

	protected void loadCommentsUserInfo(Comments comments) 
    {
        if(comments != null)
        {  
            List<Stream_Post> streampost_list = comments.stream_posts;
            ArrayList<Long> uid_list = new ArrayList<Long>();
            for(Stream_Post stream_post : streampost_list)
            {
                FacebookUser user = orm.getFacebookUser(stream_post.fromid);
                if(user == null)
                {
                    // construct uids which need get userinfo from website
                    //Log.d(TAG," uid = "+ stream_post.fromid);
                    addUIDToArrayList(stream_post.fromid, uid_list);
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
            boolean exist = false;
            for(int i = 0 ; i < uid_list.size() ; i++)
            {
               if(fromid == uid_list.get(i))
               {
                   exist = true;
                   break;
               }               
            }  
            
            if(exist == false)
            {
                uid_list.add(fromid);
            }
        }
    }

    void removeCommentAsync(String comment_id)
    {  
        synchronized(mLock)
        {
            inprocess = true;
        }
        final String commentid = comment_id;
        facebookA.removeCommentAsync(String.valueOf(comment_id),new FacebookAdapter()
        {
            @Override public void removeComments(boolean suc)
            {
                Log.d(TAG, "after remove comments="+suc);
                
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                if(suc)
                {
                   Message msd = handler.obtainMessage(RESET_COMMENT_UI);
                   msd.getData().putString("comment_id", commentid);
                   handler.sendMessage(msd);
                }
                
                Message rmsg = basichandler.obtainMessage(FACEBOOK_REMOVE_COMMENT_END);
                rmsg.getData().putBoolean(RESULT, suc);
                rmsg.sendToTarget();
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                Log.d(TAG, "remove comments exception "+e.getMessage());
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
                    Message rmsg = basichandler.obtainMessage(FACEBOOK_REMOVE_COMMENT_END);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.sendToTarget();
                }
                
            }
        });
         
    }
    
    void resetCommentUI(String commentid)
    {
        FacebookCommentsAdapter sa = (FacebookCommentsAdapter) comList.getAdapter();
        Comments com = sa.comment;       
        List<Stream_Post> postlist = com.stream_posts;
        for(Stream_Post post : postlist)
        {
            if(post.id.equals(commentid))
            {
                postlist.remove(post);
                break;
            }
        }
        ((BaseAdapter)sa).notifyDataSetChanged();
    }
   
	void addComments(String content)
	{
	    Log.d(TAG," start to add comments");
	    if(isInProcess()== true)
	    {
	        return;
	    }
	    if(existSession() == false)
        {
            return;
        }    
	    
		begin();
		showDialog(DLG_ADD_COMMAND);
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
		facebookA.streamAddCommentsAsync(String.valueOf(post_id), content,new FacebookAdapter()
    	{
    		@Override public void streamAddComments(String commandID)
            {
    			Log.d(TAG, "after add comments="+commandID);
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				commentsAdd.add(commandID);
                
                Message rmsg = handler.obtainMessage(ADD_COMMENTS_END);
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
	            	Message rmsg = handler.obtainMessage(ADD_COMMENTS_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.sendToTarget();
            	}
            }
    	});
		
	}


	@Override
	public void onLogin() 
	{		
		super.onLogin();
		
		//get comments again      
        if(fornotificationsend == false)
        {
            handler.obtainMessage(GET_COMMENTS).sendToTarget();
        }    
	}
	
	@Override
	public void onLogout() 
	{
		super.onLogout();
	}
	
	public void registerAccountListener() 
	{	
	    AccountManager.registerAccountListener("FacebookCommentsActivity", this);        
	}
	public void unregisterAccountListener() 
	{	
	    AccountManager.unregisterAccountListener("FacebookCommentsActivity");        
	}
	
	private boolean processResult()
	{
		 boolean ret = false;
		 if(this.commentsAdd.size() > 0)
         {
         	Intent intent = new Intent();
         	intent.putExtra("post_id", post_id);
         	intent.putStringArrayListExtra("newcomments", (ArrayList<String>) commentsAdd);
         	this.setResult(RESULT_OK, intent);
         	
         	this.finish();
         	ret = true;
         }
		 return ret;
	}
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {         
        	Log.d(TAG, "KEYCODE_BACK coming="+this);
            stopLoading();
            restoreTitle();
            System.gc();
            
           if(processResult() == true)
           {
        	   return true;
           }
                
        }
        return super.onKeyDown(keyCode, event);
    }

}
