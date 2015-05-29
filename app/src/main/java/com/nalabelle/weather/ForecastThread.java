package com.nalabelle.weather;

import android.os.AsyncTask;

import com.arcusweather.forecastio.ForecastIO;
import com.arcusweather.forecastio.ForecastIOResponse;

/**
 * Separate thread for launching the Forecast.io request
 * For non-blocking updates.
 *
 * See ForecastIOLibrary
 */
public class ForecastThread extends AsyncTask<ForecastIO, Void, ForecastIOResponse> {
    public ForecastThread() {
        super();
    }

    @Override
    protected ForecastIOResponse doInBackground(ForecastIO... forecastIOs) {
        ForecastIO FIO = forecastIOs[0];
        FIO.makeRequest();
        String responseString = FIO.getResponseString();
        ForecastIOResponse FIOR = new ForecastIOResponse(responseString);
        return FIOR;
    }
}
