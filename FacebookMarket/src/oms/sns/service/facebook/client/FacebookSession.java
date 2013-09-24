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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.util.Log;

import org.apache.http.entity.FileEntity;

import com.msocial.nofree.service.ObjectHandler;
import com.msocial.nofree.ui.ActivityBase;
import com.msocial.nofree.ui.FacebookBaseActivity;
import com.msocial.nofree.ui.NetworkConnectionListener;

import com.msocial.nofree.util.DateUtil;
import oms.sns.service.facebook.client.FacebookMethod.Auth;
import oms.sns.service.facebook.client.FacebookMethod.Comments_new;
import oms.sns.service.facebook.client.FacebookMethod.Events;
import oms.sns.service.facebook.client.FacebookMethod.FBNotes;
import oms.sns.service.facebook.client.FacebookMethod.Fbml;
import oms.sns.service.facebook.client.FacebookMethod.Feed;
import oms.sns.service.facebook.client.FacebookMethod.Fql;
import oms.sns.service.facebook.client.FacebookMethod.Friends;
import oms.sns.service.facebook.client.FacebookMethod.Groups;
import oms.sns.service.facebook.client.FacebookMethod.Link;
import oms.sns.service.facebook.client.FacebookMethod.Mailbox;
import oms.sns.service.facebook.client.FacebookMethod.Marketplace;
import oms.sns.service.facebook.client.FacebookMethod.Message;
import oms.sns.service.facebook.client.FacebookMethod.Notifications;
import oms.sns.service.facebook.client.FacebookMethod.OpenStream;
import oms.sns.service.facebook.client.FacebookMethod.Pages;
import oms.sns.service.facebook.client.FacebookMethod.Phonebook;
import oms.sns.service.facebook.client.FacebookMethod.Photos;
import oms.sns.service.facebook.client.FacebookMethod.Poke;
import oms.sns.service.facebook.client.FacebookMethod.Profile;
import oms.sns.service.facebook.client.FacebookMethod.Status;
import oms.sns.service.facebook.client.FacebookMethod.Users;
import oms.sns.service.facebook.client.FacebookMethod.WALL;
import oms.sns.service.facebook.client.xml.XmlFacebookMethod;
import oms.sns.service.facebook.client.xml.XmlFacebookParser;
import oms.sns.service.facebook.model.Attachment;
import oms.sns.service.facebook.model.ContactInfo;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.model.EventMembersByStatus;
import oms.sns.service.facebook.model.ExtendedPermission;
import oms.sns.service.facebook.model.FBNotifications;
import oms.sns.service.facebook.model.FriendRelationship;
import oms.sns.service.facebook.model.MessageThreadInfo;
import oms.sns.service.facebook.model.Notes;
import oms.sns.service.facebook.model.PhoneBook;
import oms.sns.service.facebook.model.FacebookEmail;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.FeedStory;
import oms.sns.service.facebook.model.FeedStoryTemplate;
import oms.sns.service.facebook.model.Group;
import oms.sns.service.facebook.model.GroupMembersByRole;
import oms.sns.service.facebook.model.Listing;
import oms.sns.service.facebook.model.MailboxMessage;
import oms.sns.service.facebook.model.MailboxThread;
import oms.sns.service.facebook.model.OutstandingNotifications;
import oms.sns.service.facebook.model.Page;
import oms.sns.service.facebook.model.Photo;
import oms.sns.service.facebook.model.PhotoAlbum;
import oms.sns.service.facebook.model.PhotoComment;
import oms.sns.service.facebook.model.PhotoTag;
import oms.sns.service.facebook.model.PokeResponse;
import oms.sns.service.facebook.model.Relationship;
import oms.sns.service.facebook.model.Stream;
import oms.sns.service.facebook.model.StreamFilter;
import oms.sns.service.facebook.model.UserInfo;
import oms.sns.service.facebook.model.UserStatus;
import oms.sns.service.facebook.model.Video;
import oms.sns.service.facebook.model.Wall;
import oms.sns.service.facebook.model.Attachment.BundleActionLink;
import oms.sns.service.facebook.model.Stream.Comments;
import oms.sns.service.facebook.model.UserInfo.Field;
import oms.sns.service.facebook.util.ArrayUtils;
import oms.sns.service.facebook.util.FqlUtils;
import oms.sns.service.facebook.util.StringUtils;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.content.FileBody;


/**
 * Main entry point and common interface to Facebook REST interface 
 * functionality.  Encapsulates and requires a Facebook session key, 
 * thus all functionality here is accessible to logged-in users only.
 * 
 * This class is generally not instantiated directly.  Depending on the 
 * type of application, an instance may be obtained as follows:
 * 
 * TODO G: doc: CanvasRequest.getSession, DesktopApplication.createSession, etc.
 * TODO G: method to check if session is expired?
 * 
 * @author Gino Miceli
 * @author Mino Togna
 */
public class FacebookSession
{
	private FacebookClient	fbClient;
	private long			loggedInUserId	= 0;
	private String secretKey="";
	
	WeakReference<NetworkConnectionListener> connectionListener;
	
	private final static String TAG = "FacebookSession";
    
	public static String mergePID_UID(String pid,String userid)
    {
        long paramAID = Long.parseLong(pid);
        long uid = Long.parseLong(userid);
        String resultStr = uid+"_"+paramAID;
        
        if((uid >= -9223372036854775808L && uid<-4294967296L) || (uid >4294967296L && uid<=9223372036854775807L))
        {
            resultStr = uid + "_"+paramAID;
        }
        else
        {
            resultStr = String.valueOf((uid << 32) + (paramAID & 0xFFFFFFFF));
        }
        
        return resultStr;
    }
	
	public FacebookSession( String apiKey, String secretKey, String sessionKey )
	{
		fbClient = new FacebookClient( apiKey, secretKey, sessionKey );
		this.secretKey = secretKey;
	}

	public FacebookSession( String apiKey, String secretKey, String sessionKey, long loggedInUserId )
	{
		this( apiKey, secretKey, sessionKey );
		this.loggedInUserId = loggedInUserId;		
	}
	
	public FacebookClient getFbClient(){
		return fbClient;
	}
	public void attachActivity(NetworkConnectionListener baseAc) 
	{        
		connectionListener = new WeakReference<NetworkConnectionListener>(baseAc);
		fbClient.attachActivity(baseAc);
	}
	public NetworkConnectionListener getBaseActivity()
	{
		return connectionListener==null ?null:connectionListener.get();
	}

	public void dispose() 	
	{
		fbClient.dispose();
		fbClient = null;
		
		secretKey = null;		
	}
	
	public String getSessionKey() {
		return fbClient.getSessionKey();
	}
	public long getLogerInUserID()
	{
		return loggedInUserId;
	}
	public String getSecretKey()
	{
		return secretKey;
	}
	
	public boolean AuthLogin(String ses_key, String secret_key, String email, String pwd ) throws FacebookException
	{
		Document doc =  fbClient.callSecureMethod( new XmlFacebookMethod(Auth.LOGIN),  /*FacebookClient.LoginParameters.AUTH_TOKEN, token,*/ FacebookClient.RestParameters.SESSION_KEY, ses_key, FacebookClient.RestParameters.SECRET_KEY, secret_key, FacebookClient.LoginParameters.EMAIL, email, FacebookClient.LoginParameters.PASSWORD, pwd);
		
		//create a FacebookSession
		NodeList nl = doc.getElementsByTagName( "session_key" );
		if(nl != null && nl.item( 0 ) != null && nl.item( 0 ).getFirstChild()!= null)
		{	
			String sessionkey = nl.item( 0 ).getFirstChild().getNodeValue();
			return true;
		}
		
		return false;
		//TODO
	}
	
	////////////////////////////////////////
	////////// facebook.fbml.xxx ///////////
	////////////////////////////////////////

	public Document refreshImgSrc( String url ) throws FacebookException
	{
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("url", url);
		return fbClient.callMethod( new XmlFacebookMethod(Fbml.REFRESH_IMG_SRC), paramsMap );
	}

	public Document refreshRefUrl( String url ) throws FacebookException
	{
		return fbClient.callMethod( new XmlFacebookMethod(Fbml.REFRESH_REF_URL), "url", url );
	}

	public Document setRefHandle( String handle, String fbml ) throws FacebookException
	{
		return fbClient.callMethod( new XmlFacebookMethod(Fbml.SET_REF_HANDLE), "handle", handle, "fbml", fbml );
	}
	
	public void ExpireSesssion(String sessionkey)throws FacebookException
	{	
		fbClient.callMethod( new XmlFacebookMethod(Auth.SESSION_EXPIRE), FacebookClient.RestParameters.SESSION_KEY, sessionkey);		
	}

	////////////////////////////////////////
	////////// facebook.feed.xxx ///////////
	////////////////////////////////////////

	//TODO: rename to publishStoryToUser ?!?! It could be less confusing
	public Document publishStory( FeedStory story ) throws FacebookException
	{
		return fbClient.callMethod( new XmlFacebookMethod(Feed.PUBLISH_STORY_TO_USER), "title", story.getTitle(), "body", story.getBody(),
				"image_1", story.getImage1(), "image_1_link", story.getImage1Link() ,
				"image_2", story.getImage2(), "image_2_link", story.getImage2Link() ,
				"image_3", story.getImage3(), "image_3_link", story.getImage3Link() ,
				"image_4", story.getImage4(), "image_4_link", story.getImage4Link()
			);
	}

	public Document publishAction( FeedStory story ) throws FacebookException
	{
		return fbClient.callMethod( new XmlFacebookMethod(Feed.PUBLISH_ACTION_OF_USER), "title", story.getTitle(), "body", story.getBody(),
				"image_1", story.getImage1(), "image_1_link", story.getImage1Link() ,
				"image_2", story.getImage2(), "image_2_link", story.getImage2Link() ,
				"image_3", story.getImage3(), "image_3_link", story.getImage3Link() ,
				"image_4", story.getImage4(), "image_4_link", story.getImage4Link()
			);
	}

	public Document publishAction( FeedStoryTemplate template ) throws FacebookException
	{
		return fbClient.callMethod( new XmlFacebookMethod(Feed.PUBLISH_TEMPLATIZED_ACTION), "actor_id", template.getActorId(), 
				"title_template", template.getTitleTemplate(), "title_data", template.getTitleData(),
				"body_template", template.getBodyTemplate(), "body_data", template.getBodyData(), 
				"body_general", template.getBodyGeneral(), "target_ids",template.getTargetUserIds(),
				"image_1", template.getImage1(), "image_1_link", template.getImage1Link() ,
				"image_2", template.getImage2(), "image_2_link", template.getImage2Link() ,
				"image_3", template.getImage3(), "image_3_link", template.getImage3Link() ,
				"image_4", template.getImage4(), "image_4_link", template.getImage4Link()
			);
	}

	///////////////////////////////////////
	////////// facebook.fql.xxx ///////////
	///////////////////////////////////////

	
	@SuppressWarnings( "unchecked" )
	public Document executeQuery( String fql ) throws FacebookException
	{
		String tableName = FqlUtils.extractTableName( fql );		

		return fbClient.callMethod(new XmlFacebookMethod(Fql.QUERY) , "query", fql );
	}
	

	//////////////////////////////////////
	//////// facebook.friends.xxx ////////
	//////////////////////////////////////

	public Relationship[] getRelationship( long[] userIds1, long[] userIds2 ) throws FacebookException
	{
		Document doc =  fbClient.callMethod( new XmlFacebookMethod(Friends.ARE_FRIENDS), "uids1", userIds1, "uids2", userIds2 );
		
		return null;
	}

//	public boolean areFriends( long userId1, long userId2 ) throws Exception
//	{
//		// return ( fbClient.callMethod( Friends.ARE_FRIENDS, "uids1", new
//		// long[] { userId1 }, "uids2", new long[] { userId2 } )
//		// .areFriends( userId1, userId2 ) );
//		// TODO ?: Implement me?!
//		return true;
//	}

