package com.tormas.litesina.ui.view;

import com.tormas.litesina.*;
import oms.sns.TwitterStatus;
import oms.sns.TwitterUser;
import com.tormas.litesina.R;
import com.tormas.litesina.ui.TwitterMainActivity;
import com.tormas.litesina.ui.TwitterSearchActivity;
import com.tormas.litesina.ui.TwitterTweetsActivity;
import com.tormas.litesina.ui.TwitterTweetsDetailActivity;
import twitter4j.Last10Trends.TrendsItem;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class TrendItemView extends SNSItemView {

	TrendsItem trend;
	private String TAG="TrendItemView";
	TextView txtView;
	String   url;
	String   name;
	int      pos;
	
	public TrendItemView(Context context, TrendsItem di, int position) {
		super(context);
		
		this.mContext = context;
		
		trend = di;
		
		Log.d(TAG, "call TweetItemView");
		pos = position;
		init();
	}


	//create the view
	private void init() 
	{		
		Log.d(TAG,  "call init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();
		
		//container
		FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);		
		FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(paras);
		view.setVerticalScrollBarEnabled(true);
		addView(view);
		
		//child 1
		View v = factory.inflate(R.layout.twitter_trend_item, null);
		LayoutParams vparas = new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT);		
		v.setLayoutParams(vparas);		
		view.addView(v);	
		//txtView = new TextView(mContext);
		txtView = (TextView)v.findViewById(R.id.trend_publish_text);
		
		if(trend != null)
		    txtView.setText(String.format("%1$s  %2$s", pos+1, trend.name));
	}


	//call search
	public void chooseTrendsListener() 
	{	
		//setOnClickListener(trendOnClik);
	}

	public TrendsItem getTrendsItem()
	{
		return trend;
	}
	View.OnClickListener trendOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "trendOnClik you click first one="+url);			 
			 //view details
			 Intent intent = new Intent(mContext, TwitterSearchActivity.class);
			 intent.putExtra("currenttrendurl",  trend.link);
			 intent.putExtra("currenttrendname", trend.name);			 
			 
			 ((TwitterMainActivity)mContext).startActivityForResult(intent, TwitterMainActivity.TWITTER_TWEETS);			 
		}
	};
	
	public void setTrendsItem(TrendsItem di, int position) 
	{	
		trend = di;
		url  = di.link;
		name = di.name;
		txtView.setText(String.format("%1$s  %2$s", position+1, name));
	}


	@Override
	public String getText() 
	{
		return trend.name + " url: " + trend.link;
	}

}
