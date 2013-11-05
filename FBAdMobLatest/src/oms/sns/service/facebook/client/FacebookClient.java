package oms.sns.service.facebook.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.msocial.freefb.service.SNSService;
import com.msocial.freefb.ui.ActivityBase;
import com.msocial.freefb.ui.FacebookBaseActivity;
import com.msocial.freefb.ui.NetworkConnectionListener;
import oms.sns.service.facebook.client.FacebookMethod.BatchRun;
import oms.sns.service.facebook.client.FacebookMethod.OpenStream;
import oms.sns.service.facebook.client.xml.XmlFacebookMethod;
import oms.sns.service.facebook.client.xml.XmlFacebookParser;
import oms.sns.service.facebook.util.ArrayUtils;
import oms.sns.service.facebook.util.EncryptionUtils;
import oms.sns.service.facebook.util.StringUtils;

import org.apache.http.client.HttpClient;

import android.content.ContentUris;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.json.*;

import org.apache.http.entity.FileEntity;


public class FacebookClient {
	private static final String USER_AGENT="oms";
	//private HttpClient   httpClient;
	private String       apiKey;
	private String       secretKey;
	private String       sessionKey;
	private boolean      batchMode = false;
	private List<BatchQuery>        queries;
	
	public FacebookClient(String apiKey, String secretKey)
	{
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		
		/*this.httpClient = AndroidHttpClient.newInstance(USER_AGENT);*/
	}
	
	NetworkConnectionListener connectionListener;
	public void attachActivity(NetworkConnectionListener baseAc) 
	{        
		connectionListener = baseAc;
	}

	
	public FacebookClient(String apiKey, String secretKey, String sessionKey)
	{
		this(apiKey, secretKey);
		this.sessionKey = sessionKey;
	}
	public void dispose()
	{
		
	}
	public String getSessionKey() {
		// TODO Auto-generated method stub
		return this.sessionKey;
	}
	
	 public void beginBatch() {
			this.batchMode = true;
			this.queries = new ArrayList();
	}
	
	 
	 private String encodeMethods( List<BatchQuery> queries ) throws FacebookException {
			JSONArray result = new JSONArray();
			for ( BatchQuery query : queries ) {
			  // Log.d("FacebookClient", "==== queries params is "+query.getMethod().methodName);
			   
				if ( query.getMethod().methodName.indexOf("upload")>0 ) {
					throw new FacebookException("File upload API calls cannot be batched:  " + query.getMethod().methodName );
				}
			   // Log.d("FacebookClient", "==== query paramap is "+query.getParams().size());
				result.put( delimit( query.getParams(), "&", "=", true ) );
			}
			if(SNSService.DEBUG)
			Log.d("FacebookClient", "====json string "+result.toString());
			return result.toString();
	}
	 
	 protected static String delimit(Map<String,String> paramap, CharSequence delimiter, CharSequence equals, boolean doEncode ) {
			if ( paramap == null || paramap.isEmpty() ) {
				return null;
			}
			StringBuilder buffer = new StringBuilder();
			boolean notFirst = false;
			Set<Entry<String,String>> entries = paramap.entrySet();
			Iterator it = entries.iterator();
			while(it.hasNext()){
				Entry<String,String> entry = (Entry<String,String>)it.next();
				if(notFirst){
					buffer.append(delimiter);
				}else{
					notFirst = true;
				}
				String key = entry.getKey();
				String value = entry.getValue();
				buffer.append( entry.getKey() ).append( equals ).append( doEncode ? encode( value ) : value );
			}
			if(SNSService.DEBUG)
		    Log.d("FacebookClient", "===feedmethod is =="+buffer.toString());
			return buffer.toString();
	}
	 
   private static String encode( CharSequence target ) {
			String result = ( target != null ) ? target.toString() : "";
			try {
				result = URLEncoder.encode( result, "UTF-8" );
			}
			catch ( UnsupportedEncodingException e ) {
				System.err.println( "Unsuccessful attempt to encode '" + result + "' into UTF-8" );
			}
			return result;
  }
	 
