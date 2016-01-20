package fr.nantes.iut.ruvcom.Activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import fr.nantes.iut.ruvcom.Bean.Conversation;
import fr.nantes.iut.ruvcom.Bean.Message;
import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.Utils.Config;
import fr.nantes.iut.ruvcom.Utils.Requestor;

public class ConversationActivity extends AppCompatActivity implements View.OnClickListener {

    private User user;
    private User distantUser;

    private ImageButton sendButton;
    private EditText message;
    private String messageToSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        user = (User) getIntent().getSerializableExtra("user");
        distantUser = (User) getIntent().getSerializableExtra("distantUser");

        sendButton = (ImageButton) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);

        message = (EditText) findViewById(R.id.message);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(distantUser.getDisplayName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                messageToSend = message.getText().toString();
                new getMessagesTask().execute();
                break;
        }
    }

    private class getMessagesTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            //showProgressDialog();
        }

        @Override
        protected String doInBackground(Void... u) {

            String result = "";

            try{
                String URL = String.format(Config.API_MESSAGE_POST, String.valueOf(user.getId()), String.valueOf(distantUser.getId()));

                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("token", Config.SECRET_TOKEN));
                params.add(new BasicNameValuePair("message", messageToSend));

                JSONObject json = new Requestor(URL).post(params);
                JSONArray message = null;
                if (json != null) {
                    if (!json.isNull("data")) {
                        message = json.getJSONArray("data");
                    }
                }
            }
            catch(Exception ex) {
                Log.d("CONV ACTIVITY", "Fail : " + ex.getMessage());
            }

            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            message.setText("");
            Toast.makeText(getBaseContext(), "Message envoyé !", Toast.LENGTH_SHORT).show();
        }
    }
}