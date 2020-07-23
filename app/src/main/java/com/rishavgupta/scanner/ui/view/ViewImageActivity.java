package com.rishavgupta.scanner.ui.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.exifinterface.media.ExifInterface;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rishavgupta.scanner.R;
import com.rishavgupta.scanner.databinding.ActivityViewImageBinding;
import com.rishavgupta.scanner.utils.AppConstants;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ViewImageActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityViewImageBinding activityViewImageBinding;
    private File photoFile;
    private Toolbar toolbar;
    private MapView mapView;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityViewImageBinding = DataBindingUtil.setContentView(this, R.layout.activity_view_image);
        photoFile = (File) Objects.requireNonNull(getIntent().getExtras()).get(AppConstants.FILE);
        if (photoFile != null)
            activityViewImageBinding.setFile(photoFile);
        init();
        setUpMap(savedInstanceState);
        setToolbar();
    }

    private void setUpMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = activityViewImageBinding.mapView;
        mapView.getMapAsync(this);
        mapView.onCreate(mapViewBundle);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void init() {
        toolbar = activityViewImageBinding.toolbar;
    }

    private void setToolbar() {
        toolbar.setTitle("Details");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMinZoomPreference(12);
            googleMap.getUiSettings().setAllGesturesEnabled(false);
            ExifInterface exifInterface = new ExifInterface(photoFile.getPath());
            double[] latLong = exifInterface.getLatLong();
            if (latLong != null) {
                LatLng ny = new LatLng(latLong[0], latLong[1]);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(ny);
                googleMap.addMarker(markerOptions);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(ny));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
