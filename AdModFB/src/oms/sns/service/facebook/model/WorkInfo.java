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
 * @author Gino Miceli
 */
public class WorkInfo extends FacebookObject<WorkInfo.Field>
{
	private static final long	serialVersionUID	= 1L;

	public WorkLocation getLocation()
	{
		return (WorkLocation) get( Field.LOCATION );
	}

	public String getCompanyName()
	{
		return getString( Field.COMPANY_NAME );
	}

	public String getPosition()
	{
		return getString( Field.POSITION );
	}

	public String getDescription()
	{
		return getString( Field.DESCRIPTION );
	}

	// TODO ?: Parse?
	public String getStartDate()
	{
		return getString( Field.START_DATE );
	}

	// TODO ?: Parse?
	public String getEndDate()
	{
		return getString( Field.END_DATE );
	}

	@Override
	protected Field fieldForName( String fieldName )
	{
		return Field.valueOf( fieldName.toUpperCase() );
	}

	public enum Field implements FacebookField
	{
		LOCATION, COMPANY_NAME, POSITION, DESCRIPTION, START_DATE, END_DATE;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}
}
