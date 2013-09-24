package com.msocial.freefb.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.ui.AccountListener;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.MailboxMessage;
import oms.sns.service.facebook.model.MessageThreadInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/*
 * save the thread information into database
 * need to discuss
 * save the unread thread message detail into database
 * 
 */
public class MailService implements ServiceInterface, AccountListener
{
	final String TAG="sns-MailService";
	
	SNSService mContext;
	
	FacebookLoginHelper   loginHelper;
	private AsyncFacebook facebookA;
	FacebookSession       perm;
	SocialORM             orm;
	Handler               handler;
	
	int currentInPos    =0;
	int currentOutPos   =0;
	int currentupdatePos=0;
	int currentMsgPos   =0;
	int limit           =20;		
	int nErrorTimes     = 0;
		
	public MailService(SNSService con, SocialORM orm, FacebookLoginHelper loginHelper)
	{
		mContext  = con;
		this.orm  = orm;
		this.loginHelper = loginHelper;
		
		registerAccountListener();
		handler = new MailHanlder();		
	}
	
	public void afterLogin()
	{}
	
	public void Start()
	{
		Log.d(TAG, "start Facebook mail service");
		perm = loginHelper.getPermanentSesstion();
		if(SNSService.TEST_LOOP || perm != null)
		{
			rescheduleMail(true);
		}
	}	
	
	public void checkMailRightNow()
	{
		getMessageFromFacebook(20);		
	}
	
