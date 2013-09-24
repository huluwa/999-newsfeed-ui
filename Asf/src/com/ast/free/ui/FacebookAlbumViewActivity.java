package com.ast.free.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ast.free.R;
import com.ast.free.providers.SocialORM;
import com.ast.free.ui.FacebookAlbumActivity.FacebookAlbumItemView;
import com.ast.free.ui.view.ImageRun;
import com.ast.free.ui.view.SNSItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.Photo;
import oms.sns.service.facebook.model.PhotoAlbum;
import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;


public class FacebookAlbumViewActivity extends FacebookBaseActivity{
    private final static String TAG = "FacebookAlbumViewActivity";

	private GridView mGrid;
	private TextView album_name;
	private TextView album_number;
	
	private String foraid="";
	private PhotoAlbum album;
	private List<Photo> photos = new ArrayList<Photo>();
    private boolean fromoutside;
    private long    ownerfromout=-1;
    
    public boolean isFromOutSide()
    {
        return fromoutside;
    }
    
	private String getAlbumName()
	{
		if(album != null)
		{
		    return album.name;
		}
		return "";
	}
	
	private int getAlbumSize()
	{
		if(album != null)
		{
		    return album.size;
		}
		return 0;
	}
	
	protected String getAlbumID()
	{
		if(album != null)
		{
		    return album.aid;
		}
		
		return foraid;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.facebook_album_detail);
        //this.getWindow().setBackgroundDrawable();
        mGrid = (GridView) findViewById(R.id.album_grid);        
        mGrid.setFocusableInTouchMode(true);
        mGrid.setFocusable(true);
        mGrid.setSelected(true);
        mGrid.setClickable(true);  
        mGrid.setOnCreateContextMenuListener(this);
        mGrid.setOnItemClickListener(listItemClickListener);
        
        album_name   = (TextView)this.findViewById(R.id.album_name_detail);
        album_number = (TextView)this.findViewById(R.id.album_number_detail);
        
        album  = this.getIntent().getParcelableExtra("photo_album");  
        foraid = this.getIntent().getStringExtra("albumid");
        fromoutside = this.getIntent().getBooleanExtra("fromoutside", false);
        ownerfromout = this.getIntent().getLongExtra("owner", -1);
        Log.d(TAG,"fromoutside is "+fromoutside);
        
        if(album == null && isEmpty(foraid) == false)
        {
            album = orm.getAlbum(foraid);
        }
        
        album_name.setText(getAlbumName());        
        album_number.setText(String.format(getString(R.string.photo_number_format), getAlbumSize()));
        
