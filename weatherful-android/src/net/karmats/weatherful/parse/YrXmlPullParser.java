package net.karmats.weatherful.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.karmats.weatherful.exception.WeatherfulException;
import net.karmats.weatherful.exception.WeatherfulException.ErrorCode;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class YrXmlPullParser implements ForecastParser {

    /**
     * Parses weather forecast from yr.no. See schema http://api.met.no/weatherapi/locationforecast/1.8/schema
     * 
     * @param is
     *            The xml as {@link InputStream}
     * @return A list of {@link Forecast}
     * @see net.karmats.weatherful.parse.ForecastParser#parse(java.io.InputStream)
     */
    @Override
    public List<Forecast> parse(InputStream is) throws WeatherfulException {
        Long start = System.currentTimeMillis();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            parser.nextTag();
            List<Forecast> result = readWeatherData(parser);
            Log.i("xmlpullparser", "Done parsing took " + (System.currentTimeMillis() - start) + " ms");
            return result;
        } catch (IOException e) {
            throw new WeatherfulException(ErrorCode.IO_ERROR, e);
        } catch (XmlPullParserException e) {
            throw new WeatherfulException(ErrorCode.PARSE_ERROR, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }

    private List<Forecast> readWeatherData(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Forecast> result = new ArrayList<Forecast>();

        parser.require(XmlPullParser.START_TAG, null, "weatherdata");
        Forecast forecast = null;
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the time tag
            if (name.equals("time")) {
                String utcFrom = parser.getAttributeValue(null, "from");
                String utcTo = parser.getAttributeValue(null, "to");
                if (utcFrom.equals(utcTo)) {
                    forecast = new Forecast();
                    forecast.setDateFrom(new Date());
                    forecast.setDateTo(new Date());
                    // in location
                    parser.nextTag();
                    readTemperatureAndWind(parser, forecast);
                } else {
                    parser.nextTag();
                    readRainAndSymbol(parser, forecast);
                    result.add(forecast);
                }
            }
        }
        return result;
    }

    private void readTemperatureAndWind(XmlPullParser parser, Forecast forecast) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "location");
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            String name = parser.getName();
            // End of location tag it's time to leave
            if (parser.getEventType() == XmlPullParser.END_TAG && name.equals("location")) {
                break;
            } else if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (name.equals("temperature")) {
                forecast.setTemperature(Double.parseDouble(readAttribute(name, "value", parser)));
                parser.next();
            } else if (name.equals("windDirection")) {
                forecast.setWindDirection(readAttribute(name, "name", parser));
                parser.next();
            } else if (name.equals("windSpeed")) {
                forecast.setWindSpeed(Double.parseDouble(readAttribute(name, "mps", parser)));
            } else {
                skip(parser);
            }
        }
    }

    private void readRainAndSymbol(XmlPullParser parser, Forecast forecast) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "location");
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            String name = parser.getName();
            if (parser.getEventType() == XmlPullParser.END_TAG && name.equals("location")) {
                break;
            } else if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (name.equals("precipitation")) {
                forecast.setPrecipitation(Double.parseDouble(readAttribute(name, "value", parser)));
                parser.next();
            } else if (name.equals("symbol")) {
                forecast.setSymbol(YrWeatherSymbol.fromId(Integer.parseInt(readAttribute(name, "number", parser))));
            } else {
                skip(parser);
            }
        }
    }

    private String readAttribute(String tag, String attribute, XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String value = parser.getAttributeValue(null, attribute);
        return value;
    }

    // Skips a tag
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                depth--;
                break;
            case XmlPullParser.START_TAG:
                depth++;
                break;
            }
        }
    }
}
