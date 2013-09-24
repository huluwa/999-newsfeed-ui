package com.borqs.omshome25;

import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Align;
import android.text.TextPaint;
import android.util.Log;

public class CalendarReceiver extends BroadcastReceiver {
    private static final String TAG = "Launcher.CalendarRecevier";
	private Calendar mCalendar;
    private CharSequence[] monthTexts;
    
    private TextPaint monthPaint;
    private TextPaint dayPaint;
    
    private ComponentName calendarComponentName;
    private Calendar lastDay;
    
    private static int sIconWidth = -1;
    private static int sIconHeight = -1;
    
    private static float sMonthSize = -1;
    private static float sDaySize = -1;
    
    private static int sX1 = -1;
    private static int sX2 = -1;
    private static int sY1 = -1;
    private static int sY2 = -1;
    
	public CalendarReceiver() {        
        monthPaint = new TextPaint();
        monthPaint.setColor(Color.WHITE);
        monthPaint.setAntiAlias(true);
        monthPaint.setTextAlign(Align.CENTER);
		
        dayPaint = new TextPaint();
        dayPaint.setColor(Color.BLACK);
        dayPaint.setAntiAlias(true);
        dayPaint.setFakeBoldText(true);
        dayPaint.setTextAlign(Align.CENTER);
        dayPaint.setAlpha(200);
        
        mCalendar = Calendar.getInstance();        
        
        String monthColor = "#FFFFFFFF";
        String dateColor = "#FF000000"; 
        try {
            FileInputStream fis = new FileInputStream("/opl/etc/calendar_color_config.xml");
            Properties mProp = new Properties();
            mProp.loadFromXML(fis);
            monthColor = mProp.getProperty("Month","#FFFFFFFF");
            dateColor = mProp.getProperty("Date","#FF000000");
          } catch (Exception e) {
              e.printStackTrace();
          }finally{
              monthPaint.setColor(Color.parseColor(monthColor));
              dayPaint.setColor(Color.parseColor(dateColor));    
              mCalendar = Calendar.getInstance();
          }
	}

	@Override
	public void onReceive(Context context, Intent intent) {
//	    if(Launcher.LOGD)Log.d(TAG, "CalendarRecevier action is "+intent.getAction());
		if (sIconWidth == -1) {
            final Resources resources = context.getResources();
            sIconWidth = sIconHeight = (int) resources.getDimension(R.dimen.app_icon_size);
            
            sMonthSize = resources.getDimension(R.dimen.month_size);

            sDaySize = resources.getDimension(R.dimen.day_size);

            sX1 = (int) resources.getDimension(R.dimen.x_coordinate_1);
            sX2 = (int) resources.getDimension(R.dimen.x_coordinate_2);
            
            sY1 = (int) resources.getDimension(R.dimen.y_coordinate_1);
            sY2 = (int) resources.getDimension(R.dimen.y_coordinate_2);
            
//            Log.d(TAG,"onReceive  sX1:"+sX1+" sX2:"+sX2+ " sMonthSize:"+sMonthSize+"  context:"+context+"  ");
        }
		
		monthPaint.setTextSize(sMonthSize);
		dayPaint.setTextSize(sDaySize);
		 
        monthTexts = context.getResources().getTextArray(R.array.months);

        Intent calendarIntent = new Intent(Intent.ACTION_MAIN);
      
        calendarComponentName = new ComponentName("com.android.calendar", "com.android.calendar.MonthActivity");
        calendarIntent.setComponent(calendarComponentName);
        
        if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
            String tz = intent.getStringExtra("time-zone");
            mCalendar.setTimeZone(TimeZone.getTimeZone(tz));
        }

        long time = System.currentTimeMillis();
        mCalendar.setTimeInMillis(time);
        
    	if (intent.getAction().equals(Launcher.ACTION_ITEM_ADDED)){
    		Intent launcherIntent = intent.getParcelableExtra(Launcher.EXTRA_ITEM_LAUNCH_INTENT);    
    		if(launcherIntent != null && launcherIntent.getComponent().getClassName().equals(Workspace.CALENDAR_CLASS_NAME)){
    			onTimeChanged(context, calendarIntent);
    		}
//    		if(launcherIntent != null && calendarIntent.filterEquals(launcherIntent)){
//                onTimeChanged(context, calendarIntent);
//            }
    	} else if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
    	    if(lastDay == null || mCalendar.get(Calendar.YEAR) != lastDay.get(Calendar.YEAR)
    	    		||mCalendar.get(Calendar.DAY_OF_YEAR) != lastDay.get(Calendar.DAY_OF_YEAR)) {
                onTimeChanged(context, calendarIntent);
    	    }
    	} else if (intent.getAction().equals(Launcher.ACTION_LOAD_COMPLETE)) {
    		IntentFilter calFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
			context.registerReceiver(this, calFilter);
			
    		onTimeChanged(context, calendarIntent);
    	} else {
    		onTimeChanged(context, calendarIntent);
    	}
	}

    private void onTimeChanged(Context context, Intent calendarIntent) {        
        if(Launcher.LOGD)Log.d(TAG,"onTimeChanged");
        final String month = monthTexts[mCalendar.get(Calendar.MONTH)].toString();
        final int monthDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        final String day = ((monthDay < 10)?"0":"") + Integer.toString(monthDay);
        
        Bitmap bitmap = Bitmap.createBitmap(sIconWidth, sIconHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(month, sX1, sY1, monthPaint);
        canvas.drawText(day, sX2, sY2, dayPaint);
        
		Intent countNumIntent = new Intent(Launcher.ACTION_UPDATE_CALENDAR);
		countNumIntent.putExtra(Launcher.SHORTCUT_SYMBOL_BITMAP, bitmap);
		context.sendBroadcast(countNumIntent);
		
		if(lastDay == null) {
		    lastDay = Calendar.getInstance();
		}
		
		lastDay.set(Calendar.YEAR, mCalendar.get(Calendar.YEAR));
        lastDay.set(Calendar.DAY_OF_YEAR, mCalendar.get(Calendar.DAY_OF_YEAR));
    }

    public static void clearStaticData(){
	    sIconWidth = -1;
	    sIconHeight = -1;
	    sMonthSize = -1;
	    sDaySize = -1;
	 
	    sX1 = -1;
	    sX2 = -1;
	    sY1 = -1;
	    sY2 = -1;
    }
}
