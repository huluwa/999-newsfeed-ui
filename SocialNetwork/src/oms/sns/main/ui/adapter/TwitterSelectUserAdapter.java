package oms.sns.main.ui.adapter;

import java.util.List;
import oms.sns.main.providers.SocialORM.Follow;
import oms.sns.main.ui.view.TwitterSelectUserItemView;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TwitterSelectUserAdapter extends BaseAdapter  {
	private final String TAG = "TwitterUserSelectActivity";        
    private Context mContext;    
    public TwitterSelectUserAdapter(Context con,  List<Follow> users)
    {
    	mContext = con;
    	mUserItems = users;   
    	Log.d(TAG, "create TwitterSelectUserapter");
    }
    
	public int getCount() 
	{
		return mUserItems.size();
	}
	public Object getItem(int pos) {		
	    return mUserItems.get(pos);
	}
	public long getItemId(int pos) 
	{
		return mUserItems.get(pos).UID;
	}
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
         }
         
		 TwitterSelectUserItemView v=null;
	
		 Follow di = (Follow)getItem(position);
         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
             v = new TwitterSelectUserItemView(mContext, di);
         } else {
              v = (TwitterSelectUserItemView) convertView;
              v.setUserItem(di);
         }        
         v.chooseSelectListener();
         return v;
	}	
	private List<Follow> mUserItems;
}
