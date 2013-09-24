package com.msocial.free.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.msocial.free.*;
import com.msocial.free.providers.SocialORM;
import com.msocial.free.service.SNSService;
import com.msocial.free.ui.FacebookPhonebookActivity.SyncContactTask;
import com.msocial.free.ui.view.ImageCacheManager;
import com.msocial.free.ui.view.ImageRun;
import com.msocial.free.ui.view.SNSItemView;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.PhoneBook;
import oms.sns.service.facebook.util.StringUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookPhonebookDetailActivity extends FacebookBaseActivity
{
    private final String TAG      = "FacebookPhonebookDetailActivity";    	
	private ImageView    userLogo;
	private TextView     userName;
	
	private PhoneBook    phonebook;
	private FacebookUser user;

	private TextView phonebook_other;
	private ListView phonebook_info_list;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_phonebook_detail);
        setTitle();
        setTitle(title);
        long uid = this.getIntent().getLongExtra("phonebookid", -1);
        phonebook = orm.getPhonebook(uid);
        user      = orm.getFacebookUser(uid);
        userLogo = (ImageView)this.findViewById(R.id.phonebook_img_logo_ui);
        phonebook_info_list = (ListView)this.findViewById(R.id.phonebook_info_list);
        phonebook_info_list.setOnItemClickListener(itemClickListener);
        userName = (TextView)this.findViewById(R.id.phonebook_user_name);
        
        if(user != null)
        {   
            if(isEmpty(user.name) == false) userName.setText(user.name);
            userName.setOnClickListener(toUserDetailClick);
            userLogo.setOnClickListener(toUserDetailClick);
            String tmpurl = getImgURL(user);
            //first set small icon and then set the right icon 
            String smallFile = TwitterHelper.getImagePathFromURL_noFetch(user.pic_square);
            int ret = -1;
            if(new File(smallFile).exists() == true)
            {
                try{
                    Bitmap tmp = BitmapFactory.decodeFile(smallFile);
                    if(tmp != null)
                    {
                        //ImageCacheManager.instance().addCache(user.pic_square, tmp);
                        //userLogo.setImageBitmap(tmp);
                        
                        Matrix matrix = new Matrix();
                        int h = 120;
                        Bitmap mBaseImage = tmp;                 
                        float scale = (float)h/(float)mBaseImage.getWidth();
                        matrix.setScale(scale, scale);
                        mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage.getWidth(), mBaseImage.getHeight(), matrix, true);           
                        userLogo.setImageBitmap(mBaseImage);      
                    }
                    else
                    {
                        ret = R.drawable.no_avatar;
                    }
                }
                catch(Exception ne)
                {
                    ret = R.drawable.no_avatar;
                }
            }
            else
            {
                ret = R.drawable.no_avatar;
            }
            
            if(ret > -1)
            {
                Matrix matrix = new Matrix();
                int h = 120;
                Bitmap mBaseImage = BitmapFactory.decodeResource(SNSService.getSNSService().getResources(), ret);                 
                float scale = (float)h/(float)mBaseImage.getWidth();
                matrix.setScale(scale, scale);
                mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage.getWidth(), mBaseImage.getHeight(), matrix, true);           
                userLogo.setImageBitmap(mBaseImage);                
            }
            
            if(tmpurl != null)
            {
               setImage(tmpurl);
            }  
        }
        else
        {
            Log.e(TAG, "***************Why user is null");
            userLogo.setImageResource(R.drawable.no_avatar);
        }
        //cell/mobile/email/address
        List<PhoneBookDetailItem> items = createPhonebookDetailItems();
        
        if(items.size()>0)
        {
        	FacebookPhoneBookDetailAdapter adapter = new FacebookPhoneBookDetailAdapter(mContext,items);
            phonebook_info_list.setAdapter(adapter);
        }
        else
        {
        	phonebook_info_list.setAdapter(null);
        }
          
        setTitle("");
        SocialORM.Account account = orm.getFacebookAccount();
        if(checkFacebookAccount(this, account))
        {
        	perm_session = loginHelper.getPermanentSesstion(this);
        	if(perm_session != null)
        	{
	        	perm_session.attachActivity(this);
	        	facebookA = new AsyncFacebook(perm_session);	        	
        	}
        	else
        	{
        		launchFacebookLogin();
        	}
        }
    }
	
	View.OnClickListener toUserDetailClick = new View.OnClickListener()
	{
        public void onClick(View v) 
        {
            Log.d(TAG, "entering userLogo Click...");
            Intent intent = new Intent(mContext, FacebookAccountActivity.class);
            intent.putExtra("uid",      user.uid);
            intent.putExtra("username", user.name);
            intent.putExtra("imageurl", user.pic_square);                   
            ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
        }
	};
	
	List<PhoneBookDetailItem> createPhonebookDetailItems()
	{
		List<PhoneBookDetailItem> items = new ArrayList<PhoneBookDetailItem>();
		if(!StringUtils.isEmpty(phonebook.email))
		{
			items.add(new PhoneBookDetailItem(PhoneBookDetailItem.EMAIL_TYPE,phonebook.email));
		}
		if(!StringUtils.isEmpty(phonebook.cell))
		{
			items.add(new PhoneBookDetailItem(PhoneBookDetailItem.CELL_TYPE,phonebook.cell));
		}
		if(!StringUtils.isEmpty(phonebook.phone))
		{
			items.add(new PhoneBookDetailItem(PhoneBookDetailItem.PHONE_TYPE,phonebook.phone));
		}
		
		String address = formatOtherInfo();
		if(!StringUtils.isEmpty(address))
		{
			items.add(new PhoneBookDetailItem(PhoneBookDetailItem.ADDRESS_TYPE,address));
		}
		
		return items;
		
	}
	
	private String getImgURL(FacebookUser user)
    {
        if(user == null)
            return null;
        
        String url = null;
        int icon_size = orm.getFacebookIconSizeSetting(); //0 big 1 middle 2 small
        switch(icon_size)
        {
            case 0:
            {
                url = user.pic;
                if(url == null)
                {
                    url = user.pic_square;
                }
                break;
            }
            case 1:
            { 
                url = user.pic_small;
                if(url == null)
                {
                    url = user.pic_square;
                }
                break;
            }
            case 2:
            {
                url = user.pic_square;
                break;
            }
        }
        return url;
    }
	
	
	AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
        {
            if(FacebookPhonebookDetailItemView.class.isInstance(v))
            {
            	PhoneBookDetailItem item = ((FacebookPhonebookDetailItemView)v).getMItem();
            	if(item.type == PhoneBookDetailItem.EMAIL_TYPE)
            	{
            		 Log.d(TAG, "emailOnClik you click first one=");
        			 Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + item.content));
        		     intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		     startActivity(intent);
            	}
            	else if(item.type == PhoneBookDetailItem.CELL_TYPE || item.type == PhoneBookDetailItem.PHONE_TYPE)
            	{
            		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + item.content));
       	            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       	            startActivity(intent);
            	}
            }
        }
    };
	
    @Override
    protected void doAddPhonebookIntoContact() 
    {
    	  if(orm.isEnableSyncPhonebook() == false)
          {
              Message msg = handler.obtainMessage(SYNC_PHONEBOOK_ONE_ADDRESSBOOK);
              msg.getData().putLong("uid", phonebook.uid);            
              SyncAddressBookHelper.processSyncAddressBook(mContext, true, null, msg);
          }
          else
          {
              Log.d(TAG, "sync phonebook to contact="+phonebook);
              
              Message msg = handler.obtainMessage(SYNC_PHONEBOOK_ONE_ADDRESSBOOK);
              msg.getData().putLong("uid", phonebook.uid);
              SyncAddressBookHelper.checkIsEnableAddressbookSync(mContext, orm, msg);             
          }
    }
    
    /*
	@Override
    public void titleSelected() 
    {
        super.titleSelected();
        
        if(orm.isEnableSyncPhonebook() == false)
        {
            Message msg = handler.obtainMessage(SYNC_PHONEBOOK_ONE_ADDRESSBOOK);
            msg.getData().putLong("uid", phonebook.uid);            
            SyncAddressBookHelper.processSyncAddressBook(mContext, true, null, msg);
        }
        else
        {
            Log.d(TAG, "sync phonebook to contact="+phonebook);
            
            Message msg = handler.obtainMessage(SYNC_PHONEBOOK_ONE_ADDRESSBOOK);
            msg.getData().putLong("uid", phonebook.uid);
            SyncAddressBookHelper.checkIsEnableAddressbookSync(mContext, orm, msg);
            
            
//          Toast.makeText(FacebookPhonebookActivity.this, R.string.facebook_phonebook_sync_to_contact, Toast.LENGTH_SHORT).show();
//          new SyncContactTask(FacebookPhonebookActivity.this.getApplicationContext(), phonebook.clone(), orm).execute();
        }
    }*/
	
    final int SYNC_PHONEBOOK_ONE_ADDRESSBOOK =0;	
	private class PhonebookDetailHandler extends Handler 
	{
        public PhonebookDetailHandler()
        {
            super();            
            Log.d(TAG, "new PhonebookDetailHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case SYNC_PHONEBOOK_ONE_ADDRESSBOOK:
                {
                	if(msg.getData().getBoolean("RESULT", false) == true)
                	{
                		Toast.makeText(FacebookPhonebookDetailActivity.this, R.string.facebook_phonebook_sync_to_contact, Toast.LENGTH_SHORT).show();
                		
                		Log.d(TAG, "sync phonebook to contact="+phonebook);
                		new SyncContactTask(FacebookPhonebookDetailActivity.this.getApplicationContext(), phonebook.clone(), orm).execute();                		
                	}
                	else
                	{
                		Log.d(TAG, "user select cancel");
                	}
                	break;
                }
            }
        }
	}
	
	private String formatOtherInfo() 
	{
		StringBuilder sb = new StringBuilder();
		//if(isEmpty(phonebook.screenname) == false)
		//    sb.append("screenname: "+phonebook.screenname+"\n");
		
		if(isEmpty(phonebook.address) == false)
		{
		    sb.append(phonebook.address+" ");
		}
		
		if(isEmpty(phonebook.street) == false)
		    sb.append(phonebook.street+" ");	
		if(isEmpty(phonebook.state) == false)
		    sb.append(phonebook.state+" ");
		if(isEmpty(phonebook.city) == false)
		    sb.append(phonebook.city+" ");
		if(isEmpty(phonebook.country) == false)
		    sb.append( phonebook.country+" ");
		if(isEmpty(phonebook.zip) == false)
		    sb.append(phonebook.zip+" ");
		
//		if(isEmpty(phonebook.latitude) == false)
//		{
//			sb.append("\n");
//		    sb.append("latitude: "+phonebook.latitude+"\n");
//		}
//		if(isEmpty(phonebook.longitude) == false)
//		    sb.append("longitude: "+phonebook.longitude+"\n");		
		
		return sb.toString();
	}
	public void setTitle()
    {
    	title = getString(R.string.facebook_phonebook_title);
    }
	
	private void setImage(String pic_square) 
	{
		ImageRun imagerun = new ImageRun(handler, pic_square, 1);
		imagerun.use_avatar = true;
		imagerun.need_scale = true;
		imagerun.noimage    = true;
		imagerun.setImageView(userLogo);		
		imagerun.post(imagerun);
	}

	@Override
	protected void createHandler() 
	{	
		handler = new PhonebookDetailHandler();
	}
	
	class PhoneBookDetailItem 
	{
		int type;
		String content;
		String label;
		
		 PhoneBookDetailItem(int type,String content)
		{
			this.type = type;
			this.content = content;
			
			if(type == CELL_TYPE)
			{
				this.label = "Cell:";
			}
			else if(type == PHONE_TYPE)
			{
				this.label = "Phone:";
			}
			else if(type == EMAIL_TYPE)
			{
				this.label = "Email:";
			}
			else if(type == ADDRESS_TYPE)
			{
				this.label = "Adress:";
			}
		}
		
		final static int CELL_TYPE    = 0;
	    final static int EMAIL_TYPE   = 1;
		final static int ADDRESS_TYPE = 2;
		final static int PHONE_TYPE   = 3;
		
		
	}
	
	
    class FacebookPhoneBookDetailAdapter extends BaseAdapter
    {   
    	 private Context mContext;
    	 private List<PhoneBookDetailItem> mPhonebookdetailitems;
    	 
    	 public FacebookPhoneBookDetailAdapter(Context con,  List<PhoneBookDetailItem> phonebookdetailitems)
	     {
	    	mContext = con;
	    	mPhonebookdetailitems = phonebookdetailitems;    	
	    	Log.d(TAG, "create FacebookPhoneBookDetailItem");
	     }
    	
		public int getCount() {	
			return mPhonebookdetailitems.size();
		}

		public Object getItem(int position) {
			return mPhonebookdetailitems.get(position);
		}

		public long getItemId(int position) {
			return mPhonebookdetailitems.get(position).type;
		}

		public View getView(int position, View convertView, ViewGroup arg2) 
		{		
			 if (position < 0 || position >= getCount()) 
			 {
	             return null;    
	         }
	         
			 FacebookPhonebookDetailItemView v=null;
		
			 PhoneBookDetailItem di = (PhoneBookDetailItem)getItem(position);
	         if (convertView == null ) 
	         {
	             v = new FacebookPhonebookDetailItemView(mContext, di);
	         } 
	         else
	         {
	             v = (FacebookPhonebookDetailItemView) convertView;
	             v.setMItem(di);
	         }        
	         return v;
		}	
    	
    }
    
    class FacebookPhonebookDetailItemView extends SNSItemView
    {
        PhoneBookDetailItem mItem;
        Context mContext;
        TextView label;
        TextView data;
        
        public PhoneBookDetailItem getMItem() {
			return mItem;
		}

		public void setMItem(PhoneBookDetailItem item) {
			mItem = item;
			setUI();
		}
		
		public FacebookPhonebookDetailItemView(Context ctx, AttributeSet attrs) {
			super(ctx, attrs);
			init();
		}
		
		public FacebookPhonebookDetailItemView(Context context,PhoneBookDetailItem item)
		{ 
			super(context);
			mItem = item;
			mContext = context;		
			init();
		}

		public FacebookPhonebookDetailItemView(Context context) {
			super(context);
			mContext = context;
			init();
		}
		
		private void init()
		{
			LayoutInflater factory = LayoutInflater.from(mContext);
			removeAllViews();
			
			View v  = factory.inflate(R.layout.facebook_phonebook_detail_item, null);		
			v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));			
			addView(v);
			
			label = (TextView)this.findViewById(R.id.label);
			data = (TextView)this.findViewById(R.id.data);
			
			setUI();
		}
		
		private void setUI()
		{
			if(mItem!=null)
			{
				if(label!= null)
				   label.setText(mItem.label);
				if(data!=null)
					data.setText(mItem.content);
			}			
		}

		@Override
		public String getText() {
			
			return "";
		}
    	
    }
}
