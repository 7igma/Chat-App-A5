package edu.stevens.cs522.chat.async;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import edu.stevens.cs522.chat.managers.TypedCursor;

/**
 * Created by dduggan.
 */

public class QueryBuilder<T> implements LoaderManager.LoaderCallbacks<Cursor> {

    public static interface IQueryListener<T> {

        public void handleResults(TypedCursor<T> results);

        public void closeResults();

    }

    // TODO complete the implementation of this
    private String tag;
    private Context context;
    private Uri uri;
    private int loaderID;
    private IEntityCreator<T> creator;
    private IQueryListener<T> listener;

    private QueryBuilder(String tag, Context context, Uri uri, int loaderID, IEntityCreator<T> creator, IQueryListener<T> listener)
    {
        this.tag = tag;
        this.context = context;
        this.uri = uri;
        this.loaderID = loaderID;
        this.creator = creator;
        this.listener = listener;
    }

    public static <T> void executeQuery(String tag, Activity context, Uri uri, int loaderID, IEntityCreator<T> creator, IQueryListener<T> listener)
    {
        Log.i("QueryBuilder", "executeQuery: initializing loaderManager");
        QueryBuilder<T> qb = new QueryBuilder<>(tag, context, uri, loaderID, creator, listener);

        LoaderManager lm = context.getLoaderManager();

        lm.initLoader(loaderID, null, qb);
    }

    public static <T> void executeQuery(String tag, Activity context, Uri uri, int loaderID, String[] projection, String select, String[] selectArgs, IEntityCreator<T> creator, IQueryListener<T> listener)
    {
        QueryBuilder<T> qb = new QueryBuilder<>(tag, context, uri, loaderID, creator, listener);

        LoaderManager lm = context.getLoaderManager();

        lm.initLoader(loaderID, null, qb);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args)
    {
        if (id == loaderID)
        {
            return new CursorLoader(context, uri, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        if (loader.getId() == loaderID)
        {
            listener.handleResults(new TypedCursor<T>(cursor, creator));
        }
        else
        {
            throw new IllegalStateException("Unexpected loader callback");
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        if (loader.getId() == loaderID)
        {
            listener.closeResults();
        }
        else
        {
            throw new IllegalStateException("Unexpected loader callback");
        }
    }
}
