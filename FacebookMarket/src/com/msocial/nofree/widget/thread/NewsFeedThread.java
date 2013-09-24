package com.msocial.nofree.widget.thread;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.widget.model.LiteStream;
import com.msocial.nofree.widget.provider.WidgetORM;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Stream;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import static com.msocial.nofree.widget.StaticFlag.*;

public class NewsFeedThread{
	static final String TAG = "NewsFeedThread";
	final boolean DEBUG = true;

	final int GET_DB_NEWSFEED = 0;
	final int GET_WEB_NEWSFEED = 1;
	final int STORE_NEWSFEED = 2;
	final int CALLBACK_NEWSFEED = 3;
	final int UPDATE_STATUS = 4;

	private int WAIT_TO_GET_FROM_WEB = 5;

	private long lasttime = 0;
	public int limitation = 10;
	final int delay = 1;

	AsyncFacebook facebookA;
	Context mContext;
	NewsFeedHandler mHandler;
	SocialORM mSocialORM;
	WidgetORM mWidgetORM;
	FacebookSession perm;

	HandlerThread mHandlerThread;

	private Object mLock = new Object();
	private static NewsFeedThread mNewsFeedThread;

	public boolean isProcessing = false;
	boolean isFirst = true;
	ArrayList<LiteStream> streams;

	private NewsFeedThread() {

	}

	private NewsFeedThread(Context context, FacebookSession perm,
			AsyncFacebook facebookA, SocialORM sorm, WidgetORM worm) {
		this.mContext = context;
		this.facebookA = facebookA;
		this.mSocialORM = sorm;
		this.mWidgetORM = worm;
		this.perm = perm;
		mHandlerThread = new HandlerThread(TAG);
		mHandlerThread.start();
		mHandler = new NewsFeedHandler(mHandlerThread.getLooper());
	}

	public static NewsFeedThread getInstance(Context context,
			FacebookSession perm, AsyncFacebook facebookA, SocialORM sorm,
			WidgetORM worm) {
		if (mNewsFeedThread == null) {
			mNewsFeedThread = new NewsFeedThread(context, perm, facebookA,
					sorm, worm);
		}
		return mNewsFeedThread;
	}

	public void update(boolean isFirst) {
		if (isFirst) {
			this.isFirst = isFirst;
			lasttime = 0;
			// mWidgetORM.removeAllStreams();// to be removed
			mHandler.obtainMessage(GET_WEB_NEWSFEED).sendToTarget();
		} else {
			if (mHandler.hasMessages(GET_WEB_NEWSFEED) || isProcessing) {
				Log.d(TAG, "newsfeed request is ignored.");
				return;
			}
			this.isFirst = isFirst;
			mHandler.obtainMessage(GET_DB_NEWSFEED).sendToTarget();
			Message msg = mHandler.obtainMessage(GET_WEB_NEWSFEED);
			mHandler.sendMessageDelayed(msg, WAIT_TO_GET_FROM_WEB * 1000);
		}
	}

	private class NewsFeedHandler extends Handler {
		public NewsFeedHandler(Looper l) {
			super(l);
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_DB_NEWSFEED: {
				streams = mWidgetORM.getStreams();
				if (streams != null && streams.size() != 0) {
					composeStream(streams);
					Intent data = new Intent(ACTION_NEWSFEED);
					data.putExtra(FLAG, NEWSFEED_CALLBACK_RESULT);
					data.putParcelableArrayListExtra(DATA_STREAM, streams);
					mContext.sendBroadcast(data);
					if (DEBUG)
						Log.d(TAG, "GET_DB_NEWSFEED with result");
				} else {
					if (DEBUG)
						Log.d(TAG, "GET_DB_NEWSFEED no result");
				}
				break;
			}
			case GET_WEB_NEWSFEED: {
				if (DEBUG)
					Log.d(TAG, "GET_WEB_NEWSFEED" + " lasttime is " + lasttime);
				getNewsFeed();
				break;
			}
			case STORE_NEWSFEED: {
				if (streams != null && streams.size() != 0) {
					mWidgetORM.addStreams(streams, limitation);
				}
				break;
			}
			case CALLBACK_NEWSFEED: {
				if (DEBUG)
					Log.d(TAG, "CALLBACK_NEWSFEED");
				Intent notify = new Intent(ACTION_NEWSFEED);
				notify.putExtra(FLAG, NEWSFEED_CALLBACK_USERINFO);
				notify.putExtra(USER_ID, msg.getData().getLong(USER_ID));
				notify.putExtra(USER_NAME, msg.getData().getString(USER_NAME));
				notify.putExtra(USER_HEAD, msg.getData().getString(USER_HEAD));
				mContext.sendBroadcast(notify);
				break;
			}
			default:
				break;
			}
		}

