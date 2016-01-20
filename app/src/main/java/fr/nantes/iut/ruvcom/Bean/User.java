package fr.nantes.iut.ruvcom.Bean;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ughostephan on 19/01/2016.
 */
public class User extends BaseBean {

    private int id;
    private String googleId;
    private String displayName;
    private String email;
    private String imageUrl;
    private String coverImageUrl;

    public User() {
        // Default constructor
    }

    public User(JSONObject data) {
        if (data != null) {
            try {
                this.id = data.getInt("id");
                this.googleId = data.getString("googleID");
                this.displayName = data.getString("displayName");
                this.email = data.getString("email");
                this.imageUrl = data.getString("imageUrl");
                this.coverImageUrl = data.getString("coverImageUrl");
            } catch (JSONException e) {
                Log.e("USER", e.getMessage());
            }
        }
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
