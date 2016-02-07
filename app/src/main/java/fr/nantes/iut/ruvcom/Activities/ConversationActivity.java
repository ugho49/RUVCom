package fr.nantes.iut.ruvcom.Activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.jensdriller.libs.multistatelistview.MultiStateListView;
import com.orhanobut.logger.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.nantes.iut.ruvcom.Adapter.ListViewMessagesAdapter;
import fr.nantes.iut.ruvcom.Bean.Message;
import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.Utils.Config;
import fr.nantes.iut.ruvcom.Utils.Requestor;

public class ConversationActivity extends RUVBaseActivity implements View.OnClickListener {

    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    //private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final int UPLOAD_ACTIVITY_REQUEST_CODE = 300;

    public static final int MEDIA_TYPE_IMAGE = 1;
    //public static final int MEDIA_TYPE_VIDEO = 2;

    private static final String TAG = "ConversationActivity";

    public static User user;
    public static User distantUser;

    private EditText editTextMessage;

    public static ListViewMessagesAdapter adapter;
    public static MultiStateListView messageListView;

    private Uri fileUri; // file url to store image/video

    public static List<Message> messages = new ArrayList<>();

    public static Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_conversation);

        user = (User) getIntent().getSerializableExtra("user");
        distantUser = (User) getIntent().getSerializableExtra("distantUser");

        ImageButton sendButton = (ImageButton) findViewById(R.id.sendButton);
        ImageButton btnCapturePicture = (ImageButton) findViewById(R.id.cameraButton);
        editTextMessage = (EditText) findViewById(R.id.message);
        messageListView = (MultiStateListView) findViewById(R.id.listViewMessages);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        sendButton.setOnClickListener(this);
        editTextMessage.setHint("Message à " + distantUser.getDisplayName());

        toolbar.setTitle(distantUser.getDisplayName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            btnCapturePicture.setVisibility(View.GONE);
        } else {
            btnCapturePicture.setOnClickListener(this);
        }

        // Disable notification if exist
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();

        new getMessagesTask().execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_left_to_right, R.anim.out_left_to_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.in_left_to_right, R.anim.out_left_to_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendButton:
                if(!"".equals(editTextMessage.getText().toString())) {
                    String m = editTextMessage.getText().toString();
                    new sendMessageTask(m).execute();
                }
                break;
            case R.id.cameraButton:
                captureImage();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Intent uploadIntent = new Intent(ConversationActivity.this, UploadActivity.class);
                uploadIntent.putExtra("filePath", fileUri.getPath());
                uploadIntent.putExtra("user", user);
                uploadIntent.putExtra("distantUser", distantUser);
                startActivityForResult(uploadIntent, UPLOAD_ACTIVITY_REQUEST_CODE);

                overridePendingTransition(R.anim.in_right_to_left, R.anim.out_right_to_left);

            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled Image capture
                Logger.t(TAG).d("User cancelled image capture");

            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Impossible de capturer la photo", Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == UPLOAD_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Message result = (Message) data.getSerializableExtra("message");
                messages.add(result);
                loadListView();

            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(), "Erreur lors de l'envoi, vérifier votre connexion réseau !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.LOCAL_IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Logger.t(TAG).d(TAG, "Oops! Failed create "
                        + Config.LOCAL_IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    /**
     * Checking device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }


    public static void goBottomListView() {
        //messageListView.setSelection(messageListView.getCount() - 1);
        messageListView.smoothScrollToPosition(0, messageListView.getHeight());
    }

    public static void loadListView() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... u) {
                if (adapter == null) {
                    adapter = new ListViewMessagesAdapter(context, messages, user, distantUser);
                } else {
                    adapter.setList(messages);
                    adapter.setDistantUser(distantUser);
                }

                updateRead();

                return null;
            }

            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(String result) {
                messageListView.setAdapter(adapter);
                goBottomListView();
            }
        }.execute();
    }

    private class sendMessageTask extends AsyncTask<Void, Void, Boolean> {

        private String message;

        public sendMessageTask(String message) {
            this.message = message;
        }

        @Override
        protected void onPreExecute() {
            editTextMessage.setText("");
        }

        @Override
        protected Boolean doInBackground(Void... u) {

            Boolean error = true;

            try{
                String URL = String.format(Config.API_MESSAGE_POST, String.valueOf(user.getId()), String.valueOf(distantUser.getId()));

                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("token", Config.SECRET_TOKEN));
                params.add(new BasicNameValuePair("message", message));

                final JSONObject json = new Requestor(URL).post(params);

                if (json != null) {
                    error = json.getBoolean("error");

                    if(!error) {
                        if(!json.isNull("data")) {
                            messages.add(new Message(json.getJSONObject("data")));
                        } else {
                            error = true;
                        }
                    }
                }
            }
            catch(Exception ex) {
                Logger.e(ex, "message");
            }

            return error;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Boolean error) {
            if(error) {
                Toast.makeText(getBaseContext(), "Message non envoyé :(", Toast.LENGTH_SHORT).show();
            } else {
                loadListView();
            }
        }
    }

    private class getMessagesTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            messages = new ArrayList<>();
        }

        @Override
        protected String doInBackground(Void... u) {

            //List<Message> result = new ArrayList<>();

            try{
                updateRead();

                String URL = String.format(Config.API_MESSAGES_GET, String.valueOf(user.getId()), String.valueOf(distantUser.getId()));

                final JSONObject json = new Requestor(URL).get();
                JSONArray arrayMessages = null;

                if (json != null) {
                    if (!json.isNull("data")) {
                        arrayMessages = json.getJSONArray("data");
                    }
                }

                if (arrayMessages != null) {
                    for(int i = 0 ; i < arrayMessages.length(); i++){
                        JSONObject msgObj = arrayMessages.getJSONObject(i);

                        if (msgObj != null) {
                            messages.add(new Message(msgObj));
                        }
                    }
                }
            }
            catch(Exception ex) {
                Logger.e(ex, "message");
            }

            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            loadListView();
        }
    }

    public static void updateRead() {
        // UPDATE MESSAGES READ
        String URL_UPDATE_MESSAGE_READ = String.format(Config.API_UPDATE_MESSAGE_READ, String.valueOf(user.getId()), String.valueOf(distantUser.getId()));

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", Config.SECRET_TOKEN));

        new Requestor(URL_UPDATE_MESSAGE_READ).post(params);
        // END UPDATE
    }
}