    package com.msocial.facebook.ui.adapter;

import java.util.List;
import com.msocial.facebook.ui.view.FacebookPhoneBookItemView;
import oms.sns.service.facebook.model.PhoneBook;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FacebookLookupAdapter extends BaseAdapter 
{
    private final String TAG = "FacebookLookupAdapter";        
    private Context mContext;  
    private boolean fromsearch = false;
    public FacebookLookupAdapter(Context con,  List<PhoneBook>phonebooks)
    {
        mContext = con;
        mPhoneBookItems = phonebooks;       
        Log.d(TAG, "create FacebookLookupAdapter");
    }
    public FacebookLookupAdapter(Context con,  List<PhoneBook>phonebooks, boolean fromsearch)
    {
        this.fromsearch = fromsearch;
        mContext = con;
        mPhoneBookItems = phonebooks;   
        
        Log.d(TAG, "create FacebookLookupAdapter");
    }
    
    public int getCount() 
    {
        return mPhoneBookItems.size();
    }
    public Object getItem(int pos) {        
        return mPhoneBookItems.get(pos);
    }
    public long getItemId(int pos) 
    {
        return mPhoneBookItems.get(pos).uid;
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
              v.setPhoneBookItem(di);
         }        
         v.choosePhoneBookListener();
         return v;
    }   
    private List<PhoneBook> mPhoneBookItems;
}
