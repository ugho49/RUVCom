package fr.nantes.iut.ruvcom.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.orhanobut.logger.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.RUVComApplication;
import fr.nantes.iut.ruvcom.Utils.Config;
import fr.nantes.iut.ruvcom.Utils.ConnectionDetector;
import fr.nantes.iut.ruvcom.Utils.NamedPreferences;
import fr.nantes.iut.ruvcom.Utils.Requestor;
import fr.nantes.iut.ruvcom.gcm.RegistrationIntentService;

/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile.
 */
public class SignInActivity extends RUVBaseActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        View.OnClickListener {

    private static final String TAG = "SignInActivity";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInAccount mGoogleSignInAccount;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private CoordinatorLayout layoutForSnack;
    private ProgressDialog mProgressDialog;
    private SignInButton signInButton;

    private User distantUserFromNotif = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.blue_grey_900));
        }

        // Views
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        layoutForSnack = (CoordinatorLayout) findViewById(R.id.layoutForSnack);

        // Button listeners
        signInButton.setOnClickListener(this);

        if(getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(NamedPreferences.DISTANT_USER_FROM_PUSH)) {
                distantUserFromNotif = (User) getIntent().getSerializableExtra(NamedPreferences.DISTANT_USER_FROM_PUSH);
            }
        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(NamedPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Logger.t(TAG).d("Token retrieved and sent to server! You can now use gcmsender to send downstream messages to this app.");
                } else {
                    Logger.t(TAG).d("An error occurred while either fetching the InstanceID token,\n" +
                            "sending the fetched token to the server or subscribing to the PubSub topic. Please try\n" +
                            "running the sample again.");
                }
            }
        };

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        RUVComApplication.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
        .build();

        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());

        // Override preferences for colors
        applyColorActivitySignIn();

    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(RUVComApplication.mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Logger.t(TAG).d("Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        RUVComApplication.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(RUVComApplication.mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(NamedPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Logger.t(TAG).d("handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            mGoogleSignInAccount = result.getSignInAccount();
            User user = new User();
            user.setGoogleId(mGoogleSignInAccount.getId());
            user.setEmail(mGoogleSignInAccount.getEmail().toString());
            user.setDisplayName(mGoogleSignInAccount.getDisplayName().toString());

            if(mGoogleSignInAccount.getPhotoUrl() == null) {
                user.setImageUrl("");
            } else {
                user.setImageUrl(mGoogleSignInAccount.getPhotoUrl().toString());
            }

            new LoginTask(user).execute();
        } else {
            setSigninButtonHidden(false);

            if (!ConnectionDetector.isConnectingToInternet(getApplicationContext())) {
                showSnackError("Aucune connexion internet !");
            }
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(RUVComApplication.mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Logger.t(TAG).d("onConnectionFailed:" + connectionResult);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void setSigninButtonHidden(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        } else {
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private boolean checkPlayServices() {
        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Logger.t(TAG).i("This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void showSnackError(String message) {
        final Snackbar snackbar = Snackbar
                .make(layoutForSnack, message, Snackbar.LENGTH_LONG)
                .setAction("Réessayer", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        signIn();
                    }
                });

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private class LoginTask extends AsyncTask<Void, Void, User> {

        private User user;

        public LoginTask(User user) {
            this.user = user;
        }

        @Override
        protected void onPreExecute() {
            setSigninButtonHidden(true);
            showProgressDialog();
        }

        @Override
        protected User doInBackground(Void... u) {

            User result = null;

            try{
                StringBuilder URL_COVER = new StringBuilder();
                URL_COVER.append("https://www.googleapis.com/plus/v1/people/");
                URL_COVER.append(user.getGoogleId());
                URL_COVER.append("?fields=cover%2FcoverPhoto%2Furl&key=");
                URL_COVER.append(Config.API_KEY);

                String googleID      = URLEncoder.encode(user.getGoogleId(), "UTF-8");
                String displayName   = URLEncoder.encode(user.getDisplayName(), "UTF-8");
                String email         = URLEncoder.encode(user.getEmail(), "UTF-8");
                String imageURL      = user.getImageUrl();
                String coverURL      = "";

                final JSONObject coverObj = new Requestor(URL_COVER.toString()).get();

                if (!coverObj.isNull("cover")) {
                    if (!coverObj.getJSONObject("cover").isNull("coverPhoto")) {
                        coverURL = coverObj.getJSONObject("cover").getJSONObject("coverPhoto").getString("url");
                    }
                }

                String URL_USER_EXIST = String.format(Config.API_USER_EXIST, googleID);

                final JSONObject resultExists = new Requestor(URL_USER_EXIST).get();
                Boolean error = resultExists.getBoolean("error");

                String URL_UPDATE_OR_REGISTER = null;

                ArrayList<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("token", Config.SECRET_TOKEN));
                params.add(new BasicNameValuePair("imageUrl", imageURL));
                params.add(new BasicNameValuePair("coverImageUrl", coverURL));

                if(error) {
                    URL_UPDATE_OR_REGISTER = String.format(Config.API_USER_CREATE, googleID, displayName, email);
                } else {
                    String userId = String.valueOf(resultExists.getJSONObject("data").getInt("id"));
                    URL_UPDATE_OR_REGISTER = String.format(Config.API_USER_UPDATE, userId, googleID, displayName, email);
                }

                final JSONObject json = new Requestor(URL_UPDATE_OR_REGISTER).post(params);
                JSONObject data = null;
                if (json != null) {
                    if (!json.isNull("data")) {
                        data = json.getJSONObject("data");
                    }
                }

                result = new User(data);
            }
            catch(Exception ex) {
                Logger.e(ex, "message");
            }

            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(User result) {

            hideProgressDialog();

            if (result == null) {
                showSnackError("Erreur d'authentification");
                setSigninButtonHidden(false);
            } else {

                if (checkPlayServices()) {
                    // Start IntentService to register this application with GCM.
                    Intent intent = new Intent(SignInActivity.this, RegistrationIntentService.class);
                    intent.putExtra("user", result);
                    startService(intent);
                }

                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("user", result);

                if(distantUserFromNotif != null) {
                    intent.putExtra(NamedPreferences.DISTANT_USER_FROM_PUSH, distantUserFromNotif);
                }

                startActivity(intent);
                finish();
            }
        }
    }
}
