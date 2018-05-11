package com.sdsuchatapp.chatitout;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;

/**
 * Created by priyankadeshmukh on 5/7/18.
 */

//future scope

public class DbHandler extends SQLiteOpenHelper{

    private static final int dbVersion=1;
    private static final String dbName="chats.db";
    private static final String tableName="friendsInformation";
    private static final String tableNameChats="chatsInformation";
    private static final String columnId="id";
    private static final String columnUserId="userId";
    private static final String columnProfilePicture="profilePicture";
    private static final String columnDisplayName="displayName";
    private static final String columnMessage="userId";
    private static final String columnTimeStamp="profilePicture";



    public DbHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, dbName, factory, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
//        String createTableFriends ="CREATE TABLE "+ tableName + " ("+
//                columnId+ " INTEGER PRIMARY KEY AUTOINCREMENT,"+
//                columnUserId+ " TEXT,"+
//                columnProfilePicture+" TEXT,"+
//                columnDisplayName+" TEXT;";
//        sqLiteDatabase.execSQL(createTableFriends);
        String createTableChats ="CREATE TABLE "+ tableNameChats + " ("+
                columnId+ " INTEGER PRIMARY KEY AUTOINCREMENT,"+
                columnUserId+ " TEXT,"+
                columnMessage+" TEXT,"+
                columnTimeStamp+ " DATETIME, "+
                columnProfilePicture+" TEXT,"+
                columnDisplayName+" TEXT;";
        sqLiteDatabase.execSQL(createTableChats);


    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+  tableNameChats);
        onCreate(sqLiteDatabase);
    }

    public void saveChat(){

    }

}
