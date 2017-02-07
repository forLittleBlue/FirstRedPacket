package com.littleblue.autopacket;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class HistoryProvider extends ContentProvider {
    private static final String TAG = "RedPacket.HistroyProvider";

    public static final String AUTHORITY = "com.littleblue.redpacket.history.provider";
    public static final String HISTORY_TABLE = "history";
    public static final int HISTORY_URI_CODE = 0;

    public static final Uri HISTORY_URI = Uri.parse("content://" + AUTHORITY + "/" + HISTORY_TABLE);

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, HISTORY_TABLE, HISTORY_URI_CODE);//关联表和uri code
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        Utils.logI(TAG, "delete");
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        Utils.logI(TAG, "getType");
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        Utils.logI(TAG, "insert");
        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        Utils.logI(TAG, "onCreate");
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        Utils.logI(TAG, "query");
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        Utils.logI(TAG, "update");
        return 0;
    }
}
