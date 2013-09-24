package oms.sns.facebook.widget;

import oms.sns.facebook.providers.SocialORM;
import oms.sns.facebook.service.FacebookLoginHelper;
import oms.sns.facebook.widget.provider.WidgetORM;
import oms.sns.facebook.widget.thread.FriendThread;
import oms.sns.facebook.widget.thread.NewsFeedThread;
import oms.sns.facebook.widget.thread.PhoneBookThread;
import oms.sns.facebook.widget.thread.StatusSingleThread;
import oms.sns.facebook.widget.thread.StatusTravelThread;
import oms.sns.facebook.widget.thread.StatusUpdateThread;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookSession;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import static oms.sns.facebook.widget.StaticFlag.*;

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
