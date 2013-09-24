package com.msocial.nofree.widget.thread;

import java.util.List;
import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.ui.view.ImageRun;
import com.msocial.nofree.widget.model.LiteStatus;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.UserStatus;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import static com.msocial.nofree.widget.StaticFlag.*;

public class StatusSingleThread {
	static final String TAG = "StatusSingleThread";
	final boolean DEBUG = true;

	final int GET_WEB_STATUS = 1;
	final int CALLBACK_STATUS = 2;

	final int delay = 1;

	AsyncFacebook facebookA;
	Context mContext;
	StatusSingleHandler mHandler;
	SocialORM mSocialORM;
	FacebookSession perm;
	boolean isFirst;
	HandlerThread mHandlerThread;
	public boolean isProcessing = false;
	private Object mLock = new Object();
	private static StatusSingleThread mStatusThread = null;
	LiteStatus lite;

	private StatusSingleThread(Context context, FacebookSession perm,
			AsyncFacebook facebookA, SocialORM orm) {
		this.mContext = context;
		this.facebookA = facebookA;
		this.mSocialORM = orm;
		this.perm = perm;
		mHandlerThread = new HandlerThread(TAG);
		mHandlerThread.start();
		mHandler = new StatusSingleHandler(mHandlerThread.getLooper());
		lite = new LiteStatus();
	}

	public static StatusSingleThread getInstance(Context context,
			FacebookSession perm, AsyncFacebook facebookA, SocialORM orm) {
		if (mStatusThread == null) {
			mStatusThread = new StatusSingleThread(context, perm, facebookA,
					orm);
		}
		return mStatusThread;
	}

	public void update(long uid) {
		if (mHandler.hasMessages(GET_WEB_STATUS) || isProcessing) {
			Log.d(TAG, "your request is ignored");
			return;
		}
		Message msg = mHandler.obtainMessage(GET_WEB_STATUS);
		msg.getData().putLong(USER_ID, uid);
		msg.sendToTarget();
	}

	private void getUserStatus(long uid) {
		synchronized (mLock) {
			isProcessing = true;
		}
		long[] uids = new long[1];
		uids[0] = uid;
		facebookA.getBasicUsersAsync(uids, new FacebookAdapter() {
			@Override
			public void getUsers(List<FacebookUser> users) {
				synchronized (mLock) {
					isProcessing = false;
				}
				if (DEBUG)
					Log.d(TAG, "users size is " + users.size());
				if (users != null && users.size() != 0) {
					FacebookUser user = users.get(0);
					lite.uid = user.uid;
					lite.time = user.statustime;
					lite.message = user.message;
					lite.username = user.name;
					if (user.message != null && user.statustime != 0) {
						mHandler.obtainMessage(CALLBACK_STATUS).sendToTarget();
					}
					mSocialORM.addFacebookUser(user);
				}
			}

			@Override
			public void onException(FacebookException e, int method) {
				synchronized (mLock) {
					isProcessing = false;
				}
			}
		});

	}

	private class StatusSingleHandler extends Handler {
		public StatusSingleHandler(Looper l) {
			super(l);
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_WEB_STATUS: {
				getUserStatus(msg.getData().getLong(USER_ID));
				break;
			}
			case CALLBACK_STATUS: {
				Intent result = new Intent(ACTION_STATUS_SINGLE);
				result.putExtra(FLAG, STATUS_SINGLE_CALLBACK_RESULT);
				result.putExtra(DATA_STATUS, lite);
				mContext.sendBroadcast(result);
				break;
			}
			default:
			}
		}
	}

}
