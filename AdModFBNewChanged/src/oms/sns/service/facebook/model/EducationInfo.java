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
public class EducationInfo extends FacebookObject<EducationInfo.Field>
{
	private static final long	serialVersionUID	= 1L;

	public String getName()
	{
		return getString( Field.NAME );
	}

	public Integer getYear()
	{
		return getInteger( Field.YEAR );
	}

	public String[] getConcentrations()
	{
		return getStringArray( Field.CONCENTRATIONS );
	}
	
	public String getDegree()
	{
		return getString( Field.DEGREE );
	}

	@Override
	protected Field fieldForName( String fieldName )
	{
		return Field.valueOf( fieldName.toUpperCase() );
	}
	
	public enum Field implements FacebookField
	{
		CONCENTRATIONS, YEAR, NAME, DEGREE;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}
}
