package net.karmats.weatherful.service.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.karmats.weatherful.exception.WeatherfulException;
import net.karmats.weatherful.exception.WeatherfulException.ErrorCode;
import net.karmats.weatherful.service.PlaceService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GooglePlaceService implements PlaceService {

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete/json";
    private static final String TYPE_DETAILS = "/details/json";

    private static final String API_KEY = "AIzaSyCo86w85tpLaBCnJydCfpkW9MAWy4FDqoo";

    @Override
    public List<PlaceAutocomplete> autocompletePlace(String query) {
        List<PlaceAutocomplete> resultList = new ArrayList<PlaceAutocomplete>(0);

        String jsonResults = "";
        try {
            Locale locale = Locale.getDefault();
            // Bulild the url
            StringBuilder urlString = new StringBuilder(PLACES_API_BASE).append(TYPE_AUTOCOMPLETE);
            // No sensor (GPS etc)
            urlString.append("?sensor=false");
            // The api key
            urlString.append("&key=").append(API_KEY);
            // The language to return the results in, user language by default
            urlString.append("&language=" + locale.getLanguage());
            // Only geocoded types
            urlString.append("&types=(cities)");
            // The search string
            urlString.append("&input=" + URLEncoder.encode(query, "utf8"));
            Log.i("LocationSearch", "Going to google with search param " + urlString.toString());
            jsonResults = fetchJsonResults(urlString.toString());

        } catch (WeatherfulException e) {
            Log.e("LocationSearch", "Error processing autocomplete" + e.getErrorCode());
            return resultList;
        } catch (UnsupportedEncodingException e) {
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults);
            JSONArray jsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<PlaceAutocomplete>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject placeJson = jsonArray.getJSONObject(i);
                resultList.add(createPlace(placeJson));
            }
        } catch (JSONException e) {
            Log.e("LocationSearch", "Cannot process JSON results", e);
        }

        return resultList;
    }

    @Override
    public PlaceDetails fetchDetails(String reference) {

        String jsonResults = "";
        try {
            Locale locale = Locale.getDefault();
            // Bulild the url
            StringBuilder urlString = new StringBuilder(PLACES_API_BASE).append(TYPE_DETAILS);
            // No sensor (GPS etc)
            urlString.append("?sensor=false");
            // The api key
            urlString.append("&key=").append(API_KEY);
            // The language to return the results in, user language by default
            urlString.append("&language=").append(locale.getLanguage());
            // Only geocoded types
            urlString.append("&reference=").append(reference);
            Log.i("LocationSearch", "Going to google with search param " + urlString.toString());
            jsonResults = fetchJsonResults(urlString.toString());
        } catch (WeatherfulException e) {
            Log.e("LocationSearch", "Error when fetching details" + e.getErrorCode());
            return null;
        }
        try {
            // Create a JSON object from the results
            JSONObject jsonObj = new JSONObject(jsonResults);
            return createPlaceDetails(jsonObj);
        } catch (JSONException e) {
            Log.e("LocationSearch", "Cannot process JSON results", e);
        }
        return null;
    }

    private String fetchJsonResults(String urlString) throws WeatherfulException {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
            return jsonResults.toString();
        } catch (MalformedURLException e) {
            Log.e("LocationSearch", "Error processing Places API URL", e);
            throw new WeatherfulException(ErrorCode.MALFORMED_URL);
        } catch (IOException e) {
            Log.e("LocationSearch", "Error connecting to Places API", e);
            throw new WeatherfulException(ErrorCode.IO_ERROR);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private PlaceAutocomplete createPlace(JSONObject placeJson) throws JSONException {
        String desc = placeJson.getString("description");
        String city = desc.split(",")[0];
        String rest = desc.substring(desc.indexOf(",") + 1).trim();
        String ref = placeJson.getString("reference");
        return new PlaceAutocomplete(ref, city, rest);
    }

    private PlaceDetails createPlaceDetails(JSONObject placeDetailsJson) throws JSONException {
        JSONObject jsonResult = placeDetailsJson.getJSONObject("result");
        JSONObject location = jsonResult.getJSONObject("geometry").getJSONObject("location");
        String formattedAddress = jsonResult.getString("formatted_address");
        String city = formattedAddress.split(",")[0];
        String rest = formattedAddress.substring(formattedAddress.indexOf(",") + 1).trim();
        return new PlaceDetails(location.getDouble("lat"), location.getDouble("lng"), city, rest);
    }

}
