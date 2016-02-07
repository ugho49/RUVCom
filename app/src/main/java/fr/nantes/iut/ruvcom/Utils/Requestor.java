package fr.nantes.iut.ruvcom.Utils;

import android.util.Log;

import com.orhanobut.logger.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by ughostephan on 20/01/2016.
 */
public class Requestor {

    private String URL;
    private static HttpClient httpclient;

    public Requestor(String URL) {
        if (httpclient == null) {
            httpclient = new DefaultHttpClient();
        }
        this.URL = URL;
    }

    public JSONObject post(ArrayList<NameValuePair> params) {

        HttpPost httppost = new HttpPost(URL);
        JSONObject response = null;

        try {
            if (!params.isEmpty()) {
                httppost.setEntity(new UrlEncodedFormEntity(params));
            }

            response = returnResponse(httpclient.execute(httppost));
        } catch (UnsupportedEncodingException e) {
            //Log.e("Requestor_HTTPPOST", e.getMessage());
            Logger.e(e, "message");
        } catch (IOException e) {
            //Log.e("Requestor_HTTPPOST", e.getMessage());
            Logger.e(e, "message");
        }

        return response;
    }

    public JSONObject post(AndroidMultiPartEntity entity) {

        HttpPost httppost = new HttpPost(URL);
        JSONObject response = null;

        try {
            if (entity != null) {
                httppost.setEntity(entity);
            }

            response = returnResponse(httpclient.execute(httppost));
        } catch (UnsupportedEncodingException e) {
            //Log.e("Requestor_HTTPPOST", e.getMessage());
            Logger.e(e, "message");
        } catch (IOException e) {
            //Log.e("Requestor_HTTPPOST", e.getMessage());
            Logger.e(e, "message");
        }

        return response;
    }

    public JSONObject get() {
        HttpGet httpget = new HttpGet(URL);

        JSONObject response = null;

        try {
            response = returnResponse(httpclient.execute(httpget));
        } catch (IOException e) {
            //Log.e("Requestor_HTTPGET", e.getMessage());
            Logger.e(e, "message");
        }

        return response;
    }

    private JSONObject returnResponse(HttpResponse httpResponse) {
        JSONObject jsonresult = null;

        try {
            String result = EntityUtils.toString(httpResponse.getEntity());
            jsonresult = new JSONObject(result);
        } catch (JSONException e) {
            //Log.e("Requestor_Response", e.getMessage());
            Logger.e(e, "message");
        } catch (IOException e) {
            //Log.e("Requestor_Response", e.getMessage());
            Logger.e(e, "message");
        }

        return jsonresult;
    }
}
