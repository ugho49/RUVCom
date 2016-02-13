package fr.nantes.iut.ruvcom.Utils;

import com.orhanobut.logger.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;

import okhttp3.Headers;
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
    private static HttpClient httpclient;

    public Requestor(String URL) {
        httpclient = getNewHttpClient();
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
        } catch (IOException e) {
            //Log.e("Requestor_HTTPPOST", e.getMessage());
            Logger.e(e, "message");
        }

        return response;
    }

    public JSONObject post(RequestBody body) {

        JSONObject object = null;
        final OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                Logger.i("Unexpected code " + response);
            }

            object = new JSONObject(response.body().string());
        } catch (JSONException | IOException e) {
            Logger.e(e, "message");
        }

        return object;
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
        } catch (JSONException | IOException e) {
            //Log.e("Requestor_Response", e.getMessage());
            Logger.e(e, "message");
        }

        return jsonresult;
    }

    private HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    public static Headers getheader(String name) {
        return Headers.of("Content-Disposition", "form-data; name=\"" + name + "\"");
    }
}
