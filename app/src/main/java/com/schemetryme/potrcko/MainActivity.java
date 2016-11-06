package com.schemetryme.potrcko;

import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.schemetryme.potrcko.ThreadPoolExecutor.DefaultExecutorSupplier;
import com.schemetryme.potrcko.bus.BusProvider;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback{

    protected Location mMyLocation;
    protected GoogleMap mGoogleMap;
    protected Marker mMyMarker = null;
    HashMap<String, Marker> markers = new HashMap<>();

    Bus mBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(this);

        mBus = BusProvider.getInstance();

        mMyLocation = getIntent().getParcelableExtra(LauncherActivity.KEY_LOCATION);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_search) {
            // Handle the camera action
        }  else if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_extra) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;

        CameraPosition position = new CameraPosition.Builder().
                target(LoadMyPosition(mMyLocation)).zoom(16).bearing(19).tilt(30).build();
        //_googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

        //googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        setMyLocation(mMyLocation);
        mBus.post("string");
    }

    private LatLng LoadMyPosition(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng myPosition = new LatLng(latitude, longitude);
        return myPosition;
    }

    @Override
    public void onResume(){
        super.onResume();
        mBus.register(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        mBus.unregister(this);
    }

    @Subscribe
    public void setMyLocation(final Location location){

        DefaultExecutorSupplier.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                mMyLocation = location;
                final MarkerOptions mo = new
                        MarkerOptions().position(LoadMyPosition(location)).title("start");
                DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        if(mMyMarker != null)
                            mMyMarker.remove();
                        mMyMarker = mGoogleMap.addMarker(mo);
                    }
                });
            }
        });

    }

    @Subscribe
    public void setLocations(final JSONArray locations){

        DefaultExecutorSupplier.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < locations.length(); i++){
                    try{
                        final JSONObject obj = locations.getJSONObject(i);

                        final MarkerOptions marker = new MarkerOptions()
                                .position(new LatLng(new Double(obj.getString("latitude")), new Double(obj.getString("longitude"))))
                                .title(obj.getString("username"));

                        DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    markers.put(obj.getString("mail"), mGoogleMap.addMarker(marker));
                                }catch (Exception e){
                                    e.getStackTrace();
                                }
                            }
                        });


                    }catch (JSONException e){
                        e.getStackTrace();
                    }
                }
            }
        });

    }

    @Subscribe
    public void setLocation(final JSONObject obj){
        DefaultExecutorSupplier.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (markers.containsValue(obj.getString("mail"))) {
                        final Marker m = markers.get(obj.getString("mail"));
                        markers.remove(obj.getString("mail"));

                        final MarkerOptions marker = new MarkerOptions()
                                .position(new LatLng(new Double(obj.getString("latitude")), new Double(obj.getString("longitude"))))
                                .title(obj.getString("username"));
                        markers.put(obj.getString("mail"), m);
                        DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    m.remove();
                                    markers.put(obj.getString("mail"),mGoogleMap.addMarker(marker));
                                }catch (JSONException e){
                                    e.getStackTrace();
                                }
                            }
                        });

                    }
                }catch (JSONException e){
                    e.getStackTrace();
                }
            }
        });

    }

    @Subscribe
    public void userDisconect(final String str){
        if(str.equals("string")) return;
        DefaultExecutorSupplier.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                try{
                    JSONObject obj = new JSONObject(str);
                    if(markers.containsValue(obj.getString("mail"))){
                        final Marker m = markers.get(obj.getString("mail"));
                        markers.remove(obj.getString("mail"));
                        DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(new Runnable() {
                            @Override
                            public void run() {
                                m.remove();
                            }
                        });
                    }
                }catch (JSONException e){
                    e.getStackTrace();
                }
            }
        });

    }
}