	 public List<? extends Object> executeBatch( boolean serial ) throws FacebookException{
			this.batchMode = false;
			List<Object> result = new ArrayList<Object>();
			List<BatchQuery> buffer = new ArrayList<BatchQuery>();
			while ( !this.queries.isEmpty() ) {
				buffer.add( this.queries.remove( 0 ) );
				if ( ( buffer.size() ==15 ) || ( this.queries.isEmpty() ) ) {
					// we can only actually batch up to 15 at once
					if(SNSService.DEBUG)
					Log.d("FacebookClient","=== buffer size "+buffer.size());
					
					Document doc = batch_run( this.encodeMethods( buffer ), serial );
					if(doc == null)
						throw new FacebookException("No response");
					
					NodeList responses = doc.getElementsByTagName( "batch_run_response_elt" );
					for ( int count = 0; count < responses.getLength(); count++ ) {
					    Node node = responses.item(count);	
						try {
						    if(node.getNodeType() == Element.ELEMENT_NODE)
						    {
						      String response = XmlFacebookParser.getChildText("batch_run_response_elt", (Element)node);
							  DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
							  Document respDoc = builder.parse( new ByteArrayInputStream( response.getBytes( "UTF-8" ) ) );
							  // possible types are document, string, bool, int, long, void					
							  result.add( respDoc );	
						    }
						    else
						    {
						        result.add(null);
						    }
						}
						catch ( Exception ignored ) {
							if ( result.size() < count + 1 ) {
								result.add( null );
							}
						}
					}
				}
			}
			return result;
	}
	   
	 public Document batch_run( String methods, boolean serial ) throws FacebookException{
			Map<String,String> params = new HashMap<String,String>();
			params.put("method_feed", methods);
			/*if ( serial ) {
				params.put( "serial_only", "1" );
			}*/
			return callMethod( new XmlFacebookMethod(BatchRun.BATCH_RUN), params);
	}
	   
	public <T>T callMethod(FacebookMethod<T>method, Map<String, String>params) throws FacebookException 
	{
		 RestPostMethod post = new RestPostMethod( Server.REST_URL, USER_AGENT );
		 post.attachActivity(connectionListener);
		 
		 Map<String, String> preparedParams = prepareParams( method, params );

		 signParameters( preparedParams );	
		 
		 if(this.batchMode){
			 boolean addToBatch = true;
			 if(SNSService.DEBUG)
			 Log.d("FacebookClient", "==batch =="+method.methodName);
				/*if ( method.methodName.equals( FacebookMethod.USERS_GET_LOGGED_IN_USER.methodName() ) ) {
					Exception trace = new Exception();feedmethod
					StackTraceElement[] traceElems = trace.getStackTrace();
					int index = 0;
					for ( StackTraceElement elem : traceElems ) {
						if ( elem.getMethodName().indexOf( "_" ) != -1 ) {
							StackTraceElement caller = traceElems[index + 1];
							if ( ( caller.getClassName().equals( this.getClass().getName() ) ) && ( !caller.getMethodName().startsWith( "auth_" ) ) ) {
								addToBatch = false;
							}
							break;
						}
						index++ ;
					}
				}*/
				if ( addToBatch ) {
					this.queries.add( new BatchQuery(method, preparedParams) );
				}
				
				return null;
		 }
		 
         String response = post.execute(preparedParams, method );
         T t = null;	
	     try 
	     {
		 	t = method.parseResponse( response );
		 } 
	     catch(NoExtPermissionException e)
	     {
	    	 if(connectionListener != null)
	    	 {
	    	     if(ActivityBase.class.isInstance(connectionListener))
	    		 {
	    		     ((ActivityBase)connectionListener).ProcessNoExtPermission(e.getExtPermisson());
	    		 }
	    	 }
			 throw new FacebookException(e.getMessage(), e.getErrorCode());
	     }
	     catch (InvalidSesssionException e) 
	     {			 
	    	 if( connectionListener != null && ActivityBase.class.isInstance(connectionListener))
    		 {
    		     ((ActivityBase)connectionListener).ProcessInvaidSession();
	    	 }
			 throw new FacebookException(e.getMessage(), e.getErrorCode());
		 }	 
	     catch(FacebookPermissionErrorException e)
        {
            if( connectionListener != null && ActivityBase.class.isInstance(connectionListener))
            {
                ((ActivityBase)connectionListener).ProcessPermissionError(e.getMessage());
            }
            throw new FacebookException(e.getMessage(), e.getErrorCode());
        }
	     
	     return t;
					
	}

	
	 /*
	* (non-Javadoc)
	* 
	* @see com.belasius.fb4j.client.DeleteMe#callMethod(java.lang.String,
	*      java.lang.String, java.lang.Object)
	*/
	public <T> T callMethod( FacebookMethod<T> method, Object... params ) throws FacebookException
	{
		Map<String, String> paramsMap = toMap( params );

		T t = null;
		t = callMethod( method, paramsMap );	
	
		return t;
	}

