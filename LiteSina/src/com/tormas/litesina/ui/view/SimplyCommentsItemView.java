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

import twitter4j.SimplyComments;
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

public class SimplyCommentsItemView extends SNSItemView {
    private String baseURL = "http://api.t.sina.com.cn/";//"http://api.twitter.com/";
	private final String TAG="SimplyCommentsItemView";
	private ImageView imageView;	
	private TextView publishDate;
	private TextView publishTxt;
	private TextView username;	
	
	private TextView facebook_stream_comments;
	private TextView facebook_stream_like;	
	private ImageView  tweet_pic_res;
	
	private SimplyComments status;	
	String  imageURL;
	Handler handler;
	int type;//0, status, 1 tweet
	java.util.Random random = new java.util.Random();
	private boolean showOnlyText=false;
	
	public SimplyCommentsItemView(Context ctx, AttributeSet attrs) 
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
	public SimplyComments getStatus()
	{
		return status;
	}
	
	
	public SimplyCommentsItemView(Context context, SimplyComments di) 
	{		
		super(context);
		mContext = context;
		status = di;
		
		Log.d(TAG, "call SimplyStatusItemView");
		type = 0;
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
        if(status != null)
            ret = status.getUser().getScreenName();
        
        return ret;
    }
	
	public long getStatusID()
	{
	    long ret = -1;
	    if(status != null)
	        ret = status.getId();
	    
	    return ret;
	}
	public String getText()
    {
        String ret = "";
        if(status != null)
            ret = status.getText();
        
        return ret;
    }
	public Object GetContent()
	{
	    Object ret = null;
        if(status != null)
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
		
		facebook_stream_comments = (TextView)findViewById(R.id.facebook_stream_comments);
		facebook_stream_like = (TextView)findViewById(R.id.facebook_stream_like);		
		tweet_pic_res = (ImageView)findViewById(R.id.tweet_pic_res);
		
		facebook_stream_comments.setOnClickListener(commetnsOnClik);
		facebook_stream_like.setOnClickListener(likeOnClik);
		
		if(type == 0 && status != null)
		{			
			setStatusItem(status);			
		}		
	}	
	
	View.OnClickListener commetnsOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Intent intent = new Intent(mContext, TwitterComposeActivity.class);             
	         intent.putExtra(TwitterBaseActivity.STATUS_ID,     status.id);    
	         intent.putExtra(TwitterBaseActivity.TWITTER_ID,    String.valueOf(status.user.screenName)); 
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
		}
	};
	

	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	

	public void setStatusItem(SimplyComments di) 
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
	}
	
	private String getText(SimplyComments status2) {
        String str = String.format("<a href='%1$s'>"+status2.getUser().getScreenName()+"</a>",baseURL+status2.getUser().getScreenName());
        str = str + " "+status2.getText();
        //Log.d(TAG, "SimplyStatusItemView is "+str);
        return str;
    }

    public CharSequence getName() {		
		return status.getUser().name;		
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

