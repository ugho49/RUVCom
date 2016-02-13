package fr.nantes.iut.ruvcom.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import fr.nantes.iut.ruvcom.Activities.ConversationActivity;
import fr.nantes.iut.ruvcom.Activities.MainActivity;
import fr.nantes.iut.ruvcom.Bean.Message;
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

    private static final String TAG = RUVGcmListenerService.class.getSimpleName();

    private SharedPreferences preferences;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Message message = new Message();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        message.setId(data.getInt("id"));
        message.setMessage(data.getString("message"));
        message.setIdUserSender(data.getInt("idUserSender"));
        message.setIdUserReceiver(data.getInt("idUserReceiver"));
        message.setIsRead(data.getBoolean("isRead"));
        message.setDateTime(data.getString("dateTime"));
        message.setPhoto(null);

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
                message.setPhoto(photo);
            }
        }
        catch (JSONException e) {
            //Log.e(TAG, e.getMessage());
            Logger.e(e, "message");
        }

        Logger.t(TAG).i("Notification Content " + message.getMessage());

        if(RUVComApplication.applicationOnPause) {
            sendNotification(distantUser, message, photo, false);
        } else {

            if(ConversationActivity.class.getSimpleName().equals(RUVComApplication.activityRunningName)) {
                User currentDistantUser = ConversationActivity.distantUser;

                if(currentDistantUser != null) {
                    if(currentDistantUser.getId() == distantUser.getId()) {
                        ConversationActivity.messages.add(message);
                        ConversationActivity.loadListView();
                    } else {
                        sendNotification(distantUser, message, photo, true);
                    }
                }
            } else {
                if(MainActivity.class.getSimpleName().equals(RUVComApplication.activityRunningName)) {
                    MainActivity.reloadListConv();
                }
                sendNotification(distantUser, message, photo, true);
            }
        }
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(User distantuser, Message message, Photo photo, Boolean running) {
        Intent intent;

        if(running) {
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("user", MainActivity.user);
        } else {
            intent = new Intent(this, SignInActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(NamedPreferences.DISTANT_USER_FROM_PUSH, distantuser);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String title = "Message de " + distantuser.getDisplayName();
        String content = message.getMessage();

        if(photo != null) {
            content = "Vous avez reçu une photo";
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String sound = preferences.getString(NamedPreferences.NOTIFICATION_RINGTONE, defaultSoundUri.toString());
        Uri soundUri = Uri.parse(sound);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        if (!preferences.getBoolean(NamedPreferences.NOTIFICATION_VIBRATE_ENABLE, true)) {
            // Disable vibrate
            notificationBuilder.setVibrate(new long[]{0l});
        }

        notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setLights(Color.BLUE, 3000, 3000)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (preferences.getBoolean(NamedPreferences.NOTIFICATION_ENABLE, true)) {
            // Display notification only if user want it
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

}