	/*
	* (non-Javadoc)
	* 
	* @see com.belasius.fb4j.client.DeleteMe#callSecureMethod(java.lang.String,
	*      java.lang.String, java.util.Map)
	*/
	public <T> T callSecureMethod( FacebookMethod<T> method, Map<String, String> params ) throws FacebookException
	{
		try
		{
			RestPostMethod post;
			String methodname =method.getMethodName(); 
			if(methodname.equalsIgnoreCase(FacebookMethod.Auth.GET_SESSION))
			{
			    post = new RestPostMethod( Server.SECURE_REST_URL, USER_AGENT );
			}
			else if(methodname.equalsIgnoreCase(FacebookMethod.Auth.LOGIN) || methodname.equalsIgnoreCase(FacebookMethod.Auth.LOGIN_NOSESSION))
			{
				post = new RestPostMethod( Server.SECURE_REST_URL, USER_AGENT );
			}
			else
			{
				post = new RestPostMethod( Server.REST_URL, USER_AGENT );
			}
		
			post.attachActivity(connectionListener);
			Map<String, String> preparedParams = prepareParams( method, params );
		
			signParameters( preparedParams );
		
			//post.addParameters( preparedParams );
		
			String response = post.execute(preparedParams, method);
		
			return method.parseResponse( response );
		}
		catch ( FacebookException e )
		{
			throw e;
		}
		catch(NoExtPermissionException e)
	    {
			if(connectionListener != null && ActivityBase.class.isInstance(connectionListener))
   		    {
   		         ((ActivityBase)connectionListener).ProcessNoExtPermission(e.getExtPermisson());
	    	}
			throw new FacebookException(e.getMessage(), e.getErrorCode());
	    }
		catch(InvalidSesssionException e)
		{
			if(connectionListener != null && ActivityBase.class.isInstance(connectionListener))
   		    {
   		        ((ActivityBase)connectionListener).ProcessInvaidSession();
			}
			
			throw new FacebookException(e.getMessage(), e.getErrorCode());
		}
		catch(FacebookPermissionErrorException e)
        {
            if( connectionListener != null && ActivityBase.class.isInstance(connectionListener))
            {
                ((ActivityBase)connectionListener).ProcessPermissionError(e.getMessage());
            }
            throw new FacebookException(e.getMessage(), e.getErrorCode());
        }
	}

	/*
	* (non-Javadoc)
	* 
	* @see com.belasius.fb4j.client.DeleteMe#callSecureMethod(java.lang.String,
	*      java.lang.String, java.lang.Object)
	*/
	public <T> T callSecureMethod( FacebookMethod<T> method, Object... params ) throws FacebookException
	{
		Map<String, String> paramsMap = toMap( params );
		return callSecureMethod( method, paramsMap );
	}

	/**
	    * 
	    * @param <T>
	    * @param method
	    * @param files
	    * @param params
	    * @return
	     * @throws InvalidSesssionException 
	    * @throws FacebookClientException
	     * @throws HttpException 
	    */
	    public <T> T callUploadMethod( FacebookMethod<T> method,String filepath, Map<String, String> params ) throws FacebookException
	    {       
	        RestPostMethod post = new RestPostMethod( Server.REST_URL, USER_AGENT );
	        if(method.methodName.equals(FacebookMethod.Video.VIDEO_UPLOAD)){
	            Log.d("facebookclient", "video upload server "+Server.VIDEO_URL);
	            post = new RestPostMethod(Server.VIDEO_URL,USER_AGENT);
	            
	        }
	        post.attachActivity(connectionListener);
	        
	        Map<String, String> preparedParams = prepareParams( method, params );
	    
	        signParameters( preparedParams );
	        String response = "";
	        try
	        {
	            File file = new File(filepath);
                String mimeType = getMimeType(file,method.methodName);
                Log.d("upload video/image","mimeType is="+mimeType);
                InputStream fileStream = new FileInputStream(file);
                response= post.postRequest(post.getURI().toURL(), preparedParams,file.getName(),mimeType, fileStream);
                
                Log.d("uplod video/image","response is="+response);
	        }catch(Exception e)
	        {
	            Log.d("callUploadMethod", e.getMessage());
	        }
	        
	        T t= null;
	        try 
	        {
	            t = method.parseResponse( response );
	        }
	        catch(NoExtPermissionException e)
	        {    
	            if(connectionListener != null && ActivityBase.class.isInstance(connectionListener))
	            {
	                 ((ActivityBase)connectionListener).ProcessNoExtPermission(e.getExtPermisson());
	            }
	            throw new FacebookException(e.getMessage(), e.getErrorCode());
	        }
	        catch (InvalidSesssionException e) 
	        {
	            if(connectionListener != null && ActivityBase.class.isInstance(connectionListener))
	            {
	                 ((ActivityBase)connectionListener).ProcessInvaidSession();
	            }
	            
	            throw new FacebookException(e.getMessage(), e.getErrorCode());
	        } 
	        catch(FacebookPermissionErrorException e)
	        {
	            if( connectionListener != null && ActivityBase.class.isInstance(connectionListener))
	            {
	                ((ActivityBase)connectionListener).ProcessPermissionError(e.getMessage());
	            }
	            throw new FacebookException(e.getMessage(), e.getErrorCode());
	        }
	        return t;
	    }
   
