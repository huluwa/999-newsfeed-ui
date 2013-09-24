/*
 * Copyright (C) 2006-2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package oms.sns.facebook.service.dell;

import java.util.ArrayList;
import java.util.List;

import oms.sns.facebook.service.dell.OmsTask.ACTION;

import android.util.Config;
import android.util.Log;

public class MessageQueue 
{
	final static String TAG="OmsService";
	OmsTask  mMessages;
	
    private boolean mQuiting = false;
    boolean mQuitAllowed = true;

    Object lock1 = new Object();
    Object lock2 = new Object();  
    MessageQueue() 
    {}
        
    boolean enqueueMessage(OmsTask msg)
    {
    	return enqueueMessage(msg, 0);
    }
    
    List<OmsTask> next(int count) 
    {        
    	List<OmsTask> al = new ArrayList<OmsTask>();    	
    	synchronized (lock1)
        {
            // Try to retrieve the next message, returning if found.
    	    boolean meetNoneLookup = false;
    		while (true)     
            {   
    			OmsTask msg = null;
    			while(count > 0)
    			{
                    msg = pullNextLocked();
                    if(msg != null)
                    {
                    	al.add(msg);
                    	if(msg.action != OmsTask.ACTION.CONTACT_LOOK_UP)
                        {
                    	    meetNoneLookup = true;
                            Log.d(TAG, "we get none-lookpp add 1, so break directly");
                            break;
                        }
                    }
                    else
                    {
                    	break;
                    }
                    // just get specified count tasks 
                    count--;
                    
    			}
                if (al.size()==0) 
                {
                	try{
                	    if (OmsService.DEBUG)  Log.v(TAG, "before task");
                	    lock1.wait();
                	    if (OmsService.DEBUG)  Log.v(TAG, "after task");
                	} catch (InterruptedException e) { }
                }
                else
                {
                    //if have event, we need process immediately
                    if(meetNoneLookup)
                    {
                        break;
                    }
                	//then get all lookup task
        		    OmsTask nextlookup=null;
        			//wait 2 minutes to get more task 
        			long waittime=System.currentTimeMillis();
        			long intervaltime = 0;
        			 Log.d(TAG, "intervaltime is " + intervaltime);
        			while((nextlookup = pullNextLookupLocked()) != null || (intervaltime = System.currentTimeMillis()-waittime)<2*60*1000L)
        			{        			   
        				if(nextlookup != null)
        				{
        				    al.add(nextlookup);
        				    if(nextlookup.action != OmsTask.ACTION.CONTACT_LOOK_UP)
        				    {
        				        Log.d(TAG, "we get none-lookup add 2, so break directly");
        				        break;
        				    }
        				    if(al.size() > 500)
        				    {
        				    	Log.d(TAG, "you have get 500 task, will dispatch this firstly");
        				    	break;
        				    }
        				}
        				else
        				{
        					try{
                        	    lock1.wait(20);                        	    
                        	} catch (InterruptedException e) { }
        				}
        			}
        			
        			Log.d(TAG, "intervaltime is "+intervaltime);
        			
                	break;
                }
            }
        }  
    	
    	Log.d(TAG, "get messages size ="+al.size());
    	return al;
    }    
    OmsTask pullNextLocked() {
    	OmsTask msg = mMessages;
        if (msg != null) 
        {           
        	mMessages = msg.next;
            if (OmsService.DEBUG) Log.v(  "MessageQueue", "Returning message: " + msg);
            return msg;        
        }
        return null;
    }
    
    OmsTask pullNextLookupLocked() 
    {    	
    	if(mMessages == null)
    		return null;
    	
    	//process header
    	OmsTask header = mMessages;
    	if(header != null && header.action == OmsTask.ACTION.CONTACT_LOOK_UP)
    	{
    		mMessages = header.next;
    		if (OmsService.DEBUG) Log.v(  "MessageQueue", "Returning lookup message: " + header);
    		return header;
    	}
    	
    	OmsTask pre = mMessages;
    	OmsTask msg = mMessages.next;
        while(msg != null) 
        {           
        	if(msg.action == OmsTask.ACTION.CONTACT_LOOK_UP)
        	{
        		pre.next = msg.next;
        		
        		if (OmsService.DEBUG) Log.v(  "MessageQueue", "Returning lookup message: " + msg);
                return msg;
        	}
        	else
        	{
        		pre = msg;
        		msg = msg.next;
        	}
        }
        return null;
    }
    
    boolean enqueueMessage(List<OmsTask> msgs, long when)
    {
    	if(msgs == null || msgs.size() ==0)
    	{
    		return false;
    	}
    	
    	//insert the message
        synchronized (lock1) 
        {
	    	for(int i=0;i<msgs.size();i++)
	    	{
	    		OmsTask msg = msgs.get(i);
		    	if(msg.action == ACTION.CANCEL_ALL)
			   	{
			   		removeAllTasks();
			   	}
	   	   
	            if (mQuiting) {
	                RuntimeException e = new RuntimeException(" sending message to a Handler on a dead thread");
	                Log.w("MessageQueue", e.getMessage(), e);
	                return false;
	            } 
	
	            msg.when = when;
	            Log.d("MessageQueue", "Enqueing: " + msg);
	            OmsTask p = mMessages;
	            if (p == null || when == 0 || when < p.when) 
	            {
	                msg.next = p;
	                mMessages = msg;
	                
	                //TODO
	                //remove the repeat
	                OmsTask prev = null;
		            OmsTask tmp  = null;
		            OmsTask header = mMessages;
	                while(header != null && header.next != null)
	                {
		               	prev = header;
		               	tmp = header.next;
		               	if(tmp.id != OmsTask.INVALIDID && tmp.id == msg.id)
		               	{
		               		prev.next = tmp.next;
		               		break;
		               	}   
		               	header = header.next;
	                }
	            }      
	            else 
		        {
	            	OmsTask prev = null;
	                while (p != null && p.when <= when) 
	                {
	                    prev = p;
	                    p = p.next;
	                }
	                msg.next = prev.next;
	                prev.next = msg;	                	               
		       }	                
	        }
	    	
	    	//let thread go on
	        lock1.notifyAll();
    	}    	
        return true;
    }
    
    /*
        don't use this code to substitue the function, this is a frequency call function, it is better no new
        List<OmsTask> msgs = new ArrayList<OmsTask>();
    	msgs.add(msg);
    	return enqueueMessage(msgs, when);
     */
    boolean enqueueMessage(OmsTask msg, long when) 
    {   
    	 //remove all task
    	 if(msg.action == ACTION.CANCEL_ALL)
    	 {
    		removeAllTasks();
    	 } 
    	 
    	 //insert the message
         synchronized (lock1) 
         {
             if (mQuiting) {
                 RuntimeException e = new RuntimeException(" sending message to a Handler on a dead thread");
                 Log.w("MessageQueue", e.getMessage(), e);
                 return false;
             } 

             msg.when = when;
             Log.d("MessageQueue", "Enqueing: " + msg);
             OmsTask p = mMessages;
             if (p == null || when == 0 || when < p.when) 
             {
                 msg.next = p;
                 mMessages = msg;
                 
                 //TODO
                 //remove the repeat
                 OmsTask prev = null;
	             OmsTask tmp  = null;
	             OmsTask header = mMessages;
                 while(header != null && header.next != null)
                 {
                	prev = header;
                	tmp = header.next;
                	if(tmp.id != OmsTask.INVALIDID && tmp.id == msg.id)
                	{
                		prev.next = tmp.next;
                		break;
                	}   
                	header = header.next;
                 } 
                 lock1.notifyAll();
             }      
             else 
	         {
	            	OmsTask prev = null;
	                while (p != null && p.when <= when) 
	                {
	                    prev = p;
	                    p = p.next;
	                }
	                msg.next = prev.next;
	                prev.next = msg;
	                //let thread go on
	                lock1.notifyAll();	               
	         }
                 
         }
         return true;
    }
    
    public List<OmsTask> getPenddingTask()
    {
    	Log.d(TAG,"entering get Pendding Task ");
    	List<OmsTask> penddingtask = new ArrayList<OmsTask>();    	
    	OmsTask p = mMessages;        
        while (p != null) 
        {
        	penddingtask.add(p);
            p = p.next;
        }
    	return penddingtask;    	
    }
    
    public void dumpQueue_l()
    {
    	OmsTask p = this.mMessages;
        Log.d(TAG, "message queue is:");
        while (p != null) 
        {
        	Log.d(TAG, "            " + p);
            p = p.next;
        }
    }

    void poke()
    {
        synchronized (this) 
        {
            lock1.notifyAll();            
        }
    }

    //remove one type task
	public void removeAllTasks() 
	{	
		synchronized(lock1)
		{
			OmsTask p = mMessages;
            // Remove all messages
            while (p != null)
            {
            	Log.d(TAG, "remove task == "+p);
            	OmsTask n = p.next;
                mMessages = n;
                p.recycle();
                p = null;
                p = n;
            }
		}
	}
    //remove one type task
	public void removeTasks(int type) 
	{	
        synchronized (this) 
        {
        	OmsTask p = mMessages;
        	// Remove all messages at front.
            while (p != null && p.action == type) 
            {
            	OmsTask n = p.next;
            	//change the header position
            	if(mMessages.taskID == p.taskID)
            	{
                    mMessages = p.next;
            	}
            	
                p.recycle();
                p = null;
                p = n;
            }           
        }

	}

	//remove one task
	public boolean removeTask(long taskID) 
	{		
	    synchronized (lock1) 
	    {
            OmsTask p = mMessages;
            //go to next position
            if(mMessages.taskID == taskID)
            {   
                mMessages = p.next;
            }
            
            boolean found = false;

            // Remove all messages at front.
            while (p != null && p.taskID == taskID) {	                
                found = true;
                OmsTask n = p.next;                
                p.recycle();
                p = n;
            }
            return found;
        }
	}
}
