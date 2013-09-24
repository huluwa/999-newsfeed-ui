/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package twitter4j.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import twitter4j.Configuration;
import twitter4j.TwitterException;
import twitter4j.threadpool.QueuedThreadPool.PoolThread;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import com.tormas.litesina.ui.TwitterBaseActivity;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboParameters;

/**
 * A utility class to handle HTTP request/response.
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class HttpClient implements java.io.Serializable {
	final String TAG="sns--HttpClient";
    private final int OK = 200;// OK: Success!
    private final int NOT_MODIFIED = 304;// Not Modified: There was no new data to return.
    private final int BAD_REQUEST = 400;// Bad Request: The request was invalid.  An accompanying error message will explain why. This is the status code will be returned during rate limiting.
    private final int NOT_AUTHORIZED = 401;// Not Authorized: Authentication credentials were missing or incorrect.
    private final int FORBIDDEN = 403;// Forbidden: The request is understood, but it has been refused.  An accompanying error message will explain why.
    private final int NOT_FOUND = 404;// Not Found: The URI requested is invalid or the resource requested, such as a user, does not exists.
    private final int NOT_ACCEPTABLE = 406;// Not Acceptable: Returned by the Search API when an invalid format is specified in the request.
    private final int INTERNAL_SERVER_ERROR = 500;// Internal Server Error: Something is broken.  Please post to the group so the Twitter team can investigate.
    private final int BAD_GATEWAY = 502;// Bad Gateway: Twitter is down or being upgraded.
    private final int SERVICE_UNAVAILABLE = 503;// Service Unavailable: The Twitter servers are up, but overloaded with requests. Try again later. The search and trend methods use this to indicate when you are being rate limited.

    private final static boolean DEBUG = Configuration.getDebug();

    private String basic;
    private int retryCount = 0;
    private int retryIntervalMillis = 10000;    
    private String userId        = null;
    private String password      = null;
    
    private static boolean isSetProxy   = false;
    private static String proxyHost     = null;
    private static int    proxyPort     = 0;
    private static String proxyAuthUser = null;
    private static String proxyAuthPassword = null;
    
    public static void setProxy(boolean enable, String host, int port, String user, String pwd)
    {
    	isSetProxy        = enable;
    	proxyHost         = host;
    	proxyPort         = port;
    	proxyAuthUser     = user;
    	proxyAuthPassword = pwd;           
    }
    
    private int connectionTimeout = 0;
    private int readTimeout = 0;
    private static final long serialVersionUID = 808018030183407996L;
    private boolean isJDK14orEarlier = false;
    private Map<String, String> requestHeaders = new HashMap<String, String>();
    private OAuth oauth = null;
    
    private final String requestTokenURL = "http://api.t.sina.com.cn/oauth/request_token";//"http://api.twitter.com/oauth/request_token";
    private final String authorizationURL = "http://api.t.sina.com.cn/oauth/authorize";//"http://api.twitter.com/oauth/authorize";
    private final String accessTokenURL = "http://api.t.sina.com.cn/oauth/access_token";//"http://api.twitter.com/oauth/access_token";
    private final static String xauthAccesstokenURL = "http://api.t.sina.com.cn/oauth/access_token";
    private OAuthToken oauthToken = null;

    public HttpClient(String userId, String password) {
        this();
        setUserId(userId);
        setPassword(password);        
    }

    TwitterBaseActivity activity;
    public void attachActivity(TwitterBaseActivity baseAc) 
    {        
        activity = baseAc;
    }
    
    public HttpClient() {
        this.basic = null;
        setUserAgent("Liu Huadong/liuhuadong78@gmail.com,android 1.5,2.1,2.2 and above");
        setOAuthConsumer("2359031321", "ccce25d219a325d27e67369b58e2d9a8");
        setRequestHeader("Accept-Encoding","gzip");
        
        setProxyPort(0);
        setProxyHost(null);
        setConnectionTimeout(15000);
        setReadTimeout(40000);
        setProxyAuthUser(null);
        setProxyAuthPassword(null);

        /*
        String versionStr = System.getProperty("java.specification.version");
        if (null != versionStr) {
            isJDK14orEarlier = 1.5d > Double.parseDouble(versionStr);
        }
        */
    }

    public void setUserId(String userId) {
        this.userId = userId;
        encodeBasicAuthenticationString();
    }

    public void setPassword(String password) {
        this.password = password;
        encodeBasicAuthenticationString();
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }
    
    public boolean isAuthenticationEnabled(){
        return null != basic || null != oauth;
    }

    /**
     * Sets the consumer key and consumer secret.<br>
     * System property -Dtwitter4j.oauth.consumerKey and -Dhttp.oauth.consumerSecret override this attribute.
     * @param consumerKey Consumer Key
     * @param consumerSecret Consumer Secret
     * @since Twitter4J 2.0.0
     * @see <a href="http://twitter.com/oauth_clients">Applications Using Twitter</a>
     */
    public void setOAuthConsumer(String consumerKey, String consumerSecret) {
        consumerKey    = OAuth.consumerKey;//Configuration.getOAuthConsumerKey(consumerKey);
        consumerSecret = OAuth.consumerSecret;//Configuration.getOAuthConsumerSecret(consumerSecret);
        if (null != consumerKey && null != consumerSecret
                && 0 != consumerKey.length() && 0 != consumerSecret.length()) {
            this.oauth = new OAuth(consumerKey, consumerSecret);
        }
    }

    /**
     *
     * @return request token
     * @throws TwitterException tw
     * @since Twitter4J 2.0.0
     */
    public RequestToken getOAuthRequestToken() throws TwitterException {
        this.oauthToken = new RequestToken(httpRequest(requestTokenURL, new PostParameter[0], true), this);
        return (RequestToken)this.oauthToken;
    }

    /**
     *
     * @param token request token
     * @return access token
     * @throws TwitterException
     * @since Twitter4J 2.0.0
     */
    public AccessToken getOAuthAccessToken(RequestToken token) throws TwitterException {
        try {
            this.oauthToken = token;
            this.oauthToken = new AccessToken(httpRequest(accessTokenURL, new PostParameter[0], true));
        } catch (TwitterException te) {
            throw new TwitterException("The user has not given access to the account.", te, te.getStatusCode());
        }
        return (AccessToken) this.oauthToken;
    }

    /**
     *
     * @param token request token
     * @param tokenSecret request token secret
     * @return access token
     * @throws TwitterException
     * @since Twitter4J 2.0.1
     */
    public AccessToken getOAuthAccessToken(String token, String tokenSecret) throws TwitterException {
        try {
            this.oauthToken = new OAuthToken(token, tokenSecret) {
            };
            this.oauthToken = new AccessToken(httpRequest(accessTokenURL, new PostParameter[0], true));
        } catch (TwitterException te) {
            throw new TwitterException("The user has not given access to the account.", te, te.getStatusCode());
        }
        return (AccessToken) this.oauthToken;
    }

    /**
     * Sets the authorized access token
     * @param token authorized access token
     * @since Twitter4J 2.0.0
     */

    public void setOAuthAccessToken(AccessToken token){
        this.oauthToken = token;
    }

    public void setRequestTokenURL(String requestTokenURL) {
        //this.requestTokenURL = requestTokenURL;
    }

    public String getRequestTokenURL() {
        return requestTokenURL;
    }


    public void setAuthorizationURL(String authorizationURL) {
       // this.authorizationURL = authorizationURL;
    }

    public String getAuthorizationURL() {
        return authorizationURL;
    }

    public void setAccessTokenURL(String accessTokenURL) {
        //this.accessTokenURL = accessTokenURL;
    }

    public String getAccessTokenURL() {
        return accessTokenURL;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * Sets proxy host.
     * System property -Dtwitter4j.http.proxyHost or http.proxyHost overrides this attribute.
     * @param proxyHost
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = Configuration.getProxyHost(proxyHost);
    }

    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * Sets proxy port.
     * System property -Dtwitter4j.http.proxyPort or -Dhttp.proxyPort overrides this attribute.
     * @param proxyPort
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = Configuration.getProxyPort(proxyPort);
    }

    public String getProxyAuthUser() {
        return proxyAuthUser;
    }

    /**
     * Sets proxy authentication user.
     * System property -Dtwitter4j.http.proxyUser overrides this attribute.
     * @param proxyAuthUser
     */
    public void setProxyAuthUser(String proxyAuthUser) {
        this.proxyAuthUser = Configuration.getProxyUser(proxyAuthUser);
    }

    public String getProxyAuthPassword() {
        return proxyAuthPassword;
    }

    /**
     * Sets proxy authentication password.
     * System property -Dtwitter4j.http.proxyPassword overrides this attribute.
     * @param proxyAuthPassword
     */
    public void setProxyAuthPassword(String proxyAuthPassword) {
        this.proxyAuthPassword = Configuration.getProxyPassword(proxyAuthPassword);
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets a specified timeout value, in milliseconds, to be used when opening a communications link to the resource referenced by this URLConnection.
     * System property -Dtwitter4j.http.connectionTimeout overrides this attribute.
     * @param connectionTimeout - an int that specifies the connect timeout value in milliseconds
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;//Configuration.getConnectionTimeout(connectionTimeout);

    }
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets the read timeout to a specified timeout, in milliseconds. System property -Dtwitter4j.http.readTimeout overrides this attribute.
     * @param readTimeout - an int that specifies the timeout value to be used in milliseconds
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;//Configuration.getReadTimeout(readTimeout);
    }

    private void encodeBasicAuthenticationString() {
        if (null != userId && null != password) {
            this.basic = "Basic " +
                    new String(new BASE64Encoder().encode((userId + ":" + password).getBytes()));
        }
    }

    public void setRetryCount(int retryCount) {
        if (retryCount >= 0) {
            this.retryCount = retryCount;
        } else {
            throw new IllegalArgumentException("RetryCount cannot be negative.");
        }
    }

    public void setUserAgent(String ua) {
        setRequestHeader("User-Agent", Configuration.getUserAgent(ua));
    }
    public String getUserAgent(){
        return getRequestHeader("User-Agent");
    }

    public void setRetryIntervalSecs(int retryIntervalSecs) {
        if (retryIntervalSecs >= 0) {
            this.retryIntervalMillis = retryIntervalSecs * 1000;
        } else {
            throw new IllegalArgumentException(
                    "RetryInterval cannot be negative.");
        }
    }

    public Response post(String url, PostParameter[] postParameters,
                         boolean authenticated) throws TwitterException {
        return httpRequest(url, postParameters, authenticated);
    }
    
    public Response postFile(String url, PostParameter[] postParameters,String status, String filePart,
            boolean authenticated) throws TwitterException {
        return httpRequestFile(url, postParameters, status, filePart, authenticated);
    }
    public int postNoReply(String url, PostParameter[] PostParameters,
            boolean authenticated) throws TwitterException {
	        return httpRequestNoReply(url, PostParameters, authenticated);
	}

    public Response post(String url, boolean authenticated) throws TwitterException {
        return httpRequest(url, new PostParameter[0], authenticated);
    }
    
    public int postNoReply(String url, boolean authenticated) throws TwitterException {
        return httpRequestNoReply(url, new PostParameter[0], authenticated);
    }

    public Response post(String url, PostParameter[] PostParameters) throws
            TwitterException {
        return httpRequest(url, PostParameters, false);
    }

    public Response post(String url) throws
            TwitterException {
        return httpRequest(url, new PostParameter[0], false);
    }

    public Response get(String url, boolean authenticated) throws
            TwitterException {
        return httpRequest(url, null, authenticated);
    }

    public Response get(String url) throws TwitterException {
        return httpRequest(url, null, false);
    }

    private static Integer UID=0;
    void releaseUID(int tempID)
    {
        if(tempID != -1)
        {
            if(activity != null)
                activity.releaseHttpConnection(tempID);
        }
    }
    
    private int httpRequestNoReply(String url, PostParameter[] postParams,
            boolean authenticated) throws TwitterException 
    {    	
    	 int responseCode = -1;
    	 int tempID=-1;
         int retry = retryCount + 1;      
         // update the status
         lastURL = url;
         for (retriedCount = 0; retriedCount < retry; retriedCount++) {
            
             try {
                 HttpURLConnection con = null;                 
                 InputStream is = null;
                 OutputStream osw = null;
                 try {
                 	//make sure the send successful
                     synchronized(UID)
                     {
                         con = getConnection(url); 
                         tempID = UID;
                         if(activity != null)
                         {
                             activity.addHttpConnection(UID, con);
                         
	                         Thread th = Thread.currentThread(); 
	                         if(PoolThread.class.isInstance(th))
	                         {
	                         	PoolThread pt = (PoolThread)(th);
	                         	Runnable run = pt.currentRunning();
	                         	if(run != null && twitter4j.AsyncTwitter.AsyncTask.class.isInstance(run))
	                         	{
	                         		activity.addRunnable(UID, run);
	                         	}
	                         }
                         }
                         
                         UID++;
                     }
                     
                     
 	                 con.setDoInput(true);
 	                 setHeaders(url, postParams, con, authenticated);
                     
                     if (null != postParams) 
                     {
                         log("POST ", url);
                         
                         byte[] bytes=null;
                         con.setRequestMethod("POST");
 	                     con.setRequestProperty("Content-Type",
                                "application/x-www-form-urlencoded");
                         con.setDoOutput(true);
                         String postParam = encodeParameters(postParams);
                         log("Post Params: ", postParam);
                         bytes = postParam.getBytes("UTF-8");

                         con.setRequestProperty("Content-Length",
                                 Integer.toString(bytes.length));
                         osw = con.getOutputStream();
                     
                         osw.write(bytes);
                         osw.flush();
                         osw.close();
                     } 
                     else {
                         log("GET " + url);
                         con.setRequestMethod("GET");
                     }
                     
 	                 responseCode = con.getResponseCode(); 	                 
                     log("Response code: ", String.valueOf(responseCode));                     
                     if (responseCode == NOT_AUTHORIZED || responseCode == FORBIDDEN) 
                     {
                     	 releaseUID(tempID);
                         throw new TwitterException("Fail to get the response", responseCode);
                     }
                     break;
                 } finally {
                     try {
                         is.close();
                     } catch (Exception ignore) {
                     }
                     try {
                         osw.close();
                     } catch (Exception ignore) {
                     }
                     try {
                         con.disconnect();
                     } catch (Exception ignore) {
                     }
                 }
             } catch (IOException ioe) {
                 if (responseCode == NOT_AUTHORIZED || responseCode == FORBIDDEN) {
                     //throw TwitterException without reply since this request won't success
                     if (DEBUG) {
                         ioe.printStackTrace();
                     }
                     releaseUID(tempID);
                     throw new TwitterException(ioe.getMessage(), responseCode);
                 }
                 if (retriedCount == retryCount) {
                     releaseUID(tempID);
                     throw new TwitterException(ioe.getMessage(), responseCode);
                 }
             }
             try {
                 Thread.sleep(retryIntervalMillis);
             } catch (InterruptedException ignore) {
                 //nothing to do
             }
         }
         releaseUID(tempID);
         
         return responseCode;
    }
    
    public void setMultipart(HttpPost post,  Map<String, String> params, Map<String, String>files )
    {
        try{
            MultipartEntity entity = new MultipartEntity();
            
            HttpEntity[] parts = new HttpEntity[ params.size() + files.size()];
            int i = 0;
            for ( String name : params.keySet() )
            {
                String value = params.get( name );          
                entity.addPart(name, new StringBody(value ));
                
            }
            
            for ( String name : files.keySet() )
            {
                String value = files.get( name );
                entity.addPart(name, new FileBody(new File(value)));
            }       
            post.setEntity(entity);
            
        } catch (Exception e) {
            
        }
    }
    
    public String execute( AndroidHttpClient httpClient, HttpPost post ) throws IOException, HttpException
    {
        HttpResponse res = httpClient.execute(post );
        StringBuffer buffer = new StringBuffer();
        InputStream inputStream = res.getEntity().getContent();
        Scanner scanner = new Scanner( inputStream );
        
        while ( scanner.hasNextLine() )
        {
            buffer.append( scanner.nextLine() );
        }
        return ( buffer.toString() );       
    }
       
    protected Response httpRequestFile(String url, PostParameter[] postParams,String status, String filePart,
                                 boolean authenticated) throws TwitterException {
    	int tempID=-1;
        int retriedCount;
        int retry = retryCount + 1;
        Response res = null;
        for (retriedCount = 0; retriedCount < retry; retriedCount++) {
            int responseCode = -1;
            try {
                HttpURLConnection con = null;
                OutputStream osw = null;
                try {
                	//make sure the send successful
                    synchronized(UID)
                    {
                        con = getConnection(url); 
                        tempID = UID;
                        if(activity != null)
                        {
                            activity.addHttpConnection(UID, con);
                            
                            Thread th = Thread.currentThread(); 
                            if(PoolThread.class.isInstance(th))
                            {
                            	PoolThread pt = (PoolThread)(th);
                            	Runnable run = pt.currentRunning();
                            	if(run != null && twitter4j.AsyncTwitter.AsyncTask.class.isInstance(run))
                            	{
                            		activity.addRunnable(UID, run);
                            	}
                            }
                        }
                        
                        UID++;
                    }
                    
                  
                    con.setDoInput(true);
                    setHeaders(url, postParams, con, authenticated);
                    if (null != postParams) {
                        if(DEBUG)log("POST ", url);
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Type",  "multipart/form-data");
                        con.setDoOutput(true);
                        
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < postParams.length; j++) {
                            try {                                
                                sb = sb.append("--");
                                sb = sb.append(BOUNDARY);
                                sb = sb.append("\r\n");
                                sb = sb.append("Content-Disposition: form-data; name=\""+ postParams[j].name + "\"\r\n\r\n");
                                sb = sb.append(URLEncoder.encode(postParams[j].value, "UTF-8").getBytes());
                                sb = sb.append("\r\n");
                            } catch (java.io.UnsupportedEncodingException neverHappen) {
                            }
                        }
                        
                        // send pic
                        sb = sb.append("--");
                        sb = sb.append(BOUNDARY);
                        sb = sb.append("\r\n");
                        sb = sb.append("Content-Disposition: form-data; name=\"pic\"; filename=\""+filePart+"\"\r\n");
                        sb = sb.append("Content-Type: Content-Type: image/jpeg\r\n\r\n");
                        byte[] data = sb.toString().getBytes();
                        byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();  
                        
                        long len = data.length +  new File(filePart).length() + end_data.length;
                        con.setRequestProperty("Content-Length", String.valueOf(len));
                        osw = con.getOutputStream();                                           
                                                
                        osw.write(data);
                        byte[] fbyte = new byte[8*1024];
                        int nread = 0;
                        
                        FileInputStream   fis =  new   FileInputStream(filePart);
                        while((nread =fis.read(fbyte))>0)
                        {
                            osw.write(fbyte, 0, nread);
                        }
                        fis.close();
                        osw.write(end_data);
                        
                        osw.flush();
                        osw.close();
                    } else {
                        if(DEBUG)log("GET " + url);
                        con.setRequestMethod("GET");
                    }
                    res = new Response(con);
                    responseCode = con.getResponseCode();
                    if(DEBUG)log("Response code: ", String.valueOf(responseCode));
                   
                    if(DEBUG)log("Response: ", res.toString());
                    if (responseCode != OK) {
                        if (responseCode < INTERNAL_SERVER_ERROR || retriedCount == retryCount) {
                    	releaseUID(tempID);                    	
                        throw new TwitterException(res.toString(), responseCode);
                        }
                        // will retry if the status code is INTERNAL_SERVER_ERROR 
                    } else {
                        break;
                    }
                } finally {
                    try {
                        osw.close();
                    } catch (Exception ignore) {
                    }
                }
            } catch (IOException ioe) {
                if (responseCode == NOT_AUTHORIZED || responseCode == FORBIDDEN) {
                    //throw TwitterException without reply since this request won't success
                    if (DEBUG) {
                        ioe.printStackTrace();
                    }
                    releaseUID(tempID);
                    throw new TwitterException(ioe.getMessage(), responseCode);
                }
                if (retriedCount == retryCount) {
                    releaseUID(tempID);
                    throw new TwitterException(ioe.getMessage(), ioe, responseCode);
                }
            }
            try {
                if(DEBUG){
                    res.asString(activity);
                }
                log("Sleeping " + retryIntervalMillis +" millisecs for next retry.");
                Thread.sleep(retryIntervalMillis);
            } catch (InterruptedException ignore) {
                //nothing to do
            }
        }
        releaseUID(tempID);
        return res;
    }
    
    String BOUNDARY = "---------------------------198152288819156"; // 分隔符
  //for test purpose
    /*package*/ int retriedCount = 0;
    /*package*/ String lastURL;
    protected Response httpRequest(String url, PostParameter[] postParams,
                                 boolean authenticated) throws TwitterException {
        int tempID=-1;
        int retriedCount;
        int retry = retryCount + 1;
        Response res = null;
        for (retriedCount = 0; retriedCount < retry; retriedCount++) {
            int responseCode = -1;
            try {
                HttpURLConnection con = null;
                OutputStream osw = null;
                try {
                    //make sure the send successful
                    synchronized(UID)
                    {
                        con = getConnection(url); 
                        tempID = UID;
                        if(activity != null)
                        {
                            activity.addHttpConnection(UID, con);
                            
                            Thread th = Thread.currentThread(); 
                            if(PoolThread.class.isInstance(th))
                            {
                                PoolThread pt = (PoolThread)(th);
                                Runnable run = pt.currentRunning();
                                if(run != null && twitter4j.AsyncTwitter.AsyncTask.class.isInstance(run))
                                {
                                    activity.addRunnable(UID, run);
                                }
                            }
                        }
                        
                        UID++;
                    }
                    
                  
                    con.setDoInput(true);
                    setHeaders(url, postParams, con, authenticated);
                    if (null != postParams) {
                        log("POST ", url);
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Type",
                                "application/x-www-form-urlencoded");
                        con.setDoOutput(true);
                        String postParam = encodeParameters(postParams);
                        log("Post Params: ", postParam);
                        byte[] bytes = postParam.getBytes("UTF-8");

                        con.setRequestProperty("Content-Length",
                                Integer.toString(bytes.length));
                        osw = con.getOutputStream();
                        osw.write(bytes);
                        osw.flush();
                        osw.close();
                    } else {
                        log("GET " + url);
                        con.setRequestMethod("GET");
                    }
                    res = new Response(con);
                    responseCode = con.getResponseCode();
                    log("Response code: ", String.valueOf(responseCode));
                    /*
                    if(DEBUG){
                        log("Response header: ");
                        Map<String, List<String>> responseHeaders = con.getHeaderFields();
                        for (String key : responseHeaders.keySet()) {
                            List<String> values = responseHeaders.get(key);
                            for (String value : values) {
                                if(null != key){
                                    log(key + ": " + value);
                                }else{
                                    log(value);
                                }
                            }
                        }
                    }
                    */
                    log("Response: ", res.toString());
                    if (responseCode != OK) {
                        if (responseCode < INTERNAL_SERVER_ERROR || retriedCount == retryCount) {
                        releaseUID(tempID);                     
                        throw new TwitterException(res.toString(), responseCode);
                        }
                        // will retry if the status code is INTERNAL_SERVER_ERROR 
                    } else {
                        break;
                    }
                } finally {
                    try {
                        osw.close();
                    } catch (Exception ignore) {
                    }
                }
            } catch (IOException ioe) {
                if (responseCode == NOT_AUTHORIZED || responseCode == FORBIDDEN) {
                    //throw TwitterException without reply since this request won't success
                    if (DEBUG) {
                        ioe.printStackTrace();
                    }
                    releaseUID(tempID);
                    throw new TwitterException(ioe.getMessage(), responseCode);
                }
                if (retriedCount == retryCount) {
                    releaseUID(tempID);
                    throw new TwitterException(ioe.getMessage(), ioe, responseCode);
                }
            }
            try {
                if(DEBUG){
                    res.asString(activity);
                }
                log("Sleeping " + retryIntervalMillis +" millisecs for next retry.");
                Thread.sleep(retryIntervalMillis);
            } catch (InterruptedException ignore) {
                //nothing to do
            }
        }
        releaseUID(tempID);
        return res;
    }

    public static String encodeParameters(PostParameter[] postParams) {
        StringBuffer buf = new StringBuffer();
        for (int j = 0; j < postParams.length; j++) {
            if (j != 0) {
                buf.append("&");
            }
            try {
                buf.append(postParams[j].name).append("=").append(
                        URLEncoder.encode(postParams[j].value, "UTF-8"));
            } catch (java.io.UnsupportedEncodingException neverHappen) {
            }
        }
        return buf.toString();

    }

    /**
     * sets HTTP headers
     *
     * @param connection    HttpURLConnection
     * @param authenticated boolean
     */
    private void setHeaders(String url, PostParameter[] params, HttpURLConnection connection, boolean authenticated) {
        log("Request: ");
        if (null != params) {
            log("POST ", url);
        }else{
            log("GET ", url);
        }

        if (authenticated) {
            if (basic == null && oauth == null) {
            }
            String authorization = null;
            if (null != oauth) {
                // use OAuth
            	if(params != null)
            	{
            	    WeiboParameters wp = new WeiboParameters();
	            	for(PostParameter item: params)
	            	{
	            		wp.add(item.name, item.value);
	            	}
	            	
	            	authorization = Utility.getAuthorization(params != null ? "POST" : "GET", url, wp,Weibo.getInstance().getAccessToken(), connection);
            	}            	
                //authorization = oauth.generateAuthorizationHeader(params != null ? "POST" : "GET", url, params, oauthToken);
            } else if (null != basic) {
                // use Basic Auth
                authorization = this.basic;
            } else {
                throw new IllegalStateException(
                        "Neither user ID/password combination nor OAuth consumer key/secret combination supplied");
            }
            connection.addRequestProperty("Authorization", authorization);
            log("Authorization: " + authorization);
        }
        for (String key : requestHeaders.keySet()) {
            connection.addRequestProperty(key, requestHeaders.get(key));
            log(key + ": " + requestHeaders.get(key));
        }
    }

    public void setRequestHeader(String name, String value) {
        requestHeaders.put(name, value);
    }

    public String getRequestHeader(String name) {
        return requestHeaders.get(name);
    }

    private HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection con = null;
        if (isSetProxy && proxyHost != null && !proxyHost.equals("")) 
        {
        	log("Proxy : " + proxyHost);
            if (proxyAuthUser != null && !proxyAuthUser.equals("")) {
                log("Proxy AuthUser: " + proxyAuthUser);
                log("Proxy AuthPassword: " + proxyAuthPassword);
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication
                    getPasswordAuthentication() {
                        //respond only to proxy auth requests
                        if (getRequestorType().equals(RequestorType.PROXY)) {
                            return new PasswordAuthentication(proxyAuthUser,
                                    proxyAuthPassword
                                            .toCharArray());
                        } else {
                            return null;
                        }
                    }
                });
            }
            final Proxy proxy = new Proxy(Type.HTTP, InetSocketAddress
                    .createUnresolved(proxyHost, proxyPort));
            if(DEBUG){
                log("Opening proxied connection(" + proxyHost + ":" + proxyPort + ")");
            }
            con = (HttpURLConnection) new URL(url).openConnection(proxy);
        } else {
            con = (HttpURLConnection) new URL(url).openConnection();
        }
        if (connectionTimeout > 0 && !isJDK14orEarlier) {
            con.setConnectTimeout(connectionTimeout);
        }
        if (readTimeout > 0 && !isJDK14orEarlier) {
            con.setReadTimeout(readTimeout);
        }
        return con;
    }

    @Override
    public int hashCode() {
        int result = OK;
        result = 31 * result + (DEBUG ? 1 : 0);
        result = 31 * result + (basic != null ? basic.hashCode() : 0);
        result = 31 * result + retryCount;
        result = 31 * result + retryIntervalMillis;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (proxyHost != null ? proxyHost.hashCode() : 0);
        result = 31 * result + proxyPort;
        result = 31 * result + (proxyAuthUser != null ? proxyAuthUser.hashCode() : 0);
        result = 31 * result + (proxyAuthPassword != null ? proxyAuthPassword.hashCode() : 0);
        result = 31 * result + connectionTimeout;
        result = 31 * result + readTimeout;
        result = 31 * result + (isJDK14orEarlier ? 1 : 0);
        result = 31 * result + (requestHeaders != null ? requestHeaders.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof HttpClient) {
            HttpClient that = (HttpClient) obj;
            return this.retryIntervalMillis
                            == that.retryIntervalMillis && this.basic.equals(that.basic)
                    && this.requestHeaders.equals(that.requestHeaders);
        }
        return false;
    }

    private void log(String message) {
        if (DEBUG) {
            Log.d(TAG, "[" + new java.util.Date() + "]" + message);
        }
    }

    private void log(String message, String message2) {
        if (DEBUG) {
            log(message + message2);
        }
    }


    private String getCause(int statusCode){
        String cause = null;
        // http://apiwiki.twitter.com/HTTP-Response-Codes-and-Errors
        switch(statusCode){
            case NOT_MODIFIED:
                break;
            case BAD_REQUEST:
                cause = "The request was invalid.  An accompanying error message will explain why. This is the status code will be returned during rate limiting.";
                break;
            case NOT_AUTHORIZED:
                cause = "Authentication credentials were missing or incorrect.";
                break;
            case FORBIDDEN:
                cause = "The request is understood, but it has been refused.  An accompanying error message will explain why.";
                break;
            case NOT_FOUND:
                cause = "The URI requested is invalid or the resource requested, such as a user, does not exists.";
                break;
            case NOT_ACCEPTABLE:
                cause = "Returned by the Search API when an invalid format is specified in the request.";
                break;
            case INTERNAL_SERVER_ERROR:
                cause = "Something is broken.  Please post to the group so the Twitter team can investigate.";
                break;
            case BAD_GATEWAY:
                cause = "Twitter is down or being upgraded.";
                break;
            case SERVICE_UNAVAILABLE:
                cause = "Service Unavailable: The Twitter servers are up, but overloaded with requests. Try again later. The search and trend methods use this to indicate when you are being rate limited.";
                break;
            default:
                cause = "";
        }
        return statusCode + ":" + cause;
    }

	public AccessToken getOAuthAccessToken(String token, String tokenSecret,String oauthVerifier) throws TwitterException {
		this.oauthToken = new RequestToken(token,tokenSecret);
		try {
            oauthToken = new AccessToken(post(getAccessTokenURL(), new PostParameter[]{new PostParameter("oauth_verifier", oauthVerifier)}, true).asString(null));
            return (AccessToken) oauthToken;
        } catch (TwitterException te) {
            throw new TwitterException("The user has not given access to the account.", te, te.getStatusCode());
        }
	}

	public void setOAuthAccessToken(String param1, String param2) {
		oauthToken = new AccessToken(param1,param2);
		oauth = new OAuth(null,null);
	}

    public AccessToken getXAuthAccessToken()throws TwitterException {
        try {           
            PostParameter[] params = new PostParameter[3];
            params[0] = new PostParameter("x_auth_username",getUserId());
            params[1] = new PostParameter("x_auth_password",getPassword()); 
            params[2] = new PostParameter("x_auth_mode","client_auth");           
            oauthToken = new AccessToken(post(xauthAccesstokenURL,params,true).asString(null));
            return (AccessToken) oauthToken;
        } catch (TwitterException te) {
            throw new TwitterException("The user has not given access to the account.", te, te.getStatusCode());
        }
    }
}
