package com.tormas.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.os.SystemProperties;

public class Category {
	private final static String TAG = "Launcher.Category";
	public final static int CATEGORY_ALLAPP = 0;
	public final static int CATEGORY_SYSTEMAPP = 1;
	public final static int CATEGORY_CARRIERAPP = 2;
	public final static int CATEGORY_OEMAPP = 3;
	public final static int CATEGORY_3RDAPP = 4;
	public final static int CATEGORY_RECENTUSE = 5;
	public final static int CATEGORY_CUSTOMIZE = 100;
	public final static int CATEGORY_Entertainment  = CATEGORY_CUSTOMIZE+LauncherProvider.ID_Entertainment;
	public final static int CATEGORY_Information    = CATEGORY_CUSTOMIZE+LauncherProvider.ID_Information;
	public final static int CATEGORY_Tools          = CATEGORY_CUSTOMIZE+LauncherProvider.ID_Tools;
	public final static int CATEGORY_SHORTCUT       = CATEGORY_CUSTOMIZE+LauncherProvider.ID_SHORT;
	
	public final static int COUNT_RECENTUSE = 10;
	
	private static boolean isDisplayOEMAPPS = false;
	private static boolean isDisplayCarrierAPPS = false;
	
	public static ArrayList<ApplicationInfo> allapps = new ArrayList<ApplicationInfo>();
	public static ArrayList<ApplicationInfo> systemapps = new ArrayList<ApplicationInfo>();
	public static ArrayList<ApplicationInfo> carrierapps = new ArrayList<ApplicationInfo>();
	public static ArrayList<ApplicationInfo> oemapps = new ArrayList<ApplicationInfo>();
	public static ArrayList<ApplicationInfo> the3rdapps = new ArrayList<ApplicationInfo>();
	public static ArrayList<ApplicationInfo> recentuseapps = new ArrayList<ApplicationInfo>();
	
	
	public static ArrayList<ApplicationInfo> entertainmentapps = new ArrayList<ApplicationInfo>();
	public static ArrayList<ApplicationInfo> informationapps   = new ArrayList<ApplicationInfo>();
	public static ArrayList<ApplicationInfo> toolsapps         = new ArrayList<ApplicationInfo>();
	public static ArrayList<ApplicationInfo> shortcutapps      = new ArrayList<ApplicationInfo>();
	
	public static HashMap<Integer, ArrayList<ApplicationInfo>>customizeApps = new HashMap<Integer, ArrayList<ApplicationInfo>>();
	
	public static String nameAll = null;
	public static String nameSystem = null;
	public static String nameCarrier = null;
	public static String nameOem = null;
	public static String nameThe3rd = null;
	public static String nameRecentUse = null;
	
	public static String nameEntertainment = null;
	public static String nameInformation   = null;
	public static String nameTools         = null;
	public static String nameShortcut      = null;
	
	
	public static Drawable drawableAll = null;
	public static Drawable drawableSystem = null;
	public static Drawable drawableCarrier = null;
	public static Drawable drawableOem = null;
	public static Drawable drawableThe3rd = null;
	public static Drawable drawableRecentUse = null;
	public static Drawable drawableShortcut = null;
	public static Drawable drawableEntertainment= null;
	public static Drawable drawableInformation = null;
	public static Drawable drawableTools = null;
	
	public static void initialRes(Context con){
		Log.d(TAG, "initalRes");
		final Resources res = con.getResources();
		
		nameAll = res.getString(R.string.category_all);
		nameSystem = res.getString(R.string.category_system);
		nameCarrier = res.getString(R.string.category_carrier);
		nameOem = res.getString(R.string.category_oem);
		nameThe3rd = res.getString(R.string.category_3rd);
		nameRecentUse = res.getString(R.string.category_recentuse);
		
		nameEntertainment = res.getString(R.string.app_entertainment);
		nameInformation   = res.getString(R.string.app_information);
		nameTools         = res.getString(R.string.app_tools);		
		nameShortcut      = res.getString(R.string.category_shortcut);
		 
		drawableAll = res.getDrawable(R.drawable.ic_category_all);
		drawableSystem = res.getDrawable(R.drawable.chart);
		drawableCarrier = res.getDrawable(R.drawable.production);
		drawableOem = res.getDrawable(R.drawable.chart);
		drawableThe3rd = res.getDrawable(R.drawable.ic_category_the3rdapp);
		drawableRecentUse = res.getDrawable(R.drawable.chart);
		drawableShortcut  = res.getDrawable(R.drawable.ic_category_shortcut);
		drawableEntertainment  = res.getDrawable(R.drawable.ic_category_entertainment);
		drawableInformation  = res.getDrawable(R.drawable.ic_category_information);
		drawableTools  = res.getDrawable(R.drawable.ic_category_tools);
		
//		drawableAll = res.getDrawable(R.drawable.ic_category_all);
//		drawableSystem = res.getDrawable(R.drawable.ic_category_system);
//		drawableCarrier = res.getDrawable(R.drawable.ic_category_carrier);
//		drawableOem = res.getDrawable(R.drawable.ic_category_oem);
//		drawableThe3rd = res.getDrawable(R.drawable.ic_category_3rd);
//		drawableRecentUse = res.getDrawable(R.drawable.ic_category_recentuse);
		
		isDisplayOEMAPPS =  SystemProperties.getInt("omshome.allapps.use.oem", 0) == 1; 
		isDisplayCarrierAPPS =  SystemProperties.getInt("omshome.allapps.use.carrier", 0) == 1; 
		
		customizeApps.put(LauncherProvider.ID_Entertainment, entertainmentapps);
		customizeApps.put(LauncherProvider.ID_Information,   informationapps);
		customizeApps.put(LauncherProvider.ID_Tools,         toolsapps);
		customizeApps.put(LauncherProvider.ID_SHORT,         shortcutapps);
	}
	
