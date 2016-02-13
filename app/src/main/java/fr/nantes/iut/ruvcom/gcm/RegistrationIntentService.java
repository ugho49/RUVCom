package fr.nantes.iut.ruvcom.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.io.IOException;

import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.Utils.Config;
import fr.nantes.iut.ruvcom.Utils.NamedPreferences;
import fr.nantes.iut.ruvcom.Utils.Requestor;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


/**
 * Created by ughostephan on 18/01/2016.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    private User currentUser;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        currentUser = (User) intent.getSerializableExtra("user");

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.

            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(String.valueOf(Config.SENDER_ID),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Logger.t(TAG).i("GCM Registration Token: " + token);

            sendRegistrationToServer(token);

            // Subscribe to topic channels
            subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(NamedPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            //Log.d(TAG, "Failed to complete token refresh", e);
            Logger.e(e, "Failed to complete token refresh ");
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(NamedPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(NamedPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(final String token) {
        new AsyncTask<Void, Void, String> () {

            @Override
            protected void onPreExecute() {}

            @Override
            protected String doInBackground(Void... p) {

                try{
                    String URL = String.format(Config.API_USER_REGISTER_GCM, String.valueOf(currentUser.getId()));

                    RequestBody requestBody =
                            new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("token", Config.SECRET_TOKEN)
                                    .addFormDataPart("idDevice", token)
                                    .build();

                    final JSONObject json = new Requestor(URL).post(requestBody);
                }
                catch(Exception ex) {
                    //Log.d(TAG, "Fail : " + ex.getMessage());
                    Logger.e(ex, "message");
                }

                return null;
            }

            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(String result) {}
        }.execute();
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}
