package com.msocial.facebook.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.msocial.facebook.R;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.ui.view.ImageRun;
import com.msocial.facebook.ui.view.SNSItemView;
import com.msocial.facebook.util.DateUtil;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FacebookAlbumActivity extends FacebookBaseActivity{
    final String TAG="FacebookAlbumActivity";
    private List<PhotoAlbum> albums = new ArrayList<PhotoAlbum>();
    private ListView albumlist;
    private long inputuid;
    private TextView album_name;
    private TextView album_number;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_album_ui);
        
        albumlist = (ListView)this.findViewById(R.id.facebook_album_list);   
        albumlist.setFocusableInTouchMode(true);
        albumlist.setFocusable(true);
        albumlist.setSelected(true);
        albumlist.setClickable(true);        
        albumlist.setOnCreateContextMenuListener(this);
        albumlist.setOnItemClickListener(itemClick);
        
        album_name   = (TextView)this.findViewById(R.id.album_name);
        album_number = (TextView)this.findViewById(R.id.album_number);
        
        album_name.setText(R.string.my_albums);
        
        setTitle();
        setTitle(title);
        //setTitle(R.string.menu_title_photos);
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
            perm_session = loginHelper.getPermanentSesstion(this);
            if(perm_session != null)
            {
                perm_session.attachActivity(this);
                
                facebookA = new AsyncFacebook(perm_session);
                inputuid = this.getIntent().getLongExtra("uid", perm_session.getLogerInUserID());
                if(inputuid != perm_session.getLogerInUserID())
                {
                    FacebookUser.SimpleFBUser user = orm.getSimpleFacebookUser(inputuid);
                    if(user!=null)
                    {
                        album_name.setText(String.format(getString(R.string.albums_format), user.name));
                    }
                    else
                    {
                        album_name.setText(R.string.albums_format_nocache);
                    }
                }
                launchGetAlbum();
            }
            else
            {
                launchFacebookLogin();
            }
        }
    }

    @Override
    public void onLogin() 
    {       
        super.onLogin();
        
        Log.d(TAG, "call onLogin="+this);
        
        if(facebookA != null)
        {
            launchGetAlbum();
        }
    }
    
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
                    formatFacebookIntent(intent, orm);
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
    
    @Override
	protected void onAfterDeleteAlbum(String aid) 
	{
        orm.deleteAlbum(aid);
        orm.deleteAllPhotoInAlbum(aid);
        for(int i=0;i<albums.size();i++)
        {
            PhotoAlbum album = albums.get(i);
            if(aid!=null && aid.equals(album.aid))
            {
                albums.remove(i);
            }
        } 
        FacebookAlbumActivity.FacebookAlbumAdapter fs = new FacebookAlbumActivity.FacebookAlbumAdapter(FacebookAlbumActivity.this, albums);
        albumlist.setAdapter(fs);
	}
    
    private void launchGetAlbum()
    {
       handler.obtainMessage(FACEBOOK_ALBUM_GET).sendToTarget();
    }
    @Override
    protected void createHandler() 
    {
        handler = new AlbumHandler();        
    }

    public void setTitle() 
    {
    	title = getString(R.string.menu_title_photos);
    }
    
    final static int FACEBOOK_ALBUM_GET     = 0;
    final static int FACEBOOK_ALBUM_UI      = 1;
    final static int FACEBOOK_ALBUM_GET_END = 2;
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
                    Log.d(TAG,"get facebook album ");
                    List<PhotoAlbum> pas = orm.getAlbum(inputuid);
                    addPhotoAlbum(pas);
                    
                    this.obtainMessage(FACEBOOK_ALBUM_UI).sendToTarget();
                    getFacebookAlbumAsync();
                    break;
                }
                case FACEBOOK_ALBUM_UI:
                {
                	album_number.setText(String.format(getString(R.string.album_number_format), albums.size()));
                    FacebookAlbumAdapter fa_adapter = new FacebookAlbumAdapter(FacebookAlbumActivity.this,albums);
                    albumlist.setAdapter(fa_adapter);
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
    
    void getFacebookAlbumAsync()
    {   
         if(isInProcess() == true)
         {
             return;
         }
         begin();       
         Log.d(TAG, "before get AlbumAsync");
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
            
            facebookA.batch_run_getFacebookAlbumAsync(inputuid,new FacebookAdapter()
            {
                @Override public void getFacebookAlbum(List<PhotoAlbum> photoalbums)
                {
                    Log.d(TAG, "after get album="+photoalbums.size());
                    synchronized(mLock)
                    {
                        inprocess = false;
                    }           
                    albums = photoalbums;
                    orm.addAlbum(photoalbums);
                    
                    handler.obtainMessage(FACEBOOK_ALBUM_UI).sendToTarget();
                    handler.obtainMessage(FACEBOOK_ALBUM_GET_END).sendToTarget();
                }
                
                @Override public void onException(FacebookException e, int method) 
                {
                    Log.d(TAG, "fail to get album information exception "+e.getMessage());
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
    }
    
    public void addPhotoAlbum(List<PhotoAlbum> pas) 
    {
    	synchronized(albums)
    	{
	    	for(int i=0;i<pas.size();i++)
			{
	    		PhotoAlbum item = pas.get(i);
				boolean isExist = false;
				for(int j=0;j<albums.size();j++)
				{
					PhotoAlbum exist = albums.get(j);
					if(item.aid.equalsIgnoreCase(exist.aid))
					{
						isExist=true;
						//update the content						
						exist.dispose();
						exist=null;
						
						albums.set(j, item);
						break;
					}
				}
				
				if(isExist == false && item.aid.equals("-2") == false)//no need default album
				{
					albums.add(item);				
				}
		    }	    	
	    	java.util.Collections.sort(albums);
    	}
	}
	public boolean isMySelf()
    {
        return (perm_session!=null && (perm_session.getLogerInUserID() == inputuid));
    }
    
    @Override
    protected void loadRefresh()
    {
    	super.loadRefresh();
        if(this.isInProcess() == true)
        {
            showToast();
        }
    	launchGetAlbum();
    }
    
    @Override
    public void addPhoto()
    {
        if(perm_session!=null && perm_session.getLogerInUserID() == inputuid)
        {
            Intent intent = new Intent(FacebookAlbumActivity.this,FacebookStatusUpdateActivity.class);
            startActivity(intent);
        }
    }

    public void registerAccountListener() {
        AccountManager.registerAccountListener("FacebookNotificationManActivity", this);            
    }

    public void unregisterAccountListener() {
        AccountManager.unregisterAccountListener("FacebookNotificationManActivity");            
    }
    
    
    public static class FacebookAlbumAdapter extends BaseAdapter
    {
        private Context mContext;
        private List<PhotoAlbum> mPhotoAlbum;
        
        public FacebookAlbumAdapter(Context con,  List<PhotoAlbum> albums )
        {
            mContext = con;
            mPhotoAlbum = albums;     
        }
        
        public int getCount() {
            return mPhotoAlbum.size();
        }

        public Object getItem(int pos) {
            return mPhotoAlbum.get(pos);
        }

        public long getItemId(int pos) {
            return pos;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (position < 0 || position >= getCount()) 
            {
                return null;    
            }
            
            FacebookAlbumItemView v=null;
       
            PhotoAlbum di = (PhotoAlbum)getItem(position);
            if (convertView == null) {
                v = new FacebookAlbumItemView(mContext, di);
            } else {
                 v = (FacebookAlbumItemView) convertView;
                 v.setItem(di);
            }        
            return v;
        }
        
    }
    
    public static class FacebookAlbumItemView extends SNSItemView
    {
        static final String TAG="FacebookAlbumItemView";
        private Context mContext;
        private PhotoAlbum mItem;
        private ImageView album_image_view;
        private TextView album_name_view;
        private TextView update_time_view;
        private TextView photo_number_view;
        private Handler handler;
        public FacebookAlbumItemView(Context context) 
        {
           super(context);
           mContext = context;
           handler = new Handler();
        }
        
        public void setItem(PhotoAlbum di) 
        {
            mItem = di;            
            setUI();            
        }

        public FacebookAlbumItemView(Context context,PhotoAlbum item)
        {
            super(context);
            mContext = context;
            mItem = item;
            handler = new Handler();
            init();
        }
        
        private void init() 
        {
            Log.d(TAG,  "call FacebookAlbumItemView init");
            LayoutInflater factory = LayoutInflater.from(mContext);
            removeAllViews();
            
            //container
            FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);        
            FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);     
            view.setLayoutParams(paras);
            view.setVerticalScrollBarEnabled(true);
            addView(view);
            
            int width = 0;
            int otherwidth = 10+70+70;
            int leftwidth = 0;
            if(width == 0)
            {
                if(FacebookAlbumActivity.class.isInstance(mContext))
                {
                  width = ((FacebookAlbumActivity)mContext).getWindowManager().getDefaultDisplay().getWidth();
                }
                else if(FacebookAccountActivity.class.isInstance(mContext))
                {
                    width = ((FacebookAccountActivity)mContext).getWindowManager().getDefaultDisplay().getWidth();
                }
                otherwidth = 10+70+70;
                leftwidth = width - otherwidth-5;
            }
            Log.d(TAG,"widht is "+width+"leftwidth is "+leftwidth);
            //child 1
            View v  = factory.inflate(R.layout.facebook_album_item, null);     
            v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));
            view.addView(v);
            //album_name_view.setMaxWidth(left);
            album_image_view  = (ImageView)v.findViewById(R.id.facebook_album_img_ui);
            album_name_view = (TextView)v.findViewById(R.id.facebook_album_name);    
            update_time_view = (TextView)v.findViewById(R.id.facebook_album_time);
            photo_number_view = (TextView)v.findViewById(R.id.facebook_photo_number);
            if(leftwidth > 0 )
            {
                album_name_view.setWidth(leftwidth);
                album_name_view.setMaxWidth(leftwidth);
                
            }
            //setOnClickListener(msgOnClik);  
            setUI();    
        } 
        
        private void setUI()
        {     
           album_image_view.setImageResource(R.drawable.nopics);
           ImageRun imagerun = new ImageRun(handler,mItem.cover_src_url, 0);
           imagerun.noimage = true;
           imagerun.setImageView(album_image_view);
           imagerun.post(imagerun);
           
           album_name_view.setText(mItem.name);
           photo_number_view.setText(String.format(mContext.getString(R.string.photo_number_format),mItem.size));
           Date updatetime = mItem.modified!=null?mItem.modified:mItem.created;
           if(updatetime!=null && updatetime.getTime()>0)
           {
              update_time_view.setText(DateUtil.converToRelativeTime(mContext, updatetime));     
           }
        }
        
        @Override
        protected void onFinishInflate() 
        {   
            super.onFinishInflate();        
            init();
        }        
        
        View.OnClickListener msgOnClik = new View.OnClickListener()
        {
            public void onClick(View v) 
            {
                PhotoAlbum  album = getPhotoAlbum();
                if(album != null)
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(album.link));
                    intent.setData(Uri.parse(album.link));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }    
            }
        };

        @Override
        public String getText() 
        {            
            return mItem.name;
        }
        
        @Override
        public  List<String> getLinks()
        {            
        	List<String> links = new ArrayList<String>();
            links.add(mItem.link);
            return links;
        }

        public PhotoAlbum getPhotoAlbum() {
            return mItem;            
        }        
    }

}
