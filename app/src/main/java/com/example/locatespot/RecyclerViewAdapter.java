package com.example.locatespot;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Adapter for recycler view to show address in the recycler view
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

  private Context context;
  private List<RetrievedLocation> locationAddress;
  private GoogleMap map;

  RecyclerViewAdapter(Context mContext, List<RetrievedLocation> addressVal, GoogleMap googleMap) {
    context = mContext;
    locationAddress = addressVal;
    map = googleMap;
  }

  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.address_layout, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
    holder.addressAlreadyLookedUp.setText(locationAddress.get(position).completeAddress);
    holder.parentLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Address a = locationAddress.get(position).address;
        map.clear();
        LatLng currentLocation = new LatLng(a.getLatitude(), a.getLongitude());
        map.addMarker(new MarkerOptions().position(currentLocation).title(locationAddress.get(position).completeAddress));
        map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
      }
    });
  }

  @Override
  public int getItemCount() {
    return locationAddress.size();
  }

  /**
   * View holder for holding the recycler view items
   */
  static class ViewHolder extends RecyclerView.ViewHolder {

    private TextView addressAlreadyLookedUp;
    private LinearLayout parentLayout;

    ViewHolder(View itemView) {
      super(itemView);
      addressAlreadyLookedUp = itemView.findViewById(R.id.address_already_looked_up);
      parentLayout = itemView.findViewById(R.id.parent_view);
    }
  }
}

