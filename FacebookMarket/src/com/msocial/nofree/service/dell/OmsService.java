package com.msocial.nofree.service.dell;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.service.FacebookLoginHelper;
import com.msocial.nofree.service.ServiceInterface;
import com.msocial.nofree.service.dell.OmsTask.EventAddTask;
import com.msocial.nofree.service.dell.OmsTask.TaskListener;
import com.msocial.nofree.ui.AccountListener;
import com.msocial.nofree.ui.NetworkConnectionListener;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.model.PhoneBook;

public class OmsService implements  ServiceInterface, TaskListener, AccountListener, NetworkConnectionListener
{
	final static String TAG="sns-OmsService";
	public static boolean DEBUG=true;
	
	static class STATUS{
		final static String RUNNING = "running"; 
		final static String PAUSE = "pause"; 
		final static String STOPED = "stoped"; 
		final static String INITIAL = "initial"; 
		final static String NOT_LOGIN = "not_login"; 
	}
	
	Context mContext;
	SocialORM orm;
	FacebookLoginHelper loginHelper;
	String              status;
	
	Object       mLock = new Object();	
	OmsHandler   handler;
	MessageQueue msgQueue;
	AsyncOmsService asyncOms;
	int timesforonefetch = 200;	
	private static OmsService omsService = null;
	
	//for UI process
	public static OmsService getInstance()
	{
		return omsService;
	}
	
	public static OmsService instance(Context con,SocialORM orm,FacebookLoginHelper loginHelper){
		if(omsService == null){
			omsService = new OmsService(con,orm,loginHelper);
		}
		return omsService;
	}
	private OmsService(Context con, SocialORM orm, FacebookLoginHelper loginHelper)
	{
		this.mContext = con;
		this.orm      = orm;
		this.loginHelper = loginHelper;
		status = STATUS.INITIAL;
		msgQueue = new MessageQueue();
		
		OmsTask.setTaskListener(this);
		
		registerAccountListener();
		//if session is null?
		asyncOms = new AsyncOmsService(loginHelper.constructPermSession());
		asyncOms.attachNetworkListener(this);
		//observer for Contact
	}
	public Context getContext()
	{
		return mContext;
	}
	
	public void dumpMessages()
	{
		msgQueue.dumpQueue_l();
	}
	public void Pause() 
	{
		synchronized(mLock)
		{
			if(status.equals(STATUS.RUNNING))
			{
			    status = STATUS.PAUSE;
			}
		}
	}
	public void Resume() 
	{
		synchronized(mLock)
		{
			if(status == STATUS.PAUSE)
			{
			    status = STATUS.RUNNING;
			}
		}
	}
	
	//for task
	public void onFinished(OmsTask task) 
	{
		//notify UI
		
	}
	public void onResume(OmsTask task) 
	{
		//notify UI
	}
	public void onStart(OmsTask task) 
	{
		//notify UI		
	}
	public void onStop(OmsTask task) 
	{
		//notify UI		
	}
	
	
	public Handler getHandler()
	{
		return handler;
	}
	
	public void queueTask(OmsTask task)
	{
		queueTaskToTail(task);
		/*
		synchronized(msgQueue)
		{
		    msgQueue.enqueueMessage(task, 0);
		}*/
	}
	
	/*
	 * begin queue the adding new task
	 * 
	 * when have more than 200 new task, queue them, 
	 * when have longer than 20 seconds no task queue them
	 */
	private long lastqueuetime=0;
	List<OmsTask> pendingTasks = new ArrayList<OmsTask>();
	PreProcessHandler phandler = new PreProcessHandler();
	public void queueTaskToTail(OmsTask task)
	{
		Message msg = phandler.obtainMessage(ADD_NEW_TASK);
		msg.getData().putLong("id",      task.id);
		msg.getData().putInt("action",  task.action);
		msg.getData().putLong("taskid", task.taskID);
		
		if(OmsTask.ACTION.EVENT_ADD == task.action)
		{ 
		    msg.getData().putInt("categoryid",((EventAddTask)task).categoryid);
		    msg.getData().putInt("subcategoryid", ((EventAddTask)task).subcategoryid);
		}
		phandler.sendMessage(msg);
	}	
	
	final int ADD_NEW_TASK=0;
	final int CHECK_TASK  =1;
	
