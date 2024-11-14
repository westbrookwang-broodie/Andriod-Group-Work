package it.feio.android.omninotes.utils.location;

import android.app.Dialog;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.listeners.OnLocationPickedListener;

public class LocationPickerFragment extends DialogFragment implements OnMapReadyCallback {

     GoogleMap gMap;
     Marker selectedMarker;
     Callback mCallback;
     FrameLayout frameLayout;
     MapView mapView;
     OnLocationPickedListener onLocationPickedListener;
     FragmentActivity mActivity;
     OnLocationPickedListener mOnLocationPickedListener;
     Location location;
     View view;

     public MapView getMapView(){
         return this.mapView;
     }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public LocationPickerFragment(FragmentActivity mActivity, OnLocationPickedListener mOnLocationPickedListener, Location location) {
        this.mActivity = mActivity;
        this.mOnLocationPickedListener = mOnLocationPickedListener;
        this.location = location;
    }
    public LocationPickerFragment() {
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
//    }

//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
//
//        // 加载地图布局
//        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
//        getChildFragmentManager().beginTransaction().replace(android.R.id.content, mapFragment).commit();
//        mapFragment.getMapAsync(this);
//
//        return dialog;
//    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for the fragment

        view =inflater
                .inflate(R.layout.fragment_location_picker, container);

        mapView = (MapView) view.findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState != null ? savedInstanceState : new Bundle());


        // Set up the map asynchronously
        if(mapView!=null){
            mapView.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        // Set a default location (Sydney in this case)
        LatLng defaultLocation = new LatLng(location.getLatitude(), location.getLongitude());
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15.0F));

        // Set up the map click listener to add a marker
        gMap.setOnMapClickListener(latLng -> {
            if (selectedMarker != null) selectedMarker.remove();
            selectedMarker = gMap.addMarker(new MarkerOptions().position(latLng).title("选择的位置"));

            // 选择位置后，通知调用者
            if (onLocationPickedListener != null) {
                onLocationPickedListener.onLocationPicked(latLng.latitude, latLng.longitude);
            }
            Button button = view.findViewById(R.id.btnConfirm);
            button.setVisibility(View.VISIBLE);
            // 设置按钮点击事件
            button.setOnClickListener(v -> {
                if (selectedMarker != null) {
                    // 获取选中的经纬度
                    double latitude = selectedMarker.getPosition().latitude;
                    double longitude = selectedMarker.getPosition().longitude;

                    // 通知主活动选中的位置
                    if (mCallback != null) {
                        mCallback.onLocationSet(latitude, longitude);
                    }

                    // 关闭对话框
                    dismiss();
                }
            });

        });
    }


    public void setMapLocationListener(OnLocationPickedListener listener) {
        onLocationPickedListener = listener;
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

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public interface Callback {
        void onCancelled();
        void onLocationSet(double latitude, double longitude);
    }
}
