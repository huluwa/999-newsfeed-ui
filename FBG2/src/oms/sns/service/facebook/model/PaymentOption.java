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
//TODO : check getMethods name
public class PaymentOption extends FacebookObject<PaymentOption.Field>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Boolean isCashOnly()
	{
		return getBoolean( Field.CASH_ONLY );
	}

	public Boolean isVisa()
	{
		return getBoolean( Field.VISA );
	}

	public Boolean isAmericanExpress()
	{
		return getBoolean( Field.AMEX );
	}

	public Boolean isMastercard()
	{
		return getBoolean( Field.MASTERCARD );
	}

	public Boolean isDiscover()
	{
		return getBoolean( Field.DISCOVER );
	}

	protected Field fieldForName( String fieldName )
	{
		return Field.valueOf( fieldName.toUpperCase() );
	}

	public enum Field implements FacebookField
	{
		CASH_ONLY, VISA, AMEX, MASTERCARD, DISCOVER;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}

}