	public static void initApplicationData(Context context, ArrayList<ApplicationInfo> apps){
		allapps.clear();
		systemapps.clear();
		carrierapps.clear();
		oemapps.clear();
		the3rdapps.clear();
		
		Set<Integer> sets = customizeApps.keySet();
		Iterator<Integer> it = sets.iterator();
		while(it.hasNext())
		{
			int cid = it.next();
			ArrayList<ApplicationInfo> items = customizeApps.get(cid);
			for(int step=0;step<items.size();step++)
			{
				items.get(step).unbind();
			}
			items.clear();				
		}
		
		
		
		final int N = apps.size();
		for(int i=0; i<N; i++){
			ApplicationInfo app = apps.get(i);
			switch(app.category){
			case Category.CATEGORY_SYSTEMAPP:
				systemapps.add(app);
				break;
			case Category.CATEGORY_CARRIERAPP:
				if(isDisplayCarrierAPPS){
					carrierapps.add(app);
				}else{
					systemapps.add(app);
				}
				break;
			case Category.CATEGORY_OEMAPP:
				if(isDisplayOEMAPPS){
					oemapps.add(app);
				}else{
					systemapps.add(app);
				}
				break;
			case Category.CATEGORY_3RDAPP:
			default:
				the3rdapps.add(app);
			}
		}
		
		final int nSa = systemapps.size();
		for(int i=0; i<nSa; i++){
			allapps.add(systemapps.get(i));
		}
		
		final int nCa = carrierapps.size();
		for(int i=0; i<nCa; i++){
			allapps.add(carrierapps.get(i));
		}
		
		final int nOa = oemapps.size();
		for(int i=0; i<nOa; i++){
			allapps.add(oemapps.get(i));
		}
		
		final int n3rda = the3rdapps.size();
		for(int i=0; i<n3rda; i++){
			allapps.add(the3rdapps.get(i));
		}
		
		initApplicationCategory(context);
		
		Log.d(TAG, "initApplicationData allapps:"+allapps.size()+" systemapps:"+systemapps.size()+" carrierapps:"+carrierapps.size()
				+" oemapps:"+oemapps.size()+" the3rdapps:"+the3rdapps.size()+" customize size:"+customizeApps.size());
	}
	
	private void dumpCustomizeCategory()
	{
		
	}
	/*
	 * return whether need refresh current allapps view.
	 * default return true;
	 */
	public static boolean addApps(ArrayList<ApplicationInfo> list, int currentCategory){
		int curCategoryAppAddedCount = 0;
		
		if(list==null || list.size()==0){
			return false;
		}
		
		if(Launcher.LOGD)Log.d(TAG, "addApp allapps ==== all:"+allapps.size()+" systemapps:"+systemapps.size()
				+" carrierapps:"+carrierapps.size()+" oemapps:"+oemapps.size()+" the3rdapps:"+the3rdapps.size());
		
		final int N = list.size();
		for(int i=0; i<N; i++){
			final ApplicationInfo app = list.get(i);
			
			if(currentCategory == app.category)
				curCategoryAppAddedCount ++;
			
			switch(app.category){
			case Category.CATEGORY_SYSTEMAPP:
				systemapps.add(0, app);
				break;
			case Category.CATEGORY_CARRIERAPP:
				if(isDisplayCarrierAPPS){
					carrierapps.add(0,app);
				}else{
					systemapps.add(0,app);
				}
				break;
			case Category.CATEGORY_OEMAPP:
				if(isDisplayOEMAPPS){
					oemapps.add(0,app);
				}else{
					systemapps.add(0,app);
				}
				break;
			case Category.CATEGORY_3RDAPP:
			default:
				the3rdapps.add(0, app);
			}
			
			allapps.add(0, app);
		}
		
		if(Launcher.LOGD)Log.d(TAG, "addApp allapps --- all:"+allapps.size()+" systemapps:"+systemapps.size()
				+" carrierapps:"+carrierapps.size()+" oemapps:"+oemapps.size()+" the3rdapps:"+the3rdapps.size());
		
		if(curCategoryAppAddedCount == 0){
			return false;
		}
		
		return true;
	}
	
