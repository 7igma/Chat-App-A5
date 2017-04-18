package edu.stevens.cs522.chat.managers;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.chat.async.AsyncContentResolver;
import edu.stevens.cs522.chat.async.IContinue;
import edu.stevens.cs522.chat.async.IEntityCreator;
import edu.stevens.cs522.chat.async.QueryBuilder;
import edu.stevens.cs522.chat.async.QueryBuilder.IQueryListener;
import edu.stevens.cs522.chat.async.SimpleQueryBuilder;
import edu.stevens.cs522.chat.contracts.PeerContract;
import edu.stevens.cs522.chat.entities.Peer;


/**
 * Created by dduggan.
 */

public class PeerManager extends Manager<Peer> {

    private static final int LOADER_ID = 2;

    private static final IEntityCreator<Peer> creator = new IEntityCreator<Peer>() {
        @Override
        public Peer create(Cursor cursor) {
            return new Peer(cursor);
        }
    };

    private AsyncContentResolver contentResolver;

    private Context myContext;

    public PeerManager(Context context) {
        super(context, creator, LOADER_ID);
        contentResolver = new AsyncContentResolver(context.getContentResolver());
        this.myContext = context;
    }

    public void getAllPeersAsync(IQueryListener<Peer> listener) {
        // TODO use QueryBuilder to complete this
        QueryBuilder.executeQuery("main2", (Activity) myContext, PeerContract.CONTENT_URI, LOADER_ID, creator, listener);
    }

    public void getPeerAsync(long id, SimpleQueryBuilder.ISimpleQueryListener<Peer> listener) {
        // TODO need to check that peer is not null (not in database)
        //Long[] ids = new Long[toBeDeleted.size()];
        //toBeDeleted.toArray(ids);
        String[] args = new String[] {Long.toString(id)};

        StringBuilder sb = new StringBuilder();
        sb.append(PeerContract.ID);
        sb.append("=?");
        String select = sb.toString();

        contentResolver.queryAsync(PeerContract.CONTENT_URI, null, select, args, null, listener);

    }

    public void persistAsync(final Peer peer, IContinue<Uri> callback) {
        ContentValues values = new ContentValues();
        peer.writeToProvider(values);
        contentResolver.insertAsync(PeerContract.CONTENT_URI, values, callback);
    }

}
