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


public class UserStatus 
{
  public Long uid;
  public Long statusid;
  public Date time;
  public Long source;
  public String message;
  public String username;
  
  public String getUsername() {
	return username;
}

  public String toString()
  {
	  return "uid="+uid +
	         " username="+username+
	         " message="+message;
  }
public void setUsername(String username) {
	this.username = username;
}

public Long getUid() {
	return uid;
   }
  
  public void setUid(Long uid) {
	this.uid = uid;
  }
  
  public Long getStatusid() {
	return statusid;
  }
  
  public void setStatusid(Long statusid) {
	this.statusid = statusid;
  }
  
  public Date getTime() {
	return time;
  }
  
  public void setTime(Date time) {
	this.time = time;
  }
  
  public Long getSource() {
	return source;
  }
  
  public void setSource(Long source) {
	this.source = source;
  }
  
  public String getMessage() {
	return message;
  }
  
  public void setMessage(String message) {
	this.message = message;
  }
  
	/*
	 * <status>
    <uid>1036141712</uid>
    <status_id>79147540675</status_id>
    <time>1239591391</time>
    <source>2227470867</source>
    <message>is hello,jessie</message>
  </status>
	 */
	public static class Field
	{
		public static final String UID      = "uid";
		public static final String STATUS_ID= "status_id";
		public static final String TIME     = "time";
		public static final String SOURCE   = "source";
		public static final String MESSAGE  = "message";		
	}

	public void recycle() 
	{
		 uid      = null;
		 statusid = null;
		 time     = null;
		 source   = null;
		 message  = null;
		 username = null;		
	}
}
