package net.karmats.weatherful;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.karmats.weatherful.service.PlaceService;
import net.karmats.weatherful.service.PlaceService.PlaceDetails;
import net.karmats.weatherful.service.json.GooglePlaceService;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class WeatherfulSearchActivity extends ListActivity {

    private ArrayAdapter<Address> mAdapter;
    private PlaceService mPlaceService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlaceService = new GooglePlaceService();
        setContentView(R.layout.weatherful_search);
        Intent queryIntent = getIntent();
        mAdapter = new ArrayAdapter<Address>(this, android.R.layout.simple_list_item_1);
        setListAdapter(mAdapter);
        doSearchQuery(queryIntent);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // Address address = mAdapter.getItem(position);
        // saveLocation(address);
        //startActivity(new Intent(WeatherfulSearchActivity.this, WeatherfulMainActivity.class));
        //finish();
    }

    @Override
    public void onNewIntent(final Intent newIntent) {
        super.onNewIntent(newIntent);
        Log.i("Search", "New search intent");
        final Intent queryIntent = getIntent();
        doSearchQuery(queryIntent);
    }

    private void doSearchQuery(Intent queryIntent) {
        String query = queryIntent.getStringExtra(SearchManager.QUERY);
        Log.i("Search", "Searching for " + query);
        if (Intent.ACTION_SEARCH.equals(queryIntent.getAction())) {
            List<Address> result = getLocations(query);
            mAdapter.addAll(result);
        } else if (Intent.ACTION_VIEW.equals(queryIntent.getAction())) {
            Intent intent = new Intent(WeatherfulSearchActivity.this, WeatherfulMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // new FetchPlaceDetailsTask().execute(query);
        }
    }

    private List<Address> getLocations(String queryParam) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Log.i("Search", "Searching places that starts with " + queryParam);
        try {
            return geocoder.getFromLocationName(queryParam, 5);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<Address>();
        }
    }

    // Saves address to shared preference TODO Think of smarter way rather than having this method in all classes...
    private void saveLocation(PlaceService.PlaceDetails details) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.current_city), details.getCity());
        editor.putString(getString(R.string.current_country),
                         details.getStateAndCountry() == null ? getString(R.string.unkown_location) : details.getStateAndCountry());
        editor.putFloat(getString(R.string.current_lat), details.getLatitude().floatValue());
        editor.putFloat(getString(R.string.current_lon), details.getLongitude().floatValue());
        editor.commit();
        Log.i("Search", "Saving location to shared prefs " + prefs.getString(getString(R.string.current_country), getString(R.string.unkown_location))
                + " lat/lon: " + prefs.getFloat(getString(R.string.current_lat), 0f) + ", " + prefs.getFloat(getString(R.string.current_lon), 0f));
    }

    private class FetchPlaceDetailsTask extends AsyncTask<String, Void, PlaceService.PlaceDetails> {

        @Override
        protected PlaceDetails doInBackground(String... params) {
            return mPlaceService.fetchDetails(params[0]);
        }

        @Override
        protected void onPostExecute(PlaceDetails result) {
            saveLocation(result);
            finish();
        }

    }
}