	public long[] getFriendIds() throws FacebookException
	{
		Document doc =  ( fbClient.callMethod( new XmlFacebookMethod(Friends.GET )) );
				
		if(doc == null)
			return null;
			
		NodeList list = doc.getElementsByTagName( "uid" );
		long[] fids = new 	long[list.getLength()];
		
		for(int i=0;i<list.getLength();i++)
		{
			org.w3c.dom.Node  node = list.item(i);
			String val = node.getChildNodes().item(0).getNodeValue();
			
			fids[i] = Long.parseLong(val);
		}		
		return fids;
	}
	
	
	public long[] getFriendIds(int limit, int offset) throws FacebookException
	{
		String uidsql = String.format("SELECT uid2 FROM friend WHERE uid1 = %1$s  LIMIT %2$s OFFSET %3$s", this.getLogerInUserID(), limit, offset);
		Document doc = this.executeQuery(uidsql);
		
		if(doc == null)
		{
			throw new FacebookException("no content return");
		}
			
		NodeList list = doc.getElementsByTagName( "friend_info" );
		int size = list.getLength();
		long[] fids = new 	long[list.getLength()];
		for(int i=0;i<list.getLength();i++)
		{
			org.w3c.dom.Node  node = list.item(i);
			
			for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{					
					String name = cnode.getNodeName();	
					String val = XmlFacebookParser.getChildText(name, (Element)cnode);
					if(val.equals(""))  continue;
				    if(name.equalsIgnoreCase("uid2"))
				    {
				    	fids[i] = Long.parseLong(val);
				    }
				}
			}
		}		
		return fids;
	}

	public long[] getAppUserFriendIds() throws FacebookException
	{
		Document doc =  ( fbClient.callMethod( new XmlFacebookMethod(Friends.GET_APP_USERS )) );
		
		if(doc == null)
			return null;
			
		NodeList list = doc.getElementsByTagName( "uid" );
		long[] fids = new 	long[list.getLength()];
		
		for(int i=0;i<list.getLength();i++)
		{
			org.w3c.dom.Node  node = list.item(i);
			String val = node.getChildNodes().item(0).getNodeValue();
			
			fids[i] = Long.parseLong(val);
		}		
		return fids;				
	}

	///////////////////////////////////////////
	//////// facebook.notifications.xxx ///////
	///////////////////////////////////////////

	/**
	 * notifications.get
	 * @throws FacebookException 
	 */
	public OutstandingNotifications getOutstandingNotifications() throws FacebookException
	{
		Document doc =  fbClient.callMethod( new XmlFacebookMethod(Notifications.GET ));
		return null;
	}

	public boolean sendNotification( String notification, long... toUserIds ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Notifications.SEND), "notification", notification, "to_ids", toUserIds );
		return true;
	}
	
	//TODO : check if it's valid
    public boolean sendEmail( String text,String subject,long[] receivers) throws FacebookException
    {
       fbClient.callMethod( new XmlFacebookMethod( Notifications.SEND_EMAIL), "subject", subject, "text", text, "recipients", receivers);
       return true;
    }
	
	//TODO : check if it's valid
	public long[] sendEmail( FacebookEmail facebookEmail ) throws FacebookException
	{
		//Document doc =  fbClient.callMethod( new XmlFacebookMethod( Notifications.SEND_EMAIL), "subject", facebookEmail.getSubject(), "fbml", facebookEmail.getFbml(), "text", facebookEmail.getText(), "recipients", facebookEmail.getRecipients() );
		
		Document doc =  fbClient.callMethod( new XmlFacebookMethod( Notifications.SEND_EMAIL), "subject", facebookEmail.getSubject(), "text", facebookEmail.getText(), "recipients", facebookEmail.getRecipients() );
		return null;
	}

	///////////////////////////////////////////
	////////// facebook.profile.xxx ///////////
	///////////////////////////////////////////

	public boolean setProfileFbml( long userId, String fbml ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Profile.SET_FBML), "uid", userId, "markup", fbml );
		/*
		<?xml version="1.0" encoding="UTF-8"?>
		<error_response xmlns="http://api.facebook.com/1.0/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://api.facebook.com/1.0/ http://api.facebook.com/1.0/facebook.xsd">  
		   <error_code>200</error_code>
		</error_response>
		*/
		return false;
	}

	public boolean setProfileFbml( String fbml ) throws FacebookException
	{
		return setProfileFbml( getUserId(), fbml );
	}

	public String getProfileFbml( long userId ) throws FacebookException
	{
		// TODO G: Get/set fbml not allowed for Desktop apps - does FB throw
		// exception for us, or do we need to?
		Document doc =  fbClient.callMethod( new XmlFacebookMethod( Profile.GET_FBML), "uid", userId );
		
		return null;
	}

	public String getProfileFbml() throws FacebookException
	{
		return getProfileFbml( getUserId() );
	}
	
	final static String userinfofields = "uid,first_name,last_name,birthday,name,pic_square,pic, pic_small, sex,status,about_me,activities,books,movies,"+
                                         "music,tv,current_location,education_history,hometown_location,meeting_for,meeting_sex,"+
                                         "relationship_status,work_history, online_presence, is_app_user, quotes,interests ";
	
    public FacebookUser getCompeletedUserInfo(long uid) throws FacebookException
    {
		String fql = "select "+userinfofields+" from user where uid = "+uid;
		Document doc = this.executeQuery(fql);
		if(doc != null)
		{
    		List<FacebookUser> facebookuser = XmlFacebookParser.parseFacebookUserResponse(doc,this, false,true); 
    		if(facebookuser!=null && facebookuser.size()>0)
    		{
    		    FacebookUser user =facebookuser.get(0);
    		    facebookuser = null;
    		    return user;
    		}
    		else
    		{
    		    return null;
            }
		}
		else
		{
		    return null;
		}		
    }

    public List<FacebookUser> getUserInfo(long[] userIds,boolean hasProgress, String... fields) throws FacebookException{
        String fieldNames = StringUtils.join( fields, "," );
        Document doc = fbClient.callMethod( new XmlFacebookMethod( Users.GET_INFO), "uids", userIds, "fields", fieldNames.toLowerCase() );
        return  XmlFacebookParser.parseFacebookUserResponse(doc, this, hasProgress, true);
    }
    
    public List<FacebookUser.SimpleFBUser> getSimpleUserInfo(long[] userIds,boolean hasProgress, String... fields) throws FacebookException{
        String fieldNames = StringUtils.join( fields, "," );
        Document doc = fbClient.callMethod( new XmlFacebookMethod( Users.GET_INFO), "uids", userIds, "fields", fieldNames.toLowerCase() );
        return  XmlFacebookParser.parseFacebookSimpleUserResponse(doc, this, hasProgress, true);
    }
    
	//TODO
	public List<FacebookUser> getUserInfo(long[] userIds, String ... fields) throws FacebookException{
		String fieldNames = StringUtils.join( fields, "," );
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Users.GET_INFO), "uids", userIds, "fields", fieldNames.toLowerCase() );
		return  XmlFacebookParser.parseFacebookUserResponse(doc, this, false, true);
	}
	
	public FacebookUser getUserInfo( long userId, String... fields ) throws FacebookException{
		long[] userids = {userId};
		List<FacebookUser> list = getUserInfo(userids, fields);
		if(list!=null && list.size()>0) return list.get(0);
		else return null;
	}


	public  FacebookUser getUserInfo( String... fields ) throws FacebookException
	{
		return getUserInfo( getUserId(), fields );
	}
	
	/**
	 * facebook.users_getContactInfo
	 * required params: string api_key;  string session_key;float call_id;
	 *                  string sig;string v; string format;array uids;
	 * optional params: string 	callback; int friends;
	 * @return
	 * @throws FacebookException
	 */	
	public List<PhoneBook> getContactInfo(long[] uids) throws FacebookException{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Users.GET_CONTACT_INFO_EXT),"uids",uids);
	    return XmlFacebookParser.parseContactInfoResponse(doc);
	}
	
	public List<PhoneBook> getContactInfo(long[] uids,int friends) throws FacebookException{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Users.GET_CONTACT_INFO_EXT),"uids",uids/*,"friends",friends*/);
	    return XmlFacebookParser.parseContactInfoResponse(doc);
	}
	
	public PhoneBook getContactInfo(long uid ) throws FacebookException
	{
	    long[] userids = {uid};
        List<PhoneBook> list = getContactInfo(userids);
        if(list!=null && list.size()>0) return list.get(0);
        else return null;
	}
	
	public List<PhoneBook> getContactInfo() throws FacebookException{
		long[] friends = getFriendIds();
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Users.GET_CONTACT_INFO_EXT),"uids",friends,"friends",1);
	    return XmlFacebookParser.parseContactInfoResponse(doc);
	}
	
	public List<PhoneBook> getContactInfo(int limit, int offset) throws FacebookException{
		long[] friends = getFriendIds(limit, offset);
		if(friends.length > 0)
		{
		    Document doc = fbClient.callMethod( new XmlFacebookMethod( Users.GET_CONTACT_INFO_EXT),"uids",friends,"friends",1);
	        return XmlFacebookParser.parseContactInfoResponse(doc);
		}
		else
		{
			List<PhoneBook> temp = new ArrayList<PhoneBook>();
			return temp;
		}		
	}
		
	/**
	 * Caches logged in user ID if it is already known
	 */
	public long getUserId() throws FacebookException
	{
		if ( this.loggedInUserId == 0 )
		{
			Document doc = fbClient.callMethod( new XmlFacebookMethod( Users.GET_LOGGED_IN_USER) );
		}

		//TODO
		
		this.loggedInUserId = 0;
		
		return this.loggedInUserId;
	}

	public boolean setStatus( String status ) throws FacebookException
	{
		return setStatus(status,true);
	}
	
	public boolean setStatus(String status,boolean status_includes_verb) throws FacebookException{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Users.SET_STATUS), "status", status,"status_includes_verb",status_includes_verb);
		return XmlFacebookParser.extractBoolean(doc);
	}
    
	public boolean clearStatus() throws FacebookException
	{
		Document doc =  fbClient.callMethod( new XmlFacebookMethod( Users.SET_STATUS), "clear", true );
		return false;
	}
	
	public List<UserStatus> getStatusByuid(long uid) throws FacebookException{
		long limit = 20;	
		return getStatus(uid,limit);
	}
	
	public List<UserStatus> getStatus(long uid,long limit) throws FacebookException{		
		 Document doc =  null;
		 if(getLogerInUserID() == uid ){
		    doc = fbClient.callMethod(new XmlFacebookMethod(Status.STATUS_GET), "uid",uid,"limit",limit);
		 }else{
			String query = "SELECT uid,status_id ,time,source,message from status  WHERE uid = "+uid;
			doc = executeQuery(query);
		 }
		return XmlFacebookParser.parseUserStatus(doc, this);
	}
	
	public List<UserStatus> getStatus(long uid,long from,long offset) throws FacebookException{		
		String query = "SELECT uid,status_id ,time,source,message from status  WHERE uid = "+uid + " LIMIT "+from+","+offset;
		Document doc =  executeQuery(query);
		return XmlFacebookParser.parseUserStatus(doc, this);
	}
	
	public List<UserStatus> getAllMyFriendsStatus(long from,long offset) throws FacebookException{
		String friendid = "SELECT uid2 FROM friend WHERE uid1 = "+getLogerInUserID();
		String query = "SELECT uid,status_id,time,source,message from status where uid in ("+friendid +") order by status.time desc LIMIT "+from+","+offset;
		Document doc =  executeQuery(query);
		return XmlFacebookParser.parseUserStatus(doc, this);
	}
	
	public String getUserNameByUid(long uid) throws FacebookException{
		String query = "select name from user where uid="+uid;
		Document doc = executeQuery(query);
		return XmlFacebookParser.parseUserName(doc);
	}
	
    /*public Map<String,String> getFriendIDNameMap(long from,long offset){
		String friendid = 
	}*/
	
	/**
	 * Checks if the user granted the app permission to...
	 * 
	 * @param extPerm
	 * @return
	 */
	public boolean hasAppPermission( ExtendedPermission extendedPermission ) throws FacebookException
	{
		Document doc =  ( fbClient.callMethod( new XmlFacebookMethod( Users.HAS_APP_PERMISSION), "ext_perm", extendedPermission.getFacebookPermission() ) );
		return XmlFacebookParser.extractBoolean(doc);
	}
	
	public boolean revokePermission(ExtendedPermission permission) throws FacebookException
	{	
		Document doc = fbClient.callMethod(new XmlFacebookMethod(Auth.REVOKE_EXTPERMISSION), "perm", permission.getFacebookPermission());
		return XmlFacebookParser.extractBoolean(doc);
	}   
     
	public List<Boolean> hasAppPermission_batch_run(List<String> methodMap) throws FacebookException
	{  
	    fbClient.beginBatch();
	    
	    for(String  methodName : methodMap)
	    {
	        ExtendedPermission temppermission = ExtendedPermission.getPermission(methodName);
            hasAppPermission(temppermission);
	    }
	    
	    List<? extends Object> list = fbClient.executeBatch(true);    
	    List<Boolean> resultlist = new ArrayList<Boolean>();
        
	    for(int i=0;i<list.size();i++)
	    {
	        resultlist.add(Boolean.valueOf(XmlFacebookParser.extractBoolean((Document)list.get(i))));
	    }
	    
	    return resultlist;
	}
	/**
	 * Checks whether the user has added the application to their Facebook
	 * account
	 * 
	 * @return
	 */
	//TODO : name confusing with pages.isAppAddedd() ?!?!
	public boolean isAppAdded() throws FacebookException
	{
		Document doc =  ( fbClient.callMethod( new XmlFacebookMethod( Users.IS_APP_ADDED )) );
		if(doc == null)
			return true;
		
		org.w3c.dom.Node  node = doc.getFirstChild();
		String name = node.getNodeName();
		String value = node.getNodeValue();
		String val = node.getChildNodes().item(0).getNodeValue();
		
		//Long.parseLong( doc.getElementsByTagName( "users_isAppAdded_response" ).item( 0 ).getFirstChild().getNodeValue());
		
		return  val.equalsIgnoreCase("1");		
	}

	/////////////////////////////////////
	//////// facebook.events.xxx ////////
	/////////////////////////////////////

	public List<Event> getEvents( long userId, long[] eventIds, long startTime, long endTime, EventMembersByStatus.RsvpStatus rsvpStatus ) throws FacebookException
	{
		Document doc =  ( fbClient.callMethod( new XmlFacebookMethod( Events.GET), "uid", userId, "eids", eventIds, "start_time", startTime, "end_time", endTime, "rsvp_status", rsvpStatus.toString() ) );
		
		return XmlFacebookParser.parseEventInfo(doc,this);
	}

	public List<Event> getEvents( long... eventIds ) throws FacebookException
	{
		Document doc =  ( fbClient.callMethod( new XmlFacebookMethod( Events.GET), "eids", eventIds ) );
		
		return XmlFacebookParser.parseEventInfo(doc,this);
	}

	public Event getEvent( long eventId ) throws FacebookException
	{
		List<Event> list = getEvents(eventId);
		return list==null||list.size()==0?null:list.get(0);
	}
	
	public List<Event> getUpcomingEventsNotification(long... eventIds) throws FacebookException
    {
	    long starttime = DateUtil.getCurrentTimeForEvent()/1000;
        Document doc =  ( fbClient.callMethod( new XmlFacebookMethod( Events.GET), "eids", eventIds,"start_time",starttime) );      
        return XmlFacebookParser.parseEventInfo(doc,this);
    }

	public List<Event> getEvents() throws FacebookException
	{
		Document doc =  ( fbClient.callMethod( new XmlFacebookMethod( Events.GET) ) );
		return XmlFacebookParser.parseEventInfo(doc,this);
	}
	
	public HashMap<Integer,Object> batch_run_getEventGuest(long eventId) throws FacebookException
	{
	    HashMap<Integer,Object> map = new HashMap<Integer,Object>();
        fbClient.beginBatch();
        getEventSimpleMembers(eventId, Event.ATTENDING);
        getEventSimpleMembers(eventId, Event.UNSURE);
        getEventSimpleMembers(eventId, Event.DECLINED);
        getEventSimpleMembers(eventId, Event.NOT_REPLIED);
       // getFriendsEvents();
        List<? extends Object> list = fbClient.executeBatch(true);;    
        int i=0;
        map.put(i,XmlFacebookParser.parseFacebookSimpleUserResponse((Document)list.get(i),this, true, false));
        i=1;
        map.put(i,XmlFacebookParser.parseFacebookSimpleUserResponse((Document)list.get(i),this, false, false)); 
        i=2;
        map.put(i,XmlFacebookParser.parseFacebookSimpleUserResponse((Document)list.get(i),this, false, false));
        i=3;
        map.put(i,XmlFacebookParser.parseFacebookSimpleUserResponse((Document)list.get(i),this, false, false));
        return map;
	}
	
	public HashMap<Integer,Object> batch_run_getEvents() throws FacebookException
    {   
        HashMap<Integer,Object> map = new HashMap<Integer,Object>();
        fbClient.beginBatch();
        getUpcomingEventsByFql(0,20);
        getPastEventsByFql(0,20);
       // getFriendsEvents();
        List<? extends Object> list = fbClient.executeBatch(true);;    
        int i=0;
        map.put(i,XmlFacebookParser.parseEventInfo((Document)list.get(i),this));
        i=1;
        map.put(i,XmlFacebookParser.parseEventInfo((Document)list.get(i),this)); 
        /*i=2;
        map.put(i,XmlFacebookParser.parseEventInfo((Document)list.get(i),this));*/
        return map;
    }
	
	public List<Event> getFriendsEvents() throws FacebookException
	{
	   return null;
	}
	
	static String event_fql_col = " eid,name,tagline,nid,pic,pic_big,pic_small,host,description,event_type,event_subtype,start_time,end_time,creator,update_time,location,venue ";
	
	public List<Event> getUpcomingEventsByFql(int limit,int offset) throws FacebookException
	{
	     long current_time = DateUtil.getCurrentTimeForEvent()/1000;
	     String fql = " select "+ event_fql_col+" from event where eid in (select eid from event_member where uid='"+getLogerInUserID()+"')"+
	                  " and  end_time >= "+ current_time  +" order by start_time desc LIMIT "+limit+","+offset;
	     Document doc = this.executeQuery(fql);
	     return XmlFacebookParser.parseEventInfo(doc,this);
	}
	
	public List<Event> getEvents(int limit, int offset) throws FacebookException
	{
	    String fql = "select "+event_fql_col+" from event where eid in (select eid from event_member where uid ='"+getLogerInUserID()+"')"+
	                 " order by end_time desc LIMIT "+limit+","+offset;
	    Document doc = this.executeQuery(fql);
        return XmlFacebookParser.parseEventInfo(doc,this);
	}
	
	
	public List<Event> getUpcomingEvents() throws FacebookException
	{
		long startTime = DateUtil.getCurrentTimeForEvent()/1000;
        Document doc =  ( fbClient.callMethod( new XmlFacebookMethod( Events.GET),"start_time", startTime) );
		return XmlFacebookParser.parseEventInfo(doc,this);
	}
	
	public List<Event> getPastEventsByFql(int limit,int offset) throws FacebookException
	{
	    long current_time = DateUtil.getCurrentTimeForEvent()/1000;
        String fql = " select "+ event_fql_col+" from event where eid in (select eid from event_member where uid='"+getLogerInUserID()+"')"+
                     " and start_time < "+current_time+ " order by start_time desc LIMIT "+limit+","+offset;
        Document doc = this.executeQuery(fql);
        return XmlFacebookParser.parseEventInfo(doc,this);
	}
	
	public List<Event> getPastEvents() throws FacebookException
	{
		long endTime = DateUtil.getCurrentTimeForEvent()/1000;
        Document doc =  ( fbClient.callMethod( new XmlFacebookMethod( Events.GET),"end_time", endTime) );
		return XmlFacebookParser.parseEventInfo(doc,this);
	}
	
	public Long createEvent(HashMap infomap) throws FacebookException{
		if(infomap!=null && infomap.size()>0){
			Set set = infomap.entrySet();
			Iterator<Map.Entry<String,Object>> it = set.iterator();
			StringBuffer eventinfobf = new StringBuffer("{");
			try{
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("name", String.valueOf(infomap.get("name")));
				jsonObj.put("category", ((Integer)infomap.get("category")).intValue());
				jsonObj.put("subcategory", ((Integer)infomap.get("subcategory")).intValue());
				jsonObj.put("host", String.valueOf(infomap.get("host")));
				jsonObj.put("location", String.valueOf(infomap.get("location")));
				jsonObj.put("start_time", ((Long)infomap.get("start_time")).longValue());
				jsonObj.put("end_time",((Long)infomap.get("end_time")).longValue());
				jsonObj.put("city", infomap.get("city")!=null?infomap.get("city"):"");
				jsonObj.put("description", infomap.get("description")!=null?infomap.get("description"):"");
				
		        String eventinfo = jsonObj.toString();
		        /*{"name":"name","category":"1","subcategory":"1",
				 * "host":"host","location":"location","city":"Palo Alto, CA",
				 * "start_time":1215929160,"end_time":1215929160}*/
				Document doc =  fbClient.callMethod( new XmlFacebookMethod( Events.EVENTS_CREATE),"event_info",eventinfo) ;
				return XmlFacebookParser.extractLong(doc);
				
	        }catch(JSONException e){
	        	return new Long(0);
	        }
			
		
		}else{
			return new Long(0);
		}
		
	}
	
	public List<FacebookUser.SimpleFBUser> getEventSimpleMembers(long eventId,String rsvp_status,int limit,int offset) throws FacebookException
    {
        String fql = "select uid,birthday,name,pic_square from user where uid in (" +
        "select uid from event_member where eid = "+eventId;
        if(!StringUtils.isEmpty(rsvp_status))
        {
           fql = fql +  " and rsvp_status='"+rsvp_status+"' " ;
        }
        fql = fql + ") limit "+ limit+","+offset;
        Document doc = this.executeQuery(fql);
        return XmlFacebookParser.parseFacebookSimpleUserResponse(doc, this, true, true);       
    }
	
	public List<FacebookUser.SimpleFBUser> getEventSimpleMembers(long eventId,String rsvp_status) throws FacebookException
	{
	    String fql = "select uid,birthday,name,pic_square from user where uid in (" +
        "select uid from event_member where eid = "+eventId;
	    if(!StringUtils.isEmpty(rsvp_status))
	    {
	       fql = fql +  " and rsvp_status='"+rsvp_status+"' " ;
	    }
	    fql = fql + ") limit 0,20";
	    Document doc = this.executeQuery(fql);
	    return XmlFacebookParser.parseFacebookSimpleUserResponse(doc, this, true, true);       
	}
	
	public List<FacebookUser> getEventMembers(long eventId,String rsvp_status) throws FacebookException
	{
	    String fql = "select uid,first_name,last_name,birthday,name,pic_square,pic, pic_small, sex,status from user where uid in (" +
        "select uid from event_member where eid = "+eventId;
	    if(!StringUtils.isEmpty(rsvp_status))
	    {
	       fql = fql +  " and rsvp_status='"+rsvp_status+"' " ;
	    }
	    fql = fql + ") limit 0,20";
	    Document doc = this.executeQuery(fql);
	    return XmlFacebookParser.parseFacebookUserResponse(doc, this, true, true);       
	}
	
	public String getRSVPStatus(long eventId,long uid) throws FacebookException
	{
	    String fql = "select  rsvp_status from event_member where eid="+eventId+" and uid="+uid;
       
        Document doc = this.executeQuery(fql);
        return XmlFacebookParser.parseEventRSVPStatus(doc,this);
	}
	
	public EventMembersByStatus getEventMembers(long eventId,long uid) throws FacebookException
	{
	    Document doc = ( fbClient.callMethod( new XmlFacebookMethod( Events.GET_MEMBERS), "eid", eventId,"uid",uid ) );
        return null;
	}

	public EventMembersByStatus getEventMembers( long eventId ) throws FacebookException
	{
		Document doc = ( fbClient.callMethod( new XmlFacebookMethod( Events.GET_MEMBERS), "eid", eventId ) );
		//return XmlFacebookParser.parseEventMembers(doc,this);
		return null;
	}
	
	/**
     * Sets a user's RSVP status for an event. An application can set a user's RSVP status only if the following are all true:
     * <ul>
     * <li>The application is an admin for the event.</li>
     * <li>The application has an active session for the user.</li>
     * <li>The active user has granted the application the rsvp_event extended permission.</li>
     * </ul>
     * 
     * @param eid
     *            The event ID.
     * @param rsvp_status
     *            The user's RSVP status. Specify attending, unsure, or declined.
     * 
     * @see http://wiki.developers.facebook.com/index.php/Events.rsvp
     */
    public boolean events_rsvp( Long eid, String rsvp_status ) throws FacebookException
    {
        Document doc = (fbClient.callMethod(new XmlFacebookMethod(Events.EVENTS_RSVP), "eid",eid,"rsvp_status",rsvp_status));
        return XmlFacebookParser.extractBoolean(doc);
    }
    
    public boolean event_cancel(Long eid,String message) throws FacebookException
    {
        Document doc = (fbClient.callMethod(new XmlFacebookMethod(Events.EVENTS_CANCEL), "eid",eid,"cancel_message",message));
        return XmlFacebookParser.extractBoolean(doc);
    }


	/////////////////////////////////////
	//////// facebook.groups.xxx ////////
	/////////////////////////////////////

	public List<Group > getGroups( long userId, long... groupIds ) throws FacebookException
	{
		Document doc =  ( fbClient.callMethod( new XmlFacebookMethod( Groups.GET), "uid", userId, "gids", groupIds ) );
		return XmlFacebookParser.parseGroup(doc, this);
	}

	public Group getGroup( long groupId ) throws FacebookException
	{
		List<Group> list = getGroups( getUserId(), groupId );
		return  list!=null && list.size()>0 ? list.get(0):null;
	}

	public GroupMembersByRole getGroupMembers( long groupId ) throws FacebookException
	{
		Document doc =  ( fbClient.callMethod( new XmlFacebookMethod( Groups.GET_MEMBERS), "gid", groupId ) );
		return XmlFacebookParser.parseGroupMembersByRole(doc);
	}
	
	public List<FacebookUser> getGroupMemebersinfo(long gid) throws FacebookException {
		return getGroupMembersinfo(gid,-1,-1);
	}
	
	public List<FacebookUser.SimpleFBUser> getGroupSimpleMembersinfo(long gid,long offset,long limit) throws FacebookException{
		String fql = "select uid,birthday,name,pic_square from user where uid in (" +
		"select uid from group_member where gid = "+gid ;
		
		if(offset != -1 && limit!=-1){
			fql = fql + " limit "+offset+","+limit;
		}		
		fql = fql + ")";
		
        Document doc = this.executeQuery(fql);
        return XmlFacebookParser.parseFacebookSimpleUserResponse(doc, this, false, false);
	}
	
	public List<FacebookUser> getGroupMembersinfo(long gid,long offset,long limit) throws FacebookException{
		String fql = "select uid,first_name,last_name,birthday,name,pic_square,pic, pic_small, sex,status from user where uid in (" +
		"select uid from group_member where gid = "+gid ;
		
		if(offset != -1 && limit!=-1){
			fql = fql + " limit "+offset+","+limit;
		}		
		fql = fql + ")";
		
        Document doc = this.executeQuery(fql);
        return XmlFacebookParser.parseFacebookUserResponse(doc, this, false, false);
	}
	
	public HashMap<Integer,Object> batch_run_getGroupMembersinfo_isGroupMember(long gid,long offset,long limit) throws FacebookException
	{   
	    HashMap<Integer,Object> map = new HashMap<Integer,Object>();
	    fbClient.beginBatch();
	    getGroupSimpleMembersinfo(gid,offset,limit);
	    isGroupMember(gid,getLogerInUserID());
	    List<? extends Object> list = fbClient.executeBatch(true);;    
	    int i=0;
	    map.put(i, XmlFacebookParser.parseFacebookSimpleUserResponse((Document)list.get(i),this, false, false));
	    i=1;
	    NodeList nodelist = list.get(i)!=null?((Document)list.get(i)).getElementsByTagName("group_member"):null;
	    map.put(i, (nodelist!=null && nodelist.getLength()>0)); 
	    return map;
	}
	
	public List<Group> getFriendsGroups(long uid) throws FacebookException{
		String friendid = "SELECT uid2 FROM friend WHERE uid1 = "+uid;
		String gidselect = " SELECT gid from  group_member where uid in ("+friendid+")";
		String groupsselect = "SELECT gid,name,nid,pic_small,substr(description,0,20),group_type,group_subtype,creator,update_time,venue from group where gid in ("+gidselect+")";	
		Document doc = this.executeQuery(groupsselect);
		return XmlFacebookParser.parseGroup(doc, this);
	}
	
	public List<Group> getFriendsGroups(long uid,int from,int offset) throws FacebookException{
		String friendid = "SELECT uid2 FROM friend WHERE uid1 = "+uid;
		String gidselect = " SELECT gid from  group_member where uid in ("+friendid+")";
		String groupsselect = "SELECT gid,name,nid,pic_small,substr(description,0,20),group_type,group_subtype,creator,update_time,venue from group where gid in ("+gidselect+")"+
		                       " LIMIT "+from+","+offset;	
		Document doc = this.executeQuery(groupsselect);
		
		return XmlFacebookParser.parseGroup(doc, this);
	}
	
	public List<Group> getMyGroups(long uid,int from,int offset) throws FacebookException{	
		String gidselect = " SELECT gid from  group_member where uid = "+uid;
		String groupsselect = "SELECT gid,name,nid,pic_small,substr(description,0,50),group_type,group_subtype,creator,update_time,venue from group where gid in ("+gidselect+")"+
		                       " LIMIT "+from+","+offset;	
		Document doc = this.executeQuery(groupsselect);		
		return XmlFacebookParser.parseGroup(doc, this);
	}
	
	public boolean isGroupMember(long gid,long uid) throws FacebookException
	{
	    String fql = " SELECT gid from group_member where gid="+gid+" and uid = "+uid;
	    Document doc = this.executeQuery(fql);
	    NodeList list = doc!=null?doc.getElementsByTagName("group_member"):null;
	    return (list!=null && list.getLength()>0);
	}
	
	public List<Group> getGroupByids(long[] gids) throws FacebookException
	{
	    StringBuffer gidselect = new StringBuffer();
	    for(int i = 0 ; i < gids.length ; i ++ )
	    {
	        if(i == gids.length-1)
	        {
	            gidselect.append(gids[i]);
	        }
	        else
	        {
	            gidselect.append(gids[i]+",");
	        }      
	    }
        String groupsselect = "SELECT gid,name,nid,pic_small,substr(description,0,50),group_type,group_subtype,creator,update_time,venue from group where gid in ("+gidselect.toString()+")";   
        Document doc = this.executeQuery(groupsselect);     
        return XmlFacebookParser.parseGroup(doc, this);
	}
	
	public long getGroupMembersize(long gid) throws FacebookException{
		Document doc =  ( fbClient.callMethod( new XmlFacebookMethod( Groups.GET_MEMBERS), "gid", gid ) );
		NodeList list = doc.getElementsByTagName("group_member");
		if(list!=null && list.getLength()>0) return list.getLength();
		else return 1;
	}
	
	public boolean groupsJoin(long gid,boolean join) throws FacebookException{
	    String confirm = "1";
	    if(join == false)
	    {
	        confirm = "0";
	    }
		Document doc =  ( fbClient.callMethod( new XmlFacebookMethod( Groups.GROUPS_JOIN), "gid", gid, "confirm", confirm ) );
	    return XmlFacebookParser.extractBoolean(doc);
	}

