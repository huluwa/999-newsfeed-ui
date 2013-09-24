package oms.sns.main.ui;

import java.lang.reflect.InvocationTargetException;

import oms.sns.TwitterStatus;
import oms.sns.TwitterUser;
import oms.sns.main.R;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oms.sns.main.providers.SocialORM;
import oms.sns.main.ui.adapter.SelectSimplyStatusAdapter;
import oms.sns.main.ui.adapter.SimplyStatusAdapter;
import oms.sns.main.ui.view.SelectSimplyStatusItemView;
import oms.sns.main.ui.view.SimplyStatusItemView;
import oms.sns.main.ui.view.TwitterSelectUserItemView;
import twitter4j.AsyncTwitter;
import twitter4j.SimplyDirectMessage;
import twitter4j.SimplyStatus;
import twitter4j.SimplyUser;
import twitter4j.Tweet;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class TwitterFavoritesActivity  extends StatusViewBaseActivity {
	private final String TAG="TwitterFavoritesActivity";
	
    private ListView tweets;
    private int      prevalue = 20;
	private long     FavorCount = 20;
	private long     maxCount   = 100;
	private boolean withfootview = true;
	private int     lastVisiblePos = 0;
	private List<SimplyStatus> currentStatus = new ArrayList<SimplyStatus>();;	
		
	private String  uid;
	private ArrayList<Long> ids    = new ArrayList<Long> ();	
	private int nDelCount;
	private String myuid;
	
	
	public boolean IamOthers()
	{
		return myuid.equalsIgnoreCase(uid) == false;
	}
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_LEFT_ICON);
       
        setContentView(R.layout.twitter_tweets);             
        tweets = (ListView)this.findViewById(R.id.twitter_tweets_list);
        tweets.setFocusableInTouchMode(true);
        tweets.setFocusable(true);
        tweets.setOnCreateContextMenuListener(this);     
        tweets.setOnItemClickListener(statusTweetsClickListener);

        SocialORM.Account account = orm.getTwitterAccount();        
        myuid = uid = account.screenname;
		String tid = this.getIntent().getStringExtra(TWITTER_ID);
		if(tid != null)
		{
			//get for others
			Log.d(TAG, "get favor list for ="+tid);
			uid = tid;
		}
		
		//no need control others
		if(myuid.equalsIgnoreCase(uid))
		{
		    this.setTitle(R.string.twitter_favor_title);
		}
		else
		{
			this.setTitle("");
			finalTitle = "";
		}	
		this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon_star_full);
		if(checkTwitterAccount(this, account) == true)
		{
			launchFavoritesLoad();
		}		
    }
    
    
    @Override protected void onResume() 
    {
        super.onResume();    
        orm.setTweetViewCount(String.valueOf(FavorCount));
    }
  
    @Override protected void onPause() {
        super.onPause();   
        
        orm.setTweetViewCount(String.valueOf(prevalue));
    }
    
    @Override protected void onDestroy() 
    {   
        super.onDestroy();        
        orm.setTweetViewCount(String.valueOf(prevalue));
        
        //may cause a lot of resource
        if(currentStatus != null)
        {
            currentStatus.clear();
            currentStatus = null;
        }
        System.gc();
    }
    
    public void setTitle() 
    {	
		finalTitle = getString(R.string.twitter_favor_title);
		if(inDeleteMode == true)
    	{	
			finalTitle = getString(R.string.twitter_favor_title_unfavor);
    	}
	}
    
    //TODO performance enhancement,
    //need record in memory, then load from memory, not request from network
    //
    private void reloadFavor()
    {
    	inDeleteMode = false;
    	if(this.IamOthers() == false)
    	{
    	    setTitle(R.string.twitter_favor_title);
    	}
    	else 
    	{
    		setTitle("");
    	}
    	
		launchFavoritesLoad();
    }
    
    //callback from SelectSimplyStatusItemView
    public void setSelect(long statusid, boolean sel)
    {
    	for(int i=0;i<currentStatus.size();i++)
    	{
			SimplyStatus v = currentStatus.get(i);
    	    if(v.id == statusid)
    	    {
    	    	 v.selected = sel;
    	    	 break;
    	    }
    	}
    }
    
    public void titleSelected()
    {
    	if(false == myuid.equalsIgnoreCase(uid))
		{
    		Log.d(TAG, "others no need to control");
		    return ;
		}
		
    	
    	Log.d(TAG,"entering titleselected "+isInProcess()+"Delete mode "+inDeleteMode);
    	
    	if(this.isInProcess())
    	{
    		return;
    	}
    	
    	//view delete page
    	if(inDeleteMode == false)
    	{
	    	inDeleteMode = true;    	
	    	this.setTitle(R.string.twitter_favor_title_unfavor);
	    	
	    	if(currentStatus != null)
	    	{
	    		//set the select delete item
				for(int i=0;i<currentStatus.size();i++)
		    	{
					SimplyStatus v = currentStatus.get(i);
		    	    //remove pre-selected
		    		v.selected = false;
		    	}
	    		SelectSimplyStatusAdapter ta = new SelectSimplyStatusAdapter(TwitterFavoritesActivity.this, (ArrayList<SimplyStatus>)currentStatus);
            	tweets.setAdapter(ta);
	    	}
    	}
    	else
    	{  		
    		synchronized(ids)
    		{
	    		if(ids.size() > 0)
	    		{
	    			Toast.makeText(this, R.string.twitter_delete_favorite_msg, Toast.LENGTH_SHORT).show();	    			
	    		}
	    		else
	    		{    		
	    			ids.clear();
	    			if(currentStatus != null && currentStatus.size() > 0)
	    			{
		    			for(int i=0;i<currentStatus.size();i++)
		    	    	{
		    				SimplyStatus v = currentStatus.get(i);
		    	    	    if(v.selected)
		    	    	    {
		    	    	    	ids.add(v.id);
		    	    	    }
		    	    	}
			    		
			    		nDelCount = ids.size();
			    		
			    		if(nDelCount > 0)
			    		{
				    		setTitle(R.string.sns_sending);
				    		handler.obtainMessage(FAVOR_DELETE).sendToTarget();
			    		}
	    			}
	    		}
    		}
    	}
    }
    
    @Override
    protected void ContextDeleteAction(long statusid) 
    {	
    	Log.d(TAG,"removed status id is ="+statusid);    	
    	inDeleteMode = true; 
		this.setTitle(R.string.sns_delete);
		finalTitle = getString(R.string.sns_delete);
		//set the select delete item
		for(int i=0;i<currentStatus.size();i++)
    	{
    		SimplyStatus v = currentStatus.get(i);
    		if(statusid == v.id)
    		{
    	        v.selected = true;    	        
    		}
    		else
    		{
    			//if pre enter delete and selected items, then back, then re-enter, 
    			//we don't want to remember the pre-selected, so set them to un-selected
    			v.selected = false;
    		}
    	}
		
		showDeleteUI(currentStatus);
		
	}
    
    private void showDeleteUI(List<SimplyStatus> currentStatus)
    {  	
    	if(currentStatus != null && currentStatus.size()>0)
    	{
    		SelectSimplyStatusAdapter ta = new SelectSimplyStatusAdapter(TwitterFavoritesActivity.this, (ArrayList<SimplyStatus>)currentStatus);    		
        	tweets.setAdapter(ta);
    	}else{
    		tweets.setAdapter(null);
    	}
    }
    
    @Override
    protected void doSelectAll(boolean sel) 
    {	
    	for(int i=0;i<currentStatus.size();i++){
    		SimplyStatus v = currentStatus.get(i);
    	    v.selected = sel;
    	}  	
    	showDeleteUI(currentStatus);
	}
    
	@Override
    public void createHandler() 
    {
        handler = new HandlerLoad();        
    }
    
    @Override public void loadAction()
    {
    	super.loadAction();
    	
    	launchFavoritesLoad();
    }
    @Override
    protected void loadAfterSetting()
    {
        super.loadAfterSetting();
        //reset twitterA
        twitterA = null;
        launchFavoritesLoad();
    }
    
    @Override
    protected void loadRefresh(){
        curTwitterPage = 1;
        
        //clear data
        tweets.setAdapter(null);
        currentStatus.clear();
        
    	launchFavoritesLoad();
    }
    
    private void notifyLoading() 
    {
    	notify.notifyOnce(R.string.twitter_tweets_loading, R.drawable.twitter, 30*1000);		
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        switch(requestCode)
        {
            case TWEET_DETAIL:
            {
            	//TODO
            	//return from tweets details
            	
            	Log.d(TAG, "return from detail view");
            	break;
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
    
	private void launchFavoritesLoad() 
	{
		if(twitterA == null)
		{
			SocialORM.Account account = orm.getTwitterAccount();
			uid = account.screenname;
			if(checkTwitterAccount(TwitterFavoritesActivity.this, account))
			{
				twitterA = new AsyncTwitter(account.token, account.token_secret,true);				
			}
			else
			{
				return;
			}    
		}	        
		handler.obtainMessage(TWEET_LOAD_BEGIN).sendToTarget();
	}
   
    private class HandlerLoad extends Handler 
	{
		public HandlerLoad()
		{
			super();
			
			Log.d(TAG, "new HandlerLoad");
		}
		
		@Override
		public void handleMessage(Message msg) 
		{
			switch(msg.what)
			{			    
			    case TWEET_CREATE_UI:
			    {
			    	setUIReadyProgress();
			    	TwitterFavoritesActivity.this.setTitle(TITLE_CONSTRUCT_UI);
					
			    	Log.d(TAG, "After get favor status");
			    	if(currentStatus != null)
			    	{ 
			    	    SimplyStatusAdapter ta = new SimplyStatusAdapter(TwitterFavoritesActivity.this, (ArrayList<SimplyStatus>)currentStatus);
			    	    ta.withfootview = withfootview;
			    	    tweets.setAdapter(ta);
			    	    tweets.setSelection(lastVisiblePos);
			    	}
			    	else
			    	{
			    	    Log.e(TAG, "why come here, TWEET_CREATE_UI");
			    	}
			    	break;
			    }
			    case FAVOR_DELETE:
			    {			    	
			    	Log.d(TAG, "call FAVOR_DELETE="+ids.size());
			    	//do delete
			    	synchronized(ids)
					{
			    		for(int i=0;i<ids.size();i++)
			    		{
			    			long id = ids.get(i);
			    		    twitterA.destroyFavoriteSimplyAsyncNoReturn(id, new TwitterAdapter() 
			    	        {
			    				@Override public void destroyedFavoriteSimply(boolean suc, long removeID)
			    	            {
			    					//remove from the view
			    					if(suc)
			    					{
				    					Message mes = handler.obtainMessage(FAVOR_DELETE_END);
				    					mes.getData().putLong("statusid", removeID);
				    					mes.sendToTarget();
			    					}
			    					else
			    					{
			    						Log.d(TAG, "fail destoryed message ="+removeID);
			    					}
			    	            }
		    				    @Override public void onException(TwitterException e, int method) 
		    		            {
		    		            	processTwitterException(e, method);
		    		            }			
			    	        });
			    		}
					}
		    		
		    		//enter favor UI
			    	break;
			    }
				case TWEET_LOAD_BEGIN: 
				{
					//TwitterTweetsActivity.this.showDialog(DLG_TWEET_LOADING);					
	            	Log.d(TAG, "before get favor TWEET_LOAD_BEGIN");
	            	callGetFavor(-1);       	        
					
					break;
				}
				case  FAVOR_DELETE_END:
				{
					long removeID = msg.getData().getLong("statusid", -1);
					Log.d(TAG, "destoryed favor="+removeID);
					if(removeID != -1)
					{
						synchronized(currentStatus)
						{
							for(int pos=0;pos<currentStatus.size();pos++)
							{
								if(currentStatus.get(pos).id == removeID)
								{
									currentStatus.remove(pos);
									break;
								}
							}
							SelectSimplyStatusAdapter ta = new SelectSimplyStatusAdapter(TwitterFavoritesActivity.this, (ArrayList<SimplyStatus>)currentStatus);    		
				        	tweets.setAdapter(ta);
						}
						synchronized(ids)
						{
						    ids.remove((Long)removeID);
						    setTitle(String.format("%1$s/%2$s", nDelCount-ids.size(), nDelCount),  100*(nDelCount-ids.size())/nDelCount);
						}
					}
					
					synchronized(ids)
		    		{
			    		if(ids.size() == 0)
			    		{	
			    			setTitle(getString(R.string.sns_finished_msg), 100);
			    			Toast.makeText(TwitterFavoritesActivity.this, R.string.sns_finished_msg, Toast.LENGTH_SHORT).show();
			    			
			    			reloadFavor();
			    		}
		    		}	
					break;
				}
				case  TWEET_LOAD_end:
				{    
				    end();	
					//restore the network
					if(twitterA != null)
				        twitterA.resumeCallNetWork();
				    
					dissDlg(DLG_TWEET_LOADING);		
					showFooterViewText(getString(R.string.load_older_msg));
					if(IamOthers() == true)			    	
			    	{
			    		setTitle("");
			    	}
					break;
				}
				case TWEET_UNFAVOR_END:
				{
					boolean retvalue = msg.getData().getBoolean(RESULT);
					
					if(retvalue){
						long statusid = msg.getData().getLong(STATUS_ID);
						for(int i = 0; i < currentStatus.size(); i++){
						    if(currentStatus.get(i).id == statusid){
						    	currentStatus.remove(i);
						    }
						}
					}
					
					if(currentStatus.size()>0){
					  SimplyStatusAdapter ta = new SimplyStatusAdapter(TwitterFavoritesActivity.this, (ArrayList<SimplyStatus>)currentStatus);
	            	  ta.withfootview = withfootview;
					  tweets.setAdapter(ta);   
					}else{
					  tweets.setAdapter(null);
					}
					break;
				}
			}			
	    }

		private void dissDlg(int dlgTweetLoading) 
		{
			//TwitterTweetsActivity.this.dismissDialog(DLG_TWEET_LOADING);
		}
	}
    
    private void callGetFavor(long fromtime)
    {
        begin();
	
    	Log.d(TAG, "before get tweets callGetTweets");
    	notifyLoading();  
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}
    	showFooterViewText(getString(R.string.loading_string));
    	getFavorities();    	
    }
    
    private void showFooterViewText(String text) {
        if(tweets == null) return;
         for(int i= tweets.getChildCount()-1;i>0;i--)            
         {
             View v = tweets.getChildAt(i);
             if(Button.class.isInstance(v))
             {
                 Button bt = (Button)v;
                 bt.setText(text);
                 break;
             }
         } 
     }
     
    
    @Override
    protected boolean hasMore()
    {
        if(reachlastpage == true)
            return false;
        
        if(currentStatus != null && currentStatus.size() > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    protected boolean isTheFirst()
    {
        return this.curTwitterPage <= 1;
    }
    
    protected void loadNextPage()
    {
        super.loadNextPage();   
        launchFavoritesLoad();
    }
    
    protected void loadPrePage()
    {
        super.loadPrePage();   
        launchFavoritesLoad();
    }
    
    private void getFavorities()
    {
    	twitterA.favoritesSimplyAsync(uid, curTwitterPage, new TwitterAdapter() 
        {
			@Override public void gotFavoritesSimply(List<SimplyStatus> statuses)
            {
				Log.d(TAG, "After get favorites count="+statuses.size());
				
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
				if(statuses.size() == 0)
                {
                    reachlastpage = true;   
                    if(curTwitterPage > 1)
                    {
                        curTwitterPage = curTwitterPage -1;
                    }                    
                }
				
                if(donotcallnetwork == false)//I am still alive
                {
	            	cancelNotify();	            	
                }
                
            	getFirstViewStatus(statuses);            	
            	handler.obtainMessage(TWEET_CREATE_UI).sendToTarget();  

                handler.obtainMessage(TWEET_LOAD_end).sendToTarget();
				//TODO
				//save the favorites to database or file system
				//
            }

            @Override public void onException(TwitterException e, int method) 
            {
                try {
                    if(failCallMethod!=null)
                       failCallMethod.invoke(TwitterFavoritesActivity.this, (Object[])null);
                } catch (IllegalArgumentException e1) {                     
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {                       
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {                        
                    e1.printStackTrace();
                }       
                
            	processTwitterException(e, method);
            }			
        });    
    }
    private void processTwitterException(TwitterException e, int method)
    {
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
    	    handler.obtainMessage(TWEET_LOAD_end).sendToTarget();
     	}
    	
	    
        if(donotcallnetwork == false)//I am still alive
        {            	
        	cancelNotify();
        	Log.d(TAG, "Fail to get ="+e.getMessage());
        }
        	    
    }
    
    /*
     * why can't delete directly, if clear the data, 
     * and click view, will cause force close
     */
	@SuppressWarnings("unchecked")
	private void getFirstViewStatus(List<SimplyStatus> statuses)
	{
	    
		Log.d(TAG, "After get favor status="+this);	  
		if(tweets.getFirstVisiblePosition()>0)
		{
		    lastVisiblePos = tweets.getFirstVisiblePosition()+1;
		}
    	handler.post( new Runnable()
        {
            public void run()
            {
                tweets.setAdapter(null);
            }
         });
    	
        synchronized(currentStatus)
    	{
            
            for(int i=0;i<statuses.size();i++)
            {
                while(currentStatus.size() >=maxCount && currentStatus.size()>0)
                {
                    //remove it
                    SimplyStatus st = currentStatus.get(currentStatus.size()-1);
                    st = null;
                    
                    currentStatus.remove(currentStatus.size()-1);
                }           
                currentStatus.add(statuses.get(i));         
            }	    	 
            statuses.clear();
            statuses = null;
    	}        
	}
    
	void refreshUIToInit(int type)
	{	  	
		if(this.IamOthers() == false)
    	{
    	    setTitle(R.string.twitter_favor_title);
    	}
    	else 
    	{
    		setTitle("");
    	}
	    
	    if(currentStatus != null)
    	{
    		SimplyStatusAdapter ta = new SimplyStatusAdapter(TwitterFavoritesActivity.this, (ArrayList<SimplyStatus>)currentStatus);
        	ta.withfootview = withfootview;
    		tweets.setAdapter(ta);
    	}
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {    
        	
            this.stopProcess();            
            if(inDeleteMode)
            {
            	//go back to normal mode
            	//this.launchFavoritesLoad();
            	refreshUIToInit(0);
            	inDeleteMode = false;
            	return true;
            }
            else
            {
            	 if(currentStatus != null)
                 {
                     currentStatus.clear();
                     currentStatus = null;
                 }            
            }
            
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
   
	@Override
	public void stopMyself() 
	{	    
		if(inprocess == true)
		{
            stoping();
		}
		else
		{
			handler.obtainMessage(TWEET_LOAD_end).sendToTarget();
		}
	}	
	
	public View.OnClickListener loadOlderClick = new View.OnClickListener()
	{
	    public void onClick(View v) 
	    {
	        Log.d(TAG, "load older message");               
	        loadOldPost();
	    }
	};
	
	protected void loadOldPost() 
    {
        nextPage();
    }
}
