package mfs.ese.scotlandyard;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.io.IOException;
import java.util.EventListener;
import java.util.List;
import java.util.Locale;

/**
 * Created by Benjamin on 21.04.14.
 */
public class LocationByPlay implements LocationListener,GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    public interface IAsyncFetchListener extends EventListener {
        void onConnect();
    }
    public interface IAsyncFetchLocationListener extends EventListener {
        void onNewLocation(Location location);
    }

    IAsyncFetchListener fetchListener = null;
    IAsyncFetchLocationListener fetchLocationListener = null;

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

    private Activity mMainActivity;

    public LocationByPlay(Activity activity)
    {
        mMainActivity = activity;
        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        mLocationClient = new LocationClient(mMainActivity, this, this);
    }

    public void setConnectListener(IAsyncFetchListener listener) {
        this.fetchListener = listener;
    }

    public void setNewLocationListener(IAsyncFetchLocationListener listener) {
        this.fetchLocationListener = listener;
    }

    public void connect()
    {
        if (!mLocationClient.isConnected()){
            Log.d("std","SY: Start Tracking, Connecting");
            mLocationClient.connect();
        }
    }

    public void disconnect()
    {
        if (mLocationClient.isConnected())
            mLocationClient.disconnect();
    }
    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(mMainActivity);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, "Play available");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, mMainActivity, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                //errorFragment.setDialog(dialog);//TODO Heikel
                errorFragment.show(mMainActivity.getFragmentManager(), LocationUtils.APPTAG);
            }
            return false;
        }
    }

    public void EndTracking()
    {
        if (mLocationClient.isConnected())
        {
            stopPeriodicUpdates();
            mLocationClient.disconnect();
        }
    }

    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {
        if (servicesConnected()) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }
    }

    public void StartTracking()
    {
        this.startPeriodicUpdates();
    }
    public void PauseTracking()
    {
        this.stopPeriodicUpdates();
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
        if (servicesConnected())
            mLocationClient.removeLocationUpdates(this);
    }

    public Location getLocation() {

        // If Google Play Services is available
        if (servicesConnected()) {

            // Get the current location
            return mLocationClient.getLastLocation();
        }
        return null;
    }

    public void onLocationChanged(Location location){
        if (fetchLocationListener!= null)
            this.fetchLocationListener.onNewLocation(location);
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        startPeriodicUpdates();
        if (this.fetchListener != null)
            this.fetchListener.onConnect();
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        mMainActivity,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                mMainActivity,
                LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment

            //errorFragment.setDialog(errorDialog);//TODO Heikel

            // Show the error dialog in the DialogFragment
            errorFragment.show(mMainActivity.getFragmentManager(), LocationUtils.APPTAG);
        }
    }
    /**
         * Get a Geocoder instance, get the latitude and longitude
         * look up the address, and return it
         *
         * @params params One or more Location objects
         * @return A string containing the address of the current
         * location, or an empty string if no address can be found,
         * or an error message
         */
        public String getAddress(Location params, Context context) {
            Geocoder geocoder =
                    new Geocoder(context, Locale.getDefault());//TODO context importieren
            // Get the current location from the input parameter list
            Location loc = params;
            // Create a list to contain the result address
            List<Address> addresses = null;
            try {
                /*
                 * Return 1 address.
                 */
                addresses = geocoder.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
            } catch (IOException e1) {
            Log.e("LocationSampleActivity",
                    "IO Exception in getFromLocation()");
            e1.printStackTrace();
            return ("IO Exception trying to get address");
            } catch (IllegalArgumentException e2) {
            // Error message to post in the log
            String errorString = "Illegal arguments " +
                    Double.toString(loc.getLatitude()) +
                    " , " +
                    Double.toString(loc.getLongitude()) +
                    " passed to address service";
            Log.e("LocationSampleActivity", errorString);
            e2.printStackTrace();
            return errorString;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
                String addressText = String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName());
                // Return the text
                return addressText;
            } else {
                return "No address found";
            }
        }
}
