package fr.nantes.iut.ruvcom.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.nantes.iut.ruvcom.Adapter.DialogListUserAdapter;
import fr.nantes.iut.ruvcom.Adapter.ListViewConversationAdapter;
import fr.nantes.iut.ruvcom.Bean.Conversation;
import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.Utils.Config;
import fr.nantes.iut.ruvcom.Utils.Requestor;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemClickListener {


    private static final String TAG = "MainActivity";

    private Toolbar toolbar;
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private ProgressDialog mProgressDialog;

    private CircularImageView navAvatar;
    private TextView navDisplayName;
    private TextView navEmail;
    private ImageView navBackground;

    private ListView convListView;

    private GoogleApiClient mGoogleApiClient;
    private ImageLoader imageLoader;

    private static User user;

    private ListViewConversationAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = (User) getIntent().getSerializableExtra("user");
        imageLoader = ImageLoader.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        convListView = (ListView) findViewById(R.id.listViewConversation);

        View navigationHeaderView = navigationView.getHeaderView(0);
        navAvatar = (CircularImageView) navigationHeaderView.findViewById(R.id.nav_avatar);
        navDisplayName = (TextView) navigationHeaderView.findViewById(R.id.nav_display_name);
        navEmail = (TextView) navigationHeaderView.findViewById(R.id.nav_email);
        navBackground = (ImageView) navigationHeaderView.findViewById(R.id.nav_background);

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showListUser();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        convListView.setOnItemClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        loadNavContent();

        new getConvTask().execute();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void loadNavContent() {
        imageLoader.displayImage(user.getImageUrl(), navAvatar);
        imageLoader.displayImage(user.getCoverImageUrl(), navBackground);
        navDisplayName.setText(user.getDisplayName());
        navEmail.setText(user.getEmail());
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.signout) {
            signOut();
        } else if (id == R.id.action_settings) {
            Toast.makeText(getApplicationContext(), "Click sur settings", Toast.LENGTH_SHORT).show();
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        AlertDialog.Builder signOutConfirmation = new AlertDialog.Builder(this);
        signOutConfirmation.setTitle("Déconnexion");
        signOutConfirmation.setMessage("Êtes-vous sûr ?");
        signOutConfirmation.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                Log.d(TAG, "signOut status : " + status.getStatusCode() + " - " + status.getStatusMessage());

                                Toast.makeText(getApplicationContext(), "Déconnexion réussie", Toast.LENGTH_SHORT).show();
                                Intent otherActivity = new Intent(getBaseContext(), SignInActivity.class);
                                startActivity(otherActivity);
                            }
                        });
            }
        });
        signOutConfirmation.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Code that is executed when clicking NO
                dialog.dismiss();
            }
        });
        AlertDialog alert = signOutConfirmation.create();
        alert.show();
    }

    private void showListUser() {
        new AsyncTask<Void, Void, List<User>> () {

            @Override
            protected void onPreExecute() {
                showProgressDialog();
            }

            @Override
            protected List<User> doInBackground(Void... u) {
                List<User> result = new ArrayList<>();

                try{
                    String URL = String.format(Config.API_USER_GET_ALL_EXCEPT_ME, String.valueOf(user.getId()));
                    JSONObject json = new Requestor(URL).get();
                    JSONArray userArray = null;
                    if (json != null) {
                        if (!json.isNull("data")) {
                            userArray = json.getJSONArray("data");
                        }
                    }

                    if (userArray != null) {
                        for(int i = 0 ; i < userArray.length(); i++){
                            JSONObject userObj = userArray.getJSONObject(i);

                            if (userObj != null) {
                                result.add(new User(userObj));
                            }
                        }
                    }
                }
                catch(Exception ex) {
                    Log.d(TAG, "Fail : " + ex.getMessage());
                }

                return result;
            }

            @Override
            protected void onPostExecute(List<User> result) {
                hideProgressDialog();

                if (result == null) {
                    Toast.makeText(getApplicationContext(), "Aucun utilisateur...", Toast.LENGTH_SHORT).show();
                } else {
                    DialogListUserAdapter adapter = new DialogListUserAdapter(getApplicationContext(), result);
                    afficherDialogListUser(adapter);
                }
            }
        }.execute();
    }

    private void afficherDialogListUser(final DialogListUserAdapter adapter) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Choisir un utilisateur");
        builder.setCancelable(false);
        builder.setNegativeButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                dialog.dismiss();
            }
        });
        builder.setAdapter(adapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        User u = adapter.getItem(position);

                        Intent conversationIntent = new Intent(getBaseContext(), ConversationActivity.class);
                        conversationIntent.putExtra("user", user);
                        conversationIntent.putExtra("distantUser", u);
                        startActivity(conversationIntent);

                        overridePendingTransition(R.anim.in_right_to_left, R.anim.out_right_to_left);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // Revoke
                    }
                });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void loadListView(List<Conversation> conversationList) {
        adapter = new ListViewConversationAdapter(getApplicationContext(), conversationList);
        convListView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent conversationIntent = new Intent(getBaseContext(), ConversationActivity.class);
        conversationIntent.putExtra("user", user);
        conversationIntent.putExtra("distantUser", adapter.getItem(position).getUser());
        startActivity(conversationIntent);

        overridePendingTransition(R.anim.in_right_to_left, R.anim.out_right_to_left);
    }

    private class getConvTask extends AsyncTask<Void, Void, List<Conversation>> {

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected List<Conversation> doInBackground(Void... u) {

            List<Conversation> result = new ArrayList<>();

            try{
                String URL = String.format(Config.API_CONVERSATIONS_GET, String.valueOf(user.getId()));

                JSONObject json = new Requestor(URL).get();
                JSONArray conv_array = null;
                if (json != null) {
                    if (!json.isNull("data")) {
                        conv_array = json.getJSONArray("data");
                    }
                }
                
                if (conv_array != null) {
                    for(int i = 0 ; i < conv_array.length(); i++){
                        JSONObject convObj = conv_array.getJSONObject(i);

                        if (convObj != null) {
                            result.add(new Conversation(convObj));
                        }
                    }
                }
            }
            catch(Exception ex) {
                Log.d(TAG, "Fail : " + ex.getMessage());
            }

            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(List<Conversation> result) {
            loadListView(result);
            hideProgressDialog();
        }
    }
}
