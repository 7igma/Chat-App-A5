package edu.stevens.cs522.chat.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import edu.stevens.cs522.chat.contracts.BaseContract;
import edu.stevens.cs522.chat.contracts.MessageContract;
import edu.stevens.cs522.chat.contracts.PeerContract;

public class ChatProvider extends ContentProvider {

    public ChatProvider() {
    }

    private static final String AUTHORITY = BaseContract.AUTHORITY;

    private static final String MESSAGE_CONTENT_PATH = MessageContract.CONTENT_PATH;

    private static final String MESSAGE_CONTENT_PATH_ITEM = MessageContract.CONTENT_PATH_ITEM;

    private static final String PEER_CONTENT_PATH = PeerContract.CONTENT_PATH;

    private static final String PEER_CONTENT_PATH_ITEM = PeerContract.CONTENT_PATH_ITEM;


    private static final String DATABASE_NAME = "chat2.db";

    private static final int DATABASE_VERSION = 1;

    private static final String MESSAGES_TABLE = "messages";

    private static final String PEERS_TABLE = "view_peers";

    // Create the constants used to differentiate between the different URI  requests.
    private static final int MESSAGES_ALL_ROWS = 1;
    private static final int MESSAGES_SINGLE_ROW = 2;
    private static final int PEERS_ALL_ROWS = 3;
    private static final int PEERS_SINGLE_ROW = 4;

    public static class DbHelper extends SQLiteOpenHelper {

        private static final String DATABASE_CREATE =
                "create table "+MESSAGES_TABLE+" ("+MessageContract.ID+" integer primary key autoincrement, "+MessageContract.MESSAGE_TEXT+" text, "+MessageContract.TIMESTAMP+" text, "+MessageContract.SENDER+" text, "+MessageContract.SENDER_ID+" text);";

        private static final String DATABASE_CREATE2 =
                "create table "+PEERS_TABLE+" ("+PeerContract.ID+" integer primary key autoincrement, "+PeerContract.NAME+" text, "+PeerContract.TIMESTAMP+" text, "+PeerContract.ADDRESS+" text, "+PeerContract.PORT+" text);";


