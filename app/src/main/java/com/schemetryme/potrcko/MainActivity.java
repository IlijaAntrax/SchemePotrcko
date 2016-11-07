package com.schemetryme.potrcko;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.schemetryme.potrcko.SearchPlace.FetchUrl;
import com.schemetryme.potrcko.SearchPlace.PlaceProvider;
import com.schemetryme.potrcko.Services.MyLocationService;
import com.schemetryme.potrcko.ThreadPoolExecutor.DefaultExecutorSupplier;
import com.schemetryme.potrcko.bus.BusProvider;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor> {

    protected Location mMyLocation;
    protected GoogleMap mGoogleMap;
    protected Marker mMyMarker = null;
    HashMap<String, Marker> markers = new HashMap<>();

    ArrayList<LatLng> MarkerPoints = new ArrayList<>();

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

        handleIntent(getIntent());
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
        if (id == R.id.action_search) {
            onSearchRequested();
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

        //Start location services
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                startService(new Intent(getApplicationContext(), MyLocationService.class));
            }
        });

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

    private void handleIntent(Intent intent) {
        try {
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                doSearch(intent.getStringExtra(SearchManager.QUERY));
            } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                getPlace(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
            }
        }catch (Exception e){
            e.getStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }


    private void doSearch(String query) {
        Bundle data = new Bundle();
        data.putString("query", query);
        getSupportLoaderManager().restartLoader(0, data, this);
    }

        private void getPlace(String query) {
            Bundle data = new Bundle();
            data.putString("query", query);
            getSupportLoaderManager().restartLoader(1, data, this);
        }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle query) {
        CursorLoader cLoader = null;
        if (arg0 == 0)
            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.SEARCH_URI, null, null, new String[]{query.getString("query")}, null);
        else if (arg0 == 1)
            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.DETAILS_URI, null, null, new String[]{query.getString("query")}, null);
        return cLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        showLocations(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub
     }

    private void showLocations(Cursor c) {
        MarkerOptions markerOptions = null;
        LatLng point = null;
        mGoogleMap.clear();
        while (c.moveToNext()) {
            markerOptions = new MarkerOptions();
            point = new LatLng(Double.parseDouble(c.getString(1)), Double.parseDouble(c.getString(2)));

            /*markerOptions.position(point);
            markerOptions.title(c.getString(0));
            mGoogleMap.addMarker(markerOptions);*/

            // Already two locations
            /*
            if (MarkerPoints.size() > 1) {
                MarkerPoints.clear();
                mGoogleMap.clear();
            }
            */
            // Adding new item to the ArrayList
            MarkerPoints.add(point);

            // Creating MarkerOptions
            MarkerOptions options = new MarkerOptions();

            // Setting the position of the marker
            options.position(point);

            /**
             * For the start location, the color of marker is GREEN and
             * for the end location, the color of marker is RED.
             */
            if (MarkerPoints.size() == 1) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else if (MarkerPoints.size() == 2) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }


            // Add new marker to the Google Map Android API V2
            mGoogleMap.addMarker(options);

            // Checks, whether start and end locations are captured
            if (MarkerPoints.size() >= 2) {
                LatLng origin = MarkerPoints.get(0);
                LatLng dest = MarkerPoints.get(1);

                // Getting URL to the Google Directions API
                String url = makeURL(origin.latitude, origin.longitude, dest.latitude, dest.longitude);
                Log.d("onMapClick", url.toString());
                FetchUrl FetchUrl = new FetchUrl(this);

                // Start downloading json data from Google Directions API
                FetchUrl.execute(url);
                //move map camera
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            }
        }
        /*
        if (point != null) {
            CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(point);
            mGoogleMap.animateCamera(cameraPosition);
        }
        */
    }

    public void drowDestinacion(PolylineOptions lineOptions){
        mGoogleMap.addPolyline(lineOptions);
    }

    public String makeURL (double sourcelat, double sourcelog, double destlat, double destlog ){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString( sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString( destlat));
        urlString.append(",");
        urlString.append(Double.toString( destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=AIzaSyA1rRMJk1YzVzBZTvWNVYSqgZbWy68yYHg");
        return urlString.toString();
    }
}
