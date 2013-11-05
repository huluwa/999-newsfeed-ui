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
package oms.sns.service.facebook.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Base class for entities accessible through the Facebook API.  By default, 
 * these are read-only.  FacebookObject implements Map to allow easy access (e.g. JSP EL)
 * to properties using their Facebook field name (e.g. work_history).  The Map's keys
 * are defined by an enum given in the generic constructor.  Its values contain Facebook
 * data mapped to Java objects or arrays by fb4j.   
 * 
 * Implementations of this class provide bean-style accessors using Java-friendly property 
 * names (e.g. workHistory).
 * 
 * Note that fields may be null or empty for a number of reasons (access denied by Facebook,
 * not requested in FQL query, etc.)  In some cases Facebook's REST server returns empty arrays,
 * sometimes null.  It is up to the developer to ensure that requested fields were loaded properly. 
 * 
 * @author Gino Miceli
 * @author Mino Togna
 * 
 * @param <K> the enum representing the field names for this Facebook entity
 */
public abstract class FacebookObject<K extends FacebookField> extends HashMap<K, Object>
{
	public static final Long    EMPTY_LONG    = -1L;
	public static final Integer EMPTY_INTEGER = -1;
	public static final Float 	EMPTY_FLOAT   = -1.0F;
	public static final Date 	EMPTY_DATE    = new Date(0);
	public static final String 	DATE_FORMAT   = "MM/dd/yyyy";

	@Override
	public Object get( Object key )
	{
		if ( key instanceof String )
		{
			K field = fieldForName( (String) key );
			
			return super.get( field );
		}
		else
		{
			return super.get( key );
		}
	}

	protected final Object putInternal( String fieldName, Object value )
	{
		K field = fieldForName( fieldName );

		return putInternal( field, value );
	}

	protected Object putInternal( K field, Object value )
	{
		return super.put( field, value );
	}
	
	@Override
	public final Object put( K key, Object value )
	{
		return super.put( key, value );
	}

	@Override
	public final void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final void putAll( Map<? extends K, ? extends Object> m )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final Object remove( Object key )
	{
		throw new UnsupportedOperationException();
	}
	
	protected abstract K fieldForName( String fieldName );

	protected String getString( K key )
	{
		return (String) get( key );
	}

	protected Boolean getBoolean( K key )
	{
		return (Boolean) get( key );
	}

	protected String[] getStringArray( K key )
	{
		return (String[]) get( key );
	}

	protected Number getNumber( K key )
	{
		return (Number) get( key );
	}

	protected Integer getInteger( K key )
	{
		return (Integer) get( key );
//		Number n = getNumber( key );
//
//		return ( n == null ) ? null : n.intValue();
	}

	protected Long getLong( K key )
	{
		return (Long) get( key );
//		Number n = getNumber( key );
//
//		return ( n == null ) ? null : n.longValue();
	}	

	protected Float getFloat( K key )
	{
		return (Float) get( key );
//		Number n = getNumber( key );
//
//		return ( n == null ) ? null : n.floatValue();
	}

	protected Date getDate( K key )
	{
		return (Date) get( key );
//		Float f = getFloat( key );
//
//		return ( f == null ) ? null : new Date( (long) ( f * 1000 ) );
	}
	
}
