/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package oms.sns.main.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.view.Menu;
import android.view.MenuItem;


public class MenuHelper {
    private static final int MENU_GROUP_AGENDA = 1;
    private static final int MENU_GROUP_DAY = 2;
    private static final int MENU_GROUP_WEEK = 3;
    private static final int MENU_GROUP_MONTH = 4;
    private static final int MENU_GROUP_EVENT_CREATE = 5;
    private static final int MENU_GROUP_TODAY = 6;
    private static final int MENU_GROUP_SELECT_CALENDARS = 7;
    private static final int MENU_GROUP_PREFERENCES = 8;
    private static final int MENU_GROUP_MEETING_CREATE = 9;

    public static final int MENU_GOTO_TODAY = 1;
    public static final int MENU_AGENDA = 2;
    public static final int MENU_DAY = 3;
    public static final int MENU_WEEK = 4;
    public static final int MENU_EVENT_VIEW = 5;
    public static final int MENU_EVENT_CREATE = 6;
    public static final int MENU_EVENT_EDIT = 7;
    public static final int MENU_EVENT_DELETE = 8;
    public static final int MENU_MONTH = 9;
    public static final int MENU_SELECT_CALENDARS = 10;
    public static final int MENU_PREFERENCES = 11;
    public static final int MENU_MEETING = 12;
    
    public static final String ISMEETING = "isMeeting";
   
    public static final int MONTH_VIEW = 0;
    public static final int WEEK_VIEW = 1;
    public static final int DAY_VIEW = 2;
    public static final int AGENDA_VIEW =3; 
    
    public static void onPrepareOptionsMenu(Activity activity, Menu menu) 
    {        
        if (activity instanceof TwitterTweetsActivity) {
            menu.setGroupVisible(MENU_GROUP_TODAY, false);
            menu.setGroupEnabled(MENU_GROUP_TODAY, false);
        } else {
            menu.setGroupVisible(MENU_GROUP_TODAY, true);
            menu.setGroupEnabled(MENU_GROUP_TODAY, true);
        }
    }

    public static boolean onCreateOptionsMenu(Menu menu,Context context) 
    {
        /*
        MenuItem item;        
        //trac 5467,  UI consistency
        item = menu.add(MENU_GROUP_EVENT_CREATE, MENU_EVENT_CREATE, 0, R.string.event_create);
        item.setIcon(android.R.drawable.ic_menu_add);
        item = menu.add(MENU_GROUP_MEETING_CREATE, MENU_MEETING, 0,
            	R.string.menu_meeting);
            	item.setIcon(android.R.drawable.ic_menu_add);
        
        item = menu.add(MENU_GROUP_TODAY, MENU_GOTO_TODAY, 0, R.string.goto_today);
        item.setIcon(android.R.drawable.ic_menu_today);
        //item.setAlphabeticShortcut('t');
        
        item = menu.add(MENU_GROUP_SELECT_CALENDARS, MENU_SELECT_CALENDARS,
                0, R.string.menu_select_calendars);
        item.setIcon(android.R.drawable.ic_menu_manage);
        
        item = menu.add(MENU_GROUP_PREFERENCES, MENU_PREFERENCES, 0, R.string.menu_preferences);
        item.setIcon(android.R.drawable.ic_menu_preferences);
        //item.setAlphabeticShortcut('p');
        */
        return true;
    }

    /*
    public static boolean onOptionsItemSelected(Activity activity, MenuItem item, Navigator nav) {
        switch (item.getItemId()) {
        case MENU_SELECT_CALENDARS: {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(activity, SelectCalendarsActivity.class);
            activity.startActivity(intent);
            return true;
        }
        case MENU_GOTO_TODAY:
            nav.goToToday();
            return true;
        case MENU_PREFERENCES:
            switchTo(activity, CalendarPreferenceActivity.class.getName(), nav.getSelectedTime());
            return true;
        case MENU_AGENDA:
            switchTo(activity, AgendaActivity.class.getName(), nav.getSelectedTime());
            activity.finish();
            return true;
        case MENU_DAY:
            switchTo(activity, DayActivity.class.getName(), nav.getSelectedTime());
            activity.finish();
            return true;
        case MENU_WEEK:
            switchTo(activity, WeekActivity.class.getName(), nav.getSelectedTime());
            activity.finish();
            return true;
        case MENU_MONTH:
            switchTo(activity, MonthActivity.class.getName(), nav.getSelectedTime());
            activity.finish();
            return true;
        case MENU_MEETING: {
        	Intent intentMeeting = new Intent(Intent.ACTION_EDIT);
        	intentMeeting.putExtra(ISMEETING, MeetingStatus.IS_A_MEETING);
        	intentMeeting.setClassName(activity, EditEvent.class.getName());
        	activity.startActivity(intentMeeting);
        	return true;
        }
        case MENU_EVENT_CREATE: {
            long startMillis = nav.getSelectedTime();
            long endMillis = startMillis + DateUtils.HOUR_IN_MILLIS;
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setClassName(activity, EditEvent.class.getName());
            intent.putExtra(EVENT_BEGIN_TIME, startMillis);
            intent.putExtra(EVENT_END_TIME, endMillis);
            intent.putExtra(EditEvent.EVENT_ALL_DAY, nav.getAllDay());
            intent.putExtra(ISMEETING, MeetingStatus.IS_NOT_A_MEETING);
            activity.startActivity(intent);
            return true;
        }
        }
        return false;
    }
    
     public static boolean ontitleItemSelected(Activity activity,int postion ,long id ,Navigator nav) {
        switch(postion) {
         case MONTH_VIEW:
             switchTo(activity, MonthActivity.class.getName(), nav.getSelectedTime());
             activity.finish();
             return true;
         case WEEK_VIEW:
             switchTo(activity, WeekActivity.class.getName(), nav.getSelectedTime());
             activity.finish();
             return true; 
         case DAY_VIEW:
             switchTo(activity, DayActivity.class.getName(), nav.getSelectedTime());
             activity.finish();
             return true;
         case AGENDA_VIEW:
            switchTo(activity, AgendaActivity.class.getName(), nav.getSelectedTime());
            activity.finish();
            return true;
       }
           return false; 
    }
 
      
    
    static void switchTo(Activity activity, String className, long startMillis) {
        long mTime = 0;
        if(startMillis == 0){
          mTime  = System.currentTimeMillis(); 
        }else {
          mTime = startMillis;  
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName(activity, className);
        intent.putExtra(EVENT_BEGIN_TIME, mTime);
        activity.startActivity(intent);
    }*/
}
