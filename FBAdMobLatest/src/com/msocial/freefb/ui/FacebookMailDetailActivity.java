package com.msocial.freefb.ui;

	
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.msocial.freefb.*;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.service.dell.User;
import com.msocial.freefb.ui.AccountListener.AccountManager;
import com.msocial.freefb.ui.adapter.MailAdapter;
import com.msocial.freefb.ui.adapter.MessageThreadInfoParcel;
import com.msocial.freefb.ui.view.FacebookMailItemView;
import com.msocial.freefb.ui.view.MessageThreadInfoItemView;
import com.msocial.freefb.util.DateUtil;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.MailboxMessage;
import oms.sns.service.facebook.model.MessageThreadInfo;
import oms.sns.service.facebook.model.Page;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class FacebookMailDetailActivity extends FacebookBaseActivity
{
   private final String TAG      = "FacebookMessageActivity"; 
   private ListView      messages;
   private View       search_span;
   private View       subject_name_span;
   private TextView   subject_name;
   private TextView   subject_create;
   private TextView   relative_users;
   private View       compose_view;
   private EditText   content_view;
   private Button     reply_btn;
   private Button     cancel_btn;
   private boolean onresumeTag = false;
   
   private List<MailboxMessage>  inMsg  = new ArrayList<MailboxMessage>();

   private long tid=-1;
   private boolean frominbox;
   private MessageThreadInfoParcel mailboxthread;
   private int lastVisiblePos = -1;
   private boolean isupdate = false;
   @Override
   public void onCreate(Bundle savedInstanceState){
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.facebook_message);
	   
	   messages = (ListView)this.findViewById(R.id.facebook_message_list);
	   messages.setFocusable(true);
	   messages.setFocusableInTouchMode(true);
	   messages.setOnCreateContextMenuListener(this);
	   messages.setOnItemClickListener(listItemClickListener);
		
	   search_span = this.findViewById(R.id.facebook_search_span);
	   search_span.setVisibility(View.GONE);
	   
	   subject_name_span = this.findViewById(R.id.subject_name_span);
	   subject_name_span.setVisibility(View.VISIBLE);
	   
	   subject_name   = (TextView)this.findViewById(R.id.subject_name);
	   relative_users = (TextView)this.findViewById(R.id.relative_users);
	   relative_users.setMovementMethod(LinkMovementMethod.getInstance());
	   relative_users.setLinksClickable(true);	   
	   
	   subject_create = (TextView)this.findViewById(R.id.subject_create);
	   
	   tid = this.getIntent().getLongExtra("tid", -1);
	   mailboxthread = (MessageThreadInfoParcel)this.getIntent().getParcelableExtra("mailboxthread");
	   frominbox = this.getIntent().getBooleanExtra("frominbox",false);
	   
	   compose_view = (View)this.findViewById(R.id.facebook_compose_span);
	   compose_view.setVisibility(View.VISIBLE);
	   content_view = (EditText)this.findViewById(R.id.facebook_mail_editor);
	   content_view.requestFocus();
	   reply_btn = (Button)this.findViewById(R.id.facebook_mail_send_button);
	   cancel_btn = (Button)this.findViewById(R.id.facebook_mail_reply_cancel);
	   
	   reply_btn.setOnClickListener(replyClick);
	   cancel_btn.setOnClickListener(cancelClick);
	   if(mailboxthread != null)
	   {
	       if(mailboxthread.mthread.object_id>0)
	       {
	           isupdate = true;
	           compose_view.setVisibility(View.GONE);
	       }
	       else
	       {
	           isupdate = false;
	           compose_view.setVisibility(View.VISIBLE);
	       }
	       //Between Xiaocong He, Zhang Hui, Pat Chan, Rao Hong and You
	       subject_name.setText(isEmpty(mailboxthread.mthread.subject)==true?getString(R.string.no_subject):mailboxthread.mthread.subject);
	      
    	   long time = Math.max(mailboxthread.mthread.inbox_updated_time, mailboxthread.mthread.outbox_updated_time);
    	   if(time > 0)
    	       subject_create.setText(String.format(getString(R.string.mail_update_date_fromat), DateUtil.converToRelativeTime(mContext, new Date(time))));
	       mailboxthread.mthread.unread = 0;
	       orm.addMailThread(mailboxthread.mthread); // mark as unread
	   }
	   
	   List<MailboxMessage> tmp = this.getIntent().getParcelableArrayListExtra("messages");
	   if(tmp ==null || tmp.size() == 0)
	   {
	       tmp = orm.getMailMessages(tid);
	   }
	   
	   //load from website
	   if(tmp==null || tmp.size() == 0)
	   {
	       loadRefresh();
	   }
	   else
	   {
	       inMsg.addAll(tmp);
	   }
	   
	   showUI();
	   
	   setTitle();
	   setTitle(title);
	   SocialORM.Account account = orm.getFacebookAccount();
       if(checkFacebookAccount(this, account))
       {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
            {
        	    perm_session.attachActivity(this);
                facebookA = new AsyncFacebook(perm_session);
                if(isupdate == false)
                {
                    relative_users.setVisibility(View.VISIBLE); 
                    formatUser();
                }
                else
                {
                    relative_users.setVisibility(View.GONE); 
                }
                loadMainBox();
            }
            else
            {
                launchFacebookLogin();
            }        	
       }
   } 
   
   View.OnClickListener replyClick = new View.OnClickListener() {
       public void onClick(View v) 
       {
           hideInputKeyBoard(v);         
           handler.obtainMessage(FACEBOOK_MAIL_SEND).sendToTarget();
       }
   };
   
   View.OnClickListener cancelClick = new View.OnClickListener() {
       public void onClick(View v) 
       {
           finish();
       }
   }; 
   
   public boolean isFromInbox()
   {
	   return frominbox;
   }
    AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
    {
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			if(FacebookMailItemView.class.isInstance(v))
            {
                 FacebookMailItemView fv= (FacebookMailItemView)v;                 
                 Intent intent = new Intent(mContext, FacebookSingleMailDetailActivity.class);                
                 //set mail conversation detail information
                 MailboxMessage mail    = fv.getMessage();
                 intent.putExtra("mailboxmessage", mail);
                 mContext.startActivity(intent);
            }
		}
	};   
   private class MyURLSPan extends URLSpan
   {
       String url;
       public MyURLSPan(Parcel src) {
           super(src);                
       }
       
       public MyURLSPan(String src) {
           super(src);
           url = src;
       }

       @Override
       public String getURL() {                
           return super.getURL();
       }

        @Override
        public void onClick(View widget) 
        {   
           SpannableString sb = (SpannableString)relative_users.getText();
           URLSpan[] spans = relative_users.getUrls();
               
           int start = sb.getSpanStart(this);
           int end   = sb.getSpanEnd(this);
           String text = sb.subSequence(start, end).toString();
                                             
           Log.d("MyURLSPan", "click= text="+text + " url="+getURL());
           Uri uri = Uri.parse(getURL());
           processUserNameClick(uri);
        }            
    }
   
    private void processUserNameClick(Uri uri)
    {
        boolean openinBrowser = true;
        if(isProfile(uri.toString()))
        {
            openinBrowser = false;
            Log.d(TAG, "open profile="+uri.toString());
            //launch profile
            //get id, we user user and page to get id
            String id = uri.getQueryParameter("id");
            Intent intent = new Intent(mContext, FacebookAccountActivity.class);                  
           
		    FacebookUser.SimpleFBUser targ = orm.getSimpleFacebookUser(Long.valueOf(id));
		    if(targ != null)
		    {
			    intent.putExtra("uid",      targ.uid);
			    intent.putExtra("username", targ.name);
			    intent.putExtra("imageurl", targ.pic_square);				
		    }
			else
			{
			    Page pp = orm.getPageBypid(Long.valueOf(id));
			    if(pp != null)
			 	{
			        intent.putExtra("frompage", true);
				    intent.putExtra("uid",      pp.page_id);
				    intent.putExtra("username", pp.name);
					intent.putExtra("imageurl", pp.pic_square);				
				}
			    else{
				    intent.putExtra("uid",      Long.valueOf(id));
				    intent.putExtra("username", id);
			    }
			}
            mContext.startActivity(intent);           
        }
        else
        {
        	Log.d(TAG, "what is the profile link="+uri.toString());
        }
    }
   
    private void resetLinkForProfile(String username, String rawname, TextView textview)
    {
		Log.d(TAG, "username = "+username);
		textview.setText(Html.fromHtml(username));
		
		SpannableString sb = (SpannableString)textview.getText();
        SpannableString ss = new SpannableString(rawname);                
        URLSpan[] spans = textview.getUrls();
        for (int i = 0; i < spans.length; i++) 
        {
           int start = sb.getSpanStart(spans[i]);
           int end   = sb.getSpanEnd(spans[i]);
           String text = sb.subSequence(start, end).toString();
           
           Log.d(TAG, "text="+text + " url="+spans[i].getURL());
           
           MyURLSPan my = new MyURLSPan(spans[i].getURL());
           ss.setSpan(my, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
       
        Log.d(TAG, "username  SpannableString = "+ss);
        textview.setText(ss);
	}
   
    final static String profileURL = "http://www.facebook.com/profile.php?id=";   
    private String formatReceivers(List<FacebookUser.SimpleFBUser> users)
    {
    	String rawrel  = ""; 
	    String rel     ="";
	    if(users == null || users.size() ==0)
	    {
		   rel = String.format(getString(R.string.mail_receivers_just_you), String.format("<a href='%1$s'>yourself</a>", profileURL+perm_session.getLogerInUserID()));
		   rawrel = String.format(getString(R.string.mail_receivers_just_you), "yourself");
	    }
	    else
	    {
		   for(int i=0;i<users.size();i++)
		   {
			   if(i>0)
			   {
				   rel    +=", ";
				   rawrel += ", ";
			   }
			   
			   FacebookUser.SimpleFBUser suser = users.get(i);
			   String item = String.format("<a href='%1$s'>%2$s</a>", profileURL+suser.uid, suser.name);
			   rel    += item;
			   rawrel += String.format("%1$s", suser.name);
		   }
		   
		   String you = String.format("<a href='%1$s'>You</a>", profileURL+perm_session.getLogerInUserID());
		   rel    = String.format(getString(R.string.mail_receivers), rel,     you);
		   rawrel = String.format(getString(R.string.mail_receivers), rawrel,  "You");
	    }
	   
	   
	    Log.d(TAG, "rel="+rel + "\nraw="+rawrel);
	    
	    final String relative    = rel;
	    final String rawrelative = rawrel;
	    handler.post( new Runnable()
	    {
	        public void run()
		    {
		        resetLinkForProfile(relative, rawrelative, relative_users);
		    }
	    });	   
	    	    
	    return null;
    }
   
   private void formatUser()
   {
	   List<Long> noCache = new ArrayList<Long>();	   
	   long[] list    = new long[mailboxthread.mthread.recipients.size()];
	   
	   List<FacebookUser.SimpleFBUser> susers = new ArrayList<FacebookUser.SimpleFBUser>();
	   if(mailboxthread.mthread != null)
	   {
		   for(int i=0;i<mailboxthread.mthread.recipients.size();i++)
		   {
			   long id  = (mailboxthread.mthread.recipients.get(i));
			   if(id != perm_session.getLogerInUserID())
			   {
				   list[i]  = id;
				   FacebookUser.SimpleFBUser suser = orm.getSimpleFacebookUser(mailboxthread.mthread.recipients.get(i));
				   if(suser == null)
				   {
					   noCache.add(mailboxthread.mthread.recipients.get(i));
				   }		   
				   else
				   {
					   susers.add(suser);
				   }
			   }
		   }
	   }
	   
	   if(noCache.size() == 0)
	   {
		   if(susers.size()==0)
		   {
			   formatReceivers(null);			   
		   }
		   else
		   {
		       formatReceivers(susers);		    
		   }
	   }
	   else//get from from website		   
	   {
		   if(FacebookBaseActivity.class.isInstance(mContext))
			{
				AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
				if(af != null)
				{
					af.getSimpleUsersAsync(list, new FacebookAdapter()
			    	{
			    		@Override public void getSimpleUsers(List<FacebookUser.SimpleFBUser> users)
			            {			    
			    		    orm.addFacebookSimpleUser(users);
			    			formatReceivers(users);
			            }
			    		
			            @Override public void onException(FacebookException e, int method) 
			            {
			            	Log.d(TAG, "fail to get the receivers ="+e.getMessage());			                     	
			            }
			    	});
				}
			}
	   }
   }
   public void setTitle()
   {
       if(frominbox == false)
       {
    	   title = getString(R.string.facebook_mail_detail);
       }
       else
       {
           title = getString(R.string.facebook_mail_detail);
       }
	   
   }
   
   /*
   @Override
   public void titleSelected() 
   {		
		super.titleSelected();
	    if(frominbox)
	    {
	        handler.obtainMessage(FACEBOOK_MAIL_REPLY).sendToTarget();
	    }		
	}
	*/
   
    @Override
    protected void doMailReply(Object obj) 
    {
        Intent intent = new Intent(mContext, FacebookMailActivity.class);				
	    //set mail conversation detail information		 
	    intent.putExtra("mailboxthread", mailboxthread);				 
	    intent.putExtra("reply",    true);
	    ((FacebookBaseActivity)mContext).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_MAIL_SEND);    
	}

    @Override
	protected void doMailForward(Object obj) 
	{
    	 Intent intent = new Intent(mContext, FacebookMailActivity.class);
		 intent.putExtra("mailboxthread", mailboxthread);				 
		 intent.putExtra("forward",    true);
		 
		 if(MailboxMessage.class.isInstance(obj))
		 {
			 MailboxMessage mailmsg = (MailboxMessage)obj;
			 Log.d(TAG,"doMailForward: body is "+mailmsg.body);
		     intent.putExtra("content", mailmsg.body==null?"":mailmsg.body);
		     MessageThreadInfo mt = orm.getMailThread(mailmsg.threadid, false);
		     if(mt != null)
		     {
		         intent.putExtra("subject", mt.subject==null?"":mt.subject);
		     }
		     mt.despose();
		     mt = null;
		     
		 }
		 ((FacebookBaseActivity)mContext).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_MAIL_SEND);
	}  
    
	private void showUI() 
	{		
		if(inMsg != null && inMsg.size() > 0)
		{
			MailAdapter sa = new MailAdapter(FacebookMailDetailActivity.this, inMsg);
			sa.isupdate = isupdate;
			messages.setAdapter(sa);
			messages.setSelection(lastVisiblePos);
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
    		perm_session.attachActivity(this);
    		facebookA = new AsyncFacebook(perm_session);
    		loadMainBox();
    	}
    }
    
    @Override
    protected void loadRefresh()
    {
        super.loadRefresh();    
        handler.obtainMessage(FACEBOOK_MAIL_DETAIL_GET).sendToTarget();
    }
    
	private void loadMainBox() 
	{	
		if(tid != -1)
		{
		    if(true || mailboxthread.mthread.unread > 0)
		    {
		        handler.obtainMessage(FACEBOOK_MARK_READ).sendToTarget();
		    }		    		 
		}
	}

	final int FACEBOOK_MAIL_DETAIL_GET     = 0;
	final int FACEBOOK_MAIL_DETAIL_UI      = 1;
	final int FACEBOOK_MAIL_DETAIL_GET_END = 2;
	final int FACEBOOK_MAIL_REPLY          = 3;
	final int FACEBOOK_MARK_READ           = 4;
	final int FACEBOOK_MAIL_SEND           = 5;
	final int FACEBOOK_MAIL_SEND_END       = 6;
	
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
            case FACEBOOK_MAIL_SEND:
            {
                doMailBoxReply();                
                break;
            }
            case FACEBOOK_MAIL_SEND_END:
            {
                end();      
                reply_btn.setEnabled(true);
                dismissDialog(DLG_SEND_MAIL);
                if(msg.getData().getBoolean(RESULT) == true)
                {
                        String content = msg.getData().getString("content");
                        long tid = msg.getData().getLong("mtid");
                        if(mailboxthread != null &&  mailboxthread.mthread !=null)
                        {
                            cacheReplyMessage(content,tid,mailboxthread.mthread); 
                        }
                        messages.setAdapter(null);
                        if(inMsg!=null)
                        {
                            inMsg.clear();
                        }
                        inMsg = orm.getMailMessages(tid);
                        content_view.setText("");
                        lastVisiblePos = inMsg.size()-1;
                        showUI(); 
                }
                  
                break;
            }               
                case FACEBOOK_MARK_READ:
                {
                	doMarkRead();
                	break;
                }
                case FACEBOOK_MAIL_REPLY:
                {
                	doMailReply(null);
                	break;
                }
                case FACEBOOK_MAIL_DETAIL_GET:
                {
                	getMailInBoxSummaryInfo();
                	break;
                }                
                case FACEBOOK_MAIL_DETAIL_UI:
                {
                	showUI();	
                	//continue to get the send detail
                	break;
                }                
                case FACEBOOK_MAIL_DETAIL_GET_END:
                {
                	end();
                	setTitle(R.string.facebook_mail_detail);
                	
                	if(msg.getData().getBoolean(RESULT) == false)
                	{
                	    Log.d(TAG, "get data from database");
                	    inMsg.clear();
                	    inMsg = orm.getMailMessages(tid);
                	}
                	break;
                }
            }
        }
	}
	
	boolean isReplying = false;
	Object obj = new Object();
	
	public void doMailBoxReply() 
    {
	    final String content= content_view.getText().toString().trim();

        if(isEmpty(content))
        {
            return;
        }
        if(isReplying == true)
        {
            return;
        }

        showDialog(DLG_SEND_MAIL);
        if(facebookA != null)
        {
            begin();
            reply_btn.setEnabled(false);
            synchronized(obj)
            {
                isReplying = true;
            }
            
            facebookA.mailReplyAsync(mailboxthread.mthread.thread_id, content, new FacebookAdapter()
            {
                @Override public void mailReply(long tid)
                {
                    Log.d(TAG, "after reply="+tid);
                    synchronized(obj)
                    {
                        isReplying = false;
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
                    synchronized(obj)
                    {
                        isReplying = false;
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
                long[] uids = new long[mailboxthread.mthread.recipients.size()];
                for(int i=0;i<mailboxthread.mthread.recipients.size();i++)
                {
                    uids[i] = mailboxthread.mthread.recipients.get(i);
                }
                facebookA.sendEmailAsync(content, mailboxthread.mthread.subject, uids, new FacebookAdapter()
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
   
	private void getMessages(List<MailboxMessage> msgs)
	{
	    orm.addMailMessages(msgs);	    
	    handler.post( new Runnable()
	    {
	        public void run()
	        {
	            messages.setAdapter(null);
	        }
	    });	   
	    
	    if(inMsg != null)
        {
            inMsg.clear();
            inMsg = null;
        }
        inMsg = orm.getMailMessages(tid);        
        java.util.Collections.sort(inMsg);
        
        handler.obtainMessage(FACEBOOK_MAIL_DETAIL_UI).sendToTarget();
	}
	
	public void getMailInBoxSummaryInfo() 
	{
	    if(this.isInProcess() == true)
	    {
	        showToast();
	        return;
	    }
		begin();
		
    	Log.d(TAG, "before get inbox mail");
    	//notifyLoading();  
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}
    	    	
    	facebookA.getThreadDetailAsync(tid, new FacebookAdapter()
    	{
    		@Override public void getThreadDetail(List<MailboxMessage> mails)
            {
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				orm.addMailMessages(mails);
				getMessages(mails);				
                if(donotcallnetwork == false)//I am still alive
                {
					handler.obtainMessage(FACEBOOK_MAIL_DETAIL_UI).sendToTarget();
	            	//cancelNotify();
                }       
                
                Message msd = handler.obtainMessage(FACEBOOK_MAIL_DETAIL_GET_END);
                msd.getData().putBoolean(RESULT, true);
                handler.sendMessage(msd);
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
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
	                Message msd = handler.obtainMessage(FACEBOOK_MAIL_DETAIL_GET_END);
	                msd.getData().putBoolean(RESULT, false);
	                handler.sendMessage(msd);
            	}
            }
    	});
	}
	private boolean markreadsuc=false;
    public void doMarkRead() 
    {	
    	facebookA.markReadAsync(tid, new FacebookAdapter()
    	{
    		@Override public void markRead(boolean suc)
            {
    		    markreadsuc = suc;
    			Log.d(TAG, "mark read="+suc);
            }
    		@Override public void onException(FacebookException e, int method) 
            {
    			Log.d(TAG, "fail to mark read="+e.getMessage());
            }
    	});
	}
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onresumeTag is="+onresumeTag);
        if(onresumeTag == true)
        {
           messages.setAdapter(null);
           if(inMsg!=null)
           {
               inMsg.clear();
           }
           inMsg = orm.getMailMessages(tid);
           showUI(); 
        }
        onresumeTag = false;
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onresumeTag is="+onresumeTag);
        super.onPause();
        onresumeTag = true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {         
            Log.d(TAG, "KEYCODE_BACK coming="+this);
            stopLoading();
            restoreTitle();
            if(markreadsuc == true)
            {
                Intent data = new Intent();
                data.putExtra("markread", true);
                data.putExtra("tid",       tid);
                this.setResult(FACEBOOK_MAIL_DETAIL, data);
                this.finish();
            }
            System.gc();
                
        }
        return super.onKeyDown(keyCode, event);
    }
    

	private void notifyLoading() 
    {
    	notify.notifyOnce(R.string.facebook_mailbox_loading, R.drawable.facebook_logo, 30*1000);		
	}
	
	public void registerAccountListener() {
		AccountManager.registerAccountListener("FacebookMailDetailActivity", this);		
	}
	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("FacebookMailDetailActivity");		
	}
}
