package it.feio.android.omninotes.helpers;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {

    private LocationHelper() {
        // Hides the public constructor
    }

    /**
     * Converts latitude and longitude to a readable address string
     */
    public static String getAddressFromCoordinates(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown Location";
    }

    /**
     * Converts an address string to latitude and longitude coordinates
     */
    public static Location getCoordinatesFromAddress(Context context, String addressString) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                Location location = new Location("");
                location.setLatitude(address.getLatitude());
                location.setLongitude(address.getLongitude());
                return location;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the distance between two coordinates in meters
     */
    public static float calculateDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return results[0];
    }

    /**
     * Formats latitude and longitude into a readable string with specified decimal precision
     */
    public static String formatCoordinates(double latitude, double longitude, int decimalPlaces) {
        String formatString = "%." + decimalPlaces + "f";
        return String.format("Lat: " + formatString + ", Lon: " + formatString, latitude, longitude);
    }

    /**
     * Formats a location into a string with both the address and coordinates
     */
    public static String formatLocationDetails(Context context, double latitude, double longitude) {
        String address = getAddressFromCoordinates(context, latitude, longitude);
        return address + " (" + formatCoordinates(latitude, longitude, 4) + ")";
    }

    /**
     * Converts meters into a more human-readable format (e.g., "1.2 km" or "500 m")
     */
    public static String formatDistance(Context context, float distanceInMeters) {
        if (distanceInMeters < 1000) {
            return String.format(Locale.getDefault(), "%.0f m", distanceInMeters);
        } else {
            return String.format(Locale.getDefault(), "%.2f km", distanceInMeters / 1000);
        }
    }
}
