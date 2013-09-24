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
public class HighSchoolInfo extends FacebookObject<HighSchoolInfo.Field>
{
	private static final long	serialVersionUID	= 1L;

	public String getHs1Name()
	{
		return getString( Field.HS1_NAME );
	}

	public Long getHs1Id()
	{
		return getLong( Field.HS1_ID );
	}

	public Integer getGraduationYear()
	{
		return getInteger( Field.GRAD_YEAR );
	}

	public String getHs2Name()
	{
		return getString( Field.HS2_NAME );
	}

	public Long getHs2Id()
	{
		return getLong( Field.HS2_ID );
	}

	@Override
	protected Field fieldForName( String fieldName )
	{
		return Field.valueOf( fieldName.toUpperCase() );
	}

	public enum Field implements FacebookField
	{
		HS1_NAME, HS1_ID, GRAD_YEAR, HS2_NAME, HS2_ID;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}
}
