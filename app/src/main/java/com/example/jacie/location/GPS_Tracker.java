package com.example.jacie.location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import com.example.jacie.R;

public class GPS_Tracker extends Service implements LocationListener {

    private static String TAG = GPS_Tracker.class.getName();
    private final Context mContext;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean isGPSTrackingEnabled = false;

    Location location;
    public double latitude;
    public double longitude;
    int geocoderMaxResults = 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    protected LocationManager locationManager;
    private String provider_info;
    public GPS_Tracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    @SuppressLint("MissingPermission")
    public void getLocation() {

        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPSEnabled) {
                this.isGPSTrackingEnabled = true;

                Log.d(TAG, "Application use GPS Service");

                provider_info = LocationManager.GPS_PROVIDER;

            } else if (isNetworkEnabled) { // Try to get location if you Network Service is enabled
                this.isGPSTrackingEnabled = true;

                Log.d(TAG, "Application use Network State to get GPS coordinates");

                provider_info = LocationManager.NETWORK_PROVIDER;

            }
            if (!provider_info.isEmpty()) {
                locationManager.requestLocationUpdates(
                        provider_info,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this
                );

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(provider_info);
                    updateGPSCoordinates();
                }
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            Log.e(TAG, "Impossible to connect to LocationManager", e);
        }
    }

    public void updateGPSCoordinates() {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    public boolean getIsGPSTrackingEnabled() {

        return this.isGPSTrackingEnabled;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPS_Tracker.this);
        }
    }

    /**
     * Function to show settings alert dialog
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        //Setting Dialog Title
        alertDialog.setTitle(R.string.GPSAlertDialogTitle);

        //Setting Dialog Message
        alertDialog.setMessage(R.string.GPSAlertDialogMessage);

        //On Pressing Setting button
        alertDialog.setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        //On pressing cancel button
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    /**
     * Get list of address by latitude and longitude
     * @return null or List<Address>
     */
    public List<Address> getGeocoderAddress(Context context) {
        if (location != null) {

            Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, this.geocoderMaxResults);

                return addresses;
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e(TAG, "Impossible to connect to Geocoder", e);
            }
        }

        return null;
    }

    public String getAddressLine(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);

            return addressLine;
        } else {
            return null;
        }
    }

    public String getLocality(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String locality = address.getLocality();

            return locality;
        }
        else {
            return null;
        }
    }

    public String getPostalCode(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String postalCode = address.getPostalCode();

            return postalCode;
        } else {
            return null;
        }
    }

    public String getCountryName(Context context) {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String countryName = address.getCountryName();

            return countryName;
        } else {
            return null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
