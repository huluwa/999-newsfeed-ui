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

import oms.sns.service.facebook.client.FacebookField;
import oms.sns.service.facebook.client.FacebookObject;

/**
 * @author Mino Togna
 * @author Gino Miceli
 */
public class EventLocation 
{
   public String city ;
   public String state;
   public String country;
   public String street;
   public String latitude;
   public String longitude;
   
   public String getCity() {
	return city;
   }
   
   public void setCity(String city) {
	this.city = city;
   }
   
   public String getState() {
	return state;
   }
   
   public void setState(String state) {
	this.state = state;
   }
   
   public String getCountry() {
	return country;
   }
   
   public void setCountry(String country) {
	this.country = country;
  }
   
   public String getStreet() {
	return street;
   }
   
   public void setStreet(String street) {
	this.street = street;
   }
   
   public String getLatitude() {
	return latitude;
  }
   
   public void setLatitude(String latitude) {
	this.latitude = latitude;
   }
   
   public String getLongitude() {
	return longitude;
   }
   
   public void setLongitude(String longitude) {
	this.longitude = longitude;
   }
   
	public enum Field 
	{
		CITY, STATE, COUNTRY, STREET, LATITUDE, LONGITUDE;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}
}
