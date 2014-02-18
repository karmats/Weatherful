package net.karmats.weatherful.adapter;

import java.util.Date;

import net.karmats.weatherful.R;
import net.karmats.weatherful.parse.ForecastParser;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

/**
 * List adapter that displays forecasts
 * 
 * @author mats
 * 
 */
public class ListForecastAdapter extends SimpleCursorAdapter {

    public ListForecastAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public void setViewText(TextView v, String text) {
        String formattedText = text;
        switch (v.getId()) {
        case R.id.detailFrom:
        case R.id.detailTo:
            Date d = new Date(Long.parseLong(text));
            formattedText = DateFormat.getMediumDateFormat(mContext).format(d) + " " + DateFormat.getTimeFormat(mContext).format(d);
            break;
        case R.id.detailTemparature:
            formattedText = String.valueOf(Math.round(Double.parseDouble(text)));
            SVG svgTherm = SVGParser.getSVGFromResource(v.getResources(), R.raw.thermometer_50);
            // Get a drawable from the parsed SVG and set it as the drawable for the ImageView
            Drawable symbolThermometer = svgTherm.createPictureDrawable();
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            v.setCompoundDrawablesWithIntrinsicBounds(symbolThermometer, null, null, null);
            break;
        case R.id.detailWind:
            formattedText = String.valueOf(Math.round(Double.parseDouble(text)));
            SVG svgWind = SVGParser.getSVGFromResource(v.getResources(), R.raw.wind);
            // Get a drawable from the parsed SVG and set it as the drawable for the ImageView
            Drawable symbolWind = svgWind.createPictureDrawable();
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            v.setCompoundDrawablesWithIntrinsicBounds(symbolWind, null, null, null);
            break;
        case R.id.detailPrecipitation:
            SVG svgPrecipitation = SVGParser.getSVGFromResource(v.getResources(), R.raw.umbrella);
            // Get a drawable from the parsed SVG and set it as the drawable for the ImageView
            Drawable symbolPrecipitation = svgPrecipitation.createPictureDrawable();
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            v.setCompoundDrawablesWithIntrinsicBounds(symbolPrecipitation, null, null, null);
            break;
        }
        super.setViewText(v, formattedText);
    }

    @Override
    public void setViewImage(ImageView v, String text) {
        if (v.getId() == R.id.detailSymbol) {
            Integer symbol = Integer.parseInt(text);
            SVG svg = SVGParser.getSVGFromResource(v.getResources(), ForecastParser.YrWeatherSymbol.fromId(symbol).getRawId());
            // Get a drawable from the parsed SVG and set it as the drawable for the ImageView
            Drawable symbolDrawable = svg.createPictureDrawable();
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            v.setImageDrawable(symbolDrawable);
        } else {
            super.setViewImage(v, text);
        }
    }
}
