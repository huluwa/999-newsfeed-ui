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

import oms.sns.service.facebook.client.FacebookClient.InstallParameters;
import oms.sns.service.facebook.client.FacebookClient.LoginParameters;
import oms.sns.service.facebook.client.FacebookClient.Server;
import oms.sns.service.facebook.util.UrlBuilder;

/**
 * Base class for all Facebook application types.  This class stores the 
 * API Key and Secret Key provided by the Facebook Developer application.
 * This class also provides the URL's and appropriate parameters required
 * for login and installation.  
 * 
 * @author Gino Miceli
 * @author Mino Togna
 */
public abstract class FacebookApplication
{
	protected String	apiKey;
	protected String	secretKey;
	
	protected String	_sessionSecret;
	protected boolean   _isDesktop=false;
	
	protected String _sessionKey;
	protected long _userId;
	protected long _expires;
	
	public void setDesktop(){_isDesktop= true;}

	protected FacebookApplication( String apiKey, String secretKey )
	{
		this.apiKey = apiKey;
		this.secretKey = secretKey;
	}

	/**
	 * @return The API key of the current application
	 */
	public final String getApiKey()
	{
		return apiKey;
	}

	/**
	 * 
	 * @param afterLoginUri
	 *            If null, redirects to root of app
	 * @param popup
	 * @param forceLogin
	 * @param hideSaveLoginCheckbox
	 * @param canvas
	 *            if true, redirect to facebook.com/apps/..., else to callback
	 *            url
	 * @return
	 */
	public String getLoginUrl( String afterLoginUri, boolean secure, boolean popup, boolean forceLogin, boolean hideSaveLoginCheckbox, boolean canvasAfterLogin )
	{
		UrlBuilder url = new UrlBuilder( secure ? Server.SECURE_LOGIN_URL : Server.LOGIN_URL, true );

		url.append( LoginParameters.API_KEY, getApiKey() );
		url.append( LoginParameters.API_VERSION, Server.API_VERSION );
		url.append( LoginParameters.NEXT_URL, afterLoginUri );
		url.append( LoginParameters.POPUP, popup ? "1" : null );
		url.append( LoginParameters.SKIP_COOKIE, forceLogin ? "1" : null );
		url.append( LoginParameters.HIDE_CHECKBOX, hideSaveLoginCheckbox ? "1" : null );
		url.append( LoginParameters.CANVAS, canvasAfterLogin ? "1" : null );

		return url.toString();
	}

	public String getInstallUrl( String nextUri )
	{
		UrlBuilder url = new UrlBuilder( Server.INSTALL_URL, true );
		url.append( InstallParameters.API_KEY, getApiKey() );	
		url.append( InstallParameters.NEXT_URI, nextUri );

		return url.toString();
	}
}