	public class PreProcessHandler  extends Handler 
    {
        public PreProcessHandler()
        {
            super();            
            Log.d(TAG, "new PreProcessHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case ADD_NEW_TASK:
                {
                	Log.d(TAG, "entering add new task");
                	OmsTask task=null;
                	long taskID = msg.getData().getLong("taskid");
                	if(taskID == -1)//invalid task
                	{
                		return ;
                	}
                	
                	long id = msg.getData().getLong("id", -1);                	
            		int action = msg.getData().getInt("action");  
            		
            		if(OmsTask.ACTION.EVENT_ADD == action)
            		{
            		    EventAddTask eventtask = new EventAddTask();
            		    eventtask.id = id;
            		    eventtask.subcategoryid = msg.getData().getInt("subcategoryid");
            		    eventtask.categoryid = msg.getData().getInt("categoryid");
            		    task = eventtask;
            		}
            		else{
                        task = new OmsTask(action, id, taskID);
            		}
            		
            		if(task != null)
            		{
            			synchronized(pendingTasks)
                    	{
	                		pendingTasks.add(task);
	                		lastqueuetime = System.currentTimeMillis();
	                		
	                		//start check
	                		Log.d(TAG, "entering check task");
	                		Message msd = phandler.obtainMessage(CHECK_TASK);
	                		if(task.action != OmsTask.ACTION.CONTACT_LOOK_UP)
	                		{
	                		    msd.getData().putBoolean("NONE_LOOKUP", true);
	                		}
		                	phandler.sendMessageDelayed(msd, 200);
                    	}
            		}            	    
                	break;
                }
                case CHECK_TASK:
                {	
                    //Log.d(TAG, "entering check task");
                    boolean nonelookup = msg.getData().getBoolean("NONE_LOOKUP");
                	synchronized(pendingTasks)
                	{
	                	long now = System.currentTimeMillis();
	                	long span = now-lastqueuetime;
	                	if(nonelookup || pendingTasks.size() >= 200 || (lastqueuetime != 0 && span>= 20*1000L))
	                	{
	                		Log.d(TAG, "queue a list task="+pendingTasks.size() + " span="+span);
	                		synchronized(msgQueue)
	                		{
	                		    msgQueue.enqueueMessage(pendingTasks, 0);	                		    
	                		    lastqueuetime = 0;
	                		    
	                		    //
	                		    this.removeMessages(CHECK_TASK);
	                		}                	
	                	    pendingTasks.clear();
	                    
	                	}
	                	else
	                	{       
	                	    //Log.d(TAG, " queue list size < 200 span time is "+span);
	                		removeMessages(CHECK_TASK);                		
		                	Message msd = phandler.obtainMessage(CHECK_TASK);
		                	phandler.sendMessageDelayed(msd, 200);
	                	}
                	}
                	
                	break;
                }
            }
        }
    }
	/*
	 * end queue the adding new task
	 */
	
	/*
	 * remove the task from queue
	 */
	public void removeTasks(int type)
	{
		msgQueue.removeTasks(type);
	}
	public void removeTask(long taskID)
	{
		msgQueue.removeTask(taskID);
	}
	
	public void Start() 
	{	
		Log.d(TAG, "Start");		
	}
	
	Looper        mServiceLooper;
	HandlerThread ht;
	public void startThread()
	{
		if(status == STATUS.INITIAL)
		{			
			if(handler == null)
			{
				 ht = new HandlerThread("OMS-Task-Service");
				 ht.start();
				 mServiceLooper = ht.getLooper();
				 status = STATUS.RUNNING;
				 handler = new OmsHandler(mServiceLooper);				 					 
				 handler.obtainMessage(NEXT_TASK).sendToTarget();					
			}			
		}
	}
	
	public void Stop() 
	{
		Log.d(TAG, "Stop");
		synchronized(mLock)
		{
			status = STATUS.STOPED;
		}
		
	    //stop the thread
		if(handler != null)
		{
		    handler.obtainMessage(EXIT_LOOP).sendToTarget();
		}
		
		unregisterAccountListener();
	}

	/*
	 * For login progess
	 * @see com.msocial.nofree.service.ServiceInterface#afterLogin()
	 */
	public void afterLogin() 
	{
		Log.d(TAG, "after login afterLogin");		
		//onLogin();
	}
	
