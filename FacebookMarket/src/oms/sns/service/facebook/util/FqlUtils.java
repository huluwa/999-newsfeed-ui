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
package oms.sns.service.facebook.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oms.sns.service.facebook.client.FacebookClientException;



/**
 * @author Mino Togna
 * @author Gino Miceli
 */
public class FqlUtils
{
	private final static String		REGEX	= "FROM\\s*(.*?)\\s*WHERE";
	private final static Pattern	PATTERN	= Pattern.compile( REGEX, Pattern.CASE_INSENSITIVE );

	/**
	 * @param fql
	 * @return table name, in lower case
	 * @throws FacebookClientException
	 */
	public static String extractTableName( String fql ) throws FacebookClientException
	{
		Matcher matcher = PATTERN.matcher( fql );

		if ( !matcher.find() || matcher.groupCount() != 1 )
		{
			throw new FacebookClientException( "Invalid query: " + fql );
		}

		return matcher.group( 1 ).trim().toLowerCase();
	}
}
