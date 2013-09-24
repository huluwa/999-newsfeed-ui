package oms.sns.main.ui;

import java.util.ArrayList;

import oms.sns.TwitterUser;
import oms.sns.main.R;
import oms.sns.main.ui.view.SNSItemView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

public class TwitterListActivity extends Activity{   
	private ListView listview ;
	private boolean issearch;
	private Context mContext;
		 
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        mContext = TwitterListActivity.this;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.twitter_list_account);
        
        
        listview = (ListView)this.findViewById(R.id.twitter_tweets_list);
        listview.setFocusableInTouchMode(true);
        listview.setFocusable(true);
        listview.setOnItemClickListener(itemClickListener);
        
        ArrayList<String> useraccounts = this.getIntent().getStringArrayListExtra("foruseraccount");
        ArrayList<String> forsearch = this.getIntent().getStringArrayListExtra("forsearch");
        
        if(useraccounts!=null && useraccounts.size()>0)
        {   
        	issearch = false;
        	listview.setAdapter(new TwitterSimpleAdapter(this,useraccounts,false));  	
        }
        if(forsearch != null && forsearch.size()>0)
        {
        	issearch = true;
        	listview.setAdapter(new TwitterSimpleAdapter(this,forsearch,true));
        }
    }
	
	public void setTitle() {
		// TODO Auto-generated method stub
		
	}
	
	AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener()
	{

		public void onItemClick(AdapterView<?> adapterview, View v, int arg2,long arg3) {
			
			if(TwitterListItemView.class.isInstance(v))
			{
				 TwitterListItemView view = (TwitterListItemView)v;
				 
				 String mItem = view.mItem;
				 
				 if(issearch)
				 {
					 //view details
					 Intent intent = new Intent(mContext, TwitterSearchActivity.class);
					 intent.putExtra("currenttrendname", mItem);					 
					 mContext.startActivity(intent);
				 }
				 else
				 {
					 Intent intent = new Intent(mContext,TwitterUserDetailsActivity.class);
					 TwitterUser user = new TwitterUser();
					 user.screenName = mItem;
					 user.name  = mItem;
					 
					 intent.putExtra("currentuser", user);
					 intent.putExtra("forsearchfromtwitterlist", true);		 
					 mContext.startActivity(intent);
				 }
				 TwitterListActivity.this.finish();
				
			}
			
		}
	     
	};
	
	public  class TwitterSimpleAdapter extends BaseAdapter
    {
        private Context mContext;
        private ArrayList<String> mStrings;
        private boolean forsearch;
        
        public TwitterSimpleAdapter(Context con,  ArrayList<String> strings,boolean forsearch)
        {
            mContext = con;
            mStrings = strings;   
            this.forsearch = forsearch;
        }
        
        public int getCount() {
            return mStrings.size();
        }

        public Object getItem(int pos) {
            return mStrings.get(pos);
        }

        public long getItemId(int pos) {
            return pos;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (position < 0 || position >= getCount()) 
            {
                return null;    
            }
            
            TwitterListItemView v=null;
       
            String di = (String)getItem(position);
            if (convertView == null) {
                v = new TwitterListItemView(mContext, di);
            } else {
                 v = (TwitterListItemView) convertView;
                 v.setItem(di);
            }        
            return v;
        }
        
    }
    
    public class TwitterListItemView extends SNSItemView
    {
        static final String TAG="TwitterListItemView";
        private Context mContext;
        private String  mItem;
        private TextView txtView;
        private Handler albumHandler = new Handler();;
        
        public TwitterListItemView(Context context) 
        {
           super(context);
           mContext = context;
        }
        
        public void setItem(String string) 
        {
            mItem = string;            
            setUI();            
        }

        public TwitterListItemView(Context context,String item)
        {
            super(context);
            mContext = context;
            mItem = item;  
            init();
        }
        
        private void init() 
        {
            Log.d(TAG,  "call TwitterListItemView init");
            LayoutInflater factory = LayoutInflater.from(mContext);
            removeAllViews();
            
            //container
            FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);        
            FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);     
            view.setLayoutParams(paras);
            view.setVerticalScrollBarEnabled(true);
            addView(view);
            
            //child 1
            View v  = factory.inflate(R.layout.twitter_trend_item, null);     
            v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));
            view.addView(v);
            
            txtView = (TextView)v.findViewById(R.id.trend_publish_text);
            //setOnClickListener(msgOnClik);
            
            setUI();          
        } 
        
        private void setUI()
        {        
            txtView.setText(mItem);
        }
        
        @Override
        protected void onFinishInflate() 
        {   
            super.onFinishInflate();        
            init();
        }    
        
        @Override
        public String getText() 
        {            
            return mItem;
        }
   
    }
  
	
}
