package net.karmats.weatherful.parse.handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;

import net.karmats.weatherful.parse.ForecastParser;
import net.karmats.weatherful.util.WeatherfulUtil;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * Handler for {@link SAXParser} that parses XML yr.no. See schema See schema http://api.met.no/weatherapi/locationforecast/1.8/schema
 * 
 * @author mats
 * 
 */
public class YrSaxHandler extends DefaultHandler {

    private List<ForecastParser.Forecast> forecasts = new ArrayList<ForecastParser.Forecast>();
    private ForecastParser.Forecast currentForecast;
    private Date lastToDate;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equals("time")) {
            // According to schema when from and to are equal, it's the beginning of a one hour forecast
            String utcFrom = attributes.getValue("from");
            String utcTo = attributes.getValue("to");
            if (utcFrom.equals(utcTo)) {
                if (currentForecast != null) {
                    forecasts.add(currentForecast);
                }
                currentForecast = new ForecastParser.Forecast();
                Date date = utcStringToDate(utcTo);
                currentForecast.setDateFrom(getFromDate(date));
                currentForecast.setDateTo(date);
            }
        } else if (localName.equals("temperature") && currentForecast.getTemperature() == null) {
            currentForecast.setTemperature(Double.parseDouble(attributes.getValue("value")));
        } else if (localName.equals("windDirection") && currentForecast.getWindDirection() == null) {
            currentForecast.setWindDirection(attributes.getValue("name"));
        } else if (localName.equals("windSpeed") && currentForecast.getWindSpeed() == null) {
            currentForecast.setWindSpeed(Double.parseDouble(attributes.getValue("mps")));
        } else if (localName.equals("precipitation") && currentForecast.getPrecipitation() == null) {
            currentForecast.setPrecipitation(Double.parseDouble(attributes.getValue("value")));
        } else if (localName.equals("symbol") && currentForecast.getSymbol() == null) {
            currentForecast.setSymbol(ForecastParser.YrWeatherSymbol.fromId(Integer.parseInt(attributes.getValue("number"))));
        }
    }

    @Override
    public void endDocument() throws SAXException {
        // Add the last forecast
        forecasts.add(currentForecast);
    }

    private Date utcStringToDate(String utc) {
        DateFormat format = new SimpleDateFormat(WeatherfulUtil.ISO_8601_DATE_FORMAT, Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            return format.parse(utc);
        } catch (ParseException e) {
            Log.e("YrSaxHandler", "Failed to parse utc " + utc);
            return new Date();
        }
    }

    private Date getFromDate(Date currentToDate) {
        Date result;
        if (lastToDate == null) {
            result = (Date) currentToDate.clone();
        } else {
            result = (Date) lastToDate.clone();
        }
        lastToDate = currentToDate;
        return result;
    }

    public List<ForecastParser.Forecast> getForecasts() {
        return forecasts;
    }

}
