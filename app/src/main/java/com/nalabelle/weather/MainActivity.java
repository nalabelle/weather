package com.nalabelle.weather;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;


public class MainActivity extends Activity implements
        ConnectionCallbacks, OnConnectionFailedListener,
        WeatherDisplay.OnFragmentInteractionListener {

    protected static final String TAG = "basic-location-sample";
    protected static String API_KEY = "";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected static Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null) {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
        } else {
            //We need the location to get the weather for the area.
            //Once we have the location, this triggers a new fragment to replace the old one,
            //displaying the information.
            WeatherDisplay weatherFrag = new WeatherDisplay();
            Bundle args = new Bundle();
            args.putDouble(WeatherDisplay.LAT, mLastLocation.getLatitude());
            args.putDouble(WeatherDisplay.LON, mLastLocation.getLongitude());
            weatherFrag.setArguments(args);

            getFragmentManager().beginTransaction()
                    .replace(R.id.container, weatherFrag)
                    .commit();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Using fragments instead of activity requires that you manually add them to the
        //back button stack.
        if (id == R.id.action_settings) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsFragment())
                    .addToBackStack(null)
                    .commit();
            getActionBar().setDisplayHomeAsUpEnabled(true);
            return true;
        }

        if(id == android.R.id.home) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getFragmentManager().popBackStack();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        int bs = getFragmentManager().getBackStackEntryCount();
        if(bs > 0) {
            if(bs == 1) getActionBar().setDisplayHomeAsUpEnabled(false);
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected static Location getLocation() {
        return MainActivity.mLastLocation;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //todo
    }
}
