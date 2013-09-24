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
 * 
 * @author Gino Miceli
 * @author Mino Togna
 */
public class UserInfo extends FacebookObject<UserInfo.Field>
{
	private static final long serialVersionUID = 1L;

	public Long getId()
	{
		return getLong( Field.UID );
	}

	public String getName()
	{
		return getString( Field.NAME );
	}

	public String getAboutMe()
	{
		return getString( Field.ABOUT_ME );
	}

	public String getActivities()
	{
		return getString( Field.ACTIVITIES );
	}
	
	public String getEmail(){
		return getString(Field.EMAIL);
	}
	
	public String getCell(){
		return getString(Field.CELL);
	}

	public NetworkAffiliation[] getNetworkAffiliations()
	{
		return (NetworkAffiliation[]) get( Field.AFFILIATIONS );
	}

	public String getBirthday()
	{
		return getString( Field.BIRTHDAY );
	}

	public String getFavoriteBooks()
	{
		return getString( Field.BOOKS );
	}

	public UserLocation getCurrentLocation()
	{
		return (UserLocation) get( Field.CURRENT_LOCATION );
	}

	public String getFirstName()
	{
		return getString( Field.FIRST_NAME );
	}

	public Boolean hasAddedApp()
	{
		return (Boolean) get( Field.HAS_ADDED_APP );
	}

	public EducationInfo[] getEducationHistory()
	{
		return (EducationInfo[]) get( Field.EDUCATION_HISTORY );
	}

	public HometownLocation getHometown()
	{
		return (HometownLocation) get( Field.HOMETOWN_LOCATION );
	}

	public HighSchoolInfo getHighSchoolInfo()
	{
		return (HighSchoolInfo) get( Field.HS_INFO );
	}

	public String getInterests()
	{
		return getString( Field.INTERESTS );
	}

	public Boolean isAppUser()
	{
		return getBoolean( Field.IS_APP_USER );
	}

	public String getLastName()
	{
		return getString( Field.LAST_NAME );
	}

	public String[] getMeetingFor()
	{
		return getStringArray( Field.MEETING_FOR );
	}

	public String[] getMeetingSex()
	{
		return getStringArray( Field.MEETING_SEX );
	}

	public String getFavoriteMovies()
	{
		return getString( Field.MOVIES );
	}

	public String getFavoriteMusic()
	{
		return getString( Field.MUSIC );
	}

	public Integer getNotesCount()
	{
		return getInteger( Field.NOTES_COUNT );
	}

	public String getPic()
	{
		return getString( Field.PIC );
	}

	public String getPicBig()
	{
		return getString( Field.PIC_BIG );
	}

	public String getPicSmall()
	{
		return getString( Field.PIC_SMALL );
	}

	public String getPicSquare()
	{
		return getString( Field.PIC_SQUARE );
	}

	public String getPoliticalViews()
	{
		return getString( Field.POLITICAL );
	}

	public Date getProfileUpdateTime()
	{
		return getDate( Field.PROFILE_UPDATE_TIME );
	}

	public String getFavoriteQuotes()
	{
		return getString( Field.QUOTES );
	}

	public String getRelationshipStatus()
	{
		return getString( Field.RELATIONSHIP_STATUS );
	}

	public String getReligion()
	{
		return getString( Field.RELIGION );
	}

	public Long getSignificantOtherId()
	{
		return getLong( Field.SIGNIFICANT_OTHER_ID );
	}

	// TODO ?: To enum
	public String getSex()
	{
		return getString( Field.SEX );
	}

	public UserStatus getStatus()
	{
		return (UserStatus) get( Field.STATUS );
	}

	public Integer getTimezone()
	{
		return getInteger( Field.TIMEZONE );
	}

	public String getFavoriteTvShows()
	{
		return getString( Field.TV );
	}

	public Integer getWallCount()
	{
		return getInteger( Field.WALL_COUNT );
	}

	public WorkInfo[] getWorkHistory()
	{
		return (WorkInfo[]) get( Field.WORK_HISTORY );
	}

	@Override
	protected Field fieldForName( String fieldName )
	{
		return Field.valueOf( fieldName.toUpperCase() );
	}

	public enum Field implements FacebookField
	{
		UID, FIRST_NAME, LAST_NAME, NAME, SCREENNAME, PIC_SMALL, PIC_BIG, PIC_SQUARE, PIC, 
		AFFILIATIONS, PROFILE_UPDATE_TIME, TIMEZONE, RELIGION, BIRTHDAY, SEX, CELL,EMAIL, PHONE, HOMETOWN_LOCATION, 
		MEETING_SEX, MEETING_FOR, RELATIONSHIP_STATUS, SIGNIFICANT_OTHER_ID, POLITICAL, 
		CURRENT_LOCATION, ACTIVITIES, INTERESTS, IS_APP_USER, MUSIC, TV, MOVIES, BOOKS, QUOTES, 
		ABOUT_ME, HS_INFO, EDUCATION_HISTORY, WORK_HISTORY, NOTES_COUNT, WALL_COUNT, STATUS, HAS_ADDED_APP;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}
	}
}
