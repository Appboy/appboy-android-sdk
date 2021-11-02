package com.appboy.sample.activity;

import static com.appboy.sample.R.id.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.appboy.sample.R;
import com.braze.models.BrazeGeofence;
import com.braze.support.BrazeLogger;
import com.braze.support.StringUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeofencesMapActivity extends AppCompatActivity implements OnMapReadyCallback {
  private static final String TAG = BrazeLogger.getBrazeLogTag(GeofencesMapActivity.class);
  private static final String REGISTERED_GEOFENCE_SHARED_PREFS_LOCATION = "com.appboy.support.geofences";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.geofences_map);
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(map);
    mapFragment.getMapAsync(this);
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {

    // Note that this is for testing purposes only.  This storage location and format are not a supported API.
    SharedPreferences registeredGeofencePrefs = getApplicationContext()
        .getSharedPreferences(REGISTERED_GEOFENCE_SHARED_PREFS_LOCATION, Context.MODE_PRIVATE);
    List<BrazeGeofence> registeredGeofences = retrieveBrazeGeofencesFromLocalStorage(registeredGeofencePrefs);

    int color = Color.BLUE;
    if (registeredGeofences.size() > 0) {
      for (BrazeGeofence registeredGeofence : registeredGeofences) {
        googleMap.addCircle(new CircleOptions()
            .center(new LatLng(registeredGeofence.getLatitude(), registeredGeofence.getLongitude()))
            .radius(registeredGeofence.getRadiusMeters())
            .strokeColor(Color.RED)
            .fillColor(Color.argb((int) Math.round(Color.alpha(color) * .20), Color.red(color), Color.green(color), Color.blue(color))));
        googleMap.addMarker(new MarkerOptions()
            .position(new LatLng(registeredGeofence.getLatitude(), registeredGeofence.getLongitude()))
            .title("Appboy Geofence")
            .snippet(registeredGeofence.getLatitude() + ", " + registeredGeofence.getLongitude()
                + ", radius: " + registeredGeofence.getRadiusMeters() + "m"));
      }

      BrazeGeofence firstGeofence = registeredGeofences.get(0);
      googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(firstGeofence.getLatitude(), firstGeofence.getLongitude())));
      googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), null);
    }
  }

  // Note that this is for testing purposes only.  This storage location and format are not a supported API.
  private static List<BrazeGeofence> retrieveBrazeGeofencesFromLocalStorage(SharedPreferences sharedPreferences) {
    List<BrazeGeofence> geofences = new ArrayList<>();
    Map<String, ?> storedGeofences = sharedPreferences.getAll();
    if (storedGeofences == null || storedGeofences.size() == 0) {
      BrazeLogger.d(TAG, "Did not find stored geofences.");
      return geofences;
    }
    Set<String> keys = storedGeofences.keySet();
    for (String key : keys) {
      String geofenceString = sharedPreferences.getString(key, null);
      try {
        if (StringUtils.isNullOrBlank(geofenceString)) {
          BrazeLogger.w(TAG, String.format("Received null or blank serialized "
              + " geofence string for geofence id %s from shared preferences. Not parsing.", key));
          continue;
        }
        JSONObject geofenceJson = new JSONObject(geofenceString);
        BrazeGeofence brazeGeofence = new BrazeGeofence(geofenceJson);
        geofences.add(brazeGeofence);
      } catch (JSONException e) {
        BrazeLogger.e(TAG, "Encountered Json exception while parsing stored geofence: " + geofenceString, e);
      } catch (Exception e) {
        BrazeLogger.e(TAG, "Encountered unexpected exception while parsing stored geofence: " + geofenceString, e);
      }
    }
    return geofences;
  }
}
