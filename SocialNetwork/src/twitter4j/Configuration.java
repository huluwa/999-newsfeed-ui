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
package twitter4j;

import java.util.Properties;
import java.io.IOException;

import oms.sns.service.facebook.util.StringUtils;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class Configuration {
    private static Properties defaultProperty = new Properties();
    static{
    	/*
        try {        	
        	android.content.res.AssetManager am = new android.content.res.AssetManager();
        	am.addAssetPath("oms.sns.main");
            defaultProperty.load(am.open("twitter4j.properties"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        defaultProperty.setProperty("twitter4j.clientVersion", Version.getVersion());
        */
    }

    public static String getCilentVersion(){
        return "1.0";
    }
    public static String getCilentVersion(String clientVersion){
        return "1.0";
    }
    public static String getSource(){
        return "omsDell";
    }
    public static String getSource(String source){
        return "omsDell";
    }
    public static String getProxyHost(){
        return "";
    }
    public static String getProxyHost(String proxyHost){
        return "";
    }
    public static String getProxyUser(){
        return "";
    }
    public static String getProxyUser(String user){
        return "";
    }
    public static String getClientURL(){
        return "";
    }
    public static String getClientURL(String clientURL){
        return "";
    }

    public static String getProxyPassword(){
        return "";
    }
    public static String getProxyPassword(String password){
        return "";
    }
    public static int getProxyPort(){
        return -1;
    }
    public static int getProxyPort(int port){
        return -1;
    }
    public static int getConnectionTimeout(){
        return 20000;
    }
    public static int getConnectionTimeout(int connectionTimeout){
        return 15000;//pre 20000
    }
    public static int getReadTimeout(){
        return 30000;//pre 120000
    }
    public static int getReadTimeout(int readTimeout){
        return 30000;//120000;
    }

    public static String getUser() {
        return "";
    }
    public static String getUser(String userId) {
        return userId;
    }

    public static String getPassword() {
        return "";
    }
    public static String getPassword(String password) {
        return password;
    }

    public static String getUserAgent() {
        return "oms dell";
    }
    public static String getUserAgent(String userAgent) {
        return userAgent;
    }

    public static String getOAuthConsumerKey() {
        return "cU4vQ18mEsSWCmTPEmqtQ";
    }
    public static String getOAuthConsumerKey(String consumerKey) {
        return StringUtils.isEmpty(consumerKey)?getOAuthConsumerKey():consumerKey;
    }

    public static String getOAuthConsumerSecret() {
        return "V5wsQdNBGsAqVlz4ZKbYKk1AoIzh9nU6VhMBaYmXo";
    }
    public static String getOAuthConsumerSecret(String consumerSecret) {
        return StringUtils.isEmpty(consumerSecret)?getOAuthConsumerSecret():consumerSecret;
    }

    public static boolean getBoolean(String name) {
        String value = getProperty(name);
        return Boolean.valueOf(value);
    }

    public static int getIntProperty(String name) {
        String value = getProperty(name);
        try{
            return Integer.parseInt(value);
        }catch(NumberFormatException nfe){
            return -1;
        }
    }
    public static int getIntProperty(String name, int fallbackValue) {
        String value = getProperty(name, String.valueOf(fallbackValue));
        try{
            return Integer.parseInt(value);
        }catch(NumberFormatException nfe){
            return -1;
        }
    }
    public static long getLongProperty(String name) {
        String value = getProperty(name);
        try{
            return Long.parseLong(value);
        }catch(NumberFormatException nfe){
            return -1;
        }
    }

    public static String getProperty(String name) {
        return getProperty(name, null);
    }
    public static String getProperty(String name, String fallbackValue) {
        String value = System.getProperty(name, fallbackValue);
        if(null == value){
            value = defaultProperty.getProperty(name);
        }
        if(null == value){
            String fallback = defaultProperty.getProperty(name + ".fallback");
            if(null != fallback){
                value = System.getProperty(fallback);
            }
        }
        return replace(value);
    }
    private static String replace(String value){
        if(null == value){
            return value;
        }
        String newValue = value;
        int openBrace = 0;
        if(-1 != (openBrace = value.indexOf("{", openBrace))){
            int closeBrace = value.indexOf("}", openBrace);
            if(closeBrace > (openBrace+1)){
                String name = value.substring(openBrace + 1, closeBrace);
                if(name.length() > 0){
                    newValue = value.substring(0,openBrace) + getProperty(name)
                            + value.substring(closeBrace + 1);
                    
                }
            }
        }
        if (newValue.equals(value)) {
            return value;
        } else {
            return replace(newValue);
        }
    }

    public static int getNumberOfAsyncThreads() {
        return 1;
    }

    public static boolean getDebug() {
        return false;

    }
}
