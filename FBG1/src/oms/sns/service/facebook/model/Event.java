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

import oms.sns.service.facebook.client.FacebookField;
import oms.sns.service.facebook.client.FacebookObject;

/**
 * 
 * @author Mino Togna
 * @author Gino Miceli
 * 
 */
public class Event implements Comparable<Event>
{
   public static String ATTENDING    = "attending";
   public static String UNSURE      = "unsure";
   public static String DECLINED    = "declined";
   public static String NOT_REPLIED = "not_replied";
   public long eid;
   public String name;
   public String tagline;
   public long nid;
   public String pic;
   public String pic_big;
   public String pic_small;
   public String host;
   public String description;
   public String event_type;
   public String event_sbytype;
   //public EventLocation eventlocation;
   public Date start_time;
   public Date end_time;
   public long creator;
   public Date update_time;
   public String location;
   
   public String venue;
   public String rsvp_status;
   public boolean synced;
   
   public long ceid;
   
   public boolean fornotification;
   
   
	public Long getCeid() {
	return ceid;
}

public void setCeid(Long ceid) {
	this.ceid = ceid;
}

	public boolean isSynced() {
	 return synced;
    }

    public void setSynced(boolean synced) {
	 this.synced = synced;
   }

	public String getVenue() {
	return venue;
   }

   public void setVenue(String venue) {
	this.venue = venue;
   }

	public Long getEid() {
	    return eid;
    }
	
	public void setEid(Long eid) {
	   this.eid = eid;
    }
	
	public String getName() {
	   return name;
    }
	
	public void setName(String name) {
	  this.name = name;
   }
	
	public String getTagline() {
		return tagline;
	}

	public void setTagline(String tagline) {
		this.tagline = tagline;
	}

	public Long getNid() {
	  return nid;
    }
	
	public void setNid(Long nid) {
	  this.nid = nid;
   }
	
	public String getPic() {
	  return pic;
   }
	
	public void setPic(String pic) {
	   this.pic = pic;
   }
	
	public String getPic_big() {
	   return pic_big;
   }
	
	public void setPic_big(String pic_big) {
	   this.pic_big = pic_big;
   }
	
	public String getPic_small() {
	  return pic_small;
   }
	
	public void setPic_small(String pic_small) {
	  this.pic_small = pic_small;
   }
	
	public String getHost() {
	   return host;
    }
	
	public void setHost(String host) {
	   this.host = host;
   }
	
	public String getDescription() {
	  return description;
   }
	
	public void setDescription(String description) {
	  this.description = description;
   }
	
	public String getEvent_type() {
	  return event_type;
   }
	
	public void setEvent_type(String event_type) {
	  this.event_type = event_type;
   }
	
	public String getEvent_sbytype() {
	  return event_sbytype;
   }
	
	public void setEvent_sbytype(String event_sbytype) {
	   this.event_sbytype = event_sbytype;
    }
	
	/*public EventLocation getEventlocation() {
	   return eventlocation;
   }
	
	public void setEventlocation(EventLocation eventlocation) {
	  this.eventlocation = eventlocation;
   }*/
	
	public Date getStart_time() {
	  return start_time;
   }
	
	
	public void setStart_time(Date start_time) {
	  this.start_time = start_time;
   }
	
	
	public Date getEnd_time() {
	  return end_time;
    }
	
	
	public void setEnd_time(Date end_time) {
	  this.end_time = end_time;
    }
	
	
	public Long getCreator() {
	  return creator;
    }
	
	
	public void setCreator(Long creator) {
	  this.creator = creator;
    }
	
	public Date getUpdate_time() {
	  return update_time;
    }
	
	public void setUpdate_time(Date update_time) {
	   this.update_time = update_time;
    }
	
	public String getLocation() {
	   return location;
    }
	
	public void setLocation(String location) {
	   this.location = location;
    }



	public static class Field
	{
		public final static String EID     = "eid";
		public final static String NAME    = "name";
		public final static String TAGLINE = "tagline";
		public final static String NID     = "nid";
		public final static String PIC     = "pic";
		public final static String PIC_BIG       = "pic_big";
		public final static String PIC_SMALL     = "pic_small";
		public final static String HOST          = "host";
		public final static String DESCRIPTION   = "description";
		public final static String EVENT_TYPE    = "event_type";
		public final static String EVENT_SUBTYPE = "event_subtype";
		public final static String START_TIME    = "start_time";
		public final static String END_TIME      = "end_time";
		public final static String CREATOR       = "creator";
		public final static String UPDATE_TIME   = "update_time";
		public final static String LOCATION      = "location";
		public final static String VENUE         = "venue";		
	}



	public int compareTo(Event another) 
	{
	    if(this.start_time.getTime() < another.start_time.getTime())
	    {
	    	return 1;
	    }
		else if(start_time.getTime() > another.start_time.getTime())
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}

	public void despose() {
		 eid   = 0;
		 name  = null;
		 tagline = null;
		 nid    = -1;;
		 pic    = null;
		 pic_big = null;
		 pic_small = null;
		 host      = null;
		 description = null;
		 event_type  = null;
		 event_sbytype = null;
		 //public EventLocation eventlocation;
		 start_time = null;
		 end_time   = null;
		 creator    = -1;
		 update_time= null;
		 location   = null;		   
		 venue      = null;
		 synced     = false;		   
		 ceid       = -1;
		 fornotification = false;
		
	}
}