	public void logout() 
	{	
		Log.d(TAG, "logout");
		status = STATUS.NOT_LOGIN;
	}	
	
	
	public void onLogin() 
	{	
		Log.d(TAG, "after login in OnLogin in OmsService");
		status = STATUS.RUNNING;
		asyncOms = new AsyncOmsService(loginHelper.constructPermSession());		
		asyncOms.attachNetworkListener(this);
		
		//when login, start to get the next task
		if(handler!=null)
		{
		  Message msd = handler.obtainMessage(NEXT_TASK);
		  handler.sendMessageDelayed(msd, 5*1000);
		}
	}
	public void onLogout() 
	{
		status = STATUS.NOT_LOGIN;		
		asyncOms = null;
	}
	public void registerAccountListener() 
	{
	    AccountManager.registerAccountListener("OmsService", this);
	}
	public void unregisterAccountListener() 
	{
		AccountManager.unregisterAccountListener("OmsService");		
	}
	/*
	 * end for login
	 */
	
	//TODO do we need save the task info into database??
	//
	//For maintain the task
	List<OmsTask> runningtasks = new ArrayList<OmsTask>();
	
	int max_queunesize = 200;
	//max 200
	List<OmsTask> failtasks = new ArrayList<OmsTask>();
	
	//max 200
	List<OmsTask> finishedtasks = new ArrayList<OmsTask>();
	public static int finished_task_num = 0;
	public static int failed_task_num = 0;
	
	void addProcessTask(List<OmsTask> objs)
	{	
		synchronized(runningtasks)
		{
			runningtasks.addAll(objs);
		}
	}
	
	public List<OmsTask> getRunningTasks()
	{		
		return runningtasks;		
	}
	
	public List<OmsTask> getFinishedTasks(){
		return finishedtasks;
	}
	
	public List<OmsTask> getPenddingTasks(){
		
		return msgQueue.getPenddingTask();
	}
	
	public List<OmsTask> getFailedTasks(){
		return failtasks;
	}
	
	
	//resume task
	public void resumeTask(List<OmsTask> tasks)
	{
		//check pre-process tasks
		//if fail to process the task, we need re-queue it into message queue.		
		for(int j=0;j<tasks.size();j++)
		{	
			OmsTask item = tasks.get(j);					
			Log.d(TAG, "queueTaskToTail="+item);
			queueTaskToTail(item);
		}		
	}
	
	//add into finished task directly
	void finishedTask(List<OmsTask> tasks, boolean nouse)
	{
		synchronized(finishedtasks)
		{
			finishedtasks.addAll(tasks);
			finished_task_num += tasks.size();
		}
	}
	
	//when succeed done, will add into finished task list
	void finishedTask(List<Long> tids)
	{
		finished_task_num += tids.size();
		addTask(finishedtasks, tids);
	}
	//when fail, will add into fail task list
	void failedTaks(List<Long> tids)
	{
		failed_task_num += tids.size();
		addTask(failtasks, tids);
	}
	void failedTaks(OmsTask task)
	{
		failed_task_num += 1;
		failtasks.add(0, task);
	}
	void failedTaks(List<OmsTask> tasks, boolean nouse)
	{
		if(tasks!=null && tasks.size()>0)
		{
			failed_task_num += tasks.size();
			failtasks.addAll(0, tasks);
		}
	}
	
	private void addTask(List<OmsTask>tasks, List<Long>tids)
	{
		synchronized(tasks)
		{			
			for(int j=0;j<tids.size();j++)
			{
				for(int i=0;i<runningtasks.size();i++)
				{
					OmsTask item = runningtasks.get(i);
					if(item.taskID == tids.get(j))
					{
						Log.d(TAG, "add into failed list="+item);
						tasks.add(item);						
						break;
					}
				}				
			}
			
			//remove the repeat
			removeRepeatTask(tasks);
			
			//remove the			 
			while(tasks.size() > max_queunesize)
			{
				int last = tasks.size() -1;
				OmsTask task = tasks.get(last);
				tasks.remove(last);
				
				task.recycle();
				task = null;
			}			
		}	
		
		synchronized(runningtasks)
		{
		    removeTask(runningtasks, tids);
		}
	}
	
