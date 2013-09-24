package com.msocial.free.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.msocial.free.R;
import com.msocial.free.providers.SocialORM;
import com.msocial.free.service.SNSService;
import com.msocial.free.ui.adapter.FacebookStreamAdapter;
import com.msocial.free.ui.view.FacebookSelectUserItemView;
import com.msocial.free.ui.view.FacebookStreamItemView;
import com.msocial.free.ui.view.ImageRun;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Page;
import oms.sns.service.facebook.model.Stream;
import oms.sns.service.facebook.model.StreamFilter;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookStreamActivity extends FacebookBaseActivity
{
	final String TAG="FacebookStreamActivity";
	//time out to re-get the newer stream	
	private int intervalTime    = 60*1000;	
	private int pagesize        =20;	
	private long lasttime;	
	private int limitation      = 20;
	private int lastVisiblePos  = 0;
	
	private List<Stream> streams = new ArrayList<Stream>();
	private ListView streamList;
	
	View     facebook_info_span;
	TextView facebook_info;	
	Button   wall_post;
	EditText facebook_wall_message_editor;
	String filter = "";
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_stream_ui);
        streamList = (ListView)this.findViewById(R.id.facebook_stream_list);   
        streamList.setFocusableInTouchMode(true);
        streamList.setFocusable(true);
        streamList.setOnCreateContextMenuListener(this);
        
        facebook_wall_message_editor = (EditText)this.findViewById(R.id.facebook_wall_message_editor);
        wall_post = (Button)this.findViewById(R.id.wall_post);
        wall_post.setOnClickListener(wallPostOnClik);
        
        Paint p = wall_post.getPaint();
        float width = p.measureText(wall_post.getText().toString());
        p = null;   
        wall_post.getLayoutParams().width = (int)width + 8;
        p = null;
        
        facebook_info_span = (View)this.findViewById(R.id.facebook_info_span);
		facebook_info      = (TextView)this.findViewById(R.id.facebook_info);
		
		View v = findViewById(R.id.progress_horizontal);
		if(v != null)
		{
		    progressHorizontal = (ProgressBar) v;
		}
		
        setTitle();
        SocialORM.Account account = orm.getFacebookAccount();
        intervalTime = orm.getFacebookStreamTimeout()*1000;
        
        Intent in = new Intent(this.getApplicationContext(), SNSService.class);            
        startService(in);
        
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);	        	
	        	facebookA = new AsyncFacebook(perm_session);
	        	
	        	new DeSerializationTask().execute((Void[])null);
	        	reschedule();
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }
    }
	
	View.OnClickListener wallPostOnClik = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            handler.obtainMessage(FACEBOOK_WALL_POST).sendToTarget();
        }
    };
	
	private static String newsfeed_sfile = TwitterHelper.newsfeed;	
	
	private class DeSerializationTask extends android.os.AsyncTask<Void, Void, Void>
    {       
        public DeSerializationTask()
        {
            super();            
            Log.d(TAG, "create DeSerializationTask="+this);
        }

		@Override
		protected Void doInBackground(Void... params)			  
        {
			deSerialization();
            return null;
        }
    }
	
	@Override
	protected void reloadStreamFilter(String filter, String name) 
    {
	    title = name;
	    setTitle(title);
	    
	    Log.d(TAG,"entering reloadStreamFilter filter is "+filter);
	    if(filter != null && this.filter.equals(filter))
	    {
	        Log.d(TAG, "not change the filter="+filter);
	    }
	    else
	    {
	        this.filter = filter;
	        //clear UI
	        lastVisiblePos = 0;
	        lasttime       = 0;
	        synchronized(streams)
	        {
	            streamList.setAdapter(null);  
                while(streams.size() > 0)
                {
                    int pos = streams.size() -1;
                    Stream item = streams.get(pos);
                    item.dispose();
                    item = null;
                    streams.remove(pos);
                }           
	            handler.obtainMessage(FACEBOOK_STREAM_UI).sendToTarget();
	        }
            reschedule();
	    }
        
    }
    private void deSerialization()
	{
		synchronized(streams)
		{
			FileInputStream fis = null;
			ObjectInputStream in = null;
			try{
			    fis = new FileInputStream(newsfeed_sfile);
			    in = new ObjectInputStream(fis);
			    long lastrecord = in.readLong();
			    Date now = new Date();
			    
			    if((now.getTime() -lastrecord) >2*24*60*60*1000L)
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
			    
			    if(streams.size()>0)
		        {
		            lasttime = streams.get(0).updated_time+1000;
		        }
			    
			    handler.obtainMessage(FACEBOOK_STREAM_UI).sendToTarget();
			}
			catch(IOException ex)
			{
				Log.d(TAG, "deserialization fail="+ex.getMessage());
			}
			catch(ClassNotFoundException ex)
			{
				Log.d(TAG, "deserialization fail="+ex.getMessage());
			}
		}
	}
	
	private void serialization()
	{
	    if(streams != null && streams.size() > 0)
		{
			synchronized(streams)
			{
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
	
	
	public View.OnClickListener loadOlderClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			Log.d(TAG, "load older message");		  	  	
			loadOlderPost();
		}
	};
	
	@Override
    protected void loadOlderPost()
    {
		if(streams != null && streams.size() > 0)
		{
			long prelasttime = streams.get(streams.size()-1).updated_time;
			this.lastVisiblePos = streamList.getFirstVisiblePosition()+1;			
			Log.d(TAG, "pos="+lastVisiblePos);
			
			cancelAlarming();
			getStream(prelasttime, false, filter);			
		}	
		else
		{
			getStream(0, true, filter);			
		}
    }
	
	@Override
	protected void updateComments(String post_id, List<String>comments)
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
    
    private void updateView(String post_id)
    {
    	 //process for UI
    	for(int j=0;j<streamList.getChildCount();j++)    		 
        {
            View v = streamList.getChildAt(j);
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
	
	/*@Override
	public void titleSelected()
    {
		super.titleSelected();
		
		Intent intent = new Intent(this, FacebookTabActivity.class);
		this.startActivity(intent);
		this.finish();		
	}*/
	
	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			Log.d(TAG, "stream Item clicked, but  do nothing");
		}
	};
	
	public void reschedule()
	{
		Log.d(TAG, "reschedule at "+ new Date().toLocaleString());
		long nexttime = System.currentTimeMillis()+ 100;		
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent();
        i.setClassName("com.msocial.free", "com.msocial.free.ui.FacebookStreamActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmMgr.set(AlarmManager.RTC, nexttime, pi);	        
	}
	
	static int nTimes;
	public void alarmComming()
	{
		nTimes++;
		Log.d(TAG, "tims="+nTimes+"  &&&&&&&&&&&  time it out="+ new Date().toLocaleString());
		lauchGetStream();
		
		if(this.isFromTabView() == false)
		{
			AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
			long nexttime = System.currentTimeMillis()+ orm.getFacebookStreamTimeout()*1000;	
			
			Intent i = new Intent();
	        i.setClassName("com.msocial.free", "com.msocial.free.ui.FacebookStreamActivity");
	        i.setAction(ACTION_CHECK_CONTECT);
	        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
	        
			alarmMgr.set(AlarmManager.RTC, nexttime, pi);
		}		
	}	
	
	private void cancelAlarming()
	{
		Log.d(TAG, "Cancel alarm");
        AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);		
        
        Intent i = new Intent();
        i.setClassName("com.msocial.free", "com.msocial.free.ui.FacebookStreamActivity");
        i.setAction(ACTION_CHECK_CONTECT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pi);
	}
	
	
	 
	@Override
	protected void onNewIntent(Intent intent) 
	{		
		super.onNewIntent(intent);
		
		if(intent.getAction() != null && intent.getAction().equals(ACTION_CHECK_CONTECT))
		{
			setIntent(intent);
		}
	}

	//when resume, will recall the trend,
	//to save battery, if the trend is in background, don't call the message 
    @Override protected void onResume() 
    {
    	super.onResume();	
    	
    	//cancel first
    	cancelAlarming();
    	
        //resume to call
    	alarmComming();    	
    }
    
    private void doNoContent()
    {
    	if(streams.size() == 0)
    	{
    	   //facebook_info_span.setVisibility(View.VISIBLE);
    	   //facebook_info.setText(R.string.no_news_feed_hint);
    	   // cancelAlarming();
           //resume to call
    	   // alarmComming();   
    	}
    	else
    	{
    		facebook_info_span.setVisibility(View.GONE);
    	}
    }
    
    @Override protected void onPause() 
    {   
        super.onPause();
                        
        cancelAlarming();
    }
    
    @Override protected void onDestroy() 
    {   
        cancelAlarming();        
        
        serialization();
        
        streamList.setAdapter(null);
        synchronized(streams)
        {
	        if(streams != null)
	        {
	        	for(Stream item: streams)
	        	{
	        		item.dispose();
	        		item = null;
	        	}
	            streams.clear();
	        }
        }
        
        ImageRun.revokeAllImageView(this);
        
        for(StreamFilter item:StreamFilters)
        {
        	item.despose();
        }
        StreamFilters.clear();
        StreamFilters = null;
        
        clearAsyncFacebook(true);
        super.onDestroy();
        
        System.gc();
    }
	
	public void setTitle()
	{
		//title = this.getString(R.string.menu_title_go_to_facebook);
		title = "News Feed";
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
						    	for(int j=0;j<streamList.getChildCount();j++)    		 
						        {
						            View v = streamList.getChildAt(j);
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
						    	for(int j=0;j<streamList.getChildCount();j++)    		 
						        {
						            View v = streamList.getChildAt(j);
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
	protected void doPublishStream() 
	{
		Log.d(TAG, "publish new stream, we need re-user the status-update");
		Intent intent = new Intent(FacebookStreamActivity.this, FacebookStatusUpdateActivity.class);  
		intent.putExtra("stream_publish", true);
		startActivityForResult(intent, FACEBOOK_STATUS_UPDATE_UI);
	}
	 
	//reget the stream
	@Override
	protected void loadRefresh()
	{
		lastVisiblePos = streamList.getFirstVisiblePosition();		
		//cancelAlarming();
		//reschedule();	
		 if(this.isInProcess() == true)
		 {
		     showToast();
	     }
		 lauchGetStream();
	}
	
	private void lauchGetStream()
	{
		handler.obtainMessage(FACEBOOK_STREAM_GET).sendToTarget();
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
    		//launchFacebookLogin();
    		doNoContent();
    	}
    	else
    	{    		
    		facebookA = new AsyncFacebook(perm_session);
    		perm_session.attachActivity(this);
    		reschedule();
    	}
    }    
    
    @Override
    public void onLogin() 
	{		
    	super.onLogin();
	}
	
	@Override
	protected void createHandler() {
		handler = new StramHandler();		
	}
	
	final int FACEBOOK_STREAM_GET     =0;
	final int FACEBOOK_STREAM_UI      =2;
	final int FACEBOOK_STREAM_GET_END =3;
	final int FACEBOOK_STREAM_FILTER_GET = 4;
	final int FACEBOOK_STREAM_FILTER_UI  = 5;
	final int FACEBOOK_STREAM_FILTER_GET_END = 6;
	final int FACEBOOK_STREAM_CLEAR = 7;
	
	private class StramHandler extends Handler 
    {
        public StramHandler()
        {
            super();            
            Log.d(TAG, "new StramHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {   
                case PINFO_UINFO_GET:
                {
                    long[] uids = msg.getData().getLongArray("ids");
                    getPageInfoAndUserInfo(uids);
                    break;
                }
	            case FACEBOOK_WALL_POST:
	            {
	                postWallMessage();
	                break;
	            }
	            case FACEBOOK_WALL_POST_END:
	            {   
	            	dismissDialog(DLG_POST_WALL);
	            	wall_post.setEnabled(true);
	                boolean result = msg.getData().getBoolean(RESULT);
	                if(result)
	                {
	                	reschedule();
	                    facebook_wall_message_editor.setText("");
	                    Toast.makeText(FacebookStreamActivity.this, R.string.sns_operate_succeed, Toast.LENGTH_SHORT).show();
	                }
	                else
	                {
	                	Toast.makeText(FacebookStreamActivity.this, R.string.sns_operate_failed, Toast.LENGTH_SHORT).show();
	                }
	                break;
	            }
            	case FACEBOOK_STREAM_GET:                	
                {
                	if(isBackgroud() == true)
                	{
                	   Log.d(TAG, "I am in back, don't call");                	   
                	}
                	else
                	{
                		getStream(lasttime, true, filter);                		
                	}
                	break;
                }
            	case FACEBOOK_STREAM_UI:
            	{
            		FacebookStreamAdapter af = new FacebookStreamAdapter(FacebookStreamActivity.this, streams);
            		streamList.setAdapter(af);
            		
            		//scroll to pre-position
            		streamList.setSelection(lastVisiblePos);            		
            		break;
            	}            	
            	case FACEBOOK_STREAM_GET_END:                	
                {	
                	end();
                	
                	doNoContent();                	               	
                	
                	if(isFromTabView() == true)
            		{
            			Message mds = handler.obtainMessage(FACEBOOK_STREAM_GET);
            			handler.sendMessageDelayed(mds, orm.getFacebookStreamTimeout()*1000);
            		}
                	
                	//process for UI
                    for(int i=streamList.getChildCount()-1;i>0;i--)            
                    {
                        View v = streamList.getChildAt(i);
                        if(Button.class.isInstance(v))
                        {
                            Button bt = (Button)v;
                            bt.setText(getString(R.string.load_older_msg));
                            break;
                        }
                    }                    
                    
                	break;
                }
            	case  FACEBOOK_STREAM_FILTER_GET:
            	{
            	    getStreamFilterFromWeb();
            	    break;
            	}
            	case  FACEBOOK_STREAM_FILTER_UI:
            	{
            	    showFilterDialog();
            	    break;
            	}
            	case FACEBOOK_STREAM_FILTER_GET_END:
            	{
            	    Log.d(TAG," don't show dialog just update cache");            	    
            	    break;
            	}
            	case FACEBOOK_STREAM_CLEAR:
            	{
            	    getLastViewCount(tmpStreamList, msg.getData().getBoolean("newpost", false));
                    
            	    //just has new item will change the UI
                    if(tmpStreamList.size() > 0)
                    {
                        handler.obtainMessage(FACEBOOK_STREAM_UI).sendToTarget();
                        tmpStreamList.clear();
                    }
                                       
            	    break;
            	}
            }
        }
    }
	// implement update status method
	private void postWallMessage()
    {
        if(facebookA != null)
        {
            String content= facebook_wall_message_editor.getText().toString().trim();
            if(content != null && content.length() > 0)
            {
            	wall_post.setEnabled(false);
                showDialog(DLG_POST_WALL);
                long uid = -1;
                if(perm_session != null)
                {
                	uid = perm_session.getLogerInUserID();
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
                
                
               /* facebookA.postWallAsync(uid, content, new FacebookAdapter()
                {
                    @Override public void postWall(boolean suc)
                    {
                        Log.d(TAG, "post to news feed="+suc);
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
                        
                        Log.d(TAG, "post to news feed ex="+e.getMessage());
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
                });*/
            }
        }
    }    
	
	public void getPageInfoAndUserInfo(long[] uids) {
	    Log.d(TAG," entering batch run get user info and page info method");
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
                                int currentLocation = streamList.getFirstVisiblePosition();
                                FacebookStreamAdapter af = new FacebookStreamAdapter(FacebookStreamActivity.this, streams);
                                streamList.setAdapter(af);
                                streamList.setSelection(currentLocation);
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
    @Override
	public void onLowMemory() {		
		super.onLowMemory();
		
		//if memory less than 2M, release UI
		Log.d(TAG, "onLowMemory");
		freeMemory();
	}
	
	public void getStreamFilterFromWeb() {
	    if(facebookA != null)
        {
            facebookA.getOpenStreamFilterAsync(perm_session.getLogerInUserID(),new FacebookAdapter()       
            {
                 @Override 
                 public void getStreamFilter(List<StreamFilter> filters)
                 {
                     Log.d(TAG, "suc to get stream filter="+filters.size());
                     orm.addStreamFilter(filters);
                     Message msd = handler.obtainMessage(FACEBOOK_STREAM_FILTER_GET_END);
                     msd.getData().putBoolean(RESULT, true);
                     msd.sendToTarget();
                 }
                 @Override public void onException(FacebookException e, int method) 
                 {
                     Log.d(TAG, "fail to get filter exception "+e.getMessage());
                     
                     Message msd = handler.obtainMessage(FACEBOOK_STREAM_FILTER_GET_END);
                     msd.getData().putBoolean(RESULT, false);
                     msd.sendToTarget();
                 }
            });
        }
    }
    private void freeMemory()
	{
		cancelAlarming();
		
		lastVisiblePos = 0;
		lasttime       = 0;
		Toast.makeText(this, "Low memory", Toast.LENGTH_SHORT).show();
		
		synchronized(streams)
		{
			streamList.setAdapter(null);	
			
			while(streams.size() > pagesize)
	    	{
				int pos = streams.size() -1;
				Stream item = streams.get(pos);
				item.dispose();
				item = null;
	    		streams.remove(pos);
	    	}			
			handler.obtainMessage(FACEBOOK_STREAM_UI).sendToTarget();
		}
		
		System.gc();
	    reschedule();
	}

	private void getLastViewCount(final List<Stream> sts, boolean forrefresh)
	{
		synchronized(streams)
    	{
			//remove pre-serialize stream to let the stream is update to date			
			if(sts.size() == limitation && streams.size() > 0)
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
	    	//no need this size limitation, for show older post	    
	    	/*
	    	if(forrefresh == true)
	    	{
		    	while(streams.size() > pagesize)
		    	{
		    		streams.remove(streams.size() -1);
		    	}
	    	}
	    	*/
	    	
	    	java.util.Collections.sort(streams);
	    	if(streams.size()>0)
	        {
	            lasttime = streams.get(0).updated_time+1000;
	        }	
	    	
	    	loadUserInfoAndPageInfo(streams);
    	}
	}
	List<Stream> tmpStreamList = new ArrayList<Stream>();	
	private void getStream(long prelasttime, final boolean newpost, String filter) 
	{	
	    if(this.isInProcess() == true)
	    {
	        return;
	    }
		if(isSafeCallFacebook(true) == false)
    	{
    		return ;
    	}
		
		begin();
		
		//set load older button text
		//process for UI
        for(int i=streamList.getChildCount()-1;i>0;i--)            
        {
            View v = streamList.getChildAt(i);
            if(Button.class.isInstance(v))
            {
                Button bt = (Button)v;
                bt.setText(getString(R.string.loading_string));
                break;
            }
        }
        
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		
		facebookA.getStreamAsync(prelasttime, limitation,newpost, filter, new FacebookAdapter()
    	{
    		@Override public void getStream(List<Stream> sts)
            {
    			Log.d(TAG, "after get stream="+sts.size());
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
                if(donotcallnetwork == false)//I am still alive
                {
                    tmpStreamList.clear();
                    tmpStreamList.addAll(sts);                    
                    
                	if(sts.size() > 0)
                	{
	                	handler.post( new Runnable()
	                	{
	                		public void run()
	                		{
	                		    //this is to avoid crash
	                		    //remember the last postion
	                		    lastVisiblePos = streamList.getFirstVisiblePosition()+1;
	                			streamList.setAdapter(null);
	                			Message msg = handler.obtainMessage(FACEBOOK_STREAM_CLEAR);
	                			msg.getData().putBoolean("newpost", newpost);
	                			msg.sendToTarget();
	                		}
	                	});
                	}
                	//cancelNotify();
                	
                	/*
    				getLastViewCount(sts, newpost);
    				//just has new item will change the UI
                    if(sts.size() > 0)
                    {
                        handler.obtainMessage(FACEBOOK_STREAM_UI).sendToTarget();
                    }
                    
	            	//cancelNotify();		*/            	
                } 
                
                Message rmsg = handler.obtainMessage(FACEBOOK_STREAM_GET_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.getData().putBoolean("newpost", newpost);
                rmsg.sendToTarget();

                //rmsg = handler.obtainMessage(FACEBOOK_STREAM_FROM_GET);
                //handler.sendMessageDelayed(rmsg, orm.getFacebookStreamTimeout()*1000);
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
            	
            	Log.d(TAG, "after get stream ex="+e.getMessage());
            	if(isInAynscTaskAndStoped())
             	{
             		Log.d(TAG, "User stop passive");
             	}
             	else
             	{
	            	Message rmsg = handler.obtainMessage(FACEBOOK_STREAM_GET_END);
	                rmsg.getData().putBoolean(RESULT, false);
	                rmsg.getData().putBoolean("newpost", newpost);
	                rmsg.sendToTarget();
	                
	                //rmsg = handler.obtainMessage(FACEBOOK_STREAM_FROM_GET);
	                //handler.sendMessageDelayed(rmsg, orm.getFacebookStreamTimeout()*1000);
             	}
            }
    	});
	}
	
	private List<StreamFilter> StreamFilters = new ArrayList<StreamFilter>();
	
	@Override
    protected void loadStreamFilter() 
	{
        super.loadStreamFilter();
        
        List<StreamFilter> sd = orm.getStreamFilter();
        StreamFilters.clear();
        StreamFilters.addAll(sd);
        
        if(sd.size() == 0)
        {
            StreamFilter filter = new StreamFilter();
            filter.filter_key = "nf";
            filter.name = "News Feed";
            filter.icon_url = "";
            StreamFilters.add(0, filter);
            
            handler.obtainMessage(FACEBOOK_STREAM_FILTER_GET).sendToTarget();            
        }
        
        StreamFilter filter = new StreamFilter();
        filter.filter_key = "lf";
        filter.name = "Live Feed";
        filter.icon_url = "";
        StreamFilters.add(0, filter);
        
        //if we have data, that means we already get the filters
        Message msd = handler.obtainMessage(FACEBOOK_STREAM_FILTER_UI);
                
        msd.sendToTarget();
    }
    
    private void showFilterDialog() 
    {     
       buildDialog();       
    }
    
    private void buildDialog() 
    {
        String[] itemsArray = new String[StreamFilters.size()];
        for(int i =0 ;i < StreamFilters.size() ; i++)
        {
            itemsArray[i] = StreamFilters.get(i).name;
        }
        AlertDialog dialog = new AlertDialog.Builder(FacebookStreamActivity.this)
        .setTitle("")
        .setItems(itemsArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) 
            {
               Log.d(TAG,"entering item dalog click position is "+which);
               if(which>=0 && which<StreamFilters.size())
               {
                   StreamFilter filter = StreamFilters.get(which);
                   reloadStreamFilter( filter.filter_key, filter.name);
               }
            }
        })
        .create();
        dialog.show();
    }
    public void registerAccountListener() {
		AccountManager.registerAccountListener("FacebookStreamActivity", this);		
	}
	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("FacebookStreamActivity");		
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {
        	 Intent intent = new Intent(this, FacebookProfileActivity.class);             
             startActivity(intent);
             
             finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
