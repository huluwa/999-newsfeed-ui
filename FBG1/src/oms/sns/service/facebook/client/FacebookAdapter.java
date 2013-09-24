package oms.sns.service.facebook.client;

import java.util.HashMap;
import java.util.List;

import oms.sns.service.facebook.model.MessageThreadInfo;
import oms.sns.service.facebook.model.Notes;
import oms.sns.service.facebook.model.Event;
import oms.sns.service.facebook.model.FBNotifications;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.FriendRelationship;
import oms.sns.service.facebook.model.Group;
import oms.sns.service.facebook.model.MailboxMessage;
import oms.sns.service.facebook.model.MailboxThread;
import oms.sns.service.facebook.model.Notifications;
import oms.sns.service.facebook.model.Page;
import oms.sns.service.facebook.model.PhoneBook;
import oms.sns.service.facebook.model.Photo;
import oms.sns.service.facebook.model.PhotoAlbum;
import oms.sns.service.facebook.model.PhotoComment;
import oms.sns.service.facebook.model.PokeResponse;
import oms.sns.service.facebook.model.Stream;
import oms.sns.service.facebook.model.StreamFilter;
import oms.sns.service.facebook.model.UserStatus;
import oms.sns.service.facebook.model.Video;
import oms.sns.service.facebook.model.Wall;
import oms.sns.service.facebook.model.FacebookUser.SimpleFBUser;
import oms.sns.service.facebook.model.Notifications.AppInfo;
import oms.sns.service.facebook.model.Stream.Comments;

public class FacebookAdapter implements FacebookListener{

	public void getWall(List<Wall> wall) {
				
	}
	
	public void getWall(List<Wall> wall,boolean hasprogress) {
		
	}

	public void onException(FacebookException te, int method) {
				
	}
	
	public void  onException(FacebookException te, int method, Object[] args)
	{
		onException(te, method);
	}

	public void postWall(boolean suc) {		
		return;
	}

	public void getUserLogo(String url) {
		// TODO Auto-generated method stub
		return;
	}

	public void getFriendIDs(List<Long> friendIDs) {
		// TODO Auto-generated method stub
		
	}

	public void getUsers(List<FacebookUser> users) {
		// TODO Auto-generated method stub
		
	}
	
	public void updateStatus(boolean suc) {
		// TODO Auto-generated method stub
		return;
	}

	public void getStatus(List<UserStatus> users) {
		// TODO Auto-generated method stub
		return;
	}

	public void poke(boolean suc) {
		// TODO Auto-generated method stub
		return;
	}

	public void expireSession(String session) {
		// TODO Auto-generated method stub
		return;
	}

	public void friendGroups(List<Group> groups) {
		// TODO Auto-generated method stub
		return;
	}

	public void joinGroup(boolean suc) {
		// TODO Auto-generated method stub
		return;
	}

	public void getMyFriends(List<FacebookUser> frds) {
		// TODO Auto-generated method stub
		return;
	}
	public void getMyFriends(int size)
	{
		return;
	}
	public void getUpcomingBDFriends(List<FacebookUser> frds){
		return;
	}

	public void getMyGroups(List<Group> groups) {
		// TODO Auto-generated method stub
		return;
	}

	public void mailSendBoxSummary(List<MailboxThread> mailThread) {
		// TODO Auto-generated method stub
		return;
	}

	public void mailInBoxSummary(List<MailboxThread> mailThread) {
		// TODO Auto-generated method stub
		return;
	}

	public void getThreadDetail(List<MailboxMessage> mailThread) {
		// TODO Auto-generated method stub
		return;
	}

	public void getPhoneBooks(List<PhoneBook> frds) {
		// TODO Auto-generated method stub
		return;
	}

	public void uploadimage(long sid, Photo photo) {	
		return;
	}	
	public void uploadvideo(long sid, Video video){
		return;
	}
	public void postlink(long sid, long linkid){
		return;
	}

