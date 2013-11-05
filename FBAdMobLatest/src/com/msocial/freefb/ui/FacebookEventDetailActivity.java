package com.msocial.freefb.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.service.FacebookLoginHelper;
import com.msocial.freefb.ui.view.ImageRun;
import com.msocial.freefb.ui.view.SNSItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.util.StringUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookEventDetailActivity extends FacebookBaseActivity{
    final String TAG="FacebookEventDetailActivity";
    private View      eventTypeView;
    private View      eventLocationView;
    private View      eventStartView;
    private View      eventEndView;
    private View      eventHostView;
    
    private ImageView imageview;
    private TextView  hostview;
    private TextView  typeview;
    private TextView  locationview;
    private TextView  startview;
    private TextView  nameview;
    private TextView  endview;
    private TextView  eventtimeview;
    
    private TextView  descview;
    
    private Event item;
    
    private ListView optList;
    List<EventOptItem> optItems;
    
    private int current_rsvp_index  = -1;
    private int selected_rsvp_index = -1;
    static final String NOTREPLIED   = "not_replied";
    static final String ATTENDING    = "attending";
    static final String UNSURE       = "unsure";
    static final String DECLINED     = "declined";
    boolean fornotification = false;
    
    String[] RSVP_ARRAY = {ATTENDING,UNSURE,DECLINED};
    Map<String, String>stateMap = new HashMap<String, String>();
    
    public void onCreate(Bundle savedInstanceState)
    {
       super.onCreate(savedInstanceState);        
       setContentView(R.layout.facebook_event_detail_ui);
       
       optList = (ListView)findViewById(R.id.facebook_event_opt);
       
       eventTypeView = (View)findViewById(R.id.facebook_event_type);
       eventLocationView = (View)findViewById(R.id.facebook_event_location);
       eventStartView  = (View)findViewById(R.id.facebook_event_start);
       eventEndView  = (View)findViewById(R.id.facebook_event_end);
       eventHostView  = (View)findViewById(R.id.facebook_event_host);
             
       imageview  = (ImageView)findViewById(R.id.facebook_event_img_ui);
       imageview.setImageDrawable(getResources().getDrawable(R.drawable.event));
       nameview   = (TextView)findViewById(R.id.facebook_event_name);
       hostview   = (TextView)findViewById(R.id.facebook_event_host_detail);
       typeview   = (TextView)findViewById(R.id.facebook_event_type_detail);
       locationview  = (TextView)findViewById(R.id.facebook_event_location_detail);
       startview   = (TextView)findViewById(R.id.facebook_event_start_detail);
       endview   = (TextView)findViewById(R.id.facebook_event_end_detail);
       descview  = (TextView)findViewById(R.id.facebook_event_des_detail);
       eventtimeview = (TextView)findViewById(R.id.facebook_event_time);
       
       stateMap.put(NOTREPLIED, "Not Replied");
       stateMap.put(ATTENDING,  "Attending");
       stateMap.put(UNSURE,     "Maybe Attending");
       stateMap.put(DECLINED,   "Not Attending");
       
       setTitle();
       setTitle(title);
       fornotification = getIntent().getBooleanExtra("fornotification", false);
       SocialORM.Account account = orm.getFacebookAccount();
       if(checkFacebookAccount(this, account))
       {
           perm_session = loginHelper.getPermanentSesstion(this);
           if(perm_session != null)
           {
               perm_session.attachActivity(this);
               facebookA = new AsyncFacebook(perm_session);
               getEventDetailInfoFromIntent(getIntent());
               initOptList();
               if(item!= null)
               {
                   setCurrentRsvpIndex(item.rsvp_status);
               }
               launchGetEventDetailInfo();
           }
           else
           {
               launchFacebookLogin();
           }
       }     
    } 
    
    @Override
    protected void loadRefresh() {
        super.loadRefresh();
        if(this.isInProcess() == true)
        {
            showToast(); 
            return;
        }
        launchGetEventDetailInfo();
    }
    
    private void getEventDetailInfoFromIntent(Intent intent)
    {
    	long eid = intent.getLongExtra("eid", -1);
    	item = orm.getFacebookevent(eid);
    	
        if(item != null && isEmpty(item.pic_big) == false)
        {
            ImageRun imagerun = new ImageRun(handler, item.pic_big, 1);       
            imagerun.setImageView(imageview);
            imagerun.noimage    = true;   
            imagerun.post(imagerun);
        }
		SimpleDateFormat sdf = new SimpleDateFormat("h:mm a EEE, MMM d",Locale.US);//, ''yy");
		sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        nameview.setText(item.name);
        if(StringUtils.isEmpty(item.event_type) || StringUtils.isEmpty(item.event_sbytype))
        {
            eventTypeView.setVisibility(View.GONE);
        }
        else
        {
            eventTypeView.setVisibility(View.VISIBLE);
            typeview.setText(item.event_type+"-"+item.event_sbytype);   
        }
        startview.setText(sdf.format(item.start_time));
        eventtimeview.setText(sdf.format(item.start_time));
        endview.setText(sdf.format(item.end_time));
        
        if(StringUtils.isEmpty(item.description)==false)
        {
            descview.setText(item.description);
        }
        else
        {
            descview.setText(R.string.facebook_event_no_desc);
            //descview.setGravity(Gravity.CENTER_HORIZONTAL);
            //descview.setTextSize(25);
        }
       
        locationview.setText(item.location);  
    }
    
    private void initOptList()
    {
        if(item!=null)
        {
            optItems = new ArrayList<EventOptItem>();
            optItems.add(new EventOptItem(EventOptItem.RSVP_TYPE,item.rsvp_status));     
            optItems.add(new EventOptItem(EventOptItem.HOST_TYPE,String.format(getString(R.string.facebook_event_host_item), item.host)));
            optItems.add(new EventOptItem(EventOptItem.GUEST_TYPE,getString(R.string.facebook_event_guest_item)));
            FacebookEventOptAdapter optAdapter = new FacebookEventOptAdapter(mContext,optItems);
            optList.setAdapter(optAdapter);
            
            optList.setOnItemClickListener(itemClick);
        }      
    }
    
    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener()
    {

       public void onItemClick(AdapterView<?> arg0, View v, int pos,long id) {
           Log.d(TAG, "do OptItem click");
           if(FacebookOptItemView.class.isInstance(v))
           {
               FacebookOptItemView view = (FacebookOptItemView)v;
               EventOptItem optItem = view.getMItem();
               if(optItem != null)
               {
                   if(optItem.type == EventOptItem.RSVP_TYPE)
                   {
                       if(isChangingRSVP == true)
                       {
                           handler.post(new Runnable(){
                               public void run()
                               {
                                   Toast.makeText(mContext, R.string.facebook_toast_event_rsvp, Toast.LENGTH_SHORT).show();
                               }
                           });
                           return;
                       }
                       else
                       {
                           showRSVPChoiceDialog();
                       }
                   }
                   else if(optItem.type == EventOptItem.HOST_TYPE)
                   {
                       //to FacebookAccountActivity
                       if(item !=null)
                       {
                           Intent intent = new Intent(mContext, FacebookAccountActivity.class);
                           intent.putExtra("uid",item.getCreator());
                           intent.putExtra("username",item.host);                 
                           ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
                       }
                       
                   }
                   else if(optItem.type == EventOptItem.GUEST_TYPE)
                   {
                       // TODO view GUEST MEMBER LIST
                       showGuestList();
                   }
               }
           }
       }
        
    };
    
    private void showGuestList()
    {
        Intent intent = new Intent(mContext,FacebookEventGuestActivity.class);
        intent.putExtra("eid", item.eid);
        startActivity(intent);
        
    }
    
    private void showRSVPChoiceDialog()
    {
        Log.d(TAG," show RSVPChociceDialog ");
        new AlertDialog.Builder(FacebookEventDetailActivity.this)
        .setSingleChoiceItems(R.array.entries_list_rsvp_status, current_rsvp_index, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                 selected_rsvp_index = whichButton;
                     
            }
        }).setPositiveButton(R.string.sns_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(selected_rsvp_index == 0)
                {
                    //attening
                    event_rsvp("attending"); 
                }
                else if(selected_rsvp_index == 1)
                {
                    //unsure
                    event_rsvp("unsure");
                    
                }else if(selected_rsvp_index == 2)
                {
                    //declined
                    event_rsvp("declined");
                }
                
                if(selected_rsvp_index >= 0)
                {
                   refreshRSVPUI(RSVP_ARRAY[selected_rsvp_index]);
                }  
            }
        })
        .setNegativeButton(R.string.sns_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                /* User clicked No so do some stuff */
            }
        }).create().show();
    }
    
    private void event_rsvp(String rsvp_status)
    {
        if(item!=null)
        {
            Message mesd = handler.obtainMessage(EVENT_RSVP);
            mesd.getData().putLong("eid", item.eid);
            mesd.getData().putString("rsvp_status",rsvp_status );
            handler.sendMessage(mesd);
        }  
    }
    
    private void launchGetEventDetailInfo()
    {
        if(optList.getVisibility() == View.VISIBLE)
        {
            handler.obtainMessage(FACEBOOK_RSVP_GET).sendToTarget();
        }
    }
    
    private void getFacebookRSVP()
    {
        if(isInProcess() == true)
        {
            return;
        }
        begin();
        
        Log.d(TAG, "before get getFacebookRSVP");
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
            facebookA.getRSVP(item.eid,perm_session.getLogerInUserID(),new FacebookAdapter()
            {
                @Override public void getRSVP(String rsvp_status)
                {
                    Log.d(TAG,"rsvp_status is "+rsvp_status);                   
                    synchronized(mLock)
                    {
                        inprocess = false;
                    }     
                    if(isEmpty(item.rsvp_status) == true || !item.rsvp_status.equalsIgnoreCase(rsvp_status))
                    {
                        //update Event rsvp_status;
                        item.rsvp_status = rsvp_status;
                        orm.updateFacebookevent(item);
                    }
                    Message msd = handler.obtainMessage(FACEBOOK_RSVP_GET_END);
                    msd.getData().putBoolean(RESULT, true);
                    msd.getData().putString("RSVP_STATUS", rsvp_status);
                    setCurrentRsvpIndex(rsvp_status);
                   
                    handler.sendMessage(msd);
                }
                
                @Override public void onException(FacebookException e, int method) 
                {
                    synchronized(mLock)
                    {
                        inprocess = false;
                    }
                    //get from Database
                    Message msd = handler.obtainMessage(FACEBOOK_RSVP_GET_END);
                    msd.getData().putBoolean(RESULT, false);
                    handler.sendMessage(msd);
                }
             });
         }
    }
    
    protected void setCurrentRsvpIndex(String rsvp_status) {
        if(rsvp_status == null)
        {
            current_rsvp_index = -1;
        }
        else
        {
            if(rsvp_status.equals(DECLINED))
            {
                current_rsvp_index = 2;
            }
            else if(rsvp_status.equals(UNSURE))
            {
                current_rsvp_index = 1;
            }
            else if(rsvp_status.equals(ATTENDING))
            {
                current_rsvp_index = 0;
            }
            else
            {
                current_rsvp_index = -1;
            }
        }    
    }

    boolean isChangingRSVP = false;
    Object mLock_1 = new Object();
    private void process_event_rsvp(final long eid,String rsvp_status)
    {
        FacebookSession perm_session = null;
        
        if(perm_session == null)
        {
             perm_session = FacebookLoginHelper.instance(mContext).getPermanentSesstion();
             if(ActivityBase.class.isInstance(mContext))
             {
                perm_session.attachActivity((NetworkConnectionListener)mContext);
             }
        }
        
        synchronized(mLock_1)
        {
            isChangingRSVP = true;
        }
        AsyncFacebook facebookA = new AsyncFacebook(perm_session);         
        final String action=rsvp_status;
        facebookA.facebookEventRSVPAsync(eid,rsvp_status,new FacebookAdapter()
        {
            @Override public void facebookEventRSVP (boolean suc)
            {   
                synchronized(mLock_1)
                {
                    isChangingRSVP = false;
                }
                
                if(suc)
                {
                    //update Event rsvp_status;
                    item.rsvp_status = action;
                    orm.updateFacebookevent(item);
                }
                
                handler.post( new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(mContext, String.format(getString(R.string.facebook_event_rsvp_suc), stateMap.get(action)), Toast.LENGTH_SHORT).show();
                        current_rsvp_index = selected_rsvp_index;
                        if(current_rsvp_index > -1 )
                        {
                            refreshRSVPUI(RSVP_ARRAY[current_rsvp_index]);
                        }
                        
                    }
                });
                Message msg = handler.obtainMessage(EVENT_RSVP_END);
                msg.getData().putBoolean(RESULT, true);
                msg.getData().putLong("eid",eid);
                msg.sendToTarget();
                Log.d(TAG," facebook event rsvp suc=" + suc);
            }
            
            @Override public void onException(FacebookException e, int method) 
            {
                synchronized(mLock_1)
                {
                    isChangingRSVP = false;
                }
                Message msg = handler.obtainMessage(EVENT_RSVP_END);
                msg.getData().putBoolean(RESULT, false);
                msg.getData().putLong("eid",eid);
                msg.sendToTarget();
                handler.post( new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(mContext,  String.format(getString(R.string.facebook_event_rsvp_fail), action) , Toast.LENGTH_SHORT).show();
                        if(current_rsvp_index > -1)
                        {
                            refreshRSVPUI(RSVP_ARRAY[current_rsvp_index]);
                        }
                        else
                        {
                            refreshRSVPUI("");
                        }
                        
                    }
                });
                Log.d(TAG," facebook event rsvp failed "+e.getMessage());
            }
         });
        
    }
    
    private void refreshRSVPUI(String rsvp_status)
    {
        EventOptItem item = optItems.get(0);   
        item.label = rsvp_status;
        FacebookEventOptAdapter optAdapter = new FacebookEventOptAdapter(mContext,optItems);
        optList.setAdapter(optAdapter);
    }
    
    @Override
    protected void createHandler() 
    {
       handler = new EventHandler();
    }
    
    final int FACEBOOK_RSVP_GET     = 0;
    final int FACEBOOK_RSVP_GET_END = 1;
    final int EVENT_RSVP            = 2;
    final int EVENT_RSVP_END        = 3;
    
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
               case FACEBOOK_RSVP_GET:
               {
                   getFacebookRSVP();
                   break;
               }  
               case FACEBOOK_RSVP_GET_END:
               {
                   end();                  
                   boolean res = msg.getData().getBoolean(RESULT, false);
                   if(res == true)
                   { 
                       String rsvp_status = msg.getData().getString("RSVP_STATUS");
                       refreshRSVPUI(rsvp_status);
                      
                   }
                   break;
               }
               case EVENT_RSVP:
               {
                   long eid = msg.getData().getLong("eid");
                   String rsvp_status = msg.getData().getString("rsvp_status");
                   process_event_rsvp(eid,rsvp_status);
                   break;
               }
               case EVENT_RSVP_END:
               {
                   boolean result = msg.getData().getBoolean(RESULT);
                   long eid = msg.getData().getLong("eid");
                   if(fornotification == true && result == true && eid > 0 )
                   {
                      Intent intent = new Intent();
                      intent.putExtra("eid", eid);
                      setResult(RESULT_OK,intent);
                      finish(); 
                   }
               }
             }
          }
    } 

    public void setTitle() {
        title = this.getString(R.string.facebook_event_title_bar);     
    }

    public void registerAccountListener() 
    {
        AccountManager.registerAccountListener("FacebookEventActivity", this);      
    }
    public void unregisterAccountListener() 
    {
        AccountManager.unregisterAccountListener("FacebookEventActivity");      
    }
    
    class EventOptItem 
    {
        int type;
        String label;
        
        EventOptItem(int type,String label)
        {
            this.type = type;
            this.label = label; 
        }
        
        final static int RSVP_TYPE    = 0;
        final static int HOST_TYPE   = 1;
        final static int GUEST_TYPE = 2;     
    }
    
    class FacebookEventOptAdapter extends BaseAdapter
    {   
         private Context mContext;
         private List<EventOptItem> mOptItem;
         
         public FacebookEventOptAdapter(Context con,  List<EventOptItem> optItem )
         {
            mContext = con;
            mOptItem = optItem;       
           
         }
        
        public int getCount() { 
            return mOptItem.size();
        }

        public Object getItem(int position) {
            return mOptItem.get(position);
        }

        public long getItemId(int position) {
            return mOptItem.get(position).type;
        }

        public View getView(int position, View convertView, ViewGroup arg2) 
        {       
             if (position < 0 || position >= getCount()) 
             {
                 return null;    
             }
             
             FacebookOptItemView v=null;
        
             EventOptItem di = (EventOptItem)getItem(position);
             if (convertView == null ) 
             {
                 v = new FacebookOptItemView(mContext, di);
             } 
             else
             {
                 v = (FacebookOptItemView) convertView;
                 v.setMItem(di);
             }   
             return v;
        }   
        
    }
    
    class FacebookOptItemView extends SNSItemView
    {
        EventOptItem mItem;
        Context mContext;
        TextView optView;
        TextView rsvpView;
        
        public EventOptItem getMItem() {
            return mItem;
        }

        public void setMItem(EventOptItem item) {
            mItem = item;
            setUI();
        }
        
        public FacebookOptItemView(Context ctx, AttributeSet attrs) {
            super(ctx, attrs);
            init();
        }
        
        public FacebookOptItemView(Context context,EventOptItem item)
        { 
            super(context);
            mItem = item;
            mContext = context;     
            init();
        }

        public FacebookOptItemView(Context context) {
            super(context);
            mContext = context;
            init();
        }
        
        private void init()
        {
            LayoutInflater factory = LayoutInflater.from(mContext);
            removeAllViews();
            
            View v  = factory.inflate(R.layout.facebook_event_opt_item, null);       
            v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));            
            addView(v);         
            optView = (TextView)this.findViewById(R.id.facebook_opt_name);
            rsvpView = (TextView)this.findViewById(R.id.facebook_opt_name2);
            setUI();
        }
        
        private void setUI()
        {
            if(mItem!=null)
            {   
                 if(mItem.type == EventOptItem.RSVP_TYPE)
                {
                     rsvpView.setVisibility(View.VISIBLE);
                     StringBuilder sb = new StringBuilder(); 
                     sb.append(mContext.getString(R.string.facebook_event_rsvp_prefix));
                     sb.append(" ");
                     String respond = mContext.getString(R.string.facebook_event_rsvp_respond);
                     if(isEmpty(mItem.label))
                     {
                         
                     }
                     else if(mItem.label.equalsIgnoreCase(FacebookEventDetailActivity.NOTREPLIED))
                     {
                         sb.append(stateMap.get(mItem.label)+"  ");
                         respond = mContext.getString(R.string.facebook_event_rsvp_respond);
                     }
                     else 
                     {
                         sb.append(stateMap.get(mItem.label)+"  ");
                         respond = mContext.getString(R.string.facebook_event_rsvp_change);
                     }
                     optView.setText(sb.toString());
                     rsvpView.setText(respond);
                  /* SpannableStringBuilder ssb = new SpannableStringBuilder();
                   ssb.append(mContext.getText(R.string.facebook_event_rsvp_prefix));
                   String respond = mContext.getString(R.string.facebook_event_rsvp_respond);
                   int start = 0;
                   int end = 0;
                   if(isEmpty(mItem.label))
                   {   
                       ssb.append("  ");
                       ssb.append(respond);
                   }
                   else if(mItem.label.equalsIgnoreCase(FacebookEventDetailActivity.NOTREPLIED))
                   {
                       ssb.append(stateMap.get(mItem.label)+"  ");
                       respond = mContext.getString(R.string.facebook_event_rsvp_respond);
                       ssb.append(respond);
                   }
                   else 
                   {
                       ssb.append(stateMap.get(mItem.label)+"  ");
                       respond = mContext.getString(R.string.facebook_event_rsvp_change);
                       ssb.append(respond);
                   }
                   String text = ssb.toString();
                   SpannableString sss = new SpannableString(text);
                   end = text.length();
                   start = end - respond.length();
                   sss.setSpan(new UnderlineSpan(),start, end,Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                   optView.setText(sss);*/
                }
                else
                {
                    rsvpView.setVisibility(View.GONE);
                    if(mItem.label!= null)
                        optView.setText(mItem.label);
                }
                 
            }         
        }

        @Override
        public String getText() {
            
            return "";
        }
        
    }

}