/////////////////////////////////////
	//////// facebook.photos.xxx ////////
	/////////////////////////////////////

	//TODO. seperate for xml
	public boolean addPhotoTags( PhotoTag... photoTags ) throws FacebookException
	{
		
		long pid = photoTags.length > 0 ? photoTags[0].getPid(): 0;
		/*
		FacebookJsonConfig<?> jsonConfig = FacebookJsonConfig.byFqlTable( "photo_tag" );
		String tags = (new JSONArray( photoTags, jsonConfig )).toString();
		*/
				
		Document doc =  fbClient.callMethod( new XmlFacebookMethod( Photos.ADD_TAG), "pid", pid, "tags", "" );
		return XmlFacebookParser.extractBoolean(doc);
	}
	
	public PhotoAlbum createPhotoAlbum(String name) throws FacebookException
	{
	    Document doc = fbClient.callMethod(new XmlFacebookMethod(Photos.CREATE_ALBUM), "name",name);
	    return XmlFacebookParser.parseCreateAlbum(doc);
	}

	public PhotoAlbum createPhotoAlbum( PhotoAlbum album ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Photos.CREATE_ALBUM), "name", album.name , "location", album.location, "description", album.description );
		
		return XmlFacebookParser.parseCreateAlbum(doc);
	}

	public List<Photo> getPhotos( int subjectId, String albumId, String... photoIds ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Photos.GET), "subj_id", subjectId, "aid", albumId, "pids", photoIds );
		return XmlFacebookParser.parseGetPhotos(doc);
	}

	public List<Photo> getPhotosById( String... photoIds ) throws FacebookException
	{
		Document doc =  fbClient.callMethod( new XmlFacebookMethod( Photos.GET), "pids", photoIds );
		return XmlFacebookParser.parseGetPhotos(doc);
	}
	
	final static String photoColStr = "pid,aid,owner,src_small,src_small_height,src_small_width,src_big,src_big_height,src_big_width,"+
	                                   "src,src_height,src_width,link,caption,created,modified,object_id";
	public List<Photo> getPhotoListById(String pid) throws FacebookException
	{
	    String fql = "select "+ photoColStr+ " from photo where aid in"+
	                  "(select aid from photo where pid='"+pid+"')";
        Document doc = executeQuery(fql);
        return XmlFacebookParser.parseGetPhotos(doc);	    
	}

	public Photo getPhotoById( String photoId ) throws FacebookException
	{
		 List<Photo> photos = getPhotosById( photoId );
		return photos!=null&&photos.size()>0?photos.get(0):null;
	}
	
	public HashMap<Integer,Object> batch_run_getAlbum_Photos(String albumId) throws FacebookException
	{
	    HashMap<Integer,Object> map = new HashMap<Integer,Object>();
	    FacebookClient client = this.getFbClient();
	    client.beginBatch();
	    getPhotoAlbum(albumId);
	    getPhotosByAlbum(albumId);  
	    
	    List<? extends Object> list = client.executeBatch(true);    
	    int i=0;
	    map.put(i, XmlFacebookParser.parsePhotoAlbum((Document)list.get(i)));
	    i=1;
	    map.put(i, XmlFacebookParser.parseGetPhotos((Document)list.get(i)));
	    
	    return map;
	}

	public List<Photo> getPhotosByAlbum( String albumId ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Photos.GET), "aid", albumId );
		return XmlFacebookParser.parseGetPhotos(doc);
	}

	public List<Photo> getPhotosOfUser() throws FacebookException
	{
		return getPhotosOfUser( getUserId() );
	}

	public  List<Photo> getPhotosOfUser( long userId ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Photos.GET), "subj_id", userId );
		return XmlFacebookParser.parseGetPhotos(doc);
	}

	public List<PhotoAlbum> getPhotoAlbums( long userId, long[] albumIds ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Photos.GET_ALBUMS), "uid", userId, "pids", albumIds );
		return XmlFacebookParser.parsePhotoAlbum(doc);
	}

	public List<PhotoAlbum> getPhotoAlbums( String... albumIds ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Photos.GET_ALBUMS), "aids", albumIds );
		return XmlFacebookParser.parsePhotoAlbum(doc);
	}

	public PhotoAlbum getPhotoAlbum( String albumId ) throws FacebookException
	{
		 List<PhotoAlbum> list = getPhotoAlbums( albumId );
		return list!=null&&list.size()>0?list.get(0):null;
	}

	public List<PhotoAlbum> getPhotoAlbumsByUser( long userId ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Photos.GET_ALBUMS), "uid", userId );
		return XmlFacebookParser.parsePhotoAlbum(doc);
	}
	public HashMap<Integer,Object> batch_run_getAlbumAndPhotoByUser(long userId)  throws FacebookException{
	    HashMap<Integer,Object> resultMap = new HashMap<Integer,Object>();
	    List<PhotoAlbum> photoalbum_list = new ArrayList<PhotoAlbum>();
        fbClient.beginBatch();
        getPhotoAlbumsByUser(userId);
        getAlbumCoverPhotoByUser(userId); 
        getPhotoByFql(userId);
        getOpenStreamFilter(userId);
        
        List<? extends Object> list = fbClient.executeBatch(true);    
        photoalbum_list = XmlFacebookParser.parsePhotoAlbum((Document)list.get(0));
        List<Photo> photo_list = XmlFacebookParser.parseAlbumCoverPhoto((Document)list.get(1));
        List<Photo> photos     = XmlFacebookParser.parseGetPhotos((Document)list.get(2));
        for(PhotoAlbum photoalbum : photoalbum_list)
        {
            for(Photo photo : photo_list)
            {
                if(photoalbum.aid.equals(photo.aid))
                {
                    photoalbum.cover_src_url = photo.src_small;
                    photo_list.remove(photo); //reduce the compare times
                    break;
                }
            }
        }  
        List<StreamFilter> stream_filter = XmlFacebookParser.parserStreamFilter((Document)list.get(3), null);
        
        resultMap.put(0, photoalbum_list);
        resultMap.put(1, photos);
        resultMap.put(2, stream_filter);
        return resultMap;
	}
	
	
	public List<PhotoAlbum> batch_run_getPhotoAlbumByUser(long userId) throws FacebookException
	{
	    List<PhotoAlbum> photoalbum_list = new ArrayList<PhotoAlbum>();
	    fbClient.beginBatch();
	    getPhotoAlbumsByUser(userId);
	    getAlbumCoverPhotoByUser(userId);   
	    List<? extends Object> list = fbClient.executeBatch(true);    
	    photoalbum_list = XmlFacebookParser.parsePhotoAlbum((Document)list.get(0));
	    List<Photo> photo_list = XmlFacebookParser.parseAlbumCoverPhoto((Document)list.get(1));
	    
	    for(PhotoAlbum photoalbum : photoalbum_list)
	    {
	        for(Photo photo : photo_list)
	        {
	            if(photoalbum.aid.equals(photo.aid))
	            {
	                photoalbum.cover_src_url = photo.src_small;
	                photo_list.remove(photo); //reduce the compare times
	                break;
	            }
	        }
	    }	    
	    return photoalbum_list;
	}
	
	public List<Photo> getPhotoByFql(long userId)throws FacebookException
	{
	    String fql = "select "+photoColStr +" from photo where owner="+userId+")";
        Document doc = executeQuery(fql);
        return XmlFacebookParser.parseGetPhotos(doc);
	}
	
	public List<Photo> getAlbumCoverPhotoByUser(long userId) throws FacebookException
	{
	    String fql = "select pid,src_small,aid from photo where pid in (select cover_pid from album where owner="+userId+")";
	    Document doc = executeQuery(fql);
	    return XmlFacebookParser.parseAlbumCoverPhoto(doc);
	}

	public List<PhotoAlbum> getPhotoAlbumsByUser() throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Photos.GET_ALBUMS ));
		return XmlFacebookParser.parsePhotoAlbum(doc);
	}

	public List<PhotoTag> getPhotoTags( long... photoIds ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Photos.GET_TAGS), "pids", photoIds );
		return XmlFacebookParser.parsePhotoTag(doc);
	}
	
	public Photo uploadPhoto( String filepath ) throws FacebookException
	{
		Log.d(TAG, "enter facebooksession uploadPhoto");
		try
		{
			Map<String, String>filePart = new HashMap<String, String>();
			filePart.put("data", filepath);
			Document doc =  fbClient.callUploadMethod( new XmlFacebookMethod(Photos.UPLOAD), filePart);
			//Log.d(TAG, "raw response "+fbClient.)
		    return XmlFacebookParser.parsePhotoUpload(doc);
		}
		catch ( Exception e )
		{
			throw new FacebookClientException( "Photo upload failed: file not found!", e );
		}		
	}
	
	public Photo uploadPhotoByAlbumid( String filepath,String albumid ) throws FacebookException
    {
        Log.d(TAG, "enter facebooksession uploadPhoto");
            
        Map<String,String> objparams = new HashMap<String,String>();
        if(!StringUtils.isEmpty(albumid) && !"-2".equals(albumid))
        {
            objparams.put("aid", albumid);
        }
        
        Document doc =  fbClient.callUploadMethod( new XmlFacebookMethod(Photos.UPLOAD), filepath,objparams);
        //Log.d(TAG, "raw response "+fbClient.)
        return XmlFacebookParser.parsePhotoUpload(doc);
        
    }
	
	public Photo uploadPhoto(String filepath,String caption,String albumid ) throws FacebookException
    { 
        Map<String,String> objparams = new HashMap<String,String>();
        if(!StringUtils.isEmpty(albumid) && !"-2".equals(albumid))
        {
            objparams.put("aid", albumid);
        }
        objparams.put("caption",caption);
        Document doc = fbClient.callUploadMethod( new XmlFacebookMethod(Photos.UPLOAD), filepath,objparams); 
        return XmlFacebookParser.parsePhotoUpload(doc);    
    }
	
	public Photo uploadPhoto(String filepath,String caption ) throws FacebookException
	{
		Map<String,String> objparams = new HashMap<String,String>();
        objparams.put("caption",caption);
		Document doc =  fbClient.callUploadMethod( new XmlFacebookMethod(Photos.UPLOAD), filepath,objparams);
		return XmlFacebookParser.parsePhotoUpload(doc);
	}

