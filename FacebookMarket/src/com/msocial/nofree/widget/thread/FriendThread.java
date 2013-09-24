package com.msocial.nofree.widget.thread;

import java.util.ArrayList;
import java.util.List;

import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.widget.model.LitePhoneBook;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.FacebookUser;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import static com.msocial.nofree.widget.StaticFlag.*;

public class FriendThread{
	static final String TAG = "FriendThread";
	final boolean DEBUG = true;

	final int GET_DB_FRIEND = 1;
	final int GET_WEB_FRIEND = 2;
	final int CALLBACK_FRIEND = 3;

	final int delay = 1;

	AsyncFacebook facebookA;
	Context mContext;
	FriendHandler mHandler;
	SocialORM mSocialORM;
	FacebookSession perm;
	boolean isFirst;
	HandlerThread mHandlerThread;
	public boolean isProcessing = false;
	private Object mLock = new Object();
	private static FriendThread mFriendThread = null;
	ArrayList<LitePhoneBook> list;

	private FriendThread() {

	}

	private FriendThread(Context context, FacebookSession perm,
			AsyncFacebook facebookA, SocialORM orm) {
		this.mContext = context;
		this.facebookA = facebookA;
		this.mSocialORM = orm;
		this.perm = perm;
		mHandlerThread = new HandlerThread(TAG);
		mHandlerThread.start();
		mHandler = new FriendHandler(mHandlerThread.getLooper());
	}

	public static FriendThread getInstance(Context context,
			FacebookSession perm, AsyncFacebook facebookA, SocialORM orm) {
		Log.d(TAG, "using friend thread");
		if (mFriendThread == null) {
			mFriendThread = new FriendThread(context, perm, facebookA, orm);
		}
		return mFriendThread;
	}

	public void update(boolean isFirst) {
		if (isFirst) {
			this.isFirst = isFirst;
			mHandler.obtainMessage(GET_WEB_FRIEND).sendToTarget();
		} else {
			if(mHandler.hasMessages(GET_WEB_FRIEND)||isProcessing){
				Log.d(TAG, "your request is ignored");
				return;
			}
			this.isFirst = isFirst;
			mHandler.obtainMessage(GET_DB_FRIEND).sendToTarget();
			Message dmsg = mHandler.obtainMessage(GET_WEB_FRIEND);
			mHandler.sendMessageDelayed(dmsg, delay * 1000);
		}
	}

	private void getFriends() {
		synchronized (mLock) {
			isProcessing = true;
		}
		facebookA.getMyFriendsAsync(perm.getLogerInUserID(), true,
				new FacebookAdapter() {
					@Override
					public void getMyFriends(List<FacebookUser> frds) {
						synchronized (mLock) {
							isProcessing = false;
						}
						mHandler.post(new RunTask(frds) {
							public void run() {
								FacebookUser[] us = new FacebookUser[obj.size()];
								us = obj.toArray(us);
								new SaveFacebookUsers(mSocialORM).execute(us);
							}
						});
					}

					@Override
					public void onException(FacebookException e, int method) {
						Log.d(TAG, e.toString());
						synchronized (mLock) {
							isProcessing = false;
						}
					}
				});
	}

	private class FriendHandler extends Handler {
		public FriendHandler(Looper l) {
			super(l);
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_DB_FRIEND: {
				List<FacebookUser> users = mSocialORM.getAllFacebookUsers();
				Log.d(TAG, "users " + users.size());
				if (users != null && users.size() != 0) {
					list = new ArrayList<LitePhoneBook>();
					for (FacebookUser user : users) {
						LitePhoneBook lite = new LitePhoneBook();
						lite.uid = user.uid;
						lite.username = user.name;
						lite.pic_square = user.pic_square;
						list.add(lite);
					}
					mHandler.obtainMessage(CALLBACK_FRIEND).sendToTarget();
				}

				break;
			}
			case GET_WEB_FRIEND: {
				if (DEBUG)
					Log.d(TAG, "GET_FRIEND");
				getFriends();
				break;
			}
			case CALLBACK_FRIEND: {
				if (DEBUG)
					Log.d(TAG, "CALLBACK_FRIEND");
				Intent result = new Intent(ACTION_FRIEND);
				result.putExtra(FLAG, FRIEND_CALLBACK_RESULT);
				result.putExtra(DATA_FRIEND, list);
				mContext.sendBroadcast(result);
				break;
			}
			default:
			}
		}
	}

	private static class RunTask implements Runnable {
		List<FacebookUser> obj;

		public RunTask(List<FacebookUser> obj) {
			super();
			this.obj = obj;
		}

		public void run() {
		}
	}

	private class SaveFacebookUsers extends
			android.os.AsyncTask<FacebookUser, Void, Void> {

		public SaveFacebookUsers(SocialORM orm) {
			super();
			this.orm = orm;
			Log.d(TAG, "create SaveFacebookUsers");
		}

		public SocialORM orm;

		@Override
		protected Void doInBackground(FacebookUser... pbs) {
			if (pbs != null) {
				Log.d(TAG, "exec SaveFacebookUsers");
				for (FacebookUser pb : pbs) {
					orm.addFacebookUser(pb);
				}
			}
			if (isFirst) {
				mHandler.obtainMessage(GET_DB_FRIEND).sendToTarget();
			}
			return null;
		}

	}
}
