package net.karmats.weatherful.parse;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import net.karmats.weatherful.R;
import net.karmats.weatherful.exception.WeatherfulException;

public interface ForecastParser {

    /**
     * Parse a {@link InputStream} and return a list of {@link Forecast}
     * 
     * @param is
     *            The {@link InputStream} to parse
     * @return A list of {@link Forecast}
     * @throws WeatherfulException
     */
    List<Forecast> parse(InputStream is) throws WeatherfulException;

    /**
     * Entity that holds data for a forecast
     * 
     * @author mats
     * 
     */
    static class Forecast {
        private Date dateFrom;
        private Date dateTo;
        private YrWeatherSymbol symbol;
        private Double temperature;
        private Double precipitation;
        private Double windSpeed;
        private String windDirection;

        public Date getDateFrom() {
            return dateFrom;
        }

        public void setDateFrom(Date dateFrom) {
            this.dateFrom = dateFrom;
        }

        public Date getDateTo() {
            return dateTo;
        }

        public void setDateTo(Date dateTo) {
            this.dateTo = dateTo;
        }

        public YrWeatherSymbol getSymbol() {
            return symbol;
        }

        public void setSymbol(YrWeatherSymbol symbol) {
            this.symbol = symbol;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getPrecipitation() {
            return precipitation;
        }

        public void setPrecipitation(Double precipitation) {
            this.precipitation = precipitation;
        }

        public Double getWindSpeed() {
            return windSpeed;
        }

        public void setWindSpeed(Double windSpeed) {
            this.windSpeed = windSpeed;
        }

        public String getWindDirection() {
            return windDirection;
        }

        public void setWindDirection(String windDirection) {
            this.windDirection = windDirection;
        }

    }

    /**
     * Represents a weather symbol from YR. Has two fields, yrID and rawId
     * 
     * @author mats
     * 
     */
    enum YrWeatherSymbol {

        SUN(1, R.raw.sun, R.raw.sun_night, R.string.sun), 
        LIGHT_CLOUD(2, R.raw.cloud_sun, R.raw.cloud_sun_night, R.string.light_cloud), 
        PARTLY_CLOUD(3, R.raw.cloud_sun, R.raw.cloud_sun_night, R.string.partly_cloud), 
        CLOUD(4, R.raw.cloud, R.raw.cloud, R.string.cloud), 
        LIGHT_RAIN_SUN(5, R.raw.rain_sun, R.raw.rain_sun_night, R.string.light_rain_sun), 
        LIGHT_RAIN_THUNDER_SUN(6, R.raw.lightning, R.raw.lightning, R.string.rain_thunder_sun), 
        SLEET_SUN(7, R.raw.sleet_sun, R.raw.sleet_sun_night, R.string.sleet_sun), 
        SNOW_SUN(8, R.raw.snow_sun, R.raw.snow_sun_night, R.string.snow_sun), 
        LIGHT_RAIN(9, R.raw.drizzle, R.raw.drizzle, R.string.light_rain), 
        RAIN(10, R.raw.rain, R.raw.rain, R.string.rain), 
        RAIN_THUNDER(11, R.raw.lightning, R.raw.lightning, R.string.rain_thunder), 
        SLEET(12, R.raw.sleet, R.raw.sleet, R.string.sleet), 
        SNOW(13, R.raw.snow, R.raw.snow, R.string.snow), 
        SNOW_THUNDER(14, R.raw.lightning, R.raw.lightning, R.string.snow_thunder), 
        FOG(15, R.raw.fog, R.raw.fog, R.string.fog), 
        SUN_WINTER_DARKNESS(16, R.raw.sun, R.raw.sun_night, R.string.sun), 
        LIGHT_CLOUD_WINTER_DARKNESS(17, R.raw.cloud_sun, R.raw.cloud_sun_night, R.string.light_cloud), 
        LIGHT_RAIN_SUN_WINTER_DARKNESS(18, R.raw.rain_sun, R.raw.rain_sun_night, R.string.light_rain_sun), 
        SNOW_SUN_WINTER_DARKNESS(19, R.raw.snow_sun, R.raw.snow_sun_night, R.string.snow_sun), 
        SLEET_SUN_THUNDER(20, R.raw.lightning, R.raw.lightning, R.string.sleet_thunder), 
        SNOW_SUN_THUNDER(21, R.raw.lightning, R.raw.lightning, R.string.snow_thunder), 
        LIGHT_RAIN_THUNDER(22, R.raw.lightning, R.raw.lightning, R.string.rain_thunder_sun), 
        SLEET_THUNDER(23, R.raw.lightning, R.raw.lightning, R.string.sleet_thunder);

        private final int yrId;
        private final int rawId;
        private final int rawNightId;
        private final int stringId;

        private YrWeatherSymbol(int id, int rawId, int rawNightId, int stringId) {
            this.yrId = id;
            this.rawId = rawId;
            this.rawNightId = rawNightId;
            this.stringId = stringId;
        }

        public int getYrId() {
            return yrId;
        }

        public int getRawId() {
            return rawId;
        }
        
        public int getRawNightId() {
            return rawNightId;
        }

        public int getStringId() {
            return stringId;
        }

        public static YrWeatherSymbol fromId(int id) {
            for (YrWeatherSymbol symbol : YrWeatherSymbol.values()) {
                if (symbol.yrId == id) {
                    return symbol;
                }
            }
            return null;
        }
    }
}
