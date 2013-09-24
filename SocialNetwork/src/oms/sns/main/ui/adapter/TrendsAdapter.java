package oms.sns.main.ui.adapter;

import java.util.List;

import oms.sns.main.ui.TwitterMainActivity;
import oms.sns.main.ui.view.TrendItemView;
import twitter4j.Last10Trends;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.Last10Trends.TrendsItem;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TrendsAdapter extends BaseAdapter {
    private final String TAG = "TweetsAdapter";        
    private Context mContext;
	    
   
    public TrendsAdapter(Context con,  Last10Trends currentTweets) {		
    	mContext = con;
    	//TODO
    	mTrendsItems = currentTweets.last_10; 
	}

	private List<TrendsItem> mTrendsItems;

	public int getCount() {		
		return mTrendsItems.size();
	}

	public Object getItem(int pos) 
	{		
		return mTrendsItems.get(pos);
	}

	public long getItemId(int pos) 
	{	
		return pos;
	}

	public View getView(int position, View convertView, ViewGroup arg2) {
		
		 if (position < 0 || position >= getCount()) 
		 {
             return null;    
         }
         
		 TrendsItem di = (TrendsItem)getItem(position);          
		 TrendItemView v;
         if (convertView == null /*|| convertView instanceof SeparatorView*/) {
             v = new TrendItemView(mContext, di, position);
         } else {
              v = (TrendItemView) convertView;
              v.setTrendsItem(di, position);
         }
         v.chooseTrendsListener();
         
         return v;		
	}
}
