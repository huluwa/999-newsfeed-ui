package com.msocial.freefb.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.msocial.freefb.*;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.service.SNSService;
import com.msocial.freefb.ui.FacebookAlbumActivity.FacebookAlbumItemView;
import com.msocial.freefb.ui.adapter.FacebookCommentsAdapter;
import com.msocial.freefb.ui.adapter.FacebookStatusAdapter;
import com.msocial.freefb.ui.adapter.FacebookStreamAdapter;
import com.msocial.freefb.ui.view.FacebookStreamItemView;
import com.msocial.freefb.ui.view.ImageRun;
import com.msocial.freefb.util.DateUtil;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Page;
import oms.sns.service.facebook.model.PhoneBook;
import oms.sns.service.facebook.model.Photo;
import oms.sns.service.facebook.model.PhotoAlbum;
import oms.sns.service.facebook.model.Stream;
import oms.sns.service.facebook.model.UserStatus;
import oms.sns.service.facebook.model.Stream.Comments.Stream_Post;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

public class FacebookAccountActivity extends FacebookBaseActivity
{
	final String TAG="FacebookAccountActivity";
	
    private ImageView imageView;
	private TextView  facebook_username;
	private TextView  facebook_user_status;
	private TextView  facebook_publish_time;
	private ListView  status_list;
	private ListView  facebook_photo_list;	
	private View      facebook_more_info_span;
	private boolean frompage;
	private Page page;
	//private TextView  facebook_user_info;
	
	protected long uid;
	protected String username;
	private String imgURL;
	protected boolean comefrommyself;
	//private List<UserStatus> statuses;
	private List<Stream> streams = new ArrayList<Stream>();
	private List<PhotoAlbum> albums;
	private FacebookUser extUser;
	private PhoneBook phonebook;	
	private boolean isFriend;
    
    private Button wall_button;
    private Button info_button;
    private Button photo_button;
    
    private int     showIndex = 0;//0 wall, 1 info, 2 photo    
    private final int  WALL =0;
    private final int  INFO =1;
    private final int  PHOTO =2;
    int width = 50;
    
    private WebView facebook_info;
    
    private Button add_as_friend_button;
    
   // private View facebook_status_button_span;
   // private Button facebook_update_status_button;
   // private Button facebook_add_photo_button;
    private View facebook_wall_compose_span;
    private EditText facebook_wall_editor;
    private Button facebook_wall_post;
    private boolean isAddAsFriend = false;
    private boolean isPoking      =  false; 
    private boolean force_deserialize = false;
    
