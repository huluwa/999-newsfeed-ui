package com.ast.free.ui;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import com.ast.free.ui.FacebookNoteEditActivity;
import com.ast.free.R;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Notes;
import com.ast.free.providers.SocialORM;
import com.ast.free.ui.view.ImageRun;
import com.ast.free.ui.view.SNSItemView;
import com.ast.free.util.DateUtil;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookNotesActivity extends FacebookBaseActivity
{
	private ListView notesList;
	private View     facebook_info_span;
	private TextView facebook_info;
	 
    private int  limitsize  = 20;
    private int  currentPos = 0;    
    private int  lastVisiblePos = -1;
    private String footerText = "";
    private ArrayList<Notes> notes = new ArrayList<Notes>();
    
    private Cursor cursor;
    private boolean withfooterview = false;
    
    @Override protected void onDestroy() 
    {    
        super.onDestroy();
        
        if(cursor != null)
        {
            cursor.close();
            cursor = null;
        }
    }
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_notes_ui);
        
        notesList = (ListView)this.findViewById(R.id.facebook_wall_list);   
        notesList.setFocusableInTouchMode(true);
        notesList.setFocusable(true);                
        notesList.setOnCreateContextMenuListener(this);
        notesList.setOnItemClickListener(listItemClickListener);
        
        facebook_info_span = (View)this.findViewById(R.id.facebook_info_span);
		facebook_info      = (TextView)this.findViewById(R.id.facebook_info);

		View v = findViewById(R.id.progress_horizontal);
		if(v != null)
		{
		    progressHorizontal = (ProgressBar) v;
		}
		
		FacebookNotesActivity.this.setTitle(R.string.facebook_notes_title);
        
		this.setTitle(R.string.facebook_main_notes);
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);
	        	
	        	facebookA = new AsyncFacebook(perm_session);
	        	setMenu(true);
	        	launchLoadNotesFromDB();
	        	launchLoadNotes();	        	
        	}
        	else
        	{
        		setMenu(false);
        		launchFacebookLogin();
        	}
        }
    }
	
	 View.OnClickListener loadOlderNotesClick = new View.OnClickListener()
     {
         public void onClick(View v) 
         {
             Log.d(TAG, "load older Guest"); 
             if(isInProcess())
             {
                handler.post(new Runnable(){
                 public void run() {
                    int index = footerText.indexOf(".");
                    String text = footerText;
                    if(index > 0)
                    {
                       text = text.substring(0,index);
                    }
                    Toast.makeText(FacebookNotesActivity.this, text + " please wait...", Toast.LENGTH_SHORT).show();   
                 } 
                });
             }
             else
             {
                 launchLoadNotes();
             }
             
         }
     };
	
     private void setNomoreNotes(List<Notes> notelist)
     {
         if(notelist.size() == 0 || notelist.size() < limitsize)
         {
              withfooterview = false;
         }
         else
         {
              withfooterview = true;
         }         
     }
	
	protected void loadRefresh()
    {
        super.loadRefresh();
        currentPos = 0;
        lastVisiblePos = -1;
        launchLoadNotes();
    }
	private void doNoContent()
    {
    	if(notes.size() == 0)
    	{
    		facebook_info_span.setVisibility(View.VISIBLE);
    		facebook_info.setText(R.string.no_notes_hint);
    	}
    	else
    	{
    		facebook_info_span.setVisibility(View.GONE);
    	}
    }
	
	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			Log.d(TAG, "do note edit");
			if(NotesItemView.class.isInstance(v))
			{
				NotesItemView view = (NotesItemView)v;
				Notes note = view.getNotes();
				Intent intent = new Intent(mContext,FacebookNoteDetailActivity.class);
				intent.putExtra("noteid", note.note_id);
				mContext.startActivity(intent);
				/*if(note != null)
				{
                    Intent intent = new Intent(mContext,FacebookNoteEditActivity.class);
                    intent.putExtra("note_id", note.note_id);
                    intent.putExtra("content", note.content);
                    intent.putExtra("title",   note.title);
                    startActivityForResult(intent, RESULT_OK);
				}*/
			}
		}
	};
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        // TODO Auto-generated method stub
        //super.onActivityResult(requestCode, resultCode, intent);
	    loadRefresh();
	    super.onActivityResult(requestCode, resultCode, intent);
    }
	
    @Override
    protected void onResume() {
        Log.d(TAG, "entering FacebookNotesActivity onResume");
        handler.obtainMessage(FACEBOOK_NOTES_UI).sendToTarget();
        super.onResume();
    }
    private void launchLoadNotesFromDB()
	{
	    handler.obtainMessage(FACEBOOK_NOTES_DB_GET).sendToTarget();
	    
	}
	
	private void launchLoadNotes() 
	{
		handler.obtainMessage(FACEBOOK_NOTES_GET).sendToTarget();	
	}
	protected  static final int FACEBOOK_NOTES_GET         = 1;
	protected  static final int FACEBOOK_NOTES_GET_END     = 2;
	protected  static final int FACEBOOK_NOTES_UI          = 3;
	protected  static final int FACEBOOK_NOTES_DELETE      = 4;	
	protected  static final int FACEBOOK_NOTES_DB_GET     = 5;
	protected  static final int FACEBOOK_CLEAR_CACHE    = 6;
	
	private class NotesHanlder extends Handler
	{
		public NotesHanlder()
        {
            super();            
            Log.d(TAG, "new NotesHanlder");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case FACEBOOK_NOTES_DB_GET:
                {
                    handler.obtainMessage(FACEBOOK_NOTES_UI).sendToTarget();
                    break;
                }
	            case FACEBOOK_NOTES_GET:
	            {  
	            	notesGet();
	            	break;
	            }
	            case FACEBOOK_NOTES_UI:
	            { 
	                if(cursor != null && cursor.isClosed() == false)
	                {
	                    cursor.close();	                    
	                }
	                long uid = perm_session.getLogerInUserID();
                    cursor = orm.getNoteCursorByUID(uid);
                    Log.d(TAG,"tet NoteCursorByUID cursor is "+cursor +" uid is "+uid);
                    if(lastVisiblePos > 0)
                    {
                        lastVisiblePos = notesList.getLastVisiblePosition();
                    }
                    NoteAdapter na = new NoteAdapter(FacebookNotesActivity.this,cursor,withfooterview);
                    notesList.setAdapter(na);
                    notesList.setSelection(lastVisiblePos+1);
	            	break;
	            }
	            case FACEBOOK_NOTES_DELETE:
	            {
	            	break;
	            }	            
	            case FACEBOOK_NOTES_GET_END:
	            {
	                footerText = getString(R.string.load_more_msg);
	                showFooterViewText(footerText);
	            	end();
	            	break;
	            }
	            case FACEBOOK_CLEAR_CACHE:
	            {
	                clearcache();
	                break;
	            }
            }
        }		
	}
	
	//
	private void notesGetFriends()
    {   
        if(this.isInProcess() == true)
        {
            Log.d(TAG, "previouse is still in loading, return");
        }
        
        if(existSession() == false)
        {
            return;
        }       
        
        begin();            
        Log.d(TAG, "before get notes message");
        //notifyLoading();  
        
        synchronized(mLock)
        {
            inprocess = true;
        }           
        
        facebookA.getNotesAsync(perm_session.getLogerInUserID(),currentPos, limitsize, new FacebookAdapter()
        {
            @Override public void getNotes(List<Notes> tnotes)
            {
                Log.d(TAG, "after get notes="+tnotes.size());
                synchronized(mLock)
                {
                    inprocess = false;
                }
                
                 orm.insertNote(tnotes);
                //addNotes(tnotes);
                
                if(donotcallnetwork == false )//I am still alive
                {   
                     Message msd = handler.obtainMessage(FACEBOOK_NOTES_UI);
                     msd.sendToTarget();
                     //cancelNotify();
                }       
                
                Message msd = handler.obtainMessage(FACEBOOK_NOTES_GET_END);                
                handler.sendMessage(msd);
            }
            
            @Override public void onException(FacebookException e, int method, Object[] args) 
            {
                Log.d(TAG, "fail to get get notes="+e.getMessage());
                synchronized(mLock)
                {
                    inprocess = false;
                }
                if(donotcallnetwork == false )//I am still alive
                {   
                     //cancelNotify();
                }   
                if(isInAynscTaskAndStoped())
                {
                    Log.d(TAG, "User stop passive");
                }
                else
                {
                     Message msd = handler.obtainMessage(FACEBOOK_NOTES_GET_END);                    
                     handler.sendMessage(msd);
                }
            }
        });
    }
	
	public void clearcache() {
       if(cursor != null && cursor.getCount()>0)
       {
           int ret = orm.deleteAllNotes();
           if(ret>=0)
           {
              Toast.makeText(mContext, R.string.facebook_clear_cache_successfully, Toast.LENGTH_SHORT).show();
           }
           else
           {
               Toast.makeText(mContext, R.string.facebook_clear_cache_failed, Toast.LENGTH_SHORT).show();
           } 
           notesList.setAdapter(null);
           cursor.close();
           cursor = null;
           handler.obtainMessage(FACEBOOK_NOTES_UI).sendToTarget();
       }  
    }
    private void notesGet()
	{	
		if(this.isInProcess() == true)
		{
			Log.d(TAG, "previouse is still in loading, return");
			showToast();
			return;
		}
		
		if(existSession() == false)
    	{
    		return;
    	}    	
		
        begin();	
        footerText = getString(R.string.loading_notes);
        showFooterViewText(footerText);
       
    	Log.d(TAG, "before get notes message");
     	//notifyLoading();  
    	
    	synchronized(mLock)
    	{
    	    inprocess = true;
    	}	    	
    	
    	facebookA.getMyNotesAsync(perm_session.getLogerInUserID(),currentPos, limitsize, new FacebookAdapter()
    	{
    		@Override public void getNotes(List<Notes> tnotes)
            {
    			Log.d(TAG, "after get notes="+tnotes.size());
				synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	}
				
				orm.insertNote(tnotes);
				setNomoreNotes(tnotes);
				currentPos += tnotes.size();
				//addNotes(tnotes);	
                if(donotcallnetwork == false )//I am still alive
                {   
                	 Message msd = handler.obtainMessage(FACEBOOK_NOTES_UI);
                	 msd.sendToTarget();
	            	 //cancelNotify();
                }       
                
                Message msd = handler.obtainMessage(FACEBOOK_NOTES_GET_END);                
                handler.sendMessage(msd);
            }
    		
            @Override public void onException(FacebookException e, int method, Object[] args) 
            {
            	Log.d(TAG, "fail to get get notes="+e.getMessage());
            	synchronized(mLock)
		    	{
		    	    inprocess = false;
		    	    
		    	}
            	if(donotcallnetwork == false )//I am still alive
                {   
                     //cancelNotify();
                }   
            	if(isInAynscTaskAndStoped())
            	{
            		Log.d(TAG, "User stop passive");
            	}
            	else
            	{
	            	 Message msd = handler.obtainMessage(FACEBOOK_NOTES_GET_END);	                 
	                 handler.sendMessage(msd);
            	}
            }
    	});
	}

	private void showFooterViewText(String footerText) {
	    for(int i= notesList.getChildCount()-1;i>0;i--)            
        {
            View v = notesList.getChildAt(i);
            if(Button.class.isInstance(v))
            {
                Button bt = (Button)v;
                bt.setText(footerText);
                break;
            }
        } 
    }
    @Override
	protected void createHandler() 
	{
		handler = new NotesHanlder();
	}
    
    @Override
    protected void createNewNotes()
    {
    	//create the new notes
		Intent intent = new Intent(mContext,FacebookNoteEditActivity.class);
		startActivity(intent);
    }
    
    /*
	@Override
	public void titleSelected() 
	{	
		super.titleSelected();
		
		//create the new notes
		Intent intent = new Intent(mContext,FacebookNoteEditActivity.class);
		startActivity(intent);
	}*/

	public void setTitle() 
	{
		title = getString(R.string.facebook_main_notes);				
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
    		Log.d(TAG, "fail to get permanent session");
    		Toast.makeText(this, R.string.facebook_no_valid_session, Toast.LENGTH_SHORT).show();
    		setMenu(false); 		
    		//reLaunchFacebookLogin();
    	}
    	else
    	{
    		setMenu(true);
    		
    		facebookA = new AsyncFacebook(perm_session);
    		perm_session.attachActivity(this);
    		
    		launchLoadNotes();
    	}   
    }
	
	private void setMenu(boolean logined)
    {
    	if(logined == false)
    	{
    		notesList.setOnCreateContextMenuListener(null);
    		notesList.setOnItemClickListener(null);
	    	if(optionMenu != null)
	    	{
				optionMenu.setGroupVisible(R.id.facebook_groupd_nemu, false);				
				optionMenu.setGroupVisible(R.id.facebook_groupd_login_nemu, true);
				
				optionMenu.findItem(R.id.facebook_menu_login).setVisible(true);
				optionMenu.findItem(R.id.facebook_menu_settings).setVisible(true);
	    	}	    	
		}
		else
		{
			notesList.setOnCreateContextMenuListener(this);
			notesList.setOnItemClickListener(listItemClickListener);
			if(optionMenu != null)
			{
				optionMenu.setGroupVisible(R.id.facebook_groupd_nemu, true);
				optionMenu.setGroupVisible(R.id.facebook_groupd_login_nemu, false);
				optionMenu.findItem(R.id.facebook_menu_login).setVisible(false);
			}			
	    }
    }
	
	private void notifyLoading() 
    {
    	notify.notifyOnce(R.string.facebook_notes_loading, R.drawable.facebook_logo, 30*1000);		
	}
	
	@Override
    public void onLogin() 
    {       
        super.onLogin();
        
        //get notes again       
        handler.obtainMessage(FACEBOOK_NOTES_GET).sendToTarget();           
    }
    
    @Override
    public void onLogout() 
    {
        super.onLogout();
    }
    
	public void registerAccountListener() {
		AccountManager.registerAccountListener("FacebookNotesActivity", this);			
	}

	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("FacebookNotesActivity");			
	}
	
	private class NoteAdapter extends BaseAdapter 
	{
	    private final String TAG = "NoteAdapter";        
	    private Context mContext;
	    private Cursor mCursor;
	    private boolean withfooterview = false;
	    public NoteAdapter(Context con, Cursor cursor,boolean withfooterview)
	    {
	        mContext = con;
	        mCursor  = cursor;
	        this.withfooterview = withfooterview;
	    }	 
	    
		public int getCount() 
		{
		    if(mCursor != null)
		    {
		        if(withfooterview && mCursor.getCount()>0)
		        {
		            return mCursor.getCount()+1;
		        }
		        else
		        {
		            return mCursor.getCount();
		        }
		    }
		    return 0 ;
		}
		
		public Object getItem(int pos) 
		{
	        if(mCursor != null && mCursor.moveToFirst())
            {
	           /* if(pos == mCursor.getCount())
	            {
	                return null;
	            }
	            else*/ if(mCursor.moveToPosition(pos))
                {
                    return SocialORM.instance(mContext).formatNote(mCursor);
                }
            }           
            return null;		
		}
		
		public long getItemId(int pos) 
		{
		    if(mCursor != null  && mCursor.moveToFirst())
            {
                if(mCursor.moveToPosition(pos))
                {
                    return SocialORM.instance(mContext).formatNote(mCursor).note_id;
                }
            }           
            return -1;			
		}
		
		public View getView(int position, View convertView, ViewGroup arg2) 
		{		
			 if (position < 0 || position >= getCount()) 
			 {
	             return null;
	         }
	         
			 NotesItemView v=null;
			
			 Notes di = (Notes)getItem(position);
			 if(di != null)
			 {
    	         if (convertView == null || false == (convertView instanceof NotesItemView)) {
    	             v = new NotesItemView(mContext, di);
    	         } else {
    	              v = (NotesItemView) convertView;
    	              v.setNoteItem(di);
    	         }
			 }
			 else
			 {
			      Log.d(TAG, "entering create footerview");
			      Button but = new Button(mContext);
	              but.setTextAppearance(mContext, R.style.sns_load_old);
	              but.setBackgroundColor(Color.WHITE);              
	              but.setText(mContext.getString(R.string.load_older_msg));
	              if(FacebookNotesActivity.class.isInstance(mContext))
	              {
	                  FacebookNotesActivity fn = (FacebookNotesActivity)mContext;
	                  but.setOnClickListener(fn.loadOlderNotesClick);
	                  if(fn.isInProcess())
	                  {
	                      but.setText(mContext.getString(R.string.loading_notes));
	                  }
	              }
	             
	              return but;
			   
			 }
	         
	         return v;
		}
	}
	
	public class NotesItemView extends SNSItemView {
		private final String TAG="NotesItemView";
		
		private ImageView imageView;
		private TextView publishDate;
		private TextView publishTxt;
		private TextView publishcontent;
		private TextView username;	
		
		private Notes  note;	
		
		String  imageURL;
		Handler handler;
		
		SocialORM orm;
		FacebookUser user;
		
		public Notes getNotes()
		{
			return note;
		}		
		
		public NotesItemView(Context ctx, AttributeSet attrs) 
		{
			super(ctx, attrs);
			mContext = ctx;
			orm = SocialORM.instance(mContext);
			setOrientation(LinearLayout.VERTICAL);
	        this.setVisibility(View.VISIBLE);   
	        
	        handler = new NotesInternalHandler();
	    }
		
		public NotesItemView(Context context, Notes di) 
		{		
			super(context);
			mContext = context;
			note = di;
			
			orm = SocialORM.instance(mContext);
			Log.d(TAG, "call mail box NotesItemView");
		
			handler = new NotesInternalHandler();
			init();
		}	
		
		
		private void updateUIFromUser()
		{
			handler.obtainMessage(UPDATE_UI).sendToTarget();
		}
		
		final int UPDATE_UI=0;
		final int UPDATE_PAT_UI=1;
		final int UPDATE_IMAGE_UI=2;
		public class NotesInternalHandler extends Handler
		{
		    public NotesInternalHandler()
	        {
	            super();            
	            Log.d(TAG, "new NotesInternalHandler");
	        }
	        
	        @Override
	        public void handleMessage(Message msg) 
	        {
	            switch(msg.what)
	            {
	            	case UPDATE_UI:
	            	{	       
	            		if(user != null)
	            		{
	            			username.setText(user.name);
	            		}
	            		break;
	            	}
	            	case UPDATE_PAT_UI:
	            	{
	            		String names = msg.getData().getString("usernames");            		
	            		username.setText(names);
	            		break;
	            	}
	            	case UPDATE_IMAGE_UI:
	            	{
	            		String url = msg.getData().getString("imageurl");
	            		if(url == null)
	            		{
	            			imageView.setImageResource(R.drawable.noimage);
	            		}            		
	            		else
	            		{            			
		            		ImageRun imagerun = new ImageRun(handler, url, 0);		
		            		imagerun.use_avatar = true;
		            		imagerun.setImageView(imageView);
		            		imagerun.post(imagerun);
	            		}
	            		break;
	            	}
	            }
	        }	        
		}
				
		public long getFromUID()
		{
			return note.uid;
		}
		//get the image from database, 
		//if the user is not exist, will load the user data, and save them into database
		//
		private void setImage()
		{	
			if(imageURL == null)
			{
				long id = getFromUID();
				user = orm.getFacebookUser(id);
				boolean getFromDB=false;
				if(user == null)
				{
					getFromDB = true;
				}
				else
				{
					Log.d(TAG, "who am I="+user);
					imageURL = user.pic_square;
					//no user data, maybe the user has image
					//don't get from network, if the user exist, 
					//this is to save request limitation
					/*
					if((imageURL == null || imageURL.length() ==0 ) && (user.name == null || user.name.length() == 0))
					{
						getFromDB = true;
					}
					*/					
				}			    
			    //the person might have no pic, so no need to call this fun
			    if(getFromDB == true)
				{	
					if(FacebookBaseActivity.class.isInstance(mContext))
					{
						AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
						if(af != null)
						{
							long[] uids = new long[1];
							uids[0] = id;
							af.getBasicUsersAsync(uids, new FacebookAdapter()
					    	{
					    		@Override public void getUsers(List<FacebookUser> users)
					            {
					    			if(users != null && users.size()>0)
					    			{
					    				user = users.get(0);
					    				Log.d(TAG, "after get user info="+user);
					    				
						    			imageURL = user.pic_square;					    			
						    			getImageBMP(imageURL, true);
										
										//update database
										orm.addFacebookUser(user);
										
										updateUIFromUser();
					    			}
					            }
					    		
					            @Override public void onException(FacebookException e, int method) 
					            {
					            	Log.d(TAG, "fail to get the image");
					            	getImageBMP(null, true);         	
					            }
					    	});
						}
					}
					
				}
				else
				{	
					getImageBMP(imageURL, false);
				}
				
			}
			else//I have get the image
			{
				getImageBMP(imageURL, false);
			}
		}
		
		private void getImageBMP(String url, boolean fromAnotherThread)
		{
			if(fromAnotherThread == true)
			{
				Message msg = handler.obtainMessage(UPDATE_IMAGE_UI);
				msg.getData().putString("imageurl", url);
				handler.sendMessage(msg);
			}
			else//from the same thread
			{
				ImageRun imagerun = new ImageRun(handler, url, 0);	
				imagerun.use_avatar = true;
	    		imagerun.setImageView(imageView);
	    		imagerun.post(imagerun);
			}
		}
		
		protected void updateUIFromUser(List<FacebookUser> users) 
		{
			String uname="";
			for(int i=0;i<users.size();i++)
			{
				FacebookUser user = users.get(i);
				if(user.name != null )
				{
				    if(i>0)
					    uname +=", ";
				    
				    uname += user.name;
				}
			}
			
			if(uname.length() > 0)
			{
				Message msg = handler.obtainMessage(UPDATE_PAT_UI);
				msg.getData().putString("usernames", uname);
				handler.sendMessage(msg);
			}
		}
			
		private void init() 
		{
			Log.d(TAG,  "call NotesItemView init");
			LayoutInflater factory = LayoutInflater.from(mContext);
			removeAllViews();
			
			//container
			FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);
			FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
			view.setLayoutParams(paras);
			view.setVerticalScrollBarEnabled(true);
			addView(view);
			
			//child 1
			View v  = factory.inflate(R.layout.facebook_message_item, null);		
			v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
			view.addView(v);
			
			imageView  = (ImageView)v.findViewById(R.id.tweet_img_ui);						
			publishDate  = (TextView)v.findViewById(R.id.tweet_publish_time);
			publishTxt   = (TextView)v.findViewById(R.id.tweet_publish_text);	
			publishcontent = (TextView)v.findViewById(R.id.tweet_publish_content);
			//publishTxt.setMovementMethod(LinkMovementMethod.getInstance());
			//publishTxt.setLinksClickable(true);
			//publishTxt.setAutoLinkMask(Linkify.WEB_URLS);
			
			username     = (TextView)v.findViewById(R.id.tweet_user_name);
			imageView.setOnClickListener(viewUserDetailsClick);
			username.setSingleLine(true);
			setUI();		
		}

        View.OnClickListener viewUserDetailsClick = new View.OnClickListener()
    	{
    		public void onClick(View v) 
    		{
    		    Log.d(TAG, "viewUserDetailsClick you click first one=");	
    			Intent intent = new Intent(mContext, FacebookAccountActivity.class);
    			if(user != null)
    			{
    			    intent.putExtra("uid",      user.uid);
    			    intent.putExtra("username", user.name);
    				intent.putExtra("imageurl", user.pic_square);				
    			}
    			else
    			{
    			    intent.putExtra("uid",      note.uid);
    			    intent.putExtra("username", String.valueOf(note.uid));				
    			}			
    			((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
    		}
    	};
	    	
		@Override
		protected void onFinishInflate() 
		{	
			super.onFinishInflate();		
			init();
		}
		
		public String getText()
		{
		    String content  = "";
		    if(isEmpty(note.content) == false)
		    {
		        content = note.content;
		    }
		    return  content;
		}
		private void setUI()
		{	
			user = orm.getFacebookUser(getFromUID());			
			if(user != null)
			{
			    username.setText(user.name);
			    imageURL = user.pic_square;
			}			
		    setImage();
		    
			publishDate.setText(DateUtil.converToRelativeTime(mContext, note.updated_time));
			String content = getText();
			if(isEmpty(content) == false)
			{
			   publishcontent.setVisibility(View.VISIBLE);  
			   publishcontent.setText(content);
			}
			else
			{
			    publishcontent.setVisibility(View.GONE);
			}
			publishTxt.setText(note.title);
			//publishTxt.setText(URLDecoder.decode(getText()));
		}
		
		public void setNoteItem(Notes di) 
		{  
		    note.despose();
		    note = null;
			note = di;		
			//need reget the image
			imageURL = null;
			setUI();
		}
	}
	
	@Override
	public void doClearCache()
	{
	    Log.d(TAG,"clear facebook notes chache");
        handler.obtainMessage(FACEBOOK_CLEAR_CACHE).sendToTarget();
	}
}
