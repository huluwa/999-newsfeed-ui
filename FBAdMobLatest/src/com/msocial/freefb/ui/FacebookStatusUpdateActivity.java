package com.msocial.freefb.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.msocial.freefb.*;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.providers.SocialORM.FacebookUsersCol;
import com.msocial.freefb.ui.AccountListener.AccountManager;
import com.msocial.freefb.ui.FacebookMailActivity.FBUDecorater;
import com.msocial.freefb.ui.adapter.FacebookAlbumSpinnerAdapter;
import com.msocial.freefb.ui.adapter.FacebookStatusContentAdapter;
import com.msocial.freefb.ui.adapter.FacebookStatusItem;
import com.msocial.freefb.ui.adapter.RecipientsAdapter;
import com.msocial.freefb.ui.view.FacebookStatusContentItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.Attachment;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Photo;
import oms.sns.service.facebook.model.PhotoAlbum;
import oms.sns.service.facebook.model.Video;
import oms.sns.service.facebook.model.Attachment.AttachmentMedia;
import oms.sns.service.facebook.model.Attachment.BundleActionLink;
import oms.sns.service.facebook.model.Attachment.AttachmentMediaImage;
import oms.sns.service.facebook.model.Attachment.AttachmentMediaVideo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.text.Annotation;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import com.msocial.freefb.AddressPad;
import com.msocial.freefb.AddressPad.AddressDecorator;


public class FacebookStatusUpdateActivity extends FacebookBaseActivity implements View.OnClickListener{
    private final static String TAG = "FacebookStatusUpdateActivity";
	private EditText contentEdit;
	private MyWatcher watcher;
	private TextView textCount;
	private View     upload_span;
	private Button   upload_button;
	private AddressPad mRecipientsEditor;
	private Button   receiversel_button;
	    
    private static String tmpPath=TwitterHelper.getTmpPath();	
    ListView      contentList;
    private List<FacebookStatusItem> currentStatuses = new ArrayList<FacebookStatusItem>();   
    private boolean stream_publish;
    private boolean post_wall;   
    
    private String contact_name;
    private long fuid;
    private String albumid;
	private List<Long> currentSelected = new ArrayList<Long>();
	
    String  mExternalAddress;
	private Button mToButton;
	
	private View     albumSpan;
    private Spinner  albumSpinner;
    private EditText album_editor;
    private Button   new_album_button;
    private Button facebook_share_button;
    
    private int image_num = 0;
	
