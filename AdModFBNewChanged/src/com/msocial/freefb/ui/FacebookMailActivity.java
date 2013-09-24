package com.msocial.freefb.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.service.dell.AsyncOmsService;
import com.msocial.freefb.service.dell.OmsServiceAdapter;
import com.msocial.freefb.ui.AccountListener.AccountManager;
import com.msocial.freefb.ui.adapter.MessageThreadInfoParcel;
import com.msocial.freefb.ui.adapter.RecipientsAdapter;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.MailboxMessage;
import oms.sns.service.facebook.model.MessageThreadInfo;
import oms.sns.service.facebook.model.PhoneBook;
import oms.sns.service.facebook.model.Stream;
import oms.sns.service.facebook.util.ArrayUtils;
import oms.sns.service.facebook.util.StringUtils;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import com.msocial.freefb.AddressPad;
import com.msocial.freefb.AddressPad.AddressDecorator;
import android.widget.Toast;

public class FacebookMailActivity extends FacebookBaseActivity
{
    private final String TAG      = "FacebookMailActivity";
    
    private MessageThreadInfoParcel  mthread;
	private boolean isForward;
	private boolean isReply;	
	private EditText   mail_editor;
	private AddressPad mail_rev;
	private EditText mail_sub;
	private Button   mail_revB;
	private Button facebook_mail_send_button;
	private View receiver_span;
	private View subject_span;
	private List<Long> currentSelected = new ArrayList<Long>();
	private long mailtowho;
	private boolean isNewmail;
	private boolean needSerialization = true;
	
	protected void showOptionMenu() 
    {    
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
 	   super.onCreate(savedInstanceState);
 	   setContentView(R.layout.facebook_mail);
 	   
 	   mail_editor = (EditText)this.findViewById(R.id.facebook_mail_editor);
 	   mail_editor.setHint("Compose words");
 	   
 	   mthread = (MessageThreadInfoParcel)this.getIntent().getParcelableExtra("mailboxthread");
 	   isNewmail = this.getIntent().getBooleanExtra("newmail", false);
 	   isForward = this.getIntent().getBooleanExtra("forward", false);
 	   mailtowho = this.getIntent().getLongExtra("mailtowho", -1);
 	   
 	   if(isForward)
 	   {
 		  String content = this.getIntent().getStringExtra("content");
 		  if(content != null)
 		  {
 		      mail_editor.setText("FW: " + content);
 		  }
 	   }
 	   isReply   = this.getIntent().getBooleanExtra("reply", false);
 	   
 	   receiver_span = this.findViewById(R.id.facebook_mail_receiver_span);
 	   subject_span  = this.findViewById(R.id.facebook_mail_subject_span);
 	   if(isForward || isNewmail)
 	   {
 		  initAddressBarView(); 		  
 	   }
 	   
 	   facebook_mail_send_button = (Button)this.findViewById(R.id.facebook_mail_send_button);
 	   facebook_mail_send_button.setOnClickListener(sendClick);
 	   
 	   SocialORM.Account account = orm.getFacebookAccount();
       if(checkFacebookAccount(this, account))
       {
       	   perm_session = loginHelper.getPermanentSesstion(this);
       	   if(perm_session!=null){
              perm_session.attachActivity(this);
       	      facebookA = new AsyncFacebook(perm_session);
       	      //
       	      //just new message restore pre-message
       	      //
       	      if(isNewmail)
       	      {
       	          new DeSerializationTask().execute((Void[])null);
       	      }
       	   }
       	   else
       	   {
       	      launchFacebookLogin(); 
       	   }
       }
       
       setMailTitle();
       //because the title have association with the intent
       setTitle();
    }
    
    
    View.OnClickListener sendClick = new View.OnClickListener() {
		public void onClick(View v) 
		{
		    hideInputKeyBoard(v);
			String content= mail_editor.getText().toString().trim();
	        String subject="";
	        if(mail_sub != null)
	        {
	           subject = mail_sub.getText().toString().trim();
	        }
	        
	        if(isEmpty(content) && isEmpty(subject))
	        {
	            Log.d(TAG, "you need input subject or content field");
	            return ;
	        }
	         
	        //TODO, should remove the repeat id
	        String[] adds = null;
	        if(mail_rev !=null)
	        {
	            adds = mail_rev.getAddresses();
	        }
	        if((adds == null || adds.length == 0) && (isForward || isNewmail))
	        {
	        	 Log.d(TAG, "no receivers");
		         return ;
	        }
	        
			handler.obtainMessage(FACEBOOK_MAIL_SEND).sendToTarget();
		}
	};
    @Override
    protected void onDestroy() 
    {
        if(needSerialization == true)
        {
            serialization();
        }
        super.onDestroy();
    }
    
