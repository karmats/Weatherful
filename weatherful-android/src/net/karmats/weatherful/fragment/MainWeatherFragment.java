package net.karmats.weatherful.fragment;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import net.karmats.weatherful.R;
import net.karmats.weatherful.parse.ForecastParser;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

public class MainWeatherFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this fragment.
     */
    public static final String ARG_SECTION_NUMBER = "section_number";
    private static TextView mTextView;
    private LinearLayout mMainLayout;
    private static final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            mTextView.setText(msg.obj.toString());
        };
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
        mTextView = (TextView) root.findViewById(R.id.main_weather_location);
        mMainLayout = (LinearLayout) root.findViewById(R.id.main_fragment);

        for (ForecastParser.YrWeatherSymbol sym : ForecastParser.YrWeatherSymbol.values()) {
            LinearLayout wrapper = new LinearLayout(getActivity());
            wrapper.setOrientation(LinearLayout.HORIZONTAL);
            TextView textView = new TextView(getActivity());
            textView.setText(getString(sym.getStringId()));
            textView.setPadding(0, 30, 0, 0);
            TextView textView2 = new TextView(getActivity());
            textView2.setText(sym.name());
            textView2.setPadding(20, 30, 0, 0);
            SVG svg = SVGParser.getSVGFromResource(getResources(), sym.getRawId());
            Drawable symbol = svg.createPictureDrawable();
            ImageView imageView = new ImageView(getActivity());
            imageView.setImageDrawable(symbol);
            imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            svg = SVGParser.getSVGFromResource(getResources(), sym.getRawNightId());
            symbol = svg.createPictureDrawable();
            ImageView imageView2 = new ImageView(getActivity());
            imageView2.setImageDrawable(symbol);
            imageView2.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            wrapper.addView(imageView);
            wrapper.addView(imageView2);
            wrapper.addView(textView);
            wrapper.addView(textView2);
            mMainLayout.addView(wrapper);
        }
        // Update the ui get location etc
        updateUI();
        return root;
    }

    private void updateUI() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Double lat = (double) sharedPrefs.getFloat(getString(R.string.current_lat), 1000);
        Double lon = (double) sharedPrefs.getFloat(getString(R.string.current_lon), 1000);
        if (lat == 1000 || lon == 1000) {
            LocationManager locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
            Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            new ReverseGeocodingTask(getActivity()).execute(loc);
        } else {
            updateLocationFromPrefs(sharedPrefs);
        }
    }

    // AsyncTask encapsulating the reverse-geocoding API. Since the geocoder API is blocked,
    // we do not want to invoke it from the UI thread.
    public class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
        Context mContext;

        public ReverseGeocodingTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected Void doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

            List<Address> addresses = null;
            try {
                // Call the synchronous getFromLocation() method by passing in the lat/long values.
                addresses = geocoder.getFromLocation(params[0].getLatitude(), params[0].getLongitude(), 1);
            } catch (IOException e) {
                Message.obtain(mHandler, 0, e.getMessage()).sendToTarget();
                // Try to get last known location from prefs
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                updateLocationFromPrefs(prefs);
            }
            if (addresses != null && addresses.size() > 0) {
                Address addressToUse = null;
                for (Address address : addresses) {
                    // Format the city and country name. Save to preferences
                    if (address.getLocality() != null) {
                        addressToUse = address;
                        break;
                    } else if (address.getFeatureName() != null) {
                        addressToUse = address;
                        // Note don't break, we primary want the locality (city)
                    } else if (address.getThoroughfare() != null) {
                        addressToUse = address;
                        // Note don't break, we primary want the locality (city)
                    }
                }
                saveLocation(addressToUse);
                String addressText = String.format("%s, %s", addressToUse.getLocality(), addressToUse.getCountryName());
                // Update the UI via a message handler.
                Message.obtain(mHandler, 1, addressText).sendToTarget();
            }
            return null;
        }
    }

    // Saves address to shared preference
    private void saveLocation(Address address) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.current_city), getCityName(address));
        editor.putString(getString(R.string.current_country), address.getCountryName() == null ? getString(R.string.unkown_location) : address.getCountryName());
        editor.putFloat(getString(R.string.current_lat), (float) address.getLatitude());
        editor.putFloat(getString(R.string.current_lon), (float) address.getLongitude());
        editor.apply();
    }

    // Get the presented city name as a string
    private String getCityName(Address address) {
        String city = getString(R.string.unkown_location);
        if (address.getLocality() != null) {
            city = address.getLocality();
        } else if (address.getFeatureName() != null) {
            city = address.getFeatureName();
        } else if (address.getThoroughfare() != null) {
            city = address.getThoroughfare();
        }
        return city;
    }

    private void updateLocationFromPrefs(SharedPreferences prefs) {
        String country = prefs.getString(getString(R.string.current_country), getString(R.string.unkown_location));
        String city = prefs.getString(getString(R.string.current_city), getString(R.string.unkown_location));
        String addressText = String.format("%s, %s", city, country);
        Message.obtain(mHandler, 1, addressText).sendToTarget();
    }
}
