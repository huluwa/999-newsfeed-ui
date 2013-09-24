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

import java.util.Date;

import oms.sns.service.facebook.client.FacebookField;
import oms.sns.service.facebook.client.FacebookObject;

/**
 * @author Gino Miceli
 * @author Mino Togna
 * 
 */
public class Listing extends FacebookObject<Listing.Field>
{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private static final String[] PAY_TYPE = { "Salary", "Hourly", "One Time" };

	public Listing()
	{
		super();
	}

	/***************************************************************************
	 * ALL
	 **************************************************************************/

	public String getCategory()
	{
		return getString( Field.CATEGORY );
	}

	public void setCategory( String category )
	{
		super.putInternal( Field.CATEGORY, category );
	}

	public String getDescription()
	{
		return getString( Field.DESCRIPTION );
	}

	public void setDescription( String description )
	{
		super.putInternal( Field.DESCRIPTION, description );
	}

	public Long getId()
	{
		return getLong( Field.LISTING_ID );
	}

	public String[] getImageUrls()
	{
		return getStringArray( Field.IMAGE_URLS );
	}

	public Long getPoster()
	{
		return getLong( Field.POSTER );
	}

	public String getSubCategory()
	{
		return getString( Field.SUBCATEGORY );
	}

	public void setSubCategory( String subCategory )
	{
		super.putInternal( Field.SUBCATEGORY, subCategory );
	}

	public String getTitle()
	{
		return getString( Field.TITLE );
	}

	public void setTitle( String title )
	{
		super.putInternal( Field.TITLE, title );
	}

	public Date getUpdateTime()
	{
		return getDate( Field.UPDATE_TIME );
	}

	public String getUrl()
	{
		return getString( Field.URL );
	}

	/***************************************************************************
	 * FORSALE
	 **************************************************************************/

	public String getCondition()
	{
		return getString( Field.CONDITION );
	}

	public void setCondition( String condition )
	{
		super.putInternal( Field.CONDITION, condition );
	}

	public String getIsbn()
	{
		return getString( Field.ISBN );
	}

	public void setIsbn( String isbn )
	{
		super.putInternal( Field.ISBN, isbn );
	}

	/***************************************************************************
	 * FORSALE / HOUSING :: REALESTATE
	 **************************************************************************/

	public Long getPrice()
	{
		return getLong( Field.PRICE );
	}

	public void setPrice( Long price )
	{
		super.putInternal( Field.PRICE.toString(), price );
	}

	/***************************************************************************
	 * HOUSING
	 **************************************************************************/

	public Integer getNumBeds()
	{
		return getInteger( Field.NUM_BEDS );
	}

	public void setNumBeds( Integer numBeds )
	{
		super.putInternal( Field.NUM_BEDS, numBeds );
	}

	public Integer getNumBaths()
	{
		return getInteger( Field.NUM_BATHS );
	}

	public void setNumBaths( Integer numBaths )
	{
		super.putInternal( Field.NUM_BATHS, numBaths );
	}

	public Boolean areDogsAllowed()
	{
		return getBoolean( Field.DOGS );
	}

	public void setDogsAllowed( Boolean areDogsAllowed )
	{
		super.putInternal( Field.DOGS, areDogsAllowed );
	}

	public Boolean areCatsAllowed()
	{
		return getBoolean( Field.CATS );
	}

	public void setCatsAllowed( Boolean areCatsAllowed )
	{
		super.putInternal( Field.CATS, areCatsAllowed );
	}

	public Boolean areSmokersAllowed()
	{
		return getBoolean( Field.SMOKING );
	}

	public void setSmokersAllowed( Boolean areSmokersAllowed )
	{
		super.putInternal( Field.SMOKING, areSmokersAllowed );
	}

	public Integer getSquareFootage()
	{
		return getInteger( Field.SQUARE_FOOTAGE );
	}

	public void setSquareFootage( Integer squareFootage )
	{
		super.putInternal( Field.SQUARE_FOOTAGE, squareFootage );
	}

	public String getStreet()
	{
		return getString( Field.STREET );
	}

	public void setStreet( String street )
	{
		super.putInternal( Field.STREET, street );
	}

	public String getCrossStreet()
	{
		return getString( Field.CROSSSTREET );
	}

	public void setCrossStreet( String crossStreet )
	{
		super.putInternal( Field.CROSSSTREET, crossStreet );
	}

	// TODO ?: Is the postal code a string?!!?
	public String getPostalCode()
	{
		return getString( Field.POSTAL );
	}

	public void setPostalCode( String postalCode )
	{
		super.putInternal( Field.POSTAL, postalCode );
	}

	/***************************************************************************
	 * HOUSING :: RENTALS / HOUSING :: SUBLETS
	 **************************************************************************/

	public Integer getRent()
	{
		return getInteger( Field.RENT );
	}

	public void setRent( Integer rent )
	{
		super.putInternal( Field.RENT, rent );
	}

	/***************************************************************************
	 * JOBS
	 **************************************************************************/

	public Integer getPay()
	{
		return getInteger( Field.PAY );
	}

	public void setPay( Integer pay )
	{
		super.putInternal( Field.PAY, pay );
	}

	public Boolean isFullTime()
	{
		return getBoolean( Field.FULL );
	}

	public void setFullTime( Boolean isFullTime )
	{
		super.putInternal( Field.FULL, isFullTime );
	}

	public Boolean isPartTime()
	{
		return !isFullTime();
	}

	public void setPartTime( Boolean isPartTime )
	{
		super.putInternal( Field.FULL, !isPartTime );
	}

	public Boolean isInternship()
	{
		return getBoolean( Field.INTERN );
	}

	public void setInternship( Boolean isInternship )
	{
		super.putInternal( Field.INTERN, isInternship );
	}

	public Boolean isSeasonalJob()
	{
		return getBoolean( Field.SUMMER );
	}

	public void setSeasonalJob( Boolean isSeasonalJob )
	{
		super.putInternal( Field.SUMMER, isSeasonalJob );
	}

	public Boolean isNonProfit()
	{
		return getBoolean( Field.NONPROFIT );
	}

	public void setNonProfit( Boolean isNonProfit )
	{
		super.putInternal( Field.NONPROFIT, isNonProfit );
	}

	public Integer getPayType()
	{
		return getInteger( Field.PAY_TYPE );
	}

	public void setPayType( Integer payType )
	{
		super.putInternal( Field.PAY_TYPE, payType );
	}

	public String getPayTypeDescription()
	{
		return PAY_TYPE[getPayType()];
	}

	@Override
	protected Field fieldForName( String fieldName )
	{
		return Field.valueOf( fieldName.toUpperCase() );
	}

	public enum Field implements FacebookField
	{
		LISTING_ID, URL, TITLE, DESCRIPTION, POSTER, UPDATE_TIME, CATEGORY,
		SUBCATEGORY, IMAGE_URLS, PRICE, CONDITION, ISBN, NUM_BEDS, DOGS, CATS,
		SMOKING, SQUARE_FOOTAGE, STREET, CROSSSTREET, POSTAL, RENT, NUM_BATHS,
		PAY, FULL, INTERN, SUMMER, NONPROFIT, PAY_TYPE;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}
}
