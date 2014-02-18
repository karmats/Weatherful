package net.karmats.weatherful.fragment;

import net.karmats.weatherful.R;
import net.karmats.weatherful.adapter.ListForecastAdapter;
import net.karmats.weatherful.provider.Forecast;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;

public class DetailForecastFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String[] from = { Forecast.Forecasts.FROM_DATE_COLUMN, Forecast.Forecasts.TO_DATE_COLUMN, Forecast.Forecasts.TEMPERATURE_COLUMN,
                Forecast.Forecasts.SYMBOL_COLUMN, Forecast.Forecasts.PRECIPITATION_COLUMN, Forecast.Forecasts.WIND_SPEED_COLUMN };
        int[] to = { R.id.detailFrom, R.id.detailTo, R.id.detailTemparature, R.id.detailSymbol, R.id.detailPrecipitation, R.id.detailWind };
        mAdapter = new ListForecastAdapter(getActivity(), R.layout.forecast_item, null, from, to, 0);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, savedInstanceState, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(getActivity(), Forecast.Forecasts.CONTENT_URI, new String[] {}, Forecast.Forecasts.TO_DATE_COLUMN + " > ?",
                                new String[] { String.valueOf(System.currentTimeMillis()) }, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

}
