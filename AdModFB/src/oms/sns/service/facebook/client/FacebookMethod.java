package oms.sns.service.facebook.client;

import java.util.HashMap;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import com.android.internal.http.multipart.FilePart;

public abstract class FacebookMethod <T>{
	private String format; //xml, json
	protected String methodName;
	private boolean _takefiles=false;
	
	public FacebookMethod(String methodName, String format)
	{
		this.methodName = methodName;
		this.format = format;
	}	
	
	public boolean takefiles()
	{
		return _takefiles;
	}
	
	public abstract T parseResponse(String response) throws FacebookException, InvalidSesssionException, NoExtPermissionException,FacebookPermissionErrorException;
	
	public String getFormat(){return format;}
	public String getMethodName(){return methodName;}
	
	public static HashMap<String, String> extPermMapMethod = new HashMap<String, String>();
	static 
	{
		
		extPermMapMethod.put(OpenStream.STREAM_GET,        "read_stream");
		extPermMapMethod.put(OpenStream.STREAM_PUBLISH,    "publish_stream");
		extPermMapMethod.put(Events.EVENTS_RSVP,           "rsvp_event");		
		extPermMapMethod.put(Notifications.SEND_EMAIL,     "email");
		extPermMapMethod.put(Users.SET_STATUS,     "status_update");
		extPermMapMethod.put(Photos.UPLOAD,        "photo_upload");
		extPermMapMethod.put(Events.EVENTS_CREATE, "create_event");
		extPermMapMethod.put(Video.VIDEO_UPLOAD,   "video_upload");
		extPermMapMethod.put(Link.SHARE_LINK,      "share_item");
	}
	
	public static String  getExtPermission(String method)
	{
		String perm =  extPermMapMethod.get(method);
		if(perm == null)
		{
			perm = method;
		}
		return perm;
	}
	
	public static String  getExtPermissionName(String method)
    {
        String perm =  extPermMapMethod.get(method);        
        return perm;
    }
    
	
	public static final class Auth
	{
		public static final String CREATE_TOKEN = "facebook.auth.createToken";
		public static final String SESSION_EXPIRE = "facebook.auth.expireSession";
		public static final String GET_SESSION = "facebook.auth.getSession";
		public static final String LOGIN = "facebook.auth_login";
		public static final String LOGIN_NOSESSION = "facebook.auth.login";		
		public static final String REVOKE_EXTPERMISSION = "facebook.auth.revokeExtendedPermission";
	}
	
	// Permissions
	public static final class Permissions
	{
		public static final String  PERM_GRANT_API_ACCESS           = ("facebook.permissions.grantApiAccess");
		public static final String  PERM_CHECK_AVAILABLE_API_ACCESS = ("facebook.permissions.checkAvailableApiAccess");
		public static final String  PERM_REVOKE_API_ACCESS          = ("facebook.permissions.revokeApiAccess");
		public static final String  PERM_CHECK_GRANTED_API_ACCESS   = ("facebook.permissions.checkGrantedApiAccess");
	}

	public static final class Users
	{
		public static final String GET_INFO = "facebook.users.getInfo";
		public static final String GET_CONTACT_INFO = "facebook.users.getcontactinfo";		
		public static final String GET_LOGGED_IN_USER = "facebook.users.getLoggedInUser";
		public static final String IS_APP_ADDED = "facebook.users.isAppAdded";
		public static final String HAS_APP_PERMISSION = "facebook.users.hasAppPermission";
		public static final String SET_STATUS = "facebook.users.setStatus";
		public static final String GET_CONTACT_INFO_EXT = "facebook.users_getContactInfo";
		public static final String SET_CONTACT_INFO = "facebook.user_setContactInfo";
	}
	
	public static final class Status{
		public static final String STATUS_GET = "facebook.status.get";
	}

	public static final class Fql
	{
		public static final String QUERY = "facebook.fql.query";
	}

	public static final class Events
	{
		public static final String GET = "facebook.events.get";
		public static final String EVENTS_CREATE = "facebook.events.create";
		public static final String GET_MEMBERS = "facebook.events.getMembers";
		public static final String EVENTS_CANCEL = "facebook.events.cancel";
		public static final String EVENTS_RSVP   = "facebook.events.rsvp";
	}
	
	public static final class WALL
	{
		public static final String WALL_POST = "facebook.wall_post";
		public static final String WALL_GET = "facebook.wall_get";
	}
	
	public static final class Message
	{
	    public static final String MESSAGE_GETTHREAD_INFO = "facebook.message.getThreadsInFolder";
	}
	
	public static final class Mailbox
	{
		public static final String MAILBOX_GETINBOX = "facebook.mailbox_getInbox";
		public static final String MAILBOX_GETOUTBOX = "facebook.mailbox_getOutbox";
		public static final String MAILBOX_GETTHREAD = "facebook.mailbox_getThread";
		public static final String MAILBOX_GETTHTEADMESSAGES = "facebook.mailbox_getThreadMessages";
		public static final String MAILBOX_SEND = "facebook.mailbox_send";
		public static final String MAILBOX_REPLY = "facebook.mailbox_reply";
	    public static final String MAILBOX_MARKREAD = "facebook.mailbox_markRead";
	    public static final String MAILBOX_MARKUNREAD = "facebook.mailbox_markUnread";
	}

