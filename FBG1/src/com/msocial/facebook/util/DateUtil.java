package com.msocial.facebook.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.msocial.facebook.R;

public class DateUtil {  
    
    public static boolean isCurrentWeek(long time)
    {
        Date date = new Date(time);
        return isCurrentWeek(date);
    }
    static String[] weekArray;
    static Object lock = new Object();
    public static String[] getWeekArray(Context con)
    {
        synchronized(lock)
        {
            if(weekArray == null)
            {
                weekArray = con.getResources().getStringArray(R.array.entries_week);
            }
        }
        return weekArray;
    }
    public static boolean isCurrentMonth(long time)
    {
        Date date = new Date(time);
        return isCurrentMonth(date);
    }
    
    public static boolean isCurrentWeek(Date date)
    {
        if(isCurrentMonth(date))
        {
            Date now = new Date();
            Calendar ca = Calendar.getInstance(Locale.getDefault());
            ca.setTime(now);
            int curWeek = ca.get(Calendar.WEEK_OF_MONTH);
            ca.setTime(date);
            int parWeek = ca.get(Calendar.WEEK_OF_MONTH);
            return (curWeek==parWeek);
        }
        else
        {
            return false;
        }
        
    }
    
    public static boolean isCurrentMonth(Date date)
    {
        Date now = new Date();
        if(isCurrentYear(date))
        {
             Calendar ca = Calendar.getInstance(Locale.getDefault());
             ca.setTime(now);
             int curMonth = ca.get(Calendar.MONTH);
             ca.setTime(date);
             int parMonth = ca.get(Calendar.MONTH);
             return (curMonth==parMonth);
        }
        else
        {
            return false;
        }
    }
    
    private static boolean isCurrentYear(Date date)
    {
        Date now = new Date();
        return (now.getYear()==date.getYear());
    }
    
    private static boolean isToday(Date date)
    {  
       return DateUtils.isToday(date.getTime());
    }
    
