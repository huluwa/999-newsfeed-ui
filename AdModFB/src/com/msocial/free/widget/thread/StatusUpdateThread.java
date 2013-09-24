package com.msocial.free.widget.thread;

import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import static com.msocial.free.widget.StaticFlag.*;

public class StatusUpdateThread {
	static final String TAG = "StatusUpdateThread";
	final boolean DEBUG = true;

	final int UPDATE_STATUS = 1;
	final int CALLBACK_STATUS = 2;

	String status;
	AsyncFacebook facebookA;
	Context context;
	StatusHandler mHandler;
	HandlerThread mHandlerThread;
	static StatusUpdateThread mStatusUpdateThread = null;

	public static StatusUpdateThread getInstance(Context context,
			AsyncFacebook facebookA) {
		Log.d(TAG, "using status thread");
		if (mStatusUpdateThread == null) {
			mStatusUpdateThread = new StatusUpdateThread(context, facebookA);
		}
		return mStatusUpdateThread;
	}

	private StatusUpdateThread(Context context, AsyncFacebook facebookA) {
		Log.d(TAG, "using status thread");
		this.context = context;
		this.facebookA = facebookA;
		mHandlerThread = new HandlerThread(TAG);
		mHandlerThread.start();
		mHandler = new StatusHandler(mHandlerThread.getLooper());
	}

	public void update(String status) {
		this.status = status;
		Message msg = mHandler.obtainMessage(UPDATE_STATUS);
		msg.getData().putString(STATUS_VALUE, status);
		msg.sendToTarget();
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

	private class StatusHandler extends Handler {
		public StatusHandler(Looper l) {
			super(l);
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_STATUS: {
				String status = msg.getData().getString(STATUS_VALUE);
				updateStatus(status);
				break;
			}
			case CALLBACK_STATUS: {
				Intent notify = new Intent(ACTION_NEWSFEED);
				notify.putExtra(FLAG, STATUS_CALLBACK_RESULT);
				notify.putExtra(STATUS_SUCCESS, msg.getData().getBoolean(
						STATUS_SUCCESS));
				context.sendBroadcast(notify);
				break;
			}
			default:
				break;
			}
		}
	}
}
