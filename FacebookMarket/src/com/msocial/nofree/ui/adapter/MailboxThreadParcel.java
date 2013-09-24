package com.msocial.nofree.ui.adapter;

import java.util.ArrayList;
import java.util.Date;

import oms.sns.service.facebook.model.MailboxThread;

import android.os.Parcel;
import android.os.Parcelable;

public class MailboxThreadParcel  implements Parcelable {

	public MailboxThread mthread;

	public int describeContents() 
	{		
		return 0;
	}
	
	 public MailboxThreadParcel(MailboxThread mt)
     {
		 mthread = mt;
     }
	 public MailboxThreadParcel(Parcel in)
     {
         readFromParcel(in);
     }
	 
     public void readFromParcel(Parcel in)
     {
    	 mthread = new MailboxThread();
    	 mthread.hasattachment = in.readInt();
    	 mthread.lastupdate    = new Date(in.readLong());
    	 mthread.msgcount      = in.readInt();
    	 mthread.originator    = in.readLong();
    	 mthread.parent_tid    = in.readLong();
    	 int count = in.readInt();
    	 mthread.participants = new ArrayList<Long>();
    	 for(int i=0;i<count;i++)
    	 {
    		 mthread.participants.add(in.readLong());
    	 }
    	 
    	 count = in.readInt();
    	 mthread.recentauthors = new ArrayList<Long>();
    	 for(int i=0;i<count;i++)
    	 {
    		 mthread.recentauthors.add(in.readLong());
    	 }
    	 
    	 mthread.subject = in.readString();
    	 mthread.threadid = in.readLong();
    	 mthread.unreadcount = in.readInt();    	 
     }
     
	 public void writeToParcel(Parcel out, int arg1) 
	 {
		 out.writeInt(mthread.hasattachment);		 
		 out.writeLong(mthread.lastupdate.getTime());		 
		 out.writeInt(mthread.msgcount);
		 out.writeLong(mthread.originator);
		 out.writeLong(mthread.parent_tid);
		 out.writeInt(mthread.participants.size());
		 for(int i=0;i<mthread.participants.size();i++)
		 {
			 out.writeLong(mthread.participants.get(i));
		 }
		 out.writeInt(mthread.recentauthors.size());
		 for(int i=0;i<mthread.recentauthors.size();i++)
		 {
			 out.writeLong(mthread.recentauthors.get(i));
		 }
		 
		 out.writeString(mthread.subject==null?"":mthread.subject);
		 out.writeLong(mthread.threadid);
		 out.writeInt(mthread.unreadcount);
	 }
	 
	 public static final Parcelable.Creator<MailboxThreadParcel> CREATOR
		     = new Parcelable.Creator<MailboxThreadParcel>() 
		 {
			 public MailboxThreadParcel createFromParcel(Parcel in) 
			 {
				 return new MailboxThreadParcel(in);
		     }
	
	     public MailboxThreadParcel[] newArray(int size) 
	     {
	         return new MailboxThreadParcel[size];
	     }
     };
}