	void removeRepeatTask(List<OmsTask> list)
	{}	
	//remove task from queue
	void removeTask(List<OmsTask> list, List<Long> tids)
	{
		int count=0;
		synchronized(list)
		{
			for(int j=0;j<tids.size();j++)
			{
				for(int i=0;i<list.size();i++)
				{
					OmsTask item = list.get(i);					
					if(item.taskID == tids.get(j))
					{					
						list.remove(i);		
						count++;
						break;
					}
				}
			}
		}
		
		Log.d(TAG, "remove from runing task="+tids.size() + " removed="+count);
	}
	
	public static final int NEXT_TASK               =0;	
	public static final int EXIT_LOOP               = 200;
	public static final int TASK_DO_NOTHING         = 201;
	public static final int TASK_STOP               = 202;
	
	public class OmsHandler  extends Handler 
    {
        public OmsHandler(Looper loop)
        {
            super(loop);            
            Log.d(TAG, "new OmsHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case NEXT_TASK:
                {
                	this.removeMessages(NEXT_TASK);
                	
                	Log.d(TAG, "load NEXT_TASK");
                	if(status == STATUS.PAUSE )
                	{
                		//for check
                		Log.d(TAG, "you have pause the process, please resume it to work");
                		Message msd = handler.obtainMessage(NEXT_TASK);
                		handler.sendMessageDelayed(msd, 5*1000);
                		return;
                	}
                	
                	if(loginHelper.getPermanentSesstion() == null )
                	{
                		Log.d(TAG, "don't have session, please resume it to work");
                		return;
                	}
                	
                	List<OmsTask> nexttasks = msgQueue.next(timesforonefetch);
                	//do contact sync
                	Log.d(TAG, "get new task="+nexttasks.size());                	
                	processNewTask(nexttasks);
                	break;
                }
            	case EXIT_LOOP:
            	{
            		Log.d(TAG, "quit service");
            		if(mServiceLooper != null)
                    {
                        mServiceLooper.quit();
                        mServiceLooper = null;
                    }
            		break;
            	}      
            	case TASK_STOP:
            	{
            		doStopAllTask();
            		break;
            	}
            	case TASK_DO_NOTHING:
            	{
            		doNothing();
            		break;
            	}
            }
        }
    }
	
	public Message getHideMessage()
	{
		Message msg = handler.obtainMessage(TASK_DO_NOTHING);		
		return msg;
	}
	
	public Message getStopMessage()
	{
		Message msg = handler.obtainMessage(TASK_STOP);		
		return msg;
	}
	
	void doStopAllTask()
	{
		Log.d(TAG, "call stop task");		
	}
	
	void doNothing()
	{
		Log.d(TAG, "call hide UI");
	}
	
	private void processNewTask(List<OmsTask> tasks)
	{	
		List<OmsTask> cancel = new ArrayList<OmsTask>();
		List<OmsTask> lookups = new ArrayList<OmsTask>();
		List<OmsTask> eventsync = new ArrayList<OmsTask>();
		List<OmsTask> eventadds = new ArrayList<OmsTask>();
		List<OmsTask> contactSyncs = new ArrayList<OmsTask>();
		List<OmsTask> lookupall = new ArrayList<OmsTask>();	
		List<OmsTask> addasfriend = new ArrayList<OmsTask>();
		
		OmsServiceHelper.getAllocatedTask(cancel, tasks, lookups, eventsync, eventadds, contactSyncs, lookupall,addasfriend);
		int hasFalse=0;
		if(lookupall.size()>0)
		{
		    boolean ret = processLookupAllTasks(lookupall);
		    if(ret==false)
		    	hasFalse++;
		}
		
		if(lookups.size() > 0)
		{
		    boolean ret = processLookupTasks(lookups);
		    if(ret==false)
		    	hasFalse++;
		}
		
		if(eventsync.size() > 0)
		{
		    boolean ret = processEventSyncTasks(eventsync);
		    if(ret==false)
		    	hasFalse++;
		}
		
		if(eventadds.size() > 0)
		{
		    boolean ret = processEventAddTasks(eventadds);
		    if(ret==false)
		    	hasFalse++;
		}
		
		if(contactSyncs.size() > 0)
		{
		    boolean ret = processContactSyncTasks(contactSyncs);
		    if(ret==false)
		    	hasFalse++;
		}
		
		if(addasfriend.size()>0)
		{
		    boolean ret = processAddAsFriendTask(addasfriend);
		    if(ret = false)
		        hasFalse++;
		}
		
		//process cancel all
		if(cancel.size()> 0)
		    processCancelAllTask(cancel);	
		
		if(hasFalse > 0)
		{
			handler.obtainMessage(NEXT_TASK).sendToTarget();
		}
	}
	
