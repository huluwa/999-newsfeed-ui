package com.msocial.freefb.ui.adapter;

import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.ui.view.FacebookPhoneBookItemView;
import oms.sns.service.facebook.model.PhoneBook;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FacebookPhoneBookAdapter extends BaseAdapter 
{
    private final String TAG = "FacebookGroupAdapterextends";        
    private Context mContext;  
    private boolean fromsearch = false;
    private Cursor mCursor;
    public FacebookPhoneBookAdapter(Context con,Cursor cursor)
    {
        mContext = con;
        mCursor = cursor;       
        Log.d(TAG, "create FacebookPhoneBookAdapter");
    }
    public FacebookPhoneBookAdapter(Context con,  Cursor cursor, boolean fromsearch)
    {
        this.fromsearch = fromsearch;
        mContext = con;
        mCursor = cursor;  
        
        Log.d(TAG, "create FacebookPhoneBookAdapter");
    }
    
    public int getCount() 
    {
        return  mCursor!= null?mCursor.getCount():0;
    }
    
    public Object getItem(int pos) 
    {        
        try{
            if(mCursor != null && mCursor.isClosed() == false && mCursor.moveToFirst())
            {
                if(mCursor.moveToPosition(pos))
                {
                    return SocialORM.instance(mContext).formatPhoneBook(mCursor);
                }
            }   
        }catch(Exception ne){}
        return null;        
    }
    
    public long getItemId(int pos) 
    {
        if(mCursor != null  && mCursor.moveToFirst())
        {
            if(mCursor.moveToPosition(pos))
            {
                return SocialORM.instance(mContext).formatPhoneBook(mCursor).uid;
            }
        }           
        return -1;  
    }
    public View getView(int position, View convertView, ViewGroup arg2) 
    {       
         if (position < 0 || position >= getCount()) 
         {
             return null;    
         }
         
         FacebookPhoneBookItemView v=null;
    
         PhoneBook di = (PhoneBook)getItem(position);
         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
             v = new FacebookPhoneBookItemView(mContext, di,fromsearch);
         } else {
              v = (FacebookPhoneBookItemView) convertView;
              v.setPhoneBookItem(di,true);
         }        
         v.choosePhoneBookListener();
         return v;
    }   
}