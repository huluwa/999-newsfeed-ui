package com.msocial.freefb.widget.thread;

import java.util.ArrayList;
import java.util.List;

import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.widget.model.LiteStatus;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.UserStatus;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import static com.msocial.freefb.widget.StaticFlag.*;

public class StatusTravelThread{
	static final String TAG = "StatusTravelThread";
	final boolean DEBUG = true;

	final int GET_WEB_STATUS = 1;
	final int CALLBACK_STATUS = 2;

	final int delay = 1;

	AsyncFacebook facebookA;
	Context mContext;
	StatusHandler mHandler;
	SocialORM mSocialORM;
	FacebookSession perm;
	boolean isFirst;
	HandlerThread mHandlerThread;
	public boolean isProcessing = false;
	private Object mLock = new Object();
	private static StatusTravelThread mStatusThread = null;
	ArrayList<LiteStatus> list;

	private StatusTravelThread(Context context, FacebookSession perm,
			AsyncFacebook facebookA, SocialORM orm) {
		this.mContext = context;
		this.facebookA = facebookA;
		this.mSocialORM = orm;
		this.perm = perm;
		mHandlerThread = new HandlerThread(TAG);
		mHandlerThread.start();
		mHandler = new StatusHandler(mHandlerThread.getLooper());
	}

	public static StatusTravelThread getInstance(Context context,
			FacebookSession perm, AsyncFacebook facebookA, SocialORM orm) {
		if (mStatusThread == null) {
			mStatusThread = new StatusTravelThread(context, perm, facebookA,
					orm);
		}
		return mStatusThread;
	}

	public void update() {
		if (mHandler.hasMessages(GET_WEB_STATUS) || isProcessing) {
			Log.d(TAG, "your request is ignored");
			return;
		}
		mHandler.obtainMessage(GET_WEB_STATUS).sendToTarget();
	}

	private void getStatus() {
		synchronized (mLock) {
			isProcessing = true;
		}
		facebookA.getFriendsStatusAsync(1, 1000, new FacebookAdapter() {
			@Override
			public void getFriendsStatus(List<UserStatus> frds) {
				synchronized (mLock) {
					isProcessing = false;
				}
				if (DEBUG)
					Log.d(TAG, "userstatus size is " + frds.size());
				if (frds != null && frds.size() != 0) {
					list = new ArrayList<LiteStatus>();
					for (UserStatus status : frds) {
						LiteStatus lite = new LiteStatus();
						lite.uid = status.uid;
						lite.time = status.time.getTime();
						lite.message = status.message;
						lite.username = status.username;
						list.add(lite);
					}
					mHandler.obtainMessage(CALLBACK_STATUS).sendToTarget();
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

	private class StatusHandler extends Handler {
		public StatusHandler(Looper l) {
			super(l);
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_WEB_STATUS: {
				getStatus();
				break;
			}
			case CALLBACK_STATUS: {
				Intent result = new Intent(ACTION_STATUS);
				result.putExtra(FLAG, STATUS_NORMAL_CALLBACK_RESULT);
				result.putExtra(DATA_STATUS, list);
				mContext.sendBroadcast(result);
				break;
			}
			default:
			}
		}
	}

}
