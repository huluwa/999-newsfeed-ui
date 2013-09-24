package oms.sns.service.facebook.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Element;

public class MailboxThread implements Comparable<Object>{
	public int  _id;
	public long threadid;
	public long parent_tid;
	public long    originator;
	public List<Long>  participants;
	public List<Long>  recentauthors;
	public int      msgcount;
	public String   subject;
	public int      unreadcount;
	public Date     lastupdate;
	public int      hasattachment;
	public boolean  isinbox;
	public boolean  isoutbox;
	//added
	public String text;
	
	public MessageThreadInfo toMessageThreadInfo()
	{
	    MessageThreadInfo threadinfo = new MessageThreadInfo();
        threadinfo.message_count    = msgcount;
        threadinfo.parent_thread_id = parent_tid;
        
        threadinfo.recipients = new ArrayList<Long>();
        if(participants != null)
        {
            for(int i=0 ; i<participants.size();i++)
            {
                threadinfo.recipients.add(participants.get(i));
            }
        }
        
        threadinfo.recipients       = participants;
        threadinfo.snippet          = text;
        threadinfo.snippet_author   = originator;
        threadinfo.subject          = subject;
        threadinfo.thread_id        = threadid;
        threadinfo.unread           = unreadcount;
        threadinfo.updated_time     = lastupdate.getTime();
        
        threadinfo.isinbox           = isinbox;
        threadinfo.isoutbox          = isoutbox;
        
        return threadinfo;
	}
	
	
	public Long getThreadid() {
		return threadid;
	}

	public void setThreadid(Long threadid) {
		this.threadid = threadid;
	}

	public Long getParent_tid() {
		return parent_tid;
	}

	public void setParent_tid(Long parent_tid) {
		this.parent_tid = parent_tid;
	}

	public long getOriginator() {
		return originator;
	}

	public void setOriginator(Long originator) {
		this.originator = originator;
	}


	public List<Long> getParticipants() {
		return participants;
	}

	public void setParticipants(List<Long> participants) {
		this.participants = participants;
	}

	public List<Long> getRecentauthors() {
		return recentauthors;
	}

	public void setRecentauthors(List<Long> recentauthors) {
		this.recentauthors = recentauthors;
	}

	public Integer getMsgcount() {
		return msgcount;
	}

	public void setMsgcount(Integer msgcount) {
		this.msgcount = msgcount;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Integer getUnreadcount() {
		return unreadcount;
	}

	public void setUnreadcount(Integer unreadcount) {
		this.unreadcount = unreadcount;
	}

	public Date getLastupdate() {
		return lastupdate;
	}
	
	public void setLastupdate(Date lastupdate) {
		this.lastupdate = lastupdate;
	}

	public Integer getHasattachment() {
		return hasattachment;
	}

	public void setHasattachment(Integer hasattachment) {
		this.hasattachment = hasattachment;
	}

	public enum Field
	{
		TID, PARENT_TID, 
		ORIGINATOR, 
		PARTICIPANTS,
		RECENT_AUTHORS,
		SUBJECT,
		MSG_COUNT,
		UNREAD_COUNT,
		LAST_UPDATE,
	    HAS_ATTACHMENT;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}

	public int compareTo(Object another) 
	{		
		if(MailboxThread.class.isInstance(another))
		{
			Date anDate = ((MailboxThread)another).lastupdate;
			if(lastupdate.getTime() > anDate.getTime())
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
		
		return 0;
	}

}
