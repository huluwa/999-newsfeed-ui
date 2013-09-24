package oms.sns.main.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oms.sns.main.R;
import oms.sns.main.providers.SocialORM;
import oms.sns.main.ui.adapter.FacebookStatusContentAdapter;
import oms.sns.main.ui.adapter.FacebookStatusItem;
import oms.sns.main.ui.view.FacebookStatusContentItemView;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TwitterUploadPictureActivity extends StatusViewBaseActivity {

	private String TAG="TwitterUploadPictureActivity";
	private EditText contentEdit;
	private MyWatcher watcher;
	private TextView textCount;	
	    
    private static String tmpPath=TwitterHelper.getTmpPath();	
    ListView      contentList;
    private List<FacebookStatusItem> currentStatuses = new ArrayList<FacebookStatusItem>();   
    
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        //this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.twitpiclogo);
        
        setContentView(R.layout.twitter_photo_upload);
               
        contentList = (ListView)this.findViewById(R.id.facebook_update_content_list);
        
        contentEdit = (EditText)this.findViewById(R.id.facebook_status_message_editor);
        contentEdit.setHint(R.string.facebook_status_update_compose);      
        contentEdit.setVerticalScrollBarEnabled(true);
        InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(defaultTextLength)};
        contentEdit.setFilters(filters);
        textCount = (TextView)this.findViewById(R.id.facebook_status_text_counter); 
        
        watcher = new MyWatcher(); 	    
        contentEdit.addTextChangedListener(watcher);        
        setTitle(R.string.twitter_pic_upload_title);
        
        handler = new UpdateHandler();
      
        SocialORM.Account account = orm.getTwitterAccount();
        if(checkTwitterAccount(this, account) == true)
        {
            
        }
        //need more to set
        setTitle();
        
        handlerIntent();
        
        twitter_action.setVisibility(View.VISIBLE);
    }
	
	@Override
    protected void doAfterLogin()
    {
        Log.d(TAG, "after login");
        //try to get the session
        SocialORM.Account account = orm.getTwitterAccount();
        if(checkTwitterAccount(this, account) == true)
        {
            
        }
    }
	
	@Override
    protected void doAfterLoginNothing() {
        this.finish();
        super.doAfterLoginNothing();
        
    }

    @Override protected void loadAfterSettingNoChange()
	{
	    super.loadAfterSettingNoChange();
	    if(SocialORM.twitterChanged == false)
		{
	    	Log.d(TAG, "call loadAfterSettingNoChange"+this);
			finish();
		}
	}
	
	@Override
    protected void loadAfterSetting()
	{
		super.loadAfterSetting();
		if(SocialORM.twitterChanged == false)
		{
			finish();
		}
	}
	 
	public void setTitle() 
	{
		this.finalTitle = getString(R.string.twitter_pic_upload_title);
	}
	
	protected void requestCamera()
	{
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
        if(orm.isTwitterUseOriginalPhoto())
        {
            intent.putExtra("get_uri", true);
        }
        startActivityForResult(intent, STATUS_CAPTURE_PHOTO);
	}
	
	private void handlerIntent()
    {
    	  Intent intent = this.getIntent();
    	  
          if(intent!=null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND))
          {
         	  Log.d(TAG, "input intent ="+intent);
              
              String contentpath = "";
              String mime        = "";
 	          Uri mMessageUri = (Uri)intent.getData();
 	         
              mime = intent.getType();      
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
            	  Log.d(TAG, "what is data 21="+extrbudle.get(Intent.EXTRA_STREAM));
                  String[] cols = new String[] {MediaStore.Images.Media.DATA,};
                  Cursor mCursor = this.getContentResolver().query(path, cols, null , null, null); 
                  if(mCursor != null && mCursor.moveToFirst())
                  {  
                      contentpath = mCursor.getString(0);
                      Log.d(TAG, "what is data 212="+contentpath);
                      mCursor.close();
                  }                
              }
              
              
              String url = intent.getStringExtra(Intent.EXTRA_TEXT);              
              if(url != null)
              {
            	  Log.d(TAG, "what is EXTRA_TEXT="+url);
            	  contentpath = (String )url;
            	  mime = intent.getType();
              }             
 	          
 	          if(contentpath.length() > 0)
 	          {
 		          FacebookStatusItem newItem = new FacebookStatusItem();
 		          if(mime.indexOf("image")>=0)
 				      newItem.type = FacebookStatusItem.ContentType.IMAGE;
 		          
 				  newItem.url  = contentpath;
 				  newItem.name = contentpath;
 				  currentStatuses.add(newItem);
 				  updateUI();
 	          }
          }         
    }
	
	//remove the click item and update UI
	public void removeFromUI(long id) 
	{
		boolean changed=false;
		for(int i=0;i<currentStatuses.size();i++)
		{
			FacebookStatusItem item = currentStatuses.get(i);
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
		if(currentStatuses != null)
		{
			FacebookStatusContentAdapter fa = new FacebookStatusContentAdapter(TwitterUploadPictureActivity.this, currentStatuses);
			contentList.setAdapter(fa);
		}else{
			contentList.setAdapter(null);
		}
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
	      switch(requestCode)
	      {  
				case STATUS_INSERT_IMG:
				{
					Log.d(TAG, "after insert image==="+intent);
					if(intent != null)
					{
					    showImg(intent,STATUS_INSERT_IMG);
					}
					break;
				}
				case STATUS_CAPTURE_PHOTO:
				{
					//Log.d(Tag, msg)
					if(intent != null)
					{
					    showImg(intent,STATUS_CAPTURE_PHOTO);
					}
					break;
			    }
	      }
	      
	      super.onActivityResult(requestCode, resultCode, intent);
	}
	protected void  showImg(Intent intent,int requestcode)
	{
		Log.d(TAG, "entering image");
		
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
	   		 	   		 
	   		 if(orm.isTwitterUseOriginalPhoto())
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
	   		     Log.d(TAG,"progess compresed photo from camera");
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
	
	private void uploadedSIDUI(long ssid) 
	{
		for(int i=0;i<currentStatuses.size();i++)
		{
			FacebookStatusItem item = currentStatuses.get(i);
			if(item.id == ssid)
			{		
				item.uploadStatus = FacebookStatusItem.Status.SUC_UPLOAD;
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
    	//handler.obtainMessage(UPDATE_UI).sendToTarget();
	}
    protected void uploadedSID(long ssid) 
    {
    	Message msd = handler.obtainMessage(UPDATE_UPLOADED);
    	msd.getData().putLong("ssid", ssid);
    	handler.sendMessage(msd);
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
	
    public void titleSelected() 
    {		
		//send message to my wall
		if(this.isInProcess() == false)
		{			
		    handler.obtainMessage(UPDATE_STATUS).sendToTarget();			
		}
	}
	
	//call update status
	final int UPDATE_STATUS     = 1;
	final int UPLOAD_PHOTO      = 2;
	
	final int UPDATE_STATUS_END = 4;	
	final int UPDATE_UI         = 5;
	final int UPDATE_UPLOADED   = 6;
	
	
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
            		String content = contentEdit.getText().toString().trim();
	            	//current we upload all the content
                	for(int i=0;i<currentStatuses.size();i++)
                	{
                		FacebookStatusItem item = currentStatuses.get(i);
                		if(item.uploadStatus == FacebookStatusItem.Status.INIT)
                		{
                		    shareContent(content, item);
                		}
                	}            		
            		break;
            	}
            	case UPDATE_STATUS_END:
            	{
            		end();            		
            		
            		contentEdit.setText("");
            		//check whether finished the send
            		boolean finished=true;
            		synchronized(currentStatuses)
            		{	
	            		for(int i=0;i<currentStatuses.size();i++)
	                	{
	                		FacebookStatusItem item = currentStatuses.get(i);
	                		if(item.uploadStatus != FacebookStatusItem.Status.SUC_UPLOAD)
	                		{
	                			finished = false;
	                			break;
	                		}
	                	}	            		
            		}
            		
            		String errorMessage = msg.getData().getString("errormessage");
            		if(errorMessage != null)
            		{
            			Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
            		}
            		
            		//after finished all content and text
            		if(finished == true)
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
                	uploadImg(sid, photo,text);
                	break;
                }                      
                case UPDATE_UPLOADED:
                {
                	long ssid = msg.getData().getLong("ssid", -1);
                	uploadedSIDUI(ssid);
                	break;
                }                
            }
        }
    }	
	
	public void shareContent(String content, FacebookStatusItem item) 
	{
		if(item.type == FacebookStatusItem.ContentType.IMAGE || item.type == FacebookStatusItem.ContentType.CAMERA)
		{
			Log.d(TAG, "upload image");
			String filepath = item.url;
			Message message = handler.obtainMessage(UPLOAD_PHOTO);
			message.getData().putString("imagepath",filepath);
			message.getData().putLong("sid",item.id);
			if(content!=null && content.length()>0)
			{
			   message.getData().putString("text", content);
			}
			message.sendToTarget();
		}
	}

	void uploadImg(long sid, String filepath,String message)
	{
		if(twitterA == null)
		{
			Log.d(TAG, "no twitter account");
			return ;
		}
		
		Log.d(TAG, "entering upload Imag");
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
		twitterA.uploadPhotoAsync(sid, filepath,message,new TwitterAdapter()
    	{
			 @Override public void uploadPhoto(long ssid, boolean uploadPhoto)
			 {
    			synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}

                if(donotcallnetwork == false)//I am still alive
                {							
	            	cancelNotify();
                }      
                
                uploadedSID(ssid);
                Message rmsg = handler.obtainMessage(UPDATE_STATUS_END);               
                rmsg.getData().putBoolean(RESULT, true);
                handler.sendMessage(rmsg);
            }
    		
            @Override public void onException(TwitterException e,int method)              
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
	                handler.sendMessage(rmsg);
             	}
            }
    	});
	}	
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {         
             Log.d(TAG, "KEYCODE_BACK coming="+this);            
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
}
