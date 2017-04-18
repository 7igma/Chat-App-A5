package edu.stevens.cs522.chat.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dduggan.
 */

public class MessageContract extends BaseContract {

    public static final Uri CONTENT_URI = CONTENT_URI(AUTHORITY, "Message");

    public static final Uri CONTENT_URI(long id) {
        return CONTENT_URI(Long.toString(id));
    }

    public static final Uri CONTENT_URI(String id) {
        return withExtendedPath(CONTENT_URI, id);
    }

    public static final String CONTENT_PATH = CONTENT_PATH(CONTENT_URI);

    public static final String CONTENT_PATH_ITEM = CONTENT_PATH(CONTENT_URI("#"));


    public static final String ID = _ID;

    public static final String MESSAGE_TEXT = "message_text";

    public static final String TIMESTAMP = "timestamp";

    public static final String SENDER = "sender";
    public static final String SENDER_ID = "sender_id";
    public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

    // TODO remaining columns in Messages table

    private static int messageTextColumn = -1;
    public static String getMessageText(Cursor cursor) {
        if (messageTextColumn < 0) {
            messageTextColumn = cursor.getColumnIndexOrThrow(MESSAGE_TEXT);
        }
        return cursor.getString(messageTextColumn);
    }

    public static void putMessageText(ContentValues out, String messageText) {
        out.put(MESSAGE_TEXT, messageText);
    }

    // TODO remaining getter and putter operations for other columns
    public static void putTimestamp(ContentValues values, Date timestamp)
    {
        values.put(TIMESTAMP, dateFormat.format(timestamp));
    }
    private static int timestampColumn = -1;
    public static Date getTimestamp(Cursor cursor)
    {
        if (timestampColumn < 0)
        {
            timestampColumn = cursor.getColumnIndexOrThrow(TIMESTAMP);
        }
        try {
            return dateFormat.parse(cursor.getString(timestampColumn));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void putSender(ContentValues values, String sender)
    {
        values.put(SENDER, sender);
    }
    private static int senderColumn = -1;
    public static String getSender(Cursor cursor)
    {
        if (senderColumn < 0)
        {
            senderColumn = cursor.getColumnIndexOrThrow(SENDER);
        }
        return cursor.getString(senderColumn);
    }



    public static void putSenderId(ContentValues values, Long sender_id)
    {
        values.put(SENDER_ID, Long.toString(sender_id));
    }
    private static int senderIdColumn = -1;
    public static Long getSenderId(Cursor cursor)
    {
        if (senderIdColumn < 0)
        {
            senderIdColumn = cursor.getColumnIndexOrThrow(SENDER_ID);
        }
        return Long.parseLong(cursor.getString(senderIdColumn));
    }

    public static String contentType(String content)
    {
        return "vnd.android.cursor/vnd."+AUTHORITY+"."+content+"s";
    }

    public static String contentItemType(String content)
    {
        return "vnd.android.cursor/vnd."+AUTHORITY+"."+content;
    }
}
