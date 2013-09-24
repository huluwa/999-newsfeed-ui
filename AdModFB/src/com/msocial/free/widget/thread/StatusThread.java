package com.msocial.free.widget.thread;

import java.util.ArrayList;

import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class StatusThread implements Runnable {
	final String TAG = "StatusThread";
	final boolean DEBUG = true;

	final String ACTION_NOFITY = "com.borqs.facebook.widget.NewsfeedWidget";

	final int UPDATE_STATUS = 1;
	final int CALLBACK_STATUS = 2;

	final String FLAG = "flag";
	final int STATUS_UPDATE = 21;
	final int STATUS_CALLBACK_RESULT = 31;
	final String STATUS_VALUE = "status_value";
	final String STATUS_SUCCESS = "status_success";

	String status;
	AsyncFacebook facebookA;
	Context context;
	StatusHandler mHandler;

	public StatusThread(Context context, AsyncFacebook facebookA, String status) {
		Log.d(TAG, "using status thread");
		this.context = context;
		this.status = status;
		this.facebookA = facebookA;
		mHandler = new StatusHandler();
	}

	public void run() {
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
		public StatusHandler() {
			super();
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
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
				context.sendBroadcast(notify);
				break;
			}
			default:
				break;
			}
		}
	}
}
