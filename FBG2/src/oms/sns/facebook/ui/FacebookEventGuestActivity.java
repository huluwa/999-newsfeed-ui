package oms.sns.facebook.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import oms.sns.facebook.R;
import oms.sns.facebook.providers.SocialORM;
import oms.sns.facebook.ui.adapter.FacebookFriendAdapter;
import oms.sns.facebook.ui.view.FacebookFriendItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.FacebookUser.SimpleFBUser;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class FacebookEventGuestActivity extends FacebookBaseActivity{
    final String TAG = "FacebookEventGuestActivity";
    private View infoSpan;
    private Button attending_tab;
    private Button unsure_tab;
    private Button not_attending_tab;
    private Button not_replied_tab;
    private ListView guest_list;
    
    private List<FacebookUser.SimpleFBUser> attending_guest = new ArrayList<FacebookUser.SimpleFBUser>();
    private List<FacebookUser.SimpleFBUser> unsure_guest = new ArrayList<FacebookUser.SimpleFBUser>();
    private List<FacebookUser.SimpleFBUser> not_attending_guest = new ArrayList<FacebookUser.SimpleFBUser>();
    private List<FacebookUser.SimpleFBUser> not_replied_guest  = new ArrayList<FacebookUser.SimpleFBUser>();
    
    private long eventid = -1;
    private boolean showAttending     = true;
    private boolean showUnsure        = false;
    private boolean showNotAttending  = false;
    private boolean showNotReplied    = false;
    
    private boolean nomoreAttending    = true;
    private boolean nomoreUnsure       = true;
    private boolean nomoreNotAttending = true;
    private boolean nomoreNotReplied    = true;
    
    private boolean withfooterview = false;
    
    private int currentAttendingPos     = 0;
    private int currentUnsurePos        = 0;
    private int currentNotAttendingpos  = 0;
    private int currentNotRepliedPos    = 0;
    private int offset                  = 20;
    private boolean changed = false;
    
    private String footerText = "";
    
    public void onCreate(Bundle savedInstanceState)
    {
       super.onCreate(savedInstanceState);        
       setContentView(R.layout.facebook_event_guest_ui);
       infoSpan = (View)this.findViewById(R.id.facebook_info_span);
       attending_tab = (Button)this.findViewById(R.id.facebook_guest_attending);
       attending_tab.setId(1);
       unsure_tab = (Button)this.findViewById(R.id.facebook_guest_unsure);
       unsure_tab.setId(2);
       not_attending_tab = (Button)this.findViewById(R.id.facebook_guest_decline);
       not_attending_tab.setId(3);
       not_replied_tab = (Button)this.findViewById(R.id.facebook_guest_not_reply);
       not_replied_tab.setId(4);
       
       attending_tab.setOnClickListener(showContentListener);
       unsure_tab.setOnClickListener(showContentListener);
       not_attending_tab.setOnClickListener(showContentListener);
       not_replied_tab.setOnClickListener(showContentListener);
       guest_list = (ListView)this.findViewById(R.id.facebook_event_guest_list);
       guest_list.setOnItemClickListener(itemClick);
       
       eventid = getIntent().getLongExtra("eid", -1);
       setTitle();
       setTitle(title);
       footerText = getString(R.string.load_more_msg);
       updateSubTabUI();
       SocialORM.Account account = orm.getFacebookAccount();
       if(checkFacebookAccount(this, account))
       {
           
           
           perm_session = loginHelper.getPermanentSesstion(this);
           if(perm_session != null)
           {
               perm_session.attachActivity(this);
               facebookA = new AsyncFacebook(perm_session);
               launchGetEventGuestInfo();
           }
           else
           {
               launchFacebookLogin();
           }
       }     
    }
    
    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener()
    {

       public void onItemClick(AdapterView<?> arg0, View v, int pos,long id) {
           Log.d(TAG, "do note edit");
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
    protected boolean  goPrePage()
    {
        super.goPrePage();
        
        changed = false;
        if(showAttending == true)
        {
            changed = false;         
            showAttending    = true;
            showUnsure       = false;
            showNotAttending = false;
            showNotReplied   = false;
        }
        else if(showUnsure == true)
        {
            changed   = true;             
            showAttending    = true;
            showUnsure       = false;
            showNotAttending = false;
            showNotReplied   = false;
        }
        else if(showNotAttending ==true)
        {
            changed = true;
            
            showAttending    = false;
            showUnsure       = true;
            showNotAttending = false;
            showNotReplied   = false;
        }
        else if(showNotReplied == true)
        {
            changed = true;
            
            showAttending    = false;
            showUnsure       = false;
            showNotAttending = true;
            showNotReplied   = false;
        }
        if(changed == true)//don't repeat to show
        {
            updateSubTabUI();
            return true;
        }
        return false;
    }
    
    @Override
    protected boolean  goNextPage()
    {
        super.goNextPage();   
        
        changed = false;
        if(showAttending == true)
        {
            changed = true;         
            showAttending    = false;
            showUnsure       = true;
            showNotAttending = false;
            showNotReplied   = false;
        }
        else if(showUnsure == true)
        {
            changed   = true;             
            showAttending = false;
            showUnsure = false;
            showNotAttending = true;
            showNotReplied = false;
        }
        else if(showNotAttending ==true)
        {
            changed = true;
            
            showAttending = false;
            showUnsure = false;
            showNotAttending = false;
            showNotReplied = true;
        }
        else if(showNotReplied == true)
        {
            changed = false;
            
            showAttending    = false;
            showUnsure       = false;
            showNotAttending = false;
            showNotReplied   = true;
        }
        if(changed == true)//don't repeat to show
        {   
            updateSubTabUI();
            return true;
        }
        return false;
    }
    
    View.OnClickListener showContentListener = new OnClickListener()
    {
        public void onClick(View v)
        {
            changed = false;
            if(v.getId() == 1)
            {
                if(showAttending == false)
                {
                    changed = true;
                }
                showAttending = true;
                showUnsure = false;
                showNotAttending = false;
                showNotReplied = false;
            }
            else if(v.getId() ==2)
            {
                if(showUnsure == false)
                {
                    changed = true;
                }
                showAttending = false;
                showUnsure = true;
                showNotAttending = false;
                showNotReplied = false;
            }
            else if(v.getId() ==3)
            {
                if(showNotAttending == false)
                {
                    changed = true;
                }
                showAttending = false;
                showUnsure = false;
                showNotAttending = true;
                showNotReplied = false;
            }
            else if(v.getId() == 4)
            {
                if(showNotReplied == false)
                {
                    changed = true;
                }
                showAttending = false;
                showUnsure = false;
                showNotAttending = false;
                showNotReplied = true;
            }
            if(changed == true)//don't repeat to show
            {
                updateSubTabUI();
            }
        }
    };
    
    private static int ATTENDING_TYPE     =1;
    private static int UNSURE_TYPE       = 2;
    private static int NO_ATTENDING_TYPE  = 3;
    private static int NOT_REPLIED_TYPE   =4;
    private void updateSubTabUI()
    {  
        int lastvisiblePos = -1;
        if(changed == false)
            lastvisiblePos = guest_list.getLastVisiblePosition();
        else
        {
            // has changed ui and then recover changed = false
            changed = false;
        }
        if(showAttending)
        {
            attending_tab.setBackgroundResource(R.drawable.btn_blue_light);
            unsure_tab.setBackgroundResource(R.color.facebook_light_grey);
            not_attending_tab.setBackgroundResource(R.color.facebook_light_grey);
            not_replied_tab.setBackgroundResource(R.color.facebook_light_grey);
            
            attending_tab.setTextColor(Color.WHITE);        
            unsure_tab.setTextColor(getResources().getColor(R.color.light_blue));
            not_attending_tab.setTextColor(getResources().getColor(R.color.light_blue));
            not_replied_tab.setTextColor(getResources().getColor(R.color.light_blue));
            
            withfooterview = !nomoreAttending;
            if(attending_guest!=null && attending_guest.size()>0)
            {
                infoSpan.setVisibility(View.GONE);
                FacebookFriendAdapter fea = new FacebookFriendAdapter(FacebookEventGuestActivity.this,attending_guest,ATTENDING_TYPE,withfooterview);
                guest_list.setAdapter(fea);
            }
            else
            {
                infoSpan.setVisibility(View.VISIBLE);                    
            }                  
        }  
        //if for info, hide other
        else if(showUnsure)
        {  
            attending_tab.setBackgroundResource(R.color.facebook_light_grey);                
            unsure_tab.setBackgroundResource(R.drawable.btn_blue_light);
            not_attending_tab.setBackgroundResource(R.color.facebook_light_grey);
            not_replied_tab.setBackgroundResource(R.color.facebook_light_grey);
            
            attending_tab.setTextColor(getResources().getColor(R.color.light_blue));        
            unsure_tab.setTextColor(Color.WHITE);
            not_attending_tab.setTextColor(getResources().getColor(R.color.light_blue));
            not_replied_tab.setTextColor(getResources().getColor(R.color.light_blue));
            withfooterview = !nomoreUnsure;
            if(unsure_guest!=null && unsure_guest.size()>0)
            {
                infoSpan.setVisibility(View.GONE);
                FacebookFriendAdapter fea = new FacebookFriendAdapter(FacebookEventGuestActivity.this,unsure_guest,UNSURE_TYPE,withfooterview
                        );
                guest_list.setAdapter(fea);
            }
            else
            {
                infoSpan.setVisibility(View.VISIBLE);
                //eventinfoview.setText(R.string.no_event_info);                      
            }            
            
        }
        //if for photo, hide others
        else if(showNotAttending)
        {      
            attending_tab.setBackgroundResource(R.color.facebook_light_grey);                
            unsure_tab.setBackgroundResource(R.color.facebook_light_grey);
            not_attending_tab.setBackgroundResource(R.drawable.btn_blue_light);
            not_replied_tab.setBackgroundResource(R.color.facebook_light_grey);
            
            attending_tab.setTextColor(getResources().getColor(R.color.light_blue));        
            unsure_tab.setTextColor(getResources().getColor(R.color.light_blue));
            not_attending_tab.setTextColor(Color.WHITE);
            not_replied_tab.setTextColor(getResources().getColor(R.color.light_blue));
            withfooterview = !nomoreNotAttending;
            if(not_attending_guest!=null && not_attending_guest.size()>0)
            {
                infoSpan.setVisibility(View.GONE);
                FacebookFriendAdapter fea = new FacebookFriendAdapter(FacebookEventGuestActivity.this,not_attending_guest,NO_ATTENDING_TYPE,withfooterview);
                guest_list.setAdapter(fea);
            }
            else
            {
                infoSpan.setVisibility(View.VISIBLE);
                //eventinfoview.setText(R.string.no_event_info);                      
            }    
        }
        else if(showNotReplied)
        {
            attending_tab.setBackgroundResource(R.color.facebook_light_grey);                
            unsure_tab.setBackgroundResource(R.color.facebook_light_grey);
            not_attending_tab.setBackgroundResource(R.color.facebook_light_grey);
            not_replied_tab.setBackgroundResource(R.drawable.btn_blue_light);
            
            attending_tab.setTextColor(getResources().getColor(R.color.light_blue));        
            unsure_tab.setTextColor(getResources().getColor(R.color.light_blue));
            not_attending_tab.setTextColor(getResources().getColor(R.color.light_blue));
            not_replied_tab.setTextColor(Color.WHITE);
            withfooterview = !nomoreNotReplied;
            if(not_replied_guest!=null && not_replied_guest.size()>0)
            {
                infoSpan.setVisibility(View.GONE);
                FacebookFriendAdapter fea = new FacebookFriendAdapter(FacebookEventGuestActivity.this,not_replied_guest,NOT_REPLIED_TYPE,withfooterview );
                guest_list.setAdapter(fea);
            }
            else
            {
                infoSpan.setVisibility(View.VISIBLE);
                //eventinfoview.setText(R.string.no_event_info);                      
            }            
        }
        guest_list.setSelection(lastvisiblePos + 1);
    }
    
   public  View.OnClickListener loadOlderGuestClick = new View.OnClickListener()
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
                   Toast.makeText(FacebookEventGuestActivity.this, text+" please wait...", Toast.LENGTH_SHORT).show();   
                } 
               });
            }
            else
            {
                launchLoadOlderGuest();
            }
            
        }
    };
    
    private void launchLoadOlderGuest()
    {
        handler.obtainMessage(FACEBOOK_GUEST_LOAD_OLD_GET).sendToTarget();
    }
    
    private void loadOlderGuest()
    {
      if(isInProcess())
      {
          return;
      }
      begin();
      Log.d(TAG,"befor loadOlderGuest ");
      synchronized(mLock)
      {
          inprocess = true;
      }
      if(perm_session == null)
      {
          perm_session = loginHelper.getPermanentSesstion(this);
          if(perm_session != null)
          {
              perm_session.attachActivity(this);
          }
          if(facebookA == null)
          {
              facebookA = new AsyncFacebook(perm_session);
          }         
      }
      
      if(facebookA != null)
      {
          facebookA.setSession(perm_session);
          String rsvp_status = Event.ATTENDING;
          int position = 0;
          
          if(showAttending)
          {
             //((Button)oldb).setText("loading attending guests");
              footerText = getString(R.string.loading_attending_guest);
              rsvp_status = Event.ATTENDING;
              position = currentAttendingPos;
          }
          else if(showUnsure)
          {
              //((Button)oldb).setText("loading maybe attending guests");
              footerText = getString(R.string.loading_unsure_guest);
              rsvp_status = Event.UNSURE;
              position = currentUnsurePos;
          }
          else if(showNotAttending)
          {
              //((Button)oldb).setText("loading not attending guests");
              footerText = getString(R.string.loading_declined_guest);
              rsvp_status = Event.DECLINED;
              position = currentNotAttendingpos;
          }
          else if(showNotReplied)
          {
              //((Button)oldb).setText("loading not replied guests");
              footerText = getString(R.string.loading_not_replied_guest);
              rsvp_status = Event.NOT_REPLIED;
              position = currentNotRepliedPos;
          }
          
          showFooterViewText(footerText);
          final String rsvp_status_param = rsvp_status;
          
          facebookA.loadOldGuest(eventid,rsvp_status_param,position,offset, new FacebookAdapter()
          {
            @Override
            public void getEventSimpleMembers(List<SimpleFBUser> eventSimpleMembers) 
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }  
                
                int membersize = eventSimpleMembers.size();
                if(eventSimpleMembers !=null && membersize > 0)
                {
                    if(rsvp_status_param.equals(Event.ATTENDING))
                    {
                        currentAttendingPos = currentAttendingPos + membersize;
                        addAttendingGuest(eventSimpleMembers);
                        setNomoreAttending(eventSimpleMembers);
                    }
                    else if(rsvp_status_param.equals(Event.UNSURE))
                    {
                        currentUnsurePos = currentUnsurePos + membersize;
                        addUnsureGuest(eventSimpleMembers);
                        setNomoreUnsure(eventSimpleMembers);
                    }
                    else if(rsvp_status_param.equals(Event.DECLINED))
                    {
                        currentNotAttendingpos = currentNotAttendingpos + membersize;
                        addNotAttendingGuest(eventSimpleMembers);
                        setNomoreNoAttending(eventSimpleMembers);
                    }
                    else if(rsvp_status_param.equals(Event.NOT_REPLIED))
                    {
                        currentNotRepliedPos = currentNotRepliedPos + membersize;
                        addNotRepliedGuest(eventSimpleMembers);
                        setNotReplied(eventSimpleMembers);
                    }
                    
                    handler.obtainMessage(FACEBOOK_EVENT_GUEST_UI).sendToTarget();
                    
                }
                
                Message rmsg = handler.obtainMessage(FACEBOOK_EVENT_GUEST_GET_END);
                rmsg.getData().putBoolean(RESULT, true);
                rmsg.sendToTarget(); 
            }
            
            @Override
            public void onException(FacebookException te, int method) 
            {
                synchronized(mLock)
                {
                    inprocess = false;
                }
                //get from Database
                Message msd = handler.obtainMessage(FACEBOOK_EVENT_GUEST_GET_END);
                msd.getData().putBoolean(RESULT, false);
                handler.sendMessage(msd);
            }
              
          });
      }
    }
    
    private void showFooterViewText(String footerText2) {
        for(int i= guest_list.getChildCount()-1;i>0;i--)            
        {
            View v = guest_list.getChildAt(i);
            if(Button.class.isInstance(v))
            {
                Button bt = (Button)v;
                bt.setText(footerText);
                break;
            }
        } 
    }

    private void addAttendingGuest(List<SimpleFBUser> members)
    {
        synchronized(attending_guest)
        {
            for(int i=0;i<members.size();i++)
            {
                SimpleFBUser user = members.get(i);
                boolean isExist = false;
                for(int j=0;j<attending_guest.size();j++)
                {
                    SimpleFBUser existUser = attending_guest.get(j);
                    if(user.uid == existUser.uid)
                    {
                        isExist=true;                      
                        existUser.despose();
                        existUser=null; 
                        attending_guest.set(j,user);
                        break;
                    }
                }
                
                if(isExist == false)
                {
                    attending_guest.add(user);              
                }
            }
        } 
    }
    
    private void addUnsureGuest(List<SimpleFBUser> members)
    {
        synchronized(unsure_guest)
        {
            for(int i=0;i<members.size();i++)
            {
                SimpleFBUser user = members.get(i);
                boolean isExist = false;
                for(int j=0;j<unsure_guest.size();j++)
                {
                    SimpleFBUser existUser = unsure_guest.get(j);
                    if(user.uid == existUser.uid)
                    {
                        isExist=true;                      
                        existUser.despose();
                        existUser=null; 
                        unsure_guest.set(j,user);
                        break;
                    }
                }
                
                if(isExist == false)
                {
                    unsure_guest.add(user);              
                }
            }
        } 
    }
    
    private void addNotAttendingGuest(List<SimpleFBUser> members)
    { 
        synchronized(not_attending_guest)
        {
            for(int i=0;i<members.size();i++)
            {
                SimpleFBUser user = members.get(i);
                boolean isExist = false;
                for(int j=0;j<not_attending_guest.size();j++)
                {
                    SimpleFBUser existUser = not_attending_guest.get(j);
                    if(user.uid == existUser.uid)
                    {
                        isExist=true;                      
                        existUser.despose();
                        existUser=null; 
                        not_attending_guest.set(j,user);
                        break;
                    }
                }
                
                if(isExist == false)
                {
                    not_attending_guest.add(user);              
                }
            }
        } 
    }
    
    private void addNotRepliedGuest(List<SimpleFBUser> members)
    {
        synchronized(not_replied_guest)
        {
            for(int i=0;i<members.size();i++)
            {
                SimpleFBUser user = members.get(i);
                boolean isExist = false;
                for(int j=0;j<not_replied_guest.size();j++)
                {
                    SimpleFBUser existUser = not_replied_guest.get(j);
                    if(user.uid == existUser.uid)
                    {
                        isExist=true;                      
                        existUser.despose();
                        existUser=null; 
                        not_replied_guest.set(j,user);
                        break;
                    }
                }
                
                if(isExist == false)
                {
                    not_replied_guest.add(user);              
                }
            }
        } 
    }
    
    private void addAttendingGuestToHead(List<SimpleFBUser> attendingmembers)
    {
        synchronized(attending_guest)
        {
            for(int i=0;i<attendingmembers.size();i++)
            {
                SimpleFBUser user = attendingmembers.get(i);
                for(int j=0;j<attending_guest.size();j++)
                {
                    SimpleFBUser existUser = attending_guest.get(j);
                    if(user.uid == existUser.uid)
                    {   
                        attending_guest.remove(existUser);
                        existUser.despose();
                        existUser=null;
                        break;
                    }
                }
                attending_guest.add(i,user);       
            }
        } 
    }
    
    private void  addUnsureGuestToHead(List<SimpleFBUser> unsuremembers)
    {
        synchronized(unsure_guest)
        {
            for(int i=0;i<unsuremembers.size();i++)
            {
                SimpleFBUser user = unsuremembers.get(i);
                for(int j=0;j<unsure_guest.size();j++)
                {
                    SimpleFBUser existUser = unsure_guest.get(j);
                    if(user.uid == existUser.uid)
                    {   
                        unsure_guest.remove(existUser);
                        existUser.despose();
                        existUser=null;
                        break;
                    }
                }
                unsure_guest.add(i,user);       
            }
        } 
    }
    
    private void addNotatteingGuestToHead(List<SimpleFBUser> notattendingmembers)
    {
        synchronized(not_attending_guest)
        {
            for(int i=0;i<notattendingmembers.size();i++)
            {
                SimpleFBUser user = notattendingmembers.get(i);
                for(int j=0;j<not_attending_guest.size();j++)
                {
                    SimpleFBUser existUser = not_attending_guest.get(j);
                    if(user.uid == existUser.uid)
                    {   
                        not_attending_guest.remove(existUser);
                        existUser.despose();
                        existUser=null;
                        break;
                    }
                }
                not_attending_guest.add(i,user);       
            }
        } 
    }
    
    private void addNotrepliedGuestToHead(List<SimpleFBUser> notrepliedmembers)
    {
        synchronized(not_replied_guest)
        {
            for(int i=0;i<notrepliedmembers.size();i++)
            {
                SimpleFBUser user = notrepliedmembers.get(i);
                for(int j=0;j<not_replied_guest.size();j++)
                {
                    SimpleFBUser existUser = not_replied_guest.get(j);
                    if(user.uid == existUser.uid)
                    {   
                        not_replied_guest.remove(existUser);
                        existUser.despose();
                        existUser=null;
                        break;
                    }
                }
                not_replied_guest.add(i,user);       
            }
        } 
    }
    
    
    
    private void batch_run_loadEventGuest()
    {
        if(isInProcess())
        {
            return;
        }
        begin();
        
        Log.d(TAG, "before get getFacebookEventGuest"); 
        synchronized(mLock)
        {
            inprocess = true;
        }
        
        if(perm_session == null)
        {
             perm_session = loginHelper.getPermanentSesstion(this);
             if(perm_session != null)
             {
                 perm_session.attachActivity(this);
             }
             if(facebookA == null)
             {
                 facebookA = new AsyncFacebook(perm_session);
             }             
        }           
        if(facebookA != null)
        {
            facebookA.setSession(perm_session);
            facebookA.batch_run_getFacebookEventGuestAsync(eventid,new FacebookAdapter()
            {
                @Override public void batch_run_getFacebookEventGuest(HashMap<Integer, Object> guestMap)
                {
                    synchronized(mLock)
                    {
                        inprocess = false;
                    }                   
                    // events = results;
                    //orm.addFacebookevent(events);
                    guest_list.setOnItemClickListener(null);
                    
                    List<SimpleFBUser> attendingmembers = (List<SimpleFBUser>)guestMap.get(0);
                    addAttendingGuestToHead(attendingmembers);
                    currentAttendingPos += attending_guest.size();
                    setNomoreAttending(attendingmembers);
                    
                    List<SimpleFBUser> unsuremembers = (List<SimpleFBUser>)guestMap.get(1);
                    addUnsureGuestToHead(unsuremembers);
                    currentUnsurePos += unsure_guest.size();
                    setNomoreUnsure(unsuremembers);
                    
                    List<SimpleFBUser> notattendingmembers = (List<SimpleFBUser>)guestMap.get(2);
                    addNotatteingGuestToHead(notattendingmembers);
                    currentNotAttendingpos += not_attending_guest.size();
                    setNomoreNoAttending(notattendingmembers);
                    
                    List<SimpleFBUser> notrepliedmembers = (List<SimpleFBUser>)guestMap.get(3);
                    addNotrepliedGuestToHead(notrepliedmembers);
                    currentNotRepliedPos += not_replied_guest.size();
                    setNotReplied(notrepliedmembers);
                    
                    if(donotcallnetwork == false)//I am still alive
                    {
                        handler.obtainMessage(FACEBOOK_EVENT_GUEST_UI).sendToTarget();
                    }
                    
                    Message msd = handler.obtainMessage(FACEBOOK_EVENT_GUEST_GET_END);
                    msd.getData().putBoolean(RESULT, true);
                    handler.sendMessage(msd);
                }
                
                @Override public void onException(FacebookException e, int method) 
                {
                    synchronized(mLock)
                    {
                        inprocess = false;
                    }
                    //get from Database
                    Message msd = handler.obtainMessage(FACEBOOK_EVENT_GUEST_GET_END);
                    msd.getData().putBoolean(RESULT, false);
                    handler.sendMessage(msd);
                }
             });
         }
    }
    
    protected void setNotReplied(List<SimpleFBUser> notrepliedmembers) {
        
        if(notrepliedmembers.size() ==0 || notrepliedmembers.size() < offset)
        {
            nomoreNotReplied = true;
        }
        else
        {
            nomoreNotReplied = false;
        }
       
    }

    protected void setNomoreNoAttending(List<SimpleFBUser> notattendingmembers) {
        if(notattendingmembers.size() == 0 || notattendingmembers.size() < offset)
        {  
             nomoreNotAttending = true; 
        }
        else
        {
            nomoreNotAttending = false;
        }
       
    }

    protected void setNomoreUnsure(List<SimpleFBUser> unsuremembers) {
      
        if(unsuremembers.size() ==0 || unsuremembers.size() < offset)
        {
            nomoreUnsure = true;
        }
        else
        {
            nomoreUnsure = false;
        }
        
    }

    protected void setNomoreAttending(List<SimpleFBUser> attendingmembers) {
        
        if(attendingmembers.size() == 0 || attendingmembers.size() < offset)
        {
            nomoreAttending = true;
        }
        else
        {
            nomoreAttending = false;
        }
        
    }

    @Override
	protected void loadRefresh()
    {   
        if(this.isInProcess() == true)
        {
            showToast();
            return;
        }
        currentAttendingPos     = 0;
        currentUnsurePos        = 0;
        currentNotAttendingpos  = 0;
        currentNotRepliedPos    = 0;
    	launchGetEventGuestInfo();
    }
    
    private void launchGetEventGuestInfo()
    {
        handler.obtainMessage(FACEBOOK_EVENT_GUEST_GET).sendToTarget();
    }
    @Override
    protected void createHandler() 
    {
       handler = new EventHandler();
    }
    
    final int FACEBOOK_EVENT_GUEST_GET     = 0;
    final int FACEBOOK_EVENT_GUEST_UI      = 1;
    final int FACEBOOK_EVENT_GUEST_GET_END = 2;
    final int FACEBOOK_GUEST_LOAD_OLD_GET  = 3;
    private class EventHandler extends Handler 
    {
           public EventHandler()
           {
               super();            
               Log.d(TAG, "new EventHandler");
           }
           
           @Override
           public void handleMessage(Message msg) 
           {
             switch(msg.what)
             {
                 case FACEBOOK_EVENT_GUEST_GET:
                 {
                     batch_run_loadEventGuest();
                     break;
                 }
                 case FACEBOOK_EVENT_GUEST_UI:
                 {
                     guest_list.setOnItemClickListener(itemClick);
                     updateSubTabUI();
                     break;
                 }
                 case FACEBOOK_EVENT_GUEST_GET_END:
                 {
                     footerText = getString(R.string.load_more_msg);
                     showFooterViewText(footerText);                           
                     end();
                     break;
                 }
                 case FACEBOOK_GUEST_LOAD_OLD_GET:
                 {
                     loadOlderGuest();
                     break;
                 }
             
             }
          }
    } 

    public void setTitle() {
        title =  this.getString(R.string.facebook_event_title_bar);  
    }

    public void registerAccountListener() 
    {
        AccountManager.registerAccountListener("FacebookEventActivity", this);      
    }
    public void unregisterAccountListener() 
    {
        AccountManager.unregisterAccountListener("FacebookEventActivity");      
    }

    public CharSequence getFooterText() {
        return footerText;
    }

}