    private class DeSerializationTask extends android.os.AsyncTask<Void, Void, Void>
    {       
        public DeSerializationTask()
        {
            super();            
            Log.d(TAG, "create DeSerializationTask="+this);
        }

		@Override
		protected Void doInBackground(Void... params)			  
        {
			deSerialization();
            return null;
        }
    }
    
    private static String lastmail_sfile = TwitterHelper.lastmail;	
	private long   taskID = 0;
	private void deSerialization()
	{
		
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try{
		    fis = new FileInputStream(lastmail_sfile);
		    in = new ObjectInputStream(fis);
		    taskID = in.readLong();
		    //receivers		    
		    int count = in.readInt();
		    List<String> receivers = new ArrayList<String>();		    
		    for(int i=0;i<count;i++)
		    {
		    	receivers.add((String)in.readObject());		    	
		    }		    
		    final String sub     = (String)in.readObject();
		    final String content = (String)in.readObject();
		    in.close();
		    //set UI
		    String ss="";
		    for(String item:receivers)
		    {
		    	ss +=item + ",";
		    }
		    
		    final String rec = ss;
		    handler.post( new Runnable()
		    {
		    	public void run()
		    	{
		    		if(isForward || isNewmail)
				    {
		    			if(mailtowho <= 0)
		    		    {
					    	if(isEmpty(rec) == false)
					    	{
					    		Log.d(TAG, "receirvers="+rec);
						        mail_rev.setAddresses(String.valueOf(rec), ",");
					    	}
		    		    }
					    mail_sub.setText(sub);
				    }
				    mail_editor.setText(content);
		    	}
		    });
		    
		}
		catch(IOException ex)
		{
			try{
			    new File(lastmail_sfile).delete();
			}catch(Exception ne){}
			Log.d(TAG, "deserialization fail="+ex.getMessage());
		}
		catch(ClassNotFoundException ex)
		{
			try{
			    new File(lastmail_sfile).delete();
			}catch(Exception ne){}
			Log.d(TAG, "deserialization fail="+ex.getMessage());
		}		
	}
	
	private void serialization()
	{
		FileOutputStream fos = null;
	    ObjectOutputStream out = null;
		try
		{
		    fos = new FileOutputStream(lastmail_sfile);
		    out = new ObjectOutputStream(fos);				    
		    out.writeLong(++taskID);
		    if(mail_rev != null)
		    {
		        String[] adds = mail_rev.getAddresses();
		        out.writeInt(adds.length);
		        
		        for(int i=0;i<adds.length;i++)
		        {
		        	out.writeObject(adds[i]);
		        }
		    }
		    else
		    {
		    	out.writeInt(0);
		    }
		    
		    if(mail_sub != null)
		    {
		        String subject=mail_sub.getText().toString().trim();
		        out.writeObject(subject);
		    }
		    else
		    {
		    	out.writeObject("");
		    }
	        
	        String content= mail_editor.getText().toString().trim();
	        out.writeObject(content);
		    
		    out.close();
		}
		catch(IOException ex)
		{
		    Log.d(TAG, "serialization fail="+ex.getMessage());
		}
	}
    
    public void initAddressBarView()
	{		
	   receiver_span.setVisibility(View.VISIBLE);
	   subject_span.setVisibility(View.VISIBLE);
	   mail_rev = (AddressPad)this.findViewById(R.id.facebook_mail_receiver_editor);
	   //mail_rev.setHint("Receivers");
	   mail_rev.setAdapter(new RecipientsAdapter(this));
	   mail_rev.setAddressDecorator(new FBUDecorater());
	   
	   mail_revB = (Button)this.findViewById(R.id.facebook_mail_receivers_button);
	   mail_revB.setVisibility(View.VISIBLE);
	   mail_revB.setOnClickListener(receiverGetClick);
	   
	   mail_sub = (EditText)this.findViewById(R.id.facebook_mail_subject);
	   mail_sub.setHint("Subject");
	   
	   String subject = this.getIntent().getStringExtra("subject");
	   if(subject != null)
	   {
		   mail_sub.setText("FW: "+ subject);
	   }
	   
	   if(mailtowho > 0)
	   {
		   mail_rev.setAddresses(String.valueOf(mailtowho), ",");
	   }
	}


