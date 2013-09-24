/*
 *  fb4j: Java API for Facebook
 *  Copyright (C) 2007-2008 Biagio Miceli Jr, Cosimo Togna
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Full license may be found in LICENSE.txt or downloaded from 
 *  <http://www.gnu.org/licenses/>.  fb4j documentation, updates and other 
 *  info can be found at <http://fb4j.sourceforge.net/>
 *
 *  @version $Id$
 */
package oms.sns.service.facebook.model;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;


public class Photo implements Comparable , Parcelable
{
    //facebook sometime return 100000025376015_49298 as pid, which can be parsed as Long, so use string
	//
	public String pid;
	public String aid;
	public long owner;
	public String src_small;
	public String src_big;
	public String src;
	public String link;
	public String caption;
	public Date created;
	public String toString()
	{
	    return "pid="+pid +
	    "\naid="+aid+
	    "\nowner="+owner+
	    "\nlink="+link+
	    "\ncaption="+caption;
	}
	public String getPid() {
		return pid;
	}
	
	public void setPid(String pid) {
		this.pid = pid;
	}
	
	public String getAid() {
		return aid;
	}
	
	public void setAid(String aid) {
		this.aid = aid;
	}
	
	public Long getOwner() {
		return owner;
	}
	
	public void setOwner(Long owner) {
		this.owner = owner;
	}
	
	public String getSrc_small() {
		return src_small;
	}
	
	public void setSrc_small(String src_small) {
		this.src_small = src_small;
	}
	
	public String getSrc_big() {
		return src_big;
	}
	
	public void setSrc_big(String src_big) {
		this.src_big = src_big;
	}
	
	public String getSrc() {
		return src;
	}
	
	public void setSrc(String src) {
		this.src = src;
	}
	
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	public String getCaption() {
		return caption;
	}
	
	public void setCaption(String caption) {
		this.caption = caption;
	}
	
	public Date getCreated() {
		return created;
	}
	
	public void setCreated(Date created) {
		this.created = created;
	}
	
	public static class Field 
	{
		public static final String PID       ="pid";
		public static final String AID       ="aid";		
		public static final String OWNER     = "owner";
		public static final String SRC_SMALL = "src_small";
		public static final String SRC_BIG   = "src_big";
		public static final String SRC       = "src";
		public static final String LINK      = "link";
		public static final String CAPTION   = "caption";
		public static final String CREATED   = "created";		
	}

	public void dispose() {
		pid       = null;
		aid       = null;
		owner     = 0;
		src_small = null;
		src_big   = null;
		src       = null;
		link      = null;
		caption   = null;
		created   = null;		
	}

	public int compareTo(Object another) 
	{		
		if(Photo.class.isInstance(another))
		{
			long anDate = ((Photo)another).created.getTime();
			if(created.getTime() > anDate)
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
	
	public Photo()
	{
		
	}
	public Photo(Parcel in)
    {
        readFromParcel(in);
    }
	 
    public void readFromParcel(Parcel in)
    {
    	 pid       = in.readString();
    	 aid       = in.readString();
    	 owner     = in.readLong();
    	 src_small = in.readString();
    	 src_big   = in.readString();
    	 src       = in.readString();
    	 link      = in.readString();
    	 caption   = in.readString();
    	 created  = new Date(in.readLong());    	 
    }
	public void writeToParcel(Parcel out, int flags) 
	{	
		 out.writeString(pid==null?"":pid);		 
		 out.writeString(aid==null?"":aid);
		 out.writeLong(owner);
		 
		 out.writeString(src_small==null?"":src_small);
		 out.writeString(src_big  ==null?"":src_big);
		 out.writeString(src      ==null?"":src);
		 out.writeString(link     ==null?"":link);
		 out.writeString(caption  ==null?"":caption);
		 out.writeLong(created ==null?0:created.getTime());
	}
	
	public static final Parcelable.Creator<Photo> CREATOR
    = new Parcelable.Creator<Photo>() 
	{
		 public Photo createFromParcel(Parcel in) 
		 {
			 return new Photo(in);
	    }
	
		public Photo[] newArray(int size) 
		{
		    return new Photo[size];
		}
	};

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
}
