package com.tormas.litesina.quickaction;

import oms.sns.TwitterStatus;
import oms.sns.TwitterUser;
import twitter4j.SimplyStatus;

import com.tormas.litesina.R;
import com.tormas.litesina.ui.StatusViewBaseActivity;
import com.tormas.litesina.ui.TwitterBaseActivity;
import com.tormas.litesina.ui.TwitterComposeActivity;
import com.tormas.litesina.ui.TwitterTweetsDetailActivity;
import com.tormas.litesina.ui.view.SimplyStatusItemView;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;


public class QuickLauncher {	
	private final static String TAG="Launcher.QuickLauncher";
	public  static QuickAction qa ;
    private StatusViewBaseActivity mLauncher;
    public static boolean ChangedShortcut = true;
    private Object mObject = new Object();
    
    public void popupQuickLauncher(final StatusViewBaseActivity launcher, final View mCategoryView, final SimplyStatus status)
    {
    	mLauncher = launcher;    	
    	
    	if(qa != null)
    	{
    		qa.dismiss();
    		qa = null;
    	}
    	
    	synchronized(mObject)
		{		  
	    	qa = new QuickAction(mCategoryView);
	    	qa.setOnDismissListener( new PopupWindow.OnDismissListener()
	    	{
				public void onDismiss() {
					synchronized(mObject)
					{	
					    qa = null;
					}
				}    		
	    	});
	    	
	    	 final TwitterStatus twitem = new TwitterStatus();
	         twitem.createdAt = status.getCreatedAt().getTime();
	         twitem.id        = status.getId();		        
	         twitem.isFavorited       = status.isFavorited();		        
	         twitem.text    = status.getText();
	         
	         twitem.user = new TwitterUser();
	         twitem.user.id = status.getUser().getId();		         
	         twitem.user.name           = status.getUser().getName();
	         twitem.user.profileImageUrl = status.getUser().getProfileImageURL().toString();
	         twitem.user.screenName      = status.getUser().getScreenName();	
	         twitem.user.notifications    = status.getUser().notifications;
	         twitem.user.following    = status.getUser().following;
	         
			 
			qa.setAnimStyle(QuickAction.ANIM_GROW_FROM_RIGHT);
			final ActionItem setShortcutItem = new ActionItem();
			setShortcutItem.setTitle(launcher.getString(R.string.twitter_to_detail));    			
			setShortcutItem.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.ic_swipe_profile_focused));
			setShortcutItem.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
					Intent intent = new Intent(launcher,TwitterTweetsDetailActivity.class);
	                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                intent.putExtra("currentstatus", twitem);
	                intent.putExtra("fromstatus", true);
	                launcher.startActivity(intent);
	                
	                dissmissQuickAction();
				}
			});		  	
			qa.addActionItem(setShortcutItem);
			
			
			final ActionItem rt = new ActionItem();
			rt.setTitle(launcher.getString(R.string.twitter_option_reply));    			
			rt.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.ic_swipe_reply_focused));
			rt.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
				    Intent intent = new Intent(launcher, TwitterComposeActivity.class);             
		            intent.putExtra(TwitterBaseActivity.STATUS_ID,     twitem.id);    
		            intent.putExtra(TwitterBaseActivity.TWITTER_ID,    String.valueOf(twitem.user.id)); 
		            intent.putExtra(TwitterBaseActivity.REPLY, true);
		            launcher.startActivity(intent);
		            
		            dissmissQuickAction();
				}
			});		  	
			qa.addActionItem(rt);
			
			final ActionItem fav = new ActionItem();
			fav.setTitle(launcher.getString(R.string.twitter_option_add_favorite));    			
			fav.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.ic_swipe_fav_on_focused));
			fav.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
					if(twitem.isFavorited == false)
					    mLauncher.addFav(twitem.id, String.valueOf(twitem.user.id));
					else
						mLauncher.removeFav(twitem.id, String.valueOf(twitem.user.id));
					
					dissmissQuickAction();
				}
			});		  	
			
			qa.addActionItem(fav);
			
			final ActionItem rtt = new ActionItem();
			rtt.setTitle(launcher.getString(R.string.twitter_option_retweet));    			
			rtt.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.ic_swipe_rt_focused));
			rtt.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
					mLauncher.retweet(twitem.id);
					
					dissmissQuickAction();
				}
			});		  	
			qa.addActionItem(rtt);
			
			final ActionItem dm = new ActionItem();
			dm.setTitle(launcher.getString(R.string.twitter_option_send_dm));    			
			dm.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.ic_toolbar_mention_focused));
			dm.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
		            Intent intent = new Intent(launcher, TwitterComposeActivity.class);             
		            intent.putExtra(TwitterBaseActivity.STATUS_ID,     twitem.id);    
		            intent.putExtra(TwitterBaseActivity.TWITTER_ID,    String.valueOf(twitem.user.id)); 
		            intent.putExtra(TwitterBaseActivity.DIRECT, true);
		            launcher.startActivity(intent);
		            
		            dissmissQuickAction();
				}
			});		 
			
			qa.addActionItem(dm);
			
			final ActionItem share = new ActionItem();
			share.setTitle(launcher.getString(R.string.facebook_wall_share));    			
			share.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.ic_swipe_share_focused));
			share.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
					 Intent send = new Intent(Intent.ACTION_SEND);
				        send.setType("text/plain");
				        send.putExtra(Intent.EXTRA_SUBJECT,launcher.getString(R.string.facebook_wall_share));  
				        if(SimplyStatusItemView.class.isInstance(mCategoryView))
				        {
				        	send.putExtra(Intent.EXTRA_TEXT,  ((SimplyStatusItemView)(mCategoryView)).getText());
				        }
				        else
				        {
				        	send.putExtra(Intent.EXTRA_TEXT,  "http://market.android.com/search?q=com.tormas.litesina or search \"Liteweibo for sina\" \nor directly download from http://cloud.borqs.com/search?q=com.tormas.litesina");
				        }
				       
				        try {
				        	launcher.startActivity(Intent.createChooser(send, launcher.getString(R.string.facebook_wall_share)));//name
				        } catch(android.content.ActivityNotFoundException ex) {
				            // if no app handles it, do nothing
				        }
		            
		            dissmissQuickAction();
				}
			});		 
			
			
			qa.addActionItem(share);
	    	qa.show();
		}
    }
  
    public static void dissmissQuickAction()
    {
    	if(qa != null)
    	{
    		try{
    		    qa.dismiss();
    		}catch(Exception ne){}
    		qa = null;
    	}
    }
    
    
}
