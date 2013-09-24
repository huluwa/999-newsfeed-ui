package com.tormas.litetwitter.ui.view;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.tormas.litetwitter.R;
import com.tormas.litetwitter.service.SNSService;
import com.tormas.litetwitter.ui.ActivityBase;
import com.tormas.litetwitter.ui.TwitterHelper;
import com.tormas.litetwitter.ui.view.ImageCacheManager.ImageCache;
import twitter4j.AsyncTwitter;
import twitter4j.threadpool.QueuedThreadPool;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ImageRun implements Runnable
{
    private static final String TAG = "ImageRun";
	private String url;
    private ImageView imgView;
    boolean fromlocal;
    int     highPriority;    
    Handler pHandler;
    
    public int     width=120;
    public boolean noimage;
    public boolean use_avatar;
    public boolean need_scale;
    public boolean use_page;
    public boolean forceweb;//if true, every time get the image from web
    public boolean addHostAndPath=true;//need have a rule to name the save file
    public boolean need_compress = false; //need compress image as soon as get Image from web server only for (link photo,video photo)
    
    public ImageRun(Handler handler, String url, int highPriority)
    {
        this.url = url;
        this.highPriority = highPriority;        
        pHandler = new Handler();
    }
    public ImageRun(Handler handler, String url, boolean fromLocal, int highPriority)
    {
        this.url = url;
        fromlocal = fromLocal;
        this.highPriority = highPriority;        
        pHandler = new Handler();
    }    
    
    public void post(Runnable run )
    {
    	if(iaminCache == false && url != null)
    	{
    	    getThreadPool().dispatch(this);
    	}
    }
    
    static final int ImagePoolSize=8;
    static QueuedThreadPool threadpool=null;
    public static  QueuedThreadPool getThreadPool()
	{
		synchronized(QueuedThreadPool.class)
		{
	        if(null == threadpool)
	        {
	            threadpool = new QueuedThreadPool(ImagePoolSize);
	            threadpool.setName("Image--Thread--Pool");
	            try 
	            {
	            	threadpool.start();
	            } catch (Exception e) {}
	            
	            Runtime.getRuntime().addShutdownHook(new Thread() 
	            {
	                public void run() 
	                {
	                    if(threadpool != null)
	                    {
	                        try {
	                            threadpool.stop();
	                        } catch (Exception e) {}
	                    }
	                }
	            });
	        }
		}		
	    return threadpool;
	}
	 
    boolean iaminCache = false;
    static Map<View, String>imageurl = new HashMap<View, String>();
    public static void revokeAllImageView(Context con)
    {
    	Log.d(TAG, "*********  revokeAllImageView");
    	synchronized(imageurl)
    	{
    		Set<View> sets =  imageurl.keySet();
    		Iterator<View> it = sets.iterator();
    		while(it.hasNext())
    		{
    			View view = it.next();
    			Context tmp = view.getContext();
    			Log.d(TAG, "view ="+view + " context="+tmp);
    			if(ActivityBase.class.isInstance(tmp) && ActivityBase.class.isInstance(con) && tmp.equals(con))
    			{
    				String url = imageurl.get(view);
    				Log.d(TAG, "remove ="+view + " image url="+url + " context="+tmp + " current context="+con);
    				it.remove();
    				//imageurl.remove(view);
    			}
    		}
    	}
    }
    
    public void setImageView(ImageView view)
    {
    	iaminCache = false;
    	imgView = view;
    	final ImageCache cache = ImageCacheManager.instance().getCache(url);
    	if(cache != null && cache.bmp != null)
    	{
    		iaminCache = true;
    		pHandler.post( new Runnable()
        	{
        		public void run()
        		{
        			if(need_scale)
   			        {
	    			     Matrix matrix = new Matrix();
        		    	 int h = width;//imgView.getWidth();
        		    	 Bitmap mBaseImage = cache.bmp;
        		    	 
        		    	 if(mBaseImage != null)
        		    	 {
	                         float scale = (float)h/(float)mBaseImage.getWidth();
	        		         matrix.setScale(scale, scale);
	        		         mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage
	        		                  .getWidth(), mBaseImage.getHeight(), matrix, true);
	        		            
	        		         setImageBmp(mBaseImage);
        		    	 }
        		         matrix = null;
   			        }
        			else
        			{
        			    setImageBmp(cache.bmp);
        			}
        			
        		}
        	});
    	}
    	else
    	{   
    		//get from local, if has in local,
    		final String localpath = TwitterHelper.isImageExistInPhone(url, addHostAndPath);
    		if(localpath != null)
    		{
    		    if(forceweb == false)//no need force get image from web
    		    {
        			iaminCache = true;
        			//load from local
        			pHandler.post( new Runnable()
                	{
                		public void run()
                		{    
                			try{
    			    			 Bitmap tmp = BitmapFactory.decodeFile(localpath);
    			    			 if(tmp != null)
    			    			 {
    			    			     ImageCacheManager.instance().addCache(url, tmp);
    			    			     
    			    			     if(need_scale)
    			    			     {
	    			    			     Matrix matrix = new Matrix();
		 		        		    	 int h = width;//imgView.getWidth();
		 		        		    	 Bitmap mBaseImage = tmp;
		 		        		    	  
		 		        		    	 Log.d(TAG, "image ="+mBaseImage);
		                                 float scale = (float)h/(float)mBaseImage.getWidth();
		 		        		         matrix.setScale(scale, scale);
		 		        		         mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage
		 		        		                  .getWidth(), mBaseImage.getHeight(), matrix, true);
		 		        		            
		 		        		        setImageBmp(mBaseImage);
		 		        		        matrix = null;
    			    			     }
    			    			     else
    			    			     {
    			    			         setImageBmp(tmp);
    			    			     }
    			    			 }
    		    			 }catch(Exception ne)
    		    			 {
    		    				 dispose();
    		    			     //should we remove the file, maybe the file is bad
    		    			     //
    		    			     try{
    		    			         new File(localpath).delete();
    		    			     }catch(Exception nee){}
    		    				 Log.d(TAG, "exception=+"+ne.getMessage());
    		    			 }
    		    			 
                		}
                	});
    		    }
    		    else//TODO a bug for forceweb
    		    {
    		    	dispose();
    		    }    		    
    		}
    		else
    		{    		
	    		if(url != null && url.equals(defaultTwitterIcon))
	    		{
	    			iaminCache = true;
	    			//set as no image firstly, this will remove the pre-image
	        		pHandler.post( new Runnable()
	            	{
	            		public void run()
	            		{            			
	            			setImageBmp(R.drawable.default_profile_normal);
	        			}            		
	            	});
	    		}
	    		else
	    		{
	    			iaminCache = false;
		    		//set as no image firstly, this will remove the pre-image
		    		pHandler.post( new Runnable()
		        	{
		        		public void run()
		        		{            		
		        		    if(noimage == false)
		        		    {
		        		    	int res = R.drawable.no_avatar;
		        		    	if(use_avatar == false)
		        		    	{
		        		    		if(use_page)
		        		    		{
		        		    			res = R.drawable.pages;
		        		    		}
		        		    		else
		        		    		{
		        		    			res = R.drawable.noimage;
		        		    		}
		        		    	}
		        		    	
		        		    	if(need_scale)
		        		    	{
		        		    	    Matrix matrix = new Matrix();
		        		    	    int h = width;//imgView.getWidth();
		        		    	    Bitmap mBaseImage = BitmapFactory.decodeResource(SNSService.getSNSService().getResources(), res);
		        		    	  
		        		    	    Log.d(TAG, "image h="+ h+ "= height="+mBaseImage.getHeight() + " width="+mBaseImage.getWidth());
                                    float scale = (float)h/(float)mBaseImage.getWidth();
		        		            matrix.setScale(scale, scale);
		        		            mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage
		        		                  .getWidth(), mBaseImage.getHeight(), matrix, true);
		        		            
		        		            setImageBmp(mBaseImage);
		        		            matrix = null;
		        		    	}
		        		    	else
		        		    	{
		        		            setImageBmp(res);
		        		    	}
		        		    	
		        		    }
		        		   
		    			}            		
		        	});
		    		
		    		if(url != null)
		    		{
		    		    synchronized(imageurl)
		    		    {
				    	    imageurl.put(view,  url);
				    	    Log.d(TAG, "new mapcount="+imageurl.size() + " image="+url + " view="+imgView);
		    		    }				    	
		    		}
	    		}
    		}
    	}
    }
    
    final static String defaultTwitterIcon = "http://static.twitter.com/images/default_profile_normal.png";    
    final static String defaultSTwitterIcon = "https://static.twitter.com/images/default_profile_normal.png";
    
    Bitmap bmp  = null;
    public void run()
    {
    	//remove the pre-bmp
    	bmp = null;
    	if(imgView != null)
    	{	
    		if(fromlocal == false)
    		{
	            bmp = TwitterHelper.getImageFromURL(imgView.getContext(), url, highPriority==0?false:true, addHostAndPath);
    		}
    		else
    		{
    			bmp =  BitmapFactory.decodeFile(url);
    		}
    		
    		if(bmp == null)
            {
    		    synchronized(imageurl)
                {
        			String v = imageurl.get(imgView);
        			if(v != null && v.equals(url))
        			{        			    
        				imageurl.remove(imgView);                    
    	            	pHandler.post( new Runnable()
    	            	{
    	            		public void run()
    	            		{            		
    	            		    if(noimage == false)
    	            		    {
    	            		    	if(use_avatar == false)
    		        		    	{
    	            		    		if(use_page)
    		        		    		{
    		    	            			setImageBmp(R.drawable.pages);
    		        		    		}
    		        		    		else
    		        		    		{
    		    	            			setImageBmp(R.drawable.noimage);
    		        		    		}
    		        		    	}
    		        		    	else
    		        		    	{
    		        		    		setImageBmp(R.drawable.no_avatar);
    		        		    	}
    	            		    }
    	            		    else
    	            		    {
    	            		    	dispose();
    	            		    }
                			}            		
    	            	});
        			}
        			else
        			{
        				dispose();
        			}
                }
            }
            else
            {
                synchronized(imageurl)
                {
                	String v = imageurl.get(imgView);
        			if(v != null && v.equals(url))
        			{
        				imageurl.remove(imgView);
    	            	pHandler.post( new Runnable()
    	            	{
    	            		public void run()
    	            		{   
    	            		    if(need_compress == true)
    	            		    {
    	            		        if(bmp.getWidth()>width)
    	            		        {
    	            		            Matrix matrix = new Matrix();
                                        int h = width;
                                        Bitmap mBaseImage = bmp;
                                         
                                        Log.d(TAG, "need_compress image ="+mBaseImage);
                                        float scale = (float)h/(float)mBaseImage.getWidth();
                                        matrix.setScale(scale, scale);
                                        mBaseImage = Bitmap.createBitmap(mBaseImage, 0, 0, mBaseImage
                                                 .getWidth(), mBaseImage.getHeight(), matrix, true);
                                           
                                       setImageBmp(mBaseImage);
                                       matrix = null;
    	            		        }
    	            		        else
    	            		        {
    	            		            setImageBmp(bmp); 
    	            		        }
    	            		       
    	            		    }
    	            		    else
    	            		    {
                                    setImageBmp(bmp);
    	            		    }
    	            		}
    	            	});
        			}
        			else
        			{
        				dispose();
        			}
                }
            }
    	}    	
    }
    
    
    private void setImageBmp(int resbmp)
    {
    	if(null != imgView){
    		imgView.setImageResource(resbmp);
    		dispose();
    	}
    }
    
    private void setImageBmp(Bitmap bmp)
    {
    	if(null != imgView){
	    	imgView.setImageBitmap(bmp);
	    	dispose();
    	}
    }
    
    private void dispose()
    {
    	Log.i(TAG,"despose ================== remove reference="+imgView);
    	imgView = null;
    	pHandler = null;
    	url      = null;
    }
}
