/*
 *  fb4j: Java API for Facebook
 *  Copyright (C) 2007-2008 Biagio Miceli Jr, Cosimo Togna
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Full license may be found in LICENSE.txt or downloaded from 
 *  <http://www.gnu.org/licenses/>.  fb4j documentation, updates and other 
 *  info can be found at <http://fb4j.sourceforge.net/>
 *
 *  @version $Id$
 */
package oms.sns.service.facebook.client;

import com.ast.free.R;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.Authenticator.RequestorType;
import java.net.Proxy.Type;
import java.security.KeyStore;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.ast.free.service.SNSService;
import com.ast.free.ui.NetworkConnectionListener;
import oms.sns.service.facebook.client.FacebookClient.Server;
import oms.sns.service.facebook.client.FacebookMethod.Phonebook;

import org.apache.http.client.HttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;

import twitter4j.threadpool.QueuedThreadPool.PoolThread;

import com.android.internal.http.multipart.FilePart;
import com.android.internal.http.multipart.MultipartEntity;
import com.android.internal.http.multipart.Part;
import com.android.internal.http.multipart.StringPart;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Log;

/**
 * 
 * @author Gino Miceli
 */
public class RestPostMethod extends HttpPost
{
	private final int UNAUTHORIZED = 401;
	private final int FORBIDDEN = 403;
	    
	private final boolean DEBUG=SNSService.DEBUG;
	
	final static String TAG = "RestPostMethod";
	public RestPostMethod( String url, String userAgent )
	{
		super( url );
		this.setHeader("User-Agent", userAgent );
		
		//setFollowRedirects( false );		
	}

	NetworkConnectionListener connectionListener;
	
	private static boolean isSetProxy;
	private static String proxyHost;
	private static int    proxyPort;
	private static String proxyAuthUser;
	private static String proxyAuthPassword;
	
	public static void setProxy(boolean enable, String host, int port, String user, String pwd)
    {
    	isSetProxy        = enable;
    	proxyHost         = host;
    	proxyPort         = port;
    	proxyAuthUser     = user;
    	proxyAuthPassword = pwd;           
    }
	
	private boolean isJDK14orEarlier = false;
	private int connectionTimeout=10000;
	private int readTimeout=30000;
	
	public void attachActivity(NetworkConnectionListener baseAc) 
	{        
		connectionListener = baseAc;
	}
	
	public void addParameters( Map<String, String> params )
	{
		for ( String name : params.keySet() )
		{
			String value = params.get( name );
			this.addHeader(name, value );
		}
	}

	public void addParameters( String... params )
	{
		if ( params.length % 2 != 0 )
		{
			throw new IllegalArgumentException( "Invalid number of parameters; each name must have a corresponding value!" );
		}

		for ( int i = 0; i < params.length; )
		{
			String name = params[i++];
			String value = params[i++];
			addHeader( name, value );
		}
	}

	public void setMultipart( Map<String, String> params, FilePart[] files )
	{
		Part[] parts = new Part[ params.size() + files.length ];
		int i = 0;
		for ( String name : params.keySet() )
		{
			String value = params.get( name );
			parts[i++] = new StringPart( name, value );
		}
		for ( FilePart file : files )
		{
			parts[i++] = file;
		}

		this.setEntity( new MultipartEntity( parts, getParams() ) );
	}
	
	protected static CharSequence delimit( Collection<Entry<String,String>> entries, CharSequence delimiter, CharSequence equals, boolean doEncode ) 
	{
		if ( entries == null || entries.isEmpty() )
		return null;

		StringBuilder buffer = new StringBuilder();
		boolean notFirst = false;
		for ( Map.Entry<String,String> entry : entries ) 
		{
			if ( notFirst )
				buffer.append( delimiter );
			else
				notFirst = true;
			
			CharSequence value = entry.getValue();
			buffer.append( entry.getKey() ).append( equals ).append( doEncode ? encode( value ) : value );
		}
		return buffer;
	}
	