	private boolean contentchanged = false; //contentEdit changed || updateUI() ||  addressPad changed
	private int itemNumShouldBeProcessed = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //post to wall
        fuid = (Long)this.getIntent().getLongExtra("fuid", -1);        
        Log.d(TAG,"fuid is "+fuid);
        if(fuid != -1)
        {
        	setContentView(R.layout.facebook_post_wall);
        	
        	post_wall = true;
	        contact_name = String.valueOf(this.getIntent().getStringExtra("contact_name"));	
	        mExternalAddress = String.valueOf(fuid);
	        initWallView(contact_name, fuid);
	        setTitle(R.string.facebook_wall_share);
        }
        else
        {
        	setContentView(R.layout.facebook_status_update);
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
                    swithtoSpinner();
                }            
            });

        }
        
        contentList = (ListView)this.findViewById(R.id.facebook_update_content_list);        
        contentEdit = (EditText)this.findViewById(R.id.facebook_status_message_editor);
        contentEdit.setHint(R.string.facebook_status_update_compose);      
        contentEdit.setVerticalScrollBarEnabled(true);
        textCount = (TextView)this.findViewById(R.id.facebook_status_text_counter);
        
        facebook_share_button = (Button)this.findViewById(R.id.facebook_share_button);
        facebook_share_button.setOnClickListener(shareClick);
        
        //load draft
        if(post_wall == true)
        {
        	loadDraft(fuid);
        }
        
        watcher = new MyWatcher(); 	    
        contentEdit.addTextChangedListener(watcher);        
        setTitle(R.string.facebook_wall_share);
        
        //for none-post wall
        if(post_wall == false)
        {
            stream_publish = this.getIntent().getBooleanExtra("stream_publish", false);
            if(stream_publish)
            {
            	setTitle(R.string.facebook_wall_share);
            	upload_span = (View)this.findViewById(R.id.facebook_upload_span);
            	upload_button = (Button)this.findViewById(R.id.media_upload);
            	upload_button.setVisibility(View.GONE);
            	//upload_button.setOnClickListener(mediaUploadClick);
            	//upload_button.setText(R.string.facebook_media_upload);
            }
            
            albumid = this.getIntent().getStringExtra("aid");
            if(isEmpty(albumid) == false)
            {
                //show album
                if(albumSpan.getVisibility() == View.GONE)
                {
                    albumSpan.setVisibility(View.VISIBLE);  
                    Message msg = handler.obtainMessage(SET_SPINNER_UI);
                    msg.getData().putBoolean("addphototospecifiedalbum", true);
                    msg.getData().putString("albumid",albumid);
                    msg.sendToTarget();
                }
            }
        }
        
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);	
	        	if(post_wall == false)
	        	{
	        	    handler.obtainMessage(FACEBOOK_ALBUM_GET).sendToTarget();
	        	}
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }  
        
        //need more to set
        setTitle();
    }
	
	View.OnClickListener shareClick = new View.OnClickListener() 
	{		
		public void onClick(View v) {
			//send message to my wall
			if(isInProcess() == false)
			{  
			    handler.obtainMessage(UPDATE_STATUS).sendToTarget();
			}			
		}
	};	
	
	OnItemSelectedListener spannerListener = new OnItemSelectedListener(){

        public void onItemSelected(AdapterView<?> adapterview, View view, int arg2,long arg3) {
            Log.d(TAG,"entering selecte album item");
            Object selectedObj = albumSpinner.getSelectedItem();
            String aid = null;
            if(selectedObj!=null && PhotoAlbum.class.isInstance(selectedObj))
            {
                albumid = ((PhotoAlbum)selectedObj).aid;
            }
        }

        public void onNothingSelected(AdapterView<?> adapterview) {
            
        }
	    
	};
	
	public void setTitle()
    {
    	title = this.getString(R.string.facebook_wall_share);
    	/*if(stream_publish)
        {
    	   title = getString(R.string.facebook_wall_share);
        }
    	
    	if(fuid != -1)
    		title = getString(R.string.facebook_walltowall_title);*/
    }
	
	public void initWallView(String name, long uid)
	{		
	    ((View)findViewById(R.id.facebook_wall_receiver_span)).setVisibility(View.VISIBLE);
	    mRecipientsEditor = (AddressPad)findViewById(R.id.facebook_wall_receiver_editor);
        mRecipientsEditor.setAdapter(new RecipientsAdapter(this));
        mRecipientsEditor.setAddressDecorator(new FBUDecorater());
        
        if(mExternalAddress!=null){
            mRecipientsEditor.setAddresses(mExternalAddress, ",");
            mExternalAddress = null;
        }
        mToButton = (Button)findViewById((R.id.to_button));
        mToButton.setOnClickListener(this);
        mToButton.setVisibility(View.VISIBLE);        	
	}
	
	
	public void onClick(View v) 
	{
		Intent intent = new Intent(FacebookStatusUpdateActivity.this, FacebookUserSelectActivity.class);            
 		//startActivityForResult(intent, FACEBOOK_USER_SELECT);
        startActivityIfNeeded(intent, FACEBOOK_USER_SELECT);
    }
	
	@Override public boolean onPrepareOptionsMenu(Menu menu)
    {    	
		super.onPrepareOptionsMenu(menu);
		if(fuid!=-1){
			menu.findItem(R.id.facebook_menu_save_to_draft).setVisible(true);
		}
		return true;
    }
	
	@Override public boolean onOptionsItemSelected(MenuItem item) 
    {
       super.onOptionsItemSelected(item);
       switch (item.getItemId()) 
       {
	        case R.id.facebook_menu_save_to_draft:
	        {
	        	saveToDraft();
	        	break;
	        }
       }      
       return true;
       
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		  switch(requestCode)
	      {  
	            case FACEBOOK_USER_SELECT:
	            {
	            	if(resultCode == 100)
	            	{
	            		Log.d(TAG, "user select");
	            		long[] uids = intent.getLongArrayExtra("uids");
	            		if(uids == null || uids.length ==0)
	            		{
	            			Log.d(TAG, "no selected user");
	            		}
	            		else
	            		{
	            			contentchanged = true;
	            			currentSelected.clear();
	            			String numbers="";
	            			boolean isExist;
	            			for(int i=0;i<uids.length;i++)
	            			{
	            				//check whether exist
	            				isExist = false;
	            				for(int j=0;i<currentSelected.size();j++)
	            				{
	            					if(currentSelected.get(j) == uids[i])
	            					{
	            						isExist=true;
	            						break;
	            					}
	            				}
	            				if(isExist == false)
	            				{
		            				currentSelected.add(uids[i]);
		            				numbers +=String.valueOf(uids[i]) + ",";
	            				}
	            			}
	            			
	            			String originalnum="";
        				    if(mRecipientsEditor!=null){
        		                originalnum = mRecipientsEditor.getAddresses(",");
        		            } 
        		            
        		            if("".equals(originalnum)){
        		                mRecipientsEditor.setAddresses(numbers, ",");
        		            }else{
        		                mRecipientsEditor.setAddresses(originalnum+","+numbers, ",");
        		            }
        		            
	            		}
	            		//no need more
	            		uids = null;
	            	}
	            	break;
	           }
	           default:
	        	   super.onActivityResult(requestCode, resultCode, intent);
	               break;
	      }
	}
	
	public void loadDraft(long fuid)
	{
	   List<FacebookStatusItem> items = orm.getWallDraftByFuid(fuid);
	   if(items.size()>0){
		   for(FacebookStatusItem item : items){
			   if(item.type == FacebookStatusItem.ContentType.NONE){
				  this.contentEdit.setText(item.url);
			   }else{
				   currentStatuses.add(item); 
			   }
	
		   }
	   }
	   
	   updateUI();
	}
	
	
	public void saveToDraft(){
		String[] address = mRecipientsEditor.getAddresses();
		String message = contentEdit.getText().toString();
		
		if(address!=null && address.length>0){
			for(int i = 0 ; i <address.length ; i++){
				orm.saveWallAsDraft(message, currentStatuses, new Long(address[i].trim()));
			}
		}
		
		Toast.makeText(FacebookStatusUpdateActivity.this, R.string.facebook_save_wall_message, Toast.LENGTH_LONG).show();
	}
	
	View.OnClickListener mediaUploadClick = new View.OnClickListener()
	{
		//upload the media
		public void onClick(View arg0) 
		{			
			//upload the content first, and get the url link for publish stream
			handler.obtainMessage(UPDATE_STATUS).sendToTarget();
		}		
	};
	//remove the click item and update UI
	public void removeFromUI(long id) 
	{
		boolean changed=false;
		for(int i=0;i<currentStatuses.size();i++)
		{
			FacebookStatusItem item = currentStatuses.get(i);
			Log.d(TAG, "remove check pre item id is "+item.id);
			if(item.id == id)
			{
				currentStatuses.remove(i);
				
				if(item.type == FacebookStatusItem.ContentType.CAMERA)               
                {
                    File file = new File(item.url);
                    if(file.exists() == true && file.getPath().indexOf(tmpPath)>=0)
                    {
                        file.delete();
                    }
                    
                    image_num--;
                }
				else if(item.type == FacebookStatusItem.ContentType.IMAGE)
				{
				    image_num--;
				}
				
				if(image_num<=0)
				{
				    albumSpan.setVisibility(View.GONE);
				}
				
				changed=true;
				break;
			}
		}
		
		if(currentStatuses.size() == 0 && upload_span != null)
		{
			upload_span.setVisibility(View.GONE);
		}
		
		if(changed == true)
		{
			updateUI();
		}
	}
	
	private void updateUI()
	{
		contentchanged = true;
		if(contentList != null)
		{
			if(currentStatuses != null && currentStatuses.size() > 0)
			{
			    contentList.setAdapter(null);
		        FacebookStatusContentAdapter fa = new FacebookStatusContentAdapter(FacebookStatusUpdateActivity.this, currentStatuses);
	            contentList.setAdapter(fa);
			}
			else
			{
				contentList.setAdapter(null);
			}
		}
	}
	
	@Override
    protected void doAfterLogin()
    {
    	Log.d(TAG, "after login");
    	//try to get the session
    	perm_session = loginHelper.getPermanentSesstion();
    	if(perm_session == null)
    	{
    		//re-launch the login UI
    		launchFacebookLogin();
    	}
    	else
    	{
    		facebookA = new AsyncFacebook(perm_session);
    		perm_session.attachActivity(this);
    	}
    }
	
	@Override
	protected void  showImg(Intent intent,int requestcode)
	{
		Log.d(TAG, "entering image");
		 image_num++;
		//showAlbum
		 if(albumSpan.getVisibility() == View.GONE)
		 {
		     albumSpan.setVisibility(View.VISIBLE);       
	         handler.obtainMessage(SET_SPINNER_UI).sendToTarget();
		 }
		
		
		FacebookStatusItem.ContentType TYPE = FacebookStatusItem.ContentType.NONE;
		String mSrc = "";
		if(requestcode == STATUS_INSERT_IMG)
		{		
			TYPE = FacebookStatusItem.ContentType.IMAGE;
    	    Uri imgUri = intent.getData();   	 
    	    if(imgUri!=null)
    	    {			
	    		Cursor cursor = this.getContentResolver().query(imgUri, null, null, null,null);
	    		
	    		 if (cursor != null ) {
	    			 cursor.moveToFirst();
	    			 mSrc = cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DATA));
	                 Log.d(TAG,"file path is "+mSrc);     
	                 cursor.close();
	            }    		  		
    	     }    	
    	}
		else
		{
	   		 Log.d(TAG, "capture photo");   	
	   		 TYPE = FacebookStatusItem.ContentType.CAMERA;
	   		 if(orm.isFacebookUseOriginalPhoto())
             {
                 Log.d(TAG,"progress original photo from camera");
                 Uri imgUri = intent.getData();        
                 if(imgUri!=null)
                 {           
                     Cursor cursor = this.getContentResolver().query(imgUri, null, null, null,null);                    
                     if (cursor != null && cursor.moveToFirst()) 
                     {   
                         mSrc = cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DATA));
                         Log.d(TAG,"file path is "+mSrc);     
                         cursor.close();
                     }                   
                 }      
             }
	   		 else
	   		 {
    			 Bitmap bitmap = (Bitmap) intent.getParcelableExtra("data");	 
    			 if(bitmap!=null)
    			 {
    			     String filename = String.valueOf(new Date().getTime());
    				 String filepath = tmpPath + filename+".jpg"; 					
    				 if(new File(filepath).exists() == false)
    				 {
    				     FileOutputStream out = null;				 
    					 try{
    						new File(filepath).createNewFile();			    
    					    out = new FileOutputStream(new File(filepath));
    					    bitmap.compress(CompressFormat.JPEG,100,out);
    					    out.close();
    					    mSrc = filepath;
    					 }catch(IOException ne)
    					 {
    						Log.d(TAG, "store capture pic failed "+ne);
    					 }
    				 }
    			 }
			 }
        }
		
		if(mSrc.length()>0)
		{
			if(this.stream_publish)upload_span.setVisibility(View.VISIBLE);
				
			FacebookStatusItem newItem = new FacebookStatusItem();
			newItem.type = TYPE;
			newItem.url  = mSrc;
			newItem.name = getNameFromPath(mSrc);
			
			if(addNewItem(newItem) )
			{
			    updateUI();
			}
		}
    }
	
	//TODO get the content name
	private String getNameFromPath(String src) 
	{
		return src;
	}

	@Override
	protected void showVideo(Intent intent)
	{	
    	 Uri imgUri = intent.getData();
    	 String mSrc = "";
         if(imgUri!=null)
         {			
    		Cursor cursor = this.getContentResolver().query(imgUri, null, null, null,null);  		
    		 if (cursor != null && cursor.getCount()>0) 
    		 {
    			 cursor.moveToFirst();
    			 mSrc = cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DATA));
                 Log.d(TAG,"file path is "+mSrc);     
                 cursor.close();
            }
         }
         if(mSrc.length()>0)
 		 {
        	if(this.stream_publish)upload_span.setVisibility(View.VISIBLE);
 			FacebookStatusItem newItem = new FacebookStatusItem();
 			newItem.type = FacebookStatusItem.ContentType.VIDEO;
 			newItem.url  = mSrc;
 			newItem.name = getNameFromPath(mSrc);
 			
 			if(addNewItem(newItem) )
			{
			    updateUI();
			}
 		 }
	}
	
	//after all attachment were uploaded, get the video url list,image url list, link list and publish stream
	
    protected void uploadedSID(long ssid,Object obj) 
    {
        for(int i=0;i<currentStatuses.size();i++)
        {
            FacebookStatusItem item = currentStatuses.get(i);
            if(item.id == ssid)
            {       
                item.uploadStatus = FacebookStatusItem.Status.SUC_UPLOAD;
                if(obj!=null)
                {
                    item.obj = obj;                   
                }
                
                if(item.type == FacebookStatusItem.ContentType.CAMERA)               
                {
                    File file = new File(item.url);
                    if(file.getPath().indexOf(tmpPath)>=0)
                    {
                        file.delete();
                    }
                }           
            }
        }
        
        for(int i=0;i<contentList.getChildCount();i++)
        {
            View v = contentList.getChildAt(i);
            if(FacebookStatusContentItemView.class.isInstance(v))
            {
                FacebookStatusContentItemView view = (FacebookStatusContentItemView)v;
                FacebookStatusItem item  = view.getStatusContent();
                if(item.id == ssid)
                {
                    view.setUploadedBackGround(true);                 
                    break;
                }
            }
        }
        contentList.requestLayout();        
    }
	
	boolean addNewItem(FacebookStatusItem newItem)
	{
		for(int i=0;i<currentStatuses.size();i++)
		{
			FacebookStatusItem item = currentStatuses.get(i);
			if(item.url.equals(newItem.url))
			{
				return false;
			}
		}
		currentStatuses.add(newItem);
		return true;
	}
	
	@Override
	protected void showLink(Intent intent)
	{
		if(intent != null)
		{
			if(this.stream_publish)upload_span.setVisibility(View.VISIBLE);
			Intent shortIn = (Intent)intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
			Uri uri = shortIn.getData();
			
			Bitmap icon = (Bitmap)intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
			String title= (String)intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
			
				
			FacebookStatusItem newItem = new FacebookStatusItem();
 			newItem.type = FacebookStatusItem.ContentType.LINK;
 			newItem.url  = uri.toString(); 			
 			newItem.name = title;
 			newItem.bmp  = icon;
 			
 			if(addNewItem(newItem) )
			{
			    updateUI();
			}			
		}
	}

	/*
    @Override
	public void titleSelected() 
    {		
		super.titleSelected();
		//send message to my wall
		if(this.isInProcess() == false)
		{
		    handler.obtainMessage(UPDATE_STATUS).sendToTarget();
		}
	}
	*/
	    
	    
	@Override
	protected void createHandler() 
	{
		handler = new UpdateHandler();
	}
	
	//call update status
	final int UPDATE_STATUS =1;
	final int UPLOAD_PHOTO  =2;
	final int CAPTURE_PHOTO =3;
	final int UPLOAD_Vedio  =4;
	final int UPDATE_STATUS_END = 5;
	final int POST_LINK = 6;
	final int UPDATE_UI = 7;
	
	final int STREAM_PUBLISH         = 8;
	final int POST_WALL              = 10;
	final int FACEBOOK_ALBUM_GET     = 11;
	final int CREATE_ALBUM_AND_UPLOAD= 12;
	final int SET_SPINNER_UI          =13;
	final int PUBLISH_STREAM_WITH_ATTACHMENT = 14;
	final int CREATE_ALBUM_END          = 15;
	
	boolean finishedText = false;
	private class UpdateHandler extends Handler 
    {
        public UpdateHandler()
        {
            super();            
            Log.d(TAG, "new UpdateHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case UPDATE_UI:
                {
                	updateUI();
                	break;
                }            	
            	case UPDATE_STATUS://update status, post to wall
            	{
            		//get context 
            	    showDialog(DLG_POST_CONTENT);
            	    facebook_share_button.setEnabled(false);
            	    boolean needDismissDialog = true;
            		String content = contentEdit.getText().toString().trim();
            		if(isEmpty(content) == false) 
            		{
	            		if(post_wall)//for post to wall
	            		{
	            		    //if is myself don't post content to my wall
	            		    //if is others post content to other
	      			        postWall(content);
                            needDismissDialog = false;
	            		}
	            		else if(stream_publish)
	            		{
	            		    if(currentStatuses.size() == 0)
	            		    {
	            		        handler.obtainMessage(STREAM_PUBLISH).sendToTarget();
	            		        needDismissDialog = false;
	            		    }
	            		}
	            		else
	            		{
	            		    //don't send text, when user want to share photo, vidoe, link, 
                            //if no attachment, will post text to facebook
                            if(currentStatuses.size() == 0)
                            {
                                updateStatus(content);
                                needDismissDialog = false;
                            } 
	            		}
	            		
            		}
            		
            		if(album_editor != null && hasInitShareItem()== true)
            		{
	            		String albumname = album_editor.getText().toString().trim();
	                    if(albumSpan.getVisibility() == View.VISIBLE)
	                    {
	                        if(album_editor.getVisibility()== View.VISIBLE && !isEmpty(albumname) )
	                        {
	                            //firstCreateAlbum
	                            Message message = handler.obtainMessage(CREATE_ALBUM_AND_UPLOAD);
	                            message.getData().putString("name", albumname);
	                            message.getData().putString("content", content);
	                            message.sendToTarget();
	                            needDismissDialog = false;
	                        }
	                        else
	                        {         
	                           Object selectedObj = albumSpinner.getSelectedItem();
	                           String aid = null;
	                           if(selectedObj!=null && PhotoAlbum.class.isInstance(selectedObj))
	                           {
	                               aid = ((PhotoAlbum)selectedObj).aid;
	                           }
	                            uploadMeida(content,aid);
	                            needDismissDialog = false;
	                        }
	                    }
	                    else
	                    {
	                        uploadMeida(content,null);
	                        needDismissDialog = false;
	                    }
            		}
            		else
            		{
            		    if(needDismissDialog == true)
            		    {
                            dismissDialog(DLG_POST_CONTENT);
                            facebook_share_button.setEnabled(true);
            		    }
            		}
            		break;
            	}
            	case CREATE_ALBUM_END:
            	{
            	    end();
            	}
            	case UPDATE_STATUS_END:
            	{
                    end();     
            		boolean sucsend = msg.getData().getBoolean(RESULT, false);
            		if(sucsend == false)
            		{
            			Toast.makeText(mContext, R.string.twitter_message_fail_update_status, Toast.LENGTH_SHORT).show();
            		}            			
            		//after send the text content            		
            		finishedText = msg.getData().getBoolean("fortextmessage");
            		long ssid = msg.getData().getLong("ssid", -1);
            		if(finishedText && sucsend)
            		{
            			orm.clearWallDraftByFuid(fuid);
            			contentEdit.setText("");
            		}
            		
            		String text = contentEdit.getText().toString().trim();
            		if(text == null ||  text.length() == 0)            		
            		{
            			finishedText = true;
            		}
            		
            		//check whether finished the send and tag link,video,photo as hasProgress and check 
            		// whether finished all 
            		boolean finished     = true;
            		boolean hasProcessed = false;
            		int processedCount   = 0;
            		synchronized(currentStatuses)
            		{	
	            		for(int i=0;i<currentStatuses.size();i++)
	                	{
	                		FacebookStatusItem item = currentStatuses.get(i);
	                		//
	                		//if upload fail, need set the status to INIT, so the user can re-upload again
	                		//
	                		if(ssid != -1 && ssid == item.id )
                            {
                               if(sucsend == false)
                               {
                                   item.uploadStatus = FacebookStatusItem.Status.INIT;
                               }                                   
                            }
	                		
                		    if(item.uploadStatus != FacebookStatusItem.Status.SUC_UPLOAD)
                            {
                                finished = false;                                
                            }
                		    

                            if(ssid == item.id)
                            {
                               item.hasProcessed = true;    
                            }                          
                            if(item.hasProcessed)
                            {
                                ++processedCount;
                            }
                            
	                	}	
	            		if(processedCount == itemNumShouldBeProcessed)
	            		{
	            		    hasProcessed = true; //all attachment has been uploaded whatever successfully or failed
	            		}
            		}
            		
            		String errorMessage = msg.getData().getString("errormessage");
            		if(errorMessage != null)
            		{
            			Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
            		}
            		
            		if(post_wall || stream_publish || currentStatuses.size()<= 0)
                    {
                        dismissDialog(DLG_POST_CONTENT);
                        facebook_share_button.setEnabled(true);
                        contentEdit.setText("");
                    }
                    else if(hasProcessed == true)
                    {
                       dismissDialog(DLG_POST_CONTENT);  
                       facebook_share_button.setEnabled(true);
                       contentEdit.setText("");
                    }                   
                    
            		/*if(hasProcessed && (stream_publish || post_wall))
            		{
            		    //Publish stream with attachment in background
            		    handler.obtainMessage(PUBLISH_STREAM_WITH_ATTACHMENT).sendToTarget();
            		    
            		}*/
            		//after finished all content and text
            		if(finished == true && finishedText && sucsend == true)
            		{
        		        setResult(0);
        		        finish();            		    
        		    }            		
            		break;
            	}
                case UPLOAD_PHOTO:                	
                {
                	long   sid   = msg.getData().getLong("sid");
                	String photo = msg.getData().getString("imagepath");
                	String text  = msg.getData().getString("text");
                	String aid = msg.getData().getString("aid");
                	uploadImg(sid, photo,text,aid);
                	break;
                }
                case CAPTURE_PHOTO:
                {
                	break;
                }
                case UPLOAD_Vedio:
                { 
                	long   sid   = msg.getData().getLong("sid");
                	String video = msg.getData().getString("videopath");
                	String text = msg.getData().getString("text");
                	uploadVideo(sid, video,text);
                	break;
                }
                case POST_LINK:
                {
                	long   sid   = msg.getData().getLong("sid");
                	String linkurl = msg.getData().getString("linkpath");
                	String text = msg.getData().getString("text");
                	postlink(sid, linkurl,text);
                	break;
                }    
                case STREAM_PUBLISH:
                {
                	String content = contentEdit.getText().toString().trim();
                	if(isEmpty(content) == false )
                	{
                	    pubilshStream(content);
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
                    if(albumSpinner != null)
                    {
                        List<PhotoAlbum> albums = orm.getAlbum(perm_session.getLogerInUserID());     
                        if(albums != null && albums.size() > 0)
                        {
                            FacebookAlbumSpinnerAdapter adapter = new FacebookAlbumSpinnerAdapter(mContext,android.R.layout.simple_spinner_item,albums);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            Object selectedObj = albumSpinner.getSelectedItem();
                            String lastaid = null;
                            if(selectedObj!=null && PhotoAlbum.class.isInstance(selectedObj))
                            {
                                lastaid = ((PhotoAlbum)selectedObj).aid;
                            }
                            albumSpinner.setAdapter(adapter);  
                            boolean aftercreate = msg.getData().getBoolean("aftercreate");
                            boolean addphototospecifiedalbum = msg.getData().getBoolean("addphototospecifiedalbum");
                            if(aftercreate == true)
                            {
                                String newaid= msg.getData().getString("newalbumid");
                                int pos = adapter.getPos(newaid);
                                albumSpinner.setSelection(pos);                            
                            }
                            else if(addphototospecifiedalbum == true)
                            {
                                String aid = msg.getData().getString("albumid");
                                int pos = adapter.getPos(aid);
                                albumSpinner.setSelection(pos);
                            }
                            else
                            {
                                
                                //set selected pos same as before
                                if(isEmpty(lastaid) == false)
                                {
                                    int pos = adapter.getPos(lastaid);
                                    albumSpinner.setSelection(pos);
                                }
                            }
                    }
                    }
                    break;
                }
                /*case PUBLISH_STREAM_WITH_ATTACHMENT:
                {  
                    if(post_wall)
                    {
                        String[] address = mRecipientsEditor.getAddresses();
                        if(address!=null && address.length >0){
                            for( int i = 0 ; i<address.length ; i++){
                                Log.d(TAG, "user id is "+address[i].trim());
                                publishStreamWithAttachment(new Long(address[i].trim()));
                            }
                        }
                    }
                    else if(stream_publish && perm_session!=null)
                    {
                        publishStreamWithAttachment(new Long(perm_session.getLogerInUserID()));
                    }
                    
                    break;
                }*/
            }
        }
    }
	
	public void uploadMeida(String content,String aid)
	{
	  //current we upload all the content to our wall
	  //how much items should be uploaded this time
	    itemNumShouldBeProcessed = 0;
        for(int i=0;i<currentStatuses.size();i++)
        {
            FacebookStatusItem item = currentStatuses.get(i);  
            //initialized hasProcessed 
            item.hasProcessed = false;
            if(item.uploadStatus == FacebookStatusItem.Status.INIT)
            {   
                itemNumShouldBeProcessed++;
                shareContent(content, item,aid);
            }
        } 
	}
	
	public boolean hasInitShareItem() {
	    boolean hasInit = false;
        for(int i=0;i<currentStatuses.size();i++)
        {
            FacebookStatusItem item = currentStatuses.get(i);            
            if(item.uploadStatus == FacebookStatusItem.Status.INIT)
            {
                hasInit = true;
                break;
            }
        } 
        return hasInit;
    }

    private void publishStreamWithAttachment(Long targetid)
	{
	    Log.d(TAG,"entering publishStreamWithAttachment targetid is "+targetid);
        publishMediaImage(targetid);
        publishMediaVideo(targetid);
      
	}
	
	private void publishMediaImage(Long targetid)
	{
	    Log.d(TAG,"entering publishMediaImage");
        
        synchronized(currentStatuses)
        {   
            int count=0;   
          
            List<Photo> photos = new ArrayList<Photo>();
            for(int i=0;i<currentStatuses.size();i++)
            {                 
                FacebookStatusItem item = currentStatuses.get(i);
                if(item.type == FacebookStatusItem.ContentType.CAMERA || item.type == FacebookStatusItem.ContentType.IMAGE)
                {     
                    if(item.obj != null && Photo.class.isInstance(item.obj))
                    {
                        count++;                        
                        Photo photo = (Photo)item.obj; 
                        photos.add(photo);    
                        if(count == 5)
                        {  
                            count = 0;
                            Attachment attachment = new Attachment();
                            AttachmentMediaImage imageMedia =  attachment.new AttachmentMediaImage();
                            //get last 5 items
                            for(int j=0;j<5;j++)
                            {
                                Photo tmp = photos.get(photos.size()-j-1);
                                imageMedia.addImage(tmp.src_small, tmp.link);
                            }  
                            attachment.media = imageMedia;                    
                            publishStream(photo.caption,attachment,null,targetid);
                        }                        
                    }
                }      
            } 
            
            if(photos.size()%5 != 0)//get last not finished
            {
                Attachment attachment = new Attachment();
                AttachmentMediaImage imageMedia =  attachment.new AttachmentMediaImage();
                //get last 5 items
                String title= "";
                for(int j=0;j< photos.size()%5;j++)
                {
                    Photo tmp = photos.get(photos.size()-j-1);
                    if(isEmpty(title))
                    {
                        title = tmp.caption;
                    }
                    imageMedia.addImage(tmp.src_small, tmp.link);
                }
               
                attachment.media = imageMedia;                    
                publishStream(title,attachment,null,targetid);
            }               
            photos.clear();
            photos = null;
            
            // clear publishStreamWithAttachment has published successfully item.obj
            for(int i=0;i<currentStatuses.size();i++)
            {                 
                FacebookStatusItem item = currentStatuses.get(i);
                if(item.type == FacebookStatusItem.ContentType.CAMERA || item.type == FacebookStatusItem.ContentType.IMAGE)
                {     
                    if(item.obj != null && Photo.class.isInstance(item.obj))
                    {
                        item.obj = null;
                    }
                }
            }
        }
	}
	
	private void publishMediaVideo(Long targetid)
	{
	    Log.d(TAG,"entering publishMediaVideo");
	    
	    synchronized(currentStatuses)
        {   
            List<Photo> photos = new ArrayList<Photo>();
            for(int i=0;i<currentStatuses.size();i++)
            {                
                FacebookStatusItem item = currentStatuses.get(i);
                if(item.type == FacebookStatusItem.ContentType.VIDEO )
                {
                    if(item.obj != null && Video.class.isInstance(item.obj))
                    {
                        Video video = (Video)item.obj;
                        Attachment attachment = new Attachment();
                        Attachment.AttachmentMediaVideo videoMedia= attachment.new AttachmentMediaVideo();
                        videoMedia.videoSrc = video.link;
                        videoMedia.link=video.link;
                        videoMedia.previewImg="http://www.sina.cn/jessie/";
                        videoMedia.title=video.title;
                        attachment.media = videoMedia;
                        item.obj = null;
                        publishStream(video.title,attachment,null,targetid);                        
                    }
                }
            } 
        }
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
            @Override public void publishStream(long pid)
            {
                Log.d(TAG, "post to wall="+pid);
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                if(donotcallnetwork == false)//I am still alive
                {                           
                    //cancelNotify();
                }       
                
                Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.getData().putBoolean("fortextmessage", true);
                handler.sendMessage(rmsg);
                
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
                    Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
                    rmsg.getData().putBoolean(RESULT, false);
                    rmsg.getData().putBoolean("fortextmessage", true);
                    rmsg.getData().putString("errormessage", e.getMessage());
                    handler.sendMessage(rmsg);
                }
            }
        });
	}
	
	
	private void createFacebookAlbum(final String name,final String content)
    {
        begin();
        
        synchronized(mLock)
        {
            inprocess = true;
        }
            
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
                    Log.d(TAG, "createAlbumAsync suc ");
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
                    albumid = photoalbum.aid;
                    msd.getData().putString("newalbumid", photoalbum.aid);
                    msd.getData().putBoolean("aftercreate", true);
                    msd.sendToTarget();
                    handler.obtainMessage(CREATE_ALBUM_END).sendToTarget();
                    uploadMeida(content,photoalbum.aid);
                }                  
                @Override public void onException(FacebookException e, int method) 
                {
                    Log.d(TAG, "fail to get album information exception "+e.getMessage());
                    
                    synchronized(mLock)
                    {
                        inprocess = false;
                    }      
                    handler.obtainMessage(CREATE_ALBUM_END).sendToTarget();
                    uploadMeida(content,null);
                }
            });
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
                    Log.d(TAG, "after get album="+photoalbums.size());
                    orm.addAlbum(photoalbums);
                    handler.obtainMessage(SET_SPINNER_UI).sendToTarget();
                }
                
                @Override public void onException(FacebookException e, int method) 
                {
                    Log.d(TAG, "fail to get album information exception "+e.getMessage());
                }
            });
         }     
    }
	
	
	void pubilshStream(String content){
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
		facebookA.publishStreamAsync(content,new FacebookAdapter()
    	{
    		@Override public void publishStream(long pid)
            {
    			Log.d(TAG, "post to wall="+pid);
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
                if(donotcallnetwork == false)//I am still alive
                {							
	            	//cancelNotify();
                }       
                
                Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.getData().putBoolean("fortextmessage", true);
                handler.sendMessage(rmsg);
                
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
	            	Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.getData().putBoolean("fortextmessage", true);
	                rmsg.getData().putString("errormessage", e.getMessage());
	                handler.sendMessage(rmsg);
             	}
            }
    	});
	}
	
	void updateStatus(String content)
	{
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
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
                
                Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.getData().putBoolean("fortextmessage", true);
                handler.sendMessage(rmsg);
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
	            	Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.getData().putBoolean("fortextmessage", true);
	                rmsg.getData().putString("errormessage", e.getMessage());
	                handler.sendMessage(rmsg);
             	}
            }
    	});
	}
	
	public void postWall(String content){
        begin();
        
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
		String[] address = mRecipientsEditor.getAddresses();
		
		if(address!=null && address.length >0){
			for( int i = 0 ; i<address.length ; i++){
				Log.d(TAG, "user id is "+address[i].trim());
				if((String.valueOf(perm_session.getLogerInUserID()).equals(address[i].trim())) && currentStatuses.size()>0)
				{
				    Log.d(TAG, "dont post content to myself");
				    continue;
				}
				facebookA.postWallAsync(Long.valueOf(address[i].trim()), content, new FacebookAdapter()
		    	{
		    		@Override public void postWall(boolean suc)
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
		                
		                Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
		                rmsg.getData().putBoolean(RESULT, true);
		                rmsg.getData().putBoolean("fortextmessage", true);
		                handler.sendMessage(rmsg);
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
			            	Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
			                rmsg.getData().putBoolean(RESULT, false);
			                rmsg.getData().putBoolean("fortextmessage", true);
			                rmsg.getData().putString("errormessage", e.getMessage());
			                handler.sendMessage(rmsg);
		             	}
		            }
		    	});
			}
		}		
	}
	
	public void shareContent(String content, FacebookStatusItem item,String aid) 
	{
		if(item.type == FacebookStatusItem.ContentType.IMAGE || item.type == FacebookStatusItem.ContentType.CAMERA)
		{
			Log.d(TAG, "upload image");
			item.uploadStatus = FacebookStatusItem.Status.UPLOADING;
			String filepath = item.url;
			Message message = handler.obtainMessage(UPLOAD_PHOTO);
			message.getData().putString("imagepath",filepath);
			message.getData().putLong("sid",item.id);
			if(aid!=null)
			{
			  message.getData().putString("aid", aid);
			}
			if(content!=null && content.length()>0)
			{
			   message.getData().putString("text", content);
			}
			message.sendToTarget();
		}
		else if(item.type == FacebookStatusItem.ContentType.VIDEO)
		{         			
			item.uploadStatus = FacebookStatusItem.Status.UPLOADING;
			Message message = handler.obtainMessage(UPLOAD_Vedio);
			message.getData().putString("videopath", item.url);
			message.getData().putLong("sid",item.id);
			if(content!=null && content.length()>0)
			{
				   message.getData().putString("text", content);
			}
			message.sendToTarget();
		}
		else if(item.type == FacebookStatusItem.ContentType.LINK)
		{
			String linkurl = item.url;
			if(linkurl!=null && linkurl.length()>0 && linkurl.startsWith("http://"))
			{
				item.uploadStatus = FacebookStatusItem.Status.UPLOADING;
				Message message = handler.obtainMessage(POST_LINK);
				message.getData().putLong("sid",item.id);
				message.getData().putString("linkpath",linkurl);
				if(content!=null && content.length()>0)
				{
	  			   message.getData().putString("text", content);
	  			}
				message.sendToTarget();
		    }
		}
	}

	void uploadImg(final long sid, String filepath,String caption,String aid)
	{
		
		Log.d(TAG, "entering upload Imag");
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}

		facebookA.uploadImageAsync(sid, filepath,caption,aid,new FacebookAdapter()
    	{
    		@Override public void uploadimage(final long ssid, final Photo photo)
            {
    			//Log.d(TAG, "post to wall="+suc);
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}

                if(donotcallnetwork == false)//I am still alive
                {							
	            	cancelNotify();
                }      
                
                handler.post(new Runnable(){
                   public void run()
                   {
                       uploadedSID(ssid,photo);
                   }
                });
                Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
               // rmsg.getData().putString("imagepath",imgFilepathEdit.getText().toString());
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.getData().putLong("ssid",ssid);
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
	            	Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.getData().putString("errormessage", e.getMessage());
	                rmsg.getData().putLong("ssid",sid);
	                rmsg.sendToTarget();
             	}
            }
    	});

	}
	
	
	void uploadVideo(final long sid, String filepath,String text)
	{
		
		Log.d(TAG, "entering upload video");
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
		
		facebookA.uploadVideoAsync(sid, filepath,text,new FacebookAdapter()
    	{
    		@Override public void uploadvideo(final long ssid, final Video video)
            {
    			//Log.d(TAG, "post to wall="+suc);
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
                if(donotcallnetwork == false)//I am still alive
                {							
	            	cancelNotify();
                }   
                handler.post(new Runnable(){
                    public void run()
                    {
                        uploadedSID(ssid,video);
                    }
                 });
               
                Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.getData().putLong("ssid",sid);
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
	            	Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.getData().putString("errormessage", e.getMessage());
	                rmsg.getData().putLong("ssid",sid);
	                rmsg.sendToTarget();
             	}
            }
    	});

	}
	
	void postlink(final long sid, String linkpath,String comment)
	{
		
		Log.d(TAG, "entering post link");
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
		
		facebookA.postLinkAsync(sid, linkpath,comment,new FacebookAdapter()
    	{
    		@Override public void postlink(final long ssid, long linkid)
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
                handler.post(new Runnable(){
                    public void run()
                    {
                        uploadedSID(ssid,null);
                    }
                 });
                
                Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.getData().putLong("ssid",sid);
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
	            	Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.getData().putString("errormessage", e.getMessage());
	                rmsg.getData().putLong("ssid",sid);
	                rmsg.sendToTarget();
             	}
            }
    	});

	}
	
	@Override 
	public void stopLoading()
	{
		Log.d(TAG, "don't stop upload and update status data connection");
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {         
        	Log.d(TAG, "KEYCODE_BACK coming="+this);
          
        	 if( fuid!=-1 && contentchanged )
        	 {
     	    	if((currentStatuses!=null && currentStatuses.size()>0) ||
     	    		contentEdit.getText().toString().length()>0) {
     	             saveToDraft();   
     	    	}       
     	     }
        	 
        	 //release the capture file
        	 if(currentStatuses != null)
        	 {
            	 for(int i=0;i<currentStatuses.size();i++)
                 {
                     FacebookStatusItem item = currentStatuses.get(i);                  
                     if(item.type == FacebookStatusItem.ContentType.CAMERA)               
                     {
                         File file = new File(item.url);
                         if(file.exists() == true && file.getPath().indexOf(tmpPath)>=0)
                         {
                             file.delete();
                         }
                     }                 
                 }
        	 }  
        }
        return super.onKeyDown(keyCode, event);
    }
	
	public boolean isPostToWall()
	{
		return post_wall;
	}
	
	public class FBUDecorater implements AddressDecorator
	{
        public CharSequence decorateAddress(AddressPad addressPad, CharSequence address, boolean hasFocus) 
        {
            String suid = address.toString().trim();
            try
            {
                    long uid = Long.valueOf(suid);
                    FacebookUser user = orm.getFacebookUser(uid);
                    if(user != null && user.name != null && user.name.length()>0)
                    {
                        return user.name;
                    }
            }
            catch(NumberFormatException ne)
            {               
                Log.d(TAG, "why come here="+ne.getMessage() + " address="+address);
            }
            return address;         
        }
    }
	 
	private class MyWatcher implements TextWatcher 
	{   
       public void afterTextChanged(Editable s) 
       {
    	   contentchanged = true;
    	   textCount.setText(String.format("%1$s", s.length()));
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }
	
	public void registerAccountListener() {
		AccountManager.registerAccountListener("FacebookStatusUpdateActivity", this);		
	}
	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("FacebookStatusUpdateActivity");		
	}
}
