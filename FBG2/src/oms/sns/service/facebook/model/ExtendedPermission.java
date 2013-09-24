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

import oms.sns.service.facebook.client.FacebookException;

/**
 * email, 
 * offline_access,
 *  status_update,
 *  photo_upload, 
 *  create_listing, 
 *  create_event, rsvp_event, 
 *  sms, video_upload, 
 *  create_note, share_item
 * @author Gino Miceli
 */
public enum ExtendedPermission
{
	PUBLISH_STATUS( "publish_stream"),
	READ_STREAM( "read_stream"),
	STATUS_UPDATE( "status_update" ), 
	PHOTO_UPLOAD( "photo_upload" ),
	VIDEO_UPLOAD("video_upload"),
	SEND_EMAIL("email"),
	OFFLINE_ACCESS("offline_access"),
	CREATE_LISTING( "create_listing" ),
	CREATE_EVENT("create_event"),	
	RSVP_EVENT("rsvp_event"),
	SMS("sms"),
	CREATE_NOTE("create_note"),
	LINK_SHARE("share_item"),
	READ_MAILBOX("read_mailbox"),
	NONE_PERM("none_nothing");

	private String	facebookPermission;

	private ExtendedPermission( String fbperm )
	{
		this.facebookPermission = fbperm;
	}

	public static ExtendedPermission getPermission(String perm) throws FacebookException
	{
		if(perm.equalsIgnoreCase("publish_stream"))
			return PUBLISH_STATUS;
		else if(perm.equalsIgnoreCase("read_stream"))
			return READ_STREAM;
	    else if(perm.equalsIgnoreCase("status_update"))
			return STATUS_UPDATE;
		else if(perm.equalsIgnoreCase("photo_upload"))
			return PHOTO_UPLOAD;
		else if(perm.equalsIgnoreCase("video_upload"))
			return VIDEO_UPLOAD;
		else if(perm.equalsIgnoreCase("email"))
			return SEND_EMAIL;
		else if(perm.equalsIgnoreCase("read_mailbox"))
		    return READ_MAILBOX;
		else if(perm.equalsIgnoreCase("offline_access"))
			return OFFLINE_ACCESS;
		else if(perm.equalsIgnoreCase("create_listing"))
			return  CREATE_LISTING;		
		else if(perm.equalsIgnoreCase("create_event"))
			return  CREATE_EVENT;
		else if(perm.equalsIgnoreCase("rsvp_event"))
			return  RSVP_EVENT;
		else if(perm.equalsIgnoreCase("sms"))
			return  SMS;
		else if(perm.equalsIgnoreCase("create_note"))
			return  CREATE_EVENT;
		else if(perm.equalsIgnoreCase("share_item"))
			return  LINK_SHARE;
		else
		{
			throw new FacebookException("what is permission="+perm);			
		}
			
	}
	public String getFacebookPermission()
	{
		return facebookPermission;
	}
}
