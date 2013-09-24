package com.ast.free.ui;

import java.util.ArrayList;
import java.util.List;

import com.ast.free.*;
import com.ast.free.providers.SocialORM;
import com.ast.free.ui.AccountListener.AccountManager;
import com.ast.free.ui.adapter.MailboxThreadParcel;
import com.ast.free.ui.adapter.MessageThreadAdapter;
import com.ast.free.ui.adapter.MessageThreadInfoParcel;
import com.ast.free.ui.view.MessageThreadInfoItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.MailboxMessage;
import oms.sns.service.facebook.model.MessageThreadInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FacebookMessageActivity extends FacebookBaseActivity
{
   private final String TAG      = "FacebookMessageActivity"; 
   private final long timetoload = 60L*1000;
   
   private ListView messages;
   private View     searchSpan;
   private EditText keyEdit;
   private Button   searchDo;
	
   //just show first limit request progress
   int currentPos   = 0;   
   int limit        = 20;
   
   final   int UPDATE_SYMBOL = 4;
   boolean switch_type    = false;
   
   private Cursor cursor;   
   private Cursor searchCursor;
   
   private int mTypeResIdx;
   private MyWatcher watcher;
   private boolean withfooterview = true;
   
   //for tab control     
   private final int  MESSAGES =0;
   private final int  SENT     =1;
   private final int  UPDATE   =2;
   
   private Button msg_button;
   private Button sent_button;
   private Button update_button;
   private boolean onresumeTag = false;
   int lastVisiblePos = -1;
   
   @Override
   protected void enableProgress()
   {
       this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
   }
   
   @Override
   public void onCreate(Bundle savedInstanceState){
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.facebook_message);
	   
	   messages = (ListView)this.findViewById(R.id.facebook_message_list);
	   messages.setFocusable(true);
	   messages.setFocusableInTouchMode(true);
	   messages.setOnCreateContextMenuListener(this);
	   messages.setOnItemClickListener(listItemClickListener);
	   
	   searchSpan = this.findViewById(R.id.facebook_search_span);
       searchSpan.setVisibility(View.VISIBLE);
	   
       keyEdit = (EditText)this.findViewById(R.id.embedded_text_editor);
       watcher = new MyWatcher();         
       keyEdit.addTextChangedListener(watcher);
       searchDo = (Button)this.findViewById(R.id.search_do);
       searchDo.setOnClickListener(seachListener);       
	   
       View facebook_tab_span = this.findViewById(R.id.facebook_tab_span);
       facebook_tab_span.setVisibility(View.VISIBLE);
       
       msg_button = (Button)this.findViewById(R.id.facebook_tab_message_button);
	   msg_button.setId(1);
       sent_button = (Button)this.findViewById(R.id.facebook_tab_sent_button);
       sent_button.setId(2);        
       update_button = (Button)this.findViewById(R.id.facebook_tab_update_button);
       update_button.setId(3);
       
       msg_button.setOnClickListener(showContentClick);
       sent_button.setOnClickListener(showContentClick);
       update_button.setOnClickListener(showContentClick);
       
       updateSubTabUIInit();
       
	   SocialORM.Account account = orm.getFacebookAccount();
       if(checkFacebookAccount(this, account))
       {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session!=null)
        	{
            	perm_session.attachActivity(this);
            	facebookA = new AsyncFacebook(perm_session);
            	    		
            	loadMainBox(true);            	
                showUI(true);
        	}
        	else
        	{
        	    launchFacebookLogin();
        	}
       }
       
       setTitle(title);       
    }
   
   public int getType()
   {
	   return mTypeResIdx;
   }
   
   View.OnClickListener showContentClick = new OnClickListener()
	{
	    public void onClick(View v)
	    {
	        boolean changed = false;
	        if(v.getId() == 1)
	        {
	            if(mTypeResIdx != MESSAGES)
	            {
	                changed = true;
	            }
	            mTypeResIdx = MESSAGES;   	        
   	        
	        }
	        else if(v.getId() == 2)
	        {
	            if(mTypeResIdx != SENT)
                {
                   changed = true;
                }
	            mTypeResIdx = SENT;	            
	        }
	        else if(v.getId() == 3)
	        {
	            if(mTypeResIdx != UPDATE)
                {
                   changed = true;
                }
	            mTypeResIdx = UPDATE;	            
	        }
	        
	        if(changed == true)//don't repeat to show
	        {
	            updateSubTabUI();
	        }
	    }
	};
	
	private void updateSubTabUIInit()
	{
	    title = getString(MessageMenuResId[mTypeResIdx]);
        setTitle(title);
        if(mTypeResIdx == MESSAGES)
        {
            msg_button.setBackgroundResource(R.drawable.facebook_profile_button_white);                
            sent_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);
            update_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);
            
            msg_button.setTextColor(Color.BLACK);        
            sent_button.setTextColor(Color.WHITE);
            update_button.setTextColor(Color.WHITE);
            
            showUI(true);
        }
        
        //if for info, hide other
        else if(mTypeResIdx == SENT)
        {                            
            msg_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);                
            sent_button.setBackgroundResource(R.drawable.facebook_profile_button_white);
            update_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);
            
            msg_button.setTextColor(Color.WHITE);        
            sent_button.setTextColor(Color.BLACK);
            update_button.setTextColor(Color.WHITE);
            
            showUI(true);
        }
        
        //if for info, hide other
        else if(mTypeResIdx == UPDATE)
        {                            
            msg_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);                
            sent_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);
            update_button.setBackgroundResource(R.drawable.facebook_profile_button_white);
            
            msg_button.setTextColor(Color.WHITE);        
            sent_button.setTextColor(Color.WHITE);
            update_button.setTextColor(Color.BLACK);
            
            showUI(true);
        }
	}
	
	private void updateSubTabUI()
    {
		title = getString(MessageMenuResId[mTypeResIdx]);
		setTitle(title);
		
		//reload the message
		loadMainBox(true);
        //if for status, show wall, hide others
        if(mTypeResIdx == MESSAGES)
        {
            msg_button.setBackgroundResource(R.drawable.facebook_profile_button_white);                
            sent_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);
            update_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);
            
            msg_button.setTextColor(Color.BLACK);        
            sent_button.setTextColor(Color.WHITE);
            update_button.setTextColor(Color.WHITE);
            
            showUI(true);
        }
        
        //if for info, hide other
        else if(mTypeResIdx == SENT)
        {                            
        	msg_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);                
            sent_button.setBackgroundResource(R.drawable.facebook_profile_button_white);
            update_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);
            
            msg_button.setTextColor(Color.WHITE);        
            sent_button.setTextColor(Color.BLACK);
            update_button.setTextColor(Color.WHITE);
            
            showUI(true);
	    }
        
        //if for info, hide other
        else if(mTypeResIdx == UPDATE)
        {                            
        	msg_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);                
            sent_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);
            update_button.setBackgroundResource(R.drawable.facebook_profile_button_white);
            
            msg_button.setTextColor(Color.WHITE);        
            sent_button.setTextColor(Color.WHITE);
            update_button.setTextColor(Color.BLACK);
            
            showUI(true);
	    }
    }  
	
	@Override
    protected boolean  goNextPage()
	{
    	super.goNextPage();
    	boolean changed = false;
    	if(mTypeResIdx == MESSAGES)
    	{	
    		changed = true;
    		mTypeResIdx = SENT;    
	    }
    	else if(mTypeResIdx == SENT)
    	{
    		changed   = true;
    		mTypeResIdx = UPDATE;
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
    	if(mTypeResIdx == UPDATE)
    	{	
    		changed   = true;
    		mTypeResIdx  = SENT; 	         	        
	    }
    	else if(mTypeResIdx == SENT)
    	{
    		changed   = true;
    		mTypeResIdx  = MESSAGES;
    	}	    	 
    	if(changed == true)
    	{
	        updateSubTabUI();
	        return true;
    	}
    	return false;
	}   
    
   @Override
   protected void onNewIntent(Intent intent) 
   {		
		super.onNewIntent(intent);		
		int tmp = intent.getIntExtra("type", 0);
		loadMessages(tmp);      
   }
   
   private void loadMessages(int tmp)
   {
	   setTitle(MessageMenuResId[mTypeResIdx]);
	   //call 
	   if (mTypeResIdx == tmp) 
	   {
		   switch_type = false;
	       return;
	   }

       switch_type =  true;
	   refreshUIToInit(tmp);	   
	   //title will be changed
	   title = this.getString(MessageMenuResId[mTypeResIdx]);
   }
   
    @Override
    public void onLogin() 
    {		
	   	super.onLogin();
	   	
	   	Log.d(TAG, "call onLogin="+this);
	   	
	   	if(facebookA != null)
	   	{
	   		loadMainBox(true);
	   	}
	}
   
    @Override
    protected void onRestoreInstanceState(Bundle arg0) 
    {	
	    super.onRestoreInstanceState(arg0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {	
	    super.onSaveInstanceState(outState);
	    
	    //save the content	    
	}

	public void setTitle()
    {
    	title = getString(MessageMenuResId[mTypeResIdx]);
    }
    
    AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			Log.d(TAG, "message Item clicked");
			
			if(MessageThreadInfoItemView.class.isInstance(v))
			{
			     MessageThreadInfoItemView fv= (MessageThreadInfoItemView)v;
				 Intent intent = new Intent(mContext, FacebookMailDetailActivity.class);				
				 //set mail conversation detail information
				 MessageThreadInfo mthread    = fv.getMailboxThread();				 
				 MessageThreadInfoParcel mail = new MessageThreadInfoParcel(fv.getMailboxThread());
				 
				 intent.putExtra("mailboxthread", mail);				 
				 intent.putExtra("tid",      mthread.thread_id);
				 boolean frominbox = mTypeResIdx==0?true:false;
				 intent.putExtra("frominbox",frominbox);
				 
				 //intent.putParcelableArrayListExtra("messages", (ArrayList<MailboxMessage>)(orm.getMailMessages(mthread.thread_id)));//(ArrayList<MailboxMessage>)mthread.messages);
				 
				 ((FacebookBaseActivity)mContext).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_MAIL_DETAIL);
			}
		}
	};
   
   View.OnClickListener seachListener = new View.OnClickListener()
   {
       public void onClick(View v) 
       {
           String key = keyEdit.getText().toString().trim();
           doSearch(key);
       }
   };
   
   private void doSearch(String key)
   {
       if(searchCursor != null)
       {
           searchCursor.close();
           searchCursor = null;
       }
       
       searchCursor = orm.searchMailCursor(mTypeResIdx,key);
       if(isEmpty(key) == true)
       {
           showUI(true);
       }
       else
       {
           if(searchCursor != null)
           {
               MessageThreadAdapter adapter = new MessageThreadAdapter(FacebookMessageActivity.this,searchCursor,orm,mTypeResIdx);
               messages.setAdapter(adapter);
           }
           else
           {
               messages.setAdapter(null);
           }
       }       
   }
   
   
   
    @Override
    protected void doMailReply(Object obj) 
    {	 
    	if(MessageThreadInfo.class.isInstance(obj))
    	{	 
    	    Intent intent = new Intent(mContext, FacebookMailActivity.class);				
		    //set mail conversation detail information
    	    MessageThreadInfoParcel mail = new MessageThreadInfoParcel((MessageThreadInfo)obj);
		    intent.putExtra("mailboxthread", mail);				 
		    intent.putExtra("reply",    true);
		    ((FacebookBaseActivity)mContext).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_MAIL_DETAIL);
    	}
	}

    @Override
	protected void doMailForward(Object obj) 
	{
    	if(MessageThreadInfo.class.isInstance(obj))
    	{
	    	Intent intent = new Intent(mContext, FacebookMailActivity.class);	
	    	MessageThreadInfoParcel mail = new MessageThreadInfoParcel((MessageThreadInfo)obj);
			intent.putExtra("mailboxthread", mail);				 
			intent.putExtra("forward",    true);
			intent.putExtra("content",    ((MessageThreadInfo)obj).subject);
			((FacebookBaseActivity)mContext).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_MAIL_DETAIL);
    	}
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
          switch(requestCode)
          {  
                case FACEBOOK_MAIL_DETAIL:
                {
                    Log.d(TAG, "return from mail detail");
                    //do we need do this?
                    if(intent != null && intent.getBooleanExtra("markread", false) == true)
                    {
                        long tid = intent.getLongExtra("tid", -1);
                        MessageThreadInfo mm = orm.getMailThread(tid);
                        if(mm != null)
                        {
                            mm.unread = 0;
                            orm.addMailThread(mm);
                            
                            showUI(false);
                        }
                    }
                    
                    break;
                }
          }
          
          super.onActivityResult(requestCode, resultCode, intent);
    }
	
    /*
    @Override
	public void titleItemSelected(int position, long id) 
    {		
		super.titleItemSelected(position, id);
		
		setTitle(MessageMenuResId[mTypeResIdx]);
		
		//call 
		if (mTypeResIdx == position) 
		{
		    switch_type = false;
	        return;
	    }

        switch_type =  true;
	    refreshUIToInit(position);
	    //title will be changed
	    title = this.getString(MessageMenuResId[mTypeResIdx]);
	}
	*/

    private void checkAndSetType(int type) 
    {
        if (type < 0 || type > MessageMenuResId.length) {
            type = mDefaultTypeIdx;
        }
        mTypeResIdx = type;        
    }
    
    void refreshUIToInit(int type)
    {
    	checkAndSetType(type);    	
        setTitle(MessageMenuResId[mTypeResIdx]);
        //call next view
        showUI(true);
    }
    
	private void showUI(boolean refresh) 
	{
		if(cursor != null)
    	{
    		cursor.close();
    	}
		
	    if(this.mTypeResIdx == 0)
	    {
	    	cursor = orm.getMailInboxThreadsCursor();
	    }
        else if(this.mTypeResIdx == 1)
        {
            cursor = orm.getMailOutboxThreadsCursor();         
        }
        else if(this.mTypeResIdx == 2)
        {
            cursor = orm.getMailUpdateThreadsCursor();            
        }
	    
		if(cursor != null)
		{
		    if(refresh == false)
		    {
		        if(onresumeTag == false) //first time back to message UI scroll to the position when leave this UI
		        {
		            if(switch_type == false)
	                {
	                    lastVisiblePos = messages.getLastVisiblePosition(); 
	                }
	                else
	                {
	                    //switch ui. and recover switch_type = false
	                    switch_type = false;
	                } 
		        }
		       
		    }
		    else
		    {
		        lastVisiblePos = -1;
		    }
		    
		    Log.d(TAG," last visible position is "+lastVisiblePos);
            MessageThreadAdapter adapter = new MessageThreadAdapter(FacebookMessageActivity.this,cursor,orm,withfooterview,mTypeResIdx);
            messages.setAdapter(adapter);
            if(refresh == false)
            {
                messages.setSelection(lastVisiblePos);    
            } 
		}
		else
		{
		    messages.setAdapter(null);
		}
	}	
	
	public View.OnClickListener loadOlderSentClick = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            Log.d(TAG, "load older sent message");               
            loadOlderSent();
        }
    };
	
	public View.OnClickListener loadOlderInboxClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            Log.d(TAG, "load older message");               
            loadOlderInbox();
        }
    };
    
	public View.OnClickListener loadOlderUpdateClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            Log.d(TAG, "load update message");               
            loadOlderUpdate();
        }
    };
    
    private void loadOlderUpdate()
    {
    	Log.d(TAG, "load old for update");
    	if(cursor != null)
        {
            currentPos = cursor.getCount();
        }
    	getMailUpdateSummaryInfo(false);
    }
    private void loadOlderSent()
    {
        Log.d(TAG, "load old for send");
        if(cursor != null)
        {
            currentPos = cursor.getCount();
        }
        getSendMailBoxSummaryInfo(false);
    }
    
    private void loadOlderInbox()
    {
        Log.d(TAG, "load old for inbox");
        //
        if(cursor != null)
        {
            currentPos = cursor.getCount();
        }
        getMailInBoxSummaryInfo(false);
    }

    @Override
    protected void doClearCache()
    {
        currentPos   = 0;        
        orm.cleanAllMail();
        
        if(cursor != null)
        {
            cursor.close();
            cursor = null;
        }
        
        showUI(true);   
    }

    
	@Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onresumeTag is="+onresumeTag);
        if(onresumeTag == true)
        {
            showUI(false);
        }
        onresumeTag = false;
    }
	
	

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onresumeTag is="+onresumeTag);
        lastVisiblePos = messages.getFirstVisiblePosition();
        onresumeTag = true;
    }

    @Override
	protected void loadRefresh()
	{
		super.loadRefresh();

        if(mTypeResIdx == 1)
        {
            if(inprocessSend == true)//when in inbox request the mail, we need get the send
            {
                showToast();
                return;
            }
        }
        else if(mTypeResIdx == 0)
        {
            if(inprocess == true)
            {
                showToast();
                return;
            }
        }
        else if(mTypeResIdx == 2)
        {
            if(inprocessupdate== true)
            {
                showToast();
                return;
            }
        }
		currentPos   = 0;		
		loadMainBox(true);
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
    		perm_session.attachActivity(this);
    		facebookA = new AsyncFacebook(perm_session);
    		loadMainBox(true);
    	}
    }

	private void loadMainBox(boolean refresh) 
	{	
	    showUI(refresh);
	    switch(mTypeResIdx)
	    {
		    case 0:
		    {
				Message msg = handler.obtainMessage(FACEBOOK_MAIL_INBOX_GET);
				msg.getData().putBoolean("refresh", refresh);
				msg.sendToTarget();
				break;
		    }
		    case 1:
		    {
		        Message msg = handler.obtainMessage(FACEBOOK_MAIL_SEND_GET);
				msg.getData().putBoolean("refresh", refresh);
				handler.sendMessageDelayed(msg, 1*1000);
				break;
		    }
		    case 2:
		    {
		    	Message msg = handler.obtainMessage(FACEBOOK_MAIL_UPDATE_GET);
			    msg.getData().putBoolean("refresh", refresh);
			    handler.sendMessageDelayed(msg, 1*1000);
			    break;
		    }
	    }
	}

    @Override
	protected void createHandler() {
		handler = new MsgHandler();		
	}
	private class MsgHandler extends Handler 
	{
        public MsgHandler()
        {
            super();            
            Log.d(TAG, "new MsgHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case FACEBOOK_MAIL_INBOX_GET:
                {
                    //getMailInBoxSummaryInfo from web site and then save to databases
                    boolean refresh = msg.getData().getBoolean("refresh");
                	getMailInBoxSummaryInfo(refresh);
                	break;
                }     
                case FACEBOOK_MAIL_SEND_GET:
                {
                    boolean refresh = msg.getData().getBoolean("refresh");
                	getSendMailBoxSummaryInfo(refresh);
                	break;
                }
                case FACEBOOK_MAIL_UPDATE_GET:
                {
                    boolean refresh = msg.getData().getBoolean("refresh");
                	getMailUpdateSummaryInfo(refresh);
                	break;
                }
                case FACEBOOK_MAIL_INBOX_UI:
                {	
                    boolean refresh = msg.getData().getBoolean("refresh");
                	if(mTypeResIdx == 0 )
                	{
                		showUI(refresh);
                	}                	
                	//continue to get the send detail
                	break;
                }                
                case FACEBOOK_MAIL_SEND_UI:
                {       
                    boolean refresh = msg.getData().getBoolean("refresh");
                	if(mTypeResIdx == 1)
                	{	
                		showUI(refresh);
                	}
                	
                	//continue to get the send detail
                	break;
                }
                case FACEBOOK_MAIL_UPDATE_UI:
                {   
                    boolean refresh = msg.getData().getBoolean("refresh");
                	if(mTypeResIdx == 2)
                	{	
                		showUI(refresh);
                	}
                	
                	//continue to get the send detail
                	break;
                }
                case FACEBOOK_MAIL_INBOX_GET_END:
                {
                	//just the first time get inbox show the end progress
                    if(mTypeResIdx == 0)
                    {
                	    end();
                	    //set load older button text
                        //process for UI
                        showFooterViewText(getString(R.string.load_older_msg));
                    }
                     
                	if(mTypeResIdx == 0)
                	{
                		SelectFirstBox();
                	}                	
                	break;
                }
                case FACEBOOK_MAIL_SEND_GET_END:
                {	
                    if(mTypeResIdx == 1)
                    {
                	    end();
                	    //set load older button text
                        //process for UI
                        showFooterViewText(getString(R.string.load_older_msg));  
                    }
                	
                	if(mTypeResIdx == 0)
                	{
                		SelectFirstBox();
                	}                	
                	   	
                	break;
                }
                case FACEBOOK_MAIL_UPDATE_GET_END:
                {
                	//just the first time get inbox show the end progress
                    if(mTypeResIdx == 2)
                    {
                	    end();
                	    //set load older button text
                        //process for UI
                        showFooterViewText(getString(R.string.load_older_msg));
                    }                   
                    
                	if(mTypeResIdx == 0)
                	{
                		SelectFirstBox();
                	}                	
                	break;
                }
            }
        }
	}
	
	public boolean inprocessSend=false;
	public void getSendMailBoxSummaryInfo(final boolean refresh)
	{
		if(this.isFinishing() == true)
		{
			Log.d(TAG, "I am finishing, return");
			return ;
		}
		
	    if(mTypeResIdx == 1)
        {
    	    if(inprocessSend == true)//when in inbox request the mail, we need get the send
    	    {
    	        Log.d(TAG, "send i am still in run");
    	        return;
    	    }	    
	        begin();
        }
	    //set load older button text
        //process for UI
	    showFooterViewText(getString(R.string.loading_string));
		//notifyLoading();
    	Log.d(TAG, "before get loading send mailbox");
    	
    	synchronized(mLock)
    	{
    	    inprocessSend = true;
    	}
    	boolean hasProgress = false;
    	if(mTypeResIdx == 1)
        {
    	    hasProgress = true;
        }
    	facebookA.getMessageThreadAsync(1, -1, limit, currentPos, hasProgress, new FacebookAdapter()
    	{
    		@Override public void getMessageThread(List<MessageThreadInfo> threads)
            {
    			Log.d(TAG, "out mail ids="+threads.size());
				synchronized(mLock)
		    	{
				    inprocessSend = false;
		    	}
				if(threads.size()==20)
                {
                    withfooterview = true;
                }
                else
                {
                    withfooterview = false;
                }
				currentPos += threads.size();
				//addOutThread(threads);
				orm.addOutMailThread(threads);
				if(donotcallnetwork == false)//I am still alive
                {
                    Message msd = handler.obtainMessage(FACEBOOK_MAIL_SEND_UI);
                    msd.sendToTarget();
                    //cancelNotify();
                }       
                
                Message msd = handler.obtainMessage(FACEBOOK_MAIL_SEND_GET_END);
                msd.getData().putBoolean(RESULT, true);
                msd.getData().putInt("count", threads.size());
                handler.sendMessage(msd);
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	if(donotcallnetwork == false)//I am still alive
                {
            	    //cancelNotify();
                }
            	synchronized(mLock)
		    	{
            	    inprocessSend = false;
		    	}
            	
            	if(isInAynscTaskAndStoped())
            	{
            		Log.d(TAG, "User stop passive");
            	}
            	else
            	{
	            	 Message msd = handler.obtainMessage(FACEBOOK_MAIL_SEND_GET_END);
	                 msd.getData().putBoolean(RESULT, false);
	                 handler.sendMessage(msd);
            	}
            }
    	});
	}
	
	public void requery(Cursor cursor)
	{
	    if(cursor != null)
	    {
	        cursor.requery();
	    }
	}

	public void getMailInBoxSummaryInfo(final boolean refresh) 
	{		
		if(this.isFinishing() == true)
		{
			Log.d(TAG, "I am finishing, return");
			return ;
		}
		
	    if(this.isInProcess())
        {
            Log.d(TAG, "inbox i am still in run");
            return;
        }
	    if(mTypeResIdx == 0)
        {
	        begin();
        }
	    showFooterViewText(getString(R.string.loading_string));
		//notifyLoading(); 
    	Log.d(TAG, "before get inbox mail");
    	
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}
    	
    	boolean hasProgress = false;
    	if(mTypeResIdx == 0 )
    	{	
    		hasProgress = true;
    	}
    	
    	facebookA.getMessageThreadAsync(0,-1, limit,currentPos, hasProgress, new FacebookAdapter()
    	{
    		@Override public void getMessageThread(List<MessageThreadInfo> threads)
            {
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
				Log.d(TAG, "currentInPos = "+currentPos + " mail ids size="+threads.size());	
    			currentPos += threads.size();	
    			//addInThread(threads);
    			orm.addInMailThread(threads);
    			if(threads.size()==20)
    			{
    			    withfooterview = true;
    			}
    			else
    			{
    			    withfooterview = false;
    			}
    			// TODO save message thread to database orm.addMailThread(threads);    			
                if(donotcallnetwork == false)//I am still alive
                {
					Message msd = handler.obtainMessage(FACEBOOK_MAIL_INBOX_UI);
                    msd.getData().putBoolean("refresh", refresh);
                    msd.sendToTarget();
	            	//cancelNotify();
                }       
                //get all messages of threads
                Message msd = handler.obtainMessage(FACEBOOK_MAIL_INBOX_GET_END);
                msd.getData().putBoolean(RESULT, true);
                msd.getData().putInt("count",threads.size());
                handler.sendMessage(msd);
            }
    		
            @Override public void onException(FacebookException e, int method) 
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
	            	Message msd = handler.obtainMessage(FACEBOOK_MAIL_INBOX_GET_END);
	                msd.getData().putBoolean(RESULT, false);
	                handler.sendMessage(msd);
            	}
            }
    	});
	}	
	
	public boolean inprocessupdate=false;
	public void getMailUpdateSummaryInfo(final boolean refresh) 
	{		
		if(this.isFinishing() == true)
		{
			Log.d(TAG, "I am finishing, return");
			return ;
		}
		
	    if(inprocessupdate == true)
        {
            Log.d(TAG, "inbox i am still in run");
            return;
        }
	    if(mTypeResIdx == 2)
        {
	        begin();
        }
	    //set load older button text
        //process for UI
	    showFooterViewText(getString(R.string.loading_string));
		//notifyLoading(); 
    	Log.d(TAG, "before get update mail");
    	
    	synchronized(mLock)
    	{
    		inprocessupdate = true;
    	}
    	
    	boolean hasProgress = false;
    	if(mTypeResIdx == 2 )
    	{	
    		hasProgress = true;
    	}
    	
    	facebookA.getMessageThreadAsync(UPDATE_SYMBOL,-1, limit,currentPos, hasProgress, new FacebookAdapter()
    	{
    		@Override public void getMessageThread(List<MessageThreadInfo> threads)
            {
				synchronized(mLock)
		    	{
					inprocessupdate = false;
		    	}
				
				Log.d(TAG, "mail ids="+threads.size());	
				if(threads.size()==20)
                {
                    withfooterview = true;
                }
                else
                {
                    withfooterview = false;
                }
				currentPos += threads.size();	
    			
    			orm.addUpdateMailThread(threads);
    			
    			//batch get object_id info
    			/*Message mssg = handler.obtainMessage(FACEBOOK_GET_UPDATE_AUTHORINFO);
    			ArrayList<Long> object_ids = new ArrayList<Long>();
    			for(int i=0;i<threads.size();i++)
    			{
    			    MessageThreadInfo info = threads.get(i);
    			    long id = info.object_id;
    			    if(object_ids.contains(id))
    			    {
    			        object_ids.add(id);
    			    }
    			}
    			mssg.getData()*/
				
    			// TODO save message thread to database orm.addMailThread(threads);    			
                if(donotcallnetwork == false)//I am still alive
                {
					Message msd = handler.obtainMessage(FACEBOOK_MAIL_UPDATE_UI);
					msd.getData().putBoolean("refresh", refresh);
					msd.sendToTarget();
	            	//cancelNotify();
                }       
                
                Message msd = handler.obtainMessage(FACEBOOK_MAIL_UPDATE_GET_END);
                msd.getData().putBoolean(RESULT, true);
                msd.getData().putInt("count",threads.size());
                handler.sendMessage(msd);
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	synchronized(mLock)
		    	{
            		inprocessupdate = false;
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
	            	Message msd = handler.obtainMessage(FACEBOOK_MAIL_UPDATE_GET_END);
	                msd.getData().putBoolean(RESULT, false);
	                handler.sendMessage(msd);
            	}
            }
    	});
	}	

    private void showFooterViewText(String text) {
        for(int i= messages.getChildCount()-1;i>0;i--)            
        {
            View v = messages.getChildAt(i);
            if(Button.class.isInstance(v))
            {
                Button bt = (Button)v;
                bt.setText(text);
                break;
            }
        } 
    }

    private void notifyLoading() 
    {
    	if(notify != null)
    	{
    		if(mTypeResIdx == 0)
    	        notify.notifyOnce(R.string.facebook_mailbox_inbox_loading, R.drawable.facebook_logo, 30*1000);
    		else if(mTypeResIdx == 1)
    	        notify.notifyOnce(R.string.facebook_mailbox_sent_loading, R.drawable.facebook_logo, 30*1000);
    		else if(mTypeResIdx == 2)
    	        notify.notifyOnce(R.string.facebook_mailbox_update_loading, R.drawable.facebook_logo, 30*1000);
    	}
	}
    
    private void SelectFirstBox()
    {
    	loadMessages(0);
    }
    
    
	@Override
    protected void onDestroy() 
	{        
        super.onDestroy();
        try{
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }    
        }
        catch(Exception e){}
        
        
        
        try{
            if(searchCursor!=null)
            {
                searchCursor.close();
                searchCursor = null;
            }
        }catch(Exception e){}        
    }
	
    /********************************************
	 * Menu list adapter
	 * 
	 * ******************************************
	 */
	private static final int[] MessageMenuResId = new int[] 
    {
	   R.string.facebook_messages_title,
       R.string.facebook_sent_title,
       R.string.facebook_updates_title,      
    };
	private static final int[] MessageMenuResIconId = new int[] 
    {
        R.drawable.inbox,
        R.drawable.send,
        R.drawable.inbox,
    };
    private static final int mDefaultTypeIdx = 0;    
   
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
