package fr.nantes.iut.ruvcom.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import fr.nantes.iut.ruvcom.Bean.LocatedPicture;
import fr.nantes.iut.ruvcom.Bean.User;
import fr.nantes.iut.ruvcom.R;
import fr.nantes.iut.ruvcom.Utils.Config;
import fr.nantes.iut.ruvcom.Utils.Requestor;

public class PictureMapsActivity extends RUVBaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private User user;
    private Marker marker;
    private Hashtable<String, LocatedPicture> markers = new Hashtable<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_maps);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setSupportActionBar(toolbar);

        user = (User) getIntent().getSerializableExtra("user");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        }

        applyColor();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true); // Showing Current Location
        mMap.getUiSettings().setMyLocationButtonEnabled(true); // My Location Button
        mMap.getUiSettings().setZoomControlsEnabled(true); // Zooming Buttons
        mMap.getUiSettings().setZoomGesturesEnabled(true); // Zooming Functionality
        mMap.getUiSettings().setCompassEnabled(true); // Compass Functionality
        mMap.getUiSettings().setRotateGesturesEnabled(true); // Map Rotate Gesture

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                final LocatedPicture locatedPicture = markers.get(marker.getId());
                Intent fullScreenImageIntent = new Intent(getApplicationContext(), FullScreenImageActivity.class);
                fullScreenImageIntent.putExtra("imageUrl", locatedPicture.getPhoto().getUrl());
                fullScreenImageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(fullScreenImageIntent);
            }
        });
        loadMarkers();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMap != null) {
            loadMarkers();
        }
    }

    private void loadMarkers() {
        new AsyncTask<Void, Void, List<LocatedPicture>>() {
            @Override
            protected List<LocatedPicture> doInBackground(Void... u) {
                List<LocatedPicture> listRetour = new ArrayList<>();

                try {
                    String URL = String.format(Config.API_PICTURES_LOCATED_FOR_MAP, String.valueOf(user.getId()));
                    final JSONObject json = new Requestor(URL).get();
                    JSONArray arrayPictures = null;

                    if (json != null) {
                        if (!json.isNull("data")) {
                            arrayPictures = json.getJSONArray("data");
                        }
                    }

                    if (arrayPictures != null) {
                        for(int i = 0 ; i < arrayPictures.length(); i++){
                            JSONObject locatedPictureObj = arrayPictures.getJSONObject(i);

                            if (locatedPictureObj != null) {
                                listRetour.add(new LocatedPicture(locatedPictureObj));
                            }
                        }
                    }
                } catch (JSONException e) {
                    Logger.e("message", e);
                }

                return listRetour;
            }

            @Override
            protected void onPostExecute(List<LocatedPicture> list) {
                if (list.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Aucune photo reçu n'est localisée", Toast.LENGTH_SHORT).show();
                } else {
                    mMap.clear();
                    markers.clear();
                    for (LocatedPicture l : list) {
                        addMarker(l);
                    }
                }
            }
        }.execute();
    }

    private void addMarker(final LocatedPicture l) {
        final LatLng position = new LatLng(Double.parseDouble(l.getPhoto().getLatitude()),
                Double.parseDouble(l.getPhoto().getLongitude()));

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(position);
        markerOptions.title(l.getDisplayName());

        final Marker m = mMap.addMarker(markerOptions);
        final String id = m.getId();
        markers.put(id, l);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        // Zoom out to zoom level 10, animating with a duration of 0.5 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 500, null);
    }

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private View view;

        public CustomInfoWindowAdapter() {
            view = getLayoutInflater().inflate(R.layout.custom_marker, null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            if (PictureMapsActivity.this.marker != null && PictureMapsActivity.this.marker.isInfoWindowShown()) {
                PictureMapsActivity.this.marker.hideInfoWindow();
                PictureMapsActivity.this.marker.showInfoWindow();
            }
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            PictureMapsActivity.this.marker = marker;

            final LocatedPicture locatedPicture;

            if (marker.getId() != null && markers != null && markers.size() > 0) {
                if ( markers.get(marker.getId()) != null && markers.get(marker.getId()) != null) {
                    locatedPicture = markers.get(marker.getId());
                } else {
                    locatedPicture = new LocatedPicture();
                }
            } else {
                locatedPicture = new LocatedPicture();
            }

            ImageView image = ((ImageView) view.findViewById(R.id.custom_marker_image));
            TextView title = ((TextView) view.findViewById(R.id.custom_marker_title));

            if (locatedPicture.getPhoto() != null && locatedPicture.getPhoto().getUrl() != null) {
                ImageLoader.getInstance().displayImage(locatedPicture.getPhoto().getUrl(), image,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                super.onLoadingComplete(imageUri, view, loadedImage);
                                getInfoContents(marker);
                            }
                        });
            }

            title.setText(locatedPicture.getDisplayName());

            return view;
        }
    }
}
