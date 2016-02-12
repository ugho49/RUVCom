package fr.nantes.iut.ruvcom.Activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;

import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.RUVComApplication;
import fr.nantes.iut.ruvcom.Utils.NamedPreferences;
import fr.nantes.iut.ruvcom.Utils.RUVComUtils;

/**
 * Created by ughostephan on 22/01/2016.
 */
public abstract class RUVBaseActivity extends AppCompatActivity {

    protected Window window;
    protected Toolbar toolbar;
    protected FloatingActionButton fab;
    protected NotificationManager notificationManager;
    protected final static int baseColor = Color.parseColor("#37474F");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        window = getWindow();
        // Disable notification if exist
         notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        notificationManager.cancelAll();

        if(!SignInActivity.class.getSimpleName().equals(RUVComApplication.activityRunningName)) {
            applyColor();
        }
    }

    private void applyTheme(int color) {
        if (toolbar != null) {
            toolbar.setBackgroundColor(color);
        }

        if (fab != null) {
            fab.setBackgroundTintList(ColorStateList.valueOf(color));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // finally change the color
            window.setStatusBarColor(RUVComUtils.darkerColor(color));
            window.setNavigationBarColor(color);
        }
    }

    protected void applyColor() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        applyTheme(preferences.getInt(NamedPreferences.GENERAL_COLOR, baseColor));
    }

    protected void applyColorActivitySignIn() {
        applyTheme(getResources().getColor(R.color.blue_grey_700));
    }
}