	private static String encode( CharSequence target )
	{
		if ( target == null ) {
			return "";
		}
		String result = target.toString();
		try {
			result = URLEncoder.encode( result, "UTF8" );
		}
		catch ( UnsupportedEncodingException e ) {
			Log.e(TAG,  String.format("Unsuccessful attempt to encode '%$1s' into UTF8", result));
	     }
		return result;
	}


	public String execute2( AndroidHttpClient httpClient, Map<String, String> preparedParams  ) throws IOException, HttpException
	{
		CharSequence bufferss = ( null == preparedParams ) ? "" : delimit( preparedParams.entrySet(), "&", "=", true );
		this.setEntity(new StringEntity(bufferss.toString()));
		
		return execute(httpClient);
	}
	
	public class myHostnameVerifier extends AbstractVerifier 
	{	        
        public myHostnameVerifier() 
        {
        	
        }
        public final void verify(final String host, final String[] cns, final String[] subjectAlts)
        {
        	Log.d(TAG, "host ="+host);
        }
	}
	
	private static Integer UID=0;
    void releaseUID(int tempID)
    {
        if(tempID != -1)
        {
            if(connectionListener != null)
            	connectionListener.releaseHttpConnection(tempID);
        }
    }
    
    private static InputStream getKeyStoreFileName(Context con)
    {
    	return con.getResources().openRawResource(R.raw.facebook_keystore);
    }
    /**
     * Builds and returns the context used for secure socket creation.
     */
    final static String KS_PASSWORD="123456";
    static    SSLContext ctx;
    private static SSLContext getContext(Context con) throws Exception 
    {
    	if(ctx == null)
    	{
	        String type = KeyStore.getDefaultType();
	       
	        InputStream fis = getKeyStoreFileName(con);
	
	        KeyStore ks = KeyStore.getInstance(type);
	        ks.load(fis, KS_PASSWORD.toCharArray());
	
	        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
	                .getDefaultAlgorithm());
	        kmf.init(ks, KS_PASSWORD.toCharArray());
	
	        TrustManagerFactory tmf = TrustManagerFactory
	                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        tmf.init(ks);
	
	        ctx = SSLContext.getInstance("TLSv1");
	        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    	}
    	
    	SSLSocketFactory socketFactory = (SSLSocketFactory) ctx.getSocketFactory();
    	HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
    	
