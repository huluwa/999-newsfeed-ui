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
package oms.sns.service.facebook.Desktop;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.util.Log;

import oms.sns.service.facebook.client.FacebookApplication;
import oms.sns.service.facebook.client.FacebookClient;
import oms.sns.service.facebook.client.FacebookClientException;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.client.FacebookClient.LoginParameters;
import oms.sns.service.facebook.client.FacebookClient.Server;
import oms.sns.service.facebook.client.FacebookMethod.Auth;
import oms.sns.service.facebook.client.xml.XmlFacebookMethod;
import oms.sns.service.facebook.util.UrlBuilder;


/**
 * @author Gino Miceli
 * @author Mino Togna
 */
public class DesktopApplication extends FacebookApplication
{
	final String TAG = "DesktopApplication";
	private FacebookSession	session;
	private FacebookClient	fbClient;

	public DesktopApplication( String apiKey, String secretKey )
	{		
		super( apiKey, secretKey );
		_isDesktop = true;
		fbClient = new FacebookClient( apiKey, secretKey );
	}

	public String requestToken() throws FacebookException
	{
		Document doc = fbClient.callSecureMethod( new XmlFacebookMethod(Auth.CREATE_TOKEN) );
		
		String val = null;
		NodeList  nodelist = doc.getElementsByTagName("auth_createToken_response");
		if(nodelist != null)
		{
		    String name = nodelist.item(0).getNodeName();		
		    val         = nodelist.item(0).getFirstChild().getNodeValue();		
		    Log.d(TAG, "requestToken ="+val);
		}
		return val;
	}
	
	public void dispose()
	{
		fbClient.dispose();
	}

	// TODO G: Check if session is expired, if not, return false
	public boolean isUserLoggedIn()
	{
		return session == null;
	}

	/**
	 * Return the Facebook Session given the token as input
	 * 
	 * @param token
	 *            to create the session
	 * @return a new Facebook Session
	 * @throws FacebookClientException
	 */
	public FacebookSession requestSession( String token ) throws FacebookException
	{
		Document doc =  fbClient.callSecureMethod( new XmlFacebookMethod(Auth.GET_SESSION), FacebookClient.LoginParameters.AUTH_TOKEN, token);
		//TODO
		
		//create a FacebookSession
		NodeList nl = doc.getElementsByTagName( "session_key" );
		if(nl != null && nl.item( 0 ) != null && nl.item( 0 ).getFirstChild()!= null)
		{
			this._sessionKey = nl.item( 0 ).getFirstChild().getNodeValue();
					
			this._userId = Long.parseLong( doc.getElementsByTagName( "uid" ).item( 0 ).getFirstChild().getNodeValue());
			this._expires = Long.parseLong( doc.getElementsByTagName( "expires" ).item( 0 ).getFirstChild().getNodeValue());
			
			if ( this._isDesktop && doc.getElementsByTagName( "secret" ) != null) 
			{
				if(doc.getElementsByTagName( "secret" ).item( 0 ) != null && doc.getElementsByTagName( "secret" ).item( 0 ).getFirstChild() != null)
				{
			        this._sessionSecret = doc.getElementsByTagName( "secret" ).item( 0 ).getFirstChild().getNodeValue();
			        
			        return new FacebookSession(this.apiKey, this._sessionSecret, this._sessionKey, this._userId);
				}
			}
			
			return new FacebookSession(this.apiKey, this.secretKey, this._sessionKey, this._userId);
		}

		//TODO
		return null;
	}
	
	public FacebookSession AuthLogin(String ses_key, String secret_key, String email, String pwd ) throws FacebookException
	{
		Document doc =  fbClient.callSecureMethod( new XmlFacebookMethod(Auth.LOGIN),  /*FacebookClient.LoginParameters.AUTH_TOKEN, token,*/ FacebookClient.RestParameters.SESSION_KEY, ses_key, FacebookClient.RestParameters.SECRET_KEY, secret_key, FacebookClient.LoginParameters.EMAIL, email, FacebookClient.LoginParameters.PASSWORD, pwd);
		
		//create a FacebookSession
		NodeList nl = doc.getElementsByTagName( "session_key" );
		if(nl != null && nl.item( 0 ) != null && nl.item( 0 ).getFirstChild()!= null)
		{
			this._sessionKey = nl.item( 0 ).getFirstChild().getNodeValue();
					
			this._userId = Long.parseLong( doc.getElementsByTagName( "uid" ).item( 0 ).getFirstChild().getNodeValue());
			NodeList expireNode = doc.getElementsByTagName( "expires" );
			if(expireNode != null && expireNode.getLength() > 0)
			{
			    this._expires = Long.parseLong( expireNode.item( 0 ).getFirstChild().getNodeValue());
			}
			
			if ( this._isDesktop && doc.getElementsByTagName( "secret" ) != null) 
			{
				if(doc.getElementsByTagName( "secret" ).item( 0 ) != null && doc.getElementsByTagName( "secret" ).item( 0 ).getFirstChild() != null)
				{
			        this._sessionSecret = doc.getElementsByTagName( "secret" ).item( 0 ).getFirstChild().getNodeValue();
			        
			        return new FacebookSession(this.apiKey, this._sessionSecret, this._sessionKey, this._userId);
				}
			}
			
			return new FacebookSession(this.apiKey, this.secretKey, this._sessionKey, this._userId);
		}
		
		return null;
		//TODO
	}
	
