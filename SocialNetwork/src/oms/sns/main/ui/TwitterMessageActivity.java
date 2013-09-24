package oms.sns.main.ui;

import oms.sns.main.R;
import java.util.ArrayList;
import java.util.List;

import oms.sns.main.providers.SocialORM;
import oms.sns.main.providers.SocialORM.Follow;
import oms.sns.main.ui.adapter.SelectSimplyStatusAdapter;
import oms.sns.main.ui.adapter.SimplyStatusAdapter;
import oms.sns.main.ui.view.SelectSimplyStatusItemView;
import twitter4j.AsyncTwitter;
import twitter4j.Query;
import twitter4j.QueryResult;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class TwitterMessageActivity  extends StatusViewBaseActivity {
	private final String TAG="TwitterMessageActivity";
    ListView tweets;
    View     twitter_trend_search_span;
    
	//TODO	
	private  long FriendCount = 20;
		
	private List<SimplyStatus> inboxMessages = new ArrayList<SimplyStatus> ();
	private List<SimplyStatus> sendMessages = new ArrayList<SimplyStatus> ();	
	private List<Tweet>        searchTweet;
	
	private List<SimplyStatus> deleteMessages = new ArrayList<SimplyStatus>();
	String  uid;
	
	private int mTypeResIdx;
	ArrayList<Long> ids    =new ArrayList<Long> ();	
	private int nDelCount;
	private int nRecordCount;
	private Button   seachDo;
	private EditText searchEdit;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_tweets);
                
        tweets = (ListView)this.findViewById(R.id.twitter_tweets_list);
        tweets.setFocusableInTouchMode(true);
        tweets.setFocusable(true);
        tweets.setOnCreateContextMenuListener(this);    
        tweets.setOnItemClickListener(statusTweetsClickListener);
        
        twitter_trend_search_span = this.findViewById(R.id.twitter_trend_search_span);
        
        seachDo = (Button)this.findViewById(R.id.search_do);    	
    	searchEdit = (EditText)this.findViewById(R.id.embedded_text_editor);
    	//searchEdit.requestFocus();
    	seachDo.setOnClickListener(seachListener);
    	
		processIntent();
    }
    
    public synchronized boolean  isInProcess()
    {
        if(mTypeResIdx ==0 )
        {
            return inprocess;
        }
        else
        {
            return isLoadingSend;
        }
    }
    
    private void processIntent()
    {
    	SocialORM.Account account = orm.getTwitterAccount();
		uid = account.screenname;
		if(checkTwitterAccount(this, account) == true)
		{
			launchMessagesLoad(true);
		}		
    }
    
    @Override
	protected void onNewIntent(Intent intent) 
    {
    	Log.d(TAG, "new request for message view="+intent);
    	super.onNewIntent(intent);
		
		setIntent(intent);		
		processIntent();
	}
	
    public void setTitle() 
    {    
    	finalTitle = getString(MessageMenuResId[mTypeResIdx]);
    }
    
	@Override
	protected void loadRefresh()
	{
		super.loadRefresh();
		launchMessagesLoad(true);
	}
    
    View.OnClickListener seachListener = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			String key = searchEdit.getText().toString().trim();
			if(key != null && key.length()>0)
			{
			     Message msg = handler.obtainMessage(TWEET_SEARCH);			     
		         msg.getData().putString("keyword", key);		       
		       	 msg.getData().putString("to", twitterid_db);
		         handler.sendMessage(msg);
			}
			searchEdit.setText("");
		}
	};    
    
    private void showInboxUI()
    {
    	twitter_trend_search_span.setVisibility(View.VISIBLE);
    	if(inboxMessages.size()>0 )
    	{
    		SimplyStatusAdapter ta = new SimplyStatusAdapter(TwitterMessageActivity.this, (ArrayList<SimplyStatus>)inboxMessages);
        	tweets.setAdapter(ta);
    	}
    	else
    	{
    		tweets.setAdapter(null);
    	}    	
    }
    
    private void showSendBoxUI()
    {
    	twitter_trend_search_span.setVisibility(View.GONE);
    	if(sendMessages.size()>0)
    	{
    		SimplyStatusAdapter ta = new SimplyStatusAdapter(TwitterMessageActivity.this, (ArrayList<SimplyStatus>)sendMessages);
        	tweets.setAdapter(ta);
    	}
    	else
    	{
    		tweets.setAdapter(null);
    	}    
    }
    
    private void showUI()
    {
        if(inDeleteMode == false)
        {
        	twitter_action.setVisibility(View.GONE);
            if(mTypeResIdx == 1)
            {
                showSendBoxUI();
            }
            else if(mTypeResIdx == 0)
            {
                showInboxUI();
            }   
        }  	
    }
    
    private void showSearchUI()
    {
    	twitter_trend_search_span.setVisibility(View.VISIBLE);
    	//for search
    	if(searchTweet != null && searchTweet.size() > 0)
    	{
		    SimplyStatusAdapter ta = new SimplyStatusAdapter(TwitterMessageActivity.this, (ArrayList<Tweet>)searchTweet, true);
    	    tweets.setAdapter(ta);
    	}
    	else
    	{
    		tweets.setAdapter(null);
    	}
    }
    
    private void showDeleteUI(List<SimplyStatus> currentStatus)
    {
    	twitter_action.setVisibility(View.VISIBLE);
    	twitter_trend_search_span.setVisibility(View.GONE);    	
    	if(currentStatus != null && currentStatus.size()>0)
    	{
    		SelectSimplyStatusAdapter ta = new SelectSimplyStatusAdapter(TwitterMessageActivity.this, (ArrayList<SimplyStatus>)currentStatus);    		
        	tweets.setAdapter(ta);
    	}else{
    		tweets.setAdapter(null);
    	}
    }
    
    @Override public void loadAction()
    {
    	super.loadAction();    	
    	launchMessagesLoad(false);
    }
    @Override 
    protected void loadAfterSetting()
    {
        super.loadAfterSetting();
        //reset twitterA
        twitterA = null;
        launchMessagesLoad(false);
    }
    
    public void titleSelected()
    {
    	//view delete page
    	if(inDeleteMode == true)
    	{
    		twitter_action.setVisibility(View.VISIBLE);
	    	//do delete action    		
			getSelectedItems();
	    	if(nDelCount > 0)
	    	{
	    		
	    		Log.d(TAG, "last time delete is not finished, please waiting");
    			Toast.makeText(this, R.string.twitter_verify_account_wait_msg, Toast.LENGTH_SHORT).show();
    			
		    	handler.obtainMessage(MESSAGE_INBOX_DELETE).sendToTarget();
	    	}
    	}
    	else
    	{
    		twitter_action.setVisibility(View.GONE);
    	}
    }
    //callback from SelectSimplyStatusItemView
    public void setSelect(long statusid, boolean sel)
    {
    	for(int i=0;i<deleteMessages.size();i++)
    	{
			SimplyStatus v = deleteMessages.get(i);
    	    if(v.id == statusid)
    	    {
    	    	 v.selected = sel;
    	    	 break;
    	    }
    	}
    }
    protected void getSelectedItems()
    {
    	ids.clear();
		for(int i=0;i<deleteMessages.size();i++)
    	{
			SimplyStatus v = deleteMessages.get(i);
    	    if(v.selected)
    	    {
    	    	ids.add(v.id);
    	    }
    	}
		
		nRecordCount = nDelCount = ids.size();
    }
    
    @Override
    protected void loadInboxMessage()
    {
    	titleItemSelected(0,0);
    }
    
    @Override
    protected void loadOutboxMessage()
    {
    	titleItemSelected(1,0);
    }
    
    public void titleItemSelected(int position, long id)
    {
        if (position < 0) return;        
        if (mTypeResIdx == position)  return;
        refreshUIToInit(position); 
    }
    
    private void checkAndSetType(int type) 
    {
        if (type < 0 || type > MessageMenuResId.length) {
            type = mDefaultTypeIdx;
        }
        mTypeResIdx = type;        
    }
    
    void refreshUIToInit(int type)
    {
    	checkAndSetType(type);   	
    	finalTitle = getString(MessageMenuResId[mTypeResIdx]);
        setTitle(finalTitle); 
        showUI();
        callGetMessages(type);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
    {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	//no reply for send message
    	if(mTypeResIdx == 1)
    	{
    		menu.findItem(R.id.context_reply).setVisible(false);
    	}    	    	
    }
    
    @Override public boolean onPrepareOptionsMenu(Menu menu)
    {    	
    	 super.onPrepareOptionsMenu(menu);
    	 
    	 if(inDeleteMode == false)
    	 {
    	     if(mTypeResIdx == 0 )
             {
                 menu.findItem(R.id.menu_message_new).setVisible(true);
                 menu.findItem(R.id.menu_message_delete).setVisible(true);
             }
             else
             {
                 menu.findItem(R.id.menu_message_delete).setVisible(true);
             }
    	 } 
    	 else
    	 {
    	     menu.findItem(R.id.menu_message_new).setVisible(false);
             menu.findItem(R.id.menu_message_delete).setVisible(false);
    	 }
    	 return true;
    }
    
    @Override
    protected void loadNewMessage()
	{
		super.loadNewMessage();
		
	    Intent intent = new Intent(mContext, TwitterComposeActivity.class);
        intent.putExtra(TWITTER_ID,    twitterid_db); 
        intent.putExtra(DIRECT, true);
       //intent.putExtra(UPDATE, true);
        startActivityForResult(intent, TWITTER_DONOTHING);  
	}
    
    @Override
    protected void loadDeleteMessage()
    {
    	super.loadDeleteMessage();
    	
    	if(inDeleteMode == false)
    	{ 
    	    //stop process run in backgroun
    	    stopProcess();
    		ContextDeleteAction(-1);
    	}
    }
    
    //when call delete context menu
    @Override
    protected void ContextDeleteAction(long statusid) 
    {	
    	Log.d(TAG,"removed status id is ="+statusid);    	
    	inDeleteMode = true; 
    	
		if(mTypeResIdx == 0)
			deleteMessages = inboxMessages;
		else
			deleteMessages = sendMessages;
		
		//Window.setTitleDropDownAdapter(null);
		this.setTitle(R.string.context_status_delete);
		finalTitle = getString(R.string.context_status_delete);
		
		//set the select delete item
		for(int i=0;i<deleteMessages.size();i++)
    	{
    		SimplyStatus v = deleteMessages.get(i);
    		if(statusid == v.id)
    		{
    	        v.selected = true;    	        
    		}
    		else
    		{
    			//remove pre-selected
    			v.selected = false;    	       
    		}
    	}
		
		showDeleteUI(deleteMessages);
	}
    
    //TDOD, there is a bug to select all
    @Override
    protected void doSelectAll(boolean sel) 
    {	
    	//update content
    	for(int i=0;i<deleteMessages.size();i++)
    	{
    		SimplyStatus v = deleteMessages.get(i);
    	    v.selected = sel;
    	}
    	//update UI, why we need this, UI and activity have different data source
    	showDeleteUI(deleteMessages);
    }
    
	@Override
    public void createHandler() 
    {
        handler = new HandlerLoad();        
    }

    
    private void notifyLoading() 
    {
    	notify.notifyOnce(R.string.twitter_message_loading, R.drawable.twitter, 30*1000);		
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
    
	private void launchMessagesLoad(boolean forrefresh) 
	{
		if(twitterA == null)
		{
			SocialORM.Account account = orm.getTwitterAccount();
			uid = account.screenname;
			if(checkTwitterAccount(TwitterMessageActivity.this, account))
			{
				twitterA = new AsyncTwitter(account.token, account.token_secret,true);				
			}
			else
			{
				return;
			}    
		}	        
		Message msd = handler.obtainMessage(MESSAGE_DIRECT);
		msd.getData().putBoolean("forrefresh", forrefresh);
		msd.sendToTarget();
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
				case TWEET_SEARCH:
	            {
	            	seachDo.setEnabled(false);
			    	//TwitterSearchActivity.this.showDialog(DLG_TWEET_SEARCH);
			    	String key = msg.getData().getString("keyword");
			    	String to = msg.getData().getString("to");
			    	searchTrends(key, to);
			    	Log.d(TAG, "before get twee");
	            	break;
	            }
			    case TWEET_CREATE_UI:
			    {
			    	Log.d(TAG, "After get message ");
			    	int type = msg.getData().getInt("type", -1);
			    	boolean hasProgress = (type==mTypeResIdx);
			    	if(hasProgress)
			    	{
			    	    using();
			    	}
			    	showUI();
			    	break;
			    }
			    case TWEET_CONTRUCT_SEARCH_UI:
			    {
			    	using();	            	
	            	showSearchUI();
			    	break;
			    }			  
			    case TWEET_SEARCH_END:
			    {
			    	Log.d(TAG, "TWEET_SEARCH_END");
			    	end();					
				    seachDo.setEnabled(true);
			    	twitterA.resumeCallNetWork();
			    	break;
			    }
			    case MESSAGE_INBOX_DELETE:
			    {
			    	Log.d(TAG, "call FAVOR_DELETE="+ids.size());
			    	//do delete
			    	synchronized(ids)
					{
			    		begin();
		    			Log.d(TAG, "delete  message"+mTypeResIdx);
			    		for(int i=0;i<ids.size();i++)
			    		{
			    			long id = ids.get(i);
			    		    twitterA.deleteDirectMessageAsyncNoReply(id, new TwitterAdapter() 
			    	        {
			    				@Override public void deletedDirectMessage(boolean suc, long removeID)
			    	            {
			    					if(suc)
			    					{
				    					nDelCount--;
				    					Log.d(TAG, "destoryed MESSAGE ="+removeID);
				    					Message mes = handler.obtainMessage(MESSAGE_INBOX_DELETE_END);
				    					mes.getData().putLong("statusid", removeID);
				    					mes.sendToTarget();
			    					}
			    					else
			    					{
			    						Log.d(TAG, "fail destoryed MESSAGE ="+removeID);
			    					}
			    	            }
		    				    @Override public void onException(TwitterException e, int method) 
		    		            {
		    				    	nDelCount--;		    		            	
		    		            	Message mes = handler.obtainMessage(MESSAGE_INBOX_DELETE_END);
		    		            }			
			    	        });
			    		}
			    		
					}
		    		
			    	break;
			    }
			    case MESSAGE_INBOX_DELETE_END:
			    {
			    	long removedID = msg.getData().getLong("statusid", -1);
			    	if(removedID != -1)
			    	{
			    		List<SimplyStatus> currentStatus;
						if(mTypeResIdx == 0)
				    		currentStatus = inboxMessages;
				    	else
				    		currentStatus = sendMessages;
						
						synchronized(currentStatus)
						{
							for(int pos=0;pos<currentStatus.size();pos++)
							{
								if(currentStatus.get(pos).id == removedID)
								{
									currentStatus.remove(pos);
									break;
								}
							}
							showDeleteUI(currentStatus);		  
						}
						
				    	synchronized(ids)
						{
						    ids.remove((Long)removedID);
						    setTitle(String.format("%1$s/%2$s", nRecordCount-ids.size(), nRecordCount),  100*(nRecordCount-ids.size())/nRecordCount);
						}
			    	}
			    	
			    	if(nDelCount <=0)
			    	{
			    		Toast.makeText(TwitterMessageActivity.this, R.string.sns_finished_delete_message, Toast.LENGTH_SHORT).show();
			    		setTitle(R.string.context_status_delete);
			    	}
			    	break;
			    }
				case MESSAGE_SEND: 
				{
					//TwitterTweetsActivity.this.showDialog(DLG_TWEET_LOADING);					
	            	Log.d(TAG, "before get message MESSAGE_SEND");	            		            	
	            	callGetMessages(1);					
					break;
				}
				
				case MESSAGE_DIRECT: 
				{
					//TwitterTweetsActivity.this.showDialog(DLG_TWEET_LOADING);					
	            	Log.d(TAG, "before get message MESSAGE_DIRECT");	
	            	callGetMessages(mTypeResIdx);
					break;
				}
				
				case  TWEET_MESSAGE_LOAD_end:
				{
					int type = msg.getData().getInt("type", -1);
			    	boolean hasProgress = (type==mTypeResIdx);
			    	if(hasProgress && inDeleteMode ==false )
			    	{
			    		end();
			    	}
			    	
					//restore the network
				    twitterA.resumeCallNetWork();				    
					dissDlg(DLG_TWEET_LOADING);					
					break;
				}
			}			
	    }

		private void dissDlg(int dlgTweetLoading) 
		{
			//TwitterTweetsActivity.this.dismissDialog(DLG_TWEET_LOADING);
		}
	}
    
    //type=0, direct, 1 send
    private void callGetMessages(int type)
    {
    	getMessageStatuses(type);    	
    }
    boolean isLoadingSend = false;   
    private void getMessageStatuses(final int type)
    {
    	boolean hasProgress = (type==mTypeResIdx);
    	if(type == 0)
    	{  
    	    if(this.isInProcess() == true)
    	    {
    	        return;
    	    }
    	    if(type == mTypeResIdx)
    	    {
    	        begin();
    	    }
    	    notifyLoading();  
            synchronized(mLock)
            {
                inprocess = true;
            }
            
	    	twitterA.getDirectMessagesSimplyAsync(hasProgress, new TwitterAdapter() 
	    	{
	    		@Override public void gotDirectMessagesSimply(List<SimplyDirectMessage> statuses)
	            {
					synchronized(mLock)
			    	{
			    	    inprocess = false;
			    	}
					
					getFirstViewDirect(statuses,type);
                    if(donotcallnetwork == false)//I am still alive
                    {
    	            	Log.d(TAG, "After get direct message to me count="+statuses.size());    	            	
    	            	cancelNotify();
    	            	
    	            	Message msd = handler.obtainMessage(TWEET_CREATE_UI);
    	            	msd.getData().putInt("type", 0);
    	            	handler.sendMessage(msd);
                    }
                    
                    Message msd = handler.obtainMessage(TWEET_MESSAGE_LOAD_end);
                    msd.getData().putInt("type", 0);
                    msd.getData().putBoolean(RESULT, true);
                    handler.sendMessage(msd);
                    //second to load the send message
                   // handler.obtainMessage(MESSAGE_SEND).sendToTarget();	                    
	            }
	
	            @Override public void onException(TwitterException e, int method) 
	            {
	            	synchronized(mLock)
	            	{
	            	    inprocess = false;
	            	}    	
	            	
	            	Log.d(TAG, "Fail to get inbox="+e.getMessage());	
	            	if(donotcallnetwork == false )//I am still alive
	                {   
	                     cancelNotify();
	                }   
	            	if(isInAynscTaskAndStoped())
	             	{
	             		Log.d(TAG, "User stop passive");
	             	}
	             	else
	             	{
		            	Message msd = handler.obtainMessage(TWEET_MESSAGE_LOAD_end);
		            	msd.getData().putInt("type", 0);
		            	msd.getData().putBoolean(RESULT, false);
		            	handler.sendMessage(msd);
		            	
		            	//second to load the send message
		            	//handler.obtainMessage(MESSAGE_SEND).sendToTarget();
	             	}
	            }			
	    	});
    	}
    	else
    	{
    	    if(isLoadingSend == true)
            {
                return;
            }
            if(type == mTypeResIdx)
            {
                begin();
            }
            notifyLoading();  
            synchronized(mLock)
            {
                isLoadingSend = true;
            }
            
	    	twitterA.getSentDirectMessagesSimplyAsync(hasProgress,  new TwitterAdapter() 
	        {
				@Override public void gotSentDirectMessagesSimply(List<SimplyDirectMessage> statuses)
	            {
					synchronized(mLock)
			    	{
					    isLoadingSend = false;
			    	}
					
					getFirstViewSend(statuses,type);
                    if(donotcallnetwork == false)//I am still alive
                    {
    	            	Log.d(TAG, "After get send message count="+statuses.size());    	            	
    	            	cancelNotify();
    	            	Message msd = handler.obtainMessage(TWEET_CREATE_UI);
    	            	msd.getData().putInt("type", 1);
    	            	handler.sendMessage(msd);
                    }
                    
                    Message msd = handler.obtainMessage(TWEET_MESSAGE_LOAD_end);
                    msd.getData().putInt("type", 1);
                    msd.getData().putBoolean(RESULT, true);
                    handler.sendMessage(msd);
	            }
	
	            @Override public void onException(TwitterException e, int method) 
	            {
	            	synchronized(mLock)
	            	{
	            	    isLoadingSend = false;
	            	}  
	            	Message msd = handler.obtainMessage(TWEET_MESSAGE_LOAD_end);
	            	msd.getData().putInt("type", 1);
	            	msd.getData().putBoolean(RESULT, false);
	            	handler.sendMessage(msd);
	                Log.d(TAG, "Fail to get send message="+e.getMessage());	                
	            }			
	        });    
    	}
    }
	
	/*
	 * TODO
	 * construct the Simply status
	 */
	private void getFirstViewDirect(List<SimplyDirectMessage> msgs,int type)
	{
		if(msgs == null || msgs.size() == 0)
			return;
		if(type == mTypeResIdx)
		{
		    handler.post(new Runnable()
		    {

                public void run() {
                    tweets.setAdapter(null);
                }
		        
		    });
		}
	    inboxMessages.clear();			
	
		try 
		{
			for(int i=0;i<msgs.size();i++)
			{
				if(i < FriendCount)
				{
					SimplyDirectMessage msg = msgs.get(i);
			
					SimplyStatus item = new SimplyStatus();
					item.createdAt = msg.getCreatedAt();
					item.text      = msg.getText();
					item.id        = msg.getId();
					item.isFavorited = false;
					
					item.user = new SimplyUser();
					item.user.profileImageUrl = msg.getSender().getProfileImageURL();
					item.user.name = msg.getSender().getName();
					item.user.id   = msg.getSenderId();
					item.user.screenName = msg.sender_screen_name;
					
					item.user.following  = msg.getSender().following;
					item.user.notifications  = msg.getSender().notifications;
				
				    inboxMessages.add(item);										
				}
			}
		} catch (TwitterException e) {}
		
	}
	
	private void getFirstViewSend(List<SimplyDirectMessage> msgs,int type)
	{
		if(msgs == null || msgs.size() == 0)
			return;
		if(type == mTypeResIdx)
		{
		    handler.post(new Runnable()
            {

                public void run() {
                    tweets.setAdapter(null);
                }
                
            }); 
		}  
	    sendMessages.clear();
		
		try 
		{
			for(int i=0;i<msgs.size();i++)
			{
				if(i < FriendCount)
				{
					SimplyDirectMessage msg = msgs.get(i);			
					SimplyStatus item = new SimplyStatus();
					item.createdAt = msg.getCreatedAt();
					item.text      = msg.getText();
					item.id        = msg.getId();
					item.isFavorited = false;
					item.user = new SimplyUser();
					item.user.profileImageUrl = msg.getRecipient().getProfileImageURL();
					item.user.name = msg.getRecipient().getName();
					item.user.id   = msg.getRecipient().getId();
					item.user.screenName = msg.recipient_screen_name;
					
					item.user.following      = msg.getRecipient().following;
					item.user.notifications  = msg.getRecipient().notifications;
				
				    sendMessages.add(item);		
				}
			}
		} catch (TwitterException e) {}
		
	}
	
	private boolean isSendMessage(SimplyDirectMessage msg) 
	{
		return uid.equalsIgnoreCase(msg.getSenderScreenName());		
	}	
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {  
            this.stopLoading();
	        if(inDeleteMode)
	        {
	        	//don't show progress when return back to inbox or send
	        	this.setProgress(100*100);
	        	
	        	//TODO, if no action, should just load the pre-content
	        	//go back to normal mode
	        	//this.launchMessagesLoad();
	        	inDeleteMode = false;
	        	refreshUIToInit(this.mTypeResIdx);	        	
	        	return true;
	        }
        }
        	
        return super.onKeyDown(keyCode, event);
    }
    
    public void searchTrends(String key, String to) 
	{
		synchronized(mLock)
    	{
    	    inprocess        = true;
    	}
    	begin();
    	
    	Query query = new Query();
        query.setQuery(key);
        query.setTo(to);
    	twitterA.searchAcync(query, new TwitterAdapter() 
        {
			@Override public void searched(QueryResult result)
            {				
			   
		    	synchronized(mLock)
		    	{
		    	    inprocess = false;			    	    
		    	}
		    	
            	searchTweet= result.getTweets();            	
                if(donotcallnetwork == false)//I am still alive
                {
                	Log.d(TAG, "search result="+result.getTweets().size());                    	
            	    handler.obtainMessage(TWEET_CONTRUCT_SEARCH_UI).sendToTarget();            	    
        	    }
                handler.obtainMessage(TWEET_SEARCH_END).sendToTarget();
            }

            @Override public void onException(TwitterException e, int method) 
            {  
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}

            	Log.d(TAG, "Fail to search ="+e.getMessage());
            	if(isInAynscTaskAndStoped())
             	{
             		Log.d(TAG, "User stop passive");
             	}
             	else
             	{
                    handler.obtainMessage(TWEET_SEARCH_END).sendToTarget();
             	}            	    
            }			
        });  
	}

	@Override
	public void stopMyself() 
	{	    
		if(isInProcess() == true)
		{
            stoping();
		}
	}
	@Override
	public void titleUpdateAfterNetwork()
	{
		 updateProgress(DATA_READY_PROGRESS*100);
         updateTitle(TITLE_FINISH_NETWORK);
	}
	
	/********************************************
	 * Menu list adapter
	 * 
	 * ******************************************
	 */
	private static final int[] MessageMenuResId = new int[] 
    {
	   R.string.twitter_message_inbox_option,
       R.string.twitter_message_send_option,       
    };
	private static final int[] MessageMenuResIconId = new int[] 
    {
        R.drawable.inbox,
        R.drawable.send,       
    };

    private static final int mDefaultTypeIdx = 0;
}

