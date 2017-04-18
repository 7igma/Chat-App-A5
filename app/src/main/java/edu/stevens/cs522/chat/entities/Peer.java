package edu.stevens.cs522.chat.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.net.InetAddress;
import java.util.Date;

import edu.stevens.cs522.chat.contracts.PeerContract;

/**
 * Created by dduggan.
 */

public class Peer implements Parcelable {

    public long id;

    public String name;

    // Last time we heard from this peer.
    public Date timestamp;

    public String address;

    public int port;

    // TODO add operations for parcels (Parcelable), cursors and contentvalues
    public Peer()
    {
    }

    protected Peer(Parcel in) {
        id = in.readLong();
        name = in.readString();
        long tmpTimestamp = in.readLong();
        timestamp = tmpTimestamp != -1 ? new Date(tmpTimestamp) : null;
        address = in.readString();
        port = in.readInt();
    }

    public Peer(Cursor cursor)
    {
        this.name = PeerContract.getName(cursor);
        this.timestamp = PeerContract.getTimestamp(cursor);
        this.address = PeerContract.getAddress(cursor);
        this.port = PeerContract.getPort(cursor);
    }

    public void writeToProvider(ContentValues out)
    {
        PeerContract.putName(out, name);
        PeerContract.putTimestamp(out, timestamp);
        PeerContract.putAddress(out, address);
        PeerContract.putPort(out, port);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeLong(timestamp != null ? timestamp.getTime() : -1L);
        dest.writeString(address);
        dest.writeInt(port);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Peer> CREATOR = new Parcelable.Creator<Peer>() {
        @Override
        public Peer createFromParcel(Parcel in) {
            return new Peer(in);
        }

        @Override
        public Peer[] newArray(int size) {
            return new Peer[size];
        }
    };
}

