package fr.nantes.iut.ruvcom.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;

public class ConversationActivity extends AppCompatActivity implements View.OnClickListener {

    User user;
    User distantUser;

    private ImageButton sendButton;
    private EditText message;


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
                Toast.makeText(getBaseContext(), user.getDisplayName() + " to " + distantUser.getDisplayName() + " : " + message.getText(), Toast.LENGTH_LONG).show();
                message.setText("");
                break;
        }
    }
}
