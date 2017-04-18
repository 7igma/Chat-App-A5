package edu.stevens.cs522.chat.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.entities.Peer;

/**
 * Created by dduggan.
 */

public class ViewPeerActivity extends Activity {

    public static final String PEER_KEY = "peer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_peer);

        Peer peer = getIntent().getParcelableExtra(PEER_KEY);
        if (peer == null) {
            throw new IllegalArgumentException("Expected peer as intent extra");
        }

        // TODO init the UI
        TextView nameText = (TextView) findViewById(R.id.view_user_name);
        nameText.setText(peer.name);

        TextView dateText = (TextView) findViewById(R.id.view_timestamp);
        dateText.setText(peer.timestamp.toString());

        TextView addrText = (TextView) findViewById(R.id.view_address);
        addrText.setText(peer.address);

        TextView portText = (TextView) findViewById(R.id.view_port);
        portText.setText(Integer.toString(peer.port));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 1, 0, "Back");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case 1:
                finish();
        }
        return false;
    }
}