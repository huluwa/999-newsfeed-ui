package oms.sns.service.facebook.client.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Notification;
import android.util.Log;

import com.msocial.free.service.ObjectHandler;
import com.msocial.free.ui.ActivityBase;
import com.msocial.free.ui.NetworkConnectionListener;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.EventMembersByStatus;
import oms.sns.service.facebook.model.FBNotifications;
import oms.sns.service.facebook.model.FriendRelationship;
import oms.sns.service.facebook.model.MessageThreadInfo;
import oms.sns.service.facebook.model.Notifications;
import oms.sns.service.facebook.model.Notes;
import oms.sns.service.facebook.model.Page;
import oms.sns.service.facebook.model.PhoneBook;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Group;
import oms.sns.service.facebook.model.GroupLocation;
import oms.sns.service.facebook.model.GroupMembersByRole;
import oms.sns.service.facebook.model.MailboxMessage;
import oms.sns.service.facebook.model.MailboxThread;
import oms.sns.service.facebook.model.Photo;
import oms.sns.service.facebook.model.PhotoAlbum;
import oms.sns.service.facebook.model.PhotoComment;
import oms.sns.service.facebook.model.PhotoTag;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.model.EventLocation;
import oms.sns.service.facebook.model.PokeResponse;
import oms.sns.service.facebook.model.Stream;
import oms.sns.service.facebook.model.StreamFilter;
import oms.sns.service.facebook.model.UserStatus;
import oms.sns.service.facebook.model.Video;
import oms.sns.service.facebook.model.Wall;
import oms.sns.service.facebook.model.GroupMembersByRole.Role;
import oms.sns.service.facebook.model.Notifications.AppInfo;
import oms.sns.service.facebook.model.Notifications.DeveloperInfo;
import oms.sns.service.facebook.model.Stream.Comments;
import oms.sns.service.facebook.model.Stream.Comments.Stream_Post;

public class XmlFacebookParser {
   
	private static String TAG="XmlFacebookParser";
    
	public static List<PokeResponse> parserPokeResponse(Document doc,boolean hasProgress,FacebookSession session){
		
	    NetworkConnectionListener tmp = null;
        if(session != null)
        {
            tmp = session.getBaseActivity();
            if(hasProgress && tmp != null && ActivityBase.class.isInstance(tmp))
            {
                ((ActivityBase)tmp).titleUpdateAfterNetwork();
            }
        }
        
	    ArrayList<PokeResponse> pokes = new ArrayList<PokeResponse>();	
		if ( doc == null ) {
			return pokes;
		}
		NodeList list =doc.getElementsByTagName("poke");
		
		if(list==null || list.getLength()<1) return pokes;
		
		for(int i=0;i<list.getLength();i++){	
		    
    		   if(hasProgress && tmp != null && ActivityBase.class.isInstance(tmp))
               {
                   ((ActivityBase)tmp).updateProgress(i, list.getLength());
               }
            
			   Node node = list.item(i);
				PokeResponse poke = new PokeResponse();
				for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
				{
					if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
					{					
						String name = cnode.getNodeName();
						String val = getChildText(name, (Element)cnode);
						if(val.equals("")) continue;
						 else if(name.equalsIgnoreCase(PokeResponse.Field.TIME.toString())) poke.setTime(new Date(Long.valueOf(val)*1000L));
						 else if(name.equalsIgnoreCase(PokeResponse.Field.UID.toString())) poke.setUid(Long.valueOf(val));
						 else if(name.equalsIgnoreCase(PokeResponse.Field.NAME.toString())) poke.setName(val);					
					}		
				}
			  pokes.add(poke);
		   }
		   return pokes;
		
	}
	
	public static Video parserVideoUpload(Document doc){
		if(doc == null) return null;
		
		NodeList list = doc.getElementsByTagName( "video_upload_response" );
		if(list!=null && list.item(0)!=null){
			Node node = list.item(0) ;          
			return parseVideo(node);
		}else{
			return null;
		}
	}
	
	public static Video parseVideo(Node node){
		Video video = new Video();
		NodeList nodelist = node.getChildNodes();
		for(int i=0;i<nodelist.getLength();i++)
		{
		    Node cnode = nodelist.item(i);
			if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
			{					
				String name = cnode.getNodeName();
				String val = getChildText(name, (Element)cnode);
			    if(val.equals("")) continue;			
			    else if(name.equalsIgnoreCase(Video.Field.VID.toString())) video.setVid(Long.valueOf(val));
			    else if(name.equalsIgnoreCase(Video.Field.TITLE.toString())) video.setTitle(val);
			    else if(name.equalsIgnoreCase(Video.Field.LINK.toString()))video.setLink(val);
			    else if(name.equalsIgnoreCase(Video.Field.DESCRIPTION.toString())) video.setDescription(val);						
			}
		}
		return video;
	}
	
	public static Photo parsePhotoUpload(Document doc){
		if(doc == null) return null;
		
		NodeList list = doc.getElementsByTagName( "photos_upload_response" );	
		if(list!=null && list.item(0)!=null){
			Node node = list.item(0) ;          
			return parsePhoto(node);
		}else{
			return null;
		}
		
	}
	
