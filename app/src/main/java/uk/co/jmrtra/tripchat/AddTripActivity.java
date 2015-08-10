package uk.co.jmrtra.tripchat;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flurry.android.FlurryAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import uk.co.jmrtra.tripchat.adapter.StationsAdapter;


public class AddTripActivity extends AppCompatActivity {

    private TextView mDepartureDateTxt;
    private TextView mArrivalDateTxt;
    private TextView mDepartureTimeTxt;
    private TextView mArrivalTimeTxt;
    private AutoCompleteTextView mDepartureStationAutoTxt;
    private AutoCompleteTextView mArrivalStationAutoTxt;
    private Spinner mSpinner;
    private final static Integer TYPE_ARRIVAL = 0;
    private final static Integer TYPE_DEPARTURE = 1;
    private StationsAdapter mDepartureAdapter;
    private StationsAdapter mArrivalAdapter;
    private Calendar mDepartureCalendar;
    private Calendar mArrivalCalendar;
    private View mStationDepartureProgressBar;
    private View mStationArrivalProgressBar;
    private boolean mLoadedSuggestions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        initToolbar();

        mDepartureDateTxt = (TextView) findViewById(R.id.add_trip_departure_date_txt);
        mArrivalDateTxt = (TextView) findViewById(R.id.add_trip_arrival_date_txt);
        mDepartureTimeTxt = (TextView) findViewById(R.id.add_trip_departure_time_txt);
        mArrivalTimeTxt = (TextView) findViewById(R.id.add_trip_arrival_time_txt);
        mDepartureStationAutoTxt = (AutoCompleteTextView)
                findViewById(R.id.add_trip_station_departure_auto_txt);
        mArrivalStationAutoTxt = (AutoCompleteTextView)
                findViewById(R.id.add_trip_station_arrival_auto_txt);
        mSpinner = (Spinner)
                findViewById(R.id.add_trip_spinner);
        mStationDepartureProgressBar = findViewById(R.id.add_trip_station_departure_progress_bar);
        mStationArrivalProgressBar = findViewById(R.id.add_trip_station_arrival_progress_bar);

        mDepartureDateTxt.setOnClickListener(mDateClickListener);
        mArrivalDateTxt.setOnClickListener(mDateClickListener);

        mDepartureTimeTxt.setOnClickListener(mTimeClickListener);
        mArrivalTimeTxt.setOnClickListener(mTimeClickListener);

        mDepartureCalendar = Calendar.getInstance();
        mArrivalCalendar = Calendar.getInstance();

        initTextFields();

