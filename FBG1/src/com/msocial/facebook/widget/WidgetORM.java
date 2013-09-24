package com.msocial.facebook.widget;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class WidgetORM {
	final String TAG = "WidgetORM";
	final boolean DEBUG = false;

	private Context context;

	public WidgetORM(Context co) {
		context = co;
	}

	public static class DBStreamCol {
		public static final String ID = "_id";
		public static final String POST_ID = "post_id";
		public static final String SOURCE_ID = "source_id";
		public static final String MESSAGE = "message";
		public static final String UPDATED_TIME = "updated_time";
		public static final String CREATED_TIME = "created_time";
	}

	public static String[] DBStreamProject = new String[] { "_id", "post_id" };

	public ArrayList<DBStream> getStreams() {
		if (DEBUG)
			Log.d(TAG, "getStreams");
		ArrayList<DBStream> list = new ArrayList<DBStream>();
		Uri uri = Uri.parse(WidgetProvider.CONTENT_URI + "/stream");
		String[] projection = { "_id", "post_id", "source_id", "message",
				"updated_time", "created_time" };
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(uri, projection, null,
					null, "updated_time desc");
			if (cursor != null)
				while (cursor.moveToNext()) {
					DBStream stream = new DBStream();
					stream._id = cursor.getLong(cursor
							.getColumnIndex(DBStreamCol.ID));
					stream.post_id = cursor.getString(cursor
							.getColumnIndex(DBStreamCol.POST_ID));
					stream.source_id = cursor.getLong(cursor
							.getColumnIndex(DBStreamCol.SOURCE_ID));
					stream.message = cursor.getString(cursor
							.getColumnIndex(DBStreamCol.MESSAGE));
					stream.updated_time = cursor.getLong(cursor
							.getColumnIndex(DBStreamCol.UPDATED_TIME));
					stream.created_time = cursor.getLong(cursor
							.getColumnIndex(DBStreamCol.CREATED_TIME));
					list.add(stream);
				}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return list;
	}

	public DBStream getStreamsByPostId(String postid) {
		if (DEBUG)
			Log.d(TAG, "getStreamsByPostId" + postid);
		DBStream stream = null;
		Uri uri = Uri.parse(WidgetProvider.CONTENT_URI + "/stream");
		String[] projection = { "_id", "post_id", "source_id", "message",
				"updated_time", "created_time" };
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(uri, projection,
					"post_id = '" + postid + "'", null, "updated_time desc");
			if (cursor != null)
				while (cursor.moveToNext()) {
					stream = new DBStream();
					stream._id = cursor.getLong(cursor
							.getColumnIndex(DBStreamCol.ID));
					stream.post_id = cursor.getString(cursor
							.getColumnIndex(DBStreamCol.POST_ID));
					stream.source_id = cursor.getLong(cursor
							.getColumnIndex(DBStreamCol.SOURCE_ID));
					stream.message = cursor.getString(cursor
							.getColumnIndex(DBStreamCol.MESSAGE));
					stream.updated_time = cursor.getLong(cursor
							.getColumnIndex(DBStreamCol.UPDATED_TIME));
					stream.created_time = cursor.getLong(cursor
							.getColumnIndex(DBStreamCol.CREATED_TIME));
				}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return stream;
	}

	public boolean insertStream(DBStream stream) {
		if (DEBUG)
			Log.d(TAG, "insertStream");
		Uri ret = null;
		Uri CONTENT_URI = Uri.parse(WidgetProvider.CONTENT_URI + "/stream");
		android.content.ContentValues ct = new android.content.ContentValues();
		ct.put(DBStreamCol.POST_ID, stream.post_id);
		ct.put(DBStreamCol.SOURCE_ID, stream.source_id);
		ct.put(DBStreamCol.MESSAGE, stream.message);
		ct.put(DBStreamCol.UPDATED_TIME, stream.updated_time);
		ct.put(DBStreamCol.CREATED_TIME, stream.created_time);
		ret = context.getContentResolver().insert(CONTENT_URI, ct);
		return ret != null;
	}

	public boolean removeAllStreams() {
		if (DEBUG)
			Log.d(TAG, "removeAllStreams");
		int ret = -1;
		Uri CONTENT_URI = Uri.parse(WidgetProvider.CONTENT_URI + "/stream");
		try {
			ret = context.getContentResolver().delete(CONTENT_URI, null, null);
		} catch (SQLiteException ne) {
			ne.printStackTrace();
		}
		return ret > 0;
	}

	public boolean removeStreamById(long id) {
		if (DEBUG)
			Log.d(TAG, "removeStreamById " + id);
		int ret = -1;
		Uri CONTENT_URI = Uri.parse(WidgetProvider.CONTENT_URI + "/stream");
		try {
			ret = context.getContentResolver().delete(CONTENT_URI,
					"_id = " + id, null);
		} catch (SQLiteException ne) {
			ne.printStackTrace();
		}
		return ret > 0;
	}

	public boolean removeStreamByPostId(String postid) {
		if (DEBUG)
			Log.d(TAG, "removeStreamByPostId " + postid);
		int ret = -1;
		Uri CONTENT_URI = Uri.parse(WidgetProvider.CONTENT_URI + "/stream");
		try {
			ret = context.getContentResolver().delete(CONTENT_URI,
					"post_id = '" + postid + "'", null);
		} catch (SQLiteException ne) {
			ne.printStackTrace();
		}
		return ret > 0;
	}

	public List<Long> getStreamIds() {
		Uri CONTENT_URI = Uri.parse(WidgetProvider.CONTENT_URI + "/stream");
		Cursor cursor = null;
		ArrayList<Long> ls = new ArrayList<Long>();
		try {
			cursor = context.getContentResolver().query(CONTENT_URI,
					DBStreamProject, null, null, "updated_time asc");
			if (cursor != null) {
				while (cursor.moveToNext()) {
					ls.add(cursor
							.getLong(cursor.getColumnIndex(DBStreamCol.ID)));
				}
			}
		} catch (SQLiteException ne) {
			ne.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return ls;
	}

	public boolean addStream(DBStream stream, int streams_num) {
		DBStream dbstream = getStreamsByPostId(stream.post_id);
		if (dbstream != null) {
			if (dbstream.updated_time < stream.updated_time) {
				removeStreamByPostId(stream.post_id);
			} else {
				return false;
			}
		}
		List<Long> ids = getStreamIds();
		if (DEBUG)
			Log.d(TAG, "addStream " + ids.size());
		if (ids.size() >= streams_num) {
			for (int i = 0; i <= ids.size() - streams_num; i++)
				removeStreamById(ids.get(i));
		}
		return insertStream(stream);
	}

	public void addStreams(List<DBStream> streams, int streams_num) {
		for (DBStream stream : streams)
			addStream(stream, streams_num);
	}
}