	/*
	 * return whether need refresh current allapps view.
	 * default return true;
	 */
	public static boolean removeApps(ArrayList<ApplicationInfo> list, int currentCategory){
		int curCategoryAppAddedCount = 0;
		
		if(list==null || list.size()==0){
			return false;
		}
		
		if(Launcher.LOGD)Log.d(TAG, "removeApps allapps ==== all:"+allapps.size()+" systemapps:"+systemapps.size()
				+" carrierapps:"+carrierapps.size()+" oemapps:"+oemapps.size()+" the3rdapps:"+the3rdapps.size());
		
		final int N = list.size();
		for(int i=0; i<N; i++){
			final ApplicationInfo app = list.get(i);
			
			if(app.category == currentCategory)
				curCategoryAppAddedCount ++;
			
			switch(app.category){
			case Category.CATEGORY_SYSTEMAPP:
				systemapps.remove(app);
				break;
			case Category.CATEGORY_CARRIERAPP:
				carrierapps.remove(app);
				break;
			case Category.CATEGORY_OEMAPP:
				oemapps.remove(app);
				break;
			case Category.CATEGORY_3RDAPP:
			default:
				the3rdapps.remove(app);
			}
			
			//TODO, bug??????????
			if(entertainmentapps.remove(app))curCategoryAppAddedCount++;
			if(informationapps.remove(app))curCategoryAppAddedCount++;
			if(toolsapps.remove(app))curCategoryAppAddedCount++;
			if(shortcutapps.remove(app))curCategoryAppAddedCount++;
			
			allapps.remove(app);
		}
		
		if(Launcher.LOGD)Log.d(TAG, "removeApps allapps --- all:"+allapps.size()+" systemapps:"+systemapps.size()
				+" carrierapps:"+carrierapps.size()+" oemapps:"+oemapps.size()+" the3rdapps:"+the3rdapps.size());
		
		if(curCategoryAppAddedCount == 0){
			return false;
		}
		
		return true;
	}
	
	public static void addRecentUseApp(ApplicationInfo app){
		if(app == null)
			return;
		
		if(recentuseapps.size()==COUNT_RECENTUSE){
			recentuseapps.remove(COUNT_RECENTUSE-1);
		    recentuseapps.add(0, app);
		}else{
		    recentuseapps.add(0, app);
		}
	}
	
	public static ArrayList<ApplicationInfo> getAppsByCategory(int category){
		switch(category){
		case CATEGORY_SYSTEMAPP:
			return systemapps;
		case CATEGORY_CARRIERAPP:
			return carrierapps;
		case CATEGORY_OEMAPP:
			return oemapps;
		case CATEGORY_3RDAPP:
			return the3rdapps;
		case CATEGORY_Entertainment:
			return entertainmentapps;
		case CATEGORY_Information:
			return informationapps;
		case CATEGORY_Tools:
			return toolsapps;
		case CATEGORY_SHORTCUT:
			return shortcutapps;
		case CATEGORY_ALLAPP:
		default:
			return allapps;
		}
	}
	
	public static String getCategoryNameByCategory(int category){
		switch(category){
		case Category.CATEGORY_SYSTEMAPP:
			return nameSystem;
		case Category.CATEGORY_CARRIERAPP:
			return nameCarrier;
		case Category.CATEGORY_OEMAPP:
			return nameOem;
		case Category.CATEGORY_3RDAPP:
			return nameThe3rd;
		case Category.CATEGORY_RECENTUSE:
			return nameRecentUse;
		case Category.CATEGORY_ALLAPP:
			return nameAll;
		case Category.CATEGORY_SHORTCUT:
			return nameShortcut;
		case Category.CATEGORY_Entertainment:
			return nameEntertainment;
		case Category.CATEGORY_Information:
			return nameInformation;
		case Category.CATEGORY_Tools:
			return nameTools;
		default:
			return "";
		}
	}
	
