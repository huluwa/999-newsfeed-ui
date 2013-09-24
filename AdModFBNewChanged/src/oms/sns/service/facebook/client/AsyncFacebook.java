package oms.sns.service.facebook.client;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.msocial.freefb.service.ObjectHandler;
import com.msocial.freefb.ui.view.ImageRun;
import oms.sns.service.facebook.model.Attachment;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.ExtendedPermission;
import oms.sns.service.facebook.model.Group;
import oms.sns.service.facebook.model.MailboxMessage;
import oms.sns.service.facebook.model.MailboxThread;
import oms.sns.service.facebook.model.Page;
import oms.sns.service.facebook.model.PhoneBook;
import oms.sns.service.facebook.model.UserStatus;
import oms.sns.service.facebook.model.Wall;
import oms.sns.service.facebook.model.Attachment.BundleActionLink;
import twitter4j.threadpool.QueuedThreadPool;


public class AsyncFacebook 
{
	WeakReference<FacebookSession> fs;
	public AsyncFacebook(FacebookSession session)
	{
		fs = new WeakReference<FacebookSession>(session);
	}
	public void setSession(FacebookSession session)
	{
		fs = new WeakReference<FacebookSession>(session);
	}
	
	/*
	 * get wall
	 */
	
	public void getWallAsync(int start, int limit, FacebookListener listener ) 
	{
		 getWallAsync(fs.get().getLogerInUserID(), start,limit,true,listener);
	}	 
	
