package com.msocial.free.ui;

import java.util.List;

import com.msocial.free.*;
import com.msocial.free.service.SNSService;
import com.msocial.free.service.dell.OmsService;
import com.msocial.free.service.dell.OmsTask;
import com.msocial.free.service.dell.OmsTask.CancelTask;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class TaskManagementActivity extends FacebookBaseActivity{
	 final String TAG="TASK UI";
     TextView finished_text_view ;
     TextView running_text_view;
     TextView failed_text_view;
     TextView pendding_text_view;
     
     ListView finished_list_view;
     ListView running_list_view;
     ListView failed_list_view;
     ListView pendding_list_view;
     
     Button        cancel_button;
     
     List<OmsTask> runningtasks;
     List<OmsTask> finishedtasks;
     List<OmsTask> failedtasks;
     List<OmsTask> penddingtasks;
	 
	 @Override
	 public void onCreate(Bundle savedInstanceState)
	 {
	       super.onCreate(savedInstanceState);        
	       setContentView(R.layout.sns_task_management_ui);
	       
	       failed_text_view     = (TextView)findViewById(R.id.sns_failed_task_text);
	       running_text_view    = (TextView)findViewById(R.id.sns_running_task_text);
	       pendding_text_view   = (TextView)findViewById(R.id.sns_pendding_task_text);
	       finished_text_view   = (TextView)findViewById(R.id.sns_finished_task_text);
	       
	       failed_list_view     = (ListView)findViewById(R.id.sns_failed_task_list);
	       running_list_view    = (ListView)findViewById(R.id.sns_running_task_list);
	       finished_list_view   = (ListView)findViewById(R.id.sns_finished_task_list);
	       pendding_list_view   = (ListView)findViewById(R.id.sns_pendding_task_list);
	       
	       cancel_button        =  (Button)findViewById(R.id.cancel_all_task_button);
	       cancel_button.setOnClickListener(cancelListener);
	       cancel_button.setText(R.string.task_cancel_all);
	       	       
	       failed_text_view.setText(R.string.sns_failed_task_title);
	       running_text_view.setText(R.string.sns_running_task_title);
	       finished_text_view.setText(R.string.sns_finished_task_title);
	       pendding_text_view.setText(R.string.sns_pendding_task_title); 
	       
	       initListView();
	       setTitle(R.string.menu_title_refresh);
	       
	       Message mds = handler.obtainMessage(UI_REFRESH);
	       mds.getData().putBoolean("CONTINUE", true);
       	   handler.sendMessageDelayed(mds, 10*1000);
	 }
	 	 
	 @Override
	public void titleSelected()
	{		
		super.titleSelected();
		
		initListView();
	}

	View.OnClickListener cancelListener = new View.OnClickListener(){

		public void onClick(View arg0) {
			Log.d(TAG, "entering cancel all task ");
			CancelTask cancelTask = new CancelTask();
			if(OmsService.getInstance() == null)
			{
				Intent in = new Intent(TaskManagementActivity.this, SNSService.class);            
		        startService(in);
			}
			else
			{
				OmsService.getInstance().queueTask(cancelTask);
			}
		}
	
	 };
	 
	 
	 private void initListView()
	 {
		  if(OmsService.getInstance() == null)
		  {
			 Intent in = new Intent(this, SNSService.class);            
	         startService(in);
			 return;
		  }
		 
		  this.runningtasks  = OmsService.getInstance().getRunningTasks();
		  this.failedtasks   = OmsService.getInstance().getFailedTasks();
		  this.finishedtasks = OmsService.getInstance().getFinishedTasks();
		  this.penddingtasks = OmsService.getInstance().getPenddingTasks();
		  
		  Log.d(TAG,"runing= "+runningtasks.size()+ " failed= "+OmsService.failed_task_num + " finished="+OmsService.finished_task_num + " pend="+penddingtasks.size());		  
		  failed_text_view.setText(String.format("%1$s(%2$s)", getString(R.string.sns_failed_task_title),      OmsService.failed_task_num));
	      running_text_view.setText(String.format("%1$s(%2$s)", getString(R.string.sns_running_task_title),    runningtasks.size()));
	      finished_text_view.setText(String.format("%1$s(%2$s)", getString(R.string.sns_finished_task_title),  OmsService.finished_task_num));
	      pendding_text_view.setText(String.format("%1$s(%2$s)", getString(R.string.sns_pendding_task_title),  penddingtasks.size())); 
		  
		  SNSTaskAdapter taskAdapter;
		  if(runningtasks.size()>0){
			  taskAdapter = new SNSTaskAdapter(this,runningtasks);
			  running_list_view.setAdapter(taskAdapter);
		  }else{
			  running_list_view.setAdapter(null);
		  }
		  
		  if(failedtasks.size()>0){
			  taskAdapter = new SNSTaskAdapter(this,failedtasks);
			  failed_list_view.setAdapter(taskAdapter);
		  }else{
			  failed_list_view.setAdapter(null);
		  }
		  
		  if(finishedtasks.size()>0){
			  taskAdapter = new SNSTaskAdapter(this,finishedtasks);
			  finished_list_view.setAdapter(taskAdapter);
		  }else{
			  finished_list_view.setAdapter(null);
		  }
		  
		  if(penddingtasks.size()>0){
			  taskAdapter = new SNSTaskAdapter(this,penddingtasks);
			  pendding_list_view.setAdapter(taskAdapter);
		  }else{
			  pendding_list_view.setAdapter(null);
		  }
	 }
	 
	public void setTitle() 
	{
		title = "";
	}
	
	//no use
	@Override protected void createHandler()
	{
		handler = new RefreshHandler();
	}
	
	final int UI_REFRESH=0;
    private class RefreshHandler extends Handler 
    {
        public RefreshHandler()
        {
            super();            
            Log.d(TAG, "new RefreshHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
	            case UI_REFRESH:
	            {
	            	initListView();	            	
	            	this.removeMessages(UI_REFRESH);
	            	
	            	if(msg.getData().getBoolean("CONTINUE"))
	            	{	            	
    	            	Message mds = handler.obtainMessage(UI_REFRESH);
    	            	mds.getData().putBoolean("CONTINUE", true);
    	            	handler.sendMessageDelayed(mds, 10*1000);
	            	}
	            	break;
	            }
            }
        }
    }
    
    @Override protected void onPause() 
    {
        super.onPause();
             
        Message mds = handler.obtainMessage(UI_REFRESH);
        mds.getData().putBoolean("CONTINUE", false);
        handler.sendMessage(mds);
    }
    
    @Override protected void onResume() 
    {
        super.onResume();
        
        Message mds = handler.obtainMessage(UI_REFRESH);
        mds.getData().putBoolean("CONTINUE", true);
        handler.sendMessageDelayed(mds, 10*1000);
    }
    
	public void registerAccountListener() {}
	public void unregisterAccountListener() {}
	
	public class SNSTaskAdapter extends BaseAdapter
	{
		
		private final String TAG = "SNSTaskAdapter";
		Context context;
		List<OmsTask> items;
		
	    public SNSTaskAdapter(Context  context, List<OmsTask> tasks)
	    {	    	
	    	this.context = context;
	    	this.items   = tasks;
	    }
	    
		public int getCount() {
			return items.size();
		}

		public Object getItem(int pos) {

			return items.get(pos);
		}

		public long getItemId(int pos) {
			return items.get(pos).id;
		}

		public View getView(int pos, View convertView, ViewGroup arg2) {
			 if (pos < 0 || pos>= getCount()) 
			 {
	             return null;    
	         }
	         
			 SNSTaskItemView v=null;
		
			 OmsTask di = (OmsTask)getItem(pos);
	         if (convertView == null ) {
	             v = new SNSTaskItemView(context, di);
	         } else {
	              v = (SNSTaskItemView) convertView;
	              v.setTaskItem(di);
	         }        
	         
	         return v;
		}
	}
	
	public class SNSTaskItemView extends LinearLayout{
	    final String TAG = "SNSTaskItemView";
		Context mContext;
		OmsTask task;
		
		TextView taskId;
		TextView taskType;
		
		public SNSTaskItemView(Context context){
			super(context);
			this.mContext = context;
		}
		
		public SNSTaskItemView(Context context, OmsTask task){		
			super(context);
			this.mContext = context;
			this.task = task;
			init();
		}
		
		private void init()
		{			
			LayoutInflater factory = LayoutInflater.from(mContext);
			removeAllViews();
			
			//container
			FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);		
			FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
			view.setLayoutParams(paras);
			view.setVerticalScrollBarEnabled(true);
			addView(view);
			
			//child 1
			View v  = factory.inflate(R.layout.sns_task_item, null);		
			v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
			view.addView(v);
			
			taskId   = (TextView)findViewById(R.id.sns_task_id);
			taskType = (TextView)findViewById(R.id.sns_task_type);
			
			setUI();			
		}
		
		private void setUI(){
			taskId.setText(String.valueOf(task.taskID));
			taskType.setText(OmsTask.ACTION.getString(task.action));
		}
		
		public void setTaskItem(OmsTask item){
			this.task = item;
			setUI();
		}

	}
	
}
