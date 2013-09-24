package oms.sns.service.facebook.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageThreadInfo implements Comparable<Object>{  
    
    public int         _id;
    public long        thread_id;
    public int         folder_id;
    public String      subject;
    public List<Long>  recipients;
    public long        updated_time;//just for xml parse and save the time
    public long        update_update_time;
    public long        inbox_updated_time;
    public long        outbox_updated_time;
    public String      parent_message_id;
    public long        parent_thread_id;
    public int         message_count;
    public String      snippet;
    public long        snippet_author;
    public long      object_id;
    public int         unread;
    public List<MailboxMessage> messages;
    
    public boolean  isinbox;
    public boolean  isoutbox;
    
    public MailboxThread toMailboxThread()
    {
        MailboxThread mailbox = new MailboxThread();
        mailbox.lastupdate = new Date(updated_time);
        mailbox.msgcount   = message_count;
        mailbox.originator = snippet_author;
        mailbox.parent_tid = parent_thread_id;
        mailbox.participants = new ArrayList<Long>();
        if(recipients != null)
        {
            for(int i=0 ; i<recipients.size();i++)
            {
                mailbox.participants.add(recipients.get(i));
            }
        }
        mailbox.participants = recipients;
        mailbox.subject      = subject;
        mailbox.threadid     = thread_id;
        mailbox.unreadcount  = unread;
        mailbox.text         =  snippet;
        mailbox.isinbox      = isinbox;
        mailbox.isoutbox     = isoutbox;
        return mailbox;
    }
    public static class Field
    {
        public static final String THREAD_ID        = "thread_id";
        public static final String folder_id        = "folder_id";
        public static final String SUBJECT          = "subject"; 
        public static final String RECIPIENTS       = "recipients"; 
        public static final String UPDATED_TIME     ="updated_time";
        public static final String PARENT_THREAD_ID = "parent_thread_id";
        public static final String PARENT_MESSAGE_ID="parent_message_id";
        public static final String MESSAGE_COUNT    ="message_count";
        public static final String SNIPPET          = "snippet";
        public static final String SNIPPET_AUTHOR   = "snippet_author";
        public static final String OBJECT_ID        = "object_id";
        public static final String UNREAD           = "unread";
        public static final String MESSAGES         = "messages";
       
    }
    
    public int compareTo(Object another) 
    {       
        if(MessageThreadInfo.class.isInstance(another))
        {
            long anotherUpdatetime = ((MessageThreadInfo)another).updated_time;
            if(updated_time > anotherUpdatetime )
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

    public void despose() {  
    	  _id       = -1;
    	  thread_id = -1;
    	  subject   = null;
    	  if(recipients != null)
    	  {
    	      recipients.clear();
    	      recipients = null;
    	  }
    	  updated_time = -1;
    	  parent_message_id = null;
    	  parent_thread_id = -1;
    	  message_count    = -1;
    	  snippet          = null;
    	  snippet_author   = -1;
    	  object_id        = -1;
    	  unread           = 0;
    	  if(messages != null)
    	  {
    		  for(MailboxMessage msg:messages)
    		  {
    			  msg.despose();
    			  msg = null;
    		  }
    		  messages.clear();
    		  messages = null;
    	  }    	    
    	  isinbox = false;
    	  isoutbox = false;
    }
}
