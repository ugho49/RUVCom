package fr.nantes.iut.ruvcom.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;

import com.orhanobut.logger.Logger;

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
    protected LocationManager locationManager;
    protected LocationListener locationListener;

    public static final int REQUEST_CODE_PERMISSION_LOCALISATION = 1234679987;

    protected final static int baseColor = Color.parseColor("#37474F");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        window = getWindow();
        // Acquire notification manager
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                RUVComApplication.setLastLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Check the permission to access to location only if API 23 or greater
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String permissions[] = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                requestPermissions(permissions, REQUEST_CODE_PERMISSION_LOCALISATION);
            } else {
                requestLocationUpdate();
            }
        } else {
            requestLocationUpdate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCALISATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // La permission est garantie
                    requestLocationUpdate();
                } else {
                    // La permission est refusée
                }
            }
        }
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

    protected void requestLocationUpdate() {
        try {
            enabledLocationIfNotEnabled();
            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            Logger.e(e, "message");
        }
    }

    private void enabledLocationIfNotEnabled() {
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {
            Logger.e(ex, "GPS LOCATION not enabled");
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {
            Logger.e(ex, "NETWORK LOCATION not enabled");
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean questionAlreadAsk = preferences.getBoolean(NamedPreferences.ASK_ACTIVATE_GPS, false);

        if(!gps_enabled && !network_enabled && !questionAlreadAsk) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(RUVBaseActivity.this);
            dialog.setTitle(getResources().getString(R.string.gps_network_not_enabled));
            dialog.setMessage(getResources().getString(R.string.open_location_settings));
            dialog.setPositiveButton(getResources().getString(R.string.activate_gps),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(NamedPreferences.ASK_ACTIVATE_GPS, true);
                    editor.commit();
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(NamedPreferences.ASK_ACTIVATE_GPS, true);
                    editor.commit();
                }
            });
            dialog.show();
        }
    }
}
