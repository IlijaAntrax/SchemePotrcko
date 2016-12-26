package com.schemetryme.potrcko.Search.User;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;

import com.schemetryme.potrcko.LocalServices.MyLocalService;
import com.schemetryme.potrcko.LocalServices.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Stefan on 12/25/2016.
 */

public class Provider extends ContentProvider  {
    

    public static final String AUTHORITY = "com.schemetryme.potrcko.Search.User.Provider";

    public static final Uri SEARCH_URI = Uri.parse("content://"+AUTHORITY+"/search");

    public static final Uri DETAILS_URI = Uri.parse("content://"+AUTHORITY+"/details");

    private static final int SEARCH = 1;
    private static final int SUGGESTIONS = 2;
    private static final int DETAILS = 3;


    private static final UriMatcher mUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // URI for "Go" button
        uriMatcher.addURI(AUTHORITY, "search", SEARCH );

        // URI for suggestions in Search Dialog
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,SUGGESTIONS);

        // URI for Details
        uriMatcher.addURI(AUTHORITY, "details",DETAILS);

        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor c = null;

        MatrixCursor mCursor = null;
        UserJSONParser parser;
        List<HashMap<String, String>> list;
        String jsonString;

        String user;

        switch(mUriMatcher.match(uri)){
            case SEARCH:


                break;
            case SUGGESTIONS :

                // Defining a cursor object with columns id, SUGGEST_COLUMN_TEXT_1, SUGGEST_COLUMN_INTENT_EXTRA_DATA
                mCursor = new MatrixCursor(new String[] { "_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA } );

                // Creating a parser object to parse places in JSON format
                parser = new UserJSONParser();

                jsonString = getUsers(selectionArgs);

                try {
                    // Parse the users ( JSON => List )
                    list = parser.parse(new JSONObject(jsonString));

                    // Creating cursor object with users
                    for(int i=0;i<list.size();i++){
                        HashMap<String, String> hMap = (HashMap<String, String>) list.get(i);

                        // Adding place details to cursor
                        mCursor.addRow(new String[] { Integer.toString(i), hMap.get("name"), hMap.get("email"), hMap.get("email") });
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }
                c = mCursor;
                break;

            case DETAILS :

                mCursor = new MatrixCursor(new String[] {"user"});

                user = getUser(selectionArgs[0]);

                mCursor.addRow(new String[] { user});

                c = mCursor;

                break;
        }
        
        
        return c;
        
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            e.getStackTrace();
            //Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    

    private String getUsers(String[] params){
        String data = "";
        String url  = "";
        try {
            url = MyLocalService.URL + "users/search?email=" + URLEncoder.encode(params[0], "utf-8");
            data = downloadUrl(url);
        }
        catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return data;
    }

    private String getUser(String param){
        String data = "";
        String url = "";
        try{
            url = MyLocalService.URL + "users/search?email=" +  URLEncoder.encode(param, "utf-8");
            data = downloadUrl(url);
        }
        catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return data;
    }
    
}
