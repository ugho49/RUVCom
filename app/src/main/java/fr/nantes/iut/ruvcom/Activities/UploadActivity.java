package fr.nantes.iut.ruvcom.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import fr.nantes.iut.ruvcom.Bean.Message;
import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.Utils.AndroidMultiPartEntity;
import fr.nantes.iut.ruvcom.Utils.Config;
import fr.nantes.iut.ruvcom.Utils.Requestor;

public class UploadActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtPercentage = (TextView) findViewById(R.id.txtPercentageUpload);
        progressBar = (ProgressBar) findViewById(R.id.progressBarUpload);
        imgPreview = (ImageView) findViewById(R.id.imgPreviewUpload);

        Intent intent = getIntent();
        // image or video path that is captured in previous activity
        filePath = intent.getStringExtra("filePath");
        user = (User) intent.getSerializableExtra("user");
        distantUser = (User) intent.getSerializableExtra("distantUser");

        if (filePath != null) {
            // Displaying the image or video on the screen
            previewMedia();
            new UploadFileTask().execute();
        } else {
            Toast.makeText(getApplicationContext(), "Erreur de capture", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_left_to_right, R.anim.out_left_to_right);
    }

    /**
     * Displaying captured image on the screen
     * */
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
     * */
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
                String URL = String.format(Config.API_UPLOAD_PICTURE, String.valueOf(user.getId()), String.valueOf(distantUser.getId()));

                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                final File sourceFile = new File(filePath);
                /*final String name = sourceFile.getName();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Bitmap bm = BitmapFactory.decodeFile(filePath);
                bm.compress(Bitmap.CompressFormat.JPEG, 90, baos);*/

                FileBody fileBody = new FileBody(sourceFile);

                // Adding file data to http body
                entity.addPart("image", fileBody);
                //entity.addPart("image", new ByteArrayBody(baos.toByteArray(), name));
                totalSize = entity.getContentLength();
                entity.addPart("token", new StringBody(Config.SECRET_TOKEN));

                JSONObject response = new Requestor(URL).post(entity);


                if(response != null) {
                    if (!response.isNull("data")) {
                        result = new Message(response.getJSONObject("data"));
                    }
                }

            } catch (IOException e) {
               Log.e("UploadPicture", e.getMessage());
            } catch (JSONException e) {
                Log.e("UploadPicture", e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(Message result) {

            if(result == null) {
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
