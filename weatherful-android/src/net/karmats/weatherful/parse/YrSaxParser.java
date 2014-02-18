package net.karmats.weatherful.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.karmats.weatherful.exception.WeatherfulException;
import net.karmats.weatherful.exception.WeatherfulException.ErrorCode;
import net.karmats.weatherful.parse.handler.YrSaxHandler;

import org.xml.sax.SAXException;

import android.util.Log;

/**
 * XML-parser that parses data yr.no. See schema See schema http://api.met.no/weatherapi/locationforecast/1.8/schema
 * 
 * @author mats
 * 
 */
public class YrSaxParser implements ForecastParser {

    /*
     * (non-Javadoc)
     * 
     * @see net.karmats.weatherful.parse.ForecastParser#parse(java.io.InputStream)
     */
    @Override
    public List<ForecastParser.Forecast> parse(InputStream is) throws WeatherfulException {
        SAXParser parser;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
            YrSaxHandler saxHandler = new YrSaxHandler();
            long start = System.currentTimeMillis();
            parser.parse(is, saxHandler);
            Log.i("YrSaxParser", "Parsing done took " + (System.currentTimeMillis() - start) + "ms");
            return saxHandler.getForecasts();
        } catch (ParserConfigurationException e) {
            throw new WeatherfulException(ErrorCode.PARSE_ERROR, e);
        } catch (SAXException e) {
            throw new WeatherfulException(ErrorCode.PARSE_ERROR, e);
        } catch (IOException e) {
            throw new WeatherfulException(ErrorCode.IO_ERROR, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new WeatherfulException(ErrorCode.IO_ERROR, e);
            }
        }
    }

}
