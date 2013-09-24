package oms.sns.facebook.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import oms.sns.facebook.*;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FacebookShortCutActivity extends ListActivity
{	
	private class Item
	{
		String  activity;
		int     titleRes;
		int     iconres;
	}
	
	ListView lists;
	private List<Item> shotcuts = new ArrayList<Item>();
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        //build data
        buildData();
        setListAdapter(new ActivityAdapter(this, shotcuts));    
        lists = this.getListView();
        lists.setOnItemClickListener(listener);
    }
	
	AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adv, View v, int pos,long ID) 
		{
			//make short cut for pos
			constructShortCut(shotcuts.get(pos));
		}
	};
	
	private void constructShortCut(Item shotcut)
	{
		final Intent intent = createShortcutIntent(shotcut.titleRes,  shotcut.iconres, shotcut.activity);
        setResult(RESULT_OK, intent);
        finish();
	}
	
	 static Bitmap centerToFit(Bitmap bitmap, int width, int height, Context context) {
	        final int bitmapWidth = bitmap.getWidth();
	        final int bitmapHeight = bitmap.getHeight();

	        if (bitmapWidth < width || bitmapHeight < height) {
	            //int color = context.getResources().getColor(R.color.light_blue);

	            Bitmap centered = Bitmap.createBitmap(bitmapWidth < width ? width : bitmapWidth,
	                    bitmapHeight < height ? height : bitmapHeight, Bitmap.Config.RGB_565);
	            centered.setDensity(bitmap.getDensity());
	            Canvas canvas = new Canvas(centered);
	            //canvas.drawColor(color);
	            canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(2,2,centered.getWidth()-2, centered.getHeight()-2),
	                    null);

	            bitmap = centered;
	        }

	        return bitmap;
	    }
	
	private Intent createShortcutIntent(int titleRes, int iconRes, String activity) 
	{
        final Intent i = new Intent();
        final Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
        shortcutIntent.setClassName("oms.sns.facebook", activity);        		
        i.putExtra(Intent.EXTRA_SHORTCUT_INTENT,    shortcutIntent);
        i.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(titleRes));
        
        //this is photo
        Bitmap bmp = null;		
		try{				    
			bmp = BitmapFactory.decodeResource(getResources(), iconRes);	    	    
	    }
		catch(Exception ne){}
		
		//resize the bmp to 60dip
		bmp = centerToFit(bmp, 72, 72, this);
		
        Bitmap favicon = BitmapFactory.decodeResource(getResources(),R.drawable.facebook_ico);
        
        // Make a copy of the regular icon so we can modify the pixels.
        Bitmap copy = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(copy);

        // Make a Paint for the white background rectangle and for
        // filtering the favicon.
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(Color.WHITE);

        // Create a rectangle that is slightly wider than the favicon
        final DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        final float density = metrics.density;
        
        // Create a rectangle that is slightly wider than the favicon
        final float iconSize = 8*density; // 16x16 favicon
        final float padding = 1*density;   // white padding around icon
        
        final float rectSize = iconSize + 2 * padding;
        final float y = bmp.getHeight() - rectSize;
        RectF r = new RectF(0, y, rectSize, y + rectSize);

        // Draw a white rounded rectangle behind the favicon
        canvas.drawRoundRect(r, 2, 2, p);

        // Draw the favicon in the same rectangle as the rounded rectangle
        // but inset by the padding (results in a 16x16 favicon).
        r.inset(padding, padding);
        canvas.drawBitmap(favicon, null, r, p);
        i.putExtra(Intent.EXTRA_SHORTCUT_ICON, copy);
    
        // Do not allow duplicate items
        i.putExtra("duplicate", false);
        return i;
    }
	
	private void buildData()
	{
		PackageManager pm = getPackageManager();
        PackageInfo pinfo = null;
        try 
        {
            pinfo = pm.getPackageInfo("oms.sns.facebook", PackageManager.GET_ACTIVITIES);
            if (pinfo != null) 
            {
                for (ActivityInfo act : pinfo.activities) 
                {
                	if(
                	   act.name.contains(".ui.FacebookStreamActivity") ||                	   
                	   act.name.contains(".ui.FacebookNotesActivity") ||      
                	   act.name.contains(".ui.FacebookLocationUpdateActivity") ||
                	   act.name.contains(".ui.FacebookNotificationsActivity") ||
                	   act.name.contains(".ui.FacebookAccountActivity") ||
                	   act.name.contains(".ui.FacebookSettingPreference") ||                	                  		
                	   act.name.contains(".ui.FacebookMessageActivity") ||
                 	   act.name.contains(".ui.FacebookFriendsActivity") ||
                 	   act.name.contains(".ui.FacebookPhonebookActivity") ||
                 	   act.name.contains(".ui.FacebookEventActivity")     ||
                 	   act.name.contains(".ui.FacebookAlbumActivity")     ||
                 	   act.name.contains(".ui.FacebookPageActivity")
                 	   )                		
                	{
                		Item item = new Item();
                		item.activity = act.name;
                		item.iconres  = act.icon;
                		item.titleRes = act.labelRes;
                		shotcuts.add(item);                		
                	}
                }
            }
        }catch (Exception e) {}
	}
	
	private static class ActivityAdapter extends BaseAdapter 
    {
        private LayoutInflater mInflater;	         

        private Context mContex;
        List<Item> items;
        public ActivityAdapter(Context context, List<Item> items) 
        {        
            mInflater = LayoutInflater.from(context);
            this.items = items;
            mContex = context;
        }

        public int getCount() {
            return items.size();
        }

        public Object getItem(int position) 
        {
            return items.get(position);
        }	       
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) 
            {
                convertView = mInflater.inflate(R.layout.list_item_icon_text, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);

                convertView.setTag(holder);
            } 
            else 
            {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            Item item = items.get(position);
            
            holder.text.setText(item.titleRes);
            holder.icon.setImageBitmap(BitmapFactory.decodeResource(mContex.getResources(), item.iconres));

            return convertView;
        }
        
        static class ViewHolder 
        {
            TextView text;
            ImageView icon;
        }
    }	   
}
