package com.msocial.facebook.ui;
import java.util.ArrayList;
import java.util.List;
import com.msocial.facebook.R;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.providers.SocialORM.FacebookeventCol;
import com.msocial.facebook.service.SNSService;
import com.msocial.facebook.ui.adapter.FacebookEventAdapter;
import com.msocial.facebook.ui.view.FacebookEventItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.util.ArrayUtils;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookEventActivity extends FacebookBaseActivity{
     final String TAG="FacebookEventActivity";
     private ListView contentList; 
     TextView eventinfoview;
     View     infoSpan;
     
     private boolean fornotification;
     private long[] eids;
     
     private Cursor event  ;
     private List<Event> notification_event   = new ArrayList<Event>();
     
     private int curEventPos = 0;   
     private int offset = 20;
     private boolean withFooterView = false;
     private String footerText = "";
     private boolean simpleViewMode=true;
     
     private int visibleposition = -1;
     @Override protected void onDestroy() 
     {
         super.onDestroy();
         
         if(event != null)
         {
             try{
                 event.close();
             }catch(Exception ne){}
             event = null;
         }
     }

     public void onCreate(Bundle savedInstanceState)
     {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.facebook_event_ui);
        contentList = (ListView)this.findViewById(R.id.facebook_event_list);
        contentList.setFocusableInTouchMode(true);
        contentList.setFocusable(true);
        contentList.setOnCreateContextMenuListener(this);
        contentList.setOnItemClickListener(itemClick);
        
        infoSpan      = (View)findViewById(R.id.facebook_info_span);
        eventinfoview = (TextView)findViewById(R.id.facebook_info);
        fornotification = this.getIntent().getBooleanExtra("fornotification", false);
        eids = this.getIntent().getLongArrayExtra("eids");
        
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
                if(fornotification == true)
                {
                    event = orm.getFacebookeventsNotificationCursor(eids);
                    
                    if(event != null && event.getCount()>0)
                    {
                        handler.obtainMessage(FACEBOOK_EVENT_UI).sendToTarget();
                    }
                    launchLoadEvents();
                }
                else
                {
                    event = orm.getFacebookeventsCursor();
                    
                    if(event == null || event.getCount() == 0)
                    {
                        launchLoadEvents();
                    }
                    else
                    {
                		
                		handler.obtainMessage(FACEBOOK_EVENT_UI).sendToTarget();
                    }
                }
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
            if(FacebookEventItemView.class.isInstance(v))
            {
                FacebookEventItemView view = (FacebookEventItemView)v;
                Event item = view.getEvent();
                if(item != null)
                {
                    Intent intent = new Intent(mContext,FacebookEventDetailActivity.class);
                    intent.putExtra("eid", item.eid);
                    intent.putExtra("fornotification",fornotification);
                    //startActivity(intent);
                    startActivityForResult(intent, EVENT_REQUEST_CODE);
                }
            }
        }
         
     }; 
     
     @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {     
        switch(requestCode)
        {  
              case EVENT_REQUEST_CODE:
              {
                  Log.d(TAG, "back from event detail");
                  if(intent != null)
                  {
                      long eid = intent.getLongExtra("eid", -1);
                      Log.d(TAG,"eid is ="+eid);
                      removeEid(eid);
                      reShowUI();
                  }
                 
                  break;
              }
             
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void reShowUI() {
        if(event != null)
        {
            event.close();
            event = null;
        }
        event = orm.getFacebookeventsNotificationCursor(eids);
        handler.obtainMessage(FACEBOOK_EVENT_UI).sendToTarget();
    }

    private void removeEid(long eid) {
        
        if(eid > 0 && eids != null)
        {
            synchronized(eids)
            {
                for(int i=0;i<eids.length;i++)
                {
                    if(eids[i]==eid)
                    {
                        eids[i] = 0;
                    }
                }
            }
           
        }
       
    }

    private void launchLoadOlderEvent()
     {
         handler.obtainMessage(FACEBOOK_EVENT_LOAD_OLD_GET).sendToTarget();
     }
     
     public View.OnClickListener loadOlderEventClick = new View.OnClickListener()
     {
         public void onClick(View v) 
         {
             Log.d(TAG, "load older Event"); 
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
                    Toast.makeText(FacebookEventActivity.this, text + " please wait...", Toast.LENGTH_SHORT).show();   
                 } 
                });
             }
             else
             {
                 launchLoadOlderEvent();
             }
             
         }
     };
     
     private void updateSubTabUI(boolean refresh)
     { 
         int lastPosition = contentList.getLastVisiblePosition();
         Log.d(TAG,"lastVisible position is "+lastPosition);
         FacebookEventAdapter fea = new FacebookEventAdapter(FacebookEventActivity.this, event,withFooterView, simpleViewMode);
         contentList.setAdapter(fea); 
         //if after refreshing, do not set select position.
         if(refresh == false)
         {
             contentList.setSelection(lastPosition+1);
         }
     }   
     
     public void setTitle() 
     {
         title = this.getString(R.string.facebook_event_title_bar);    
     }
               
     /*
     @Override
     public void titleSelected() 
     {  
        super.titleSelected();
        if(!fornotification)
        {
            Log.d(TAG, "sync event to calendar");
            doGlobalSyncToEvent();
        }        
    }*/
     
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
            
            launchLoadEvents();
        }
        
        
    }
    
    @Override
    protected void doClearCache()
    {
        Log.d(TAG,"clear chache");
        handler.obtainMessage(FACEBOOK_CLEAR_CACHE).sendToTarget();
    }
    
    protected void clearCache()
    {
       Cursor cursor = orm.getFacebookeventsCursor();
       
       if(cursor != null && cursor.getCount()>0)
       {
           List<Long> ceids = new ArrayList<Long>(); //store calendar eventid
           while(cursor.moveToNext())
           { 
               Event event = orm.formatEvent(cursor);
               if(event.ceid>0) ceids.add(event.ceid);
           }    
           int ret = orm.deleteAllFacebookEvent();  
           if(ret>=0)
           {
              Toast.makeText(mContext, R.string.facebook_clear_cache_successfully, Toast.LENGTH_SHORT).show();
           }
           else
           {
               Toast.makeText(mContext, R.string.facebook_clear_cache_failed, Toast.LENGTH_SHORT).show();
           } 
           contentList.setAdapter(null);
           cursor.close();
           cursor = null;
           handler.obtainMessage(FACEBOOK_EVENT_UI).sendToTarget();
       }   
      
    }    
     
     protected void launchLoadEvents(){
         handler.obtainMessage(FACEBOOK_EVENT_GET).sendToTarget();
     }
     
     private void notifyLoading() {
         notify.notifyOnce(R.string.facebook_event_loading, R.drawable.facebook_logo, 30*1000); 
     }
     
     @Override
     protected void doGlobalSyncToEvent()
     {
         Log.d(TAG, "start global sync event"); 
         Intent intent = new Intent(FacebookEventActivity.this,SNSService.class);
         intent.putExtra("eventsync", true);
         this.startService(intent);
     }
     
     @Override
     protected void loadRefresh()
     {
         if(this.isInProcess() == true)
         {
             showToast();
             return;
         }
         curEventPos = 0;
         SocialORM.Account account = orm.getFacebookAccount();
         if(checkFacebookAccount(this, account))
         {
              perm_session = loginHelper.getPermanentSesstion(this);
              if(perm_session != null)
              {
                  perm_session.attachActivity(this);
                  facebookA = new AsyncFacebook(perm_session);
                  Message message = handler.obtainMessage(FACEBOOK_EVENT_GET);
                  message.getData().putBoolean("refresh", true);
                  message.sendToTarget();
              }
              else
              {
                  launchFacebookLogin();
              }
         }
      }
     
     @Override
     protected void doCancel(Event event)
     {
         if(perm_session == null)
         {
              perm_session = loginHelper.getPermanentSesstion();
              if(facebookA == null)
              {
                  facebookA = new AsyncFacebook(perm_session);
              }             
         }           
         if(facebookA != null)
         {
             facebookA.setSession(perm_session);
             if(event.creator != perm_session.getLogerInUserID())
             {
                Log.d(TAG," has no privilege to cancel event");
                Toast.makeText(mContext, R.string.event_cancel_no_privilege, Toast.LENGTH_SHORT).show();
                return;
             }
             final long eventid = event.eid;
             Event tempevent = null;
             if(event.ceid <= 0)
             {
                tempevent = orm.getFacebookevent(eventid);
                if(tempevent!=null)
                {
                    event.ceid = tempevent.ceid;
                }
             }
             final long ceid = event.ceid;

             facebookA.cancelEvent(eventid, new FacebookAdapter()
             {
                 @Override public void event_cancel(boolean retvalue)
                 {
                     Log.d(TAG,"cancel event successfully "+retvalue);
                     if(retvalue)
                     {
                         orm. deleteFacebookEvent(eventid);                         
                         //refresh UI  
                         handler.obtainMessage(FACEBOOK_EVENT_UI).sendToTarget();
                         
                         handler.post(new Runnable(){
                            public void run(){
                                Toast.makeText(mContext,R.string.event_cancel_successfully, Toast.LENGTH_SHORT).show();                                                     
                            } 
                         });
                     }
                     else
                     {
                         Log.d(TAG,"cancel Event fail ");  
                         handler.post(new Runnable()
                         {
                             public void run()
                             {
                                 Toast.makeText(mContext, R.string.event_cancel_failed, Toast.LENGTH_SHORT).show();
                             } 
                         });
                     }
                         
                 }
                 
                 @Override public void onException(FacebookException e, int method) 
                 {
                    //get from Database
                    Log.d(TAG,"cancel Event exception "+e.getMessage());   
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(mContext, R.string.event_cancel_failed, Toast.LENGTH_SHORT).show();
                        } 
                    });
                 }
             });
         }
     }
     
     protected void getNotificationFacebookEvent(long[] tmpids)
     {
        if(isInProcess())
        {
            return;
        }        
        begin();    
        Log.d(TAG, "before get getNotificationFacebookEvent");
        //notifyLoading();  
        synchronized(mLock)
        {
            inprocess = true;
        }
        
        if(perm_session == null)
        {
             perm_session = loginHelper.getPermanentSesstion();
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
            facebookA.getNotificationFacebookEventsAsync(tmpids,new FacebookAdapter()
            {
                @Override public void getFacebookEvents(List<Event> results)
                {
                    synchronized(mLock)
                    {
                        inprocess = false;
                    } 
                    notification_event = results;  
                    
                    if(notification_event!=null && notification_event.size()>0)
                    {
                        for(Event event : notification_event)
                        {  
                            event.fornotification = true;
                        }                                  
                        orm.addFacebookevent(notification_event);  
                        if(donotcallnetwork == false)//I am still alive
                        {
                            handler.obtainMessage(FACEBOOK_EVENT_UI).sendToTarget();
                            //cancelNotify();
                        }             
                    }
                    
                    Message msd = handler.obtainMessage(FACEBOOK_NOTIFY_EVENT_GET_END);
                    msd.getData().putBoolean(RESULT, true);
                    handler.sendMessage(msd);
                }
                
                @Override public void onException(FacebookException e, int method) 
                {
                    synchronized(mLock)
                    {
                        inprocess = false;
                    }
                    if(donotcallnetwork == false )//I am still alive
                    {   
                         //cancelNotify();
                    }   
                    //get from Database
                    Message msd = handler.obtainMessage(FACEBOOK_NOTIFY_EVENT_GET_END);
                    msd.getData().putBoolean(RESULT, false);
                    handler.sendMessage(msd);
                }
             });
         }
     }
     
     private void loadOldEvents()
     {
           getFacebookEvent(false);  
     }
     
     protected void getFacebookEvent(final boolean refresh){
         if(isInProcess())
         {
             return;
         }
         begin();
         //set load older button text
         //process for UI
         footerText = getString(R.string.loading_event);
         showFooterViewText(footerText);
         Log.d(TAG, "before get getFacebookEvent");
         
         //notifyLoading();  
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
             facebookA.getFacebookEventsAsync(curEventPos,offset,new FacebookAdapter(){
                 @Override
                 public void getFacebookEvents(List<Event> events) {
                     synchronized(mLock)
                     {
                        inprocess = false;
                     }
                     if(events!=null && events.size()>0)
                     {
                         setNomoreEvents(events);
                         orm.addFacebookevent(events);
                         if(donotcallnetwork == false)//I am still alive
                         {
                             Message mssd = handler.obtainMessage(FACEBOOK_EVENT_UI);
                             mssd.getData().putBoolean("refresh", refresh);
                             mssd.sendToTarget();
                         }
                         
                         curEventPos += events.size();
                     }
                      
                      if(donotcallnetwork == false )//I am still alive
                      {   
                           //cancelNotify();
                      }       
                     Message msd = handler.obtainMessage(FACEBOOK_EVENT_GET_END);
                     msd.getData().putBoolean(RESULT, true);
                     handler.sendMessage(msd);
                 }

                 @Override
                 public void onException(FacebookException te, int method) {
                     synchronized(mLock)
                     {
                         inprocess = false;
                     }
                     if(donotcallnetwork == false )//I am still alive
                     {   
                          //cancelNotify();
                     }   
                     //get from Database
                     Message msd = handler.obtainMessage(FACEBOOK_EVENT_GET_END);
                     msd.getData().putBoolean(RESULT, false);
                     handler.sendMessage(msd);
                 }
             });
          }
      }
    
   
     private void showFooterViewText(String footerText) {
         for(int i= contentList.getChildCount()-1;i>0;i--)            
         {
             View v = contentList.getChildAt(i);
             if(Button.class.isInstance(v))
             {
                 Button bt = (Button)v;
                 bt.setText(footerText);
                 break;
             }
         } 
    }
     
    private void setNomoreEvents(List<Event> events) {
         if(events.size() == 0 || events.size() < offset)
         {
             withFooterView = false;
         }
         else
         {
             withFooterView = true;
         }         
     }
     
     @Override
     protected void createHandler() 
     {
        handler = new EventHandler();
     }
     
     @Override
     protected void switchEvent()
     {
         simpleViewMode = !simpleViewMode;
         handler.obtainMessage(FACEBOOK_EVENT_UI).sendToTarget();   
     }
     
     final int FACEBOOK_EVENT_GET          = 0;
     final int FACEBOOK_EVENT_UI           = 1;
     final int FACEBOOK_EVENT_GET_END      = 2;
     final int FACEBOOK_NOTIFY_EVENT_GET_END      = 22;
     final int FACEBOOK_CLEAR_CACHE        = 3;
     final int FACEBOOK_EVENT_LOAD_OLD_GET = 4;
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
                case FACEBOOK_EVENT_GET:
                {
                    boolean refresh = msg.getData().getBoolean("refresh");
                    if(fornotification)
                    {
                        
                        if(event==null || event.getCount() != eids.length)
                        {
                            long[] tmpids = getEventidsNoinCache();
                            if(tmpids.length>0)
                            {
                                getNotificationFacebookEvent(tmpids);
                            }
                           
                        }

                    }
                    else
                    {
                    	//get from database, show UI                    	
                    	/*handler.obtainMessage(FACEBOOK_EVENT_UI).sendToTarget(); */
                        getFacebookEvent(refresh);
                    }
                    break;
                }
                case FACEBOOK_EVENT_UI:
                {
                    contentList.setAdapter(null);
                    if(event != null)
                    {
                        event.requery();
                    }     
                    contentList.setOnItemClickListener(itemClick);
                    boolean refresh = msg.getData().getBoolean("refresh");
                    updateSubTabUI(refresh);       
                    break;
                }
                case FACEBOOK_EVENT_GET_END:
                {
                    end();                  
                    boolean res = msg.getData().getBoolean(RESULT, false);
                    if(res == false)
                    {
                        showToast(getString(R.string.facebook_event_load_fail));
                    }
                    footerText = getString(R.string.load_more_msg);
                    showFooterViewText(footerText);
                    break;
                }   
                case FACEBOOK_NOTIFY_EVENT_GET_END:
                {
                    end();                  
                    boolean res = msg.getData().getBoolean(RESULT, false);
                    if(res == false)
                    {
                        showToast(getString(R.string.facebook_event_load_fail));
                    }
                    break;
                }
                case FACEBOOK_CLEAR_CACHE:
                {
                    clearCache();
                    break;
                }
                case FACEBOOK_EVENT_LOAD_OLD_GET:
                {
                    loadOldEvents();
                    break;
                }
              }
           }
     } 
    
    public void registerAccountListener() 
    {
        AccountManager.registerAccountListener("FacebookEventActivity", this);      
    }
    public long[] getEventidsNoinCache() {
        ArrayList<Long> e_ids = new ArrayList<Long>();
        for(int i=0;i<eids.length;i++)
        {
            if(orm.isFacebookeventExist(eids[i]) == false)
            {
                e_ids.add(eids[i]);
            }
        }
        
        long[] tmpids = new long[e_ids.size()];
        for(int j=0;j<tmpids.length;j++)
        {
            tmpids[j] = e_ids.get(j);
        }
        return tmpids;
    }
    public void unregisterAccountListener() 
    {
        AccountManager.unregisterAccountListener("FacebookEventActivity");      
    }
}

