package com.msocial.nofree.widget.thread;

import java.util.ArrayList;
import java.util.List;

import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.widget.model.LitePhoneBook;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.client.FacebookSession;
import oms.sns.service.facebook.model.PhoneBook;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import static com.msocial.nofree.widget.StaticFlag.*;

public class PhoneBookThread{
	static final String TAG = "PhoneBookThread";
	final boolean DEBUG = true;

	final int GET_DB_PHONEBOOK = 1;
	final int GET_WEB_PHONEBOOK = 2;
	final int CALLBACK_PHONEBOOK = 3;

	final int delay = 1;

	AsyncFacebook facebookA;
	Context mContext;
	PhoneBookHandler mHandler;
	HandlerThread mHandlerThread;
	SocialORM mSocialORM;
	FacebookSession perm;
	boolean isFirst;
	public boolean isProcessing = false;
	private Object mLock = new Object();
	private static PhoneBookThread mPhoneBookThread = null;
	ArrayList<LitePhoneBook> list;

	private PhoneBookThread(Context context, FacebookSession perm,
			AsyncFacebook facebookA, SocialORM orm) {
		this.mContext = context;
		this.facebookA = facebookA;
		this.mSocialORM = orm;
		this.perm = perm;
		mHandlerThread = new HandlerThread(TAG);
		mHandlerThread.start();
		mHandler = new PhoneBookHandler(mHandlerThread.getLooper());
	}

	public static PhoneBookThread getInstance(Context context,
			FacebookSession perm, AsyncFacebook facebookA, SocialORM orm) {
		Log.d(TAG, "using friend thread");
		if (mPhoneBookThread == null) {
			mPhoneBookThread = new PhoneBookThread(context, perm, facebookA,
					orm);
		}
		return mPhoneBookThread;
	}

	public void update(boolean isFirst) {
		if (isFirst) {
			this.isFirst = isFirst;
			mHandler.obtainMessage(GET_WEB_PHONEBOOK).sendToTarget();
		} else {
			if (mHandler.hasMessages(GET_WEB_PHONEBOOK) || isProcessing) {
				Log.d(TAG, "your request is ignored");
				return;
			}
			this.isFirst = isFirst;
			mHandler.obtainMessage(GET_DB_PHONEBOOK).sendToTarget();
			Message dmsg = mHandler.obtainMessage(GET_WEB_PHONEBOOK);
			mHandler.sendMessageDelayed(dmsg, delay * 1000);
		}
	}

	private void getPhoneBook() {
		synchronized (mLock) {
			isProcessing = true;
		}
		facebookA.getPhoneBooksAsync(perm.getLogerInUserID(),
				new FacebookAdapter() {
					@Override
					public void getPhoneBooks(List<PhoneBook> frds) {
						synchronized (mLock) {
							isProcessing = false;
						}
						mHandler.post(new PhoneRunTask(frds) {
							public void run() {
								PhoneBook[] us = new PhoneBook[obj.size()];
								us = obj.toArray(us);
								new SavePhonebooks(mSocialORM).execute(us);
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

	private class PhoneBookHandler extends Handler {
		public PhoneBookHandler(Looper l) {
			super(l);
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_DB_PHONEBOOK: {
				List<PhoneBook> phones = mSocialORM.getPhonebooks();
				Log.d(TAG, "phones " + phones.size());
				if (phones != null && phones.size() != 0) {
					list = new ArrayList<LitePhoneBook>();
					for (PhoneBook phone : phones) {
						LitePhoneBook lite = new LitePhoneBook();
						lite.uid = phone.uid;
						lite.email = phone.email;
						lite.cell = phone.cell;
						lite.phone = phone.phone;
						lite.username = phone.username;
						lite.screenname = phone.screenname;
						lite.pic_square = phone.pic_square;
						list.add(lite);
					}
					mHandler.obtainMessage(CALLBACK_PHONEBOOK).sendToTarget();
				}

				break;
			}
			case GET_WEB_PHONEBOOK: {
				if (DEBUG)
					Log.d(TAG, "GET_PHONEBOOK");
				getPhoneBook();
				break;
			}
			case CALLBACK_PHONEBOOK: {
				if (DEBUG)
					Log.d(TAG, "CALLBACK_PHONEBOOK");
				Intent result = new Intent(ACTION_PHONEBOOK);
				result.putExtra(FLAG, PHONEBOOK_CALLBACK_RESULT);
				result.putExtra(DATA_PHONEBOOK, list);
				mContext.sendBroadcast(result);
				break;
			}
			default:
			}
		}
	}

	private static class PhoneRunTask implements Runnable {
		List<PhoneBook> obj;

		public PhoneRunTask(List<PhoneBook> obj) {
			super();
			this.obj = obj;
		}

		public void run() {

		}
	}

	private class SavePhonebooks extends
			android.os.AsyncTask<PhoneBook, Void, Void> {

		public SavePhonebooks(SocialORM orm) {
			super();
			this.orm = orm;

			Log.d(TAG, "create SavePhonebooks");
		}

		public SocialORM orm;

		@Override
		protected Void doInBackground(PhoneBook... pbs) {
			if (pbs != null) {
				Log.d(TAG, "exec SavePhonebooks");
				for (PhoneBook pb : pbs) {
					orm.addPhonebook(pb);
				}
			}
			if (isFirst) {
				mHandler.obtainMessage(GET_DB_PHONEBOOK).sendToTarget();
			}
			pbs = null;
			return null;
		}

	}
}