//	//TODO G/M: do we want this method?
//	public Photo uploadPhoto( long albumId, String caption, InputStream inputStream ) throws FacebookException
//	{	final byte[] byteArray = extractByteArray( inputStream );
//		FilePart[] filePart = new FilePart[]{ new FilePart( "data", new ByteArrayPartSource( "data", byteArray ) ) };
//		return fbClient.callUploadMethod( Photos.UPLOAD, filePart, "aid", albumId, "caption", caption );
//	}

//	private byte[] extractByteArray( InputStream inputStream ) throws FacebookException
//	{
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//		byte[] buffer = new byte[ 1024 ];
//		int read = 0;
//		try
//		{
//			while ( ( read = inputStream.read( buffer ) ) > 0 )
//			{
//				outputStream.write( buffer, 0, read );
//			}
//		}
//		catch ( IOException e )
//		{
//			throw new FacebookException( e );
//		}
//		finally
//		{
//			try
//			{
//				outputStream.flush();
//				outputStream.close();
//			}
//			catch ( IOException e )
//			{
//				throw new FacebookException( e );
//			}
//
//		}
//		return outputStream.toByteArray();
//	}
	//////////////////////////////////////////
	//////// facebook.marketplace.xxx ////////
	//////////////////////////////////////////

	public String[] getMarketplaceCategories() throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Marketplace.GET_CATEGORIES ));
		
		return null;
	}

	public String[] getMarketplaceSubcategories( String category ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Marketplace.GET_SUBCATEGORIES), "category", category );
		return null;
	}

	public Listing[] getMarketplaceListingsByUser() throws FacebookException
	{
		return getMarketplaceListingsByUsers( getUserId() );
	}

	public Listing[] getMarketplaceListingsByUsers( long... userIds ) throws FacebookException
	{
		return getMarketplaceListings( null, userIds );
	}
	
	public Listing getMarketplaceListing( long listingId ) throws FacebookException
	{
		return ArrayUtils.firstOrNull( getMarketplaceListings( listingId ) );
	}

	public Listing[] getMarketplaceListings( long... listingIds ) throws FacebookException
	{
		return getMarketplaceListings( listingIds, null );
	}

	public Listing[] getMarketplaceListings( long[] listingIds , long[] userIds ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Marketplace.GET_LISTINGS), "listing_ids", listingIds, "uids", userIds );
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Listing[] findMarketplaceListings( String category, String subcategory, String queryString ) throws FacebookException
	{
		Document doc =  fbClient.callMethod( new XmlFacebookMethod( Marketplace.SEARCH), "category", category, "subcategory", subcategory, "query", queryString );
		return null;
	}

	/**
	 * facebook.marketplace.createListing
	 */
	public long createMarketplaceListing( Listing listing, boolean showOnProfile ) throws FacebookException
	{
		return updateMarketplaceListing( listing, 0, showOnProfile );
	}

	/**
	 * facebook.marketplace.createListing
	 */
	public long updateMarketplaceListing( Listing listing, boolean showOnProfile ) throws FacebookException
	{
		return updateMarketplaceListing( listing, listing.getId() , showOnProfile );
	}

	//TODO, how to seperate
	private long updateMarketplaceListing( Listing listing, long listingId, boolean showOnProfile ) throws FacebookException
	{		
		String listingAttrs = listing.toString();
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Marketplace.CREATE_LISTING), "listing_id", listingId, "show_on_profile", showOnProfile, "listing_attrs", listingAttrs );
		
		return 0;
	}
	
	/**
	 * facebook.marketplace.removeListing
	 */
	//TODO ?: is 'status' an Enum??
	public boolean removeMarketplaceListing( long listingId, String status ) throws FacebookException
	{
		Document doc =  fbClient.callMethod( new XmlFacebookMethod( Marketplace.REMOVE_LISTING), "listing_id" , listingId, "status", status );
		return false;
	}
		
	public List<Page> getPageInfo( String... fields ) throws FacebookException
	{
		String fieldNames = StringUtils.join( fields, "," );
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Pages.GET_INFO), "fields", fieldNames );
		
		return XmlFacebookParser.parsePageinfoResponse(doc);
	}
	
	public Page getPageInfo( long pageId, String... fields ) throws FacebookException
	{
		List<Page> list = getPageInfo(new long[]{pageId},fields);
		return  list!=null && list.size()>0 ? list.get(0):null;
		
	}

	public List<Page> getPageInfo( long[] pageIds, String... fields ) throws FacebookException
	{
		String fieldNames = StringUtils.join( fields, "," );
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Pages.GET_INFO), "page_ids", pageIds, "fields", fieldNames );
		
		return XmlFacebookParser.parsePageinfoResponse(doc);
	}

	public List<Page> getPageInfo( long[] pageIds, long userId, String type, Page.Field... fields ) throws FacebookException
	{
		String fieldNames = StringUtils.join( fields, "," );
		Document doc =  fbClient.callMethod( new XmlFacebookMethod( Pages.GET_INFO), "page_ids", pageIds, "uid", userId, "type", type, "fields", fieldNames );		
		return XmlFacebookParser.parsePageinfoResponse(doc);
	}
	
	private static String page_col = " page_id,name,pic_small,pic_big,pic_square,pic,pic_large,page_url,type,website,"+
	                                 "has_added_app,founded,company_overview,mission,products,location ";
	public int getPage(int limit,int offset,long uid, ObjectHandler hanlder) throws FacebookException
	{
	    String fql = " select "+page_col+" from page where page_id in "+
	                  "( select target_id from connection where source_id="+uid+" and target_type='page')"+
	                  " limit "+offset+","+limit;
	    Document doc = executeQuery(fql);
	    return XmlFacebookParser.parsePageinfoResponse(doc, hanlder);
	}
	
	
	// TODO : check the name
	public boolean isAppAddedToPage() throws FacebookException
	{
		Document doc =  fbClient.callMethod( new XmlFacebookMethod( Pages.IS_APP_ADDED ));
		
		return false;
	}

	// TODO : check the name
	public boolean isAppAddedToPage( long pageId ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Pages.IS_APP_ADDED), "page_id", pageId );
		
		return false;
	}
	
	public boolean isPageAdmin( long pageId ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Pages.IS_ADMIN), "page_id", pageId );
		
		return false;
	}

	public boolean isPageFan( long pageId ) throws FacebookException
	{
		Document doc = fbClient.callMethod( new XmlFacebookMethod( Pages.IS_FAN), "page_id", pageId );
		
		return false;
	}

	public boolean isPageFan( long pageId, long userId ) throws FacebookException
	{
		Document doc =  fbClient.callMethod( new XmlFacebookMethod( Pages.IS_FAN), "page_id", pageId, "uid", userId );
		
		return false;
	}
	
	
	/********************add by jessie ************/
	/*
	 * phonebook.lookup
	 * required params: String session_key; string api_key;  float call_id;
	 *                  string sig;string v; array entries;
	 * optional params: string 	callback; string format;
	 * entries example:[{"email":"tormasliu@gmail.com"},{"email":"kongkong2046@gmail.com"}]
	 */
	public List<PhoneBook> phoneBookLookup(String entries,String callback) throws FacebookException{
		Document doc = fbClient.callMethod(new XmlFacebookMethod(Phonebook.PHONEBOOK_LOOKUP), "entries",entries ,"callback",null); 		
		return XmlFacebookParser.parsePhonebookLookupResponse(doc);
	}
	
	/**
	 * facebook.friends_add
	 * required params: String session_key; string api_key;  float call_id;
	 *                  string sig;string v; long uid;
	 * optional params: string 	callback; string format,String message
	 * @param uid
	 * @return
	 * @throws FacebookException
	 */
	public boolean phoneBookRequest(long uid) throws FacebookException{
		Document doc = fbClient.callMethod(new XmlFacebookMethod(Phonebook.PHONEBOOK_REQUEST), "uid",uid);
		return XmlFacebookParser.extractBoolean(doc);
	}
	
	/**
	 * 
	 * @param email
	 * @param cell
	 * @return
	 * @throws FacebookException
	 */
    public boolean setContactInfo(String email,String cell,String other) throws FacebookException{   	
    	HashMap<String,String> map = new HashMap<String,String>();
    	if(email!=null && !email.equals("")){
    		map.put("email", email);
    	}
    	if(other!=null && !other.equals("")){
    		map.put("other", other);
    	}
    	if(cell!=null && !cell.equals("")){
    		map.put("cell", cell);
    	}
		Document doc = fbClient.callMethod(new XmlFacebookMethod(Users.SET_CONTACT_INFO), map);
	    return XmlFacebookParser.extractBoolean(doc);	
    }
    
    
    /**
     * 
     * @return
     * @throws FacebookException
     */
    public boolean friendsAdd(long uid,String message) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Friends.FRIENDS_ADD),"uid",uid,"message",null);
	    return XmlFacebookParser.extractBoolean(doc);	
    }
    
    /**
     * @param uid
     * @param body
     * @return
     * @throws FacebookException
     */
    public boolean wallpost(long uid,String body) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(WALL.WALL_POST), "to",String.valueOf(uid),"body",body);
    	return true;
    }
    
    /**
     * uid Optional - The user whose wall to get
     * start 	Optional - Starting offset
     *  limit 	Optional - Max to return (20 maximum)
     *	uid_from 	Optional - Only return posts by uid_from
     * @return
     * @throws FacebookException
     */
    public List<Wall> wallget(long uid, Object...params) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(WALL.WALL_GET), "uid", uid, params);
    	return XmlFacebookParser.parseWall(doc, this, false);
    }
    
    public List<Wall> wallget(long uid, long fromuid) throws FacebookException{
        Document doc = fbClient.callMethod(new XmlFacebookMethod(WALL.WALL_GET), "uid", uid,"uid_from",fromuid);
        return XmlFacebookParser.parseWall(doc, this, false);
    }
    
    public List<Wall> wallget(int start, int limit) throws FacebookException
    {
    	return wallget(this.getLogerInUserID(), start,limit,true);
    }
    
    public List<Wall> wallget(long uid, int start, int limit,Boolean hasprogress) throws FacebookException
    {
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(WALL.WALL_GET), "uid", uid, "start", start, "limit", limit);
    	return XmlFacebookParser.parseWall(doc, this, hasprogress.booleanValue());
    }
    
    public List<MessageThreadInfo> message_getThreadsInfo(int folder_id ,long uid,int limit,int offset,boolean hasprogress) throws FacebookException
    {   
        String fql = "select " +
        		"thread_id,        folder_id,       subject," +
        		"recipients,       updated_time,    parent_message_id," +
        		"parent_thread_id, message_count,   snippet," +
        		"snippet_author ,unread,object_id," +
        		"viewer_id  from thread WHERE folder_id = "+ folder_id  +"  LIMIT "+limit+" OFFSET "+offset;
        Document doc = this.executeQuery(fql);
       
        //Document doc = fbClient.callMethod(new XmlFacebookMethod(Message.MESSAGE_GETTHREAD_INFO),params);
        return XmlFacebookParser.parseMessageThreadInfo(doc, this,hasprogress);
    }
     
    public List<MailboxThread> mailbox_getInbox() throws FacebookException{
    	return mailbox_getInbox(0,20);
    }
    
    public List<MailboxThread> mailbox_getInbox(int start) throws FacebookException{
    	 return mailbox_getInbox(start,20);
    }
    
    public List<MailboxThread> mailbox_getInbox(int start,int limit) throws FacebookException{ //limit<20
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Mailbox.MAILBOX_GETINBOX),"start",start,"limit",limit);
    	return XmlFacebookParser.parseMailboxThread(doc, this, true);
    }
    public List<MailboxThread> mailbox_getInbox(int start,int limit, boolean hasProgress) throws FacebookException{ //limit<20
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Mailbox.MAILBOX_GETINBOX),"start",start,"limit",limit);
    	return XmlFacebookParser.parseMailboxThread(doc, hasProgress==true?this:null, true);
    }
    
    public List<MailboxThread> mailbox_getOutbox() throws FacebookException{
    	return mailbox_getOutbox(0,20);
    }
    
    public List<MailboxThread> mailbox_getOutbox(int start) throws FacebookException{
    	 return mailbox_getOutbox(start,20);
    }
    
    public List<MailboxThread> mailbox_getOutbox(int start,int limit) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Mailbox.MAILBOX_GETOUTBOX),"start",start,"limit",limit);
    	return XmlFacebookParser.parseMailboxThread(doc, this, false);
    }
    
    public List<MailboxThread> mailbox_getOutbox(int start,int limit, boolean hasProgress) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Mailbox.MAILBOX_GETOUTBOX),"start",start,"limit",limit);
    	return XmlFacebookParser.parseMailboxThread(doc, hasProgress==true?this:null, false);
    }
    
    public MailboxThread mailbox_getThread(int tid) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Mailbox.MAILBOX_GETINBOX),"tid",tid);
    	return ArrayUtils.firstOrNull((MailboxThread[])XmlFacebookParser.parseMailboxThread(doc, this, false).toArray());
    }
    
    public List<MailboxMessage> mailbox_getThreadMessage(long tid,int start) throws FacebookException{
    	return mailbox_getThreadMessage(tid,start,20);
    }
    

    public List<MailboxMessage> mailbox_getThreadMessage(long tid) throws FacebookException{
    	return mailbox_getThreadMessage(tid,0,20);
    }
    

    public List<MailboxMessage> mailbox_getThreadMessage(long tid,int start,int limit) throws FacebookException
    {
        long []tids = new long[1];
        tids[0] = tid;
        return mailbox_getThreadMessage(tids, start, limit);
        /*
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Mailbox.MAILBOX_GETTHTEADMESSAGES),"tid",tid,"start",start,"limit",limit);
    	List<MailboxMessage> list =  XmlFacebookParser.parseMailboxMessage(doc, this);
    	return list;
    	*/
    }
    
    public List<MailboxMessage> mailbox_getThreadMessage(long[] tids,int start,int limit) throws FacebookException
    {
        StringBuffer fql_buf = new StringBuffer();
        fql_buf.append("select message_id,thread_id,author_id,body,created_time,attachment,viewer_id from message where thread_id in (" +
                      ArrayUtils.join(tids)+") ");       
//        if(start>-1)
//        {
//            fql_buf.append(" and created_time > " + start);
//        }        
        if(limit>-1)
        {
            //LIMIT "+limit+" OFFSET "+offset;
            fql_buf.append(" LIMIT "+limit+" OFFSET "+start);
        }        
        Document doc = this.executeQuery(fql_buf.toString()); 
        List<MailboxMessage> list =  XmlFacebookParser.parseMailMessage(doc.getDocumentElement());       
        return list;
    }
    
    public long mailbox_send(long[] to, String subject,String body) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Mailbox.MAILBOX_SEND),"to",to,"subject",subject,"body",body);
    	return XmlFacebookParser.extractLong(doc);
    }
    
    public long mailbox_send(long[] to, String subject,String body,String[] emails) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Mailbox.MAILBOX_SEND),"to",to,"subject",subject,"body",body,"emails",emails);
    	return XmlFacebookParser.extractLong(doc);
    }
    
    public long mailbox_replay(long tid,String body) throws FacebookException {
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Mailbox.MAILBOX_REPLY),"tid",tid, "body",body);
    	return XmlFacebookParser.extractLong(doc);
    }
    
    public long mailbox_replay(int tid,int mid,String body) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Mailbox.MAILBOX_REPLY),"tid",tid,"mid",mid,"body",body);
    	return XmlFacebookParser.extractLong(doc);
    }
    
    public boolean mailbox_markread(long tid) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Mailbox.MAILBOX_MARKREAD),"tid",tid);
    	return XmlFacebookParser.extractBoolean(doc);
    	
    }
    
    public boolean mailbox_markunread(long tid) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Mailbox.MAILBOX_MARKUNREAD),"tid",tid);
    	return XmlFacebookParser.extractBoolean(doc);
    }
    
    public Video  vedio_post(String  filepath,String title,String description) throws FacebookException{
		Log.d(TAG,"entering vedio_post method");
		Map<String,String> params = new HashMap<String,String>();
		if(title!=null && !title.equals("")){
		    params.put("title", title);
		}
		if(description!=null && !description.equals("")){
		    params.put("description",description);
		}
		Document doc =  fbClient.callUploadMethod(new XmlFacebookMethod(FacebookMethod.Video.VIDEO_UPLOAD),filepath,params);
		return XmlFacebookParser.parserVideoUpload(doc);		
    }
    
    /**
     * required parameters: uid  url comment
     * optional parameters:callback,format,session_key
     * @throws FacebookException 
     */
    public long links_post(long uid,String url,String comment) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Link.SHARE_LINK), "uid",uid,"url",url,"comment",comment);
    	return XmlFacebookParser.extractLong(doc);
    }
    
    public long links_post(String url,String comment) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Link.SHARE_LINK), "uid",String.valueOf(getLogerInUserID()),"url",url,"comment",comment);
    	return XmlFacebookParser.extractLong(doc);
    }
    
    public boolean pokes_post(long uid) throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Poke.POKE_POST), "uid",uid);
    	return XmlFacebookParser.extractBoolean(doc);
    }
    public List<PokeResponse> pokes_get() throws FacebookException{
    	Document doc = fbClient.callMethod(new XmlFacebookMethod(Poke.POKE_GET));
    	return XmlFacebookParser.parserPokeResponse(doc,false,this);
    }
    
    public List<PokeResponse> pokes_get( boolean hasProgress) throws FacebookException
    {
        Document doc = fbClient.callMethod(new XmlFacebookMethod(Poke.POKE_GET));
        return XmlFacebookParser.parserPokeResponse(doc,hasProgress,this);
    }
   public List<FacebookUser> getFriendList(long uid) throws FacebookException{
    	String fql = "select uid,first_name,last_name,birthday,name,pic_square,pic, pic_small, sex,status from user where uid in (" +
		"SELECT uid2 FROM friend WHERE uid1 = "+uid+")";
         Document doc = this.executeQuery(fql);
         boolean isFriend = (uid == this.getLogerInUserID());
         return XmlFacebookParser.parseFacebookUserResponse(doc,this, true, isFriend);
    }
   
   public List<FacebookUser> getFriendList(long uid, boolean hasProgress) throws FacebookException{
   	String fql = "select uid,first_name,last_name,birthday,name,pic_square,pic, pic_small,sex,status from user where uid in (" +
		"SELECT uid2 FROM friend WHERE uid1 = "+uid+")";
        Document doc = this.executeQuery(fql);
        boolean isFriend = (uid == this.getLogerInUserID());
      return XmlFacebookParser.parseFacebookUserResponse(doc,this, hasProgress, isFriend);
   }
   
   String[] monthArray = {"January","February","March","April","May","June","Junly","August","September","December","Octomber","Nobember"};
   public List<FacebookUser> getUpcomingBDFriendList(long uid, boolean hasProgress) throws FacebookException{	 
	   String curMonth = monthArray[new Date().getMonth()];
	   int curMonthlength = curMonth.length();
	   
	   String fql = "select uid,first_name,last_name,birthday,name,pic_square,pic, pic_small,sex,status from user where uid in (" +
		"SELECT uid2 FROM friend WHERE uid1 = "+uid+") and substr(birthday,0,"+curMonthlength+")='"+curMonth+"' order by birthday desc";
	   
	   Log.d(TAG,"upcoming BD Friendslist fql =="+fql);
	   Document doc = this.executeQuery(fql);	   
      return XmlFacebookParser.parseFacebookUserResponse(doc,this, hasProgress, true);
   }
   
   public List<FacebookUser> getBDFriendList(long uid, boolean hasProgress) throws FacebookException{	 
	   String fql = "select uid,first_name,last_name,birthday,name,pic_square,pic, pic_small,sex,status from user where uid in (" +
		"SELECT uid2 FROM friend WHERE uid1 = "+uid+") and birthday != ''  order by birthday desc";   
	   Log.d(TAG,"upcoming BD Friendslist fql =="+fql);
	   Document doc = this.executeQuery(fql);	   
      return XmlFacebookParser.parseFacebookUserResponse(doc,this, hasProgress, true);
   }
   
   public List<FacebookUser.SimpleFBUser> getBDSimpleFriendList(long uid, boolean hasProgress) throws FacebookException{	 
	   String fql = "select uid,birthday,name,pic_square from user where uid in (" +
		"SELECT uid2 FROM friend WHERE uid1 = "+uid+") and birthday != ''  order by birthday desc";   
	   Log.d(TAG,"upcoming BD Friendslist fql =="+fql);
	   Document doc = this.executeQuery(fql);	   
      return XmlFacebookParser.parseFacebookSimpleUserResponse(doc,this, hasProgress, true);
   }
   
   public List<FacebookUser> getFriendList(long uid,int limit,int offset, boolean hasProgress, ObjectHandler handler) throws FacebookException{
	   String fql = "select uid,first_name,last_name,birthday,name,pic_square,pic, pic_small,sex,status from user where uid in (" +
		"SELECT uid2 FROM friend WHERE uid1 = "+uid+") LIMIT "+limit+" OFFSET "+offset;
        Document doc = this.executeQuery(fql);
        boolean isFriend = (uid==this.getLogerInUserID());
      return XmlFacebookParser.parseFacebookUserResponse(doc,this, hasProgress,isFriend, handler);
   }
   
   public int  getFriendList(long uid,int limit,int offset, ObjectHandler handler) throws FacebookException{
	    String fql = "select uid,first_name,last_name,birthday,name,pic_square,pic, pic_small,sex,status from user where uid in (" +
		"SELECT uid2 FROM friend WHERE uid1 = "+uid+") LIMIT "+limit+" OFFSET "+offset;
        Document doc = this.executeQuery(fql);
        boolean isFriend = (uid==this.getLogerInUserID());
        return XmlFacebookParser.parseFacebookUserResponse(doc,this, handler);
   }
   
   
   public List<UserStatus> getAllMyFriendsLastedStatus(int from,int offset) throws FacebookException{
	   String fql = "select uid, name, status from user where uid in (" +
		"SELECT uid2 FROM friend WHERE uid1 = "+getLogerInUserID()+") ORDER BY status.time desc LIMIT "+from+","+offset;
	   Document doc = executeQuery(fql);
	   return XmlFacebookParser.parseAllMyFriendsLastedStstus(doc, this);
   }
   
   public  List<Stream> getOpenStream(long start_time, int limit,boolean newpost, String filter) throws FacebookException
   {
	   if(newpost)
	   {
		   Document doc = fbClient.callMethod(new XmlFacebookMethod(OpenStream.STREAM_GET), "viewer_id", this.getLogerInUserID(),"limit", limit, "start_time", start_time/1000, "filter_key", filter);
	   	   return XmlFacebookParser.parserStream(doc, this);
	   }
	   else
	   {
		   Document doc = fbClient.callMethod(new XmlFacebookMethod(OpenStream.STREAM_GET), "viewer_id", this.getLogerInUserID(),"limit", limit, "end_time", start_time/1000,"filter_key", filter);
	   	   return XmlFacebookParser.parserStream(doc, this);
	   }	   
   }
   
   public  List<StreamFilter> getOpenStreamFilter(long uid) throws FacebookException
   {
	   
        Document doc = fbClient.callMethod(new XmlFacebookMethod(OpenStream.STREAM_GET_Filters), "uid", uid);
	    return XmlFacebookParser.parserStreamFilter(doc, this);	   	   
   }
   
  /**
   * get wall message by FQL 
   * @param start_time
   * @param limit
   * @return
   * @throws FacebookException
   */
   public List<Stream> getWallByfql(long uid,long start_time,int limit, boolean newpost) throws FacebookException
   {
	   String fql = "select post_id,viewer_id,app_id,source_id,updated_time,created_time,filter_key,"+
       "attribution,actor_id,target_id,app_data,message,action_links,attachment,comments,likes,"+
       "privacy,type,permalink,tagged_ids,is_hidden from stream  where source_id="+uid;
		if(start_time>0)
		{
			if(newpost == true)
			{
		       fql = fql + " and updated_time>="+start_time/1000;
			}
			else
			{
				fql = fql + " and updated_time <"+start_time/1000;
			}
		}
		if(limit>0)
		{
		   fql = fql + " limit 0,"+limit;
		}		
		Document doc = executeQuery(fql);
		return XmlFacebookParser.parserStream(doc, this);
   }
   
   public List<Stream> getOpenStreamByfql(long start_time,int limit) throws FacebookException
   {
       String fql = "select post_id,viewer_id,app_id,source_id,updated_time,created_time,filter_key,"+
                     "attribution,actor_id,target_id,app_data,message,action_links,attachment,comments,likes,"+
                     "privacy,type,permalink,tagged_ids,is_hidden from stream  where source_id in (" +
                     "SELECT uid2 FROM friend WHERE uid1 = "+this.getLogerInUserID()+") or source_id="+this.getLogerInUserID();
       if(start_time>0)
       {
           fql = fql + " and updated_time>="+start_time/1000;
       }
       if(limit>0)
       {
           fql = fql + " limit 0,"+limit;
       }
       
       Document doc = executeQuery(fql);
       return XmlFacebookParser.parserStream(doc, this);
   }
   
   public Comments getComments(String postId) throws FacebookException
   {
       Document doc = fbClient.callMethod(new XmlFacebookMethod(OpenStream.STREAM_GET_COMMENTS), "post_id",postId);
       return XmlFacebookParser.parserStreamComments(doc,-1,this);
   }
   
   public Comments getComments(String postId,long source_id, int offset, int limit) throws FacebookException
   {
	   String fql = "select xid, object_id, post_id, fromid, time, text, id, username, reply_xid " +
	   		        "from comment where post_id ='" +postId +"'" +" LIMIT "+limit+" OFFSET "+offset;;
	   		        
       Document doc = executeQuery(fql);
       //Document doc = fbClient.callMethod(new XmlFacebookMethod(OpenStream.STREAM_GET_COMMENTS), "post_id",postId);
       return XmlFacebookParser.parserStreamComments(doc,source_id,this);
   }
   
   public List<PhotoComment> getPhotoComment(String pid) throws FacebookException
   {
       Document doc = fbClient.callMethod(new XmlFacebookMethod(Photos.GET_COMMENTS), "pid",pid);
       return XmlFacebookParser.parserPhotoComments(doc,this);
   }
   
   public boolean addPhotoComment(String pid,String body) throws FacebookException
   {
       Document doc = fbClient.callMethod(new XmlFacebookMethod(Photos.ADD_COMMENTS), "pid",pid,"body",body);
       return XmlFacebookParser.extractBoolean(doc);
   }
   
   
   public boolean editPhoto(String pid,String caption) throws FacebookException
   {
       Document doc = fbClient.callMethod(new XmlFacebookMethod(Photos.EDIT_PHOTO), "pid",pid,"caption",caption);
       return XmlFacebookParser.extractBoolean(doc);
   }
   
   public boolean deleteAlbum(String aid) throws FacebookException
   {
       Document doc = fbClient.callMethod(new XmlFacebookMethod(Photos.DELETE_ALBUM), "aid",aid);
       return XmlFacebookParser.extractBoolean(doc);
   }
   
   public boolean deletePhoto(String pid) throws FacebookException
   {
       Document doc = fbClient.callMethod(new XmlFacebookMethod(Photos.DELETE_PHOTO), "pid",pid);
       return XmlFacebookParser.extractBoolean(doc);
   }
   
   public boolean editAlbum(String aid,String name,String description,String location,String visible) throws FacebookException
   {
       HashMap<String,String> map = new HashMap<String,String>();
       if(StringUtils.isEmpty(aid))
       {
           return false;
       }
       else
       {
           map.put("aid", aid);
       }
       if(!StringUtils.isEmpty(name))
       {
           map.put("name", name);
       }
       if(!StringUtils.isEmpty(description))
       {
           map.put("description",description);
       }
       if(!StringUtils.isEmpty(location))
       {
           map.put("location", location);
       }
       if(!StringUtils.isEmpty("visible"))
       {
           map.put("visible", visible);
       }
       Document doc = fbClient.callMethod(new XmlFacebookMethod(Photos.EDIT_ALBUM),map);
       return XmlFacebookParser.extractBoolean(doc);
   }
   
   public  String addComments(String post_id, String content) throws FacebookException
   {
	   Document doc = fbClient.callMethod(new XmlFacebookMethod(OpenStream.STREAM_ADD_COMMENTS), "post_id", post_id,"comment", content);
	   return XmlFacebookParser.extractString(doc);
   }
 
   public  boolean streamLike(String post_id) throws FacebookException
   {
	   Document doc = fbClient.callMethod(new XmlFacebookMethod(OpenStream.STREAM_ADD_LIKE), "uid", this.getLogerInUserID(), "post_id", post_id);
   	   return XmlFacebookParser.extractBoolean(doc);
   }
   
   public boolean removeComments(String comment_id) throws FacebookException
   {
       Document doc = fbClient.callMethod(new XmlFacebookMethod(OpenStream.STREAM_REMOVE_COMMENTS), "uid", this.getLogerInUserID(), "comment_id", comment_id);
       return XmlFacebookParser.extractBoolean(doc);
   }
   

   public  boolean streamUnLike(String post_id) throws FacebookException
   {
	   Document doc = fbClient.callMethod(new XmlFacebookMethod(OpenStream.STREAM_REMOVE_LIKE), "uid", this.getLogerInUserID(), "post_id", post_id);
   	   return XmlFacebookParser.extractBoolean(doc);
   }
   
   
   public Long stream_publish(String message,Attachment attachment,List<BundleActionLink> actionLinks, Long targetId, Long userId )throws FacebookException 
   {

       Map<String,String> params = new HashMap<String,String>();
       if(userId == null)
       {
           params.put("uid", Long.toString(getLogerInUserID()));    
       }
       else
       {
           params.put("uid", userId.toString());    
       }
       
       if (message!=null && !message.equals("")) {
            params.put("message", message );
       }

       // A JSON-encoded object containing the text of the post, relevant links, a media type (image, video, mp3, flash), as well as any other key/value pairs you may
       // want to add.
       if ( attachment != null ) {
           params.put("attachment", attachment.toJson().toString());
           Log.d(TAG," attachment string is "+attachment.toJson().toString());
       }

       // An array of action link objects, containing the link text and a hyperlink.
       JSONArray jsonActionLinks = new JSONArray();
       if ( actionLinks != null && !actionLinks.isEmpty() ) {
           for ( BundleActionLink actionLink : actionLinks ) {
               jsonActionLinks.put( actionLink.toJson() );
           }
       }

       // associate to param
       if ( jsonActionLinks.length() > 0 ) {
           params.put("action_links", jsonActionLinks.toString());
       }

       if ( targetId != null ) {
           params.put("target_id", targetId.toString());
       }
       
       Document doc = fbClient.callMethod(new XmlFacebookMethod(OpenStream.STREAM_PUBLISH), params);
       //return XmlFacebookParser.extractLong(doc);
       return 0L;
   }
   /**
    *
    * @param message
    * @param attachement
    * @param action_links
    * @param target_id
    * @return
    * @throws FacebookException
    */
   public long streampublish(String message,Map<String,Object> attachementMap,List<Map<String,String>> actionlinks,Long targetid,String mediatype) throws FacebookException
   {
	   Map<String,String> params = new HashMap<String,String>();
	   if(message!=null && !message.equals("")){
		   params.put("message", message);
	   }
	   
	   if(attachementMap!=null && attachementMap.size()>0){
		   // encapsulate JSON String
		   StringBuffer strBuf = new StringBuffer();
		   strBuf.append("{");
		   Iterator it =  attachementMap.entrySet().iterator();
		   while(it.hasNext()){    	  
	    	   Map.Entry<String,String> para = (Map.Entry<String, String>)it.next();
	    	   String key = para.getKey();
	    	   Object value = para.getValue();
               if(key.equals("name") && value!=null ){
            	    strBuf.append("\"name\":\""+String.valueOf(value)+"\",");
               }else if(key.equals("caption") && value!=null){
            	    strBuf.append("\"caption\":\""+String.valueOf(value)+"\",");
               }else if(key.equals("description") && value!=null && !"".equals(value)){
            	    strBuf.append("\"description\":\""+String.valueOf(value)+"\",");
               }else if(key.equals("href") && value!=null){
            	    strBuf.append("\"href\":\""+String.valueOf(value)+"\",");
               }else if(key.equals("media") && value!=null){
            	    List medialist = (List<Map<String,String>>)value;
            	    if(medialist.size()>0 && mediatype!=null){
            	      String mediastr =  getMediaJSONStr(medialist,mediatype);
            	      strBuf.append("\"media\":\""+mediastr+"\",");
            	    }
               }else if(key.equals("properties") && value!=null){          	 
            	    strBuf.append("\"properties\":\""+String.valueOf(value)+"\",");
               }else{}
	       }
		   
		   String jsonattachment = strBuf.toString();
           if(jsonattachment.endsWith(",")){
        	   jsonattachment = jsonattachment.substring(0,jsonattachment.length()-1);
           }
           jsonattachment = jsonattachment+"}";
           params.put("attachment", jsonattachment);
	   }
	   
	   if(actionlinks!=null && actionlinks.size()>0){
		   // encapsulate JSON String
		   StringBuffer strBuf2 = new StringBuffer();
		   strBuf2.append("[");
		   
		   Iterator it =  actionlinks.iterator();
		   while(it.hasNext()){    	  
	    	   Map<String,String> para = (Map<String, String>)it.next();
	    	   String text = para.get("text");
	    	   String href = para.get("href");
	    	   if(text!=null && href !=null ){
	    		   strBuf2.append("{");
	    		   strBuf2.append("\"text\":\""+text+"\",");
	    		   strBuf2.append("\"href\":\""+href+"\"");
	    		   strBuf2.append("},");
	    	   }        
	      }
		   String jsonactionlink = strBuf2.toString();
           if(jsonactionlink.endsWith(",")){
        	   jsonactionlink = jsonactionlink.substring(0,jsonactionlink.length()-1);
           }
           jsonactionlink = jsonactionlink+"]";
           params.put("action_links", jsonactionlink);
	   }
	   
	   if(targetid!=null){
		   params.put("target_id", String.valueOf(targetid));
	   }
       
	   //String attachment = "{\"name\":\"hello\",\"caption\":\"I dont know what is this\",\"description\":\"it s just a test so there is nothing to say\",\"href\":\"http://www.baidu.com\",\"properties\":{\"category\":{\"text\":\"wowow\",\"href\":\"http://www.google.com\"},\"ratings\":\"5 starts\"},\"media\":[{\"type\": \"image\", \"src\": \"http://icanhascheezburger.files.wordpress.com/2009/03/funny-pictures-kitten-finished-his-milk-and-wants-a-cookie.jpg\", \"href\": \"http://icanhascheezburger.com/2009/03/30/funny-pictures-awlll-gone-cookie-now/\"}, {\"type\": \"image\", \"src\": \"http://images.icanhascheezburger.com/completestore/2009/1/18/128768048603560273.jpg\", \"href\": \"http://ihasahotdog.com/upcoming/?pid=20869\"}]}";
   
	   //String actionlinks = "[{\"text\":\"Upload a video\",\"href\":\"http://www.youtube.com/my_videos_upload\"}]";
	   //Document doc = fbClient.callMethod(new XmlFacebookMethod(OpenStream.STREAM_PUBLISH), "message",message,"target_id","1624264093","attachment",attachment,"action_links",actionlinks);   
	   Document doc = fbClient.callMethod(new XmlFacebookMethod(OpenStream.STREAM_PUBLISH), params);
	   return 0;
	   //return XmlFacebookParser.extractLong(doc);
   }
   
   public String getMediaJSONStr(List<Map<String,String>> list,String mediatype){
	   StringBuffer strBuf = new StringBuffer();
	   strBuf.append("[");
	   Iterator it = list.iterator();
	   while(it.hasNext()){    	  
    	   Map<String,String> para = (Map<String, String>)it.next();
    	   //String text = para.get("type");
    	   if(mediatype.equals("image")){
    		   String imgelementstr = createImageMediaStr(para);
    		   strBuf.append(imgelementstr);
    		   strBuf.append(",");
    	   }else if(mediatype.equals("flash")){
    		   String flashelementstr = createFlashMediaStr(para);
    		   strBuf.append(flashelementstr);
    		   strBuf.append(",");
    	   }else if(mediatype.equals("mp3")){
    		   String mp3elementstr = createMp3MediaStr(para);
    		   strBuf.append(mp3elementstr);
    		   strBuf.append(",");
    	   }else if(mediatype.equals("video")){
    		   String videoelementstr = createVideoMediaStr(para);
    		   strBuf.append(videoelementstr);
    		   strBuf.append(",");
    	   }else{}
    	   
      }
	   
	   String mediastr = strBuf.toString();
       if(mediastr.endsWith(",")){
    	   mediastr = mediastr.substring(0,mediastr.length()-1);
       }
       mediastr = mediastr+"]";
       return mediastr;
   }
   
   public String createImageMediaStr(Map<String,String>para){
	   StringBuffer strBuf = new StringBuffer();
	   strBuf.append("{\"type\":\"image\"");
	   if(para.get("src")!=null){
		   strBuf.append(",\"src\":\""+para.get("src")+"\"");
	   }else if(para.get("href")!=null){
		   strBuf.append(",\"href\":\""+para.get("href")+"\"");
	   }
	   strBuf.append("}");
	   return strBuf.toString();
   }
   
   public String createFlashMediaStr(Map<String,String>para){
	   StringBuffer strBuf = new StringBuffer();
	   strBuf.append("{\"type\":\"flash\"");
	   if(para.get("swfsrc")!=null){
		   strBuf.append(",\"swfsrc\":\""+para.get("swfsrc")+"\"");
	   }else if(para.get("imgsrc")!=null){
		   strBuf.append(",\"imgsrc\":\""+para.get("imgsrc")+"\"");
	   }else if(para.get("width")!=null){
		   strBuf.append(",\"width\":"+Integer.parseInt(para.get("width")));
	   }else if(para.get("height")!=null){
		   strBuf.append(",\"height\":"+Integer.parseInt(para.get("height")));
	   }else if(para.get("expanded_width")!=null){
		   strBuf.append(",\"expanded_width\":"+Integer.parseInt(para.get("expanded_width")));
	   }else if(para.get("expanded_height")!=null){
		   strBuf.append(",\"expanded_height\":"+Integer.parseInt(para.get("expanded_height")));
	   }
	   strBuf.append("}");
	   return strBuf.toString();
   }
   
   public String createMp3MediaStr(Map<String,String>para){
	   StringBuffer strBuf = new StringBuffer();
	   strBuf.append("{\"type\":\"mp3\"");
	   if(para.get("src")!=null){
		   strBuf.append(",\"src\":\""+para.get("src")+"\"");
	   }else if(para.get("title")!=null){
		   strBuf.append(",\"title\":\""+para.get("title")+"\"");
	   }else if(para.get("artist")!=null){
		   strBuf.append(",\"artist\":\""+para.get("artist")+"\"");
	   }else if(para.get("album")!=null){
		   strBuf.append(",\"album\":\""+para.get("album")+"\"");
	   }
	   strBuf.append("}");
	   return strBuf.toString();
   }
   
   public String createVideoMediaStr(Map<String,String>para){
	   StringBuffer strBuf = new StringBuffer();
	   strBuf.append("{\"type\":\"video\"");
	   if(para.get("video_src")!=null){
		   strBuf.append(",\"video_src\":\""+para.get("video_src")+"\"");
	   }else if(para.get("preview_img")!=null){
		   strBuf.append(",\"preview_img\":\""+para.get("preview_img")+"\"");
	   }else if(para.get("video_link")!=null){
		   strBuf.append(",\"video_link\":\""+para.get("video_link")+"\"");
	   }
	   strBuf.append("}");
	   return strBuf.toString();
   }

