package com.example.stefi.remap.view.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.stefi.remap.R;
import com.example.stefi.remap.model.db.RealmController;
import com.example.stefi.remap.view.fragments.MapFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Stefi on 16.01.2017.
 */

public class NewTaskActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, LocationListener {

    private static final int MY_PERMISSION_REQUEST_LOCATION = 99;
    private static final String TAG = "DanDebug";

    private SupportMapFragment map;
    private Location mLastLocation;
    private LatLng mPinLocation = null;
    private SeekBar mRangeSelector;
    private TextView mRangeValue;
    boolean MapReady = false;
    private ImageView mCalendar;
    private ImageView mClock;
    private static EditText mDate;
    private static EditText mTime;
    private EditText mDescription;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private RealmController controller;
    private Marker pin;
    private Circle geoFenceLimits;
    InputMethodManager inputManager;
    private int range = 1000;
    private PendingIntent mGeofencePendingIntent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newtask);
        inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mCalendar = (ImageView) findViewById(R.id.calendar);
        mCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        mDate = (EditText) findViewById(R.id.input_date);

        mClock = (ImageView) findViewById(R.id.timeButton);
        mClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        mTime = (EditText) findViewById(R.id.input_time);
        mDescription = (EditText) findViewById(R.id.input_description);

        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);//remember getMap() is deprecated!

        mRangeValue = (TextView) findViewById(R.id.TaskRangeText);

        mRangeSelector = (SeekBar) findViewById(R.id.TaskRange);
        mRangeSelector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float km = (float) progress / 100;
                range = progress * 10;
                Log.i(TAG, "onProgressChanged: " + range);
                mRangeValue.setText(km + "KM");
                if (geoFenceLimits != null)
                    geoFenceLimits.setRadius((double) range);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.i(TAG, "onMapReady: ");
        MapReady = true;
        mMap = map;
        controller = new RealmController(NewTaskActivity.this);

        if (mLastLocation != null) {
            if (pin == null)
                pin = map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.
                        defaultMarker(195)).position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
            if (geoFenceLimits != null)
                geoFenceLimits.remove();
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                    .strokeColor(ContextCompat.getColor(NewTaskActivity.this, R.color.colorAccent))
                    .fillColor(ContextCompat.getColor(NewTaskActivity.this, R.color.colorAccentTransparent))
                    .radius(((double) range));
            geoFenceLimits = mMap.addCircle(circleOptions);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_LOCATION);

                return;
            } else {
                map.setMyLocationEnabled(true);
            }
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (pin != null) {
                    pin.remove();
                }

                pin = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(195))
                        .position(latLng));
                mPinLocation = latLng;

                if (geoFenceLimits != null)
                    geoFenceLimits.remove();
                CircleOptions circleOptions = new CircleOptions()
                        .center(latLng)
                        .strokeColor(ContextCompat.getColor(NewTaskActivity.this, R.color.colorAccent))
                        .fillColor(ContextCompat.getColor(NewTaskActivity.this, R.color.colorAccentTransparent))
                        .radius(((double) range));
                geoFenceLimits = mMap.addCircle(circleOptions);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Successfully gotten permission: Location");
                    try {
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                mGoogleApiClient);
                        if (mLastLocation != null) {
                            if (MapReady) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 16));
                                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(195)).anchor(0.0f, 1.0f)
                                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))); //Iasi, Romania
                            }

                            if (geoFenceLimits != null)
                                geoFenceLimits.remove();
                            CircleOptions circleOptions = new CircleOptions()
                                    .center(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                                    .strokeColor(ContextCompat.getColor(NewTaskActivity.this, R.color.colorAccent))
                                    .fillColor(ContextCompat.getColor(NewTaskActivity.this, R.color.colorAccentTransparent))
                                    .radius(((double) range));
                            geoFenceLimits = mMap.addCircle(circleOptions);
                        }
                    } catch (SecurityException e) {
                        Log.e(TAG, "onRequestPermissionsResult: Failed to get Location Permission", e);
                    }
                } else {

                }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.check:
                SaveNewTask();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void SaveNewTask() {
        if (TextUtils.isEmpty(mDescription.getText())) {
            Toast.makeText(this, "Please enter description...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(mDate.getText())) {
            Toast.makeText(this, "Please pick a date...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(mTime.getText())) {
            Toast.makeText(this, "Please pick a time...", Toast.LENGTH_SHORT).show();
            return;
        }

        long expirationMillis, dateMillis = 0;
        Calendar calendar = Calendar.getInstance();
        long currTimeMillis = calendar.getTimeInMillis();

        String string_date = mDate.getText().toString() + "-" + mTime.getText().toString();
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy-HH:mm");

        try {
            Date d = f.parse(string_date);
            dateMillis = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (dateMillis < currTimeMillis) {
            Toast.makeText(this, "Date is incorrect..", Toast.LENGTH_SHORT).show();
            return;
        } else {
            expirationMillis = dateMillis - currTimeMillis;
            controller.insertInDb(
                    mDescription.getText().toString(),
                    mDate.getText().toString(),
                    mTime.getText().toString(),
                    mPinLocation.latitude,
                    mPinLocation.longitude,
                    String.valueOf(range)
            );

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mGeofencePendingIntent = getGeofencePendingIntent();
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    createNewGeoFence(expirationMillis),
                    mGeofencePendingIntent
            ).setResultCallback(this);

            finish();
        }
    }

    public GeofencingRequest createNewGeoFence(long expirationMillis) {

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(new Geofence.Builder()
                .setRequestId(mDescription.getText().toString())
                .setCircularRegion(mPinLocation.latitude, mPinLocation.longitude, range)
                .setExpirationDuration(expirationMillis)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
        return builder.build();

    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.i(TAG, "onResult: success" + status.getResolution());
        } else {
            // inform about fail
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onLocationConnected: ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_LOCATION);
            return;
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                if (MapReady) {
                    mPinLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 16));
                    if (pin != null) {
                        pin.remove();
                    }
                    pin = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(195)).anchor(0.0f, 1.0f)
                            .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
                    if (geoFenceLimits != null)
                        geoFenceLimits.remove();
                    CircleOptions circleOptions = new CircleOptions()
                            .center(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .strokeColor(ContextCompat.getColor(NewTaskActivity.this, R.color.colorAccent))
                            .fillColor(ContextCompat.getColor(NewTaskActivity.this, R.color.colorAccentTransparent))
                            .radius(((double) range));
                    geoFenceLimits = mMap.addCircle(circleOptions);
                }
            }
            startLocationUpdates();
        }

    }

    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(NewTaskActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NewTaskActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mLastLocation != null) {
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            if (pin != null) {
                pin.remove();
            }

            pin = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(195))
                    .position(latLng));
            mPinLocation = latLng;

            if (geoFenceLimits != null)
                geoFenceLimits.remove();
            CircleOptions circleOptions = new CircleOptions()
                    .center(latLng)
                    .strokeColor(ContextCompat.getColor(NewTaskActivity.this, R.color.colorAccent))
                    .fillColor(ContextCompat.getColor(NewTaskActivity.this, R.color.colorAccentTransparent))
                    .radius(((double) range));
            geoFenceLimits = mMap.addCircle(circleOptions);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Stefi", "Mapata ne se priklucuva");
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            mDate.setText(String.format("%02d", day) + "." + String.format("%02d", (month + 1)) + "." + year);

        }

    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            mTime.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
        }
    }

    Bitmap getBitmap() {
        int px = getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
        Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mDotMarkerBitmap);
        Drawable shape = getResources().getDrawable(R.drawable.ic_pin);
        shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
        shape.draw(canvas);
        return mDotMarkerBitmap;
    }
}

