package oms.sns.facebook.ui.adapter;

import java.util.ArrayList;
import oms.sns.service.facebook.model.MessageThreadInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class MessageThreadInfoParcel implements Parcelable 
{
    public MessageThreadInfo mthread;

    public int describeContents() 
    {       
        return 0;
    }    
    public MessageThreadInfoParcel(Parcel in)
    {
         readFromParcel(in);
    }
     
    public MessageThreadInfoParcel(MessageThreadInfo mt) {
         mthread = mt;
    }

    public void readFromParcel(Parcel in)
    {
         mthread = new MessageThreadInfo();
         mthread.message_count = in.readInt();
         mthread.object_id = in.readLong();
         mthread.parent_message_id = in.readString();
         mthread.parent_thread_id = in.readLong();
         int count = in.readInt();
         mthread.recipients = new ArrayList<Long>();
         for(int i = 0 ; i < count ; i++)
         {
            mthread.recipients.add(in.readLong());
         }
         mthread.snippet = in.readString();
         mthread.snippet_author = in.readLong();
         mthread.subject = in.readString();
         mthread.thread_id = in.readLong();
         mthread.unread = in.readInt();
         mthread.updated_time = in.readLong();  
         mthread.inbox_updated_time = in.readLong();
         mthread.outbox_updated_time = in.readLong();
         mthread.update_update_time = in.readLong();
     }
     private boolean isEmpty(String str)
     {
         return str == null || str.length() ==0;
     }
     public void writeToParcel(Parcel out, int arg1) 
     {
         out.writeInt(mthread.message_count);                 
         out.writeLong(mthread.object_id);        
         out.writeString(mthread.parent_message_id);
         out.writeLong(mthread.parent_thread_id);
         out.writeInt(mthread.recipients.size());
         for(int i=0;i<mthread.recipients.size();i++)
         {
             out.writeLong(mthread.recipients.get(i));
         }
         out.writeString( isEmpty(mthread.snippet)==true?"":mthread.snippet);
         out.writeLong(mthread.snippet_author);
         out.writeString(isEmpty(mthread.subject)==true?"":mthread.subject);
         out.writeLong(mthread.thread_id);
         out.writeInt(mthread.unread);
         out.writeLong(mthread.updated_time);
         out.writeLong(mthread.inbox_updated_time);
         out.writeLong(mthread.outbox_updated_time);
         out.writeLong(mthread.update_update_time);
     }
     
     public static final Parcelable.Creator<MessageThreadInfoParcel> CREATOR
             = new Parcelable.Creator<MessageThreadInfoParcel>() 
         {
             public MessageThreadInfoParcel createFromParcel(Parcel in) 
             {
                 return new MessageThreadInfoParcel(in);
             }
    
         public MessageThreadInfoParcel[] newArray(int size) 
         {
             return new MessageThreadInfoParcel[size];
         }
     };
}
