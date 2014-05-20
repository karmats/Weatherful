package net.karmats.weatherful.provider;

import java.util.List;

import net.karmats.weatherful.service.PlaceService;
import net.karmats.weatherful.service.json.GooglePlaceService;
import android.app.SearchManager;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

/**
 * Provider for handling location searches
 * 
 * @author mats
 * 
 */
public class LocationSearchProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "net.karmats.weatherful.provider.LocationSearchProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    private String mLastSearchQuery = "";
    private List<PlaceService.PlaceAutocomplete> mCachedPlaces;
    private PlaceService mPlaceService;

    private static final String[] COLUMNS = {
            "_id", // must include this column
            SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_QUERY,
            SearchManager.SUGGEST_COLUMN_INTENT_ACTION };

    public LocationSearchProvider() {
        mPlaceService = new GooglePlaceService();
        setupSuggestions(AUTHORITY, MODE);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String searchQuery = selectionArgs.length <= 0 ? "" : selectionArgs[0];
        Log.i("LocationSearch", "Doing search with selectionArgs " + searchQuery);
        Cursor cursor;
        if (searchQuery.isEmpty()) {
            cursor = super.query(uri, projection, selection, selectionArgs, sortOrder);
        } else {
            cursor = new MatrixCursor(COLUMNS, 10);
            // Don't do unnecessary calls to server
            if (!mLastSearchQuery.startsWith(searchQuery)) {
                mCachedPlaces = mPlaceService.autocompletePlace(searchQuery);
            }
            for (int i = 0; i < mCachedPlaces.size(); i++) {
                PlaceService.PlaceAutocomplete place = mCachedPlaces.get(i);
                ((MatrixCursor) cursor).addRow(createCursorRow(i, place.getCity(), place.getStateAndCountry(), place.getReference()));
            }
            mLastSearchQuery = searchQuery;
        }
        return cursor;
    }

    private Object[] createCursorRow(Integer id, String text1, String text2, String query) {
        return new Object[] { id, // _id
                text1, // text1
                text2, // text2
                query, "android.intent.action.VIEW" // action
        };
    }

}
