package com.msocial.facebook.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.conn.ssl.AbstractVerifier;

import com.msocial.facebook.service.SNSService;
import com.msocial.facebook.ui.view.ImageCacheManager;
import com.msocial.facebook.ui.view.ImageCacheManager.ImageCache;
import oms.sns.service.facebook.client.RestPostMethod.myHostnameVerifier;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StatFs;
import android.util.Log;
import android.os.FileUtils;

public class TwitterHelper {
	private static final String TAG="Image-fetch-Helper";
	
	private static ImageCacheManager cachemanager;
	public static String tempimagePath = "/sdcard/.sns/";
	public static final String tempimagePath_nosdcard = "/data/data/com.msocial.facebook/files/";
	public static final String data_files = "/data/data/com.msocial.facebook/files/";
	
	private static String tmpPath=tempimagePath;
	
	public static String newsfeed     = TwitterHelper.getTmpCachePath() + "fb_newsfeed.ser";
	public static String lastmail     = TwitterHelper.getTmpCachePath() + "fb_lastmail.ser";
	public static String notification = TwitterHelper.getTmpCachePath() + "fb_notifications.ser";
	public static String filter       = TwitterHelper.getTmpCachePath() + "fb_filter.ser";
	public static String profile      = TwitterHelper.getTmpCachePath() ;
	public static String twitterfriendline = TwitterHelper.getTmpCachePath()+"friendline/";
	public static String twitteruserline   = TwitterHelper.getTmpCachePath()+"userline/";
	
