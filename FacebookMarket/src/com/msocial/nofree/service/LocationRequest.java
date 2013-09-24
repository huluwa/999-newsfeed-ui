package com.msocial.nofree.service;

import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class LocationRequest 
{	
	private static String TAG            = "LocationRequest";	
	public static Location mLastLocation = new Location("");	
	static int proSize=3; 
	static LocationListener[] locListener= new LocationListener[proSize];
	boolean            mValid  = false;
	static boolean activated   = false;
	static Object mActivateObj = new Object();
	FacebookLocationListener listener;
	
	final int mapHeight = 300;
	final int mapWidth  = 300;
	
	//global object	
	private static LocationRequest _instance = null;
	public static LocationRequest instance()
	{
		if(_instance == null)
			_instance = new LocationRequest();
		
		return _instance;
	}
	
	public void setLocationListener(FacebookLocationListener loc)
	{
		listener = loc;
	}
	private Context mContext;
	private Context mBackContext;
	public void registerContext(Context con)
	{
		if(mContext == null)
		{
			mContext = con;
			mBackContext = con;
		}
		else
		{
			mBackContext = con;
		}
	}
	
	public void unRegisterContext(Context con)
	{
		if(mContext == con)
		{
			mContext = null;
		}
		
		if(mBackContext == con)
		{
			mBackContext = null;
		}
	}
	
	
	private LocationRequest()
	{
		init();
	}	
	
	private void init()
	{
		locListener[0] = new LocationListener("agps"/*LocationManager.AGPS_PROVIDER*/);
	    locListener[1] = new LocationListener(LocationManager.GPS_PROVIDER);
	    locListener[2] = new LocationListener("network");
	}
	//activate to get location, when user select to activate
	public void activate(Context con)
	{
		if(con == null)
			return ;
		
		synchronized(mActivateObj)
		{
			if(activated == true)
			{
				Log.d(TAG, "I am activated now, why do you repeat it.");				
			}
			else
			{
				activated = true;
				registerLocationListener(con);
			}
		    
	    }		
	}
	
	//stop, when user select to stop
	public void deActivate(Context con)
	{
		synchronized(mActivateObj)
		{
			activated = false;		
			LocationManager mService = (LocationManager)con.getSystemService(Context.LOCATION_SERVICE);
			for(int i=0;i<proSize;i++)
			{
				try{
			        mService.removeUpdates(locListener[i]);
				}catch(Exception ne){
					Log.d(TAG, "removeUpdates  "+locListener[i].mProvider + " ne="+ne.getMessage());
				}
			}
			if(exitHandler != null)
			{
				Message msd = exitHandler.obtainMessage();
				msd.sendToTarget();
			}
		}
	}
	
	android.os.Looper myLoop;
	Handler           exitHandler;
	//
	//do it in a thread
	//
	private void registerLocationListener(final Context con)
	{
		//enable location		
	    new Thread(new Runnable()
	    {
	    	public void run()
	    	{	    		
	    	    Looper.prepare();	    	    
	    	    myLoop = Looper.getMainLooper();
	    	    exitHandler = new Handler(myLoop);
	    	    
			  	LocationManager mService = (LocationManager)con.getSystemService(Context.LOCATION_SERVICE);	  	
			    for(int i=0;i<proSize;i++)
			    {	
			    	Log.d(TAG, "before "+locListener[i].mProvider);
			    	try{
			    	mService.requestLocationUpdates(locListener[i].mProvider, 2*60, 500F, locListener[i]);
			    	}catch(Exception ne){
			    		Log.d(TAG, " requestLocationUpdates "+locListener[i].mProvider + " ne="+ne.getMessage());
			    	}
			    	Log.d(TAG, "after "+locListener[i].mProvider);
			    }
			    
			    Looper.loop();
			    Log.d(TAG, "exit looper for location request");
	    	}
	    }).start();
	}	
	
	public Address getLocationAddress(Location location)
	{	
	    if(location.getLatitude() != new Location("").getLatitude())
	    {
			Geocoder geo = new Geocoder(mContext);//, Locale.CHINA);
			Address address = null;
	        try
	        {
				List<Address>ads = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
				if(ads.size() != 1)
				{
					Log.d(TAG, "Fail to get geocoded address");
				}
				else
				{
					address = ads.get(0);
	              
	             }
	                
			}
	        catch(Exception e)
			{
				Log.d(TAG, "get Location exception "+e.getMessage());
			}
			return address;
	    }
	    
	    return null;
	}
	
	public String getMapsSearchString( Location location)
	{
		if(location == null || (location.getLatitude() == 0.0 && location.getLongitude() == 0.0))
			return "";
		
	    Uri mapUrl = Uri.parse(String.format("http://maps.google.com/maps"));
        Builder urlBuilder = mapUrl.buildUpon();
        Address address = getLocationAddress(location);      
        if(address!=null)
        {                       
            String sb = getAddressInfo(address) ;
            urlBuilder.appendQueryParameter("q", sb);
            urlBuilder.appendQueryParameter("ll", String.format("%f,%f", location.getLatitude(), location.getLongitude()));
        }
        else
        {        
            urlBuilder.appendQueryParameter("q", String.format("%f,%f", location.getLatitude(), location.getLongitude()));
        }
        String locationString =  String.format(" Search me at %1$s", urlBuilder.toString());
        return locationString;
	}
	
	public String getAddressInfo(Address address)
	{
		StringBuffer sb = new StringBuffer();
		String featureName = address.getFeatureName();
		if(sb != null)
		{
			sb.append("at "+featureName);
		}
		
		String on=address.getThoroughfare();
		if(on != null)
		{
			sb.append(" on "+on);
		}
		
		
		if (address.getLocality() != null) {
            sb.append(" in " + address.getLocality());
            if (address.getAdminArea() != null) {
                sb.append(", " + address.getAdminArea());
            }
        }
        
		return sb.toString();
	}
	
	public String getStaticMapurl(Location address){
        String wheremaps = "maps";
    	String mapurl = String.format("http://%5$s.google.com/staticmap?center=%1$s,%2$s&zoom=12&size=%3$sx%4$s&markers=%1$s,%2$s,blues&key=ABQIAAAAJ5KvEbXlxTlrI1vXZNA9kRSMVUGCF5-vvbtM1Uo2l2QUBYJ1ahQgkjSC6mTJPj25VNFp12mORfXnpQ&sensor=true", address.getLatitude(), address.getLongitude(), mapWidth, mapHeight, wheremaps);
	    return mapurl;
	}	
	
	private class LocationListener implements android.location.LocationListener
	{		
	    String mProvider;
	    public LocationListener(String provider) 
	    {
	        mProvider = provider;
	    }

	    public void onLocationChanged(Location newLocation) 
	    {
	        if (newLocation.getLatitude() == 0.0 && newLocation.getLongitude() == 0.0) {
	            // Hack to filter out 0.0,0.0 locations
	            return;
	        }
	        mLastLocation.set(newLocation);
	        mValid = true;
	        listener.updateLocation(mLastLocation);
	        Log.d(TAG, "get location="+newLocation);            
	    }

	    public void onProviderEnabled(String provider)
	    {
	    	Log.e(TAG, "provider="+provider + " is enable.");
	    }

	    public void onProviderDisabled(String provider) 
	    {
	        mValid = false;
	        Log.e(TAG, "provider="+provider + " is disable.");
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) 
	    {
	        if (status == LocationProvider.OUT_OF_SERVICE) {
	            mValid = false;
	            Log.e(TAG, "out of service");    
	        }
	        Log.d(TAG, "onStatusChanged");
	    }

	    public Location current() 
	    {
	        return mValid ? mLastLocation : null;
	    }
	}

	public Location getCurrentLastLocation() {		
		return mLastLocation;
	};
	
	public interface FacebookLocationListener{
		public void updateLocation(Location loc);
	}
}


