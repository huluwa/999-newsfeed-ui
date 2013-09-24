package com.borqs.omshome25;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.borqs.omshome25.CellLayout.CellInfo;
import com.borqs.omshome25.R;

public class AllAppsListDialog implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
	private AllAppsListAdapter mAllAppsListAdapter;
	private AlertDialog mAlertDialog;
	private ArrayList<ApplicationInfo> appslist;
	private Launcher mLauncher;
	private boolean isSelectedForShortCut;
	private int cateID;
	private String TAG="AllAppsListDialog";
	
	private BroadcastReceiver mHomeReceiver = new HomeKeyReceiver();
	
	public AllAppsListDialog(Launcher launcher, boolean flag){
		this(launcher, flag, -1);
	}
	
	public AllAppsListDialog(Launcher launcher, boolean flag, int categoryID){
		mLauncher             = launcher;
		isSelectedForShortCut = flag;
		cateID = categoryID;
		
		if(categoryID == -1)
		{
			Log.d(TAG, "for pick application");
		}
		
		registerHomeKeyPress(launcher);
	}
	
	private void setSelected(ArrayList<ApplicationInfo> list)
	{
		ArrayList<ApplicationInfo> tmpList = Category.getAppsByCategory(cateID);
		for(ApplicationInfo info: list)
		{
			info.selected = false;
		}
		
		for(ApplicationInfo info: tmpList)
		{
			info.selected = true;
		}
	}
	
	public Dialog createDialog(ArrayList<ApplicationInfo> list) {
		appslist = list;
		setSelected(appslist);
		
		mAllAppsListAdapter = new AllAppsListAdapter(mLauncher, list);
		if(isSelectedForShortCut)
		{
		    mAllAppsListAdapter.setSelection(true);
		}
		else
		{
			mAllAppsListAdapter.setSelection(false);
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(mLauncher);
		builder.setTitle(mLauncher.getResources().getString(R.string.activity_picker_label));	
		if(isSelectedForShortCut == false)
		{
		    builder.setAdapter(mAllAppsListAdapter, this);
		}
		else
		{
			builder.setAdapter(mAllAppsListAdapter, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface di, int which) {				
					Log.d(TAG, "onClick="+which);
					appslist.get(which).selected = !appslist.get(which).selected;
					
					AppSelectView av = (AppSelectView)mAllAppsListAdapter.getView(which, null, null);
					av.setUI();					
				}
			});
			
			builder.setNegativeButton(mLauncher.getString(R.string.cancel_action),
	            new Dialog.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	
	                }
	            }
		    );
				
			builder.setPositiveButton(mLauncher.getString(R.string.rename_action),
	            new Dialog.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	LauncherORM.instance(mLauncher).deleteCategory(cateID);
	                	Category.getAppsByCategory(cateID).clear();
	                	for(ApplicationInfo item :appslist)
	                	{	                	
	                		if(item.selected)
	                		{
	                	        Category.addApplicationCategory(mLauncher, cateID, item, true);
	                		}
	                	}
	                	
	                	if(aa2DLayout != null)
	                	{
	                	    aa2DLayout.refreshAllAppsUI();
	                	}
	                }
	            }
		    );
			
		}
			
		mAlertDialog = builder.create();
		mAlertDialog.setOnCancelListener(this);
		
		mAlertDialog.setOnDismissListener(new OnDismissListener()
		{
			public void onDismiss(DialogInterface arg0) {				
				mLauncher.unregisterReceiver(mHomeReceiver);
			}			
		});		
		

		return mAlertDialog;
	}

	private void registerHomeKeyPress(Context con)
	{
		 IntentFilter backAndHomefilter = new IntentFilter(Launcher.HOMEKEY_PRESSED_IN_HOME); //home
		 con.registerReceiver(mHomeReceiver, backAndHomefilter);
	}
	
	private class HomeKeyReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			if(mAlertDialog != null && mAlertDialog.isShowing())
			{
				mAlertDialog.dismiss();
			}
		}
	}
	
	private static class AllAppsListAdapter extends BaseAdapter {
		private ArrayList<ApplicationInfo> apps;
		private Context context;
		private boolean isForSelection;
		
		public AllAppsListAdapter(Context con, ArrayList<ApplicationInfo> list) {
			apps = list;
			context = con;
		}
		
		public void setSelection(boolean sel) {
			isForSelection = sel;
		}

		public int getCount() {
			return apps.size();
		}

		public Object getItem(int position) {
			return apps.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (position < 0 || position >= getCount()) {
				return null;
			}

			View ttv;
			if(isForSelection == false)
			{
				TextView tv;
				if (convertView != null && convertView instanceof TextView) {
					tv = (TextView) convertView;
				} else {
					LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					tv = (TextView) inflate.inflate(R.layout.all_apps_list_item, null);
				}
	
				final ApplicationInfo app = apps.get(position);
				
				final BitmapDrawable bd = new BitmapDrawable(app.iconBitmap);
				bd.setTargetDensity(context.getResources().getDisplayMetrics());
				tv.setCompoundDrawablesWithIntrinsicBounds(bd, null, null, null);
				tv.setText(app.title);
				
				ttv = tv;
			}
			else
			{
				AppSelectView tv;
				if (convertView != null && convertView instanceof TextView) {
					tv = (AppSelectView) convertView;
				} else {
					LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					tv = (AppSelectView) inflate.inflate(R.layout.all_apps_list_item_sel, null);
				}
	
				final ApplicationInfo app = apps.get(position);
				tv.setAppInfo(app);				
				
				ttv = tv;
			}

			return ttv;
		}
	}
	
	private void cleanup() {
		mAlertDialog.dismiss();
	}
	
	public void onClick(DialogInterface dialog, int which) {
		cleanup();
		
		final ApplicationInfo app = (ApplicationInfo) appslist.get(which);
		if(isSelectedForShortCut){
			if(aasLayout != null){
				Category.addApplicationCategory(mLauncher, cateID, app);
				aasLayout.refreshAllAppsUI();
			}else if(aa2DLayout != null)
			{			
				app.selected = !app.selected;
				mAllAppsListAdapter.notifyDataSetChanged();
				//Category.addApplicationCategory(mLauncher, cateID, app);
				//aa2DLayout.refreshAllAppsUI();
			}
		}else{
			CellInfo cellInfo = mLauncher.getMaddItemCellInfo();
			final Workspace mWorkspace = mLauncher.getWorkspace();
			cellInfo.screen = mWorkspace.getCurrentScreen();
			if (!mLauncher.findSingleSlot(cellInfo)) return;
			
			final ShortcutInfo info = mLauncher.getLauncherModel().getShortcutInfo(mLauncher.getPackageManager(),
					app.intent, mLauncher);
			
			if (info != null) {
				info.setActivity(app.intent.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK |
						Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				info.container = ItemInfo.NO_ID;
				mWorkspace.addApplicationShortcut(info, cellInfo, mLauncher.isWorkspaceLocked());
			}   
		}
	}

	public void onCancel(DialogInterface dialog) {
		cleanup();
	}
	
	private AllAppsScreenLayout aasLayout;
	public void setShortcutLayout(AllAppsScreenLayout layout){
		aasLayout = layout;
	}
	
	private AllApps2DWithCategory aa2DLayout;
	public void setShortcutLayout(AllApps2DWithCategory layout){
		aa2DLayout = layout;
	}
}
