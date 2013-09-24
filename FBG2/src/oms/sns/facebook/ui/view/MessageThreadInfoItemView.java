package oms.sns.facebook.ui.view;

import java.util.List;

import oms.sns.facebook.R;
import oms.sns.facebook.providers.SocialORM;
import oms.sns.facebook.ui.FacebookBaseActivity;
import oms.sns.facebook.ui.FacebookMailDetailActivity;
import oms.sns.service.facebook.model.MailboxMessage;
import oms.sns.service.facebook.model.MessageThreadInfo;
import oms.sns.service.facebook.model.Page;
import oms.sns.facebook.ui.adapter.MessageThreadInfoParcel;
import oms.sns.facebook.util.DateUtil;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.util.StringUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageThreadInfoItemView extends SNSItemView{

    private final String TAG="MessageThreadInfoItemView";
    
    private ImageView imageView;
    private TextView publishDate;
    private TextView publishSubject;
    private TextView username;  
    private MessageThreadInfo mthread;
    
    private ImageView readFlagView;
    private TextView  publishTxt;
    private View      body_region;
    
    static float mWidth = (float) 100.0;
    String  imageURL;
    Handler handler;
    SocialORM orm;
    FacebookUser user;
    Page page;
    private int mType = -1;
    private final static int  MESSAGES =0;
    private final static int  SENT     =1;
    private final static int  UPDATE   =2;
   
    public MessageThreadInfo getMailboxThread()
    {
        return mthread;
    }
    public String getImagePath()
    {
        return imageURL;
    }
    
    public boolean isUpdate()
    {
    	return mthread.isinbox==false && mthread.isoutbox==false;
    }
    public MessageThreadInfoItemView(Context ctx, AttributeSet attrs) 
    {
        super(ctx, attrs);
        mContext = ctx;
        orm = SocialORM.instance(mContext);
        setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);   
        
        handler = new MessageHandler();
    }
    private void updateUIFromUser()
    {
        handler.obtainMessage(UPDATE_UI).sendToTarget();
    }
    
    final int UPDATE_UI=0;
    final int UPDATE_PAT_UI=1;
    final int UPDATE_IMAGE_UI=2;
    final int UPDATE_PAGE_UI=3;
    public class MessageHandler extends Handler
    {
        public MessageHandler()
        {
            super();            
            Log.d(TAG, "new MessageHandler");
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
                        if(mthread.unread > 0)
                                username.setText( user.name+String.format(" (%1$s)", mthread.unread));
                         else
                                username.setText(user.name);
                        
                    }
                    break;
                }
                case UPDATE_PAGE_UI:
                {          
                    if(page != null)
                    {
                        if(mthread.unread > 0)
                                username.setText( page.name+String.format(" (%1$s)", mthread.unread));
                         else
                                username.setText(page.name);
                        
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
                        imageView.setImageResource(R.drawable.no_avatar);
                    }                   
                    else
                    {                       
                        ImageRun imagerun = new ImageRun(handler, url, 0);      
                        imagerun.setImageView(imageView);
                        imagerun.use_avatar = true;
                        imagerun.post(imagerun);
                    }
                    break;
                }
            }
        }           
    }
    
    public MessageThreadInfoItemView(Context context, MessageThreadInfo di) 
    {       
        super(context.getApplicationContext());
        mContext = context;
        mthread = di;
        
        orm = SocialORM.instance(mContext);
        Log.d(TAG, "call mail box MessageThreadInfoItemView");
        handler = new MessageHandler();
        init();
    }
    
    public MessageThreadInfoItemView(Context context, MessageThreadInfo di,int mType) 
    {       
        super(context.getApplicationContext());
        mContext = context;
        mthread = di;
        this.mType = mType;
        orm = SocialORM.instance(mContext);
        Log.d(TAG, "call mail box MessageThreadInfoItemView");
        handler = new MessageHandler();
        init();
    }
   
    public long getFromUID()
    {
        FacebookSession af = ((FacebookBaseActivity)mContext).getFSession();
        long meid=af.getLogerInUserID();
        MailboxMessage message = orm.getLatestedMessage(mthread.thread_id,meid);
        if(message != null)
        {
            meid = message.author;
            
            return meid;
        }
        
        
        if(mthread.messages != null && mthread.messages.size() > 0)
        {
            for(int i=0;i<mthread.messages.size();i++)
            {
                long uid = mthread.messages.get(i).author;
                if(uid != meid)
                {
                    meid = uid; 
                    break;
                }
            }        	
        }
        
        if(mthread.recipients!=null && mthread.recipients.size()>0)
        {
            //user also in receiver list, we need choose another user
            List<Long> receivers = mthread.recipients;
            if(receivers != null && receivers.size()> 0)
            {
                for(long item: receivers)
                {
                    if(item != af.getLogerInUserID())
                    {
                        meid = item;
                        break;
                    }
                }
            }
        }
        else
        {
           meid = mthread.snippet_author;
        }
        
        return meid;
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
                if((imageURL == null || imageURL.length() ==0 ) && (user.name == null || user.name.length() == 0))
                {
                    getFromDB = true;
                }
                
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
            imagerun.setImageView(imageView);
            imagerun.use_avatar = true;
            imagerun.post(imagerun);
        }
    }
    private String getDate()
    {
        long update_time = 0;
        if(mType == MESSAGES)
        {
            update_time = mthread.inbox_updated_time;
        }
        else if(mType == SENT)
        {
            update_time = mthread.outbox_updated_time;
        }
        else if(mType == UPDATE)
        {
            update_time = mthread.update_update_time;
        }
        
        return DateUtil.converToRelativeTime(mContext,update_time,true);
        //return mthread.getLastupdate().toLocaleString();
    }
    
    public String getSubject()
    {
        if(mthread.subject == null || mthread.subject.length()==0)
        {
            return "<no subject>";
        }
        
        return mthread.subject;
    }
    
    public String getText()
    {
        if(StringUtils.isEmpty(mthread.snippet))
        {
            return "";
        }
        
        return mthread.snippet;
    }
    
    public String getViewUserName() 
    {
        FacebookUser.SimpleFBUser tempuser = orm.getSimpleFacebookUser(this.getFromUID());
        if(tempuser != null)
        {
            if(mthread.unread > 0)
                return tempuser.name + String.format(" (%1$s)", mthread.unread);
            else
                return tempuser.name;
        }
        else
        {
            //begin to get all user information         
            if(FacebookBaseActivity.class.isInstance(mContext))
            {
                AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
                if(af != null)
                {   
                    long[] uids = {getFromUID()};
                    af.getBasicUsersAsync(uids, new FacebookAdapter()
                    {
                        @Override public void getUsers(List<FacebookUser> users)
                        {
                            if(users != null && users.size()>0)
                            {
                                Log.d(TAG, "after get author info="+users.size());
                                //update database
                                for(int i=0;i<users.size();i++)
                                {
                                    user = users.get(i);
                                    orm.addFacebookUser(user);
                                }
                                
                                updateUIFromUser(users);
                            }
                        }
                        
                        @Override public void onException(FacebookException e, int method) 
                        {
                            Log.d(TAG, "fail to get the author info"+e.getMessage());                                         
                        }
                    });
                }
            }
        }
        return ""; 
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
    public String getUserName() 
    {
        FacebookUser user = orm.getFacebookUser(getFromUID());
        if(user != null)
            return user.name;
        else
        {
            Page page = orm.getPageBypid(mthread.object_id);
            if(page != null)
            {
                return page.name;
            }
        }
        return "";
    }
        
    private void init() 
    {
        Log.d(TAG,  "call MessageItemView init");
        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();
        
        //container
        FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);
        FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);     
        view.setLayoutParams(paras);
        view.setVerticalScrollBarEnabled(true);
        addView(view);
        
        //child 1
        int resID = R.layout.facebook_mail_item;
        View v  = factory.inflate(resID, null);
        
        v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));
        view.addView(v);
   
        
        imageView  = (ImageView)v.findViewById(R.id.mail_img_ui);
        publishDate  = (TextView)v.findViewById(R.id.mail_publish_time);
        publishSubject   = (TextView)v.findViewById(R.id.mail_publish_subject);       
        username     = (TextView)v.findViewById(R.id.mail_user_name);
        readFlagView = (ImageView)v.findViewById(R.id.mail_img_read_ornot);
        publishTxt   = (TextView)v.findViewById(R.id.mail_publish_text);
        body_region = v.findViewById(R.id.body_region);
        username.setSingleLine(true);
        setUI();        
    }

    public void chooseMessageListener()
    {
        //setOnClickListener(msgOnClik);
    }
    
    View.OnClickListener msgOnClik = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
             Log.d(TAG, "msgOnClik you click first one="); 
             if(mthread != null)
             {
                                 
                 Intent intent = new Intent(mContext, FacebookMailDetailActivity.class);                
                 //set mail conversation detail information
                 MessageThreadInfoParcel mail = new MessageThreadInfoParcel(mthread);
                 
                 intent.putExtra("MessageThreadInfo", mail);                 
                 intent.putExtra("tid",      mthread.thread_id);
                 intent.putExtra("ouid",     getFromUID());
                 intent.putExtra("username", getUserName());
                 intent.putExtra("imageurl", imageURL); 
                 
                 ((FacebookBaseActivity)mContext).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_MAIL_DETAIL);
             }
             
        }
    };
    

    @Override
    protected void onFinishInflate() 
    {   
        super.onFinishInflate();        
        init();
    }
    
    private void setUI()
    {
        //set no text and image firstly
        username.setText("");
        imageView.setImageResource(R.drawable.no_avatar);
        if(isUpdate())
        {
            setImagePage();
        }
        else
        {
            setImageUser();  
        }
        
        //username.setText(getViewUserName());         
        //set autor image
        //setImage();
        
        publishDate.setText(this.getDate());
        publishSubject.setText(this.getSubject());
        
        if(mthread.unread == 0 && isEmpty(this.getText()))
        {
            body_region.setVisibility(View.GONE);                            
        	this.setBackgroundDrawable(null);            
        }
        else
        {
            body_region.setVisibility(View.VISIBLE);
            publishTxt.setText(this.getText());
            
            if(mthread!= null && mthread.unread > 0 && mType!=SENT)
            {
            	setBackgroundDrawable(getResources().getDrawable(R.color.inboxunread));            	
                //readFlagView.setVisibility(View.VISIBLE);
                //this.readFlagView.setImageResource(R.drawable.cmcc_list_mail_unread);
            }
            else
            {
            	this.setBackgroundDrawable(null);
                readFlagView.setVisibility(View.GONE);                
            }            
        }        
    }
  
    private void setImagePage() {
        long id = mthread.object_id;
        page = orm.getPageBypid(id);
        boolean getFromDB=false;
        if(page != null)
        {
            Log.d(TAG, "who am I="+page);
            imageURL = page.pic_square;
            getImageBMP(imageURL, false);
            username.setText(page.name);
        }               
        else
        {   
            if(FacebookBaseActivity.class.isInstance(mContext))
            {
                AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
                if(af != null)
                {
                    af.getPageInfoAsync(id,new FacebookAdapter()
                    {
                        @Override public void getPageInfo(Page tmpPage)
                        {
                            if(tmpPage != null )
                            {
                                page = tmpPage;
                                Log.d(TAG, "after get user info="+user);                               
                                imageURL = page.pic_square;                                 
                                getImageBMP(imageURL, true);                               
                                //update database
                                orm.insertPage(tmpPage);                           
                                updateUIFromPage();
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
        
    }
    protected void updateUIFromPage() {
        handler.obtainMessage(UPDATE_PAGE_UI).sendToTarget();   
    }
    private void setImageUser() {
        long id = getFromUID();
        user = orm.getFacebookUser(id);
        boolean getFromDB=false;
        if(user != null)
        {
            Log.d(TAG, "who am I="+user);
            imageURL = user.pic_square;
            getImageBMP(imageURL, false);
            username.setText(user.name);
        }               
        else
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
    }
    public void setMessageItem(MessageThreadInfo di) 
    {
        mthread.despose();
        mthread = null;
        
        mthread = di;
        //need reget the image
        imageURL = null;        
        setUI();
    }
    public void setMessageItem(MessageThreadInfo di,int mType) 
    {
        mthread.despose();
        mthread = null;
        mthread = di;
        //need reget the image
        imageURL = null;  
        this.mType = mType;
        setUI();
    }
    
    public MessageThreadInfo getThread() 
    {
        if(mthread != null)
        {
            return mthread;
        }
        return null;
    }   
}