        initSpinner();
    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    View.OnClickListener mDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AddTripActivity.this,
                    R.style.MaterialDialogStyle);
            if (v.getId() == R.id.add_trip_departure_date_txt) {
                dialogBuilder.setTitle("Pick Departure Date");
            } else {
                dialogBuilder.setTitle("Arrival Departure Date");
            }
            View dialogView = LayoutInflater.from(AddTripActivity.this)
                    .inflate(R.layout.dialog_date, null);
            final DatePicker datePicker
                    = (DatePicker) dialogView.findViewById(R.id.add_trip_date_picker);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int day = datePicker.getDayOfMonth();
                    int month = datePicker.getMonth();
                    int year = datePicker.getYear();
                    if (v.getId() == R.id.add_trip_departure_date_txt) {
                        updateDateTextFields(day, month, year, TYPE_DEPARTURE);
                    } else {
                        updateDateTextFields(day, month, year, TYPE_ARRIVAL);
                    }

                    dialog.cancel();
                }
            });
            dialogBuilder.create().show();
        }
    };

    View.OnClickListener mTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AddTripActivity.this,
                    R.style.MaterialDialogStyle);
            if (v.getId() == R.id.add_trip_departure_time_txt) {
                dialogBuilder.setTitle("Pick Departure Time");
            } else {
                dialogBuilder.setTitle("Arrival Departure Time");
            }
            View dialogView = LayoutInflater.from(AddTripActivity.this)
                    .inflate(R.layout.dialog_time, null);
            final TimePicker timePicker
                    = (TimePicker) dialogView.findViewById(R.id.add_trip_time_picker);
            timePicker.setIs24HourView(true);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int hours = timePicker.getCurrentHour();
                    int minutes = timePicker.getCurrentMinute();
                    if (v.getId() == R.id.add_trip_departure_time_txt) {
                        updateTimeTextFields(hours, minutes, TYPE_DEPARTURE);
                    } else {
                        updateTimeTextFields(hours, minutes, TYPE_ARRIVAL);
                    }

                    dialog.cancel();
                }
            });
            dialogBuilder.create().show();
        }
    };

    private void initSpinner() {
        String[] types = getResources().getStringArray(R.array.transport_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, types);
        mSpinner.setAdapter(adapter);
    }

    private void initTextFields() {
        Calendar c = Calendar.getInstance();
        int minutes = c.get(Calendar.MINUTE);
        int hours = c.get(Calendar.HOUR_OF_DAY);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        updateTimeTextFields(hours, minutes, TYPE_DEPARTURE);
        updateTimeTextFields(hours, minutes, TYPE_ARRIVAL);
        updateDateTextFields(day, month, year, TYPE_DEPARTURE);
        updateDateTextFields(day, month, year, TYPE_ARRIVAL);

        loadStationSuggestions();

        mDepartureStationAutoTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mDepartureAdapter != null && mDepartureAdapter.selectedStation != null) {
                    mDepartureAdapter.stationIsIncorrect = !mDepartureAdapter.selectedStation
                            .getName().equals(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mArrivalStationAutoTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mArrivalAdapter != null && mArrivalAdapter.selectedStation != null) {
                    mArrivalAdapter.stationIsIncorrect = !mArrivalAdapter.selectedStation
                            .getName().equals(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void updateTimeTextFields(int hours, int minutes, int type) {
        if (type == TYPE_DEPARTURE) {
            mDepartureTimeTxt.setText(String.format("%02d:%02d", hours, minutes));
            mDepartureCalendar.set(Calendar.HOUR_OF_DAY, hours);
            mDepartureCalendar.set(Calendar.MINUTE, minutes);
        } else {
            mArrivalTimeTxt.setText(String.format("%02d:%02d", hours, minutes));
            mArrivalCalendar.set(Calendar.HOUR_OF_DAY, hours);
            mArrivalCalendar.set(Calendar.MINUTE, minutes);
        }
    }

    private void updateDateTextFields(int day, int month, int year, int type) {
        if (type == TYPE_DEPARTURE) {
            mDepartureDateTxt.setText(String.format("%02d/%02d", day, month + 1) + "/" + (year + "")
                    .substring(2, 4));
            mDepartureCalendar.set(Calendar.DAY_OF_MONTH, day);
            mDepartureCalendar.set(Calendar.MONTH, month);
            mDepartureCalendar.set(Calendar.YEAR, year);
        } else {
            mArrivalDateTxt.setText(String.format("%02d/%02d", day, month + 1) + "/" + (year + "")
                    .substring(2, 4));
            mArrivalCalendar.set(Calendar.DAY_OF_MONTH, day);
            mArrivalCalendar.set(Calendar.MONTH, month);
            mArrivalCalendar.set(Calendar.YEAR, year);
        }
    }

    private void loadStationSuggestions() {
        // Get user coordinates to find stations nearby
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location != null) {
            final double lat = location.getLatitude();
            final double lng = location.getLongitude();

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);

            mStationDepartureProgressBar.setVisibility(View.VISIBLE);
            mStationArrivalProgressBar.setVisibility(View.VISIBLE);
            mDepartureStationAutoTxt.setEnabled(false);
            mArrivalStationAutoTxt.setEnabled(false);
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    Util.URL_GET_SUGGESTIONS, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject responseJSON = new JSONObject(response);
                        if (responseJSON.getString("result").equals("success")) {
                            JSONArray stationsJSON = new JSONArray(responseJSON.getString("stations"));

                            ArrayList<Util.Station> stations = new ArrayList<>();
                            int stationsCount = stationsJSON.length();
                            for (int i = 0; i < stationsCount; i++) {
                                JSONObject stationJSON = stationsJSON.getJSONObject(i);
                                String name = stationJSON.getString("name");
                                String code = stationJSON.getString("code");
                                String lat = stationJSON.getString("lat");
                                String lng = stationJSON.getString("lng");
                                String img = stationJSON.getString("img");
                                String distance = stationJSON.getString("distance");

                                stations.add(new Util.Station(name, code, lat, lng, img, distance));
                            }

                            mDepartureAdapter = new StationsAdapter(AddTripActivity.this,
                                    R.layout.item_stations, R.id.add_trip_station_name_txt, stations);
                            mArrivalAdapter = new StationsAdapter(AddTripActivity.this,
                                    R.layout.item_stations, R.id.add_trip_station_name_txt, stations);
                            mDepartureStationAutoTxt.setAdapter(mDepartureAdapter);
                            mArrivalStationAutoTxt.setAdapter(mArrivalAdapter);

                            mLoadedSuggestions = true;
                        } else {
                            Toast.makeText(AddTripActivity.this, "Unknown server error",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        if (Util.isDebugging()) {
                            Toast.makeText(AddTripActivity.this, "JSON error: " + response,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(AddTripActivity.this, "Unknown server error",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    Util.Log(response);

                    mStationDepartureProgressBar.setVisibility(View.GONE);
                    mStationArrivalProgressBar.setVisibility(View.GONE);
                    mDepartureStationAutoTxt.setEnabled(true);
                    mArrivalStationAutoTxt.setEnabled(true);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(AddTripActivity.this, "Server error",
                            Toast.LENGTH_LONG).show();
                    Util.Log("Server error: " + error);

                    mStationDepartureProgressBar.setVisibility(View.GONE);
                    mStationArrivalProgressBar.setVisibility(View.GONE);
                    mDepartureStationAutoTxt.setEnabled(true);
                    mArrivalStationAutoTxt.setEnabled(true);
                }
            }) {
                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    if (volleyError.networkResponse != null
                            && volleyError.networkResponse.data != null) {
                        volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                    }

                    return volleyError;
                }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("lat", lat + "");
                    params.put("lng", lng + "");
                    return params;
                }
            };
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        } else {
            Toast.makeText(AddTripActivity.this, "Can't get your location, please enable GPS",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void initToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_trip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_done) {
            if (mLoadedSuggestions) {
                addTrip();
            }
            return true;
        } else {
            finish();
            return true;
        }
    }

    private String mDepartureName = null;
    private String mDepartureCode = null;
    private String mArrivalName = null;
    private String mArrivalCode = null;
    private String mImage = null;

    private void addTrip() {
        Boolean containsErrors = false;

        final String type = mSpinner.getSelectedItemPosition() + "";
        Util.Station departureStation = mDepartureAdapter.getSelectedStation();
        if (departureStation != null) {
            mDepartureName = departureStation.getName();
            mDepartureCode = departureStation.getCode();
        }
        final Long departureTimestamp = mDepartureCalendar.getTimeInMillis();
        Util.Station arrivalStation = mArrivalAdapter.getSelectedStation();
        if (arrivalStation != null) {
            mArrivalName = arrivalStation.getName();
            mArrivalCode = arrivalStation.getCode();
            mImage = arrivalStation.getImage();
        }
        final Long arrivalTimestamp = mArrivalCalendar.getTimeInMillis();

        if (mDepartureName == null || TextUtils.isEmpty(mDepartureName)) {
            Toast.makeText(this, "Departure Station is required", Toast.LENGTH_LONG).show();
            containsErrors = true;
        } else if (mDepartureAdapter.stationIsIncorrect) {
            Toast.makeText(this, "Please pick Departure Station from a drop down", Toast
                    .LENGTH_LONG).show();
            containsErrors = true;
        } else if (mArrivalName == null || TextUtils.isEmpty(mArrivalName)) {
            Toast.makeText(this, "Arrival Station is required", Toast.LENGTH_LONG).show();
            containsErrors = true;
        } else if (mArrivalAdapter.stationIsIncorrect) {
            Toast.makeText(this, "Please pick Arrival Station from a drop down", Toast.LENGTH_LONG)
                    .show();
            containsErrors = true;
        } else if (departureTimestamp > arrivalTimestamp) {
            Toast.makeText(this, "Arrival can't be before departure", Toast.LENGTH_LONG).show();
            containsErrors = true;
        } else if (departureTimestamp.equals(arrivalTimestamp)) {
            Toast.makeText(this, "Arrival can't be at the same time, as departure",
                    Toast.LENGTH_LONG).show();
            containsErrors = true;
        }


        if (!containsErrors) {
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Util.URL_ADD_TRIP,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject responseJSON = new JSONObject(response);
                                if (responseJSON.getString("result").equals("success")) {
                                    Toast.makeText(AddTripActivity.this, "Trip added",
                                            Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                } else {
                                    Util.Log("Unknown server error");
                                    Toast.makeText(AddTripActivity.this, "Unknown server error",
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                Util.Log("JSON error: " + e);
                                Toast.makeText(AddTripActivity.this, "JSON error: " + e,
                                        Toast.LENGTH_LONG).show();
                            }
                            Util.Log(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Util.Log("Server error: " + error);
                    Toast.makeText(AddTripActivity.this, "Server error: " + error,
                            Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError) {
                    if (volleyError.networkResponse != null
                            && volleyError.networkResponse.data != null) {
                        volleyError = new VolleyError(new String(volleyError.networkResponse.data));
                    }

                    return volleyError;
                }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("type", type);
                    params.put("departure_name", mDepartureName);
                    params.put("departure_code", mDepartureCode);
                    params.put("arrival_name", mArrivalName);
                    params.put("arrival_code", mArrivalCode);
                    params.put("departure_timestamp", departureTimestamp + "");
                    params.put("arrival_timestamp", arrivalTimestamp + "");
                    params.put("image", mImage);
                    return params;
                }
            };
            // Add the request to the RequestQueue.
            Volley.newRequestQueue(AddTripActivity.this).add(stringRequest);
        }
    }
}
