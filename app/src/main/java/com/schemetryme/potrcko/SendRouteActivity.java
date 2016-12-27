package com.schemetryme.potrcko;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.schemetryme.potrcko.BottomSheet.BottomSheetBehaviorGoogleMapsLike;
import com.schemetryme.potrcko.BottomSheet.MergedAppBarLayoutBehavior;
import com.schemetryme.potrcko.LocalServices.MyLocalService;
import com.schemetryme.potrcko.LocalServices.User;
import com.schemetryme.potrcko.Search.User.Provider;
import com.schemetryme.potrcko.Search.User.UserDetalisJSONProvider;

public class SendRouteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    int[] mDrawables = {
            R.drawable.cheese_3,
            R.drawable.cheese_3,
            R.drawable.cheese_3,
            R.drawable.cheese_3,
            R.drawable.cheese_3,
            R.drawable.cheese_3
    };

    private BottomSheetBehaviorGoogleMapsLike behavior;

    private TextView    _destination;
    private TextView    _time;
    private TextView    _distance;
    private ImageButton _call;
    private ImageButton _sms;
    private ImageButton _mail;
    private TextView    _firstname;
    private TextView    _lastname;
    private TextView    _email;
    private ListView    _evauate_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_route);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(" ");
        }

        /**
         * If we want to listen for states callback
         */
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        behavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d("bottomsheet-", "STATE_HIDDEN");
                        break;
                    default:
                        Log.d("bottomsheet-", "STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        AppBarLayout mergedAppBarLayout = (AppBarLayout) findViewById(R.id.merged_appbarlayout);
        MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        mergedAppBarLayoutBehavior.setToolbarTitle("Destinacion");
        mergedAppBarLayoutBehavior.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
            }
        });


        handleIntent(getIntent());

        defineBottomSheet();

        Intent i = getIntent();
        Bundle b = i.getBundleExtra("myLocation");


    }

    private void defineBottomSheet(){
         _destination   = (TextView)    findViewById(R.id.bottom_sheet_desination);
         _time          = (TextView)    findViewById(R.id.bottom_sheet_time);
         _distance      = (TextView)    findViewById(R.id.bottom_sheet_distance);
         _call          = (ImageButton) findViewById(R.id.bottom_sheet_button_call);
         _sms           = (ImageButton) findViewById(R.id.bottom_sheet_button_sms);
         _mail          = (ImageButton) findViewById(R.id.bottom_sheet_button_mail);
        _firstname      = (TextView)    findViewById(R.id.bottom_sheet_fiestname);
         _lastname      = (TextView)    findViewById(R.id.bottom_sheet_lastname);
         _email         = (TextView)    findViewById(R.id.bottom_sheet_email);
         _evauate_list  = (ListView)    findViewById(R.id.bottom_sheet_evaluate_list);
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

    private void handleIntent(Intent intent) {
        try {
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                doSearch(intent.getStringExtra(SearchManager.QUERY));
            } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                getUser(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
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

    private void getUser(String query) {
        Bundle data = new Bundle();
        data.putString("query", query);
        getSupportLoaderManager().restartLoader(1, data, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle query) {
        CursorLoader cLoader = null;
        if (arg0 == 0)
            cLoader = new CursorLoader(getBaseContext(), Provider.SEARCH_URI, null, null, new String[]{query.getString("query")}, null);
        else if (arg0 == 1)
            cLoader = new CursorLoader(getBaseContext(), Provider.DETAILS_URI, null, null, new String[]{query.getString("query")}, null);
        return cLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        showUser(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub
    }

    private void showUser(Cursor c) {
        UserDetalisJSONProvider parser = new UserDetalisJSONProvider();
        User usr = null;
        try {
            if(c.moveToNext())
                usr = parser.parse(c.getString(0));
        }catch (Exception e){
            e.printStackTrace();
        }

        if(usr == null)
            return;

        setBottomSeetUser(usr);
    }

    private void setBottomSeetUser(User usr){
        _firstname.setText(usr.getLastname());
        _lastname.setText(usr.getLastname());
        _email.setText(usr.getEmail());

        // TODO: 12/26/2016 Button listener and evaluate

        behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED);

        ItemPagerAdapter adapter = new ItemPagerAdapter(this,mDrawables);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);
    }

}