	public static final class Friends
	{
		public static final String GET = "facebook.friends.get";
		public static final String GET_APP_USERS = "facebook.friends.getAppUsers";
		public static final String GET_REQUESTS = "facebook.friends.getRequests";
		public static final String ARE_FRIENDS = "facebook.friends.areFriends";
		public static final String FRIENDS_ADD = "facebook.friends_add";
		public static final String FRIENDS_CONFIRM = "facebook.friends_confirm";
	}

	public static final class Photos
	{
		public static final String GET = "facebook.photos.get";
		public static final String GET_ALBUMS = "facebook.photos.getAlbums";
		public static final String GET_TAGS = "facebook.photos.getTags";
		public static final String CREATE_ALBUM = "facebook.photos.createAlbum";
		public static final String ADD_TAG = "facebook.photos.addTag";
		public static final String UPLOAD = "facebook.photos.upload";
		public static final String ADD_COMMENTS = "facebook.photos.addComment";
		public static final String GET_COMMENTS = "facebook.photos.getComments";
		public static final String EDIT_ALBUM   = "facebook.photos.editAlbum";
		public static final String DELETE_ALBUM = "facebook.photos.deleteAlbum";
		public static final String EDIT_PHOTO   = "facebook.photos.editPhoto";
		public static final String DELETE_PHOTO = "facebook.photos.deletePhoto";
	}

	public static final class Notifications
	{
		public static final String GET = "facebook.notifications.get";
		public static final String SEND = "facebook.notifications.send";
		public static final String SEND_EMAIL = "facebook.notifications.sendEmail";
		public static final String GET_LIST  = "facebook.notifications.getList";
		public static final String MARK_READ = "facebook.notifications.markRead";
	}
	
	public static final class FBNotes
	{
		public static final String GET    = "facebook.notes.get";
		public static final String DELETE = "facebook.notes.delete";
		public static final String CREATE = "facebook.notes.create";
		public static final String EDIT   = "facebook.notes.edit";
	}

	public static final class Groups
	{
		public static final String GET = "facebook.groups.get";
		public static final String GET_MEMBERS = "facebook.groups.getMembers";
		public static final String GROUPS_JOIN = "facebook.groups_join";
	}

	public static final class Profile
	{
		public static final String SET_FBML = "facebook.profile.setFBML";
		public static final String GET_FBML = "facebook.profile.getFBML";
	}

	public static final class Fbml
	{
		public static final String REFRESH_REF_URL = "facebook.fbml.refreshRefUrl";
		public static final String REFRESH_IMG_SRC = "facebook.fbml.refreshImgSrc";
		public static final String SET_REF_HANDLE = "facebook.fbml.setRefHandle";
	}

	public static final class Feed
	{
		public static final String PUBLISH_ACTION_OF_USER = "facebook.feed.publishActionOfUser";
		public static final String PUBLISH_STORY_TO_USER = "facebook.feed.publishStoryToUser";
		public static final String PUBLISH_TEMPLATIZED_ACTION = "facebook.feed.publishTemplatizedAction";
	}

	public static final class Marketplace
	{
		public static final String CREATE_LISTING         = "facebook.marketplace.createListing";
		public static final String GET_CATEGORIES         = "facebook.marketplace.getCategories";
		public static final String GET_SUBCATEGORIES      = "facebook.marketplace.getSubcategories";
		public static final String GET_LISTINGS           = "facebook.marketplace.getListings";
		public static final String SEARCH                 = "facebook.marketplace.search";
		public static final String REMOVE_LISTING         = "facebook.marketplace.removeListing";
	}
	public static final class Pages
	{
		public static final String GET_INFO               = "facebook.pages.getInfo";
		public static final String IS_APP_ADDED           = "facebook.pages.isAppAdded";
		public static final String IS_ADMIN               = "facebook.pages.isAdmin";
		public static final String IS_FAN                 = "facebook.pages.isFan";
	}
	public static final class Phonebook
	{
		public static final String PHONEBOOK_LOOKUP       = "PHONEBOOK_LOOKUP";
		public static final String PHONEBOOK_REQUEST      = "facebook.phonebook_request";
	}
	
	public static final class Video
	{
		public static final String VIDEO_UPLOAD           = "facebook.video.upload";		
	}
	
	public static final class Link
	{
		public static final String SHARE_LINK             = "facebook.links.post";		
	}
	
	public static final class Poke{
		public static final String POKE_POST              = "facebook.pokes_poke";
		public static final String POKE_GET               = "facebook.pokes_get";
	}
	
	public static final class Comments_new{
	    public static final String COMMENTS_ADD          = "facebook.comments.add";
	    public static final String COMMENTS_REMOVE       = "facebook.comments.remove";
	}
	
	public static final class OpenStream
	{
		public static final String STREAM_PUBLISH         = "facebook.stream.publish";
		public static final String STREAM_REMOVE          = "facebook.stream.remove";
		
		public static final String STREAM_GET             = "facebook.stream.get";
		
		public static final String STREAM_GET_COMMENTS    = "facebook.stream.getComments";
		public static final String STREAM_ADD_COMMENTS    = "facebook.stream.addComment";
		public static final String STREAM_REMOVE_COMMENTS = "facebook.stream.removeComment";
		
		public static final String STREAM_ADD_LIKE        = "facebook.stream.addLike";		
		public static final String STREAM_REMOVE_LIKE     = "facebook.stream.removeLike";
		
		
		public static final String STREAM_GET_Filters     = "facebook.stream.getFilters";
		
	}
	
	public static final class BatchRun{
		public static final String BATCH_RUN              = "facebook.batch.run";
	}
}


