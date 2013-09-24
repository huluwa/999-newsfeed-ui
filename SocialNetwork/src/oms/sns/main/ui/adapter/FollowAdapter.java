package oms.sns.main.ui.adapter;

import java.util.List;

import oms.sns.main.ui.view.FollowItemView;
import twitter4j.SimplyUser;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FollowAdapter extends BaseAdapter 
{
    private final String TAG = "FollowAdapterextends";        
    private Context mContext;
    private int type=0;//0 following, 1 follower
    public FollowAdapter(Context con,  List<SimplyUser>status, int type)
    {
    	mContext = con;
    	mUserItems = status;
    	this.type = type;
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
		return mUserItems.get(pos).id;
	}
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
         }
         
		 FollowItemView v=null;
	
		 SimplyUser di = (SimplyUser)getItem(position);
         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
             v = new FollowItemView(mContext, di);
         } else {
              v = (FollowItemView) convertView;
              v.setUserItem(di);
         }
         if(type == 0)
         {
             v.chooseFollowListener();
         }
         return v;
	}
	
	private List<SimplyUser> mUserItems;			
	
}