	private void stopCurrentConnection()
	{
		stopLoading();
	}
	private boolean processCancelAllTask(List<OmsTask> cancel) 
	{
		Log.d(TAG, "enter cancel task");
		//stop all current running task
		stopCurrentConnection();
		
		//add the finished
		synchronized(finishedtasks)
		{			
			for(int i=0;i<cancel.size();i++)
			{
				OmsTask item = cancel.get(i);			
				finishedtasks.add(0, item);			
			}
		}		
		handler.obtainMessage(NEXT_TASK).sendToTarget();
		return true;
	}

	private static boolean inEventSync=false;
	boolean processEventSyncTasks(List<OmsTask> tasks)
	{
		return false;
	}
	
	boolean processAddAsFriendTask(List<OmsTask> addasfriend)
	{
	    if(asyncOms == null)
	    {
	        Log.d(TAG, "no invalid session for processAddAsFriendTask");
	        failedTaks(addasfriend,true);
	        return false;
	    }
	    Log.d(TAG,"processAddAsFriendTask");
	    if(addasfriend !=null && addasfriend.size()>0)
	    {
	        Log.d(TAG," get addasfriend tasks ");
	        addProcessTask(addasfriend);
	        for(OmsTask task : addasfriend )
	        {
	                     
                Long tid = task.taskID;
                Long fuid = task.id;
                       
                asyncOms.addAsFriendAsync(tid,fuid,new OmsServiceAdapter()
                {
                    @Override
                    public void addAsFriend(Long tid, boolean retvalue)
                    {
                        ArrayList<Long> temparray = new ArrayList<Long>();
                        temparray.add(tid);
                        
                        if(retvalue) 
                            finishedTask(temparray);    
                        else
                            failedTaks(temparray);
                        
                        temparray = null;
                        
                        handler.obtainMessage(NEXT_TASK).sendToTarget();
                    }
                    public void onException(FacebookException te, int method, Object[] args)
                    {
                        Log.d(TAG, "fail to process oms task id="+method + " ex="+te.getMessage()); 
                        List<Long> list = new ArrayList<Long>();
                        list.add((Long)args[0]);
                        failedTaks(list);                       
                        list = null;
                        
                        handler.obtainMessage(NEXT_TASK).sendToTarget();
                    }
                });             
	            }           
	            return true;            
	            
	    }
	    else
	    {
	        return false;
	    }
	 }
	
	boolean processEventAddTasks(List<OmsTask> tasks)
	{
		return false;
	}
	
	private static boolean inContactSync=false;
	boolean processContactSyncTasks(List<OmsTask> tasks)
	{		
		if(asyncOms == null)		
		{
			Log.d(TAG, "no invalid session="+this);
			failedTaks(tasks, true);
			return false;
		}
		Log.d(TAG, "processContactSyncTasks");	
		
		if(inContactSync == true)
		{
			Log.d(TAG, "I am in doing contact sync, please wait. will ignore the new contact sync");			
			return false;
		}		
			
		if(tasks != null && tasks.size() > 0)
    	{
			inContactSync = true;
    		Log.d(TAG, "get sync Facebook phonebook to Contact="+tasks.size());
    		List<OmsTask> task = new ArrayList<OmsTask>();
    		task.add(tasks.get(0));
    		addProcessTask(task);        
    		Long tid = tasks.get(0).taskID;
    		asyncOms.contactSync(tid,new OmsServiceAdapter()
    		{
    			@Override
    			public void contactSync(Long tid, List<PhoneBook> phonebooks)
    			{
    				Log.d(TAG, "after get all phonebook for contact sync to Contact database");
    				//removeTask(tid);
    				ArrayList<Long> temparray = new ArrayList<Long>();
    				temparray.add(tid);
    				finishedTask(temparray);
    				
                   // ContactHelper.syncFacebookContact(phonebooks);
                    inContactSync = false;
                    
    				handler.obtainMessage(NEXT_TASK).sendToTarget();
    			}
    			public void onException(FacebookException te, int method, Object[] args)
    			{
    				inContactSync = false;
    				Log.d(TAG, "fail to process oms task id="+method + " ex="+te.getMessage());	    				
    				List<Long> list = new ArrayList<Long>();
    				list.add((Long)args[0]);
    				failedTaks(list);				
    				handler.obtainMessage(NEXT_TASK).sendToTarget();
    			}
    		});
    		return true;
    	}
		else
		{
			return false;
		}
	}
	/*
	 * this is mainly for update contact facebook information, from Contact application
	 */
	public void processUpdateContactInfo(long fuid,long peopleid,boolean updatelogo, boolean updatebirthday,boolean updateemail,boolean updatecell)
	{
	    //add address to contact_methods
        ContactHelper.SyncData data = new ContactHelper.SyncData();	                 
        data.fuid           = fuid;
        data.peopleid       = (int)peopleid;
        data.updatelogo     = updatelogo;
        data.updatebirthday = updatebirthday;
        data.updateemail    = updateemail;
        data.updatecell     = updatecell;
        
	    ContactHelper.updateContactInfo(mContext, orm,asyncOms.fs , data,true, false);
	    data = null;
	}
	