        setTitle();
        setTitle(title);
        
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
            perm_session = loginHelper.getPermanentSesstion(this);
            if(perm_session != null)
            {
                perm_session.attachActivity(this);
                
                facebookA = new AsyncFacebook(perm_session);
                boolean needRegetFromWeb = true;
                if(album != null)
                {
                    album = orm.getAlbum(album.aid);
                	List<Photo> phs = orm.getAlbumPhotos(getAlbumID());
	            	if(phs.size() > 0 && phs.size() == album.size)
	            	{
	            		needRegetFromWeb = false;	            		
		            	addPhotos(phs);
	            	}
                }
                
                //always true, if user update comments, she/he need to know the latest 
                if(needRegetFromWeb == true)
                {
                    launchAlbumDetail();
                }
                else
                {
                	handler.obtainMessage(FACEBOOK_ALBUM_UI).sendToTarget();	
                }
            }
            else
            {
                launchFacebookLogin();
            }
        }
	}
	
	public boolean isOwner()
	{
	    if(perm_session !=null && album!=null && perm_session.getLogerInUserID() == album.owner)
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
	
	public PhotoAlbum getAlbum()
	{
		return album;
	}
	
	@Override
	protected void loadRefresh() {		
		super.loadRefresh();
		if(this.isInProcess() == true)
		{
		    showToast();
		}
		launchAlbumDetail();
	}
	
	@Override
	protected void GoToAlbumList()
    {
	    if(getOwnerID() > 0)
        {
            Intent intent = new Intent(mContext, FacebookAlbumActivity.class); 
            intent.putExtra("uid",getOwnerID()); 
            mContext.startActivity(intent);   
        }
        else
        {
            Log.d(TAG," why owner id is 0 ?");
        }
    }

	protected long getOwnerID() {
        // TODO Auto-generated method stub
	    if(album != null)
	    {
	       return album.owner;
	    }
	    else
	    {
	        return 0;
	    }
        
    }

    private void launchAlbumDetail() 
	{
		handler.obtainMessage(FACEBOOK_ALBUM_GET).sendToTarget();	
	}

	AdapterView.OnItemClickListener listItemClickListenerFroBrowser = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int position,long ID) 
		{
			Log.d(TAG, "click photo items");
			Photo photo = ((AlbumItemView)v).getPhoto();
            if(photo != null)
            {
            	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(photo.link));
                intent.setData(Uri.parse(photo.link));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                formatFacebookIntent(intent, orm);
                mContext.startActivity(intent);
            }    
		}
	};
	
	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int position,long ID) 
		{
			Log.d(TAG, "click photo items");
			Photo photo = ((AlbumItemView)v).getPhoto();
            if(photo != null)
            {
            	Intent intent = new Intent(mContext, FacebookPhotoCommentsActivity.class);
        		intent.putExtra("photo", photo);
        		intent.putExtra("forimageview", true);
        		intent.putParcelableArrayListExtra("photos", (ArrayList<Photo>)photos);
        		startActivity(intent);
            }    
		}
	};	
	
	public class AlbumsAdapter extends BaseAdapter 
    {
		public AlbumsAdapter(Context con,  List<Photo> albums )
        {
            photos = albums;     
        }
        public View getView(int position, View convertView, ViewGroup parent) 
        {
        	 if (position < 0 || position >= getCount()) 
             {
                 return null;    
             }
             
             AlbumItemView v=null;
        
             Photo di = (Photo)getItem(position);
             if (convertView == null) {
                 v = new AlbumItemView(mContext, di);
             } 
             else 
             {
                 v = (AlbumItemView) convertView;
                 v.setItem(di);
             }        
             return v;            
        }

        public final int getCount() {
            return photos.size();
        }

        public final Object getItem(int position) {
            return photos.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }
        
        List<Photo> photos;
    }
	    
    public class AlbumItemView extends SNSItemView 
    {
    	private String TAG="AlbumItemView";
    	ImageView      imageView;	    	
    	Photo          photo;
    	
    	public AlbumItemView(Context context, Photo di) {
            super(context);
    		
    	    photo = di;    		
    	    Log.d(TAG, "call AlbumItemView");    		
    	    init();
    	}

		public void setItem(Photo di) 
		{			
			photo = di;
			setUI();
		}

		public Photo getPhoto()
		{
			return photo;
		}
		//create the view
    	private void init() 
    	{		
    		Log.d(TAG,  "call init");
    		LayoutInflater factory = LayoutInflater.from(mContext);
    		removeAllViews();
    		
    		//child 1
    		View v = factory.inflate(R.layout.facebook_album_detail_grid_view, null);    		
    		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
    		addView(v);
    		
    		imageView = (ImageView)v.findViewById(R.id.ablum_image);
    		setUI();
    	}
    	
		private void setUI() 
		{
		    imageView.setImageResource(R.drawable.nopics);
		    ImageRun imagerun = new ImageRun(handler, photo.src_small, 0);
		    imagerun.noimage = true;
	        imagerun.setImageView(imageView);
	        imagerun.post(imagerun);
		}

		@Override
		public String getText() 
		{			
		    return photo.link;
		}		
    }

	@Override
	protected void createHandler() 
	{
		handler = new AlbumHandler();
	}
	
	protected  static final int FACEBOOK_ALBUM_GET         = 1;
    protected  static final int FACEBOOK_ALBUM_GET_END     = 2;
    protected  static final int FACEBOOK_ALBUM_UI          = 3;
    private class AlbumHandler extends Handler
    {
	    public AlbumHandler()
        {
            super();            
            Log.d(TAG, "new AlbumHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case FACEBOOK_ALBUM_GET:
	            {
	            	//get from database firstly
	            	List<Photo> phs = orm.getAlbumPhotos(getAlbumID());
	            	mGrid.setAdapter(null);
	            	addPhotos(phs);
	            	this.obtainMessage(FACEBOOK_ALBUM_UI).sendToTarget();
	            	
	            	AlbumGet();
	            	break;
	            }
	            case FACEBOOK_ALBUM_UI:
	            {
	            	AlbumsAdapter adapter = new AlbumsAdapter(mContext, photos);
	            	mGrid.setAdapter(adapter);
	            	if(album!=null)
	            	{
	            	    album_name.setText(album.name);	
	            	    album_number.setText(String.valueOf(getAlbumSize()));
	            	}
	            	break;
	            }
	            case FACEBOOK_ALBUM_GET_END:
	            {
	            	end();
	            	break;
	            }
            }
        }
    }

	public void setTitle() 
	{
		title = getString(R.string.facebook_album_title);		
	}

	private void addPhotos(List<Photo> photoList)
	{
		synchronized(photos)
    	{
		    List<String> needremovePid = new ArrayList<String>();
		    for(int j=0;j<photos.size();j++)
		    {
		        Photo item = photos.get(j);
		        boolean isExist = false;
		        for(int i=0;i<photoList.size();i++)
		        {
		            Photo newphoto = photoList.get(i);
		            if(item.pid.equalsIgnoreCase(newphoto.pid))
		            {
		                isExist = true;
		                photos.set(j,newphoto);
		                item.dispose();
		                item = null;
		                photoList.remove(i);
		                break;
		            }
		        }		        
		        if(isExist == false)
		        {
		            needremovePid.add(item.pid);
		        }
		    }
		    
		    for(int i=0;i<photos.size();i++)
		    {
		        Photo item = photos.get(i);
		        for(int j=0;j<needremovePid.size();j++)
		        {
		            String pid = needremovePid.get(j);
		            if(item.pid.equalsIgnoreCase(pid))
		            {
		                photos.remove(i);
		                break;
		            }
		        }
		    }
		    
		    if(photoList.size()>0)
		    {
		        photos.addAll(photoList);
		    }	  
	    	java.util.Collections.sort(photos);
    	}
	}
	
    public void AlbumGet() 
    {	
    	if(this.isInProcess())
    	{
    		Log.d(TAG, "already in process");
    		return ;
    	}
    	
        begin();       
        Log.d(TAG, "before get photos");
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
           
            if(album != null)
            {
                //getPhotoByAlbumAsync(album.aid);
                batch_run_getAlbum_Photos(album.aid);
            }
            else if(isEmpty(foraid) == false)
            {
                batch_run_getAlbum_Photos(foraid);
            }
        }   
	}
    
    private void getPhotoByAlbumAsync(String aid)
    {
        facebookA.getPhotoByAlbumAsync(getAlbumID(), new FacebookAdapter()
        {
            @Override public void getPhotosByAlbum(List<Photo> photlists)
            {
                Log.d(TAG, "after get photos="+photlists.size());
                synchronized(mLock)
                {
                    inprocess = false;
                }           
                addPhotos(photlists);
                
                orm.addPhoto(photos);
                
                handler.obtainMessage(FACEBOOK_ALBUM_UI).sendToTarget();
                handler.obtainMessage(FACEBOOK_ALBUM_GET_END).sendToTarget();
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                Log.d(TAG, "fail to get album photo information exception "+e.getMessage());
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                   handler.obtainMessage(FACEBOOK_ALBUM_GET_END).sendToTarget();
                }
               
            }
        });
    }
    
    private void batch_run_getAlbum_Photos(String aid)
    {
        facebookA.batch_run_getAlbum_PhotosAsync(aid, new FacebookAdapter()
        {
            @Override public void  batch_run_getAlbum_Photos(HashMap<Integer, Object> resultmap)
            {
                Log.d(TAG, "after get batch_run_getAlbum_photos= "+ resultmap.size());
                synchronized(mLock)
                {
                    inprocess = false;
                }        
                List<PhotoAlbum>albumlist = (List<PhotoAlbum>)resultmap.get(0);
                if(albumlist.size()>0)
                {
                    album = albumlist.get(0);
                }
                List<Photo> photolists = (List<Photo>)resultmap.get(1);
                addPhotos(photolists); 
                orm.addPhoto(photos);
                if(album != null)
                {
                    orm.addAlbum(album);
                }
                handler.obtainMessage(FACEBOOK_ALBUM_UI).sendToTarget();
                handler.obtainMessage(FACEBOOK_ALBUM_GET_END).sendToTarget();
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                Log.d(TAG, "fail to get album photo information exception "+e.getMessage());
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                   handler.obtainMessage(FACEBOOK_ALBUM_GET_END).sendToTarget();
                }
               
            }
        });
   }
    
    @Override
    public void addPhoto()
    {
        if(perm_session!=null && perm_session.getLogerInUserID() == getOwnerID())
        {
            Intent intent = new Intent(FacebookAlbumViewActivity.this,FacebookStatusUpdateActivity.class);
            intent.putExtra("aid", getAlbumID());
            startActivity(intent);
        }
    }

	public void registerAccountListener() {
        AccountManager.registerAccountListener("FacebookAlbumViewActivity", this);            
    }

    public void unregisterAccountListener() {
        AccountManager.unregisterAccountListener("FacebookAlbumViewActivity");            
    }

    //from context menu
	@Override
	protected void onAfterdeletePhoto(Photo photo) 
    {
        //delete from db
	    String pid = photo.pid;
	    orm.deletePhoto(pid);
	    //refresh ui
	    for(Photo mPhoto: photos)
	    {
	        Log.d(TAG, "mPhoto  pid is "+mPhoto.pid);
	        if(pid.equals(mPhoto.pid))
	        {
	            photos.remove(mPhoto);
	            break;
	        }
	    }
	    AlbumsAdapter adapter = new AlbumsAdapter(mContext, photos);
        mGrid.setAdapter(adapter);
	}
	
	@Override
	protected void onAfterDeleteAlbum(String aid)
    {
	    orm.deleteAlbum(aid);
        orm.deleteAllPhotoInAlbum(aid);
        finish();
       //to album view
        GoToAlbumList();
    }
}
