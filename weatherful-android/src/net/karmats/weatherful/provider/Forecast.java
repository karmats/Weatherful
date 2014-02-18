package net.karmats.weatherful.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class Forecast {
    public static final String AUTHORITY = "net.karmats.weatherful.provider.ForecastProvider";

    // Not initatable
    private Forecast() {
    }

    public static abstract class Forecasts implements BaseColumns {

        // Not initatable
        private Forecasts() {
        }

        // Name of the table
        public static final String TABLE_NAME = "forecast";

        // The scheme part for this provider's URI
        private static final String SCHEME = "content://";
        // Path to forecast URI
        private static final String PATH_FORECAST = "/forecasts";

        // Path part for the Forecast ID URI
        private static final String PATH_NOTE_ID = "/forecasts/";

        // The content:// style URL for this table
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_FORECAST);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);

        // The MIME type of {@link #CONTENT_URI} providing a directory of forecasts.
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.net.karmats.provider.forecast";
        // The MIME type of {@link #CONTENT_URI} providing a directory of one forecast.
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.net.karmats.provider.forecast";

        // Default sort order for the table
        public static final String DEFAULT_SORT_ORDER = "from_date ASC";

        // Columns
        public static final String FROM_DATE_COLUMN = "from_date";
        public static final String TO_DATE_COLUMN = "to_date";
        public static final String SYMBOL_COLUMN = "symbol";
        public static final String TEMPERATURE_COLUMN = "temperature";
        public static final String PRECIPITATION_COLUMN = "precipitation";
        public static final String WIND_SPEED_COLUMN = "wind_speed";
        public static final String WIND_DIRECTION_COLUMN = "wind_direction";

    }

}
