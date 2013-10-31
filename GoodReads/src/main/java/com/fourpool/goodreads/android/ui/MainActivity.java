package com.fourpool.goodreads.android.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.fourpool.goodreads.android.GoodReadsApplication;
import com.fourpool.goodreads.android.R;
import com.fourpool.goodreads.android.event.OAuthVerifierFetchedEvent;
import com.fourpool.goodreads.android.event.SessionCreatedEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import oauth.signpost.OAuth;
import timber.log.Timber;

import static com.fourpool.goodreads.android.Constants.CALLBACK_SCHEME;

public class MainActivity extends Activity {
    @Inject Bus bus;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inject ourself into the object graph.
        ((GoodReadsApplication) getApplication()).inject(this);
    }

    @Override protected void onStart() {
        super.onStart();
        bus.register(this);
    }

    @Override protected void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    @Override public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.d("onNewIntent called");

        final Uri uri = intent.getData();
        if (uri != null
                && uri.getScheme().equals(CALLBACK_SCHEME)) {

            final String oauthVerifier = uri
                    .getQueryParameter(OAuth.OAUTH_TOKEN);

            Timber.d("Extracted OAuth verifier: %s", oauthVerifier);
            bus.post(new OAuthVerifierFetchedEvent(oauthVerifier));
        }
    }

    @Subscribe public void onSessionCreatedEvent(SessionCreatedEvent event) {
        RecentUpdatesFragment fragment = new RecentUpdatesFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment container with this fragment, and add the transaction to the back stack
        // so the user can navigate back.
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);

        // Commit the transaction.
        transaction.commit();
    }
}