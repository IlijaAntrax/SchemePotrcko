package com.schemetryme.potrcko.Services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;


import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.IO;
import com.google.android.gms.maps.model.LatLng;
import com.schemetryme.potrcko.LocalServices.MyLocalService;
import com.schemetryme.potrcko.LocalServices.Notifications;
import com.schemetryme.potrcko.ThreadPoolExecutor.DefaultExecutorSupplier;
import com.squareup.otto.Bus;

import com.schemetryme.potrcko.bus.BusProvider;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;


public class MySocketService extends Service {

    protected Bus mBus = BusProvider.getInstance();
    private Socket mSocket;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = true;

        try {
            mSocket = IO.socket(MyLocalService.URL, options);
        } catch (URISyntaxException e) {
            mSocket = null;
        }

        if (mSocket != null) {
            mSocket.connect();

            mSocket.on("location", onLocations);
            mSocket.on("changeLocation", changeLocation);
            mSocket.on("diconected", onDisconect);
            mSocket.on("newNotification", newNotification);
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        mSocket.off("disconnect", onDisconect);
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

    @Subscribe
    public void changeLocation(Location location){
        JSONObject obj = getData(new LatLng(location.getLatitude(), location.getLongitude()));

        if(obj != null)
            mSocket.emit("changeLocation", obj.toString());
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

    private Emitter.Listener newNotification = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    HTTPNotificaition httpNotificaition = new HTTPNotificaition();
                    httpNotificaition.execute(MyLocalService.URL + "/myNotification?token=" + MyLocalService.getToken());
                }
            });
        }
    };

    public void sendNotificaiton(Notifications not){
        mBus.post(not);
    }

    private class HTTPNotificaition extends AsyncTask<String, Integer, Notifications> {
        @Override
        protected Notifications doInBackground(String... params) {
            Notifications notification = null;
            try{
                String data = downloadUrl(params[0]);
                notification = new Notifications(data);
            }catch (IOException e){
                e.getStackTrace();
            }catch (JSONException e){
                e.getStackTrace();
            }

            return notification;
        }

        @Override
        protected void onPostExecute(Notifications notifications) {
            sendNotificaiton(notifications);
        }

        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.connect();

                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                br.close();

            } catch (Exception e) {
                e.getStackTrace();
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

}
