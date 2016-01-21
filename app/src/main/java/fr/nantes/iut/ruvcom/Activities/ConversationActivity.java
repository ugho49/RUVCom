package fr.nantes.iut.ruvcom.Activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.nantes.iut.ruvcom.Adapter.ListViewMessagesAdapter;
import fr.nantes.iut.ruvcom.Bean.Message;
import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.Utils.Config;
import fr.nantes.iut.ruvcom.Utils.Requestor;

public class ConversationActivity extends AppCompatActivity
        implements View.OnClickListener {

    private User user;
    private User distantUser;

    private ImageButton sendButton;
    private ImageButton cameraButton;
    private EditText editTextMessage;
    private Toolbar toolbar;

    private ListViewMessagesAdapter adapter;
    private ListView messageListView;

    private List<Message> messages = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_conversation);

        user = (User) getIntent().getSerializableExtra("user");
        distantUser = (User) getIntent().getSerializableExtra("distantUser");

        sendButton = (ImageButton) findViewById(R.id.sendButton);
        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        editTextMessage = (EditText) findViewById(R.id.message);
        messageListView = (ListView) findViewById(R.id.listViewMessages);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        sendButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);

        toolbar.setTitle(distantUser.getDisplayName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                    new sendMessageTask(editTextMessage.getText().toString()).execute();
                }
                break;
            case R.id.cameraButton:
                Toast.makeText(getBaseContext(), "Send picture ....", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void goBottomListView() {
        messageListView.setSelection(messageListView.getCount() - 1);
    }

    private void loadListView() {
        if (adapter == null) {
            adapter = new ListViewMessagesAdapter(getApplicationContext(), messages, user, distantUser);
        } else {
            adapter.setList(messages);
        }

        messageListView.setAdapter(adapter);
        goBottomListView();
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

                JSONObject json = new Requestor(URL).post(params);

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
                Log.d("CONV ACTIVITY", "Fail : " + ex.getMessage());
            }

            return error;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Boolean error) {
            if(error) {
                Toast.makeText(getBaseContext(), "Message non envoyé :(", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), "Message envoyé !", Toast.LENGTH_SHORT).show();
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
                String URL = String.format(Config.API_MESSAGES_GET, String.valueOf(user.getId()), String.valueOf(distantUser.getId()));

                JSONObject json = new Requestor(URL).get();
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
                Log.d("CONV ACTIVITY", "Fail : " + ex.getMessage());
            }

            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            loadListView();
        }
    }
}