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
public class FeedStoryTemplate
{
	private long actorId;
	private String titleTemplate;
	private String titleData;
	private String bodyTemplate;
	private String bodyData;
	private String bodyGeneral;
	private String image1;
	private String image1Link;
	private String image2;
	private String image2Link;
	private String image3;
	private String image3Link;
	private String image4;
	private String image4Link;
	private long[] targetUserIds;

	public FeedStoryTemplate( long actorId, String titleTemplate )
	{
		this.actorId = actorId;
		this.titleTemplate = titleTemplate;
	}

	/**
	 * @return the actorId
	 */
	public long getActorId()
	{
		return actorId;
	}

	/**
	 * @param actorId
	 *            the actorId to set
	 */
	public void setActorId( long actorId )
	{
		this.actorId = actorId;
	}

	/**
	 * @return the titleTemplate
	 */
	public String getTitleTemplate()
	{
		return titleTemplate;
	}

	/**
	 * @param titleTemplate
	 *            the titleTemplate to set
	 */
	public void setTitleTemplate( String titleTemplate )
	{
		this.titleTemplate = titleTemplate;
	}

	/**
	 * @return the titleData
	 */
	public String getTitleData()
	{
		return titleData;
	}

	/**
	 * @param titleData
	 *            the titleData to set
	 */
	public void setTitleData( String titleData )
	{
		this.titleData = titleData;
	}

	/**
	 * @return the bodyTemplate
	 */
	public String getBodyTemplate()
	{
		return bodyTemplate;
	}

	/**
	 * @param bodyTemplate
	 *            the bodyTemplate to set
	 */
	public void setBodyTemplate( String bodyTemplate )
	{
		this.bodyTemplate = bodyTemplate;
	}

	/**
	 * @return the bodyData
	 */
	public String getBodyData()
	{
		return bodyData;
	}

	/**
	 * @param bodyData
	 *            the bodyData to set
	 */
	public void setBodyData( String bodyData )
	{
		this.bodyData = bodyData;
	}

	/**
	 * @return the bodyGeneral
	 */
	public String getBodyGeneral()
	{
		return bodyGeneral;
	}

	/**
	 * @param bodyGeneral
	 *            the bodyGeneral to set
	 */
	public void setBodyGeneral( String bodyGeneral )
	{
		this.bodyGeneral = bodyGeneral;
	}

	/**
	 * @return the image1
	 */
	public String getImage1()
	{
		return image1;
	}

	/**
	 * @param image1
	 *            the image1 to set
	 */
	public void setImage1( String image1 )
	{
		this.image1 = image1;
	}

	/**
	 * @return the image1Link
	 */
	public String getImage1Link()
	{
		return image1Link;
	}

	/**
	 * @param image1Link
	 *            the image1Link to set
	 */
	public void setImage1Link( String image1Link )
	{
		this.image1Link = image1Link;
	}

	/**
	 * @return the image2
	 */
	public String getImage2()
	{
		return image2;
	}

	/**
	 * @param image2
	 *            the image2 to set
	 */
	public void setImage2( String image2 )
	{
		this.image2 = image2;
	}

	/**
	 * @return the image2Link
	 */
	public String getImage2Link()
	{
		return image2Link;
	}

	/**
	 * @param image2Link
	 *            the image2Link to set
	 */
	public void setImage2Link( String image2Link )
	{
		this.image2Link = image2Link;
	}

	/**
	 * @return the image3
	 */
	public String getImage3()
	{
		return image3;
	}

	/**
	 * @param image3
	 *            the image3 to set
	 */
	public void setImage3( String image3 )
	{
		this.image3 = image3;
	}

	/**
	 * @return the image3Link
	 */
	public String getImage3Link()
	{
		return image3Link;
	}

	/**
	 * @param image3Link
	 *            the image3Link to set
	 */
	public void setImage3Link( String image3Link )
	{
		this.image3Link = image3Link;
	}

	/**
	 * @return the image4
	 */
	public String getImage4()
	{
		return image4;
	}

	/**
	 * @param image4
	 *            the image4 to set
	 */
	public void setImage4( String image4 )
	{
		this.image4 = image4;
	}

	/**
	 * @return the image4Link
	 */
	public String getImage4Link()
	{
		return image4Link;
	}

	/**
	 * @param image4Link
	 *            the image4Link to set
	 */
	public void setImage4Link( String image4Link )
	{
		this.image4Link = image4Link;
	}

	/**
	 * @return the targetUserIds
	 */
	public long[] getTargetUserIds()
	{
		return targetUserIds;
	}

	/**
	 * @param targetUserIds the targetUserIds to set
	 */
	public void setTargetUserIds( long[] targetUserIds )
	{
		this.targetUserIds = targetUserIds;
	}

}
