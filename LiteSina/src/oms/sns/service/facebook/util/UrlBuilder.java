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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Gino Miceli
 */
public class UrlBuilder
{
	private StringBuffer	sb;
	private boolean			first	= true;
	private boolean			ignoreIfNull;

	public UrlBuilder( String url, boolean ignoreIfNull )
	{
		this.sb = new StringBuffer( url );

		this.ignoreIfNull = ignoreIfNull;
	}

	public UrlBuilder append( String key, String value )
	{
		if ( value == null && ignoreIfNull )
		{
			return this;
		}

		if ( first )
		{
			sb.append( "?" );
			first = false;
		}
		else
		{
			sb.append( "&" );
		}

		try
		{
			sb.append( URLEncoder.encode( key, "UTF-8" ) );
			sb.append( "=" );
			sb.append( URLEncoder.encode( value, "UTF-8" ) );
		}
		catch ( UnsupportedEncodingException e )
		{
		}

		return this;
	}

	@Override
	public String toString()
	{
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public void appendAll(Map parameterMap)
	{
		Set<Map.Entry<String,String[]>> entries = parameterMap.entrySet();
		for ( Map.Entry<String,String[]> entry : entries ) 
		{
			appendAll( entry.getKey(), entry.getValue() );
		}
	}

	 public void appendAll(String key, String[] values) 
	 {
		for ( String value : values ) 
		{
			append( key, value );
		}
		
	}
}
