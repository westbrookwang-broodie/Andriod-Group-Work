package com.example.map;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapDialogFragment extends DialogFragment implements OnMapReadyCallback {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // 加载地图布局
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(android.R.id.content, mapFragment).commit();
        mapFragment.getMapAsync(this);

        return dialog;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // 设置初始位置
        LatLng location = new LatLng(-34, 151);
        googleMap.addMarker(new MarkerOptions().position(location).title("Marker"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
    }
}