	public static List<Photo> parseGetPhotos(Document doc){
		ArrayList<Photo> photos = new ArrayList<Photo>();
		if(doc == null)return photos;
		NodeList list =doc.getElementsByTagName("photo");
		
		if(list!=null && list.getLength()>0){		
			for(int i=0;i<list.getLength();i++){
				photos.add(parsePhoto(list.item(i)));
			}	
		}
		return photos;
	}
	
	
	private static Photo parsePhoto(Node node){
		Photo photo = new Photo();
		//Log.d(TAG, String.valueOf(node.getChildNodes().getLength()));
		NodeList nodelist = node.getChildNodes();
		for(int i=0;i<nodelist.getLength();i++)
		{
			 Node cnode = nodelist.item(i);
			 
			if(cnode!=null && cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
			{					
				String name = cnode.getNodeName();
				String val = getChildText(name, (Element)cnode);	
				if(val.equals("")) continue;
				else if(name.equalsIgnoreCase(Photo.Field.PID.toString())) photo.setPid(val);
				else if(name.equalsIgnoreCase(Photo.Field.OWNER.toString())) photo.setOwner(Long.parseLong(val));
				else if(name.equalsIgnoreCase(Photo.Field.AID.toString())) photo.setAid(val);
				else if(name.equalsIgnoreCase(Photo.Field.CAPTION.toString())) photo.setCaption(val);
				else if(name.equalsIgnoreCase(Photo.Field.CREATED.toString())) photo.setCreated(new Date(Long.valueOf(val)*1000L));
				else if(name.equalsIgnoreCase(Photo.Field.LINK.toString())) photo.setLink(val);
				else if(name.equalsIgnoreCase(Photo.Field.SRC.toString())) photo.setSrc(val);
				else if(name.equalsIgnoreCase(Photo.Field.SRC_BIG.toString())) photo.setSrc_big(val);
				else if(name.equalsIgnoreCase(Photo.Field.SRC_SMALL.toString())) photo.setSrc_small(val);							
			}
		}
		return photo;
	}
	
	/**
	 * <photo_tag> 
	 *  <pid>34995991612795</pid>  
	 *  <subject>1240078</subject> 
	 *   <xcoord>51.4901</xcoord>  
	 *   <ycoord>23.6203</ycoord>  
	 *   <created>1132183424</created> 
	 *    </photo_tag>
	 * @param doc
	 * @return
	 */
	public static List<PhotoTag> parsePhotoTag(Document doc){
		 ArrayList<PhotoTag> phototags = new ArrayList<PhotoTag>();
		 if(doc == null) return phototags;
		 NodeList list = doc.getElementsByTagName("photo_tag");
		
		 if(list==null || list.getLength()<1){
			   return phototags;
		 }
		   
		   for(int i=0;i<list.getLength();i++){			  
			   Node node = list.item(i);
				PhotoTag phototag = new PhotoTag();
				for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
				{
					if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
					{					
						String name = cnode.getNodeName();
						String val = getChildText(name, (Element)cnode);
						if(val.equals("")) continue;
						else if(name.equalsIgnoreCase(PhotoTag.Field.CREATED.toString())) phototag.setCreated(new Date(Long.valueOf(val)*1000L));
						else if(name.equalsIgnoreCase(PhotoTag.Field.PID.toString())) phototag.setPid(Long.valueOf(val));
						else if(name.equalsIgnoreCase(PhotoTag.Field.SUBJECT.toString())) phototag.setSubject(Long.valueOf(val));
						else if(name.equalsIgnoreCase(PhotoTag.Field.TEXT.toString())) phototag.setText(val);
						else if(name.equalsIgnoreCase(PhotoTag.Field.XCOORD.toString())) phototag.setXcoord( Float.valueOf(val));
						else if(name.equalsIgnoreCase(PhotoTag.Field.YCOORD.toString())) phototag.setYcoord(Float.valueOf(val));
				
					}		
				}
			  phototags.add(phototag);
		   }
		   return phototags;
		
	}
	
	public static PhotoAlbum parseCreateAlbum(Document doc){
		if(doc == null) return null;
		
		NodeList list = doc.getElementsByTagName( "photos_createAlbum_response" );
		if(list!=null && list.item(0)!=null){
			Node node = list.item(0) ;           
			return parseSignalAlbum(node);
		}else{
			return null;
		}
	}
	
	public static PhotoAlbum parseSignalAlbum(Node node){
		PhotoAlbum photoalbum = new PhotoAlbum();
		for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
		{
			if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
			{					
				String name = cnode.getNodeName();			
				String val = getChildText(name, (Element)cnode);
				if(val.equals("")) continue;
				else if(name.equalsIgnoreCase(PhotoAlbum.Field.AID.toString())) photoalbum.aid = val;
				else if(name.equalsIgnoreCase(PhotoAlbum.Field.COVER_PID.toString())) photoalbum.cover_pid = val;
				else if(name.equalsIgnoreCase(PhotoAlbum.Field.CREATED.toString())) photoalbum.created = new Date(Long.valueOf(val)*1000L);
				else if(name.equalsIgnoreCase(PhotoAlbum.Field.DESCRIPTION.toString())) photoalbum.description = val;
				else if(name.equalsIgnoreCase(PhotoAlbum.Field.OWNER.toString())) photoalbum.owner = Long.valueOf(val);
				else if(name.equalsIgnoreCase(PhotoAlbum.Field.SIZE.toString())) photoalbum.size = Integer.valueOf(val).intValue();
				else if(name.equalsIgnoreCase(PhotoAlbum.Field.LINK.toString())) photoalbum.link = val;
				else if(name.equalsIgnoreCase(PhotoAlbum.Field.LOCATION.toString())) photoalbum.location = val;
				else if(name.equalsIgnoreCase(PhotoAlbum.Field.MODIFIED.toString())) photoalbum.modified = new Date(Long.valueOf(val)*1000L);
				else if(name.equalsIgnoreCase(PhotoAlbum.Field.NAME.toString())) photoalbum.name = val;	
				else if(name.equalsIgnoreCase("visible")) photoalbum.visible = val;
				else if(name.equalsIgnoreCase("modified_major")) photoalbum.modified_major = val;
			}		
		} 
		return photoalbum;
	}
	

    public static List<Photo> parseAlbumCoverPhoto(Document doc)
    {
        List<Photo> photos = new ArrayList<Photo>();
        if(doc == null) return photos;
        NodeList list = doc.getElementsByTagName("photo");
        
        if(list==null || list.getLength()<1){
            return photos;
        }
        
        for(int i=0;i<list.getLength();i++){  
            Node node = list.item(i);
            Photo photo = new Photo();
            for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
            {
                if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
                {                   
                    String name = cnode.getNodeName();          
                    String val = getChildText(name, (Element)cnode);
                    if(val.equals("")) continue;
                    else if(name.equalsIgnoreCase(Photo.Field.PID.toString())) photo.pid = String.valueOf(val);
                    else if(name.equalsIgnoreCase(Photo.Field.AID.toString())) photo.aid = String.valueOf(val);
                    else if(name.equalsIgnoreCase(Photo.Field.SRC_SMALL.toString())) photo.src_small = String.valueOf(val);                        
                }       
            } 
            photos.add(photo);
        }
        
        return photos;
    }
	
	/**
	 * <album>  <aid>34595963571485</aid> 
	 *  <cover_pid>34595991612812</cover_pid> 
	 *   <owner>8055</owner>  
	 *   <name>Films you will never see</name> 
	 *    <created>1132553109</created> 
	 *     <modified>1132553363</modified>
	 *       <description>No I will not make out with you</description>
	 *         <location>York, PA</location>  
	 *         <link>http://www.facebook.com/album.php?aid=2002205&id=8055</link> 
	 *          <size>30</size>
	 *            <visible>friends</visible>  </album>
	 * @param doc
	 * @return
	 */
	public static List<PhotoAlbum> parsePhotoAlbum(Document doc){
		ArrayList<PhotoAlbum> photoalbums = new ArrayList<PhotoAlbum>();
		if(doc == null) return photoalbums;
		 NodeList list = doc.getElementsByTagName("album");
		 
		   if(list==null || list.getLength()<1){
			   return photoalbums;
		   }
		   
		   for(int i=0;i<list.getLength();i++){  
			    Node node = list.item(i);
				photoalbums.add(parseSignalAlbum(node));
		   }
		   return photoalbums;
	}

	
	public static List<UserStatus> parseUserStatus(Document doc, FacebookSession session)
	{		
		List<UserStatus> userstatus = new ArrayList<UserStatus>();	
	    if(doc == null) return userstatus;
	    
	    NetworkConnectionListener tmp = null;
        if(session != null)
        {
            tmp = session.getBaseActivity();
            if(tmp != null && ActivityBase.class.isInstance(tmp))
            {
                ((ActivityBase)tmp).titleUpdateAfterNetwork();
            }
        }
        
	    NodeList list = doc.getElementsByTagName("user_status");
	    if(list==null || list.getLength()<1){
	    	list = doc.getElementsByTagName("status");
	    	if(list==null || list.getLength()<1)
	    	{
		        return userstatus;
	    	}
	    }		   
	    
	    HashMap<String,String> uid_name_map = new HashMap();
	    int size = list.getLength();
	    
	    for(int i=0;i<list.getLength();i++){
		   Node node = list.item(i);
		   UserStatus ust = new UserStatus();
		   boolean invalid = true;
		   		   
		   if(tmp != null && ActivityBase.class.isInstance(tmp))
		   {
		       ((ActivityBase)tmp).updateProgress(i, size);
		   }
		   
			for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{					
					String name = cnode.getNodeName();						
					String val = getChildText(name, (Element)cnode);
					if(val.equals("")) 
						continue;				
					if(name.equalsIgnoreCase(UserStatus.Field.UID.toString())) {
						ust.setUid(Long.valueOf(val)); 
						String username = uid_name_map.get(val);
						if(username ==null ){
						  try{
							username = session.getUserNameByUid(Long.valueOf(val));
							ust.setUsername(username);
							uid_name_map.put(val,username);
						   }catch(Exception e){}
						}else{
							ust.setUsername(username);
						}
					}
					else if(name.equalsIgnoreCase(UserStatus.Field.SOURCE.toString())) ust.setSource(Long.valueOf(val));
					else if(name.equalsIgnoreCase(UserStatus.Field.STATUS_ID.toString())) ust.setStatusid(Long.valueOf(val));
					
					else if(name.equalsIgnoreCase(UserStatus.Field.MESSAGE.toString()))
				    {
					    ust.setMessage(val);
					    if(isEmpty(val))
					    {
					    	invalid = false;
					    }
					}
					
					else if(name.equalsIgnoreCase(UserStatus.Field.TIME.toString())) ust.setTime(new Date(Long.valueOf(val)*1000L));
					
				}		
			} 
			if(invalid == true)
			{
			    userstatus.add(ust);
			}
			else
			{
				ust.recycle();
				ust = null;
			}
	    }
	    return userstatus;
	}
	
	
	private static boolean isEmpty(String str)
	{
		return str == null || str.length() == 0;
	}
	public static List<UserStatus>parseAllMyFriendsLastedStstus(Document doc, FacebookSession session){
		NetworkConnectionListener tmp = null;
		if(session != null)
		{
			tmp = session.getBaseActivity();
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).titleUpdateAfterNetwork();
			}
		}
		
		List<UserStatus> userstatuslist = new ArrayList<UserStatus>();
		
		if(doc == null) return userstatuslist;
		
		NodeList list = doc.getElementsByTagName( "user" );
		
		if(list==null || list.getLength()<1){
		    return userstatuslist;
	    }	
		
		int size = list.getLength();		
		for(int i=0;i<list.getLength();i++)
		{
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).updateProgress(i, size);
			}
			
			org.w3c.dom.Node node = list.item(i);
			UserStatus userstatus   = new UserStatus();;
			boolean isValid         =true;
			boolean isGetMessage    =false;
			for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				if(isValid == false)
				{	
					break;
				}
				
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{					
					String name = cnode.getNodeName();			
					if (name.equalsIgnoreCase("status"))
                    {   
                        NodeList nodelist = ((Element)cnode).getElementsByTagName("status");
                        if(nodelist==null || nodelist.getLength()<1) 
                            continue;
                        
                        Node s_node = nodelist.item(0);                   
                        String s_val = getChildText(UserStatus.Field.MESSAGE.toString(), (Element)s_node);
                        if(s_val.length() > 0)
                        {
                        	isValid      = true;
                        	isGetMessage = true;
                        	userstatus.setMessage(s_val);
	                        
                        	s_val = getChildText(UserStatus.Field.SOURCE.toString(), (Element)s_node);
                        	if(isEmpty(s_val) == false)
                        	{
                        		try{
                        	        userstatus.setSource(Long.valueOf(s_val));
                        		}catch(java.lang.NumberFormatException ne){}
                        	}
                        	
	                        s_val = getChildText(UserStatus.Field.STATUS_ID.toString(), (Element)s_node);
	                        try{
	                            userstatus.setStatusid(Long.valueOf(s_val));
	                    	}catch(java.lang.NumberFormatException ne){}
	                        
	                        s_val = getChildText(UserStatus.Field.TIME.toString(), (Element)s_node);
	                        try{
	                            userstatus.setTime(new Date(Long.valueOf(s_val)*1000L));
	                        }catch(java.lang.NumberFormatException ne){}
                        }
                        else
                        {
                        	isValid = false;
                        }
                    } 
					else
					{
						String val = getChildText(name, (Element)cnode);
						if(val.equals(""))  continue;
						
						if(name.equalsIgnoreCase(FacebookUser.Field.UID.toString())) 
					    {
							try{
						        userstatus.setUid(Long.valueOf(val));
							}catch(java.lang.NumberFormatException ne){}
					    }
						else if(name.equalsIgnoreCase(FacebookUser.Field.NAME.toString())) userstatus.setUsername(val);
					}	
				}
			}
			
			if(isGetMessage && isValid)
			{
			    userstatuslist.add(userstatus);
			}
		}
		return userstatuslist;
	
	}
		

	
	/**
	 * pare
	 * @param doc
	 * @return
	 */
	public static List<PhoneBook> parseContactInfoResponse(Document doc){
		List<PhoneBook> facebookcontacts = new ArrayList<PhoneBook>();
		if(doc == null) return facebookcontacts;
		NodeList list = doc.getElementsByTagName( "contact_info" );
		
	    if(list==null || list.getLength()<1){
		    return facebookcontacts;
	    }		   
		for(int i=0;i<list.getLength();i++)
		{
			org.w3c.dom.Node node = list.item(i);
			PhoneBook facebookcontact = new PhoneBook();
			boolean valid=false;
			NodeList clist = node.getChildNodes();
			for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{					
					String name = cnode.getNodeName();					
					String val = getChildText(name, (Element)cnode);					
					if(val.equals("")) continue;
					
					else if(name.equalsIgnoreCase(PhoneBook.Field.CELL.toString())) facebookcontact.setCell(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.EMAIL.toString())) facebookcontact.setEmail(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.PHONE.toString())) facebookcontact.setPhone(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.UID.toString()))
					{
						valid = true;
						facebookcontact.setUid(Long.valueOf(val));
					}
					else if(name.equalsIgnoreCase(PhoneBook.Field.NAME.toString())) facebookcontact.setUsername(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.ADDRESS.toString())) facebookcontact.setAddress(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.CITY.toString())) facebookcontact.setCity(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.COUNTRY.toString())) facebookcontact.setCountry(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.SCREENNAME.toString())) facebookcontact.setScreenname(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.STATE.toString())) facebookcontact.setState(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.STREET.toString())) facebookcontact.setStreet(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.LATITUDE.toString())) facebookcontact.setLatitude(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.LONGITUDE.toString())) facebookcontact.setLongitude(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.ZIP.toString())) facebookcontact.setZip(val);
				}
			}
			if(valid == true)
			{
		        facebookcontacts.add(facebookcontact);
			}
			else
			{
				valid = false;
			}
		}
		
	  return facebookcontacts;
	}
	
	public static List<FacebookUser.SimpleFBUser> parseFacebookSimpleUserResponse(Document doc, FacebookSession session, boolean hasProgress, boolean isFriend)
	{
		List<FacebookUser.SimpleFBUser> facebookusers = new ArrayList<FacebookUser.SimpleFBUser>();
		if(doc == null) return facebookusers;
		
		NetworkConnectionListener tmp = null;
        if(session != null)
        {
            tmp = session.getBaseActivity();
            if(hasProgress && tmp != null && ActivityBase.class.isInstance(tmp))
            {
                ((ActivityBase)tmp).titleUpdateAfterNetwork();
            }
        }
        
		NodeList list = doc.getElementsByTagName( "user" );
		
		if(list==null || list.getLength()<1){
		    return facebookusers;
	    }	
		int size = list.getLength();
		
		for(int i=0;i<list.getLength();i++)
		{			
			if(hasProgress && tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).updateProgress(i, size);
			}
			
			org.w3c.dom.Node node = list.item(i);
			FacebookUser.SimpleFBUser facebookuser = new FacebookUser.SimpleFBUser();
			facebookuser.isfriend = isFriend;
			boolean invaliduser = false;			
			for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{					
					String name = cnode.getNodeName();	
					String val = getChildText(name, (Element)cnode);
					if(val.equals(""))  continue;
					
					else if(name.equalsIgnoreCase(FacebookUser.Field.UID.toString()))
					{
						invaliduser = true;
						facebookuser.uid = Long.valueOf(val);
					}
				    else if(name.equalsIgnoreCase(FacebookUser.Field.BIRTHDAY.toString()))facebookuser.setBirthday(val);
				    else if(name.equalsIgnoreCase(FacebookUser.Field.NAME.toString())) facebookuser.name = val;
				    else if(name.equalsIgnoreCase(FacebookUser.Field.PIC_SQUARE.toString())) facebookuser.pic_square = val;
				}
			}
			if(invaliduser)
			{
			    facebookusers.add(facebookuser);
			}
			else
			{
				facebookuser = null;
			}
		}
		return facebookusers;
	}
	
	public static int parseFacebookUserResponse(Document doc, FacebookSession session, ObjectHandler handler)
	{
		int size = 0;	
		if(handler == null)
		{
			Log.d(TAG, "why no handler, ignore the process");
		}
		else
		{
					
			if(doc == null) return size;	        
			NodeList list = doc.getElementsByTagName( "user" );			
			if(list==null || list.getLength()<1){
			    return size;
		    }	
			
			for(int i=0;i<list.getLength();i++)
			{			
				org.w3c.dom.Node node = list.item(i);
				FacebookUser facebookuser = parseFacebookUser(node, true);
				if(facebookuser != null)
				{
					handler.process(facebookuser);
					size++;
				}
				
			}			
		}
		return size;
	}
	
	public static List<FacebookUser> parseFacebookUserResponse(Document doc, FacebookSession session, boolean hasProgress, boolean isFriend, ObjectHandler handler)
	{
		if(handler == null)
		{
			return parseFacebookUserResponse(doc, session, hasProgress, isFriend);
		}
		else
		{
			List<FacebookUser> facebookusers = new ArrayList<FacebookUser>();
			if(doc == null) return facebookusers;	        
			NodeList list = doc.getElementsByTagName( "user" );			
			if(list==null || list.getLength()<1){
			    return facebookusers;
		    }	
			
			for(int i=0;i<list.getLength();i++)
			{			
				org.w3c.dom.Node node = list.item(i);
				FacebookUser facebookuser = parseFacebookUser(node, isFriend);
				if(facebookuser != null)
				{
					handler.process(facebookuser);
				}
				
			}
			return facebookusers;
		}
	}
	public static List<FacebookUser> parseFacebookUserResponse(Document doc, FacebookSession session, boolean hasProgress, boolean isFriend)
	{
		List<FacebookUser> facebookusers = new ArrayList<FacebookUser>();
		if(doc == null) return facebookusers;
		
		NetworkConnectionListener tmp = null;
        if(session != null)
        {
            tmp = session.getBaseActivity();
            if(hasProgress && tmp != null && ActivityBase.class.isInstance(tmp))
            {
                ((ActivityBase)tmp).titleUpdateAfterNetwork();
            }
        }
        
		NodeList list = doc.getElementsByTagName( "user" );
		
		if(list==null || list.getLength()<1){
		    return facebookusers;
	    }	
		int size = list.getLength();
		
		for(int i=0;i<list.getLength();i++)
		{			
			if(hasProgress && tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).updateProgress(i, size);
			}
			
			org.w3c.dom.Node node = list.item(i);
			FacebookUser facebookuser = parseFacebookUser(node, isFriend);
			if(facebookuser != null)
			{
			    facebookusers.add(facebookuser);
			}
			
		}
		return facebookusers;
	}
	
	private static FacebookUser parseFacebookUser(org.w3c.dom.Node node, boolean isFriend)
	{
		FacebookUser facebookuser = new FacebookUser();
		facebookuser.isfriend = isFriend;
		boolean invaliduser = false;			
		for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
		{
			if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
			{					
				String name = cnode.getNodeName();				
				if(name.equalsIgnoreCase("current_location"))
			    {
			    	facebookuser.current_location = new FacebookUser.Current_Location();	
			    	Element cl = (Element)cnode;
                    String val_s = getChildText("city", (Element)cl);                        
                    facebookuser.current_location.city = val_s;     
                    
                    val_s = getChildText("country", (Element)cl);                        
                    facebookuser.current_location.country = val_s;
                    
                    val_s = getChildText("state", (Element)cl);                        
                    facebookuser.current_location.state = val_s;
                    
                    val_s = getChildText("zip", (Element)cl);                        
                    facebookuser.current_location.zip = val_s;
			    }
				else if (name.equalsIgnoreCase("hometown_location"))
				{
					facebookuser.hometown_location = new FacebookUser.Current_Location();	
			    	Element cl = (Element)cnode;
                    String val_s = getChildText("city", (Element)cl);                        
                    facebookuser.hometown_location.city = val_s;     
                    
                    val_s = getChildText("country", (Element)cl);                        
                    facebookuser.hometown_location.country = val_s;
                    
                    val_s = getChildText("state", (Element)cl);                        
                    facebookuser.hometown_location.state = val_s;
                    
                    val_s = getChildText("zip", (Element)cl);                        
                    facebookuser.hometown_location.zip = val_s;
				}
				else if(name.equalsIgnoreCase("meeting_for"))
				{
					Element meetingss = (Element)cnode;
					if(meetingss.getChildNodes() != null && meetingss.getChildNodes().getLength() >0)
					{   
						facebookuser.meeting_for = new ArrayList<String>();
					    //try to get seeking;
						NodeList metlist = meetingss.getElementsByTagName("seeking");
						if(metlist != null && metlist.getLength() > 0)
						{
							for(int meti=0;meti<metlist.getLength();meti++)
							{											
								Node meet_node = metlist.item(meti);
								if(meet_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
								{	
									String meet_node_name = meet_node.getNodeName();
									if("seeking".equals(meet_node_name))
									{	
										String val = getChildText("seeking", (Element)meet_node);
										facebookuser.meeting_for.add(val);
									}
								}
							}
						}
					}
				}
				else if(name.equalsIgnoreCase("meeting_sex"))
				{
					Element meetingss = (Element)cnode;
					if(meetingss.getChildNodes() != null && meetingss.getChildNodes().getLength() >0)
					{   
						facebookuser.meeting_sex = new ArrayList<String>();
					    //try to get seeking;
						NodeList metlist = meetingss.getElementsByTagName("sex");
						if(metlist != null && metlist.getLength() > 0)
						{
							for(int meti=0;meti<metlist.getLength();meti++)
							{											
								Node meet_node = metlist.item(meti);
								if(meet_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
								{	
									String meet_node_name = meet_node.getNodeName();
									if("sex".equals(meet_node_name))
									{	
										String val = getChildText("sex", (Element)meet_node);
										facebookuser.meeting_sex.add(val);
									}
								}
							}
						}
					}
				}
				else if (name.equalsIgnoreCase("education_history"))
				{
					Element eduhs = (Element)cnode;
					if(eduhs.getChildNodes() != null && eduhs.getChildNodes().getLength() >0)
					{
						facebookuser.education_history = new ArrayList<FacebookUser.Education_History>();
						for(org.w3c.dom.Node com_node=eduhs.getFirstChild();com_node != null;com_node=com_node.getNextSibling())
						{
							String com_name = com_node.getNodeName();		
							if(com_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
							{	
								if("education_info".equals(com_name))
								{	
									//try to find
									FacebookUser.Education_History object = new FacebookUser.Education_History();
									Element edu_node = (Element)com_node;
									
									//try to get concentrations
									for(org.w3c.dom.Node like_node=edu_node.getFirstChild();like_node != null;like_node=like_node.getNextSibling())
									{
										String nodename = like_node.getNodeName();		
										if(like_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
										{	
											if("concentrations".equals(nodename))
											{
												object.concentrations = new ArrayList<String>();
												Element friends_node = (Element)like_node;									
												for(org.w3c.dom.Node uid_node=friends_node.getFirstChild();uid_node != null;uid_node=uid_node.getNextSibling())
												{	
													String uid_name = uid_node.getNodeName();		
													if(uid_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
													{	
														if("concentration".equals(uid_name))
														{
															String one = getChildText("concentration", (Element)uid_node);
															object.concentrations.add(one);
														}
													}
												}
											}			
											else if("name".equals(nodename))
											{
												String val_s = getChildText("name", (Element)edu_node);																								
												object.name = val_s;
											}
											else if("year".equals(nodename))
											{
												String val_s = getChildText("year", (Element)edu_node);																								
												object.year = val_s;
											}
											else if("degree".equals(nodename))
											{
												String val_s = getChildText("degree", (Element)edu_node);																								
												object.degree = val_s;
											}
											else if("school_type".equals(nodename))
											{
												String val_s = getChildText("school_type", (Element)edu_node);
												object.school_type = val_s;
											}
										}
								    }
																								
									facebookuser.education_history.add(object);
								}									
							}	
						}
					}
				}
				
				else if (name.equalsIgnoreCase("work_history"))
				{
					Element works = (Element)cnode;
					if(works.getChildNodes() != null && works.getChildNodes().getLength() >0)
					{
						facebookuser.work_history = new ArrayList<FacebookUser.Work_History>();
						for(org.w3c.dom.Node work_node=works.getFirstChild();work_node != null;work_node=work_node.getNextSibling())
						{
							String com_name = work_node.getNodeName();		
							if(work_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
							{	
								if("work_info".equals(com_name))
								{	
									//try to find
									FacebookUser.Work_History object = new FacebookUser.Work_History();
									Element edu_node = (Element)work_node;													
									String val_s = getChildText("company_name", (Element)edu_node);																								
									object.company_name = val_s;
									
									val_s = getChildText("position", (Element)edu_node);																								
									object.position = val_s;
									
									val_s = getChildText("description", (Element)edu_node);																								
									object.description = val_s;
									
									val_s = getChildText("start_date", (Element)edu_node);	
									if(val_s.length() > 0)
									{
									    object.start_date = val_s;
									}
									
									val_s = getChildText("end_date", (Element)edu_node);	
									if(val_s.length() > 0)
									{
									    object.end_date = val_s;
									}
									
									//try to get concentrations
									NodeList cons = edu_node.getElementsByTagName("location");
									if(cons != null && cons.getLength() > 0)
									{
										Node con_node = cons.item(0);
										if(con_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
										{
											String val_loc = getChildText("city", (Element)con_node);
											object.city = val_loc;
											
											val_loc = getChildText("state", (Element)con_node);
											object.state = val_loc;
											
											val_loc = getChildText("country", (Element)con_node);
											object.country = val_loc;
										}
									}										
									facebookuser.work_history.add(object);
								}									
							}	
						}
					}
				}
		        else if (name.equalsIgnoreCase("status"))
                {   
                    NodeList nodelist = ((Element)cnode).getElementsByTagName("status");
                    if(nodelist==null || nodelist.getLength()<1) 
                        continue;
                    
                    Node s_node = nodelist.item(0);                   
                    String s_val = getChildText(UserStatus.Field.MESSAGE.toString(), (Element)s_node);
                    if(s_val.length() > 0)
                    {
                        facebookuser.setMessage(s_val);
                        
                        s_val = getChildText(UserStatus.Field.STATUS_ID.toString(), (Element)s_node);
                        try{
                            facebookuser.setStatusid(Long.valueOf(s_val));
                        }catch(java.lang.NumberFormatException ne){}
                        
                        s_val = getChildText(UserStatus.Field.TIME.toString(), (Element)s_node);
                        try{
                            facebookuser.setStatustime(Long.valueOf(s_val)*1000L);
                        }catch(java.lang.NumberFormatException ne){}
                    }
                } 
		        else//root value
		        {
					String val = getChildText(name, (Element)cnode);
					if(val.equals(""))  continue;
					
					else if(name.equalsIgnoreCase(FacebookUser.Field.UID.toString()))
					{
						invaliduser = true;
						facebookuser.setUid(Long.valueOf(val));
					}
				    else if(name.equalsIgnoreCase(FacebookUser.Field.BIRTHDAY.toString()))facebookuser.setBirthday(val);					
				    else if(name.equalsIgnoreCase(FacebookUser.Field.FIRST_NAME.toString())) facebookuser.setFirst_name(val);
				    else if(name.equalsIgnoreCase(FacebookUser.Field.LAST_NAME.toString())) facebookuser.setLast_name(val);
				    else if(name.equalsIgnoreCase(FacebookUser.Field.NAME.toString())) facebookuser.setName(val);
				    else if(name.equalsIgnoreCase(FacebookUser.Field.PIC_SQUARE.toString())) facebookuser.setPic_square(val);
				    else if(name.equalsIgnoreCase(FacebookUser.Field.PIC.toString())) facebookuser.pic = val;
				    else if(name.equalsIgnoreCase(FacebookUser.Field.PIC_SMALL.toString())) facebookuser.pic_small = val;
					
				    else if(name.equalsIgnoreCase(FacebookUser.Field.SEX.toString())) facebookuser.setSex(val);
				    else if(name.equalsIgnoreCase("about_me"))
				    {
				    	facebookuser.about_me = val;
				    }
				    else if(name.equalsIgnoreCase("activities"))
				    {
				    	facebookuser.activities = val;
				    }
				    else if(name.equalsIgnoreCase("quotes"))
				    {
				    	facebookuser.quotes = val;
				    }						
				    else if(name.equalsIgnoreCase("books"))
				    {
				    	facebookuser.books = val;
				    }
				    else if(name.equalsIgnoreCase("movies"))
				    {
				    	facebookuser.movies = val;				    	
				    }
				    else if(name.equalsIgnoreCase("music"))
				    {
				    	facebookuser.music = val;
				    }
				    else if(name.equalsIgnoreCase("tv"))
				    {
				    	facebookuser.tv = val;
				    }
				    else if(name.equalsIgnoreCase("relationship_status"))
				    {
				    	facebookuser.relationship_status = val;
				    }
				    else if(name.equalsIgnoreCase("online_presence"))
				    {
				    	facebookuser.online_presence = val;
				    }
				    else if(name.equalsIgnoreCase("is_app_user"))
				    {
				    	facebookuser.is_app_user = val.equalsIgnoreCase("1")?true:false;
				    }
				    else if(name.equalsIgnoreCase("interests"))
				    {
				        facebookuser.interests = val;
				    }
		        }
				//else if(name.equalsIgnoreCase(FacebookUser.Field.STATUS.toString()))facebookuser.setSt

			}
		}
		if(invaliduser == false)
		{
			facebookuser = null;
		}
		
		return facebookuser;
	}
	/**
	 * pare
	 * @param doc
	 * @return
	 */
	public static List<PhoneBook> parsePhonebookLookupResponse(Document doc){
		List<PhoneBook> facebookcontacts  = new ArrayList<PhoneBook>();
		if(doc == null) return facebookcontacts;
		NodeList list = doc.getElementsByTagName( "contact_info_user_map" );
		
		if(list==null || list.getLength()<1){
		    return facebookcontacts;
	    }	
		for(int i=0;i<list.getLength();i++)
		{
			org.w3c.dom.Node node = list.item(i);
			PhoneBook facebookcontact = new PhoneBook();
			boolean invalid = false;
			NodeList clist = node.getChildNodes();
			for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{					
					String name = cnode.getNodeName();
					String val = getChildText(name, (Element)cnode);
					if(val.equals("")) continue;
					else if(name.equalsIgnoreCase(PhoneBook.Field.CELL.toString())) facebookcontact.setCell(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.EMAIL.toString())) facebookcontact.setEmail(val);
					else if(name.equalsIgnoreCase(PhoneBook.Field.UID.toString()))
					{
						invalid = true;
						facebookcontact.setUid(Long.parseLong(val));
					}
				}
			}
			if(invalid)
			{
			    facebookcontacts.add(facebookcontact);
			}
			else
			{
				facebookcontact = null;
			}
		}
		
	  return facebookcontacts;
	}
	
	public static List<EventMembersByStatus> parseEventMembers(Document doc, FacebookSession session)
	{
	        
	    return null;
	}
	
	public static List<Long> parseEventID(Document doc)
	{
	    List<Long> eids = new ArrayList<Long>();
        if(doc==null) return eids;
        
        NodeList list = doc.getElementsByTagName( "event" );
        
        if(list==null || list.getLength()<1) return eids;

        for(int i=0;i<list.getLength();i++)
        {
            Node node = list.item(i);    
            String val = getChildText("eid", (Element)node);
            if(val.equals("")) continue;
            else eids.add(Long.valueOf(val));
        }
          
        return eids;
	}
	
	public static List<Event> parseEventInfo(Document doc,FacebookSession session){
		NetworkConnectionListener tmp = null;
		if(session != null)
		{
			tmp = session.getBaseActivity();
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).titleUpdateAfterNetwork();
			}
		}
		List<Event> events = new ArrayList<Event>();
		if(doc==null) return events;
		
		NodeList list = doc.getElementsByTagName( "event" );
		
		if(list==null || list.getLength()<1) return events;

		for(int i=0;i<list.getLength();i++)
		{
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).updateProgress(i, list.getLength());
			}
			
			Node node = list.item(i);
			Event event = new Event();			
			for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{					
					String name = cnode.getNodeName();
					NodeList leavesnode = cnode.getChildNodes();
					Node leafnode = leavesnode.item(0);
					if(leavesnode==null || leafnode==null) continue;				
					if(leafnode.getNodeType() == Node.ELEMENT_NODE && leafnode.getNodeName().equalsIgnoreCase(Event.Field.VENUE.toString())){
						 /*<venue><street/><city/><state/><country/></venue>*/
						//EventLocation e_location = new EventLocation();
						StringBuffer strBuf = new StringBuffer();
						for(Node v_node = leafnode.getFirstChild();v_node !=null;v_node = v_node.getNextSibling()){							
							String v_name = v_node.getNodeName();
							if(v_node.getNodeType() == Node.ELEMENT_NODE){
							 String v_value = getChildText(v_name, (Element)v_node);
							 if(v_value.equals("")) continue;
							 if(name.equalsIgnoreCase(EventLocation.Field.STREET.toString())) strBuf.append(v_value +" Street,");
							 else if(name.equalsIgnoreCase(EventLocation.Field.CITY.toString()))strBuf.append(" "+ v_value +" City,");
							 else if(name.equalsIgnoreCase(EventLocation.Field.STATE.toString()))strBuf.append(" "+ v_value );	
							 else if(name.equalsIgnoreCase(EventLocation.Field.COUNTRY.toString()))strBuf.append(" "+v_value);
						  }
						}
						event.setVenue(strBuf.toString());
						//event.setEventlocation(e_location);
						
					}else{
					
					//String val  = leafnode.getNodeValue();	
				    String val = getChildText(name, (Element)cnode);
				    if(val.equals("")) continue;
				    else if(name.equalsIgnoreCase(Event.Field.EID.toString()))event.setEid(Long.valueOf(val));
				    else if(name.equalsIgnoreCase(Event.Field.NAME.toString())) event.setName(val);
				    else if(name.equalsIgnoreCase(Event.Field.TAGLINE.toString())) event.setTagline(val);
				    else if(name.equalsIgnoreCase(Event.Field.NID.toString())) event.setNid(Long.valueOf(val));
				    else if(name.equalsIgnoreCase(Event.Field.PIC.toString())) event.setPic(val);
				    else if(name.equalsIgnoreCase(Event.Field.PIC_BIG.toString())) event.setPic_big(val);
				    else if(name.equalsIgnoreCase(Event.Field.PIC_SMALL.toString()))event.setPic_small(val);
				    else if(name.equalsIgnoreCase(Event.Field.HOST.toString())) event.setHost(val);
				    else if(name.equalsIgnoreCase(Event.Field.DESCRIPTION.toString())) event.setDescription(val);
				    else if(name.equalsIgnoreCase(Event.Field.EVENT_TYPE.toString())) event.setEvent_type(val);
				    else if(name.equalsIgnoreCase(Event.Field.EVENT_SUBTYPE.toString())) event.setEvent_sbytype(val);
				    else if(name.equalsIgnoreCase(Event.Field.START_TIME.toString())) event.setStart_time(new Date(Long.valueOf(val)*1000L));
				    else if(name.equalsIgnoreCase(Event.Field.END_TIME.toString())) event.setEnd_time(new Date(Long.valueOf(val)*1000L));
				    else if(name.equalsIgnoreCase(Event.Field.CREATOR.toString())) event.setCreator(Long.valueOf(val));
				    else if(name.equalsIgnoreCase(Event.Field.LOCATION.toString())) event.setLocation(val);
				    else if(name.equalsIgnoreCase(Event.Field.UPDATE_TIME.toString())) event.setUpdate_time(new Date(Long.valueOf(val)*1000L));
					}
				}
			}
			events.add(event);
		}		
		return events;
	}
	
	public static String getChildText(String str, Element elem) 
	{
		if(elem.getElementsByTagName(str) == null || elem.getElementsByTagName(str).item(0) == null)
		{
			return "";
		}
		
    	if(elem.getElementsByTagName(str).item(0).getFirstChild()!=null)
    	{
    		StringBuilder result= new StringBuilder();
    		NodeList nlist = elem.getElementsByTagName(str).item(0).getChildNodes();
    		for(int i=0;i<nlist.getLength();i++)
    		{
    			Node node = nlist.item(i);
    			if(node.getNodeType() == Node.ENTITY_REFERENCE_NODE)
    			{
    			    String name = node.getNodeName();
    			    if(name.indexOf("#") == 0)
    			    {
    			    	name = name.substring(1);
    			    	try{
	    			    	long uni = Long.parseLong(name);
	    			    	char val = (char) uni;
	    			    	result.append(val);
    			    	}catch(NumberFormatException ne)
    			    	{
    			    		result.append(name);
    			    	}
    			    }    			    
    			}
    			else if(node.getNodeType() == Node.TEXT_NODE)
    			{
    				result.append(node.getNodeValue());
    			}
    			else
    				result.append(node.getNodeValue()==null?"":node.getNodeValue());
    			
    		}
    		return result.toString();
    		//return elem.getElementsByTagName(str).item(0).getFirstChild().getNodeValue();
    	}
    	else
    	{
    		return "";
    	}        
   }
	
   public static List<Wall>parseWall(Document doc, FacebookSession session,boolean hasprogress){
	   NetworkConnectionListener tmp = null;
	   if(session != null)
	   {
	   	tmp = session.getBaseActivity();
	   	if(hasprogress && tmp != null && ActivityBase.class.isInstance(tmp))
	   	{
	   	    ((ActivityBase)tmp).titleUpdateAfterNetwork();
	   	}
	   }
	   
	   List<Wall> walls = new ArrayList<Wall>();
	   if(doc == null) return walls;
	   NodeList list = doc.getElementsByTagName("wallpost");
	   
	   if(list==null || list.getLength()<1){
		   return walls;
	   }	
	 
	   int size = list.getLength();
	   for(int i=0;i<list.getLength();i++){
		   /*<wpid>4909692</wpid><to>204686</to><from>212213</from><name>James Test</name><body>happy birthday!</body><time>1187956703</time>*/
		    Node node = list.item(i);		    
		    if(hasprogress && tmp != null && ActivityBase.class.isInstance(tmp))
		    {
		        ((ActivityBase)tmp).updateProgress(i, size);
		    }
		    
			Wall wall = new Wall();
			for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{					
					String name = cnode.getNodeName();				
					String val = getChildText(name, (Element)cnode);					
					//String val  = leafnode.getNodeValue();
					if(val.equals("")) continue;
					else if(name.equalsIgnoreCase(Wall.Field.WPID.toString())) wall.setWpid(Long.parseLong(val));
					else if(name.equalsIgnoreCase(Wall.Field.TO.toString())) wall.setTouserid(Long.parseLong(val));
					else if(name.equalsIgnoreCase(Wall.Field.FROM.toString())) wall.setFromid(Long.parseLong(val));
					else if(name.equalsIgnoreCase(Wall.Field.NAME.toString())) wall.setFromusername(val);
					else if(name.equalsIgnoreCase(Wall.Field.BODY.toString())) wall.setBody(val);
					else if(name.equalsIgnoreCase(Wall.Field.TIME.toString())) wall.setTime(new Date(Long.parseLong(val)*1000L));
				}		
			} 
			walls.add(wall);
	   }
	   return walls;
   }
   
   /*
    * 
    <messages list="true">
        <message>
           <message_id>1080947110912_0</message_id>
           <author_id>1036141712</author_id>
           <body>HH</body>
           <created_time>1249285457</created_time>
           <attachment/>
           <thread_id>1080947110912</thread_id>
       </message>
    </messages>
    */
   public static List<MessageThreadInfo> parseMessageThreadInfo(Document doc,FacebookSession session,boolean hasprogress)
   {
       NetworkConnectionListener tmp = null;
       if(session != null)
       {
           tmp = session.getBaseActivity();
           if(tmp != null && ActivityBase.class.isInstance(tmp) && hasprogress)
           {
               ((ActivityBase)tmp).titleUpdateAfterNetwork();
           }
       }
       
       List<MessageThreadInfo> threads = new ArrayList<MessageThreadInfo>();
       if(doc == null) return threads;
       NodeList list = doc.getElementsByTagName( "thread" );
       
       if(list == null || list.getLength()<1) 
           return threads;
   
       int size = list.getLength();
       for(int i=0;i<list.getLength();i++){
           Node node = list.item(i);
           MessageThreadInfo thread = new MessageThreadInfo();
           
           if(tmp != null && ActivityBase.class.isInstance(tmp) && hasprogress)
           {
               ((ActivityBase)tmp).updateProgress(i, size);
           }
            
           for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
           {
               if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
               {                   
                   String name = cnode.getNodeName();
                   if(name.equalsIgnoreCase(MessageThreadInfo.Field.RECIPIENTS.toString())){
                       NodeList leavesnode = ((Element)cnode).getElementsByTagName("uid");                     
                       if(leavesnode==null || leavesnode.getLength() ==0) 
                           continue;
                       
                       ArrayList<Long> array = new ArrayList<Long>();
                       for(int step=0;step<leavesnode.getLength();step++)
                       {
                            Node v_node = leavesnode.item(step);
                            String v_value = v_node.getFirstChild().getNodeValue();
                            if(v_value.equals("")) continue;
                            array.add(Long.valueOf(v_value));
                       }
                       thread.recipients = array;
                       
                   }
                   else
                   {                   
                       //String val  = leafnode.getNodeValue();
                       String val = getChildText(name, (Element)cnode);
                       if(val.equals("")) continue;
                       else if(name.equalsIgnoreCase(MessageThreadInfo.Field.THREAD_ID.toString())) thread.thread_id = Long.valueOf(val);
                       else if(name.equalsIgnoreCase(MessageThreadInfo.Field.SUBJECT.toString()))  thread.subject = val;
                       else if(name.equalsIgnoreCase(MessageThreadInfo.Field.MESSAGE_COUNT.toString())) thread.message_count = Integer.valueOf(val);
                       else if(name.equalsIgnoreCase(MessageThreadInfo.Field.OBJECT_ID.toString())) thread.object_id = Long.valueOf(val);
                       else if(name.equalsIgnoreCase(MessageThreadInfo.Field.PARENT_MESSAGE_ID.toString())) thread.parent_message_id = val;
                       else if(name.equalsIgnoreCase(MessageThreadInfo.Field.PARENT_THREAD_ID.toString())) thread.parent_thread_id = Long.valueOf(val);
                       else if(name.equalsIgnoreCase(MessageThreadInfo.Field.SNIPPET.toString())) thread.snippet = val;
                       else if(name.equalsIgnoreCase(MessageThreadInfo.Field.SNIPPET_AUTHOR.toString())) thread.snippet_author = Long.valueOf(val);
                       else if(name.equalsIgnoreCase(MessageThreadInfo.Field.THREAD_ID.toString())) thread.thread_id = Long.valueOf(val);
                       else if(name.equalsIgnoreCase(MessageThreadInfo.Field.UNREAD.toString())) thread.unread = Integer.valueOf(val);
                       else if(name.equalsIgnoreCase(MessageThreadInfo.Field.UPDATED_TIME.toString())) thread.updated_time = Long.valueOf(val)*1000L;
                       
                       //parse message
                       NodeList lists = ((Element)cnode).getElementsByTagName( "messages" );
                       for(int step=0;step<lists.getLength();step++)
                       {
                           Node msgnode = lists.item(step);
                           if(msgnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
                           {
                               List<MailboxMessage> msgs = parseMailMessage((Element)msgnode);
                               thread.messages = msgs;
                               break;
                           }
                       }
                   }                                   
               }       
           }
           
           threads.add(thread);
       }
       
       return threads;
   }
	
   /*
    * 
    <message>
     <message_id>1233029829433_0</message_id>
     <thread_id>1233029829433</thread_id>
     <author_id>625142542</author_id>
     <body>You wenti</body>
     <created_time>1267199874</created_time>
     <attachment/>
     <viewer_id>625142542</viewer_id>
   </message>
   */
   public static List<MailboxMessage> parseMailMessage(Element elem){
       List<MailboxMessage> messages = new ArrayList<MailboxMessage>();
       if(elem == null) return messages;
       
       NodeList list = elem.getElementsByTagName( "message" );
       
       if(list==null || list.getLength()<1) return messages;
       
       for(int i=0;i<list.getLength();i++){
           Node node = list.item(i);
                   
           MailboxMessage message = new MailboxMessage();
           for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
           {
               if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
               {                   
                   String name = cnode.getNodeName();
                   String val = getChildText(name, (Element)cnode);    
                   if(val.equals("")) continue;
                   else if(name.equalsIgnoreCase("thread_id")) message.threadid = (Long.valueOf(val));
                   else if(name.equalsIgnoreCase("author_id")) message.author = (Long.valueOf(val));
                   else if(name.equalsIgnoreCase("body")) message.body = (val);
                   else if(name.equalsIgnoreCase("attachment"))
                   {
                       try{
                       message.hasattachment = Integer.valueOf(val);
                       }catch(Exception ne){}
                   }
                   else if(name.equalsIgnoreCase("message_id")) message.mid = val;
                   else if(name.equalsIgnoreCase("created_time")) message.timesent = (new Date(Long.valueOf(val).longValue()*1000L));                
               }       
           }
           messages.add(message);
       }
       
       return messages;
   }
	public static List<MailboxThread> parseMailboxThread(Document doc, FacebookSession session, boolean isInbox){
		NetworkConnectionListener tmp = null;
		if(session != null)
		{
			tmp = session.getBaseActivity();
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).titleUpdateAfterNetwork();
			}
		}
		
		List<MailboxThread> threads = new ArrayList<MailboxThread>();
		if(doc == null) return threads;
		NodeList list = doc.getElementsByTagName( "thread" );
		
		if(list == null || list.getLength()<1) 
			return threads;
	
		int size = list.getLength();
		for(int i=0;i<list.getLength();i++){
			Node node = list.item(i);
			MailboxThread thread = new MailboxThread();
			thread.isinbox  = isInbox;
			thread.isoutbox = !isInbox;
			
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).updateProgress(i, size);
			}
			 
			for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{					
					String name = cnode.getNodeName();
					if(name.equalsIgnoreCase(MailboxThread.Field.ORIGINATOR.toString()))
					{
						NodeList leavesnode = ((Element)cnode).getElementsByTagName("uid");						
						if(leavesnode==null || leavesnode.getLength() ==0) 
							continue;
						
						/*<originator><uid></uid></originator>*/	 
						//thread.setOriginator(Long.valueOf(leafnode.getFirstChild().getChildNodes().item(0).getNodeValue()));
						
						String v_value = leavesnode.item(0).getFirstChild().getNodeValue();
						if(!v_value.equals(""))
						{
						   thread.setOriginator(Long.valueOf(v_value));
						}
					}else if(name.equalsIgnoreCase(MailboxThread.Field.PARTICIPANTS.toString())){
						NodeList leavesnode = ((Element)cnode).getElementsByTagName("uid");						
						if(leavesnode==null || leavesnode.getLength() ==0) 
							continue;
						
						/*<participants><uid></uid><uid></uid></participants>*/
						ArrayList<Long> array = new ArrayList<Long>();
						for(int step=0;step<leavesnode.getLength();step++)
						{
							 Node v_node = leavesnode.item(step);
							 String v_value = v_node.getFirstChild().getNodeValue();
							 if(v_value.equals("")) continue;
							 array.add(Long.valueOf(v_value));
						}
						thread.setParticipants(array);
						
					}else if(name.equalsIgnoreCase(MailboxThread.Field.RECENT_AUTHORS.toString())){
						NodeList leavesnode = ((Element)cnode).getElementsByTagName("uid");						
						if(leavesnode==null || leavesnode.getLength() ==0) 
							continue;
						
						/*<recent_authors><uid></uid><uid></uid></recent_authors>*/
						ArrayList<Long> array = new ArrayList<Long>();
						for(int step=0;step<leavesnode.getLength();step++)
						{
							 Node v_node = leavesnode.item(step);
							 String v_value = v_node.getFirstChild().getNodeValue();
							 if(v_value.equals("")) continue;
							 array.add(Long.valueOf(v_value));
						}
						thread.setRecentauthors(array);						
					}	
					else
					{					
						//String val  = leafnode.getNodeValue();
						String val = getChildText(name, (Element)cnode);
						if(val.equals("")) continue;
						else if(name.equalsIgnoreCase(MailboxThread.Field.TID.toString())) thread.setThreadid(Long.valueOf(val));
						else if(name.equalsIgnoreCase(MailboxThread.Field.PARENT_TID.toString())) thread.setParent_tid(Long.valueOf(val));
						else if(name.equalsIgnoreCase(MailboxThread.Field.HAS_ATTACHMENT.toString()))
						{
	                       try{
	                           thread.hasattachment = Integer.valueOf(val);
	                       }catch(Exception ne){}
						}
						else if(name.equalsIgnoreCase(MailboxThread.Field.LAST_UPDATE.toString())) thread.setLastupdate(new Date(Long.valueOf(val).longValue()*1000L));
						else if(name.equalsIgnoreCase(MailboxThread.Field.MSG_COUNT.toString())) thread.setMsgcount(Integer.valueOf(val));
						else if(name.equalsIgnoreCase(MailboxThread.Field.SUBJECT.toString())) thread.setSubject(val);
						else if(name.equalsIgnoreCase(MailboxThread.Field.UNREAD_COUNT.toString())) thread.setUnreadcount(Integer.valueOf(val));
					
						
					}									
				}		
			}
			
			threads.add(thread);
		}
		
		return threads;
	}
	
	
	public static List<MailboxMessage> parseMailboxMessage(Document doc, FacebookSession session){
		NetworkConnectionListener tmp = null;
		if(session != null)
		{
			tmp = session.getBaseActivity();
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).titleUpdateAfterNetwork();
			}
		}
		
		List<MailboxMessage> messages = new ArrayList<MailboxMessage>();
		if(doc == null) return messages;
		
		NodeList list = doc.getElementsByTagName( "message" );
		
		if(list==null || list.getLength()<1) return messages;
		
		for(int i=0;i<list.getLength();i++){
			Node node = list.item(i);
			
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).updateProgress(i, list.getLength());
			}

			
			MailboxMessage message = new MailboxMessage();
			for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{					
					String name = cnode.getNodeName();
					String val = getChildText(name, (Element)cnode);	
					if(val.equals("")) continue;
					else if(name.equalsIgnoreCase(MailboxMessage.Field.TID.toString())) message.threadid = Long.valueOf(val);
					else if(name.equalsIgnoreCase(MailboxMessage.Field.AUTHOR.toString())) message.author = Long.valueOf(val);
					else if(name.equalsIgnoreCase(MailboxMessage.Field.BODY.toString())) message.body = val;
					else if(name.equalsIgnoreCase(MailboxMessage.Field.HAS_ATTACHMENT.toString()))
					{
					    try{
					    message.hasattachment = (Integer.valueOf(val));
					    }catch(Exception ne){}
					}
					else if(name.equalsIgnoreCase(MailboxMessage.Field.MID.toString())) message.mid = val;
					else if(name.equalsIgnoreCase(MailboxMessage.Field.TIME_SENT.toString())) message.timesent = (new Date(Long.valueOf(val).longValue()*1000L));
				
				}		
			}
			messages.add(message);
		}
		
		return messages;
	}
	
	/*
	 * 
	<stream_get_response xsi:schemaLocation="http://api.facebook.com/1.0/ http://api.facebook.com/1.0/facebook.xsd">
	<posts list="true">
	<stream_post>
		<post_id>1120167345_97307846536</post_id>
		<viewer_id>625142542</viewer_id>
		<source_id>1120167345</source_id>
		<type>46</type>
		<app_id>2231777543</app_id>
		<actor_id>1120167345</actor_id>
	    <message>
            sssssss
        </message>
        
        <likes>
	        <href>http://www.facebook.com/s.php?k=100000004&amp;id=83745831466&amp;gr=2409997254</href>
	        <count>1</count>
	        <sample list="true"/>
	        <friends list="true">
	          <uid>1624264093</uid>
	        </friends>
	        <user_likes>0</user_likes>
	        <can_like>1</can_like>
        </likes>
        
        <comments>
	        <can_remove>1</can_remove>
	        <can_post>1</can_post>
	        <count>2</count>
	        <posts list="true">
	          <stream_post>
	            <fromid>1624264093</fromid>
	            <time>1241406395</time>
	            <text>don't play more Kaixin</text>
	            <id>1624264093_83745831466_1650317</id>
	          </stream_post>
	          <stream_post>
	            <fromid>625142542</fromid>
	            <time>1241406909</time>
	            <text>yeah, thanks</text>
	            <id>1624264093_83745831466_1650389</id>
	          </stream_post>
	          
	        </posts>
      </comments>
     
     <attachment>
	    <media list="true"/>
	    <name>sssss</name>
	    <href>http://www.facebook.com/ext/share.php?sid=83745831466&amp;h=jMMwy&amp;u=ELNpZ</href>
	    <caption>Source: tech.sina.com.cn</caption>
	    <descriptionxxxxxxx</description>
	    <properties list="true"/>
      </attachment>
      
	 */
	public static List<Stream> parserStream(Document doc, FacebookSession session) 
	{
		NetworkConnectionListener tmp = null;
		if(session != null)
		{
			tmp = session.getBaseActivity();
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).titleUpdateAfterNetwork();
			}
		}
		
		List<Stream> streams = new ArrayList<Stream>();
		if(doc == null) return streams;
		
		NodeList list = doc.getElementsByTagName( "stream_post" );
		
		if(list==null || list.getLength()<1) 
			return streams;
		
		int size = list.getLength();
		for(int i=0;i<size;i++)
		{
			Node stream_post = list.item(i);
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).updateProgress(i, size);
			}
			
			Stream stream = new Stream();
			boolean invalideStream=false;
			for(org.w3c.dom.Node cnode=stream_post.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				String name = cnode.getNodeName();			
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{	
					if(name.equals("likes"))
					{
						Element likes = (Element)cnode;
						if(likes.getChildNodes() != null && likes.getChildNodes().getLength() >0)
						{
							stream.likes = new Stream.Likes();
							stream.likes.can_like = 1;					
							String can_like = getChildText("can_like", likes);
							if(isEmpty(can_like) == false)
							{
								stream.likes.can_like = Integer.valueOf(can_like);
								if(stream.likes.can_like == 0)
									continue;
							}
							
							
							String count = getChildText("count", likes);
							if(isEmpty(count) == false)
							{
							    stream.likes.count = Integer.valueOf(count);
							}
							
							String user_likes = getChildText("user_likes", likes);
							if(isEmpty(count) == false)
                            {
							    stream.likes.user_likes = Integer.valueOf(user_likes);
                            }
							for(org.w3c.dom.Node like_node=likes.getFirstChild();like_node != null;like_node=like_node.getNextSibling())
							{
								String like_name = like_node.getNodeName();		
								if(like_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
								{	
									if("friends".equals(like_name))
									{
										Element friends_node = (Element)like_node;									
										for(org.w3c.dom.Node uid_node=friends_node.getFirstChild();uid_node != null;uid_node=uid_node.getNextSibling())
										{	
											String uid_name = uid_node.getNodeName();		
											if(uid_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
											{	
												if("uid".equals(uid_name))
												{
													long uid = Long.valueOf(getChildText("uid", (Element)uid_node));
													stream.likes.friends.add(uid);
												}
											}
										}								    
									}									
								}
						    }
						}
						
					}
					else if(name.equals("comments"))
					{
						Element comments = (Element)cnode;
						if(comments.getChildNodes() != null && comments.getChildNodes().getLength() >0)
						{
							stream.comments = new Stream.Comments();
							String count = getChildText("count", (Element)cnode);
							stream.comments.count = Integer.valueOf(count);
							
							for(org.w3c.dom.Node com_node=comments.getFirstChild();com_node != null;com_node=com_node.getNextSibling())
							{
								String com_name = com_node.getNodeName();		
								if(com_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
								{	
									if("comment_list".equals(com_name))
									{
										
										//try to find stream_media
										Element media_node = (Element)com_node;
																			
										for(org.w3c.dom.Node m_node=media_node.getFirstChild();m_node != null;m_node=m_node.getNextSibling())
										{	
											String post_name = m_node.getNodeName();		
											if(m_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
											{	
												if("comment".equals(post_name))
												{
													Stream.Comments.Stream_Post comment_item= new Stream.Comments.Stream_Post();
													
													String fromid = getChildText("fromid", (Element)m_node);
													if(fromid == null)
													{
														break;
													}
													comment_item.fromid = Long.valueOf(fromid);
													
													
													String time = getChildText("time", (Element)m_node);
													comment_item.time = Long.valueOf(time)*1000;
													
													String text = getChildText("text", (Element)m_node);
													comment_item.text = text;
													
													String username = getChildText("username", (Element)m_node);
													comment_item.username = username;
													
													String id = getChildText("id", (Element)m_node);									
													comment_item.id = id;
													
													comment_item.parentsuid = stream.source_id;
													stream.comments.stream_posts.add(comment_item);														
												}
											}
										}									
									}									
								}	
							}
						}
						
						 if(stream.comments.stream_posts!=null && stream.comments.stream_posts.size()>0)
					     {
							java.util.Collections.sort(stream.comments.stream_posts);
				            //List<Stream_Post> newlist = reSortComments(stream.comments.stream_posts);
				            //stream.comments.stream_posts.clear();
				            //stream.comments.stream_posts = newlist;
					     }
					}
					else if(name.equals("attachment"))
					{
						Element att_elem = (Element)cnode;
						if(att_elem.getChildNodes() != null && att_elem.getChildNodes().getLength()> 0)
						{
							stream.attachment = new Stream.Attachment();
							String at_name = getChildText("name", (Element)cnode);
							stream.attachment.name = at_name;
							
							String href = getChildText("href", (Element)cnode);
							stream.attachment.href = href;
							
							String caption = getChildText("caption", (Element)cnode);
							stream.attachment.caption = caption;
							
							String description = getChildText("description", (Element)cnode);
							stream.attachment.description = description;
							
							String icon = getChildText("icon", (Element)cnode);
							stream.attachment.icon = icon;
							
							for(org.w3c.dom.Node att_node=att_elem.getFirstChild();att_node != null;att_node=att_node.getNextSibling())
							{
								String att_name = att_node.getNodeName();		
								if(att_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
								{	
									if("media".equals(att_name))
									{
										//try to find stream_media
										Element media_node = (Element)att_node;
																			
										for(org.w3c.dom.Node m_node=media_node.getFirstChild();m_node != null;m_node=m_node.getNextSibling())
										{	
											String com_name = m_node.getNodeName();		
											if(m_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
											{	
												if("stream_media".equals(com_name))
												{
													Stream.Attachment.Media media_item= new Stream.Attachment.Media();
													
													String type = getChildText("type", (Element)m_node);
													media_item.type = type;
													
													String src = getChildText("src", (Element)m_node);
													media_item.src = src;
													
													String m_href = getChildText("href", (Element)m_node);
													media_item.href = m_href;
													
													stream.attachment.attachments.add(media_item);		
												}
											}												
										}									
									}									
								}												
							}
						}
					}
					else if(name.equals("action_links"))
					{
						stream.links = new Stream.Action_Links();
					}
					else if(name.equals("app_data"))
					{
						
					}
					else
					{		
					    Element tempElement = (Element)cnode;
						String val = getChildText(name, tempElement);
						if(val.equals("")) continue;					
						//TODO
						else if(name.equalsIgnoreCase("post_id"))
						{
							stream.post_id = val;
							invalideStream = true;
						}
						else if(name.equalsIgnoreCase("viewer_id")) stream.viewer_id = Long.valueOf(val);
						else if(name.equalsIgnoreCase("source_id")) stream.source_id = Long.valueOf(val);
						else if(name.equalsIgnoreCase("type"))      stream.type      = Integer.valueOf(val);
						else if(name.equalsIgnoreCase("app_id"))    stream.app_id    = Long.valueOf(val);
						else if(name.equalsIgnoreCase("actor_id"))  stream.actor_id  = Long.valueOf(val);
						else if(name.equalsIgnoreCase("target_id")) stream.target_id = Long.valueOf(val);
						
						else if(name.equalsIgnoreCase("message"))       stream.message        = val;
						else if(name.equalsIgnoreCase("attribution"))   stream.attribution    = val;						
						else if(name.equalsIgnoreCase("updated_time"))  
							stream.updated_time   = Long.valueOf(val)*1000;
						else if(name.equalsIgnoreCase("created_time"))  
							stream.created_time   = Long.valueOf(val)*1000;
						else if(name.equalsIgnoreCase("permalink"))  stream.permalink   = val;	
						else if(name.equalsIgnoreCase("is_hidden"))  stream.is_hidden = "1".equals(val);
					}
				}				
			}
			
			if(stream.is_hidden == true)
			{
			    stream.dispose();
			    stream = null;
			}
			if(invalideStream && stream != null)
			    streams.add(stream);
			
		}
		
		return streams;
	}
	/*
	 *
    <stream_filter>
	    <uid>625142542</uid>
	
	    <filter_key>city_342748</filter_key>
	
	    <name>Beijing Shi, China</name>
	
	    <rank>1</rank>
	
	    <icon_url>http://www.facebook.com/images/app_icons/geo_city.gif</icon_url>
	
	    <is_visible>1</is_visible>
	
	    <type>other</type>
	
	    <value>342748</value>
    </stream_filter>
	 */
	public static List<StreamFilter> parserStreamFilter(Document doc, FacebookSession session) 
	{
		NetworkConnectionListener tmp = null;
		
		List<StreamFilter> streams = new ArrayList<StreamFilter>();
		if(doc == null) return streams;
		
		NodeList list = doc.getElementsByTagName( "stream_filter" );
		
		if(list==null || list.getLength()<1) 
			return streams;
		
		int size = list.getLength();
		for(int i=0;i<size;i++)
		{
			Node stream_post = list.item(i);
			StreamFilter stream = new StreamFilter();
			boolean invalideStream = false;
			for(org.w3c.dom.Node cnode=stream_post.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				String name = cnode.getNodeName();			
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{	
					Element tempElement = (Element)cnode;
					String val = getChildText(name, tempElement);
					if(val.equals("")) continue;					
					//TODO
					else if(name.equalsIgnoreCase("uid"))
					{
						stream.uid = Long.valueOf(val);
						invalideStream = true;
					}
					else if(name.equalsIgnoreCase("filter_key")) stream.filter_key = val;
					else if(name.equalsIgnoreCase("name"))       stream.name = val;
					else if(name.equalsIgnoreCase("rank"))       stream.rank = Integer.valueOf(val);
					else if(name.equalsIgnoreCase("icon_url"))   stream.icon_url    = val;
					else if(name.equalsIgnoreCase("is_visible")) stream.is_visible  = Integer.valueOf(val) ==1?true:false;
					else if(name.equalsIgnoreCase("type"))       stream.type = val;
					else if(name.equalsIgnoreCase("value"))      stream.value = val;
				}
			}
			if(invalideStream == true)
			{
				streams.add(stream);
			}
		}
		return streams;
	}
	
	public static List<AppInfo> parseAppinfo(Document doc) {
	    ArrayList<AppInfo> list = new ArrayList<AppInfo>();
	    if(doc == null) return list;
	    NodeList appsnode       = doc.getElementsByTagName("app_info");
	    for(int i=0;i<appsnode.getLength();i++)
        {
            Notifications.AppInfo appinfo = new Notifications.AppInfo();
            Node appinfonode = appsnode.item(i);
            Element appinfoelement;
            if(appinfonode.getNodeType() == Element.ELEMENT_NODE)
            {
                appinfoelement = (Element)appinfonode;
                
                String app_id = getChildText("app_id",appinfoelement);
                appinfo.app_id  = !isEmpty(app_id)?Long.parseLong(app_id):0L;
                
                String api_key = getChildText("api_key",appinfoelement);
                appinfo.api_key  = api_key;
                
                String canvas_name = getChildText("canvas_name",appinfoelement);
                appinfo.canvas_name  = canvas_name;
                
                String display_name = getChildText("display_name",appinfoelement);
                appinfo.display_name  = display_name;
                
                String icon_url = getChildText("icon_url",appinfoelement);
                appinfo.icon_url  = icon_url;
      
                String logo_url = getChildText("logo_url",appinfoelement);
                appinfo.logo_url  = logo_url;
               
                String company_name = getChildText("company_name",appinfoelement);
                appinfo.company_name  = company_name;
      
                String description = getChildText("description",appinfoelement);
                appinfo.description  = description;
                
                String daily_active_users = getChildText("daily_active_users",appinfoelement);
                appinfo.daily_active_users  = !isEmpty(daily_active_users)?Long.parseLong(daily_active_users):0L;
                
                String weekly_active_users = getChildText("weekly_active_users",appinfoelement);
                appinfo.weekly_active_users  = !isEmpty(weekly_active_users)?Long.parseLong(weekly_active_users):0L;
                
                String monthly_active_users = getChildText("monthly_active_users",appinfoelement);
                appinfo.monthly_active_users  = !isEmpty(monthly_active_users)?Long.parseLong(monthly_active_users):0L;
                
                NodeList developers_nodelist = appinfoelement.getElementsByTagName("developer_info");
                
                if(developers_nodelist!=null && developers_nodelist.getLength()>0)
                {
                    ArrayList<DeveloperInfo> developerinfo_list = new ArrayList<DeveloperInfo>();
                    
                    for(int j=0;j<developers_nodelist.getLength();j++)
                    {
                        
                        Node developerinfonode = developers_nodelist.item(j);
                        Element developerinfoelement;
                        if(developerinfonode.getNodeType() == Element.ELEMENT_NODE)
                        {
                            developerinfoelement = (Element)developerinfonode;
                        }
                        else
                        {
                            continue;
                        }
                         DeveloperInfo developerinfo = new Notifications.DeveloperInfo();     
                         
                         String uid = getChildText("uid",developerinfoelement);
                         developerinfo.uid = !isEmpty(uid)?Long.parseLong(uid):0L;
                         
                         String name = getChildText("name",developerinfoelement);
                         developerinfo.name = name;
                         
                         developerinfo_list.add(developerinfo);
                    }
                    
                    appinfo.developer_info = developerinfo_list;
                }
                          
                list.add(appinfo);                      
            }
        }
	    return list;
	}
	
	public static FBNotifications parseNotifications(Document doc) 
	{	
		FBNotifications notify = new FBNotifications();
		if(doc == null) return notify;
		
		NodeList list = doc.getElementsByTagName( "notifications_get_response" );		
		if(list==null || list.getLength()<1) 
			return notify;
		
		int size = list.getLength();
		for(int i=0;i<size;i++)
		{
			Node stream_post = list.item(i);
			for(org.w3c.dom.Node cnode=stream_post.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				String name = cnode.getNodeName();			
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{	
					if(name.equals("messages"))
					{
						Element messages = (Element)cnode;						
						if(messages.getChildNodes() != null && messages.getChildNodes().getLength() >0)
						{
							String unread = getChildText("unread", messages);
							notify.msg.unread = Integer.valueOf(unread);
							
							
							String most_recent = getChildText("most_recent", messages);
							if(most_recent != null)
							{
								try{
							        notify.msg.most_recent = Long.valueOf(most_recent);
								}catch(NumberFormatException ne){}
							}
							
						}
						
					}
					else if(name.equals("pokes"))
					{
						Element pokes = (Element)cnode;
						if(pokes.getChildNodes() != null && pokes.getChildNodes().getLength() >0)
						{
							String unread = getChildText("unread", pokes);
							notify.poke.unread = Integer.valueOf(unread);
							
							
							String most_recent = getChildText("most_recent", pokes);
							if(most_recent != null)
							{
								try{
							        notify.poke.most_recent = Long.valueOf(most_recent);
								}catch(NumberFormatException ne){}
							}
							
						}
						
					}
					else if(name.equals("shares"))
					{
						Element shares = (Element)cnode;
						if(shares.getChildNodes() != null && shares.getChildNodes().getLength() >0)
						{
							String unread = getChildText("unread", shares);
							notify.share.unread = Integer.valueOf(unread);							
							
							String most_recent = getChildText("most_recent", shares);
							if(most_recent != null)
							{
								try{
							        notify.share.most_recent = Long.valueOf(most_recent);
								}catch(NumberFormatException ne){}
							}							
						}						
					}
					else if(name.equals("friend_requests"))
					{
						Element friend_requests = (Element)cnode;
						if(friend_requests.getChildNodes() != null && friend_requests.getChildNodes().getLength()> 0)
						{							
							Element friends_node = (Element)friend_requests;
							for(org.w3c.dom.Node uid_node=friends_node.getFirstChild();uid_node != null;uid_node=uid_node.getNextSibling())
							{	
								String uid_name = uid_node.getNodeName();		
								if(uid_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
								{	
									if("uid".equals(uid_name))
									{
										long uid = Long.valueOf(getChildText("uid", (Element)uid_node));
										notify.frdRequest.uids.add(uid);
									}
								}
							}								    
									
						}
					}
					
					else if(name.equals("group_invites"))
					{
						Element friend_requests = (Element)cnode;
						if(friend_requests.getChildNodes() != null && friend_requests.getChildNodes().getLength()> 0)
						{							
							Element friends_node = (Element)friend_requests;
							for(org.w3c.dom.Node uid_node=friends_node.getFirstChild();uid_node != null;uid_node=uid_node.getNextSibling())
							{	
								String uid_name = uid_node.getNodeName();		
								if(uid_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
								{	
									if("gid".equals(uid_name))
									{
										long uid = Long.valueOf(getChildText("gid", (Element)uid_node));
										notify.grdInvite.uids.add(uid);
									}
								}
							}								    
									
						}
					}
					else if(name.equals("event_invites"))
					{
						Element friend_requests = (Element)cnode;
						if(friend_requests.getChildNodes() != null && friend_requests.getChildNodes().getLength()> 0)
						{							
							Element friends_node = (Element)friend_requests;
							for(org.w3c.dom.Node uid_node=friends_node.getFirstChild();uid_node != null;uid_node=uid_node.getNextSibling())
							{	
								String uid_name = uid_node.getNodeName();		
								if(uid_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
								{	
									if("eid".equals(uid_name))
									{
										long uid = Long.valueOf(getChildText("eid", (Element)uid_node));
										notify.entInvite.uids.add(uid);
									}
								}
							}		
						}
					}					
				}				
			}
		}		
		return notify;
	}	
	
	public static Notifications parseNotificationGetList(Document doc) 
	{
	    Notifications notifications = new Notifications();
        if(doc == null) return notifications;
        
        NodeList notificationsnode = doc.getElementsByTagName("notification");
        NodeList appsnode       = doc.getElementsByTagName("app_info");
        
        boolean hasNotifications = notificationsnode != null && notificationsnode.getLength()>0;
        boolean hasAppinfo       = appsnode != null && appsnode.getLength()>0;
            
        if(hasNotifications)
        {
            parseNotification(notificationsnode,notifications);
        }
        
        if(hasAppinfo)
        {
            parseAppInfo(appsnode,notifications);
        }            
        return notifications;
	}
	
	static void parseNotification(NodeList notificationsnode,Notifications notifications)
	{
	    ArrayList<Notifications.Notification> list = notifications.notificationlist;
	    
	    for(int i=0;i<notificationsnode.getLength();i++)
        {
            Node notificationnode = notificationsnode.item(i);
            Element notificationelement;
            if(notificationnode.getNodeType() == Element.ELEMENT_NODE)
            {
                notificationelement = (Element)notificationnode;
                Notifications.Notification notification = new Notifications.Notification();
                
                String notification_id = getChildText("notification_id",notificationelement);
                notification.notification_id = !isEmpty(notification_id)?Long.parseLong(notification_id):0L;
                
                String sender_id = getChildText("sender_id",notificationelement);
                notification.sender_id  = Long.parseLong(sender_id);
                
                String recipient_id = getChildText("recipient_id",notificationelement);
                if(isEmpty(recipient_id) == false)
                    notification.recipient_id  = Long.parseLong(recipient_id);
                
                String created_time = getChildText("created_time",notificationelement);
                notification.created_time  = Long.parseLong(created_time)*1000L;
                
                String updated_time = getChildText("updated_time",notificationelement);
                if(isEmpty(updated_time) == false)
                    notification.updated_time  = Long.parseLong(updated_time)*1000L;
                else
                    notification.updated_time = notification.created_time;
                
                String title_html = getChildText("title_html",notificationelement);
                notification.title_html  = title_html;
                           
                String title_text = getChildText("title_text",notificationelement);
                notification.title_text  = title_text;
                
                String body_html = getChildText("body_html",notificationelement);
                notification.body_html  = body_html;
                
                String body_text = getChildText("body_text",notificationelement);
                notification.body_text  = body_text;
                
                String href = getChildText("href",notificationelement);
                notification.href  = href;
        
                String app_id = getChildText("app_id",notificationelement);
                notification.app_id  = (!isEmpty(app_id)?Long.parseLong(app_id):0L);

                String is_unread = getChildText("is_unread",notificationelement);
                notification.is_unread  = (!isEmpty(is_unread)&&is_unread.equals("1")?true:false);
                
                String is_hidden = getChildText("is_hidden",notificationelement);
                notification.is_hidden  = (!isEmpty(is_hidden)&&is_hidden.equals("1")?true:false);
               
                list.add(notification);
            }
        }
	}
	
	static void  parseAppInfo(NodeList appsnode,Notifications notifications)
	{
	    
        ArrayList<Notifications.AppInfo> list = notifications.appinfo;
        
        for(int i=0;i<appsnode.getLength();i++)
        {
            Notifications.AppInfo appinfo = new Notifications.AppInfo();
            Node appinfonode = appsnode.item(i);
            Element appinfoelement;
            if(appinfonode.getNodeType() == Element.ELEMENT_NODE)
            {
                appinfoelement = (Element)appinfonode;
                
                String app_id = getChildText("app_id",appinfoelement);
                appinfo.app_id  = !isEmpty(app_id)?Long.parseLong(app_id):0L;
                
                String api_key = getChildText("api_key",appinfoelement);
                appinfo.api_key  = api_key;
                
                String canvas_name = getChildText("canvas_name",appinfoelement);
                appinfo.canvas_name  = canvas_name;
                
                String display_name = getChildText("display_name",appinfoelement);
                appinfo.display_name  = display_name;
                
                String icon_url = getChildText("icon_url",appinfoelement);
                appinfo.icon_url  = icon_url;
      
                String logo_url = getChildText("logo_url",appinfoelement);
                appinfo.logo_url  = logo_url;
               
                String company_name = getChildText("company_name",appinfoelement);
                appinfo.company_name  = company_name;
      
                String description = getChildText("description",appinfoelement);
                appinfo.description  = description;
                
                String daily_active_users = getChildText("daily_active_users",appinfoelement);
                appinfo.daily_active_users  = !isEmpty(daily_active_users)?Long.parseLong(daily_active_users):0L;
                
                String weekly_active_users = getChildText("weekly_active_users",appinfoelement);
                appinfo.weekly_active_users  = !isEmpty(weekly_active_users)?Long.parseLong(weekly_active_users):0L;
                
                String monthly_active_users = getChildText("monthly_active_users",appinfoelement);
                appinfo.monthly_active_users  = !isEmpty(monthly_active_users)?Long.parseLong(monthly_active_users):0L;
                
                NodeList developers_nodelist = appinfoelement.getElementsByTagName("developer_info");
                
                if(developers_nodelist!=null && developers_nodelist.getLength()>0)
                {
                    ArrayList<DeveloperInfo> developerinfo_list = new ArrayList<DeveloperInfo>();
                    
                    for(int j=0;j<developers_nodelist.getLength();j++)
                    {
                        
                        Node developerinfonode = developers_nodelist.item(j);
                        Element developerinfoelement;
                        if(developerinfonode.getNodeType() == Element.ELEMENT_NODE)
                        {
                            developerinfoelement = (Element)developerinfonode;
                        }
                        else
                        {
                            continue;
                        }
                         DeveloperInfo developerinfo = new Notifications.DeveloperInfo();     
                         
                         String uid = getChildText("uid",developerinfoelement);
                         developerinfo.uid = !isEmpty(uid)?Long.parseLong(uid):0L;
                         
                         String name = getChildText("name",developerinfoelement);
                         developerinfo.name = name;
                         
                         developerinfo_list.add(developerinfo);
                    }
                    
                    appinfo.developer_info = developerinfo_list;
                }
                          
                list.add(appinfo);                      
            }
        }
	}
	
	public static List<PhotoComment> parserPhotoComments(Document doc,FacebookSession session)
	{
	    ArrayList<PhotoComment> comments = new ArrayList<PhotoComment>();
	    NetworkConnectionListener tmp = null;
        if(session != null)
        {
            tmp = session.getBaseActivity();
            if(tmp != null && ActivityBase.class.isInstance(tmp))
            {
                ((ActivityBase)tmp).titleUpdateAfterNetwork();
            }
        }
        
        if(doc == null) return comments;
        
        NodeList list = doc.getElementsByTagName( "photo_comment" );
        
        if(list==null || list.getLength()<1) 
            return comments;
        
        int size = list.getLength();
        for(int i=0;i<size;i++)
        {
            Node comment_node = list.item(i);
            if(tmp != null && ActivityBase.class.isInstance(tmp))
            {
                ((ActivityBase)tmp).updateProgress(i, size);
            }
            if(comment_node.getChildNodes() != null && comment_node.getChildNodes().getLength() >0)
            {
                    PhotoComment photoComment = new PhotoComment();           
                    String pid = getChildText("pid", (Element)comment_node);
                    photoComment.pid = pid;
                    
                    String from = getChildText("from",(Element)comment_node);
                    photoComment.from = Long.valueOf(from);
                    
                    String time = getChildText("time", (Element)comment_node);
                    photoComment.time = Long.valueOf(time)*1000;
                    
                    String text = getChildText("body", (Element)comment_node);
                    photoComment.body = text;
                    
                    String pcid = getChildText("pcid",(Element)comment_node);
                    photoComment.pcid = pcid;
                    
                    comments.add(photoComment); 
            }          
        }
        Collections.sort(comments);
        return comments;  
	}
	
	public static Comments parserStreamComments(Document doc,long source_id,FacebookSession session)
	{
	    NetworkConnectionListener tmp = null;
        if(session != null)
        {
            tmp = session.getBaseActivity();
            if(tmp != null && ActivityBase.class.isInstance(tmp))
            {
                ((ActivityBase)tmp).titleUpdateAfterNetwork();
            }
        }
        
        if(doc == null) return null;
        
        NodeList list = doc.getElementsByTagName( "comment" );
        
        if(list==null || list.getLength()<1) 
            return null;
        
        int size = list.getLength();
	    Comments comment = new Comments();
        for(int i=0;i<size;i++)
        {
            Node comment_node = list.item(i);
            if(tmp != null && ActivityBase.class.isInstance(tmp))
            {
                ((ActivityBase)tmp).updateProgress(i, size);
            }
            if(comment_node.getChildNodes() != null && comment_node.getChildNodes().getLength() >0)
            {
                    Stream.Comments.Stream_Post comment_item= new Stream_Post();                 
                    String fromid = getChildText("fromid", (Element)comment_node);
                    if(fromid == null)
                    {
                        continue;
                    }
                    comment_item.fromid = Long.valueOf(fromid);
                    
                    
                    String time = getChildText("time", (Element)comment_node);
                    comment_item.time = Long.valueOf(time)*1000;
                    
                    String text = getChildText("text", (Element)comment_node);
                    comment_item.text = text;
                    
                    String username = getChildText("username", (Element)comment_node);
                    comment_item.username = username;                    
                    
                    String id = getChildText("id", (Element)comment_node);                                    
                    comment_item.id = id;
                    if(source_id > 0)
                    {
                        comment_item.parentsuid = source_id;
                    }
                    comment.stream_posts.add(comment_item);  
            }          
        }
        
        if(comment.stream_posts!=null && comment.stream_posts.size()>0)
        {
        	java.util.Collections.sort(comment.stream_posts);
            //List<Stream_Post> newlist = reSortComments(comment.stream_posts);
            //comment.stream_posts.clear();
            //comment.stream_posts = newlist;
        }
        
	    return comment;
	}
	
	/*
	public static List<Stream_Post> reSortComments(List<Stream_Post> stream_posts)
	{  
        Stream_Post[] beforesort = new Stream_Post[stream_posts.size()];
        beforesort = stream_posts.toArray(beforesort);
        Arrays.sort(beforesort);            
        return (List<Stream_Post>)Arrays.asList(beforesort);    
	}
	*/
	
	/*public static boolean parserStreamComments(Document doc) 
	{	
		return false;
	}*/
	
	public static boolean parserStreamLike(Document doc) 
	{	
		return false;
	}	
	
	public static List<Notes> parseNotes(Document doc, FacebookSession session) 
	{
		NetworkConnectionListener tmp = null;
		if(session != null)
		{
			tmp = session.getBaseActivity();
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).titleUpdateAfterNetwork();
			}
		}
		
		List<Notes> notes = new ArrayList<Notes>();
		if(doc == null ) return notes;
		
		NodeList list = doc.getElementsByTagName( "note" );
		if(list == null || list.getLength()<1) return notes;

		int size = list.getLength();
		for(int i=0;i<list.getLength();i++)
		{
			Node node = list.item(i);
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).updateProgress(i, size);
			}
			
			if(node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
			{					
				String name = node.getNodeName();
				if(name.equals("note"))
				{
					Element att_elem = (Element)node;
					if(att_elem.getChildNodes() != null && att_elem.getChildNodes().getLength()> 0)
					{
						Notes note = new Notes();
					
						String note_id = getChildText("note_id", (Element)node);
						note.note_id = Long.valueOf(note_id);
						
						String title = getChildText("title", (Element)node);
						note.title = title;
						
						String content = getChildText("content", (Element)node);
						note.content = content;
						
						String created_time = getChildText("created_time", (Element)node);
						note.created_time = Long.valueOf(created_time)*1000;
						
						String updated_time = getChildText("updated_time", (Element)node);
						note.updated_time = Long.valueOf(updated_time)*1000;
						
						String uid = getChildText("uid", (Element)node);
						note.uid = Long.valueOf(uid);
						
					    notes.add(note);
					}
				}
			}
			
	    }		
	    return notes;
	} 
	
	public static List<Group>parseGroup(Document doc, FacebookSession session){
		NetworkConnectionListener tmp = null;
		if(session != null)
		{
			tmp = session.getBaseActivity();
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).titleUpdateAfterNetwork();
			}
		}

		List<Group> groups = new ArrayList<Group>();
		if(doc == null ) return groups;
		
		NodeList list = doc.getElementsByTagName( "group" );
		
		if(list == null || list.getLength()<1) return groups;

		int size = list.getLength();
		for(int i=0;i<list.getLength();i++){
			Node node = list.item(i);
			if(tmp != null && ActivityBase.class.isInstance(tmp))
			{
			    ((ActivityBase)tmp).updateProgress(i, size);
			}
			
			Group group = new Group();
			for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
			{
				if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				{					
					String name = cnode.getNodeName();
					
					if(name.equalsIgnoreCase(Group.Field.VENUE.toString()))
					{
						StringBuffer location = new StringBuffer();
						//GroupLocation gl = new GroupLocation();
						for(Node v_node = cnode.getFirstChild();v_node !=null;v_node = v_node.getNextSibling())
						{	
							if(v_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
							{
								 String v_name = v_node.getNodeName();
								 String v_value = getChildText(v_name, (Element) v_node);								 
								 if(v_value.equals("")) continue;
								 
								 else if(name.equalsIgnoreCase(GroupLocation.Field.STREET.toString())) location.append( v_value +" Street ");//gl.put(GroupLocation.Field.STREET, v_value);
								 else if(name.equalsIgnoreCase(GroupLocation.Field.CITY.toString()))  location.append(v_value + " ");//gl.put(GroupLocation.Field.CITY, v_value);
								 else if(name.equalsIgnoreCase(GroupLocation.Field.STATE.toString()))  location.append(v_value + " ");//gl.put(GroupLocation.Field.STATE, v_value);					 
								 else if(name.equalsIgnoreCase(GroupLocation.Field.COUNTRY.toString())) location.append(v_value + " ");//gl.put(GroupLocation.Field.COUNTRY, v_value);
							}
						}
						//groups[i].put(MailboxThread.Field.PARTICIPANTS,array.toArray());
						group.setLocation(location.toString());
					}
					else
					{				
						//String val  = leafnode.getNodeValue();
						String val = getChildText(name, (Element)node);
						if(val.equals("")) continue;
						else if(name.equalsIgnoreCase(Group.Field.GID.toString())) group.setGid(Long.valueOf(val));
						else if(name.equalsIgnoreCase(Group.Field.NAME.toString())) group.setName(val);
						else if(name.equalsIgnoreCase(Group.Field.NID.toString())) group.setNid(Long.valueOf(val));
						else if(name.equalsIgnoreCase(Group.Field.DESCRIPTION.toString())) group.setDescription(val);
						else if(name.equalsIgnoreCase("anon")) group.setDescription(val);
						
						else if(name.equalsIgnoreCase(Group.Field.GROUP_TYPE.toString())) group.setGroup_typ(val);
						else if(name.equalsIgnoreCase(Group.Field.GROUP_SUBTYPE.toString())) group.setGroup_subtype(val);
						else if(name.equalsIgnoreCase(Group.Field.CREATOR.toString())) group.setCreator(Long.valueOf(val));
						else if(name.equalsIgnoreCase(Group.Field.PIC.toString())) group.setPic(val);
						else if(name.equalsIgnoreCase(Group.Field.PIC_BIG.toString())) group.setPic_big(val);
						else if(name.equalsIgnoreCase(Group.Field.PIC_SMALL.toString())) group.setPic_samll(val);
						else if(name.equalsIgnoreCase(Group.Field.UPDATE_TIME.toString())) group.setUpdate_time(new Date(Long.valueOf(val)*1000L));
						else if(name.equalsIgnoreCase(Group.Field.OFFICE.toString())) group.setOffice(val);
						else if(name.equalsIgnoreCase(Group.Field.WEBSITE.toString())) group.setWebsite(val);		
					}
												
				}		
			}
			groups.add(group);
	   }
		
		return groups;
	}	
	
	public static GroupMembersByRole parseGroupMembersByRole(Document doc){
		
		if(doc == null) return null;
		
		GroupMembersByRole gmr = new GroupMembersByRole();
		NodeList memberlist = doc.getElementsByTagName( "members" );
		NodeList adminlist = doc.getElementsByTagName("admins");
		NodeList officerlist = doc.getElementsByTagName("officers");
		NodeList notrepliedlist = doc.getElementsByTagName("not_replied");
		if(memberlist!=null && memberlist.item(0)!=null){
			
			long[] muids = parseUids(memberlist.item(0));
			if(muids!=null) gmr.put(Role.MEMBERS, muids);
		}
		
		if(adminlist!=null && adminlist.item(0)!=null){
			long[] auids = parseUids(adminlist.item(0));
			if(auids!=null) gmr.put(Role.ADMINS, auids);
		}
		
		if(officerlist!=null && officerlist.item(0)!=null){
			long[] ouids = parseUids(officerlist.item(0));
			if(ouids!=null) gmr.put(Role.OFFICERS, ouids);
		}
		
		if(notrepliedlist!=null && notrepliedlist.item(0)!=null){
			long[] nuids = parseUids(notrepliedlist.item(0));
			if(nuids!=null) gmr.put(Role.NOT_REPLIED, nuids);
		}
       		
		return gmr;	
	}
	
	 public static boolean isMyFriend(Document doc){
		 if ( doc == null ) {
				return false;
		 }
		 boolean retvalue = false;
		 NodeList nodelist = doc.getElementsByTagName("friend_info");
		 if(nodelist==null || nodelist.getLength()<1) return false;
		 Node node = nodelist.item(0);
		 if(node.getNodeType()==Node.ELEMENT_NODE){
			 NodeList c_nodelist = ((Element)node).getElementsByTagName("are_friends");
			 if(c_nodelist!=null && c_nodelist.getLength()>0){
				 Node c_node = c_nodelist.item(0);
				 Log.d(TAG, c_node.getChildNodes()==null?"YES":"NO");
				 String nodevalue = c_node.getChildNodes()!=null && c_node.getChildNodes().getLength()>0?
				                    c_node.getChildNodes().item(0).getNodeValue():null;
				 if(nodevalue !=null && nodevalue.equals("1")){
					 retvalue = true;
				 }
				 else
			     {
			        nodevalue = c_node.getAttributes()!=null && c_node.getAttributes().getLength()>0?c_node.getAttributes().item(0).getNodeValue():"false";
			       retvalue = nodevalue.equals("true");
			     }
			 }
		 }
		 
		 return retvalue;
		 
	 }
	 
	public static int  parsePageinfoResponse(Document doc,ObjectHandler handler)
    {
        int size = 0;
        if(doc == null ) return size;
        
        NodeList list = doc.getElementsByTagName( "page" );
        
        if(list == null || list.getLength()<1) return size;

        int count = list.getLength();
        for(int i=0;i<count;i++)
        {
            Node node = list.item(i);
            Page page = new Page();
            for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
            {
                if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
                {                   
                    String name = cnode.getNodeName();
                    
                    String val = getChildText(name, (Element)node);
                    if(val.equals("")) continue;
                    else if(name.equalsIgnoreCase(Page.Field.PAGE_ID.toString())) page.page_id = Long.valueOf(val);
                    else if(name.equalsIgnoreCase(Page.Field.NAME.toString())) page.name = val;
                    else if(name.equalsIgnoreCase(Page.Field.PAGE_URL.toString())) page.page_url = val;
                    else if(name.equalsIgnoreCase(Page.Field.PIC_SMALL.toString())) page.pic_small = val;
                    else if(name.equalsIgnoreCase(Page.Field.PIC_SQUARE.toString())) page.pic_square = val; 
                    else if(name.equalsIgnoreCase(Page.Field.PIC.toString())) page.pic = val;
                    else if(name.equalsIgnoreCase(Page.Field.PIC_LARGE.toString())) page.pic_large = val;
                    else if(name.equalsIgnoreCase(Page.Field.PIC_BIG.toString())) page.pic_big = val;
                    else if(name.equalsIgnoreCase(Page.Field.TYPE.toString())) page.type = val;
                    else if(name.equalsIgnoreCase(Page.Field.WEBSITE.toString())) page.website = val;   
                    else if(name.equalsIgnoreCase(Page.Field.COMPANY_OVERVIEW.toString())) page.company_overview = val;
                    else if(name.equalsIgnoreCase(Page.Field.FOUNDED.toString())) page.founded = val;
                }       
            }
            if(handler != null)
            {
                handler.process(page);
                page.despose();
                page = null;
                size ++;
            }
        }
            
        return size;
     }
	 
	 public static List<Page> parsePageinfoResponse(Document doc)
	 {
		 List<Page> pages = new ArrayList<Page>();
			if(doc == null ) return pages;
			
			NodeList list = doc.getElementsByTagName( "page" );
			
			if(list == null || list.getLength()<1) return pages;

			int size = list.getLength();
			for(int i=0;i<list.getLength();i++){
				Node node = list.item(i);
				Page page = new Page();
				for(org.w3c.dom.Node cnode=node.getFirstChild();cnode != null;cnode=cnode.getNextSibling())
				{
					if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
					{					
						String name = cnode.getNodeName();
						
						String val = getChildText(name, (Element)node);
						if(val.equals("")) continue;
						else if(name.equalsIgnoreCase(Page.Field.PAGE_ID.toString())) page.page_id = Long.valueOf(val);
						else if(name.equalsIgnoreCase(Page.Field.NAME.toString())) page.name = val;
						else if(name.equalsIgnoreCase(Page.Field.PAGE_URL.toString())) page.page_url = val;
						else if(name.equalsIgnoreCase(Page.Field.PIC_SMALL.toString())) page.pic_small = val;
						else if(name.equalsIgnoreCase(Page.Field.PIC_SQUARE.toString())) page.pic_square = val;	
						else if(name.equalsIgnoreCase(Page.Field.PIC.toString())) page.pic = val;
						else if(name.equalsIgnoreCase(Page.Field.PIC_LARGE.toString())) page.pic_large = val;
						else if(name.equalsIgnoreCase(Page.Field.PIC_BIG.toString())) page.pic_big = val;
						else if(name.equalsIgnoreCase(Page.Field.TYPE.toString())) page.type = val;
						else if(name.equalsIgnoreCase(Page.Field.WEBSITE.toString())) page.website = val;	
						else if(name.equalsIgnoreCase(Page.Field.COMPANY_OVERVIEW.toString())) page.company_overview = val;
						else if(name.equalsIgnoreCase(Page.Field.FOUNDED.toString())) page.founded = val;
					}		
				}
				pages.add(page);
		   }
			
			return pages;
	 }
	 
	 public static List<FriendRelationship> areMyFriends(Document doc){
		 List<FriendRelationship> fids = new ArrayList<FriendRelationship>();
		 if ( doc == null ) {
				return  fids;
		 }

		 NodeList nodelist = doc.getElementsByTagName("friend_info");
		 if(nodelist==null || nodelist.getLength()<1) return fids;
		 for(int i=0;i<nodelist.getLength();i++){
			 Node oneNode = nodelist.item(i);
			 
			 if(oneNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
			 {
				 FriendRelationship fr = new FriendRelationship();
				 
				 String uid1 = getChildText("uid1", (Element) oneNode);
				 String uid2 = getChildText("uid2", (Element) oneNode);				 
				 String are_friends = getChildText("are_friends", (Element) oneNode);	
				 fr.uid1 = Long.parseLong(uid1.trim());
				 fr.uid2 = Long.parseLong(uid2.trim());
				 fr.isFriends = are_friends.trim().equals("1");
				 fids.add(fr);
			 }					
		 }		
         return fids;
	 }
	
	
	private static long[] parseUids(Node node){
		NodeList nodelist = node.getChildNodes();
		if(nodelist==null || nodelist.getLength()<1) return null;		
		long[] uids = new long[nodelist.getLength()];
		
		for(int i=0;i<nodelist.getLength();i++){
			Node cnode = nodelist.item(i);
			if(cnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
			{					
				String name = cnode.getNodeName();
				NodeList leavesnode = cnode.getChildNodes();
				Node leafnode = leavesnode.item(0);
				if(leavesnode==null || leafnode==null) continue;			
				String val  = leafnode.getNodeValue();				
				uids[i] = Long.parseLong(val);
			}		
		}		
		return uids;
	}
	
	
   public static boolean extractBoolean( Node doc ) {
		if ( doc == null ) {
			return false;
		}

		NodeList nlist = doc.getChildNodes();
		String content = "0";
		for(int i=0;i<nlist.getLength();i++)
		{
			Node node = nlist.item(i);
			String nodename = node.getNodeName();
			Log.d(TAG, node.getNodeName()+"**"+node.getNodeValue()+"**"+node.getNodeType());
			if(nodename.indexOf("response")>0)
			{
				if(node.getChildNodes()!=null && node.getChildNodes().getLength()>0)
				{
				    content = node.getChildNodes().item(0).getNodeValue();
				}
				break;
			}
		}
        Log.d(TAG, "==result is =="+content);
		return "0".equals( content ) == true?false:true;
	}
   
   public static Integer extractInteger( Node doc ) {
		if ( doc == null ) {
			return new Integer(-1);
		}
		NodeList nlist = doc.getChildNodes();
		String content = "0";
		for(int i=0;i<nlist.getLength();i++)
		{
			Node node = nlist.item(i);
			String nodename = node.getNodeName();
			Log.d(TAG, node.getNodeName()+"**"+node.getNodeValue()+"**"+node.getNodeType());
			if(nodename.indexOf("response")>0)
			{
				if(node.getChildNodes()!=null&&node.getChildNodes().getLength()>0)
				{
				    content = node.getChildNodes().item(0).getNodeValue();
				}
				break;
			}
		}
      
		return new Integer(content);
	}
   
   public static String extractString( Node doc ) {
		if ( doc == null ) {
			return "";
		}
		NodeList nlist = doc.getChildNodes();
		String content = "";
		for(int i=0;i<nlist.getLength();i++)
		{
			Node node = nlist.item(i);
			String nodename = node.getNodeName();
			Log.d(TAG, node.getNodeName()+"**"+node.getNodeValue()+"**"+node.getNodeType());
			if(nodename.indexOf("response")>0)
			{
				if(node.getChildNodes()!=null && node.getChildNodes().getLength()>0)
				{
				    content = node.getChildNodes().item(0).getNodeValue();
				}
				break;
			}
		}     
		return content;
	}
   
   public static Long extractLong( Node doc ) {
		if ( doc == null ) {
			return new Long(-1);
		}
		NodeList nlist = doc.getChildNodes();
		String content = "0";
		for(int i=0;i<nlist.getLength();i++)
		{
			Node node = nlist.item(i);
			String nodename = node.getNodeName();
			Log.d(TAG, node.getNodeName()+"**"+node.getNodeValue()+"**"+node.getNodeType());
			if(nodename.indexOf("response")>0){
				content = node.getChildNodes()!=null&&node.getChildNodes().getLength()>0?node.getChildNodes().item(0).getNodeValue():"0";		
				break;
			}
		}
		return new Long(content);
	}
   public static String parseUserName(Document doc){
	   if ( doc == null ) {
			return "";
		}
		NodeList nlist = doc.getElementsByTagName("name");
		String content = "";
		if(nlist == null && nlist.getLength()<1) return content;
		 Node node = nlist.item(0);
		 String val = getChildText("name",(Element)node);
		 if(!val.equals("")) content = val;
		return content;
   }
   
   /**
    * <?xml version="1.0" encoding="UTF-8"?>

       <fql_query_response xmlns="http://api.facebook.com/1.0/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" list="true">

       <event_member>

       <rsvp_status>not_replied</rsvp_status>

       </event_member>

      </fql_query_response>
    * @param doc
    * @param session
    * @return
    */
   public static String parseEventRSVPStatus(Document doc , FacebookSession session)
   {
       if ( doc == null ) {
           return "";
       }
       
       NodeList nlist = doc.getElementsByTagName("rsvp_status");
       String content = "";
       if(nlist == null || nlist.getLength()<1) return content;
        Node node = nlist.item(0);
        String val = getChildText("rsvp_status",(Element)node);
        if(!val.equals("")) content = val;
       return content;
   }
}
