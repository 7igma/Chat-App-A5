package edu.stevens.cs522.chat.async;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dduggan.
 */


public class SimpleQueryBuilder<T> implements IContinue<Cursor>{

    public interface ISimpleQueryListener<T> {

        public void handleResults(List<T> results);

    }

    // TODO Complete the implementation of this

    private IEntityCreator<T> helper;
    private ISimpleQueryListener<T> listener;

    private SimpleQueryBuilder(IEntityCreator<T> helper, ISimpleQueryListener<T> listener)
    {
        this.helper = helper;
        this.listener = listener;
    }

    public static <T> void executeQuery(Activity context, Uri uri, IEntityCreator<T> helper, ISimpleQueryListener<T> listener)
    {
        SimpleQueryBuilder<T> qb = new SimpleQueryBuilder<T>(helper, listener);

        AsyncContentResolver resolver = new AsyncContentResolver(context.getContentResolver());

        resolver.queryAsync(uri, null, null, null, null, qb);
    }

    public static <T> void executeQuery(Activity context, Uri uri, String[] projection, String select, String[] selectArgs, IEntityCreator<T> helper, ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder<T> qb = new SimpleQueryBuilder<T>(helper, listener);

        AsyncContentResolver resolver = new AsyncContentResolver(context.getContentResolver());

        resolver.queryAsync(uri, projection, select, selectArgs, null, qb);
    }

    @Override
    public void kontinue(Cursor value) {
        // TODO complete this
        if (value != null)
        {
            List<T> instances = new ArrayList<T>();
            if (value.moveToFirst()) {
                do {
                    T instance = helper.create(value);
                    instances.add(instance);
                } while (value.moveToNext());
            }
            value.close();
            listener.handleResults(instances);
        }
        else
        {
            listener.handleResults(null);
        }
    }

}