    private static boolean isYesterday(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, +1);
        return DateUtils.isToday(calendar.getTimeInMillis());
    }
    
    public static String converToRelativeTime(Context con,Date date)
    {
        long curtime = System.currentTimeMillis();
        long paramtime = date.getTime();       
        long interval = curtime-paramtime;       
        return calculateTime(con,paramtime,interval);
    }

    public static String converToRelativeTime(Context con,long time)
    {
        long curtime = System.currentTimeMillis();
        long paramtime = time;       
        long interval = curtime-paramtime;       
        return calculateTime(con,paramtime,interval);
    }
    
    public static String converToRelativeTime(Context con,long time,boolean withoutsecond)
    {
        long curtime = System.currentTimeMillis();
        long paramtime = time;       
        long interval = curtime-paramtime;       
        return calculateTime(con,paramtime,interval,withoutsecond);
    }
    
    private static String calculateTime(Context con,long realtime,long interval,boolean withoutsecond)
    {
        if(withoutsecond == false)
        {
            return calculateTime(con,realtime,interval);
        }
        
        String str = "";
        Date realDate = new Date(realtime);
        Date today = new Date();
        if(interval >0 && interval < 60*1000L)
        {
            str = Long.toString(interval/1000L)+ " " + con.getString(R.string.sec);
        }
        else if(interval >=60*1000L && interval <2*60*1000L)
        {
            //>= 1min <2min about a minute ago
            str = con.getString(R.string.facebook_time_tracte_min);
        }
        else if( interval >= 2*60*1000L && interval <60*60*1000L)
        {
            //>2 mins <1 hour ? minutes ago
            str = Long.toString(interval/(60*1000L))+" "+con.getString(R.string.min);
        }
        else if(interval >=60*60*1000L && interval < 2*60*60*1000L)
        {
            //>=1hour <2 hour about an hour ago
            str = con.getString(R.string.facebook_time_tracte_hour);
        }
        else if(interval >=2*60*60*1000L && interval < 12*60*60*1000L)
        {
            str = Long.toString(interval/(60*60*1000L))+ " " +String.format(con.getString(R.string.hour),getamORpm(realDate));
        }
        else if(isToday(realDate))
        { 
            //today
            str = con.getString(R.string.facebook_time_tracte_td)+" "+ getamORpm(realDate);
        }
        else if(isYesterday(realDate))
        {
            str = con.getString(R.string.facebook_time_tracte_yd)+" "+getamORpm(realDate);
        }
        else if(isCurrentWeek(realDate))
        {
            //is current week
            String[] array = getWeekArray(con);
            Calendar ca = Calendar.getInstance(Locale.getDefault());
            ca.setTime(realDate);
            int week_index = ca.get(Calendar.DAY_OF_WEEK);
            str = array[week_index-1]+" "+getamORpm(realDate);
        }
        else if(isCurrentYear(realDate))
        {  
            //Mar 21 at 2:23pm
            str = getMonthDate(realDate,con) + getamORpm(realDate);  
        }
        else 
        {
            DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT,Locale.getDefault());
            Date realdate = new Date(realtime);
            str = format.format(realdate);
        }
        
        return str;
    }
    
    
    private static String calculateTime(Context con,long realtime,long interval)
    {
        String str = "";
        Date realDate = new Date(realtime);
        Date today = new Date();
        if(interval >0 && interval < 60*1000L)
        {
            str = Long.toString(interval/1000L)+ " " + con.getString(R.string.sec);
        }
        else if(interval >=60*1000L && interval <2*60*1000L)
        {
            //>= 1min <2min about a minute ago
            str = con.getString(R.string.facebook_time_tracte_min);
        }
        else if( interval >= 2*60*1000L && interval <60*60*1000L)
        {
            //>2 mins <1 hour ? minutes ago
            str = Long.toString(interval/(60*1000L))+" "+con.getString(R.string.min);
        }
        else if(interval >=60*60*1000L && interval < 2*60*60*1000L)
        {
            //>=1hour <2 hour about an hour ago
            str = con.getString(R.string.facebook_time_tracte_hour);
        }
        else if(interval >=2*60*60*1000L && interval < 12*60*60*1000L)
        {
            //>2 hours < 12 hours ? hours ago
            str = Long.toString(interval/(60*60*1000L))+ " " +String.format(con.getString(R.string.hour),getamORpm(realDate));
        }
        else if(isToday(realDate))
        { 
            //today
            str = con.getString(R.string.facebook_time_tracte_td)+" "+ getamORpm(realDate);
        }
        else if(isYesterday(realDate))
        {
            str = con.getString(R.string.facebook_time_tracte_yd)+" "+ getamORpm(realDate);
        }
        else if(isCurrentWeek(realDate))
        {
            //is current week
            String[] array = getWeekArray(con);
            Calendar ca = Calendar.getInstance(Locale.getDefault());
            ca.setTime(realDate);
            int week_index = ca.get(Calendar.DAY_OF_WEEK);
            str = array[week_index-1]+ " " +getamORpm(realDate);
        }
        else if(isCurrentYear(realDate))
        {  
            //Mar 21 at 2:23pm
            str = getMonthDate(realDate,con) + getamORpm(realDate);  
        }
        else 
        {
            DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT,Locale.getDefault());
            Date realdate = new Date(realtime);
            str = format.format(realdate);
        }    
        return str;
    }
    
    private static String getMonthDate(Date realdate,Context con)
    {
        if(Locale.CHINESE.getDisplayName().equals(Locale.getDefault().getDisplayName()))
        {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd ",Locale.getDefault());
            return sdf.format(realdate);
        }
        else
        {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d ",Locale.getDefault());
            return sdf.format(realdate) + con.getString(R.string.facebook_time_tracte_at)+" ";    
        }  
    }
    
    private static String getamORpm(Date realdate)
    {
        DateFormat format = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        return format.format(realdate);
    }
    public static String calculateCurTimeForNote()
    {
        Date realdate = new Date();
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT,Locale.getDefault());
        String str = format.format(realdate);
        return str;
    }
    
    public static int getPSTDate(Date date)
    {
    	Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"), Locale.US);
    	calendar.setTime(date);
    	int day = calendar.get(Calendar.DATE);
    	return day;
    }
    
    
    public static long getCurrentTimeForEvent()
    {
        SimpleDateFormat objSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateStr = objSdf.format(new Date());
        objSdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        long now = System.currentTimeMillis();
        try{
            now = objSdf.parse(dateStr).getTime();
        }catch(Exception e)
        {
            
        }
        
        return now;
    }
}
