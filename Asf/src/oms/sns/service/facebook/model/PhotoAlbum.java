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


/**
 * @author Gino Miceli
 * @author Mino Togna
 */
public class PhotoAlbum implements Parcelable, Comparable 
{  
    public long id;
	public String aid;
	public String cover_pid;
	public long owner;
	public String name;
	public Date created;
	public Date modified;
	public String description;
	public String location;
	public String cover_src_url;
	public int size;
	public String link;
	public String visible;
	public String modified_major;
 
	public static class Field 
	{
		public static final String AID       ="aid";
		public static final String COVER_PID = "cover_pid";
		public static final String OWNER     = "owner";
		public static final String NAME      = "name";
		public static final String CREATED   = "created";
		public static final String MODIFIED  = "modified";
		public static final String DESCRIPTION  ="description";
	    public static final String LOCATION     = "location";
	    public static final String SIZE         = "size";
	    public static final String LINK         = "link";
	}
	
	public String toString()
	{
	    return name;
	}

	public int describeContents() {		
		return 0;
	}

	public PhotoAlbum()
    {
		
    }
	
	public PhotoAlbum(Parcel in)
    {
        readFromParcel(in);
    }
	 
    public void readFromParcel(Parcel in)
    {
    	 id        = in.readLong();
    	 aid       = in.readString();
    	 cover_pid = in.readString();
    	 owner     = in.readLong();
    	 name      = in.readString();
    	 created   = new Date(in.readLong());
    	 modified  = new Date(in.readLong());
    	 description = in.readString();
    	 location    = in.readString();
    	 cover_src_url = in.readString();
    	 size          = in.readInt();
    	 link          = in.readString();    
    	 visible       = in.readString();
    	 modified_major = in.readString();
    }
	public void writeToParcel(Parcel out, int flags) 
	{	
		 out.writeLong(id);
		 out.writeString(aid==null?"":aid);		 
		 out.writeString(cover_pid==null?"":cover_pid);
		 out.writeLong(owner);
		 
		 out.writeString(name==null?"":name);
		 out.writeLong(created!=null?created.getTime():0L);
		 out.writeLong(modified!=null?modified.getTime():0L);
		 
		 out.writeString(description==null?"":description);		 
		 out.writeString(location==null?"":location);		 
		 out.writeString(cover_src_url==null?"":cover_src_url);		 
		 out.writeInt(size);		 
		 out.writeString(link==null?"":link);
		 out.writeString(visible==null?"everyover":visible);
		 out.writeString(modified_major==null?"":modified_major);
		 
	}
	
	public static final Parcelable.Creator<PhotoAlbum> CREATOR
    = new Parcelable.Creator<PhotoAlbum>() 
	{
		 public PhotoAlbum createFromParcel(Parcel in) 
		 {
			 return new PhotoAlbum(in);
	    }
	
		public PhotoAlbum[] newArray(int size) 
		{
		    return new PhotoAlbum[size];
		}
	};

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public int compareTo(Object another) {
		
		if(PhotoAlbum.class.isInstance(another))
		{
			long anDate = ((PhotoAlbum)another).created.getTime();
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
}
