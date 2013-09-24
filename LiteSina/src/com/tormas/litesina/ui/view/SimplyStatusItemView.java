package com.tormas.litesina.ui.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oms.sns.*;
import com.tormas.litesina.R;
import com.tormas.litesina.providers.SocialORM;
import com.tormas.litesina.ui.TwitterComposeActivity;
import com.tormas.litesina.ui.TwitterHelper;
import com.tormas.litesina.ui.TwitterBaseActivity;
import com.tormas.litesina.ui.TwitterTweetsDetailActivity;
import com.tormas.litesina.util.DateUtil;
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
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class SimplyStatusItemView extends SNSItemView {
    private String baseURL = "http://api.t.sina.com.cn/";//"http://api.twitter.com/";
	private final String TAG="SimplyStatusItemView";
	private ImageView imageView;
	private ImageView tweet_img_pic_ui;
	private TextView publishDate;
	private TextView publishTxt;
	private TextView username;	
	
	private TextView facebook_stream_comments;
	private TextView facebook_stream_like;
	private ImageView facebook_photo_fetch;
	private ImageView  tweet_pic_res;
	
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
	static final String userLinkOtherPartern = "@[^\\s]+:";
	static final String userLinkSpacePartern = "@[^\\s^:]+ ";
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
		
		p = Pattern.compile(userLinkOtherPartern);
        m = p.matcher(text);
        while(m.find())
        {
            boolean have = false;
            final String sname = text.substring(m.start()+1,m.end()-1).trim();
            for(String name:screennames)
            {
                if(sname.equals(name))
                {
                    have = true;
                    break;
                }
            }
            if(have == false)
            {
                screennames.add(sname);
            }
        }
        
        p = Pattern.compile(userLinkSpacePartern);
        m = p.matcher(text);
        while(m.find())
        {
            boolean have = false;
            final String sname = text.substring(m.start()+1,m.end()-1).trim();
            for(String name:screennames)
            {
                if(sname.equals(name))
                {
                    have = true;
                    break;
                }
            }
            if(have == false)
            {
                screennames.add(sname);
            }
        }
        
		return screennames;
	}
	
	static final String searchLinkPartern = "#[^\\s]+#";
	public ArrayList<String> getSearchString()
	{
		ArrayList<String> searchStrs = new ArrayList<String>();
		String text = this.getText();
		Pattern p = Pattern.compile(searchLinkPartern);
		Matcher m = p.matcher(text);
		while(m.find())
		{
			searchStrs.add(text.substring(m.start()+1,m.end()-1).trim());
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
        {
            ret = tweet.getText();
        }
        else if(status != null)
        {
            ret = status.getText();
            if(status.isRetweet())
            {
            	ret +="\n----->>\n" + status.retweetDetails.text;
            }
        }
        
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
		tweet_img_pic_ui = (ImageView)v.findViewById(R.id.tweet_img_pic_ui);
		if(showOnlyText == true)
			username.setVisibility(View.GONE);
		
		facebook_stream_comments = (TextView)findViewById(R.id.facebook_stream_comments);
		facebook_stream_like = (TextView)findViewById(R.id.facebook_stream_like);
		facebook_photo_fetch = (ImageView)findViewById(R.id.facebook_photo_fetch);
		tweet_pic_res = (ImageView)findViewById(R.id.tweet_pic_res);
		
		facebook_stream_comments.setOnClickListener(commetnsOnClik);
		facebook_stream_like.setOnClickListener(likeOnClik);
		
		if(type == 0 && status != null)
		{			
			setStatusItem(status);			
		}
		else if(type ==1 && tweet != null)
		{	
			setTweetItem(tweet);
		}		
	}	
	
	View.OnClickListener commetnsOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Intent intent = new Intent(mContext, TwitterComposeActivity.class);             
	         intent.putExtra(TwitterBaseActivity.STATUS_ID,     status.id);    
	         intent.putExtra(TwitterBaseActivity.TWITTER_ID,    String.valueOf(status.user.screenName)); 
	         intent.putExtra(TwitterBaseActivity.COMMENTS, true);
	         intent.putExtra(TwitterBaseActivity.REPLY, true);
	         getContext().startActivity(intent);
		}
	};
	
	View.OnClickListener likeOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Intent intent = new Intent(mContext, TwitterComposeActivity.class);             
	         intent.putExtra(TwitterBaseActivity.STATUS_ID,     status.id);    
	         intent.putExtra(TwitterBaseActivity.TWITTER_ID,    String.valueOf(status.user.screenName));
	         intent.putExtra(TwitterBaseActivity.CONTENT,       getContext().getString(R.string.facebook_stream_you_like)); 
	         intent.putExtra(TwitterBaseActivity.REPLY, true);
	         intent.putExtra(TwitterBaseActivity.COMMENTS, true);
	         getContext().startActivity(intent);
		}
	};
	
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
		if(tweet != di)
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
		if(status != di)
		    status = di;
		
		imageView.setImageResource(R.drawable.no_avatar);
		if(showOnlyText == false)
		{
			username.setText(status.getUser().name);
			String url = status.getUser().getProfileImageURL().toString();			
			getImageBMP(url);
		}
		publishDate.setText(getDate(status.getCreatedAt()));
		if(status.isRetweet())
		    publishTxt.setText(status.getText() +"\n---------------->>\n"+status.retweetDetails.text);
		else
			publishTxt.setText(status.getText() );
		
		tweet_pic_res.setOnClickListener(null);
		if(SocialORM.instance(getContext()).isTwitterLoadAutoPhoto() == true)
		{
			tweet_img_pic_ui.setVisibility(isEmpty(status.thumbnail_pic)==false ?View.VISIBLE:View.GONE);
			tweet_img_pic_ui.setBackgroundResource(R.drawable.pic);
			
			
			tweet_pic_res.setImageResource(R.drawable.loading);
			if(isEmpty(status.thumbnail_pic) == false)
			{
				facebook_photo_fetch.setVisibility(View.VISIBLE);
				tweet_pic_res.setVisibility(View.VISIBLE);
				imageView.setImageResource(R.drawable.no_avatar);
		        ImageRun imagerun = new ImageRun(handler, status.thumbnail_pic, 0);
		        imagerun.use_avatar = false;
		        imagerun.noimage = true;
		        imagerun.addHostAndPath = true;
		        imagerun.setImageView(tweet_pic_res);        
		        imagerun.post(null);
		        tweet_pic_res.setOnClickListener(new View.OnClickListener() {
					public void onClick(View arg0) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
                    	intent.setData(Uri.parse(status.original_pic));
                    	try
                    	{
                    	    getContext().startActivity(intent);
                    	}
                    	catch(Exception ne)
                    	{
                    		Log.e(TAG, "fail to start activity="+ne.getMessage());                    		
                    	}
					}
				});
			}
			else
			{
				facebook_photo_fetch.setVisibility(View.GONE);			
				tweet_pic_res.setVisibility(View.GONE);
				tweet_pic_res.setOnClickListener(null);
			}
		}	
		
		//set comment count
		if(status.commentsCount>0)
		{
			facebook_stream_comments.setText(String.format("%1$s(%2$s)", getContext().getString(R.string.sns_add_comment), status.commentsCount));
		}
		else
		{
			facebook_stream_comments.setText(String.format("%1$s(%2$s)", getContext().getString(R.string.sns_add_comment), 0));
			//facebook_stream_comments.setText(R.string.sns_add_comment);
		}
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