	static StatFs stat = null;
	static
	{
		tempimagePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sns/";
		tmpPath=tempimagePath;
		Log.d(TAG, "sdcard ="+tempimagePath);
	    cachemanager = ImageCacheManager.instance();
	    setStaticVariabl(false);		  
	}
	static long remainSize= 4*1024*1024L;
	static void setStaticVariabl(boolean readonly)
	{
		if(readonly==true)
		{
			tmpPath = tempimagePath_nosdcard;
			stat = null;//new StatFs("/data/data/oms.sns.main/files/");
			remainSize = 4*1024*1024L;
		}
		else
		{		
			  if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) == false)
		      {
			      tmpPath = tempimagePath_nosdcard;
			      remainSize = 4*1024*1024L;
		      }
			  else
			  {
				  tmpPath=tempimagePath;
				  remainSize = 4*1024*1024L;
				  
				  boolean snsdir = new File(tmpPath).mkdirs();
				  if(snsdir == false)
				  {
					  Log.d(TAG, "why make dir fail="+tmpPath);
				  }
			  }
			  			
              
			  //for statics
			  if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) == true)
			  {    	    
				  try
				  {
					  stat = null;
			          stat = new StatFs(android.os.Environment.getExternalStorageDirectory().getAbsolutePath());
				  }
				  catch(java.lang.IllegalArgumentException ne)
				  {
					  Log.d(TAG, "why come here="+ne.getMessage());
				  }
			  }
			  else
			  {
				  stat = null;//new StatFs(data_files);
			  }
		}
	}
	
	//TODO monitor sdcard unmount/mount
	//if mounted sdcard, need to move the sns/images to /sdcard/
	//and reset stat, tmpPath
	public static void unmountSdcard()
	{
		setStaticVariabl(false);
	}
	public static void mountSdcard(boolean readonly)
	{
		setStaticVariabl(readonly);
	}
	
	public static String getTmpPath()
	{
		return tmpPath;
	}
	
	private static String cachePath = "";
	public static String getTmpCachePath()
	{
		 if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) == false)
	     {
			  cachePath = data_files+"cache/";
	     }
		 else
		 {
			 cachePath = tempimagePath + "cache/";
		 }
		 
		 if(new File(cachePath).exists() == false)
		 {
		      new File(cachePath).mkdirs();
		 }
		 return cachePath;
	}
	
	public static void ClearCache()
	{
		Log.d(TAG, "clear all cache files");
		try{
			 File path = new File(data_files+"cache/");
	         deleteFiles(path);
	      	 
	         File path_nosdcard = new File(tempimagePath+"cache/");
	         deleteFiles(path_nosdcard);
		}catch(Exception ne){}
	}
	
    public static boolean deleteDirectory(File path) 
    {
	    if( path.exists() ) 
	    {
	        File[] files = path.listFiles();
	        for(int i=0; i<files.length; i++) 
                {
	            if(files[i].isDirectory()) 
	            {
	                deleteDirectory(files[i]);
	            }
	            else 
	            {
	            	try
	            	{
	                    files[i].delete();
	            	}
	            	catch(Exception ne)
	            	{
	            		Log.d(TAG, "delete file fail="+files[i].getAbsolutePath());
	            	}
	            }
	        }
	    }
	    return( path.delete() );
    }
   
    public static void deleteFiles(File path) 
    {
       if( path.exists() ) 
	   {
	       File[] files = path.listFiles();
	       for(int i=0; i<files.length; i++) 
	       {
	           if(files[i].isDirectory()) 
	           {
	               try
	               {
	                   deleteDirectory(files[i]);
	               }
	               catch(Exception ne)
	               {
	            		Log.d(TAG, "delete file fail="+files[i].getAbsolutePath());
	               }
	           }
	           else 
	           {
	               try
	               {
	                   files[i].delete();
	               }
	               catch(Exception ne)
	               {
	            		Log.d(TAG, "delete file fail="+files[i].getAbsolutePath());
	               }
	           }
	       }
	   }     
    }
	
	public static String isImageExistInPhone(String url, boolean addHostAndPath)
    {   
         String  localpath = null;
         try
         {
             URL  imageurl = new URL(url);
             String filepath = getImageFilePath(imageurl, addHostAndPath);
              
             File file = new File(filepath);
             if(file.exists() == true && file.length() >0)
             {
                 localpath = filepath;
                 if(SNSService.DEBUG)
                 Log.d(TAG, "true   isImageExistInPhone   url="+url +" file="+filepath);
             }
         }catch(java.net.MalformedURLException ne){
             Log.d(TAG, "isImageExistInPhone exception="+ne.getMessage() + " url="+url);
         }
         return localpath;
	    
    }
	
	public static class myHostnameVerifier extends AbstractVerifier 
	{	        
        public myHostnameVerifier() 
        {
        	
        }
        public final void verify(final String host, final String[] cns, final String[] subjectAlts)
        {
        	Log.d(TAG, "host ="+host);
        }
	}
	
	public static String getImagePathFromURL_noFetch(String url)
	{
		try{
			 URL  imageurl = new URL(url);
	         String filename = getImageFileName(imageurl.getFile());
	         String filepath = tmpPath + new File(filename).getName();		
	         return filepath;
		}catch(MalformedURLException ne){}
		return "";
	}
	
	public static String getImagePathFromURL(Context con, String url, boolean addHostAndPath)
	{
		if(url == null || url.length() == 0)
	        return null;
			
        try
        {
            URL  imageurl = new URL(url);
            String filepath = getImageFilePath(imageurl, addHostAndPath);
            
            Log.d(TAG, "getImagePathFromURL \n   url="+url +" file="+filepath);
            
            File file = new File(filepath);
            if(file.exists() == false || file.length() ==0)
            {
            	if(file.exists() == true)
            	{
            	    try{
                    file.delete();
            	    }catch(Exception ne){}
                    Log.d(TAG, "remove invalid files="+filepath);
            	}
                //check the available space
                if(stat != null)
        	    {
        			int blockSize = stat.getBlockSize();
        			int availableBlocks = stat.getAvailableBlocks();	    	       
        			if (blockSize * ((long) availableBlocks - 4) <= remainSize) 
        			{
        				Log.d(TAG, "no enought space to save the image");
        				return null;
        			}
        	    }
                
                //get bitmap
                HttpURLConnection conn = null;
                try
                {
                	conn = (HttpURLConnection)imageurl.openConnection();
                    
                    if(HttpsURLConnection.class.isInstance(conn) )
       			    {
       				     myHostnameVerifier passv = new myHostnameVerifier();
       				     //((HttpsURLConnection)conn).setHostnameVerifier(passv);
       				     ((HttpsURLConnection)conn).setHostnameVerifier(new org.apache.http.conn.ssl.AllowAllHostnameVerifier());
       			    }
                    
                    conn.setConnectTimeout(15*1000);
                    conn.setReadTimeout(30*1000);
                    InputStream in = conn.getInputStream();
                    
                    File filep = createTempPackageFile(con, filepath);
                    filepath = filep.getAbsolutePath();
                    FileOutputStream fos = new FileOutputStream(filep);
                    
                    int len = -1;
                    byte []buf = new byte[1024*4];
                    while((len = in.read(buf, 0, 1024*4)) > 0)
                    {
                    	fos.write(buf, 0, len);
                    }
                    buf = null;
                    fos.close();        			
        			
                    Log.d(TAG, "save url="+url +" as file="+filepath);
                }catch(IOException ne)
                {
                    Log.d(TAG, "fail to get image="+ne.getMessage());                    
                    file.delete();
                    return null;
                }
                finally
                {
                    if(conn != null)
                    {
                        conn.disconnect();
                    }
                }                       
            }
            return filepath;
        }catch(java.net.MalformedURLException ne)
        {
            Log.e(TAG, "getImageFromURL url="+url+" exception="+ne.getMessage());
            return null;
        }
	}
	private static String getImageFileName(String filename)
	{
         if(filename.contains("=") || filename.contains("?") || filename.contains("&") ||filename.contains("%"))
         {
        	 filename = filename.replace("?", "");
        	 filename = filename.replace("=", "");
        	 filename = filename.replace("&", "");
        	 filename = filename.replace("%", "");
         }
         
         return filename;
	}
	/*
	 * photos-a.ak.fbcdn.net
	 * api.facebook.com
	 * secure-profile.facebook.com
	 * ssl.facebook.com
	 * www.facebook.com
     * x.facebook.com
     * api-video.facebook.com
     * developers.facebook.com
     * iphone.facebook.com
     * developer.facebook.com
     * m.facebook.com
     * s-static.ak.facebook.com
     * secure-profile.facebook.com
     * secure-media-sf2p.facebook.com
     * ssl.facebook.com
     * profile.ak.facebook.com
     * b.static.ak.facebook.com
     * 
     * photos-h.ak.fbcdn.net
     * photos-f.ak.fbcdn.net
	 */	
	private static boolean isInTrustHost(String host)
	{
	    if(host.contains(".fbcdn.net"))
	        return true;
	    
	    if(host.contains("secure-profile.facebook.com"))
	        return true;
	    
	    return false;
	}
	
	private static String getImageFilePath(URL imageurl, boolean addHostAndPath)
    {
	    if(addHostAndPath == false)
	    {
	        return tmpPath + new File(getImageFileName(imageurl.getFile())).getName();
	    }
	    else
	    {
             String filename = imageurl.getFile();
             filename = removeChar(filename);
             
             String host = imageurl.getHost();
             if(isInTrustHost(host) == false)
             {
             
                 filename = host + "_" + filename;                 
                 if(filename.contains("/"))
                 {
                     filename = filename.replace("/", "");
                 }
             }
             else
             {
                 Log.d(TAG, "***********   i am in trust="+host + " filename="+filename);
             }
             
             
             
             return tmpPath + new File(filename).getName();
	    }
    }
	
	private static  String removeChar(String filename)
	{
		if(filename.contains("=") || filename.contains("?") || filename.contains("&") ||filename.contains("%"))
        {
            filename = filename.replace("?", "");
            filename = filename.replace("=", "");
            filename = filename.replace("&", "");
            filename = filename.replace("%", "");
            filename = filename.replace(",", "");
            filename = filename.replace(".", "");
            filename = filename.replace("-", "");
           
        }
		return filename;
	}
		
	public static Bitmap getImageFromURL(Context con, String url, boolean isHighPriority, boolean addHostAndPath)
    {
        ImageCache cache = cachemanager.getCache(url);
        if(cache == null)
        {
            String filePath = getImagePathFromURL(con, url, addHostAndPath);
            if(filePath != null)
            {
                Bitmap tmp = BitmapFactory.decodeFile(filePath);
                cachemanager.addCache(url, tmp);
                return tmp;
            }
            else
                return null;
        }
        else
        {
            return cache.bmp;
        }
    }
    
	static File createTempPackageFile(Context con, String filePath) 
	{
		File tmpPackageFile;
		if(android.os.Environment.getExternalStorageDirectory() != null && 
		   filePath.startsWith(android.os.Environment.getExternalStorageDirectory().getAbsolutePath()) == true)
		{
			tmpPackageFile = new File(filePath);
			return tmpPackageFile;
		}
		
        int i = filePath.lastIndexOf("/");
        String tmpFileName;
        if(i != -1) 
        {
            tmpFileName = filePath.substring(i+1);
        } 
        else 
        {
            tmpFileName = filePath;
        }
        FileOutputStream fos;
        try 
        {
            fos=con.openFileOutput(tmpFileName, 1|2);
        } 
        catch (FileNotFoundException e1) 
        {
            Log.e(TAG, "Error opening file "+tmpFileName);
            return null;
        }
        try 
        {
            fos.close();
        } 
        catch (IOException e) 
        {
            Log.e(TAG, "Error opening file "+tmpFileName);
            return null;
        }
        tmpPackageFile=con.getFileStreamPath(tmpFileName);            
        return tmpPackageFile;
	}

}