public boolean requestFriend(Long uid) throws FacebookException {
	Document doc = fbClient.callMethod(new XmlFacebookMethod(Friends.FRIENDS_ADD), "uid",uid.toString());	
	return XmlFacebookParser.extractBoolean(doc);
}

public boolean isMyFriend(Long uid) throws FacebookException{
	long[] uids1 = {uid};
	long[] uids2 = {getLogerInUserID()};
    Document doc = fbClient.callMethod(new XmlFacebookMethod(Friends.ARE_FRIENDS), "uids1",uids1,"uids2",uids2);
	return XmlFacebookParser.isMyFriend(doc);
}

public List<FriendRelationship> areMyFriends(List<Long> users) throws FacebookException{
	long uid =  getLogerInUserID();
	if(users!=null && users.size()>0){
		long[] uids1 = new long[users.size()];
		long[] uids2 = new long[users.size()];
		for(int i = 0 ;i< users.size();i++){
			uids1[i] = uid;
			uids2[i] = users.get(i);
		}	
		Document doc = fbClient.callMethod(new XmlFacebookMethod(Friends.ARE_FRIENDS), "uids1",uids1,"uids2",uids2);
		return XmlFacebookParser.areMyFriends(doc);
	}
	
	return null;
}


public boolean requestPhoneNumber(long uid) throws FacebookException{
	Document doc = fbClient.callMethod(new XmlFacebookMethod(Phonebook.PHONEBOOK_REQUEST),"target",uid);	
	return XmlFacebookParser.extractBoolean(doc);
}

