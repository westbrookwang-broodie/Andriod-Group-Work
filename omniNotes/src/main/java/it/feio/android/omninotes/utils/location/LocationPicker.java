package it.feio.android.omninotes.utils.location;

import android.location.Location;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

import it.feio.android.omninotes.models.listeners.OnLocationPickedListener;

public class LocationPicker {

    private FragmentActivity mActivity;
    private OnLocationPickedListener mOnLocationPickedListener;

    public LocationPicker(FragmentActivity mActivity, OnLocationPickedListener mOnLocationPickedListener) {
        this.mActivity = mActivity;
        this.mOnLocationPickedListener = mOnLocationPickedListener;
    }

    public void pick(Location location) {
        LocationPickerFragment locationPickerFragment = new LocationPickerFragment(mActivity, mOnLocationPickedListener, location);
        locationPickerFragment.setCallback(new LocationPickerFragment.Callback() {
            @Override
            public void onCancelled() {
                // Handle cancellation if needed
            }

            @Override
            public void onLocationSet(double latitude, double longitude) {
                // Pass the selected location to the listener
                mOnLocationPickedListener.onLocationPicked(latitude, longitude);
            }
        });

        Bundle bundle = new Bundle();
        locationPickerFragment.setArguments(bundle);
        locationPickerFragment.show(mActivity.getSupportFragmentManager(), "Location_Picker");
    }
}
