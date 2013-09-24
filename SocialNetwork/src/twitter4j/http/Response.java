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

import oms.sns.main.ui.TwitterBaseActivity;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.util.Log;
import twitter4j.TwitterException;
import twitter4j.Configuration;
import twitter4j.org.json.JSONException;
import twitter4j.org.json.JSONObject;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

/**
 * A data class representing HTTP Response
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class Response {
	final String TAG="twitter-response";
    private final static boolean DEBUG = Configuration.getDebug();

    private static ThreadLocal<DocumentBuilder> builders =
            new ThreadLocal<DocumentBuilder>() {
                @Override
                protected DocumentBuilder initialValue() {
                    try {
                        return
                                DocumentBuilderFactory.newInstance()
                                        .newDocumentBuilder();
                    } catch (ParserConfigurationException ex) {
                        throw new ExceptionInInitializerError(ex);
                    }
                }
            };

    private int statusCode;
    private Document responseAsDocument = null;
    private String responseAsString = null;
    private InputStream is;
    private HttpURLConnection con;
    private boolean streamConsumed = false;


    public Response(HttpURLConnection con) throws IOException {
        this.con = con;
        this.statusCode = con.getResponseCode();
        if (statusCode == 200) {
            is = con.getInputStream();
        } else {
            is = con.getErrorStream();
        }
        if ("gzip".equals(con.getContentEncoding())) {
            // the response is gzipped        	
        	log("I am gzip="+con.getContentLength());
        	
        	if(is == null)
        		throw new IOException("no content");
        	
            is = new GZIPInputStream(is);
        }
    }

    // for test purpose
    /*package*/ Response(String content) {
        this.responseAsString = content;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseHeader(String name) {
        return con.getHeaderField(name);
    }

    /**
     * Returns the response stream.<br>
     * This method cannot be called after calling asString() or asDcoument()<br>
     * It is suggested to call disconnect() after consuming the stream.
     *
     * Disconnects the internal HttpURLConnection silently.
     * @return response body stream
     * @throws TwitterException
     * @see #disconnect()
     */
    public InputStream asStream() {
        if(streamConsumed){
            throw new IllegalStateException("Stream has already been consumed.");
        }
        return is;
    }

    /**
     * Returns the response body as string.<br>
     * Disconnects the internal HttpURLConnection silently.
     * @return response body     
     * @throws oms.sns.service.twitter4j.TwitterException 
     */
    public String asString(TwitterBaseActivity activity) throws TwitterException{
        if(null == responseAsString){
            BufferedReader br;
            try {
                InputStream stream = asStream();
                br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                
                StringBuffer buf = new StringBuffer();
                
                char[] chBuf = new char[4*1024];
                int lineLen =0;            
                while ( ( lineLen = br.read(chBuf, 0, 4*1024) ) >0 ) 
                {           
                    if(activity != null && activity.getTwitter() != null && activity.getTwitter().exitma())
                        throw new IOException("user exit the read");
                    
                    buf.append( chBuf, 0, lineLen );                     
                }
                this.responseAsString = buf.toString();
                
                log(responseAsString);
                stream.close();
                con.disconnect();
                streamConsumed = true;
            } catch (NullPointerException npe) {
                // don't remember in which case npe can be thrown
                throw new TwitterException(npe.getMessage(), npe);
            } catch (IOException ioe) {
                throw new TwitterException(ioe.getMessage(), ioe);
            }            
        }
        return responseAsString;
    }

    /**
     * Returns the response body as org.w3c.dom.Document.<br>
     * Disconnects the internal HttpURLConnection silently.
     * @return response body as org.w3c.dom.Document
     * @throws TwitterException
     */
    public Document asDocument(TwitterBaseActivity activity) throws TwitterException {
        if (null == responseAsDocument) 
        {
            try 
            {
                // it should be faster to read the inputstream directly.
                // but makes it difficult to troubleshoot
                this.responseAsDocument = builders.get().parse(new ByteArrayInputStream(asString(activity).getBytes("UTF-8")));
            }
            catch (SAXException saxe) {
                throw new TwitterException("The response body was not well-formed:\n" + responseAsString, saxe);
            } catch (IOException ioe) {
                throw new TwitterException("There's something with the connection.", ioe);
            }
            catch(Exception ne)
            {
                throw new TwitterException("Wrong xml return, please check connection", ne);
            }
                    
        }
        return responseAsDocument;
    }

    /**
     * Returns the response body as twitter4j.org.json.JSONObject.<br>
     * Disconnects the internal HttpURLConnection silently.
     * @return response body as twitter4j.org.json.JSONObject
     * @throws TwitterException
     */
    public JSONObject asJSONObject(TwitterBaseActivity activity) throws TwitterException {
        try {
            return new JSONObject(asString(activity));
        } catch (JSONException jsone) {
            throw new TwitterException(jsone.getMessage() + ":" + this.responseAsString, jsone);
        }
    }

    public InputStreamReader asReader() {
        try {
            return new InputStreamReader(is, "UTF-8");
        } catch (java.io.UnsupportedEncodingException uee) {
            return new InputStreamReader(is);
        }
    }

    public void disconnect(){
        con.disconnect();
    }

    @Override
    public String toString() {
        if(null != responseAsString){
            return responseAsString;
        }
        return "Response{" +
                "statusCode=" + statusCode +
                ", response=" + responseAsDocument +
                ", responseString='" + responseAsString + '\'' +
                ", is=" + is +
                ", con=" + con +
                '}';
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
}