	public void markRead(boolean suc) {
		// TODO Auto-generated method stub
		return;
	}
	
	public void mailReply(long tid) {
		// TODO Auto-generated method stub
		return;
	}

	public void mailSend(long tid) {
		// TODO Auto-generated method stub
		return;
	}
	
	public void hasAppPermission(String perm, boolean hasPerm) {
		// TODO Auto-generated method stub
		return;
	}

	public void revokePermission(String chperm, boolean revoked) {
		// TODO Auto-generated method stub
		return;
	}

	public void getFriendsStatus(List<UserStatus> frds) {
		// TODO Auto-generated method stub
		return;
	}

	public void getStream(List<Stream> sts) {
		// TODO Auto-generated method stub
		return;
	}

	public void streamAddComments(String commandID) {
		// TODO Auto-generated method stub
		return;
	}

	public void streamLike(boolean suc) {
		// TODO Auto-generated method stub
		return;
	}
	
	public void getFacebookEvents(List<Event> events){
		return;
	}
	
	public void publishStream(long pid){}
	
	public void createEvent(long eid){}

	public void requestFriend(long uid, boolean suc) {
		// TODO Auto-generated method stub
		return;
	}

	public void requestPhoneNumber(boolean suc) {
		// TODO Auto-generated method stub
		return;
	}

	public void setContact(boolean suc) {
		// TODO Auto-generated method stub
		return;
	}

	public void geNotifications(FBNotifications notifications) {
		// TODO Auto-generated method stub
		return;
	}

	public void confirms(long uid, boolean suc) {
		// TODO Auto-generated method stub
		return;
	}

	public void addAsFriend(long uid, boolean suc) {
		// TODO Auto-generated method stub
		return;
	}

	public void isMyFriend(boolean ismyfriend) {
		// TODO Auto-generated method stub
		
	}
	
	public void getGroupMembers(List<FacebookUser> groupmembers){
		
	}
	
	public void getGroupMemberSize(long groupmembersize){}

	public void areFriends(List<FriendRelationship> fids) {
		
	}

    public void sendEmail(boolean suc) {
    }
    public void getContactInfo(PhoneBook phonebook) {
        // TODO Auto-generated method stub
        return;
    }

    public void getPokes(List<PokeResponse> responsepoke) {
        // TODO Auto-generated method stub
        return;
    }
    public void getGroups(List<Group> groups) {
        // TODO Auto-generated method stub
        return;
    }

    public void facebookEventRSVP(boolean suc) {
        // TODO Auto-generated method stub
        return;
    }
	public void getCompleteUserInfo(FacebookUser user) {
		// TODO Auto-generated method stub
		return;
	}

    public void event_cancel(boolean retvalue) {
        // TODO Auto-generated method stub
        return;
    }

	public void sendNotifications(boolean suc) {
		// TODO Auto-generated method stub
		return;
	}

    public void getComments(Comments comments) {
        // TODO Auto-generated method stub
        
    }

	public void streamUnLike(boolean suc) {
		// TODO Auto-generated method stub
		return;
	}

    public void removeComments(boolean suc) {
        // TODO Auto-generated method stub
        return;
    }

	public void getNotes(List<Notes> notes) {
		// TODO Auto-generated method stub
		return;
	}

    public void getNotificationList(Notifications notifications_getList) {
        // TODO Auto-generated method stub
        return;
    }

    public void createNotes(boolean createNotes) {
        // TODO Auto-generated method stub
        
    }

    public void loadUserAccountInfoBatch(
            HashMap<Integer, Object> batch_run_getUserAccountInfo) {
        // TODO Auto-generated method stub
        
    }

	public void loadFriendsAndContactBatch(HashMap<Integer, Object> map) {
		// TODO Auto-generated method stub
		return;
	}

    public void getFacebookAlbum(List<PhotoAlbum> photoAlbumsByUser) {
        // TODO Auto-generated method stub
        
    }

    public void createPhotoAlbum(PhotoAlbum createPhotoAlbum) {
        // TODO Auto-generated method stub
        
    }

