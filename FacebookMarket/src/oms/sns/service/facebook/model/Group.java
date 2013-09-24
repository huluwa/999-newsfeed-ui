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
 * @author Mino Togna
 * @author Gino Miceli
 */
public class Group
{
	
	public long gid;
	public long nid;
	public String name;
	public String description;
	public String group_typ;
	public String group_subtype;
	public String pic;
	public String pic_big;
	public String pic_samll;
	public long creator;
	public Date update_time;
	public String office;
	public String website;
	//public GroupLocation grouplocation;
	public String location;
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public Long getGid() {
		return gid;
	}
	public void setGid(Long gid) {
		this.gid = gid;
	}
	
	public Long getNid() {
		return nid;
	}
	
	public void setNid(Long nid) {
		this.nid = nid;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getGroup_typ() {
		return group_typ;
	}
	
	public void setGroup_typ(String group_typ) {
		this.group_typ = group_typ;
	}

	public String getGroup_subtype() {
		return group_subtype;
	}
	public void setGroup_subtype(String group_subtype) {
		this.group_subtype = group_subtype;
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
	public String getPic_samll() {
		return pic_samll;
	}
	public void setPic_samll(String pic_samll) {
		this.pic_samll = pic_samll;
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
	public String getOffice() {
		return office;
	}
	public void setOffice(String office) {
		this.office = office;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	/*public GroupLocation getGrouplocation() {
		return grouplocation;
	}
	public void setGrouplocation(GroupLocation grouplocation) {
		this.grouplocation = grouplocation;
	}*/
	
	public enum Field 
	{
		GID, NAME, NID, DESCRIPTION, GROUP_TYPE, GROUP_SUBTYPE, RECENT_NEWS,
		PIC, PIC_BIG, PIC_SMALL, CREATOR, UPDATE_TIME, OFFICE, WEBSITE, VENUE;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}
}