	public class LookupAllThread extends Thread
	{
		public OmsTask task;
		public LookupAllThread()
		{
			super();
			this.setName("LookupAllThread"+this.getId());
		}
		
		public void run()
		{
			if(asyncOms == null)		
			{
				//TODO do we need resume the task?
				Log.d(TAG, "no invalid session="+this);
				failedTaks(task);				
				handler.obtainMessage(NEXT_TASK).sendToTarget();
				return;
			}
			
			//too long
			//new a thread to process	
			if(task!=null )
			{
				ArrayList<OmsTask> array = new ArrayList<OmsTask>();
	    		array.add(task);	    		
	    		
	    		// TODO loop for lookupall tasks	    		
	    		int totalcontacts = ContactHelper.getContactnumber(omsService.getContext());
	    		if(totalcontacts > 0)
	    		{
	    			addProcessTask(array);		    		
		    		ArrayList<Long> tids = new ArrayList<Long>();
		    		tids.add(task.taskID);
		    		int limit = 400;		
		    		int counttimes = totalcontacts/limit + totalcontacts%limit==0?0:1;
		    		for(int i=0;i<counttimes;i++)
		    		{	    			
		    			int offset = i;	    
		    			List<Long> pids = ContactHelper.getPeopleIds(omsService.getContext(), limit, offset);
		    			String entries = ContactHelper.createLookupEntries(omsService.getContext(),pids);
		        		Log.d(TAG,"entries is " + entries);
		        		asyncOms.phoneLookupAsync(tids,pids,entries,new OmsServiceAdapter()
		        		{
		        			public void phoneLookup(List<Long> tids,List<Long> pids, List<PhoneBook> phoneBookLookup)
		        			{
		        				//update the look up date
		        				lastlookupall = System.currentTimeMillis();
		        				
		        				finishedTask(tids);
		                        //tag Facebook in Contact database		                        
		                        ContactHelper.syncFacebookUserToContact(orm,asyncOms.fs,omsService.getContext(),phoneBookLookup);		                        
		        				handler.obtainMessage(NEXT_TASK).sendToTarget();
		        			}
		        			public void onException(FacebookException te, int method, Object[] args)
		        			{
		        				Log.d(TAG, "fail to process oms task id="+method + " ex="+te.getMessage());	    				
		        				failedTaks((List<Long>)args[0]);    				
		        				handler.obtainMessage(NEXT_TASK).sendToTarget();
		        			}
		        		});
		        		
		    		}
	    		}
	    		else
	    		{
	    			Log.d(TAG, "no available user, so just finish the lookup all task");
	    			finishedTask(array, true); 
	    			handler.obtainMessage(NEXT_TASK).sendToTarget();
	    		}
			}
		}
	}
	
	//TODO
	long lastlookupall=0;
	boolean processLookupAllTasks(List<OmsTask> tasks)
	{
		if(tasks == null || tasks.size() == 0)
		{
			return false;
		}
		Log.d(TAG, "processLookupAllTasks");
		//TODO, could user set the value?
		/*if(System.currentTimeMillis() - lastlookupall < 30*60*1000)
		{
			Log.d(TAG, "you can't do look up all twice in half an hour");
			return false;
		}*/
		LookupAllThread thread = new LookupAllThread();
		thread.task = tasks.get(0);
		thread.start();
		return true;
	}
	