    public void hasAppPermission_batch_run(
            List<Boolean> hasAppPermission_batch_run) {
        // TODO Auto-generated method stub
        
    }

    public void batch_run_getGroupMember_isGroupMember(
            HashMap<Integer, Object> batch_run_getGroupMembersinfo_isGroupMember) {
        // TODO Auto-generated method stub
        
    }

	public void getPageInfo(Page page) {
		// TODO Auto-generated method stub
		return;
	}

	public void notificationMarkRead(boolean notifications_markRead, long[] nids)
	{
		return;
	}

	public void getWallStream(List<Stream> sts) {
		// TODO Auto-generated method stub
		return;
	}
	
    public void batch_run_getFacebookEvents(HashMap<Integer, Object> eventMap) {
        // TODO Auto-generated method stub
        return;
    }

	public void getPhotosByAlbum(List<Photo> photlists) {
		// TODO Auto-generated method stub
		return;
	}
	
    public void getRSVP(String rsvp_status) {
        // TODO Auto-generated method stub
        return;
    }

    public void batch_run_getFacebookEventGuest(
            HashMap<Integer, Object> guestMap) {
        // TODO Auto-generated method stub
        return;
    }
    
    public void getPhotoComment(List<PhotoComment> photoComment) {
        // TODO Auto-generated method stub
        
    }

    public void photoAddComments(boolean suc) {
        // TODO Auto-generated method stub
        return;
    }
    
    public void editPhoto(boolean suc) {
        // TODO Auto-generated method stub
        return;
    }

    public void photoEditAlbum(boolean photoAlbum) {
        // TODO Auto-generated method stub
        return;
    }

    public void deleteAlbum(boolean suc) {
        // TODO Auto-generated method stub
        return;
    }

    public void deletePhoto(boolean suc) {
        // TODO Auto-generated method stub
        return;
    }

    public void getMessageThread(List<MessageThreadInfo> message_getThreadsInfo) {
        // TODO Auto-generated method stub
        return;
    }

	public void getSimpleUsers(List<SimpleFBUser> users) {
		// TODO Auto-generated method stub
		return;
	}

	public void getUpcomingBDSimpleFriends(List<SimpleFBUser> frds) {
		// TODO Auto-generated method stub
		return;
	}

    public void getEventSimpleMembers(List<SimpleFBUser> eventSimpleMembers) {
        // TODO Auto-generated method stub
        
    }

    public void getUpcomingEventsByFql(List<Event> upcomingEventsByFql) {
        // TODO Auto-generated method stub
        
    }

    public void getPastEventByFql(List<Event> pastEventsByFql) {
        // TODO Auto-generated method stub
        
    }

    public void getPhotoListByPID(List<Photo> photoListById) {
        // TODO Auto-generated method stub
        
    }

    public void batch_run_getAlbum_Photos(
            HashMap<Integer, Object> batch_run_getAlbum_Photos) {
        // TODO Auto-generated method stub
        
    }

    public void batch_run_getMessageThread(
            HashMap<Integer, Object> batch_run_getMessageThread) {
        // TODO Auto-generated method stub
        
    }

    public void getFacebookAlbumAndPhoto(
            HashMap<Integer, Object> batch_run_getAlbumAndPhotoByUser) {
        // TODO Auto-generated method stub
        
    }

	public void getStreamFilter(List<StreamFilter> openStreamFilter) {
		// TODO Auto-generated method stub
		
	}

    public void getPage(List<Page> page) {
        // TODO Auto-generated method stub
        
    }

    public void getPage(int pagecount) {
        // TODO Auto-generated method stub
        return;
    }

    public void getAppinfo(List<AppInfo> applicationInfo) {
        // TODO Auto-generated method stub
        
    }

    public void batch_run_getPageInfoAndUserInfo(
            HashMap<Integer, Object> batch_run_getPageInfoAndUserInfo) {
        // TODO Auto-generated method stub
        
    }
}
