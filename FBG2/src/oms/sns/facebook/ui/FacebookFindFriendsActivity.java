package oms.sns.facebook.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import oms.sns.facebook.R;
import oms.sns.facebook.providers.SocialORM;
import oms.sns.facebook.providers.SocialORM.PeopleMapFacebook;
import oms.sns.facebook.service.dell.AsyncOmsService;
import oms.sns.facebook.service.dell.ContactHelper;
import oms.sns.facebook.service.dell.ContactID;
import oms.sns.facebook.service.dell.OmsServiceAdapter;
import oms.sns.facebook.ui.adapter.FacebookFindFriendsAdapter;
import oms.sns.facebook.ui.adapter.FacebookLookupAdapter;
import oms.sns.facebook.ui.adapter.FacebookPhoneBookAdapter;
import oms.sns.facebook.ui.view.FacebookFindFriendItemView;
import oms.sns.facebook.ui.view.FacebookPhoneBookItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.FriendRelationship;
import oms.sns.service.facebook.model.PhoneBook;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
public class FacebookFindFriendsActivity extends FacebookBaseActivity
{
    private final String TAG="FacebookFindFriendsActivity";
    
	private ListView   friendList;
	private View       searchSpan;
	private MyWatcher  watcher;
	
	private List<PeopleMapFacebook> friends = new ArrayList<PeopleMapFacebook>();
	private List<PeopleMapFacebook> searchResult = new ArrayList<PeopleMapFacebook>();
	private List<PhoneBook> lookupResult = new ArrayList<PhoneBook>();	
	
	AsyncOmsService asyncOms;
	private EditText keyEdit;
	
