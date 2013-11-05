/*
 *  fb4j: Java API for Facebook
 *  Copyright (C) 2007-2008 Biagio Miceli Jr, Cosimo Togna
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Full license can be found in LICENSE.txt.  Documentation, updates and other 
 *  info can be found at http://fb4j.sourceforge.net/ 
 *
 *  @version $Id$
 */
package oms.sns.service.facebook.client;

/**
 * Thrown when an trying to call method that requires login on behalf of
 * a user that has not yet logged in.
 * 
 * @author Gino Miceli
 */
public class UserNotLoggedInError extends FacebookRuntimeException
{
	private static final long	serialVersionUID	= 1L;

	public UserNotLoggedInError()
	{
		super();
	}

	public UserNotLoggedInError( String msg )
	{
		super( msg );
	}

}
