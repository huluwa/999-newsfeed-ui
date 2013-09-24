package com.msocial.facebook.ui.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.msocial.facebook.R;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.providers.SocialORM.FacebookeventCol;
import com.msocial.facebook.ui.FacebookEventActivity;
import com.msocial.facebook.ui.FacebookStreamActivity;
import com.msocial.facebook.ui.view.FacebookEventItemView;
import com.msocial.facebook.util.DateUtil;
import oms.sns.service.facebook.model.Event;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class FacebookEventAdapter extends BaseAdapter {

	private final String TAG = "FacebookEventAdapter";        
    private Context mContext;
    //for event guest
    private List<Event> eventItems;
    private boolean  useCursor;
    
    private Cursor   mCursor;    
    private int[]    mItemMap;    
    private boolean withFooterView = false;
    
    private int firstweekpos =  -1;
    private int firstmonthpos = -1;
    private int firstotherpos = -1;
    private int upcomingpos   = -1;
    private int pastpos       = -2;
    private int upcomingIndex = 0;
    private int pastIndex = 0;
    
    public FacebookEventAdapter(Context con,  List<Event>events)
    {
    	mContext = con;
    	eventItems = events; 
    	Log.d(TAG, "create FacebookEventAdapter");
    }
    
    public FacebookEventAdapter(Context con,Cursor totalEvent,boolean withFooterView, boolean simgleMode)
    {
         this.withFooterView = withFooterView;
         useCursor = true;        
         mCursor = totalEvent;
         
         buildMap(simgleMode);
         mContext = con;
         Log.d(TAG, "create FacebookEventAdapter for cursor mode");
    }
    
    int realItemCount = 0;
    public void buildMap(boolean simgleMode)
    {
        if(simgleMode == true)
        {
            buildMapUpcomming();
        }
        else
        {
            buildMapThree();
        }
    }
    
    public void buildMapThree() 
    {
        ArrayList<Integer> cur_week = new ArrayList<Integer>();
        ArrayList<Integer> cur_month = new ArrayList<Integer>();
        ArrayList<Integer> others     = new ArrayList<Integer>();
      
        int eventIndex = -1;
        if (mCursor.moveToFirst() && mCursor.getCount() > 0) 
        {
            long now = System.currentTimeMillis();
            
            int itemIndex = 0;
            while (!mCursor.isAfterLast()) 
            {
                if(useCursor)
                {
                    long start = mCursor.getLong(mCursor.getColumnIndex(FacebookeventCol.STARTTIME));
                    if(DateUtil.isCurrentWeek(start))
                    {
                        //TODO put into Week box
                        cur_week.add(mCursor.getPosition());
                    }
                    else if(DateUtil.isCurrentMonth(start))
                    {
                        //TODO put into Month box
                        cur_month.add(mCursor.getPosition());
                    }
                    else
                    {
                        //TODO put into others
                        others.add(mCursor.getPosition());
                    }
                   
                }
                itemIndex++;
                mCursor.moveToNext();
            }
            if(withFooterView)
            {
               //array[itemIndex] = -3;
               itemIndex++;
            }
           
             realItemCount = itemIndex;
        }
        mergArrayListToItemMap(cur_week,cur_month,others);
        cur_week.clear();
        cur_week = null;
        
        cur_month.clear();
        cur_month = null;
        
        others.clear();
        others = null;  
    }
    
    private void mergArrayListToItemMap(ArrayList<Integer> cur_week,ArrayList<Integer> cur_month,ArrayList<Integer> others)
    {  
        mItemMap = new int[realItemCount];
        Log.d(TAG,"mItemMap size is =="+ realItemCount+" this week size is "+cur_week.size()+
              " mont size is "+cur_month.size()+" other size is "+others.size());
        if(mItemMap!=null && mItemMap.length>0)
        {
            int i=0;
            for(int j=0;j<cur_week.size();j++)
            {
                if(i < realItemCount) 
                { 
                    mItemMap[i] = cur_week.get(j);
                    if(j ==0 )
                    {
                        firstweekpos = mItemMap[i];
                    }
                    i++;
                }
                    
            }
            
            for(int k=0;k<cur_month.size();k++)
            {
                if(i < realItemCount) 
                {
                    mItemMap[i] = cur_month.get(k);
                    if(k ==0 )
                    {
                        firstmonthpos = mItemMap[i];
                    }
                    i++;
                }
            }
            
            for(int m=0;m<others.size();m++)
            {
                if(i < realItemCount) 
                {
                    Log.d(TAG," i = "+i +" m = "+m);
                    mItemMap[i] = others.get(m);
                    if(m==0)
                    {
                         firstotherpos = mItemMap[i];
                    }
                    i++;
                }
            } 
            
            if(withFooterView && (i+1)== realItemCount)
            {
               mItemMap[i] = -3; // footerview
            }
        }
       
    }

	private void buildMapUpcomming() 
    {
	    int realItemCount = 0;
        int n = mCursor.getCount();
        if(useCursor == true)
        {
            if(withFooterView==true && n>0)
            {
                n += 1;
            }
//            else
//            {
//                n += 2;
//            }
        }
        int array[] = new int[n];
        int eventIndex = -1;
        if (mCursor.moveToFirst() && mCursor.getCount() > 0) 
        {
            long now = DateUtil.getCurrentTimeForEvent();
            int itemIndex = 0;
            while (!mCursor.isAfterLast()) 
            {
                if(useCursor)
                {
                    long end = mCursor.getLong(mCursor.getColumnIndex(FacebookeventCol.STARTTIME));
                    int index = (end>now?0:1);
                    if (index > eventIndex) {                        
                        eventIndex = index;
                       // array[itemIndex] = eventIndex - 2;
                       // itemIndex++;
                        if(eventIndex == 0)
                        {  
                            upcomingpos = mCursor.getPosition();
                            upcomingIndex = itemIndex;
                        }
                        else if(eventIndex == 1)
                        {
                            pastpos = mCursor.getPosition();     
                            pastIndex = itemIndex;
                        }               
                    }
                }
                array[itemIndex] = mCursor.getPosition();
                itemIndex++;
                mCursor.moveToNext();
            }
            
            if(withFooterView && n >0)
            {
               array[itemIndex] = -3;
               itemIndex++;
            }
           
             realItemCount = itemIndex;
        }
        else 
        {
            // The db is empty, just add the heading for the first item
//            eventIndex = 0;
//            if(useCursor)
//            {
//                array[0] = eventIndex - 2;
//                array[1] = eventIndex - 1;
//                realItemCount = 2;
//            }
//            else
//            {
//                if(n > 0)
//                {
//                    array[0] = -1;
//                }
//            }
        } 
        
        mItemMap = new int[realItemCount];
      
       for(int i=0;i<realItemCount;i++)
       {
           if(i<pastIndex)
           {
               mItemMap[i] = array[pastIndex-i-1];
               array[pastIndex-i-1] = 0;
               if(i == 0)
               {
                   upcomingpos = mItemMap[0];
               }         
           }
           else
           {
               mItemMap[i] = array[i];
               array[i] = 0;
           } 
       }
        array = null;
    }
	 


  
    public int getCount() {
        Log.d(TAG,"count is "+mItemMap.length);
		if(useCursor == false)
		    return eventItems.size();
		else		    
			return  getCountForCursor();
	}
	
    public int getCountForCursor() 
    {
        return mItemMap.length;
    }
    
    public Object getItemForCursor(int pos) 
    {       
        if(mItemMap[pos] < 0)
        {
            return null;
        }
        else
        {
            if(mCursor != null && mCursor.moveToPosition(mItemMap[pos]))
            {
                return SocialORM.instance(mContext).formatEvent(mCursor);
            }
            else
            {
                return null;
            }
        }
    }
    
    public Object getItem(int pos)
    {
		if(useCursor == false)
		{
		    return eventItems.get(pos);
		}
		else
		{
		   return getItemForCursor(pos);
		}
	}

	public long getItemId(int pos) 
	{
		if(useCursor == false)
		{
		    return eventItems.get(pos).eid;
		}
		else
		{
		    return mItemMap[pos];			
		}
	}
	
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
		 }
		 if(mItemMap[position] == -3)
		 {
		     //create footerview
		     Button but = new Button(mContext);
             but.setTextAppearance(mContext, R.style.sns_load_old);
             but.setBackgroundColor(Color.WHITE);              
             but.setText(mContext.getString(R.string.load_more_msg));
             if(FacebookEventActivity.class.isInstance(mContext))
             {
                 FacebookEventActivity fe = (FacebookEventActivity)mContext;
                 but.setOnClickListener(fe.loadOlderEventClick);
                 if(fe.isInProcess())
                 {
                     but.setText(mContext.getString(R.string.loading_event));
                 }
             }
             return but;
		 }
		 if(mItemMap[position] < 0)
		 {
		     //create header view
		     return getHeaderView(position,convertView);		     
		 }
		 else
		 {
		     return getFacebookEventItemView(position,convertView);
		 }
	}

    private View getHeaderView(int position, View convertView) 
    {
        TextView item;
        if (null == convertView || !(convertView instanceof TextView)) 
        {
            LayoutInflater factory = LayoutInflater.from(mContext);
            item = (TextView) factory.inflate(android.R.layout.preference_category, null);
        } 
        else 
        {
            item = (TextView) convertView;
        }
        
        if(mItemMap[position] == -2)//for upcoming
            item.setText(R.string.facebook_upcoming_event);
        
        else if(mItemMap[position] == -1)//for psst
            item.setText(R.string.facebook_past_event);
        
        return item;
    }

    private View getFacebookEventItemView(int position, View convertView) 
    {
        FacebookEventItemView v=null;
        Event event = (Event)getItem(position);
        Log.d(TAG," firstweekpos is "+firstmonthpos +" firstotherpos is "+firstotherpos +
                "firstmonthpos is"+firstmonthpos+" curpos is"+mItemMap[position]);
       
        
        if (convertView == null || false == (convertView instanceof FacebookEventItemView)) 
        {
            v = new FacebookEventItemView(mContext, event);
        } 
        else 
        {
            if(event != null)
            {
                v = (FacebookEventItemView) convertView;
                if(useCursor)
                {
                    v.setContentItem(event, true);
                }
                else
                {
                    v.setContentItem(event);
                }
            }
        } 
        if(mItemMap != null)
        {
            if(mItemMap[position] == firstmonthpos)
            {
                v.setTimeViewVisible(-2);
            }
            else if(mItemMap[position] == firstweekpos)
            {
                v.setTimeViewVisible(-1);
            }
            else if(mItemMap[position] == firstotherpos)
            {
                v.setTimeViewVisible(-3);
            }
            else if(mItemMap[position] == upcomingpos)
            {
                v.setTimeViewVisible(-4);
            }
            else if(mItemMap[position] == pastpos)
            {
                v.setTimeViewVisible(-5);
            }
            else
            {
                v.setTimeViewGone();
            }
        }
       
        return v;
    }

}
