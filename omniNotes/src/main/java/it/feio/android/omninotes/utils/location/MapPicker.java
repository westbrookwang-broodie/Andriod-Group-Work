package it.feio.android.omninotes.utils.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import it.feio.android.omninotes.R;

public class MapPicker extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng selectedLocation;

//    public MapPicker(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_location_picker);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // 设置地图的起始位置
        LatLng defaultLocation = new LatLng(-34.0, 151.0); // 这里可以设置为默认位置
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));

        // 单击地图设置标记
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            mMap.clear(); // 清除之前的标记
            mMap.addMarker(new MarkerOptions().position(selectedLocation).title("选择的地点"));
        });
    }

    public LatLng getSelectedLocation() {
        return selectedLocation;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}