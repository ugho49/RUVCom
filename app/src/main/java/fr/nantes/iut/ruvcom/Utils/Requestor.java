package fr.nantes.iut.ruvcom.Utils;

import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by ughostephan on 20/01/2016.
 */
public class Requestor {

    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    public static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");

    private String URL;
    private static OkHttpClient client;

    public Requestor(String URL) {
        client = new OkHttpClient();
        this.URL = URL;
    }

    public JSONObject post(RequestBody body) {
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        return getResponseObject(request);
    }

    public JSONObject get() {
        Request request = new Request.Builder()
                .url(URL)
                .build();

        return getResponseObject(request);
    }

    private JSONObject getResponseObject(Request request) {
        JSONObject responseObject = null;

        try {
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                Logger.i("Unexpected code " + response);
            }

            responseObject = new JSONObject(response.body().string());
        } catch (JSONException | IOException e) {
            Logger.e(e, "message");
        }

        return responseObject;
    }
}
