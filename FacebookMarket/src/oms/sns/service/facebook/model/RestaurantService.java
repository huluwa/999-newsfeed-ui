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

import oms.sns.service.facebook.client.FacebookField;
import oms.sns.service.facebook.client.FacebookObject;

/**
 * @author Mino Togna
 * 
 */
//TODO: check methods names
public class RestaurantService extends FacebookObject<RestaurantService.Field>
{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public Boolean isReserve()
	{
		return getBoolean( Field.RESERVE );
	}

	public Boolean isWalkins()
	{
		return getBoolean( Field.WALKINS );
	}

	public Boolean isGroups()
	{
		return getBoolean( Field.GROUPS );
	}

	public Boolean isKids()
	{
		return getBoolean( Field.KIDS );
	}

	public Boolean isTakeOut()
	{
		return getBoolean( Field.TAKEOUT );
	}

	public Boolean isDelivery()
	{
		return getBoolean( Field.DELIVERY );
	}

	public Boolean isCatering()
	{
		return getBoolean( Field.CATERING );
	}

	public Boolean isWaiter()
	{
		return getBoolean( Field.WAITER );
	}

	public Boolean isOutdoor()
	{
		return getBoolean( Field.OUTDOOR );
	}

	@Override
	protected Field fieldForName( String fieldName )
	{
		return Field.valueOf( fieldName.toUpperCase() );
	}

	public enum Field implements FacebookField
	{
		RESERVE, WALKINS, GROUPS, KIDS, TAKEOUT, DELIVERY, CATERING, WAITER,
		OUTDOOR;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}

	}

}
