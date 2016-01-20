package fr.nantes.iut.ruvcom.Bean;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

/**
 * Created by ughostephan on 19/01/2016.
 */
public class Photo extends BaseBean {
    private int id;
    private String url;
    private String filesize;

    public Photo(JSONObject data) {
        if (data != null) {
            try {
                this.id = data.getInt("id");
                this.url = data.getString("url");
                this.filesize = data.getString("filesize");
            } catch (JSONException e) {
                Log.e("PHOTO", e.getMessage());
            }
        }
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