public FBNotifications geNotifications(long uid)throws FacebookException{
	
    Document doc = fbClient.callMethod(new XmlFacebookMethod(Notifications.GET),"target",uid);	
	return XmlFacebookParser.parseNotifications(doc);
	
}

public FBNotifications batch_run_getNotifications(long uid) throws FacebookException
{
    HashMap<Integer,Object> map = new HashMap<Integer,Object>();
    fbClient.beginBatch();
    geNotifications(uid);
    getUpcomingEventsNotificationEid(uid);
    List<? extends Object> list = fbClient.executeBatch(true);   
    int i=0;
    FBNotifications notification = XmlFacebookParser.parseNotifications((Document)list.get(i));
    i=1;
    List<Long> eids = XmlFacebookParser.parseEventID((Document)list.get(i));
    notification.entInvite.uids = eids;
    return notification;
}

private List<Long> getUpcomingEventsNotificationEid(long uid) throws FacebookException{
   String fql1 = "select  eid  from event_member where rsvp_status='not_replied' and uid="+uid;
   String fql2 = "select eid from event where end_time>="+DateUtil.getCurrentTimeForEvent()/1000+
                  " and eid in ("+fql1+")";  
   
   Document doc = executeQuery(fql2);
   return XmlFacebookParser.parseEventID(doc); 
}

