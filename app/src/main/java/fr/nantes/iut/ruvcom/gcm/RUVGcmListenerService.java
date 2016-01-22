package fr.nantes.iut.ruvcom.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import fr.nantes.iut.ruvcom.Activities.RUVBaseActivity;
import fr.nantes.iut.ruvcom.Bean.Photo;
import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.Activities.SignInActivity;
import fr.nantes.iut.ruvcom.RUVComApplication;
import fr.nantes.iut.ruvcom.Utils.NamedPreferences;

/**
 * Created by ughostephan on 21/01/2016.
 */
public class RUVGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String jsonUser = data.getString("userSender");
        String jsonPhoto = data.getString("photo");

        User distantUser = null;
        Photo photo = null;

        try {
            if(jsonUser != null && jsonUser != "") {
                JSONObject jsonObjectUser = new JSONObject(jsonUser);
                distantUser = new User(jsonObjectUser);
            }

            if(jsonPhoto != null && jsonPhoto != "") {
                JSONObject jsonObjectPhoto = new JSONObject(jsonPhoto);
                photo = new Photo(jsonObjectPhoto);
            }
        }
        catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        if(RUVComApplication.applicationOnPause) {
            sendNotification(distantUser, message, photo);
        } else {
            // TODO : recupérer activity et ouvrir
        }
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(User distantuser, String message, Photo photo) {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(NamedPreferences.DISTANT_USER_FROM_PUSH, distantuser);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String title = "Message de " + distantuser.getDisplayName();
        String content = message;

        if(photo != null) {
            content = "Vous avez reçu une photo";
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