	static int nErrorCount=0;
	public void rescheduleMail(boolean force)
	{
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
		long nexttime = System.currentTimeMillis()+ orm.getFacebookMailUpdatePeriod()*60*60*1000L;	
		
		if(force == true)
		{
			nexttime = System.currentTimeMillis()+ 5*60*1000;
		}
		
		if(SNSService.TEST_LOOP)
		{
			nexttime = System.currentTimeMillis()+ 60*1000;
		}
		
		Intent i = new Intent();
		i.setClassName("com.msocial.freefb", "com.msocial.freefb.service.SNSService");
		i.setAction("com.msocial.freefb.intent.action.MAIL_CHECK");
		PendingIntent mailpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, nexttime, mailpi);
	}
	
	//time is up, need reget the data
	public void alarmMailCheckComming()
	{
		Log.d(TAG, "alarmMailCheckComming");		
		getMessageFromFacebook(20);			
		
		long nexttime = System.currentTimeMillis()+ orm.getFacebookMailUpdatePeriod()*60*60*1000L;	
		if(SNSService.TEST_LOOP)
		{
			nexttime = System.currentTimeMillis()+ 60*1000;
		}
		
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		
		Intent i = new Intent();
		i.setClassName("com.msocial.freefb", "com.msocial.freefb.service.SNSService");
		i.setAction("com.msocial.freefb.intent.action.MAIL_CHECK");
		PendingIntent mailpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, nexttime, mailpi);
	}
	
	public void Stop()
	{
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent();
		i.setClassName("com.msocial.freefb", "com.msocial.freefb.service.SNSService");
		i.setAction("com.msocial.freefb.intent.action.MAIL_CHECK");
		PendingIntent mailpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmMgr.cancel(mailpi);
	}
	
	private void getMessageFromFacebook(int timout_seconds)
	{
		//Could we get from database firstly?		
		Message msg = handler.obtainMessage(FACEBOOK_MESSAGE_GET);
        handler.sendMessageDelayed(msg, timout_seconds*1000);  
	}
	
	private void getMailBoxMessage()
    {    	
		SocialORM.Account account = orm.getFacebookAccount();
        if(mContext.checkFacebookAccount(mContext, account))
        {
			
        	perm = null;
			perm = loginHelper.getPermanentSesstion();
			
			if(perm != null)
			{
				if(facebookA == null)
				{
					facebookA = new AsyncFacebook(perm);
				}				
	        	facebookA.setSession(perm);
	        	
	        	//TODO batch_run getInboxMessage,getOutboxMessage,getUpdateMessage
	        	
	        	facebookA.batch_run_getMessageThreadAsync(-1, limit, 0, false, new FacebookAdapter()
                {
	        	    
	        	    @Override public void batch_run_getMessageThread(HashMap<Integer, Object> batchResult) {
	        	        Log.d(TAG," batch_run_getMessageThrea ");
	        	        if(batchResult != null)
	        	        {
	        	            List<MessageThreadInfo> inMsg     = (List<MessageThreadInfo>)batchResult.get(0);
	        	            List<MessageThreadInfo> outMsg    = (List<MessageThreadInfo>)batchResult.get(1);
	        	            List<MessageThreadInfo> updateMsg = (List<MessageThreadInfo>)batchResult.get(2);
	        	            orm.addInMailThread (inMsg);
	        	            orm.addOutMailThread(outMsg);
	        	            orm.addUpdateMailThread(updateMsg);
	        	            
	        	            //get ids
	        	            List<Long> tids = new ArrayList<Long>();
	        	            for(MessageThreadInfo item:inMsg)
	        	            {
	        	                tids.add(item.thread_id);
	        	            }
	        	            
	        	            for(MessageThreadInfo item:outMsg)
                            {
	        	                if(tids.contains(item.thread_id) == false)
	        	                {
                                    tids.add(item.thread_id);
	        	                }
                            }
	        	            
	        	            for(MessageThreadInfo item:updateMsg)
                            {
                                if(tids.contains(item.thread_id) == false)
                                {
                                    tids.add(item.thread_id);
                                }
                            }
	        	            
	        	            long[] tidsarr = new long[tids.size()];
	        	            for(int i=0;i<tids.size();i++)
	        	            {
	        	                tidsarr[i] = tids.get(i);
	        	            }
	        	            tids.clear();
	        	            tids = null;
	        	            
	        	            Message ddd = handler.obtainMessage(FACEBOOK_MESSAGE_DETAIL_GET);
	        	            ddd.getData().putLongArray("tids", tidsarr);
	        	            ddd.sendToTarget();
	        	            
	        	        }
	        	        Message msd = handler.obtainMessage(FACEBOOK_MESSAGE_GET_END);
	        	        msd.getData().putBoolean("RESULT", true);
	        	        msd.sendToTarget();
	        	    }

                    @Override public void onException(FacebookException e, int method) 
                    {
                        Log.d(TAG, "fail to get basic mail inbox information");
                        Message msd = handler.obtainMessage(FACEBOOK_MESSAGE_GET_END);
                        msd.getData().putBoolean("RESULT", false);
	        	        msd.sendToTarget();
                    }
                });
	        	
        	}
        	else
        	{
        		Log.d(TAG, "no session");	
				mContext.needLogin();
        	}
        }
    }
	
	//
	//mailbox_getThreadMessage
	public void getMessages(final long[] tids)
	{
	    Log.d(TAG, "to get message for thread size="+tids.length);
	    
	    SocialORM.Account account = orm.getFacebookAccount();
        if(mContext.checkFacebookAccount(mContext, account))
        {
            
            perm = null;
            perm = loginHelper.getPermanentSesstion();
            
            if(perm != null)
            {
                if(facebookA == null)
                {
                    facebookA = new AsyncFacebook(perm);
                }               
                facebookA.setSession(perm);
                
                //TODO batch_run getInboxMessage,getOutboxMessage,getUpdateMessage
                
                facebookA.getMailThreadMessageAsync(tids, currentMsgPos, limit, new FacebookAdapter()
                {
                    @Override public void getThreadDetail(List<MailboxMessage> msgs)
                    {
                        Log.d(TAG, "get messages="+msgs.size());
                        
                        if(msgs.size() == limit)
                        {
                            currentMsgPos += limit;
                            
                            Log.d(TAG, "continue to message pos="+currentMsgPos);
                            getMessages(tids);
                        }
                        
                        orm.addMailMessages(msgs);
                        
                        msgs.clear();
                        msgs = null;
                    }
                    @Override public void onException(FacebookException e, int method) 
                    {
                        Log.d(TAG, "fail to get messages="+e.getMessage());
                    }
                });
            }
        }
	}
	
	final static int FACEBOOK_MESSAGE_GET           = 1;
	final static int FACEBOOK_MESSAGE_GET_END       = 2;
	final static int FACEBOOK_MESSAGE_DETAIL_GET    = 3;
	final static int FACEBOOK_MESSAGE_DETAIL_GET_END= 4;
	final static int FACEBOOK_MESSAGE_DETAIL_UIDS_GET=5;
	private class MailHanlder extends Handler 
    {
        public MailHanlder()
        {
            super();            
            Log.d(TAG, "new MailHanlder");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
            	case FACEBOOK_MESSAGE_GET:
            	{
            		Log.d(TAG, "call FACEBOOK_MESSAGE_GET");
            		getMailBoxMessage();
            		break;
            	}
            	case FACEBOOK_MESSAGE_GET_END:
            	{
            		if(msg.getData().getBoolean("RESULT", false) == true)
            		{
            			Log.d(TAG, "suc to get mail");
            			nErrorTimes = 0;
            		}
            		else
            		{
            			//user have three times to re-get the mail
            			nErrorTimes++;
            			if(nErrorTimes < 3)
            			{
            				//re get at 5 minutes later
            				getMessageFromFacebook(5*60);
            			}
            		}
            		Log.d(TAG, "call FACEBOOK_MESSAGE_GET_END");            		
            		break;
            	}    
            	case FACEBOOK_MESSAGE_DETAIL_GET:
                {       
                    long[]tidss = msg.getData().getLongArray("tids");
                    getMessages(tidss);
                    break;
                }
            	case FACEBOOK_MESSAGE_DETAIL_GET_END:
            	{  		
            		Log.d(TAG, "FACEBOOK_MESSAGE_DETAIL_GET_END");
            		break;
            	}
            	case FACEBOOK_MESSAGE_DETAIL_UIDS_GET:
            	{
            		Log.d(TAG, "get message FACEBOOK_MESSAGE_DETAIL_UIDS_GET");            		            		
            		break;
            	}
            }
        }
    }
	public void Pause() {
		// TODO Auto-generated method stub
		
	}

	public void logout() {
		// TODO Auto-generated method stub
		
	}

	public void onLogin() 
	{
		perm = null;
		perm = loginHelper.getPermanentSesstion();
		
		if(perm != null)
		{
			if(facebookA == null)
			{
				facebookA = new AsyncFacebook(perm);
			}				
        	facebookA.setSession(perm);
        	
        	//just logined
        	rescheduleMail(true);
		}		
	}

	public void onLogout() 
	{
		perm = null;
		facebookA = null;
		
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent();
		i.setClassName("com.msocial.freefb", "com.msocial.freefb.service.SNSService");
		i.setAction("com.msocial.freefb.intent.action.MAIL_CHECK");
		PendingIntent mailpi = PendingIntent.getService(mContext.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmMgr.cancel(mailpi);
	}

	public void registerAccountListener() {
		AccountManager.registerAccountListener("MailService", this);
		
	}

	public void unregisterAccountListener() 
	{
		AccountManager.unregisterAccountListener("MailService");		
	}
	
}
