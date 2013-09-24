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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @author Gino Miceli
 */
public final class EncryptionUtils
{
	public static String md5Encode( String params )
	{
		try
		{
			MessageDigest md = java.security.MessageDigest.getInstance( "MD5" );
			StringBuffer sig = new StringBuffer();
			for ( byte b : md.digest( params.getBytes() ) )
			{
				sig.append( Integer.toHexString( ( b & 0xf0 ) >>> 4 ) );
				sig.append( Integer.toHexString( b & 0x0f ) );
			}
			return sig.toString();
		}
		catch ( NoSuchAlgorithmException ex )
		{
			throw new RuntimeException( "JDK installation problem; MD5 not found." );
		}
	}
}
