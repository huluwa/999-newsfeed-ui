package com.msocial.facebook.widget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.service.FacebookLoginHelper;
import com.msocial.facebook.widget.DBStream;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.Stream;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class NewsFeedService extends Service {
	final String TAG = "NewsFeedService";
	final boolean DEBUG = true;

	final int GET_DB_NEWSFEED = 0;
	final int GET_WEB_NEWSFEED = GET_DB_NEWSFEED + 1;
	final int STORE_NEWSFEED = GET_WEB_NEWSFEED + 1;
	final int CALLBACK_NEWSFEED = STORE_NEWSFEED + 1;
	final int UPDATE_STATUS = CALLBACK_NEWSFEED + 1;
	final int CALLBACK_STATUS = UPDATE_STATUS + 1;

	final String ACTION_NOFITY = "com.borqs.facebook.InflatedLayout";
	private int WAIT_TO_GET_FROM_WEB = 5;

	private long lasttime = 0;
	public int limitation = 10;

	final String FLAG = "flag";
	final int DEFAULT = 0;
	final int NETWORK_UNAVAILABLE = 100;
	final int NEWSFEED_PERIOD = 1;
	final int NEWSFEED_INSTANT = 2;
	final int NEWSFEED_CALLBACK_RESULT = 11;
	final int NEWSFEED_CALLBACK_USERINFO = 12;
	final int STATUS_UPDATE = 21;
	final int STATUS_CALLBACK_RESULT = 31;

	final String STATUS_VALUE = "status_value";
	final String STATUS_SUCCESS = "status_success";

	final String USER_ID = "user_id";
	final String USER_NAME = "user_name";
	final String USER_HEAD = "user_head";

	final String DATA_DBSTREAMS = "DBStreams";

	NewsFeedHandler mHandler;
	WidgetORM mWidgetORM;
	SocialORM mSocialORM;
	AsyncFacebook facebookA;
	FacebookSession perm_session;
	boolean is_db_changed = true;
	boolean is_first = true;
	boolean is_processing = false;

	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		mHandler = new NewsFeedHandler();
		mWidgetORM = new WidgetORM(this);
		mSocialORM = new SocialORM(this);
	}

	public void onStart(Intent intent, int startId) {
		if (DEBUG)
			Log.d(TAG, "onStart");
		FacebookLoginHelper loginHelper = FacebookLoginHelper.instance(this);
		perm_session = loginHelper.constructPermSession();
		if (perm_session != null) {
			facebookA = new AsyncFacebook(perm_session);
		} else {
			if (DEBUG)
				Log.d(TAG, "perm_session is null");
			Intent back = new Intent(ACTION_NOFITY);
			back.putExtra(FLAG, NETWORK_UNAVAILABLE);
			sendBroadcast(back);
		}

		int flag = intent.getIntExtra(FLAG, DEFAULT);
		switch (flag) {
		case NEWSFEED_INSTANT: {
			is_first = true;
			is_processing = true;
			lasttime = 0;
			mWidgetORM.removeAllStreams();// to be removed
			mHandler.obtainMessage(GET_WEB_NEWSFEED).sendToTarget();
			break;
		}
		case NEWSFEED_PERIOD: {
			is_first = false;
			if (is_processing) {
				if (DEBUG)
					Log.d(TAG, "your request is ignored.");
				return;
			}
			is_processing = true;
			mHandler.obtainMessage(GET_DB_NEWSFEED).sendToTarget();
			Message msg = mHandler.obtainMessage(GET_WEB_NEWSFEED);
			mHandler.sendMessageDelayed(msg, WAIT_TO_GET_FROM_WEB * 1000);
			break;
		}
		case STATUS_UPDATE: {
			if (DEBUG)
				Log
						.d(TAG, "TYPE_STATUS "
								+ intent.getStringExtra(STATUS_VALUE));
			Message msg = mHandler.obtainMessage(UPDATE_STATUS);
			Bundle bundle = new Bundle();
			bundle.putString(STATUS_VALUE, intent.getStringExtra(STATUS_VALUE));
			msg.setData(bundle);
			mHandler.sendMessageAtFrontOfQueue(msg);
			break;
		}
		default:
		}
	}

	private class NewsFeedHandler extends Handler {
		public NewsFeedHandler() {
			super();
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_DB_NEWSFEED: {
				ArrayList<DBStream> dbStreams = mWidgetORM.getStreams();
				if (dbStreams != null && dbStreams.size() != 0) {
					composeStream(dbStreams);
					Intent data = new Intent(ACTION_NOFITY);
					data.putExtra(FLAG, NEWSFEED_CALLBACK_RESULT);
					data.putParcelableArrayListExtra(DATA_DBSTREAMS, dbStreams);
					sendBroadcast(data);
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
				getNewsFeed(is_first);
				break;
			}
			case STORE_NEWSFEED: {
				ArrayList<DBStream> streams = msg.getData()
						.getParcelableArrayList(DATA_DBSTREAMS);
				if (streams != null && streams.size() != 0) {
					mWidgetORM.addStreams(streams, limitation);
				}
				break;
			}
			case CALLBACK_NEWSFEED: {
				if (DEBUG)
					Log.d(TAG, "CALLBACK_NEWSFEED");
				Intent notify = new Intent(ACTION_NOFITY);
				notify.putExtra(FLAG, NEWSFEED_CALLBACK_USERINFO);
				notify.putExtra(USER_ID, msg.getData().getLong(USER_ID));
				notify.putExtra(USER_NAME, msg.getData().getString(USER_NAME));
				notify.putExtra(USER_HEAD, msg.getData().getString(USER_HEAD));
				sendBroadcast(notify);
				break;
			}
			case UPDATE_STATUS: {
				if (DEBUG)
					Log.d(TAG, "UPDATE_STATUS");
				String status = msg.getData().getString(STATUS_VALUE);
				updateStatus(status);
				break;
			}
			case CALLBACK_STATUS: {
				if (DEBUG)
					Log.d(TAG, "CALLBACK_STATUS");
				Intent notify = new Intent(ACTION_NOFITY);
				notify.putExtra(FLAG, STATUS_CALLBACK_RESULT);
				notify.putExtra(STATUS_SUCCESS, msg.getData().getBoolean(
						STATUS_SUCCESS));
				sendBroadcast(notify);
				break;
			}
			default:
				break;
			}
		}

		private void getNewsFeed(final boolean is_needed_callback) {
			if (DEBUG)
				Log.d(TAG, "getNewsFeed");
			if (facebookA == null)
				return;
			facebookA.getStreamAsync(lasttime / 1000, limitation,true,"",
					new FacebookAdapter() {
						@Override
						public void getStream(List<Stream> sts) {
							is_processing = false;
							if (sts != null && sts.size() != 0) {
								lasttime = sts.get(0).updated_time + 1000;
								if (DEBUG)
									Log.d(TAG, "the size of streams is "
											+ sts.size());
								ArrayList<DBStream> streams = new ArrayList<DBStream>();
								DBStream dbStream;
								for (Stream stream : sts) {
									dbStream = new DBStream();
									dbStream.post_id = stream.post_id;
									dbStream.source_id = stream.source_id;
									dbStream.message = stream.message;
									dbStream.updated_time = stream.updated_time;
									dbStream.created_time = stream.created_time;
									streams.add(dbStream);
								}
								if (is_needed_callback) {
									composeStream(streams);
									Intent data = new Intent(ACTION_NOFITY);
									data.putExtra(FLAG,
											NEWSFEED_CALLBACK_RESULT);
									data.putParcelableArrayListExtra(
											"DBStreams", streams);
									sendBroadcast(data);

									Message msg = mHandler
											.obtainMessage(STORE_NEWSFEED);
									msg.getData().putParcelableArrayList(
											DATA_DBSTREAMS, streams);
									mHandler.sendMessageDelayed(msg, 5000);
								} else {
									mWidgetORM.addStreams(streams, limitation);
								}
							}
						}

						@Override
						public void onException(FacebookException e, int method) {
							is_processing = false;
							if (DEBUG)
								Log.d(TAG, "after get stream ex = "
										+ e.getMessage());
						}
					});
		}

		private void composeStream(List<DBStream> dbStreams) {
			Set<Long> ids = new HashSet<Long>();
			boolean getFromWeb = false;
			for (DBStream dBStream : dbStreams) {
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

	private void updateStatus(String status) {
		if (DEBUG)
			Log.d(TAG, "update status " + status);
		if (facebookA != null) {
			if (status != null && status.length() > 0) {
				facebookA.updateStatusAsync(status, new FacebookAdapter() {
					@Override
					public void updateStatus(boolean suc) {
						if (DEBUG)
							Log.d(TAG, "update status is " + suc);
						sendMessage(suc);
					}

					@Override
					public void onException(FacebookException e, int method) {
						if (DEBUG)
							Log.d(TAG, "update status ex=" + e.getMessage());
						sendMessage(false);
					}

					public void sendMessage(boolean suc) {
						Message msg = mHandler.obtainMessage(CALLBACK_STATUS);
						Bundle b = new Bundle();
						b.putBoolean(STATUS_SUCCESS, suc);
						msg.setData(b);
						msg.sendToTarget();
					}
				});
			}
		}
	}
}
