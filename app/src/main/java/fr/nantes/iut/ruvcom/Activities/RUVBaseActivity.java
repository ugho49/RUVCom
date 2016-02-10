package fr.nantes.iut.ruvcom.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;

import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.Utils.RUVComUtils;

/**
 * Created by ughostephan on 22/01/2016.
 */
public abstract class RUVBaseActivity extends AppCompatActivity {

    protected Window window;
    protected Toolbar toolbar;
    protected FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        window = getWindow();
    }

    private void applyTheme(int color) {
        if (toolbar != null) {
            toolbar.setBackgroundColor(color);
        }

        if (fab != null) {
            fab.setBackgroundColor(color);
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
        applyTheme(RUVComUtils.hexStringToColor("#36648b"));
    }

    protected void applyColorActivitySignIn() {
        applyTheme(getResources().getColor(R.color.blue_grey_700));
    }
}
