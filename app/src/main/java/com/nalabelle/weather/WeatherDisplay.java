package com.nalabelle.weather;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arcusweather.forecastio.ForecastIO;
import com.arcusweather.forecastio.ForecastIODataPoint;
import com.arcusweather.forecastio.ForecastIOResponse;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WeatherDisplay.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WeatherDisplay#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeatherDisplay extends Fragment {
    public static final String LAT = "mLat";
    public static final String LON = "mLon";

    private double mLat;
    private double mLon;

    private boolean modelAAT;
    private boolean modelWC;
    private double speed = 26.8224;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mLat Latitude from Google Location API.
     * @param mLon Longitude from the same.
     * @return A new instance of fragment WeatherDisplay.
     */
    public static WeatherDisplay newInstance(double mLat, double mLon) {
        WeatherDisplay fragment = new WeatherDisplay();
        Bundle args = new Bundle();
        args.putDouble(LAT, mLat);
        args.putDouble(LON, mLon);
        fragment.setArguments(args);
        return fragment;
    }

    public WeatherDisplay() {
        // Required empty public constructor
    }

    /**
     * Triggers when the fragment is created, initializes default settings.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get our LAT and LON.
        if (getArguments() != null) {
            mLat = getArguments().getDouble(LAT);
            mLon = getArguments().getDouble(LON);
        }
        setUpPrefs();
    }

    /**
     * Get the preferences set in the settings activity.
     */
    public void setUpPrefs() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // Weather Model options, default to showing both.
        modelAAT = pref.getBoolean("pref_weathermodel1", true);
        modelWC = pref.getBoolean("pref_weathermodel2", true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        updateWeather();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather_display, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    /**
     * Runs the weather update. Grabs data from forecast.io in a separate thread.
     *
     * Triggers a callback when the data is returned.
     */
    protected void updateWeather() {
        setUpPrefs();

        Location mLastLocation = MainActivity.getLocation();

        ForecastIO FIO = new ForecastIO(MainActivity.API_KEY, mLastLocation.getLatitude(), mLastLocation.getLongitude());

        HashMap<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("units", "si");
        requestParams.put("userAgent", "Custom User Agent 1.0");
        FIO.setRequestParams(requestParams);

        new ForecastThread() {
            protected void onPostExecute(ForecastIOResponse FIOR) {
                onWeatherUpdate(FIOR);
            }
        }.execute(FIO);
    }

    /**
     * The ForecastThread sends this callback when it receives data.
     *
     *
     * @param response
     */
    private void onWeatherUpdate(ForecastIOResponse response) {
        if(response == null) {
            //well, we shouldn't get a weather update event with no response.
            return;
        }

        //alerts defaults to first alert if not given an index. (Usually there is only one alert).
        String alertDescription = response.getValue("alerts-description");

        //Get the results for today and tomorrow.
        ForecastIODataPoint today = response.getDataPoints("daily")[0];
        ForecastIODataPoint tomorrow = response.getDataPoints("daily")[1];

        //Update the UI.
        ((TextView) getView().findViewById(R.id.todayStatus))
                .setText(today.getValue("summary"));
        ((TextView) getView().findViewById(R.id.todayNormal))
                .setText(String.format("%.1f/%.1f",
                        today.getValueAsDouble("temperatureMax"),
                        today.getValueAsDouble("temperatureMin")));

        //Calculate the windchill depending on what preferences the user has chosen.
        //TODO: refactor this so it's not horribly ugly.
        String chill = "";
        if(modelAAT) chill = chill +
                String.format("AAT: %.1f/%.1f",
                        Util.getConversion(speed,
                                today.getValueAsDouble("temperatureMax"),
                                today.getValueAsDouble("humidity")),
                        Util.getConversion(speed,
                                today.getValueAsDouble("temperatureMin"),
                                today.getValueAsDouble("humidity")));
        if(modelAAT && modelWC)
            chill = chill + "\n";
        if(modelWC) chill = chill +
                String.format("WC: %.1f/%.1f",
                        Util.getAltConversion(speed, today.getValueAsDouble("temperatureMax")),
                        Util.getAltConversion(speed, today.getValueAsDouble("temperatureMin")));
        ((TextView) getView().findViewById(R.id.todayChill)).setText(chill);

        ((TextView) getView().findViewById(R.id.tomorrowStatus))
                .setText(tomorrow.getValue("summary"));
        ((TextView) getView().findViewById(R.id.tomorrowNormal))
                .setText(String.format("%.1f/%.1f",
                        tomorrow.getValueAsDouble("temperatureMax"),
                        tomorrow.getValueAsDouble("temperatureMin")));

        chill = "";
        if(modelAAT) chill = chill +
                String.format("AAT: %.1f/%.1f",
                        Util.getConversion(speed,
                                tomorrow.getValueAsDouble("temperatureMax"),
                                tomorrow.getValueAsDouble("humidity")),
                        Util.getConversion(speed,
                                tomorrow.getValueAsDouble("temperatureMin"),
                                tomorrow.getValueAsDouble("humidity")));
        if(modelAAT && modelWC)
            chill = chill + "\n";
        if(modelWC) chill = chill +
                String.format("WC: %.1f/%.1f",
                        Util.getAltConversion(speed, tomorrow.getValueAsDouble("temperatureMax")),
                        Util.getAltConversion(speed, tomorrow.getValueAsDouble("temperatureMin")));
        ((TextView) getView().findViewById(R.id.tomorrowChill)).setText(chill);
    }

}
