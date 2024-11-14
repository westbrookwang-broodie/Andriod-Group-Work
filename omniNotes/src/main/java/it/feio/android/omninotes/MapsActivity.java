package it.feio.android.omninotes;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import it.feio.android.omninotes.databinding.ActivityMapsBinding;
import it.feio.android.omninotes.models.listeners.OnLocationPickedListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        float zoomLevel = 15.0f;

        // 设置初始位置
        LatLng initialPosition = new LatLng(-34, 151);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, zoomLevel));

        Log.d("MapZoom", "Initial Position: " + initialPosition);
        Log.d("MapZoom", "Zoom Level: " + zoomLevel);

        // 添加点击监听器
        mMap.setOnMapClickListener(latLng -> {
            // 移动并更新标记
            if (marker != null) marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title("选择的位置"));
        });
    }

}