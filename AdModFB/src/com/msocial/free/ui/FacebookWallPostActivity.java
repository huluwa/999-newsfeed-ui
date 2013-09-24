package com.msocial.free.ui;

import com.msocial.free.R;
import com.msocial.free.providers.SocialORM;
import com.msocial.free.ui.AccountListener.AccountManager;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookWallPostActivity extends FacebookBaseActivity
{
	private EditText contentEdit;
	private TextView textCount;
	private MyWatcher watcher;
	private long posttouid;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_wall_post);
        
        contentEdit = (EditText)this.findViewById(R.id.facebook_status_message_editor);
        contentEdit.setHint("Compose words");
  
        posttouid = this.getIntent().getLongExtra("posttouid", -1);
        contentEdit.setVerticalScrollBarEnabled(true);
        textCount = (TextView)this.findViewById(R.id.facebook_status_text_counter);
        watcher = new MyWatcher(); 	    
        contentEdit.addTextChangedListener(watcher);
        
        setTitle(R.string.facebook_WALL_POST_title);
        
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
	public void setTitle()
	{
		title = this.getString(R.string.facebook_WALL_POST_title);
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
	
	@Override
	public void titleSelected() 
    {		
		super.titleSelected();
		
		//send message to my wall
		if(isInProcess() == false)
		{
		    handler.obtainMessage(WALL_POST).sendToTarget();
		}
	}
	 
	@Override
	protected void createHandler() {
		handler = new PostHandler();		
	}

	
    final int WALL_POST = 0;	
    final int WALL_POST_END = 1;
    
	private class PostHandler extends Handler 
    {
        public PostHandler()
        {
            super();            
            Log.d(TAG, "new PostHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
            	case WALL_POST://update text
            	{	
            		postWallMessage(posttouid);
            		break;
            	}
            	case WALL_POST_END:
            	{
            		end();
    				setTitle(R.string.facebook_WALL_POST_title);
    				if(msg.getData().getBoolean(RESULT) == true)
    				{
    				    FacebookWallPostActivity.this.finish();
    				}
    				else
    				{
    					Toast.makeText(FacebookWallPostActivity.this, R.string.sns_post_failed, Toast.LENGTH_SHORT).show();
    				}
            		break;
            	}
            }
        }
    }
	
	private void postWallMessage(long uuid)
    {
    	if(facebookA != null)
		{
			String content= contentEdit.getText().toString().trim();
			if(content != null && content.length() > 0)
			{
				begin();
				
				synchronized(mLock)
		    	{
		    	    inprocess = true;
		    	}
		    	
			    facebookA.postWallAsync(uuid, content, new FacebookAdapter()
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
		                Message rmsg = handler.obtainMessage(WALL_POST_END);
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
			            	Message rmsg = handler.obtainMessage(WALL_POST_END);
			                rmsg.getData().putBoolean(RESULT, false);
			                rmsg.sendToTarget();
		             	}
		            }
		    	});
			}
		}
    }
	
	private class MyWatcher implements TextWatcher 
	{   
       public void afterTextChanged(Editable s) 
       {
    	   textCount.setText(String.format("%1$s", s.length()));
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }
	public void registerAccountListener() {
		AccountManager.registerAccountListener("FacebookWallPostActivity", this);		
	}
	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("FacebookWallPostActivity");		
	}
}
