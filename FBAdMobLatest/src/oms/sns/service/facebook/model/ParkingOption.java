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
import oms.sns.service.facebook.client.FacebookRuntimeException;

/**
 * @author Mino Togna
 *
 */
public class ParkingOption extends FacebookObject<ParkingOption.Field>
{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	//TODO: Check names getmethods
	
	public Boolean isStreet()
	{
		return getBoolean( Field.STREET );
	}
	
	public Boolean isLot()
	{
		return getBoolean( Field.LOT );
	}
	
	public Boolean isValet()
	{
		return getBoolean( Field.VALET );
	}
	@Override
	protected Field fieldForName( String fieldName )
	{
		return Field.valueOf( fieldName.toUpperCase() );
	}
	
	
	@Override
	protected Object putInternal( Field field, Object value )
	{
		return super.putInternal( field, parseBoolean( value ) );
	}

	private Boolean parseBoolean( Object value )
	{
		if ( value instanceof Boolean )
		{
			return (Boolean) value;
		}
		else if ( value instanceof Number || value instanceof String )
		{
			String string = value.toString();
			return new Boolean( ! ( "0".equals( string ) || "false".equals( string ) ) );
		}
		else
		{
			throw new FacebookRuntimeException( "Excepted a Boolean but got a " + value.getClass().getName() );
		}
	}
	
	public enum Field implements FacebookField
	{
		STREET, LOT, VALET;
		
		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}
}
