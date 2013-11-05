package com.msocial.freefb.ui.view;

import java.util.ArrayList;
import java.util.List;

import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.ui.FacebookAccountActivity;
import com.msocial.freefb.ui.FacebookBaseActivity;
import com.msocial.freefb.ui.FacebookPokeActivity;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.PhoneBook;
import oms.sns.service.facebook.util.StringUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import com.msocial.freefb.R;

public class FacebookPokeItemView extends SNSItemView{
    private final String TAG="FacebookPokeItemView";
    
    private ImageView imgView;
    private TextView  userName;
    //private Button    pokeBack;
    private ImageView pokeBack;
    private SocialORM orm;
    private FacebookUser.SimpleFBUser  user;
    public long getUID()
    {
        if(user != null)
        {
            return user.uid;
        }
        return -1;
    }
    Handler handler;
    
    boolean forcursor = false;
    public void setForCusor(boolean cursor)
    {
        forcursor = cursor;
    }
    
    public FacebookUser.SimpleFBUser getUser()
    {
        return user;
    }
    
    public FacebookPokeItemView(Context ctx, AttributeSet attrs) 
    {
        super(ctx, attrs);      
        mContext = ctx;
         
        setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
        handler = new Handler();
        orm = SocialORM.instance(ctx);
    }

    
    public FacebookPokeItemView(Context context, FacebookUser.SimpleFBUser di) 
    {       
        super(context);
        mContext = context;
        user = di;
        
        Log.d(TAG, "call  FacebookPokeItemView");
        handler = new Handler();
        orm = SocialORM.instance(context); 
        init();
    }
        
    private void init() 
    {
        Log.d(TAG,  "call FacebookPokeItemView init");
        LayoutInflater factory = LayoutInflater.from(mContext.getApplicationContext());
        removeAllViews();
        
        //container
        FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);        
        FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);     
        view.setLayoutParams(paras);
        view.setVerticalScrollBarEnabled(true);
        addView(view);
        
        //child 1
        View v = factory.inflate(R.layout.facebook_new_friend_item, null);     
        v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,    LayoutParams.WRAP_CONTENT));
        view.addView(v);
        
        int width = 0;
        int otherwidth = 10+60+60;
        int leftwidth = 0;
        width = ((FacebookPokeActivity)mContext).getWindowManager().getDefaultDisplay().getWidth();
        leftwidth = width - otherwidth;
        
        imgView  = (ImageView)v.findViewById(R.id.facebook_friends_img_ui);
        userName = (TextView)v.findViewById(R.id.facebook_friend_user_name);    
        if(leftwidth > 0)
        {
            userName.setWidth(leftwidth);
            userName.setMaxWidth(leftwidth);
        }
        pokeBack = (ImageView)v.findViewById(R.id.facebook_friend_poke_back);
        pokeBack.setVisibility(View.VISIBLE);       
        pokeBack.setOnClickListener(pokeBackClick);
        setUI();          
    }   
    
    private void setUI()
    {
        if(isEmpty(user.pic_square) == false)
        {
            ImageRun imagerun = new ImageRun(handler, user.pic_square, 0);
            imagerun.use_avatar = true;
            imagerun.setImageView(imgView);
            imagerun.post(imagerun);
        }
        else
        {
            imgView.setImageResource(R.drawable.no_avatar);
        }
        
        userName.setText(user.name);
        
    }
    
    @Override
    protected void onFinishInflate() 
    {   
        super.onFinishInflate();        
        init();
    }
    
    
    public void chooseFriendListener()
    {
        setOnClickListener(friendOnClik);
    }
    
    View.OnClickListener friendOnClik = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
             Log.d(TAG, "friendOnClik you click first one=");            
             //view details
            Intent intent = new Intent(mContext, FacebookAccountActivity.class);
            intent.putExtra("uid", user.uid);
            intent.putExtra("username", user.name);
            intent.putExtra("imageurl", user.pic_square);               
            ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
        }
    };
    View.OnClickListener pokeBackClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
             Log.d(TAG, "pokeback click");            
             //view details
             if(FacebookPokeActivity.class.isInstance(mContext))
             {
                 String uname = StringUtils.isEmpty(user.name)?String.valueOf(user.uid):user.name;
                 new AlertDialog.Builder(mContext)
                 .setTitle(String.format(mContext.getString(R.string.facebook_poke_dialog_title),uname))
                 .setMessage(String.format(mContext.getString(R.string.facebook_poke_dialog_msg),uname))
                 .setPositiveButton(R.string.menu_title_poke, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         FacebookPokeActivity fp = (FacebookPokeActivity)mContext;
                         fp.doPoke(user.uid,user.name);
                     }
                 })
                 .setNegativeButton(R.string.hint_album_cancel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                     }
                 })
                 .create().show();
             }
        }
    };
    
    public void setUserItem(FacebookUser.SimpleFBUser di) 
    {
        if(forcursor)
        {
            user.despose();
            user = null;
        }
        user = di;
        setUI();
    }
    @Override
    public String getText() 
    {       
        return "";
    }
}