public List<oms.sns.service.facebook.model.Notifications.AppInfo> getApplicationInfo(String appids) throws FacebookException
{
    Document doc;
    if(StringUtils.isEmpty(appids) == false)
    {
        String fql = "select app_id,api_key,canvas_name,display_name,icon_url,logo_url,company_name from application where app_id in ("+appids+")";
        doc = executeQuery(fql);
        return XmlFacebookParser.parseAppinfo(doc);
    }
    else
    {
        return new ArrayList<oms.sns.service.facebook.model.Notifications.AppInfo>();
    }
}

public oms.sns.service.facebook.model.Notifications notifications_getList(Long starttime,Boolean includeread) throws FacebookException
{
    Document doc;
    if(starttime>0)
    {
       doc = fbClient.callMethod(new XmlFacebookMethod(Notifications.GET_LIST),"start_time",starttime,"include_read",includeread);   
    }
    else
    {
       doc = fbClient.callMethod(new XmlFacebookMethod(Notifications.GET_LIST),"include_read",includeread);  
    }
    return XmlFacebookParser.parseNotificationGetList(doc);
}

public boolean notifications_markRead(long[] notificationids) throws FacebookException
{
    Document doc ;
    if(notificationids!=null && notificationids.length>0)
    {
       doc = fbClient.callMethod(new XmlFacebookMethod(Notifications.MARK_READ),"notification_ids",notificationids);    
    }
    else
    {
       doc = fbClient.callMethod(new XmlFacebookMethod(Notifications.MARK_READ));
    }
    return XmlFacebookParser.extractBoolean(doc);
}

