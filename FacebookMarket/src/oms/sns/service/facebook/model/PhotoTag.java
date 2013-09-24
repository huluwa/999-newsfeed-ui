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
public class PhotoTag
{
  public Long pid;
  public Float xcoord;
  public Float ycoord;
  public Long subject;
  public String text;
  public Date created;
 
	public Long getPid() {
	  return pid;
	}
    
	
	public Float getXcoord() {
		return xcoord;
	}


	public void setXcoord(Float xcoord) {
		this.xcoord = xcoord;
	}


	public Float getYcoord() {
		return ycoord;
	}


	public void setYcoord(Float ycoord) {
		this.ycoord = ycoord;
	}


	public Long getSubject() {
		return subject;
	}
	
	public void setSubject(Long subject) {
		this.subject = subject;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public Date getCreated() {
		return created;
	}
	
	public void setCreated(Date created) {
		this.created = created;
	}
	
	public void setPid(Long pid) {
		this.pid = pid;
	}
	
	public enum Field
	{
		PID, SUBJECT, TEXT, XCOORD, YCOORD, CREATED;
		

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}
}
