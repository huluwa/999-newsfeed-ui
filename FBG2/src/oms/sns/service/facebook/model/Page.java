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
 * @author Mino Togna
 * 
 */
public class Page
{
   public int id;  // stands for android databases _id
   public long page_id;
   public String name;
   public String page_url;
   public String pic_small;
   public String pic_big;
   public String pic_square;
   public String pic;
   public String pic_large;
   public String type;
   public String website;
   public Boolean hasaddapp;
   public String founded;
   public String company_overview;
   public String mission;
   public String products;
   public String location;
   public String street;
   public String hours;
   public String bio;
   public String band_members;
   public String home_town;
   public String genre;
   
	public static class Field
	{
		public static final String PAGE_ID="page_id";
		public static final String NAME            ="name";
		public static final String PAGE_URL        ="page_url";
		public static final String PIC_SMALL       ="pic_small";
		public static final String PIC_BIG         ="pic_big";
		public static final String PIC_SQUARE      ="pic_square";
		public static final String PIC             ="pic";
		public static final String PIC_LARGE       ="pic_large";
		public static final String TYPE            ="type";		
		public static final String WEBSITE         ="website";
		public static final String HAS_ADDED_APP   ="has_added_app";
		public static final String FOUNDED         ="founded";
		public static final String COMPANY_OVERVIEW="company_overview";
		public static final String MISSION         ="mission";
		public static final String PRODUCTS        ="products";
		public static final String LOCATION        ="location";
		public static final String PARKING         ="parking"; 
		public static final String PUBLIC_TRANSIT  = "public_transit";
		public static final String HOURS  		   = "hours"; 
		public static final String ATTIRE          = "attire";
		public static final String PAYMENT_OPTIONS = "payment_options";
		public static final String CULINARY_TEAM   = "culinary_team";
		public static final String GENERAL_MANAGER = "general_manager";
		public static final String PRICE_RANGE     = "price_range";
		public static final String RESTAURANT_SERVICES    = "restaurant_services";
		public static final String RESTAURANT_SPECIALTIES = "restaurant_specialties";
		public static final String RELEASE_DATE   = "release_date";
		public static final String GENRE          = "genre";
		public static final String STARRING       = "starring";
		public static final String SCREENPLAY_BY  = "screenplay_by";
		public static final String DIRECTED_BY    = "directed_by";
		public static final String PRODUCED_BY    = "produced_by";
		public static final String STUDIO         = "studio";
		public static final String AWARDS         = "awards";
		public static final String PLOT_OUTLINE   = "plot_outline";
		public static final String NETWORK        = "network";
		public static final String SEASON         = "season";
		public static final String SCHEDULE       = "schedule";
		public static final String WRITTEN_BY     = "written_by";
		public static final String BAND_MEMBERS   = "band_members";
		public static final String HOMETOWN       = "hometown";
		public static final String CURRENT_LOCATION ="current_location";
		public static final String RECORD_LABEL     = "record_label";
		public static final String BOOKING_AGENT    = "booking_agent";
		public static final String ARTISTS_WE_LIKE  = "artists_we_like";
		public static final String INFLUENCES       = "influences";
		public static final String BAND_INTERESTS   = "band_interests";
		public static final String BIO              = "bio";
		public static final String AFFILIATION      = "affiliation";
		public static final String BIRTHDAY         = "birthday";
		public static final String PERSONAL_INFO    = "personal_info";
		public static final String PERSONAL_INTERESTS = "personal_interests";
		public static final String MEMBERS            = "members";
		public static final String BUILT              = "built";
		public static final String FEATURES           = "features";
		public static final String MPG                = "mpg";
		public static final String GENERAL_INFO       = "general_info";
	}

	public void despose() {
				
	}
}