	public FacebookSession AuthLoginNoSession(String email, String pwd ) throws FacebookException
	{
		Document doc =  fbClient.callSecureMethod( new XmlFacebookMethod(Auth.LOGIN_NOSESSION),  FacebookClient.LoginParameters.EMAIL, email, FacebookClient.LoginParameters.PASSWORD, pwd);
		
		//create a FacebookSession
		NodeList nl = doc.getElementsByTagName( "session_key" );
		if(nl != null && nl.item( 0 ) != null && nl.item( 0 ).getFirstChild()!= null)
		{
			this._sessionKey = nl.item( 0 ).getFirstChild().getNodeValue();
					
			this._userId = Long.parseLong( doc.getElementsByTagName( "uid" ).item( 0 ).getFirstChild().getNodeValue());
			NodeList expireNode = doc.getElementsByTagName( "expires" );
			if(expireNode != null && expireNode.getLength() > 0)
			{
			    this._expires = Long.parseLong( expireNode.item( 0 ).getFirstChild().getNodeValue());
			}
			
			if ( this._isDesktop && doc.getElementsByTagName( "secret" ) != null) 
			{
				if(doc.getElementsByTagName( "secret" ).item( 0 ) != null && doc.getElementsByTagName( "secret" ).item( 0 ).getFirstChild() != null)
				{
			        this._sessionSecret = doc.getElementsByTagName( "secret" ).item( 0 ).getFirstChild().getNodeValue();
			        
			        return new FacebookSession(this.apiKey, this._sessionSecret, this._sessionKey, this._userId);
				}
			}
			
			return new FacebookSession(this.apiKey, this.secretKey, this._sessionKey, this._userId);
		}
		
		return null;
		//TODO
	}
	

	public long getExpire()
	{
		return _expires;
	}
	public FacebookSession getSession()
	{
		return session;
	}

	public String getLoginUrl( String authToken )
	{
		return getLoginUrl( authToken, null, true, true, false );
	}

	public String getExtPermURL(String permission)	
	{
		/*UrlBuilder url = new UrlBuilder( Server.PERM_URL, true );

		url.append( LoginParameters.API_KEY, getApiKey() );
		url.append( LoginParameters.API_VERSION, Server.API_VERSION );
		url.append( "ext_perm", permission);
		url.append( LoginParameters.NEXT_URL, Server.PERM_OK);
		url.append( LoginParameters.NEXT_Cancel, Server.PERM_cancel);*/
	    //http://m.facebook.com/authorize.php?api_key=YOUR_API_KEY&v=1.0&ext_perm=PERMISSION_NAME
	    UrlBuilder url = new UrlBuilder(Server.PERM_URL,true);
	    url.append( LoginParameters.API_KEY, getApiKey() );
        url.append( LoginParameters.API_VERSION, Server.API_VERSION );
        url.append( "ext_perm", permission);

		return url.toString();
	}
	public String getLoginUrl( String authToken, String next, boolean popup, boolean forceLogin, boolean hideSaveLoginCheckbox )
	{
		UrlBuilder url = new UrlBuilder( Server.LOGIN_URL, true );

		url.append( LoginParameters.API_KEY, getApiKey() );
		url.append( LoginParameters.API_VERSION, Server.API_VERSION );
		url.append( LoginParameters.NEXT_URL, next );
		url.append( LoginParameters.AUTH_TOKEN, authToken );
		url.append( LoginParameters.POPUP, popup ? "1" : null );
		url.append( LoginParameters.SKIP_COOKIE, forceLogin ? "1" : null );
		url.append( LoginParameters.HIDE_CHECKBOX, hideSaveLoginCheckbox ? "1" : null );
		url.append( "ext_perm", "email");

		return url.toString();
	}
}
