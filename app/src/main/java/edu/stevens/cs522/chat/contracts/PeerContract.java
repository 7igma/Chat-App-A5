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

public class PeerContract extends BaseContract {

    public static final Uri CONTENT_URI = CONTENT_URI(AUTHORITY, "Peer");

    public static final Uri CONTENT_URI(long id) {
        return CONTENT_URI(Long.toString(id));
    }

    public static final Uri CONTENT_URI(String id) {
        return withExtendedPath(CONTENT_URI, id);
    }

    public static final String CONTENT_PATH = CONTENT_PATH(CONTENT_URI);

    public static final String CONTENT_PATH_ITEM = CONTENT_PATH(CONTENT_URI("#"));


    public static final String ID = _ID;

    // TODO define column names, getters for cursors, setters for contentvalues
    public static final String NAME = "name";
    public static final String TIMESTAMP = "timestamp";
    public static final String ADDRESS = "address";
    public static final String PORT = "port";
    public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

    public static void putName(ContentValues values, String name)
    {
        values.put(NAME, name);
    }
    private static int nameColumn = -1;
    public static String getName(Cursor cursor)
    {
        if (nameColumn < 0)
        {
            nameColumn = cursor.getColumnIndexOrThrow(NAME);
        }
        return cursor.getString(nameColumn);
    }


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



    public static void putAddress(ContentValues values, String address)
    {
        values.put(ADDRESS, address);
    }
    private static int addressColumn = -1;
    public static String getAddress(Cursor cursor)
    {
        if (addressColumn < 0)
        {
            addressColumn = cursor.getColumnIndexOrThrow(ADDRESS);
        }
        return cursor.getString(addressColumn);
    }



    public static void putPort(ContentValues values, int port)
    {
        values.put(PORT, Integer.toString(port));
    }
    private static int portColumn = -1;
    public static int getPort(Cursor cursor)
    {
        if (portColumn < 0)
        {
            portColumn = cursor.getColumnIndexOrThrow(PORT);
        }
        return Integer.parseInt(cursor.getString(portColumn));
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
