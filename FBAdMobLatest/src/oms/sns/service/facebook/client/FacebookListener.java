package oms.sns.service.facebook.client;

import java.util.HashMap;
import java.util.List;

import oms.sns.service.facebook.model.EventMembersByStatus;
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

public interface FacebookListener 
{
	void onException(FacebookException te, int method);
	void onException(FacebookException te, int method, Object[] args);
	void getWall(List<Wall> wall);
	void getWall(List<Wall> wall,boolean hasprogress);
	void postWall(boolean postWall);
	void getUserLogo(String userLogo);
	void getFriendIDs(List<Long> friendIDs);
	void getSimpleUsers(List<FacebookUser.SimpleFBUser> users);
	void getUsers(List<FacebookUser> users);
	void updateStatus(boolean updateStatus);
	void getStatus(List<UserStatus> status);
	void poke(boolean poke);
	void expireSession(String sKey);
	void friendGroups(List<Group> friendGroups);
	void joinGroup(boolean joinGroup);
	void getMyFriends(List<FacebookUser> myFriends);
	void getMyFriends(int size);
	public void getUpcomingBDFriends(List<FacebookUser> frds);
	public void getUpcomingBDSimpleFriends(List<FacebookUser.SimpleFBUser> frds);
	void getMyGroups(List<Group> myGroups);
	void mailInBoxSummary(List<MailboxThread> mails);
	void mailSendBoxSummary(List<MailboxThread> mailSendBoxSummary);	
	void getThreadDetail(List<MailboxMessage> threadDetail);
	void getPhoneBooks(List<PhoneBook> phoneBooks);
	
	void uploadimage(long sid,Photo photo);
	void uploadvideo(long sid,Video video);
	void postlink(long sid, long links_post);
	
	void markRead(boolean markRead);
	void mailReply(long tid);
	void mailSend(long tid);
		
	void hasAppPermission(String string, boolean hasAppPermission);
	void revokePermission(String string, boolean revokePermission);	
	void getFriendsStatus(List<UserStatus> allMyFriendsStatus);
	void getStream(List<Stream> openStream);
	void streamAddComments(String commentID);
	void streamLike(boolean streamLike);
	
	void getFacebookEvents(List<Event> events);
	void publishStream(long pid);
	
	void createEvent(long eid);
	void requestFriend(long uid, boolean requestFriend);
	void requestPhoneNumber(boolean suc);
	void setContact(boolean setContactInfo);
	void geNotifications(FBNotifications geNotifications);
	void confirms(long uid, boolean confirm);
	void addAsFriend(long uid, boolean friendsAdd);
	
	void isMyFriend(boolean ismyfriend);
    void getGroupMembers(List<FacebookUser> groupmembers);
    void getGroupMemberSize(long groupmembersize);
    void areFriends(List<FriendRelationship> fids);
    void sendEmail(boolean suc);
    void getContactInfo(PhoneBook contactInfo);
    void getPokes(List<PokeResponse> responsepoke);
    void getGroups(List<Group> groups);
    void facebookEventRSVP(boolean suc);
	void getCompleteUserInfo(FacebookUser compeletedUserInfo);
    void event_cancel(boolean event_cancel);
	void sendNotifications(boolean suc);
    void getComments(Comments comments);
	void streamUnLike(boolean suc);
    void removeComments(boolean removeComments);
	void getNotes(List<Notes> notes);
    void getNotificationList(Notifications notifications_getList);
    void createNotes(boolean createNotes);
    void loadUserAccountInfoBatch(HashMap<Integer, Object> batch_run_getUserAccountInfo);
	void loadFriendsAndContactBatch(HashMap<Integer, Object> map);
    void getFacebookAlbum(List<PhotoAlbum> photoAlbumsByUser);
    void createPhotoAlbum(PhotoAlbum createPhotoAlbum);
    void hasAppPermission_batch_run(List<Boolean> hasAppPermission_batch_run);
    void batch_run_getGroupMember_isGroupMember(
            HashMap<Integer, Object> batch_run_getGroupMembersinfo_isGroupMember);
	void getPageInfo(Page pageInfo);
	void notificationMarkRead(boolean notifications_markRead, long[] nids);
	void getWallStream(List<Stream> wallByfql);
	void getPhotosByAlbum(List<Photo> photosByAlbum);
    void batch_run_getFacebookEvents(HashMap<Integer, Object> batch_run_getEvents);
    void getRSVP(String status);
    void batch_run_getFacebookEventGuest(
            HashMap<Integer, Object> batch_run_getEvents);
    void getPhotoComment(List<PhotoComment> photoComment);
    void photoAddComments(boolean addPhotoComment);
    void photoEditAlbum(boolean photoEditAlbum);
    void editPhoto(boolean editPhoto);
    void deleteAlbum(boolean deleteAlbum);
    void deletePhoto(boolean deletePhoto);
    void getMessageThread(List<MessageThreadInfo> message_getThreadsInfo);
    void getEventSimpleMembers(List<SimpleFBUser> eventSimpleMembers);
    void getUpcomingEventsByFql(List<Event> upcomingEventsByFql);
    void getPastEventByFql(List<Event> pastEventsByFql);
    void getPhotoListByPID(List<Photo> photoListById);
    void batch_run_getAlbum_Photos(
            HashMap<Integer, Object> batch_run_getAlbum_Photos);
    void batch_run_getMessageThread(
            HashMap<Integer, Object> batch_run_getMessageThread);
    void getFacebookAlbumAndPhoto(
            HashMap<Integer, Object> batch_run_getAlbumAndPhotoByUser);
	void getStreamFilter(List<StreamFilter> openStreamFilter);
    void getPage(List<Page> page);
    void getPage(int pagesize);
    void getAppinfo(List<AppInfo> applicationInfo);
    void batch_run_getPageInfoAndUserInfo(
            HashMap<Integer, Object> batch_run_getPageInfoAndUserInfo);
	
}
