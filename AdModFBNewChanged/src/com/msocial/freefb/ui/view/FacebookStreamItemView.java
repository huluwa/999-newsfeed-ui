package com.msocial.freefb.ui.view;

import com.msocial.freefb.ui.view.CommentsSimpleView;
import com.msocial.freefb.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.service.SNSService;
import com.msocial.freefb.ui.FacebookAccountActivity;
import com.msocial.freefb.ui.FacebookAlbumViewActivity;
import com.msocial.freefb.ui.FacebookBaseActivity;
import com.msocial.freefb.ui.FacebookCommentsActivity;
import com.msocial.freefb.ui.FacebookPhotoCommentsActivity;
import com.msocial.freefb.ui.TwitterHelper;
import com.msocial.freefb.util.DateUtil;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Page;

import oms.sns.service.facebook.model.Stream;
import oms.sns.service.facebook.model.Stream.Attachment.Media;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.provider.Browser;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FacebookStreamItemView extends SNSItemView {
	private Stream item;	
	private final String TAG = "FacebookStreamItemView";
    Handler handler;
	private ImageView author_logo;
	private View      author_span;
    private TextView  author_name;
    private ImageView facebook_stream_icon;
    private TextView  publish_time;
    
    private View      message_content_span;
    private TextView  message_content;
    
	private View      photo_span;
	private ImageView photo_1;
	private ImageView photo_2;
	private ImageView photo_3;
	private TextView  photo_hint;
	
	private View      link_span;
    private ImageView link_photo;
    private TextView  link_title;
    private TextView  link_content;
	
    private View      video_span;
    private ImageView video_photo;
    private TextView  video_title;
	
	private TextView  message_title;
	private TextView  message_no_title;
	private TextView  message_des;
	
	private View      action_span;
	private Button    comments_add;
	private Button    like_add;
	
	private SocialORM orm;
	private FacebookUser user;
	private Page page;
	private View like_span;
	private ImageView stream_like;
	private TextView like_string;
	
	private View comment_span;
	private ImageView comment_icon;
	private TextView   comment_string;
	
	private View               share_comment_ll;
	private CommentsSimpleView comment_1;
	private CommentsSimpleView comment_2;
	private ImageView          stream_comment_divider;
	
    private boolean forwall = false;
	public FacebookStreamItemView(Context context, Stream di,boolean forwall) {
		super(context.getApplicationContext());
		this.forwall = forwall;
		mContext = context;
		item = di;
		orm = SocialORM.instance(mContext);
		Log.d(TAG, "call  FacebookStatusContentItemView");
		handler = new MessageHandler();
		init();
	}
    
    public Stream getStream()
    {
    	return item;
    }
    
	public FacebookStreamItemView(Context ctx, AttributeSet attrs) 
	{
		super(ctx.getApplicationContext(), attrs);		
		mContext = ctx;
		orm = SocialORM.instance(mContext);
		
		setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
        handler = new Handler();
    }		
	
	private void init() 
	{
		//if(SNSService.DEBUG)
		//Log.d(TAG,  "call FacebookStatusContentItemView init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();
		
		//container
		//FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);
		//FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		//view.setLayoutParams(paras);
		//view.setVerticalScrollBarEnabled(true);
		//addView(view);
		
		//child 1
		View v  = factory.inflate(R.layout.facebook_stream_item, null);		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		addView(v);
		
		author_logo = (ImageView)v.findViewById(R.id.facebook_stream_author_logo);
		//auth
		//author_span = (View)this.findViewById(R.id.facebook_stream_author_span);		
		author_name = (TextView)this.findViewById(R.id.facebook_stream_author_name);
		facebook_stream_icon = (ImageView)this.findViewById(R.id.facebook_stream_icon);
		publish_time= (TextView)this.findViewById(R.id.facebook_stream_publish_time);
		
		author_name.setMovementMethod(LinkMovementMethod.getInstance());
		author_name.setLinksClickable(true);
		 
		//message content
		//message_content_span = (View)this.findViewById(R.id.facebook_stream_message_content_span);
		message_content      = (TextView)this.findViewById(R.id.facebook_stream_message_content);
		
		//photo region
		photo_span   = (View)this.findViewById(R.id.facebook_stream_photo_span);
		photo_1      = (ImageView)this.findViewById(R.id.facebook_stream_photo_1);
		photo_2      = (ImageView)this.findViewById(R.id.facebook_stream_photo_2);
		photo_3      = (ImageView)this.findViewById(R.id.facebook_stream_photo_3);		
		photo_hint   = (TextView)this.findViewById(R.id.facebook_stream_photo_title);
		
		photo_1.setId(0);
		photo_2.setId(1);
		photo_3.setId(2);
		photo_1.setOnClickListener(imageClick);
		photo_2.setOnClickListener(imageClick);
		photo_3.setOnClickListener(imageClick);
		
		//link
		link_span   = (View)this.findViewById(R.id.facebook_stream_link_span);
		link_photo  = (ImageView)this.findViewById(R.id.facebook_stream_link_icon);
		link_photo.setId(0);
		link_photo.setOnClickListener(imageClick);
		link_title  = (TextView)this.findViewById(R.id.facebook_stream_link_title);
		link_content= (TextView)this.findViewById(R.id.facebook_stream_link_content);
				
	    video_span   = (View)this.findViewById(R.id.facebook_stream_video_span);
	    video_photo  = (ImageView)this.findViewById(R.id.facebook_stream_video_icon);
	    video_photo.setId(0);
	    video_photo.setOnClickListener(imageClick);
	    video_title  = (TextView)this.findViewById(R.id.facebook_stream_video_title);
		
	    message_no_title = (TextView)this.findViewById(R.id.facebook_stream_message_no_title);	    
		message_des    = (TextView)this.findViewById(R.id.facebook_stream_message_des);
		
		like_span   = (View)this.findViewById(R.id.facebook_stream_like_span);
		stream_like = (ImageView)this.findViewById(R.id.facebook_stream_like_image);
		like_string = (TextView)this.findViewById(R.id.facebook_stream_like_string);
		
		comment_span = (View)this.findViewById(R.id.facebook_stream_comment_span);
		comment_icon = (ImageView)this.findViewById(R.id.facebook_stream_comment_image);
		comment_string = (TextView)this.findViewById(R.id.facebook_stream_comment_string);
		
		//action span
		//action_span  = (View)this.findViewById(R.id.facebook_stream_action_span);
		comments_add = (Button)this.findViewById(R.id.facebook_stream_comments);
		like_add     = (Button)this.findViewById(R.id.facebook_stream_like);
		
		share_comment_ll = findViewById(R.id.share_comment_ll);
	    comment_1 = (CommentsSimpleView)findViewById(R.id.qiupu_stream_comment_1);
	    comment_2 = (CommentsSimpleView)findViewById(R.id.qiupu_stream_comment_2);
	    stream_comment_divider = (ImageView)findViewById(R.id.stream_comment_divider);
	    
	    comment_1.setOnClickListener(commentsOnClik);
		comment_2.setOnClickListener(commentsOnClik);
		
		setUI();
	}	
	
	public void refreshUI()
	{
		Log.d(TAG, "refresh UI");
		setUI();
	}
	
	public long getFromUID()
	{
		if(forwall)
		{
			if(SNSService.DEBUG)
			Log.d(TAG,"for wall is true get actor_id");
			return item.actor_id;
		}
		else
		{
			if(item.source_id != item.actor_id && item.actor_id != 0)
			{
				return item.actor_id;
			}
			
		    return item.source_id;
		}
	}
	
	private void setSourceImage()
	{	
		user = orm.getFacebookUser(getFromUID());
		if(user == null)
		{
			page = orm.getPageBypid(getFromUID());
			
			if(page == null)
			{
				getPageInfoAsyn();
			}
			else
			{
				item.frompage = true;
				getLogoImageBMP(page.pic_square, true);                                                                                                                                  
                updateUIFromUser(page.name, item.source_id);
			}
		}
		else
		{
			//get User imageurl and name. update UI
			item.frompage = false;
			getUserPicAndName(false);
		}
	}
	
	private void getUserPicAndName(boolean fromAnotherThread)
	{
		String pic_sq = user !=null?user.pic_square:null;		
		if(pic_sq == null)
		{
			long id = getFromUID();
			
			boolean getFromWeb=false;
			if(user == null)
			{
				getFromWeb = true;
			}
			else
			{
				if(SNSService.DEBUG)
				Log.d(TAG, "who am I="+user);
				String imageURL = user.pic_square;
				//no user data, maybe the user has image
				//we can't do this, it will lead a lot of user info request
				/*				 
				if((imageURL == null || imageURL.length() ==0 ) && (user.name == null || user.name.length() == 0))
				{
					getFromWeb = true;
				}*/
				
			}			    
		    //the person might have no pic, so no need to call this fun
		    if(getFromWeb == true)
			{	
				if(FacebookBaseActivity.class.isInstance(mContext))
				{
					AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
					if(af != null)
					{
						long[] uids = new long[1];
						uids[0] = id;
						af.getBasicUsersAsync(uids, new FacebookAdapter()
				    	{
				    		@Override public void getUsers(List<FacebookUser> users)
				            {
				    			if(users != null && users.size()>0)
				    			{
				    			    user = users.get(0);
				    				FacebookUser tmp = users.get(0);
                                    Log.d(TAG, "after get user info="+user);
				    				getLogoImageBMP(tmp.pic_square, true);									
									//update database
									orm.addFacebookUser(tmp);									
									updateUIFromUser(tmp.name, tmp.uid);
									tmp.despose();
									tmp = null;
				    			}
				    			
				            }
				    		
				            @Override public void onException(FacebookException e, int method) 
				            {
				            	Log.d(TAG, "fail to get the image");
				            	getLogoImageBMP(null, true);         	
				            }
				    	});
					}
				}
				
			}
			else
			{	
				getLogoImageBMP(user.pic_square, fromAnotherThread);//change from false
			}
			
		}
		else//I have get the image
		{
			getLogoImageBMP(pic_sq, fromAnotherThread);//change from false;
		}
	}
	
	private void getPageInfoAsyn()
	{
	    AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
        if(af != null)
        {
        	long pageid = getFromUID();
			af.getPageInfoAsync(pageid, new FacebookAdapter()
			{
				@Override public void getPageInfo(Page page)
				{
			         if(page != null)
			         {
			                 Log.d(TAG, "after get page info="+ page.pic_small + "=== name is "+page.name);  
			                 orm.insertPage(page);
			                 getLogoImageBMP(page.pic_square, true);                                                                                                                                  
			                 updateUIFromUser(page.name, page.page_id);
			                 item.frompage = true;
			         }
			         else
			         {
			        	 //get user from website
			        	 item.frompage = false;
			        	 getUserPicAndName(true);
			         }
				}
			 
				@Override public void onException(FacebookException e, int method) 
				{
					Log.d(TAG, "fail to get the image");
					getLogoImageBMP(null, true);            
				}
			});
        }
	}
	
	private void updateUIFromUser(String name, long uid)
	{
		Message msd = handler.obtainMessage(UPDATE_UI);
		msd.getData().putString("username", name);//sendToTarget();
		msd.getData().putLong("uid", uid);//sendToTarget();
		msd.sendToTarget();
	}
	
	private void resetLinkForProfile(String username, String rawname)
	{
		//if(SNSService.DEBUG)
		//Log.d(TAG, "username = "+username);
		author_name.setText(Html.fromHtml(username));
		SpannableString sb = (SpannableString)author_name.getText();
        SpannableString ss = new SpannableString(rawname);                
        URLSpan[] spans = author_name.getUrls();
        for (int i = 0; i < spans.length; i++) {
            int start = sb.getSpanStart(spans[i]);
            int end   = sb.getSpanEnd(spans[i]);
            String text = sb.subSequence(start, end).toString();
            
            //if(SNSService.DEBUG)
            //Log.d(TAG, "text="+text + " url="+spans[i].getURL());
            
            MyURLSPan my = new MyURLSPan(spans[i].getURL());
            ss.setSpan(my, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            //Log.i(TAG,"----- ss:"+ss);
            my = null;
        }
        
        //if(SNSService.DEBUG)
        //Log.d(TAG, "username  SpannableString = "+ss);
        author_name.setText(ss);
        ss = null;
        sb = null;
	}
	
	final int UPDATE_UI=0;
	final  int UPDATE_IMAGE_UI=1; 
	public class MessageHandler extends Handler
	{
	    public MessageHandler()
        {
            super();            
            Log.d(TAG, "new MessageHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
            	case UPDATE_UI:
            	{
            	    String name = msg.getData().getString("username");
            	    long   uid  = msg.getData().getLong("uid", -1);
            	    String username = formatWallToWall(name, uid, item.target_id);
            	    resetLinkForProfile(username, getRawText(name, uid));
            		break;
            	}
            	case UPDATE_IMAGE_UI:
            	{
            		String url = msg.getData().getString("imageurl");
            		ImageRun imagerun = new ImageRun(handler, url, 0);	
            		imagerun.use_avatar = true;
            		imagerun.setImageView(author_logo);            		
            		imagerun.post(imagerun);
            		break;
            	}
            }
        }	        
	}



	private void getLogoImageBMP(String url, boolean fromAnotherThread)
	{	
		if(fromAnotherThread == true)
		{
			Message msg = handler.obtainMessage(UPDATE_IMAGE_UI);
			msg.getData().putString("imageurl", url);			
			handler.sendMessage(msg);
		}
		else//from the same thread
		{
			ImageRun imagerun = new ImageRun(handler, url, 0);		
			imagerun.use_avatar = true;
    		imagerun.setImageView(author_logo);
    		imagerun.post(imagerun);
		}
	}
	
	View.OnClickListener imageClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 String href = "";
			 String src  = "";
			 int id = v.getId();
			 switch(id)
			 {
				 case 0:
				 {
				 	 href = item.attachment.attachments.get(0).href;
				 	 src  = item.attachment.attachments.get(0).src; 
				 	 break;
				 }
				 case 1:
				 {
					 href = item.attachment.attachments.get(1).href;
					 src  = item.attachment.attachments.get(1).src;
				 	 break;
				 }
				 case 2:
				 {
					 href = item.attachment.attachments.get(2).href;
					 src  = item.attachment.attachments.get(2).src;
					 break;
				 }
			 }
			 if(isEmpty(href) == false)
			 {
				 processUserNameClick(Uri.parse(href), src);
			 }
		}
	};
	
	private class MyURLSPan extends URLSpan
     {

         String url;
         public MyURLSPan(Parcel src) {
             super(src);                
         }
         
         public MyURLSPan(String src) {
             super(src);
             url = src;
         }

         @Override
         public String getURL() {                
             return super.getURL();
         }
         

        @Override
		public void updateDrawState(TextPaint ds) 
        {			
			//super.updateDrawState(ds);
        	ds.setColor(ds.linkColor);
            ds.setUnderlineText(false);
		}

		@Override
         public void onClick(View widget) {   
             SpannableString sb = (SpannableString)author_name.getText();
             URLSpan[] spans = author_name.getUrls();
                 
             int start = sb.getSpanStart(this);
             int end   = sb.getSpanEnd(this);
             String text = sb.subSequence(start, end).toString();
                 
             if(SNSService.DEBUG)
             Log.d("MyURLSPan", "click= text="+text + " url="+getURL());
             Uri uri = Uri.parse(getURL());
             processUserNameClick(uri, null);
         }            
     }
	
	 private void processUserNameClick(Uri uri, String src)
     {
         boolean openinBrowser = true;
         if(isPhoto(uri.toString()))
 		 {
 			String owner = uri.getQueryParameter("id");
 	    	String pid = uri.getQueryParameter("pid");
 	    	String aid = uri.getQueryParameter("aid");
 	    	
 	    	if(SNSService.DEBUG)
 	    	Log.d(TAG, "owner="+owner + " pid="+pid + " aid="+aid);
 	    	if(isEmpty(pid) == false)
 	    	{
 	    		 openinBrowser = false;
 	    		 String mergedpid = FacebookSession.mergePID_UID(pid,owner);
 	    		 Log.d(TAG, "open photo="+uri.toString() + "owner="+owner + " pid="+pid + " aid="+aid +" mergedpid="+mergedpid);
 	        	 Intent intent = new Intent(mContext, FacebookPhotoCommentsActivity.class);	
 	        	 intent.putExtra("forimageview", true);
 	             intent.putExtra("pid", mergedpid);
 	             intent.putExtra("owner",Long.parseLong(owner));
 	             intent.putExtra("fromoutside",  true);
 	             intent.putExtra("href", src);
 	             mContext.startActivity(intent);
 	    	}
 	    	else if(isEmpty(aid) == false)
 	    	{
 	    		 openinBrowser = false;
 	    	     String mergedaid = FacebookSession.mergePID_UID(aid,owner);
 	    		 Log.d(TAG, "open album="+uri.toString() + "owner="+owner + " pid="+pid + " aid="+aid+" mergedaid="+mergedaid);
 	             Intent intent = new Intent(mContext, FacebookAlbumViewActivity.class); 
 	             intent.putExtra("albumid",      mergedaid);
 	             intent.putExtra("owner",        Long.parseLong(owner));
 	             intent.putExtra("fromoutside",  true);
 	             intent.putExtra("href",         src);
                  mContext.startActivity(intent);                 
 	    	}
 		 }
         else if(isProfile(uri.toString()))
         {
             openinBrowser = false;
             Log.d(TAG, "open profile="+uri.toString());
             //launch profile
             //get id, we user user and page to get id
             String id = uri.getQueryParameter("id");
             Intent intent = new Intent(mContext, FacebookAccountActivity.class);                  
             intent.putExtra("frompage", item.frompage);
 			 if(item.frompage == false)
 			 {
 				 FacebookUser.SimpleFBUser targ = orm.getSimpleFacebookUser(Long.valueOf(id));
 				 if(targ != null)
 				 {
 				     intent.putExtra("uid",      targ.uid);
 				     intent.putExtra("username", targ.name);
 					 intent.putExtra("imageurl", targ.pic_square);				
 				 }
 				 else
 				 {
 					 Page page = orm.getPageBypid(Long.valueOf(id)); 					 
 					 if(page != null)
 					 {
 						 intent.putExtra("frompage", true);
 						 intent.putExtra("uid",       page.page_id);
 	 				     intent.putExtra("username", page.name);
 	 					 intent.putExtra("imageurl", page.pic_square);
 					 }
 					 else
 					 {
	 				     intent.putExtra("uid",      Long.valueOf(id));
	 				     intent.putExtra("username", id);
 					 }
 				 }
 			 }
 			 else//for page
 			 {	
 				 Page pp = orm.getPageBypid(Long.valueOf(id));
 			     if(pp != null)
 			 	 {
 			         Log.d(TAG, "open page="+id);
 			         
 				     intent.putExtra("uid",      pp.page_id);
 				     intent.putExtra("username", pp.name);
 					 intent.putExtra("imageurl", pp.pic_square);				
 				 }
 				 else
 				 {
 				     if(page != null )
 				     {
 				         Log.d(TAG, "I guess I am not page ="+id + " page_id="+page.page_id);
 				         String pagestr = String.valueOf(page.page_id);
 				         if(id.equalsIgnoreCase(pagestr) == false)
 				         {
 				             Log.d(TAG, "yes, you are not");
 				             intent.putExtra("frompage", false);
 				         }
 				     }
 				     intent.putExtra("uid",      Long.valueOf(id));
 				     intent.putExtra("username", id);				
 				 }
 			 }
             mContext.startActivity(intent);
             
         }        
         
         if(openinBrowser == true)
         {                               
             Intent intent = new Intent(Intent.ACTION_VIEW, uri);
             intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
             FacebookBaseActivity.formatFacebookIntent(intent, orm);                                
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
             mContext.startActivity(intent);
        }
    }
	 
	final static String profileURL = "http://www.facebook.com/profile.php?id=";
	private String formatWallToWall(String name, long uid, long targetid)
	{
		String username = name;
		/*if(item.frompage )
		{
			if(SNSService.DEBUG)
			Log.d(TAG, "I am from page");
			if(false)
			{
				username = String.format("<a href='%3$s%2$s'>%1$s</a>", username, uid , profileURL);
			}
			else//TODO
			{
			    if(targetid>0)
			    {
			        
			    }
				if(item.actor_id > 0 && item.actor_id != uid)
				{
					//TODO, what is for page			
					FacebookUser.SimpleFBUser target = orm.getSimpleFacebookUser(item.actor_id);
					if(target != null)
					{
						//format a html
						username = String.format("<a href='%5$s%3$s'>%1$s</a>><a href='%5$s%4$s'>%2$s</a>", username, target.name, uid ,target.uid, profileURL);				
					}
					else
					{
						username = String.format("<a href='%5$s%3$s'>%1$s</a>><a href='%5$s%4$s'>%2$s</a>", item.actor_id, username,item.actor_id , uid, profileURL);
					}			
				}       
				else
				{
					username = String.format("<a href='%3$s%2$s'>%1$s</a>", username, uid , profileURL);
				}
			}
		}
		else
		{*/
			if(item.target_id > 1)
			{
				//TODO, what is for page			
				FacebookUser.SimpleFBUser target = orm.getSimpleFacebookUser(item.target_id);
				if(target != null)
				{
					//format a html
					username = String.format("<a href='%5$s%3$s'>%1$s</a> > <a href='%5$s%4$s'>%2$s</a>", username, target.name, uid ,target.uid, profileURL);				
				}
				else
				{
					Page page = orm.getPageBypid(item.target_id);
					if(page != null)
					{
						username = String.format("<a href='%5$s%3$s'>%1$s</a> > <a href='%5$s%4$s'>%2$s</a>", username, page.name, uid,page.page_id, profileURL);
					}
					else
					{
					    username = String.format("<a href='%5$s%3$s'>%1$s</a> > <a href='%5$s%4$s'>%2$s</a>", username, item.target_id, uid,item.target_id, profileURL);
					}
				}			
			}       
			else
			{
				username = String.format("<a href='%3$s%2$s'>%1$s</a>", username, uid, profileURL);
			}
		//}
		return username;
	}
	/*
	 * name and uid is the content author(user/page) name and uid
	 */
	private String getRawText(String name, long uid)
	{
		String username = name;
		/*if(item.frompage )
		{
			//TODO
			if(false)
			{
				return username;
			}
			else
			{
				if(item.actor_id > 0 && item.actor_id != uid)
				{
					//TODO, what is for page			
					FacebookUser.SimpleFBUser target = orm.getSimpleFacebookUser(item.actor_id);
					if(target != null)
					{
						//format a html
						username = String.format("%1$s>%2$s", target.name, username);				
					}
					else
					{
						username = String.format("%1$s>%2$s", item.actor_id, username);
					}			
				}
			}
		}
		else
		{*/
			if(item.target_id > 1)
			{
				//TODO, what is for page
				FacebookUser.SimpleFBUser target = orm.getSimpleFacebookUser(item.target_id);
				if(target != null)
				{
					username = String.format("%1$s > %2$s", username, target.name);				
				}
				else
				{
					Page page = orm.getPageBypid(item.target_id);
					if(page != null)
					{
						username = String.format("%1$s > %2$s", username, page.name);
					}
					else
					{
						username = String.format("%1$s > %2$s", username, item.target_id);
					}					
				}
			}        	
		//}
		return username;
	}
	
	private void setUI()
	{
		try{
	    //clear firstly
	    author_logo.setImageResource(R.drawable.no_avatar);
	    author_name.setText("");
	    
		//set source image
		setSourceImage();
		
		//set user name, publish time
		if(user != null)
		{
			String username = formatWallToWall(user.name, user.uid, item.target_id);
		    resetLinkForProfile(username, getRawText(user.name, user.uid));            
		}
		else
		{
		    if(page != null)
		    {
    		    String username = formatWallToWall(page.name, page.page_id, item.target_id);
                resetLinkForProfile(username, getRawText(page.name, page.page_id));
		    }
		}
		
		facebook_stream_icon.setVisibility(View.VISIBLE);
		if(item.attachment != null && isEmpty(item.attachment.icon) == false)
		{
			ImageRun imagerun = new ImageRun(handler, item.attachment.icon, 0);
			imagerun.noimage = true;
			imagerun.setImageView(facebook_stream_icon);
			imagerun.post(imagerun);
		}
		else
		{
			facebook_stream_icon.setVisibility(View.GONE);
		}
		
		publish_time.setText(DateUtil.converToRelativeTime(mContext, new Date(item.created_time)));
		
		//set visible first		
		message_content.setVisibility(View.VISIBLE);		
		
		photo_span.setVisibility(View.GONE);
	    photo_1.setVisibility(View.GONE);
	    photo_2.setVisibility(View.GONE);
	    photo_3.setVisibility(View.GONE);
	    photo_hint.setVisibility(View.GONE);
	    
		link_span.setVisibility(View.GONE);
		link_content.setText("");
		boolean iamlinkres = false;
	        
		video_span.setVisibility(View.GONE);
		video_photo.setVisibility(View.GONE);
		video_title.setVisibility(View.GONE);
		
		message_no_title.setVisibility(View.GONE);
		
		//desc
		message_des.setVisibility(View.VISIBLE);
		
		//like
		like_span.setVisibility(View.VISIBLE);		
		if(message_title != null && message_title != link_title)
		{
			message_title.setVisibility(View.GONE);
		    message_title = null;
		}
		if(item.attachment != null && item.attachment.attachments != null)
		{
		    if(item.attachment.attachments.size() > 0)
		    {
		    	if(SNSService.DEBUG)
		    	Log.d(TAG, "media size="+item.attachment.attachments.size());
		    	
		        List<Media> media = item.attachment.attachments;
		        String type = media.get(0).type;
		        if(type.equals("link"))
		        {	
		        	iamlinkres = true;
		            link_span.setVisibility(View.VISIBLE);		
		            message_title= link_title;		            
		            
		            link_photo.setImageResource(R.drawable.nopics);
		            
		            //process link UI		     
		            String url = getURL(media.get(0).src);     
		            String localpath = TwitterHelper.isImageExistInPhone(url, true);
		            
		            if(isEmpty(localpath) ==false )
		            {
		                File file = new File(localpath);
		                if(file.exists() && file.length()< 50*1024)
		                {
    		            	try{
    			    			 Bitmap tmp = BitmapFactory.decodeFile(localpath);
    			    			 if(tmp != null)
    			    			 {
    			    			     if(tmp.getWidth() > 120)
    			    			     {
       			    			         Matrix matrix = new Matrix();
    	 		        		    	 //int h = 120;
                                         int h = (int)(120*getContext().getResources().getDisplayMetrics().density);
    	 		        		    	 Bitmap mBaseImage = tmp;
    	 		        		    	  
    	 		        		    	 Log.d(TAG, "image ="+mBaseImage);
    	                                 float scale = (float)h/(float)mBaseImage.getWidth();
    	 		        		         matrix.setScale(scale, scale);
    	 		        		         mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage
    	 		        		                  .getWidth(), mBaseImage.getHeight(), matrix, true);
    	 		        		            
    	 		        		        link_photo.setImageBitmap(mBaseImage);
    	 		        		        matrix = null;
    	 		        		        
    	 		        		        //tmp.recycle();
    			    			     }
    			    			     else
    			    			     {
    			    			    	 link_photo.setImageBitmap(tmp);
    			    			     }
    			    			 }
    		    			 }catch(Exception ne)
    		    			 {
    		    				 
    		    			     //should we remove the file, maybe the file is bad
    		    			     //
    		    			     try{
    		    			         new File(localpath).delete();
    		    			     }catch(Exception nee){}
    		    				 Log.d(TAG, "exception=+"+ne.getMessage());
    		    			 }
		                }
		            }
		            else
		            {
	                    ImageRun imagerun = new ImageRun(handler, url, 0);
	                    imagerun.need_compress = true;
	                    imagerun.addHostAndPath = true;
	                    imagerun.width      = 120;
	                    imagerun.noimage    = true;
	                    imagerun.need_scale = true;
		                imagerun.setImageView(link_photo);
		                imagerun.post(imagerun);
		            }
                	
	                photo_span.setVisibility(View.GONE);	                
		        }
		        else if(type.equals("photo"))
		        {
		            photo_span.setVisibility(View.VISIBLE);
		            message_title= photo_hint;		            
		            
		            //process photo UI   
		            if(media.size()>2)
                    {
		            	photo_3.setVisibility(View.VISIBLE);
                        String url = getURL(media.get(2).src);                
                        ImageRun imagerun = new ImageRun(handler, url, 0);
                        imagerun.addHostAndPath = true;
                        imagerun.setImageView(photo_3);
                        imagerun.post(imagerun);
                    }                    
                    if(media.size()>0)
                    {
                    	photo_1.setVisibility(View.VISIBLE);
		                String url = getURL(media.get(0).src);                
                        ImageRun imagerun = new ImageRun(handler, url, 0);
                        imagerun.addHostAndPath = true;
                        imagerun.setImageView(photo_1);
                        imagerun.post(imagerun);
                    }
                    if(media.size()>1)
                    {
                    	photo_2.setVisibility(View.VISIBLE);
                        String url = getURL(media.get(1).src);                
                        ImageRun imagerun = new ImageRun(handler, url, 0);
                        imagerun.addHostAndPath = true;
                        imagerun.setImageView(photo_2);
                        imagerun.post(imagerun);
                    }
                    
                    if(media.size() == 1)
                    {
                    	LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    	photo_1.setLayoutParams(params);
                    }
                    else if(media.size() > 1)
                    {
                    	//LayoutParams params = new LayoutParams(80, ViewGroup.LayoutParams.WRAP_CONTENT);
                    	LayoutParams params = (LayoutParams) photo_2.getLayoutParams();
                    	photo_1.setLayoutParams(params);
                    }
                    photo_hint.setVisibility(View.VISIBLE);
		        }
		        else if(type.equals("video"))
		        {
		            video_span.setVisibility(View.VISIBLE);
		            message_title= video_title;
		            video_photo.setVisibility(View.VISIBLE);
		            link_photo.setImageBitmap(null);
		            
		            //process video UI           
		            String url = getURL(media.get(0).src);                
                   /* ImageRun imagerun = new ImageRun(handler, url, 0);
                    imagerun.addHostAndPath = true;
                    imagerun.setImageView(video_photo);
                    imagerun.post(imagerun);*/
                        
                    String localpath = TwitterHelper.isImageExistInPhone(url, true);
                    if(isEmpty(localpath) ==false && new File(localpath).exists())
                    {
                        try{
                             Bitmap tmp = BitmapFactory.decodeFile(localpath);
                             if(tmp != null)
                             {
                                 if(tmp.getWidth() > 120)
                                 {
                                     Matrix matrix = new Matrix();                                     
                                     int h = (int)(120*getContext().getResources().getDisplayMetrics().density);
                                     Bitmap mBaseImage = tmp;
                                      
                                     Log.d(TAG, "image ="+mBaseImage);
                                     float scale = (float)h/(float)mBaseImage.getWidth();
                                     matrix.setScale(scale, scale);
                                     mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage
                                              .getWidth(), mBaseImage.getHeight(), matrix, true);
                                        
                                    video_photo.setImageBitmap(mBaseImage);
                                    matrix = null;
                                 }
                                 else
                                 {
                                     video_photo.setImageBitmap(tmp);
                                 }
                             }
                         }catch(Exception ne)
                         {
                             
                             //should we remove the file, maybe the file is bad
                             //
                             try{
                                 new File(localpath).delete();
                             }catch(Exception nee){}
                             Log.d(TAG, "exception=+"+ne.getMessage());
                         }
                    }
                    else
                    {
                        ImageRun imagerun = new ImageRun(handler, url, 0);
                        imagerun.need_compress = true;
                        imagerun.addHostAndPath = true;
                        imagerun.width      = 120;
                        imagerun.noimage    = true;
                        imagerun.need_scale = true;
                        imagerun.setImageView(video_photo);
                        imagerun.post(imagerun);
                    }
                    
                    video_title.setVisibility(View.VISIBLE);
                    photo_span.setVisibility(View.GONE);
		        }
		    }
		    else
		    {
		    	photo_span.setVisibility(View.GONE);
		    }		    
		}
		else
		{
			photo_span.setVisibility(View.GONE);
		}
		
		//for title
		if(message_title == null)
		{
			if(SNSService.DEBUG)
		    Log.d(TAG, "no link. photo, video, use default title");
		    message_no_title.setVisibility(View.VISIBLE);
		    
		    if(item.attachment != null)
		    {
			    if(isEmpty(item.attachment.name))
	            {
	                if(isEmpty(item.attachment.caption))
	                {
	                    message_no_title.setVisibility(View.GONE);                
	                }
	                else
	                {
	                	String msg = item.attachment.caption.trim().replaceAll("\r\n", "");
	         			msg = msg.replaceAll("\n", "");
	         			msg = removeHTML(msg, true);
	                    message_no_title.setText(msg);
	                }
	            }
	            else
	            {
	            	if(SNSService.DEBUG)
	                Log.d(TAG, "use name as title="+item.attachment.name);
	                String msg = item.attachment.name.trim().replaceAll("\r\n", "");
	    			msg = msg.replaceAll("\n", "");
	    			msg = removeHTML(msg, true);
	                message_no_title.setText(msg);                
	            }
		    }
		}
		else
		{
		    message_no_title.setVisibility(View.GONE);
		}
		
		if(message_title != null && item.attachment != null)
		{		
		    message_title.setVisibility(View.VISIBLE);
    		if(isEmpty(item.attachment.name))
            {
                if(isEmpty(item.attachment.caption))
                {
                    message_title.setVisibility(View.GONE);                
                }
                else
                {
                	String msg = item.attachment.caption.trim().replaceAll("\r\n", "");
        			msg = msg.replaceAll("\n", "");
        			msg = removeHTML(msg, true);
        			
        			if(SNSService.DEBUG)
        			Log.d(TAG, "1 msg="+msg);
        			
                    message_title.setText(msg);
                }
            }
            else
            {
            	if(SNSService.DEBUG)
                Log.d(TAG, "use name as title="+item.attachment.name);
            	
                String msg = item.attachment.name.trim().replaceAll("\r\n", "");
    			msg = msg.replaceAll("\n", "");
    			/*
    			if(isEmpty(item.attachment.caption) == false && msg.equalsIgnoreCase(item.attachment.caption) == false)
    			{
    				msg = msg + "\n\n" + item.attachment.caption;
    			}
    			*/
    			msg = removeHTML(msg, true);
    			
    			if(SNSService.DEBUG)
    			Log.d(TAG, "2 msg="+msg);
                message_title.setText(msg );                
                
                if(iamlinkres)
                {
	                if(isEmpty(item.attachment.caption) == false)
	            	{
	                	link_content.setVisibility(View.VISIBLE);
	                	String capture = removeHTML(item.attachment.caption, true);
	            	    link_content.setText(capture);
	            	}	      
	                else
	                {
	                	link_content.setVisibility(View.GONE);
	                }
                }
            }
		}
		
		//set message
		if(isEmpty(item.message))
		{
			if(isEmpty(item.attribution))
			{
				 message_content.setVisibility(View.GONE);
			}
			else
			{
				String msg = item.attribution.trim().replaceAll("\r\n", "");
				msg = msg.replaceAll("\n", "");
				msg = removeHTML(msg, false);
			    message_content.setText(msg);
			}
		}
		else
		{
			String msg = item.message.trim().replaceAll("\r\n", "");
			msg = msg.replaceAll("\n", "");
			msg = removeHTML(msg, false);
		    message_content.setText(msg);
		}
		
		//message region, 
		if(item.attachment != null )
		{
			List<Media> media = item.attachment.attachments;			
			//for des
			if(isEmpty(item.attachment.description))
			{
				message_des.setVisibility(View.GONE);
			}
			else
			{
				String content = item.attachment.description;
				String temp = removeHTML(content, false);
				
				int len = (temp.length()>150?150:temp.length()-1);
				String des = temp.substring(0, len);
				
				des = des.replaceAll("\r\n", "");
				des = des.replaceAll("\n", "");
			    message_des.setText(des);
			}
		}
		else
		{
			facebook_stream_icon.setVisibility(View.GONE);
			message_des.setVisibility(View.GONE);			
		}
		
		//like
		boolean IamLikeIt = false;
		if(item.likes != null && item.likes.count >0 )
		{
			like_span.setVisibility(View.VISIBLE);			
			
			//find friends like
			String userlist="";
			if(item.likes.friends != null)
			{
				for(int i=0;i<item.likes.friends.size();i++)
				{
					if(FacebookBaseActivity.class.isInstance(mContext))
					{
						FacebookSession af = ((FacebookBaseActivity)mContext).getFSession();
						long uid = af.getLogerInUserID();
						if(item.likes.friends.get(i) == uid)
						{
							IamLikeIt=true;
							continue;
						}
						
						FacebookUser user = orm.getFacebookUser(item.likes.friends.get(i));
						if(user != null && user.name != null)
						{
							if(i!= 0)
							{
								userlist  +=", ";
							}
							
						    userlist+=user.name;
						}
					}			
				}
			}
			
			//do I like?
			if(item.likes.user_likes > 0)
			{
				IamLikeIt = true;
			}
			
			if(IamLikeIt == false && userlist.length() == 0)
			{
			    like_span.setVisibility(View.GONE);
			}
			
			if(IamLikeIt == true || userlist.length() > 0)//set UI
			{
				like_span.setVisibility(View.VISIBLE);
				stream_like.setImageResource(R.drawable.ic_like);				
				if(IamLikeIt==true )
				{
					if(userlist.length()==0)
					{
					    like_string.setText(R.string.facebook_stream_you_like);
					}
					else
					{
						like_string.setText(userlist + " and " + mContext.getString(R.string.facebook_stream_you_like));
					}
				}	
				else
				{
					like_string.setText(userlist + " like this.");
				}
			}
		}
		else
		{
			like_span.setVisibility(View.GONE);
		}

		//action region
		comments_add.setText(R.string.facebook_comment_btn_txt);
		comments_add.setOnClickListener(commentsOnClik);
		if(IamLikeIt == false)
		{
		    like_add.setText(R.string.sns_like);
		    like_add.setOnClickListener(likeOnClik);
		}
		else
		{
			like_add.setText(R.string.sns_unlike);
			like_add.setOnClickListener(removelikeOnClik);
		}
		
		//
		if(item.comments != null)
		{
			if( item.comments.count > 0)
			{
			    String commentstr = (item.comments.count==1)?
			                         mContext.getString(R.string.sns_add_comment_stream_sig):
			                         mContext.getString(R.string.sns_add_comment_stream_multi);
				comments_add.setText(item.comments.count+" " +commentstr);
			}
			
			if(item.likes != null && item.likes.count > 0)
			{	
			    String likestr = (item.likes.count==1)?
			                      " 1 "+mContext.getString(R.string.sns_like_stream_sig):
			                      " "+item.likes.count + " "+mContext.getString(R.string.sns_like_stream_multi);
			    if(item.likes.count == 1 && IamLikeIt == true)
			    {
			        likestr = mContext.getString(R.string.sns_unlike);
			    }
				like_add.setText(likestr);				
			}		
		}
		
		//show comments
		if(item.comments != null && item.comments.stream_posts.size()>0)
		{
			if(share_comment_ll.getBackground() == null)
			{
			    share_comment_ll.setBackgroundResource(R.drawable.comments_bg);
			}
			stream_comment_divider.setVisibility(View.VISIBLE);
		    comment_1.setVisibility(View.VISIBLE);
		    comment_1.setCommentItem(item.comments.stream_posts.get(0));
		    
		    if(item.comments.stream_posts.size()>1)
		    {
		    	comment_2.setVisibility(View.VISIBLE);
			    comment_2.setCommentItem(item.comments.stream_posts.get(1));
		    }
		    else
		    {
		    	comment_2.setVisibility(View.GONE);
		    }
		}
		else
		{
			share_comment_ll.setBackgroundDrawable(null);
			comment_2.setVisibility(View.GONE);
			comment_1.setVisibility(View.GONE);
			stream_comment_divider.setVisibility(View.GONE);
		}
		
		this.requestLayout();
		}catch(Exception ne){}
	}
	private String getURL(String url)
	{
		if(SNSService.DEBUG)
	    Log.d(TAG, "image path="+url);
	    
	    String tmp = url;
	    if(url.indexOf("src=http")>0)
        {
            url = url.substring(url.indexOf("src=http")+4);
            tmp = java.net.URLDecoder.decode(url);
        }
	    else if(url.indexOf("url=http")>0)
        {
            url = url.substring(url.indexOf("url=http")+4);
            tmp = java.net.URLDecoder.decode(url);
        }
	    
	    if(tmp.indexOf(".png")>0)
	    {
	        tmp = tmp.substring(0, tmp.indexOf(".png")+4);
	    }
	    else if(tmp.indexOf(".jpg")>0)
        {
            tmp = tmp.substring(0, tmp.indexOf(".jpg")+4);
        }
	    else if(tmp.indexOf(".jpeg")>0)
        {
            tmp = tmp.substring(0, tmp.indexOf(".jpeg")+5);
        }
	    else if(tmp.indexOf(".gif")>0)
        {
            tmp = tmp.substring(0, tmp.indexOf(".gif")+4);
        }
	    else if(tmp.indexOf(".bmp")>0)
        {
            tmp = tmp.substring(0, tmp.indexOf(".bmp")+4);
        }
	    
	    if(SNSService.DEBUG)
		    Log.d(TAG, "image path after adjust="+tmp);
	    
	    return tmp;
	}
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
		
	public void setStreamItem(Stream di,boolean forwall) {
		item = di;
		this.forwall = forwall;
		if(user != null)
		{
    		user.despose();
    		user = null;
		}
		
		if(page !=null)
		{
		    page.despose();
		    page = null;
		}
		
		setUI();		
	}

	public void chooseStreamListener() 
	{
		//choose comments and like
		author_logo.setOnClickListener(viewUserDetailsClick);
		//author_name.setOnClickListener(viewUserDetailsClick);
		comments_add.setOnClickListener(commentsOnClik);
		if(message_title!= null)
		{
		    message_title.setOnClickListener(sourceOnClik);
		}
	}	
	
	View.OnClickListener viewUserDetailsClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "viewUserDetailsClick you click first one=");	
			Intent intent = new Intent(mContext, FacebookAccountActivity.class);
			intent.putExtra("frompage", item.frompage);
			if(item.frompage == false)
			{
				if(user != null)
				{
				    intent.putExtra("uid",      user.uid);
				    intent.putExtra("username", user.name);
					intent.putExtra("imageurl", user.pic_square);				
				}
				else
				{
				    intent.putExtra("uid",      item.source_id);
				    intent.putExtra("username", String.valueOf(item.source_id));				
				}
			}
			else//for page
			{	
				if(page != null)
				{
				    intent.putExtra("uid",      page.page_id);
				    intent.putExtra("username", page.name);
					intent.putExtra("imageurl", page.pic_square);				
				}
				else
				{
				    intent.putExtra("uid",      item.source_id);
				    intent.putExtra("username", String.valueOf(item.source_id));				
				}
			}
			((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
		}
	};
	
	View.OnClickListener commentsOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "commentsOnClik you click first one=");
			 
			 Intent intent = new Intent(mContext, FacebookCommentsActivity.class);	
			 Stream.CommentsParcel cp = new Stream.CommentsParcel(item.comments,item.source_id);
			 intent.putExtra("comments", cp);
			 intent.putExtra("post_id", item.post_id);			 
			 ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_COMMENTS);
		}
    };
    
    
    
    View.OnClickListener sourceOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "sourceOnClik you click first one=");		
			 //call browser to view
			 if(item.attachment.href != null && item.attachment.href.length()>0)
			 {
				 processUserNameClick(Uri.parse(item.attachment.href), item.attachment.icon);
				 /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.attachment.href));
		         intent.setData(Uri.parse(item.attachment.href));
		         mContext.startActivity(intent);*/
			     /*
				 Intent intent = new Intent(Intent.ACTION_VIEW);    	 	        	
				 intent.setData(Uri.parse(item.attachment.href)); 
	 	         FacebookBaseActivity.formatFacebookIntent(intent, orm);
	 	         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             	 mContext.startActivity(intent);*/
			 }
		}
    };
    
    View.OnClickListener likeOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "likeOnClik you click first one=");
			 if(FacebookBaseActivity.class.isInstance(mContext));
			 {
				 FacebookBaseActivity act = (FacebookBaseActivity)mContext;
				 
				 Message msd = act.getBasicHandler().obtainMessage(FacebookBaseActivity.LIKE_STREAM);
				 msd.getData().putString("post_id", item.post_id);
				 act.getBasicHandler().sendMessage(msd);
			 }
			 
		}
    };
    
    View.OnClickListener removelikeOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "removelikeOnClik you click first one=");
			 if(FacebookBaseActivity.class.isInstance(mContext));
			 {
				 FacebookBaseActivity act = (FacebookBaseActivity)mContext;
				 
				 Message msd = act.getBasicHandler().obtainMessage(FacebookBaseActivity.UNLIKE_STREAM);
				 msd.getData().putString("post_id", item.post_id);				 
				 act.getBasicHandler().sendMessage(msd);
			 }			 
		}
    };
    
    
    View.OnClickListener viewCommentsClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "viewCommentsClick you click first one=");
			 if(FacebookBaseActivity.class.isInstance(mContext));
			 {
				 Intent intent = new Intent(mContext, FacebookCommentsActivity.class);
				 //set commends data to show
				 Stream.CommentsParcel cp = new Stream.CommentsParcel(item.comments,item.source_id);
				 intent.putExtra("comments", cp);
				 intent.putExtra("post_id", item.post_id);					 
		         mContext.startActivity(intent);
			 }			 
		}
    };
    
   
	@Override
	public String getText() 
	{		
		return item.message;
	}
	
	@Override
	public List<String> getLinks()
	{
		List<String> links = new ArrayList<String>();
		if(item.links != null)
		{
			for(int i=0;i<item.links.links.size();i++)
			{
			    links.add(item.links.links.get(i).href);
			}
		}
		if(item.attachment != null)
		{
			for(int i=0;i<item.attachment.attachments.size();i++)
			{
			    links.add(item.attachment.attachments.get(i).href);
			}
		}		
		return links;
	}
}
