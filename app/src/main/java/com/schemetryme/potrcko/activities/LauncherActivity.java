package com.schemetryme.potrcko.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.schemetryme.potrcko.LocalServices.MyLocalService;
import com.schemetryme.potrcko.LocalServices.User;
import com.schemetryme.potrcko.R;
import com.schemetryme.potrcko.fragments.LoginFragment;

import java.io.Serializable;

public class LauncherActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult>,
        LocationListener {

    public static final String KEY_LOCATION = "location";
    public static final String CURRENT_USER = "user";
    protected static final int REQUEST_CHECK_SETTINGS = 5;

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mLocation;

    private MyLocalService mMyLocalService;

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, LauncherActivity.class);
        return intent;
    }

    public Location getmLocation() {
        return mLocation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        ActivityCompat.requestPermissions(
                this,
                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                1);
        //ask if request was denied!

        buildGoogleApiClient();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                final LocationSettingsStates locationSettingsStates = LocationSettingsStates.fromIntent(data);
                if (resultCode == Activity.RESULT_OK) {
                    // All required changes were successfully made
                    updateLocation();
                } else {
                    // The user was asked to change settings, but chose not to
                    finishWithLocationNotAvailable();
                }
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        buildLocationSettingsRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // All location settings are satisfied. The client can initialize location
                // requests here.
                updateLocation();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    finishWithLocationNotAvailable();
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way to fix the
                // settings so we won't show the dialog.
                finishWithLocationNotAvailable();
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        stopLocationUpdates();
        mLocation = location;
        //updateUserLocation(mLocation);
        startMainActivity(mLocation);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void buildLocationSettingsRequest() {
        mLocationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> locationSettingsResult =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        locationSettingsResult.setResultCallback(this);
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        return locationRequest;
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    protected void updateLocation() {
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation != null) {
            //updateUserLocation(mLocation);
            startMainActivity(mLocation);
        } else {
            startLocationUpdates();
        }
    }
    protected void startMainActivity(Location location) {
        if (mMyLocalService.getInstance().getLogin(this) == false) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(KEY_LOCATION, location);
            intent.putExtra(CURRENT_USER, (Serializable) mMyLocalService.getInstance().getUser());
            startActivity(intent);
            finish();
        } else {
            //open login fragment and take data, no user
            LoginFragment loginFragment = new LoginFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container_login,loginFragment);
            fragmentTransaction.commit();
            //mMyLocalService.getInstance().setLogin(this, new User(), "USER_LOGIN");
        }
    }
    protected void finishWithLocationNotAvailable() {
        Toast.makeText(this, "Location is not available, please try later", Toast.LENGTH_SHORT).show();
        finish();
    }
}
