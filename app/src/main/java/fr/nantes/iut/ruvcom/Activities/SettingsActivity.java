package fr.nantes.iut.ruvcom.Activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.Collection;

import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.Utils.Config;

/**
 * Created by ughostephan on 07/02/2016.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private Preference prefCache;
    private Preference prefVersion;
    private Preference prefDownloadLastApk;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        addPreferencesFromResource(R.xml.settings);

        String versionName = "";

        try {
            Context c = getApplicationContext();
            versionName = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(e, "message");
        }

        prefCache = findPreference(getString(R.string.pref_cache));
        prefVersion = findPreference(getString(R.string.pref_version));
        prefDownloadLastApk = findPreference(getString(R.string.pref_download_last_apk));

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
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    protected int sizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else {
            return data.getByteCount();
        }
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
            cacheSize += sizeOf(memoryCache.get(key));
        }

        return humanReadableByteCount(cacheSize, true);
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
