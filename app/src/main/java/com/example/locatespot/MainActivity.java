package com.example.locatespot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private int locationRequestCode = 1000;
    private FusedLocationProviderClient mFusedLocationClient;
    private EditText addr;
    private GoogleMap map;
    private RecyclerView recyclerView;
    private List<RetrieveLocation> alreadyAvailableAddress;
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
        alreadyAvailableAddressLookUp = new HashSet<>();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
        } else {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.maps)).getMapAsync(this);
        }
    }

    public void findAddress(View view) {
        addr = findViewById(R.id.address);
        findAddress(addr.getText().toString());
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map = googleMap;
        recyclerViewAdapter = new RecyclerViewAdapter(getApplicationContext(), alreadyAvailableAddress, map);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mFusedLocationClient.getLastLocation().addOnCompleteListener(
                new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        final Location location = task.getResult();
                        if (location == null) {
                            LocationRequest mLocationRequest = new LocationRequest();
                            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            mLocationRequest.setInterval(0);
                            mLocationRequest.setFastestInterval(0);
                            mLocationRequest.setNumUpdates(1);
                            mFusedLocationClient.requestLocationUpdates(
                                    mLocationRequest,
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

    private void setCurrentLocation(double latitude, double longitude, String loc) {
        map.clear();
        LatLng currentLocation = new LatLng(latitude, longitude);
        map.addMarker(new MarkerOptions().position(currentLocation).title(loc == null ? "Your Location" : loc));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15), 3500, null);
    }

    public void findAddress(String addressVal) {
        Geocoder gc = new Geocoder(this);
        if(gc.isPresent()){
            List<Address> availableAddress = new ArrayList<>();

            try {
                availableAddress = gc.getFromLocationName(addressVal, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (availableAddress.size() > 0) {
                recyclerView.setVisibility(View.VISIBLE);
                Address address = availableAddress.get(0);
                recyclerView.setAlpha(1);
                RetrieveLocation r = new RetrieveLocation(address, addressVal);
                if (!alreadyAvailableAddressLookUp.contains(addressVal)) {
                    alreadyAvailableAddress.add(r);
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

}