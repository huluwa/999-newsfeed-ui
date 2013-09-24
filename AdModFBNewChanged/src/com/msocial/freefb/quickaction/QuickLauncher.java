package com.msocial.freefb.quickaction;

import oms.sns.service.facebook.model.FacebookUser;

import com.msocial.freefb.ui.DashBoardActivity;
import com.msocial.freefb.ui.FacebookAccountActivity;
import com.msocial.freefb.ui.FacebookBaseActivity;
import com.msocial.freefb.ui.FacebookProfileActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;
import com.msocial.freefb.R;


public class QuickLauncher {	
	private final static String TAG="Launcher.QuickLauncher";
	public  static QuickAction qa ;
    private FacebookBaseActivity mLauncher;
    public static boolean ChangedShortcut = true;
    private Object mObject = new Object();
    
    public void popupQuickLauncher(final FacebookBaseActivity launcher, final View mCategoryView, final FacebookUser.SimpleFBUser user)
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
	         
			 
			qa.setAnimStyle(QuickAction.ANIM_GROW_FROM_RIGHT);
			final ActionItem setShortcutItem = new ActionItem();
			setShortcutItem.setTitle(launcher.getString(R.string.facebook_to_detail));    			
			setShortcutItem.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.ic_swipe_profile_default));
			setShortcutItem.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
					Intent intent = new Intent(mLauncher, FacebookAccountActivity.class);
					intent.putExtra("uid",      user.uid);
					intent.putExtra("username", user.name);
					intent.putExtra("imageurl", user.pic_square);					
					(mLauncher).startActivity(intent);
					
	                dissmissQuickAction();
				}
			});		  	
			qa.addActionItem(setShortcutItem);
			
			
			final ActionItem rt = new ActionItem();
			rt.setTitle(launcher.getString(R.string.quickaction_mail));    			
			rt.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.cmcc_send_mail));
			rt.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
				    if(FacebookBaseActivity.class.isInstance(mLauncher))
				    {
				    	((FacebookBaseActivity)mLauncher).newMail(user.uid);
				    }
		            dissmissQuickAction();
				}
			});		  	
			qa.addActionItem(rt);
			
			final ActionItem rtt = new ActionItem();
			rtt.setTitle(launcher.getString(R.string.quickaction_wall));    			
			rtt.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.cmcc_icon_edit));
			rtt.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {				
					
				    if(FacebookBaseActivity.class.isInstance(mLauncher))
				    {
				    	((FacebookBaseActivity)mLauncher).postToWall(user.uid);
				    }
					dissmissQuickAction();
				}
			});		  	
			qa.addActionItem(rtt);
			
			if(DashBoardActivity.class.isInstance(launcher) || FacebookProfileActivity.class.isInstance(launcher))
		    {	
                final ActionItem remove = new ActionItem();
                remove.setTitle(launcher.getString(R.string.quickaction_remove));
                remove.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.cmcc_icon_delete_record));
                remove.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            if(FacebookBaseActivity.class.isInstance(mLauncher))
                            {
                                ((FacebookBaseActivity)mLauncher).removeShortCut(user);
                            }
                            dissmissQuickAction();
                        }
                });

                qa.addActionItem(remove);
                
                final ActionItem removeall = new ActionItem();
                removeall.setTitle(launcher.getString(R.string.quickaction_remove_all));
                removeall.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.cmcc_all_closed));
                removeall.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            if(FacebookBaseActivity.class.isInstance(mLauncher))
                            {
                                ((FacebookBaseActivity)mLauncher).removeAllShortcut();
                            }
                            dissmissQuickAction();
                        }
                });

                qa.addActionItem(removeall);
			}
			else
			{
				final ActionItem fav = new ActionItem();
				fav.setTitle(launcher.getString(R.string.quickaction_add_shotcut));    			
				fav.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.cmcc_new_contact));
				fav.setOnClickListener(new View.OnClickListener() {			
					public void onClick(View v) {
						
						if(FacebookBaseActivity.class.isInstance(mLauncher))
					    {
					    	((FacebookBaseActivity)mLauncher).addshortCut(user);
					    }
						dissmissQuickAction();
					}
				});		  	
				
				qa.addActionItem(fav);
			}
			
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
