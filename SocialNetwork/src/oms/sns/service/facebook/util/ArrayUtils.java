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

import java.lang.reflect.Array;
import java.util.List;

/**
 * 
 * @author Gino Miceli
 * @author Mino Togna
 */
public abstract class ArrayUtils
{
	@SuppressWarnings( "unchecked" )
	public static <T> T[] createArray( Class<T> cls, int length )
	{
		return (T[]) Array.newInstance( cls, length );
	}

	// I don't know why, but in tests this is actually 15% faster than calling
	// toArray directly... wtf??
	public static <T> T[] toArray( List<T> list, Class<T> cls )
	{
		return (T[]) list.toArray( createArray( cls, list.size() ) );
	}

	public static <T> T firstOrNull( T[] array )
	{
		return ( array == null || array.length == 0 ) ? null : array[0];
	}

	public static String join( long[] array )
	{
		return join( array, "," );
	}

	public static String join( long[] array, String seperator )
	{
		StringBuffer sb = new StringBuffer();

		for ( int i = 0; i < array.length; i++ )
		{
			if ( i > 0 )
			{
				sb.append( seperator );
			}

			sb.append( array[i] );
		}

		return sb.toString();
	}

	@SuppressWarnings( "unchecked" )
	public static long[] toLongArray( List<? extends Number> list )
	{
		long[] array = new long[ list.size() ];

		for ( int i = 0; i < list.size(); i++ )
		{
			array[i] = list.get( i ).longValue();
		}

		return array;
	}
	
	public static long[] toLongArray( String[] strings, char c )
	{
		long[] array = new long[ strings.length ];

		for ( int i = 0; i < strings.length; i++ )
		{
			String string = strings[i].trim();
			if ( string.charAt( 0 ) == c && string.charAt( string.length() - 1 ) == c )
			{
				string = string.substring( 1, string.length() - 1 );
			}
			array[i] = Long.parseLong( string );
		}

		return array;
	}
}
