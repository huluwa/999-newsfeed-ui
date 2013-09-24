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
package oms.sns.service.facebook.model;

/**
 * @author Mino Togna
 * 
 */
// TODO M&G: DO we need the setParams Methods?!
public class FacebookEmail
{
	private long[] recipients;
	private String fbml;
	private String subject;
	private String text;

	public FacebookEmail( String subject, String fbml, String text, long... recipients )
	{
		this.recipients = recipients;
		this.fbml = fbml;
		this.subject = subject;
		this.text = text;
	}

	public FacebookEmail( String subject, String fbml, long... recipients )
	{
		this.recipients = recipients;
		this.fbml = fbml;
		this.subject = subject;
	}

	/**
	 * @return the recipients
	 */
	public long[] getRecipients()
	{
		return recipients;
	}

	/**
	 * @param recipients
	 *            the recipients to set
	 */
	public void setRecipients( long[] recipients )
	{
		this.recipients = recipients;
	}

	/**
	 * @return the fbml
	 */
	public String getFbml()
	{
		return fbml;
	}

	/**
	 * @param fbml
	 *            the fbml to set
	 */
	public void setFbml( String fbml )
	{
		this.fbml = fbml;
	}

	/**
	 * @return the subject
	 */
	public String getSubject()
	{
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject( String subject )
	{
		this.subject = subject;
	}

	/**
	 * @return the text
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText( String text )
	{
		this.text = text;
	}

}