	final String[] video_type = {"3g2","3gp","3gpp","asf","avi","dat","flv","m4v","mkv","mod","mov",
	                              "mp4","mpe","mpeg","mpeg4","mpg","nsv","ogm","ogv","qt","tod","vob","wmv"};	

    private String getMimeType(File file, String methodname) 
    {
        String mimeType = "";
        String extension = "";
        String filepath = file.getPath();
        extension = android.webkit.MimeTypeMap.getSingleton().getFileExtensionFromUrl("file://"+filepath);
        mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if(mimeType== null || mimeType.length() == 0)
        {
            if(methodname.equals(FacebookMethod.Video.VIDEO_UPLOAD))
            {
                mimeType = "image";
            }
            else if(methodname.equals(FacebookMethod.Photos.UPLOAD))
            {
                mimeType = "video";
            }
        }
        
        return mimeType;
    }


    /**
	* 
	* @param <T>
	* @param method
	* @param files
	* @param params
	* @return
	 * @throws InvalidSesssionException 
	* @throws FacebookClientException
	 * @throws HttpException 
	*/
	public <T> T callUploadMethod( FacebookMethod<T> method, Map<String, String> files, Map<String, String> params ) throws FacebookException
	{		
		RestPostMethod post = new RestPostMethod( Server.REST_URL, USER_AGENT );
		if(method.methodName.equals(FacebookMethod.Video.VIDEO_UPLOAD)){
			Log.d("facebookclient", "video upload server "+Server.VIDEO_URL);
			post = new RestPostMethod(Server.VIDEO_URL,USER_AGENT);
			
		}
		post.attachActivity(connectionListener);
		
		Map<String, String> preparedParams = prepareParams( method, params );
	
		signParameters( preparedParams );
	
		post.setMultipart( preparedParams, files );
		String response="";
		AndroidHttpClient httpClient=null;
		try{
			httpClient = AndroidHttpClient.newInstance(USER_AGENT);
			response = post.execute( httpClient );	
			if(SNSService.DEBUG)
			Log.d("facebookclient", response);
			
			httpClient.close();
		}catch(IOException ne)
		{
			throw new FacebookException(ne);
		}
		catch(HttpException ne)
		{
			throw new FacebookException(ne);
		}
		finally
		{
			if(httpClient != null)
			{
				try{
				    httpClient.close();
				}catch(Exception ne){}
			}
		}
		
		T t= null;
		try 
		{
			t = method.parseResponse( response );
		}
		catch(NoExtPermissionException e)
	    {	 
			if(connectionListener != null && ActivityBase.class.isInstance(connectionListener))
   		    {
   		         ((ActivityBase)connectionListener).ProcessNoExtPermission(e.getExtPermisson());
	    	}
			throw new FacebookException(e.getMessage(), e.getErrorCode());
	    }
		catch (InvalidSesssionException e) 
		{
			if(connectionListener != null && ActivityBase.class.isInstance(connectionListener))
   		    {
   		         ((ActivityBase)connectionListener).ProcessInvaidSession();
			}
			
			throw new FacebookException(e.getMessage(), e.getErrorCode());
		} 
		catch(FacebookPermissionErrorException e)
		{
		    if( connectionListener != null && ActivityBase.class.isInstance(connectionListener))
		    {
		        ((ActivityBase)connectionListener).ProcessPermissionError(e.getMessage());
	        }
		    throw new FacebookException(e.getMessage(), e.getErrorCode());
		}
		return t;
	}

