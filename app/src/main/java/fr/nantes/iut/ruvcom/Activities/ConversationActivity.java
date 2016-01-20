package fr.nantes.iut.ruvcom.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;

public class ConversationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        //User user = (User) getIntent().getSerializableExtra("user");
        User distantUser = (User) getIntent().getSerializableExtra("distantUser");

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
}
