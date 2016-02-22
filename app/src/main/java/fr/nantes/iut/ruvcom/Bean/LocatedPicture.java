package fr.nantes.iut.ruvcom.Bean;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ughostephan on 22/02/2016.
 */
public class LocatedPicture extends BaseBean {
    private String displayName;
    private Photo photo;

    public LocatedPicture() {
    }

    public LocatedPicture(JSONObject data) {
        if (data != null) {
            try {
                this.displayName = data.getString("displayName");

                if(!data.isNull("photo")) {
                    this.photo = new Photo(data.getJSONObject("photo"));
                } else {
                    this.photo = null;
                }
            } catch (JSONException e) {
                Log.e("MESSAGE", e.getMessage());
            }
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }
}