	private View       lookupSpan;
	private EditText   email;
	private EditText   phone;
	private EditText   username;
    private Button     facebook_search_button;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);        
        Intent intent = this.getIntent();    
        setContentView(R.layout.facebook_find_friends_ui);
        friendList = (ListView)this.findViewById(R.id.facebook_wall_list);   
        friendList.setFocusableInTouchMode(true);
        friendList.setFocusable(true);
        friendList.setOnCreateContextMenuListener(this);
        friendList.setOnItemClickListener(listItemClickListener);        
        
        searchSpan = this.findViewById(R.id.facebook_search_span);
        searchSpan.setVisibility(View.VISIBLE);
        keyEdit = (EditText)this.findViewById(R.id.embedded_text_editor);
        watcher = new MyWatcher();         
        keyEdit.addTextChangedListener(watcher);
        searchSpan.setVisibility(View.GONE);
        
        lookupSpan = this.findViewById(R.id.facebook_lookup_span);        
        email    =  (EditText)this.findViewById(R.id.embedded_lookup_text_editor);
        phone    =  (EditText)this.findViewById(R.id.embedded_lookup_phone_editor);  
        username  =  (EditText)this.findViewById(R.id.embedded_lookup_name_editor);  
        lookupSpan.setVisibility(View.VISIBLE);
        
        facebook_search_button = (Button)this.findViewById(R.id.facebook_search_button);
        facebook_search_button.setOnClickListener(lookupFriendClick);
        
        setTitle(R.string.facebook_find_friends);
        SocialORM.Account account = orm.getFacebookAccount();        
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);
	        	//if session is null?
	    		asyncOms = new AsyncOmsService(loginHelper.constructPermSession());	        	
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }        
    }
	
	View.OnClickListener lookupFriendClick = new View.OnClickListener()
	{
		public void onClick(View arg0) 
		{			
			lookupFacebookFriends();
			hideInputKeyBoard(arg0);
		}		
	};
	
	/*
    @Override
	public void titleSelected() 
    {		
		super.titleSelected();
		//restore the title
		end();
		
		if(this.amLookupContact)
		{
			this.backToSearchFriends();
		}
		else
		{
			lookupFacebookFriends();
		}
	}*/
	private void lookupFacebookFriends()
    {
		String emailString    = email.getText().toString().trim();
		String phoneString    = phone.getText().toString().trim();
		String usernameString = username.getText().toString().trim();
		
		if(isEmpty(emailString) == true && isEmpty(phoneString) == true && isEmpty(usernameString) == true )
		{
		    Toast.makeText(mContext, R.string.facebook_find_people_prompt, Toast.LENGTH_SHORT).show();
			return;
		}
		
    	if(this.isInProcess() == true)
    	{
    		Log.d(TAG, "I am lookuping contacts");
    		dismissDialog(DLG_FIND_PEOPLE);
    		showDialog(DLG_FIND_PEOPLE);
    		return;
    	}
    	
    	Log.d(TAG, "before get lookup Friends email= "+emailString + " phone="+phoneString + " user name="+usernameString);    	
    	//TODO each for specific items
    	JSONArray jsonArray   = new JSONArray();
    	
		try{
			JSONObject email = new JSONObject();	
			if(emailString.indexOf("@")>0){
				email.put("email", emailString);
				jsonArray.put(email);
			}
			
			if(isPhoneNumber(phoneString))
			{
				JSONObject cell = new JSONObject();			
				cell.put("cell", phoneString);
				jsonArray.put(cell);
				
				/*
				JSONObject other = new JSONObject();			
				other.put("other_phone", phoneString);
				jsonArray.put(other);
				*/	
			}
			
			if(isUserName(usernameString) == true)
			{
				JSONObject name = new JSONObject();			
				name.put("name", usernameString);
				jsonArray.put(name);
			}
		}
		catch(JSONException e)
		{
			Log.d(TAG, "create  jsonobject exception "+e.getMessage());
		}	    
		String entries = jsonArray.length()>0?jsonArray.toString():"";		
		Log.d(TAG, "entries is "+entries);
		
    	if(entries.length() > 0)
    	{   	
        	begin();   
        	facebook_search_button.setEnabled(false);
        	showDialog(DLG_FIND_PEOPLE);
        	synchronized(mLock)
        	{
        	    inprocess = true;        	    
        	}
        	
			Log.d(TAG, "lookupFacebookUserFromContacts entries is ==="+entries);		
			ArrayList<Long> tids = new ArrayList<Long>();
			
			asyncOms.phoneLookupAsync(tids, entries, new OmsServiceAdapter()
			{
				public void phoneLookup(List<Long> tids, List<PhoneBook> phones)
				{
					synchronized(mLock)
			    	{
			    	    inprocess = false;			    	    
			    	}
					
					lookupResult.clear();
					lookupResult.addAll(phones);					
					handler.obtainMessage(FACEBOOK_LOOKUP_KEY_UI).sendToTarget();
					//handler.obtainMessage(FACEBOOK_LOOKUP_KEY_END).sendToTarget();
				}
				public void onException(FacebookException te, int method, Object[] args)
				{
					synchronized(mLock)
			    	{
			    	    inprocess = false;			    	    
			    	}
					
					Log.d(TAG, "fail to look up="+te.getMessage());
					if(isInAynscTaskAndStoped())
	            	{
	            		Log.d(TAG, "User stop passive");
	            	}
	            	else
	            	{
	            		lookupResult.clear();					
						handler.obtainMessage(FACEBOOK_LOOKUP_KEY_UI).sendToTarget();
					    //handler.obtainMessage(FACEBOOK_LOOKUP_KEY_END).sendToTarget();
	            	}
				}
			});
    	}
    	else
    	{
    	    Toast.makeText(mContext, R.string.facebook_find_people_prompt, Toast.LENGTH_SHORT).show(); 
    	}
    }
	
	private boolean isPhoneNumber(String searchstring) 
	{

		if(isEmpty(searchstring) == true)
			return false;
		
		return true;
	}
	private boolean isUserName(String username) 
	{
		if(isEmpty(username) == true)
			return false;
		
		return true;
	}
	//UI process
	private void backToSearchFriends()
	{
		Log.d(TAG, "backToSearchFriends");
		amLookupContact = false;
		//update UI
		lookupSpan.setVisibility(View.VISIBLE);
		searchSpan.setVisibility(View.GONE);
		
		//remove content from ListView
		friendList.setAdapter(null);
	}
	
	private boolean amLookupContact =false;
    protected void lookupContact()
    {
    	amLookupContact = true;
    	Log.d(TAG, "lookupContact");
    	lookupSpan.setVisibility(View.GONE);
		searchSpan.setVisibility(View.VISIBLE);
		
    	lauchLookup();
    }	    
	private void doSearch(String key)
	{
        searchResult.clear();        
        if(friends != null && key != null && key.length()>0)
        {
            for(int i=0;i<friends.size();i++)
            {
            	PeopleMapFacebook user = friends.get(i);
                if(user.Name != null && user.Name.toLowerCase().indexOf(key.toLowerCase())>=0)
                {
                    searchResult.add(user);
                }
            }
            //show UI
            //refresh the UI
            FacebookFindFriendsAdapter fa = new FacebookFindFriendsAdapter(FacebookFindFriendsActivity.this, searchResult);
        	friendList.setAdapter(fa);  
        }
        else
        {
        	FacebookFindFriendsAdapter fa = new FacebookFindFriendsAdapter(FacebookFindFriendsActivity.this, friends);
        	friendList.setAdapter(fa);  
        }
    }

	public void setTitle() 
	{
	 	title = getString(R.string.facebook_find_friends);		
	}
  
	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			Log.d(TAG, "listItemClickListener  you click first one="+v);	
			//TODO load contact UI give the people id	
			if(FacebookFindFriendItemView.class.isInstance(v))
			{
				FacebookFindFriendItemView view = (FacebookFindFriendItemView)v;				
				Uri peopleuri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, view.getPeopleID());
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(peopleuri);
				((FacebookBaseActivity)(mContext)).startActivity(intent);
			}
			else if(FacebookPhoneBookItemView.class.isInstance(v))
			{
				 Intent intent = new Intent(mContext, FacebookAccountActivity.class);
				 FacebookPhoneBookItemView pv = (FacebookPhoneBookItemView)v;
                 intent.putExtra("uid",      pv.getFromUID());
                 FacebookUser user = orm.getFacebookUser(pv.getFromUID());
                 if(user != null)
                 {
                	 intent.putExtra("username", user.name);
                     intent.putExtra("imageurl", user.pic_square);
                 }
                 else
                 {
                	 intent.putExtra("username", pv.getPhoneBook().username);
                	 intent.putExtra("imageurl", pv.getImageUrl());
                 }
                                    
                 ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
			}
		}
	};

	@Override
	protected void requestPhoneNumber(long fuid) 
	{
		if(facebookA == null)
		{
			Log.d(TAG, "facebook async is null");
			return ;
		}
		else
		{
			begin();
			synchronized(mLock)
	    	{
	    	    inprocess = true;
	    	}
			facebookA.requestPhoneNunberAsync(fuid,new FacebookAdapter()
	    	{
	    		@Override public void requestPhoneNumber(boolean suc)
	            {
					synchronized(mLock)
			    	{
			    	    inprocess = false;
			    	}
					Log.d(TAG, "request phone number sent");
					handler.obtainMessage(FACEBOOK_FRIENDS_PHONE_NUMBER_END).sendToTarget();
	            }
	    		
	            @Override public void onException(FacebookException e, int method) 
	            {
	            	synchronized(mLock)
			    	{
			    	    inprocess = false;
			    	}
	            	Log.d(TAG, "fail to request phone number sent="+e.getMessage());
	            	
	            	if(isInAynscTaskAndStoped())
	            	{
	            		Log.d(TAG, "User stop passive");
	            	}
	            	else
	            	{
	            	    handler.obtainMessage(FACEBOOK_FRIENDS_PHONE_NUMBER_END).sendToTarget();
	            	}
	            }
	    	});
		}
	}
	
		
    @Override
	protected void createHandler() 
    {
		handler = new MainHandler();		
	}
    
    //try to get lookup message
    private void lauchLookup()
    {
    	handler.post( new Runnable()
    	{
    		public void run()
    		{
		    	Message msd = handler.obtainMessage(FACEBOOK_FRIENDS_LOOKUP_GET);		    	
				SyncAddressBookHelper.checkIsEnableAddressbookSync(mContext,orm, msd);
    		}
    	});
    }
    
    //reget the wall
    @Override
    protected void loadRefresh()
    {
    	handler.post( new Runnable()
    	{
    		public void run()
    		{
		    	if(isInProcess() == false)
		    	{
		    		//clear the cache?
		    		new File(ContactHelper.entrys.filePath).delete();
		    		
		    	    lookupFacebookUserFromContacts();
		    	}
		    	else
		    	{
		    		Log.d(TAG, "you are geting the look up, pelease wait");
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
    		launchFacebookLogin();
    	}
    	else
    	{
    		facebookA = new AsyncFacebook(perm_session);
    		perm_session.attachActivity(this);
    		
    		lauchLookup();
    	}
    }
    
    //context menu, post to wall
    @Override
    protected void postToWall(long uid)
    {
    	/*
    	Intent intent = new Intent(this, FacebookWallPostActivity.class);
    	intent.putExtra("posttouid", uid);
    	startActivity(intent);
    	*/
    	Intent wallintent = new Intent(this,FacebookStatusUpdateActivity.class);
		wallintent.putExtra("fuid", new Long(uid));
		startActivity(wallintent);
    }
        
    final static int FACEBOOK_FRIENDS_LOOKUP_GET  =0;
    final static int FACEBOOK_FRIEND_UI      =1;
    final static int FACEBOOK_FRIENDS_LOOKUP_END=2;
    final static int FACEBOOK_FRIENDS_LOOKUP_DB =3;
    final static int FACEBOOK_FRIEND_SAVE_DB    =4;
    final static int FACEBOOK_FRIENDS_LOOKUP_QUERY=5;
    final static int FACEBOOK_FRIENDS_PHONE_NUMBER_END=6;
    final static int FACEBOOK_LOOKUP_KEY_UI     = 7;
    final static int FACEBOOK_LOOKUP_KEY_END    = 8;
    final static int FRIENDSHIP_LOOKUP_END      = 9;
    final static int FACEBOOK_FRIEND_UI_END     = 10;
    
    private class MainHandler extends Handler 
    {
        public MainHandler()
        {
            super();            
            Log.d(TAG, "new lookup MainHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case FACEBOOK_LOOKUP_KEY_UI:
                {
                	if(lookupResult.size()>0)
		        	{
		        		//begin to get whether they are friend
		        	    areFriendshipForLookupSpecific();
		        	}
		        	else
		        	{
		        	    Toast.makeText(mContext, R.string.facebook_find_friend_no_result, Toast.LENGTH_SHORT).show();
		        	    friendList.setAdapter(null);
		        	    end();
		        	    dismissDialog(DLG_FIND_PEOPLE);
		        	    facebook_search_button.setEnabled(true);
		        	}
		        	
                	break;
                }      
                case FRIENDSHIP_LOOKUP_END:
                {
                	if(lookupResult != null && lookupResult.size() > 0)
                	{
                	    FacebookLookupAdapter sa = new FacebookLookupAdapter(FacebookFindFriendsActivity.this, lookupResult,true);
		        	    friendList.setAdapter(sa);
                	}
                	else
                	{
                		friendList.setAdapter(null);
                	}
                	dismissDialog(DLG_FIND_PEOPLE);
                	facebook_search_button.setEnabled(true);
	        	    end();
                	break;
                }
                case FACEBOOK_LOOKUP_KEY_END://just for exception
                {
                	end();
                	break;
                }              
            	case FACEBOOK_FRIENDS_LOOKUP_GET:                	
                {
                	if(msg.getData().getBoolean("RESULT", false) == true)
                	{
                		Log.d(TAG, "user agree to sync with Facebook");
                	    lookupFacebookUserFromContacts();
                	}
                	else
                	{
                		Log.d(TAG, "user don't want to sync with Facebook");
                	}
                	break;
                }

                case FACEBOOK_FRIEND_UI:
                {
                    //for back key, when back from lookup all, the task is still running, it will have effect on UI, so ignore
                	if(amLookupContact == false)
                	{	
                		return;
                	}
                	
                	//begin to get whether they are friend
                	if(friends != null && friends.size() > 0)
                	{
                        areFriendshipForLookup();
                	}
                	else
                	{
                		end();
                	}
                	break;
                } 
                case FACEBOOK_FRIEND_UI_END:
                {
                	if(friends != null && friends.size() > 0)
                	{
	                	FacebookFindFriendsAdapter fa = new FacebookFindFriendsAdapter(FacebookFindFriendsActivity.this, friends);
	                	friendList.setAdapter(fa);
                	}
                	else
                	{
                		friendList.setAdapter(null);
                	}
                	
                	end();
                	break;
                }                
                case FACEBOOK_FRIENDS_LOOKUP_DB:
                {
                	if(amLookupContact == false)
                		return;
                	
                	friends = orm.getPeopleMapFacebooks();                	
                	handler.obtainMessage(FACEBOOK_FRIEND_UI).sendToTarget();
                	handler.obtainMessage(FACEBOOK_FRIENDS_LOOKUP_END).sendToTarget();
                	break;
                }
                case FACEBOOK_FRIENDS_PHONE_NUMBER_END:
                case FACEBOOK_FRIENDS_LOOKUP_END:
                {
                	if(amLookupContact == false)
                		return;
                	
                	end();
                	setTitle(R.string.facebook_find_friends);
                	break;
                }      
                case FACEBOOK_FRIEND_SAVE_DB:
                {
                	break;
                }
                case FACEBOOK_FRIENDS_LOOKUP_QUERY:
                {
                	using();
                	break;
                }
            }
        }
    }
    private void areFriendshipForLookupSpecific()
    {
        List<Long> uids = new ArrayList<Long>();
        if(lookupResult!=null && lookupResult.size()>0){
            for(PhoneBook user : lookupResult){
                uids.add(user.uid);
            }
        }
        
        if(uids.size() == 0)
        {
        	end();
            return;
        }
        
        facebookA.areFriendsAsync(uids,new FacebookAdapter()
        {
            @Override public void areFriends(List<FriendRelationship> frs )
            {
                for(FriendRelationship fr: frs)
                {
                    if(fr.isFriends == false)
                        continue;
                    
                    for(int i = 0;i<lookupResult.size();i++)
                    {                   
                        PhoneBook user = lookupResult.get(i);
                        if(user.isFriend == false && user.uid == fr.uid2)
                        {
                            user.isFriend = true;               
                            break;
                        }
                    }               
                }        
                
                Log.d(TAG, "after get friends ship ");
                handler.obtainMessage(FRIENDSHIP_LOOKUP_END).sendToTarget();
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
            	handler.obtainMessage(FRIENDSHIP_LOOKUP_END).sendToTarget();
                Log.d(TAG, "get are friendship fail="+e.getMessage());     
            }
        });
    }
    
    private void areFriendshipForLookup()
    {
    	List<Long> uids = new ArrayList<Long>();
		if(friends!=null && friends.size()>0){
			for(PeopleMapFacebook user : friends){
				uids.add(user.uid);
			}
		}
		if(uids.size() == 0)
		{
		    return;
		}
		
		facebookA.areFriendsAsync(uids,new FacebookAdapter()
    	{
			@Override public void areFriends(List<FriendRelationship> frs )
            {
    			for(FriendRelationship fr: frs)
				{
					if(fr.isFriends == false)
						continue;
					
					for(int i = 0;i<friends.size();i++)
					{					
					    PeopleMapFacebook user = friends.get(i);
						if(user.isFriend == false && user.uid == fr.uid2)
						{
							user.isFriend = true;				
							break;
						}
					}				
				}
    			
    			 Log.d(TAG, "after get friends ship ");
                 handler.obtainMessage(FACEBOOK_FRIEND_UI_END).sendToTarget();
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	Log.d(TAG, "get are friends fail");
            	final String error_msg = e.getMessage();
            	handler.post(new Runnable()
        		{
        			public void run()
        			{
        				Toast.makeText(mContext, getString(R.string.facebook_lookupcontact_failed)+"\n"+error_msg, Toast.LENGTH_SHORT).show();
        			}
        		});
            	handler.obtainMessage(FRIENDSHIP_LOOKUP_END).sendToTarget();
            }
    	});
    }
    
    boolean insaveing=false;
    private void lookupFacebookUserFromContacts()
    {
    	if(insaveing == true)
    	{
    		Log.d(TAG, "I am lookup contacts");
    		Toast.makeText(FacebookFindFriendsActivity.this, R.string.facebook_lookup_waitting, Toast.LENGTH_LONG).show();
    		return;
    	}
    	
    	Log.d(TAG, "before get lookupFacebookUserFromContacts");    	
    	new Thread( new Runnable()
    	{
    		public void run()
    		{
    			//notifyLoading();  
		    	synchronized(mLock)
		    	{
		    	    inprocess = true;
		    	    insaveing = true;
		    	}
		    	
		    	prepare();
		    	//TODO each for specific items		    	
		    	String entries = ContactHelper.createLookupEntriesFromCache(FacebookFindFriendsActivity.this.getApplicationContext());		    	
		    	begin();
		    	
		    	if(entries.length() > 0)
		    	{    			
					Log.d(TAG, "lookupFacebookUserFromContacts entries is ==="+entries);		
					ArrayList<Long> tids = new ArrayList<Long>();
					
					asyncOms.phoneLookupAsync(tids, entries, new OmsServiceAdapter()
					{
						public void phoneLookup(List<Long> tids, List<PhoneBook> phones)
						{
							synchronized(mLock)
					    	{
					    	    inprocess = false;
					    	    insaveing = false;
					    	}
							
							//save database, after UI,
							processLookup(phones);
							
							//TODO
							//check the return Facebook whether is our friends;
							//handler.obtainMessage(FACEBOOK_FRIENDS_LOOKUP_QUERY).sendToTarget();
							handler.obtainMessage(FACEBOOK_FRIEND_UI).sendToTarget();
							
							//don't do this
							//handler.obtainMessage(FACEBOOK_FRIENDS_LOOKUP_END).sendToTarget();
						}
						public void onException(FacebookException te, int method, Object[] args)
						{
							final String error_msg = te.getMessage();
							synchronized(mLock)
					    	{
					    	    inprocess = false;
					    	    insaveing = false;
					    	}
							
							Log.d(TAG, "fail to look up="+te.getMessage());
							
							if(isInAynscTaskAndStoped())
			            	{
			            		Log.d(TAG, "User stop passive");
			            	}
			            	else
			            	{
			            		handler.post(new Runnable()
			            		{
			            			public void run()
			            			{
			            				Toast.makeText(mContext, getString(R.string.facebook_lookupcontact_failed)+"\n"+error_msg, Toast.LENGTH_SHORT).show();
			            			}
			            		});
								handler.obtainMessage(FACEBOOK_FRIENDS_LOOKUP_DB).sendToTarget();
								handler.obtainMessage(FACEBOOK_FRIENDS_LOOKUP_END).sendToTarget();
			            	}
						}
					});
		    	}
		    	else
		    	{
		    		end();		    		
		    		Log.d(TAG, "you have no contacts");
		    	}
    		}
    	}).start();
    	
    }
    
    //TODO to enhance the performance
    //get the ContactID from cache data
    protected void processLookup(List<PhoneBook> phones) 
    {
    	if(phones==null || phones.size() ==0)
		{
			return;			
		}
    	
    	friends.clear();   	
    	Log.d(TAG, "method= processLookup Find friend size = " + phones.size());
	    for(PhoneBook phonebook : phones)
		{
		   // Log.d(TAG, "phonebook="+phonebook);
		    List<ContactID> cons= ContactHelper.getContactInfo(FacebookFindFriendsActivity.this, phonebook);
		    
		    //eliminate duplicate contactID
		    removeDuplicateContactID(cons);
		    
		    for(int i=0;i<cons.size();i++)
		    {
		    	ContactID item = cons.get(i);
		    	if(item.people_id >0)
		    	{
				    Uri peopleuri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, item.people_id);	
				    SocialORM.PeopleMapFacebook map = new PeopleMapFacebook();
				    map.peopleid = item.people_id;
				    map.uid      = phonebook.uid;
				    friends.add(map);
		    	}
		    }
	    }	
	    	   
        //do save
		Log.d(TAG, "save people and facebook map");
		orm.addPeopleMapFacebook(friends);	   
	}    
    
    private void removeDuplicateContactID(List<ContactID> cons){
        ArrayList<ContactID> templist = new ArrayList<ContactID>();
        if(cons!=null && cons.size()>0){
        	for(ContactID contactid : cons){
        		fillTemplist(templist,contactid);
        	}
        	
        	cons.clear();
        	cons.addAll(templist);
        }
    }
    
    private void fillTemplist(ArrayList<ContactID> templist,ContactID contactid){
       int pid = contactid.people_id;
       if(templist.size()>0){
			 boolean islastone = false;		   
			 for(int i = 0; i < templist.size() ; i++){   
				if(i == templist.size()-1) islastone = true;	   
				if(pid == templist.get(i).people_id){
					break;
				}	
				if(islastone){
					templist.add(contactid);
				}
			 }	   
	  }else{
			 templist.add(contactid);  
	  }
    }

    
	private void notifyLoading() 
    {
    	  notify.notifyOnce(R.string.facebook_find_friends_loading, R.drawable.facebook_logo, 30*1000);    
	}

	public void requestFrinds(long uid) 
	{
		if(facebookA == null)
		{
			Log.d(TAG, "facebook async is null");
			return ;
		}
		else
		{
			synchronized(mLock)
	    	{
	    	    inprocess = true;
	    	}
			facebookA.requestFriendAsync(uid,new FacebookAdapter()
	    	{
	    		@Override public void requestFriend(long uid, boolean suc)
	            {
					synchronized(mLock)
			    	{
			    	    inprocess = false;
			    	}
					orm.reqestedFriends(uid, true);
					handler.post(new Runnable(){
		    			   public void run()
		    			   {
		    			       Toast.makeText(mContext, R.string.facebook_add_as_friend_successfully, Toast.LENGTH_LONG).show();
		    			   }
		    			});	
	            }
	    		
	            @Override public void onException(FacebookException e, int method) 
	            {
	            	synchronized(mLock)
			    	{
			    	    inprocess = false;
			    	}
	            	
	            	if(isInAynscTaskAndStoped())
	            	{
	            		Log.d(TAG, "User stop passive");
	            	}
	            	
	            	handler.post(new Runnable(){
                        public void run()
                        {
                            Toast.makeText(mContext, R.string.facebook_add_as_friend_failed, Toast.LENGTH_LONG).show();
                        }
                     });
	            }
	    	});
		}
	}

	public void setUserName(int peopleid, String data) 
	{
		for(int i=0;i<friends.size();i++)
		{
			PeopleMapFacebook item = friends.get(i);
			if(item.peopleid == peopleid)
			{
				item.Name = data;
			}
		}		
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {        
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {         
        	Log.d(TAG, "KEYCODE_BACK coming="+this);
            stopLoading();
            restoreTitle();
            System.gc();
            
            if(this.amLookupContact)
            {
            	this.backToSearchFriends();
            	return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
	 
	private class MyWatcher implements TextWatcher 
    {   
       public void afterTextChanged(Editable s) 
       {
           //do search
           doSearch(s.toString());
       }
       public void beforeTextChanged(CharSequence s, int start, int count, int after) 
       {
       }
       public void onTextChanged(CharSequence s, int start, int before, int count) {}
   }

	public void requestedUser(long uid, int peopleid) 
	{
        for(int i=0;i<friends.size();i++)
        {
        	PeopleMapFacebook user = friends.get(i);
        	if(user.uid == uid && user.peopleid == peopleid)
        	{
        		user.requested = true;
        		friends.set(i, user);
        		break;
        	}
        }		
	}
	
	public void registerAccountListener() {
		AccountManager.registerAccountListener("FacebookFindFriendsActivity", this);		
	}
	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("FacebookFindFriendsActivity");		
	}
}
