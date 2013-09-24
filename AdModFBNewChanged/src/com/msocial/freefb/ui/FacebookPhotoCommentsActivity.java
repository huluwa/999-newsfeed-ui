package com.msocial.freefb.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.ui.view.ImageRun;
import com.msocial.freefb.ui.view.ImageViewTouchBase;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.Photo;
import oms.sns.service.facebook.model.PhotoAlbum;
import oms.sns.service.facebook.util.StringUtils;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager;

public class FacebookPhotoCommentsActivity extends FacebookBaseActivity
{
	private Photo            photo;
	private PhotoAlbum       album;
	private List<Photo> photos;
	private String          photoid;
	private int              pos;
	private boolean forimageview;
	
	private Button left_button ;
	private Button right_button;
	private Button comments_button;
	private Button share_button;
	private Button setas_button;
	private Button zoom_small;
	private Button zoom_big;
	
	private Button photo_save;
	private Button photo_cancel;
	
	private Button album_update;
	private ImageViewTouchBase photo_image;
	
	private EditText photo_comments;
	private Spinner who_can_see;
    private View    photo_comments_layout;
    
	private EditText album_name;
	private EditText album_location;
	private EditText album_desc;
	private TextView nav_text;
	private final static String[] visibleValues = {"everyone","networks","friends-of-friends","friends"};
	private boolean fromoutside;
	private long    ownerfromout=-1;
	private String  href;
	private float   factor=1.0F; 
	
	@Override
	protected void enableNoTitleBar()
	{
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 
	}
	
	@Override
	protected void showOptionMenu()
	{   
	    boolean fromout = this.getIntent().getBooleanExtra("fromoutside", false);
	    if(fromout)
	    {
	        setTitleMenuIconVisible(true);
	    }
	}
	