	public void setTitle()
	{
		if(isReply)
	    {
		    title = getString(R.string.facebook_mail_detail_reply);
	    }
	    else if(isForward || isNewmail)
  	    {
	    	title = getString(R.string.facebook_mail_send);
  	    }
	}	

	public void setMailTitle() 
	{
		if(isBackgroud() == false)
		{
		    if(isReply)
		    {
			    setTitle(R.string.facebook_mail_detail_reply);
		    }
		    else if(isForward || isNewmail)
	  	    {
	  		    setTitle(R.string.facebook_mail_send);
	  	    }
		}
		else
		{
			Log.d(TAG, "I am in background");
		}
	}
	@Override
	protected void createHandler() 
	{
		handler = new MailHandler();		
	}
	
	
	/*
	@Override
	public void titleSelected() 
	{		 
		super.titleSelected();
		handler.obtainMessage(FACEBOOK_MAIL_SEND).sendToTarget();
	}
	*/

	View.OnClickListener receiverGetClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "receiverGetClick clicked");
			 //call a select sub activity
			 
			 Intent intent = new Intent(FacebookMailActivity.this, FacebookUserSelectActivity.class);            
     		 startActivityForResult(intent, FACEBOOK_USER_SELECT);
		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		  switch(requestCode)
	      {  
	            case FACEBOOK_USER_SELECT:
	            {
	            	if(resultCode == 100)
	            	{
	            		Log.d(TAG, "user select");	            		
	            		long[] uids = intent.getLongArrayExtra("uids");
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
	            					if(currentSelected.get(j) == uids[i])
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
        				    if(mail_rev!=null){
        		                originalnum = mail_rev.getAddresses(",");
        		            } 
        		            
        		            if("".equals(originalnum)){
        		            	mail_rev.setAddresses(numbers, ",");
        		            }else{
        		            	mail_rev.setAddresses(originalnum+","+numbers, ",");
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
	
	final int FACEBOOK_MAIL_SEND          = 0;
	final int FACEBOOK_MAIL_SEND_END      = 1;	
	final int SEND_MESSAGE_IN_BACKGROUND   = 3;
	
	private class MailHandler extends Handler 
	{
        public MailHandler()
        {
            super();            
            Log.d(TAG, "new MailHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case FACEBOOK_MAIL_SEND:
                {
                	showDialog(DLG_SEND_MAIL);
                	if(isReply)
                	    doMailBoxReply();
                	else
                		doMailBoxSend();
                	
                	break;
                }
                case FACEBOOK_MAIL_SEND_END:
                {
                	end();    	
                	facebook_mail_send_button.setEnabled(true);
                	dismissDialog(DLG_SEND_MAIL);
    				if(msg.getData().getBoolean(RESULT) == true)
    				{
    				    if(isReply)
    				    {
    				        String content = msg.getData().getString("content");
    				        long tid = msg.getData().getLong("mtid");
    				        if(mthread != null && mthread.mthread != null)
    				        {
                                cacheReplyMessage(content,tid,mthread.mthread); 
    				        }
    				    }
    				    else
    				    {
                            String content = msg.getData().getString("content");
                            String subject = msg.getData().getString("subject");
                            long[] uids = msg.getData().getLongArray("uids");
                            long tid = msg.getData().getLong("tid");
    				        cacheNewMessage(content,subject,uids,tid);
    				    }
    				    needSerialization = false;
    					//remove cache file
    					try{
	    					if((new File(lastmail_sfile)).exists() == true)
	    					{
	    						new File(lastmail_sfile).delete();
	    					}
    					}catch(Exception ne){}
    					FacebookMailActivity.this.finish();
    				}
    				else
    				{
    					serialization();
    					Toast.makeText(FacebookMailActivity.this, R.string.sns_send_message_failed, Toast.LENGTH_SHORT).show();
    				}
                	break;
                }               
                case SEND_MESSAGE_IN_BACKGROUND:
                {
                    String content = msg.getData().getString("content");
                    String subject = msg.getData().getString("subject");
                    ArrayList<String> email_receiver = msg.getData().getStringArrayList("email_receiver");                    
                    sendMessageInBackground(content, subject, email_receiver);
                    break;
                }
            }
        }
	}
	public void doMailBoxSend() 
	{
	    if(this.isInProcess() == true)
	    {
	        Log.d(TAG,"mail send is still in run");
	        dismissDialog(DLG_SEND_MAIL);
	        showDialog(DLG_SEND_MAIL);      
	        return;
	    }
	    final String content= mail_editor.getText().toString().trim();
        final String subject=mail_sub.getText().toString().trim();
        
        if(isEmpty(content) && isEmpty(subject))
        {
            Log.d(TAG, "you need input subject or content field");
            return ;
        }
         
        //TODO, should remove the repeat id
        String[] adds = mail_rev.getAddresses();
        ArrayList<Long> sids = new ArrayList<Long>();
        ArrayList<String> email_receiver = new ArrayList<String>();
        for(int i=0;i<adds.length;i++)
        {       
            if(isEmail(adds[i].trim()))
            {
                email_receiver.add(adds[i].trim());
            }
            else
            {
                try{
                    sids.add(Long.valueOf(adds[i].trim()));
                }catch(NumberFormatException ne){
                    Log.d(TAG, "wrong receiver ");
                }
            }
        }
        
		if(facebookA != null)
		{	
			int userLen    =sids.size();
			final long userids[] = new long[userLen];
			for(int i=0;i<userLen;i++)
			{
				userids[i] = sids.get(i);
			}
			
			if((isEmpty(content)==false || isEmpty(subject)==false)&& userLen>0)
			{
				begin();
				facebook_mail_send_button.setEnabled(false);
				synchronized(mLock)
		    	{
		    	    inprocess = true;
		    	}				
			    facebookA.mailSendAsync(userids, subject, content, new FacebookAdapter()
		    	{
		    		@Override public void mailSend(long tid)
		            {
		    			Log.d(TAG, "after send="+tid);
						synchronized(mLock)
				    	{
				    	    inprocess = false;
				    	}
						
		                if(donotcallnetwork == false)//I am still alive
		                {							
			            	//cancelNotify();
		                }       
		                Message rmsg = handler.obtainMessage(FACEBOOK_MAIL_SEND_END);
		                rmsg.getData().putBoolean(RESULT, true);
		                rmsg.getData().putLong("tid",tid);
		                rmsg.getData().putLongArray("uids", userids);
		                rmsg.getData().putString("subject", subject);
		                rmsg.getData().putString("content", content);      
		                rmsg.sendToTarget();
		            }
		    		
		            @Override public void onException(FacebookException e, int method) 
		            {
		            	synchronized(mLock)
				    	{
				    	    inprocess = false;
				    	}
		            	
		            	Log.d(TAG, "after send ex="+e.getMessage());
		            	if(isInAynscTaskAndStoped())
		            	{
		            		Log.d(TAG, "User stop passive");
		            	}
		            	else
		            	{
			            	Message rmsg = handler.obtainMessage(FACEBOOK_MAIL_SEND_END);
			                rmsg.getData().putBoolean(RESULT, false);
			                rmsg.sendToTarget();
		            	}
		            }
		    	});
			  			    
			    //copy to email
			    if(orm.copytoEmail())
			    {
    			    facebookA.sendEmailAsync(content, subject, userids, new FacebookAdapter()
    			    {
    			        @Override public void  sendEmail(boolean suc)
    			        {
    			            Log.d(TAG, "suc to send email");
    			        }
    			        @Override public void onException(FacebookException e, int method) 
                        {
    			            Log.d(TAG, "fail to send email="+e.getMessage());
                        }
    			    });
			    }
			}
		}
		//
		if(email_receiver!=null && email_receiver.size()>0)
        {
             Log.d(TAG,"send message by email"+ email_receiver.size());
             
             if(sids.size()==0)
             {  
                 //Toast.makeText(mContext,R.string.facebook_mail_sending, Toast.LENGTH_SHORT);
                 Message rmsg = handler.obtainMessage(FACEBOOK_MAIL_SEND_END);
                 rmsg.getData().putBoolean(RESULT, true);
                 handler.sendMessageDelayed(rmsg, 2*1000);
                 //rmsg.sendToTarget();
             }
             Message msd = handler.obtainMessage(SEND_MESSAGE_IN_BACKGROUND);
             if(isEmpty(subject) == false)
                 msd.getData().putString("subject", subject);
             
             if(isEmpty(content) == false)
                 msd.getData().putString("content", content);
             
             msd.getData().putStringArrayList("email_receiver", email_receiver);
             msd.sendToTarget();
        }
	}
	
	public void cacheNewMessage(String content, String subject, long[] uids,long tid)
	{
	    Log.d(TAG,"entering cacheNewMessage Method content is="+content+" subject is="+subject+" thread id is="+tid);
	    if(tid>0)
	    {
	        MailboxMessage message = new MailboxMessage();
	        message.author = perm_session.getLogerInUserID();
            message.body = content;
            message.hasattachment = 0;
            message.threadid = tid;
            message.mid = "-1"; // this first message
            message.timesent = new Date(); 
            orm.addMailMessages(message); // save MailMessage
	        
	        MessageThreadInfo mtInfo = new MessageThreadInfo();
            mtInfo.isoutbox = true;
            ArrayList<Long> recips = new ArrayList<Long>();
            for(int i=0;i<uids.length;i++)
            {   
                if(uids[i] == perm_session.getLogerInUserID())
                {
                    mtInfo.isinbox = true;
                }
                recips.add(uids[i]);
            } 
            mtInfo.recipients = recips;
            mtInfo.snippet = content;
            mtInfo.snippet_author = perm_session.getLogerInUserID();
            mtInfo.subject = subject;
            mtInfo.thread_id = tid;
            mtInfo.unread = 0;
            mtInfo.inbox_updated_time = System.currentTimeMillis();
            mtInfo.outbox_updated_time = System.currentTimeMillis();
            mtInfo.updated_time = 0;
            orm.addMailThread(mtInfo); //save and update MailThread /Sent info
	    }
    }
	
    private void sendMessageInBackground(String con,String sub, List<String> email_receiver)
	{
	    final String content = isEmpty(con)?"":con;
	    final String subject = isEmpty(sub)?"":sub;
	    
	    Log.d(TAG, "entering sendMessage In background "+email_receiver.size());
	    if(email_receiver==null || email_receiver.size()==0)
        {
	        return;
        }
	    
	    AsyncOmsService asyncOms = null; 
	    perm_session = loginHelper.constructPermSession();
        if(perm_session != null)
        {
            asyncOms = new AsyncOmsService(perm_session);
        }
        else
        {
            Log.d(TAG, "no session");
            return ;
        }
	  
        JSONArray jsonArray   = new JSONArray();
        for(int i=0; i<email_receiver.size();i++)
        {
            try{
                JSONObject email = new JSONObject();    
                email.put("email", email_receiver.get(i));
                jsonArray.put(email);
            }
            catch(JSONException e)
            {
                Log.d(TAG, "create  jsonobject exception "+e.getMessage());
                return ;
            }       
        }
        
        String entries = jsonArray.length()>0?jsonArray.toString():"";
        if(entries.length() > 0)
        {   
            Log.d(TAG, "lookupFacebookUserFromContacts entries is ==="+entries);
            asyncOms.phoneLookupAsync( entries, new OmsServiceAdapter()
            {               
                public void phoneLookup(List<PhoneBook> phones)
                {
                    Log.d(TAG," look up phonebook "+phones.size());
                    //progressMailRecive
                    if(phones!=null && phones.size()>0 )
                    {  
                        long[] uids = new long[phones.size()];
                        for(int j=0;j<phones.size();j++)
                        {
                            uids[j] = phones.get(j).uid;
                        }
                       
                        new BackgroundThread(content,subject,uids).start();                       
                    }
                }
                
                public void onException(FacebookException te, int method, Object[] args)
                {
                    Log.d(TAG," lookup failed do nothing="+te.getMessage());                   
                }
            });
          }

	}
	
	
	public void doMailBoxReply() 
	{
	    if(this.isInProcess() == true)
	    {
	        dismissDialog(DLG_SEND_MAIL);
	        showDialog(DLG_SEND_MAIL);
	        return;
	    }
		if(facebookA != null)
		{
			final String content= mail_editor.getText().toString().trim();
			if(content != null && content.length() > 0)
			{
				begin();
				facebook_mail_send_button.setEnabled(false);
				synchronized(mLock)
		    	{
		    	    inprocess = true;
		    	}
		    	
			    facebookA.mailReplyAsync(mthread.mthread.thread_id, content, new FacebookAdapter()
		    	{
		    		@Override public void mailReply(long tid)
		            {
		    			Log.d(TAG, "after reply="+tid);
						synchronized(mLock)
				    	{
				    	    inprocess = false;
				    	}
						
		                if(donotcallnetwork == false)//I am still alive
		                {							
			            	//cancelNotify();
		                } 
		                // pass content/title/target_id/actor_id/messageid/
		                Message rmsg = handler.obtainMessage(FACEBOOK_MAIL_SEND_END);
		                rmsg.getData().putBoolean(RESULT, true);
		                rmsg.getData().putLong("mtid", tid);
		                rmsg.getData().putString("content",content);
		                rmsg.sendToTarget();
		            }
		    		
		            @Override public void onException(FacebookException e, int method) 
		            {
		            	synchronized(mLock)
				    	{
				    	    inprocess = false;
				    	}
		            	
		            	Log.d(TAG, "after reply ex="+e.getMessage());
		            	if(isInAynscTaskAndStoped())
		            	{
		            		Log.d(TAG, "User stop passive");
		            	}
		            	else
		            	{
			            	Message rmsg = handler.obtainMessage(FACEBOOK_MAIL_SEND_END);
			                rmsg.getData().putBoolean(RESULT, false);
			                rmsg.sendToTarget();
		            	}
		            }
		    	});
			    if(orm.copytoEmail())
			    {
			        //copy to email
	                long[] uids = new long[mthread.mthread.recipients.size()];
	                for(int i=0;i<mthread.mthread.recipients.size();i++)
	                {
	                    uids[i] = mthread.mthread.recipients.get(i);
	                }
    			    facebookA.sendEmailAsync(content, mthread.mthread.subject, uids, new FacebookAdapter()
                    {
                        @Override public void  sendEmail(boolean suc)
                        {
                            Log.d(TAG, "suc to send email");
                        }
                        @Override public void onException(FacebookException e, int method) 
                        {
                            Log.d(TAG, "fail to send email="+e.getMessage());
                        }
                    });
			    }
			}
		}
	}
	
	public void registerAccountListener() {
		AccountManager.registerAccountListener("FacebookMailActivity", this);		
	}
	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("FacebookMailActivity");		
	}
	
    private boolean isEmail(String str)
    {
        Pattern emailPattern = Pattern.compile("^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+[-|_]?([a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
        Matcher matcher = emailPattern.matcher(str);
        if(matcher.find())
        {
            return true;
        }
        else
        {
            return false;
        } 
    }
    
    public class FBUDecorater implements AddressDecorator {        
        public CharSequence decorateAddress(AddressPad addressPad,
                CharSequence address, boolean hasFocus) {            
            String suid = address.toString().trim();
            try
            {
                    long uid = Long.valueOf(suid);
                    FacebookUser user = orm.getFacebookUser(uid);
                    if(user != null && user.name != null && user.name.length()>0)
                    {
                        return user.name;
                    }
            }
            catch(NumberFormatException ne)
            {               
                Log.d(TAG, "why come here="+ne.getMessage() + " address="+address);
            }
            return address;       
        }
    }
    
    class BackgroundThread extends Thread{
        String mailcontent;
        String mailsubject;
        long[] uids;
        BackgroundThread(String mailcontent,String mailsubject,long[] uids){
            this.mailcontent = mailcontent;
            this.mailsubject = mailsubject;
            this.uids = uids;
        }
        
        public void run()
        {
            Log.d(TAG,"progress Mail sending in BackgrounThread");           
            perm_session = loginHelper.constructPermSession();
            if(perm_session != null)
            {
                facebookA = new AsyncFacebook(perm_session);
            }
            
            facebookA.mailSendAsync(uids, mailsubject, mailcontent, new FacebookAdapter()
            {
                @Override public void mailSend(long tid)
                {
                    Log.d(TAG, "send mail background successed");
                    
                }
                
                @Override public void onException(FacebookException e, int method) 
                {
                   Log.d(TAG,"send mail background exception "+e.getMessage());
                }
            });
        }
    }
}
