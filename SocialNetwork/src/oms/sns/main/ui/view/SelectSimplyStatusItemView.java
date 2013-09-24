package oms.sns.main.ui.view;

import oms.sns.main.R;
import oms.sns.main.ui.TwitterFavoritesActivity;
import oms.sns.main.ui.TwitterMessageActivity;
import oms.sns.main.ui.adapter.SelectStatusItem;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SelectSimplyStatusItemView  extends SNSItemView 
{
	private final String TAG="SelectSimplyStatusItemView";
	
	private TextView publishDate;
	private TextView publishTxt;
	private TextView username;
	private CheckBox chekbox;
	
	SelectStatusItem status;	

	public SelectSimplyStatusItemView(Context context, SelectStatusItem di) {
		super(context);
		mContext = context;
		status = di;
		
		Log.d(TAG, "call SelectSimplyStatusItemView");
		init();
	} 
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	public String getText()
	{
		return status.text;
	}
	public String getName()
	{
		return status.name;
	}
	public String getScreenName()
	{
		return status.screenname;
	}
	public long getStatusID()
	{
		return status.id;
	}
	private void init() 
	{
		Log.d(TAG,  "call SelectSimplyStatusItemView init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();
		
		//container
		FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);
		FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(paras);
		view.setVerticalScrollBarEnabled(true);
		addView(view);
		
		//child 1
		View v  = factory.inflate(R.layout.twitter_status_item_check, null);		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		view.addView(v);
		
		chekbox      = (CheckBox)v.findViewById(R.id.message_delete_checkbox);
		publishDate  = (TextView)v.findViewById(R.id.message_delete_publish_time);
		publishTxt   = (TextView)v.findViewById(R.id.message_delete_publish_text);
		username     = (TextView)v.findViewById(R.id.message_delete_user_name);
		
		setUI();		
	}	
	
	private void setUI()
	{
		username.setText(status.name);
		publishDate.setText(status.date.toLocaleString());			
		publishTxt.setText(status.text);
		chekbox.setChecked(status.selected);
		
	}
	public void setStatusItem(SelectStatusItem di) 
	{
	    status = di;
	    
	    setUI();
	}

	public boolean isSelected()
	{
		return status.selected;
	}
	
	public void setCheckBoxSelected(boolean sel)
	{
		status.selected = sel;
		chekbox.setChecked(status.selected);
		chekbox.invalidate();
		
		Log.d(TAG, "setSelected select ="+status.selected);
	}
	
	public void chooseStatusListener()
	{
		setOnClickListener(stOnClik);
	}
	
	View.OnClickListener stOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			status.selected = !status.selected;
			chekbox.setChecked(status.selected);			
			
			if(TwitterMessageActivity.class.isInstance(mContext))
			{
				TwitterMessageActivity tm = (TwitterMessageActivity)mContext;
			    tm.setSelect(status.id, status.selected);	
			}
			else if(TwitterFavoritesActivity.class.isInstance(mContext))
			{
				TwitterFavoritesActivity tf = (TwitterFavoritesActivity)mContext;
				tf.setSelect(status.id,status.selected);
			}
			
			Log.d(TAG, "onClick select ="+status.selected);
		}
	};
}
