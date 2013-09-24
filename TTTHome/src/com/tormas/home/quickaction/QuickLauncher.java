package com.tormas.home.quickaction;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;

import com.tormas.home.AllAppsScreenLayout;
import com.tormas.home.ApplicationInfo;
import com.tormas.home.Category;
import com.tormas.home.Launcher;
import com.tormas.home.R;
import com.tormas.home.R.drawable;
import com.tormas.home.R.string;
import android.os.SystemProperties;

public class QuickLauncher {	
	private final static String TAG="Launcher.QuickLauncher";
	
	//???	 
    public  static QuickAction qa ;
    private Launcher mLauncher;
    
    public void popupQuickLauncher(Launcher launcher, View mCategoryView)
    {
    	mLauncher = launcher;
    	//BUG
    	//TODO memory issue
    	
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
			
			final ActionItem itemPageManager = new ActionItem();
	    	itemPageManager.setTitle(mLauncher.getResources().getString(R.string.qa_page_manager));    			
	    	itemPageManager.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.cmcc_mainmenu_ophone));
			itemPageManager.setOnClickListener(new View.OnClickListener() {			
				public void onClick(View v) {
					qa.dismiss();			
					mLauncher.startPageManager(v);
				}
			});
			
			qa.addActionItem(itemPageManager);
			
			final ActionItem email = new ActionItem();
			email.setTitle(mLauncher.getResources().getString(R.string.contact_author));             
			email.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.email));
			email.setOnClickListener(new View.OnClickListener() {         
                public void onClick(View v) {
                    qa.dismiss();           
                    //email
                    //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:liuhuadong78@gmail.com"));
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    
                    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"liuhuadong78@gmail.com", "zwbjtu@gmail.com"});

                    String mySubject = "Quick Launcher";
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mySubject);
                    String myBodyText = "";
                    
                    String platform = SystemProperties.get("apps.setting.platformversion");
                    myBodyText = "\n\n\n\n\n" + "---------------------\n" +platform;                    
                    
                    String version = SystemProperties.get("net.bt.name") + "  " + SystemProperties.get("ro.build.version.release");
                    myBodyText += "\n" + version;
                    
                    String vendor = SystemProperties.get("apps.setting.product.vendor") + "  " + SystemProperties.get("apps.setting.btdevice");
                    myBodyText += "\n" + vendor;
                    
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, myBodyText);
                    
                    try {
                        mLauncher.startActivity(emailIntent);
                    }catch(Exception ne){}
                }
            });            
            qa.addActionItem(email);
            
            final ActionItem share = new ActionItem();
            share.setTitle(mLauncher.getResources().getString(R.string.share_app));             
            share.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.share));
            share.setOnClickListener(new View.OnClickListener() {         
                public void onClick(View v) {
                    qa.dismiss();           
                    Intent send = new Intent(Intent.ACTION_SEND);
                    send.setType("text/plain");
                    send.putExtra(Intent.EXTRA_TEXT,  "Thanks your sharing, search \"quicklauncher\" in market or open the bellow link in your phone, it will open Market application to search it.\n\n  http://market.android.com/search?q=com.tormas.home");                        

                    try {
                        mLauncher.startActivity(Intent.createChooser(send, mLauncher.getText(com.android.internal.R.string.sendText)));
                    } catch(android.content.ActivityNotFoundException ex) {
                        // if no app handles it, do nothing
                    }                    
                }
            });            
            qa.addActionItem(share);
            
            			
	    	if(Category.shortcutapps.size() > 0)
	    	{
	    		for(int i=0;i<Category.shortcutapps.size();i++)
	    		{
	    			final ApplicationInfo ai = Category.shortcutapps.get(i);
	    			final ActionItem item = new ActionItem();
	    			item.setTitle(ai.title.toString());    			
	    			item.setIcon(ai.iconBitmap);
	    			qa.addActionItem(item);
	    			item.setOnClickListener(new View.OnClickListener() {			
						public void onClick(View v) {						
							mLauncher.startActivitySafely(ai.intent, null);						
							qa.dismiss();							
						}
	    			});
	    		  	
	    		}    		
	    	}else{
				final ActionItem setShortcutItem = new ActionItem();
				setShortcutItem.setTitle(mLauncher.getResources().getString(R.string.qa_set_shortcuts));    			
				setShortcutItem.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.ic_allapp_add_shortcut));
				setShortcutItem.setOnClickListener(new View.OnClickListener() {			
					public void onClick(View v) {
						qa.dismiss();		
						mLauncher.showAllApps(true);
						((AllAppsScreenLayout)(mLauncher.mAllAppsGrid)).refreshAllAppsUI(Category.CATEGORY_SHORTCUT, true);
					}
				});
			  	
				qa.addActionItem(setShortcutItem);
	    	}	    	
	    	qa.show();
		}
    }
  
    public static void dissmissQuickAction()
    {
    	if(hintqa != null)
    	{
    		try{
    		    hintqa.dismiss();
    		}catch(Exception ne){}
    		hintqa = null;
    	}
    	
    	if(qa != null)
    	{
    		try{
    		    qa.dismiss();
    		}catch(Exception ne){}
    		qa = null;
    	}
    }
    
    public static QuickAction hintqa ;
    private static boolean isDismissed=true;
    private static Object mObject = new Object();
    public static void popupHint(final Context con, final View mHintView)
    {
    	synchronized(mObject)
		{
		    if(hintqa == null)
	    	{
		    	hintqa = new QuickAction(mHintView);
		    	hintqa.setAnimStyle(QuickAction.ANIM_GROW_FROM_RIGHT);
		    	hintqa.setOnDismissListener( new PopupWindow.OnDismissListener()
		    	{
					public void onDismiss() {
						synchronized(mObject)
						{
						    isDismissed = true;
						    hintqa = null;
						}
					}    		
		    	});
		    	
		    	final ActionItem item = new ActionItem();
		    	item.setTitle(con.getString(R.string.click_switch_category_hint));
		    	item.setOnClickListener(new View.OnClickListener() {			
					public void onClick(View v) {	
						//popup dialoag to select						
						mHintView.performClick();
                                                synchronized(mObject)
                                                {
                                                    if(hintqa != null)
                                                    {
						        hintqa.dismiss();
                                                    }
                                                }
					}
				});
		    	hintqa.addActionItem(item);
	    	}
	    	
	    	if(isDismissed == true && hintqa!= null)
			{
	    	    hintqa.show();
			    isDismissed = false;
			}		
		}
    }
    
	public static void dissmissHint() {		
		synchronized(mObject)
		{
			if(isDismissed == false && hintqa != null)
			{
			    hintqa.dismiss();
			}			
		}
	}
}
