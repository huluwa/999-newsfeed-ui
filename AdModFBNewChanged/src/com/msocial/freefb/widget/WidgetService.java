package com.msocial.freefb.widget;

import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.service.FacebookLoginHelper;
import com.msocial.freefb.widget.provider.WidgetORM;
import com.msocial.freefb.widget.thread.FriendThread;
import com.msocial.freefb.widget.thread.NewsFeedThread;
import com.msocial.freefb.widget.thread.PhoneBookThread;
import com.msocial.freefb.widget.thread.StatusSingleThread;
import com.msocial.freefb.widget.thread.StatusTravelThread;
import com.msocial.freefb.widget.thread.StatusUpdateThread;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookSession;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import static com.msocial.freefb.widget.StaticFlag.*;

public class WidgetService extends Service {
	final String TAG = "WidgetService";
	final boolean DEBUG = true;

	WidgetORM mWidgetORM;
	SocialORM mSocialORM;
	AsyncFacebook facebookA;
	FacebookSession perm_session;

	PhoneBookThread mPhonebookThread;
	FriendThread mFriendThread;
	StatusTravelThread mStatusTravelThread;

	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		mWidgetORM = new WidgetORM(this);
		mSocialORM = new SocialORM(this);
	}

	public void onStart(Intent intent, int startId) {
		if (DEBUG)
			Log.d(TAG, "onStart");
		FacebookLoginHelper loginHelper = FacebookLoginHelper.instance(this);
		perm_session = loginHelper.constructPermSession();
		boolean isOnline = true;
		if (perm_session != null) {
			facebookA = new AsyncFacebook(perm_session);
			if (facebookA == null) {
				isOnline = false;
			}
		} else {
			isOnline = false;
		}

		if (!isOnline) {
			if (DEBUG)
				Log.d(TAG, "be not online");
			Intent back = new Intent(ACTION_NEWSFEED);
			back.putExtra(FLAG, NETWORK_UNAVAILABLE);
			sendBroadcast(back);
			return;
		}

		int flag = intent.getIntExtra(FLAG, DEFAULT);
		switch (flag) {
		case NEWSFEED_INSTANT: {
			NewsFeedThread.getInstance(this, perm_session, facebookA,
					mSocialORM, mWidgetORM).update(true);
			break;
		}
		case NEWSFEED_PERIOD: {
			NewsFeedThread.getInstance(this, perm_session, facebookA,
					mSocialORM, mWidgetORM).update(false);
			break;
		}
		case PHONEBOOK_NORMAL: {
			if (mPhonebookThread == null) {
				mPhonebookThread = PhoneBookThread.getInstance(this,
						perm_session, facebookA, mSocialORM);
				mPhonebookThread.update(true);
			} else {
				mPhonebookThread.update(false);
			}
			break;
		}
		case FRIEND_NORMAL: {
			if (mFriendThread == null) {
				mFriendThread = FriendThread.getInstance(this, perm_session,
						facebookA, mSocialORM);
				mFriendThread.update(true);
			} else {
				mFriendThread.update(false);
			}
			break;
		}
		case STATUS_UPDATE: {
			StatusUpdateThread.getInstance(this, facebookA).update(
					intent.getStringExtra(STATUS_VALUE));
			break;
		}
		case STATUS_NORMAL: {
			StatusTravelThread.getInstance(this, perm_session, facebookA,
					mSocialORM).update();
			break;
		}
		case STATUS_SINGLE_NORMAL: {
			long uid = intent.getLongExtra(USER_ID, -1);
			if (uid != -1) {
				StatusSingleThread.getInstance(this, perm_session, facebookA,
						mSocialORM).update(uid);
			}
			break;
		}
		default:
		}
	}
}
