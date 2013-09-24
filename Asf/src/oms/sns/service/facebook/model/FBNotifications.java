package oms.sns.service.facebook.model;

import java.util.ArrayList;
import java.util.List;

public class FBNotifications 
{
	public static class NotifyType
	{
		public final static int Pokes         =0;		 
		public final static int EventInvites = 1;
		public final static int Message      = 2;
		public final static int NONE         = 3;
		public final static int Shares       = 4;
		public final static int FriendRequests=5;
		public final static int GroupInvites = 6;
		
	};	
	
	public Messages msg;
	public Pokes    poke;
	public Shares   share;
	public FriendRequests frdRequest;
	
	public GroupInvites   grdInvite;
	public EventInvites   entInvite;
	
	public FBNotifications()
	{
		msg        = new Messages();
		poke       = new Pokes();
		share      = new Shares();
		frdRequest = new FriendRequests();
		grdInvite  = new GroupInvites();
		entInvite  = new EventInvites();
	}
	public int getTypes()
	{		
		int size=0;
        if(msg != null) size++;
        if(poke != null)size++;
        if(share != null)size++;
        //if(frdRequest != null)size++;
        //if(grdInvite != null)size++;
        if(entInvite != null)size++;
        return size;				
	}
	
	public NotifyBase get(int type) 
	{	
		switch(type)
		{
			case NotifyType.Message:
			{
				return msg;
			}
			case NotifyType.Pokes:
			{
				return poke;
			}			
			case NotifyType.Shares:
			{
				return share;				
			}
			case NotifyType.FriendRequests:
			{
				return frdRequest;
			}
			case NotifyType.GroupInvites:
			{
				return grdInvite;
			}
			case NotifyType.EventInvites:
			{
				return entInvite;
			}
		}
		return null;
	}
	
	
	//content class define
	public class NotifyBase
	{	
		public int type;
	}
	
	public class Messages extends NotifyBase
	{
		public int  unread;
		public long most_recent;
		
		public Messages()
		{
			type = NotifyType.Message;
		}
	}
	
	public class Pokes extends NotifyBase
	{
		public int unread;
		public long most_recent;
		
		public Pokes()
		{
			type = NotifyType.Pokes;
		}
	}
	
	public class Shares extends NotifyBase
	{
		public int  unread;
		public long most_recent;
		
		public Shares()
		{
			type = NotifyType.Shares;
		}
	}
	
	public class FriendRequests extends NotifyBase
	{
		public List<Long> uids;
		public FriendRequests()
		{
			type = NotifyType.FriendRequests;
			uids = new ArrayList<Long>();
		}
		public void dispose() {
			type = 0;
			uids.clear();
			uids = null;
		}
	}
	
	public class GroupInvites extends NotifyBase
	{
		public List<Long> uids;
		
		public GroupInvites()
		{
			type = NotifyType.GroupInvites;
			uids = new ArrayList<Long>();
		}

		public void dispose() 
		{
			type = 0;
			uids.clear();
			uids = null;
		}
	}
	
	public class EventInvites extends NotifyBase
	{
		public List<Long> uids;
		
		public EventInvites()
		{
			type = NotifyType.EventInvites;
			uids = new ArrayList<Long>();
		}

		public void dispose() 
		{
			uids.clear();
			uids = null;
			type = 0;
		}
	}
	
	public static int getPos(int pos)
	{   
		/*
        if(pos==0)     return NotifyType.Pokes;       
        else if(pos==1)return NotifyType.FriendRequests;
        else if(pos==2)return NotifyType.GroupInvites;
        else if(pos==3)return NotifyType.EventInvites;
        else if(pos==4)     return NotifyType.Message;
        else return NotifyType.NONE;
        */
	    
        if(pos==0)     return NotifyType.Message;
		else if(pos==1)return NotifyType.Pokes;
		else if(pos==2)return NotifyType.Shares;		
		else if(pos==3)return NotifyType.EventInvites;
		else if(pos==4)return NotifyType.FriendRequests;
		else if(pos==5)return NotifyType.GroupInvites;
		else return NotifyType.NONE;		
	}
	
	public void dispose()
	{
		msg = null;
		poke = null;
		share = null;
		
		frdRequest.dispose();		
		frdRequest = null;
		
		grdInvite.dispose();
		grdInvite = null;
		
		entInvite.dispose();
		entInvite = null;
	}
}