	public void getWallAsync(long uid, int start, int limit,boolean hasprogress, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(WALL_GET, listener, new Object[]{uid, start, limit,hasprogress}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getWall(fs.get().wallget((Long)args[0], (Integer)args[1], (Integer)args[2],(Boolean)args[3]),(Boolean)args[3]);
            }
        });
	}	 
	
	public void getWallAsync(FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(WALL_GET, listener, new Object[]{}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getWall(fs.get().wallget(fs.get().getLogerInUserID()));
            }
        });
	}	 
		
	public void getWallAsync(long uid, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(WALL_GET, listener, new Object[]{uid}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getWall(fs.get().wallget( (Long)args[0]));
            }
        });
	}
	/**
	 * get Wall by fql
	 * @param uid
	 * @param currentPos
	 * @param limit
	 * @param listener
	 */
	public void getWallStreamAsync(long uid, long start_time, int limit, boolean newpost,
			FacebookListener listener) {
		 getThreadPool().dispatch(new AsyncTask(WALL_GET, listener, new Object[]{uid,start_time,limit, newpost}) 
	     {
	            @SuppressWarnings("unchecked")
	            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
	                listener.getWallStream(fs.get().getWallByfql((Long)args[0],(Long)args[1], (Integer)args[2], (Boolean)args[3]));
	            }
	     });
		
	}	
	
	public void getWalltoWallMessageAsync(long uid,long fromuid,boolean hasprogress,FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(WALL_GET, listener, new Object[]{uid,fromuid,hasprogress}) 
        {
            @SuppressWarnings("unchecked")
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getWall(fs.get().getWalltoWallMessageBatchRun((Long)args[0],(Long)args[1]),(Boolean)args[2]);
            }
        });
	}
	
	/*
	 * post wall
	 */
	public void postWallAsync(long uid, String content, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(WALL_GET, listener, new Object[]{uid, content}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.postWall(postWall((Long)args[0], (String)(args[1])));
            }
        });
	}	 
	

	protected boolean postWall(Long uid, String body) throws FacebookException 
	{		
		return fs.get().wallpost(uid, body);
	}
	
	
	/*
	 * get user log file
	 */
	public void getUserLogoAsync(long uid, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(LOGO_GET, listener, new Object[]{uid}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getUserLogo(getUserLogo((Long)args[0]));
            }
        });
	}	 
	

	protected String getUserLogo(Long uid) throws FacebookException 
	{		
		String url="";
		FacebookUser user = fs.get().getUserInfo(uid, FacebookUser.Field.PIC_SQUARE);
		if(user != null)
		{
			url = user.getPic_square();
		}
		return url;
	}

	/*
	 * get friend ids
	 */
	public void getFriendUIDsAsync(long uid, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(FRIEND_IDS, listener, new Object[]{uid}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getFriendIDs(getFriendIDs((Long)args[0]));
            }
        });
	}	 
	

	protected List<Long> getFriendIDs(Long uid) throws FacebookException 
	{	
		long[] ids = fs.get().getFriendIds();
		List<Long> idlist = new ArrayList<Long>(); 
		if(ids != null)
		{
			for(int i=0;i<ids.length;i++)
			{
				idlist.add(ids[i]);
			}
		}
		
		return idlist;
	}
	
	/*
	 * get user basic information
	 */
	public void getBasicUsersAsync(long[] uids, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(USER_INFO, listener, new Object[]{uids}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getUsers(getUsers((long[])args[0],false));
            }
        });
	}
	
	public void getSimpleUsersAsync(long[] uids, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(USER_INFO, listener, new Object[]{uids}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getSimpleUsers(getSimpleUsers((long[])args[0],false));
            }
        });
	}
	
	public void getPageInfoAsync(Long pageid,FacebookListener listener)
	{
		getThreadPool().dispatch(new AsyncTask(PAGE_INFO, listener, new Object[]{pageid}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getPageInfo(fs.get().getPageInfo((Long)args[0], Page.Field.PAGE_ID,Page.Field.NAME,Page.Field.PAGE_URL,Page.Field.PIC_SMALL,Page.Field.PIC_SQUARE,Page.Field.PIC_BIG));
            }
        });
		
	}
	
	public void getPageAsync(int limit, int offset,long uid,ObjectHandler handler, FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(PAGE_INFO, listener, new Object[]{limit,offset,uid, handler}) 
        {
            @SuppressWarnings("unchecked")
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getPage(fs.get().getPage((Integer)args[0],(Integer)args[1],(Long)args[2], (ObjectHandler)args[3]));
            }
        });
        
    }
	
	/*
     * get user basic information has no process
     */
    public void getBasicUsersAsync(long[] uids,boolean hasprocess, FacebookListener listener ) 
    {
        getThreadPool().dispatch(new AsyncTask(USER_INFO, listener, new Object[]{uids,hasprocess}) 
        {
            @SuppressWarnings("unchecked")
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getUsers(getUsers((long[])args[0],(Boolean)args[1]));
            }
        });
    }
    
    /*
     * get user basic information has no process
     */
    public void getSimpleUsersAsync(long[] uids,boolean hasprocess, FacebookListener listener ) 
    {
        getThreadPool().dispatch(new AsyncTask(USER_INFO, listener, new Object[]{uids,hasprocess}) 
        {
            @SuppressWarnings("unchecked")
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getUsers(getUsers((long[])args[0],(Boolean)args[1]));
            }
        });
    }
    
    /*
     * get user basic information has no process
     */
    public void searchUsersAsync(boolean forfriends, String key, FacebookListener listener ) 
    {
        getThreadPool().dispatch(new AsyncTask(SEARCH_USER_INFO, listener, new Object[]{forfriends,key}) 
        {
            @SuppressWarnings("unchecked")
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getSimpleUsers(searchUsers((Boolean)args[0],(String)args[1]));
            }
        });
    }
	
	public void getCompleteUserInfoAsync(long uid,FacebookListener listener )
    {
        getThreadPool().dispatch(new AsyncTask(USER_INFO, listener, new Object[]{uid})
        {
            @SuppressWarnings("unchecked")
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getCompleteUserInfo(fs.get().getCompeletedUserInfo((Long)args[0]));
            }
        });
    }
	
	public void isMyFriendAsync(Long uid,FacebookListener listener)
	{
		getThreadPool().dispatch(new AsyncTask(IS_MY_FRIENDS, listener, new Object[]{uid}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.isMyFriend(fs.get().isMyFriend((Long)args[0]));
            }
        });
	}
	
	protected List<FacebookUser.SimpleFBUser> searchUsers(boolean forfriends, String key)	
	{
	    //just ignore for friends
	    return null;
	}

	protected List<FacebookUser> getUsers(long[] uids,boolean hasProgress) throws FacebookException 
	{	
		List<FacebookUser> users = fs.get().getUserInfo(uids,hasProgress, FacebookUser.Field.FIRST_NAME,
				                                        FacebookUser.Field.LAST_NAME, 
				                                        FacebookUser.Field.NAME,
				                                        FacebookUser.Field.BIRTHDAY, 
				                                        FacebookUser.Field.PIC_SQUARE,
				                                        FacebookUser.Field.PIC,
				                                        FacebookUser.Field.PIC_SMALL,
				                                        FacebookUser.Field.SEX ,
				                                        FacebookUser.Field.STATUS);
		return users;
	}
	
	protected List<FacebookUser.SimpleFBUser> getSimpleUsers(long[] uids,boolean hasProgress) throws FacebookException 
	{	
		List<FacebookUser.SimpleFBUser> users = fs.get().getSimpleUserInfo(uids,hasProgress,  
				                                        FacebookUser.Field.NAME,
				                                        FacebookUser.Field.BIRTHDAY, 
				                                        FacebookUser.Field.PIC_SQUARE);
		return users;
	}
	
	/*
	 * get friend list information
	 */
	public void getMyFriendsAsync(long uid, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(FRIEND_GET, listener, new Object[]{uid}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getMyFriends(getMyFriends((Long)args[0], true));
            }
        });
	}	 
	
	public void getMyFriendsAsync(long uid, int limit, int offset,ObjectHandler handler, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(FRIEND_GET, listener, new Object[]{uid, limit, offset, handler}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getMyFriends(getMyFriends((Long)args[0], (Integer)args[1], (Integer)args[2], (ObjectHandler)args[3]));
            }
        });
	}	 
	
	public void getMyFriendsAsync(long uid, boolean hasProgress, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(FRIEND_GET, listener, new Object[]{uid, hasProgress}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getMyFriends(getMyFriends((Long)args[0], (Boolean)args[1]));
            }
        });
	}	 
	

	protected List<FacebookUser> getMyFriends(long uid, boolean hasProgress) throws FacebookException 
	{	
		List<FacebookUser> users = fs.get().getFriendList(uid, hasProgress);
		return users;
	}
	
	protected int getMyFriends(long uid, int limit, int offset, ObjectHandler handler) throws FacebookException 
	{	
		return fs.get().getFriendList(uid, limit, offset, handler);		
	}
	
	public void getUpcomingBDFriendsAsync(long uid, boolean hasProgress, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(FRIEND_GET, listener, new Object[]{uid, hasProgress}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getUpcomingBDFriends(fs.get().getBDFriendList((Long)args[0], (Boolean)args[1]));
            }
        });
	}	 
	
	public void getUpcomingBDSimpleFriendsAsync(long uid, boolean hasProgress, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(FRIEND_GET, listener, new Object[]{uid, hasProgress}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getUpcomingBDSimpleFriends(fs.get().getBDSimpleFriendList((Long)args[0], (Boolean)args[1]));
            }
        });
	}	 
	
	
	/*
	 * update user's status
	 */
	public void updateStatusAsync(String content, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(UPDATE_STATUS, listener, new Object[]{content}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.updateStatus(updateStatus((String)args[0]));
            }
        });
	}
	
	public void publishStreamAsync(String content,FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(PUBLISH_STREAM, listener, new Object[]{content}) 
        {
            @SuppressWarnings("unchecked")
			public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.publishStream(fs.get().streampublish((String)args[0],null,null,null,null));
            }
        });
	}
	
	public void publishStreamAsync(String content,Attachment attachment,List<BundleActionLink> actionLinks,Long targetId,FacebookListener listener){
        getThreadPool().dispatch(new AsyncTask(PUBLISH_STREAM, listener, new Object[]{content,attachment,actionLinks,targetId}) 
        {
            @SuppressWarnings("unchecked")
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                String content = args[0]== null?"":(String)args[0];
                Attachment attachment = args[1]==null?null:(Attachment)args[1];
                List<BundleActionLink> actionLinks = ((args[2]!=null)? (List<BundleActionLink>)args[2]:null);
                Long targetId = ((args[3]==null)?null:(Long)args[3]);       
                listener.publishStream(fs.get().stream_publish(content,attachment,actionLinks,targetId,null));
            }
        });
    }
	

	protected boolean updateStatus(String content) throws FacebookException 
	{	
		return fs.get().setStatus(content);		
	}
	
	/*
	 * get status by uid
	 */
	public void getStatusAsync(long uid, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(GET_STATUS, listener, new Object[]{uid}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getStatus(getStatus((Long)args[0]));
            }
        });
	}	 
	

	protected  List<UserStatus> getStatus(long uid) throws FacebookException 
	{	
		return fs.get().getStatusByuid(uid);		
	}
	
	/*
	 * expire the session key
	 */
	public void expireSesssionAsync(String sessionkey, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(EXPIRE_SESSION, listener, new Object[]{sessionkey}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.expireSession(expireSession((String)args[0]));
            }
        });
	}	 
	
	protected String expireSession(String sessionKey) throws FacebookException
	{
		fs.get().ExpireSesssion(sessionKey);
		return sessionKey;
	}
	
	
	/*
	 * get all friends group
	 */
	public void friendGroupsAsync(long uid,int from, int offset, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(FRIEND_GROUPS, listener, new Object[]{uid, from, offset}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.friendGroups(friendGroups((Long)args[0], (Integer)args[1], (Integer)args[2]));
            }
        });
	}	 
	

	protected  List<Group> friendGroups(long uid,int from, int offset ) throws FacebookException 
	{	
		return fs.get().getFriendsGroups(uid, from, offset);
	}
	
	/*
	 * get my groups
	 */
	public void getMyGroupsAsync(long uid, int from, int offset, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(MY_GROUPS, listener, new Object[]{uid, from, offset}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getMyGroups(getMyGroups((Long)args[0], (Integer)args[1], (Integer)args[2]));
            }
        });
	}	 
	

	protected  List<Group> getMyGroups(long uid, int from, int offset) throws FacebookException 
	{	
		return fs.get().getMyGroups(uid, from, offset);		
	}
	
    public void getGroupsAsync(long[] gids, FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(GET_GROUPS, listener, new Object[]{gids}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getGroups(fs.get().getGroupByids((long[])args[0]));
            }
        });
	}
	
	/*
	 * Poke someone
	 */
	public void pokeAsync(long uid, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(POKE, listener, new Object[]{uid}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.poke(poke((Long)args[0]));
            }
        });
	}	 
	

	protected  boolean poke(long uid) throws FacebookException 
	{	
		return fs.get().pokes_post(uid);
		
	}
	
	/*
	 * join group
	 */
	public void joinGroupAsync(long gid,boolean confirm, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(JOIN_GROUP, listener, new Object[]{gid,confirm}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.joinGroup(joinGroup((Long)args[0],((Boolean)args[1]).booleanValue()));
            }
        });
	}	 
	

	protected  boolean joinGroup(long gid,boolean confirm) throws FacebookException 
	{	
		return fs.get().groupsJoin(gid,confirm);
		
	}
	
	/*
	 * get mailbox summary
	 */
	public void getMailInBoxSummaryAsync(long gid, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(MAIL_IN_SUM, listener, new Object[]{gid}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.mailInBoxSummary(mailInBoxSummary((Long)args[0]));
            }
        });
	}	 
	

	protected  List<MailboxThread>  mailInBoxSummary(long gid) throws FacebookException 
	{	
		return fs.get().mailbox_getInbox(0);		
	}
	
	/*
	 * get mailbox summary
	 */
	public void getMailInBoxSummaryAsync(int start, int limit, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(MAIL_IN_SUM, listener, new Object[]{start, limit}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.mailInBoxSummary(fs.get().mailbox_getInbox((Integer)args[0], (Integer)args[1]));
            }
        });
	}
	
	public void batch_run_getMessageThreadAsync(long uid,int limit,int offset,boolean haveProgress,FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(MAIL_IN_SUM, listener, new Object[]{uid,limit, offset, haveProgress}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.batch_run_getMessageThread(fs.get().batch_run_getMessageThread((Long)args[0], (Integer)args[1], (Integer)args[2],(Boolean)args[3]));
            }
        });
	}
	
	public void getMessageThreadAsync(int folderid,long uid,int limit, int offset, boolean haveProgress, FacebookListener listener ) 
    {
        getThreadPool().dispatch(new AsyncTask(MAIL_IN_SUM, listener, new Object[]{folderid,uid,limit, offset, haveProgress}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getMessageThread(fs.get().message_getThreadsInfo((Integer)args[0], (Long)args[1], (Integer)args[2],(Integer)args[3],(Boolean)args[4]));
            }
        });
    }    
	
	public void getMailInBoxSummaryAsync(int start, int limit, boolean haveProgress, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(MAIL_IN_SUM, listener, new Object[]{start, limit, haveProgress}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.mailInBoxSummary(fs.get().mailbox_getInbox((Integer)args[0], (Integer)args[1], (Boolean)args[2]));
            }
        });
	}	 
	
	public void getMailInBoxSummaryInBackAsync(int start, int limit, boolean haveProgress, FacebookListener listener ) 
    {
	    getBackThreadPool().dispatch(new AsyncTask(MAIL_IN_SUM, listener, new Object[]{start, limit, haveProgress}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.mailInBoxSummary(fs.get().mailbox_getInbox((Integer)args[0], (Integer)args[1], (Boolean)args[2]));
            }
        });
    }
	
	/*
	 * get mailbox summary
	 */
	public void getMailSendBoxSummaryAsync(long gid, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(MAIL_SEND_SUM, listener, new Object[]{gid}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.mailSendBoxSummary(mailSendBoxSummary((Long)args[0]));
            }
        });
	}	 
	
	public void getMailSendBoxSummaryInBackAsync(long gid, FacebookListener listener ) 
    {
        getBackThreadPool().dispatch(new AsyncTask(MAIL_SEND_SUM, listener, new Object[]{gid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.mailSendBoxSummary(mailSendBoxSummary((Long)args[0]));
            }
        });
    }    

	protected  List<MailboxThread>  mailSendBoxSummary(long gid) throws FacebookException 
	{	
		return fs.get().mailbox_getOutbox();		
	}
	
	public void getMailSendBoxSummaryAsync(int start, int limit, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(MAIL_SEND_SUM, listener, new Object[]{start, limit}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.mailSendBoxSummary(fs.get().mailbox_getOutbox((Integer)args[0], (Integer)args[1]));
            }
        });
	}	 
	
	public void getMailSendBoxSummaryAsync(int start, int limit, boolean haveProgress, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(MAIL_SEND_SUM, listener, new Object[]{start, limit, haveProgress}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.mailSendBoxSummary(fs.get().mailbox_getOutbox((Integer)args[0], (Integer)args[1], (Boolean)args[2]));
            }
        });
	}
	
	public void getMailSendBoxSummaryInBackAsync(int start, int limit, boolean haveProgress, FacebookListener listener ) 
    {
        getBackThreadPool().dispatch(new AsyncTask(MAIL_SEND_SUM, listener, new Object[]{start, limit, haveProgress}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.mailSendBoxSummary(fs.get().mailbox_getOutbox((Integer)args[0], (Integer)args[1], (Boolean)args[2]));
            }
        });
    }
	
	/*
	 * get thread detail
	 */
	public void getThreadDetailAsync(long tid, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(THREAD_DETAIL, listener, new Object[]{tid}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getThreadDetail(getThreadDetail((Long)args[0]));
            }
        });
	}	 
	

	protected  List<MailboxMessage>  getThreadDetail(long tid) throws FacebookException 
	{	
		return fs.get().mailbox_getThreadMessage(tid);		
	}
	
	public void getThreadDetailAsync(long tid, int start, int limit,FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(THREAD_DETAIL, listener, new Object[]{tid, start, limit}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getThreadDetail(fs.get().mailbox_getThreadMessage((Long)args[0], (Integer)args[1], (Integer)args[2]));
            }
        });
	}	
	
	public void getThreadDetailAsyncInBack(long tid, int start, int limit,FacebookListener listener ) 
    {
        getBackThreadPool().dispatch(new AsyncTask(THREAD_DETAIL, listener, new Object[]{tid, start, limit}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getThreadDetail(fs.get().mailbox_getThreadMessage((Long)args[0], (Integer)args[1], (Integer)args[2]));
            }
        });
    }   
	
	

    public void getMailThreadMessageAsync(long[] tids,int start,int limit,FacebookListener listener)
    {
        getThreadPool().dispatch(new AsyncTask(THREAD_DETAIL, listener, new Object[]{tids,start, limit}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getThreadDetail(fs.get().mailbox_getThreadMessage((long[])args[0], ((Integer)args[1]).intValue(),((Integer)args[2]).intValue()));
            }
        });
    }
    
	
	/*
	 * thread reply
	 */
	public void mailReplyAsync(long tid, String content, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(MAIL_REPLY, listener, new Object[]{tid, content}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.mailReply(mailReply((Long)args[0], (String)args[1]));
            }
        });
	}	 
	

	protected  long mailReply(long tid, String content) throws FacebookException 
	{	
		return fs.get().mailbox_replay(tid, content);		
	}
	
	/*
	 * thread send
	 */
	public void mailSendAsync(long[] uids, String subject,  String content, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(MAIL_SEND, listener, new Object[]{uids, subject, content}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.mailSend(mailSend((long[])args[0], (String)args[1], (String)args[2]));
            }
        });
	}	 
	

	protected  long mailSend(long[] uids , String subject, String content) throws FacebookException 
	{	
		return fs.get().mailbox_send(uids, subject, content);		
	}
	
	/*
	 * get phone books
	 */
	public void getPhoneBooksAsync(long uid, FacebookListener listener ) 
	{
		
	}	 
	public void getPhoneBooksInBackAsync(long uid, int limit, int offset,  FacebookListener listener ) 
    {
        
    }    
	

	protected  List<PhoneBook>  getPhoneBooks(long uid) throws FacebookException 
	{	
		return null;		
	}
	
	protected  List<PhoneBook>  getPhoneBooks(long uid, int  limit, int offset) throws FacebookException 
	{	
		return null;		
	}
	
	public void getContactInfo(long uid,FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(GET_CONTACT_INFO, listener, new Object[]{uid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getContactInfo(fs.get().getContactInfo((Long)args[0]));
            }
        });
	}

	/*
	 * mark read for mail
	 */
	public void markReadAsync(long tid, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(MARK_READ, listener, new Object[]{tid}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.markRead(markRead((Long)args[0]));
            }
        });
	}
	
	protected  boolean  markRead(long tid) throws FacebookException 
	{	
		return fs.get().mailbox_markread(tid);		
	}
	
	public void hasAppPermission_batch_run_async(List<String> methodMap,FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(APP_PERM, listener, new Object[]{methodMap}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                try{
                    listener.hasAppPermission_batch_run(fs.get().hasAppPermission_batch_run((List<String>)args[0]));
                }
                catch(FacebookException ne)
                {
                    if(ne.getErrorCode() == 102)
                    {
                        throw new FacebookException("no valid session");
                    }
                    else
                    {
                        throw ne;
                    }
                }
            }
        });
	}
	
	public void hasAppPermissionAsync(String perm,FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(APP_PERM, listener, new Object[]{perm}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
         		try{
                    listener.hasAppPermission((String)args[0], fs.get().hasAppPermission(ExtendedPermission.getPermission( (String)args[0])));
         		}
         		catch(FacebookException ne)
         		{
         			if(ne.getErrorCode() == 102)
         			{
         				throw new FacebookException("no valid session");
         			}
         			else
         			{
         				throw ne;
         			}
         		}
            }
        });
	}	
	
	public void revokePermissionAsync(String perm,FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(REVOKE_PERM, listener, new Object[]{perm}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.revokePermission((String)args[0], fs.get().revokePermission(ExtendedPermission.getPermission( (String)args[0])));
            }
        });
	}	
	
	public void getFriendsStatusAsync(int from, int offset, FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(GET_ALL_FRIENDS_STATUS, listener, new Object[]{from, offset}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getFriendsStatus(fs.get().getAllMyFriendsLastedStatus((Integer)args[0], (Integer)args[1]));
            }
        });
	}
	
	public void uploadImageAsync(long sid, String filepath,String caption,FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(IMAGE_UPLOAD, listener, new Object[]{sid, filepath,caption}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
         		if(args[2]!=null && ((String)args[2]).length()>0){
         			listener.uploadimage((Long)args[0], fs.get().uploadPhoto((String)args[1],(String)args[2]));
         		}else{
         			listener.uploadimage((Long)args[0],fs.get().uploadPhoto((String)args[1]));
         		}
                
            }
        });
	}
	
	public void uploadImageAsync(long sid, String filepath,String caption,String albumid,FacebookListener listener){
        getThreadPool().dispatch(new AsyncTask(IMAGE_UPLOAD, listener, new Object[]{sid, filepath,caption,albumid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                String aid = (args[3]!=null?(String)args[3]:null);
                if(args[2]!=null && ((String)args[2]).length()>0){ 
                    listener.uploadimage((Long)args[0], fs.get().uploadPhoto((String)args[1],(String)args[2],aid));
                }else{
                    listener.uploadimage((Long)args[0],fs.get().uploadPhotoByAlbumid((String)args[1],aid));
                }
                
            }
        });
    }   
	
	public void uploadVideoAsync(long sid, String filepath,String title,FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(VIDEO_UPLOAD, listener, new Object[]{sid, filepath,title}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
         		if(args[2]!=null && ((String)args[2]).length()>0){
         		   listener.uploadvideo((Long)args[0], fs.get().vedio_post((String)args[1],(String)args[2], ""));
         		}else{
         		   listener.uploadvideo((Long)args[0], fs.get().vedio_post((String)args[1],"", ""));
         		}
         		
            }
        });
	}
	
	public void postLinkAsync(long sid, String linkpath,String comment,FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(LINK_POST, listener, new Object[]{sid, linkpath,comment}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.postlink((Long)args[0], fs.get().links_post((String)args[1], (String)args[2]));
            }
        });
	}
	
	public void getStreamAsync(long fromtime, int limit,boolean newpost,String filter, FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(STREAM_GET, listener, new Object[]{fromtime, limit,newpost, filter}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getStream(fs.get().getOpenStream((Long)args[0], (Integer)args[1],(Boolean)args[2], (String)args[3]));
            }
        });
	}
	
	public void getOpenStreamFilterAsync( long uid, FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(STREAM_FILTER_GET, listener, new Object[]{uid}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getStreamFilter(fs.get().getOpenStreamFilter((Long)args[0]));
            }
        });
	}
	
	
	public void photoAddCommentsAsync(String pid,String comments,FacebookListener listener){
        getThreadPool().dispatch(new AsyncTask(STREAM_ADD_COMMANDS, listener, new Object[]{pid, comments}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.photoAddComments(fs.get().addPhotoComment((String)args[0], (String)args[1]));
            }
        });
    }
	
	public void streamAddCommentsAsync(String post_id, String comments,FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(STREAM_ADD_COMMANDS, listener, new Object[]{post_id, comments}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.streamAddComments(fs.get().addComments((String)args[0], (String)args[1]));
            }
        });
	}
	
	public void getPhotoCommentsAsync(String pid,FacebookListener listener){
	    getThreadPool().dispatch(new AsyncTask(STREAM_GET, listener, new Object[]{pid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getPhotoComment(fs.get().getPhotoComment((String)args[0]));
            }
        });
	}
	
	public void getCommentsAsync(String post_id,long source_id,int offset, int limit, FacebookListener listener){
        getThreadPool().dispatch(new AsyncTask(STREAM_GET, listener, new Object[]{post_id,source_id, offset, limit}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getComments(fs.get().getComments((String)args[0],(Long)args[1], (Integer)args[2], (Integer)args[3]));
            }
        });
    }
	
	public void removeCommentAsync(String comment_id,FacebookListener listener){
	    getThreadPool().dispatch(new AsyncTask(REMOVE_COMMENT, listener, new Object[]{comment_id}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.removeComments(fs.get().removeComments((String)args[0]));
            }
        });
	}
	
	public void streamUnLikeAsync(String post_id, FacebookListener listener) {
		getThreadPool().dispatch(new AsyncTask(STREAM_UNLIKE, listener, new Object[]{post_id}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.streamUnLike(fs.get().streamUnLike((String)args[0]));
            }
        });
		
	}
	public void streamLikeAsync(String post_id,FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(STREAM_LIKE, listener, new Object[]{post_id}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.streamLike(fs.get().streamLike((String)args[0]));
            }
        });
	}
	
	public void getRSVP(long eid,long uid,FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(FACEBOOK_EVENT, listener, new Object[]{eid,uid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getRSVP(fs.get().getRSVPStatus((Long)args[0], (Long)args[1]));
            }
        });
	}
	
	 public void loadOldGuest(long eventid,String rsvp_status, int position,int offset,FacebookListener listener) {
	     getThreadPool().dispatch(new AsyncTask(FACEBOOK_EVENT, listener, new Object[]{eventid,rsvp_status,position,offset}) 
	     {
	            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
	                listener.getEventSimpleMembers(fs.get().getEventSimpleMembers((Long)args[0],(String)args[1],(Integer)args[2], (Integer)args[3]));
	            }
	     });
	 }
	
	public void batch_run_getFacebookEventGuestAsync(long eventid,FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(FACEBOOK_EVENT, listener, new Object[]{eventid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.batch_run_getFacebookEventGuest(fs.get().batch_run_getEventGuest((Long)args[0]));
            }
        });
	}
	
	public void batch_run_getFacebookEventsAsync(FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(FACEBOOK_EVENT, listener, new Object[]{}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.batch_run_getFacebookEvents(fs.get().batch_run_getEvents());
            }
        });
	}
	
	public void getUpcomingEventAsync(int limit,int offset,FacebookListener listener){
	    getThreadPool().dispatch(new AsyncTask(FACEBOOK_EVENT, listener, new Object[]{limit,offset}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getUpcomingEventsByFql(fs.get().getUpcomingEventsByFql((Integer)args[0], (Integer)args[1]));
            }
        });
	}
	
	public void getPastEventAsync(int limit,int offset,FacebookListener listener){
	    getThreadPool().dispatch(new AsyncTask(FACEBOOK_EVENT, listener, new Object[]{limit,offset}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getPastEventByFql(fs.get().getPastEventsByFql((Integer)args[0],(Integer)args[1]));
            }
        });
	}
	
	public void getFacebookEventsAsync(int limit,int offset, FacebookListener listener){
        getThreadPool().dispatch(new AsyncTask(FACEBOOK_EVENT, listener, new Object[]{limit,offset}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getFacebookEvents(fs.get().getEvents((Integer)args[0], (Integer)args[1]));
            }
        });
    }
	
	public void getFacebookUpcomingEventsAsync(FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(FACEBOOK_EVENT, listener, new Object[]{}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getFacebookEvents(fs.get().getUpcomingEvents());
            }
        });
	}
	
	public void facebookEventRSVPAsync(Long eid,String status,FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(FACEBOOK_EVENT_RSVP, listener, new Object[]{eid,status}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.facebookEventRSVP(fs.get().events_rsvp((Long)args[0], (String)args[1]));
            }
        });
	}
	
	public void getNotificationFacebookEventsAsync(long[] eids,FacebookListener listener){
	    getThreadPool().dispatch(new AsyncTask(FACEBOOK_EVENT, listener, new Object[]{eids}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.getFacebookEvents(fs.get().getEvents((long[])args[0]));
            }
        });
	}
	
	public void createEvent(HashMap informap,FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(FACEBOOK_EVENT, listener, new Object[]{informap}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.createEvent(fs.get().createEvent((HashMap)args[0]));
            }
        });
	}
	
	public void cancelEvent(Long eid,FacebookListener listener){
	    getThreadPool().dispatch(new AsyncTask(FACEBOOK_EVENT, listener, new Object[]{eid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.event_cancel(fs.get().event_cancel((Long)args[0],""));
            }
        });
	}
	
	public void requestFriendAsync(long uid,FacebookListener listener)
	{
		getThreadPool().dispatch(new AsyncTask(REQUEST_FRIENDS, listener, new Object[]{uid}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.requestFriend((Long)args[0], fs.get().requestFriend((Long)args[0]));
            }
        });
	}
	
	public void requestPhoneNunberAsync(long uid,FacebookListener listener)
	{
		getThreadPool().dispatch(new AsyncTask(REQUEST_PHONE_NUMBER, listener, new Object[]{uid}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.requestPhoneNumber(fs.get().requestPhoneNumber((Long)args[0]));
            }
        });
	}
	
	public void setContactAsync(String email, String cell, String other,FacebookListener listener)
	{
		getThreadPool().dispatch(new AsyncTask(SET_CONTACT_INFO, listener, new Object[]{email, cell, other}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.setContact(fs.get().setContactInfo((String)args[0], (String)args[1], (String)args[2]));
            }
        });
	}
	
	public void getNotificationsAsync(long uid, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(GET_NOTIFICATIONS, listener, new Object[]{uid}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.geNotifications(fs.get().batch_run_getNotifications((Long)args[0]));
            }
        });
	}
	public void sendNotificationsAsync(long uid, String content, FacebookListener listener ) 
	{
		getThreadPool().dispatch(new AsyncTask(SEND_NOTIFICATIONS, listener, new Object[]{uid, content}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
                listener.sendNotifications(fs.get().sendNotification((String)args[1], (Long)args[0]));
            }
        });
	}
	
	 public void getPokesAsync(boolean hasprogress, FacebookListener listener) {
	     getThreadPool().dispatch(new AsyncTask(GET_POKES, listener, new Object[]{hasprogress}) 
	     {
	            public void invoke(FacebookListener listener,Object[] args) throws FacebookException {
	                listener.getPokes(fs.get().pokes_get((Boolean)args[0]));
	            }
	     }); 
	        
	 }
	
	public void confirmRequestAsync(long uid, boolean confirm, FacebookListener listener) 
	{
		getThreadPool().dispatch(new AsyncTask(CONFIRM_REQEUST, listener, new Object[]{uid, confirm, uid}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
         	{
                listener.confirms((Long)args[2], fs.get().confirm((Long)args[0], (Boolean)args[1]));
            }
        });
	}
	
	public void addAsFriendAsync(long uid, FacebookListener listener) 
	{
		getThreadPool().dispatch(new AsyncTask(ADD_FRIENDS, listener, new Object[]{uid}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
         	{
                listener.addAsFriend((Long)args[0], fs.get().friendsAdd((Long)args[0], null));
            }
        });
	}
	
	public void getGroupMembersAsync(long gid,long offset,long limit,FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(GET_GROUP_MEMBERS, listener, new Object[]{gid,offset,limit}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
         	{
                listener.getGroupMembers(fs.get().getGroupMembersinfo((Long)args[0],(Long)args[1],(Long)args[2]));
            }
        });
	}
	
	public void batch_run_getGroupMemeber_isGroupMember_Async(long gid,long offset,long limit,FacebookListener listener)
   {
	    getThreadPool().dispatch(new AsyncTask(GET_GROUP_MEMBERS, listener, new Object[]{gid,offset,limit}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.batch_run_getGroupMember_isGroupMember(fs.get().batch_run_getGroupMembersinfo_isGroupMember((Long)args[0],(Long)args[1],(Long)args[2]));
            }
        });
	}
	
	public void getGroupMemberSizeAsync(long gid,FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(GET_GROUP_MEMBER_SIZE , listener, new Object[]{gid}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
         	{
                listener.getGroupMemberSize(fs.get().getGroupMembersize((Long)args[0]));
            }
        });
	}
	
	public void areFriendsAsync(List<Long> uid2s,FacebookListener listener){
		getThreadPool().dispatch(new AsyncTask(ARE_FRIENDS , listener, new Object[]{uid2s}) 
        {
         	public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
         	{
                listener.areFriends(fs.get().areMyFriends((List<Long>)args[0]));
            }
        });
	}
	
	public void loadUserAccountInfoBatchAsync(Long uid,Boolean needCallIsFriend,Boolean frompage,FacebookListener listener){
        getThreadPool().dispatch(new AsyncTask(LOAD_USER_ACCOUNTINFO_BATCH , listener, new Object[]{uid,needCallIsFriend,frompage}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException
            {
                listener.loadUserAccountInfoBatch(fs.get().batch_run_getUserAccountInfo((Long)args[0],(Boolean)args[1],(Boolean)args[2]));
            }
        });
    }
	
	public void loadFriendsAndContactBatchAsync(Long uid,FacebookListener listener){
        getThreadPool().dispatch(new AsyncTask(LOAD_USER_CONTACT_BATCH , listener, new Object[]{uid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException
            {
            	HashMap<Integer,Object> map = fs.get().batch_run_getFriendsAndContactInfo((Long)args[0]);
            	listener.loadFriendsAndContactBatch(map);
            }
        });
    }
	
	public void loadFriendsAndContactBatchInBackAsync(Long uid,FacebookListener listener){
        getBackThreadPool().dispatch(new AsyncTask(LOAD_USER_CONTACT_BATCH , listener, new Object[]{uid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException
            {
                HashMap<Integer,Object> map = fs.get().batch_run_getFriendsAndContactInfo((Long)args[0]);
                listener.loadFriendsAndContactBatch(map);
            }
        });
    }
	
	
	
	public void sendEmailAsync(String text,String subject,long[] receivers,FacebookListener listener){
        getThreadPool().dispatch(new AsyncTask(SEND_EMAIL , listener, new Object[]{text,subject,receivers}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.sendEmail(fs.get().sendEmail((String)args[0], (String)args[1], (long[])args[2]));
            }
        });
    }
	
	public void getNotesAsync(long uid,int start, int limit,	FacebookListener listener) 
	{		
	    getThreadPool().dispatch(new AsyncTask(NOTES_GET , listener, new Object[]{uid, start, limit}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.getNotes(fs.get().getNotes((Long)args[0], (Integer)args[1],(Integer)args[2]));
            }
        });
	}   
	
	public void getMyNotesAsync(long uid,int start, int limit,   FacebookListener listener) 
    {       
        getThreadPool().dispatch(new AsyncTask(NOTES_GET , listener, new Object[]{uid, start, limit}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.getNotes(fs.get().getMyNotes((Long)args[0], (Integer)args[1],(Integer)args[2]));
            }
        });
    }   
	
	public void createNotesAsync(String title, String content,   FacebookListener listener) 
    {       
        getThreadPool().dispatch(new AsyncTask(NOTES_CREATE , listener, new Object[]{title, content}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.createNotes(fs.get().createNotes((String)args[0], (String)args[1]));
            }
        });
    }  
	
	public void editNotesAsync(long noteid,String title,String content,FacebookListener listener)
	{
		getThreadPool().dispatch(new AsyncTask(NOTES_CREATE , listener, new Object[]{noteid,title, content}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.createNotes(fs.get().editNotes((Long)args[0],(String)args[1], (String)args[2]));
            }
        });
	}
	
	public void getAppinfoAsync(String app_ids,  FacebookListener listener) {
	    getThreadPool().dispatch(new AsyncTask(NOTIFICATION_GET_LIST , listener, new Object[]{app_ids}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.getAppinfo(fs.get().getApplicationInfo((String)args[0]));
            }
        });
	}
	    
	
	public void getNotificationListAsync(long starttime,boolean includeread, FacebookListener listener) 
    {       
        getThreadPool().dispatch(new AsyncTask(NOTIFICATION_GET_LIST , listener, new Object[]{starttime,includeread}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.getNotificationList(fs.get().notifications_getList((Long)args[0], (Boolean)args[1]));
            }
        });
    }  
	public void notificationMarkReadAsync(long nid,FacebookListener listener) 
    {       
        getThreadPool().dispatch(new AsyncTask(NOTIFICATION_MARK_READ , listener, new Object[]{nid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
            	long[]unids = new long[1];
            	unids[0] = (Long)args[0];
                listener.notificationMarkRead(fs.get().notifications_markRead((Long)args[0]), unids);
            }
        });
    } 
	public void notificationMarkReadArrayAsync(long[] nids,FacebookListener listener) 
    {       
        getThreadPool().dispatch(new AsyncTask(NOTIFICATION_MARK_READ , listener, new Object[]{nids}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.notificationMarkRead(fs.get().notifications_markRead((long[])args[0]), (long[])args[0]);
            }
        });
    }  
	
	private void getFacebookAlbumAsync(FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(ALBUM_GET , listener, null) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.getFacebookAlbum(fs.get().getPhotoAlbumsByUser());
            }
        });
	}
	
	public void getPhotoListByPID(String pid,FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(PHOTO_GET , listener, new Object[]{pid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.getPhotoListByPID(fs.get().getPhotoListById((String)args[0]));
            }
        });
	}
	
	public void batch_run_getAlbum_PhotosAsync(String albumid,FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(ALBUM_GET , listener, new Object[]{albumid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.batch_run_getAlbum_Photos(fs.get().batch_run_getAlbum_Photos((String)args[0]));
            }
        });
	}
	
	public void getPhotoByAlbumAsync(String albumid, FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(ALBUM_GET , listener, new Object[]{albumid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.getPhotosByAlbum(fs.get().getPhotosByAlbum((String)args[0]));
            }
        });
	}
	
	public void photoEditAlbumAsync(String aid,String albumname, String albumlocation, String albumdesc,
            String visible, FacebookListener listener) {
	    getThreadPool().dispatch(new AsyncTask(ALBUM_UPDATE, listener, new Object[]{aid,albumname,albumlocation,albumdesc,visible}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.photoEditAlbum(fs.get().editAlbum((String)args[0], (String)args[1], (String)args[2], (String)args[3], (String)args[4]));
            }
        });
    }
	
   public void editPhotoAsync(String pid, String caption,FacebookListener listener) {
       getThreadPool().dispatch(new AsyncTask(PHOTO_UPDATE, listener, new Object[]{pid,caption}) 
       {
           public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
           {
               listener.editPhoto(fs.get().editPhoto((String)args[0], (String)args[1]));
           }
       });
    }
	
   public void deleteAlbumAsync(String aid, FacebookListener listener) {
       getThreadPool().dispatch(new AsyncTask(ALBUM_DELETE, listener, new Object[]{aid}) 
       {
           public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
           {
               listener.deleteAlbum(fs.get().deleteAlbum((String)args[0]));
           }
       });
    }
  
   public void deletePhotoAsync(String pid, FacebookListener listener) {
       getThreadPool().dispatch(new AsyncTask(PHOTO_DELETE, listener, new Object[]{pid}) 
       {
           public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
           {
               listener.deletePhoto(fs.get().deletePhoto((String)args[0]));
           }
       });
    }
  
   
   public void batch_run_getFacebookAlbumAndPhotoAsync(long uid,FacebookListener listener)
   {
       getThreadPool().dispatch(new AsyncTask(ALBUM_GET , listener, new Object[]{uid}) 
       {
           public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
           {
               listener.getFacebookAlbumAndPhoto(fs.get().batch_run_getAlbumAndPhotoByUser((Long)args[0]));
           }
       });
   }
	public void batch_run_getFacebookAlbumAsync(long uid,FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(ALBUM_GET , listener, new Object[]{uid}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.getFacebookAlbum(fs.get().batch_run_getPhotoAlbumByUser((Long)args[0]));
            }
        });
	}
	
  public void batch_run_getPageInfoAndUserInfo(long[] uids,FacebookListener listener) {
      getThreadPool().dispatch(new AsyncTask(BATCH_GET_PINFO_UINFO, listener, new Object[]{uids}) 
      {
          public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
          {
              listener.batch_run_getPageInfoAndUserInfo(fs.get().batch_run_getPageInfoAndUserInfo((long[])args[0]));
          }
      });
    }
	   
	
	public void createAlbumAsync(String name,FacebookListener listener)
	{
	    getThreadPool().dispatch(new AsyncTask(ALBUM_CREATE , listener, new Object[]{name}) 
        {
            public void invoke(FacebookListener listener,Object[] args) throws FacebookException 
            {
                listener.createPhotoAlbum(fs.get().createPhotoAlbum((String)args[0]));
            }
        });
	}
	
	
	//changed by huadong
    //we just need one thread pool
    public static   QueuedThreadPool threadbackpool;        
    public static QueuedThreadPool getBackThreadPool()
    {
        synchronized(QueuedThreadPool.class)
        {
            if(null == threadbackpool){
                threadbackpool = new QueuedThreadPool(2);
                threadbackpool.setName("back-thread-pool");
                try 
                {
                    threadbackpool.start();
                } catch (Exception e) {}
                
                Runtime.getRuntime().addShutdownHook(new Thread() 
                {
                    public void run() 
                    {
                        stopBackThreadPool();
                    }
                });
            }
        }
        return threadbackpool;
    }
    //end changed

    public static void stopBackThreadPool()
    {
        if(threadbackpool != null)
            try {
                threadbackpool.stop();
            } catch (Exception e) {}
    }
    
	
	public static   QueuedThreadPool threadpool;
    private static final int NetworkPoolSize=10;   
    public static QueuedThreadPool getThreadPool()
    {
        synchronized(QueuedThreadPool.class)
        {
            if(null == threadpool){
                threadpool = new QueuedThreadPool(NetworkPoolSize);
                threadpool.setName("Async-thread-pool");
                try 
                {
                    threadpool.start();
                } catch (Exception e) {}
                
                Runtime.getRuntime().addShutdownHook(new Thread() 
                {
                    public void run() 
                    {
                        stopThreadPool();
                    }
                });
            }
        }
        return threadpool;
    }
    //end changed

    public static void stopThreadPool()
    {
        if(threadpool != null)
            try {
                threadpool.stop();
            } catch (Exception e) {}
    }
	    
    public abstract class AsyncTask implements Runnable {
    	FacebookListener listener;
        Object[] args;
        int method;
        boolean stoped;
        public void setStoped(boolean stoped)
        {
        	this.stoped = stoped;
        }
        public boolean Stoped()
        {
        	return stoped;
        }
        AsyncTask(int method, FacebookListener listener, Object[] args) 
        {
            this.method = method;
            this.listener = listener;
            this.args = args;
        }

        abstract void invoke(FacebookListener listener,Object[] args) throws FacebookException;

        public void run() 
        {
            try 
            {
                invoke(listener,args);
                listener = null;
                args = null;
            }
            catch (FacebookException te) 
            {
                if (null != listener) 
                {
                    listener.onException(te,method, args);
                    listener = null;
                    args = null;
                }
            }
        }
    }
    
    public final static int WALL_GET        = 1;
    public final static int WALL_POST       = 2;
    public final static int LOGO_GET        = 3;
    public final static int FRIEND_IDS      = 4;
    public final static int USER_INFO       = 5;
    public final static int FRIEND_GET      = 115;    
    public final static int CONTACT_INFO    = 6;
    public final static int UPDATE_STATUS   = 7;
    public final static int POKE            = 8;
    public final static int EXPIRE_SESSION  = 9;
    public final static int MARK_READ       = 10;
    public final static int APP_PERM        = 11;
    public final static int PHONE_BOOK      = 12;
    public final static int MAIL_SEND       = 13;
    public final static int THREAD_DETAIL   = 14;
    public final static int MAIL_REPLY      = 15;
    public final static int FRIEND_GROUPS   = 16;
    public final static int MY_GROUPS       = 17;
    public final static int JOIN_GROUP      = 18;
    public final static int MAIL_IN_SUM     = 19;
    public final static int MAIL_SEND_SUM   = 20; 
    public final static int IMAGE_UPLOAD    = 21;
    public final static int VIDEO_UPLOAD    = 22;
    public final static int REVOKE_PERM     = 23;
    public final static int LINK_POST       = 24;
    public final static int GET_ALL_FRIENDS_STATUS = 25;
    public final static int STREAM_GET      = 26;
    public final static int STREAM_ADD_COMMANDS = 27;
    public final static int STREAM_LIKE         = 28;
        
    public final static int FACEBOOK_EVENT        = 30;
    public final static int REQUEST_FRIENDS       = 31;
    public final static int REQUEST_PHONE_NUMBER  = 32;
    public final static int SET_CONTACT_INFO      = 33;
    public final static int GET_NOTIFICATIONS     = 34;
    public final static int CONFIRM_REQEUST       = 35;    
    public final static int PUBLISH_STREAM        = 36;
    public final static int GET_STATUS            = 37;
    public final static int ADD_FRIENDS           = 38;
    
    public final static int IS_MY_FRIENDS         = 39;
    
    public final static int GET_GROUP_MEMBERS     = 40;
    public final static int GET_GROUP_MEMBER_SIZE = 41;
    public final static int ARE_FRIENDS           = 42;
    
    public final static int SEND_EMAIL             = 43;
    public final static int GET_CONTACT_INFO        = 44;
    public final static int GET_POKES              = 45;
    public final static int GET_GROUPS             = 46;
    public final static int FACEBOOK_EVENT_RSVP   = 47;
    
    public final static int SEND_NOTIFICATIONS    = 50;
    public final static int STREAM_UNLIKE       = 51;
    public final static int REMOVE_COMMENT     = 52;
    
    public final static int NOTES_GET            = 60;	
    public final static int NOTES_CREATE         = 61;
    public final static int NOTES_DELETE         = 62;
    
    public final static int NOTIFICATION_GET_LIST            = 70;
    public final static int LOAD_USER_ACCOUNTINFO_BATCH      =  71;
    public final static int LOAD_USER_CONTACT_BATCH          =  72;
    
    public final static int ALBUM_GET                       = 73;
    public final static int ALBUM_CREATE                   = 74;
    public final static int PAGE_INFO                      = 75;
    public final static int NOTIFICATION_MARK_READ         = 76;
    public final static int SEARCH_USER_INFO               = 77;	
    public final static int ALBUM_DELETE                   = 78;
    public final static int ALBUM_UPDATE                   = 79;
    public final static int PHOTO_DELETE                   = 80;
    public final static int PHOTO_UPDATE                   = 81;
    public final static int PHOTO_GET                      = 82;
    public final static int STREAM_FILTER_GET              = 83;
    public final static int BATCH_GET_PINFO_UINFO         = 84;
   
}
