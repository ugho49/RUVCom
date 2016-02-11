package fr.nantes.iut.ruvcom.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import fr.nantes.iut.ruvcom.Bean.User;

/**
 * Created by ughostephan on 11/02/2016.
 */
public class CacheUtils {

    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor prefsEditor;
    private Gson gson;

    private final String CURRENT_USER_CACHE = "savedUserInCache";

    public CacheUtils(Context c) {
        this.context = c;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        prefsEditor = preferences.edit();
        gson = new Gson();
    }

    public void saveCurrentUser(User user) {
        prefsEditor.putString(CURRENT_USER_CACHE, gson.toJson(user));
        prefsEditor.commit();
    }

    public void removeCurrentUser() {
        if(isSavedUser()) {
            prefsEditor.remove(CURRENT_USER_CACHE);
            prefsEditor.commit();
        }
    }

    public User getCurrentUser() {
        final String json = preferences.getString(CURRENT_USER_CACHE, "");
        return gson.fromJson(json, User.class);
    }

    public Boolean isSavedUser() {
        final String json = preferences.getString(CURRENT_USER_CACHE, "");
        return (!"".equals(json));
    }
}