    int limit=20;
	int viewsize=50;
	long starttime;
	private int lastVisiblePos  = 0;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.facebook_account_ui);
        
        Log.i(TAG, "onCreate");
        imageView = (ImageView)this.findViewById(R.id.facebook_img_ui);
        
        frompage = this.getIntent().getBooleanExtra("frompage", false);
		int res = R.drawable.no_avatar;
		if(frompage)
		{
		    res = R.drawable.pages;
		}		
		setProfileImage(res);
		
        facebook_username = (TextView)this.findViewById(R.id.facebook_username);
        facebook_user_status = (TextView)this.findViewById(R.id.facebook_user_status);
        facebook_publish_time = (TextView)this.findViewById(R.id.facebook_publish_time);
        status_list = (ListView)this.findViewById(R.id.facebook_recent_status_list);        
        status_list.setFocusableInTouchMode(true);
        status_list.setFocusable(true);
        status_list.setSelected(true);
        status_list.setClickable(true);        
        status_list.setOnCreateContextMenuListener(this);
        
        facebook_photo_list = (ListView)this.findViewById(R.id.facebook_photo_list);        
        facebook_photo_list.setFocusableInTouchMode(true);
        facebook_photo_list.setFocusable(true);
        facebook_photo_list.setSelected(true);
        facebook_photo_list.setClickable(true);        
        facebook_photo_list.setOnCreateContextMenuListener(this);
        facebook_photo_list.setOnItemClickListener(itemClick);
        
        View v = findViewById(R.id.progress_horizontal);
		if(v != null)
		{
		    progressHorizontal = (ProgressBar) v;
		}
		
		wall_button = (Button)this.findViewById(R.id.facebook_tab_wall_button);
		wall_button.setId(1);
        info_button = (Button)this.findViewById(R.id.facebook_tab_info_button);
        info_button.setId(2);        
        photo_button = (Button)this.findViewById(R.id.facebook_tab_photo_button);
        photo_button.setId(3);
        
        wall_button.setOnClickListener(showContentClick);
        info_button.setOnClickListener(showContentClick);
        photo_button.setOnClickListener(showContentClick);
        
        facebook_more_info_span = (View)this.findViewById(R.id.facebook_more_info_span);
        //facebook_user_info      = (TextView)this.findViewById(R.id.facebook_user_info);       
        add_as_friend_button = (Button)this.findViewById(R.id.facebook_add_as_friend_button);
        add_as_friend_button.setOnClickListener(addFriendClick);
        
        width = (int)(width*(getResources().getDisplayMetrics().density));
        
        initInfo();         
        constructUI(getIntent());
        initStatus();
    } 
	
    @Override
    public void onLogin() 
	{		
    	super.onLogin();
    	
    	Log.d(TAG, "call onLogin="+this);
    	
    	if(facebookA != null)
    	{
    		loadAction();
    	}
	}
    
    @Override
    protected void doAfterLoginNothing()
    {
    	Log.d(TAG, "after login");
    	setResult(RESULT_CANCELED);
    	finish();
    }
    
	private String newsfeed_sfile=TwitterHelper.getTmpCachePath();
	
	private class DeSerializationTask extends android.os.AsyncTask<Void, Void, Void>
    {       
        public DeSerializationTask()
        {
            super();            
            Log.d(TAG, "create DeSerializationTask");
        }

		@Override
		protected Void doInBackground(Void... params)			  
        {
			deSerialization();
            return null;
        }
    }
	
	boolean searialzed = false;
	@Override protected void onPause() 
    {   
        super.onPause();  
        if(searialzed == false)
            serialization();
    }
	  
	@Override protected void onDestroy() 
    {   
        if(searialzed == false)
        {
            serialization();
        }
        
        //clear streams
        synchronized(streams)
        { 
	        if(streams != null)
	        {
	        	for(Stream item: streams)
	        	{
	        		item.dispose();
	        	}
	        	streams.clear();
	        	streams = null;        	
	        }
        }
        
        //clear albums
        if(albums != null)
        {
	        synchronized(albums)
	        { 
		    	if(albums != null)
		    	{
		    		for(PhotoAlbum item:albums)
		    		{
		    			item.dispose();
		    		}
		    		albums.clear();
		    		albums = null;
		    	}
	        }
        }
    	
        ImageRun.revokeAllImageView(this);
        
        //is this useful?
        facebook_info.destroy();
        facebook_info = null;
        
        //imageView = null;
        
        clearAsyncFacebook(true);    
    	super.onDestroy();
    }
	
	private void deSerialization()
	{
		if(force_deserialize == false && donotcallnetwork == true)
		{
			return;
		}
		
		if(streams != null)
		{
			synchronized(streams)
			{
			    //clear UI firstly
			    handler.post( new Runnable()
			    {
			        public void run()
			        {
			            status_list.setAdapter(null);
		                streams.clear();
			        }
			    });
			    //clear content
				
				Log.d(TAG, "begin to deSerialization="+newsfeed_sfile);
				FileInputStream fis = null;
				ObjectInputStream in = null;
				try{
				    fis = new FileInputStream(newsfeed_sfile);
				    in = new ObjectInputStream(fis);
				    long lastrecord = in.readLong();
				    Date now = new Date();
				    
				    if((now.getTime() -lastrecord) >10*24*60*60*1000L)
				    {
				    	Log.d(TAG, String.format("it is %1%s hours ago, ignore the data", (now.getTime() -lastrecord)/(60*60*1000)));
				    	in.close();
				    	return ;
				    }
				    
				    int count = in.readInt();
				    for(int i=0;i<count;i++)
				    {
				    	Stream item = (Stream) in.readObject();
				    	item.isFromSerialize = true;
				    	streams.add(item);
				    }
				    in.close();
				    
				    handler.obtainMessage(FACEBOOK_GET_STATUS_UPDATES_UI).sendToTarget();
				}
				catch(IOException ex)
				{
					Log.d(TAG, "deserialization fail="+ex.getMessage());
				}
				catch(ClassNotFoundException ex)
				{
					Log.d(TAG, "deserialization fail="+ex.getMessage());
				}
				catch(Exception ex)
	            {
	                Log.d(TAG, "deserialization fail="+ex.getMessage());
	            }
			}
		}
		force_deserialize = false;
	}
	
	private void serialization()
	{
		if(streams != null && streams.size() > 0)
		{
			synchronized(streams)
			{
				Log.d(TAG, "begin to searlize="+newsfeed_sfile);
				FileOutputStream fos = null;
				ObjectOutputStream out = null;
				try
				{
				    fos = new FileOutputStream(newsfeed_sfile);
				    out = new ObjectOutputStream(fos);
				    Date date = new Date();
				    out.writeLong(date.getTime());
				    int count = streams.size();
				    //just cache last 10 items
				    if(count > 10)
				    	count = 10;
				    out.writeInt(count);
				    for(int i=0;i<count;i++)
				    {
				    	Stream item = streams.get(i);
				    	out.writeObject(item);
				    }
				    
				    out.close();
				}
				catch(IOException ex)
				{
				    Log.d(TAG, "serialization fail="+ex.getMessage());
				}
			}
		}
	}
	
	
	private void initStatus()
	{  
	    facebook_wall_compose_span = (View)this.findViewById(R.id.facebook_wall_compose_span);
	    facebook_wall_editor = (EditText)this.findViewById(R.id.facebook_wall_editor);
	    facebook_wall_post = (Button)this.findViewById(R.id.facebook_wall_post);
	    facebook_wall_post.setText(R.string.facebook_wall_share);
        facebook_wall_post.setVerticalScrollBarEnabled(true); 
        
        Paint p = facebook_wall_post.getPaint();
        float internalwidth = p.measureText(facebook_wall_post.getText().toString());
        p = null;   
        facebook_wall_post.getLayoutParams().width = (int)internalwidth + 8;
        p = null;
        
        if(comefrommyself)
        {
           facebook_wall_editor.setHint(R.string.facebook_status_update_compose);
        }
        else
        {
            facebook_wall_editor.setHint(R.string.facebook_wall_to_wall_compose);
        }
        
        facebook_wall_editor.requestFocus();
        facebook_wall_post.setOnClickListener(wallPostOnClik);
	    updateSubTabUI();
	}
	
	View.OnClickListener wallPostOnClik = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            handler.obtainMessage(FACEBOOK_WALL_POST).sendToTarget();
        }
    };

    @Override
	protected void updateComments(String post_id, List<String>comments)
    {
    	synchronized(streams)
    	{
	    	if(streams != null)
	    	{
	    	    for(int i=0;i<streams.size();i++)
	    	    {
	    	    	Stream item = streams.get(i);
	    	    	if(item.post_id.equals(post_id))
	    	    	{
	    	    	    item.comments.count += comments.size();
	    	    	    Log.d(TAG, "after update comments="+post_id);
	    	    	    updateView(post_id);    	    	    
	    	    	    break;
	    	    	}
	    	    }
	    	}
    	}
    }
    
    private void updateView(String post_id)
    {
    	 //process for UI
    	for(int j=0;j<status_list.getChildCount();j++)    		 
        {
            View v = status_list.getChildAt(j);
            if(FacebookStreamItemView.class.isInstance(v))
            {
            	FacebookStreamItemView fv = (FacebookStreamItemView)v;
            	if(fv.getStream().post_id.equals(post_id))
            	{
	            	fv.refreshUI();
	            	break;
            	}					            	
            }
        }
    }
    
    @Override
    protected boolean  goNextPage()
	{
    	super.goNextPage();
    	boolean changed = false;
    	if(showIndex == WALL)
    	{	
    		changed = true;
    		showIndex = INFO;    
	    }
    	else if(showIndex == INFO)
    	{
    		changed   = true;
            showIndex = PHOTO;
    	}
    	else if(showIndex == PHOTO)
    	{
    		showIndex = PHOTO;
    	}    
    	if(changed == true)
    	{
	        updateSubTabUI();
	        return true;
    	}
    	return false;
	}
    
    @Override
	protected boolean  goPrePage()
	{
    	super.goPrePage();    	
    	boolean changed = false;
    	if(showIndex == WALL)
    	{	
    		showIndex  = WALL; 	         	        
	    }
    	else if(showIndex == INFO)
    	{
    		changed   = true;
    		showIndex  = WALL;  	        
  	        facebook_wall_editor.requestFocus();
    	}
    	else if(showIndex == PHOTO)
    	{
    		changed   = true;
    		showIndex  = INFO;  	        
    	}    
    	if(changed == true)
    	{
	        updateSubTabUI();
	        return true;
    	}
    	return false;
	}
    
	@Override
	protected void removeLikeAction(final String pid, boolean suc)
	{
		if(suc && streams != null)
		{
			synchronized(streams)
			{
				for(int i=0;i<streams.size();i++)
				{
					if(streams.get(i).post_id.equals(pid))
					{
						streams.get(i).likes.user_likes--;
						streams.get(i).likes.count--;
						handler.post( new Runnable()
						{
							public void run()
							{
						        //process for UI
						    	for(int j=0;j<status_list.getChildCount();j++)    		 
						        {
						            View v = status_list.getChildAt(j);
						            if(FacebookStreamItemView.class.isInstance(v))
						            {
						            	FacebookStreamItemView fv = (FacebookStreamItemView)v;
						            	if(fv.getStream().post_id.equals(pid))
						            	{
							            	fv.refreshUI();
							            	break;
						            	}					            	
						            }
						        }
							}
						});
						break;
					}
				}
			}
		}
	}
	
	@Override
	protected void addLikeAction(final String pid, boolean suc)
	{
		if(suc && streams != null)
		{
			synchronized(streams)
			{
				for(int i=0;i<streams.size();i++)
				{
					if(streams.get(i).post_id.equals(pid))
					{
						streams.get(i).likes.user_likes++;
						streams.get(i).likes.count++;
						handler.post( new Runnable()
						{
							public void run()
							{
						        //process for UI
						    	for(int j=0;j<status_list.getChildCount();j++)    		 
						        {
						            View v = status_list.getChildAt(j);
						            if(FacebookStreamItemView.class.isInstance(v))
						            {
						            	FacebookStreamItemView fv = (FacebookStreamItemView)v;
						            	if(fv.getStream().post_id.equals(pid))
						            	{
							            	fv.refreshUI();
							            	break;
						            	}					            	
						            }
						        }
							}
						});
						break;
					}
				}
			}
		}
	}
		
	private void initInfo()
	{
		facebook_info = (WebView)this.findViewById(R.id.info_view);     
		WebSettings ws = facebook_info.getSettings();
		ws.setDefaultTextEncodingName("utf-8");		
		ws = null;
	}
	
	View.OnClickListener addFriendClick = new View.OnClickListener()
	{
	    public void onClick(View v)
	    { 
	        if(isFriend())
	        {
	            PokeHim();
	        }
	        else
	        {
	            addAsFriends();
	        }
	        
	    }
	};
	
	AdapterView.OnItemClickListener itemClickForBrowser = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
        {
            if(FacebookAlbumItemView.class.isInstance(v))
            {
                PhotoAlbum album = ((FacebookAlbumItemView)v).getPhotoAlbum();
                if(album != null)
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(album.link));
                    intent.setData(Uri.parse(album.link));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    SocialORM.Account ac = orm.getFacebookAccount();
	 	        	intent.putExtra("forfacebook", true);
	 	        	intent.putExtra("email", ac.email);
	 	        	intent.putExtra("password", ac.password);
                    mContext.startActivity(intent);
                }    
            }
        }
    };
    
    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
        {
            if(FacebookAlbumItemView.class.isInstance(v))
            {
                PhotoAlbum album = ((FacebookAlbumItemView)v).getPhotoAlbum();
                if(album != null)
                {
                    Intent intent = new Intent(mContext, FacebookAlbumViewActivity.class);                    
                    intent.putExtra("photo_album", album);                    
                    mContext.startActivity(intent);
                }    
            }
        }
    };
	    
	public long getAccountUID()
	{
		return uid;
	}
	
	View.OnClickListener showContentClick = new OnClickListener()
	{
	    public void onClick(View v)
	    {
	        boolean changed = false;
	        if(v.getId() == 1)
	        {
	            if(showIndex != WALL)
	            {
	                changed = true;
	            }
    	        showIndex = WALL;    	        
    	        facebook_wall_editor.requestFocus();
	        }
	        else if(v.getId() ==2)
	        {
	            if(showIndex != INFO)
                {
                    changed = true;
                }
	            showIndex = INFO;	            
	        }
	        else if(v.getId() ==3)
            {
	            if(showIndex != PHOTO)
                {
                    changed = true;
                }
	            showIndex = PHOTO;	            
            }
	        if(changed == true)//don't repeat to show
	        {
	            updateSubTabUI();
	        }
	    }
	};

	private void updateSubTabUI()
    {
        //if for status, show wall, hide others
        if(showIndex == WALL)
        {
            wall_button.setBackgroundResource(R.drawable.facebook_profile_button_white);                
            info_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);
            photo_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);
            
            wall_button.setTextColor(Color.BLACK);        
            photo_button.setTextColor(Color.WHITE);
            info_button.setTextColor(Color.WHITE);
          
            facebook_more_info_span.setVisibility(View.GONE);
            facebook_photo_list.setVisibility(View.GONE);
            status_list.setVisibility(View.VISIBLE);
            
            facebook_wall_compose_span.setVisibility(View.VISIBLE);
            
            //set adapter            
            if(streams != null && streams.size() > 0)
            {
               // FacebookStatusAdapter fs = new FacebookStatusAdapter(FacebookAccountActivity.this, statuses);
               // status_list.setAdapter(fs);
            	FacebookStreamAdapter af = new FacebookStreamAdapter(FacebookAccountActivity.this, streams,true);            	
        		status_list.setAdapter(af);
        		status_list.setSelection(lastVisiblePos);
            }            
        }
        
        //if for info, hide other
        else if(showIndex == INFO)
        {                            
        	wall_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);                
            info_button.setBackgroundResource(R.drawable.facebook_profile_button_white);
            photo_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);
            
            wall_button.setTextColor(Color.WHITE);
            info_button.setTextColor(Color.BLACK);
            photo_button.setTextColor(Color.WHITE);            
          
            facebook_more_info_span.setVisibility(View.GONE);
            facebook_photo_list.setVisibility(View.GONE);
            status_list.setVisibility(View.VISIBLE);
            
            facebook_more_info_span.setVisibility(View.VISIBLE);
            facebook_photo_list.setVisibility(View.GONE);
            status_list.setVisibility(View.GONE);
            facebook_wall_compose_span.setVisibility(View.GONE);
            
            //load from cache
            if(page == null  && extUser==null )
            {
            	File file = new File(TwitterHelper.tempimagePath_nosdcard + String.format("%1$s.html", uid));
            	if(file != null && file.exists())
            	{
            		String filepath = file.getAbsolutePath();
            		Log.d(TAG, "load from cache="+filepath);            		
            		facebook_info.loadUrl("file://"+filepath);
            	}
            }    		
            else
            {
	        	StringBuilder buf = new StringBuilder();        	
	        	formatContent(buf);
	        	
	        	File file = TwitterHelper.createTempPackageFile(this, String.format("%1$s.html", uid));
	        	if(file != null)
	        	{
	        		String filepath = file.getAbsolutePath();
		        	file.delete();
		        	try {
						file.createNewFile();
						
			        	FileOutputStream fos = new FileOutputStream(file);	        	
			        	fos.write(buf.toString().getBytes("utf-8"));
			        	fos.close();
			        	
			        	facebook_info.loadUrl("file://"+filepath);
	        	    } catch (IOException e) {}
	        	}
            }
	    }
        
        //if for photo, hide others
        else if(showIndex == PHOTO)
        {           
        	wall_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);                
            info_button.setBackgroundResource(R.drawable.facebook_profile_button_blue);
            photo_button.setBackgroundResource(R.drawable.facebook_profile_button_white);
            
            wall_button.setTextColor(Color.WHITE);
            info_button.setTextColor(Color.WHITE);
            photo_button.setTextColor(Color.BLACK);
            
            facebook_more_info_span.setVisibility(View.GONE);
            facebook_photo_list.setVisibility(View.VISIBLE);
            status_list.setVisibility(View.GONE);
            facebook_wall_compose_span.setVisibility(View.GONE);
            
            facebook_photo_list.setOnItemClickListener(itemClick);
            facebook_photo_list.setAdapter(null);
            if(albums != null && albums.size() > 0)
            {
                FacebookAlbumActivity.FacebookAlbumAdapter fs = new FacebookAlbumActivity.FacebookAlbumAdapter(FacebookAccountActivity.this, albums);
                facebook_photo_list.setAdapter(fs);
            }
        }
    }    
	
	private void formatContent(StringBuilder sb)
	{  	
		sb.append("<html>");
		String meta = "<META http-equiv=\"Content-Type\" content=\"text/html\" charset='utf-8'/>"; 
		sb.append("<HEAD>"+meta+"</HEAD><body><table border=0 cellpadding=5>"); 
		//<table BORDER=1 RULES=ALL FRAME=VOID CELLPADDING=5 BORDERCOLOR=#cccccc
		
		if(frompage)
		{
			if(page !=null && page.page_id == uid)
			{

				formatPageContent(sb);
			}
		}
		else
		{
			if(extUser != null && extUser.uid == uid)
			{
				formatUserContent(sb);
			}
		}
	    
        sb.append("</table></body></html>");
	}
	
	private void formatPageContent(StringBuilder sb)
	{
		if(page!=null)
		{
			sb.append("<tr align=\"left\" valign=\"top\" ><td colspan=2 valign=\"top\" ><b>Detailed Info:</b></td></tr>");
			if(isEmpty(page.website)==false)
			{
				sb.append("<tr>");
	        	sb.append("<td valign=\"top\" >Website:</td>");
	        	sb.append(String.format("<td valign=\"top\" style=\"color: #333333;\"><a href='%1$s' target='new'>%1$s</a></td>", formatHtml(page.website)));
	        	sb.append("</tr>");
			}
			if(isEmpty(page.company_overview)==false)
			{
				sb.append("<tr>");
	        	sb.append("<td valign=\"top\" >Company Overview:</td>");
	        	sb.append(String.format("<td valign=\"top\" style=\"color: #333333;\">%1$s</td>", formatHtml(page.company_overview)));
	        	sb.append("</tr>");
			}
			if(isEmpty(page.mission)==false)
			{
				sb.append("<tr>");
	        	sb.append("<td valign=\"top\" >Mission:</td>");
	        	sb.append(String.format("<td valign=\"top\" style=\"color: #333333;\">%1$s</td>", formatHtml(page.mission)));
	        	sb.append("</tr>");
			}
		}
	}

    private void formatUserContent(StringBuilder sb)
    {
    	if(extUser != null)
		{	
    	    //sb.append("<tr><td colSpan=2></td><tr>");
	    	sb.append("<tr align=\"left\" valign=\"top\" ><td colspan=2 ><b>Basic information:</b></td></tr>");
	    	if(isEmpty(extUser.relationship_status) == false)
	    	{
	    	    sb.append("<tr valign=\"top\" >");
                sb.append("<td>RelationShip Status:</td>");
                sb.append(String.format("<td style=\"color: #333333;\">%1$s</td>", formatHtml(extUser.relationship_status)));
                sb.append("</tr>");
	    	}
	        if(isEmpty(extUser.sex) == false)
            {   
	            String userSex = extUser.sex.trim();
                if (userSex.equalsIgnoreCase("female")) {
                    userSex = "Female";
                } else if (userSex.equalsIgnoreCase("male")) {
                    userSex = "Male";
                }
	        	sb.append("<tr valign=\"top\" >");
	        	sb.append("<td>Sex:</td>");
	        	sb.append(String.format("<td style=\"color: #333333;\">%1$s</td>", userSex));
	        	sb.append("</tr>");
            }            
            if(isEmpty(extUser.birthday) == false)
            {  
            	sb.append("<tr valign=\"top\" >");
	        	sb.append("<td>Birthday:</td>");
	        	sb.append(String.format("<td style=\"color: #333333;\">%1$s</td>", extUser.birthday));
	        	sb.append("</tr>");               
            }            
            if(extUser.hometown_location != null && isEmpty(extUser.hometown_location.city) == false)
            {            
            	sb.append("<tr valign=\"top\" >");
	        	sb.append("<td>Hometown:</td>");
	        	sb.append(String.format("<td style=\"color: #333333;\">%1$s %2$s %3$s </td>", formatHtml(extUser.hometown_location.city) ,  formatHtml(extUser.hometown_location.country ),  formatHtml(extUser.hometown_location.zip)));
	        	sb.append("</tr>");
            }
            if(extUser.current_location != null && isEmpty(extUser.current_location.city) == false)
            {
            	sb.append("<tr valign=\"top\" >");
	        	sb.append("<td>Current Location:</td>");
	        	sb.append(String.format("<td style=\"color: #333333;\">%1$s %2$s %3$s </td>", formatHtml(extUser.current_location.city) , formatHtml(extUser.current_location.country) , formatHtml(extUser.current_location.zip)));
	        	sb.append("</tr>");               
            }
            
            if(extUser.meeting_for != null && extUser.meeting_for.size() > 0)
            {
            	String meeting="";
                for(int i=0;i<extUser.meeting_for.size();i++)
                {
                    if(i>0)
                        meeting +=", ";
                    meeting += extUser.meeting_for.get(i);
                }
                sb.append("<tr valign=\"top\" >");
	        	sb.append("<td>Meeting for:</td>");
	        	sb.append(String.format("<td style=\"color: #333333;\">%1$s</td>", formatHtml(meeting)));
	        	sb.append("</tr>");    
                
            }
            
            if(extUser.meeting_sex != null && extUser.meeting_sex.size() > 0)
            {
                String sex="";
                for(int i=0;i<extUser.meeting_sex.size();i++)
                {
                    if(i>0)
                        sex +=", ";
                    sex += extUser.meeting_sex.get(i);
                }   
                sb.append("<tr valign=\"top\" >");
	        	sb.append("<td>Looking for:</td>");
	        	sb.append(String.format("<td style=\"color: #333333;\">%1$s</td>", formatHtml(sex)));
	        	sb.append("</tr>");
            }  
            
	     
            if(isEmpty(extUser.activities) && isEmpty(extUser.tv) && isEmpty(extUser.movies) && isEmpty(extUser.quotes) && isEmpty(extUser.about_me))
            {
                ;
            }
            else
            {            	
            	sb.append("<tr align=\"left\" valign=\"top\" ><td colspan=2 ><b>Personal information:</b></td></tr>");
            	
            	if(isEmpty(extUser.interests) == false)
            	{
            	    sb.append("<tr valign=\"top\" >");
                    sb.append("<td>Interests:</td>");                  
                    sb.append(String.format("<td style=\"color: #3b5998;\">%1$s</td>", formatHtml(extUser.interests)));                    
                    sb.append("</tr>");               
            	}
            	if(isEmpty(extUser.music) == false)
            	{
            	    sb.append("<tr valign=\"top\" >");
                    sb.append("<td>Favorite Music:</td>");                  
                    sb.append(String.format("<td style=\"color: #3b5998;\">%1$s</td>", formatHtml(extUser.music)));                    
                    sb.append("</tr>"); 
            	}
                if(isEmpty(extUser.activities) == false)
                {
                	sb.append("<tr valign=\"top\" >");
     	        	sb.append("<td>Activities:</td>");     	        	
					sb.append(String.format("<td style=\"color: #3b5998;\">%1$s</td>", formatHtml(extUser.activities)));					
     	        	sb.append("</tr>");                    
                }
                if(isEmpty(extUser.tv) == false){	  
                	sb.append("<tr valign=\"top\" >");
     	        	sb.append("<td>Favorite TV Shows:</td>");
     	        	sb.append(String.format("<td style=\"color: #3b5998;\">%1$s</td>",formatHtml(extUser.tv)));
					
     	        	sb.append("</tr>");
                }
                if(isEmpty(extUser.movies) == false)
                {
                	sb.append("<tr valign=\"top\" >");
     	        	sb.append("<td>Favorite Movies:</td>");
     	        	sb.append(String.format("<td style=\"color: #3b5998;\">%1$s</td>", formatHtml(extUser.movies)));
					
     	        	sb.append("</tr>");                    
                }
                if(isEmpty(extUser.quotes) == false)
                {
                	sb.append("<tr valign=\"top\" >");
     	        	sb.append("<td>Quotations:</td>");
     	        	sb.append(String.format("<td style=\"color: #3b5998;\">%1$s</td>", formatHtml(extUser.quotes)));
					
     	        	sb.append("</tr>");
                }
                if(isEmpty(extUser.about_me) == false)
                {	        
                	sb.append("<tr valign=\"top\" >");
     	        	sb.append("<td>About Me:</td>");
     	        	sb.append(String.format("<td style=\"color: #3b5998;\">%1$s</td>", formatHtml(extUser.about_me)));					
     	        	sb.append("</tr>");                    
                }
            }
            
	        if(phonebook!=null)
            {
	            if(isEmpty(phonebook.email)==true && isEmpty(phonebook.cell) ==true && isEmpty(phonebook.phone) == true)
	            {
	                
	            }
	            else
	            {
    	        	//sb.append("<tr align=\"left\" valign=\"top\" ><td colspan=2></td></tr>");
    	        	sb.append("<tr align=\"left\" valign=\"top\" ><td colspan=2><b>Contact Information:</b></td></tr>");
                    if(isEmpty(phonebook.email)==false)
                    {	          
                    	sb.append("<tr valign=\"top\" >");
         	        	sb.append("<td>Email:</td>");
         	        	sb.append(String.format("<td><a href='mailto:%1$s'>%1$s</a></td>", phonebook.email));
         	        	sb.append("</tr>");                    
                    }
                    if(isEmpty(phonebook.cell) ==false)
                    {
                    	sb.append("<tr valign=\"top\" >");
         	        	sb.append("<td>Mobile Number:</td>");
         	        	sb.append(String.format("<td><a href='tel:%1$s'>%1$s</a></td>", phonebook.cell));
         	        	sb.append("</tr>");                    
                    }
                       
                    if(isEmpty(phonebook.phone) == false)
                    {
                    	sb.append("<tr valign=\"top\" >");
         	        	sb.append("<td>Other:</td>");
         	        	sb.append(String.format("<td><a href='tel:%1$s'>%1$s</a></td>", phonebook.phone));
         	        	sb.append("</tr>");                    
                    }
	            }
            }
        
	        if(extUser.education_history != null && extUser.education_history.size() > 0)
            {
	        	//sb.append("<tr align=\"left\" valign=\"top\" ><td colspan=2></td></tr>");
	        	sb.append("<tr align=\"left\" valign=\"top\" ><td colspan=2><b>Education history:</b></td></tr>");
                for(int i=0;i<extUser.education_history.size();i++)
                {
                    FacebookUser.Education_History item = extUser.education_history.get(i);
                    sb.append("<tr valign=\"top\" >");
     	        	sb.append(String.format("<td>%1$s:</td>", item.school_type));
     	        	sb.append(String.format("<td style=\"color: #333333;\">%1$s,%2$s, %3$s</td>", formatHtml(item.name),  formatHtml(item.year) ,formatHtml(item.degree)));
     	        	sb.append("</tr>");   
     	        	
                    String dgree="";
                    if(item.concentrations != null && item.concentrations.size() > 0)
                    {
                        for(int j=0;j<item.concentrations.size();j++)
                        {
                            if(j > 0)
                                dgree+=", ";
                            dgree +=item.concentrations.get(j);
                        }
                        
                        Log.d(TAG, "concentrations="+ dgree);
                    }
                    else
                    {
                    	Log.d(TAG, "I have no education concentrations="+item.toString());
                    }
                    sb.append("<tr valign=\"top\" >");
     	        	sb.append("<td>Concentrations:</td>");
     	        	sb.append(String.format("<td style=\"color: #333333;\">%1$s</td>", dgree));
     	        	sb.append("</tr>");
     	        	
     	        	//sb.append("<tr align=\"left\"><td colspan=2></td></tr>");     	        	
                }
            }
              
            if(extUser.work_history != null && extUser.work_history.size() > 0)
            {
            	//sb.append("<tr align=\"left\" valign=\"top\" ><td colspan=2></td></tr>");
            	sb.append("<tr align=\"left\" valign=\"top\" ><td colspan=2><b>Work history:</b></td></tr>");
            	
                for(int i=0;i<extUser.work_history.size();i++)
                {
                    FacebookUser.Work_History item = extUser.work_history.get(i);  
                    if(item == null) continue;
                    if(isEmpty(item.company_name)== false)
                    {
                        sb.append("<tr valign=\"top\" >");
                        sb.append("<td>Employer:</td>");
                        sb.append(String.format("<td style=\"color: #333333;\"><b>%1$s</b></td>", formatHtml(item.company_name)));
                        sb.append("</tr>");
                    }
                    
     	        	if(isEmpty(item.position) == false)
     	        	{
     	        	    sb.append("<tr valign=\"top\" >");
                        sb.append("<td>Position:</td>");
                        sb.append(String.format("<td style=\"color: #333333;\">%1$s</td>", formatHtml(item.position)));
                        sb.append("</tr>");
     	        	}
     	        	
       	        	if(isEmpty(item.start_date) == false)
       	        	{
           	        	 String end = (item.end_date==null?"":item.end_date);  
                         sb.append("<tr valign=\"top\" >");
                         sb.append("<td>Time Period:</td>");
                         sb.append(String.format("<td style=\"color: #333333;\">%1$s</td>", item.start_date+"--"+end));
                         sb.append("</tr>");
       	        	}
     	        	
       	        	if(isEmpty(item.city) == false || isEmpty(item.country) == false)
       	        	{
           	        	 sb.append("<tr valign=\"top\" >");
                         sb.append("<td>Location:</td>");
                         sb.append(String.format("<td style=\"color: #333333;\">%1$s</td>", formatHtml(item.city+"  "+item.country)));
                         sb.append("</tr>");
       	        	}
  	        	    
                    if(isEmpty(item.description) == false)
                    {
                         sb.append("<tr valign=\"top\" >");
                         sb.append("<td>Description:</td>");
                         sb.append(String.format("<td style=\"color: #333333;\">%1$s</td>", formatHtml(item.description)));
                         sb.append("</tr>");  
                    }
     	        	
     	        	//sb.append("<tr align=\"left\" valign=\"top\" ><td colspan=2></td></tr>");
                }
            }
		}
    }
	
	private String formatHtml(String source)
	{
		if(source == null)
			return "";
		
		String tmp;
		try {
			tmp = new String(source.getBytes(), "UTF-8");
			tmp = tmp.replaceAll("\r\n", "<br>");
			tmp = tmp.replaceAll("\r", "<br>");
			tmp = tmp.replaceAll("\n", "<br>");
			return tmp;
		} catch (Exception e) {}					
		return source;
	}
	
	protected void doNotificationSend()
	{
		 Intent intent = new Intent(mContext, FacebookCommentsActivity.class);			 
		 intent.putExtra("uid", uid);
		 intent.putExtra("fornotificationsend", true);
		 (mContext).startActivity(intent);
	}
	
	public boolean isPage()
	{
		return frompage;
	}
	
	public boolean isFriend() 
	{
	    if(comefrommyself == false)
	    {
	    	if(orm.isFriends(uid) == true)
	    	{
	    		isFriend = true;
	    		return true;
	    	}
	    	
            return isFriend;
	    }
	    else
	    {
	    	return true;
	    }
    }   
	
	@Override
	protected  boolean isOnline()
	{
		if(extUser != null)
		{
			return extUser.online_presence.equalsIgnoreCase("active");
		}
		
		return false;
	}
	public boolean isAppUser()
	{
		if(extUser != null)
		{
			return extUser.is_app_user;
		}
		
		return false;
	}
	
	public long getUID()
	{
	    return uid;	    
	}
	private void constructUI(Intent intent)
	{   
	    Log.d(TAG,"entering facebook accountActivity intent scheme is "+intent.getScheme()+" data is "+intent.getDataString());
	   
		frompage = intent.getBooleanExtra("frompage", false);
		comefrommyself = intent.getBooleanExtra("comefrommyself", false);
        //for user details
        if(comefrommyself == false)
        {
	        //get FacebookUser
            if("fb".equals(intent.getScheme()))
            {
            	try{
                    uid = ContentUris.parseId(intent.getData());
            	}catch(Exception ne){}
            }
            else
            {
                uid = this.getIntent().getLongExtra("uid", -1);
            }
            
	        username = this.getIntent().getStringExtra("username");
	        imgURL   = this.getIntent().getStringExtra("imageurl");
	        
	        if(frompage == false)
	        {
	        	 FacebookUser tmp = orm.getFacebookUser(uid);
	        	 //user show this user, so add it as shot cut
	        	 orm.updateFacebookUserShortCut(tmp, true);
	 	         if(tmp != null)
	 	         {
	 	            String tmpurl = getImgURL(tmp);
	 	            if(tmpurl != null)
	 	            {
	 	                imgURL = tmpurl;
	 	            }
	 	         }
	        }
	        else
	        {
	        	// no need for page
	        }
	       
	        albums   = orm.getAlbumPhotosByOwner(uid);
            if(albums != null)
            {
                Log.d(TAG, "how many album="+albums.size() + " =uid="+uid);
            }  
            setUI(username,imgURL);
        }
       
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
        	    //for pre load
        	    if(comefrommyself == true)
        	    {
        	    	uid = perm_session.getLogerInUserID();
            	    albums   = orm.getAlbumPhotosByOwner(perm_session.getLogerInUserID());
                    if(albums != null)
                    {
                        Log.d(TAG, "how many album="+albums.size() + " =uid="+uid);
                    }
        	    }
                
	        	perm_session.attachActivity(this);
	        	//for check myself
	        	if(uid == perm_session.getLogerInUserID())
	        	{
	        		comefrommyself = true;
	        	}
	        	
	        	facebookA = new AsyncFacebook(perm_session);
	        	
	        	newsfeed_sfile = TwitterHelper.profile + uid;
	        	new DeSerializationTask().execute((Void[])null);
	        	loadAction();
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }   
        //set for resume
        setTitle();
        
        setTitle(title);
    }
	
	public void setTitle() 
	{
	    if(frompage == true)
	    {
	        title = getString(R.string.menu_title_page);
	    }
	    else
	    {
	        title = getString(R.string.menu_title_my_account);
	    }
	   
	}
	
	@Override
	protected void onNewIntent(Intent intent) 
	{		
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent"+intent);
		Log.d(TAG,"entering facebook accountActivity intent scheme is "+intent.getScheme()+"data is "+intent.getDataString());
        //When current UI is loading data from web, enter another users profile.
		// we set inprocess = false so that next UI can excute loadAction normally
		synchronized(mLock)
        {
            inprocess = false;
        }
		
		setIntent(intent);
		
		//use for fb://profile/facebookid
		long tmpid = -1;
		if("fb".equals(intent.getScheme()))
		{
		    tmpid =ContentUris.parseId(intent.getData());
		}
		else
		{
		    tmpid = this.getIntent().getLongExtra("uid", -1);
		}
		if(uid == tmpid)
		{
		    Log.d(TAG, "i am the same, no need do again");
		    return;
		}
//		else
//		{
//			finish();
//			startActivity(intent);
//			return;
		    force_deserialize = true;
		    intent.putExtra("comefrommyself", false);
//		}
		//end for fb schema
		Log.d(TAG," old uid is="+uid);
		uid = tmpid;
		Log.d(TAG," new uid is="+uid);
		if(extUser != null)
		{
			extUser.despose();
			extUser = null;
		}
		
		if(page != null)
		{
			page.despose();
			page = null;
		}
		
		//clear UI
		frompage = intent.getBooleanExtra("frompage", false);
		int res = R.drawable.no_avatar;
		if(frompage)
		{
		    res = R.drawable.pages;
		}		
		setProfileImage(res);
        

		//initialized button set visibility as gone
		add_as_friend_button.setVisibility(View.GONE);
		facebook_username.setText("");
		facebook_user_status.setText("");
		facebook_publish_time.setText("");        
		facebook_info.loadData("<html><body></body></html>", "text/html", "utf-8");
		facebook_photo_list.setAdapter(null);
		
		//
		//clear pre-user wall information		
		//clear streams
		status_list.setAdapter(null);
        synchronized(streams)
        { 
	        if(streams != null)
	        {
	        	for(Stream item: streams)
	        	{
	        		item.dispose();
	        	}
	        	streams.clear();	        	   	
	        }
        }		
		
		//come from another new
		initInfo(); 
		constructUI(intent);
		initStatus();
	}
	
	private void setProfileImage(int res)
	{
		try{
			Matrix matrix = new Matrix();
	        int h = width;
	        Bitmap mBaseImage = BitmapFactory.decodeResource(this.getResources(), res);
	        if(mBaseImage != null)
	        {
		        float scale = (float)h/(float)mBaseImage.getWidth();
		        matrix.setScale(scale, scale);
		        mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage.getWidth(), mBaseImage.getHeight(), matrix, true);           
		        imageView.setImageBitmap(mBaseImage);
	        }
		}catch(Exception ne){}
	}

	@Override protected void onResume() 
    {
        super.onResume();        
        //it is used to de-serialization when show another user in wall
       // constructUI(getIntent());
    }
	
	private String getImgURL(FacebookUser user)
    {
	    if(user == null)
	        return null;
	    
	    String url = null;
	    int icon_size = orm.getFacebookIconSizeSetting(); //0 big 1 middle 2 small
        switch(icon_size)
        {
            case 0:
            {
                url = user.pic;
                if(url == null)
                {
                    url = user.pic_square;
                }
                break;
            }
            case 1:
            { 
                url = user.pic_small;
                if(url == null)
                {
                    url = user.pic_square;
                }
                break;
            }
            case 2:
            {
                url = user.pic_square;
                break;
            }
        }
        //if the file is not exist, we just use the pic_square
        String filepath = TwitterHelper.getImagePathFromURL_noFetch(url);
        if(new File(filepath).exists() == false)
        {
        	url = user.pic_square;
        }
        return url;
    }
	
	private void setImgURL(FacebookUser user)
	{
	    String url = getImgURL(user);
	    if(url != null)
	    {
	        imgURL = url;
	    }
	    if(user!=null && user.name!=null)
	    {
	       username = user.name;
	    }
	}
	
	private void getImageBMP(String imageurl)
	{	
		if(imageurl != null && !imageurl.equals(""))
        {
			ImageRun imagerun = new ImageRun(handler, imageurl, 1);
			if(frompage)
			{
			    imagerun.use_page = true;
			}
			else
			{
				imagerun.use_avatar = true;
			}
			imagerun.setImageView(imageView);
			imagerun.width = width;
			imagerun.need_scale = true;			
			imagerun.post(imagerun);
        }
        else//get from web
        {	
        	
        	// comment by jessie 2010-03-27 because all all data will be load through batch_run method
        	/*if(frompage == false)
        	{
        		getUserInfoFromWeb();
        	}
        	else
        	{
        		getPageInfoFromWeb();
        	}*/
        }
			
	}
	
	private void getPageInfoFromWeb()
	{
		facebookA.getPageInfoAsync(uid, new FacebookAdapter()
    	{
    		@Override public void getPageInfo(Page page)
            {
    			if(page != null )
    			{	    				
    				Log.d(TAG, "after get page info="+page);
    				username = page.name;
    				imgURL   = page.pic_square;
    				handler.post(new Runnable()
    				{
    				    public void run()
    				    {
    				    	ImageRun imagerun = new ImageRun(handler, imgURL, 1);
    				    	if(frompage)
    						{
    						    imagerun.use_page = true;
    						}
    						else
    						{
    							imagerun.use_avatar = true;
    						}
    				    	imagerun.width = width;
    				    	imagerun.need_scale = true;
                            imagerun.setImageView(imageView);       
                            imagerun.post(imagerun);
                            
                            if(username!=null)
                            {
                                facebook_username.setText(username);
                            }
    				    }
    				});
    				
					//update database
					orm.insertPage(page);
    			}
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	Log.d(TAG, "fail to get the basic page info");
            	getImageBMP(null);         	
            }
    	});
	}
	
	private void getUserInfoFromWeb()
	{
		if(facebookA != null)
		{
			long[] uids = new long[1];
			uids[0] = uid;
			facebookA.getBasicUsersAsync(uids, new FacebookAdapter()
	    	{
	    		@Override public void getUsers(List<FacebookUser> users)
	            {
	    			if(users != null && users.size()>0)
	    			{

	    		        FacebookUser user = users.get(0);
	    				updateProfileUI(user);
						//update database
						orm.addFacebookUser(user);
						orm.updateFacebookUserShortCut(user, true);
	    			}
	            }
	    		
	            @Override public void onException(FacebookException e, int method) 
	            {
	            	Log.d(TAG, "fail to get the basic user info");
	            	getImageBMP(null);         	
	            }
	    	});	
        }
	}
	
	private void updatePageUI(Page page)
	{
		Log.d(TAG, "after get page info="+page);
		username = page.name;
		imgURL   = page.pic_square;
		handler.post(new Runnable()
		{
		    public void run()
		    {
		    	ImageRun imagerun = new ImageRun(handler, imgURL, 1);
		    	if(frompage)
				{
				    imagerun.use_page = true;
				}
				else
				{
					imagerun.use_avatar = true;
				}
		    	imagerun.need_scale = true;
                imagerun.setImageView(imageView);       
                imagerun.post(imagerun);
                facebook_user_status.setText("");
                facebook_publish_time.setVisibility(View.GONE);
                if(username!=null)
                {
                    facebook_username.setText(username);
                }
		    }
		});
	}
	
	private void updateProfileUI(FacebookUser user)
	{
        Log.d(TAG, "after get user info="+user);
        final String message = user.message;
        final long messagetime = user.statustime;
        setImgURL(user);    
        handler.post(new Runnable()
        {
            public void run()
            {
                ImageRun imagerun = new ImageRun(handler, imgURL, 1);
                if(frompage)
                {
                    imagerun.use_page = true;
                }
                else
                {
                    imagerun.use_avatar = true;
                }
                imagerun.width = width;
                imagerun.need_scale = true;
                imagerun.setImageView(imageView);       
                imagerun.post(imagerun);
                
                if(username!=null)
                {
                    facebook_username.setText(username);
                }
                
                if(isEmpty(message) == false)
                {
                    facebook_user_status.setText(message);
                    if(messagetime > 0)
                    {
                        facebook_publish_time.setVisibility(View.VISIBLE);
                        facebook_publish_time.setText(DateUtil.converToRelativeTime(mContext, messagetime));
                    }
                    else
                    {
                        facebook_publish_time.setVisibility(View.GONE);
                    }
                }
            }
        });
        
	}
	
	private void setUI(String name, String imageurl)
	{
		//just get username,imageurl info from cache. never get data from web server. 
		// set username, user image, user status,status publish time
		if(name != null)
        {
            facebook_username.setText(name);
            
            //set status
            FacebookUser fuser = orm.getFacebookUser(uid);
		    if(fuser!=null && fuser.name!=null)
		    {
		       if(isEmpty(fuser.message) == false)
		       {
		    	   facebook_user_status.setVisibility(View.VISIBLE);
		           facebook_user_status.setText(fuser.message);
                   if(fuser.statustime > 0)
                   {
                       facebook_publish_time.setVisibility(View.VISIBLE);
                       facebook_publish_time.setText(DateUtil.converToRelativeTime(mContext, fuser.statustime));
                   }
                   else
                   {
                       facebook_publish_time.setVisibility(View.GONE); 
                   }
		       }
		    }
        }
		else
		{
			if(frompage == false)
			{
				FacebookUser fuser = orm.getFacebookUser(uid);
			    if(fuser!=null && fuser.name!=null)
			    {
			       facebook_username.setText(fuser.name);
			       if(isEmpty(fuser.message) == false)
			       {
			    	   facebook_user_status.setVisibility(View.VISIBLE);
			           facebook_user_status.setText(fuser.message);
			           
			           if(fuser.statustime >0)
			           {
			               facebook_publish_time.setVisibility(View.VISIBLE);
			               facebook_publish_time.setText(DateUtil.converToRelativeTime(mContext, fuser.statustime));
			           }
			           else
			           {
			               facebook_publish_time.setVisibility(View.GONE);
			           }
			           
			       }
			    }
			}
			else
			{
				Page page = orm.getPageBypid(uid);
				if(page !=null && page.name != null)
				{
					facebook_username.setText(page.name);
				}
			}
		    
		}
		getImageBMP(imageurl);
        
	}
	
	/*
	@Override
	public void titleSelected() 
	{
		super.titleSelected();
		loadAction();
	}
	*/
	
	private void PokeHim() 
	{	
		handler.obtainMessage(FACEBOOK_POKE).sendToTarget();
	}

	@Override
	protected void loadRefresh()
    {
	    if(this.isInProcess() == true)
        {
            showToast();
        }
	    loadAction();
    }
	
	private void loadAction() 
	{	    
		//get myselft information
		if(comefrommyself)
		{
			uid = perm_session.getLogerInUserID();
			FacebookUser user = orm.getFacebookUser(uid);
			if(user != null)
			{
				username=user.name;
				setImgURL(user);	
				setUI(username, imgURL);
			}
			else//get information
			{			
				//handler.obtainMessage(FACEBOOK_GET_USER_INFO).sendToTarget();
			}		
		}
		Message msd = handler.obtainMessage(FACEBOOK_GET_USER_ACCOUNT_INFO);
		msd.getData().putBoolean("ismyself",comefrommyself);
		msd.sendToTarget();
	}
	
	@Override
	protected void onAfterDeleteAlbum(String aid) 
	{
	    //remove photoAlbum from databases;
	    orm.deleteAlbum(aid);
	    orm.deleteAllPhotoInAlbum(aid);
	    for(PhotoAlbum album : albums)
	    {
	        if(album.aid.equals(aid))
	        {
	            albums.remove(album);
	        }
	    }
	    FacebookAlbumActivity.FacebookAlbumAdapter fs = new FacebookAlbumActivity.FacebookAlbumAdapter(FacebookAccountActivity.this, albums);
        facebook_photo_list.setAdapter(fs);
	}
	
    public void loadUserAccountInfo(boolean ismyself) 
    {

    	if(isSafeCallFacebook(true) == false)
    	{
    		return ;
    	}
       
    	synchronized(mLock)
        {
            inprocess = true;
        }
    	
        begin();
        final boolean needCallIsFriends = (ismyself==true?false:true);
        facebookA.loadUserAccountInfoBatchAsync(uid,needCallIsFriends,frompage, new FacebookAdapter()
        {   
            @Override public void loadUserAccountInfoBatch(HashMap<Integer, Object> userAccountInfo)
            {
            	synchronized(mLock)
                {
                     inprocess = false;
                }
            	long tmpid = -1;
            	boolean ispage = false;
            	int i=0;
            	List<Page> pagelist = ((ArrayList<Page>)userAccountInfo.get(i));
                if(pagelist!=null && pagelist.size()>0)
                {
                    page = pagelist.get(0);
                    ispage = true;
                    tmpid = page.page_id;
                    orm.insertPage(page);
                }
                
            	if(frompage == false)
                {
            	    i++;
            	    List<FacebookUser> users = ((ArrayList<FacebookUser>)userAccountInfo.get(i));
                    extUser  = (users!=null && users.size()>0?users.get(0):null);                     
                    if(extUser!=null)
                    {
                        tmpid = extUser.uid;
                        orm.addFacebookUser(extUser);
                        orm.updateFacebookUserShortCut(extUser,true);
                    }
                }
               
            	i++;
            	//no need for back
            	if(donotcallnetwork == false)
            	{
            	    if(tmpid == uid)
            	    {
            		    getLastViewCount((ArrayList<Stream>)userAccountInfo.get(i));
            	    }
            	}

            	i++;
                //process album data
                if(albums != null)
                {
                	synchronized(albums)
                	{
                        facebook_photo_list.setOnItemClickListener(null);
                        albums.clear();
                        albums.addAll((ArrayList<PhotoAlbum>)userAccountInfo.get(i));
                	}
                }
                else
                {
                	//save the album, whatever in front or back
                	List<PhotoAlbum> tmp = (ArrayList<PhotoAlbum>)userAccountInfo.get(i);
                	orm.addAlbum(tmp);                    	
                }
                
                i++;
                if(albums != null)
                {
                    List<Photo> photos = (ArrayList<Photo>)userAccountInfo.get(i);                    
                    for(PhotoAlbum photoalbum : albums)
                    {
                        for(Photo photo : photos)
                        {
                            if(photoalbum.aid.equals(photo.aid))
                            {
                                photoalbum.cover_src_url = photo.src_small;
                                photos.remove(photo); //reduce the compare times
                                photo.dispose();
                                photo = null;
                                break;
                            }
                        }
                    }
                    
                    //a little waste time
                    orm.addAlbum(albums);
                }
                
                if(donotcallnetwork == false)
            	{
                    //end                 
                    if(frompage==false)
                    {
                        i++;
                    	phonebook = userAccountInfo.get(i)!=null?(PhoneBook)userAccountInfo.get(i):null;
                    	if(phonebook != null)
                    	{
                            orm.addPhonebook(phonebook);
                    	}
                        if(needCallIsFriends == true)
                        {
                            i++;
                            isFriend = ((Boolean)userAccountInfo.get(i)).booleanValue();
                            if(isFriend == true)
                            {
                                //add relationship to DB
                                if(extUser !=null && extUser.uid>0)
                                {
                                    long[] friendid = {extUser.uid};
                                    orm.addFriends(perm_session.getLogerInUserID(),friendid);
                                }
                            }
                        }
                    } 
                    // if uid is different the loaduid, don't update UI 
                    if(tmpid == uid)
                    {
                        frompage = ispage;
                        handler.obtainMessage(FACEBOOK_SHOW_ADD_FRIEND_BUTTON).sendToTarget();
                        handler.obtainMessage(FACEBOOK_GET_STATUS_UPDATES_UI).sendToTarget();                   
                    
                        //for end UI
                        handler.obtainMessage(FACEBOOK_GET_STATUS_UPDATES_END).sendToTarget();
                    }
                   
            	}
                // if uid is different the tmpid, don't update UI
                if(tmpid == uid)
                {
                    frompage = ispage;
                    Message mds = handler.obtainMessage(FACEBOOK_GET_USER_INFO_END);
                    mds.getData().putBoolean(RESULT, true);
                    handler.sendMessage(mds);                
                }
                                   
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                synchronized(mLock)
                {
                     inprocess = false;
                }
                Log.d(TAG, "fail to load useraccount info batch="+e.getMessage());
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                   handler.obtainMessage(FACEBOOK_GET_USER_ACCOUNT_INFO_END).sendToTarget();                   
                }
            }
            });        
    }
	
	@Override
    protected void doAfterLogin()
    {
    	Log.d(TAG, "after login");
    	//try to get the session
    	perm_session = loginHelper.getPermanentSesstion(this);
    	if(perm_session == null)
    	{
    		//re-launch the login UI
    		if(optionMenu != null)
    		{
	    		optionMenu.setGroupVisible(R.id.facebook_groupd_nemu, false);
	    		optionMenu.setGroupVisible(R.id.facebook_groupd_login_nemu, true);
    		}
    		//reLaunchFacebookLogin();
    		
    		return ;
    	}
    	else
    	{
    		if(optionMenu != null)
    		{
	    		optionMenu.setGroupVisible(R.id.facebook_groupd_nemu, true);
	    		optionMenu.setGroupVisible(R.id.facebook_groupd_login_nemu, false);
    		}
    		
    		facebookA = new AsyncFacebook(perm_session);
    		perm_session.attachActivity(this);
    	}	
    	//get status
    	loadAction();
    }
	
	private void postWallMessage()
    {
		if(isSafeCallFacebook(false) == false)
    	{
    		return ;
    	}
		
        String content= facebook_wall_editor.getText().toString().trim();
        if(content != null && content.length() > 0)
        {
        	facebook_wall_post.setEnabled(false);           
            
            showDialog(DLG_POST_WALL);
            
            //if is myself invoke update status method , if is others invoke post wall method
            
            if(uid == perm_session.getLogerInUserID())
            {
                updateStatus(content);
            }
            else
            {
                postWall(uid,content);
            }
            
        }
    }    

	private void postWall(long uid2, String content) {
	    facebookA.postWallAsync(uid, content, new FacebookAdapter()
        {
            @Override public void postWall(boolean suc)
            {
                Log.d(TAG, "post to wall="+suc);                   
                
                if(donotcallnetwork == false)//I am still alive
                {                           
                    //cancelNotify();
                }       
                Message rmsg = handler.obtainMessage(FACEBOOK_WALL_POST_END);
                rmsg.getData().putBoolean(RESULT, suc);
                rmsg.sendToTarget();
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                Log.d(TAG, "post to wall ex="+e.getMessage());
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                    Message rmsg = handler.obtainMessage(FACEBOOK_WALL_POST_END);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.sendToTarget();
                }
            }
        });
    }

    private void updateStatus(String content) {
        facebookA.updateStatusAsync(content,new FacebookAdapter()
        {
            @Override public void updateStatus(boolean suc)
            {
                Log.d(TAG, "post to wall="+suc);
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                if(donotcallnetwork == false)//I am still alive
                {                           
                    //cancelNotify();
                }       
                
                Message rmsg = handler.obtainMessage(FACEBOOK_WALL_POST_END);
                rmsg.getData().putBoolean(RESULT, suc);
                rmsg.sendToTarget();
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                Log.d(TAG, "post to wall ex="+e.getMessage());
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                    Message rmsg = handler.obtainMessage(FACEBOOK_WALL_POST_END);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.sendToTarget();
                }
            }
        });
        
    }

    @Override
	protected void createHandler() 
	{		
		handler = new AccoutHandler();
	}
	
	private final int  FACEBOOK_GET_STATUS_UPDATES_UI    =1;
	private final int  FACEBOOK_GET_STATUS_UPDATES_END   =2;
	private final int  FACEBOOK_POKE                     =3;
	private final int  FACEBOOK_POKE_END                 =4;
	private final int  FACEBOOK_GET_USER_INFO            =5;
	private final int  FACEBOOK_GET_USER_INFO_END        =6;
	private final int  FACEBOOK_GET_USER_ACCOUNT_INFO    =7;
	private final int  FACEBOOK_GET_USER_ACCOUNT_INFO_END=9;
	private final int  FACEBOOK_SHOW_ADD_FRIEND_BUTTON   =10;
	private final int  FACEBOOK_ADD_AS_FRIEND_END        =11;
	private final int  FACEBOOK_GET_WALL_END             =12;
	
    private class AccoutHandler extends Handler 
    {
        public AccoutHandler()
        {
            super();            
            Log.d(TAG, "new AccoutHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case FACEBOOK_WALL_POST:
                {
                    postWallMessage();
                    break;
                }
                case PINFO_UINFO_GET:
                {
                    long[] uids = msg.getData().getLongArray("ids");
                    getPageInfoAndUserInfo(uids);
                    break;
                }
                case FACEBOOK_WALL_POST_END:
                {   
                	dismissDialog(DLG_POST_WALL);
                	facebook_wall_post.setEnabled(true);
                    boolean result = msg.getData().getBoolean(RESULT);
                    if(result)
                    {
                        loadAction();
                        facebook_wall_editor.setText("");
                        Toast.makeText(FacebookAccountActivity.this, R.string.sns_operate_succeed, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                    	Toast.makeText(FacebookAccountActivity.this, R.string.sns_operate_failed, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
	            case FACEBOOK_GET_USER_INFO:
	            {
	            	getUserInfo();
	                break;	
	            }
	            case FACEBOOK_GET_USER_ACCOUNT_INFO:
	            {
	                boolean ismyself = msg.getData().getBoolean("ismyself");
	                loadUserAccountInfo(ismyself);
	                break;
	            }
	            case FACEBOOK_GET_USER_INFO_END:
	            {
	                end();
	            	if(msg.getData().getBoolean(RESULT) == true)
	            	{
	            	    setUI(username, imgURL);	            	   
	            	}
	            	break;
	            }	           
	            case FACEBOOK_GET_STATUS_UPDATES_UI:
	            {
	            	if(donotcallnetwork == false)
	            	{
	            		if(frompage == true)
	            		{
	            			if(page !=null)
	            			{
	            				updatePageUI(page);
	            			}
	            		}
	            		else
	            		{
	            			if(extUser!=null)
			                {
			                    updateProfileUI(extUser); 
			                }
	            		}  
		            	updateSubTabUI();
	            	}
	            	break;
	            }
	            case FACEBOOK_GET_STATUS_UPDATES_END:
	            {
	            	end();
	            	
	            	 //process for UI
	                for(int i=status_list.getChildCount()-1;i>0;i--)            
	                {
	                    View v = status_list.getChildAt(i);
	                    if(Button.class.isInstance(v))
	                    {
	                        Button bt = (Button)v;
	                        bt.setText(getString(R.string.load_older_msg));
	                        break;
	                    }
	                }
	                
	            	break;
	            }
	            case FACEBOOK_GET_USER_ACCOUNT_INFO_END:
	            {
	                end(); 		
	                break;
	            }
	            case FACEBOOK_GET_WALL_END:
	            {
	            	end();
	                //process for UI
	                for(int i=status_list.getChildCount()-1;i>0;i--)            
	                {
	                    View v = status_list.getChildAt(i);
	                    if(Button.class.isInstance(v))
	                    {
	                        Button bt = (Button)v;
	                        bt.setText(getString(R.string.load_older_msg));
	                        break;
	                    }
	                }
	            	break;
	            }
	            case FACEBOOK_POKE:
	            {	            	
	            	Message pokeMessage = basichandler.obtainMessage(POKE_SOMEONE);
	            	pokeMessage.getData().putLong("pokeuid", uid);
	            	basichandler.sendMessage(pokeMessage);
	            	break;
	            }
	            case FACEBOOK_POKE_END:
	            {
	            	end();	            	
	            	if(msg.getData().getBoolean(RESULT, false) == true)
	            	{
	                    Toast.makeText(FacebookAccountActivity.this,getString(R.string.facebook_poke_success), Toast.LENGTH_SHORT).show();
	            	}
	            	else
	            	{
	            		String message = msg.getData().getString("message");
		                Toast.makeText(FacebookAccountActivity.this,getString(R.string.facebook_poke_failed) + ": "+message, Toast.LENGTH_SHORT).show();
		            }
	            	
	            	Message pokeMessage = basichandler.obtainMessage(POKE_SOMEONE_END);	            	
	            	basichandler.sendMessage(pokeMessage);
	            	
	            	break;
	            }
	            case FACEBOOK_ADD_AS_FRIEND_END:
	            {
	                end();
	                break;
	            }
	            case FACEBOOK_SHOW_ADD_FRIEND_BUTTON:
	            {
	                if(comefrommyself || frompage == true || isFriend)//move poke to option menu
                    {
                        add_as_friend_button.setVisibility(View.GONE);
                    }
	                else if(frompage == false)
	                {
		                if(isFriend || isFriend())
		                {
		                    add_as_friend_button.setVisibility(View.VISIBLE);
		                    add_as_friend_button.setText(getString(R.string.facebook_account_tile_poke));
		                    
		                    Resources res = getResources();
	                        Drawable mCacheSym = res.getDrawable(R.drawable.poke);            
	                        mCacheSym.setBounds(new Rect(0, 0, 40, 40));
	                        add_as_friend_button.setCompoundDrawables(mCacheSym, null, null, null);
		                }
		                else 
		                {
		                    add_as_friend_button.setVisibility(View.VISIBLE);
		                    add_as_friend_button.setText(getText(R.string.menu_facebook_title_add_as_friend));
		                    
		                    Resources res = getResources();
	                        Drawable mCacheSym = res.getDrawable(R.drawable.add_as_friends);            
	                        mCacheSym.setBounds(new Rect(0, 0, 40, 40));
	                        add_as_friend_button.setCompoundDrawables(mCacheSym, null, null, null);
		                }
	                }	                
	                else 
	                {
	                    add_as_friend_button.setVisibility(View.VISIBLE);
	                    add_as_friend_button.setText(getText(R.string.menu_facebook_title_add_as_friend));
	                    
	                    Resources res = getResources();
                        Drawable mCacheSym = res.getDrawable(R.drawable.add_as_friends);            
                        mCacheSym.setBounds(new Rect(0, 0, 40, 40));
                        add_as_friend_button.setCompoundDrawables(mCacheSym, null, null, null);
	                }
	            }
            }
        }
    }
    
    @Override
    public void addAsFriends()
    {
    	 if(isSafeCallFacebook(false) == false)
    	 {
    		 return ;
    	 }
    	
         if(isAddAsFriend == true)
         {
            Log.d(TAG,"is doing add as friend, so exit");
            return;
         }
         synchronized(mLock)
         {
            isAddAsFriend = true;
         }         
         begin();
    
         facebookA.addAsFriendAsync(uid, new FacebookAdapter()
         {
            @Override public void addAsFriend(long uid,boolean returnret)
            {
                Log.d(TAG, "add success ");
                synchronized(mLock)
                {
                    isAddAsFriend = false;
                }
                
                handler.obtainMessage(FACEBOOK_ADD_AS_FRIEND_END).sendToTarget();
                handler.post(new Runnable(){
                   public void run()
                   {
                       Toast.makeText(mContext, R.string.facebook_add_as_friend_successfully, Toast.LENGTH_LONG).show();
                       isFriend = true;
                   }
                }); 
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                Log.d(TAG, "fail to add friend");
                synchronized(mLock)
                {
                    isAddAsFriend = false;
                }
                
                handler.obtainMessage(FACEBOOK_ADD_AS_FRIEND_END).sendToTarget();
                handler.post(new Runnable(){
                   public void run()
                   {
                       Toast.makeText(mContext, R.string.facebook_add_as_friend_failed, Toast.LENGTH_LONG).show();
                   }
                });
                
            }
        });
    }
    
    public void getPageInfoAndUserInfo(long[] uids) {
        Log.d(TAG," entering batch run get user info and page info method");
        if(facebookA != null)
        {
	        facebookA.batch_run_getPageInfoAndUserInfo(uids, new FacebookAdapter()
	        {
	            @Override public void batch_run_getPageInfoAndUserInfo(HashMap<Integer, Object>  pageInfoAndUserInfo)
	            {
	                if(pageInfoAndUserInfo != null)
	                {
	                    List<FacebookUser.SimpleFBUser> fusers = (List<FacebookUser.SimpleFBUser>)pageInfoAndUserInfo.get(0);
	                    List<Page> pages = (List<Page>)pageInfoAndUserInfo.get(1);
	                    Log.d(TAG," pageinfo size is="+pages.size()+" SimpleFBuser size is="+fusers.size());
	                    orm.addFacebookSimpleUser(fusers);
	                    orm.insertPage(pages);
	                }
	                
	                //just reset UI
	                handler.post(new Runnable()
	                {
	                    public void run()
	                    {
	                        Log.d(TAG,"entering re set UI after get all userinfo and pageinfo");
	                        if(streams != null && streams.size() > 0)
	                        {
	                            synchronized(streams)
	                            {
	                                int currentLocation = status_list.getFirstVisiblePosition();
	                                FacebookStreamAdapter af = new FacebookStreamAdapter(FacebookAccountActivity.this, streams,true);
	                                status_list.setAdapter(af);
	                                status_list.setSelection(currentLocation);
	                            }
	                        }
	                    }
	                });                 
	            }
	            
	            @Override public void onException(FacebookException e, int method) 
	            {
	                Log.d(TAG, "batch run get userinfo and page info fail="+e.getMessage());  
	            }
	        }); 
        }
    }

    @Override
    public void finishPoke() 
	{	
    	Log.d(TAG, "finished poke");
	}

    public void getUserInfo() 
    {
    	if(isSafeCallFacebook(false) == false)
    	{
    		return ;
    	}
    	
    	facebookA.getCompleteUserInfoAsync(uid, new FacebookAdapter()
    	{
    		@Override public void getCompleteUserInfo(FacebookUser user)
            {
    			if(user != null)
    			{
    				extUser = user.clone();
    				Log.d(TAG, "after get complete user info="+extUser);
    				username = extUser.name;
    				setImgURL(extUser);
					orm.addFacebookUser(extUser);
					
					Message mds = handler.obtainMessage(FACEBOOK_GET_USER_INFO_END);
					mds.getData().putBoolean(RESULT, true);
					handler.sendMessage(mds);
					
    			}
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	Log.d(TAG, "fail to get the basic info="+e.getMessage());
            	
            	if(isInAynscTaskAndStoped())
            	{
            		Log.d(TAG, "User stop passive");
            	}
            	else
            	{
	            	Message mds = handler.obtainMessage(FACEBOOK_GET_USER_INFO_END);
					mds.getData().putBoolean(RESULT, false);
					handler.sendMessage(mds);
            	}
            }
	    });    	
	}

	@Override
	public void doPoke(long uuid,String username) 
	{
		if(isSafeCallFacebook(false) == false)
    	{
    		return ;
    	}
    	
	    if(isPoking == true)
	    {
	        Log.d(TAG,"is Poking, so exit");
	        return;
	    }
    	synchronized(mLock)
        {
           isPoking = true;
        }   	    
		begin();
    	facebookA.pokeAsync(uid,  new FacebookAdapter()
    	{
    		@Override public void poke(boolean suc)
            {
    		    synchronized(mLock)
                {
                   isPoking = false;
                }
    			Message msd = handler.obtainMessage(FACEBOOK_POKE_END);
    			msd.getData().putBoolean(RESULT, true);
    			msd.sendToTarget();
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	Log.d(TAG, "fail to poke="+e.getMessage());	            	
            	synchronized(mLock)
                {
                   isPoking = false;
                }
            	
            	if(isInAynscTaskAndStoped())
            	{
            		Log.d(TAG, "User stop passive");
            	}
            	else
            	{
        		    Message msd = handler.obtainMessage(FACEBOOK_POKE_END);
        		    msd.getData().putBoolean(RESULT,    false);
        		    msd.getData().putString("message", e.getMessage());
	    			msd.sendToTarget();	        		   
            	}
            }
    	});    	
	}
	
	public View.OnClickListener loadOlderClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			Log.d(TAG, "load older message");
			loadOlderPost();
		}
	};
	
	
	private void getLastViewCount(final List<Stream> sts)
	{
		if(streams != null)
		{
			//before clear data, we need clean UI to make sure not crash in view
			if(sts.size() > 0)
			{
				handler.post( new Runnable()
            	{
            		public void run()
            		{
            			status_list.setAdapter(null);
            		}
            	});
			}
			
			synchronized(streams)
	    	{
				//remove pre-serialize stream to let the stream is update to date			
				if(sts.size() == limit && streams.size() > 0)
				{
					Stream tmp = streams.get(0);
		    		if(tmp.isFromSerialize == true)
		    		{
		    		   for(Stream item: streams)
		    		   {
		    			   item.dispose();
		    			   item = null;
		    		   }
		    		   streams.clear();
		    		}		    		
				}
				
		    	for(int i=0;i<sts.size();i++)
				{
		    		Stream item = sts.get(i);
					boolean isExist = false;
					for(int j=0;j<streams.size();j++)
					{
						Stream exist = streams.get(j);
						if(item.post_id.equalsIgnoreCase(exist.post_id))
						{
							isExist=true;
							//update the content						
							exist.dispose();
							exist=null;
							
							streams.set(j, item);
							break;
						}
					}
					
					if(isExist == false)
					{
						streams.add(item);				
					}
			    }	    	
		    	java.util.Collections.sort(streams);	    	
		    	//remove the no use wall, tmp we don't need, but this will cause memory issue   
		    	if(streams.size()>0)
		        {
		             starttime = streams.get(0).updated_time+1000;
		        }	
	    	}
			
			
			// get pageinfo or user info according to streams source_id
			// and then update UI;
			loadUserInfoAndPageInfo(streams);
		}
	}
	
	
    @Override
    protected void loadOlderPost()
    {
		if(streams != null && streams.size() > 0)
		{
			long lasttime = streams.get(streams.size()-1).updated_time;
			
			this.lastVisiblePos = status_list.getFirstVisiblePosition()+1;			
			Log.d(TAG, "pos="+lastVisiblePos);
			
			getWallMessage(lasttime, false);
		}	
		else
		{
			getWallMessage(0, true);
		}
    }
	
	//for load older wall message
	boolean inprocessLoadMsgOlder = false;
	private void getWallMessage(long fromstartTime, boolean newpost) 
	{	
		if(isSafeCallFacebook(false) == false)
    	{
    		return ;
    	}
		
		if(inprocessLoadMsgOlder == true)
		{
			Log.d(TAG, "I am still in loading, return");
			return ;
		}
		
		begin();
		
		//process for UI
        for(int i=status_list.getChildCount()-1;i>0;i--)            
        {
            View v = status_list.getChildAt(i);
            if(Button.class.isInstance(v))
            {
                Button bt = (Button)v;
                bt.setText(getString(R.string.loading_string));
                break;
            }
        }         
        
		synchronized(mLock)
    	{
			inprocessLoadMsgOlder = true;
    	}
		
		facebookA.getWallStreamAsync(uid,fromstartTime, limit, newpost, new FacebookAdapter()
    	{
    		@Override public void getWallStream(List<Stream> sts)
            {
    			Log.d(TAG, "after get stream="+sts.size());
				synchronized(mLock)
		    	{
					inprocessLoadMsgOlder = false;
		    	}
				
				if(donotcallnetwork == false)//I am still alive
                {
					handler.post( new Runnable()
                	{
                		public void run()
                		{
                			status_list.setAdapter(null);
                		}
                	});
					
					getLastViewCount(sts);
					//just has new item will change the UI
	                if(sts.size() > 0)
	                {
	                    handler.obtainMessage(FACEBOOK_GET_STATUS_UPDATES_UI).sendToTarget();                                  
	                }	                
	            	//cancelNotify();		            	
                }
                
                Message rmsg = handler.obtainMessage(FACEBOOK_GET_WALL_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.sendToTarget();
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	synchronized(mLock)
		    	{
            		inprocessLoadMsgOlder = false;
		    	}
            	
            	Message rmsg = handler.obtainMessage(FACEBOOK_GET_WALL_END);
                rmsg.getData().putBoolean(RESULT, false);
                rmsg.sendToTarget();             	
            }
    	});
	}
	
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {         
        	Log.d(TAG, "KEYCODE_BACK coming="+this);            
            System.gc();
            
            this.setResult(0);
            finish();                
        }
        return super.onKeyDown(keyCode, event);
    }	

	public void registerAccountListener() 
	{
		AccountManager.registerAccountListener("FacebookAccountActivity", this);
	}

	public void unregisterAccountListener() 
	{
		AccountManager.unregisterAccountListener("FacebookAccountActivity");
	}
}
