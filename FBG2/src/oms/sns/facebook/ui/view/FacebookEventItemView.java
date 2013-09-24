package oms.sns.facebook.ui.view;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.util.StringUtils;
import oms.sns.facebook.R;
import oms.sns.facebook.service.FacebookLoginHelper;
import oms.sns.facebook.ui.ActivityBase;
import oms.sns.facebook.ui.NetworkConnectionListener;
import oms.sns.facebook.util.DateUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookEventItemView extends SNSItemView{
    
	private Event item;
	private final static String TAG = "FacebookEventItemView";
	private boolean setDate = true;
	
	private TextView  timeview;
	private TextView  whenview;
	private ImageView imageview;
	private TextView  nameview;
	private TextView  dateview;
	
	private Handler handler;
	
	 public Event getEvent()
	 {
	    	return item;
	 }
	 
     public FacebookEventItemView(Context ctx, AttributeSet attrs) 
	 {
			super(ctx, attrs);		
			mContext = ctx;		
			setOrientation(LinearLayout.VERTICAL);
	        this.setVisibility(View.VISIBLE);
	    }
		
		public FacebookEventItemView(Context context, Event event) 
		{		
			super(context);
			mContext = context;
			item = event;
			Log.d(TAG, "call  FacebookEventItemView");
			init();
		}
			
		private void init() 
		{
			Log.d(TAG,  "call FacebookEventtItemView init");
			LayoutInflater factory = LayoutInflater.from(mContext);
			removeAllViews();
			
			//container
			//FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);			
			//FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
			//view.setLayoutParams(paras);
			//view.setVerticalScrollBarEnabled(true);
			//addView(view);
			
			//child 1
			View v  = factory.inflate(R.layout.facebook_event_item, null);	
			v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		
			addView(v);			
			dateview   = (TextView)v.findViewById(R.id.facebook_event_date);
			timeview   = (TextView)v.findViewById(R.id.facebook_event_time);
			imageview  = (ImageView)v.findViewById(R.id.facebook_event_img_ui);
			nameview   = (TextView)v.findViewById(R.id.facebook_event_name);
			whenview   = (TextView)v.findViewById(R.id.facebook_event_when_detail);

			
					
			setUI();
		}
		
		private void setUI()
		{
		    if(setDate == true)
		    {
		        setDateUI();
		    }
		    else
		    {
		        dateview.setVisibility(View.GONE);
		        if(isEmpty(item.pic_small) == false)
	            {	                
	                imageview.setImageResource(R.drawable.event);
	                ImageRun imagerun = new ImageRun(handler, item.pic_small, false, 0);
	                imagerun.noimage = true;
	                //imagerun.need_scale = true;
	                //imagerun.width = 50;
	                imagerun.setImageView(imageview);           
	                imagerun.post(imagerun);	               
	            }
	            else
	            {
	                imageview.setImageResource(R.drawable.event);
	            }
		    }
		    
			if(item.tagline != null && item.tagline.length() > 0)
				nameview.setText(item.name);
			else
			    nameview.setText(item.name);
		   
			SimpleDateFormat sdf = new SimpleDateFormat("h:mm a EEE, MMM d",Locale.US);//, ''yy");
			sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
			whenview.setText(sdf.format(item.start_time));		    
		}
		
		private void setDateUI() {
		    dateview.setVisibility(View.VISIBLE);
		    float density = getResources().getDisplayMetrics().density;
		    Matrix matrix = new Matrix();
            int h = (int)(50*density);//imgView.getWidth();
            Bitmap mBaseImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.calendaricon_60);
            
            if(mBaseImage != null)
            {
                float scale = (float)h/(float)mBaseImage.getWidth();
                //Log.d(TAG,"baseImage widht is "+mBaseImage.getWidth()+"width is "+h +"scale is "+scale);
                matrix.setScale(scale, scale);
                mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage
                         .getWidth(), mBaseImage.getHeight(), matrix, true);
                imageview.setImageBitmap(mBaseImage);
            }
            dateview.setText(String.valueOf(DateUtil.getPSTDate(item.start_time)));
            if(item.start_time.getDate()<10)
                dateview.setPadding((int)(21*density), 0, 0, 0);
            else
                dateview.setPadding((int)(17*density), 0, 0, 0);
        }

        @Override
		protected void onFinishInflate() 
		{	
			super.onFinishInflate();		
			init();
		}
			

		public void setContentItem(Event event) 
		{
			item = event;		
			setUI();
		}
		

		public void setContentItem(Event event, boolean useCursor) 
		{
			item.despose();
			item = null;
			item = event;		
			setUI();
		}

		@Override
		public String getText() 
		{			
			StringBuilder sb = new StringBuilder();
			sb.append(mContext.getString(R.string.facebook_event_hosted_by)+"\t\t"+item.host);
			sb.append(mContext.getString(R.string.facebook_event_type)+"       \t\t"+item.event_type+"-"+item.event_sbytype);
			sb.append(mContext.getString(R.string.facebook_event_where)+"      \t\t"+item.location);
			sb.append(mContext.getString(R.string.facebook_event_when)+"       \t\t"+
                    mContext.getString(R.string.facebook_event_from)+" "+item.start_time.toLocaleString()+" "+
                    mContext.getString(R.string.facebook_event_to)+" "+item.end_time.toLocaleString());
			return sb.toString();
		}

        public void setTimeViewGone() {            
            timeview.setVisibility(View.GONE);
        }

        public void setTimeViewVisible(int cat) {            
            timeview.setVisibility(View.VISIBLE);
            if(cat == -1)
            {
                timeview.setText(R.string.facebook_event_this_week);
            }
            else if(cat ==-2)
            {
                timeview.setText(R.string.facebook_event_this_month);
            }
            else if(cat == -3)
            {
                timeview.setText(R.string.facebook_event_others);
            }
            else if(cat == -4)
            {
                timeview.setText(R.string.facebook_upcoming_event);
            }
            else if(cat == -5)
            {
                timeview.setText(R.string.facebook_past_event);
            }            
        }		
}
