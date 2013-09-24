package com.msocial.freefb.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.ui.AccountListener.AccountManager;
import com.msocial.freefb.ui.adapter.FacebookAlbumSpinnerAdapter;
import com.msocial.freefb.ui.adapter.FacebookStatusContentAdapter;
import com.msocial.freefb.ui.adapter.FacebookStatusItem;
import com.msocial.freefb.ui.view.FacebookStatusContentItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.Attachment;
import oms.sns.service.facebook.model.Photo;
import oms.sns.service.facebook.model.PhotoAlbum;
import oms.sns.service.facebook.model.Video;
import oms.sns.service.facebook.model.Attachment.AttachmentMediaImage;
import oms.sns.service.facebook.model.Attachment.BundleActionLink;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MediaUploadActivity extends FacebookBaseActivity {
    private final String TAG="MediaUploadActivity";
    private EditText contentEdit;
	private MyWatcher watcher;
	private TextView textCount;
	private View     albumSpan;
	private Spinner  albumSpinner;
	private EditText album_editor;
	private Button   new_album_button;
	
	private String mSrc = "";
	private String content = "";
	ListView      contentList;	
	private List<FacebookStatusItem> currentStatuses = new ArrayList<FacebookStatusItem>();
	private String mediatype;
	private Button facebook_share_button;
	/** Called when the activity is first created.*/
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);   
        setContentView(R.layout.facebook_status_update);// upload ui same withe facebook_status_update
        
        contentEdit = (EditText)this.findViewById(R.id.facebook_status_message_editor);
        contentEdit.setHint(R.string.facebook_photo_upload_compose);      
        contentEdit.setVerticalScrollBarEnabled(true);
        textCount = (TextView)this.findViewById(R.id.facebook_status_text_counter); 
        contentList = (ListView)this.findViewById(R.id.facebook_update_content_list);
        albumSpan    = (View)this.findViewById(R.id.facebook_album_span);
        albumSpinner = (Spinner)this.findViewById(R.id.facebook_album_spinner);
        album_editor = (EditText)this.findViewById(R.id.facebook_album_editor);
        new_album_button = (Button)this.findViewById(R.id.facebook_new_album_button);
        new_album_button.setText(R.string.facebook_new_album);
        album_editor.setHint(R.string.facebook_new_album_hint);
        new_album_button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v) 
            {
               if(albumSpinner.getVisibility() != View.GONE)
               {
                  albumSpinner.setVisibility(View.GONE);
                  album_editor.setVisibility(View.VISIBLE);
                  //facebook_new_album
                  new_album_button.setText(R.string.close_about);
               }
               else
               {
                   albumSpinner.setVisibility(View.VISIBLE);
                   album_editor.setVisibility(View.GONE); 
                   new_album_button.setText(R.string.facebook_new_album);
               }               
            }            
        });
        
        watcher = new MyWatcher(); 	    
        contentEdit.addTextChangedListener(watcher);   
        
        facebook_share_button = (Button)this.findViewById(R.id.facebook_share_button);
        facebook_share_button.setOnClickListener(shareClick);
        
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);	
	        	
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }     
        
        this.setTitle(R.string.facebook_photo_upload);        
        handlerIntent();
    }
    

	View.OnClickListener shareClick = new View.OnClickListener() 
	{		
		public void onClick(View v) 
		{
			//send message to my wall
			if(isInProcess() == false)
			{
				shareContent();
			}			
		}
	};	
    
    private void handlerIntent()
    {
    	  Intent intent = this.getIntent();         
          if(intent!=null && intent.getAction().equals("oms.sns.intent.action.upload"))
          {
     		  Uri mediaUri = (Uri)intent.getData();
     		  mediatype = intent.getType();
     		  if(mediaUri!=null)
     	      {			
 	    		  Cursor cursor = this.getContentResolver().query(mediaUri, null, null, null,null);	    		
 	    	      if (cursor != null && cursor.moveToFirst()) 
 	              {
 		    		  mSrc = cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DATA));
 		              Log.d(TAG,"file path is "+mSrc);     
 		              cursor.close();
 		          }    		  		
     	      }    	
     		  showMediaContent(mediatype);
     	  }
          else if(intent!=null && intent.getAction().equals(Intent.ACTION_SEND))
          {
         	  Log.d(TAG, "input intent ="+intent);
              
              String contentpath = "";
              String mime        = "";
 	          Uri mMessageUri = (Uri)intent.getData();
 	         
              mime = intent.getType();
              mediatype = mime;
              if(mMessageUri != null)
              {
            	  Log.d(TAG, "what is data="+mMessageUri);
                  String[] cols = new String[] {MediaStore.Images.Media.DATA,};
                  Cursor mCursor = this.getContentResolver().query(mMessageUri, cols, null , null, null); 
                  if(mCursor != null && mCursor.moveToFirst())
                  {  
                      contentpath = mCursor.getString(0);
                      Log.d(TAG, "what is data 1="+contentpath);
                      mCursor.close();
                  }
                  else
                  {
                	  contentpath = mMessageUri.getPath();
                	  Log.d(TAG, "what is data 2="+contentpath);
                  }
              }
              
              Bundle extrbudle = intent.getExtras();
              if(extrbudle != null)
              {
            	  Uri path = (Uri)extrbudle.get(Intent.EXTRA_STREAM);
            	  if(path == null)
            	  {
            		  String pathurl = (String)extrbudle.get(Intent.EXTRA_TEXT);
            		  Log.d(TAG, "what is data 20="+pathurl);
            		  if(pathurl != null)
            		  {
            			  path = Uri.parse(pathurl);
            		  }            		  
            	  }
            	  if(path != null)
            	  {
	            	  Log.d(TAG, "what is data 21="+path);
	                  String[] cols = new String[] {MediaStore.Images.Media.DATA,};
	                  Cursor mCursor = this.getContentResolver().query(path, cols, null , null, null); 
	                  if(mCursor != null && mCursor.moveToFirst())
	                  {  
	                      contentpath = mCursor.getString(0);
	                      Log.d(TAG, "what is data 212="+contentpath);
	                      mCursor.close();
	                  }
	                  else//for link
	                  {
	                	  contentpath = path.toString();
	                  }
            	  }
              }
 	          
 	          
 	          if(contentpath.length() > 0)
 	          {
 		          FacebookStatusItem newItem = new FacebookStatusItem();
 		          if(mime.indexOf("image")>=0)
 				      newItem.type = FacebookStatusItem.ContentType.IMAGE;
 		          
 		          if(mime.indexOf("video")>=0)
 				      newItem.type = FacebookStatusItem.ContentType.VIDEO;
 		          
 		          if(mime.indexOf("text")>=0 || mime.indexOf("http://")>=0 || mime.indexOf("https://")>=0)
 		          {
 		              newItem.type = FacebookStatusItem.ContentType.LINK; 				      
 		          }
 		          
 				  newItem.url  = contentpath;
 				  newItem.name = contentpath;
 				  currentStatuses.add(newItem);
 				  mSrc = contentpath;
 				  updateUI();
 				 if(newItem.type == FacebookStatusItem.ContentType.IMAGE)
                 {
 				    handler.obtainMessage(SET_SPINNER_UI).sendToTarget();
                 }
 				  
 	          }
          }         
    }
    
    public void setTitle()
    {
    	title = this.getString(R.string.facebook_photo_upload);
    }
    
    @Override
    protected void doAfterLoginNothing() {
        this.finish();
        super.doAfterLoginNothing();
        
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
    		launchFacebookLogin();
    	}
    	else
    	{
    	    if(mediatype.indexOf("image")>=0)
            {
               handler.obtainMessage(SET_SPINNER_UI).sendToTarget();
            }
    		facebookA = new AsyncFacebook(perm_session);
    		perm_session.attachActivity(this);
            
    	}
    }
    
    
    public void showDialog(){
    	AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle(R.string.facebook_upload_img_title)
        .setMessage(R.string.facebook_upload_img_prompt_msg)
        .setPositiveButton(getString(R.string.sns_ok), new DialogInterface.OnClickListener() 
        {
           public void onClick(DialogInterface dialog, int whichButton) 
           {	     
               //if is image && create Album
               String albumname = album_editor.getText().toString();
               if(album_editor.getVisibility()== View.VISIBLE && !isEmpty(albumname))
               {
                   //firstCreateAlbum
                   Message message = handler.obtainMessage(CREATE_ALBUM_AND_UPLOAD);
                   message.getData().putString("name", albumname);
                   message.sendToTarget();
               }
               else
               {
           			Message message = handler.obtainMessage(UPLOAD_MEDIA);
           			message.getData().putString("mediapath",mSrc);
           			message.sendToTarget();
               }
   			
           }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
        {
           public void onClick(DialogInterface dialog, int whichButton) 
           {
           	 //show image 
               showMediaContent(mediatype);
           }
        })
        .create();
        dialog.show();		
    }
    
    public void showMediaContent(String type)
    {
    	if(mSrc.length()>0)
		{
			FacebookStatusItem newItem = new FacebookStatusItem();
			newItem.type = mediatype.indexOf("image")>=0?FacebookStatusItem.ContentType.IMAGE:FacebookStatusItem.ContentType.VIDEO;
			newItem.url  = mSrc;
			newItem.name = mSrc;
			currentStatuses.add(newItem);
			updateUI();
			
			if(newItem.type == FacebookStatusItem.ContentType.IMAGE)
            {
               handler.obtainMessage(SET_SPINNER_UI).sendToTarget();
            }
		}
    }
    
    public void removeFromUI(long id) 
	{
    	Log.d(TAG, "entering removeFromUI");
		boolean changed=false;
		for(int i=0;i<currentStatuses.size();i++)
		{
			FacebookStatusItem item = currentStatuses.get(i);
			if(item.id == id)
			{
				currentStatuses.remove(i);
				
				if(item.type == FacebookStatusItem.ContentType.IMAGE || item.type == FacebookStatusItem.ContentType.CAMERA)
				{
				    albumSpan.setVisibility(View.GONE);
				}
				changed=true;
				break;
			}
		}
		if(changed == true)
		{
			updateUI();
		}
	}
    
    private void updateUI()
	{
    	Log.d(TAG, "entering updateUI");
		if(currentStatuses != null && currentStatuses.size()>0)
		{
			FacebookStatusContentAdapter fa = new FacebookStatusContentAdapter(MediaUploadActivity.this, currentStatuses);
			View v = contentList.getChildAt(0);
	    	if(FacebookStatusContentItemView.class.isInstance(v))
	    	{
	    	    FacebookStatusContentItemView view = (FacebookStatusContentItemView)v;	    	    
	    	    view.hiddenRemoveBotton();
	    	}
			contentList.setAdapter(fa);
		}
		else
		{
			contentList.setAdapter(null);
		}
	}
    
    void postLink(String linkpath,String comment)
    {
        
        Log.d(TAG, "entering post link"+linkpath +" comment is "+comment);
        begin();
        
        synchronized(mLock)
        {
            inprocess = true;
        }
        
        facebookA.postLinkAsync(-1, linkpath,comment,new FacebookAdapter()
        {
            @Override public void postlink(final long ssid, long linkid)
            {
                //Log.d(TAG, "post to wall="+suc);
                synchronized(mLock)
                {
                    inprocess = false;
                }
               
                Message rmsg = handler.obtainMessage(UPLOAD_MEDIA_END);
                rmsg.getData().putBoolean(RESULT, true);
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
                    Message rmsg = handler.obtainMessage(UPLOAD_MEDIA_END);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.getData().putString("errormessage", e.getMessage());
                    rmsg.sendToTarget();
                }
            }
        });

    }
    
    private void shareImgLink(String mediapath,String text)
    {
        Log.d(TAG,"entering shareImgLink ");
        
       /* if(photo != null)
        {  
            //only for facebook photo share
            Attachment attachment = new Attachment();
            AttachmentMediaImage imageMedia =  attachment.new AttachmentMediaImage();
            imageMedia.addImage(photo.src_small,mediapath);
            
            attachment.media = imageMedia;            
            publishStream(text, attachment,null,perm_session.getLogerInUserID());
        } 
        else
        {*/
            //for link share
            postLink(mediapath,text);
        //}
      
    }
    
    private void publishStream(String content,Attachment attachment,List<BundleActionLink> actionlinks,Long targetid)
    {
        begin();
        
        synchronized(mLock)
        {
            inprocess = true;
        }      
        facebookA.publishStreamAsync(content, attachment, actionlinks, targetid, new FacebookAdapter()
        {
            @Override public void publishStream(long postid)
            {
                Log.d(TAG, "publish stream="+postid);
                synchronized(mLock)
                {
                    inprocess = false;
                }    
                
                Message rmsg = handler.obtainMessage(UPLOAD_MEDIA_END);
                rmsg.getData().putBoolean(RESULT, true);
                handler.sendMessage(rmsg);
                
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                Log.d(TAG, "publish stream ex="+e.getMessage());
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                    Message rmsg = handler.obtainMessage(UPLOAD_MEDIA_END);
                    rmsg.getData().putBoolean(RESULT, false);
                    handler.sendMessage(rmsg);
                }
            }
        });
    }
    
    private void shareContent()
    {
    	if(currentStatuses!=null && currentStatuses.size()<=0)
		{
		    return;
		}
		
		//send message to my wall
		if(perm_session == null)
		{
		    launchFacebookLogin();
		}
		else if(this.isInProcess() == false )
		{   
		    showDialog(DLG_UPLOAD_MEDIA);
		    facebook_share_button.setEnabled(false);
		    if(albumSpan.getVisibility() == View.VISIBLE)
		    {   
		        String albumname = album_editor.getText().toString();
		        if(album_editor.getVisibility() == View.VISIBLE && !isEmpty(albumname))
		        {
		            Message message = handler.obtainMessage(CREATE_ALBUM_AND_UPLOAD);
	                message.getData().putString("name", albumname);
	                String content = contentEdit.getText().toString().trim();
                    if(content != null && content.length() >0)
                    {
                        message.getData().putString("text",content);
                    } 
	                message.sendToTarget();
		        }
		        else
		        {
		            Message message = handler.obtainMessage(UPLOAD_MEDIA);
		            message.getData().putString("mediapath",mSrc);
		            String content = contentEdit.getText().toString().trim();
		            if(content != null && content.length() >0)
		            {
		                message.getData().putString("text",content);
		            } 
		            Object selectedObj = albumSpinner.getSelectedItem();
                    String aid = null;
                    if(selectedObj!=null && PhotoAlbum.class.isInstance(selectedObj))
                    {
                        aid = ((PhotoAlbum)selectedObj).aid;
                    }
	                message.getData().putString("aid", aid);
	                message.sendToTarget();
		        }
		    }
		    else
		    {
		        Message message = handler.obtainMessage(UPLOAD_MEDIA);
                message.getData().putString("mediapath",mSrc);
                String content = contentEdit.getText().toString().trim();
                if(content != null && content.length() >0)
                {
                    message.getData().putString("text",content);
                }              
                message.sendToTarget();
		    }
		   
		}
    }

    /*
    @Override
	public void titleSelected() 
    {		
		super.titleSelected();
		
		shareContent();
	}*/
    
  
	@Override
	protected void createHandler() {
		handler = new UploadHandler();
	}
	
    final int UPLOAD_MEDIA             = 0;
    final int UPLOAD_MEDIA_END         = 1;
    final int FACEBOOK_ALBUM_GET       =2;
    final int CREATE_ALBUM_AND_UPLOAD  =3;
    final int SET_SPINNER_UI            =4;
	
	private class UploadHandler extends Handler 
    {
        public UploadHandler()
        {
            super();            
            Log.d(TAG, "new UploadHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case UPLOAD_MEDIA :
                {
                	String mediapath = msg.getData().getString("mediapath");
                	String text  = msg.getData().getString("text");
                	String aid = msg.getData().getString("aid");   
                	
                	Log.d(TAG, "media path is "+mediapath+" text is "+text+"aid is "+aid +"photo is ");
                	 if(mediapath.startsWith("file://") == true)
                     {
                         mediapath = mediapath.substring(7);
                     }
                	 Log.d(TAG, "media path is "+mediapath);
                	if(mediatype.indexOf("image")>=0)
                	{
                	    uploadImg(0,mediapath,text,aid);
                	}
                	else if(mediatype.indexOf("video")>=0)
                	{
                	    uploadVideo(0,mediapath,text);
                	}
                	else if(mediatype.indexOf("text")>=0 || mediatype.indexOf("http://")>=0 || mediatype.indexOf("https://")>=0)
                	{
                	    Log.d(TAG, "process share link");
                	    shareImgLink(mediapath,text);
                	}
                	else
                	{
                	    facebook_share_button.setEnabled(true);
                	    dismissDialog(DLG_UPLOAD_MEDIA);
                	    end();
                	}
                	break;
                }
                case UPLOAD_MEDIA_END:
                {
                    dismissDialog(DLG_UPLOAD_MEDIA);
                    facebook_share_button.setEnabled(true);
                	end();             	
                	if(msg.getData().getBoolean(RESULT) == true)
            		{            	   
            		   contentEdit.setText(""); 
            		   Toast.makeText(MediaUploadActivity.this, R.string.sns_share_success, Toast.LENGTH_SHORT).show();
                       finish();
            		}
                	else
            		{
            		    //TODO failed process
            		    String errormessage = msg.getData().getString("errormessage");
            		    if(errormessage==null || errormessage.length()==0)
            		    {
            		       errormessage = getString(R.string.sns_share_failed);   
            		    }
            		    Toast.makeText(MediaUploadActivity.this, errormessage, Toast.LENGTH_SHORT).show();
            		    
            		}
                	
            		break;
                }
                case FACEBOOK_ALBUM_GET:
                {
                    Log.d(TAG,"get facebook album ");
                    getFacebookAlbumAsync();
                    break;
                }
                case CREATE_ALBUM_AND_UPLOAD:
                {
                    Log.d(TAG, "create facebook album ");
                    String name = msg.getData().getString("name");
                    String content = msg.getData().getString("content");
                    createFacebookAlbum(name,content);
                    break;
                }
                case SET_SPINNER_UI:
                {   
                    Log.d(TAG,"set Spinner UI");
                    albumSpan.setVisibility(View.VISIBLE);
                    if(perm_session == null)
                    {
                        return ;
                    }
                    List<PhotoAlbum> albums = orm.getAlbum(perm_session.getLogerInUserID());
                    if(albums != null && albums.size() > 0)
                    {
                        FacebookAlbumSpinnerAdapter adapter = new FacebookAlbumSpinnerAdapter(mContext,android.R.layout.simple_spinner_item,albums);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        albumSpinner.setAdapter(adapter);
                   
                        boolean aftercreate = msg.getData().getBoolean("aftercreate");
                        if(aftercreate == true)
                        {
                            String aid= msg.getData().getString("newalbumid");
                            int pos = adapter.getPos(aid);
                            albumSpinner.setSelection(pos);                            
                        }                        
                    }
                    break;         
                }
            }
        }
    }
	
	private void createFacebookAlbum(final String name,final String content)
	{
        begin();
        
        synchronized(mLock)
        {
            inprocess = true;
        }
        
	    SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(mContext, account))
        {
            
            perm_session = null;
            perm_session = loginHelper.getPermanentSesstion();
            
            if(perm_session != null)
            {
                if(facebookA == null)
                {
                    facebookA = new AsyncFacebook(perm_session);
                }               
                facebookA.setSession(perm_session);
                
                facebookA.createAlbumAsync(name,new FacebookAdapter()
                {
                    @Override public void createPhotoAlbum(PhotoAlbum photoalbum)
                    {
                        synchronized(mLock)
                        {
                            inprocess = false;
                        }  
                        orm.addAlbum(photoalbum);
                        handler.post(new Runnable()
                        {
                            public void run()
                            {
                                album_editor.setText("");
                                swithtoSpinner();
                            }
                        });
                        Message msd = handler.obtainMessage(SET_SPINNER_UI);
                        msd.getData().putString("newalbumid", photoalbum.aid);
                        msd.getData().putBoolean("aftercreate", true);
                        msd.sendToTarget();
                        
                        
                        Message message = handler.obtainMessage(UPLOAD_MEDIA);
                        message.getData().putString("mediapath",mSrc);
                        if(content != null && content.length() >0)
                        {
                            message.getData().putString("text",content);
                        }
                        message.getData().putString("aid", photoalbum.aid);
                        message.sendToTarget();
                    }                  
                    @Override public void onException(FacebookException e, int method) 
                    {
                        Log.d(TAG, "fail to get album information exception "+e.getMessage());
                        
                        synchronized(mLock)
                        {
                            inprocess = false;
                        }
                        Message message = handler.obtainMessage(UPLOAD_MEDIA);
                        message.getData().putString("mediapath",mSrc);
                        message.sendToTarget();
                    }
                });              
            }
        }
	}
	
	private void swithtoSpinner()
    {
        if(albumSpinner.getVisibility() != View.GONE)
        {
           albumSpinner.setVisibility(View.GONE);
           album_editor.setVisibility(View.VISIBLE);
           //facebook_new_album
           new_album_button.setText(R.string.close_about);
        }
        else
        {
            albumSpinner.setVisibility(View.VISIBLE);
            album_editor.setVisibility(View.GONE); 
            new_album_button.setText(R.string.facebook_new_album);
        }               
    }
	
	private void getFacebookAlbumAsync()
    {
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(mContext, account))
        {
            
            perm_session = null;
            perm_session = loginHelper.getPermanentSesstion();
            
            if(perm_session != null)
            {
                if(facebookA == null)
                {
                    facebookA = new AsyncFacebook(perm_session);
                }               
                facebookA.setSession(perm_session);
                
                facebookA.batch_run_getFacebookAlbumAsync(perm_session.getLogerInUserID(),new FacebookAdapter()
                {
                    @Override public void getFacebookAlbum(List<PhotoAlbum> photoalbums)
                    {
                        orm.addAlbum(photoalbums);
                        //retset Spinner TODO
                        handler.obtainMessage(SET_SPINNER_UI).sendToTarget();
                    }
                    
                    @Override public void onException(FacebookException e, int method) 
                    {
                        Log.d(TAG, "fail to get album information exception "+e.getMessage());
                    }
                });              
            }
        }
    }
	
	
	
	void uploadVideo(long sid, String filepath,String text)
    {
        
        Log.d(TAG, "entering upload video");
        begin();
        
        synchronized(mLock)
        {
            inprocess = true;
        }
        
        
        facebookA.uploadVideoAsync(sid, filepath,text,new FacebookAdapter()
        {
            @Override public void uploadvideo(long ssid, Video video)
            {
                //Log.d(TAG, "post to wall="+suc);
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                if(donotcallnetwork == false)//I am still alive
                {                           
                    //cancelNotify();
                }
                Message rmsg = handler.obtainMessage(UPLOAD_MEDIA_END);
                
                if(video != null)
                {
                    rmsg.getData().putBoolean(RESULT, true);
                }
                else
                {
                    rmsg.getData().putBoolean(RESULT, false);
                }
               
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
	                Message rmsg = handler.obtainMessage(UPLOAD_MEDIA_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.getData().putString("errormessage", e.getMessage());
	                rmsg.sendToTarget();
            	}
            }
        });

    }
	
	void uploadImg(long sid, String filepath,String caption,String aid)
	{
		
		Log.d(TAG, "entering upload Imag");
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
		facebookA.uploadImageAsync(sid, filepath,caption,aid,new FacebookAdapter()
    	{
    		@Override public void uploadimage(long ssid, Photo photo)
            {
    			//Log.d(TAG, "post to wall="+suc);
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}

                if(donotcallnetwork == false)//I am still alive
                {							
	            	//cancelNotify();
                }      
                
                Message rmsg = handler.obtainMessage(UPLOAD_MEDIA_END);
                rmsg.getData().putBoolean(RESULT, true);
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
	            	Message rmsg = handler.obtainMessage(UPLOAD_MEDIA_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.sendToTarget();
             	}
            }
    	});

	}
	
    
	private class MyWatcher implements TextWatcher 
	{   
       public void afterTextChanged(Editable s) 
       {
    	   textCount.setText(String.format("%1$s", s.length()));
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }
	
	public void registerAccountListener() {
		AccountManager.registerAccountListener("MediaUploadActivity", this);		
	}
	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("MediaUploadActivity");		
	}
	
}
