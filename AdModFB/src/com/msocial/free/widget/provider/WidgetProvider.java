package com.msocial.free.widget.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class WidgetProvider extends ContentProvider {
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.msocial.free.widget.WidgetProvider");
	private static final String sDatabaseName = "widget.db";
	private static final String Authorities = "com.msocial.free.widget.WidgetProvider";
	private SQLiteOpenHelper dbHelper;
	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	private static final int DATABASE_VERSION = 2;
	private static final int stream_id = 1;
	static {
		sURIMatcher.addURI(Authorities, "stream", stream_id);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private Context mContext;

		public DatabaseHelper(Context context) {
			super(context, sDatabaseName, null, DATABASE_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL("CREATE TABLE stream ("
					+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "post_id  TEXT," + "source_id INTEGER,"
					+ "message  TEXT," + "updated_time  INTEGER,"
					+ "created_time INTEGER" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		int count = 0;
		SQLiteDatabase mDB = dbHelper.getWritableDatabase();

		int match = sURIMatcher.match(uri);
		if (match == stream_id) {
			count = mDB.delete("stream", selection, selectionArgs);

			// Notify any listeners and return the URI of the new row.
			getContext().getContentResolver().notifyChange(
					Uri.parse(CONTENT_URI + "/stream"), null);
		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		Uri ret = null;
		SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		long rowID = -1;
		int match = sURIMatcher.match(uri);
		if (match == stream_id) {
			rowID = mDB.insert("stream", null, values);
			if (rowID != -1) {
				ret = Uri.parse(CONTENT_URI + "/stream/" + rowID);
			}
		}
		return ret;
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return (dbHelper == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;
		// Log.w(TAG,"query query="+queryString);

		SQLiteDatabase mDB = dbHelper.getReadableDatabase();
		SQLiteQueryBuilder q = new SQLiteQueryBuilder();
		int match = sURIMatcher.match(uri);
		if (match == stream_id) {
			q.setTables("stream");
			cursor = q.query(mDB, projection, selection, selectionArgs, null,
					null, sortOrder);
		}
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
