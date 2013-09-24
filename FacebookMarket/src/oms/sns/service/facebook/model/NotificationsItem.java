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
 * 
 * TODO G: Better name?
 */
public class NotificationsItem extends FacebookObject<NotificationsItem.Field>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public Integer getUnread()
	{
		return getInteger( Field.UNREAD );
	}
	
	public Date getMostRecent()
	{
		return getDate( Field.MOST_RECENT );
	}
	
	@Override
	protected Field fieldForName( String fieldName )
	{
		return Field.valueOf( fieldName.toUpperCase() );
	}

	public enum Field implements FacebookField
	{
		UNREAD, MOST_RECENT;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}

}