public boolean notifications_markRead(long notify) throws FacebookException
{
    Document doc ;
    if(notify >0)
    {
    	long[] notifys = new long[1];
    	notifys[0] = notify;
        doc = fbClient.callMethod(new XmlFacebookMethod(Notifications.MARK_READ),"notification_ids",notifys);    
    }
    else
    {
       doc = fbClient.callMethod(new XmlFacebookMethod(Notifications.MARK_READ));
    }
    return XmlFacebookParser.extractBoolean(doc);
}

public List<Notes> getNotes(long uid, int start, int limit) throws FacebookException
{	
	String friendid = "SELECT uid2 FROM friend WHERE uid1 = "+uid;
	String notefql = " SELECT uid,note_id,created_time,updated_time,content,title from note where uid in ("+friendid+") or uid="+uid+ " LIMIT "+ start+","+limit;	
	Document doc = executeQuery(notefql);
    return XmlFacebookParser.parseNotes(doc, this);
}

public boolean createNotes(String title, String content) throws FacebookException
{   
    Document doc = fbClient.callMethod(new XmlFacebookMethod(FBNotes.CREATE), "uid", this.getLogerInUserID(), "title", title, "content", content);
    return XmlFacebookParser.extractBoolean(doc);
}

public boolean editNotes(Long noteid,String title,String content) throws FacebookException
{
	Document doc = fbClient.callMethod(new XmlFacebookMethod(FBNotes.EDIT), "note_id", noteid, "title", title, "content", content);
    return XmlFacebookParser.extractBoolean(doc);
}

public List<Notes> getMyNotes(long uid, int start, int limit) throws FacebookException
{	
	String friendid = "SELECT uid2 FROM friend WHERE uid1 = "+uid;
	String notefql = " SELECT uid,note_id,created_time,updated_time,content,title from note where uid ="+uid+ " LIMIT "+ start+","+limit;	
	Document doc = executeQuery(notefql);
    return XmlFacebookParser.parseNotes(doc, this);
}


public boolean confirm(long uid, Boolean confirm) throws FacebookException{
	int confirmtag = 0; //1 to be friends, 0 to ignore the request
	if(confirm.booleanValue()){
		confirmtag = 1;
	}
	Document doc = fbClient.callMethod(new XmlFacebookMethod(Friends.FRIENDS_CONFIRM), "uid",uid,"confirm",confirmtag);

	return XmlFacebookParser.extractBoolean(doc);
}

public HashMap<Integer,Object> batch_run_getFriendsAndContactInfo(Long uid) throws FacebookException
{
    long[] friends = getFriendIds();
    if(friends == null || friends.length == 0)
    {
    	throw new FacebookException("Fail to get your friends is list, you need make sure the network and have friends already");
    }
    
    HashMap<Integer,Object> map = new HashMap<Integer,Object>();
    FacebookClient client = this.getFbClient();
    client.beginBatch();
    getFriendList(uid, false);
    getContactInfo(friends);   
    
    List<? extends Object> list = client.executeBatch(true);    
    int i=0;
    map.put(i, XmlFacebookParser.parseFacebookUserResponse((Document)list.get(i),this, false, true));
    i=1;
    map.put(i, XmlFacebookParser.parseContactInfoResponse((Document)list.get(i)));
    
    return map;
}

public HashMap<Integer,Object> batch_run_getUserAccountInfo(Long uid,Boolean needCallIsFriend,Boolean frompage) throws FacebookException
{
    HashMap<Integer,Object> map = new HashMap<Integer,Object>();
    
    FacebookClient client = this.getFbClient();
    client.beginBatch();
    //for user info
   // if(frompage == true)
   // {
    getPageInfo(uid,Page.Field.COMPANY_OVERVIEW,Page.Field.WEBSITE,Page.Field.MISSION,Page.Field.NAME,Page.Field.PAGE_ID,Page.Field.PIC_SQUARE);
    //}
    if(frompage == false)
    {
        getCompeletedUserInfo(uid);
    }
    //for status
    //getStatusByuid(uid);
    getWallByfql(uid, 0, 20, true);
    //for album
    getPhotoAlbumsByUser(uid);
    getAlbumCoverPhotoByUser(uid);
    
    if(frompage == false)
    {
    	getContactInfo(uid);
    	if(true == needCallIsFriend)
    	{
    	        isMyFriend(uid);
    	}
    }
    //for is friends
    List<? extends Object> list = client.executeBatch(true);
    
    int i=0;
    map.put(i, XmlFacebookParser.parsePageinfoResponse((Document)list.get(i)));
    if(frompage == false)
    {
         i++;
         map.put(i, XmlFacebookParser.parseFacebookUserResponse((Document)list.get(i),this, false,true));
    }
    
    i++;
    map.put(i, XmlFacebookParser.parserStream((Document)list.get(i), this));
    
    //for album data
    i++;
    List<PhotoAlbum> photoalbum_list = XmlFacebookParser.parsePhotoAlbum((Document)list.get(i));
    map.put(i, photoalbum_list);
    
    i++;
    List<Photo> photo_list = XmlFacebookParser.parseAlbumCoverPhoto((Document)list.get(i));
    map.put(i, photo_list);
    
    if(frompage == false)
    {
	    i++;
	    List<PhoneBook> phonebook_list = XmlFacebookParser.parseContactInfoResponse((Document)list.get(i));
	    PhoneBook phonebook = phonebook_list.size()>0?phonebook_list.get(0):null;
	    map.put(i,phonebook);
	    if(needCallIsFriend == true)
	    {
	    	 i++;
	         map.put(i, XmlFacebookParser.isMyFriend((Document)list.get(i)));
	    }
    }
   
    return map;
}

public HashMap<Integer,Object> batch_run_getPhone_User_Friend_Info(List<Long> uids) throws FacebookException
{
    HashMap<Integer,Object> map = new HashMap<Integer,Object>(); 
    long[] array_uids = new long[uids.size()];
    for(int i=0;i<uids.size();i++)
    {
        array_uids[i] = uids.get(i).longValue();
    }
    FacebookClient client = this.getFbClient();
    client.beginBatch();
    areMyFriends(uids);
    getContactInfo(array_uids);
    getUserInfo(array_uids, 
                FacebookUser.Field.FIRST_NAME,
                FacebookUser.Field.LAST_NAME, 
                FacebookUser.Field.NAME,
                FacebookUser.Field.BIRTHDAY, 
                FacebookUser.Field.PIC_SQUARE,
                FacebookUser.Field.PIC,
                FacebookUser.Field.PIC_SMALL,
                FacebookUser.Field.SEX,
                FacebookUser.Field.STATUS);
   
    List<? extends Object> list = client.executeBatch(true);
    List<FriendRelationship> frs = XmlFacebookParser.areMyFriends((Document)list.get(0));
    List<PhoneBook> phonebooks = XmlFacebookParser.parseContactInfoResponse((Document)list.get(1));
    List<FacebookUser> facebookusers = XmlFacebookParser.parseFacebookUserResponse((Document)list.get(2), this, false, false);

    if(frs != null)
    {
        for(FriendRelationship fr: frs)
        {
            if(fr.isFriends == false)
                continue;
            
            for(int i = 0;i<phonebooks.size();i++)
            {                   
                PhoneBook user = phonebooks.get(i);
                if(user.isFriend == false && user.uid == fr.uid2)
                {
                    user.isFriend = true;               
                    break;
                }
            }               
        }
    }   
    map.put(0,phonebooks);
    map.put(1, facebookusers);   
    return map;
}


public HashMap<Integer,Object> batch_run_getMessageThread(long uid, int limit,int offset,boolean hasProgress) throws FacebookException  {
    HashMap<Integer,Object> map = new HashMap<Integer,Object>(); 
    //0 inbox 1 outbox 4 update
    FacebookClient client = this.getFbClient();
    client.beginBatch();
    message_getThreadsInfo(0, uid, limit, offset, hasProgress);
    message_getThreadsInfo(1, uid, limit, offset, hasProgress);
    message_getThreadsInfo(4, uid, limit, offset, hasProgress);
    
    List<? extends Object> list = client.executeBatch(true);
    
    int i=0;
    map.put(i, XmlFacebookParser.parseMessageThreadInfo((Document)list.get(i), this, false));
    
    i=1;
    map.put(i, XmlFacebookParser.parseMessageThreadInfo((Document)list.get(i), this, false));
    
    i=2;
    map.put(i, XmlFacebookParser.parseMessageThreadInfo((Document)list.get(i), this, false));
    
    return map;
}

public HashMap<Integer,Object>  batch_run_getPageInfoAndUserInfo(long[] uids)  throws FacebookException
{
    HashMap<Integer,Object> map = new HashMap<Integer,Object>(); 
    //0 inbox 1 outbox 4 update
    FacebookClient client = this.getFbClient();
    client.beginBatch();
    getSimpleUserInfo(uids,false,FacebookUser.Field.NAME,FacebookUser.Field.BIRTHDAY, FacebookUser.Field.PIC_SQUARE);
    getPageInfo(uids,Page.Field.PAGE_ID,Page.Field.NAME,Page.Field.PIC_SQUARE,Page.Field.PAGE_URL,
                      Page.Field.COMPANY_OVERVIEW,Page.Field.WEBSITE,Page.Field.MISSION);
    List<? extends Object> list = client.executeBatch(true);
    
    int i=0;
    map.put(i, XmlFacebookParser.parseFacebookSimpleUserResponse((Document)list.get(i),this, false, false));
    
    i=1;
    map.put(i, XmlFacebookParser.parsePageinfoResponse((Document)list.get(i)));
    return map;
}


public List<Wall> getWalltoWallMessageBatchRun(long uid1,long uid2) throws FacebookException
{
    FacebookClient client = this.getFbClient();
    client.beginBatch();
    wallget(uid1,uid2);
    wallget(uid2,uid1);
    List<? extends Object> list = client.executeBatch(true);
    List<Wall> wall_list_1 = XmlFacebookParser.parseWall((Document)list.get(0),this,true);
    List<Wall> wall_list_2 = XmlFacebookParser.parseWall((Document)list.get(1),this,false);
    List<Wall> final_list = MergeWallListAndSort(wall_list_1,wall_list_2);    
    return final_list;
}

public List<Wall> MergeWallListAndSort(List<Wall> wall_list_1,List<Wall> wall_list_2)
{
  wall_list_1.addAll(wall_list_2);
  java.util.Collections.sort(wall_list_1);
  return wall_list_1;
}

//destroy my self
public void destroy() 
{
    //there is a bug for delay call facebook api when activity is destroyed
    /*
	fbClient        = null;
	loggedInUserId	= 0;
	secretKey       = null;
	connectionListener = null;
	*/	
}

public void destroy(boolean forceclean) 
{
    if(forceclean)
    {
		fbClient        = null;
		loggedInUserId	= 0;
		secretKey       = null;
		connectionListener = null;
    }	
}




  
   /*public String getPropertyJSONStr(List<Map<String,String>> list){
	   StringBuffer strBuf = new StringBuffer();
	   strBuf.append("{");
	   Iterator it = list.iterator();
	   while(it.hasNext()){    	  
    	   Map<String,String> para = (Map<String, String>)it.next();
    	   String text = para.get("text");
    	   String href = para.get("href");
    	   if(text!=null && href !=null ){
    		   strBuf.append("{");
    		   strBuf.append("\"text\":\""+text+"\",");
    		   strBuf.append("\"href\":\""+href+"\"");
    		   strBuf.append("},");
    	   }        
      }
   }*/
   
}
