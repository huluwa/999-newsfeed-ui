package com.android.omshome.quickaction;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;

import com.android.omshome.AllApps2DWithCategory;
import com.android.omshome.AllAppsScreenLayout;
import com.android.omshome.ApplicationInfo;
import com.android.omshome.Category;
import com.android.omshome.Launcher;
import com.android.omshome.LauncherORM;
import com.android.omshome.R;
import com.android.omshome.R.drawable;
import com.android.omshome.R.string;

import android.os.Handler;
import android.os.SystemProperties;

public class QuickLauncher {	
	private final static String TAG="Launcher.QuickLauncher";
	
	//???	 
    public  static QuickAction qa ;
    private Launcher mLauncher;
    public static boolean ChangedShortcut = true;
    
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
			
//		            
//	    	if(Category.shortcutapps.size() > 0)
//	    	{

	    		if(LauncherORM.instance(launcher).isAlwaysShowAddQuickLaunch())
	    		{
	    			if(mLauncher.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
	    			{
		    			final ActionItem setShortcutItem = new ActionItem();
						setShortcutItem.setTitle(mLauncher.getResources().getString(R.string.qa_set_shortcuts));    			
						setShortcutItem.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.cmcc_launcher_setting_quick_start));
						setShortcutItem.setOnClickListener(new View.OnClickListener() {			
							public void onClick(View v) {
								qa.dismiss();		
								mLauncher.showAllApps(true);
								((AllApps2DWithCategory)(mLauncher.mAllAppsGrid)).currentCategory = Category.CATEGORY_SHORTCUT;
								((AllApps2DWithCategory)(mLauncher.mAllAppsGrid)).isEditModel = true;
								((AllApps2DWithCategory)(mLauncher.mAllAppsGrid)).refreshAllAppsUI();
							}
						});
					  	
						qa.addActionItem(setShortcutItem);
	    			}
	    			else
	    			{
	    				//just do at portrait
	    			}
	    		}
	            
	    		
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
							
							new Handler().postDelayed(new Runnable()
							{
								public void run()
								{
									if(qa != null)
									    qa.dismiss();
								}
							}, 1000);												
						}
	    			});
	    		  	
	    		}   
	    		
//	    	}else{
//				final ActionItem setShortcutItem = new ActionItem();
//				setShortcutItem.setTitle(mLauncher.getResources().getString(R.string.qa_set_shortcuts));    			
//				setShortcutItem.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.cmcc_launcher_home_icon_shortcut));
//				setShortcutItem.setOnClickListener(new View.OnClickListener() {			
//					public void onClick(View v) {
//						qa.dismiss();		
//						mLauncher.showAllApps(true);
//						((AllApps2DWithCategory)(mLauncher.mAllAppsGrid)).currentCategory = Category.CATEGORY_SHORTCUT;
//						((AllApps2DWithCategory)(mLauncher.mAllAppsGrid)).isEditModel = true;
//						((AllApps2DWithCategory)(mLauncher.mAllAppsGrid)).refreshAllAppsUI();
//					}
//				});
//			  	
//				qa.addActionItem(setShortcutItem);
//	    	}	    	
	    	
	    	//move to bottom
//	    	final ActionItem email = new ActionItem();
//            email.setTitle(mLauncher.getResources().getString(R.string.contact_author));             
//            email.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.email));
//            email.setOnClickListener(new View.OnClickListener() {         
//                public void onClick(View v) {
//                    qa.dismiss();           
//                    //email
//                    //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:liuhuadong78@gmail.com"));
//                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    
//                    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
//                    emailIntent.setType("plain/text");
//                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"liuhuadong78@gmail.com", "zwbjtu@gmail.com", "hongkuan.zhang@gmail.com"});
//
//                    String mySubject = "Quick Launcher";
//                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mySubject);
//                    String myBodyText = "";
//                    
//                    String platform = SystemProperties.get("apps.setting.platformversion");
//                    myBodyText = "\n\n\n\n\n" + "---------------------\n" +platform;                    
//                    
//                    String version = SystemProperties.get("net.bt.name") + "  " + SystemProperties.get("ro.build.version.release");
//                    myBodyText += "\n" + version;
//                    
//                    String vendor = SystemProperties.get("apps.setting.product.vendor") + "  " + SystemProperties.get("apps.setting.btdevice");
//                    myBodyText += "\n" + vendor;
//                    
//                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, myBodyText);
//                    
//                    try {
//                        mLauncher.startActivity(emailIntent);
//                    }catch(Exception ne){}
//                }
//            });            
//            qa.addActionItem(email);
            
//            final ActionItem share = new ActionItem();
//            share.setTitle(mLauncher.getResources().getString(R.string.share_app));             
//            share.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.share));
//            share.setOnClickListener(new View.OnClickListener() {         
//                public void onClick(View v) {
//                    qa.dismiss();           
//                    Intent send = new Intent(Intent.ACTION_SEND);
//                    send.setType("text/plain");
//                    send.putExtra(Intent.EXTRA_TEXT,  "Thanks your sharing, search \"quicklauncher\" in market or open the bellow link in your phone, it will open Market application to search it.\n\n  http://market.android.com/search?q=com.tormas.home");                        
//
//                    try {
//                        mLauncher.startActivity(Intent.createChooser(send, mLauncher.getText(com.android.internal.R.string.sendText)));
//                    } catch(android.content.ActivityNotFoundException ex) {
//                        // if no app handles it, do nothing
//                    }                    
//                }
//            });            
//            qa.addActionItem(share);

            final ActionItem itemPageManager = new ActionItem();
            itemPageManager.setTitle(mLauncher.getResources().getString(R.string.qa_page_manager));             
            itemPageManager.setIcon(BitmapFactory.decodeResource(mLauncher.getResources(), R.drawable.cmcc_launcher_management_of_the_screen));
            itemPageManager.setOnClickListener(new View.OnClickListener() {         
                public void onClick(View v) {
                    qa.dismiss();           
                    mLauncher.startPageManager(v);
                }
            });
            
            qa.addActionItem(itemPageManager);
            
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
    	/*if(LauncherORM.instance(con).isAlwaysShowHint() == false)
    	{
    		return ;
    	}*/
    	
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