	// formoutside means activity was launched by FacebookNotificationManActivity or FacebookStreamItemView
	public boolean isFromOutSide()
	{
	    return fromoutside;
	}
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);    

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 

        Log.d(TAG,"window class name is="+this.getWindow().getClass().getName() +" Window manager name is="+this.getWindow().getClass().getName());
        photo = this.getIntent().getParcelableExtra("photo");
        //pid is from FacebookNotification when photo is null 
        //if photo is null, pid is existed , we can get photo info/album info from website used photoid 
        photoid = this.getIntent().getStringExtra("pid"); 
        album = this.getIntent().getParcelableExtra("album");
        fromoutside = this.getIntent().getBooleanExtra("fromoutside", false);
        ownerfromout = this.getIntent().getLongExtra("owner", -1);
        href         = this.getIntent().getStringExtra("href");
        
        setTitle();
        setTitle(title);


        if(album != null)
        {
            setContentView(R.layout.facebook_album_edit);
        }
        else if(photo != null || isEmpty(photoid) == false)
        {
        	setContentView(R.layout.facebook_album_photo_edit);
        	
        	left_button = (Button)this.findViewById(R.id.left_button);
         	right_button = (Button)this.findViewById(R.id.right_button);
         	right_button.setOnClickListener(nextPhotoClick);
         	left_button.setOnClickListener(prePhotoClick);
         	left_button.setClickable(true);
         	nav_text = (TextView)this.findViewById(R.id.nav_text);
         	nav_text.setText("0/0");
         	comments_button = (Button)this.findViewById(R.id.comments_button); 
            comments_button.setOnClickListener(onClickComments);
            
            share_button = (Button)this.findViewById(R.id.share_button_photo); 
            share_button.setOnClickListener(onClickShare);            
            
            setas_button = (Button)this.findViewById(R.id.setas_button_photo);
            setas_button.setOnClickListener(setAsClick);
            
            photo_comments_layout = this.findViewById(R.id.photo_comments_layout);  
            
            zoom_small = (Button)this.findViewById(R.id.zoom_small);
            zoom_small.setId(0);
            zoom_big = (Button)this.findViewById(R.id.zoom_big);
            zoom_big.setId(1);
            zoom_small.setOnClickListener(zoomClick);
            zoom_big.setOnClickListener(zoomClick);
        }

        View v = findViewById(R.id.facebook_header_id);
        if(v != null)
        {
       	    v.setVisibility(View.GONE);
        }
       
        photo_image = (ImageViewTouchBase)this.findViewById(R.id.photo_image);  
        if(photo_image != null)
        {
        	photo_image.context = this;
        }
        
        forimageview = this.getIntent().getBooleanExtra("forimageview", false);      
        
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
            perm_session = loginHelper.getPermanentSesstion(this);
            if(perm_session != null)
            {
                perm_session.attachActivity(this);
                
                facebookA = new AsyncFacebook(perm_session);
                
                if(forimageview == true)
                {
                    photos = this.getIntent().getParcelableArrayListExtra("photos");
                    
                    //get photo from db,get photolist from db
                    // if photo is not existed in db, get launch get photo from website
                    if(photo == null && isEmpty(photoid)== false)
                    {
                        photo = orm.getAlbumPhoto(photoid);
                        if(photo!=null && photos == null)
                        {
                            photos = orm.getAlbumPhotos(photo.aid);
                        }
                        else
                        {
                            launchGetPhotoInfoByPID(photoid);
                        }               
                    }
                }

                initButtonVisiblity();
                navigatePhoto();
                setUI();
            }
            else
            {
                launchFacebookLogin();
            }
        }
    }
	

    private void initButtonVisiblity() {
        if(photo != null)
        {
            int visible = 0;
            if(perm_session!=null && perm_session.getLogerInUserID()==photo.owner)
            {
                visible = View.VISIBLE;
            }
            else
            {
                visible = View.INVISIBLE;
            }
            visible = View.VISIBLE;
            if(setas_button != null)
            {
            	//BUG
            	//always for user
                //setas_button.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            if(share_button != null)
            {
                share_button.setVisibility(View.VISIBLE);
            }
            if(setas_button != null)
            {
                setas_button.setVisibility(View.INVISIBLE);
            }
        }
    }


    View.OnClickListener zoomClick = new View.OnClickListener()
    {
        public void onClick(View v) {
            if(v.getId() == 0)
            {
                scaleSmall();
            }
            else if(v.getId() == 1)
            {
                scaleLarge();
            }
        }      
    };
	
	private void setPhoto(String pid)
	{
	    for(Photo ph : photos)
        {
            if(ph.pid.equals(pid))
            {
                photo = ph;
                break;
            }
        }
	}
	
	private void navigatePhoto()
	{
	    String temp_pid = "";
	    if(photo!=null)
	    {
	       temp_pid = photo.pid; 
	    }
	    else if(isEmpty(photoid) == false)
	    {
	        temp_pid = photoid;
	    }
	    if(photos != null && isEmpty(temp_pid) == false)
        {
            Log.d(TAG, "I have photos="+photos.size());
            //find current pos
            for(int i=0;i<photos.size();i++)
            {
                Photo item = photos.get(i);                 
                if(item.pid.equals(temp_pid))
                {
                    Log.d(TAG, "I am at ="+i);
                    pos = i;
                    break;
                }
            }
        }
	}
	
	private void launchGetPhotoInfoByPID(String pid)
	{
	    Message msd = handler.obtainMessage(PHOTO_GET_BEGIN);
	    msd.getData().putString("pid", pid);
	    msd.sendToTarget();
	}
	
	private void getPhotoInfoByPID(final String pid)
	{
	    Log.d(TAG,"entering getPhotoInfoByPID pid is "+pid);
        if(this.isInProcess() == true)
        {
            return;
        }
        
        if(existSession() == false)
        {
            return;
        }  
        
        begin();
        
        synchronized(mLock)
        {
            inprocess = true;
        }
        
        facebookA.getPhotoListByPID(pid, new FacebookAdapter()
        {
            @Override
            public void getPhotoListByPID(List<Photo> photoList) {
                // TODO Auto-generated method stub
                Log.d(TAG,"photos size is "+photoList.size()); 
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                if(photos == null)
                {
                    photos = (ArrayList<Photo>)photoList;
                }
                
                if(photos.size() > 0 )
                {
                    //add photo to photos
                    orm.addPhoto(photos,true);
                }
                setPhoto(pid);
                handler.obtainMessage(PHOTO_GET_UI).sendToTarget();
                handler.obtainMessage(PHOTO_GET_END).sendToTarget();
            }
            @Override public void onException(FacebookException e, int method) 
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                Log.d(TAG, "get PhotoList Exception ex="+e.getMessage());
                
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                    Message rmsg = handler.obtainMessage(PHOTO_GET_END);
                    //rmsg.getData().putBoolean(RESULT, false);
                    rmsg.sendToTarget();
                }
            }
        });
	}
	
	//@Override
	protected boolean  goRightPage()
	{
		//super.goNextPage();
		if(forimageview && photos != null)
		{
			if(pos <photos.size()-1)
			{
				photo = photos.get(pos+1);
				pos++;
				setUI();
				return true;
			}
		}
		return false;
	}	
	
	//@Override
	protected boolean  goLeftPage()
	{
		//super.goPrePage();		
		if(forimageview && photos != null && photos.size() > 1)
		{
			if(pos > 0)
			{
				photo = photos.get(pos-1);
				pos--;				
				setUI();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void stopLoading()
    {
	    super.stopLoading();
	    
	    if(photo != null)
        {
            if(photo_image != null)
            {
            	Log.i(TAG,"photo_image setImageBitmap null~~~~~~~~~");
                photo_image.setImageBitmap(null);
            }
        }
    }
	
	public boolean isOwner()
    {
        if(perm_session !=null && (
                (album!=null && perm_session.getLogerInUserID() == album.owner) ||
                (photo!=null && perm_session.getLogerInUserID() == photo.owner)))
        {
            return true;
        }
        else
        {
            //we ignore the case, not get data from network
            //
            if(perm_session !=null && ownerfromout == perm_session.getLogerInUserID())
            {
                return true;
            }
            return false;
        }
    }
	/*
	 * not owner, let them add commend, 
	 * for owner, let he/she change caption
	 */
	private void setPhotoUI(boolean isnotOwner, boolean isForComments)
	{
	    if(isnotOwner || isForComments)
        {
            //show comments and like?
            photo_save   = (Button)this.findViewById(R.id.photo_save);
            photo_cancel = (Button)this.findViewById(R.id.photo_cancel);
            
            photo_comments = (EditText)this.findViewById(R.id.photo_comments);
            photo_comments.setHint(R.string.photo_your_comments);
                            
            photo_save.setText(R.string.sns_add_comment);
            //no like currently
            //photo_cancel.setText(R.string.sns_like);
            
            photo_save.setOnClickListener(photoCommentsClick);  
            //no like currently, so just cancel
            //photo_cancel.setOnClickListener(photoLikeClick);
            photo_cancel.setOnClickListener(cancelClick);
        }
        else                
        {
            View photo_comments_layout = this.findViewById(R.id.photo_comments_layout);
            photo_comments_layout.setVisibility(View.VISIBLE);              
            if(photo != null)
            {
                Configuration newConfig = getResources().getConfiguration();
                if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
                {
                    photo_comments_layout.setVisibility(View.GONE);
                }
                else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) 
                {
                    photo_comments_layout.setVisibility(View.VISIBLE);
                    
                    photo_comments = (EditText)this.findViewById(R.id.photo_comments);
                    photo_comments.setText(photo.caption);   
                    photo_comments.setHint(R.string.photo_hint_caption_comments);
                    
                    photo_save   = (Button)this.findViewById(R.id.photo_save);
                    photo_cancel = (Button)this.findViewById(R.id.photo_cancel);
                    
                    photo_save.setOnClickListener(photoCaptionClick);
                    photo_cancel.setOnClickListener(cancelClick);
                }
            }
        }
	}
	protected void setZoom(float scale)
	{
	    Bitmap bmp = photo_image.getDrawingCache();
	    if(bmp != null)
	    {
	        
	    }
	}
	
	int mX = 0;
	int mY = 0;
	private void scaleSmall() {
		 Log.d(TAG, "scaleSmall");
	    int width  = photo_image.getWidth();
        int height = photo_image.getHeight();
        int screenWidth = mContext.getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = mContext.getWindowManager().getDefaultDisplay().getHeight();
        
        if(width>screenWidth && width > 200)
        {
            //screenHeight = (int)(((float)screenWidth * (float)height)/(float)width);
            if(height<screenHeight)
            {
                photo_image.scrollBy(-mX, 0);
            }
            else
            {
                photo_image.scrollBy(-mX, -mY);
            }
        }
        
        Log.d(TAG, "width="+width + " height="+height+" screenWidth="+screenWidth+"screenHeight="+screenHeight); 
        if(width > 200)
        {
            int afterwidth  = (int)(width*0.9);
            int afterheight =  (int)(height*0.9);
            LayoutParams params = photo_image.getLayoutParams();
            Log.d(TAG, params.getClass().getName());
            params.height = afterheight;
            params.width  = afterwidth;
            photo_image.setLayoutParams(params);
            
            if(afterwidth > screenWidth)
            {
                if(afterheight < screenHeight)
                {
                    mX = (int)((afterwidth - screenWidth)/2);
                    mY = 0;
                    photo_image.scrollBy(mX, mY);
                }
                else
                {
                    mX = (int)((afterwidth - screenWidth)/2);
                    mY = (int)((afterheight - screenHeight)/2);
                    photo_image.scrollBy(mX, mY);
                }
            }
        }
        
//        Log.d(TAG, "width2="+photo_image.getWidth() + " height2="+photo_image.getHeight());
//        Log.d(TAG, "Left="+photo_image.getLeft());
//        photo_image.layout(photo_image.getLeft(), photo_image.getTop(), photo_image.getLeft()+photo_image.getWidth(), photo_image.getTop()+photo_image.getHeight());
//        photo_image.invalidate();
//        Log.d(TAG, "invalidate---");
        //setImage();
        /*
	    Matrix matrix = new Matrix();
        matrix.setScale(0.9F, 0.9F);        
        
        Bitmap mBaseImage =  photo_image.getDrawingCache();
        if(mBaseImage != null)
        {
            mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage
                    .getWidth(), mBaseImage.getHeight(), matrix, true);
            photo_image.setImageBitmap(mBaseImage);
        }
        */
    }

    private void scaleLarge() {
        int width  = photo_image.getWidth();
        int height = photo_image.getHeight();
        int screenWidth = mContext.getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = mContext.getWindowManager().getDefaultDisplay().getHeight();
        
        if(width>screenWidth && width < 2000)
        {
            screenHeight = (int)(((float)screenWidth * (float)height)/(float)width);
            if(height<screenHeight)
            {
                photo_image.scrollBy(-mX, 0);
            }
            else
            {
                photo_image.scrollBy(-mX, -mY);
            }
        }
        Log.d(TAG, "width="+width + " height="+height+" screenWidth="+screenWidth+"screenHeight="+screenHeight); 
//        Log.i(TAG, "paddingLeft:"+photo_image.getPaddingLeft()+" getLeft:"+photo_image.getLeft());
//        Log.i(TAG, "click cache:"+   photo_image.getDrawingCache()+"  drawable:"+photo_image.getDrawable());
//        BitmapDrawable drawable = (BitmapDrawable)photo_image.getDrawable();
//        Bitmap mBaseImage = drawable.getBitmap();
//        Log.d(TAG, "mBaseImage: width="+width + " height="+height);
        if(width < 2000)
        {
            int afterwidth  = (int)(width*1.1);
            int afterheight =  (int)(height*1.1);
            LayoutParams params = photo_image.getLayoutParams();
            Log.d(TAG, params.getClass().getName());
            params.height = afterheight;
            params.width  = afterwidth;
            photo_image.setLayoutParams(params);
            if(afterwidth > screenWidth)
            {
                if(afterheight < screenHeight)
                {
                    mX = (int)((afterwidth - screenWidth)/2);
                    mY = 0;
                    photo_image.scrollBy(mX, mY);
                }
                else
                {
                    mX = (int)((afterwidth - screenWidth)/2);
                    mY = (int)((afterheight - screenHeight)/2);
                    photo_image.scrollBy(mX, mY);
                }
            }
            //photo_image.scrollBy(scaledwidth, scaledheight);
//            Log.i(TAG, "id:"+photo_image.getId()+"paddingLeft2:"+photo_image.getPaddingLeft()+" getLeft:"+photo_image.getLeft());
            //setImage();
            
            /*
            Matrix matrix = new Matrix();
            matrix.setScale(1.1F, 1.1F);        
            Bitmap mBaseImage =  photo_image.getDrawingCache();
            if(mBaseImage != null)
            {
                if(afterwidth > mBaseImage.getWidth())
                {
                    mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage
                            .getWidth(), mBaseImage.getHeight(), matrix, true);
                    photo_image.setImageBitmap(mBaseImage);
                }
            }*/
//            if(mBaseImage != null)
//            {
////                if(afterwidth > mBaseImage.getWidth())
//                {
//                	int left = photo_image.getLeft();
//                	int top = photo_image.getTop();
//                	LinearLayout layout = (LinearLayout)this.findViewById(R.id.photo_span);
//                	layout.removeView(photo_image);
//                	
//                	float scaleWidth = mBaseImage.getWidth()*1.1F;
//                	float scaleHeigth = mBaseImage.getHeight()*1.1F;
//                	Matrix matrix = new Matrix();
//                	matrix.postScale(1.1f, 1.1f);        
//                	
//                    mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage.getWidth(), mBaseImage.getHeight(), matrix, true);
//                    ImageViewTouchBase new_photo_image = new ImageViewTouchBase(this.getApplicationContext());
//                    new_photo_image.setImageBitmap(mBaseImage);
//                    Log.i(TAG,"newImage width="+mBaseImage.getWidth()+"  height="+mBaseImage.getHeight());
////                    new_photo_image.setPadding(photo_image.getPaddingLeft(), photo_image.getPaddingTop(), photo_image.getPaddingLeft()+mBaseImage.getWidth(),photo_image.getPaddingTop()+mBaseImage.getHeight());
//                    new_photo_image.setPadding(photo_image.getLeft(), photo_image.getTop(), photo_image.getLeft()+mBaseImage.getWidth(),photo_image.getTop()+mBaseImage.getHeight());
//                    new_photo_image.setId(R.id.photo_image);
//                    photo_image = new_photo_image;
//                    layout.addView(photo_image);
//                    layout.postInvalidate();
//                }
//            }else{
//            	  Log.i(TAG,"mBaseImage is null ~~");
//            }
        }
    }

    private void rotateLeft() {
        Matrix matrix = new Matrix();
        matrix.setRotate(-90.0F);
        Bitmap mBaseImage =  photo_image.getDrawingCache();
        if(mBaseImage != null)
        {
            mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage.getHeight(), mBaseImage
                    .getWidth(), matrix, true);
            photo_image.setImageBitmap(mBaseImage);
        }
    }

    private void rotateRight() {
        Matrix matrix = new Matrix();
        matrix.setRotate(90.0F);
        Bitmap mBaseImage =  photo_image.getDrawingCache();
        if(mBaseImage != null)
        {
            mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage.getHeight(), mBaseImage
                    .getWidth(), matrix, true);
            photo_image.setImageBitmap(mBaseImage);
        }
    }
    
    private void setImage()
    {
        String filebigPath = TwitterHelper.getImagePathFromURL_noFetch(photo.src_big);
        if(filebigPath != null && new File(filebigPath).exists() && new File(filebigPath).length() > 0)
        {
            Log.d(TAG, "I have big one, no need use the small one");
            try{                    
                Bitmap tmp = BitmapFactory.decodeFile(filebigPath);
                photo_image.setImageBitmap(tmp);
            }
            catch(Exception ne)
            {
                Log.d(TAG, "bad big one, set small one="+ne.getMessage());
                //if fail to set big pic, set small one
                String filePath = TwitterHelper.getImagePathFromURL_noFetch(photo.src_small);
                if(filePath != null && new File(filePath).exists() == true)
                {
                    try{
                        Log.d(TAG, "set small one="+filePath);
                        Bitmap tmp = BitmapFactory.decodeFile(filePath);
                        photo_image.setImageBitmap(tmp);
                    }catch(Exception e)
                    {
                        Log.d(TAG, "fail to set small image="+filePath);
                    }
                }
            }
        }
        else
        { 
            try{
                Log.d(TAG, "big not exist, so set small one");
                String filePath = TwitterHelper.getImagePathFromURL_noFetch(photo.src_small);
                if(filePath != null && new File(filePath).exists() == true)
                {
                    Bitmap tmp = BitmapFactory.decodeFile(filePath);
                    photo_image.setImageBitmap(tmp);                    
                }
            }catch(Exception ne){Log.d(TAG, "set small one="+ne.getMessage());}
        }
    }
    
	private void setUI() 
	{   
	    if(photo_image != null)
	    {
	        photo_image.scrollTo(0, 0);
	    }
	    //for show photo	    
	    if(photo == null && forimageview == true)
	    {
	        //for show small photo
	        if(isEmpty(href) == false)
	        {
    	        String filePath = TwitterHelper.getImagePathFromURL_noFetch(href);
                if(new File(filePath).exists())
                {
                    Log.d(TAG, "I have small one");                
                    try{                        
                        if(filePath != null)
                        {
                            Bitmap tmp = BitmapFactory.decodeFile(filePath);
                            photo_image.setImageBitmap(tmp);                    
                        }
                    }catch(Exception ne){}
                }
                
                ImageRun imagerun = new ImageRun(handler, href, 0);                
                imagerun.setImageView(photo_image);
                imagerun.post(imagerun);
	        }
            
	        setPhotoUI(isOwner()==false, false);
	    }
	    else if(photo != null)
		{   
		    int photonum = photos!=null?photos.size():0;
		    nav_text.setText((pos+1)+"/"+photonum);			
			photo_image.setImageBitmap(null);
			String filebigPath = TwitterHelper.getImagePathFromURL_noFetch(photo.src_big);
			if(filebigPath != null && new File(filebigPath).exists() && new File(filebigPath).length()>0)
			{
				Log.d(TAG, "I have big one, no need use the small one");
				try{				    
		    	    Bitmap tmp = BitmapFactory.decodeFile(filebigPath);
		    	    photo_image.setImageBitmap(tmp);
			    }
				catch(Exception ne)
				{
					Log.d(TAG, "bad big one, set small one="+ne.getMessage());
				    //bad fle, re-get it
				    ImageRun imagerun = new ImageRun(handler,photo.src_big, 0);
	                imagerun.noimage = true;
	                imagerun.setImageView(photo_image);
	                imagerun.post(imagerun);	                
					
					//if fail to set big pic, set small one
					String filePath = TwitterHelper.getImagePathFromURL_noFetch(photo.src_small);
				    if(filePath != null && new File(filePath).exists() == true)
		    	    {
				        try{
				        	Log.d(TAG, "set small one="+filePath);
		    	            Bitmap tmp = BitmapFactory.decodeFile(filePath);
		    	            photo_image.setImageBitmap(tmp);
				        }catch(Exception e)
				        {
				            Log.d(TAG, "fail to set small image="+filePath);
				        }
		    	    }
				}
			}
			else
			{   
			    ImageRun imagerun = new ImageRun(handler,photo.src_big, 0);
                imagerun.noimage = true;
                imagerun.setImageView(photo_image);
                imagerun.post(imagerun);
                
				try{
				    Log.d(TAG, "big not exist, so set small one");
				    String filePath = TwitterHelper.getImagePathFromURL_noFetch(photo.src_small);
				    if(filePath != null && new File(filePath).exists() == true)
		    	    {
		    	        Bitmap tmp = BitmapFactory.decodeFile(filePath);
		    	        photo_image.setImageBitmap(tmp);	    	        
		    	    }
			    }catch(Exception ne){Log.d(TAG, "set small one="+ne.getMessage());}
			}
	        
	        boolean forcomments = this.getIntent().getBooleanExtra("forcomments", false);
	        boolean isnotOwner = (photo.owner != getLoginUserID());	        
	        setPhotoUI(isnotOwner,  forcomments);	        
	        
	        if(photos != null)
	        {
	        	if(pos < photos.size()-1)
	        	{
	        		right_button.setVisibility(View.VISIBLE);
	        	}
	        	else if(pos >= photos.size()-1)
	        	{
	        		right_button.setVisibility(View.GONE);
	        	}
	        	
	        	if(pos >0)
	        	{
	        		left_button.setVisibility(View.VISIBLE);
	        	}
	        	else if(pos == 0)
	        	{
	        		left_button.setVisibility(View.GONE);
	        	}
	        }
	        else
	        {
	        	left_button.setVisibility(View.GONE);
	        	right_button.setVisibility(View.GONE);
	        }
		}
		else if(album != null)
		{
		    PhotoAlbum tmpAlbum = orm.getAlbum(album.aid);
		    if(tmpAlbum!=null)
		    {
		        album = tmpAlbum;
		    }
			album_name = (EditText)this.findViewById(R.id.album_name);
			album_name.setText(album.name);
			
			album_location = (EditText)this.findViewById(R.id.album_location);
			album_location.setText(album.location);
			
		    album_desc = (EditText)this.findViewById(R.id.album_desc);
			album_desc.setText(album.description);

			
			album_update = (Button)this.findViewById(R.id.album_update);
			Button album_cancel = (Button)this.findViewById(R.id.album_cancel);
			
			album_update.setOnClickListener(updateAlbumClick);
			album_cancel.setOnClickListener(cancelClick);
			
			String album_visible = (isEmpty(album.visible)== true)?"everyone":album.visible;
			int selectPos = 0;
			//set Spin data
			who_can_see = (Spinner)this.findViewById(R.id.who_can_see);
			List<String>items = new ArrayList<String>();
			CharSequence[] visualNames = getResources().getTextArray(R.array.share_orientation_list);
			for(int i=0;i<visualNames.length;i++)
			{
			    String itemstr = visualNames[i].toString();
				items.add(itemstr);
				if(album_visible.equalsIgnoreCase(visibleValues[i]))
				{
				    selectPos = i;
				}
			}
			
			WhoSpinnerAdapter sa = new WhoSpinnerAdapter(mContext, android.R.layout.simple_spinner_item, items);
			sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			who_can_see.setAdapter(sa);
			if(isEmpty(album.visible) == false)
			{
			    who_can_see.setSelection(selectPos);
			}
		}
	}
	
	
	public class WhoSpinnerAdapter extends ArrayAdapter<String>{
	    List<String> objects ;
	    Context mContext;
	    public WhoSpinnerAdapter(Context context,
	            int resource, List<String> albums) {
	        super(context,resource, albums);
	        objects = albums;
	        mContext = context;
	    }
	    @Override
	    public int getCount() {
	        return objects.size();
	    }
	    @Override
	    public String getItem(int position) {
	        return objects.get(position);
	    }   
	    
	    public int getPos(String aid)
	    {
	        for(int i=0;i<objects.size();i++)
	        {
	            if(objects.get(i).equals(aid))
	            {
	                return i;
	            }
	        }
	        
	        return -1;
	    }
	}
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {		
		super.onConfigurationChanged(newConfig);
		if(photo != null)
		{
			if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
			{
			    photo_comments_layout.setVisibility(View.GONE);
			}
			else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) 
			{
				photo_comments_layout.setVisibility(View.VISIBLE);
			}
		}
	}

	public void launchAddPhotoComment()
	{
	    handler.obtainMessage(COMMENTS_ADD_POST).sendToTarget();
	}
	
	   
    private void launchEditPhotoAlbum()
    {
        handler.obtainMessage(ALBUM_EDIT).sendToTarget();
    }
    
    private void launchEditPhoto()
    {
        handler.obtainMessage(PHOTO_EDIT).sendToTarget();
    }
    private void editPhotoAlbum()
    {
        final String albumname = album_name.getText().toString();
        final String albumlocation = album_location.getText().toString(); 
        final String albumdesc = album_desc.getText().toString();
        int selectedIndex = who_can_see.getSelectedItemPosition();
        Log.d(TAG, "selected item is "+selectedIndex);
        final String visible = visibleValues[selectedIndex];
        if(StringUtils.isEmpty(albumname) == true)
        {
            return ;
        }
        
        if(existSession() == false)
        {
            return;
        }
        
        if(this.isInProcess() == true)
        {
            dismissDialog(DLG_SAVE_CHANGE);
            showDialog(DLG_SAVE_CHANGE);
            return;
        }
        
        begin();
        album_update.setEnabled(false);
        showDialog(DLG_SAVE_CHANGE);
        synchronized(mLock)
        {
            inprocess = true;
        }
        
        
        facebookA.photoEditAlbumAsync(album.aid,albumname.trim(),albumlocation.trim(),albumdesc.trim(),visible,new FacebookAdapter()
        {
            @Override public void photoEditAlbum(boolean photoAlbum)
            {
                Log.d(TAG, "after edit album = ");
                synchronized(mLock)
                {
                    inprocess = false;
                }
                if(photoAlbum == true)
                {
                    album.name = albumname.trim();
                    album.description = albumdesc.trim();
                    album.location = albumlocation.trim();
                    album.visible = visible;
                    orm.updateAlbum(album);
                }
                Message rmsg = handler.obtainMessage(ALBUM_EDIT_END);
                rmsg.getData().putBoolean(RESULT, photoAlbum);
                rmsg.sendToTarget();
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                Log.d(TAG, "edit album ex="+e.getMessage());
                
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                    Message rmsg = handler.obtainMessage(ALBUM_EDIT_END);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.sendToTarget();
                }
            }
        });
        
    }
	
	private void addPhotoComment()
	{
	    String content = photo_comments.getText().toString();
        if(StringUtils.isEmpty(content) == true)
        {
            return;
        }
        
	    if(existSession() == false)
        {
            return;
        }   
	    if(this.isInProcess() == true)
	    {
	        dismissDialog(DLG_ADD_COMMAND);
	        showDialog(DLG_ADD_COMMAND);
	        return;
	    }
        String tmpid = photo == null?photoid:photo.pid;
        if(isEmpty(tmpid) == true) return;
        
        begin();
        photo_save.setEnabled(false);
        
        synchronized(mLock)
        {
            inprocess = true;
        }
        showDialog(DLG_ADD_COMMAND);
        facebookA.photoAddCommentsAsync(tmpid, content.trim(),new FacebookAdapter()
        {
            @Override public void photoAddComments(boolean suc)
            {
                Log.d(TAG, "after add comments="+suc);
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                Message rmsg = handler.obtainMessage(COMMENTS_ADD_POST_END);
                rmsg.getData().putBoolean(RESULT, suc);
                rmsg.sendToTarget();
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                Log.d(TAG, "add photo comment ex="+e.getMessage());
                
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                    Message rmsg = handler.obtainMessage(COMMENTS_ADD_POST_END);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.sendToTarget();
                }
            }
        });
	}
	
	private void editPhoto(){
	    final String content = photo_comments.getText().toString();
        if(StringUtils.isEmpty(content) == true)
        {
            return;
        }
        
        if(existSession() == false)
        {
            return;
        } 
        if(this.isInProcess() ==  true)
        {
            dismissDialog(DLG_EDIT_PHOTO_CAPTION);
            showDialog(DLG_EDIT_PHOTO_CAPTION);
            return;
        }
        
        String tmpid = photo == null?photoid:photo.pid;
        if(isEmpty(tmpid) == true) return;
        
        begin();
        showDialog(DLG_EDIT_PHOTO_CAPTION);
        photo_save.setEnabled(false);
        synchronized(mLock)
        {
            inprocess = true;
        }
        
        facebookA.editPhotoAsync(tmpid, content.trim(),new FacebookAdapter()
        {
            @Override public void editPhoto(boolean suc)
            {
                Log.d(TAG, "after edit photo="+suc);
                synchronized(mLock)
                {
                    inprocess = false;
                }
                if(suc == true)
                {
                    photo.caption = content.trim();
                    orm.updatePhoto(photo);
                }
                Message rmsg = handler.obtainMessage(PHOTO_EDIT_END);
                rmsg.getData().putBoolean(RESULT, suc);
                rmsg.sendToTarget();
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                Log.d(TAG, "edit photo  ex="+e.getMessage());
                
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                    Message rmsg = handler.obtainMessage(PHOTO_EDIT_END);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.sendToTarget();
                }
            }
        });
	}

	
	View.OnClickListener nextPhotoClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
		    goRightPage();
		}
	};
	
	View.OnClickListener prePhotoClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			Log.d(TAG, "pre view");
			goLeftPage();
		}
	};
	
	//for comments	list show
	View.OnClickListener onClickComments = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			Log.d(TAG, "click comment list ");
			if(photo != null)
            {   
            	Intent intent = new Intent(mContext, FacebookPhotoCommentsViewActivity.class);
        		intent.putExtra("photo", photo);
        		startActivity(intent);
            }
		}
	};
	//for set as
	
	View.OnClickListener setAsClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			Log.d(TAG, "click set as items");
			Intent send = new Intent(mContext, Wallpaper.class);
            if(photo != null)
            {	                 
	             String ss = TwitterHelper.getImagePathFromURL_noFetch(photo.src_big);
	             if(isEmpty(ss) == false && new File(ss).exists() == true)
	             {
	                 send.setData(Uri.fromFile(new File(ss)));
	             }
	             else
	             {
	            	 Log.d(TAG, "data is not save in phone");
	             }
            }
            FacebookPhotoCommentsActivity.this.startActivityForResult(send, 9879);
		}
	};	

	//for share
	View.OnClickListener onClickShare = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			Log.d(TAG, "click share items");		
            if(photo != null)
            {
            	 Intent send = new Intent(Intent.ACTION_SEND);
                 send.setType("text/plain");                 
                 send.putExtra(Intent.EXTRA_TEXT,   photo.link);                                  
                 
                 try {
                	 FacebookPhotoCommentsActivity.this.startActivity(Intent.createChooser(send,"Share"));
                 } catch(android.content.ActivityNotFoundException ex) {
                     // if no app handles it, do nothing
                 }

            }
		}
	};	
	
	//for photo caption
	View.OnClickListener photoCaptionClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
		    hideInputKeyBoard(v);
			launchEditPhoto();
		}
	};
			
	//for comments
	View.OnClickListener photoCommentsClick = new View.OnClickListener()
	{

		public void onClick(View v) 
		{
		    hideInputKeyBoard(v); 
		    launchAddPhotoComment();
		}
	};
	
	//for photo like
	View.OnClickListener photoLikeClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			finish();
		}
	};
	
	//for update album
	View.OnClickListener updateAlbumClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{   hideInputKeyBoard(v);
			launchEditPhotoAlbum();
		}
	};
	View.OnClickListener cancelClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			finish();
		}
	};
	
	@Override
    protected void GoToAlbum()
	{
	    if(isEmpty(getAlbumID()) == false)
	    {
	        Log.d(TAG," go to album albumid is "+getAlbumID());
	        Intent intent = new Intent(mContext, FacebookAlbumViewActivity.class); 
	        intent.putExtra("albumid",getAlbumID());  
	        intent.putExtra("fromoutside",  true);
	        mContext.startActivity(intent);   
	    }
	    else
	    {
	        Log.d(TAG," why album id is empty");
	    }
	   
	}

	@Override
	protected void createHandler() 
	{
		handler = new EditHandler();
	}
	
	final static int COMMENTS_ADD_POST     = 0;    
    final static int COMMENTS_ADD_POST_END = 1;
    final static int ALBUM_EDIT            = 2;    
    final static int ALBUM_EDIT_END        = 3;
    final static int PHOTO_EDIT            = 4;
    final static int PHOTO_EDIT_END       = 5;
    final static int PHOTO_GET_BEGIN       = 6;
    final static int PHOTO_GET_UI          = 7;
    final static int PHOTO_GET_END         = 8; 
    
    private class EditHandler extends Handler 
    {
        public EditHandler()
        {
            super();            
            Log.d(TAG, "new EditHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case COMMENTS_ADD_POST:
                {
                    addPhotoComment();
                	break;
                }
                case COMMENTS_ADD_POST_END:
                {
                    end();
                    dismissDialog(DLG_ADD_COMMAND);
                    photo_save.setEnabled(true);
                    boolean result = msg.getData().getBoolean(RESULT);
                    if(result == true)
                    {
                        photo_comments.setText("");
                        Toast.makeText(mContext, R.string.sns_operate_succeed, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                    	Toast.makeText(mContext, R.string.sns_operate_failed, Toast.LENGTH_SHORT).show();                        
                    }
                    
                    break;
                }
                case ALBUM_EDIT:
                {
                    editPhotoAlbum();
                    break;
                }
                case ALBUM_EDIT_END:
                {
                    end();
                    dismissDialog(DLG_SAVE_CHANGE);
                    album_update.setEnabled(true);
                    if(photo_save!=null)
                    {
                        photo_save.setEnabled(true);
                    }
                    boolean result = msg.getData().getBoolean(RESULT);
                    if(result == true)
                    {                     
                        Toast.makeText(mContext, R.string.sns_operate_succeed, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                    	Toast.makeText(mContext, R.string.sns_operate_failed, Toast.LENGTH_SHORT).show();                        
                    }
                    
                    break;
                }
                case PHOTO_EDIT:
                {
                    editPhoto();
                    break;
                }
                case PHOTO_EDIT_END:
                { 
                    end();
                    dismissDialog(DLG_EDIT_PHOTO_CAPTION);
                    photo_save.setEnabled(true);
                    boolean result = msg.getData().getBoolean(RESULT);
                    if(result == true)
                    {
                    	photo_comments.setText("");
                        Toast.makeText(mContext, R.string.sns_operate_succeed, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                    	Toast.makeText(mContext, R.string.sns_operate_failed, Toast.LENGTH_SHORT).show();                        
                    }                    
                    break;
                }
                case PHOTO_GET_BEGIN:
                {
                    String pid = msg.getData().getString("pid");
                    if(isEmpty(pid) == false)
                    {
                       getPhotoInfoByPID(pid);
                    }
                    break;
                }
                case PHOTO_GET_UI:
                {
                    initButtonVisiblity();
                    navigatePhoto();
                    setUI();
                    break;
                }
                case PHOTO_GET_END:
                {
                    end();
                    break;
                }
            }
        }
    }
    
    
	protected String getAlbumID()
	{
	    if(photo!=null)
	    {
	       return photo.aid;
	    }
	    else if(photos != null && photos.size()>0)
	    {
	        return photos.get(0).aid;
	    }
	    else
	    {
	        return "";
	    }
	}
	
    public void setTitle() 
	{
		title = "";
	}

	public void registerAccountListener() 
	{
		
	}

	public void unregisterAccountListener() 
	{	
		
	}
}