	boolean processLookupTasks(List<OmsTask> lookuptasks)
	{
		if(asyncOms == null)		
		{
			//TODO do we need resume the task?
			Log.d(TAG, "no invalid session="+this);
			
			if(lookuptasks !=null && lookuptasks.size()>0){
				//TODO failedtasks();
			}
			
			return false;
		}
		Log.d(TAG, "processLookupTasks");	
		
		if(lookuptasks != null && lookuptasks.size() > 0)
    	{
    		Log.d(TAG, "get new lookup task="+lookuptasks.size());
    		
    		String entries= OmsServiceHelper.createLookupEntries(mContext, lookuptasks);  		
    		if(entries!=null && !"".equals(entries))
    		{
    			addProcessTask(lookuptasks);
    			
    			//remember the task ids
    			List<Long> tids = new ArrayList<Long>();
    			List<Long> peopleids = new ArrayList<Long>();
    			
    			for(int i=0;i<lookuptasks.size();i++)
    			{
    				//TODO fix repeat issue, same people id
    				tids.add(lookuptasks.get(i).taskID);
    				peopleids.add(lookuptasks.get(i).id);
    			}
    			
    			Log.d(TAG, "before phoneLookupAsync="+tids.size());
	    		asyncOms.phoneLookupAsync(tids,peopleids,entries,new OmsServiceAdapter()
	    		{
	    			public void phoneLookup(List<Long> tids, List<Long> peopleids, List<PhoneBook> phoneBookLookup)
	    			{
	    				finishedTask(tids);
                        //tag Facebook in Contact database
	    			    //ContactHelper.tagContactAsFacebook(omsService.getContext(), phoneBookLookup);
	    			    
	    			    //get extra phonebook info (username , pic ) and process Contact DB;
	    			    ContactHelper.syncFacebookUserToContact(orm,asyncOms.fs,omsService.getContext(),phoneBookLookup);
	    			    
	    				handler.obtainMessage(NEXT_TASK).sendToTarget();
	    			}
	    			public void onException(FacebookException te, int method, Object[] args)
	    			{
	    				Log.d(TAG, "fail to process oms task id="+method + " ex="+te.getMessage());	    				
	    				failedTaks((List<Long>)args[0]);    				
	    				handler.obtainMessage(NEXT_TASK).sendToTarget();
	    			}
	    		});
	    		return true;
    	   }
    	   else
    	   {
    		   Log.d(TAG, "no available user, so just finish the lookup task");
    		   finishedTask(lookuptasks, true);
    		   return false;
    	   }
    	}
		else
		{
			return false;
		}
	}

	
	public HashMap<Integer, HttpURLConnection> connections = new HashMap<Integer, HttpURLConnection>();	
    public void stopLoading()
    {
        if(DEBUG)
        Log.d("********task-network", "call stopLoading="+connections.size()+" this="+this);
        synchronized(connections)
        {
            Set<Integer> set = connections.keySet();
            Iterator<Integer> it = set.iterator();
            while(it.hasNext())
            {
                int UID = it.next();
                HttpURLConnection con = connections.get(UID);
                if(DEBUG)
                Log.d(TAG, "stop UID="+UID+" connection="+con.getURL());
                con.setConnectTimeout(10);
                con.setReadTimeout(10); 
            }            
            connections.clear();            
        }
    }
    
    public void addHttpConnection(int UID, HttpURLConnection con)
    {
        if(DEBUG)
        Log.d("********task-network", "add UID="+UID+" connection="+con.getURL() +" this="+this);
        synchronized(connections)
        {
            connections.put(UID, con);            
        }
    }
    public void releaseHttpConnection(int UID)
    {
        if(DEBUG)
        Log.d("********task-network", "remove connection="+UID+" this="+this);
        synchronized(connections)
        {
            connections.remove(UID);            
        }
    }

	public void addRunnable(Integer uid, Runnable run) 
	{
		
	}

	public void titleUpdateAfterNetwork() {
				
	}

	public void updateProgress(int pos, int count) {
				
	}
}
