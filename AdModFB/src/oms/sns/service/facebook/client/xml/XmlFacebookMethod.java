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
package oms.sns.service.facebook.client.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import oms.sns.service.facebook.client.FacebookClientException;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookMethod;
import oms.sns.service.facebook.client.FacebookPermissionErrorException;
import oms.sns.service.facebook.client.FacebookRuntimeException;
import oms.sns.service.facebook.client.InvalidSesssionException;
import oms.sns.service.facebook.client.NoExtPermissionException;
import oms.sns.service.facebook.client.FacebookClient.Format;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class XmlFacebookMethod extends FacebookMethod<Document>
{
	public XmlFacebookMethod( String methodName )
	{
		super( methodName, Format.XML );
	}

	@Override
	public Document parseResponse( String response ) throws FacebookException, InvalidSesssionException, NoExtPermissionException, FacebookPermissionErrorException
	{
		try
		{
			// TODO ?: Use shared instance of D.B.F.? NOTE: Not thread safe!
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			StringReader stringReader = new StringReader( response );
			InputSource inputSource = new InputSource( stringReader );
			Document doc = db.parse( inputSource );
             
			NodeList errors = doc.getElementsByTagName("error_response");
			if ( errors.getLength() > 0 ) {
				 NodeList errorcodenodelist = doc.getElementsByTagName("error_code");
				 NodeList messagenodelist = doc.getElementsByTagName("error_msg");
				int errorCode = (errorcodenodelist!=null && errorcodenodelist.getLength()>0)?Integer.parseInt(errorcodenodelist.item(0).getFirstChild().getNodeValue()):0;
				String message = (messagenodelist!=null && messagenodelist.getLength()>0)?messagenodelist.item(0).getFirstChild().getNodeValue():"";
				
				if(errorCode == 102)
				{
					throw new InvalidSesssionException(message, errorCode);
				}
				else if(errorCode == 200)//no ext permission, two type, one is for no permission, another is illeage op
				{
				    Log.d("sns-XmlFacebookMethod", "200 error code methodname="+methodName+" error message="+message);
				    if(FacebookMethod.getExtPermissionName(methodName) == null)
				    {
				        //actually, we can't process one case
				        //when return 200, and the method is not in our record list, 
				        //we don't know what cause this, maybe no permission, maybe no extended permission,
				        //here, we just look it as no permission.
				        throw new FacebookPermissionErrorException(methodName,errorCode,message);//why come here
				    }
				    else
				    {
					    throw new NoExtPermissionException(methodName, errorCode);
				    }
				}
				else if(errorCode == 250 || 
						errorCode == 260 ||
						errorCode == 270 || 
						errorCode == 280 ||
						errorCode == 281 || 
						errorCode == 282 ||
						errorCode == 290 || /*errorCode == 291 || errorCode == 292 ||*/ 
						errorCode == 299 ||
						errorCode == 298  
						)
				{
					throw new NoExtPermissionException(methodName, errorCode);
				}
				else
				{				
				    throw new FacebookException(message, errorCode);
				}
			}
			return doc;
		}
		catch ( ParserConfigurationException e )
		{
			throw new FacebookXmlException( e );
		}
		catch ( SAXException e )
		{
			throw new FacebookXmlException( e );
		}
		catch ( IOException e )
		{
			throw new FacebookRuntimeException( "IOException parsing String - should never happen!", e );
		}
	}
}
