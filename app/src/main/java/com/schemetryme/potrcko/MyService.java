package com.schemetryme.potrcko;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.IO;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.schemetryme.potrcko.LocalServices.MyLocalService;
import com.schemetryme.potrcko.ThreadPoolExecutor.DefaultExecutorSupplier;
import com.squareup.otto.Bus;

import com.schemetryme.potrcko.bus.BusProvider;
import com.squareup.otto.Subscribe;

import org.json.JSONObject;

import java.net.URISyntaxException;


public class MyService extends Service implements LocationSource.OnLocationChangedListener{

    protected Bus mBus = BusProvider.getInstance();
    private Socket mSocket;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            mSocket = IO.socket(MyLocalService.URL);
        } catch (URISyntaxException e) {
            mSocket = null;
        }

        if(mSocket != null) {
            mSocket.connect();

            mSocket.on("location", onLocations);
            mSocket.on("changeLocation", changeLocation);
            mSocket.on("diconected", onDisconect);
        }

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

        }catch (Exception e){
            e.getStackTrace();
            obj = null;
        }

        return obj;
    }

    @Override
    public void onLocationChanged (Location location){
        JSONObject obj = getData(new LatLng(location.getLatitude(), location.getLongitude()));

        if(obj != null)
            mSocket.emit("changeLocation", obj.toString());
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
}
