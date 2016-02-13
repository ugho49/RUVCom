package fr.nantes.iut.ruvcom.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import fr.nantes.iut.ruvcom.Bean.Message;
import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.RUVComApplication;
import fr.nantes.iut.ruvcom.Utils.Config;
import fr.nantes.iut.ruvcom.Utils.CountingRequestBody;
import fr.nantes.iut.ruvcom.Utils.Requestor;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class UploadActivity extends RUVBaseActivity {

    private ProgressBar progressBar;
    private String filePath = null;
    private TextView txtPercentage;
    private ImageView imgPreview;
    long totalSize = 0;

    private User user;
    private User distantUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        txtPercentage = (TextView) findViewById(R.id.txtPercentageUpload);
        progressBar = (ProgressBar) findViewById(R.id.progressBarUpload);
        imgPreview = (ImageView) findViewById(R.id.imgPreviewUpload);

        Intent intent = getIntent();
        // image or video path that is captured in previous activity
        filePath = intent.getStringExtra("filePath");
        user = (User) intent.getSerializableExtra("user");
        distantUser = (User) intent.getSerializableExtra("distantUser");

        setSupportActionBar(toolbar);

        if (filePath != null) {
            // Displaying the image or video on the screen
            previewMedia();
            new UploadFileTask().execute();
        } else {
            Toast.makeText(getApplicationContext(), "Impossible de capturer la photo", Toast.LENGTH_LONG).show();
            finish();
        }

        applyColor();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //overridePendingTransition(R.anim.in_left_to_right, R.anim.out_left_to_right);
    }

    /**
     * Displaying captured image on the screen
     */
    private void previewMedia() {
        imgPreview.setVisibility(View.VISIBLE);
        // bimatp factory
        BitmapFactory.Options options = new BitmapFactory.Options();
        // down sizing image as it throws OutOfMemory Exception for larger
        // images
        options.inSampleSize = 8;

        final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        imgPreview.setImageBitmap(bitmap);
    }

    /**
     * Uploading the file to server
     */
    private class UploadFileTask extends AsyncTask<Void, Integer, Message> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected Message doInBackground(Void... p) {

            Message result = null;

            try {
                String URL;
                if (RUVComApplication.mLastLocation != null) {
                    String geoLat = String.valueOf(RUVComApplication.mLastLocation.getLatitude());
                    String geoLong = String.valueOf(RUVComApplication.mLastLocation.getLongitude());
                    URL = String.format(Config.API_UPLOAD_PICTURE_GEO, String.valueOf(user.getId()), String.valueOf(distantUser.getId()), geoLat, geoLong);
                } else {
                    URL = String.format(Config.API_UPLOAD_PICTURE, String.valueOf(user.getId()), String.valueOf(distantUser.getId()));
                }

                final File sourceFile = new File(filePath);

                RequestBody requestBody =
                        new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("token", Config.SECRET_TOKEN)
                                .addFormDataPart("image", sourceFile.getName(),
                                        RequestBody.create(Requestor.MEDIA_TYPE_JPEG, sourceFile))
                                .build();

                // Decorate the request body to keep track of the upload progress
                CountingRequestBody countingBody = new CountingRequestBody(requestBody,
                        new CountingRequestBody.Listener() {
                            @Override
                            public void onRequestProgress(long bytesWritten, long contentLength) {
                                float percentage = 100f * bytesWritten / contentLength;
                                publishProgress((int) percentage);
                            }
                        });

                totalSize = countingBody.contentLength();

                final JSONObject response = new Requestor(URL).post(countingBody);

                if (response != null) {
                    if (!response.isNull("data")) {
                        result = new Message(response.getJSONObject("data"));
                    }
                }

            } catch (JSONException e) {
                Logger.e(e, "message");
            }

            return result;
        }

        @Override
        protected void onPostExecute(Message result) {
            try {
                File file = new File(filePath);

                file.delete();
                if (file.getParentFile().isDirectory()) {
                    File directory = file.getParentFile();

                    if (directory.listFiles().length == 0) {
                        directory.delete();
                    }
                }
            } catch (Exception e) {
                Logger.e(e, "message");
            }

            if (result == null) {
                // finish error
                setResult(RESULT_CANCELED);
            } else {
                // finish success with message
                getIntent().putExtra("message", result);
                setResult(RESULT_OK, getIntent());
            }

            finish();

            super.onPostExecute(result);
        }
    }
}
