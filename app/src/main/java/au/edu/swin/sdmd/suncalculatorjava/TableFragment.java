package au.edu.swin.sdmd.suncalculatorjava;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.TimeZone;

import au.edu.swin.sdmd.suncalculatorjava.calc.AstronomicalCalendar;
import au.edu.swin.sdmd.suncalculatorjava.calc.GeoLocation;

public class TableFragment extends Fragment {
    private Date sunrise, sunset;
    private View view;
    private String city;
    private int day, month, year;

    public TableFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TableFragment newInstance(int year, int month, int day, String city) {
        TableFragment fragment = new TableFragment();
        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);
        args.putString("city", city);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_table, container, false);
        day = getArguments().getInt("day");
        month = getArguments().getInt("month");
        year = getArguments().getInt("year", 2019);
        city = getArguments().getString("city", "Melbourne");
        //Log.d("received: ", String.valueOf(month));
        updateTimeTable(year, month, day, city);
        return view;
    }

    public void updateTimeTable(int year, int month, int day, String city) {
        int monthOftheYear = month + 1; //as datePicker counts months from 0-11
        LocalDate date = LocalDate.of(year, monthOftheYear, day);
        TimeZone tz = TimeZone.getDefault();
        double latitude = 0; //default value
        double longitude = 0;

        if (city.equalsIgnoreCase("Melbourne")) {
            latitude = -37.50;
            longitude = 145.01;
        } else if (city.equalsIgnoreCase("Sydney")) {
            latitude = -33.86;
            longitude = 151.21;
        } else if (city.equalsIgnoreCase("Brisbane")) {
            latitude = -27.47;
            longitude = 153.02;
        } else if (city.equalsIgnoreCase("Tasmania")) {
            latitude = -41.45;
            longitude = 145.97;
        }

//        Log.d( "lat_long: ", latitude + "/" + longitude);
//        Log.d( "table_received: ", Integer.valueOf(day).toString()
//                + "/" + Integer.valueOf(year).toString()
//                + "/" + Integer.valueOf(monthOftheYear).toString());

        GeoLocation geolocation = new GeoLocation(city, latitude, longitude, tz);
        AstronomicalCalendar ac = new AstronomicalCalendar(geolocation);

        //display 7 days
        for (int i = 1; i <= 7; i++) {
            LocalDate ld = date.plusDays(i - 1); //dont increase the day on the first date
            ac.getCalendar().set(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth());
            sunrise = ac.getSunrise();
            sunset = ac.getSunset();
            updateTimeTable(ld, sunrise, sunset, i);
        }
    }

    private void updateTimeTable(LocalDate day, Date sunrise, Date sunset, int i) {
        //to display date, sunrise, sunset in each row e.g date1, date2, date3
        String date = "date"+i;
        String sunriseTimeTV = "sunriseTimeTV"+i;
        String sunsetTimeTV = "sunsetTimeTV"+i;

        //find resource ids
        int dateId = getResources().getIdentifier(date, "id", getActivity().getPackageName());
        int sunriseId = getResources().getIdentifier(sunriseTimeTV, "id", getActivity().getPackageName());
        int sunsetId = getResources().getIdentifier(sunsetTimeTV, "id", getActivity().getPackageName());

        TextView dateTV = view.findViewById(dateId);
        TextView sunriseTV = view.findViewById(sunriseId);
        TextView sunsetTV = view.findViewById(sunsetId);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        dateTV.setText(day.getDayOfMonth() +"/"+ day.getMonthValue() + "/"+ day.getYear());
        sunriseTV.setText(sdf.format(sunrise));
        sunsetTV.setText(sdf.format(sunset));
    }
}