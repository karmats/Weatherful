package net.karmats.weatherful.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Provider for handling cached forecasts in db
 * 
 * @author mats
 * 
 */
public class ForecastProvider extends ContentProvider {

    // Version and name
    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "Weatherful.db";

    // The incoming URI matches the Forecasts URI pattern
    private static final int FORECASTS = 1;
    // The incoming URI matches the Forecast ID URI pattern
    private static final int FORECAST_ID = 2;

    // A UriMatcher instance
    private static final UriMatcher sUriMatcher;

    // Handle to a new database helper
    private ForecastHelper mDatabaseHelper;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Forecast.AUTHORITY, "forecasts", FORECASTS);
        sUriMatcher.addURI(Forecast.AUTHORITY, "forecasts/#", FORECAST_ID);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete is not implemented yet");
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        switch (sUriMatcher.match(uri)) {
        case FORECASTS:
            SQLiteDatabase writeableDB = mDatabaseHelper.getWritableDatabase();
            // Delete all and insert new values
            writeableDB.delete(Forecast.Forecasts.TABLE_NAME, null, null);
            // Add the new data
            for (ContentValues contentValues : values) {
                writeableDB.insert(Forecast.Forecasts.TABLE_NAME, null, contentValues);
            }
            return values.length;
        default:
            throw new IllegalArgumentException("Invalid uri: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case FORECASTS:
            return Forecast.Forecasts.CONTENT_TYPE;
        case FORECAST_ID:
            return Forecast.Forecasts.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Invalid uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
        case FORECASTS:
            SQLiteDatabase writeableDB = mDatabaseHelper.getWritableDatabase();
            // Add the new data
            long rowId = writeableDB.insert(Forecast.Forecasts.TABLE_NAME, null, values);
            return ContentUris.withAppendedId(Forecast.Forecasts.CONTENT_ID_URI_BASE, rowId);
        default:
            throw new IllegalArgumentException("Invalid uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        // / Creates a new helper object. Note that the database itself isn't opened until
        // something tries to access it, and it's only created if it doesn't already exist.
        mDatabaseHelper = new ForecastHelper(getContext());

        // Assumes that any failures will be reported by a thrown exception.
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (sUriMatcher.match(uri)) {
        case FORECASTS:
            SQLiteDatabase readableDb = mDatabaseHelper.getReadableDatabase();
            return readableDb.query(Forecast.Forecasts.TABLE_NAME, projection, selection, selectionArgs, null, null,
                                    sortOrder == null ? Forecast.Forecasts.DEFAULT_SORT_ORDER : sortOrder);
        default:
            throw new IllegalArgumentException("Invalid uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Update is not implemented yet");
    }

    static class ForecastHelper extends SQLiteOpenHelper {

        // SQL statements
        private static final String CREATE_DB = "CREATE TABLE " + Forecast.Forecasts.TABLE_NAME + " (" + Forecast.Forecasts._ID + " INTEGER PRIMARY KEY,"
                + Forecast.Forecasts.FROM_DATE_COLUMN + " INTEGER, " + Forecast.Forecasts.TO_DATE_COLUMN + " INTEGER, " + Forecast.Forecasts.SYMBOL_COLUMN
                + " INTEGER, " + Forecast.Forecasts.TEMPERATURE_COLUMN + " REAL, " + Forecast.Forecasts.PRECIPITATION_COLUMN + " REAL, "
                + Forecast.Forecasts.WIND_DIRECTION_COLUMN + " TEXT, " + Forecast.Forecasts.WIND_SPEED_COLUMN + " REAL " + " )";
        private static final String DELETE_ALL = "DROP TABLE IF EXISTS " + Forecast.Forecasts.TABLE_NAME;

        public ForecastHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(DELETE_ALL);
            db.execSQL(CREATE_DB);
        }

    }

}