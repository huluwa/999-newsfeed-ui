package com.tormas.litetwitter.ui;

import java.util.ArrayList;
import java.util.List;

import com.tormas.litetwitter.R;
import com.tormas.litetwitter.providers.SocialORM.Follow;
import com.tormas.litetwitter.ui.adapter.RecipientsAdapter;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.tormas.litetwitter.AddressPad;
import com.tormas.litetwitter.AddressPad.AddressDecorator;
import com.tormas.litetwitter.AddressPad.OnAddressKeyListener;


public class TwitterComposeActivity extends StatusViewBaseActivity {
	
    private static final String TAG = "TwitterComposeActivity";
	//View      receiverSpan;
	//View      Span;
	TextView  textCount;
	EditText  sendcontent;
	private boolean isForReply=true;
	private MyWatcher watcher;
	String to_uid;
	long   status_id;
	
	 boolean isReply   ;
	 boolean isForward ;
     boolean isRetweet ;
     boolean isDirect  ;
     boolean isUpdate  ;
     
     boolean islastdata;
     
     
 	private AddressPad message_rev; 	
 	private Button     message_revB;
 	private View       receiver_span;
 	
 	private List<String> currentSelected = new ArrayList<String>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_compose_msg);
        
        textCount = (TextView)this.findViewById(R.id.twitter_message_text_counter);
        sendcontent = (EditText) this.findViewById(R.id.twitter_message_editor);
        sendcontent.setHint("Compose message content");
        InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(defaultTextLength)};
        sendcontent.setFilters(filters);   
        sendcontent.setVerticalScrollBarEnabled(true);
        watcher = new MyWatcher(); 	    
        sendcontent.addTextChangedListener(watcher);
       
        //addresspad for send message to multiuser
        receiver_span = this.findViewById(R.id.twitter_message_receiver_span);
        message_rev = (AddressPad)this.findViewById(R.id.twitter_message_receiver_editor);
        message_revB = (Button)this.findViewById(R.id.twitter_message_receivers_button);
  	    
        //direct
        to_uid = this.getIntent().getStringExtra(TWITTER_ID);        
        status_id = this.getIntent().getLongExtra(STATUS_ID, -1);
        isReply  = this.getIntent().getBooleanExtra(REPLY,    false);
        isForward = this.getIntent().getBooleanExtra(FORWARD, false);
        isRetweet = this.getIntent().getBooleanExtra(RETWEET, false);
        isDirect  = this.getIntent().getBooleanExtra(DIRECT,  false);
        isUpdate  = this.getIntent().getBooleanExtra(UPDATE,  false);
        Log.d(TAG, "twitter_id="+to_uid +" status_id="+status_id + " reply="+isReply + " isRetweet="+isRetweet + " isDirect="+isDirect + " isUpdate"+isUpdate);
        
        if(isForward)//set content
        {
            String tweetContent = this.getIntent().getStringExtra(CONTENT);
            sendcontent.setText("FW @"+to_uid+" "+tweetContent);
        }       
        
        if(isDirect || isForward || isReply)
        {
        	receiver_span.setVisibility(View.VISIBLE);
        	message_revB.setVisibility(View.VISIBLE);
        	initAddressBarView();
        }
       
        //to remember the title
        setTitle();
        
        setTitle(finalTitle);
        checkTwitterAccount(this, orm.getTwitterAccount());
        
        twitter_action.setVisibility(View.VISIBLE);
        twitter_refresh.setVisibility(View.GONE);
	}
	
	public void setTitle() 
	{		
	    finalTitle=getString(R.string.twitter_message_title_send);
        if(isReply)
        	finalTitle=getString(R.string.twitter_message_title_reply);
        
        if(isForward)
            finalTitle=getString(R.string.twitter_message_title_forward);      
        
        if(isRetweet)
        	finalTitle = getString(R.string.twitter_message_title_retweet);
        
        if(isDirect)
        	finalTitle = getString(R.string.twitter_message_title_direct_message);
        
        if(isUpdate)
        	finalTitle = getString(R.string.twitter_message_title_update);
	}
	
	public void initAddressBarView()
	{		
	    receiver_span.setVisibility(View.VISIBLE);
	    message_rev.setAdapter(new RecipientsAdapter(this, true));
	    message_rev.setAddressDecorator(new FBUDecorater());
	    
	    if(to_uid!=null && isRetweet == false)
	    {
	       message_rev.setAddresses(String.valueOf(to_uid), ",");		   	   
	    }
	    message_revB.setVisibility(View.VISIBLE);
	    message_revB.setOnClickListener(receiverGetClick);
	    message_revB.setBackgroundResource(R.drawable.select);   
	 }

    
	    
	    View.OnClickListener receiverGetClick = new View.OnClickListener()
	   {
			public void onClick(View v) 
			{
				 Log.d(TAG, "receiverGetClick clicked");
				 //call a select sub activity			 
				 Intent intent = new Intent(TwitterComposeActivity.this, TwitterUserSelectActivity.class);            
	     		 startActivityForResult(intent, TWITTER_USER_SELECT);
			}
		};
	
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent intent)
		{
			  switch(requestCode)
		      {  
		            case TWITTER_USER_SELECT:
		            {
		            	if(resultCode == 100)
		            	{
		            		Log.d(TAG, "user select");	            		
		            		String[] uids = intent.getStringArrayExtra("snames");
		            		if(uids == null || uids.length ==0)
		            		{
		            			Log.d(TAG, "no selected user");
		            		}
		            		else
		            		{
		            			currentSelected.clear();
		            			boolean isExist;
		            			String numbers="";
		            			for(int i=0;i<uids.length;i++)
		            			{
		            				//check whether exist
		            				isExist = false;
		            				for(int j=0;i<currentSelected.size();j++)
		            				{
		            					if(currentSelected.get(j).equalsIgnoreCase(uids[i]))
		            					{
		            						isExist=true;
		            						break;
		            					}
		            				}
		            				if(isExist == false)
		            				{
			            				currentSelected.add(uids[i]);
			            				numbers +=String.valueOf(uids[i]) + ",";
		            				}
		            			}
		            			
		            			String originalnum="";
	        				    if(message_rev!=null){
	        		                originalnum = message_rev.getAddresses(",");
	        		            } 
	        		            
	        		            if("".equals(originalnum)){
	        		            	message_rev.setAddresses(numbers, ",");
	        		            }else{
	        		            	message_rev.setAddresses(originalnum+","+numbers, ",");
	        		            }
	        		            
		            		}
		            		//no need more
		            		uids = null;
		            	}
		            	break;
		           }
		           default:
		        	   super.onActivityResult(requestCode, resultCode, intent);
		               break;
		      }
		}
		
	//if no setting account, will prompt the setting ui
	@Override
    protected void loadAfterSetting()
    {
        super.loadAfterSetting();
        twitterA = null;
        
        checkTwitterAccount(this, orm.getTwitterAccount());
    }
	
	public void titleSelected()
	{
		
		//begin to send the message
		String content = this.sendcontent.getText().toString().trim();
		if(content != null && content.length() > 0)
		{   		
		    if(isUpdate)
		    {
		    	Message msg = null;
		        msg = handler.obtainMessage(TWITTER_UPDATE_STATUS);//update current auth user status
		        msg.getData().putString("content", content);
	    		handler.sendMessageDelayed(msg, 1000);
		    }
		    else if(isRetweet)
		    {
		        Message msg = null;
		        msg = handler.obtainMessage(TWITTER_RETWEET);
		        msg.getData().putString("content",content);
		        msg.getData().putLong("statusid", status_id);
		        handler.sendMessage(msg);
		    }
		    else
		    {
		    	//send to others, might be reply, send direct message
		    	processReplyDerectMessage(content);
		    }		    
		}
	}
	
	//send to others, might be reply, send direct message
	private void processReplyDerectMessage(String content)
	{
		 String[] touids =  message_rev.getAddresses();
         if(touids == null || touids.length == 0)
        	 return;
         
         if(insending == true)
         {
        	 Log.d(TAG, "is sending message="+this);
        	 return;
         }
         
         //show dialog
         mContext.showDialog(DLG_SEND_MSG);
         
         failSends.clear();
         MessageThread messageT = new MessageThread();
         messageT.content = content;
         messageT.receivers = touids;
         messageT.source = this.to_uid;
         messageT.status_id = this.status_id;
         messageT.start();
         
         synchronized (mLock)
         {
             try 
             {	
 				mLock.wait(30*1000);
 				if(sendHandler != null)
 				{
 					Message mds = sendHandler.obtainMessage(NEXT_RECEIVER);				
 					mds.getData().putInt("position", 0);				
 					sendHandler.sendMessage(mds);
 				}
 				else
 				{
 					Log.d(TAG, "why I am null="+this);
 				}
 			} catch (InterruptedException e) {}
         }    
	}
	
	public boolean insending = false;
	Looper      sendLoop;
	SendHandler sendHandler;
	List<String> failSends =  new ArrayList<String>();
	public class MessageThread extends Thread{
		public String    content;
		public String[]  receivers;
		public String    source;//come who
		public long      status_id;
		
		public MessageThread()
		{
			super();
			this.setName("Twitter Message"+this.getId());
		}
		
		public void run()
		{
			synchronized(mLock)
	    	{
	    	    insending = true;	    	
	    	}
			
			handler.post( new Runnable()
			{
				public void run()
				{			
			        message_revB.setEnabled(false);
			        message_rev.setEnabled(false);			
			    }
			});
			
			Looper.prepare();
			sendLoop = Looper.myLooper();
			
			sendHandler = new SendHandler();
			sendHandler.content   = this.content;
			sendHandler.receivers = this.receivers;
			sendHandler.source    = this.source;//come who
			sendHandler.status_id = this.status_id;
			
			//let wait reach first
			try{
			   Thread.currentThread().sleep(2000);
		    }catch(InterruptedException ne){}
			
		    synchronized (mLock)
	        {
	            mLock.notifyAll();
	        }
			Looper.loop();	
			Log.d(TAG , "exit send twitter message thread");
			
			
			handler.post( new Runnable()
			{
				public void run()
				{					
			    	message_revB.setEnabled(true);
			        message_rev.setEnabled(true);			    	
			    }
			});
			
			synchronized(mLock)
	    	{
				insending = false;
				
	    	}
			//after finish send
			boolean sendsuc=false;
			if(failSends.size() == 0)
			{
				sendsuc = true;
			}	
			
			Message msd = handler.obtainMessage(TWITTER_SEND_MESSAGE_END);
			msd.getData().putBoolean("isupdatemyself", true);
			msd.getData().putBoolean(RESULT, sendsuc);
		    handler.sendMessage(msd);
			
		}
	}
	
	final int NEXT_RECEIVER =0;
	public class SendHandler extends Handler
	{
		public String    content;
		public String[]  receivers;
		public String    source;//come who
		public long      status_id;
		
	    @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case NEXT_RECEIVER:
	            {
	            	int pos = msg.getData().getInt("position", -1);
	            	synchronized(receivers)
	            	{
		            	if(pos != -1 && pos < receivers.length)
		            	{
			            	String who =  receivers[pos];
			            	Log.d(TAG, "before send to="+who);
			            	progressSet(pos, receivers.length);
							sendTwitterMessage(who);							
							
							//process next one
							Message mds = this.obtainMessage(NEXT_RECEIVER);
							pos++;		
							mds.getData().putInt("position", pos);							
							sendMessage(mds);
		            	}
		            	else
		            	{
		            		mContext.dismissDialog(DLG_SEND_MSG);		            		
		            		Log.d(TAG, "quit the save");
		            		sendLoop.quit();
		            	}
	            	}										
	            	break;
	            }
            }
        }
	    
        //sync to send the message
		private void sendTwitterMessage(String who) 
		{
			//do reply
			if(who.equalsIgnoreCase(to_uid) && isReply )
			{
				String tmp = "@"+who+" "+content;
				try{
				    Status st = twitterA.updateStatus(tmp, status_id);
				    Log.d(TAG, "after reply to="+who+" status="+st);
				    sucSend(who);				    
				}
				catch(TwitterException ne)
				{
				    Log.d(TAG, "Fail to reply to="+who);
				    failSend(who);
				}
			}
			else//send direct message
			{
				try{
					DirectMessage dm = twitterA.sendDirectMessage(who, content);
				    Log.d(TAG, "after send message to="+who+" status="+dm);
				    sucSend(who);
				}
				catch(TwitterException ne)
				{
				    Log.d(TAG, "Fail to send message to="+who);
				    failSend(who);
				}
			}
		}
	};
	
	//update title and progress
	void progressSet(int pos, int size)
	{
		Message msg = handler.obtainMessage(TWITTER_UPDATE_TIRLE_PROGRESS);
		msg.getData().putInt("pos", pos+1);
		msg.getData().putInt("size", size);
		
		handler.sendMessage(msg);		
	}
	//remove who from address bar
	private void sucSend(String who)
	{
		String[] adds = message_rev.getAddresses();
		//reconstruct the address
		String news="";	
		for(int i=0;i<adds.length;i++)
		{
			if(who.equalsIgnoreCase(adds[i].trim()) == false)
			{
				news += adds[i] + ",";
			}
		}
		if(news != null)
		{
			Message msd = handler.obtainMessage(TWITTER_MESSAGE_SET_ADD_BAR);
			msd.getData().putString("newaddress", news);
			handler.sendMessage(msd);
		    
		}
		
	}
	private void failSend(String who)
	{
		failSends.add(who);
	}
	
	private class HandlerLoad extends Handler 
    {
        public HandlerLoad()
        {
            super();
            
            Log.d(TAG, "new HandlerLoad");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {  
	            case TWITTER_UPDATE_TIRLE_PROGRESS:
	            {
	            	if(isBackgroud() == false)
	                {
	            		setTitle(START_REQUEST);
		            	int pos = msg.getData().getInt("pos",   -1);
		            	int size = msg.getData().getInt("size", -1);
		            	setProgress(100*(10 + (int)((float)pos/(float)size) * 90 ));
	                }
	            	break;
	            }
	            case TWITTER_RETWEET:
	            {
	                //mContext.showDialog()
	                String con = msg.getData().getString("content");
	                long statusId = msg.getData().getLong("statusid");
	                twitterA.retweetStatusAsync(statusId, new TwitterAdapter()
	                {
	                    @Override public void  retweetedStatus(Status status)
                        {
                           Log.d(TAG, "after retweet status="+status);
                           Message smd = handler.obtainMessage(TWITTER_SEND_MESSAGE_END);
                           smd.getData().putBoolean(RESULT, true);
                           handler.sendMessage(smd);
                        }
                       
                        @Override public void onException(TwitterException e, int method) 
                        {                   
                           Log.d(TAG, "Fail to retweet status ="+e.getMessage());               
                           if(isInAynscTaskAndStoped())
                           {
                               Log.d(TAG, "User stop passive");
                           }
                           else
                           {
                               Message smd = handler.obtainMessage(TWITTER_SEND_MESSAGE_END);
                               smd.getData().putBoolean(RESULT, false);
                               handler.sendMessage(smd);
                           }
                        }           
	                });
	                break;
	            }
                case TWITTER_UPDATE_STATUS:
                {
                    
                    mContext.showDialog(DLG_TWEET_COMPOSE);
                   
                    String con = msg.getData().getString("content");                   
                    twitterA.updateStatusAsync(con, new TwitterAdapter() 
                    {
                    	 @Override public void  updatedStatus(Status status)
                         {
                            Log.d(TAG, "after update status="+status);
                            Message smd = handler.obtainMessage(TWITTER_SEND_MESSAGE_END);
                            smd.getData().putBoolean(RESULT, true);
                            handler.sendMessage(smd);
                         }
                        
                         @Override public void onException(TwitterException e, int method) 
                         {                   
                            Log.d(TAG, "Fail to updated ="+e.getMessage());               
                            if(isInAynscTaskAndStoped())
                        	{
                        		Log.d(TAG, "User stop passive");
                        	}
                        	else
                        	{
	                            Message smd = handler.obtainMessage(TWITTER_SEND_MESSAGE_END);
	                            smd.getData().putBoolean(RESULT, false);
	                            handler.sendMessage(smd);
                        	}
                         }           
                    });              
                    
                    break;
                }                
                case TWITTER_SEND_MESSAGE_END:
                {
                	end();
                	boolean isForUpdate = msg.getData().getBoolean("isupdatemyself");
                	if(isUpdate)
                	{
                      mContext.dismissDialog(DLG_TWEET_COMPOSE);
                	}
                
                	boolean res = msg.getData().getBoolean(RESULT);
                	if(res)//success send data
                	{
                        mContext.finish();
                	}
                	else
                	{
                		if(isForUpdate)
                		{
                		    Toast.makeText(mContext, R.string.twitter_message_fail_send_all, Toast.LENGTH_SHORT).show();
                		}
                		else
                		{
                			Toast.makeText(mContext, R.string.twitter_message_fail_update_status, Toast.LENGTH_SHORT).show();
                		}
                	}
                    break;
                }    
                case TWITTER_MESSAGE_SET_ADD_BAR:
                {
                    String news = msg.getData().getString("newaddress");
                    message_rev.setAddresses(news, ",");
                	break;
                }
            }
        }
    }
	
	public class FBUDecorater implements AddressDecorator {
		public CharSequence decorateAddress(AddressPad addressPad,
				CharSequence address, boolean hasFocus) {
			String suid = address.toString().trim();
			 List<Follow> follows = orm.getTwitterUser(suid);                    
	            if(follows.size()>0){
	               return follows.get(0).Name;
	            }
	        return address;			
		}
    }
	
	private class MyWatcher implements TextWatcher 
	{   
       public void afterTextChanged(Editable s) 
       {
    	   textCount.setText(String.format("%1$s",defaultTextLength-s.length()));
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }

    @Override
    public void createHandler() 
    {
        handler = new  HandlerLoad();        
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {            
            this.stopLoading();
            System.gc();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }	
}
