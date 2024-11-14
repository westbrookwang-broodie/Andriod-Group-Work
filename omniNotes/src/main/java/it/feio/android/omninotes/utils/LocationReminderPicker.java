package it.feio.android.omninotes.utils;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import it.feio.android.omninotes.R;

public class LocationReminderPicker extends DialogFragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private LatLng selectedLocation;
    private LocationSelectListener listener;

    public interface LocationSelectListener {
        void onLocationSelected(LatLng location);
    }

    public LocationReminderPicker(LocationSelectListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setContentView(R.layout.fragment_location_picker); // Custom layout for map
        mapView = dialog.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return dialog;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Set initial map position and zoom level
        LatLng initialLocation = new LatLng(-34, 151); // Example coordinates, use user location if available
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15));

        // Add marker and allow user to select location by tapping
        googleMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
        });
    }

    // Handle location confirmation (e.g., a confirm button in dialog)
    private void confirmLocation() {
        if (selectedLocation != null && listener != null) {
            listener.onLocationSelected(selectedLocation);
            dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}

