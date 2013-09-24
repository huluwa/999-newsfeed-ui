package oms.sns.main.ui.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oms.sns.*;
import oms.sns.main.R;
import oms.sns.main.ui.TwitterHelper;
import oms.sns.main.ui.TwitterBaseActivity;
import oms.sns.main.ui.TwitterTweetsDetailActivity;
import oms.sns.main.util.DateUtil;
import twitter4j.SimplyStatus;
import twitter4j.Status;
import twitter4j.Tweet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class SimplyStatusItemView extends SNSItemView {
    private String baseURL = "http://api.t.sina.com.cn/";//"http://api.twitter.com/";
	private final String TAG="SimplyStatusItemView";
	private ImageView imageView;
	private TextView publishDate;
	private TextView publishTxt;
	private TextView username;	
	
	private SimplyStatus status;
	private Tweet  tweet;	
	String  imageURL;
	Handler handler;
	int type;//0, status, 1 tweet
	java.util.Random random = new java.util.Random();
	private boolean showOnlyText=false;
	
	public SimplyStatusItemView(Context ctx, AttributeSet attrs) 
	{
		super(ctx, attrs);
		
		mContext = ctx;
		setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);   
        
        handler = new Handler();
    }
	
	static final String urlPartern="(^|[ \t\r\n])(" +
			"(ftp|http|https|gopher|mailto|tel|news|nntp|telnet|wais|file|prospero|aim|webcal)" +
			":" +
			"(" +
			"([A-Za-z0-9$_.+!*(),;/?:@&~=-])|%[A-Fa-f0-9]{2}){2,}(#([a-zA-Z0-9][a-zA-Z0-9$_.+!*(),;/?:@&~=%-]*))?([A-Za-z0-9$_+!*();/?:~-]))";
	public List<String> getLinks()
	{
		List<String> links = new ArrayList<String>();
		String text = this.getText();
		Pattern p = Pattern.compile(urlPartern);
		Matcher m = p.matcher(text);
		
		while(m.find())
		{
			links.add(text.substring(m.start(), m.end()).trim());
		}
		return links;
	}
	
	static final String userLinkPartern = "@[a-zA-Z0-9_]+";
	public ArrayList<String> getUserScreenName()
	{
		ArrayList<String> screennames = new ArrayList<String>();
		String text = this.getText();
		Pattern p = Pattern.compile(userLinkPartern);
		Matcher m = p.matcher(text);
		while(m.find())
		{
			screennames.add(text.substring(m.start()+1,m.end()).trim());
		}
		return screennames;
	}
	
	static final String searchLinkPartern = "#[^\\s]+";
	public ArrayList<String> getSearchString()
	{
		ArrayList<String> searchStrs = new ArrayList<String>();
		String text = this.getText();
		Pattern p = Pattern.compile(searchLinkPartern);
		Matcher m = p.matcher(text);
		while(m.find())
		{
			searchStrs.add(text.substring(m.start()+1,m.end()).trim());
		}
		return searchStrs;
	}
	
	public int getType()
	{
		return type;
	}
	public SimplyStatus getStatus()
	{
		return status;
	}
	public Tweet getTweet()
	{
		return tweet;
	}
	
	public SimplyStatusItemView(Context context, SimplyStatus di) 
	{		
		super(context);
		mContext = context;
		status = di;
		
		Log.d(TAG, "call SimplyStatusItemView");
		type = 0;
		handler = new Handler();
		init();
	}
	
	public SimplyStatusItemView(Context context, Tweet di) 
	{		
		super(context);
		mContext = context;
		tweet = di;
		
		Log.d(TAG, "call SimplyStatusItemView");
		type = 1;
		handler = new Handler();
		init();
	}
	
	public void showForDetail()
	{
		showOnlyText = true;
	}
	public String getTwitterID()
    {
        String ret = "";
        if(tweet != null)
            ret = tweet.getFromUser();
        else if(status != null)
            ret = status.getUser().getScreenName();
        
        return ret;
    }
	
	public long getStatusID()
	{
	    long ret = -1;
	    if(tweet != null)
	        ret = tweet.getId();
	    else if(status != null)
	        ret = status.getId();
	    
	    return ret;
	}
	public String getText()
    {
        String ret = "";
        if(tweet != null)
            ret = tweet.getText();
        else if(status != null)
            ret = status.getText();
        
        return ret;
    }
	public Object GetContent()
	{
	    Object ret = null;
        if(tweet != null)
            ret = tweet;
        else if(status != null)
            ret = status;
        
        return ret;
	}
	
	private void init() 
	{
		Log.d(TAG,  "call SimplyStatusItemView init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();
		
		//container
		FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);
		FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(paras);
		view.setVerticalScrollBarEnabled(true);
		addView(view);
		
		//child 1
		View v  = factory.inflate(R.layout.twitter_tweets_item, null);		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		view.addView(v);
		
		imageView  = (ImageView)v.findViewById(R.id.tweet_img_ui);
		if(showOnlyText==true)
			imageView.setVisibility(View.GONE);
		
		imageView.setImageResource(R.drawable.no_avatar);
		
		publishDate  = (TextView)v.findViewById(R.id.tweet_publish_time);
		publishTxt   = (TextView)v.findViewById(R.id.tweet_publish_text);		
		username     = (TextView)v.findViewById(R.id.tweet_user_name);
		if(showOnlyText == true)
			username.setVisibility(View.GONE);
		
		if(type == 0 && status != null)
		{			
			if(showOnlyText == false)
			{
				username.setText(status.getUser().name);
				imageURL = status.getUser().getProfileImageURL().toString();
				getImageBMP(imageURL);
			}
			
			publishDate.setText(getDate(status.getCreatedAt()));			
			publishTxt.setText(status.getText());
		}
		else if(type ==1 && tweet != null)
		{	
			if(showOnlyText == false)
			{
				username.setText(tweet.getFromUser());
				imageURL = tweet.getProfileImageUrl();
				getImageBMP(imageURL);
			}
			publishDate.setText(getDate(tweet.getCreatedAt()));			
			publishTxt.setText(tweet.getText());
		}		
	}	
	
	private String getText(Tweet tweet) {
        String str = String.format("<a href='%1$s'>"+tweet.getFromUser()+"</a>",baseURL+tweet.getFromUserId());
        str = str + " "+tweet.getText();
       // Log.d(TAG, "SimplyStatusItemView text is "+str);
        return str;
    }

    private void getImageBMP(String url)
    {
	    imageView.setImageResource(R.drawable.no_avatar);
        ImageRun imagerun = new ImageRun(handler, url, 0);
        imagerun.use_avatar = true;
        imagerun.addHostAndPath = true;
        imagerun.setImageView(imageView);        
        imagerun.post(null);
    }
	
	public String getDate(Date date)
	{
		return DateUtil.converToRelativeTime(mContext,date);
	}

	//A bug for list view
	public void chooseTweetsListener()
	{
		//setOnClickListener(tweetOnClik);
	}
	
	View.OnClickListener tweetOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "tweetOnClik you click first one=");			 
			 //view details
			 Intent intent = new Intent(mContext, TwitterTweetsDetailActivity.class);
			 			 
			 if(type ==0 && status != null)
			 {
				 TwitterStatus twitem = new TwitterStatus();
		         twitem.createdAt = status.getCreatedAt().getTime();
		         twitem.id        = status.getId();		        
		         twitem.isFavorited       = status.isFavorited();		        
		         twitem.text    = status.getText();
		         
		         twitem.user = new TwitterUser();
		         twitem.user.id = status.getUser().getId();		         
		         twitem.user.name           = status.getUser().getName();
		         twitem.user.profileImageUrl = status.getUser().getProfileImageURL().toString();
		         twitem.user.screenName      = status.getUser().getScreenName();	
		         twitem.user.notifications    = status.getUser().notifications;
		         twitem.user.following    = status.getUser().following;
		         
				 intent.putExtra("currentstatus", twitem);
				 intent.putExtra("fromstatus", true);
				 ((TwitterBaseActivity)mContext).startActivityForResult(intent, TwitterBaseActivity.TWEET_DETAIL);
			 }
			 else if(type ==1 && tweet != null)
			 {
				 TwitterStatus twitem = new TwitterStatus();
		         twitem.createdAt = tweet.getCreatedAt().getTime();
		         twitem.id        = tweet.getId();		         
		         twitem.inReplyToUserId   = tweet.getToUserId();
		         twitem.isFavorited       = false;
		         twitem.text    = tweet.getText();
		         
		         twitem.user = new TwitterUser();
		         twitem.user.id = tweet.getFromUserId();		         		         
		         twitem.user.name           = tweet.getFromUser();
		         twitem.user.profileImageUrl = tweet.getProfileImageUrl();
		         twitem.user.screenName      = tweet.getFromUser();
		                 
				 intent.putExtra("currenttweet", twitem);
				 intent.putExtra("fromtweet", true);
				 
				 ((TwitterBaseActivity)mContext).startActivityForResult(intent, TwitterBaseActivity.TWEET_DETAIL);
			 }
			 
		}
	};
	

	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	public void setTweetItem(Tweet di) 
	{
		tweet = di;		
		imageView.setImageResource(R.drawable.no_avatar);
		if(showOnlyText == false)
		{
			username.setText(tweet.getFromUser());
			String url = tweet.getProfileImageUrl();			
			getImageBMP(url);
		}
		publishDate.setText(getDate(tweet.getCreatedAt()));
		publishTxt.setText(tweet.getText());
	}

	public void setStatusItem(SimplyStatus di) 
	{
		status = di;		
		if(showOnlyText == false)
		{
			username.setText(status.getUser().name);
			String url = status.getUser().getProfileImageURL().toString();			
			getImageBMP(url);
		}
		publishDate.setText(getDate(status.getCreatedAt()));
		
		publishTxt.setText(status.getText());
	}
	
	private String getText(SimplyStatus status2) {
        String str = String.format("<a href='%1$s'>"+status2.getUser().getScreenName()+"</a>",baseURL+status2.getUser().getScreenName());
        str = str + " "+status2.getText();
        //Log.d(TAG, "SimplyStatusItemView is "+str);
        return str;
    }

    public CharSequence getName() {
		if(type ==0)
		    return status.getUser().name;
		else
			return tweet.getFromUser();
	}
	
	
	public String shortenUserName(String username)
	{
	    int maxlen = 5;
	    String tempStr = getShortenString(username,maxlen);
	    if(tempStr.length() == username.length())
	    {
	        return tempStr;
	    }
	    else
	    {
	        return tempStr+"...";
	    }
	}
	
	public String getShortenString(String str1,int maxlen)
    {
      int currentLen=0;
      String tempchar="";
      char t;
      for(int n=0;n<str1.length();n++)
      {
          t=str1.charAt(n);
          if (((int) t)<127)
          {
              currentLen=currentLen+1;
          }
          else
          {
              currentLen=currentLen+2;
          }
          if (currentLen<=maxlen*2)
          {
              tempchar=tempchar+String.valueOf(t);
          }
          else 
          {
              break;
          }
      }
      return tempchar;
  }
}

