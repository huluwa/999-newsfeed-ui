package com.msocial.nofree.ui.adapter;

import java.util.Date;

import oms.sns.service.facebook.model.Group;
import android.os.Parcel;
import android.os.Parcelable;

public class GroupParcel implements Parcelable 
{
    public Group group;
	public int describeContents() 
	{	
		return 0;
	}
	
	 public GroupParcel(Group groups)
     {
		 group = groups;
     }
	 public GroupParcel(Parcel in)
     {
         readFromParcel(in);
     }
	 
     public void readFromParcel(Parcel in)
     {
    	 group = new Group();
    	 group.creator = in.readLong();
    	 group.description = in.readString();
    	 group.gid         = in.readLong();    	 
    	 group.group_subtype = in.readString();
    	 group.group_typ     = in.readString();
    	 group.name          = in.readString();
    	 group.nid           = in.readLong();
    	 group.office        = in.readString();
    	 group.pic           = in.readString();
    	 group.pic_big       = in.readString();
    	 group.pic_samll     = in.readString();
    	 group.update_time   = new Date(in.readLong());
    	 group.website       = in.readString();    	 
     }
     
	 public void writeToParcel(Parcel out, int arg1) 
	 {
		 out.writeLong(group.creator);
		 out.writeString(group.description==null?"":group.description);
		 out.writeLong(group.gid);
		 out.writeString(group.group_subtype==null?"":group.group_subtype);
		 out.writeString(group.group_typ==null?"":group.group_typ);
		 out.writeString(group.name==null?"":group.name);
		 out.writeLong(group.nid);
		 out.writeString(group.office==null?"":group.office);
		 out.writeString(group.pic==null?"":group.pic);
		 out.writeString(group.pic_big==null?"":group.pic_big);
		 out.writeString(group.pic_samll==null?"":group.pic_samll);
		 out.writeLong(group.update_time.getTime());
		 out.writeString(group.website==null?"":group.website);
	 }
	 
	 public static final Parcelable.Creator<GroupParcel> CREATOR
		     = new Parcelable.Creator<GroupParcel>() 
		 {
			 public GroupParcel createFromParcel(Parcel in) 
			 {
				 return new GroupParcel(in);
		     }
	
	     public GroupParcel[] newArray(int size) 
	     {
	         return new GroupParcel[size];
	     }
     };
}
