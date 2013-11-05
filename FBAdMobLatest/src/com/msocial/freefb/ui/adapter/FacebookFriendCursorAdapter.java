package com.msocial.freefb.ui.adapter;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.providers.SocialORM.FacebookUsersCol;
import com.msocial.freefb.ui.view.FacebookFriendItemView;
import oms.sns.service.facebook.model.FacebookUser;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.Browser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DateSorter;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FacebookFriendCursorAdapter extends BaseAdapter 
{
    private final String TAG = "FacebookFriendCursorAdapter";        
    private Context mContext;   
    private boolean isfriendbd = false;   
    private Cursor mCursor;
    
    public  boolean forsearch=false;
    private int     mItemMap[];
    private int     itemLength = 0;
    static  String[]month_array;
    public void buildMap() 
    {
    	int n = mCursor.getCount();
    	if(isfriendbd && forsearch == false)
    	{
    	    n += 12;//12 months
    	}
        int array[] = new int[n];
        boolean emptyBins[] = new boolean[12];
          
        int monthIndex = -1;
		if (mCursor.moveToFirst() && mCursor.getCount() > 0) 
		{
			int itemIndex = 0;
			while (!mCursor.isAfterLast()) 
			{
				if(isfriendbd && forsearch == false)
				{
					int month = mCursor.getInt(mCursor.getColumnIndex(FacebookUsersCol.B_MONTH));
					int index = month;
					if (index > monthIndex) {
						Log.d(TAG, "month="+monthIndex + " new index="+index);
						monthIndex = index;
						array[itemIndex] = monthIndex - 12;
						itemIndex++;
						emptyBins[index] = false;
					}
				}
				array[itemIndex] = mCursor.getPosition();
				itemIndex++;
				mCursor.moveToNext();
			}
			itemLength = itemIndex;
		}
		else 
		{
			// The db is empty, just add the heading for the first item
			monthIndex = 0;
			if(isfriendbd && forsearch == false)
			{
			    array[0] = monthIndex - 12;
			}
			else
			{
				if(n > 0)
				{
				    array[0] = -1;
				}
			}
		} 
		
		mItemMap = array;
    }
    public FacebookFriendCursorAdapter(Context con,  Cursor cursor, boolean isfriendbd, boolean forsearch)
    {
        mContext        = con;
        mCursor         = cursor;
        this.isfriendbd = isfriendbd;
        this.forsearch  = forsearch;
        
        buildMap();
        Log.d(TAG, "create FacebookFriendCursorAdapter");
    }
    
    public int getCount() 
    {
    	return itemLength;
    }
    
    public Object getItem(int pos) 
    {       
    	if(mItemMap[pos] < 0)
    	{
            return null;
    	}
    	else
    	{
    		if(mCursor != null && mCursor.moveToPosition(mItemMap[pos]))
    		{
    		    return SocialORM.instance(mContext).formatSimpleFacebookUser(mCursor);
    		}
    		else
    		{
    			return null;
    		}
    	}
    }
    public long getItemId(int pos) 
    {
    	return mItemMap[pos];
    }
    public View getView(int position, View convertView, ViewGroup arg2) 
    {       
         if (position < 0 || position >= getCount()) 
         {
             return null;    
         }
         
         if(mItemMap[position] < 0) 
         {
             return getHeaderView(position, convertView);
         }  
         else
         {
	         FacebookFriendItemView v=null;    
	         FacebookUser.SimpleFBUser di = (FacebookUser.SimpleFBUser)getItem(position);
	         di.isFriend(mContext);
	         Log.d(TAG," di. isfriend is "+di.isfriend);
	         if (convertView == null || false == (convertView instanceof FacebookFriendItemView)) 
	         {
	             v = new FacebookFriendItemView(mContext, di,isfriendbd);
	             v.setForCusor(true);
	         }
	         else 
	         {
	              v = (FacebookFriendItemView) convertView;
	              v.setUserItem(di,isfriendbd);
	              v.setForCusor(true);
	         }
	         return v;
         }
    }
    
   
    View getHeaderView(int position, View convertView) 
    {
    	 if (position < 0 || position >= getCount()) 
         {
             return null;    
         }

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
        
        if(month_array == null)
        {
            month_array = mContext.getResources().getStringArray(R.array.entries_month);
        }
        item.setText(month_array[mItemMap[position] + 12]);
        
        return item;
    }
}


