package fr.nantes.iut.ruvcom.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.danielnilsson9.colorpickerview.dialog.ColorPickerDialogFragment;
import com.github.danielnilsson9.colorpickerview.preference.ColorPreference;
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.Collection;

import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.Utils.Config;
import fr.nantes.iut.ruvcom.Utils.NamedPreferences;
import fr.nantes.iut.ruvcom.Utils.RUVComUtils;

/**
 * Created by ughostephan on 07/02/2016.
 */
public class SettingsActivity extends RUVBaseActivity
        implements ColorPickerDialogFragment.ColorPickerDialogListener,
        ColorPickerView.OnColorChangedListener{

    private SettingsFragment settingsFragment;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        settingsFragment = new SettingsFragment();

        // Display the fragment as the main content.
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.settingsFragment, settingsFragment)
                    .commit();
        }

        applyColor();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.in_left_to_right, R.anim.out_left_to_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                //overridePendingTransition(R.anim.in_left_to_right, R.anim.out_left_to_right);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ValidFragment")
    public class SettingsFragment extends PreferenceFragment {

        public Preference prefCache;
        public Preference prefVersion;
        public Preference prefResetColor;
        public Preference prefDownloadLastApk;
        public ColorPreference prefGeneralColor;

        public SettingsFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            String versionName = "";

            try {
                Context c = getActivity().getApplicationContext();
                versionName = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Logger.e(e, "message");
            }

            prefCache = findPreference(getString(R.string.pref_cache));
            prefVersion = findPreference(getString(R.string.pref_version));
            prefResetColor = findPreference(getString(R.string.pref_reset_color));
            prefDownloadLastApk = findPreference(getString(R.string.pref_download_last_apk));
            prefGeneralColor = (ColorPreference) findPreference(NamedPreferences.GENERAL_COLOR);

            int savedColor = preferences.getInt(NamedPreferences.GENERAL_COLOR, baseColor);
            prefGeneralColor.saveValue(savedColor);
            prefVersion.setSummary(versionName);
            prefCache.setSummary(getCache());

            prefCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    cleanCache();
                    return false;
                }
            });

            prefDownloadLastApk.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(Config.URL_DOWNLOAD_LAST_APK));
                    startActivity(i);
                    return false;
                }
            });

            prefResetColor.setOnPreferenceClickListener((new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference p) {
                    prefGeneralColor.saveValue(baseColor);
                    applyColor();

                    return false;
                }
            }));

            prefGeneralColor.setOnShowDialogListener(new ColorPreference.OnShowDialogListener() {

                @Override
                public void onShowColorPickerDialog(String title, int currentColor) {

                    // Preference was clicked, we need to show the dialog.
                    ColorPickerDialogFragment dialog = ColorPickerDialogFragment
                            .newInstance(1, getString(R.string.pref_title_customisation_dialog), null, currentColor, false);

                    // PLEASE READ!
                    // Show the dialog, the result from the dialog
                    // will end up in the parent activity since
                    // there really isn't any good way for fragments
                    // to communicate with each other. The recommended
                    // ways is for them to communicate through their
                    // host activity, thats what we will do.
                    // In our case, we must then make sure that MainActivity
                    // implements ColorPickerDialogListener because that
                    // is expected by ColorPickerDialogFragment.
                    //
                    // We also make this fragment implement ColorPickerDialogListener
                    // and when we receive the result in the activity's
                    // ColorPickerDialogListener when just forward them
                    // to this fragment instead.
                    dialog.show(getFragmentManager(), "pre_dialog");
                }
            });
        }

        private void cleanCache() {
            ImageLoader imageLoader = ImageLoader.getInstance();

            imageLoader.clearDiskCache();
            imageLoader.clearMemoryCache();

            prefCache.setSummary(getCache());
        }

        private String getCache() {
            long cacheSize = 0;
            ImageLoader imageLoader = ImageLoader.getInstance();

            File[] diskFiles = imageLoader.getDiskCache().getDirectory().listFiles();

            for (File f : diskFiles) {
                cacheSize += f.length();
            }

            Collection<String> memoryFiles = imageLoader.getMemoryCache().keys();
            MemoryCache memoryCache = imageLoader.getMemoryCache();

            for (String key : memoryFiles) {
                cacheSize += RUVComUtils.sizeOf(memoryCache.get(key));
            }

            return RUVComUtils.humanReadableByteCount(cacheSize, true);
        }
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        //Toast.makeText(SettingsActivity.this, "Selected Color: " + RUVComUtils.colorToHexString(color), Toast.LENGTH_SHORT).show();
        settingsFragment.prefGeneralColor.saveValue(color);

        // Apply the new color
        applyColor();
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    @Override
    public void onColorChanged(int newColor) {
        //Toast.makeText(SettingsActivity.this, "Changed Color: " + RUVComUtils.colorToHexString(newColor), Toast.LENGTH_SHORT).show();
    }
}
