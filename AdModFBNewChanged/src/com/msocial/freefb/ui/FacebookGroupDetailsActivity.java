package com.msocial.freefb.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.ui.adapter.FacebookFriendAdapter;
import com.msocial.freefb.ui.adapter.GroupParcel;
import com.msocial.freefb.ui.view.FacebookFriendItemView;
import com.msocial.freefb.ui.view.ImageRun;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.ContactInfo;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.FriendRelationship;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

public class FacebookGroupDetailsActivity extends FacebookBaseActivity
{
	GroupParcel group;
	private TextView group_detail;
	private ImageView group_img_ui;
	
	private ListView group_member_view;
	private List<FacebookUser.SimpleFBUser> group_members;
	private View     facebook_group_slider_span;
	private ImageButton pre_slide;
	private ImageButton next_slide;
	private TextView    current_slide;
	private Button join_in;
	private Button decline;
	
	
	private long currentPage = 0;
	private long pagesize    = 100;
	private long pageCount   = 1;
	private boolean isGroupMember = true; //if the logged in user is a member of this group or not
	private boolean  fornotification;
	
	private TextView updateview;
	private TextView typeview;
	private TextView desview;
	private TextView subview;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_group_detail_ui);
        
        setTitle();  
        setTitle(title);
        group = this.getIntent().getParcelableExtra("group");
        fornotification = this.getIntent().getBooleanExtra("fornotification", false);
        
        group_img_ui = (ImageView)this.findViewById(R.id.facebook_group_img_ui);
        group_img_ui.setImageResource(R.drawable.friends);
        group_detail = (TextView)this.findViewById(R.id.facebook_group_detail); 
        
        group_member_view = (ListView)this.findViewById(R.id.facebook_group_member_list);
        group_member_view.setOnCreateContextMenuListener(this);
        group_member_view.setFocusableInTouchMode(true);
        group_member_view.setFocusable(true);
        group_member_view.setSelected(true);
        group_member_view.setClickable(true);
        group_member_view.setOnItemClickListener(listItemClickListener);
        
        updateview = (TextView)this.findViewById(R.id.group_update_detail);
        typeview = (TextView)this.findViewById(R.id.group_type_detail);
        subview =(TextView)this.findViewById(R.id.group_sub_detail);
        desview = (TextView)this.findViewById(R.id.group_des_detail);
        
       join_in = (Button)this.findViewById(R.id.facebook_group_join_in);
       decline = (Button)this.findViewById(R.id.facebook_group_decline);
       
       join_in.setOnClickListener(new OnClickListener()
       {
           public void onClick(View view)
           {
              processJoinGroup( true);
           }
       });
       
       decline.setOnClickListener(new OnClickListener()
       {
           public void onClick(View view)
           {
               processJoinGroup(false);
           }
       });
        
       if(fornotification)
       {
           join_in.setVisibility(View.VISIBLE);
           decline.setVisibility(View.VISIBLE);
           join_in.setClickable(true);
           decline.setClickable(true);
       }
       else
       {
           join_in.setVisibility(View.GONE);
           decline.setVisibility(View.GONE);
           join_in.setClickable(false);
           decline.setClickable(false);
       }
        setUI();
        
        //TODO
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);	        	
	        	facebookA = new AsyncFacebook(perm_session);	    
	        	
	        	getMembers();
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }
    }
	
	private void processJoinGroup(boolean confirm)
	{
	    if(isInProcess() == true)
	    {
	        showToast(getString(R.string.facebook_is_processing));
	        return;
	    }
        if(isInProcess() == false && !isGroupMember)
        {  
            Message message = handler.obtainMessage(JOIN_IN_GROUP);
            message.getData().putBoolean("confirm", confirm);
            message.sendToTarget();
        }

	}
	
	AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{		
			if(FacebookFriendItemView.class.isInstance(v))
			{
				FacebookFriendItemView fv= (FacebookFriendItemView)v;
				Intent intent = new Intent(mContext, FacebookAccountActivity.class);
				intent.putExtra("uid",      fv.getUser().uid);
				intent.putExtra("username", fv.getUser().name);
				intent.putExtra("imageurl", fv.getUser().pic_square);					
				((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);				
			}
		}
	};
	
	@Override 
	protected boolean hasMore()
	{
		if( group_members!= null)
		    return group_members.size() == pagesize;
		else
			return false;
	}
	
	@Override 
	protected boolean isTheFirst()
	{
		return this.currentPage == 0;
	}
	
	@Override
	protected void prePage()
	{
		currentPage = currentPage -1;
        goPage(currentPage);		
	}
	@Override	
	protected void  nextPage()
	{
        currentPage = currentPage + 1;
		goPage(currentPage);

	}
	
	private void goFistPage()
	{
		goPage(0);
	}
	
	private void goPage(long page)
	{
		Message msd = handler.obtainMessage(GET_GROUP_MEMBERS);
		msd.getData().putLong("offset", page*pagesize);
		handler.sendMessage(msd);	
	}
	
	public void setTitle() 
    {
       title = getString(R.string.facebook_groups_title);      
    }
	private void setUI()
	{
		ImageRun imagerun = new ImageRun(handler, group.group.pic_samll, 1);
		imagerun.noimage = true;
		imagerun.width=70;
		imagerun.setImageView(group_img_ui);		
		imagerun.post(imagerun);
		
		group_detail.setText(group.group.name);
		updateview.setText(group.group.update_time.toLocaleString());
		typeview.setText(group.group.group_typ);
		subview.setText(group.group.group_subtype);
		desview.setText(group.group.description);
	}
	@Override
	protected void createHandler() 
	{		
		handler = new JoinHandler();
	}
	
	@Override
	public void titleSelected() 
    {		
		super.titleSelected();		
		//send message to my wall
		if(isInProcess() == false && !isGroupMember && fornotification)
		{  
		    Message message = handler.obtainMessage(JOIN_IN_GROUP);
		    message.getData().putBoolean("confirm", true);
		    message.sendToTarget();
		}
	}
	
	private void getMembers()
	{
		//handler.obtainMessage(GET_GROUP_MEMBERS_SIZE_BEGIN).sendToTarget();
		goFistPage();
	}
	
	protected void loadRefresh()
	{
	    goPage(currentPage);
	}
	
	private void joinGroup(boolean confirm)
	{
		begin();
		
		synchronized(mLock)
    	{
    	    inprocess = true;
    	}
		facebookA.joinGroupAsync(group.group.gid, confirm,new FacebookAdapter()
    	{
    		@Override public void joinGroup(boolean suc)
            {
    			synchronized(mLock)
    	    	{
    	    	    inprocess = false;
    	    	}
    			Log.d(TAG, "after join group="+group.group.name);
    			
    			Message msdd = handler.obtainMessage(JOIN_IN_GROUP_END);
    			msdd.getData().putBoolean(RESULT, suc);
    			msdd.sendToTarget();
    			
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	Log.d(TAG, "fail to get friends information");
            	
            	handler.post(new Runnable(){

					public void run() {
						//setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.error);						
					}
            		
            	});
            	
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
	        		Message msdd = handler.obtainMessage(JOIN_IN_GROUP_END);
	    			msdd.getData().putBoolean(RESULT, false);
	    			msdd.sendToTarget();
            	}
            }
    	});
	}
	
	final static int JOIN_IN_GROUP                =0;
	final static int JOIN_IN_GROUP_END            =1;	
	final static int GROUP_UI_SHOW= 2;
	final static int GET_GROUP_MEMBERS            =3;
	final static int GET_GROUP_MEMBERS_END        =4;	
	
	final static int JUDGE_ARE_REQUESTS           =5;
	final static int JUDGE_ARE_REQUESTS_END       =6;
	
	private class JoinHandler extends Handler 
    {
        public JoinHandler()
        {
            super();            
            Log.d(TAG, "new JoinHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case JOIN_IN_GROUP:
	            {
	                boolean confirm = msg.getData().getBoolean("confirm");
	            	joinGroup(confirm);
	            	break;
	            }
	            case JOIN_IN_GROUP_END:
	            {
	            	end();	            	
	            	FacebookGroupDetailsActivity.this.finish();
	            	if(msg.getData().getBoolean(RESULT) == true)
	            	{
	            	    Toast.makeText(FacebookGroupDetailsActivity.this, R.string.sns_operate_succeed, Toast.LENGTH_SHORT).show();
	            	    FacebookGroupDetailsActivity.this.finish();
	            	}
	            	break;
	            } 
	            case GET_GROUP_MEMBERS:
	            {  
	            	long offset = msg.getData().getLong("offset");
	            	
	            	Log.d(TAG,"entering get group members "+offset);
	            	getGroupMembers(offset);
	            	break;
	            }
	            case GROUP_UI_SHOW:
	            {
	            	if(group_members!=null && group_members.size()>0){
	                	FacebookFriendAdapter sa = new FacebookFriendAdapter(FacebookGroupDetailsActivity.this, group_members,false);
	            		group_member_view.setAdapter(sa);
	                }else{
	            		group_member_view.setAdapter(null);
	                }  
	            	
	            	setTitle();
	            	break;
	            }
	            case JUDGE_ARE_REQUESTS_END:
	            {
	            	end();
	            	break;
	            }
	            case GET_GROUP_MEMBERS_END:
	            {	            	
	            	break;
	            }
	            case JUDGE_ARE_REQUESTS:
	            {
	            	areFriendship();
	            	break;
	            }
        
            }
        }
    }
	
	private void areFriendship(){
		List<Long> uids = new ArrayList<Long>();
		if(group_members!=null && group_members.size()>0){
			for(FacebookUser.SimpleFBUser user : group_members){
				uids.add(user.uid);
			}
		}
		
		if(uids.size() == 0)
            return;
		
		facebookA.areFriendsAsync(uids,new FacebookAdapter()
    	{
			@Override public void areFriends(List<FriendRelationship> frs )
            {
    			for(FriendRelationship fr: frs)
				{
					if(fr.isFriends == false)
						continue;
					
					for(int i = 0;i<group_members.size();i++)
					{					
						FacebookUser.SimpleFBUser user = group_members.get(i);
						if(user.isfriend == false && user.uid == fr.uid2)
						{
							user.isfriend = true;				
							break;
						}
					}				
				}
				
    			handler.obtainMessage(GROUP_UI_SHOW).sendToTarget();
                handler.obtainMessage(JUDGE_ARE_REQUESTS_END).sendToTarget();
            }
    		
            @Override public void onException(FacebookException e, int method) 
            {
            	handler.obtainMessage(JUDGE_ARE_REQUESTS_END).sendToTarget();         	
            }
    	});
	}
	
	private void getGroupMembers(long offset)
    {
	    if(this.isInProcess() == true)
	    {
	        showToast();
	        return;
	    }
	    synchronized(mLock)
        {
            inprocess = true;
        }
        begin();
        facebookA.batch_run_getGroupMemeber_isGroupMember_Async(group.group.gid,offset,pagesize,new FacebookAdapter()
        {
            @Override public void batch_run_getGroupMember_isGroupMember(HashMap<Integer,Object> batch_result)
            {
                group_members = (List<FacebookUser.SimpleFBUser>)batch_result.get(0);
                isGroupMember = ((Boolean)batch_result.get(1)).booleanValue();
                handler.obtainMessage(JUDGE_ARE_REQUESTS).sendToTarget();
                synchronized(mLock)
                {
                    inprocess = false;
                }
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                handler.obtainMessage(GET_GROUP_MEMBERS_END).sendToTarget();   
                synchronized(mLock)
                {
                    inprocess = false;
                }
            }
        });
    }
	
	public void registerAccountListener() {
		AccountManager.registerAccountListener("FacebookGroupDetailsActivity", this);		
	}
	public void unregisterAccountListener() {
		AccountManager.unregisterAccountListener("FacebookGroupDetailsActivity");		
	}
}
