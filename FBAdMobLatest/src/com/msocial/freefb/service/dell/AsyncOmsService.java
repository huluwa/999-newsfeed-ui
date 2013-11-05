package com.msocial.freefb.service.dell;

import java.util.HashMap;
import java.util.List;

import com.msocial.freefb.ui.NetworkConnectionListener;

import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import twitter4j.threadpool.QueuedThreadPool;

public class AsyncOmsService {
	FacebookSession fs;
	public AsyncOmsService(FacebookSession session)
	{
		fs = session;
	}
	public void setSession(FacebookSession session)
	{
		fs = session;
	}

	public void phoneLookupAsync(List<Long> tid, String entities,OmsServiceListner listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(PHONE_LOOKUP, listener, new Object[]{tid, entities}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(OmsServiceListner listener,Object[] args) throws FacebookException {
            	//showNotification===>start to run task;
            	if(fs == null)
            		throw new FacebookException("no facebook session", PHONE_LOOKUP);
            	
                listener.phoneLookup((List<Long>)args[0],fs.phoneBookLookup((String)args[1], null));
                //cancelNotification===>cancel Notification;
            }
        });
	}
	
	public void phoneLookupAsync(String entities,OmsServiceListner listener ) 
    {
        getThreadPool().dispatch(new AsyncTask(PHONE_LOOKUP, listener, new Object[]{entities}) 
        {
            @SuppressWarnings("unchecked")
            public void invoke(OmsServiceListner listener,Object[] args) throws FacebookException {
                //showNotification===>start to run task;
                if(fs == null)
                    throw new FacebookException("no facebook session", PHONE_LOOKUP);
                
                listener.phoneLookup(fs.phoneBookLookup((String)args[0], null));
                //cancelNotification===>cancel Notification;
            }
        });
    }
	
	public void phoneLookupAsync(List<Long> tid,List<Long> peopleids,String entities,OmsServiceListner listener)
	{
	    getThreadPool().dispatch(new AsyncTask(PHONE_LOOKUP, listener, new Object[]{tid,peopleids,entities}) 
        {
            @SuppressWarnings("unchecked")
            public void invoke(OmsServiceListner listener,Object[] args) throws FacebookException {
                //showNotification===>start to run task;
                if(fs == null)
                    throw new FacebookException("no facebook session", PHONE_LOOKUP);
                listener.phoneLookup((List<Long>)args[0],(List<Long>)args[1],fs.phoneBookLookup((String)args[2], null));              
            }
        });
	}
	
	public void addAsFriendAsync(Long tid,Long fuid,OmsServiceListner listener)
    {
	    getThreadPool().dispatch(new AsyncTask(REQUEST_FRIENDS,listener,new Object[]{tid,fuid})
	    {
            @Override
            void invoke(OmsServiceListner listener, Object[] args) throws FacebookException {
                listener.addAsFriend((Long)args[0], fs.requestFriend((Long)args[1]));
            }
	    });
       
    }	
	
	public void eventSync(Long tid,OmsServiceListner listener)
	{
		getThreadPool().dispatch(new AsyncTask(EVENT_SYNC, listener, new Object[]{tid}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(OmsServiceListner listener,Object[] args) throws FacebookException {
            	if(fs == null)
            		throw new FacebookException("no facebook session", EVENT_SYNC);
            	
                listener.eventSync((Long)args[0],fs.getUpcomingEvents());
            }
        });
		
	}
	
	public void eventAdd(Long tid,HashMap eventinfo,Long p_eid,OmsServiceListner listener)
	{
		getThreadPool().dispatch(new AsyncTask(EVENT_ADD, listener, new Object[]{tid,eventinfo,p_eid}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(OmsServiceListner listener,Object[] args) throws FacebookException {
            	if(fs == null)
            		throw new FacebookException("no facebook session", EVENT_ADD);
            	
                listener.eventAdd((Long)args[0],fs.createEvent((HashMap)args[1]),(Long)args[2]);
            }
        });
	}
	
	public void contactSync(Long tid,OmsServiceListner listener)
	{
		getThreadPool().dispatch(new AsyncTask(CONTACT_SYNC, listener, new Object[]{tid}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(OmsServiceListner listener,Object[] args) throws FacebookException {
            	if(fs == null)
            		throw new FacebookException("no facebook session", CONTACT_SYNC);
            	
                listener.contactSync((Long)args[0],fs.getContactInfo());
            }
        });
	}
	
	public void phoneLookupAll(Long tid,OmsServiceListner listener)
	{
		getThreadPool().dispatch(new AsyncTask(PHONE_LOOKUP_ALL, listener, new Object[]{tid}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(OmsServiceListner listener,Object[] args) throws FacebookException {
            	
            	if(fs == null)
            		throw new FacebookException("no facebook session", PHONE_LOOKUP_ALL);
            	
                listener.phoneLookupAll((Long)args[0]);
            }
        });
	}
	
	/*
	 * for thread dispatch
	 */
	private QueuedThreadPool getThreadPool()
	{
		return AsyncFacebook.getThreadPool();
	}
	 
    abstract class AsyncTask implements Runnable {
    	OmsServiceListner listener;
        Object[] args;
        int method;
        AsyncTask(int method, OmsServiceListner listener, Object[] args) 
        {
            this.method = method;
            this.listener = listener;
            this.args = args;
        }

        abstract void invoke(OmsServiceListner listener,Object[] args) throws FacebookException;

        public void run() 
        {
            try 
            {
                   invoke(listener,args);
            }
            catch (FacebookException te) 
            {
                if (null != listener) {
                    listener.onException(te,method, args);
                }
            }
        }
    }
    
    public final static int PHONE_LOOKUP  = 1;
    public final static int CONTACT_SYNC  = 2;
    public final static int EVENT_ADD     = 3;
    public final static int EVENT_SYNC    = 4;
    public final static int PHONE_LOOKUP_ALL  = 5;
    public final static int REQUEST_FRIENDS   = 6;
	public void attachNetworkListener(NetworkConnectionListener listener) 
	{
		if(fs != null)
			fs.attachActivity(listener);
	}
}
