package com.schemetryme.potrcko.SearchPlace;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.schemetryme.potrcko.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

    MainActivity activiry;
    List<HashMap<String, String >> legs;
    public ParserTask(MainActivity activity) {
        this.activiry = activity;
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        DataAdapter routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            //Log.d("ParserTask", jsonData[0].toString());
            DataParser parser = new DataParser();
            //Log.d("ParserTask", parser.toString());

            // Starts parsing data
            routes = parser.parse(jObject);
            //Log.d("ParserTask", "Executing routes");
            //Log.d("ParserTask", routes.toString());

        } catch (Exception e) {
            //Log.d("ParserTask", e.toString());
            e.printStackTrace();
        }

        legs = routes.getLegs();
        return routes.getRoutes();
    }

    public class DataAdapter{
        List<List<HashMap<String, String>>> routes;
        List<HashMap<String, String>> legs;

        public DataAdapter(List<List<HashMap<String, String>>> routes, List<HashMap<String, String>> legs){
            this.routes = routes;
            this.legs = legs;
        }

        public List<List<HashMap<String, String>>> getRoutes(){return routes;}
        public List<HashMap<String, String>> getLegs(){return legs;}
    }

    public class DataParser {

        /**
         * Receives a JSONObject and returns a list of lists containing latitude and longitude
         */
        public DataAdapter parse(JSONObject jObject) {

            List<List<HashMap<String, String>>> routes = new ArrayList<>();
            List<HashMap<String, String>> distance = new ArrayList<>();

            JSONArray jRoutes;
            JSONArray jLegs;
            JSONArray jSteps;

            try {

                jRoutes = jObject.getJSONArray("routes");

                /** Traversing all routes */
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");

                    List path = new ArrayList<>();

                    /** Traversing all legs */
                    for (int j = 0; j < jLegs.length(); j++) {

                        HashMap<String, String> dd = new HashMap<>();
                        dd.put("distance", (String) ((JSONObject) ((JSONObject) jLegs.get(j)).get("distance")).get("text"));
                        dd.put("duration", (String) ((JSONObject) ((JSONObject) jLegs.get(j)).get("duration")).get("text"));
                        distance.add(dd);

                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for (int k = 0; k < jSteps.length(); k++) {

                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            /** Traversing all points */
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("lat", Double.toString((list.get(l)).latitude));
                                hm.put("lng", Double.toString((list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
            }


            return new DataAdapter(routes, distance);
        }


        /**
         * Method to decode polyline points
         * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
         */
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points;
        List<PolylineOptions> lineOptions = new ArrayList<>();

        // Traversing through all the routes
        // TODO: 11/8/2016 Razdvojiti rute
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            PolylineOptions line = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            line.addAll(points);
            line.width(10);
            line.color(Color.RED);

            lineOptions.add(line);

            Log.d("onPostExecute", "onPostExecute lineoptions decoded");

        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            activiry.drowDestinacion(lineOptions, legs);
        } else {
            Log.d("onPostExecute", "without Polylines drawn");
        }
    }
}