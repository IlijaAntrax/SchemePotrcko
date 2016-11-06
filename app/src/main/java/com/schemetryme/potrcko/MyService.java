package com.schemetryme.potrcko;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.IO;
import com.google.android.gms.maps.model.LatLng;
import com.schemetryme.potrcko.LocalServices.MyLocalService;
import com.schemetryme.potrcko.ThreadPoolExecutor.DefaultExecutorSupplier;
import com.squareup.otto.Bus;

import com.schemetryme.potrcko.bus.BusProvider;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


public class MyService extends Service {

    protected Bus mBus = BusProvider.getInstance();
    private Socket mSocket;

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            mSocket = IO.socket(MyLocalService.URL);
        } catch (URISyntaxException e) {
            mSocket = null;
        }

        if (mSocket != null) {
            mSocket.connect();

            mSocket.on("location", onLocations);
            mSocket.on("changeLocation", changeLocation);
            mSocket.on("diconected", onDisconect);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();

        if (
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }

        //void requestLocationUpdates (String provider, => the name of the provider with which to register
        //            long minTime, => minimum time interval between location updates, in milliseconds
        //            float minDistance, => minimum distance between location updates, in meters
        //            LocationListener listener) => LocationListener

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 50, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 50, listener);

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate(){
        super.onCreate();
        mBus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBus.unregister(this);
        mSocket.emit("disconect");
        mSocket.disconnect();
        mSocket.off("location", onLocations);
        mSocket.off("changeLocation", changeLocation);
        mSocket.off("diconected", onDisconect);

        if (
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        &&
                ActivityCompat.checkSelfPermission(this,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {

        }

        locationManager.removeUpdates(listener);
    }

    private JSONObject getData(LatLng location){

        JSONObject obj;
        try{
            obj = new JSONObject();

            obj.put("userId", MyLocalService.getUser().get_id());
            obj.put("mail", MyLocalService.getUser().getEmail());
            obj.put("username", MyLocalService.getUser().getFirstname() + " " + MyLocalService.getUser().getLastname());
            obj.put("longitude", Double.toString(location.longitude));
            obj.put("latitude", Double.toString(location.latitude));
            obj.put("busy", Boolean.toString(MyLocalService.getUser().getBusy()));
            obj.put("radius", Double.toString(MyLocalService.getUser().getRadius()));

        }catch (JSONException e){
            e.getStackTrace();
            obj = null;
        }
        catch (Exception e){
            e.getStackTrace();
            obj = null;
        }

        return obj;
    }


    @Subscribe
    public void getLocatin(String s){
        mSocket.emit("allLocation");
    }

    private Emitter.Listener onLocations = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        mBus.post(data.getJSONArray("location"));
                    }catch (Exception e){
                        e.getStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener changeLocation = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    mBus.post(data);
                }
            });
        }
    };

    private Emitter.Listener onDisconect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    mBus.post(data.toString());
                }
            });
        }
    };

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }



    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public class MyLocationListener implements LocationListener
    {

        public void onLocationChanged(final Location location)
        {
            if(isBetterLocation(location, previousBestLocation)) {

                JSONObject obj = getData(new LatLng(location.getLatitude(), location.getLongitude()));

                if(obj != null)
                    mSocket.emit("changeLocation", obj.toString());

            }
        }

        public void onProviderDisabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
        }


        public void onProviderEnabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

    }

}