	/**
	* 
	* @param <T>
	* @param method
	* @param files
	* @param params
	* @return
	 * @throws InvalidSesssionException 
	* @throws FacebookClientException
	 * @throws HttpException 
	*/
	public <T> T callUploadMethod( FacebookMethod<T> method, Map<String, String> files, Object... params ) throws FacebookException
	{
		Map<String, String> paramsMap = toMap( params );
		
		return callUploadMethod( method, files, paramsMap );
	}
	
	static Map<String, String> toMap( Object... params )
	{
		if ( ( params.length % 2 ) != 0 )
		{
		    throw new IllegalArgumentException( "Odd number of parameters not allowed!" );
		}
		Map<String, String> map = new HashMap<String, String>( params.length / 2 );
	
		for ( int i = 0; i < params.length; )
		{
			String key = (String) params[i++];
			Object value = params[i++];
			String keyStr = (String) key;
			String valueStr;
		
			if ( value instanceof String )
			{
				valueStr = (String) value;
			}
			else if ( value == null )
			{
				valueStr = "";
			}
			else if ( value instanceof long[] )
			{
				valueStr = ArrayUtils.join( (long[]) value );
			}
			else if ( value instanceof String[] )
			{
				valueStr = StringUtils.join( (String[]) value );
			}
			else
			{
				valueStr = value.toString();
			}
		
			map.put( keyStr, valueStr );
		}
		return map;
	}

	protected Map<String, String> prepareParams( FacebookMethod<?> method, Map<String, String> params )
	{
		Map<String, String> p = new HashMap<String, String>();
	
		if ( params != null )
		{
		p.putAll( params );
		}
	
		p.put( RestParameters.API_KEY, apiKey );
		p.put( RestParameters.API_VERSION, Server.API_VERSION );
		p.put( RestParameters.METHOD, method.getMethodName() );
		p.put( RestParameters.FORMAT, method.getFormat() );
	
		if ( sessionKey != null )
		{
		p.put( RestParameters.SESSION_KEY, sessionKey );
		p.put( RestParameters.CALL_ID, Long.toString( System.currentTimeMillis() ) );
		}
	
		return p;
	}

	private final void signParameters( Map<String, String> params )
	{
		String sig = generateSignature( params, secretKey );	
		params.put( "sig", sig );
	}

	/**
	* @param params
	*            without signature
	* @return
	*/
	public static final String generateSignature( Map<String, String> params, String secretKey )
	{
		// Prepare parameters
		StringBuffer sb = new StringBuffer();
		List<String> keys = new ArrayList<String>( params.keySet() );
		Collections.sort( keys );
		for ( String key : keys )
		{
		String value = params.get( key );
		sb.append( key );
		sb.append( "=" );
		sb.append( value );
	
		}
		sb.append( secretKey );
		String p = sb.toString();
	
		// Encode and return
		return EncryptionUtils.md5Encode( p );
	}

	public static final class Format
	{
		public static final String JSON = "JSON";
		public static final String XML = "XML";
	}

	public static boolean isUsingSecurity()
	{
		return SNSService.isUsingSecurity();
	}
	public static final class Server
	{
		public static  String REST_URL;
		public static  String VIDEO_URL;
		public static  String SECURE_REST_URL;
		public final   static  String SECURE_REST_AUTH_URL = "https://m.facebook.com/auth.php";
		public static  String API_VERSION ;
		public static  String LOGIN_URL ;
		public static  String PERM_URL;
		public static  String SECURE_LOGIN_URL;
		public static  String LOGOUT_URL ;
		public static  String INSTALL_URL;
		public static  String PERM_OK ;
		public static  String PERM_cancel ;
		
		static 
		{
			if(isUsingSecurity())
			{
				setSecureURL();
			}
			else
			{
				setNormalURL();
			}
		}
		
		synchronized public static void retSetURL(boolean isSecure)
		{
		    if(isSecure)
		    {
		        setSecureURL();
		    }
		    else
		    {
		        setNormalURL();
		    }
		}
		
		static void setNormalURL()
		{
		    REST_URL         = ServerNormal.REST_URL;
            VIDEO_URL        = ServerNormal.VIDEO_URL;
            SECURE_REST_URL  = ServerNormal.SECURE_REST_URL;
            API_VERSION      = ServerNormal.API_VERSION;
            LOGIN_URL        = ServerNormal.LOGIN_URL;
            PERM_URL         = ServerNormal.PERM_URL;
            SECURE_LOGIN_URL = ServerNormal.SECURE_LOGIN_URL;
            LOGOUT_URL       = ServerNormal.LOGOUT_URL;
            INSTALL_URL      = ServerNormal.INSTALL_URL;
            PERM_OK          = ServerNormal.PERM_OK;
            PERM_cancel      = ServerNormal.PERM_cancel;
		}
		
