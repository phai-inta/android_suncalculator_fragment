package au.edu.swin.sdmd.suncalculatorjava;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Spinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import au.edu.swin.sdmd.suncalculatorjava.calc.AstronomicalCalendar;
import au.edu.swin.sdmd.suncalculatorjava.calc.GeoLocation;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private Spinner spinner;
    private TextView textView;
    private String city = "Melbourne";
    private Bundle bundle;
    private int day, month, year;
    private TableFragment tableFragment;
    private ShareActionProvider shareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();

        //sets current date
        DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
        year = datePicker.getYear();
        month = datePicker.getMonth()+1;
        day = datePicker.getDayOfMonth();

        //add toolbar and spinner (drop-down list)
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        spinner = (Spinner) findViewById(R.id.spinner);
        textView = (TextView) findViewById(R.id.locationTV);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.cities));

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //attach data adapter to spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //get the selected city and display
                String item = adapterView.getItemAtPosition(i).toString();
                textView = (TextView) findViewById(R.id.locationTV);
                textView.setText(item);
                city = item;

                //when the city changed, should set the date to the current one
                initializeUI();

                //save new data to be used by the fragment
                FragmentManager fm = getSupportFragmentManager();
                tableFragment = (TableFragment) fm.findFragmentById(R.id.timeTableFragment);
                bundle = new Bundle();
                bundle.putString("city", city);
                bundle.putInt("day", day);
                bundle.putInt("month", month);
                bundle.putInt("year", year);
                tableFragment.setArguments(bundle);
                setSharedIntent();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        setSharedIntent();
        return true;
    }

    public void setSharedIntent() {
        Intent sharedIntent = new Intent(Intent.ACTION_SEND);
        TextView sunriseTV = findViewById(R.id.sunriseTimeTV);
        TextView sunsetTV = findViewById(R.id.sunsetTimeTV);
        TextView c = findViewById(R.id.locationTV);
        sharedIntent.setType("text/plain");

        DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
        int y = datePicker.getYear();
        int m = datePicker.getMonth()+1;
        int d = datePicker.getDayOfMonth();

        String subject = "Sunrise/set details";
        String info = "City: " + c.getText() + ", Date: " + d
                + ", Month: " + m + ", Year: " + y
                + ", Sunrise: " + sunriseTV.getText()
                + ", Sunset: " + sunsetTV.getText();

        sharedIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sharedIntent.putExtra(Intent.EXTRA_TEXT, info);
        if (shareActionProvider != null) {
            Log.d( "here_shared: ", info);
            shareActionProvider.setShareIntent(sharedIntent);
        }
    }

    private void initializeUI() {
        DatePicker dp = findViewById(R.id.datePicker);
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

        //initialise the table fragment
        FragmentManager fm = getSupportFragmentManager(); //get fragment manager object
        FragmentTransaction fragmentTransaction = fm.beginTransaction(); //begin transaction
        fragmentTransaction.replace(R.id.timeTableFragment, TableFragment.newInstance(year, month, day, city));
        fragmentTransaction.commit();

        dp.init(year,month,day,dateChangeHandler); // setup initial values and reg. handler
        updateTime(year, month, day);
    }

    private void updateTime(int year, int month, int dayOfMonth) {
        TimeZone tz = TimeZone.getDefault();
        GeoLocation geolocation = new GeoLocation("Melbourne", -37.50, 145.01, tz);
        AstronomicalCalendar ac = new AstronomicalCalendar(geolocation);
        ac.getCalendar().set(year, month, dayOfMonth);
        Date srise = ac.getSunrise();
        Date sset = ac.getSunset();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        TextView sunriseTV = findViewById(R.id.sunriseTimeTV);
        TextView sunsetTV = findViewById(R.id.sunsetTimeTV);
        sunriseTV.setText(sdf.format(srise));
        sunsetTV.setText(sdf.format(sset));
    }

    DatePicker.OnDateChangedListener dateChangeHandler = new DatePicker.OnDateChangedListener()
    {
        public void onDateChanged(DatePicker dp, int year, int month, int day)
        {
            updateTime(year, month, day);
            //modify table fragment once date selected
            TableFragment tableFragment = new TableFragment();
            FragmentManager fm = getSupportFragmentManager(); //get fragment manager object
            FragmentTransaction fragmentTransaction = fm.beginTransaction(); //begin transaction
            fragmentTransaction.replace(R.id.timeTableFragment, tableFragment); //replace the layout holder with the required fragment object
            fragmentTransaction.commit();

            bundle = new Bundle();
            bundle.putString("city", city);
            bundle.putInt("day", day);
            bundle.putInt("month", month);
            bundle.putInt("year", year);
            tableFragment.setArguments(bundle);
            Log.d("here_datePicker: ", city+"/"+day+"/"+month+"/"+ year);
            setSharedIntent();
        }
    };
}