	public static void initApplicationCategory(Context context){
		if(Launcher.LOGD)Log.d(TAG, "initApplicationCategory");
		
		List<LauncherORM.Category> enintents = LauncherORM.instance(context).getCategory(-1);
		if(enintents != null && enintents.size()>0){
			final int N = enintents.size();
			for(int i=0; i<N; i++){				
				String str = enintents.get(i).Intent;
				for(int j=0; j<allapps.size(); j++){
					ApplicationInfo info = allapps.get(j);
					
					String tmp = info.intent.getComponent().getPackageName()+"/"+info.intent.getComponent().getClassName();
					if(tmp.equals(str)){
						if(enintents.get(i).CID == LauncherProvider.ID_Entertainment)
						{
						    entertainmentapps.add(info);
						}else if(enintents.get(i).CID == LauncherProvider.ID_Information)
						{
							informationapps.add(info);
						}
						else if(enintents.get(i).CID == LauncherProvider.ID_Tools)
						{
							toolsapps.add(info);
						}
						else if(enintents.get(i).CID == LauncherProvider.ID_SHORT)
						{
							shortcutapps.add(info);
						}
					}				
				}
			}
		}
	}
	
	public static void addApplicationCategory(Context con, int cid, ApplicationInfo app){
		if(cid == CATEGORY_Entertainment)
		{
			for(int i=0; i<entertainmentapps.size(); i++){
				final ApplicationInfo info = entertainmentapps.get(i);
				if(info.intent.toUri(0).equals(app.intent.toUri(0)))
					return;
			}
			entertainmentapps.add(app);
		}else if(cid == CATEGORY_Information)
		{
			for(int i=0; i<informationapps.size(); i++){
				final ApplicationInfo info = informationapps.get(i);
				if(info.intent.toUri(0).equals(app.intent.toUri(0)))
					return;
			}
			informationapps.add(app);
		} 
		else if(cid == CATEGORY_Tools)
		{
			for(int i=0; i<toolsapps.size(); i++){
				final ApplicationInfo info = toolsapps.get(i);
				if(info.intent.toUri(0).equals(app.intent.toUri(0)))
					return;
			}
			toolsapps.add(app);
		} 
		else if(cid == CATEGORY_SHORTCUT)
		{
			for(int i=0; i<shortcutapps.size(); i++){
				final ApplicationInfo info = shortcutapps.get(i);
				if(info.intent.toUri(0).equals(app.intent.toUri(0)))
					return;
			}
			shortcutapps.add(app);
		} 
		
		String intent = app.intent.getComponent().getPackageName()+"/"+app.intent.getComponent().getClassName();		
		LauncherORM.instance(con).AddCategoryItem(cid-CATEGORY_CUSTOMIZE, intent);
	}
	
	public static void removeApplicationCategory(Context con,int cid, ApplicationInfo app){
		
		if(cid == CATEGORY_Entertainment)
		{
			for(int i=0; i<entertainmentapps.size(); i++){
				final ApplicationInfo info = entertainmentapps.get(i);
				if(info.intent.toUri(0).equals(app.intent.toUri(0)))
				{
					entertainmentapps.remove(i);
					break;
				}
					
			}			
		}else if(cid == CATEGORY_Information)
		{
			for(int i=0; i<informationapps.size(); i++){
				final ApplicationInfo info = informationapps.get(i);
				if(info.intent.toUri(0).equals(app.intent.toUri(0)))
				{
					informationapps.remove(i);
					break;
				}
			}			
		} 
		else if(cid == CATEGORY_Tools)
		{
			for(int i=0; i<toolsapps.size(); i++){
				final ApplicationInfo info = toolsapps.get(i);
				if(info.intent.toUri(0).equals(app.intent.toUri(0)))
				{
					toolsapps.remove(i);
					break;
				}
			}			
		} 
		else if(cid == CATEGORY_SHORTCUT)
		{
			for(int i=0; i<shortcutapps.size(); i++){
				final ApplicationInfo info = shortcutapps.get(i);
				if(info.intent.getComponent().equals(app.intent.getComponent())){
					Log.d(TAG, "removeApplicationShortcut info:"+info.intent.getComponent()+" app:"+app.intent.getComponent());
					shortcutapps.remove(i);
					break;
				}
			}			
		} 
		
		String intent = app.intent.getComponent().getPackageName()+"/"+app.intent.getComponent().getClassName();		
		LauncherORM.instance(con).deleteCategoryItem(cid-CATEGORY_CUSTOMIZE, intent);
	}
	
}

