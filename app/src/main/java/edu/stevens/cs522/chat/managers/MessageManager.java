package edu.stevens.cs522.chat.managers;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import edu.stevens.cs522.chat.async.AsyncContentResolver;
import edu.stevens.cs522.chat.async.IContinue;
import edu.stevens.cs522.chat.async.IEntityCreator;
import edu.stevens.cs522.chat.async.QueryBuilder;
import edu.stevens.cs522.chat.async.QueryBuilder.IQueryListener;
import edu.stevens.cs522.chat.contracts.MessageContract;
import edu.stevens.cs522.chat.entities.ChatMessage;


/**
 * Created by dduggan.
 */

public class MessageManager extends Manager<ChatMessage> {

    private static final int LOADER_ID = 1;

    private static final IEntityCreator<ChatMessage> creator = new IEntityCreator<ChatMessage>() {
        @Override
        public ChatMessage create(Cursor cursor) {
            return new ChatMessage(cursor);
        }
    };

    private AsyncContentResolver contentResolver;

    private Context myContext;

    public MessageManager(Context context) {
        super(context, creator, LOADER_ID);
        contentResolver = new AsyncContentResolver(context.getContentResolver());
        this.myContext = context;
    }

    public void getAllMessagesAsync(IQueryListener<ChatMessage> listener) {
        // TODO use QueryBuilder to complete this
        QueryBuilder.executeQuery("main", (Activity) myContext, MessageContract.CONTENT_URI, LOADER_ID, creator, listener);
    }

    public void persistAsync(final ChatMessage message) {
        IContinue<Uri> callback = new IContinue<Uri>() {
            public void kontinue(Uri uri) {
                message.id = (int) MessageContract.getId(uri);
            }
        };
        ContentValues values = new ContentValues();
        Log.i("MessageManager", "persistAsync: message text="+message.messageText);
        message.writeToProvider(values);
        Log.i("MessageManager", "persistAsync: calling contentresolver insertAsync");
        contentResolver.insertAsync(MessageContract.CONTENT_URI, values, callback);
    }

}
