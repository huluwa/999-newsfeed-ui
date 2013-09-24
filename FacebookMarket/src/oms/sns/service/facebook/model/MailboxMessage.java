package oms.sns.service.facebook.model;

import java.util.Date;
import android.os.Parcel;
import android.os.Parcelable;


public class MailboxMessage  implements Parcelable, Comparable<Object>
{
	public int     _id;
    public long    threadid;    
    public String  mid;
    public long    author;
    public Date    timesent;
    public String  body;
    public int     hasattachment;    
    public boolean synced;
	   
    public static class Field 
	{
        //changed to new message table items
        public static final String TID            = "thread_id";
        public static final String MID            = "message_id";
        public static final String AUTHOR         = "author_id";
        public static final String TIME_SENT      = "created_time";
        public static final String BODY           = "body";
        public static final String HAS_ATTACHMENT = "attachment";		
	}

    
    public int compareTo(Object another) 
    {       
        if(MailboxMessage.class.isInstance(another))
        {
            long anotherUpdatetime = ((MailboxMessage)another).timesent.getTime();
            if(timesent.getTime() > anotherUpdatetime )
            {
                return 1;
            }
            else
            {
                return -1;
            }
        }        
        return 0;
    }
    
    public int describeContents() 
    {       
        return 0;
    }    
    public MailboxMessage()
    {
    }
    public MailboxMessage(Parcel in)
    {
         readFromParcel(in);
    }
     
    public void readFromParcel(Parcel in)
    {
        
        threadid = in.readLong();
        mid = in.readString();
        author   = in.readLong();
        timesent = new Date(in.readLong());
        body = in.readString();
        hasattachment = in.readInt();
        synced        = in.readInt()==0?false:true;
        
     }
     private boolean isEmpty(String str)
     {
         return str == null || str.length() ==0;
     }
     public void writeToParcel(Parcel out, int arg1) 
     {
         out.writeLong(threadid);                 
         out.writeString(isEmpty(mid) ==true?"":mid);        
         out.writeLong(author);
         if(timesent != null)
         {
             out.writeLong(timesent.getTime());
         }
         else
         {
             out.writeLong(0);
         }         
         out.writeString(isEmpty(body)?"":body);        
         out.writeInt(hasattachment);
         out.writeInt(synced==true?1:0);
     }
     
     public static final Parcelable.Creator<MailboxMessage> CREATOR
             = new Parcelable.Creator<MailboxMessage>() 
         {
             public MailboxMessage createFromParcel(Parcel in) 
             {
                 return new MailboxMessage(in);
             }
    
         public MailboxMessage[] newArray(int size) 
         {
             return new MailboxMessage[size];
         }
     };

	public void despose() {
		_id      = -1;
	    threadid = -1;
	    mid      = null;
	    author   = -1;	    
	    timesent = null;
	    body     = null;
	    hasattachment = -1;    
	    synced        = false;		
	}
}
