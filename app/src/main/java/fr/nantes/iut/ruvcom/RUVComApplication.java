package fr.nantes.iut.ruvcom;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.orhanobut.logger.Logger;

/**
 * Created by ughostephan on 19/01/2016.
 */
public class RUVComApplication extends Application implements Application.ActivityLifecycleCallbacks {

    public static boolean applicationOnPause = true;
    public static String activityRunningName = "";
    public static Location mLastLocation;
    public static GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.init();
        initImageLoader(getApplicationContext());
        registerActivityLifecycleCallbacks(this);
    }

    public static void initImageLoader(Context context) {
        // Create global configuration and initialize ImageLoader with this configuration
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        ImageLoader.getInstance().init(config);
    }

    public static void setLastLocation(Location location) {
        mLastLocation = location;

        String info = "";
        if (location != null) {
            info += "lat:" + location.getLatitude() + ", long:" +location.getLongitude();
        }
        Logger.i("Location change : " + info);
    }

    @Override
    public void onActivityCreated(Activity arg0, Bundle arg1) {
        Log.e("", "onActivityCreated");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.e("","onActivityDestroyed ");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        applicationOnPause = true;
        activityRunningName = activity.getClass().getSimpleName();
        Log.e("","onActivityPaused "+activity.getClass());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        applicationOnPause = false;
        activityRunningName = activity.getClass().getSimpleName();
        Log.e("","onActivityResumed "+activity.getClass());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.e("","onActivitySaveInstanceState");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.e("","onActivityStarted");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.e("","onActivityStopped");
    }
}
