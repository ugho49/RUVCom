package fr.nantes.iut.ruvcom.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public abstract class AbstractGoogleActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    protected static final int RC_SIGN_IN = 0;

    // Profile pic image size in pixels
    protected static final int PROFILE_PIC_SIZE = 400;

    // Google client to interact with Google API
    protected GoogleApiClient mGoogleApiClient;

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    protected boolean mIntentInProgress;

    protected boolean mSignInClicked;

    protected ConnectionResult mConnectionResult;


    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


}
