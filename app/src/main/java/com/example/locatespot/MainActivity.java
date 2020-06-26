package com.example.locatespot;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

  private int permissionCode = 500;
  private FusedLocationProviderClient locationClient;
  private EditText addr;
  private GoogleMap map;
  private static final String TAG = MainActivity.class.getSimpleName();
  private RecyclerView recyclerView;
  private List<RetrievedLocation> alreadyAvailableAddress;
  private Set<String> alreadyAvailableAddressLookUp;
  private RecyclerViewAdapter recyclerViewAdapter;
  private AlertDialog invalidAddressAlert;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    recyclerView = findViewById(R.id.looked_up_address);
    recyclerView.setVisibility(View.GONE);
    alreadyAvailableAddress = new ArrayList<>();
    Log.d(TAG, "In OnCreate");
    alreadyAvailableAddressLookUp = new HashSet<>();



    if (isUserPermissionAvailable()) {
      LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
          || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps)).getMapAsync(this);

      } else {
        Intent providerSetting = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(providerSetting);
      }

    } else {
      ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION}, permissionCode);
    }

  }

  /**
   * Checks for the user permission for accessing location
   * @return true if permission is granted otherwise false
   */

  private boolean isUserPermissionAvailable() {
    if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
        (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
      return false;
    }
    return true;
  }

  /**
   * Request for user permission
   * @param requestCode Request Code
   * @param permissions Permissions needed
   * @param grantResults Grant results
   */

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (permissionCode == requestCode && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      locationClient = LocationServices.getFusedLocationProviderClient(this);
      ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps)).getMapAsync(this);
    } else {
      finish();
    }

  }

  /**
   * Get the address of the location
   * @param view Address view
   */

  public void findAddress(View view) {
    addr = findViewById(R.id.address);
    findAddress(addr.getText().toString());
  }

  @Override
  public void onMapReady(final GoogleMap googleMap) {
    map = googleMap;
    map.setMyLocationEnabled(true);
    UiSettings uiToolFeatures = map.getUiSettings();
    uiToolFeatures.setZoomControlsEnabled(true);
    uiToolFeatures.setMapToolbarEnabled(true);
    uiToolFeatures.setZoomGesturesEnabled(true);
    uiToolFeatures.setTiltGesturesEnabled(true);
    uiToolFeatures.setRotateGesturesEnabled(true);
    try {
      boolean result = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_design));
      if (!result) {
        Log.e(TAG, "Style parsing failed.");
      }
    } catch (Resources.NotFoundException e) {
      Log.e(TAG, "Can't find style. Error: ", e);
    }
    recyclerViewAdapter = new RecyclerViewAdapter(getApplicationContext(), alreadyAvailableAddress, map);
    recyclerView.setAdapter(recyclerViewAdapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    // Get the current location of the user
    locationClient.getLastLocation().addOnCompleteListener(
        new OnCompleteListener<Location>() {
          @Override
          public void onComplete(@NonNull Task<Location> task) {
            final Location location = task.getResult();
            if (location == null) {
              LocationRequest locRequest = new LocationRequest();
              locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
              locRequest.setInterval(5);
              locRequest.setFastestInterval(0);
              locRequest.setNumUpdates(1);
              locationClient.requestLocationUpdates(
                  locRequest,
                  new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                      Location loc = locationResult.getLastLocation();
                      setCurrentLocation(loc.getLatitude(), loc.getLongitude(), null);
                    }
                  },
                  Looper.myLooper()
              );
            } else {
              setCurrentLocation(location.getLatitude(), location.getLongitude(), null);
            }
          }
        }
    );
  }

  /**
   * Show the location in marker
   * @param latitude latitude
   * @param longitude longitude
   * @param loc Location
   */

  private void setCurrentLocation(double latitude, double longitude, String loc) {
    map.clear();
    LatLng currentLocation = new LatLng(latitude, longitude);
    map.addMarker(new MarkerOptions().position(currentLocation).title(loc == null ? "Your Location" : loc));
    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16), 3500, null);
  }

  /**
   * With provided address, it finds the latitude, longitude ({@link Address}) of the location
   * @param addressVal
   */

  private void findAddress(String addressVal) {
    Geocoder gc = new Geocoder(this);
    if (Geocoder.isPresent()){
      List<Address> availableAddress;
      try {
        availableAddress = gc.getFromLocationName(addressVal, 1);
      } catch (IOException e) {
        Log.e(getClass().getCanonicalName(), e.getMessage());
        return;
      }
      if (!availableAddress.isEmpty()) {
        recyclerView.setVisibility(View.VISIBLE);
        Address address = availableAddress.get(0);
        recyclerView.setAlpha(1);
        RetrievedLocation r = new RetrievedLocation(address, getAddressLines(address));
        if (!alreadyAvailableAddressLookUp.contains(addressVal)) {
          //Always add recent one first
          alreadyAvailableAddress.add(0, r);
          alreadyAvailableAddressLookUp.add(addressVal);
          recyclerViewAdapter.notifyDataSetChanged();
        }
        setCurrentLocation(address.getLatitude(), address.getLongitude(), addressVal);
        addr.setText("");
      } else {
        invalidAddressAlert = new AlertDialog.Builder(this).create();
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.alert_box_layout,null);
        Button alertButton = view.findViewById(R.id.alert_try_agin);
        alertButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            addr.setText("");
            map.clear();
            invalidAddressAlert.dismiss();
          }
        });
        invalidAddressAlert.setView(view);
        invalidAddressAlert.show();
      }
    }
  }

  /**
   * Retrieve the address from the {@link Address}
   * @param address
   * @return address of the location
   */
  private String getAddressLines(Address address) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
      sb.append(address.getAddressLine(i));
    }
    return sb.toString();
  }

}
