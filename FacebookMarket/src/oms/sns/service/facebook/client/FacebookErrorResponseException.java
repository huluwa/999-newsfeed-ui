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

/**
 * Thrown when Facebook REST Server returns an error message/code.
 * 
 * @author Gino Miceli
 */
public class FacebookErrorResponseException extends FacebookClientException
{
	private static final long	serialVersionUID	= 1L;
	private int					code;
	private String				args;

	public FacebookErrorResponseException( String msg, String args, int code )
	{
		super( msg );

		this.args = args;
		this.code = code;
	}

	public int getCode()
	{
		return code;
	}

	public String getArgs()
	{
		return args;
	}
}