		private void getNewsFeed() {
			if (DEBUG)
				Log.d(TAG, "lasttime " + lasttime / 1000);
			synchronized (mLock) {
				isProcessing = true;
			}
			facebookA.getStreamAsync(lasttime / 1000, limitation,true,"",
					new FacebookAdapter() {
						@Override
						public void getStream(List<Stream> sts) {
							synchronized (mLock) {
								isProcessing = false;
							}
							if (sts != null && sts.size() != 0) {
								lasttime = sts.get(0).updated_time + 1000;
								if (DEBUG)
									Log.d(TAG, "the size of streams is "
											+ sts.size());
								streams = new ArrayList<LiteStream>();
								LiteStream liteStream;
								for (Stream stream : sts) {
									liteStream = new LiteStream();
									liteStream.post_id = stream.post_id;
									liteStream.source_id = stream.source_id;
									liteStream.message = stream.message;
									liteStream.updated_time = stream.updated_time;
									liteStream.created_time = stream.created_time;
									streams.add(liteStream);
								}
								if (isFirst) {
									composeStream(streams);
									Intent data = new Intent(ACTION_NEWSFEED);
									data.putExtra(FLAG,
											NEWSFEED_CALLBACK_RESULT);
									data.putParcelableArrayListExtra(
											DATA_STREAM, streams);
									mContext.sendBroadcast(data);

									Message msg = mHandler
											.obtainMessage(STORE_NEWSFEED);
									mHandler.sendMessageDelayed(msg, 5000);
								} else {
									mWidgetORM.addStreams(streams, limitation);
								}
							}
						}

						@Override
						public void onException(FacebookException e, int method) {
							synchronized (mLock) {
								isProcessing = false;
							}
							if (DEBUG)
								Log.d(TAG, "after get stream ex = "
										+ e.getMessage());
						}
					});
		}

		private void composeStream(List<LiteStream> dbStreams) {
			Set<Long> ids = new HashSet<Long>();
			boolean getFromWeb = false;
			for (LiteStream dBStream : dbStreams) {
				FacebookUser user = mSocialORM
						.getFacebookUser(dBStream.source_id);
				if (user == null) {
					if (DEBUG)
						Log.d(TAG, "user == null");
					getFromWeb = true;
					ids.add(dBStream.source_id);
				} else {
					String name = user.name;
					if (name == null || name.equals("")) {
						getFromWeb = true;
						ids.add(dBStream.source_id);
					}
				}
				if (!getFromWeb) {// store directly
					dBStream.user_name = user.name;
					dBStream.user_head = user.pic_square;
				}
			}
			if (getFromWeb) {
				for (long id : ids) {
					AsyncFacebook af = facebookA;
					if (af != null) {
						long[] uids = new long[1];
						uids[0] = id;
						af.getBasicUsersAsync(uids, new FacebookAdapter() {
							@Override
							public void getUsers(List<FacebookUser> users) {
								if (users != null && users.size() > 0) {
									FacebookUser user = users.get(0);
									mSocialORM.addFacebookUser(user);
									Message msg = mHandler
											.obtainMessage(CALLBACK_NEWSFEED);
									Bundle b = new Bundle();
									b.putLong(USER_ID, user.uid);
									b.putString(USER_NAME, user.name);
									b.putString(USER_HEAD, user.pic_square);
									msg.setData(b);
									msg.sendToTarget();
								}
							}

							@Override
							public void onException(FacebookException e,
									int method) {
								if (DEBUG)
									Log.d(TAG, "fail to get the image "
											+ e.getMessage());
							}
						});
					}
				}
			}
		}
	}
}