        public DbHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO initialize database tables
            db.execSQL(DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE2);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO upgrade database if necessary
            db.execSQL("DROP TABLE IF EXISTS "+MESSAGES_TABLE);
            db.execSQL("DROP TABLE IF EXISTS "+PEERS_TABLE);
            onCreate(db);
        }
    }

    private DbHelper dbHelper;

    @Override
    public boolean onCreate() {
        // Initialize your content provider on startup.
        dbHelper = new DbHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return true;
    }

    // Used to dispatch operation based on URI
    private static final UriMatcher uriMatcher;

    // uriMatcher.addURI(AUTHORITY, CONTENT_PATH, OPCODE)
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MESSAGE_CONTENT_PATH, MESSAGES_ALL_ROWS);
        uriMatcher.addURI(AUTHORITY, MESSAGE_CONTENT_PATH_ITEM, MESSAGES_SINGLE_ROW);
        uriMatcher.addURI(AUTHORITY, PEER_CONTENT_PATH, PEERS_ALL_ROWS);
        uriMatcher.addURI(AUTHORITY, PEER_CONTENT_PATH_ITEM, PEERS_SINGLE_ROW);
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALL_ROWS:
                return MessageContract.contentType("book");
            case MESSAGES_SINGLE_ROW:
                return MessageContract.contentItemType("book");
            case PEERS_ALL_ROWS:
                return PeerContract.contentType("peer");
            case PEERS_SINGLE_ROW:
                return PeerContract.contentItemType("peer");
            default:
                throw new IllegalArgumentException("Unsupported URI: "+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALL_ROWS:
                // TODO: Implement this to handle requests to insert a new row.
                Log.i("ChatProvider", "insert: inserting values");
                id = db.insert(MESSAGES_TABLE, null, values);
                if (id > 0)
                {
                    Uri insertid = ContentUris.withAppendedId(MessageContract.CONTENT_URI, id);
                    getContext().getContentResolver().notifyChange(insertid, null);
                    return insertid;
                }
                else
                {
                    return null;
                }
            case MESSAGES_SINGLE_ROW:
                throw new IllegalArgumentException("insert expects a whole-table URI");
            case PEERS_ALL_ROWS:
                Log.i("ChatProvider", "insert: inserting values");
                String Query = "Select * from " + PEERS_TABLE + " where " + PeerContract.NAME + " = '" + values.get(PeerContract.NAME) + "'";
                Cursor cursor = db.rawQuery(Query, null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    long pid = cursor.getLong(cursor.getColumnIndex("_id"));
                    id = db.update(PEERS_TABLE, values, "_id=" + pid, null);
                    Uri updateid = ContentUris.withAppendedId(PeerContract.CONTENT_URI, id);
                    getContext().getContentResolver().notifyChange(updateid, null);
                    return updateid;
                }
                else
                {
                    //Log.i("ChatProvider", "insert: inserting values");
                    id = db.insert(PEERS_TABLE, null, values);
                    if (id > 0)
                    {
                        Uri insertid = ContentUris.withAppendedId(PeerContract.CONTENT_URI, id);
                        getContext().getContentResolver().notifyChange(insertid, null);
                        return insertid;
                    }
                    else
                    {
                        return null;
                    }
                }
            case PEERS_SINGLE_ROW:
                throw new IllegalArgumentException("insert expects a whole-table URI");
            default:
                throw new IllegalStateException("insert: bad case");
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String rowId;
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALL_ROWS:
                // TODO: Implement this to handle query of all books.
                //return db.query(MESSAGES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                cursor = db.query(MESSAGES_TABLE, new String[] {MessageContract.ID, MessageContract.MESSAGE_TEXT, MessageContract.TIMESTAMP,
                        MessageContract.SENDER, MessageContract.SENDER_ID}, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                if (cursor != null)
                {
                    cursor.moveToFirst();
                }
                return cursor;


            case MESSAGES_SINGLE_ROW:
                // TODO: Implement this to handle query of a specific book.
                rowId = uri.getLastPathSegment();
                cursor = db.query(MESSAGES_TABLE, new String[] {MessageContract.ID, MessageContract.MESSAGE_TEXT, MessageContract.TIMESTAMP, MessageContract.SENDER, MessageContract.SENDER_ID}, selection, selectionArgs,
                        null, null, null, null);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                if (cursor != null)
                {
                    cursor.moveToFirst();
                }
                return cursor;
            case PEERS_ALL_ROWS:
                // TODO: Implement this to handle query of all books.
                //return db.query(MESSAGES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                cursor = db.query(PEERS_TABLE, new String[] {PeerContract.ID, PeerContract.NAME, PeerContract.TIMESTAMP, PeerContract.ADDRESS,
                        PeerContract.PORT}, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                if (cursor != null)
                {
                    cursor.moveToFirst();
                }
                return cursor;


            case PEERS_SINGLE_ROW:
                // TODO: Implement this to handle query of a specific book.
                rowId = uri.getLastPathSegment();
                cursor = db.query(PEERS_TABLE, new String[] {PeerContract.ID, PeerContract.NAME, PeerContract.TIMESTAMP, PeerContract.ADDRESS,
                        PeerContract.PORT}, selection, selectionArgs, null, null, null, null);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                if (cursor != null)
                {
                    cursor.moveToFirst();
                }
                return cursor;
            //return db.query(true, MESSAGES_TABLE, projection, selection, selectionArgs, null, null, sortOrder, null);
            default:
                throw new IllegalStateException("query: bad case");
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        // TODO Implement this to handle requests to delete one or more rows.
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String rowId;
        switch (uriMatcher.match(uri)) {
            case MESSAGES_ALL_ROWS:
                db.delete(MESSAGES_TABLE, where, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return 0;
            case MESSAGES_SINGLE_ROW:
                rowId = uri.getLastPathSegment();
                String[] message_args = {rowId};
                db.delete(MESSAGES_TABLE, MessageContract.ID+"=?",message_args);
                getContext().getContentResolver().notifyChange(uri, null);
                return Integer.parseInt(rowId);
            case PEERS_ALL_ROWS:
                db.delete(PEERS_TABLE, where, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return 0;
            case PEERS_SINGLE_ROW:
                rowId = uri.getLastPathSegment();
                String[] peer_args = {rowId};
                db.delete(PEERS_TABLE, MessageContract.ID+"=?",peer_args);
                getContext().getContentResolver().notifyChange(uri, null);
                return Integer.parseInt(rowId);
            default:
                throw new IllegalStateException("delete: bad case");
        }
    }

}

