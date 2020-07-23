package com.rishavgupta.scanner.ui.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.exifinterface.media.ExifInterface;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.rishavgupta.scanner.R;
import com.rishavgupta.scanner.databinding.ActivityScanImageBinding;
import com.rishavgupta.scanner.utils.AppConstants;
import com.rishavgupta.scanner.utils.CommonUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class ScanImageActivity extends AppCompatActivity {

    private static final int ALL_PERMISSIONS = 100;
    private static final int REVIEW_INTENT = 200;
    private static final int PICK_IMAGE = 300;
    private Preview preview;
    private ActivityScanImageBinding activityScanImageBinding;
    private PreviewView previewView;
    private View cropView;
    private ImageView imgCapture;
    private ImageView imgFlash;
    private ImageView imgGallery;
    private ImageCapture imageCapture;
    private ImageView imgBack;
    private java.io.File outputDirectory;
    private double longitude;
    private double latitude;
    private static final String TAG = "ScanImageActivity";
    private static final String FILENAME_FORMAT = "yyyyMMdd HH:mm:ss:SSS";
    private String[] allPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private FlashState flashState = FlashState.AUTO;

    private enum FlashState {
        ON, OFF, AUTO
    }

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityScanImageBinding = DataBindingUtil.setContentView(this, R.layout.activity_scan_image);
        init();
        checkPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (CommonUtils.locationEnabled(this)) {
            setUpLocationListener();
        } else {
            showGpsDialog();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient != null) {
            setUpLocationListener();
        }
    }

    private void setUpLocationListener() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest()
                .setInterval(0)
                .setFastestInterval(0)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.d("LatLong", latitude + " " + longitude);
                }
            }
        };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        });
    }

    private void showGpsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Enable GPS")
                .setMessage("This is required to scan the picture")
                .setCancelable(false)
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .setPositiveButton("Enable Now", (dialog, which) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).show();
    }


    private void checkPermissions() {
        if (CommonUtils.hasPermissions(this, allPermissions)) {
            checkIfFolderExists();
        } else {
            ActivityCompat.requestPermissions(this, allPermissions, ALL_PERMISSIONS);
        }
    }

    private void checkIfFolderExists() {
        if (CommonUtils.createFolder(this)) {
            setClickListener();
            startCamera();
        }
    }

    private void init() {
        previewView = activityScanImageBinding.previewView;
        imgBack = activityScanImageBinding.imgBack;
        imgCapture = activityScanImageBinding.imgCapture;
        imgFlash = activityScanImageBinding.imgFlash;
        imgGallery = activityScanImageBinding.imgGallery;
        cropView = activityScanImageBinding.cropView;
        outputDirectory = CommonUtils.getOutputDirectory(this);
    }

    private void setClickListener() {

        imgBack.setOnClickListener(v -> finish());

        imgFlash.setOnClickListener(v -> {
            if (flashState == FlashState.OFF) {
                flashState = FlashState.ON;
                imageCapture.setFlashMode(ImageCapture.FLASH_MODE_ON);
                imgFlash.setImageDrawable(getDrawable(R.drawable.ic_flash_on_24dp));
            } else if (flashState == FlashState.ON) {
                flashState = FlashState.AUTO;
                imageCapture.setFlashMode(ImageCapture.FLASH_MODE_AUTO);
                imgFlash.setImageDrawable(getDrawable(R.drawable.ic_flash_auto_24dp));
            } else if (flashState == FlashState.AUTO) {
                flashState = FlashState.OFF;
                imageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
                imgFlash.setImageDrawable(getDrawable(R.drawable.ic_flash_off_24dp));
            }
        });

        imgCapture.setOnClickListener(v -> takePhoto());

        imgGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });
    }


    private void takePhoto() {
        if (imageCapture == null)
            return;
        java.io.File photoFile = new java.io.File(outputDirectory, "Scanned_" +
                new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        MediaActionSound sound = new MediaActionSound();
                        sound.play(MediaActionSound.SHUTTER_CLICK);
                        cropImage(BitmapFactory.decodeFile(photoFile.getAbsolutePath()), previewView, cropView, photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                    }
                });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                preview = new Preview.Builder()
                        .setTargetResolution(new Size(previewView.getWidth(), previewView.getHeight()))
                        .build();
                imageCapture = new ImageCapture.Builder()
                        .setTargetResolution(new Size(previewView.getWidth(), previewView.getHeight()))
                        .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                        .build();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                preview.setSurfaceProvider(previewView.createSurfaceProvider());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void cropImage(Bitmap bitmap, View previewView, View cropView, File photoFile) {
        Bitmap croppedBitmap = null;
        Display display = ((WindowManager) Objects.requireNonNull(getSystemService
                (Context.WINDOW_SERVICE))).getDefaultDisplay();

        if (display.getRotation() == Surface.ROTATION_0) {

            //rotate bitmap, because camera sensor usually in landscape mode
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            if (rotatedBitmap != null) {
                //calculate aspect ratio
                float aspectX = (float) rotatedBitmap.getWidth() / (float) previewView.getWidth();
                float aspectY = (float) rotatedBitmap.getHeight() / previewView.getHeight();

                //get cropView border size and position on the screen
                int x1 = cropView.getLeft();
                int y1 = cropView.getTop();

                int x2 = cropView.getWidth();
                int y2 = cropView.getHeight();

                //calculate position and size for cropping
                int cropStartX = Math.round(x1 * aspectX);
                int cropStartY = Math.round(y1 * aspectY);

                int cropWidthX = Math.round(x2 * aspectX);
                int cropHeightY = Math.round(y2 * aspectY);

                //check limits and make crop
                if (cropStartX + cropWidthX <= rotatedBitmap.getWidth() &&
                        cropStartY + cropHeightY <= rotatedBitmap.getHeight()) {
                    croppedBitmap = Bitmap.createBitmap(rotatedBitmap, cropStartX,
                            cropStartY, cropWidthX, cropHeightY);
                }

                //save result
                if (croppedBitmap != null) {
                    if (photoFile.exists())
                        //noinspection ResultOfMethodCallIgnored
                        photoFile.delete();

                    saveBitmapToFolder(croppedBitmap, photoFile);
                }
            }

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REVIEW_INTENT) {
            if (resultCode == Activity.RESULT_OK) {
                finish();
            }
        } else if (requestCode == PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    InputStream inputStream = null;
                    if (data != null) {
                        inputStream = getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
                    }
                    Bitmap photoBitmap = BitmapFactory.decodeStream(inputStream);

                    java.io.File photoFile = new java.io.File(outputDirectory, "Scanned_" +
                            new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg");
                    saveBitmapToFolder(photoBitmap, photoFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void startReviewActivity(File photoFile) {
        Intent reviewIntent = new Intent(ScanImageActivity.this, ReviewScanImageActivity.class);
        reviewIntent.putExtra(AppConstants.FILE, photoFile);
        startActivityForResult(reviewIntent, REVIEW_INTENT);
    }

    private void saveBitmapToFolder(Bitmap imageBitmap, File file) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    out
            );
            out.flush();
            out.close();
            ExifInterface exifInterface = new ExifInterface(file.getPath());
            exifInterface.setLatLong(latitude, longitude);
            exifInterface.saveAttributes();
            startReviewActivity(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == ALL_PERMISSIONS) {
            if (grantResults.length == allPermissions.length && CommonUtils.allPermissionsGranted(grantResults)) {
                startCamera();
                setClickListener();
            } else {
                showPermissionActionSnackbar();
            }
        }
    }

    private void showPermissionActionSnackbar() {
        Snackbar.make(previewView, R.string.permission_request,
                Snackbar.LENGTH_LONG).setAction(R.string.settings, view -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }).show();
    }
}
