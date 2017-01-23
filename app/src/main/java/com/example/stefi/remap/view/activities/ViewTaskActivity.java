package com.example.stefi.remap.view.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

import java.util.Calendar;

/**
 * Created by Stefi on 16.01.2017.
 */

public class ViewTaskActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private static final String TAG = "NewTaskActivity";
    public static final String EXTRA_DATE = "date_extra";
    public static final String EXTRA_DESC = "desc_extra";
    public static final String EXTRA_TIME = "time_extra";
    public static final String EXTRA_LAT = "lat_extra";
    public static final String EXTRA_LON = "lon_extra";
    public static final String EXTRA_RADIUS = "radius_extra";

    private SupportMapFragment map;
    boolean MapReady = false;
    private EditText mDescription;
    private EditText mTime;
    private EditText mDate;
    private GoogleMap mMap;
    private RealmController controller;
    private Marker pin;
    private LatLng mLatLng;
    private String mDescriptionValue;
    private String mDateValue;
    private String mTimeValue;
    private String radius;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewtask);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        controller = new RealmController(ViewTaskActivity.this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDescription = (EditText) findViewById(R.id.input_description);
        mDescriptionValue = getIntent().getStringExtra(EXTRA_DESC);
        mDescription.setText(mDescriptionValue);

        mTime = (EditText) findViewById(R.id.input_time);
        mTimeValue = getIntent().getStringExtra(EXTRA_TIME);
        mTime.setText(mTimeValue);

        mDate = (EditText) findViewById(R.id.input_date);
        mDateValue = getIntent().getStringExtra(EXTRA_DATE);
        mDate.setText(mDateValue);

        radius = getIntent().getStringExtra(EXTRA_RADIUS);

        mLatLng = new LatLng(
                Double.parseDouble(getIntent().getStringExtra(EXTRA_LAT)),
                Double.parseDouble(getIntent().getStringExtra(EXTRA_LON))
        );


        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);//remember getMap() is deprecated!
    }
    @Override
    public void onMapReady(GoogleMap map) {
        MapReady = true;
        mMap = map;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16));
        pin = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(195))
                        .position(new LatLng(mLatLng.latitude, mLatLng.longitude)));
        drawGeofence();
        pin.setTitle(mDateValue + " - " + mTimeValue);
        pin.setSnippet(mDescriptionValue);
        pin.showInfoWindow();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_viewtask, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.check:
                DeleteTask();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void DeleteTask() {
        controller.DeleteTask(mDescriptionValue, mDateValue, mTimeValue);
        finish();
    }

    private Circle geoFenceLimits;
    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");
        if ( geoFenceLimits != null )
            geoFenceLimits.remove();
        CircleOptions circleOptions = new CircleOptions()
                .center(mLatLng)
                .strokeColor(ContextCompat.getColor(ViewTaskActivity.this, R.color.colorAccent))
                .fillColor(ContextCompat.getColor(ViewTaskActivity.this, R.color.colorAccentTransparent) )
                .radius(Double.parseDouble(radius));
        geoFenceLimits = mMap.addCircle( circleOptions );
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

