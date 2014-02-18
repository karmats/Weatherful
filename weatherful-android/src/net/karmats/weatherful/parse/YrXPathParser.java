package net.karmats.weatherful.parse;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import net.karmats.weatherful.exception.WeatherfulException;
import net.karmats.weatherful.exception.WeatherfulException.ErrorCode;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

public class YrXPathParser implements ForecastParser {

    /*
     * (non-Javadoc)
     * 
     * @see net.karmats.weatherful.parse.ForecastParser#parse(java.io.InputStream)
     */
    @Override
    public List<Forecast> parse(InputStream stream) throws WeatherfulException {
        Long start = System.currentTimeMillis();
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xpath.evaluate("//time", new InputSource(stream), XPathConstants.NODESET);
            List<Forecast> result = new ArrayList<Forecast>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                // Node node = nodeList.item(i);
                // String dateFrom = node.getAttributes().getNamedItem("from").getNodeValue();
                // String dateTo = node.getAttributes().getNamedItem("to").getNodeValue();
                Forecast forecast = new Forecast();
                forecast.setDateFrom(new Date());
                forecast.setDateTo(new Date());
                result.add(forecast);
            }
            Log.i("xpathparser", "Done parsing took " + (System.currentTimeMillis() - start) + " ms");
            return result;
        } catch (XPathException e) {
            throw new WeatherfulException(ErrorCode.PARSE_ERROR, e);
        }
    }
}
