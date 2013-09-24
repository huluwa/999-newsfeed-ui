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
//TODO : check if they should returns the long value or the hours like 9:00 AM
//TODO : check getMethods name
public class OperatingHours extends FacebookObject<OperatingHours.Field>
{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public Long getMon1Open()
	{
		return getLong( Field.MON_1_OPEN );
	}

	public Long getMon1Close()
	{
		return getLong( Field.MON_1_CLOSE );
	}

	public Long getMon2Open()
	{
		return getLong( Field.MON_2_OPEN );
	}

	public Long getMon2Close()
	{
		return getLong( Field.MON_2_CLOSE );
	}

	public Long getTue1Open()
	{
		return getLong( Field.TUE_1_OPEN );
	}

	public Long getTue1Close()
	{
		return getLong( Field.TUE_1_CLOSE );
	}

	public Long getTue2Open()
	{
		return getLong( Field.TUE_2_OPEN );
	}

	public Long getTue2Close()
	{
		return getLong( Field.TUE_2_CLOSE );
	}

	public Long getWed1Open()
	{
		return getLong( Field.WED_1_OPEN );
	}

	public Long getWed1Close()
	{
		return getLong( Field.WED_1_CLOSE );
	}

	public Long getWed2Open()
	{
		return getLong( Field.WED_2_OPEN );
	}

	public Long getWed2Close()
	{
		return getLong( Field.WED_2_CLOSE );
	}

	public Long getThu1Open()
	{
		return getLong( Field.THU_1_OPEN );
	}

	public Long getThu1Close()
	{
		return getLong( Field.THU_1_CLOSE );
	}

	public Long getThu2Open()
	{
		return getLong( Field.THU_2_OPEN );
	}

	public Long getThu2Close()
	{
		return getLong( Field.THU_2_CLOSE );
	}

	public Long getFri1Open()
	{
		return getLong( Field.FRI_1_OPEN );
	}

	public Long getFri1Close()
	{
		return getLong( Field.FRI_1_CLOSE );
	}

	public Long getFri2Open()
	{
		return getLong( Field.FRI_2_OPEN );
	}

	public Long getFri2Close()
	{
		return getLong( Field.FRI_2_CLOSE );
	}

	public Long getSat1Open()
	{
		return getLong( Field.SAT_1_OPEN );
	}

	public Long getSat1Close()
	{
		return getLong( Field.SAT_1_CLOSE );
	}

	public Long getSat2Open()
	{
		return getLong( Field.SAT_2_OPEN );
	}

	public Long getSat2Close()
	{
		return getLong( Field.SAT_2_CLOSE );
	}

	public Long getSun1Open()
	{
		return getLong( Field.SUN_1_OPEN );
	}

	public Long getSun1Close()
	{
		return getLong( Field.SUN_1_CLOSE );
	}

	public Long getSun2Open()
	{
		return getLong( Field.SUN_2_OPEN );
	}

	public Long getSun2Close()
	{
		return getLong( Field.SUN_2_CLOSE );
	}

	@Override
	protected Field fieldForName( String fieldName )
	{
		return Field.valueOf( fieldName.toUpperCase() );
	}

	public enum Field implements FacebookField
	{
		MON_1_OPEN, MON_1_CLOSE, TUE_1_OPEN, TUE_1_CLOSE, WED_1_OPEN,
		WED_1_CLOSE, THU_1_OPEN, THU_1_CLOSE, FRI_1_OPEN, FRI_1_CLOSE,
		SAT_1_OPEN, SAT_1_CLOSE, SUN_1_OPEN, SUN_1_CLOSE, MON_2_OPEN,
		MON_2_CLOSE, TUE_2_OPEN, TUE_2_CLOSE, WED_2_OPEN, WED_2_CLOSE,
		THU_2_OPEN, THU_2_CLOSE, FRI_2_OPEN, FRI_2_CLOSE, SAT_2_OPEN,
		SAT_2_CLOSE, SUN_2_OPEN, SUN_2_CLOSE;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}

	}

}
