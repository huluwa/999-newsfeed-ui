package oms.sns.main.ui.adapter;


import java.util.ArrayList;
import java.util.List;

import oms.sns.main.ui.view.SelectSimplyStatusItemView;
import twitter4j.SimplyStatus;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SelectSimplyStatusAdapter extends BaseAdapter {
    private final String TAG = "SelectSimplyStatusAdapter";        
    private Context mContext;
  
    public SelectSimplyStatusAdapter(Context con,  List<SelectStatusItem>status)
    {
    	mContext = con;
    	mStatusItems = status;
    }
    public SelectSimplyStatusAdapter(Context con, ArrayList<SimplyStatus> currentStatus) 
    {
		if(currentStatus != null)
		{
			if(mStatusItems == null)
				mStatusItems = new ArrayList<SelectStatusItem>();
			for(SimplyStatus item: currentStatus)
			{
				SelectStatusItem si = new SelectStatusItem();
				si.date = item.createdAt;
				si.text = item.text;
				si.name = item.getUser().name;
				si.id   = item.id;
				si.screenname = item.getUser().screenName;
				si.selected = item.selected;
				
				mStatusItems.add(si);
			}
		}
		mContext = con;		
	}
    
	public int getCount() {
		    return mStatusItems.size();
	}
	public Object getItem(int pos) {	
		    return mStatusItems.get(pos);
	}
	public long getItemId(int pos) {
		    return mStatusItems.get(pos).id;
	}
	public View getView(int position, View convertView, ViewGroup arg2) 
	{		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
         }         
		 SelectSimplyStatusItemView v=null;		
		 SelectStatusItem di = (SelectStatusItem)getItem(position);
         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
             v = new SelectSimplyStatusItemView(mContext, di);
         } else {
              v = (SelectSimplyStatusItemView) convertView;
              v.setStatusItem(di);
         }
	     v.chooseStatusListener();
		
         return v;
	}
	
	private List<SelectStatusItem> mStatusItems;	
	
}