		static void setSecureURL()
		{
		    REST_URL         = SecureServer.REST_URL;
            VIDEO_URL        = SecureServer.VIDEO_URL;
            SECURE_REST_URL  = SecureServer.SECURE_REST_URL;
            API_VERSION      = SecureServer.API_VERSION;
            LOGIN_URL        = SecureServer.LOGIN_URL;
            PERM_URL         = SecureServer.PERM_URL;
            SECURE_LOGIN_URL = SecureServer.SECURE_LOGIN_URL;
            LOGOUT_URL       = SecureServer.LOGOUT_URL;
            INSTALL_URL      = SecureServer.INSTALL_URL;
            PERM_OK          = SecureServer.PERM_OK;
            PERM_cancel      = SecureServer.PERM_cancel;
		}
	}
	
	public static final class ServerNormal
	{
		public static final String REST_URL = "http://api.facebook.com/restserver.php";
		public static final String VIDEO_URL = "http://api-video.facebook.com/restserver.php";
		public static final String SECURE_REST_URL = "https://api.facebook.com/restserver.php";
		public static final String SECURE_REST_AUTH_URL = "https://m.facebook.com/auth.php";
		public static final String API_VERSION = "1.0";
		public static final String LOGIN_URL = "http://www.facebook.com/login.php";
		public static final String PERM_URL = "http://m.facebook.com/authorize.php";
		public static final String SECURE_LOGIN_URL = "https://login.facebook.com/login.php";
		public static final String LOGOUT_URL = "http://www.facebook.com/logout.php";
		public static final String INSTALL_URL = "http://www.facebook.com/install.php";
		public static final String PERM_OK = "http://www.facebook.com";
		public static final String PERM_cancel = "http://www.facebook.com";
	}
	public static final class SecureServer
	{
		public static final String REST_URL = "https://api.facebook.com/restserver.php";
		public static final String VIDEO_URL = "https://api-video.facebook.com/restserver.php";
		public static final String SECURE_REST_URL = "https://api.facebook.com/restserver.php";
		public static final String SECURE_REST_AUTH_URL = "https://m.facebook.com/auth.php";
		public static final String API_VERSION = "1.0";
		public static final String LOGIN_URL = "https://www.facebook.com/login.php";
		public static final String PERM_URL = "https://m.facebook.com/authorize.php";
		public static final String SECURE_LOGIN_URL = "https://login.facebook.com/login.php";
		public static final String LOGOUT_URL = "https://www.facebook.com/logout.php";
		public static final String INSTALL_URL = "https://www.facebook.com/install.php";
		public static final String PERM_OK = "https://www.facebook.com";
		public static final String PERM_cancel = "https://www.facebook.com";
	}

	// FB REST Parameters
	public static final class RestParameters
	{
		public static final String API_KEY = "api_key";
		public static final String API_VERSION = "v";
		public static final String FORMAT = "format";
		public static final String METHOD = "method";
		public static final String SESSION_KEY = "session_key";
		public static final String CALL_ID = "call_id";
		public static final String SECRET_KEY = "secret_key";		
	};

	public static final class LoginParameters
	{
		public static final String API_KEY = "api_key";
		public static final String API_VERSION = "v";
		public static final String NEXT_URL = "next";
		public static final String NEXT_Cancel = "next_cancel";
		public static final String AUTH_TOKEN = "auth_token";
		public static final String POPUP = "popup";
		public static final String SKIP_COOKIE = "skipcookie";
		public static final String HIDE_CHECKBOX = "hide_checkbox";
		public static final String CANVAS = "canvas";
		public static final String EMAIL    = "email";
		public static final String PASSWORD = "password";
	}

	public static final class InstallParameters
	{
	    public static final String API_KEY = "api_key";
	    public static final String NEXT_URI = "next";
	}
	
	/** batch run sample code*/
	/* Log.d(TAG, "Entering batch run ");
	  FacebookSession session = loginHelper.getPermanentSesstion();
	  FacebookClient client = session.getFbClient();
	  client.beginBatch();
	  session.getEvents();
	  session.getFriendIds();
	  client.executeBatch(true);*/
	
}