        return ctx;
    }
    
    private void log(String message) 
    {
        if (DEBUG) 
        {
            Log.d(TAG, "[" + new java.util.Date() + "]" + message);
        }
    }

    private HttpURLConnection getConnection(String url) throws IOException 
    {
        HttpURLConnection con = null;
        if (isSetProxy && proxyHost != null && !proxyHost.equals("")) 
        {
        	log("Proxy : " + proxyHost);
            if (proxyAuthUser != null && !proxyAuthUser.equals("")) 
            {
                log("Proxy AuthUser: " + proxyAuthUser);
                log("Proxy AuthPassword: " + proxyAuthPassword);
                Authenticator.setDefault(new Authenticator() 
                {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() 
                    {
                        //respond only to proxy auth requests
                        if (getRequestorType().equals(RequestorType.PROXY)) 
                        {
                            return new PasswordAuthentication( proxyAuthUser, proxyAuthPassword.toCharArray());
                        } 
                        else 
                        {
                            return null;
                        }
                    }
                });
            }
            final Proxy proxy = new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(proxyHost, proxyPort));         
            log("Opening proxied connection(" + proxyHost + ":" + proxyPort + ")");         
            con = (HttpURLConnection) new URL(url).openConnection(proxy);
        } 
        else 
        {
            con = (HttpURLConnection) new URL(url).openConnection();
        }
        if (connectionTimeout > 0 && !isJDK14orEarlier) 
        {
            con.setConnectTimeout(connectionTimeout);
        }
        if (readTimeout > 0 && !isJDK14orEarlier) 
        {
            con.setReadTimeout(readTimeout);
        }
        return con;
    }

	//TODO for desktop, need security URL
	//
	public String execute( Map<String, String> preparedParams , FacebookMethod<?> method) throws FacebookException	
	{
		 CharSequence bufferss = ( null == preparedParams ) ? "" : delimit( preparedParams.entrySet(), "&", "=", true );		 
		 URL serverUrl ;
		 try
		 {
		     serverUrl = this.getURI().toURL();
	     }catch(MalformedURLException ne)
	     {
	    	 throw new FacebookException(ne);
	     }
		 String xmlResp="";
		 
		 int responseCode =-1;
		 int tempID=-1; 
		 
		 HttpURLConnection conn = null;
         InputStream is = null;
         OutputStream osw = null;
         try 
         {	        	 
			 conn = getConnection(serverUrl.toString());
			 synchronized(UID)
	         {    
	             tempID = UID;
	             if(connectionListener != null)
	             {		            	 
	            	 connectionListener.addHttpConnection(UID, conn);
	             
		             Thread th = Thread.currentThread(); 
                     if(PoolThread.class.isInstance(th))
                     {
                     	PoolThread pt = (PoolThread)(th);
                     	Runnable run = pt.currentRunning();
                     	if(run != null && oms.sns.service.facebook.client.AsyncFacebook.AsyncTask.class.isInstance(run))
                     	{
                     		connectionListener.addRunnable(UID, run);
                     	}
                     }
	             }
	             UID++;
	         }
			 
			 if(HttpsURLConnection.class.isInstance(conn) )
			 {
				 myHostnameVerifier passv = new myHostnameVerifier();
				 //((HttpsURLConnection)conn).setHostnameVerifier(passv/*new org.apache.http.conn.ssl.AllowAllHostnameVerifier()*/);
				 ((HttpsURLConnection)conn).setHostnameVerifier(new org.apache.http.conn.ssl.AllowAllHostnameVerifier());
			 }
			 conn.setRequestMethod( "POST" );				 
			 conn.addRequestProperty("Accept-Encoding", "gzip");
			 conn.setConnectTimeout(10000);
			 //lookup need more time
			 if(method.getMethodName().equalsIgnoreCase(Phonebook.PHONEBOOK_LOOKUP))
			 {
				 conn.setReadTimeout(60000);
			 }
			 else
			 {
				 conn.setReadTimeout(30000);
			 }
			 
			 
			 conn.setDoOutput( true );
			 conn.connect();			 
			 osw = conn.getOutputStream();
			 osw.write( bufferss.toString().getBytes() );
			 if(DEBUG)
				 Log.d(TAG, bufferss.toString());
			 
			 responseCode = conn.getResponseCode();
			 
			 if(DEBUG)
			     Log.d(TAG, "Response code: " + String.valueOf(responseCode));                     
	         if (responseCode == UNAUTHORIZED || responseCode == FORBIDDEN) 
	         {
	         	 releaseUID(tempID);
	             throw new FacebookException("Fail to get the response", responseCode);
	         }
	         BufferedReader in = null;
	         is =  conn.getInputStream();		
	         String encode = conn.getContentEncoding();
             if(encode != null && encode.equalsIgnoreCase("gzip"))
     		 {
            	 if(DEBUG)
            	     Log.d(TAG, "i am gzip length="+conn.getContentLength());
            	 
            	 GZIPInputStream gzin = new GZIPInputStream(is);
            	 in = new BufferedReader( new InputStreamReader( gzin, "UTF-8" ) );
     		 }
             else
             {
			     in = new BufferedReader( new InputStreamReader( is, "UTF-8" ) );
             }
             
			 StringBuilder buffer = new StringBuilder();
			 char[] chBuf = new char[4*1024];
			 int lineLen =0;
			 boolean insideTagBody = false;
			 while ( ( lineLen = in.read(chBuf, 0, 4*1024) ) >0 ) 
			 {				
				 buffer.append( chBuf, 0, lineLen );					 
			 }
			 xmlResp = buffer.toString();
         }
         catch (IOException ioe) 
         {
        	 if(DEBUG)
        	 {
    		     Log.d(TAG, "what is the exception="+ioe.getMessage());
        	 }
        	 
             if (responseCode == UNAUTHORIZED || responseCode == FORBIDDEN) 
             {             
                 if (DEBUG) {
                     ioe.printStackTrace();
                 }
                 releaseUID(tempID);
                 throw new FacebookException(ioe.getMessage(), responseCode);
             }            
         }
         finally//release resource 
         {
             try {
                 is.close();
             } catch (Exception ignore) {
             }
             try {
                 osw.close();
             } catch (Exception ignore) {
             }
             try {
                 conn.disconnect();
             } catch (Exception ignore) {}
         }
          
         releaseUID(tempID);
         
         if(DEBUG)
		     Log.d(TAG, "callMethod res="+xmlResp);
         
		 return xmlResp;
	}

	public String execute( AndroidHttpClient httpClient ) throws IOException, HttpException
	{
		HttpResponse res = httpClient.execute(this );
		StringBuffer buffer = new StringBuffer();
		InputStream inputStream = res.getEntity().getContent();
		Scanner scanner = new Scanner( inputStream );
		
		while ( scanner.hasNextLine() )
		{
			buffer.append( scanner.nextLine() );
		}
		return ( buffer.toString() );		
	}
	
	public String postRequest( URL serverUrl, Map<String, String> params, String fileName, String mimeType, InputStream fileStream ) throws IOException {
	    String CRLF = "\r\n";
	    String PREF = "--";
	    int UPLOAD_BUFFER_SIZE = 1024;
	    HttpURLConnection con = null;
        OutputStream urlOut = null;
        InputStream in = null;
        try {
            String boundary = Long.toString( System.currentTimeMillis(), 16 );
            con = (HttpURLConnection) serverUrl.openConnection();
            if ( connectionTimeout != -1 ) {
                con.setConnectTimeout( connectionTimeout );
            }
            if ( readTimeout != -1 ) {
                con.setReadTimeout( readTimeout );
            }
            con.setDoInput( true );
            con.setDoOutput( true );
            con.setUseCaches( false );
            con.setRequestProperty( "Content-Type", "multipart/form-data; boundary=" + boundary );
            con.setRequestProperty( "MIME-version", "1.0" );

            urlOut = con.getOutputStream();
            DataOutputStream out = new DataOutputStream( urlOut );

            for ( Map.Entry<String,String> entry : params.entrySet() ) {
                out.writeBytes( PREF + boundary + CRLF );
                out.writeBytes( "Content-Type: text/plain;charset=utf-8" + CRLF );
                out.writeBytes( "Content-disposition: form-data; name=\"" + entry.getKey() + "\"" + CRLF );
                out.writeBytes( CRLF );
                byte[] valueBytes = entry.getValue().toString().getBytes( "UTF-8" );
                out.write( valueBytes );
                out.writeBytes( CRLF );
            }
            out.writeBytes( PREF + boundary + CRLF );
            if(serverUrl.toString().equals(Server.VIDEO_URL))
            {
                out.writeBytes( "Content-Type: " +mimeType+ CRLF ); 
            }
            else
            {
                out.writeBytes( "Content-Type: " +mimeType+ CRLF ); 
            }
            out.writeBytes( "Content-disposition: form-data; filename=\"" + fileName + "\"" + CRLF );
            // Write the file
            out.writeBytes( CRLF );
            byte buf[] = new byte[UPLOAD_BUFFER_SIZE];
            int len = 0;
            while ( len >= 0 ) {
                out.write( buf, 0, len );
                len = fileStream.read( buf );
            }

            out.writeBytes( CRLF + PREF + boundary + PREF + CRLF );
            out.flush();
            in = con.getInputStream();
            return BasicClientHelper.toString( in );
        }
        finally {
            BasicClientHelper.close( urlOut );
            BasicClientHelper.close( in );
            BasicClientHelper.disconnect( con );
            BasicClientHelper.close(fileStream);
        }
    }		
}
