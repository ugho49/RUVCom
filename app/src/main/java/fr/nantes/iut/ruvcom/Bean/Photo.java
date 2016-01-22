package fr.nantes.iut.ruvcom.Bean;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ughostephan on 19/01/2016.
 */
public class Photo extends BaseBean {
    private int id;
    private String url;
    private String filesize;
    private String latitude;
    private String longitude;

    public Photo(JSONObject data) {
        if (data != null) {
            try {
                this.id = data.getInt("id");
                this.url = data.getString("url");
                this.filesize = data.getString("filesize");

                if(!data.isNull("geoLat")) {
                    this.latitude = data.getString("geoLat");
                } else {
                    this.latitude = "";
                }

                if(!data.isNull("geoLong")) {
                    this.longitude = data.getString("geoLong");
                } else {
                    this.longitude = "";
                }
            } catch (JSONException e) {
                Log.e("PHOTO", e.getMessage());
            }
        }
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilesize() {
        return filesize;
    }

    public void setFilesize(String filesize) {
        this.filesize = filesize;
    }
}
