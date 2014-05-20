package net.karmats.weatherful;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.karmats.weatherful.fragment.DetailForecastFragment;
import net.karmats.weatherful.fragment.MainWeatherFragment;
import net.karmats.weatherful.parse.ForecastParser;
import net.karmats.weatherful.parse.YrSaxParser;
import net.karmats.weatherful.provider.Forecast;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;

public class WeatherfulMainActivity extends FragmentActivity {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    /**
     * Progress bar that indicates when background jobs is running
     */
    private ProgressBar mProgressBar;

    /**
     * Search view in action bar
     */
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weatherful_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        // Initialize the progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.progress);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.weatherful_main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) searchItem.getActionView();
        setupSearchView();

        return true;
    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
        mSearchView.setSearchableInfo(info);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_refresh:
            // Fetch from yr and store to db
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            // If current location isn't saved yet, do a location based call
            Double lat = (double) sharedPrefs.getFloat(getString(R.string.current_lat), 1000);
            Double lon = (double) sharedPrefs.getFloat(getString(R.string.current_lon), 1000);
            Log.i("Main",
                  "Lat lon from shared prefs " + lat + ", " + lon + " country "
                          + sharedPrefs.getString(getString(R.string.current_country), getString(R.string.unkown_location)));
            if (lat == 1000 || lon == 1000) {
                Log.i("Main", "Couldnt find loc, searching default");
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                lat = loc.getLatitude();
                lon = loc.getLongitude();
            }
            new FetchWeatherDataTask().execute(lat, lon);
            return true;
        }
        return false;
    }

    // Toogles progress indicator
    protected void toogleProgress(boolean progress) {
        if (progress) {
            mViewPager.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mViewPager.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private MainWeatherFragment mainFragment;
        private DetailForecastFragment detailsFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
            case 0:
                if (mainFragment == null) {
                    mainFragment = new MainWeatherFragment();
                }
                return mainFragment;
            case 1:
                if (detailsFragment == null) {
                    detailsFragment = new DetailForecastFragment();
                }
                return detailsFragment;
            default:
                Log.i("SectionsPageAdapter", position + " is not valid");
                return new MainWeatherFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            case 0:
                return getString(R.string.main_fragment);
            case 1:
                return getString(R.string.detail_fragment);
            }
            return null;
        }

        @Override
        public void notifyDataSetChanged() {
            getContentResolver().notifyChange(Forecast.Forecasts.CONTENT_URI, null);
            if (detailsFragment != null) {
                detailsFragment.getLoaderManager().restartLoader(0, null, detailsFragment);
            }
            super.notifyDataSetChanged();
        }
    }

    /**
     * Fetches weather from yr.no and stores it to local database
     * 
     * @author mats
     * 
     */
    public class FetchWeatherDataTask extends AsyncTask<Double, Void, List<ForecastParser.Forecast>> {

        @Override
        protected List<ForecastParser.Forecast> doInBackground(Double... location) {
            try {
                String url = getResources().getString(R.string.yr_url, location[0], location[1]);
                Log.i("fetchweathertask", "Fetching weather from " + url);
                return loadDataFromNetwork(url);
            } catch (Exception e) {
                Log.e("fetchweathertask", "Failed to receive data for location " + location[0] + ", " + location[1] + " Exception is " + e);
                return new ArrayList<ForecastParser.Forecast>();
            }
        }

        @Override
        protected void onPreExecute() {
            toogleProgress(true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<ForecastParser.Forecast> result) {
            if (!result.isEmpty()) {
                getContentResolver().bulkInsert(Forecast.Forecasts.CONTENT_URI, forecastsToContentValuesArray(result));
                Log.i("fetchweathertask", "Got " + result.size() + " forecasts from yr.no");
            } else {
                Log.e("fetchweathertask", "Failed to get result from yr.no");
            }
            // Remove progress indicator and tell adapter the data has changed
            mPagerAdapter.notifyDataSetChanged();
            toogleProgress(false);
        }

        private List<ForecastParser.Forecast> loadDataFromNetwork(String url) throws Exception {
            ForecastParser parser = new YrSaxParser();
            return parser.parse(downloadXml(url));
        }

        private InputStream downloadXml(String urlString) throws IOException {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.connect();
            return connection.getInputStream();
        }

        private ContentValues[] forecastsToContentValuesArray(List<ForecastParser.Forecast> forecasts) {
            ContentValues[] result = new ContentValues[forecasts.size()];
            for (int i = 0; i < forecasts.size(); i++) {
                result[i] = forecastToContentValues(forecasts.get(i));
            }
            return result;
        }

        private ContentValues forecastToContentValues(ForecastParser.Forecast forecast) {
            ContentValues result = new ContentValues();
            result.put(Forecast.Forecasts.FROM_DATE_COLUMN, forecast.getDateFrom().getTime());
            result.put(Forecast.Forecasts.TO_DATE_COLUMN, forecast.getDateTo().getTime());
            result.put(Forecast.Forecasts.SYMBOL_COLUMN, forecast.getSymbol().getYrId());
            result.put(Forecast.Forecasts.TEMPERATURE_COLUMN, forecast.getTemperature());
            result.put(Forecast.Forecasts.PRECIPITATION_COLUMN, forecast.getPrecipitation());
            result.put(Forecast.Forecasts.WIND_DIRECTION_COLUMN, forecast.getWindDirection());
            result.put(Forecast.Forecasts.WIND_SPEED_COLUMN, forecast.getWindSpeed());
            return result;
        }
    }

}
