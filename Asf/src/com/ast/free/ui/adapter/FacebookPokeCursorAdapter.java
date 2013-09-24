package com.ast.free.ui.adapter;

import com.ast.free.providers.SocialORM;
import com.ast.free.ui.view.FacebookPokeItemView;
import oms.sns.service.facebook.model.FacebookUser;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FacebookPokeCursorAdapter extends BaseAdapter{
    private final String TAG = "FacebookPokeCursorAdapter";        
    private Context mContext;   
    private Cursor mCursor;
   
    public FacebookPokeCursorAdapter(Context con,  Cursor cursor)
    {
        mContext        = con;
        mCursor         = cursor;
    }
    
    public int getCount() 
    {
        return mCursor!=null?mCursor.getCount():0;
    }
    
    public Object getItem(int pos) 
    { 
        if(mCursor != null && mCursor.moveToPosition(pos))
        {
            return SocialORM.instance(mContext).formatSimpleFacebookUser(mCursor);
        }
        else
        {
            return null;
        }
    }
    public long getItemId(int pos) 
    {
        if(getItem(pos)!=null)
        {
            return ((FacebookUser.SimpleFBUser)getItem(pos)).uid;
        }
        else
        {
            return -1;
        }
    }
    public View getView(int position, View convertView, ViewGroup arg2) 
    {       
         if (position < 0 || position >= getCount()) 
         {
             return null;    
         }
         FacebookPokeItemView v=null;    
         FacebookUser.SimpleFBUser di = (FacebookUser.SimpleFBUser)getItem(position);
         di.isFriend(mContext);
         Log.d(TAG," di. isfriend is "+di.isfriend);
         if (convertView == null || false == (convertView instanceof FacebookPokeItemView)) 
         {
             v = new FacebookPokeItemView(mContext, di);
             v.setForCusor(true);
         }
         else 
         {
              v = (FacebookPokeItemView) convertView;
              v.setUserItem(di);
              v.setForCusor(true);
         }
         return v;
    }
   
}
