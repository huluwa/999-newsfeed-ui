package com.msocial.facebook.service.dell;

public class OmsTask 
{
	private static  TaskListener taskListener;
	public static void setTaskListener(TaskListener lis){taskListener = lis;}
	public interface TaskListener
	{
		public void onStart(OmsTask task);
		public void onStop(OmsTask task);
		public void onResume(OmsTask task);
		public void onFinished(OmsTask task);		
	}
	
	public static final int EventSyncID  = -200;
	public static final int ContactSyncID= -100;
	public static final int LOOKUPALLID  = -300;
	OmsTask next;
	
	public long    when;
	public long    dispatchtime;	
	public boolean processed;	
	
	
	//-100 for contact sync, -200 for envent sync
	public long     id;       //event, event id, (contact id, lookup)
	public int     action;   //event, contact	
	public long  taskID =0;
	
	static long  staskID =0;
	public static long  INVALIDID=-1;
	public OmsTask()
	{
		staskID++;
		taskID = staskID;
		when = System.currentTimeMillis();
	}
	
	public OmsTask(int action, long id, long taskid)
	{
		this.action = action;
		this.id = id;
		this.taskID = taskid;
	}
	
	public void process(){}
	

	public static class ACTION
	{
		public final static int EVENT_ADD           = 0;
		public final static int EVENT_SYNC          = 1;
		public final static int CONTACT_LOOK_UP     = 2;
		public final static int CONTACT_LOOK_ALL    = 3;
		public final static int CONACT_SYNC         = 4;
		public final static int CANCEL_ALL          = 5;
		public final static int ADD_AS_FRIEND       = 6;
				
		public static String getString(int type)
		{
			switch(type)
			{
			    case EVENT_ADD:
				    return "EVENT_ADD";
			    case EVENT_SYNC:
					return "EVENT_SYNC";
			    case CONTACT_LOOK_UP:
					return "CONTACT_LOOK_UP";
			    case CONACT_SYNC:
					return "CONACT_SYNC";
			    case CANCEL_ALL:
					return "CANCEL_ALL";	
			    case CONTACT_LOOK_ALL:
			    	return "CONTACT_LOOK_ALL";
			    case ADD_AS_FRIEND:
			        return "ADD_AS_FRIEND";
				default:
					return "";
			}
		}
	};
	
	
	public void onStart()
	{
		if(taskListener != null)
		{
			taskListener.onStart(this);
		}
	}
	public void onStop()
	{
		if(taskListener != null)
		{
			taskListener.onStop(this);
		}
	}
	public void onResume()
	{
		if(taskListener != null)
		{
			taskListener.onResume(this);
		}
	}
	public void onFinished()
	{
		if(taskListener != null)
		{
			taskListener.onFinished(this);
		}
	}
	
	public String toString()
	{
		return " id="+id+
		       " action="+ACTION.getString(action)+
		       " taskID="+taskID;
	}
	
	
	static public class LookupTask extends OmsTask{
		//content region
		
		public LookupTask()
		{
			super();	
			this.action = ACTION.CONTACT_LOOK_UP;
		}
	}
	
	static public class ContactSyncTask extends OmsTask{
		//content region
		
		public ContactSyncTask()
		{
			super();	
			this.action = ACTION.CONACT_SYNC;
			this.id = -100;
		}
	}
	
	static public class CancelTask extends OmsTask{
		//content region
		
		public CancelTask()
		{
			super();	
			this.action = ACTION.CANCEL_ALL;
			this.id = -100;
		}
	}
	
	static public class AddAsFriendTask extends OmsTask{
	    public AddAsFriendTask(long fuid)
	    {
	        super();
	        this.action = ACTION.ADD_AS_FRIEND;
	        this.id =  fuid;
	    }
	}	
	static public class EventAddTask extends OmsTask{
		//content region
		public int subcategoryid;
		public int categoryid;
		
		public EventAddTask()
		{
			super();	
			this.action = ACTION.EVENT_ADD;
			id = -200;
		}
	}
	
	static public class EventSyncTask extends OmsTask{
		//content region
		
		public EventSyncTask()
		{
			super();	
			this.action = ACTION.EVENT_SYNC;
		}
	}

	public void recycle() 
	{
		next = null;		
		when =0;
		dispatchtime=0;		
		id=0;		
	}
}
