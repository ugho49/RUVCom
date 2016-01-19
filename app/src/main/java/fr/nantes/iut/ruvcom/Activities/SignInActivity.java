package fr.nantes.iut.ruvcom.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Contacts;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.internal.model.people.PersonEntity;
import com.google.android.gms.plus.model.people.Person;
import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.Utils.Config;

/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile.
 */
public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount mGoogleSignInAccount;

    private ProgressDialog mProgressDialog;
    private SignInButton signInButton;

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

        // Button listeners
        signInButton.setOnClickListener(this);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
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
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
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

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            mGoogleSignInAccount = result.getSignInAccount();
            setSigninButtonHidden(true);
            //Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            Log.d(TAG, mGoogleSignInAccount.zzmI());

            User user = new User();
            user.setGoogleId(mGoogleSignInAccount.getId());
            user.setEmail(mGoogleSignInAccount.getEmail().toString());
            user.setDisplayName(mGoogleSignInAccount.getDisplayName().toString());

            if(mGoogleSignInAccount.getPhotoUrl() == null) {
                user.setImageUrl("");
            } else {
                user.setImageUrl(mGoogleSignInAccount.getPhotoUrl().toString());
            }

            LoginTask task = new LoginTask(user);
            task.execute();
        } else {
            setSigninButtonHidden(false);
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
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

    private class LoginTask extends AsyncTask<Void, Void, User> {

        private User user;
        private String bannerUrl = "";

        public LoginTask(User user) {
            this.user = user;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected User doInBackground(Void... u) {

            User result = null;

            try{

                String googleID      = URLEncoder.encode(user.getGoogleId(), "UTF-8");
                String displayName   = URLEncoder.encode(user.getDisplayName(), "UTF-8");
                String email         = URLEncoder.encode(user.getEmail(), "UTF-8");
                String imageURL      = user.getImageUrl();

                HttpClient httpclient = new DefaultHttpClient();

                String URL_USER_EXIST = String.format(Config.API_USER_EXIST, googleID);

                HttpGet httpget = new HttpGet(URL_USER_EXIST);
                HttpResponse responseUserExist = httpclient.execute(httpget);
                String entityUserExist = EntityUtils.toString(responseUserExist.getEntity());

                JSONObject resultExists = new JSONObject(entityUserExist);
                Boolean error = resultExists.getBoolean("error");

                String URL_UPDATE_OR_REGISTER = null;

                String urlBanner = "https://www.googleapis.com/plus/v1/people/" +
                        user.getGoogleId() +
                        "?fields=cover%2FcoverPhoto%2Furl&key=" +
                        Config.API_KEY;

                HttpGet httpgetbanner = new HttpGet(urlBanner);
                HttpResponse responseBanner = httpclient.execute(httpgetbanner);
                String entityBanner = EntityUtils.toString(responseBanner.getEntity());

                bannerUrl = new JSONObject(entityBanner).getJSONObject("cover").getJSONObject("coverPhoto").getString("url");

                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("token", Config.SECRET_TOKEN));
                params.add(new BasicNameValuePair("imageUrl", imageURL));

                if(error) {

                    String message = resultExists.getString("message");

                    if("No user found !".equals(message)) {
                        URL_UPDATE_OR_REGISTER = String.format(Config.API_USER_CREATE, googleID, displayName, email);
                    }

                } else {
                    String userId = String.valueOf(resultExists.getJSONObject("data").getInt("id"));
                    URL_UPDATE_OR_REGISTER = String.format(Config.API_USER_UPDATE, userId, googleID, displayName, email);
                }

                HttpPost httpPost = new HttpPost(URL_UPDATE_OR_REGISTER);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
                HttpResponse responseUpdateOrRegister = httpclient.execute(httpPost);
                String entityUpdateOrRegister = EntityUtils.toString(responseUpdateOrRegister.getEntity());

                JSONObject resultUpdateOrRegister = new JSONObject(entityUpdateOrRegister);

                //result = entityUpdateOrRegister;
            }
            catch(Exception ex)
            {
                Log.d(TAG, "Fail : " + ex.getMessage());
            }

            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(User result) {
            hideProgressDialog();

            //Toast.makeText(getApplicationContext(), "Connexion réussie", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            //intent.putExtra("background", person.getImage().getUrl());
            intent.putExtra("avatar", mGoogleSignInAccount.getPhotoUrl().toString());
            intent.putExtra("email", mGoogleSignInAccount.getEmail().toString());
            intent.putExtra("background", bannerUrl);
            intent.putExtra("displayName", mGoogleSignInAccount.getDisplayName().toString());
            startActivity(intent);
        }
    }
}
