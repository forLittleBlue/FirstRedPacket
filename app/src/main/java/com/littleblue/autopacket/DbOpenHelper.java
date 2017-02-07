package com.littleblue.autopacket;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 10964492 on 2017/2/7.
 */

public class DbOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "RedPacket.DbOpenHelper";

    private static final String DB_NAME = "redepacket_history.db";
    public static final String HISTORY_TABLE_NAME = "history";
    private static final int DB_VERSION = 1;
    private String CREAT_HIRTORY_TABLE = "CREAT TABLE IF NOT EXISTS" + HISTORY_TABLE_NAME + "(time LONG PRIMARY KEY," + "who TEXT," + "money FLOAT)";

    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
